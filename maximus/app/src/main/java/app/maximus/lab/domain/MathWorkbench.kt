package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.math.max

/** Text-driven maths tools (plotter, solver, matrices, statistics, ODEs) with all parsing kept here. */
object MathWorkbench {
    data class Sampled(val xs: List<Double>, val ys: List<Double>)

    /** Compiles an expression in the given variables; unknown names are reported as an error. */
    fun compile(text: String, vars: Set<String>): Result<Expr> = runCatching {
        val e = ExprParser.parse(text)
        val unknown = e.variables() - vars
        if (unknown.isNotEmpty()) throw ExprException("Unbekannte Variable: ${unknown.joinToString()} (erlaubt: ${vars.joinToString()})", 0)
        e
    }

    fun sample(e: Expr, a: Double, b: Double, n: Int = 400): Sampled {
        val xs = grid(a, b, n)
        val ys = xs.map { x -> runCatching { e.eval(mapOf("x" to x)) }.getOrDefault(Double.NaN) }
        return Sampled(xs, ys)
    }

    /**
     * Vertical range that ignores poles: 2nd to 98th percentile of the finite values, widened by 10 %.
     * Without this a single value near a pole (tan x at π/2) would flatten the whole plot.
     */
    fun robustRange(values: List<Double>): Pair<Double, Double>? {
        val f = values.filter { it.isFinite() }.sorted()
        if (f.isEmpty()) return null
        val lo = f[(0.02 * (f.size - 1)).toInt()]
        val hi = f[(0.98 * (f.size - 1)).toInt()]
        val span = if (hi > lo) hi - lo else max(1.0, abs(hi))
        return (lo - 0.1 * span) to (hi + 0.1 * span)
    }

    data class Analysis(val roots: List<Double>, val extrema: List<Triple<Double, Double, Boolean>>, val integral: Double?)

    fun analyze(e: Expr, a: Double, b: Double): Analysis {
        val f = { x: Double -> runCatching { e.eval(mapOf("x" to x)) }.getOrDefault(Double.NaN) }
        val roots = Calculus.roots(f, a, b, 800).take(20)
        val extrema = Calculus.extrema(f, a, b).filter { it.second.isFinite() }.take(20)
        val integral = runCatching { Calculus.integrate(f, a, b, 1e-9) }.getOrNull()?.takeIf { it.isFinite() }
        return Analysis(roots, extrema, integral)
    }

    /** Matrix from text: rows separated by newlines or ";", entries by spaces, tabs or "|". */
    fun parseMatrix(text: String): Result<Matrix> = runCatching {
        val rows = text.split('\n', ';').map { it.trim() }.filter { it.isNotEmpty() }
            .map { r -> r.split(Regex("[\\s|]+")).filter { it.isNotEmpty() }.map { tok -> Fmt.parse(tok) ?: ExprParser.parse(tok).eval(emptyMap()) } }
        require(rows.isNotEmpty()) { "Keine Einträge" }
        val n = rows.first().size
        require(rows.all { it.size == n }) { "Alle Zeilen brauchen gleich viele Einträge" }
        Matrix(rows.size, n, Array(rows.size) { rows[it].toDoubleArray() })
    }

    /** Numbers from text; returns (x, y) pairs if every non-empty line holds exactly two numbers. */
    fun parseData(text: String): Pair<List<Double>, List<Pair<Double, Double>>?> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val perLine = lines.map { l -> l.split(Regex("[\\s;|]+")).mapNotNull { Fmt.parse(it) } }
        val pairs = if (perLine.size >= 3 && perLine.all { it.size == 2 }) perLine.map { it[0] to it[1] } else null
        return perLine.flatten() to pairs
    }

    /** y' = f(t, y) (order 1) or y'' = f(t, y, v) with v = y' (order 2), integrated with RK4. */
    fun solveOde(e: Expr, order: Int, y0: Double, v0: Double, t0: Double, t1: Double, steps: Int = 1000): Ode.Solution {
        val f: (Double, DoubleArray) -> DoubleArray = if (order == 1) {
            { t, y -> doubleArrayOf(e.eval(mapOf("t" to t, "y" to y[0]))) }
        } else {
            { t, y -> doubleArrayOf(y[1], e.eval(mapOf("t" to t, "y" to y[0], "v" to y[1]))) }
        }
        val init = if (order == 1) doubleArrayOf(y0) else doubleArrayOf(y0, v0)
        return Ode.rk4(f, init, t0, t1, steps)
    }
}

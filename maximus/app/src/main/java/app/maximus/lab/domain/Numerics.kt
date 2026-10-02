package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Numerical calculus on real functions f: ℝ → ℝ. */
object Calculus {
    /**
     * First derivative by Richardson extrapolation of central differences:
     * D(h) = (f(x+h) − f(x−h)) / 2h has error c₂h² + c₄h⁴ + …, so (4D(h/2) − D(h)) / 3 removes the h² term.
     * Two extrapolation levels give O(h⁶) truncation; h is scaled with |x| to balance rounding error.
     */
    fun derivative(f: (Double) -> Double, x: Double): Double {
        val h = 1e-2 * max(1.0, abs(x))
        fun d(hh: Double) = (f(x + hh) - f(x - hh)) / (2 * hh)
        val d1 = d(h); val d2 = d(h / 2); val d3 = d(h / 4)
        val r1 = (4 * d2 - d1) / 3
        val r2 = (4 * d3 - d2) / 3
        return (16 * r2 - r1) / 15
    }

    /** Second derivative (f(x+h) − 2f(x) + f(x−h)) / h² with one Richardson step. */
    fun secondDerivative(f: (Double) -> Double, x: Double): Double {
        val h = 1e-2 * max(1.0, abs(x))
        fun d(hh: Double) = (f(x + hh) - 2 * f(x) + f(x - hh)) / (hh * hh)
        return (4 * d(h / 2) - d(h)) / 3
    }

    /** Adaptive Simpson quadrature with the Lyness correction (S₂ + (S₂ − S₁)/15); depth-limited. */
    fun integrate(f: (Double) -> Double, a: Double, b: Double, tol: Double = 1e-10): Double {
        if (a == b) return 0.0
        fun simpson(l: Double, r: Double, fl: Double, fm: Double, fr: Double) = (r - l) / 6 * (fl + 4 * fm + fr)
        fun rec(l: Double, r: Double, fl: Double, fm: Double, fr: Double, whole: Double, eps: Double, depth: Int): Double {
            val m = (l + r) / 2
            val lm = (l + m) / 2; val rm = (m + r) / 2
            val flm = f(lm); val frm = f(rm)
            val left = simpson(l, m, fl, flm, fm)
            val right = simpson(m, r, fm, frm, fr)
            val delta = left + right - whole
            return if (depth <= 0 || abs(delta) <= 15 * eps) left + right + delta / 15
            else rec(l, m, fl, flm, fm, left, eps / 2, depth - 1) + rec(m, r, fm, frm, fr, right, eps / 2, depth - 1)
        }
        // Start from 16 panels so that narrow peaks between the first three samples are not missed.
        val panels = 16
        var sum = 0.0
        for (k in 0 until panels) {
            val l = a + (b - a) * k / panels; val r = a + (b - a) * (k + 1) / panels
            val fl = f(l); val fr = f(r); val fm = f((l + r) / 2)
            sum += rec(l, r, fl, fm, fr, simpson(l, r, fl, fm, fr), tol / panels, 30)
        }
        return sum
    }

    private val GL_X = doubleArrayOf(0.0, -0.5384693101056831, 0.5384693101056831, -0.9061798459386640, 0.9061798459386640)
    private val GL_W = doubleArrayOf(0.5688888888888889, 0.4786286704993665, 0.4786286704993665, 0.2369268850561891, 0.2369268850561891)

    /** Composite 5-point Gauss–Legendre on n panels: exact for polynomials of degree ≤ 9 per panel. */
    fun gaussLegendre(f: (Double) -> Double, a: Double, b: Double, panels: Int = 64): Double {
        val h = (b - a) / panels
        var sum = 0.0
        for (k in 0 until panels) {
            val c = a + (k + 0.5) * h
            for (j in 0 until 5) sum += GL_W[j] * f(c + GL_X[j] * h / 2)
        }
        return sum * h / 2
    }

    /** Brent's method: bracketing root finder combining bisection, secant and inverse quadratic interpolation. */
    fun brent(f: (Double) -> Double, lo: Double, hi: Double, tol: Double = 1e-12, maxIter: Int = 200): Double? {
        var a = lo; var b = hi
        var fa = f(a); var fb = f(b)
        if (fa.isNaN() || fb.isNaN() || fa * fb > 0) return null
        if (abs(fa) < abs(fb)) { val t = a; a = b; b = t; val u = fa; fa = fb; fb = u }
        var c = a; var fc = fa
        var mflag = true
        var d = 0.0
        repeat(maxIter) {
            if (fb == 0.0 || abs(b - a) < tol) return b
            var s = if (fa != fc && fb != fc) {
                a * fb * fc / ((fa - fb) * (fa - fc)) + b * fa * fc / ((fb - fa) * (fb - fc)) + c * fa * fb / ((fc - fa) * (fc - fb))
            } else b - fb * (b - a) / (fb - fa)
            val cond1 = (s - (3 * a + b) / 4) * (s - b) >= 0
            val cond2 = mflag && abs(s - b) >= abs(b - c) / 2
            val cond3 = !mflag && abs(s - b) >= abs(c - d) / 2
            val cond4 = mflag && abs(b - c) < tol
            val cond5 = !mflag && abs(c - d) < tol
            if (cond1 || cond2 || cond3 || cond4 || cond5) { s = (a + b) / 2; mflag = true } else mflag = false
            val fs = f(s)
            d = c; c = b; fc = fb
            if (fa * fs < 0) { b = s; fb = fs } else { a = s; fa = fs }
            if (abs(fa) < abs(fb)) { val t = a; a = b; b = t; val u = fa; fa = fb; fb = u }
        }
        return b
    }

    /** All sign-change roots in [a, b] found on a grid of n cells and refined by Brent. */
    fun roots(f: (Double) -> Double, a: Double, b: Double, n: Int = 400): List<Double> {
        val out = ArrayList<Double>()
        var x0 = a
        var f0 = f(x0)
        for (k in 1..n) {
            val x1 = a + (b - a) * k / n
            val f1 = f(x1)
            if (f0 == 0.0) out += x0
            else if (f0.isFinite() && f1.isFinite() && f0 * f1 < 0) {
                brent(f, x0, x1)?.let { r -> if (abs(f(r)) < 1e-6 * max(1.0, abs(f0) + abs(f1))) out += r }
            }
            x0 = x1; f0 = f1
        }
        return out.distinctBy { Math.round(it * 1e9) }
    }

    /** Local extrema as roots of the numerical derivative; returns (x, f(x), isMaximum). */
    fun extrema(f: (Double) -> Double, a: Double, b: Double): List<Triple<Double, Double, Boolean>> =
        roots({ derivative(f, it) }, a, b, 300).map { x -> Triple(x, f(x), secondDerivative(f, x) < 0) }

    /** Taylor coefficients f⁽ᵏ⁾(x₀)/k! for k = 0..order by finite differences (order ≤ 4 for useful accuracy). */
    fun taylor(f: (Double) -> Double, x0: Double, order: Int = 3): List<Double> {
        val h = 1e-2 * max(1.0, abs(x0))
        val coeffs = ArrayList<Double>()
        var fact = 1.0
        for (k in 0..order) {
            if (k > 0) fact *= k
            // k-th central difference: Σ (−1)^j C(k, j) f(x₀ + (k/2 − j)h) / h^k
            var sum = 0.0
            for (j in 0..k) {
                var binom = 1.0
                for (q in 1..j) binom = binom * (k - q + 1) / q
                sum += (if (j % 2 == 0) 1 else -1) * binom * f(x0 + (k / 2.0 - j) * h)
            }
            coeffs += sum / h.pow(k) / fact
        }
        return coeffs
    }
}

/** Ordinary differential equations y' = f(t, y) with y ∈ ℝⁿ. */
object Ode {
    data class Solution(val t: List<Double>, val y: List<DoubleArray>)

    /** Classical fourth-order Runge–Kutta with fixed step h; global error O(h⁴). */
    fun rk4(f: (Double, DoubleArray) -> DoubleArray, y0: DoubleArray, t0: Double, t1: Double, steps: Int): Solution {
        val h = (t1 - t0) / steps
        val ts = ArrayList<Double>(steps + 1)
        val ys = ArrayList<DoubleArray>(steps + 1)
        var t = t0
        var y = y0.copyOf()
        ts += t; ys += y.copyOf()
        val n = y.size
        repeat(steps) {
            val k1 = f(t, y)
            val k2 = f(t + h / 2, DoubleArray(n) { y[it] + h / 2 * k1[it] })
            val k3 = f(t + h / 2, DoubleArray(n) { y[it] + h / 2 * k2[it] })
            val k4 = f(t + h, DoubleArray(n) { y[it] + h * k3[it] })
            y = DoubleArray(n) { y[it] + h / 6 * (k1[it] + 2 * k2[it] + 2 * k3[it] + k4[it]) }
            t += h
            ts += t; ys += y.copyOf()
        }
        return Solution(ts, ys)
    }

    /** Explicit Euler, kept for didactic comparison (global error O(h)). */
    fun euler(f: (Double, DoubleArray) -> DoubleArray, y0: DoubleArray, t0: Double, t1: Double, steps: Int): Solution {
        val h = (t1 - t0) / steps
        val ts = ArrayList<Double>(); val ys = ArrayList<DoubleArray>()
        var t = t0; var y = y0.copyOf()
        ts += t; ys += y.copyOf()
        repeat(steps) {
            val k = f(t, y)
            y = DoubleArray(y.size) { y[it] + h * k[it] }
            t += h
            ts += t; ys += y.copyOf()
        }
        return Solution(ts, ys)
    }

    /** Symplectic velocity Verlet for x'' = a(x); conserves a shadow Hamiltonian, so energy does not drift. */
    fun verlet(a: (Double) -> Double, x0: Double, v0: Double, dt: Double, steps: Int): Solution {
        val ts = ArrayList<Double>(); val ys = ArrayList<DoubleArray>()
        var x = x0; var v = v0; var acc = a(x)
        ts += 0.0; ys += doubleArrayOf(x, v)
        for (k in 1..steps) {
            x += v * dt + 0.5 * acc * dt * dt
            val aNew = a(x)
            v += 0.5 * (acc + aNew) * dt
            acc = aNew
            ts += k * dt; ys += doubleArrayOf(x, v)
        }
        return Solution(ts, ys)
    }
}

/** Complex number with the operations needed for impedances, roots and eigenvalues. */
data class Complex(val re: Double, val im: Double = 0.0) {
    operator fun plus(o: Complex) = Complex(re + o.re, im + o.im)
    operator fun minus(o: Complex) = Complex(re - o.re, im - o.im)
    operator fun times(o: Complex) = Complex(re * o.re - im * o.im, re * o.im + im * o.re)
    operator fun times(k: Double) = Complex(re * k, im * k)
    operator fun div(o: Complex): Complex {
        val d = o.re * o.re + o.im * o.im
        return Complex((re * o.re + im * o.im) / d, (im * o.re - re * o.im) / d)
    }
    operator fun unaryMinus() = Complex(-re, -im)
    val abs: Double get() = hypot(re, im)
    val arg: Double get() = atan2(im, re)
    val conj: Complex get() = Complex(re, -im)
    fun exp(): Complex = Complex(kotlin.math.exp(re) * cos(im), kotlin.math.exp(re) * sin(im))
    fun ln(): Complex = Complex(kotlin.math.ln(abs), arg)
    fun pow(n: Double): Complex = if (abs == 0.0) Complex(0.0) else polar(abs.pow(n), arg * n)
    fun sqrt(): Complex = pow(0.5)

    override fun toString(): String = format(4)

    fun format(digits: Int): String {
        val r = Fmt.num(re, digits)
        val i = Fmt.num(abs(im), digits)
        return when {
            abs(im) < 1e-12 * max(1.0, abs(re)) -> r
            abs(re) < 1e-12 * max(1.0, abs(im)) -> (if (im < 0) "−" else "") + i + " i"
            else -> "$r ${if (im < 0) "−" else "+"} $i i"
        }
    }

    companion object {
        val I = Complex(0.0, 1.0)
        fun polar(r: Double, phi: Double) = Complex(r * cos(phi), r * sin(phi))
    }
}

/** Number formatting with German-style decimal comma and engineering exponents for very large/small values. */
object Fmt {
    fun num(v: Double, digits: Int = 4): String {
        if (v.isNaN()) return "—"
        if (v.isInfinite()) return if (v > 0) "∞" else "−∞"
        if (v == 0.0) return "0"
        val a = abs(v)
        val s = if (a >= 1e6 || a < 1e-4) {
            val e = kotlin.math.floor(kotlin.math.log10(a)).toInt()
            val m = v / 10.0.pow(e)
            val ms = String.format(java.util.Locale.GERMANY, "%.${max(0, digits - 1)}f", m).trimEnd('0').trimEnd(',')
            "$ms·10${superscript(e)}"
        } else {
            val decimals = max(0, digits - 1 - kotlin.math.floor(kotlin.math.log10(a)).toInt())
            val t = String.format(java.util.Locale.GERMANY, "%.${min(decimals, 10)}f", v)
            if (t.contains(',')) t.trimEnd('0').trimEnd(',') else t
        }
        return s.replace('-', '−')
    }

    private const val SUP = "⁰¹²³⁴⁵⁶⁷⁸⁹"
    fun superscript(n: Int): String = (if (n < 0) "⁻" else "") + abs(n).toString().map { SUP[it - '0'] }.joinToString("")

    /** Parses user input with decimal comma or point; also accepts "1e-3" and "1,5e3". */
    fun parse(text: String): Double? = text.trim().replace(" ", "").replace('−', '-').replace(',', '.').toDoubleOrNull()
}

/** Dense real matrix with the classical algorithms of numerical linear algebra. */
class Matrix(val rows: Int, val cols: Int, val a: Array<DoubleArray> = Array(rows) { DoubleArray(cols) }) {
    operator fun get(i: Int, j: Int) = a[i][j]
    operator fun set(i: Int, j: Int, v: Double) { a[i][j] = v }

    fun copy() = Matrix(rows, cols, Array(rows) { a[it].copyOf() })

    operator fun times(o: Matrix): Matrix {
        require(cols == o.rows)
        return Matrix(rows, o.cols).also { m ->
            for (i in 0 until rows) for (k in 0 until cols) { val x = a[i][k]; if (x != 0.0) for (j in 0 until o.cols) m.a[i][j] += x * o.a[k][j] }
        }
    }

    fun transpose() = Matrix(cols, rows).also { m -> for (i in 0 until rows) for (j in 0 until cols) m.a[j][i] = a[i][j] }

    /** LU decomposition with partial pivoting: PA = LU. Returns (LU packed, permutation, sign) or null if singular. */
    fun lu(): Triple<Matrix, IntArray, Int>? {
        require(rows == cols)
        val n = rows
        val m = copy()
        val perm = IntArray(n) { it }
        var sign = 1
        for (k in 0 until n) {
            var p = k
            for (i in k + 1 until n) if (abs(m.a[i][k]) > abs(m.a[p][k])) p = i
            if (abs(m.a[p][k]) < 1e-300) return null
            if (p != k) { val t = m.a[p]; m.a[p] = m.a[k]; m.a[k] = t; val q = perm[p]; perm[p] = perm[k]; perm[k] = q; sign = -sign }
            for (i in k + 1 until n) {
                m.a[i][k] /= m.a[k][k]
                val f = m.a[i][k]
                for (j in k + 1 until n) m.a[i][j] -= f * m.a[k][j]
            }
        }
        return Triple(m, perm, sign)
    }

    fun determinant(): Double {
        val (m, _, sign) = lu() ?: return 0.0
        var d = sign.toDouble()
        for (i in 0 until rows) d *= m.a[i][i]
        return d
    }

    /** Solves Ax = b by LU; null if A is singular. */
    fun solve(b: DoubleArray): DoubleArray? {
        val (m, perm, _) = lu() ?: return null
        val n = rows
        val y = DoubleArray(n) { b[perm[it]] }
        for (i in 0 until n) for (j in 0 until i) y[i] -= m.a[i][j] * y[j]
        for (i in n - 1 downTo 0) { for (j in i + 1 until n) y[i] -= m.a[i][j] * y[j]; y[i] /= m.a[i][i] }
        return y
    }

    fun inverse(): Matrix? {
        val n = rows
        val inv = Matrix(n, n)
        for (j in 0 until n) {
            val e = DoubleArray(n).also { it[j] = 1.0 }
            val col = solve(e) ?: return null
            for (i in 0 until n) inv.a[i][j] = col[i]
        }
        return inv
    }

    fun trace(): Double = (0 until min(rows, cols)).sumOf { a[it][it] }

    /** Rank via Gaussian elimination with tolerance 1e-10 · max|aᵢⱼ|. */
    fun rank(): Int {
        val m = copy()
        val tol = 1e-10 * max(1e-300, a.maxOf { r -> r.maxOf { abs(it) } })
        var r = 0
        for (c in 0 until cols) {
            var p = -1
            for (i in r until rows) if (abs(m.a[i][c]) > tol && (p < 0 || abs(m.a[i][c]) > abs(m.a[p][c]))) p = i
            if (p < 0) continue
            val t = m.a[p]; m.a[p] = m.a[r]; m.a[r] = t
            for (i in r + 1 until rows) { val f = m.a[i][c] / m.a[r][c]; for (j in c until cols) m.a[i][j] -= f * m.a[r][j] }
            r++
            if (r == rows) break
        }
        return r
    }

    /**
     * Eigenvalues of a general real square matrix: reduction to upper Hessenberg form, then the
     * shifted QR algorithm with Wilkinson shift and deflation (complex pairs from 2×2 blocks).
     */
    fun eigenvalues(): List<Complex> {
        require(rows == cols)
        val n = rows
        val h = copy().a
        // Householder-free Hessenberg reduction by Gaussian similarity transforms with pivoting.
        for (m in 1 until n - 1) {
            var x = 0.0; var piv = m
            for (j in m until n) if (abs(h[j][m - 1]) > abs(x)) { x = h[j][m - 1]; piv = j }
            if (piv != m) {
                for (j in m - 1 until n) { val t = h[piv][j]; h[piv][j] = h[m][j]; h[m][j] = t }
                for (i in 0 until n) { val t = h[i][piv]; h[i][piv] = h[i][m]; h[i][m] = t }
            }
            if (x != 0.0) for (i in m + 1 until n) {
                var y = h[i][m - 1]
                if (y != 0.0) {
                    y /= x
                    h[i][m - 1] = y
                    for (j in m until n) h[i][j] -= y * h[m][j]
                    for (j in 0 until n) h[j][m] += y * h[j][i]
                }
            }
        }
        for (i in 2 until n) for (j in 0 until i - 1) h[i][j] = 0.0
        return hqr(h, n)
    }

    /** Francis double-shift QR on an upper Hessenberg matrix (after Numerical Recipes hqr). */
    private fun hqr(a: Array<DoubleArray>, n: Int): List<Complex> {
        val wr = DoubleArray(n); val wi = DoubleArray(n)
        var anorm = 0.0
        for (i in 0 until n) for (j in max(i - 1, 0) until n) anorm += abs(a[i][j])
        var nn = n - 1
        var t = 0.0
        var p = 0.0; var q = 0.0; var r = 0.0; var s = 0.0; var w = 0.0; var x = 0.0; var y = 0.0; var z = 0.0
        while (nn >= 0) {
            var its = 0
            var l: Int
            do {
                l = nn
                while (l >= 1) {
                    s = abs(a[l - 1][l - 1]) + abs(a[l][l])
                    if (s == 0.0) s = anorm
                    if (abs(a[l][l - 1]) + s == s) { a[l][l - 1] = 0.0; break }
                    l--
                }
                x = a[nn][nn]
                if (l == nn) {
                    wr[nn] = x + t; wi[nn] = 0.0; nn--
                } else {
                    y = a[nn - 1][nn - 1]
                    w = a[nn][nn - 1] * a[nn - 1][nn]
                    if (l == nn - 1) {
                        p = 0.5 * (y - x)
                        q = p * p + w
                        z = sqrt(abs(q))
                        x += t
                        if (q >= 0.0) {
                            z = p + if (p >= 0) z else -z
                            wr[nn - 1] = x + z; wr[nn] = wr[nn - 1]
                            if (z != 0.0) wr[nn] = x - w / z
                            wi[nn - 1] = 0.0; wi[nn] = 0.0
                        } else {
                            wr[nn - 1] = x + p; wr[nn] = x + p
                            wi[nn - 1] = -z; wi[nn] = z
                        }
                        nn -= 2
                    } else {
                        if (its == 60) return (0 until n).map { Complex(Double.NaN) }
                        if (its == 10 || its == 20) {
                            t += x
                            for (i in 0..nn) a[i][i] -= x
                            s = abs(a[nn][nn - 1]) + abs(a[nn - 1][nn - 2])
                            x = 0.75 * s; y = x; w = -0.4375 * s * s
                        }
                        its++
                        var m = nn - 2
                        while (m >= l) {
                            z = a[m][m]
                            r = x - z
                            s = y - z
                            p = (r * s - w) / a[m + 1][m] + a[m][m + 1]
                            q = a[m + 1][m + 1] - z - r - s
                            r = a[m + 2][m + 1]
                            s = abs(p) + abs(q) + abs(r)
                            p /= s; q /= s; r /= s
                            if (m == l) break
                            val u = abs(a[m][m - 1]) * (abs(q) + abs(r))
                            val v = abs(p) * (abs(a[m - 1][m - 1]) + abs(z) + abs(a[m + 1][m + 1]))
                            if (u + v == v) break
                            m--
                        }
                        for (i in m + 2..nn) { a[i][i - 2] = 0.0; if (i != m + 2) a[i][i - 3] = 0.0 }
                        var k = m
                        while (k <= nn - 1) {
                            if (k != m) {
                                p = a[k][k - 1]; q = a[k + 1][k - 1]; r = 0.0
                                if (k != nn - 1) r = a[k + 2][k - 1]
                                x = abs(p) + abs(q) + abs(r)
                                if (x != 0.0) { p /= x; q /= x; r /= x }
                            }
                            s = sqrt(p * p + q * q + r * r).let { if (p >= 0) it else -it }
                            if (s != 0.0) {
                                if (k == m) { if (l != m) a[k][k - 1] = -a[k][k - 1] } else a[k][k - 1] = -s * x
                                p += s
                                x = p / s; y = q / s; z = r / s
                                q /= p; r /= p
                                for (j in k..nn) {
                                    p = a[k][j] + q * a[k + 1][j]
                                    if (k != nn - 1) { p += r * a[k + 2][j]; a[k + 2][j] -= p * z }
                                    a[k + 1][j] -= p * y
                                    a[k][j] -= p * x
                                }
                                val mmin = if (nn < k + 3) nn else k + 3
                                for (i in l..mmin) {
                                    p = x * a[i][k] + y * a[i][k + 1]
                                    if (k != nn - 1) { p += z * a[i][k + 2]; a[i][k + 2] -= p * r }
                                    a[i][k + 1] -= p * q
                                    a[i][k] -= p
                                }
                            }
                            k++
                        }
                    }
                }
            } while (l < nn - 1)
        }
        return (0 until n).map { Complex(wr[it], wi[it]) }.sortedWith(compareByDescending<Complex> { it.re }.thenByDescending { it.im })
    }

    companion object {
        fun identity(n: Int) = Matrix(n, n).also { m -> for (i in 0 until n) m.a[i][i] = 1.0 }
        fun of(vararg rows: DoubleArray) = Matrix(rows.size, rows[0].size, Array(rows.size) { rows[it].copyOf() })
    }
}

/** Descriptive statistics and least squares. */
object Stats {
    fun mean(x: List<Double>) = x.sum() / x.size
    /** Unbiased sample variance s² = Σ(xᵢ − x̄)² / (n − 1), computed with Welford's update for stability. */
    fun variance(x: List<Double>): Double {
        var m = 0.0; var s = 0.0; var k = 0
        for (v in x) { k++; val d = v - m; m += d / k; s += d * (v - m) }
        return if (k > 1) s / (k - 1) else 0.0
    }
    fun std(x: List<Double>) = sqrt(variance(x))
    fun median(x: List<Double>): Double { val s = x.sorted(); val n = s.size; return if (n % 2 == 1) s[n / 2] else (s[n / 2 - 1] + s[n / 2]) / 2 }

    data class Fit(val slope: Double, val intercept: Double, val r2: Double, val slopeErr: Double, val interceptErr: Double)

    /** Ordinary least squares y = a + b x with standard errors of a and b. */
    fun linearFit(x: List<Double>, y: List<Double>): Fit? {
        val n = x.size
        if (n < 3) return null
        val mx = mean(x); val my = mean(y)
        val sxx = x.sumOf { (it - mx) * (it - mx) }
        if (sxx == 0.0) return null
        val sxy = x.indices.sumOf { (x[it] - mx) * (y[it] - my) }
        val b = sxy / sxx
        val a = my - b * mx
        val ssr = x.indices.sumOf { val e = y[it] - a - b * x[it]; e * e }
        val sst = y.sumOf { (it - my) * (it - my) }
        val s2 = ssr / (n - 2)
        return Fit(b, a, if (sst > 0) 1 - ssr / sst else 1.0, sqrt(s2 / sxx), sqrt(s2 * (1.0 / n + mx * mx / sxx)))
    }

    /** Standard normal density and cumulative distribution. */
    fun normalPdf(z: Double) = exp(-z * z / 2) / sqrt(2 * PI)
    fun normalCdf(z: Double) = 0.5 * (1 + SpecialFunctions.erf(z / sqrt(2.0)))

    fun binomial(n: Int, k: Int, p: Double): Double {
        if (k < 0 || k > n) return 0.0
        val logC = lnGamma(n + 1.0) - lnGamma(k + 1.0) - lnGamma(n - k + 1.0)
        return exp(logC + k * ln(p) + (n - k) * ln(1 - p))
    }

    fun poisson(k: Int, lambda: Double): Double = exp(k * ln(lambda) - lambda - lnGamma(k + 1.0))

    fun lnGamma(x: Double): Double = SpecialFunctions.lnGamma(x)
}

/** Radix-2 Cooley–Tukey FFT (in place on copies); length must be a power of two. */
object Fft {
    fun transform(re: DoubleArray, im: DoubleArray, inverse: Boolean = false): Pair<DoubleArray, DoubleArray> {
        val n = re.size
        require(n > 0 && n and (n - 1) == 0) { "Length must be a power of two" }
        val xr = re.copyOf(); val xi = im.copyOf()
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) { j = j xor bit; bit = bit shr 1 }
            j = j xor bit
            if (i < j) { var t = xr[i]; xr[i] = xr[j]; xr[j] = t; t = xi[i]; xi[i] = xi[j]; xi[j] = t }
        }
        var len = 2
        while (len <= n) {
            val ang = 2 * PI / len * if (inverse) 1 else -1
            val wr = cos(ang); val wi = sin(ang)
            var i = 0
            while (i < n) {
                var cr = 1.0; var ci = 0.0
                for (k in 0 until len / 2) {
                    val ur = xr[i + k]; val ui = xi[i + k]
                    val vr = xr[i + k + len / 2] * cr - xi[i + k + len / 2] * ci
                    val vi = xr[i + k + len / 2] * ci + xi[i + k + len / 2] * cr
                    xr[i + k] = ur + vr; xi[i + k] = ui + vi
                    xr[i + k + len / 2] = ur - vr; xi[i + k + len / 2] = ui - vi
                    val ncr = cr * wr - ci * wi
                    ci = cr * wi + ci * wr; cr = ncr
                }
                i += len
            }
            len = len shl 1
        }
        if (inverse) for (k in 0 until n) { xr[k] /= n; xi[k] /= n }
        return xr to xi
    }

    /** Amplitude spectrum |X_k|·2/N for k = 0..N/2 of a real signal. */
    fun amplitudes(signal: DoubleArray): DoubleArray {
        val (r, i) = transform(signal, DoubleArray(signal.size))
        val n = signal.size
        return DoubleArray(n / 2 + 1) { k -> hypot(r[k], i[k]) * (if (k == 0 || k == n / 2) 1.0 else 2.0) / n }
    }
}

/** Roots of real polynomials Σ cₖ xᵏ via the eigenvalues of the companion matrix. */
object Polynomials {
    fun roots(coeffsAscending: List<Double>): List<Complex> {
        val c = coeffsAscending.dropLastWhile { it == 0.0 }
        val n = c.size - 1
        if (n < 1) return emptyList()
        if (n == 1) return listOf(Complex(-c[0] / c[1]))
        if (n == 2) {
            val (a0, a1, a2) = Triple(c[0], c[1], c[2])
            val disc = a1 * a1 - 4 * a2 * a0
            return if (disc >= 0) {
                // Numerically stable form: q = −(b + sgn(b)√Δ)/2, x₁ = q/a, x₂ = c/q.
                val q = -0.5 * (a1 + (if (a1 >= 0) 1 else -1) * sqrt(disc))
                listOfNotNull(Complex(q / a2), if (q != 0.0) Complex(a0 / q) else Complex(0.0)).sortedByDescending { it.re }
            } else listOf(Complex(-a1 / (2 * a2), sqrt(-disc) / (2 * a2)), Complex(-a1 / (2 * a2), -sqrt(-disc) / (2 * a2)))
        }
        val m = Matrix(n, n)
        for (i in 1 until n) m[i, i - 1] = 1.0
        for (i in 0 until n) m[i, n - 1] = -c[i] / c[n]
        return m.eigenvalues()
    }
}

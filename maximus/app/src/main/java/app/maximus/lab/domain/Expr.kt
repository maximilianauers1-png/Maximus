package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

class ExprException(message: String, val position: Int) : IllegalArgumentException(message)

/**
 * Abstract syntax tree of a real-valued expression. Evaluation takes a variable binding; unknown
 * variables raise an [ExprException] so typos are reported instead of silently becoming zero.
 */
sealed interface Expr {
    fun eval(vars: Map<String, Double>): Double

    data class Num(val v: Double) : Expr { override fun eval(vars: Map<String, Double>) = v }
    data class Var(val name: String) : Expr {
        override fun eval(vars: Map<String, Double>) = vars[name] ?: Expr.CONSTANTS[name] ?: throw ExprException("Unbekannte Variable „$name“", 0)
    }
    data class Neg(val e: Expr) : Expr { override fun eval(vars: Map<String, Double>) = -e.eval(vars) }
    data class Bin(val op: Char, val l: Expr, val r: Expr) : Expr {
        override fun eval(vars: Map<String, Double>): Double {
            val a = l.eval(vars)
            val b = r.eval(vars)
            return when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> a / b
                '^' -> pow(a, b)
                '%' -> a - b * floor(a / b)
                else -> Double.NaN
            }
        }
    }
    data class Call(val fn: String, val args: List<Expr>) : Expr {
        override fun eval(vars: Map<String, Double>): Double {
            val x = args.map { it.eval(vars) }
            return Expr.applyFunction(fn, x)
        }
    }
    data class Factorial(val e: Expr) : Expr { override fun eval(vars: Map<String, Double>) = SpecialFunctions.gamma(e.eval(vars) + 1) }

    /** Free variables (names not bound to built-in constants). */
    fun variables(): Set<String> = when (this) {
        is Num -> emptySet()
        is Var -> if (name in CONSTANTS) emptySet() else setOf(name)
        is Neg -> e.variables()
        is Bin -> l.variables() + r.variables()
        is Call -> args.flatMap { it.variables() }.toSet()
        is Factorial -> e.variables()
    }

    companion object {
        val CONSTANTS: Map<String, Double> = mapOf("pi" to PI, "π" to PI, "e" to E, "tau" to 2 * PI, "phi" to (1 + sqrt(5.0)) / 2)

        /** Real power that also handles odd roots of negative bases, e.g. (−8)^(1/3) = −2. */
        fun pow(a: Double, b: Double): Double {
            if (a >= 0 || b == floor(b)) return a.pow(b)
            // Accept b ≈ 1/q with odd q.
            val q = 1.0 / b
            val qr = round(q)
            return if (abs(q - qr) < 1e-9 && qr.toLong() % 2L != 0L) -(-a).pow(b) else Double.NaN
        }

        val FUNCTIONS: Map<String, IntRange> = mapOf(
            "sin" to 1..1, "cos" to 1..1, "tan" to 1..1, "asin" to 1..1, "acos" to 1..1, "atan" to 1..2, "arcsin" to 1..1,
            "arccos" to 1..1, "arctan" to 1..1, "sinh" to 1..1, "cosh" to 1..1, "tanh" to 1..1, "asinh" to 1..1, "acosh" to 1..1,
            "atanh" to 1..1, "exp" to 1..1, "ln" to 1..1, "log" to 1..2, "log10" to 1..1, "log2" to 1..1, "sqrt" to 1..1, "cbrt" to 1..1,
            "abs" to 1..1, "floor" to 1..1, "ceil" to 1..1, "round" to 1..1, "sign" to 1..1, "min" to 2..8, "max" to 2..8,
            "gamma" to 1..1, "erf" to 1..1, "erfc" to 1..1, "sec" to 1..1, "csc" to 1..1, "cot" to 1..1, "sinc" to 1..1,
            "heaviside" to 1..1, "deg" to 1..1, "rad" to 1..1, "hypot" to 2..2, "besselj0" to 1..1, "fact" to 1..1
        )

        fun applyFunction(fn: String, x: List<Double>): Double = when (fn) {
            "sin" -> sin(x[0]); "cos" -> cos(x[0]); "tan" -> tan(x[0])
            "asin", "arcsin" -> asin(x[0]); "acos", "arccos" -> acos(x[0])
            "atan", "arctan" -> if (x.size == 2) atan2(x[0], x[1]) else atan(x[0])
            "sinh" -> sinh(x[0]); "cosh" -> cosh(x[0]); "tanh" -> tanh(x[0])
            "asinh" -> ln(x[0] + sqrt(x[0] * x[0] + 1)); "acosh" -> ln(x[0] + sqrt(x[0] * x[0] - 1))
            "atanh" -> 0.5 * ln((1 + x[0]) / (1 - x[0]))
            "exp" -> exp(x[0]); "ln" -> ln(x[0])
            "log" -> if (x.size == 2) ln(x[1]) / ln(x[0]) else log10(x[0])
            "log10" -> log10(x[0]); "log2" -> log2(x[0])
            "sqrt" -> sqrt(x[0]); "cbrt" -> cbrt(x[0]); "abs" -> abs(x[0])
            "floor" -> floor(x[0]); "ceil" -> ceil(x[0]); "round" -> round(x[0]); "sign" -> sign(x[0])
            "min" -> x.reduce { a, b -> min(a, b) }; "max" -> x.reduce { a, b -> max(a, b) }
            "gamma" -> SpecialFunctions.gamma(x[0]); "fact" -> SpecialFunctions.gamma(x[0] + 1)
            "erf" -> SpecialFunctions.erf(x[0]); "erfc" -> 1 - SpecialFunctions.erf(x[0])
            "sec" -> 1 / cos(x[0]); "csc" -> 1 / sin(x[0]); "cot" -> cos(x[0]) / sin(x[0])
            "sinc" -> if (x[0] == 0.0) 1.0 else sin(x[0]) / x[0]
            "heaviside" -> if (x[0] < 0) 0.0 else 1.0
            "deg" -> x[0] * 180 / PI; "rad" -> x[0] * PI / 180
            "hypot" -> kotlin.math.hypot(x[0], x[1])
            "besselj0" -> SpecialFunctions.besselJ0(x[0])
            else -> Double.NaN
        }
    }
}

/**
 * Recursive-descent parser for real expressions.
 *
 * Grammar (precedence low → high):
 *   sum     := product (('+' | '−' | '-') product)*
 *   product := unary (('*' | '·' | '×' | '/' | '%' | implicit) unary)*
 *   unary   := ('-' | '+') unary | power
 *   power   := postfix ('^' unary)?          — right-associative, so 2^3^2 = 2^9 and −2^2 = −4
 *   postfix := atom '!'*
 *   atom    := number | name | name '(' args ')' | '(' sum ')' | '|' sum '|'
 * Implicit multiplication: "2x", "2(x+1)", "x sin(x)", "(a)(b)". Decimal comma is accepted.
 */
object ExprParser {
    fun parse(text: String): Expr {
        val p = P(normaliseSuperscripts(text))
        val e = p.sum()
        p.skip()
        if (p.i < p.s.length) throw ExprException("Unerwartetes Zeichen „${p.s[p.i]}“", p.i)
        return e
    }

    /** Converts "x²" → "x^2", "x³" → "x^3" and the usual operator glyphs to ASCII. */
    private fun normaliseSuperscripts(t: String): String {
        val sb = StringBuilder()
        for (ch in t) {
            when (ch) {
                '²' -> sb.append("^2"); '³' -> sb.append("^3")
                '−' -> sb.append('-'); '·', '×' -> sb.append('*')
                '√' -> sb.append("sqrt")
                else -> sb.append(ch)
            }
        }
        return sb.toString().replace("**", "^")
    }

    fun tryParse(text: String): Result<Expr> = runCatching { parse(text) }

    private class P(val s: String) {
        var i = 0
        /** Parenthesis depth: a comma inside parentheses separates arguments, outside it is a decimal comma. */
        var depth = 0
        fun skip() { while (i < s.length && s[i].isWhitespace()) i++ }
        fun peek(): Char? { skip(); return if (i < s.length) s[i] else null }

        fun sum(): Expr {
            var e = product()
            while (true) {
                when (peek()) {
                    '+' -> { i++; e = Expr.Bin('+', e, product()) }
                    '-' -> { i++; e = Expr.Bin('-', e, product()) }
                    else -> return e
                }
            }
        }

        fun product(): Expr {
            var e = unary()
            while (true) {
                val c = peek() ?: return e
                e = when {
                    c == '*' -> { i++; Expr.Bin('*', e, unary()) }
                    c == '/' -> { i++; Expr.Bin('/', e, unary()) }
                    c == '%' -> { i++; Expr.Bin('%', e, unary()) }
                    c == '(' || c.isLetter() || c == 'π' || c.isDigit() || c == '.' -> Expr.Bin('*', e, power())
                    else -> return e
                }
            }
        }

        fun unary(): Expr = when (peek()) {
            '-' -> { i++; Expr.Neg(unary()) }
            '+' -> { i++; unary() }
            else -> power()
        }

        fun power(): Expr {
            val base = postfix()
            return if (peek() == '^') { i++; Expr.Bin('^', base, unary()) } else base
        }

        fun postfix(): Expr {
            var e = atom()
            while (peek() == '!') { i++; e = Expr.Factorial(e) }
            return e
        }

        fun atom(): Expr {
            val c = peek() ?: throw ExprException("Ausdruck endet unerwartet", i)
            if (c == '(') {
                i++
                depth++
                val e = sum()
                if (peek() != ')') throw ExprException("„)“ fehlt", i)
                i++
                depth--
                return e
            }
            if (c == '|') {
                i++
                val e = sum()
                if (peek() != '|') throw ExprException("Schließendes „|“ fehlt", i)
                i++
                return Expr.Call("abs", listOf(e))
            }
            if (c.isDigit() || c == '.') return number()
            if (c.isLetter() || c == 'π' || c == '_') {
                val start = i
                while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_' || s[i] == 'π')) i++
                val name = s.substring(start, i)
                if (peek() == '(' && name in Expr.FUNCTIONS) {
                    i++
                    depth++
                    val args = ArrayList<Expr>()
                    if (peek() != ')') {
                        args += sum()
                        while (peek() == ',' || peek() == ';') { i++; args += sum() }
                    }
                    if (peek() != ')') throw ExprException("„)“ nach den Argumenten von $name fehlt", i)
                    i++
                    depth--
                    val arity = Expr.FUNCTIONS.getValue(name)
                    if (args.size !in arity) throw ExprException("$name erwartet ${arity.first}${if (arity.last != arity.first) "–${arity.last}" else ""} Argument(e)", start)
                    return Expr.Call(name, args)
                }
                // A function name without parentheses applied to the next factor: "sin x".
                val next = peek()
                if (name in Expr.FUNCTIONS && Expr.FUNCTIONS.getValue(name).first == 1 && next != null && next != ')' && next != ',' && next !in "+-*/^!|") {
                    return Expr.Call(name, listOf(power()))
                }
                return Expr.Var(name)
            }
            throw ExprException("Unerwartetes Zeichen „$c“", i)
        }

        fun number(): Expr {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == ',')) {
                // A comma between digits is a decimal comma; otherwise it separates arguments.
                if (s[i] == ',' && (depth > 0 || !(i + 1 < s.length && s[i + 1].isDigit() && !s.substring(start, i).contains('.') && !s.substring(start, i).contains(',')))) break
                i++
            }
            if (i < s.length && (s[i] == 'e' || s[i] == 'E') && i + 1 < s.length && (s[i + 1].isDigit() || ((s[i + 1] == '-' || s[i + 1] == '+') && i + 2 < s.length && s[i + 2].isDigit()))) {
                i += 2
                while (i < s.length && s[i].isDigit()) i++
            }
            val text = s.substring(start, i).replace(',', '.')
            return Expr.Num(text.toDoubleOrNull() ?: throw ExprException("Ungültige Zahl „$text“", start))
        }
    }
}

/** Gamma, error function and Bessel J₀ with double precision adequate for plotting and checks. */
object SpecialFunctions {
    /**
     * Γ(z) via the Stirling series for ln Γ after shifting the argument to z ≥ 15 with Γ(z) = Γ(z+n)/(z(z+1)…(z+n−1)),
     * and the reflection formula Γ(z)Γ(1−z) = π/sin(πz) for z < ½. Relative error ≈ 1e-15; exact for small integers.
     */
    fun gamma(z: Double): Double {
        if (z == floor(z) && z > 0 && z <= 171) return (2..(z.toInt() - 1)).fold(1.0) { a, k -> a * k }
        if (z < 0.5) return PI / (sin(PI * z) * gamma(1 - z))
        var x = z
        var shift = 1.0
        while (x < 15) { shift *= x; x += 1.0 }
        return exp(lnGammaStirling(x)) / shift
    }

    /** ln Γ(x) for x > 0 without overflow. */
    fun lnGamma(x: Double): Double = if (x < 15) ln(abs(gamma(x))) else lnGammaStirling(x)

    private fun lnGammaStirling(x: Double): Double {
        val x2 = x * x
        val series = 1 / (12 * x) - 1 / (360 * x * x2) + 1 / (1260 * x * x2 * x2) - 1 / (1680 * x * x2 * x2 * x2) + 1 / (1188 * x * x2 * x2 * x2 * x2)
        return (x - 0.5) * ln(x) - x + 0.5 * ln(2 * PI) + series
    }

    /** erf via the Abramowitz–Stegun 7.1.26-type rational approximation refined by a series for small |x|; |error| < 1.2e-7. */
    fun erf(x: Double): Double {
        if (abs(x) < 0.5) {
            // Maclaurin series: erf x = 2/√π Σ (−1)^n x^(2n+1) / (n!(2n+1)).
            var term = x
            var sum = x
            var n = 0
            while (abs(term) > 1e-17 && n < 60) {
                n++
                term *= -x * x / n
                sum += term / (2 * n + 1)
            }
            return 2 / sqrt(PI) * sum
        }
        // Numerical Recipes erfc Chebyshev fit (fractional error < 1.2e-7).
        val z = abs(x)
        val t = 1.0 / (1.0 + 0.5 * z)
        val r = t * exp(
            -z * z - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * (0.09678418 + t * (-0.18628806 +
                t * (0.27886807 + t * (-1.13520398 + t * (1.48851587 + t * (-0.82215223 + t * 0.17087277))))))))
        )
        return if (x >= 0) 1 - r else r - 1
    }

    /** Bessel J₀ by its power series for |x| < 12 and the Hankel asymptotic form beyond. */
    fun besselJ0(x: Double): Double {
        val ax = abs(x)
        if (ax < 12) {
            var term = 1.0
            var sum = 1.0
            var k = 0
            while (abs(term) > 1e-17 * abs(sum) && k < 200) {
                k++
                term *= -(x * x / 4) / (k.toDouble() * k)
                sum += term
            }
            return sum
        }
        val w = ax - PI / 4
        return sqrt(2 / (PI * ax)) * (cos(w) * (1 - 9 / (128 * ax * ax)) + sin(w) / (8 * ax))
    }
}

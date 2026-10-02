package app.maximus.strongman.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

object SpecialFunctions {
    private val LANCZOS = doubleArrayOf(
        0.99999999999980993, 676.5203681218851, -1259.1392167224028, 771.32342877765313,
        -176.61502916214059, 12.507343278686905, -0.13857109526572012,
        9.9843695780195716e-6, 1.5056327351493116e-7
    )

    /** ln Gamma(x) via Lanczos (g = 7, n = 9); reflection formula for x < 1/2. */
    fun lnGamma(x: Double): Double {
        if (x < 0.5) return ln(PI / abs(sin(PI * x))) - lnGamma(1.0 - x)
        val z = x - 1.0
        var a = LANCZOS[0]
        val t = z + 7.5
        for (i in 1 until 9) a += LANCZOS[i] / (z + i)
        return 0.5 * ln(2.0 * PI) + (z + 0.5) * ln(t) - t + ln(a)
    }

    /** Regularized incomplete beta I_x(a, b), modified Lentz continued fraction. */
    fun regularizedIncompleteBeta(x: Double, a: Double, b: Double): Double {
        require(x in 0.0..1.0)
        if (x == 0.0 || x == 1.0) return x
        val lnFront = lnGamma(a + b) - lnGamma(a) - lnGamma(b) + a * ln(x) + b * ln(1.0 - x)
        return if (x < (a + 1.0) / (a + b + 2.0)) {
            exp(lnFront) * betaContinuedFraction(x, a, b) / a
        } else {
            1.0 - exp(lnFront) * betaContinuedFraction(1.0 - x, b, a) / b
        }
    }

    private fun betaContinuedFraction(x: Double, a: Double, b: Double): Double {
        val tiny = 1e-300
        var c = 1.0
        var d = 1.0 - (a + b) * x / (a + 1.0)
        if (abs(d) < tiny) d = tiny
        d = 1.0 / d
        var h = d
        for (m in 1..300) {
            val m2 = 2 * m
            var aa = m * (b - m) * x / ((a + m2 - 1) * (a + m2))
            d = 1.0 + aa * d; if (abs(d) < tiny) d = tiny
            c = 1.0 + aa / c; if (abs(c) < tiny) c = tiny
            d = 1.0 / d
            h *= d * c
            aa = -(a + m) * (a + b + m) * x / ((a + m2) * (a + m2 + 1))
            d = 1.0 + aa * d; if (abs(d) < tiny) d = tiny
            c = 1.0 + aa / c; if (abs(c) < tiny) c = tiny
            d = 1.0 / d
            val del = d * c
            h *= del
            if (abs(del - 1.0) < 1e-15) break
        }
        return h
    }
}

object StudentT {
    /** CDF of Student's t with nu degrees of freedom: 1 - I_{nu/(nu+t^2)}(nu/2, 1/2)/2 for t >= 0. */
    fun cdf(t: Double, nu: Double): Double {
        val tail = 0.5 * SpecialFunctions.regularizedIncompleteBeta(nu / (nu + t * t), nu / 2.0, 0.5)
        return if (t >= 0.0) 1.0 - tail else tail
    }

    /** Quantile by bisection; the CDF is strictly monotone, 200 halvings reach machine precision. */
    fun quantile(p: Double, nu: Double): Double {
        require(p > 0.0 && p < 1.0 && nu > 0.0)
        if (p == 0.5) return 0.0
        if (p < 0.5) return -quantile(1.0 - p, nu)
        var lo = 0.0
        var hi = 1.0
        while (cdf(hi, nu) < p) hi *= 2.0
        repeat(200) {
            val mid = 0.5 * (lo + hi)
            if (cdf(mid, nu) < p) lo = mid else hi = mid
        }
        return 0.5 * (lo + hi)
    }
}

/**
 * OLS fit y = a + b t with the 95 % prediction band
 * y_hat(t0) +/- t_{n-2, 0.975} * s * sqrt(1 + 1/n + (t0 - tbar)^2 / Sxx), s^2 = SSE/(n-2).
 */
data class LinearFit(
    val intercept: Double,
    val slope: Double,
    val n: Int,
    val residualStdError: Double,
    val tBar: Double,
    val sxx: Double,
    val tQuantile: Double
) {
    fun predict(t: Double): Double = intercept + slope * t

    fun predictionHalfWidth(t0: Double): Double =
        tQuantile * residualStdError * sqrt(1.0 + 1.0 / n + (t0 - tBar) * (t0 - tBar) / sxx)
}

object Regression {
    const val MIN_POINTS = 5

    fun fit(ts: List<Double>, ys: List<Double>, level: Double = 0.95): LinearFit? {
        require(ts.size == ys.size)
        val n = ts.size
        if (n < MIN_POINTS) return null
        val tBar = ts.average()
        val yBar = ys.average()
        var sxx = 0.0
        var sxy = 0.0
        for (i in 0 until n) {
            val dt = ts[i] - tBar
            sxx += dt * dt
            sxy += dt * (ys[i] - yBar)
        }
        if (sxx <= 0.0) return null
        val b = sxy / sxx
        val a = yBar - b * tBar
        var sse = 0.0
        for (i in 0 until n) {
            val r = ys[i] - (a + b * ts[i])
            sse += r * r
        }
        val s = sqrt(sse / (n - 2))
        val q = StudentT.quantile(0.5 + level / 2.0, (n - 2).toDouble())
        return LinearFit(a, b, n, s, tBar, sxx, q)
    }
}

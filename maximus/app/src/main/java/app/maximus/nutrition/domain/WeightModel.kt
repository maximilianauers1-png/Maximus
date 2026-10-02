package app.maximus.nutrition.domain

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Two-compartment energy-partition model after Hall (Am J Physiol Endocrinol Metab 2008;294:E1133)
 * with the Forbes partition:
 *
 *   dF/dt = (1 − p) ΔE / ρF,   dL/dt = p ΔE / ρL,   p = C / (C + F),   C = 10.4 kg · ρL/ρF,
 *   ρF = 39.5 MJ/kg = 9440 kcal/kg,   ρL = 7.6 MJ/kg = 1816 kcal/kg,   ΔE = EI − EE(t).
 *
 * Energy expenditure follows body weight through the BMR equation at fixed PAL,
 *   EE(t) = TDEE₀ · BMR(m(t)) / BMR(m₀),
 * so a measured (adaptive) TDEE₀ can calibrate the model. Integration: explicit Euler with Δt = 1 d
 * (timescale of the system ≫ 1 d, local error O(Δt²) negligible). Adaptive thermogenesis beyond the
 * mass effect and training-induced repartitioning are not modelled; the 7700-kcal rule is shown for
 * comparison, and the spread between both is displayed as the model-uncertainty band.
 */
object WeightModel {
    const val RHO_F = 9440.0
    const val RHO_L = 1816.0
    const val RHO_MIXED = 7700.0
    val C = 10.4 * RHO_L / RHO_F

    data class State(val fatKg: Double, val leanKg: Double) { val weightKg get() = fatKg + leanKg }

    class Input(
        val body: Body,
        val tdee0: Double,
        val bmrOf: (Double) -> Double
    ) {
        val fat0: Double = body.weightKg * (body.bodyFatPercent ?: BodyComposition.deurenberg(body.sex, body.ageYears, BodyComposition.bmi(body.weightKg, body.heightCm))).coerceIn(3.0, 60.0) / 100.0
        val bmr0 = bmrOf(body.weightKg)
    }

    /** Daily states for days 0..days at constant intake [intakeKcal]. */
    fun simulate(input: Input, intakeKcal: Double, days: Int): List<State> {
        var s = State(input.fat0, input.body.weightKg - input.fat0)
        val out = ArrayList<State>(days + 1)
        out += s
        repeat(days) {
            val ee = input.tdee0 * input.bmrOf(s.weightKg) / input.bmr0
            val de = intakeKcal - ee
            val p = C / (C + s.fatKg)
            s = State((s.fatKg + (1 - p) * de / RHO_F).coerceAtLeast(0.5), s.leanKg + p * de / RHO_L)
            out += s
        }
        return out
    }

    /** Constant intake that reaches [targetKg] after [days] days (bisection; final mass is strictly increasing in intake). */
    fun intakeFor(input: Input, targetKg: Double, days: Int, lo: Double = 600.0, hi: Double = 9000.0): Double? {
        require(days >= 1)
        fun final(ei: Double) = simulate(input, ei, days).last().weightKg
        if (final(lo) > targetKg || final(hi) < targetKg) return null
        var a = lo; var b = hi
        repeat(60) {
            val m = (a + b) / 2
            if (final(m) < targetKg) a = m else b = m
            if (b - a < 0.1) return (a + b) / 2
        }
        return (a + b) / 2
    }

    /** Classical static rule: ΔE = 7700 kcal/kg · Δm / days. */
    fun linearIntake(tdee0: Double, startKg: Double, targetKg: Double, days: Int) = tdee0 + RHO_MIXED * (targetKg - startKg) / days

    /** Mass trajectory for the linear rule (no adaptation): m(t) = m₀ + t (EI − TDEE₀)/7700. */
    fun linearTrajectory(tdee0: Double, startKg: Double, intake: Double, days: Int) = List(days + 1) { t -> startKg + t * (intake - tdee0) / RHO_MIXED }

    /** Weekly rate in % of body weight. */
    fun weeklyRatePercent(startKg: Double, targetKg: Double, days: Int) = (targetKg - startKg) / startKg * 100.0 * 7.0 / days
}

enum class RateAssessment { MAINTAIN, MODERATE_LOSS, AGGRESSIVE_LOSS, MODERATE_GAIN, FAST_GAIN }

/**
 * Thresholds: loss 0.5–1.0 %/wk preserves lean mass in trained athletes (Helms et al., JISSN 2014; Garthe 2011);
 * gain > 0.5 %/wk mostly adds fat in trained lifters (Iraki et al., Sports 2019).
 */
fun assessRate(percentPerWeek: Double): RateAssessment = when {
    abs(percentPerWeek) < 0.05 -> RateAssessment.MAINTAIN
    percentPerWeek < -1.0 -> RateAssessment.AGGRESSIVE_LOSS
    percentPerWeek < 0 -> RateAssessment.MODERATE_LOSS
    percentPerWeek > 0.5 -> RateAssessment.FAST_GAIN
    else -> RateAssessment.MODERATE_GAIN
}

data class LogPoint(val epochDay: Long, val weightKg: Double?, val kcal: Double?)

data class TdeeEstimate(
    val tdee: Double,
    val sigma: Double,
    val slopeKgPerWeek: Double,
    val meanIntake: Double,
    val weightDays: Int,
    val intakeDays: Int
)

/**
 * Empirical TDEE from logged intake and weight over the last [windowDays] days:
 * ordinary least squares m(t) = a + b t over all weigh-ins, then TDEE = Ī − ρ b with ρ = 7700 kcal/kg.
 * Standard error: σ_b = √(Σr²/(n−2) / Σ(t−t̄)²), σ_TDEE = ρ σ_b (intake-logging error not included).
 * Requires ≥ 10 weigh-ins spanning ≥ 14 days and ≥ 10 days with intake.
 */
object AdaptiveTdee {
    fun estimate(points: List<LogPoint>, today: Long, windowDays: Int = 28): TdeeEstimate? {
        val w = points.filter { it.epochDay > today - windowDays && it.epochDay <= today }
        val ws = w.filter { it.weightKg != null }
        val ks = w.mapNotNull { it.kcal }
        if (ws.size < 10 || ks.size < 10) return null
        val t = ws.map { it.epochDay.toDouble() }
        val y = ws.map { it.weightKg!! }
        if (t.max() - t.min() < 14) return null
        val (b, se) = slope(t, y)
        val mean = ks.average()
        return TdeeEstimate(mean - WeightModel.RHO_MIXED * b, WeightModel.RHO_MIXED * se, b * 7, mean, ws.size, ks.size)
    }

    /** OLS slope and its standard error. */
    fun slope(x: List<Double>, y: List<Double>): Pair<Double, Double> {
        val n = x.size
        val mx = x.average(); val my = y.average()
        var sxx = 0.0; var sxy = 0.0
        for (i in 0 until n) { sxx += (x[i] - mx) * (x[i] - mx); sxy += (x[i] - mx) * (y[i] - my) }
        val b = sxy / sxx
        val a = my - b * mx
        var rss = 0.0
        for (i in 0 until n) { val r = y[i] - (a + b * x[i]); rss += r * r }
        val se = if (n > 2) sqrt(rss / (n - 2) / sxx) else Double.NaN
        return b to se
    }

    /** Exponentially weighted moving average (Walker's "trend", α = 0.1) over consecutive weigh-ins. */
    fun ewma(values: List<Double>, alpha: Double = 0.1): List<Double> {
        if (values.isEmpty()) return emptyList()
        val out = ArrayList<Double>(values.size)
        var s = values[0]
        for (v in values) { s += alpha * (v - s); out += s }
        return out
    }
}

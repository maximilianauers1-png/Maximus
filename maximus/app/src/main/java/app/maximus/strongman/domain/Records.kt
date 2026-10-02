package app.maximus.strongman.domain

import java.util.TreeMap
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/** One personal record: the best performance at a given rep count, with the day it was set. */
data class RepRecord(val reps: Int, val weightKg: Double, val epochDay: Long, val setId: Long, val e1rm: Double?)

data class EventRecord(val mode: EventMode, val value: Double, val weightKg: Double, val epochDay: Long, val setId: Long)

data class ExerciseRecords(
    val exerciseId: Long,
    val byReps: List<RepRecord>,
    val bestE1rm: RepRecord?,
    val bestVolumeDay: Pair<Long, Double>?,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolume: Double,
    val lastDay: Long?,
    val events: List<EventRecord>
)

/**
 * Record book per exercise.
 *
 * A set at r reps with load w is a rep record iff no earlier set with the same r used more weight.
 * Because every rep count has its own record, the result is the empirical "load-repetition curve"
 * w(r), which is monotonically decreasing in r for a consistent athlete; violations of that monotony
 * are reported by [staleRecords] as records that a later, heavier set at a higher rep count beats.
 */
object RecordBook {
    fun forExercise(sets: List<AnalyticsSet>, exerciseId: Long, events: List<EventSet> = emptyList()): ExerciseRecords {
        val mine = sets.filter { it.exerciseId == exerciseId }
        val best = TreeMap<Int, RepRecord>()
        for (s in mine.sortedWith(compareBy({ it.epochDay }, { it.id }))) {
            if (s.reps <= 0 || s.weightKg <= 0) continue
            val current = best[s.reps]
            if (current == null || s.weightKg > current.weightKg) best[s.reps] = RepRecord(s.reps, s.weightKg, s.epochDay, s.id, s.e1rm)
        }
        val byDay = TreeMap<Long, Double>()
        for (s in mine) byDay.merge(s.epochDay, s.weightKg * s.reps, Double::plus)
        val bestE1rm = mine.filter { it.e1rm != null }.maxByOrNull { it.e1rm!! }
            ?.let { RepRecord(it.reps, it.weightKg, it.epochDay, it.id, it.e1rm) }
        val eventRecords = events.filter { it.exerciseId == exerciseId }
            .groupBy { it.mode }
            .mapNotNull { (mode, list) ->
                val best2 = if (mode == EventMode.FOR_TIME) list.minByOrNull { it.value } else list.maxByOrNull { it.value }
                best2?.let { EventRecord(mode, it.value, it.weightKg, it.epochDay, it.id) }
            }
        return ExerciseRecords(
            exerciseId, best.values.toList(), bestE1rm,
            byDay.maxByOrNull { it.value }?.let { it.key to it.value },
            mine.size, mine.sumOf { it.reps }, mine.sumOf { it.weightKg * it.reps },
            mine.maxOfOrNull { it.epochDay }, eventRecords
        )
    }

    /** Rep records that are dominated by a record at a higher rep count (a sign of an untested rep range). */
    fun staleRecords(records: List<RepRecord>): List<RepRecord> =
        records.filter { r -> records.any { it.reps > r.reps && it.weightKg >= r.weightKg } }

    /**
     * Fit of the load-repetition curve w(r) = w₁ · exp(−k(r − 1)) by ordinary least squares on
     * ln w = ln w₁ − k(r − 1). Returns (w₁, k, R²); w₁ is the predicted single and k the fatigue rate.
     * Requires at least three distinct rep counts.
     */
    fun fitLoadRepCurve(records: List<RepRecord>): Triple<Double, Double, Double>? {
        val pts = records.filter { it.weightKg > 0 }.map { (it.reps - 1).toDouble() to ln(it.weightKg) }
        if (pts.size < 3) return null
        val n = pts.size
        val mx = pts.sumOf { it.first } / n
        val my = pts.sumOf { it.second } / n
        var sxx = 0.0; var sxy = 0.0; var syy = 0.0
        for ((x, y) in pts) { sxx += (x - mx) * (x - mx); sxy += (x - mx) * (y - my); syy += (y - my) * (y - my) }
        if (sxx <= 0.0) return null
        val b = sxy / sxx
        val a = my - b * mx
        val r2 = if (syy <= 0.0) 1.0 else (sxy * sxy) / (sxx * syy)
        return Triple(exp(a), -b, r2)
    }

    /** Predicted load for [reps] from the fitted curve. */
    fun predict(fit: Triple<Double, Double, Double>, reps: Int): Double = fit.first * exp(-fit.second * (reps - 1))
}

data class EventSet(val id: Long, val epochDay: Long, val exerciseId: Long, val mode: EventMode, val value: Double, val weightKg: Double)

data class TrainingLoadPoint(val epochDay: Long, val load: Double, val acute: Double, val chronic: Double, val ratio: Double?)

/**
 * Session load and the acute/chronic workload ratio.
 *
 * Session RPE load (Foster 1998) is sRPE × duration; without a duration the tonnage-weighted
 * equivalent w·r·(RPE/10) is used, which keeps the units consistent across sessions.
 * The acute load is the exponentially weighted average over τ_a = 7 days, the chronic one over
 * τ_c = 28 days, both with λ = 2/(τ+1) (Williams et al., Br J Sports Med 2017), which avoids the
 * discontinuities of the rolling-average form. ACWR = acute/chronic; values far above 1.5 have been
 * associated with higher injury risk in team sports, though the evidence is contested — the number is
 * a trend indicator, not a diagnosis.
 */
object TrainingLoad {
    const val ACUTE_DAYS = 7
    const val CHRONIC_DAYS = 28

    fun dailyLoad(sets: List<AnalyticsSet>): TreeMap<Long, Double> {
        val out = TreeMap<Long, Double>()
        for (s in sets) {
            val intensity = (s.rpe ?: 7.5) / 10.0
            out.merge(s.epochDay, s.weightKg * s.reps * intensity, Double::plus)
        }
        return out
    }

    fun series(sets: List<AnalyticsSet>, today: Long): List<TrainingLoadPoint> {
        val daily = dailyLoad(sets)
        if (daily.isEmpty()) return emptyList()
        val from = daily.firstKey()
        val la = 2.0 / (ACUTE_DAYS + 1)
        val lc = 2.0 / (CHRONIC_DAYS + 1)
        var acute = 0.0
        var chronic = 0.0
        val out = ArrayList<TrainingLoadPoint>()
        var day = from
        while (day <= today) {
            val load = daily[day] ?: 0.0
            acute += la * (load - acute)
            chronic += lc * (load - chronic)
            out += TrainingLoadPoint(day, load, acute, chronic, if (chronic > 1e-9) acute / chronic else null)
            day++
        }
        return out
    }
}

/**
 * Strongman implement maths that the plate calculator does not cover.
 */
object ImplementMath {
    /**
     * Load on the hands of a yoke or frame carry: the implement is supported at two points, so each
     * hand carries half of the total mass when the load is centred. With the centre of mass shifted by
     * [offsetM] from the middle of a carry of width [widthM], the near side takes
     * F = m g (1/2 + offset/width) and the far side the rest (static moment balance).
     */
    fun handLoadKg(totalKg: Double, widthM: Double, offsetM: Double): Pair<Double, Double> {
        require(widthM > 0)
        val f = (0.5 + offsetM / widthM).coerceIn(0.0, 1.0)
        return totalKg * f to totalKg * (1 - f)
    }

    /**
     * Torque about the lower back when holding a load at horizontal distance [leverM] from the hips:
     * M = m g d. Doubling the distance doubles the moment, which is why a stone held close is far
     * easier than the same stone held away from the body.
     */
    fun lumbarTorqueNm(loadKg: Double, leverM: Double): Double = loadKg * 9.80665 * leverM

    /**
     * Mechanical work of a carry against gravity is zero on level ground; the metabolic cost is
     * dominated by the time under load. A useful comparable number is the impulse m · t in kg·s,
     * which is what strongman carries are actually scored by when distance is fixed.
     */
    fun carryImpulse(loadKg: Double, seconds: Double): Double = loadKg * seconds

    /** Average speed of a carry in m/s and the equivalent pace in s per 10 m. */
    fun carrySpeed(distanceM: Double, seconds: Double): Pair<Double, Double> {
        require(seconds > 0 && distanceM > 0)
        return distanceM / seconds to seconds / distanceM * 10.0
    }

    /**
     * Mechanical work of a stone-over-bar rep: W = m g h per rep; power follows from the time.
     * This is the one strongman event where work and power are well defined.
     */
    fun liftWorkJoule(loadKg: Double, heightM: Double, reps: Int = 1): Double = loadKg * 9.80665 * heightM * reps

    fun liftPowerWatt(loadKg: Double, heightM: Double, reps: Int, seconds: Double): Double =
        if (seconds <= 0) 0.0 else liftWorkJoule(loadKg, heightM, reps) / seconds
}

/**
 * Projection of a record forward in time: ordinary least squares on the daily best e1RM with a
 * prediction interval, so "when will I pull 300 kg" has an answer with an honest uncertainty.
 */
object RecordProjection {
    data class Projection(
        val slopePerWeek: Double,
        val intercept: Double,
        val sigma: Double,
        val n: Int,
        val r2: Double,
        val daysToTarget: Double?,
        val daysToTargetLow: Double?,
        val daysToTargetHigh: Double?
    )

    /** Student-t 95 % two-sided quantiles for ν = 1..30, then the normal limit. */
    private val T95 = doubleArrayOf(
        12.706, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306, 2.262, 2.228,
        2.201, 2.179, 2.160, 2.145, 2.131, 2.120, 2.110, 2.101, 2.093, 2.086,
        2.080, 2.074, 2.069, 2.064, 2.060, 2.056, 2.052, 2.048, 2.045, 2.042
    )

    fun t95(df: Int): Double = if (df <= 0) Double.NaN else if (df <= 30) T95[df - 1] else 1.960

    /**
     * [points] are (epochDay, best e1RM). The slope b and its standard error σ_b come from OLS; the
     * time to reach [targetKg] is (target − ŷ(t_last))/b counted from the last data point, and the
     * interval uses b ± t_{0.975,n−2} σ_b. A non-positive or statistically indistinguishable slope
     * yields null, because extrapolating a flat trend to a target is meaningless.
     */
    fun project(points: List<Pair<Long, Double>>, targetKg: Double): Projection? {
        if (points.size < 4) return null
        val x = points.map { it.first.toDouble() }
        val y = points.map { it.second }
        val n = x.size
        val mx = x.average(); val my = y.average()
        var sxx = 0.0; var sxy = 0.0; var syy = 0.0
        for (i in 0 until n) { sxx += (x[i] - mx) * (x[i] - mx); sxy += (x[i] - mx) * (y[i] - my); syy += (y[i] - my) * (y[i] - my) }
        if (sxx <= 0.0) return null
        val b = sxy / sxx
        val a = my - b * mx
        var rss = 0.0
        for (i in 0 until n) { val r = y[i] - (a + b * x[i]); rss += r * r }
        val df = n - 2
        val s = sqrt(rss / df)
        val seB = s / sqrt(sxx)
        val r2 = if (syy <= 0.0) 1.0 else 1 - rss / syy
        val last = x.max()
        val yHat = a + b * last
        val t = t95(df)
        fun days(slope: Double): Double? = if (slope <= 1e-9) null else (targetKg - yHat) / slope
        return Projection(
            slopePerWeek = b * 7, intercept = a, sigma = seB * 7, n = n, r2 = r2,
            daysToTarget = days(b), daysToTargetLow = days(b + t * seB), daysToTargetHigh = days(b - t * seB)
        )
    }
}

/**
 * Wilks-style relative strength is covered by [Dots]; this adds the strongman-specific comparison of
 * an absolute lift against the athlete's own history, expressed as a z-score of the last 90 days.
 */
object Consistency {
    fun zScore(values: List<Double>, value: Double): Double? {
        if (values.size < 3) return null
        val m = values.average()
        val sd = sqrt(values.sumOf { (it - m) * (it - m) } / (values.size - 1))
        return if (sd < 1e-9) null else (value - m) / sd
    }

    /** Coefficient of variation of the session loads: a stability measure for the training block. */
    fun coefficientOfVariation(values: List<Double>): Double? {
        if (values.size < 2) return null
        val m = values.average()
        if (abs(m) < 1e-9) return null
        val sd = sqrt(values.sumOf { (it - m) * (it - m) } / (values.size - 1))
        return sd / m
    }
}

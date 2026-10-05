package app.maximus.strongman.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A record entered after the fact ("2019: 220 kg × 1 im Wettkampf"). Kept apart from the training log so
 * that years of old bests can be added without inventing sessions, but merged into every progression curve.
 */
data class HistoricRecord(
    val id: Long,
    val exerciseId: Long,
    val epochDay: Long,
    val weightKg: Double,
    val reps: Int,
    val competition: Boolean = false,
    val note: String = ""
) {
    val e1rm: Double? get() = Performance.e1rm(weightKg, reps, null, null)
}

/**
 * e1RM for comparisons across time and populations. A true single without RPE is taken as what it is —
 * the Epley term w(1 + 1/30) would otherwise inflate every single by 1.7 % — and everything else uses the
 * stored estimate (RPE table) or the default estimator.
 */
object Performance {
    fun e1rm(weightKg: Double, reps: Int, rpe: Double?, stored: Double?): Double? = when {
        !(weightKg > 0) || reps < 1 -> null
        reps == 1 && (rpe == null || rpe >= 10.0) -> weightKg
        stored != null -> stored
        else -> (OneRepMax.estimate(weightKg, reps) as? OneRmResult.Estimate)?.kg
    }
}

/** Text codec for one [HistoricRecord] (stored encrypted in app_meta, one row per record). */
object HistoricCodec {
    const val KEY_PREFIX = "strongman.hist."

    fun key(id: Long) = KEY_PREFIX + id

    fun encode(r: HistoricRecord): String =
        listOf("v1", r.exerciseId, r.epochDay, r.weightKg, r.reps, if (r.competition) 1 else 0, r.note.replace('\n', ' ')).joinToString("|")

    fun decode(key: String, value: String): HistoricRecord? {
        val id = key.removePrefix(KEY_PREFIX).toLongOrNull() ?: return null
        val p = value.split('|', limit = 7)
        if (p.size < 6 || p[0] != "v1") return null
        val ex = p[1].toLongOrNull() ?: return null
        val day = p[2].toLongOrNull() ?: return null
        val w = p[3].toDoubleOrNull()?.takeIf { it > 0 } ?: return null
        val reps = p[4].toIntOrNull()?.takeIf { it >= 1 } ?: return null
        return HistoricRecord(id, ex, day, w, reps, p[5] == "1", p.getOrElse(6) { "" })
    }
}

/** One performance on the time axis, from the log or from the historic record book. */
data class PerfPoint(val epochDay: Long, val e1rm: Double, val weightKg: Double, val reps: Int, val historic: Boolean)

enum class CurveModel(val title: String, val params: Int) {
    LINEAR("Linear", 2),
    LOGARITHMIC("Logarithmisch", 3),
    SATURATING("Sättigung (Plateau)", 3)
}

/** One fitted model; predictions are in kg, t in years since [StrengthCurve.Fit.day0]. */
data class ModelFit(
    val model: CurveModel,
    /** y = a + b·u(t) with u = t, ln(1 + t/τ) or −e^{−k t}; [shape] holds τ or k. */
    val a: Double,
    val b: Double,
    val shape: Double,
    val rss: Double,
    val n: Int,
    val aicc: Double,
    /** (XᵀX)⁻¹ of the conditional linear problem, row-major 2×2, for the leverage in the prediction band. */
    private val inv: DoubleArray,
    val weight: Double = 0.0
) {
    fun u(t: Double): Double = when (model) {
        CurveModel.LINEAR -> t
        CurveModel.LOGARITHMIC -> ln(1 + max(t, -0.999 * shape) / shape)
        CurveModel.SATURATING -> -exp(-shape * t)
    }

    fun predict(t: Double): Double = a + b * u(t)

    val s2: Double get() = rss / max(1, n - model.params)

    /** Variance of a new observation at t: s²(1 + x₀ᵀ(XᵀX)⁻¹x₀), x₀ = (1, u(t)). */
    fun predictionVariance(t: Double): Double {
        val x = u(t)
        val h = inv[0] + 2 * inv[1] * x + inv[3] * x * x
        return s2 * (1 + max(0.0, h))
    }

    /** Asymptote of the saturating model (a = plateau since u → 0), else null. */
    val plateau: Double? get() = if (model == CurveModel.SATURATING && b > 0) a else null

    fun withWeight(w: Double) = copy(weight = w)
}

/**
 * Long-term strength progression.
 *
 * Data: performances are binned into 28-day windows and the best e1RM of each window is kept, which
 * estimates "what you could lift then" instead of averaging over sub-maximal training days. Three
 * models are fitted to these maxima (t in years):
 *   linear        y = a + b t
 *   logarithmic   y = a + b ln(1 + t/τ)         (diminishing returns, no ceiling)
 *   saturating    y = a − b e^{−k t}            (approach to a plateau a; natural-athlete ceiling)
 * For fixed τ or k each model is linear in (a, b), so the shape parameter is found by a grid search on
 * the residual sum of squares and (a, b) in closed form. Models are compared by AICc and averaged with
 * Akaike weights w_i = e^{−Δ_i/2}/Σ e^{−Δ_j/2}; the averaged prediction band uses Buckland's unconditional
 * variance √Var = Σ w_i √(Var_i + (ŷ_i − ŷ)²) and a Student-t quantile.
 */
object StrengthCurve {
    const val BIN_DAYS = 28
    const val MIN_BINS = 4
    private const val YEAR = 365.25

    fun points(sets: List<AnalyticsSet>, historic: List<HistoricRecord>, exerciseIds: Set<Long>): List<PerfPoint> {
        val fromLog = sets.filter { it.exerciseId in exerciseIds }
            .mapNotNull { s -> Performance.e1rm(s.weightKg, s.reps, s.rpe, s.e1rm)?.let { PerfPoint(s.epochDay, it, s.weightKg, s.reps, false) } }
        val fromBook = historic.filter { it.exerciseId in exerciseIds }
            .mapNotNull { h -> h.e1rm?.let { PerfPoint(h.epochDay, it, h.weightKg, h.reps, true) } }
        return (fromLog + fromBook).sortedBy { it.epochDay }
    }

    /** Best point per 28-day bin, chronological. */
    fun binMaxima(points: List<PerfPoint>): List<PerfPoint> =
        points.groupBy { Math.floorDiv(it.epochDay, BIN_DAYS.toLong()) }.toSortedMap().values.map { bin -> bin.maxBy { it.e1rm } }

    /** Record staircase: the points at which the all-time best e1RM increased. */
    fun envelope(points: List<PerfPoint>): List<PerfPoint> {
        var best = Double.NEGATIVE_INFINITY
        val out = ArrayList<PerfPoint>()
        for (p in points.sortedBy { it.epochDay }) if (p.e1rm > best + 1e-9) { best = p.e1rm; out += p }
        return out
    }

    data class Fit(
        val day0: Long,
        val lastDay: Long,
        val models: List<ModelFit>,
        val n: Int
    ) {
        fun t(day: Double): Double = (day - day0) / YEAR
        val best: ModelFit get() = models.maxBy { it.weight }

        fun predict(day: Double): Double = models.sumOf { it.weight * it.predict(t(day)) }

        /** Half-width of the 95 % prediction band at [day] (model-averaged, unconditional). */
        fun halfWidth(day: Double): Double {
            val tt = t(day)
            val y = predict(day)
            val sd = models.sumOf { m -> val d = m.predict(tt) - y; m.weight * sqrt(m.predictionVariance(tt) + d * d) }
            val df = max(1, n - best.model.params)
            return RecordProjection.t95(df) * sd
        }

        /** Rate of change at [day] in kg per 30 days (numerical derivative of the averaged curve). */
        fun ratePerMonth(day: Double): Double = (predict(day + 15) - predict(day - 15))

        /**
         * First day ≥ lastDay on which the curve [which] (−1 lower band, 0 mean, +1 upper band) reaches
         * [targetKg], searched up to [horizonDays]; null if never within the horizon.
         */
        fun dayReaching(targetKg: Double, which: Int = 0, horizonDays: Int = 3650): Long? {
            fun f(d: Double) = predict(d) + which * halfWidth(d)
            if (f(lastDay.toDouble()) >= targetKg) return lastDay
            var d = lastDay.toLong()
            val end = lastDay + horizonDays
            while (d < end) {
                d += 7
                if (f(d.toDouble()) >= targetKg) {
                    var lo = d - 7; var hi = d
                    while (hi - lo > 1) { val mid = (lo + hi) / 2; if (f(mid.toDouble()) >= targetKg) hi = mid else lo = mid }
                    return hi
                }
            }
            return null
        }

        val plateau: Double? get() = models.firstOrNull { it.model == CurveModel.SATURATING }?.plateau
    }

    fun fit(points: List<PerfPoint>): Fit? {
        val bins = binMaxima(points)
        if (bins.size < MIN_BINS) return null
        val day0 = bins.first().epochDay
        val ts = bins.map { (it.epochDay - day0) / YEAR }
        val ys = bins.map { it.e1rm }
        if (ts.last() - ts.first() < 30 / YEAR) return null
        val n = ts.size
        val fits = ArrayList<ModelFit>()
        fitShape(CurveModel.LINEAR, ts, ys, listOf(0.0))?.let(fits::add)
        if (n >= 5) {
            fitShape(CurveModel.LOGARITHMIC, ts, ys, grid(0.02, 5.0, 60))?.let(fits::add)
            // Saturating only with an increasing curve (b > 0); a decreasing fit would be a decline model.
            fitShape(CurveModel.SATURATING, ts, ys, grid(0.05, 6.0, 70))?.takeIf { it.b > 0 }?.let(fits::add)
        }
        if (fits.isEmpty()) return null
        val minA = fits.minOf { it.aicc }
        val raw = fits.map { exp(-(it.aicc - minA) / 2) }
        val sum = raw.sum()
        return Fit(day0, bins.last().epochDay, fits.mapIndexed { i, f -> f.withWeight(raw[i] / sum) }, n)
    }

    private fun grid(lo: Double, hi: Double, count: Int): List<Double> =
        (0 until count).map { lo * (hi / lo).pow(it / (count - 1.0)) }

    private fun fitShape(model: CurveModel, ts: List<Double>, ys: List<Double>, shapes: List<Double>): ModelFit? {
        var best: ModelFit? = null
        for (s in shapes) {
            val probe = ModelFit(model, 0.0, 0.0, s, 0.0, ts.size, 0.0, DoubleArray(4))
            val us = ts.map { probe.u(it) }
            val f = linear(model, s, us, ys) ?: continue
            if (best == null || f.rss < best.rss) best = f
        }
        return best
    }

    /** Closed-form least squares y = a + b u with the AICc of the full model (p parameters). */
    private fun linear(model: CurveModel, shape: Double, us: List<Double>, ys: List<Double>): ModelFit? {
        val n = us.size
        val p = model.params
        if (n < p + 2) return null
        val su = us.sum(); val sy = ys.sum()
        val suu = us.sumOf { it * it }
        val suy = us.indices.sumOf { us[it] * ys[it] }
        val det = n * suu - su * su
        if (abs(det) < 1e-12) return null
        val b = (n * suy - su * sy) / det
        val a = (sy - b * su) / n
        val rss = us.indices.sumOf { val r = ys[it] - (a + b * us[it]); r * r }.coerceAtLeast(1e-9)
        val aicc = n * ln(rss / n) + 2 * p + 2.0 * p * (p + 1) / (n - p - 1)
        val inv = doubleArrayOf(suu / det, -su / det, -su / det, n / det)
        return ModelFit(model, a, b, shape, rss, n, aicc, inv)
    }
}

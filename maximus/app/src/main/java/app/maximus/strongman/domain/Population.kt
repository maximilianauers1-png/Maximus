package app.maximus.strongman.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/** Standard normal distribution: CDF via erfc and the quantile by Acklam's rational approximation. */
object Normal {
    /** erfc with fractional error < 1.2·10⁻⁷ everywhere (Numerical Recipes, Chebyshev fit). */
    fun erfc(x: Double): Double {
        val z = abs(x)
        val t = 1.0 / (1.0 + 0.5 * z)
        val r = t * exp(
            -z * z - 1.26551223 + t * (1.00002368 + t * (0.37409196 + t * (0.09678418 + t * (-0.18628806 +
                t * (0.27886807 + t * (-1.13520398 + t * (1.48851587 + t * (-0.82215223 + t * 0.17087277))))))))
        )
        return if (x >= 0) r else 2.0 - r
    }

    /** Φ(z) = ½ erfc(−z/√2). */
    fun cdf(z: Double): Double = 0.5 * erfc(-z / sqrt(2.0))

    fun pdf(z: Double): Double = exp(-0.5 * z * z) / sqrt(2.0 * Math.PI)

    /** Φ⁻¹(p), relative error < 1.2·10⁻⁹ (P. J. Acklam); p is clamped to (10⁻¹², 1 − 10⁻¹²). */
    fun quantile(p0: Double): Double {
        val p = p0.coerceIn(1e-12, 1 - 1e-12)
        val a = doubleArrayOf(-3.969683028665376e+01, 2.209460984245205e+02, -2.759285104469687e+02, 1.383577518672690e+02, -3.066479806614716e+01, 2.506628277459239e+00)
        val b = doubleArrayOf(-5.447609879822406e+01, 1.615858368580409e+02, -1.556989798598866e+02, 6.680131188771972e+01, -1.328068155288572e+01)
        val c = doubleArrayOf(-7.784894002430293e-03, -3.223964580411365e-01, -2.400758277161838e+00, -2.549732539343734e+00, 4.374664141464968e+00, 2.938163982698783e+00)
        val d = doubleArrayOf(7.784695709041462e-03, 3.224671290700398e-01, 2.445134137142996e+00, 3.754408661907416e+00)
        val low = 0.02425
        return when {
            p < low -> {
                val q = sqrt(-2 * ln(p))
                (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
            }
            p <= 1 - low -> {
                val q = p - 0.5
                val r = q * q
                (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q /
                    (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1)
            }
            else -> {
                val q = sqrt(-2 * ln(1 - p))
                -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
            }
        }
    }
}

enum class Population(val title: String, val blurb: String) {
    GYM("Gym", "Regelmäßig trainierende Freizeitsportler (≥ 1 Jahr Krafttraining)"),
    STRONGMAN("Strongman", "Wettkampf-Strongmen/-Strongwomen, Amateur- bis nationale Ebene"),
    POWERLIFTING("Powerlifting", "Raw-Wettkampfheber getesteter Verbände (Natural)")
}

enum class ScoreMode(val title: String) { DOTS("DOTS (relativ)"), ABSOLUTE("Absolut (kg)") }

/** The lifts that have population norms; [seedKeys] map logged exercises onto them (first match wins per set). */
enum class StandardLift(val title: String, val short: String, val seedKeys: List<String>) {
    SQUAT("Kniebeuge", "KB", listOf("back_squat")),
    BENCH("Bankdrücken", "BD", listOf("bench_press")),
    DEADLIFT("Kreuzheben", "KH", listOf("deadlift_conventional", "deadlift_sumo")),
    STRICT_PRESS("Strict Press", "OHP", listOf("strict_press")),
    LOG("Log Clean & Press", "Log", listOf("log_clean_press")),
    AXLE("Axle Clean & Press", "Axle", listOf("axle_clean_press", "axle_press")),
    AXLE_DEADLIFT("Axle-Kreuzheben", "AxDL", listOf("deadlift_axle")),
    STONE("Atlas Stone", "Stein", listOf("atlas_stone_over_bar", "atlas_stone_platform")),
    TOTAL("SBD-Total", "Total", emptyList());

    companion object {
        val singles: List<StandardLift> = entries.filter { it != TOTAL }
        fun forSeedKey(key: String?): StandardLift? = key?.let { k -> singles.firstOrNull { k in it.seedKeys } }
    }
}

/**
 * Lognormal model of one lift in one population: ln X ~ N(ln m, σ²).
 * [sigmaDots] is the log-spread of the bodyweight-normalized score; the absolute spread follows from
 * σ_abs² = σ_dots² + (β σ_lnBW)², with β ≈ 2/3 the allometric exponent that DOTS approximately removes
 * and σ_lnBW the log-spread of bodyweights in that population.
 */
data class LiftNorm(val medianKg: Double, val sigmaDots: Double, val sigmaLnBodyweight: Double) {
    val sigmaAbs: Double get() = sqrt(sigmaDots * sigmaDots + (ALLOMETRIC * sigmaLnBodyweight).let { it * it })

    companion object { const val ALLOMETRIC = 2.0 / 3.0 }
}

data class PopulationNorm(val population: Population, val sex: Sex, val refBodyweightKg: Double, val lifts: Map<StandardLift, LiftNorm>) {
    /** Median DOTS score of [lift] = median kg × DOTS coefficient at the reference bodyweight. */
    fun medianDots(lift: StandardLift): Double? =
        lifts[lift]?.let { n -> Dots.coefficient(sex, refBodyweightKg)?.let { n.medianKg * it } }
}

/** Result of placing one performance into one population. */
data class Placement(
    val lift: StandardLift,
    val population: Population,
    val mode: ScoreMode,
    val valueKg: Double,
    val score: Double,
    val z: Double,
    val percentile: Double,
    val rank: Rank,
    val rating: Int,
    /** Load at the athlete's bodyweight that reaches the next tier (null at the top tier). */
    val nextTierKg: Double?,
    /** Population median expressed as kg at the athlete's bodyweight (for DOTS) or absolute. */
    val medianKgEquivalent: Double,
    val sigma: Double
)

/**
 * Strength standards as lognormal populations.
 *
 * Parameters are estimates calibrated on public summaries: OpenPowerlifting distributions of tested raw
 * lifters (median male total ≈ 565 kg at ≈ 90 kg, DOTS ≈ 365), published strongman results of amateur to
 * national contests, and crowd-sourced gym strength standards for trained recreational lifters. They are
 * model values, not a census; a percentile is accurate to a few points in the middle and less so in the tails.
 */
object StrengthStandards {
    private fun n(m: Double, s: Double, bw: Double) = LiftNorm(m, s, bw)

    private val norms: List<PopulationNorm> = listOf(
        PopulationNorm(Population.GYM, Sex.MALE, 84.0, mapOf(
            StandardLift.SQUAT to n(125.0, 0.28, 0.16), StandardLift.BENCH to n(95.0, 0.28, 0.16),
            StandardLift.DEADLIFT to n(155.0, 0.27, 0.16), StandardLift.STRICT_PRESS to n(62.0, 0.27, 0.16),
            StandardLift.LOG to n(72.0, 0.30, 0.16), StandardLift.AXLE to n(68.0, 0.30, 0.16),
            StandardLift.AXLE_DEADLIFT to n(140.0, 0.29, 0.16), StandardLift.STONE to n(90.0, 0.30, 0.16),
            StandardLift.TOTAL to n(375.0, 0.25, 0.16)
        )),
        PopulationNorm(Population.POWERLIFTING, Sex.MALE, 90.0, mapOf(
            StandardLift.SQUAT to n(200.0, 0.19, 0.20), StandardLift.BENCH to n(135.0, 0.20, 0.20),
            StandardLift.DEADLIFT to n(230.0, 0.18, 0.20), StandardLift.STRICT_PRESS to n(85.0, 0.21, 0.20),
            StandardLift.LOG to n(100.0, 0.23, 0.20), StandardLift.AXLE to n(95.0, 0.23, 0.20),
            StandardLift.AXLE_DEADLIFT to n(200.0, 0.21, 0.20), StandardLift.STONE to n(120.0, 0.25, 0.20),
            StandardLift.TOTAL to n(565.0, 0.17, 0.20)
        )),
        PopulationNorm(Population.STRONGMAN, Sex.MALE, 108.0, mapOf(
            StandardLift.SQUAT to n(230.0, 0.18, 0.17), StandardLift.BENCH to n(150.0, 0.19, 0.17),
            StandardLift.DEADLIFT to n(280.0, 0.17, 0.17), StandardLift.STRICT_PRESS to n(110.0, 0.18, 0.17),
            StandardLift.LOG to n(130.0, 0.18, 0.17), StandardLift.AXLE to n(125.0, 0.18, 0.17),
            StandardLift.AXLE_DEADLIFT to n(260.0, 0.17, 0.17), StandardLift.STONE to n(160.0, 0.17, 0.17),
            StandardLift.TOTAL to n(660.0, 0.16, 0.17)
        )),
        PopulationNorm(Population.GYM, Sex.FEMALE, 66.0, mapOf(
            StandardLift.SQUAT to n(72.0, 0.30, 0.17), StandardLift.BENCH to n(45.0, 0.30, 0.17),
            StandardLift.DEADLIFT to n(95.0, 0.29, 0.17), StandardLift.STRICT_PRESS to n(35.0, 0.29, 0.17),
            StandardLift.LOG to n(40.0, 0.31, 0.17), StandardLift.AXLE to n(38.0, 0.31, 0.17),
            StandardLift.AXLE_DEADLIFT to n(85.0, 0.30, 0.17), StandardLift.STONE to n(50.0, 0.31, 0.17),
            StandardLift.TOTAL to n(212.0, 0.27, 0.17)
        )),
        PopulationNorm(Population.POWERLIFTING, Sex.FEMALE, 68.0, mapOf(
            StandardLift.SQUAT to n(125.0, 0.20, 0.20), StandardLift.BENCH to n(67.5, 0.21, 0.20),
            StandardLift.DEADLIFT to n(145.0, 0.19, 0.20), StandardLift.STRICT_PRESS to n(47.0, 0.22, 0.20),
            StandardLift.LOG to n(55.0, 0.24, 0.20), StandardLift.AXLE to n(52.0, 0.24, 0.20),
            StandardLift.AXLE_DEADLIFT to n(125.0, 0.22, 0.20), StandardLift.STONE to n(70.0, 0.25, 0.20),
            StandardLift.TOTAL to n(337.0, 0.18, 0.20)
        )),
        PopulationNorm(Population.STRONGMAN, Sex.FEMALE, 78.0, mapOf(
            StandardLift.SQUAT to n(145.0, 0.19, 0.18), StandardLift.BENCH to n(82.0, 0.20, 0.18),
            StandardLift.DEADLIFT to n(180.0, 0.18, 0.18), StandardLift.STRICT_PRESS to n(62.0, 0.19, 0.18),
            StandardLift.LOG to n(72.0, 0.19, 0.18), StandardLift.AXLE to n(68.0, 0.19, 0.18),
            StandardLift.AXLE_DEADLIFT to n(160.0, 0.18, 0.18), StandardLift.STONE to n(100.0, 0.18, 0.18),
            StandardLift.TOTAL to n(407.0, 0.17, 0.18)
        ))
    )

    fun norm(population: Population, sex: Sex): PopulationNorm = norms.first { it.population == population && it.sex == sex }

    /**
     * Places [kg] of [lift] into [population].
     *  ABSOLUTE: z = (ln kg − ln m)/σ_abs, bodyweight ignored.
     *  DOTS:     z = (ln(kg·c(bw)) − ln(m·c(bw_ref)))/σ_dots, which needs the athlete's bodyweight.
     * Returns null if the lift has no norm, kg ≤ 0, or DOTS is asked for without a valid bodyweight.
     */
    fun place(lift: StandardLift, kg: Double, population: Population, sex: Sex, bodyweightKg: Double?, mode: ScoreMode): Placement? {
        if (!(kg > 0)) return null
        val pn = norm(population, sex)
        val norm = pn.lifts[lift] ?: return null
        return when (mode) {
            ScoreMode.ABSOLUTE -> {
                val z = (ln(kg) - ln(norm.medianKg)) / norm.sigmaAbs
                build(lift, population, mode, kg, kg, z, norm.medianKg, norm.sigmaAbs) { zt -> norm.medianKg * exp(norm.sigmaAbs * zt) }
            }
            ScoreMode.DOTS -> {
                val bw = bodyweightKg ?: return null
                val c = Dots.coefficient(sex, bw) ?: return null
                val med = pn.medianDots(lift) ?: return null
                val score = kg * c
                val z = (ln(score) - ln(med)) / norm.sigmaDots
                build(lift, population, mode, kg, score, z, med / c, norm.sigmaDots) { zt -> med * exp(norm.sigmaDots * zt) / c }
            }
        }
    }

    private inline fun build(
        lift: StandardLift, population: Population, mode: ScoreMode, kg: Double, score: Double, z: Double,
        medianKg: Double, sigma: Double, kgAt: (Double) -> Double
    ): Placement {
        val pct = 100 * Normal.cdf(z)
        val rank = Ranks.forPercentile(pct)
        val next = Ranks.next(rank)?.let { kgAt(Normal.quantile(it.minPercentile / 100.0)) }
        return Placement(lift, population, mode, kg, score, z, pct, rank, Ranks.rating(z), next, medianKg, sigma)
    }

    /**
     * Lognormal density of the population expressed in kg at the athlete's bodyweight, for plotting:
     * f(x) = φ((ln x − ln m)/σ) / (x σ). Returned as (x, f) on [m e^{−3σ}, m e^{+3.2σ}].
     */
    fun density(p: Placement, points: Int = 120): List<Pair<Double, Double>> {
        val m = p.medianKgEquivalent
        val s = p.sigma
        val lo = m * exp(-3.0 * s)
        val hi = max(m * exp(3.2 * s), p.valueKg * 1.08)
        return (0 until points).map { i ->
            val x = lo + (hi - lo) * i / (points - 1)
            x to Normal.pdf((ln(x) - ln(m)) / s) / (x * s)
        }
    }

    /**
     * Combined standing over several lifts. The mean of k correlated standard scores with pairwise
     * correlation ρ has variance (1 + (k−1)ρ)/k, so z̄ is rescaled by that to be standard normal again;
     * ρ ≈ 0.7 is typical for the correlation of maximal strength between the big lifts.
     */
    fun combinedZ(zs: List<Double>, rho: Double = 0.7): Double? {
        if (zs.isEmpty()) return null
        val k = zs.size
        val mean = zs.average()
        return mean / sqrt((1 + (k - 1) * rho) / k)
    }
}

/** Epic tiers, defined on the population percentile. */
enum class Rank(val title: String, val minPercentile: Double, val motto: String) {
    KNAPPE("Knappe", 0.0, "Jeder Titan hat einmal die Stange leer gehoben."),
    WAFFENKNECHT("Waffenknecht", 20.0, "Die Grundlagen sitzen – jetzt wird geschmiedet."),
    LANDSKNECHT("Landsknecht", 40.0, "Mitten im Haufen, bereit für den Durchbruch."),
    RITTER("Ritter", 60.0, "Stärker als die meisten, die je eine Halle betreten haben."),
    HAUPTMANN("Hauptmann", 75.0, "Andere schauen zu, wenn du ziehst."),
    KRIEGSHERR("Kriegsherr", 85.0, "Eisen gehorcht dir."),
    RECKE("Recke", 92.0, "Ein Name, den man in Wettkampflisten liest."),
    TITAN("Titan", 96.0, "Die Kraft der alten Götter."),
    JOTUN("Jötunn", 98.5, "Riesenkraft – kaum jemand steht über dir."),
    LEGENDE("Legende", 99.5, "Man wird von dir erzählen.");

    val tier: Int get() = ordinal
}

object Ranks {
    fun forPercentile(p: Double): Rank = Rank.entries.last { p >= it.minPercentile }

    fun next(rank: Rank): Rank? = Rank.entries.getOrNull(rank.ordinal + 1)

    /** Sub-division I–III inside a tier (III = just entered, I = about to rank up), equal thirds in z. */
    fun division(p: Double): Int {
        val r = forPercentile(p)
        val nx = next(r) ?: return 1
        val z0 = if (r.minPercentile <= 0.0) -3.0 else Normal.quantile(r.minPercentile / 100)
        val z1 = Normal.quantile(nx.minPercentile / 100)
        val z = Normal.quantile(p / 100)
        val f = ((z - z0) / (z1 - z0)).coerceIn(0.0, 0.9999)
        return 3 - (f * 3).toInt()
    }

    /** Progress 0..1 through the current tier, measured in z (so every tier feels equally long). */
    fun tierProgress(p: Double): Double {
        val r = forPercentile(p)
        val nx = next(r) ?: return 1.0
        val z0 = if (r.minPercentile <= 0.0) -3.0 else Normal.quantile(r.minPercentile / 100)
        val z1 = Normal.quantile(nx.minPercentile / 100)
        return ((Normal.quantile(p / 100) - z0) / (z1 - z0)).coerceIn(0.0, 1.0)
    }

    /** Strength rating, Elo-like: 1000 at the median, ±250 per standard deviation. */
    fun rating(z: Double): Int = (1000 + 250 * z).toInt().coerceAtLeast(0)

    fun roman(division: Int): String = when (division) { 1 -> "I"; 2 -> "II"; else -> "III" }
}

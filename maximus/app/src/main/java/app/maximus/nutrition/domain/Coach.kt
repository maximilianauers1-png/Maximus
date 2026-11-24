package app.maximus.nutrition.domain

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The plan prescribed by the athlete's coach (Leicht Coaching, "Fuel strong. Spend smart.", maintenance phase).
 * These numbers are the anchor of the whole module: every phase is derived from them, never invented freely.
 */
object CoachPlan {
    const val COACH_NAME = "Mikkel Leicht"
    const val KCAL = 3700.0
    const val PROTEIN_G = 205.0
    const val CARB_G = 475.0
    const val FAT_G = 110.0
    /** Estimated maintenance from the Week-41 food log (mean intake ≈ 3771 kcal/d at stable weight). */
    val MAINTENANCE_RANGE = 3700.0..3900.0
    val BUDGET_SEK_PER_DAY = 85.0..100.0

    val targets = MacroTargets(KCAL, PROTEIN_G, FAT_G, CARB_G, 14.0 * KCAL / 1000.0)

    /** Slot of the day → coach meal; slot order M1…M5 is also the order used by the week store. */
    val mealKeys = listOf("coach_m1", "coach_m2", "coach_m3", "coach_m4", "coach_m5")

    val meals: List<Recipe> get() = mealKeys.map { Recipes.byKey.getValue(it) }
    val dayTotal: Nutrients get() = meals.fold(Nutrients.ZERO) { a, r -> a + r.nutrients }
    val dayCostSek: Double get() = meals.sumOf { it.costSek ?: 0.0 }
}

/**
 * Coaching phases with their target rate of weight change r* in % of body mass per week.
 *  MAINTENANCE: the coach plan unchanged.
 *  RECOMP: −0.25 %/wk; small deficit, body recomposition is realistic for trained lifters with
 *          adequate protein (Barakat et al., Strength Cond J 2020).
 *  CUT: −0.7 %/wk; the slow-loss group of Garthe et al. (IJSNEM 2011, 0.7 %/wk) kept strength and gained
 *       lean mass while the fast group (1.4 %/wk) did not; Helms et al. (JISSN 2014) recommend 0.5–1 %/wk.
 *  DIET_BREAK: two weeks at maintenance after 8–12 weeks of deficit (MATADOR, Byrne et al., IJO 2018).
 */
enum class CoachPhase(val targetRatePercent: Double) {
    MAINTENANCE(0.0), RECOMP(-0.25), CUT(-0.7), DIET_BREAK(0.0)
}

data class CoachTargets(
    val phase: CoachPhase,
    /** Maintenance energy the phase is derived from (coach value or calibrated by the log). */
    val maintenanceKcal: Double,
    val maintenanceCalibrated: Boolean,
    /** Planned deficit from the target rate before check-in corrections, kcal/d. */
    val plannedDeficit: Double,
    /** Sum of accepted weekly check-in corrections, kcal/d. */
    val adjustment: Double,
    val weightKg: Double,
    val macros: MacroTargets
)

data class CompositionGoal(
    val leanKg: Double,
    val fatKg: Double,
    val targetWeightKg: Double,
    val fatToLoseKg: Double,
    /** Weeks at the phase rate (compounded), null if the phase does not lose weight or the goal is reached. */
    val weeks: Double?
)

enum class CheckInVerdict { NEED_DATA, ADHERENCE, ON_TRACK, ADJUST_DOWN, ADJUST_UP }

enum class CheckInNote { PROTEIN_LOW, LOSS_TOO_FAST, DIET_BREAK_DUE, LOG_INTAKE, END_DIET_BREAK }

data class CheckIn(
    val verdict: CheckInVerdict,
    val weighIns: Int,
    val intakeDays: Int,
    /** Observed rate of change of the trend, % body mass per week (OLS over 14 days). */
    val rateObserved: Double?,
    val rateSe: Double?,
    val rateTarget: Double,
    val meanKcal: Double?,
    val meanProtein: Double?,
    /** Recommended change of the daily energy target, kcal/d (0 = keep). */
    val kcalChange: Double,
    val notes: Set<CheckInNote>
)

data class DayLogPoint(val epochDay: Long, val weightKg: Double?, val kcal: Double?, val proteinG: Double?)

/**
 * The coaching engine. Units: kcal/d, g/d, kg, % body mass per week. ρ = 7700 kcal/kg (mixed tissue).
 */
object CoachEngine {
    const val RHO = WeightModel.RHO_MIXED
    /** Largest deficit relative to maintenance (≈ 25 %; beyond that lean-mass loss rises sharply). */
    const val MAX_DEFICIT_FRACTION = 0.25
    /** Damping of the weekly controller: only half of the measured error is corrected per week. */
    const val GAIN = 0.5
    const val MAX_STEP = 250.0
    const val STEP_ROUNDING = 25.0
    const val DIET_BREAK_AFTER_DAYS = 8 * 7
    const val DIET_BREAK_LENGTH_DAYS = 14

    /**
     * Energy and macro targets of a phase.
     *
     *   E_m  = coach maintenance (or log-calibrated TDEE when σ < 200 kcal),
     *   D    = −r* · m · ρ / (7 · 100),  limited to 25 % of E_m,
     *   E    = E_m − D + A        (A = accepted check-in corrections),
     *   P    = max(P_coach, k_P · FFM) with k_P = 2.5 (recomp) / 2.8 (cut) g/kg FFM
     *          (Helms 2014: 2.3–3.1 g/kg FFM in a deficit); FFM ≈ 0.85 m if body fat is unknown,
     *   F    = clamp(0.25 E / 9, 0.8 m, F_coach)  in a deficit; F_coach at maintenance,
     *   C    = (E − 4P − 9F) / 4  (training fuel keeps the remainder).
     *
     * At maintenance without corrections the coach numbers are returned verbatim.
     */
    fun targets(
        phase: CoachPhase,
        weightKg: Double,
        bodyFatPercent: Double?,
        adjustment: Double,
        calibratedTdee: TdeeEstimate? = null
    ): CoachTargets {
        val calibrated = calibratedTdee != null && calibratedTdee.sigma.isFinite() && calibratedTdee.sigma < 200.0
        val em = if (calibrated) calibratedTdee!!.tdee else CoachPlan.KCAL
        val ffm = weightKg * (1 - (bodyFatPercent ?: 15.0) / 100.0)
        val deficit = min(-phase.targetRatePercent / 100.0 * weightKg * RHO / 7.0, MAX_DEFICIT_FRACTION * em).coerceAtLeast(0.0)
        val e = (em - deficit + adjustment).coerceIn((1 - MAX_DEFICIT_FRACTION) * em, em + 1000.0)
        val macros = if (deficit == 0.0 && adjustment == 0.0 && !calibrated) CoachPlan.targets else {
            val kP = when (phase) { CoachPhase.CUT -> 2.8; CoachPhase.RECOMP -> 2.5; else -> 0.0 }
            val p = max(CoachPlan.PROTEIN_G, kP * ffm)
            val f = if (deficit > 0) (0.25 * e / 9.0).coerceIn(min(0.8 * weightKg, CoachPlan.FAT_G), CoachPlan.FAT_G) else CoachPlan.FAT_G
            val c = (e - Atwater.PROTEIN * p - Atwater.FAT * f) / Atwater.CARB
            MacroTargets(e, p, f, c, 14.0 * e / 1000.0)
        }
        return CoachTargets(phase, em, calibrated, deficit, adjustment, weightKg, macros)
    }

    /**
     * Target weight at constant lean mass: m* = FFM / (1 − bf*). Weeks at the phase rate r with compounding
     * m_k = m₀ (1 + r/100)^k ⇒ k = ln(m* ÷ m₀) / ln(1 + r/100). Assumes lean mass is preserved, which needs
     * adequate protein and progressive resistance training.
     */
    fun composition(weightKg: Double, bodyFatPercent: Double, targetBodyFatPercent: Double, ratePercent: Double): CompositionGoal {
        val lean = weightKg * (1 - bodyFatPercent / 100.0)
        val fat = weightKg - lean
        val target = lean / (1 - targetBodyFatPercent / 100.0)
        val toLose = max(0.0, weightKg - target)
        val weeks = if (toLose > 0 && ratePercent < 0) ln(target / weightKg) / ln(1 + ratePercent / 100.0) else null
        return CompositionGoal(lean, fat, target, toLose, weeks)
    }

    /** Trend weight: EWMA (α = 0.1) of all weigh-ins, null without any. */
    fun trendWeight(points: List<DayLogPoint>): Double? =
        points.filter { it.weightKg != null }.sortedBy { it.epochDay }.map { it.weightKg!! }.takeIf { it.isNotEmpty() }?.let { AdaptiveTdee.ewma(it).last() }

    /**
     * Weekly check-in, a damped proportional controller on the weight trend.
     *
     *  1. Rate: OLS slope b ± σ_b over weigh-ins of the last 14 days (≥ 6 weigh-ins spanning ≥ 9 days),
     *     r = 700 b / m (%/wk), σ_r = 700 σ_b / m.
     *  2. Adherence first: if ≥ 4 of the last 7 days have logged intake and their mean deviates more than
     *     10 % from the target, the target is not the problem, so nothing is changed.
     *  3. Error e = r − r*. Dead band τ = max(0.15 %/wk, σ_r): inside it the rate is on track (daily water
     *     and glycogen swings of 0.5–1 kg make smaller corrections noise-driven).
     *  4. Correction ΔE = −K · e/100 · m · ρ/7 with K = 0.5, limited to ±250 kcal/d and rounded to 25 kcal.
     */
    fun checkIn(
        points: List<DayLogPoint>,
        today: Long,
        weightKg: Double,
        targets: CoachTargets,
        phaseStartDay: Long?
    ): CheckIn {
        val notes = LinkedHashSet<CheckInNote>()
        val rTarget = targets.phase.targetRatePercent
        val weights = points.filter { it.weightKg != null && it.epochDay > today - 14 && it.epochDay <= today }
        val intake = points.filter { it.epochDay > today - 7 && it.epochDay <= today }
        val kcals = intake.mapNotNull { it.kcal }
        val proteins = intake.mapNotNull { it.proteinG }
        val meanKcal = kcals.takeIf { it.isNotEmpty() }?.average()
        val meanProtein = proteins.takeIf { it.isNotEmpty() }?.average()
        if (proteins.size >= 3 && meanProtein!! < 0.9 * targets.macros.proteinG) notes += CheckInNote.PROTEIN_LOW
        if (kcals.size < 4) notes += CheckInNote.LOG_INTAKE
        if (phaseStartDay != null) {
            val days = today - phaseStartDay
            if (targets.phase == CoachPhase.CUT && days >= DIET_BREAK_AFTER_DAYS) notes += CheckInNote.DIET_BREAK_DUE
            if (targets.phase == CoachPhase.DIET_BREAK && days >= DIET_BREAK_LENGTH_DAYS) notes += CheckInNote.END_DIET_BREAK
        }

        val span = if (weights.size >= 2) weights.maxOf { it.epochDay } - weights.minOf { it.epochDay } else 0L
        if (weights.size < 6 || span < 9) {
            return CheckIn(CheckInVerdict.NEED_DATA, weights.size, kcals.size, null, null, rTarget, meanKcal, meanProtein, 0.0, notes)
        }
        val (b, se) = AdaptiveTdee.slope(weights.map { it.epochDay.toDouble() }, weights.map { it.weightKg!! })
        val r = 700.0 * b / weightKg
        val sr = if (se.isFinite()) 700.0 * se / weightKg else 0.0
        if (r < -1.0) notes += CheckInNote.LOSS_TOO_FAST

        if (kcals.size >= 4 && abs(meanKcal!! / targets.macros.kcal - 1) > 0.10) {
            return CheckIn(CheckInVerdict.ADHERENCE, weights.size, kcals.size, r, sr, rTarget, meanKcal, meanProtein, 0.0, notes)
        }
        val e = r - rTarget
        val tau = max(0.15, sr)
        val raw = if (abs(e) <= tau) 0.0 else -GAIN * e / 100.0 * weightKg * RHO / 7.0
        val step = ((raw.coerceIn(-MAX_STEP, MAX_STEP) / STEP_ROUNDING).roundToInt() * STEP_ROUNDING)
        val verdict = when {
            step == 0.0 -> CheckInVerdict.ON_TRACK
            step < 0 -> CheckInVerdict.ADJUST_DOWN
            else -> CheckInVerdict.ADJUST_UP
        }
        return CheckIn(verdict, weights.size, kcals.size, r, sr, rTarget, meanKcal, meanProtein, step, notes)
    }

    /**
     * Protein distribution: meals below 0.25 g/kg body mass are under the leucine threshold that maximally
     * stimulates muscle protein synthesis (Moore et al., J Gerontol 2015; Schoenfeld & Aragon, JISSN 2018).
     */
    fun lowProteinMeals(meals: List<Recipe>, weightKg: Double, portions: List<Double> = List(meals.size) { 1.0 }): List<Int> =
        meals.indices.filter { meals[it].nutrients.proteinG * portions[it] < 0.25 * weightKg }
}

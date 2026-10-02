package app.maximus.strongman.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

object Rpe {
    const val MIN = 5.0
    const val MAX = 10.0

    /** All admissible RPE values 5.0, 5.5, ..., 10.0. */
    val values: List<Double> = (10..20).map { it / 2.0 }

    fun isValid(rpe: Double): Boolean {
        val twice = rpe * 2.0
        return rpe in MIN..MAX && abs(twice - twice.roundToInt()) < 1e-9
    }

    fun tenths(rpe: Double): Int = (rpe * 10.0).roundToInt()

    fun rir(rpe: Double): Double = 10.0 - rpe

    /** Snaps to the nearest half point, clamped to [MIN, MAX]. */
    fun snap(rpe: Double): Double = (floor(rpe * 2.0 + 0.5) / 2.0).coerceIn(MIN, MAX)
}

data class FKey(val reps: Int, val rpeTenths: Int)

/**
 * Fraction table F(r, RPE) = 1 / (1 + (r + 10 - RPE)/30) with user overrides.
 * Both directions use the SAME table, so e1RM(w, r, RPE) = w / F(r, RPE) and
 * prescribe(1RM, r, RPE) = 1RM * F(r, RPE) are exact inverses before rounding.
 */
class FTable(val overrides: Map<FKey, Double> = emptyMap()) {

    init {
        overrides.values.forEach { require(it > 0.0 && it <= 1.0) { "F must lie in (0, 1]" } }
    }

    fun formula(reps: Int, rpe: Double): Double = 1.0 / (1.0 + (reps + 10.0 - rpe) / 30.0)

    fun isOverridden(reps: Int, rpe: Double): Boolean = FKey(reps, Rpe.tenths(rpe)) in overrides

    fun fraction(reps: Int, rpe: Double): Double =
        overrides[FKey(reps, Rpe.tenths(rpe))] ?: formula(reps, rpe)

    fun e1rm(w: Double, reps: Int, rpe: Double): OneRmResult = when {
        !(w > 0.0) -> OneRmResult.Refused(RefusalReason.NON_POSITIVE_LOAD)
        reps < 1 -> OneRmResult.Refused(RefusalReason.REPS_BELOW_ONE)
        reps > OneRepMax.MAX_REPS -> OneRmResult.Refused(RefusalReason.REPS_ABOVE_LIMIT)
        !Rpe.isValid(rpe) -> OneRmResult.Refused(RefusalReason.INVALID_RPE)
        else -> OneRmResult.Estimate(w / fraction(reps, rpe), OneRmMethod.RPE_TABLE)
    }

    fun prescribe(oneRm: Double, reps: Int, rpe: Double, increment: Double): Double =
        roundToIncrement(oneRm * fraction(reps, rpe), increment)
}

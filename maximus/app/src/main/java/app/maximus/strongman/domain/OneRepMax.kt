package app.maximus.strongman.domain

import kotlin.math.floor
import kotlin.math.pow

enum class OneRmMethod { EPLEY, BRZYCKI, LOMBARDI, MEAN_EPLEY_BRZYCKI, RPE_TABLE }

enum class RefusalReason { NON_POSITIVE_LOAD, REPS_BELOW_ONE, REPS_ABOVE_LIMIT, INVALID_RPE }

sealed interface OneRmResult {
    data class Estimate(val kg: Double, val method: OneRmMethod) : OneRmResult
    data class Refused(val reason: RefusalReason) : OneRmResult
}

/** Rounds to the nearest multiple of [increment], halves rounded up (not banker's rounding). */
fun roundToIncrement(x: Double, increment: Double): Double {
    require(increment > 0.0) { "increment must be positive" }
    return increment * floor(x / increment + 0.5)
}

object OneRepMax {
    const val MAX_REPS = 15
    const val BRZYCKI_MAX_REPS = 10

    fun epley(w: Double, r: Int): Double = w * (1.0 + r / 30.0)

    fun brzycki(w: Double, r: Int): Double {
        require(r in 1..36) { "Brzycki is undefined for r >= 37" }
        return w * 36.0 / (37.0 - r)
    }

    fun lombardi(w: Double, r: Int): Double = w * r.toDouble().pow(0.10)

    /** Brief 3.1 default: mean(Epley, Brzycki) for r <= 10, Epley for 11..15, refuse r > 15. */
    fun estimate(w: Double, r: Int): OneRmResult = when {
        !(w > 0.0) -> OneRmResult.Refused(RefusalReason.NON_POSITIVE_LOAD)
        r < 1 -> OneRmResult.Refused(RefusalReason.REPS_BELOW_ONE)
        r > MAX_REPS -> OneRmResult.Refused(RefusalReason.REPS_ABOVE_LIMIT)
        r <= BRZYCKI_MAX_REPS ->
            OneRmResult.Estimate((epley(w, r) + brzycki(w, r)) / 2.0, OneRmMethod.MEAN_EPLEY_BRZYCKI)
        else -> OneRmResult.Estimate(epley(w, r), OneRmMethod.EPLEY)
    }
}

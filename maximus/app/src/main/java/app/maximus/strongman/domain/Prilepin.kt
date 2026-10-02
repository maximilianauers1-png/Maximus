package app.maximus.strongman.domain

/**
 * Zones as half-open intervals [min, max) on %1RM. The brief's zones overlap at 80 %
 * and leave 65-70 % undefined; here 55 <= p < 70, 70 <= p < 80, 80 <= p < 90, p >= 90.
 */
data class PrilepinZone(
    val minPercent: Double,
    val maxPercentExclusive: Double,
    val repsPerSet: IntRange,
    val optimalTotal: Int,
    val totalRange: IntRange
)

enum class PrilepinVerdict { BELOW_RANGE, WITHIN_RANGE, ABOVE_RANGE }

data class PrilepinAssessment(val zone: PrilepinZone, val verdict: PrilepinVerdict, val deviationFromOptimal: Int)

object Prilepin {
    val zones: List<PrilepinZone> = listOf(
        PrilepinZone(55.0, 70.0, 3..6, 24, 18..30),
        PrilepinZone(70.0, 80.0, 3..6, 18, 12..24),
        PrilepinZone(80.0, 90.0, 2..4, 15, 10..20),
        PrilepinZone(90.0, Double.POSITIVE_INFINITY, 1..2, 7, 4..10)
    )

    fun zoneFor(percent: Double): PrilepinZone? =
        zones.firstOrNull { percent >= it.minPercent && percent < it.maxPercentExclusive }

    fun assess(percent: Double, totalReps: Int): PrilepinAssessment? {
        val zone = zoneFor(percent) ?: return null
        val verdict = when {
            totalReps < zone.totalRange.first -> PrilepinVerdict.BELOW_RANGE
            totalReps > zone.totalRange.last -> PrilepinVerdict.ABOVE_RANGE
            else -> PrilepinVerdict.WITHIN_RANGE
        }
        return PrilepinAssessment(zone, verdict, totalReps - zone.optimalTotal)
    }
}

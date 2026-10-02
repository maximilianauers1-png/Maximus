package app.maximus.strongman.domain

data class WarmupStep(val kg: Double, val reps: Int)

object WarmupLadder {
    val DEFAULT_SCHEME: List<Pair<Double, Int>> =
        listOf(0.40 to 5, 0.55 to 3, 0.70 to 2, 0.80 to 1, 0.90 to 1)

    /** Empty bar first, then fractions of the working weight; strictly increasing, below the work set. */
    fun build(
        workKg: Double,
        barKg: Double,
        incrementKg: Double,
        scheme: List<Pair<Double, Int>> = DEFAULT_SCHEME,
        barReps: Int = 10
    ): List<WarmupStep> {
        if (workKg <= barKg) return emptyList()
        val steps = mutableListOf(WarmupStep(barKg, barReps))
        for ((fraction, reps) in scheme) {
            val kg = maxOf(barKg, roundToIncrement(fraction * workKg, incrementKg))
            if (kg > steps.last().kg && kg < workKg) steps += WarmupStep(kg, reps)
        }
        return steps
    }
}

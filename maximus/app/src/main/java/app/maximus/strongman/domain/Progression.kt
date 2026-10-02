package app.maximus.strongman.domain

import kotlin.math.floor

object ProgramGenerator {
    const val DELOAD_FACTOR = 0.6
    const val MAX_WEEKS = 16

    fun isDeloadWeek(week: Int, deloadEvery: Int): Boolean = deloadEvery > 0 && week % deloadEvery == 0

    /**
     * Progression level of 1-based [week]: deload weeks repeat the previous level and do not
     * advance progression, level(w) = w - floor(w / D) for D > 0, otherwise w.
     */
    fun progressionLevel(week: Int, deloadEvery: Int): Int =
        if (deloadEvery > 0) week - week / deloadEvery else week

    fun generate(base: ProgramWeek, weeks: Int, deloadEvery: Int): List<ProgramWeek> {
        require(weeks in 1..MAX_WEEKS) { "weeks must be in 1..$MAX_WEEKS" }
        require(deloadEvery >= 0)
        return (1..weeks).map { w ->
            val level = progressionLevel(w, deloadEvery)
            val deload = isDeloadWeek(w, deloadEvery)
            ProgramWeek(base.days.map { day ->
                day.copy(exercises = day.exercises.map { ex ->
                    val progressed = ex.sets.map { applyRule(it, ex.rule, level) }
                    ex.copy(sets = if (deload) deload(progressed) else progressed)
                })
            })
        }
    }

    fun applyRule(set: SetPrescription, rule: ProgressionRule, level: Int): SetPrescription {
        val k = level - 1
        return when (rule) {
            ProgressionRule.None -> set
            is ProgressionRule.Linear -> when (val l = set.load) {
                is LoadSpec.Absolute -> set.copy(load = LoadSpec.Absolute(l.kg + rule.kgPerWeek * k))
                is LoadSpec.PercentOneRm -> set.copy(load = LoadSpec.PercentOneRm(l.percent + rule.percentPerWeek * k))
                else -> set
            }
            is ProgressionRule.DoubleProgression -> {
                val span = (rule.maxReps - rule.minReps + 1).coerceAtLeast(1)
                val reps = rule.minReps + k % span
                val bumps = k / span
                val load = when (val l = set.load) {
                    is LoadSpec.Absolute -> LoadSpec.Absolute(l.kg + rule.incrementKg * bumps)
                    else -> l
                }
                set.copy(reps = reps, load = load)
            }
            is ProgressionRule.RpeAutoregulation -> when (val l = set.load) {
                is LoadSpec.AtRpe -> set.copy(load = LoadSpec.AtRpe(Rpe.snap(l.rpe + rule.rpeStepPerWeek * k)))
                else -> set
            }
            is ProgressionRule.Wave -> {
                val len = rule.length.coerceAtLeast(1)
                val p = rule.stepPercent * (k % len) + rule.wavePercent * (k / len)
                when (val l = set.load) {
                    is LoadSpec.PercentOneRm -> set.copy(load = LoadSpec.PercentOneRm(l.percent + p))
                    is LoadSpec.Absolute -> set.copy(load = LoadSpec.Absolute(l.kg * (1.0 + p / 100.0)))
                    else -> set
                }
            }
        }
    }

    /**
     * Volume x 0.6 at constant intensity: the total repetitions R of an exercise are cut to
     * T = max(1, round(0.6 R)) by keeping sets in order and trimming the last kept set, so the
     * volume load sum(w r) of equal-load sets is scaled by exactly T/R. Distances scale by 0.6.
     */
    fun deload(sets: List<SetPrescription>, factor: Double = DELOAD_FACTOR): List<SetPrescription> {
        if (sets.isEmpty()) return sets
        val total = sets.sumOf { it.reps }
        val target = maxOf(1, floor(factor * total + 0.5).toInt())
        var remaining = target
        val out = mutableListOf<SetPrescription>()
        for (s in sets) {
            if (remaining <= 0) break
            val reps = minOf(s.reps, remaining)
            out += s.copy(
                reps = reps,
                event = s.event?.let { e -> e.copy(distanceM = e.distanceM?.times(factor)) }
            )
            remaining -= reps
        }
        return out
    }
}

/** Log-driven double progression for a single exercise. */
object DoubleProgressionLogic {
    data class Next(val kg: Double, val targetReps: Int)

    fun next(kg: Double, targetReps: Int, minReps: Int, maxReps: Int, incrementKg: Double, achieved: List<Int>): Next =
        when {
            achieved.isEmpty() -> Next(kg, targetReps)
            achieved.all { it >= maxReps } -> Next(kg + incrementKg, minReps)
            achieved.all { it >= targetReps } -> Next(kg, minOf(targetReps + 1, maxReps))
            else -> Next(kg, targetReps)
        }
}

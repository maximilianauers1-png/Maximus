package app.maximus.strongman.domain

sealed interface LoadSpec {
    data class Absolute(val kg: Double) : LoadSpec
    data class PercentOneRm(val percent: Double) : LoadSpec
    data class AtRpe(val rpe: Double) : LoadSpec
    data class BodyweightRelative(val factor: Double) : LoadSpec
}

enum class EventMode { MAX_REPS, MAX_LOAD, FOR_TIME, MAX_DISTANCE, MAX_HEIGHT, MAX_HOLD_TIME }

data class EventSpec(
    val distanceM: Double? = null,
    val heightCm: Double? = null,
    val implementKg: Double? = null,
    val timeCapS: Int? = null,
    val mode: EventMode = EventMode.MAX_REPS
)

data class SetPrescription(
    val reps: Int,
    val load: LoadSpec,
    val restSeconds: Int = 180,
    val note: String = "",
    val event: EventSpec? = null
)

sealed interface ProgressionRule {
    data object None : ProgressionRule
    /** Absolute loads + kgPerWeek, %1RM loads + percentPerWeek (percentage points). */
    data class Linear(val kgPerWeek: Double, val percentPerWeek: Double) : ProgressionRule
    /** Planned double progression: reps climb minReps..maxReps, then load + incrementKg and reps reset. */
    data class DoubleProgression(val minReps: Int, val maxReps: Int, val incrementKg: Double) : ProgressionRule
    /** RPE targets rise by rpeStepPerWeek; loads resolve from the latest e1RM at training time. */
    data class RpeAutoregulation(val rpeStepPerWeek: Double) : ProgressionRule
    /** p(k) = stepPercent * (k mod length) + wavePercent * floor(k / length), k = level - 1. */
    data class Wave(val stepPercent: Double, val wavePercent: Double, val length: Int) : ProgressionRule
}

data class ProgramExercise(val exerciseId: Long, val rule: ProgressionRule, val sets: List<SetPrescription>)

data class ProgramDay(val name: String, val exercises: List<ProgramExercise>)

data class ProgramWeek(val days: List<ProgramDay>)

data class Program(
    val id: Long,
    val name: String,
    val isTemplate: Boolean,
    val deloadEvery: Int,
    val weeks: List<ProgramWeek>
) {
    fun withWeek(index: Int, week: ProgramWeek): Program =
        copy(weeks = weeks.mapIndexed { i, w -> if (i == index) week else w })

    /** Copies week [from] onto week [to] (0-based). */
    fun copyWeek(from: Int, to: Int): Program = withWeek(to, weeks[from])
}

package app.maximus.strongman.domain

data class LoadContext(
    val oneRmKg: Double?,
    val bodyweightKg: Double?,
    val incrementKg: Double,
    val fTable: FTable
)

object LoadResolver {
    /** Concrete training load in kg, or null if the required reference (1RM / bodyweight) is unknown. */
    fun resolve(set: SetPrescription, ctx: LoadContext): Double? = when (val l = set.load) {
        is LoadSpec.Absolute -> l.kg
        is LoadSpec.PercentOneRm -> ctx.oneRmKg?.let { roundToIncrement(it * l.percent / 100.0, ctx.incrementKg) }
        is LoadSpec.AtRpe -> ctx.oneRmKg?.let { ctx.fTable.prescribe(it, set.reps, l.rpe, ctx.incrementKg) }
        is LoadSpec.BodyweightRelative -> ctx.bodyweightKg?.let { roundToIncrement(it * l.factor, ctx.incrementKg) }
    }
}

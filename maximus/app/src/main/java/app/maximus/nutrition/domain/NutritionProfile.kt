package app.maximus.nutrition.domain

/** Everything the nutrition module needs; persisted as app_meta rows under [PREFIX]. */
data class NutritionProfile(
    val sex: BioSex = BioSex.MALE,
    val ageYears: Int = 30,
    val heightCm: Double = 180.0,
    val weightKg: Double = 100.0,
    val bodyFatPercent: Double? = null,
    val formula: BmrFormula = BmrFormula.MIFFLIN_ST_JEOR,
    val activity: ActivityLevel = ActivityLevel.HIGH,
    val goalWeightKg: Double? = null,
    val goalEpochDay: Long? = null,
    val proteinPerKg: Double = 2.0,
    val fatPerKg: Double = 1.0,
    val kcalOverride: Double? = null,
    val useAdaptiveTdee: Boolean = false,
    val plan: PlanSettings = PlanSettings(),
    /** Coach mode: targets come from the coach plan and the selected phase instead of the BMR formulas. */
    val coachMode: Boolean = true,
    val phase: CoachPhase = CoachPhase.MAINTENANCE,
    val phaseStartDay: Long? = null,
    /** Sum of accepted weekly check-in corrections, kcal/d. */
    val coachAdjustKcal: Double = 0.0,
    val lastCheckInDay: Long? = null,
    val targetBodyFatPercent: Double = 12.0
) {
    val body: Body get() = Body(sex, ageYears, heightCm, weightKg, bodyFatPercent)

    fun toMap(): Map<String, String> = mapOf(
        "sex" to sex.name, "age" to ageYears.toString(), "height" to heightCm.toString(), "weight" to weightKg.toString(),
        "bf" to (bodyFatPercent?.toString() ?: ""), "formula" to formula.name, "activity" to activity.name,
        "goalWeight" to (goalWeightKg?.toString() ?: ""), "goalDay" to (goalEpochDay?.toString() ?: ""),
        "protein" to proteinPerKg.toString(), "fat" to fatPerKg.toString(), "kcal" to (kcalOverride?.toString() ?: ""),
        "adaptive" to useAdaptiveTdee.toString(),
        "meals" to plan.mealsPerDay.toString(), "distinct" to plan.distinctRecipes.toString(),
        "veg" to plan.vegetarianOnly.toString(), "excluded" to plan.excluded.sorted().joinToString(","), "seed" to plan.seed.toString(),
        "trainingDays" to plan.trainingDays.sorted().joinToString(","), "trainingCarbs" to plan.trainingCarbBoostG.toString(),
        "coach" to coachMode.toString(), "phase" to phase.name, "phaseStart" to (phaseStartDay?.toString() ?: ""),
        "coachAdjust" to coachAdjustKcal.toString(), "checkIn" to (lastCheckInDay?.toString() ?: ""), "targetBf" to targetBodyFatPercent.toString()
    ).mapKeys { PREFIX + it.key }

    companion object {
        const val PREFIX = "nutrition."
        const val MAX_GOAL_WEEKS = 520

        fun fromMap(raw: Map<String, String>): NutritionProfile {
            val m = raw.filterKeys { it.startsWith(PREFIX) }.mapKeys { it.key.removePrefix(PREFIX) }
            val d = NutritionProfile()
            fun dbl(k: String) = m[k]?.toDoubleOrNull()?.takeIf { it.isFinite() }
            fun <E : Enum<E>> enumOf(values: Array<E>, k: String, def: E) = values.firstOrNull { it.name == m[k] } ?: def
            val meals = m["meals"]?.toIntOrNull()?.coerceIn(MealPlanner.MIN_MEALS, MealPlanner.MAX_MEALS) ?: d.plan.mealsPerDay
            val distinct = m["distinct"]?.toIntOrNull()?.coerceIn(MealPlanner.MIN_DISTINCT, MealPlanner.MAX_DISTINCT) ?: d.plan.distinctRecipes
            return NutritionProfile(
                sex = enumOf(BioSex.entries.toTypedArray(), "sex", d.sex),
                ageYears = m["age"]?.toIntOrNull()?.coerceIn(14, 100) ?: d.ageYears,
                heightCm = dbl("height")?.coerceIn(120.0, 230.0) ?: d.heightCm,
                weightKg = dbl("weight")?.coerceIn(35.0, 250.0) ?: d.weightKg,
                bodyFatPercent = dbl("bf")?.coerceIn(3.0, 60.0),
                formula = enumOf(BmrFormula.entries.toTypedArray(), "formula", d.formula),
                activity = enumOf(ActivityLevel.entries.toTypedArray(), "activity", d.activity),
                goalWeightKg = dbl("goalWeight")?.coerceIn(35.0, 250.0),
                goalEpochDay = m["goalDay"]?.toLongOrNull(),
                proteinPerKg = dbl("protein")?.coerceIn(0.8, 3.5) ?: d.proteinPerKg,
                fatPerKg = dbl("fat")?.coerceIn(0.3, 2.5) ?: d.fatPerKg,
                kcalOverride = dbl("kcal")?.coerceIn(1000.0, 9000.0),
                useAdaptiveTdee = m["adaptive"]?.toBooleanStrictOrNull() ?: d.useAdaptiveTdee,
                plan = PlanSettings(
                    mealsPerDay = meals, distinctRecipes = distinct,
                    vegetarianOnly = m["veg"]?.toBooleanStrictOrNull() ?: false,
                    excluded = m["excluded"].orEmpty().split(',').filter { it.isNotBlank() }.toSet(),
                    seed = m["seed"]?.toLongOrNull() ?: 1L,
                    trainingDays = m["trainingDays"]?.split(',')?.mapNotNull { it.toIntOrNull()?.takeIf { day -> day in 1..7 } }
                        ?.toSet() ?: d.plan.trainingDays,
                    trainingCarbBoostG = m["trainingCarbs"]?.toIntOrNull()?.coerceIn(0, MealPlanner.MAX_TRAINING_CARB_BOOST) ?: 0
                ),
                coachMode = m["coach"]?.toBooleanStrictOrNull() ?: d.coachMode,
                phase = enumOf(CoachPhase.entries.toTypedArray(), "phase", d.phase),
                phaseStartDay = m["phaseStart"]?.toLongOrNull(),
                coachAdjustKcal = dbl("coachAdjust")?.coerceIn(-2000.0, 2000.0) ?: 0.0,
                lastCheckInDay = m["checkIn"]?.toLongOrNull(),
                targetBodyFatPercent = dbl("targetBf")?.coerceIn(5.0, 35.0) ?: d.targetBodyFatPercent
            )
        }
    }
}

enum class TdeeSource { FORMULA, ADAPTIVE }

data class GoalProjection(
    val days: Int,
    val intakeKcal: Double,
    val linearIntakeKcal: Double,
    val weeklyRatePercent: Double,
    val assessment: RateAssessment,
    val trajectory: List<Double>,
    val linearTrajectory: List<Double>,
    val finalFatFraction: Double
)

data class DerivedTargets(
    val bmr: Double,
    val tdee: Double,
    val tdeeSource: TdeeSource,
    val projection: GoalProjection?,
    val macros: MacroTargets,
    val warnings: List<MacroWarning>,
    val belowBmr: Boolean,
    /** Present in coach mode: the phase targets that [macros] were taken from. */
    val coach: CoachTargets? = null
)

object NutritionTargets {
    /**
     * BMR by the chosen formula (falls back to Mifflin if the formula needs a body-fat value that is
     * missing); TDEE = BMR · PAL or the adaptive estimate; energy target from the Hall model when a goal
     * weight with a future date is set, otherwise maintenance; an explicit override wins.
     */
    fun derive(p: NutritionProfile, today: Long, adaptive: TdeeEstimate?): DerivedTargets {
        val body = p.body
        val formula = if (Energy.bmr(body, p.formula) == null) BmrFormula.MIFFLIN_ST_JEOR else p.formula
        val bmr = Energy.bmr(body, formula)!!
        val measuredTdee = adaptive?.tdee?.takeIf { p.useAdaptiveTdee && it.isFinite() && it > 0.0 }
        val tdee = measuredTdee ?: bmr * p.activity.pal
        val bmrOf: (Double) -> Double = { m -> Energy.bmr(body.copy(weightKg = m, bodyFatPercent = null), BmrFormula.MIFFLIN_ST_JEOR)!! }

        val projection = run {
            val goal = p.goalWeightKg ?: return@run null
            val day = p.goalEpochDay ?: return@run null
            val daysRemaining = day - today
            if (daysRemaining !in 7L..7L * NutritionProfile.MAX_GOAL_WEEKS) return@run null
            val days = daysRemaining.toInt()
            val input = WeightModel.Input(body, tdee, bmrOf)
            val ei = WeightModel.intakeFor(input, goal, days) ?: return@run null
            val traj = WeightModel.simulate(input, ei, days)
            val lin = WeightModel.linearIntake(tdee, body.weightKg, goal, days)
            val rate = WeightModel.weeklyRatePercent(body.weightKg, goal, days)
            GoalProjection(
                days, ei, lin, rate, assessRate(rate),
                traj.map { it.weightKg }, WeightModel.linearTrajectory(tdee, body.weightKg, lin, days),
                traj.last().fatKg / traj.last().weightKg
            )
        }
        if (p.coachMode) {
            val c = CoachEngine.targets(p.phase, body.weightKg, p.bodyFatPercent, p.coachAdjustKcal, adaptive?.takeIf { measuredTdee != null })
            return DerivedTargets(bmr, tdee, if (measuredTdee != null) TdeeSource.ADAPTIVE else TdeeSource.FORMULA, projection, c.macros,
                MacroCalculator.warnings(c.macros, body.weightKg), c.macros.kcal < bmr, c)
        }
        val kcal = p.kcalOverride ?: projection?.intakeKcal ?: tdee
        val macros = MacroCalculator.targets(kcal, body.weightKg, p.proteinPerKg, p.fatPerKg)
        return DerivedTargets(bmr, tdee, if (measuredTdee != null) TdeeSource.ADAPTIVE else TdeeSource.FORMULA, projection, macros,
            MacroCalculator.warnings(macros, body.weightKg), kcal < bmr)
    }
}

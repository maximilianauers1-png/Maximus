package app.maximus.strongman.data

import app.maximus.strongman.domain.EventMode
import app.maximus.strongman.domain.EventSpec
import app.maximus.strongman.domain.LoadSpec
import app.maximus.strongman.domain.Program
import app.maximus.strongman.domain.ProgramDay
import app.maximus.strongman.domain.ProgramExercise
import app.maximus.strongman.domain.ProgramWeek
import app.maximus.strongman.domain.ProgressionRule
import app.maximus.strongman.domain.SetPrescription

object ProgramMapper {

    fun loadToRow(load: LoadSpec): Pair<String, Double> = when (load) {
        is LoadSpec.Absolute -> "ABS" to load.kg
        is LoadSpec.PercentOneRm -> "PCT" to load.percent
        is LoadSpec.AtRpe -> "RPE" to load.rpe
        is LoadSpec.BodyweightRelative -> "BW" to load.factor
    }

    fun rowToLoad(type: String, value: Double): LoadSpec = when (type) {
        "PCT" -> LoadSpec.PercentOneRm(value)
        "RPE" -> LoadSpec.AtRpe(value)
        "BW" -> LoadSpec.BodyweightRelative(value)
        else -> LoadSpec.Absolute(value)
    }

    data class RuleRow(val rule: String, val a: Double, val b: Double, val c: Int)

    fun ruleToRow(rule: ProgressionRule): RuleRow = when (rule) {
        ProgressionRule.None -> RuleRow("NONE", 0.0, 0.0, 0)
        is ProgressionRule.Linear -> RuleRow("LINEAR", rule.kgPerWeek, rule.percentPerWeek, 0)
        is ProgressionRule.DoubleProgression -> RuleRow("DOUBLE", rule.incrementKg, rule.minReps.toDouble(), rule.maxReps)
        is ProgressionRule.RpeAutoregulation -> RuleRow("RPE", rule.rpeStepPerWeek, 0.0, 0)
        is ProgressionRule.Wave -> RuleRow("WAVE", rule.stepPercent, rule.wavePercent, rule.length)
    }

    fun rowToRule(r: RuleRow): ProgressionRule = when (r.rule) {
        "LINEAR" -> ProgressionRule.Linear(r.a, r.b)
        "DOUBLE" -> ProgressionRule.DoubleProgression(r.b.toInt(), r.c, r.a)
        "RPE" -> ProgressionRule.RpeAutoregulation(r.a)
        "WAVE" -> ProgressionRule.Wave(r.a, r.b, r.c)
        else -> ProgressionRule.None
    }

    fun toDomain(
        program: ProgramEntity,
        days: List<ProgramDayEntity>,
        exercises: List<ProgramExerciseEntity>,
        sets: List<SetPrescriptionEntity>
    ): Program {
        val setsByExercise = sets.groupBy { it.programExerciseId }
        val exercisesByDay = exercises.groupBy { it.dayId }
        val daysByWeek = days.groupBy { it.week }
        val weeks = (0 until program.weekCount).map { w ->
            ProgramWeek(daysByWeek[w].orEmpty().sortedBy { it.dayIndex }.map { d ->
                ProgramDay(d.name, exercisesByDay[d.id].orEmpty().sortedBy { it.position }.map { pe ->
                    ProgramExercise(
                        exerciseId = pe.exerciseId,
                        rule = rowToRule(RuleRow(pe.rule, pe.ruleA, pe.ruleB, pe.ruleC)),
                        sets = setsByExercise[pe.id].orEmpty().sortedBy { it.position }.map { sp ->
                            SetPrescription(
                                reps = sp.reps,
                                load = rowToLoad(sp.loadType, sp.loadValue),
                                restSeconds = sp.restSeconds,
                                note = sp.note,
                                event = sp.eventMode?.let { mode ->
                                    EventSpec(
                                        distanceM = sp.distanceM,
                                        heightCm = sp.heightCm,
                                        implementKg = sp.implementKg,
                                        timeCapS = sp.timeCapS,
                                        mode = runCatching { EventMode.valueOf(mode) }.getOrDefault(EventMode.MAX_REPS)
                                    )
                                }
                            )
                        }
                    )
                })
            })
        }
        return Program(program.id, program.name, program.isTemplate, program.deloadEvery, weeks)
    }

    fun setToRow(programExerciseId: Long, position: Int, s: SetPrescription): SetPrescriptionEntity {
        val (type, value) = loadToRow(s.load)
        return SetPrescriptionEntity(
            programExerciseId = programExerciseId,
            position = position,
            reps = s.reps,
            loadType = type,
            loadValue = value,
            restSeconds = s.restSeconds,
            note = s.note,
            distanceM = s.event?.distanceM,
            heightCm = s.event?.heightCm,
            implementKg = s.event?.implementKg,
            timeCapS = s.event?.timeCapS,
            eventMode = s.event?.mode?.name
        )
    }
}

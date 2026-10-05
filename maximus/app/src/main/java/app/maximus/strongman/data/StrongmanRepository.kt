package app.maximus.strongman.data

import androidx.room.withTransaction
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.strongman.domain.AnalyticsSet
import app.maximus.strongman.domain.EventMode
import app.maximus.strongman.domain.EventSet
import app.maximus.strongman.domain.ConjugateTemplates
import app.maximus.strongman.domain.FKey
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.HistoricCodec
import app.maximus.strongman.domain.HistoricRecord
import app.maximus.strongman.domain.OneRepMax
import app.maximus.strongman.domain.OneRmResult
import app.maximus.strongman.domain.PrCandidate
import app.maximus.strongman.domain.PrDetector
import app.maximus.strongman.domain.Program
import app.maximus.strongman.domain.ProgramDay
import app.maximus.strongman.domain.ProgramWeek
import app.maximus.strongman.domain.Rpe
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class LoggedSetInput(
    val sessionId: Long,
    val exerciseId: Long,
    val epochDay: Long,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val distanceM: Double?,
    val heightCm: Double?,
    val timeS: Double?
)

/**
 * All database access runs on Dispatchers.IO; the database itself is resolved lazily there,
 * because its first resolution performs the Keystore unwrap of the SQLCipher key.
 */
@Singleton
class StrongmanRepository @Inject constructor(
    private val database: Lazy<MaximusDatabase>
) {
    private val io = Dispatchers.IO
    private val writeScope = CoroutineScope(SupervisorJob() + io)
    private val programMutex = Mutex()
    private val seedMutex = Mutex()
    @Volatile private var seeded = false
    private val saveQueue = Channel<Program>(Channel.UNLIMITED)

    init {
        writeScope.launch {
            for (p in saveQueue) programMutex.withLock { writeProgram(p) }
        }
    }

    private fun dao() = database.get().strongmanDao()
    private fun meta() = database.get().appMetaDao()

    private fun <T> dbFlow(block: () -> Flow<T>): Flow<T> = flow { emitAll(block()) }.flowOn(io)

    suspend fun ensureSeeded() = withContext(io) {
        seedMutex.withLock {
            if (!seeded) {
                dao().insertExercisesIgnore(SeedExercises.all)
                seedTemplatesOnce()
                seeded = true
            }
        }
    }

    /**
     * Inserts the three conjugate templates exactly once per installation (flag in app_meta), so templates
     * the user deleted or edited are never overwritten.
     */
    private suspend fun seedTemplatesOnce() {
        if (meta().get(KEY_TEMPLATES_SEEDED) != null) return
        val ids = dao().allExercises().mapNotNull { e -> e.seedKey?.let { it to e.id } }.toMap()
        val missing = ConjugateTemplates.referencedKeys() - ids.keys
        if (missing.isNotEmpty()) return
        for (t in ConjugateTemplates.all { key -> ids.getValue(key) }) {
            val id = dao().insertProgram(
                ProgramEntity(name = t.name, isTemplate = true, deloadEvery = ConjugateTemplates.DELOAD_EVERY, weekCount = t.weeks.size, createdAt = System.currentTimeMillis())
            )
            programMutex.withLock { writeProgram(Program(id, t.name, true, ConjugateTemplates.DELOAD_EVERY, t.weeks)) }
        }
        meta().upsert(AppMetaEntity(KEY_TEMPLATES_SEEDED, "1"))
    }

    // ---- Exercises ----
    val exercises: Flow<List<ExerciseEntity>> = dbFlow { dao().observeExercises() }

    suspend fun saveExercise(e: ExerciseEntity): Long = withContext(io) {
        if (e.id == 0L) dao().insertExercise(e) else { dao().updateExercise(e); e.id }
    }

    /** Returns false (and deletes nothing) if the exercise is referenced by a program or the log. */
    suspend fun deleteExerciseIfUnused(id: Long): Boolean = withContext(io) {
        if (dao().exerciseUsage(id) > 0) false else { dao().deleteExercise(id); true }
    }

    // ---- Settings ----
    val settings: Flow<StrengthSettings> = dbFlow { meta().observePrefix(StrengthSettings.KEY_PREFIX) }
        .map { rows -> StrengthSettings.fromMap(rows.associate { it.key to it.value }) }

    suspend fun saveSettings(s: StrengthSettings) = withContext(io) {
        val m = meta()
        m.upsert(AppMetaEntity(StrengthSettings.KEY_SEX, s.sex.name))
        m.upsert(AppMetaEntity(StrengthSettings.KEY_BODYWEIGHT, s.bodyweightKg?.toString() ?: ""))
        m.upsert(AppMetaEntity(StrengthSettings.KEY_INCREMENT, s.incrementKg.toString()))
        m.upsert(AppMetaEntity(StrengthSettings.KEY_BAR, s.barKg.toString()))
        m.upsert(AppMetaEntity(StrengthSettings.KEY_PLATES, StrengthSettings.encodePlates(s.plates)))
    }

    // ---- F table ----
    val fTable: Flow<FTable> = dbFlow { dao().observeOverrides() }
        .map { rows -> FTable(rows.associate { FKey(it.reps, it.rpeTenths) to it.fraction }) }

    suspend fun setOverride(reps: Int, rpe: Double, fraction: Double) = withContext(io) {
        require(fraction > 0.0 && fraction <= 1.0)
        dao().upsertOverride(FTableOverrideEntity(reps, Rpe.tenths(rpe), fraction))
    }

    suspend fun clearOverride(reps: Int, rpe: Double) = withContext(io) {
        dao().deleteOverride(reps, Rpe.tenths(rpe))
    }

    // ---- Programs ----
    val programs: Flow<List<ProgramEntity>> = dbFlow { dao().observePrograms() }

    suspend fun createProgram(name: String, weeks: Int, deloadEvery: Int, dayName: String): Long = withContext(io) {
        val id = dao().insertProgram(
            ProgramEntity(name = name, isTemplate = false, deloadEvery = deloadEvery, weekCount = weeks, createdAt = System.currentTimeMillis())
        )
        val emptyWeek = ProgramWeek(listOf(ProgramDay(dayName, emptyList())))
        writeProgram(Program(id, name, false, deloadEvery, List(weeks) { emptyWeek }))
        id
    }

    suspend fun loadProgram(id: Long): Program? = withContext(io) {
        val p = dao().program(id) ?: return@withContext null
        ProgramMapper.toDomain(p, dao().days(id), dao().programExercises(id), dao().setPrescriptions(id))
    }

    /**
     * Fire-and-forget save. A single consumer drains an ordered channel, so writes happen in
     * exactly the order of the edits (independent launches could be reordered by the dispatcher).
     */
    fun saveProgramAsync(program: Program) {
        saveQueue.trySend(program)
    }

    private suspend fun writeProgram(program: Program) {
        val db = database.get()
        db.withTransaction {
            val d = db.strongmanDao()
            val existing = d.program(program.id) ?: return@withTransaction
            d.updateProgram(
                existing.copy(name = program.name, isTemplate = program.isTemplate, deloadEvery = program.deloadEvery, weekCount = program.weeks.size)
            )
            d.deleteProgramDays(program.id)
            program.weeks.forEachIndexed { w, week ->
                week.days.forEachIndexed { di, day ->
                    val dayId = d.insertDay(ProgramDayEntity(programId = program.id, week = w, dayIndex = di, name = day.name))
                    day.exercises.forEachIndexed { ei, ex ->
                        val r = ProgramMapper.ruleToRow(ex.rule)
                        val peId = d.insertProgramExercise(
                            ProgramExerciseEntity(dayId = dayId, exerciseId = ex.exerciseId, position = ei, rule = r.rule, ruleA = r.a, ruleB = r.b, ruleC = r.c)
                        )
                        d.insertSetPrescriptions(ex.sets.mapIndexed { si, s -> ProgramMapper.setToRow(peId, si, s) })
                    }
                }
            }
        }
    }

    /** Deep copy; used for "save as template" (asTemplate = true) and "new program from template". */
    suspend fun duplicateProgram(sourceId: Long, newName: String, asTemplate: Boolean): Long? = withContext(io) {
        val src = loadProgram(sourceId) ?: return@withContext null
        val id = dao().insertProgram(
            ProgramEntity(name = newName, isTemplate = asTemplate, deloadEvery = src.deloadEvery, weekCount = src.weeks.size, createdAt = System.currentTimeMillis())
        )
        programMutex.withLock { writeProgram(src.copy(id = id, name = newName, isTemplate = asTemplate)) }
        id
    }

    suspend fun deleteProgram(id: Long) = withContext(io) { programMutex.withLock { dao().deleteProgram(id) } }

    suspend fun latestE1rms(exerciseIds: Collection<Long>): Map<Long, Double> = withContext(io) {
        exerciseIds.mapNotNull { id -> dao().latestE1rm(id)?.let { id to it } }.toMap()
    }

    // ---- Training log ----
    val sessions: Flow<List<WorkoutSessionEntity>> = dbFlow { dao().observeSessions() }

    fun sessionSets(sessionId: Long): Flow<List<SetLogEntity>> = dbFlow { dao().observeSessionSets(sessionId) }

    val allSets: Flow<List<AnalyticsSet>> = dbFlow { dao().observeAllSets() }.map { rows ->
        rows.map { AnalyticsSet(it.id, it.epochDay, it.exerciseId, it.weightKg, it.reps, it.rpe, it.e1rm, it.isE1rmPr, it.isRepPr) }
    }

    suspend fun createSession(epochDay: Long): Long = withContext(io) {
        dao().insertSession(WorkoutSessionEntity(epochDay = epochDay, note = ""))
    }

    suspend fun deleteSession(id: Long) = withContext(io) {
        val affected = dao().exercisesInSession(id)
        dao().deleteSession(id)
        affected.forEach { recomputePrs(it) }
    }

    /** e1RM: RPE table if an RPE was given, otherwise the brief's default estimator; null if refused. */
    fun e1rmFor(weightKg: Double, reps: Int, rpe: Double?, table: FTable): Double? {
        val r = if (rpe != null) table.e1rm(weightKg, reps, rpe) else OneRepMax.estimate(weightKg, reps)
        return (r as? OneRmResult.Estimate)?.kg
    }

    suspend fun logSet(input: LoggedSetInput, table: FTable): Long = withContext(io) {
        val id = dao().insertSetLog(
            SetLogEntity(
                sessionId = input.sessionId,
                exerciseId = input.exerciseId,
                epochDay = input.epochDay,
                position = dao().nextSetPosition(input.sessionId),
                weightKg = input.weightKg,
                reps = input.reps,
                rpe = input.rpe,
                distanceM = input.distanceM,
                heightCm = input.heightCm,
                timeS = input.timeS,
                e1rm = e1rmFor(input.weightKg, input.reps, input.rpe, table),
                isE1rmPr = false,
                isRepPr = false
            )
        )
        recomputePrs(input.exerciseId)
        id
    }

    /** Event sets for the record book: every logged set that carries a distance, height or time. */
    val eventSets: Flow<List<EventSet>> = dbFlow { dao().observeAllSets() }.map { rows ->
        rows.mapNotNull { r ->
            val (mode, value) = when {
                r.timeS != null && r.distanceM != null -> EventMode.FOR_TIME to r.timeS
                r.timeS != null -> EventMode.MAX_HOLD_TIME to r.timeS
                r.distanceM != null -> EventMode.MAX_DISTANCE to r.distanceM
                r.heightCm != null -> EventMode.MAX_HEIGHT to r.heightCm
                else -> return@mapNotNull null
            }
            EventSet(r.id, r.epochDay, r.exerciseId, mode, value, r.weightKg)
        }
    }

    /** Updates a logged set in place (weight, reps, RPE, event values) and refreshes the PR flags. */
    suspend fun updateSetLog(id: Long, input: LoggedSetInput, table: FTable) = withContext(io) {
        val old = dao().setLog(id) ?: return@withContext
        dao().updateSetLog(
            old.copy(
                exerciseId = input.exerciseId, weightKg = input.weightKg, reps = input.reps, rpe = input.rpe,
                distanceM = input.distanceM, heightCm = input.heightCm, timeS = input.timeS,
                e1rm = e1rmFor(input.weightKg, input.reps, input.rpe, table)
            )
        )
        if (old.exerciseId != input.exerciseId) recomputePrs(old.exerciseId)
        recomputePrs(input.exerciseId)
    }

    /** Copies every set of [sessionId] into [targetSessionId] (repeat the last session). */
    suspend fun duplicateSession(sessionId: Long, targetEpochDay: Long, table: FTable): Long = withContext(io) {
        val sets = dao().setsInSession(sessionId)
        val newId = dao().insertSession(WorkoutSessionEntity(epochDay = targetEpochDay, note = ""))
        for (s in sets.sortedBy { it.position }) {
            dao().insertSetLog(
                s.copy(id = 0, sessionId = newId, epochDay = targetEpochDay, isE1rmPr = false, isRepPr = false)
            )
        }
        sets.map { it.exerciseId }.distinct().forEach { recomputePrs(it) }
        newId
    }

    suspend fun setSessionNote(sessionId: Long, note: String) = withContext(io) { dao().updateSessionNote(sessionId, note) }

    suspend fun deleteSetLog(id: Long, exerciseId: Long) = withContext(io) {
        dao().deleteSetLog(id)
        recomputePrs(exerciseId)
    }

    /** Full chronological recomputation keeps PR flags correct for back-dated entries and deletions. */
    private suspend fun recomputePrs(exerciseId: Long) {
        val sets = dao().setsForExercise(exerciseId)
        val flags = PrDetector.evaluate(sets.map { PrCandidate(it.id, it.epochDay, it.weightKg, it.reps, it.e1rm) })
        val db = database.get()
        db.withTransaction {
            for (s in sets) {
                val f = flags[s.id] ?: continue
                if (f.e1rmPr != s.isE1rmPr || f.repPr != s.isRepPr) db.strongmanDao().updatePrFlags(s.id, f.e1rmPr, f.repPr)
            }
        }
    }

    /** Creates a finished program (e.g. generated by a training system) in one go. */
    suspend fun createProgramFromWeeks(name: String, weeks: List<ProgramWeek>): Long = withContext(io) {
        val id = dao().insertProgram(
            ProgramEntity(name = name, isTemplate = false, deloadEvery = 0, weekCount = weeks.size, createdAt = System.currentTimeMillis())
        )
        programMutex.withLock { writeProgram(Program(id, name, false, 0, weeks)) }
        id
    }

    // ---- Historic record book (past years), stored encrypted in app_meta ----
    val historic: Flow<List<HistoricRecord>> = dbFlow { meta().observePrefix(HistoricCodec.KEY_PREFIX) }
        .map { rows -> rows.mapNotNull { HistoricCodec.decode(it.key, it.value) }.sortedByDescending { it.epochDay } }

    suspend fun saveHistoric(r: HistoricRecord): Long = withContext(io) {
        val id = if (r.id > 0) r.id else System.currentTimeMillis()
        meta().upsert(AppMetaEntity(HistoricCodec.key(id), HistoricCodec.encode(r.copy(id = id))))
        id
    }

    suspend fun deleteHistoric(id: Long) = withContext(io) { meta().delete(HistoricCodec.key(id)) }

    // ---- Arena / coach preferences (population, score mode, contest date, …) ----
    val prefs: Flow<Map<String, String>> = dbFlow { meta().observePrefix(PREF_PREFIX) }
        .map { rows -> rows.associate { it.key.removePrefix(PREF_PREFIX) to it.value } }

    suspend fun setPref(key: String, value: String?) = withContext(io) {
        if (value == null) meta().delete(PREF_PREFIX + key) else meta().upsert(AppMetaEntity(PREF_PREFIX + key, value))
    }

    companion object {
        const val PREF_PREFIX = "strongman.pref."
        const val KEY_TEMPLATES_SEEDED = "strongman.templates.conjugate.v1"
    }
}

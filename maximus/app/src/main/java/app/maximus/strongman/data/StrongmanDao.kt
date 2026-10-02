package app.maximus.strongman.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StrongmanDao {

    @Query("SELECT * FROM exercise ORDER BY isEvent DESC, nameDe COLLATE NOCASE")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercise")
    suspend fun allExercises(): List<ExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercisesIgnore(exercises: List<ExerciseEntity>)

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercise WHERE id = :id")
    suspend fun deleteExercise(id: Long)

    @Query("SELECT (SELECT COUNT(*) FROM program_exercise WHERE exerciseId = :id) + (SELECT COUNT(*) FROM set_log WHERE exerciseId = :id)")
    suspend fun exerciseUsage(id: Long): Int

    @Query("SELECT * FROM program ORDER BY isTemplate, createdAt DESC")
    fun observePrograms(): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM program WHERE id = :id")
    suspend fun program(id: Long): ProgramEntity?

    @Insert
    suspend fun insertProgram(program: ProgramEntity): Long

    @Update
    suspend fun updateProgram(program: ProgramEntity)

    @Query("DELETE FROM program WHERE id = :id")
    suspend fun deleteProgram(id: Long)

    @Query("DELETE FROM program_day WHERE programId = :programId")
    suspend fun deleteProgramDays(programId: Long)

    @Insert
    suspend fun insertDay(day: ProgramDayEntity): Long

    @Insert
    suspend fun insertProgramExercise(exercise: ProgramExerciseEntity): Long

    @Insert
    suspend fun insertSetPrescriptions(sets: List<SetPrescriptionEntity>)

    @Query("SELECT * FROM program_day WHERE programId = :programId ORDER BY week, dayIndex")
    suspend fun days(programId: Long): List<ProgramDayEntity>

    @Query("SELECT pe.* FROM program_exercise pe JOIN program_day d ON pe.dayId = d.id WHERE d.programId = :programId ORDER BY pe.position")
    suspend fun programExercises(programId: Long): List<ProgramExerciseEntity>

    @Query(
        "SELECT sp.* FROM set_prescription sp JOIN program_exercise pe ON sp.programExerciseId = pe.id " +
            "JOIN program_day d ON pe.dayId = d.id WHERE d.programId = :programId ORDER BY sp.position"
    )
    suspend fun setPrescriptions(programId: Long): List<SetPrescriptionEntity>

    @Query("SELECT * FROM workout_session ORDER BY epochDay DESC, id DESC")
    fun observeSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert
    suspend fun insertSession(session: WorkoutSessionEntity): Long

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun deleteSession(id: Long)

    @Query("SELECT DISTINCT exerciseId FROM set_log WHERE sessionId = :sessionId")
    suspend fun exercisesInSession(sessionId: Long): List<Long>

    @Insert
    suspend fun insertSetLog(set: SetLogEntity): Long

    @Query("DELETE FROM set_log WHERE id = :id")
    suspend fun deleteSetLog(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM set_log WHERE sessionId = :sessionId")
    suspend fun nextSetPosition(sessionId: Long): Int

    @Query("SELECT * FROM set_log WHERE sessionId = :sessionId ORDER BY position")
    fun observeSessionSets(sessionId: Long): Flow<List<SetLogEntity>>

    @Query("SELECT * FROM set_log WHERE exerciseId = :exerciseId")
    suspend fun setsForExercise(exerciseId: Long): List<SetLogEntity>

    @Query("SELECT * FROM set_log WHERE id = :id")
    suspend fun setLog(id: Long): SetLogEntity?

    @Update
    suspend fun updateSetLog(set: SetLogEntity)

    @Query("SELECT * FROM set_log WHERE sessionId = :sessionId ORDER BY position")
    suspend fun setsInSession(sessionId: Long): List<SetLogEntity>

    @Query("UPDATE workout_session SET note = :note WHERE id = :id")
    suspend fun updateSessionNote(id: Long, note: String)

    @Query("UPDATE set_log SET isE1rmPr = :e1rmPr, isRepPr = :repPr WHERE id = :id")
    suspend fun updatePrFlags(id: Long, e1rmPr: Boolean, repPr: Boolean)

    @Query("SELECT * FROM set_log ORDER BY epochDay, id")
    fun observeAllSets(): Flow<List<SetLogEntity>>

    @Query("SELECT e1rm FROM set_log WHERE exerciseId = :exerciseId AND e1rm IS NOT NULL ORDER BY epochDay DESC, id DESC LIMIT 1")
    suspend fun latestE1rm(exerciseId: Long): Double?

    @Query("SELECT * FROM f_table_override")
    fun observeOverrides(): Flow<List<FTableOverrideEntity>>

    @Upsert
    suspend fun upsertOverride(override: FTableOverrideEntity)

    @Query("DELETE FROM f_table_override WHERE reps = :reps AND rpeTenths = :rpeTenths")
    suspend fun deleteOverride(reps: Int, rpeTenths: Int)
}

package app.maximus.strongman.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "exercise", indices = [Index(value = ["seedKey"], unique = true)])
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seedKey: String?,
    val nameDe: String,
    val nameEn: String,
    val category: String,
    val isEvent: Boolean,
    val defaultMode: String,
    val notes: String
)

@Entity(tableName = "program")
data class ProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isTemplate: Boolean,
    val deloadEvery: Int,
    val weekCount: Int,
    val createdAt: Long
)

@Entity(
    tableName = "program_day",
    foreignKeys = [ForeignKey(entity = ProgramEntity::class, parentColumns = ["id"], childColumns = ["programId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["programId"])]
)
data class ProgramDayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val week: Int,
    val dayIndex: Int,
    val name: String
)

@Entity(
    tableName = "program_exercise",
    foreignKeys = [ForeignKey(entity = ProgramDayEntity::class, parentColumns = ["id"], childColumns = ["dayId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["dayId"])]
)
data class ProgramExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val exerciseId: Long,
    val position: Int,
    val rule: String,
    val ruleA: Double,
    val ruleB: Double,
    val ruleC: Int
)

@Entity(
    tableName = "set_prescription",
    foreignKeys = [ForeignKey(entity = ProgramExerciseEntity::class, parentColumns = ["id"], childColumns = ["programExerciseId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["programExerciseId"])]
)
data class SetPrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programExerciseId: Long,
    val position: Int,
    val reps: Int,
    val loadType: String,
    val loadValue: Double,
    val restSeconds: Int,
    val note: String,
    val distanceM: Double?,
    val heightCm: Double?,
    val implementKg: Double?,
    val timeCapS: Int?,
    val eventMode: String?
)

@Entity(tableName = "workout_session")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val note: String
)

@Entity(
    tableName = "set_log",
    foreignKeys = [ForeignKey(entity = WorkoutSessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["sessionId"]), Index(value = ["exerciseId"])]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val epochDay: Long,
    val position: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val distanceM: Double?,
    val heightCm: Double?,
    val timeS: Double?,
    val e1rm: Double?,
    val isE1rmPr: Boolean,
    val isRepPr: Boolean
)

@Entity(tableName = "f_table_override", primaryKeys = ["reps", "rpeTenths"])
data class FTableOverrideEntity(
    val reps: Int,
    val rpeTenths: Int,
    val fraction: Double
)

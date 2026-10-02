package app.maximus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import app.maximus.calendar.data.CalendarDao
import app.maximus.dnd.data.DndCharacterEntity
import app.maximus.dnd.data.DndDao
import app.maximus.dnd.data.DndMonsterEntity
import app.maximus.dnd.data.DndRollEntity
import app.maximus.calendar.data.CalendarEventEntity
import app.maximus.calendar.data.CalendarExdateEntity
import app.maximus.notes.data.NoteDao
import app.maximus.nutrition.data.NutritionDao
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.notes.data.NoteEntity
import app.maximus.strongman.data.ExerciseEntity
import app.maximus.strongman.data.FTableOverrideEntity
import app.maximus.strongman.data.ProgramDayEntity
import app.maximus.strongman.data.ProgramEntity
import app.maximus.strongman.data.ProgramExerciseEntity
import app.maximus.strongman.data.SetLogEntity
import app.maximus.strongman.data.SetPrescriptionEntity
import app.maximus.strongman.data.StrongmanDao
import app.maximus.strongman.data.WorkoutSessionEntity
import app.maximus.vault.data.VaultDao
import app.maximus.vault.data.VaultEntryEntity

@Database(
    entities = [
        AppMetaEntity::class,
        ExerciseEntity::class,
        ProgramEntity::class,
        ProgramDayEntity::class,
        ProgramExerciseEntity::class,
        SetPrescriptionEntity::class,
        WorkoutSessionEntity::class,
        SetLogEntity::class,
        FTableOverrideEntity::class,
        VaultEntryEntity::class,
        CalendarEventEntity::class,
        CalendarExdateEntity::class,
        NoteEntity::class,
        NutritionLogEntity::class,
        DndCharacterEntity::class,
        DndMonsterEntity::class,
        DndRollEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class MaximusDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun strongmanDao(): StrongmanDao
    abstract fun vaultDao(): VaultDao
    abstract fun calendarDao(): CalendarDao
    abstract fun noteDao(): NoteDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun dndDao(): DndDao

    companion object {
        const val NAME = "maximus.db"
        const val VERSION = 5
    }
}

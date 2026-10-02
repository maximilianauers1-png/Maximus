package app.maximus.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import app.maximus.calendar.`data`.CalendarDao
import app.maximus.calendar.`data`.CalendarDao_Impl
import app.maximus.dnd.`data`.DndDao
import app.maximus.dnd.`data`.DndDao_Impl
import app.maximus.notes.`data`.NoteDao
import app.maximus.notes.`data`.NoteDao_Impl
import app.maximus.nutrition.`data`.NutritionDao
import app.maximus.nutrition.`data`.NutritionDao_Impl
import app.maximus.strongman.`data`.StrongmanDao
import app.maximus.strongman.`data`.StrongmanDao_Impl
import app.maximus.vault.`data`.VaultDao
import app.maximus.vault.`data`.VaultDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MaximusDatabase_Impl : MaximusDatabase() {
  private val _appMetaDao: Lazy<AppMetaDao> = lazy {
    AppMetaDao_Impl(this)
  }

  private val _strongmanDao: Lazy<StrongmanDao> = lazy {
    StrongmanDao_Impl(this)
  }

  private val _vaultDao: Lazy<VaultDao> = lazy {
    VaultDao_Impl(this)
  }

  private val _calendarDao: Lazy<CalendarDao> = lazy {
    CalendarDao_Impl(this)
  }

  private val _noteDao: Lazy<NoteDao> = lazy {
    NoteDao_Impl(this)
  }

  private val _nutritionDao: Lazy<NutritionDao> = lazy {
    NutritionDao_Impl(this)
  }

  private val _dndDao: Lazy<DndDao> = lazy {
    DndDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(5, "4ebb019c9395c3b4a82a36ca4be26416", "fbad481506666583d327796b5e70f3b1") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `app_meta` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `seedKey` TEXT, `nameDe` TEXT NOT NULL, `nameEn` TEXT NOT NULL, `category` TEXT NOT NULL, `isEvent` INTEGER NOT NULL, `defaultMode` TEXT NOT NULL, `notes` TEXT NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_seedKey` ON `exercise` (`seedKey`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `program` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isTemplate` INTEGER NOT NULL, `deloadEvery` INTEGER NOT NULL, `weekCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `program_day` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `programId` INTEGER NOT NULL, `week` INTEGER NOT NULL, `dayIndex` INTEGER NOT NULL, `name` TEXT NOT NULL, FOREIGN KEY(`programId`) REFERENCES `program`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_program_day_programId` ON `program_day` (`programId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `program_exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dayId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `rule` TEXT NOT NULL, `ruleA` REAL NOT NULL, `ruleB` REAL NOT NULL, `ruleC` INTEGER NOT NULL, FOREIGN KEY(`dayId`) REFERENCES `program_day`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_program_exercise_dayId` ON `program_exercise` (`dayId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `set_prescription` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `programExerciseId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `reps` INTEGER NOT NULL, `loadType` TEXT NOT NULL, `loadValue` REAL NOT NULL, `restSeconds` INTEGER NOT NULL, `note` TEXT NOT NULL, `distanceM` REAL, `heightCm` REAL, `implementKg` REAL, `timeCapS` INTEGER, `eventMode` TEXT, FOREIGN KEY(`programExerciseId`) REFERENCES `program_exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_prescription_programExerciseId` ON `set_prescription` (`programExerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `workout_session` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epochDay` INTEGER NOT NULL, `note` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `set_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, `position` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `reps` INTEGER NOT NULL, `rpe` REAL, `distanceM` REAL, `heightCm` REAL, `timeS` REAL, `e1rm` REAL, `isE1rmPr` INTEGER NOT NULL, `isRepPr` INTEGER NOT NULL, FOREIGN KEY(`sessionId`) REFERENCES `workout_session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_log_sessionId` ON `set_log` (`sessionId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_set_log_exerciseId` ON `set_log` (`exerciseId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `f_table_override` (`reps` INTEGER NOT NULL, `rpeTenths` INTEGER NOT NULL, `fraction` REAL NOT NULL, PRIMARY KEY(`reps`, `rpeTenths`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `vault_entry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT NOT NULL, `title` TEXT NOT NULL, `username` TEXT NOT NULL, `url` TEXT NOT NULL, `favorite` INTEGER NOT NULL, `payload` BLOB NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `passwordChangedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vault_entry_uid` ON `vault_entry` (`uid`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `calendar_event` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `location` TEXT NOT NULL, `notes` TEXT NOT NULL, `allDay` INTEGER NOT NULL, `startDay` INTEGER NOT NULL, `startMinute` INTEGER NOT NULL, `endDay` INTEGER NOT NULL, `endMinute` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `interval` INTEGER NOT NULL, `weekdayMask` INTEGER NOT NULL, `untilDay` INTEGER, `count` INTEGER, `color` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `calendar_exdate` (`eventId` INTEGER NOT NULL, `day` INTEGER NOT NULL, PRIMARY KEY(`eventId`, `day`), FOREIGN KEY(`eventId`) REFERENCES `calendar_event`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_exdate_eventId` ON `calendar_exdate` (`eventId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `tags` TEXT NOT NULL, `pinned` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `nutrition_log` (`epochDay` INTEGER NOT NULL, `weightKg` REAL, `kcal` REAL, `proteinG` REAL, `note` TEXT NOT NULL, PRIMARY KEY(`epochDay`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dnd_character` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `summary` TEXT NOT NULL, `payload` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dnd_monster` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `challengeRating` REAL NOT NULL, `payload` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dnd_roll` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `expression` TEXT NOT NULL, `label` TEXT NOT NULL, `total` INTEGER NOT NULL, `detail` TEXT NOT NULL, `epochMillis` INTEGER NOT NULL, `favorite` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_dnd_roll_epochMillis` ON `dnd_roll` (`epochMillis`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4ebb019c9395c3b4a82a36ca4be26416')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `app_meta`")
        connection.execSQL("DROP TABLE IF EXISTS `exercise`")
        connection.execSQL("DROP TABLE IF EXISTS `program`")
        connection.execSQL("DROP TABLE IF EXISTS `program_day`")
        connection.execSQL("DROP TABLE IF EXISTS `program_exercise`")
        connection.execSQL("DROP TABLE IF EXISTS `set_prescription`")
        connection.execSQL("DROP TABLE IF EXISTS `workout_session`")
        connection.execSQL("DROP TABLE IF EXISTS `set_log`")
        connection.execSQL("DROP TABLE IF EXISTS `f_table_override`")
        connection.execSQL("DROP TABLE IF EXISTS `vault_entry`")
        connection.execSQL("DROP TABLE IF EXISTS `calendar_event`")
        connection.execSQL("DROP TABLE IF EXISTS `calendar_exdate`")
        connection.execSQL("DROP TABLE IF EXISTS `note`")
        connection.execSQL("DROP TABLE IF EXISTS `nutrition_log`")
        connection.execSQL("DROP TABLE IF EXISTS `dnd_character`")
        connection.execSQL("DROP TABLE IF EXISTS `dnd_monster`")
        connection.execSQL("DROP TABLE IF EXISTS `dnd_roll`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsAppMeta: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAppMeta.put("key", TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAppMeta.put("value", TableInfo.Column("value", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAppMeta: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAppMeta: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAppMeta: TableInfo = TableInfo("app_meta", _columnsAppMeta, _foreignKeysAppMeta, _indicesAppMeta)
        val _existingAppMeta: TableInfo = read(connection, "app_meta")
        if (!_infoAppMeta.equals(_existingAppMeta)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |app_meta(app.maximus.data.db.AppMetaEntity).
              | Expected:
              |""".trimMargin() + _infoAppMeta + """
              |
              | Found:
              |""".trimMargin() + _existingAppMeta)
        }
        val _columnsExercise: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExercise.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("seedKey", TableInfo.Column("seedKey", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("nameDe", TableInfo.Column("nameDe", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("nameEn", TableInfo.Column("nameEn", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("isEvent", TableInfo.Column("isEvent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("defaultMode", TableInfo.Column("defaultMode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercise.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExercise: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExercise: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesExercise.add(TableInfo.Index("index_exercise_seedKey", true, listOf("seedKey"), listOf("ASC")))
        val _infoExercise: TableInfo = TableInfo("exercise", _columnsExercise, _foreignKeysExercise, _indicesExercise)
        val _existingExercise: TableInfo = read(connection, "exercise")
        if (!_infoExercise.equals(_existingExercise)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercise(app.maximus.strongman.data.ExerciseEntity).
              | Expected:
              |""".trimMargin() + _infoExercise + """
              |
              | Found:
              |""".trimMargin() + _existingExercise)
        }
        val _columnsProgram: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgram.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgram.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgram.put("isTemplate", TableInfo.Column("isTemplate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgram.put("deloadEvery", TableInfo.Column("deloadEvery", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgram.put("weekCount", TableInfo.Column("weekCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgram.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgram: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProgram: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoProgram: TableInfo = TableInfo("program", _columnsProgram, _foreignKeysProgram, _indicesProgram)
        val _existingProgram: TableInfo = read(connection, "program")
        if (!_infoProgram.equals(_existingProgram)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |program(app.maximus.strongman.data.ProgramEntity).
              | Expected:
              |""".trimMargin() + _infoProgram + """
              |
              | Found:
              |""".trimMargin() + _existingProgram)
        }
        val _columnsProgramDay: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgramDay.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramDay.put("programId", TableInfo.Column("programId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramDay.put("week", TableInfo.Column("week", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramDay.put("dayIndex", TableInfo.Column("dayIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramDay.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgramDay: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysProgramDay.add(TableInfo.ForeignKey("program", "CASCADE", "NO ACTION", listOf("programId"), listOf("id")))
        val _indicesProgramDay: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProgramDay.add(TableInfo.Index("index_program_day_programId", false, listOf("programId"), listOf("ASC")))
        val _infoProgramDay: TableInfo = TableInfo("program_day", _columnsProgramDay, _foreignKeysProgramDay, _indicesProgramDay)
        val _existingProgramDay: TableInfo = read(connection, "program_day")
        if (!_infoProgramDay.equals(_existingProgramDay)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |program_day(app.maximus.strongman.data.ProgramDayEntity).
              | Expected:
              |""".trimMargin() + _infoProgramDay + """
              |
              | Found:
              |""".trimMargin() + _existingProgramDay)
        }
        val _columnsProgramExercise: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProgramExercise.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("dayId", TableInfo.Column("dayId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("exerciseId", TableInfo.Column("exerciseId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("rule", TableInfo.Column("rule", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("ruleA", TableInfo.Column("ruleA", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("ruleB", TableInfo.Column("ruleB", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProgramExercise.put("ruleC", TableInfo.Column("ruleC", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProgramExercise: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysProgramExercise.add(TableInfo.ForeignKey("program_day", "CASCADE", "NO ACTION", listOf("dayId"), listOf("id")))
        val _indicesProgramExercise: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesProgramExercise.add(TableInfo.Index("index_program_exercise_dayId", false, listOf("dayId"), listOf("ASC")))
        val _infoProgramExercise: TableInfo = TableInfo("program_exercise", _columnsProgramExercise, _foreignKeysProgramExercise, _indicesProgramExercise)
        val _existingProgramExercise: TableInfo = read(connection, "program_exercise")
        if (!_infoProgramExercise.equals(_existingProgramExercise)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |program_exercise(app.maximus.strongman.data.ProgramExerciseEntity).
              | Expected:
              |""".trimMargin() + _infoProgramExercise + """
              |
              | Found:
              |""".trimMargin() + _existingProgramExercise)
        }
        val _columnsSetPrescription: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSetPrescription.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("programExerciseId", TableInfo.Column("programExerciseId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("reps", TableInfo.Column("reps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("loadType", TableInfo.Column("loadType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("loadValue", TableInfo.Column("loadValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("restSeconds", TableInfo.Column("restSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("distanceM", TableInfo.Column("distanceM", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("heightCm", TableInfo.Column("heightCm", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("implementKg", TableInfo.Column("implementKg", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("timeCapS", TableInfo.Column("timeCapS", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetPrescription.put("eventMode", TableInfo.Column("eventMode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSetPrescription: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSetPrescription.add(TableInfo.ForeignKey("program_exercise", "CASCADE", "NO ACTION", listOf("programExerciseId"), listOf("id")))
        val _indicesSetPrescription: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSetPrescription.add(TableInfo.Index("index_set_prescription_programExerciseId", false, listOf("programExerciseId"), listOf("ASC")))
        val _infoSetPrescription: TableInfo = TableInfo("set_prescription", _columnsSetPrescription, _foreignKeysSetPrescription, _indicesSetPrescription)
        val _existingSetPrescription: TableInfo = read(connection, "set_prescription")
        if (!_infoSetPrescription.equals(_existingSetPrescription)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |set_prescription(app.maximus.strongman.data.SetPrescriptionEntity).
              | Expected:
              |""".trimMargin() + _infoSetPrescription + """
              |
              | Found:
              |""".trimMargin() + _existingSetPrescription)
        }
        val _columnsWorkoutSession: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWorkoutSession.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("epochDay", TableInfo.Column("epochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWorkoutSession.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWorkoutSession: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWorkoutSession: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoWorkoutSession: TableInfo = TableInfo("workout_session", _columnsWorkoutSession, _foreignKeysWorkoutSession, _indicesWorkoutSession)
        val _existingWorkoutSession: TableInfo = read(connection, "workout_session")
        if (!_infoWorkoutSession.equals(_existingWorkoutSession)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |workout_session(app.maximus.strongman.data.WorkoutSessionEntity).
              | Expected:
              |""".trimMargin() + _infoWorkoutSession + """
              |
              | Found:
              |""".trimMargin() + _existingWorkoutSession)
        }
        val _columnsSetLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSetLog.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("sessionId", TableInfo.Column("sessionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("exerciseId", TableInfo.Column("exerciseId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("epochDay", TableInfo.Column("epochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("position", TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("weightKg", TableInfo.Column("weightKg", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("reps", TableInfo.Column("reps", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("rpe", TableInfo.Column("rpe", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("distanceM", TableInfo.Column("distanceM", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("heightCm", TableInfo.Column("heightCm", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("timeS", TableInfo.Column("timeS", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("e1rm", TableInfo.Column("e1rm", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("isE1rmPr", TableInfo.Column("isE1rmPr", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSetLog.put("isRepPr", TableInfo.Column("isRepPr", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSetLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSetLog.add(TableInfo.ForeignKey("workout_session", "CASCADE", "NO ACTION", listOf("sessionId"), listOf("id")))
        val _indicesSetLog: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSetLog.add(TableInfo.Index("index_set_log_sessionId", false, listOf("sessionId"), listOf("ASC")))
        _indicesSetLog.add(TableInfo.Index("index_set_log_exerciseId", false, listOf("exerciseId"), listOf("ASC")))
        val _infoSetLog: TableInfo = TableInfo("set_log", _columnsSetLog, _foreignKeysSetLog, _indicesSetLog)
        val _existingSetLog: TableInfo = read(connection, "set_log")
        if (!_infoSetLog.equals(_existingSetLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |set_log(app.maximus.strongman.data.SetLogEntity).
              | Expected:
              |""".trimMargin() + _infoSetLog + """
              |
              | Found:
              |""".trimMargin() + _existingSetLog)
        }
        val _columnsFTableOverride: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFTableOverride.put("reps", TableInfo.Column("reps", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFTableOverride.put("rpeTenths", TableInfo.Column("rpeTenths", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFTableOverride.put("fraction", TableInfo.Column("fraction", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFTableOverride: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFTableOverride: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFTableOverride: TableInfo = TableInfo("f_table_override", _columnsFTableOverride, _foreignKeysFTableOverride, _indicesFTableOverride)
        val _existingFTableOverride: TableInfo = read(connection, "f_table_override")
        if (!_infoFTableOverride.equals(_existingFTableOverride)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |f_table_override(app.maximus.strongman.data.FTableOverrideEntity).
              | Expected:
              |""".trimMargin() + _infoFTableOverride + """
              |
              | Found:
              |""".trimMargin() + _existingFTableOverride)
        }
        val _columnsVaultEntry: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsVaultEntry.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("uid", TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("username", TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("url", TableInfo.Column("url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("favorite", TableInfo.Column("favorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("payload", TableInfo.Column("payload", "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsVaultEntry.put("passwordChangedAt", TableInfo.Column("passwordChangedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysVaultEntry: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesVaultEntry: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesVaultEntry.add(TableInfo.Index("index_vault_entry_uid", true, listOf("uid"), listOf("ASC")))
        val _infoVaultEntry: TableInfo = TableInfo("vault_entry", _columnsVaultEntry, _foreignKeysVaultEntry, _indicesVaultEntry)
        val _existingVaultEntry: TableInfo = read(connection, "vault_entry")
        if (!_infoVaultEntry.equals(_existingVaultEntry)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |vault_entry(app.maximus.vault.data.VaultEntryEntity).
              | Expected:
              |""".trimMargin() + _infoVaultEntry + """
              |
              | Found:
              |""".trimMargin() + _existingVaultEntry)
        }
        val _columnsCalendarEvent: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCalendarEvent.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("location", TableInfo.Column("location", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("allDay", TableInfo.Column("allDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("startDay", TableInfo.Column("startDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("startMinute", TableInfo.Column("startMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("endDay", TableInfo.Column("endDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("endMinute", TableInfo.Column("endMinute", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("frequency", TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("interval", TableInfo.Column("interval", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("weekdayMask", TableInfo.Column("weekdayMask", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("untilDay", TableInfo.Column("untilDay", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("count", TableInfo.Column("count", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("color", TableInfo.Column("color", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarEvent.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCalendarEvent: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCalendarEvent: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCalendarEvent: TableInfo = TableInfo("calendar_event", _columnsCalendarEvent, _foreignKeysCalendarEvent, _indicesCalendarEvent)
        val _existingCalendarEvent: TableInfo = read(connection, "calendar_event")
        if (!_infoCalendarEvent.equals(_existingCalendarEvent)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |calendar_event(app.maximus.calendar.data.CalendarEventEntity).
              | Expected:
              |""".trimMargin() + _infoCalendarEvent + """
              |
              | Found:
              |""".trimMargin() + _existingCalendarEvent)
        }
        val _columnsCalendarExdate: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCalendarExdate.put("eventId", TableInfo.Column("eventId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCalendarExdate.put("day", TableInfo.Column("day", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCalendarExdate: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysCalendarExdate.add(TableInfo.ForeignKey("calendar_event", "CASCADE", "NO ACTION", listOf("eventId"), listOf("id")))
        val _indicesCalendarExdate: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCalendarExdate.add(TableInfo.Index("index_calendar_exdate_eventId", false, listOf("eventId"), listOf("ASC")))
        val _infoCalendarExdate: TableInfo = TableInfo("calendar_exdate", _columnsCalendarExdate, _foreignKeysCalendarExdate, _indicesCalendarExdate)
        val _existingCalendarExdate: TableInfo = read(connection, "calendar_exdate")
        if (!_infoCalendarExdate.equals(_existingCalendarExdate)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |calendar_exdate(app.maximus.calendar.data.CalendarExdateEntity).
              | Expected:
              |""".trimMargin() + _infoCalendarExdate + """
              |
              | Found:
              |""".trimMargin() + _existingCalendarExdate)
        }
        val _columnsNote: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNote.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("body", TableInfo.Column("body", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("pinned", TableInfo.Column("pinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNote.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNote: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNote: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNote: TableInfo = TableInfo("note", _columnsNote, _foreignKeysNote, _indicesNote)
        val _existingNote: TableInfo = read(connection, "note")
        if (!_infoNote.equals(_existingNote)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |note(app.maximus.notes.data.NoteEntity).
              | Expected:
              |""".trimMargin() + _infoNote + """
              |
              | Found:
              |""".trimMargin() + _existingNote)
        }
        val _columnsNutritionLog: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNutritionLog.put("epochDay", TableInfo.Column("epochDay", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNutritionLog.put("weightKg", TableInfo.Column("weightKg", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNutritionLog.put("kcal", TableInfo.Column("kcal", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNutritionLog.put("proteinG", TableInfo.Column("proteinG", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNutritionLog.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNutritionLog: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNutritionLog: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNutritionLog: TableInfo = TableInfo("nutrition_log", _columnsNutritionLog, _foreignKeysNutritionLog, _indicesNutritionLog)
        val _existingNutritionLog: TableInfo = read(connection, "nutrition_log")
        if (!_infoNutritionLog.equals(_existingNutritionLog)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |nutrition_log(app.maximus.nutrition.data.NutritionLogEntity).
              | Expected:
              |""".trimMargin() + _infoNutritionLog + """
              |
              | Found:
              |""".trimMargin() + _existingNutritionLog)
        }
        val _columnsDndCharacter: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDndCharacter.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndCharacter.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndCharacter.put("summary", TableInfo.Column("summary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndCharacter.put("payload", TableInfo.Column("payload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndCharacter.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndCharacter.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDndCharacter: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDndCharacter: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDndCharacter: TableInfo = TableInfo("dnd_character", _columnsDndCharacter, _foreignKeysDndCharacter, _indicesDndCharacter)
        val _existingDndCharacter: TableInfo = read(connection, "dnd_character")
        if (!_infoDndCharacter.equals(_existingDndCharacter)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dnd_character(app.maximus.dnd.data.DndCharacterEntity).
              | Expected:
              |""".trimMargin() + _infoDndCharacter + """
              |
              | Found:
              |""".trimMargin() + _existingDndCharacter)
        }
        val _columnsDndMonster: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDndMonster.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndMonster.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndMonster.put("challengeRating", TableInfo.Column("challengeRating", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndMonster.put("payload", TableInfo.Column("payload", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndMonster.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndMonster.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDndMonster: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDndMonster: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDndMonster: TableInfo = TableInfo("dnd_monster", _columnsDndMonster, _foreignKeysDndMonster, _indicesDndMonster)
        val _existingDndMonster: TableInfo = read(connection, "dnd_monster")
        if (!_infoDndMonster.equals(_existingDndMonster)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dnd_monster(app.maximus.dnd.data.DndMonsterEntity).
              | Expected:
              |""".trimMargin() + _infoDndMonster + """
              |
              | Found:
              |""".trimMargin() + _existingDndMonster)
        }
        val _columnsDndRoll: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDndRoll.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("expression", TableInfo.Column("expression", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("label", TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("total", TableInfo.Column("total", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("detail", TableInfo.Column("detail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("epochMillis", TableInfo.Column("epochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDndRoll.put("favorite", TableInfo.Column("favorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDndRoll: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDndRoll: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDndRoll.add(TableInfo.Index("index_dnd_roll_epochMillis", false, listOf("epochMillis"), listOf("ASC")))
        val _infoDndRoll: TableInfo = TableInfo("dnd_roll", _columnsDndRoll, _foreignKeysDndRoll, _indicesDndRoll)
        val _existingDndRoll: TableInfo = read(connection, "dnd_roll")
        if (!_infoDndRoll.equals(_existingDndRoll)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dnd_roll(app.maximus.dnd.data.DndRollEntity).
              | Expected:
              |""".trimMargin() + _infoDndRoll + """
              |
              | Found:
              |""".trimMargin() + _existingDndRoll)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "app_meta", "exercise", "program", "program_day", "program_exercise", "set_prescription", "workout_session", "set_log", "f_table_override", "vault_entry", "calendar_event", "calendar_exdate", "note", "nutrition_log", "dnd_character", "dnd_monster", "dnd_roll")
  }

  public override fun clearAllTables() {
    super.performClear(true, "app_meta", "exercise", "program", "program_day", "program_exercise", "set_prescription", "workout_session", "set_log", "f_table_override", "vault_entry", "calendar_event", "calendar_exdate", "note", "nutrition_log", "dnd_character", "dnd_monster", "dnd_roll")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AppMetaDao::class, AppMetaDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(StrongmanDao::class, StrongmanDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(VaultDao::class, VaultDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CalendarDao::class, CalendarDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(NoteDao::class, NoteDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(NutritionDao::class, NutritionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DndDao::class, DndDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun appMetaDao(): AppMetaDao = _appMetaDao.value

  public override fun strongmanDao(): StrongmanDao = _strongmanDao.value

  public override fun vaultDao(): VaultDao = _vaultDao.value

  public override fun calendarDao(): CalendarDao = _calendarDao.value

  public override fun noteDao(): NoteDao = _noteDao.value

  public override fun nutritionDao(): NutritionDao = _nutritionDao.value

  public override fun dndDao(): DndDao = _dndDao.value
}

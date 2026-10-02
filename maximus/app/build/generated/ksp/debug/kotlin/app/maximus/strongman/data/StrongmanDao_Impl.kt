package app.maximus.strongman.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StrongmanDao_Impl(
  __db: RoomDatabase,
) : StrongmanDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExerciseEntity: EntityInsertAdapter<ExerciseEntity>

  private val __insertAdapterOfExerciseEntity_1: EntityInsertAdapter<ExerciseEntity>

  private val __insertAdapterOfProgramEntity: EntityInsertAdapter<ProgramEntity>

  private val __insertAdapterOfProgramDayEntity: EntityInsertAdapter<ProgramDayEntity>

  private val __insertAdapterOfProgramExerciseEntity: EntityInsertAdapter<ProgramExerciseEntity>

  private val __insertAdapterOfSetPrescriptionEntity: EntityInsertAdapter<SetPrescriptionEntity>

  private val __insertAdapterOfWorkoutSessionEntity: EntityInsertAdapter<WorkoutSessionEntity>

  private val __insertAdapterOfSetLogEntity: EntityInsertAdapter<SetLogEntity>

  private val __updateAdapterOfExerciseEntity: EntityDeleteOrUpdateAdapter<ExerciseEntity>

  private val __updateAdapterOfProgramEntity: EntityDeleteOrUpdateAdapter<ProgramEntity>

  private val __updateAdapterOfSetLogEntity: EntityDeleteOrUpdateAdapter<SetLogEntity>

  private val __upsertAdapterOfFTableOverrideEntity: EntityUpsertAdapter<FTableOverrideEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfExerciseEntity = object : EntityInsertAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `exercise` (`id`,`seedKey`,`nameDe`,`nameEn`,`category`,`isEvent`,`defaultMode`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindLong(1, entity.id)
        val _tmpSeedKey: String? = entity.seedKey
        if (_tmpSeedKey == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpSeedKey)
        }
        statement.bindText(3, entity.nameDe)
        statement.bindText(4, entity.nameEn)
        statement.bindText(5, entity.category)
        val _tmp: Int = if (entity.isEvent) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.defaultMode)
        statement.bindText(8, entity.notes)
      }
    }
    this.__insertAdapterOfExerciseEntity_1 = object : EntityInsertAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `exercise` (`id`,`seedKey`,`nameDe`,`nameEn`,`category`,`isEvent`,`defaultMode`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindLong(1, entity.id)
        val _tmpSeedKey: String? = entity.seedKey
        if (_tmpSeedKey == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpSeedKey)
        }
        statement.bindText(3, entity.nameDe)
        statement.bindText(4, entity.nameEn)
        statement.bindText(5, entity.category)
        val _tmp: Int = if (entity.isEvent) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.defaultMode)
        statement.bindText(8, entity.notes)
      }
    }
    this.__insertAdapterOfProgramEntity = object : EntityInsertAdapter<ProgramEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `program` (`id`,`name`,`isTemplate`,`deloadEvery`,`weekCount`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgramEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmp: Int = if (entity.isTemplate) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindLong(4, entity.deloadEvery.toLong())
        statement.bindLong(5, entity.weekCount.toLong())
        statement.bindLong(6, entity.createdAt)
      }
    }
    this.__insertAdapterOfProgramDayEntity = object : EntityInsertAdapter<ProgramDayEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `program_day` (`id`,`programId`,`week`,`dayIndex`,`name`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgramDayEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.programId)
        statement.bindLong(3, entity.week.toLong())
        statement.bindLong(4, entity.dayIndex.toLong())
        statement.bindText(5, entity.name)
      }
    }
    this.__insertAdapterOfProgramExerciseEntity = object : EntityInsertAdapter<ProgramExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `program_exercise` (`id`,`dayId`,`exerciseId`,`position`,`rule`,`ruleA`,`ruleB`,`ruleC`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProgramExerciseEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.dayId)
        statement.bindLong(3, entity.exerciseId)
        statement.bindLong(4, entity.position.toLong())
        statement.bindText(5, entity.rule)
        statement.bindDouble(6, entity.ruleA)
        statement.bindDouble(7, entity.ruleB)
        statement.bindLong(8, entity.ruleC.toLong())
      }
    }
    this.__insertAdapterOfSetPrescriptionEntity = object : EntityInsertAdapter<SetPrescriptionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `set_prescription` (`id`,`programExerciseId`,`position`,`reps`,`loadType`,`loadValue`,`restSeconds`,`note`,`distanceM`,`heightCm`,`implementKg`,`timeCapS`,`eventMode`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SetPrescriptionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.programExerciseId)
        statement.bindLong(3, entity.position.toLong())
        statement.bindLong(4, entity.reps.toLong())
        statement.bindText(5, entity.loadType)
        statement.bindDouble(6, entity.loadValue)
        statement.bindLong(7, entity.restSeconds.toLong())
        statement.bindText(8, entity.note)
        val _tmpDistanceM: Double? = entity.distanceM
        if (_tmpDistanceM == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpDistanceM)
        }
        val _tmpHeightCm: Double? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpHeightCm)
        }
        val _tmpImplementKg: Double? = entity.implementKg
        if (_tmpImplementKg == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpImplementKg)
        }
        val _tmpTimeCapS: Int? = entity.timeCapS
        if (_tmpTimeCapS == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTimeCapS.toLong())
        }
        val _tmpEventMode: String? = entity.eventMode
        if (_tmpEventMode == null) {
          statement.bindNull(13)
        } else {
          statement.bindText(13, _tmpEventMode)
        }
      }
    }
    this.__insertAdapterOfWorkoutSessionEntity = object : EntityInsertAdapter<WorkoutSessionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `workout_session` (`id`,`epochDay`,`note`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WorkoutSessionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.epochDay)
        statement.bindText(3, entity.note)
      }
    }
    this.__insertAdapterOfSetLogEntity = object : EntityInsertAdapter<SetLogEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `set_log` (`id`,`sessionId`,`exerciseId`,`epochDay`,`position`,`weightKg`,`reps`,`rpe`,`distanceM`,`heightCm`,`timeS`,`e1rm`,`isE1rmPr`,`isRepPr`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SetLogEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.sessionId)
        statement.bindLong(3, entity.exerciseId)
        statement.bindLong(4, entity.epochDay)
        statement.bindLong(5, entity.position.toLong())
        statement.bindDouble(6, entity.weightKg)
        statement.bindLong(7, entity.reps.toLong())
        val _tmpRpe: Double? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpRpe)
        }
        val _tmpDistanceM: Double? = entity.distanceM
        if (_tmpDistanceM == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpDistanceM)
        }
        val _tmpHeightCm: Double? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpHeightCm)
        }
        val _tmpTimeS: Double? = entity.timeS
        if (_tmpTimeS == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpTimeS)
        }
        val _tmpE1rm: Double? = entity.e1rm
        if (_tmpE1rm == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpE1rm)
        }
        val _tmp: Int = if (entity.isE1rmPr) 1 else 0
        statement.bindLong(13, _tmp.toLong())
        val _tmp_1: Int = if (entity.isRepPr) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
      }
    }
    this.__updateAdapterOfExerciseEntity = object : EntityDeleteOrUpdateAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `exercise` SET `id` = ?,`seedKey` = ?,`nameDe` = ?,`nameEn` = ?,`category` = ?,`isEvent` = ?,`defaultMode` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindLong(1, entity.id)
        val _tmpSeedKey: String? = entity.seedKey
        if (_tmpSeedKey == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpSeedKey)
        }
        statement.bindText(3, entity.nameDe)
        statement.bindText(4, entity.nameEn)
        statement.bindText(5, entity.category)
        val _tmp: Int = if (entity.isEvent) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.defaultMode)
        statement.bindText(8, entity.notes)
        statement.bindLong(9, entity.id)
      }
    }
    this.__updateAdapterOfProgramEntity = object : EntityDeleteOrUpdateAdapter<ProgramEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `program` SET `id` = ?,`name` = ?,`isTemplate` = ?,`deloadEvery` = ?,`weekCount` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProgramEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmp: Int = if (entity.isTemplate) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        statement.bindLong(4, entity.deloadEvery.toLong())
        statement.bindLong(5, entity.weekCount.toLong())
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.id)
      }
    }
    this.__updateAdapterOfSetLogEntity = object : EntityDeleteOrUpdateAdapter<SetLogEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `set_log` SET `id` = ?,`sessionId` = ?,`exerciseId` = ?,`epochDay` = ?,`position` = ?,`weightKg` = ?,`reps` = ?,`rpe` = ?,`distanceM` = ?,`heightCm` = ?,`timeS` = ?,`e1rm` = ?,`isE1rmPr` = ?,`isRepPr` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SetLogEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.sessionId)
        statement.bindLong(3, entity.exerciseId)
        statement.bindLong(4, entity.epochDay)
        statement.bindLong(5, entity.position.toLong())
        statement.bindDouble(6, entity.weightKg)
        statement.bindLong(7, entity.reps.toLong())
        val _tmpRpe: Double? = entity.rpe
        if (_tmpRpe == null) {
          statement.bindNull(8)
        } else {
          statement.bindDouble(8, _tmpRpe)
        }
        val _tmpDistanceM: Double? = entity.distanceM
        if (_tmpDistanceM == null) {
          statement.bindNull(9)
        } else {
          statement.bindDouble(9, _tmpDistanceM)
        }
        val _tmpHeightCm: Double? = entity.heightCm
        if (_tmpHeightCm == null) {
          statement.bindNull(10)
        } else {
          statement.bindDouble(10, _tmpHeightCm)
        }
        val _tmpTimeS: Double? = entity.timeS
        if (_tmpTimeS == null) {
          statement.bindNull(11)
        } else {
          statement.bindDouble(11, _tmpTimeS)
        }
        val _tmpE1rm: Double? = entity.e1rm
        if (_tmpE1rm == null) {
          statement.bindNull(12)
        } else {
          statement.bindDouble(12, _tmpE1rm)
        }
        val _tmp: Int = if (entity.isE1rmPr) 1 else 0
        statement.bindLong(13, _tmp.toLong())
        val _tmp_1: Int = if (entity.isRepPr) 1 else 0
        statement.bindLong(14, _tmp_1.toLong())
        statement.bindLong(15, entity.id)
      }
    }
    this.__upsertAdapterOfFTableOverrideEntity = EntityUpsertAdapter<FTableOverrideEntity>(object : EntityInsertAdapter<FTableOverrideEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `f_table_override` (`reps`,`rpeTenths`,`fraction`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: FTableOverrideEntity) {
        statement.bindLong(1, entity.reps.toLong())
        statement.bindLong(2, entity.rpeTenths.toLong())
        statement.bindDouble(3, entity.fraction)
      }
    }, object : EntityDeleteOrUpdateAdapter<FTableOverrideEntity>() {
      protected override fun createQuery(): String = "UPDATE `f_table_override` SET `reps` = ?,`rpeTenths` = ?,`fraction` = ? WHERE `reps` = ? AND `rpeTenths` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: FTableOverrideEntity) {
        statement.bindLong(1, entity.reps.toLong())
        statement.bindLong(2, entity.rpeTenths.toLong())
        statement.bindDouble(3, entity.fraction)
        statement.bindLong(4, entity.reps.toLong())
        statement.bindLong(5, entity.rpeTenths.toLong())
      }
    })
  }

  public override suspend fun insertExercisesIgnore(exercises: List<ExerciseEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseEntity.insert(_connection, exercises)
  }

  public override suspend fun insertExercise(exercise: ExerciseEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfExerciseEntity_1.insertAndReturnId(_connection, exercise)
    _result
  }

  public override suspend fun insertProgram(program: ProgramEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgramEntity.insertAndReturnId(_connection, program)
    _result
  }

  public override suspend fun insertDay(day: ProgramDayEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgramDayEntity.insertAndReturnId(_connection, day)
    _result
  }

  public override suspend fun insertProgramExercise(exercise: ProgramExerciseEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProgramExerciseEntity.insertAndReturnId(_connection, exercise)
    _result
  }

  public override suspend fun insertSetPrescriptions(sets: List<SetPrescriptionEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSetPrescriptionEntity.insert(_connection, sets)
  }

  public override suspend fun insertSession(session: WorkoutSessionEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfWorkoutSessionEntity.insertAndReturnId(_connection, session)
    _result
  }

  public override suspend fun insertSetLog(`set`: SetLogEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSetLogEntity.insertAndReturnId(_connection, set)
    _result
  }

  public override suspend fun updateExercise(exercise: ExerciseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfExerciseEntity.handle(_connection, exercise)
  }

  public override suspend fun updateProgram(program: ProgramEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfProgramEntity.handle(_connection, program)
  }

  public override suspend fun updateSetLog(`set`: SetLogEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSetLogEntity.handle(_connection, set)
  }

  public override suspend fun upsertOverride(`override`: FTableOverrideEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfFTableOverrideEntity.upsert(_connection, override)
  }

  public override fun observeExercises(): Flow<List<ExerciseEntity>> {
    val _sql: String = "SELECT * FROM exercise ORDER BY isEvent DESC, nameDe COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("exercise")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSeedKey: Int = getColumnIndexOrThrow(_stmt, "seedKey")
        val _columnIndexOfNameDe: Int = getColumnIndexOrThrow(_stmt, "nameDe")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "nameEn")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsEvent: Int = getColumnIndexOrThrow(_stmt, "isEvent")
        val _columnIndexOfDefaultMode: Int = getColumnIndexOrThrow(_stmt, "defaultMode")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSeedKey: String?
          if (_stmt.isNull(_columnIndexOfSeedKey)) {
            _tmpSeedKey = null
          } else {
            _tmpSeedKey = _stmt.getText(_columnIndexOfSeedKey)
          }
          val _tmpNameDe: String
          _tmpNameDe = _stmt.getText(_columnIndexOfNameDe)
          val _tmpNameEn: String
          _tmpNameEn = _stmt.getText(_columnIndexOfNameEn)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsEvent: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEvent).toInt()
          _tmpIsEvent = _tmp != 0
          val _tmpDefaultMode: String
          _tmpDefaultMode = _stmt.getText(_columnIndexOfDefaultMode)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = ExerciseEntity(_tmpId,_tmpSeedKey,_tmpNameDe,_tmpNameEn,_tmpCategory,_tmpIsEvent,_tmpDefaultMode,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun allExercises(): List<ExerciseEntity> {
    val _sql: String = "SELECT * FROM exercise"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSeedKey: Int = getColumnIndexOrThrow(_stmt, "seedKey")
        val _columnIndexOfNameDe: Int = getColumnIndexOrThrow(_stmt, "nameDe")
        val _columnIndexOfNameEn: Int = getColumnIndexOrThrow(_stmt, "nameEn")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsEvent: Int = getColumnIndexOrThrow(_stmt, "isEvent")
        val _columnIndexOfDefaultMode: Int = getColumnIndexOrThrow(_stmt, "defaultMode")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSeedKey: String?
          if (_stmt.isNull(_columnIndexOfSeedKey)) {
            _tmpSeedKey = null
          } else {
            _tmpSeedKey = _stmt.getText(_columnIndexOfSeedKey)
          }
          val _tmpNameDe: String
          _tmpNameDe = _stmt.getText(_columnIndexOfNameDe)
          val _tmpNameEn: String
          _tmpNameEn = _stmt.getText(_columnIndexOfNameEn)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpIsEvent: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsEvent).toInt()
          _tmpIsEvent = _tmp != 0
          val _tmpDefaultMode: String
          _tmpDefaultMode = _stmt.getText(_columnIndexOfDefaultMode)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = ExerciseEntity(_tmpId,_tmpSeedKey,_tmpNameDe,_tmpNameEn,_tmpCategory,_tmpIsEvent,_tmpDefaultMode,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exerciseUsage(id: Long): Int {
    val _sql: String = "SELECT (SELECT COUNT(*) FROM program_exercise WHERE exerciseId = ?) + (SELECT COUNT(*) FROM set_log WHERE exerciseId = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePrograms(): Flow<List<ProgramEntity>> {
    val _sql: String = "SELECT * FROM program ORDER BY isTemplate, createdAt DESC"
    return createFlow(__db, false, arrayOf("program")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsTemplate: Int = getColumnIndexOrThrow(_stmt, "isTemplate")
        val _columnIndexOfDeloadEvery: Int = getColumnIndexOrThrow(_stmt, "deloadEvery")
        val _columnIndexOfWeekCount: Int = getColumnIndexOrThrow(_stmt, "weekCount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ProgramEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgramEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsTemplate: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsTemplate).toInt()
          _tmpIsTemplate = _tmp != 0
          val _tmpDeloadEvery: Int
          _tmpDeloadEvery = _stmt.getLong(_columnIndexOfDeloadEvery).toInt()
          val _tmpWeekCount: Int
          _tmpWeekCount = _stmt.getLong(_columnIndexOfWeekCount).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ProgramEntity(_tmpId,_tmpName,_tmpIsTemplate,_tmpDeloadEvery,_tmpWeekCount,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun program(id: Long): ProgramEntity? {
    val _sql: String = "SELECT * FROM program WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIsTemplate: Int = getColumnIndexOrThrow(_stmt, "isTemplate")
        val _columnIndexOfDeloadEvery: Int = getColumnIndexOrThrow(_stmt, "deloadEvery")
        val _columnIndexOfWeekCount: Int = getColumnIndexOrThrow(_stmt, "weekCount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: ProgramEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIsTemplate: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsTemplate).toInt()
          _tmpIsTemplate = _tmp != 0
          val _tmpDeloadEvery: Int
          _tmpDeloadEvery = _stmt.getLong(_columnIndexOfDeloadEvery).toInt()
          val _tmpWeekCount: Int
          _tmpWeekCount = _stmt.getLong(_columnIndexOfWeekCount).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = ProgramEntity(_tmpId,_tmpName,_tmpIsTemplate,_tmpDeloadEvery,_tmpWeekCount,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun days(programId: Long): List<ProgramDayEntity> {
    val _sql: String = "SELECT * FROM program_day WHERE programId = ? ORDER BY week, dayIndex"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProgramId: Int = getColumnIndexOrThrow(_stmt, "programId")
        val _columnIndexOfWeek: Int = getColumnIndexOrThrow(_stmt, "week")
        val _columnIndexOfDayIndex: Int = getColumnIndexOrThrow(_stmt, "dayIndex")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<ProgramDayEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgramDayEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProgramId: Long
          _tmpProgramId = _stmt.getLong(_columnIndexOfProgramId)
          val _tmpWeek: Int
          _tmpWeek = _stmt.getLong(_columnIndexOfWeek).toInt()
          val _tmpDayIndex: Int
          _tmpDayIndex = _stmt.getLong(_columnIndexOfDayIndex).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = ProgramDayEntity(_tmpId,_tmpProgramId,_tmpWeek,_tmpDayIndex,_tmpName)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun programExercises(programId: Long): List<ProgramExerciseEntity> {
    val _sql: String = "SELECT pe.* FROM program_exercise pe JOIN program_day d ON pe.dayId = d.id WHERE d.programId = ? ORDER BY pe.position"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDayId: Int = getColumnIndexOrThrow(_stmt, "dayId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfRule: Int = getColumnIndexOrThrow(_stmt, "rule")
        val _columnIndexOfRuleA: Int = getColumnIndexOrThrow(_stmt, "ruleA")
        val _columnIndexOfRuleB: Int = getColumnIndexOrThrow(_stmt, "ruleB")
        val _columnIndexOfRuleC: Int = getColumnIndexOrThrow(_stmt, "ruleC")
        val _result: MutableList<ProgramExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProgramExerciseEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDayId: Long
          _tmpDayId = _stmt.getLong(_columnIndexOfDayId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpRule: String
          _tmpRule = _stmt.getText(_columnIndexOfRule)
          val _tmpRuleA: Double
          _tmpRuleA = _stmt.getDouble(_columnIndexOfRuleA)
          val _tmpRuleB: Double
          _tmpRuleB = _stmt.getDouble(_columnIndexOfRuleB)
          val _tmpRuleC: Int
          _tmpRuleC = _stmt.getLong(_columnIndexOfRuleC).toInt()
          _item = ProgramExerciseEntity(_tmpId,_tmpDayId,_tmpExerciseId,_tmpPosition,_tmpRule,_tmpRuleA,_tmpRuleB,_tmpRuleC)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setPrescriptions(programId: Long): List<SetPrescriptionEntity> {
    val _sql: String = "SELECT sp.* FROM set_prescription sp JOIN program_exercise pe ON sp.programExerciseId = pe.id JOIN program_day d ON pe.dayId = d.id WHERE d.programId = ? ORDER BY sp.position"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfProgramExerciseId: Int = getColumnIndexOrThrow(_stmt, "programExerciseId")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfLoadType: Int = getColumnIndexOrThrow(_stmt, "loadType")
        val _columnIndexOfLoadValue: Int = getColumnIndexOrThrow(_stmt, "loadValue")
        val _columnIndexOfRestSeconds: Int = getColumnIndexOrThrow(_stmt, "restSeconds")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfImplementKg: Int = getColumnIndexOrThrow(_stmt, "implementKg")
        val _columnIndexOfTimeCapS: Int = getColumnIndexOrThrow(_stmt, "timeCapS")
        val _columnIndexOfEventMode: Int = getColumnIndexOrThrow(_stmt, "eventMode")
        val _result: MutableList<SetPrescriptionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetPrescriptionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpProgramExerciseId: Long
          _tmpProgramExerciseId = _stmt.getLong(_columnIndexOfProgramExerciseId)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpLoadType: String
          _tmpLoadType = _stmt.getText(_columnIndexOfLoadType)
          val _tmpLoadValue: Double
          _tmpLoadValue = _stmt.getDouble(_columnIndexOfLoadValue)
          val _tmpRestSeconds: Int
          _tmpRestSeconds = _stmt.getLong(_columnIndexOfRestSeconds).toInt()
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpImplementKg: Double?
          if (_stmt.isNull(_columnIndexOfImplementKg)) {
            _tmpImplementKg = null
          } else {
            _tmpImplementKg = _stmt.getDouble(_columnIndexOfImplementKg)
          }
          val _tmpTimeCapS: Int?
          if (_stmt.isNull(_columnIndexOfTimeCapS)) {
            _tmpTimeCapS = null
          } else {
            _tmpTimeCapS = _stmt.getLong(_columnIndexOfTimeCapS).toInt()
          }
          val _tmpEventMode: String?
          if (_stmt.isNull(_columnIndexOfEventMode)) {
            _tmpEventMode = null
          } else {
            _tmpEventMode = _stmt.getText(_columnIndexOfEventMode)
          }
          _item = SetPrescriptionEntity(_tmpId,_tmpProgramExerciseId,_tmpPosition,_tmpReps,_tmpLoadType,_tmpLoadValue,_tmpRestSeconds,_tmpNote,_tmpDistanceM,_tmpHeightCm,_tmpImplementKg,_tmpTimeCapS,_tmpEventMode)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSessions(): Flow<List<WorkoutSessionEntity>> {
    val _sql: String = "SELECT * FROM workout_session ORDER BY epochDay DESC, id DESC"
    return createFlow(__db, false, arrayOf("workout_session")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<WorkoutSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WorkoutSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item = WorkoutSessionEntity(_tmpId,_tmpEpochDay,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun exercisesInSession(sessionId: Long): List<Long> {
    val _sql: String = "SELECT DISTINCT exerciseId FROM set_log WHERE sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _result: MutableList<Long> = mutableListOf()
        while (_stmt.step()) {
          val _item: Long
          _item = _stmt.getLong(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun nextSetPosition(sessionId: Long): Int {
    val _sql: String = "SELECT COALESCE(MAX(position), -1) + 1 FROM set_log WHERE sessionId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSessionSets(sessionId: Long): Flow<List<SetLogEntity>> {
    val _sql: String = "SELECT * FROM set_log WHERE sessionId = ? ORDER BY position"
    return createFlow(__db, false, arrayOf("set_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfTimeS: Int = getColumnIndexOrThrow(_stmt, "timeS")
        val _columnIndexOfE1rm: Int = getColumnIndexOrThrow(_stmt, "e1rm")
        val _columnIndexOfIsE1rmPr: Int = getColumnIndexOrThrow(_stmt, "isE1rmPr")
        val _columnIndexOfIsRepPr: Int = getColumnIndexOrThrow(_stmt, "isRepPr")
        val _result: MutableList<SetLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetLogEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpe: Double?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getDouble(_columnIndexOfRpe)
          }
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpTimeS: Double?
          if (_stmt.isNull(_columnIndexOfTimeS)) {
            _tmpTimeS = null
          } else {
            _tmpTimeS = _stmt.getDouble(_columnIndexOfTimeS)
          }
          val _tmpE1rm: Double?
          if (_stmt.isNull(_columnIndexOfE1rm)) {
            _tmpE1rm = null
          } else {
            _tmpE1rm = _stmt.getDouble(_columnIndexOfE1rm)
          }
          val _tmpIsE1rmPr: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsE1rmPr).toInt()
          _tmpIsE1rmPr = _tmp != 0
          val _tmpIsRepPr: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsRepPr).toInt()
          _tmpIsRepPr = _tmp_1 != 0
          _item = SetLogEntity(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpEpochDay,_tmpPosition,_tmpWeightKg,_tmpReps,_tmpRpe,_tmpDistanceM,_tmpHeightCm,_tmpTimeS,_tmpE1rm,_tmpIsE1rmPr,_tmpIsRepPr)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setsForExercise(exerciseId: Long): List<SetLogEntity> {
    val _sql: String = "SELECT * FROM set_log WHERE exerciseId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfTimeS: Int = getColumnIndexOrThrow(_stmt, "timeS")
        val _columnIndexOfE1rm: Int = getColumnIndexOrThrow(_stmt, "e1rm")
        val _columnIndexOfIsE1rmPr: Int = getColumnIndexOrThrow(_stmt, "isE1rmPr")
        val _columnIndexOfIsRepPr: Int = getColumnIndexOrThrow(_stmt, "isRepPr")
        val _result: MutableList<SetLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetLogEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpe: Double?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getDouble(_columnIndexOfRpe)
          }
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpTimeS: Double?
          if (_stmt.isNull(_columnIndexOfTimeS)) {
            _tmpTimeS = null
          } else {
            _tmpTimeS = _stmt.getDouble(_columnIndexOfTimeS)
          }
          val _tmpE1rm: Double?
          if (_stmt.isNull(_columnIndexOfE1rm)) {
            _tmpE1rm = null
          } else {
            _tmpE1rm = _stmt.getDouble(_columnIndexOfE1rm)
          }
          val _tmpIsE1rmPr: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsE1rmPr).toInt()
          _tmpIsE1rmPr = _tmp != 0
          val _tmpIsRepPr: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsRepPr).toInt()
          _tmpIsRepPr = _tmp_1 != 0
          _item = SetLogEntity(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpEpochDay,_tmpPosition,_tmpWeightKg,_tmpReps,_tmpRpe,_tmpDistanceM,_tmpHeightCm,_tmpTimeS,_tmpE1rm,_tmpIsE1rmPr,_tmpIsRepPr)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setLog(id: Long): SetLogEntity? {
    val _sql: String = "SELECT * FROM set_log WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfTimeS: Int = getColumnIndexOrThrow(_stmt, "timeS")
        val _columnIndexOfE1rm: Int = getColumnIndexOrThrow(_stmt, "e1rm")
        val _columnIndexOfIsE1rmPr: Int = getColumnIndexOrThrow(_stmt, "isE1rmPr")
        val _columnIndexOfIsRepPr: Int = getColumnIndexOrThrow(_stmt, "isRepPr")
        val _result: SetLogEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpe: Double?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getDouble(_columnIndexOfRpe)
          }
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpTimeS: Double?
          if (_stmt.isNull(_columnIndexOfTimeS)) {
            _tmpTimeS = null
          } else {
            _tmpTimeS = _stmt.getDouble(_columnIndexOfTimeS)
          }
          val _tmpE1rm: Double?
          if (_stmt.isNull(_columnIndexOfE1rm)) {
            _tmpE1rm = null
          } else {
            _tmpE1rm = _stmt.getDouble(_columnIndexOfE1rm)
          }
          val _tmpIsE1rmPr: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsE1rmPr).toInt()
          _tmpIsE1rmPr = _tmp != 0
          val _tmpIsRepPr: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsRepPr).toInt()
          _tmpIsRepPr = _tmp_1 != 0
          _result = SetLogEntity(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpEpochDay,_tmpPosition,_tmpWeightKg,_tmpReps,_tmpRpe,_tmpDistanceM,_tmpHeightCm,_tmpTimeS,_tmpE1rm,_tmpIsE1rmPr,_tmpIsRepPr)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setsInSession(sessionId: Long): List<SetLogEntity> {
    val _sql: String = "SELECT * FROM set_log WHERE sessionId = ? ORDER BY position"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfTimeS: Int = getColumnIndexOrThrow(_stmt, "timeS")
        val _columnIndexOfE1rm: Int = getColumnIndexOrThrow(_stmt, "e1rm")
        val _columnIndexOfIsE1rmPr: Int = getColumnIndexOrThrow(_stmt, "isE1rmPr")
        val _columnIndexOfIsRepPr: Int = getColumnIndexOrThrow(_stmt, "isRepPr")
        val _result: MutableList<SetLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetLogEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpe: Double?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getDouble(_columnIndexOfRpe)
          }
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpTimeS: Double?
          if (_stmt.isNull(_columnIndexOfTimeS)) {
            _tmpTimeS = null
          } else {
            _tmpTimeS = _stmt.getDouble(_columnIndexOfTimeS)
          }
          val _tmpE1rm: Double?
          if (_stmt.isNull(_columnIndexOfE1rm)) {
            _tmpE1rm = null
          } else {
            _tmpE1rm = _stmt.getDouble(_columnIndexOfE1rm)
          }
          val _tmpIsE1rmPr: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsE1rmPr).toInt()
          _tmpIsE1rmPr = _tmp != 0
          val _tmpIsRepPr: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsRepPr).toInt()
          _tmpIsRepPr = _tmp_1 != 0
          _item = SetLogEntity(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpEpochDay,_tmpPosition,_tmpWeightKg,_tmpReps,_tmpRpe,_tmpDistanceM,_tmpHeightCm,_tmpTimeS,_tmpE1rm,_tmpIsE1rmPr,_tmpIsRepPr)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAllSets(): Flow<List<SetLogEntity>> {
    val _sql: String = "SELECT * FROM set_log ORDER BY epochDay, id"
    return createFlow(__db, false, arrayOf("set_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exerciseId")
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpe: Int = getColumnIndexOrThrow(_stmt, "rpe")
        val _columnIndexOfDistanceM: Int = getColumnIndexOrThrow(_stmt, "distanceM")
        val _columnIndexOfHeightCm: Int = getColumnIndexOrThrow(_stmt, "heightCm")
        val _columnIndexOfTimeS: Int = getColumnIndexOrThrow(_stmt, "timeS")
        val _columnIndexOfE1rm: Int = getColumnIndexOrThrow(_stmt, "e1rm")
        val _columnIndexOfIsE1rmPr: Int = getColumnIndexOrThrow(_stmt, "isE1rmPr")
        val _columnIndexOfIsRepPr: Int = getColumnIndexOrThrow(_stmt, "isRepPr")
        val _result: MutableList<SetLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SetLogEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpExerciseId: Long
          _tmpExerciseId = _stmt.getLong(_columnIndexOfExerciseId)
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          val _tmpWeightKg: Double
          _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpe: Double?
          if (_stmt.isNull(_columnIndexOfRpe)) {
            _tmpRpe = null
          } else {
            _tmpRpe = _stmt.getDouble(_columnIndexOfRpe)
          }
          val _tmpDistanceM: Double?
          if (_stmt.isNull(_columnIndexOfDistanceM)) {
            _tmpDistanceM = null
          } else {
            _tmpDistanceM = _stmt.getDouble(_columnIndexOfDistanceM)
          }
          val _tmpHeightCm: Double?
          if (_stmt.isNull(_columnIndexOfHeightCm)) {
            _tmpHeightCm = null
          } else {
            _tmpHeightCm = _stmt.getDouble(_columnIndexOfHeightCm)
          }
          val _tmpTimeS: Double?
          if (_stmt.isNull(_columnIndexOfTimeS)) {
            _tmpTimeS = null
          } else {
            _tmpTimeS = _stmt.getDouble(_columnIndexOfTimeS)
          }
          val _tmpE1rm: Double?
          if (_stmt.isNull(_columnIndexOfE1rm)) {
            _tmpE1rm = null
          } else {
            _tmpE1rm = _stmt.getDouble(_columnIndexOfE1rm)
          }
          val _tmpIsE1rmPr: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsE1rmPr).toInt()
          _tmpIsE1rmPr = _tmp != 0
          val _tmpIsRepPr: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsRepPr).toInt()
          _tmpIsRepPr = _tmp_1 != 0
          _item = SetLogEntity(_tmpId,_tmpSessionId,_tmpExerciseId,_tmpEpochDay,_tmpPosition,_tmpWeightKg,_tmpReps,_tmpRpe,_tmpDistanceM,_tmpHeightCm,_tmpTimeS,_tmpE1rm,_tmpIsE1rmPr,_tmpIsRepPr)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun latestE1rm(exerciseId: Long): Double? {
    val _sql: String = "SELECT e1rm FROM set_log WHERE exerciseId = ? AND e1rm IS NOT NULL ORDER BY epochDay DESC, id DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, exerciseId)
        val _result: Double?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getDouble(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeOverrides(): Flow<List<FTableOverrideEntity>> {
    val _sql: String = "SELECT * FROM f_table_override"
    return createFlow(__db, false, arrayOf("f_table_override")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfReps: Int = getColumnIndexOrThrow(_stmt, "reps")
        val _columnIndexOfRpeTenths: Int = getColumnIndexOrThrow(_stmt, "rpeTenths")
        val _columnIndexOfFraction: Int = getColumnIndexOrThrow(_stmt, "fraction")
        val _result: MutableList<FTableOverrideEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: FTableOverrideEntity
          val _tmpReps: Int
          _tmpReps = _stmt.getLong(_columnIndexOfReps).toInt()
          val _tmpRpeTenths: Int
          _tmpRpeTenths = _stmt.getLong(_columnIndexOfRpeTenths).toInt()
          val _tmpFraction: Double
          _tmpFraction = _stmt.getDouble(_columnIndexOfFraction)
          _item = FTableOverrideEntity(_tmpReps,_tmpRpeTenths,_tmpFraction)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteExercise(id: Long) {
    val _sql: String = "DELETE FROM exercise WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteProgram(id: Long) {
    val _sql: String = "DELETE FROM program WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteProgramDays(programId: Long) {
    val _sql: String = "DELETE FROM program_day WHERE programId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, programId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSession(id: Long) {
    val _sql: String = "DELETE FROM workout_session WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSetLog(id: Long) {
    val _sql: String = "DELETE FROM set_log WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateSessionNote(id: Long, note: String) {
    val _sql: String = "UPDATE workout_session SET note = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, note)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePrFlags(
    id: Long,
    e1rmPr: Boolean,
    repPr: Boolean,
  ) {
    val _sql: String = "UPDATE set_log SET isE1rmPr = ?, isRepPr = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (e1rmPr) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        val _tmp_1: Int = if (repPr) 1 else 0
        _stmt.bindLong(_argIndex, _tmp_1.toLong())
        _argIndex = 3
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOverride(reps: Int, rpeTenths: Int) {
    val _sql: String = "DELETE FROM f_table_override WHERE reps = ? AND rpeTenths = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, reps.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, rpeTenths.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}

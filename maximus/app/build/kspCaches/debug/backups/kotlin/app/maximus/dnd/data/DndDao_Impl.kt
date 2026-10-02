package app.maximus.dnd.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
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
public class DndDao_Impl(
  __db: RoomDatabase,
) : DndDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDndCharacterEntity: EntityInsertAdapter<DndCharacterEntity>

  private val __insertAdapterOfDndMonsterEntity: EntityInsertAdapter<DndMonsterEntity>

  private val __insertAdapterOfDndRollEntity: EntityInsertAdapter<DndRollEntity>

  private val __updateAdapterOfDndCharacterEntity: EntityDeleteOrUpdateAdapter<DndCharacterEntity>

  private val __updateAdapterOfDndMonsterEntity: EntityDeleteOrUpdateAdapter<DndMonsterEntity>

  private val __updateAdapterOfDndRollEntity: EntityDeleteOrUpdateAdapter<DndRollEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDndCharacterEntity = object : EntityInsertAdapter<DndCharacterEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `dnd_character` (`id`,`name`,`summary`,`payload`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DndCharacterEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.summary)
        statement.bindText(4, entity.payload)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
      }
    }
    this.__insertAdapterOfDndMonsterEntity = object : EntityInsertAdapter<DndMonsterEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `dnd_monster` (`id`,`name`,`challengeRating`,`payload`,`createdAt`,`updatedAt`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DndMonsterEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.challengeRating)
        statement.bindText(4, entity.payload)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
      }
    }
    this.__insertAdapterOfDndRollEntity = object : EntityInsertAdapter<DndRollEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `dnd_roll` (`id`,`expression`,`label`,`total`,`detail`,`epochMillis`,`favorite`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DndRollEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.expression)
        statement.bindText(3, entity.label)
        statement.bindLong(4, entity.total.toLong())
        statement.bindText(5, entity.detail)
        statement.bindLong(6, entity.epochMillis)
        val _tmp: Int = if (entity.favorite) 1 else 0
        statement.bindLong(7, _tmp.toLong())
      }
    }
    this.__updateAdapterOfDndCharacterEntity = object : EntityDeleteOrUpdateAdapter<DndCharacterEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `dnd_character` SET `id` = ?,`name` = ?,`summary` = ?,`payload` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DndCharacterEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.summary)
        statement.bindText(4, entity.payload)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
        statement.bindLong(7, entity.id)
      }
    }
    this.__updateAdapterOfDndMonsterEntity = object : EntityDeleteOrUpdateAdapter<DndMonsterEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `dnd_monster` SET `id` = ?,`name` = ?,`challengeRating` = ?,`payload` = ?,`createdAt` = ?,`updatedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DndMonsterEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.challengeRating)
        statement.bindText(4, entity.payload)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
        statement.bindLong(7, entity.id)
      }
    }
    this.__updateAdapterOfDndRollEntity = object : EntityDeleteOrUpdateAdapter<DndRollEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `dnd_roll` SET `id` = ?,`expression` = ?,`label` = ?,`total` = ?,`detail` = ?,`epochMillis` = ?,`favorite` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DndRollEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.expression)
        statement.bindText(3, entity.label)
        statement.bindLong(4, entity.total.toLong())
        statement.bindText(5, entity.detail)
        statement.bindLong(6, entity.epochMillis)
        val _tmp: Int = if (entity.favorite) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        statement.bindLong(8, entity.id)
      }
    }
  }

  public override suspend fun insertCharacter(e: DndCharacterEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDndCharacterEntity.insertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun insertMonster(e: DndMonsterEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDndMonsterEntity.insertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun insertRoll(e: DndRollEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDndRollEntity.insertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun updateCharacter(e: DndCharacterEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfDndCharacterEntity.handle(_connection, e)
  }

  public override suspend fun updateMonster(e: DndMonsterEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfDndMonsterEntity.handle(_connection, e)
  }

  public override suspend fun updateRoll(e: DndRollEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfDndRollEntity.handle(_connection, e)
  }

  public override fun observeCharacters(): Flow<List<DndCharacterEntity>> {
    val _sql: String = "SELECT * FROM dnd_character ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("dnd_character")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSummary: Int = getColumnIndexOrThrow(_stmt, "summary")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<DndCharacterEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DndCharacterEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpPayload: String
          _tmpPayload = _stmt.getText(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = DndCharacterEntity(_tmpId,_tmpName,_tmpSummary,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun character(id: Long): DndCharacterEntity? {
    val _sql: String = "SELECT * FROM dnd_character WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfSummary: Int = getColumnIndexOrThrow(_stmt, "summary")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: DndCharacterEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpPayload: String
          _tmpPayload = _stmt.getText(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = DndCharacterEntity(_tmpId,_tmpName,_tmpSummary,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeMonsters(): Flow<List<DndMonsterEntity>> {
    val _sql: String = "SELECT * FROM dnd_monster ORDER BY challengeRating, name COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("dnd_monster")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfChallengeRating: Int = getColumnIndexOrThrow(_stmt, "challengeRating")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<DndMonsterEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DndMonsterEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpChallengeRating: Double
          _tmpChallengeRating = _stmt.getDouble(_columnIndexOfChallengeRating)
          val _tmpPayload: String
          _tmpPayload = _stmt.getText(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = DndMonsterEntity(_tmpId,_tmpName,_tmpChallengeRating,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun monster(id: Long): DndMonsterEntity? {
    val _sql: String = "SELECT * FROM dnd_monster WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfChallengeRating: Int = getColumnIndexOrThrow(_stmt, "challengeRating")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: DndMonsterEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpChallengeRating: Double
          _tmpChallengeRating = _stmt.getDouble(_columnIndexOfChallengeRating)
          val _tmpPayload: String
          _tmpPayload = _stmt.getText(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = DndMonsterEntity(_tmpId,_tmpName,_tmpChallengeRating,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeRolls(limit: Int): Flow<List<DndRollEntity>> {
    val _sql: String = "SELECT * FROM dnd_roll ORDER BY epochMillis DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("dnd_roll")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExpression: Int = getColumnIndexOrThrow(_stmt, "expression")
        val _columnIndexOfLabel: Int = getColumnIndexOrThrow(_stmt, "label")
        val _columnIndexOfTotal: Int = getColumnIndexOrThrow(_stmt, "total")
        val _columnIndexOfDetail: Int = getColumnIndexOrThrow(_stmt, "detail")
        val _columnIndexOfEpochMillis: Int = getColumnIndexOrThrow(_stmt, "epochMillis")
        val _columnIndexOfFavorite: Int = getColumnIndexOrThrow(_stmt, "favorite")
        val _result: MutableList<DndRollEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DndRollEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExpression: String
          _tmpExpression = _stmt.getText(_columnIndexOfExpression)
          val _tmpLabel: String
          _tmpLabel = _stmt.getText(_columnIndexOfLabel)
          val _tmpTotal: Int
          _tmpTotal = _stmt.getLong(_columnIndexOfTotal).toInt()
          val _tmpDetail: String
          _tmpDetail = _stmt.getText(_columnIndexOfDetail)
          val _tmpEpochMillis: Long
          _tmpEpochMillis = _stmt.getLong(_columnIndexOfEpochMillis)
          val _tmpFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfFavorite).toInt()
          _tmpFavorite = _tmp != 0
          _item = DndRollEntity(_tmpId,_tmpExpression,_tmpLabel,_tmpTotal,_tmpDetail,_tmpEpochMillis,_tmpFavorite)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCharacter(id: Long) {
    val _sql: String = "DELETE FROM dnd_character WHERE id = ?"
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

  public override suspend fun deleteMonster(id: Long) {
    val _sql: String = "DELETE FROM dnd_monster WHERE id = ?"
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

  public override suspend fun clearHistory() {
    val _sql: String = "DELETE FROM dnd_roll WHERE favorite = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteRoll(id: Long) {
    val _sql: String = "DELETE FROM dnd_roll WHERE id = ?"
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

  public override suspend fun trimHistory(keep: Int) {
    val _sql: String = "DELETE FROM dnd_roll WHERE favorite = 0 AND id NOT IN (SELECT id FROM dnd_roll WHERE favorite = 0 ORDER BY epochMillis DESC LIMIT ?)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, keep.toLong())
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

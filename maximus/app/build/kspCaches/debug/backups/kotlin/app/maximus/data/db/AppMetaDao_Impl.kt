package app.maximus.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
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
public class AppMetaDao_Impl(
  __db: RoomDatabase,
) : AppMetaDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfAppMetaEntity: EntityUpsertAdapter<AppMetaEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfAppMetaEntity = EntityUpsertAdapter<AppMetaEntity>(object : EntityInsertAdapter<AppMetaEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `app_meta` (`key`,`value`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AppMetaEntity) {
        statement.bindText(1, entity.key)
        statement.bindText(2, entity.value)
      }
    }, object : EntityDeleteOrUpdateAdapter<AppMetaEntity>() {
      protected override fun createQuery(): String = "UPDATE `app_meta` SET `key` = ?,`value` = ? WHERE `key` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AppMetaEntity) {
        statement.bindText(1, entity.key)
        statement.bindText(2, entity.value)
        statement.bindText(3, entity.key)
      }
    })
  }

  public override suspend fun upsert(entity: AppMetaEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfAppMetaEntity.upsert(_connection, entity)
  }

  public override suspend fun `get`(key: String): String? {
    val _sql: String = "SELECT value FROM app_meta WHERE `key` = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
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

  public override fun observe(key: String): Flow<String?> {
    val _sql: String = "SELECT value FROM app_meta WHERE `key` = ?"
    return createFlow(__db, false, arrayOf("app_meta")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
        val _result: String?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getText(0)
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

  public override fun observePrefix(prefix: String): Flow<List<AppMetaEntity>> {
    val _sql: String = "SELECT * FROM app_meta WHERE `key` LIKE ? || '%'"
    return createFlow(__db, false, arrayOf("app_meta")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, prefix)
        val _columnIndexOfKey: Int = getColumnIndexOrThrow(_stmt, "key")
        val _columnIndexOfValue: Int = getColumnIndexOrThrow(_stmt, "value")
        val _result: MutableList<AppMetaEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AppMetaEntity
          val _tmpKey: String
          _tmpKey = _stmt.getText(_columnIndexOfKey)
          val _tmpValue: String
          _tmpValue = _stmt.getText(_columnIndexOfValue)
          _item = AppMetaEntity(_tmpKey,_tmpValue)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(key: String) {
    val _sql: String = "DELETE FROM app_meta WHERE `key` = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, key)
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

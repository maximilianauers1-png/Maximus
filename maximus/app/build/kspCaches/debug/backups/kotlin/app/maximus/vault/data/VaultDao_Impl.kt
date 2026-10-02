package app.maximus.vault.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.ByteArray
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
public class VaultDao_Impl(
  __db: RoomDatabase,
) : VaultDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfVaultEntryEntity: EntityInsertAdapter<VaultEntryEntity>

  private val __updateAdapterOfVaultEntryEntity: EntityDeleteOrUpdateAdapter<VaultEntryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfVaultEntryEntity = object : EntityInsertAdapter<VaultEntryEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `vault_entry` (`id`,`uid`,`title`,`username`,`url`,`favorite`,`payload`,`createdAt`,`updatedAt`,`passwordChangedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: VaultEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.uid)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.username)
        statement.bindText(5, entity.url)
        val _tmp: Int = if (entity.favorite) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindBlob(7, entity.payload)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.updatedAt)
        statement.bindLong(10, entity.passwordChangedAt)
      }
    }
    this.__updateAdapterOfVaultEntryEntity = object : EntityDeleteOrUpdateAdapter<VaultEntryEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `vault_entry` SET `id` = ?,`uid` = ?,`title` = ?,`username` = ?,`url` = ?,`favorite` = ?,`payload` = ?,`createdAt` = ?,`updatedAt` = ?,`passwordChangedAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: VaultEntryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.uid)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.username)
        statement.bindText(5, entity.url)
        val _tmp: Int = if (entity.favorite) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindBlob(7, entity.payload)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.updatedAt)
        statement.bindLong(10, entity.passwordChangedAt)
        statement.bindLong(11, entity.id)
      }
    }
  }

  public override suspend fun insert(e: VaultEntryEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfVaultEntryEntity.insertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun update(e: VaultEntryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfVaultEntryEntity.handle(_connection, e)
  }

  public override fun observeAll(): Flow<List<VaultEntryEntity>> {
    val _sql: String = "SELECT * FROM vault_entry ORDER BY favorite DESC, title COLLATE NOCASE"
    return createFlow(__db, false, arrayOf("vault_entry")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _columnIndexOfFavorite: Int = getColumnIndexOrThrow(_stmt, "favorite")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfPasswordChangedAt: Int = getColumnIndexOrThrow(_stmt, "passwordChangedAt")
        val _result: MutableList<VaultEntryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: VaultEntryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUid: String
          _tmpUid = _stmt.getText(_columnIndexOfUid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_columnIndexOfUrl)
          val _tmpFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfFavorite).toInt()
          _tmpFavorite = _tmp != 0
          val _tmpPayload: ByteArray
          _tmpPayload = _stmt.getBlob(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpPasswordChangedAt: Long
          _tmpPasswordChangedAt = _stmt.getLong(_columnIndexOfPasswordChangedAt)
          _item = VaultEntryEntity(_tmpId,_tmpUid,_tmpTitle,_tmpUsername,_tmpUrl,_tmpFavorite,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt,_tmpPasswordChangedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(id: Long): VaultEntryEntity? {
    val _sql: String = "SELECT * FROM vault_entry WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUid: Int = getColumnIndexOrThrow(_stmt, "uid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfUsername: Int = getColumnIndexOrThrow(_stmt, "username")
        val _columnIndexOfUrl: Int = getColumnIndexOrThrow(_stmt, "url")
        val _columnIndexOfFavorite: Int = getColumnIndexOrThrow(_stmt, "favorite")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfPasswordChangedAt: Int = getColumnIndexOrThrow(_stmt, "passwordChangedAt")
        val _result: VaultEntryEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUid: String
          _tmpUid = _stmt.getText(_columnIndexOfUid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpUsername: String
          _tmpUsername = _stmt.getText(_columnIndexOfUsername)
          val _tmpUrl: String
          _tmpUrl = _stmt.getText(_columnIndexOfUrl)
          val _tmpFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfFavorite).toInt()
          _tmpFavorite = _tmp != 0
          val _tmpPayload: ByteArray
          _tmpPayload = _stmt.getBlob(_columnIndexOfPayload)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpPasswordChangedAt: Long
          _tmpPasswordChangedAt = _stmt.getLong(_columnIndexOfPasswordChangedAt)
          _result = VaultEntryEntity(_tmpId,_tmpUid,_tmpTitle,_tmpUsername,_tmpUrl,_tmpFavorite,_tmpPayload,_tmpCreatedAt,_tmpUpdatedAt,_tmpPasswordChangedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM vault_entry"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM vault_entry WHERE id = ?"
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

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM vault_entry"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

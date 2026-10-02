package app.maximus.vault.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Title, username and URL are stored in the clear inside the SQLCipher database (searchable);
 * [payload] is AES-256-GCM(VDK, VaultCodec(password, totp, notes), AAD = uid) on top of that.
 */
@Entity(tableName = "vault_entry", indices = [Index(value = ["uid"], unique = true)])
class VaultEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val title: String,
    val username: String,
    val url: String,
    val favorite: Boolean,
    val payload: ByteArray,
    val createdAt: Long,
    val updatedAt: Long,
    val passwordChangedAt: Long
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_entry ORDER BY favorite DESC, title COLLATE NOCASE")
    fun observeAll(): Flow<List<VaultEntryEntity>>

    @Query("SELECT * FROM vault_entry WHERE id = :id")
    suspend fun get(id: Long): VaultEntryEntity?

    @Query("SELECT COUNT(*) FROM vault_entry")
    suspend fun count(): Int

    @Insert
    suspend fun insert(e: VaultEntryEntity): Long

    @Update
    suspend fun update(e: VaultEntryEntity)

    @Query("DELETE FROM vault_entry WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM vault_entry")
    suspend fun deleteAll()
}

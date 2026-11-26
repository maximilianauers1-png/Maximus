package app.maximus.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppMetaDao {
    @Upsert
    suspend fun upsert(entity: AppMetaEntity)

    @Upsert
    suspend fun upsertAll(entities: List<AppMetaEntity>)

    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Query("SELECT * FROM app_meta WHERE `key` LIKE :prefix || '%'")
    suspend fun readPrefix(prefix: String): List<AppMetaEntity>

    @Query("DELETE FROM app_meta WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("SELECT value FROM app_meta WHERE `key` = :key")
    fun observe(key: String): Flow<String?>

    @Query("SELECT * FROM app_meta WHERE `key` LIKE :prefix || '%'")
    fun observePrefix(prefix: String): Flow<List<AppMetaEntity>>
}

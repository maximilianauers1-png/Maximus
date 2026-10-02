package app.maximus.dnd.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import app.maximus.data.db.MaximusDatabase
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/** Characters and monsters are stored as JSON so that the schema does not change with every rule addition. */
@Entity(tableName = "dnd_character")
data class DndCharacterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val summary: String,
    val payload: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "dnd_monster")
data class DndMonsterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val challengeRating: Double,
    val payload: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "dnd_roll", indices = [Index("epochMillis")])
data class DndRollEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expression: String,
    val label: String,
    val total: Int,
    val detail: String,
    val epochMillis: Long,
    val favorite: Boolean
)

@Dao
interface DndDao {
    @Query("SELECT * FROM dnd_character ORDER BY updatedAt DESC")
    fun observeCharacters(): Flow<List<DndCharacterEntity>>

    @Query("SELECT * FROM dnd_character WHERE id = :id")
    suspend fun character(id: Long): DndCharacterEntity?

    @Insert
    suspend fun insertCharacter(e: DndCharacterEntity): Long

    @Update
    suspend fun updateCharacter(e: DndCharacterEntity)

    @Query("DELETE FROM dnd_character WHERE id = :id")
    suspend fun deleteCharacter(id: Long)

    @Query("SELECT * FROM dnd_monster ORDER BY challengeRating, name COLLATE NOCASE")
    fun observeMonsters(): Flow<List<DndMonsterEntity>>

    @Query("SELECT * FROM dnd_monster WHERE id = :id")
    suspend fun monster(id: Long): DndMonsterEntity?

    @Insert
    suspend fun insertMonster(e: DndMonsterEntity): Long

    @Update
    suspend fun updateMonster(e: DndMonsterEntity)

    @Query("DELETE FROM dnd_monster WHERE id = :id")
    suspend fun deleteMonster(id: Long)

    @Query("SELECT * FROM dnd_roll ORDER BY epochMillis DESC LIMIT :limit")
    fun observeRolls(limit: Int): Flow<List<DndRollEntity>>

    @Insert
    suspend fun insertRoll(e: DndRollEntity): Long

    @Update
    suspend fun updateRoll(e: DndRollEntity)

    @Query("DELETE FROM dnd_roll WHERE favorite = 0")
    suspend fun clearHistory()

    @Query("DELETE FROM dnd_roll WHERE id = :id")
    suspend fun deleteRoll(id: Long)

    /** Keeps the newest [keep] non-favourite rolls and deletes the rest. */
    @Query("DELETE FROM dnd_roll WHERE favorite = 0 AND id NOT IN (SELECT id FROM dnd_roll WHERE favorite = 0 ORDER BY epochMillis DESC LIMIT :keep)")
    suspend fun trimHistory(keep: Int)
}

@Singleton
class DndRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private fun dao() = database.get().dndDao()

    companion object { const val HISTORY_LIMIT = 300 }

    val characters: Flow<List<DndCharacterEntity>> = flow { emitAll(dao().observeCharacters()) }.flowOn(io)
    val monsters: Flow<List<DndMonsterEntity>> = flow { emitAll(dao().observeMonsters()) }.flowOn(io)
    val rolls: Flow<List<DndRollEntity>> = flow { emitAll(dao().observeRolls(HISTORY_LIMIT)) }.flowOn(io)

    suspend fun character(id: Long) = withContext(io) { dao().character(id) }
    suspend fun monster(id: Long) = withContext(io) { dao().monster(id) }

    suspend fun saveCharacter(id: Long, name: String, summary: String, payload: String): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val old = if (id != 0L) dao().character(id) else null
        if (old == null) dao().insertCharacter(DndCharacterEntity(name = name, summary = summary, payload = payload, createdAt = now, updatedAt = now))
        else { dao().updateCharacter(old.copy(name = name, summary = summary, payload = payload, updatedAt = now)); old.id }
    }

    suspend fun saveMonster(id: Long, name: String, cr: Double, payload: String): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val old = if (id != 0L) dao().monster(id) else null
        if (old == null) dao().insertMonster(DndMonsterEntity(name = name, challengeRating = cr, payload = payload, createdAt = now, updatedAt = now))
        else { dao().updateMonster(old.copy(name = name, challengeRating = cr, payload = payload, updatedAt = now)); old.id }
    }

    suspend fun deleteCharacter(id: Long) = withContext(io) { dao().deleteCharacter(id) }
    suspend fun deleteMonster(id: Long) = withContext(io) { dao().deleteMonster(id) }

    suspend fun addRoll(expression: String, label: String, total: Int, detail: String) = withContext(io) {
        dao().insertRoll(DndRollEntity(expression = expression, label = label, total = total, detail = detail, epochMillis = System.currentTimeMillis(), favorite = false))
        dao().trimHistory(HISTORY_LIMIT)
    }

    suspend fun toggleFavorite(e: DndRollEntity) = withContext(io) { dao().updateRoll(e.copy(favorite = !e.favorite)) }
    suspend fun deleteRoll(id: Long) = withContext(io) { dao().deleteRoll(id) }
    suspend fun clearHistory() = withContext(io) { dao().clearHistory() }
}

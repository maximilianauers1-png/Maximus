package app.maximus.notes.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** [tags]: normalised, comma-separated (see MarkdownLite.normaliseTags). */
@Entity(tableName = "note")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val tags: String,
    val pinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM note ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM note WHERE id = :id")
    suspend fun get(id: Long): NoteEntity?

    @Insert
    suspend fun insert(n: NoteEntity): Long

    @Update
    suspend fun update(n: NoteEntity)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun delete(id: Long)
}

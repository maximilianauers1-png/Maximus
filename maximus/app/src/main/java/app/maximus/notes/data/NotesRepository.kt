package app.maximus.notes.data

import app.maximus.data.db.MaximusDatabase
import app.maximus.notes.domain.MarkdownLite
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@Singleton
class NotesRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private fun dao() = database.get().noteDao()

    val notes: Flow<List<NoteEntity>> = flow { emitAll(dao().observeAll()) }.flowOn(io)

    suspend fun get(id: Long): NoteEntity? = withContext(io) { dao().get(id) }

    /** Returns the id; a new note (id 0) is inserted. Tags are normalised. */
    suspend fun save(id: Long, title: String, body: String, tags: String, pinned: Boolean): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val t = MarkdownLite.normaliseTags(tags).joinToString(",")
        val old = if (id != 0L) dao().get(id) else null
        if (old == null) dao().insert(NoteEntity(title = title, body = body, tags = t, pinned = pinned, createdAt = now, updatedAt = now))
        else {
            if (old.title != title || old.body != body || old.tags != t || old.pinned != pinned) {
                dao().update(old.copy(title = title, body = body, tags = t, pinned = pinned, updatedAt = now))
            }
            old.id
        }
    }

    suspend fun delete(id: Long) = withContext(io) { dao().delete(id) }

    // Editor persistence runs outside the composition so that leaving the screen never loses the last edit.
    private val editorScope = CoroutineScope(SupervisorJob() + io)
    private val editorMutex = Mutex()

    /** Creates an empty note row so the editor always works on a stable id (no duplicate inserts). */
    suspend fun createEmpty(): Long = withContext(io) {
        val now = System.currentTimeMillis()
        dao().insert(NoteEntity(title = "", body = "", tags = "", pinned = false, createdAt = now, updatedAt = now))
    }

    fun persistAsync(id: Long, title: String, body: String, tags: String, pinned: Boolean) {
        editorScope.launch { editorMutex.withLock { if (dao().get(id) != null) save(id, title, body, tags, pinned) } }
    }

    /** Called when the editor closes: an untouched new note is removed again. */
    fun closeEditorAsync(id: Long, title: String, body: String, tags: String, pinned: Boolean) {
        editorScope.launch {
            editorMutex.withLock {
                when {
                    dao().get(id) == null -> Unit
                    title.isBlank() && body.isBlank() -> dao().delete(id)
                    else -> save(id, title, body, tags, pinned)
                }
            }
        }
    }
}

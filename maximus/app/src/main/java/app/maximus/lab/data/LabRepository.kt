package app.maximus.lab.data

import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.lab.domain.LabEvent
import app.maximus.lab.domain.LabProgress
import app.maximus.lab.domain.LabRules
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Persists the lab progress (XP, streaks, mastery, badges, flashcard boxes) as one text record in the
 * encrypted app_meta table, so no schema migration is needed. Updates are serialised by a mutex and
 * computed by the pure [LabRules] functions.
 */
@Singleton
class LabRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private val mutex = Mutex()
    private fun meta() = database.get().appMetaDao()

    val progress: Flow<LabProgress> = flow { emitAll(meta().observe(KEY)) }.map { LabRules.decode(it) }.flowOn(io)

    /** Applies [transform] atomically and returns the events it produced (XP, level-ups, badges). */
    suspend fun update(transform: (LabProgress) -> Pair<LabProgress, List<LabEvent>>): List<LabEvent> = withContext(io) {
        mutex.withLock {
            val current = LabRules.decode(meta().get(KEY))
            val (next, events) = transform(current)
            if (next != current) meta().upsert(AppMetaEntity(KEY, LabRules.encode(next)))
            events
        }
    }

    suspend fun reset() = withContext(io) { mutex.withLock { meta().delete(KEY) } }

    companion object {
        const val KEY = "lab.progress"
    }
}

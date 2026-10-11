package app.maximus.ai.data

import app.maximus.ai.domain.AiEvent
import app.maximus.ai.domain.AiProgress
import app.maximus.ai.domain.AiRules
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.lab.data.LabRepository
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
 * Persists the academy progress (XP, rank, stars, trophies, games) as one text record in the encrypted
 * app_meta table, like the lab. On first use it imports what the old AI path of the lab had recorded.
 */
@Singleton
class AiRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private val mutex = Mutex()
    private fun meta() = database.get().appMetaDao()

    val progress: Flow<AiProgress> = flow { emitAll(meta().observe(KEY)) }.map { AiRules.decode(it) }.flowOn(io)

    /** Seeds the record from the lab's old AI path once; afterwards the record exists and this is a no-op. */
    suspend fun importFromLabOnce() = withContext(io) {
        mutex.withLock {
            if (meta().get(KEY) != null) return@withLock
            val seeded = AiRules.importFromLab(meta().get(LabRepository.KEY))
            meta().upsert(AppMetaEntity(KEY, AiRules.encode(seeded)))
        }
    }

    /** Applies [transform] atomically and returns the events it produced (XP, rank-ups, badges, trophies). */
    suspend fun update(transform: (AiProgress) -> Pair<AiProgress, List<AiEvent>>): List<AiEvent> = withContext(io) {
        mutex.withLock {
            val current = AiRules.decode(meta().get(KEY))
            val (next, events) = transform(current)
            if (next != current) meta().upsert(AppMetaEntity(KEY, AiRules.encode(next)))
            events
        }
    }

    suspend fun reset() = withContext(io) { mutex.withLock { meta().delete(KEY) } }

    companion object {
        const val KEY = "ai.progress"
    }
}

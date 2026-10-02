package app.maximus.vault.data

import app.maximus.core.crypto.VaultSecret
import app.maximus.data.db.MaximusDatabase
import dagger.Lazy
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

data class VaultDraft(
    val id: Long,
    val title: String,
    val username: String,
    val url: String,
    val favorite: Boolean,
    val secret: VaultSecret
)

@Singleton
class VaultRepository @Inject constructor(
    private val database: Lazy<MaximusDatabase>,
    private val session: VaultSession
) {
    private val io = Dispatchers.IO
    private fun dao() = database.get().vaultDao()

    val entries: Flow<List<VaultEntryEntity>> = flow { emitAll(dao().observeAll()) }.flowOn(io)

    suspend fun reveal(id: Long): VaultDraft? = withContext(io) {
        val e = dao().get(id) ?: return@withContext null
        VaultDraft(e.id, e.title, e.username, e.url, e.favorite, session.decrypt(e.uid, e.payload))
    }

    /** Insert (id = 0) or update; passwordChangedAt only moves when the password actually changed. */
    suspend fun save(d: VaultDraft): Long = withContext(io) {
        val now = System.currentTimeMillis()
        val old = if (d.id != 0L) dao().get(d.id) else null
        if (old == null) {
            val uid = UUID.randomUUID().toString()
            dao().insert(
                VaultEntryEntity(
                    uid = uid, title = d.title, username = d.username, url = d.url, favorite = d.favorite,
                    payload = session.encrypt(uid, d.secret), createdAt = now, updatedAt = now, passwordChangedAt = now
                )
            )
        } else {
            val previous = session.decrypt(old.uid, old.payload)
            dao().update(
                VaultEntryEntity(
                    id = old.id, uid = old.uid, title = d.title, username = d.username, url = d.url, favorite = d.favorite,
                    payload = session.encrypt(old.uid, d.secret), createdAt = old.createdAt, updatedAt = now,
                    passwordChangedAt = if (previous.password != d.secret.password) now else old.passwordChangedAt
                )
            )
            old.id
        }
    }

    suspend fun delete(id: Long) = withContext(io) { dao().delete(id) }
}

package app.maximus.chat.data

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import app.maximus.chat.domain.ChatCodec
import app.maximus.chat.domain.ChatSettings
import app.maximus.chat.domain.ChatStore
import app.maximus.chat.domain.Conversation
import app.maximus.chat.domain.ModelCatalog
import app.maximus.chat.domain.ModelFile
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Chats live in the encrypted app_meta table (one record per conversation, SQLCipher), the model files
 * in the app's own storage. Nothing leaves the device.
 */
@Singleton
class ChatRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: Lazy<MaximusDatabase>
) : ChatStore {
    private val io = Dispatchers.IO
    private fun meta() = database.get().appMetaDao()

    val conversations: Flow<List<Conversation>> = flow { emitAll(meta().observePrefix(PREFIX)) }
        .map { rows -> rows.mapNotNull { ChatCodec.decode(it.value) }.sortedWith(compareByDescending<Conversation> { it.pinned }.thenByDescending { it.updatedAt }) }
        .flowOn(io)

    override suspend fun save(c: Conversation) = withContext(io) { meta().upsert(AppMetaEntity(PREFIX + c.id, ChatCodec.encode(c))) }

    suspend fun load(id: Long): Conversation? = withContext(io) { ChatCodec.decode(meta().get(PREFIX + id)) }

    suspend fun delete(id: Long) = withContext(io) { meta().delete(PREFIX + id) }

    suspend fun loadSettings(): ChatSettings = withContext(io) { ChatSettings.decode(meta().get(SETTINGS)) }

    suspend fun saveSettings(s: ChatSettings) = withContext(io) { meta().upsert(AppMetaEntity(SETTINGS, ChatSettings.encode(s))) }

    // ------------------------------------------------------------------ model files

    /**
     * App-specific storage (Android/data/app.maximus/files/models): no permission needed, removed with
     * the app, and reachable from a PC over USB, so a model can also be copied there directly.
     */
    fun modelDir(): File = (context.getExternalFilesDir("models") ?: File(context.filesDir, "models")).apply { mkdirs() }

    suspend fun listModels(): List<ModelFile> = withContext(io) {
        modelDir().listFiles().orEmpty()
            .filter { it.isFile && ModelCatalog.isModelFile(it.name) }
            .sortedBy { it.name.lowercase() }
            .map { ModelFile(it.absolutePath, it.name, it.length()) }
    }

    /**
     * Copies a model chosen with the system file picker into [modelDir]. Written to a temporary file and
     * renamed at the end, so an interrupted copy never looks like a valid model.
     */
    suspend fun importModel(uri: Uri, onProgress: (Float) -> Unit): ModelFile = withContext(io) {
        val resolver = context.contentResolver
        var name = "modell.task"
        var size = -1L
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                c.getString(0)?.let { name = it }
                if (!c.isNull(1)) size = c.getLong(1)
            }
        }
        name = name.replace(Regex("[/\\\\:*?\"<>|]"), "_")
        require(ModelCatalog.isModelFile(name)) { "Nur .task-Dateien (MediaPipe-Format) werden unterstützt: $name" }
        val dir = modelDir()
        val free = StatFs(dir.path).availableBytes
        if (size > 0 && free < size + 256L * 1024 * 1024) throw IOException("Zu wenig Speicher: ${size / MB} MB benötigt, ${free / MB} MB frei")
        val target = File(dir, name)
        val tmp = File(dir, "$name.part")
        try {
            resolver.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { out ->
                    val buf = ByteArray(8 * 1024 * 1024)
                    var copied = 0L
                    var lastReport = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        copied += n
                        if (size > 0 && copied - lastReport >= 32L * MB) {
                            lastReport = copied
                            onProgress((copied.toDouble() / size).toFloat().coerceIn(0f, 1f))
                        }
                    }
                }
            } ?: throw IOException("Datei kann nicht geöffnet werden")
            if (target.exists()) target.delete()
            if (!tmp.renameTo(target)) throw IOException("Umbenennen fehlgeschlagen")
            onProgress(1f)
            ModelFile(target.absolutePath, target.name, target.length())
        } finally {
            if (tmp.exists()) tmp.delete()
        }
    }

    suspend fun deleteModel(model: ModelFile) = withContext(io) { File(model.path).delete() }

    companion object {
        const val PREFIX = "chat.c."
        const val SETTINGS = "chat.settings"
        private const val MB = 1024L * 1024
    }
}

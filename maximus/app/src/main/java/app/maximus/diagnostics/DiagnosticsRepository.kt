package app.maximus.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import app.maximus.core.memory.HeavyResourceGovernor
import app.maximus.core.security.DatabaseKeyManager
import app.maximus.core.security.KeySecurityLevel
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class EncryptionState { ENCRYPTED, PLAINTEXT, UNDETERMINED }

data class DiagnosticsSnapshot(
    val totalPssKb: Long,
    val javaHeapKb: Long?,
    val nativeHeapKb: Long?,
    val deviceTotalBytes: Long,
    val deviceAvailableBytes: Long,
    val lowMemory: Boolean,
    val databaseEncryption: EncryptionState,
    val cipherVersion: String?,
    val keySecurity: KeySecurityLevel,
    val residentHeavyComponent: String?
)

@Singleton
class DiagnosticsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: dagger.Lazy<MaximusDatabase>,
    private val keyManager: DatabaseKeyManager,
    private val governor: HeavyResourceGovernor
) {

    suspend fun snapshot(): DiagnosticsSnapshot = withContext(Dispatchers.IO) {
        val db = database.get()
        db.appMetaDao().upsert(AppMetaEntity(KEY_LAST_DIAGNOSTICS, System.currentTimeMillis().toString()))

        val sqlDb = db.openHelper.writableDatabase
        val cipherVersion = sqlDb.query("PRAGMA cipher_version").use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
        // Flush the WAL so that page 1 (the file header) is on disk before inspecting it.
        sqlDb.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }

        val memoryInfo = Debug.MemoryInfo().also(Debug::getMemoryInfo)
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val systemInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)

        DiagnosticsSnapshot(
            totalPssKb = memoryInfo.totalPss.toLong(),
            javaHeapKb = memoryInfo.getMemoryStat("summary.java-heap")?.toLongOrNull(),
            nativeHeapKb = memoryInfo.getMemoryStat("summary.native-heap")?.toLongOrNull(),
            deviceTotalBytes = systemInfo.totalMem,
            deviceAvailableBytes = systemInfo.availMem,
            lowMemory = systemInfo.lowMemory,
            databaseEncryption = inspectHeader(context.getDatabasePath(MaximusDatabase.NAME)),
            cipherVersion = cipherVersion,
            keySecurity = keyManager.securityLevel(),
            residentHeavyComponent = governor.residentName()
        )
    }

    private fun inspectHeader(file: File): EncryptionState {
        if (!file.exists()) return EncryptionState.UNDETERMINED
        val header = ByteArray(SQLITE_MAGIC.size)
        val read = file.inputStream().use { it.read(header) }
        if (read < header.size) return EncryptionState.UNDETERMINED
        return if (header.contentEquals(SQLITE_MAGIC)) EncryptionState.PLAINTEXT else EncryptionState.ENCRYPTED
    }

    private companion object {
        const val KEY_LAST_DIAGNOSTICS = "last_diagnostics_epoch_ms"
        val SQLITE_MAGIC = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
    }
}

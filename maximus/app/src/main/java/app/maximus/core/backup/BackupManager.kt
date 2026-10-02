package app.maximus.core.backup

import android.content.Context
import android.net.Uri
import app.maximus.data.db.MaximusDatabase
import app.maximus.vault.data.VaultSession
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface BackupResult {
    data class Exported(val bytes: Long, val includesVaultKey: Boolean) : BackupResult
    data class Restored(val vaultNeedsRelink: Boolean) : BackupResult
    data object WrongPassphrase : BackupResult
    data class IncompatibleVersion(val found: Int, val expected: Int) : BackupResult
    data class Failed(val reason: String) : BackupResult
}

/**
 * Encrypted full backup = a SQLCipher database written by sqlcipher_export(), keyed with a user passphrase
 * (SQLCipher 4 defaults: PBKDF2-HMAC-SHA512, 256 000 iterations, AES-256-CBC per page + HMAC-SHA512).
 * The file never exists unencrypted, not even temporarily. Written/read only through the Storage Access
 * Framework, so no storage permission and no network is involved.
 *
 * The device-bound wrapped vault key is excluded. If the vault was unlocked at export time, the raw VDK is
 * stored inside the encrypted backup (app_meta["backup.vault.vdk"]) so the vault survives a device change;
 * the backup passphrase then protects the vault as well.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: Lazy<MaximusDatabase>,
    private val vault: VaultSession
) {
    private fun tmpFile() = File(context.noBackupFilesDir, "backup.tmp.db")

    private fun cleanTmp() {
        val t = tmpFile()
        listOf(t, File(t.path + "-journal"), File(t.path + "-wal"), File(t.path + "-shm")).forEach { it.delete() }
    }

    suspend fun export(uri: Uri, passphrase: CharArray): BackupResult = withContext(Dispatchers.IO) {
        cleanTmp()
        val tmp = tmpFile()
        val db = database.get().openHelper.writableDatabase
        var includesKey = false
        try {
            db.execSQL("ATTACH DATABASE ? AS bk KEY ?", arrayOf<Any?>(tmp.absolutePath, String(passphrase)))
            try {
                db.query("SELECT sqlcipher_export('bk')").use { it.moveToFirst() }
                db.execSQL("PRAGMA bk.user_version = ${MaximusDatabase.VERSION}")
                db.execSQL("DELETE FROM bk.app_meta WHERE `key` = ?", arrayOf<Any?>(VaultSession.META_WRAPPED))
                val grant = vault.takeExportGrant()
                if (grant != null) {
                    db.execSQL("INSERT OR REPLACE INTO bk.app_meta(`key`, `value`) VALUES(?, ?)", arrayOf<Any?>(KEY_VDK, hex(grant)))
                    grant.fill(0)
                    includesKey = true
                }
            } finally {
                db.execSQL("DETACH DATABASE bk")
            }
            val out = context.contentResolver.openOutputStream(uri, "wt") ?: return@withContext BackupResult.Failed("output")
            out.use { o -> tmp.inputStream().use { it.copyTo(o) } }
            BackupResult.Exported(tmp.length(), includesKey)
        } catch (e: Exception) {
            BackupResult.Failed(e.javaClass.simpleName)
        } finally {
            cleanTmp()
        }
    }

    suspend fun restore(uri: Uri, passphrase: CharArray): BackupResult = withContext(Dispatchers.IO) {
        cleanTmp()
        val tmp = tmpFile()
        val room = database.get()
        val db = room.openHelper.writableDatabase
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext BackupResult.Failed("input")
            input.use { i -> tmp.outputStream().use { i.copyTo(it) } }
            db.execSQL("ATTACH DATABASE ? AS bk KEY ?", arrayOf<Any?>(tmp.absolutePath, String(passphrase)))
            var vdkHex: String? = null
            try {
                val readable = runCatching { db.query("SELECT count(*) FROM bk.sqlite_master").use { it.moveToFirst() } }.isSuccess
                if (!readable) return@withContext BackupResult.WrongPassphrase
                val version = db.query("PRAGMA bk.user_version").use { if (it.moveToFirst()) it.getInt(0) else 0 }
                if (version != MaximusDatabase.VERSION) return@withContext BackupResult.IncompatibleVersion(version, MaximusDatabase.VERSION)
                vdkHex = db.query("SELECT `value` FROM bk.app_meta WHERE `key` = ?", arrayOf<Any?>(KEY_VDK)).use {
                    if (it.moveToFirst()) it.getString(0) else null
                }
                room.runInTransaction(Runnable {
                    db.execSQL("PRAGMA defer_foreign_keys = ON")
                    for (t in TABLES.reversed()) db.execSQL("DELETE FROM main.`$t`")
                    for (t in TABLES) db.execSQL("INSERT INTO main.`$t` SELECT * FROM bk.`$t`")
                    // Settings: backup wins; device-bound keys stay unless the backup brings its own vault key.
                    val keep = if (vdkHex != null) emptyList() else listOf(VaultSession.META_WRAPPED)
                    val keepSql = keep.joinToString(",") { "'$it'" }
                    db.execSQL(if (keep.isEmpty()) "DELETE FROM main.app_meta" else "DELETE FROM main.app_meta WHERE `key` NOT IN ($keepSql)")
                    db.execSQL(
                        "INSERT OR REPLACE INTO main.app_meta SELECT * FROM bk.app_meta WHERE `key` NOT IN (?, ?)",
                        arrayOf<Any?>(KEY_VDK, VaultSession.META_WRAPPED)
                    )
                })
            } finally {
                runCatching { db.execSQL("DETACH DATABASE bk") }
            }
            room.invalidationTracker.refreshVersionsAsync()
            val relink = vdkHex != null
            if (vdkHex != null) {
                val key = unhex(vdkHex)
                vault.adoptRestoredKey(key)
                key.fill(0)
            } else {
                vault.lock()
                vault.refresh()
            }
            BackupResult.Restored(relink)
        } catch (e: Exception) {
            BackupResult.Failed(e.javaClass.simpleName)
        } finally {
            cleanTmp()
        }
    }

    companion object {
        const val KEY_VDK = "backup.vault.vdk"
        /** Parents before children. app_meta is handled separately. */
        val TABLES = listOf(
            "exercise", "program", "program_day", "program_exercise", "set_prescription",
            "workout_session", "set_log", "f_table_override",
            "vault_entry", "calendar_event", "calendar_exdate", "note", "nutrition_log", "dnd_character", "dnd_monster", "dnd_roll"
        )

        private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }
        private fun unhex(s: String) = ByteArray(s.length / 2) { s.substring(2 * it, 2 * it + 2).toInt(16).toByte() }
    }
}

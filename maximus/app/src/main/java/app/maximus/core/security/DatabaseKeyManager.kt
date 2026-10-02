package app.maximus.core.security

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.SecureRandom

class DatabaseKeyUnavailableException(message: String, cause: Throwable) : IllegalStateException(message, cause)

/**
 * Envelope encryption for the SQLCipher key:
 *   DEK = 256 random bits (SecureRandom), stored only as AES-GCM(KEK, DEK, AAD) in noBackupFilesDir;
 *   KEK = non-exportable Keystore key.
 * If the KEK is lost (Keystore reset) the DEK cannot be recovered and the database is
 * unreadable; this is reported explicitly and is NEVER healed by generating a new key,
 * which would silently orphan all user data. Recovery is via encrypted backup (phase P1).
 */
class DatabaseKeyManager(
    directory: File,
    kekAlias: String,
    keyFileName: String
) {
    private val aead = KeystoreAead(kekAlias)
    private val keyFile = File(directory, keyFileName)
    private val random = SecureRandom()

    /** Returns the SQLCipher raw-key literal x'…'. The caller owns the returned array. */
    @Synchronized
    fun obtainSqlCipherKey(): ByteArray {
        val raw = if (keyFile.exists()) unwrapExisting() else createAndStore()
        try {
            return SqlCipherKeyFormat.rawKeyLiteral(raw)
        } finally {
            raw.fill(0)
        }
    }

    fun securityLevel(): KeySecurityLevel = aead.keySecurityLevel()

    private fun unwrapExisting(): ByteArray =
        try {
            aead.decrypt(keyFile.readBytes(), AAD)
        } catch (e: Exception) {
            throw DatabaseKeyUnavailableException("Stored database key cannot be unwrapped", e)
        }

    private fun createAndStore(): ByteArray {
        val raw = ByteArray(SqlCipherKeyFormat.KEY_BYTES).also(random::nextBytes)
        val envelope = aead.encrypt(raw, AAD)
        val tmp = File(keyFile.parentFile, keyFile.name + ".tmp")
        FileOutputStream(tmp).use { out ->
            out.write(envelope)
            out.fd.sync()
        }
        if (!tmp.renameTo(keyFile)) {
            tmp.delete()
            raw.fill(0)
            throw IOException("Atomic rename of the database key file failed")
        }
        return raw
    }

    companion object {
        const val DEFAULT_KEK_ALIAS = "maximus.db.kek.v1"
        const val DEFAULT_KEY_FILE = "maximus_db.key.enc"
        private val AAD = "maximus-db-dek-v1".toByteArray(Charsets.US_ASCII)
    }
}

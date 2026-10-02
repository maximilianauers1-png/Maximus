package app.maximus.vault.data

import android.security.keystore.KeyPermanentlyInvalidatedException
import app.maximus.core.crypto.SoftwareAead
import app.maximus.core.crypto.VaultCodec
import app.maximus.core.crypto.VaultSecret
import app.maximus.core.security.AeadEnvelope
import app.maximus.core.security.KeystoreAead
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import dagger.Lazy
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VaultProblem { NO_SECURE_LOCK, KEY_INVALIDATED, CORRUPT }

sealed interface VaultState {
    data object Loading : VaultState
    data object NotSetUp : VaultState
    data object Locked : VaultState
    data object Unlocked : VaultState
    /** A backup containing a vault key was restored; the key must be re-wrapped for this device. */
    data object NeedsRelink : VaultState
    data class Unavailable(val problem: VaultProblem) : VaultState
}

/** Cipher to authenticate with BiometricPrompt plus the continuation to run afterwards. */
class VaultAuthRequest(val cipher: Cipher, val complete: suspend (Cipher) -> Unit)

/**
 * Key hierarchy:
 *   VDK (vault data key, 256 bit, SecureRandom) encrypts every entry payload in software (AES-GCM);
 *   VDK is stored only wrapped: AES-GCM(KEK, VDK) in app_meta["vault.vdk.wrapped"];
 *   KEK lives in the Keystore, requires BIOMETRIC_STRONG or device credential for EVERY use
 *   (per-operation auth via CryptoObject) and is not invalidated by new biometric enrolment.
 * The plaintext VDK exists only in memory between unlock and lock; lock overwrites it.
 * Auto-lock: after [IDLE_LOCK_MS] without [touch], when the app leaves the foreground (MainActivity.onStop)
 * and on memory trim.
 */
@Singleton
class VaultSession @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val kek = KeystoreAead(KEK_ALIAS, userAuthRequired = true, invalidatedByBiometricEnrollment = false)
    private val random = SecureRandom()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<VaultState>(VaultState.Loading)
    val state: StateFlow<VaultState> = _state

    private var vdk: ByteArray? = null
    private var aead: SoftwareAead? = null
    private var pendingVdk: ByteArray? = null
    private var exportGrant: ByteArray? = null
    private var exportGrantExpiry = 0L
    @Volatile private var lastTouch = 0L

    init {
        scope.launch {
            while (true) {
                delay(15_000)
                if (_state.value == VaultState.Unlocked && System.currentTimeMillis() - lastTouch > IDLE_LOCK_MS) lock()
            }
        }
    }

    private fun meta() = database.get().appMetaDao()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val s = synchronized(this@VaultSession) {
            when {
                pendingVdk != null -> VaultState.NeedsRelink
                vdk != null -> VaultState.Unlocked
                else -> null
            }
        }
        _state.value = s ?: if (meta().get(META_WRAPPED) == null) VaultState.NotSetUp else VaultState.Locked
    }

    /** Setup (new VDK) or relink (VDK from a restored backup): encrypt the VDK under the KEK after authentication. */
    fun beginWrap(): VaultAuthRequest? = guarded {
        VaultAuthRequest(kek.newEncryptCipher()) { cipher ->
            val key = synchronized(this) { pendingVdk?.copyOf() } ?: ByteArray(32).also(random::nextBytes)
            val envelope = kek.finishEncrypt(cipher, key, AAD)
            withContext(Dispatchers.IO) { meta().upsert(AppMetaEntity(META_WRAPPED, Base64.getEncoder().encodeToString(envelope))) }
            synchronized(this) {
                pendingVdk?.fill(0); pendingVdk = null
                install(key)
            }
            key.fill(0)
        }
    }

    suspend fun beginUnlock(): VaultAuthRequest? {
        val stored = withContext(Dispatchers.IO) { meta().get(META_WRAPPED) } ?: run { _state.value = VaultState.NotSetUp; return null }
        val parts = runCatching { AeadEnvelope.decode(Base64.getDecoder().decode(stored)) }.getOrElse {
            _state.value = VaultState.Unavailable(VaultProblem.CORRUPT); return null
        }
        return guarded {
            VaultAuthRequest(kek.newDecryptCipher(parts.iv)) { cipher ->
                val key = runCatching { kek.finishDecrypt(cipher, parts, AAD) }.getOrNull()
                if (key == null) {
                    _state.value = VaultState.Unavailable(VaultProblem.CORRUPT)
                } else {
                    synchronized(this) { install(key) }
                    key.fill(0)
                }
            }
        }
    }

    private fun install(key: ByteArray) {
        vdk?.fill(0); aead?.destroy()
        vdk = key.copyOf()
        aead = SoftwareAead(key)
        lastTouch = System.currentTimeMillis()
        _state.value = VaultState.Unlocked
    }

    private inline fun <T> guarded(block: () -> T): T? = try {
        block()
    } catch (e: KeyPermanentlyInvalidatedException) {
        _state.value = VaultState.Unavailable(VaultProblem.KEY_INVALIDATED); null
    } catch (e: java.security.InvalidAlgorithmParameterException) {
        _state.value = VaultState.Unavailable(VaultProblem.NO_SECURE_LOCK); null
    } catch (e: java.security.ProviderException) {
        _state.value = VaultState.Unavailable(VaultProblem.NO_SECURE_LOCK); null
    } catch (e: IllegalStateException) {
        _state.value = VaultState.Unavailable(VaultProblem.NO_SECURE_LOCK); null
    }

    @Synchronized
    fun lock() {
        vdk?.fill(0); vdk = null
        aead?.destroy(); aead = null
        if (_state.value == VaultState.Unlocked) _state.value = VaultState.Locked
    }

    fun touch() { lastTouch = System.currentTimeMillis() }

    @Synchronized
    fun encrypt(uid: String, secret: VaultSecret): ByteArray {
        val a = checkNotNull(aead) { "vault locked" }
        touch()
        return a.encrypt(VaultCodec.encode(secret), VaultCodec.aad(uid))
    }

    @Synchronized
    fun decrypt(uid: String, payload: ByteArray): VaultSecret {
        val a = checkNotNull(aead) { "vault locked" }
        touch()
        return VaultCodec.decode(a.decrypt(payload, VaultCodec.aad(uid)))
    }

    /** Copies the unlocked VDK into a one-shot grant (valid 5 min) so a backup can include it. */
    @Synchronized
    fun createExportGrant(): Boolean {
        val k = vdk ?: return false
        exportGrant?.fill(0)
        exportGrant = k.copyOf()
        exportGrantExpiry = System.currentTimeMillis() + 5 * 60_000
        return true
    }

    @Synchronized
    fun takeExportGrant(): ByteArray? {
        val g = exportGrant
        exportGrant = null
        return if (g != null && System.currentTimeMillis() <= exportGrantExpiry) g else { g?.fill(0); null }
    }

    /** Called by the restore: entries now belong to [key]; the old wrapped key was removed. */
    @Synchronized
    fun adoptRestoredKey(key: ByteArray) {
        lock()
        pendingVdk?.fill(0)
        pendingVdk = key.copyOf()
        _state.value = VaultState.NeedsRelink
    }

    /** Irreversibly deletes all vault entries, the wrapped key and the Keystore key. */
    suspend fun reset() = withContext(Dispatchers.IO) {
        synchronized(this@VaultSession) { lock(); pendingVdk?.fill(0); pendingVdk = null }
        val db = database.get()
        db.vaultDao().deleteAll()
        db.appMetaDao().delete(META_WRAPPED)
        runCatching { kek.deleteKey() }
        _state.value = VaultState.NotSetUp
    }

    companion object {
        const val KEK_ALIAS = "maximus.vault.kek.v1"
        const val META_WRAPPED = "vault.vdk.wrapped"
        const val IDLE_LOCK_MS = 5 * 60_000L
        private val AAD = "maximus-vault-vdk-v1".toByteArray(Charsets.US_ASCII)
    }
}

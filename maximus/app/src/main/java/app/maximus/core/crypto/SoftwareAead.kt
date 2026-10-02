package app.maximus.core.crypto

import app.maximus.core.security.AeadEnvelope
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256-GCM with a key held in process memory (the vault data key after unlock).
 * Fresh 96-bit IV per message from SecureRandom; collision probability after q messages
 * ≤ q²/2^97 (birthday bound), negligible for a personal vault.
 * Output uses the same envelope as the Keystore path: version ‖ IV ‖ ciphertext ‖ tag.
 */
class SoftwareAead(key: ByteArray, private val random: SecureRandom = SecureRandom()) {
    private val key = key.copyOf()

    init { require(key.size == 32) { "AES-256 key must be 32 bytes" } }

    fun encrypt(plaintext: ByteArray, aad: ByteArray): ByteArray {
        val iv = ByteArray(AeadEnvelope.IV_LENGTH).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
        cipher.updateAAD(aad)
        return AeadEnvelope.encode(iv, cipher.doFinal(plaintext))
    }

    /** Throws javax.crypto.AEADBadTagException if ciphertext, IV or AAD were altered or the key is wrong. */
    fun decrypt(envelope: ByteArray, aad: ByteArray): ByteArray {
        val parts = AeadEnvelope.decode(envelope)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, parts.iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(parts.ciphertextWithTag)
    }

    /** Overwrites this instance's key copy. JCA-internal copies are left to the GC (not controllable). */
    fun destroy() = key.fill(0)
}

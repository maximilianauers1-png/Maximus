package app.maximus.core.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM authenticated encryption with a non-exportable Android Keystore key.
 *
 * - The IV is never supplied by the caller: setRandomizedEncryptionRequired(true) forces the
 *   Keystore to draw a fresh random 96-bit IV for every encryption (C3).
 * - StrongBox is preferred and silently falls back to the TEE when unavailable.
 * - [userAuthRequired] binds every use of the key to BiometricPrompt / device credential;
 *   such keys must be used through [newEncryptCipher]/[newDecryptCipher] wrapped in a
 *   BiometricPrompt.CryptoObject, then completed with [finishEncrypt]/[finishDecrypt].
 */
class KeystoreAead(
    private val alias: String,
    private val userAuthRequired: Boolean = false,
    private val unlockedDeviceRequired: Boolean = false,
    /** false keeps the key valid when a new fingerprint/face is enrolled (vault: avoids silent data loss). */
    private val invalidatedByBiometricEnrollment: Boolean = true
) {

    fun encrypt(plaintext: ByteArray, aad: ByteArray): ByteArray =
        finishEncrypt(newEncryptCipher(), plaintext, aad)

    fun decrypt(envelope: ByteArray, aad: ByteArray): ByteArray {
        val parts = AeadEnvelope.decode(envelope)
        return finishDecrypt(newDecryptCipher(parts.iv), parts, aad)
    }

    fun newEncryptCipher(): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }

    fun newDecryptCipher(iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(TAG_BITS, iv))
        }

    fun finishEncrypt(cipher: Cipher, plaintext: ByteArray, aad: ByteArray): ByteArray {
        cipher.updateAAD(aad)
        val ciphertext = cipher.doFinal(plaintext)
        val iv = cipher.iv
        check(iv != null && iv.size == AeadEnvelope.IV_LENGTH) { "Keystore returned an unexpected IV" }
        return AeadEnvelope.encode(iv, ciphertext)
    }

    fun finishDecrypt(cipher: Cipher, parts: AeadEnvelope.Parts, aad: ByteArray): ByteArray {
        cipher.updateAAD(aad)
        return cipher.doFinal(parts.ciphertextWithTag)
    }

    fun keySecurityLevel(): KeySecurityLevel {
        val key = getOrCreateKey()
        val factory = SecretKeyFactory.getInstance(key.algorithm, ANDROID_KEYSTORE)
        val info = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (info.securityLevel) {
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeySecurityLevel.STRONGBOX
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeySecurityLevel.TRUSTED_ENVIRONMENT
                KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE -> KeySecurityLevel.SECURE_HARDWARE
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> KeySecurityLevel.SOFTWARE
                else -> KeySecurityLevel.UNKNOWN
            }
        } else {
            @Suppress("DEPRECATION")
            if (info.isInsideSecureHardware) KeySecurityLevel.SECURE_HARDWARE else KeySecurityLevel.SOFTWARE
        }
    }

    fun deleteKey() {
        synchronized(LOCK) {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (ks.containsAlias(alias)) ks.deleteEntry(alias)
        }
    }

    private fun getOrCreateKey(): SecretKey = synchronized(LOCK) {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey) ?: generateKey()
    }

    private fun generateKey(): SecretKey =
        try {
            generateKey(strongBox = true)
        } catch (e: StrongBoxUnavailableException) {
            generateKey(strongBox = false)
        }

    private fun generateKey(strongBox: Boolean): SecretKey {
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(KEY_BITS)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setIsStrongBoxBacked(strongBox)
            .setUnlockedDeviceRequired(unlockedDeviceRequired)

        if (userAuthRequired) {
            builder.setUserAuthenticationRequired(true)
            builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            }
        }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(builder.build()) }
            .generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        val LOCK = Any()
    }
}

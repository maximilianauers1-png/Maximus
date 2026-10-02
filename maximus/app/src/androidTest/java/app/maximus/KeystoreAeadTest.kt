package app.maximus

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.maximus.core.security.AeadEnvelope
import app.maximus.core.security.KeystoreAead
import java.util.UUID
import javax.crypto.AEADBadTagException
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreAeadTest {

    private val aead = KeystoreAead("test.aead." + UUID.randomUUID())
    private val aad = "test-aad".toByteArray()

    @After
    fun tearDown() = aead.deleteKey()

    @Test
    fun roundTripAndLayout() {
        val plaintext = "Kniebeuge 250 kg".toByteArray()
        val env = aead.encrypt(plaintext, aad)
        assertEquals(AeadEnvelope.HEADER_LENGTH + plaintext.size + AeadEnvelope.TAG_LENGTH, env.size)
        assertArrayEquals(plaintext, aead.decrypt(env, aad))
    }

    @Test
    fun everyEncryptionUsesAFreshIv() {
        val n = 256
        val ivs = HashSet<String>(n)
        repeat(n) {
            val iv = AeadEnvelope.decode(aead.encrypt(byteArrayOf(1, 2, 3), aad)).iv
            assertEquals(12, iv.size)
            ivs += iv.joinToString("") { b -> "%02x".format(b) }
        }
        assertEquals(n, ivs.size)
    }

    @Test
    fun tamperedCiphertextIsRejected() {
        val env = aead.encrypt(ByteArray(64) { it.toByte() }, aad)
        env[env.size - 1] = (env[env.size - 1].toInt() xor 0x01).toByte()
        assertThrows(AEADBadTagException::class.java) { aead.decrypt(env, aad) }
    }

    @Test
    fun wrongAssociatedDataIsRejected() {
        val env = aead.encrypt(ByteArray(8), aad)
        assertThrows(AEADBadTagException::class.java) { aead.decrypt(env, "other".toByteArray()) }
    }
}

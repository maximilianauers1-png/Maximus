package app.maximus.core.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AeadEnvelopeTest {

    private val iv = ByteArray(12) { it.toByte() }
    private val ct = ByteArray(16 + 5) { (100 + it).toByte() }

    @Test
    fun roundTrip() {
        val env = AeadEnvelope.encode(iv, ct)
        assertEquals(1 + 12 + 21, env.size)
        assertEquals(AeadEnvelope.VERSION, env[0])
        val parts = AeadEnvelope.decode(env)
        assertArrayEquals(iv, parts.iv)
        assertArrayEquals(ct, parts.ciphertextWithTag)
    }

    @Test
    fun rejectsWrongIvLength() {
        assertThrows(IllegalArgumentException::class.java) { AeadEnvelope.encode(ByteArray(16), ct) }
    }

    @Test
    fun rejectsCiphertextShorterThanTag() {
        assertThrows(IllegalArgumentException::class.java) { AeadEnvelope.encode(iv, ByteArray(15)) }
    }

    @Test
    fun rejectsTruncatedEnvelope() {
        assertThrows(IllegalArgumentException::class.java) { AeadEnvelope.decode(ByteArray(28)) }
    }

    @Test
    fun rejectsUnknownVersion() {
        val env = AeadEnvelope.encode(iv, ct).also { it[0] = 2 }
        assertThrows(IllegalArgumentException::class.java) { AeadEnvelope.decode(env) }
    }
}

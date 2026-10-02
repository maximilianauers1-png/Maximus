package app.maximus.core.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SqlCipherKeyFormatTest {

    @Test
    fun knownVector() {
        val key = ByteArray(32) { it.toByte() }
        val literal = String(SqlCipherKeyFormat.rawKeyLiteral(key), Charsets.US_ASCII)
        assertEquals("x'000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f'", literal)
        assertEquals(67, literal.length)
    }

    @Test
    fun highBitBytes() {
        val key = ByteArray(32) { 0xFF.toByte() }
        val literal = String(SqlCipherKeyFormat.rawKeyLiteral(key), Charsets.US_ASCII)
        assertEquals("x'" + "ff".repeat(32) + "'", literal)
    }

    @Test
    fun rejectsWrongLength() {
        assertThrows(IllegalArgumentException::class.java) { SqlCipherKeyFormat.rawKeyLiteral(ByteArray(16)) }
    }
}

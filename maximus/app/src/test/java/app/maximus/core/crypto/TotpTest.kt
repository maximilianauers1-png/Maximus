package app.maximus.core.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpTest {
    private val seed1 = "12345678901234567890".toByteArray()
    private val seed256 = "12345678901234567890123456789012".toByteArray()
    private val seed512 = "1234567890123456789012345678901234567890123456789012345678901234".toByteArray()

    /** RFC 4226 Appendix D, counters 0..9. */
    @Test fun rfc4226Vectors() {
        val expected = listOf("755224", "287082", "359152", "969429", "338314", "254676", "287922", "162583", "399871", "520489")
        expected.forEachIndexed { c, code -> assertEquals(code, Totp.hotp(seed1, c.toLong(), 6)) }
    }

    /** RFC 6238 Appendix B, 8 digits, 30 s step. */
    @Test fun rfc6238Vectors() {
        val rows = listOf(
            Triple(59L, "94287082", listOf("46119246", "90693936")),
            Triple(1111111109L, "07081804", listOf("68084774", "25091201")),
            Triple(1111111111L, "14050471", listOf("67062674", "99943326")),
            Triple(1234567890L, "89005924", listOf("91819424", "93441116")),
            Triple(2000000000L, "69279037", listOf("90698825", "38618901")),
            Triple(20000000000L, "65353130", listOf("77737706", "47863826"))
        )
        for ((t, sha1, others) in rows) {
            val c = Totp.counter(t, 30)
            assertEquals(sha1, Totp.hotp(seed1, c, 8, OtpAlgorithm.SHA1))
            assertEquals(others[0], Totp.hotp(seed256, c, 8, OtpAlgorithm.SHA256))
            assertEquals(others[1], Totp.hotp(seed512, c, 8, OtpAlgorithm.SHA512))
        }
    }

    @Test fun remainingSecondsAndFloorForNegativeTime() {
        val p = TotpParams(seed1)
        assertEquals(1, Totp.at(p, 59_000).secondsRemaining)
        assertEquals(30, Totp.at(p, 60_000).secondsRemaining)
        assertEquals(-1L, Totp.counter(-1, 30))
    }

    @Test fun parsesOtpauthUri() {
        val secret = Base32.encode(seed1)
        val p = Totp.parse("otpauth://totp/ACME%20Co:max@example.com?secret=$secret&issuer=ACME%20Co&algorithm=SHA256&digits=8&period=60")!!
        assertEquals("ACME Co", p.issuer)
        assertEquals("max@example.com", p.account)
        assertEquals(OtpAlgorithm.SHA256, p.algorithm)
        assertEquals(8, p.digits)
        assertEquals(60, p.periodSeconds)
        assertTrue(p.secret.contentEquals(seed1))
        assertNull(Totp.parse("otpauth://hotp/x?secret=$secret"))
        assertNull(Totp.parse("otpauth://totp/x?secret=$secret&algorithm=MD5"))
        assertTrue(Totp.parse(secret.lowercase().chunked(4).joinToString(" "))!!.secret.contentEquals(seed1))
    }
}

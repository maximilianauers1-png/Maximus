package app.maximus.core.crypto

import app.maximus.core.text.Roman
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoPrimitivesTest {
    /** RFC 4648 §10 test vectors. */
    @Test fun base32Rfc4648() {
        val v = listOf("" to "", "f" to "MY", "fo" to "MZXQ", "foo" to "MZXW6", "foob" to "MZXW6YQ", "fooba" to "MZXW6YTB", "foobar" to "MZXW6YTBOI")
        for ((plain, enc) in v) {
            assertEquals(enc, Base32.encode(plain.toByteArray()))
            if (plain.isNotEmpty()) assertEquals(plain, String(Base32.decode("$enc====")))
        }
        assertThrows(IllegalArgumentException::class.java) { Base32.decode("MZ1W") }
    }

    @Test fun inclusionExclusionMatchesBruteForce() {
        // Tiny case enumerable by hand: digits only (10) and length 3 -> 1000; lower+digits length 2:
        // |Σ|=36, valid = 36² − 26² − 10² = 1296 − 676 − 100 = 520.
        assertEquals(BigInteger.valueOf(1000), PasswordGenerator.validCount(PasswordPolicy(3, lower = false, upper = false, digits = true, symbols = false)))
        assertEquals(BigInteger.valueOf(520), PasswordGenerator.validCount(PasswordPolicy(2, lower = true, upper = false, digits = true, symbols = false)))
        val bits = PasswordGenerator.entropyBits(PasswordPolicy(20))
        // Unconstrained: 20 · log2(90) = 129.837. Requiring every class removes ≈ 9.8 % of strings,
        // dominated by the digit class: (80/90)^20 = 0.0948. Exact value log2|V| = 129.689.
        assertEquals(129.68905, bits, 1e-4)
    }

    @Test fun generatorRespectsPolicy() {
        val rnd = SecureRandom()
        repeat(200) {
            val p = PasswordPolicy(length = 12, excludeAmbiguous = true)
            val pw = PasswordGenerator.generate(p, rnd)
            assertEquals(12, pw.length)
            for (c in PasswordGenerator.classes(p)) assertTrue(pw.any { it in c })
            assertFalse(pw.any { it in "Il1O0o|" })
        }
    }

    @Test fun softwareAeadRoundTripAndTamper() {
        val key = ByteArray(32) { it.toByte() }
        val aead = SoftwareAead(key)
        val aad = VaultCodec.aad("uid-1")
        val env = aead.encrypt("geheim".toByteArray(), aad)
        assertArrayEquals("geheim".toByteArray(), aead.decrypt(env, aad))
        val tampered = env.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 1).toByte() }
        assertThrows(AEADBadTagException::class.java) { aead.decrypt(tampered, aad) }
        assertThrows(AEADBadTagException::class.java) { aead.decrypt(env, VaultCodec.aad("uid-2")) }
        assertFalse(env.contentEquals(aead.encrypt("geheim".toByteArray(), aad)))
    }

    @Test fun vaultCodecRoundTripAndBounds() {
        val s = VaultSecret("pä§sw0rd", "otpauth://totp/x?secret=ABC", "Zeile 1\nZeile 2 — ✓")
        assertEquals(s, VaultCodec.decode(VaultCodec.encode(s)))
        val enc = VaultCodec.encode(s)
        assertThrows(IllegalArgumentException::class.java) { VaultCodec.decode(enc.copyOf(enc.size - 1)) }
        assertThrows(IllegalArgumentException::class.java) { VaultCodec.decode(enc + byteArrayOf(0)) }
    }

    @Test fun romanNumerals() {
        assertEquals("MMXXVI", Roman.of(2026))
        assertEquals("MCMXCIV", Roman.of(1994))
        assertEquals("IV", Roman.of(4))
        assertEquals("MMMCMXCIX", Roman.of(3999))
    }

    @Test fun crackTime() {
        // 2^(H−1)/1e10 with H = 64 -> 9.22e8 s.
        assertEquals(9.223372036854776e8, PasswordGenerator.expectedCrackSeconds(64.0), 1.0)
    }
}

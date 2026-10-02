package app.maximus.core.crypto

import java.net.URLDecoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

enum class OtpAlgorithm(val jca: String) { SHA1("HmacSHA1"), SHA256("HmacSHA256"), SHA512("HmacSHA512") }

class TotpParams(
    val secret: ByteArray,
    val digits: Int = 6,
    val periodSeconds: Int = 30,
    val algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
    val issuer: String = "",
    val account: String = ""
) {
    init {
        require(digits in 6..8) { "digits must be 6..8" }
        require(periodSeconds in 1..600) { "period must be 1..600 s" }
        require(secret.isNotEmpty()) { "empty secret" }
    }
}

data class TotpCode(val code: String, val secondsRemaining: Int, val periodSeconds: Int)

/**
 * RFC 4226 (HOTP) and RFC 6238 (TOTP).
 *
 * HOTP(K, C) = Truncate(HMAC(K, C)) mod 10^d, with C the 8-byte big-endian counter and dynamic
 * truncation: o = h[len-1] & 0x0F, bin = (h[o] & 0x7F)<<24 | h[o+1]<<16 | h[o+2]<<8 | h[o+3].
 * TOTP uses C = ⌊(t − T0) / X⌋ with T0 = 0 and step X (floor division, valid for t < 0 too).
 */
object Totp {
    private val POW10 = intArrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000)

    fun hotp(key: ByteArray, counter: Long, digits: Int, algorithm: OtpAlgorithm = OtpAlgorithm.SHA1): String {
        val mac = Mac.getInstance(algorithm.jca)
        mac.init(SecretKeySpec(key, algorithm.jca))
        val msg = ByteArray(8)
        var c = counter
        for (i in 7 downTo 0) { msg[i] = (c and 0xFF).toByte(); c = c ushr 8 }
        val h = mac.doFinal(msg)
        val o = h[h.size - 1].toInt() and 0x0F
        val bin = ((h[o].toInt() and 0x7F) shl 24) or ((h[o + 1].toInt() and 0xFF) shl 16) or
            ((h[o + 2].toInt() and 0xFF) shl 8) or (h[o + 3].toInt() and 0xFF)
        return (bin % POW10[digits]).toString().padStart(digits, '0')
    }

    fun counter(epochSeconds: Long, periodSeconds: Int): Long = Math.floorDiv(epochSeconds, periodSeconds.toLong())

    fun at(params: TotpParams, epochMillis: Long): TotpCode {
        val t = Math.floorDiv(epochMillis, 1000L)
        val code = hotp(params.secret, counter(t, params.periodSeconds), params.digits, params.algorithm)
        val remaining = params.periodSeconds - Math.floorMod(t, params.periodSeconds.toLong()).toInt()
        return TotpCode(code, remaining, params.periodSeconds)
    }

    /**
     * Accepts either a bare base32 secret or an otpauth://totp/Label?secret=…&issuer=…&algorithm=…&digits=…&period=… URI
     * (Key Uri Format). Returns null for anything else, including otpauth://hotp.
     */
    fun parse(input: String): TotpParams? {
        val s = input.trim()
        if (s.isEmpty()) return null
        if (!s.startsWith("otpauth://", ignoreCase = true)) {
            return runCatching { TotpParams(Base32.decode(s)) }.getOrNull()
        }
        val rest = s.substring("otpauth://".length)
        val slash = rest.indexOf('/')
        if (slash < 0 || !rest.substring(0, slash).equals("totp", ignoreCase = true)) return null
        val afterType = rest.substring(slash + 1)
        val q = afterType.indexOf('?')
        val label = decode(if (q < 0) afterType else afterType.substring(0, q))
        val query = if (q < 0) "" else afterType.substring(q + 1)
        val params = query.split('&').filter { it.isNotEmpty() }.associate { kv ->
            val i = kv.indexOf('=')
            if (i < 0) kv.lowercase() to "" else kv.substring(0, i).lowercase() to decode(kv.substring(i + 1))
        }
        val secret = params["secret"] ?: return null
        val algorithm = when (params["algorithm"]?.uppercase()) {
            null, "SHA1" -> OtpAlgorithm.SHA1
            "SHA256" -> OtpAlgorithm.SHA256
            "SHA512" -> OtpAlgorithm.SHA512
            else -> return null
        }
        val digits = params["digits"]?.toIntOrNull() ?: 6
        val period = params["period"]?.toIntOrNull() ?: 30
        val colon = label.indexOf(':')
        val issuer = params["issuer"] ?: if (colon >= 0) label.substring(0, colon).trim() else ""
        val account = if (colon >= 0) label.substring(colon + 1).trim() else label.trim()
        return runCatching { TotpParams(Base32.decode(secret), digits, period, algorithm, issuer, account) }.getOrNull()
    }

    private fun decode(s: String): String = URLDecoder.decode(s.replace("+", "%2B"), "UTF-8")
}

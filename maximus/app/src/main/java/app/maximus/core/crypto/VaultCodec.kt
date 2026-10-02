package app.maximus.core.crypto

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/** Secret part of a vault entry; encrypted as one AEAD message. */
data class VaultSecret(val password: String, val totp: String, val notes: String)

/**
 * Binary layout: version (1 byte = 1), then for each field (password, totp, notes):
 * 4-byte big-endian length n followed by n bytes of UTF-8. Decoding checks every bound.
 */
object VaultCodec {
    private const val VERSION: Byte = 1
    private const val MAX_FIELD = 1 shl 20

    fun encode(s: VaultSecret): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(VERSION.toInt())
        for (f in listOf(s.password, s.totp, s.notes)) {
            val b = f.toByteArray(Charsets.UTF_8)
            out.write(ByteBuffer.allocate(4).putInt(b.size).array())
            out.write(b)
        }
        return out.toByteArray()
    }

    fun decode(bytes: ByteArray): VaultSecret {
        require(bytes.isNotEmpty() && bytes[0] == VERSION) { "unsupported vault payload version" }
        val buf = ByteBuffer.wrap(bytes, 1, bytes.size - 1)
        val fields = Array(3) {
            require(buf.remaining() >= 4) { "truncated payload" }
            val n = buf.int
            require(n in 0..MAX_FIELD && n <= buf.remaining()) { "invalid field length" }
            val b = ByteArray(n); buf.get(b); String(b, Charsets.UTF_8)
        }
        require(!buf.hasRemaining()) { "trailing bytes" }
        return VaultSecret(fields[0], fields[1], fields[2])
    }

    fun aad(uid: String): ByteArray = "maximus-vault-entry-v1:$uid".toByteArray(Charsets.UTF_8)
}

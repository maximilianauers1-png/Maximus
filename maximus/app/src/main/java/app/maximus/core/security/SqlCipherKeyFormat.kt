package app.maximus.core.security

/**
 * SQLCipher accepts a raw 256-bit key as the ASCII blob literal x'<64 hex digits>'.
 * A raw key bypasses PBKDF2, which is appropriate here because the key is 256 bits of
 * CSPRNG output (a key-stretching KDF only adds value for low-entropy passphrases).
 */
object SqlCipherKeyFormat {
    const val KEY_BYTES = 32
    const val LITERAL_LENGTH = 2 + 2 * KEY_BYTES + 1

    private val HEX = "0123456789abcdef".toByteArray(Charsets.US_ASCII)

    fun rawKeyLiteral(key: ByteArray): ByteArray {
        require(key.size == KEY_BYTES) { "SQLCipher raw key must be $KEY_BYTES bytes" }
        val out = ByteArray(LITERAL_LENGTH)
        out[0] = 'x'.code.toByte()
        out[1] = '\''.code.toByte()
        for (i in key.indices) {
            val v = key[i].toInt() and 0xFF
            out[2 + 2 * i] = HEX[v ushr 4]
            out[3 + 2 * i] = HEX[v and 0x0F]
        }
        out[LITERAL_LENGTH - 1] = '\''.code.toByte()
        return out
    }
}

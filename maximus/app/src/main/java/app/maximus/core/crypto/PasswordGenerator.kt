package app.maximus.core.crypto

import java.math.BigInteger
import java.security.SecureRandom
import kotlin.math.ln

data class PasswordPolicy(
    val length: Int = 20,
    val lower: Boolean = true,
    val upper: Boolean = true,
    val digits: Boolean = true,
    val symbols: Boolean = true,
    val excludeAmbiguous: Boolean = false
)

/**
 * Uniform password generation over the set V of strings of length L over Σ = ∪ C_i that contain at
 * least one character of every selected class C_i. Sampling: draw each position uniformly from Σ
 * (SecureRandom.nextInt is unbiased) and reject strings not in V. Rejection sampling of a uniform
 * distribution conditioned on V is uniform on V, so the entropy is exactly log2|V|, with
 *
 *   |V| = Σ_{S ⊆ {1..k}} (−1)^{|S|} (|Σ| − Σ_{i∈S} |C_i|)^L        (inclusion–exclusion).
 *
 * The expected number of draws is |Σ|^L / |V|, which is < 1.2 for L ≥ 12 with all four classes.
 */
object PasswordGenerator {
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!#$%&()*+,-./:;<=>?@[]^_{|}~"
    private const val AMBIGUOUS = "Il1O0o|"

    const val MIN_LENGTH = 4
    const val MAX_LENGTH = 128

    fun classes(p: PasswordPolicy): List<String> {
        val raw = buildList {
            if (p.lower) add(LOWER)
            if (p.upper) add(UPPER)
            if (p.digits) add(DIGITS)
            if (p.symbols) add(SYMBOLS)
        }
        return if (p.excludeAmbiguous) raw.map { c -> c.filterNot { it in AMBIGUOUS } } else raw
    }

    fun generate(p: PasswordPolicy, random: SecureRandom = SecureRandom()): String {
        val cls = classes(p)
        require(cls.isNotEmpty()) { "select at least one character class" }
        require(p.length in MIN_LENGTH..MAX_LENGTH && p.length >= cls.size) { "invalid length" }
        val sigma = cls.joinToString("")
        val buf = CharArray(p.length)
        while (true) {
            for (i in buf.indices) buf[i] = sigma[random.nextInt(sigma.length)]
            if (cls.all { c -> buf.any { it in c } }) return String(buf)
        }
    }

    /** |V| by inclusion–exclusion (exact, arbitrary precision). */
    fun validCount(p: PasswordPolicy): BigInteger {
        val sizes = classes(p).map { it.length }
        val total = sizes.sum()
        var sum = BigInteger.ZERO
        for (mask in 0 until (1 shl sizes.size)) {
            var removed = 0
            for (i in sizes.indices) if (mask and (1 shl i) != 0) removed += sizes[i]
            val term = BigInteger.valueOf((total - removed).toLong()).pow(p.length)
            sum = if (Integer.bitCount(mask) % 2 == 0) sum + term else sum - term
        }
        return sum
    }

    fun entropyBits(p: PasswordPolicy): Double {
        if (classes(p).isEmpty() || p.length < classes(p).size) return 0.0
        return log2(validCount(p))
    }

    /**
     * Upper bound for a password NOT produced by the generator: L · log2|pool|, where the pool is the
     * union of the classes that occur. Human choices are far from uniform, so real entropy is lower.
     */
    fun upperBoundBits(password: String): Double {
        if (password.isEmpty()) return 0.0
        var pool = 0
        if (password.any { it in LOWER }) pool += 26
        if (password.any { it in UPPER }) pool += 26
        if (password.any { it in DIGITS }) pool += 10
        if (password.any { it in SYMBOLS }) pool += SYMBOLS.length
        if (password.any { it !in LOWER && it !in UPPER && it !in DIGITS && it !in SYMBOLS }) pool += 100
        return password.length * ln(pool.toDouble()) / ln(2.0)
    }

    /**
     * Expected offline guessing time in seconds for H bits at [guessesPerSecond]:
     * a uniform secret is found after (N+1)/2 ≈ 2^(H−1) guesses on average.
     */
    fun expectedCrackSeconds(bits: Double, guessesPerSecond: Double = 1e10): Double =
        Math.pow(2.0, bits - 1.0) / guessesPerSecond

    fun log2(n: BigInteger): Double {
        require(n.signum() > 0)
        val shift = (n.bitLength() - 53).coerceAtLeast(0)
        return shift + ln(n.shiftRight(shift).toDouble()) / ln(2.0)
    }
}

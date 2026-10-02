package app.maximus.core.text

/** Standard subtractive Roman numerals, 1..3999. */
object Roman {
    private val VALUES = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    private val SYMBOLS = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

    fun of(n: Int): String {
        require(n in 1..3999) { "Roman numerals cover 1..3999" }
        var r = n
        val sb = StringBuilder()
        for (i in VALUES.indices) while (r >= VALUES[i]) { sb.append(SYMBOLS[i]); r -= VALUES[i] }
        return sb.toString()
    }
}

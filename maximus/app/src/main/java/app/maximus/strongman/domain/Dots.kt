package app.maximus.strongman.domain

enum class Sex { MALE, FEMALE }

/** DOTS = total * 500 / (a + b x + c x^2 + d x^3 + e x^4), x = bodyweight in kg (brief 3.1). */
object Dots {
    private val MALE = doubleArrayOf(-307.75076, 24.0900756, -0.1918759221, 0.0007391293, -0.000001093)
    private val FEMALE = doubleArrayOf(-57.96288, 13.6175032, -0.1126655495, 0.0005158568, -0.0000010706)

    fun denominator(sex: Sex, bodyweightKg: Double): Double {
        val c = if (sex == Sex.MALE) MALE else FEMALE
        var acc = c[4]
        for (i in 3 downTo 0) acc = acc * bodyweightKg + c[i]
        return acc
    }

    /** Null when the polynomial is non-positive (bodyweight far outside its fitted range). */
    fun coefficient(sex: Sex, bodyweightKg: Double): Double? {
        if (!(bodyweightKg > 0.0)) return null
        val d = denominator(sex, bodyweightKg)
        return if (d > 0.0) 500.0 / d else null
    }

    fun score(totalKg: Double, bodyweightKg: Double, sex: Sex): Double? =
        coefficient(sex, bodyweightKg)?.let { totalKg * it }
}

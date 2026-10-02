package app.maximus.strongman.data

import app.maximus.strongman.domain.PlateStock
import app.maximus.strongman.domain.Sex

data class StrengthSettings(
    val sex: Sex = Sex.MALE,
    val bodyweightKg: Double? = null,
    val incrementKg: Double = 2.5,
    val barKg: Double = 20.0,
    val plates: List<PlateStock> = DEFAULT_PLATES
) {
    companion object {
        const val KEY_PREFIX = "strength."
        const val KEY_SEX = "strength.sex"
        const val KEY_BODYWEIGHT = "strength.bodyweight"
        const val KEY_INCREMENT = "strength.increment"
        const val KEY_BAR = "strength.bar"
        const val KEY_PLATES = "strength.plates"

        val DEFAULT_PLATES = listOf(
            PlateStock(25.0, 4), PlateStock(20.0, 2), PlateStock(15.0, 1), PlateStock(10.0, 2),
            PlateStock(5.0, 2), PlateStock(2.5, 2), PlateStock(1.25, 2), PlateStock(0.5, 1), PlateStock(0.25, 1)
        )

        fun encodePlates(plates: List<PlateStock>): String = plates.joinToString(";") { "${it.kg}:${it.pairs}" }

        fun decodePlates(text: String): List<PlateStock> = text.split(';').mapNotNull { part ->
            val bits = part.split(':')
            val kg = bits.getOrNull(0)?.toDoubleOrNull()
            val pairs = bits.getOrNull(1)?.toIntOrNull()
            if (kg != null && pairs != null && kg > 0.0 && pairs >= 0) PlateStock(kg, pairs) else null
        }

        fun fromMap(map: Map<String, String>): StrengthSettings = StrengthSettings(
            sex = if (map[KEY_SEX] == Sex.FEMALE.name) Sex.FEMALE else Sex.MALE,
            bodyweightKg = map[KEY_BODYWEIGHT]?.toDoubleOrNull(),
            incrementKg = map[KEY_INCREMENT]?.toDoubleOrNull()?.takeIf { it > 0.0 } ?: 2.5,
            barKg = map[KEY_BAR]?.toDoubleOrNull()?.takeIf { it >= 0.0 } ?: 20.0,
            plates = map[KEY_PLATES]?.let(::decodePlates)?.takeIf { it.isNotEmpty() } ?: DEFAULT_PLATES
        )
    }
}

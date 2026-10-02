package app.maximus.nutrition.domain

import kotlin.math.max

/** Atwater general factors as used for EU labelling (Reg. 1169/2011, Annex XIV): kcal per gram. */
object Atwater {
    const val PROTEIN = 4.0
    const val CARB = 4.0
    const val FAT = 9.0
    const val FIBER = 2.0

    fun kcal(proteinG: Double, fatG: Double, carbG: Double, fiberG: Double = 0.0) =
        PROTEIN * proteinG + FAT * fatG + CARB * carbG + FIBER * fiberG
}

data class Nutrients(val kcal: Double, val proteinG: Double, val fatG: Double, val carbG: Double, val fiberG: Double) {
    operator fun plus(o: Nutrients) = Nutrients(kcal + o.kcal, proteinG + o.proteinG, fatG + o.fatG, carbG + o.carbG, fiberG + o.fiberG)
    operator fun times(k: Double) = Nutrients(kcal * k, proteinG * k, fatG * k, carbG * k, fiberG * k)

    companion object {
        val ZERO = Nutrients(0.0, 0.0, 0.0, 0.0, 0.0)
        fun of(proteinG: Double, fatG: Double, carbG: Double, fiberG: Double) =
            Nutrients(Atwater.kcal(proteinG, fatG, carbG, fiberG), proteinG, fatG, carbG, fiberG)
    }
}

data class MacroTargets(val kcal: Double, val proteinG: Double, val fatG: Double, val carbG: Double, val fiberG: Double) {
    val proteinKcalShare get() = 4 * proteinG / kcal
    val fatKcalShare get() = 9 * fatG / kcal
    val carbKcalShare get() = 4 * carbG / kcal
}

enum class MacroWarning { CARBS_NEGATIVE, FAT_BELOW_20_PERCENT, PROTEIN_BELOW_1_6, PROTEIN_ABOVE_3_0, CARBS_BELOW_3_PER_KG }

/**
 * Macronutrient split from an energy target:
 *   P = pk · m (Morton et al., Br J Sports Med 2018: gains in FFM plateau at ≈ 1.6 g/kg, 95 % CI up to 2.2 g/kg;
 *   in an energy deficit up to 2.3–3.1 g/kg of FFM is supported, Helms et al. 2014),
 *   F = fk · m (≥ 20 % of energy recommended), C = (E − 4P − 9F)/4 (remainder; ISSN: 3–7 g/kg for
 *   strength athletes). Fibre: 14 g per 1000 kcal (DGA 2020–2025).
 */
object MacroCalculator {
    fun targets(kcal: Double, weightKg: Double, proteinPerKg: Double, fatPerKg: Double): MacroTargets {
        val p = proteinPerKg * weightKg
        val f = fatPerKg * weightKg
        val c = (kcal - Atwater.PROTEIN * p - Atwater.FAT * f) / Atwater.CARB
        return MacroTargets(kcal, p, f, c, 14.0 * kcal / 1000.0)
    }

    fun warnings(t: MacroTargets, weightKg: Double): List<MacroWarning> = buildList {
        if (t.carbG < 0) add(MacroWarning.CARBS_NEGATIVE)
        if (t.fatKcalShare < 0.20) add(MacroWarning.FAT_BELOW_20_PERCENT)
        if (t.proteinG / weightKg < 1.6) add(MacroWarning.PROTEIN_BELOW_1_6)
        if (t.proteinG / weightKg > 3.0) add(MacroWarning.PROTEIN_ABOVE_3_0)
        if (t.carbG >= 0 && t.carbG / weightKg < 3.0) add(MacroWarning.CARBS_BELOW_3_PER_KG)
    }

    /** Protein per meal for maximal MPS: ≈ 0.4 g/kg per meal over ≥ 4 meals (Schoenfeld & Aragon, JISSN 2018). */
    fun proteinPerMeal(weightKg: Double, meals: Int, dailyProteinG: Double): Pair<Double, Double> =
        0.4 * weightKg to dailyProteinG / max(1, meals)
}

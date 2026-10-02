package app.maximus.nutrition.domain

import kotlin.math.log10

enum class BioSex { MALE, FEMALE }

enum class BmrFormula { MIFFLIN_ST_JEOR, HARRIS_BENEDICT_REVISED, KATCH_MCARDLE, CUNNINGHAM }

/** Physical activity level PAL = TDEE / BMR (FAO/WHO/UNU 2004 ranges). */
enum class ActivityLevel(val pal: Double) {
    SEDENTARY(1.40), LIGHT(1.55), MODERATE(1.70), HIGH(1.85), VERY_HIGH(2.05)
}

data class Body(
    val sex: BioSex,
    val ageYears: Int,
    val heightCm: Double,
    val weightKg: Double,
    val bodyFatPercent: Double? = null
) {
    val leanMassKg: Double? get() = bodyFatPercent?.let { weightKg * (1 - it / 100.0) }
}

/**
 * Resting / basal energy expenditure in kcal/d.
 *  Mifflin–St Jeor (Am J Clin Nutr 1990;51:241): 10 m + 6.25 h − 5 a + 5 (♂) / −161 (♀).
 *  Harris–Benedict revised by Roza & Shizgal (Am J Clin Nutr 1984;40:168):
 *    ♂ 88.362 + 13.397 m + 4.799 h − 5.677 a;  ♀ 447.593 + 9.247 m + 3.098 h − 4.330 a.
 *  Katch–McArdle: 370 + 21.6 FFM.  Cunningham (1980), better for athletes: 500 + 22 FFM.
 * m in kg, h in cm, a in years, FFM = fat-free mass in kg.
 */
object Energy {
    fun bmr(body: Body, formula: BmrFormula): Double? = with(body) {
        when (formula) {
            BmrFormula.MIFFLIN_ST_JEOR -> 10 * weightKg + 6.25 * heightCm - 5 * ageYears + if (sex == BioSex.MALE) 5.0 else -161.0
            BmrFormula.HARRIS_BENEDICT_REVISED ->
                if (sex == BioSex.MALE) 88.362 + 13.397 * weightKg + 4.799 * heightCm - 5.677 * ageYears
                else 447.593 + 9.247 * weightKg + 3.098 * heightCm - 4.330 * ageYears
            BmrFormula.KATCH_MCARDLE -> leanMassKg?.let { 370 + 21.6 * it }
            BmrFormula.CUNNINGHAM -> leanMassKg?.let { 500 + 22.0 * it }
        }
    }

    fun tdee(body: Body, formula: BmrFormula, level: ActivityLevel): Double? = bmr(body, formula)?.times(level.pal)

    /**
     * Net energy cost of an activity (Compendium of Physical Activities, Ainsworth 2011):
     * 1 MET ≡ 1 kcal·kg⁻¹·h⁻¹; the resting MET is subtracted because it is already contained in the BMR.
     */
    fun netActivityKcal(met: Double, weightKg: Double, minutes: Double): Double = (met - 1.0) * weightKg * minutes / 60.0
}

/** MET values from the 2011 Compendium (code in parentheses) or, marked "≈", closest matching entry. */
enum class Activity(val met: Double) {
    STRENGTH_VIGOROUS(6.0),      // 02050 resistance training, powerlifting/bodybuilding, vigorous
    STRENGTH_MODERATE(3.5),      // 02052 resistance training, multiple exercises, 8–15 reps
    STRONGMAN_EVENTS(8.0),       // ≈ 02040 circuit training, vigorous; carries and medleys
    WALKING_5KMH(3.5),           // 17190 walking 2.8–3.2 mph, level, moderate pace
    HIKING(6.0),                 // 17080 hiking, cross country
    RUNNING_10KMH(9.8),          // 12050 running 6 mph (10 min/mile)
    CYCLING_20KMH(6.8),          // 01015 bicycling, general, 12–13.9 mph
    SWIMMING_MODERATE(5.8),      // 18310 swimming laps, freestyle, light/moderate
    ROWING_MODERATE(7.0)         // 02071 stationary rowing, 100 W, moderate
}

/** Body-composition estimators; all lengths in cm, skinfolds in mm. */
object BodyComposition {
    fun bmi(weightKg: Double, heightCm: Double): Double = weightKg / (heightCm / 100.0).let { it * it }

    /** U.S. Navy circumference method (Hodgdon & Beckett 1984), metric form, via body density and Siri. */
    fun navyBodyFat(sex: BioSex, heightCm: Double, neckCm: Double, waistCm: Double, hipCm: Double?): Double? = when (sex) {
        BioSex.MALE -> if (waistCm > neckCm) 495.0 / (1.0324 - 0.19077 * log10(waistCm - neckCm) + 0.15456 * log10(heightCm)) - 450.0 else null
        BioSex.FEMALE -> hipCm?.takeIf { waistCm + it > neckCm }?.let {
            495.0 / (1.29579 - 0.35004 * log10(waistCm + it - neckCm) + 0.22100 * log10(heightCm)) - 450.0
        }
    }

    /**
     * Jackson–Pollock 3-site skinfolds: ♂ chest, abdomen, thigh (Jackson & Pollock 1978);
     * ♀ triceps, suprailiac, thigh (Jackson, Pollock & Ward 1980). Density → fat % by Siri: 495/D − 450.
     */
    fun jacksonPollock3(sex: BioSex, ageYears: Int, sumMm: Double): Double {
        val d = when (sex) {
            BioSex.MALE -> 1.10938 - 0.0008267 * sumMm + 0.0000016 * sumMm * sumMm - 0.0002574 * ageYears
            BioSex.FEMALE -> 1.0994921 - 0.0009929 * sumMm + 0.0000023 * sumMm * sumMm - 0.0001392 * ageYears
        }
        return 495.0 / d - 450.0
    }

    /** Deurenberg (Br J Nutr 1991;65:105): fat % ≈ 1.20 BMI + 0.23 age − 10.8 (♂ = 1) − 5.4; population estimate only. */
    fun deurenberg(sex: BioSex, ageYears: Int, bmi: Double): Double =
        1.20 * bmi + 0.23 * ageYears - 10.8 * (if (sex == BioSex.MALE) 1 else 0) - 5.4

    /** Fat-free mass index (Kouri 1995): FFM/h² and the height-normalised FFMI + 6.1 (1.8 − h), h in m. */
    fun ffmi(weightKg: Double, heightCm: Double, bodyFatPercent: Double): Pair<Double, Double> {
        val h = heightCm / 100.0
        val ffm = weightKg * (1 - bodyFatPercent / 100.0)
        val raw = ffm / (h * h)
        return raw to raw + 6.1 * (1.8 - h)
    }
}

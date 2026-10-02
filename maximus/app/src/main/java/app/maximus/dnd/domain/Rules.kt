package app.maximus.dnd.domain

import kotlin.math.floor

/**
 * Core 5th-edition rules maths. All content in this package is taken from the System Reference
 * Document 5.1 ("SRD 5.1"), © Wizards of the Coast LLC, licensed under CC BY 4.0
 * (https://creativecommons.org/licenses/by/4.0/legalcode). Material from other books is NOT included;
 * the homebrew editor exists so that anything beyond the SRD can be entered by the user.
 */
enum class Ability(val short: String) { STR("STR"), DEX("DEX"), CON("CON"), INT("INT"), WIS("WIS"), CHA("CHA") }

enum class Skill(val ability: Ability) {
    ACROBATICS(Ability.DEX), ANIMAL_HANDLING(Ability.WIS), ARCANA(Ability.INT), ATHLETICS(Ability.STR),
    DECEPTION(Ability.CHA), HISTORY(Ability.INT), INSIGHT(Ability.WIS), INTIMIDATION(Ability.CHA),
    INVESTIGATION(Ability.INT), MEDICINE(Ability.WIS), NATURE(Ability.INT), PERCEPTION(Ability.WIS),
    PERFORMANCE(Ability.CHA), PERSUASION(Ability.CHA), RELIGION(Ability.INT), SLEIGHT_OF_HAND(Ability.DEX),
    STEALTH(Ability.DEX), SURVIVAL(Ability.WIS)
}

enum class Size { TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN }

object Rules {
    const val MAX_LEVEL = 20

    /** Ability modifier ⌊(score − 10)/2⌋. */
    fun modifier(score: Int): Int = floor((score - 10) / 2.0).toInt()

    /** Proficiency bonus by character level: 2 + ⌊(L − 1)/4⌋ (SRD table). */
    fun proficiencyBonus(level: Int): Int = 2 + (level.coerceIn(1, MAX_LEVEL) - 1) / 4

    /** Proficiency bonus by challenge rating: 2 for CR ≤ 4, then +1 every 4 CR. */
    fun proficiencyBonusForCr(cr: Double): Int = maxOf(2, 2 + Math.ceil((cr - 4) / 4.0).toInt())

    /** Experience thresholds for character levels 1..20 (SRD). */
    val XP_FOR_LEVEL = intArrayOf(
        0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
        85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    )

    fun levelForXp(xp: Int): Int {
        var level = 1
        for (i in XP_FOR_LEVEL.indices) if (xp >= XP_FOR_LEVEL[i]) level = i + 1
        return level
    }

    /** Point buy (SRD "Variant: Customizing Ability Scores"): 27 points, scores 8..15. */
    val POINT_COST = mapOf(8 to 0, 9 to 1, 10 to 2, 11 to 3, 12 to 4, 13 to 5, 14 to 7, 15 to 9)
    const val POINT_BUY_BUDGET = 27
    val STANDARD_ARRAY = listOf(15, 14, 13, 12, 10, 8)

    fun pointBuyCost(scores: Collection<Int>): Int? = scores.fold(0) { acc, s -> acc + (POINT_COST[s] ?: return null) }

    /** Carrying capacity 15 × STR; push/drag/lift is twice that (SRD). */
    fun carryingCapacityKg(str: Int, size: Size): Double {
        val factor = when (size) { Size.TINY -> 0.5; Size.SMALL, Size.MEDIUM -> 1.0; Size.LARGE -> 2.0; Size.HUGE -> 4.0; Size.GARGANTUAN -> 8.0 }
        return 15 * str * factor * 0.4535924
    }

    /** Spell save DC = 8 + proficiency + spellcasting modifier. */
    fun spellSaveDc(proficiency: Int, modifier: Int) = 8 + proficiency + modifier

    /** Passive score = 10 + modifier + proficiency (+5 advantage, −5 disadvantage). */
    fun passive(modifier: Int, proficiency: Int, advantage: Int = 0) = 10 + modifier + proficiency + 5 * advantage

    /**
     * Probability to hit AC with a d20 attack at the given bonus. A natural 1 always misses and a
     * natural 20 always hits, so p = (21 − max(2, min(20, AC − bonus)))/20; with advantage the miss
     * chance is squared, with disadvantage the hit chance is.
     */
    fun hitChance(attackBonus: Int, ac: Int, advantage: Int = 0): Double {
        val need = (ac - attackBonus).coerceIn(2, 20)
        val p = (21 - need) / 20.0
        return when {
            advantage > 0 -> 1 - (1 - p) * (1 - p)
            advantage < 0 -> p * p
            else -> p
        }
    }

    /** Probability of at least one natural 20 (crit) per attack, with advantage/disadvantage. */
    fun critChance(advantage: Int = 0, critRange: Int = 20): Double {
        val p = (21 - critRange) / 20.0
        return when {
            advantage > 0 -> 1 - (1 - p) * (1 - p)
            advantage < 0 -> p * p
            else -> p
        }
    }

    /**
     * Expected damage per attack: hits deal the damage expression, crits add the dice again.
     * DPR = p_hit · (D + M) + p_crit · D, where D is the mean of the dice only and M the flat modifier;
     * p_hit already includes the crit cases, so the crit term adds only the extra dice.
     */
    fun expectedDamage(hitChance: Double, critChance: Double, diceMean: Double, flat: Double): Double =
        hitChance * (diceMean + flat) + critChance * diceMean
}

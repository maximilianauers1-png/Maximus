package app.maximus.dnd.domain

import kotlin.math.abs
import kotlin.math.roundToInt

enum class MonsterType { ABERRATION, BEAST, CELESTIAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, FIEND, GIANT, HUMANOID, MONSTROSITY, OOZE, PLANT, UNDEAD }

data class MonsterAction(
    val name: String,
    val attackBonus: Int?,
    val reach: String,
    val damageExpression: String,
    val damageType: String,
    val description: String = "",
    val attacksPerRound: Int = 1
) {
    /** Mean of the damage expression (dice plus flat modifier), exact. */
    val averageDamage: Double
        get() = runCatching { DiceProbability.of(DiceParser.parse(damageExpression)).mean }.getOrDefault(0.0)
}

data class Monster(
    val id: Long = 0,
    val name: String = "Neues Monster",
    val size: Size = Size.MEDIUM,
    val type: MonsterType = MonsterType.HUMANOID,
    val alignment: String = "neutral",
    val armorClass: Int = 13,
    val armorNote: String = "",
    val hitDiceCount: Int = 5,
    val hitDieSize: Int = 8,
    val speed: String = "30 ft",
    val scores: Map<Ability, Int> = Ability.entries.associateWith { 10 },
    val saveProficiencies: Set<Ability> = emptySet(),
    val skillProficiencies: Set<Skill> = emptySet(),
    val damageResistances: String = "",
    val damageImmunities: String = "",
    val conditionImmunities: String = "",
    val senses: String = "",
    val languages: String = "",
    val actions: List<MonsterAction> = emptyList(),
    val traits: List<Pair<String, String>> = emptyList(),
    val legendaryActions: Int = 0,
    val notes: String = ""
) {
    val conModifier: Int get() = Rules.modifier(scores[Ability.CON] ?: 10)

    /**
     * Hit points = ⌊n(s+1)/2⌋ + n · CON: the average of the whole pool is rounded down once, not per die
     * (2d6 gives 7, not 8; 5d8 gives 22, not 25), which matches every published stat block.
     */
    val hitPoints: Int get() = maxOf(1, hitDiceCount * (hitDieSize + 1) / 2 + hitDiceCount * conModifier)

    val hitDiceText: String get() = "${hitDiceCount}d$hitDieSize${if (conModifier != 0) (if (conModifier > 0) " + " else " - ") + abs(hitDiceCount * conModifier) else ""}"
}

data class CrRating(
    val defensiveCr: Double,
    val offensiveCr: Double,
    val finalCr: Double,
    val effectiveHp: Int,
    val effectiveAc: Int,
    val damagePerRound: Double,
    val effectiveAttackBonus: Int,
    val expectedAcForCr: Int,
    val expectedAttackForCr: Int,
    val xp: Int,
    val proficiency: Int
)

/**
 * Challenge rating after the Dungeon Master's Guide method, using only the numbers the SRD exposes:
 *
 *   1. Defensive CR from effective hit points, corrected for an armour class that deviates from the
 *      expected AC of that CR: every 2 points of AC shift the CR by one step.
 *   2. Offensive CR from the damage per round over the first three rounds, corrected the same way for
 *      the attack bonus (also 2 points per step).
 *   3. Final CR = the average of both, rounded to the nearest rating.
 *
 * The table below is the DMG's "Monster Statistics by Challenge Rating"; the HP and damage columns are
 * stored as the lower bound of each band so a lookup is a simple search.
 */
object ChallengeRating {
    data class Row(val cr: Double, val prof: Int, val ac: Int, val hpMin: Int, val hpMax: Int, val attack: Int, val dprMin: Int, val dprMax: Int, val xp: Int)

    val TABLE = listOf(
        Row(0.0, 2, 13, 1, 6, 3, 0, 1, 10),
        Row(0.125, 2, 13, 7, 35, 3, 2, 3, 25),
        Row(0.25, 2, 13, 36, 49, 3, 4, 5, 50),
        Row(0.5, 2, 13, 50, 70, 3, 6, 8, 100),
        Row(1.0, 2, 13, 71, 85, 3, 9, 14, 200),
        Row(2.0, 2, 13, 86, 100, 3, 15, 20, 450),
        Row(3.0, 2, 13, 101, 115, 4, 21, 26, 700),
        Row(4.0, 2, 14, 116, 130, 5, 27, 32, 1100),
        Row(5.0, 3, 15, 131, 145, 6, 33, 38, 1800),
        Row(6.0, 3, 15, 146, 160, 6, 39, 44, 2300),
        Row(7.0, 3, 15, 161, 175, 6, 45, 50, 2900),
        Row(8.0, 3, 16, 176, 190, 7, 51, 56, 3900),
        Row(9.0, 4, 16, 191, 205, 7, 57, 62, 5000),
        Row(10.0, 4, 17, 206, 220, 7, 63, 68, 5900),
        Row(11.0, 4, 17, 221, 235, 8, 69, 74, 7200),
        Row(12.0, 4, 17, 236, 250, 8, 75, 80, 8400),
        Row(13.0, 5, 18, 251, 265, 8, 81, 86, 10000),
        Row(14.0, 5, 18, 266, 280, 8, 87, 92, 11500),
        Row(15.0, 5, 18, 281, 295, 8, 93, 98, 13000),
        Row(16.0, 5, 18, 296, 310, 9, 99, 104, 15000),
        Row(17.0, 6, 19, 311, 325, 10, 105, 110, 18000),
        Row(18.0, 6, 19, 326, 340, 10, 111, 116, 20000),
        Row(19.0, 6, 19, 341, 355, 10, 117, 122, 22000),
        Row(20.0, 6, 19, 356, 400, 10, 123, 140, 25000),
        Row(21.0, 7, 19, 401, 445, 11, 141, 158, 33000),
        Row(22.0, 7, 19, 446, 490, 11, 159, 176, 41000),
        Row(23.0, 7, 19, 491, 535, 11, 177, 194, 50000),
        Row(24.0, 7, 19, 536, 580, 12, 195, 212, 62000),
        Row(25.0, 8, 19, 581, 625, 12, 213, 230, 75000),
        Row(26.0, 8, 19, 626, 670, 12, 231, 248, 90000),
        Row(27.0, 8, 19, 671, 715, 13, 249, 266, 105000),
        Row(28.0, 8, 19, 716, 760, 13, 267, 284, 120000),
        Row(29.0, 9, 19, 761, 805, 13, 285, 302, 135000),
        Row(30.0, 9, 19, 806, 850, 14, 303, 320, 155000)
    )

    val CR_VALUES = TABLE.map { it.cr }

    fun row(cr: Double): Row = TABLE.minByOrNull { abs(it.cr - cr) }!!

    fun byHp(hp: Int): Double = TABLE.firstOrNull { hp in it.hpMin..it.hpMax }?.cr ?: if (hp < 1) 0.0 else 30.0
    fun byDpr(dpr: Double): Double {
        val d = dpr.roundToInt()
        return TABLE.firstOrNull { d in it.dprMin..it.dprMax }?.cr ?: if (d < 1) 0.0 else 30.0
    }

    /** Nearest CR in the table to a continuous value, measured on the index scale so that 1/8-steps count equally. */
    fun nearest(value: Double): Double {
        val i = CR_VALUES.indexOfFirst { it >= value }
        if (i <= 0) return CR_VALUES.first()
        val lo = CR_VALUES[i - 1]
        val hi = CR_VALUES[i]
        return if (value - lo <= hi - value) lo else hi
    }

    /** Shifts a CR by [steps] rows in the table (used for the AC and attack-bonus corrections). */
    fun shift(cr: Double, steps: Int): Double {
        val i = CR_VALUES.indexOfFirst { abs(it - cr) < 1e-9 }.coerceAtLeast(0)
        return CR_VALUES[(i + steps).coerceIn(0, CR_VALUES.lastIndex)]
    }

    fun rate(m: Monster): CrRating {
        val hp = m.hitPoints
        var defCr = byHp(hp)
        val expectedAc = row(defCr).ac
        // Every 2 points of AC above or below the expectation move the CR one step.
        val acSteps = Math.round((m.armorClass - expectedAc) / 2.0).toInt()
        defCr = shift(defCr, acSteps)

        // Damage per round: best single action times its attack count, averaged over three rounds.
        val dpr = m.actions.maxOfOrNull { it.averageDamage * it.attacksPerRound } ?: 0.0
        var offCr = byDpr(dpr)
        val expectedAttack = row(offCr).attack
        val attackBonus = m.actions.mapNotNull { it.attackBonus }.maxOrNull() ?: 0
        val attackSteps = Math.round((attackBonus - expectedAttack) / 2.0).toInt()
        offCr = shift(offCr, attackSteps)

        val finalCr = nearest((defCr + offCr) / 2.0)
        val r = row(finalCr)
        return CrRating(defCr, offCr, finalCr, hp, m.armorClass, dpr, attackBonus, expectedAc, expectedAttack, r.xp, r.prof)
    }

    /** CR as it is written in a stat block: 1/8, 1/4, 1/2, otherwise an integer. */
    fun format(cr: Double): String = when (cr) {
        0.125 -> "1/8"
        0.25 -> "1/4"
        0.5 -> "1/2"
        else -> cr.toInt().toString()
    }
}

/**
 * Encounter difficulty (DMG): the XP of the monsters is multiplied by a factor that grows with the
 * number of monsters and is then compared to the party's thresholds, which are the sum of the per
 * character XP thresholds for the four difficulty bands.
 */
object Encounter {
    private val EASY = intArrayOf(25, 50, 75, 125, 250, 300, 350, 450, 550, 600, 800, 1000, 1100, 1250, 1400, 1600, 2000, 2100, 2400, 2800)
    private val MEDIUM = intArrayOf(50, 100, 150, 250, 500, 600, 750, 900, 1100, 1200, 1600, 2000, 2200, 2500, 2800, 3200, 3900, 4200, 4900, 5700)
    private val HARD = intArrayOf(75, 150, 225, 375, 750, 900, 1100, 1400, 1600, 1900, 2400, 3000, 3400, 3800, 4300, 4800, 5900, 6300, 7300, 8500)
    private val DEADLY = intArrayOf(100, 200, 400, 500, 1100, 1400, 1700, 2100, 2400, 2800, 3600, 4500, 5100, 5700, 6400, 7200, 8800, 9500, 10900, 12700)

    enum class Difficulty { TRIVIAL, EASY, MEDIUM, HARD, DEADLY }

    data class Thresholds(val easy: Int, val medium: Int, val hard: Int, val deadly: Int)

    fun thresholds(levels: List<Int>): Thresholds {
        fun sum(a: IntArray) = levels.sumOf { a[it.coerceIn(1, 20) - 1] }
        return Thresholds(sum(EASY), sum(MEDIUM), sum(HARD), sum(DEADLY))
    }

    /** Encounter multiplier by number of monsters; a party below three or above five shifts it one step. */
    fun multiplier(monsterCount: Int, partySize: Int): Double {
        val base = when {
            monsterCount <= 1 -> 0
            monsterCount == 2 -> 1
            monsterCount <= 6 -> 2
            monsterCount <= 10 -> 3
            monsterCount <= 14 -> 4
            else -> 5
        }
        val steps = listOf(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0)
        val index = (base + 1 + when { partySize < 3 -> 1; partySize > 5 -> -1; else -> 0 }).coerceIn(0, steps.lastIndex)
        return steps[index]
    }

    fun difficulty(partyLevels: List<Int>, monsterXp: List<Int>): Pair<Difficulty, Int> {
        val t = thresholds(partyLevels)
        val adjusted = (monsterXp.sum() * multiplier(monsterXp.size, partyLevels.size)).roundToInt()
        val d = when {
            adjusted >= t.deadly -> Difficulty.DEADLY
            adjusted >= t.hard -> Difficulty.HARD
            adjusted >= t.medium -> Difficulty.MEDIUM
            adjusted >= t.easy -> Difficulty.EASY
            else -> Difficulty.TRIVIAL
        }
        return d to adjusted
    }
}

/**
 * Expected rounds for an attacker to drop a target, assuming independent rounds:
 * rounds = HP / DPR with DPR from [Rules.expectedDamage]. Used by the monster editor to sanity-check
 * a stat block against a party of four characters of the matching level.
 */
object CombatEstimate {
    fun roundsToKill(hp: Int, damagePerRound: Double): Double = if (damagePerRound <= 0) Double.POSITIVE_INFINITY else hp / damagePerRound

    /** DPR of a monster action against a given AC, including the crit bonus. */
    fun damagePerRound(action: MonsterAction, targetAc: Int): Double {
        val bonus = action.attackBonus ?: return action.averageDamage * action.attacksPerRound
        val node = runCatching { DiceParser.parse(action.damageExpression) }.getOrNull() ?: return 0.0
        val diceOnly = stripConstants(node)
        val diceMean = DiceProbability.of(diceOnly).mean
        val flat = DiceProbability.of(node).mean - diceMean
        val hit = Rules.hitChance(bonus, targetAc)
        return action.attacksPerRound * Rules.expectedDamage(hit, Rules.critChance(), diceMean, flat)
    }

    /** Replaces every constant by 0 so only the dice remain (crits double the dice, not the modifier). */
    private fun stripConstants(node: DiceNode): DiceNode = when (node) {
        is DiceNode.Const -> DiceNode.Const(0)
        is DiceNode.Pool -> node
        is DiceNode.Negate -> DiceNode.Negate(stripConstants(node.node))
        is DiceNode.Binary -> DiceNode.Binary(node.op, stripConstants(node.left), stripConstants(node.right))
        is DiceNode.MinOf -> DiceNode.MinOf(stripConstants(node.left), stripConstants(node.right))
        is DiceNode.MaxOf -> DiceNode.MaxOf(stripConstants(node.left), stripConstants(node.right))
    }
}

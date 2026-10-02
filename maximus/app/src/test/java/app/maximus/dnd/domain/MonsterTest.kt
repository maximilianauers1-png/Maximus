package app.maximus.dnd.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonsterTest {
    @Test fun hitPointsFollowTheAverageRule() {
        val m = Monster(hitDiceCount = 5, hitDieSize = 8, scores = Ability.entries.associateWith { 10 } + (Ability.CON to 14))
        // ⌊5·9/2⌋ + 5·2 = 22 + 10 = 32.
        assertEquals(32, m.hitPoints)
        assertEquals(22, Monster(hitDiceCount = 5, hitDieSize = 8, scores = Ability.entries.associateWith { 10 }).hitPoints)
        assertEquals(2, m.conModifier)
        // Negative CON cannot push hit points below 1.
        assertEquals(1, Monster(hitDiceCount = 1, hitDieSize = 4, scores = mapOf(Ability.CON to 1)).hitPoints)
    }

    private fun goblin() = Monster(
        name = "Goblin", size = Size.SMALL, type = MonsterType.HUMANOID, armorClass = 15,
        hitDiceCount = 2, hitDieSize = 6, scores = mapOf(Ability.STR to 8, Ability.DEX to 14, Ability.CON to 10, Ability.INT to 10, Ability.WIS to 8, Ability.CHA to 8),
        actions = listOf(MonsterAction("Krummsäbel", 4, "1,5 m", "1d6+2", "Hieb"))
    )

    @Test fun ratesAKnownStatBlock() {
        val m = goblin()
        assertEquals(7, m.hitPoints)
        val r = ChallengeRating.rate(m)
        // 7 HP falls in the 1/8 band; AC 15 is 2 above the expected 13, so one step up to 1/4.
        assertEquals(0.25, r.defensiveCr, 1e-9)
        // 1d6+2 averages 5.5; the 1/4 band is 4–5, so the offensive CR starts at 1/2... with attack +4
        // one above expectation (+3) but rounding of 1/2 keeps it at 1/4.
        assertTrue(r.offensiveCr in 0.125..0.5)
        assertEquals("1/4", ChallengeRating.format(r.finalCr))
        assertEquals(50, r.xp)
        assertEquals(2, r.proficiency)
    }

    @Test fun acAndAttackCorrectionsShiftTheRating() {
        val base = goblin()
        val tanky = base.copy(armorClass = 21)
        assertTrue(ChallengeRating.rate(tanky).defensiveCr > ChallengeRating.rate(base).defensiveCr)
        val weakArmour = base.copy(armorClass = 9)
        assertTrue(ChallengeRating.rate(weakArmour).defensiveCr < ChallengeRating.rate(base).defensiveCr)
        val hitter = base.copy(actions = listOf(MonsterAction("Schlag", 10, "1,5 m", "4d8+5", "Wucht", attacksPerRound = 2)))
        val r = ChallengeRating.rate(hitter)
        // 2 × (18 + 5) = 46 damage per round.
        assertEquals(46.0, r.damagePerRound, 1e-9)
        assertTrue(r.offensiveCr >= 7.0)
    }

    @Test fun tableIsMonotoneAndComplete() {
        assertEquals(34, ChallengeRating.TABLE.size)
        val crs = ChallengeRating.TABLE.map { it.cr }
        assertEquals(crs.sorted(), crs)
        for (i in 1 until ChallengeRating.TABLE.size) {
            assertTrue(ChallengeRating.TABLE[i].hpMin > ChallengeRating.TABLE[i - 1].hpMin)
            assertTrue(ChallengeRating.TABLE[i].xp > ChallengeRating.TABLE[i - 1].xp)
        }
        assertEquals(0.5, ChallengeRating.nearest(0.4), 1e-9)
        assertEquals(1.0, ChallengeRating.nearest(0.9), 1e-9)
        assertEquals("1/8", ChallengeRating.format(0.125))
        assertEquals("1/2", ChallengeRating.format(0.5))
        assertEquals("7", ChallengeRating.format(7.0))
    }

    @Test fun encounterDifficulty() {
        // Four level-3 characters: medium threshold 4 × 150 = 600.
        val t = Encounter.thresholds(List(4) { 3 })
        assertEquals(300, t.easy)
        assertEquals(600, t.medium)
        assertEquals(900, t.hard)
        assertEquals(1600, t.deadly)
        // Four monsters at 100 XP = 400, multiplier 2 -> 800 adjusted = hard.
        val (d, xp) = Encounter.difficulty(List(4) { 3 }, List(4) { 100 })
        assertEquals(800, xp)
        assertEquals(Encounter.Difficulty.MEDIUM, d)
        assertEquals(1.0, Encounter.multiplier(1, 4), 1e-9)
        assertEquals(1.5, Encounter.multiplier(2, 4), 1e-9)
        // A small party shifts the multiplier one step up.
        assertTrue(Encounter.multiplier(3, 2) > Encounter.multiplier(3, 4))
    }

    @Test fun combatEstimate() {
        val a = MonsterAction("Hieb", 5, "1,5 m", "2d6+3", "Hieb")
        // Against AC 15: hit 55 %, crit 5 %; dice mean 7, flat 3 -> 0.55·10 + 0.05·7 = 5.85.
        assertEquals(5.85, CombatEstimate.damagePerRound(a, 15), 1e-9)
        assertEquals(10.0, CombatEstimate.roundsToKill(30, 3.0), 1e-9)
        assertTrue(CombatEstimate.roundsToKill(30, 0.0).isInfinite())
    }
}

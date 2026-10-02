package app.maximus.dnd.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildGuidesTest {
    @Test
    fun hitChanceFollowsTheD20Rules() {
        assertEquals(0.55, DprMath.hitChance(5, 15), 1e-12)
        assertEquals(0.95, DprMath.hitChance(30, 10), 1e-12)   // natural 1 always misses
        assertEquals(0.05, DprMath.hitChance(-10, 25), 1e-12)  // natural 20 always hits
        assertEquals(0.10, DprMath.hitChance(-10, 25, critMin = 19), 1e-12)
        assertEquals(1 - 0.45 * 0.45, DprMath.hitChance(5, 15, advantage = true), 1e-12)
    }

    @Test
    fun expectedAttackDamageDoublesOnlyTheDice() {
        // p = 0.55, κ = 0.05: 0.55 (4.5 + 3) + 0.05 · 4.5
        assertEquals(0.55 * 7.5 + 0.05 * 4.5, DprMath.attack(5, 15, 4.5, 3.0), 1e-12)
    }

    @Test
    fun everyPlannedSpellAndOptionExists() {
        for (g in BuildGuides.all) {
            g.spellPlan.forEach { (_, k) -> assertTrue("${g.key}: spell $k", k in Spells.byKey) }
            g.invocationPlan.forEach { assertTrue("${g.key}: invocation $it", it in EldritchInvocations.byKey) }
            g.metamagicPlan.forEach { assertTrue("${g.key}: metamagic $it", it in MetamagicOptions.byKey) }
            g.subclasses.values.forEach { assertTrue("${g.key}: subclass $it", it in SrdSubclasses.byKey) }
            g.asiPlan.mapNotNull { it.featKey }.forEach { assertTrue("${g.key}: feat $it", it in SrdFeats.byKey) }
            assertEquals(20, g.steps.size)
            assertTrue(g.raceKey in SrdRaces.byKey)
        }
    }

    @Test
    fun guidesProduceLegalLevel20Characters() {
        for (g in BuildGuides.all) {
            for (level in 1..20) {
                val b = BuildGuides.buildAt(g, level)
                assertEquals(level, b.totalLevel)
                val s = CharacterBuilder.build(b)
                val bad = s.warnings.filter { "multiclass" in it || "invocations" in it || "metamagic" in it || "above 20" in it }
                assertTrue("${g.key} L$level: $bad", bad.isEmpty())
            }
        }
    }

    @Test
    fun nuclearWizardNovaAtLevel20() {
        val b = BuildGuides.buildAt(BuildGuides.nuclearWizard, 20)
        assertEquals(listOf("wizard" to 17, "warlock" to 1, "fighter" to 2), b.classes.map { it.classKey to it.level })
        val s = CharacterBuilder.build(b)
        assertEquals(20, s.scores[Ability.INT])
        // 9th + 8th level magic missile, each dart 2.5 + 1 + PB 6 + INT 5.
        assertEquals(11 * 14.5 + 10 * 14.5, BuildGuides.dpr(BuildGuides.nuclearWizard, 20).nova, 1e-9)
    }

    @Test
    fun sorlockBeamsAndQuickenedBlast() {
        val p = BuildGuides.dpr(BuildGuides.sorlock, 17, acOverride = 19)
        assertEquals(2 * p.sustained, p.nova, 1e-9)
        val b = BuildGuides.buildAt(BuildGuides.sorlock, 17)
        val s = CharacterBuilder.build(b)
        val eb = s.spellAttacks.first { it.name.startsWith("Eldritch Blast") }
        assertEquals("Eldritch Blast × 4", eb.name)
        assertTrue(eb.damage.startsWith("1d10+"))
    }

    @Test
    fun sorcadinSlotsUseHalfPaladinLevels() {
        val b = BuildGuides.buildAt(BuildGuides.sorcadin, 20)
        assertEquals(17, SpellSlots.casterLevel(b.classes))
        val s = CharacterBuilder.build(b)
        assertTrue(s.resources.any { it.first == "Sorcery points" && it.second.startsWith("14") })
        assertTrue(BuildGuides.dpr(BuildGuides.sorcadin, 20).nova > BuildGuides.dpr(BuildGuides.sorcadin, 20).sustained)
    }

    @Test
    fun hexWarriorUsesCharisma() {
        val b = CharacterBuild(
            classes = listOf(ClassLevel("warlock", 1, "hexblade")),
            baseScores = Ability.entries.associateWith { 10 } + mapOf(Ability.CHA to 16, Ability.STR to 10),
            attacks = listOf(AttackEntry(weaponName = "Longsword"))
        )
        val s = CharacterBuilder.build(b)
        assertEquals(3 + 2, s.attacks.first().attackBonus)
    }

    @Test
    fun cantripTierScaling() {
        assertEquals(listOf(1, 1, 2, 2, 3, 4), listOf(1, 4, 5, 10, 11, 17).map { SpellAttacks.tier(it) })
    }
}

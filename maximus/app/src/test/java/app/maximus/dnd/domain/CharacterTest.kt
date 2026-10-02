package app.maximus.dnd.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterTest {
    @Test fun rulesTables() {
        assertEquals(-1, Rules.modifier(8))
        assertEquals(0, Rules.modifier(10))
        assertEquals(5, Rules.modifier(20))
        assertEquals(-5, Rules.modifier(1))
        assertEquals(listOf(2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6), (1..20).map { Rules.proficiencyBonus(it) })
        assertEquals(2, Rules.proficiencyBonusForCr(0.0))
        assertEquals(2, Rules.proficiencyBonusForCr(4.0))
        assertEquals(3, Rules.proficiencyBonusForCr(5.0))
        assertEquals(6, Rules.proficiencyBonusForCr(17.0))
        assertEquals(5, Rules.levelForXp(7000))
        assertEquals(20, Rules.levelForXp(400000))
        assertEquals(27, Rules.pointBuyCost(listOf(15, 15, 15, 8, 8, 8)))
        assertEquals(27, Rules.pointBuyCost(Rules.STANDARD_ARRAY)!! + 0)
    }

    @Test fun standardArrayCostsLessThanBudget() {
        // 15,14,13,12,10,8 costs 9+7+5+4+2+0 = 27 — exactly the point-buy budget.
        assertEquals(Rules.POINT_BUY_BUDGET, Rules.pointBuyCost(Rules.STANDARD_ARRAY))
    }

    @Test fun hitChanceAndCrits() {
        // Need 11+ on the die: 10/20.
        assertEquals(0.5, Rules.hitChance(5, 16), 1e-12)
        // Natural 1 always misses, so an unreachable AC still has 5 %.
        assertEquals(0.05, Rules.hitChance(0, 40), 1e-12)
        // Natural 20 always hits, so a trivial AC caps at 95 %.
        assertEquals(0.95, Rules.hitChance(20, 2), 1e-12)
        assertEquals(1 - 0.5 * 0.5, Rules.hitChance(5, 16, advantage = 1), 1e-12)
        assertEquals(0.25, Rules.hitChance(5, 16, advantage = -1), 1e-12)
        assertEquals(0.0975, Rules.critChance(advantage = 1), 1e-12)
        // DPR of 1d8+3 at 50 % hit and 5 % crit: 0.5·(4.5+3) + 0.05·4.5 = 3.975.
        assertEquals(3.975, Rules.expectedDamage(0.5, 0.05, 4.5, 3.0), 1e-12)
    }

    @Test fun fighterLevelOneSheet() {
        val b = CharacterBuild(
            raceKey = "dwarf_mountain", classes = listOf(ClassLevel("fighter", 1)), backgroundKey = "soldier",
            baseScores = mapOf(Ability.STR to 15, Ability.DEX to 13, Ability.CON to 14, Ability.INT to 10, Ability.WIS to 12, Ability.CHA to 8),
            armorName = "Chain mail", shield = true, attacks = listOf(AttackEntry(weaponName = "Longsword"))
        )
        val s = CharacterBuilder.build(b)
        // Mountain dwarf: +2 STR, +2 CON.
        assertEquals(17, s.scores.getValue(Ability.STR))
        assertEquals(16, s.scores.getValue(Ability.CON))
        assertEquals(3, s.modifiers.getValue(Ability.STR))
        // HP = 10 + 3.
        assertEquals(13, s.maxHp)
        // Chain mail 16, no DEX, +2 shield.
        assertEquals(18, s.armorClass)
        assertEquals(25, s.speed)
        assertEquals(2, s.proficiency)
        // Attack +3 STR +2 proficiency; damage 1d8+3 averages 7.5.
        assertEquals(5, s.attacks[0].attackBonus)
        assertEquals(7.5, s.attacks[0].averageDamage, 1e-12)
        assertEquals("1d8+3", s.attacks[0].damage)
        assertTrue(Ability.STR in s.saveProficiencies && Ability.CON in s.saveProficiencies)
        assertEquals(5, s.saves.getValue(Ability.STR))
        assertTrue(s.skills.first { it.skill == Skill.ATHLETICS }.proficient)
        assertEquals(11, s.passivePerception)
        assertTrue(s.warnings.isEmpty())
    }

    @Test fun hillDwarfToughnessAndAverageHp() {
        val b = CharacterBuild(
            raceKey = "dwarf_hill", classes = listOf(ClassLevel("barbarian", 5)),
            baseScores = Ability.entries.associateWith { 14 }, hpMethod = HpMethod.AVERAGE
        )
        val s = CharacterBuilder.build(b)
        // CON 14+2 = 16 -> +3. HP = 12+3 + 4·(7+3) + 5 (toughness) = 60.
        assertEquals(60, s.maxHp)
        // Unarmoured barbarian: 10 + DEX 2 + CON 3 = 15.
        assertEquals(15, s.armorClass)
        // Fast Movement from level 5.
        assertEquals(35, s.speed)
    }

    @Test fun multiclassSlotsAndPrerequisites() {
        // Full caster 3 + half caster 4 -> caster level 3 + 2 = 5: 4/3/2.
        val slots = SpellSlots.slots(listOf(ClassLevel("wizard", 3), ClassLevel("paladin", 4)))
        assertEquals(listOf(4, 3, 2, 0, 0, 0, 0, 0, 0), slots.toList())
        assertEquals(0, SpellSlots.slots(listOf(ClassLevel("fighter", 5))).sum())
        assertEquals(2 to 3, SpellSlots.pactSlots(5))
        val weak = CharacterBuilder.build(
            CharacterBuild(classes = listOf(ClassLevel("fighter", 1), ClassLevel("wizard", 1)),
                baseScores = Ability.entries.associateWith { 10 }, raceKey = "human_variant")
        )
        assertTrue(weak.warnings.any { it.contains("multiclassing") })
    }

    @Test fun spellcastingNumbers() {
        val s = CharacterBuilder.build(
            CharacterBuild(raceKey = "human", classes = listOf(ClassLevel("wizard", 5)),
                baseScores = mapOf(Ability.INT to 15, Ability.DEX to 14, Ability.CON to 14, Ability.STR to 8, Ability.WIS to 12, Ability.CHA to 10))
        )
        // INT 16 -> +3, proficiency 3: DC 14, attack +6.
        val block = s.spellcasting.single()
        assertEquals(Ability.INT, block.ability)
        assertEquals(14, block.saveDc)
        assertEquals(6, block.attackBonus)
        assertEquals(listOf(4, 3, 2, 0, 0, 0, 0, 0, 0), s.spellSlots.toList())
    }

    @Test fun randomBuildsAreValid() {
        val r = Random(5)
        repeat(100) {
            val b = CharacterBuilder.random(r, level = 1 + r.nextInt(20))
            val s = CharacterBuilder.build(b)
            assertTrue(s.maxHp >= s.build.totalLevel)
            assertTrue(s.armorClass in 8..25)
            assertTrue(s.scores.values.all { v -> v in 3..20 })
            assertTrue(s.build.chosenSkills.size <= SrdClasses.byKey.getValue(b.classes[0].classKey).skillChoices)
        }
    }

    @Test fun contentIntegrity() {
        assertEquals(SrdRaces.all.size, SrdRaces.byKey.size)
        assertTrue(SrdRaces.all.size >= 25)
        assertEquals(12, SrdClasses.all.size)
        assertEquals(13, SrdBackgrounds.all.size)
        // Every class has at least one subclass and the subclass points back at an existing class.
        for (c in SrdClasses.all) {
            assertTrue(c.skillList.size >= c.skillChoices)
            assertTrue(SrdSubclasses.byClass[c.key].orEmpty().isNotEmpty())
        }
        for (sub in SrdSubclasses.all) assertTrue(sub.classKey in SrdClasses.byKey)
        for (r in SrdRaces.all) assertTrue(r.speed in 20..40)
        assertTrue(SRD_ATTRIBUTION.contains("Creative Commons"))
        // Free ability points never exceed what the 2024 rules allow.
        for (r in SrdRaces.all) assertTrue(r.freeAbilityPoints <= 3 && r.freeAbilityCap <= 2)
    }

    @Test fun spellDatabaseIsConsistent() {
        assertTrue(Spells.all.size >= 150)
        assertEquals(Spells.all.size, Spells.byKey.size)
        val classKeys = SrdClasses.all.map { it.key }.toSet()
        for (spell in Spells.all) {
            assertTrue(spell.level in 0..9)
            assertTrue(spell.classes.isNotEmpty())
            assertTrue(spell.classes.all { it in classKeys })
            assertTrue(spell.text.length > 20)
        }
        // Every spellcasting class has leveled spells; paladin and ranger get no cantrips by the rules.
        for (c in SrdClasses.all.filter { it.spellcasting != null }) {
            val list = Spells.forClass(c.key)
            assertTrue(list.count { it.level > 0 } >= 20)
            if (c.key != "paladin" && c.key != "ranger") assertTrue(list.any { it.level == 0 })
        }
        assertEquals(0, Spells.cantripsKnown("paladin", 20))
        assertEquals(3, Spells.cantripsKnown("wizard", 1))
        assertEquals(5, Spells.cantripsKnown("wizard", 10))
        assertEquals(4, Spells.spellsKnown("bard", 1))
        assertEquals(22, Spells.spellsKnown("bard", 20))
        assertEquals(null, Spells.spellsKnown("wizard", 5))
        // Prepared casters: ability modifier + level.
        assertEquals(8, Spells.spellsPrepared("cleric", 5, 3))
        assertEquals(5, Spells.spellsPrepared("paladin", 6, 2))
    }

    @Test fun inventoryAndHomebrewAffectTheSheet() {
        val b = CharacterBuild(
            raceKey = "human", classes = listOf(ClassLevel("wizard", 3)),
            baseScores = Ability.entries.associateWith { 12 },
            attacks = emptyList(),
            inventory = listOf(InventoryItem("Rope", 2, 10.0), InventoryItem("Torch", 5, 1.0)),
            homebrew = listOf(
                HomebrewEntry(HomebrewKind.ITEM, "Ring of protection", "+1 AC", "ac+1"),
                HomebrewEntry(HomebrewKind.SPECIES, "Tough hide", "More hit points", "hp+10"),
                HomebrewEntry(HomebrewKind.FEATURE, "Fleet", "Faster", "speed+10"),
                HomebrewEntry(HomebrewKind.RULE, "Trained sneak", "Stealth training", "skill:STEALTH")
            )
        )
        val s = CharacterBuilder.build(b)
        // 2 × 10 + 5 × 1 = 25 lb of inventory.
        assertEquals(25.0, s.carriedLb, 1e-9)
        // Human +1 everywhere: DEX 13 -> +1. AC 10 + 1 + 1 from the ring.
        assertEquals(12, s.armorClass)
        assertEquals(40, s.speed)
        assertTrue(s.skills.first { it.skill == Skill.STEALTH }.proficient)
        // d6 class, CON 13 -> +1: 6+1 + 2 × (4+1) = 17, plus 10 from homebrew.
        assertEquals(27, s.maxHp)
    }

    @Test fun multiclassAsiCountAndFreeAbilities() {
        val classes = listOf(ClassLevel("fighter", 6), ClassLevel("rogue", 4))
        // Fighter grants ASIs at 4 and 6, rogue at 4.
        assertEquals(3, CharacterBuilder.asiCount(classes))
        val b = CharacterBuild(
            raceKey = "orc", classes = classes,
            baseScores = Ability.entries.associateWith { 14 },
            freeAbilityBonuses = mapOf(Ability.STR to 2, Ability.CON to 1),
            asiBonuses = mapOf(Ability.STR to 2)
        )
        val s = CharacterBuilder.build(b)
        assertEquals(18, s.scores.getValue(Ability.STR))
        assertEquals(15, s.scores.getValue(Ability.CON))
        assertEquals(4, s.proficiency)
        assertEquals(10, b.totalLevel)
    }
}

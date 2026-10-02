package app.maximus.dnd.domain

/**
 * Derived combat lines for attack cantrips and the list of limited-use class resources.
 *
 * Cantrip scaling uses the character level L (not the class level): the number of dice, or of
 * eldritch blast beams, is n(L) = 1 + [L ≥ 5] + [L ≥ 11] + [L ≥ 17].
 */
object SpellAttacks {
    fun tier(characterLevel: Int): Int = 1 + listOf(5, 11, 17).count { characterLevel >= it }

    private data class Cantrip(val key: String, val die: Int, val type: String, val range: String)

    private val RANGED = listOf(
        Cantrip("fire_bolt", 10, "fire", "120 ft"),
        Cantrip("ray_of_frost", 8, "cold", "60 ft"),
        Cantrip("chill_touch", 8, "necrotic", "120 ft"),
        Cantrip("shocking_grasp", 8, "lightning", "touch"),
        Cantrip("produce_flame", 8, "fire", "30 ft"),
        Cantrip("thorn_whip", 6, "piercing", "30 ft")
    )

    /** Spell attack bonus of the best caster class whose list holds [spell] (fallback: best overall). */
    private fun blockFor(spell: Spell?, casting: List<SpellcastingBlock>): SpellcastingBlock? =
        casting.filter { spell == null || it.classKey in spell.classes }.maxByOrNull { it.attackBonus }
            ?: casting.maxByOrNull { it.attackBonus }

    fun lines(
        b: CharacterBuild, mods: Map<Ability, Int>, prof: Int,
        casting: List<SpellcastingBlock>, weaponLines: List<AttackLine>
    ): List<AttackLine> {
        val n = tier(b.totalLevel)
        val out = ArrayList<AttackLine>()
        if ("eldritch_blast" in b.spellKeys) {
            val block = blockFor(Spells.byKey["eldritch_blast"], casting)
            val atk = block?.attackBonus ?: (prof + mods.getValue(Ability.CHA))
            val agonizing = "agonizing_blast" in b.invocationKeys
            val flat = if (agonizing) mods.getValue(Ability.CHA) else 0
            val dmg = "1d10" + if (flat != 0) sign(flat) else ""
            val notes = listOfNotNull(
                if (agonizing) "Agonizing" else null,
                if ("repelling_blast" in b.invocationKeys) "Repelling 10 ft" else null,
                if ("grasp_of_hadar" in b.invocationKeys) "Grasp 10 ft" else null,
                if ("lance_of_lethargy" in b.invocationKeys) "Lance −10 ft speed" else null,
                "each beam rolls separately"
            ).joinToString(", ")
            out += AttackLine(
                name = "Eldritch Blast × $n", attackBonus = atk, damage = dmg, damageType = "force",
                averageDamage = n * (5.5 + flat), properties = notes,
                range = if ("eldritch_spear" in b.invocationKeys) "300 ft" else "120 ft"
            )
        }
        for (c in RANGED) {
            if (c.key !in b.spellKeys) continue
            val spell = Spells.byKey[c.key]
            val atk = blockFor(spell, casting)?.attackBonus ?: continue
            out += AttackLine(spell?.name ?: c.key, atk, "${n}d${c.die}", c.type, n * (c.die + 1) / 2.0, "spell attack", c.range)
        }
        // Blade cantrips: a normal melee weapon attack with bonus damage from the spell.
        val meleeEntry = b.attacks.firstOrNull { a -> SrdEquipment.weaponsByName[a.weaponName]?.let { !it.ranged } == true }
        val melee = meleeEntry?.let { a ->
            val name = a.customName.ifBlank { a.weaponName }
            weaponLines.firstOrNull { it.name.startsWith(name) }
        }
        if (melee != null) {
            if ("booming_blade" in b.spellKeys) {
                val extra = n - 1
                val dmg = melee.damage + if (extra > 0) " + ${extra}d8" else ""
                out += AttackLine(
                    "Booming Blade (${melee.name})", melee.attackBonus, dmg, melee.damageType + if (extra > 0) " + thunder" else "",
                    melee.averageDamage + extra * 4.5, "if the target moves: +${n}d8 thunder", melee.range
                )
            }
            if ("green_flame_blade" in b.spellKeys) {
                val extra = n - 1
                val spellMod = blockFor(Spells.byKey["green_flame_blade"], casting)?.let { mods.getValue(it.ability) } ?: 0
                val dmg = melee.damage + if (extra > 0) " + ${extra}d8" else ""
                val leap = (if (extra > 0) "${extra}d8" else "") + if (spellMod != 0) sign(spellMod) else ""
                out += AttackLine(
                    "Green-Flame Blade (${melee.name})", melee.attackBonus, dmg, melee.damageType + if (extra > 0) " + fire" else "",
                    melee.averageDamage + extra * 4.5, "second target within 5 ft: ${leap.ifBlank { "0" }} fire", melee.range
                )
            }
        }
        return out
    }

    private fun sign(v: Int) = if (v >= 0) "+$v" else "$v"
}

object ClassResources {
    fun of(b: CharacterBuild, mods: Map<Ability, Int>, prof: Int): List<Pair<String, String>> {
        val out = ArrayList<Pair<String, String>>()
        val cha = mods.getValue(Ability.CHA)
        val sub = b.classes.mapNotNull { it.subclassKey }.toSet()

        val sorc = b.levelOf("sorcerer")
        if (sorc >= 2) out += "Sorcery points" to "$sorc (long rest)"
        if (sorc >= 2) out += "Flexible casting" to "slot level s costs 2, 3, 5, 6, 7 points for s = 1…5; a slot gives back s points"
        if ("metamagic_adept" in b.featKeys) out += "Metamagic Adept points" to "2 (long rest)"

        val pal = b.levelOf("paladin")
        if (pal >= 1) out += "Lay on Hands pool" to "${5 * pal} hit points (long rest)"
        if (pal >= 2) out += "Divine Smite" to "(1 + slot level)d8 radiant, max. 5d8; +1d8 against undead and fiends" +
            if (pal >= 11) "; Improved Divine Smite adds 1d8 to every melee hit" else ""
        if (pal >= 3) out += "Channel Divinity (paladin)" to "1 (short rest)"
        if (pal >= 6) out += "Aura of Protection" to "+${maxOf(1, cha)} to saves within ${if (pal >= 18) 30 else 10} ft"

        val wl = b.levelOf("warlock")
        if ("hexblade" in sub) out += "Hexblade's Curse" to "1 (short rest): +$prof damage per damage roll, crit on 19–20"
        if (wl >= 11) out += "Mystic Arcanum" to listOf(11 to 6, 13 to 7, 15 to 8, 17 to 9).filter { wl >= it.first }.joinToString(", ") { "${it.second}th" } + " (1 each per long rest)"

        val wiz = b.levelOf("wizard")
        if (wiz >= 1) out += "Arcane Recovery" to "slot levels up to ${(wiz + 1) / 2} (once per day, short rest)"
        if ("evocation" in sub && wiz >= 14) out += "Overchannel" to "maximise a spell of 5th level or lower; repeat use costs 2d12 necrotic per spell level, +1d12 each time"
        if ("divination" in sub && wiz >= 2) out += "Portent dice" to if (wiz >= 14) "3" else "2"

        val cleric = b.levelOf("cleric")
        if (cleric >= 2) out += "Channel Divinity (cleric)" to "${if (cleric >= 18) 3 else if (cleric >= 6) 2 else 1} (short rest)"
        if ("tempest" in sub) out += "Wrath of the Storm" to "${maxOf(1, mods.getValue(Ability.WIS))} (long rest)"

        val ftr = b.levelOf("fighter")
        if (ftr >= 1) out += "Second Wind" to "1d10 + $ftr (short rest)"
        if (ftr >= 2) out += "Action Surge" to "${if (ftr >= 17) 2 else 1} (short rest)"
        if (ftr >= 9) out += "Indomitable" to "${if (ftr >= 17) 3 else if (ftr >= 13) 2 else 1} (long rest)"

        val barb = b.levelOf("barbarian")
        if (barb >= 1) {
            val rages = when { barb >= 20 -> "unlimited"; barb >= 17 -> "6"; barb >= 12 -> "5"; barb >= 6 -> "4"; barb >= 3 -> "3"; else -> "2" }
            out += "Rage" to "$rages, +${if (barb >= 16) 4 else if (barb >= 9) 3 else 2} damage"
        }
        val monk = b.levelOf("monk")
        if (monk >= 2) out += "Ki points" to "$monk (short rest)"
        val bard = b.levelOf("bard")
        if (bard >= 1) out += "Bardic Inspiration" to "${maxOf(1, cha)} × d${when { bard >= 15 -> 12; bard >= 10 -> 10; bard >= 5 -> 8; else -> 6 }}"
        if (b.levelOf("druid") >= 2) out += "Wild Shape" to "2 (short rest)"
        if ("lucky" in b.featKeys) out += "Luck points" to "$prof (long rest)"
        return out
    }
}

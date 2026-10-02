package app.maximus.dnd.domain

import kotlin.math.min
import kotlin.math.pow

/** One step of a level-by-level plan: the class taken at that character level. */
data class BuildStep(val classKey: String, val note: String = "")

/** What an ability score improvement slot is spent on: a feat, ability increases, or both (half feats). */
data class AsiChoice(val featKey: String? = null, val bonuses: Map<Ability, Int> = emptyMap()) {
    val label: String
        get() = listOfNotNull(
            featKey?.let { SrdFeats.byKey[it]?.name ?: it },
            bonuses.takeIf { it.isNotEmpty() }?.entries?.joinToString(", ") { "${it.key.short} +${it.value}" }
        ).joinToString(" · ")
}

data class GuideSection(val title: String, val text: String)

data class BuildGuide(
    val key: String,
    val name: String,
    val tagline: String,
    val concept: String,
    val raceKey: String,
    val backgroundKey: String,
    val baseScores: Map<Ability, Int>,
    val freeBonuses: Map<Ability, Int>,
    /** Exactly 20 entries; entry i is the class taken at character level i + 1. */
    val steps: List<BuildStep>,
    val subclasses: Map<String, String>,
    val asiPlan: List<AsiChoice>,
    /** Feats granted outside the ASI slots (variant human). */
    val bonusFeats: List<String> = emptyList(),
    /** (character level from which the spell is taken, spell key). */
    val spellPlan: List<Pair<Int, String>>,
    val invocationPlan: List<String> = emptyList(),
    val metamagicPlan: List<String> = emptyList(),
    val pactBoon: String? = null,
    val fightingStyles: Set<String> = emptySet(),
    val armorByLevel: List<Pair<Int, String>>,
    val shield: Boolean,
    val weapon: String,
    val skills: Set<Skill>,
    val sections: List<GuideSection>
)

/**
 * Expected-damage mathematics for d20 attack rolls (2014 rules).
 *
 * A natural 1 always misses and a natural roll r ≥ c (crit threshold, normally 20) always hits as a
 * critical. With attack bonus a against armour class AC the hit chance is
 *   p = clamp((21 + a − AC)/20, (21 − c)/20, 0.95),  and the crit chance is  κ = (21 − c)/20.
 * A critical doubles the dice, not the flat bonus, so with mean dice damage D and flat bonus F
 *   E[damage] = p·(D + F) + κ·D.
 * Advantage replaces p by 1 − (1 − p)² and κ by 1 − (1 − κ)².
 */
object DprMath {
    fun critChance(critMin: Int = 20, advantage: Boolean = false): Double {
        val k = (21 - critMin).coerceIn(1, 20) / 20.0
        return if (advantage) 1 - (1 - k).pow(2) else k
    }

    fun hitChance(attackBonus: Int, ac: Int, critMin: Int = 20, advantage: Boolean = false): Double {
        val k = (21 - critMin).coerceIn(1, 20) / 20.0
        val p = ((21 + attackBonus - ac) / 20.0).coerceIn(k, 0.95)
        return if (advantage) 1 - (1 - p).pow(2) else p
    }

    fun attack(attackBonus: Int, ac: Int, diceMean: Double, flat: Double, critMin: Int = 20, advantage: Boolean = false): Double {
        val p = hitChance(attackBonus, ac, critMin, advantage)
        val k = critChance(critMin, advantage)
        return p * (diceMean + flat) + k * diceMean
    }

    /** Probability that at least one of n independent attacks hits. */
    fun anyHit(p: Double, n: Int): Double = 1 - (1 - p).pow(n)

    /** Mean of k dice with s sides. */
    fun dice(k: Int, s: Int): Double = k * (s + 1) / 2.0
}

data class DprPoint(val level: Int, val ac: Int, val sustained: Double, val nova: Double, val sustainedNote: String, val novaNote: String)

object BuildGuides {
    private fun steps(vararg pairs: Pair<Int, String>): List<BuildStep> {
        val out = ArrayList<BuildStep>()
        for ((count, key) in pairs) repeat(count) { out += BuildStep(key) }
        require(out.size == 20)
        return out
    }

    private fun scores(str: Int, dex: Int, con: Int, int: Int, wis: Int, cha: Int) = mapOf(
        Ability.STR to str, Ability.DEX to dex, Ability.CON to con, Ability.INT to int, Ability.WIS to wis, Ability.CHA to cha
    )

    val nuclearWizard = BuildGuide(
        key = "nuclear_wizard",
        name = "Nuclear Wizard",
        tagline = "Evocation Wizard 17 / Hexblade 1 / Fighter 2",
        concept = "Turn every magic missile dart into its own damage roll and stack flat bonuses on each of them: " +
            "Hexblade's Curse adds the proficiency bonus, Empowered Evocation adds INT, Action Surge casts the whole thing twice, " +
            "and Overchannel maximises it. Scorching ray profits the same way (one roll per ray, crits on 19–20 against the cursed target).",
        raceKey = "human_variant", backgroundKey = "sage",
        baseScores = scores(str = 8, dex = 13, con = 14, int = 15, wis = 9, cha = 13),
        freeBonuses = mapOf(Ability.INT to 1, Ability.CON to 1),
        steps = steps(5 to "wizard", 1 to "warlock", 2 to "fighter", 12 to "wizard"),
        subclasses = mapOf("wizard" to "evocation", "warlock" to "hexblade", "fighter" to "champion"),
        bonusFeats = listOf("resilient_con"),
        asiPlan = listOf(
            AsiChoice(bonuses = mapOf(Ability.INT to 2)),
            AsiChoice(bonuses = mapOf(Ability.INT to 2)),
            AsiChoice("lucky"),
            AsiChoice("alert")
        ),
        spellPlan = listOf(
            1 to "fire_bolt", 1 to "toll_the_dead", 1 to "mind_sliver", 1 to "magic_missile", 1 to "shield", 1 to "absorb_elements",
            1 to "silvery_barbs", 1 to "find_familiar", 1 to "detect_magic", 3 to "misty_step", 3 to "scorching_ray", 4 to "mirror_image",
            5 to "fireball", 5 to "counterspell", 5 to "lightning_bolt", 9 to "thunder_step", 10 to "greater_invisibility",
            10 to "storm_sphere", 11 to "polymorph", 12 to "wall_of_force", 12 to "synaptic_static", 13 to "steel_wind_strike",
            14 to "chain_lightning", 14 to "disintegrate", 16 to "delayed_blast_fireball", 16 to "crown_of_stars", 17 to "teleport",
            18 to "abi_dalzims_horrid_wilting", 20 to "meteor_swarm", 20 to "wish"
        ),
        armorByLevel = listOf(1 to "No armour", 6 to "Breastplate"),
        shield = false, weapon = "Quarterstaff",
        skills = setOf(Skill.ARCANA, Skill.INVESTIGATION, Skill.PERCEPTION),
        sections = listOf(
            GuideSection("Level order", "Wizard 1–5 first (fireball and counterspell on time), then Hexblade 1 (curse, medium armour, shields) and Fighter 2 (Action Surge, Second Wind). Every later level is Wizard: Empowered Evocation at Wizard 10 (character level 13), Overchannel at Wizard 14 (character 17), 9th-level slots at Wizard 17 (character 20)."),
            GuideSection("Multiclass requirements", "INT 13 for the wizard, CHA 13 for the warlock and STR or DEX 13 for the fighter. Point buy 8/13/14/15/9/13 with variant human +1 INT, +1 CON meets all of them."),
            GuideSection("The nuke, by the numbers", "Magic missile at slot level s fires s + 2 darts, each 1d4 + 1 force (mean 3.5), all hitting automatically. With Hexblade's Curse (+PB per damage roll) and Empowered Evocation (+INT, applied to every dart because the darts share one roll — the generous reading, ask your DM) each dart deals 2.5 + 1 + PB + INT on average. At character level 20 (PB 6, INT 20): a 9th-level slot gives 11 darts × 14.5 = 159.5; Action Surge with an 8th-level slot adds 10 × 14.5 = 145, i.e. ≈ 304 force damage that never misses. Overchannel instead maximises a 5th-level casting: 7 × (5 + 6 + 5) = 112 guaranteed."),
            GuideSection("Rotation", "Round 1: bonus action Hexblade's Curse on the boss, action magic missile at the highest slot, Action Surge for a second one. Without a target worth the nuke: fire bolt (+PB from the curse, +INT from Wizard 10, crit on 19–20)."),
            GuideSection("Weak points", "The shield spell stops magic missile completely, and the curse rulings vary between tables. Wizard progression is delayed by three levels; that is the price of the nuke. Concentration is protected by Resilient (CON) and Lucky.")
        )
    )

    val sorcadin = BuildGuide(
        key = "sorcadin",
        name = "Sorcadin",
        tagline = "Paladin 6 / Divine Soul Sorcerer 14",
        concept = "The paladin brings Extra Attack, Divine Smite and the Aura of Protection; the sorcerer turns sorcery points into extra smite slots, " +
            "adds quickened booming blade for a third attack per turn and pushes the slot table to 9th level. Every hit can become a smite chosen after the hit, so crits are devastating.",
        raceKey = "half_elf", backgroundKey = "soldier",
        baseScores = scores(str = 15, dex = 8, con = 13, int = 8, wis = 10, cha = 15),
        freeBonuses = mapOf(Ability.STR to 1, Ability.CON to 1),
        steps = steps(6 to "paladin", 14 to "sorcerer"),
        subclasses = mapOf("paladin" to "vengeance", "sorcerer" to "divine_soul"),
        asiPlan = listOf(
            AsiChoice(bonuses = mapOf(Ability.STR to 2)),
            AsiChoice("war_caster"),
            AsiChoice(bonuses = mapOf(Ability.STR to 2)),
            AsiChoice(bonuses = mapOf(Ability.CHA to 2))
        ),
        spellPlan = listOf(
            2 to "bless", 2 to "shield_of_faith", 2 to "searing_smite", 3 to "thunderous_smite", 5 to "find_steed", 5 to "aid",
            7 to "booming_blade", 7 to "green_flame_blade", 7 to "mind_sliver", 7 to "light", 7 to "shield", 7 to "absorb_elements",
            8 to "silvery_barbs", 9 to "misty_step", 10 to "hold_person", 11 to "haste", 11 to "counterspell", 12 to "spirit_guardians",
            13 to "greater_invisibility", 14 to "banishment", 15 to "synaptic_static", 16 to "wall_of_force", 17 to "heal",
            18 to "disintegrate", 19 to "plane_shift", 20 to "teleport"
        ),
        metamagicPlan = listOf("quickened", "twinned", "heightened"),
        fightingStyles = setOf("dueling"),
        armorByLevel = listOf(1 to "Chain mail", 6 to "Plate"),
        shield = true, weapon = "Longsword",
        skills = setOf(Skill.ATHLETICS, Skill.PERSUASION, Skill.INSIGHT),
        sections = listOf(
            GuideSection("Level order", "Paladin 1–6: heavy armour from level 1, Divine Smite at 2, Oath at 3, Extra Attack at 5, Aura of Protection (+CHA to every save of you and nearby allies) at 6. Then Sorcerer 1–14: Divine Soul at 1, Font of Magic at 2, Quickened Spell at 3."),
            GuideSection("Slots and smites", "Multiclass caster level = sorcerer level + ⌊paladin level / 2⌋, e.g. 14 + 3 = 17 at character level 20 → slots up to 9th level. A smite costs one slot: (1 + s)d8 radiant for slot level s, capped at 5d8 (s = 4); higher slots add nothing, so convert them into sorcery points or spend them on spells. Creating a 1st-level slot costs 2 sorcery points (≈ 2d8 = 9 damage per smite)."),
            GuideSection("Turn structure", "Action: Attack (two longsword hits, Dueling +2). Bonus action: Quickened booming blade (2 sorcery points) for a third attack with extra thunder. Smite after you see a hit — always on a critical, because the smite dice are doubled too. Vow of Enmity gives advantage, which almost doubles the crit chance: 1 − 0.95² = 9.75 %."),
            GuideSection("Requirements and saves", "STR 13 and CHA 13 for paladin, CHA 13 for sorcerer. Starting as paladin keeps heavy armour; the price is no CON save proficiency, which War Caster partly covers."),
            GuideSection("Weak points", "Paladin features stop at 6 (no Improved Divine Smite) and the sorcerer gets few spells known; choose them for action economy (haste, shield, misty step, counterspell).")
        )
    )

    val sorlock = BuildGuide(
        key = "sorlock",
        name = "Sorlock",
        tagline = "Shadow Sorcerer 18 / Hexblade 2",
        concept = "Two levels of Hexblade warlock give Agonizing Blast (+CHA per beam), Devil's Sight, armour, shields and a short-rest curse; " +
            "the sorcerer adds Quickened Spell, so you fire eldritch blast twice per turn: 8 beams at level 17, each with CHA added. " +
            "Shadow Magic's darkness plus Devil's Sight gives you advantage while enemies have disadvantage.",
        raceKey = "half_elf", backgroundKey = "charlatan",
        baseScores = scores(str = 8, dex = 14, con = 15, int = 8, wis = 10, cha = 15),
        freeBonuses = mapOf(Ability.CON to 1, Ability.DEX to 1),
        steps = steps(1 to "sorcerer", 2 to "warlock", 17 to "sorcerer"),
        subclasses = mapOf("sorcerer" to "shadow", "warlock" to "hexblade"),
        asiPlan = listOf(
            AsiChoice(bonuses = mapOf(Ability.CHA to 2)),
            AsiChoice(bonuses = mapOf(Ability.CHA to 1, Ability.DEX to 1)),
            AsiChoice("war_caster"),
            AsiChoice("lucky")
        ),
        spellPlan = listOf(
            1 to "fire_bolt", 1 to "mind_sliver", 1 to "light", 1 to "prestidigitation", 1 to "shield", 1 to "chaos_bolt",
            2 to "eldritch_blast", 2 to "hex", 3 to "armor_of_agathys", 4 to "absorb_elements", 5 to "darkness", 5 to "misty_step",
            6 to "mirror_image", 7 to "haste", 7 to "counterspell", 8 to "fireball", 9 to "greater_invisibility", 10 to "polymorph",
            11 to "synaptic_static", 12 to "wall_of_force", 13 to "chain_lightning", 14 to "mental_prison", 15 to "crown_of_stars",
            16 to "teleport", 17 to "abi_dalzims_horrid_wilting", 19 to "psychic_scream", 20 to "wish"
        ),
        invocationPlan = listOf("agonizing_blast", "devils_sight"),
        metamagicPlan = listOf("quickened", "twinned", "heightened", "subtle"),
        armorByLevel = listOf(1 to "No armour", 2 to "Breastplate"),
        shield = true, weapon = "Dagger",
        skills = setOf(Skill.DECEPTION, Skill.PERSUASION, Skill.ARCANA),
        sections = listOf(
            GuideSection("Level order", "Sorcerer 1 first for CON save proficiency (concentration), then Hexblade 1–2 (curse, medium armour, shields, Agonizing Blast and Devil's Sight), then Sorcerer to 18. Quickened Spell arrives at character level 5 together with the second eldritch blast beam."),
            GuideSection("Beam arithmetic", "Eldritch blast fires n beams, n = 1 + [L ≥ 5] + [L ≥ 11] + [L ≥ 17] by character level. Each beam is its own attack: 1d10 + CHA force, plus PB under Hexblade's Curse (crit on 19–20) or +1d6 under hex. Quickened eldritch blast (2 sorcery points) doubles n; at L 17 with CHA +5 and PB +6: 8 beams × (5.5 + 5 + 6) × p, about 100 damage against AC 19."),
            GuideSection("Darkness combo", "Cast darkness on yourself (Shadow Magic lets you do it with sorcery points and see through it, Devil's Sight works for any darkness): attackers cannot see you, you see them. In 5e terms you gain advantage, they get disadvantage. Communicate with your party — they are blind too."),
            GuideSection("Coffeelock note", "Pact slots return on a short rest and can be turned into sorcery points (and those into permanent sorcery slots). The 'coffeelock' exploit that skips long rests is table-dependent; the plan here does not rely on it."),
            GuideSection("Weak points", "Only two warlock levels: one pact slot level 1 → after Warlock 2 two slots of 1st level. Spell slots, sorcery points and quickened blasts compete; plan short rests with the party.")
        )
    )

    val all: List<BuildGuide> = listOf(nuclearWizard, sorcadin, sorlock)
    val byKey = all.associateBy { it.key }

    /** Class levels at character level [level], in the order the classes were first taken. */
    fun classesAt(guide: BuildGuide, level: Int): List<ClassLevel> {
        val taken = guide.steps.take(level.coerceIn(1, 20))
        val order = taken.map { it.classKey }.distinct()
        return order.map { key ->
            val lv = taken.count { it.classKey == key }
            val cls = SrdClasses.byKey[key]
            val sub = guide.subclasses[key]?.takeIf { cls != null && lv >= cls.subclassLevel }
            ClassLevel(key, lv, sub)
        }
    }

    /** A complete character build following [guide] up to character [level]. */
    fun buildAt(guide: BuildGuide, level: Int): CharacterBuild {
        val L = level.coerceIn(1, 20)
        val classes = classesAt(guide, L)
        val slots = CharacterBuilder.asiCount(classes)
        val asi = HashMap<Ability, Int>()
        val feats = LinkedHashSet<String>(guide.bonusFeats)
        guide.asiPlan.take(slots).forEach { c ->
            c.featKey?.let { feats += it }
            c.bonuses.forEach { (a, v) -> asi.merge(a, v, Int::plus) }
        }
        // Half feats from the variant-human bonus feat.
        guide.bonusFeats.forEach { k -> SrdFeats.byKey[k]?.let { f -> if (f.abilityAmount == 1 && f.abilityChoice.size == 1) asi.merge(f.abilityChoice[0], 1, Int::plus) } }
        val wl = classes.firstOrNull { it.classKey == "warlock" }?.level ?: 0
        val sorc = classes.firstOrNull { it.classKey == "sorcerer" }?.level ?: 0
        val invocations = guide.invocationPlan.filter { (EldritchInvocations.byKey[it]?.minLevel ?: 99) <= wl }.take(EldritchInvocations.known(wl))
        val metamagic = guide.metamagicPlan.take(MetamagicOptions.known(sorc))
        val spells = guide.spellPlan.filter { it.first <= L }.map { it.second }.filter { it in Spells.byKey }
        val armor = guide.armorByLevel.lastOrNull { it.first <= L }?.second ?: "No armour"
        val paladinStyle = classes.any { it.classKey == "paladin" && it.level >= 2 } || classes.any { it.classKey == "fighter" }
        return CharacterBuild(
            name = guide.name,
            raceKey = guide.raceKey,
            classes = classes,
            backgroundKey = guide.backgroundKey,
            baseScores = guide.baseScores,
            scoreMethod = ScoreMethod.POINT_BUY,
            freeAbilityBonuses = guide.freeBonuses,
            asiBonuses = asi,
            chosenSkills = guide.skills,
            armorName = armor,
            shield = guide.shield && (armor != "No armour" || classes.any { it.subclassKey == "hexblade" }),
            attacks = listOf(AttackEntry(weaponName = guide.weapon)),
            featKeys = feats,
            invocationKeys = invocations.toSet(),
            metamagicKeys = metamagic.toSet(),
            pactBoon = guide.pactBoon?.takeIf { wl >= 3 },
            fightingStyles = if (paladinStyle) guide.fightingStyles else emptySet(),
            spellKeys = spells.toSet(),
            preparedKeys = spells.filter { (Spells.byKey[it]?.level ?: 0) > 0 }.toSet(),
            notes = "Created from the ${guide.name} build guide (${guide.tagline})."
        )
    }

    /** Typical armour class of a monster whose CR equals the character level (DMG table). */
    fun typicalAc(level: Int): Int = ChallengeRating.row(level.toDouble()).ac

    /** Dice mean of the smite for slot level s: (1 + s)d8, capped at 5d8. */
    private fun smiteDice(slot: Int): Double = DprMath.dice(min(1 + slot, 5), 8)

    /** Highest spell slot levels available in a round, in descending order (one entry per slot). */
    private fun slotList(sheet: CharacterSheet): List<Int> {
        val out = ArrayList<Int>()
        for (lvl in 9 downTo 1) repeat(sheet.spellSlots[lvl - 1]) { out += lvl }
        repeat(sheet.pactSlots.first) { out += sheet.pactSlots.second }
        return out.sortedDescending()
    }

    fun dpr(guide: BuildGuide, level: Int, acOverride: Int? = null): DprPoint {
        val b = buildAt(guide, level)
        val s = CharacterBuilder.build(b)
        val ac = acOverride ?: typicalAc(level)
        val prof = s.proficiency
        val n = SpellAttacks.tier(level)
        val curse = b.classes.any { it.subclassKey == "hexblade" }
        val critMin = if (curse) 19 else 20
        val cursePb = if (curse) prof.toDouble() else 0.0
        return when (guide.key) {
            "nuclear_wizard" -> {
                val int = s.modifiers.getValue(Ability.INT)
                val wiz = b.levelOf("wizard")
                val atk = prof + int
                val empowered = if (wiz >= 10) int.toDouble() else 0.0
                val sustained = DprMath.attack(atk, ac, DprMath.dice(n, 10), cursePb + empowered, critMin)
                val slots = slotList(s)
                fun mm(slot: Int, maximised: Boolean): Double = (slot + 2) * ((if (maximised) 4.0 else 2.5) + 1 + cursePb + empowered)
                val first = slots.firstOrNull() ?: 0
                val overchannel = if (wiz >= 14) mm(5, true) else 0.0
                val best = maxOf(if (first > 0) mm(first, false) else 0.0, overchannel)
                val surge = b.levelOf("fighter") >= 2
                val second = if (surge) slots.getOrNull(1)?.let { mm(it, false) } ?: 0.0 else 0.0
                val nova = if (first == 0) sustained else best + second
                DprPoint(
                    level, ac, sustained, nova,
                    "Fire bolt ${n}d10" + (if (curse) " + PB (curse, crit 19–20)" else "") + (if (wiz >= 10) " + INT (Empowered Evocation)" else ""),
                    if (first == 0) "No slots yet" else "Magic missile at level $first" + (if (overchannel >= best && overchannel > 0) " (overchannelled 5th)" else "") +
                        (if (surge && second > 0) " + Action Surge magic missile at level ${slots[1]}" else "") +
                        (if (curse || wiz >= 10) " with per-dart bonuses" else "")
                )
            }
            "sorcadin" -> {
                val weapon = s.attacks.firstOrNull()
                val atk = weapon?.attackBonus ?: prof
                // averageDamage = dice mean (1d8 → 4.5) + flat bonus (STR, Dueling, magic).
                val flat = (weapon?.averageDamage ?: 4.5) - 4.5
                val pal = b.levelOf("paladin")
                val sorc = b.levelOf("sorcerer")
                val attacks = if (pal >= 5) 2 else 1
                val p = DprMath.hitChance(atk, ac)
                val k = DprMath.critChance()
                val perAttack = DprMath.attack(atk, ac, 4.5, flat)
                val slots = slotList(s)
                // Sustained: one 1st-level smite per round on the first hit (a crit smite doubles its dice).
                val smite1 = if (pal >= 2 && slots.isNotEmpty()) DprMath.anyHit(p, attacks) * smiteDice(1) + (1 - (1 - k).pow(attacks)) * smiteDice(1) else 0.0
                val sustained = attacks * perAttack + smite1
                // Nova: quickened booming blade adds an attack; every hit is smitten with the best remaining slot.
                val bb = sorc >= 3 && "quickened" in b.metamagicKeys
                val novaAttacks = attacks + if (bb) 1 else 0
                var nova = novaAttacks * perAttack + if (bb) DprMath.attack(atk, ac, DprMath.dice(n - 1, 8), 0.0) else 0.0
                if (pal >= 2) slots.take(novaAttacks).forEach { sl -> nova += p * smiteDice(sl) + k * smiteDice(sl) }
                DprPoint(
                    level, ac, sustained, nova,
                    "$attacks × longsword (Dueling)" + if (pal >= 2) " + one 1st-level smite" else "",
                    "$novaAttacks attacks" + (if (bb) " incl. quickened booming blade" else "") + if (pal >= 2) ", smite on every hit with the highest slots" else ""
                )
            }
            else -> {
                val cha = s.modifiers.getValue(Ability.CHA)
                val ebKnown = "eldritch_blast" in b.spellKeys
                val agon = if ("agonizing_blast" in b.invocationKeys) cha.toDouble() else 0.0
                val atk = s.spellcasting.maxOfOrNull { it.attackBonus } ?: (prof + cha)
                val beam = DprMath.attack(atk, ac, 5.5, agon + cursePb, critMin)
                val sustained = if (ebKnown) n * beam else DprMath.attack(atk, ac, DprMath.dice(n, 10), 0.0)
                val quick = "quickened" in b.metamagicKeys && ebKnown
                val nova = if (quick) 2 * n * beam else sustained
                DprPoint(
                    level, ac, sustained, nova,
                    if (ebKnown) "Eldritch blast $n beam(s)" + (if (agon > 0) " + CHA" else "") + if (curse) " + PB (curse)" else "" else "Fire bolt ${n}d10",
                    if (quick) "Eldritch blast + quickened eldritch blast: ${2 * n} beams" else "No quickened blast yet"
                )
            }
        }
    }

    fun series(guide: BuildGuide, acOverride: Int? = null): List<DprPoint> = (1..20).map { dpr(guide, it, acOverride) }
}

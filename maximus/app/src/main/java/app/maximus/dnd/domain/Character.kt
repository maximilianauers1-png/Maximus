package app.maximus.dnd.domain

import kotlin.math.floor
import kotlin.random.Random

enum class ScoreMethod(val label: String) {
    POINT_BUY("Point buy"), STANDARD_ARRAY("Standard array"), ROLL_4D6("Roll 4d6 drop lowest"), MANUAL("Manual")
}

enum class HpMethod(val label: String) { AVERAGE("Average"), ROLL("Rolled"), MAX("Maximum") }

enum class HomebrewKind(val label: String) {
    SPECIES("Species trait"), SUBCLASS("Subclass feature"), FEATURE("Class feature"),
    SPELL("Spell"), FEAT("Feat"), ITEM("Magic item"), RULE("House rule"), NOTE("Note")
}

data class HomebrewEntry(
    val kind: HomebrewKind = HomebrewKind.NOTE,
    val name: String = "",
    val text: String = "",
    /** Optional mechanical hooks the sheet applies: "ac+1", "hp+10", "speed+10", "skill:STEALTH", "init+2". */
    val effect: String = ""
)

data class ClassLevel(val classKey: String, val level: Int, val subclassKey: String? = null)

data class InventoryItem(
    val name: String = "",
    val quantity: Int = 1,
    val weightLb: Double = 0.0,
    val note: String = "",
    val equipped: Boolean = false,
    val attuned: Boolean = false
) {
    val totalWeight: Double get() = weightLb * quantity
}

/** A weapon the character actually fights with; the sheet turns it into an attack line. */
data class AttackEntry(
    val weaponName: String = "Longsword",
    val customName: String = "",
    val magicBonus: Int = 0,
    val twoHanded: Boolean = false,
    val extraDamage: String = "",
    val extraDamageType: String = "",
    val proficient: Boolean = true,
    val useAbility: Ability? = null
)

data class Currency(val cp: Int = 0, val sp: Int = 0, val ep: Int = 0, val gp: Int = 0, val pp: Int = 0) {
    val totalGp: Double get() = cp / 100.0 + sp / 10.0 + ep / 2.0 + gp + pp * 10.0
}

/** Everything the player chooses. All derived numbers come from [CharacterBuilder.build]. */
data class CharacterBuild(
    val name: String = "",
    val player: String = "",
    val raceKey: String = "human",
    val classes: List<ClassLevel> = listOf(ClassLevel("fighter", 1)),
    val backgroundKey: String = "soldier",
    val alignment: String = "",
    val baseScores: Map<Ability, Int> = Ability.entries.associateWith { 10 },
    val scoreMethod: ScoreMethod = ScoreMethod.POINT_BUY,
    /** Free increases from the species (variant human, half-elf, 2024 species). */
    val freeAbilityBonuses: Map<Ability, Int> = emptyMap(),
    /** Increases from ability score improvements and feats, applied on top. */
    val asiBonuses: Map<Ability, Int> = emptyMap(),
    val chosenSkills: Set<Skill> = emptySet(),
    val expertise: Set<Skill> = emptySet(),
    val saveProficiencyOverride: Set<Ability> = emptySet(),
    val armorName: String = "No armour",
    val shield: Boolean = false,
    val attacks: List<AttackEntry> = listOf(AttackEntry()),
    val inventory: List<InventoryItem> = emptyList(),
    val currency: Currency = Currency(),
    val featKeys: Set<String> = emptySet(),
    val spellKeys: Set<String> = emptySet(),
    val preparedKeys: Set<String> = emptySet(),
    val homebrew: List<HomebrewEntry> = emptyList(),
    val hpMethod: HpMethod = HpMethod.AVERAGE,
    val hpRolls: List<Int> = emptyList(),
    val bonusMaxHp: Int = 0,
    val currentHp: Int? = null,
    val tempHp: Int = 0,
    val hitDiceSpent: Int = 0,
    val deathSuccesses: Int = 0,
    val deathFailures: Int = 0,
    val inspiration: Boolean = false,
    val exhaustion: Int = 0,
    val languages: String = "",
    val toolProficiencies: String = "",
    val otherProficiencies: String = "",
    val personality: String = "",
    val ideals: String = "",
    val bonds: String = "",
    val flaws: String = "",
    val appearance: String = "",
    val backstory: String = "",
    val notes: String = ""
) {
    val totalLevel: Int get() = classes.sumOf { it.level }.coerceIn(1, Rules.MAX_LEVEL)
    val mainClassKey: String get() = classes.maxByOrNull { it.level }?.classKey ?: "fighter"
    fun levelOf(classKey: String): Int = classes.firstOrNull { it.classKey == classKey }?.level ?: 0
}

data class SkillLine(val skill: Skill, val proficient: Boolean, val expert: Boolean, val halfProficient: Boolean, val bonus: Int)

data class AttackLine(
    val name: String,
    val attackBonus: Int,
    val damage: String,
    val damageType: String,
    val averageDamage: Double,
    val properties: String,
    val range: String
)

data class SpellcastingBlock(
    val classKey: String,
    val className: String,
    val ability: Ability,
    val saveDc: Int,
    val attackBonus: Int,
    val cantripsKnown: Int,
    val spellsKnown: Int?,
    val spellsPrepared: Int?
)

data class CharacterSheet(
    val build: CharacterBuild,
    val race: Race,
    val background: Background,
    val classes: List<Pair<CharacterClass, ClassLevel>>,
    val scores: Map<Ability, Int>,
    val modifiers: Map<Ability, Int>,
    val proficiency: Int,
    val maxHp: Int,
    val hitDice: Map<Int, Int>,
    val armorClass: Int,
    val armorNote: String,
    val initiative: Int,
    val speed: Int,
    val darkvision: Int,
    val saves: Map<Ability, Int>,
    val saveProficiencies: Set<Ability>,
    val skills: List<SkillLine>,
    val passivePerception: Int,
    val passiveInvestigation: Int,
    val passiveInsight: Int,
    val attacks: List<AttackLine>,
    val spellcasting: List<SpellcastingBlock>,
    val spellSlots: IntArray,
    val pactSlots: Pair<Int, Int>,
    val carryCapacityLb: Double,
    val carriedLb: Double,
    val features: List<Pair<String, ClassFeature>>,
    val feats: List<Feat>,
    val warnings: List<String>
) {
    val classLine: String
        get() = classes.joinToString(" / ") { (c, cl) ->
            val sub = cl.subclassKey?.let { k -> SrdSubclasses.byKey[k]?.name }
            c.name + (sub?.let { " ($it)" } ?: "") + " " + cl.level
        }
}

/**
 * Multiclass spell slots: caster level is the sum of full-caster levels, half of half-caster levels
 * (rounded down) and a third of third-caster levels. Warlock pact magic is tracked separately.
 */
object SpellSlots {
    private val TABLE = arrayOf(
        intArrayOf(2, 0, 0, 0, 0, 0, 0, 0, 0), intArrayOf(3, 0, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(4, 2, 0, 0, 0, 0, 0, 0, 0), intArrayOf(4, 3, 0, 0, 0, 0, 0, 0, 0),
        intArrayOf(4, 3, 2, 0, 0, 0, 0, 0, 0), intArrayOf(4, 3, 3, 0, 0, 0, 0, 0, 0),
        intArrayOf(4, 3, 3, 1, 0, 0, 0, 0, 0), intArrayOf(4, 3, 3, 2, 0, 0, 0, 0, 0),
        intArrayOf(4, 3, 3, 3, 1, 0, 0, 0, 0), intArrayOf(4, 3, 3, 3, 2, 0, 0, 0, 0),
        intArrayOf(4, 3, 3, 3, 2, 1, 0, 0, 0), intArrayOf(4, 3, 3, 3, 2, 1, 0, 0, 0),
        intArrayOf(4, 3, 3, 3, 2, 1, 1, 0, 0), intArrayOf(4, 3, 3, 3, 2, 1, 1, 0, 0),
        intArrayOf(4, 3, 3, 3, 2, 1, 1, 1, 0), intArrayOf(4, 3, 3, 3, 2, 1, 1, 1, 0),
        intArrayOf(4, 3, 3, 3, 2, 1, 1, 1, 1), intArrayOf(4, 3, 3, 3, 3, 1, 1, 1, 1),
        intArrayOf(4, 3, 3, 3, 3, 2, 1, 1, 1), intArrayOf(4, 3, 3, 3, 3, 2, 2, 1, 1)
    )

    fun casterLevel(classes: List<ClassLevel>): Int {
        val single = classes.size == 1
        return classes.sumOf { cl ->
            val c = SrdClasses.byKey[cl.classKey] ?: return@sumOf 0
            when (c.casterDivisor) {
                1 -> cl.level
                // A lone half-caster uses its own table, which starts at level 2; in a multiclass it is level/2.
                2 -> if (single) cl.level / 2 else cl.level / 2
                3 -> cl.level / 3
                else -> 0
            }
        }
    }

    fun slots(classes: List<ClassLevel>): IntArray {
        val level = casterLevel(classes)
        return if (level <= 0) IntArray(9) else TABLE[level.coerceAtMost(20) - 1].copyOf()
    }

    fun pactSlots(warlockLevel: Int): Pair<Int, Int> {
        if (warlockLevel <= 0) return 0 to 0
        val count = when { warlockLevel >= 17 -> 4; warlockLevel >= 11 -> 3; warlockLevel >= 2 -> 2; else -> 1 }
        val level = when { warlockLevel >= 9 -> 5; warlockLevel >= 7 -> 4; warlockLevel >= 5 -> 3; warlockLevel >= 3 -> 2; else -> 1 }
        return count to level
    }
}

object CharacterBuilder {
    fun rollScores(random: Random = Random.Default): List<Int> {
        val node = DiceParser.parse("4d6kh3")
        return List(6) { DiceRoller.roll(node, random).total }
    }

    /** Effect strings on homebrew entries, e.g. "ac+1", "hp+10", "speed+10", "init+2", "skill:STEALTH". */
    private fun effectValue(build: CharacterBuild, prefix: String): Int =
        build.homebrew.sumOf { h ->
            val e = h.effect.trim().lowercase()
            if (e.startsWith(prefix)) e.removePrefix(prefix).replace("+", "").trim().toIntOrNull() ?: 0 else 0
        }

    private fun homebrewSkills(build: CharacterBuild): Set<Skill> =
        build.homebrew.mapNotNull { h ->
            val e = h.effect.trim()
            if (e.startsWith("skill:", ignoreCase = true)) {
                val n = e.substringAfter(":").trim().uppercase().replace(' ', '_')
                Skill.entries.firstOrNull { it.name == n }
            } else null
        }.toSet()

    fun build(b: CharacterBuild): CharacterSheet {
        val race = SrdRaces.byKey[b.raceKey] ?: SrdRaces.all.first()
        val background = SrdBackgrounds.byKey[b.backgroundKey] ?: SrdBackgrounds.all.first()
        val warnings = ArrayList<String>()
        val classPairs = b.classes.mapNotNull { cl -> SrdClasses.byKey[cl.classKey]?.let { it to cl } }
        val level = b.totalLevel
        val prof = Rules.proficiencyBonus(level)

        val raw = Ability.entries.associateWith { a ->
            (b.baseScores[a] ?: 10) + (race.abilityBonuses[a] ?: 0) + (b.freeAbilityBonuses[a] ?: 0) + (b.asiBonuses[a] ?: 0)
        }
        raw.forEach { (a, v) -> if (v > 20) warnings += "Score above 20: ${a.short} was capped at 20." }
        val scores = raw.mapValues { it.value.coerceIn(1, 20) }
        val mods = scores.mapValues { Rules.modifier(it.value) }
        val conMod = mods.getValue(Ability.CON)

        // ----- hit points -----
        val hitDice = HashMap<Int, Int>()
        var hp = 0
        var rollIndex = 0
        classPairs.forEachIndexed { index, (c, cl) ->
            hitDice.merge(c.hitDie.sides, cl.level, Int::plus)
            val levelsAfterFirst = if (index == 0) cl.level - 1 else cl.level
            if (index == 0) hp += c.hitDie.sides + conMod
            repeat(levelsAfterFirst) {
                val gain = when (b.hpMethod) {
                    HpMethod.MAX -> c.hitDie.sides
                    HpMethod.AVERAGE -> c.hitDie.sides / 2 + 1
                    HpMethod.ROLL -> b.hpRolls.getOrNull(rollIndex) ?: (c.hitDie.sides / 2 + 1)
                }
                rollIndex++
                hp += gain + conMod
            }
        }
        if (race.key == "dwarf_hill" || race.key == "dwarf_2024") hp += level
        if (b.classes.any { it.subclassKey == "draconic" }) hp += level
        if ("tough" in b.featKeys) hp += 2 * level
        hp += b.bonusMaxHp + effectValue(b, "hp")
        hp = maxOf(level, hp)

        // ----- armour class -----
        val armor = SrdEquipment.armorsByName[b.armorName] ?: SrdEquipment.armors.first()
        val dexMod = mods.getValue(Ability.DEX)
        val unarmoured = armor.baseAc == 10
        val barbarian = b.levelOf("barbarian") > 0
        val monk = b.levelOf("monk") > 0
        val draconic = b.classes.any { it.subclassKey == "draconic" }
        var ac: Int
        var acNote: String
        when {
            unarmoured && barbarian -> { ac = 10 + dexMod + conMod; acNote = "Unarmoured Defence (barbarian)" }
            unarmoured && monk && !b.shield -> { ac = 10 + dexMod + mods.getValue(Ability.WIS); acNote = "Unarmoured Defence (monk)" }
            unarmoured && draconic -> { ac = 13 + dexMod; acNote = "Draconic Resilience" }
            else -> {
                ac = armor.baseAc + (armor.dexCap?.let { minOf(dexMod, it) } ?: dexMod)
                acNote = armor.name
            }
        }
        if (b.shield) { ac += SrdEquipment.shield.baseAc; acNote += " + shield" }
        ac += effectValue(b, "ac")
        if (armor.strRequired > 0 && scores.getValue(Ability.STR) < armor.strRequired) {
            warnings += "${armor.name} requires Strength ${armor.strRequired}; your speed drops by 10 ft."
        }
        if ((monk || barbarian) && !unarmoured) warnings += "Unarmoured Defence only applies while wearing no armour."
        if (armor.stealthDisadvantage) acNote += ", Stealth disadvantage"

        // ----- saves, skills -----
        val saveProfs = if (b.saveProficiencyOverride.isNotEmpty()) b.saveProficiencyOverride
        else classPairs.firstOrNull()?.first?.savingThrows?.toSet().orEmpty()
        val saves = Ability.entries.associateWith { a -> mods.getValue(a) + if (a in saveProfs) prof else 0 }

        val profSkills = (race.skills + background.skills + b.chosenSkills + homebrewSkills(b)).toSet()
        val jackOfAllTrades = b.levelOf("bard") >= 2
        val remarkableAthlete = b.classes.any { it.subclassKey == "champion" } && b.levelOf("fighter") >= 7
        val skills = Skill.entries.map { s ->
            val p = s in profSkills
            val e = s in b.expertise && p
            val half = !p && (jackOfAllTrades || (remarkableAthlete && s.ability in setOf(Ability.STR, Ability.DEX, Ability.CON)))
            val bonus = mods.getValue(s.ability) + when {
                e -> 2 * prof
                p -> prof
                half -> prof / 2
                else -> 0
            }
            SkillLine(s, p, e, half, bonus)
        }
        fun passive(skill: Skill, obs: Int = 0): Int {
            val line = skills.first { it.skill == skill }
            return 10 + line.bonus + 5 * obs
        }

        // ----- attacks -----
        val attackLines = b.attacks.mapNotNull { a ->
            val w = SrdEquipment.weaponsByName[a.weaponName] ?: return@mapNotNull null
            val ability = a.useAbility ?: when {
                w.ranged -> Ability.DEX
                w.finesse && dexMod > mods.getValue(Ability.STR) -> Ability.DEX
                monk && (w.category == "Unarmed" || w.name == "Shortsword" || (w.category.startsWith("Simple") && !w.twoHanded && !w.heavy)) && dexMod > mods.getValue(Ability.STR) -> Ability.DEX
                else -> Ability.STR
            }
            val mod = mods.getValue(ability)
            val baseDice = if (a.twoHanded && w.versatile != null) w.versatile else w.damage
            val monkDie = if (monk && (w.category == "Unarmed" || w.name == "Shortsword")) monkMartialArtsDie(b.levelOf("monk")) else null
            val dice = monkDie ?: baseDice
            val flat = mod + a.magicBonus
            val damage = buildString {
                append(dice)
                if (flat != 0) append(if (flat > 0) "+$flat" else "$flat")
                if (a.extraDamage.isNotBlank()) append(" + ${a.extraDamage}")
            }
            val mean = runCatching { DiceProbability.of(DiceParser.parse(damage.substringBefore(" + "))).mean }.getOrDefault(0.0) +
                (if (a.extraDamage.isNotBlank()) runCatching { DiceProbability.of(DiceParser.parse(a.extraDamage)).mean }.getOrDefault(0.0) else 0.0)
            val props = listOfNotNull(
                if (w.finesse) "finesse" else null,
                if (w.light) "light" else null,
                if (w.heavy) "heavy" else null,
                if (w.reach) "reach" else null,
                if (w.twoHanded) "two-handed" else null,
                w.versatile?.let { "versatile ($it)" },
                w.thrown?.let { "thrown $it" }
            ).joinToString(", ")
            AttackLine(
                name = a.customName.ifBlank { w.name } + if (a.magicBonus != 0) " +${a.magicBonus}" else "",
                attackBonus = mod + a.magicBonus + if (a.proficient) prof else 0,
                damage = damage,
                damageType = w.type + if (a.extraDamageType.isNotBlank()) " + ${a.extraDamageType}" else "",
                averageDamage = mean,
                properties = props,
                range = w.range ?: w.thrown ?: if (w.reach) "10 ft" else "5 ft"
            )
        }

        // ----- spellcasting -----
        val casting = classPairs.mapNotNull { (c, cl) ->
            val ability = c.spellcasting ?: return@mapNotNull null
            val mod = mods.getValue(ability)
            SpellcastingBlock(
                classKey = c.key, className = c.name, ability = ability,
                saveDc = Rules.spellSaveDc(prof, mod), attackBonus = prof + mod,
                cantripsKnown = Spells.cantripsKnown(c.key, cl.level),
                spellsKnown = Spells.spellsKnown(c.key, cl.level),
                spellsPrepared = Spells.spellsPrepared(c.key, cl.level, mod)
            )
        }

        // ----- speed -----
        var speed = race.speed
        if (monk && unarmoured && !b.shield) speed += monkUnarmouredMovement(b.levelOf("monk"))
        if (barbarian && b.levelOf("barbarian") >= 5 && armor.baseAc < 14) speed += 10
        if (b.classes.any { it.subclassKey == "glory" } && b.levelOf("paladin") >= 7) speed += 10
        if (armor.strRequired > 0 && scores.getValue(Ability.STR) < armor.strRequired) speed -= 10
        speed += effectValue(b, "speed")

        // ----- multiclass prerequisites -----
        if (classPairs.size > 1) {
            for ((c, _) in classPairs) {
                val unmet = c.multiclassRequirement.filter { (a, v) -> scores.getValue(a) < v }
                if (unmet.isNotEmpty()) {
                    warnings += "${c.name} multiclassing needs ${unmet.entries.joinToString(" and ") { "${it.key.short} ${it.value}" }}."
                }
            }
        }
        if (level > Rules.MAX_LEVEL) warnings += "Total level is capped at 20."

        val features = classPairs.flatMap { (c, cl) ->
            val own = c.features.filter { it.level <= cl.level }.map { c.name to it }
            val sub = cl.subclassKey?.let { k ->
                SrdSubclasses.byKey[k]?.features?.filter { it.level <= cl.level }?.map { (SrdSubclasses.byKey[k]!!.name) to it }
            } ?: emptyList()
            own + sub
        }.sortedBy { it.second.level }

        val carried = b.inventory.sumOf { it.totalWeight } +
            (SrdEquipment.armorsByName[b.armorName]?.weightLb ?: 0.0) +
            (if (b.shield) SrdEquipment.shield.weightLb else 0.0) +
            b.attacks.sumOf { SrdEquipment.weaponsByName[it.weaponName]?.weightLb ?: 0.0 }

        return CharacterSheet(
            build = b, race = race, background = background, classes = classPairs,
            scores = scores, modifiers = mods, proficiency = prof,
            maxHp = hp, hitDice = hitDice, armorClass = ac, armorNote = acNote,
            initiative = dexMod + effectValue(b, "init") + if ("alert" in b.featKeys) prof else 0,
            speed = speed, darkvision = race.darkvision,
            saves = saves, saveProficiencies = saveProfs, skills = skills,
            passivePerception = passive(Skill.PERCEPTION), passiveInvestigation = passive(Skill.INVESTIGATION),
            passiveInsight = passive(Skill.INSIGHT),
            attacks = attackLines, spellcasting = casting,
            spellSlots = SpellSlots.slots(b.classes), pactSlots = SpellSlots.pactSlots(b.levelOf("warlock")),
            carryCapacityLb = 15.0 * scores.getValue(Ability.STR) * sizeFactor(race.size),
            carriedLb = carried,
            features = features, feats = b.featKeys.mapNotNull { SrdFeats.byKey[it] }, warnings = warnings
        )
    }

    private fun sizeFactor(size: Size): Double = when (size) {
        Size.TINY -> 0.5; Size.SMALL, Size.MEDIUM -> 1.0; Size.LARGE -> 2.0; Size.HUGE -> 4.0; Size.GARGANTUAN -> 8.0
    }

    fun monkMartialArtsDie(monkLevel: Int): String = when {
        monkLevel >= 17 -> "1d10"
        monkLevel >= 11 -> "1d8"
        monkLevel >= 5 -> "1d6"
        monkLevel >= 1 -> "1d4"
        else -> "1"
    }

    fun monkUnarmouredMovement(monkLevel: Int): Int = when {
        monkLevel >= 18 -> 30; monkLevel >= 14 -> 25; monkLevel >= 10 -> 20; monkLevel >= 6 -> 15; monkLevel >= 2 -> 10; else -> 0
    }

    /** Number of ability score improvements earned so far across all classes. */
    fun asiCount(classes: List<ClassLevel>): Int = classes.sumOf { cl ->
        SrdClasses.byKey[cl.classKey]?.asiLevels?.count { it <= cl.level } ?: 0
    }

    val PRIORITY: Map<String, List<Ability>> = mapOf(
        "barbarian" to listOf(Ability.STR, Ability.CON, Ability.DEX, Ability.WIS, Ability.CHA, Ability.INT),
        "bard" to listOf(Ability.CHA, Ability.DEX, Ability.CON, Ability.WIS, Ability.INT, Ability.STR),
        "cleric" to listOf(Ability.WIS, Ability.CON, Ability.STR, Ability.CHA, Ability.DEX, Ability.INT),
        "druid" to listOf(Ability.WIS, Ability.CON, Ability.DEX, Ability.INT, Ability.CHA, Ability.STR),
        "fighter" to listOf(Ability.STR, Ability.CON, Ability.DEX, Ability.WIS, Ability.CHA, Ability.INT),
        "monk" to listOf(Ability.DEX, Ability.WIS, Ability.CON, Ability.STR, Ability.INT, Ability.CHA),
        "paladin" to listOf(Ability.STR, Ability.CHA, Ability.CON, Ability.WIS, Ability.DEX, Ability.INT),
        "ranger" to listOf(Ability.DEX, Ability.WIS, Ability.CON, Ability.STR, Ability.INT, Ability.CHA),
        "rogue" to listOf(Ability.DEX, Ability.CON, Ability.INT, Ability.WIS, Ability.CHA, Ability.STR),
        "sorcerer" to listOf(Ability.CHA, Ability.CON, Ability.DEX, Ability.WIS, Ability.INT, Ability.STR),
        "warlock" to listOf(Ability.CHA, Ability.CON, Ability.DEX, Ability.WIS, Ability.INT, Ability.STR),
        "wizard" to listOf(Ability.INT, Ability.CON, Ability.DEX, Ability.WIS, Ability.CHA, Ability.STR)
    )

    fun assignScores(values: List<Int>, classKey: String): Map<Ability, Int> {
        val priority = PRIORITY[classKey] ?: Ability.entries.toList()
        return priority.zip(values.sortedDescending()).toMap()
    }

    /** Suggested starting weapon and armour for a class, used by the random builder and the "fill" button. */
    fun defaultKit(classKey: String): Triple<String, Boolean, String> = when (classKey) {
        "barbarian" -> Triple("No armour", false, "Greataxe")
        "bard" -> Triple("Leather", false, "Rapier")
        "cleric" -> Triple("Chain shirt", true, "Mace")
        "druid" -> Triple("Leather", true, "Scimitar")
        "fighter" -> Triple("Chain mail", true, "Longsword")
        "monk" -> Triple("No armour", false, "Shortsword")
        "paladin" -> Triple("Chain mail", true, "Longsword")
        "ranger" -> Triple("Studded leather", false, "Longbow")
        "rogue" -> Triple("Leather", false, "Shortsword")
        "sorcerer" -> Triple("No armour", false, "Dagger")
        "warlock" -> Triple("Leather", false, "Quarterstaff")
        else -> Triple("No armour", false, "Quarterstaff")
    }

    fun random(random: Random = Random.Default, level: Int = 1, method: ScoreMethod = ScoreMethod.STANDARD_ARRAY): CharacterBuild {
        val cls = SrdClasses.all.random(random)
        val race = SrdRaces.all.random(random)
        val background = SrdBackgrounds.all.random(random)
        val values = when (method) {
            ScoreMethod.ROLL_4D6 -> rollScores(random)
            else -> Rules.STANDARD_ARRAY
        }
        val scores = assignScores(values, cls.key)
        val subclasses = SrdSubclasses.byClass[cls.key].orEmpty()
        val (armor, shield, weapon) = defaultKit(cls.key)
        val free = if (race.freeAbilityPoints > 0) {
            val order = PRIORITY[cls.key] ?: Ability.entries.toList()
            when (race.freeAbilityPoints) {
                3 -> mapOf(order[0] to 2, order[1] to 1)
                2 -> mapOf(order[0] to 1, order[1] to 1)
                else -> emptyMap()
            }
        } else emptyMap()
        val spellPool = Spells.forClass(cls.key)
        val cantrips = spellPool.filter { it.level == 0 }.shuffled(random).take(Spells.cantripsKnown(cls.key, level)).map { it.key }
        val leveled = spellPool.filter { it.level in 1..maxOf(1, (level + 1) / 2) }
            .shuffled(random).take((Spells.spellsKnown(cls.key, level) ?: 4).coerceAtMost(8)).map { it.key }
        return CharacterBuild(
            raceKey = race.key,
            classes = listOf(ClassLevel(cls.key, level, if (level >= cls.subclassLevel) subclasses.firstOrNull()?.key else null)),
            backgroundKey = background.key,
            baseScores = Ability.entries.associateWith { scores[it] ?: 10 },
            scoreMethod = method,
            freeAbilityBonuses = free,
            chosenSkills = cls.skillList.shuffled(random).take(cls.skillChoices).toSet(),
            armorName = armor, shield = shield,
            attacks = listOf(AttackEntry(weaponName = weapon)),
            spellKeys = (cantrips + leveled).toSet(),
            preparedKeys = leveled.toSet(),
            inventory = listOf(
                InventoryItem("Explorer's pack", 1, 59.0),
                InventoryItem("Rations (1 day)", 5, 2.0),
                InventoryItem("Potion of healing", 1, 0.5, "2d4 + 2 hit points")
            ),
            currency = Currency(gp = 10 + random.nextInt(30))
        )
    }
}

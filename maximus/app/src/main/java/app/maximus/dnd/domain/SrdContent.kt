package app.maximus.dnd.domain

/**
 * Character-building content. Everything here comes from the System Reference Documents that Wizards
 * of the Coast released under CC BY 4.0: SRD 5.1 (2016 rules) and SRD 5.2 (2024 rules). Material from
 * any other book is deliberately absent because it is not freely licensed; the homebrew editor exists
 * so the user can add it themselves.
 *
 * The whole D&D module is English-only and uses feet for distances, regardless of the app language.
 */
const val SRD_ATTRIBUTION =
    "This work includes material from the System Reference Document 5.1 and the System Reference " +
        "Document 5.2 by Wizards of the Coast LLC, available at " +
        "https://dnd.wizards.com/resources/systems-reference-document, licensed under the Creative " +
        "Commons Attribution 4.0 International License, " +
        "https://creativecommons.org/licenses/by/4.0/legalcode."

enum class ContentSource(val label: String) {
    SRD51("SRD 5.1"),
    SRD52("SRD 5.2"),
    HOMEBREW("Homebrew")
}

data class Trait(val name: String, val text: String)

/**
 * A playable species or lineage. [abilityBonuses] are fixed increases (2014 style); [freeAbilityPoints]
 * is the number of points the player distributes freely (2024 style: +2/+1 or +1/+1/+1, i.e. 3 points
 * with a per-ability cap of 2).
 */
data class Race(
    val key: String,
    val name: String,
    val group: String,
    val abilityBonuses: Map<Ability, Int> = emptyMap(),
    val freeAbilityPoints: Int = 0,
    val freeAbilityCap: Int = 2,
    val size: Size = Size.MEDIUM,
    val speed: Int = 30,
    val darkvision: Int = 0,
    val skills: List<Skill> = emptyList(),
    val extraSkillChoices: Int = 0,
    val languages: List<String> = emptyList(),
    val traits: List<Trait> = emptyList(),
    val source: ContentSource = ContentSource.SRD51
)

enum class HitDie(val sides: Int) { D6(6), D8(8), D10(10), D12(12) }

data class ClassFeature(val level: Int, val name: String, val text: String)

data class Subclass(
    val key: String,
    val classKey: String,
    val name: String,
    val source: ContentSource = ContentSource.SRD51,
    val features: List<ClassFeature> = emptyList()
)

data class CharacterClass(
    val key: String,
    val name: String,
    val hitDie: HitDie,
    val savingThrows: List<Ability>,
    val skillChoices: Int,
    val skillList: List<Skill>,
    val spellcasting: Ability? = null,
    /** Fraction of the level counting toward multiclass spell slots: 1 full, 2 half, 3 third, 0 none. */
    val casterDivisor: Int = 0,
    /** Warlock-style pact magic is tracked separately from the shared slot table. */
    val pactMagic: Boolean = false,
    val armor: String,
    val weapons: String,
    val tools: String = "—",
    val startingEquipment: String = "",
    val subclassLevel: Int = 3,
    val subclassLabel: String = "Subclass",
    /** Levels at which this class grants an Ability Score Improvement (or a feat). */
    val asiLevels: List<Int> = listOf(4, 8, 12, 16, 19),
    val multiclassRequirement: Map<Ability, Int> = emptyMap(),
    val features: List<ClassFeature> = emptyList()
)

data class Background(
    val key: String,
    val name: String,
    val skills: List<Skill>,
    val tools: String,
    val languages: Int,
    val equipment: String,
    val feature: String,
    val source: ContentSource = ContentSource.SRD51
)

data class Feat(
    val key: String,
    val name: String,
    val prerequisite: String,
    val text: String,
    val abilityChoice: List<Ability> = emptyList(),
    val abilityAmount: Int = 0,
    val source: ContentSource = ContentSource.SRD51
)

private fun t(name: String, text: String) = Trait(name, text)
private fun f(level: Int, name: String, text: String) = ClassFeature(level, name, text)

object SrdRaces {
    private val COMMON = listOf("Common")

    val all: List<Race> = listOf(
        // ---------- SRD 5.1 ----------
        Race("dwarf_hill", "Hill Dwarf", "Dwarf", mapOf(Ability.CON to 2, Ability.WIS to 1),
            size = Size.MEDIUM, speed = 25, darkvision = 60, languages = COMMON + "Dwarvish",
            traits = listOf(
                t("Darkvision 60 ft", "See in dim light within 60 ft as if bright light, and in darkness as if dim light, in shades of grey."),
                t("Dwarven Resilience", "Advantage on saving throws against poison and resistance to poison damage."),
                t("Dwarven Combat Training", "Proficiency with battleaxe, handaxe, light hammer and warhammer."),
                t("Stonecunning", "Treat your proficiency bonus as doubled for History checks about stonework."),
                t("Dwarven Toughness", "Your hit point maximum increases by 1 per character level."),
                t("Speed", "Your speed is not reduced by wearing heavy armour.")
            )),
        Race("dwarf_mountain", "Mountain Dwarf", "Dwarf", mapOf(Ability.CON to 2, Ability.STR to 2),
            speed = 25, darkvision = 60, languages = COMMON + "Dwarvish",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light, in shades of grey."),
                t("Dwarven Resilience", "Advantage against poison, resistance to poison damage."),
                t("Dwarven Armour Training", "Proficiency with light and medium armour."),
                t("Stonecunning", "Double proficiency on History checks about stonework.")
            )),
        Race("elf_high", "High Elf", "Elf", mapOf(Ability.DEX to 2, Ability.INT to 1),
            darkvision = 60, skills = listOf(Skill.PERCEPTION), languages = COMMON + listOf("Elvish", "one extra language"),
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Fey Ancestry", "Advantage against being charmed; magic cannot put you to sleep."),
                t("Trance", "Four hours of meditation replace eight hours of sleep."),
                t("Cantrip", "You know one wizard cantrip; Intelligence is your spellcasting ability for it.")
            )),
        Race("elf_wood", "Wood Elf", "Elf", mapOf(Ability.DEX to 2, Ability.WIS to 1),
            speed = 35, darkvision = 60, skills = listOf(Skill.PERCEPTION), languages = COMMON + "Elvish",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Fey Ancestry", "Advantage against charm, immune to magical sleep."),
                t("Fleet of Foot", "Your base walking speed is 35 ft."),
                t("Mask of the Wild", "You can hide even when only lightly obscured by foliage, rain, snow, mist or other natural phenomena.")
            )),
        Race("elf_drow", "Drow", "Elf", mapOf(Ability.DEX to 2, Ability.CHA to 1),
            darkvision = 120, skills = listOf(Skill.PERCEPTION), languages = COMMON + "Elvish",
            traits = listOf(
                t("Superior Darkvision 120 ft", "See 120 ft in darkness as if dim light."),
                t("Sunlight Sensitivity", "Disadvantage on attack rolls and sight-based Perception checks in direct sunlight."),
                t("Drow Magic", "Dancing lights cantrip; faerie fire at 3rd level and darkness at 5th level, once per long rest each (Charisma)."),
                t("Drow Weapon Training", "Proficiency with rapier, shortsword and hand crossbow.")
            )),
        Race("halfling_lightfoot", "Lightfoot Halfling", "Halfling", mapOf(Ability.DEX to 2, Ability.CHA to 1),
            size = Size.SMALL, speed = 25, languages = COMMON + "Halfling",
            traits = listOf(
                t("Lucky", "When you roll a 1 on an attack roll, ability check or saving throw, reroll once and use the new roll."),
                t("Brave", "Advantage on saving throws against being frightened."),
                t("Halfling Nimbleness", "Move through the space of any creature larger than you."),
                t("Naturally Stealthy", "Hide even when obscured only by a creature at least one size larger.")
            )),
        Race("halfling_stout", "Stout Halfling", "Halfling", mapOf(Ability.DEX to 2, Ability.CON to 1),
            size = Size.SMALL, speed = 25, languages = COMMON + "Halfling",
            traits = listOf(
                t("Lucky", "Reroll a natural 1 on an attack roll, ability check or saving throw."),
                t("Brave", "Advantage against being frightened."),
                t("Stout Resilience", "Advantage against poison, resistance to poison damage.")
            )),
        Race("human", "Human", "Human", Ability.entries.associateWith { 1 },
            languages = COMMON + "one extra language",
            traits = listOf(t("Versatile", "All six ability scores increase by 1."))),
        Race("human_variant", "Variant Human", "Human", freeAbilityPoints = 2, freeAbilityCap = 1,
            extraSkillChoices = 1, languages = COMMON + "one extra language",
            traits = listOf(t("Variant", "Two ability scores of your choice increase by 1, you gain one skill proficiency and one feat."))),
        Race("dragonborn", "Dragonborn", "Dragonborn", mapOf(Ability.STR to 2, Ability.CHA to 1),
            languages = COMMON + "Draconic",
            traits = listOf(
                t("Draconic Ancestry", "Choose a dragon type; it determines your breath weapon and damage resistance."),
                t("Breath Weapon", "Action: 15 ft cone or 30 ft line, 2d6 damage (increasing with level), Dexterity or Constitution save DC 8 + CON + proficiency; once per short rest."),
                t("Damage Resistance", "Resistance to the damage type of your ancestry.")
            )),
        Race("gnome_rock", "Rock Gnome", "Gnome", mapOf(Ability.INT to 2, Ability.CON to 1),
            size = Size.SMALL, speed = 25, darkvision = 60, languages = COMMON + "Gnomish",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Gnome Cunning", "Advantage on INT, WIS and CHA saving throws against magic."),
                t("Artificer's Lore", "Double proficiency on History checks about magic items, alchemical objects and technological devices."),
                t("Tinker", "Proficiency with tinker's tools; build tiny clockwork devices.")
            )),
        Race("gnome_forest", "Forest Gnome", "Gnome", mapOf(Ability.INT to 2, Ability.DEX to 1),
            size = Size.SMALL, speed = 25, darkvision = 60, languages = COMMON + "Gnomish",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Gnome Cunning", "Advantage on INT, WIS and CHA saves against magic."),
                t("Natural Illusionist", "You know the minor illusion cantrip; Intelligence is your spellcasting ability."),
                t("Speak with Small Beasts", "Communicate simple ideas with Small or smaller beasts.")
            )),
        Race("half_elf", "Half-Elf", "Half-Elf", mapOf(Ability.CHA to 2), freeAbilityPoints = 2, freeAbilityCap = 1,
            darkvision = 60, extraSkillChoices = 2, languages = COMMON + listOf("Elvish", "one extra language"),
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Fey Ancestry", "Advantage against charm, immune to magical sleep."),
                t("Skill Versatility", "Proficiency in two skills of your choice.")
            )),
        Race("half_orc", "Half-Orc", "Half-Orc", mapOf(Ability.STR to 2, Ability.CON to 1),
            darkvision = 60, skills = listOf(Skill.INTIMIDATION), languages = COMMON + "Orc",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Relentless Endurance", "When reduced to 0 hit points without being killed outright, drop to 1 instead; once per long rest."),
                t("Savage Attacks", "On a melee weapon critical hit, roll one of the weapon's damage dice an additional time.")
            )),
        Race("tiefling", "Tiefling", "Tiefling", mapOf(Ability.CHA to 2, Ability.INT to 1),
            darkvision = 60, languages = COMMON + "Infernal",
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Hellish Resistance", "Resistance to fire damage."),
                t("Infernal Legacy", "Thaumaturgy cantrip; hellish rebuke at 3rd level and darkness at 5th, once per long rest each (Charisma).")
            )),

        // ---------- SRD 5.2 (2024 rules) ----------
        Race("aasimar", "Aasimar", "Aasimar", freeAbilityPoints = 3, darkvision = 60,
            languages = COMMON + "one extra language", source = ContentSource.SRD52,
            traits = listOf(
                t("Celestial Resistance", "Resistance to necrotic and radiant damage."),
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Healing Hands", "Action: touch a creature to heal d4s equal to your proficiency bonus; once per long rest."),
                t("Light Bearer", "You know the light cantrip; Charisma is your spellcasting ability."),
                t("Celestial Revelation", "From 3rd level, as a bonus action transform for 1 minute: Heavenly Wings, Inner Radiance or Necrotic Shroud; once per long rest.")
            )),
        Race("goliath_stone", "Goliath (Stone's Endurance)", "Goliath", freeAbilityPoints = 3,
            size = Size.MEDIUM, speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Stone's Endurance: reaction to reduce damage by 1d12 + CON modifier; proficiency bonus uses per long rest."),
                t("Large Form", "From 5th level, bonus action to become Large for 10 minutes, advantage on Strength checks, speed +10 ft; once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity and for pushing, dragging or lifting.")
            )),
        Race("goliath_fire", "Goliath (Fire's Burn)", "Goliath", freeAbilityPoints = 3,
            speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Fire's Burn: when you hit with an attack, deal an extra 1d10 fire damage; proficiency bonus uses per long rest."),
                t("Large Form", "From 5th level, become Large for 10 minutes once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity.")
            )),
        Race("goliath_frost", "Goliath (Frost's Chill)", "Goliath", freeAbilityPoints = 3,
            speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Frost's Chill: extra 1d6 cold damage on a hit and reduce the target's speed by 10 ft until your next turn."),
                t("Large Form", "From 5th level, become Large for 10 minutes once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity.")
            )),
        Race("goliath_cloud", "Goliath (Cloud's Jaunt)", "Goliath", freeAbilityPoints = 3,
            speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Cloud's Jaunt: bonus action to teleport 30 ft to an unoccupied space you can see."),
                t("Large Form", "From 5th level, become Large for 10 minutes once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity.")
            )),
        Race("goliath_hill", "Goliath (Hill's Tumble)", "Goliath", freeAbilityPoints = 3,
            speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Hill's Tumble: when you hit a Large or smaller creature, you can knock it prone."),
                t("Large Form", "From 5th level, become Large for 10 minutes once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity.")
            )),
        Race("goliath_storm", "Goliath (Storm's Thunder)", "Goliath", freeAbilityPoints = 3,
            speed = 35, languages = COMMON + "Giant", source = ContentSource.SRD52,
            traits = listOf(
                t("Giant Ancestry", "Storm's Thunder: reaction when damaged to deal 1d8 thunder damage to a creature within 60 ft."),
                t("Large Form", "From 5th level, become Large for 10 minutes once per long rest."),
                t("Powerful Build", "Count as one size larger for carrying capacity.")
            )),
        Race("orc", "Orc", "Orc", freeAbilityPoints = 3, darkvision = 120,
            languages = COMMON + "Orc", source = ContentSource.SRD52,
            traits = listOf(
                t("Adrenaline Rush", "Take the Dash action as a bonus action and gain temporary hit points equal to your proficiency bonus; proficiency bonus uses per short rest."),
                t("Darkvision 120 ft", "See 120 ft in darkness as if dim light."),
                t("Relentless Endurance", "Drop to 1 hit point instead of 0 once per long rest.")
            )),
        Race("tiefling_abyssal", "Tiefling (Abyssal Legacy)", "Tiefling", freeAbilityPoints = 3,
            darkvision = 60, languages = COMMON + "one extra language", source = ContentSource.SRD52,
            traits = listOf(
                t("Fiendish Legacy", "Resistance to poison damage; poison spray cantrip, ray of sickness at 3rd level, hold person at 5th, once per long rest each."),
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Otherworldly Presence", "You know the thaumaturgy cantrip.")
            )),
        Race("tiefling_chthonic", "Tiefling (Chthonic Legacy)", "Tiefling", freeAbilityPoints = 3,
            darkvision = 60, languages = COMMON + "one extra language", source = ContentSource.SRD52,
            traits = listOf(
                t("Fiendish Legacy", "Resistance to necrotic damage; chill touch cantrip, false life at 3rd level, ray of enfeeblement at 5th."),
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Otherworldly Presence", "You know the thaumaturgy cantrip.")
            )),
        Race("tiefling_infernal", "Tiefling (Infernal Legacy)", "Tiefling", freeAbilityPoints = 3,
            darkvision = 60, languages = COMMON + "one extra language", source = ContentSource.SRD52,
            traits = listOf(
                t("Fiendish Legacy", "Resistance to fire damage; fire bolt cantrip, hellish rebuke at 3rd level, darkness at 5th."),
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Otherworldly Presence", "You know the thaumaturgy cantrip.")
            )),
        Race("elf_2024", "Elf (2024, choose lineage)", "Elf", freeAbilityPoints = 3,
            darkvision = 60, skills = listOf(Skill.PERCEPTION), languages = COMMON + "Elvish", source = ContentSource.SRD52,
            traits = listOf(
                t("Elven Lineage", "Choose Drow (120 ft darkvision, dancing lights, faerie fire, darkness), High Elf (prestidigitation, detect magic, misty step) or Wood Elf (speed 35 ft, druidcraft, longstrider, pass without trace)."),
                t("Fey Ancestry", "Advantage on saving throws to avoid or end the charmed condition."),
                t("Keen Senses", "Proficiency in Insight, Perception or Survival."),
                t("Trance", "Finish a long rest in 4 hours of meditation.")
            )),
        Race("dwarf_2024", "Dwarf (2024)", "Dwarf", freeAbilityPoints = 3,
            speed = 30, darkvision = 120, languages = COMMON + "Dwarvish", source = ContentSource.SRD52,
            traits = listOf(
                t("Darkvision 120 ft", "See 120 ft in darkness as if dim light."),
                t("Dwarven Resilience", "Resistance to poison damage and advantage on saves against poison."),
                t("Dwarven Toughness", "Hit point maximum increases by 1 per character level."),
                t("Stonecunning", "Bonus action: gain tremorsense 60 ft on stone for 10 minutes; proficiency bonus uses per long rest.")
            )),
        Race("halfling_2024", "Halfling (2024)", "Halfling", freeAbilityPoints = 3,
            size = Size.SMALL, speed = 30, languages = COMMON + "Halfling", source = ContentSource.SRD52,
            traits = listOf(
                t("Brave", "Advantage on saves against the frightened condition."),
                t("Halfling Nimbleness", "Move through the space of any creature larger than you."),
                t("Luck", "When you roll a 1 on a d20 test, reroll and use the new roll."),
                t("Naturally Stealthy", "Take the Hide action even when obscured only by a larger creature.")
            )),
        Race("human_2024", "Human (2024)", "Human", freeAbilityPoints = 3,
            extraSkillChoices = 1, languages = COMMON + "two extra languages", source = ContentSource.SRD52,
            traits = listOf(
                t("Resourceful", "Gain Heroic Inspiration after each long rest."),
                t("Skillful", "Proficiency in one skill of your choice."),
                t("Versatile", "Gain an Origin feat of your choice.")
            )),
        Race("gnome_2024", "Gnome (2024)", "Gnome", freeAbilityPoints = 3,
            size = Size.SMALL, speed = 30, darkvision = 60, languages = COMMON + "Gnomish", source = ContentSource.SRD52,
            traits = listOf(
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Gnomish Cunning", "Advantage on INT, WIS and CHA saving throws."),
                t("Gnomish Lineage", "Forest Gnome (minor illusion, speak with animals) or Rock Gnome (mending, prestidigitation, clockwork devices).")
            )),
        Race("dragonborn_2024", "Dragonborn (2024)", "Dragonborn", freeAbilityPoints = 3,
            darkvision = 60, languages = COMMON + "Draconic", source = ContentSource.SRD52,
            traits = listOf(
                t("Draconic Ancestry", "Choose a dragon; it sets your breath weapon damage type and your resistance."),
                t("Breath Weapon", "Replace one attack with a 15 ft cone or 30 ft line, 1d10 damage (rising at 5th, 11th and 17th level), Dexterity save DC 8 + CON + proficiency."),
                t("Damage Resistance", "Resistance to your ancestry's damage type."),
                t("Darkvision 60 ft", "See 60 ft in darkness as if dim light."),
                t("Draconic Flight", "From 5th level, bonus action to sprout wings for 10 minutes, flying speed equal to your speed; once per long rest.")
            ))
    )

    val byKey = all.associateBy { it.key }
    val groups: List<String> = all.map { it.group }.distinct()
}

object SrdClasses {
    private val ALL_SKILLS = Skill.entries.toList()

    val all: List<CharacterClass> = listOf(
        CharacterClass(
            "barbarian", "Barbarian", HitDie.D12, listOf(Ability.STR, Ability.CON), 2,
            listOf(Skill.ANIMAL_HANDLING, Skill.ATHLETICS, Skill.INTIMIDATION, Skill.NATURE, Skill.PERCEPTION, Skill.SURVIVAL),
            armor = "Light and medium armour, shields", weapons = "Simple and martial weapons",
            startingEquipment = "Greataxe, two handaxes, explorer's pack, four javelins",
            subclassLevel = 3, subclassLabel = "Primal Path",
            multiclassRequirement = mapOf(Ability.STR to 13),
            features = listOf(
                f(1, "Rage", "Bonus action: advantage on Strength checks and saves, bonus melee damage (+2 rising to +4), resistance to bludgeoning, piercing and slashing damage. Uses per long rest rise from 2 to 6."),
                f(1, "Unarmoured Defence", "While wearing no armour, your AC equals 10 + DEX + CON. You may use a shield."),
                f(2, "Reckless Attack", "Advantage on Strength-based melee attacks this turn; attacks against you have advantage until your next turn."),
                f(2, "Danger Sense", "Advantage on Dexterity saves against effects you can see."),
                f(5, "Extra Attack", "Attack twice when you take the Attack action."),
                f(5, "Fast Movement", "Speed increases by 10 ft while not wearing heavy armour."),
                f(7, "Feral Instinct", "Advantage on initiative; you can act surprised if you rage first."),
                f(9, "Brutal Critical", "Roll one extra damage die on a melee critical hit (two at 13th, three at 17th)."),
                f(11, "Relentless Rage", "DC 10 Constitution save to drop to 1 hit point instead of 0; DC rises by 5 per use."),
                f(15, "Persistent Rage", "Your rage ends early only if you fall unconscious or choose to end it."),
                f(18, "Indomitable Might", "If your Strength check is lower than your Strength score, use the score instead."),
                f(20, "Primal Champion", "Strength and Constitution increase by 4; their maximum becomes 24.")
            )
        ),
        CharacterClass(
            "bard", "Bard", HitDie.D8, listOf(Ability.DEX, Ability.CHA), 3, ALL_SKILLS,
            spellcasting = Ability.CHA, casterDivisor = 1,
            armor = "Light armour", weapons = "Simple weapons, hand crossbow, longsword, rapier, shortsword",
            tools = "Three musical instruments of your choice",
            startingEquipment = "Rapier, diplomat's pack, lute, leather armour, dagger",
            subclassLabel = "Bard College",
            multiclassRequirement = mapOf(Ability.CHA to 13),
            features = listOf(
                f(1, "Bardic Inspiration", "Bonus action: give a creature a d6 (d8 at 5th, d10 at 10th, d12 at 15th) to add to one ability check, attack roll or saving throw. CHA modifier uses per long rest."),
                f(2, "Jack of All Trades", "Add half your proficiency bonus to ability checks you are not proficient in."),
                f(2, "Song of Rest", "Allies who spend hit dice during a short rest regain an extra 1d6 (rising to 1d12)."),
                f(3, "Expertise", "Double your proficiency bonus for two chosen skills; two more at 10th level."),
                f(5, "Font of Inspiration", "Bardic Inspiration returns on a short rest as well."),
                f(6, "Countercharm", "Action: allies within 30 ft have advantage against being charmed or frightened."),
                f(10, "Magical Secrets", "Learn two spells from any class list; two more at 14th and 18th level."),
                f(20, "Superior Inspiration", "Regain one use of Bardic Inspiration when you roll initiative with none left.")
            )
        ),
        CharacterClass(
            "cleric", "Cleric", HitDie.D8, listOf(Ability.WIS, Ability.CHA), 2,
            listOf(Skill.HISTORY, Skill.INSIGHT, Skill.MEDICINE, Skill.PERSUASION, Skill.RELIGION),
            spellcasting = Ability.WIS, casterDivisor = 1,
            armor = "Light and medium armour, shields", weapons = "Simple weapons",
            startingEquipment = "Mace, scale mail, light crossbow with 20 bolts, shield, holy symbol, priest's pack",
            subclassLevel = 1, subclassLabel = "Divine Domain",
            multiclassRequirement = mapOf(Ability.WIS to 13),
            features = listOf(
                f(1, "Spellcasting", "Wisdom is your spellcasting ability. You prepare WIS modifier + cleric level spells from the whole cleric list each day."),
                f(2, "Channel Divinity", "Turn Undead plus a domain effect; one use per short rest, two at 6th, three at 18th."),
                f(5, "Destroy Undead", "Undead of CR 1/2 or lower are destroyed instead of turned; the threshold rises with level."),
                f(10, "Divine Intervention", "Percent chance equal to your cleric level that your deity intervenes; once per 7 days on success."),
                f(20, "Divine Intervention Improved", "Your call for intervention succeeds automatically.")
            )
        ),
        CharacterClass(
            "druid", "Druid", HitDie.D8, listOf(Ability.INT, Ability.WIS), 2,
            listOf(Skill.ARCANA, Skill.ANIMAL_HANDLING, Skill.INSIGHT, Skill.MEDICINE, Skill.NATURE, Skill.PERCEPTION, Skill.RELIGION, Skill.SURVIVAL),
            spellcasting = Ability.WIS, casterDivisor = 1,
            armor = "Light and medium armour, shields (no metal)",
            weapons = "Club, dagger, dart, javelin, mace, quarterstaff, scimitar, sickle, sling, spear",
            tools = "Herbalism kit",
            startingEquipment = "Wooden shield, scimitar, leather armour, explorer's pack, druidic focus",
            subclassLevel = 2, subclassLabel = "Druid Circle",
            multiclassRequirement = mapOf(Ability.WIS to 13),
            features = listOf(
                f(1, "Druidic", "You know the secret language of druids."),
                f(2, "Wild Shape", "Action: transform into a beast you have seen, twice per short rest, for half your druid level in hours. CR and movement limits rise at 4th and 8th level."),
                f(18, "Timeless Body", "You age one year for every ten that pass."),
                f(18, "Beast Spells", "Cast many druid spells while in Wild Shape."),
                f(20, "Archdruid", "Unlimited uses of Wild Shape; ignore verbal and somatic components of druid spells.")
            )
        ),
        CharacterClass(
            "fighter", "Fighter", HitDie.D10, listOf(Ability.STR, Ability.CON), 2,
            listOf(Skill.ACROBATICS, Skill.ANIMAL_HANDLING, Skill.ATHLETICS, Skill.HISTORY, Skill.INSIGHT, Skill.INTIMIDATION, Skill.PERCEPTION, Skill.SURVIVAL),
            armor = "All armour, shields", weapons = "Simple and martial weapons",
            startingEquipment = "Chain mail, martial weapon and shield, light crossbow with 20 bolts, dungeoneer's pack",
            subclassLabel = "Martial Archetype",
            asiLevels = listOf(4, 6, 8, 12, 14, 16, 19),
            multiclassRequirement = mapOf(Ability.STR to 13),
            features = listOf(
                f(1, "Fighting Style", "Archery +2 to ranged attacks, Defence +1 AC, Duelling +2 damage with a single one-handed weapon, Great Weapon Fighting reroll 1s and 2s, Protection impose disadvantage with a shield, Two-Weapon Fighting add your modifier to the off-hand."),
                f(1, "Second Wind", "Bonus action: regain 1d10 + fighter level hit points; once per short rest."),
                f(2, "Action Surge", "One additional action on your turn; once per short rest, twice from 17th level."),
                f(5, "Extra Attack", "Two attacks per Attack action; three at 11th, four at 20th."),
                f(9, "Indomitable", "Reroll a failed saving throw; once per long rest, more at 13th and 17th level.")
            )
        ),
        CharacterClass(
            "monk", "Monk", HitDie.D8, listOf(Ability.STR, Ability.DEX), 2,
            listOf(Skill.ACROBATICS, Skill.ATHLETICS, Skill.HISTORY, Skill.INSIGHT, Skill.RELIGION, Skill.STEALTH),
            armor = "None", weapons = "Simple weapons, shortswords",
            tools = "One artisan's tools or one musical instrument",
            startingEquipment = "Shortsword, dungeoneer's pack, 10 darts",
            subclassLabel = "Monastic Tradition",
            multiclassRequirement = mapOf(Ability.DEX to 13, Ability.WIS to 13),
            features = listOf(
                f(1, "Unarmoured Defence", "While wearing no armour and no shield, your AC equals 10 + DEX + WIS."),
                f(1, "Martial Arts", "Use DEX instead of STR for monk weapons, unarmed damage is 1d4 rising to 1d10, and you get a bonus-action unarmed strike."),
                f(2, "Ki", "Ki points equal to your monk level fuel Flurry of Blows, Patient Defence and Step of the Wind; they return on a short rest."),
                f(2, "Unarmoured Movement", "Speed +10 ft, rising to +30 ft; from 9th level you can run along walls and across liquids."),
                f(3, "Deflect Missiles", "Reaction: reduce ranged weapon damage by 1d10 + DEX + monk level; spend 1 ki to throw the missile back."),
                f(4, "Slow Fall", "Reaction: reduce falling damage by five times your monk level."),
                f(5, "Extra Attack", "Attack twice per Attack action."),
                f(5, "Stunning Strike", "Spend 1 ki on a melee hit: Constitution save or stunned until the end of your next turn."),
                f(6, "Ki-Empowered Strikes", "Your unarmed strikes count as magical."),
                f(7, "Evasion", "No damage on a successful Dexterity save, half on a failure."),
                f(7, "Stillness of Mind", "Action: end one charm or fear effect on yourself."),
                f(10, "Purity of Body", "Immune to disease and poison."),
                f(14, "Diamond Soul", "Proficiency in all saving throws; spend 1 ki to reroll a failed save."),
                f(18, "Empty Body", "Spend 4 ki for invisibility and resistance to all damage but force for 1 minute; 8 ki to cast astral projection."),
                f(20, "Perfect Self", "Regain 4 ki points when you roll initiative with none left.")
            )
        ),
        CharacterClass(
            "paladin", "Paladin", HitDie.D10, listOf(Ability.WIS, Ability.CHA), 2,
            listOf(Skill.ATHLETICS, Skill.INSIGHT, Skill.INTIMIDATION, Skill.MEDICINE, Skill.PERSUASION, Skill.RELIGION),
            spellcasting = Ability.CHA, casterDivisor = 2,
            armor = "All armour, shields", weapons = "Simple and martial weapons",
            startingEquipment = "Martial weapon and shield, five javelins, priest's pack, chain mail, holy symbol",
            subclassLabel = "Sacred Oath",
            multiclassRequirement = mapOf(Ability.STR to 13, Ability.CHA to 13),
            features = listOf(
                f(1, "Divine Sense", "Action: detect celestials, fiends and undead within 60 ft; 1 + CHA uses per long rest."),
                f(1, "Lay on Hands", "A pool of 5 × paladin level hit points you can spend to heal, or 5 points to cure one disease or poison."),
                f(2, "Fighting Style", "Choose Defence, Duelling, Great Weapon Fighting or Protection."),
                f(2, "Divine Smite", "Expend a spell slot on a melee hit for 2d8 radiant damage, +1d8 per slot level above 1st, +1d8 against undead or fiends."),
                f(3, "Divine Health", "You are immune to disease."),
                f(5, "Extra Attack", "Attack twice per Attack action."),
                f(6, "Aura of Protection", "You and allies within 10 ft add your CHA modifier to saving throws; 30 ft from 18th level."),
                f(10, "Aura of Courage", "You and allies within the aura cannot be frightened."),
                f(11, "Improved Divine Smite", "Melee weapon hits deal an extra 1d8 radiant damage."),
                f(14, "Cleansing Touch", "Action: end one spell on yourself or a willing creature; CHA uses per long rest.")
            )
        ),
        CharacterClass(
            "ranger", "Ranger", HitDie.D10, listOf(Ability.STR, Ability.DEX), 3,
            listOf(Skill.ANIMAL_HANDLING, Skill.ATHLETICS, Skill.INSIGHT, Skill.INVESTIGATION, Skill.NATURE, Skill.PERCEPTION, Skill.STEALTH, Skill.SURVIVAL),
            spellcasting = Ability.WIS, casterDivisor = 2,
            armor = "Light and medium armour, shields", weapons = "Simple and martial weapons",
            startingEquipment = "Scale mail, two shortswords, dungeoneer's pack, longbow with 20 arrows",
            subclassLabel = "Ranger Archetype",
            multiclassRequirement = mapOf(Ability.DEX to 13, Ability.WIS to 13),
            features = listOf(
                f(1, "Favoured Enemy", "Advantage on Survival checks to track and Intelligence checks to recall lore about one creature type; you learn its language."),
                f(1, "Natural Explorer", "In a favoured terrain, double your proficiency bonus for INT and WIS checks, travel at normal pace while foraging or tracking, and more."),
                f(2, "Fighting Style", "Archery, Defence, Duelling or Two-Weapon Fighting."),
                f(3, "Primeval Awareness", "Expend a spell slot to sense creature types within 1 mile (6 miles in favoured terrain)."),
                f(5, "Extra Attack", "Attack twice per Attack action."),
                f(8, "Land's Stride", "Difficult terrain costs no extra movement; advantage against magical plants that impede movement."),
                f(10, "Hide in Plain Sight", "Spend 1 minute camouflaging yourself for +10 to Stealth while you stay still."),
                f(14, "Vanish", "Hide as a bonus action; you cannot be tracked non-magically."),
                f(18, "Feral Senses", "Fight invisible creatures without disadvantage; sense invisible creatures within 30 ft."),
                f(20, "Foe Slayer", "Once per turn add your WIS modifier to an attack or damage roll against a favoured enemy.")
            )
        ),
        CharacterClass(
            "rogue", "Rogue", HitDie.D8, listOf(Ability.DEX, Ability.INT), 4,
            listOf(Skill.ACROBATICS, Skill.ATHLETICS, Skill.DECEPTION, Skill.INSIGHT, Skill.INTIMIDATION, Skill.INVESTIGATION, Skill.PERCEPTION, Skill.PERFORMANCE, Skill.PERSUASION, Skill.SLEIGHT_OF_HAND, Skill.STEALTH),
            armor = "Light armour", weapons = "Simple weapons, hand crossbow, longsword, rapier, shortsword",
            tools = "Thieves' tools",
            startingEquipment = "Rapier, shortbow with 20 arrows, burglar's pack, leather armour, two daggers, thieves' tools",
            subclassLabel = "Roguish Archetype",
            asiLevels = listOf(4, 8, 10, 12, 16, 19),
            multiclassRequirement = mapOf(Ability.DEX to 13),
            features = listOf(
                f(1, "Sneak Attack", "Once per turn, add 1d6 damage per two rogue levels when you have advantage or an ally is adjacent to the target."),
                f(1, "Expertise", "Double proficiency for two skills (or thieves' tools); two more at 6th level."),
                f(1, "Thieves' Cant", "A secret mix of dialect, jargon and code."),
                f(2, "Cunning Action", "Bonus action to Dash, Disengage or Hide."),
                f(5, "Uncanny Dodge", "Reaction: halve the damage of one attack you can see."),
                f(7, "Evasion", "No damage on a successful Dexterity save, half on a failure."),
                f(11, "Reliable Talent", "Treat a d20 roll of 9 or lower as 10 for checks with proficiency."),
                f(14, "Blindsense", "Aware of hidden or invisible creatures within 10 ft."),
                f(15, "Slippery Mind", "Proficiency in Wisdom saving throws."),
                f(18, "Elusive", "No attack roll has advantage against you while you are not incapacitated."),
                f(20, "Stroke of Luck", "Turn a miss into a hit or a failed check into a 20; once per short rest.")
            )
        ),
        CharacterClass(
            "sorcerer", "Sorcerer", HitDie.D6, listOf(Ability.CON, Ability.CHA), 2,
            listOf(Skill.ARCANA, Skill.DECEPTION, Skill.INSIGHT, Skill.INTIMIDATION, Skill.PERSUASION, Skill.RELIGION),
            spellcasting = Ability.CHA, casterDivisor = 1,
            armor = "None", weapons = "Dagger, dart, sling, quarterstaff, light crossbow",
            startingEquipment = "Light crossbow with 20 bolts, component pouch, dungeoneer's pack, two daggers",
            subclassLevel = 1, subclassLabel = "Sorcerous Origin",
            multiclassRequirement = mapOf(Ability.CHA to 13),
            features = listOf(
                f(2, "Font of Magic", "Sorcery points equal to your sorcerer level; convert them into spell slots and back."),
                f(3, "Metamagic", "Two options: Careful, Distant, Empowered, Extended, Heightened, Quickened, Subtle or Twinned Spell; more at 10th and 17th level."),
                f(20, "Sorcerous Restoration", "Regain 4 sorcery points on a short rest.")
            )
        ),
        CharacterClass(
            "warlock", "Warlock", HitDie.D8, listOf(Ability.WIS, Ability.CHA), 2,
            listOf(Skill.ARCANA, Skill.DECEPTION, Skill.HISTORY, Skill.INTIMIDATION, Skill.INVESTIGATION, Skill.NATURE, Skill.RELIGION),
            spellcasting = Ability.CHA, casterDivisor = 0, pactMagic = true,
            armor = "Light armour", weapons = "Simple weapons",
            startingEquipment = "Light crossbow with 20 bolts, component pouch, scholar's pack, leather armour, simple weapon, two daggers",
            subclassLevel = 1, subclassLabel = "Otherworldly Patron",
            multiclassRequirement = mapOf(Ability.CHA to 13),
            features = listOf(
                f(1, "Pact Magic", "Few slots, always of the highest level you can cast, all restored on a short rest."),
                f(2, "Eldritch Invocations", "Two permanent magical options, rising to eight; for example Agonizing Blast, Devil's Sight or Mask of Many Faces."),
                f(3, "Pact Boon", "Pact of the Blade, Pact of the Chain or Pact of the Tome."),
                f(11, "Mystic Arcanum", "One 6th-level spell cast once per long rest; 7th at 13th, 8th at 15th, 9th at 17th level."),
                f(20, "Eldritch Master", "Regain all Pact Magic slots after one minute of entreaty; once per long rest.")
            )
        ),
        CharacterClass(
            "wizard", "Wizard", HitDie.D6, listOf(Ability.INT, Ability.WIS), 2,
            listOf(Skill.ARCANA, Skill.HISTORY, Skill.INSIGHT, Skill.INVESTIGATION, Skill.MEDICINE, Skill.RELIGION),
            spellcasting = Ability.INT, casterDivisor = 1,
            armor = "None", weapons = "Dagger, dart, sling, quarterstaff, light crossbow",
            startingEquipment = "Quarterstaff, component pouch, scholar's pack, spellbook",
            subclassLevel = 2, subclassLabel = "Arcane Tradition",
            multiclassRequirement = mapOf(Ability.INT to 13),
            features = listOf(
                f(1, "Spellbook", "Six 1st-level spells to start, two more each wizard level; copy further spells you find."),
                f(1, "Arcane Recovery", "On a short rest recover spell slots whose combined level is up to half your wizard level; once per day."),
                f(18, "Spell Mastery", "Cast one 1st- and one 2nd-level spell at will."),
                f(20, "Signature Spells", "Two 3rd-level spells, each castable once per short rest without a slot.")
            )
        )
    )

    val byKey = all.associateBy { it.key }
}

object SrdSubclasses {
    private fun s(key: String, cls: String, name: String, src: ContentSource, vararg features: ClassFeature) =
        Subclass(key, cls, name, src, features.toList())

    val all: List<Subclass> = listOf(
        s("berserker", "barbarian", "Path of the Berserker", ContentSource.SRD51,
            f(3, "Frenzy", "Rage with frenzy: a bonus-action melee weapon attack each turn; one level of exhaustion when the rage ends."),
            f(6, "Mindless Rage", "You cannot be charmed or frightened while raging."),
            f(10, "Intimidating Presence", "Action: one creature within 30 ft must make a Wisdom save (DC 8 + proficiency + CHA) or be frightened."),
            f(14, "Retaliation", "Reaction: melee attack against a creature within 5 ft that damaged you.")),
        s("wild_heart", "barbarian", "Path of the Wild Heart", ContentSource.SRD52,
            f(3, "Animal Speaker", "Cast beast sense and speak with animals as rituals."),
            f(3, "Rage of the Wilds", "While raging choose Bear (resistance to all damage but force, psychic and radiant), Eagle (Disengage and Dash as a bonus action) or Wolf (allies have advantage against foes within 5 ft of you)."),
            f(6, "Aspect of the Wilds", "Owl (60 ft darkvision), Panther (climb speed) or Salmon (swim speed)."),
            f(10, "Nature Speaker", "Cast commune with nature as a ritual."),
            f(14, "Power of the Wilds", "While raging: Falcon (flying speed), Lion (foes within 5 ft have disadvantage against others) or Ram (knock targets prone).")),
        s("lore", "bard", "College of Lore", ContentSource.SRD51,
            f(3, "Bonus Proficiencies", "Proficiency in three skills of your choice."),
            f(3, "Cutting Words", "Reaction: spend Bardic Inspiration to subtract the die from an enemy's attack roll, ability check or damage roll."),
            f(6, "Additional Magical Secrets", "Two spells from any class list."),
            f(14, "Peerless Skill", "Add a Bardic Inspiration die to your own ability check.")),
        s("dance", "bard", "College of Dance", ContentSource.SRD52,
            f(3, "Dazzling Footwork", "While unarmoured and without a shield, your AC is 10 + DEX + CHA and your unarmed strikes deal 1d6 bludgeoning plus a Bardic Inspiration die."),
            f(6, "Inspiring Movement", "Reaction: move and let an ally move half your speed without provoking opportunity attacks."),
            f(14, "Leading Evasion", "No damage on a successful Dexterity save and half on a failure, for you and nearby allies.")),
        s("life", "cleric", "Life Domain", ContentSource.SRD51,
            f(1, "Bonus Proficiency", "Proficiency with heavy armour."),
            f(1, "Disciple of Life", "Healing spells of 1st level or higher restore an extra 2 + the spell's level hit points."),
            f(2, "Channel Divinity: Preserve Life", "Restore hit points equal to five times your cleric level, split among creatures within 30 ft, up to half their maximum each."),
            f(6, "Blessed Healer", "When you heal another creature with a spell, you regain 2 + the spell's level hit points."),
            f(8, "Divine Strike", "Weapon hits deal an extra 1d8 radiant damage, 2d8 from 14th level."),
            f(17, "Supreme Healing", "Healing dice are always maximised.")),
        s("light", "cleric", "Light Domain", ContentSource.SRD52,
            f(1, "Light Domain Spells", "Burning hands, faerie fire, flaming sphere, scorching ray, daylight, fireball, guardian of faith, wall of fire, flame strike, scrying."),
            f(1, "Warding Flare", "Reaction: impose disadvantage on an attack roll against a creature you can see; WIS uses per long rest."),
            f(2, "Channel Divinity: Radiance of the Dawn", "Dispel magical darkness and deal 2d10 + cleric level radiant damage in a 30 ft radius."),
            f(6, "Improved Warding Flare", "Warding Flare also grants temporary hit points."),
            f(17, "Corona of Light", "Action: 60 ft of bright light for 1 minute; enemies in it have disadvantage on saves against radiant and fire spells.")),
        s("land", "druid", "Circle of the Land", ContentSource.SRD51,
            f(2, "Bonus Cantrip", "One extra druid cantrip."),
            f(2, "Natural Recovery", "On a short rest recover spell slots totalling half your druid level; once per day."),
            f(3, "Circle Spells", "Extra always-prepared spells from your chosen land: arctic, coast, desert, forest, grassland, mountain, swamp or underdark."),
            f(6, "Land's Stride", "Difficult terrain costs no extra movement; advantage against entangling plants."),
            f(10, "Nature's Ward", "Immune to poison and disease; cannot be charmed or frightened by elementals or fey."),
            f(14, "Nature's Sanctuary", "Beasts and plants must make a Wisdom save to attack you.")),
        s("sea", "druid", "Circle of the Sea", ContentSource.SRD52,
            f(3, "Wrath of the Sea", "Bonus action: a 5 ft emanation for 10 minutes; once per turn force a Constitution save for WIS cold damage dice and push 15 ft."),
            f(6, "Aquatic Affinity", "The emanation grows to 10 ft and you gain a swimming speed equal to your speed."),
            f(10, "Stormborn", "While the emanation is active you gain a flying speed and resistance to cold, lightning and thunder damage."),
            f(14, "Oceanic Gift", "Create the emanation around an ally instead of yourself.")),
        s("champion", "fighter", "Champion", ContentSource.SRD51,
            f(3, "Improved Critical", "Weapon attacks score a critical hit on a 19 or 20."),
            f(7, "Remarkable Athlete", "Add half your proficiency bonus to STR, DEX and CON checks; your running long jump is longer."),
            f(10, "Additional Fighting Style", "A second fighting style."),
            f(15, "Superior Critical", "Critical hits on 18 to 20."),
            f(18, "Survivor", "Regain 5 + CON hit points at the start of each turn while below half health.")),
        s("battle_master", "fighter", "Battle Master", ContentSource.SRD52,
            f(3, "Combat Superiority", "Four manoeuvres and four d8 superiority dice, regained on a short rest; manoeuvre save DC is 8 + proficiency + STR or DEX."),
            f(7, "Know Your Enemy", "Study a creature to learn its immunities, resistances and vulnerabilities."),
            f(10, "Improved Combat Superiority", "Superiority dice become d10s, d12s at 18th level."),
            f(15, "Relentless", "Regain one superiority die on initiative when you have none left.")),
        s("open_hand", "monk", "Way of the Open Hand", ContentSource.SRD51,
            f(3, "Open Hand Technique", "Flurry of Blows hits can knock prone, push 15 ft or deny reactions."),
            f(6, "Wholeness of Body", "Action: regain three times your monk level in hit points; once per long rest."),
            f(11, "Tranquility", "Gain the effect of a sanctuary spell between long rests."),
            f(17, "Quivering Palm", "Spend 3 ki to set up lethal vibrations; later reduce the target to 0 hit points on a failed Constitution save.")),
        s("warrior_shadow", "monk", "Warrior of Shadow", ContentSource.SRD52,
            f(3, "Shadow Arts", "Darkness, darkvision, pass without trace and silence for ki; you know the minor illusion cantrip."),
            f(6, "Shadow Step", "Teleport 60 ft between areas of dim light or darkness; advantage on your next melee attack."),
            f(11, "Improved Shadow Step", "Teleport without needing darkness at both ends and make an attack as part of it."),
            f(17, "Cloak of Shadows", "Become invisible in dim light or darkness until you attack or cast a spell.")),
        s("devotion", "paladin", "Oath of Devotion", ContentSource.SRD51,
            f(3, "Channel Divinity: Sacred Weapon", "Add your CHA modifier to attack rolls with a weapon for 1 minute; it glows and counts as magical."),
            f(3, "Channel Divinity: Turn the Unholy", "Turn fiends and undead within 30 ft."),
            f(7, "Aura of Devotion", "You and allies within 10 ft cannot be charmed; 30 ft from 18th level."),
            f(15, "Purity of Spirit", "You are always under the effect of protection from evil and good."),
            f(20, "Holy Nimbus", "Bright light for 1 minute, 10 radiant damage to enemies that start their turn near you, advantage on saves against fiends and undead.")),
        s("glory", "paladin", "Oath of Glory", ContentSource.SRD52,
            f(3, "Channel Divinity: Peerless Athlete", "Advantage on Athletics and Acrobatics, carry more and jump further for 1 hour."),
            f(3, "Channel Divinity: Inspiring Smite", "After a Divine Smite, distribute 2d8 + paladin level temporary hit points."),
            f(7, "Aura of Alacrity", "Your speed increases by 10 ft and allies who come near gain 10 ft of speed."),
            f(15, "Glorious Defence", "Reaction: add your CHA modifier to a target's AC against an attack and strike back on a miss."),
            f(20, "Living Legend", "Advantage on Charisma checks, turn a miss into a hit once per turn, and reroll failed saves.")),
        s("hunter", "ranger", "Hunter", ContentSource.SRD51,
            f(3, "Hunter's Prey", "Colossus Slayer (1d8 extra damage to a wounded target), Giant Killer or Horde Breaker."),
            f(7, "Defensive Tactics", "Escape the Horde, Multiattack Defence or Steel Will."),
            f(11, "Multiattack", "Volley (attack any number of creatures in a 10 ft radius) or Whirlwind Attack."),
            f(15, "Superior Hunter's Defence", "Evasion, Stand Against the Tide, or Uncanny Dodge.")),
        s("beast_master", "ranger", "Beast Master", ContentSource.SRD52,
            f(3, "Primal Companion", "Summon a Beast of the Land, Sea or Sky that acts on your turn and scales with your proficiency bonus."),
            f(7, "Exceptional Training", "Your companion's attacks count as magical and it can Dash, Disengage, Dodge or Help as a bonus action."),
            f(11, "Bestial Fury", "The companion makes two attacks when it attacks."),
            f(15, "Share Spells", "Spells you cast on yourself can also affect the companion within 30 ft.")),
        s("thief", "rogue", "Thief", ContentSource.SRD51,
            f(3, "Fast Hands", "Use Cunning Action to make a Sleight of Hand check, use thieves' tools or take the Use an Object action."),
            f(3, "Second-Story Work", "Climbing costs no extra movement and your running jump is longer."),
            f(9, "Supreme Sneak", "Advantage on Stealth if you move no more than half your speed."),
            f(13, "Use Magic Device", "Ignore class, race and level requirements on magic items."),
            f(17, "Thief's Reflexes", "Two turns in the first round of combat.")),
        s("assassin", "rogue", "Assassin", ContentSource.SRD52,
            f(3, "Assassinate", "Advantage on attacks against creatures that have not acted; a hit against a surprised creature is a critical."),
            f(3, "Assassin's Tools", "Proficiency with the disguise kit and the poisoner's kit."),
            f(9, "Infiltration Expertise", "Create a false identity and mimic speech and mannerisms."),
            f(13, "Envenom Weapons", "Your poison damage ignores resistance and your Sneak Attack can poison."),
            f(17, "Death Strike", "Double the damage against a surprised creature that fails a Constitution save.")),
        s("draconic", "sorcerer", "Draconic Bloodline", ContentSource.SRD51,
            f(1, "Dragon Ancestor", "Choose a dragon type; you speak Draconic and double your proficiency on Charisma checks with dragons."),
            f(1, "Draconic Resilience", "Hit point maximum +1 per sorcerer level; your AC is 13 + DEX without armour."),
            f(6, "Elemental Affinity", "Add your CHA modifier to one damage roll of a spell of your ancestry's type; spend 1 sorcery point for resistance."),
            f(14, "Dragon Wings", "Sprout wings with a flying speed equal to your speed."),
            f(18, "Draconic Presence", "Spend 5 sorcery points for an aura of awe or fear in a 60 ft radius.")),
        s("aberrant", "sorcerer", "Aberrant Sorcery", ContentSource.SRD52,
            f(3, "Psionic Spells", "Arms of Hadar, calm emotions, detect thoughts, hunger of Hadar, sending, telekinesis and more, always prepared."),
            f(3, "Telepathic Speech", "Telepathic link with a creature within 30 ft for your sorcerer level in minutes."),
            f(6, "Psionic Sorcery", "Cast psionic spells using sorcery points instead of slots, with no verbal or somatic components."),
            f(6, "Psychic Defences", "Resistance to psychic damage and advantage against being charmed or frightened."),
            f(18, "Warping Implosion", "Teleport 120 ft and deal 3d10 force damage around your origin.")),
        s("fiend", "warlock", "The Fiend", ContentSource.SRD51,
            f(1, "Dark One's Blessing", "Temporary hit points equal to CHA + warlock level when you reduce a hostile creature to 0 hit points."),
            f(6, "Dark One's Own Luck", "Add 1d10 to an ability check or saving throw; once per short rest."),
            f(10, "Fiendish Resilience", "Resistance to one damage type of your choice, chosen after each rest."),
            f(14, "Hurl Through Hell", "Send a creature through the lower planes for 10d10 psychic damage; once per long rest.")),
        s("great_old_one", "warlock", "The Great Old One", ContentSource.SRD52,
            f(3, "Awakened Mind", "Telepathic speech with any creature within 30 ft."),
            f(3, "Psychic Spells", "Spells you cast can deal psychic damage instead of their normal type."),
            f(6, "Clairvoyant Combatant", "Form a telepathic bond that gives you advantage and the target disadvantage."),
            f(10, "Eldritch Hex", "Hex gives the target disadvantage on saves against your spells."),
            f(14, "Create Thrall", "Charm an incapacitated creature and communicate telepathically at any distance.")),
        s("evocation", "wizard", "School of Evocation", ContentSource.SRD51,
            f(2, "Evocation Savant", "Copying evocation spells into your spellbook costs half the time and gold."),
            f(2, "Sculpt Spells", "Protect 1 + spell level creatures from your evocation spells."),
            f(6, "Potent Cantrip", "Creatures take half damage from your cantrips even on a successful save."),
            f(10, "Empowered Evocation", "Add your INT modifier to one damage roll of an evocation spell."),
            f(14, "Overchannel", "Maximise the damage of a spell of 5th level or lower; later uses cost you necrotic damage.")),
        s("abjuration", "wizard", "School of Abjuration", ContentSource.SRD52,
            f(3, "Arcane Ward", "A ward with hit points equal to twice your wizard level + INT absorbs damage for you."),
            f(6, "Projected Ward", "Spend the ward to absorb damage for a creature within 30 ft."),
            f(10, "Spell Breaker", "Dispel magic and counterspell are always prepared and cast at a higher level for free."),
            f(14, "Spell Resistance", "Advantage on saving throws against spells and resistance to their damage."))
    )

    val byClass: Map<String, List<Subclass>> = all.groupBy { it.classKey }
    val byKey = all.associateBy { it.key }
}

object SrdBackgrounds {
    val all: List<Background> = listOf(
        Background("acolyte", "Acolyte", listOf(Skill.INSIGHT, Skill.RELIGION), "—", 2,
            "Holy symbol, prayer book, 5 sticks of incense, vestments, common clothes, 15 gp",
            "Shelter of the Faithful: temples of your faith provide free healing and lodging."),
        Background("charlatan", "Charlatan", listOf(Skill.DECEPTION, Skill.SLEIGHT_OF_HAND), "Disguise kit, forgery kit", 0,
            "Fine clothes, disguise kit, tools of your favourite con, 15 gp",
            "False Identity: a second, well-documented identity and the ability to forge papers."),
        Background("criminal", "Criminal", listOf(Skill.DECEPTION, Skill.STEALTH), "One gaming set, thieves' tools", 0,
            "Crowbar, dark hooded clothing, 15 gp",
            "Criminal Contact: a reliable contact who acts as your liaison to a network of criminals."),
        Background("entertainer", "Entertainer", listOf(Skill.ACROBATICS, Skill.PERFORMANCE), "Disguise kit, one musical instrument", 0,
            "Musical instrument, the favour of an admirer, a costume, 15 gp",
            "By Popular Demand: performing earns you free lodging and food of modest standard."),
        Background("folk_hero", "Folk Hero", listOf(Skill.ANIMAL_HANDLING, Skill.SURVIVAL), "One artisan's tools, land vehicles", 0,
            "Artisan's tools, shovel, iron pot, common clothes, 10 gp",
            "Rustic Hospitality: common folk shelter and hide you."),
        Background("guild_artisan", "Guild Artisan", listOf(Skill.INSIGHT, Skill.PERSUASION), "One artisan's tools", 1,
            "Artisan's tools, letter of introduction from your guild, traveller's clothes, 15 gp",
            "Guild Membership: the guild supports you and provides lodging and food."),
        Background("hermit", "Hermit", listOf(Skill.MEDICINE, Skill.RELIGION), "Herbalism kit", 1,
            "Scroll case of notes, winter blanket, common clothes, herbalism kit, 5 gp",
            "Discovery: a unique and powerful insight from your seclusion."),
        Background("noble", "Noble", listOf(Skill.HISTORY, Skill.PERSUASION), "One gaming set", 1,
            "Fine clothes, signet ring, scroll of pedigree, 25 gp",
            "Position of Privilege: you are welcome in high society and can secure an audience."),
        Background("outlander", "Outlander", listOf(Skill.ATHLETICS, Skill.SURVIVAL), "One musical instrument", 1,
            "Staff, hunting trap, a trophy, traveller's clothes, 10 gp",
            "Wanderer: excellent memory for maps; you find food and water for yourself and five others."),
        Background("sage", "Sage", listOf(Skill.ARCANA, Skill.HISTORY), "—", 2,
            "Ink, quill, small knife, a letter posing a question, common clothes, 10 gp",
            "Researcher: you know where and from whom to obtain a piece of lore."),
        Background("sailor", "Sailor", listOf(Skill.ATHLETICS, Skill.PERCEPTION), "Navigator's tools, water vehicles", 0,
            "Belaying pin, 50 ft silk rope, a lucky charm, common clothes, 10 gp",
            "Ship's Passage: free passage for you and your companions in exchange for work."),
        Background("soldier", "Soldier", listOf(Skill.ATHLETICS, Skill.INTIMIDATION), "One gaming set, land vehicles", 0,
            "Insignia of rank, a trophy, dice or cards, common clothes, 10 gp",
            "Military Rank: soldiers of your former army recognise your authority."),
        Background("urchin", "Urchin", listOf(Skill.SLEIGHT_OF_HAND, Skill.STEALTH), "Disguise kit, thieves' tools", 0,
            "Small knife, map of your city, a pet mouse, a token of your parents, common clothes, 10 gp",
            "City Secrets: you travel twice as fast between places in a city.")
    )

    val byKey = all.associateBy { it.key }
}

object SrdFeats {
    val all: List<Feat> = listOf(
        Feat("grappler", "Grappler", "Strength 13 or higher",
            "Advantage on attack rolls against a creature you are grappling, and you can try to pin a grappled creature with an additional grapple check.",
            source = ContentSource.SRD51),
        Feat("alert", "Alert", "—",
            "Add your proficiency bonus to initiative and swap initiative with a willing ally.", source = ContentSource.SRD52),
        Feat("crafter", "Crafter", "—",
            "Proficiency with three artisan's tools, a 20 % discount on nonmagical items and faster crafting.", source = ContentSource.SRD52),
        Feat("healer", "Healer", "—",
            "Use a healer's kit to let a creature spend a hit die plus your proficiency bonus, and stabilise a creature with 1 hit point.", source = ContentSource.SRD52),
        Feat("lucky", "Lucky", "—",
            "Luck points equal to your proficiency bonus per long rest; spend one for advantage on a d20 test or to impose disadvantage on an attack against you.", source = ContentSource.SRD52),
        Feat("magic_initiate", "Magic Initiate", "—",
            "Two cantrips and one 1st-level spell from the cleric, druid or wizard list; cast the spell once per long rest without a slot.", source = ContentSource.SRD52),
        Feat("musician", "Musician", "—",
            "Proficiency with three musical instruments; after a rest grant Heroic Inspiration to allies equal to your proficiency bonus.", source = ContentSource.SRD52),
        Feat("savage_attacker", "Savage Attacker", "—",
            "Once per turn reroll the damage dice of a weapon attack and use either total.", source = ContentSource.SRD52),
        Feat("skilled", "Skilled", "—",
            "Proficiency in any combination of three skills or tools.", source = ContentSource.SRD52),
        Feat("tavern_brawler", "Tavern Brawler", "—",
            "Unarmed strikes deal 1d4, reroll a damage die of 1, push on a hit, and improvised weapons count as proficient. +1 Strength or Constitution.",
            abilityChoice = listOf(Ability.STR, Ability.CON), abilityAmount = 1, source = ContentSource.SRD52),
        Feat("tough", "Tough", "—",
            "Your hit point maximum increases by twice your character level.", source = ContentSource.SRD52),
        Feat("ability_score_improvement", "Ability Score Improvement", "Level 4+",
            "Increase one ability score by 2, or two scores by 1 each, to a maximum of 20.",
            abilityChoice = Ability.entries.toList(), abilityAmount = 2, source = ContentSource.SRD51)
    )

    val byKey = all.associateBy { it.key }
}

data class Weapon(
    val name: String,
    val damage: String,
    val type: String,
    val category: String,
    val finesse: Boolean = false,
    val ranged: Boolean = false,
    val twoHanded: Boolean = false,
    val versatile: String? = null,
    val light: Boolean = false,
    val heavy: Boolean = false,
    val reach: Boolean = false,
    val thrown: String? = null,
    val range: String? = null,
    val weightLb: Double = 2.0,
    val costGp: Double = 1.0
)

data class Armor(
    val name: String,
    val baseAc: Int,
    val dexCap: Int?,
    val strRequired: Int = 0,
    val stealthDisadvantage: Boolean = false,
    val shield: Boolean = false,
    val weightLb: Double = 10.0,
    val costGp: Double = 10.0
)

data class GearItem(val name: String, val weightLb: Double, val costGp: Double, val note: String = "")

object SrdEquipment {
    val weapons = listOf(
        // Simple melee
        Weapon("Club", "1d4", "bludgeoning", "Simple melee", light = true, weightLb = 2.0, costGp = 0.1),
        Weapon("Dagger", "1d4", "piercing", "Simple melee", finesse = true, light = true, thrown = "20/60 ft", weightLb = 1.0, costGp = 2.0),
        Weapon("Greatclub", "1d8", "bludgeoning", "Simple melee", twoHanded = true, weightLb = 10.0, costGp = 0.2),
        Weapon("Handaxe", "1d6", "slashing", "Simple melee", light = true, thrown = "20/60 ft", weightLb = 2.0, costGp = 5.0),
        Weapon("Javelin", "1d6", "piercing", "Simple melee", thrown = "30/120 ft", weightLb = 2.0, costGp = 0.5),
        Weapon("Light hammer", "1d4", "bludgeoning", "Simple melee", light = true, thrown = "20/60 ft", weightLb = 2.0, costGp = 2.0),
        Weapon("Mace", "1d6", "bludgeoning", "Simple melee", weightLb = 4.0, costGp = 5.0),
        Weapon("Quarterstaff", "1d6", "bludgeoning", "Simple melee", versatile = "1d8", weightLb = 4.0, costGp = 0.2),
        Weapon("Sickle", "1d4", "slashing", "Simple melee", light = true, weightLb = 2.0, costGp = 1.0),
        Weapon("Spear", "1d6", "piercing", "Simple melee", versatile = "1d8", thrown = "20/60 ft", weightLb = 3.0, costGp = 1.0),
        // Simple ranged
        Weapon("Light crossbow", "1d8", "piercing", "Simple ranged", ranged = true, twoHanded = true, range = "80/320 ft", weightLb = 5.0, costGp = 25.0),
        Weapon("Dart", "1d4", "piercing", "Simple ranged", finesse = true, ranged = true, thrown = "20/60 ft", weightLb = 0.25, costGp = 0.05),
        Weapon("Shortbow", "1d6", "piercing", "Simple ranged", ranged = true, twoHanded = true, range = "80/320 ft", weightLb = 2.0, costGp = 25.0),
        Weapon("Sling", "1d4", "bludgeoning", "Simple ranged", ranged = true, range = "30/120 ft", weightLb = 0.0, costGp = 0.1),
        // Martial melee
        Weapon("Battleaxe", "1d8", "slashing", "Martial melee", versatile = "1d10", weightLb = 4.0, costGp = 10.0),
        Weapon("Flail", "1d8", "bludgeoning", "Martial melee", weightLb = 2.0, costGp = 10.0),
        Weapon("Glaive", "1d10", "slashing", "Martial melee", heavy = true, reach = true, twoHanded = true, weightLb = 6.0, costGp = 20.0),
        Weapon("Greataxe", "1d12", "slashing", "Martial melee", heavy = true, twoHanded = true, weightLb = 7.0, costGp = 30.0),
        Weapon("Greatsword", "2d6", "slashing", "Martial melee", heavy = true, twoHanded = true, weightLb = 6.0, costGp = 50.0),
        Weapon("Halberd", "1d10", "slashing", "Martial melee", heavy = true, reach = true, twoHanded = true, weightLb = 6.0, costGp = 20.0),
        Weapon("Lance", "1d12", "piercing", "Martial melee", reach = true, weightLb = 6.0, costGp = 10.0),
        Weapon("Longsword", "1d8", "slashing", "Martial melee", versatile = "1d10", weightLb = 3.0, costGp = 15.0),
        Weapon("Maul", "2d6", "bludgeoning", "Martial melee", heavy = true, twoHanded = true, weightLb = 10.0, costGp = 10.0),
        Weapon("Morningstar", "1d8", "piercing", "Martial melee", weightLb = 4.0, costGp = 15.0),
        Weapon("Pike", "1d10", "piercing", "Martial melee", heavy = true, reach = true, twoHanded = true, weightLb = 18.0, costGp = 5.0),
        Weapon("Rapier", "1d8", "piercing", "Martial melee", finesse = true, weightLb = 2.0, costGp = 25.0),
        Weapon("Scimitar", "1d6", "slashing", "Martial melee", finesse = true, light = true, weightLb = 3.0, costGp = 25.0),
        Weapon("Shortsword", "1d6", "piercing", "Martial melee", finesse = true, light = true, weightLb = 2.0, costGp = 10.0),
        Weapon("Trident", "1d6", "piercing", "Martial melee", versatile = "1d8", thrown = "20/60 ft", weightLb = 4.0, costGp = 5.0),
        Weapon("War pick", "1d8", "piercing", "Martial melee", weightLb = 2.0, costGp = 5.0),
        Weapon("Warhammer", "1d8", "bludgeoning", "Martial melee", versatile = "1d10", weightLb = 2.0, costGp = 15.0),
        Weapon("Whip", "1d4", "slashing", "Martial melee", finesse = true, reach = true, weightLb = 3.0, costGp = 2.0),
        // Martial ranged
        Weapon("Blowgun", "1", "piercing", "Martial ranged", ranged = true, range = "25/100 ft", weightLb = 1.0, costGp = 10.0),
        Weapon("Hand crossbow", "1d6", "piercing", "Martial ranged", ranged = true, light = true, range = "30/120 ft", weightLb = 3.0, costGp = 75.0),
        Weapon("Heavy crossbow", "1d10", "piercing", "Martial ranged", ranged = true, heavy = true, twoHanded = true, range = "100/400 ft", weightLb = 18.0, costGp = 50.0),
        Weapon("Longbow", "1d8", "piercing", "Martial ranged", ranged = true, heavy = true, twoHanded = true, range = "150/600 ft", weightLb = 2.0, costGp = 50.0),
        Weapon("Net", "0", "—", "Martial ranged", ranged = true, thrown = "5/15 ft", weightLb = 3.0, costGp = 1.0),
        Weapon("Unarmed strike", "1", "bludgeoning", "Unarmed", weightLb = 0.0, costGp = 0.0)
    )

    val weaponsByName = weapons.associateBy { it.name }

    val armors = listOf(
        Armor("No armour", 10, null, weightLb = 0.0, costGp = 0.0),
        Armor("Padded", 11, null, stealthDisadvantage = true, weightLb = 8.0, costGp = 5.0),
        Armor("Leather", 11, null, weightLb = 10.0, costGp = 10.0),
        Armor("Studded leather", 12, null, weightLb = 13.0, costGp = 45.0),
        Armor("Hide", 12, 2, weightLb = 12.0, costGp = 10.0),
        Armor("Chain shirt", 13, 2, weightLb = 20.0, costGp = 50.0),
        Armor("Scale mail", 14, 2, stealthDisadvantage = true, weightLb = 45.0, costGp = 50.0),
        Armor("Breastplate", 14, 2, weightLb = 20.0, costGp = 400.0),
        Armor("Half plate", 15, 2, stealthDisadvantage = true, weightLb = 40.0, costGp = 750.0),
        Armor("Ring mail", 14, 0, stealthDisadvantage = true, weightLb = 40.0, costGp = 30.0),
        Armor("Chain mail", 16, 0, strRequired = 13, stealthDisadvantage = true, weightLb = 55.0, costGp = 75.0),
        Armor("Splint", 17, 0, strRequired = 15, stealthDisadvantage = true, weightLb = 60.0, costGp = 200.0),
        Armor("Plate", 18, 0, strRequired = 15, stealthDisadvantage = true, weightLb = 65.0, costGp = 1500.0)
    )

    val armorsByName = armors.associateBy { it.name }
    val shield = Armor("Shield", 2, null, shield = true, weightLb = 6.0, costGp = 10.0)

    /** Common adventuring gear, for the inventory picker. */
    val gear = listOf(
        GearItem("Backpack", 5.0, 2.0), GearItem("Bedroll", 7.0, 1.0), GearItem("Rations (1 day)", 2.0, 0.5),
        GearItem("Rope, hempen (50 ft)", 10.0, 1.0), GearItem("Rope, silk (50 ft)", 5.0, 10.0),
        GearItem("Torch", 1.0, 0.01), GearItem("Lantern, hooded", 2.0, 5.0), GearItem("Oil (flask)", 1.0, 0.1),
        GearItem("Tinderbox", 1.0, 0.5), GearItem("Waterskin", 5.0, 0.2), GearItem("Crowbar", 5.0, 2.0),
        GearItem("Hammer", 3.0, 1.0), GearItem("Piton", 0.25, 0.05), GearItem("Grappling hook", 4.0, 2.0),
        GearItem("Healer's kit", 3.0, 5.0, "Ten uses: stabilise a dying creature"),
        GearItem("Potion of healing", 0.5, 50.0, "2d4 + 2 hit points"),
        GearItem("Thieves' tools", 1.0, 25.0), GearItem("Holy symbol", 1.0, 5.0),
        GearItem("Component pouch", 2.0, 25.0), GearItem("Arcane focus", 3.0, 10.0),
        GearItem("Spellbook", 3.0, 50.0), GearItem("Druidic focus", 3.0, 10.0),
        GearItem("Arrows (20)", 1.0, 1.0), GearItem("Crossbow bolts (20)", 1.5, 1.0),
        GearItem("Caltrops (bag of 20)", 2.0, 1.0), GearItem("Ball bearings (bag of 1000)", 2.0, 1.0),
        GearItem("Chain (10 ft)", 10.0, 5.0), GearItem("Manacles", 6.0, 2.0),
        GearItem("Mirror, steel", 0.5, 5.0), GearItem("Shovel", 5.0, 2.0),
        GearItem("Climber's kit", 12.0, 25.0), GearItem("Mess kit", 1.0, 0.2),
        GearItem("Alchemist's fire (flask)", 1.0, 50.0, "1d4 fire damage per turn, DC 10 to extinguish"),
        GearItem("Acid (vial)", 1.0, 25.0, "2d6 acid damage"),
        GearItem("Antitoxin (vial)", 0.0, 50.0, "Advantage against poison for 1 hour"),
        GearItem("Poison, basic (vial)", 0.0, 100.0, "1d4 poison damage on a hit, DC 10 save"),
        GearItem("Explorer's pack", 59.0, 10.0), GearItem("Dungeoneer's pack", 61.5, 12.0),
        GearItem("Burglar's pack", 44.5, 16.0), GearItem("Priest's pack", 24.0, 19.0),
        GearItem("Scholar's pack", 10.0, 40.0), GearItem("Diplomat's pack", 39.0, 39.0),
        GearItem("Entertainer's pack", 38.0, 40.0)
    )
}

package app.maximus.dnd.domain

enum class SpellSchool(val label: String) {
    ABJURATION("Abjuration"), CONJURATION("Conjuration"), DIVINATION("Divination"),
    ENCHANTMENT("Enchantment"), EVOCATION("Evocation"), ILLUSION("Illusion"),
    NECROMANCY("Necromancy"), TRANSMUTATION("Transmutation")
}

/**
 * One spell. [level] 0 is a cantrip. [classes] holds class keys that have the spell on their list.
 * Texts are condensed to the mechanically relevant effect; the full descriptions live in the SRD.
 */
data class Spell(
    val key: String,
    val name: String,
    val level: Int,
    val school: SpellSchool,
    val castingTime: String,
    val range: String,
    val components: String,
    val duration: String,
    val classes: Set<String>,
    val text: String,
    val concentration: Boolean = false,
    val ritual: Boolean = false,
    val source: ContentSource = ContentSource.SRD51
) {
    val levelLabel: String get() = if (level == 0) "Cantrip" else "Level $level"
}

/**
 * Spells from SRD 5.1, condensed. This is a large but not complete selection: the combat, utility and
 * iconic spells of every class list are present. Anything missing can be added through the homebrew
 * editor, which produces entries with the same fields.
 */
object Spells {
    private const val BARD = "bard"
    private const val CLERIC = "cleric"
    private const val DRUID = "druid"
    private const val PALADIN = "paladin"
    private const val RANGER = "ranger"
    private const val SORCERER = "sorcerer"
    private const val WARLOCK = "warlock"
    private const val WIZARD = "wizard"

    private fun sp(
        key: String, name: String, level: Int, school: SpellSchool, time: String, range: String,
        comp: String, duration: String, classes: String, text: String,
        conc: Boolean = false, ritual: Boolean = false
    ) = Spell(key, name, level, school, time, range, comp, duration, classes.split(",").map { it.trim() }.toSet(), text, conc, ritual)

    private val A = SpellSchool.ABJURATION
    private val C = SpellSchool.CONJURATION
    private val D = SpellSchool.DIVINATION
    private val E = SpellSchool.ENCHANTMENT
    private val V = SpellSchool.EVOCATION
    private val I = SpellSchool.ILLUSION
    private val N = SpellSchool.NECROMANCY
    private val T = SpellSchool.TRANSMUTATION

    val all: List<Spell> = listOf(
        // ---------------- Cantrips ----------------
        sp("acid_splash", "Acid Splash", 0, C, "1 action", "60 ft", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Hurl a bubble of acid at one or two creatures within 5 ft of each other. Dexterity save or 1d6 acid damage. Scales at 5th, 11th and 17th level."),
        sp("blade_ward", "Blade Ward", 0, A, "1 action", "Self", "V, S", "1 round", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Resistance to bludgeoning, piercing and slashing damage from weapon attacks until the end of your next turn."),
        sp("chill_touch", "Chill Touch", 0, N, "1 action", "120 ft", "V, S", "1 round", "$SORCERER,$WARLOCK,$WIZARD",
            "Ranged spell attack for 1d8 necrotic damage; the target cannot regain hit points until your next turn. Undead also have disadvantage on attacks against you."),
        sp("dancing_lights", "Dancing Lights", 0, V, "1 action", "120 ft", "V, S, M", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "Up to four torch-sized lights you can move 60 ft as a bonus action.", conc = true),
        sp("druidcraft", "Druidcraft", 0, T, "1 action", "30 ft", "V, S", "Instantaneous", DRUID,
            "Predict the weather, make a flower bloom, light or snuff a small flame, or create a harmless sensory effect."),
        sp("eldritch_blast", "Eldritch Blast", 0, V, "1 action", "120 ft", "V, S", "Instantaneous", WARLOCK,
            "Ranged spell attack for 1d10 force damage. Two beams at 5th level, three at 11th, four at 17th; each beam is a separate attack."),
        sp("fire_bolt", "Fire Bolt", 0, V, "1 action", "120 ft", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Ranged spell attack for 1d10 fire damage; flammable objects ignite. Scales at 5th, 11th and 17th level."),
        sp("friends", "Friends", 0, E, "1 action", "Self", "S, M", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Advantage on Charisma checks against one non-hostile creature; it becomes hostile when the spell ends.", conc = true),
        sp("guidance", "Guidance", 0, D, "1 action", "Touch", "V, S", "1 minute", "$CLERIC,$DRUID",
            "The target adds 1d4 to one ability check of its choice.", conc = true),
        sp("light", "Light", 0, V, "1 action", "Touch", "V, M", "1 hour", "$BARD,$CLERIC,$SORCERER,$WIZARD",
            "An object sheds bright light in a 20 ft radius and dim light for another 20 ft."),
        sp("mage_hand", "Mage Hand", 0, C, "1 action", "30 ft", "V, S", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A spectral hand that can manipulate objects, open doors and carry up to 10 lb."),
        sp("mending", "Mending", 0, T, "1 minute", "Touch", "V, S, M", "Instantaneous", "$BARD,$CLERIC,$DRUID,$SORCERER,$WIZARD",
            "Repair a single break or tear in an object no larger than 1 ft."),
        sp("message", "Message", 0, T, "1 action", "120 ft", "V, S, M", "1 round", "$BARD,$SORCERER,$WIZARD",
            "Whisper a message that only the target hears; it can whisper back."),
        sp("minor_illusion", "Minor Illusion", 0, I, "1 action", "30 ft", "S, M", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Create a sound or an image of an object no larger than a 5 ft cube; Investigation check to discern the illusion."),
        sp("poison_spray", "Poison Spray", 0, C, "1 action", "10 ft", "V, S", "Instantaneous", "$DRUID,$SORCERER,$WARLOCK,$WIZARD",
            "Constitution save or 1d12 poison damage. Scales at 5th, 11th and 17th level."),
        sp("prestidigitation", "Prestidigitation", 0, T, "1 action", "10 ft", "V, S", "Up to 1 hour", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A harmless sensory effect, lighting or snuffing a flame, cleaning or soiling an object, or a trinket."),
        sp("produce_flame", "Produce Flame", 0, C, "1 action", "Self", "V, S", "10 minutes", DRUID,
            "A flame in your hand sheds light; hurl it as a ranged spell attack for 1d8 fire damage."),
        sp("ray_of_frost", "Ray of Frost", 0, V, "1 action", "60 ft", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Ranged spell attack for 1d8 cold damage and the target's speed drops by 10 ft until your next turn."),
        sp("resistance", "Resistance", 0, A, "1 action", "Touch", "V, S, M", "1 minute", "$CLERIC,$DRUID",
            "The target adds 1d4 to one saving throw of its choice.", conc = true),
        sp("sacred_flame", "Sacred Flame", 0, V, "1 action", "60 ft", "V, S", "Instantaneous", CLERIC,
            "Dexterity save (no benefit from cover) or 1d8 radiant damage. Scales at 5th, 11th and 17th level."),
        sp("shillelagh", "Shillelagh", 0, T, "1 bonus action", "Touch", "V, S, M", "1 minute", DRUID,
            "A club or quarterstaff deals 1d8 damage and uses your spellcasting ability for attack and damage."),
        sp("shocking_grasp", "Shocking Grasp", 0, V, "1 action", "Touch", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Melee spell attack with advantage against targets in metal armour for 1d8 lightning damage; the target cannot take reactions."),
        sp("spare_the_dying", "Spare the Dying", 0, N, "1 action", "Touch", "V, S", "Instantaneous", CLERIC,
            "Stabilise a creature at 0 hit points."),
        sp("thaumaturgy", "Thaumaturgy", 0, T, "1 action", "30 ft", "V", "Up to 1 minute", CLERIC,
            "A booming voice, flickering flames, tremors, or doors flying open."),
        sp("thorn_whip", "Thorn Whip", 0, T, "1 action", "30 ft", "V, S, M", "Instantaneous", DRUID,
            "Melee spell attack for 1d6 piercing damage and pull a Large or smaller creature 10 ft closer."),
        sp("true_strike", "True Strike", 0, D, "1 action", "30 ft", "S", "1 round", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Advantage on your first attack roll against the target on your next turn.", conc = true),
        sp("vicious_mockery", "Vicious Mockery", 0, E, "1 action", "60 ft", "V", "Instantaneous", BARD,
            "Wisdom save or 1d4 psychic damage and disadvantage on the target's next attack roll."),

        // ---------------- Level 1 ----------------
        sp("alarm", "Alarm", 1, A, "1 minute", "30 ft", "V, S, M", "8 hours", "$RANGER,$WIZARD",
            "Ward a 20 ft cube; you get a mental or audible alarm when a creature enters.", ritual = true),
        sp("animal_friendship", "Animal Friendship", 1, E, "1 action", "30 ft", "V, S, M", "24 hours", "$BARD,$DRUID,$RANGER",
            "A beast with Intelligence 3 or lower must make a Wisdom save or be charmed."),
        sp("armor_of_agathys", "Armor of Agathys", 1, A, "1 action", "Self", "V, S, M", "1 hour", WARLOCK,
            "5 temporary hit points; a creature that hits you in melee takes 5 cold damage. +5/+5 per slot level."),
        sp("bane", "Bane", 1, E, "1 action", "30 ft", "V, S, M", "1 minute", "$BARD,$CLERIC",
            "Three creatures subtract 1d4 from attack rolls and saving throws on a failed Charisma save.", conc = true),
        sp("bless", "Bless", 1, V, "1 action", "30 ft", "V, S, M", "1 minute", "$CLERIC,$PALADIN",
            "Three creatures add 1d4 to attack rolls and saving throws. One more target per slot level.", conc = true),
        sp("burning_hands", "Burning Hands", 1, V, "1 action", "Self (15 ft cone)", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Dexterity save or 3d6 fire damage, half on a success. +1d6 per slot level."),
        sp("charm_person", "Charm Person", 1, E, "1 action", "30 ft", "V, S", "1 hour", "$BARD,$DRUID,$SORCERER,$WARLOCK,$WIZARD",
            "Wisdom save or charmed; it knows it was charmed when the spell ends. One more target per slot level."),
        sp("command", "Command", 1, E, "1 action", "60 ft", "V", "1 round", "$CLERIC,$PALADIN",
            "Wisdom save or follow a one-word command such as approach, drop, flee, grovel or halt."),
        sp("comprehend_languages", "Comprehend Languages", 1, D, "1 action", "Self", "V, S, M", "1 hour", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Understand any spoken language and any written language you touch.", ritual = true),
        sp("create_or_destroy_water", "Create or Destroy Water", 1, T, "1 action", "30 ft", "V, S, M", "Instantaneous", "$CLERIC,$DRUID",
            "Create or destroy 10 gallons of water, or call rain in a 30 ft cube."),
        sp("cure_wounds", "Cure Wounds", 1, V, "1 action", "Touch", "V, S", "Instantaneous", "$BARD,$CLERIC,$DRUID,$PALADIN,$RANGER",
            "Restore 1d8 + spellcasting modifier hit points. +1d8 per slot level."),
        sp("detect_magic", "Detect Magic", 1, D, "1 action", "Self", "V, S", "10 minutes", "$BARD,$CLERIC,$DRUID,$PALADIN,$RANGER,$SORCERER,$WIZARD",
            "Sense magic within 30 ft and learn its school with an action.", conc = true, ritual = true),
        sp("disguise_self", "Disguise Self", 1, I, "1 action", "Self", "V, S", "1 hour", "$BARD,$SORCERER,$WIZARD",
            "Change your appearance; an Investigation check against your spell save DC sees through it."),
        sp("dissonant_whispers", "Dissonant Whispers", 1, E, "1 action", "60 ft", "V", "Instantaneous", BARD,
            "Wisdom save or 3d6 psychic damage and the target must use its reaction to move away. +1d6 per slot level."),
        sp("divine_favor", "Divine Favor", 1, V, "1 bonus action", "Self", "V, S", "1 minute", PALADIN,
            "Your weapon attacks deal an extra 1d4 radiant damage.", conc = true),
        sp("entangle", "Entangle", 1, C, "1 action", "90 ft", "V, S", "1 minute", DRUID,
            "Grasping weeds in a 20 ft square; Strength save or restrained.", conc = true),
        sp("expeditious_retreat", "Expeditious Retreat", 1, T, "1 bonus action", "Self", "V, S", "10 minutes", "$SORCERER,$WARLOCK,$WIZARD",
            "Dash as a bonus action on each of your turns.", conc = true),
        sp("faerie_fire", "Faerie Fire", 1, V, "1 action", "60 ft", "V", "1 minute", "$BARD,$DRUID",
            "Objects and creatures in a 20 ft cube are outlined; attacks against them have advantage and they cannot benefit from invisibility.", conc = true),
        sp("false_life", "False Life", 1, N, "1 action", "Self", "V, S, M", "1 hour", "$SORCERER,$WIZARD",
            "1d4 + 4 temporary hit points, +5 per slot level."),
        sp("feather_fall", "Feather Fall", 1, T, "1 reaction", "60 ft", "V, M", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "Up to five falling creatures descend 60 ft per round and take no falling damage."),
        sp("find_familiar", "Find Familiar", 1, C, "1 hour", "10 ft", "V, S, M", "Instantaneous", WIZARD,
            "A spirit in animal form that scouts, delivers touch spells and shares its senses with you.", ritual = true),
        sp("fog_cloud", "Fog Cloud", 1, C, "1 action", "120 ft", "V, S", "1 hour", "$DRUID,$RANGER,$SORCERER,$WIZARD",
            "A 20 ft radius sphere of heavily obscuring fog; the radius grows by 20 ft per slot level.", conc = true),
        sp("goodberry", "Goodberry", 1, T, "1 action", "Touch", "V, S, M", "Instantaneous", "$DRUID,$RANGER",
            "Ten berries; each restores 1 hit point and feeds a creature for a day."),
        sp("grease", "Grease", 1, C, "1 action", "60 ft", "V, S, M", "1 minute", WIZARD,
            "A 10 ft square of difficult terrain; Dexterity save or fall prone."),
        sp("guiding_bolt", "Guiding Bolt", 1, V, "1 action", "120 ft", "V, S", "1 round", CLERIC,
            "Ranged spell attack for 4d6 radiant damage; the next attack against the target has advantage. +1d6 per slot level."),
        sp("healing_word", "Healing Word", 1, V, "1 bonus action", "60 ft", "V", "Instantaneous", "$BARD,$CLERIC,$DRUID",
            "Restore 1d4 + spellcasting modifier hit points. +1d4 per slot level."),
        sp("hellish_rebuke", "Hellish Rebuke", 1, V, "1 reaction", "60 ft", "V, S", "Instantaneous", WARLOCK,
            "React to damage: Dexterity save or 2d10 fire damage, half on a success. +1d10 per slot level."),
        sp("heroism", "Heroism", 1, E, "1 action", "Touch", "V, S", "1 minute", "$BARD,$PALADIN",
            "Immune to fear and temporary hit points equal to your spellcasting modifier each turn.", conc = true),
        sp("hex", "Hex", 1, E, "1 bonus action", "90 ft", "V, S, M", "1 hour", WARLOCK,
            "Extra 1d6 necrotic damage on your hits against the target and disadvantage on one ability of your choice.", conc = true),
        sp("hunters_mark", "Hunter's Mark", 1, D, "1 bonus action", "90 ft", "V", "1 hour", RANGER,
            "Extra 1d6 damage on your weapon hits against the target and advantage on Perception and Survival to find it.", conc = true),
        sp("identify", "Identify", 1, D, "1 minute", "Touch", "V, S, M", "Instantaneous", "$BARD,$WIZARD",
            "Learn an item's properties, whether it requires attunement, and the spells affecting a creature.", ritual = true),
        sp("inflict_wounds", "Inflict Wounds", 1, N, "1 action", "Touch", "V, S", "Instantaneous", CLERIC,
            "Melee spell attack for 3d10 necrotic damage. +1d10 per slot level."),
        sp("jump", "Jump", 1, T, "1 action", "Touch", "V, S, M", "1 minute", "$DRUID,$RANGER,$SORCERER,$WIZARD",
            "The target's jump distance is tripled."),
        sp("longstrider", "Longstrider", 1, T, "1 action", "Touch", "V, S, M", "1 hour", "$BARD,$DRUID,$RANGER,$WIZARD",
            "The target's speed increases by 10 ft. One more target per slot level."),
        sp("mage_armor", "Mage Armor", 1, A, "1 action", "Touch", "V, S, M", "8 hours", "$SORCERER,$WIZARD",
            "An unarmoured creature's AC becomes 13 + its Dexterity modifier."),
        sp("magic_missile", "Magic Missile", 1, V, "1 action", "120 ft", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Three darts, each 1d4 + 1 force damage, which always hit. One more dart per slot level."),
        sp("protection_evil_good", "Protection from Evil and Good", 1, A, "1 action", "Touch", "V, S, M", "10 minutes", "$CLERIC,$PALADIN,$WARLOCK,$WIZARD",
            "Aberrations, celestials, elementals, fey, fiends and undead have disadvantage on attacks against the target, which cannot be charmed, frightened or possessed by them.", conc = true),
        sp("purify_food_drink", "Purify Food and Drink", 1, T, "1 action", "10 ft", "V, S", "Instantaneous", "$CLERIC,$DRUID,$PALADIN",
            "Remove poison and disease from food and drink in a 5 ft radius.", ritual = true),
        sp("sanctuary", "Sanctuary", 1, A, "1 bonus action", "30 ft", "V, S, M", "1 minute", "$CLERIC",
            "Attackers must make a Wisdom save or choose a new target; the spell ends if the warded creature attacks."),
        sp("shield", "Shield", 1, A, "1 reaction", "Self", "V, S", "1 round", "$SORCERER,$WIZARD",
            "+5 AC until your next turn, including against the triggering attack, and no damage from magic missile."),
        sp("shield_of_faith", "Shield of Faith", 1, A, "1 bonus action", "60 ft", "V, S, M", "10 minutes", "$CLERIC,$PALADIN",
            "+2 AC for the target.", conc = true),
        sp("silent_image", "Silent Image", 1, I, "1 action", "60 ft", "V, S, M", "10 minutes", "$BARD,$SORCERER,$WIZARD",
            "A purely visual illusion no larger than a 15 ft cube that you can move.", conc = true),
        sp("sleep", "Sleep", 1, E, "1 action", "90 ft", "V, S, M", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "5d8 hit points of creatures fall unconscious, lowest current hit points first. +2d8 per slot level."),
        sp("speak_with_animals", "Speak with Animals", 1, D, "1 action", "Self", "V, S", "10 minutes", "$BARD,$DRUID,$RANGER",
            "Comprehend and verbally communicate with beasts.", ritual = true),
        sp("thunderwave", "Thunderwave", 1, V, "1 action", "Self (15 ft cube)", "V, S", "Instantaneous", "$BARD,$DRUID,$SORCERER,$WIZARD",
            "Constitution save or 2d8 thunder damage and pushed 10 ft; half damage on a success. +1d8 per slot level."),
        sp("witch_bolt", "Witch Bolt", 1, V, "1 action", "30 ft", "V, S, M", "1 minute", "$SORCERER,$WARLOCK,$WIZARD",
            "Ranged spell attack for 1d12 lightning damage; sustain it for 1d12 each turn with an action.", conc = true),

        // ---------------- Level 2 ----------------
        sp("aid", "Aid", 2, A, "1 action", "30 ft", "V, S, M", "8 hours", "$CLERIC,$PALADIN",
            "Three creatures gain 5 maximum and current hit points; +5 per slot level."),
        sp("alter_self", "Alter Self", 2, T, "1 action", "Self", "V, S", "1 hour", "$SORCERER,$WIZARD",
            "Aquatic adaptation, change appearance, or natural weapons dealing 1d6.", conc = true),
        sp("animal_messenger", "Animal Messenger", 2, E, "1 action", "30 ft", "V, S, M", "24 hours", "$BARD,$DRUID,$RANGER",
            "Send a Tiny beast with a message to a place you describe.", ritual = true),
        sp("arcane_lock", "Arcane Lock", 2, A, "1 action", "Touch", "V, S, M", "Until dispelled", WIZARD,
            "A door or chest is locked; the DC to break or pick it rises by 10."),
        sp("barkskin", "Barkskin", 2, T, "1 action", "Touch", "V, S, M", "1 hour", "$DRUID,$RANGER",
            "The target's AC cannot be lower than 16.", conc = true),
        sp("blindness_deafness", "Blindness/Deafness", 2, N, "1 action", "30 ft", "V", "1 minute", "$BARD,$CLERIC,$SORCERER,$WIZARD",
            "Constitution save or blinded or deafened; one more target per slot level."),
        sp("blur", "Blur", 2, I, "1 action", "Self", "V", "1 minute", "$SORCERER,$WIZARD",
            "Attack rolls against you have disadvantage unless the attacker does not rely on sight.", conc = true),
        sp("branding_smite", "Branding Smite", 2, V, "1 bonus action", "Self", "V", "1 minute", PALADIN,
            "Your next weapon hit deals an extra 2d6 radiant damage and the target sheds light. +1d6 per slot level.", conc = true),
        sp("calm_emotions", "Calm Emotions", 2, E, "1 action", "60 ft", "V, S", "1 minute", "$BARD,$CLERIC",
            "Charisma save or suppress charm and fear, or make a creature indifferent in a 20 ft radius.", conc = true),
        sp("darkness", "Darkness", 2, V, "1 action", "60 ft", "V, M", "10 minutes", "$SORCERER,$WARLOCK,$WIZARD",
            "A 15 ft radius sphere of magical darkness that darkvision cannot penetrate.", conc = true),
        sp("darkvision", "Darkvision", 2, T, "1 action", "Touch", "V, S, M", "8 hours", "$DRUID,$RANGER,$SORCERER,$WIZARD",
            "The target gains darkvision out to 60 ft."),
        sp("detect_thoughts", "Detect Thoughts", 2, D, "1 action", "Self", "V, S, M", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "Read surface thoughts and probe deeper against a Wisdom save.", conc = true),
        sp("enhance_ability", "Enhance Ability", 2, T, "1 action", "Touch", "V, S, M", "1 hour", "$BARD,$CLERIC,$DRUID,$SORCERER",
            "Advantage on one ability's checks plus a secondary benefit; one more target per slot level.", conc = true),
        sp("enlarge_reduce", "Enlarge/Reduce", 2, T, "1 action", "30 ft", "V, S, M", "1 minute", "$SORCERER,$WIZARD",
            "Double or halve the target's size; ±1d4 weapon damage and advantage or disadvantage on Strength checks.", conc = true),
        sp("flaming_sphere", "Flaming Sphere", 2, C, "1 action", "60 ft", "V, S, M", "1 minute", "$DRUID,$WIZARD",
            "A 5 ft sphere you move 30 ft as a bonus action; Dexterity save or 2d6 fire damage. +1d6 per slot level.", conc = true),
        sp("gust_of_wind", "Gust of Wind", 2, V, "1 action", "Self (60 ft line)", "V, S, M", "1 minute", "$DRUID,$SORCERER,$WIZARD",
            "Strength save or pushed 15 ft; the line is difficult terrain against the wind.", conc = true),
        sp("heat_metal", "Heat Metal", 2, T, "1 action", "60 ft", "V, S, M", "1 minute", "$BARD,$DRUID",
            "2d8 fire damage immediately and again each turn as a bonus action; the holder may drop the object. +1d8 per slot level.", conc = true),
        sp("hold_person", "Hold Person", 2, E, "1 action", "60 ft", "V, S, M", "1 minute", "$BARD,$CLERIC,$DRUID,$SORCERER,$WARLOCK,$WIZARD",
            "A humanoid is paralysed on a failed Wisdom save, repeated at the end of each of its turns. One more target per slot level.", conc = true),
        sp("invisibility", "Invisibility", 2, I, "1 action", "Touch", "V, S, M", "1 hour", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "The target is invisible until it attacks or casts a spell. One more target per slot level.", conc = true),
        sp("knock", "Knock", 2, T, "1 action", "60 ft", "V", "Instantaneous", "$BARD,$SORCERER,$WIZARD",
            "Unlock one locked, stuck or barred object; a loud knock is heard 300 ft away."),
        sp("lesser_restoration", "Lesser Restoration", 2, A, "1 action", "Touch", "V, S", "Instantaneous", "$BARD,$CLERIC,$DRUID,$PALADIN,$RANGER",
            "End one disease or the blinded, deafened, paralysed or poisoned condition."),
        sp("levitate", "Levitate", 2, T, "1 action", "60 ft", "V, S, M", "10 minutes", "$SORCERER,$WIZARD",
            "Raise a creature or object up to 500 lb by 20 ft; move it 20 ft as an action.", conc = true),
        sp("magic_weapon", "Magic Weapon", 2, T, "1 bonus action", "Touch", "V, S", "1 hour", "$PALADIN,$WIZARD",
            "A weapon becomes magical with +1 to attack and damage; +2 at 4th level, +3 at 6th.", conc = true),
        sp("mirror_image", "Mirror Image", 2, I, "1 action", "Self", "V, S", "1 minute", "$SORCERER,$WARLOCK,$WIZARD",
            "Three duplicates; attacks may hit a duplicate instead (AC 10 + your DEX modifier)."),
        sp("misty_step", "Misty Step", 2, C, "1 bonus action", "Self", "V", "Instantaneous", "$SORCERER,$WARLOCK,$WIZARD",
            "Teleport up to 30 ft to an unoccupied space you can see."),
        sp("moonbeam", "Moonbeam", 2, V, "1 action", "120 ft", "V, S, M", "1 minute", DRUID,
            "A 5 ft radius beam; Constitution save or 2d10 radiant damage, and shapechangers revert. +1d10 per slot level.", conc = true),
        sp("pass_without_trace", "Pass without Trace", 2, A, "1 action", "Self", "V, S, M", "1 hour", "$DRUID,$RANGER",
            "You and allies within 30 ft gain +10 to Stealth and cannot be tracked.", conc = true),
        sp("ray_of_enfeeblement", "Ray of Enfeeblement", 2, N, "1 action", "60 ft", "V, S", "1 minute", "$WARLOCK,$WIZARD",
            "Ranged spell attack; the target deals half damage with Strength-based weapon attacks.", conc = true),
        sp("scorching_ray", "Scorching Ray", 2, V, "1 action", "120 ft", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Three rays, each a ranged spell attack for 2d6 fire damage. One more ray per slot level."),
        sp("see_invisibility", "See Invisibility", 2, D, "1 action", "Self", "V, S, M", "1 hour", "$BARD,$SORCERER,$WIZARD",
            "See invisible creatures and objects and into the Ethereal Plane."),
        sp("shatter", "Shatter", 2, V, "1 action", "60 ft", "V, S, M", "Instantaneous", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A 10 ft radius sphere; Constitution save or 3d8 thunder damage. +1d8 per slot level."),
        sp("silence", "Silence", 2, I, "1 action", "120 ft", "V, S", "10 minutes", "$BARD,$CLERIC,$RANGER",
            "No sound in a 20 ft radius; creatures inside are deafened and immune to thunder damage.", conc = true, ritual = true),
        sp("spider_climb", "Spider Climb", 2, T, "1 action", "Touch", "V, S, M", "1 hour", "$SORCERER,$WARLOCK,$WIZARD",
            "Climb walls and ceilings with free hands, climbing speed equal to walking speed.", conc = true),
        sp("spiritual_weapon", "Spiritual Weapon", 2, V, "1 bonus action", "60 ft", "V, S", "1 minute", CLERIC,
            "A floating weapon: melee spell attack for 1d8 + spellcasting modifier force damage; +1d8 per two slot levels."),
        sp("suggestion", "Suggestion", 2, E, "1 action", "30 ft", "V, M", "8 hours", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Wisdom save or follow a reasonable course of action you suggest.", conc = true),
        sp("web", "Web", 2, C, "1 action", "60 ft", "V, S, M", "1 hour", "$SORCERER,$WIZARD",
            "A 20 ft cube of webs; Dexterity save or restrained, Strength check to escape.", conc = true),
        sp("zone_of_truth", "Zone of Truth", 2, E, "1 action", "60 ft", "V, S", "10 minutes", "$BARD,$CLERIC,$PALADIN",
            "A 15 ft radius; creatures that fail a Charisma save cannot speak deliberate lies."),

        // ---------------- Level 3 ----------------
        sp("animate_dead", "Animate Dead", 3, N, "1 minute", "10 ft", "V, S, M", "Instantaneous", "$CLERIC,$WIZARD",
            "Create a skeleton or zombie under your command for 24 hours; reassert control with the spell."),
        sp("beacon_of_hope", "Beacon of Hope", 3, A, "1 action", "30 ft", "V, S", "1 minute", CLERIC,
            "Advantage on Wisdom and death saves, and maximum hit points from healing.", conc = true),
        sp("bestow_curse", "Bestow Curse", 3, N, "1 action", "Touch", "V, S", "1 minute", "$BARD,$CLERIC,$WIZARD",
            "Wisdom save or a curse: disadvantage on an ability, disadvantage against you, a wasted turn, or +1d8 necrotic from your attacks.", conc = true),
        sp("blink", "Blink", 3, T, "1 action", "Self", "V, S", "1 minute", "$SORCERER,$WIZARD",
            "Roll a d20 each turn; on 11 or higher you vanish to the Ethereal Plane until your next turn."),
        sp("call_lightning", "Call Lightning", 3, C, "1 action", "120 ft", "V, S", "10 minutes", DRUID,
            "A storm cloud; each turn call a bolt for 3d10 lightning damage on a failed Dexterity save. +1d10 per slot level.", conc = true),
        sp("counterspell", "Counterspell", 3, A, "1 reaction", "60 ft", "S", "Instantaneous", "$SORCERER,$WARLOCK,$WIZARD",
            "Interrupt a spell of 3rd level or lower automatically; higher spells require an ability check DC 10 + spell level."),
        sp("daylight", "Daylight", 3, V, "1 action", "60 ft", "V, S", "1 hour", "$CLERIC,$DRUID,$PALADIN,$RANGER,$SORCERER",
            "A 60 ft radius of bright light that dispels darkness created by spells of 3rd level or lower."),
        sp("dispel_magic", "Dispel Magic", 3, A, "1 action", "120 ft", "V, S", "Instantaneous", "$BARD,$CLERIC,$DRUID,$PALADIN,$SORCERER,$WARLOCK,$WIZARD",
            "End spells of 3rd level or lower; higher spells need an ability check DC 10 + spell level."),
        sp("fear", "Fear", 3, I, "1 action", "Self (30 ft cone)", "V, S, M", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Wisdom save or drop what you hold and be frightened, using your turns to run away.", conc = true),
        sp("fireball", "Fireball", 3, V, "1 action", "150 ft", "V, S, M", "Instantaneous", "$SORCERER,$WIZARD",
            "A 20 ft radius sphere; Dexterity save or 8d6 fire damage, half on a success. +1d6 per slot level."),
        sp("fly", "Fly", 3, T, "1 action", "Touch", "V, S, M", "10 minutes", "$SORCERER,$WARLOCK,$WIZARD",
            "Flying speed of 60 ft. One more target per slot level.", conc = true),
        sp("haste", "Haste", 3, T, "1 action", "30 ft", "V, S, M", "1 minute", "$SORCERER,$WIZARD",
            "Double speed, +2 AC, advantage on Dexterity saves and one extra limited action; lethargy when it ends.", conc = true),
        sp("hypnotic_pattern", "Hypnotic Pattern", 3, I, "1 action", "120 ft", "S, M", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A 30 ft cube; Wisdom save or charmed and incapacitated until damaged or shaken awake.", conc = true),
        sp("lightning_bolt", "Lightning Bolt", 3, V, "1 action", "Self (100 ft line)", "V, S, M", "Instantaneous", "$SORCERER,$WIZARD",
            "Dexterity save or 8d6 lightning damage, half on a success. +1d6 per slot level."),
        sp("magic_circle", "Magic Circle", 3, A, "1 minute", "10 ft", "V, S, M", "1 hour", "$CLERIC,$PALADIN,$WARLOCK,$WIZARD",
            "A 10 ft radius cylinder that keeps chosen creature types out or in."),
        sp("major_image", "Major Image", 3, I, "1 action", "120 ft", "V, S, M", "10 minutes", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "An illusion with sound, smell and temperature no larger than a 20 ft cube.", conc = true),
        sp("mass_healing_word", "Mass Healing Word", 3, V, "1 bonus action", "60 ft", "V", "Instantaneous", CLERIC,
            "Up to six creatures regain 1d4 + spellcasting modifier hit points. +1d4 per slot level."),
        sp("meld_into_stone", "Meld into Stone", 3, T, "1 action", "Touch", "V, S", "8 hours", "$CLERIC,$DRUID",
            "Step into a stone object large enough to contain you.", ritual = true),
        sp("plant_growth", "Plant Growth", 3, T, "1 action or 8 hours", "150 ft", "V, S", "Instantaneous", "$BARD,$DRUID,$RANGER",
            "Overgrow a 100 ft radius into difficult terrain, or enrich half a mile of land for a year."),
        sp("protection_from_energy", "Protection from Energy", 3, A, "1 action", "Touch", "V, S", "1 hour", "$CLERIC,$DRUID,$RANGER,$SORCERER,$WIZARD",
            "Resistance to acid, cold, fire, lightning or thunder damage.", conc = true),
        sp("remove_curse", "Remove Curse", 3, A, "1 action", "Touch", "V, S", "Instantaneous", "$CLERIC,$PALADIN,$WARLOCK,$WIZARD",
            "End all curses on a creature and break the attunement of a cursed item."),
        sp("revivify", "Revivify", 3, N, "1 action", "Touch", "V, S, M", "Instantaneous", "$CLERIC,$PALADIN",
            "Return a creature dead for no more than a minute to life with 1 hit point. Costs 300 gp of diamonds."),
        sp("sending", "Sending", 3, V, "1 action", "Unlimited", "V, S, M", "1 round", "$BARD,$CLERIC,$WIZARD",
            "Send a 25-word message to a creature you know anywhere; it can reply."),
        sp("sleet_storm", "Sleet Storm", 3, C, "1 action", "150 ft", "V, S, M", "1 minute", "$DRUID,$SORCERER,$WIZARD",
            "A 40 ft radius cylinder of freezing rain: difficult terrain, heavily obscured, Dexterity save or fall prone.", conc = true),
        sp("slow", "Slow", 3, T, "1 action", "120 ft", "V, S, M", "1 minute", "$SORCERER,$WIZARD",
            "Up to six creatures: halved speed, −2 AC and Dexterity saves, and only one action or bonus action per turn.", conc = true),
        sp("spirit_guardians", "Spirit Guardians", 3, C, "1 action", "Self (15 ft radius)", "V, S, M", "10 minutes", CLERIC,
            "Spirits slow enemies to half speed; Wisdom save or 3d8 radiant or necrotic damage, half on a success. +1d8 per slot level.", conc = true),
        sp("stinking_cloud", "Stinking Cloud", 3, C, "1 action", "90 ft", "V, S, M", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "A 20 ft radius sphere; Constitution save or waste the action retching.", conc = true),
        sp("tongues", "Tongues", 3, D, "1 action", "Touch", "V, M", "1 hour", "$BARD,$CLERIC,$SORCERER,$WARLOCK,$WIZARD",
            "Understand and be understood in any spoken language."),
        sp("vampiric_touch", "Vampiric Touch", 3, N, "1 action", "Self", "V, S", "1 minute", "$WARLOCK,$WIZARD",
            "Melee spell attack for 3d6 necrotic damage; you regain half as many hit points. +1d6 per slot level.", conc = true),
        sp("water_breathing", "Water Breathing", 3, T, "1 action", "30 ft", "V, S, M", "24 hours", "$DRUID,$RANGER,$SORCERER,$WIZARD",
            "Ten creatures can breathe underwater.", ritual = true),

        // ---------------- Level 4 ----------------
        sp("banishment", "Banishment", 4, A, "1 action", "60 ft", "V, S, M", "1 minute", "$CLERIC,$PALADIN,$SORCERER,$WARLOCK,$WIZARD",
            "Charisma save or banished to a harmless demiplane; extraplanar creatures stay away permanently. One more target per slot level.", conc = true),
        sp("blight", "Blight", 4, N, "1 action", "30 ft", "V, S", "Instantaneous", "$DRUID,$SORCERER,$WARLOCK,$WIZARD",
            "Constitution save or 8d8 necrotic damage; plants have disadvantage and take maximum damage. +1d8 per slot level."),
        sp("confusion", "Confusion", 4, E, "1 action", "90 ft", "V, S, M", "1 minute", "$BARD,$DRUID,$SORCERER,$WIZARD",
            "A 10 ft radius; Wisdom save or act randomly each turn. +5 ft radius per slot level.", conc = true),
        sp("dimension_door", "Dimension Door", 4, C, "1 action", "500 ft", "V", "Instantaneous", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Teleport yourself and one willing creature to a spot you can picture."),
        sp("freedom_of_movement", "Freedom of Movement", 4, A, "1 action", "Touch", "V, S, M", "1 hour", "$BARD,$CLERIC,$DRUID,$RANGER",
            "Ignore difficult terrain; immune to being restrained or paralysed by magic; escape grapples with 5 ft of movement."),
        sp("greater_invisibility", "Greater Invisibility", 4, I, "1 action", "Touch", "V, S", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "The target is invisible even while attacking and casting.", conc = true),
        sp("ice_storm", "Ice Storm", 4, V, "1 action", "300 ft", "V, S, M", "Instantaneous", "$DRUID,$SORCERER,$WIZARD",
            "A 20 ft radius cylinder: Dexterity save or 2d8 bludgeoning and 4d6 cold damage; the area becomes difficult terrain."),
        sp("polymorph", "Polymorph", 4, T, "1 action", "60 ft", "V, S, M", "1 hour", "$BARD,$DRUID,$SORCERER,$WIZARD",
            "Wisdom save or transform into a beast whose CR is no higher than the target's level.", conc = true),
        sp("stoneskin", "Stoneskin", 4, A, "1 action", "Touch", "V, S, M", "1 hour", "$DRUID,$RANGER,$SORCERER,$WIZARD",
            "Resistance to nonmagical bludgeoning, piercing and slashing damage.", conc = true),
        sp("wall_of_fire", "Wall of Fire", 4, V, "1 action", "120 ft", "V, S, M", "1 minute", "$DRUID,$SORCERER,$WIZARD",
            "A 60 ft long, 20 ft high wall; Dexterity save or 5d8 fire damage on one side. +1d8 per slot level.", conc = true),

        // ---------------- Level 5 ----------------
        sp("animate_objects", "Animate Objects", 5, T, "1 action", "120 ft", "V, S", "1 minute", "$BARD,$SORCERER,$WIZARD",
            "Animate up to ten objects that attack on your command.", conc = true),
        sp("cloudkill", "Cloudkill", 5, C, "1 action", "120 ft", "V, S", "10 minutes", "$SORCERER,$WIZARD",
            "A 20 ft radius of poisonous fog that moves 10 ft per turn; Constitution save or 5d8 poison damage. +1d8 per slot level.", conc = true),
        sp("cone_of_cold", "Cone of Cold", 5, V, "1 action", "Self (60 ft cone)", "V, S, M", "Instantaneous", "$SORCERER,$WIZARD",
            "Constitution save or 8d8 cold damage, half on a success; a creature killed becomes a frozen statue. +1d8 per slot level."),
        sp("flame_strike", "Flame Strike", 5, V, "1 action", "60 ft", "V, S, M", "Instantaneous", CLERIC,
            "A 10 ft radius column: Dexterity save or 4d6 fire and 4d6 radiant damage, half on a success."),
        sp("greater_restoration", "Greater Restoration", 5, A, "1 action", "Touch", "V, S, M", "Instantaneous", "$BARD,$CLERIC,$DRUID",
            "End one exhaustion level, charm, petrification, curse, ability score reduction or hit point maximum reduction."),
        sp("hold_monster", "Hold Monster", 5, E, "1 action", "90 ft", "V, S, M", "1 minute", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Wisdom save or paralysed, repeated at the end of each of its turns. One more target per slot level.", conc = true),
        sp("mass_cure_wounds", "Mass Cure Wounds", 5, V, "1 action", "60 ft", "V, S", "Instantaneous", "$BARD,$CLERIC,$DRUID",
            "Six creatures in a 30 ft radius regain 3d8 + spellcasting modifier hit points. +1d8 per slot level."),
        sp("raise_dead", "Raise Dead", 5, N, "1 hour", "Touch", "V, S, M", "Instantaneous", "$BARD,$CLERIC,$PALADIN",
            "Return a creature dead for up to 10 days to life with 1 hit point; −4 to rolls, fading over a few days. Costs 500 gp of diamonds."),
        sp("scrying", "Scrying", 5, D, "10 minutes", "Self", "V, S, M", "10 minutes", "$BARD,$CLERIC,$DRUID,$WARLOCK,$WIZARD",
            "Wisdom save or you see and hear a creature as if present.", conc = true),
        sp("telekinesis", "Telekinesis", 5, T, "1 action", "60 ft", "V, S", "10 minutes", "$SORCERER,$WIZARD",
            "Move a creature (contested check) or an object up to 1,000 lb by 30 ft each turn.", conc = true),
        sp("wall_of_force", "Wall of Force", 5, V, "1 action", "120 ft", "V, S, M", "10 minutes", WIZARD,
            "An invisible wall nothing can pass physically; immune to damage and only ended by disintegrate.", conc = true),

        // ---------------- Level 6 to 9 ----------------
        sp("chain_lightning", "Chain Lightning", 6, V, "1 action", "150 ft", "V, S, M", "Instantaneous", "$SORCERER,$WIZARD",
            "10d8 lightning damage to one target and three more within 30 ft; Dexterity save for half. One more target per slot level."),
        sp("disintegrate", "Disintegrate", 6, T, "1 action", "60 ft", "V, S, M", "Instantaneous", "$SORCERER,$WIZARD",
            "Dexterity save or 10d6 + 40 force damage; a creature reduced to 0 hit points turns to dust. +3d6 per slot level."),
        sp("heal", "Heal", 6, V, "1 action", "60 ft", "V, S", "Instantaneous", "$CLERIC,$DRUID",
            "Restore 70 hit points and end blindness, deafness and disease. +10 per slot level."),
        sp("true_seeing", "True Seeing", 6, D, "1 action", "Touch", "V, S, M", "1 hour", "$BARD,$CLERIC,$SORCERER,$WARLOCK,$WIZARD",
            "Truesight 120 ft: see invisible creatures, illusions, shapechangers and into the Ethereal Plane."),
        sp("delayed_blast_fireball", "Delayed Blast Fireball", 7, V, "1 action", "150 ft", "V, S, M", "1 minute", "$SORCERER,$WIZARD",
            "A bead that gains 1d6 each round, then explodes for 12d6 or more fire damage in a 20 ft radius.", conc = true),
        sp("finger_of_death", "Finger of Death", 7, N, "1 action", "60 ft", "V, S", "Instantaneous", "$SORCERER,$WARLOCK,$WIZARD",
            "Constitution save or 7d8 + 30 necrotic damage; a humanoid killed rises as a zombie under your control."),
        sp("plane_shift", "Plane Shift", 7, C, "1 action", "Touch", "V, S, M", "Instantaneous", "$CLERIC,$DRUID,$SORCERER,$WARLOCK,$WIZARD",
            "Transport up to eight willing creatures to another plane, or banish an unwilling one."),
        sp("teleport", "Teleport", 7, C, "1 action", "10 ft", "V", "Instantaneous", "$BARD,$SORCERER,$WIZARD",
            "Teleport a party to a destination on the same plane; familiarity determines the chance of mishap."),
        sp("dominate_monster", "Dominate Monster", 8, E, "1 action", "60 ft", "V, S", "1 hour", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "Wisdom save or charmed and under your command via a telepathic link.", conc = true),
        sp("power_word_stun", "Power Word Stun", 8, E, "1 action", "60 ft", "V", "Instantaneous", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A creature with 150 hit points or fewer is stunned until it succeeds on a Constitution save."),
        sp("sunburst", "Sunburst", 8, V, "1 action", "150 ft", "V, S, M", "Instantaneous", "$DRUID,$SORCERER,$WIZARD",
            "A 60 ft radius of brilliant light: Constitution save or 12d6 radiant damage and blinded for 1 minute."),
        sp("meteor_swarm", "Meteor Swarm", 9, V, "1 action", "1 mile", "V, S", "Instantaneous", "$SORCERER,$WIZARD",
            "Four 40 ft radius spheres: Dexterity save or 20d6 fire and 20d6 bludgeoning damage, half on a success."),
        sp("power_word_kill", "Power Word Kill", 9, E, "1 action", "60 ft", "V", "Instantaneous", "$BARD,$SORCERER,$WARLOCK,$WIZARD",
            "A creature with 100 hit points or fewer dies instantly."),
        sp("time_stop", "Time Stop", 9, T, "1 action", "Self", "V", "Instantaneous", "$SORCERER,$WIZARD",
            "1d4 + 1 turns in a row; the spell ends if you affect another creature or an object it carries."),
        sp("true_polymorph", "True Polymorph", 9, T, "1 action", "30 ft", "V, S, M", "1 hour", "$BARD,$WARLOCK,$WIZARD",
            "Transform a creature into another creature or an object, permanently if you concentrate for the full duration.", conc = true),
        sp("wish", "Wish", 9, C, "1 action", "Self", "V", "Instantaneous", "$SORCERER,$WIZARD",
            "Duplicate any spell of 8th level or lower, or alter reality at the risk of never casting wish again.")
    )

    val byKey = all.associateBy { it.key }

    fun forClass(classKey: String): List<Spell> = all.filter { classKey in it.classes }

    fun byLevel(spells: List<Spell>): Map<Int, List<Spell>> = spells.groupBy { it.level }.toSortedMap()

    /**
     * Cantrips known by class and level (SRD tables). Classes not listed learn none.
     */
    fun cantripsKnown(classKey: String, level: Int): Int = when (classKey) {
        "bard", "druid", "sorcerer", "warlock" -> when {
            level >= 10 -> if (classKey == "sorcerer") 6 else 4
            level >= 4 -> if (classKey == "sorcerer") 5 else 3
            else -> if (classKey == "sorcerer") 4 else 2
        }
        "cleric", "wizard" -> when {
            level >= 10 -> 5
            level >= 4 -> 4
            else -> 3
        }
        else -> 0
    }

    /** Spells known for classes that learn a fixed list (bard, ranger, sorcerer, warlock). */
    fun spellsKnown(classKey: String, level: Int): Int? = when (classKey) {
        "bard" -> intArrayOf(4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 15, 16, 18, 19, 19, 20, 22, 22, 22)[level.coerceIn(1, 20) - 1]
        "sorcerer" -> intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 12, 13, 13, 14, 14, 15, 15, 15, 15)[level.coerceIn(1, 20) - 1]
        "warlock" -> intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15)[level.coerceIn(1, 20) - 1]
        "ranger" -> if (level < 2) 0 else intArrayOf(0, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11)[level.coerceIn(1, 20) - 1]
        else -> null
    }

    /** Prepared-caster formula: ability modifier + (level / divisor), minimum 1. */
    fun spellsPrepared(classKey: String, level: Int, abilityModifier: Int): Int? = when (classKey) {
        "cleric", "druid", "wizard" -> maxOf(1, abilityModifier + level)
        "paladin", "artificer" -> maxOf(1, abilityModifier + level / 2)
        else -> null
    }
}

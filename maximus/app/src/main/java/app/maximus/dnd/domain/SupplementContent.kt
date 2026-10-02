package app.maximus.dnd.domain

/**
 * Content from books outside the System Reference Documents (Xanathar's Guide, Tasha's Cauldron, the
 * Player's Handbook beyond the SRD, Sword Coast Adventurer's Guide, Strixhaven). This is a private,
 * sideloaded app: every entry is a short summary of the game mechanic written in our own words, not
 * the book text, and is tagged [ContentSource.SUPPLEMENT] so it can always be told apart from the SRD.
 *
 * The selection serves the three favourite builds (Nuclear Wizard, Sorcadin, Sorlock) and their usual
 * alternatives; see [BuildGuides].
 */
private fun f(level: Int, name: String, text: String) = ClassFeature(level, name, text)

private val SUP = ContentSource.SUPPLEMENT

object SupplementSubclasses {
    private fun s(key: String, cls: String, name: String, vararg features: ClassFeature) = Subclass(key, cls, name, SUP, features.toList())

    val all: List<Subclass> = listOf(
        // ---------------- Warlock ----------------
        s("hexblade", "warlock", "The Hexblade",
            f(1, "Hexblade's Curse", "Bonus action: curse a creature within 30 ft for 1 minute. Against it you add your proficiency bonus to damage rolls, score critical hits on 19–20, and regain warlock level + CHA hit points (min. 1) when it dies. Once per short rest."),
            f(1, "Hex Warrior", "Proficiency with medium armour, shields and martial weapons. After a long rest, touch one one-handed weapon you are proficient with: it uses CHA instead of STR or DEX for attack and damage rolls. Applies to every pact weapon you summon."),
            f(1, "Expanded Spells", "Shield, wrathful smite (1st); blur, branding smite (2nd); blink, elemental weapon (3rd); phantasmal killer, staggering smite (4th); banishing smite, cone of cold (5th)."),
            f(6, "Accursed Specter", "When you slay a humanoid, raise its spirit as a specter with temporary hit points equal to half your warlock level; it adds your CHA modifier to its attacks. Once per long rest."),
            f(10, "Armor of Hexes", "When the target of your Hexblade's Curse hits you, roll a d6: on 4 or higher the attack misses."),
            f(14, "Master of Hexes", "When the cursed creature dies, move the curse to another creature within 30 ft without spending a use (no healing for that move).")),
        s("archfey", "warlock", "The Archfey",
            f(1, "Fey Presence", "Action: creatures in a 10 ft cube make a WIS save or are charmed or frightened until the end of your next turn. Once per short rest."),
            f(6, "Misty Escape", "Reaction when damaged: turn invisible and teleport up to 60 ft until the start of your next turn. Once per short rest."),
            f(10, "Beguiling Defenses", "Immune to charm; reflect a charm attempt back at its caster (WIS save)."),
            f(14, "Dark Delirium", "Action: a creature within 60 ft makes a WIS save or is charmed or frightened in an illusory realm for 1 minute (concentration).")),
        s("celestial", "warlock", "The Celestial",
            f(1, "Healing Light", "A pool of d6s equal to 1 + warlock level; bonus action to heal a creature within 60 ft with up to CHA modifier dice from the pool."),
            f(1, "Bonus Cantrips", "Light and sacred flame count as warlock cantrips."),
            f(6, "Radiant Soul", "Resistance to radiant damage; add CHA to one radiant or fire damage roll of a spell."),
            f(10, "Celestial Resilience", "After a rest you gain warlock level + CHA temporary hit points; up to five allies gain half that."),
            f(14, "Searing Vengeance", "When you make a death save, stand up with half your hit points instead and blind and burn nearby enemies (2d8 + CHA radiant). Once per long rest.")),

        // ---------------- Sorcerer ----------------
        s("divine_soul", "sorcerer", "Divine Soul",
            f(1, "Divine Magic", "Learn sorcerer spells from the cleric list as well; plus one affinity spell: cure wounds, inflict wounds, bless, bane or protection from evil and good."),
            f(1, "Favored by the Gods", "When you fail a save or miss with an attack, add 2d4 to the roll. Once per short rest."),
            f(6, "Empowered Healing", "Spend 1 sorcery point to reroll healing dice of yourself or an ally within 5 ft."),
            f(14, "Otherworldly Wings", "Bonus action: wings give you a flying speed of 30 ft."),
            f(18, "Unearthly Recovery", "Bonus action below half hit points: regain half your maximum. Once per long rest.")),
        s("shadow", "sorcerer", "Shadow Magic",
            f(1, "Eyes of the Dark", "Darkvision 120 ft. From 3rd level cast darkness with 2 sorcery points and see through it."),
            f(1, "Strength of the Grave", "When reduced to 0 hit points (not by radiant or a critical hit), make a CHA save (DC 5 + damage) to drop to 1 instead. Once per long rest."),
            f(6, "Hound of Ill Omen", "Bonus action, 3 sorcery points: a dire-wolf hound hunts one target; the target has disadvantage on saves against your spells while the hound is next to it."),
            f(14, "Shadow Walk", "Bonus action in dim light or darkness: teleport up to 120 ft to another dim or dark space."),
            f(18, "Umbral Form", "6 sorcery points: become shadow for 1 minute, resistance to all damage except force and radiant, move through creatures and objects.")),
        s("storm", "sorcerer", "Storm Sorcery",
            f(1, "Wind Speaker", "Speak, read and write Primordial."),
            f(1, "Tempestuous Magic", "Before or after casting a spell of 1st level or higher, fly 10 ft as a bonus action without provoking opportunity attacks."),
            f(6, "Heart of the Storm", "Resistance to lightning and thunder; casting a lightning or thunder spell deals half your sorcerer level as damage to chosen creatures within 10 ft."),
            f(6, "Storm Guide", "Control rain and wind direction around you."),
            f(14, "Storm's Fury", "Reaction when hit in melee: deal sorcerer level lightning damage, STR save or be pushed 20 ft."),
            f(18, "Wind Soul", "Immune to lightning and thunder, flying speed 60 ft; share flight with allies for an hour once per short rest.")),
        s("wild", "sorcerer", "Wild Magic",
            f(1, "Wild Magic Surge", "After casting a sorcerer spell of 1st level or higher the DM may have you roll a d20; on a 1 roll on the surge table."),
            f(1, "Tides of Chaos", "Gain advantage on one attack, check or save; it recharges on a long rest or when a surge happens."),
            f(6, "Bend Luck", "Reaction, 2 sorcery points: add or subtract 1d4 from another creature's attack, check or save."),
            f(14, "Controlled Chaos", "Roll twice on the surge table and choose."),
            f(18, "Spell Bombardment", "When you roll maximum on a damage die, roll it once more and add it (once per turn).")),
        s("clockwork", "sorcerer", "Clockwork Soul",
            f(1, "Clockwork Magic", "Always-known order spells: alarm, protection from evil and good, aid, lesser restoration, dispel magic, protection from energy, freedom of movement, summon construct, greater restoration, wall of force."),
            f(1, "Restore Balance", "Reaction: cancel advantage or disadvantage on a roll within 60 ft; PB times per long rest."),
            f(6, "Bastion of Law", "1–5 sorcery points create a ward of that many d8 that absorbs damage for one creature."),
            f(14, "Trance of Order", "Bonus action for 1 minute: attacks against you lack advantage and you treat d20 rolls below 10 as 10. Once per long rest or 5 sorcery points."),
            f(18, "Clockwork Cavalcade", "Action: 30 ft cube heals 100 hit points, repairs objects and ends spells of 6th level and lower.")),

        // ---------------- Paladin ----------------
        s("vengeance", "paladin", "Oath of Vengeance",
            f(3, "Channel Divinity: Vow of Enmity", "Bonus action: advantage on attack rolls against one creature within 10 ft for 1 minute."),
            f(3, "Channel Divinity: Abjure Enemy", "Action: a creature within 60 ft makes a WIS save or is frightened and its speed is 0 for 1 minute."),
            f(3, "Oath Spells", "Bane, hunter's mark, hold person, misty step, haste, protection from energy, banishment, dimension door, hold monster, scrying."),
            f(7, "Relentless Avenger", "After an opportunity attack hits, move half your speed without provoking."),
            f(15, "Soul of Vengeance", "Reaction: weapon attack against the target of your Vow of Enmity when it attacks."),
            f(20, "Avenging Angel", "For 1 hour: wings with 60 ft flight and a 30 ft aura of menace (WIS save or frightened, attacks against frightened foes have advantage).")),
        s("conquest", "paladin", "Oath of Conquest",
            f(3, "Channel Divinity: Conquering Presence", "Action: creatures within 30 ft make a WIS save or are frightened for 1 minute."),
            f(3, "Channel Divinity: Guided Strike", "Add +10 to one attack roll after seeing the roll."),
            f(3, "Oath Spells", "Armor of Agathys, command, hold person, spiritual weapon, bestow curse, fear, dominate beast, stoneskin, cloudkill, dominate person."),
            f(7, "Aura of Conquest", "Frightened creatures within 10 ft have speed 0 and take half your paladin level psychic damage at the start of their turns."),
            f(15, "Scornful Rebuke", "Creatures that hit you take CHA modifier psychic damage."),
            f(20, "Invincible Conqueror", "For 1 minute: resistance to all damage, an extra attack, and crits on 19–20.")),
        s("ancients", "paladin", "Oath of the Ancients",
            f(3, "Channel Divinity: Nature's Wrath", "Spectral vines restrain a creature (STR or DEX save)."),
            f(3, "Channel Divinity: Turn the Faithless", "Fey and fiends within 30 ft are turned."),
            f(7, "Aura of Warding", "You and allies within 10 ft resist damage from spells."),
            f(15, "Undying Sentinel", "Drop to 1 hit point instead of 0 once per long rest; you no longer age."),
            f(20, "Elder Champion", "For 1 minute: regain 10 hit points per turn, cast paladin spells as a bonus action, enemies nearby have disadvantage on saves against your spells.")),

        // ---------------- Wizard ----------------
        s("war_magic", "wizard", "War Magic",
            f(2, "Arcane Deflection", "Reaction: +2 AC against an attack or +4 to a failed save; afterwards only cantrips until the end of your next turn."),
            f(2, "Tactical Wit", "Add your INT modifier to initiative."),
            f(6, "Power Surge", "Store surges (max = INT modifier); when you dispel or counter a spell you gain one; spend one to add half your wizard level as force damage."),
            f(10, "Durable Magic", "+2 AC and +2 to all saves while concentrating."),
            f(14, "Deflecting Shroud", "Arcane Deflection also deals half your wizard level force damage to up to three creatures within 60 ft.")),
        s("bladesinging", "wizard", "Bladesinging",
            f(2, "Training in War and Song", "Light armour, one one-handed melee weapon, Performance."),
            f(2, "Bladesong", "Bonus action for 1 minute: + INT to AC and to concentration saves, +10 ft speed, advantage on Acrobatics. PB uses per long rest."),
            f(6, "Extra Attack", "Attack twice; one attack may be replaced by a cantrip."),
            f(10, "Song of Defense", "Reaction: spend a spell slot to reduce damage by five times its level."),
            f(14, "Song of Victory", "Add INT to the damage of melee weapon attacks while Bladesong is active.")),
        s("chronurgy", "wizard", "Chronurgy Magic",
            f(2, "Chronal Shift", "Reaction: force a reroll of an attack, check or save you can see; twice per long rest."),
            f(2, "Temporal Awareness", "Add your INT modifier to initiative."),
            f(6, "Momentary Stasis", "Action: Large or smaller creature makes a CON save or is incapacitated with speed 0 until your next turn."),
            f(10, "Arcane Abeyance", "Store a spell of 4th level or lower in a bead that anyone can trigger."),
            f(14, "Convergent Future", "Reaction: decide that an attack, check or save succeeds or fails; you gain a level of exhaustion.")),
        s("divination", "wizard", "School of Divination",
            f(2, "Portent", "After a long rest roll two d20s; replace any attack, check or save you see with one of them."),
            f(6, "Expert Divination", "Casting a divination spell of 2nd level or higher regains a lower slot."),
            f(10, "The Third Eye", "Darkvision, ethereal sight, read any language or see invisibility."),
            f(14, "Greater Portent", "Roll three d20s for Portent.")),

        // ---------------- Cleric ----------------
        s("tempest", "cleric", "Tempest Domain",
            f(1, "Bonus Proficiencies", "Martial weapons and heavy armour."),
            f(1, "Wrath of the Storm", "Reaction when hit in melee: 2d8 lightning or thunder damage (DEX save for half); WIS modifier uses per long rest."),
            f(1, "Domain Spells", "Fog cloud, thunderwave, gust of wind, shatter, call lightning, sleet storm, control water, ice storm, destructive wave, insect plague."),
            f(2, "Channel Divinity: Destructive Wrath", "When you roll lightning or thunder damage, deal the maximum instead of rolling."),
            f(6, "Thunderbolt Strike", "Lightning damage you deal to Large or smaller creatures can push them 10 ft."),
            f(8, "Divine Strike", "Once per turn +1d8 thunder on a weapon hit, 2d8 from 14th level."),
            f(17, "Stormborn", "Flying speed equal to your walking speed outdoors.")),
        s("war", "cleric", "War Domain",
            f(1, "Bonus Proficiencies", "Martial weapons and heavy armour."),
            f(1, "War Priest", "Bonus-action weapon attack after the Attack action; WIS modifier uses per long rest."),
            f(2, "Channel Divinity: Guided Strike", "+10 to one attack roll."),
            f(6, "Channel Divinity: War God's Blessing", "Reaction: +10 to an ally's attack roll within 30 ft."),
            f(8, "Divine Strike", "Once per turn +1d8 weapon damage, 2d8 from 14th level."),
            f(17, "Avatar of Battle", "Resistance to non-magical bludgeoning, piercing and slashing damage.")),

        // ---------------- Fighter ----------------
        s("eldritch_knight", "fighter", "Eldritch Knight",
            f(3, "Spellcasting", "Third-caster using INT; most spells from abjuration and evocation."),
            f(3, "Weapon Bond", "Bond with up to two weapons; summon one to your hand as a bonus action."),
            f(7, "War Magic", "After casting a cantrip, make one weapon attack as a bonus action."),
            f(10, "Eldritch Strike", "A creature you hit has disadvantage on its next save against your spell."),
            f(15, "Arcane Charge", "Teleport 30 ft when you use Action Surge."),
            f(18, "Improved War Magic", "After casting a spell, make one weapon attack as a bonus action."))
    )
}

object SupplementSpells {
    private const val SOR = "sorcerer"
    private const val WAR = "warlock"
    private const val WIZ = "wizard"

    private fun sp(
        key: String, name: String, level: Int, school: SpellSchool, time: String, range: String,
        comp: String, duration: String, classes: String, text: String, conc: Boolean = false
    ) = Spell(key, name, level, school, time, range, comp, duration, classes.split(",").map { it.trim() }.toSet(), text, conc, false, SUP)

    private val A = SpellSchool.ABJURATION
    private val C = SpellSchool.CONJURATION
    private val D = SpellSchool.DIVINATION
    private val E = SpellSchool.ENCHANTMENT
    private val V = SpellSchool.EVOCATION
    private val I = SpellSchool.ILLUSION
    private val N = SpellSchool.NECROMANCY
    private val T = SpellSchool.TRANSMUTATION

    val all: List<Spell> = listOf(
        // Cantrips
        sp("booming_blade", "Booming Blade", 0, V, "1 action", "Self (5 ft)", "S, M", "1 round", "$SOR,$WAR,$WIZ",
            "Melee weapon attack as part of the spell. On a hit the target is sheathed in thunder: if it willingly moves before your next turn it takes 1d8 thunder. At 5th level the hit adds 1d8 thunder and the move trigger 2d8, rising by 1d8 each at 11th and 17th."),
        sp("green_flame_blade", "Green-Flame Blade", 0, V, "1 action", "Self (5 ft)", "S, M", "Instantaneous", "$SOR,$WAR,$WIZ",
            "Melee weapon attack as part of the spell. On a hit, fire leaps to a second creature within 5 ft for spellcasting modifier fire damage. From 5th level the target takes +1d8 fire and the second creature 1d8 + modifier, rising at 11th and 17th."),
        sp("toll_the_dead", "Toll the Dead", 0, N, "1 action", "60 ft", "V, S", "Instantaneous", "cleric,$WAR,$WIZ",
            "WIS save or 1d8 necrotic damage, 1d12 if the target is already missing hit points. Scales at 5th, 11th and 17th level."),
        sp("mind_sliver", "Mind Sliver", 0, E, "1 action", "60 ft", "V", "1 round", "$SOR,$WAR,$WIZ",
            "INT save or 1d6 psychic damage and subtract 1d4 from the next save it makes before the end of your next turn. Scales like other cantrips."),
        sp("sword_burst", "Sword Burst", 0, C, "1 action", "Self (5 ft)", "V", "Instantaneous", "$SOR,$WAR,$WIZ",
            "Every creature within 5 ft makes a DEX save or takes 1d6 force damage. Scales at 5th, 11th and 17th level."),
        sp("lightning_lure", "Lightning Lure", 0, V, "1 action", "Self (15 ft)", "V", "Instantaneous", "$SOR,$WAR,$WIZ",
            "STR save or the target is pulled 10 ft toward you and takes 1d8 lightning if it ends within 5 ft. Scales at 5th, 11th and 17th level."),
        sp("frostbite", "Frostbite", 0, V, "1 action", "60 ft", "V, S", "Instantaneous", "druid,$SOR,$WAR,$WIZ",
            "CON save or 1d6 cold damage and disadvantage on the next weapon attack. Scales at 5th, 11th and 17th level."),
        sp("create_bonfire", "Create Bonfire", 0, C, "1 action", "60 ft", "V, S", "1 minute", "druid,$SOR,$WAR,$WIZ",
            "A 5 ft bonfire; creatures in it make a DEX save or take 1d8 fire. Scales at 5th, 11th and 17th level.", conc = true),

        // 1st level
        sp("absorb_elements", "Absorb Elements", 1, A, "1 reaction", "Self", "S", "1 round", "druid,ranger,$SOR,$WIZ",
            "When you take acid, cold, fire, lightning or thunder damage, gain resistance to that type until your next turn; your next melee hit adds 1d6 of that type (+1d6 per slot level above 1st)."),
        sp("silvery_barbs", "Silvery Barbs", 1, E, "1 reaction", "60 ft", "V", "Instantaneous", "bard,$SOR,$WIZ",
            "When a creature succeeds on an attack, check or save, force it to reroll and use the lower result; a different creature gains advantage on its next roll."),
        sp("chaos_bolt", "Chaos Bolt", 1, V, "1 action", "120 ft", "V, S", "Instantaneous", SOR,
            "Ranged spell attack: 2d8 + 1d6 damage of a type set by the d8s. If both d8s match, the bolt leaps to another creature. +1d6 per slot level above 1st."),
        sp("ice_knife", "Ice Knife", 1, C, "1 action", "60 ft", "S, M", "Instantaneous", "druid,$SOR,$WIZ",
            "Ranged spell attack for 1d10 piercing; hit or miss, it explodes: creatures within 5 ft make a DEX save or take 2d6 cold (+1d6 per slot level)."),
        sp("searing_smite", "Searing Smite", 1, V, "1 bonus action", "Self", "V", "1 minute", "paladin",
            "Your next weapon hit deals +1d6 fire and ignites the target (1d6 fire each turn until a CON save). +1d6 per slot level above 1st.", conc = true),
        sp("thunderous_smite", "Thunderous Smite", 1, V, "1 bonus action", "Self", "V", "1 minute", "paladin",
            "Your next weapon hit deals +2d6 thunder; STR save or pushed 10 ft and knocked prone.", conc = true),
        sp("wrathful_smite", "Wrathful Smite", 1, V, "1 bonus action", "Self", "V", "1 minute", "paladin,$WAR",
            "Your next weapon hit deals +1d6 psychic; WIS save or frightened until it uses its action on a WIS check.", conc = true),
        sp("catapult", "Catapult", 1, T, "1 action", "60 ft", "S", "Instantaneous", "$SOR,$WIZ",
            "Hurl an object of 1–5 lb up to 90 ft; a creature in its path makes a DEX save or takes 3d8 bludgeoning (+1d8 per slot level)."),

        // 2nd level
        sp("shadow_blade", "Shadow Blade", 2, I, "1 bonus action", "Self", "V, S", "1 minute", "$SOR,$WAR,$WIZ",
            "A finesse, light, thrown (20/60 ft) blade of shadow dealing 2d8 psychic; advantage in dim light or darkness. 3d8 with a 3rd–4th level slot, 4d8 with 5th–6th, 5d8 with 7th+.", conc = true),
        sp("find_steed", "Find Steed", 2, C, "10 minutes", "30 ft", "V, S", "Instantaneous", "paladin",
            "Summon an intelligent celestial, fey or fiendish steed; spells that target only you can also target it while you ride."),
        sp("dragons_breath", "Dragon's Breath", 2, T, "1 bonus action", "Touch", "V, S, M", "1 minute", "$SOR,$WIZ",
            "A willing creature can use its action to exhale a 15 ft cone of acid, cold, fire, lightning or poison: 3d6 on a failed DEX save, half on success. +1d6 per slot level.", conc = true),
        sp("mind_spike", "Mind Spike", 2, D, "1 action", "60 ft", "S", "1 hour", "$SOR,$WAR,$WIZ",
            "WIS save or 3d8 psychic (half on success); you know where the target is for the duration. +1d8 per slot level.", conc = true),

        // 3rd level
        sp("thunder_step", "Thunder Step", 3, C, "1 action", "90 ft", "V", "Instantaneous", "$SOR,$WAR,$WIZ",
            "Teleport up to 90 ft, optionally with a willing creature; everyone within 10 ft of where you left makes a CON save or takes 3d10 thunder (half on success). +1d10 per slot level."),
        sp("spirit_shroud", "Spirit Shroud", 3, N, "1 bonus action", "Self", "V, S", "1 minute", "cleric,paladin,$WAR,$WIZ",
            "Your attacks against creatures within 10 ft deal +1d8 radiant, necrotic or cold, and they cannot regain hit points; enemies starting their turn there lose 10 ft of speed. +1d8 per two slot levels above 3rd.", conc = true),
        sp("blinding_smite", "Blinding Smite", 3, V, "1 bonus action", "Self", "V", "1 minute", "paladin",
            "Your next weapon hit deals +3d8 radiant; CON save or blinded until it succeeds on a later save.", conc = true),
        sp("crusaders_mantle", "Crusader's Mantle", 3, V, "1 action", "Self", "V", "1 minute", "paladin",
            "Allies within 30 ft deal +1d4 radiant with weapon hits.", conc = true),
        sp("hunger_of_hadar", "Hunger of Hadar", 3, C, "1 action", "150 ft", "V, S, M", "1 minute", WAR,
            "A 20 ft sphere of blinding void: creatures starting their turn inside take 2d6 cold, those ending it inside make a DEX save or take 2d6 acid. +1d6 each per slot level.", conc = true),
        sp("melfs_minute_meteors", "Melf's Minute Meteors", 3, V, "1 action", "Self", "V, S, M", "10 minutes", "$SOR,$WIZ",
            "Six meteors orbit you; as a bonus action send one or two to points within 120 ft: 2d6 fire to each creature within 5 ft (DEX half). +2 meteors per slot level.", conc = true),
        sp("elemental_weapon", "Elemental Weapon", 3, T, "1 action", "Touch", "V, S", "1 hour", "paladin",
            "A weapon becomes magical with +1 to attack and +1d4 acid, cold, fire, lightning or thunder damage; +2/+2d4 at 5th–6th level slots, +3/+3d4 at 7th+.", conc = true),

        // 4th level
        sp("staggering_smite", "Staggering Smite", 4, V, "1 bonus action", "Self", "V", "1 minute", "paladin",
            "Your next weapon hit deals +4d6 psychic; WIS save or disadvantage on attacks and checks and no reactions until the end of its next turn.", conc = true),
        sp("sickening_radiance", "Sickening Radiance", 4, V, "1 action", "120 ft", "V, S", "10 minutes", "$SOR,$WAR,$WIZ",
            "A 30 ft sphere of dim green light: creatures in it make a CON save or take 4d10 radiant, gain a level of exhaustion and shed light that prevents invisibility.", conc = true),
        sp("storm_sphere", "Storm Sphere", 4, V, "1 action", "150 ft", "V, S", "1 minute", "$SOR,$WIZ",
            "A 20 ft sphere of whirling air: creatures ending a turn inside make a STR save or take 2d6 bludgeoning. Bonus action: lightning bolt from the centre, ranged spell attack for 4d6 lightning (advantage if the target is inside). +1d6 each per slot level.", conc = true),
        sp("shadow_of_moil", "Shadow of Moil", 4, N, "1 action", "Self", "V, S, M", "1 minute", WAR,
            "Flame-like shadows heavily obscure you, give resistance to radiant damage, and deal 2d8 necrotic to creatures that hit you within 10 ft.", conc = true),

        // 5th level
        sp("banishing_smite", "Banishing Smite", 5, A, "1 bonus action", "Self", "V", "1 minute", "paladin,$WAR",
            "Your next weapon hit deals +5d10 force; if that leaves the target at 50 hit points or fewer it is banished to a harmless demiplane for the duration.", conc = true),
        sp("synaptic_static", "Synaptic Static", 5, E, "1 action", "120 ft", "V, S", "Instantaneous", "bard,$SOR,$WAR,$WIZ",
            "20 ft sphere: INT save or 8d6 psychic (half on success); those who fail subtract 1d6 from attacks, checks and concentration saves for 1 minute."),
        sp("steel_wind_strike", "Steel Wind Strike", 5, C, "1 action", "30 ft", "S, M", "Instantaneous", "ranger,$WIZ",
            "Melee spell attacks against up to five creatures, 6d10 force each, then teleport next to one of them."),
        sp("far_step", "Far Step", 5, C, "1 bonus action", "Self", "V", "1 minute", "$SOR,$WAR,$WIZ",
            "Teleport up to 60 ft now and again as a bonus action on each later turn.", conc = true),
        sp("holy_weapon", "Holy Weapon", 5, V, "1 bonus action", "Touch", "V, S", "1 hour", "cleric,paladin",
            "A weapon deals +2d8 radiant on a hit; dismiss it in a 30 ft burst for 4d8 radiant (CON half) and blindness.", conc = true),

        // 6th–9th level
        sp("mental_prison", "Mental Prison", 6, I, "1 action", "60 ft", "S", "1 minute", "$SOR,$WAR,$WIZ",
            "INT save or 5d10 psychic and the target is restrained by an illusion; leaving it costs 10d10 psychic. Half damage and no prison on a success.", conc = true),
        sp("crown_of_stars", "Crown of Stars", 7, V, "1 action", "Self", "V, S", "1 hour", "$SOR,$WAR,$WIZ",
            "Seven motes orbit you; bonus action to send one as a ranged spell attack for 4d12 radiant. +2 motes per slot level above 7th."),
        sp("abi_dalzims_horrid_wilting", "Abi-Dalzim's Horrid Wilting", 8, N, "1 action", "150 ft", "V, S, M", "Instantaneous", "$SOR,$WIZ",
            "30 ft cube: CON save or 12d8 necrotic (half on success); plants and water elementals save with disadvantage and take maximum damage."),
        sp("psychic_scream", "Psychic Scream", 9, E, "1 action", "90 ft", "S", "Instantaneous", "bard,$SOR,$WAR,$WIZ",
            "Up to ten creatures with INT 3+ make an INT save or take 14d6 psychic and are stunned (half and no stun on a success); a creature killed this way has its head explode.")
    )
}

object SupplementFeats {
    private val MENTAL = listOf(Ability.INT, Ability.WIS, Ability.CHA)
    val all: List<Feat> = listOf(
        Feat("war_caster", "War Caster", "Ability to cast a spell",
            "Advantage on CON saves to keep concentration, cast with weapons or a shield in hand, and cast a single-target spell instead of an opportunity attack (e.g. quickened booming blade on the run).", source = SUP),
        Feat("resilient_con", "Resilient (Constitution)", "—",
            "+1 CON and proficiency in CON saving throws; the sheet adds the save automatically.",
            abilityChoice = listOf(Ability.CON), abilityAmount = 1, source = SUP),
        Feat("resilient_wis", "Resilient (Wisdom)", "—",
            "+1 WIS and proficiency in WIS saving throws; the sheet adds the save automatically.",
            abilityChoice = listOf(Ability.WIS), abilityAmount = 1, source = SUP),
        Feat("elemental_adept", "Elemental Adept", "Ability to cast a spell",
            "Choose acid, cold, fire, lightning or thunder: your spells ignore resistance to it, and damage dice of 1 count as 2.", source = SUP),
        Feat("spell_sniper", "Spell Sniper", "Ability to cast a spell",
            "Double the range of attack spells, ignore half and three-quarters cover, learn one attack cantrip.", source = SUP),
        Feat("great_weapon_master", "Great Weapon Master", "—",
            "Bonus-action attack after a crit or a kill; before a heavy-weapon attack take −5 to hit for +10 damage.", source = SUP),
        Feat("polearm_master", "Polearm Master", "—",
            "Bonus-action butt-end attack (1d4) with glaive, halberd, quarterstaff or spear; opportunity attacks when a creature enters your reach.", source = SUP),
        Feat("sentinel", "Sentinel", "—",
            "Opportunity attacks reduce speed to 0, ignore Disengage, and you strike when an adjacent foe attacks an ally.", source = SUP),
        Feat("fey_touched", "Fey Touched", "—",
            "+1 INT, WIS or CHA; learn misty step and one 1st-level divination or enchantment spell, each free once per long rest.",
            abilityChoice = MENTAL, abilityAmount = 1, source = SUP),
        Feat("shadow_touched", "Shadow Touched", "—",
            "+1 INT, WIS or CHA; learn invisibility and one 1st-level illusion or necromancy spell, each free once per long rest.",
            abilityChoice = MENTAL, abilityAmount = 1, source = SUP),
        Feat("metamagic_adept", "Metamagic Adept", "Spellcasting or Pact Magic",
            "Two Metamagic options and 2 sorcery points usable only for them, regained on a long rest.", source = SUP),
        Feat("eldritch_adept", "Eldritch Adept", "Spellcasting or Pact Magic",
            "Learn one Eldritch Invocation without a level prerequisite (e.g. Agonizing Blast needs eldritch blast).", source = SUP),
        Feat("mobile", "Mobile", "—",
            "+10 ft speed, Dash ignores difficult terrain, no opportunity attacks from creatures you attacked in melee.", source = SUP),
        Feat("inspiring_leader", "Inspiring Leader", "CHA 13",
            "10-minute speech gives up to six creatures temporary hit points equal to your level + CHA modifier.", source = SUP),
        Feat("telekinetic", "Telekinetic", "—",
            "+1 INT, WIS or CHA; invisible mage hand and a bonus-action shove of 5 ft (STR save).",
            abilityChoice = MENTAL, abilityAmount = 1, source = SUP),
        Feat("sharpshooter", "Sharpshooter", "—",
            "No disadvantage at long range, ignore half and three-quarters cover, −5 to hit for +10 damage with ranged weapons.", source = SUP),
        Feat("mage_slayer", "Mage Slayer", "—",
            "Reaction attack against adjacent casters, they have disadvantage on concentration saves, advantage on saves against adjacent casters.", source = SUP),
        Feat("piercer", "Piercer", "—",
            "+1 STR or DEX; reroll one piercing damage die per turn and add one die on a critical hit.",
            abilityChoice = listOf(Ability.STR, Ability.DEX), abilityAmount = 1, source = SUP)
    )
}

/** A class option chosen from a list: invocations, metamagic, pact boons, fighting styles. */
data class ClassOption(
    val key: String,
    val name: String,
    val text: String,
    val prerequisite: String = "—",
    /** Minimum level in the owning class. */
    val minLevel: Int = 1,
    val source: ContentSource = ContentSource.SRD51
)

object EldritchInvocations {
    private fun o(key: String, name: String, text: String, pre: String = "—", lvl: Int = 2, src: ContentSource = ContentSource.SRD51) =
        ClassOption(key, name, text, pre, lvl, src)

    val all: List<ClassOption> = listOf(
        o("agonizing_blast", "Agonizing Blast", "Add your CHA modifier to the damage of every eldritch blast beam.", "eldritch blast cantrip"),
        o("repelling_blast", "Repelling Blast", "Each eldritch blast beam that hits pushes the target 10 ft away.", "eldritch blast cantrip"),
        o("eldritch_spear", "Eldritch Spear", "Eldritch blast range becomes 300 ft.", "eldritch blast cantrip"),
        o("grasp_of_hadar", "Grasp of Hadar", "Once per turn an eldritch blast hit pulls the target 10 ft toward you.", "eldritch blast cantrip", src = SUP),
        o("lance_of_lethargy", "Lance of Lethargy", "Once per turn an eldritch blast hit reduces the target's speed by 10 ft.", "eldritch blast cantrip", src = SUP),
        o("armor_of_shadows", "Armor of Shadows", "Cast mage armour on yourself at will."),
        o("devils_sight", "Devil's Sight", "See normally in magical and non-magical darkness to 120 ft (great with the darkness spell)."),
        o("eldritch_mind", "Eldritch Mind", "Advantage on CON saves to keep concentration.", src = SUP),
        o("eldritch_sight", "Eldritch Sight", "Cast detect magic at will."),
        o("mask_of_many_faces", "Mask of Many Faces", "Cast disguise self at will."),
        o("misty_visions", "Misty Visions", "Cast silent image at will."),
        o("beguiling_influence", "Beguiling Influence", "Proficiency in Deception and Persuasion."),
        o("fiendish_vigor", "Fiendish Vigor", "Cast false life on yourself at will as a 1st-level spell."),
        o("gaze_of_two_minds", "Gaze of Two Minds", "Perceive through a willing humanoid's senses."),
        o("beast_speech", "Beast Speech", "Cast speak with animals at will."),
        o("book_of_ancient_secrets", "Book of Ancient Secrets", "Inscribe and cast rituals from any class list.", "Pact of the Tome"),
        o("voice_of_the_chain_master", "Voice of the Chain Master", "Communicate with and perceive through your familiar at any distance.", "Pact of the Chain"),
        o("improved_pact_weapon", "Improved Pact Weapon", "Pact weapon gains +1 to attack and damage and may be a bow or crossbow; it can serve as a focus.", "Pact of the Blade", src = SUP),
        o("eldritch_smite", "Eldritch Smite", "Once per turn on a pact weapon hit, spend a pact slot: +1d8 force plus 1d8 per slot level, and knock the target prone if it is Huge or smaller.", "Pact of the Blade", 5, SUP),
        o("thirsting_blade", "Thirsting Blade", "Attack twice with your pact weapon when you take the Attack action.", "Pact of the Blade", 5),
        o("one_with_shadows", "One with Shadows", "In dim light or darkness become invisible until you move or act.", lvl = 5),
        o("mire_the_mind", "Mire the Mind", "Cast slow once with a pact slot per long rest.", lvl = 5),
        o("sign_of_ill_omen", "Sign of Ill Omen", "Cast bestow curse once with a pact slot per long rest.", lvl = 5),
        o("tomb_of_levistus", "Tomb of Levistus", "Reaction: encase yourself in ice for 10 temporary hit points per warlock level; vulnerable to fire and speed 0 until your next turn. Once per short rest.", lvl = 5, src = SUP),
        o("maddening_hex", "Maddening Hex", "Bonus action: the target of your hex or hexblade's curse and creatures within 5 ft take CHA modifier psychic damage.", "Hex or a warlock curse", 5, SUP),
        o("bewitching_whispers", "Bewitching Whispers", "Cast compulsion once with a pact slot per long rest.", lvl = 7),
        o("dreadful_word", "Dreadful Word", "Cast confusion once with a pact slot per long rest.", lvl = 7),
        o("sculptor_of_flesh", "Sculptor of Flesh", "Cast polymorph once with a pact slot per long rest.", lvl = 7),
        o("relentless_hex", "Relentless Hex", "Bonus action: teleport 30 ft to a space within 5 ft of your cursed target.", "Hex or a warlock curse", 7, SUP),
        o("ascendant_step", "Ascendant Step", "Cast levitate on yourself at will.", lvl = 9),
        o("minions_of_chaos", "Minions of Chaos", "Cast conjure elemental once with a pact slot per long rest.", lvl = 9),
        o("otherworldly_leap", "Otherworldly Leap", "Cast jump on yourself at will.", lvl = 9),
        o("whispers_of_the_grave", "Whispers of the Grave", "Cast speak with dead at will.", lvl = 9),
        o("lifedrinker", "Lifedrinker", "Pact weapon hits deal extra necrotic damage equal to your CHA modifier.", "Pact of the Blade", 12),
        o("chains_of_carceri", "Chains of Carceri", "Cast hold monster at will on celestials, fiends or elementals.", "Pact of the Chain", 15),
        o("master_of_myriad_forms", "Master of Myriad Forms", "Cast alter self at will.", lvl = 15),
        o("visions_of_distant_realms", "Visions of Distant Realms", "Cast arcane eye at will.", lvl = 15),
        o("witch_sight", "Witch Sight", "See the true form of shapechangers and illusions within 30 ft.", lvl = 15),
        o("shroud_of_shadow", "Shroud of Shadow", "Cast invisibility at will.", lvl = 15, src = SUP)
    )
    val byKey = all.associateBy { it.key }

    /** Invocations known by warlock level (2014 table). */
    fun known(warlockLevel: Int): Int = when {
        warlockLevel >= 18 -> 8; warlockLevel >= 15 -> 7; warlockLevel >= 12 -> 6; warlockLevel >= 9 -> 5
        warlockLevel >= 7 -> 4; warlockLevel >= 5 -> 3; warlockLevel >= 2 -> 2; else -> 0
    }
}

object MetamagicOptions {
    private fun o(key: String, name: String, cost: String, text: String, src: ContentSource = ContentSource.SRD51) =
        ClassOption(key, name, "$cost. $text", minLevel = 3, source = src)

    val all: List<ClassOption> = listOf(
        o("quickened", "Quickened Spell", "2 sorcery points", "Cast a 1-action spell as a bonus action (you may still cast a cantrip with your action)."),
        o("twinned", "Twinned Spell", "Points = spell level (1 for a cantrip)", "A spell that targets one creature and not self also targets a second creature in range."),
        o("heightened", "Heightened Spell", "3 sorcery points", "One target has disadvantage on its first save against the spell."),
        o("subtle", "Subtle Spell", "1 sorcery point", "Cast without verbal or somatic components; it cannot be counterspelled by sight."),
        o("careful", "Careful Spell", "1 sorcery point", "Up to CHA modifier creatures automatically succeed on the spell's save."),
        o("distant", "Distant Spell", "1 sorcery point", "Double the range, or touch becomes 30 ft."),
        o("empowered", "Empowered Spell", "1 sorcery point", "Reroll up to CHA modifier damage dice; combinable with another metamagic."),
        o("extended", "Extended Spell", "1 sorcery point", "Double a duration of 1 minute or longer (max. 24 h)."),
        o("seeking", "Seeking Spell", "2 sorcery points", "Reroll a missed spell attack.", SUP),
        o("transmuted", "Transmuted Spell", "1 sorcery point", "Change acid, cold, fire, lightning, poison or thunder damage to another of these types.", SUP)
    )
    val byKey = all.associateBy { it.key }

    /** Metamagic options known by sorcerer level (2014 table). */
    fun known(sorcererLevel: Int): Int = when { sorcererLevel >= 17 -> 4; sorcererLevel >= 10 -> 3; sorcererLevel >= 3 -> 2; else -> 0 }
}

object PactBoons {
    val all: List<ClassOption> = listOf(
        ClassOption("blade", "Pact of the Blade", "Create a pact weapon in your hand as an action; you are proficient with it. Combined with Hex Warrior it uses CHA.", minLevel = 3),
        ClassOption("chain", "Pact of the Chain", "Find familiar with special forms (imp, pseudodragon, quasit, sprite); it can attack with its reaction.", minLevel = 3),
        ClassOption("tome", "Pact of the Tome", "A Book of Shadows with three cantrips from any class list.", minLevel = 3),
        ClassOption("talisman", "Pact of the Talisman", "The wearer adds 1d4 to a failed ability check; PB uses per long rest.", minLevel = 3, source = SUP)
    )
    val byKey = all.associateBy { it.key }
}

object FightingStyles {
    val all: List<ClassOption> = listOf(
        ClassOption("defense", "Defense", "+1 AC while wearing armour (applied on the sheet)."),
        ClassOption("dueling", "Dueling", "+2 damage with a one-handed melee weapon and no other weapon (applied on the sheet)."),
        ClassOption("great_weapon", "Great Weapon Fighting", "Reroll 1s and 2s on damage dice of two-handed or versatile melee weapons (≈ +1.33 per d6, +0.83 per d8… on average)."),
        ClassOption("archery", "Archery", "+2 to ranged weapon attack rolls (applied on the sheet)."),
        ClassOption("protection", "Protection", "Reaction with a shield: impose disadvantage on an attack against an adjacent ally."),
        ClassOption("two_weapon", "Two-Weapon Fighting", "Add your ability modifier to the off-hand attack's damage."),
        ClassOption("blessed_warrior", "Blessed Warrior", "Two cleric cantrips (e.g. toll the dead, guidance) using CHA.", source = SUP),
        ClassOption("blind_fighting", "Blind Fighting", "Blindsight 10 ft.", source = SUP),
        ClassOption("interception", "Interception", "Reaction: reduce damage to an adjacent ally by 1d10 + proficiency.", source = SUP)
    )
    val byKey = all.associateBy { it.key }
}

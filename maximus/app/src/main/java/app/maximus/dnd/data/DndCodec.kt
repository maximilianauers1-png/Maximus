package app.maximus.dnd.data

import app.maximus.dnd.domain.Ability
import app.maximus.dnd.domain.AttackEntry
import app.maximus.dnd.domain.CharacterBuild
import app.maximus.dnd.domain.ClassLevel
import app.maximus.dnd.domain.Currency
import app.maximus.dnd.domain.HomebrewEntry
import app.maximus.dnd.domain.HomebrewKind
import app.maximus.dnd.domain.HpMethod
import app.maximus.dnd.domain.InventoryItem
import app.maximus.dnd.domain.Monster
import app.maximus.dnd.domain.MonsterAction
import app.maximus.dnd.domain.MonsterType
import app.maximus.dnd.domain.ScoreMethod
import app.maximus.dnd.domain.Size
import app.maximus.dnd.domain.Skill
import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON serialisation for characters and monsters. Decoding never throws on unknown or malformed
 * fields: every value falls back to the default, so an entry written by a newer version still opens.
 */
object DndCodec {
    private const val VERSION = 1

    fun encode(b: CharacterBuild): String {
        val o = JSONObject()
        o.put("v", VERSION)
        o.put("name", b.name)
        o.put("player", b.player)
        o.put("race", b.raceKey)
        o.put("background", b.backgroundKey)
        o.put("alignment", b.alignment)
        o.put("classes", JSONArray().apply {
            b.classes.forEach { put(JSONObject().put("key", it.classKey).put("level", it.level).put("sub", it.subclassKey ?: JSONObject.NULL)) }
        })
        o.put("scores", abilityJson(b.baseScores))
        o.put("free", abilityJson(b.freeAbilityBonuses))
        o.put("asi", abilityJson(b.asiBonuses))
        o.put("method", b.scoreMethod.name)
        o.put("skills", strings(b.chosenSkills.map { it.name }))
        o.put("expertise", strings(b.expertise.map { it.name }))
        o.put("saveOverride", strings(b.saveProficiencyOverride.map { it.name }))
        o.put("armor", b.armorName)
        o.put("shield", b.shield)
        o.put("attacks", JSONArray().apply {
            b.attacks.forEach {
                put(JSONObject().put("w", it.weaponName).put("custom", it.customName).put("magic", it.magicBonus)
                    .put("two", it.twoHanded).put("extra", it.extraDamage).put("extraType", it.extraDamageType)
                    .put("prof", it.proficient).put("ability", it.useAbility?.name ?: JSONObject.NULL))
            }
        })
        o.put("inventory", JSONArray().apply {
            b.inventory.forEach {
                put(JSONObject().put("name", it.name).put("qty", it.quantity).put("w", it.weightLb)
                    .put("note", it.note).put("eq", it.equipped).put("att", it.attuned))
            }
        })
        o.put("currency", JSONObject().put("cp", b.currency.cp).put("sp", b.currency.sp)
            .put("ep", b.currency.ep).put("gp", b.currency.gp).put("pp", b.currency.pp))
        o.put("feats", strings(b.featKeys))
        o.put("invocations", strings(b.invocationKeys))
        o.put("metamagic", strings(b.metamagicKeys))
        o.put("pactBoon", b.pactBoon ?: JSONObject.NULL)
        o.put("styles", strings(b.fightingStyles))
        o.put("spells", strings(b.spellKeys))
        o.put("prepared", strings(b.preparedKeys))
        o.put("homebrew", JSONArray().apply {
            b.homebrew.forEach { put(JSONObject().put("kind", it.kind.name).put("name", it.name).put("text", it.text).put("effect", it.effect)) }
        })
        o.put("hpMethod", b.hpMethod.name)
        o.put("hpRolls", JSONArray().apply { b.hpRolls.forEach { put(it) } })
        o.put("bonusHp", b.bonusMaxHp)
        o.put("curHp", b.currentHp ?: JSONObject.NULL)
        o.put("tempHp", b.tempHp)
        o.put("hdSpent", b.hitDiceSpent)
        o.put("dSucc", b.deathSuccesses)
        o.put("dFail", b.deathFailures)
        o.put("inspiration", b.inspiration)
        o.put("exhaustion", b.exhaustion)
        o.put("languages", b.languages)
        o.put("tools", b.toolProficiencies)
        o.put("otherProf", b.otherProficiencies)
        o.put("personality", b.personality)
        o.put("ideals", b.ideals)
        o.put("bonds", b.bonds)
        o.put("flaws", b.flaws)
        o.put("appearance", b.appearance)
        o.put("backstory", b.backstory)
        o.put("notes", b.notes)
        return o.toString()
    }

    fun decodeCharacter(json: String): CharacterBuild {
        val d = CharacterBuild()
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return d
        val classes = o.optJSONArray("classes")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val c = arr.optJSONObject(i) ?: return@mapNotNull null
                ClassLevel(c.optString("key", "fighter"), c.optInt("level", 1).coerceIn(1, 20), c.optString("sub").ifBlank { null })
            }
        }?.takeIf { it.isNotEmpty() } ?: d.classes
        val attacks = o.optJSONArray("attacks")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val a = arr.optJSONObject(i) ?: return@mapNotNull null
                AttackEntry(
                    weaponName = a.optString("w", "Longsword"), customName = a.optString("custom", ""),
                    magicBonus = a.optInt("magic", 0).coerceIn(-5, 10), twoHanded = a.optBoolean("two", false),
                    extraDamage = a.optString("extra", ""), extraDamageType = a.optString("extraType", ""),
                    proficient = a.optBoolean("prof", true),
                    useAbility = Ability.entries.firstOrNull { it.name == a.optString("ability") }
                )
            }
        } ?: d.attacks
        val inventory = o.optJSONArray("inventory")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val it2 = arr.optJSONObject(i) ?: return@mapNotNull null
                InventoryItem(
                    name = it2.optString("name", ""), quantity = it2.optInt("qty", 1).coerceIn(0, 9999),
                    weightLb = it2.optDouble("w", 0.0).coerceIn(0.0, 1000.0), note = it2.optString("note", ""),
                    equipped = it2.optBoolean("eq", false), attuned = it2.optBoolean("att", false)
                )
            }
        } ?: emptyList()
        val homebrew = o.optJSONArray("homebrew")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val h = arr.optJSONObject(i) ?: return@mapNotNull null
                HomebrewEntry(
                    kind = enumOf(HomebrewKind.entries.toTypedArray(), h.optString("kind"), HomebrewKind.NOTE),
                    name = h.optString("name", ""), text = h.optString("text", ""), effect = h.optString("effect", "")
                )
            }
        } ?: emptyList()
        val cur = o.optJSONObject("currency")
        return CharacterBuild(
            name = o.optString("name", d.name),
            player = o.optString("player", ""),
            raceKey = o.optString("race", d.raceKey),
            classes = classes,
            backgroundKey = o.optString("background", d.backgroundKey),
            alignment = o.optString("alignment", ""),
            baseScores = abilityMap(o.optJSONObject("scores"), 10),
            scoreMethod = enumOf(ScoreMethod.entries.toTypedArray(), o.optString("method"), d.scoreMethod),
            freeAbilityBonuses = abilityMap(o.optJSONObject("free"), 0).filterValues { it != 0 },
            asiBonuses = abilityMap(o.optJSONObject("asi"), 0).filterValues { it != 0 },
            chosenSkills = skillSet(o.optJSONArray("skills")),
            expertise = skillSet(o.optJSONArray("expertise")),
            saveProficiencyOverride = o.optJSONArray("saveOverride")?.let { a ->
                (0 until a.length()).mapNotNull { i -> Ability.entries.firstOrNull { it.name == a.optString(i) } }.toSet()
            } ?: emptySet(),
            armorName = o.optString("armor", d.armorName),
            shield = o.optBoolean("shield", d.shield),
            attacks = attacks,
            inventory = inventory,
            currency = Currency(
                cp = cur?.optInt("cp") ?: 0, sp = cur?.optInt("sp") ?: 0, ep = cur?.optInt("ep") ?: 0,
                gp = cur?.optInt("gp") ?: 0, pp = cur?.optInt("pp") ?: 0
            ),
            featKeys = stringSet(o.optJSONArray("feats")),
            invocationKeys = stringSet(o.optJSONArray("invocations")),
            metamagicKeys = stringSet(o.optJSONArray("metamagic")),
            pactBoon = if (o.isNull("pactBoon")) null else o.optString("pactBoon").ifBlank { null },
            fightingStyles = stringSet(o.optJSONArray("styles")),
            spellKeys = stringSet(o.optJSONArray("spells")),
            preparedKeys = stringSet(o.optJSONArray("prepared")),
            homebrew = homebrew,
            hpMethod = enumOf(HpMethod.entries.toTypedArray(), o.optString("hpMethod"), d.hpMethod),
            hpRolls = o.optJSONArray("hpRolls")?.let { a -> (0 until a.length()).map { a.optInt(it) } } ?: emptyList(),
            bonusMaxHp = o.optInt("bonusHp", 0),
            currentHp = if (o.isNull("curHp")) null else o.optInt("curHp"),
            tempHp = o.optInt("tempHp", 0),
            hitDiceSpent = o.optInt("hdSpent", 0),
            deathSuccesses = o.optInt("dSucc", 0).coerceIn(0, 3),
            deathFailures = o.optInt("dFail", 0).coerceIn(0, 3),
            inspiration = o.optBoolean("inspiration", false),
            exhaustion = o.optInt("exhaustion", 0).coerceIn(0, 6),
            languages = o.optString("languages", ""),
            toolProficiencies = o.optString("tools", ""),
            otherProficiencies = o.optString("otherProf", ""),
            personality = o.optString("personality", ""),
            ideals = o.optString("ideals", ""),
            bonds = o.optString("bonds", ""),
            flaws = o.optString("flaws", ""),
            appearance = o.optString("appearance", ""),
            backstory = o.optString("backstory", ""),
            notes = o.optString("notes", "")
        )
    }

    fun encode(m: Monster): String {
        val o = JSONObject()
        o.put("v", VERSION)
        o.put("name", m.name)
        o.put("size", m.size.name)
        o.put("type", m.type.name)
        o.put("alignment", m.alignment)
        o.put("ac", m.armorClass)
        o.put("acNote", m.armorNote)
        o.put("hdCount", m.hitDiceCount)
        o.put("hdSize", m.hitDieSize)
        o.put("speed", m.speed)
        o.put("scores", JSONObject().apply { m.scores.forEach { (a, v) -> put(a.name, v) } })
        o.put("saves", JSONArray().apply { m.saveProficiencies.forEach { put(it.name) } })
        o.put("skills", JSONArray().apply { m.skillProficiencies.forEach { put(it.name) } })
        o.put("resist", m.damageResistances)
        o.put("immune", m.damageImmunities)
        o.put("condImmune", m.conditionImmunities)
        o.put("senses", m.senses)
        o.put("languages", m.languages)
        o.put("legendary", m.legendaryActions)
        o.put("notes", m.notes)
        o.put("actions", JSONArray().apply {
            m.actions.forEach {
                put(JSONObject().put("name", it.name).put("bonus", it.attackBonus ?: JSONObject.NULL).put("reach", it.reach)
                    .put("damage", it.damageExpression).put("type", it.damageType).put("desc", it.description).put("count", it.attacksPerRound))
            }
        })
        o.put("traits", JSONArray().apply { m.traits.forEach { put(JSONObject().put("name", it.first).put("text", it.second)) } })
        return o.toString()
    }

    fun decodeMonster(json: String): Monster {
        val d = Monster()
        val o = runCatching { JSONObject(json) }.getOrNull() ?: return d
        val actions = o.optJSONArray("actions")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val a = arr.optJSONObject(i) ?: return@mapNotNull null
                MonsterAction(
                    a.optString("name", "Angriff"),
                    if (a.isNull("bonus")) null else a.optInt("bonus"),
                    a.optString("reach", "1,5 m"),
                    a.optString("damage", "1d6"),
                    a.optString("type", "Wucht"),
                    a.optString("desc", ""),
                    a.optInt("count", 1).coerceIn(1, 10)
                )
            }
        } ?: emptyList()
        val traits = o.optJSONArray("traits")?.let { arr ->
            (0 until arr.length()).mapNotNull { i ->
                val t = arr.optJSONObject(i) ?: return@mapNotNull null
                t.optString("name") to t.optString("text")
            }
        } ?: emptyList()
        return Monster(
            name = o.optString("name", d.name),
            size = enumOf(Size.entries.toTypedArray(), o.optString("size"), d.size),
            type = enumOf(MonsterType.entries.toTypedArray(), o.optString("type"), d.type),
            alignment = o.optString("alignment", d.alignment),
            armorClass = o.optInt("ac", d.armorClass).coerceIn(1, 40),
            armorNote = o.optString("acNote", ""),
            hitDiceCount = o.optInt("hdCount", d.hitDiceCount).coerceIn(1, 100),
            hitDieSize = o.optInt("hdSize", d.hitDieSize).coerceIn(4, 20),
            speed = o.optString("speed", d.speed),
            scores = abilityMap(o.optJSONObject("scores"), 10),
            saveProficiencies = o.optJSONArray("saves")?.let { a -> (0 until a.length()).mapNotNull { i -> Ability.entries.firstOrNull { it.name == a.optString(i) } }.toSet() } ?: emptySet(),
            skillProficiencies = skillSet(o.optJSONArray("skills")),
            damageResistances = o.optString("resist", ""),
            damageImmunities = o.optString("immune", ""),
            conditionImmunities = o.optString("condImmune", ""),
            senses = o.optString("senses", ""),
            languages = o.optString("languages", ""),
            actions = actions,
            traits = traits,
            legendaryActions = o.optInt("legendary", 0).coerceIn(0, 5),
            notes = o.optString("notes", "")
        )
    }

    private fun abilityJson(m: Map<Ability, Int>) = JSONObject().apply { m.forEach { (a, v) -> put(a.name, v) } }

    private fun strings(values: Collection<String>) = JSONArray().apply { values.forEach { put(it) } }

    private fun stringSet(a: JSONArray?): Set<String> =
        a?.let { arr -> (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotBlank() }.toSet() } ?: emptySet()

    private fun abilityMap(o: JSONObject?, default: Int): Map<Ability, Int> =
        Ability.entries.associateWith { a -> o?.optInt(a.name, default) ?: default }

    private fun skillSet(a: JSONArray?): Set<Skill> =
        a?.let { arr -> (0 until arr.length()).mapNotNull { i -> Skill.entries.firstOrNull { it.name == arr.optString(i) } }.toSet() } ?: emptySet()

    private fun <E : Enum<E>> enumOf(values: Array<E>, name: String, default: E): E =
        values.firstOrNull { it.name == name } ?: default
}

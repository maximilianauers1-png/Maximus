@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.domain.Ability
import app.maximus.dnd.domain.AttackEntry
import app.maximus.dnd.domain.CharacterBuild
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.dnd.domain.CharacterSheet
import app.maximus.dnd.domain.ClassLevel
import app.maximus.dnd.domain.HomebrewEntry
import app.maximus.dnd.domain.HomebrewKind
import app.maximus.dnd.domain.HpMethod
import app.maximus.dnd.domain.InventoryItem
import app.maximus.dnd.domain.Rules
import app.maximus.dnd.domain.ScoreMethod
import app.maximus.dnd.domain.Skill
import app.maximus.dnd.domain.Spells
import app.maximus.dnd.domain.SrdBackgrounds
import app.maximus.dnd.domain.SrdClasses
import app.maximus.dnd.domain.SrdEquipment
import app.maximus.dnd.domain.SrdFeats
import app.maximus.dnd.domain.SrdRaces
import app.maximus.dnd.domain.SrdSubclasses
import app.maximus.ui.components.MaximusTopBar
import kotlin.random.Random
import kotlinx.coroutines.launch

private val EDITOR_TABS = listOf("Basics", "Classes", "Abilities", "Skills", "Gear", "Spells", "Feats", "Homebrew", "Story")

fun skillName(s: Skill): String = s.name.split('_').joinToString(" ") { part ->
    part.lowercase().replaceFirstChar { it.uppercase() }
}

@Composable
fun CharacterEditorScreen(services: AppServices, characterId: Long, onBack: () -> Unit, onOpenSheet: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    var build by remember { mutableStateOf<CharacterBuild?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var savedId by remember { mutableStateOf(characterId) }
    var tab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(characterId) {
        build = if (characterId == 0L) CharacterBuild()
        else services.dnd.character(characterId)?.let { DndCodec.decodeCharacter(it.payload) } ?: CharacterBuild()
        loaded = true
    }

    val b = build
    val sheet = remember(b) { b?.let { CharacterBuilder.build(it) } }

    fun save(andOpen: Boolean) {
        val current = build ?: return
        val s = CharacterBuilder.build(current)
        scope.launch {
            val id = services.dnd.saveCharacter(savedId, current.name.ifBlank { "Unnamed" }, summaryOf(s), DndCodec.encode(current))
            savedId = id
            if (andOpen) onOpenSheet(id) else onBack()
        }
    }

    Scaffold(
        topBar = {
            MaximusTopBar(
                title = if (characterId == 0L) "New character" else "Edit character",
                onBack = onBack,
                actions = { TextButton(onClick = { save(false) }) { Text("Save") } }
            )
        }
    ) { padding ->
        if (!loaded || b == null || sheet == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SummaryStrip(sheet)
            ScrollableTabRow(
                selectedTabIndex = tab, edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
            ) {
                EDITOR_TABS.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
            }
            val set: ((CharacterBuild) -> CharacterBuild) -> Unit = { transform -> build = transform(b) }
            when (tab) {
                0 -> BasicsSection(b, sheet, set) { save(true) }
                1 -> ClassesSection(b, set)
                2 -> AbilitiesSection(b, sheet, set)
                3 -> SkillsSection(b, sheet, set)
                4 -> GearSection(b, sheet, set)
                5 -> SpellsSection(b, set)
                6 -> FeatsSection(b, sheet, set)
                7 -> HomebrewSection(b, set)
                else -> StorySection(b, set)
            }
        }
    }
}

/** Always-visible summary so the consequences of every choice are immediately readable. */
@Composable
private fun SummaryStrip(s: CharacterSheet) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatBox("Level", "${s.build.totalLevel}", "prof ${sign(s.proficiency)}", modifier = Modifier.weight(1f))
        StatBox("HP", "${s.maxHp}", null, modifier = Modifier.weight(1f))
        StatBox("AC", "${s.armorClass}", null, modifier = Modifier.weight(1f))
        StatBox("Init", sign(s.initiative), null, modifier = Modifier.weight(1f))
        StatBox("Speed", "${s.speed}", "ft", modifier = Modifier.weight(1f))
    }
}

// ---------------------------------------------------------------- Basics

@Composable
private fun BasicsSection(b: CharacterBuild, s: CharacterSheet, set: ((CharacterBuild) -> CharacterBuild) -> Unit, onOpenSheet: () -> Unit) {
    var raceGroup by rememberSaveable { mutableStateOf<String?>(null) }
    val groups = SrdRaces.groups
    val racesInGroup = SrdRaces.all.filter { raceGroup == null || it.group == raceGroup }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Name and identity") {
                TextFieldRow("Character name", b.name) { v -> set { it.copy(name = v) } }
                TextFieldRow("Player", b.player) { v -> set { it.copy(player = v) } }
                TextFieldRow("Alignment", b.alignment) { v -> set { it.copy(alignment = v) } }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(onClick = onOpenSheet) { Text("Save and open sheet") }
                    OutlinedButton(onClick = {
                        val random = CharacterBuilder.random(Random(System.nanoTime()), level = b.totalLevel)
                        set { random.copy(name = it.name, player = it.player) }
                    }) { Text("Randomise") }
                }
            }
        }
        item {
            DndCard("Species") {
                Text("Filter by lineage", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(raceGroup == null, { raceGroup = null }, label = { Text("All") })
                    groups.forEach { g -> FilterChip(raceGroup == g, { raceGroup = g }, label = { Text(g) }) }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    racesInGroup.forEach { r ->
                        FilterChip(b.raceKey == r.key, { set { it.copy(raceKey = r.key, freeAbilityBonuses = emptyMap()) } },
                            label = { Text(r.name) })
                    }
                }
                val race = s.race
                StatRow("Source", race.source.label)
                StatRow("Size and speed", "${race.size.name.lowercase().replaceFirstChar { it.uppercase() }}, ${race.speed} ft")
                if (race.darkvision > 0) StatRow("Darkvision", "${race.darkvision} ft")
                if (race.abilityBonuses.isNotEmpty()) {
                    StatRow("Fixed increases", race.abilityBonuses.entries.joinToString(", ") { "${it.key.short} +${it.value}" })
                }
                if (race.freeAbilityPoints > 0) {
                    StatRow("Free increases", "${race.freeAbilityPoints} points, max +${race.freeAbilityCap} per ability")
                    FreeAbilityPicker(b, race.freeAbilityPoints, race.freeAbilityCap, set)
                }
                race.traits.forEach { trait ->
                    Text(trait.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    BodyText(trait.text, muted = true)
                }
            }
        }
        item {
            DndCard("Background") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SrdBackgrounds.all.forEach { bg ->
                        FilterChip(b.backgroundKey == bg.key, { set { it.copy(backgroundKey = bg.key) } }, label = { Text(bg.name) })
                    }
                }
                val bg = s.background
                StatRow("Skills", bg.skills.joinToString(", ") { skillName(it) })
                StatRow("Tools", bg.tools)
                StatRow("Languages", "${bg.languages}")
                BodyText(bg.feature, modifier = Modifier.padding(top = 6.dp))
                BodyText("Equipment: ${bg.equipment}", muted = true)
            }
        }
    }
}

@Composable
private fun FreeAbilityPicker(b: CharacterBuild, points: Int, cap: Int, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    val used = b.freeAbilityBonuses.values.sum()
    Text("Assigned $used of $points", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
    Ability.entries.forEach { a ->
        val v = b.freeAbilityBonuses[a] ?: 0
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(a.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Stepper(v, { nv ->
                val delta = nv - v
                if (delta > 0 && used + delta > points) return@Stepper
                set { it.copy(freeAbilityBonuses = (it.freeAbilityBonuses + (a to nv)).filterValues { x -> x > 0 }) }
            }, min = 0, max = cap)
        }
    }
}

// ---------------------------------------------------------------- Classes

@Composable
private fun ClassesSection(b: CharacterBuild, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    var adding by remember { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Classes and levels") {
                StatRow("Total level", "${b.totalLevel} of 20", emphasise = true)
                BodyText(
                    "Add a second or third class for multiclass builds. Each class needs its minimum ability scores; " +
                        "the rule check on the sheet tells you when one is missing.", muted = true
                )
                Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Add a class") }
            }
        }
        items(b.classes) { cl ->
            val c = SrdClasses.byKey[cl.classKey] ?: return@items
            val subclasses = SrdSubclasses.byClass[cl.classKey].orEmpty()
            DndCard(c.name) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Level ${cl.level}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    if (b.classes.size > 1) {
                        TextButton(onClick = { set { it.copy(classes = it.classes.filter { x -> x.classKey != cl.classKey }) } }) { Text("Remove") }
                    }
                }
                Slider(
                    value = cl.level.toFloat(),
                    onValueChange = { v ->
                        set { bb ->
                            bb.copy(classes = bb.classes.map { if (it.classKey == cl.classKey) it.copy(level = v.toInt()) else it })
                        }
                    },
                    valueRange = 1f..20f, steps = 18
                )
                StatRow("Hit die", "d${c.hitDie.sides}")
                StatRow("Saving throws", c.savingThrows.joinToString(", ") { it.name })
                StatRow("Armour", c.armor)
                StatRow("Weapons", c.weapons)
                if (c.tools != "—") StatRow("Tools", c.tools)
                if (c.spellcasting != null) {
                    StatRow("Spellcasting", "${c.spellcasting.name}${if (c.pactMagic) " (pact magic)" else ""}")
                }
                if (c.multiclassRequirement.isNotEmpty()) {
                    StatRow("Multiclass requirement", c.multiclassRequirement.entries.joinToString(", ") { "${it.key.short} ${it.value}" })
                }
                StatRow("ASI at levels", c.asiLevels.joinToString(", "))
                if (subclasses.isNotEmpty()) {
                    Text(
                        "${c.subclassLabel} (from level ${c.subclassLevel})",
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(cl.subclassKey == null, {
                            set { bb -> bb.copy(classes = bb.classes.map { if (it.classKey == cl.classKey) it.copy(subclassKey = null) else it }) }
                        }, label = { Text("None") })
                        subclasses.forEach { sc ->
                            FilterChip(cl.subclassKey == sc.key, {
                                set { bb -> bb.copy(classes = bb.classes.map { if (it.classKey == cl.classKey) it.copy(subclassKey = sc.key) else it }) }
                            }, label = { Text("${sc.name} (${sc.source.label})") })
                        }
                    }
                    cl.subclassKey?.let { k ->
                        SrdSubclasses.byKey[k]?.features?.filter { it.level <= cl.level }?.forEach { feat ->
                            Text("${feat.level}. ${feat.name}", style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                            BodyText(feat.text, muted = true)
                        }
                    }
                    BodyText(
                        "Only the subclasses released under the open licences are listed. Add any other subclass as a " +
                            "homebrew entry; its features then appear on the sheet.", muted = true
                    )
                }
                Text("Features up to level ${cl.level}", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                c.features.filter { it.level <= cl.level }.forEach { feat ->
                    Text("${feat.level}. ${feat.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                    BodyText(feat.text, muted = true)
                }
            }
        }
    }

    if (adding) {
        AlertDialog(
            onDismissRequest = { adding = false },
            title = { Text("Add a class") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SrdClasses.all.filter { c -> b.classes.none { it.classKey == c.key } }.forEach { c ->
                        TextButton(
                            onClick = {
                                set { it.copy(classes = it.classes + ClassLevel(c.key, 1)) }
                                adding = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(c.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "d${c.hitDie.sides} · " + c.multiclassRequirement.entries.joinToString(", ") { "${it.key.short} ${it.value}" }
                                        .ifBlank { "no requirement" },
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { adding = false }) { Text("Close") } }
        )
    }
}

// ---------------------------------------------------------------- Abilities

@Composable
private fun AbilitiesSection(b: CharacterBuild, s: CharacterSheet, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    val pointCost = Rules.pointBuyCost(b.baseScores.values)
    val asiEarned = CharacterBuilder.asiCount(b.classes)
    val asiUsed = b.asiBonuses.values.sum()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Generation method") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ScoreMethod.entries.forEach { m ->
                        FilterChip(b.scoreMethod == m, {
                            set { bb ->
                                when (m) {
                                    ScoreMethod.STANDARD_ARRAY -> bb.copy(scoreMethod = m, baseScores = CharacterBuilder.assignScores(Rules.STANDARD_ARRAY, bb.mainClassKey))
                                    ScoreMethod.ROLL_4D6 -> bb.copy(scoreMethod = m, baseScores = CharacterBuilder.assignScores(CharacterBuilder.rollScores(), bb.mainClassKey))
                                    else -> bb.copy(scoreMethod = m)
                                }
                            }
                        }, label = { Text(m.label) })
                    }
                }
                when (b.scoreMethod) {
                    ScoreMethod.POINT_BUY -> {
                        if (pointCost == null) {
                            Text("Point buy allows scores from 8 to 15 only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        } else {
                            StatRow("Points spent", "$pointCost of ${Rules.POINT_BUY_BUDGET}", emphasise = pointCost > Rules.POINT_BUY_BUDGET)
                            BodyText("Costs: 8→0, 9→1, 10→2, 11→3, 12→4, 13→5, 14→7, 15→9.", muted = true)
                        }
                    }
                    ScoreMethod.ROLL_4D6 -> {
                        OutlinedButton(onClick = {
                            set { bb -> bb.copy(baseScores = CharacterBuilder.assignScores(CharacterBuilder.rollScores(), bb.mainClassKey)) }
                        }) { Text("Roll again") }
                    }
                    ScoreMethod.STANDARD_ARRAY -> BodyText("Standard array: 15, 14, 13, 12, 10, 8.", muted = true)
                    ScoreMethod.MANUAL -> BodyText("Enter any values you like; nothing is enforced.", muted = true)
                }
            }
        }
        item {
            DndCard("Base scores") {
                Ability.entries.forEach { a ->
                    val base = b.baseScores[a] ?: 10
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(a.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Stepper(base, { v -> set { it.copy(baseScores = it.baseScores + (a to v)) } }, min = 1, max = 20)
                        }
                        val race = (s.race.abilityBonuses[a] ?: 0) + (b.freeAbilityBonuses[a] ?: 0)
                        val asi = b.asiBonuses[a] ?: 0
                        Text(
                            "base $base" + (if (race != 0) "  species ${sign(race)}" else "") + (if (asi != 0) "  ASI ${sign(asi)}" else "") +
                                "  →  ${s.scores.getValue(a)} (${sign(s.modifiers.getValue(a))})",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            DndCard("Ability score improvements") {
                StatRow("Improvements earned", "$asiEarned")
                StatRow("Points assigned", "$asiUsed of ${asiEarned * 2}", emphasise = asiUsed > asiEarned * 2)
                BodyText("Each improvement gives two points, or you can take a feat instead on the Feats tab.", muted = true)
                Ability.entries.forEach { a ->
                    val v = b.asiBonuses[a] ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(a.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Stepper(v, { nv -> set { it.copy(asiBonuses = (it.asiBonuses + (a to nv)).filterValues { x -> x != 0 }) } }, min = 0, max = 12)
                    }
                }
            }
        }
        item {
            DndCard("Saving throw proficiencies") {
                BodyText("By default these come from your first class. Override them for unusual builds.", muted = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Ability.entries.forEach { a ->
                        val on = a in s.saveProficiencies
                        FilterChip(on, {
                            set { bb ->
                                val current = if (bb.saveProficiencyOverride.isEmpty()) s.saveProficiencies else bb.saveProficiencyOverride
                                bb.copy(saveProficiencyOverride = if (on) current - a else current + a)
                            }
                        }, label = { Text(a.short) })
                    }
                }
                if (b.saveProficiencyOverride.isNotEmpty()) {
                    TextButton(onClick = { set { it.copy(saveProficiencyOverride = emptySet()) } }) { Text("Reset to class default") }
                }
            }
        }
        item {
            DndCard("Hit points") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HpMethod.entries.forEach { m -> FilterChip(b.hpMethod == m, { set { it.copy(hpMethod = m) } }, label = { Text(m.label) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Extra maximum hit points", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(b.bonusMaxHp, { v -> set { it.copy(bonusMaxHp = v) } }, min = -50, max = 200)
                }
                StatRow("Resulting maximum", "${s.maxHp}", emphasise = true)
                BodyText("Level 1 always gives the full hit die. Average adds half the die plus one per level.", muted = true)
            }
        }
    }
}

// ---------------------------------------------------------------- Skills

@Composable
private fun SkillsSection(b: CharacterBuild, s: CharacterSheet, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    val mainClass = SrdClasses.byKey[b.mainClassKey]
    val classList = mainClass?.skillList.orEmpty()
    val granted = (s.race.skills + s.background.skills).toSet()
    val chosenCount = b.chosenSkills.size
    val allowed = (mainClass?.skillChoices ?: 0) + s.race.extraSkillChoices

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Skill proficiencies") {
                StatRow("Chosen", "$chosenCount of $allowed", emphasise = chosenCount > allowed)
                BodyText(
                    "Your species and background already grant: " +
                        (granted.joinToString(", ") { skillName(it) }.ifBlank { "none" }) + ".", muted = true
                )
                Text("Class list", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    classList.forEach { sk ->
                        val on = sk in b.chosenSkills
                        FilterChip(on, { set { it.copy(chosenSkills = if (on) it.chosenSkills - sk else it.chosenSkills + sk) } },
                            label = { Text(skillName(sk)) })
                    }
                }
                Text("Any other skill", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Skill.entries.filter { it !in classList }.forEach { sk ->
                        val on = sk in b.chosenSkills
                        FilterChip(on, { set { it.copy(chosenSkills = if (on) it.chosenSkills - sk else it.chosenSkills + sk) } },
                            label = { Text(skillName(sk)) })
                    }
                }
            }
        }
        item {
            DndCard("Expertise") {
                BodyText("Double proficiency. Rogues get two at level 1 and two more at 6; bards two at 3 and two more at 10.", muted = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    s.skills.filter { it.proficient }.forEach { line ->
                        val on = line.skill in b.expertise
                        FilterChip(on, { set { it.copy(expertise = if (on) it.expertise - line.skill else it.expertise + line.skill) } },
                            label = { Text(skillName(line.skill)) })
                    }
                }
            }
        }
        item {
            DndCard("Resulting bonuses") {
                s.skills.sortedByDescending { it.bonus }.take(10).forEach { line ->
                    StatRow(skillName(line.skill) + " (" + line.skill.ability.short + ")", sign(line.bonus))
                }
            }
        }
        item {
            DndCard("Languages and tools") {
                TextFieldRow("Extra languages, comma separated", b.languages) { v -> set { it.copy(languages = v) } }
                TextFieldRow("Tool proficiencies", b.toolProficiencies) { v -> set { it.copy(toolProficiencies = v) } }
                TextFieldRow("Other proficiencies and notes", b.otherProficiencies, minLines = 2) { v -> set { it.copy(otherProficiencies = v) } }
            }
        }
    }
}

// ---------------------------------------------------------------- Gear

@Composable
private fun GearSection(b: CharacterBuild, s: CharacterSheet, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    var addingItem by remember { mutableStateOf(false) }
    var editingAttack by remember { mutableStateOf<Int?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Armour") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SrdEquipment.armors.forEach { a ->
                        FilterChip(b.armorName == a.name, { set { it.copy(armorName = a.name) } }, label = { Text(a.name) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Shield (+2 AC)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(b.shield, { v -> set { it.copy(shield = v) } })
                }
                StatRow("Armour class", "${s.armorClass}", emphasise = true)
                StatRow("Detail", s.armorNote)
            }
        }
        item {
            DndCard("Weapons and attacks") {
                BodyText("Each entry becomes a tappable attack on the sheet, with its own ability, magic bonus and extra damage.", muted = true)
                b.attacks.forEachIndexed { index, a ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.customName.ifBlank { a.weaponName }, style = MaterialTheme.typography.titleMedium)
                            val line = s.attacks.getOrNull(index)
                            if (line != null) {
                                Text("${sign(line.attackBonus)} · ${line.damage} ${line.damageType}",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        TextButton(onClick = { editingAttack = index }) { Text("Edit") }
                        TextButton(onClick = { set { bb -> bb.copy(attacks = bb.attacks.filterIndexed { i, _ -> i != index }) } }) { Text("Remove") }
                    }
                }
                Button(onClick = { set { it.copy(attacks = it.attacks + AttackEntry()) } }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Text("Add a weapon")
                }
            }
        }
        item {
            DndCard("Inventory") {
                StatRow("Carried", "${fmt1(s.carriedLb)} of ${fmt1(s.carryCapacityLb)} lb")
                b.inventory.forEachIndexed { index, item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        OutlinedTextField(
                            item.name, { v -> set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(name = v) else x }) } },
                            label = { Text("Item") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Quantity", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Stepper(item.quantity, { v ->
                                set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(quantity = v) else x }) }
                            }, min = 0, max = 999)
                        }
                        OutlinedTextField(
                            if (item.weightLb == 0.0) "" else fmt1(item.weightLb),
                            { v -> set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(weightLb = v.replace(',', '.').toDoubleOrNull() ?: 0.0) else x }) } },
                            label = { Text("Weight per item (lb)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            item.note, { v -> set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(note = v) else x }) } },
                            label = { Text("Note") }, modifier = Modifier.fillMaxWidth()
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(item.equipped, { v -> set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(equipped = v) else x }) } })
                            Text("Equipped", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Checkbox(item.attuned, { v -> set { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, x -> if (i == index) x.copy(attuned = v) else x }) } })
                            Text("Attuned", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            TextButton(onClick = { set { bb -> bb.copy(inventory = bb.inventory.filterIndexed { i, _ -> i != index }) } }) { Text("Delete") }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Button(onClick = { set { it.copy(inventory = it.inventory + InventoryItem()) } }) { Text("Empty row") }
                    OutlinedButton(onClick = { addingItem = true }) { Text("From the gear list") }
                }
            }
        }
        item {
            DndCard("Money") {
                listOf("Platinum" to b.currency.pp, "Gold" to b.currency.gp, "Electrum" to b.currency.ep,
                    "Silver" to b.currency.sp, "Copper" to b.currency.cp).forEach { (label, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Stepper(value, { v ->
                            set {
                                it.copy(currency = when (label) {
                                    "Platinum" -> it.currency.copy(pp = v)
                                    "Gold" -> it.currency.copy(gp = v)
                                    "Electrum" -> it.currency.copy(ep = v)
                                    "Silver" -> it.currency.copy(sp = v)
                                    else -> it.currency.copy(cp = v)
                                })
                            }
                        }, min = 0, max = 99999)
                    }
                }
            }
        }
    }

    if (addingItem) {
        AlertDialog(
            onDismissRequest = { addingItem = false },
            title = { Text("Add gear") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SrdEquipment.gear.forEach { g ->
                        TextButton(
                            onClick = {
                                set { it.copy(inventory = it.inventory + InventoryItem(g.name, 1, g.weightLb, g.note)) }
                                addingItem = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(g.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${fmt1(g.weightLb)} lb · ${g.costGp} gp" + if (g.note.isNotBlank()) " · ${g.note}" else "",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { addingItem = false }) { Text("Close") } }
        )
    }

    editingAttack?.let { index ->
        val entry = b.attacks.getOrNull(index) ?: return@let
        AttackDialog(entry, onDismiss = { editingAttack = null }) { updated ->
            set { bb -> bb.copy(attacks = bb.attacks.mapIndexed { i, x -> if (i == index) updated else x }) }
            editingAttack = null
        }
    }
}

@Composable
private fun AttackDialog(initial: AttackEntry, onDismiss: () -> Unit, onSave: (AttackEntry) -> Unit) {
    var a by remember(initial) { mutableStateOf(initial) }
    val weapon = SrdEquipment.weaponsByName[a.weaponName]
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Weapon") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Simple melee", "Simple ranged", "Martial melee", "Martial ranged", "Unarmed").forEach { cat ->
                    Text(cat, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SrdEquipment.weapons.filter { it.category == cat }.forEach { w ->
                            FilterChip(a.weaponName == w.name, { a = a.copy(weaponName = w.name) }, label = { Text(w.name) })
                        }
                    }
                }
                weapon?.let { w ->
                    StatRow("Damage", "${w.damage} ${w.type}")
                    if (w.versatile != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Wield two-handed (${w.versatile})", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Switch(a.twoHanded, { a = a.copy(twoHanded = it) })
                        }
                    }
                }
                OutlinedTextField(a.customName, { a = a.copy(customName = it) }, label = { Text("Custom name (optional)") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Magic bonus", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(a.magicBonus, { a = a.copy(magicBonus = it) }, min = 0, max = 5)
                }
                OutlinedTextField(a.extraDamage, { a = a.copy(extraDamage = it) },
                    label = { Text("Extra damage dice, e.g. 2d6") }, singleLine = true)
                OutlinedTextField(a.extraDamageType, { a = a.copy(extraDamageType = it) },
                    label = { Text("Extra damage type, e.g. fire") }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Proficient", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(a.proficient, { a = a.copy(proficient = it) })
                }
                Text("Ability used", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(a.useAbility == null, { a = a.copy(useAbility = null) }, label = { Text("Automatic") })
                    Ability.entries.forEach { ab ->
                        FilterChip(a.useAbility == ab, { a = a.copy(useAbility = ab) }, label = { Text(ab.short) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(a) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ---------------------------------------------------------------- Spells

@Composable
private fun SpellsSection(b: CharacterBuild, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    var filterClass by rememberSaveable { mutableStateOf<String?>(null) }
    var filterLevel by rememberSaveable { mutableIntStateOf(-1) }
    var search by rememberSaveable { mutableStateOf("") }
    val casterKeys = b.classes.map { it.classKey }.filter { SrdClasses.byKey[it]?.spellcasting != null }

    val pool = remember(filterClass, filterLevel, search) {
        Spells.all.filter { spell ->
            (filterClass == null || filterClass in spell.classes) &&
                (filterLevel < 0 || spell.level == filterLevel) &&
                (search.isBlank() || spell.name.contains(search, ignoreCase = true) || spell.text.contains(search, ignoreCase = true))
        }.sortedWith(compareBy({ it.level }, { it.name }))
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Spell list") {
                StatRow("Spells selected", "${b.spellKeys.size}")
                StatRow("Marked as prepared", "${b.preparedKeys.size}")
                OutlinedTextField(search, { search = it }, label = { Text("Search by name or effect") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
                Text("Class list", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(filterClass == null, { filterClass = null }, label = { Text("All") })
                    casterKeys.forEach { k ->
                        FilterChip(filterClass == k, { filterClass = k }, label = { Text(SrdClasses.byKey[k]?.name ?: k) })
                    }
                    SrdClasses.all.filter { it.spellcasting != null && it.key !in casterKeys }.forEach { c ->
                        FilterChip(filterClass == c.key, { filterClass = c.key }, label = { Text(c.name) })
                    }
                }
                Text("Level", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(filterLevel == -1, { filterLevel = -1 }, label = { Text("Any") })
                    (0..9).forEach { l -> FilterChip(filterLevel == l, { filterLevel = l }, label = { Text(if (l == 0) "Cantrip" else "$l") }) }
                }
            }
        }
        items(pool) { spell ->
            val selected = spell.key in b.spellKeys
            val prepared = spell.key in b.preparedKeys
            DndCard(spell.name) {
                Text(
                    "${spell.levelLabel} · ${spell.school.label} · ${spell.classes.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${spell.castingTime} · ${spell.range} · ${spell.components} · ${spell.duration}" +
                        (if (spell.concentration) " · concentration" else "") + (if (spell.ritual) " · ritual" else ""),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
                BodyText(spell.text, modifier = Modifier.padding(top = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    FilterChip(selected, {
                        set { bb ->
                            if (selected) bb.copy(spellKeys = bb.spellKeys - spell.key, preparedKeys = bb.preparedKeys - spell.key)
                            else bb.copy(spellKeys = bb.spellKeys + spell.key)
                        }
                    }, label = { Text(if (selected) "Known" else "Add") })
                    FilterChip(prepared, {
                        set { bb ->
                            if (prepared) bb.copy(preparedKeys = bb.preparedKeys - spell.key)
                            else bb.copy(spellKeys = bb.spellKeys + spell.key, preparedKeys = bb.preparedKeys + spell.key)
                        }
                    }, label = { Text("Prepared") })
                }
            }
        }
        item {
            DndCard("Missing a spell?") {
                BodyText(
                    "This list holds the spells from the open licences. Anything else you can add on the Homebrew tab " +
                        "as a spell entry; it then shows up in the spell section of the sheet.", muted = true
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Feats

@Composable
private fun FeatsSection(b: CharacterBuild, s: CharacterSheet, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Feats") {
                StatRow("Improvements earned", "${CharacterBuilder.asiCount(b.classes)}")
                BodyText("Each ability score improvement can be traded for one feat. Variant humans start with one extra.", muted = true)
            }
        }
        items(SrdFeats.all) { feat ->
            val on = feat.key in b.featKeys
            DndCard(feat.name) {
                Text("${feat.source.label} · prerequisite ${feat.prerequisite}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BodyText(feat.text, modifier = Modifier.padding(top = 4.dp))
                FilterChip(on, { set { it.copy(featKeys = if (on) it.featKeys - feat.key else it.featKeys + feat.key) } },
                    label = { Text(if (on) "Taken" else "Take") }, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

// ---------------------------------------------------------------- Homebrew

@Composable
private fun HomebrewSection(b: CharacterBuild, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Homebrew and content from other books") {
                BodyText(
                    "Add anything the app does not ship: a subclass, a species trait, a spell, a feat, a magic item or a " +
                        "house rule. Text entries appear on the sheet. An optional effect is applied to the numbers."
                )
                BodyText(
                    "Effects understood: ac+1, hp+10, speed+10, init+2, skill:STEALTH. One effect per entry.", muted = true
                )
                Button(onClick = { set { it.copy(homebrew = it.homebrew + HomebrewEntry()) } },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Add an entry") }
            }
        }
        items(b.homebrew.indices.toList()) { index ->
            val h = b.homebrew[index]
            DndCard("Entry ${index + 1}") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HomebrewKind.entries.forEach { k ->
                        FilterChip(h.kind == k, {
                            set { bb -> bb.copy(homebrew = bb.homebrew.mapIndexed { i, x -> if (i == index) x.copy(kind = k) else x }) }
                        }, label = { Text(k.label) })
                    }
                }
                OutlinedTextField(h.name, { v ->
                    set { bb -> bb.copy(homebrew = bb.homebrew.mapIndexed { i, x -> if (i == index) x.copy(name = v) else x }) }
                }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                OutlinedTextField(h.text, { v ->
                    set { bb -> bb.copy(homebrew = bb.homebrew.mapIndexed { i, x -> if (i == index) x.copy(text = v) else x }) }
                }, label = { Text("Rules text") }, minLines = 3, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                OutlinedTextField(h.effect, { v ->
                    set { bb -> bb.copy(homebrew = bb.homebrew.mapIndexed { i, x -> if (i == index) x.copy(effect = v) else x }) }
                }, label = { Text("Effect (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                TextButton(onClick = { set { bb -> bb.copy(homebrew = bb.homebrew.filterIndexed { i, _ -> i != index }) } },
                    modifier = Modifier.padding(top = 4.dp)) { Text("Delete entry") }
            }
        }
    }
}

// ---------------------------------------------------------------- Story

@Composable
private fun StorySection(b: CharacterBuild, set: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Personality") {
                TextFieldRow("Personality traits", b.personality, minLines = 2) { v -> set { it.copy(personality = v) } }
                TextFieldRow("Ideals", b.ideals, minLines = 2) { v -> set { it.copy(ideals = v) } }
                TextFieldRow("Bonds", b.bonds, minLines = 2) { v -> set { it.copy(bonds = v) } }
                TextFieldRow("Flaws", b.flaws, minLines = 2) { v -> set { it.copy(flaws = v) } }
            }
        }
        item {
            DndCard("Description") {
                TextFieldRow("Appearance", b.appearance, minLines = 3) { v -> set { it.copy(appearance = v) } }
                TextFieldRow("Backstory", b.backstory, minLines = 5) { v -> set { it.copy(backstory = v) } }
                TextFieldRow("Notes", b.notes, minLines = 3) { v -> set { it.copy(notes = v) } }
            }
        }
    }
}

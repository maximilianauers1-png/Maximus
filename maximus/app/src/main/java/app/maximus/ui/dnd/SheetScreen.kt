@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.domain.Ability
import app.maximus.dnd.domain.CharacterBuild
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.dnd.domain.CharacterSheet
import app.maximus.dnd.domain.EldritchInvocations
import app.maximus.dnd.domain.FightingStyles
import app.maximus.dnd.domain.MetamagicOptions
import app.maximus.dnd.domain.PactBoons
import app.maximus.dnd.domain.Spell
import app.maximus.dnd.domain.Spells
import app.maximus.ui.components.MaximusTopBar
import kotlinx.coroutines.launch

private val SHEET_TABS = listOf("Core", "Skills", "Combat", "Spells", "Gear", "Features", "Story")

@Composable
fun CharacterSheetScreen(services: AppServices, characterId: Long, onBack: () -> Unit, onEdit: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    var build by remember { mutableStateOf<CharacterBuild?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var roll by remember { mutableStateOf<RollRequest?>(null) }

    LaunchedEffect(characterId) {
        build = services.dnd.character(characterId)?.let { DndCodec.decodeCharacter(it.payload) }
        loaded = true
    }

    /** Persists a change to the build immediately; the sheet is a live document, not a preview. */
    fun update(transform: (CharacterBuild) -> CharacterBuild) {
        val current = build ?: return
        val next = transform(current)
        build = next
        val sheet = CharacterBuilder.build(next)
        scope.launch {
            services.dnd.saveCharacter(characterId, next.name.ifBlank { "Unnamed" }, summaryOf(sheet), DndCodec.encode(next))
        }
    }

    val b = build
    Scaffold(
        topBar = {
            MaximusTopBar(
                title = b?.name?.ifBlank { "Unnamed" } ?: "Character",
                onBack = onBack,
                actions = { TextButton(onClick = { onEdit(characterId) }) { Text("Edit") } }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        if (b == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("This character no longer exists.") }
            return@Scaffold
        }
        val sheet = remember(b) { CharacterBuilder.build(b) }
        val bubble by services.dnd.bubbleEnabled.collectAsState(initial = true)
        Box(Modifier.fillMaxSize().padding(padding)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = tab, edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
            ) {
                SHEET_TABS.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
            }
            when (tab) {
                0 -> CoreTab(sheet, { roll = it }, ::update)
                1 -> SkillsTab(sheet) { roll = it }
                2 -> CombatTab(sheet, { roll = it }, ::update)
                3 -> SpellsTab(sheet) { roll = it }
                4 -> GearTab(sheet, ::update)
                5 -> FeaturesTab(sheet)
                else -> StoryTab(sheet)
            }
        }
        if (bubble) QuickRollBubble(services)
        }
    }

    roll?.let { r ->
        RollDialog(r, onDismiss = { roll = null }) { expr, total, detail ->
            scope.launch { services.dnd.addRoll(expr, r.label, total, detail) }
        }
    }
}

fun summaryOf(s: CharacterSheet): String =
    "${s.race.name} · ${s.classLine} · AC ${s.armorClass} · ${s.maxHp} HP"

// ---------------------------------------------------------------- Core

@Composable
private fun CoreTab(s: CharacterSheet, onRoll: (RollRequest) -> Unit, update: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    val b = s.build
    val currentHp = b.currentHp ?: s.maxHp
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Identity") {
                StatRow("Species", s.race.name)
                StatRow("Class", s.classLine)
                StatRow("Background", s.background.name)
                StatRow("Level", "${b.totalLevel}  (proficiency ${sign(s.proficiency)})", emphasise = true)
                if (b.alignment.isNotBlank()) StatRow("Alignment", b.alignment)
                if (b.player.isNotBlank()) StatRow("Player", b.player)
            }
        }
        item {
            DndCard("Ability scores — tap to roll a check") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Ability.entries.forEach { a ->
                        StatBox(
                            label = a.short,
                            big = sign(s.modifiers.getValue(a)),
                            small = "${s.scores.getValue(a)}",
                            modifier = Modifier.width(96.dp),
                            onClick = { onRoll(RollRequest("${a.short} check", s.modifiers.getValue(a))) }
                        )
                    }
                }
            }
        }
        item {
            DndCard("Saving throws — tap to roll") {
                Ability.entries.forEach { a ->
                    val proficient = a in s.saveProficiencies
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onRoll(RollRequest("${a.short} saving throw", s.saves.getValue(a))) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (proficient) "●" else "○",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(a.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(sign(s.saves.getValue(a)), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            DndCard("Defence") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBox("Armour class", "${s.armorClass}", s.armorNote, modifier = Modifier.width(140.dp))
                    StatBox("Initiative", sign(s.initiative), "tap to roll", modifier = Modifier.width(110.dp)) {
                        onRoll(RollRequest("Initiative", s.initiative))
                    }
                    StatBox("Speed", "${s.speed} ft", if (s.darkvision > 0) "darkvision ${s.darkvision} ft" else null, modifier = Modifier.width(110.dp))
                }
            }
        }
        item {
            DndCard("Hit points") {
                StatRow("Maximum", "${s.maxHp}", emphasise = true)
                LinearProgressIndicator(
                    progress = { (currentHp.toFloat() / s.maxHp).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Current", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(currentHp, { v -> update { it.copy(currentHp = v) } }, min = 0, max = s.maxHp + 100)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Temporary", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(b.tempHp, { v -> update { it.copy(tempHp = v) } }, min = 0, max = 200)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Exhaustion", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(b.exhaustion, { v -> update { it.copy(exhaustion = v) } }, min = 0, max = 6)
                }
                StatRow("Hit dice", s.hitDice.entries.sortedBy { it.key }.joinToString("  ") { "${it.value}d${it.key}" })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    TextButton(onClick = { onRoll(RollRequest("Death saving throw", 0)) }) { Text("Death save") }
                    TextButton(onClick = { update { it.copy(currentHp = s.maxHp, tempHp = 0, hitDiceSpent = 0, deathFailures = 0, deathSuccesses = 0) } }) {
                        Text("Long rest")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(b.inspiration, { v -> update { it.copy(inspiration = v) } })
                    Text("Inspiration", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            DndCard("Senses") {
                StatRow("Passive Perception", "${s.passivePerception}")
                StatRow("Passive Investigation", "${s.passiveInvestigation}")
                StatRow("Passive Insight", "${s.passiveInsight}")
                StatRow("Carrying capacity", "${fmt1(s.carriedLb)} / ${fmt1(s.carryCapacityLb)} lb")
            }
        }
        if (s.warnings.isNotEmpty()) {
            item {
                DndCard("Rule checks") {
                    s.warnings.forEach { w ->
                        Text(w, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Skills

@Composable
private fun SkillsTab(s: CharacterSheet, onRoll: (RollRequest) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Skills — tap to roll") {
                Text(
                    "◆ expertise   ● proficient   ◐ half proficiency   ○ untrained",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                )
                s.skills.sortedBy { it.skill.name }.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onRoll(RollRequest("${skillName(line.skill)} check", line.bonus)) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when {
                                line.expert -> "◆"
                                line.proficient -> "●"
                                line.halfProficient -> "◐"
                                else -> "○"
                            },
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(skillName(line.skill), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(line.skill.ability.short, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(sign(line.bonus), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            DndCard("Other proficiencies") {
                StatRow("Armour", s.classes.firstOrNull()?.first?.armor ?: "—")
                StatRow("Weapons", s.classes.firstOrNull()?.first?.weapons ?: "—")
                StatRow("Tools", listOf(s.classes.firstOrNull()?.first?.tools, s.background.tools, s.build.toolProficiencies)
                    .filter { !it.isNullOrBlank() && it != "—" }.joinToString(", ").ifBlank { "—" })
                StatRow("Languages", (s.race.languages + s.build.languages.split(",").map { it.trim() })
                    .filter { it.isNotBlank() }.joinToString(", "))
                if (s.build.otherProficiencies.isNotBlank()) BodyText(s.build.otherProficiencies)
            }
        }
    }
}

// ---------------------------------------------------------------- Combat

@Composable
private fun CombatTab(s: CharacterSheet, onRoll: (RollRequest) -> Unit, update: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Attacks — tap to roll attack and damage") {
                if (s.attacks.isEmpty()) BodyText("No weapons configured. Add them in the editor.", muted = true)
                s.attacks.forEach { a ->
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                onRoll(
                                    RollRequest(
                                        label = a.name, modifier = a.attackBonus,
                                        damageExpression = a.damage.substringBefore(" + "),
                                        damageLabel = "Damage (${a.damageType})"
                                    )
                                )
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(a.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(sign(a.attackBonus), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            "${a.damage} ${a.damageType}   average ${fmt1(a.averageDamage)}   range ${a.range}",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (a.properties.isNotBlank()) {
                            Text(a.properties, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
        if (s.spellAttacks.isNotEmpty()) {
            item {
                DndCard("Spell attacks — tap to roll") {
                    s.spellAttacks.forEach { a ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .clickable {
                                    onRoll(
                                        RollRequest(
                                            label = a.name, modifier = a.attackBonus,
                                            damageExpression = a.damage.replace(" ", ""),
                                            damageLabel = "Damage (${a.damageType})"
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(a.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Text(sign(a.attackBonus), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                "${a.damage} ${a.damageType}   average ${fmt1(a.averageDamage)}   range ${a.range}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (a.properties.isNotBlank()) {
                                Text(a.properties, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    BodyText("Eldritch blast: roll once per beam; the average already counts every beam.", muted = true)
                }
            }
        }
        if (s.resources.isNotEmpty()) {
            item {
                DndCard("Class resources") {
                    s.resources.forEach { (name, value) -> StatRow(name, value) }
                }
            }
        }
        item {
            DndCard("Defence and movement") {
                StatRow("Armour class", "${s.armorClass}", emphasise = true)
                StatRow("Armour", s.armorNote)
                StatRow("Initiative", sign(s.initiative))
                StatRow("Speed", "${s.speed} ft")
                if (s.darkvision > 0) StatRow("Darkvision", "${s.darkvision} ft")
            }
        }
        item {
            DndCard("Hit points and resources") {
                val current = s.build.currentHp ?: s.maxHp
                StatRow("Hit points", "$current / ${s.maxHp}", emphasise = true)
                if (s.build.tempHp > 0) StatRow("Temporary", "${s.build.tempHp}")
                StatRow("Hit dice spent", "${s.build.hitDiceSpent}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Spend a hit die", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(s.build.hitDiceSpent, { v -> update { it.copy(hitDiceSpent = v) } }, min = 0, max = s.build.totalLevel)
                }
                val die = s.hitDice.keys.maxOrNull() ?: 8
                TextButton(onClick = {
                    onRoll(RollRequest("Hit die (d$die + CON)", rawExpression = "1d$die${signExpr(s.modifiers.getValue(Ability.CON))}"))
                }) { Text("Roll a hit die") }
            }
        }
        item {
            DndCard("Conditions and death saves") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Death save successes", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(s.build.deathSuccesses, { v -> update { it.copy(deathSuccesses = v) } }, min = 0, max = 3)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Death save failures", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Stepper(s.build.deathFailures, { v -> update { it.copy(deathFailures = v) } }, min = 0, max = 3)
                }
                BodyText("Three successes stabilise you; three failures kill you. A natural 20 restores 1 hit point, a natural 1 counts as two failures.", muted = true)
            }
        }
    }
}

private fun signExpr(v: Int) = if (v >= 0) "+$v" else "$v"

// ---------------------------------------------------------------- Spells

@Composable
private fun SpellsTab(s: CharacterSheet, onRoll: (RollRequest) -> Unit) {
    val known = remember(s.build.spellKeys) {
        s.build.spellKeys.mapNotNull { Spells.byKey[it] }.sortedWith(compareBy({ it.level }, { it.name }))
    }
    val homebrewSpells = s.build.homebrew.filter { it.kind == app.maximus.dnd.domain.HomebrewKind.SPELL }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (s.spellcasting.isEmpty() && known.isEmpty() && homebrewSpells.isEmpty()) {
            item { DndCard("Spellcasting") { BodyText("This character has no spellcasting.", muted = true) } }
        }
        items(s.spellcasting) { block ->
            DndCard("${block.className} spellcasting") {
                StatRow("Ability", block.ability.name)
                StatRow("Spell save DC", "${block.saveDc}", emphasise = true)
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onRoll(RollRequest("${block.className} spell attack", block.attackBonus)) }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Spell attack (tap to roll)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sign(block.attackBonus), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                StatRow("Cantrips known", "${block.cantripsKnown}")
                block.spellsKnown?.let { StatRow("Spells known", "$it") }
                block.spellsPrepared?.let { StatRow("Spells prepared", "$it") }
            }
        }
        item {
            DndCard("Spell slots") {
                val slots = s.spellSlots
                if (slots.all { it == 0 } && s.pactSlots.first == 0) {
                    BodyText("No spell slots.", muted = true)
                } else {
                    slots.forEachIndexed { i, n -> if (n > 0) StatRow("Level ${i + 1}", "$n") }
                    if (s.pactSlots.first > 0) {
                        StatRow("Pact magic", "${s.pactSlots.first} × level ${s.pactSlots.second}", emphasise = true)
                    }
                }
            }
        }
        if (known.isNotEmpty()) {
            val byLevel = known.groupBy { it.level }.toSortedMap()
            byLevel.forEach { (level, spells) ->
                item {
                    DndCard(if (level == 0) "Cantrips" else "Level $level spells") {
                        spells.forEach { spell -> SpellRow(spell, spell.key in s.build.preparedKeys, onRoll) }
                    }
                }
            }
        }
        if (homebrewSpells.isNotEmpty()) {
            item {
                DndCard("Homebrew spells") {
                    homebrewSpells.forEach { h ->
                        Text(h.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                        BodyText(h.text, muted = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellRow(spell: Spell, prepared: Boolean, onRoll: (RollRequest) -> Unit) {
    var open by remember(spell.key) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (prepared) "●" else "○", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
            Text(spell.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(spell.school.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            listOfNotNull(
                spell.castingTime, spell.range, spell.components, spell.duration,
                if (spell.concentration) "concentration" else null,
                if (spell.ritual) "ritual" else null
            ).joinToString(" · "),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 20.dp)
        )
        if (open) {
            BodyText(spell.text, modifier = Modifier.padding(start = 20.dp))
            val dice = Regex("\\d+d\\d+").findAll(spell.text).map { it.value }.distinct().take(3).toList()
            if (dice.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(start = 20.dp, top = 4.dp)) {
                    dice.forEach { d ->
                        AssistChip(onClick = { onRoll(RollRequest("${spell.name} — $d", rawExpression = d)) }, label = { Text("Roll $d") })
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Gear

@Composable
private fun GearTab(s: CharacterSheet, update: ((CharacterBuild) -> CharacterBuild) -> Unit) {
    val b = s.build
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Carried weight") {
                StatRow("Carried", "${fmt1(s.carriedLb)} lb")
                StatRow("Capacity", "${fmt1(s.carryCapacityLb)} lb")
                StatRow("Push, drag or lift", "${fmt1(2 * s.carryCapacityLb)} lb")
                if (s.carriedLb > s.carryCapacityLb) {
                    Text("You are over your capacity and cannot move.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
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
                            update {
                                it.copy(currency = when (label) {
                                    "Platinum" -> it.currency.copy(pp = v)
                                    "Gold" -> it.currency.copy(gp = v)
                                    "Electrum" -> it.currency.copy(ep = v)
                                    "Silver" -> it.currency.copy(sp = v)
                                    else -> it.currency.copy(cp = v)
                                })
                            }
                        }, min = 0, max = 99999, step = 1)
                    }
                }
                StatRow("Total value", "${fmt1(b.currency.totalGp)} gp")
            }
        }
        item {
            DndCard("Inventory") {
                if (b.inventory.isEmpty()) BodyText("Empty. Add items in the editor.", muted = true)
                b.inventory.forEachIndexed { index, item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.name.ifBlank { "Item" },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis
                            )
                            Stepper(item.quantity, { v ->
                                update { bb -> bb.copy(inventory = bb.inventory.mapIndexed { i, it2 -> if (i == index) it2.copy(quantity = v) else it2 }) }
                            }, min = 0, max = 999)
                        }
                        val meta = listOfNotNull(
                            if (item.weightLb > 0) "${fmt1(item.totalWeight)} lb" else null,
                            if (item.equipped) "equipped" else null,
                            if (item.attuned) "attuned" else null
                        ).joinToString(" · ")
                        if (meta.isNotBlank()) {
                            Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        if (item.note.isNotBlank()) BodyText(item.note, muted = true)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Features

@Composable
private fun FeaturesTab(s: CharacterSheet) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("${s.race.name} traits") {
                Text(s.race.source.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                s.race.traits.forEach { trait ->
                    Text(trait.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    BodyText(trait.text, muted = true)
                }
            }
        }
        item {
            DndCard("Background: ${s.background.name}") {
                BodyText(s.background.feature)
                StatRow("Skills", s.background.skills.joinToString(", ") { skillName(it) })
                StatRow("Tools", s.background.tools)
                BodyText("Equipment: ${s.background.equipment}", muted = true)
            }
        }
        if (s.feats.isNotEmpty()) {
            item {
                DndCard("Feats") {
                    s.feats.forEach { feat ->
                        Text(feat.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        if (feat.prerequisite != "—") {
                            Text("Prerequisite: ${feat.prerequisite}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        BodyText(feat.text, muted = true)
                    }
                }
            }
        }
        val b = s.build
        val options = b.invocationKeys.mapNotNull { EldritchInvocations.byKey[it] }.map { "Invocation" to it } +
            b.metamagicKeys.mapNotNull { MetamagicOptions.byKey[it] }.map { "Metamagic" to it } +
            listOfNotNull(b.pactBoon?.let { PactBoons.byKey[it] }).map { "Pact boon" to it } +
            b.fightingStyles.mapNotNull { FightingStyles.byKey[it] }.map { "Fighting style" to it }
        if (options.isNotEmpty()) {
            item {
                DndCard("Class options") {
                    options.forEach { (kind, o) ->
                        Text(o.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Text("$kind · ${o.source.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        BodyText(o.text, muted = true)
                    }
                }
            }
        }
        item {
            DndCard("Class features") {
                if (s.features.isEmpty()) BodyText("None yet.", muted = true)
                s.features.forEach { (source, feature) ->
                    Text(
                        "${feature.level}. ${feature.name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Text(source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    BodyText(feature.text, muted = true)
                }
            }
        }
        val homebrew = s.build.homebrew.filter { it.kind != app.maximus.dnd.domain.HomebrewKind.SPELL }
        if (homebrew.isNotEmpty()) {
            item {
                DndCard("Homebrew") {
                    homebrew.forEach { h ->
                        Text(h.name.ifBlank { h.kind.label }, style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Text(h.kind.label + if (h.effect.isNotBlank()) "  ·  effect ${h.effect}" else "",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        BodyText(h.text, muted = true)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Story

@Composable
private fun StoryTab(s: CharacterSheet) {
    val b = s.build
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            DndCard("Personality") {
                if (b.personality.isBlank() && b.ideals.isBlank() && b.bonds.isBlank() && b.flaws.isBlank()) {
                    BodyText("Nothing written yet. Fill this in from the editor.", muted = true)
                }
                if (b.personality.isNotBlank()) { StatRow("Traits", ""); BodyText(b.personality) }
                if (b.ideals.isNotBlank()) { StatRow("Ideals", ""); BodyText(b.ideals) }
                if (b.bonds.isNotBlank()) { StatRow("Bonds", ""); BodyText(b.bonds) }
                if (b.flaws.isNotBlank()) { StatRow("Flaws", ""); BodyText(b.flaws) }
            }
        }
        if (b.appearance.isNotBlank()) item { DndCard("Appearance") { BodyText(b.appearance) } }
        if (b.backstory.isNotBlank()) item { DndCard("Backstory") { BodyText(b.backstory) } }
        if (b.notes.isNotBlank()) item { DndCard("Notes") { BodyText(b.notes) } }
    }
}

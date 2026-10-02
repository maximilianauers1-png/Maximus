@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.data.DndMonsterEntity
import app.maximus.dnd.domain.Ability
import app.maximus.dnd.domain.ChallengeRating
import app.maximus.dnd.domain.CombatEstimate
import app.maximus.dnd.domain.CrRating
import app.maximus.dnd.domain.Monster
import app.maximus.dnd.domain.MonsterAction
import app.maximus.dnd.domain.MonsterType
import app.maximus.dnd.domain.Rules
import app.maximus.dnd.domain.Size
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.components.SteelRule
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.parseDecimal
import kotlinx.coroutines.launch

@Composable
fun MonstersTab(services: AppServices) {
    val scope = rememberCoroutineScope()
    val saved by services.dnd.monsters.collectAsState(initial = emptyList<DndMonsterEntity>())
    var editing by remember { mutableStateOf<Pair<Long, Monster>?>(null) }
    var viewing by remember { mutableStateOf<Monster?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Button(onClick = { editing = 0L to Monster() }, modifier = Modifier.fillMaxWidth()) { Text("New monster") } }
        if (saved.isEmpty()) item { EmptyState("No monsters yet.") }
        items(saved, key = { it.id }) { m ->
            val monster = remember(m.payload) { DndCodec.decodeMonster(m.payload) }
            PlateCard(onClick = { viewing = monster }) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.name, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${sizeLabel(monster.size)} ${typeLabel(monster.type)} · RK ${monster.armorClass} · ${monster.hitPoints} TP",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("CR ${ChallengeRating.format(m.challengeRating)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = { editing = m.id to monster }) { Text("Edit") }
                }
            }
        }
    }

    editing?.let { (id, m) ->
        MonsterEditor(id, m, onDismiss = { editing = null },
            onSave = { monster ->
                val cr = ChallengeRating.rate(monster).finalCr
                scope.launch { services.dnd.saveMonster(id, monster.name, cr, DndCodec.encode(monster)); editing = null }
            },
            onDelete = if (id == 0L) null else { { scope.launch { services.dnd.deleteMonster(id); editing = null } } })
    }
    viewing?.let { StatBlockDialog(it) { viewing = null } }
}

fun sizeLabel(s: Size): String = when (s) {
    Size.TINY -> "Tiny"; Size.SMALL -> "Small"; Size.MEDIUM -> "Medium"
    Size.LARGE -> "Large"; Size.HUGE -> "Huge"; Size.GARGANTUAN -> "Gargantuan"
}

fun typeLabel(t: MonsterType): String = when (t) {
    MonsterType.ABERRATION -> "Aberration"; MonsterType.BEAST -> "Beast"
    MonsterType.CELESTIAL -> "Celestial"; MonsterType.CONSTRUCT -> "Construct"
    MonsterType.DRAGON -> "Dragon"; MonsterType.ELEMENTAL -> "Elemental"
    MonsterType.FEY -> "Fey"; MonsterType.FIEND -> "Fiend"
    MonsterType.GIANT -> "Giant"; MonsterType.HUMANOID -> "Humanoid"
    MonsterType.MONSTROSITY -> "Monstrosity"; MonsterType.OOZE -> "Ooze"
    MonsterType.PLANT -> "Plant"; MonsterType.UNDEAD -> "Undead"
}

@Composable
private fun MonsterEditor(id: Long, initial: Monster, onDismiss: () -> Unit, onSave: (Monster) -> Unit, onDelete: (() -> Unit)?) {
    var m by remember(initial) { mutableStateOf(initial) }
    val rating = remember(m) { ChallengeRating.rate(m) }
    var actionEdit by remember { mutableStateOf<Pair<Int, MonsterAction>?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (id == 0L) "New monster" else "Edit monster") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(m.name, { m = m.copy(name = it) }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Size.entries.forEach { s -> FilterChip(m.size == s, { m = m.copy(size = s) }, label = { Text(sizeLabel(s)) }) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonsterType.entries.forEach { t -> FilterChip(m.type == t, { m = m.copy(type = t) }, label = { Text(typeLabel(t)) }) }
                }
                OutlinedTextField(m.alignment, { m = m.copy(alignment = it) }, label = { Text("Alignment") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Armour class", m.armorClass.toString(), { v -> v.toIntOrNull()?.let { m = m.copy(armorClass = it.coerceIn(1, 40)) } }, Modifier.weight(1f), integer = true)
                    NumberField("Hit dice", m.hitDiceCount.toString(), { v -> v.toIntOrNull()?.let { m = m.copy(hitDiceCount = it.coerceIn(1, 100)) } }, Modifier.weight(1f), integer = true)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(4, 6, 8, 10, 12, 20).forEach { d -> FilterChip(m.hitDieSize == d, { m = m.copy(hitDieSize = d) }, label = { Text("d$d") }) }
                }
                OutlinedTextField(m.speed, { m = m.copy(speed = it) }, label = { Text("Speed") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                Ability.entries.forEach { a ->
                    val v = m.scores[a] ?: 10
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(a.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { m = m.copy(scores = m.scores + (a to (v - 1).coerceAtLeast(1))) }) { Text("−") }
                        Text("$v (${sign(Rules.modifier(v))})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 10.dp))
                        OutlinedButton(onClick = { m = m.copy(scores = m.scores + (a to (v + 1).coerceAtMost(30))) }) { Text("+") }
                    }
                }

                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Actions", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { actionEdit = -1 to MonsterAction("Angriff", 4, "1,5 m", "1d6+2", "Hieb") }) { Text("Add") }
                }
                m.actions.forEachIndexed { i, a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${a.attackBonus?.let { sign(it) + " · " } ?: ""}${a.damageExpression} ${a.damageType} · ⌀ ${fmt(a.averageDamage, 1)}${if (a.attacksPerRound > 1) " ×${a.attacksPerRound}" else ""}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { actionEdit = i to a }) { Text("Edit") }
                        TextButton(onClick = { m = m.copy(actions = m.actions.filterIndexed { j, _ -> j != i }) }) { Text("Delete") }
                    }
                }

                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(m.senses, { m = m.copy(senses = it) }, label = { Text("Senses") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(m.languages, { m = m.copy(languages = it) }, label = { Text("Languages") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(m.damageResistances, { m = m.copy(damageResistances = it) }, label = { Text("Damage resistances") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(m.damageImmunities, { m = m.copy(damageImmunities = it) }, label = { Text("Damage immunities") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(m.notes, { m = m.copy(notes = it) }, label = { Text("Notes") }, minLines = 2, modifier = Modifier.fillMaxWidth())

                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                CrCard(rating)
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text("Delete") }
                TextButton(enabled = m.name.isNotBlank(), onClick = { onSave(m) }) { Text("Save") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    actionEdit?.let { (index, action) ->
        ActionEditor(action, onDismiss = { actionEdit = null }) { a ->
            m = if (index < 0) m.copy(actions = m.actions + a) else m.copy(actions = m.actions.mapIndexed { i, old -> if (i == index) a else old })
            actionEdit = null
        }
    }
}

@Composable
private fun ActionEditor(initial: MonsterAction, onDismiss: () -> Unit, onSave: (MonsterAction) -> Unit) {
    var a by remember(initial) { mutableStateOf(initial) }
    var bonusText by remember(initial) { mutableStateOf(initial.attackBonus?.toString() ?: "") }
    val valid = runCatching { app.maximus.dnd.domain.DiceParser.parse(a.damageExpression) }.isSuccess
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Action") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(a.name, { a = a.copy(name = it) }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(bonusText, { bonusText = it; a = a.copy(attackBonus = it.trim().toIntOrNull()) }, label = { Text("Attack bonus (empty = no attack roll)") }, singleLine = true)
                OutlinedTextField(a.reach, { a = a.copy(reach = it) }, label = { Text("Reach") }, singleLine = true)
                OutlinedTextField(
                    a.damageExpression, { a = a.copy(damageExpression = it) },
                    label = { Text("Damage expression") }, singleLine = true, isError = !valid,
                    supportingText = { Text(if (valid) "Mean: ${fmt(a.averageDamage, 2)}" else "Invalid dice expression.") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                )
                OutlinedTextField(a.damageType, { a = a.copy(damageType = it) }, label = { Text("Damage type") }, singleLine = true)
                NumberField("Attacks per round", a.attacksPerRound.toString(), { v -> v.toIntOrNull()?.let { a = a.copy(attacksPerRound = it.coerceIn(1, 10)) } }, integer = true)
                OutlinedTextField(a.description, { a = a.copy(description = it) }, label = { Text("Description") }, minLines = 2)
            }
        },
        confirmButton = { TextButton(enabled = valid && a.name.isNotBlank(), onClick = { onSave(a) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun CrCard(r: CrRating) {
    SectionCard("Challenge rating") {
        ResultLine("Final", "${ChallengeRating.format(r.finalCr)}   (${r.xp} XP)")
        ResultLine("Defensive", "${ChallengeRating.format(r.defensiveCr)}   ${r.effectiveHp} HP, AC ${r.effectiveAc} (expected ${r.expectedAcForCr})")
        ResultLine("Offensive", "${ChallengeRating.format(r.offensiveCr)}   ${fmt(r.damagePerRound, 1)} dmg/round, ${sign(r.effectiveAttackBonus)} (expected ${sign(r.expectedAttackForCr)})")
        ResultLine("Proficiency bonus", sign(r.proficiency))
        Text("Computed by the Dungeon Master's Guide method: defensive CR from hit points corrected by the armour-class deviation (2 points = one step); offensive CR from damage per round corrected by the attack bonus; the average of both gives the CR.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatBlockDialog(m: Monster, onDismiss: () -> Unit) {
    val r = remember(m) { ChallengeRating.rate(m) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(m.name, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("${sizeLabel(m.size)} ${typeLabel(m.type)}, ${m.alignment}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                ResultLine("Armour class", "${m.armorClass}${if (m.armorNote.isNotBlank()) " (${m.armorNote})" else ""}")
                ResultLine("Hit points", "${m.hitPoints} (${m.hitDiceText})")
                ResultLine("Speed", m.speed)
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                Row {
                    Ability.entries.forEach { a ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.short, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${m.scores[a] ?: 10}", style = MaterialTheme.typography.bodyMedium)
                            Text(sign(Rules.modifier(m.scores[a] ?: 10)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                if (m.damageResistances.isNotBlank()) ResultLine("Damage resistances", m.damageResistances)
                if (m.damageImmunities.isNotBlank()) ResultLine("Damage immunities", m.damageImmunities)
                if (m.senses.isNotBlank()) ResultLine("Senses", m.senses)
                if (m.languages.isNotBlank()) ResultLine("Languages", m.languages)
                ResultLine("Challenge", "${ChallengeRating.format(r.finalCr)} (${r.xp} XP)")
                if (m.traits.isNotEmpty()) {
                    SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                    m.traits.forEach { (name, text) ->
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Text(text, style = MaterialTheme.typography.bodySmall)
                    }
                }
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                Text("Actions", style = MaterialTheme.typography.titleLarge)
                m.actions.forEach { a ->
                    Text(a.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        buildString {
                            a.attackBonus?.let { append("${"Attack"} ${sign(it)}, ${a.reach}. ") }
                            append("${a.damageExpression} ${a.damageType} (⌀ ${fmt(a.averageDamage, 1)})")
                            if (a.attacksPerRound > 1) append(" ×${a.attacksPerRound}")
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (a.description.isNotBlank()) Text(a.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 4.dp))
                Text("Damage against typical armour classes", style = MaterialTheme.typography.titleLarge)
                listOf(13, 15, 17, 19).forEach { ac ->
                    val dpr = m.actions.maxOfOrNull { CombatEstimate.damagePerRound(it, ac) } ?: 0.0
                    ResultLine("Against AC $ac", "${fmt(dpr, 1)} per round")
                }
                if (m.notes.isNotBlank()) Text(m.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

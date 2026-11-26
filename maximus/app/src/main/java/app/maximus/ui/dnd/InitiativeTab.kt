package app.maximus.ui.dnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.dnd.data.DndCharacterEntity
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.data.DndRepository
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.dnd.domain.Combatant
import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceRoller
import app.maximus.dnd.domain.Initiative
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.strongman.NumberField
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InitiativeTab(
    repository: DndRepository,
    initiative: Initiative?,
    characters: List<DndCharacterEntity>,
    scope: CoroutineScope
) {
    var adding by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var hpId by rememberSaveable { mutableStateOf<String?>(null) }
    var reset by rememberSaveable { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    fun change(transform: (Initiative) -> Initiative) {
        failed = false
        scope.launch {
            try {
                repository.updateInitiative(transform)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                failed = true
            }
        }
    }

    if (initiative == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
        return
    }

    val active = initiative.active
    val ordered = initiative.ordered
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (failed) item {
            Text(stringResource(R.string.dnd_combat_save_error), color = MaterialTheme.colorScheme.error)
        }
        if (ordered.isNotEmpty()) stickyHeader {
            DndCard(
                stringResource(R.string.dnd_combat_round, initiative.round),
                Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                Text(
                    if (active == null) stringResource(R.string.dnd_combat_not_started)
                    else stringResource(R.string.dnd_combat_turn, active.name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (active != null) {
                        GlyphButton(
                            Glyph.CHEVRON_LEFT, stringResource(R.string.dnd_combat_previous),
                            { change(Initiative::previous) },
                            enabled = initiative.round > 1 || ordered.first().id != active.id
                        )
                    }
                    Button(onClick = { change(Initiative::next) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                        Text(stringResource(if (active == null) R.string.dnd_combat_start else R.string.dnd_combat_next))
                    }
                }
            }
        }
        if (ordered.isEmpty()) item { EmptyState(stringResource(R.string.dnd_combat_empty)) }
        item {
            Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.dnd_combat_new))
            }
        }
        items(ordered, key = Combatant::id) { entry ->
            val current = entry.id == active?.id
            val colors = MaterialTheme.colorScheme
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (current) colors.primaryContainer else colors.surfaceContainerLow),
                border = BorderStroke(1.dp, if (current) colors.primary else colors.outlineVariant)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
                    if (current) Text(stringResource(R.string.dnd_combat_active), style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(entry.initiative.toString(), style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"))
                        Column(Modifier.weight(1f)) {
                            Text(entry.name, style = MaterialTheme.typography.titleMedium)
                            if (entry.maxHp != null) {
                                val hp = entry.hp ?: entry.maxHp
                                Text(
                                    stringResource(R.string.dnd_combat_hp, hp, entry.maxHp),
                                    color = if (hp == 0) colors.error else colors.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = { editingId = entry.id }) { Text(stringResource(R.string.edit)) }
                    }
                    if (entry.maxHp != null) {
                        OutlinedButton(onClick = { hpId = entry.id }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.dnd_combat_hp_change))
                        }
                    }
                }
            }
        }
        if (ordered.isNotEmpty()) item {
            TextButton(onClick = { reset = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dnd_combat_reset))
            }
        }
    }

    if (adding) {
        CombatantDialog(characters = characters, onDismiss = { adding = false }, onSave = { entry ->
            adding = false
            change { it.add(entry) }
        })
    }
    initiative.combatants.firstOrNull { it.id == editingId }?.let { entry ->
        CombatantDialog(
            entry = entry,
            onDismiss = { editingId = null },
            onSave = { updated -> editingId = null; change { it.change(entry.id) { updated } } },
            onSelect = { editingId = null; change { it.select(entry.id) } },
            onRemove = { editingId = null; change { it.remove(entry.id) } }
        )
    }
    initiative.combatants.firstOrNull { it.id == hpId }?.let { entry ->
        HpDialog(entry, onDismiss = { hpId = null }, onApply = { delta ->
            hpId = null
            change { state -> state.change(entry.id) { it.changeHp(delta) } }
        })
    }
    if (reset) {
        AlertDialog(
            onDismissRequest = { reset = false },
            title = { Text(stringResource(R.string.dnd_combat_reset)) },
            text = { Text(stringResource(R.string.dnd_combat_reset_message)) },
            confirmButton = {
                TextButton(onClick = { reset = false; change { Initiative() } }) {
                    Text(stringResource(R.string.dnd_combat_reset), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { reset = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CombatantDialog(
    entry: Combatant? = null,
    characters: List<DndCharacterEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Combatant) -> Unit,
    onSelect: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    var name by rememberSaveable(entry?.id) { mutableStateOf(entry?.name.orEmpty()) }
    var initiative by rememberSaveable(entry?.id) { mutableStateOf(entry?.initiative?.toString().orEmpty()) }
    var maxHp by rememberSaveable(entry?.id) { mutableStateOf(entry?.maxHp?.toString().orEmpty()) }
    var hp by rememberSaveable(entry?.id) { mutableStateOf(entry?.hp?.toString().orEmpty()) }
    var modifier by rememberSaveable { mutableStateOf<Int?>(null) }
    val order = initiative.trim().toIntOrNull()
    val maximum = maxHp.trim().toIntOrNull()?.takeIf { it > 0 }
    val current = hp.trim().toIntOrNull()
    val valid = name.isNotBlank() && order != null && (maxHp.isBlank() || (
        maximum != null && (hp.isBlank() || current != null && current in 0..maximum)
    ))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (entry == null) R.string.dnd_combat_new else R.string.dnd_combat_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (characters.isNotEmpty()) {
                    CharacterMenu(characters, stringResource(R.string.dnd_combat_import)) { character ->
                        name = character.name
                        initiative = ""
                        modifier = CharacterBuilder.build(DndCodec.decodeCharacter(character.payload)).initiative
                    }
                }
                OutlinedTextField(
                    name, { name = it }, label = { Text(stringResource(R.string.dnd_combat_name)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                // A manual initiative total can be negative, so this field permits a leading minus.
                OutlinedTextField(
                    initiative, { initiative = it }, label = { Text(stringResource(R.string.dnd_nav_initiative)) },
                    singleLine = true, isError = initiative.isNotBlank() && order == null,
                    modifier = Modifier.fillMaxWidth()
                )
                val bonus = modifier
                if (bonus != null) {
                    OutlinedButton(
                        onClick = { initiative = DiceRoller.roll(DiceParser.parse(d20Expression(bonus, 0))).total.toString() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.dnd_combat_import_die, sign(bonus))) }
                }
                NumberField(stringResource(R.string.dnd_combat_hp_max), maxHp, { maxHp = it },
                    modifier = Modifier.fillMaxWidth(), integer = true)
                if (maximum != null) {
                    NumberField(stringResource(R.string.dnd_combat_hp_current), hp, { hp = it },
                        modifier = Modifier.fillMaxWidth(), integer = true)
                }
                if (onSelect != null) OutlinedButton(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dnd_combat_select))
                }
                if (onRemove != null) TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dnd_combat_remove), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(enabled = valid, onClick = {
                if (order != null) onSave(Combatant(entry?.id ?: UUID.randomUUID().toString(), name.trim(), order,
                    maximum, maximum?.let { current ?: it }))
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
internal fun HpDialog(entry: Combatant, onDismiss: () -> Unit, onApply: (Int) -> Unit) {
    var amount by rememberSaveable(entry.id) { mutableStateOf("") }
    val value = amount.trim().toIntOrNull()?.takeIf { it > 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val maximum = entry.maxHp
                if (maximum != null) Text(stringResource(R.string.dnd_combat_hp, entry.hp ?: maximum, maximum))
                NumberField(stringResource(R.string.dnd_combat_amount), amount, { amount = it },
                    modifier = Modifier.fillMaxWidth(), integer = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { value?.let { onApply(-it) } }, enabled = value != null,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.dnd_combat_damage)) }
                    OutlinedButton(
                        onClick = { value?.let(onApply) }, enabled = value != null,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.dnd_combat_heal)) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

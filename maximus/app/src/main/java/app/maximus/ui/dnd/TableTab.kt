package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.dnd.data.DndCharacterEntity
import app.maximus.dnd.data.DndRepository
import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceRoller
import app.maximus.dnd.domain.Initiative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun TableTab(
    repository: DndRepository,
    characters: List<DndCharacterEntity>,
    initiative: Initiative?,
    scope: CoroutineScope,
    onOpenCharacter: (Long) -> Unit,
    onOpenDice: () -> Unit,
    onOpenCombat: () -> Unit,
    onOpenCharacters: () -> Unit
) {
    val history by repository.rolls.collectAsState(initial = emptyList())
    var rollTotal by rememberSaveable { mutableStateOf<Int?>(null) }
    var rollExpression by rememberSaveable { mutableStateOf("") }
    var rollDetail by rememberSaveable { mutableStateOf("") }
    var rollTime by rememberSaveable { mutableLongStateOf(0L) }
    var natural by rememberSaveable { mutableIntStateOf(0) }
    var modifier by rememberSaveable { mutableIntStateOf(0) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val historyLabel = stringResource(R.string.dnd_table_roll_label)
    val latest = history.firstOrNull()
    val showLocal = rollTotal != null && (latest == null ||
        latest.epochMillis <= rollTime ||
        latest.expression == rollExpression && latest.total == rollTotal && latest.detail == rollDetail)
    val character = characters.firstOrNull { it.id == selectedId } ?: characters.firstOrNull()

    fun roll(expression: String, isD20: Boolean) {
        val result = DiceRoller.roll(DiceParser.parse(expression), Random.Default)
        val detail = detailText(result)
        rollTime = System.currentTimeMillis()
        rollTotal = result.total
        rollExpression = expression
        rollDetail = detail
        natural = if (isD20) result.pools.firstOrNull()?.dice?.firstOrNull { it.kept }?.value ?: 0 else 0
        scope.launch { repository.addRoll(expression, historyLabel, result.total, detail) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DndCard(stringResource(R.string.dnd_table_roll)) {
                Column(
                    Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val result = if (showLocal) rollTotal else latest?.total
                    Text(stringResource(R.string.dnd_table_last_roll), style = MaterialTheme.typography.labelMedium)
                    if (result == null) {
                        BodyText(stringResource(R.string.dnd_table_no_roll), muted = true)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(result.toString(), style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    stringResource(
                                        R.string.dnd_table_roll_detail,
                                        if (showLocal) rollExpression else latest?.expression.orEmpty(),
                                        if (showLocal) rollDetail else latest?.detail.orEmpty()
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (showLocal && (natural == 20 || natural == 1)) {
                                    Text(
                                        stringResource(if (natural == 20) R.string.dnd_table_nat20 else R.string.dnd_table_nat1),
                                        color = if (natural == 20) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.dnd_table_modifier), modifier = Modifier.weight(1f))
                    Stepper(modifier, { modifier = it }, min = -20, max = 30)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(R.string.dnd_table_normal, R.string.dnd_table_advantage, R.string.dnd_table_disadvantage)
                        .forEachIndexed { index, label ->
                            val advantage = when (index) { 1 -> 1; 2 -> -1; else -> 0 }
                            Button(
                                onClick = { roll(d20Expression(modifier, advantage), isD20 = true) },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 3.dp, vertical = 8.dp)
                            ) {
                                Text(stringResource(label), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                }
                listOf(listOf(4, 6, 8), listOf(10, 12, 100)).forEach { dice ->
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dice.forEach { sides ->
                            OutlinedButton(
                                onClick = { roll("1d$sides", isD20 = false) },
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) { Text(stringResource(R.string.dnd_table_die, sides)) }
                        }
                    }
                }
                TextButton(onClick = onOpenDice, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.dnd_table_advanced))
                }
            }
        }
        item {
            DndCard(stringResource(R.string.dnd_nav_initiative)) {
                val active = initiative?.active
                if (active != null) {
                    Text(stringResource(R.string.dnd_combat_round, initiative.round), style = MaterialTheme.typography.labelMedium)
                    Text(stringResource(R.string.dnd_combat_turn, active.name), style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(onClick = onOpenCombat, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.dnd_nav_combat))
                }
            }
        }
        item {
            DndCard(stringResource(R.string.dnd_nav_characters)) {
                if (character == null) {
                    BodyText(stringResource(R.string.dnd_table_no_characters), muted = true)
                    TextButton(onClick = onOpenCharacters) { Text(stringResource(R.string.dnd_nav_characters)) }
                } else {
                    CharacterMenu(characters, character.name, onSelected = { selectedId = it.id })
                    BodyText(character.summary, muted = true)
                    OutlinedButton(onClick = { onOpenCharacter(character.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.dnd_sheet_open_combat))
                    }
                }
            }
        }
    }
}

@Composable
internal fun CharacterMenu(
    characters: List<DndCharacterEntity>,
    label: String,
    onSelected: (DndCharacterEntity) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            characters.forEach { character ->
                DropdownMenuItem(
                    text = { Text(character.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                    onClick = { open = false; onSelected(character) }
                )
            }
        }
    }
}

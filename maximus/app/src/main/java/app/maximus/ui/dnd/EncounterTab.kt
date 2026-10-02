@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndMonsterEntity
import app.maximus.dnd.domain.ChallengeRating
import app.maximus.dnd.domain.Encounter
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt

@Composable
fun EncounterTab(services: AppServices) {
    val monsters by services.dnd.monsters.collectAsState(initial = emptyList<DndMonsterEntity>())
    var partySize by rememberSaveable { mutableIntStateOf(4) }
    var partyLevel by rememberSaveable { mutableIntStateOf(3) }
    var picks by remember { mutableStateOf<Map<Long, Int>>(emptyMap()) }

    val chosen = monsters.filter { (picks[it.id] ?: 0) > 0 }
    val xpList = chosen.flatMap { m -> List(picks.getValue(m.id)) { ChallengeRating.row(m.challengeRating).xp } }
    val levels = List(partySize) { partyLevel }
    val thresholds = Encounter.thresholds(levels)
    val (difficulty, adjusted) = Encounter.difficulty(levels, xpList)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard("Party") {
                Text("Party size: $partySize", style = MaterialTheme.typography.labelLarge)
                Slider(value = partySize.toFloat(), onValueChange = { partySize = it.toInt() }, valueRange = 1f..8f, steps = 6)
                Text("Party level: $partyLevel", style = MaterialTheme.typography.labelLarge)
                Slider(value = partyLevel.toFloat(), onValueChange = { partyLevel = it.toInt() }, valueRange = 1f..20f, steps = 18)
                ResultLine("Easy", "${thresholds.easy} XP")
                ResultLine("Medium", "${thresholds.medium} XP")
                ResultLine("Hard", "${thresholds.hard} XP")
                ResultLine("Deadly", "${thresholds.deadly} XP")
            }
        }
        item {
            SectionCard("Assessment") {
                ResultLine("Raw XP", "${xpList.sum()} XP")
                ResultLine("Group multiplier", "× ${fmt(Encounter.multiplier(xpList.size, partySize), 1)}")
                ResultLine("Adjusted XP", "$adjusted XP")
                Text(
                    difficultyLabel(difficulty),
                    style = MaterialTheme.typography.headlineSmall,
                    color = when (difficulty) {
                        Encounter.Difficulty.DEADLY -> MaterialTheme.colorScheme.tertiary
                        Encounter.Difficulty.HARD -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                if (xpList.isNotEmpty()) {
                    ResultLine("XP per character (raw)", "${xpList.sum() / partySize.coerceAtLeast(1)} XP")
                }
                Text("The multiplier rises with the number of monsters because more enemies mean more actions per round. Award XP stays unadjusted; only the difficulty assessment uses the adjusted value.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (monsters.isEmpty()) item { EmptyState("Create monsters first to build an encounter.") }
        items(monsters, key = { it.id }) { m ->
            val n = picks[m.id] ?: 0
            PlateCard {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(m.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text("CR ${ChallengeRating.format(m.challengeRating)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                    TextButton(onClick = { picks = picks + (m.id to (n - 1).coerceAtLeast(0)) }) { Text("−") }
                    Text("$n", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { picks = picks + (m.id to (n + 1).coerceAtMost(20)) }) { Text("+") }
                }
            }
        }
    }
}

private fun difficultyLabel(d: Encounter.Difficulty): String = when (d) {
    Encounter.Difficulty.TRIVIAL -> "Trivial"
    Encounter.Difficulty.EASY -> "Easy"
    Encounter.Difficulty.MEDIUM -> "Medium"
    Encounter.Difficulty.HARD -> "Hard"
    Encounter.Difficulty.DEADLY -> "Deadly"
}

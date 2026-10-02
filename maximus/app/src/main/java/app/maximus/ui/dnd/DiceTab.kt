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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndRollEntity
import app.maximus.dnd.domain.DiceNode
import app.maximus.dnd.domain.DiceParseException
import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceProbability
import app.maximus.dnd.domain.DiceRoller
import app.maximus.dnd.domain.Distribution
import app.maximus.dnd.domain.RollResult
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawBarChart
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlinx.coroutines.launch

private val PRESETS = listOf("1d20", "1d20+5", "2d20kh1", "2d20kl1", "4d6kh3", "1d4", "1d6", "1d8", "1d10", "1d12", "2d6", "8d6", "1d100", "4dF", "1d6!", "2d6ro<2")

@Composable
fun DiceTab(services: AppServices) {
    val scope = rememberCoroutineScope()
    val history by services.dnd.rolls.collectAsState(initial = emptyList<DndRollEntity>())
    var expression by rememberSaveable { mutableStateOf("1d20") }
    var label by rememberSaveable { mutableStateOf("") }
    var count by rememberSaveable { mutableIntStateOf(1) }
    var showStats by rememberSaveable { mutableStateOf(true) }
    var lastRolls by remember { mutableStateOf<List<RollResult>>(emptyList()) }
    val random = remember { Random(System.nanoTime()) }

    val parsed: Result<DiceNode> = remember(expression) { runCatching { DiceParser.parse(expression) } }
    val node = parsed.getOrNull()
    val distribution: Distribution? = remember(node) {
        node?.let { runCatching { DiceProbability.of(it) }.getOrNull() }
    }
    val truncation = remember(node) { node?.let { DiceProbability.truncationError(it) } ?: 0.0 }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard("Dice roll") {
                OutlinedTextField(
                    expression, { expression = it },
                    label = { Text("Expression") },
                    singleLine = true,
                    isError = parsed.isFailure,
                    supportingText = {
                        val e = parsed.exceptionOrNull()
                        Text(
                            when (e) {
                                null -> "e.g. 2d6+3, 4d6kh3, 2d20kl1, 1d6!, 8d6/2 — case-insensitive, W works instead of d"
                                is DiceParseException -> "Error at position ${e.position + 1}: ${e.message ?: ""}"
                                else -> e.message ?: "Invalid dice expression."
                            }
                        )
                    },
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(label, { label = it }, label = { Text("Label (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Number of rolls: $count", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 3, 4, 6, 8, 10, 20).forEach { n -> FilterChip(count == n, { count = n }, label = { Text("×$n") }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Button(
                        enabled = node != null,
                        onClick = {
                            val n = node ?: return@Button
                            val results = List(count) { DiceRoller.roll(n, random) }
                            lastRolls = results
                            scope.launch {
                                results.forEach { r -> services.dnd.addRoll(expression, label, r.total, detailOf(r)) }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Roll") }
                    OutlinedButton(onClick = { showStats = !showStats }) { Text(if (showStats) "Hide statistics" else "Statistics") }
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PRESETS.forEach { p -> AssistChip(onClick = { expression = p }, label = { Text(p, fontFamily = FontFamily.Monospace) }) }
            }
        }
        if (lastRolls.isNotEmpty()) item { ResultCard(lastRolls) }
        if (showStats && distribution != null) item { StatsCard(distribution, truncation) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("History", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { scope.launch { services.dnd.clearHistory() } }) { Text("Clear") }
            }
        }
        if (history.isEmpty()) item { EmptyState("No rolls yet.") }
        items(history, key = { it.id }) { h -> HistoryRow(h, onFavorite = { scope.launch { services.dnd.toggleFavorite(h) } }, onReuse = { expression = h.expression }) }
    }
}

private fun detailOf(r: RollResult): String = r.pools.joinToString("; ") { p ->
    p.dice.joinToString(" ") { d ->
        val v = d.value.toString()
        when {
            !d.kept -> "($v)"
            d.rerolled -> "$v↻"
            d.exploded -> "$v!"
            else -> v
        }
    }
}

@Composable
private fun ResultCard(results: List<RollResult>) {
    SectionCard("Result") {
        results.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    "${r.total}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(detailOf(r), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (results.size > 1) {
            val totals = results.map { it.total }
            ResultLine("Sum", "${totals.sum()}")
            ResultLine("Mean of the rolls", fmt(totals.average(), 2))
            ResultLine("Lowest / highest roll", "${totals.min()} / ${totals.max()}")
        }
        Text("Dropped dice are in brackets, ↻ means rerolled, ! exploded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatsCard(d: Distribution, truncation: Double) {
    SectionCard("Exact distribution") {
        ResultLine("Expected value", fmt(d.mean, 3))
        ResultLine("Standard deviation", fmt(d.stdDev, 3))
        ResultLine("Range", "${d.min} … ${d.max}")
        ResultLine("Median", "${d.quantile(0.5)}")
        ResultLine("Number of possible outcomes", "${d.denominator}")
        if (truncation > 1e-12) {
            Text("Exploding dice are truncated after six levels; the neglected probability is below ${fmt(100 * truncation, 4)} %", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        val support = d.support
        if (support.size in 2..120) {
            ChartFrame("Probability per outcome", "maximus-wuerfelverteilung.png", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                drawBarChart(
                    hits, m, c,
                    labels = support.map { it.toString() },
                    values = support.map { d.probability(it) },
                    tooltips = support.map { v -> "$v: ${fmt(100 * d.probability(v), 2)} %  ≥ ${fmt(100 * d.atLeast(v), 1)} %" }
                )
            }
        }
        Text("The distribution is computed exactly with integer fractions, not simulated. Tapping also shows the probability of reaching at least that value.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryRow(h: DndRollEntity, onFavorite: () -> Unit, onReuse: () -> Unit) {
    val time = remember(h.epochMillis) {
        Instant.ofEpochMilli(h.epochMillis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"))
    }
    PlateCard(onClick = onReuse) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${h.total}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(listOf(h.label, h.expression).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                Text("$time   ${h.detail}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
            }
            GlyphButton(if (h.favorite) Glyph.STAR_FILLED else Glyph.STAR, "Keep", onFavorite, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

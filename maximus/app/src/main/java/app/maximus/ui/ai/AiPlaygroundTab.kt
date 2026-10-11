@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiGame
import app.maximus.ai.domain.AiGlossary
import app.maximus.ai.domain.AiRules
import app.maximus.chat.domain.Focus
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.SectionCard

/** "Spielwiese": the nine learning games with best scores; a game opens inside the tab, back returns. */
@Composable
fun AiPlaygroundTab(ctx: AiContext) {
    var gameName by rememberSaveable { mutableStateOf<String?>(null) }
    val game = gameName?.let { n -> AiGame.entries.firstOrNull { it.name == n } }
    BackHandler(enabled = game != null) { gameName = null }
    if (game != null) AiGameHost(ctx, game, onClose = { gameName = null })
    else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Die Spielwiese") {
                Text("Neun Spiele, in denen du Algorithmen mit den Händen begreifst: Lernrate wählen, Perzeptron trennen, Cluster schieben, " +
                    "Schwellen regeln, Bäume bauen, einen Q-Learning-Agenten trainieren. Jedes Spiel gibt 5 + Punkte/5 XP, ein neuer Bestwert 25 extra.",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Gespielt: ${ctx.progress.games.size}/${AiGame.entries.size}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiGame.entries.forEach { g -> GameTile(g, ctx.progress.games[g.name]) { gameName = g.name } }
            }
        }
    }
}

@Composable
private fun GameTile(g: AiGame, best: Int?, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (best != null && best >= 80) accent else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(160.dp).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(g.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(g.tagline, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
            Text(if (best == null) "Noch nicht gespielt" else "Bestwert $best", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Frame of every game: title, links to the bite and to Maximus, and the finish callback with XP. */
@Composable
private fun AiGameHost(ctx: AiContext, game: AiGame, onClose: () -> Unit) {
    val finish: (Int) -> Unit = { score -> ctx.act { AiRules.gameFinished(it, game, score, ctx.today) } }
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClose) { Text("← Spiele") }
            Text(game.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { ctx.openBite(game.chapterKey) }) { Text("Happen") }
            TextButton(onClick = {
                ctx.ask(AskRequest(
                    "Spiel „${game.title}“", Focus.CODE,
                    listOf("Erkläre mir die Idee hinter diesem Spiel", "Welche Strategie bringt die meisten Punkte – und warum?",
                        "Wo begegnet mir das im IBM AI Engineering Kurs?")
                ))
            }) { Text("Maximus") }
        }
        when (game) {
            AiGame.TECHNIQUE -> TechniqueGameScreen(finish)
            AiGame.GRADIENT -> GradientGameScreen(finish)
            AiGame.PERCEPTRON -> PerceptronGameScreen(finish)
            AiGame.KMEANS -> KMeansGameScreen(finish)
            AiGame.OVERFIT -> OverfitGameScreen(finish)
            AiGame.THRESHOLD -> ThresholdGameScreen(finish)
            AiGame.TREE -> TreeGameScreen(finish)
            AiGame.QLEARN -> QLearnGameScreen(finish)
            AiGame.GLOSSARY -> GlossaryDuelScreen(finish)
        }
    }
}

/** "Glossar": every term with its definition, linked to the bite that explains it. */
@Composable
fun GlossaryTab(ctx: AiContext) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    val list = remember(query, group) {
        AiGlossary.terms.filter { t ->
            (group == null || t.group == group) && (query.isBlank() || t.term.contains(query, true) || t.definition.contains(query, true))
        }.sortedBy { it.term.lowercase() }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("${list.size} von ${AiGlossary.terms.size} Begriffen", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(query, { query = it }, label = { Text("Suchen") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                FilterChip(group == null, { group = null }, label = { Text("Alle") })
                AiGlossary.groups.forEach { g -> FilterChip(group == g, { group = if (group == g) null else g }, label = { Text(g) }) }
            }
        }
        items(list, key = { it.term }) { t ->
            PlateCard(onClick = { ctx.openBite(t.chapterKey) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(t.term, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(t.definition, style = MaterialTheme.typography.bodyMedium)
                    Text("→ ${AiCourse.byKey[t.chapterKey]?.fullTitle ?: t.chapterKey}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

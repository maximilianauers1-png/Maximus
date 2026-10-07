@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import app.maximus.chat.domain.Focus
import app.maximus.lab.domain.AiCurriculum
import app.maximus.lab.domain.AiGame
import app.maximus.lab.domain.AiGlossary
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.QuizEngine
import app.maximus.lab.domain.Topic
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.SectionCard

/**
 * "AI-Spielwiese": the AI learning path (bites in order, progress per course), the glossary and the
 * learning games. Games and the glossary open inside the tab; back returns to the overview.
 */
@Composable
fun AiPlaygroundTab(ctx: LabContext) {
    var gameName by rememberSaveable { mutableStateOf<String?>(null) }
    var glossary by rememberSaveable { mutableStateOf(false) }
    val game = gameName?.let { n -> AiGame.entries.firstOrNull { it.name == n } }
    BackHandler(enabled = game != null || glossary) { gameName = null; glossary = false }

    when {
        game != null -> AiGameHost(ctx, game, onClose = { gameName = null })
        glossary -> GlossaryScreen(ctx, onClose = { glossary = false })
        else -> PlaygroundHome(ctx, onGame = { gameName = it.name }, onGlossary = { glossary = true })
    }
}

@Composable
private fun PlaygroundHome(ctx: LabContext, onGame: (AiGame) -> Unit, onGlossary: () -> Unit) {
    val p = ctx.progress
    val read = AiCurriculum.keys.count { it in p.readChapters }
    val total = AiCurriculum.keys.size
    val nextKey = AiCurriculum.next(p.readChapters)
    val next = nextKey?.let { Compendium.byKey[it] }
    var openCourse by rememberSaveable { mutableStateOf<String?>(null) }
    val accent = topicColor(Topic.AI)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PlateCard {
                Column(Modifier.padding(16.dp)) {
                    Text("AI-Lernpfad", style = MaterialTheme.typography.headlineSmall, color = accent)
                    Text("Von null bis AI Engineer in $total Happen – ein Kapitel nach dem anderen.", style = MaterialTheme.typography.bodyMedium)
                    ProgressBar(read.toFloat() / total, accent, Modifier.padding(top = 10.dp))
                    Text("$read von $total Happen gelesen", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    if (next != null) {
                        Button(onClick = { ctx.openChapter(next.key) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            Text("Weiter: ${next.title}", maxLines = 2)
                        }
                        OutlinedButton(
                            onClick = {
                                ctx.startQuiz(QuizConfig(Topic.AI, QuizEngine.difficultyFor(p.topic(Topic.AI).mastery), 10, course = next.course.ifEmpty { null }))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Quiz zum Kurs „${next.course}“") }
                    } else {
                        Text("Alle Happen gelesen – du bist durch den Kurs! Wiederhole mit Quiz und Spielen.", style = MaterialTheme.typography.bodyMedium,
                            color = accent, modifier = Modifier.padding(top = 8.dp))
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("AI-Fragen", "${QuizEngine.bank.count { it.topic == Topic.AI }}+", Modifier.weight(1f))
                        StatTile("Begriffe", "${AiGlossary.terms.size}", Modifier.weight(1f))
                        StatTile("Spiele", "${p.games.size}/${AiGame.entries.size}", Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Text("Spiele", style = MaterialTheme.typography.titleLarge, color = accent)
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AiGame.entries.forEach { g -> GameTile(g, p.games[g.name]) { onGame(g) } }
            }
        }
        item {
            SectionCard("AI-Glossar") {
                Text("${AiGlossary.terms.size} Begriffe von „Feature“ bis „Konzeptdrift“, einfach erklärt und mit dem passenden Happen verlinkt.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onGlossary, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Glossar öffnen") }
            }
        }
        item { Text("Kurse", style = MaterialTheme.typography.titleLarge, color = accent) }
        items(AiCurriculum.path, key = { it.first }) { (course, keys) ->
            val done = keys.count { it in p.readChapters }
            PlateCard(onClick = { openCourse = if (openCourse == course) null else course }) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(course, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text("$done/${keys.size}", style = MaterialTheme.typography.labelLarge, color = accent)
                    }
                    ProgressBar(done.toFloat() / keys.size, accent, Modifier.padding(top = 6.dp), height = 5.dp)
                    if (openCourse == course) {
                        keys.mapNotNull { Compendium.byKey[it] }.forEach { ch ->
                            Row(
                                Modifier.fillMaxWidth().clickable { ctx.openChapter(ch.key) }.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (ch.key in p.readChapters) "✔" else "○", color = if (ch.key in p.readChapters) accent else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.width(24.dp))
                                Text(ch.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            }
                        }
                        TextButton(onClick = {
                            ctx.startQuiz(QuizConfig(Topic.AI, QuizEngine.difficultyFor(p.topic(Topic.AI).mastery), 10, course = course))
                        }) { Text("Quiz zu diesem Kurs") }
                    }
                }
            }
        }
        item {
            Text("XP: Spiele geben 5 + Punkte/5 XP, ein neuer Bestwert zusätzlich 25. Abzeichen warten für 10, 30 und alle Happen.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun GameTile(g: AiGame, best: Int?, onClick: () -> Unit) {
    val accent = topicColor(Topic.AI)
    Surface(
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (best != null && best >= 80) accent else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(160.dp).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(g.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(g.tagline, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
            Text(if (best == null) "Noch nicht gespielt" else "Bestwert $best", style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Frame of every game: title, links to the chapter and to Maximus, and the finish callback with XP. */
@Composable
private fun AiGameHost(ctx: LabContext, game: AiGame, onClose: () -> Unit) {
    val finish: (Int) -> Unit = { score -> ctx.act { LabRules.gameFinished(it, game, score, ctx.today) } }
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onClose) { Text("← Spiele") }
            Text(game.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { ctx.openChapter(game.chapterKey) }) { Text("Kapitel") }
            TextButton(onClick = {
                ctx.ask(AskRequest(
                    "Spiel „${game.title}“", Focus.SCIENCE,
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

@Composable
private fun GlossaryScreen(ctx: LabContext, onClose: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf<String?>(null) }
    val list = remember(query, group) {
        AiGlossary.terms.filter { t ->
            (group == null || t.group == group) &&
                (query.isBlank() || t.term.contains(query, true) || t.definition.contains(query, true))
        }.sortedBy { it.term.lowercase() }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text("← Spielwiese") }
                Text("AI-Glossar · ${list.size} Begriffe", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(query, { query = it }, label = { Text("Suchen") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                FilterChip(group == null, { group = null }, label = { Text("Alle") })
                AiGlossary.groups.forEach { g -> FilterChip(group == g, { group = if (group == g) null else g }, label = { Text(g) }) }
            }
        }
        items(list, key = { it.term }) { t ->
            PlateCard(onClick = { ctx.openChapter(t.chapterKey) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(t.term, style = MaterialTheme.typography.titleSmall, color = topicColor(Topic.AI), fontWeight = FontWeight.Bold)
                    Text(t.definition, style = MaterialTheme.typography.bodyMedium)
                    Text("→ ${Compendium.byKey[t.chapterKey]?.title ?: t.chapterKey}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

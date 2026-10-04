package app.maximus.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Calculators
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.QuizEngine
import app.maximus.lab.domain.Topic
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.SectionCard

@Composable
fun CompendiumTab(ctx: LabContext) {
    var topic by rememberSaveable { mutableStateOf<Topic?>(null) }
    var course by rememberSaveable { mutableStateOf<String?>(null) }
    var search by rememberSaveable { mutableStateOf("") }
    val chapters = remember(topic, course, search) {
        Compendium.all.filter { ch ->
            (topic == null || ch.topic == topic) && (course == null || ch.course == course) && (search.isBlank() ||
                ch.title.contains(search, true) || ch.summary.contains(search, true) ||
                ch.sections.any { s -> s.title.contains(search, true) || s.body.contains(search, true) || s.formulas.any { it.name.contains(search, true) } })
        }
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column {
                TopicChips(topic, { topic = it; course = null })
                CourseChips(topic, course) { course = it }
            }
        }
        item {
            OutlinedTextField(search, { search = it }, label = { Text("Suche in Texten und Formeln") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (chapters.isEmpty()) item { EmptyState("Nichts gefunden.") }
        items(chapters, key = { it.key }) { ch ->
            val read = ch.key in ctx.progress.readChapters
            PlateCard(onClick = { ctx.openChapter(ch.key) }) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ch.topic.title + if (ch.course.isNotEmpty()) " · ${ch.course}" else "",
                            style = MaterialTheme.typography.labelMedium, color = topicColor(ch.topic), modifier = Modifier.weight(1f)
                        )
                        Text(
                            Compendium.levelLabel(ch.level) + if (read) "  ✓" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (read) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(ch.title, style = MaterialTheme.typography.titleLarge)
                    Text(ch.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${ch.sections.size} Abschnitte · ${ch.formulas.size} Formeln" + if (ch.calculatorKeys.isNotEmpty()) " · ${ch.calculatorKeys.size} Rechner" else "",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun ChapterScreen(ctx: LabContext, key: String) {
    val ch = Compendium.byKey[key] ?: run { EmptyState("Kapitel nicht gefunden."); return }
    val accent = topicColor(ch.topic)
    val read = ch.key in ctx.progress.readChapters
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text("${ch.topic.title} · ${Compendium.levelLabel(ch.level)}", style = MaterialTheme.typography.labelLarge, color = accent)
                Text(ch.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(ch.sections) { s ->
            SectionCard(s.title) {
                RichBody(s.body, accent)
                if (s.formulas.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        s.formulas.forEach { f -> FormulaBox(f.expr, name = f.name, note = f.note, accent = accent) }
                    }
                }
            }
        }
        if (ch.calculatorKeys.isNotEmpty()) {
            item {
                SectionCard("Ausprobieren") {
                    ch.calculatorKeys.mapNotNull { Calculators.byKey[it] }.forEach { c ->
                        OutlinedButton(onClick = { ctx.openCalculator(c.key) }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) { Text(c.title) }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (read) {
                    Text("Kapitel abgeschlossen ✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(onClick = { ctx.act { p -> LabRules.chapterRead(p, ch.key, ctx.today) } }, modifier = Modifier.fillMaxWidth()) {
                        Text("Kapitel abschließen  +15 XP")
                    }
                }
                OutlinedButton(
                    onClick = {
                        ctx.startQuiz(QuizConfig(ch.topic, QuizEngine.difficultyFor(ctx.progress.topic(ch.topic).mastery), 8, course = ch.course.ifEmpty { null }))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Wissen prüfen: Training ${ch.course.ifEmpty { ch.topic.title }}") }
                OutlinedButton(onClick = { ctx.ask(labRequest(ch)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Maximus zu diesem Kapitel fragen")
                }
            }
        }
    }
}

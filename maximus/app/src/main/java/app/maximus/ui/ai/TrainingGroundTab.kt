@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.QKind
import app.maximus.ui.strongman.SectionCard

/** "Übungsplatz": free rounds over one lesson or the whole course, filtered by kind of question. */
@Composable
fun TrainingGroundTab(ctx: AiContext) {
    val p = ctx.progress
    var lesson by rememberSaveable { mutableStateOf<Int?>(null) }
    var kind by rememberSaveable { mutableStateOf<QKind?>(null) }
    var difficulty by rememberSaveable { mutableIntStateOf(0) } // 0 = adaptive
    var count by rememberSaveable { mutableIntStateOf(10) }
    val mastery = lesson?.let { p.lesson(it).mastery } ?: AiCourse.lessons.map { p.lesson(it.number).mastery }.average()
    val effective = if (difficulty == 0) difficultyFor(mastery) else difficulty
    val pool = AiCourse.questions.count { q -> (lesson == null || AiCourse.lessonOf(q.bite) == lesson) && (kind == null || q.kind == kind) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Übungsrunde") {
                Text("Lektion", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(lesson == null, { lesson = null }, label = { Text("Alle") })
                    AiCourse.lessons.forEach { l -> FilterChip(lesson == l.number, { lesson = l.number }, label = { Text("${l.number}") }) }
                }
                Text(lesson?.let { AiCourse.lesson(it).title } ?: "Der ganze Kurs", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Art der Fragen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(kind == null, { kind = null }, label = { Text("Gemischt") })
                    QKind.entries.forEach { k -> FilterChip(kind == k, { kind = k }, label = { Text("${kindIcon(k)} ${k.label}") }) }
                }
                Text("Schwierigkeit", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(difficulty == 0, { difficulty = 0 }, label = { Text("Adaptiv (${diamonds(difficultyFor(mastery))})") })
                    (1..3).forEach { d -> FilterChip(difficulty == d, { difficulty = d }, label = { Text(diamonds(d)) }) }
                }
                Text("Fragen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 20, 30).forEach { n -> FilterChip(count == n, { count = n }, label = { Text("$n") }) }
                }
                Text(if (kind == QKind.CALC) "Rechenaufgaben werden frisch erzeugt." else "$pool Fragen im Pool.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 6.dp))
                Button(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.TRAINING, lesson = lesson, kind = kind, difficulty = effective, count = count)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Runde starten") }
            }
        }
        item {
            SectionCard("Code-Drill") {
                Text("Nur Code: Ausgaben vorhersagen, Fehler finden, die richtige Zeile wählen – scikit-learn, Keras, PyTorch, Hugging Face und LangChain.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.TRAINING, lesson = lesson, kind = QKind.CODE, difficulty = effective, count = 10)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("10 Code-Fragen" + (lesson?.let { " aus Lektion $it" } ?: "")) }
            }
        }
        item {
            SectionCard("Fehlerschmiede") {
                Text(if (p.wrong.isEmpty()) "Keine offenen Fehler – stark." else "${p.wrong.size} falsch beantwortete Fragen warten auf eine zweite Chance.",
                    style = MaterialTheme.typography.bodyMedium)
                if (p.wrong.isNotEmpty()) OutlinedButton(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.WEAK, count = minOf(10, p.wrong.size))) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Fehler schmieden") }
            }
        }
    }
}

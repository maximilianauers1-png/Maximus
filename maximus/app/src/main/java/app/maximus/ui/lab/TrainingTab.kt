@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.Question
import app.maximus.lab.domain.QuizEngine
import app.maximus.lab.domain.Topic
import app.maximus.ui.strongman.SectionCard
import androidx.compose.foundation.background
import androidx.compose.material3.TextButton
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.ModuleContext
import app.maximus.lab.domain.QuizMode
import app.maximus.ui.chat.AskRequest
import kotlin.random.Random

@Composable
fun TrainingTab(ctx: LabContext) {
    var topic by rememberSaveable { mutableStateOf<Topic?>(null) }
    var difficulty by rememberSaveable { mutableIntStateOf(0) } // 0 = adaptive
    var count by rememberSaveable { mutableIntStateOf(10) }
    var course by rememberSaveable { mutableStateOf<String?>(null) }
    var mode by rememberSaveable { mutableStateOf(QuizMode.CONCEPT) }
    val mastery = topic?.let { ctx.progress.topic(it).mastery } ?: (Topic.entries.map { ctx.progress.topic(it).mastery }.average())
    val effective = if (difficulty == 0) QuizEngine.difficultyFor(mastery) else difficulty
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Trainingsrunde") {
                Text("Gebiet", style = MaterialTheme.typography.labelLarge)
                TopicChips(topic, { topic = it; course = null })
                CourseChips(topic, course) { course = it }
                Text("Art der Fragen", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuizMode.entries.forEach { m -> FilterChip(mode == m, { mode = m }, label = { Text(m.label) }) }
                }
                Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Schwierigkeit", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(difficulty == 0, { difficulty = 0 }, label = { Text("Adaptiv (${stars(QuizEngine.difficultyFor(mastery))})") })
                    (1..3).forEach { d -> FilterChip(difficulty == d, { difficulty = d }, label = { Text(stars(d)) }) }
                }
                Text("Aufgaben", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 20).forEach { n -> FilterChip(count == n, { count = n }, label = { Text("$n") }) }
                }
                Button(onClick = { ctx.startQuiz(QuizConfig(topic, effective, count, course = course, mode = mode)) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    Text("Runde starten")
                }
                Text(
                    "Verständnis- und Formelfragen als Multiple Choice; Rechenaufgaben nur in den Modi Gemischt und Rechnen, dann immer mit Formelhilfe und Konstanten. " +
                        "Nach jeder Antwort gibt es die Erklärung, bei Bedarf vertieft durch Maximus.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        item {
            SectionCard("Schnellstart") {
                Topic.entries.forEach { t ->
                    val s = ctx.progress.topic(t)
                    Row(
                        Modifier.fillMaxWidth().clickable { ctx.startQuiz(QuizConfig(t, QuizEngine.difficultyFor(s.mastery), 10)) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.titleMedium, color = topicColor(t))
                            Text(t.blurb, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(stars(QuizEngine.difficultyFor(s.mastery)), color = topicColor(t))
                    }
                }
            }
        }
    }
}

private fun stars(d: Int) = "◆".repeat(d) + "◇".repeat(3 - d)

@Composable
fun QuizRunner(ctx: LabContext, config: QuizConfig, onClose: () -> Unit) {
    var seed by remember { mutableLongStateOf(config.seed) }
    // A retry round replays exactly the questions answered wrongly.
    var retry by remember { mutableStateOf<List<Question>?>(null) }
    val questions = remember(config, seed, retry) {
        retry ?: if (config.daily) QuizEngine.daily(ctx.today)
        else QuizEngine.session(config.topic, config.difficulty, config.count, Random(seed), config.course, config.mode)
    }
    var index by remember(questions) { mutableIntStateOf(0) }
    var correct by remember(questions) { mutableIntStateOf(0) }
    var mistakes by remember(questions) { mutableStateOf(listOf<Question>()) }
    var choice by remember(questions, index) { mutableIntStateOf(-1) }
    var input by remember(questions, index) { mutableStateOf("") }
    var verdict by remember(questions, index) { mutableStateOf<Boolean?>(null) }
    var showHint by remember(questions, index) { mutableStateOf(false) }
    var showFormulas by remember(questions, index) { mutableStateOf(false) }
    var finished by remember(questions) { mutableStateOf(false) }
    val startXp = remember(questions) { ctx.progress.xp }

    fun submit(q: Question, ok: Boolean) {
        verdict = ok
        if (ok) correct++ else mistakes = mistakes + q
        ctx.act { p -> LabRules.answer(p, q, ok, ctx.today) }
    }

    fun next() {
        if (index + 1 < questions.size) index++
        else {
            finished = true
            val c = correct
            if (config.daily && retry == null) ctx.act { p -> LabRules.dailyFinished(p, c, ctx.today) }
            else ctx.act { p -> LabRules.sessionFinished(p, c, questions.size, ctx.today) }
        }
    }

    fun explain(q: Question, ok: Boolean) {
        val chosen = when {
            q.isChoice && choice >= 0 -> q.options[choice]
            !q.isChoice && input.isNotBlank() -> input + if (q.unit.isNotBlank()) " ${q.unit}" else ""
            else -> null
        }
        ctx.ask(AskRequest(
            "Quiz · ${q.topic.title}", Focus.SCIENCE,
            question = ModuleContext.quizQuestion(q, ok), autoSend = true,
            context = { ModuleContext.quiz(q, chosen, ok) }
        ))
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (finished) {
            item {
                SectionCard(if (correct == questions.size) "Fehlerfrei!" else "Runde beendet") {
                    Text("$correct von ${questions.size} richtig", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    ProgressBar(correct.toFloat() / questions.size, MaterialTheme.colorScheme.primary, Modifier.padding(vertical = 8.dp))
                    Text("Gewonnene XP: ${ctx.progress.xp - startXp}", style = MaterialTheme.typography.titleMedium)
                    Text("Stufe ${LabRules.level(ctx.progress.xp)} · ${LabRules.title(LabRules.level(ctx.progress.xp))}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (mistakes.isNotEmpty()) {
                        Button(onClick = { retry = mistakes.shuffled() }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            Text("Fehler wiederholen (${mistakes.size})")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        if (!config.daily) OutlinedButton(onClick = { retry = null; seed = System.nanoTime() }, modifier = Modifier.weight(1f)) { Text("Neue Runde") }
                        OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Schließen") }
                    }
                }
            }
            return@LazyColumn
        }
        val q = questions.getOrNull(index) ?: return@LazyColumn
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((if (retry != null) "Wiederholung " else "Frage ") + "${index + 1} von ${questions.size}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${q.topic.short}  ${stars(q.difficulty)}", color = topicColor(q.topic), style = MaterialTheme.typography.labelLarge)
                }
                ProgressBar((index + if (verdict != null) 1 else 0).toFloat() / questions.size, topicColor(q.topic), Modifier.padding(top = 6.dp), height = 6.dp)
            }
        }
        item {
            val kind = when {
                q.id.startsWith("fq:") -> "Formelfrage"
                !q.isChoice || q.hint.isNotEmpty() -> "Rechenaufgabe"
                else -> "Verständnisfrage"
            }
            SectionCard(kind) {
                Text(q.prompt, style = MaterialTheme.typography.bodyLarge)
                if (q.hint.isNotEmpty()) {
                    TextButton(onClick = { showHint = !showHint }, modifier = Modifier.padding(top = 4.dp)) {
                        Text(if (showHint) "Formelhilfe ausblenden" else "Formelhilfe anzeigen")
                    }
                    AnimatedVisibility(visible = showHint, enter = fadeIn() + expandVertically()) {
                        Text(q.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)).padding(10.dp))
                    }
                }
            }
        }
        if (q.isChoice) {
            q.options.forEachIndexed { i, opt ->
                item(key = "opt-${q.id}-$index-$i") {
                    val isCorrect = i == q.correctIndex
                    val target = when {
                        verdict == null -> MaterialTheme.colorScheme.outlineVariant
                        isCorrect -> MaterialTheme.colorScheme.primary
                        i == choice -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    val border by animateColorAsState(target, tween(300), label = "opt")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (verdict != null && isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(if (verdict != null && (isCorrect || i == choice)) 2.dp else 1.dp, border),
                        modifier = Modifier.fillMaxWidth().clickable(enabled = verdict == null) { choice = i; submit(q, q.checkChoice(i)) }
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("ABCD".getOrElse(i) { '•' }.toString(), style = MaterialTheme.typography.titleMedium, color = topicColor(q.topic),
                                modifier = Modifier.padding(end = 12.dp))
                            Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f),
                                color = if (verdict != null && isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            if (verdict != null && isCorrect) Text("✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            else if (verdict != null && i == choice) Text("✗", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        } else {
            item {
                Column {
                    OutlinedTextField(
                        input, { input = it }, singleLine = true, enabled = verdict == null,
                        label = { Text("Ergebnis") }, suffix = if (q.unit.isNotBlank()) ({ Text(q.unit) }) else null,
                        isError = input.isNotBlank() && Question.parseAnswer(input) == null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (verdict == null) {
                        Button(onClick = { submit(q, q.checkNumber(input)) }, enabled = Question.parseAnswer(input) != null, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                            Text("Prüfen")
                        }
                    }
                }
            }
        }
        item {
            AnimatedVisibility(visible = verdict != null, enter = fadeIn() + expandVertically()) {
                val ok = verdict == true
                SectionCard(if (ok) "Richtig!" else "Nicht ganz – so geht es") {
                    if (!ok && q.isChoice && choice >= 0) {
                        Text("Deine Antwort: ${q.options[choice]}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                        Text("Richtig ist: ${q.options[q.correctIndex]}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                    if (!q.isChoice) {
                        Text("Lösung: ${Fmt.num(q.answer, 5)} ${q.unit}", style = MaterialTheme.typography.titleMedium,
                            color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
                    }
                    Text("Erklärung", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp))
                    Text(q.solution, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                    q.chapterKey?.let { k -> Compendium.byKey[k] }?.let { ch ->
                        TextButton(onClick = { showFormulas = !showFormulas }, modifier = Modifier.padding(top = 4.dp)) {
                            Text((if (showFormulas) "▴ " else "▾ ") + "Formeln aus „${ch.title}“ (${ch.formulas.size})", color = topicColor(ch.topic))
                        }
                        AnimatedVisibility(visible = showFormulas, enter = fadeIn() + expandVertically()) {
                            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)).padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ch.formulas.forEach { f ->
                                    Text("${f.name}:  ${f.expr}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedButton(onClick = { explain(q, ok) }, modifier = Modifier.weight(1f)) { Text(if (ok) "Maximus vertieft" else "Maximus erklärt") }
                        Button(onClick = { next() }, modifier = Modifier.weight(1f)) {
                            Text(if (index + 1 < questions.size) "Weiter" else "Auswertung")
                        }
                    }
                }
            }
        }
    }
}

package app.maximus.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiQuestion
import app.maximus.ai.domain.AiQuizEngine
import app.maximus.ai.domain.AiRules
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.ModuleContext
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.lab.ProgressBar
import app.maximus.ui.strongman.SectionCard
import kotlin.random.Random

/** Builds the question list of a run. */
fun questionsFor(ctx: AiContext, c: AiQuizConfig, seed: Long): List<AiQuestion> = when (c.mode) {
    AiQuizMode.BITE -> AiQuizEngine.biteQuiz(c.bite ?: "", Random(seed))
    AiQuizMode.DAILY -> AiQuizEngine.daily(ctx.today)
    AiQuizMode.WEAK -> AiQuizEngine.weakSpots(ctx.progress.wrong, c.count, Random(seed))
    AiQuizMode.TRAINING -> AiQuizEngine.session(c.lesson, c.kind, c.difficulty, c.count, Random(seed))
}

/** Opens Maximus with the question, the user's answer and the stored explanation. */
fun explainWithMaximus(ctx: AiContext, q: AiQuestion, chosen: String?, ok: Boolean) {
    ctx.ask(AskRequest(
        "Quiz · ${AiCourse.byKey[q.bite]?.title ?: "AI"}", Focus.CODE,
        question = ModuleContext.aiQuizQuestion(q, ok), autoSend = true,
        context = { ModuleContext.aiQuiz(q, chosen, ok) }
    ))
}

@Composable
fun AiQuizRunner(ctx: AiContext, config: AiQuizConfig, onClose: () -> Unit) {
    var seed by remember { mutableLongStateOf(config.seed) }
    var retry by remember { mutableStateOf<List<AiQuestion>?>(null) }
    val questions = remember(config, seed, retry) { retry ?: questionsFor(ctx, config, seed) }
    var index by remember(questions) { mutableIntStateOf(0) }
    var correct by remember(questions) { mutableIntStateOf(0) }
    var mistakes by remember(questions) { mutableStateOf(listOf<AiQuestion>()) }
    var choice by remember(questions, index) { mutableIntStateOf(-1) }
    var finished by remember(questions) { mutableStateOf(false) }
    val startXp = remember(questions) { ctx.progress.xp }

    fun next() {
        if (index + 1 < questions.size) { index++; return }
        finished = true
        val c = correct
        when {
            retry != null -> {}
            config.mode == AiQuizMode.DAILY -> ctx.act { p -> AiRules.dailyFinished(p, c, ctx.today) }
            config.mode == AiQuizMode.BITE && config.bite != null -> ctx.act { p -> AiRules.biteQuizFinished(p, config.bite, c, questions.size, ctx.today) }
        }
    }

    if (questions.isEmpty()) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                SectionCard("Keine Fragen") {
                    Text("Für diese Auswahl gibt es keine Fragen.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Zurück") }
                }
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (finished) {
            item {
                val bite = config.bite?.let { AiCourse.byKey[it] }
                val st = AiRules.stars(correct, questions.size)
                SectionCard(if (correct == questions.size) "Makellos!" else "Runde beendet") {
                    if (config.mode == AiQuizMode.BITE && retry == null) Text(stars(st), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text("$correct von ${questions.size} richtig", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    ProgressBar(correct.toFloat() / questions.size, MaterialTheme.colorScheme.primary, Modifier.padding(vertical = 8.dp))
                    Text("Gewonnene XP: ${ctx.progress.xp - startXp}", style = MaterialTheme.typography.titleMedium)
                    val level = AiRules.level(ctx.progress.xp)
                    Text("Stufe $level · ${AiRules.rank(level)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (mistakes.isNotEmpty()) Button(onClick = { retry = mistakes.shuffled() }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        Text("Fehler wiederholen (${mistakes.size})")
                    }
                    if (bite != null) {
                        val next = AiCourse.bites.getOrNull(bite.number)
                        if (next != null) Button(onClick = {
                            ctx.act { p -> AiRules.biteRead(p, bite.key, ctx.today) }
                            onClose(); ctx.openBite(next.key)
                        }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text("Weiter zu Happen ${next.number}") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        if (config.mode != AiQuizMode.DAILY) OutlinedButton(onClick = { retry = null; seed = System.nanoTime() }, modifier = Modifier.weight(1f)) { Text("Neue Runde") }
                        OutlinedButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Schließen") }
                    }
                }
            }
            return@LazyColumn
        }
        val q = questions[index]
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((if (retry != null) "Wiederholung " else "Frage ") + "${index + 1} von ${questions.size}",
                        style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("${kindLabel(q)}  ${diamonds(q.difficulty)}", color = lessonColor(AiCourse.lessonOf(q.bite)), style = MaterialTheme.typography.labelLarge)
                }
                ProgressBar((index + if (choice >= 0) 1 else 0).toFloat() / questions.size, MaterialTheme.colorScheme.primary, Modifier.padding(top = 6.dp), height = 6.dp)
            }
        }
        questionItems(ctx, q, index, choice, onPick = { i ->
            choice = i
            val ok = q.check(i)
            if (ok) correct++ else mistakes = mistakes + q
            ctx.act { p -> AiRules.answer(p, q, ok, ctx.today) }
        }, nextLabel = if (index + 1 < questions.size) "Weiter" else "Auswertung", onNext = { next() })
    }
}

/**
 * The question (prompt, code), its four options and, once answered, the verdict with explanation.
 * Shared by quizzes and tournaments.
 */
fun LazyListScope.questionItems(
    ctx: AiContext, q: AiQuestion, index: Int, choice: Int, onPick: (Int) -> Unit, nextLabel: String, onNext: () -> Unit,
    showExplainButton: Boolean = true
) {
    // choice: −1 open, −2 the hourglass ran out (tournament), otherwise the picked option.
    val answered = choice != -1
    item(key = "prompt-${q.id}-$index") {
        val bite = AiCourse.byKey[q.bite]
        SectionCard(bite?.let { "Happen ${it.number} · ${it.title}" } ?: "Frage") {
            Text(q.prompt, style = MaterialTheme.typography.bodyLarge)
            if (q.code.isNotBlank()) CodeBlock(q.code, Modifier.padding(top = 8.dp))
        }
    }
    q.options.forEachIndexed { i, opt ->
        item(key = "opt-${q.id}-$index-$i") {
            val isCorrect = i == q.correctIndex
            val target = when {
                !answered -> MaterialTheme.colorScheme.outlineVariant
                isCorrect -> MaterialTheme.colorScheme.primary
                i == choice -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val border by animateColorAsState(target, tween(300), label = "opt")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (answered && isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(if (answered && (isCorrect || i == choice)) 2.dp else 1.dp, border),
                modifier = Modifier.fillMaxWidth().clickable(enabled = !answered) { onPick(i) }
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ABCD".getOrElse(i) { '•' }.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp))
                    val style = if (q.monoOptions) MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodyMedium
                    Text(opt, style = style, modifier = Modifier.weight(1f),
                        color = if (answered && isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    if (answered && isCorrect) Text("✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    else if (answered && i == choice) Text("✗", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
    item(key = "verdict-${q.id}-$index") {
        AnimatedVisibility(visible = answered, enter = fadeIn() + expandVertically()) {
            val ok = choice >= 0 && q.check(choice)
            SectionCard(if (ok) "Treffer!" else if (choice == -2) "Die Sanduhr ist abgelaufen" else "Daneben – so geht es") {
                if (!ok) {
                    Text("Richtig ist: ${q.options[q.correctIndex]}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text("Erklärung", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp))
                Text(q.explanation, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
                AiCourse.byKey[q.bite]?.let { b ->
                    TextButton(onClick = { ctx.openBite(b.key) }) { Text("→ Happen ${b.number}: ${b.title}", maxLines = 1) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    if (showExplainButton) OutlinedButton(onClick = { explainWithMaximus(ctx, q, q.options.getOrNull(choice), ok) }, modifier = Modifier.weight(1f)) {
                        Text(if (ok) "Maximus vertieft" else "Maximus erklärt")
                    }
                    Button(onClick = onNext, modifier = Modifier.weight(1f)) { Text(nextLabel) }
                }
            }
        }
    }
}

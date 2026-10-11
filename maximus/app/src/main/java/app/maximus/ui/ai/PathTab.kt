package app.maximus.ui.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiQuizEngine
import app.maximus.ai.domain.AiRules
import app.maximus.ai.domain.QKind
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.lab.FormulaBox
import app.maximus.ui.lab.ProgressBar
import app.maximus.ui.lab.RichBody
import app.maximus.ui.strongman.SectionCard

/** "Lehrpfad": the thirteen lessons; a tap unfolds the bites with read mark and quiz stars. */
@Composable
fun PathTab(ctx: AiContext) {
    val p = ctx.progress
    var open by rememberSaveable { mutableStateOf(AiCourse.next(p.readBites)?.lesson ?: 1) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            val read = p.readBites.count { it in AiCourse.byKey }
            PlateCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Der Weg zum AI Engineer", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("13 Lektionen nach dem IBM AI Engineering Professional Certificate, ${AiCourse.bites.size} Happen, " +
                        "${AiCourse.questions.size}+ Fragen. Jeder Happen endet mit einem Quiz, jede Lektion mit einem Turnier.",
                        style = MaterialTheme.typography.bodyMedium)
                    ProgressBar(read.toFloat() / AiCourse.bites.size, MaterialTheme.colorScheme.primary, Modifier.padding(top = 10.dp))
                    Text("$read von ${AiCourse.bites.size} Happen gelesen · ${p.starTotal} Sterne", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        items(AiCourse.lessons, key = { it.number }) { l ->
            val bites = AiCourse.bitesOf(l.number)
            val done = bites.count { it.key in p.readBites }
            val starsGot = bites.sumOf { p.stars[it.key] ?: 0 }
            val accent = lessonColor(l.number)
            PlateCard(onClick = { open = if (open == l.number) 0 else l.number }) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lektion ${l.number}", style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.weight(1f))
                        TrophyBadge(p.trophies[l.number] ?: 0)
                    }
                    Text(l.title, style = MaterialTheme.typography.titleMedium)
                    Text("IBM: ${l.ibmTitle} · ${l.hours} h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        ProgressBar(done.toFloat() / bites.size, accent, Modifier.weight(1f), height = 5.dp)
                        Text("  $done/${bites.size} · ★ $starsGot/${3 * bites.size}", style = MaterialTheme.typography.labelMedium, color = accent)
                    }
                    if (open == l.number) {
                        Text(l.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        bites.forEach { b ->
                            val read = b.key in p.readBites
                            Row(
                                Modifier.fillMaxWidth().clickable { ctx.openBite(b.key) }.padding(vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (read) "✔" else "○", color = if (read) accent else MaterialTheme.colorScheme.outline, modifier = Modifier.width(22.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(b.fullTitle, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    Text(levelLabel(b.level), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(stars(p.stars[b.key] ?: 0), style = MaterialTheme.typography.labelLarge, color = accent)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(onClick = {
                                ctx.startQuiz(AiQuizConfig(AiQuizMode.TRAINING, lesson = l.number, difficulty = difficultyFor(p.lesson(l.number).mastery)))
                            }, modifier = Modifier.weight(1f)) { Text("Üben") }
                            Button(onClick = { ctx.startTournament(l.number) }, modifier = Modifier.weight(1f)) { Text("Zum Turnier") }
                        }
                    }
                }
            }
        }
    }
}

fun levelLabel(level: Int) = when (level) { 1 -> "Grundlagen"; 2 -> "Vertiefung"; else -> "Fortgeschritten" }

/** One bite: goals, sections with code and formulas, then the quiz and the way on. */
@Composable
fun BiteScreen(ctx: AiContext, key: String) {
    val b = AiCourse.byKey[key] ?: run { EmptyState("Happen nicht gefunden."); return }
    val lesson = AiCourse.lesson(b.lesson)
    val accent = lessonColor(b.lesson)
    val read = b.key in ctx.progress.readBites
    val own = AiCourse.questionsByBite[b.key].orEmpty()
    val nextBite = AiCourse.bites.getOrNull(b.number)
    val quizSize = remember(b.key) { AiQuizEngine.biteQuiz(b.key, kotlin.random.Random(0)).size }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text("Lektion ${lesson.number} · ${lesson.title}", style = MaterialTheme.typography.labelLarge, color = accent)
                Text(b.title, style = MaterialTheme.typography.headlineSmall)
                Text("${levelLabel(b.level)} · ${own.size} Quizfragen, davon ${own.count { it.kind == QKind.CODE }} Code", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline)
                Text(b.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
        item {
            SectionCard(if (b.goals.isNotEmpty()) "Lernziele" else "In diesem Happen") {
                (b.goals.ifEmpty { b.sections.map { it.title } }).forEach { g ->
                    Row(Modifier.padding(vertical = 2.dp)) {
                        Text("⚔", color = accent, modifier = Modifier.width(22.dp))
                        Text(g, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        items(b.sections) { s ->
            SectionCard(s.title) {
                RichBody(s.body, accent)
                if (s.formulas.isNotEmpty()) Column(Modifier.padding(top = 8.dp)) {
                    s.formulas.forEach { f -> FormulaBox(f.expr, name = f.name, note = f.note, accent = accent) }
                }
            }
        }
        item {
            SectionCard("Prüfung des Happens") {
                val st = ctx.progress.stars[b.key] ?: 0
                Text(if (st > 0) "Bestes Ergebnis: ${stars(st)}" else "Noch nicht geprüft. ★ ab 60 %, ★★ ab 85 %, ★★★ fehlerfrei – 20 XP je neuem Stern.",
                    style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.BITE, bite = b.key)) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Happen-Quiz starten ($quizSize Fragen)")
                }
                if (read) Text("Happen gelesen ✓", style = MaterialTheme.typography.titleSmall, color = accent, modifier = Modifier.padding(top = 8.dp))
                else OutlinedButton(onClick = { ctx.act { p -> AiRules.biteRead(p, b.key, ctx.today) } }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text("Als gelesen markieren  +15 XP")
                }
                if (nextBite != null) OutlinedButton(onClick = {
                    if (!read) ctx.act { p -> AiRules.biteRead(p, b.key, ctx.today) }
                    ctx.openBite(nextBite.key)
                }, modifier = Modifier.fillMaxWidth()) { Text("Weiter: ${nextBite.fullTitle}", maxLines = 1, overflow = TextOverflow.Ellipsis) }
                OutlinedButton(onClick = { ctx.ask(aiRequest(b)) }, modifier = Modifier.fillMaxWidth()) { Text("Maximus zu diesem Happen fragen") }
            }
        }
    }
}

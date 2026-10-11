@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.ai

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiRules
import app.maximus.ui.components.PlateCard
import app.maximus.ui.lab.FlameGlyph
import app.maximus.ui.lab.ProgressBar
import app.maximus.ui.lab.StatTile
import app.maximus.ui.strongman.SectionCard

/** "Burgsaal": rank and XP, streak, the next bite, the daily trial, weak spots and the coats of arms. */
@Composable
fun HallTab(ctx: AiContext) {
    val p = ctx.progress
    val level = AiRules.level(p.xp)
    val next = AiCourse.next(p.readBites)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PlateCard {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RankSeal(level)
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(AiRules.rank(level), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Stufe $level · ${p.xp} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ProgressBar(AiRules.levelProgress(p.xp).toFloat(), MaterialTheme.colorScheme.primary, Modifier.padding(top = 8.dp))
                        Text(
                            "Noch ${AiRules.xpForLevel(level + 1) - p.xp} XP bis Stufe ${level + 1}: ${AiRules.rank(level + 1)}",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val streak = if (p.lastActiveDay >= ctx.today - 1) p.streak else 0
                Surface(
                    shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FlameGlyph(streak)
                        Column(Modifier.padding(start = 6.dp)) {
                            Text("$streak", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            Text("Tage Serie", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                StatTile("Trefferquote", if (p.answered > 0) "${100 * p.correct / p.answered} %" else "–", Modifier.weight(1f))
                StatTile("Wappen", "${p.badges.size}/${AiRules.BADGES.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Happen", "${p.readBites.size}/${AiCourse.bites.size}", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                StatTile("Sterne", "${p.starTotal}/${3 * AiCourse.bites.size}", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                StatTile("Pokale", "${p.trophies.size}/13", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
            }
        }
        item {
            SectionCard("Dein nächster Happen") {
                if (next == null) {
                    Text("Alle Happen verspeist – der Kurs liegt hinter dir. Hole dir jetzt die goldenen Pokale.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    val lesson = AiCourse.lesson(next.lesson)
                    Text("Lektion ${lesson.number} · ${lesson.title}", style = MaterialTheme.typography.labelLarge, color = lessonColor(lesson.number))
                    Text(next.fullTitle, style = MaterialTheme.typography.titleMedium)
                    Text(next.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ProgressBar(p.readBites.count { it in AiCourse.byKey }.toFloat() / AiCourse.bites.size, MaterialTheme.colorScheme.primary,
                        Modifier.padding(top = 8.dp), height = 6.dp)
                    Button(onClick = { ctx.openBite(next.key) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Happen lesen") }
                }
            }
        }
        item { DailyTrialCard(ctx) }
        if (p.wrong.isNotEmpty()) item {
            SectionCard("Fehlerschmiede") {
                Text("${p.wrong.size} Fragen hast du zuletzt falsch beantwortet. Schmiede sie neu – richtig beantwortet verschwinden sie aus der Liste.",
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.WEAK, count = minOf(10, p.wrong.size))) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Zur Esse (${minOf(10, p.wrong.size)} Fragen)") }
            }
        }
        item {
            SectionCard("Meisterschaft je Lektion") {
                AiCourse.lessons.forEach { l ->
                    val s = p.lesson(l.number)
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${l.number}. ${l.title}", style = MaterialTheme.typography.bodyMedium, color = lessonColor(l.number),
                                modifier = Modifier.weight(1f), maxLines = 1)
                            Text("${(100 * s.mastery).toInt()} %", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ProgressBar(s.mastery.toFloat(), lessonColor(l.number), Modifier.padding(top = 3.dp), height = 5.dp)
                    }
                }
            }
        }
        item {
            SectionCard("Wappen") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AiRules.BADGES.forEach { b ->
                        val on = b.key in p.badges
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (on) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.width(150.dp)
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Text(b.title, style = MaterialTheme.typography.labelLarge,
                                    color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline)
                                Text(b.description, style = MaterialTheme.typography.labelSmall,
                                    color = if (on) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Die Regeln der Akademie") {
                Text(
                    "XP gibt es für Treffer (Verständnis 10, Code und Rechnen 14, jeweils mal Schwierigkeit 1–3, mit bis zu +100 % Serienbonus), " +
                        "für jeden gelesenen Happen (15), für Sterne im Happen-Quiz (20 je neuem Stern: ★ ab 60 %, ★★ ab 85 %, ★★★ fehlerfrei), " +
                        "für Turniere (10 + 2 je Treffer, Pokal Bronze 100, Silber 175, Gold 250 XP – nur die Verbesserung zählt) und für die Tagesprobe " +
                        "(30 + 10 je Treffer). Stufe L erreicht man bei 50·L·(L − 1) XP; jede Stufe trägt ihren eigenen Rang.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DailyTrialCard(ctx: AiContext) {
    val done = ctx.progress.lastDailyDay == ctx.today
    SectionCard("Tagesprobe") {
        Text(
            if (done) "Heute bestanden. Morgen ruft der Herold mit fünf neuen Fragen."
            else "Fünf Fragen aus fünf Lektionen – heute für alle gleich. Lohn: 30 XP plus 10 je Treffer.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!done) Button(onClick = { ctx.startQuiz(AiQuizConfig(AiQuizMode.DAILY, count = 5)) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Probe antreten")
        }
    }
}

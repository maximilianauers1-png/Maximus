@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Calculators
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.QuizEngine
import app.maximus.lab.domain.Topic
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.theme.Palette

@Composable
fun OverviewTab(ctx: LabContext) {
    val p = ctx.progress
    val level = LabRules.level(p.xp)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PlateCard {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelSeal(level)
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(LabRules.title(level), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                        Text("Stufe $level · ${p.xp} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ProgressBar(LabRules.levelProgress(p.xp).toFloat(), MaterialTheme.colorScheme.primary, Modifier.padding(top = 8.dp))
                        Text(
                            "Noch ${LabRules.xpForLevel(level + 1) - p.xp} XP bis Stufe ${level + 1} (${LabRules.title(level + 1)})",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FlameGlyph(if (p.lastActiveDay >= ctx.today - 1) p.streak else 0)
                        Column(Modifier.padding(start = 6.dp)) {
                            Text("${if (p.lastActiveDay >= ctx.today - 1) p.streak else 0}", style = MaterialTheme.typography.headlineSmall, color = Palette.Heraldic)
                            Text("Tage Serie", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                StatTile("Trefferquote", if (p.answered > 0) "${100 * p.correct / p.answered} %" else "–", Modifier.weight(1f))
                StatTile("Abzeichen", "${p.badges.size}/${LabRules.BADGES.size}", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Kapitel gelesen", "${p.readChapters.size}/${Compendium.all.size}", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                StatTile("Rechner benutzt", "${p.usedCalculators.size}/${Calculators.all.size}", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
                StatTile("Karten fällig", "${LabRules.dueCards(p, ctx.today).size}", Modifier.weight(1f), color = MaterialTheme.colorScheme.secondary)
            }
        }
        item { DailyCard(ctx) }
        item { RecommendationCard(ctx) }
        item {
            SectionCard("Meisterschaft") {
                Topic.entries.forEach { t ->
                    val s = p.topic(t)
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { ctx.startQuiz(QuizConfig(t, QuizEngine.difficultyFor(s.mastery), 10)) }
                            .padding(vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t.title, style = MaterialTheme.typography.titleMedium, color = topicColor(t), modifier = Modifier.weight(1f))
                            Text("${(100 * s.mastery).toInt()} %  ·  ${s.correct}/${s.answered}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ProgressBar(s.mastery.toFloat(), topicColor(t), Modifier.padding(top = 4.dp), height = 6.dp)
                    }
                }
                Text("Antippen startet ein Training mit passender Schwierigkeit. Meisterschaft ist ein gleitender Mittelwert deiner Treffer; schwere Aufgaben zählen stärker.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            SectionCard("Abzeichen") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabRules.BADGES.forEach { b ->
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
            SectionCard("So funktioniert das Labor") {
                Text(
                    "XP gibt es für richtige Antworten (Konzeptfragen 10, Rechenaufgaben 20, jeweils mal Schwierigkeit 1–3), für jedes gelesene Kapitel (15), " +
                        "jeden neu benutzten Rechner (5), Karteikarten (1–3) und die Tageschallenge (30 + 10 pro richtige Antwort). Eine Serie aufeinanderfolgender " +
                        "Tage erhöht die XP für Antworten um bis zu 100 %. Stufe L erreicht man bei 50·L·(L − 1) XP.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Heraldic seal with the level number. */
@Composable
private fun LevelSeal(level: Int) {
    val steel = MaterialTheme.colorScheme.primary
    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(64.dp)) {
            val w = size.width; val h = size.height
            val shield = Path().apply {
                moveTo(w * 0.12f, h * 0.08f); lineTo(w * 0.88f, h * 0.08f); lineTo(w * 0.88f, h * 0.48f)
                cubicTo(w * 0.88f, h * 0.74f, w * 0.7f, h * 0.88f, w * 0.5f, h * 0.97f)
                cubicTo(w * 0.3f, h * 0.88f, w * 0.12f, h * 0.74f, w * 0.12f, h * 0.48f); close()
            }
            drawPath(shield, Brush.verticalGradient(listOf(Palette.Highest, Palette.Container)))
            drawPath(shield, steel, style = Stroke(width = 2.dp.toPx()))
            drawLine(steel.copy(alpha = 0.4f), Offset(w * 0.2f, h * 0.22f), Offset(w * 0.8f, h * 0.22f), strokeWidth = 1.dp.toPx())
        }
        Text("$level", style = MaterialTheme.typography.headlineMedium, color = steel)
    }
}

@Composable
private fun DailyCard(ctx: LabContext) {
    val done = ctx.progress.lastDailyDay == ctx.today
    SectionCard("Tageschallenge") {
        Text(
            if (done) "Heute erledigt. Morgen wartet eine neue Mischung aus fünf Gebieten."
            else "Fünf Aufgaben aus fünf Gebieten, für alle gleich an diesem Tag. Bonus: 30 XP plus 10 je richtige Antwort.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (!done) {
            Button(onClick = { ctx.startQuiz(QuizConfig(null, 2, 5, daily = true)) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Challenge starten") }
        }
    }
}

@Composable
private fun RecommendationCard(ctx: LabContext) {
    val p = ctx.progress
    val weakest = Topic.entries.minByOrNull { p.topic(it).mastery + 0.001 * it.ordinal } ?: Topic.MATH
    val nextChapter = Compendium.forTopic(weakest).sortedBy { it.level }.firstOrNull { it.key !in p.readChapters }
    SectionCard("Empfehlung") {
        Text("Dein schwächstes Gebiet: ${weakest.title}", style = MaterialTheme.typography.titleMedium, color = topicColor(weakest))
        Text(weakest.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            if (nextChapter != null) {
                OutlinedButton(onClick = { ctx.openChapter(nextChapter.key) }, modifier = Modifier.weight(1f)) { Text("Lesen: ${nextChapter.title}", maxLines = 1) }
            }
            Button(onClick = { ctx.startQuiz(QuizConfig(weakest, QuizEngine.difficultyFor(p.topic(weakest).mastery), 10)) }, modifier = Modifier.weight(1f)) { Text("Trainieren") }
        }
    }
}

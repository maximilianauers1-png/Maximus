package app.maximus.ui.lab

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.FlashCard
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.Topic
import app.maximus.ui.strongman.SectionCard

@Composable
fun FlashcardsTab(ctx: LabContext) {
    var topic by rememberSaveable { mutableStateOf<Topic?>(null) }
    var practice by rememberSaveable { mutableStateOf(false) }
    var practiceIndex by rememberSaveable { mutableIntStateOf(0) }
    val due = remember(ctx.progress.cards, topic) { LabRules.dueCards(ctx.progress, ctx.today, topic) }
    val pool = remember(topic) { Compendium.flashcards.filter { topic == null || it.chapter.topic == topic }.shuffled() }
    val card: FlashCard? = if (practice) pool.getOrNull(practiceIndex % pool.size.coerceAtLeast(1)) else due.firstOrNull()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TopicChips(topic, { topic = it; practiceIndex = 0 }) }
        item {
            SectionCard("Leitner-Kasten") {
                val boxes = IntArray(6)
                Compendium.flashcards.filter { topic == null || it.chapter.topic == topic }.forEach { c -> boxes[ctx.progress.cards[c.id]?.box ?: 0]++ }
                val total = boxes.sum().coerceAtLeast(1)
                (0..5).forEach { b ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(if (b == 0) "neu" else "Fach $b", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.25f))
                        ProgressBar(boxes[b].toFloat() / total, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f + 0.12f * b), Modifier.weight(0.6f), height = 6.dp)
                        Text("${boxes[b]}", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End, modifier = Modifier.weight(0.15f))
                    }
                }
                Text("Gewusste Karten wandern ein Fach weiter und kommen nach 1, 2, 4, 8 bzw. 16 Tagen wieder; vergessene zurück in Fach 1. Heute fällig: ${due.size}.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    if (practice) OutlinedButton(onClick = { practice = false }, modifier = Modifier.weight(1f)) { Text("Fällige Karten") }
                    else OutlinedButton(onClick = { practice = true }, modifier = Modifier.weight(1f)) { Text("Freies Üben") }
                }
            }
        }
        item {
            if (card == null) {
                SectionCard("Alles wiederholt") {
                    Text("Für heute ist nichts mehr fällig. Mit „Freies Üben“ kannst du ohne Planung weiterblättern.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                FlipCard(card, key = "${card.id}-${if (practice) practiceIndex else due.size}") { knew ->
                    if (practice) practiceIndex++
                    else ctx.act { p -> LabRules.reviewCard(p, card.id, knew, ctx.today) }
                }
            }
        }
    }
}

@Composable
private fun FlipCard(card: FlashCard, key: String, onAnswer: (Boolean) -> Unit) {
    var flipped by remember(key) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(450), label = "flip")
    val accent = topicColor(card.chapter.topic)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.6f)),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp)
                .graphicsLayer { rotationY = rotation; cameraDistance = 14f * density }
                .clickable { flipped = !flipped }
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = 200.dp).padding(20.dp), contentAlignment = Alignment.Center) {
                if (rotation <= 90f) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.chapter.topic.title + " · " + card.chapter.title, style = MaterialTheme.typography.labelMedium, color = accent, textAlign = TextAlign.Center)
                        Text(card.formula.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
                        Text("Antippen zum Umdrehen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 16.dp))
                    }
                } else {
                    // Counter-rotate the back so its text reads normally.
                    Column(Modifier.graphicsLayer { rotationY = 180f }, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(card.formula.name, style = MaterialTheme.typography.labelLarge, color = accent)
                        Text(card.formula.expr, style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif), textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 10.dp))
                        if (card.formula.note.isNotBlank()) {
                            Text(card.formula.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
        }
        if (flipped) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAnswer(false) }, modifier = Modifier.weight(1f)) { Text("Nochmal üben") }
                Button(onClick = { onAnswer(true) }, modifier = Modifier.weight(1f)) { Text("Gewusst") }
            }
        } else {
            TextButton(onClick = { flipped = true }, modifier = Modifier.fillMaxWidth()) { Text("Lösung zeigen") }
        }
    }
}

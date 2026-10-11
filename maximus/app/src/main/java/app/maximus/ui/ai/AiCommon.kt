package app.maximus.ui.ai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiEvent
import app.maximus.ai.domain.AiProgress
import app.maximus.ai.domain.AiQuestion
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.Joust
import app.maximus.ai.domain.QKind
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.lab.LabToast

/** What a quiz run contains. */
enum class AiQuizMode { BITE, TRAINING, DAILY, WEAK }

data class AiQuizConfig(
    val mode: AiQuizMode,
    val bite: String? = null,
    val lesson: Int? = null,
    val kind: QKind? = null,
    val difficulty: Int = 2,
    val count: Int = 10,
    val seed: Long = System.nanoTime()
)

/** Everything the academy tabs need from the hub: progress and navigation/update callbacks. */
class AiContext(
    val progress: AiProgress,
    val today: Long,
    val act: ((AiProgress) -> Pair<AiProgress, List<AiEvent>>) -> Unit,
    val openBite: (String) -> Unit,
    val startQuiz: (AiQuizConfig) -> Unit,
    val startTournament: (Int) -> Unit,
    val ask: (AskRequest) -> Unit
)

/** Adaptive difficulty from a lesson's mastery: 0…1 → 1…3. */
fun difficultyFor(mastery: Double): Int = when { mastery < 0.4 -> 1; mastery < 0.75 -> 2; else -> 3 }

/** Accent of a lesson: the three module tones in turn, so the path reads like a banner without a rainbow. */
@Composable
fun lessonColor(n: Int): Color = when (n % 3) {
    1 -> MaterialTheme.colorScheme.primary
    2 -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.tertiary
}

fun stars(n: Int, max: Int = 3) = "★".repeat(n.coerceIn(0, max)) + "☆".repeat((max - n).coerceIn(0, max))

fun diamonds(d: Int) = "◆".repeat(d) + "◇".repeat(3 - d)

fun kindIcon(k: QKind) = when (k) { QKind.CONCEPT -> "❖"; QKind.CODE -> "⌨"; QKind.CALC -> "∑"; QKind.TERM -> "✒" }

/** Code in a dark, scrollable plate; never wraps, so indentation stays readable. */
@Composable
fun CodeBlock(code: String, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(code, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), softWrap = false)
    }
}

/** Heraldic seal with the level number: a shield with a gear-toothed rim, the academy's coat of arms. */
@Composable
fun RankSeal(level: Int, size: Dp = 64.dp) {
    val gold = MaterialTheme.colorScheme.primary
    val second = MaterialTheme.colorScheme.secondary
    val high = MaterialTheme.colorScheme.surfaceContainerHighest
    val low = MaterialTheme.colorScheme.surfaceContainer
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width; val h = this.size.height
            val shield = Path().apply {
                moveTo(w * 0.14f, h * 0.1f); lineTo(w * 0.86f, h * 0.1f); lineTo(w * 0.86f, h * 0.5f)
                cubicTo(w * 0.86f, h * 0.75f, w * 0.68f, h * 0.88f, w * 0.5f, h * 0.97f)
                cubicTo(w * 0.32f, h * 0.88f, w * 0.14f, h * 0.75f, w * 0.14f, h * 0.5f); close()
            }
            drawPath(shield, Brush.verticalGradient(listOf(high, low)))
            drawPath(shield, gold, style = Stroke(width = 2.dp.toPx()))
            // Little "neurons" along the top edge.
            for (i in 0..4) drawCircle(second.copy(alpha = 0.7f), radius = 1.6.dp.toPx(), center = Offset(w * (0.24f + 0.13f * i), h * 0.18f))
        }
        Text("$level", style = MaterialTheme.typography.headlineMedium, color = gold, textAlign = TextAlign.Center)
    }
}

/** Trophy glyph with the tier colour (gold, silver, bronze, empty). */
@Composable
fun TrophyBadge(tier: Int, modifier: Modifier = Modifier) {
    val c = when (tier) { 3 -> Color(0xFFE8BE4F); 2 -> Color(0xFFC9D1DB); 1 -> Color(0xFFC08457); else -> MaterialTheme.colorScheme.outlineVariant }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("🏆", color = c, style = MaterialTheme.typography.titleMedium)
        Text(if (tier > 0) Joust.tierName(tier).substringBefore(' ') else "–", style = MaterialTheme.typography.labelMedium, color = c,
            modifier = Modifier.padding(start = 4.dp))
    }
}

fun aiToasts(events: List<AiEvent>, nextId: () -> Long): List<LabToast> {
    val out = ArrayList<LabToast>()
    val xp = events.filterIsInstance<AiEvent.Xp>()
    if (xp.isNotEmpty()) out += LabToast(nextId(), "+${xp.sumOf { it.amount }} XP · ${xp.last().reason}", false)
    events.filterIsInstance<AiEvent.StreakExtended>().forEach { out += LabToast(nextId(), "Serie: ${it.days} Tage in Folge", false) }
    events.filterIsInstance<AiEvent.Stars>().forEach { out += LabToast(nextId(), "${stars(it.stars)}  ${AiCourse.byKey[it.bite]?.title ?: ""}", true) }
    events.filterIsInstance<AiEvent.Trophy>().forEach { out += LabToast(nextId(), "${Joust.tierName(it.tier)} · Lektion ${it.lesson}", true) }
    events.filterIsInstance<AiEvent.BadgeUnlocked>().forEach { out += LabToast(nextId(), "Wappen: ${it.badge.title}", true) }
    events.filterIsInstance<AiEvent.RankUp>().forEach { out += LabToast(nextId(), "Stufe ${it.level}: ${it.rank}", true) }
    return out
}

/** Label of the kind of a question, with its little glyph. */
fun kindLabel(q: AiQuestion) = "${kindIcon(q.kind)} ${q.kind.label}"

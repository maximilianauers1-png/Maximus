@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.LabEvent
import app.maximus.lab.domain.Topic
import app.maximus.ui.theme.Palette
import kotlinx.coroutines.delay

/** One accent per field, chosen to stay readable (≥ 7:1) on the iron ground. */
fun topicColor(t: Topic): Color = when (t) {
    Topic.THERMO -> Color(0xFFD9A066)        // glowing brass
    Topic.ELECTRO -> Palette.Blued            // blued steel
    Topic.QUANTUM -> Color(0xFFB3A2E0)        // violet
    Topic.SEMICONDUCTOR -> Color(0xFF8CC7B3)  // verdigris
    Topic.CALORIC -> Palette.Heraldic         // heraldic red
    Topic.QFT -> Color(0xFFE3C77A)            // gold
    Topic.MATH -> Palette.Steel               // polished steel
}

/** Formula in a framed plate, set in a serif face for the mathematical look. */
@Composable
fun FormulaBox(expr: String, modifier: Modifier = Modifier, name: String? = null, note: String? = null, accent: Color = MaterialTheme.colorScheme.primary) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (name != null) Text(name, style = MaterialTheme.typography.labelMedium, color = accent)
            Text(expr, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif), color = MaterialTheme.colorScheme.onSurface)
            if (!note.isNullOrBlank()) Text(note, style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Renders the compendium markup: blank-line separated paragraphs, "• " bullets, "> " key statements.
 */
@Composable
fun RichBody(text: String, accent: Color = MaterialTheme.colorScheme.primary) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        text.split(Regex("\\n\\s*\\n")).forEach { block ->
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) return@forEach
            if (lines.all { it.startsWith("• ") }) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    lines.forEach { l ->
                        Row {
                            Text("•", color = accent, modifier = Modifier.width(14.dp), style = MaterialTheme.typography.bodyMedium)
                            Text(l.removePrefix("• "), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (lines.first().startsWith("> ")) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.width(3.dp).height(20.dp).background(accent))
                    Text(
                        lines.joinToString(" ") { it.removePrefix("> ") },
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = accent, modifier = Modifier.padding(start = 10.dp).weight(1f)
                    )
                }
            } else {
                // Mixed blocks: paragraph lines followed by bullets.
                val para = lines.takeWhile { !it.startsWith("• ") }
                val bullets = lines.drop(para.size)
                if (para.isNotEmpty()) Text(para.joinToString(" "), style = MaterialTheme.typography.bodyMedium)
                bullets.forEach { l ->
                    Row {
                        Text("•", color = accent, modifier = Modifier.width(14.dp), style = MaterialTheme.typography.bodyMedium)
                        Text(l.removePrefix("• "), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Topic filter chips; [selected] null means "all". */
@Composable
fun TopicChips(selected: Topic?, onSelect: (Topic?) -> Unit, allowAll: Boolean = true) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (allowAll) FilterChip(selected == null, { onSelect(null) }, label = { Text("Alle") })
        Topic.entries.forEach { t ->
            FilterChip(selected == t, { onSelect(t) }, label = { Text(t.short, color = if (selected == t) MaterialTheme.colorScheme.onSurface else topicColor(t)) })
        }
    }
}

/** Animated horizontal bar with a metallic gradient in the given colour. */
@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val anim by animateFloatAsState(fraction.coerceIn(0f, 1f), tween(700), label = "bar")
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier.fillMaxWidth().height(height)) {
        val r = CornerRadius(size.height / 2, size.height / 2)
        drawRoundRect(track, cornerRadius = r)
        if (anim > 0f) {
            drawRoundRect(
                Brush.horizontalGradient(listOf(color.copy(alpha = 0.65f), color)),
                size = Size(size.width * anim, size.height), cornerRadius = r
            )
        }
    }
}

/** Little flame glyph for the day streak; brighter with longer streaks. */
@Composable
fun FlameGlyph(streak: Int, modifier: Modifier = Modifier.size(28.dp)) {
    val hot = Color(0xFFE8A04A)
    val alpha = (0.35f + 0.065f * streak.coerceAtMost(10))
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val outer = Path().apply {
            moveTo(w * 0.5f, h * 0.04f)
            cubicTo(w * 0.95f, h * 0.38f, w * 0.92f, h * 0.95f, w * 0.5f, h * 0.97f)
            cubicTo(w * 0.08f, h * 0.95f, w * 0.05f, h * 0.55f, w * 0.32f, h * 0.36f)
            cubicTo(w * 0.32f, h * 0.55f, w * 0.42f, h * 0.6f, w * 0.48f, h * 0.55f)
            cubicTo(w * 0.4f, h * 0.38f, w * 0.45f, h * 0.18f, w * 0.5f, h * 0.04f)
            close()
        }
        drawPath(outer, Brush.verticalGradient(listOf(hot.copy(alpha = alpha), Palette.Heraldic.copy(alpha = alpha))))
        drawCircle(Color(0xFFFFE2A8).copy(alpha = alpha), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.74f))
    }
}

data class LabToast(val id: Long, val text: String, val big: Boolean)

fun toastsOf(events: List<LabEvent>, nextId: () -> Long): List<LabToast> {
    val xp = events.filterIsInstance<LabEvent.Xp>()
    val out = ArrayList<LabToast>()
    if (xp.isNotEmpty()) out += LabToast(nextId(), "+${xp.sumOf { it.amount }} XP · ${xp.last().reason}", false)
    events.filterIsInstance<LabEvent.StreakExtended>().forEach { out += LabToast(nextId(), "Serie: ${it.days} Tage in Folge", false) }
    events.filterIsInstance<LabEvent.BadgeUnlocked>().forEach { out += LabToast(nextId(), "Abzeichen: ${it.badge.title}", true) }
    events.filterIsInstance<LabEvent.LevelUp>().forEach { out += LabToast(nextId(), "Stufe ${it.level}: ${it.title}", true) }
    return out
}

/** Stack of short-lived toasts at the top edge; each disappears after a few seconds. */
@Composable
fun BoxScope.LabToastHost(toasts: List<LabToast>, onExpire: (Long) -> Unit) {
    Column(
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        toasts.takeLast(3).forEach { t ->
            LaunchedEffect(t.id) { delay(if (t.big) 3800 else 2200); onExpire(t.id) }
            val state = remember(t.id) { MutableTransitionState(false).apply { targetState = true } }
            AnimatedVisibility(visibleState = state, enter = fadeIn() + slideInVertically { -it }, exit = fadeOut() + slideOutVertically { -it }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (t.big) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                    border = BorderStroke(1.dp, if (t.big) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        t.text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = if (t.big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
                        color = if (t.big) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** Small label + value pair used in stat grids. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.strongman.domain.Population
import app.maximus.strongman.domain.Rank
import app.maximus.strongman.domain.ScoreMode

/** Tier colour: steel for the first tiers, then parchment, heraldic red, and gold for the top. */
@Composable
fun rankColor(rank: Rank): Color {
    val cs = MaterialTheme.colorScheme
    return when {
        rank.ordinal <= 1 -> cs.outline
        rank.ordinal <= 3 -> cs.tertiary
        rank.ordinal <= 5 -> cs.primary
        else -> cs.secondary
    }
}

/** Heraldic shield with the tier colour, chevrons for the tier inside its colour band and the division numeral. */
@Composable
fun RankEmblem(rank: Rank, division: Int, size: Dp = 72.dp) {
    val color = rankColor(rank)
    val ground = MaterialTheme.colorScheme.surfaceContainerHighest
    val chevrons = (rank.ordinal % 3) + 1 + if (rank.ordinal >= 9) 1 else 0
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = this.size.width; val h = this.size.height
            val shield = Path().apply {
                moveTo(w * 0.08f, h * 0.06f); lineTo(w * 0.92f, h * 0.06f); lineTo(w * 0.92f, h * 0.48f)
                cubicTo(w * 0.92f, h * 0.76f, w * 0.68f, h * 0.88f, w * 0.5f, h * 0.97f)
                cubicTo(w * 0.32f, h * 0.88f, w * 0.08f, h * 0.76f, w * 0.08f, h * 0.48f); close()
            }
            drawPath(shield, Brush.verticalGradient(listOf(color.copy(alpha = 0.55f), ground)))
            drawPath(shield, color, style = Stroke(width = 2.5.dp.toPx()))
            val sw = 2.2.dp.toPx()
            repeat(chevrons) { i ->
                val y = h * (0.66f + 0.075f * i)
                drawLine(color, Offset(w * 0.3f, y - h * 0.06f), Offset(w * 0.5f, y), strokeWidth = sw)
                drawLine(color, Offset(w * 0.7f, y - h * 0.06f), Offset(w * 0.5f, y), strokeWidth = sw)
            }
        }
        Text(app.maximus.strongman.domain.Ranks.roman(division), color = color, fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.26f).sp, modifier = Modifier.padding(bottom = size * 0.22f))
    }
}

/** Thin progress bar with rounded ends, in the given colour (no Material progress indicator glitches at 0/1). */
@Composable
fun MeterBar(fraction: Double, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(50)).background(track)) {
        val f = fraction.toFloat().coerceIn(0f, 1f)
        if (f > 0f) Box(Modifier.fillMaxWidth(f).height(height).clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.7f), color))))
    }
}

@Composable
fun PopulationModeSelector(population: Population, mode: ScoreMode, onPopulation: (Population) -> Unit, onMode: (ScoreMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Population.entries.forEach { p -> FilterChip(population == p, { onPopulation(p) }, label = { Text(p.title) }) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ScoreMode.entries.forEach { m -> FilterChip(mode == m, { onMode(m) }, label = { Text(m.title) }) }
        }
    }
}

/** Big number with a small caption below, for hero rows. */
@Composable
fun StatPill(value: String, caption: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"), color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
fun Hint(text: String, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = color)
}

@Composable
fun StatRow(content: @Composable (Modifier) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { content(Modifier.weight(1f)) }
}

fun pct(p: Double): String = if (p >= 99.0) fmt(p, 2) + " %" else fmt(p, 1) + " %"

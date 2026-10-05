@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceProbability
import app.maximus.dnd.domain.DiceRoller
import app.maximus.dnd.domain.RollResult
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Outline of a d20 seen from a vertex: hexagonal silhouette with the central triangle face. */
@Composable
fun D20Glyph(modifier: Modifier = Modifier, color: androidx.compose.ui.graphics.Color, strokeDp: Float = 1.6f) {
    Canvas(modifier) {
        val r = size.minDimension / 2f * 0.92f
        val c = Offset(size.width / 2f, size.height / 2f)
        fun vertex(k: Int, rr: Float): Offset {
            val a = Math.toRadians(-90.0 + 60.0 * k)
            return Offset(c.x + rr * cos(a).toFloat(), c.y + rr * sin(a).toFloat())
        }
        val hex = Path().apply { for (k in 0 until 6) { val v = vertex(k, r); if (k == 0) moveTo(v.x, v.y) else lineTo(v.x, v.y) }; close() }
        // Inner triangle points down, its corners on the hexagon's inner radius.
        val t = (0 until 3).map { k -> val a = Math.toRadians(-30.0 + 120.0 * k); Offset(c.x + r * 0.55f * cos(a).toFloat(), c.y + r * 0.55f * sin(a).toFloat()) }
        val tri = Path().apply { moveTo(t[0].x, t[0].y); lineTo(t[1].x, t[1].y); lineTo(t[2].x, t[2].y); close() }
        val stroke = Stroke(width = strokeDp.dp.toPx(), join = StrokeJoin.Round)
        drawPath(hex, color, style = stroke)
        drawPath(tri, color, style = stroke)
        // Spokes from the triangle to the hexagon corners.
        listOf(0 to 1, 0 to 0, 1 to 2, 1 to 3, 2 to 4, 2 to 5).forEach { (ti, hk) -> drawLine(color, t[ti], vertex(hk, r), strokeWidth = stroke.width * 0.8f) }
        drawLine(color, t[0], vertex(1, r), strokeWidth = stroke.width * 0.8f)
    }
}

private val DICE = listOf(4, 6, 8, 10, 12, 20, 100)

private data class QuickRoll(val id: Int, val expression: String, val result: RollResult, val sides: Int)

/**
 * Floating d20 bubble for the D&D hub. Drag it anywhere; tap to open or close the quick-roll window.
 * The window rolls standard dice with count, modifier and advantage, animates the result and logs
 * every roll to the shared dice history.
 */
@Composable
fun BoxScope.QuickRollBubble(services: AppServices) {
    var open by rememberSaveable { mutableStateOf(false) }
    var dragX by rememberSaveable { mutableStateOf(0f) }
    var dragY by rememberSaveable { mutableStateOf(0f) }
    val pressScale by animateFloatAsState(if (open) 0.9f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "bubble")
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(350, easing = FastOutSlowInEasing), label = "turn")

    AnimatedVisibility(
        visible = open,
        enter = fadeIn(tween(160)) + scaleIn(tween(220, easing = FastOutSlowInEasing), initialScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)),
        exit = fadeOut(tween(140)) + scaleOut(tween(160), targetScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)),
        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 92.dp)
    ) {
        QuickRollWindow(services, onClose = { open = false })
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 20.dp, bottom = 20.dp)
            .offset { IntOffset(dragX.roundToInt(), dragY.roundToInt()) }
            .size(60.dp)
            .scale(pressScale)
            .shadow(10.dp, CircleShape)
            // Landsknecht colours: crimson enamel with a saffron rim.
            .background(Brush.radialGradient(listOf(Color(0xFFE0565C), Color(0xFFB3202A), Color(0xFF4A0B10))), CircleShape)
            .border(2.dp, Color(0xFFF2C14E), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, amount ->
                    change.consume()
                    dragX = (dragX + amount.x).coerceIn(-size.width * 5f, 0f)
                    dragY = (dragY + amount.y).coerceIn(-size.height * 11f, 0f)
                }
            }
            .clickable { open = !open }
            .semantics { contentDescription = if (open) "Close quick dice" else "Open quick dice" },
        contentAlignment = Alignment.Center
    ) {
        D20Glyph(Modifier.size(36.dp).rotate(turn), color = Color(0xFFF2C14E), strokeDp = 2f)
        Text("20", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFEDE3CF))
    }
}

@Composable
private fun QuickRollWindow(services: AppServices, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val random = remember { Random(System.nanoTime()) }
    var sides by rememberSaveable { mutableIntStateOf(20) }
    var count by rememberSaveable { mutableIntStateOf(1) }
    var modifier by rememberSaveable { mutableIntStateOf(0) }
    var advantage by rememberSaveable { mutableIntStateOf(0) }
    var last by remember { mutableStateOf<QuickRoll?>(null) }
    var recent by remember { mutableStateOf<List<QuickRoll>>(emptyList()) }
    var display by remember { mutableStateOf<String?>(null) }
    var nextId by remember { mutableIntStateOf(0) }
    val spin = remember { Animatable(0f) }

    val dice = if (sides == 20 && count == 1 && advantage != 0) (if (advantage > 0) "2d20kh1" else "2d20kl1") else "${count}d$sides"
    val expression = dice + when { modifier > 0 -> "+$modifier"; modifier < 0 -> "$modifier"; else -> "" }
    val mean = remember(expression) { runCatching { DiceProbability.of(DiceParser.parse(expression)).mean }.getOrNull() }

    fun roll() {
        val node = runCatching { DiceParser.parse(expression) }.getOrNull() ?: return
        val r = DiceRoller.roll(node, random)
        val q = QuickRoll(nextId++, expression, r, sides)
        last = q
        recent = (listOf(q) + recent).take(6)
        scope.launch { services.dnd.addRoll(expression, "Quick roll", r.total, r.pools.joinToString("; ") { p -> p.dice.joinToString(" ") { d -> if (d.kept) "${d.value}" else "(${d.value})" } }) }
    }

    // Tumbling animation: random faces flicker while the die spins, then the real total lands.
    LaunchedEffect(last?.id) {
        val q = last ?: return@LaunchedEffect
        launch { spin.snapTo(0f); spin.animateTo(720f, tween(520, easing = FastOutSlowInEasing)) }
        repeat(9) {
            display = (count * 1 + random.nextInt((count * q.sides - count + 1).coerceAtLeast(1)) + modifier).toString()
            delay(50)
        }
        display = q.result.total.toString()
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 12.dp,
        modifier = Modifier.widthIn(max = 340.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quick roll", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                TextButton(onClick = onClose) { Text("Close") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DICE.forEach { s -> DieButton(s, selected = sides == s) { sides = s; if (s != 20) advantage = 0 } }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dice", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Stepper(count, { count = it; if (it > 1) advantage = 0 }, min = 1, max = 20)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Modifier", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                Stepper(modifier, { modifier = it }, min = -20, max = 30)
            }
            if (sides == 20 && count == 1) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(advantage == 0, { advantage = 0 }, label = { Text("Normal") })
                    FilterChip(advantage > 0, { advantage = 1 }, label = { Text("Advantage") })
                    FilterChip(advantage < 0, { advantage = -1 }, label = { Text("Disadv.") })
                }
            }
            // Result face.
            Box(
                modifier = Modifier.fillMaxWidth().height(96.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .clickable { roll() },
                contentAlignment = Alignment.Center
            ) {
                D20Glyph(Modifier.size(84.dp).rotate(spin.value), color = MaterialTheme.colorScheme.outlineVariant)
                val q = last
                val natural = q?.result?.pools?.firstOrNull()?.dice?.firstOrNull { it.kept }?.value
                val crit = q != null && q.sides == 20 && natural == 20 && display == q.result.total.toString()
                val fumble = q != null && q.sides == 20 && natural == 1 && display == q.result.total.toString()
                Text(
                    display ?: "Tap",
                    style = MaterialTheme.typography.displaySmall,
                    color = when { crit -> MaterialTheme.colorScheme.primary; fumble -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.onSurface }
                )
            }
            last?.let { q ->
                val detail = q.result.pools.joinToString("  ") { p -> p.dice.joinToString(" ") { d -> if (d.kept) "${d.value}" else "(${d.value})" } }
                val natural = q.result.pools.firstOrNull()?.dice?.firstOrNull { it.kept }?.value
                Text(
                    "${q.expression}: $detail" + when {
                        q.sides == 20 && natural == 20 -> "   natural 20!"
                        q.sides == 20 && natural == 1 -> "   natural 1"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = { roll() }, modifier = Modifier.fillMaxWidth()) {
                Text("Roll $expression" + (mean?.let { "   (mean ${fmt1(it)})" } ?: ""))
            }
            if (recent.size > 1) {
                Text(
                    "Recent: " + recent.drop(1).joinToString("  ·  ") { "${it.expression} = ${it.result.total}" },
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun DieButton(sides: Int, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier.size(width = 42.dp, height = 36.dp)
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("d$sides", style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

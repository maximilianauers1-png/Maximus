package app.maximus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.ui.theme.Palette

/** Hairline of polished steel that fades out at both ends, with a small lozenge at the centre. */
@Composable
fun SteelRule(modifier: Modifier = Modifier, lozenge: Boolean = true) {
    Canvas(modifier = modifier.fillMaxWidth().height(9.dp)) {
        val y = size.height / 2
        val brush = Brush.horizontalGradient(
            0f to Color.Transparent, 0.35f to Palette.SteelDeep, 0.5f to Palette.Steel, 0.65f to Palette.SteelDeep, 1f to Color.Transparent
        )
        drawLine(brush, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        if (lozenge) {
            val r = 3.5.dp.toPx(); val cx = size.width / 2
            val p = Path().apply { moveTo(cx, y - r); lineTo(cx + r, y); lineTo(cx, y + r); lineTo(cx - r, y); close() }
            drawPath(p, Palette.Ground)
            drawPath(p, Palette.Steel, style = Stroke(width = 1.2.dp.toPx()))
        }
    }
}

enum class Glyph { BACK, GEAR, PULSE, LOCK, PLUS, CHEVRON_LEFT, CHEVRON_RIGHT, STAR, STAR_FILLED, EYE, COPY, SHIELD }

/** Small line icons drawn on a 24-unit grid (no icon library needed). */
@Composable
fun GlyphIcon(glyph: Glyph, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurface, size: Dp = 22.dp) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.minDimension / 24f
        val stroke = Stroke(width = 1.7f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun p(vararg pts: Float, closed: Boolean = false) = Path().apply {
            moveTo(pts[0] * u, pts[1] * u)
            var i = 2
            while (i < pts.size) { lineTo(pts[i] * u, pts[i + 1] * u); i += 2 }
            if (closed) close()
        }
        when (glyph) {
            Glyph.BACK, Glyph.CHEVRON_LEFT -> drawPath(p(15f, 5f, 8f, 12f, 15f, 19f), tint, style = stroke)
            Glyph.CHEVRON_RIGHT -> drawPath(p(9f, 5f, 16f, 12f, 9f, 19f), tint, style = stroke)
            Glyph.PLUS -> { drawPath(p(12f, 5f, 12f, 19f), tint, style = stroke); drawPath(p(5f, 12f, 19f, 12f), tint, style = stroke) }
            Glyph.PULSE -> drawPath(p(3f, 13f, 7.5f, 13f, 10f, 6f, 14f, 18f, 16.5f, 11f, 21f, 11f), tint, style = stroke)
            Glyph.GEAR -> {
                drawCircle(tint, radius = 3.2f * u, center = Offset(12f * u, 12f * u), style = stroke)
                for (k in 0 until 8) {
                    val a = Math.toRadians(k * 45.0)
                    val c = Math.cos(a).toFloat(); val s = Math.sin(a).toFloat()
                    drawLine(tint, Offset((12f + 6.2f * c) * u, (12f + 6.2f * s) * u), Offset((12f + 8.6f * c) * u, (12f + 8.6f * s) * u), strokeWidth = 2.2f * u, cap = StrokeCap.Round)
                }
                drawCircle(tint, radius = 6.2f * u, center = Offset(12f * u, 12f * u), style = stroke)
            }
            Glyph.LOCK -> {
                drawPath(p(6f, 11f, 18f, 11f, 18f, 20f, 6f, 20f, closed = true), tint, style = stroke)
                drawPath(Path().apply {
                    moveTo(8.5f * u, 11f * u); lineTo(8.5f * u, 8f * u)
                    cubicTo(8.5f * u, 3.5f * u, 15.5f * u, 3.5f * u, 15.5f * u, 8f * u); lineTo(15.5f * u, 11f * u)
                }, tint, style = stroke)
            }
            Glyph.STAR, Glyph.STAR_FILLED -> {
                val pts = FloatArray(20)
                for (k in 0 until 10) {
                    val r = if (k % 2 == 0) 8.5f else 3.6f
                    val a = Math.toRadians(-90.0 + k * 36.0)
                    pts[2 * k] = 12f + r * Math.cos(a).toFloat(); pts[2 * k + 1] = 12.5f + r * Math.sin(a).toFloat()
                }
                val path = p(*pts, closed = true)
                if (glyph == Glyph.STAR_FILLED) drawPath(path, tint) else drawPath(path, tint, style = stroke)
            }
            Glyph.EYE -> {
                drawPath(Path().apply {
                    moveTo(2.5f * u, 12f * u); cubicTo(7f * u, 5f * u, 17f * u, 5f * u, 21.5f * u, 12f * u)
                    cubicTo(17f * u, 19f * u, 7f * u, 19f * u, 2.5f * u, 12f * u); close()
                }, tint, style = stroke)
                drawCircle(tint, radius = 2.8f * u, center = Offset(12f * u, 12f * u), style = stroke)
            }
            Glyph.SHIELD -> {
                // Heater shield: flat chief, sides curving to a point; a pale (vertical band) as charge.
                val shield = Path().apply {
                    moveTo(4f * u, 3f * u); lineTo(20f * u, 3f * u); lineTo(20f * u, 11f * u)
                    cubicTo(20f * u, 16.5f * u, 16.5f * u, 20f * u, 12f * u, 22f * u)
                    cubicTo(7.5f * u, 20f * u, 4f * u, 16.5f * u, 4f * u, 11f * u); close()
                }
                drawPath(shield, tint, style = stroke)
                drawLine(tint, Offset(12f * u, 3.5f * u), Offset(12f * u, 21.3f * u), strokeWidth = 1.7f * u)
                drawLine(tint, Offset(4.5f * u, 9f * u), Offset(19.5f * u, 9f * u), strokeWidth = 1.7f * u)
            }
            Glyph.COPY -> {
                drawPath(p(8f, 8f, 19f, 8f, 19f, 20f, 8f, 20f, closed = true), tint, style = stroke)
                drawPath(p(5f, 16f, 5f, 4f, 15f, 4f), tint, style = stroke)
            }
        }
    }
}

@Composable
fun GlyphButton(glyph: Glyph, description: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface, enabled: Boolean = true) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.semantics { contentDescription = description }) {
        GlyphIcon(glyph, tint = if (enabled) tint else tint.copy(alpha = 0.38f))
    }
}

/** App-wide top bar: serif title, drawn back chevron, steel hairline underneath. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaximusTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onTitleClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier
                )
            },
            navigationIcon = {
                if (onBack != null) GlyphButton(Glyph.BACK, stringResource(R.string.nav_back), onBack, tint = MaterialTheme.colorScheme.primary)
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
        SteelRule(lozenge = false)
    }
}

/** Card with a hairline border: the "engraved plate" used for grouped content. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colors, border = border) { content() }
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colors, border = border) { content() }
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

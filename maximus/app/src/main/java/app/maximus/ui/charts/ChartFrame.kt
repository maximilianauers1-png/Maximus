package app.maximus.ui.charts

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.maximus.R
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Tap target registered during drawing, in untransformed canvas pixels. */
data class HitPoint(val x: Float, val y: Float, val label: String)

/** Plain (non-state) buffer refilled on every draw pass; read by the tap handler. */
class HitRegistry {
    val points = ArrayList<HitPoint>()
    fun add(x: Float, y: Float, label: String) { points += HitPoint(x, y, label) }
}

data class ChartColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val onSurface: Color,
    val grid: Color,
    val band: Color
)

@Composable
fun rememberChartColors(): ChartColors {
    val cs = MaterialTheme.colorScheme
    return ChartColors(
        primary = cs.primary,
        secondary = cs.secondary,
        tertiary = cs.tertiary,
        onSurface = cs.onSurface,
        grid = cs.onSurface.copy(alpha = 0.15f),
        band = cs.primary.copy(alpha = 0.18f)
    )
}

/**
 * Common chart frame: two-finger pinch zoom (1x..10x), pan once zoomed, tap tooltips,
 * reset and PNG export (via the system file picker, no storage permission).
 * Charts are static (no animation), so the reduced-motion setting is satisfied by design.
 */
@Composable
fun ChartFrame(
    title: String,
    exportFileName: String,
    modifier: Modifier = Modifier,
    height: Dp = 240.dp,
    onDraw: DrawScope.(HitRegistry, TextMeasurer, ChartColors) -> Unit
) {
    val layer = rememberGraphicsLayer()
    val measurer = rememberTextMeasurer()
    val colors = rememberChartColors()
    val hits = remember { HitRegistry() }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var tooltip by remember { mutableStateOf<HitPoint?>(null) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var tipSize by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val tapRadiusPx = with(LocalDensity.current) { 32.dp.toPx() }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        if (uri != null) {
            scope.launch {
                val bitmap = layer.toImageBitmap().asAndroidBitmap()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
            }
        }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (scale != 1f) {
                    TextButton(onClick = { scale = 1f; offset = Offset.Zero; tooltip = null }) {
                        Text(stringResource(R.string.chart_reset))
                    }
                }
                TextButton(onClick = { exporter.launch("$exportFileName.png") }) { Text(stringResource(R.string.chart_export_png)) }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clipToBounds()
                    .onSizeChanged { boxSize = it }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val fingers = event.changes.count { it.pressed }
                                if (fingers >= 2 || scale > 1f) {
                                    val newScale = (scale * event.calculateZoom()).coerceIn(1f, 10f)
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val raw = offset + event.calculatePan()
                                    offset = Offset(raw.x.coerceIn(w - w * newScale, 0f), raw.y.coerceIn(h - h * newScale, 0f))
                                    scale = newScale
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            val p = Offset((tap.x - offset.x) / scale, (tap.y - offset.y) / scale)
                            val nearest = hits.points.minByOrNull { hypot(it.x - p.x, it.y - p.y) }
                            tooltip = nearest?.takeIf { hypot(it.x - p.x, it.y - p.y) <= tapRadiusPx / scale }
                        }
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            layer.record { this@drawWithContent.drawContent() }
                            drawLayer(layer)
                        }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                ) {
                    hits.points.clear()
                    onDraw(hits, measurer, colors)
                }
                tooltip?.let { t ->
                    Surface(
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .onSizeChanged { tipSize = it }
                            .offset {
                                // Keep the tooltip inside the chart: flip below the point when there is no room above,
                                // and slide it left when it would leave the right edge.
                                val px = (t.x * scale + offset.x).roundToInt()
                                val py = (t.y * scale + offset.y).roundToInt()
                                val gap = 8.dp.roundToPx()
                                val x = (px + gap).coerceAtMost(boxSize.width - tipSize.width).coerceAtLeast(0)
                                val above = py - gap - tipSize.height
                                val y = (if (above >= 0) above else py + gap).coerceIn(0, (boxSize.height - tipSize.height).coerceAtLeast(0))
                                IntOffset(x, y)
                            }
                    ) {
                        Text(t.label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
                    }
                }
            }
        }
    }
}

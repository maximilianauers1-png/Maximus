package app.maximus.ui.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Linear map of a data interval onto a pixel interval. */
class Axis(private val d0: Double, private val d1: Double, private val p0: Float, private val p1: Float) {
    fun map(v: Double): Float = if (d1 == d0) (p0 + p1) / 2f else (p0 + (v - d0) / (d1 - d0) * (p1 - p0)).toFloat()
}

/** "Nice" tick values (1-2-5 series) spanning [lo, hi], Heckbert's algorithm. */
fun niceTicks(lo: Double, hi: Double, count: Int = 5): List<Double> {
    if (!(hi > lo)) return listOf(lo)
    fun nice(x: Double, round: Boolean): Double {
        val e = floor(log10(x))
        val f = x / 10.0.pow(e)
        val nf = if (round) when { f < 1.5 -> 1.0; f < 3 -> 2.0; f < 7 -> 5.0; else -> 10.0 }
        else when { f <= 1 -> 1.0; f <= 2 -> 2.0; f <= 5 -> 5.0; else -> 10.0 }
        return nf * 10.0.pow(e)
    }
    val range = nice(hi - lo, false)
    val step = nice(range / (count - 1), true)
    val start = floor(lo / step) * step
    val end = ceil(hi / step) * step
    val out = mutableListOf<Double>()
    var v = start
    while (v <= end + step * 0.5) { out += v; v += step }
    return out
}

private val LABEL = TextStyle(fontSize = 10.sp)

fun DrawScope.label(m: TextMeasurer, text: String, at: Offset, color: Color, alignRight: Boolean = false, center: Boolean = false) {
    val layout = m.measure(text, LABEL.copy(color = color))
    val x = when {
        alignRight -> at.x - layout.size.width
        center -> at.x - layout.size.width / 2f
        else -> at.x
    }
    drawText(layout, topLeft = Offset(x, at.y - layout.size.height / 2f))
}

fun formatTick(v: Double): String = if (abs(v - Math.rint(v)) < 1e-9) v.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.1f", v)

/** Draws horizontal grid and y labels; returns the plot rectangle and the y axis. */
fun DrawScope.plotArea(m: TextMeasurer, c: ChartColors, yLo: Double, yHi: Double): Pair<Rect, Axis> {
    val ticks = niceTicks(yLo, yHi)
    val left = 44f
    val rect = Rect(left, 8f, size.width - 8f, size.height - 24f)
    val y = Axis(ticks.first(), ticks.last(), rect.bottom, rect.top)
    for (t in ticks) {
        val py = y.map(t)
        drawLine(c.grid, Offset(rect.left, py), Offset(rect.right, py), strokeWidth = 1f)
        label(m, formatTick(t), Offset(rect.left - 4f, py), c.onSurface, alignRight = true)
    }
    return rect to y
}

/** Scatter + fitted line + shaded prediction band. */
fun DrawScope.drawTrendChart(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    xs: List<Double>, ys: List<Double>, pointLabels: List<String>,
    fitX: List<Double>, fitY: List<Double>, bandLo: List<Double>, bandHi: List<Double>,
    xLabel: (Double) -> String
) {
    if (xs.isEmpty()) return
    val yLo = (ys + bandLo).minOrNull()!!
    val yHi = (ys + bandHi).maxOrNull()!!
    val pad = max(1.0, (yHi - yLo) * 0.05)
    val (rect, yAxis) = plotArea(m, c, yLo - pad, yHi + pad)
    val xLo = xs.minOrNull()!!
    val xHi = max(xs.maxOrNull()!!, fitX.maxOrNull() ?: xLo)
    val xAxis = Axis(xLo, if (xHi > xLo) xHi else xLo + 1, rect.left + 6f, rect.right - 6f)
    if (fitX.size >= 2) {
        val band = Path()
        fitX.forEachIndexed { i, x -> if (i == 0) band.moveTo(xAxis.map(x), yAxis.map(bandHi[i])) else band.lineTo(xAxis.map(x), yAxis.map(bandHi[i])) }
        for (i in fitX.indices.reversed()) band.lineTo(xAxis.map(fitX[i]), yAxis.map(bandLo[i]))
        band.close()
        drawPath(band, c.band)
        val line = Path()
        fitX.forEachIndexed { i, x -> if (i == 0) line.moveTo(xAxis.map(x), yAxis.map(fitY[i])) else line.lineTo(xAxis.map(x), yAxis.map(fitY[i])) }
        drawPath(line, c.secondary, style = Stroke(width = 3f))
    }
    xs.indices.forEach { i ->
        val p = Offset(xAxis.map(xs[i]), yAxis.map(ys[i]))
        drawCircle(c.primary, radius = 5f, center = p)
        hits.add(p.x, p.y, pointLabels[i])
    }
    label(m, xLabel(xLo), Offset(rect.left, rect.bottom + 12f), c.onSurface)
    label(m, xLabel(xHi), Offset(rect.right, rect.bottom + 12f), c.onSurface, alignRight = true)
}

/** Vertical bars with category labels (every k-th label shown to avoid overlap). */
fun DrawScope.drawBarChart(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    labels: List<String>, values: List<Double>, tooltips: List<String>
) {
    if (values.isEmpty()) return
    val (rect, yAxis) = plotArea(m, c, 0.0, max(1e-9, values.maxOrNull()!!))
    val n = values.size
    val slot = rect.width / n
    val barW = slot * 0.7f
    val every = max(1, ceil(n / 8.0).toInt())
    values.forEachIndexed { i, v ->
        val x = rect.left + i * slot + (slot - barW) / 2f
        val top = yAxis.map(v)
        drawRect(c.primary, topLeft = Offset(x, top), size = Size(barW, rect.bottom - top))
        hits.add(x + barW / 2f, top, tooltips[i])
        if (i % every == 0) label(m, labels[i], Offset(x + barW / 2f, rect.bottom + 12f), c.onSurface, center = true)
    }
}

/** Heatmap: rows = weekdays (0 = Monday), columns = weeks; value in [0, 1] maps to alpha. */
fun DrawScope.drawHeatmap(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    columns: List<Long>, rowLabels: List<String>, value: (Long, Int) -> Double?, tooltip: (Long, Int, Double) -> String,
    columnLabel: (Long) -> String
) {
    if (columns.isEmpty()) return
    val left = 32f
    val bottom = size.height - 20f
    val cellW = (size.width - left - 4f) / columns.size
    val cellH = (bottom - 4f) / 7f
    for (r in 0 until 7) label(m, rowLabels[r], Offset(2f, 4f + r * cellH + cellH / 2f), c.onSurface)
    columns.forEachIndexed { ci, week ->
        for (r in 0 until 7) {
            val v = value(week, r)
            val topLeft = Offset(left + ci * cellW + 1f, 4f + r * cellH + 1f)
            val cell = Size(cellW - 2f, cellH - 2f)
            if (v == null) {
                drawRect(c.grid, topLeft, cell)
            } else {
                drawRect(c.primary.copy(alpha = (0.15f + 0.85f * v.toFloat().coerceIn(0f, 1f))), topLeft, cell)
                hits.add(topLeft.x + cell.width / 2f, topLeft.y + cell.height / 2f, tooltip(week, r, v))
            }
        }
    }
    val every = max(1, ceil(columns.size / 6.0).toInt())
    columns.forEachIndexed { ci, w ->
        if (ci % every == 0) label(m, columnLabel(w), Offset(left + ci * cellW, bottom + 10f), c.onSurface)
    }
}

/** Radar (spider) chart; values are normalized to the largest axis. */
fun DrawScope.drawRadar(hits: HitRegistry, m: TextMeasurer, c: ChartColors, labels: List<String>, values: List<Double>, tooltips: List<String>) {
    val n = labels.size
    if (n < 3) return
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = min(size.width, size.height) / 2f - 28f
    val maxV = max(1e-9, values.maxOrNull() ?: 1.0)
    fun point(i: Int, frac: Double): Offset {
        val a = -PI / 2 + 2 * PI * i / n
        return Offset((center.x + radius * frac * cos(a)).toFloat(), (center.y + radius * frac * sin(a)).toFloat())
    }
    for (ring in 1..4) {
        val p = Path()
        for (i in 0 until n) { val q = point(i, ring / 4.0); if (i == 0) p.moveTo(q.x, q.y) else p.lineTo(q.x, q.y) }
        p.close()
        drawPath(p, c.grid, style = Stroke(width = 1f))
    }
    val poly = Path()
    for (i in 0 until n) {
        drawLine(c.grid, center, point(i, 1.0))
        val q = point(i, values[i] / maxV)
        if (i == 0) poly.moveTo(q.x, q.y) else poly.lineTo(q.x, q.y)
        hits.add(q.x, q.y, tooltips[i])
        label(m, labels[i], point(i, 1.12), c.onSurface, center = true)
    }
    poly.close()
    drawPath(poly, c.band)
    drawPath(poly, c.primary, style = Stroke(width = 3f))
}

/** Timeline: x = day, one row per series; filled marker = e1RM PR, ring = rep PR. */
fun DrawScope.drawTimeline(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    rows: List<String>, events: List<Triple<Long, Int, Boolean>>, tooltips: List<String>, xLabel: (Double) -> String
) {
    if (events.isEmpty() || rows.isEmpty()) return
    val left = 8f
    val rect = Rect(left, 8f, size.width - 8f, size.height - 24f)
    val rowH = rect.height / rows.size
    val xLo = events.minOf { it.first }.toDouble()
    val xHi = events.maxOf { it.first }.toDouble()
    val x = Axis(xLo, if (xHi > xLo) xHi else xLo + 1, rect.left + 8f, rect.right - 8f)
    rows.forEachIndexed { i, name ->
        val y = rect.top + i * rowH + rowH / 2f
        drawLine(c.grid, Offset(rect.left, y), Offset(rect.right, y))
        label(m, name, Offset(rect.left, y - rowH / 2f + 8f), c.onSurface)
    }
    events.forEachIndexed { i, (day, row, e1rmPr) ->
        val p = Offset(x.map(day.toDouble()), rect.top + row * rowH + rowH / 2f)
        if (e1rmPr) drawCircle(c.primary, 6f, p) else drawCircle(c.tertiary, 6f, p, style = Stroke(width = 2.5f))
        hits.add(p.x, p.y, tooltips[i])
    }
    label(m, xLabel(xLo), Offset(rect.left, rect.bottom + 12f), c.onSurface)
    label(m, xLabel(xHi), Offset(rect.right, rect.bottom + 12f), c.onSurface, alignRight = true)
}

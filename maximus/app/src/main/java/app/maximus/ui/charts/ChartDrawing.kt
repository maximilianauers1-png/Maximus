package app.maximus.ui.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
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

/** Gap between labels and the plot, and the outer safety margin, in dp (converted per density). */
private val GAP = 4.dp
private val EDGE = 2.dp

/**
 * Draws a label and keeps it fully inside the canvas: the anchor is moved inwards when the text
 * would leave the drawing area. This is what prevents clipped axis labels on high-density screens.
 */
fun DrawScope.label(m: TextMeasurer, text: String, at: Offset, color: Color, alignRight: Boolean = false, center: Boolean = false) {
    val layout = m.measure(text, LABEL.copy(color = color))
    val w = layout.size.width.toFloat()
    val h = layout.size.height.toFloat()
    val rawX = when {
        alignRight -> at.x - w
        center -> at.x - w / 2f
        else -> at.x
    }
    val x = rawX.coerceIn(0f, max(0f, size.width - w))
    val y = (at.y - h / 2f).coerceIn(0f, max(0f, size.height - h))
    drawText(layout, topLeft = Offset(x, y))
}

private fun DrawScope.labelSize(m: TextMeasurer, text: String): Size {
    val s = m.measure(text, LABEL).size
    return Size(s.width.toFloat(), s.height.toFloat())
}

/** Tick label with as many decimals as the tick step needs (0.25 → "0.25", 50 → "50"). */
fun formatTick(v: Double, step: Double = 1.0): String {
    if (abs(v - Math.rint(v)) < 1e-9 && step >= 1.0) return v.toLong().toString()
    val decimals = if (step > 0) (-floor(log10(step))).toInt().coerceIn(0, 6) else 1
    return String.format(java.util.Locale.getDefault(), "%.${max(decimals, 1)}f", v)
}

/** Plot rectangle with margins sized from the measured labels. */
private fun DrawScope.measuredPlot(m: TextMeasurer, yLabels: List<String>, hasXLabels: Boolean, rightLabel: String? = null): Rect {
    val gap = GAP.toPx()
    val edge = EDGE.toPx()
    val lineH = labelSize(m, "0").height
    val yW = yLabels.maxOfOrNull { labelSize(m, it).width } ?: 0f
    val left = edge + yW + gap
    val right = size.width - edge - (rightLabel?.let { labelSize(m, it).width / 2f } ?: 0f)
    val top = edge + lineH / 2f
    val bottom = size.height - edge - if (hasXLabels) lineH + gap else lineH / 2f
    return Rect(left, top, max(left + 1f, right), max(top + 1f, bottom))
}

/** Draws horizontal grid and y labels; returns the plot rectangle and the y axis. */
fun DrawScope.plotArea(m: TextMeasurer, c: ChartColors, yLo: Double, yHi: Double, hasXLabels: Boolean = true): Pair<Rect, Axis> {
    val ticks = niceTicks(yLo, yHi)
    val step = if (ticks.size > 1) ticks[1] - ticks[0] else 1.0
    val labels = ticks.map { formatTick(it, step) }
    val rect = measuredPlot(m, labels, hasXLabels)
    val y = Axis(ticks.first(), ticks.last(), rect.bottom, rect.top)
    ticks.forEachIndexed { i, t ->
        val py = y.map(t)
        drawLine(c.grid, Offset(rect.left, py), Offset(rect.right, py), strokeWidth = 1f)
        label(m, labels[i], Offset(rect.left - GAP.toPx(), py), c.onSurface, alignRight = true)
    }
    return rect to y
}

/** y coordinate of the centre of an x-axis label line under [rect]. */
private fun DrawScope.xLabelY(m: TextMeasurer, rect: Rect): Float = rect.bottom + GAP.toPx() + labelSize(m, "0").height / 2f

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
    val inset = 5.dp.toPx()
    val xAxis = Axis(xLo, if (xHi > xLo) xHi else xLo + 1, rect.left + inset, rect.right - inset)
    if (fitX.size >= 2) {
        val band = Path()
        fitX.forEachIndexed { i, x -> if (i == 0) band.moveTo(xAxis.map(x), yAxis.map(bandHi[i])) else band.lineTo(xAxis.map(x), yAxis.map(bandHi[i])) }
        for (i in fitX.indices.reversed()) band.lineTo(xAxis.map(fitX[i]), yAxis.map(bandLo[i]))
        band.close()
        drawPath(band, c.band)
        val line = Path()
        fitX.forEachIndexed { i, x -> if (i == 0) line.moveTo(xAxis.map(x), yAxis.map(fitY[i])) else line.lineTo(xAxis.map(x), yAxis.map(fitY[i])) }
        drawPath(line, c.secondary, style = Stroke(width = 2.dp.toPx()))
    }
    val r = 3.5.dp.toPx()
    xs.indices.forEach { i ->
        val p = Offset(xAxis.map(xs[i]), yAxis.map(ys[i]))
        drawCircle(c.primary, radius = r, center = p)
        hits.add(p.x, p.y, pointLabels[i])
    }
    val ly = xLabelY(m, rect)
    label(m, xLabel(xLo), Offset(rect.left, ly), c.onSurface)
    label(m, xLabel(xHi), Offset(rect.right, ly), c.onSurface, alignRight = true)
}

/** Vertical bars with category labels (every k-th label shown so that labels never overlap). */
fun DrawScope.drawBarChart(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    labels: List<String>, values: List<Double>, tooltips: List<String>
) {
    if (values.isEmpty()) return
    val (rect, yAxis) = plotArea(m, c, 0.0, max(1e-9, values.maxOrNull()!!))
    val n = values.size
    val slot = rect.width / n
    val barW = slot * 0.7f
    val widest = labels.maxOfOrNull { labelSize(m, it).width } ?: 0f
    val every = max(1, ceil((widest + GAP.toPx() * 2) / slot).toInt())
    val ly = xLabelY(m, rect)
    values.forEachIndexed { i, v ->
        val x = rect.left + i * slot + (slot - barW) / 2f
        val top = yAxis.map(v)
        drawRect(c.primary, topLeft = Offset(x, top), size = Size(barW, rect.bottom - top))
        hits.add(x + barW / 2f, top, tooltips[i])
        if (i % every == 0) label(m, labels[i], Offset(x + barW / 2f, ly), c.onSurface, center = true)
    }
}

/** Heatmap: rows = weekdays (0 = Monday), columns = weeks; value in [0, 1] maps to alpha. */
fun DrawScope.drawHeatmap(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    columns: List<Long>, rowLabels: List<String>, value: (Long, Int) -> Double?, tooltip: (Long, Int, Double) -> String,
    columnLabel: (Long) -> String
) {
    if (columns.isEmpty()) return
    val gap = GAP.toPx()
    val edge = EDGE.toPx()
    val lineH = labelSize(m, "0").height
    val left = edge + (rowLabels.maxOfOrNull { labelSize(m, it).width } ?: 0f) + gap
    val top = edge
    val bottom = size.height - edge - lineH - gap
    val cellW = (size.width - left - edge) / columns.size
    val cellH = (bottom - top) / 7f
    for (r in 0 until 7) label(m, rowLabels[r], Offset(edge, top + r * cellH + cellH / 2f), c.onSurface)
    columns.forEachIndexed { ci, week ->
        for (r in 0 until 7) {
            val v = value(week, r)
            val topLeft = Offset(left + ci * cellW + 1f, top + r * cellH + 1f)
            val cell = Size(max(1f, cellW - 2f), max(1f, cellH - 2f))
            if (v == null) {
                drawRect(c.grid, topLeft, cell)
            } else {
                drawRect(c.primary.copy(alpha = (0.15f + 0.85f * v.toFloat().coerceIn(0f, 1f))), topLeft, cell)
                hits.add(topLeft.x + cell.width / 2f, topLeft.y + cell.height / 2f, tooltip(week, r, v))
            }
        }
    }
    val widest = columns.maxOfOrNull { labelSize(m, columnLabel(it)).width } ?: 0f
    val every = max(1, ceil((widest + gap * 2) / cellW).toInt())
    val ly = bottom + gap + lineH / 2f
    columns.forEachIndexed { ci, w ->
        if (ci % every == 0) label(m, columnLabel(w), Offset(left + ci * cellW, ly), c.onSurface)
    }
}

/** Radar (spider) chart; values are normalized to the largest axis. Labels are kept inside the canvas. */
fun DrawScope.drawRadar(hits: HitRegistry, m: TextMeasurer, c: ChartColors, labels: List<String>, values: List<Double>, tooltips: List<String>) {
    val n = labels.size
    if (n < 3) return
    val lineH = labelSize(m, "0").height
    val widest = labels.maxOfOrNull { labelSize(m, it).width } ?: 0f
    val center = Offset(size.width / 2f, size.height / 2f)
    // Room for one label line above/below and for half the widest label left/right, capped so the web stays readable.
    val radius = max(
        24.dp.toPx(),
        min(size.height / 2f - lineH - GAP.toPx() * 2, size.width / 2f - min(widest, size.width / 3f) / 2f - GAP.toPx() * 2)
    )
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
    val labelFrac = 1.0 + (lineH / 2f + GAP.toPx()) / radius
    for (i in 0 until n) {
        drawLine(c.grid, center, point(i, 1.0))
        val q = point(i, values[i] / maxV)
        if (i == 0) poly.moveTo(q.x, q.y) else poly.lineTo(q.x, q.y)
        hits.add(q.x, q.y, tooltips[i])
        val lp = point(i, labelFrac)
        val dx = lp.x - center.x
        when {
            abs(dx) < 1f -> label(m, labels[i], lp, c.onSurface, center = true)
            dx > 0 -> label(m, labels[i], lp, c.onSurface)
            else -> label(m, labels[i], lp, c.onSurface, alignRight = true)
        }
    }
    poly.close()
    drawPath(poly, c.band)
    drawPath(poly, c.primary, style = Stroke(width = 2.dp.toPx()))
}

/** Timeline: x = day, one row per series; filled marker = e1RM PR, ring = rep PR. */
fun DrawScope.drawTimeline(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    rows: List<String>, events: List<Triple<Long, Int, Boolean>>, tooltips: List<String>, xLabel: (Double) -> String
) {
    if (events.isEmpty() || rows.isEmpty()) return
    val edge = EDGE.toPx()
    val lineH = labelSize(m, "0").height
    val rect = Rect(edge, edge, size.width - edge, size.height - edge - lineH - GAP.toPx())
    val rowH = rect.height / rows.size
    val xLo = events.minOf { it.first }.toDouble()
    val xHi = events.maxOf { it.first }.toDouble()
    val inset = 8.dp.toPx()
    val x = Axis(xLo, if (xHi > xLo) xHi else xLo + 1, rect.left + inset, rect.right - inset)
    rows.forEachIndexed { i, name ->
        val y = rect.top + i * rowH + rowH / 2f
        drawLine(c.grid, Offset(rect.left, y), Offset(rect.right, y))
        label(m, name, Offset(rect.left, rect.top + i * rowH + lineH / 2f), c.onSurface)
    }
    val r = 4.dp.toPx()
    events.forEachIndexed { i, (day, row, e1rmPr) ->
        val p = Offset(x.map(day.toDouble()), rect.top + row * rowH + rowH / 2f + lineH / 4f)
        if (e1rmPr) drawCircle(c.primary, r, p) else drawCircle(c.tertiary, r, p, style = Stroke(width = 1.6.dp.toPx()))
        hits.add(p.x, p.y, tooltips[i])
    }
    val ly = xLabelY(m, rect)
    label(m, xLabel(xLo), Offset(rect.left, ly), c.onSurface)
    label(m, xLabel(xHi), Offset(rect.right, ly), c.onSurface, alignRight = true)
}

/** One curve of a line chart. [dashed] draws it dashed (e.g. a reference or second series). */
data class LineSeries(val name: String, val xs: List<Double>, val ys: List<Double>, val color: Color, val dashed: Boolean = false, val markers: Boolean = false, val connect: Boolean = true)

/**
 * Multi-series line chart with nice ticks on both axes. Non-finite points split a curve (poles of a
 * plotted function). [yRange] fixes the vertical range; otherwise it is fitted to the data with 5 % padding.
 */
fun DrawScope.drawLineChart(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    series: List<LineSeries>,
    xFormat: ((Double) -> String)? = null,
    yRange: Pair<Double, Double>? = null,
    tooltip: (LineSeries, Int) -> String = { s, i -> "${s.name}: ${formatTick(s.xs[i], 0.01)} → ${formatTick(s.ys[i], 0.001)}" },
    hitEvery: Int = 1
) {
    val pts = series.flatMap { s -> s.xs.indices.filter { s.ys[it].isFinite() && s.xs[it].isFinite() }.map { s.xs[it] to s.ys[it] } }
    if (pts.isEmpty()) return
    val xLo = pts.minOf { it.first }
    val xHi = pts.maxOf { it.first }.let { if (it > xLo) it else xLo + 1 }
    val (yLo0, yHi0) = yRange ?: run {
        val lo = pts.minOf { it.second }
        val hi = pts.maxOf { it.second }
        val pad = if (hi > lo) (hi - lo) * 0.05 else max(1.0, abs(lo) * 0.1)
        (lo - pad) to (hi + pad)
    }
    val (rect, yAxis) = plotArea(m, c, yLo0, yHi0)
    val xTicks = niceTicks(xLo, xHi, 5)
    val xStep = if (xTicks.size > 1) xTicks[1] - xTicks[0] else 1.0
    val xAxis = Axis(xLo, xHi, rect.left, rect.right)
    val ly = xLabelY(m, rect)
    xTicks.filter { it >= xLo - 1e-9 && it <= xHi + 1e-9 }.forEach { t ->
        val px = xAxis.map(t)
        drawLine(c.grid, Offset(px, rect.top), Offset(px, rect.bottom), strokeWidth = 1f)
        label(m, xFormat?.invoke(t) ?: formatTick(t, xStep), Offset(px, ly), c.onSurface, center = true)
    }
    // Zero lines for orientation.
    if (yLo0 < 0 && yHi0 > 0) drawLine(c.onSurface.copy(alpha = 0.35f), Offset(rect.left, yAxis.map(0.0)), Offset(rect.right, yAxis.map(0.0)), strokeWidth = 1.5f)
    if (xLo < 0 && xHi > 0) drawLine(c.onSurface.copy(alpha = 0.35f), Offset(xAxis.map(0.0), rect.top), Offset(xAxis.map(0.0), rect.bottom), strokeWidth = 1.5f)
    val stroke = 2.dp.toPx()
    val dash = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
    for (s in series) {
        val path = Path()
        var pen = false
        s.xs.indices.forEach { i ->
            val y = s.ys[i]
            if (!y.isFinite() || y < yLo0 - (yHi0 - yLo0) * 4 || y > yHi0 + (yHi0 - yLo0) * 4) { pen = false; return@forEach }
            val px = xAxis.map(s.xs[i])
            val py = yAxis.map(y).coerceIn(rect.top - 2 * rect.height, rect.bottom + 2 * rect.height)
            if (pen) path.lineTo(px, py) else path.moveTo(px, py)
            pen = true
            if (i % hitEvery == 0 && py in rect.top..rect.bottom) hits.add(px, py, tooltip(s, i))
            if (s.markers && py in rect.top..rect.bottom) drawCircle(s.color, 3.dp.toPx(), Offset(px, py))
        }
        if (s.connect) clipRect(rect.left, rect.top - 1f, rect.right, rect.bottom + 1f) {
            drawPath(path, s.color, style = Stroke(width = stroke, pathEffect = if (s.dashed) dash else null))
        }
    }
}

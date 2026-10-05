package app.maximus.ui.charts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

/** A measured point on a progression chart; [special] marks entries from the historic record book. */
data class ProgPoint(val day: Long, val value: Double, val label: String, val special: Boolean = false)

/** Model curve sample: day, mean, half-width of the prediction band. */
data class CurveSample(val day: Double, val mean: Double, val half: Double)

private val SMALL = TextStyle(fontSize = 10.sp)

private fun DrawScope.textH(m: TextMeasurer): Float = m.measure("0", SMALL).size.height.toFloat()
private fun DrawScope.textW(m: TextMeasurer, s: String): Float = m.measure(s, SMALL).size.width.toFloat()

/**
 * Calendar ticks between two epoch days: the month step (1, 2, 3, 6, 12, 24, 60) is the smallest that
 * keeps the labels from overlapping in [width] pixels. Ticks sit on the first day of a month.
 */
fun DrawScope.dateTicks(m: TextMeasurer, lo: Double, hi: Double, width: Float): List<Pair<Double, String>> {
    val d0 = LocalDate.ofEpochDay(lo.toLong())
    val d1 = LocalDate.ofEpochDay(hi.toLong())
    val months = max(1L, (d1.year - d0.year) * 12L + (d1.monthValue - d0.monthValue))
    val labelW = textW(m, "00/00") + 12.dp.toPx()
    val capacity = max(2, (width / labelW).toInt())
    val step = listOf(1, 2, 3, 6, 12, 24, 60).firstOrNull { months / it + 1 <= capacity } ?: 120
    var t = d0.withDayOfMonth(1).plusMonths(1)
    if (step >= 12) t = LocalDate.of(d0.year + 1, 1, 1)
    else while ((t.monthValue - 1) % step != 0) t = t.plusMonths(1)
    val out = ArrayList<Pair<Double, String>>()
    while (!t.isAfter(d1)) {
        val label = if (step >= 12) "${t.year}" else "%02d/%02d".format(t.monthValue, t.year % 100)
        out += t.toEpochDay().toDouble() to label
        t = t.plusMonths(step.toLong())
    }
    if (out.isEmpty()) {
        // Less than a month of data: label both ends with the day instead.
        fun dm(d: LocalDate) = "%02d.%02d.".format(d.dayOfMonth, d.monthValue)
        out += lo to dm(d0)
        if (hi - lo >= 7) out += hi to dm(d1)
    }
    return out
}

/**
 * Strength progression over time: measured points (circles; record-book entries as gold rings), the
 * record staircase, the fitted model with its 95 % band reaching into the future (dashed after today),
 * an optional target line. Everything is drawn inside the measured plot rectangle; markers are inset by
 * their radius, curves are clipped, labels are kept inside the canvas.
 */
fun DrawScope.drawProgression(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    points: List<ProgPoint>,
    envelope: List<ProgPoint>,
    curve: List<CurveSample>,
    today: Long,
    target: Double?
) {
    if (points.isEmpty()) return
    val ys = points.map { it.value }
    val dataLo = ys.min(); val dataHi = ys.max()
    val span = max(5.0, dataHi - dataLo)
    // The band may not blow up the scale: clamp it to a window around the data.
    val clampLo = dataLo - 0.6 * span
    val clampHi = dataHi + 0.8 * span
    val curveLo = curve.minOfOrNull { (it.mean - it.half).coerceAtLeast(clampLo) } ?: dataLo
    val curveHi = curve.maxOfOrNull { (it.mean + it.half).coerceAtMost(clampHi) } ?: dataHi
    var yLo = min(dataLo, curveLo); var yHi = max(dataHi, curveHi)
    if (target != null && target < clampHi + span) { yLo = min(yLo, target); yHi = max(yHi, target) }
    val pad = max(1.0, (yHi - yLo) * 0.06)
    val (rect, yAxis) = plotArea(m, c, yLo - pad, yHi + pad)
    val xLo = points.minOf { it.day }.toDouble()
    val xHi = max(points.maxOf { it.day }.toDouble(), curve.maxOfOrNull { it.day } ?: xLo).let { if (it > xLo) it else xLo + 30 }
    val inset = 6.dp.toPx()
    val xAxis = Axis(xLo, xHi, rect.left + inset, rect.right - inset)

    // Calendar grid.
    val ly = rect.bottom + 4.dp.toPx() + textH(m) / 2f
    for ((d, s) in dateTicks(m, xLo, xHi, rect.width)) {
        val px = xAxis.map(d)
        drawLine(c.grid, Offset(px, rect.top), Offset(px, rect.bottom), strokeWidth = 1f)
        // A label that would reach under the y-axis labels is left out (its grid line stays).
        if (px - textW(m, s) / 2f >= rect.left - 2.dp.toPx()) label(m, s, Offset(px, ly), c.onSurface, center = true)
    }

    clipRect(rect.left, rect.top, rect.right, rect.bottom) {
        if (curve.size >= 2) {
            val band = Path()
            curve.forEachIndexed { i, s ->
                val y = yAxis.map((s.mean + s.half).coerceAtMost(clampHi))
                if (i == 0) band.moveTo(xAxis.map(s.day), y) else band.lineTo(xAxis.map(s.day), y)
            }
            for (i in curve.indices.reversed()) band.lineTo(xAxis.map(curve[i].day), yAxis.map((curve[i].mean - curve[i].half).coerceAtLeast(clampLo)))
            band.close()
            drawPath(band, c.band)
            val past = Path(); val future = Path()
            var pStarted = false; var fStarted = false
            for (s in curve) {
                val p = Offset(xAxis.map(s.day), yAxis.map(s.mean))
                if (s.day <= today) { if (pStarted) past.lineTo(p.x, p.y) else past.moveTo(p.x, p.y); pStarted = true }
                if (s.day >= today - 1) { if (fStarted) future.lineTo(p.x, p.y) else future.moveTo(p.x, p.y); fStarted = true }
            }
            drawPath(past, c.primary.copy(alpha = 0.9f), style = Stroke(width = 2.dp.toPx()))
            drawPath(future, c.primary, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))))
        }
        if (envelope.size >= 2) {
            val step = Path()
            envelope.forEachIndexed { i, p ->
                val x = xAxis.map(p.day.toDouble()); val y = yAxis.map(p.value)
                if (i == 0) step.moveTo(x, y) else { step.lineTo(x, yAxis.map(envelope[i - 1].value)); step.lineTo(x, y) }
            }
            step.lineTo(xAxis.map(min(xHi, today.toDouble())), yAxis.map(envelope.last().value))
            drawPath(step, c.secondary, style = Stroke(width = 1.6.dp.toPx()))
        }
        if (today.toDouble() in xLo..xHi) {
            val tx = xAxis.map(today.toDouble())
            drawLine(c.onSurface.copy(alpha = 0.45f), Offset(tx, rect.top), Offset(tx, rect.bottom), strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
        }
        if (target != null) {
            val ty = yAxis.map(target)
            drawLine(c.tertiary.copy(alpha = 0.8f), Offset(rect.left, ty), Offset(rect.right, ty), strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
        }
    }
    if (target != null) {
        val ty = yAxis.map(target)
        if (ty in rect.top..rect.bottom) label(m, "Ziel", Offset(rect.right - 2.dp.toPx(), ty - textH(m) / 2f - 1.dp.toPx()), c.tertiary, alignRight = true)
    }
    val r = 3.dp.toPx()
    for (p in points) {
        val o = Offset(xAxis.map(p.day.toDouble()), yAxis.map(p.value))
        if (p.special) {
            drawCircle(c.secondary, r * 1.6f, o, style = Stroke(width = 2.dp.toPx()))
        } else drawCircle(c.primary, r, o)
        hits.add(o.x, o.y, p.label)
    }
    for (s in curve.filterIndexed { i, _ -> i % 6 == 0 }.filter { it.day > today }) {
        val o = Offset(xAxis.map(s.day), yAxis.map(s.mean))
        if (o.y in rect.top..rect.bottom) hits.add(o.x, o.y, "Prognose ${LocalDate.ofEpochDay(s.day.toLong()).let { "%02d.%02d.%02d".format(it.dayOfMonth, it.monthValue, it.year % 100) }}: ${"%.1f".format(s.mean)} ± ${"%.1f".format(s.half)} kg")
    }
}

/**
 * Population distribution (lognormal density in kg) with the athlete's value: the area left of the value
 * — the share of the population below — is filled; tier boundaries are faint dashed lines (tap for name).
 */
fun DrawScope.drawDistribution(
    hits: HitRegistry, m: TextMeasurer, c: ChartColors,
    density: List<Pair<Double, Double>>,
    value: Double,
    valueLabel: String,
    tiers: List<Pair<Double, String>>
) {
    if (density.size < 2) return
    val edge = 2.dp.toPx()
    val lineH = textH(m)
    val rect = Rect(edge + 2.dp.toPx(), edge + lineH + 6.dp.toPx(), size.width - edge - 2.dp.toPx(), size.height - edge - lineH - 4.dp.toPx())
    val xLo = density.first().first; val xHi = density.last().first
    val fMax = density.maxOf { it.second }
    val x = Axis(xLo, xHi, rect.left, rect.right)
    val y = Axis(0.0, fMax * 1.05, rect.bottom, rect.top)
    drawLine(c.grid, Offset(rect.left, rect.bottom), Offset(rect.right, rect.bottom), strokeWidth = 1f)
    val ticks = niceTicks(xLo, xHi, 6).filter { it in xLo..xHi }
    val step = if (ticks.size > 1) ticks[1] - ticks[0] else 1.0
    for (t in ticks) {
        val px = x.map(t)
        drawLine(c.grid, Offset(px, rect.bottom), Offset(px, rect.bottom + 3.dp.toPx()))
        label(m, formatTick(t, step), Offset(px, rect.bottom + 4.dp.toPx() + lineH / 2f), c.onSurface, center = true)
    }
    val full = Path(); val left = Path()
    density.forEachIndexed { i, (kx, f) ->
        val px = x.map(kx); val py = y.map(f)
        if (i == 0) full.moveTo(px, py) else full.lineTo(px, py)
    }
    val vx = x.map(value.coerceIn(xLo, xHi))
    left.moveTo(rect.left, rect.bottom)
    for ((kx, f) in density) { if (kx > value) break; left.lineTo(x.map(kx), y.map(f)) }
    val fAtV = density.minBy { kotlin.math.abs(it.first - value) }.second
    left.lineTo(vx, y.map(fAtV)); left.lineTo(vx, rect.bottom); left.close()
    clipRect(rect.left, rect.top - 2f, rect.right, rect.bottom) {
        drawPath(left, c.primary.copy(alpha = 0.35f))
        drawPath(full, c.onSurface.copy(alpha = 0.75f), style = Stroke(width = 1.8.dp.toPx()))
        for ((tk, _) in tiers) if (tk in xLo..xHi) {
            val px = x.map(tk)
            drawLine(c.secondary.copy(alpha = 0.45f), Offset(px, rect.top), Offset(px, rect.bottom), strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
        }
    }
    for ((tk, name) in tiers) if (tk in xLo..xHi) hits.add(x.map(tk), rect.top + 6.dp.toPx(), "$name ab ${"%.1f".format(tk)} kg")
    drawLine(c.primary, Offset(vx, rect.top - 2.dp.toPx()), Offset(vx, rect.bottom), strokeWidth = 2.5.dp.toPx())
    drawCircle(c.primary, 4.dp.toPx(), Offset(vx, rect.top - 2.dp.toPx()))
    label(m, valueLabel, Offset(vx, edge + lineH / 2f), c.primary, center = true)
    hits.add(vx, rect.top, valueLabel)
}

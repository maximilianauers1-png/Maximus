@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.AiGlossary
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.GlossaryDuel
import app.maximus.lab.domain.GradientGame
import app.maximus.lab.domain.GridWorld
import app.maximus.lab.domain.KMeansGame
import app.maximus.lab.domain.MlTechnique
import app.maximus.lab.domain.OverfitGame
import app.maximus.lab.domain.P2
import app.maximus.lab.domain.Perceptron
import app.maximus.lab.domain.PerceptronGame
import app.maximus.lab.domain.QLearner
import app.maximus.lab.domain.TechniqueDetective
import app.maximus.lab.domain.ThresholdGame
import app.maximus.lab.domain.ThresholdMission
import app.maximus.lab.domain.Topic
import app.maximus.lab.domain.TreeGame
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.LineSeries
import app.maximus.ui.charts.drawLineChart
import app.maximus.ui.strongman.SectionCard
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

private fun f(v: Double, d: Int = 3) = Fmt.num(v, d)

/** Shared column for all games: scrollable, padded. */
@Composable
private fun GameColumn(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) { content() }
}

/** Score banner after a scored attempt. */
@Composable
private fun ScoreBanner(score: Int, text: String) {
    val accent = topicColor(Topic.AI)
    Surface(shape = RoundedCornerShape(12.dp), color = accent.copy(alpha = 0.12f), border = BorderStroke(1.dp, accent)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$score", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accent, modifier = Modifier.width(64.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LabeledSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, display: String, onChange: (Float) -> Unit) {
    Column {
        Row {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(display, style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"), color = topicColor(Topic.AI))
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}

/** Square canvas with a light frame; content draws in unit coordinates via [toPx]. */
@Composable
private fun GameCanvas(modifier: Modifier = Modifier, square: Boolean = true, onTap: ((Offset, Size) -> Unit)? = null, draw: DrawScope.() -> Unit) {
    val frame = MaterialTheme.colorScheme.outlineVariant
    val bg = MaterialTheme.colorScheme.surfaceContainerLow
    var m = modifier.fillMaxWidth().let { if (square) it.aspectRatio(1f) else it }.clipToBounds().background(bg, RoundedCornerShape(10.dp))
    // The latest callback is read at tap time, so state replaced by remember(keys) is never stale.
    val tap by rememberUpdatedState(onTap)
    if (onTap != null) m = m.pointerInput(Unit) { detectTapGestures { o -> tap?.invoke(o, Size(size.width.toFloat(), size.height.toFloat())) } }
    Canvas(m) {
        draw()
        drawRect(frame, style = Stroke(width = 1.dp.toPx()))
    }
}

// =====================================================================================================
// Technik-Detektiv
// =====================================================================================================

@Composable
internal fun TechniqueGameScreen(finish: (Int) -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    val round = remember(seed) { TechniqueDetective.round(seed) }
    var index by remember(seed) { mutableIntStateOf(0) }
    var answers by remember(seed) { mutableStateOf(listOf<Boolean>()) }
    var picked by remember(seed) { mutableStateOf<MlTechnique?>(null) }
    var reported by remember(seed) { mutableStateOf(false) }
    val accent = topicColor(Topic.AI)

    GameColumn {
        if (index >= round.size) {
            val score = TechniqueDetective.score(answers)
            LaunchedEffect(seed) { if (!reported) { reported = true; finish(score) } }
            ScoreBanner(score, "${answers.count { it }} von ${round.size} Fällen gelöst. Serien ab drei richtigen geben Bonuspunkte.")
            Button(onClick = { seed = System.currentTimeMillis() }, modifier = Modifier.fillMaxWidth()) { Text("Neue Fälle") }
            return@GameColumn
        }
        val case = round[index]
        Text("Fall ${index + 1} von ${round.size} · Serie ${answers.takeLastWhile { it }.size}", style = MaterialTheme.typography.labelLarge, color = accent)
        SectionCard("Der Fall") { Text(case.scenario, style = MaterialTheme.typography.bodyLarge) }
        Text("Welche Technik passt?", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MlTechnique.entries.forEach { t ->
                val shown = picked != null
                FilterChip(
                    selected = shown && (t == case.answer || t == picked),
                    onClick = {
                        if (picked == null) { picked = t; answers = answers + (t == case.answer) }
                    },
                    label = { Text(t.title + if (shown && t == case.answer) " ✔" else if (shown && t == picked) " ✘" else "") }
                )
            }
        }
        picked?.let { p ->
            Text(if (p == case.answer) "Richtig! ${case.why}" else "Leider nein – gesucht war ${case.answer.title}. ${case.why}",
                style = MaterialTheme.typography.bodyMedium, color = if (p == case.answer) accent else MaterialTheme.colorScheme.error)
            Button(onClick = { index++; picked = null }, modifier = Modifier.fillMaxWidth()) { Text(if (index + 1 < round.size) "Nächster Fall" else "Auswertung") }
        }
    }
}

// =====================================================================================================
// Gradientenabstieg
// =====================================================================================================

@Composable
internal fun GradientGameScreen(finish: (Int) -> Unit) {
    var levelIndex by rememberSaveable { mutableIntStateOf(0) }
    // Learning rate on a log scale: η = 10^s, s ∈ [−2, 0.3] → η ∈ [0.01, 2].
    var etaExp by rememberSaveable { mutableFloatStateOf(-1f) }
    var beta by rememberSaveable { mutableFloatStateOf(0f) }
    var result by remember(levelIndex) { mutableStateOf<Int?>(null) }
    val level = GradientGame.levels[levelIndex]
    val l = level.landscape
    val eta = 10.0.pow(etaExp.toDouble())
    val b = if (level.allowMomentum) beta.toDouble() else 0.0
    val path = remember(levelIndex, eta, b) { GradientGame.run(l, eta, b, level.steps) }
    val end = GradientGame.finalLoss(l, path)
    val accent = topicColor(Topic.AI)
    val curveColor = MaterialTheme.colorScheme.onSurface
    val bad = MaterialTheme.colorScheme.error

    GameColumn {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GradientGame.levels.forEachIndexed { i, lv -> FilterChip(levelIndex == i, { levelIndex = i }, label = { Text("${i + 1}. ${lv.landscape.title}") }) }
        }
        Text(l.hint, style = MaterialTheme.typography.bodySmall)
        GameCanvas {
            val (x0, x1) = l.range
            val xs = (0..200).map { x0 + (x1 - x0) * it / 200.0 }
            val ys = xs.map(l.f)
            val yLo = ys.min(); val yHi = ys.max()
            val pad = 0.08f * size.height
            fun px(x: Double) = ((x - x0) / (x1 - x0) * size.width).toFloat()
            fun py(y: Double) = (pad + (1 - (y - yLo) / (yHi - yLo)) * (size.height - 2 * pad)).toFloat()
            val curve = Path()
            xs.forEachIndexed { i, x -> if (i == 0) curve.moveTo(px(x), py(ys[i])) else curve.lineTo(px(x), py(ys[i])) }
            drawPath(curve, curveColor.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))
            // Minimum marker
            drawLine(accent.copy(alpha = 0.4f), Offset(0f, py(l.minimum)), Offset(size.width, py(l.minimum)),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            clipRect {
                var prev: Offset? = null
                path.forEachIndexed { i, w ->
                    val inRange = w.isFinite() && w in x0..x1
                    val o = Offset(px(w.coerceIn(x0, x1)), py(l.f(w.coerceIn(x0, x1))))
                    prev?.let { drawLine(accent.copy(alpha = 0.6f), it, o, strokeWidth = 1.5.dp.toPx()) }
                    drawCircle(if (inRange) accent else bad, radius = if (i == path.lastIndex) 6.dp.toPx() else 3.5.dp.toPx(), center = o)
                    prev = o
                }
            }
        }
        LabeledSlider("Lernrate η (log)", etaExp, -2f..0.3f, f(eta, 3)) { etaExp = it; result = null }
        if (level.allowMomentum) LabeledSlider("Momentum β", beta, 0f..0.95f, f(beta.toDouble(), 2)) { beta = it; result = null }
        val diverged = !end.isFinite()
        Text(
            if (diverged) "Divergiert! Die Schritte werden immer größer – Lernrate senken."
            else "Nach ${level.steps} Schritten: Verlust ${f(end)} (Ziel ≤ ${f(level.targetLoss)}, Minimum ${f(l.minimum)})",
            style = MaterialTheme.typography.bodyMedium, color = if (diverged) bad else MaterialTheme.colorScheme.onSurface
        )
        Text("Update: v ← βv − η·∂L/∂w, w ← w + v. Ohne Momentum (β = 0) ist das der reine Gradientenabstieg.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Button(onClick = { val s = GradientGame.score(level, path); result = s; finish(s) }, modifier = Modifier.fillMaxWidth()) { Text("Lauf werten") }
        result?.let { ScoreBanner(it, if (it == 100) "Ziel erreicht! Probier das nächste Level." else "Noch nicht im Ziel – verändere η (und β).") }
    }
}

// =====================================================================================================
// Perzeptron
// =====================================================================================================

@Composable
internal fun PerceptronGameScreen(finish: (Int) -> Unit) {
    var xorMode by rememberSaveable { mutableStateOf(false) }
    var seed by rememberSaveable { mutableLongStateOf(7L) }
    val data = remember(xorMode, seed) { if (xorMode) PerceptronGame.xor(seed) else PerceptronGame.separable(seed) }
    var w1 by rememberSaveable { mutableFloatStateOf(1f) }
    var w2 by rememberSaveable { mutableFloatStateOf(0f) }
    var b by rememberSaveable { mutableFloatStateOf(0f) }
    var epochs by rememberSaveable { mutableIntStateOf(0) }
    var result by remember(xorMode, seed) { mutableStateOf<Int?>(null) }
    val p = Perceptron(w1.toDouble(), w2.toDouble(), b.toDouble())
    val acc = p.accuracy(data)
    val c0 = MaterialTheme.colorScheme.tertiary
    val c1 = topicColor(Topic.AI)
    val line = MaterialTheme.colorScheme.onSurface

    GameColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(!xorMode, { xorMode = false; epochs = 0 }, label = { Text("Trennbar") })
            FilterChip(xorMode, { xorMode = true; epochs = 0 }, label = { Text("XOR") })
            FilterChip(false, { seed += 1; epochs = 0 }, label = { Text("Neue Punkte") })
        }
        GameCanvas {
            fun px(x: Double) = ((x + 1) / 2 * size.width).toFloat()
            fun py(y: Double) = ((1 - (y + 1) / 2) * size.height).toFloat()
            // Shade the half-planes on a coarse grid.
            val n = 24
            val cw = size.width / n; val ch = size.height / n
            for (i in 0 until n) for (j in 0 until n) {
                val x = -1 + (i + 0.5) * 2 / n; val y = 1 - (j + 0.5) * 2 / n
                drawRect((if (p.predict(x, y) == 1) c1 else c0).copy(alpha = 0.08f), Offset(i * cw, j * ch), Size(cw, ch))
            }
            // Decision line w1 x + w2 y + b = 0
            if (abs(w2) > 1e-6) {
                val ya = (-b - w1 * -1.0) / w2; val yb = (-b - w1 * 1.0) / w2
                drawLine(line, Offset(px(-1.0), py(ya)), Offset(px(1.0), py(yb)), strokeWidth = 2.dp.toPx())
            } else if (abs(w1) > 1e-6) {
                val x = (-b / w1).toDouble()
                drawLine(line, Offset(px(x), 0f), Offset(px(x), size.height), strokeWidth = 2.dp.toPx())
            }
            for (pt in data) {
                val ok = p.predict(pt.x, pt.y) == pt.label
                val o = Offset(px(pt.x), py(pt.y))
                if (pt.label == 1) drawCircle(c1, 5.dp.toPx(), o) else drawRect(c0, Offset(o.x - 4.dp.toPx(), o.y - 4.dp.toPx()), Size(8.dp.toPx(), 8.dp.toPx()))
                if (!ok) drawCircle(Color.Red, 8.dp.toPx(), o, style = Stroke(width = 1.5.dp.toPx()))
            }
        }
        Text("Genauigkeit ${f(100 * acc, 3)} % · Epochen ${epochs}", style = MaterialTheme.typography.titleSmall)
        LabeledSlider("Gewicht w₁", w1, -2f..2f, f(w1.toDouble(), 2)) { w1 = it; result = null }
        LabeledSlider("Gewicht w₂", w2, -2f..2f, f(w2.toDouble(), 2)) { w2 = it; result = null }
        LabeledSlider("Bias b", b, -2f..2f, f(b.toDouble(), 2)) { b = it; result = null }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val q = PerceptronGame.epoch(p, data, 0.1)
                w1 = q.w1.toFloat().coerceIn(-2f, 2f); w2 = q.w2.toFloat().coerceIn(-2f, 2f); b = q.b.toFloat().coerceIn(-2f, 2f); epochs++
            }, modifier = Modifier.weight(1f)) { Text("1 Epoche lernen") }
            Button(onClick = { val s = PerceptronGame.score(acc); result = s; finish(s) }, modifier = Modifier.weight(1f)) { Text("Werten") }
        }
        Text("Lernregel: Bei jedem falsch klassifizierten Punkt w ← w + η(y − ŷ)x mit η = 0,1. Rote Ringe markieren Fehler." +
            if (xorMode) " Bei XOR gibt es keine trennende Gerade – mehr als etwa 75 % sind unmöglich. Genau deshalb braucht man versteckte Schichten." else "",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        result?.let { ScoreBanner(it, if (it == 100) "Perfekt getrennt!" else "Noch Fehler übrig – Gewichte verändern oder weiter lernen lassen.") }
    }
}

// =====================================================================================================
// k-Means
// =====================================================================================================

@Composable
internal fun KMeansGameScreen(finish: (Int) -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(11L) }
    var k by rememberSaveable { mutableIntStateOf(3) }
    val points = remember(seed) { KMeansGame.blobs(seed, k = 3 + (seed % 2).toInt()) }
    var centers by remember(seed, k) { mutableStateOf(listOf<P2>()) }
    var labels by remember(seed, k) { mutableStateOf<IntArray?>(null) }
    var steps by remember(seed, k) { mutableIntStateOf(0) }
    var result by remember(seed, k) { mutableStateOf<Int?>(null) }
    val best = remember(seed, k) { KMeansGame.bestInertia(points, k) }
    val palette = listOf(topicColor(Topic.AI), MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.secondary, Color(0xFF64B5F6), Color(0xFFE57373))
    val grey = MaterialTheme.colorScheme.outline
    val inertia = labels?.let { if (centers.size == k) KMeansGame.inertia(points, it, centers) else null }

    GameColumn {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("k =", style = MaterialTheme.typography.labelLarge)
            (2..5).forEach { kk -> FilterChip(k == kk, { k = kk }, label = { Text("$kk") }) }
            FilterChip(false, { seed += 1 }, label = { Text("Neue Daten") })
        }
        Text(if (centers.size < k) "Tippe ${k - centers.size} Startzentren in das Bild." else "Wechsle zwischen Zuordnen und Verschieben, bis sich nichts mehr ändert.",
            style = MaterialTheme.typography.bodySmall)
        GameCanvas(onTap = { o, s ->
            val p = P2((o.x / s.width).toDouble().coerceIn(0.0, 1.0), (o.y / s.height).toDouble().coerceIn(0.0, 1.0))
            if (centers.size < k) { centers = centers + p; labels = null; result = null }
        }) {
            for ((i, pt) in points.withIndex()) {
                val c = labels?.get(i)?.let { palette[it % palette.size] } ?: grey
                drawCircle(c, 4.dp.toPx(), Offset((pt.x * size.width).toFloat(), (pt.y * size.height).toFloat()))
            }
            centers.forEachIndexed { i, c ->
                val o = Offset((c.x * size.width).toFloat(), (c.y * size.height).toFloat())
                val col = palette[i % palette.size]
                drawCircle(col, 9.dp.toPx(), o, style = Stroke(width = 3.dp.toPx()))
                drawLine(col, Offset(o.x - 6.dp.toPx(), o.y), Offset(o.x + 6.dp.toPx(), o.y), strokeWidth = 2.dp.toPx())
                drawLine(col, Offset(o.x, o.y - 6.dp.toPx()), Offset(o.x, o.y + 6.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
        }
        Text("Schritte $steps · Inertia ${inertia?.let { f(it, 4) } ?: "–"} · bestes bekanntes Ergebnis ${f(best, 4)}", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(enabled = centers.size == k, onClick = { labels = KMeansGame.assign(points, centers); steps++ }, modifier = Modifier.weight(1f)) { Text("Zuordnen") }
            OutlinedButton(enabled = labels != null, onClick = { centers = KMeansGame.update(points, labels!!, centers); steps++ }, modifier = Modifier.weight(1f)) { Text("Verschieben") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { centers = emptyList(); labels = null; steps = 0; result = null }, modifier = Modifier.weight(1f)) { Text("Neu starten") }
            Button(enabled = inertia != null, onClick = { val s = KMeansGame.score(inertia!!, best); result = s; finish(s) }, modifier = Modifier.weight(1f)) { Text("Werten") }
        }
        Text("Inertia J = Σ |x − μ_k|². Jeder Schritt senkt J oder lässt es gleich. Schlechte Startzentren enden in einem lokalen Minimum – " +
            "das Spiel vergleicht mit dem besten von 30 Zufallsstarts. Probier auch ein falsches k und sieh, wie die Inertia reagiert.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        result?.let { ScoreBanner(it, if (it >= 98) "Optimale Cluster gefunden!" else "Da geht noch was – andere Startpunkte oder anderes k probieren.") }
    }
}

// =====================================================================================================
// Überanpassung
// =====================================================================================================

@Composable
internal fun OverfitGameScreen(finish: (Int) -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(3L) }
    var degree by rememberSaveable { mutableIntStateOf(1) }
    var revealed by remember(seed) { mutableStateOf(false) }
    var result by remember(seed) { mutableStateOf<Int?>(null) }
    val data = remember(seed) { OverfitGame.data(seed) }
    val fit = remember(seed, degree) { OverfitGame.fit(data, degree) }
    val all = remember(seed) { OverfitGame.degrees.map { OverfitGame.fit(data, it) } }
    val accent = topicColor(Topic.AI)
    val train = MaterialTheme.colorScheme.onSurface
    val valC = MaterialTheme.colorScheme.tertiary
    val truth = MaterialTheme.colorScheme.outline

    GameColumn {
        Text("14 verrauschte Trainingspunkte (dunkel), 40 Validierungspunkte (hell). Welcher Polynomgrad sagt NEUE Punkte am besten voraus?",
            style = MaterialTheme.typography.bodySmall)
        ChartFrame("Grad $degree", "maximus-ueberanpassung", height = 260.dp) { hits, m, c ->
            val gx = (0..120).map { -1 + 2.0 * it / 120 }
            drawLineChart(
                hits, m, c,
                listOf(
                    LineSeries("Wahrheit", gx, gx.map(data.truth), truth, dashed = true),
                    LineSeries("Modell", gx, gx.map { fit.eval(it) }, accent),
                    LineSeries("Validierung", data.valX, data.valY, valC, markers = true, connect = false),
                    LineSeries("Training", data.trainX, data.trainY, train, markers = true, connect = false)
                ),
                yRange = -2.5 to 2.5, hitEvery = 4
            )
        }
        LabeledSlider("Polynomgrad", degree.toFloat(), 0f..12f, "$degree") { degree = it.roundToInt(); result = null }
        Text("Trainingsfehler (MSE) ${f(fit.trainMse)} · Validierungsfehler ${f(fit.valMse)}", style = MaterialTheme.typography.titleSmall)
        Button(onClick = { val s = OverfitGame.score(data, degree); result = s; revealed = true; finish(s) }, modifier = Modifier.fillMaxWidth()) {
            Text("Diesen Grad wählen")
        }
        result?.let {
            val best = OverfitGame.bestDegree(data)
            ScoreBanner(it, if (degree == best) "Volltreffer: Grad $best hat den kleinsten Validierungsfehler." else "Der beste Grad war $best. Siehe die U-Kurve unten.")
        }
        if (revealed) {
            ChartFrame("Fehler gegen Grad: die U-Kurve", "maximus-u-kurve", height = 220.dp) { hits, m, c ->
                val ds = all.map { it.degree.toDouble() }
                val cap = 1.5
                drawLineChart(
                    hits, m, c,
                    listOf(
                        LineSeries("Training", ds, all.map { min(cap, it.trainMse) }, train, markers = true),
                        LineSeries("Validierung", ds, all.map { min(cap, it.valMse) }, valC, markers = true)
                    ),
                    yRange = 0.0 to cap
                )
            }
            Text("Der Trainingsfehler sinkt mit jedem Grad, der Validierungsfehler steigt nach einem Minimum wieder: links Underfitting (Bias), rechts Overfitting (Varianz).",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            OutlinedButton(onClick = { seed += 1; degree = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Neue Daten") }
        }
    }
}

// =====================================================================================================
// Schwellen-Regler
// =====================================================================================================

@Composable
internal fun ThresholdGameScreen(finish: (Int) -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(5L) }
    var missionName by rememberSaveable { mutableStateOf(ThresholdMission.SCREENING.name) }
    var threshold by rememberSaveable { mutableFloatStateOf(0.5f) }
    var result by remember(seed, missionName) { mutableStateOf<Int?>(null) }
    val mission = ThresholdMission.valueOf(missionName)
    val s = remember(seed) { ThresholdGame.scores(seed) }
    val c = ThresholdGame.confusion(s, threshold.toDouble())
    val ok = ThresholdGame.objective(mission, c) != null
    val pos = topicColor(Topic.AI)
    val neg = MaterialTheme.colorScheme.tertiary
    val line = MaterialTheme.colorScheme.onSurface

    GameColumn {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ThresholdMission.entries.forEach { m -> FilterChip(mission == m, { missionName = m.name }, label = { Text(m.title) }) }
        }
        Text(mission.description, style = MaterialTheme.typography.bodyMedium)
        GameCanvas(Modifier.height(200.dp), square = false) {
            val bins = 25
            val hp = IntArray(bins); val hn = IntArray(bins)
            s.positives.forEach { hp[min(bins - 1, (it * bins).toInt())]++ }
            s.negatives.forEach { hn[min(bins - 1, (it * bins).toInt())]++ }
            val maxC = max(1, max(hp.max(), hn.max()))
            val bw = size.width / bins
            val top = 8.dp.toPx()
            for (i in 0 until bins) {
                val hN = (size.height - top) * hn[i] / maxC; val hP = (size.height - top) * hp[i] / maxC
                drawRect(neg.copy(alpha = 0.55f), Offset(i * bw + 1, size.height - hN), Size(bw / 2 - 1, hN))
                drawRect(pos.copy(alpha = 0.8f), Offset(i * bw + bw / 2, size.height - hP), Size(bw / 2 - 1, hP))
            }
            val tx = threshold * size.width
            drawLine(line, Offset(tx, 0f), Offset(tx, size.height), strokeWidth = 2.5.dp.toPx())
        }
        Text("Balken: Scores der Negativen (hell) und Positiven (kräftig). Alles rechts der Linie wird als positiv gemeldet.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        LabeledSlider("Schwelle", threshold, 0f..1f, f(threshold.toDouble(), 2)) { threshold = (it * 100).roundToInt() / 100f; result = null }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ConfusionCell("TP", c.tp, pos, Modifier.weight(1f)); ConfusionCell("FP", c.fp, MaterialTheme.colorScheme.error, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ConfusionCell("FN", c.fn, MaterialTheme.colorScheme.error, Modifier.weight(1f)); ConfusionCell("TN", c.tn, neg, Modifier.weight(1f))
        }
        Text("Precision ${f(c.precision)} · Recall ${f(c.recall)} · F1 ${f(c.f1)} · Accuracy ${f(c.accuracy)}", style = MaterialTheme.typography.titleSmall)
        Text(if (ok) "Bedingung der Mission erfüllt." else "Bedingung der Mission noch nicht erfüllt.",
            color = if (ok) pos else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = { val sc = ThresholdGame.score(s, mission, threshold.toDouble()); result = sc; finish(sc) }, modifier = Modifier.fillMaxWidth()) {
            Text("Schwelle festlegen")
        }
        result?.let { ScoreBanner(it, if (it == 100) "Optimal! Besser geht es mit diesem Modell nicht." else "Gültig, aber nicht optimal – oder Bedingung verletzt.") }
        OutlinedButton(onClick = { seed += 1 }, modifier = Modifier.fillMaxWidth()) { Text("Neues Modell") }
    }
}

@Composable
private fun ConfusionCell(label: String, value: Int, color: Color, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(1.dp, color.copy(alpha = 0.6f)), modifier = modifier) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("$value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// =====================================================================================================
// Baumeister
// =====================================================================================================

@Composable
internal fun TreeGameScreen(finish: (Int) -> Unit) {
    var root by rememberSaveable { mutableStateOf<Int?>(null) }
    var childChoices by remember(root) { mutableStateOf(mapOf<String, Int>()) }
    var reported by remember(root) { mutableStateOf(false) }
    val rows = TreeGame.rows
    val allF = TreeGame.features.indices.toList()
    val accent = topicColor(Topic.AI)

    GameColumn {
        SectionCard("Daten: Heute schwer trainieren?") {
            Row {
                (TreeGame.features + "Ja?").forEach { h -> Text(h, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
            }
            rows.forEach { (fs, y) ->
                Row {
                    fs.forEach { v -> Text(v, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall) }
                    Text(if (y) "ja" else "nein", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = if (y) accent else MaterialTheme.colorScheme.error)
                }
            }
            Text("Entropie der Wurzel: ${f(TreeGame.entropy(rows.map { it.second }))} Bit (${rows.count { it.second }} ja / ${rows.count { !it.second }} nein)",
                style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
        }
        Text("Schritt 1: Welche Frage stellst du an der Wurzel?", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            allF.forEach { i -> FilterChip(root == i, { if (root == null) root = i }, label = { Text(TreeGame.features[i]) }) }
        }
        val r = root ?: return@GameColumn
        val gains = TreeGame.gains(rows, allF)
        GainList(gains, r)
        val rootScore = TreeGame.score(rows, allF, r)
        val children = rows.groupBy { it.first[r] }
        val impure = children.filter { (_, g) -> g.any { it.second } && g.any { !it.second } }
        Text("Schritt 2: Für jeden noch gemischten Ast die beste nächste Frage.", style = MaterialTheme.typography.titleSmall)
        children.forEach { (value, g) ->
            val yes = g.count { it.second }; val no = g.size - yes
            SectionCard("${TreeGame.features[r]} = $value  ($yes ja / $no nein)") {
                if (value !in impure) {
                    Text("Rein – Blatt: „${if (yes > 0) "ja" else "nein"}“.", style = MaterialTheme.typography.bodyMedium, color = accent)
                } else {
                    val rest = allF - r
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rest.forEach { i -> FilterChip(childChoices[value] == i, { if (value !in childChoices) childChoices = childChoices + (value to i) }, label = { Text(TreeGame.features[i]) }) }
                    }
                    childChoices[value]?.let { GainList(TreeGame.gains(g, rest), it) }
                }
            }
        }
        if (impure.keys.all { it in childChoices }) {
            val scores = listOf(rootScore) + impure.map { (v, g) -> TreeGame.score(g, allF - r, childChoices.getValue(v)) }
            val total = scores.average().roundToInt()
            LaunchedEffect(root, childChoices) { if (!reported) { reported = true; finish(total) } }
            ScoreBanner(total, "Ein Entscheidungsbaum wählt gierig an jedem Knoten die Frage mit dem größten Informationsgewinn IG = H(Eltern) − Σ (n_k/n)·H(Kind k).")
            OutlinedButton(onClick = { root = null }, modifier = Modifier.fillMaxWidth()) { Text("Nochmal bauen") }
        }
    }
}

@Composable
private fun GainList(gains: Map<Int, Double>, chosen: Int) {
    val best = gains.maxBy { it.value }.key
    val accent = topicColor(Topic.AI)
    Column {
        gains.entries.sortedByDescending { it.value }.forEach { (i, g) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(TreeGame.features[i] + (if (i == chosen) " (deine Wahl)" else ""), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (i == chosen) FontWeight.Bold else FontWeight.Normal)
                Text("IG ${f(g)} Bit" + if (i == best) " ★" else "", style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                    color = if (i == best) accent else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// =====================================================================================================
// Q-Learning-Labyrinth
// =====================================================================================================

@Composable
internal fun QLearnGameScreen(finish: (Int) -> Unit) {
    var alpha by rememberSaveable { mutableFloatStateOf(0.5f) }
    var gamma by rememberSaveable { mutableFloatStateOf(0.9f) }
    var eps by rememberSaveable { mutableFloatStateOf(0.2f) }
    var generation by rememberSaveable { mutableIntStateOf(0) }
    val world = remember { GridWorld() }
    val learner = remember(generation) { QLearner(world, alpha.toDouble(), gamma.toDouble(), eps.toDouble(), seed = generation.toLong() + 1) }
    var version by remember(generation) { mutableIntStateOf(0) }
    var result by remember(generation) { mutableStateOf<Int?>(null) }
    val accent = topicColor(Topic.AI)
    val wallC = MaterialTheme.colorScheme.outline
    val trapC = MaterialTheme.colorScheme.error
    val text = MaterialTheme.colorScheme.onSurface

    GameColumn {
        Text("Der Agent startet unten links und sucht den Schatz oben rechts (+10). Fallen kosten −10, jeder Schritt −1, Mauern blockieren.",
            style = MaterialTheme.typography.bodySmall)
        val episodes = version.let { learner.episodeReturns.size }
        GameCanvas {
            if (version < 0) return@GameCanvas // reading the tick redraws the grid after every training burst
            val cw = size.width / world.cols; val ch = size.height / world.rows
            val maxAbs = learner.q.maxOf { row -> row.maxOf { abs(it) } }.coerceAtLeast(1e-9)
            for (r in 0 until world.rows) for (c in 0 until world.cols) {
                val s = r to c
                val tl = Offset(c * cw, r * ch)
                val cell = Size(cw - 2, ch - 2)
                when {
                    s in world.walls -> drawRect(wallC, tl, cell)
                    s in world.traps -> drawRect(trapC.copy(alpha = 0.5f), tl, cell)
                    s == world.goal -> drawRect(accent.copy(alpha = 0.6f), tl, cell)
                    else -> {
                        val v = learner.q[world.index(s)].max()
                        drawRect((if (v >= 0) accent else trapC).copy(alpha = (0.05 + 0.35 * min(1.0, abs(v) / maxAbs)).toFloat()), tl, cell)
                        if (learner.episodeReturns.isNotEmpty()) {
                            val m = learner.best(s)
                            val cx = tl.x + cw / 2; val cy = tl.y + ch / 2
                            val len = min(cw, ch) * 0.3f
                            val end = Offset(cx + m.dc * len, cy + m.dr * len)
                            drawLine(text, Offset(cx - m.dc * len * 0.6f, cy - m.dr * len * 0.6f), end, strokeWidth = 2.dp.toPx())
                            drawCircle(text, 3.dp.toPx(), end)
                        }
                    }
                }
                if (s == world.start) drawCircle(text, min(cw, ch) * 0.12f, Offset(tl.x + cw * 0.2f, tl.y + ch * 0.8f))
            }
        }
        Text("Episoden $episodes · letzter Return ${learner.episodeReturns.lastOrNull()?.let { f(it, 3) } ?: "–"}",
            style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { learner.episode(); version++ }, modifier = Modifier.weight(1f)) { Text("+1 Episode") }
            OutlinedButton(onClick = { repeat(50) { learner.episode() }; version++ }, modifier = Modifier.weight(1f)) { Text("+50") }
            OutlinedButton(onClick = { repeat(300) { learner.episode() }; version++ }, modifier = Modifier.weight(1f)) { Text("+300") }
        }
        if (episodes >= 2) {
            val rs = learner.episodeReturns
            val window = 10
            val avg = rs.indices.map { i -> rs.subList(max(0, i - window + 1), i + 1).average() }
            ChartFrame("Return pro Episode (gleitendes Mittel über $window)", "maximus-q-learning", height = 180.dp) { hits, m, c ->
                drawLineChart(hits, m, c, listOf(LineSeries("Return", rs.indices.map { it + 1.0 }, avg, accent)), hitEvery = max(1, rs.size / 40))
            }
        }
        LabeledSlider("Lernrate α", alpha, 0.05f..1f, f(alpha.toDouble(), 2)) { alpha = it }
        LabeledSlider("Diskont γ", gamma, 0.5f..0.99f, f(gamma.toDouble(), 2)) { gamma = it }
        LabeledSlider("Erkundung ε", eps, 0f..0.6f, f(eps.toDouble(), 2)) { eps = it }
        Text("Neue Parameter gelten nach „Neu trainieren“. Update: Q(s,a) ← Q(s,a) + α[r + γ·max Q(s′,·) − Q(s,a)]. Pfeile zeigen die aktuell beste Aktion.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { generation++ }, modifier = Modifier.weight(1f)) { Text("Neu trainieren") }
            Button(onClick = { val s = QLearner.score(learner); result = s; finish(s) }, modifier = Modifier.weight(1f)) { Text("Strategie werten") }
        }
        result?.let {
            val g = learner.greedyReturn()
            ScoreBanner(it, if (g == null) "Die gierige Strategie erreicht den Schatz noch nicht – mehr Episoden!"
            else "Gierige Strategie: Return ${f(g, 3)}, optimal wären ${f(QLearner.optimalReturn(world), 3)}.")
        }
    }
}

// =====================================================================================================
// Begriffe-Duell
// =====================================================================================================

@Composable
internal fun GlossaryDuelScreen(finish: (Int) -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    val round = remember(seed) { GlossaryDuel.round(seed) }
    var index by remember(seed) { mutableIntStateOf(0) }
    var points by remember(seed) { mutableIntStateOf(0) }
    var correct by remember(seed) { mutableIntStateOf(0) }
    var shownAt by remember(seed, index) { mutableLongStateOf(System.currentTimeMillis()) }
    var picked by remember(seed, index) { mutableStateOf<Int?>(null) }
    var reported by remember(seed) { mutableStateOf(false) }
    val accent = topicColor(Topic.AI)
    val maxPoints = round.size * 12

    GameColumn {
        if (index >= round.size) {
            val score = (100 * points / maxPoints).coerceIn(0, 100)
            LaunchedEffect(seed) { if (!reported) { reported = true; finish(score) } }
            ScoreBanner(score, "$correct von ${round.size} richtig, $points Punkte. Schnelle Antworten (unter 3 s) geben Bonus.")
            Button(onClick = { seed = System.currentTimeMillis() }, modifier = Modifier.fillMaxWidth()) { Text("Neues Duell") }
            Text("Alle ${AiGlossary.terms.size} Begriffe findest du im Glossar der Spielwiese.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline)
            return@GameColumn
        }
        val card = round[index]
        Text("Begriff ${index + 1}/${round.size} · $points Punkte", style = MaterialTheme.typography.labelLarge, color = accent)
        Box(Modifier.fillMaxWidth().background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Text(card.term.term, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
        card.options.forEachIndexed { i, opt ->
            val state = picked
            val bg = when {
                state == null -> MaterialTheme.colorScheme.surfaceContainerHigh
                i == card.correct -> accent.copy(alpha = 0.25f)
                i == state -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surfaceContainerHigh
            }
            Surface(
                shape = RoundedCornerShape(10.dp), color = bg, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                onClick = {
                    if (picked == null) {
                        picked = i
                        val ok = i == card.correct
                        if (ok) correct++
                        points += GlossaryDuel.points(ok, System.currentTimeMillis() - shownAt)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp)) }
        }
        if (picked != null) Button(onClick = { index++ }, modifier = Modifier.fillMaxWidth()) { Text(if (index + 1 < round.size) "Weiter" else "Auswertung") }
    }
}

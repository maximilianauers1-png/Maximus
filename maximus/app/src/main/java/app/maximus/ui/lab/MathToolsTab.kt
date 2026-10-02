@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Expr
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.MathWorkbench
import app.maximus.lab.domain.Stats
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.LineSeries
import app.maximus.ui.charts.drawLineChart
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard

private val TOOLS = listOf("Funktionsplotter", "Gleichung lösen", "Matrizen", "Statistik", "Differentialgleichung")

@Composable
fun MathToolsTab(@Suppress("UNUSED_PARAMETER") ctx: LabContext) {
    var tool by rememberSaveable { mutableIntStateOf(0) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TOOLS.forEachIndexed { i, t -> FilterChip(tool == i, { tool = i }, label = { Text(t) }) }
            }
        }
        item {
            when (tool) {
                0 -> PlotterTool()
                1 -> SolverTool()
                2 -> MatrixTool()
                3 -> StatisticsTool()
                else -> OdeTool()
            }
        }
        item {
            SectionCard("Syntax") {
                Text(
                    "Operatoren + − * / ^ und !, implizite Multiplikation (2x, 3(x+1), x sin x), |x| für Beträge, Dezimalkomma oder -punkt. " +
                        "Funktionen: sin cos tan asin acos atan sinh cosh tanh exp ln log log2 sqrt cbrt abs floor ceil round sign min max gamma erf sinc heaviside besselj0. " +
                        "Konstanten: pi, e, tau, phi.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ExprField(label: String, value: String, error: String?, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, onChange, label = { Text(label) }, singleLine = true, isError = error != null,
        supportingText = error?.let { { Text(it) } },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}

@Composable
private fun NumberRow(a: String, b: String, labelA: String, labelB: String, onA: (String) -> Unit, onB: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(a, onA, label = { Text(labelA) }, singleLine = true, isError = Fmt.parse(a) == null, modifier = Modifier.weight(1f))
        OutlinedTextField(b, onB, label = { Text(labelB) }, singleLine = true, isError = Fmt.parse(b) == null, modifier = Modifier.weight(1f))
    }
}

private val PLOT_EXAMPLES = listOf("sin(x)/x", "x^3 - 3x", "exp(-x^2)", "tan(x)", "gamma(x)", "besselj0(x)", "1/(1+x^2)", "x sin(1/x)")

@Composable
private fun PlotterTool() {
    var f1 by rememberSaveable { mutableStateOf("sin(x)/x") }
    var f2 by rememberSaveable { mutableStateOf("") }
    var f3 by rememberSaveable { mutableStateOf("") }
    var a by rememberSaveable { mutableStateOf("-10") }
    var b by rememberSaveable { mutableStateOf("10") }
    var x0 by rememberSaveable { mutableStateOf("1") }
    val c1 = remember(f1) { if (f1.isBlank()) null else MathWorkbench.compile(f1, setOf("x")) }
    val c2 = remember(f2) { if (f2.isBlank()) null else MathWorkbench.compile(f2, setOf("x")) }
    val c3 = remember(f3) { if (f3.isBlank()) null else MathWorkbench.compile(f3, setOf("x")) }
    val lo = Fmt.parse(a) ?: -10.0
    val hi = (Fmt.parse(b) ?: 10.0).let { if (it > lo) it else lo + 1 }
    val exprs = listOfNotNull(c1?.getOrNull(), c2?.getOrNull(), c3?.getOrNull())
    val samples = remember(f1, f2, f3, lo, hi) { exprs.map { MathWorkbench.sample(it, lo, hi, 500) } }
    val analysis = remember(f1, lo, hi) { c1?.getOrNull()?.let { MathWorkbench.analyze(it, lo, hi) } }
    val cs = MaterialTheme.colorScheme
    val colors = listOf(cs.primary, cs.secondary, cs.tertiary)

    SectionCard("Funktionsplotter") {
        ExprField("f₁(x)", f1, c1?.exceptionOrNull()?.message) { f1 = it }
        ExprField("f₂(x) (optional)", f2, c2?.exceptionOrNull()?.message) { f2 = it }
        ExprField("f₃(x) (optional)", f3, c3?.exceptionOrNull()?.message) { f3 = it }
        NumberRow(a, b, "x von", "x bis", { a = it }, { b = it })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PLOT_EXAMPLES.forEach { ex -> AssistChip(onClick = { f1 = ex }, label = { Text(ex, fontFamily = FontFamily.Monospace) }) }
        }
        if (samples.isNotEmpty()) {
            val range = MathWorkbench.robustRange(samples.flatMap { it.ys })
            ChartFrame("Graph", "maximus-funktionsplot", modifier = Modifier.padding(top = 8.dp), height = 280.dp) { hits, m, c ->
                drawLineChart(
                    hits, m, c,
                    samples.mapIndexed { i, s -> LineSeries("f${i + 1}", s.xs, s.ys, colors[i]) },
                    yRange = range, hitEvery = 8
                )
            }
        }
        analysis?.let { an ->
            Text("Analyse von f₁ auf [${Fmt.num(lo)}, ${Fmt.num(hi)}]", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            ResultLine("Nullstellen", if (an.roots.isEmpty()) "keine (Vorzeichenwechsel)" else an.roots.joinToString("; ") { Fmt.num(it, 6) })
            an.extrema.forEach { (x, y, isMax) -> ResultLine(if (isMax) "Maximum" else "Minimum", "x = ${Fmt.num(x, 6)}, f = ${Fmt.num(y, 6)}") }
            ResultLine("∫ f₁ dx", an.integral?.let { Fmt.num(it, 8) } ?: "divergiert/undefiniert")
            OutlinedTextField(x0, { x0 = it }, label = { Text("Stelle x₀ für Ableitungen") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            val e1 = c1?.getOrNull()
            val xv = Fmt.parse(x0)
            if (e1 != null && xv != null) {
                val f = { x: Double -> runCatching { e1.eval(mapOf("x" to x)) }.getOrDefault(Double.NaN) }
                ResultLine("f₁(x₀)", Fmt.num(f(xv), 8))
                ResultLine("f₁′(x₀)", Fmt.num(app.maximus.lab.domain.Calculus.derivative(f, xv), 8))
                ResultLine("f₁″(x₀)", Fmt.num(app.maximus.lab.domain.Calculus.secondDerivative(f, xv), 6))
                val tay = app.maximus.lab.domain.Calculus.taylor(f, xv, 3)
                ResultLine("Taylor bis Ordnung 3", tay.mapIndexed { k, c -> "${Fmt.num(c, 4)}·h^$k" }.joinToString(" + "))
            }
            Text("Nullstellen über Vorzeichenwechsel + Brent, Extrema über f′ = 0, Integral adaptiv nach Simpson, Ableitungen per Richardson-Extrapolation.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SolverTool() {
    var left by rememberSaveable { mutableStateOf("cos(x)") }
    var right by rememberSaveable { mutableStateOf("x") }
    var a by rememberSaveable { mutableStateOf("-5") }
    var b by rememberSaveable { mutableStateOf("5") }
    val cl = remember(left) { MathWorkbench.compile(left, setOf("x")) }
    val cr = remember(right) { MathWorkbench.compile(right, setOf("x")) }
    val lo = Fmt.parse(a) ?: -5.0
    val hi = (Fmt.parse(b) ?: 5.0).let { if (it > lo) it else lo + 1 }
    SectionCard("Gleichung lösen") {
        ExprField("Linke Seite", left, cl.exceptionOrNull()?.message) { left = it }
        ExprField("Rechte Seite", right, cr.exceptionOrNull()?.message) { right = it }
        NumberRow(a, b, "Suche von", "bis", { a = it }, { b = it })
        val l = cl.getOrNull(); val r = cr.getOrNull()
        if (l != null && r != null) {
            val diff = remember(left, right) { Expr.Bin('-', l, r) }
            val an = remember(left, right, lo, hi) { MathWorkbench.analyze(diff, lo, hi) }
            val s = remember(left, right, lo, hi) { MathWorkbench.sample(diff, lo, hi, 400) }
            ResultLine("Lösungen", if (an.roots.isEmpty()) "keine im Intervall" else "")
            an.roots.forEach { x -> ResultLine("x", Fmt.num(x, 10)) }
            val primary = MaterialTheme.colorScheme.primary
            ChartFrame("Differenz links − rechts", "maximus-gleichung", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                drawLineChart(hits, m, c, listOf(LineSeries("Differenz", s.xs, s.ys, primary)), yRange = MathWorkbench.robustRange(s.ys), hitEvery = 8)
            }
            Text("Gelöst wird links − rechts = 0 mit Brent-Verfahren zwischen Vorzeichenwechseln (doppelte Nullstellen ohne Vorzeichenwechsel findet der Plotter als Extremum).",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MatrixTool() {
    var text by rememberSaveable { mutableStateOf("4 1 2\n1 3 0\n2 0 5") }
    var rhs by rememberSaveable { mutableStateOf("1 2 3") }
    val parsed = remember(text) { MathWorkbench.parseMatrix(text) }
    SectionCard("Matrizen") {
        OutlinedTextField(
            text, { text = it }, label = { Text("Matrix (Zeilen untereinander, Einträge mit Leerzeichen)") }, minLines = 3,
            isError = parsed.isFailure, supportingText = parsed.exceptionOrNull()?.let { e -> { Text(e.message ?: "Fehler") } },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace), modifier = Modifier.fillMaxWidth()
        )
        val m = parsed.getOrNull() ?: return@SectionCard
        ResultLine("Größe", "${m.rows} × ${m.cols}")
        ResultLine("Rang", "${m.rank()}")
        if (m.rows == m.cols) {
            ResultLine("Determinante", Fmt.num(m.determinant(), 8))
            ResultLine("Spur", Fmt.num(m.trace(), 8))
            val ev = remember(text) { m.eigenvalues() }
            ev.forEachIndexed { i, e -> ResultLine("λ${i + 1}", e.format(6)) }
            val inv = remember(text) { m.inverse() }
            Text("Inverse", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
            if (inv == null) Text("existiert nicht (singulär)", color = MaterialTheme.colorScheme.tertiary)
            else Text(
                (0 until inv.rows).joinToString("\n") { r -> (0 until inv.cols).joinToString("   ") { Fmt.num(inv[r, it], 5) } },
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
            )
            OutlinedTextField(rhs, { rhs = it }, label = { Text("Rechte Seite b für Ax = b") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            val b = rhs.split(Regex("[\\s;]+")).mapNotNull { Fmt.parse(it) }
            if (b.size == m.rows) {
                val x = m.solve(b.toDoubleArray())
                ResultLine("Lösung x", x?.joinToString("; ") { Fmt.num(it, 6) } ?: "keine eindeutige Lösung")
            }
        }
        Text("LU mit Spaltenpivotisierung, Rang per Gauß-Elimination, Eigenwerte per Hessenberg + Francis-QR (komplexe Paare möglich).",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatisticsTool() {
    var text by rememberSaveable { mutableStateOf("1 2,1\n2 3,9\n3 6,2\n4 7,8\n5 10,1\n6 12,2") }
    val (values, pairs) = remember(text) { MathWorkbench.parseData(text) }
    SectionCard("Statistik und Regression") {
        OutlinedTextField(
            text, { text = it }, label = { Text("Messwerte (eine Spalte) oder Paare x y je Zeile") }, minLines = 4,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace), modifier = Modifier.fillMaxWidth()
        )
        if (pairs != null) {
            val xs = pairs.map { it.first }; val ys = pairs.map { it.second }
            val fit = Stats.linearFit(xs, ys)
            ResultLine("Punkte", "${pairs.size}")
            if (fit != null) {
                ResultLine("Steigung b", "${Fmt.num(fit.slope, 6)} ± ${Fmt.num(fit.slopeErr, 3)}")
                ResultLine("Achsenabschnitt a", "${Fmt.num(fit.intercept, 6)} ± ${Fmt.num(fit.interceptErr, 3)}")
                ResultLine("R²", Fmt.num(fit.r2, 6))
                val primary = MaterialTheme.colorScheme.primary; val secondary = MaterialTheme.colorScheme.secondary
                val lo = xs.minOrNull() ?: 0.0; val hi = xs.maxOrNull() ?: 1.0
                ChartFrame("Messpunkte und Ausgleichsgerade", "maximus-regression", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                    drawLineChart(hits, m, c, listOf(
                        LineSeries("Messwerte", xs, ys, primary, markers = true, connect = false),
                        LineSeries("y = a + bx", listOf(lo, hi), listOf(fit.intercept + fit.slope * lo, fit.intercept + fit.slope * hi), secondary)
                    ))
                }
            } else Text("Für eine Regression braucht es mindestens drei Punkte mit verschiedenen x.", color = MaterialTheme.colorScheme.tertiary)
        } else if (values.isNotEmpty()) {
            ResultLine("Anzahl n", "${values.size}")
            ResultLine("Mittelwert", Fmt.num(Stats.mean(values), 8))
            ResultLine("Median", Fmt.num(Stats.median(values), 8))
            ResultLine("Standardabweichung s", Fmt.num(Stats.std(values), 6))
            ResultLine("Standardfehler s/√n", Fmt.num(Stats.std(values) / kotlin.math.sqrt(values.size.toDouble()), 6))
            ResultLine("Minimum / Maximum", "${Fmt.num(values.min())} / ${Fmt.num(values.max())}")
            ResultLine("Summe", Fmt.num(values.sum(), 8))
        }
        Text("Kleinste Quadrate mit Standardfehlern der Parameter; Varianz nach Welford (numerisch stabil, Bessel-Korrektur n − 1).",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OdeTool() {
    var order by rememberSaveable { mutableIntStateOf(2) }
    var expr by rememberSaveable { mutableStateOf("-0.2v - sin(y)") }
    var y0 by rememberSaveable { mutableStateOf("2") }
    var v0 by rememberSaveable { mutableStateOf("0") }
    var t1 by rememberSaveable { mutableStateOf("30") }
    val vars = if (order == 1) setOf("t", "y") else setOf("t", "y", "v")
    val compiled = remember(expr, order) { MathWorkbench.compile(expr, vars) }
    SectionCard("Differentialgleichung (RK4)") {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(order == 1, { order = 1; expr = "-0.5y + sin(t)" }, label = { Text("y′ = f(t, y)") })
            FilterChip(order == 2, { order = 2; expr = "-0.2v - sin(y)" }, label = { Text("y″ = f(t, y, v)") })
        }
        ExprField(if (order == 1) "y′ =" else "y″ =  (v = y′)", expr, compiled.exceptionOrNull()?.message) { expr = it }
        NumberRow(y0, if (order == 2) v0 else t1, "y(0)", if (order == 2) "y′(0)" else "bis t", { y0 = it }, { if (order == 2) v0 = it else t1 = it })
        if (order == 2) OutlinedTextField(t1, { t1 = it }, label = { Text("bis t") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        val e = compiled.getOrNull()
        val tEnd = (Fmt.parse(t1) ?: 10.0).coerceIn(0.01, 1e4)
        if (e != null) {
            val sol = remember(expr, order, y0, v0, t1) {
                runCatching { MathWorkbench.solveOde(e, order, Fmt.parse(y0) ?: 0.0, Fmt.parse(v0) ?: 0.0, 0.0, tEnd, 1500) }.getOrNull()
            }
            if (sol == null) {
                Text("Die Lösung ist nicht auswertbar (Division durch null oder Überlauf).", color = MaterialTheme.colorScheme.tertiary)
            } else {
                val primary = MaterialTheme.colorScheme.primary; val secondary = MaterialTheme.colorScheme.secondary
                ResultLine("y(t_end)", Fmt.num(sol.y.last()[0], 8))
                ChartFrame("Lösung", "maximus-dgl", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                    drawLineChart(hits, m, c, listOfNotNull(
                        LineSeries("y(t)", sol.t, sol.y.map { it[0] }, primary),
                        if (order == 2) LineSeries("y′(t)", sol.t, sol.y.map { it[1] }, secondary, dashed = true) else null
                    ), yRange = MathWorkbench.robustRange(sol.y.flatMap { it.toList() }), hitEvery = 25)
                }
                if (order == 2) {
                    ChartFrame("Phasenraum (y, y′)", "maximus-phasenraum", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                        drawLineChart(hits, m, c, listOf(LineSeries("Bahn", sol.y.map { it[0] }, sol.y.map { it[1] }, primary)), hitEvery = 25)
                    }
                }
            }
        }
        Text("Beispiele: gedämpftes Pendel −0.2v − sin(y); Van-der-Pol 2(1 − y²)v − y; logistisch y(1 − y) (1. Ordnung).",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

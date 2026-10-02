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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.CalcResult
import app.maximus.lab.domain.Calculator
import app.maximus.lab.domain.Calculators
import app.maximus.lab.domain.Curve
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.LabRules
import app.maximus.lab.domain.Param
import app.maximus.lab.domain.Topic
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.LineSeries
import app.maximus.ui.charts.drawLineChart
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CalculatorsTab(ctx: LabContext) {
    var topic by rememberSaveable { mutableStateOf<Topic?>(null) }
    val list = remember(topic) { topic?.let { Calculators.forTopic(it) } ?: Calculators.all }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { TopicChips(topic, { topic = it }) }
        item {
            Text("${list.size} Rechner und Simulationen. Ein neu benutzter Rechner bringt 5 XP.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(list, key = { it.key }) { c ->
            val used = c.key in ctx.progress.usedCalculators
            PlateCard(onClick = { ctx.openCalculator(c.key) }) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.topic.title, style = MaterialTheme.typography.labelMedium, color = topicColor(c.topic), modifier = Modifier.weight(1f))
                        if (used) Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(c.title, style = MaterialTheme.typography.titleLarge)
                    Text(c.formula, style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }
    }
}

/** Text shown in an input field for a default value: integers without decimals, decimal comma, "1e-12" style exponents. */
private fun editText(v: Double): String = when {
    v == Math.rint(v) && kotlin.math.abs(v) < 1e9 -> v.toLong().toString()
    kotlin.math.abs(v) >= 1e6 || kotlin.math.abs(v) < 1e-3 ->
        String.format(java.util.Locale.US, "%.4e", v).replace("e+", "e").replace(Regex("e(-?)0+(\\d)"), "e$1$2").replace(Regex("\\.?0+e"), "e")
    else -> v.toString().replace('.', ',')
}

private fun seriesColors(primary: Color, secondary: Color, tertiary: Color) =
    listOf(primary, secondary, tertiary, Color(0xFFD9A066), Color(0xFF8CC7B3), Color(0xFFB3A2E0))

@Composable
fun CalculatorScreen(ctx: LabContext, key: String) {
    val calc = Calculators.byKey[key] ?: run { EmptyState("Rechner nicht gefunden."); return }
    val texts = remember(key) { mutableStateMapOf<String, String>().apply { calc.params.forEach { put(it.key, editText(it.default)) } } }
    val parsed: Map<String, Double> = calc.params.associate { p -> p.key to (Fmt.parse(texts[p.key] ?: "") ?: p.default) }
    val invalid = calc.params.filter { p -> p.choices == null && Fmt.parse(texts[p.key] ?: "") == null }
    val result by produceState<CalcResult?>(null, parsed) { value = withContext(Dispatchers.Default) { calc.run(parsed) } }

    LaunchedEffect(key) { ctx.act { p -> LabRules.calculatorUsed(p, key, ctx.today) } }

    val accent = topicColor(calc.topic)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column {
                Text(calc.topic.title, style = MaterialTheme.typography.labelLarge, color = accent)
                Text(calc.description, style = MaterialTheme.typography.bodyMedium)
                FormulaBox(calc.formula, Modifier.padding(top = 8.dp), accent = accent)
            }
        }
        item {
            SectionCard("Eingaben") {
                calc.params.forEach { p -> ParamInput(p, texts[p.key] ?: "", accent) { texts[p.key] = it } }
                if (invalid.isNotEmpty()) {
                    Text("Ungültige Zahl in: ${invalid.joinToString { it.label }} — es wird der Standardwert verwendet.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
                TextButton(onClick = { calc.params.forEach { texts[it.key] = editText(it.default) } }) { Text("Standardwerte") }
            }
        }
        val r = result
        if (r == null) {
            item { CircularProgressIndicator(Modifier.padding(16.dp)) }
        } else {
            if (r.warnings.isNotEmpty()) {
                item {
                    SectionCard("Hinweise") { r.warnings.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary) } }
                }
            }
            if (r.outputs.isNotEmpty()) {
                item {
                    SectionCard("Ergebnisse") {
                        r.outputs.forEach { o ->
                            ResultLine(o.label, o.text)
                            if (o.note.isNotBlank()) Text(o.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            items(r.curves) { curve -> CurveCard(curve, calc.key) }
            if (r.steps.isNotEmpty()) {
                item {
                    SectionCard("Rechenweg") { r.steps.forEach { FormulaBox(it, accent = accent) } }
                }
            }
        }
    }
}

@Composable
private fun ParamInput(p: Param, text: String, accent: Color, onChange: (String) -> Unit) {
    val choices = p.choices
    if (choices != null) {
        val selected = (Fmt.parse(text) ?: p.default).toInt()
        Text(p.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            choices.forEachIndexed { i, label -> FilterChip(selected == i, { onChange(i.toString()) }, label = { Text(label) }) }
        }
    } else {
        val v = Fmt.parse(text)
        val outOfRange = v != null && (v < p.min || v > p.max)
        OutlinedTextField(
            value = text, onValueChange = onChange, singleLine = true,
            label = { Text(p.label) },
            suffix = if (p.unit.isNotBlank()) ({ Text(p.unit) }) else null,
            isError = v == null || outOfRange,
            supportingText = when {
                v == null -> ({ Text("Zahl eingeben, z. B. 1,5 oder 2e-3") })
                outOfRange -> ({ Text("Erlaubt: ${Fmt.num(p.min)} … ${Fmt.num(p.max)} (wird begrenzt)") })
                p.hint.isNotBlank() -> ({ Text(p.hint, color = accent) })
                else -> null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )
    }
}

@Composable
fun CurveCard(curve: Curve, exportKey: String) {
    val cs = MaterialTheme.colorScheme
    val colors = seriesColors(cs.primary, cs.secondary, cs.tertiary)
    Column(Modifier.fillMaxWidth()) {
    ChartFrame(curve.title, "maximus-labor-$exportKey", height = 260.dp) { hits, m, c ->
        drawLineChart(
            hits, m, c,
            curve.series.mapIndexed { i, s -> LineSeries(s.name, s.xs, s.ys, colors[i % colors.size], dashed = s.dashed) },
            yRange = curve.yRange,
            tooltip = { s, i -> "${s.name}: ${curve.xLabel.substringBefore(" in ")} = ${Fmt.num(s.xs[i])}, ${Fmt.num(s.ys[i])}" },
            hitEvery = maxOf(1, (curve.series.firstOrNull()?.xs?.size ?: 1) / 60)
        )
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
        curve.series.forEachIndexed { i, s ->
            Text("${if (s.dashed) "┄" else "━"} ${s.name}", color = colors[i % colors.size], style = MaterialTheme.typography.labelMedium)
        }
    }
    Text("x: ${curve.xLabel}   ·   y: ${curve.yLabel}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

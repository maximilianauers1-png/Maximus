package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.nutrition.domain.AdaptiveTdee
import app.maximus.nutrition.domain.TdeeEstimate
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawTrendChart
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.fmtDay
import app.maximus.ui.strongman.parseDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@Composable
fun JournalTab(log: List<NutritionLogEntity>, adaptive: TdeeEstimate?, today: Long, repo: NutritionRepository) {
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    var day by remember { mutableLongStateOf(today) }
    val existing = log.firstOrNull { it.epochDay == day }
    var weight by remember(day, existing) { mutableStateOf(existing?.weightKg?.let { fmt(it, 1) } ?: "") }
    var kcal by remember(day, existing) { mutableStateOf(existing?.kcal?.let { fmt(it, 0) } ?: "") }
    var protein by remember(day, existing) { mutableStateOf(existing?.proteinG?.let { fmt(it, 0) } ?: "") }
    val weights = log.filter { it.weightKg != null }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard(stringResource(R.string.nu_journal_entry)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphButton(Glyph.CHEVRON_LEFT, stringResource(R.string.cal_prev), { day -= 1 }, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern(if (locale.language == "de") "EEEE, d. MMMM" else "EEEE, d MMMM", locale)),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)
                    )
                    GlyphButton(Glyph.CHEVRON_RIGHT, stringResource(R.string.cal_next), { if (day < today) day += 1 }, tint = MaterialTheme.colorScheme.primary, enabled = day < today)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_weight), weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField(stringResource(R.string.nu_energy), kcal, { kcal = it }, Modifier.weight(1f), suffix = "kcal")
                    NumberField(stringResource(R.string.nu_protein), protein, { protein = it }, Modifier.weight(1f), suffix = "g")
                }
                Text(stringResource(R.string.nu_journal_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(
                    onClick = { scope.launch { repo.saveDay(NutritionLogEntity(day, parseDecimal(weight), parseDecimal(kcal), parseDecimal(protein), existing?.note ?: "")) } },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                ) { Text(stringResource(R.string.save)) }
            }
        }
        item {
            SectionCard(stringResource(R.string.nu_adaptive_title)) {
                if (adaptive == null) {
                    Text(stringResource(R.string.nu_adaptive_missing), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    ResultLine(stringResource(R.string.nu_adaptive_tdee), "${fmt(adaptive.tdee, 0)} ± ${fmt(adaptive.sigma, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_adaptive_slope), "${fmt(adaptive.slopeKgPerWeek, 2)} kg")
                    ResultLine(stringResource(R.string.nu_adaptive_intake), "${fmt(adaptive.meanIntake, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_adaptive_n), "${adaptive.weightDays} / ${adaptive.intakeDays}")
                    Text(stringResource(R.string.nu_adaptive_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (weights.size >= 2) item {
            val trend = remember(weights) { AdaptiveTdee.ewma(weights.map { it.weightKg!! }) }
            ChartFrame(stringResource(R.string.nu_weight_chart), "maximus-gewicht.png") { hits, m, c ->
                val xs = weights.map { it.epochDay.toDouble() }
                drawTrendChart(
                    hits, m, c,
                    xs = xs, ys = weights.map { it.weightKg!! },
                    pointLabels = weights.map { "${fmtDay(it.epochDay)}: ${fmt(it.weightKg!!, 1)} kg" },
                    fitX = xs, fitY = trend, bandLo = trend, bandHi = trend,
                    xLabel = { x -> fmtDay(x.toLong()) }
                )
            }
        }
        items(log.sortedByDescending { it.epochDay }.take(30), key = { it.epochDay }) { e ->
            PlateCard(onClick = { day = e.epochDay }) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(fmtDay(e.epochDay), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(
                        listOfNotNull(e.weightKg?.let { "${fmt(it, 1)} kg" }, e.kcal?.let { "${fmt(it, 0)} kcal" }, e.proteinG?.let { "${fmt(it, 0)} g P" }).joinToString("   "),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

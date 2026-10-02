@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Analytics
import app.maximus.strongman.domain.AnalyticsSet
import app.maximus.strongman.domain.Consistency
import app.maximus.strongman.domain.EventMode
import app.maximus.strongman.domain.EventSet
import app.maximus.strongman.domain.RecordBook
import app.maximus.strongman.domain.RecordProjection
import app.maximus.strongman.domain.RepRecord
import app.maximus.strongman.domain.TrainingLoad
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawBarChart
import app.maximus.ui.charts.drawTrendChart
import app.maximus.ui.components.EmptyState
import java.time.LocalDate

@Composable
fun RecordsTab(repository: StrongmanRepository) {
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val sets by repository.allSets.collectAsState(initial = emptyList<AnalyticsSet>())
    val events by repository.eventSets.collectAsState(initial = emptyList<EventSet>())
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    val today = remember { LocalDate.now().toEpochDay() }

    val trained = remember(sets, events) { (sets.map { it.exerciseId } + events.map { it.exerciseId }).toSet() }
    val options = exercises.filter { it.id in trained }
    val current = selected ?: options.firstOrNull()?.id
    val records = remember(sets, events, current) {
        current?.let { RecordBook.forExercise(sets, it, events) }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { LoadCard(sets, today) }
        if (options.isEmpty()) {
            item { EmptyState(stringResource(R.string.sm_rec_no_data)) }
            return@LazyColumn
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { e ->
                    FilterChip(current == e.id, { selected = e.id }, label = { Text(e.displayName()) })
                }
            }
        }
        val id = current ?: return@LazyColumn
        val rec = records ?: return@LazyColumn
        item { RecordListCard(rec.byReps, rec.totalSets, rec.totalReps, rec.totalVolume) }
        if (rec.events.isNotEmpty()) item { EventRecordCard(rec.events) }
        if (rec.byReps.size >= 3) item { CurveCard(rec.byReps) }
        item { ProjectionCard(sets, id, today) }
    }
}

@Composable
private fun RecordListCard(records: List<RepRecord>, totalSets: Int, totalReps: Int, totalVolume: Double) {
    val stale = remember(records) { RecordBook.staleRecords(records).map { it.reps }.toSet() }
    SectionCard(stringResource(R.string.sm_rec_table)) {
        if (records.isEmpty()) {
            Text(stringResource(R.string.sm_rec_none), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        records.forEach { r ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Text(
                    stringResource(R.string.sm_rec_reps, r.reps),
                    modifier = Modifier.width(80.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (r.reps in stale) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                Text("${fmtKg(r.weightKg)} kg", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"))
                Text(fmtDay(r.epochDay), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (stale.isNotEmpty()) {
            Text(stringResource(R.string.sm_rec_stale), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp))
        }
        ResultLine(stringResource(R.string.sm_rec_total_sets), "$totalSets")
        ResultLine(stringResource(R.string.sm_rec_total_reps), "$totalReps")
        ResultLine(stringResource(R.string.sm_rec_total_volume), "${fmt(totalVolume / 1000, 1)} t")
    }
}

@Composable
private fun EventRecordCard(events: List<app.maximus.strongman.domain.EventRecord>) {
    SectionCard(stringResource(R.string.sm_rec_events)) {
        events.forEach { e ->
            val label = stringResource(
                when (e.mode) {
                    EventMode.FOR_TIME -> R.string.sm_rec_best_time
                    EventMode.MAX_DISTANCE -> R.string.sm_rec_best_distance
                    EventMode.MAX_HEIGHT -> R.string.sm_rec_best_height
                    EventMode.MAX_HOLD_TIME -> R.string.sm_rec_best_hold
                    EventMode.MAX_REPS -> R.string.sm_rec_best_reps
                    EventMode.MAX_LOAD -> R.string.sm_rec_best_load
                }
            )
            val unit = when (e.mode) {
                EventMode.FOR_TIME, EventMode.MAX_HOLD_TIME -> "s"
                EventMode.MAX_DISTANCE -> "m"
                EventMode.MAX_HEIGHT -> "cm"
                else -> ""
            }
            ResultLine(label, "${fmt(e.value, 1)} $unit   @ ${fmtKg(e.weightKg)} kg   ${fmtDay(e.epochDay)}")
        }
    }
}

@Composable
private fun CurveCard(records: List<RepRecord>) {
    val fit = remember(records) { RecordBook.fitLoadRepCurve(records) }
    SectionCard(stringResource(R.string.sm_rec_curve)) {
        if (fit == null) {
            Text(stringResource(R.string.sm_rec_curve_needs_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }
        val (w1, k, r2) = fit
        ResultLine(stringResource(R.string.sm_rec_curve_single), "${fmt(w1, 1)} kg")
        ResultLine(stringResource(R.string.sm_rec_curve_decay), "${fmt(100 * (1 - Math.exp(-k)), 2)} % ${stringResource(R.string.sm_rec_per_rep)}")
        ResultLine("R²", fmt(r2, 3))
        val maxRep = records.maxOf { it.reps }.coerceAtLeast(10)
        ChartFrame(stringResource(R.string.sm_rec_curve_chart), "maximus-last-wiederholungs-kurve.png", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
            val fx = (1..maxRep).map { it.toDouble() }
            val fy = fx.map { RecordBook.predict(fit, it.toInt()) }
            drawTrendChart(
                hits, m, c,
                xs = records.map { it.reps.toDouble() }, ys = records.map { it.weightKg },
                pointLabels = records.map { "${it.reps} × ${fmtKg(it.weightKg)} kg" },
                fitX = fx, fitY = fy, bandLo = fy, bandHi = fy,
                xLabel = { x -> "${x.toInt()}" }
            )
        }
        Text(stringResource(R.string.sm_rec_curve_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProjectionCard(sets: List<AnalyticsSet>, exerciseId: Long, today: Long) {
    val daily = remember(sets, exerciseId) { Analytics.dailyBestE1rm(sets, exerciseId) }
    var targetText by rememberSaveable(exerciseId) { mutableStateOf("") }
    val best = daily.maxOfOrNull { it.second } ?: 0.0
    val target = parseDecimal(targetText) ?: (Math.ceil(best * 1.05 / 5) * 5)
    val projection = remember(daily, target) { RecordProjection.project(daily, target) }

    SectionCard(stringResource(R.string.sm_rec_projection)) {
        NumberField(stringResource(R.string.sm_rec_target), targetText, { targetText = it }, suffix = "kg")
        if (targetText.isBlank()) {
            Text(stringResource(R.string.sm_rec_target_default, fmt(target, 1)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (projection == null) {
            Text(stringResource(R.string.sm_rec_projection_needs_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }
        ResultLine(stringResource(R.string.sm_rec_slope), "${fmt(projection.slopePerWeek, 3)} ± ${fmt(projection.sigma, 3)} kg")
        ResultLine("R²", fmt(projection.r2, 3))
        ResultLine(stringResource(R.string.sm_rec_points), "${projection.n}")
        val days = projection.daysToTarget
        if (days == null || days < 0) {
            Text(stringResource(R.string.sm_rec_no_eta), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        } else {
            ResultLine(stringResource(R.string.sm_rec_eta), fmtDay(today + days.toLong()))
            val lo = projection.daysToTargetLow
            val hi = projection.daysToTargetHigh
            if (lo != null && hi != null && hi >= lo) {
                ResultLine(stringResource(R.string.sm_rec_eta_interval), "${fmtDay(today + lo.toLong())} … ${fmtDay(today + hi.toLong())}")
            } else {
                Text(stringResource(R.string.sm_rec_eta_open), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        Text(stringResource(R.string.sm_rec_projection_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoadCard(sets: List<AnalyticsSet>, today: Long) {
    val series = remember(sets, today) { TrainingLoad.series(sets, today) }
    SectionCard(stringResource(R.string.sm_rec_load)) {
        if (series.isEmpty()) {
            Text(stringResource(R.string.sm_rec_no_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }
        val last = series.last()
        ResultLine(stringResource(R.string.sm_rec_acute), fmt(last.acute, 0))
        ResultLine(stringResource(R.string.sm_rec_chronic), fmt(last.chronic, 0))
        val ratio = last.ratio
        ResultLine(stringResource(R.string.sm_rec_acwr), ratio?.let { fmt(it, 2) } ?: "—")
        if (ratio != null) {
            Text(
                stringResource(
                    when {
                        ratio > 1.5 -> R.string.sm_rec_acwr_high
                        ratio < 0.8 -> R.string.sm_rec_acwr_low
                        else -> R.string.sm_rec_acwr_ok
                    }
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (ratio > 1.5) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val window = series.takeLast(90)
        val cv = remember(window) { Consistency.coefficientOfVariation(window.filter { it.load > 0 }.map { it.load }) }
        cv?.let { ResultLine(stringResource(R.string.sm_rec_cv), "${fmt(100 * it, 0)} %") }
        if (window.size >= 7) {
            ChartFrame(stringResource(R.string.sm_rec_load_chart), "maximus-trainingslast.png", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
                drawTrendChart(
                    hits, m, c,
                    xs = window.map { it.epochDay.toDouble() }, ys = window.map { it.acute },
                    pointLabels = window.map { "${fmtDay(it.epochDay)}: ${fmt(it.acute, 0)}" },
                    fitX = window.map { it.epochDay.toDouble() }, fitY = window.map { it.chronic },
                    bandLo = window.map { it.chronic }, bandHi = window.map { it.chronic },
                    xLabel = { x -> fmtDay(x.toLong()) }
                )
            }
        }
        Text(stringResource(R.string.sm_rec_load_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

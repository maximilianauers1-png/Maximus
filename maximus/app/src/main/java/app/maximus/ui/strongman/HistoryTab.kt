@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.RadarLifts
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Analytics
import app.maximus.strongman.domain.Regression
import app.maximus.strongman.domain.WeekMath
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawBarChart
import app.maximus.ui.charts.drawHeatmap
import app.maximus.ui.charts.drawRadar
import app.maximus.ui.charts.drawTimeline
import app.maximus.ui.charts.drawTrendChart
import java.time.LocalDate

private enum class Range(val days: Long?) { W4(28), W12(84), Y1(365), ALL(null) }

@Composable
fun HistoryTab(repository: StrongmanRepository) {
    val allSets by repository.allSets.collectAsState(initial = emptyList())
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    val byId = remember(exercises) { exercises.associateBy { it.id } }
    var range by rememberSaveable { mutableStateOf(Range.W12) }
    var exerciseId by rememberSaveable { mutableStateOf<Long?>(null) }
    var picking by remember { mutableStateOf(false) }

    val today = LocalDate.now().toEpochDay()
    val sets = remember(allSets, range) { range.days?.let { d -> allSets.filter { it.epochDay >= today - d } } ?: allSets }
    val trained = remember(allSets) { allSets.map { it.exerciseId }.distinct() }
    val chosen = exerciseId ?: trained.lastOrNull()
    val weekdays = listOf(
        stringResource(R.string.dow_mo), stringResource(R.string.dow_tu), stringResource(R.string.dow_we),
        stringResource(R.string.dow_th), stringResource(R.string.dow_fr), stringResource(R.string.dow_sa), stringResource(R.string.dow_su)
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(Range.W4 to R.string.range_4w, Range.W12 to R.string.range_12w, Range.Y1 to R.string.range_1y, Range.ALL to R.string.range_all)
                .forEach { (r, label) -> FilterChip(selected = range == r, onClick = { range = r }, label = { Text(stringResource(label)) }) }
        }
        if (allSets.isEmpty()) {
            Text(stringResource(R.string.sm_history_empty))
            return@Column
        }
        OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
            Text(chosen?.let { byId[it]?.displayName() } ?: stringResource(R.string.sm_pick_exercise))
        }

        // 1. e1RM trend with 95 % prediction band (least squares, requires n >= 5 days).
        if (chosen != null) {
            val daily = Analytics.dailyBestE1rm(sets, chosen)
            val xs = daily.map { it.first.toDouble() }
            val ys = daily.map { it.second }
            val fit = Regression.fit(xs, ys)
            if (fit == null) {
                Text(stringResource(R.string.sm_trend_need_points, Regression.MIN_POINTS, daily.size), style = MaterialTheme.typography.bodySmall)
            } else {
                val x0 = xs.first()
                val x1 = xs.last() + 14.0
                val grid = (0..40).map { x0 + (x1 - x0) * it / 40.0 }
                val slopePerWeek = fit.slope * 7.0
                ChartFrame(stringResource(R.string.chart_e1rm_trend, fmt(slopePerWeek, 2)), "e1rm_trend") { hits, m, c ->
                    drawTrendChart(
                        hits, m, c, xs, ys,
                        daily.map { (d, v) -> "${fmtDay(d)}: ${fmt(v, 1)} kg" },
                        grid, grid.map(fit::predict),
                        grid.map { fit.predict(it) - fit.predictionHalfWidth(it) },
                        grid.map { fit.predict(it) + fit.predictionHalfWidth(it) }
                    ) { fmtDay(it.toLong()) }
                }
            }
        }

        // 2. Weekly volume load.
        val weekly = Analytics.weeklyVolume(sets)
        if (weekly.isNotEmpty()) {
            val keys = weekly.keys.toList()
            ChartFrame(stringResource(R.string.chart_weekly_volume), "weekly_volume") { hits, m, c ->
                drawBarChart(
                    hits, m, c,
                    keys.map { fmtDay(WeekMath.mondayOfWeek(it)).substring(0, 5) },
                    keys.map { weekly.getValue(it) },
                    keys.map { "${fmtDay(WeekMath.mondayOfWeek(it))}: ${fmtKg(weekly.getValue(it))} kg" }
                )
            }
        }

        // 3. Intensity heatmap (week x weekday), mean w / running best e1RM.
        val heat = Analytics.intensityHeatmap(sets)
        if (heat.isNotEmpty()) {
            val weeks = heat.keys.map { it.first }.distinct().sorted()
            ChartFrame(stringResource(R.string.chart_intensity), "intensity_heatmap") { hits, m, c ->
                drawHeatmap(
                    hits, m, c, weeks, weekdays,
                    value = { w, d -> heat[w to d]?.let { ((it - 0.5) / 0.5).coerceIn(0.0, 1.0) } },
                    tooltip = { w, d, _ -> "${fmtDay(WeekMath.mondayOfWeek(w) + d)}: ${fmt(100 * (heat[w to d] ?: 0.0), 0)} %" },
                    columnLabel = { fmtDay(WeekMath.mondayOfWeek(it)).substring(0, 5) }
                )
            }
        }

        // 4. Lift ratios relative to bodyweight (radar).
        val best = Analytics.bestE1rmByExercise(allSets)
        val radarEntities = RadarLifts.keys.mapNotNull { key -> exercises.firstOrNull { it.seedKey == key } }
        val bw = settings.bodyweightKg
        if (bw == null) {
            Text(stringResource(R.string.sm_radar_need_bw), style = MaterialTheme.typography.bodySmall)
        } else if (radarEntities.count { best[it.id] != null } >= 3) {
            val labels = radarEntities.map { it.displayName() }
            val ratios = radarEntities.map { (best[it.id] ?: 0.0) / bw }
            ChartFrame(stringResource(R.string.chart_ratios), "lift_ratios", height = 280.dp) { hits, m, c ->
                drawRadar(hits, m, c, labels, ratios, labels.indices.map { "${labels[it]}: ${fmt(ratios[it], 2)} × BW" })
            }
        }

        // 5. RPE distribution.
        val hist = Analytics.rpeHistogram(sets)
        if (hist.isNotEmpty()) {
            val keys = hist.keys.toList()
            ChartFrame(stringResource(R.string.chart_rpe_distribution), "rpe_distribution") { hits, m, c ->
                drawBarChart(
                    hits, m, c,
                    keys.map { fmt(it, 1) },
                    keys.map { hist.getValue(it).toDouble() },
                    keys.map { "RPE ${fmt(it, 1)}: ${hist.getValue(it)}" }
                )
            }
        }

        // 6. PR timeline.
        val prs = sets.filter { it.isE1rmPr || it.isRepPr }
        if (prs.isNotEmpty()) {
            val rowIds = prs.map { it.exerciseId }.distinct().take(8)
            val rows = rowIds.map { byId[it]?.displayName() ?: "#$it" }
            val events = prs.filter { it.exerciseId in rowIds }
            ChartFrame(stringResource(R.string.chart_pr_timeline), "pr_timeline", height = (60 + 36 * rows.size).dp) { hits, m, c ->
                drawTimeline(
                    hits, m, c, rows,
                    events.map { Triple(it.epochDay, rowIds.indexOf(it.exerciseId), it.isE1rmPr) },
                    events.map { "${fmtDay(it.epochDay)}: ${fmtKg(it.weightKg)} kg × ${it.reps}" + (it.e1rm?.let { e -> " (e1RM ${fmt(e, 1)})" } ?: "") }
                ) { fmtDay(it.toLong()) }
            }
            Text(stringResource(R.string.chart_pr_legend), style = MaterialTheme.typography.bodySmall)
        }
    }

    if (picking) {
        ExercisePickerDialog(exercises.filter { it.id in trained }, { picking = false }) { e -> exerciseId = e.id; picking = false }
    }
}

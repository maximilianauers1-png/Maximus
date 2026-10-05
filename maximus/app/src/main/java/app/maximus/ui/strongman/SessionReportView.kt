@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Arena
import app.maximus.strongman.domain.ExerciseReport
import app.maximus.strongman.domain.Placement
import app.maximus.strongman.domain.SessionReports
import app.maximus.strongman.domain.StandardLift
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.LineSeries
import app.maximus.ui.charts.drawBarChart
import app.maximus.ui.charts.drawLineChart

/** A rank that changed with this session: before and after placement of one lift. */
private data class RankChange(val before: Placement?, val after: Placement)

/**
 * After-training debrief: verdict, XP, records, rank changes, the e1RM course of every trained exercise
 * with today highlighted, tonnage compared with the last training days, INOL and relative intensity.
 */
@Composable
fun SessionReportDialog(repository: StrongmanRepository, sessionDay: Long, sessionSetIds: Set<Long>, onDismiss: () -> Unit) {
    val d = rememberStrongmanData(repository)
    val sessionSets = remember(d, sessionSetIds) { d.sets.filter { it.id in sessionSetIds } }
    val report = remember(d, sessionSets) { SessionReports.build(sessionDay, sessionSets, d.sets) }
    val changes = remember(d, sessionDay) {
        val before = Arena.placements(Arena.bests(d.events, asOfDay = sessionDay - 1), d.population, d.settings.sex, d.settings.bodyweightKg, d.mode)
            .associateBy { it.lift }
        val after = Arena.placements(Arena.bests(d.events, asOfDay = sessionDay), d.population, d.settings.sex, d.settings.bodyweightKg, d.mode)
        after.mapNotNull { a ->
            val b = before[a.lift]
            if (b == null || a.percentile > b.percentile + 0.05) RankChange(b, a) else null
        }.filter { c -> d.sets.any { it.id in sessionSetIds && d.liftOf(it.exerciseId) == c.after.lift } || c.after.lift == StandardLift.TOTAL }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Trainingsbericht · ${fmtDay(sessionDay)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text(report.verdict, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("+${report.xp}", "XP", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                        StatPill(fmt(report.tonnage / 1000, 2) + " t", "Volumen", Modifier.weight(1f))
                        StatPill("${report.sets}", "Sätze", Modifier.weight(1f))
                        StatPill("${report.e1rmPrs + report.repPrs}", "Rekorde", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                    }
                }
                if (changes.isNotEmpty()) item {
                    SectionCard("Aufstieg in der Arena") {
                        changes.forEach { c ->
                            val up = c.before == null || c.before.rank != c.after.rank
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                RankEmblem(c.after.rank, app.maximus.strongman.domain.Ranks.division(c.after.percentile), 44.dp)
                                Column(Modifier.padding(start = 10.dp)) {
                                    Text(c.after.lift.title + if (up) " · NEUER RANG: ${c.after.rank.title}!" else "",
                                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = rankColor(c.after.rank))
                                    Text(c.before?.let { "${pct(it.percentile)} → ${pct(c.after.percentile)}" } ?: "Erstmals gewertet: ${pct(c.after.percentile)}",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                if (report.tonnageHistory.size >= 2) item {
                    ChartFrame("Volumen der letzten Trainingstage (heute in Gold)", "maximus-volumen-bericht", height = 200.dp) { hits, m, c ->
                        drawBarChart(
                            hits, m, c,
                            report.tonnageHistory.map { fmtDay(it.first).substring(0, 5) },
                            report.tonnageHistory.map { it.second / 1000 },
                            report.tonnageHistory.map { "${fmtDay(it.first)}: ${fmt(it.second / 1000, 2)} t" },
                            highlight = report.tonnageHistory.indexOfFirst { it.first == sessionDay }.takeIf { it >= 0 }
                        )
                    }
                }
                items(report.exercises, key = { it.exerciseId }) { ex -> ExerciseReportCard(ex, d.nameOf(ex.exerciseId), sessionDay) }
                item { Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Weiter so") } }
            }
        }
    }
}

@Composable
private fun ExerciseReportCard(ex: ExerciseReport, name: String, day: Long) {
    SectionCard(name) {
        if (ex.prs > 0 || ex.repPrs > 0) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ex.prs > 0) AssistChip(onClick = {}, label = { Text("e1RM-Rekord", fontWeight = FontWeight.Bold) })
                if (ex.repPrs > 0) AssistChip(onClick = {}, label = { Text("${ex.repPrs}× Wiederholungsrekord") })
            }
        }
        ResultLine("Sätze · Wdh. · Volumen", "${ex.sets} · ${ex.reps} · ${fmtKg(ex.tonnage)} kg")
        ex.bestE1rm?.let { b ->
            ResultLine("Bestes e1RM heute", "${fmt(b, 1)} kg" + (ex.deltaPercent?.let { "  (" + (if (it >= 0) "+" else "") + fmt(it, 1) + " %)" } ?: ""))
        }
        ex.previousBestE1rm?.let { ResultLine("Bisheriger Rekord", "${fmt(it, 1)} kg") }
        ex.relativeIntensity?.let { ResultLine("Mittlere Intensität", "${fmt(100 * it, 0)} % des e1RM") }
        ex.inol?.let {
            ResultLine("INOL", fmt(it, 2) + when {
                it < 0.4 -> " · leicht"
                it <= 1.0 -> " · produktiv"
                it <= 2.0 -> " · hart"
                else -> " · sehr hart"
            })
        }
        ex.tonnageVsUsual?.let { ResultLine("Volumen vs. üblich", "${fmt(100 * it, 0)} %") }
        if (ex.e1rmHistory.size >= 2) {
            val xs = ex.e1rmHistory.indices.map { it.toDouble() }
            val ys = ex.e1rmHistory.map { it.second }
            ChartFrame("e1RM-Verlauf (letzte ${ex.e1rmHistory.size} Tage mit dieser Übung)", "maximus-e1rm-bericht", Modifier.padding(top = 8.dp), height = 180.dp) { hits, m, c ->
                drawLineChart(
                    hits, m, c,
                    listOf(
                        LineSeries("e1RM", xs, ys, c.primary, markers = true),
                        LineSeries("heute", listOf(xs.last()), listOf(ys.last()), c.secondary, markers = true, connect = false)
                    ),
                    xFormat = { x -> ex.e1rmHistory.getOrNull(x.toInt())?.first?.let { if (x % 1.0 == 0.0) fmtDay(it).substring(0, 5) else "" } ?: "" },
                    tooltip = { s, i -> val k = if (s.name == "heute") ex.e1rmHistory.lastIndex else i; "${fmtDay(ex.e1rmHistory[k].first)}: ${fmt(ex.e1rmHistory[k].second, 1)} kg" }
                )
            }
        }
        if (ex.e1rmHistory.lastOrNull()?.first != day) Hint("Für diese Übung wurde heute kein e1RM berechnet (Event-Satz oder > 15 Wdh.).")
    }
}

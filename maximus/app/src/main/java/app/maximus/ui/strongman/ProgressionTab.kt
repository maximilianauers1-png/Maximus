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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.strongman.data.ExerciseEntity
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.CurveModel
import app.maximus.strongman.domain.HistoricRecord
import app.maximus.strongman.domain.StandardLift
import app.maximus.strongman.domain.StrengthCurve
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.CurveSample
import app.maximus.ui.charts.ProgPoint
import app.maximus.ui.charts.drawProgression
import java.time.LocalDate
import kotlinx.coroutines.launch

/**
 * Long-term progression: the record book over the years (entries for the past), the record staircase, a
 * model-averaged progression curve and the forecast "e1RM in t" with an honest prediction band.
 */
@Composable
fun ArenaProgressionTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val scope = rememberCoroutineScope()
    var liftName by rememberSaveable { mutableStateOf<String?>(null) }
    var customId by rememberSaveable { mutableStateOf<Long?>(null) }
    var picking by remember { mutableStateOf(false) }
    var horizon by rememberSaveable { mutableIntStateOf(12) }
    var targetText by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<HistoricRecord?>(null) }

    val liftsWithData = remember(d) { StandardLift.singles.filter { l -> d.idsFor(l).isNotEmpty() && d.events.any { it.lift == l } } }
    val lift = if (customId != null) null else (liftsWithData.firstOrNull { it.name == liftName } ?: liftsWithData.firstOrNull())
    val ids: Set<Long> = customId?.let { setOf(it) } ?: lift?.let { d.idsFor(it) } ?: emptySet()
    val title = customId?.let { d.nameOf(it) } ?: lift?.title ?: "Übung"

    val points = remember(d, ids) { StrengthCurve.points(d.sets, d.historic, ids) }
    val envelope = remember(points) { StrengthCurve.envelope(points) }
    val fit = remember(points) { StrengthCurve.fit(points) }
    val best = envelope.lastOrNull()?.e1rm
    val target = parseDecimal(targetText) ?: best?.let { Math.ceil(it * 1.05 / 2.5) * 2.5 }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                liftsWithData.forEach { l -> FilterChip(customId == null && lift == l, { liftName = l.name; customId = null }, label = { Text(l.title) }) }
                FilterChip(customId != null, { picking = true }, label = { Text(customId?.let { d.nameOf(it) } ?: "Andere Übung …") })
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { editing = HistoricRecord(0, ids.firstOrNull() ?: 0, d.today, 0.0, 1) }, modifier = Modifier.weight(1f)) { Text("Rekord eintragen") }
            }
        }
        if (points.isEmpty()) {
            item { Hint("Für $title gibt es noch keine Daten. Trage alte Rekorde aus vergangenen Jahren ein oder logge ein Training.") }
        } else {
            item { CurveCard(title, points, envelope, fit, horizon, { horizon = it }, target, targetText, { targetText = it }, d.today) }
            item { RecordStairCard(envelope) }
        }
        val mine = d.historic.filter { it.exerciseId in ids }
        if (mine.isNotEmpty()) {
            item { Text("Rekordbuch · $title", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
            items(mine, key = { it.id }) { h ->
                SectionCard("${fmtDay(h.epochDay)} · ${fmtKg(h.weightKg)} kg × ${h.reps}" + if (h.competition) " · Wettkampf" else "") {
                    h.e1rm?.let { ResultLine("e1RM", "${fmt(it, 1)} kg") }
                    if (h.note.isNotBlank()) Hint(h.note)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editing = h }) { Text("Bearbeiten") }
                        TextButton(onClick = { scope.launch { repository.deleteHistoric(h.id) } }) { Text("Löschen") }
                    }
                }
            }
        }
        item {
            SectionCard("Das Modell") {
                Hint(
                    "Daten: bestes e1RM je 28-Tage-Fenster (Training + Rekordbuch) – das schätzt, was du damals konntest, statt leichte Tage zu mitteln. " +
                        "Modelle (t in Jahren): linear y = a + bt; logarithmisch y = a + b·ln(1 + t/τ); Sättigung y = a − b·e^(−kt) mit Plateau a. " +
                        "τ und k per Gitter-Suche, a und b per kleinste Quadrate. Gewichtung per AICc: wᵢ = e^(−Δᵢ/2)/Σⱼe^(−Δⱼ/2). " +
                        "Band: 95 %-Prognoseintervall mit Bucklands Modellmittel-Varianz und t-Quantil. Gold: Rekordtreppe, Ringe: Rekordbuch."
                )
                Hint("Prognosen sind Fortschreibungen deiner Vergangenheit – Trainingsumstellungen, Verletzungen oder Gewichtsklassenwechsel kann kein Modell sehen.",
                    MaterialTheme.colorScheme.tertiary)
            }
        }
    }

    if (picking) {
        ExercisePickerDialog(d.exercises, { picking = false }) { e -> customId = e.id; picking = false }
    }
    editing?.let { h ->
        HistoricDialog(h, d.exercises, onDismiss = { editing = null }) { r ->
            scope.launch { repository.saveHistoric(r) }
            editing = null
        }
    }
}

@Composable
private fun CurveCard(
    title: String,
    points: List<app.maximus.strongman.domain.PerfPoint>,
    envelope: List<app.maximus.strongman.domain.PerfPoint>,
    fit: StrengthCurve.Fit?,
    horizon: Int, onHorizon: (Int) -> Unit,
    target: Double?, targetText: String, onTarget: (String) -> Unit,
    today: Long
) {
    val daily = remember(points) {
        points.groupBy { it.epochDay }.map { (_, l) -> l.maxBy { it.e1rm } }.sortedBy { it.epochDay }.takeLast(400)
    }
    val curve = remember(fit, horizon, today) {
        fit?.let { f ->
            val start = f.day0.toDouble()
            val end = maxOf(today.toDouble(), f.lastDay.toDouble()) + horizon * 30.44
            (0..90).map { i -> val x = start + (end - start) * i / 90; CurveSample(x, f.predict(x), f.halfWidth(x)) }
        } ?: emptyList()
    }
    SectionCard("Progression · $title") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(6, 12, 24, 36).forEach { m -> FilterChip(horizon == m, { onHorizon(m) }, label = { Text("+$m Mon.") }) }
        }
        ChartFrame("e1RM über die Jahre", "maximus-progression", height = 260.dp) { hits, m, c ->
            drawProgression(
                hits, m, c,
                points = daily.map { p ->
                    ProgPoint(p.epochDay, p.e1rm, "${fmtDay(p.epochDay)}: ${fmtKg(p.weightKg)} × ${p.reps} → e1RM ${fmt(p.e1rm, 1)}" + if (p.historic) " (Rekordbuch)" else "", p.historic)
                },
                envelope = envelope.map { ProgPoint(it.epochDay, it.e1rm, "") },
                curve = curve,
                today = today,
                target = target
            )
        }
        NumberField("Zielgewicht (e1RM)", targetText, onTarget, Modifier.fillMaxWidth(), suffix = "kg")
        if (fit == null) {
            Hint("Für Kurve und Prognose braucht es Daten aus mindestens ${StrengthCurve.MIN_BINS} verschiedenen 4-Wochen-Fenstern.")
            return@SectionCard
        }
        val last = maxOf(today, fit.lastDay).toDouble()
        ResultLine("Aktueller Trend", "${signed(fit.ratePerMonth(last))} kg/Monat")
        listOf(3, 6, 12, 24).forEach { mo ->
            val x = last + mo * 30.44
            ResultLine("e1RM in $mo Monaten", "${fmt(fit.predict(x), 1)} ± ${fmt(fit.halfWidth(x), 1)} kg")
        }
        fit.plateau?.let { ResultLine("Plateau-Schätzung (Sättigungsmodell)", "${fmt(it, 1)} kg") }
        if (target != null) {
            val mean = fit.dayReaching(target, 0)
            val early = fit.dayReaching(target, 1)
            val late = fit.dayReaching(target, -1)
            ResultLine("Ziel ${fmtKg(target)} kg erreicht", mean?.let { fmtDay(it) } ?: "nicht in 10 Jahren")
            if (mean != null) ResultLine("Spanne (95 %)", "${early?.let { fmtDay(it) } ?: "—"} … ${late?.let { fmtDay(it) } ?: "offen"}")
        }
        Text("Modellgewichte (AICc)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
        fit.models.sortedByDescending { it.weight }.forEach { mdl ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(mdl.model.title + when (mdl.model) {
                    CurveModel.LINEAR -> ""
                    CurveModel.LOGARITHMIC -> " (τ = ${fmt(mdl.shape, 2)} J)"
                    CurveModel.SATURATING -> " (k = ${fmt(mdl.shape, 2)}/J)"
                }, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                Text("${fmt(100 * mdl.weight, 0)} %", style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Bold)
            }
            MeterBar(mdl.weight, MaterialTheme.colorScheme.secondary, height = 4.dp)
        }
    }
}

@Composable
private fun RecordStairCard(envelope: List<app.maximus.strongman.domain.PerfPoint>) {
    SectionCard("Rekordtreppe") {
        envelope.takeLast(12).reversed().forEachIndexed { i, p ->
            val prev = envelope.getOrNull(envelope.size - 2 - i)
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                Text(fmtDay(p.epochDay), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text("${fmt(p.e1rm, 1)} kg", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"))
                Text(prev?.let { "+" + fmt(p.e1rm - it.e1rm, 1) } ?: "Start", style = MaterialTheme.typography.labelMedium,
                    color = if (p.historic) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
            }
        }
        if (envelope.size >= 2) {
            val years = (envelope.last().epochDay - envelope.first().epochDay) / 365.25
            if (years > 0.1) Hint("Seit ${fmtDay(envelope.first().epochDay)}: +${fmt(envelope.last().e1rm - envelope.first().e1rm, 1)} kg " +
                "(${fmt((envelope.last().e1rm - envelope.first().e1rm) / years, 1)} kg/Jahr im Mittel)")
        }
    }
}

private fun signed(x: Double) = (if (x >= 0) "+" else "") + fmt(x, 2)

@Composable
private fun HistoricDialog(initial: HistoricRecord, exercises: List<ExerciseEntity>, onDismiss: () -> Unit, onSave: (HistoricRecord) -> Unit) {
    val date = LocalDate.ofEpochDay(initial.epochDay)
    var exerciseId by remember { mutableStateOf(initial.exerciseId.takeIf { id -> exercises.any { it.id == id } }) }
    var day by remember { mutableStateOf(date.dayOfMonth.toString()) }
    var month by remember { mutableStateOf(date.monthValue.toString()) }
    var year by remember { mutableStateOf(date.year.toString()) }
    var weight by remember { mutableStateOf(if (initial.weightKg > 0) fmtKg(initial.weightKg) else "") }
    var reps by remember { mutableStateOf(initial.reps.toString()) }
    var competition by remember { mutableStateOf(initial.competition) }
    var note by remember { mutableStateOf(initial.note) }
    var picking by remember { mutableStateOf(false) }
    val parsedDate = runCatching { LocalDate.of(year.trim().toInt(), month.trim().toInt(), day.trim().toInt()) }.getOrNull()
        ?.takeIf { !it.isAfter(LocalDate.now()) && it.year >= 1950 }
    val w = parseDecimal(weight)?.takeIf { it > 0 }
    val r = reps.trim().toIntOrNull()?.takeIf { it in 1..30 }
    val ok = exerciseId != null && parsedDate != null && w != null && r != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "Rekord eintragen" else "Rekord bearbeiten") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(exercises.firstOrNull { it.id == exerciseId }?.displayName() ?: "Übung wählen")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Tag", day, { day = it }, Modifier.weight(1f), integer = true)
                    NumberField("Monat", month, { month = it }, Modifier.weight(1f), integer = true)
                    NumberField("Jahr", year, { year = it }, Modifier.weight(1.4f), integer = true)
                }
                if (parsedDate == null) Hint("Gültiges Datum in der Vergangenheit eingeben.", MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Gewicht", weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField("Wdh.", reps, { reps = it }, Modifier.weight(1f), integer = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(competition, { competition = it })
                    Text("Im Wettkampf")
                }
                OutlinedTextField(note, { note = it }, label = { Text("Notiz (z. B. Wettkampf, Ort)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Hint("Das Rekordbuch fließt in Progressionskurven, Ränge und Abzeichen ein, aber nicht in Trainingslast und Volumen.")
            }
        },
        confirmButton = {
            TextButton(enabled = ok, onClick = {
                onSave(initial.copy(exerciseId = exerciseId!!, epochDay = parsedDate!!.toEpochDay(), weightKg = w!!, reps = r!!, competition = competition, note = note.trim()))
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
    if (picking) ExercisePickerDialog(exercises, { picking = false }) { e -> exerciseId = e.id; picking = false }
}

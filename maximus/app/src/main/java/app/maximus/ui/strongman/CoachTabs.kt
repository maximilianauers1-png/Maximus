@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Arena
import app.maximus.strongman.domain.Coach
import app.maximus.strongman.domain.Population
import app.maximus.strongman.domain.ReadinessInput
import app.maximus.strongman.domain.ScoreMode
import app.maximus.strongman.domain.StandardLift
import app.maximus.strongman.domain.SysWeek
import app.maximus.strongman.domain.TrainingLoad
import app.maximus.strongman.domain.TrainingSystem
import app.maximus.strongman.domain.TrainingSystems
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawRadar
import app.maximus.ui.components.EmptyState
import java.time.LocalDate
import kotlinx.coroutines.launch

/** e1RM in current form: best of the last 120 days, otherwise the all-time best. */
private fun StrongmanData.formE1rm(lift: StandardLift): Double? =
    Arena.recentBest(events, lift, today).takeIf { it > 0 } ?: bests[lift]?.e1rm

@Composable
private fun ScaleRow(label: String, value: Int, low: String, high: String, onChange: (Int) -> Unit) {
    Column(Modifier.padding(vertical = 3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Text("$low … $high", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { v -> FilterChip(value == v, { onChange(v) }, label = { Text("$v") }, modifier = Modifier.weight(1f)) }
        }
    }
}

// =====================================================================================================
// Heute: Bereitschaft, Last-Steuerung, Top-Sätze
// =====================================================================================================

@Composable
fun CoachTodayTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    var sleep by rememberSaveable { mutableStateOf("7.5") }
    var quality by rememberSaveable { mutableIntStateOf(3) }
    var stress by rememberSaveable { mutableIntStateOf(3) }
    var soreness by rememberSaveable { mutableIntStateOf(2) }
    var motivation by rememberSaveable { mutableIntStateOf(4) }
    val load = remember(d) { TrainingLoad.series(d.sets, d.today).lastOrNull() }
    val readiness = Coach.readiness(ReadinessInput(parseDecimal(sleep) ?: 7.0, quality, stress, soreness, motivation, load?.ratio))

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Bereitschafts-Check") {
                NumberField("Schlaf letzte Nacht", sleep, { sleep = it }, Modifier.fillMaxWidth(), suffix = "h")
                ScaleRow("Schlafqualität", quality, "schlecht", "top") { quality = it }
                ScaleRow("Stress", stress, "entspannt", "hoch") { stress = it }
                ScaleRow("Muskelkater / Gelenke", soreness, "frisch", "zerstört") { soreness = it }
                ScaleRow("Motivation", motivation, "null", "Bestie") { motivation = it }
            }
        }
        item {
            SectionCard("Urteil des Coaches") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${readiness.score}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold,
                        color = when {
                            readiness.score >= 80 -> MaterialTheme.colorScheme.secondary
                            readiness.score >= 62 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        })
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(readiness.title, style = MaterialTheme.typography.titleLarge)
                        Text("Last ×${fmt(readiness.loadFactor, 3)} · Volumen ×${fmt(readiness.volumeFactor, 1)} · max. RPE ${fmt(readiness.maxRpe, 1)}",
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                MeterBar(readiness.score / 100.0, MaterialTheme.colorScheme.primary, Modifier.padding(vertical = 6.dp))
                Text(readiness.advice, style = MaterialTheme.typography.bodyMedium)
                load?.let { l ->
                    ResultLine("Akute Last (7 d, EWMA)", fmt(l.acute, 0))
                    ResultLine("Chronische Last (28 d)", fmt(l.chronic, 0))
                    ResultLine("ACWR", l.ratio?.let { fmt(it, 2) } ?: "—")
                    l.ratio?.let { r ->
                        Hint(when {
                            r > 1.5 -> "Lastspitze: deutlich mehr als gewohnt. Verletzungsrisiko steigt – Volumen drosseln."
                            r > 1.3 -> "Erhöhte Last – vertretbar in einem Überlastungsblock, aber nicht dauerhaft."
                            r < 0.8 -> "Du trainierst weniger als gewohnt (Deload oder Pause)."
                            else -> "Last im Sweet Spot (0,8–1,3)."
                        })
                    }
                }
                Hint("Score: 25 % Schlafdauer, 20 % Schlafqualität, 15 % Stress, 20 % Erholung, 20 % Motivation; Abzug bei ACWR > 1,3. " +
                    "Autoregulation nach RPE schlägt starre Prozente, weil die Tagesform um ±5–10 % schwankt.")
            }
        }
        val lifts = StandardLift.singles.filter { d.formE1rm(it) != null }
        if (lifts.isEmpty()) {
            item { EmptyState("Sobald du Grundübungen oder Events loggst, schlägt dir der Coach hier die heutigen Top-Sätze vor.") }
        } else item {
            SectionCard("Top-Sätze für heute") {
                Hint("Aus deinem e1RM der letzten 120 Tage über die RPE-Tabelle, angepasst an deine Bereitschaft.")
                lifts.forEach { lift ->
                    val e1 = d.formE1rm(lift)!!
                    val plans = Coach.topSets(e1, d.table, d.settings.incrementKg, readiness)
                    Text("${lift.title} · e1RM ${fmt(e1, 1)} kg", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        plans.forEach { p ->
                            Text("${p.reps} @ ${fmt(p.rpe, 1)}: ${fmtKg(p.kg)} kg", style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Naturalathlet – die Grundregeln") {
                listOf(
                    "Protein 1,6–2,2 g/kg/Tag, auf 3–5 Mahlzeiten verteilt (Morton 2018).",
                    "Schlaf 8–9 h: Schlafmangel senkt Maximalkraft und Testosteron messbar.",
                    "Kraftzuwachs folgt dem Trainingsalter: Anfänger +1–2 %/Monat, Fortgeschrittene +0,5 %, Elite +0,1–0,2 %.",
                    "Pro Muster 10–20 harte Sätze/Woche; Events zählen mit (Yoke ≈ Kniebeuge, Stein ≈ Kreuzheben + Rücken).",
                    "Alle 4–6 Wochen Deload (Volumen −40 %) – Ermüdung verdeckt Fitness."
                ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 1.dp)) }
            }
        }
    }
}

// =====================================================================================================
// Wettkampf (OSG): Countdown, Phase, Versuche, Gewichtsklasse
// =====================================================================================================

@Composable
fun CoachMeetTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val scope = rememberCoroutineScope()
    val meetDay = d.prefs[StrongmanData.PREF_MEET_DAY]?.toLongOrNull()
    val meetName = d.prefs[StrongmanData.PREF_MEET_NAME].orEmpty()
    val limit = d.prefs[StrongmanData.PREF_MEET_LIMIT]?.toDoubleOrNull()
    var editing by rememberSaveable { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (meetDay == null || editing) {
            item {
                MeetEditor(meetName, meetDay ?: (d.today + 84), limit) { name, day, lim ->
                    scope.launch {
                        repository.setPref(StrongmanData.PREF_MEET_NAME, name)
                        repository.setPref(StrongmanData.PREF_MEET_DAY, day.toString())
                        repository.setPref(StrongmanData.PREF_MEET_LIMIT, lim?.toString())
                    }
                    editing = false
                }
            }
            if (meetDay == null) return@LazyColumn
        }
        val plan = Coach.meetPlan(d.today, meetDay)
        item {
            SectionCard(meetName.ifBlank { "Nächster Wettkampf" }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (plan.daysOut >= 0) "${plan.daysOut}" else "✔", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(if (plan.daysOut >= 0) "Tage bis ${fmtDay(meetDay)}" else "Wettkampf am ${fmtDay(meetDay)}", style = MaterialTheme.typography.titleMedium)
                        Text("Phase: ${plan.phase.title}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        plan.nextPhaseInDays?.let { Hint("nächste Phase in $it Tagen") }
                    }
                }
                PhaseTimeline(plan.daysOut)
                TextButton(onClick = { editing = true }) { Text("Wettkampf ändern") }
            }
        }
        item {
            SectionCard("Phase: ${plan.phase.title}") {
                ResultLine("Intensität", plan.phase.intensity)
                ResultLine("Volumen", plan.phase.volume)
                Text(plan.phase.focus, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                Text("Checkliste", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
                plan.checklist.forEach { Text("☐ $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 1.dp)) }
            }
        }
        val lifts = StandardLift.singles.filter { d.formE1rm(it) != null }
        if (lifts.isNotEmpty()) item {
            SectionCard("Versuchsplanung") {
                Row(Modifier.fillMaxWidth()) {
                    Text("", Modifier.weight(1.4f))
                    listOf("1.", "2.", "3.", "3. sicher").forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                }
                lifts.forEach { lift ->
                    val a = Coach.attempts(d.formE1rm(lift)!!, d.settings.incrementKg)
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(lift.short, Modifier.weight(1.4f), style = MaterialTheme.typography.labelLarge)
                        listOf(a.opener, a.second, a.third, a.conservativeThird).forEach {
                            Text(fmtKg(it), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                        }
                    }
                }
                Hint("Opener ≈ 91 % (schaffst du am schlechtesten Tag), 2. ≈ 96 %, 3. ≈ 101 % für den Rekord oder ≈ 98,5 % wenn die Platzierung zählt. " +
                    "Bei Strongman-Ladders und Max-Events gilt dasselbe Prinzip für die ersten Stufen.")
            }
        }
        val bw = d.settings.bodyweightKg
        if (limit != null && bw != null && plan.daysOut > 0) item {
            val wc = Coach.weightClass(bw, limit, d.today, meetDay)
            SectionCard("Gewichtsklasse bis ${fmtKg(limit)} kg") {
                ResultLine("Aktuell", "${fmtKg(bw)} kg")
                ResultLine("Nötig pro Woche", "${fmt(wc.kgPerWeek, 2)} kg (${fmt(wc.percentPerWeek, 2)} %)")
                Text(wc.verdict, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            SectionCard("Warum ein Taper funktioniert") {
                val t = Coach.banisterPeakDays()
                Hint("Banister-Modell: Leistung p(t) = p₀ + k₁·Fitness(t) − k₂·Ermüdung(t), beide exponentiell abklingend (τ₁ = 42 d, τ₂ = 7 d). " +
                    "Nach dem Ende einer Belastung ist der Nettoeffekt k₁e^(−t/τ₁) − k₂e^(−t/τ₂) maximal bei " +
                    "t* = τ₁τ₂·ln(k₂τ₁/(k₁τ₂))/(τ₁ − τ₂) ≈ ${fmt(t, 1)} Tagen (k₂/k₁ = 2). Deshalb: letzter wirklich harter Block 2–3 Wochen vorher, " +
                    "danach Volumen runter, Intensität halten.")
            }
        }
    }
}

@Composable
private fun PhaseTimeline(daysOut: Long) {
    val cs = MaterialTheme.colorScheme
    val colors = listOf(cs.outline, cs.tertiary, cs.primary, cs.secondary, cs.error, cs.secondary)
    val bounds = listOf(140L, 112L, 70L, 35L, 13L, 6L, 0L)
    Column(Modifier.padding(top = 10.dp)) {
        Canvas(Modifier.fillMaxWidth().height(22.dp)) {
            val w = size.width; val h = size.height
            val span = bounds.first().toFloat()
            fun x(day: Long) = (1f - day.coerceIn(0, bounds.first()) / span) * w
            for (i in 0 until bounds.size - 1) {
                val x0 = x(bounds[i]); val x1 = x(bounds[i + 1])
                drawRoundRect(colors[i].copy(alpha = 0.55f), Offset(x0 + 1f, h * 0.3f), Size((x1 - x0 - 2f).coerceAtLeast(1f), h * 0.4f), CornerRadius(4f, 4f))
            }
            val px = x(daysOut).coerceIn(3.dp.toPx(), w - 3.dp.toPx())
            drawCircle(cs.onSurface, 5.dp.toPx().coerceAtMost(h / 2), Offset(px, h / 2))
        }
        Row(Modifier.fillMaxWidth()) {
            Text("−20 Wo.", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text("Wettkampf", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
        Hint("Basis → Akkumulation (16–10 Wo.) → Intensivierung (10–5) → Peaking (5–2) → Taper → Wettkampfwoche")
    }
}

@Composable
private fun MeetEditor(name0: String, day0: Long, limit0: Double?, onSave: (String, Long, Double?) -> Unit) {
    val date = LocalDate.ofEpochDay(day0)
    var name by rememberSaveable { mutableStateOf(name0) }
    var day by rememberSaveable { mutableStateOf(date.dayOfMonth.toString()) }
    var month by rememberSaveable { mutableStateOf(date.monthValue.toString()) }
    var year by rememberSaveable { mutableStateOf(date.year.toString()) }
    var limit by rememberSaveable { mutableStateOf(limit0?.let { fmtKg(it) } ?: "") }
    val parsed = runCatching { LocalDate.of(year.trim().toInt(), month.trim().toInt(), day.trim().toInt()) }.getOrNull()
    SectionCard("Wettkampf planen") {
        OutlinedTextField(name, { name = it }, label = { Text("Name (z. B. OSG Qualifier)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumberField("Tag", day, { day = it }, Modifier.weight(1f), integer = true)
            NumberField("Monat", month, { month = it }, Modifier.weight(1f), integer = true)
            NumberField("Jahr", year, { year = it }, Modifier.weight(1.4f), integer = true)
        }
        NumberField("Gewichtsklasse (Limit, optional)", limit, { limit = it }, Modifier.fillMaxWidth(), suffix = "kg")
        Button(onClick = { onSave(name.trim(), parsed!!.toEpochDay(), parseDecimal(limit)) }, enabled = parsed != null, modifier = Modifier.fillMaxWidth()) {
            Text("Speichern")
        }
    }
}

// =====================================================================================================
// Systeme
// =====================================================================================================

@Composable
fun CoachSystemsTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val scope = rememberCoroutineScope()
    var systemName by rememberSaveable { mutableStateOf(TrainingSystem.WENDLER_531.name) }
    val system = TrainingSystem.valueOf(systemName)
    var liftName by rememberSaveable { mutableStateOf(StandardLift.DEADLIFT.name) }
    val lift = StandardLift.valueOf(liftName)
    var manual by rememberSaveable { mutableStateOf("") }
    var created by remember { mutableStateOf<String?>(null) }
    val known = d.formE1rm(lift)
    val oneRm = parseDecimal(manual)?.takeIf { it > 0 } ?: known
    val lower = lift in setOf(StandardLift.SQUAT, StandardLift.DEADLIFT, StandardLift.AXLE_DEADLIFT, StandardLift.STONE)
    val weeks = remember(system, oneRm, d.settings.incrementKg, lower) {
        oneRm?.let { TrainingSystems.generate(system, it, d.settings.incrementKg, lower) } ?: emptyList()
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TrainingSystem.entries.forEach { s -> FilterChip(system == s, { systemName = s.name; created = null }, label = { Text(s.title) }) }
            }
        }
        item {
            SectionCard("${system.title} · ${system.origin}") {
                Text(system.summary, style = MaterialTheme.typography.bodyMedium)
                Hint(system.forWhom, MaterialTheme.colorScheme.tertiary)
            }
        }
        item {
            SectionCard("Übung und Maximum") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StandardLift.singles.forEach { l -> FilterChip(lift == l, { liftName = l.name; manual = ""; created = null }, label = { Text(l.short) }) }
                }
                ResultLine("e1RM aus dem Log", known?.let { "${fmt(it, 1)} kg" } ?: "—")
                NumberField(if (system == TrainingSystem.EVENT_PEAK) "Wettkampflast / Maximum (optional)" else "Eigenes Maximum (optional)",
                    manual, { manual = it }, Modifier.fillMaxWidth(), suffix = "kg")
            }
        }
        if (oneRm == null) {
            item { EmptyState("Trage ein Maximum ein oder logge ${lift.title}, um den Plan zu berechnen.") }
            return@LazyColumn
        }
        weeks.forEach { w -> item(key = "w_${w.name}") { SystemWeekCard(w) } }
        item {
            val exerciseId = d.idsFor(lift).firstOrNull()
            Button(enabled = exerciseId != null, modifier = Modifier.fillMaxWidth(), onClick = {
                val name = "${system.title} · ${lift.title} (${fmtKg(oneRm)} kg)"
                scope.launch {
                    repository.createProgramFromWeeks(name, TrainingSystems.toProgramWeeks(weeks, exerciseId!!))
                    created = name
                }
            }) { Text("Als Programm anlegen") }
            created?.let { Hint("„$it“ angelegt – zu finden unter Training → Programme.", MaterialTheme.colorScheme.secondary) }
        }
    }
}

@Composable
private fun SystemWeekCard(w: SysWeek) {
    SectionCard(w.name) {
        w.days.forEach { day ->
            if (w.days.size > 1) Text(day.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            day.sets.groupConsecutive().forEach { (s, count) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text((if (count > 1) "$count × " else "") + "${s.reps}${if (s.amrap) "+" else ""}", Modifier.width(72.dp), style = MaterialTheme.typography.titleSmall)
                    Text("${fmtKg(s.kg)} kg", Modifier.width(84.dp), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                    Text("${fmt(s.percent, 0)} %", Modifier.width(52.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(s.note, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Collapses identical consecutive sets into "n × reps". */
private fun List<app.maximus.strongman.domain.SysSet>.groupConsecutive(): List<Pair<app.maximus.strongman.domain.SysSet, Int>> {
    val out = ArrayList<Pair<app.maximus.strongman.domain.SysSet, Int>>()
    for (s in this) {
        val last = out.lastOrNull()
        if (last != null && last.first == s) out[out.lastIndex] = last.first to last.second + 1 else out += s to 1
    }
    return out
}

// =====================================================================================================
// Schwachstellen
// =====================================================================================================

@Composable
fun CoachWeakTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val mode = if (d.settings.bodyweightKg != null) ScoreMode.DOTS else ScoreMode.ABSOLUTE
    val placements = remember(d, mode) { d.placements(Population.STRONGMAN, mode).filter { it.lift != StandardLift.TOTAL } }
    val weak = remember(placements) { Coach.weakPoints(placements) }
    val ratios = remember(d) { Coach.ratios(d.bests.mapValues { it.value.e1rm }, d.settings.sex) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (placements.size < 2) {
            item { EmptyState("Für die Schwachstellen-Analyse braucht es mindestens zwei gewertete Disziplinen.") }
            return@LazyColumn
        }
        if (placements.size >= 3) item {
            ChartFrame("Profil: Perzentil je Disziplin (Strongman, ${if (mode == ScoreMode.DOTS) "DOTS" else "absolut"})", "maximus-kraftprofil", height = 280.dp) { hits, m, c ->
                drawRadar(hits, m, c, placements.map { it.lift.short }, placements.map { it.percentile },
                    placements.map { "${it.lift.title}: ${fmt(it.percentile, 1)} % (${it.rank.title})" })
            }
        }
        item {
            SectionCard("Schwachstellen") {
                if (weak.isEmpty()) Hint("Keine Disziplin liegt deutlich (≥ 0,35 σ) unter deinem Mittel – ein ausgeglichenes Profil.")
                weak.forEach { w ->
                    Text("${w.lift.title}: ${fmt(w.gap, 2)} σ unter deinem Schnitt", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp))
                    Text(w.advice, style = MaterialTheme.typography.bodySmall)
                }
                Hint("Gemessen am eigenen Mittel der z-Werte – nicht am absoluten Niveau. So sieht man, welche Disziplin dich in Wettkämpfen Punkte kostet.")
            }
        }
        if (ratios.isNotEmpty()) item {
            SectionCard("Kraftverhältnisse") {
                ratios.forEach { r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(r.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("${fmt(r.actual, 2)} (Ref ${fmt(r.reference, 2)})", style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"))
                    }
                    val dev = r.deviation
                    MeterBar(0.5 + dev.coerceIn(-0.5, 0.5), if (kotlin.math.abs(dev) < 0.08) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error, height = 5.dp)
                    Hint((if (dev >= 0) "+" else "") + fmt(100 * dev, 0) + " % gegenüber dem Median der Strongman-Population (Balken-Mitte = Referenz)")
                }
            }
        }
        item {
            SectionCard("Alle Disziplinen") {
                placements.sortedBy { it.z }.forEach { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(p.lift.title, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text("${if (p.z >= 0) "+" else ""}${fmt(p.z, 2)} σ · ${p.rank.title}", style = MaterialTheme.typography.labelMedium, color = rankColor(p.rank))
                    }
                }
            }
        }
    }
}

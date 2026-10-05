@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.LoggedSetInput
import app.maximus.strongman.data.SetLogEntity
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.data.WorkoutSessionEntity
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.domain.PlateCalculator
import app.maximus.strongman.domain.PlateResult
import app.maximus.strongman.domain.RecordBook
import app.maximus.strongman.domain.Rpe
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrainingTab(repository: StrongmanRepository) {
    val sessions by repository.sessions.collectAsState(initial = emptyList())
    val table by repository.fTable.collectAsState(initial = FTable())
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    var newDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }

    val selected = sessions.firstOrNull { it.id == selectedId }
    if (selected != null) {
        SessionScreen(repository, selected, onClose = { selectedId = null })
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { newDay -= 1 }) { Text("−1") }
                        Text(fmtDay(newDay), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        TextButton(onClick = { newDay += 1 }) { Text("+1") }
                    }
                    Button(onClick = { scope.launch { selectedId = repository.createSession(newDay) } }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.sm_session_new))
                    }
                }
            }
        }
        items(sessions, key = { it.id }) { s ->
            Card(modifier = Modifier.fillMaxWidth().clickable { selectedId = s.id }) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.sm_session_title, fmtDay(s.epochDay)))
                        if (s.note.isNotBlank()) {
                            Text(s.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    TextButton(onClick = {
                        scope.launch { selectedId = repository.duplicateSession(s.id, newDay, table) }
                    }) { Text(stringResource(R.string.sm_session_repeat)) }
                    TextButton(onClick = { scope.launch { repository.deleteSession(s.id) } }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun SessionScreen(repository: StrongmanRepository, session: WorkoutSessionEntity, onClose: () -> Unit) {
    val sets by remember(session.id) { repository.sessionSets(session.id) }.collectAsState(initial = emptyList())
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val table by repository.fTable.collectAsState(initial = FTable())
    val byId = remember(exercises) { exercises.associateBy { it.id } }
    val scope = rememberCoroutineScope()

    var exerciseId by rememberSaveable(session.id) { mutableStateOf<Long?>(null) }
    var picking by remember { mutableStateOf(false) }
    var weight by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("") }
    var rpe by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable(session.id) { mutableStateOf(session.note) }
    var editing by remember { mutableStateOf<SetLogEntity?>(null) }
    var reporting by remember { mutableStateOf(false) }
    var restSeconds by rememberSaveable { mutableIntStateOf(180) }
    var restStartedAt by rememberSaveable { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val allSets by repository.allSets.collectAsState(initial = emptyList())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())

    LaunchedEffect(restStartedAt) {
        while (restStartedAt > 0L) { now = System.currentTimeMillis(); delay(250) }
    }

    LaunchedEffect(sets.size) {
        if (exerciseId == null) exerciseId = sets.lastOrNull()?.exerciseId
    }

    val entity = exerciseId?.let { byId[it] }
    val w = parseDecimal(weight.ifBlank { "0" })
    val r = reps.trim().toIntOrNull()
    val rpeV = rpe.takeIf { it.isNotBlank() }?.let(::parseDecimal)
    val rpeOk = rpe.isBlank() || (rpeV != null && Rpe.isValid(rpeV))
    val canSave = entity != null && w != null && w >= 0.0 && r != null && r >= 0 && rpeOk
    val preview = if (canSave && w!! > 0.0 && r!! > 0) repository.e1rmFor(w, r, rpeV, table) else null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text(stringResource(R.string.nav_back)) }
                Text(stringResource(R.string.sm_session_title, fmtDay(session.epochDay)), style = MaterialTheme.typography.titleMedium)
            }
        }
        if (sets.isNotEmpty()) item {
            Button(onClick = { reporting = true }, modifier = Modifier.fillMaxWidth()) { Text("Training abschließen & auswerten") }
        }
        item {
            SectionCard(stringResource(R.string.sm_log_set)) {
                OutlinedButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(entity?.displayName() ?: stringResource(R.string.sm_pick_exercise))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.sm_weight), weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField(stringResource(R.string.sm_reps), reps, { reps = it }, Modifier.weight(1f), integer = true)
                    NumberField(stringResource(R.string.sm_rpe_optional), rpe, { rpe = it }, Modifier.weight(1f))
                }
                if (entity?.isEvent == true) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField(stringResource(R.string.sm_distance), distance, { distance = it }, Modifier.weight(1f), suffix = "m")
                        NumberField(stringResource(R.string.sm_height), height, { height = it }, Modifier.weight(1f), suffix = "cm")
                        NumberField(stringResource(R.string.sm_time), time, { time = it }, Modifier.weight(1f), suffix = "s")
                    }
                }
                if (!rpeOk) Text(stringResource(R.string.sm_refuse_rpe), color = MaterialTheme.colorScheme.error)
                preview?.let { ResultLine(stringResource(R.string.sm_e1rm), "${fmt(it, 1)} kg") }
                Button(
                    enabled = canSave,
                    onClick = {
                        val input = LoggedSetInput(
                            sessionId = session.id,
                            exerciseId = entity!!.id,
                            epochDay = session.epochDay,
                            weightKg = w!!,
                            reps = r!!,
                            rpe = rpeV,
                            distanceM = parseDecimal(distance),
                            heightCm = parseDecimal(height),
                            timeS = parseDecimal(time)
                        )
                        scope.launch { repository.logSet(input, table) }
                        restStartedAt = System.currentTimeMillis()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.sm_save_set)) }
            }
        }
        item {
            RestTimerCard(
                seconds = restSeconds, startedAt = restStartedAt, now = now,
                onSelect = { restSeconds = it },
                onStart = { restStartedAt = System.currentTimeMillis() },
                onStop = { restStartedAt = 0L }
            )
        }
        if (entity != null) {
            item { ReferenceCard(allSets, entity.id, session.epochDay, w ?: 0.0, settings) }
        }
        item {
            SectionCard(stringResource(R.string.sm_session_note)) {
                OutlinedTextField(
                    note, { note = it }, label = { Text(stringResource(R.string.sm_session_note_label)) },
                    minLines = 2, modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { scope.launch { repository.setSessionNote(session.id, note) } }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save))
                }
            }
        }
        item {
            val volume = sets.sumOf { it.weightKg * it.reps }
            val tonnageByExercise = sets.groupBy { it.exerciseId }.mapValues { (_, v) -> v.sumOf { it.weightKg * it.reps } }
            SectionCard(stringResource(R.string.sm_session_summary)) {
                ResultLine(stringResource(R.string.sm_session_volume_label), "${fmtKg(volume)} kg")
                ResultLine(stringResource(R.string.sm_session_sets), "${sets.size}")
                ResultLine(stringResource(R.string.sm_session_reps), "${sets.sumOf { it.reps }}")
                tonnageByExercise.forEach { (id, v) ->
                    ResultLine(byId[id]?.displayName() ?: "#" + id, "${fmtKg(v)} kg")
                }
            }
        }
        items(sets, key = { it.id }) { s ->
            LoggedSetRow(
                s, byId[s.exerciseId]?.displayName() ?: "#" + s.exerciseId,
                onEdit = { editing = s },
                onDelete = { scope.launch { repository.deleteSetLog(s.id, s.exerciseId) } }
            )
        }
    }

    if (picking) {
        ExercisePickerDialog(exercises, { picking = false }) { e -> exerciseId = e.id; picking = false }
    }
    if (reporting) {
        SessionReportDialog(repository, session.epochDay, sets.map { it.id }.toSet(), onDismiss = { reporting = false })
    }
    editing?.let { set ->
        EditSetDialog(set, byId[set.exerciseId]?.displayName() ?: "", onDismiss = { editing = null }) { input ->
            scope.launch { repository.updateSetLog(set.id, input, table) }
            editing = null
        }
    }
}

/**
 * Rest timer. The remaining time is derived from the wall clock, not from a counter, so it stays
 * correct when the screen sleeps or the app is backgrounded.
 */
@Composable
private fun RestTimerCard(seconds: Int, startedAt: Long, now: Long, onSelect: (Int) -> Unit, onStart: () -> Unit, onStop: () -> Unit) {
    val elapsed = if (startedAt > 0L) ((now - startedAt) / 1000).toInt() else 0
    val remaining = (seconds - elapsed).coerceAtLeast(0)
    SectionCard(stringResource(R.string.sm_rest_timer)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(60, 90, 120, 180, 240, 300, 420).forEach { t ->
                FilterChip(seconds == t, { onSelect(t) }, label = { Text(if (t < 60) t.toString() + " s" else (t / 60).toString() + " min") })
            }
        }
        if (startedAt > 0L) {
            Text(
                "%d:%02d".format(remaining / 60, remaining % 60),
                style = MaterialTheme.typography.displaySmall,
                color = if (remaining == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            LinearProgressIndicator(
                progress = { if (seconds > 0) (elapsed.toFloat() / seconds).coerceIn(0f, 1f) else 0f },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStart) { Text(stringResource(R.string.sm_rest_start)) }
            if (startedAt > 0L) OutlinedButton(onClick = onStop) { Text(stringResource(R.string.sm_rest_stop)) }
        }
        Text(stringResource(R.string.sm_rest_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Shows the last session with this exercise, the current rep records and the plate loading. */
@Composable
private fun ReferenceCard(
    allSets: List<app.maximus.strongman.domain.AnalyticsSet>,
    exerciseId: Long,
    today: Long,
    weightKg: Double,
    settings: StrengthSettings
) {
    val records = remember(allSets, exerciseId) { RecordBook.forExercise(allSets, exerciseId) }
    val lastDay = remember(allSets, exerciseId, today) {
        allSets.filter { it.exerciseId == exerciseId && it.epochDay < today }.maxOfOrNull { it.epochDay }
    }
    val lastSets = remember(allSets, exerciseId, lastDay) {
        allSets.filter { it.exerciseId == exerciseId && it.epochDay == lastDay }
    }
    SectionCard(stringResource(R.string.sm_reference)) {
        if (lastDay == null) {
            Text(stringResource(R.string.sm_reference_first_time), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            ResultLine(stringResource(R.string.sm_reference_last), fmtDay(lastDay))
            Text(
                lastSets.joinToString("   ") { fmtKg(it.weightKg) + " × " + it.reps },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (records.byReps.isNotEmpty()) {
            Text(
                records.byReps.take(6).joinToString("   ") { it.reps.toString() + ": " + fmtKg(it.weightKg) },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary
            )
        }
        if (weightKg > 0) {
            val text = when (val r = PlateCalculator.solve(weightKg, settings.barKg, settings.plates)) {
                is PlateResult.Loadable ->
                    if (r.perSide.isEmpty()) stringResource(R.string.sm_plate_empty_bar)
                    else stringResource(R.string.sm_plate_per_side, r.perSide.joinToString(" + ") { fmtKg(it) })
                is PlateResult.NotLoadable -> stringResource(R.string.sm_plate_not_loadable)
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EditSetDialog(set: SetLogEntity, name: String, onDismiss: () -> Unit, onSave: (LoggedSetInput) -> Unit) {
    var weight by remember(set.id) { mutableStateOf(fmtKg(set.weightKg)) }
    var reps by remember(set.id) { mutableStateOf(set.reps.toString()) }
    var rpe by remember(set.id) { mutableStateOf(set.rpe?.let { fmt(it, 1) } ?: "") }
    var distance by remember(set.id) { mutableStateOf(set.distanceM?.let { fmtKg(it) } ?: "") }
    var height by remember(set.id) { mutableStateOf(set.heightCm?.let { fmtKg(it) } ?: "") }
    var time by remember(set.id) { mutableStateOf(set.timeS?.let { fmtKg(it) } ?: "") }
    val w = parseDecimal(weight)
    val r = reps.trim().toIntOrNull()
    val rpeV = rpe.takeIf { it.isNotBlank() }?.let(::parseDecimal)
    val rpeOk = rpe.isBlank() || (rpeV != null && Rpe.isValid(rpeV))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_edit_set)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.sm_weight), weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField(stringResource(R.string.sm_reps), reps, { reps = it }, Modifier.weight(1f), integer = true)
                    NumberField(stringResource(R.string.sm_rpe_optional), rpe, { rpe = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.sm_distance), distance, { distance = it }, Modifier.weight(1f), suffix = "m")
                    NumberField(stringResource(R.string.sm_height), height, { height = it }, Modifier.weight(1f), suffix = "cm")
                    NumberField(stringResource(R.string.sm_time), time, { time = it }, Modifier.weight(1f), suffix = "s")
                }
                if (!rpeOk) Text(stringResource(R.string.sm_refuse_rpe), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = w != null && r != null && r >= 0 && rpeOk,
                onClick = {
                    onSave(
                        LoggedSetInput(
                            sessionId = set.sessionId, exerciseId = set.exerciseId, epochDay = set.epochDay,
                            weightKg = w!!, reps = r!!, rpe = rpeV,
                            distanceM = parseDecimal(distance), heightCm = parseDecimal(height), timeS = parseDecimal(time)
                        )
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun LoggedSetRow(s: SetLogEntity, name: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleSmall)
                    val parts = listOfNotNull(
                        "${fmtKg(s.weightKg)} kg × ${s.reps}",
                        s.rpe?.let { "RPE ${fmt(it, 1)}" },
                        s.distanceM?.let { "${fmtKg(it)} m" },
                        s.heightCm?.let { "${fmtKg(it)} cm" },
                        s.timeS?.let { "${fmtKg(it)} s" },
                        s.e1rm?.let { "e1RM ${fmt(it, 1)}" }
                    )
                    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
            if (s.isE1rmPr || s.isRepPr) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (s.isE1rmPr) AssistChip(onClick = {}, label = { Text(stringResource(R.string.sm_pr_e1rm), fontWeight = FontWeight.Bold) })
                    if (s.isRepPr) AssistChip(onClick = {}, label = { Text(stringResource(R.string.sm_pr_rep)) })
                }
            }
        }
    }
}

@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.ui.components.MaximusTopBar
import app.maximus.strongman.data.ExerciseEntity
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.EventMode
import app.maximus.strongman.domain.EventSpec
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.LoadContext
import app.maximus.strongman.domain.LoadResolver
import app.maximus.strongman.domain.LoadSpec
import app.maximus.strongman.domain.Program
import app.maximus.strongman.domain.ProgramDay
import app.maximus.strongman.domain.ProgramExercise
import app.maximus.strongman.domain.ProgramGenerator
import app.maximus.strongman.domain.ProgramWeek
import app.maximus.strongman.domain.ProgressionRule
import app.maximus.strongman.domain.Rpe
import app.maximus.strongman.domain.SetPrescription

private sealed interface EditorDialog {
    data object Rename : EditorDialog
    data object Weeks : EditorDialog
    data object CopyWeek : EditorDialog
    data object Generate : EditorDialog
    data class AddExercise(val day: Int) : EditorDialog
    data class RenameDay(val day: Int) : EditorDialog
    data class Rule(val day: Int, val ex: Int) : EditorDialog
    data class EditSet(val day: Int, val ex: Int, val set: Int) : EditorDialog
}

@Composable
fun ProgramEditorScreen(repository: StrongmanRepository, programId: Long, onBack: () -> Unit) {
    var program by remember { mutableStateOf<Program?>(null) }
    var week by remember { mutableIntStateOf(0) }
    var dialog by remember { mutableStateOf<EditorDialog?>(null) }
    var e1rms by remember { mutableStateOf<Map<Long, Double>>(emptyMap()) }
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    val table by repository.fTable.collectAsState(initial = FTable())
    val byId = remember(exercises) { exercises.associateBy { it.id } }

    LaunchedEffect(programId) {
        repository.ensureSeeded()
        program = repository.loadProgram(programId)
    }
    LaunchedEffect(program?.weeks) {
        val ids = program?.weeks?.flatMap { w -> w.days.flatMap { d -> d.exercises.map { it.exerciseId } } }?.toSet().orEmpty()
        e1rms = repository.latestE1rms(ids)
    }

    fun commit(p: Program) {
        program = p
        repository.saveProgramAsync(p)
    }

    val p = program
    Scaffold(
        topBar = {
            MaximusTopBar(title = p?.name ?: "", onBack = onBack, onTitleClick = { dialog = EditorDialog.Rename })
        }
    ) { padding ->
        if (p == null) {
            Text(stringResource(R.string.diag_loading), modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        val w = week.coerceIn(0, p.weeks.lastIndex)
        val currentWeek = p.weeks[w]
        fun updateWeek(transform: (ProgramWeek) -> ProgramWeek) = commit(p.withWeek(w, transform(currentWeek)))
        fun updateDay(d: Int, transform: (ProgramDay) -> ProgramDay) =
            updateWeek { wk -> wk.copy(days = wk.days.mapIndexed { i, day -> if (i == d) transform(day) else day }) }
        fun updateExercise(d: Int, e: Int, transform: (ProgramExercise) -> ProgramExercise) =
            updateDay(d) { day -> day.copy(exercises = day.exercises.mapIndexed { i, ex -> if (i == e) transform(ex) else ex }) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(p.weeks.size) { i ->
                        val deload = ProgramGenerator.isDeloadWeek(i + 1, p.deloadEvery)
                        FilterChip(
                            selected = i == w,
                            onClick = { week = i },
                            label = { Text(if (deload) stringResource(R.string.sm_week_deload_short, i + 1) else stringResource(R.string.sm_week_short, i + 1)) }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { dialog = EditorDialog.Weeks }) { Text(stringResource(R.string.sm_weeks_settings)) }
                    OutlinedButton(onClick = { dialog = EditorDialog.CopyWeek }) { Text(stringResource(R.string.sm_copy_week)) }
                    OutlinedButton(onClick = { dialog = EditorDialog.Generate }) { Text(stringResource(R.string.sm_generate)) }
                }
            }
            itemsIndexed(currentWeek.days) { di, day ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(day.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).clickable { dialog = EditorDialog.RenameDay(di) })
                            TextButton(onClick = { updateWeek { wk -> wk.copy(days = wk.days.filterIndexed { i, _ -> i != di }) } }) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                        day.exercises.forEachIndexed { ei, ex ->
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            val entity = byId[ex.exerciseId]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entity?.displayName() ?: "#${ex.exerciseId}", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        ruleSummary(ex.rule),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.clickable { dialog = EditorDialog.Rule(di, ei) }
                                    )
                                }
                                TextButton(onClick = { updateDay(di) { d -> d.copy(exercises = d.exercises.filterIndexed { i, _ -> i != ei }) } }) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                            val ctx = LoadContext(e1rms[ex.exerciseId], settings.bodyweightKg, settings.incrementKg, table)
                            ex.sets.forEachIndexed { si, set ->
                                Text(
                                    setSummary(si + 1, set, LoadResolver.resolve(set, ctx)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth().clickable { dialog = EditorDialog.EditSet(di, ei, si) }.padding(vertical = 6.dp)
                                )
                            }
                            TextButton(onClick = {
                                updateExercise(di, ei) { e ->
                                    val template = e.sets.lastOrNull() ?: defaultSet(entity)
                                    e.copy(sets = e.sets + template)
                                }
                            }) { Text(stringResource(R.string.sm_add_set)) }
                        }
                        TextButton(onClick = { dialog = EditorDialog.AddExercise(di) }) { Text(stringResource(R.string.sm_add_exercise)) }
                    }
                }
            }
            item {
                val dayName = stringResource(R.string.sm_day_default, currentWeek.days.size + 1)
                OutlinedButton(onClick = { updateWeek { wk -> wk.copy(days = wk.days + ProgramDay(dayName, emptyList())) } }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.sm_add_day))
                }
            }
        }

        when (val d = dialog) {
            null -> Unit
            EditorDialog.Rename -> TextInputDialog(stringResource(R.string.sm_name), p.name, { dialog = null }) {
                commit(p.copy(name = it)); dialog = null
            }
            is EditorDialog.RenameDay -> TextInputDialog(stringResource(R.string.sm_day_name), currentWeek.days[d.day].name, { dialog = null }) { name ->
                updateDay(d.day) { it.copy(name = name) }; dialog = null
            }
            EditorDialog.Weeks -> WeeksDialog(p, { dialog = null }) { weeks, deload ->
                val resized = if (weeks <= p.weeks.size) p.weeks.take(weeks) else p.weeks + List(weeks - p.weeks.size) { p.weeks.last() }
                commit(p.copy(weeks = resized, deloadEvery = deload))
                dialog = null
            }
            EditorDialog.CopyWeek -> CopyWeekDialog(p.weeks.size, w, { dialog = null }) { targets ->
                var np: Program = p
                targets.forEach { t -> np = np.copyWeek(w, t) }
                commit(np)
                dialog = null
            }
            EditorDialog.Generate -> AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(stringResource(R.string.sm_generate)) },
                text = { Text(stringResource(R.string.sm_generate_text, p.weeks.size)) },
                confirmButton = {
                    TextButton(onClick = {
                        commit(p.copy(weeks = ProgramGenerator.generate(p.weeks[0], p.weeks.size, p.deloadEvery)))
                        dialog = null
                    }) { Text(stringResource(R.string.sm_generate_confirm)) }
                },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) } }
            )
            is EditorDialog.AddExercise -> ExercisePickerDialog(exercises, { dialog = null }) { entity ->
                updateDay(d.day) { day -> day.copy(exercises = day.exercises + ProgramExercise(entity.id, ProgressionRule.None, listOf(defaultSet(entity)))) }
                dialog = null
            }
            is EditorDialog.Rule -> {
                val ex = currentWeek.days.getOrNull(d.day)?.exercises?.getOrNull(d.ex)
                if (ex == null) dialog = null else RuleDialog(ex.rule, { dialog = null }) { rule ->
                    // The rule belongs to the exercise slot and is applied to the same slot in every week.
                    commit(p.copy(weeks = p.weeks.map { wk ->
                        wk.copy(days = wk.days.mapIndexed { i, day ->
                            if (i != d.day) day else day.copy(exercises = day.exercises.mapIndexed { j, e -> if (j == d.ex) e.copy(rule = rule) else e })
                        })
                    }))
                    dialog = null
                }
            }
            is EditorDialog.EditSet -> {
                val ex = currentWeek.days.getOrNull(d.day)?.exercises?.getOrNull(d.ex)
                val set = ex?.sets?.getOrNull(d.set)
                if (ex == null || set == null) dialog = null else SetEditDialog(
                    set = set,
                    isEvent = byId[ex.exerciseId]?.isEvent == true,
                    onDismiss = { dialog = null },
                    onDelete = {
                        updateExercise(d.day, d.ex) { e -> e.copy(sets = e.sets.filterIndexed { i, _ -> i != d.set }) }
                        dialog = null
                    },
                    onSave = { newSet ->
                        updateExercise(d.day, d.ex) { e -> e.copy(sets = e.sets.mapIndexed { i, s -> if (i == d.set) newSet else s }) }
                        dialog = null
                    }
                )
            }
        }
    }
}

private fun defaultSet(entity: ExerciseEntity?): SetPrescription {
    val mode = entity?.let { runCatching { EventMode.valueOf(it.defaultMode) }.getOrNull() } ?: EventMode.MAX_REPS
    return if (entity?.isEvent == true) {
        SetPrescription(reps = 1, load = LoadSpec.Absolute(100.0), restSeconds = 180, event = EventSpec(mode = mode))
    } else {
        SetPrescription(reps = 5, load = LoadSpec.PercentOneRm(75.0), restSeconds = 180)
    }
}

@Composable
fun ruleSummary(rule: ProgressionRule): String = when (rule) {
    ProgressionRule.None -> stringResource(R.string.sm_rule_none)
    is ProgressionRule.Linear -> stringResource(R.string.sm_rule_linear_summary, fmtKg(rule.kgPerWeek), fmtKg(rule.percentPerWeek))
    is ProgressionRule.DoubleProgression -> stringResource(R.string.sm_rule_double_summary, rule.minReps, rule.maxReps, fmtKg(rule.incrementKg))
    is ProgressionRule.RpeAutoregulation -> stringResource(R.string.sm_rule_rpe_summary, fmt(rule.rpeStepPerWeek, 1))
    is ProgressionRule.Wave -> stringResource(R.string.sm_rule_wave_summary, fmtKg(rule.stepPercent), rule.length, fmtKg(rule.wavePercent))
}

@Composable
private fun setSummary(index: Int, set: SetPrescription, resolvedKg: Double?): String {
    val load = when (val l = set.load) {
        is LoadSpec.Absolute -> "${fmtKg(l.kg)} kg"
        is LoadSpec.PercentOneRm -> "${fmtKg(l.percent)} %" + (resolvedKg?.let { " ≈ ${fmtKg(it)} kg" } ?: "")
        is LoadSpec.AtRpe -> "@RPE ${fmt(l.rpe, 1)}" + (resolvedKg?.let { " ≈ ${fmtKg(it)} kg" } ?: "")
        is LoadSpec.BodyweightRelative -> "${fmtKg(l.factor)} × BW" + (resolvedKg?.let { " ≈ ${fmtKg(it)} kg" } ?: "")
    }
    val event = set.event?.let { e ->
        listOfNotNull(
            e.distanceM?.let { "${fmtKg(it)} m" },
            e.heightCm?.let { "${fmtKg(it)} cm" },
            e.implementKg?.let { "${stringResource(R.string.sm_implement_short)} ${fmtKg(it)} kg" },
            e.timeCapS?.let { "≤ $it s" },
            eventModeLabel(e.mode)
        ).joinToString(" · ")
    }
    val base = "$index.  ${set.reps} × $load · ${set.restSeconds} s"
    return listOfNotNull(base, event, set.note.takeIf { it.isNotBlank() }).joinToString(" · ")
}

@Composable
fun eventModeLabel(mode: EventMode): String = when (mode) {
    EventMode.MAX_REPS -> stringResource(R.string.sm_mode_max_reps)
    EventMode.MAX_LOAD -> stringResource(R.string.sm_mode_max_load)
    EventMode.FOR_TIME -> stringResource(R.string.sm_mode_for_time)
    EventMode.MAX_DISTANCE -> stringResource(R.string.sm_mode_max_distance)
    EventMode.MAX_HEIGHT -> stringResource(R.string.sm_mode_max_height)
    EventMode.MAX_HOLD_TIME -> stringResource(R.string.sm_mode_max_hold)
}

@Composable
fun TextInputDialog(label: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(label) },
        text = { OutlinedTextField(text, { text = it }, singleLine = true) },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.trim()) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun WeeksDialog(p: Program, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var weeks by remember { mutableStateOf(p.weeks.size.toString()) }
    var deload by remember { mutableStateOf(p.deloadEvery.toString()) }
    val w = weeks.trim().toIntOrNull()
    val d = deload.trim().toIntOrNull()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_weeks_settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField(stringResource(R.string.sm_weeks_range), weeks, { weeks = it }, integer = true)
                NumberField(stringResource(R.string.sm_deload_every), deload, { deload = it }, integer = true)
                Text(stringResource(R.string.sm_weeks_hint), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(enabled = w != null && w in 1..ProgramGenerator.MAX_WEEKS && d != null && d >= 0, onClick = { onConfirm(w!!, d!!) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun CopyWeekDialog(weekCount: Int, from: Int, onDismiss: () -> Unit, onConfirm: (Set<Int>) -> Unit) {
    var targets by remember { mutableStateOf(emptySet<Int>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_copy_week_title, from + 1)) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until weekCount) {
                    if (i == from) continue
                    FilterChip(
                        selected = i in targets,
                        onClick = { targets = if (i in targets) targets - i else targets + i },
                        label = { Text(stringResource(R.string.sm_week_short, i + 1)) }
                    )
                }
            }
        },
        confirmButton = { TextButton(enabled = targets.isNotEmpty(), onClick = { onConfirm(targets) }) { Text(stringResource(R.string.sm_copy)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
fun ExercisePickerDialog(exercises: List<ExerciseEntity>, onDismiss: () -> Unit, onPick: (ExerciseEntity) -> Unit) {
    var query by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_pick_exercise)) },
        text = {
            Column {
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.search)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val q = query.trim().lowercase()
                val filtered = exercises.filter { q.isEmpty() || it.nameDe.lowercase().contains(q) || it.nameEn.lowercase().contains(q) }
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.id }) { e ->
                        Text(
                            e.displayName(),
                            modifier = Modifier.fillMaxWidth().clickable { onPick(e) }.padding(vertical = 10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun RuleDialog(initial: ProgressionRule, onDismiss: () -> Unit, onConfirm: (ProgressionRule) -> Unit) {
    // Explicit keys (not class names), because R8 renames classes in release builds.
    val init = remember {
        when (initial) {
            ProgressionRule.None -> listOf("None", "", "", "")
            is ProgressionRule.Linear -> listOf("Linear", fmtKg(initial.kgPerWeek), fmtKg(initial.percentPerWeek), "")
            is ProgressionRule.DoubleProgression -> listOf("DoubleProgression", initial.minReps.toString(), initial.maxReps.toString(), fmtKg(initial.incrementKg))
            is ProgressionRule.RpeAutoregulation -> listOf("RpeAutoregulation", fmt(initial.rpeStepPerWeek, 1), "", "")
            is ProgressionRule.Wave -> listOf("Wave", fmtKg(initial.stepPercent), initial.length.toString(), fmtKg(initial.wavePercent))
        }
    }
    var type by remember { mutableStateOf(init[0]) }
    var a by remember { mutableStateOf(init[1]) }
    var b by remember { mutableStateOf(init[2]) }
    var c by remember { mutableStateOf(init[3]) }
    val rule: ProgressionRule? = when (type) {
        "Linear" -> {
            val kg = parseDecimal(a.ifBlank { "0" }); val pct = parseDecimal(b.ifBlank { "0" })
            if (kg != null && pct != null) ProgressionRule.Linear(kg, pct) else null
        }
        "DoubleProgression" -> {
            val min = a.trim().toIntOrNull(); val max = b.trim().toIntOrNull(); val inc = parseDecimal(c)
            if (min != null && max != null && inc != null && min >= 1 && max >= min) ProgressionRule.DoubleProgression(min, max, inc) else null
        }
        "RpeAutoregulation" -> parseDecimal(a)?.let { ProgressionRule.RpeAutoregulation(it) }
        "Wave" -> {
            val step = parseDecimal(a); val len = b.trim().toIntOrNull(); val wave = parseDecimal(c)
            if (step != null && len != null && len >= 1 && wave != null) ProgressionRule.Wave(step, wave, len) else null
        }
        else -> ProgressionRule.None
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_rule_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "None" to R.string.sm_rule_none,
                        "Linear" to R.string.sm_rule_linear,
                        "DoubleProgression" to R.string.sm_rule_double,
                        "RpeAutoregulation" to R.string.sm_rule_rpe,
                        "Wave" to R.string.sm_rule_wave
                    ).forEach { (key, label) ->
                        FilterChip(selected = type == key, onClick = { type = key }, label = { Text(stringResource(label)) })
                    }
                }
                when (type) {
                    "Linear" -> {
                        NumberField(stringResource(R.string.sm_rule_kg_per_week), a, { a = it }, suffix = "kg")
                        NumberField(stringResource(R.string.sm_rule_pct_per_week), b, { b = it }, suffix = "%")
                    }
                    "DoubleProgression" -> {
                        NumberField(stringResource(R.string.sm_rule_min_reps), a, { a = it }, integer = true)
                        NumberField(stringResource(R.string.sm_rule_max_reps), b, { b = it }, integer = true)
                        NumberField(stringResource(R.string.sm_rule_increment), c, { c = it }, suffix = "kg")
                    }
                    "RpeAutoregulation" -> NumberField(stringResource(R.string.sm_rule_rpe_step), a, { a = it })
                    "Wave" -> {
                        NumberField(stringResource(R.string.sm_rule_wave_step), a, { a = it }, suffix = "%")
                        NumberField(stringResource(R.string.sm_rule_wave_length), b, { b = it }, integer = true)
                        NumberField(stringResource(R.string.sm_rule_wave_bump), c, { c = it }, suffix = "%")
                    }
                }
                Text(stringResource(R.string.sm_rule_hint), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(enabled = rule != null, onClick = { rule?.let(onConfirm) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun SetEditDialog(
    set: SetPrescription,
    isEvent: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (SetPrescription) -> Unit
) {
    val (initType, initValue) = when (val l = set.load) {
        is LoadSpec.Absolute -> "ABS" to fmtKg(l.kg)
        is LoadSpec.PercentOneRm -> "PCT" to fmtKg(l.percent)
        is LoadSpec.AtRpe -> "RPE" to fmt(l.rpe, 1)
        is LoadSpec.BodyweightRelative -> "BW" to fmtKg(l.factor)
    }
    var reps by remember { mutableStateOf(set.reps.toString()) }
    var loadType by remember { mutableStateOf(initType) }
    var value by remember { mutableStateOf(initValue) }
    var rest by remember { mutableStateOf(set.restSeconds.toString()) }
    var note by remember { mutableStateOf(set.note) }
    val showEvent = isEvent || set.event != null
    var distance by remember { mutableStateOf(set.event?.distanceM?.let(::fmtKg) ?: "") }
    var height by remember { mutableStateOf(set.event?.heightCm?.let(::fmtKg) ?: "") }
    var implement by remember { mutableStateOf(set.event?.implementKg?.let(::fmtKg) ?: "") }
    var timeCap by remember { mutableStateOf(set.event?.timeCapS?.toString() ?: "") }
    var mode by remember { mutableStateOf(set.event?.mode ?: EventMode.MAX_REPS) }

    val r = reps.trim().toIntOrNull()
    val v = parseDecimal(value)
    val restS = rest.trim().toIntOrNull()
    val load: LoadSpec? = v?.let {
        when (loadType) {
            "PCT" -> LoadSpec.PercentOneRm(it)
            "RPE" -> if (Rpe.isValid(it)) LoadSpec.AtRpe(it) else null
            "BW" -> LoadSpec.BodyweightRelative(it)
            else -> LoadSpec.Absolute(it)
        }
    }
    val valid = r != null && r >= 0 && load != null && restS != null && restS >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_edit_set)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                NumberField(stringResource(R.string.sm_reps), reps, { reps = it }, integer = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ABS" to R.string.sm_load_abs, "PCT" to R.string.sm_load_pct, "RPE" to R.string.sm_load_rpe, "BW" to R.string.sm_load_bw)
                        .forEach { (key, label) -> FilterChip(selected = loadType == key, onClick = { loadType = key }, label = { Text(stringResource(label)) }) }
                }
                NumberField(stringResource(R.string.sm_load_value), value, { value = it })
                NumberField(stringResource(R.string.sm_rest_seconds), rest, { rest = it }, integer = true, suffix = "s")
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.sm_note)) }, singleLine = true)
                if (showEvent) {
                    Text(stringResource(R.string.sm_event_section), style = MaterialTheme.typography.titleSmall)
                    NumberField(stringResource(R.string.sm_distance), distance, { distance = it }, suffix = "m")
                    NumberField(stringResource(R.string.sm_height), height, { height = it }, suffix = "cm")
                    NumberField(stringResource(R.string.sm_implement), implement, { implement = it }, suffix = "kg")
                    NumberField(stringResource(R.string.sm_time_cap), timeCap, { timeCap = it }, integer = true, suffix = "s")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EventMode.entries.forEach { m -> FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(eventModeLabel(m)) }) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSave(
                    SetPrescription(
                        reps = r!!,
                        load = load!!,
                        restSeconds = restS!!,
                        note = note.trim(),
                        event = if (showEvent) EventSpec(
                            distanceM = parseDecimal(distance),
                            heightCm = parseDecimal(height),
                            implementKg = parseDecimal(implement),
                            timeCapS = timeCap.trim().toIntOrNull(),
                            mode = mode
                        ) else null
                    )
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

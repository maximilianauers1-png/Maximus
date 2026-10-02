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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.ExerciseCategory
import app.maximus.strongman.data.ExerciseEntity
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.EventMode
import kotlinx.coroutines.launch

@Composable
fun categoryLabel(category: String): String = when (category) {
    ExerciseCategory.SQUAT -> stringResource(R.string.cat_squat)
    ExerciseCategory.BENCH -> stringResource(R.string.cat_bench)
    ExerciseCategory.DEADLIFT -> stringResource(R.string.cat_deadlift)
    ExerciseCategory.PRESS -> stringResource(R.string.cat_press)
    ExerciseCategory.STONE -> stringResource(R.string.cat_stone)
    ExerciseCategory.CARRY -> stringResource(R.string.cat_carry)
    ExerciseCategory.EVENT -> stringResource(R.string.cat_event)
    else -> stringResource(R.string.cat_accessory)
}

@Composable
fun LibraryTab(repository: StrongmanRepository) {
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var editing by remember { mutableStateOf<ExerciseEntity?>(null) }
    val q = query.trim().lowercase()
    val filtered = exercises.filter { q.isEmpty() || it.nameDe.lowercase().contains(q) || it.nameEn.lowercase().contains(q) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.search)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = { editing = ExerciseEntity(seedKey = null, nameDe = "", nameEn = "", category = ExerciseCategory.EVENT, isEvent = true, defaultMode = EventMode.MAX_REPS.name, notes = "") },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.sm_exercise_new)) }
        }
        item { Text(stringResource(R.string.sm_library_count, exercises.size), style = MaterialTheme.typography.bodySmall) }
        items(filtered, key = { it.id }) { e ->
            Card(modifier = Modifier.fillMaxWidth().clickable { editing = e }) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(e.displayName(), style = MaterialTheme.typography.titleSmall)
                    val mode = runCatching { EventMode.valueOf(e.defaultMode) }.getOrDefault(EventMode.MAX_REPS)
                    Text(
                        listOfNotNull(
                            categoryLabel(e.category),
                            if (e.isEvent) stringResource(R.string.sm_event) else null,
                            if (e.isEvent) eventModeLabel(mode) else null
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (e.notes.isNotBlank()) Text(e.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    editing?.let { e -> ExerciseEditDialog(repository, e) { editing = null } }
}

@Composable
private fun ExerciseEditDialog(repository: StrongmanRepository, initial: ExerciseEntity, onDone: () -> Unit) {
    val scope = rememberCoroutineScope()
    var de by remember { mutableStateOf(initial.nameDe) }
    var en by remember { mutableStateOf(initial.nameEn) }
    var category by remember { mutableStateOf(initial.category) }
    var isEvent by remember { mutableStateOf(initial.isEvent) }
    var mode by remember { mutableStateOf(runCatching { EventMode.valueOf(initial.defaultMode) }.getOrDefault(EventMode.MAX_REPS)) }
    var notes by remember { mutableStateOf(initial.notes) }
    var inUse by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDone,
        title = { Text(stringResource(if (initial.id == 0L) R.string.sm_exercise_new else R.string.sm_exercise_edit)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(de, { de = it }, label = { Text(stringResource(R.string.sm_name_de)) }, singleLine = true)
                OutlinedTextField(en, { en = it }, label = { Text(stringResource(R.string.sm_name_en)) }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExerciseCategory.all.forEach { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(categoryLabel(c)) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEvent, onCheckedChange = { isEvent = it })
                    Text(stringResource(R.string.sm_event))
                }
                if (isEvent) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EventMode.entries.forEach { m -> FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(eventModeLabel(m)) }) }
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.sm_note)) })
                if (inUse) Text(stringResource(R.string.sm_exercise_in_use), color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(enabled = de.isNotBlank() || en.isNotBlank(), onClick = {
                val nameDe = de.trim().ifEmpty { en.trim() }
                val nameEn = en.trim().ifEmpty { de.trim() }
                scope.launch {
                    repository.saveExercise(initial.copy(nameDe = nameDe, nameEn = nameEn, category = category, isEvent = isEvent, defaultMode = mode.name, notes = notes.trim()))
                    onDone()
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row {
                if (initial.id != 0L) {
                    TextButton(onClick = {
                        scope.launch { if (repository.deleteExerciseIfUnused(initial.id)) onDone() else inUse = true }
                    }) { Text(stringResource(R.string.delete)) }
                }
                TextButton(onClick = onDone) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

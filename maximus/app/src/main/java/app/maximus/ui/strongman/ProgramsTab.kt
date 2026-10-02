package app.maximus.ui.strongman

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.ProgramEntity
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.ProgramGenerator
import kotlinx.coroutines.launch

@Composable
fun ProgramsTab(repository: StrongmanRepository, onOpenProgram: (Long) -> Unit) {
    val programs by repository.programs.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var creating by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<ProgramEntity?>(null) }
    var fromTemplate by remember { mutableStateOf<ProgramEntity?>(null) }
    val defaultDayName = stringResource(R.string.sm_day_default, 1)
    val templateSuffix = stringResource(R.string.sm_template_suffix)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Button(onClick = { creating = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.sm_program_new)) }
        }
        val (templates, plans) = programs.partition { it.isTemplate }
        if (plans.isNotEmpty()) item { Text(stringResource(R.string.sm_programs), style = MaterialTheme.typography.titleMedium) }
        items(plans, key = { it.id }) { p ->
            ProgramRow(
                p,
                onOpen = { onOpenProgram(p.id) },
                primaryAction = stringResource(R.string.sm_save_as_template),
                onPrimary = { scope.launch { repository.duplicateProgram(p.id, "${p.name} $templateSuffix", asTemplate = true) } },
                onDelete = { confirmDelete = p }
            )
        }
        if (templates.isNotEmpty()) item { Text(stringResource(R.string.sm_templates), style = MaterialTheme.typography.titleMedium) }
        items(templates, key = { it.id }) { p ->
            ProgramRow(
                p,
                onOpen = { onOpenProgram(p.id) },
                primaryAction = stringResource(R.string.sm_use_template),
                onPrimary = { fromTemplate = p },
                onDelete = { confirmDelete = p }
            )
        }
    }

    if (creating) {
        NewProgramDialog(
            onDismiss = { creating = false },
            onCreate = { name, weeks, deload ->
                creating = false
                scope.launch { onOpenProgram(repository.createProgram(name, weeks, deload, defaultDayName)) }
            }
        )
    }
    fromTemplate?.let { t ->
        var name by remember(t.id) { mutableStateOf(t.name.removeSuffix(" $templateSuffix")) }
        AlertDialog(
            onDismissRequest = { fromTemplate = null },
            title = { Text(stringResource(R.string.sm_use_template)) },
            text = { OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.sm_name)) }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    fromTemplate = null
                    scope.launch { repository.duplicateProgram(t.id, name.ifBlank { t.name }, asTemplate = false)?.let(onOpenProgram) }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = { TextButton(onClick = { fromTemplate = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    confirmDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.sm_delete_program_title)) },
            text = { Text(stringResource(R.string.sm_delete_program_text, p.name)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = null; scope.launch { repository.deleteProgram(p.id) } }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ProgramRow(p: ProgramEntity, onOpen: () -> Unit, primaryAction: String, onPrimary: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(p.name, style = MaterialTheme.typography.titleMedium)
            Text(
                if (p.deloadEvery > 0) stringResource(R.string.sm_program_meta_deload, p.weekCount, p.deloadEvery)
                else stringResource(R.string.sm_program_meta, p.weekCount),
                style = MaterialTheme.typography.bodySmall
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPrimary) { Text(primaryAction) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun NewProgramDialog(onDismiss: () -> Unit, onCreate: (String, Int, Int) -> Unit) {
    val defaultName = stringResource(R.string.sm_program_default_name)
    var name by remember { mutableStateOf(defaultName) }
    var weeks by remember { mutableStateOf("8") }
    var deload by remember { mutableStateOf("4") }
    val w = weeks.trim().toIntOrNull()
    val d = deload.trim().toIntOrNull()
    val valid = name.isNotBlank() && w != null && w in 1..ProgramGenerator.MAX_WEEKS && d != null && d >= 0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sm_program_new)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.sm_name)) }, singleLine = true)
                NumberField(stringResource(R.string.sm_weeks_range), weeks, { weeks = it }, integer = true)
                NumberField(stringResource(R.string.sm_deload_every), deload, { deload = it }, integer = true)
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onCreate(name.trim(), w!!, d!!) }) { Text(stringResource(R.string.create)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

package app.maximus.ui.dnd

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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndCharacterEntity
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
fun CharactersTab(services: AppServices, onOpenSheet: (Long) -> Unit, onEdit: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    val saved by services.dnd.characters.collectAsState(initial = emptyList<DndCharacterEntity>())
    var deleting by remember { mutableStateOf<DndCharacterEntity?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEdit(0L) }, modifier = Modifier.weight(1f)) { Text("New character") }
                OutlinedButton(onClick = {
                    scope.launch {
                        val build = CharacterBuilder.random(Random(System.nanoTime()), level = 1)
                        val sheet = CharacterBuilder.build(build)
                        val id = services.dnd.saveCharacter(0L, "Random hero", summaryOf(sheet), DndCodec.encode(build))
                        onEdit(id)
                    }
                }) { Text("Roll a random one") }
            }
        }
        if (saved.isEmpty()) {
            item { EmptyState("No characters yet. Create one, or roll a random build and edit it.") }
        }
        items(saved, key = { it.id }) { c ->
            PlateCard(onClick = { onOpenSheet(c.id) }) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(c.name.ifBlank { "Unnamed" }, style = MaterialTheme.typography.titleLarge)
                    Text(c.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        TextButton(onClick = { onOpenSheet(c.id) }) { Text("Open sheet") }
                        TextButton(onClick = { onEdit(c.id) }) { Text("Edit") }
                        TextButton(onClick = { deleting = c }) { Text("Delete") }
                    }
                }
            }
        }
    }

    deleting?.let { c ->
        ConfirmDialog(
            title = "Delete ${c.name.ifBlank { "this character" }}?",
            text = "The character and everything on its sheet are removed permanently.",
            confirmLabel = "Delete",
            onConfirm = { scope.launch { services.dnd.deleteCharacter(c.id) } },
            onDismiss = { deleting = null }
        )
    }
}

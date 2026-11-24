@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.Foods
import app.maximus.nutrition.domain.MealType
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.RecipeTag
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.parseDecimal

/**
 * Create or edit an own recipe (e.g. one the coach sends): ingredients come from the food database, so
 * nutrients are computed exactly like for the built-in recipes and the recipe works in planner and week store.
 */
@Composable
fun RecipeEditorDialog(initial: Recipe?, newKey: () -> String, onSave: (Recipe) -> Unit, onDismiss: () -> Unit) {
    val de = isGerman()
    var name by remember { mutableStateOf(initial?.nameDe ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: MealType.MAIN) }
    var minutes by remember { mutableStateOf(initial?.minutes?.toString() ?: "20") }
    var cut by remember { mutableStateOf(initial?.let { RecipeTag.FAT_LOSS in it.tags } ?: false) }
    var protein by remember { mutableStateOf(initial?.let { RecipeTag.HIGH_PROTEIN in it.tags } ?: false) }
    var veg by remember { mutableStateOf(initial?.let { RecipeTag.VEGETARIAN in it.tags } ?: false) }
    var steps by remember { mutableStateOf(initial?.stepsDe?.joinToString("\n") ?: "") }
    val items = remember { mutableStateListOf<Pair<String, String>>().apply { initial?.items?.forEach { (k, g) -> add(k to fmt(g, 0)) } } }
    var search by remember { mutableStateOf("") }

    val parsed = items.mapNotNull { (k, g) -> parseDecimal(g)?.takeIf { it > 0 }?.let { k to it } }
    val preview = Recipe("preview", name, name, type, 0, emptySet(), parsed, listOf(""), listOf(""))
    val valid = name.isNotBlank() && parsed.isNotEmpty() && parsed.size == items.size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.nr_new else R.string.nr_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.nr_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MealType.entries.forEach { t -> FilterChip(type == t, { type = t }, label = { Text(mealTypeLabel(t)) }) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(cut, { cut = !cut }, label = { Text(stringResource(R.string.nu_tag_cut)) })
                    FilterChip(protein, { protein = !protein }, label = { Text(stringResource(R.string.nu_tag_protein)) })
                    FilterChip(veg, { veg = !veg }, label = { Text(stringResource(R.string.nu_tag_veg)) })
                }
                NumberField(stringResource(R.string.nu_minutes), minutes, { minutes = it }, integer = true, suffix = "min")

                Text(stringResource(R.string.nu_ingredients), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
                items.forEachIndexed { i, (k, g) ->
                    val f = Foods.byKey[k]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(f?.let { if (de) it.nameDe else it.nameEn } ?: k, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        NumberField("g", g, { items[i] = k to it }, Modifier.width(96.dp))
                        GlyphButton(Glyph.TRASH, stringResource(R.string.delete), { items.removeAt(i) }, tint = MaterialTheme.colorScheme.outline)
                    }
                }
                OutlinedTextField(search, { search = it }, label = { Text(stringResource(R.string.nr_add_food)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val q = search.trim().lowercase()
                if (q.length >= 2) {
                    Foods.all.filter { (if (de) it.nameDe else it.nameEn).lowercase().contains(q) }.take(8).forEach { f ->
                        Text(
                            "+ " + (if (de) f.nameDe else f.nameEn),
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().clickable { items.add(f.key to "100"); search = "" }.padding(vertical = 6.dp)
                        )
                    }
                }
                if (parsed.isNotEmpty()) Text(macroLine(preview.computed, withFiber = true), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(steps, { steps = it }, label = { Text(stringResource(R.string.nr_steps_hint)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val tags = buildSet {
                    add(RecipeTag.CUSTOM)
                    if (cut) add(RecipeTag.FAT_LOSS)
                    if (protein) add(RecipeTag.HIGH_PROTEIN)
                    if (veg) add(RecipeTag.VEGETARIAN)
                }
                val st = steps.lines().map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf("–") }
                onSave(Recipe(initial?.key ?: newKey(), name.trim(), name.trim(), type, minutes.trim().toIntOrNull() ?: 0, tags, parsed, st, st))
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

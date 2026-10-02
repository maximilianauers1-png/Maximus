@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.Foods
import app.maximus.nutrition.domain.MealType
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.RecipeTag
import app.maximus.nutrition.domain.Recipes
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.components.SteelRule
import app.maximus.ui.strongman.fmt

@Composable
fun tagLabel(t: RecipeTag) = stringResource(
    when (t) {
        RecipeTag.HIGH_PROTEIN -> R.string.nu_tag_protein
        RecipeTag.VEGETARIAN -> R.string.nu_tag_veg
        RecipeTag.MEAL_PREP -> R.string.nu_tag_prep
        RecipeTag.QUICK -> R.string.nu_tag_quick
        RecipeTag.CLASSIC_GERMAN -> R.string.nu_tag_german
        RecipeTag.MASS_GAIN -> R.string.nu_tag_mass
    }
)

@Composable
fun RecipesTab(p: NutritionProfile, onSave: (NutritionProfile) -> Unit) {
    val de = isGerman()
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<MealType?>(null) }
    var tag by rememberSaveable { mutableStateOf<RecipeTag?>(null) }
    var open by remember { mutableStateOf<Recipe?>(null) }
    val q = query.trim().lowercase()
    val list = Recipes.all.filter { r ->
        (type == null || r.type == type) && (tag == null || tag in r.tags) &&
            (q.isEmpty() || r.name(de).lowercase().contains(q) || r.items.any { (k, _) -> Foods.byKey.getValue(k).let { f -> (if (de) f.nameDe else f.nameEn).lowercase().contains(q) } })
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.nu_recipe_search)) }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MealType.entries.forEach { t -> FilterChip(type == t, { type = if (type == t) null else t }, label = { Text(mealTypeLabel(t)) }) }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RecipeTag.entries.forEach { t -> FilterChip(tag == t, { tag = if (tag == t) null else t }, label = { Text(tagLabel(t)) }) }
            }
        }
        if (list.isEmpty()) item { EmptyState(stringResource(R.string.nu_no_recipes)) }
        items(list, key = { it.key }) { r ->
            val n = r.nutrients
            val excluded = r.key in p.plan.excluded
            PlateCard(onClick = { open = r }) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(r.name(de), style = MaterialTheme.typography.titleLarge, color = if (excluded) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${mealTypeLabel(r.type)}   ${r.minutes} min   " + macroLine(n),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (excluded) Text(stringResource(R.string.nu_excluded), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
    open?.let { r ->
        RecipeDialog(r, 1.0, excluded = r.key in p.plan.excluded,
            onToggleExclude = { ex -> onSave(p.copy(plan = p.plan.copy(excluded = if (ex) p.plan.excluded + r.key else p.plan.excluded - r.key))) },
            onDismiss = { open = null })
    }
}

/** Recipe with adjustable portions: every quantity and nutrient scales linearly with the portion factor. */
@Composable
fun RecipeDialog(r: Recipe, initialPortions: Double, excluded: Boolean, onToggleExclude: (Boolean) -> Unit, onDismiss: () -> Unit) {
    val de = isGerman()
    var portions by remember(r.key) { mutableDoubleStateOf(initialPortions) }
    var ex by remember(r.key, excluded) { mutableStateOf(excluded) }
    val n = r.nutrients * portions
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(r.name(de), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${mealTypeLabel(r.type)}   ${r.minutes} min   " + r.tags.map { tagLabel(it) }.joinToString("  "),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.nu_portions), modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { portions = (portions - 0.25).coerceAtLeast(0.25) }) { Text("−") }
                    Text(fmt(portions, 2), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 10.dp))
                    OutlinedButton(onClick = { portions = (portions + 0.25).coerceAtMost(12.0) }) { Text("+") }
                }
                Text(
                    macroLine(n, withFiber = true),
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary
                )
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 6.dp))
                Text(stringResource(R.string.nu_ingredients), style = MaterialTheme.typography.titleLarge)
                r.items.forEach { (k, g) ->
                    val f = Foods.byKey.getValue(k)
                    Row {
                        Text("${fmt(g * portions, 0)} g", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(70.dp))
                        Text(if (de) f.nameDe else f.nameEn, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(stringResource(R.string.nu_spices_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 6.dp))
                Text(stringResource(R.string.nu_steps), style = MaterialTheme.typography.titleLarge)
                r.steps(de).forEachIndexed { i, s ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("${i + 1}.", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(24.dp))
                        Text(s, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Checkbox(ex, { ex = it; onToggleExclude(it) })
                    Text(stringResource(R.string.nu_exclude_from_plan))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

/** "812 kcal  P 52  F 30  C 78 g" with localised macro letters. */
@Composable
fun macroLine(n: app.maximus.nutrition.domain.Nutrients, withFiber: Boolean = false): String {
    val base = stringResource(R.string.nu_macro_line, fmt(n.kcal, 0), fmt(n.proteinG, 0), fmt(n.fatG, 0), fmt(n.carbG, 0))
    return if (withFiber) base + "  " + stringResource(R.string.nu_fiber_short, fmt(n.fiberG, 0)) else base
}

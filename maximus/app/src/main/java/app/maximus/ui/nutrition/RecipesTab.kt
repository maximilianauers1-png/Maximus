@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.Foods
import app.maximus.nutrition.domain.MealType
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.RecipeTag
import app.maximus.nutrition.domain.Recipes
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.fmt

@Composable
fun tagLabel(tag: RecipeTag) = stringResource(
    when (tag) {
        RecipeTag.HIGH_PROTEIN -> R.string.nu_tag_protein
        RecipeTag.VEGETARIAN -> R.string.nu_tag_veg
        RecipeTag.MEAL_PREP -> R.string.nu_tag_prep
        RecipeTag.QUICK -> R.string.nu_tag_quick
        RecipeTag.CLASSIC_GERMAN -> R.string.nu_tag_german
        RecipeTag.MASS_GAIN -> R.string.nu_tag_mass
    }
)

private fun Recipe.matches(query: String, de: Boolean): Boolean {
    if (query.isBlank()) return true
    return name(de).contains(query, ignoreCase = true) ||
        items.any { (key, _) -> Foods.byKey.getValue(key).name(de).contains(query, ignoreCase = true) }
}

@Composable
internal fun RecipesTab(
    excluded: Set<String>,
    onExclude: (String, Boolean) -> Unit,
    onLog: (Recipe, Double) -> Unit,
    onPrep: () -> Unit
) {
    val de = isGerman()
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf<MealType?>(null) }
    var tag by rememberSaveable { mutableStateOf<RecipeTag?>(null) }
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val recipes = remember(query, type, tag, de) {
        Recipes.all.filter { recipe ->
            (type == null || recipe.type == type) && (tag == null || tag in recipe.tags) && recipe.matches(query.trim(), de)
        }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { NutritionHeading(stringResource(R.string.nu_tab_kitchen)) }
        item { NutritionShortcut(stringResource(R.string.nu_batch_prep), stringResource(R.string.nu_batch_shortcut), Glyph.BOWL, onPrep) }
        item {
            OutlinedTextField(
                query, { query = it }, label = { Text(stringResource(R.string.nu_recipe_search)) },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MealType.entries.forEach { filter ->
                    FilterChip(type == filter, { type = if (type == filter) null else filter }, label = { Text(mealTypeLabel(filter)) })
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RecipeTag.entries.forEach { filter ->
                    FilterChip(tag == filter, { tag = if (tag == filter) null else filter }, label = { Text(tagLabel(filter)) })
                }
            }
        }
        if (recipes.isEmpty()) item { EmptyState(stringResource(R.string.nu_no_recipes)) }
        items(recipes, key = { it.key }) { recipe ->
            PlateCard(onClick = { open = recipe.key }) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(recipe.name(de), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.nu_recipe_meta, mealTypeLabel(recipe.type), recipe.minutes),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary
                        )
                        Text(macroLine(recipe.nutrients), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (recipe.key in excluded) Text(
                            stringResource(R.string.nu_excluded), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    GlyphIcon(Glyph.CHEVRON_RIGHT, tint = MaterialTheme.colorScheme.outline, size = 18.dp)
                }
            }
        }
    }
    open?.let(Recipes.byKey::get)?.let { recipe ->
        RecipeDialog(
            recipe, initialPortions = 1.0,
            excludeLabel = stringResource(if (recipe.key in excluded) R.string.nu_include_in_plan else R.string.nu_exclude_from_plan),
            onExclude = { onExclude(recipe.key, recipe.key !in excluded) },
            logLabel = stringResource(R.string.nu_log_recipe_today),
            onLog = { portions -> onLog(recipe, portions); open = null },
            onDismiss = { open = null }
        )
    }
}

@Composable
internal fun RecipeDialog(
    recipe: Recipe,
    initialPortions: Double,
    breakdown: String? = null,
    isBatch: Boolean = false,
    readOnly: Boolean = false,
    logLabel: String = "",
    onLog: ((Double) -> Unit)? = null,
    onApplyPortions: ((Double) -> Unit)? = null,
    onSwap: (() -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    excludeLabel: String = "",
    onExclude: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val de = isGerman()
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var portions by rememberSaveable(recipe.key, initialPortions) { mutableDoubleStateOf(initialPortions) }
    val maxPortions = maxOf(12.0, initialPortions)
    val n = recipe.nutrients * portions

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = (LocalConfiguration.current.screenHeightDp * 0.80f).dp)
                .verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NutritionHeading(recipe.name(de), stringResource(R.string.nu_recipe_meta, mealTypeLabel(recipe.type), recipe.minutes))
            if (recipe.tags.isNotEmpty()) {
                Text(recipe.tags.joinToStringLocal(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (breakdown != null) Text(breakdown, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(if (isBatch) R.string.nu_batch_portions else R.string.nu_portions),
                    style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f)
                )
                if (!readOnly) PortionButton("−", R.string.nu_fewer_portions, portions > 0.25) { portions = (portions - 0.25).coerceAtLeast(0.25) }
                Text(fmt(portions, 2), style = MaterialTheme.typography.titleMedium)
                if (!readOnly) PortionButton("+", R.string.nu_more_portions, portions < maxPortions) { portions = (portions + 0.25).coerceAtMost(maxPortions) }
            }
            Text(macroLine(n, withFiber = true), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            if (onLog != null) {
                Button(onClick = { onLog(portions) }, modifier = Modifier.fillMaxWidth()) { Text(logLabel) }
            }
            if (onApplyPortions != null || onSwap != null || onReset != null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onApplyPortions != null) {
                        OutlinedButton(enabled = portions != initialPortions, onClick = { onApplyPortions(portions) }) {
                            Text(stringResource(R.string.nu_apply_portions))
                        }
                    }
                    if (onSwap != null) OutlinedButton(onClick = onSwap) { Text(stringResource(R.string.nu_swap_recipe)) }
                    if (onReset != null) TextButton(onClick = onReset) { Text(stringResource(R.string.nu_reset_meal)) }
                }
            }
            NutritionDivider()
            Text(stringResource(R.string.nu_ingredients), style = MaterialTheme.typography.titleMedium)
            recipe.items.forEach { (key, grams) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(Foods.byKey.getValue(key).name(de), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("${fmt(grams * portions, 0)} g", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Text(stringResource(R.string.nu_spices_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            NutritionDivider()
            Text(stringResource(R.string.nu_steps), style = MaterialTheme.typography.titleMedium)
            recipe.steps(de).forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${index + 1}.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Text(step, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (isBatch) Text(
                stringResource(R.string.nu_scaled_method), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (onExclude != null) TextButton(onClick = onExclude) { Text(excludeLabel) }
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    }
}

@Composable
private fun PortionButton(symbol: String, description: Int, enabled: Boolean, onClick: () -> Unit) {
    val label = stringResource(description)
    OutlinedButton(
        onClick = onClick, enabled = enabled, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.semantics { contentDescription = label }
    ) { Text(symbol) }
}

@Composable
internal fun SwapRecipeDialog(recipes: List<Recipe>, onSelect: (Recipe) -> Unit, onDismiss: () -> Unit) {
    val de = isGerman()
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(recipes, query, de) { recipes.filter { it.matches(query.trim(), de) }.sortedBy { it.name(de) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nu_swap_recipe)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.nu_recipe_search)) }, singleLine = true)
                if (shown.isEmpty()) Text(stringResource(R.string.nu_no_recipes))
                LazyColumn(Modifier.heightIn(max = 280.dp)) {
                    items(shown, key = { it.key }) { recipe ->
                        Column(
                            Modifier.fillMaxWidth().clickable { onSelect(recipe) }.padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(recipe.name(de), style = MaterialTheme.typography.titleSmall)
                            Text(macroLine(recipe.nutrients), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        NutritionDivider()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun Set<RecipeTag>.joinToStringLocal(): String = map { tagLabel(it) }.joinToString("  ·  ")

@Composable
fun macroLine(n: Nutrients, withFiber: Boolean = false): String {
    val base = stringResource(R.string.nu_macro_line, fmt(n.kcal, 0), fmt(n.proteinG, 0), fmt(n.fatG, 0), fmt(n.carbG, 0))
    return if (withFiber) base + "  " + stringResource(R.string.nu_fiber_short, fmt(n.fiberG, 0)) else base
}

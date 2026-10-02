@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.DerivedTargets
import app.maximus.nutrition.domain.MealPlanner
import app.maximus.nutrition.domain.MealType
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.PlannedMeal
import app.maximus.nutrition.domain.WeekPlan
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import java.time.DayOfWeek
import java.time.format.TextStyle as JTextStyle

@Composable
fun mealTypeLabel(t: MealType) = stringResource(
    when (t) { MealType.BREAKFAST -> R.string.nu_type_breakfast; MealType.MAIN -> R.string.nu_type_main; MealType.SNACK -> R.string.nu_type_snack }
)

@Composable
fun PlanTab(p: NutritionProfile, d: DerivedTargets, onSave: (NutritionProfile) -> Unit) {
    val de = isGerman()
    val locale = LocalConfiguration.current.locales[0]
    val plan: WeekPlan? = remember(d.macros, p.plan) { MealPlanner.plan(d.macros, p.plan) }
    var distinct by remember(p.plan.distinctRecipes) { mutableFloatStateOf(p.plan.distinctRecipes.toFloat()) }
    var openMeal by remember { mutableStateOf<PlannedMeal?>(null) }
    var shopping by remember { mutableStateOf(false) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard(stringResource(R.string.nu_plan_settings)) {
                Text(stringResource(R.string.nu_meals_per_day), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (MealPlanner.MIN_MEALS..MealPlanner.MAX_MEALS).forEach { n ->
                        FilterChip(p.plan.mealsPerDay == n, { onSave(p.copy(plan = p.plan.copy(mealsPerDay = n))) }, label = { Text("$n") })
                    }
                }
                Text(stringResource(R.string.nu_distinct, distinct.toInt(), 7 * p.plan.mealsPerDay), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Slider(
                    value = distinct, onValueChange = { distinct = it },
                    onValueChangeFinished = { onSave(p.copy(plan = p.plan.copy(distinctRecipes = distinct.toInt()))) },
                    valueRange = MealPlanner.MIN_DISTINCT.toFloat()..MealPlanner.MAX_DISTINCT.toFloat(),
                    steps = MealPlanner.MAX_DISTINCT - MealPlanner.MIN_DISTINCT - 1
                )
                Text(stringResource(R.string.nu_distinct_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.nu_vegetarian), modifier = Modifier.weight(1f))
                    Switch(p.plan.vegetarianOnly, { onSave(p.copy(plan = p.plan.copy(vegetarianOnly = it))) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSave(p.copy(plan = p.plan.copy(seed = p.plan.seed + 1))) }) { Text(stringResource(R.string.nu_reroll)) }
                    OutlinedButton(enabled = plan != null, onClick = { shopping = true }) { Text(stringResource(R.string.nu_shopping)) }
                }
            }
        }
        if (plan == null) {
            item { EmptyState(stringResource(R.string.nu_plan_impossible)) }
        } else {
            item {
                val avg = plan.days.fold(Nutrients.ZERO) { a, day -> a + day.totals } * (1.0 / plan.days.size)
                SectionCard(stringResource(R.string.nu_week_average)) {
                    ResultLine(stringResource(R.string.nu_energy), "${fmt(avg.kcal, 0)} / ${fmt(d.macros.kcal, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_protein), "${fmt(avg.proteinG, 0)} / ${fmt(d.macros.proteinG, 0)} g")
                    ResultLine(stringResource(R.string.nu_fat), "${fmt(avg.fatG, 0)} / ${fmt(d.macros.fatG, 0)} g")
                    ResultLine(stringResource(R.string.nu_carbs), "${fmt(avg.carbG, 0)} / ${fmt(maxOf(0.0, d.macros.carbG), 0)} g")
                    ResultLine(stringResource(R.string.nu_distinct_used), "${plan.distinctRecipes}")
                }
            }
            itemsIndexed(plan.days) { i, day ->
                PlateCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(DayOfWeek.of(i + 1).getDisplayName(JTextStyle.FULL, locale), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            Text("${fmt(day.totals.kcal, 0)} kcal · ${fmt(day.totals.proteinG, 0)} g P", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        day.meals.forEach { m ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { openMeal = m }.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mealTypeLabel(m.type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(82.dp))
                                Text(m.recipe.name(de), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text("× ${fmt(m.portions, 2)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    openMeal?.let { m ->
        RecipeDialog(m.recipe, m.portions, excluded = m.recipe.key in p.plan.excluded,
            onToggleExclude = { ex -> onSave(p.copy(plan = p.plan.copy(excluded = if (ex) p.plan.excluded + m.recipe.key else p.plan.excluded - m.recipe.key))) },
            onDismiss = { openMeal = null })
    }
    if (shopping && plan != null) ShoppingDialog(plan) { shopping = false }
}

@Composable
private fun ShoppingDialog(plan: WeekPlan, onDismiss: () -> Unit) {
    val de = isGerman()
    val items = remember(plan) { MealPlanner.shoppingList(plan) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nu_shopping)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                var lastCat: Any? = null
                items.forEach { item ->
                    if (item.food.category != lastCat) {
                        lastCat = item.food.category
                        Text(foodCategoryLabel(item.food.category), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                    }
                    Row {
                        Text(if (de) item.food.nameDe else item.food.nameEn, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(gramsLabel(item.grams), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(stringResource(R.string.nu_shopping_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

/** Rounded up to 10 g below 1 kg, else to 0.05 kg: shopping quantities, not lab values. */
fun gramsLabel(g: Double): String = if (g < 1000) "${(Math.ceil(g / 10.0) * 10).toInt()} g" else "${fmt(Math.ceil(g / 50.0) * 0.05, 2)} kg"

@Composable
fun foodCategoryLabel(c: app.maximus.nutrition.domain.FoodCategory) = stringResource(
    when (c) {
        app.maximus.nutrition.domain.FoodCategory.PRODUCE -> R.string.nu_cat_produce
        app.maximus.nutrition.domain.FoodCategory.DAIRY_EGGS -> R.string.nu_cat_dairy
        app.maximus.nutrition.domain.FoodCategory.MEAT_FISH -> R.string.nu_cat_meat
        app.maximus.nutrition.domain.FoodCategory.GRAINS -> R.string.nu_cat_grains
        app.maximus.nutrition.domain.FoodCategory.LEGUMES -> R.string.nu_cat_legumes
        app.maximus.nutrition.domain.FoodCategory.NUTS_OILS -> R.string.nu_cat_nuts
        app.maximus.nutrition.domain.FoodCategory.PANTRY -> R.string.nu_cat_pantry
    }
)

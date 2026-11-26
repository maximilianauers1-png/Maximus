@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.LoggedMeal
import app.maximus.nutrition.data.plannedMealId
import app.maximus.nutrition.domain.MacroTargets
import app.maximus.nutrition.domain.MealPlanner
import app.maximus.nutrition.domain.MealType
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.PlanSettings
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.nutrition.domain.WeekPlan
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.fmt
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

@Composable
fun mealTypeLabel(type: MealType) = stringResource(
    when (type) {
        MealType.BREAKFAST -> R.string.nu_type_breakfast
        MealType.MAIN -> R.string.nu_type_main
        MealType.SNACK -> R.string.nu_type_snack
    }
)

@Composable
internal fun weekRange(start: LocalDate, end: LocalDate = start.plusDays(6)): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(LocalConfiguration.current.locales[0])
    return "${start.format(formatter)} – ${end.format(formatter)}"
}

@Composable
internal fun PlanTab(
    weekStart: LocalDate,
    weekOffset: Int,
    today: LocalDate,
    onWeekChange: (Int) -> Unit,
    settings: PlanSettings,
    goals: List<MacroTargets>,
    plan: WeekPlan?,
    loading: Boolean,
    logged: List<LoggedMeal>,
    book: RecipeBook,
    onEdit: ((PlanSettings) -> PlanSettings) -> Unit,
    onReroll: () -> Unit,
    onShopping: () -> Unit,
    onSavedWeek: () -> Unit,
    onMeal: (Int, Int) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    var day by rememberSaveable(weekStart.toEpochDay()) { mutableIntStateOf(if (weekOffset == 0) today.dayOfWeek.value - 1 else 0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val dayListState = key(weekStart) { rememberLazyListState(initialFirstVisibleItemIndex = (day - 3).coerceAtLeast(0)) }
    val selectedDate = weekStart.plusDays(day.toLong())
    val planned = plan?.days?.get(day)
    val byId = remember(logged) { logged.associateBy { it.id } }
    val target = goals[day]

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            NutritionHeading(stringResource(R.string.nu_tab_plan))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CHEVRON_LEFT, stringResource(R.string.nu_previous_week), { onWeekChange(0) }, enabled = weekOffset > 0)
                Text(weekRange(weekStart), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                GlyphButton(Glyph.CHEVRON_RIGHT, stringResource(R.string.nu_next_week), { onWeekChange(1) }, enabled = weekOffset == 0)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onShopping, enabled = plan != null) { Text(stringResource(R.string.nu_shopping)) }
                OutlinedButton(onClick = onSavedWeek) { Text(stringResource(R.string.nu_saved_week)) }
                OutlinedButton(onClick = { showSettings = !showSettings }) {
                    Text(stringResource(if (showSettings) R.string.nu_hide_setup else R.string.nu_plan_settings))
                }
            }
        }
        if (showSettings) item { PlannerSettings(settings, goals, onEdit, onReroll) }
        item {
            LazyRow(state = dayListState, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(7) { index ->
                    val date = weekStart.plusDays(index.toLong())
                    FilterChip(
                        selected = day == index,
                        onClick = { day = index },
                        label = { Text("${date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} ${date.dayOfMonth}") }
                    )
                }
            }
        }
        item {
            NutritionCard(selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)) {
                if (settings.trainingDays.isNotEmpty()) {
                    val training = (day + 1) in settings.trainingDays
                    Text(
                        stringResource(if (training) R.string.nu_training_day else R.string.nu_rest_day),
                        color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(
                    stringResource(R.string.nu_day_targets, fmt(target.kcal, 0), fmt(target.proteinG, 0), fmt(target.carbG.coerceAtLeast(0.0), 0)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall
                )
                if (planned != null) Text(macroLine(planned.totals), style = MaterialTheme.typography.titleSmall)
            }
        }
        if (plan == null || planned == null) {
            if (!loading) item { EmptyState(stringResource(R.string.nu_plan_impossible)) }
        } else {
            itemsIndexed(planned.meals) { slot, meal ->
                MealRow(meal, byId[plannedMealId(selectedDate.toEpochDay(), slot)], book) { onMeal(day, slot) }
            }
            item {
                val avg = plan.days.fold(Nutrients.ZERO) { sum, day -> sum + day.totals } * (1.0 / plan.days.size)
                fun average(value: (MacroTargets) -> Double) = goals.sumOf(value) / goals.size
                NutritionCard(stringResource(R.string.nu_week_average)) {
                    ResultLine(stringResource(R.string.nu_energy), "${fmt(avg.kcal, 0)} / ${fmt(average { it.kcal }, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_protein), "${fmt(avg.proteinG, 0)} / ${fmt(average { it.proteinG }, 0)} g")
                    ResultLine(stringResource(R.string.nu_fat), "${fmt(avg.fatG, 0)} / ${fmt(average { it.fatG }, 0)} g")
                    ResultLine(stringResource(R.string.nu_carbs), "${fmt(avg.carbG, 0)} / ${fmt(average { it.carbG }.coerceAtLeast(0.0), 0)} g")
                    ResultLine(stringResource(R.string.nu_distinct_used), plan.distinctRecipes.toString())
                }
            }
        }
    }
}

@Composable
private fun PlannerSettings(settings: PlanSettings, goals: List<MacroTargets>, onEdit: ((PlanSettings) -> PlanSettings) -> Unit, onReroll: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    var distinct by remember(settings.distinctRecipes) { mutableFloatStateOf(settings.distinctRecipes.toFloat()) }
    var boost by remember(settings.trainingCarbBoostG) { mutableFloatStateOf(settings.trainingCarbBoostG.toFloat()) }
    NutritionCard(stringResource(R.string.nu_plan_settings)) {
        Text(stringResource(R.string.nu_meals_per_day), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (MealPlanner.MIN_MEALS..MealPlanner.MAX_MEALS).forEach { count ->
                FilterChip(settings.mealsPerDay == count, { onEdit { it.copy(mealsPerDay = count) } }, label = { Text(count.toString()) })
            }
        }
        Text(stringResource(R.string.nu_distinct, distinct.toInt(), 7 * settings.mealsPerDay), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = distinct, onValueChange = { distinct = it },
            onValueChangeFinished = {
                val selected = distinct.toInt()
                onEdit { it.copy(distinctRecipes = selected) }
            },
            valueRange = MealPlanner.MIN_DISTINCT.toFloat()..MealPlanner.MAX_DISTINCT.toFloat(),
            steps = MealPlanner.MAX_DISTINCT - MealPlanner.MIN_DISTINCT - 1
        )
        Text(stringResource(R.string.nu_distinct_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.nu_vegetarian), Modifier.weight(1f))
            Switch(settings.vegetarianOnly, { enabled -> onEdit { it.copy(vegetarianOnly = enabled) } })
        }
        NutritionDivider()
        Text(stringResource(R.string.nu_training_days), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day.value in settings.trainingDays,
                    onClick = { onEdit { s ->
                        val days = if (day.value in s.trainingDays) s.trainingDays - day.value else s.trainingDays + day.value
                        s.copy(trainingDays = days)
                    } },
                    label = { Text(day.getDisplayName(TextStyle.SHORT, locale)) }
                )
            }
        }
        Text(stringResource(R.string.nu_carb_boost, boost.toInt()), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = boost, onValueChange = { boost = it },
            onValueChangeFinished = {
                val selected = boost.toInt()
                onEdit { it.copy(trainingCarbBoostG = selected) }
            },
            valueRange = 0f..MealPlanner.MAX_TRAINING_CARB_BOOST.toFloat(),
            steps = 9
        )
        Text(stringResource(R.string.nu_carb_boost_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (settings.trainingCarbBoostG > 0 && goals.map { it.carbG }.distinct().size <= 1) {
            Text(stringResource(R.string.nu_carb_boost_unavailable), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
        }
        OutlinedButton(onClick = onReroll) { Text(stringResource(R.string.nu_reroll)) }
    }
}

package app.maximus.ui.nutrition

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.LoggedMeal
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.nutrition.data.plannedMealId
import app.maximus.nutrition.domain.MacroTargets
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.PlannedDay
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.strongman.fmt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TodayTab(
    day: LocalDate,
    training: Boolean?,
    workoutLogged: Boolean,
    target: MacroTargets,
    planned: PlannedDay?,
    loading: Boolean,
    checkIn: NutritionLogEntity?,
    meals: List<LoggedMeal>,
    checkout: Pair<Int, Int>,
    goalWeight: Double?,
    latestWeight: Double?,
    onMeal: (Int) -> Unit,
    navigate: (NutritionPage) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val totals = remember(meals) { meals.fold(Nutrients.ZERO) { sum, meal -> sum + meal.nutrients } }
    val loggedById = remember(meals) { meals.associateBy { it.id } }
    val date = day.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    val calories = checkIn?.kcal ?: totals.kcal.takeIf { meals.isNotEmpty() }
    val protein = checkIn?.proteinG ?: totals.proteinG.takeIf { meals.isNotEmpty() }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TodayHero(
                date = date,
                dayType = when {
                    workoutLogged -> stringResource(R.string.nu_workout_logged)
                    training == true -> stringResource(R.string.nu_training_day)
                    training == false -> stringResource(R.string.nu_rest_day)
                    else -> null
                }
            )
        }
        item {
            NutritionCard(stringResource(R.string.nu_intake_today)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    DailyMetric(stringResource(R.string.nu_energy), calories, target.kcal, "kcal", Modifier.weight(1f))
                    DailyMetric(stringResource(R.string.nu_protein), protein, target.proteinG, "g", Modifier.weight(1f))
                }
                NutritionDivider()
                Text(
                    stringResource(R.string.nu_other_macro_targets, fmt(target.carbG.coerceAtLeast(0.0), 0), fmt(target.fatG, 0)),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            NutritionShortcut(
                stringResource(R.string.nu_shopping),
                if (checkout.second == 0) stringResource(R.string.nu_shopping_from_plan)
                else stringResource(R.string.nu_shopping_progress, checkout.first, checkout.second),
                Glyph.BAG
            ) { navigate(NutritionPage.SHOPPING) }
        }
        item {
            NutritionShortcut(
                stringResource(R.string.nu_batch_prep), stringResource(R.string.nu_batch_shortcut), Glyph.BOWL
            ) { navigate(NutritionPage.PREP) }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.nu_today_meals), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = { navigate(NutritionPage.PLAN) }) { Text(stringResource(R.string.nu_open_week)) }
            }
        }
        if (planned == null) {
            if (!loading) item { EmptyState(stringResource(R.string.nu_plan_impossible)) }
        } else {
            itemsIndexed(planned.meals) { slot, meal ->
                MealRow(meal, loggedById[plannedMealId(day.toEpochDay(), slot)]) { onMeal(slot) }
            }
        }
        item {
            val summary = listOfNotNull(
                latestWeight?.let { stringResource(R.string.nu_latest_weight, fmt(it, 1)) },
                goalWeight?.let { stringResource(R.string.nu_target_weight, fmt(it, 1)) }
            ).joinToString("  ·  ")
            NutritionShortcut(
                stringResource(R.string.nu_weight_trend),
                summary.ifEmpty { stringResource(R.string.nu_open_checkin) },
                Glyph.PULSE
            ) { navigate(NutritionPage.JOURNAL) }
        }
    }
}

@Composable
private fun TodayHero(date: String, dayType: String?) {
    Box(Modifier.fillMaxWidth().heightIn(min = 158.dp).clip(RoundedCornerShape(20.dp))) {
        Image(
            painterResource(R.drawable.nutrition_hero), contentDescription = null,
            modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(0f to Color(0xF6131D15), 0.58f to Color(0xDB131D15), 1f to Color(0x44131D15))
            )
        )
        Column(
            Modifier.fillMaxWidth(0.76f).padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(date, style = MaterialTheme.typography.labelMedium, color = Color(0xFFD4DDCF))
            Text(stringResource(R.string.cal_today), style = MaterialTheme.typography.displaySmall, color = Color(0xFFF3F7EF))
            if (dayType != null) Text(dayType, style = MaterialTheme.typography.labelLarge, color = Color(0xFFC7E3B7))
        }
    }
}

@Composable
private fun DailyMetric(label: String, actual: Double?, target: Double, unit: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
        Text(actual?.let { "${fmt(it, 0)} $unit" } ?: "—", style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
        LinearProgressIndicator(
            progress = { if (target > 0) ((actual ?: 0.0) / target).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth(), color = colors.primary, trackColor = colors.surfaceVariant
        )
        Text(
            stringResource(R.string.nu_metric_target, fmt(target, 0), unit),
            style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant
        )
    }
}

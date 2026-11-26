package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.MealPlanner
import app.maximus.nutrition.domain.PrepBatch
import app.maximus.nutrition.domain.WeekPlan
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.strongman.fmt
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
internal fun PrepTab(weekStart: LocalDate, today: LocalDate, plan: WeekPlan?, loading: Boolean) {
    val de = isGerman()
    val locale = LocalConfiguration.current.locales[0]
    var window by rememberSaveable(weekStart.toEpochDay()) {
        mutableIntStateOf(if (today >= weekStart.plusDays(3) && today <= weekStart.plusDays(6)) 1 else 0)
    }
    var open by remember { mutableStateOf<PrepBatch?>(null) }
    val dayRanges = listOf(0..2, 3..6)
    val days = dayRanges[window]
    val batches = remember(plan, days) { plan?.let { MealPlanner.prepBatches(it, days) }.orEmpty() }

    fun dayLabel(i: Int) = weekStart.plusDays(i.toLong()).dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            NutritionHeading(stringResource(R.string.nu_batch_prep), weekRange(weekStart))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dayRanges.forEachIndexed { index, range ->
                    FilterChip(window == index, { window = index }, label = { Text("${dayLabel(range.first)} – ${dayLabel(range.last)}") })
                }
            }
            Text(
                stringResource(R.string.nu_batch_help), modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (batches.isEmpty() && !loading) item { EmptyState(stringResource(R.string.nu_plan_impossible)) }
        items(batches, key = { it.recipe.key }) { batch ->
            NutritionShortcut(
                batch.recipe.name(de),
                pluralStringResource(R.plurals.nu_batch_summary, batch.meals.size, fmt(batch.portions, 2), batch.meals.size),
                Glyph.BOWL
            ) { open = batch }
        }
    }

    open?.let { batch ->
        val split = batch.meals.joinToString("  ·  ") { (day, meal) -> "${dayLabel(day)} × ${fmt(meal.portions, 2)}" }
        RecipeDialog(
            recipe = batch.recipe, initialPortions = batch.portions, breakdown = split, isBatch = true,
            onDismiss = { open = null }
        )
    }
}

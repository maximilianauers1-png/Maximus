package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.LoggedMeal
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.nutrition.domain.AdaptiveTdee
import app.maximus.nutrition.domain.Food
import app.maximus.nutrition.domain.Foods
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.nutrition.domain.TdeeEstimate
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawTrendChart
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.fmtDay
import app.maximus.ui.strongman.parseDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch

@Composable
internal fun JournalTab(
    log: List<NutritionLogEntity>,
    meals: List<LoggedMeal>,
    adaptive: TdeeEstimate?,
    today: Long,
    book: RecipeBook,
    repo: NutritionRepository,
    write: (suspend () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val de = isGerman()
    var selected by rememberSaveable { mutableLongStateOf(today) }
    val day = minOf(selected, today)
    val existing = log.firstOrNull { it.epochDay == day }
    var weight by rememberSaveable(day, existing) { mutableStateOf(existing?.weightKg?.let { fmt(it, 1) }.orEmpty()) }
    var kcal by rememberSaveable(day, existing) { mutableStateOf(existing?.kcal?.let { fmt(it, 0) }.orEmpty()) }
    var protein by rememberSaveable(day, existing) { mutableStateOf(existing?.proteinG?.let { fmt(it, 0) }.orEmpty()) }
    var note by rememberSaveable(day, existing) { mutableStateOf(existing?.note.orEmpty()) }
    var saved by rememberSaveable(day) { mutableStateOf(false) }
    var deleteMeal by remember { mutableStateOf<LoggedMeal?>(null) }
    var addingFood by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val dailyMeals = remember(meals, day) { meals.filter { it.day == day }.sortedBy { it.id } }
    val weights = remember(log) { log.filter { it.weightKg != null } }
    val logByDay = remember(log) { log.associateBy { it.epochDay } }
    val mealCounts = remember(meals) { meals.groupingBy { it.day }.eachCount() }
    val recent = remember(logByDay, mealCounts) { (logByDay.keys + mealCounts.keys).sortedDescending().take(30) }
    val valid = isValidOptional(weight, allowZero = false) && isValidOptional(kcal) && isValidOptional(protein)

    LazyColumn(state = scrollState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { NutritionHeading(stringResource(R.string.nu_tab_journal)) }
        item {
            NutritionCard(stringResource(R.string.nu_checkin)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphButton(Glyph.CHEVRON_LEFT, stringResource(R.string.cal_prev), { selected = day - 1 })
                    Text(
                        LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)
                    )
                    GlyphButton(Glyph.CHEVRON_RIGHT, stringResource(R.string.cal_next), { selected = day + 1 }, enabled = day < today)
                }
                NumberField(stringResource(R.string.nu_weight), weight, { weight = it; saved = false }, Modifier.fillMaxWidth(), suffix = "kg")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_energy), kcal, { kcal = it; saved = false }, Modifier.weight(1f), suffix = "kcal")
                    NumberField(stringResource(R.string.nu_protein), protein, { protein = it; saved = false }, Modifier.weight(1f), suffix = "g")
                }
                Text(stringResource(R.string.nu_daily_totals_help), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (dailyMeals.isNotEmpty()) {
                    TextButton(onClick = {
                        kcal = fmt(dailyMeals.sumOf { it.nutrients.kcal }, 0)
                        protein = fmt(dailyMeals.sumOf { it.nutrients.proteinG }, 0)
                        saved = false
                    }) { Text(stringResource(R.string.nu_use_meal_totals)) }
                }
                OutlinedTextField(
                    note, { note = it; saved = false }, label = { Text(stringResource(R.string.nu_performance_note)) },
                    modifier = Modifier.fillMaxWidth(), maxLines = 4
                )
                if (!valid) Text(stringResource(R.string.nu_checkin_range), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = {
                        val entry = NutritionLogEntity(day, parseDecimal(weight), parseDecimal(kcal), parseDecimal(protein), note.trim())
                        write { repo.saveDay(entry); saved = true }
                    },
                    enabled = valid, modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(if (saved) R.string.nu_saved else R.string.save)) }
            }
        }
        item {
            NutritionCard(stringResource(R.string.nu_logged_meals)) {
                dailyMeals.forEach { meal ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            val amount = if (meal.isFood) " · ${fmt(meal.amount, 0)} g" else ""
                            Text(loggedMealName(meal, de, book) + amount, style = MaterialTheme.typography.titleSmall)
                            Text(macroLine(meal.nutrients), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { deleteMeal = meal }) { Text(stringResource(R.string.delete)) }
                    }
                }
                TextButton(onClick = { addingFood = true }) { Text(stringResource(R.string.nu_log_food)) }
            }
        }
        if (weights.size >= 2) item {
            val trend = remember(weights) { AdaptiveTdee.ewma(weights.map { it.weightKg!! }) }
            ChartFrame(stringResource(R.string.nu_weight_chart), "maximus-gewicht.png") { hits, m, c ->
                val xs = weights.map { it.epochDay.toDouble() }
                drawTrendChart(
                    hits, m, c,
                    xs = xs, ys = weights.map { it.weightKg!! },
                    pointLabels = weights.map { "${fmtDay(it.epochDay)}  ${fmt(it.weightKg!!, 1)} kg" },
                    fitX = xs, fitY = trend, bandLo = trend, bandHi = trend,
                    xLabel = { x -> fmtDay(x.toLong()) }
                )
            }
        }
        item {
            NutritionCard(stringResource(R.string.nu_adaptive_title)) {
                if (adaptive == null) {
                    Text(stringResource(R.string.nu_adaptive_missing), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    ResultLine(stringResource(R.string.nu_adaptive_tdee), "${fmt(adaptive.tdee, 0)} ± ${fmt(adaptive.sigma, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_adaptive_slope), "${fmt(adaptive.slopeKgPerWeek, 2)} kg")
                    ResultLine(stringResource(R.string.nu_adaptive_intake), "${fmt(adaptive.meanIntake, 0)} kcal")
                    ResultLine(stringResource(R.string.nu_adaptive_n), "${adaptive.weightDays} / ${adaptive.intakeDays}")
                    Text(stringResource(R.string.nu_adaptive_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (recent.isNotEmpty()) item { Text(stringResource(R.string.nu_recent_checkins), style = MaterialTheme.typography.titleMedium) }
        items(recent, key = { it }) { date ->
            val entry = logByDay[date]
            val summary = listOfNotNull(
                entry?.weightKg?.let { "${fmt(it, 1)} kg" },
                entry?.kcal?.let { "${fmt(it, 0)} kcal" },
                mealCounts[date]?.let { pluralStringResource(R.plurals.nu_meal_count, it, it) }
            ).joinToString("  ·  ")
            NutritionShortcut(fmtDay(date), summary.ifEmpty { entry?.note.orEmpty().take(90) }, Glyph.BOOK) {
                selected = date
                scope.launch { scrollState.scrollToItem(1) }
            }
        }
    }

    deleteMeal?.let { meal ->
        AlertDialog(
            onDismissRequest = { deleteMeal = null },
            title = { Text(stringResource(R.string.nu_delete_meal_title)) },
            text = { Text(loggedMealName(meal, de, book)) },
            confirmButton = {
                TextButton(onClick = { deleteMeal = null; write { repo.deleteMeal(meal.id) } }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleteMeal = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (addingFood) FoodLogDialog(
        onConfirm = { food, grams ->
            addingFood = false
            write { repo.logFood(day, food, grams) }
        },
        onDismiss = { addingFood = false }
    )
}

private fun isValidOptional(value: String, allowZero: Boolean = true) =
    value.isBlank() || parseDecimal(value)?.let { it.isFinite() && (it > 0 || (allowZero && it == 0.0)) } == true

@Composable
private fun FoodLogDialog(onConfirm: (Food, Double) -> Unit, onDismiss: () -> Unit) {
    val de = isGerman()
    var query by rememberSaveable { mutableStateOf("") }
    var key by rememberSaveable { mutableStateOf<String?>(null) }
    var amount by rememberSaveable { mutableStateOf("100") }
    val food = key?.let { Foods.byKey[it] }
    val grams = parseDecimal(amount)?.takeIf { it.isFinite() && it > 0 }
    val matches = remember(query, de) {
        Foods.all.filter { it.name(de).contains(query.trim(), ignoreCase = true) }.sortedBy { it.name(de) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food?.name(de) ?: stringResource(R.string.nu_log_food)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (food == null) {
                    OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.nu_find_food)) }, singleLine = true)
                    if (matches.isEmpty()) Text(stringResource(R.string.nu_no_food))
                    LazyColumn(Modifier.heightIn(max = 260.dp)) {
                        items(matches, key = { it.key }) { match ->
                            Text(
                                match.name(de), Modifier.fillMaxWidth().clickable { key = match.key }.padding(vertical = 13.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            NutritionDivider()
                        }
                    }
                } else {
                    NumberField(stringResource(R.string.nu_food_amount), amount, { amount = it }, suffix = "g")
                    if (grams != null) Text(macroLine(food.per100 * (grams / 100.0)), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { key = null }) { Text(stringResource(R.string.nu_change_food)) }
                }
            }
        },
        confirmButton = {
            if (food != null) TextButton(onClick = { grams?.let { onConfirm(food, it) } }, enabled = grams != null) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

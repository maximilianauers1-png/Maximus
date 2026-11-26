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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.nutrition.domain.DerivedTargets
import app.maximus.nutrition.domain.MealPlanner
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.PlanSettings
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.nutrition.domain.RecipeTag
import app.maximus.nutrition.domain.WeekBook
import app.maximus.nutrition.domain.WeekEntry
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import kotlinx.coroutines.launch

/**
 * Weekly recipe store: every ISO week is saved separately (7 days × M1–M5), can be filled from the coach plan,
 * generated from the fat-loss recipes, copied from the previous week and reopened from the archive.
 */
@Composable
fun WeekTab(d: DerivedTargets, today: Long, book: RecipeBook, repo: NutritionRepository, initialMonday: Long = WeekBook.monday(today)) {
    val de = isGerman()
    val locale = LocalConfiguration.current.locales[0]
    val scope = rememberCoroutineScope()
    var monday by rememberSaveable(initialMonday) { mutableLongStateOf(initialMonday) }
    val weekId = WeekBook.weekId(monday)
    val week by remember(weekId) { repo.week(weekId) }.collectAsState(initial = WeekBook(weekId))
    val archive by repo.storedWeeks.collectAsState(initial = emptyList())
    var seed by rememberSaveable { mutableIntStateOf(1) }
    var editing by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var shopping by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    val isCurrent = monday == WeekBook.monday(today)
    val fmtDate = DateTimeFormatter.ofPattern("d.M.", locale)

    fun save(b: WeekBook) { scope.launch { repo.saveWeek(b) } }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard(stringResource(R.string.nw_title)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlyphButton(Glyph.CHEVRON_LEFT, stringResource(R.string.cal_prev), { monday -= 7 }, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.nw_week_label, weekId.substringAfter("-W").toInt()), style = MaterialTheme.typography.titleMedium)
                        Text("${LocalDate.ofEpochDay(monday).format(fmtDate)} – ${LocalDate.ofEpochDay(monday + 6).format(fmtDate)}" +
                            if (isCurrent) "  · " + stringResource(R.string.nw_this_week) else "",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    GlyphButton(Glyph.CHEVRON_RIGHT, stringResource(R.string.cal_next), { monday += 7 }, tint = MaterialTheme.colorScheme.primary)
                }
                Text(stringResource(R.string.nw_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Button(onClick = { save(WeekBook.coachWeek(weekId)) }) { Text(stringResource(R.string.nw_fill_coach)) }
                    Button(onClick = {
                        val pool = book.all.filter { RecipeTag.FAT_LOSS in it.tags }
                        val plan = MealPlanner.plan(d.macros, PlanSettings(mealsPerDay = 5, distinctRecipes = 10, seed = seed.toLong()), pool)
                        seed++
                        if (plan != null) save(WeekBook.fromPlan(weekId, plan))
                    }) { Text(stringResource(R.string.nw_fill_cut)) }
                    OutlinedButton(onClick = {
                        scope.launch { val prev = repo.loadWeek(WeekBook.weekId(monday - 7)); if (!prev.isEmpty) repo.saveWeek(week.copyPlanFrom(prev)) }
                    }) { Text(stringResource(R.string.nw_copy_prev)) }
                    OutlinedButton(enabled = !week.isEmpty, onClick = { shopping = true }) { Text(stringResource(R.string.nu_shopping)) }
                    OutlinedButton(enabled = !week.isEmpty, onClick = { confirmClear = true }) { Text(stringResource(R.string.nw_clear)) }
                }
                Text(stringResource(R.string.nw_cut_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
        if (!week.isEmpty) item {
            val filledDays = (0..6).filter { i -> week.days[i].any { it != null } }
            val avg = filledDays.fold(Nutrients.ZERO) { a, i -> a + week.dayTotals(i, book::resolve) } * (1.0 / filledDays.size.coerceAtLeast(1))
            val costs = filledDays.map { week.dayCostSek(it, book::resolve) }
            SectionCard(stringResource(R.string.nu_week_average)) {
                ResultLine(stringResource(R.string.nu_energy), "${fmt(avg.kcal, 0)} / ${fmt(d.macros.kcal, 0)} kcal")
                ResultLine(stringResource(R.string.nu_protein), "${fmt(avg.proteinG, 0)} / ${fmt(d.macros.proteinG, 0)} g")
                ResultLine(stringResource(R.string.nu_carbs), "${fmt(avg.carbG, 0)} / ${fmt(d.macros.carbG, 0)} g")
                ResultLine(stringResource(R.string.nu_fat), "${fmt(avg.fatG, 0)} / ${fmt(d.macros.fatG, 0)} g")
                ResultLine(stringResource(R.string.nw_planned_meals), "${week.filledSlots} / 35")
                if (costs.isNotEmpty() && costs.all { it != null }) ResultLine(stringResource(R.string.nw_week_cost), "${fmt(costs.sumOf { it!! }, 0)} SEK")
            }
        }
        items((0..6).toList(), key = { it }) { i ->
            val totals = week.dayTotals(i, book::resolve)
            val isToday = isCurrent && i == WeekBook.dayIndex(today)
            PlateCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(DayOfWeek.of(i + 1).getDisplayName(JTextStyle.FULL, locale) + if (isToday) " ·" else "",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f))
                        if (totals.kcal > 0) {
                            val off = totals.kcal / d.macros.kcal - 1
                            Text("${fmt(totals.kcal, 0)} kcal · ${fmt(totals.proteinG, 0)} g P",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (kotlin.math.abs(off) <= 0.05) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    for (s in 0 until WeekBook.SLOTS) {
                        val e = week.days[i][s]
                        val r = e?.let { book.resolve(it.recipeKey) }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { editing = i to s }.padding(vertical = 2.dp).heightIn(min = 40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(slotLabel(s), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(96.dp))
                            if (e == null) {
                                Text(stringResource(R.string.nw_add), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            } else {
                                Text(r?.name(de) ?: e.recipeKey, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                if (e.portions != 1.0) Text("× ${fmt(e.portions, 2)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Checkbox(e.eaten, { save(week.toggleEaten(i, s)) })
                            }
                        }
                    }
                }
            }
        }
        if (archive.isNotEmpty()) item {
            SectionCard(stringResource(R.string.nw_archive)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    archive.take(26).forEach { id ->
                        FilterChip(id == weekId, {
                            val y = id.substringBefore("-W").toInt(); val w = id.substringAfter("-W").toInt()
                            monday = LocalDate.of(y, 1, 4).with(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR, w.toLong()).with(DayOfWeek.MONDAY).toEpochDay()
                        }, label = { Text(id) })
                    }
                }
            }
        }
    }

    editing?.let { (day, slot) ->
        SlotDialog(
            book = book, slot = slot, current = week.days[day][slot],
            onPick = { e -> save(week.set(day, slot, e)); editing = null },
            onDismiss = { editing = null }
        )
    }
    if (shopping) {
        val from = if (isCurrent) WeekBook.dayIndex(today) else 0
        ShoppingListDialog(remember(week, book, from) { week.shoppingList(book::resolve, from) }, if (from > 0) stringResource(R.string.nw_shopping_rest) else null) { shopping = false }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.nw_clear)) },
            text = { Text(stringResource(R.string.nw_clear_confirm)) },
            confirmButton = { TextButton(onClick = { save(WeekBook(weekId)); confirmClear = false }) { Text(stringResource(R.string.delete)) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

/** Pick a recipe for one slot: recipes of the slot's meal type first (toggle for all), search, portions. */
@Composable
private fun SlotDialog(book: RecipeBook, slot: Int, current: WeekEntry?, onPick: (WeekEntry?) -> Unit, onDismiss: () -> Unit) {
    val de = isGerman()
    var query by remember { mutableStateOf("") }
    var allTypes by remember { mutableStateOf(false) }
    var cutOnly by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf(current?.recipeKey?.let { book.resolve(it) }) }
    var portions by remember { mutableDoubleStateOf(current?.portions ?: 1.0) }
    val type = WeekBook.slotTypes[slot]
    val q = query.trim().lowercase()
    val list = book.all.filter { r ->
        (allTypes || r.type == type) && (!cutOnly || RecipeTag.FAT_LOSS in r.tags) && (q.isEmpty() || r.name(de).lowercase().contains(q))
    }.sortedWith(compareBy<Recipe>({ RecipeTag.COACH_PLAN !in it.tags }, { RecipeTag.CUSTOM !in it.tags }, { it.name(de) }))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(slotLabel(slot)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                chosen?.let { r ->
                    Text(r.name(de), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(macroLine(r.nutrients * portions), style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.nu_portions), modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { portions = (portions - 0.25).coerceAtLeast(0.25) }) { Text("−") }
                        Text(fmt(portions, 2), modifier = Modifier.padding(horizontal = 8.dp))
                        OutlinedButton(onClick = { portions = (portions + 0.25).coerceAtMost(6.0) }) { Text("+") }
                    }
                }
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.nu_recipe_search)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(allTypes, { allTypes = !allTypes }, label = { Text(stringResource(R.string.nw_all_types)) })
                    FilterChip(cutOnly, { cutOnly = !cutOnly }, label = { Text(stringResource(R.string.nu_tag_cut)) })
                }
                list.take(60).forEach { r ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { chosen = r }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.name(de), style = MaterialTheme.typography.bodyMedium,
                                color = if (chosen?.key == r.key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            Text(macroLine(r.nutrients), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (RecipeTag.COACH_PLAN in r.tags) Text(stringResource(R.string.nu_tag_coach), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = chosen != null, onClick = { chosen?.let { onPick(WeekEntry(it.key, portions, current != null && current.eaten && current.recipeKey == it.key)) } }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Row {
                if (current != null) TextButton(onClick = { onPick(null) }) { Text(stringResource(R.string.nw_remove)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}

@Composable
fun ShoppingListDialog(items: List<app.maximus.nutrition.domain.ShoppingItem>, subtitle: String?, onDismiss: () -> Unit) {
    val de = isGerman()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nu_shopping)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
                Text(stringResource(R.string.nw_shopping_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

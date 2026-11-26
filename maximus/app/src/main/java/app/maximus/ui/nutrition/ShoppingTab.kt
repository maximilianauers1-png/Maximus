package app.maximus.ui.nutrition

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.Food
import app.maximus.nutrition.domain.FoodCategory
import app.maximus.nutrition.domain.ShoppingItem
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.fmt
import java.text.Collator
import java.time.LocalDate
import kotlin.math.ceil

internal fun ShoppingItem.roundedGrams() = ceil(grams).toInt()
internal fun Food.name(de: Boolean) = if (de) nameDe else nameEn

@Composable
internal fun ShoppingTab(
    start: LocalDate,
    items: List<ShoppingItem>,
    checked: Map<String, Int>,
    loading: Boolean,
    onCheck: (ShoppingItem, Boolean) -> Unit
) {
    val context = LocalContext.current
    val de = isGerman()
    val locale = LocalConfiguration.current.locales[0]
    val title = stringResource(R.string.nu_shopping)
    val heading = weekRange(start)
    var showChecked by rememberSaveable { mutableStateOf(true) }
    val grouped = remember(items, locale, de) {
        val collator = Collator.getInstance(locale)
        items.groupBy { it.food.category }.toSortedMap().mapValues { (_, list) ->
            list.sortedWith(compareBy(collator) { it.food.name(de) })
        }
    }
    fun isChecked(item: ShoppingItem) = checked[item.food.key] == item.roundedGrams()
    val count = items.count { isChecked(it) }
    val shareText = buildList {
        add(title)
        add(heading)
        grouped.forEach { (category, entries) ->
            val remaining = entries.filterNot { isChecked(it) }
            if (remaining.isNotEmpty()) {
                add("")
                add(foodCategoryLabel(category))
                remaining.forEach { add("${it.food.name(de)}  ·  ${it.roundedGrams()} g") }
            }
        }
    }.joinToString("\n")

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item { NutritionHeading(title, heading) }
        if (items.isEmpty()) {
            if (!loading) item { EmptyState(stringResource(R.string.nu_plan_impossible)) }
        } else {
            item {
                NutritionCard(stringResource(R.string.nu_shopping_progress, count, items.size)) {
                    LinearProgressIndicator(progress = { count.toFloat() / items.size }, modifier = Modifier.fillMaxWidth())
                    OutlinedButton(
                        enabled = count < items.size,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
                            context.startActivity(Intent.createChooser(intent, title))
                        }
                    ) { Text(stringResource(R.string.nu_share_shopping)) }
                    FilterChip(showChecked, { showChecked = !showChecked }, label = { Text(stringResource(R.string.nu_show_checked)) })
                }
            }
            if (!showChecked && count == items.size) item { EmptyState(stringResource(R.string.nu_shopping_complete)) }
            grouped.forEach { (category, entries) ->
                val visible = if (showChecked) entries else entries.filterNot { isChecked(it) }
                if (visible.isNotEmpty()) item {
                    Text(
                        foodCategoryLabel(category), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(visible, key = { it.food.key }) { item ->
                    val done = isChecked(item)
                    PlateCard {
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 56.dp)
                                .toggleable(value = done, role = Role.Checkbox, onValueChange = { onCheck(item, it) })
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(done, onCheckedChange = null)
                            Text(
                                item.food.name(de), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (done) TextDecoration.LineThrough else null
                            )
                            val grams = item.roundedGrams()
                            Text(
                                if (grams >= 1000) "${fmt(grams / 1000.0, 3).trimEnd('0').trimEnd(',', '.')} kg" else "$grams g",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.nu_shopping_note), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun foodCategoryLabel(category: FoodCategory) = stringResource(
    when (category) {
        FoodCategory.PRODUCE -> R.string.nu_cat_produce
        FoodCategory.DAIRY_EGGS -> R.string.nu_cat_dairy
        FoodCategory.MEAT_FISH -> R.string.nu_cat_meat
        FoodCategory.GRAINS -> R.string.nu_cat_grains
        FoodCategory.LEGUMES -> R.string.nu_cat_legumes
        FoodCategory.NUTS_OILS -> R.string.nu_cat_nuts
        FoodCategory.PANTRY -> R.string.nu_cat_pantry
    }
)

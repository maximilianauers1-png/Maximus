package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.LoggedMeal
import app.maximus.nutrition.domain.Foods
import app.maximus.nutrition.domain.PlannedMeal
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.fmt

@Composable
internal fun NutritionHeading(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun NutritionCard(
    title: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    PlateCard(onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (title != null) Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
internal fun NutritionShortcut(title: String, detail: String, icon: Glyph, onClick: () -> Unit) {
    NutritionCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlyphIcon(icon, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            GlyphIcon(Glyph.CHEVRON_RIGHT, tint = MaterialTheme.colorScheme.outline, size = 18.dp)
        }
    }
}

@Composable
internal fun MealRow(meal: PlannedMeal, logged: LoggedMeal?, book: RecipeBook, onClick: () -> Unit) {
    val de = isGerman()
    val name = logged?.let { loggedMealName(it, de, book) } ?: meal.recipe.name(de)
    val amounts = logged?.nutrients ?: meal.nutrients
    PlateCard(onClick = onClick) {
        Row(
            modifier = Modifier.heightIn(min = 76.dp).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    if (logged != null) stringResource(R.string.nu_logged) else mealTypeLabel(meal.type),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary
                )
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.nu_meal_summary, fmt(amounts.kcal, 0), fmt(amounts.proteinG, 0), fmt(logged?.amount ?: meal.portions, 2)),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            GlyphIcon(
                if (logged != null) Glyph.CHECK else Glyph.CHEVRON_RIGHT,
                tint = if (logged != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                size = 20.dp
            )
        }
    }
}

internal fun loggedMealName(meal: LoggedMeal, de: Boolean, book: RecipeBook): String =
    if (meal.isFood) {
        val key = meal.itemKey.removePrefix("food:")
        Foods.byKey[key]?.name(de) ?: key
    } else book.resolve(meal.itemKey)?.name(de) ?: meal.itemKey

@Composable
internal fun NutritionDivider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

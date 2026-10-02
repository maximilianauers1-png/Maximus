package app.maximus.ui.strongman

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.maximus.ui.components.SteelRule
import app.maximus.ui.components.PlateCard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.maximus.strongman.data.ExerciseEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Accepts both decimal comma and decimal point. */
fun parseDecimal(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

fun fmt(value: Double, decimals: Int = 1): String = String.format(Locale.getDefault(), "%.${decimals}f", value)

/** Up to two decimals, trailing zeros removed: 100.0 -> "100", 102.5 -> "102,5" (de). */
fun fmtKg(value: Double): String = fmt(value, 2).trimEnd('0').trimEnd(',', '.')

private val DATE = DateTimeFormatter.ofPattern("dd.MM.yy")

fun fmtDay(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(DATE)

@Composable
fun ExerciseEntity.displayName(): String =
    if (LocalConfiguration.current.locales[0].language == "de") nameDe else nameEn

@Composable
fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    integer: Boolean = false,
    suffix: String? = null
) {
    val invalid = value.isNotBlank() && (if (integer) value.trim().toIntOrNull() == null else parseDecimal(value) == null)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = invalid,
        suffix = suffix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (integer) KeyboardType.Number else KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    PlateCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            SteelRule(lozenge = false, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
            content()
        }
    }
}

/** Label left in muted ink, value right-aligned in tabular figures. */
@Composable
fun ResultLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"), textAlign = TextAlign.End)
    }
}

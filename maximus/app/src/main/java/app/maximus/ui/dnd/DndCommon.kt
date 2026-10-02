package app.maximus.ui.dnd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.ui.components.SteelRule

/** Signed modifier, e.g. "+3" or "-1". */
fun sign(v: Int): String = if (v >= 0) "+$v" else "$v"

fun fmt1(v: Double): String = String.format(java.util.Locale.US, "%.1f", v)

/**
 * Card with a title and a steel rule. Titles wrap instead of being cut, and the content column always
 * gets the full width, which is what keeps long rule text from colliding with values.
 */
@Composable
fun DndCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            SteelRule(lozenge = false, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
            content()
        }
    }
}

/**
 * Label on the left, value on the right. The label takes the remaining width and wraps; the value is
 * right-aligned and never shrinks below its own content, so the two can never overlap.
 */
@Composable
fun StatRow(label: String, value: String, modifier: Modifier = Modifier, emphasise: Boolean = false) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            value,
            style = if (emphasise) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (emphasise) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

/** Multi-line body text that never collides with anything: full width, generous line height. */
@Composable
fun BodyText(text: String, modifier: Modifier = Modifier, muted: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth().padding(top = 2.dp)
    )
}

/** A compact rectangular stat block, used for the six ability scores and for AC/HP/initiative. */
@Composable
fun StatBox(label: String, big: String, small: String? = null, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val inner: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(big, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            if (small != null) {
                Text(small, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) Card(onClick = onClick, modifier = modifier, colors = colors, border = border) { inner() }
    else Card(modifier = modifier, colors = colors, border = border) { inner() }
}

/** Plus/minus stepper with a fixed-width number, so the row never jumps or overlaps. */
@Composable
fun Stepper(value: Int, onChange: (Int) -> Unit, min: Int = 0, max: Int = 99, step: Int = 1) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onChange((value - step).coerceAtLeast(min)) },
            enabled = value > min,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) { Text("−") }
        Box(modifier = Modifier.widthIn(min = 44.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            Text("$value", style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
        OutlinedButton(
            onClick = { onChange((value + step).coerceAtMost(max)) },
            enabled = value < max,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) { Text("+") }
    }
}

@Composable
fun MonoText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), modifier = modifier)
}

/** [onChange] is the last parameter on purpose, so callers can pass it as a trailing lambda. */
@Composable
fun TextFieldRow(label: String, value: String, modifier: Modifier = Modifier, minLines: Int = 1, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, onChange, label = { Text(label) },
        singleLine = minLines == 1, minLines = minLines,
        modifier = modifier.fillMaxWidth().padding(vertical = 3.dp)
    )
}

/** Confirmation dialog used for deletions. */
@Composable
fun ConfirmDialog(title: String, text: String, confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

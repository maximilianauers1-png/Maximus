package app.maximus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.maximus.core.app.DesignRepository
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.theme.Design
import app.maximus.ui.theme.DesignConcept
import kotlinx.coroutines.launch

/** Switches between the design concepts; the change applies immediately and is remembered. */
@Composable
fun DesignPicker(repository: DesignRepository) {
    val scope = rememberCoroutineScope()
    val current = Design.concept
    SectionCard("Design") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DesignConcept.entries.forEach { c ->
                val selected = c == current
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (selected) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
                        .border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .clickable { scope.launch { repository.select(c) } }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected, onClick = { scope.launch { repository.select(c) } })
                    Column(Modifier.weight(1f).padding(start = 4.dp)) {
                        Text(c.label, style = MaterialTheme.typography.titleMedium)
                        Text(c.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                            c.swatches.forEach { col ->
                                Box(Modifier.size(18.dp).clip(CircleShape).background(col).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

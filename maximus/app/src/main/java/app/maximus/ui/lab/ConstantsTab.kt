package app.maximus.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.Phys
import app.maximus.ui.strongman.SectionCard

/** CODATA 2018 constants grouped by field, with exactness or relative uncertainty. */
@Composable
fun ConstantsTab() {
    val groups = Phys.all.groupBy { it.group }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        groups.forEach { (group, list) ->
            item {
                SectionCard(group) {
                    list.forEach { k ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                            Text(k.symbol, style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(72.dp))
                            Column(Modifier.weight(1f)) {
                                Text(k.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${Fmt.num(k.value, 11)} ${k.unit}", style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                            }
                            Text(if (k.exact) "exakt" else "±${Fmt.num(k.relUncertainty, 2)}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.End, modifier = Modifier.width(68.dp))
                        }
                    }
                }
            }
        }
        item {
            Text("CODATA 2018 (NIST, 2019). Seit der SI-Reform 2019 sind c, h, e, k_B und N_A per Definition exakt; daraus abgeleitete Größen ebenso. " +
                "Die rechte Spalte nennt die relative Standardunsicherheit.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

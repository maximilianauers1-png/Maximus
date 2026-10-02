package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.dnd.domain.ChallengeRating
import app.maximus.dnd.domain.Rules
import app.maximus.dnd.domain.SRD_ATTRIBUTION
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard

private val DICE_SYNTAX = listOf(
    "2d6+3" to "Dice, count and modifier",
    "4d6kh3" to "Keep the highest three of four",
    "2d20kh1" to "Advantage",
    "2d20kl1" to "Disadvantage",
    "4d6dl1" to "Drop the lowest die",
    "1d6r<2" to "Reroll results of 2 or less until higher",
    "1d6ro<2" to "Reroll results of 2 or less once (great weapon fighting)",
    "1d6!" to "Roll again on the maximum (exploding)",
    "1d6min3" to "Every die counts at least 3",
    "2d6/2" to "Halve the total, rounded down",
    "max(1d20,1d20)" to "Take the better of two rolls",
    "1d%" to "Percentile die, 1 to 100",
    "4dF" to "Fudge dice with -1, 0 and +1",
    "2W6" to "German W works just like d"
)

@Composable
fun ReferenceTab() {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Dice syntax") {
                DICE_SYNTAX.forEach { (expr, desc) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Text(expr, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(132.dp))
                        Text(desc, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            SectionCard("Proficiency bonus by level") {
                (1..20 step 4).forEach { l -> ResultLine("Level $l–${minOf(l + 3, 20)}", "+${Rules.proficiencyBonus(l)}") }
                Text("Formula: 2 + ⌊(level − 1)/4⌋.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Monster statistics by challenge rating") {
                ChallengeRating.TABLE.filter { it.cr <= 10 }.forEach { r ->
                    ResultLine("CR ${ChallengeRating.format(r.cr)}", "AC ${r.ac} · ${r.hpMin}–${r.hpMax} HP · ${sign(r.attack)} · ${r.dprMin}–${r.dprMax} dmg · ${r.xp} XP")
                }
                Text("Expected values from the Dungeon Master's Guide used as the reference by the calculator (shown up to CR 10).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Licence and scope") {
                Text("This module contains rules content from the System Reference Document 5.1 under the CC BY 4.0 licence. Material from other books is deliberately absent because it is not freely licensed. Anything beyond that you enter yourself as a monster, note or character note.", style = MaterialTheme.typography.bodySmall)
                Text(SRD_ATTRIBUTION, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceProbability
import app.maximus.dnd.domain.DiceRoller
import app.maximus.dnd.domain.RollResult
import kotlin.random.Random

/** What the sheet asks the dice engine to roll when the user taps a line. */
data class RollRequest(
    val label: String,
    /** Flat modifier for a d20 test; ignored when [rawExpression] is set. */
    val modifier: Int = 0,
    val rawExpression: String? = null,
    /** Second expression rolled alongside, used for weapon damage next to the attack roll. */
    val damageExpression: String? = null,
    val damageLabel: String = "Damage",
    val critRange: Int = 20
)

private fun d20Expression(modifier: Int, advantage: Int): String {
    val dice = when {
        advantage > 0 -> "2d20kh1"
        advantage < 0 -> "2d20kl1"
        else -> "1d20"
    }
    return if (modifier == 0) dice else dice + (if (modifier > 0) "+$modifier" else "$modifier")
}

/**
 * Roll dialog. A d20 test shows the natural die, the modifier and the total, plus the probability of
 * beating a chosen DC; a damage roll shows the dice and their mean. Advantage and disadvantage reroll
 * immediately, and a critical hit doubles the damage dice (not the flat modifier), as the rules say.
 */
@Composable
fun RollDialog(request: RollRequest, onDismiss: () -> Unit, onLog: (String, Int, String) -> Unit) {
    val random = remember { Random(System.nanoTime()) }
    var advantage by remember(request) { mutableStateOf(0) }
    var seed by remember(request) { mutableStateOf(0) }
    var dc by remember(request) { mutableStateOf(15) }
    var crit by remember(request) { mutableStateOf(false) }

    val expression = request.rawExpression ?: d20Expression(request.modifier, advantage)
    val result: RollResult? = remember(expression, seed) {
        runCatching { DiceRoller.roll(DiceParser.parse(expression), random) }.getOrNull()
    }
    val natural = result?.pools?.firstOrNull()?.dice?.firstOrNull { it.kept }?.value
    val isD20 = request.rawExpression == null

    val damageExpr = request.damageExpression?.let { if (crit) doubleDice(it) else it }
    val damage: RollResult? = remember(damageExpr, seed) {
        damageExpr?.let { e -> runCatching { DiceRoller.roll(DiceParser.parse(e), random) }.getOrNull() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(request.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(expression, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${result?.total ?: "—"}",
                    style = MaterialTheme.typography.displaySmall,
                    color = when {
                        isD20 && natural == 20 -> MaterialTheme.colorScheme.primary
                        isD20 && natural == 1 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                result?.let { r ->
                    Text(detailText(r), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isD20) {
                    if (natural == 20) Text("Natural 20", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    if (natural == 1) Text("Natural 1", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Target DC", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Stepper(dc, { dc = it }, min = 1, max = 35)
                    }
                    val chance = remember(request.modifier, advantage, dc) {
                        val d = DiceProbability.of(DiceParser.parse(d20Expression(request.modifier, advantage)))
                        d.atLeast(dc)
                    }
                    StatRow("Chance to meet the DC", "${fmt1(100 * chance)} %")
                }
                damage?.let { d ->
                    StatRow(if (crit) "${request.damageLabel} (critical)" else request.damageLabel, "${d.total}", emphasise = true)
                    Text(damageExpr ?: "", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isD20) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                        OutlinedButton(onClick = { advantage = 1; seed++ }) { Text("Adv") }
                        OutlinedButton(onClick = { advantage = 0; seed++ }) { Text("Flat") }
                        OutlinedButton(onClick = { advantage = -1; seed++ }) { Text("Dis") }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (request.damageExpression != null) {
                        OutlinedButton(onClick = { crit = !crit; seed++ }) { Text(if (crit) "Normal" else "Crit") }
                    }
                    Button(onClick = { seed++ }) { Text("Roll again") }
                    TextButton(onClick = {
                        result?.let { onLog(expression, it.total, detailText(it)) }
                        onDismiss()
                    }) { Text("Save & close") }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

private fun detailText(r: RollResult): String = r.pools.joinToString("; ") { p ->
    p.dice.joinToString(" ") { d -> if (d.kept) "${d.value}" else "(${d.value})" }
}

/**
 * Doubles only the dice of a damage expression, leaving flat modifiers alone: "1d8+3" becomes "2d8+3".
 * Expressions the parser cannot split are returned unchanged.
 */
fun doubleDice(expression: String): String {
    val regex = Regex("(\\d*)[dD](\\d+)")
    return regex.replace(expression) { m ->
        val count = m.groupValues[1].toIntOrNull() ?: 1
        "${count * 2}d${m.groupValues[2]}"
    }
}

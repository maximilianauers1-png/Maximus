@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.domain.BuildGuide
import app.maximus.dnd.domain.BuildGuides
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.dnd.domain.DprPoint
import app.maximus.dnd.domain.EldritchInvocations
import app.maximus.dnd.domain.MetamagicOptions
import app.maximus.dnd.domain.Spells
import app.maximus.dnd.domain.SrdClasses
import app.maximus.dnd.domain.SrdSubclasses
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.LineSeries
import app.maximus.ui.charts.drawLineChart
import kotlinx.coroutines.launch

/**
 * Favourite multiclass builds: a level slider shows the exact character at every level, the damage
 * model plots expected damage per round against a typical monster, and one tap creates the character.
 */
@Composable
fun BuildsTab(services: AppServices, onEdit: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    var guideKey by rememberSaveable { mutableStateOf(BuildGuides.all.first().key) }
    var level by rememberSaveable { mutableIntStateOf(10) }
    var fixedAc by rememberSaveable { mutableIntStateOf(0) } // 0 = typical AC for CR = level
    val guide = BuildGuides.byKey[guideKey] ?: BuildGuides.all.first()
    val acOverride = fixedAc.takeIf { it > 0 }

    val build = remember(guide, level) { BuildGuides.buildAt(guide, level) }
    val sheet = remember(build) { CharacterBuilder.build(build) }
    val series = remember(guide, acOverride) { BuildGuides.series(guide, acOverride) }
    val all = remember(acOverride) { BuildGuides.all.associateWith { BuildGuides.series(it, acOverride) } }
    val point = series[level - 1]

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                BuildGuides.all.forEach { g -> FilterChip(guideKey == g.key, { guideKey = g.key }, label = { Text(g.name) }) }
            }
        }
        item {
            DndCard(guide.name) {
                Text(guide.tagline, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                BodyText(guide.concept, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item { LevelCard(guide, level, onLevel = { level = it }, sheetLine = summaryOf(sheet)) }
        item {
            DndCard("Character at level $level") {
                StatRow("Classes", sheet.classLine, emphasise = true)
                StatRow("HP / AC", "${sheet.maxHp} / ${sheet.armorClass}")
                StatRow("Proficiency", sign(sheet.proficiency))
                sheet.spellcasting.forEach { StatRow("${it.className} DC / attack", "${it.saveDc} / ${sign(it.attackBonus)}") }
                val slots = sheet.spellSlots.withIndex().filter { it.value > 0 }.joinToString("  ") { "${it.index + 1}:${it.value}" }
                if (slots.isNotBlank()) StatRow("Spell slots", slots)
                if (sheet.pactSlots.first > 0) StatRow("Pact slots", "${sheet.pactSlots.first} × level ${sheet.pactSlots.second}")
                if (build.featKeys.isNotEmpty()) StatRow("Feats", build.featKeys.joinToString(", ") { app.maximus.dnd.domain.SrdFeats.byKey[it]?.name ?: it })
                if (build.invocationKeys.isNotEmpty()) StatRow("Invocations", build.invocationKeys.joinToString(", ") { EldritchInvocations.byKey[it]?.name ?: it })
                if (build.metamagicKeys.isNotEmpty()) StatRow("Metamagic", build.metamagicKeys.joinToString(", ") { MetamagicOptions.byKey[it]?.name ?: it })
                StatRow("Scores", sheet.scores.entries.joinToString(" ") { "${it.key.short} ${it.value}" })
                val newSpells = guide.spellPlan.filter { it.first == level }.mapNotNull { Spells.byKey[it.second]?.name }
                if (newSpells.isNotEmpty()) StatRow("New spells now", newSpells.joinToString(", "))
                sheet.warnings.forEach { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary) }
                Button(
                    onClick = {
                        scope.launch {
                            val b = BuildGuides.buildAt(guide, level)
                            val id = services.dnd.saveCharacter(0L, "${guide.name} $level", summaryOf(CharacterBuilder.build(b)), DndCodec.encode(b))
                            onEdit(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) { Text("Create this character at level $level") }
            }
        }
        item { DprCard(guide, series, point, fixedAc, onFixedAc = { fixedAc = it }) }
        item {
            val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
            DndCard("Burst damage of all three builds") {
                ChartFrame("Nova damage per round vs level", "maximus-builds-nova", modifier = Modifier.padding(top = 4.dp)) { hits, m, c ->
                    drawLineChart(
                        hits, m, c,
                        all.entries.mapIndexed { i, (g, pts) ->
                            LineSeries(g.name, pts.map { it.level.toDouble() }, pts.map { it.nova }, colors[i % colors.size], markers = false)
                        },
                        xFormat = { "${it.toInt()}" },
                        tooltip = { s, i -> "${s.name}, level ${i + 1}: ${fmt1(s.ys[i])}" }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
                    all.keys.forEachIndexed { i, g -> Text("● ${g.name}", color = colors[i % colors.size], style = MaterialTheme.typography.labelMedium) }
                }
                BodyText("Burst assumes a fresh short rest: all slots, sorcery points, Action Surge and the curse are available for the first round.", muted = true)
            }
        }
        guide.sections.forEach { sec -> item { DndCard(sec.title) { BodyText(sec.text) } } }
        item { ProgressionTable(guide) }
    }
}

@Composable
private fun LevelCard(guide: BuildGuide, level: Int, onLevel: (Int) -> Unit, sheetLine: String) {
    DndCard("Level") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Character level $level", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            val step = guide.steps[level - 1]
            Text("takes ${SrdClasses.byKey[step.classKey]?.name ?: step.classKey}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = level.toFloat(), onValueChange = { onLevel(it.toInt().coerceIn(1, 20)) }, valueRange = 1f..20f, steps = 18)
        BodyText(sheetLine, muted = true)
    }
}

@Composable
private fun DprCard(guide: BuildGuide, series: List<DprPoint>, point: DprPoint, fixedAc: Int, onFixedAc: (Int) -> Unit) {
    DndCard("Damage per round") {
        StatRow("Target AC", if (fixedAc > 0) "$fixedAc (fixed)" else "${point.ac} (typical for CR ${point.level})")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(fixedAc == 0, { onFixedAc(0) }, label = { Text("CR = level") })
            listOf(13, 15, 17, 19, 21).forEach { ac -> FilterChip(fixedAc == ac, { onFixedAc(ac) }, label = { Text("AC $ac") }) }
        }
        StatRow("Sustained", fmt1(point.sustained), emphasise = true)
        BodyText(point.sustainedNote, muted = true)
        StatRow("Burst (nova)", fmt1(point.nova), emphasise = true)
        BodyText(point.novaNote, muted = true)
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        ChartFrame("${guide.name}: expected damage per round", "maximus-dpr-${guide.key}", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
            drawLineChart(
                hits, m, c,
                listOf(
                    LineSeries("Sustained", series.map { it.level.toDouble() }, series.map { it.sustained }, secondary, markers = true),
                    LineSeries("Burst", series.map { it.level.toDouble() }, series.map { it.nova }, primary, markers = true)
                ),
                xFormat = { "${it.toInt()}" },
                tooltip = { s, i -> "${s.name}, level ${i + 1}: ${fmt1(s.ys[i])}" }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 6.dp)) {
            Text("● Burst", color = primary, style = MaterialTheme.typography.labelMedium)
            Text("● Sustained", color = secondary, style = MaterialTheme.typography.labelMedium)
        }
        BodyText(
            "Model: p = clamp((21 + attack − AC)/20, κ, 0.95), κ = (21 − crit range)/20; E = p(D + F) + κD with dice mean D and flat bonus F. " +
                "Magic missile hits automatically. Values are expectations, not rolls.",
            muted = true
        )
    }
}

@Composable
private fun ProgressionTable(guide: BuildGuide) {
    DndCard("Level-by-level plan") {
        val counts = HashMap<String, Int>()
        var asiIndex = 0
        guide.steps.forEachIndexed { i, step ->
            val classLevel = (counts[step.classKey] ?: 0) + 1
            counts[step.classKey] = classLevel
            val cls = SrdClasses.byKey[step.classKey]
            val notes = ArrayList<String>()
            val sub = guide.subclasses[step.classKey]
            if (cls != null && sub != null && classLevel == cls.subclassLevel) notes += SrdSubclasses.byKey[sub]?.name ?: sub
            if (cls != null && classLevel in cls.asiLevels) {
                guide.asiPlan.getOrNull(asiIndex)?.let { notes += it.label }
                asiIndex++
            }
            cls?.features?.filter { it.level == classLevel }?.take(2)?.forEach { notes += it.name }
            if (sub != null) SrdSubclasses.byKey[sub]?.features?.filter { it.level == classLevel && it.level != cls?.subclassLevel }?.take(1)?.forEach { notes += it.name }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                Text("${i + 1}", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(28.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("${cls?.name ?: step.classKey} $classLevel", style = MaterialTheme.typography.bodyMedium)
                    if (notes.isNotEmpty()) Text(notes.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.ActivityLevel
import app.maximus.nutrition.domain.BioSex
import app.maximus.nutrition.domain.BmrFormula
import app.maximus.nutrition.domain.DerivedTargets
import app.maximus.nutrition.domain.MacroCalculator
import app.maximus.nutrition.domain.MacroWarning
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.RateAssessment
import app.maximus.nutrition.domain.TdeeEstimate
import app.maximus.nutrition.domain.TdeeSource
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawTrendChart
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.parseDecimal
import kotlin.math.max
import kotlin.math.min

@Composable
fun GoalsTab(p: NutritionProfile, d: DerivedTargets, adaptive: TdeeEstimate?, today: Long, onSave: (NutritionProfile) -> Unit) {
    var sex by remember(p) { mutableStateOf(p.sex) }
    var age by remember(p) { mutableStateOf(p.ageYears.toString()) }
    var height by remember(p) { mutableStateOf(fmt(p.heightCm, 0)) }
    var weight by remember(p) { mutableStateOf(fmt(p.weightKg, 1)) }
    var bf by remember(p) { mutableStateOf(p.bodyFatPercent?.let { fmt(it, 1) } ?: "") }
    var formula by remember(p) { mutableStateOf(p.formula) }
    var activity by remember(p) { mutableStateOf(p.activity) }
    var goal by remember(p) { mutableStateOf(p.goalWeightKg?.let { fmt(it, 1) } ?: "") }
    var weeks by remember(p) { mutableStateOf(p.goalEpochDay?.let { ((it - today) / 7).coerceAtLeast(1).toString() } ?: "") }
    var protein by remember(p) { mutableDoubleStateOf(p.proteinPerKg) }
    var fat by remember(p) { mutableDoubleStateOf(p.fatPerKg) }
    var kcalOverride by remember(p) { mutableStateOf(p.kcalOverride?.let { fmt(it, 0) } ?: "") }
    var useAdaptive by remember(p) { mutableStateOf(p.useAdaptiveTdee) }

    fun edited(): NutritionProfile? {
        val a = age.trim().toIntOrNull() ?: return null
        val h = parseDecimal(height) ?: return null
        val w = parseDecimal(weight) ?: return null
        val wk = weeks.trim().toIntOrNull()
        val g = parseDecimal(goal)
        return p.copy(
            sex = sex, ageYears = a, heightCm = h, weightKg = w, bodyFatPercent = parseDecimal(bf),
            formula = formula, activity = activity,
            goalWeightKg = if (g != null && wk != null) g else null,
            goalEpochDay = if (g != null && wk != null && wk > 0) today + 7L * wk else null,
            proteinPerKg = protein, fatPerKg = fat, kcalOverride = parseDecimal(kcalOverride), useAdaptiveTdee = useAdaptive
        ).let { NutritionProfile.fromMap(it.toMap()) }
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TargetsCard(d, p) }
        if (d.projection != null) item { ProjectionCard(d, p) }
        item {
            SectionCard(stringResource(R.string.nu_profile)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BioSex.entries.forEach { s -> FilterChip(sex == s, { sex = s }, label = { Text(stringResource(if (s == BioSex.MALE) R.string.sm_male else R.string.sm_female)) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_age), age, { age = it }, Modifier.weight(1f), integer = true)
                    NumberField(stringResource(R.string.nu_height), height, { height = it }, Modifier.weight(1f), suffix = "cm")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_weight), weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField(stringResource(R.string.nu_bodyfat), bf, { bf = it }, Modifier.weight(1f), suffix = "%")
                }
                Text(stringResource(R.string.nu_formula), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BmrFormula.entries.forEach { f -> FilterChip(formula == f, { formula = f }, label = { Text(formulaLabel(f)) }) }
                }
                Text(stringResource(R.string.nu_activity), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActivityLevel.entries.forEach { a -> FilterChip(activity == a, { activity = a }, label = { Text("${activityLabel(a)} ${fmt(a.pal, 2)}") }) }
                }
            }
        }
        item {
            SectionCard(stringResource(R.string.nu_goal)) {
                Text(stringResource(R.string.nu_goal_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_goal_weight), goal, { goal = it }, Modifier.weight(1f), suffix = "kg")
                    NumberField(stringResource(R.string.nu_goal_weeks), weeks, { weeks = it }, Modifier.weight(1f), integer = true)
                }
                Text(stringResource(R.string.nu_protein_per_kg, fmt(protein, 1)), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                Slider(value = protein.toFloat(), onValueChange = { protein = Math.round(it * 10) / 10.0 }, valueRange = 1.2f..3.0f, steps = 17)
                Text(stringResource(R.string.nu_fat_per_kg, fmt(fat, 1)), style = MaterialTheme.typography.labelLarge)
                Slider(value = fat.toFloat(), onValueChange = { fat = Math.round(it * 10) / 10.0 }, valueRange = 0.5f..1.6f, steps = 10)
                NumberField(stringResource(R.string.nu_kcal_override), kcalOverride, { kcalOverride = it }, suffix = "kcal")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.nu_use_adaptive), modifier = Modifier.weight(1f))
                    Switch(useAdaptive, { useAdaptive = it }, enabled = adaptive != null || useAdaptive)
                }
                if (adaptive == null) Text(stringResource(R.string.nu_adaptive_missing), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val e = edited()
                Button(enabled = e != null, onClick = { e?.let(onSave) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(stringResource(R.string.save)) }
            }
        }
    }
}

@Composable
fun formulaLabel(f: BmrFormula) = stringResource(
    when (f) {
        BmrFormula.MIFFLIN_ST_JEOR -> R.string.nu_f_mifflin
        BmrFormula.HARRIS_BENEDICT_REVISED -> R.string.nu_f_harris
        BmrFormula.KATCH_MCARDLE -> R.string.nu_f_katch
        BmrFormula.CUNNINGHAM -> R.string.nu_f_cunningham
    }
)

@Composable
fun activityLabel(a: ActivityLevel) = stringResource(
    when (a) {
        ActivityLevel.SEDENTARY -> R.string.nu_act_sedentary
        ActivityLevel.LIGHT -> R.string.nu_act_light
        ActivityLevel.MODERATE -> R.string.nu_act_moderate
        ActivityLevel.HIGH -> R.string.nu_act_high
        ActivityLevel.VERY_HIGH -> R.string.nu_act_very_high
    }
)

@Composable
private fun TargetsCard(d: DerivedTargets, p: NutritionProfile) {
    val m = d.macros
    SectionCard(stringResource(R.string.nu_targets)) {
        ResultLine(stringResource(R.string.nu_bmr), "${fmt(d.bmr, 0)} kcal")
        ResultLine(
            stringResource(if (d.tdeeSource == TdeeSource.ADAPTIVE) R.string.nu_tdee_adaptive else R.string.nu_tdee_formula),
            "${fmt(d.tdee, 0)} kcal"
        )
        ResultLine(stringResource(R.string.nu_kcal_target), "${fmt(m.kcal, 0)} kcal")
        ResultLine(stringResource(R.string.nu_protein), "${fmt(m.proteinG, 0)} g  (${fmt(100 * m.proteinKcalShare, 0)} %)")
        ResultLine(stringResource(R.string.nu_fat), "${fmt(m.fatG, 0)} g  (${fmt(100 * m.fatKcalShare, 0)} %)")
        ResultLine(stringResource(R.string.nu_carbs), "${fmt(max(0.0, m.carbG), 0)} g  (${fmt(100 * max(0.0, m.carbKcalShare), 0)} %)")
        ResultLine(stringResource(R.string.nu_fiber), "≥ ${fmt(m.fiberG, 0)} g")
        val (mps, perMeal) = MacroCalculator.proteinPerMeal(p.weightKg, p.plan.mealsPerDay, m.proteinG)
        ResultLine(stringResource(R.string.nu_protein_meal), "${fmt(perMeal, 0)} g / ${fmt(mps, 0)} g")
        if (d.belowBmr) Text(stringResource(R.string.nu_warn_below_bmr), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        d.warnings.forEach { w ->
            Text(
                stringResource(
                    when (w) {
                        MacroWarning.CARBS_NEGATIVE -> R.string.nu_warn_carbs_negative
                        MacroWarning.FAT_BELOW_20_PERCENT -> R.string.nu_warn_fat_low
                        MacroWarning.PROTEIN_BELOW_1_6 -> R.string.nu_warn_protein_low
                        MacroWarning.PROTEIN_ABOVE_3_0 -> R.string.nu_warn_protein_high
                        MacroWarning.CARBS_BELOW_3_PER_KG -> R.string.nu_warn_carbs_low
                    }
                ),
                color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ProjectionCard(d: DerivedTargets, p: NutritionProfile) {
    val proj = d.projection ?: return
    SectionCard(stringResource(R.string.nu_projection)) {
        ResultLine(stringResource(R.string.nu_proj_hall), "${fmt(proj.intakeKcal, 0)} kcal/d")
        ResultLine(stringResource(R.string.nu_proj_linear), "${fmt(proj.linearIntakeKcal, 0)} kcal/d")
        ResultLine(stringResource(R.string.nu_proj_rate), stringResource(R.string.nu_rate_value, fmt(proj.weeklyRatePercent, 2)))
        ResultLine(stringResource(R.string.nu_proj_fat_end), "${fmt(100 * proj.finalFatFraction, 1)} %")
        Text(
            stringResource(
                when (proj.assessment) {
                    RateAssessment.MAINTAIN -> R.string.nu_rate_maintain
                    RateAssessment.MODERATE_LOSS -> R.string.nu_rate_moderate_loss
                    RateAssessment.AGGRESSIVE_LOSS -> R.string.nu_rate_aggressive_loss
                    RateAssessment.MODERATE_GAIN -> R.string.nu_rate_moderate_gain
                    RateAssessment.FAST_GAIN -> R.string.nu_rate_fast_gain
                }
            ),
            style = MaterialTheme.typography.bodySmall,
            color = if (proj.assessment == RateAssessment.AGGRESSIVE_LOSS || proj.assessment == RateAssessment.FAST_GAIN) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        val weekLabel = stringResource(R.string.nu_week_short)
        ChartFrame(stringResource(R.string.nu_proj_chart), "maximus-gewichtsprognose.png", modifier = Modifier.padding(top = 8.dp)) { hits, m, c ->
            val xs = proj.trajectory.indices.map { it.toDouble() }
            val lo = proj.trajectory.indices.map { min(proj.trajectory[it], proj.linearTrajectory[it]) }
            val hi = proj.trajectory.indices.map { max(proj.trajectory[it], proj.linearTrajectory[it]) }
            drawTrendChart(
                hits, m, c,
                xs = listOf(0.0, proj.days.toDouble()),
                ys = listOf(p.weightKg, proj.trajectory.last()),
                pointLabels = listOf("${fmt(p.weightKg, 1)} kg", "${fmt(proj.trajectory.last(), 1)} kg"),
                fitX = xs, fitY = proj.trajectory, bandLo = lo, bandHi = hi,
                xLabel = { x -> "${(x / 7).toInt()} $weekLabel" }
            )
        }
        Text(stringResource(R.string.nu_proj_model_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.domain.Activity
import app.maximus.nutrition.domain.BioSex
import app.maximus.nutrition.domain.BmrFormula
import app.maximus.nutrition.domain.BodyComposition
import app.maximus.nutrition.domain.Energy
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.parseDecimal

@Composable
fun ToolsTab(p: NutritionProfile) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { BmrComparison(p) }
        item { NavyCard(p) }
        item { SkinfoldCard(p) }
        item { FfmiCard(p) }
        item { MetCard(p) }
    }
}

@Composable
private fun Explain(id: Int) = Text(stringResource(id), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun BmrComparison(p: NutritionProfile) {
    SectionCard(stringResource(R.string.nu_tool_bmr)) {
        Explain(R.string.nu_tool_bmr_explain)
        BmrFormula.entries.forEach { f ->
            val v = Energy.bmr(p.body, f)
            ResultLine(formulaLabel(f), v?.let { "${fmt(it, 0)} kcal  ×${fmt(p.activity.pal, 2)} = ${fmt(it * p.activity.pal, 0)}" } ?: stringResource(R.string.nu_needs_bodyfat))
        }
    }
}

@Composable
private fun NavyCard(p: NutritionProfile) {
    var neck by rememberSaveable { mutableStateOf("") }
    var waist by rememberSaveable { mutableStateOf("") }
    var hip by rememberSaveable { mutableStateOf("") }
    SectionCard(stringResource(R.string.nu_tool_navy)) {
        Explain(R.string.nu_tool_navy_explain)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.nu_neck), neck, { neck = it }, Modifier.weight(1f), suffix = "cm")
            NumberField(stringResource(R.string.nu_waist), waist, { waist = it }, Modifier.weight(1f), suffix = "cm")
            if (p.sex == BioSex.FEMALE) NumberField(stringResource(R.string.nu_hip), hip, { hip = it }, Modifier.weight(1f), suffix = "cm")
        }
        val n = parseDecimal(neck); val w = parseDecimal(waist)
        val bf = if (n != null && w != null) BodyComposition.navyBodyFat(p.sex, p.heightCm, n, w, parseDecimal(hip)) else null
        if (bf != null) {
            ResultLine(stringResource(R.string.nu_bodyfat), "${fmt(bf, 1)} %")
            ResultLine(stringResource(R.string.nu_lean_mass), "${fmt(p.weightKg * (1 - bf / 100), 1)} kg")
        }
    }
}

@Composable
private fun SkinfoldCard(p: NutritionProfile) {
    var a by rememberSaveable { mutableStateOf("") }
    var b by rememberSaveable { mutableStateOf("") }
    var c by rememberSaveable { mutableStateOf("") }
    val sites = if (p.sex == BioSex.MALE) listOf(R.string.nu_site_chest, R.string.nu_site_abdomen, R.string.nu_site_thigh)
    else listOf(R.string.nu_site_triceps, R.string.nu_site_suprailiac, R.string.nu_site_thigh)
    SectionCard(stringResource(R.string.nu_tool_skinfold)) {
        Explain(R.string.nu_tool_skinfold_explain)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(sites[0]), a, { a = it }, Modifier.weight(1f), suffix = "mm")
            NumberField(stringResource(sites[1]), b, { b = it }, Modifier.weight(1f), suffix = "mm")
            NumberField(stringResource(sites[2]), c, { c = it }, Modifier.weight(1f), suffix = "mm")
        }
        val vals = listOf(a, b, c).map { parseDecimal(it) }
        if (vals.all { it != null }) {
            val sum = vals.sumOf { it!! }
            ResultLine(stringResource(R.string.nu_skinfold_sum), "${fmt(sum, 0)} mm")
            ResultLine(stringResource(R.string.nu_bodyfat), "${fmt(BodyComposition.jacksonPollock3(p.sex, p.ageYears, sum), 1)} %")
        }
    }
}

@Composable
private fun FfmiCard(p: NutritionProfile) {
    var bf by rememberSaveable { mutableStateOf(p.bodyFatPercent?.let { fmt(it, 1) } ?: "") }
    SectionCard(stringResource(R.string.nu_tool_ffmi)) {
        Explain(R.string.nu_tool_ffmi_explain)
        NumberField(stringResource(R.string.nu_bodyfat), bf, { bf = it }, suffix = "%")
        val bmi = BodyComposition.bmi(p.weightKg, p.heightCm)
        ResultLine("BMI", fmt(bmi, 1))
        parseDecimal(bf)?.takeIf { it in 3.0..60.0 }?.let { f ->
            val (raw, norm) = BodyComposition.ffmi(p.weightKg, p.heightCm, f)
            ResultLine("FFMI", fmt(raw, 1))
            ResultLine(stringResource(R.string.nu_ffmi_norm), fmt(norm, 1))
        }
        ResultLine(stringResource(R.string.nu_deurenberg), "${fmt(BodyComposition.deurenberg(p.sex, p.ageYears, bmi), 1)} %")
    }
}

@Composable
private fun MetCard(p: NutritionProfile) {
    var activity by rememberSaveable { mutableStateOf(Activity.STRENGTH_VIGOROUS) }
    var minutes by rememberSaveable { mutableStateOf("75") }
    SectionCard(stringResource(R.string.nu_tool_met)) {
        Explain(R.string.nu_tool_met_explain)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Activity.entries.forEach { a -> FilterChip(activity == a, { activity = a }, label = { Text("${activityName(a)} (${fmt(a.met, 1)})") }) }
        }
        NumberField(stringResource(R.string.nu_minutes), minutes, { minutes = it }, suffix = "min")
        parseDecimal(minutes)?.let { m ->
            ResultLine(stringResource(R.string.nu_met_net), "${fmt(Energy.netActivityKcal(activity.met, p.weightKg, m), 0)} kcal")
            ResultLine(stringResource(R.string.nu_met_gross), "${fmt(activity.met * p.weightKg * m / 60, 0)} kcal")
        }
    }
}

@Composable
private fun activityName(a: Activity) = stringResource(
    when (a) {
        Activity.STRENGTH_VIGOROUS -> R.string.nu_met_strength_hard
        Activity.STRENGTH_MODERATE -> R.string.nu_met_strength_mod
        Activity.STRONGMAN_EVENTS -> R.string.nu_met_events
        Activity.WALKING_5KMH -> R.string.nu_met_walk
        Activity.HIKING -> R.string.nu_met_hike
        Activity.RUNNING_10KMH -> R.string.nu_met_run
        Activity.CYCLING_20KMH -> R.string.nu_met_bike
        Activity.SWIMMING_MODERATE -> R.string.nu_met_swim
        Activity.ROWING_MODERATE -> R.string.nu_met_row
    }
)

package app.maximus.ui.strongman

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Dots
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.OneRepMax
import app.maximus.strongman.domain.OneRmResult
import app.maximus.strongman.domain.Prilepin
import app.maximus.strongman.domain.PrilepinVerdict
import app.maximus.strongman.domain.RefusalReason
import app.maximus.strongman.domain.Rpe
import app.maximus.strongman.domain.Sex
import kotlinx.coroutines.launch

@Composable
fun refusalText(reason: RefusalReason): String = when (reason) {
    RefusalReason.NON_POSITIVE_LOAD -> stringResource(R.string.sm_refuse_load)
    RefusalReason.REPS_BELOW_ONE -> stringResource(R.string.sm_refuse_reps_low)
    RefusalReason.REPS_ABOVE_LIMIT -> stringResource(R.string.sm_refuse_reps_high)
    RefusalReason.INVALID_RPE -> stringResource(R.string.sm_refuse_rpe)
}

@Composable
fun CalculatorTab(repository: StrongmanRepository) {
    val table by repository.fTable.collectAsState(initial = FTable())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OneRmCard(table)
        PrescriptionCard(table, settings)
        DotsCard(settings)
        PrilepinCard()
        FTableCard(table, repository)
    }
}

@Composable
private fun OneRmCard(table: FTable) {
    var w by rememberSaveable { mutableStateOf("100") }
    var r by rememberSaveable { mutableStateOf("5") }
    var rpe by rememberSaveable { mutableStateOf("") }
    SectionCard(stringResource(R.string.sm_calc_1rm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_weight), w, { w = it }, Modifier.weight(1f), suffix = "kg")
            NumberField(stringResource(R.string.sm_reps), r, { r = it }, Modifier.weight(1f), integer = true)
            NumberField(stringResource(R.string.sm_rpe_optional), rpe, { rpe = it }, Modifier.weight(1f))
        }
        val wv = parseDecimal(w)
        val rv = r.trim().toIntOrNull()
        if (wv != null && rv != null) {
            when (val d = OneRepMax.estimate(wv, rv)) {
                is OneRmResult.Estimate -> {
                    ResultLine(stringResource(R.string.sm_epley), "${fmt(OneRepMax.epley(wv, rv), 2)} kg")
                    if (rv <= OneRepMax.BRZYCKI_MAX_REPS) ResultLine(stringResource(R.string.sm_brzycki), "${fmt(OneRepMax.brzycki(wv, rv), 2)} kg")
                    ResultLine(stringResource(R.string.sm_lombardi), "${fmt(OneRepMax.lombardi(wv, rv), 2)} kg")
                    Text(
                        stringResource(R.string.sm_default_estimate, fmt(d.kg, 2)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                is OneRmResult.Refused -> Text(refusalText(d.reason), color = MaterialTheme.colorScheme.error)
            }
            parseDecimal(rpe)?.let { rpeV ->
                when (val e = table.e1rm(wv, rv, rpeV)) {
                    is OneRmResult.Estimate -> ResultLine(
                        stringResource(R.string.sm_e1rm_rpe, fmt(Rpe.rir(rpeV), 1)),
                        "${fmt(e.kg, 2)} kg"
                    )
                    is OneRmResult.Refused -> Text(refusalText(e.reason), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PrescriptionCard(table: FTable, settings: StrengthSettings) {
    var oneRm by rememberSaveable { mutableStateOf("200") }
    var r by rememberSaveable { mutableStateOf("5") }
    var rpe by rememberSaveable { mutableStateOf("8") }
    SectionCard(stringResource(R.string.sm_calc_prescription)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_one_rm), oneRm, { oneRm = it }, Modifier.weight(1f), suffix = "kg")
            NumberField(stringResource(R.string.sm_reps), r, { r = it }, Modifier.weight(1f), integer = true)
            NumberField(stringResource(R.string.sm_rpe), rpe, { rpe = it }, Modifier.weight(1f))
        }
        val o = parseDecimal(oneRm)
        val rv = r.trim().toIntOrNull()
        val rpeV = parseDecimal(rpe)
        if (o != null && rv != null && rpeV != null) {
            if (!Rpe.isValid(rpeV)) {
                Text(stringResource(R.string.sm_refuse_rpe), color = MaterialTheme.colorScheme.error)
            } else if (rv !in 1..OneRepMax.MAX_REPS) {
                Text(stringResource(R.string.sm_refuse_reps_high), color = MaterialTheme.colorScheme.error)
            } else {
                val f = table.fraction(rv, rpeV)
                ResultLine(stringResource(R.string.sm_fraction), "${fmt(f * 100, 2)} %")
                Text(
                    stringResource(R.string.sm_prescribed_load, fmtKg(table.prescribe(o, rv, rpeV, settings.incrementKg)), fmtKg(settings.incrementKg)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DotsCard(settings: StrengthSettings) {
    var total by rememberSaveable { mutableStateOf("600") }
    var bw by rememberSaveable { mutableStateOf(settings.bodyweightKg?.let(::fmtKg) ?: "80") }
    var female by rememberSaveable { mutableStateOf(settings.sex == Sex.FEMALE) }
    SectionCard(stringResource(R.string.sm_calc_dots)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_total), total, { total = it }, Modifier.weight(1f), suffix = "kg")
            NumberField(stringResource(R.string.sm_bodyweight), bw, { bw = it }, Modifier.weight(1f), suffix = "kg")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !female, onClick = { female = false }, label = { Text(stringResource(R.string.sm_male)) })
            FilterChip(selected = female, onClick = { female = true }, label = { Text(stringResource(R.string.sm_female)) })
        }
        val t = parseDecimal(total)
        val b = parseDecimal(bw)
        if (t != null && b != null) {
            val sex = if (female) Sex.FEMALE else Sex.MALE
            val coef = Dots.coefficient(sex, b)
            if (coef == null) {
                Text(stringResource(R.string.sm_dots_invalid), color = MaterialTheme.colorScheme.error)
            } else {
                ResultLine(stringResource(R.string.sm_dots_coefficient), fmt(coef, 4))
                Text(stringResource(R.string.sm_dots_score, fmt(t * coef, 1)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PrilepinCard() {
    var pct by rememberSaveable { mutableStateOf("80") }
    var reps by rememberSaveable { mutableStateOf("15") }
    SectionCard(stringResource(R.string.sm_calc_prilepin)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_percent_1rm), pct, { pct = it }, Modifier.weight(1f), suffix = "%")
            NumberField(stringResource(R.string.sm_total_reps), reps, { reps = it }, Modifier.weight(1f), integer = true)
        }
        val p = parseDecimal(pct)
        val r = reps.trim().toIntOrNull()
        if (p != null && r != null) {
            val a = Prilepin.assess(p, r)
            if (a == null) {
                Text(stringResource(R.string.sm_prilepin_no_zone))
            } else {
                ResultLine(stringResource(R.string.sm_prilepin_reps_per_set), "${a.zone.repsPerSet.first}–${a.zone.repsPerSet.last}")
                ResultLine(stringResource(R.string.sm_prilepin_optimal), "${a.zone.optimalTotal} (${a.zone.totalRange.first}–${a.zone.totalRange.last})")
                val verdict = when (a.verdict) {
                    PrilepinVerdict.BELOW_RANGE -> stringResource(R.string.sm_prilepin_below)
                    PrilepinVerdict.WITHIN_RANGE -> stringResource(R.string.sm_prilepin_within)
                    PrilepinVerdict.ABOVE_RANGE -> stringResource(R.string.sm_prilepin_above)
                }
                Text(verdict, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FTableCard(table: FTable, repository: StrongmanRepository) {
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<Pair<Int, Double>?>(null) }
    val rpes = Rpe.values.reversed()
    SectionCard(stringResource(R.string.sm_ftable_title)) {
        Text(stringResource(R.string.sm_ftable_hint), style = MaterialTheme.typography.bodySmall)
        Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp)) {
            Row {
                Box(Modifier.width(40.dp)) { Text(stringResource(R.string.sm_reps_short), style = MaterialTheme.typography.labelSmall) }
                rpes.forEach { rpe -> Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) { Text(fmt(rpe, 1), style = MaterialTheme.typography.labelSmall) } }
            }
            for (reps in 1..12) {
                Row {
                    Box(Modifier.width(40.dp).padding(vertical = 6.dp)) { Text("$reps", style = MaterialTheme.typography.labelMedium) }
                    rpes.forEach { rpe ->
                        val overridden = table.isOverridden(reps, rpe)
                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 32.dp)
                                .padding(1.dp)
                                .background(
                                    if (overridden) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { editing = reps to rpe },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(fmt(table.fraction(reps, rpe) * 100, 1), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
    editing?.let { (reps, rpe) ->
        var text by remember(reps, rpe) { mutableStateOf(fmt(table.fraction(reps, rpe) * 100, 2)) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(stringResource(R.string.sm_ftable_edit, reps, fmt(rpe, 1))) },
            text = {
                Column {
                    NumberField(stringResource(R.string.sm_fraction), text, { text = it }, suffix = "%")
                    Text(stringResource(R.string.sm_ftable_formula, fmt(table.formula(reps, rpe) * 100, 2)), style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = parseDecimal(text)
                    if (v != null && v > 0.0 && v <= 100.0) {
                        scope.launch { repository.setOverride(reps, rpe, v / 100.0) }
                        editing = null
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { scope.launch { repository.clearOverride(reps, rpe) }; editing = null }) {
                        Text(stringResource(R.string.sm_ftable_reset))
                    }
                    TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) }
                }
            }
        )
    }
}

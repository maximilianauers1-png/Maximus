package app.maximus.ui.strongman

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.strongman.data.SetLogEntity
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Dots
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.OneRepMax
import app.maximus.strongman.domain.OneRmMethod
import app.maximus.strongman.domain.OneRmResult
import app.maximus.strongman.domain.Prilepin
import app.maximus.strongman.domain.PrilepinVerdict
import app.maximus.strongman.domain.RefusalReason
import app.maximus.strongman.domain.Rpe
import app.maximus.strongman.domain.Sex
import app.maximus.ui.components.Hairline
import app.maximus.ui.components.PlateCard
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun refusalText(reason: RefusalReason): String = when (reason) {
    RefusalReason.NON_POSITIVE_LOAD -> stringResource(R.string.sm_refuse_load)
    RefusalReason.REPS_BELOW_ONE -> stringResource(R.string.sm_refuse_reps_low)
    RefusalReason.REPS_ABOVE_LIMIT -> stringResource(R.string.sm_refuse_reps_high)
    RefusalReason.INVALID_RPE -> stringResource(R.string.sm_refuse_rpe)
}

@Composable
fun CalculatorTab(
    repository: StrongmanRepository,
    requestedSetId: Long?,
    onRequestedSetHandled: () -> Unit
) {
    val table by repository.fTable.collectAsState(initial = FTable())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    val latestSet by repository.latestStrengthSet.collectAsState(initial = null)
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    var targetMode by rememberSaveable { mutableStateOf(false) }
    var weight by rememberSaveable { mutableStateOf("") }
    var reps by rememberSaveable { mutableStateOf("5") }
    var rpe by rememberSaveable { mutableStateOf<Double?>(null) }
    var importedExercise by rememberSaveable { mutableStateOf<Long?>(null) }
    var missingSet by rememberSaveable { mutableStateOf(false) }
    var oneRm by rememberSaveable { mutableStateOf("") }
    var exactOneRm by rememberSaveable { mutableStateOf<Double?>(null) }
    var targetReps by rememberSaveable { mutableStateOf("5") }
    var targetRpe by rememberSaveable { mutableDoubleStateOf(8.0) }
    var showMore by rememberSaveable { mutableStateOf(false) }
    val extras = rememberSaveableStateHolder()
    val scroll = rememberScrollState()

    fun importSet(set: SetLogEntity) {
        weight = fmtKg(set.weightKg).takeIf { parseDecimal(it) == set.weightKg } ?: set.weightKg.toString()
        reps = set.reps.toString()
        rpe = set.rpe?.takeIf(Rpe::isValid)
        importedExercise = set.exerciseId
        targetMode = false
        missingSet = false
    }

    LaunchedEffect(requestedSetId) {
        if (requestedSetId != null) {
            val set = repository.calculatorSet(requestedSetId)
            if (set != null) importSet(set) else missingSet = true
            scroll.scrollTo(0)
            onRequestedSetHandled()
        }
    }
    LaunchedEffect(targetMode) { scroll.scrollTo(0) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(R.string.sm_calc_mode_estimate, R.string.sm_calc_mode_target).forEachIndexed { index, label ->
                val target = index == 1
                SegmentedButton(
                    selected = targetMode == target,
                    onClick = { targetMode = target },
                    shape = SegmentedButtonDefaults.itemShape(index, count = 2),
                    label = { Text(stringResource(label)) }
                )
            }
        }
        if (!targetMode) {
            if (missingSet) {
                Text(stringResource(R.string.sm_calc_set_missing), color = MaterialTheme.colorScheme.error)
            }
            EstimatePanel(
                table = table, weight = weight, reps = reps, rpe = rpe,
                exercise = exercises.firstOrNull { it.id == importedExercise }?.displayName(),
                onWeightChange = { weight = it }, onRepsChange = { reps = it }, onRpeChange = { rpe = it },
                onUseLatest = latestSet?.let { set -> { importSet(set) } },
                onUseEstimate = {
                    exactOneRm = it
                    oneRm = loadKg(it, 0.01)
                    targetMode = true
                }
            )
        } else {
            TargetPanel(
                table = table, increment = settings.incrementKg, oneRm = oneRm,
                exactOneRm = exactOneRm, reps = targetReps, rpe = targetRpe,
                onOneRmChange = { oneRm = it; exactOneRm = null },
                onRepsChange = { targetReps = it }, onRpeChange = { targetRpe = it }
            )
        }

        OutlinedButton(onClick = { showMore = !showMore }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (showMore) R.string.sm_calc_hide_extra else R.string.sm_calc_show_extra))
        }
        if (showMore) {
            extras.SaveableStateProvider("additional-calculators") {
                DotsCard(settings)
                PrilepinCard()
                FTableCard(table, repository)
            }
        }
    }
}

@Composable
private fun EstimatePanel(
    table: FTable,
    weight: String,
    reps: String,
    rpe: Double?,
    exercise: String?,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (Double?) -> Unit,
    onUseLatest: (() -> Unit)?,
    onUseEstimate: (Double) -> Unit
) {
    val load = parseDecimal(weight)
    val count = reps.trim().toIntOrNull()
    val result = if (load != null && count != null) {
        if (rpe == null) OneRepMax.estimate(load, count) else table.e1rm(load, count, rpe)
    } else {
        null
    }
    var showMethods by rememberSaveable { mutableStateOf(false) }

    SectionCard(stringResource(R.string.sm_calc_work_set)) {
        if (exercise != null) Text(exercise, style = MaterialTheme.typography.titleSmall)
        if (onUseLatest != null) {
            TextButton(onClick = onUseLatest) { Text(stringResource(R.string.sm_calc_use_latest)) }
        }
        LoadAndReps(weight, reps, R.string.sm_weight, onWeightChange, onRepsChange)
        RpeSelector(rpe, onRpeChange, optional = true)
    }
    when (result) {
        is OneRmResult.Estimate -> {
            if (!result.kg.isFinite()) {
                CalculatorError(R.string.sm_calc_number_range)
            } else {
                val method = when (result.method) {
                    OneRmMethod.RPE_TABLE -> R.string.sm_calc_method_rpe
                    OneRmMethod.MEAN_EPLEY_BRZYCKI -> R.string.sm_calc_method_mean
                    else -> R.string.sm_epley
                }
                LoadResult(R.string.sm_calc_e1rm, result.kg, stringResource(method)) {
                    Button(onClick = { onUseEstimate(result.kg) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.sm_calc_use_estimate))
                    }
                }
                if (load != null && count != null) {
                    TextButton(onClick = { showMethods = !showMethods }) {
                        Text(stringResource(if (showMethods) R.string.sm_calc_hide_methods else R.string.sm_calc_show_methods))
                    }
                    if (showMethods) {
                        SectionCard(stringResource(R.string.sm_calc_show_methods)) {
                            ResultLine(stringResource(R.string.sm_epley), "${fmtKg(OneRepMax.epley(load, count))} kg")
                            if (count <= OneRepMax.BRZYCKI_MAX_REPS) {
                                ResultLine(stringResource(R.string.sm_brzycki), "${fmtKg(OneRepMax.brzycki(load, count))} kg")
                            }
                            ResultLine(stringResource(R.string.sm_lombardi), "${fmtKg(OneRepMax.lombardi(load, count))} kg")
                        }
                    }
                }
            }
        }
        is OneRmResult.Refused -> Text(refusalText(result.reason), color = MaterialTheme.colorScheme.error)
        null -> if (weight.isBlank()) CalculatorHint(R.string.sm_calc_estimate_hint)
    }
}

@Composable
private fun TargetPanel(
    table: FTable,
    increment: Double,
    oneRm: String,
    exactOneRm: Double?,
    reps: String,
    rpe: Double,
    onOneRmChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onRpeChange: (Double) -> Unit
) {
    SectionCard(stringResource(R.string.sm_calc_target_set)) {
        LoadAndReps(oneRm, reps, R.string.sm_one_rm, onOneRmChange, onRepsChange)
        RpeSelector(rpe, { if (it != null) onRpeChange(it) })
    }
    val load = exactOneRm ?: parseDecimal(oneRm)
    val count = reps.trim().toIntOrNull()
    when {
        oneRm.isBlank() -> CalculatorHint(R.string.sm_calc_target_hint)
        load == null || count == null -> Unit
        load <= 0 -> CalculatorError(R.string.sm_refuse_load)
        count < 1 -> CalculatorError(R.string.sm_refuse_reps_low)
        count > OneRepMax.MAX_REPS -> CalculatorError(R.string.sm_refuse_reps_high)
        else -> {
            val fraction = table.fraction(count, rpe)
            val rounded = table.prescribe(load, count, rpe, increment)
            if (!rounded.isFinite()) {
                CalculatorError(R.string.sm_calc_number_range)
                return
            }
            val step = loadKg(increment, increment)
            LoadResult(
                title = R.string.sm_calc_target_load,
                kg = rounded.takeIf { it > 0.0 },
                subtitle = stringResource(R.string.sm_calc_rounding, step),
                increment = increment
            ) {
                ResultLine(
                    stringResource(R.string.sm_calc_raw_load),
                    stringResource(R.string.sm_calc_kg, loadKg(load * fraction, increment))
                )
                ResultLine(stringResource(R.string.sm_fraction), "${fmt(fraction * 100, 1)} %")
            }
            val neighbors = listOf(rpe - 0.5, rpe + 0.5).filter(Rpe::isValid)
            SectionCard(stringResource(R.string.sm_calc_neighbor_rpe)) {
                neighbors.forEach { candidate ->
                    val neighbor = table.prescribe(load, count, candidate, increment)
                    if (neighbor.isFinite()) {
                        ResultLine(
                            stringResource(R.string.sm_calc_rpe_value, fmtKg(candidate)),
                            if (neighbor > 0.0) stringResource(R.string.sm_calc_kg, loadKg(neighbor, increment))
                            else stringResource(R.string.sm_calc_below_step)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadAndReps(
    weight: String,
    reps: String,
    @StringRes label: Int,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField(stringResource(label), weight, onWeightChange, Modifier.weight(1f), suffix = "kg")
        NumberField(stringResource(R.string.sm_reps), reps, onRepsChange, Modifier.weight(1f), integer = true)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (quick in listOf(1, 3, 5, 8, 10)) {
            val description = stringResource(R.string.sm_calc_quick_reps, quick)
            FilterChip(
                selected = reps.trim().toIntOrNull() == quick,
                onClick = { onRepsChange(quick.toString()) },
                label = { Text(quick.toString()) },
                modifier = Modifier.semantics {
                    contentDescription = description
                }
            )
        }
    }
}

@Composable
private fun RpeSelector(rpe: Double?, onChange: (Double?) -> Unit, optional: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(if (optional) R.string.sm_rpe_optional else R.string.sm_rpe), style = MaterialTheme.typography.titleSmall)
        if (rpe != null) {
            Text(
                stringResource(R.string.sm_calc_rir, fmtKg(Rpe.rir(rpe))),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.Center) {
        if (optional) {
            FilterChip(rpe == null, onClick = { onChange(null) }, label = { Text(stringResource(R.string.sm_calc_no_rpe)) })
        }
        val common = listOf(7.0, 8.0, 9.0, 10.0)
        common.forEach { choice ->
            FilterChip(rpe == choice, onClick = { onChange(choice) }, label = { Text(fmtKg(choice)) })
        }
        Box {
            FilterChip(
                selected = rpe != null && rpe !in common,
                onClick = { expanded = true },
                label = { Text(if (rpe != null && rpe !in common) fmtKg(rpe) else stringResource(R.string.sm_calc_all_rpe)) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Rpe.values.forEach { choice ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.sm_calc_rpe_value, fmtKg(choice))) },
                        onClick = { onChange(choice); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadResult(
    @StringRes title: Int,
    kg: Double?,
    subtitle: String,
    increment: Double = 0.01,
    content: @Composable () -> Unit
) {
    PlateCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(
                if (kg == null) stringResource(R.string.sm_calc_below_step) else stringResource(R.string.sm_calc_kg, loadKg(kg, increment)),
                style = if (kg == null) MaterialTheme.typography.titleLarge
                else MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.primary
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Hairline()
            content()
        }
    }
}

private fun loadKg(kg: Double, increment: Double): String {
    val decimals = BigDecimal.valueOf(increment).stripTrailingZeros().scale().coerceIn(2, 10)
    if (kg > 0 && (kg < 1e-10 || kg >= 1e9)) return String.format(Locale.getDefault(), "%.4g", kg)
    return fmt(kg, decimals).trimEnd('0').trimEnd(',', '.')
}

@Composable
private fun CalculatorHint(@StringRes message: Int) {
    Text(stringResource(message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun CalculatorError(@StringRes message: Int) {
    Text(stringResource(message), color = MaterialTheme.colorScheme.error)
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
            for (reps in 1..OneRepMax.MAX_REPS) {
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

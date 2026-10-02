@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.R
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.PlateCalculator
import app.maximus.strongman.domain.PlateResult
import app.maximus.strongman.domain.Sex
import app.maximus.strongman.domain.WarmupLadder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ToolsTab(repository: StrongmanRepository) {
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RestTimerCard()
        IntervalTimerCard()
        PlateCard(settings)
        WarmupCard(settings)
        SettingsCard(settings) { s -> repository.saveSettings(s) }
    }
}

@Composable
private fun rememberBeeper(): () -> Unit {
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90) }.getOrNull() }
    DisposableEffect(Unit) { onDispose { tone?.release() } }
    return { tone?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250) }
}

@Composable
private fun KeepScreenOn(active: Boolean) {
    val view = LocalView.current
    DisposableEffect(active) {
        view.keepScreenOn = active
        onDispose { view.keepScreenOn = false }
    }
}

private fun mmss(ms: Long): String {
    val s = (ms + 999) / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

/** Countdown driven by SystemClock.elapsedRealtime(), so it cannot drift with frame timing. */
@Composable
private fun RestTimerCard() {
    val beep = rememberBeeper()
    var durationS by rememberSaveable { mutableStateOf(180) }
    var running by rememberSaveable { mutableStateOf(false) }
    var endAt by rememberSaveable { mutableLongStateOf(0L) }
    var remainingMs by rememberSaveable { mutableLongStateOf(180_000L) }
    KeepScreenOn(running)
    LaunchedEffect(running) {
        while (running) {
            val rem = endAt - SystemClock.elapsedRealtime()
            remainingMs = rem.coerceAtLeast(0L)
            if (rem <= 0L) { running = false; beep() }
            delay(100)
        }
    }
    SectionCard(stringResource(R.string.sm_rest_timer)) {
        Text(mmss(remainingMs), fontSize = 48.sp, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(60, 90, 120, 180, 240, 300).forEach { s ->
                FilterChip(selected = durationS == s, onClick = {
                    durationS = s
                    if (!running) remainingMs = s * 1000L
                }, label = { Text(mmss(s * 1000L)) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (running) {
                    running = false
                } else {
                    if (remainingMs <= 0L) remainingMs = durationS * 1000L
                    endAt = SystemClock.elapsedRealtime() + remainingMs
                    running = true
                }
            }) { Text(stringResource(if (running) R.string.timer_pause else R.string.timer_start)) }
            OutlinedButton(onClick = { running = false; remainingMs = durationS * 1000L }) { Text(stringResource(R.string.timer_reset)) }
        }
    }
}

/**
 * Interval / EMOM timer. With cycle c = work + rest and elapsed time t:
 * round = floor(t / c) + 1, phase = WORK if (t mod c) < work else REST.
 * EMOM is the special case work = 60 s, rest = 0.
 */
@Composable
private fun IntervalTimerCard() {
    val beep = rememberBeeper()
    var work by rememberSaveable { mutableStateOf("60") }
    var rest by rememberSaveable { mutableStateOf("0") }
    var rounds by rememberSaveable { mutableStateOf("10") }
    var running by rememberSaveable { mutableStateOf(false) }
    var accumulatedMs by rememberSaveable { mutableLongStateOf(0L) }
    var startedAt by rememberSaveable { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    KeepScreenOn(running)

    val workMs = (work.trim().toIntOrNull() ?: 0).coerceAtLeast(1) * 1000L
    val restMs = (rest.trim().toIntOrNull() ?: 0).coerceAtLeast(0) * 1000L
    val totalRounds = (rounds.trim().toIntOrNull() ?: 1).coerceAtLeast(1)
    val cycle = workMs + restMs
    val elapsed = accumulatedMs + if (running) now - startedAt else 0L
    val finished = elapsed >= cycle * totalRounds
    val round = (elapsed / cycle + 1).coerceAtMost(totalRounds.toLong())
    val within = elapsed % cycle
    val inWork = within < workMs
    val phaseRemaining = if (finished) 0L else if (inWork) workMs - within else cycle - within
    val phaseKey = if (finished) -1L else elapsed / cycle * 2 + if (inWork) 0 else 1

    var lastPhase by remember { mutableLongStateOf(phaseKey) }
    LaunchedEffect(phaseKey) {
        if (running && phaseKey != lastPhase) beep()
        lastPhase = phaseKey
    }
    LaunchedEffect(running) {
        while (running) {
            now = SystemClock.elapsedRealtime()
            if (accumulatedMs + now - startedAt >= cycle * totalRounds) {
                accumulatedMs = cycle * totalRounds
                running = false
                beep()
            }
            delay(100)
        }
    }

    SectionCard(stringResource(R.string.sm_interval_timer)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_work_s), work, { work = it }, Modifier.weight(1f), integer = true)
            NumberField(stringResource(R.string.sm_rest_s), rest, { rest = it }, Modifier.weight(1f), integer = true)
            NumberField(stringResource(R.string.sm_rounds), rounds, { rounds = it }, Modifier.weight(1f), integer = true)
        }
        Text(
            when {
                finished -> stringResource(R.string.sm_interval_done)
                inWork -> stringResource(R.string.sm_interval_work, round, totalRounds)
                else -> stringResource(R.string.sm_interval_rest, round, totalRounds)
            },
            style = MaterialTheme.typography.titleMedium
        )
        Text(mmss(phaseRemaining), fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (running) {
                    accumulatedMs += SystemClock.elapsedRealtime() - startedAt
                    running = false
                } else if (!finished) {
                    startedAt = SystemClock.elapsedRealtime()
                    now = startedAt
                    running = true
                }
            }) { Text(stringResource(if (running) R.string.timer_pause else R.string.timer_start)) }
            OutlinedButton(onClick = { running = false; accumulatedMs = 0L }) { Text(stringResource(R.string.timer_reset)) }
            OutlinedButton(onClick = { work = "60"; rest = "0" }) { Text(stringResource(R.string.sm_emom)) }
        }
    }
}

@Composable
private fun PlateCard(settings: StrengthSettings) {
    var target by rememberSaveable { mutableStateOf("140") }
    SectionCard(stringResource(R.string.sm_plate_calculator)) {
        NumberField(stringResource(R.string.sm_target_weight), target, { target = it }, suffix = "kg")
        Text(stringResource(R.string.sm_plate_bar, fmtKg(settings.barKg)), style = MaterialTheme.typography.bodySmall)
        parseDecimal(target)?.let { t ->
            when (val r = PlateCalculator.solve(t, settings.barKg, settings.plates)) {
                is PlateResult.Loadable -> {
                    Text(
                        if (r.perSide.isEmpty()) stringResource(R.string.sm_plate_empty_bar)
                        else stringResource(R.string.sm_plate_per_side, r.perSide.joinToString(" + ") { fmtKg(it) }),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!r.usedGreedy) Text(stringResource(R.string.sm_plate_dp_note), style = MaterialTheme.typography.bodySmall)
                }
                is PlateResult.NotLoadable -> {
                    Text(stringResource(R.string.sm_plate_not_loadable), color = MaterialTheme.colorScheme.error)
                    val options = listOfNotNull(r.nearestBelowKg, r.nearestAboveKg).joinToString(" / ") { "${fmtKg(it)} kg" }
                    if (options.isNotEmpty()) Text(stringResource(R.string.sm_plate_nearest, options))
                }
            }
        }
    }
}

@Composable
private fun WarmupCard(settings: StrengthSettings) {
    var work by rememberSaveable { mutableStateOf("180") }
    SectionCard(stringResource(R.string.sm_warmup)) {
        NumberField(stringResource(R.string.sm_work_weight), work, { work = it }, suffix = "kg")
        parseDecimal(work)?.let { w ->
            val steps = WarmupLadder.build(w, settings.barKg, settings.incrementKg)
            if (steps.isEmpty()) Text(stringResource(R.string.sm_warmup_none))
            steps.forEach { s -> Text("${fmtKg(s.kg)} kg × ${s.reps}", style = MaterialTheme.typography.bodyLarge) }
            Text("${fmtKg(w)} kg — ${stringResource(R.string.sm_work_set)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsCard(settings: StrengthSettings, onSave: suspend (StrengthSettings) -> Unit) {
    val scope = rememberCoroutineScope()
    var bw by remember(settings) { mutableStateOf(settings.bodyweightKg?.let(::fmtKg) ?: "") }
    var inc by remember(settings) { mutableStateOf(fmtKg(settings.incrementKg)) }
    var bar by remember(settings) { mutableStateOf(fmtKg(settings.barKg)) }
    var plates by remember(settings) { mutableStateOf(settings.plates.joinToString("; ") { "${fmtKg(it.kg)}×${it.pairs}" }) }
    var female by remember(settings) { mutableStateOf(settings.sex == Sex.FEMALE) }

    val parsedPlates = plates.split(';').mapNotNull { part ->
        val bits = part.split('×', 'x', 'X')
        val kg = bits.getOrNull(0)?.let(::parseDecimal)
        val pairs = bits.getOrNull(1)?.trim()?.toIntOrNull()
        if (kg != null && pairs != null && kg > 0.0 && pairs >= 0) app.maximus.strongman.domain.PlateStock(kg, pairs) else null
    }
    val incV = parseDecimal(inc)
    val barV = parseDecimal(bar)
    val bwV = bw.takeIf { it.isNotBlank() }?.let(::parseDecimal)
    val valid = incV != null && incV > 0.0 && barV != null && barV >= 0.0 && parsedPlates.isNotEmpty() && (bw.isBlank() || bwV != null)

    SectionCard(stringResource(R.string.sm_strength_settings)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField(stringResource(R.string.sm_bodyweight), bw, { bw = it }, Modifier.weight(1f), suffix = "kg")
            NumberField(stringResource(R.string.sm_increment), inc, { inc = it }, Modifier.weight(1f), suffix = "kg")
            NumberField(stringResource(R.string.sm_bar), bar, { bar = it }, Modifier.weight(1f), suffix = "kg")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !female, onClick = { female = false }, label = { Text(stringResource(R.string.sm_male)) })
            FilterChip(selected = female, onClick = { female = true }, label = { Text(stringResource(R.string.sm_female)) })
        }
        androidx.compose.material3.OutlinedTextField(
            plates, { plates = it },
            label = { Text(stringResource(R.string.sm_plates_stock)) },
            supportingText = { Text(stringResource(R.string.sm_plates_hint)) }
        )
        Button(enabled = valid, onClick = {
            scope.launch {
                onSave(StrengthSettings(if (female) Sex.FEMALE else Sex.MALE, bwV, incV!!, barV!!, parsedPlates))
            }
        }) { Text(stringResource(R.string.save)) }
    }
}

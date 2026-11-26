@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.nutrition

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.nutrition.data.toDayPoint
import app.maximus.nutrition.domain.CheckInNote
import app.maximus.nutrition.domain.CheckInVerdict
import app.maximus.nutrition.domain.CoachEngine
import app.maximus.nutrition.domain.CoachPhase
import app.maximus.nutrition.domain.CoachPlan
import app.maximus.nutrition.domain.DerivedTargets
import app.maximus.nutrition.domain.MacroCalculator
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.RecipeBook
import app.maximus.nutrition.domain.WeekBook
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.strongman.ResultLine
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.strongman.fmt
import app.maximus.ui.strongman.fmtDay
import app.maximus.ui.strongman.parseDecimal
import kotlinx.coroutines.launch

@Composable
fun phaseLabel(ph: CoachPhase) = stringResource(
    when (ph) {
        CoachPhase.MAINTENANCE -> R.string.nc_phase_maintenance
        CoachPhase.RECOMP -> R.string.nc_phase_recomp
        CoachPhase.CUT -> R.string.nc_phase_cut
        CoachPhase.DIET_BREAK -> R.string.nc_phase_break
    }
)

@Composable
private fun phaseExplain(ph: CoachPhase) = stringResource(
    when (ph) {
        CoachPhase.MAINTENANCE -> R.string.nc_phase_maintenance_explain
        CoachPhase.RECOMP -> R.string.nc_phase_recomp_explain
        CoachPhase.CUT -> R.string.nc_phase_cut_explain
        CoachPhase.DIET_BREAK -> R.string.nc_phase_break_explain
    }
)

@Composable
fun slotLabel(slot: Int) = stringResource(
    when (slot) {
        0 -> R.string.nc_slot_1; 1 -> R.string.nc_slot_2; 2 -> R.string.nc_slot_3; 3 -> R.string.nc_slot_4; else -> R.string.nc_slot_5
    }
)

/** The coach: phase, targets, today's meals, weekly check-in, body-composition goal and the coach's rules. */
@Composable
fun CoachTab(
    p: NutritionProfile,
    d: DerivedTargets,
    log: List<NutritionLogEntity>,
    today: Long,
    week: WeekBook,
    book: RecipeBook,
    repo: NutritionRepository,
    onSave: ((NutritionProfile) -> NutritionProfile) -> Unit
) {
    val de = isGerman()
    val scope = rememberCoroutineScope()
    val points = remember(log) { log.map { it.toDayPoint() } }
    val trend = remember(points) { CoachEngine.trendWeight(points) }
    val c = d.coach
    val todayIdx = WeekBook.dayIndex(today)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // ---------- coach and phase
        item {
            SectionCard(stringResource(R.string.nc_title)) {
                Text(stringResource(R.string.nc_coach_line, CoachPlan.COACH_NAME), style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.nc_mode), modifier = Modifier.weight(1f))
                    Switch(p.coachMode, { onSave { current -> current.copy(coachMode = it) } })
                }
                if (!p.coachMode) {
                    Text(stringResource(R.string.nc_mode_off), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                } else {
                    Text(stringResource(R.string.nc_phase), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        CoachPhase.entries.forEach { ph ->
                            FilterChip(p.phase == ph, {
                                if (p.phase != ph) onSave { current ->
                                    if (current.phase == ph) current else current.copy(phase = ph, phaseStartDay = today, coachAdjustKcal = 0.0, lastCheckInDay = null)
                                }
                            }, label = { Text(phaseLabel(ph)) })
                        }
                    }
                    Text(phaseExplain(p.phase), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    p.phaseStartDay?.let {
                        Text(stringResource(R.string.nc_phase_since, fmtDay(it), ((today - it) / 7).toInt()), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                    if (p.phase != CoachPhase.MAINTENANCE) {
                        Text(stringResource(R.string.nc_coach_agree), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }

        // ---------- daily targets with derivation
        item {
            val m = d.macros
            SectionCard(stringResource(R.string.nc_targets)) {
                ResultLine(stringResource(R.string.nu_kcal_target), "${fmt(m.kcal, 0)} kcal")
                ResultLine(stringResource(R.string.nu_protein), "${fmt(m.proteinG, 0)} g  (${fmt(m.proteinG / p.weightKg, 1)} g/kg)")
                ResultLine(stringResource(R.string.nu_carbs), "${fmt(m.carbG, 0)} g  (${fmt(m.carbG / p.weightKg, 1)} g/kg)")
                ResultLine(stringResource(R.string.nu_fat), "${fmt(m.fatG, 0)} g  (${fmt(100 * m.fatKcalShare, 0)} %)")
                ResultLine(stringResource(R.string.nu_fiber), "≥ ${fmt(m.fiberG, 0)} g")
                if (c != null) {
                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    ResultLine(stringResource(if (c.maintenanceCalibrated) R.string.nc_maint_measured else R.string.nc_maint_coach), "${fmt(c.maintenanceKcal, 0)} kcal")
                    if (c.plannedDeficit > 0) ResultLine(stringResource(R.string.nc_planned_deficit, fmt(-c.phase.targetRatePercent, 2)), "−${fmt(c.plannedDeficit, 0)} kcal")
                    if (c.adjustment != 0.0) ResultLine(stringResource(R.string.nc_adjustments), "${if (c.adjustment > 0) "+" else "−"}${fmt(kotlin.math.abs(c.adjustment), 0)} kcal")
                    Text(stringResource(R.string.nc_targets_formula), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val (opt, _) = MacroCalculator.proteinPerMeal(p.weightKg, 5, m.proteinG)
                ResultLine(stringResource(R.string.nc_protein_per_meal), "${fmt(m.proteinG / 5, 0)} g / ≥ ${fmt(opt, 0)} g")
            }
        }

        // ---------- today
        item {
            val entries = week.days[todayIdx]
            val eaten = week.dayTotals(todayIdx, book::resolve, eatenOnly = true)
            val planned = week.dayTotals(todayIdx, book::resolve)
            SectionCard(stringResource(R.string.nc_today)) {
                if (entries.all { it == null }) {
                    Text(stringResource(R.string.nc_today_empty), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = {
                        scope.launch { repo.saveWeek(WeekBook.coachWeek(week.weekId).let { cw -> if (week.isEmpty) cw else week.copy(days = week.days.mapIndexed { i, dd -> if (i == todayIdx) cw.days[i] else dd }) }) }
                    }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text(stringResource(if (week.isEmpty) R.string.nc_fill_week_coach else R.string.nc_fill_today_coach)) }
                } else {
                    entries.forEachIndexed { slot, e ->
                        if (e == null) return@forEachIndexed
                        val r = book.resolve(e.recipeKey)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(e.eaten, { scope.launch { repo.saveWeek(week.toggleEaten(todayIdx, slot)) } })
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slotLabel(slot), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(r?.name(de) ?: e.recipeKey, style = MaterialTheme.typography.bodyMedium)
                            }
                            r?.let { Text("${fmt(it.nutrients.kcal * e.portions, 0)} kcal · ${fmt(it.nutrients.proteinG * e.portions, 0)} P", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                    Text(stringResource(R.string.nc_eaten_of, fmt(eaten.kcal, 0), fmt(d.macros.kcal, 0)), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                    LinearProgressIndicator(progress = { (eaten.kcal / d.macros.kcal).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.nc_protein_of, fmt(eaten.proteinG, 0), fmt(d.macros.proteinG, 0)), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                    LinearProgressIndicator(progress = { (eaten.proteinG / d.macros.proteinG).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    Text(stringResource(R.string.nc_planned_day, fmt(planned.kcal, 0), fmt(planned.proteinG, 0)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    OutlinedButton(
                        enabled = eaten.kcal > 0,
                        onClick = {
                            val old = log.firstOrNull { it.epochDay == today }
                            scope.launch { repo.saveDay(NutritionLogEntity(today, old?.weightKg, eaten.kcal, eaten.proteinG, old?.note ?: "")) }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) { Text(stringResource(R.string.nc_to_journal)) }
                }
            }
        }

        // ---------- weekly check-in
        if (p.coachMode && c != null) item {
            val ci = remember(points, c, today) { CoachEngine.checkIn(points, today, trend ?: p.weightKg, c, p.phaseStartDay) }
            val due = p.lastCheckInDay == null || today - p.lastCheckInDay >= 7
            val unit = stringResource(R.string.nc_rate_unit)
            SectionCard(stringResource(R.string.nc_checkin)) {
                Text(
                    stringResource(
                        when (ci.verdict) {
                            CheckInVerdict.NEED_DATA -> R.string.nc_ci_need_data
                            CheckInVerdict.ADHERENCE -> R.string.nc_ci_adherence
                            CheckInVerdict.ON_TRACK -> R.string.nc_ci_on_track
                            CheckInVerdict.ADJUST_DOWN -> R.string.nc_ci_down
                            CheckInVerdict.ADJUST_UP -> R.string.nc_ci_up
                        }
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (ci.verdict == CheckInVerdict.ON_TRACK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                ci.rateObserved?.let { ResultLine(stringResource(R.string.nc_rate_obs), "${fmt(it, 2)} ± ${fmt(ci.rateSe ?: 0.0, 2)} $unit") }
                ResultLine(stringResource(R.string.nc_rate_target), "${fmt(ci.rateTarget, 2)} $unit")
                ResultLine(stringResource(R.string.nc_weighins), "${ci.weighIns} / 14 · ${ci.intakeDays} / 7")
                ci.meanKcal?.let { ResultLine(stringResource(R.string.nc_mean_kcal), "${fmt(it, 0)} / ${fmt(d.macros.kcal, 0)} kcal") }
                ci.meanProtein?.let { ResultLine(stringResource(R.string.nc_mean_protein), "${fmt(it, 0)} / ${fmt(d.macros.proteinG, 0)} g") }
                if (ci.kcalChange != 0.0) {
                    ResultLine(stringResource(R.string.nc_recommend), "${if (ci.kcalChange > 0) "+" else "−"}${fmt(kotlin.math.abs(ci.kcalChange), 0)} kcal/d")
                    Text(stringResource(R.string.nc_change_from_carbs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        enabled = due,
                        onClick = { onSave { current ->
                            if (current.lastCheckInDay == today) current else current.copy(coachAdjustKcal = current.coachAdjustKcal + ci.kcalChange, lastCheckInDay = today)
                        } },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) { Text(stringResource(R.string.nc_apply)) }
                } else if (ci.verdict == CheckInVerdict.ON_TRACK && due) {
                    OutlinedButton(onClick = { onSave { current -> current.copy(lastCheckInDay = today) } }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) { Text(stringResource(R.string.nc_confirm_checkin)) }
                }
                if (!due) Text(stringResource(R.string.nc_next_checkin, fmtDay(p.lastCheckInDay!! + 7)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                ci.notes.forEach { n ->
                    Text(
                        "• " + stringResource(
                            when (n) {
                                CheckInNote.PROTEIN_LOW -> R.string.nc_note_protein
                                CheckInNote.LOSS_TOO_FAST -> R.string.nc_note_too_fast
                                CheckInNote.DIET_BREAK_DUE -> R.string.nc_note_break
                                CheckInNote.LOG_INTAKE -> R.string.nc_note_log
                                CheckInNote.END_DIET_BREAK -> R.string.nc_note_end_break
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(stringResource(R.string.nc_checkin_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
        }

        // ---------- body composition goal
        item {
            var bf by remember(p.bodyFatPercent) { mutableStateOf(p.bodyFatPercent?.let { fmt(it, 1) } ?: "") }
            var target by remember(p.targetBodyFatPercent) { mutableStateOf(fmt(p.targetBodyFatPercent, 1)) }
            SectionCard(stringResource(R.string.nc_body)) {
                ResultLine(stringResource(R.string.nc_weight_profile), "${fmt(p.weightKg, 1)} kg")
                trend?.let { t ->
                    ResultLine(stringResource(R.string.nc_weight_trend), "${fmt(t, 1)} kg")
                    if (kotlin.math.abs(t - p.weightKg) >= 0.3) {
                        OutlinedButton(onClick = { onSave { current -> current.copy(weightKg = Math.round(t * 10) / 10.0) } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.nc_use_trend)) }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(stringResource(R.string.nu_bodyfat), bf, { bf = it }, Modifier.weight(1f), suffix = "%")
                    NumberField(stringResource(R.string.nc_target_bf), target, { target = it }, Modifier.weight(1f), suffix = "%")
                }
                val bfV = parseDecimal(bf)?.takeIf { it in 3.0..60.0 }
                val tV = parseDecimal(target)?.takeIf { it in 5.0..35.0 }
                if ((bfV != p.bodyFatPercent || tV != p.targetBodyFatPercent) && tV != null) {
                    Button(onClick = { onSave { current -> current.copy(bodyFatPercent = bfV, targetBodyFatPercent = tV) } }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
                }
                val pbf = p.bodyFatPercent
                if (pbf == null) {
                    Text(stringResource(R.string.nc_need_bf), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val rate = if (p.phase.targetRatePercent < 0) p.phase.targetRatePercent else CoachPhase.CUT.targetRatePercent
                    val g = CoachEngine.composition(p.weightKg, pbf, p.targetBodyFatPercent, rate)
                    ResultLine(stringResource(R.string.nu_lean_mass), "${fmt(g.leanKg, 1)} kg")
                    ResultLine(stringResource(R.string.nc_fat_mass), "${fmt(g.fatKg, 1)} kg")
                    ResultLine(stringResource(R.string.nc_target_weight), "${fmt(g.targetWeightKg, 1)} kg")
                    ResultLine(stringResource(R.string.nc_fat_to_lose), "${fmt(g.fatToLoseKg, 1)} kg")
                    g.weeks?.let { ResultLine(stringResource(R.string.nc_weeks_at, fmt(-rate, 2)), "≈ ${fmt(it, 0)}") }
                    Text(stringResource(R.string.nc_body_explain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // ---------- coach plan analysis: protein distribution and budget
        item {
            val meals = CoachPlan.meals
            val low = CoachEngine.lowProteinMeals(meals, p.weightKg)
            SectionCard(stringResource(R.string.nc_plan_analysis)) {
                meals.forEachIndexed { i, r ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("M${i + 1}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(32.dp))
                        Text(r.name(de), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text("${fmt(r.nutrients.proteinG, 0)} g P · ${fmt(r.costSek ?: 0.0, 0)} SEK",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (i in low) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                val t = CoachPlan.dayTotal
                ResultLine(stringResource(R.string.nc_day_total), "${fmt(t.kcal, 0)} kcal · ${fmt(t.proteinG, 0)} P · ${fmt(t.carbG, 0)} K · ${fmt(t.fatG, 0)} F")
                ResultLine(stringResource(R.string.nc_cost), "${fmt(CoachPlan.dayCostSek, 0)} SEK / ${fmt(7 * CoachPlan.dayCostSek, 0)} SEK")
                if (low.isNotEmpty()) {
                    Text(stringResource(R.string.nc_low_protein_meal, low.joinToString { "M${it + 1}" }, fmt(0.25 * p.weightKg, 0)),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        // ---------- the coach's rules
        item {
            SectionCard(stringResource(R.string.nc_rules)) {
                listOf(
                    R.string.nc_rule_1, R.string.nc_rule_2, R.string.nc_rule_3, R.string.nc_rule_4,
                    R.string.nc_rule_5, R.string.nc_rule_6, R.string.nc_rule_7, R.string.nc_rule_8
                ).forEachIndexed { i, id ->
                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                        Text("${i + 1}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(22.dp))
                        Text(stringResource(id), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

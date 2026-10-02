@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package app.maximus.ui.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.calendar.data.CalendarEventEntity
import app.maximus.calendar.data.CalendarSnapshot
import app.maximus.calendar.data.Occurrence
import app.maximus.calendar.data.recurrence
import app.maximus.calendar.domain.Frequency
import app.maximus.calendar.domain.Holiday
import app.maximus.calendar.domain.HolidayRegion
import app.maximus.calendar.domain.Holidays
import app.maximus.calendar.domain.MonthGrid
import app.maximus.calendar.domain.Recurrence
import app.maximus.core.app.AppServices
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.PlateCard
import app.maximus.ui.strongman.NumberField
import app.maximus.ui.theme.Palette
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JTextStyle
import java.time.temporal.IsoFields
import java.util.Locale
import kotlinx.coroutines.launch

/** Event colours: steel, blued steel, heraldic red, moss, ochre, pewter. */
val EVENT_COLORS = listOf(Palette.Steel, Palette.Blued, Palette.Heraldic, Color(0xFF8FB39A), Color(0xFFC9A36B), Palette.Muted)

private val TIME = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun CalendarTab(services: AppServices) {
    val repo = services.calendar
    val snapshot by repo.snapshot.collectAsState(initial = CalendarSnapshot(emptyList(), emptyMap()))
    val region by repo.holidayRegion.collectAsState(initial = HolidayRegion.NONE)
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selected by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var editing by remember { mutableStateOf<CalendarEventEntity?>(null) }
    var deleting by remember { mutableStateOf<Occurrence?>(null) }
    val today = LocalDate.now()

    val cells = remember(month) { MonthGrid.cells(month) }
    val byDay = remember(snapshot, month) {
        val map = HashMap<LocalDate, MutableList<Occurrence>>()
        for (o in snapshot.occurrences(cells.first(), cells.last())) {
            var d = maxOf(o.startDay, cells.first())
            val end = minOf(o.endDay, cells.last())
            while (!d.isAfter(end)) { map.getOrPut(d) { ArrayList() } += o; d = d.plusDays(1) }
        }
        map
    }
    val holidays = remember(region, month) { Holidays.inRange(region, cells.first(), cells.last()) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyph.CHEVRON_LEFT, stringResource(R.string.cal_prev), { month = month.minusMonths(1) }, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(month.month.getDisplayName(JTextStyle.FULL_STANDALONE, locale).replaceFirstChar { it.titlecase(locale) }, style = MaterialTheme.typography.headlineSmall)
                    Text("${month.year}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GlyphButton(Glyph.CHEVRON_RIGHT, stringResource(R.string.cal_next), { month = month.plusMonths(1) }, tint = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { month = YearMonth.now(); selected = today }) { Text(stringResource(R.string.cal_today)) }
                Text(stringResource(R.string.cal_holidays), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HolidayRegion.entries.forEach { r ->
                    FilterChip(
                        selected = region == r,
                        onClick = { scope.launch { repo.setHolidayRegion(r) } },
                        label = { Text(when (r) { HolidayRegion.NONE -> stringResource(R.string.cal_none); HolidayRegion.DE -> "DE"; HolidayRegion.SE -> "SE" }) }
                    )
                }
            }
        }
        item { MonthGridView(cells, month, today, selected, byDay, holidays, locale) { selected = it } }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                Text(
                    selected.format(DateTimeFormatter.ofPattern(if (locale.language == "de") "EEEE, d. MMMM" else "EEEE, d MMMM", locale)),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)
                )
                Button(onClick = { editing = newEvent(selected) }) { Text(stringResource(R.string.cal_add)) }
            }
        }
        holidays[selected]?.let { hs ->
            items(hs) { h -> Text(holidayName(h, locale), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary) }
        }
        val agenda = byDay[selected].orEmpty()
        if (agenda.isEmpty() && holidays[selected].isNullOrEmpty()) item { EmptyState(stringResource(R.string.cal_empty_day)) }
        items(agenda) { o -> AgendaRow(o, selected) { editing = o.event } }
    }

    editing?.let { e ->
        val deleteAction: (() -> Unit)? = if (e.id == 0L) null else fun() {
            val occ = byDay[selected]?.firstOrNull { it.event.id == e.id }
            if (e.recurrence().frequency != Frequency.NONE && occ != null) {
                deleting = occ
                editing = null
            } else {
                scope.launch { repo.delete(e.id); editing = null }
            }
        }
        EventEditDialog(
            initial = e,
            onDismiss = { editing = null },
            onSave = { ev -> scope.launch { repo.save(ev); editing = null } },
            onDelete = deleteAction
        )
    }
    deleting?.let { o ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.cal_delete_series_title)) },
            text = { Text(stringResource(R.string.cal_delete_series_text)) },
            confirmButton = {
                Row {
                    TextButton(onClick = { scope.launch { repo.excludeOccurrence(o.event.id, o.startDay); deleting = null } }) { Text(stringResource(R.string.cal_delete_one)) }
                    TextButton(onClick = { scope.launch { repo.delete(o.event.id); deleting = null } }) { Text(stringResource(R.string.cal_delete_all)) }
                }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

private fun holidayName(h: Holiday, locale: Locale) = when (locale.language) { "de" -> h.nameDe; "sv" -> h.nameSv; else -> h.nameEn }

private fun newEvent(day: LocalDate) = CalendarEventEntity(
    title = "", location = "", notes = "", allDay = false,
    startDay = day.toEpochDay(), startMinute = 9 * 60, endDay = day.toEpochDay(), endMinute = 10 * 60,
    frequency = Frequency.NONE.name, interval = 1, weekdayMask = 0, untilDay = null, count = null, color = 0, createdAt = 0
)

@Composable
private fun MonthGridView(
    cells: List<LocalDate>,
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    byDay: Map<LocalDate, List<Occurrence>>,
    holidays: Map<LocalDate, List<Holiday>>,
    locale: Locale,
    onSelect: (LocalDate) -> Unit
) {
    PlateCard {
        Column(modifier = Modifier.padding(8.dp)) {
            Row {
                Text(stringResource(R.string.cal_week_short), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center, modifier = Modifier.width(28.dp))
                DayOfWeek.entries.forEach { d ->
                    Text(
                        d.getDisplayName(JTextStyle.SHORT_STANDALONE, locale).take(2),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (d == DayOfWeek.SUNDAY) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
                    )
                }
            }
            for (week in cells.chunked(7)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${week[0].get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)}",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center, modifier = Modifier.width(28.dp)
                    )
                    for (d in week) {
                        DayCell(
                            day = d, inMonth = YearMonth.from(d) == month, isToday = d == today, isSelected = d == selected,
                            isHoliday = holidays.containsKey(d), events = byDay[d].orEmpty(),
                            modifier = Modifier.weight(1f), onClick = { onSelect(d) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate, inMonth: Boolean, isToday: Boolean, isSelected: Boolean, isHoliday: Boolean,
    events: List<Occurrence>, modifier: Modifier, onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier.aspectRatio(0.9f).padding(2.dp).clip(MaterialTheme.shapes.small)
            .background(if (isSelected) cs.primaryContainer else Color.Transparent)
            .then(if (isToday) Modifier.border(BorderStroke(1.dp, cs.primary), MaterialTheme.shapes.small) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                "${day.dayOfMonth}",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    !inMonth -> cs.outline
                    isSelected -> cs.onPrimaryContainer
                    isHoliday || day.dayOfWeek == DayOfWeek.SUNDAY -> cs.tertiary
                    else -> cs.onSurface
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                events.distinctBy { it.event.id }.take(3).forEach { o ->
                    Box(Modifier.size(5.dp).clip(CircleShape).background(EVENT_COLORS[o.event.color.coerceIn(0, EVENT_COLORS.lastIndex)]))
                }
            }
        }
    }
}

@Composable
private fun AgendaRow(o: Occurrence, day: LocalDate, onClick: () -> Unit) {
    val e = o.event
    PlateCard(onClick = onClick) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 3.dp, height = 36.dp).background(EVENT_COLORS[e.color.coerceIn(0, EVENT_COLORS.lastIndex)]))
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(e.title.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
                val time = when {
                    e.allDay -> stringResource(R.string.cal_all_day)
                    o.startDay == o.endDay -> "${o.start.toLocalTime().format(TIME)}–${o.end.toLocalTime().format(TIME)}"
                    day == o.startDay -> "${o.start.toLocalTime().format(TIME)} →"
                    day == o.endDay -> "→ ${o.end.toLocalTime().format(TIME)}"
                    else -> stringResource(R.string.cal_all_day)
                }
                val sub = listOf(time, e.location).filter { it.isNotBlank() }.joinToString("  ")
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (e.recurrence().frequency != Frequency.NONE) Text("↻", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class EndMode { NEVER, UNTIL, COUNT }
private sealed interface Picker { data object StartDate : Picker; data object EndDate : Picker; data object Until : Picker; data object StartTime : Picker; data object EndTime : Picker }

@Composable
private fun EventEditDialog(initial: CalendarEventEntity, onDismiss: () -> Unit, onSave: (CalendarEventEntity) -> Unit, onDelete: (() -> Unit)?) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFmt = remember(locale) { DateTimeFormatter.ofPattern("EE dd.MM.yyyy", locale) }
    var title by remember { mutableStateOf(initial.title) }
    var location by remember { mutableStateOf(initial.location) }
    var notes by remember { mutableStateOf(initial.notes) }
    var allDay by remember { mutableStateOf(initial.allDay) }
    var startDay by remember { mutableStateOf(LocalDate.ofEpochDay(initial.startDay)) }
    var endDay by remember { mutableStateOf(LocalDate.ofEpochDay(initial.endDay)) }
    var startMin by remember { mutableStateOf(initial.startMinute) }
    var endMin by remember { mutableStateOf(initial.endMinute) }
    val rule0 = initial.recurrence()
    var freq by remember { mutableStateOf(rule0.frequency) }
    var interval by remember { mutableStateOf(rule0.interval.toString()) }
    var mask by remember { mutableStateOf(rule0.weekdayMask) }
    var endMode by remember { mutableStateOf(when { rule0.until != null -> EndMode.UNTIL; rule0.count != null -> EndMode.COUNT; else -> EndMode.NEVER }) }
    var until by remember { mutableStateOf(rule0.until ?: startDay.plusMonths(3)) }
    var count by remember { mutableStateOf((rule0.count ?: 10).toString()) }
    var color by remember { mutableStateOf(initial.color) }
    var picker by remember { mutableStateOf<Picker?>(null) }

    val startKey = startDay.toEpochDay() * 1440 + if (allDay) 0 else startMin
    val endKey = endDay.toEpochDay() * 1440 + if (allDay) 0 else endMin
    val intervalN = interval.trim().toIntOrNull()
    val countN = count.trim().toIntOrNull()
    val valid = title.isNotBlank() && endKey >= startKey && (freq == Frequency.NONE || (intervalN != null && intervalN >= 1)) &&
        (endMode != EndMode.COUNT || (countN != null && countN >= 1)) && (endMode != EndMode.UNTIL || !until.isBefore(startDay))

    fun fmtMin(m: Int) = LocalTime.of(m / 60, m % 60).format(TIME)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial.id == 0L) R.string.cal_new_event else R.string.cal_edit_event)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.cal_title)) }, singleLine = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.cal_all_day), modifier = Modifier.weight(1f))
                    Switch(allDay, { allDay = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.cal_start), modifier = Modifier.width(48.dp), style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(onClick = { picker = Picker.StartDate }) { Text(startDay.format(dateFmt)) }
                    if (!allDay) OutlinedButton(onClick = { picker = Picker.StartTime }) { Text(fmtMin(startMin)) }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.cal_end), modifier = Modifier.width(48.dp), style = MaterialTheme.typography.labelMedium)
                    OutlinedButton(onClick = { picker = Picker.EndDate }) { Text(endDay.format(dateFmt)) }
                    if (!allDay) OutlinedButton(onClick = { picker = Picker.EndTime }) { Text(fmtMin(endMin)) }
                }
                if (endKey < startKey) Text(stringResource(R.string.cal_end_before_start), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.cal_location)) }, singleLine = true)

                Text(stringResource(R.string.cal_repeat), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Frequency.entries.forEach { f ->
                        FilterChip(freq == f, { freq = f }, label = {
                            Text(stringResource(when (f) {
                                Frequency.NONE -> R.string.cal_rep_none
                                Frequency.DAILY -> R.string.cal_rep_daily
                                Frequency.WEEKLY -> R.string.cal_rep_weekly
                                Frequency.MONTHLY -> R.string.cal_rep_monthly
                                Frequency.YEARLY -> R.string.cal_rep_yearly
                            }))
                        })
                    }
                }
                if (freq != Frequency.NONE) {
                    NumberField(stringResource(R.string.cal_interval), interval, { interval = it }, integer = true)
                    if (freq == Frequency.WEEKLY) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DayOfWeek.entries.forEach { d ->
                                val bit = 1 shl (d.value - 1)
                                FilterChip(mask and bit != 0, { mask = mask xor bit }, label = { Text(d.getDisplayName(JTextStyle.SHORT_STANDALONE, locale).take(2)) })
                            }
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(endMode == EndMode.NEVER, { endMode = EndMode.NEVER }, label = { Text(stringResource(R.string.cal_end_never)) })
                        FilterChip(endMode == EndMode.UNTIL, { endMode = EndMode.UNTIL }, label = { Text(stringResource(R.string.cal_end_until)) })
                        FilterChip(endMode == EndMode.COUNT, { endMode = EndMode.COUNT }, label = { Text(stringResource(R.string.cal_end_count)) })
                    }
                    when (endMode) {
                        EndMode.UNTIL -> OutlinedButton(onClick = { picker = Picker.Until }) { Text(until.format(dateFmt)) }
                        EndMode.COUNT -> NumberField(stringResource(R.string.cal_count), count, { count = it }, integer = true)
                        EndMode.NEVER -> Unit
                    }
                }
                Text(stringResource(R.string.cal_color), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EVENT_COLORS.forEachIndexed { i, c ->
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(c)
                                .then(if (i == color) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                                .clickable { color = i }
                        )
                    }
                }
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.cal_notes)) }, minLines = 2)
            }
        },
        confirmButton = {
            Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
                TextButton(enabled = valid, onClick = {
                    val rule = if (freq == Frequency.NONE) Recurrence.NONE else Recurrence(
                        frequency = freq, interval = intervalN ?: 1, weekdayMask = if (freq == Frequency.WEEKLY) mask else 0,
                        until = if (endMode == EndMode.UNTIL) until else null, count = if (endMode == EndMode.COUNT) countN else null
                    )
                    onSave(
                        initial.copy(
                            title = title.trim(), location = location.trim(), notes = notes.trim(), allDay = allDay,
                            startDay = startDay.toEpochDay(), startMinute = if (allDay) 0 else startMin,
                            endDay = endDay.toEpochDay(), endMinute = if (allDay) 0 else endMin,
                            frequency = rule.frequency.name, interval = rule.interval, weekdayMask = rule.weekdayMask,
                            untilDay = rule.until?.toEpochDay(), count = rule.count, color = color
                        )
                    )
                }) { Text(stringResource(R.string.save)) }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )

    when (val p = picker) {
        null -> Unit
        Picker.StartDate, Picker.EndDate, Picker.Until -> {
            val initialDate = when (p) { Picker.StartDate -> startDay; Picker.EndDate -> endDay; else -> until }
            DatePickerModal(initialDate, onDismiss = { picker = null }) { d ->
                when (p) {
                    Picker.StartDate -> {
                        val shift = d.toEpochDay() - startDay.toEpochDay()
                        startDay = d; endDay = endDay.plusDays(shift)
                    }
                    Picker.EndDate -> endDay = d
                    else -> until = d
                }
                picker = null
            }
        }
        Picker.StartTime, Picker.EndTime -> {
            val initialMin = if (p == Picker.StartTime) startMin else endMin
            TimePickerModal(initialMin, onDismiss = { picker = null }) { m ->
                if (p == Picker.StartTime) {
                    val duration = endMin - startMin
                    startMin = m
                    if (startDay == endDay) endMin = (m + duration.coerceAtLeast(0)).coerceAtMost(23 * 60 + 59)
                } else endMin = m
                picker = null
            }
        }
    }
}

@Composable
private fun DatePickerModal(initial: LocalDate, onDismiss: () -> Unit, onPick: (LocalDate) -> Unit) {
    // Material's DatePicker works in UTC milliseconds at midnight.
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onDismiss()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    ) { DatePicker(state = state) }
}

@Composable
private fun TimePickerModal(initialMinute: Int, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = initialMinute / 60, initialMinute = initialMinute % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onPick(state.hour * 60 + state.minute) }) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

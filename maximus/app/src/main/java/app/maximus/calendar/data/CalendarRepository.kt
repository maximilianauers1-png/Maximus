package app.maximus.calendar.data

import app.maximus.calendar.domain.Frequency
import app.maximus.calendar.domain.HolidayRegion
import app.maximus.calendar.domain.Recurrence
import app.maximus.calendar.domain.RecurrenceExpander
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import dagger.Lazy
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** One concrete occurrence of an event, as displayed. */
data class Occurrence(val event: CalendarEventEntity, val start: LocalDateTime, val end: LocalDateTime) {
    val startDay: LocalDate get() = start.toLocalDate()
    val endDay: LocalDate get() = end.toLocalDate()
}

class CalendarSnapshot(val events: List<CalendarEventEntity>, val exdates: Map<Long, Set<LocalDate>>) {
    fun occurrences(from: LocalDate, to: LocalDate): List<Occurrence> = events.flatMap { e ->
        val start = LocalDate.ofEpochDay(e.startDay)
        val span = (e.endDay - e.startDay).coerceAtLeast(0)
        RecurrenceExpander.occurrences(start, e.recurrence(), from, to, span, exdates[e.id].orEmpty()).map { d ->
            Occurrence(
                e,
                d.atStartOfDay().plusMinutes(e.startMinute.toLong()),
                d.plusDays(span).atStartOfDay().plusMinutes(e.endMinute.toLong())
            )
        }
    }.sortedWith(compareBy<Occurrence>({ it.start.toLocalDate() }, { !it.event.allDay }, { it.start }))
}

fun CalendarEventEntity.recurrence(): Recurrence = Recurrence(
    frequency = runCatching { Frequency.valueOf(frequency) }.getOrDefault(Frequency.NONE),
    interval = interval.coerceAtLeast(1),
    weekdayMask = weekdayMask,
    until = untilDay?.let(LocalDate::ofEpochDay),
    count = count
)

@Singleton
class CalendarRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private fun dao() = database.get().calendarDao()

    val snapshot: Flow<CalendarSnapshot> = flow {
        emitAll(
            combine(dao().observeEvents(), dao().observeExdates()) { ev, ex ->
                CalendarSnapshot(ev, ex.groupBy({ it.eventId }, { LocalDate.ofEpochDay(it.day) }).mapValues { it.value.toSet() })
            }
        )
    }.flowOn(io)

    val holidayRegion: Flow<HolidayRegion> = flow {
        emitAll(database.get().appMetaDao().observe(KEY_REGION).map { v ->
            v?.let { runCatching { HolidayRegion.valueOf(it) }.getOrNull() } ?: HolidayRegion.NONE
        })
    }.flowOn(io)

    suspend fun setHolidayRegion(r: HolidayRegion) = withContext(io) {
        database.get().appMetaDao().upsert(AppMetaEntity(KEY_REGION, r.name))
    }

    suspend fun save(e: CalendarEventEntity): Long = withContext(io) {
        if (e.id == 0L) dao().insert(e.copy(createdAt = System.currentTimeMillis())) else { dao().update(e); e.id }
    }

    suspend fun delete(id: Long) = withContext(io) { dao().delete(id) }

    suspend fun excludeOccurrence(eventId: Long, day: LocalDate) = withContext(io) {
        dao().insertExdate(CalendarExdateEntity(eventId, day.toEpochDay()))
    }

    companion object { const val KEY_REGION = "calendar.holidays" }
}

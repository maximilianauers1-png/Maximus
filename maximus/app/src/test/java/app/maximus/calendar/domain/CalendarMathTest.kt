package app.maximus.calendar.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarMathTest {
    private fun d(s: String) = LocalDate.parse(s)

    @Test fun easterKnownDates() {
        val known = mapOf(1961 to "1961-04-02", 2000 to "2000-04-23", 2008 to "2008-03-23", 2011 to "2011-04-24",
            2024 to "2024-03-31", 2025 to "2025-04-20", 2026 to "2026-04-05", 2027 to "2027-03-28", 2038 to "2038-04-25", 2285 to "2285-03-22")
        for ((y, date) in known) assertEquals(d(date), Easter.sunday(y))
    }

    /** Easter always falls on a Sunday between 22 March and 25 April (period 5 700 000 years; sample 1583–4099). */
    @Test fun easterRangeInvariant() {
        for (y in 1583..4099) {
            val e = Easter.sunday(y)
            assertEquals(DayOfWeek.SUNDAY, e.dayOfWeek)
            assertTrue(!e.isBefore(LocalDate.of(y, 3, 22)) && !e.isAfter(LocalDate.of(y, 4, 25)))
        }
    }

    @Test fun swedishMovableHolidays2026() {
        val h = Holidays.of(HolidayRegion.SE, 2026).associate { it.nameSv to it.date }
        assertEquals(d("2026-06-19"), h["Midsommarafton"])
        assertEquals(d("2026-06-20"), h["Midsommardagen"])
        assertEquals(d("2026-10-31"), h["Alla helgons dag"])
        assertEquals(d("2026-05-14"), h["Kristi himmelsfärdsdag"])
        assertEquals(d("2026-05-24"), h["Pingstdagen"])
        val de = Holidays.of(HolidayRegion.DE, 2026).associate { it.nameDe to it.date }
        assertEquals(d("2026-04-03"), de["Karfreitag"])
        assertEquals(d("2026-05-25"), de["Pfingstmontag"])
        assertEquals(9, de.size)
    }

    @Test fun monthlyOn31SkipsShortMonths() {
        val occ = RecurrenceExpander.occurrences(d("2026-01-31"), Recurrence(Frequency.MONTHLY), d("2026-01-01"), d("2026-12-31"))
        assertEquals(listOf("2026-01-31", "2026-03-31", "2026-05-31", "2026-07-31", "2026-08-31", "2026-10-31", "2026-12-31").map(::d), occ)
    }

    @Test fun yearlyLeapDay() {
        val occ = RecurrenceExpander.occurrences(d("2024-02-29"), Recurrence(Frequency.YEARLY), d("2024-01-01"), d("2033-01-01"))
        assertEquals(listOf("2024-02-29", "2028-02-29", "2032-02-29").map(::d), occ)
    }

    @Test fun weeklyByDayWithIntervalAndCount() {
        val rule = Recurrence(Frequency.WEEKLY, interval = 2, weekdayMask = Recurrence.maskOf(listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY)), count = 5)
        // DTSTART Wednesday 2026-09-30: Monday of that week (28th) precedes DTSTART and is not produced.
        val occ = RecurrenceExpander.occurrences(d("2026-09-30"), rule, d("2026-01-01"), d("2027-12-31"))
        assertEquals(listOf("2026-10-01", "2026-10-12", "2026-10-15", "2026-10-26", "2026-10-29").map(::d), occ)
    }

    @Test fun dailyWindowJumpEqualsFullScan() {
        val start = d("2020-01-01")
        val rule = Recurrence(Frequency.DAILY, interval = 3)
        val window = RecurrenceExpander.occurrences(start, rule, d("2026-09-01"), d("2026-09-30"))
        val full = RecurrenceExpander.occurrences(start, rule, start, d("2026-09-30")).filter { !it.isBefore(d("2026-09-01")) }
        assertEquals(full, window)
        assertEquals(10, window.size)
    }

    @Test fun untilExdateAndSpan() {
        val rule = Recurrence(Frequency.DAILY, until = d("2026-10-05"))
        val occ = RecurrenceExpander.occurrences(d("2026-10-01"), rule, d("2026-10-03"), d("2026-10-31"), spanDays = 1, excluded = setOf(d("2026-10-04")))
        // Span 1 day: the occurrence on 10-02 overlaps 10-03.
        assertEquals(listOf("2026-10-02", "2026-10-03", "2026-10-05").map(::d), occ)
    }

    @Test fun monthGridStartsMonday() {
        val cells = MonthGrid.cells(YearMonth.of(2026, 9))
        assertEquals(42, cells.size)
        assertEquals(d("2026-08-31"), cells.first())
        assertEquals(DayOfWeek.MONDAY, cells.first().dayOfWeek)
    }
}

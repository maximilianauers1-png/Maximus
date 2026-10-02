package app.maximus.calendar.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class Frequency { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

/**
 * Subset of RFC 5545 RRULE: FREQ, INTERVAL, BYDAY (weekly only, WKST = MO), UNTIL (inclusive) or COUNT.
 * [weekdayMask]: bit (d−1) set for ISO day d (Monday = bit 0). Empty mask = weekday of DTSTART.
 * As in RFC 5545, a monthly rule on day 31 skips shorter months and a yearly rule on 29 February
 * occurs in leap years only; COUNT counts occurrences actually produced.
 */
data class Recurrence(
    val frequency: Frequency = Frequency.NONE,
    val interval: Int = 1,
    val weekdayMask: Int = 0,
    val until: LocalDate? = null,
    val count: Int? = null
) {
    init {
        require(interval >= 1) { "interval must be ≥ 1" }
        require(count == null || count >= 1) { "count must be ≥ 1" }
        require(weekdayMask in 0..0x7F) { "weekday mask out of range" }
    }

    companion object {
        val NONE = Recurrence()
        fun maskOf(days: Collection<DayOfWeek>): Int = days.fold(0) { m, d -> m or (1 shl (d.value - 1)) }
        fun daysOf(mask: Int): List<DayOfWeek> = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }
    }
}

object RecurrenceExpander {
    private const val SAFETY_LIMIT = 200_000

    /**
     * Start dates of all occurrences s with from − spanDays ≤ s ≤ to, i.e. every occurrence of an event
     * lasting [spanDays] additional days that overlaps [from, to]. [excluded] removes single dates (EXDATE);
     * excluded dates still consume COUNT, as in RFC 5545.
     */
    fun occurrences(
        start: LocalDate,
        rule: Recurrence,
        from: LocalDate,
        to: LocalDate,
        spanDays: Long = 0,
        excluded: Set<LocalDate> = emptySet()
    ): List<LocalDate> {
        if (to.isBefore(from)) return emptyList()
        val lower = from.minusDays(spanDays)
        val end = listOfNotNull(to, rule.until).minOrNull()!!
        val out = ArrayList<LocalDate>()
        fun accept(d: LocalDate) { if (!d.isBefore(lower) && d !in excluded) out += d }

        when (rule.frequency) {
            Frequency.NONE -> if (!start.isAfter(end)) accept(start)
            Frequency.DAILY -> {
                val step = rule.interval.toLong()
                var k = if (rule.count == null) ceilDiv(ChronoUnit.DAYS.between(start, lower), step).coerceAtLeast(0) else 0L
                while (true) {
                    if (rule.count != null && k >= rule.count) break
                    val d = start.plusDays(k * step)
                    if (d.isAfter(end)) break
                    accept(d); k++
                }
            }
            Frequency.WEEKLY -> {
                val mask = if (rule.weekdayMask == 0) 1 shl (start.dayOfWeek.value - 1) else rule.weekdayMask
                val days = Recurrence.daysOf(mask)
                val week0 = start.minusDays((start.dayOfWeek.value - 1).toLong())
                val step = rule.interval.toLong()
                var w = if (rule.count == null) ceilDiv(ChronoUnit.WEEKS.between(week0, lower.minusDays((lower.dayOfWeek.value - 1).toLong())), step).coerceAtLeast(0) else 0L
                var produced = 0
                var guard = 0
                outer@ while (guard++ < SAFETY_LIMIT) {
                    val monday = week0.plusWeeks(w * step)
                    if (monday.isAfter(end)) break
                    for (dow in days) {
                        val d = monday.plusDays((dow.value - 1).toLong())
                        if (d.isBefore(start)) continue
                        if (d.isAfter(end)) break@outer
                        if (rule.count != null && produced >= rule.count) break@outer
                        produced++
                        accept(d)
                    }
                    w++
                }
            }
            Frequency.MONTHLY, Frequency.YEARLY -> {
                val months = if (rule.frequency == Frequency.MONTHLY) rule.interval.toLong() else 12L * rule.interval
                val ym0 = YearMonth.from(start)
                var k = if (rule.count == null) {
                    ceilDiv(ChronoUnit.MONTHS.between(ym0, YearMonth.from(lower)), months).coerceAtLeast(0) - 1
                } else 0L
                if (k < 0) k = 0
                var produced = 0
                var guard = 0
                while (guard++ < SAFETY_LIMIT) {
                    val ym = ym0.plusMonths(k * months)
                    if (ym.atDay(1).isAfter(end)) break
                    if (start.dayOfMonth <= ym.lengthOfMonth()) {
                        val d = ym.atDay(start.dayOfMonth)
                        if (d.isAfter(end)) break
                        if (rule.count != null && produced >= rule.count) break
                        produced++
                        accept(d)
                    }
                    k++
                }
            }
        }
        return out
    }

    private fun ceilDiv(a: Long, b: Long): Long = -Math.floorDiv(-a, b)
}

/** Six-week month grid, Monday first (ISO 8601). */
object MonthGrid {
    fun cells(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val start = first.minusDays((first.dayOfWeek.value - 1).toLong())
        return List(42) { start.plusDays(it.toLong()) }
    }
}

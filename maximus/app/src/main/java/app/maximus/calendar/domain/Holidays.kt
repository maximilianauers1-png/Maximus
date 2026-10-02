package app.maximus.calendar.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class HolidayRegion { NONE, DE, SE }

data class Holiday(val date: LocalDate, val nameDe: String, val nameEn: String, val nameSv: String)

object Easter {
    /**
     * Gregorian Easter Sunday by the anonymous (Meeus/Jones/Butcher) algorithm, exact for all
     * years ≥ 1583. a = Y mod 19 is the Metonic position; h is the epact-derived offset of the
     * paschal full moon; l shifts to the following Sunday; m corrects the two exceptional epacts.
     */
    fun sunday(year: Int): LocalDate {
        require(year >= 1583) { "Gregorian computus only" }
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }
}

object Holidays {
    /** Nationwide public holidays (Germany: federal only, no state holidays; Sweden: helgdagar + customary eves). */
    fun of(region: HolidayRegion, year: Int): List<Holiday> {
        if (region == HolidayRegion.NONE) return emptyList()
        val e = Easter.sunday(year)
        val list = when (region) {
            HolidayRegion.DE -> listOf(
                Holiday(LocalDate.of(year, 1, 1), "Neujahr", "New Year's Day", "Nyårsdagen"),
                Holiday(e.minusDays(2), "Karfreitag", "Good Friday", "Långfredagen"),
                Holiday(e.plusDays(1), "Ostermontag", "Easter Monday", "Annandag påsk"),
                Holiday(LocalDate.of(year, 5, 1), "Tag der Arbeit", "Labour Day", "Första maj"),
                Holiday(e.plusDays(39), "Christi Himmelfahrt", "Ascension Day", "Kristi himmelsfärdsdag"),
                Holiday(e.plusDays(50), "Pfingstmontag", "Whit Monday", "Annandag pingst"),
                Holiday(LocalDate.of(year, 10, 3), "Tag der Deutschen Einheit", "German Unity Day", "Tysklands enhetsdag"),
                Holiday(LocalDate.of(year, 12, 25), "1. Weihnachtstag", "Christmas Day", "Juldagen"),
                Holiday(LocalDate.of(year, 12, 26), "2. Weihnachtstag", "St Stephen's Day", "Annandag jul")
            )
            HolidayRegion.SE -> {
                val midsummerEve = LocalDate.of(year, 6, 19).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                val allSaints = LocalDate.of(year, 10, 31).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                listOf(
                    Holiday(LocalDate.of(year, 1, 1), "Neujahr", "New Year's Day", "Nyårsdagen"),
                    Holiday(LocalDate.of(year, 1, 6), "Heilige Drei Könige", "Epiphany", "Trettondedag jul"),
                    Holiday(e.minusDays(2), "Karfreitag", "Good Friday", "Långfredagen"),
                    Holiday(e, "Ostersonntag", "Easter Sunday", "Påskdagen"),
                    Holiday(e.plusDays(1), "Ostermontag", "Easter Monday", "Annandag påsk"),
                    Holiday(LocalDate.of(year, 5, 1), "Erster Mai", "May Day", "Första maj"),
                    Holiday(e.plusDays(39), "Christi Himmelfahrt", "Ascension Day", "Kristi himmelsfärdsdag"),
                    Holiday(e.plusDays(49), "Pfingstsonntag", "Whitsunday", "Pingstdagen"),
                    Holiday(LocalDate.of(year, 6, 6), "Nationalfeiertag", "National Day", "Sveriges nationaldag"),
                    Holiday(midsummerEve, "Mittsommerabend", "Midsummer Eve", "Midsommarafton"),
                    Holiday(midsummerEve.plusDays(1), "Mittsommertag", "Midsummer Day", "Midsommardagen"),
                    Holiday(allSaints, "Allerheiligen", "All Saints' Day", "Alla helgons dag"),
                    Holiday(LocalDate.of(year, 12, 24), "Heiligabend", "Christmas Eve", "Julafton"),
                    Holiday(LocalDate.of(year, 12, 25), "1. Weihnachtstag", "Christmas Day", "Juldagen"),
                    Holiday(LocalDate.of(year, 12, 26), "2. Weihnachtstag", "Boxing Day", "Annandag jul"),
                    Holiday(LocalDate.of(year, 12, 31), "Silvester", "New Year's Eve", "Nyårsafton")
                )
            }
            HolidayRegion.NONE -> emptyList()
        }
        return list.sortedBy { it.date }
    }

    fun inRange(region: HolidayRegion, from: LocalDate, to: LocalDate): Map<LocalDate, List<Holiday>> =
        (from.year..to.year).flatMap { of(region, it) }
            .filter { !it.date.isBefore(from) && !it.date.isAfter(to) }
            .groupBy { it.date }
}

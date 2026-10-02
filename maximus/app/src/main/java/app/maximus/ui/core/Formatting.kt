package app.maximus.ui.core

import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val SUPERSCRIPT = "⁰¹²³⁴⁵⁶⁷⁸⁹"

private fun superscript(n: Int): String {
    val s = n.toString().map { c -> if (c == '-') '⁻' else SUPERSCRIPT[c - '0'] }
    return String(s.toCharArray())
}

/** m × 10ⁿ with one decimal of mantissa, e.g. 3,5 × 10²¹ (locale-aware decimal separator). */
fun scientific(x: Double, locale: Locale = Locale.getDefault()): String {
    if (x == 0.0 || x.isNaN() || x.isInfinite()) return String.format(locale, "%.1f", x)
    val e = floor(log10(x)).toInt()
    var m = x / 10.0.pow(e)
    var exp = e
    if (String.format(Locale.ROOT, "%.1f", m) == "10.0") { m = 1.0; exp += 1 }
    return if (exp in 0..5) String.format(locale, "%,.0f", x) else String.format(locale, "%.1f × 10%s", m, superscript(exp))
}

enum class DurationUnit { SECONDS, MINUTES, HOURS, DAYS, YEARS }

/** Picks the largest sensible unit; years use the Julian year 365.25 d. */
fun splitDuration(seconds: Double): Pair<Double, DurationUnit> = when {
    seconds < 60 -> seconds to DurationUnit.SECONDS
    seconds < 3600 -> seconds / 60 to DurationUnit.MINUTES
    seconds < 86_400 -> seconds / 3600 to DurationUnit.HOURS
    seconds < 31_557_600 -> seconds / 86_400 to DurationUnit.DAYS
    else -> seconds / 31_557_600 to DurationUnit.YEARS
}

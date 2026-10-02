package app.maximus.strongman.domain

import kotlin.math.roundToLong

data class PlateStock(val kg: Double, val pairs: Int)

sealed interface PlateResult {
    /** [perSide] in descending order; [usedGreedy] is false when the exact DP fallback was needed. */
    data class Loadable(val perSide: List<Double>, val totalKg: Double, val usedGreedy: Boolean) : PlateResult

    data class NotLoadable(val nearestBelowKg: Double?, val nearestAboveKg: Double?) : PlateResult
}

/**
 * Greedy first (optimal for canonical stocks), then an exact bounded-subset-sum DP as the
 * feasibility check: greedy can fail with limited pairs, e.g. stock {15, 10, 10} and a
 * per-side target of 20 (greedy takes 15 and is stuck with 5; DP finds 10 + 10).
 * All arithmetic in integer grams to avoid floating-point residues.
 */
object PlateCalculator {

    private fun grams(kg: Double): Long = (kg * 1000.0).roundToLong()

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    fun solve(targetKg: Double, barKg: Double, stock: List<PlateStock>): PlateResult {
        val target = grams(targetKg)
        val bar = grams(barKg)
        val items = stock
            .filter { it.kg > 0.0 && it.pairs > 0 }
            .sortedByDescending { it.kg }
            .flatMap { s -> List(s.pairs) { grams(s.kg) } }

        if (target < bar) return PlateResult.NotLoadable(null, barKg)
        if (target == bar) return PlateResult.Loadable(emptyList(), barKg, usedGreedy = true)

        val diff = target - bar
        if (diff % 2L == 0L) {
            greedy(diff / 2L, items)?.let { return PlateResult.Loadable(it.map { g -> g / 1000.0 }, targetKg, true) }
        }
        if (items.isEmpty()) return PlateResult.NotLoadable(null, null)

        val unit = items.fold(0L) { acc, g -> gcd(acc, g) }
        val maxSum = items.sum()
        val n = (maxSum / unit).toInt()
        val from = IntArray(n + 1) { -1 }
        val reachable = BooleanArray(n + 1).also { it[0] = true }
        items.forEachIndexed { idx, g ->
            val w = (g / unit).toInt()
            for (s in n downTo w) {
                if (!reachable[s] && reachable[s - w]) {
                    reachable[s] = true
                    from[s] = idx
                }
            }
        }

        if (diff % 2L == 0L && (diff / 2L) % unit == 0L) {
            val s = ((diff / 2L) / unit).toInt()
            if (s <= n && reachable[s]) {
                val plates = mutableListOf<Double>()
                var cur = s
                while (cur > 0) {
                    val i = from[cur]
                    plates += items[i] / 1000.0
                    cur -= (items[i] / unit).toInt()
                }
                return PlateResult.Loadable(plates.sortedDescending(), targetKg, usedGreedy = false)
            }
        }

        val perSideExact = diff / 2.0
        var below: Double? = null
        var above: Double? = null
        for (s in 0..n) {
            if (!reachable[s]) continue
            val perSide = (s.toLong() * unit).toDouble()
            val total = (bar + 2.0 * perSide) / 1000.0
            if (perSide < perSideExact) below = total
            if (perSide > perSideExact && above == null) above = total
        }
        return PlateResult.NotLoadable(below, above)
    }

    private fun greedy(perSide: Long, items: List<Long>): List<Long>? {
        var rem = perSide
        val used = mutableListOf<Long>()
        for (g in items) {
            if (g <= rem) {
                used += g
                rem -= g
            }
            if (rem == 0L) return used
        }
        return null
    }
}

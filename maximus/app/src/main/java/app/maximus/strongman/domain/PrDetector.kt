package app.maximus.strongman.domain

data class PrCandidate(val id: Long, val epochDay: Long, val weightKg: Double, val reps: Int, val e1rm: Double?)

data class PrFlags(val e1rmPr: Boolean, val repPr: Boolean)

/**
 * Chronological sweep (epochDay, then id).
 * e1RM PR: strictly above every earlier e1RM of the exercise.
 * Rep PR: the set is Pareto-undominated, i.e. no earlier set had weight >= w AND reps >= r.
 * The Pareto frontier is maintained incrementally.
 */
object PrDetector {
    private const val EPS = 1e-9

    fun evaluate(sets: List<PrCandidate>): Map<Long, PrFlags> {
        val out = HashMap<Long, PrFlags>(sets.size * 2)
        var best = Double.NEGATIVE_INFINITY
        val frontier = mutableListOf<Pair<Double, Int>>()
        for (s in sets.sortedWith(compareBy<PrCandidate>({ it.epochDay }, { it.id }))) {
            val e = s.e1rm
            val e1rmPr = e != null && e > best + EPS
            if (e != null && e > best) best = e
            var repPr = false
            if (s.weightKg > 0.0 && s.reps > 0) {
                val dominated = frontier.any { (w, r) -> w >= s.weightKg - EPS && r >= s.reps }
                if (!dominated) {
                    repPr = true
                    frontier.removeAll { (w, r) -> w <= s.weightKg + EPS && r <= s.reps }
                    frontier += s.weightKg to s.reps
                }
            }
            out[s.id] = PrFlags(e1rmPr, repPr)
        }
        return out
    }
}

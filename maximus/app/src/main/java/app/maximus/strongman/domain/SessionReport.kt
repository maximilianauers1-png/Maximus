package app.maximus.strongman.domain

import kotlin.math.max

/** Per-exercise summary of one training day, compared with everything logged before it. */
data class ExerciseReport(
    val exerciseId: Long,
    val sets: Int,
    val reps: Int,
    val tonnage: Double,
    val bestE1rm: Double?,
    val previousBestE1rm: Double?,
    val prs: Int,
    val repPrs: Int,
    /** Mean load relative to the previous best e1RM (relative intensity), 0..1+. */
    val relativeIntensity: Double?,
    /** INOL = Σ reps / (100 − %1RM) (Hristov): ≈ 0.4–1 productive, > 1 hard, > 2 very taxing. */
    val inol: Double?,
    /** Tonnage relative to the mean of the previous five days with this exercise (1.0 = as usual). */
    val tonnageVsUsual: Double?,
    /** (day, best e1RM) of the last days with this exercise including today, for the mini chart. */
    val e1rmHistory: List<Pair<Long, Double>>
) {
    val deltaPercent: Double? get() = if (bestE1rm != null && previousBestE1rm != null && previousBestE1rm > 0) 100 * (bestE1rm / previousBestE1rm - 1) else null
}

data class SessionReport(
    val epochDay: Long,
    val exercises: List<ExerciseReport>,
    val tonnage: Double,
    val sets: Int,
    val reps: Int,
    val e1rmPrs: Int,
    val repPrs: Int,
    val xp: Long,
    /** (day, tonnage) of the last training days up to and including this one. */
    val tonnageHistory: List<Pair<Long, Double>>,
    val verdict: String
)

private fun AnalyticsSet.perf(): Double? = Performance.e1rm(weightKg, reps, rpe, e1rm)

object SessionReports {
    fun build(day: Long, sessionSets: List<AnalyticsSet>, allSets: List<AnalyticsSet>, historyLength: Int = 10): SessionReport {
        val before = allSets.filter { it.epochDay < day }
        val exercises = sessionSets.groupBy { it.exerciseId }.map { (exId, list) ->
            val prior = before.filter { it.exerciseId == exId }
            val prevBest = prior.mapNotNull { it.perf() }.maxOrNull()
            val best = list.mapNotNull { it.perf() }.maxOrNull()
            val ref = prevBest ?: best
            val rel = ref?.let { r -> list.filter { it.weightKg > 0 }.map { it.weightKg / r }.takeIf { it.isNotEmpty() }?.average() }
            val inol = ref?.let { r ->
                list.filter { it.weightKg > 0 && it.reps > 0 }.sumOf { s -> s.reps / max(1.0, 100.0 - 100.0 * s.weightKg / r) }
            }
            val priorDays = prior.groupBy { it.epochDay }.toSortedMap()
            val usual = priorDays.values.toList().takeLast(5).map { d -> d.sumOf { it.weightKg * it.reps } }
            val ton = list.sumOf { it.weightKg * it.reps }
            val history = (priorDays.mapNotNull { (d, s) -> s.mapNotNull { it.perf() }.maxOrNull()?.let { d to it } } +
                listOfNotNull(best?.let { day to it })).takeLast(historyLength)
            ExerciseReport(
                exId, list.size, list.sumOf { it.reps }, ton, best, prevBest,
                list.count { it.isE1rmPr }, list.count { it.isRepPr }, rel, inol,
                usual.takeIf { it.isNotEmpty() && it.average() > 0 }?.let { ton / it.average() }, history
            )
        }.sortedByDescending { it.tonnage }
        val dayTonnage = (allSets.filter { it.epochDay < day } + sessionSets).groupBy { it.epochDay }.toSortedMap()
            .map { (d, s) -> d to s.sumOf { it.weightKg * it.reps } }.takeLast(historyLength)
        val ton = sessionSets.sumOf { it.weightKg * it.reps }
        val e1rmPrs = sessionSets.count { it.isE1rmPr }
        val repPrs = sessionSets.count { it.isRepPr }
        return SessionReport(
            day, exercises, ton, sessionSets.size, sessionSets.sumOf { it.reps }, e1rmPrs, repPrs,
            Arena.xpOf(sessionSets), dayTonnage, verdict(exercises, e1rmPrs, repPrs)
        )
    }

    private fun verdict(ex: List<ExerciseReport>, prs: Int, repPrs: Int): String {
        val inol = ex.mapNotNull { it.inol }.maxOrNull()
        val vol = ex.mapNotNull { it.tonnageVsUsual }.takeIf { it.isNotEmpty() }?.average()
        return when {
            prs > 0 -> "Rekordtag! $prs neue e1RM-Bestleistung${if (prs > 1) "en" else ""}. Gönn dir Schlaf und Protein – Kraft wird in der Erholung gebaut."
            repPrs > 0 -> "Starke Einheit: $repPrs Wiederholungsrekord${if (repPrs > 1) "e" else ""}. Die Basis für den nächsten Maximalrekord wächst."
            inol != null && inol > 2.0 -> "Sehr harte Einheit (INOL ${"%.1f".format(inol)}). Plane 48–72 h bis zur nächsten schweren Belastung dieses Musters ein."
            vol != null && vol < 0.7 -> "Leichter Tag – ideal als Technik- oder Erholungseinheit."
            vol != null && vol > 1.3 -> "Viel mehr Volumen als üblich. Beobachte Erholung und Bewegungsqualität in den nächsten Tagen."
            else -> "Solide Arbeit. Beständigkeit schlägt Heldentaten – genau so entsteht Bärenkraft."
        }
    }
}

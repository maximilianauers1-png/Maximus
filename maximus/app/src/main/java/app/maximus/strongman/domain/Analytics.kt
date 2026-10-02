package app.maximus.strongman.domain

import java.util.SortedMap
import java.util.TreeMap

/** 1970-01-01 (epochDay 0) was a Thursday. Weeks start on Monday. */
object WeekMath {
    fun mondayWeekIndex(epochDay: Long): Long = Math.floorDiv(epochDay + 3, 7L)

    /** 0 = Monday ... 6 = Sunday. */
    fun dayOfWeek(epochDay: Long): Int = Math.floorMod(epochDay + 3, 7L).toInt()

    fun mondayOfWeek(weekIndex: Long): Long = weekIndex * 7L - 3L
}

data class AnalyticsSet(
    val id: Long,
    val epochDay: Long,
    val exerciseId: Long,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val e1rm: Double?,
    val isE1rmPr: Boolean,
    val isRepPr: Boolean
)

object Analytics {
    fun volumeLoad(sets: List<AnalyticsSet>): Double = sets.sumOf { it.weightKg * it.reps }

    fun weeklyVolume(sets: List<AnalyticsSet>): SortedMap<Long, Double> {
        val map = TreeMap<Long, Double>()
        for (s in sets) map.merge(WeekMath.mondayWeekIndex(s.epochDay), s.weightKg * s.reps, Double::plus)
        return map
    }

    /** Daily maximum e1RM of one exercise, ordered by day. */
    fun dailyBestE1rm(sets: List<AnalyticsSet>, exerciseId: Long): List<Pair<Long, Double>> {
        val map = TreeMap<Long, Double>()
        for (s in sets) {
            val e = s.e1rm ?: continue
            if (s.exerciseId == exerciseId) map.merge(s.epochDay, e, ::maxOf)
        }
        return map.map { it.key to it.value }
    }

    /**
     * Relative intensity of a set = w / (running best e1RM of its exercise, including the set itself);
     * averaged per (Monday-week, weekday) cell.
     */
    fun intensityHeatmap(sets: List<AnalyticsSet>): Map<Pair<Long, Int>, Double> {
        val best = HashMap<Long, Double>()
        val sum = HashMap<Pair<Long, Int>, Double>()
        val count = HashMap<Pair<Long, Int>, Int>()
        for (s in sets.sortedWith(compareBy<AnalyticsSet>({ it.epochDay }, { it.id }))) {
            s.e1rm?.let { e -> best.merge(s.exerciseId, e, ::maxOf) }
            val ref = best[s.exerciseId] ?: continue
            if (s.weightKg <= 0.0 || ref <= 0.0) continue
            val key = WeekMath.mondayWeekIndex(s.epochDay) to WeekMath.dayOfWeek(s.epochDay)
            sum.merge(key, s.weightKg / ref, Double::plus)
            count.merge(key, 1, Int::plus)
        }
        return sum.mapValues { (k, v) -> v / count.getValue(k) }
    }

    fun rpeHistogram(sets: List<AnalyticsSet>): SortedMap<Double, Int> {
        val map = TreeMap<Double, Int>()
        for (s in sets) s.rpe?.let { map.merge(it, 1, Int::plus) }
        return map
    }

    fun bestE1rmByExercise(sets: List<AnalyticsSet>): Map<Long, Double> {
        val map = HashMap<Long, Double>()
        for (s in sets) s.e1rm?.let { map.merge(s.exerciseId, it, ::maxOf) }
        return map
    }
}

package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PrAndAnalyticsTest {

    @Test
    fun paretoRepPrAndE1rmPr() {
        val sets = listOf(
            PrCandidate(1, 10, 100.0, 5, 116.0),
            PrCandidate(2, 11, 100.0, 4, 112.0),   // dominated by (100,5)
            PrCandidate(3, 12, 90.0, 8, 114.0),    // undominated: more reps at lower weight
            PrCandidate(4, 13, 105.0, 3, 115.0),   // undominated: heavier
            PrCandidate(5, 14, 110.0, 3, 121.0)    // dominates (105,3), new e1RM best
        )
        val f = PrDetector.evaluate(sets)
        assertEquals(PrFlags(true, true), f[1])
        assertEquals(PrFlags(false, false), f[2])
        assertEquals(PrFlags(false, true), f[3])
        assertEquals(PrFlags(false, true), f[4])
        assertEquals(PrFlags(true, true), f[5])
    }

    @Test
    fun weekMath() {
        assertEquals(3, WeekMath.dayOfWeek(0))            // 1970-01-01 Thursday
        assertEquals(0, WeekMath.dayOfWeek(-3))           // 1969-12-29 Monday
        assertEquals(0L, WeekMath.mondayWeekIndex(-3))
        assertEquals(1L, WeekMath.mondayWeekIndex(4))     // 1970-01-05 Monday
        assertEquals(-3L, WeekMath.mondayOfWeek(0))
    }

    @Test
    fun volumeAndHeatmap() {
        val s = listOf(
            AnalyticsSet(1, 4, 1, 100.0, 5, 8.0, 125.0, true, true),
            AnalyticsSet(2, 4, 1, 110.0, 3, 9.0, 125.0, false, true)
        )
        assertEquals(830.0, Analytics.volumeLoad(s), 1e-9)
        assertEquals(830.0, Analytics.weeklyVolume(s)[1L]!!, 1e-9)
        assertEquals((100.0 / 125.0 + 110.0 / 125.0) / 2.0, Analytics.intensityHeatmap(s)[1L to 0]!!, 1e-12)
        assertEquals(2, Analytics.rpeHistogram(s).size)
    }
}

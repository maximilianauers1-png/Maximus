package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test vectors T1-T4 of the build brief, section 5. */
class BriefTestVectorsTest {

    @Test
    fun t1_oneRepMaxFormulas() {
        assertEquals(116.67, OneRepMax.epley(100.0, 5), 0.005)
        assertEquals(112.50, OneRepMax.brzycki(100.0, 5), 0.005)
        assertEquals(117.46, OneRepMax.lombardi(100.0, 5), 0.005)
    }

    @Test
    fun t2_e1rmFromRpe() {
        val r = FTable().e1rm(100.0, 5, 8.0)
        assertTrue(r is OneRmResult.Estimate)
        assertEquals(123.33, (r as OneRmResult.Estimate).kg, 0.005)
    }

    @Test
    fun t3_fractionTable() {
        assertEquals(0.8108, FTable().fraction(5, 8.0), 0.00005)
        assertEquals(0.8571, FTable().fraction(5, 10.0), 0.00005)
    }

    @Test
    fun t4_dotsMale80() {
        assertEquals(0.6895, Dots.coefficient(Sex.MALE, 80.0)!!, 0.00005)
        assertEquals(413.7, Dots.score(600.0, 80.0, Sex.MALE)!!, 0.05)
    }
}

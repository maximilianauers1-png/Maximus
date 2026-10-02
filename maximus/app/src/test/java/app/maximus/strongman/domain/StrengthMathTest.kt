package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StrengthMathTest {

    @Test
    fun defaultEstimatorBranches() {
        val mean = OneRepMax.estimate(100.0, 5) as OneRmResult.Estimate
        assertEquals((116.6666667 + 112.5) / 2, mean.kg, 1e-6)
        assertEquals(OneRmMethod.MEAN_EPLEY_BRZYCKI, mean.method)
        val epley = OneRepMax.estimate(100.0, 12) as OneRmResult.Estimate
        assertEquals(140.0, epley.kg, 1e-9)
        assertEquals(OneRmMethod.EPLEY, epley.method)
        assertEquals(OneRmResult.Refused(RefusalReason.REPS_ABOVE_LIMIT), OneRepMax.estimate(100.0, 16))
        assertEquals(OneRmResult.Refused(RefusalReason.NON_POSITIVE_LOAD), OneRepMax.estimate(0.0, 5))
    }

    @Test
    fun prescriptionRoundsHalfUp() {
        // 200 * 30/37 = 162.162 -> 162.5 ; exact half 101.25 -> 102.5 with 2.5 increments
        assertEquals(162.5, FTable().prescribe(200.0, 5, 8.0, 2.5), 1e-9)
        assertEquals(102.5, roundToIncrement(101.25, 2.5), 1e-9)
    }

    @Test
    fun overridesAreUsedByBothDirections() {
        val t = FTable(mapOf(FKey(5, 80) to 0.80))
        assertTrue(t.isOverridden(5, 8.0))
        assertEquals(125.0, (t.e1rm(100.0, 5, 8.0) as OneRmResult.Estimate).kg, 1e-9)
        assertEquals(100.0, t.prescribe(125.0, 5, 8.0, 2.5), 1e-9)
        assertFalse(t.isOverridden(5, 9.0))
    }

    @Test
    fun rpeValidation() {
        assertTrue(Rpe.isValid(7.5))
        assertFalse(Rpe.isValid(7.3))
        assertFalse(Rpe.isValid(4.5))
        assertEquals(11, Rpe.values.size)
    }

    @Test
    fun dotsFemaleIsFinite() {
        val c = Dots.coefficient(Sex.FEMALE, 60.0)!!
        assertTrue(c > 0.8 && c < 1.3)
        assertNull(Dots.coefficient(Sex.MALE, 0.0))
    }

    @Test
    fun prilepinZones() {
        assertEquals(24, Prilepin.zoneFor(60.0)!!.optimalTotal)
        assertEquals(24, Prilepin.zoneFor(67.5)!!.optimalTotal)
        assertEquals(15, Prilepin.zoneFor(80.0)!!.optimalTotal)
        assertEquals(7, Prilepin.zoneFor(95.0)!!.optimalTotal)
        assertNull(Prilepin.zoneFor(50.0))
        assertEquals(PrilepinVerdict.ABOVE_RANGE, Prilepin.assess(85.0, 25)!!.verdict)
    }

    @Test
    fun plateCalculatorGreedyAndDpFallback() {
        val std = listOf(PlateStock(25.0, 4), PlateStock(20.0, 1), PlateStock(10.0, 1), PlateStock(5.0, 1),
            PlateStock(2.5, 1), PlateStock(1.25, 1))
        val r = PlateCalculator.solve(182.5, 20.0, std) as PlateResult.Loadable
        assertEquals(listOf(25.0, 25.0, 25.0, 5.0, 1.25), r.perSide)
        assertTrue(r.usedGreedy)

        val tricky = listOf(PlateStock(15.0, 1), PlateStock(10.0, 2))
        val t = PlateCalculator.solve(60.0, 20.0, tricky) as PlateResult.Loadable
        assertEquals(listOf(10.0, 10.0), t.perSide)
        assertFalse(t.usedGreedy)

        val n = PlateCalculator.solve(61.0, 20.0, tricky) as PlateResult.NotLoadable
        assertEquals(60.0, n.nearestBelowKg!!, 1e-9)
        assertEquals(70.0, n.nearestAboveKg!!, 1e-9)

        assertEquals(PlateResult.NotLoadable(null, 20.0), PlateCalculator.solve(15.0, 20.0, std))
    }

    @Test
    fun warmupLadderIsIncreasingAndBelowWork() {
        val steps = WarmupLadder.build(200.0, 20.0, 2.5)
        assertEquals(20.0, steps.first().kg, 1e-9)
        assertEquals(listOf(20.0, 80.0, 110.0, 140.0, 160.0, 180.0), steps.map { it.kg })
        assertTrue(WarmupLadder.build(20.0, 20.0, 2.5).isEmpty())
    }
}

package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.exp

class RecordsTest {
    private fun s(id: Long, day: Long, ex: Long, w: Double, reps: Int, rpe: Double? = null, e1rm: Double? = null) =
        AnalyticsSet(id, day, ex, w, reps, rpe, e1rm, false, false)

    @Test fun repRecordsKeepTheHeaviestPerRepCount() {
        val sets = listOf(
            s(1, 10, 1, 200.0, 1), s(2, 20, 1, 210.0, 1), s(3, 15, 1, 180.0, 3),
            s(4, 30, 1, 205.0, 1), s(5, 35, 1, 185.0, 3), s(6, 40, 2, 300.0, 1)
        )
        val r = RecordBook.forExercise(sets, 1)
        assertEquals(2, r.byReps.size)
        assertEquals(210.0, r.byReps.first { it.reps == 1 }.weightKg, 1e-9)
        assertEquals(20L, r.byReps.first { it.reps == 1 }.epochDay)
        assertEquals(185.0, r.byReps.first { it.reps == 3 }.weightKg, 1e-9)
        assertEquals(5, r.totalSets)
        // 1 + 1 + 3 + 1 + 3 = 9 reps; exercise 2 is not counted.
        assertEquals(9, r.totalReps)
        assertEquals(35L, r.lastDay)
    }

    @Test fun staleRecordsAreDetected() {
        val records = listOf(
            RepRecord(1, 200.0, 1, 1, null), RepRecord(3, 210.0, 2, 2, null), RepRecord(5, 150.0, 3, 3, null)
        )
        // The triple at 210 kg dominates the single at 200 kg.
        val stale = RecordBook.staleRecords(records)
        assertEquals(listOf(1), stale.map { it.reps })
    }

    @Test fun loadRepCurveRecoversItsParameters() {
        // Synthetic data from w(r) = 250 · exp(−0.035 (r − 1)).
        val w1 = 250.0; val k = 0.035
        val records = (1..10).map { r -> RepRecord(r, w1 * exp(-k * (r - 1)), 0, r.toLong(), null) }
        val fit = RecordBook.fitLoadRepCurve(records)!!
        assertEquals(w1, fit.first, 1e-6)
        assertEquals(k, fit.second, 1e-9)
        assertEquals(1.0, fit.third, 1e-12)
        assertEquals(w1 * exp(-k * 4), RecordBook.predict(fit, 5), 1e-9)
        assertNull(RecordBook.fitLoadRepCurve(records.take(2)))
    }

    @Test fun trainingLoadIsExponentiallyWeighted() {
        // Single session of load 1000 on day 0; the acute average jumps to λ_a · 1000 = 250.
        val sets = listOf(s(1, 0, 1, 100.0, 10, rpe = 10.0))
        val series = TrainingLoad.series(sets, 0)
        assertEquals(1, series.size)
        assertEquals(1000.0, series[0].load, 1e-9)
        assertEquals(1000.0 * 2 / 8, series[0].acute, 1e-9)
        assertEquals(1000.0 * 2 / 29, series[0].chronic, 1e-9)
        assertEquals(29.0 / 8, series[0].ratio!!, 1e-9)
        // Constant daily load converges to that load in both averages, so the ratio tends to 1.
        val daily = (0L..200L).map { d -> s(d + 1, d, 1, 100.0, 10, rpe = 10.0) }
        val long = TrainingLoad.series(daily, 200)
        assertEquals(1.0, long.last().ratio!!, 1e-3)
        assertEquals(1000.0, long.last().chronic, 1.0)
    }

    @Test fun implementMaths() {
        // Centred yoke: both hands carry half.
        val (a, b) = ImplementMath.handLoadKg(400.0, 1.2, 0.0)
        assertEquals(200.0, a, 1e-9); assertEquals(200.0, b, 1e-9)
        // Shifted by 0.3 m on a 1.2 m frame: 75 % / 25 %.
        val (c, d) = ImplementMath.handLoadKg(400.0, 1.2, 0.3)
        assertEquals(300.0, c, 1e-9); assertEquals(100.0, d, 1e-9)
        // Torque doubles with the lever arm.
        assertEquals(2 * ImplementMath.lumbarTorqueNm(100.0, 0.3), ImplementMath.lumbarTorqueNm(100.0, 0.6), 1e-9)
        assertEquals(100 * 9.80665 * 0.3, ImplementMath.lumbarTorqueNm(100.0, 0.3), 1e-9)
        // Stone over a 1.2 m bar, 5 reps at 120 kg.
        assertEquals(120 * 9.80665 * 1.2 * 5, ImplementMath.liftWorkJoule(120.0, 1.2, 5), 1e-9)
        assertEquals(ImplementMath.liftWorkJoule(120.0, 1.2, 5) / 30.0, ImplementMath.liftPowerWatt(120.0, 1.2, 5, 30.0), 1e-9)
        val (speed, pace) = ImplementMath.carrySpeed(20.0, 10.0)
        assertEquals(2.0, speed, 1e-9)
        assertEquals(5.0, pace, 1e-9)
    }

    @Test fun recordProjectionIsHonestAboutUncertainty() {
        // Perfectly linear: 200 kg on day 0 rising by 0.1 kg/day; target 250 kg is 500 days out.
        val pts = (0L..20L).map { d -> (d * 7) to (200.0 + 0.1 * d * 7) }
        val p = RecordProjection.project(pts, 250.0)!!
        assertEquals(0.7, p.slopePerWeek, 1e-9)
        assertEquals(1.0, p.r2, 1e-12)
        // Last point is day 140 at 214 kg; (250 − 214)/0.1 = 360 days.
        assertEquals(360.0, p.daysToTarget!!, 1e-6)
        assertEquals(0.0, p.sigma, 1e-9)
        // A flat series has no finite projection.
        val flat = (0L..20L).map { d -> (d * 7) to 200.0 }
        assertNull(RecordProjection.project(flat, 250.0)!!.daysToTarget)
        assertNull(RecordProjection.project(pts.take(3), 250.0))
        assertEquals(2.776, RecordProjection.t95(4), 1e-12)
        assertEquals(1.960, RecordProjection.t95(999), 1e-12)
    }

    @Test fun consistency() {
        assertEquals(0.0, Consistency.zScore(listOf(1.0, 2.0, 3.0), 2.0)!!, 1e-12)
        assertEquals(1.0, Consistency.zScore(listOf(1.0, 2.0, 3.0), 3.0)!!, 1e-12)
        assertNull(Consistency.zScore(listOf(5.0, 5.0, 5.0), 6.0))
        assertEquals(0.5, Consistency.coefficientOfVariation(listOf(1.0, 2.0, 3.0))!!, 1e-12)
    }

    @Test fun eventRecordsPickTheRightExtreme() {
        val events = listOf(
            EventSet(1, 10, 1, EventMode.FOR_TIME, 12.5, 300.0),
            EventSet(2, 20, 1, EventMode.FOR_TIME, 11.2, 300.0),
            EventSet(3, 30, 1, EventMode.MAX_REPS, 5.0, 120.0),
            EventSet(4, 40, 1, EventMode.MAX_REPS, 7.0, 120.0)
        )
        val r = RecordBook.forExercise(emptyList(), 1, events)
        assertEquals(11.2, r.events.first { it.mode == EventMode.FOR_TIME }.value, 1e-9)
        assertEquals(7.0, r.events.first { it.mode == EventMode.MAX_REPS }.value, 1e-9)
    }
}

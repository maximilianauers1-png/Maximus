package app.maximus.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class NutritionMathTest {
    private val body = Body(BioSex.MALE, 30, 180.0, 100.0, 15.0)

    @Test fun bmrFormulas() {
        assertEquals(1980.0, Energy.bmr(body, BmrFormula.MIFFLIN_ST_JEOR)!!, 1e-9)
        assertEquals(2121.572, Energy.bmr(body, BmrFormula.HARRIS_BENEDICT_REVISED)!!, 1e-6)
        assertEquals(2206.0, Energy.bmr(body, BmrFormula.KATCH_MCARDLE)!!, 1e-9)
        assertEquals(2370.0, Energy.bmr(body, BmrFormula.CUNNINGHAM)!!, 1e-9)
        assertEquals(null, Energy.bmr(body.copy(bodyFatPercent = null), BmrFormula.CUNNINGHAM))
        // Net MET cost: (6 − 1) · 100 kg · 1 h = 500 kcal.
        assertEquals(500.0, Energy.netActivityKcal(6.0, 100.0, 60.0), 1e-9)
    }

    @Test fun bodyComposition() {
        assertEquals(18.3675, BodyComposition.navyBodyFat(BioSex.MALE, 180.0, 40.0, 90.0, null)!!, 1e-3)
        assertEquals(15.0844, BodyComposition.jacksonPollock3(BioSex.MALE, 30, 50.0), 1e-3)
        val (raw, norm) = BodyComposition.ffmi(100.0, 180.0, 15.0)
        assertEquals(85.0 / 3.24, raw, 1e-9)
        assertEquals(raw, norm, 1e-9) // h = 1.8 m: no height correction
        assertEquals(30.864, BodyComposition.bmi(100.0, 180.0), 1e-3)
    }

    @Test fun macroSplit() {
        val t = MacroCalculator.targets(3000.0, 100.0, 2.0, 1.0)
        assertEquals(200.0, t.proteinG, 1e-9)
        assertEquals(100.0, t.fatG, 1e-9)
        assertEquals(325.0, t.carbG, 1e-9)
        assertEquals(42.0, t.fiberG, 1e-9)
        assertTrue(MacroCalculator.warnings(t, 100.0).isEmpty())
        assertTrue(MacroWarning.CARBS_NEGATIVE in MacroCalculator.warnings(MacroCalculator.targets(1500.0, 100.0, 2.5, 1.0), 100.0))
    }

    private fun input(tdee: Double = 3200.0) = WeightModel.Input(body, tdee) { m ->
        Energy.bmr(body.copy(weightKg = m, bodyFatPercent = null), BmrFormula.MIFFLIN_ST_JEOR)!!
    }

    @Test fun hallModelInvariants() {
        assertEquals(2.0007, WeightModel.C, 1e-4)
        // Intake = TDEE₀ at the starting weight is an exact fixed point.
        val flat = WeightModel.simulate(input(), 3200.0, 200)
        assertTrue(flat.all { abs(it.weightKg - 100.0) < 1e-9 })
        // A deficit loses mass monotonically and with diminishing speed (expenditure falls with mass).
        val loss = WeightModel.simulate(input(), 2600.0, 180).map { it.weightKg }
        for (i in 1 until loss.size) assertTrue(loss[i] < loss[i - 1])
        assertTrue(loss[1] - loss[0] < loss[180] - loss[179])
        // Initial partition at F = 15 kg: p = C/(C+F) ≈ 0.118 of the energy comes from lean tissue.
        val s = WeightModel.simulate(input(), 2200.0, 1)
        val dF = 15.0 - s[1].fatKg; val dL = 85.0 - s[1].leanKg
        val p = WeightModel.C / (WeightModel.C + 15.0)
        assertEquals(p, dL * WeightModel.RHO_L / (dL * WeightModel.RHO_L + dF * WeightModel.RHO_F), 1e-9)
    }

    @Test fun hallSolverHitsTarget() {
        val ei = WeightModel.intakeFor(input(), 95.0, 84)!!
        assertEquals(95.0, WeightModel.simulate(input(), ei, 84).last().weightKg, 0.01)
        val gain = WeightModel.intakeFor(input(), 102.0, 120)!!
        assertTrue(gain > 3200.0)
        assertEquals(RateAssessment.MODERATE_LOSS, assessRate(WeightModel.weeklyRatePercent(100.0, 95.0, 84)))
        assertEquals(RateAssessment.AGGRESSIVE_LOSS, assessRate(WeightModel.weeklyRatePercent(100.0, 90.0, 42)))
    }

    @Test fun adaptiveTdeeRecoversTruth() {
        // True slope −0.07 kg/d at 2500 kcal ⇒ TDEE = 2500 + 7700 · 0.07 = 3039 kcal/d, zero residuals ⇒ σ = 0.
        val pts = (0..27).map { d -> LogPoint(1000L + d, 100.0 - 0.07 * d, 2500.0) }
        val e = AdaptiveTdee.estimate(pts, 1027L)!!
        assertEquals(3039.0, e.tdee, 1e-6)
        assertEquals(0.0, e.sigma, 1e-6)
        assertEquals(-0.49, e.slopeKgPerWeek, 1e-9)
        assertEquals(null, AdaptiveTdee.estimate(pts.take(8), 1007L))
        val tr = AdaptiveTdee.ewma(listOf(100.0, 100.0, 90.0))
        assertEquals(99.0, tr[2], 1e-9)
    }

    @Test fun derivedTargetsUseGoal() {
        val p = NutritionProfile(weightKg = 100.0, bodyFatPercent = 15.0, goalWeightKg = 95.0, goalEpochDay = 1084L)
        val d = NutritionTargets.derive(p, 1000L, null)
        assertEquals(1980.0 * 1.85, d.tdee, 1e-9)
        assertTrue(d.projection != null && d.macros.kcal < d.tdee)
        assertEquals(p, NutritionProfile.fromMap(p.toMap()))
    }
}

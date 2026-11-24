package app.maximus.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.ln

class CoachTest {
    @Test fun coachPlanMatchesTheDocument() {
        // Day total printed in the plan: 3,730 kcal · 219 g P · 468 g C · 103 g F · ~103 SEK.
        val t = CoachPlan.dayTotal
        assertEquals(3730.0, t.kcal, 1e-9)
        assertEquals(219.0, t.proteinG, 1e-9)
        assertEquals(468.0, t.carbG, 1e-9)
        assertEquals(103.0, t.fatG, 1e-9)
        assertEquals(103.0, CoachPlan.dayCostSek, 1e-9)
        // Macro targets are energy-consistent within rounding: 4·205 + 9·110 + 4·475 = 3710 ≈ 3700.
        assertEquals(CoachPlan.KCAL, Atwater.kcal(CoachPlan.PROTEIN_G, CoachPlan.FAT_G, CoachPlan.CARB_G), 15.0)
        // The prescribed ingredient amounts reproduce the stated energy within 10 % from the food database.
        for (r in CoachPlan.meals) assertTrue(r.key, abs(r.computed.kcal / r.nutrients.kcal - 1) < 0.12)
    }

    @Test fun maintenanceReturnsCoachNumbersVerbatim() {
        val c = CoachEngine.targets(CoachPhase.MAINTENANCE, 100.0, null, 0.0)
        assertEquals(CoachPlan.targets, c.macros)
        assertEquals(0.0, c.plannedDeficit, 0.0)
    }

    @Test fun cutDeficitFollowsTargetRate() {
        // D = 0.7 % · 100 kg · 7700 / 7 = 770 kcal/d; E = 3700 − 770 = 2930.
        val c = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0)
        assertEquals(770.0, c.plannedDeficit, 1e-9)
        assertEquals(2930.0, c.macros.kcal, 1e-9)
        // P = max(205, 2.8 · 80) = 224; F = clamp(0.25·2930/9, 80, 110) = 81.39; C = remainder.
        assertEquals(224.0, c.macros.proteinG, 1e-9)
        assertEquals(0.25 * 2930 / 9, c.macros.fatG, 1e-9)
        assertEquals((2930 - 4 * 224 - 9 * c.macros.fatG) / 4, c.macros.carbG, 1e-9)
        assertTrue(c.macros.carbG / 100.0 >= 3.0) // ISSN lower bound for strength athletes
        // Deficit is capped at 25 % of maintenance for very heavy athletes: 0.7 % · 200 · 1100 = 1540 > 925.
        val heavy = CoachEngine.targets(CoachPhase.CUT, 200.0, 25.0, 0.0)
        assertEquals(0.25 * 3700, heavy.plannedDeficit, 1e-9)
        // Corrections can never push below the 25 % cap either.
        assertEquals(0.75 * 3700, CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, -2000.0).macros.kcal, 1e-9)
    }

    @Test fun recompIsSmallDeficit() {
        val c = CoachEngine.targets(CoachPhase.RECOMP, 100.0, null, 0.0)
        assertEquals(275.0, c.plannedDeficit, 1e-9)
        // F = clamp(0.25 · 3425 / 9, 80, 110) = 95.1 g
        assertEquals(0.25 * 3425 / 9, c.macros.fatG, 1e-9)
    }

    @Test fun compositionGoal() {
        // 100 kg at 20 % → FFM 80 kg; 12 % target → 80/0.88 = 90.909 kg.
        val g = CoachEngine.composition(100.0, 20.0, 12.0, -0.7)
        assertEquals(80.0, g.leanKg, 1e-9)
        assertEquals(80.0 / 0.88, g.targetWeightKg, 1e-9)
        assertEquals(ln(g.targetWeightKg / 100.0) / ln(0.993), g.weeks!!, 1e-9)
        assertNull(CoachEngine.composition(100.0, 10.0, 12.0, -0.7).weeks)
    }

    private fun series(days: Int, startKg: Double, kgPerDay: Double, kcal: Double?, protein: Double? = 210.0, today: Long = 1000L) =
        (0 until days).map { i -> DayLogPoint(today - days + 1 + i, startKg + kgPerDay * i, kcal, protein) }

    @Test fun checkInNeedsData() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0)
        val c = CoachEngine.checkIn(series(4, 100.0, 0.0, 2930.0), 1000L, 100.0, t, null)
        assertEquals(CheckInVerdict.NEED_DATA, c.verdict)
    }

    @Test fun checkInOnTrackAndCorrections() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0)
        // Exactly −0.7 %/wk: −0.1 kg/d at 100 kg.
        val ok = CoachEngine.checkIn(series(14, 101.3, -0.1, 2930.0), 1000L, 100.0, t, 950L)
        assertEquals(CheckInVerdict.ON_TRACK, ok.verdict)
        assertEquals(-0.7, ok.rateObserved!!, 1e-9)
        // Stalled (r = 0): e = 0.7 → ΔE = −0.5 · 0.007 · 100 · 1100 = −385 → limited to −250.
        val stall = CoachEngine.checkIn(series(14, 100.0, 0.0, 2930.0), 1000L, 100.0, t, 950L)
        assertEquals(CheckInVerdict.ADJUST_DOWN, stall.verdict)
        assertEquals(-250.0, stall.kcalChange, 1e-9)
        // Losing 0.35 %/wk (half the target): e = 0.35 → ΔE = −0.5 · 0.0035 · 110000 = −192.5 → rounded to −200.
        val slow = CoachEngine.checkIn(series(14, 100.0, -0.05, 2930.0), 1000L, 100.0, t, 950L)
        assertEquals(-200.0, slow.kcalChange, 1e-9)
        // Too fast (−1.4 %/wk): eat more and flag it.
        val fast = CoachEngine.checkIn(series(14, 101.0, -0.2, 2930.0), 1000L, 100.0, t, 950L)
        assertEquals(CheckInVerdict.ADJUST_UP, fast.verdict)
        assertTrue(CheckInNote.LOSS_TOO_FAST in fast.notes)
        assertEquals(250.0, fast.kcalChange, 1e-9)
    }

    @Test fun adherenceBeforeTargetChange() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0)
        // Eating 3500 instead of 2930 (+19 %): no target change, the plan is not the problem.
        val c = CoachEngine.checkIn(series(14, 100.0, 0.0, 3500.0, protein = 150.0), 1000L, 100.0, t, 950L)
        assertEquals(CheckInVerdict.ADHERENCE, c.verdict)
        assertEquals(0.0, c.kcalChange, 0.0)
        assertTrue(CheckInNote.PROTEIN_LOW in c.notes)
    }

    @Test fun dietBreakAfterEightWeeks() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0)
        val c = CoachEngine.checkIn(series(14, 101.3, -0.1, 2930.0), 1000L, 100.0, t, 1000L - 56)
        assertTrue(CheckInNote.DIET_BREAK_DUE in c.notes)
    }

    @Test fun lowProteinMealsDetected() {
        // At 100 kg the threshold is 25 g: only M5 (dates and nuts, 8 g) is below it.
        assertEquals(listOf(4), CoachEngine.lowProteinMeals(CoachPlan.meals, 100.0))
    }

    @Test fun fatLossRecipesFollowDesignRules() {
        assertEquals(20, Recipes.fatLoss.size)
        for (r in Recipes.fatLoss) {
            val n = r.nutrients
            assertTrue(r.key + " protein " + n.proteinG, n.proteinG >= 30.0)
            assertTrue(r.key + " share", 4 * n.proteinG / n.kcal >= 0.25)
            assertTrue(r.key + " density " + r.energyDensity, r.energyDensity <= 1.3)
            if (r.type == MealType.MAIN) assertTrue(r.key + " kcal " + n.kcal, n.kcal <= 750.0)
            assertTrue(RecipeTag.FAT_LOSS in r.tags)
        }
        for (t in MealType.entries) assertTrue(Recipes.fatLoss.count { it.type == t } >= 4)
    }

    @Test fun cutPlanFromFatLossRecipesHitsTargets() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0).macros
        val plan = MealPlanner.plan(t, PlanSettings(mealsPerDay = 5, distinctRecipes = 10, seed = 3L), Recipes.fatLoss)!!
        val avg = plan.days.fold(Nutrients.ZERO) { a, d -> a + d.totals } * (1.0 / 7)
        assertEquals(1.0, avg.kcal / t.kcal, 0.06)
        assertTrue(avg.proteinG >= 0.95 * t.proteinG)
    }

    @Test fun profileRoundTripWithCoachFields() {
        val p = NutritionProfile(phase = CoachPhase.CUT, phaseStartDay = 20400L, coachAdjustKcal = -150.0, lastCheckInDay = 20410L, targetBodyFatPercent = 14.0)
        assertEquals(p, NutritionProfile.fromMap(p.toMap()))
        val d = NutritionTargets.derive(p, 20410L, null)
        assertEquals(CoachPhase.CUT, d.coach!!.phase)
        assertEquals(d.coach!!.macros, d.macros)
    }
}

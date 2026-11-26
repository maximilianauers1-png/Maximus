package app.maximus.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MealPlannerTest {
    @Test fun recipeDatabaseIsConsistent() {
        assertEquals(82 + 5 + 20, Recipes.all.size)
        assertEquals(Recipes.all.size, Recipes.byKey.size)
        for (r in Recipes.all) {
            assertTrue(r.items.all { it.first in Foods.byKey })
            assertTrue(r.stepsDe.size == r.stepsEn.size && r.stepsDe.isNotEmpty())
            val n = r.nutrients
            assertTrue(n.kcal in 150.0..1600.0)
            // Coach meals carry the coach's stated values; all others are the database sum.
            if (r.stated == null) assertEquals(n.kcal, Atwater.kcal(n.proteinG, n.fatG, n.carbG, n.fiberG), 1e-6)
        }
        // Overnight oats: 80 g oats + 200 g milk + 150 g skyr + 100 g berries + 10 g chia + 10 g honey.
        // P = 0.8·13.5 + 2·3.4 + 1.5·11 + 1·1.0 + 0.1·17 + 0.1·0.4 = 36.84 g.
        assertEquals(36.84, Recipes.byKey.getValue("overnight_oats").nutrients.proteinG, 1e-9)
    }

    private val target = MacroCalculator.targets(3200.0, 100.0, 2.0, 1.0)

    @Test fun planHitsEnergyAndRespectsSettings() {
        for (meals in 2..6) {
            val s = PlanSettings(mealsPerDay = meals, distinctRecipes = 8, seed = 7L)
            val plan = MealPlanner.plan(target, s)!!
            assertEquals(7, plan.days.size)
            assertTrue(plan.distinctRecipes <= 8 + 1)
            for (d in plan.days) {
                assertEquals(meals, d.meals.size)
                val portion = d.meals.first().portions
                if (portion > MealPlanner.MIN_PORTION && portion < MealPlanner.MAX_PORTION) {
                    // Portion factor rounded to 0.05: |ŝ − s| ≤ 0.025 ⇒ relative energy error ≤ 0.025 / (ŝ − 0.025).
                    assertTrue(abs(d.totals.kcal / target.kcal - 1) <= 0.025 / (portion - 0.025) + 1e-9)
                }
            }
        }
    }

    @Test fun foodDatabaseIsConsistent() {
        assertEquals(141, Foods.all.size)
        assertEquals(Foods.all.size, Foods.byKey.size)
        for (f in Foods.all) {
            val n = f.per100
            assertEquals(n.kcal, Atwater.kcal(n.proteinG, n.fatG, n.carbG, n.fiberG), 1e-9)
            // Macronutrients cannot exceed 100 g per 100 g of food.
            assertTrue(n.proteinG + n.fatG + n.carbG + n.fiberG <= 100.5)
            assertTrue(n.kcal <= 900.1)
        }
        // Every recipe uses at least two ingredients and every food is reachable by key.
        for (r in Recipes.all) assertTrue(r.items.size >= 2)
    }

    @Test fun everyMealTypeHasEnoughRecipes() {
        for (t in MealType.entries) assertTrue(Recipes.all.count { it.type == t } >= 8)
        // Vegetarian-only planning must remain possible for every meal count.
        for (t in MealType.entries) assertTrue(Recipes.all.any { it.type == t && it.vegetarian })
    }

    @Test fun excludedAndVegetarian() {
        val s = PlanSettings(mealsPerDay = 4, distinctRecipes = 10, vegetarianOnly = true, excluded = setOf("overnight_oats"), seed = 3L)
        val plan = MealPlanner.plan(target, s)!!
        val used = plan.days.flatMap { d -> d.meals.map { it.recipe } }
        assertTrue(used.all { it.vegetarian && it.key != "overnight_oats" })
    }

    @Test fun deterministicAndShoppingListSums() {
        val s = PlanSettings(mealsPerDay = 3, distinctRecipes = 5, seed = 11L)
        val a = MealPlanner.plan(target, s)!!
        val b = MealPlanner.plan(target, s)!!
        assertEquals(a, b)
        val list = MealPlanner.shoppingList(a)
        val fromList = list.sumOf { it.grams }
        val direct = a.days.sumOf { d -> d.meals.sumOf { m -> m.recipe.items.sumOf { it.second } * m.portions } }
        assertEquals(direct, fromList, 1e-6)
    }

    @Test fun trainingCarbsPreserveTheWeeklyBudgetAndRestDayFloor() {
        val settings = PlanSettings(trainingDays = setOf(1, 3, 6), trainingCarbBoostG = 60)
        val goals = MealPlanner.weeklyTargets(target, settings, bmr = 1980.0)
        assertEquals(target.kcal * 7, goals.sumOf { it.kcal }, 1e-6)
        assertEquals(target.carbG * 7, goals.sumOf { it.carbG }, 1e-6)
        assertEquals(target.carbG + 60, goals[0].carbG, 1e-9)
        assertEquals(target.carbG - 45, goals[1].carbG, 1e-9)
        assertTrue(goals.all { it.proteinG == target.proteinG && it.fatG == target.fatG })

        val nearBmr = MacroCalculator.targets(2000.0, 100.0, 2.0, 1.0)
        val limited = MealPlanner.weeklyTargets(nearBmr, settings, bmr = 1980.0)
        assertEquals(1980.0, limited[1].kcal, 1e-9)
        assertEquals(nearBmr.kcal * 7, limited.sumOf { it.kcal }, 1e-6)
        assertEquals(List(7) { target }, MealPlanner.weeklyTargets(target, settings.copy(trainingDays = emptySet()), 1980.0))
    }

    @Test fun datedEditsDriveBothShoppingAndPrepQuantities() {
        val settings = PlanSettings(mealsPerDay = 3)
        val targets = List(7) { target }
        val original = MealPlanner.plan(targets, settings)!!
        val originalMeal = original.days[0].meals[1]
        val alternative = Recipes.all.first { it.type == originalMeal.type && it.key != originalMeal.recipe.key }
        val edit = MealOverride(0, 1, 3, alternative.key, 2.5)
        val edited = MealPlanner.applyOverrides(original, targets, settings, listOf(edit))
        assertEquals(PlannedMeal(originalMeal.type, alternative, 2.5), edited.days[0].meals[1])
        assertEquals(original.days.drop(1), edited.days.drop(1))
        assertEquals(original, MealPlanner.applyOverrides(original, targets, settings, listOf(edit.copy(mealCount = 4))))

        val batches = MealPlanner.prepBatches(edited, 0..2) + MealPlanner.prepBatches(edited, 3..6)
        val shopping = MealPlanner.shoppingList(edited)
        for (item in shopping) {
            val grams = batches.sumOf { batch -> batch.recipe.items.filter { it.first == item.food.key }.sumOf { it.second } * batch.portions }
            assertEquals(item.grams, grams, 1e-6)
        }
    }

    @Test fun trainingSettingsRoundTripAndOlderProfilesKeepTheirPlan() {
        val profile = NutritionProfile(plan = PlanSettings(trainingDays = emptySet(), trainingCarbBoostG = 40))
        assertEquals(profile, NutritionProfile.fromMap(profile.toMap()))
        val legacy = profile.toMap().filterKeys { !it.startsWith("nutrition.training") }
        assertEquals(PlanSettings(), NutritionProfile.fromMap(legacy).plan)
    }
}

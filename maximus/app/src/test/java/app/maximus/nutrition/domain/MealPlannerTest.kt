package app.maximus.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MealPlannerTest {
    @Test fun recipeDatabaseIsConsistent() {
        assertEquals(82, Recipes.all.size)
        assertEquals(Recipes.all.size, Recipes.byKey.size)
        for (r in Recipes.all) {
            assertTrue(r.items.all { it.first in Foods.byKey })
            assertTrue(r.stepsDe.size == r.stepsEn.size && r.stepsDe.isNotEmpty())
            val n = r.nutrients
            assertTrue(n.kcal in 150.0..1600.0)
            assertEquals(n.kcal, Atwater.kcal(n.proteinG, n.fatG, n.carbG, n.fiberG), 1e-6)
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
        assertEquals(135, Foods.all.size)
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
}

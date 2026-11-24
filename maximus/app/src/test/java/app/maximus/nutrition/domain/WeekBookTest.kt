package app.maximus.nutrition.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeekBookTest {
    private val book = RecipeBook()

    @Test fun isoWeekIds() {
        assertEquals("2026-W48", WeekBook.weekId(LocalDate.of(2026, 11, 24).toEpochDay()))
        // 1 Jan 2027 is a Friday and belongs to ISO week 53 of 2026.
        assertEquals("2026-W53", WeekBook.weekId(LocalDate.of(2027, 1, 1).toEpochDay()))
        assertEquals(LocalDate.of(2026, 11, 23).toEpochDay(), WeekBook.monday(LocalDate.of(2026, 11, 29).toEpochDay()))
        assertEquals(6, WeekBook.dayIndex(LocalDate.of(2026, 11, 29).toEpochDay()))
    }

    @Test fun coachWeekTotalsAndCost() {
        val w = WeekBook.coachWeek("2026-W48")
        assertEquals(35, w.filledSlots)
        assertEquals(3730.0, w.dayTotals(0, book::resolve).kcal, 1e-9)
        assertEquals(103.0, w.dayCostSek(0, book::resolve)!!, 1e-9)
        assertEquals(0.0, w.dayTotals(0, book::resolve, eatenOnly = true).kcal, 0.0)
        val eaten = w.toggleEaten(0, 0).toggleEaten(0, 1)
        assertEquals(980.0 + 935.0, eaten.dayTotals(0, book::resolve, eatenOnly = true).kcal, 1e-9)
        // 7 × 250 g chicken on the shopping list.
        assertEquals(1750.0, w.shoppingList(book::resolve).first { it.food.key == "chicken" }.grams, 1e-9)
    }

    @Test fun codecRoundTrip() {
        val w = WeekBook.coachWeek("2026-W48").set(2, 3, WeekEntry("cut_turkey_chili", 1.35, eaten = true)).set(6, 4, null)
        val back = WeekBook.decode("2026-W48", WeekBook.encode(w))
        assertEquals(w, back)
        // Garbage is skipped, not fatal.
        val g = WeekBook.decode("x", "9.9=a*1*0;0.0=BAD KEY*1*0;1.2=coach_m3*abc*1;nonsense")
        assertEquals(1, g.filledSlots)
        assertEquals(WeekEntry("coach_m3", 1.0, true), g.days[1][2])
        assertTrue(WeekBook.decode("x", null).isEmpty)
    }

    @Test fun copyClearsEatenFlags() {
        val a = WeekBook.coachWeek("2026-W47").toggleEaten(0, 0)
        val b = WeekBook("2026-W48").copyPlanFrom(a)
        assertEquals("2026-W48", b.weekId)
        assertTrue(b.days.flatten().filterNotNull().none { it.eaten })
    }

    @Test fun planMapsOntoSlots() {
        val t = CoachEngine.targets(CoachPhase.CUT, 100.0, 20.0, 0.0).macros
        val plan = MealPlanner.plan(t, PlanSettings(mealsPerDay = 5, distinctRecipes = 10, seed = 1L), Recipes.fatLoss)!!
        val w = WeekBook.fromPlan("2026-W48", plan)
        assertEquals(35, w.filledSlots)
        for (d in 0..6) for (s in 0 until WeekBook.SLOTS) {
            assertEquals(WeekBook.slotTypes[s], book.resolve(w.days[d][s]!!.recipeKey)!!.type)
        }
        assertEquals(plan.days[0].totals.kcal, w.dayTotals(0, book::resolve).kcal, 1e-6)
    }

    @Test fun customRecipeCodec() {
        val key = CustomRecipeCodec.newKey(setOf("u_1", "u_7"))
        assertEquals("u_8", key)
        val r = Recipe(key, "Mikkels Reis\u001Fpfanne", "x", MealType.MAIN, 20, setOf(RecipeTag.FAT_LOSS, RecipeTag.CUSTOM),
            listOf("chicken" to 200.0, "rice" to 80.0), listOf("Braten.", "Essen."), listOf())
        val back = CustomRecipeCodec.decode(CustomRecipeCodec.encode(r))!!
        assertEquals("Mikkels Reis pfanne", back.nameDe)
        assertEquals(r.items, back.items)
        assertEquals(setOf(RecipeTag.FAT_LOSS, RecipeTag.CUSTOM), back.tags)
        assertEquals(listOf("Braten.", "Essen."), back.stepsDe)
        assertEquals(r.computed, back.nutrients)
        assertNull(CustomRecipeCodec.decode("garbage"))
        assertEquals(back, RecipeBook(listOf(back)).resolve(key))
    }
}

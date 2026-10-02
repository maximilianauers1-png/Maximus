package app.maximus.nutrition.domain

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

data class PlanSettings(
    val mealsPerDay: Int = 4,
    /** Distinct recipes cooked per week (meal-prep friendly when small). */
    val distinctRecipes: Int = 8,
    val vegetarianOnly: Boolean = false,
    val excluded: Set<String> = emptySet(),
    val seed: Long = 1L
) {
    init {
        require(mealsPerDay in MealPlanner.MIN_MEALS..MealPlanner.MAX_MEALS)
        require(distinctRecipes in MealPlanner.MIN_DISTINCT..MealPlanner.MAX_DISTINCT)
    }
}

data class PlannedMeal(val type: MealType, val recipe: Recipe, val portions: Double) {
    val nutrients: Nutrients get() = recipe.nutrients * portions
}

data class PlannedDay(val meals: List<PlannedMeal>) {
    val totals: Nutrients get() = meals.fold(Nutrients.ZERO) { a, m -> a + m.nutrients }
}

data class WeekPlan(val days: List<PlannedDay>, val cost: Double) {
    val distinctRecipes: Int get() = days.flatMap { d -> d.meals.map { it.recipe.key } }.toSet().size
}

data class ShoppingItem(val food: Food, val grams: Double)

/**
 * Weekly plan generator.
 *
 * Slots per day follow [slots]. The week draws a pool of recipes per meal type (sizes proportional to
 * the slot count, total = distinctRecipes) and rotates through it, so each recipe is cooked in batches.
 * Each day is then scaled by one portion factor s_d = Ê/E_d (rounded to 0.05, clamped to [0.5, 3]),
 * which fixes energy; the pool is chosen to minimise the macro error
 *
 *   J = Σ_d [ 5 (E_d/Ê − 1)² + 3 (P_d/P̂ − 1)² + (F_d/F̂ − 1)² + (C_d/Ĉ − 1)² ],
 *
 * by random search over [SAMPLES] pools (deterministic given the seed). Protein carries the largest
 * weight after energy because it is the macro with a physiological minimum for strength athletes.
 */
object MealPlanner {
    const val MIN_MEALS = 2
    const val MAX_MEALS = 6
    const val MIN_DISTINCT = 3
    const val MAX_DISTINCT = 28
    const val SAMPLES = 400
    const val MIN_PORTION = 0.5
    const val MAX_PORTION = 3.0

    fun slots(mealsPerDay: Int): List<MealType> = when (mealsPerDay) {
        2 -> listOf(MealType.MAIN, MealType.MAIN)
        3 -> listOf(MealType.BREAKFAST, MealType.MAIN, MealType.MAIN)
        4 -> listOf(MealType.BREAKFAST, MealType.MAIN, MealType.SNACK, MealType.MAIN)
        5 -> listOf(MealType.BREAKFAST, MealType.SNACK, MealType.MAIN, MealType.SNACK, MealType.MAIN)
        6 -> listOf(MealType.BREAKFAST, MealType.SNACK, MealType.MAIN, MealType.SNACK, MealType.MAIN, MealType.SNACK)
        else -> throw IllegalArgumentException("mealsPerDay")
    }

    fun eligible(recipes: List<Recipe>, s: PlanSettings): List<Recipe> =
        recipes.filter { it.key !in s.excluded && (!s.vegetarianOnly || it.vegetarian) }

    fun plan(target: MacroTargets, s: PlanSettings, recipes: List<Recipe> = Recipes.all, days: Int = 7): WeekPlan? {
        val slots = slots(s.mealsPerDay)
        val perType = slots.groupingBy { it }.eachCount()
        val candidates = eligible(recipes, s).groupBy { it.type }
        if (perType.keys.any { candidates[it].isNullOrEmpty() }) return null
        val sizes = poolSizes(perType, s.distinctRecipes, candidates.mapValues { it.value.size })
        val rnd = Random(s.seed)
        var best: WeekPlan? = null
        repeat(SAMPLES) {
            val pools = sizes.mapValues { (t, n) -> candidates.getValue(t).shuffled(rnd).take(n) }
            val plan = assemble(slots, pools, target, days)
            if (best == null || plan.cost < best!!.cost) best = plan
        }
        return best
    }

    /** n_t = max(1, round(D · c_t / Σc)), capped by the number of candidates. */
    fun poolSizes(perType: Map<MealType, Int>, distinct: Int, available: Map<MealType, Int>): Map<MealType, Int> {
        val total = perType.values.sum()
        return perType.mapValues { (t, c) -> max(1, (distinct.toDouble() * c / total).roundToInt()).coerceAtMost(available[t] ?: 1) }
    }

    private fun assemble(slots: List<MealType>, pools: Map<MealType, List<Recipe>>, target: MacroTargets, days: Int): WeekPlan {
        var cost = 0.0
        val out = ArrayList<PlannedDay>(days)
        for (d in 0 until days) {
            val seen = HashMap<MealType, Int>()
            val picks = slots.map { t ->
                val k = seen.getOrDefault(t, 0); seen[t] = k + 1
                val pool = pools.getValue(t)
                val perDay = slots.count { it == t }
                t to pool[(d * perDay + k) % pool.size]
            }
            val raw = picks.fold(Nutrients.ZERO) { a, (_, r) -> a + r.nutrients }
            val s = (Math.round(target.kcal / raw.kcal * 20.0) / 20.0).coerceIn(MIN_PORTION, MAX_PORTION)
            val day = PlannedDay(picks.map { (t, r) -> PlannedMeal(t, r, s) })
            cost += dayCost(day.totals, target)
            out += day
        }
        return WeekPlan(out, cost)
    }

    fun dayCost(n: Nutrients, t: MacroTargets): Double {
        fun sq(a: Double, b: Double) = if (b <= 0) 0.0 else (a / b - 1).let { it * it }
        return 5 * sq(n.kcal, t.kcal) + 3 * sq(n.proteinG, t.proteinG) + sq(n.fatG, t.fatG) + sq(n.carbG, t.carbG)
    }

    /** Σ grams over all meals of the week, grouped by food and sorted by shop category. */
    fun shoppingList(plan: WeekPlan): List<ShoppingItem> {
        val grams = LinkedHashMap<String, Double>()
        for (d in plan.days) for (m in d.meals) for ((k, g) in m.recipe.items) grams[k] = (grams[k] ?: 0.0) + g * m.portions
        return grams.map { (k, g) -> ShoppingItem(Foods.byKey.getValue(k), g) }
            .sortedWith(compareBy({ it.food.category.ordinal }, { it.food.nameDe }))
    }
}

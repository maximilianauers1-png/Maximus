package app.maximus.nutrition.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.util.Locale

/** One planned serving in the week store. */
data class WeekEntry(val recipeKey: String, val portions: Double = 1.0, val eaten: Boolean = false)

/**
 * Weekly recipe store: 7 days × [SLOTS] meal slots (M1 breakfast … M5 evening snack, the structure of the
 * coach plan), keyed by ISO week ("2026-W48"). Persisted as one text row per week.
 */
data class WeekBook(val weekId: String, val days: List<List<WeekEntry?>> = List(7) { List(SLOTS) { null } }) {
    init {
        require(days.size == 7 && days.all { it.size == SLOTS })
    }

    val isEmpty: Boolean get() = days.all { d -> d.all { it == null } }
    val filledSlots: Int get() = days.sumOf { d -> d.count { it != null } }

    fun set(day: Int, slot: Int, e: WeekEntry?): WeekBook =
        copy(days = days.mapIndexed { i, d -> if (i != day) d else d.mapIndexed { j, x -> if (j == slot) e else x } })

    fun toggleEaten(day: Int, slot: Int): WeekBook = days[day][slot]?.let { set(day, slot, it.copy(eaten = !it.eaten)) } ?: this

    fun dayTotals(day: Int, resolve: (String) -> Recipe?, eatenOnly: Boolean = false): Nutrients =
        days[day].filterNotNull().filter { !eatenOnly || it.eaten }
            .fold(Nutrients.ZERO) { a, e -> resolve(e.recipeKey)?.let { a + it.nutrients * e.portions } ?: a }

    fun dayCostSek(day: Int, resolve: (String) -> Recipe?): Double? {
        val entries = days[day].filterNotNull()
        if (entries.isEmpty()) return null
        var sum = 0.0
        for (e in entries) sum += (resolve(e.recipeKey)?.costSek ?: return null) * e.portions
        return sum
    }

    /** Copy the plan of another week; eaten flags are cleared. */
    fun copyPlanFrom(other: WeekBook): WeekBook = copy(days = other.days.map { d -> d.map { it?.copy(eaten = false) } })

    /** Total grams per food for the week (or only the days not yet past when [fromDay] is given). */
    fun shoppingList(resolve: (String) -> Recipe?, fromDay: Int = 0): List<ShoppingItem> {
        val grams = LinkedHashMap<String, Double>()
        for (d in fromDay until 7) for (e in days[d].filterNotNull()) {
            val r = resolve(e.recipeKey) ?: continue
            for ((k, g) in r.items) grams[k] = (grams[k] ?: 0.0) + g * e.portions
        }
        return grams.mapNotNull { (k, g) -> Foods.byKey[k]?.let { ShoppingItem(it, g) } }
            .sortedWith(compareBy({ it.food.category.ordinal }, { it.food.nameDe }))
    }

    companion object {
        const val SLOTS = 5
        val slotTypes = listOf(MealType.BREAKFAST, MealType.MAIN, MealType.SNACK, MealType.MAIN, MealType.SNACK)

        fun weekId(epochDay: Long): String {
            val d = LocalDate.ofEpochDay(epochDay)
            return String.format(Locale.ROOT, "%d-W%02d", d.get(IsoFields.WEEK_BASED_YEAR), d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR))
        }

        /** Monday of the ISO week containing [epochDay]. */
        fun monday(epochDay: Long): Long = LocalDate.ofEpochDay(epochDay).with(DayOfWeek.MONDAY).toEpochDay()

        /** Day index 0 (Monday) … 6 (Sunday). */
        fun dayIndex(epochDay: Long): Int = LocalDate.ofEpochDay(epochDay).dayOfWeek.value - 1

        fun coachWeek(weekId: String): WeekBook =
            WeekBook(weekId, List(7) { CoachPlan.mealKeys.map { WeekEntry(it) } })

        /**
         * Maps a generated 5-meal plan onto the slots. The planner's slot order for 5 meals is
         * BREAKFAST, SNACK, MAIN, SNACK, MAIN; the store uses BREAKFAST, MAIN, SNACK, MAIN, SNACK, so meals are
         * assigned to the next free slot of the same type.
         */
        fun fromPlan(weekId: String, plan: WeekPlan): WeekBook = WeekBook(weekId, plan.days.map { day ->
            val out = MutableList<WeekEntry?>(SLOTS) { null }
            for (m in day.meals) {
                val slot = (0 until SLOTS).firstOrNull { out[it] == null && slotTypes[it] == m.type } ?: (0 until SLOTS).firstOrNull { out[it] == null } ?: continue
                out[slot] = WeekEntry(m.recipe.key, m.portions)
            }
            out
        })

        /** "d.s=key*portions*e;…" — keys are restricted to [a-z0-9_]. Unknown or malformed parts are skipped. */
        fun encode(b: WeekBook): String = buildList {
            b.days.forEachIndexed { d, slots ->
                slots.forEachIndexed { s, e ->
                    if (e != null) add("$d.$s=${e.recipeKey}*${String.format(Locale.ROOT, "%.2f", e.portions)}*${if (e.eaten) 1 else 0}")
                }
            }
        }.joinToString(";")

        fun decode(weekId: String, text: String?): WeekBook {
            var b = WeekBook(weekId)
            if (text.isNullOrBlank()) return b
            for (part in text.split(';')) {
                val (pos, value) = part.split('=', limit = 2).takeIf { it.size == 2 } ?: continue
                val (d, s) = pos.split('.').mapNotNull { it.toIntOrNull() }.takeIf { it.size == 2 } ?: continue
                if (d !in 0..6 || s !in 0 until SLOTS) continue
                val f = value.split('*')
                val key = f[0].takeIf { KEY.matches(it) } ?: continue
                val portions = f.getOrNull(1)?.toDoubleOrNull()?.takeIf { it > 0 && it <= 12 } ?: 1.0
                b = b.set(d, s, WeekEntry(key, portions, f.getOrNull(2) == "1"))
            }
            return b
        }

        val KEY = Regex("[a-z0-9_]{1,64}")
    }
}

/**
 * Codec for user-created recipes. Fields are separated by U+001F, list elements by U+001E; both control
 * characters are stripped from user text, so any printable text round-trips.
 */
object CustomRecipeCodec {
    private const val F = '\u001F'
    private const val L = '\u001E'
    const val PREFIX = "u_"

    private fun clean(s: String) = s.replace(F.toString(), " ").replace(L.toString(), " ").trim()

    fun encode(r: Recipe): String = listOf(
        "1", r.key, clean(r.nameDe), r.type.name, r.minutes.toString(),
        (r.tags - RecipeTag.CUSTOM).joinToString(L.toString()) { it.name },
        r.items.joinToString(L.toString()) { (k, g) -> "$k:${String.format(Locale.ROOT, "%.1f", g)}" },
        r.stepsDe.map(::clean).filter { it.isNotEmpty() }.joinToString(L.toString())
    ).joinToString(F.toString())

    fun decode(text: String): Recipe? {
        val f = text.split(F)
        if (f.size != 8 || f[0] != "1") return null
        val key = f[1].takeIf { it.startsWith(PREFIX) && WeekBook.KEY.matches(it) } ?: return null
        val name = f[2].ifBlank { return null }
        val type = MealType.entries.firstOrNull { it.name == f[3] } ?: return null
        val minutes = f[4].toIntOrNull()?.coerceIn(0, 600) ?: 0
        val tags = f[5].split(L).mapNotNull { t -> RecipeTag.entries.firstOrNull { it.name == t } }.toSet() + RecipeTag.CUSTOM
        val items = f[6].split(L).mapNotNull { part ->
            val (k, g) = part.split(':').takeIf { it.size == 2 } ?: return@mapNotNull null
            val grams = g.toDoubleOrNull()?.takeIf { it > 0 && it <= 5000 } ?: return@mapNotNull null
            if (k in Foods.byKey) k to grams else null
        }
        if (items.isEmpty()) return null
        val steps = f[7].split(L).filter { it.isNotBlank() }
        return Recipe(key, name, name, type, minutes, tags, items, steps, steps)
    }

    /** New key that does not collide with [existing]. */
    fun newKey(existing: Set<String>): String {
        var n = existing.mapNotNull { it.removePrefix(PREFIX).toIntOrNull() }.maxOrNull()?.plus(1) ?: 1
        while ("$PREFIX$n" in existing) n++
        return "$PREFIX$n"
    }
}

/** Built-in recipes plus the user's own. */
class RecipeBook(val custom: List<Recipe> = emptyList()) {
    val all: List<Recipe> = Recipes.all + custom
    val byKey: Map<String, Recipe> = Recipes.byKey + custom.associateBy { it.key }
    fun resolve(key: String): Recipe? = byKey[key]
}

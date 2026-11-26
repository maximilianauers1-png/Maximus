package app.maximus.nutrition.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import androidx.room.withTransaction
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.nutrition.domain.Food
import app.maximus.nutrition.domain.LogPoint
import app.maximus.nutrition.domain.MealOverride
import app.maximus.nutrition.domain.Nutrients
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.Recipe
import dagger.Lazy
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** One row per calendar day: optional morning weight, total intake and protein. */
@Entity(tableName = "nutrition_log")
data class NutritionLogEntity(
    @PrimaryKey val epochDay: Long,
    val weightKg: Double?,
    val kcal: Double?,
    val proteinG: Double?,
    val note: String
)

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_log ORDER BY epochDay")
    fun observeLog(): Flow<List<NutritionLogEntity>>

    @Upsert
    suspend fun upsert(e: NutritionLogEntity)

    @Query("DELETE FROM nutrition_log WHERE epochDay = :day")
    suspend fun delete(day: Long)
}

fun NutritionLogEntity.toPoint() = LogPoint(epochDay, weightKg, kcal)

data class LoggedMeal(val id: String, val day: Long, val itemKey: String, val amount: Double, val nutrients: Nutrients) {
    val isFood: Boolean get() = itemKey.startsWith("food:")
}

private const val MEALS = "nutritionMeals."
private fun planPrefix(week: Long) = "nutritionWeek.$week.plan."
private fun shoppingPrefix(week: Long) = "nutritionWeek.$week.shopping."
private fun seedKey(week: Long) = "nutritionWeek.$week.seed"
fun plannedMealId(day: Long, slot: Int) = "$day.slot-$slot"

private class ProfileEdit(val transform: (NutritionProfile) -> NutritionProfile, val result: CompletableDeferred<Unit>)

@Singleton
class NutritionRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private fun meta() = database.get().appMetaDao()
    private fun dao() = database.get().nutritionDao()

    val profile: Flow<NutritionProfile> = flow {
        emitAll(meta().observePrefix(NutritionProfile.PREFIX).map { rows -> NutritionProfile.fromMap(rows.associate { it.key to it.value }) })
    }.flowOn(io)

    val log: Flow<List<NutritionLogEntity>> = flow { emitAll(dao().observeLog()) }.flowOn(io)

    private val scope = CoroutineScope(SupervisorJob() + io)
    private val profileQueue = Channel<ProfileEdit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (edit in profileQueue) {
                try {
                    database.get().withTransaction {
                        val meta = meta()
                        val current = NutritionProfile.fromMap(meta.readPrefix(NutritionProfile.PREFIX).associate { it.key to it.value })
                        meta.upsertAll(edit.transform(current).toMap().map { (key, value) -> AppMetaEntity(key, value) })
                    }
                    edit.result.complete(Unit)
                } catch (error: CancellationException) {
                    edit.result.cancel(error)
                    throw error
                } catch (error: Exception) {
                    edit.result.completeExceptionally(error)
                }
            }
        }
    }

    suspend fun updateProfile(edit: (NutritionProfile) -> NutritionProfile) {
        val result = CompletableDeferred<Unit>()
        profileQueue.send(ProfileEdit(edit, result))
        result.await()
    }

    private fun observeMeta(prefix: String): Flow<List<AppMetaEntity>> = flow {
        emitAll(meta().observePrefix(prefix))
    }.flowOn(io)

    val meals: Flow<List<LoggedMeal>> = observeMeta(MEALS).map { rows ->
        rows.mapNotNull { row ->
            val id = row.key.removePrefix(MEALS)
            val day = id.substringBefore('.').toLongOrNull() ?: return@mapNotNull null
            val values = row.value.split('|')
            if (values.size != 7) return@mapNotNull null
            val amounts = values.drop(1).map { value ->
                value.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: return@mapNotNull null
            }
            val nutrients = Nutrients(amounts[1], amounts[2], amounts[3], amounts[4], amounts[5])
            LoggedMeal(id, day, values.first(), amounts[0], nutrients)
        }
    }.flowOn(io)

    suspend fun logMeal(day: Long, recipe: Recipe, portions: Double, slot: Int? = null) =
        logIntake(day, recipe.key, portions, recipe.nutrients * portions, slot)

    suspend fun logFood(day: Long, food: Food, grams: Double) =
        logIntake(day, "food:${food.key}", grams, food.per100 * (grams / 100.0))

    private suspend fun logIntake(day: Long, itemKey: String, amount: Double, n: Nutrients, slot: Int? = null) = withContext(io) {
        require(amount.isFinite() && amount > 0)
        val id = if (slot == null) "$day.${UUID.randomUUID()}" else plannedMealId(day, slot)
        val value = listOf(itemKey, amount, n.kcal, n.proteinG, n.fatG, n.carbG, n.fiberG).joinToString("|")
        meta().upsert(AppMetaEntity(MEALS + id, value))
    }

    suspend fun deleteMeal(id: String) = withContext(io) { meta().delete(MEALS + id) }

    fun checkedShopping(week: Long): Flow<Map<String, Int>> {
        val prefix = shoppingPrefix(week)
        return observeMeta(prefix).map { rows -> rows.mapNotNull { row ->
            row.value.toIntOrNull()?.let { row.key.removePrefix(prefix) to it }
        }.toMap() }
    }

    suspend fun setShoppingChecked(week: Long, foodKey: String, grams: Int, checked: Boolean) = withContext(io) {
        val key = shoppingPrefix(week) + foodKey
        if (checked) meta().upsert(AppMetaEntity(key, grams.toString())) else meta().delete(key)
    }

    fun weekSeed(week: Long): Flow<Long?> = flow {
        emitAll(meta().observe(seedKey(week)).map { it?.toLongOrNull() })
    }.flowOn(io)

    suspend fun rerollWeek(week: Long, originalSeed: Long) = withContext(io) {
        val key = seedKey(week)
        database.get().withTransaction {
            val seed = meta().get(key)?.toLongOrNull() ?: originalSeed
            meta().upsert(AppMetaEntity(key, (seed + 1).toString()))
        }
    }

    fun planOverrides(week: Long): Flow<List<MealOverride>> {
        val prefix = planPrefix(week)
        return observeMeta(prefix).map { rows -> rows.mapNotNull { row ->
            val day = row.key.removePrefix(prefix).substringBefore('.').toIntOrNull() ?: return@mapNotNull null
            val slot = row.key.substringAfterLast('.').toIntOrNull() ?: return@mapNotNull null
            val values = row.value.split('|')
            if (values.size != 3) return@mapNotNull null
            val count = values[0].toIntOrNull() ?: return@mapNotNull null
            val portions = values[2].toDoubleOrNull()?.takeIf { it in 0.25..12.0 } ?: return@mapNotNull null
            MealOverride(day, slot, count, values[1], portions)
        } }
    }

    suspend fun savePlanMeal(week: Long, edit: MealOverride) = withContext(io) {
        val key = planPrefix(week) + "${edit.day}.${edit.slot}"
        meta().upsert(AppMetaEntity(key, "${edit.mealCount}|${edit.recipeKey}|${edit.portions}"))
    }

    suspend fun resetPlanMeal(week: Long, day: Int, slot: Int) = withContext(io) {
        meta().delete(planPrefix(week) + "$day.$slot")
    }

    /** A row whose three values are all empty is deleted instead of stored. */
    suspend fun saveDay(e: NutritionLogEntity) = withContext(io) {
        if (e.weightKg == null && e.kcal == null && e.proteinG == null && e.note.isBlank()) dao().delete(e.epochDay) else dao().upsert(e)
    }

    suspend fun deleteDay(day: Long) = withContext(io) { dao().delete(day) }
}

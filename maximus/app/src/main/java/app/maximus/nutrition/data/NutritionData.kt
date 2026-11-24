package app.maximus.nutrition.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.nutrition.domain.CustomRecipeCodec
import app.maximus.nutrition.domain.DayLogPoint
import app.maximus.nutrition.domain.LogPoint
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.Recipe
import app.maximus.nutrition.domain.WeekBook
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
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
fun NutritionLogEntity.toDayPoint() = DayLogPoint(epochDay, weightKg, kcal, proteinG)

@Singleton
class NutritionRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    private val io = Dispatchers.IO
    private fun meta() = database.get().appMetaDao()
    private fun dao() = database.get().nutritionDao()

    val profile: Flow<NutritionProfile> = flow {
        emitAll(meta().observePrefix(NutritionProfile.PREFIX).map { rows -> NutritionProfile.fromMap(rows.associate { it.key to it.value }) })
    }.flowOn(io)

    val log: Flow<List<NutritionLogEntity>> = flow { emitAll(dao().observeLog()) }.flowOn(io)

    suspend fun saveProfile(p: NutritionProfile) = withContext(io) {
        val m = meta()
        for ((k, v) in p.toMap()) m.upsert(AppMetaEntity(k, v))
    }

    private val scope = CoroutineScope(SupervisorJob() + io)
    /** Conflated single consumer: edits are written in order and only the newest pending profile survives. */
    private val profileQueue = Channel<NutritionProfile>(Channel.CONFLATED)

    init {
        scope.launch { for (p in profileQueue) saveProfile(p) }
    }

    fun saveProfileAsync(p: NutritionProfile) { profileQueue.trySend(p) }

    /** A row whose three values are all empty is deleted instead of stored. */
    suspend fun saveDay(e: NutritionLogEntity) = withContext(io) {
        if (e.weightKg == null && e.kcal == null && e.proteinG == null && e.note.isBlank()) dao().delete(e.epochDay) else dao().upsert(e)
    }

    suspend fun deleteDay(day: Long) = withContext(io) { dao().delete(day) }

    // ---------------------------------------------------------------- weekly recipe store (app_meta rows)

    /** ISO week ids that have a stored week, newest first. */
    val storedWeeks: Flow<List<String>> = flow {
        emitAll(meta().observePrefix(WEEK_PREFIX).map { rows -> rows.filter { it.value.isNotBlank() }.map { it.key.removePrefix(WEEK_PREFIX) }.sortedDescending() })
    }.flowOn(io)

    fun week(weekId: String): Flow<WeekBook> = flow {
        emitAll(meta().observe(WEEK_PREFIX + weekId).map { WeekBook.decode(weekId, it) })
    }.flowOn(io)

    suspend fun loadWeek(weekId: String): WeekBook = withContext(io) { WeekBook.decode(weekId, meta().get(WEEK_PREFIX + weekId)) }

    /** An empty week is deleted instead of stored, so the archive only lists planned weeks. */
    suspend fun saveWeek(b: WeekBook) = withContext(io) {
        if (b.isEmpty) meta().delete(WEEK_PREFIX + b.weekId) else meta().upsert(AppMetaEntity(WEEK_PREFIX + b.weekId, WeekBook.encode(b)))
    }

    val customRecipes: Flow<List<Recipe>> = flow {
        emitAll(meta().observePrefix(RECIPE_PREFIX).map { rows -> rows.mapNotNull { CustomRecipeCodec.decode(it.value) }.sortedBy { it.nameDe.lowercase() } })
    }.flowOn(io)

    suspend fun saveCustomRecipe(r: Recipe) = withContext(io) { meta().upsert(AppMetaEntity(RECIPE_PREFIX + r.key, CustomRecipeCodec.encode(r))) }

    suspend fun deleteCustomRecipe(key: String) = withContext(io) { meta().delete(RECIPE_PREFIX + key) }

    companion object {
        /** Deliberately not under "nutrition." so profile observers are not re-triggered by week edits. */
        const val WEEK_PREFIX = "nutriweek."
        const val RECIPE_PREFIX = "nutrirecipe."
    }
}

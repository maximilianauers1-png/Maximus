package app.maximus.ui.strongman

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.maximus.strongman.data.ExerciseEntity
import app.maximus.strongman.data.StrengthSettings
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.AnalyticsSet
import app.maximus.strongman.domain.Arena
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.HistoricRecord
import app.maximus.strongman.domain.LiftBest
import app.maximus.strongman.domain.Placement
import app.maximus.strongman.domain.Population
import app.maximus.strongman.domain.ScoreMode
import app.maximus.strongman.domain.StandardLift
import java.time.LocalDate

/**
 * Snapshot of everything the Arena and the coach need, derived once per data change. Expensive parts
 * are lazy so a tab only pays for what it shows.
 */
class StrongmanData(
    val exercises: List<ExerciseEntity>,
    val sets: List<AnalyticsSet>,
    val historic: List<HistoricRecord>,
    val settings: StrengthSettings,
    val prefs: Map<String, String>,
    val table: FTable
) {
    val today: Long = LocalDate.now().toEpochDay()
    val byId: Map<Long, ExerciseEntity> = exercises.associateBy { it.id }
    val liftOf: (Long) -> StandardLift? = { id -> StandardLift.forSeedKey(byId[id]?.seedKey) }
    val isEvent: (Long) -> Boolean = { id -> byId[id]?.let { it.isEvent || it.category in EVENT_CATEGORIES } == true }
    val categoryOf: (Long) -> String? = { id -> byId[id]?.category }

    val population: Population = prefs[PREF_POPULATION]?.let { p -> Population.entries.firstOrNull { it.name == p } } ?: Population.STRONGMAN
    val mode: ScoreMode = prefs[PREF_MODE]?.let { p -> ScoreMode.entries.firstOrNull { it.name == p } }
        ?: if (settings.bodyweightKg != null) ScoreMode.DOTS else ScoreMode.ABSOLUTE

    val events: List<Arena.LiftEvent> by lazy { Arena.liftEvents(sets, historic, liftOf) }
    private val historicDays by lazy { historic.mapNotNull { h -> liftOf(h.exerciseId)?.let { it to h.epochDay } }.toSet() }
    val bests: Map<StandardLift, LiftBest> by lazy { Arena.bests(events, historicDays) }

    fun placements(population: Population = this.population, mode: ScoreMode = this.mode): List<Placement> =
        Arena.placements(bests, population, settings.sex, settings.bodyweightKg, mode)

    val xp: Long by lazy { Arena.xpOf(sets) + Arena.questXp(sets, isEvent) + historic.size * XP_HISTORIC }
    val level by lazy { Arena.level(xp) }
    val trainingDays: List<Long> by lazy { sets.map { it.epochDay }.distinct().sorted() }

    /** Exercise ids that count for [lift] (e.g. conventional and sumo deadlift). */
    fun idsFor(lift: StandardLift): Set<Long> = exercises.filter { StandardLift.forSeedKey(it.seedKey) == lift }.map { it.id }.toSet()

    fun nameOf(id: Long): String = byId[id]?.nameDe ?: "#$id"

    companion object {
        const val PREF_POPULATION = "arena.population"
        const val PREF_MODE = "arena.mode"
        const val PREF_MEET_DAY = "meet.day"
        const val PREF_MEET_NAME = "meet.name"
        const val PREF_MEET_LIMIT = "meet.limit"
        const val XP_HISTORIC = 30L
        val EVENT_CATEGORIES = setOf("STONE", "CARRY", "EVENT")
    }
}

@Composable
fun rememberStrongmanData(repository: StrongmanRepository): StrongmanData {
    val exercises by repository.exercises.collectAsState(initial = emptyList())
    val sets by repository.allSets.collectAsState(initial = emptyList())
    val historic by repository.historic.collectAsState(initial = emptyList())
    val settings by repository.settings.collectAsState(initial = StrengthSettings())
    val prefs by repository.prefs.collectAsState(initial = emptyMap())
    val table by repository.fTable.collectAsState(initial = FTable())
    return remember(exercises, sets, historic, settings, prefs, table) { StrongmanData(exercises, sets, historic, settings, prefs, table) }
}

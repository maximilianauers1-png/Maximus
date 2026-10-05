package app.maximus.chat.data

import app.maximus.chat.domain.ChatController
import app.maximus.chat.domain.ChatSettings
import app.maximus.chat.domain.ChatTools
import app.maximus.chat.domain.ModuleContext
import app.maximus.chat.engine.MediaPipeLlm
import app.maximus.dnd.data.DndCodec
import app.maximus.dnd.data.DndRepository
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.nutrition.data.NutritionRepository
import app.maximus.nutrition.data.toPoint
import app.maximus.nutrition.domain.AdaptiveTdee
import app.maximus.nutrition.domain.NutritionTargets
import app.maximus.strongman.data.StrongmanRepository
import java.time.LocalDate
import app.maximus.lab.domain.Fmt
import app.maximus.strongman.domain.Arena
import app.maximus.strongman.domain.Coach
import app.maximus.strongman.domain.Population
import app.maximus.strongman.domain.ScoreMode
import app.maximus.strongman.domain.StandardLift
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Application-wide owner of the chat: the controller outlives the screen, so an answer keeps streaming
 * when the user briefly looks at another module and the warm model is reused when they come back.
 */
@Singleton
class ChatService @Inject constructor(
    val repository: ChatRepository,
    val llm: MediaPipeLlm,
    private val strongman: StrongmanRepository,
    private val nutrition: NutritionRepository,
    private val dnd: DndRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val controller = ChatController(llm, repository, scope, personal = ::trainingSummary)

    private val _settings = MutableStateFlow(ChatSettings())
    val settings: StateFlow<ChatSettings> = _settings

    @Volatile private var settingsLoaded = false

    init {
        scope.launch { ensureSettings() }
    }

    suspend fun ensureSettings(): ChatSettings {
        if (!settingsLoaded) {
            val loaded = repository.loadSettings()
            // A model file deleted outside the app must not be kept as the active model.
            val valid = loaded.model?.let { m -> if (java.io.File(m.path).canRead()) loaded else loaded.copy(model = null) } ?: loaded
            settingsLoaded = true
            apply(valid)
        }
        return _settings.value
    }

    fun update(transform: (ChatSettings) -> ChatSettings) {
        val next = transform(_settings.value)
        apply(next)
        scope.launch { repository.saveSettings(next) }
    }

    private fun apply(s: ChatSettings) {
        _settings.value = s
        controller.settings = s
    }

    // ------------------------------------------------------------------ "Frag Maximus" context per module

    /** The last training days with all sets, plus the e1RM profile (the profile is added by the controller). */
    suspend fun strongmanContext(): String {
        val names = strongman.exercises.first().associate { it.id to it.nameDe }
        val sets = strongman.allSets.first().map { s ->
            ModuleContext.LoggedSet(s.epochDay, names[s.exerciseId] ?: "Übung ${s.exerciseId}", s.weightKg, s.reps, s.rpe)
        }
        val log = ModuleContext.strongman(sets) ?: "Noch keine Trainingseinheiten protokolliert."
        return log + "\n" + strongmanStanding()
    }

    /** Best lifts, Arena rank against the strongman population and the planned contest, for the prompt. */
    private suspend fun strongmanStanding(): String = runCatching {
        val exercises = strongman.exercises.first()
        val byId = exercises.associateBy { it.id }
        val settings = strongman.settings.first()
        val prefs = strongman.prefs.first()
        val events = Arena.liftEvents(strongman.allSets.first(), strongman.historic.first()) { StandardLift.forSeedKey(byId[it]?.seedKey) }
        val bests = Arena.bests(events)
        if (bests.isEmpty()) return@runCatching ""
        val mode = if (settings.bodyweightKg != null) ScoreMode.DOTS else ScoreMode.ABSOLUTE
        val places = Arena.placements(bests, Population.STRONGMAN, settings.sex, settings.bodyweightKg, mode).associateBy { it.lift }
        buildString {
            append("Athlet: Naturalathlet, startet bei OSG")
            settings.bodyweightKg?.let { append(", Körpergewicht ${Fmt.num(it, 4)} kg") }
            append(".\nBestleistungen (e1RM) und Rang gegen Strongman-Population (${mode.title}):\n")
            bests.values.sortedBy { it.lift.ordinal }.forEach { b ->
                val p = places[b.lift]
                append("- ${b.lift.title}: ${Fmt.num(b.e1rm, 4)} kg" + (p?.let { ", ${it.rank.title}, Perzentil ${Fmt.num(it.percentile, 3)}" } ?: "") + "\n")
            }
            prefs["meet.day"]?.toLongOrNull()?.let { day ->
                val plan = Coach.meetPlan(LocalDate.now().toEpochDay(), day)
                append("Nächster Wettkampf ${prefs["meet.name"].orEmpty()} in ${plan.daysOut} Tagen, Phase: ${plan.phase.title}.\n")
            }
        }
    }.getOrDefault("")

    suspend fun nutritionContext(): String {
        val today = LocalDate.now().toEpochDay()
        val profile = nutrition.profile.first()
        val log = nutrition.log.first()
        val adaptive = AdaptiveTdee.estimate(log.map { it.toPoint() }, today)
        val targets = NutritionTargets.derive(profile, today, adaptive)
        return ModuleContext.nutrition(profile, targets, log.map { ModuleContext.DayLog(it.epochDay, it.weightKg, it.kcal, it.proteinG) }, today)
    }

    /** One character in full, or a one-line overview of all characters. */
    suspend fun dndContext(characterId: Long? = null): String {
        if (characterId != null) {
            val e = dnd.character(characterId) ?: return ""
            return runCatching { ModuleContext.dnd(CharacterBuilder.build(DndCodec.decodeCharacter(e.payload))) }.getOrDefault(e.summary)
        }
        val all = dnd.characters.first()
        return if (all.isEmpty()) "Noch keine Charaktere angelegt."
        else "Charaktere des Nutzers: " + all.joinToString("; ") { "${it.name} (${it.summary})" }
    }

    /** Best estimated 1RM per exercise and recent training frequency; only used for strongman questions. */
    private suspend fun trainingSummary(): String? {
        val exercises = strongman.exercises.first()
        val e1rms = strongman.latestE1rms(exercises.map { it.id })
        val since = LocalDate.now().toEpochDay() - 28
        val sessions = strongman.sessions.first().count { it.epochDay >= since }
        return ChatTools.trainingSummary(exercises.mapNotNull { e -> e1rms[e.id]?.let { e.nameDe to it } }, sessions)
    }
}

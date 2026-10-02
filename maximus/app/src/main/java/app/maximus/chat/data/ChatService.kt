package app.maximus.chat.data

import app.maximus.chat.domain.ChatController
import app.maximus.chat.domain.ChatSettings
import app.maximus.chat.domain.ChatTools
import app.maximus.chat.engine.MediaPipeLlm
import app.maximus.strongman.data.StrongmanRepository
import java.time.LocalDate
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
    private val strongman: StrongmanRepository
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

    /** Best estimated 1RM per exercise and recent training frequency; only used for strongman questions. */
    private suspend fun trainingSummary(): String? {
        val exercises = strongman.exercises.first()
        val e1rms = strongman.latestE1rms(exercises.map { it.id })
        val since = LocalDate.now().toEpochDay() - 28
        val sessions = strongman.sessions.first().count { it.epochDay >= since }
        return ChatTools.trainingSummary(exercises.mapNotNull { e -> e1rms[e.id]?.let { e.nameDe to it } }, sessions)
    }
}

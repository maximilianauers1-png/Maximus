package app.maximus.chat.domain

import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatSettings(
    val model: ModelFile? = null,
    val backend: Backend = Backend.GPU,
    /** 0 = from the model profile / file name. */
    val maxTokens: Int = 0,
    val maxReply: Int = 1024,
    /** Adds a short summary of the user's logged lifts to strongman questions (stays on the device). */
    val includeTraining: Boolean = true,
    /** How long the model stays in RAM after leaving the chat; 0 = release immediately. */
    val keepLoadedSeconds: Int = 300,
    val showStats: Boolean = true
) {
    fun effectiveMaxTokens(): Int = if (maxTokens > 0) maxTokens else model?.let { ModelCatalog.maxTokensFor(it.fileName) } ?: 2048

    companion object {
        fun encode(s: ChatSettings): String = Pack.pack(
            "s1", s.model?.path.orEmpty(), s.model?.fileName.orEmpty(), (s.model?.sizeBytes ?: 0).toString(), s.backend.name,
            s.maxTokens.toString(), s.maxReply.toString(), if (s.includeTraining) "1" else "0", s.keepLoadedSeconds.toString(),
            if (s.showStats) "1" else "0"
        )

        fun decode(text: String?): ChatSettings = try {
            val f = Pack.unpack(text ?: "")
            if (f.size < 10 || f[0] != "s1") ChatSettings()
            else ChatSettings(
                model = f[1].takeIf { it.isNotEmpty() }?.let { ModelFile(it, f[2], f[3].toLong()) },
                backend = Backend.entries.firstOrNull { it.name == f[4] } ?: Backend.GPU,
                maxTokens = f[5].toInt(), maxReply = f[6].toInt(), includeTraining = f[7] == "1",
                keepLoadedSeconds = f[8].toInt(), showStats = f[9] == "1"
            )
        } catch (e: RuntimeException) {
            ChatSettings()
        }
    }
}

interface ChatStore {
    suspend fun save(c: Conversation)
}

/**
 * Orchestrates one chat: tool routing, the engine session with its KV cache, prompt planning,
 * streaming with frame-rate-limited UI updates, and persistence. All mutable state is touched only
 * from [scope] (the main thread in the app), the engine does its own threading.
 */
class ChatController(
    private val llm: LocalLlm,
    private val store: ChatStore,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
    private val personal: suspend () -> String? = { null },
    private val random: Random = Random.Default,
    /** Minimum interval between UI updates while streaming (≈ 25 fps): a recomposition per token is wasted work. */
    private val frameMs: Long = 40
) {
    enum class Phase { LOADING, PREFILL, GENERATING }

    data class Live(val text: String, val thinking: String, val tokens: Int, val phase: Phase, val tools: List<ToolCard>)

    data class State(val conversation: Conversation? = null, val live: Live? = null, val error: String? = null)

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    val engine: StateFlow<EngineState> get() = llm.state

    var settings: ChatSettings = ChatSettings()
        set(value) {
            if (value.model != field.model || value.backend != field.backend || value.effectiveMaxTokens() != field.effectiveMaxTokens()) dropSession()
            field = value
        }

    private var job: Job? = null
    private var idleJob: Job? = null
    private var lastId = 0L

    // The live engine session and what is in its KV cache.
    private var session: LlmSession? = null
    private var sessionConversation: Long? = null
    private var sessionKey: String? = null
    /** The engine load the session belongs to; a reload (e.g. after a memory-pressure release) yields a new instance. */
    private var sessionEngine: LoadInfo? = null
    private var sessionTokens = 0
    private var sessionTurns = 0
    private var sessionFocus: Focus? = null

    val busy: Boolean get() = job?.isActive == true

    private fun nextId(): Long = maxOf(now(), lastId + 1).also { lastId = it }

    fun open(conversation: Conversation?) {
        if (busy) return
        if (conversation?.id != sessionConversation) dropSession()
        _state.value = State(conversation)
    }

    fun setFocusMode(focus: Focus?) {
        val c = _state.value.conversation ?: Conversation(nextId(), "Neuer Chat", emptyList(), now())
        _state.update { it.copy(conversation = c.copy(focusMode = focus)) }
        if (c.messages.isNotEmpty()) scope.launch { store.save(c.copy(focusMode = focus)) }
    }

    /** Starts answering [text]; returns false while another answer is running. */
    fun send(text: String): Boolean {
        if (busy || text.isBlank()) return false
        job = scope.launch { runTurn(text.trim()) }
        return true
    }

    /**
     * Starts a NEW conversation from another module ("Frag Maximus"): [question] is what the user sees,
     * [context] (quiz question, training log, character sheet, …) goes only into the prompt. Returns the
     * id of the new conversation, or null while another answer is running. The conversation is saved
     * like any chat, so it can be continued in the chat screen.
     */
    fun ask(question: String, context: String, focus: Focus): Long? {
        if (busy || question.isBlank()) return null
        dropSession()
        val conv = Conversation(nextId(), Conversation.titleFrom(question), emptyList(), now(), focusMode = focus)
        _state.value = State(conv)
        job = scope.launch { runTurn(question.trim(), context) }
        return conv.id
    }

    /** Stops the running generation; the partial answer is kept and marked. */
    fun stop() {
        job?.cancel()
    }

    /** Answers the last question again from a clean session. */
    fun regenerate(): Boolean {
        val c = _state.value.conversation ?: return false
        if (busy) return false
        val lastUser = c.messages.indexOfLast { it.role == Role.USER }
        if (lastUser < 0) return false
        val text = c.messages[lastUser].text
        dropSession()
        _state.update { it.copy(conversation = c.copy(messages = c.messages.subList(0, lastUser))) }
        return send(text)
    }

    /** Chat screen left: release the model after the keep-alive time (frees up to several GB). */
    fun onHidden() {
        idleJob?.cancel()
        idleJob = scope.launch {
            delay(settings.keepLoadedSeconds * 1000L)
            while (busy) delay(1000)
            dropSession()
            llm.release()
        }
    }

    /** Chat screen shown: cancel a pending release and warm the engine up so the first answer starts fast. */
    fun onVisible() {
        idleJob?.cancel()
        val model = settings.model ?: return
        if (llm.state.value is EngineState.Ready || llm.state.value is EngineState.Loading) return
        scope.launch { runCatching { llm.load(EngineConfig(model, settings.backend, settings.effectiveMaxTokens())) } }
    }

    fun releaseNow() {
        if (busy) return
        dropSession()
        llm.release()
    }

    private fun dropSession() {
        runCatching { session?.close() }
        session = null
        sessionConversation = null
        sessionKey = null
        sessionEngine = null
        sessionTokens = 0
        sessionTurns = 0
        sessionFocus = null
    }

    private fun publish(c: Conversation, live: Live?, error: String? = null) {
        _state.value = State(c, live, error)
    }

    private suspend fun runTurn(text: String, extraContext: String = "") {
        val start = _state.value.conversation
        val history = start?.messages.orEmpty()
        val previousFocus = history.lastOrNull { it.focus != null }?.focus
        val routed = ToolRouter.route(text, start?.focusMode, previousFocus, random)
        val user = ChatMessage(nextId(), Role.USER, text, focus = routed.focus, createdAt = now())
        var conv = (start ?: Conversation(user.id, Conversation.titleFrom(text), emptyList(), now()))
            .let { it.copy(messages = it.messages + user, updatedAt = now(), title = if (it.messages.isEmpty()) Conversation.titleFrom(text) else it.title) }
        val cards = routed.results.map { it.card }

        if (routed.direct) {
            conv = finish(conv, ChatMessage(nextId(), Role.ASSISTANT, "", tools = cards, focus = routed.focus, createdAt = now()))
            return
        }
        val model = settings.model
        if (model == null) {
            conv = finish(conv, ChatMessage(nextId(), Role.ASSISTANT, NO_MODEL, tools = cards, focus = routed.focus, createdAt = now()))
            return
        }
        publish(conv, Live("", "", 0, Phase.LOADING, cards))

        val filter = StreamFilter()
        var promptTokens = 0
        var deltas = 0
        var firstAt = -1L
        val t0: Long
        var halted = false
        var failure: String? = null
        var backendLabel = settings.backend.label
        try {
            val info = llm.load(EngineConfig(model, settings.backend, settings.effectiveMaxTokens()))
            backendLabel = info.backend.label
            val key = "${model.path}|${info.backend}|${info.maxTokens}"
            if (session == null || sessionConversation != conv.id || sessionKey != key || sessionEngine !== info) dropSession()
            val personalText = if (routed.focus == Focus.STRONGMAN && settings.includeTraining) runCatching { personal() }.getOrNull() else null
            val context = listOf(extraContext, routed.context).filter { it.isNotBlank() }.joinToString("\n\n")
            val turn = PromptPlanner.turn(routed.query, context, personalText)

            var s = session ?: llm.newSession(routed.focus.sampling).also { session = it; sessionConversation = conv.id; sessionKey = key; sessionEngine = info }
            var prompt = if (sessionTurns == 0) PromptPlanner.opening(routed.focus, history, turn, info.maxTokens)
            else PromptPlanner.followUp(routed.focus, sessionFocus, turn)
            promptTokens = s.countTokens(prompt)
            if (sessionTurns > 0 && !PromptPlanner.fits(sessionTokens, promptTokens, info.maxTokens, settings.maxReply)) {
                dropSession()
                s = llm.newSession(routed.focus.sampling).also { session = it; sessionConversation = conv.id; sessionKey = key; sessionEngine = info }
                prompt = PromptPlanner.opening(routed.focus, history, turn, info.maxTokens)
                promptTokens = s.countTokens(prompt)
            }
            if (sessionTurns == 0) sessionFocus = routed.focus

            publish(conv, Live("", "", 0, Phase.PREFILL, cards))
            t0 = now()
            var lastFrame = 0L
            try {
                s.generate(prompt).collect { delta ->
                    if (firstAt < 0) firstAt = now()
                    deltas++
                    val snap = filter.push(delta)
                    if (snap.ended || snap.looping || deltas >= settings.maxReply) {
                        halted = snap.looping || deltas >= settings.maxReply
                        throw Halt()
                    }
                    val t = now()
                    if (t - lastFrame >= frameMs) {
                        lastFrame = t
                        publish(conv, Live(snap.visible, snap.thinking, deltas, Phase.GENERATING, cards))
                    }
                }
            } catch (h: Halt) {
                // Early stop requested by the filter; the collector's cancellation stops the engine.
            }
            withContext(NonCancellable) {
                val outTokens = runCatching { s.countTokens(filter.rawText) }.getOrDefault(deltas)
                sessionTokens += promptTokens + outTokens
                sessionTurns++
                if (halted) dropSession() // a loop or a cut-off answer must not poison later turns
                conv = finish(conv, reply(filter, cards, routed.focus, GenStats(promptTokens, outTokens, (if (firstAt < 0) now() else firstAt) - t0, now() - t0, backendLabel), halted))
            }
        } catch (e: CancellationException) {
            withContext(NonCancellable) {
                dropSession() // the native session may be mid-turn; start clean next time
                conv = finish(conv, reply(filter, cards, routed.focus, null, stopped = true))
            }
            throw e
        } catch (e: Exception) {
            failure = e.message ?: e.javaClass.simpleName
            dropSession()
            conv = finish(conv, ChatMessage(nextId(), Role.ASSISTANT, "⚠ Das Sprachmodell konnte nicht antworten: $failure", tools = cards, focus = routed.focus, createdAt = now()), failure)
        }
    }

    private fun reply(filter: StreamFilter, cards: List<ToolCard>, focus: Focus, stats: GenStats?, stopped: Boolean): ChatMessage {
        val snap = filter.snapshot()
        val text = snap.visible.trim().ifEmpty { if (stopped) "(abgebrochen)" else "" }
        return ChatMessage(nextId(), Role.ASSISTANT, text, tools = cards, thinking = snap.thinking, stats = stats, focus = focus, stopped = stopped, createdAt = now())
    }

    private suspend fun finish(conv: Conversation, reply: ChatMessage, error: String? = null): Conversation {
        val done = conv.copy(messages = conv.messages + reply, updatedAt = now())
        publish(done, null, error)
        store.save(done)
        return done
    }

    private class Halt : RuntimeException() {
        override fun fillInStackTrace(): Throwable = this
    }

    companion object {
        const val NO_MODEL = "Es ist noch kein Sprachmodell eingerichtet. Die Werkzeuge oben funktionieren trotzdem.\n\n" +
            "Lege unter **Modell** (oben rechts) eine .task-Datei an, zum Beispiel Gemma 3n E2B. Danach läuft alles lokal auf diesem Gerät."

        /** Fixed prompt for the speed test; same text for every backend so the numbers compare. */
        const val BENCHMARK_PROMPT = "Erkläre in fünf Sätzen, warum der Himmel blau ist, und nenne die Formel der Rayleigh-Streuung."
    }

    /**
     * Speed test: fresh session, fixed prompt, at most [maxDeltas] streamed pieces. Returns prefill and
     * decode rates for the requested backend (the engine is reloaded if needed).
     */
    suspend fun benchmark(backend: Backend, maxDeltas: Int = 96): GenStats {
        val model = settings.model ?: error("Kein Modell")
        dropSession()
        val info = llm.load(EngineConfig(model, backend, settings.effectiveMaxTokens()))
        val s = llm.newSession(Focus.GENERAL.sampling)
        try {
            val prompt = PromptPlanner.opening(Focus.GENERAL, emptyList(), BENCHMARK_PROMPT, info.maxTokens)
            val promptTokens = s.countTokens(prompt)
            val filter = StreamFilter()
            var first = -1L
            var n = 0
            val t0 = now()
            try {
                s.generate(prompt).collect { d ->
                    if (first < 0) first = now()
                    filter.push(d)
                    if (++n >= maxDeltas) throw Halt()
                }
            } catch (h: Halt) {
            }
            val end = now()
            val out = runCatching { s.countTokens(filter.rawText) }.getOrDefault(n)
            return GenStats(promptTokens, out, (if (first < 0) end else first) - t0, end - t0, info.backend.label)
        } finally {
            runCatching { s.close() }
        }
    }
}

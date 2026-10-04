package app.maximus.chat.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Scripted engine: answers with [replies] in order, one word per streamed piece, 10 ms apart. */
private class FakeLlm(var replies: MutableList<String>) : LocalLlm {
    override val state = MutableStateFlow<EngineState>(EngineState.Idle)
    val sessions = ArrayList<FakeSession>()
    var loads = 0
    var releases = 0
    var failLoad = false
    private var loaded: Pair<EngineConfig, LoadInfo>? = null

    override suspend fun load(config: EngineConfig): LoadInfo {
        loaded?.let { (c, i) -> if (c == config) return i }
        loads++
        if (failLoad) error("GPU-Treiber abgelehnt")
        val info = LoadInfo(config.backend, config.maxTokens, 5, false)
        loaded = config to info
        state.value = EngineState.Ready(config.model, info)
        return info
    }

    override suspend fun newSession(sampling: Sampling): LlmSession = FakeSession(this, sampling).also { sessions += it }

    override fun release() {
        releases++
        loaded = null
        state.value = EngineState.Idle
    }

    class FakeSession(private val llm: FakeLlm, val sampling: Sampling) : LlmSession {
        val prompts = ArrayList<String>()
        var closed = false
        override suspend fun countTokens(text: String): Int = text.split(Regex("\\s+")).count { it.isNotEmpty() }
        override fun generate(prompt: String): Flow<String> = flow {
            prompts += prompt
            val reply = llm.replies.removeAt(0)
            for (w in Regex("\\S+\\s*").findAll(reply)) { delay(10); emit(w.value) }
        }
        override fun close() { closed = true }
    }
}

private class MemoryStore : ChatStore {
    val saved = LinkedHashMap<Long, Conversation>()
    override suspend fun save(c: Conversation) { saved[c.id] = c }
}

class ChatControllerTest {
    private val model = ModelFile("/m/gemma-3n-E2B-it-int4.task", "gemma-3n-E2B-it-int4.task", 3_000_000_000)

    private fun TestScope.controller(llm: FakeLlm, store: ChatStore = MemoryStore(), maxTokens: Int = 4096) =
        ChatController(llm, store, this, now = { testScheduler.currentTime + 1 }, personal = { "Kreuzheben e1RM 260 kg" }).also {
            it.settings = ChatSettings(model = model, maxTokens = maxTokens, maxReply = 200)
        }

    @Test
    fun withoutModelToolsStillAnswer() = runTest {
        val llm = FakeLlm(mutableListOf())
        val c = ChatController(llm, MemoryStore(), this, now = { testScheduler.currentTime + 1 })
        c.send("Würfle 1d20")
        advanceUntilIdle()
        val reply = c.state.value.conversation!!.messages.last()
        assertEquals(ChatController.NO_MODEL, reply.text)
        assertEquals("w", reply.tools.single().tool)
        assertEquals(0, llm.loads)
    }

    @Test
    fun slashCommandsNeverTouchTheModel() = runTest {
        val llm = FakeLlm(mutableListOf())
        val store = MemoryStore()
        val c = controller(llm, store)
        c.send("/1rm 200x3")
        advanceUntilIdle()
        val conv = c.state.value.conversation!!
        assertEquals(2, conv.messages.size)
        assertEquals("1rm", conv.messages[1].tools.single().tool)
        assertEquals(0, llm.loads)
        assertEquals(conv, store.saved[conv.id])
    }

    @Test
    fun followUpsReuseTheSessionAndOnlySendTheNewTurn() = runTest {
        val llm = FakeLlm(mutableListOf("Erste Antwort zum Log.", "Zweite Antwort."))
        val c = controller(llm)
        c.send("Wie trainiere ich Log Clean and Press?")
        advanceUntilIdle()
        c.send("Und wie viele Sätze?")
        advanceUntilIdle()
        assertEquals(1, llm.sessions.size)
        val prompts = llm.sessions[0].prompts
        assertTrue(prompts[0].startsWith(Focus.PERSONA))
        assertTrue("personal data for strongman", prompts[0].contains("Kreuzheben e1RM 260 kg"))
        assertFalse(prompts[1].contains(Focus.PERSONA))
        val msgs = c.state.value.conversation!!.messages
        assertEquals(listOf(Role.USER, Role.ASSISTANT, Role.USER, Role.ASSISTANT), msgs.map { it.role })
        assertEquals("Zweite Antwort.", msgs.last().text)
        val stats = msgs.last().stats!!
        assertEquals(2, stats.outputTokens)
        assertTrue(stats.totalMs >= stats.firstTokenMs)
        assertEquals(Focus.STRONGMAN.sampling, llm.sessions[0].sampling)
    }

    @Test
    fun contextOverflowOpensAFreshSessionWithReplay() = runTest {
        val long = (1..120).joinToString(" ") { "wort$it" }
        val llm = FakeLlm(mutableListOf(long, "Kurz."))
        val c = controller(llm, maxTokens = 360)
        c.send("Erste Frage zur Thermodynamik")
        advanceUntilIdle()
        c.send("Zweite Frage")
        advanceUntilIdle()
        assertEquals(2, llm.sessions.size)
        assertTrue(llm.sessions[0].closed)
        assertTrue(llm.sessions[1].prompts.single().contains("Bisheriger Gesprächsverlauf"))
    }

    @Test
    fun loopsAreCutAndTheSessionIsReset() = runTest {
        val llm = FakeLlm(mutableListOf("Start " + "das wiederholt sich immer ".repeat(30), "Neu."))
        val c = controller(llm)
        c.send("Hallo")
        advanceUntilIdle()
        val reply = c.state.value.conversation!!.messages.last()
        assertTrue(reply.stopped)
        assertTrue(reply.text.length < 400)
        c.send("Nochmal")
        advanceUntilIdle()
        assertEquals(2, llm.sessions.size)
    }

    @Test
    fun thinkingIsSeparatedFromTheAnswer() = runTest {
        val llm = FakeLlm(mutableListOf("<think>2 mal 3 ist 6</think> Die Antwort ist 6."))
        val c = controller(llm)
        c.send("Was ist 2 mal 3?")
        advanceUntilIdle()
        val reply = c.state.value.conversation!!.messages.last()
        assertEquals("Die Antwort ist 6.", reply.text)
        assertEquals("2 mal 3 ist 6", reply.thinking)
    }

    @Test
    fun stopKeepsThePartialAnswer() = runTest {
        val llm = FakeLlm(mutableListOf((1..100).joinToString(" ") { "w$it" }))
        val c = controller(llm)
        c.send("Erzähl was Langes")
        advanceTimeBy(205)
        assertTrue(c.busy)
        assertEquals(ChatController.Phase.GENERATING, c.state.value.live!!.phase)
        c.stop()
        advanceUntilIdle()
        val reply = c.state.value.conversation!!.messages.last()
        assertTrue(reply.stopped)
        assertTrue(reply.text, reply.text.startsWith("w1 w2"))
        assertNull(c.state.value.live)
        assertFalse(c.busy)
    }

    @Test
    fun loadFailureBecomesAMessage() = runTest {
        val llm = FakeLlm(mutableListOf()).apply { failLoad = true }
        val c = controller(llm)
        c.send("Hallo")
        advanceUntilIdle()
        assertTrue(c.state.value.conversation!!.messages.last().text.contains("GPU-Treiber abgelehnt"))
    }

    @Test
    fun regenerateAnswersTheLastQuestionAgain() = runTest {
        val llm = FakeLlm(mutableListOf("Alt.", "Neu."))
        val c = controller(llm)
        c.send("Frage")
        advanceUntilIdle()
        assertTrue(c.regenerate())
        advanceUntilIdle()
        val msgs = c.state.value.conversation!!.messages
        assertEquals(2, msgs.size)
        assertEquals("Neu.", msgs.last().text)
        assertEquals(2, llm.sessions.size)
    }

    @Test
    fun idleReleaseAfterLeavingTheChat() = runTest {
        val llm = FakeLlm(mutableListOf("Ok."))
        val c = controller(llm)
        c.settings = c.settings.copy(keepLoadedSeconds = 60)
        c.send("Hallo")
        advanceUntilIdle()
        c.onHidden()
        advanceTimeBy(59_000)
        assertEquals(0, llm.releases)
        advanceTimeBy(2_000)
        assertEquals(1, llm.releases)
        c.onVisible()
        advanceUntilIdle()
        assertEquals(2, llm.loads)
    }

    @Test
    fun anEngineReleasedBehindTheControllersBackGetsAFreshSession() = runTest {
        val llm = FakeLlm(mutableListOf("Eins.", "Zwei."))
        val c = controller(llm)
        c.send("Erste Frage")
        advanceUntilIdle()
        llm.release() // e.g. HeavyResourceGovernor under memory pressure
        c.send("Zweite Frage")
        advanceUntilIdle()
        assertEquals(2, llm.sessions.size)
        assertTrue(llm.sessions[1].prompts.single().contains("Bisheriger Gesprächsverlauf"))
        assertEquals("Zwei.", c.state.value.conversation!!.messages.last().text)
    }

    @Test
    fun askFromAModuleStartsANewChatWithHiddenContext() = runTest {
        val llm = FakeLlm(mutableListOf("Alt.", "Die Kraft ist konservativ."))
        val store = MemoryStore()
        val c = controller(llm, store)
        c.send("Erste Unterhaltung")
        advanceUntilIdle()
        val first = c.state.value.conversation!!.id
        val id = c.ask("Erkläre mir diese Frage", "Quizfrage: Ist F = −∇V konservativ? Richtige Antwort: ja", Focus.SCIENCE)!!
        assertTrue(id != first)
        advanceUntilIdle()
        val conv = c.state.value.conversation!!
        assertEquals(id, conv.id)
        assertEquals(Focus.SCIENCE, conv.focusMode)
        assertEquals("Erkläre mir diese Frage", conv.messages.first().text) // the context is not shown
        assertTrue(llm.sessions.last().prompts.single().contains("Richtige Antwort: ja"))
        assertEquals(2, store.saved.size)
        assertNull(c.ask("", "", Focus.GENERAL))
    }

    @Test
    fun benchmarkMeasuresBothPhases() = runTest {
        val llm = FakeLlm(mutableListOf((1..200).joinToString(" ") { "t$it" }))
        val c = controller(llm)
        val stats = c.benchmark(Backend.CPU, maxDeltas = 50)
        assertEquals("CPU", stats.backend)
        assertEquals(50, stats.outputTokens)
        assertTrue(stats.decodeTokensPerSecond > 0)
    }
}

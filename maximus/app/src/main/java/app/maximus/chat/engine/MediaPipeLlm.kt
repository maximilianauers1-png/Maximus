package app.maximus.chat.engine

import android.content.Context
import android.os.SystemClock
import app.maximus.chat.domain.Backend
import app.maximus.chat.domain.EngineConfig
import app.maximus.chat.domain.EngineState
import app.maximus.chat.domain.LlmSession
import app.maximus.chat.domain.LoadInfo
import app.maximus.chat.domain.LocalLlm
import app.maximus.chat.domain.ModelCatalog
import app.maximus.chat.domain.Sampling
import app.maximus.core.memory.HeavyComponent
import app.maximus.core.memory.HeavyResourceGovernor
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The on-device model through Google's MediaPipe LLM Inference runtime (XNNPACK int4/int8 kernels on the
 * CPU, OpenCL/OpenGL delegate on the GPU). This is the ONLY file that touches the runtime; everything
 * else talks to [LocalLlm], so swapping in another runtime (LiteRT-LM, llama.cpp) means replacing this file.
 *
 * Threading: every native call except the streaming callback runs on one dedicated thread, which
 * serialises load/release/session operations without locks in native code. No network is involved:
 * the app has no INTERNET permission at all (removed in the manifest), the model is a local file.
 *
 * Memory: weights are memory-mapped plus the KV cache, M ≈ M_weights + 2·n_layer·n_kv·d_head·n_ctx·2 B.
 * The engine registers with [HeavyResourceGovernor], which releases it under memory pressure.
 */
@Singleton
class MediaPipeLlm @Inject constructor(
    @ApplicationContext private val context: Context,
    private val governor: HeavyResourceGovernor
) : LocalLlm {

    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "maximus-llm").apply { isDaemon = true } }
    private val dispatcher = executor.asCoroutineDispatcher()
    private val mutex = Mutex()

    // Mutated only on the engine thread.
    private var engine: LlmInference? = null
    private var loadedFor: EngineConfig? = null
    private var loadInfo: LoadInfo? = null
    private val openSessions = HashSet<MpSession>()

    /** Incremented on every release; sessions of an older engine become inert instead of touching freed memory. */
    @Volatile private var epoch = 0

    private val _state = MutableStateFlow<EngineState>(EngineState.Idle)
    override val state: StateFlow<EngineState> = _state

    private val heavy = object : HeavyComponent {
        override val name = "Sprachmodell (Maximus)"
        override fun release() = releaseInternal()
    }

    override suspend fun load(config: EngineConfig): LoadInfo = mutex.withLock {
        withContext(dispatcher) {
            val current = loadInfo
            if (engine != null && current != null && loadedFor == config) return@withContext current
            closeEngine()
            require(File(config.model.path).canRead()) { "Modelldatei nicht lesbar: ${config.model.fileName}" }
            governor.acquire(heavy)
            _state.value = EngineState.Loading(config.model, config.backend)
            val started = SystemClock.elapsedRealtime()
            val backends = if (config.backend == Backend.GPU) listOf(Backend.GPU, Backend.CPU) else listOf(Backend.CPU)
            var lastError: Throwable? = null
            for (backend in backends) {
                for (maxTokens in ModelCatalog.fallbackLadder(config.maxTokens)) {
                    try {
                        val options = LlmInference.LlmInferenceOptions.builder()
                            .setModelPath(config.model.path)
                            .setMaxTokens(maxTokens)
                            .setMaxTopK(64)
                            .setPreferredBackend(if (backend == Backend.GPU) LlmInference.Backend.GPU else LlmInference.Backend.CPU)
                            .build()
                        val created = LlmInference.createFromOptions(context, options)
                        val info = LoadInfo(backend, maxTokens, SystemClock.elapsedRealtime() - started,
                            fellBack = backend != config.backend || maxTokens != config.maxTokens)
                        engine = created
                        loadedFor = config
                        loadInfo = info
                        _state.value = EngineState.Ready(config.model, info)
                        return@withContext info
                    } catch (e: Exception) {
                        lastError = e
                    }
                }
            }
            governor.releaseIfResident(heavy)
            val message = lastError?.message ?: "unbekannter Fehler"
            _state.value = EngineState.Failed(message)
            throw IllegalStateException("Modell konnte nicht geladen werden: $message", lastError)
        }
    }

    override suspend fun newSession(sampling: Sampling): LlmSession = withContext(dispatcher) {
        val e = engine ?: error("Kein Modell geladen")
        val options = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTopK(sampling.topK)
            .setTopP(sampling.topP)
            .setTemperature(sampling.temperature)
            .setRandomSeed(Random.nextInt(1, Int.MAX_VALUE))
            .build()
        MpSession(LlmInferenceSession.createFromOptions(e, options), epoch).also { openSessions += it }
    }

    override fun release() {
        governor.releaseIfResident(heavy)
        releaseInternal()
    }

    /** Idempotent; may be called from any thread (the governor calls it from onTrimMemory). */
    private fun releaseInternal() {
        epoch++
        _state.value = EngineState.Idle
        executor.execute { closeEngine() }
    }

    /** Engine thread only. Sessions must be closed before their engine. */
    private fun closeEngine() {
        openSessions.toList().forEach { it.closeNative() }
        openSessions.clear()
        runCatching { engine?.close() }
        engine = null
        loadedFor = null
        loadInfo = null
    }

    private inner class MpSession(private val session: LlmInferenceSession, private val bornIn: Int) : LlmSession {
        @Volatile private var closed = false
        @Volatile private var generating = false

        private fun alive() = !closed && bornIn == epoch

        override suspend fun countTokens(text: String): Int = withContext(dispatcher) {
            check(alive()) { "Sitzung beendet" }
            session.sizeInTokens(text)
        }

        /**
         * Streams the answer. The runtime calls the listener from its own thread with text deltas; they
         * go through an unbounded channel, so the native side never blocks on the UI.
         */
        override fun generate(prompt: String): Flow<String> = callbackFlow {
            check(alive()) { "Sitzung beendet" }
            session.addQueryChunk(prompt)
            generating = true
            val future = session.generateResponseAsync { partial: String, done: Boolean ->
                if (partial.isNotEmpty()) trySend(partial)
                if (done) {
                    generating = false
                    close()
                }
            }
            future.addListener({
                try {
                    future.get()
                } catch (e: Exception) {
                    generating = false
                    close(e.cause ?: e)
                }
            }, { it.run() })
            awaitClose {
                if (generating && alive()) {
                    generating = false
                    executor.execute { if (alive()) runCatching { session.cancelGenerateResponseAsync() } }
                }
            }
        }.buffer(Channel.UNLIMITED).flowOn(dispatcher)

        override fun close() {
            if (closed) return
            closed = true
            executor.execute {
                if (openSessions.remove(this)) closeNative()
            }
        }

        fun closeNative() {
            closed = true
            if (generating) runCatching { session.cancelGenerateResponseAsync() }
            runCatching { session.close() }
        }
    }
}

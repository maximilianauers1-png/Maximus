package app.maximus.chat.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class Backend(val label: String) { GPU("GPU"), CPU("CPU") }

/** A model file in app storage. [maxTokens] is the context (prompt + answer) the engine is opened with. */
data class ModelFile(val path: String, val fileName: String, val sizeBytes: Long)

data class EngineConfig(val model: ModelFile, val backend: Backend, val maxTokens: Int)

data class LoadInfo(val backend: Backend, val maxTokens: Int, val loadMs: Long, val fellBack: Boolean)

sealed interface EngineState {
    data object Idle : EngineState
    data class Loading(val model: ModelFile, val backend: Backend) : EngineState
    data class Ready(val model: ModelFile, val info: LoadInfo) : EngineState
    data class Failed(val message: String) : EngineState
}

/**
 * The on-device language model. Implementations keep exactly one engine resident and run every native
 * call on one dedicated thread; [load] is idempotent for an unchanged configuration.
 */
interface LocalLlm {
    val state: StateFlow<EngineState>

    /**
     * Loads (or keeps) the engine; tries [EngineConfig.backend] first and falls back to the CPU. While the
     * engine stays loaded the SAME [LoadInfo] instance is returned, so callers detect reloads by identity.
     */
    suspend fun load(config: EngineConfig): LoadInfo

    /** Opens a conversation session with its own KV cache. Only one session is used at a time. */
    suspend fun newSession(sampling: Sampling): LlmSession

    /** Frees weights, KV cache and native buffers. Idempotent. */
    fun release()
}

interface LlmSession {
    /** Exact token count of [text] with the model's tokenizer. */
    suspend fun countTokens(text: String): Int

    /**
     * Appends [prompt] as the next user turn (the runtime applies the model's chat template) and streams
     * the answer as text deltas. Cancelling the collector cancels the generation; the KV cache keeps
     * every completed turn, so later turns only pay prefill for their own new tokens.
     */
    fun generate(prompt: String): Flow<String>

    fun close()
}

/** Known on-device models (MediaPipe/LiteRT ".task" bundles) and how to run them on an 8-GB phone. */
data class ModelProfile(
    val key: String,
    val name: String,
    val pattern: Regex,
    val approxGb: Double,
    val defaultMaxTokens: Int,
    val strengths: String,
    val source: String,
    val thinking: Boolean = false,
    val recommended: Boolean = false
)

object ModelCatalog {
    val profiles = listOf(
        ModelProfile(
            "gemma3n-e2b", "Gemma 3n E2B (int4)", Regex("gemma-?3n.*e2b", RegexOption.IGNORE_CASE), 3.1, 4096,
            "Beste Qualität für Deutsch und Erklärungen auf dem A55; ausgewogenes Tempo.",
            "huggingface.co/google/gemma-3n-E2B-it-litert-preview", recommended = true
        ),
        ModelProfile(
            "gemma3n-e4b", "Gemma 3n E4B (int4)", Regex("gemma-?3n.*e4b", RegexOption.IGNORE_CASE), 4.4, 4096,
            "Noch stärker, aber auf 8 GB RAM grenzwertig und deutlich langsamer.",
            "huggingface.co/google/gemma-3n-E4B-it-litert-preview"
        ),
        ModelProfile(
            "gemma3-1b", "Gemma 3 1B (int4)", Regex("gemma-?3-1b", RegexOption.IGNORE_CASE), 0.55, 2048,
            "Sehr schnell und klein; gut für kurze Fragen, schwächer bei Herleitungen.",
            "huggingface.co/litert-community/Gemma3-1B-IT"
        ),
        ModelProfile(
            "qwen25-1.5b", "Qwen 2.5 1.5B Instruct", Regex("qwen2\\.?5.*1\\.5b", RegexOption.IGNORE_CASE), 1.6, 4096,
            "Stark bei Mathe und Code für seine Größe.",
            "huggingface.co/litert-community/Qwen2.5-1.5B-Instruct"
        ),
        ModelProfile(
            "deepseek-r1-1.5b", "DeepSeek-R1-Distill-Qwen 1.5B", Regex("deepseek.*r1.*1\\.5b", RegexOption.IGNORE_CASE), 1.8, 4096,
            "Denkt vor der Antwort (Gedanken einklappbar); gut für Rechenaufgaben, langsamer.",
            "huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-1.5B", thinking = true
        )
    )

    val UNKNOWN = ModelProfile("custom", "Eigenes Modell", Regex(".*"), 0.0, 2048, "Unbekanntes Modell, konservative Einstellungen.", "")

    fun profileFor(fileName: String): ModelProfile = profiles.firstOrNull { it.pattern.containsMatchIn(fileName) } ?: UNKNOWN

    /** MediaPipe LLM bundles end in ".task" (".litertlm" files need the newer LiteRT-LM runtime and are rejected). */
    fun isModelFile(fileName: String): Boolean = fileName.endsWith(".task", true)

    /** KV-cache length baked into converted bundles ("…_ekv4096.task"); the context must not exceed it. */
    fun maxTokensFor(fileName: String): Int {
        val baked = Regex("ekv(\\d{3,5})", RegexOption.IGNORE_CASE).find(fileName)?.groupValues?.get(1)?.toIntOrNull()
        return baked ?: profileFor(fileName).defaultMaxTokens
    }

    /** Context sizes tried in order when the engine refuses the requested one. */
    fun fallbackLadder(requested: Int): List<Int> = (listOf(requested) + listOf(4096, 2048, 1024).filter { it < requested }).distinct()
}

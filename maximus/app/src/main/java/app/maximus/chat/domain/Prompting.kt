package app.maximus.chat.domain

import kotlin.math.ceil

/**
 * Prompt assembly under a token budget.
 *
 * The engine session keeps the KV cache of all earlier turns, so a follow-up question only costs
 * prefill for its own tokens (prefill is O(n) in new tokens; replaying the whole history every turn
 * would make turn k cost O(Σ previous) and grow quadratically over a chat). The history is replayed
 * only when a session is (re)opened: after an app restart, a model switch, or when the context of
 * size N would overflow: used + prompt + reserve > N. The replay is then cut to a fraction of N,
 * newest turns first.
 */
object PromptPlanner {
    /** Share of the context the replayed history may occupy. */
    const val HISTORY_SHARE = 0.4

    /** Heuristic for German/English text with SentencePiece/BPE vocabularies (≈ 3,3 characters per token). */
    fun estimateTokens(text: String): Int = ceil(text.length / 3.3).toInt()

    fun system(focus: Focus): String = Focus.PERSONA + "\n" + focus.instruction

    /** The user turn: tool context first (small models attend best to what directly precedes the question). */
    fun turn(query: String, context: String, personal: String? = null): String {
        val blocks = ArrayList<String>()
        if (!personal.isNullOrBlank()) blocks += "Trainingsdaten des Nutzers (privat, lokal):\n$personal"
        if (context.isNotBlank()) blocks += "Exakte Ergebnisse und Nachschlagewerk der App:\n$context"
        return if (blocks.isEmpty()) query else blocks.joinToString("\n\n") + "\n\nFrage: $query"
    }

    /**
     * Compressed transcript of [history] (oldest first in the output) within [budgetTokens]. Thinking and
     * tool cards are dropped; long answers are clipped, because the model needs the gist, not the full text.
     */
    fun replay(history: List<ChatMessage>, budgetTokens: Int, estimate: (String) -> Int = ::estimateTokens): String {
        val lines = ArrayList<String>()
        var used = 0
        for (m in history.asReversed()) {
            if (m.text.isBlank()) continue
            val line = when (m.role) {
                Role.USER -> "Nutzer: " + ChatTools.clip(m.text.trim(), 500)
                Role.ASSISTANT -> "Maximus: " + ChatTools.clip(m.text.trim(), 700)
            }
            val cost = estimate(line) + 1
            if (used + cost > budgetTokens) {
                // Clip the newest turn that does not fit instead of dropping everything older than it.
                val left = budgetTokens - used
                if (left >= 40) lines += ChatTools.clip(line, (left * 3.0).toInt())
                break
            }
            lines += line
            used += cost
        }
        if (lines.isEmpty()) return ""
        return "Bisheriger Gesprächsverlauf (gekürzt):\n" + lines.asReversed().joinToString("\n")
    }

    /** First prompt of a fresh session: instructions, optional history, then the turn. */
    fun opening(focus: Focus, history: List<ChatMessage>, turn: String, maxTokens: Int, estimate: (String) -> Int = ::estimateTokens): String {
        val replay = replay(history, (maxTokens * HISTORY_SHARE).toInt(), estimate)
        return buildString {
            append(system(focus))
            if (replay.isNotEmpty()) append("\n\n").append(replay)
            append("\n\n").append(turn)
        }
    }

    /** Prompt of a continued session; a focus change is announced in one line instead of a new system prompt. */
    fun followUp(focus: Focus, sessionFocus: Focus?, turn: String): String =
        if (sessionFocus == null || sessionFocus == focus) turn else "[Fokus jetzt: ${focus.label}. ${focus.instruction}]\n\n$turn"

    /** Tokens kept free for the answer. */
    fun reserve(maxTokens: Int, maxReply: Int): Int = minOf(maxReply, maxTokens / 3)

    fun fits(used: Int, prompt: Int, maxTokens: Int, maxReply: Int): Boolean = used + prompt + reserve(maxTokens, maxReply) <= maxTokens
}

/**
 * Turns the raw token stream into what the user sees:
 *  • reasoning between think tags goes to [Snapshot.thinking] (also when the template already opened
 *    the tag and only the closing tag appears),
 *  • end-of-turn markers that leak through some bundles end the answer,
 *  • a partially streamed marker at the end ("<end_of") is hidden instead of flickering,
 *  • a degenerate loop (the same block repeated three times at the end) is detected so generation can
 *    be stopped early, which saves time and battery on small models.
 */
class StreamFilter {
    data class Snapshot(val visible: String, val thinking: String, val ended: Boolean, val looping: Boolean)

    private val raw = StringBuilder()
    private var checkedAt = 0
    private var looping = false

    val rawText: String get() = raw.toString()

    fun push(delta: String): Snapshot {
        raw.append(delta)
        return snapshot()
    }

    fun snapshot(): Snapshot {
        var text = raw.toString()
        var ended = false
        for (m in END_MARKERS) {
            val i = text.indexOf(m)
            if (i >= 0) { text = text.substring(0, i); ended = true }
        }
        var thinking = ""
        val open = text.indexOf(THINK_OPEN)
        val close = text.indexOf(THINK_CLOSE)
        when {
            open >= 0 && close > open -> { thinking = text.substring(open + THINK_OPEN.length, close); text = text.substring(0, open) + text.substring(close + THINK_CLOSE.length) }
            open >= 0 -> { thinking = text.substring(open + THINK_OPEN.length); text = text.substring(0, open) }
            close >= 0 -> { thinking = text.substring(0, close); text = text.substring(close + THINK_CLOSE.length) }
        }
        text = hidePartialMarker(text)
        if (!looping && text.length - checkedAt >= 48) {
            checkedAt = text.length
            looping = isLooping(text)
        }
        return Snapshot(text.trimStart(), hidePartialMarker(thinking).trim(), ended, looping)
    }

    companion object {
        const val THINK_OPEN = "<think>"
        const val THINK_CLOSE = "</think>"
        val END_MARKERS = listOf("<end_of_turn>", "<|im_end|>", "<|endoftext|>", "<|eot_id|>", "<eos>", "<｜end▁of▁sentence｜>")
        private val ALL = END_MARKERS + THINK_OPEN + THINK_CLOSE

        fun hidePartialMarker(s: String): String {
            val lt = s.lastIndexOf('<')
            if (lt < 0 || s.length - lt > 24) return s
            val tail = s.substring(lt)
            return if (ALL.any { it.startsWith(tail) && it != tail }) s.substring(0, lt) else s
        }

        /**
         * True if the text ends with some block of length p (20 ≤ p ≤ 240) repeated three times. Cost
         * O(Σp) ≈ 3·10⁴ character comparisons, run at most every 48 new characters.
         */
        fun isLooping(s: String): Boolean {
            for (p in 20..240) {
                if (3 * p > s.length) return false
                val end = s.length
                val block = s.regionMatches(end - p, s, end - 2 * p, p) && s.regionMatches(end - 2 * p, s, end - 3 * p, p)
                if (block && s.substring(end - p).isNotBlank()) return true
            }
            return false
        }
    }
}

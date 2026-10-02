package app.maximus.chat.domain

/**
 * The local assistant "Maximus". Everything in this package is plain Kotlin without Android or engine
 * types, so routing, tools, prompt planning and stream filtering are unit-tested on the JVM; the
 * on-device language model is reached only through the [LocalLlm] interface.
 */
enum class Role { USER, ASSISTANT }

/** Result of a deterministic in-app tool, shown as a card above the answer. */
data class ToolCard(val tool: String, val title: String, val body: String, val ok: Boolean = true)

/** Timing of one generation. Decode speed excludes the time to the first token (prefill). */
data class GenStats(
    val promptTokens: Int,
    val outputTokens: Int,
    val firstTokenMs: Long,
    val totalMs: Long,
    val backend: String
) {
    val decodeTokensPerSecond: Double
        get() = if (totalMs > firstTokenMs && outputTokens > 1) (outputTokens - 1) * 1000.0 / (totalMs - firstTokenMs) else 0.0
    val prefillTokensPerSecond: Double
        get() = if (firstTokenMs > 0 && promptTokens > 0) promptTokens * 1000.0 / firstTokenMs else 0.0
}

data class ChatMessage(
    val id: Long,
    val role: Role,
    val text: String,
    val tools: List<ToolCard> = emptyList(),
    /** Reasoning of "thinking" models (between think tags); shown collapsed, never replayed. */
    val thinking: String = "",
    val stats: GenStats? = null,
    val focus: Focus? = null,
    /** True when the user stopped the generation or the loop guard cut it off. */
    val stopped: Boolean = false,
    val createdAt: Long = 0
)

data class Conversation(
    val id: Long,
    val title: String,
    val messages: List<ChatMessage>,
    val updatedAt: Long,
    /** Fixed focus chosen by the user; null = detected per message. */
    val focusMode: Focus? = null,
    val pinned: Boolean = false
) {
    companion object {
        /** Title from the first user message: first line, at most 42 characters, cut at a word. */
        fun titleFrom(text: String): String {
            val line = text.trim().lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty().removePrefix("/")
            if (line.length <= 42) return line.ifEmpty { "Neuer Chat" }
            val cut = line.take(42)
            val sp = cut.lastIndexOf(' ')
            return (if (sp > 20) cut.take(sp) else cut).trimEnd(',', '.', ';', ':') + " …"
        }
    }
}

/**
 * Length-prefixed text encoding ("<length>:<content>" per field), so arbitrary text including
 * newlines, colons and digits round-trips without escaping. Nested records are packed fields.
 */
internal object Pack {
    fun pack(fields: List<String>): String = buildString {
        for (f in fields) append(f.length).append(':').append(f)
    }

    fun pack(vararg fields: String): String = pack(fields.toList())

    fun unpack(s: String): List<String> {
        val out = ArrayList<String>()
        var i = 0
        while (i < s.length) {
            val colon = s.indexOf(':', i)
            require(colon > i) { "corrupt record" }
            val n = s.substring(i, colon).toInt()
            require(n >= 0 && colon + 1 + n <= s.length) { "corrupt record" }
            out += s.substring(colon + 1, colon + 1 + n)
            i = colon + 1 + n
        }
        return out
    }
}

/** Storage format of a conversation (one app_meta record per conversation, encrypted by SQLCipher). */
object ChatCodec {
    private const val VERSION = "c1"

    fun encode(c: Conversation): String = Pack.pack(
        VERSION, c.id.toString(), c.title, c.updatedAt.toString(), c.focusMode?.name.orEmpty(), if (c.pinned) "1" else "0",
        Pack.pack(c.messages.map(::encodeMessage))
    )

    /** Returns null for corrupt or unknown records instead of throwing (a damaged chat must not crash the list). */
    fun decode(s: String?): Conversation? = try {
        val f = Pack.unpack(s ?: return null)
        if (f.size < 7 || f[0] != VERSION) null
        else Conversation(
            id = f[1].toLong(), title = f[2], updatedAt = f[3].toLong(),
            focusMode = f[4].takeIf { it.isNotEmpty() }?.let { n -> Focus.entries.firstOrNull { it.name == n } },
            pinned = f[5] == "1",
            messages = Pack.unpack(f[6]).map(::decodeMessage)
        )
    } catch (e: RuntimeException) {
        null
    }

    private fun encodeMessage(m: ChatMessage): String = Pack.pack(
        m.id.toString(), m.role.name, m.text, m.thinking, m.focus?.name.orEmpty(), if (m.stopped) "1" else "0", m.createdAt.toString(),
        Pack.pack(m.tools.map { Pack.pack(it.tool, it.title, it.body, if (it.ok) "1" else "0") }),
        m.stats?.let { Pack.pack(it.promptTokens.toString(), it.outputTokens.toString(), it.firstTokenMs.toString(), it.totalMs.toString(), it.backend) }.orEmpty()
    )

    private fun decodeMessage(s: String): ChatMessage {
        val f = Pack.unpack(s)
        return ChatMessage(
            id = f[0].toLong(), role = Role.valueOf(f[1]), text = f[2], thinking = f[3],
            focus = f[4].takeIf { it.isNotEmpty() }?.let { n -> Focus.entries.firstOrNull { it.name == n } },
            stopped = f[5] == "1", createdAt = f[6].toLong(),
            tools = Pack.unpack(f[7]).map { t -> Pack.unpack(t).let { ToolCard(it[0], it[1], it[2], it[3] == "1") } },
            stats = f[8].takeIf { it.isNotEmpty() }?.let { st ->
                Pack.unpack(st).let { GenStats(it[0].toInt(), it[1].toInt(), it[2].toLong(), it[3].toLong(), it[4]) }
            }
        )
    }
}

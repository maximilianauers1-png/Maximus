package app.maximus.notes.domain

/** Block-level structure of a note; [line] is the 0-based source line (used to toggle checkboxes). */
sealed interface MdBlock {
    val line: Int
    data class Heading(override val line: Int, val level: Int, val text: String) : MdBlock
    data class Bullet(override val line: Int, val depth: Int, val text: String) : MdBlock
    data class Numbered(override val line: Int, val depth: Int, val number: Int, val text: String) : MdBlock
    data class Task(override val line: Int, val depth: Int, val done: Boolean, val text: String) : MdBlock
    data class Quote(override val line: Int, val text: String) : MdBlock
    data class Code(override val line: Int, val text: String) : MdBlock
    data class Rule(override val line: Int) : MdBlock
    data class Paragraph(override val line: Int, val text: String) : MdBlock
    data class Blank(override val line: Int) : MdBlock
}

data class MdSpan(val text: String, val bold: Boolean = false, val italic: Boolean = false, val code: Boolean = false)

/**
 * Deliberately small Markdown dialect: # headings (1–3), "-" or "*" bullets, 1. lists, - [ ] / - [x] tasks,
 * > quotes, ``` fenced code, --- rules; inline **bold**, *italic* / _italic_, `code`.
 * Indentation depth = ⌊leading spaces / 2⌋. Consecutive paragraph lines are joined with a space.
 */
object MarkdownLite {
    private val HEADING = Regex("^(#{1,3})\\s+(.*)$")
    private val TASK = Regex("^(\\s*)[-*]\\s+\\[([ xX])]\\s+(.*)$")
    private val BULLET = Regex("^(\\s*)[-*]\\s+(.*)$")
    private val NUMBERED = Regex("^(\\s*)(\\d{1,9})[.)]\\s+(.*)$")
    private val RULE = Regex("^\\s*(-{3,}|\\*{3,}|_{3,})\\s*$")

    fun parse(source: String): List<MdBlock> {
        val lines = source.replace("\r\n", "\n").split('\n')
        val out = ArrayList<MdBlock>()
        var i = 0
        while (i < lines.size) {
            val raw = lines[i]
            val trimmed = raw.trim()
            when {
                trimmed.startsWith("```") -> {
                    val start = i
                    val body = StringBuilder()
                    i++
                    while (i < lines.size && !lines[i].trim().startsWith("```")) {
                        if (body.isNotEmpty()) body.append('\n')
                        body.append(lines[i]); i++
                    }
                    out += MdBlock.Code(start, body.toString())
                }
                trimmed.isEmpty() -> out += MdBlock.Blank(i)
                RULE.matches(raw) -> out += MdBlock.Rule(i)
                HEADING.matches(trimmed) -> {
                    val m = HEADING.find(trimmed)!!
                    out += MdBlock.Heading(i, m.groupValues[1].length, m.groupValues[2].trim())
                }
                TASK.matches(raw) -> {
                    val m = TASK.find(raw)!!
                    out += MdBlock.Task(i, depth(m.groupValues[1]), m.groupValues[2] != " ", m.groupValues[3])
                }
                BULLET.matches(raw) -> {
                    val m = BULLET.find(raw)!!
                    out += MdBlock.Bullet(i, depth(m.groupValues[1]), m.groupValues[2])
                }
                NUMBERED.matches(raw) -> {
                    val m = NUMBERED.find(raw)!!
                    out += MdBlock.Numbered(i, depth(m.groupValues[1]), m.groupValues[2].toInt(), m.groupValues[3])
                }
                trimmed.startsWith(">") -> out += MdBlock.Quote(i, trimmed.removePrefix(">").trim())
                else -> {
                    val prev = out.lastOrNull()
                    if (prev is MdBlock.Paragraph) out[out.size - 1] = prev.copy(text = prev.text + " " + trimmed)
                    else out += MdBlock.Paragraph(i, trimmed)
                }
            }
            i++
        }
        return out
    }

    private fun depth(indent: String): Int = indent.replace("\t", "  ").length / 2

    /** Inline spans; an unmatched marker is kept as literal text. */
    fun inline(text: String): List<MdSpan> {
        val out = ArrayList<MdSpan>()
        val plain = StringBuilder()
        fun flush() { if (plain.isNotEmpty()) { out += MdSpan(plain.toString()); plain.clear() } }
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '`') {
                val j = text.indexOf('`', i + 1)
                if (j > i + 1) { flush(); out += MdSpan(text.substring(i + 1, j), code = true); i = j + 1; continue }
            }
            if (text.startsWith("**", i)) {
                var j = text.indexOf("**", i + 2)
                // "***" closing a bold span that ends with an italic span: the bold marker is the last two.
                while (j > 0 && j + 2 < text.length && text[j + 2] == '*') j++
                if (j > i + 2) {
                    flush()
                    inline(text.substring(i + 2, j)).forEach { out += it.copy(bold = true) }
                    i = j + 2; continue
                }
            }
            if ((c == '*' || c == '_') && i + 1 < text.length && text[i + 1] != ' ') {
                val j = text.indexOf(c, i + 1)
                if (j > i + 1 && text[j - 1] != ' ') {
                    flush()
                    inline(text.substring(i + 1, j)).forEach { out += it.copy(italic = true) }
                    i = j + 1; continue
                }
            }
            plain.append(c); i++
        }
        flush()
        return out
    }

    /** Flips "[ ]" ↔ "[x]" on source line [line] if that line is a task; otherwise returns the source unchanged. */
    fun toggleTask(source: String, line: Int): String {
        val lines = source.replace("\r\n", "\n").split('\n').toMutableList()
        if (line !in lines.indices) return source
        val m = TASK.find(lines[line]) ?: return source
        val done = m.groupValues[2] != " "
        val idx = lines[line].indexOf('[')
        lines[line] = lines[line].substring(0, idx + 1) + (if (done) " " else "x") + lines[line].substring(idx + 2)
        return lines.joinToString("\n")
    }

    /** Normalised tag list: trimmed, lower-case, leading '#' removed, deduplicated, sorted. */
    fun normaliseTags(input: String): List<String> =
        input.split(',', ';', ' ').map { it.trim().removePrefix("#").lowercase() }.filter { it.isNotEmpty() }.distinct().sorted()
}

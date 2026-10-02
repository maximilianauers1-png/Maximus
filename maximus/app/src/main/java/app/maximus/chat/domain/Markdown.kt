package app.maximus.chat.domain

/**
 * A small Markdown dialect, enough for model answers: headings, paragraphs, bullet and numbered lists,
 * fenced code (an unclosed fence while streaming is treated as code up to the end), quotes, rules,
 * tables and display maths ($$ … $$ or \[ … \]). Parsing is linear in the text length, so re-parsing the
 * growing answer on every UI frame is cheap.
 */
sealed interface MdBlock {
    data class Heading(val level: Int, val spans: List<Span>) : MdBlock
    data class Paragraph(val spans: List<Span>) : MdBlock
    data class ListBlock(val ordered: Boolean, val items: List<ListItem>) : MdBlock
    data class Code(val language: String, val code: String, val closed: Boolean) : MdBlock
    data class Quote(val spans: List<Span>) : MdBlock
    data class Math(val text: String) : MdBlock
    data class Table(val header: List<List<Span>>, val rows: List<List<List<Span>>>) : MdBlock
    data object Rule : MdBlock
}

data class ListItem(val indent: Int, val marker: String, val spans: List<Span>)

enum class SpanStyle { BOLD, ITALIC, CODE }

data class Span(val text: String, val styles: Set<SpanStyle> = emptySet())

object Markdown {
    private val HEADING = Regex("^(#{1,6})\\s+(.*)$")
    private val BULLET = Regex("^(\\s*)([-*+•])\\s+(.*)$")
    private val ORDERED = Regex("^(\\s*)(\\d{1,3})[.)]\\s+(.*)$")
    private val RULE = Regex("^\\s*([-*_])(\\s*\\1){2,}\\s*$")
    private val TABLE_SEP = Regex("^\\s*\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?\\s*$")

    fun parse(text: String): List<MdBlock> {
        val lines = text.replace("\r\n", "\n").split('\n')
        val out = ArrayList<MdBlock>()
        val para = StringBuilder()
        fun flush() {
            if (para.isNotBlank()) out += MdBlock.Paragraph(inline(para.toString().trim()))
            para.clear()
        }
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()
            when {
                trimmed.startsWith("```") -> {
                    flush()
                    val lang = trimmed.removePrefix("```").trim()
                    val code = StringBuilder()
                    var j = i + 1
                    var closed = false
                    while (j < lines.size) {
                        if (lines[j].trim().startsWith("```")) { closed = true; break }
                        if (code.isNotEmpty()) code.append('\n')
                        code.append(lines[j])
                        j++
                    }
                    out += MdBlock.Code(lang, code.toString(), closed)
                    i = if (closed) j + 1 else lines.size
                    continue
                }
                trimmed.startsWith("$$") || trimmed.startsWith("\\[") -> {
                    flush()
                    val closer = if (trimmed.startsWith("$$")) "$$" else "\\]"
                    val first = trimmed.drop(2)
                    if (first.trimEnd().endsWith(closer) && first.isNotBlank()) {
                        out += MdBlock.Math(Latex.toUnicode(first.trimEnd().dropLast(2).trim()))
                        i++
                        continue
                    }
                    val sb = StringBuilder(first)
                    var j = i + 1
                    while (j < lines.size && !lines[j].trim().endsWith(closer)) { sb.append('\n').append(lines[j]); j++ }
                    if (j < lines.size) sb.append('\n').append(lines[j].trim().dropLast(2))
                    out += MdBlock.Math(Latex.toUnicode(sb.toString().trim()))
                    i = j + 1
                    continue
                }
                trimmed.isEmpty() -> flush()
                RULE.matches(trimmed) -> { flush(); out += MdBlock.Rule }
                HEADING.matches(trimmed) -> {
                    flush()
                    val m = HEADING.find(trimmed)!!
                    out += MdBlock.Heading(m.groupValues[1].length, inline(m.groupValues[2].trimEnd('#', ' ')))
                }
                trimmed.startsWith(">") -> {
                    flush()
                    val sb = StringBuilder()
                    var j = i
                    while (j < lines.size && lines[j].trim().startsWith(">")) { sb.append(lines[j].trim().removePrefix(">").trim()).append(' '); j++ }
                    out += MdBlock.Quote(inline(sb.toString().trim()))
                    i = j
                    continue
                }
                trimmed.startsWith("|") && i + 1 < lines.size && TABLE_SEP.matches(lines[i + 1]) -> {
                    flush()
                    val header = cells(trimmed)
                    val rows = ArrayList<List<List<Span>>>()
                    var j = i + 2
                    while (j < lines.size && lines[j].trim().startsWith("|")) { rows += cells(lines[j].trim()); j++ }
                    out += MdBlock.Table(header, rows)
                    i = j
                    continue
                }
                BULLET.matches(line) || ORDERED.matches(line) -> {
                    flush()
                    val ordered = ORDERED.matches(line)
                    val items = ArrayList<ListItem>()
                    var j = i
                    while (j < lines.size) {
                        val l = lines[j]
                        val m = (if (ordered) ORDERED else BULLET).find(l)
                        if (m != null) {
                            val marker = if (ordered) m.groupValues[2] + "." else "•"
                            items += ListItem(m.groupValues[1].length / 2, marker, inline(m.groupValues[3]))
                        } else if (l.isNotBlank() && l.startsWith("  ") && items.isNotEmpty()) {
                            val last = items.removeAt(items.size - 1)
                            items += last.copy(spans = last.spans + Span(" ") + inline(l.trim()))
                        } else break
                        j++
                    }
                    out += MdBlock.ListBlock(ordered, items)
                    i = j
                    continue
                }
                else -> para.append(line).append('\n')
            }
            i++
        }
        flush()
        return out
    }

    private fun cells(row: String): List<List<Span>> =
        row.trim().removePrefix("|").removeSuffix("|").split('|').map { inline(it.trim()) }

    /** Inline markup: **bold**, *italic* / _italic_, `code`, [text](link) → text, $math$ → Unicode. */
    fun inline(s: String): List<Span> {
        val out = ArrayList<Span>()
        val buf = StringBuilder()
        var bold = false
        var italic = false
        fun styles() = buildSet { if (bold) add(SpanStyle.BOLD); if (italic) add(SpanStyle.ITALIC) }
        fun emit() {
            if (buf.isNotEmpty()) {
                val raw = buf.toString()
                val t = if ('\\' in raw) Latex.toUnicode(raw) else Latex.carets(raw)
                if (out.isNotEmpty() && out.last().styles == styles()) out[out.size - 1] = Span(out.last().text + t, styles())
                else out += Span(t, styles())
                buf.clear()
            }
        }
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '`' -> {
                    val end = s.indexOf('`', i + 1)
                    if (end < 0) { buf.append(c); i++; continue }
                    emit()
                    out += Span(s.substring(i + 1, end), setOf(SpanStyle.CODE))
                    i = end + 1
                    continue
                }
                c == '$' && s.indexOf('$', i + 1) > i + 1 -> {
                    val end = s.indexOf('$', i + 1)
                    emit()
                    out += Span(Latex.toUnicode(s.substring(i + 1, end)), styles())
                    i = end + 1
                    continue
                }
                s.startsWith("**", i) || s.startsWith("__", i) -> { emit(); bold = !bold; i += 2; continue }
                (c == '*' || c == '_') && italicDelimiter(s, i) -> { emit(); italic = !italic; i++; continue }
                c == '[' -> {
                    val close = s.indexOf("](", i)
                    val end = if (close > 0) s.indexOf(')', close) else -1
                    if (close > 0 && end > 0) { buf.append(s, i + 1, close); i = end + 1; continue }
                }
            }
            buf.append(c)
            i++
        }
        emit()
        return out
    }

    /** "*" or "_" toggles italics only next to a word character (so 2 * 3 and snake_case stay literal). */
    private fun italicDelimiter(s: String, i: Int): Boolean {
        val before = s.getOrNull(i - 1)
        val after = s.getOrNull(i + 1)
        val opens = after != null && !after.isWhitespace() && (before == null || !before.isLetterOrDigit())
        val closes = before != null && !before.isWhitespace() && (after == null || !after.isLetterOrDigit())
        return opens || closes
    }

    /** Plain text of an answer for copying (markup removed, code kept). */
    fun plain(blocks: List<MdBlock>): String = blocks.joinToString("\n\n") { b ->
        when (b) {
            is MdBlock.Heading -> b.spans.joinToString("") { it.text }
            is MdBlock.Paragraph -> b.spans.joinToString("") { it.text }
            is MdBlock.ListBlock -> b.items.joinToString("\n") { "  ".repeat(it.indent) + it.marker + " " + it.spans.joinToString("") { s -> s.text } }
            is MdBlock.Code -> b.code
            is MdBlock.Quote -> b.spans.joinToString("") { it.text }
            is MdBlock.Math -> b.text
            is MdBlock.Table -> (listOf(b.header) + b.rows).joinToString("\n") { r -> r.joinToString(" | ") { c -> c.joinToString("") { it.text } } }
            MdBlock.Rule -> "———"
        }
    }
}

/**
 * LaTeX → Unicode for the formulas models like to emit ("\frac{\hbar^2}{2m}\nabla^2\psi"). Covers
 * Greek letters, operators, fractions, roots, sub/superscripts with Unicode glyphs where they exist,
 * and drops layout commands. Not a TeX engine — but it turns typical answers into readable maths.
 */
object Latex {
    private val SYMBOLS = linkedMapOf(
        "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "epsilon" to "ε", "varepsilon" to "ε", "zeta" to "ζ",
        "eta" to "η", "theta" to "θ", "vartheta" to "ϑ", "iota" to "ι", "kappa" to "κ", "lambda" to "λ", "mu" to "μ", "nu" to "ν",
        "xi" to "ξ", "pi" to "π", "rho" to "ρ", "sigma" to "σ", "tau" to "τ", "upsilon" to "υ", "phi" to "φ", "varphi" to "φ",
        "chi" to "χ", "psi" to "ψ", "omega" to "ω", "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ", "Xi" to "Ξ",
        "Pi" to "Π", "Sigma" to "Σ", "Phi" to "Φ", "Psi" to "Ψ", "Omega" to "Ω",
        "hbar" to "ħ", "nabla" to "∇", "partial" to "∂", "infty" to "∞", "int" to "∫", "iint" to "∬", "iiint" to "∭", "oint" to "∮",
        "sum" to "Σ", "prod" to "Π", "cdot" to "·", "times" to "×", "div" to "÷", "pm" to "±", "mp" to "∓", "leq" to "≤", "le" to "≤",
        "geq" to "≥", "ge" to "≥", "neq" to "≠", "ne" to "≠", "approx" to "≈", "equiv" to "≡", "sim" to "∼", "propto" to "∝",
        "to" to "→", "rightarrow" to "→", "leftarrow" to "←", "Rightarrow" to "⇒", "Leftarrow" to "⇐", "Leftrightarrow" to "⇔",
        "iff" to "⇔", "implies" to "⇒", "mapsto" to "↦", "in" to "∈", "notin" to "∉", "subset" to "⊂", "subseteq" to "⊆",
        "cup" to "∪", "cap" to "∩", "emptyset" to "∅", "forall" to "∀", "exists" to "∃", "neg" to "¬", "wedge" to "∧", "vee" to "∨",
        "otimes" to "⊗", "oplus" to "⊕", "dagger" to "†", "langle" to "⟨", "rangle" to "⟩", "ell" to "ℓ", "Re" to "Re", "Im" to "Im",
        "degree" to "°", "circ" to "∘", "perp" to "⊥", "parallel" to "∥", "dots" to "…", "ldots" to "…", "cdots" to "⋯",
        "mathbb{R}" to "ℝ", "mathbb{C}" to "ℂ", "mathbb{N}" to "ℕ", "mathbb{Z}" to "ℤ", "mathbb{Q}" to "ℚ",
        "sin" to "sin", "cos" to "cos", "tan" to "tan", "exp" to "exp", "ln" to "ln", "log" to "log", "lim" to "lim",
        "det" to "det", "max" to "max", "min" to "min", "sup" to "sup", "inf" to "inf"
    )
    private const val SUP_SRC = "0123456789+-=()niax"
    private const val SUP_DST = "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾ⁿⁱᵃˣ"
    private const val SUB_SRC = "0123456789+-=()aeoxijkmnpst"
    private const val SUB_DST = "₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎ₐₑₒₓᵢⱼₖₘₙₚₛₜ"

    fun toUnicode(input: String): String {
        if ('\\' !in input && '^' !in input && '_' !in input) return input
        var s = input
        s = s.replace("\\left", "").replace("\\right", "").replace("\\,", " ").replace("\\;", " ").replace("\\!", "").replace("\\quad", "  ")
        s = s.replace("\\(", "").replace("\\)", "")
        // Wrappers whose argument is shown as is.
        s = Regex("\\\\(?:mathrm|text|mathbf|boldsymbol|operatorname|mathit|vec)\\{([^{}]*)}").replace(s) { it.groupValues[1] }
        s = Regex("\\\\mathbb\\{([RCNZQ])}").replace(s) { SYMBOLS["mathbb{${it.groupValues[1]}}"] ?: it.value }
        repeat(3) {
            s = Regex("\\\\[dt]?frac\\{([^{}]*)}\\{([^{}]*)}").replace(s) { m -> frac(m.groupValues[1], m.groupValues[2]) }
            s = Regex("\\\\sqrt\\{([^{}]*)}").replace(s) { m -> "√" + group(m.groupValues[1]) }
        }
        s = Regex("\\\\([A-Za-z]+)").replace(s) { m -> SYMBOLS[m.groupValues[1]] ?: m.groupValues[1] }
        s = Regex("\\^\\{([^{}]*)}|\\^(\\S)").replace(s) { m -> script(m.groupValues[1].ifEmpty { m.groupValues[2] }, SUP_SRC, SUP_DST, "^") }
        s = Regex("_\\{([^{}]*)}|_([A-Za-z0-9])").replace(s) { m -> script(m.groupValues[1].ifEmpty { m.groupValues[2] }, SUB_SRC, SUB_DST, "_") }
        return s.replace("{", "").replace("}", "")
    }

    /** Plain prose: only "mc^2"-style powers become superscripts (underscores stay, file_name is not maths). */
    fun carets(s: String): String =
        if ('^' !in s) s else Regex("(?<=[\\p{L}\\p{N})])\\^(\\{[^{}]*}|[0-9n+\\-]{1,3})").replace(s) { m ->
            script(m.groupValues[1].removePrefix("{").removeSuffix("}"), SUP_SRC, SUP_DST, "^")
        }

    private fun group(x: String) = if (x.length <= 1 || x.all { it.isLetterOrDigit() }) x else "($x)"

    private fun frac(a: String, b: String) = group(a) + "/" + group(b)

    /** Unicode super/subscript if every character has a glyph; otherwise ^(…) / _(…). */
    private fun script(x: String, src: String, dst: String, fallback: String): String =
        if (x.isNotEmpty() && x.all { it in src }) x.map { dst[src.indexOf(it)] }.joinToString("")
        else if (x.length == 1) "$fallback$x" else "$fallback($x)"
}

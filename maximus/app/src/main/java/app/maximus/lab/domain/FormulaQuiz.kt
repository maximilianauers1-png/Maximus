package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.random.Random

/**
 * Multiple-choice questions about formulas, generated from the compendium: every usable formula yields
 * two questions, "which formula is X?" and "what does this formula describe?". With ~700 formulas this
 * gives well over a thousand formula questions without a single hand-written duplicate.
 *
 * Distractors: for the formula direction one "near miss" is built by mutating the correct expression
 * (exponent, sign, factor 2, sin/cos, inequality direction) — this trains precision instead of
 * recognition — and the rest come from the same chapter, then the same topic, so the options always
 * look alike and cannot be ruled out by subject alone.
 */
object FormulaQuiz {
    data class Card(val chapter: Chapter, val section: String, val formula: Formula, val index: Int) {
        val id: String get() = "${chapter.key}#$index"
    }

    /** An equation or relation of reasonable length; pure word lists and tables are skipped. */
    fun usable(f: Formula): Boolean =
        f.expr.length in 4..140 && f.expr.any { it in "=≈∝<>≤≥⇔⇒→" } && f.name.length in 3..70

    val cards: List<Card> by lazy {
        Compendium.all.flatMap { ch ->
            var i = 0
            ch.sections.flatMap { s -> s.formulas.map { f -> Card(ch, s.title, f, i++) } }.filter { usable(it.formula) }
        }
    }

    fun cardsFor(topic: Topic?, course: String? = null): List<Card> =
        cards.filter { (topic == null || it.chapter.topic == topic) && (course == null || it.chapter.course == course) }

    private val MUTATIONS: List<Pair<String, String>> = listOf(
        "²" to "³", "³" to "²", "⁴" to "²", "½" to "¼", "2π" to "π", "4π" to "2π", "/2" to "/4", "/(2" to "/(4",
        " + " to " − ", " − " to " + ", "sin" to "cos", "cos" to "sin", "≤" to "≥", "≥" to "≤", "<" to ">", "exp(−" to "exp(",
        "e^{−" to "e^{", "√" to "", "ħ" to "h", "k_B" to "R", "−1" to "+1", "+1" to "−1", "(1 −" to "(1 +", "(1 +" to "(1 −"
    )

    /** First mutation that changes the right-hand side (the left side stays, otherwise the option is trivially wrong). */
    fun nearMiss(expr: String, r: Random): String? {
        val eq = expr.indexOf('=').takeIf { it > 0 } ?: 0
        val left = expr.substring(0, eq)
        val right = expr.substring(eq)
        for ((from, to) in MUTATIONS.shuffled(r)) {
            val at = right.indexOf(from)
            if (at < 0) continue
            val mutated = left + right.substring(0, at) + to + right.substring(at + from.length)
            if (mutated != expr && mutated.isNotBlank()) return mutated
        }
        return null
    }

    private fun distractors(card: Card, r: Random, key: (Card) -> String, n: Int, exclude: Set<String>): List<String> {
        val taken = exclude.toMutableSet()
        val out = ArrayList<String>()
        val pools = listOf(
            cards.filter { it.chapter.key == card.chapter.key },
            cards.filter { it.chapter.topic == card.chapter.topic },
            cards
        )
        for (pool in pools) {
            for (c in pool.shuffled(r)) {
                if (out.size == n) return out
                val k = key(c)
                if (c.id != card.id && k !in taken) { taken += k; out += k }
            }
        }
        return out
    }

    private fun explanation(card: Card): String = buildString {
        append("${card.formula.name}:  ${card.formula.expr}")
        if (card.formula.note.isNotBlank()) append("\n").append(card.formula.note)
        append("\n\nAus „${card.chapter.title} – ${card.section}“.")
    }

    /** "Which formula belongs to <name>?" — options are expressions. */
    fun formulaQuestion(card: Card, r: Random): Question {
        val correct = card.formula.expr
        val wrong = ArrayList<String>()
        val miss = nearMiss(correct, r)
        if (miss != null) wrong += miss
        wrong += distractors(card, r, { it.formula.expr }, 3 - wrong.size, setOf(correct) + wrong)
        val options = listOf(correct) + wrong
        return Question(
            "fq:${card.id}:f", card.chapter.topic, card.chapter.level,
            "Welche Formel gilt für „${card.formula.name}“?  (${card.chapter.title})",
            explanation(card) + if (miss != null) "\n\nFalle: „$miss“ unterscheidet sich nur in einem Detail (Exponent, Vorzeichen, Faktor oder Funktion)." else "",
            options, 0, chapterKey = card.chapter.key
        )
    }

    /** "What does <expression> describe?" — options are formula names. */
    fun nameQuestion(card: Card, r: Random): Question {
        val correct = card.formula.name
        val wrong = distractors(card, r, { it.formula.name }, 3, setOf(correct))
        return Question(
            "fq:${card.id}:n", card.chapter.topic, card.chapter.level,
            "Was beschreibt diese Formel?  (${card.chapter.title})\n\n${card.formula.expr}",
            explanation(card), listOf(correct) + wrong, 0, chapterKey = card.chapter.key
        )
    }

    fun question(card: Card, r: Random): Question = if (r.nextBoolean()) formulaQuestion(card, r) else nameQuestion(card, r)
}

/**
 * Calculation tasks as multiple choice: the exact result plus three typical slips (factor 10, 2, π, …),
 * and a hint with the matching formulas of the chapter and the constants, so nothing has to be looked up.
 */
object NumericChoice {
    private val FACTORS = listOf(10.0, 0.1, 2.0, 0.5, PI, 1 / PI, 4.0, 0.25, 1000.0, 0.001)

    fun options(answer: Double, r: Random): List<String> {
        val correct = Fmt.num(answer, 3)
        val out = linkedSetOf(correct)
        for (f in FACTORS.shuffled(r)) {
            if (out.size == 4) break
            val v = answer * f
            if (v.isFinite()) out += Fmt.num(v, 3)
        }
        // Degenerate answers (0, or values whose multiples round alike): shift additively instead.
        var k = 1.0
        while (out.size < 4) { out += Fmt.num(answer + k, 3); k += 1.0 }
        return out.toList()
    }

    fun toChoice(q: Question, r: Random): Question {
        if (q.isChoice) return q
        val unit = if (q.unit.isNotBlank()) " ${q.unit}" else ""
        return q.copy(options = options(q.answer, r).map { it + unit }, correctIndex = 0, hint = q.hint.ifEmpty { hint(q) })
    }

    /** Formulas of the question's chapter that share the most words with the prompt, plus common constants. */
    fun hint(q: Question): String {
        val ch = q.chapterKey?.let { Compendium.byKey[it] }
        val words = q.prompt.lowercase().split(Regex("[^\\p{L}]+")).filter { it.length > 3 }.toSet()
        val formulas = ch?.formulas.orEmpty()
            .sortedByDescending { f -> (f.name + " " + f.expr).lowercase().split(Regex("[^\\p{L}]+")).count { it in words } }
            .take(3)
        return buildString {
            if (formulas.isNotEmpty()) {
                append("Formeln:\n")
                formulas.forEach { append("• ${it.name}: ${it.expr}\n") }
            }
            append("Konstanten: ").append(CONSTANTS)
        }
    }

    const val CONSTANTS = "c = 2,998·10⁸ m/s · h = 6,626·10⁻³⁴ J s · ħ = 1,055·10⁻³⁴ J s · e = 1,602·10⁻¹⁹ C · " +
        "k_B = 1,381·10⁻²³ J/K · N_A = 6,022·10²³ /mol · R = 8,314 J/(mol K) · ε₀ = 8,854·10⁻¹² F/m · μ₀ = 1,257·10⁻⁶ N/A² · " +
        "mₑ = 9,109·10⁻³¹ kg · mₚ = 1,673·10⁻²⁷ kg · G = 6,674·10⁻¹¹ m³/(kg s²) · σ = 5,670·10⁻⁸ W/(m² K⁴) · g = 9,81 m/s²"
}

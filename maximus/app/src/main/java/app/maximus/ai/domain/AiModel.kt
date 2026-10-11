package app.maximus.ai.domain

import app.maximus.lab.domain.Formula
import app.maximus.lab.domain.Section

/**
 * The AI engineering academy ("AI-Ingenieurwesen"): thirteen lessons (Lektionen) modelled on the IBM AI
 * Engineering Professional Certificate, each split into bites (Happen) with explanation, code and a quiz,
 * and closed by a tournament. All content is German, like the physics lab.
 *
 * A bite reuses the lab's text markup ([Section]): blank-line paragraphs, "• " bullets, "> " key
 * statements and "$ " code lines.
 */
data class Bite(
    val key: String,
    /** 1 = Grundlagen, 2 = Vertiefung, 3 = Fortgeschritten. */
    val level: Int,
    val title: String,
    val summary: String,
    val sections: List<Section>,
    /** Learning goals ("Nach diesem Happen kannst du …"); may be empty for older bites. */
    val goals: List<String> = emptyList(),
    /** Lesson number 1…13 and global bite number, assigned by [AiCourse]. */
    val lesson: Int = 0,
    val number: Int = 0
) {
    val formulas: List<Formula> get() = sections.flatMap { it.formulas }
    val fullTitle: String get() = "Happen $number · $title"
}

/** The lesson's tournament champion, a knight to beat in the final joust. */
data class Champion(val name: String, val epithet: String, val taunt: String)

data class Lesson(
    val number: Int,
    /** German title of the lesson. */
    val title: String,
    /** Title of the IBM course it follows. */
    val ibmTitle: String,
    val hours: Int,
    val blurb: String,
    val champion: Champion,
    val biteKeys: List<String>
)

/** Kind of a quiz question; code questions show a snippet and/or code options in monospace. */
enum class QKind(val label: String) {
    CONCEPT("Verständnis"), CODE("Code"), CALC("Rechnen"), TERM("Begriff")
}

/**
 * A multiple-choice question of the academy. [code] is an optional snippet shown under the prompt;
 * [monoOptions] renders the answer options as code.
 */
data class AiQuestion(
    val id: String,
    val bite: String,
    val difficulty: Int,
    val kind: QKind,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val code: String = "",
    val monoOptions: Boolean = false
) {
    fun check(index: Int) = index == correctIndex
}

internal fun goals(vararg g: String): List<String> = g.toList()

internal fun bite(key: String, level: Int, title: String, summary: String, vararg sections: Section) =
    Bite(key, level, title, summary, sections.toList())

internal fun bite(key: String, level: Int, title: String, summary: String, goals: List<String>, vararg sections: Section) =
    Bite(key, level, title, summary, sections.toList(), goals)

/**
 * Small DSL for hand-written questions. `on(key)` selects the bite; ids are "prefix-bite-n", so they stay
 * stable as long as questions are only appended to a bite.
 */
internal class QuizBuilder(private val prefix: String) {
    val out = ArrayList<AiQuestion>()
    private var bite = ""
    private val counters = HashMap<String, Int>()

    fun on(key: String) { bite = key }

    private fun add(diff: Int, kind: QKind, prompt: String, code: String, mono: Boolean, correct: String, wrong: Array<out String>, why: String) {
        require(bite.isNotEmpty()) { "on(bite) missing before \"$prompt\"" }
        val n = (counters[bite] ?: 0) + 1
        counters[bite] = n
        out += AiQuestion("$prefix-$bite-$n", bite, diff, kind, prompt, listOf(correct) + wrong, 0, why, code.trimIndent(), mono)
    }

    /** Concept question. */
    fun mc(diff: Int, prompt: String, correct: String, vararg wrong: String, why: String) =
        add(diff, QKind.CONCEPT, prompt, "", false, correct, wrong, why)

    /** Question about a code snippet; the options are prose. */
    fun code(diff: Int, prompt: String, code: String, correct: String, vararg wrong: String, why: String) =
        add(diff, QKind.CODE, prompt, code, false, correct, wrong, why)

    /** Question about a code snippet whose options are themselves code (outputs, shapes, lines). */
    fun codeOut(diff: Int, prompt: String, code: String, correct: String, vararg wrong: String, why: String) =
        add(diff, QKind.CODE, prompt, code, true, correct, wrong, why)

    /** "Which line does it?" — options are code lines, no snippet. */
    fun line(diff: Int, prompt: String, correct: String, vararg wrong: String, why: String) =
        add(diff, QKind.CODE, prompt, "", true, correct, wrong, why)
}

internal fun quiz(prefix: String, block: QuizBuilder.() -> Unit): List<AiQuestion> = QuizBuilder(prefix).apply(block).out

package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.random.Random

/**
 * A question is either multiple choice ([options] with [correctIndex]) or numeric ([answer] in [unit],
 * accepted within the relative [tolerance]). [solution] is the worked solution shown after answering.
 */
data class Question(
    val id: String,
    val topic: Topic,
    val difficulty: Int,
    val prompt: String,
    val solution: String,
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1,
    val answer: Double = Double.NaN,
    val unit: String = "",
    val tolerance: Double = 0.02,
    val chapterKey: String? = null,
    /** Formulas and constants that may be used (shown on request before answering). */
    val hint: String = ""
) {
    val isChoice: Boolean get() = options.isNotEmpty()

    fun checkChoice(index: Int): Boolean = isChoice && index == correctIndex

    /** Numeric check with relative tolerance; values within 1e-9 of zero compare absolutely. */
    fun checkNumber(input: String): Boolean {
        val v = parseAnswer(input) ?: return false
        if (abs(answer) < 1e-12) return abs(v) < 1e-9
        return abs(v - answer) <= tolerance * abs(answer)
    }

    companion object {
        /** Accepts "1,6e-19", "1.6E-19", "1,6·10^-19", "1,6×10⁻¹⁹", "−3,2". */
        fun parseAnswer(input: String): Double? {
            var s = input.trim().replace(" ", "").replace('−', '-').replace(',', '.')
            val sup = "⁰¹²³⁴⁵⁶⁷⁸⁹"
            s = s.map { c -> val i = sup.indexOf(c); if (i >= 0) ('0' + i) else if (c == '⁻') '-' else c }.joinToString("")
            val m = Regex("^([-+]?[0-9]*\\.?[0-9]+)(?:[*·×x]10\\^?([-+]?[0-9]+))?$").find(s)
            if (m != null) {
                val mant = m.groupValues[1].toDoubleOrNull() ?: return null
                val exp = m.groupValues[2]
                return if (exp.isEmpty()) mant else mant * Math.pow(10.0, exp.toDouble())
            }
            return s.toDoubleOrNull()
        }
    }
}

/**
 * What a round contains. Understanding comes first: concept questions and formula multiple choice;
 * calculations are optional and then come with the formulas and constants as a hint.
 */
enum class QuizMode(val label: String, val description: String) {
    CONCEPT("Verständnis", "Konzeptfragen und Formel-Multiple-Choice"),
    MIXED("Gemischt", "Dazu Rechenaufgaben als Multiple Choice, mit Formelhilfe"),
    CALC("Rechnen", "Rechenaufgaben mit freier Eingabe, Formeln und Konstanten als Hilfe")
}

/** Generates a fresh numeric question from a random source. */
fun interface QuestionGenerator { fun make(r: Random): Question }

object QuizEngine {
    val generators: Map<Topic, List<QuestionGenerator>> by lazy { QuizGenerators.all }
    val bank: List<Question> by lazy { QuizBank.all + QuizConcepts.all + QuizConcepts2.all + QuizAi.all + AiGlossary.questions }

    /** Chapter of each generator, read from a sample question (a generator always stays within one chapter). */
    private val generatorChapter: Map<QuestionGenerator, String?> by lazy {
        generators.values.flatten().associateWith { it.make(Random(0)).chapterKey }
    }

    /** Number of available questions (bank + generators) for a course; used to hide empty course filters. */
    fun courseSize(topic: Topic, course: String): Int =
        bank.count { it.topic == topic && Compendium.byKey[it.chapterKey]?.course == course } +
            generators[topic].orEmpty().count { Compendium.byKey[generatorChapter[it]]?.course == course }

    /** Adaptive difficulty: mastery 0..1 → difficulty 1..3. */
    fun difficultyFor(mastery: Double): Int = when { mastery < 0.4 -> 1; mastery < 0.75 -> 2; else -> 3 }

    /**
     * A session of [count] questions for one topic (or all topics when null). Difficulty is centred on
     * [difficulty] (±1 by random tie-breaking). With a [course] (e.g. "Analysis II") only that lecture.
     *
     * CONCEPT: hand-written concept questions and generated formula questions, alternating (concept first).
     * MIXED: as CONCEPT, every third question a calculation as multiple choice with formula hint.
     * CALC: calculations with free input (hint available), concept questions in between.
     */
    fun session(topic: Topic?, difficulty: Int, count: Int, r: Random, course: String? = null, mode: QuizMode = QuizMode.CONCEPT): List<Question> {
        val topics = topic?.let { listOf(it) } ?: Topic.entries
        fun inCourse(chapterKey: String?) = course == null || Compendium.byKey[chapterKey]?.course == course
        // Sort keys are drawn once per element: a random value inside a comparator would break the sort contract.
        fun <T> byDifficulty(items: List<T>, level: (T) -> Int): List<T> =
            items.map { it to abs(level(it) - difficulty) + r.nextDouble() }.sortedBy { it.second }.map { it.first }
        val concept = byDifficulty(bank.filter { it.topic in topics && inCourse(it.chapterKey) }) { it.difficulty }.toMutableList()
        val formulas = byDifficulty(FormulaQuiz.cards.filter { it.chapter.topic in topics && inCourse(it.chapter.key) }) { it.chapter.level }
            .take(count * 4).shuffled(r).toMutableList()
        val gens = topics.flatMap { t -> generators[t].orEmpty() }.filter { course == null || inCourse(generatorChapter[it]) }.shuffled(r)
        var gi = 0
        fun calc(): Question? = if (gens.isEmpty()) null else gens[gi++ % gens.size].make(r).let { it.copy(hint = NumericChoice.hint(it)) }
        val out = ArrayList<Question>()
        var guard = 0
        while (out.size < count && guard++ < count * 6) {
            val slot = out.size
            val q: Question? = when {
                mode == QuizMode.CALC && slot % 3 != 2 -> calc()
                mode == QuizMode.MIXED && slot % 3 == 2 -> calc()?.let { NumericChoice.toChoice(it, r) }
                slot % 2 == 0 && concept.isNotEmpty() -> concept.removeAt(0)
                formulas.isNotEmpty() -> FormulaQuiz.question(formulas.removeAt(0), r)
                concept.isNotEmpty() -> concept.removeAt(0)
                else -> calc()?.let { NumericChoice.toChoice(it, r) }
            }
            if (q != null && out.none { it.id == q.id }) out += q
        }
        return out.map { shuffleOptions(it, r) }
    }

    /** Daily challenge: the same five questions for everyone on a given day — concept and formula questions, rotating topics. */
    fun daily(epochDay: Long): List<Question> {
        val r = Random(epochDay * 7919 + 17)
        val topics = Topic.entries.shuffled(r).take(5)
        return topics.mapIndexed { i, t ->
            val bankT = bank.filter { it.topic == t }
            val cardsT = FormulaQuiz.cardsFor(t)
            val q = if ((i % 2 == 1 || bankT.isEmpty()) && cardsT.isNotEmpty()) FormulaQuiz.question(cardsT[r.nextInt(cardsT.size)], r)
            else bankT[r.nextInt(bankT.size)]
            shuffleOptions(q, r).copy(id = "daily-$epochDay-$i-${q.id}")
        }
    }

    private fun shuffleOptions(q: Question, r: Random): Question {
        if (!q.isChoice) return q
        val order = q.options.indices.shuffled(r)
        return q.copy(options = order.map { q.options[it] }, correctIndex = order.indexOf(q.correctIndex))
    }
}

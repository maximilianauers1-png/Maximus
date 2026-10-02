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
    val chapterKey: String? = null
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

/** Generates a fresh numeric question from a random source. */
fun interface QuestionGenerator { fun make(r: Random): Question }

object QuizEngine {
    val generators: Map<Topic, List<QuestionGenerator>> by lazy { QuizGenerators.all }
    val bank: List<Question> by lazy { QuizBank.all }

    /** Adaptive difficulty: mastery 0..1 → difficulty 1..3. */
    fun difficultyFor(mastery: Double): Int = when { mastery < 0.4 -> 1; mastery < 0.75 -> 2; else -> 3 }

    /**
     * A session of [count] questions for one topic (or all topics when null). Roughly half the
     * questions are generated calculations; difficulty is centred on [difficulty] but varies by ±1.
     */
    fun session(topic: Topic?, difficulty: Int, count: Int, r: Random): List<Question> {
        val topics = topic?.let { listOf(it) } ?: Topic.entries
        val out = ArrayList<Question>()
        val pool = bank.filter { it.topic in topics }.shuffled(r)
        val gens = topics.flatMap { t -> generators[t].orEmpty() }.shuffled(r)
        var gi = 0
        val wanted = pool.sortedBy { abs(it.difficulty - difficulty) + r.nextDouble() }.toMutableList()
        while (out.size < count && (wanted.isNotEmpty() || gens.isNotEmpty())) {
            val takeNumeric = gens.isNotEmpty() && (out.size % 2 == 1 || wanted.isEmpty())
            if (takeNumeric) {
                out += gens[gi % gens.size].make(r)
                gi++
            } else if (wanted.isNotEmpty()) {
                out += wanted.removeAt(0)
            }
        }
        return out.take(count).map { shuffleOptions(it, r) }
    }

    /** Daily challenge: the same five questions for everyone on a given day, one per rotating topic. */
    fun daily(epochDay: Long): List<Question> {
        val r = Random(epochDay * 7919 + 17)
        val topics = Topic.entries.shuffled(r).take(5)
        return topics.mapIndexed { i, t ->
            val gens = generators[t].orEmpty()
            val q = if (i % 2 == 0 && gens.isNotEmpty()) gens[r.nextInt(gens.size)].make(r)
            else bank.filter { it.topic == t }.let { b -> if (b.isEmpty()) gens[r.nextInt(gens.size)].make(r) else b[r.nextInt(b.size)] }
            shuffleOptions(q, r).copy(id = "daily-$epochDay-$i-${q.id}")
        }
    }

    private fun shuffleOptions(q: Question, r: Random): Question {
        if (!q.isChoice) return q
        val order = q.options.indices.shuffled(r)
        return q.copy(options = order.map { q.options[it] }, correctIndex = order.indexOf(q.correctIndex))
    }
}

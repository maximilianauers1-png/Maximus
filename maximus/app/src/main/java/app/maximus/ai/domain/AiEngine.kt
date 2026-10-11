package app.maximus.ai.domain

import app.maximus.lab.domain.NumericChoice
import kotlin.math.abs
import kotlin.random.Random

/** A calculation with an exact result; the engine turns it into multiple choice. */
data class NumericTask(
    val id: String,
    val bite: String,
    val difficulty: Int,
    val prompt: String,
    val answer: Double,
    val unit: String,
    val solution: String
)

fun interface TaskGenerator { fun make(r: Random): NumericTask }

/** One round of a tournament: name, target difficulty and number of questions. */
data class JoustRound(val name: String, val difficulty: Int, val questions: Int)

/**
 * Builds every question set of the academy: bite quizzes, free training, the daily trial, weak-spot
 * training and tournaments. Pure and seeded, so it is testable and a round can be replayed.
 */
object AiQuizEngine {
    /** Calculation as multiple choice: exact value plus three typical slips (×10, ×2, …). */
    fun toChoice(t: NumericTask, r: Random): AiQuestion {
        val unit = if (t.unit.isNotBlank()) " ${t.unit}" else ""
        return AiQuestion(
            t.id + "-" + r.nextInt(1_000_000), t.bite, t.difficulty, QKind.CALC, t.prompt,
            NumericChoice.options(t.answer, r).map { it + unit }, 0, t.solution, monoOptions = false
        )
    }

    fun shuffle(q: AiQuestion, r: Random): AiQuestion {
        val order = q.options.indices.shuffled(r)
        return q.copy(options = order.map { q.options[it] }, correctIndex = order.indexOf(q.correctIndex))
    }

    private fun tasksOf(bites: Set<String>) = AiCourse.tasks.filter { AiCourse.taskBite[it] in bites }

    /** Sort by distance to the target difficulty with random tie-breaking (keys drawn once per element). */
    private fun <T> near(items: List<T>, difficulty: Int, r: Random, level: (T) -> Int): List<T> =
        items.map { it to abs(level(it) - difficulty) + r.nextDouble() }.sortedBy { it.second }.map { it.first }

    /**
     * The quiz of one bite: its own hand-written questions (code questions are always included), glossary
     * questions only to fill up, and one or two calculations when the bite has generators. At most [max].
     */
    fun biteQuiz(biteKey: String, r: Random, max: Int = 10): List<AiQuestion> {
        val own = AiCourse.questionsByBite[biteKey].orEmpty()
        val core = own.filter { it.kind != QKind.TERM }.shuffled(r)
        val terms = own.filter { it.kind == QKind.TERM }.shuffled(r)
        val code = core.filter { it.kind == QKind.CODE }
        val concept = core.filter { it.kind != QKind.CODE }
        val calc = tasksOf(setOf(biteKey)).shuffled(r).take(2).map { toChoice(it.make(r), r) }
        val picked = ArrayList<AiQuestion>()
        picked += code.take(max / 2)
        picked += concept.take(max - picked.size - calc.size.coerceAtMost(1))
        picked += calc.take(max - picked.size)
        picked += terms.take((max - picked.size).coerceAtLeast(0))
        if (picked.size < max) picked += (code.drop(max / 2) + concept).filter { q -> picked.none { it.id == q.id } }.take(max - picked.size)
        return picked.shuffled(r).map { shuffle(it, r) }
    }

    /**
     * Free training: [lesson] null = whole course; [kind] null = every kind. Difficulty centred on
     * [difficulty]; calculations are mixed in when the kind allows.
     */
    fun session(lesson: Int?, kind: QKind?, difficulty: Int, count: Int, r: Random): List<AiQuestion> {
        val bites = (if (lesson == null) AiCourse.keys else AiCourse.bitesOf(lesson).map { it.key }).toSet()
        val bank = AiCourse.questions.filter { it.bite in bites && (kind == null || it.kind == kind) }
        val pool = near(bank, difficulty, r) { it.difficulty }.take(count * 3).shuffled(r).toMutableList()
        val gens = if (kind == null || kind == QKind.CALC) tasksOf(bites).shuffled(r) else emptyList()
        val out = ArrayList<AiQuestion>()
        var gi = 0
        while (out.size < count && (pool.isNotEmpty() || gens.isNotEmpty())) {
            val useCalc = gens.isNotEmpty() && (kind == QKind.CALC || pool.isEmpty() || out.size % 4 == 3)
            out += if (useCalc) toChoice(gens[gi++ % gens.size].make(r), r) else pool.removeAt(0)
        }
        return out.map { shuffle(it, r) }
    }

    /** Daily trial: five questions from five different lessons, the same for everyone on that day. */
    fun daily(epochDay: Long): List<AiQuestion> {
        val r = Random(epochDay * 6007 + 31)
        return AiCourse.lessons.shuffled(r).take(5).mapIndexed { i, l ->
            val keys = l.biteKeys.toSet()
            val bank = AiCourse.questions.filter { it.bite in keys && it.kind != QKind.TERM }
            shuffle(bank[r.nextInt(bank.size)], r).let { it.copy(id = "daily-$epochDay-$i-${it.id}") }
        }
    }

    /** Questions answered wrongly before (still in [wrong]), newest knowledge gaps first. */
    fun weakSpots(wrong: Collection<String>, count: Int, r: Random): List<AiQuestion> =
        wrong.mapNotNull { AiCourse.questionById[it] }.shuffled(r).take(count).map { shuffle(it, r) }

    val ROUNDS = listOf(
        JoustRound("Knappentjost", 1, 5),
        JoustRound("Rittertjost", 2, 5),
        JoustRound("Königstjost", 3, 6)
    )

    /**
     * A tournament of one lesson: three rounds of rising difficulty, no question twice. The last round
     * prefers code and calculation questions – the champion fights with the hardest weapons.
     */
    fun tournament(lesson: Int, r: Random): List<List<AiQuestion>> {
        val keys = AiCourse.bitesOf(lesson).map { it.key }.toSet()
        val bank = AiCourse.questions.filter { it.bite in keys && it.kind != QKind.TERM }.toMutableList()
        val gens = tasksOf(keys).shuffled(r)
        var gi = 0
        return ROUNDS.mapIndexed { ri, round ->
            val sorted = near(bank, round.difficulty, r) { it.difficulty + if (ri == 2 && it.kind == QKind.CODE) -1 else 0 }
            val take = sorted.take(round.questions).toMutableList()
            bank.removeAll(take.toSet())
            if (ri == 2 && gens.isNotEmpty() && take.size >= 2) take[take.size - 1] = toChoice(gens[gi++ % gens.size].make(r), r)
            while (take.size < round.questions && gens.isNotEmpty()) take += toChoice(gens[gi++ % gens.size].make(r), r)
            take.shuffled(r).map { shuffle(it, r) }
        }
    }

    /** Number of distinct questions a lesson offers (bank, without glossary) – shown on the lesson card. */
    fun lessonSize(lesson: Int): Int {
        val keys = AiCourse.bitesOf(lesson).map { it.key }.toSet()
        return AiCourse.questions.count { it.bite in keys }
    }
}

/** Score of a tournament: points per hit grow with the lance streak (combo) and the round. */
object Joust {
    const val SHIELDS = 3

    fun points(round: Int, combo: Int): Int = (10 + 5 * round) * (1 + minOf(combo, 5))

    /** Trophy: 3 = Gold (no shield lost), 2 = Silber, 1 = Bronze, 0 = defeated. */
    fun tier(won: Boolean, shieldsLeft: Int): Int = if (!won) 0 else shieldsLeft.coerceIn(1, 3)

    fun tierName(tier: Int): String = when (tier) { 3 -> "Goldener Pokal"; 2 -> "Silberner Pokal"; 1 -> "Bronzener Pokal"; else -> "Kein Pokal" }
}

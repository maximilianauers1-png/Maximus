package app.maximus.lab.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabGameTest {
    @Test
    fun compendiumIsConsistent() {
        assertEquals(Compendium.all.size, Compendium.byKey.size)
        assertTrue(Compendium.all.size >= 40)
        for (t in Topic.entries) assertTrue("$t has chapters", Compendium.forTopic(t).size >= 5)
        for (ch in Compendium.all) {
            assertTrue(ch.key, ch.sections.isNotEmpty())
            ch.calculatorKeys.forEach { assertTrue("${ch.key} → $it", it in Calculators.byKey) }
            ch.sections.forEach { s -> assertTrue("${ch.key}/${s.title}", s.body.isNotBlank()) }
        }
        assertEquals(Compendium.flashcards.size, Compendium.flashcardById.size)
        assertTrue(Compendium.flashcards.size >= 250)
    }

    @Test
    fun questionBankIsWellFormed() {
        assertTrue(QuizEngine.bank.size >= 90)
        assertEquals(QuizEngine.bank.size, QuizEngine.bank.map { it.id }.toSet().size)
        for (q in QuizEngine.bank) {
            assertTrue(q.id, q.correctIndex in q.options.indices)
            assertEquals(q.id, q.options.size, q.options.toSet().size)
            assertTrue(q.id, q.chapterKey in Compendium.byKey)
            assertTrue(q.id, q.difficulty in 1..3)
        }
    }

    @Test
    fun generatorsProduceSolvableQuestions() {
        for ((topic, gens) in QuizEngine.generators) {
            assertTrue("$topic generators", gens.size >= 6)
            for (g in gens) repeat(60) { seed ->
                val q = g.make(Random(seed))
                assertTrue("${q.prompt} → ${q.answer}", q.answer.isFinite())
                assertTrue(q.prompt, q.checkNumber(Fmt.num(q.answer, 6)))
                assertTrue(q.prompt, q.checkNumber(q.answer.toString()))
                assertFalse(q.prompt, q.checkNumber((q.answer * 1.5 + 7).toString()))
                assertTrue(q.chapterKey in Compendium.byKey)
            }
        }
    }

    @Test
    fun answerParsing() {
        assertEquals(1.6e-19, Question.parseAnswer("1,6e-19")!!, 1e-30)
        assertEquals(1.6e-19, Question.parseAnswer("1,6·10^-19")!!, 1e-30)
        assertEquals(1.6e-19, Question.parseAnswer("1,6×10⁻¹⁹")!!, 1e-30)
        assertEquals(-3.2, Question.parseAnswer("−3,2")!!, 1e-12)
        assertEquals(1.234e5, Question.parseAnswer(Fmt.num(123400.0, 4).replace("·", "·"))!!, 1.0)
    }

    @Test
    fun sessionsAndDaily() {
        for (t in Topic.entries) {
            // Default: understanding first — only multiple choice, concept and formula questions alternate.
            val s = QuizEngine.session(t, 2, 10, Random(3))
            assertEquals(10, s.size)
            assertTrue(s.all { it.topic == t && it.isChoice })
            assertTrue(t.name, s.any { it.id.startsWith("fq:") } && s.any { !it.id.startsWith("fq:") })
            // Mixed: calculations appear as multiple choice with formulas and constants as hint.
            val mixed = QuizEngine.session(t, 2, 9, Random(4), mode = QuizMode.MIXED)
            assertTrue(mixed.all { it.isChoice })
            assertTrue(t.name, mixed.any { it.hint.contains("Konstanten") })
            // Calculation mode keeps free input, always with a hint.
            val calc = QuizEngine.session(t, 2, 9, Random(5), mode = QuizMode.CALC)
            assertTrue(calc.any { !it.isChoice })
            assertTrue(calc.filter { !it.isChoice }.all { it.hint.isNotEmpty() })
        }
        assertEquals(QuizEngine.daily(20000).map { it.prompt }, QuizEngine.daily(20000).map { it.prompt })
        assertEquals(5, QuizEngine.daily(20001).size)
    }

    @Test
    fun levelCurveIsConsistent() {
        assertEquals(1, LabRules.level(0)); assertEquals(2, LabRules.level(100)); assertEquals(2, LabRules.level(299)); assertEquals(3, LabRules.level(300))
        for (xp in 0L..20000L step 37) {
            val l = LabRules.level(xp)
            assertTrue(LabRules.xpForLevel(l) <= xp && xp < LabRules.xpForLevel(l + 1))
        }
    }

    @Test
    fun streaksXpAndBadges() {
        val q = QuizEngine.bank.first()
        var p = LabProgress()
        val (p1, ev1) = LabRules.answer(p, q, true, 100)
        assertEquals(1, p1.streak)
        assertTrue(ev1.any { it is LabEvent.BadgeUnlocked && it.badge.key == "first" })
        p = LabRules.answer(p1, q, true, 101).first
        assertEquals(2, p.streak)
        p = LabRules.answer(p, q, false, 103).first
        assertEquals(1, p.streak)
        assertEquals(2, p.bestStreak)
        assertEquals(3, p.answered); assertEquals(2, p.correct)
        // Daily only counts once per day.
        val d1 = LabRules.dailyFinished(p, 5, 103).first
        val d2 = LabRules.dailyFinished(d1, 5, 103).first
        assertEquals(d1.xp, d2.xp)
    }

    @Test
    fun leitnerScheduling() {
        val id = Compendium.flashcards.first().id
        var p = LabProgress()
        assertTrue(LabRules.dueCards(p, 50).any { it.id == id })
        p = LabRules.reviewCard(p, id, true, 50).first
        assertEquals(CardState(1, 51), p.cards[id])
        p = LabRules.reviewCard(p, id, true, 51).first
        assertEquals(CardState(2, 53), p.cards[id])
        assertFalse(LabRules.dueCards(p, 52).any { it.id == id })
        p = LabRules.reviewCard(p, id, false, 53).first
        assertEquals(CardState(1, 54), p.cards[id])
    }

    @Test
    fun progressRoundTrip() {
        var p = LabProgress()
        p = LabRules.answer(p, QuizEngine.bank[3], true, 10).first
        p = LabRules.chapterRead(p, "th_laws", 10).first
        p = LabRules.calculatorUsed(p, "planck", 11).first
        p = LabRules.reviewCard(p, Compendium.flashcards[5].id, true, 11).first
        val decoded = LabRules.decode(LabRules.encode(p))
        assertEquals(p, decoded)
        assertEquals(LabProgress(), LabRules.decode(null))
        assertEquals(LabProgress(), LabRules.decode("garbage\nmore=garbage"))
    }
}

class MathWorkbenchTest {
    @Test
    fun plotterAndAnalysis() {
        val e = MathWorkbench.compile("x^3 - 3x", setOf("x")).getOrThrow()
        val a = MathWorkbench.analyze(e, -3.0, 3.0)
        assertEquals(3, a.roots.size)
        assertEquals(kotlin.math.sqrt(3.0), a.roots.maxOrNull()!!, 1e-8)
        assertEquals(2, a.extrema.size)
        assertEquals(0.0, a.integral!!, 1e-8)
        assertTrue(MathWorkbench.compile("x + q", setOf("x")).isFailure)
        val tan = MathWorkbench.sample(MathWorkbench.compile("tan(x)", setOf("x")).getOrThrow(), -1.5, 1.5)
        val r = MathWorkbench.robustRange(tan.ys)!!
        assertTrue(r.second < 100)
    }

    @Test
    fun matrixAndDataParsing() {
        val m = MathWorkbench.parseMatrix("1 2\n3 4").getOrThrow()
        assertEquals(-2.0, m.determinant(), 1e-12)
        assertTrue(MathWorkbench.parseMatrix("1 2\n3").isFailure)
        val (all, pairs) = MathWorkbench.parseData("1 2\n2 4,1\n3 5,9\n4 8")
        assertEquals(8, all.size)
        assertEquals(4, pairs!!.size)
        assertEquals(null, MathWorkbench.parseData("1 2 3\n4 5").second)
    }

    @Test
    fun odeSecondOrder() {
        val e = MathWorkbench.compile("-y", setOf("t", "y", "v")).getOrThrow()
        val sol = MathWorkbench.solveOde(e, 2, 1.0, 0.0, 0.0, Math.PI, 2000)
        assertEquals(-1.0, sol.y.last()[0], 1e-8)
    }
}

class FormulaQuizTest {
    @Test
    fun everyFormulaCardYieldsWellFormedQuestions() {
        assertTrue(FormulaQuiz.cards.size > 400)
        for (t in Topic.entries) assertTrue(t.name, FormulaQuiz.cardsFor(t).size >= 20)
        val r = Random(7)
        for (card in FormulaQuiz.cards) {
            for (q in listOf(FormulaQuiz.formulaQuestion(card, r), FormulaQuiz.nameQuestion(card, r))) {
                assertEquals(q.prompt, 4, q.options.size)
                assertEquals(q.prompt, 4, q.options.toSet().size)
                assertEquals(0, q.correctIndex)
                assertTrue(q.solution.contains(card.formula.expr))
            }
        }
    }

    @Test
    fun nearMissChangesOnlyTheRightHandSide() {
        val r = Random(1)
        val m = FormulaQuiz.nearMiss("E = mc²", r)
        assertEquals("E = mc³", m)
        assertEquals(null, FormulaQuiz.nearMiss("a = b", r))
    }

    @Test
    fun numericChoicesAreDistinct() {
        for (v in listOf(0.0, 1.0, 3.7e-19, 42.0, -5.0)) {
            val o = NumericChoice.options(v, Random(2))
            assertEquals(4, o.toSet().size)
            assertEquals(Fmt.num(v, 3), o[0])
        }
    }

    @Test
    fun conceptQuestionsAreWellFormed() {
        assertTrue(QuizConcepts.all.size >= 90)
        for (q in QuizConcepts.all) {
            assertTrue(q.prompt, q.chapterKey in Compendium.byKey)
            assertEquals(q.prompt, q.options.size, q.options.toSet().size)
            assertTrue(q.prompt, q.solution.length > 40)
        }
        assertEquals(QuizEngine.bank.size, QuizEngine.bank.map { it.id }.toSet().size)
    }
}

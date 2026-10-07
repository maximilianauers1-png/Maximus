package app.maximus.lab.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiLabTest {

    // ---------------- Curriculum ----------------

    @Test
    fun everyAiChapterIsOnThePathExactlyOnce() {
        val ai = Compendium.forTopic(Topic.AI)
        assertEquals(AiCurriculum.keys.size, AiCurriculum.keys.toSet().size)
        assertTrue("path keys exist", AiCurriculum.keys.all { it in Compendium.byKey })
        assertTrue("no AI chapter off the path", ai.all { it.key in AiCurriculum.keys })
        assertTrue(ai.size >= 60)
        assertEquals(ai.map { it.key }, AiCurriculum.keys)
        assertTrue(ai.first().title.startsWith("Happen 1 · "))
        assertEquals("AI von Null", Compendium.courses(Topic.AI).first())
        assertEquals(1, AiCurriculum.bite("ai0_what"))
        assertEquals("ai0_data", AiCurriculum.next(setOf("ai0_what")))
    }

    @Test
    fun aiChaptersAreSubstantial() {
        for (ch in Compendium.forTopic(Topic.AI)) {
            assertTrue(ch.key, ch.sections.size >= 2)
            assertTrue(ch.key, ch.sections.sumOf { it.body.length } >= 600)
            for (s in ch.sections) for (block in s.body.split(Regex("\\n\\s*\\n"))) {
                val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                // A code block must not be mixed with prose, or the renderer would glue it into a paragraph.
                if (lines.any { it.startsWith("$") }) assertTrue("${ch.key}/${s.title}: $block", lines.all { it.startsWith("$") })
            }
        }
    }

    // ---------------- Questions ----------------

    @Test
    fun aiQuestionBankIsLargeAndWellFormed() {
        val ai = QuizEngine.bank.filter { it.topic == Topic.AI }
        assertTrue("AI bank size ${ai.size}", ai.size >= 500)
        assertTrue(QuizAi.all.size >= 150)
        assertEquals(ai.size, ai.map { it.id }.toSet().size)
        for (q in QuizAi.all + AiGlossary.questions) {
            assertTrue(q.id, q.isChoice && q.options.size == 4)
            assertEquals(q.id, 4, q.options.toSet().size)
            assertTrue(q.id, q.chapterKey in Compendium.byKey)
            assertTrue(q.id, q.solution.isNotBlank())
        }
        assertTrue((QuizEngine.generators[Topic.AI]?.size ?: 0) >= 60)
    }

    @Test
    fun everyPathChapterHasQuestions() {
        val withQuestions = QuizEngine.bank.filter { it.topic == Topic.AI }.mapNotNull { it.chapterKey }.toSet()
        val missing = AiCurriculum.keys.filter { it !in withQuestions }
        assertTrue("chapters without questions: $missing", missing.isEmpty())
    }

    @Test
    fun glossaryTermsAreUniqueAndLinked() {
        val terms = AiGlossary.terms
        assertTrue(terms.size >= 180)
        assertEquals(terms.size, terms.map { it.term }.toSet().size)
        assertTrue(terms.all { it.chapterKey in Compendium.byKey })
    }

    @Test
    fun aiSessionsMixEverything() {
        val s = QuizEngine.session(Topic.AI, 1, 12, Random(3), course = "ML: Die sieben Techniken")
        assertEquals(12, s.size)
        assertTrue(s.all { Compendium.byKey[it.chapterKey]?.course == "ML: Die sieben Techniken" })
    }

    // ---------------- Games ----------------

    @Test
    fun techniqueDetectiveCoversAllTechniques() {
        assertTrue(TechniqueDetective.cases.size >= 30)
        assertEquals(MlTechnique.entries.toSet(), TechniqueDetective.cases.map { it.answer }.toSet())
        assertEquals(10, TechniqueDetective.round(5).size)
        assertEquals(100, TechniqueDetective.score(List(10) { true }))
        assertEquals(0, TechniqueDetective.score(List(10) { false }))
    }

    @Test
    fun everyGradientLevelIsSolvable() {
        val etas = (1..200).map { it * 0.01 }
        for (level in GradientGame.levels) {
            val betas = if (level.allowMomentum) listOf(0.0, 0.5, 0.7, 0.8, 0.9) else listOf(0.0)
            val solved = etas.any { e -> betas.any { b -> GradientGame.score(level, GradientGame.run(level.landscape, e, b, level.steps)) == 100 } }
            assertTrue(level.landscape.title, solved)
            // A far too large learning rate must not win (except on the flat plain, whose lesson is "take bigger steps").
            if (level.landscape.title != "Flache Ebene")
                assertTrue(level.landscape.title, GradientGame.score(level, GradientGame.run(level.landscape, 5.0, 0.0, level.steps)) < 100)
        }
        // The double well: plain descent from the right gets stuck in the local minimum.
        val dw = GradientGame.levels[3]
        assertTrue(GradientGame.score(dw, GradientGame.run(dw.landscape, 0.01, 0.0, dw.steps)) < 100)
    }

    @Test
    fun perceptronLearnsSeparableButNotXor() {
        val ds = PerceptronGame.separable(7)
        var p = Perceptron(0.0, 0.0, 0.0)
        repeat(200) { p = PerceptronGame.epoch(p, ds, 0.1) }
        assertEquals(1.0, p.accuracy(ds), 1e-12)
        val x = PerceptronGame.xor(7)
        var q = Perceptron(0.0, 0.0, 0.0)
        repeat(200) { q = PerceptronGame.epoch(q, x, 0.1) }
        assertTrue(q.accuracy(x) < 0.9)
    }

    @Test
    fun kMeansLloydConvergesAndScores() {
        val pts = KMeansGame.blobs(11)
        val start = pts.take(3)
        val l0 = KMeansGame.assign(pts, start)
        val i0 = KMeansGame.inertia(pts, l0, start)
        val (l, c) = KMeansGame.lloyd(pts, start)
        val i1 = KMeansGame.inertia(pts, l, c)
        assertTrue(i1 <= i0 + 1e-12)
        val best = KMeansGame.bestInertia(pts, 3)
        assertTrue(best <= i1 + 1e-12)
        assertEquals(100, KMeansGame.score(best, best))
        assertTrue(KMeansGame.score(2 * best, best) in 45..55)
    }

    @Test
    fun overfittingHasAnInteriorOptimum() {
        val d = OverfitGame.data(3)
        val best = OverfitGame.bestDegree(d)
        assertTrue("best degree $best", best in 2..9)
        val f0 = OverfitGame.fit(d, 0); val f12 = OverfitGame.fit(d, 12); val fb = OverfitGame.fit(d, best)
        assertTrue(f12.trainMse < f0.trainMse)
        assertTrue(fb.valMse < f0.valMse && fb.valMse <= f12.valMse)
        assertEquals(100, OverfitGame.score(d, best))
        // Exact solve on a tiny system.
        val x = OverfitGame.solve(arrayOf(doubleArrayOf(2.0, 1.0), doubleArrayOf(1.0, 3.0)), doubleArrayOf(3.0, 5.0))
        assertEquals(0.8, x[0], 1e-12); assertEquals(1.4, x[1], 1e-12)
    }

    @Test
    fun thresholdMissionsAreFeasible() {
        val s = ThresholdGame.scores(5)
        for (m in ThresholdMission.entries) {
            assertTrue(m.name, ThresholdGame.bestObjective(s, m) > 0)
            val bestT = ThresholdGame.grid.maxBy { ThresholdGame.objective(m, ThresholdGame.confusion(s, it)) ?: -1.0 }
            assertEquals(100, ThresholdGame.score(s, m, bestT))
        }
        val low = ThresholdGame.confusion(s, 0.0)
        assertEquals(1.0, low.recall, 1e-12)
        val c = Confusion(8, 2, 85, 5)
        assertEquals(0.8, c.precision, 1e-12); assertEquals(8.0 / 13, c.recall, 1e-12)
    }

    @Test
    fun treeGainsMatchHandCalculation() {
        val all = TreeGame.rows
        val h = TreeGame.entropy(all.map { it.second })
        assertTrue(h > 0.9 && h <= 1.0)
        val g = TreeGame.gains(all, TreeGame.features.indices.toList())
        assertTrue(g.values.all { it >= -1e-12 && it <= h + 1e-12 })
        val bestFeature = g.maxBy { it.value }.key
        assertEquals(100, TreeGame.score(all, TreeGame.features.indices.toList(), bestFeature))
        assertEquals(0.0, TreeGame.entropy(listOf(true, true, true)), 1e-12)
        assertEquals(1.0, TreeGame.entropy(listOf(true, false)), 1e-12)
    }

    @Test
    fun qLearningFindsTheOptimalPath() {
        val w = GridWorld()
        val learner = QLearner(w, 0.5, 0.95, 0.2, seed = 1)
        repeat(400) { learner.episode() }
        val g = learner.greedyReturn()
        assertNotNull(g)
        assertEquals(QLearner.optimalReturn(w), g!!, 1e-9)
        assertEquals(100, QLearner.score(learner))
        // Untrained agent: no reliable path.
        assertTrue(QLearner.score(QLearner(w, 0.5, 0.95, 0.2, seed = 2)) < 100)
    }

    @Test
    fun glossaryDuelRoundsAreValid() {
        val round = GlossaryDuel.round(9)
        assertEquals(12, round.size)
        for (c in round) {
            assertEquals(4, c.options.toSet().size)
            assertEquals(c.term.definition, c.options[c.correct])
        }
        assertEquals(12, GlossaryDuel.points(true, 0))
        assertEquals(8, GlossaryDuel.points(true, 5000))
        assertEquals(0, GlossaryDuel.points(false, 100))
    }

    // ---------------- Progress ----------------

    @Test
    fun gameProgressIsRewardedAndPersisted() {
        val (p1, ev1) = LabRules.gameFinished(LabProgress(), AiGame.KMEANS, 80, 100)
        assertEquals(80, p1.games["KMEANS"])
        assertEquals(5 + 16 + 25L, p1.xp)
        assertTrue(ev1.any { it is LabEvent.BadgeUnlocked && it.badge.key == "aiPlay" })
        val (p2, _) = LabRules.gameFinished(p1, AiGame.KMEANS, 50, 100)
        assertEquals(80, p2.games["KMEANS"])
        assertEquals(p1.xp + 15, p2.xp)
        val back = LabRules.decode(LabRules.encode(p2))
        assertEquals(p2.games, back.games)
        assertFalse(LabRules.decode("game.X=abc").games.containsKey("X"))
    }
}

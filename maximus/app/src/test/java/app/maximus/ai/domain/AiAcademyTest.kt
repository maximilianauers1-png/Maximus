package app.maximus.ai.domain

import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.QuizEngine
import app.maximus.lab.domain.Topic
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAcademyTest {

    // ---------------- Curriculum ----------------

    @Test
    fun thirteenLessonsLikeTheIbmCertificate() {
        assertEquals(13, AiCourse.lessons.size)
        assertEquals((1..13).toList(), AiCourse.lessons.map { it.number })
        assertEquals("Machine Learning with Python", AiCourse.lesson(1).ibmTitle)
        assertEquals("Project: Generative AI Applications with RAG and LangChain", AiCourse.lesson(13).ibmTitle)
        assertEquals(13, AiCourse.lessons.map { it.champion.name }.toSet().size)
    }

    @Test
    fun everyListedBiteExistsExactlyOnce() {
        val listed = AiCourse.lessons.flatMap { it.biteKeys }
        assertEquals(listed.size, listed.toSet().size)
        val missing = listed.filter { it !in AiCourse.byKey }
        assertTrue("missing bites: $missing", missing.isEmpty())
        assertEquals(listed, AiCourse.keys)
        assertEquals((1..listed.size).toList(), AiCourse.bites.map { it.number })
        assertTrue("bites: ${AiCourse.bites.size}", AiCourse.bites.size >= 125)
        for (l in AiCourse.lessons) assertTrue("lesson ${l.number}", l.biteKeys.size >= 5)
        assertEquals("ai0_what", AiCourse.bites.first().key)
        assertEquals("ai0_data", AiCourse.next(setOf("ai0_what"))?.key)
        assertEquals(1, AiCourse.lessonOf("ai0_what"))
        assertEquals(13, AiCourse.lessonOf("gen_eval"))
    }

    @Test
    fun aiIsGoneFromThePhysicsLab() {
        assertTrue(Topic.entries.none { it.name == "AI" })
        assertTrue(Compendium.all.none { it.key in AiCourse.byKey })
        val labIds = QuizEngine.bank.map { it.id }.toSet()
        assertTrue(AiCourse.questions.none { it.id in labIds })
    }

    @Test
    fun bitesAreSubstantialAndCodeBlocksArePure() {
        for (b in AiCourse.bites) {
            assertTrue(b.key, b.sections.size >= 2)
            assertTrue(b.key, b.sections.sumOf { it.body.length } >= 600)
            for (s in b.sections) for (block in s.body.split(Regex("\\n\\s*\\n"))) {
                val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                // A code block must not be mixed with prose, or the renderer would glue it into a paragraph.
                if (lines.any { it.startsWith("$") }) assertTrue("${b.key}/${s.title}: $block", lines.all { it.startsWith("$") })
            }
        }
    }

    // ---------------- Questions ----------------

    @Test
    fun questionBankIsHugeAndWellFormed() {
        val qs = AiCourse.questions
        assertTrue("bank size ${qs.size}", qs.size >= 1400)
        assertEquals(qs.size, qs.map { it.id }.toSet().size)
        for (q in qs) {
            assertEquals(q.id, 4, q.options.size)
            assertEquals(q.id, 4, q.options.toSet().size)
            assertEquals(q.id, 0, q.correctIndex)
            assertTrue(q.id, q.bite in AiCourse.byKey)
            assertTrue(q.id, q.explanation.isNotBlank())
            assertTrue(q.id, q.difficulty in 1..3)
        }
        assertTrue("code questions ${qs.count { it.kind == QKind.CODE }}", qs.count { it.kind == QKind.CODE } >= 350)
        assertTrue(AiCourse.tasks.size >= 60)
    }

    @Test
    fun everyBiteHasItsOwnQuizWithCode() {
        val thin = AiCourse.bites.filter { b -> AiCourse.questionsByBite[b.key].orEmpty().count { it.kind != QKind.TERM } < 6 }.map { it.key }
        assertTrue("bites with fewer than 6 own questions: $thin", thin.isEmpty())
        val noCode = AiCourse.bites.filter { b -> AiCourse.questionsByBite[b.key].orEmpty().none { it.kind == QKind.CODE } }.map { it.key }
        assertTrue("bites without code questions: $noCode", noCode.isEmpty())
        for (b in AiCourse.bites) {
            val quiz = AiQuizEngine.biteQuiz(b.key, Random(b.number.toLong()))
            assertTrue(b.key, quiz.size in 6..10)
            assertEquals(b.key, quiz.size, quiz.map { it.id }.toSet().size)
            assertTrue(b.key, quiz.all { it.bite == b.key && it.options[it.correctIndex].isNotEmpty() })
        }
    }

    @Test
    fun generatorsStayInTheirBite() {
        for (t in AiCourse.tasks) {
            val a = t.make(Random(1)); val b = t.make(Random(2))
            assertEquals(a.bite, b.bite)
            assertTrue(a.bite, a.bite in AiCourse.byKey)
            assertTrue(a.prompt, a.answer.isFinite())
            val q = AiQuizEngine.toChoice(a, Random(3))
            assertEquals(4, q.options.toSet().size)
        }
    }

    @Test
    fun glossaryTermsAreUniqueAndLinked() {
        val terms = AiGlossary.terms
        assertTrue(terms.size >= 180)
        assertEquals(terms.size, terms.map { it.term }.toSet().size)
        assertTrue(terms.all { it.chapterKey in AiCourse.byKey })
    }

    @Test
    fun sessionsDailyAndWeakSpots() {
        val s = AiQuizEngine.session(9, null, 2, 12, Random(3))
        assertEquals(12, s.size)
        assertTrue(s.all { AiCourse.lessonOf(it.bite) == 9 })
        val code = AiQuizEngine.session(null, QKind.CODE, 2, 10, Random(4))
        assertEquals(10, code.size)
        assertTrue(code.all { it.kind == QKind.CODE })
        val calc = AiQuizEngine.session(1, QKind.CALC, 2, 5, Random(5))
        assertEquals(5, calc.size)
        val d1 = AiQuizEngine.daily(20000); val d2 = AiQuizEngine.daily(20000)
        assertEquals(5, d1.size)
        assertEquals(d1, d2)
        assertEquals(5, d1.map { AiCourse.lessonOf(it.bite) }.toSet().size)
        val ids = AiCourse.questions.take(7).map { it.id }
        assertEquals(5, AiQuizEngine.weakSpots(ids, 5, Random(1)).size)
    }

    @Test
    fun everyLessonHasAFullTournament() {
        for (l in AiCourse.lessons) {
            val t = AiQuizEngine.tournament(l.number, Random(l.number.toLong()))
            assertEquals(3, t.size)
            t.forEachIndexed { i, round -> assertEquals("lesson ${l.number} round $i", AiQuizEngine.ROUNDS[i].questions, round.size) }
            val all = t.flatten()
            assertEquals(all.size, all.map { it.id }.toSet().size)
            assertTrue(all.all { AiCourse.lessonOf(it.bite) == l.number })
        }
        assertEquals(3, Joust.tier(true, 3)); assertEquals(1, Joust.tier(true, 1)); assertEquals(0, Joust.tier(false, 2))
        assertTrue(Joust.points(2, 3) > Joust.points(0, 0))
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
    fun everyRankNameIsUnique() {
        assertEquals(60, AiRules.RANKS.size)
        val names = (1..120).map { AiRules.rank(it) }
        assertEquals(names.size, names.toSet().size)
        assertEquals("Lehrbub der Lochkarte", AiRules.rank(1))
        assertTrue(AiRules.rank(61).endsWith(" I"))
        assertEquals(1, AiRules.level(0)); assertEquals(2, AiRules.level(100)); assertEquals(3, AiRules.level(300))
    }

    @Test
    fun answersStarsTrophiesAndDailyAreRewarded() {
        val q = AiCourse.questions.first { it.kind == QKind.CODE && it.difficulty == 2 }
        val (p1, _) = AiRules.answer(AiProgress(), q, true, 100)
        assertEquals(AiRules.xpFor(q, 1).toLong(), p1.xp)
        assertEquals(28 * 11 / 10, AiRules.xpFor(q, 1))
        val (p2, _) = AiRules.answer(p1, q, false, 100)
        assertTrue(q.id in p2.wrong)
        val (p3, _) = AiRules.answer(p2, q, true, 101)
        assertFalse(q.id in p3.wrong)
        assertEquals(2, p3.streak)
        assertEquals(3, p3.lesson(AiCourse.lessonOf(q.bite)).answered)

        assertEquals(3, AiRules.stars(8, 8)); assertEquals(2, AiRules.stars(7, 8)); assertEquals(1, AiRules.stars(5, 8)); assertEquals(0, AiRules.stars(4, 8))
        val (s1, ev) = AiRules.biteQuizFinished(AiProgress(), "dl_keras", 5, 8, 7)
        assertEquals(1, s1.stars["dl_keras"]); assertEquals(20L, s1.xp)
        assertTrue(ev.any { it is AiEvent.Stars })
        val (s2, _) = AiRules.biteQuizFinished(s1, "dl_keras", 8, 8, 7)
        assertEquals(3, s2.stars["dl_keras"]); assertEquals(60L, s2.xp); assertEquals(1, s2.perfectQuizzes)
        val (s3, _) = AiRules.biteQuizFinished(s2, "dl_keras", 2, 8, 7)
        assertEquals(s2.xp, s3.xp)

        val (t1, _) = AiRules.tournamentFinished(AiProgress(), 4, 1, 10, 9)
        assertEquals(10 + 20 + 100L, t1.xp)
        assertTrue("champ4" in t1.badges)
        val (t2, _) = AiRules.tournamentFinished(t1, 4, 3, 16, 9)
        assertEquals(t1.xp + 10 + 32 + 150, t2.xp)
        assertTrue("gold1" in t2.badges)

        val (d1, _) = AiRules.dailyFinished(AiProgress(), 4, 50)
        assertEquals(70L, d1.xp)
        assertEquals(d1, AiRules.dailyFinished(d1, 5, 50).first)
    }

    @Test
    fun progressRoundTripsAndImportsFromTheLab() {
        var p = AiProgress()
        p = AiRules.biteRead(p, "ai0_what", 10).first
        p = AiRules.biteQuizFinished(p, "ai0_what", 9, 10, 10).first
        p = AiRules.tournamentFinished(p, 2, 2, 12, 10).first
        p = AiRules.gameFinished(p, AiGame.KMEANS, 80, 10).first
        p = AiRules.answer(p, AiCourse.questions[3], false, 10).first
        val back = AiRules.decode(AiRules.encode(p))
        assertEquals(p, back)
        assertEquals(AiProgress(), AiRules.decode(null))
        assertFalse(AiRules.decode("game.X=abc").games.containsKey("X"))

        val lab = "v=1\nxp=900\nread=th_basics,ai0_what,ai0_data,dl_keras\ngame.KMEANS=70\ngame.FOO=3\n"
        val imported = AiRules.importFromLab(lab)
        assertEquals(setOf("ai0_what", "ai0_data", "dl_keras"), imported.readBites)
        assertEquals(mapOf("KMEANS" to 70), imported.games)
        assertEquals(3 * 15 + 25L, imported.xp)
        assertTrue("play1" in imported.badges)
        assertEquals(AiProgress(), AiRules.importFromLab(null))
    }

    @Test
    fun gameProgressIsRewarded() {
        val (p1, ev1) = AiRules.gameFinished(AiProgress(), AiGame.KMEANS, 80, 100)
        assertEquals(80, p1.games["KMEANS"])
        assertEquals(5 + 16 + 25L, p1.xp)
        assertTrue(ev1.any { it is AiEvent.BadgeUnlocked && it.badge.key == "play1" })
        val (p2, _) = AiRules.gameFinished(p1, AiGame.KMEANS, 50, 100)
        assertEquals(80, p2.games["KMEANS"])
        assertEquals(p1.xp + 15, p2.xp)
    }
}

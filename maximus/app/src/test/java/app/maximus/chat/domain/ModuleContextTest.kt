package app.maximus.chat.domain

import app.maximus.dnd.domain.CharacterBuild
import app.maximus.dnd.domain.CharacterBuilder
import app.maximus.dnd.domain.ClassLevel
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.FormulaQuiz
import app.maximus.lab.domain.QuizEngine
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.NutritionTargets
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleContextTest {
    @Test
    fun quizContextCarriesAnswerSolutionAndFormulas() {
        val q = FormulaQuiz.formulaQuestion(FormulaQuiz.cards.first { it.chapter.key == "th_cycles" }, Random(1))
        val ctx = ModuleContext.quiz(q, q.options[1], correct = false)
        assertTrue(ctx.contains("Richtige Antwort: ${q.options[0]}"))
        assertTrue(ctx.contains("(falsch)"))
        assertTrue(ctx.contains("Formeln aus"))
        assertTrue(ModuleContext.quizQuestion(q, false).startsWith("Ich habe diese Frage falsch"))
        val numeric = QuizEngine.generators.values.first().first().make(Random(2))
        assertTrue(ModuleContext.quiz(numeric, "42", false).contains("Richtiges Ergebnis"))
    }

    @Test
    fun chapterContextIsShort() {
        for (ch in Compendium.all) assertTrue(ch.key, PromptPlanner.estimateTokens(ModuleContext.chapter(ch)) < 600)
    }

    @Test
    fun strongmanContextGroupsSetsPerDayAndExercise() {
        assertNull(ModuleContext.strongman(emptyList()))
        val sets = listOf(
            ModuleContext.LoggedSet(20000, "Kreuzheben", 220.0, 3, 8.0),
            ModuleContext.LoggedSet(20000, "Kreuzheben", 230.0, 1, null),
            ModuleContext.LoggedSet(20003, "Log", 120.0, 5, 7.5)
        )
        val s = ModuleContext.strongman(sets)!!
        assertTrue(s, s.contains("Kreuzheben 220×3 @8, 230×1"))
        assertTrue(s.indexOf("Kreuzheben") < s.indexOf("Log"))
    }

    @Test
    fun nutritionAndDndContexts() {
        val p = NutritionProfile()
        val t = NutritionTargets.derive(p, 20000, null)
        val n = ModuleContext.nutrition(p, t, listOf(ModuleContext.DayLog(19995, 100.4, 3100.0, 210.0)), 20000)
        assertTrue(n, n.contains("Protein") && n.contains("100,4 kg"))
        val sheet = CharacterBuilder.build(CharacterBuild(name = "Vex", classes = listOf(ClassLevel("warlock", 3), ClassLevel("sorcerer", 2))))
        val d = ModuleContext.dnd(sheet)
        assertTrue(d, d.contains("Vex") && d.contains("Gesamtstufe 5"))
        assertTrue(PromptPlanner.estimateTokens(d) < 500)
    }

    @Test
    fun focusDetectsNutrition() {
        assertEquals(Focus.NUTRITION, FocusDetector.detect("Wie viel Protein und Kalorien brauche ich in der Diät?"))
    }
}

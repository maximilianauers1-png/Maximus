package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabMechanicsTest {
    private fun out(key: String, label: String, values: Map<String, Double> = emptyMap()): Double {
        val c = Calculators.byKey.getValue(key)
        return c.run(c.defaults() + values).outputs.first { it.label.startsWith(label) }.value
    }

    @Test
    fun mercuryPerihelionAdvanceIs43ArcsecondsPerCentury() {
        assertEquals(43.0, out("mech_kepler", "ART-Periheldrehung pro Jahrhundert"), 0.3)
        assertEquals(87.97, out("mech_kepler", "Umlaufzeit"), 0.05)
    }

    @Test
    fun gpsClocksGainAbout38MicrosecondsPerDay() {
        val total = out("mech_gr", "Orbit vs. Oberfläche")
        assertEquals(38.5, total, 1.0)
        assertEquals(45.7, out("mech_gr", "davon ART"), 1.0)
        assertEquals(-7.2, out("mech_gr", "davon SRT"), 0.5)
    }

    @Test
    fun projectileInVacuumMatchesTheParabolaAndDragShortensIt() {
        val vac = MechanicsPhysics.projectile(20.0, 45.0, 0.0, 9.81).last()[1]
        assertEquals(20.0 * 20.0 / 9.81, vac, 1e-3)
        val drag = MechanicsPhysics.projectile(20.0, 45.0, 0.02, 9.81).last()[1]
        assertTrue(drag < vac)
    }

    @Test
    fun coupledPendulaHaveTheAnalyticNormalModes() {
        val w1 = out("mech_coupled", "ω₁"); val w2 = out("mech_coupled", "ω₂")
        assertEquals(sqrt(9.81), w1, 1e-6)
        assertEquals(sqrt(9.81 + 1.0), w2, 1e-6)
    }

    @Test
    fun specialRelativityIsConsistent() {
        val g = out("mech_sr", "Lorentz-Faktor", mapOf("beta" to 0.6))
        assertEquals(1.25, g, 1e-12)
        assertEquals(0.5 * kotlin.math.ln(4.0), out("mech_sr", "Rapidität", mapOf("beta" to 0.6)), 1e-12)
        assertTrue(MechanicsPhysics.harmonicEnergyDrift(1.0, 2000) < 1e-6)
    }

    @Test
    fun mechanicsAndCourseGeneratorsAgreeWithNumerics() {
        // Spot checks of closed forms used in generated solutions against direct quadrature.
        assertEquals(sqrt(PI / 2), Calculus.integrate({ exp(-2 * it * it) }, -10.0, 10.0), 1e-8)
        assertEquals(PI / 2, Calculus.integrate({ 1 / (it * it + 4) }, -1e4, 1e4), 1e-3)
        val a = 2.0
        assertEquals(exp(a) * (a - 1) / (a * a) + 1 / (a * a), Calculus.integrate({ it * exp(a * it) }, 0.0, 1.0), 1e-9)
        assertEquals(2.0 * (-1.0) / 2, Calculus.integrate({ it * kotlin.math.sin(2 * it) }, -PI, PI) / PI, 1e-9)
        // Every generated question has a finite answer and a valid chapter.
        for ((topic, gens) in QuizGenerators.all) for (g in gens) repeat(30) {
            val q = g.make(Random(it))
            assertTrue("$topic ${q.prompt}", q.answer.isFinite() && abs(q.answer) < 1e30)
            assertTrue(q.chapterKey in Compendium.byKey)
        }
    }

    @Test
    fun mathTopicCoversTheWholeCurriculum() {
        val courses = Compendium.courses(Topic.MATH)
        for (c in listOf("Analysis I", "Analysis II", "Analysis III", "Lineare Algebra", "Funktionentheorie", "Funktionalanalysis"))
            assertTrue(c, c in courses)
        assertTrue(Compendium.courses(Topic.MECHANICS).containsAll(listOf("Theoretische Mechanik", "Relativitätstheorie")))
        for (c in courses) assertTrue(c, QuizEngine.session(Topic.MATH, 2, 8, Random(1), course = c).isNotEmpty())
    }
}

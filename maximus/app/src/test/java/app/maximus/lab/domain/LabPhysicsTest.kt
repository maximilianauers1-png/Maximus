package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabPhysicsTest {
    private fun ev(s: String, x: Double = 0.0) = ExprParser.parse(s).eval(mapOf("x" to x))

    @Test
    fun parserFollowsMathematicalPrecedence() {
        assertEquals(13.0, ev("2x^2 + 3x - 1", 2.0), 1e-12)
        assertEquals(-4.0, ev("-2^2"), 1e-12)
        assertEquals(512.0, ev("2^3^2"), 1e-12)
        assertEquals(1.0, ev("sin(pi/2)"), 1e-12)
        assertEquals(3.0, ev("|−3|"), 1e-12)
        assertEquals(120.0, ev("5!"), 1e-9)
        assertEquals(2.0, ev("max(1,2)"), 1e-12)
        assertEquals(5.0, ev("2,5*2"), 1e-12)
        assertEquals(-2.0, ev("(−8)^(1/3)"), 1e-12)
        assertEquals(4.0, ev("x²", 2.0), 1e-12)
        assertEquals(2 * sin1(), ev("2 sin x", 1.0), 1e-12)
        assertEquals(6.0, ev("2(x+1)", 2.0), 1e-12)
        assertEquals(1e-3, ev("1e-3"), 1e-18)
    }

    private fun sin1() = kotlin.math.sin(1.0)

    @Test
    fun specialFunctions() {
        assertEquals(24.0, SpecialFunctions.gamma(5.0), 1e-10)
        assertEquals(sqrt(PI), SpecialFunctions.gamma(0.5), 1e-12)
        assertEquals(0.8427007929497149, SpecialFunctions.erf(1.0), 2e-7)
        assertEquals(0.0, SpecialFunctions.besselJ0(2.404825557695773), 1e-9)
    }

    @Test
    fun calculus() {
        assertEquals(1.0, Calculus.derivative({ kotlin.math.sin(it) }, 0.0), 1e-10)
        assertEquals(9.0, Calculus.integrate({ it * it }, 0.0, 3.0), 1e-10)
        assertEquals(2.0, Calculus.gaussLegendre({ kotlin.math.sin(it) }, 0.0, PI, 4), 1e-10)
        assertEquals(PI / 2, Calculus.brent({ kotlin.math.cos(it) }, 1.0, 2.0)!!, 1e-12)
        assertEquals(listOf(-1.0, 1.0), Calculus.roots({ it * it - 1 }, -3.0, 3.0).map { Math.round(it * 1e9) / 1e9 })
    }

    @Test
    fun linearAlgebra() {
        val roots = Polynomials.roots(listOf(-6.0, 11.0, -6.0, 1.0)).map { it.re }.sorted()
        assertEquals(1.0, roots[0], 1e-9); assertEquals(2.0, roots[1], 1e-9); assertEquals(3.0, roots[2], 1e-9)
        val rot = Matrix.of(doubleArrayOf(0.0, -1.0), doubleArrayOf(1.0, 0.0)).eigenvalues()
        assertEquals(setOf(1.0, -1.0), rot.map { Math.round(it.im * 1e9) / 1e9 }.toSet())
        val m = Matrix.of(doubleArrayOf(2.0, 1.0, 0.0), doubleArrayOf(1.0, 3.0, 1.0), doubleArrayOf(0.0, 1.0, 4.0))
        val ev = m.eigenvalues()
        assertEquals(m.trace(), ev.sumOf { it.re }, 1e-9)
        assertEquals(m.determinant(), ev.fold(1.0) { a, b -> a * b.re }, 1e-9)
        val inv = m.inverse()!!
        val id = m * inv
        for (i in 0 until 3) for (j in 0 until 3) assertEquals(if (i == j) 1.0 else 0.0, id[i, j], 1e-12)
        // A random 6×6 matrix: eigenvalue sum equals the trace.
        val r = java.util.Random(7)
        val big = Matrix(6, 6).also { b -> for (i in 0 until 6) for (j in 0 until 6) b[i, j] = r.nextGaussian() }
        val bev = big.eigenvalues()
        assertEquals(big.trace(), bev.sumOf { it.re }, 1e-8)
        assertEquals(0.0, bev.sumOf { it.im }, 1e-8)
    }

    @Test
    fun odeSolvers() {
        val sol = Ode.rk4({ _, y -> doubleArrayOf(y[0]) }, doubleArrayOf(1.0), 0.0, 1.0, 100)
        assertEquals(kotlin.math.E, sol.y.last()[0], 1e-9)
    }

    @Test
    fun thermodynamics() {
        assertEquals(0.85, ThermoPhysics.carnot(2000.0, 300.0), 1e-12)
        // ∫ B_λ dλ = σT⁴/π.
        val t = 5000.0
        val total = Calculus.integrate({ ThermoPhysics.planckLambda(it, t) }, 50e-9, 100e-6, 1e-3)
        assertEquals(Phys.sigmaSB * t.pow(4) / PI, total, 2e-3 * total)
        val m = 28e-3 / Phys.NA
        assertEquals(1.0, Calculus.integrate({ ThermoPhysics.maxwell(it, m, 300.0) }, 0.0, 5000.0), 1e-6)
        assertEquals(3 * Phys.R, ThermoPhysics.debyeCv(5000.0, 343.0), 0.01 * 3 * Phys.R)
        val low = ThermoPhysics.debyeCv(5.0, 343.0)
        assertEquals(12 * PI.pow(4) / 5 * Phys.R * (5.0 / 343).pow(3), low, 1e-3 * low)
    }

    @Test
    fun semiconductors() {
        val si = SemiconductorPhysics.MATERIALS[0]
        assertEquals(1.12, si.eg(300.0), 0.01)
        assertTrue(si.ni(300.0) in 3e9..2e10)
        val vbi = SemiconductorPhysics.builtIn(1e17, 1e15, si.ni(300.0), 300.0)
        assertTrue(vbi in 0.6..0.8)
        val (n, p) = SemiconductorPhysics.carriers(1e16, 0.0, si.ni(300.0))
        assertEquals(1e16, n, 1e10)
        assertEquals(si.ni(300.0).pow(2), n * p, 1e-6 * n * p)
        val i = SemiconductorPhysics.diodeCurrent(0.7, 1e-12, 1.0, 300.0, 1.0)
        val vt = SemiconductorPhysics.kT(300.0)
        assertEquals(i, 1e-12 * (kotlin.math.exp((0.7 - i * 1.0) / vt) - 1), 1e-9 * abs(i))
    }

    @Test
    fun quantum() {
        for ((n, l) in listOf(1 to 0, 2 to 0, 2 to 1, 3 to 2, 4 to 1)) {
            val norm = Calculus.integrate({ r -> r * r * QuantumPhysics.hydrogenR(n, l, r).pow(2) }, 0.0, 80.0 * n)
            assertEquals("n=$n l=$l", 1.0, norm, 1e-6)
        }
        for (n in 0..5) assertEquals(1.0, Calculus.integrate({ QuantumPhysics.oscillatorPsi(n, it).pow(2) }, -12.0, 12.0), 1e-8)
        // Deep wide well approaches the infinite-well levels from below.
        val levels = QuantumPhysics.finiteWellLevels(1000.0, 1e-9, Phys.me)
        val e1inf = PI * PI * Phys.hbar * Phys.hbar / (2 * Phys.me * 1e-18) / Phys.e
        assertTrue(levels.first().first < e1inf && levels.first().first > 0.9 * e1inf)
        assertTrue(levels.first().second)
        val t = QuantumPhysics.barrierTransmission(2.0, 5.0, 0.5e-9, Phys.me)
        assertTrue(t in 1e-4..0.1)
    }

    @Test
    fun caloricModels() {
        val ps = CaloricPhysics.BaTiO3.polarization(0.0, 300.0)
        assertTrue("P_s(300 K) = $ps", ps in 0.2..0.32)
        assertEquals(0.0, CaloricPhysics.BaTiO3.polarization(0.0, 420.0), 1e-9)
        val dT = CaloricPhysics.BaTiO3.adiabaticDeltaT(2e6, 400.0)
        assertTrue("ΔT = $dT", dT in 0.1..4.0)
        val gd = CaloricPhysics.magnetocaloricDeltaT(CaloricPhysics.GD, 184.0, 2.0, 293.0)
        assertTrue("Gd ΔT = $gd", gd in 2.0..10.0)
        assertEquals(0.0, CaloricPhysics.GD.magnetization(0.0, 350.0), 1e-6)
    }

    @Test
    fun particlePhysics() {
        assertTrue(1 / QftPhysics.alphaQed(91.1876) in 126.0..131.0)
        assertEquals(0.1179, QftPhysics.alphaS(91.1876), 1e-12)
        assertTrue(QftPhysics.alphaS(10.0) > QftPhysics.alphaS(100.0))
        assertEquals(1.0, QftPhysics.kleinNishinaRatio(1e-6), 1e-4)
        assertEquals(0.4318, QftPhysics.kleinNishinaRatio(1.0), 2e-3)
        assertEquals(0.0, QftPhysics.twoBodyMomentum(1.0, 0.6, 0.6), 0.0)
    }

    @Test
    fun everyCalculatorRunsWithDefaults() {
        for (c in Calculators.all) {
            val r = c.run(c.defaults())
            assertTrue("${c.key}: ${r.warnings}", r.warnings.none { it.startsWith("Berechnung nicht möglich") })
            assertTrue("${c.key} has outputs", r.outputs.isNotEmpty())
            r.curves.forEach { cv -> cv.series.forEach { s -> assertEquals("${c.key}/${s.name}", s.xs.size, s.ys.size) } }
        }
        assertEquals(Calculators.all.size, Calculators.byKey.size)
    }
}

class LabStatsTest {
    @Test
    fun binomialSumsToOneForLargeN() {
        val n = 2000
        assertEquals(1.0, (0..n).sumOf { Stats.binomial(n, it, 0.3) }, 1e-9)
        assertEquals(kotlin.math.ln(3628800.0), SpecialFunctions.lnGamma(11.0), 1e-12)
        assertEquals(Stats.normalCdf(0.0), 0.5, 1e-12)
    }
}

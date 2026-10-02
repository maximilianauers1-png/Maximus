package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan

object QuantumPhysics {
    /** Physicists' Hermite polynomial Hₙ(x) by the recursion H_{n+1} = 2xHₙ − 2nH_{n−1}. */
    fun hermite(n: Int, x: Double): Double {
        if (n == 0) return 1.0
        var h0 = 1.0; var h1 = 2 * x
        for (k in 1 until n) { val h2 = 2 * x * h1 - 2 * k * h0; h0 = h1; h1 = h2 }
        return h1
    }

    /** Generalised Laguerre polynomial L_n^{(α)}(x): (k+1)L_{k+1} = (2k+1+α−x)L_k − (k+α)L_{k−1}. */
    fun laguerre(n: Int, alpha: Double, x: Double): Double {
        if (n == 0) return 1.0
        var l0 = 1.0; var l1 = 1 + alpha - x
        for (k in 1 until n) { val l2 = ((2 * k + 1 + alpha - x) * l1 - (k + alpha) * l0) / (k + 1); l0 = l1; l1 = l2 }
        return l1
    }

    private fun factorial(n: Int): Double = (1..n).fold(1.0) { a, k -> a * k }

    /** Normalised harmonic-oscillator eigenfunction in the dimensionless coordinate ξ = x/x₀, x₀ = √(ħ/mω). */
    fun oscillatorPsi(n: Int, xi: Double): Double =
        1 / sqrt(2.0.pow(n) * factorial(n)) * PI.pow(-0.25) * exp(-xi * xi / 2) * hermite(n, xi)

    /**
     * Hydrogen radial function R_{nl}(r) in units of a₀ (Z = 1):
     * R = √((2/n)³ (n−l−1)!/(2n (n+l)!)) e^{−ρ/2} ρ^l L_{n−l−1}^{(2l+1)}(ρ), ρ = 2r/n.
     */
    fun hydrogenR(n: Int, l: Int, r: Double): Double {
        val rho = 2 * r / n
        val norm = sqrt((2.0 / n).pow(3) * factorial(n - l - 1) / (2 * n * factorial(n + l)))
        return norm * exp(-rho / 2) * rho.pow(l) * laguerre(n - l - 1, 2.0 * l + 1, rho)
    }

    /** Transmission through a rectangular barrier of height V₀ and width a for a particle of energy E (exact). */
    fun barrierTransmission(eEv: Double, v0Ev: Double, widthM: Double, massKg: Double): Double {
        val e = eEv * Phys.e; val v0 = v0Ev * Phys.e
        if (e <= 0) return 0.0
        return when {
            e < v0 -> {
                val kappa = sqrt(2 * massKg * (v0 - e)) / Phys.hbar
                val s = sinh(kappa * widthM)
                if (!s.isFinite()) 0.0 else 1 / (1 + v0 * v0 * s * s / (4 * e * (v0 - e)))
            }
            e > v0 -> {
                val k2 = sqrt(2 * massKg * (e - v0)) / Phys.hbar
                val s = sin(k2 * widthM)
                1 / (1 + v0 * v0 * s * s / (4 * e * (e - v0)))
            }
            else -> 1 / (1 + massKg * widthM * widthM * v0 / (2 * Phys.hbar * Phys.hbar))
        }
    }

    /**
     * Bound states of a finite square well of depth V₀ and width 2a. With z = ka and z₀ = a√(2mV₀)/ħ:
     * even states solve z tan z = √(z₀² − z²), odd states −z cot z = √(z₀² − z²). Returns energies above the bottom, in eV.
     */
    fun finiteWellLevels(v0Ev: Double, widthM: Double, massKg: Double): List<Pair<Double, Boolean>> {
        val a = widthM / 2
        val z0 = a * sqrt(2 * massKg * v0Ev * Phys.e) / Phys.hbar
        val out = ArrayList<Pair<Double, Boolean>>()
        val even: (Double) -> Double = { z -> z * tan(z) - sqrt(max(0.0, z0 * z0 - z * z)) }
        val odd: (Double) -> Double = { z -> -z / tan(z) - sqrt(max(0.0, z0 * z0 - z * z)) }
        var k = 0
        while (k * PI / 2 < z0) {
            val lo = k * PI / 2 + 1e-9
            val hi = minOf((k + 1) * PI / 2 - 1e-9, z0)
            val f = if (k % 2 == 0) even else odd
            if (hi > lo) Calculus.brent(f, lo, hi)?.let { z -> out += (Phys.hbar * Phys.hbar * z * z / (2 * massKg * a * a) / Phys.e) to (k % 2 == 0) }
            k++
        }
        return out
    }

    private data class Metal(val name: String, val workEv: Double)
    private val METALS = listOf(Metal("Cäsium", 2.14), Metal("Kalium", 2.29), Metal("Natrium", 2.36), Metal("Zink", 4.33), Metal("Kupfer", 4.65), Metal("Platin", 5.65))

    private val MASSES = listOf("Elektron" to Phys.me, "Proton" to Phys.mp, "Myon" to Phys.me * 206.7682830)

    val calculators: List<Calculator> = listOf(
        Calculator(
            "photon", Topic.QUANTUM, "Photon und Materiewelle",
            "Umrechnung zwischen Wellenlänge, Frequenz, Energie und Impuls eines Photons sowie die de-Broglie-Wellenlänge eines Elektrons gleicher kinetischer Energie (relativistisch korrekt).",
            "E = hν = hc/λ,   p = h/λ = E/c,   λ_dB = h/p,   pc = √(E_kin² + 2E_kin mc²)",
            listOf(Param("lambda", "Wellenlänge λ", "nm", 550.0, 1e-9, 1e12))
        ) { v ->
            val l = v.getValue("lambda") * 1e-9
            val e = Phys.h * Phys.c / l
            val ek = e
            val pc = sqrt(ek * ek + 2 * ek * Phys.me * Phys.c * Phys.c)
            CalcResult(listOf(
                Output("Frequenz ν", Phys.c / l, "Hz"), Output("Energie E", e / Phys.e, "eV"), Output("Impuls p", e / Phys.c, "kg m/s"),
                Output("Wellenzahl 1/λ", 1 / l / 100, "cm⁻¹"), Output("Temperaturäquivalent E/k_B", e / Phys.kB, "K"),
                Output("Elektron gleicher E_kin: λ_dB", Phys.h * Phys.c / pc * 1e9, "nm"),
                Output("Photonen pro Joule", 1 / e, "")
            ), steps = listOf("E = hc/λ = 1239,84 eV nm / ${Fmt.num(l * 1e9)} nm = ${Fmt.num(e / Phys.e)} eV"))
        },
        Calculator(
            "photoeffect", Topic.QUANTUM, "Photoeffekt",
            "Einsteins Lichtquantenhypothese: Ein Photon gibt seine gesamte Energie an ein Elektron ab. Unterhalb der Grenzfrequenz werden — unabhängig von der Intensität — keine Elektronen ausgelöst.",
            "E_kin,max = hν − W_A,   eU_0 = E_kin,max,   λ_Grenz = hc/W_A",
            listOf(Param("metal", "Metall", "", 0.0, choices = METALS.map { "${it.name} (W_A = ${Fmt.num(it.workEv, 3)} eV)" }), Param("lambda", "Wellenlänge λ", "nm", 400.0, 1.0, 1e5))
        ) { v ->
            val m = METALS[v.getValue("metal").toInt()]; val l = v.getValue("lambda") * 1e-9
            val eph = Phys.h * Phys.c / l / Phys.e
            val ek = eph - m.workEv
            val ls = grid(100e-9, 1000e-9, 200)
            CalcResult(
                listOf(
                    Output("Photonenenergie", eph, "eV"), Output("E_kin,max", max(0.0, ek), "eV", if (ek <= 0) "keine Emission" else ""),
                    Output("Gegenspannung U₀", max(0.0, ek), "V"), Output("Grenzwellenlänge", Phys.h * Phys.c / (m.workEv * Phys.e) * 1e9, "nm"),
                    Output("Max. Elektronengeschwindigkeit", if (ek > 0) sqrt(2 * ek * Phys.e / Phys.me) else 0.0, "m/s")
                ),
                listOf(Curve("Gegenspannung gegen Frequenz (Steigung h/e)", "ν in 10¹⁴ Hz", "U₀ in V", METALS.map { mm ->
                    CurveSeries(mm.name, ls.map { Phys.c / it / 1e14 }, ls.map { max(0.0, Phys.h * Phys.c / it / Phys.e - mm.workEv) })
                }))
            )
        },
        Calculator(
            "box", Topic.QUANTUM, "Teilchen im unendlichen Potentialtopf",
            "Die stehenden Wellen ψₙ = √(2/L) sin(nπx/L) sind die Eigenfunktionen; die Energien wachsen mit n². Kurve: ψₙ, versetzt um ihre Energie (Einheiten E₁).",
            "Eₙ = n²π²ħ²/(2mL²) = n²h²/(8mL²),   ψₙ(x) = √(2/L) sin(nπx/L),   ⟨x⟩ = L/2,   ΔE_{n→n−1} = (2n−1)E₁",
            listOf(Param("L", "Breite L", "nm", 1.0, 1e-6, 1e9), Param("m", "Teilchen", "", 0.0, choices = MASSES.map { it.first }), Param("n", "Niveau n", "", 2.0, 1.0, 50.0))
        ) { v ->
            val l = v.getValue("L") * 1e-9; val mass = MASSES[v.getValue("m").toInt()].second; val n = v.getValue("n").toInt()
            val e1 = PI * PI * Phys.hbar * Phys.hbar / (2 * mass * l * l)
            val xs = grid(0.0, 1.0, 200)
            CalcResult(
                listOf(
                    Output("Grundenergie E₁", e1 / Phys.e, "eV"), Output("E_n", n * n * e1 / Phys.e, "eV"),
                    Output("Übergang n → n−1: Photon λ", if (n > 1) Phys.h * Phys.c / ((2 * n - 1) * e1) * 1e9 else 0.0, "nm"),
                    Output("Nullpunktsimpuls ħπ/L", Phys.hbar * PI / l, "kg m/s"),
                    Output("Δx·Δp im Grundzustand (in ħ)", sqrt(1.0 / 12 - 1 / (2 * PI * PI)) * PI, "", "> 1/2 erfüllt Heisenberg")
                ),
                listOf(Curve("Eigenfunktionen ψ₁…ψ₄ (versetzt um Eₙ/E₁)", "x/L", "Eₙ/E₁ + ψ", (1..4).map { k ->
                    CurveSeries("n = $k", xs, xs.map { k * k + 1.6 * sin(k * PI * it) }, dashed = k != n)
                }))
            )
        },
        Calculator(
            "finite_well", Topic.QUANTUM, "Endlicher Potentialtopf",
            "Im endlichen Topf reicht die Wellenfunktion exponentiell in die Wände (Eindringtiefe 1/κ). Es gibt immer mindestens einen gebundenen Zustand, insgesamt ⌈2z₀/π⌉. Die Energien folgen aus transzendenten Gleichungen, hier mit dem Brent-Verfahren gelöst.",
            "z = ka, z₀ = a√(2mV₀)/ħ:   gerade z tan z = √(z₀² − z²),   ungerade −z cot z = √(z₀² − z²)",
            listOf(Param("V0", "Tiefe V₀", "eV", 10.0, 1e-6, 1e9), Param("w", "Breite 2a", "nm", 1.0, 1e-6, 1e6), Param("m", "Teilchen", "", 0.0, choices = MASSES.map { it.first }))
        ) { v ->
            val v0 = v.getValue("V0"); val w = v.getValue("w") * 1e-9; val mass = MASSES[v.getValue("m").toInt()].second
            val levels = finiteWellLevels(v0, w, mass)
            val a = w / 2
            val z0 = a * sqrt(2 * mass * v0 * Phys.e) / Phys.hbar
            val e1inf = PI * PI * Phys.hbar * Phys.hbar / (2 * mass * w * w) / Phys.e
            val zs = grid(0.001, z0, 400)
            CalcResult(
                listOf(Output("z₀", z0, ""), Output("Anzahl gebundener Zustände", levels.size.toDouble(), "", digits = 3)) +
                    levels.mapIndexed { i, (e, even) -> Output("E${i + 1} (${if (even) "gerade" else "ungerade"})", e, "eV", "unendlicher Topf: ${Fmt.num((i + 1) * (i + 1) * e1inf)} eV") },
                listOf(Curve("Graphische Lösung", "z", "", listOf(
                    CurveSeries("z tan z", zs, zs.map { it * tan(it) }),
                    CurveSeries("−z cot z", zs, zs.map { -it / tan(it) }),
                    CurveSeries("√(z₀² − z²)", zs, zs.map { sqrt(max(0.0, z0 * z0 - it * it)) }, dashed = true)
                ), 0.0 to z0 * 1.4))
            )
        },
        Calculator(
            "tunnel", Topic.QUANTUM, "Tunneleffekt an der Rechteckbarriere",
            "Exakte Transmission durch eine Barriere der Höhe V₀ und Breite a. Für κa ≫ 1 gilt T ≈ 16(E/V₀)(1−E/V₀)e^{−2κa}. Oberhalb von V₀ treten Resonanzen (T = 1) bei k₂a = nπ auf.",
            "κ = √(2m(V₀−E))/ħ,   T = [1 + V₀² sinh²(κa) / (4E(V₀−E))]⁻¹  (E < V₀)",
            listOf(Param("V0", "Barrierenhöhe V₀", "eV", 5.0, 1e-6, 1e9), Param("a", "Breite a", "nm", 0.5, 1e-6, 1e6),
                Param("E", "Energie E", "eV", 2.0, 1e-9, 1e9), Param("m", "Teilchen", "", 0.0, choices = MASSES.map { it.first }))
        ) { v ->
            val v0 = v.getValue("V0"); val a = v.getValue("a") * 1e-9; val e = v.getValue("E"); val mass = MASSES[v.getValue("m").toInt()].second
            val t = barrierTransmission(e, v0, a, mass)
            val kappa = if (e < v0) sqrt(2 * mass * (v0 - e) * Phys.e) / Phys.hbar else 0.0
            val es = grid(0.01 * v0, 3 * v0, 400)
            CalcResult(
                listOf(
                    Output("Transmission T", t, ""), Output("Reflexion R = 1 − T", 1 - t, ""),
                    Output("κa", kappa * a, ""), Output("Näherung 16(E/V₀)(1−E/V₀)e^{−2κa}", if (e < v0) 16 * (e / v0) * (1 - e / v0) * exp(-2 * kappa * a) else Double.NaN, ""),
                    Output("Eindringtiefe 1/κ", if (kappa > 0) 1 / kappa * 1e9 else Double.POSITIVE_INFINITY, "nm")
                ),
                listOf(Curve("T(E)", "E/V₀", "T", listOf(CurveSeries("exakt", es.map { it / v0 }, es.map { barrierTransmission(it, v0, a, mass) })), 0.0 to 1.05))
            )
        },
        Calculator(
            "oscillator", Topic.QUANTUM, "Quantenharmonischer Oszillator",
            "Äquidistante Niveaus Eₙ = ħω(n + ½) mit Nullpunktsenergie. Die Eigenfunktionen sind Hermite-Funktionen; für großes n nähert sich |ψ|² der klassischen Aufenthaltswahrscheinlichkeit.",
            "Eₙ = ħω(n + ½),   ψₙ(ξ) = (2ⁿn!)^{−1/2} π^{−1/4} e^{−ξ²/2} Hₙ(ξ),   ξ = x√(mω/ħ)",
            listOf(Param("f", "Frequenz f = ω/2π", "THz", 64.0, 1e-9, 1e9, hint = "CO-Molekül ≈ 64 THz"), Param("n", "Niveau n", "", 3.0, 0.0, 30.0))
        ) { v ->
            val w = 2 * PI * v.getValue("f") * 1e12; val n = v.getValue("n").toInt()
            val xs = grid(-6.0, 6.0, 300)
            CalcResult(
                listOf(
                    Output("ħω", Phys.hbar * w / Phys.e * 1e3, "meV"), Output("E_n", Phys.hbar * w * (n + 0.5) / Phys.e * 1e3, "meV"),
                    Output("Übergangswellenlänge (Δn = 1)", 2 * PI * Phys.c / w * 1e6, "μm"), Output("Wellenzahl", w / (2 * PI * Phys.c) / 100, "cm⁻¹"),
                    Output("Besetzung n = 1 bei 300 K (Boltzmann)", exp(-Phys.hbar * w / (Phys.kB * 300)), "")
                ),
                listOf(Curve("|ψₙ|² versetzt um n + ½ (in ħω)", "ξ", "E/ħω", (0..minOf(n, 6)).map { k ->
                    CurveSeries("n = $k", xs, xs.map { k + 0.5 + 1.2 * oscillatorPsi(k, it).pow(2) * 2 }, dashed = k != n)
                } + CurveSeries("V = ξ²/2", xs, xs.map { it * it / 2 }, dashed = true), 0.0 to minOf(n, 6) + 2.0))
            )
        },
        Calculator(
            "hydrogen", Topic.QUANTUM, "Wasserstoffatom",
            "Energieniveaus mit reduzierter Masse, Spektralserien und die radiale Aufenthaltswahrscheinlichkeit P(r) = r²R²ₙₗ. Für l = n − 1 liegt das Maximum genau beim Bohr-Radius n²a₀.",
            "Eₙ = −(μ/mₑ) Z² Ry/n²,   1/λ = Z²R_μ(1/n₁² − 1/n₂²),   Rₙₗ(r) ∝ e^{−r/na₀} (2r/na₀)^l L^{2l+1}_{n−l−1}(2r/na₀)",
            listOf(Param("Z", "Kernladung Z", "", 1.0, 1.0, 100.0), Param("n1", "Unteres Niveau n₁", "", 2.0, 1.0, 20.0), Param("n2", "Oberes Niveau n₂", "", 3.0, 2.0, 30.0), Param("n", "Orbital n (für P(r))", "", 3.0, 1.0, 6.0))
        ) { v ->
            val z = v.getValue("Z"); val n1 = v.getValue("n1").toInt(); val n2 = max(n1 + 1, v.getValue("n2").toInt()); val n = v.getValue("n").toInt()
            val muFactor = 1 / (1 + Phys.me / Phys.mp)
            fun en(k: Int) = -muFactor * z * z * Phys.RyEv / (k * k)
            val de = en(n2) - en(n1)
            val lambda = Phys.h * Phys.c / (de * Phys.e)
            val rs = grid(0.0, 2.5 * n * n + 5, 300)
            val series = when (n1) { 1 -> "Lyman"; 2 -> "Balmer"; 3 -> "Paschen"; 4 -> "Brackett"; 5 -> "Pfund"; else -> "Humphreys u. a." }
            CalcResult(
                listOf(
                    Output("E(n₁)", en(n1), "eV"), Output("E(n₂)", en(n2), "eV"), Output("Photonenenergie", de, "eV"),
                    Output("Wellenlänge ($series-Serie)", lambda * 1e9, "nm"), Output("Seriengrenze", Phys.h * Phys.c / (-en(n1) * Phys.e) * 1e9, "nm"),
                    Output("Ionisierungsenergie aus n₁", -en(n1), "eV"), Output("Bahnradius n₁²a₀/Z (Bohr)", n1 * n1 * Phys.a0 / z * 1e12, "pm"),
                    Output("Bahngeschwindigkeit Zαc/n₁", z * Phys.alpha * Phys.c / n1, "m/s")
                ),
                listOf(Curve("Radiale Wahrscheinlichkeit P(r) = r²R²ₙₗ (Z = 1)", "r/a₀", "P(r)", (0 until n).map { l ->
                    CurveSeries("n = $n, l = $l", rs, rs.map { r -> r * r * hydrogenR(n, l, r).pow(2) }, dashed = l != n - 1)
                }))
            )
        },
        Calculator(
            "wavepacket", Topic.QUANTUM, "Zerfließen eines Gaußschen Wellenpakets",
            "Ein freies Gaußsches Wellenpaket minimaler Unschärfe verbreitert sich, weil Komponenten mit verschiedenem Impuls verschieden schnell laufen. Die Zeitskala ist τ = 2mσ₀²/ħ.",
            "σ(t) = σ₀ √(1 + (ħt/(2mσ₀²))²),   σ_p = ħ/(2σ₀),   σ_x σ_p ≥ ħ/2",
            listOf(Param("s0", "Anfangsbreite σ₀", "nm", 0.1, 1e-9, 1e9), Param("m", "Teilchen", "", 0.0, choices = MASSES.map { it.first }), Param("t", "Zeit t", "fs", 1.0, 0.0, 1e12))
        ) { v ->
            val s0 = v.getValue("s0") * 1e-9; val mass = MASSES[v.getValue("m").toInt()].second; val t = v.getValue("t") * 1e-15
            val tau = 2 * mass * s0 * s0 / Phys.hbar
            fun sigma(tt: Double) = s0 * sqrt(1 + (tt / tau).pow(2))
            val ts = grid(0.0, max(5 * tau, 2 * t), 200)
            val xs = grid(-4 * sigma(t), 4 * sigma(t), 200)
            CalcResult(
                listOf(Output("Zeitskala τ = 2mσ₀²/ħ", tau * 1e15, "fs"), Output("σ(t)", sigma(t) * 1e9, "nm"), Output("Impulsunschärfe σ_p", Phys.hbar / (2 * s0), "kg m/s"), Output("Geschwindigkeitsunschärfe", Phys.hbar / (2 * s0 * mass), "m/s")),
                listOf(
                    Curve("Breite σ(t)", "t in fs", "σ in nm", listOf(CurveSeries("σ(t)", ts.map { it * 1e15 }, ts.map { sigma(it) * 1e9 }))),
                    Curve("|ψ(x)|² bei t = 0 und t", "x in nm", "Dichte (normiert)", listOf(
                        CurveSeries("t = 0", xs.map { it * 1e9 }, xs.map { exp(-it * it / (2 * s0 * s0)) / (sqrt(2 * PI) * s0) * 1e-9 }),
                        CurveSeries("t", xs.map { it * 1e9 }, xs.map { exp(-it * it / (2 * sigma(t).pow(2))) / (sqrt(2 * PI) * sigma(t)) * 1e-9 }, dashed = true)
                    ))
                )
            )
        },
        Calculator(
            "rabi", Topic.QUANTUM, "Spin ½: Larmor-Präzession und Rabi-Oszillation",
            "Ein Zwei-Niveau-System (z. B. Elektronenspin) im statischen Feld präzediert mit ω_L = gμ_BB/ħ. Ein resonantes Wechselfeld treibt Rabi-Oszillationen; bei Verstimmung Δ sinkt die Amplitude.",
            "ω_L = g μ_B B/ħ,   P↑↓(t) = Ω²/(Ω² + Δ²) · sin²(√(Ω² + Δ²) t/2)",
            listOf(Param("B", "Statisches Feld B", "T", 0.35, 0.0, 100.0), Param("Omega", "Rabi-Frequenz Ω/2π", "MHz", 10.0, 1e-6, 1e6), Param("Delta", "Verstimmung Δ/2π", "MHz", 5.0, 0.0, 1e6))
        ) { v ->
            val b = v.getValue("B"); val om = 2 * PI * v.getValue("Omega") * 1e6; val de = 2 * PI * v.getValue("Delta") * 1e6
            val g = 2.00231930436
            val eff = sqrt(om * om + de * de)
            val ts = grid(0.0, 4 * 2 * PI / om, 300)
            CalcResult(
                listOf(
                    Output("Larmor-Frequenz f_L", g * Phys.muB * b / Phys.hbar / (2 * PI) / 1e9, "GHz"),
                    Output("Zeeman-Aufspaltung", g * Phys.muB * b / Phys.e * 1e6, "μeV"),
                    Output("π-Puls-Dauer (Δ = 0)", PI / om * 1e9, "ns"), Output("Max. Umklappwahrscheinlichkeit", om * om / (eff * eff), "")
                ),
                listOf(Curve("Besetzung des angeregten Zustands", "t in ns", "P", listOf(
                    CurveSeries("Δ wie gewählt", ts.map { it * 1e9 }, ts.map { om * om / (eff * eff) * sin(eff * it / 2).pow(2) }),
                    CurveSeries("resonant", ts.map { it * 1e9 }, ts.map { sin(om * it / 2).pow(2) }, dashed = true)
                ), 0.0 to 1.05))
            )
        },
        Calculator(
            "compton", Topic.QUANTUM, "Compton-Streuung",
            "Stoß eines Photons mit einem freien Elektron: Die Wellenlängenverschiebung hängt nur vom Streuwinkel ab — ein direkter Beweis für den Teilchencharakter des Lichts.",
            "Δλ = λ_C(1 − cos θ),   λ_C = h/(mₑc) = 2,426 pm,   E' = E / (1 + (E/mₑc²)(1 − cos θ))",
            listOf(Param("E", "Photonenenergie E", "keV", 662.0, 1e-6, 1e9, hint = "Cs-137: 662 keV"), Param("theta", "Streuwinkel θ", "°", 90.0, 0.0, 180.0))
        ) { v ->
            val e = v.getValue("E"); val th = v.getValue("theta") * PI / 180
            val mc2 = Phys.meMeV * 1e3
            fun eOut(t: Double) = e / (1 + e / mc2 * (1 - cos(t)))
            val ths = grid(0.0, PI, 180)
            CalcResult(
                listOf(Output("Δλ", Phys.lambdaC * (1 - cos(th)) * 1e12, "pm"), Output("Gestreute Energie E'", eOut(th), "keV"), Output("Elektronenenergie E − E'", e - eOut(th), "keV"), Output("Compton-Kante (θ = 180°)", e - eOut(PI), "keV")),
                listOf(Curve("Gestreute Photonenenergie", "θ in Grad", "E' in keV", listOf(CurveSeries("E'(θ)", ths.map { it * 180 / PI }, ths.map { eOut(it) }))))
            )
        }
    )
}

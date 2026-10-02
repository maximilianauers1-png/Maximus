package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object ElectroPhysics {
    const val K = 1 / (4 * PI * Phys.eps0)

    /** Under-, critically or over-damped series RLC: charge q(t) for q(0) = q₀, i(0) = 0. */
    fun rlcCharge(t: Double, r: Double, l: Double, c: Double, q0: Double): Double {
        val d = r / (2 * l)
        val w0 = 1 / sqrt(l * c)
        return when {
            d < w0 * (1 - 1e-9) -> { val wd = sqrt(w0 * w0 - d * d); q0 * exp(-d * t) * (cos(wd * t) + d / wd * sin(wd * t)) }
            d > w0 * (1 + 1e-9) -> {
                val s = sqrt(d * d - w0 * w0); val r1 = -d + s; val r2 = -d - s
                q0 * (r2 * exp(r1 * t) - r1 * exp(r2 * t)) / (r2 - r1)
            }
            else -> q0 * (1 + d * t) * exp(-d * t)
        }
    }

    private data class Metal(val name: String, val rho: Double)
    private val METALS = listOf(Metal("Silber", 1.59e-8), Metal("Kupfer", 1.68e-8), Metal("Gold", 2.44e-8), Metal("Aluminium", 2.65e-8), Metal("Eisen (μ_r ≈ 1 angenommen)", 9.71e-8), Metal("Meerwasser", 0.2))

    private data class Particle(val name: String, val mass: Double, val charge: Double)
    private val PARTICLES = listOf(Particle("Elektron", Phys.me, -Phys.e), Particle("Proton", Phys.mp, Phys.e), Particle("α-Teilchen", 6.644657e-27, 2 * Phys.e), Particle("Myon", Phys.me * 206.7682830, -Phys.e))

    val calculators: List<Calculator> = listOf(
        Calculator(
            "coulomb", Topic.ELECTRO, "Coulomb-Kraft und Potential",
            "Zwei Punktladungen im Abstand r. Die Kurve zeigt das Potential einer Ladung und die Feldstärke als Funktion des Abstands (1/r bzw. 1/r²).",
            "F = q₁q₂/(4πε₀r²),   E = q/(4πε₀r²),   φ = q/(4πε₀r),   W = q₁q₂/(4πε₀r)",
            listOf(
                Param("q1", "Ladung q₁", "nC", 10.0, -1e9, 1e9), Param("q2", "Ladung q₂", "nC", -5.0, -1e9, 1e9),
                Param("r", "Abstand r", "cm", 10.0, 1e-9, 1e9), Param("er", "Relative Permittivität ε_r", "", 1.0, 1.0, 1e5, hint = "Wasser ≈ 80")
            )
        ) { v ->
            val q1 = v.getValue("q1") * 1e-9; val q2 = v.getValue("q2") * 1e-9; val r = v.getValue("r") / 100; val er = v.getValue("er")
            val f = K * q1 * q2 / (er * r * r)
            val rs = grid(r * 0.2, r * 3, 200)
            CalcResult(
                listOf(
                    Output("Kraft F", f, "N", if (f < 0) "anziehend" else "abstoßend"),
                    Output("Feld von q₁ am Ort von q₂", K * q1 / (er * r * r), "V/m"),
                    Output("Potential von q₁ bei r", K * q1 / (er * r), "V"),
                    Output("Potentielle Energie W", K * q1 * q2 / (er * r), "J"),
                    Output("Coulomb/Gravitation für zwei Elektronen", K * Phys.e * Phys.e / (Phys.G * Phys.me * Phys.me), "", "abstandsunabhängig")
                ),
                listOf(Curve("Feld und Potential von q₁", "r in cm", "E in kV/m, φ in kV", listOf(
                    CurveSeries("E(r)", rs.map { it * 100 }, rs.map { K * q1 / (er * it * it) / 1e3 }),
                    CurveSeries("φ(r)", rs.map { it * 100 }, rs.map { K * q1 / (er * it) / 1e3 }, dashed = true)
                ))),
                listOf("F = 8,988·10⁹ N m²/C² · ${Fmt.num(q1)} C · ${Fmt.num(q2)} C / (${Fmt.num(er)} · (${Fmt.num(r)} m)²) = ${Fmt.num(f)} N")
            )
        },
        Calculator(
            "capacitor", Topic.ELECTRO, "Plattenkondensator",
            "Homogenes Feld zwischen zwei Platten. Die Energiedichte des Feldes ist w = ε₀ε_rE²/2; die Platten ziehen sich mit F = Q²/(2ε₀ε_rA) an.",
            "C = ε₀ε_r A/d,   Q = CU,   E = U/d,   W = CU²/2,   F = Q²/(2ε₀ε_r A)",
            listOf(
                Param("A", "Plattenfläche A", "cm²", 100.0, 1e-9, 1e12), Param("d", "Abstand d", "mm", 1.0, 1e-9, 1e9),
                Param("er", "Dielektrikum ε_r", "", 1.0, 1.0, 1e5), Param("U", "Spannung U", "V", 100.0, -1e9, 1e9)
            )
        ) { v ->
            val a = v.getValue("A") * 1e-4; val d = v.getValue("d") * 1e-3; val er = v.getValue("er"); val u = v.getValue("U")
            val c = Phys.eps0 * er * a / d
            val q = c * u
            CalcResult(
                listOf(
                    Output("Kapazität C", c * 1e12, "pF"), Output("Ladung Q", q * 1e9, "nC"), Output("Feldstärke E", u / d, "V/m"),
                    Output("Energie W", 0.5 * c * u * u, "J"), Output("Energiedichte w", 0.5 * Phys.eps0 * er * (u / d).pow(2), "J/m³"),
                    Output("Anziehungskraft F", q * q / (2 * Phys.eps0 * er * a), "N")
                ),
                steps = listOf("C = 8,854·10⁻¹² F/m · ${Fmt.num(er)} · ${Fmt.num(a)} m² / ${Fmt.num(d)} m = ${Fmt.num(c)} F"),
                warnings = if (abs(u / d) > 3e6 && er == 1.0) listOf("Feldstärke über der Durchschlagfestigkeit von Luft (≈ 3 MV/m).") else emptyList()
            )
        },
        Calculator(
            "rc", Topic.ELECTRO, "RC-Glied: Laden und Tiefpass",
            "Laden eines Kondensators über einen Widerstand und dasselbe Netzwerk als Tiefpassfilter. Nach 5τ ist der Kondensator zu 99,3 % geladen; bei f_c fällt die Amplitude auf 1/√2 (−3 dB).",
            "u_C(t) = U₀(1 − e^{−t/τ}),   τ = RC,   f_c = 1/(2πRC),   |H(f)| = 1/√(1 + (f/f_c)²),   φ = −arctan(f/f_c)",
            listOf(Param("R", "Widerstand R", "kΩ", 10.0, 1e-9, 1e12), Param("C", "Kapazität C", "μF", 10.0, 1e-12, 1e9), Param("U0", "Spannung U₀", "V", 5.0, -1e6, 1e6))
        ) { v ->
            val r = v.getValue("R") * 1e3; val c = v.getValue("C") * 1e-6; val u0 = v.getValue("U0")
            val tau = r * c
            val fc = 1 / (2 * PI * tau)
            val ts = grid(0.0, 5 * tau, 200)
            val fs = logGrid(fc / 100, fc * 100, 200)
            CalcResult(
                listOf(
                    Output("Zeitkonstante τ", tau, "s"), Output("Grenzfrequenz f_c", fc, "Hz"),
                    Output("Spannung nach τ", u0 * (1 - exp(-1.0)), "V", "63,2 %"),
                    Output("Gespeicherte Energie", 0.5 * c * u0 * u0, "J"), Output("Im Widerstand verheizt (Laden)", 0.5 * c * u0 * u0, "J", "immer genau so viel wie gespeichert")
                ),
                listOf(
                    Curve("Laden und Entladen", "t in s", "u in V", listOf(
                        CurveSeries("u_C laden", ts, ts.map { u0 * (1 - exp(-it / tau)) }),
                        CurveSeries("u_C entladen", ts, ts.map { u0 * exp(-it / tau) }, dashed = true)
                    )),
                    Curve("Bode-Diagramm (Betrag)", "log₁₀(f/Hz)", "|H| in dB", listOf(
                        CurveSeries("Tiefpass", fs.map { log10(it) }, fs.map { 20 * log10(1 / sqrt(1 + (it / fc).pow(2))) }),
                        CurveSeries("Phase in Grad/10", fs.map { log10(it) }, fs.map { -atan(it / fc) * 180 / PI / 10 }, dashed = true)
                    ))
                )
            )
        },
        Calculator(
            "rlc", Topic.ELECTRO, "RLC-Schwingkreis",
            "Gedämpfter Schwingkreis: Die Ladung folgt q̈ + 2δq̇ + ω₀²q = 0. Je nach δ/ω₀ ist er schwach gedämpft (Schwingung), aperiodisch oder im Kriechfall. Die Resonanzkurve zeigt den Strom bei Anregung mit U₀ cos ωt.",
            "ω₀ = 1/√(LC),   δ = R/(2L),   Q = ω₀L/R = (1/R)√(L/C),   I(ω) = U₀ / √(R² + (ωL − 1/ωC)²)",
            listOf(
                Param("R", "Widerstand R", "Ω", 10.0, 0.0, 1e9), Param("L", "Induktivität L", "mH", 10.0, 1e-9, 1e9),
                Param("C", "Kapazität C", "μF", 1.0, 1e-12, 1e9), Param("U0", "Amplitude U₀ / Anfangsspannung", "V", 1.0, 0.0, 1e6)
            )
        ) { v ->
            val r = v.getValue("R"); val l = v.getValue("L") * 1e-3; val c = v.getValue("C") * 1e-6; val u0 = v.getValue("U0")
            val w0 = 1 / sqrt(l * c); val d = r / (2 * l)
            val q = if (r > 0) w0 * l / r else Double.POSITIVE_INFINITY
            val regime = when { d < w0 * 0.999999 -> "schwach gedämpft (Schwingfall)"; d > w0 * 1.000001 -> "Kriechfall"; else -> "aperiodischer Grenzfall" }
            val tEnd = if (d > 0) minOf(8 / d, 40 * PI / w0) else 20 * PI / w0
            val ts = grid(0.0, tEnd, 400)
            val ws = grid(w0 * 0.2, w0 * 2.0, 300)
            CalcResult(
                listOf(
                    Output("Eigenfrequenz f₀", w0 / (2 * PI), "Hz"), Output("Dämpfung δ", d, "s⁻¹"), Output("Güte Q", q, ""),
                    Output("Gedämpfte Frequenz f_d", if (d < w0) sqrt(w0 * w0 - d * d) / (2 * PI) else 0.0, "Hz"),
                    Output("Bandbreite Δf = f₀/Q", if (q.isFinite()) w0 / (2 * PI) / q else 0.0, "Hz"),
                    Output("Strom in Resonanz U₀/R", if (r > 0) u0 / r else Double.POSITIVE_INFINITY, "A")
                ),
                listOf(
                    Curve("Freie Schwingung: u_C(t) ($regime)", "t in ms", "u_C in V", listOf(CurveSeries("u_C", ts.map { it * 1e3 }, ts.map { rlcCharge(it, r, l, c, c * u0) / c }))),
                    Curve("Resonanzkurve", "ω/ω₀", "I in mA", listOf(CurveSeries("|I(ω)|", ws.map { it / w0 }, ws.map { u0 / sqrt(r * r + (it * l - 1 / (it * c)).pow(2)) * 1e3 })))
                ),
                listOf("Regime: δ = ${Fmt.num(d)} s⁻¹ gegen ω₀ = ${Fmt.num(w0)} s⁻¹ → $regime")
            )
        },
        Calculator(
            "bfield", Topic.ELECTRO, "Magnetfelder: Draht, Spule, Leiterschleife",
            "Biot-Savart für einfache Geometrien. Die Kurve vergleicht den 1/r-Abfall des Drahtes mit dem Feld auf der Achse einer Leiterschleife (∝ 1/z³ für z ≫ R).",
            "B_Draht = μ₀I/(2πr),   B_Spule = μ₀NI/ℓ,   B_Schleife(z) = μ₀IR²/(2(R² + z²)^{3/2})",
            listOf(
                Param("I", "Strom I", "A", 10.0, -1e9, 1e9), Param("r", "Abstand r / Achsenabstand z", "cm", 2.0, 0.0, 1e9),
                Param("N", "Windungen N", "", 500.0, 1.0, 1e9), Param("len", "Spulenlänge ℓ", "cm", 20.0, 1e-6, 1e9), Param("R", "Schleifenradius R", "cm", 5.0, 1e-6, 1e9)
            )
        ) { v ->
            val i = v.getValue("I"); val r = v.getValue("r") / 100; val n = v.getValue("N"); val len = v.getValue("len") / 100; val rl = v.getValue("R") / 100
            fun loop(z: Double) = Phys.mu0 * i * rl * rl / (2 * (rl * rl + z * z).pow(1.5))
            val zs = grid(0.0, 5 * rl, 200)
            CalcResult(
                listOf(
                    Output("Draht bei r", if (r > 0) Phys.mu0 * i / (2 * PI * r) * 1e3 else Double.POSITIVE_INFINITY, "mT"),
                    Output("Lange Spule (innen)", Phys.mu0 * n * i / len * 1e3, "mT"),
                    Output("Schleife auf der Achse bei z = r", loop(r) * 1e3, "mT"),
                    Output("Schleifenmitte μ₀I/(2R)", Phys.mu0 * i / (2 * rl) * 1e3, "mT"),
                    Output("Energiedichte in der Spule B²/(2μ₀)", (Phys.mu0 * n * i / len).pow(2) / (2 * Phys.mu0), "J/m³"),
                    Output("Erdfeld zum Vergleich", 0.05, "mT")
                ),
                listOf(Curve("Feld gegen Abstand", "Abstand in cm", "B in mT", listOf(
                    CurveSeries("Draht", zs.drop(1).map { it * 100 }, zs.drop(1).map { Phys.mu0 * i / (2 * PI * it) * 1e3 }),
                    CurveSeries("Schleife (Achse)", zs.map { it * 100 }, zs.map { loop(it) * 1e3 }, dashed = true)
                ), 0.0 to Phys.mu0 * abs(i) / (2 * rl) * 1e3 * 2.5))
            )
        },
        Calculator(
            "cyclotron", Topic.ELECTRO, "Geladenes Teilchen im Magnetfeld",
            "Die Lorentzkraft hält das Teilchen auf einer Kreisbahn. Die Zyklotronfrequenz hängt nicht von der Geschwindigkeit ab (nichtrelativistisch) — das Prinzip des Zyklotrons und der Massenspektrometrie.",
            "F = q(E + v × B),   r = mv/(|q|B) = p/(|q|B),   ω_c = |q|B/m,   relativistisch: r = γmv/(|q|B)",
            listOf(
                Param("p", "Teilchen", "", 0.0, choices = PARTICLES.map { it.name }),
                Param("Ekin", "Kinetische Energie", "keV", 10.0, 1e-9, 1e12),
                Param("B", "Magnetfeld B", "T", 0.1, 1e-12, 1e3)
            )
        ) { v ->
            val pa = PARTICLES[v.getValue("p").toInt()]; val ek = v.getValue("Ekin") * 1e3 * Phys.e; val b = v.getValue("B")
            val mc2 = pa.mass * Phys.c * Phys.c
            val gamma = 1 + ek / mc2
            val beta = sqrt(1 - 1 / (gamma * gamma))
            val p = gamma * pa.mass * beta * Phys.c
            val pNr = sqrt(2 * pa.mass * ek)
            CalcResult(
                listOf(
                    Output("Geschwindigkeit v/c", beta, ""), Output("Lorentzfaktor γ", gamma, ""),
                    Output("Bahnradius r (relativistisch)", p / (abs(pa.charge) * b), "m"),
                    Output("Bahnradius nichtrelativistisch", pNr / (abs(pa.charge) * b), "m"),
                    Output("Zyklotronfrequenz f = |q|B/(2πγm)", abs(pa.charge) * b / (2 * PI * gamma * pa.mass), "Hz"),
                    Output("Magnetische Steifigkeit Bρ = p/q", p / abs(pa.charge), "T m")
                ),
                steps = listOf("γ = 1 + E_kin/(mc²) = ${Fmt.num(gamma)},  p = γmv = ${Fmt.num(p)} kg m/s,  r = p/(|q|B)")
            )
        },
        Calculator(
            "generator", Topic.ELECTRO, "Induktion: Generator und Transformator",
            "Eine Spule mit N Windungen rotiert im homogenen Feld: Der Fluss Φ = BA cos ωt induziert U = NBAω sin ωt. Ein idealer Transformator überträgt die Leistung verlustfrei.",
            "U_ind = −N dΦ/dt,   Û = NBAω,   U_eff = Û/√2,   U₂/U₁ = N₂/N₁,   I₂/I₁ = N₁/N₂",
            listOf(
                Param("N", "Windungen N", "", 200.0, 1.0, 1e9), Param("B", "Feld B", "T", 0.5, 0.0, 100.0),
                Param("A", "Fläche A", "cm²", 100.0, 0.0, 1e9), Param("f", "Drehfrequenz f", "Hz", 50.0, 0.0, 1e9),
                Param("ratio", "Übersetzung N₂/N₁", "", 0.1, 1e-6, 1e6)
            )
        ) { v ->
            val n = v.getValue("N"); val b = v.getValue("B"); val a = v.getValue("A") * 1e-4; val w = 2 * PI * v.getValue("f")
            val peak = n * b * a * w
            val ts = grid(0.0, 2 / v.getValue("f").coerceAtLeast(1e-9), 200)
            CalcResult(
                listOf(Output("Scheitelspannung Û", peak, "V"), Output("Effektivwert U_eff", peak / sqrt(2.0), "V"), Output("Sekundär U₂,eff", peak / sqrt(2.0) * v.getValue("ratio"), "V")),
                listOf(Curve("Induzierte Spannung", "t in ms", "U in V", listOf(
                    CurveSeries("U(t)", ts.map { it * 1e3 }, ts.map { peak * sin(w * it) }),
                    CurveSeries("Φ(t)·N·ω (Phase)", ts.map { it * 1e3 }, ts.map { peak * cos(w * it) }, dashed = true)
                )))
            )
        },
        Calculator(
            "skin", Topic.ELECTRO, "Skin-Effekt",
            "Wechselstrom fließt in einem guten Leiter nur in einer dünnen Randschicht; die Feldamplitude fällt wie e^{−x/δ} ab. Darum sind HF-Leitungen versilbert und Litzen verdrillt.",
            "δ = √(2ρ/(ωμ)) = √(ρ/(πfμ₀μ_r))",
            listOf(Param("mat", "Leiter", "", 1.0, choices = METALS.map { it.name }), Param("f", "Frequenz f", "Hz", 50.0, 1e-3, 1e15))
        ) { v ->
            val m = METALS[v.getValue("mat").toInt()]; val f = v.getValue("f")
            fun delta(ff: Double) = sqrt(m.rho / (PI * ff * Phys.mu0))
            val fs = logGrid(1.0, 1e10, 200)
            CalcResult(
                listOf(Output("Eindringtiefe δ", delta(f) * 1e3, "mm"), Output("Bei 1 MHz", delta(1e6) * 1e6, "μm"), Output("Bei 1 GHz", delta(1e9) * 1e6, "μm")),
                listOf(Curve("Eindringtiefe", "log₁₀(f/Hz)", "log₁₀(δ/m)", listOf(CurveSeries("δ(f)", fs.map { log10(it) }, fs.map { log10(delta(it)) }))))
            )
        },
        Calculator(
            "drude", Topic.ELECTRO, "Drude-Modell der Leitfähigkeit",
            "Klassisches Elektronengas mit Stoßzeit τ. Liefert Ohmsches Gesetz, Beweglichkeit und — mit der Fermi-Geschwindigkeit — die mittlere freie Weglänge.",
            "σ = ne²τ/m,   μ = eτ/m,   j = σE,   ℓ = v_F τ,   v_F = (ħ/m)(3π²n)^{1/3}",
            listOf(Param("n", "Elektronendichte n", "10²⁸ m⁻³", 8.47, 1e-12, 1e6, hint = "Kupfer 8,47"), Param("tau", "Stoßzeit τ", "fs", 25.0, 1e-6, 1e9))
        ) { v ->
            val n = v.getValue("n") * 1e28; val tau = v.getValue("tau") * 1e-15
            val sigma = n * Phys.e * Phys.e * tau / Phys.me
            val vf = Phys.hbar / Phys.me * (3 * PI * PI * n).pow(1.0 / 3)
            CalcResult(listOf(
                Output("Leitfähigkeit σ", sigma, "S/m"), Output("Spezifischer Widerstand ρ", 1 / sigma * 1e8, "μΩ cm"),
                Output("Beweglichkeit μ", Phys.e * tau / Phys.me * 1e4, "cm²/(V s)"), Output("Fermi-Geschwindigkeit v_F", vf, "m/s"),
                Output("Mittlere freie Weglänge ℓ", vf * tau * 1e9, "nm"), Output("Fermi-Energie E_F = mv_F²/2", 0.5 * Phys.me * vf * vf / Phys.e, "eV"),
                Output("Plasmafrequenz ω_p/2π", sqrt(n * Phys.e * Phys.e / (Phys.eps0 * Phys.me)) / (2 * PI), "Hz")
            ))
        }
    )
}

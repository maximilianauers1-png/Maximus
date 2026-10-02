package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atanh
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object QftPhysics {
    /** Lepton masses in GeV for QED vacuum polarisation thresholds. */
    private val LEPTONS = listOf(Phys.meMeV / 1e3, Phys.mmuMeV / 1e3, Phys.mtauMeV / 1e3)
    /** Quark masses (GeV) with charges for the hadronic part of the R ratio and α running (rough thresholds). */
    private val QUARKS = listOf(0.3 to 2.0 / 3, 0.3 to -1.0 / 3, 0.5 to -1.0 / 3, 1.5 to 2.0 / 3, 4.8 to -1.0 / 3, 173.0 to 2.0 / 3)

    /**
     * One-loop QED running coupling with leptons and (effective) quarks above threshold:
     *   1/α(Q) = 1/α(0) − (2/3π) Σ_f N_c Q_f² ln(Q/m_f)   for Q > m_f.
     * With realistic hadronic thresholds this gives 1/α(M_Z) ≈ 128–129.
     */
    fun alphaQed(qGeV: Double): Double {
        var inv = 1 / Phys.alpha
        for (m in LEPTONS) if (qGeV > m) inv -= 2 / (3 * PI) * ln(qGeV / m)
        for ((m, ch) in QUARKS) if (qGeV > m) inv -= 2 / (3 * PI) * 3 * ch * ch * ln(qGeV / m)
        return 1 / inv
    }

    /**
     * One-loop QCD: α_s(Q) = α_s(M_Z) / (1 + α_s(M_Z) b₀ ln(Q²/M_Z²)/(4π)), b₀ = 11 − 2n_f/3, matched at the
     * c (1.5 GeV) and b (4.8 GeV) thresholds so that α_s is continuous. α_s(M_Z) = 0.1179 (PDG).
     */
    fun alphaS(qGeV: Double, alphaMz: Double = 0.1179): Double {
        val mz = 91.1876
        fun run(a0: Double, q0: Double, q: Double, nf: Int): Double = a0 / (1 + a0 * (11 - 2.0 * nf / 3) * ln(q * q / (q0 * q0)) / (4 * PI))
        return when {
            qGeV >= 4.8 -> run(alphaMz, mz, qGeV, 5)
            qGeV >= 1.5 -> run(run(alphaMz, mz, 4.8, 5), 4.8, qGeV, 4)
            else -> run(run(run(alphaMz, mz, 4.8, 5), 4.8, 1.5, 4), 1.5, qGeV, 3)
        }
    }

    /** Klein–Nishina total cross section for Compton scattering, k = E_γ/mₑc², in units of the Thomson cross section. */
    fun kleinNishinaRatio(k: Double): Double {
        if (k < 1e-4) return 1 - 2 * k + 26.0 / 5 * k * k
        val a = (1 + k) / (k * k * k) * (2 * k * (1 + k) / (1 + 2 * k) - ln(1 + 2 * k))
        val b = ln(1 + 2 * k) / (2 * k) - (1 + 3 * k) / ((1 + 2 * k) * (1 + 2 * k))
        return 0.75 * (a + b)
    }

    /** Two-body decay momentum p* = √([M² − (m₁+m₂)²][M² − (m₁−m₂)²]) / 2M. */
    fun twoBodyMomentum(m: Double, m1: Double, m2: Double): Double {
        val x = (m * m - (m1 + m2).pow(2)) * (m * m - (m1 - m2).pow(2))
        return if (x <= 0) 0.0 else sqrt(x) / (2 * m)
    }

    private val PARTICLES = listOf(
        "Elektron" to Phys.meMeV, "Myon" to Phys.mmuMeV, "Pion π±" to 139.57039, "Kaon K±" to 493.677, "Proton" to Phys.mpMeV,
        "W-Boson" to 80377.0, "Z-Boson" to 91187.6, "Higgs" to 125250.0, "Top-Quark" to 172760.0
    )

    val calculators: List<Calculator> = listOf(
        Calculator(
            "natural_units", Topic.QFT, "Natürliche Einheiten (ħ = c = k_B = 1)",
            "In der Teilchenphysik werden alle Größen in Potenzen einer Energie gemessen. Die Brücke ins SI-System bilden ħc = 197,327 MeV fm und ħ = 6,582·10⁻²² MeV s. Auch die Schwinger-Feldstärke, ab der das Vakuum Elektron-Positron-Paare „siedet“, folgt so.",
            "[Länge] = [Zeit] = E⁻¹,   [Masse] = E,   L = ħc/E,   t = ħ/E,   T = E/k_B,   E_S = mₑ²c³/(eħ)",
            listOf(Param("E", "Energie E", "GeV", 1.0, 1e-30, 1e30))
        ) { v ->
            val e = v.getValue("E")
            val eMeV = e * 1e3
            CalcResult(listOf(
                Output("Länge ħc/E", Phys.hbarcMeVfm / eMeV, "fm"), Output("Zeit ħ/E", Phys.hbarMeVs / eMeV, "s"),
                Output("Masse E/c²", e * 1e9 * Phys.e / (Phys.c * Phys.c), "kg"), Output("Temperatur E/k_B", e * 1e9 * Phys.e / Phys.kB, "K"),
                Output("Wellenzahl E/(ħc)", eMeV / Phys.hbarcMeVfm, "fm⁻¹"), Output("Querschnitt 1/E² in mb", Phys.hbarc2GeV2mb / (e * e), "mb"),
                Output("Schwinger-Feldstärke (fest)", (Phys.me * Phys.c * Phys.c).pow(2) / (Phys.e * Phys.hbar * Phys.c), "V/m")
            ), steps = listOf("1 GeV⁻¹ = 0,1973 fm = 6,582·10⁻²⁵ s;  1 GeV⁻² = 0,3894 mb"))
        },
        Calculator(
            "kinematics", Topic.QFT, "Relativistische Kinematik",
            "Vierervektoren p^μ = (E, p): Die invariante Masse p² = E² − p² = m² ist in jedem Inertialsystem gleich. Für Speicherring (symmetrisch) und festes Target folgt die Schwerpunktsenergie √s; der Zwei-Körper-Zerfall hat einen festen Impuls p*.",
            "E² = p² + m²,   γ = E/m,   β = p/E,   y = ½ ln((E+p_z)/(E−p_z)),   √s_Collider = 2E_Strahl,   √s_fest = √(m₁² + m₂² + 2E₁m₂)",
            listOf(
                Param("part", "Teilchen", "", 4.0, choices = PARTICLES.map { it.first }),
                Param("p", "Impuls p", "GeV", 7000.0, 0.0, 1e9), Param("M", "Mutterteilchen M (Zerfall)", "GeV", 91.1876, 0.0, 1e6),
                Param("m1", "Tochter m₁", "GeV", 0.10566, 0.0, 1e6), Param("m2", "Tochter m₂", "GeV", 0.10566, 0.0, 1e6)
            )
        ) { v ->
            val (name, mMeV) = PARTICLES[v.getValue("part").toInt()]
            val m = mMeV / 1e3; val p = v.getValue("p")
            val e = sqrt(p * p + m * m)
            val beta = if (e > 0) p / e else 0.0
            CalcResult(listOf(
                Output("Energie E von $name", e, "GeV"), Output("Lorentzfaktor γ", e / m, ""), Output("1 − β", if (beta > 0.999) m * m / (e * (e + p)) else 1 - beta, ""),
                Output("Rapidität y", atanh(beta.coerceAtMost(1 - 1e-16)), ""),
                Output("√s Collider (2 gleiche Strahlen)", 2 * e, "GeV"),
                Output("√s gegen ruhendes Proton", sqrt(m * m + (Phys.mpMeV / 1e3).pow(2) + 2 * e * Phys.mpMeV / 1e3), "GeV"),
                Output("Zerfallsimpuls p*", twoBodyMomentum(v.getValue("M"), v.getValue("m1"), v.getValue("m2")), "GeV")
            ))
        },
        Calculator(
            "yukawa", Topic.QFT, "Yukawa-Potential und Reichweite",
            "Der Austausch eines massiven Teilchens der Masse m erzeugt ein abgeschirmtes Potential; seine Reichweite ist die reduzierte Compton-Wellenlänge ħ/(mc). Yukawa sagte 1935 so das Pion (≈ 140 MeV) als Träger der Kernkraft voraus (Reichweite ≈ 1,4 fm).",
            "(□ + m²)φ = −g δ³(x)   ⇒   V(r) = −g²/(4π) · e^{−mr}/r,   Reichweite R = ħ/(mc) = ħc/(mc²)",
            listOf(Param("m", "Austauschmasse mc²", "MeV", 139.57, 1e-9, 1e7), Param("g2", "Kopplung g²/4π", "", 14.0, 1e-9, 1e3, hint = "Pion-Nukleon ≈ 14"))
        ) { v ->
            val m = v.getValue("m"); val g = v.getValue("g2")
            val range = Phys.hbarcMeVfm / m
            val rs = grid(0.2, 5 * range, 200)
            CalcResult(
                listOf(Output("Reichweite ħ/(mc)", range, "fm"), Output("Potential bei r = R", -g * Phys.hbarcMeVfm * exp(-1.0) / range, "MeV"), Output("Unschärfe-Abschätzung Δt = ħ/(mc²)", Phys.hbarMeVs / m, "s")),
                listOf(Curve("Yukawa gegen Coulomb-artiges 1/r", "r in fm", "V in MeV", listOf(
                    CurveSeries("Yukawa", rs, rs.map { -g * Phys.hbarcMeVfm * exp(-it / range) / it }),
                    CurveSeries("masselos (1/r)", rs, rs.map { -g * Phys.hbarcMeVfm / it }, dashed = true)
                ), -g * Phys.hbarcMeVfm / range * 2 to 0.0))
            )
        },
        Calculator(
            "running", Topic.QFT, "Laufende Kopplungen: QED und QCD",
            "Vakuumpolarisation macht Kopplungen energieabhängig. In der QED schirmen virtuelle Paare die Ladung ab: α wächst von 1/137 auf ≈ 1/128 bei M_Z. In der QCD dominieren Gluonschleifen (b₀ = 11 − 2n_f/3 > 0): α_s fällt mit Q — asymptotische Freiheit (Nobelpreis 2004).",
            "1/α(Q) = 1/α(0) − (2/3π) Σ_f N_c Q_f² ln(Q/m_f),   α_s(Q) = α_s(μ) / [1 + α_s(μ) b₀ ln(Q²/μ²)/(4π)]",
            listOf(Param("Q", "Skala Q", "GeV", 91.1876, 0.5, 1e5), Param("asMz", "α_s(M_Z)", "", 0.1179, 0.05, 0.2))
        ) { v ->
            val q = v.getValue("Q"); val a = v.getValue("asMz")
            val qs = logGrid(1.0, 1e4, 200)
            CalcResult(
                listOf(Output("1/α(Q)", 1 / alphaQed(q), ""), Output("α_s(Q)", alphaS(q, a), ""), Output("Λ_QCD (1-Loop, n_f = 5)", 91.1876 * exp(-2 * PI / ((11 - 10.0 / 3) * a)) * 1e3, "MeV")),
                listOf(
                    Curve("α_s(Q)", "log₁₀(Q/GeV)", "α_s", listOf(CurveSeries("1-Loop mit Schwellen", qs.map { log10(it) }, qs.map { alphaS(it, a) })), 0.0 to 0.55),
                    Curve("1/α(Q)", "log₁₀(Q/GeV)", "1/α", listOf(CurveSeries("QED", qs.map { log10(it) }, qs.map { 1 / alphaQed(it) })))
                )
            )
        },
        Calculator(
            "ee_mumu", Topic.QFT, "e⁺e⁻ → μ⁺μ⁻ und das R-Verhältnis",
            "Der Prozess niedrigster Ordnung der QED (ein virtuelles Photon im s-Kanal). Das Verhältnis R von Hadronen- zu Myon-Paaren zählt Farben und Quarkladungen: R = N_c Σ Q_q² — der klassische Nachweis von drei Farben.",
            "dσ/dΩ = α²/(4s) (1 + cos²θ),   σ = 4πα²/(3s) ≈ 86,8 nb / s[GeV²],   R = N_c Σ_q Q_q²",
            listOf(Param("rs", "Schwerpunktsenergie √s", "GeV", 10.0, 0.25, 80.0))
        ) { v ->
            val rs = v.getValue("rs"); val s = rs * rs
            val sigmaNb = 4 * PI * Phys.alpha.pow(2) / (3 * s) * Phys.hbarc2GeV2mb * 1e6
            val active = QUARKS.filter { 2 * it.first < rs }
            val r = 3 * active.sumOf { it.second * it.second }
            val rss = grid(1.0, 60.0, 200)
            val ths = grid(0.0, PI, 120)
            CalcResult(
                listOf(Output("σ(μ⁺μ⁻)", sigmaNb, "nb"), Output("R (Partonmodell)", r, ""), Output("R mit QCD-Korrektur (1 + α_s/π)", r * (1 + alphaS(rs) / PI), ""), Output("σ(Hadronen) ≈ Rσ", r * sigmaNb, "nb")),
                listOf(
                    Curve("Wirkungsquerschnitt", "√s in GeV", "log₁₀(σ/nb)", listOf(CurveSeries("σ ∝ 1/s", rss, rss.map { log10(4 * PI * Phys.alpha.pow(2) / (3 * it * it) * Phys.hbarc2GeV2mb * 1e6) }))),
                    Curve("Winkelverteilung", "θ in Grad", "(1 + cos²θ)", listOf(CurveSeries("dσ/dΩ normiert", ths.map { it * 180 / PI }, ths.map { 1 + cos(it).pow(2) })))
                ),
                listOf("Aktive Quarks bei √s = ${Fmt.num(rs)} GeV: ${active.size} (Schwellen grob bei 2m_q)")
            )
        },
        Calculator(
            "muon_decay", Topic.QFT, "Myonzerfall und Fermi-Theorie",
            "Der Myonzerfall μ → e ν̄_e ν_μ ist der sauberste schwache Prozess; aus der Lebensdauer folgt die Fermi-Konstante. Dank Zeitdilatation erreichen kosmische Myonen den Erdboden.",
            "Γ = G_F² m_μ⁵/(192π³) (1 + Korrekturen),   τ = ħ/Γ,   Laborreichweite L = γβcτ",
            listOf(Param("E", "Myonenergie E", "GeV", 4.0, 0.106, 1e6), Param("h", "Erzeugungshöhe", "km", 15.0, 0.0, 100.0))
        ) { v ->
            val mmu = Phys.mmuMeV / 1e3
            val gamma = Phys.GF * Phys.GF * mmu.pow(5) / (192 * PI.pow(3))
            val tau = Phys.hbarMeVs / (gamma * 1e3)
            val e = v.getValue("E"); val g = e / mmu; val beta = sqrt(1 - 1 / (g * g))
            val l = g * beta * Phys.c * 2.1969811e-6
            CalcResult(listOf(
                Output("Zerfallsbreite Γ", gamma, "GeV"), Output("τ (Born-Näherung)", tau * 1e6, "μs", "Messwert 2,1970 μs"),
                Output("Lorentzfaktor γ", g, ""), Output("Mittlere Flugstrecke γβcτ", l / 1e3, "km"),
                Output("Überlebenswahrscheinlichkeit bis zum Boden", exp(-v.getValue("h") * 1e3 / l), ""),
                Output("Ohne Zeitdilatation", exp(-v.getValue("h") * 1e3 / (beta * Phys.c * 2.1969811e-6)), "")
            ))
        },
        Calculator(
            "klein_nishina", Topic.QFT, "Compton-Streuung in der QED (Klein-Nishina)",
            "Exaktes Ergebnis der Baumgraphen-QED für γe⁻ → γe⁻. Für kleine Energien geht es in den klassischen Thomson-Querschnitt über, für E ≫ mₑc² fällt es wie ln(2k)/k.",
            "σ_KN = (3σ_T/4) { (1+k)/k³ [2k(1+k)/(1+2k) − ln(1+2k)] + ln(1+2k)/(2k) − (1+3k)/(1+2k)² },   k = E_γ/mₑc²",
            listOf(Param("E", "Photonenenergie E_γ", "MeV", 1.0, 1e-6, 1e6))
        ) { v ->
            val k = v.getValue("E") / Phys.meMeV
            val ks = logGrid(1e-3, 1e3, 200)
            CalcResult(
                listOf(Output("σ_KN/σ_T", kleinNishinaRatio(k), ""), Output("σ_KN", kleinNishinaRatio(k) * Phys.sigmaT * 1e28, "barn"), Output("Mittlere freie Weglänge in Wasser (nur Compton)", 1 / (kleinNishinaRatio(k) * Phys.sigmaT * 3.343e29) * 100, "cm")),
                listOf(Curve("σ/σ_T gegen Photonenenergie", "log₁₀(E_γ/mₑc²)", "σ/σ_T", listOf(CurveSeries("Klein-Nishina", ks.map { log10(it) }, ks.map { kleinNishinaRatio(it) })), 0.0 to 1.05))
            )
        },
        Calculator(
            "rutherford", Topic.QFT, "Rutherford- und Mott-Streuung",
            "Coulomb-Streuung eines geladenen Teilchens an einem Kern. Der Mott-Faktor (1 − β² sin²(θ/2)) berücksichtigt den Elektronenspin; der Formfaktor eines ausgedehnten Kerns ist hier vernachlässigt.",
            "dσ/dΩ = (zZαħc / 4E_kin)² / sin⁴(θ/2)   (nichtrelativistisch),   Mott: × (1 − β² sin²(θ/2))",
            listOf(Param("Ek", "Kinetische Energie", "MeV", 5.0, 1e-6, 1e6), Param("Z", "Kernladung Z", "", 79.0, 1.0, 120.0), Param("z", "Projektilladung z", "", 2.0, 1.0, 100.0), Param("theta", "Winkel θ", "°", 30.0, 0.5, 180.0))
        ) { v ->
            val ek = v.getValue("Ek"); val zz = v.getValue("Z"); val z = v.getValue("z"); val th = v.getValue("theta") * PI / 180
            val a = z * zz * Phys.alpha * Phys.hbarcMeVfm / (4 * ek)
            fun ds(t: Double) = a * a / sin(t / 2).pow(4) * 10 // fm² → mb
            val ths = grid(5.0, 180.0, 200)
            CalcResult(
                listOf(Output("dσ/dΩ", ds(th), "mb/sr"), Output("Minimaler Abstand (θ = 180°) zZαħc/E", z * zz * Phys.alpha * Phys.hbarcMeVfm / ek, "fm"), Output("Stoßparameter b = (d/2) cot(θ/2)", z * zz * Phys.alpha * Phys.hbarcMeVfm / ek / 2 / kotlin.math.tan(th / 2), "fm")),
                listOf(Curve("Winkelverteilung", "θ in Grad", "log₁₀(dσ/dΩ / (mb/sr))", listOf(CurveSeries("Rutherford", ths, ths.map { log10(ds(it * PI / 180)) }))))
            )
        },
        Calculator(
            "dispersion", Topic.QFT, "Klein-Gordon-Dispersion: Phasen- und Gruppengeschwindigkeit",
            "Ebene Wellen e^{−i(Et − p·x)} lösen die Klein-Gordon-Gleichung, wenn E² = p² + m². Die Gruppengeschwindigkeit (Signal, Teilchen) bleibt unter c, die Phasengeschwindigkeit liegt darüber; ihr Produkt ist c².",
            "(□ + m²)φ = 0,   E(p) = √(p² + m²),   v_g = dE/dp = p/E < 1,   v_ph = E/p > 1,   v_g v_ph = 1",
            listOf(Param("m", "Masse m", "MeV", Phys.meMeV, 0.0, 1e6), Param("p", "Impuls p", "MeV", 1.0, 1e-9, 1e9))
        ) { v ->
            val m = v.getValue("m"); val p = v.getValue("p")
            val e = sqrt(p * p + m * m)
            val ps = grid(0.0, 4 * max(m, p), 200)
            CalcResult(
                listOf(Output("Energie E", e, "MeV"), Output("v_g/c", p / e, ""), Output("v_ph/c", e / p, ""), Output("De-Broglie-Wellenlänge 2πħc/p", 2 * PI * Phys.hbarcMeVfm / p, "fm")),
                listOf(Curve("Dispersion E(p)", "p in MeV", "E in MeV", listOf(CurveSeries("E = √(p² + m²)", ps, ps.map { sqrt(it * it + m * m) }), CurveSeries("Lichtkegel E = p", ps, ps, dashed = true))))
            )
        }
    )
}

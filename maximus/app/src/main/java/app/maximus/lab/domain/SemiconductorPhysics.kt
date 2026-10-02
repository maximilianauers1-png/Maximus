package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Semiconductor material parameters at 300 K (textbook values after Sze & Ng, "Physics of Semiconductor
 * Devices", and Ioffe NSM data; rounded). Band gap temperature dependence after Varshni:
 *   E_g(T) = E_g(0) − αT²/(T + β).
 * Effective densities of states scale as T^{3/2}.
 */
data class Semiconductor(
    val name: String,
    val eg0: Double, val alpha: Double, val beta: Double,
    val nc300: Double, val nv300: Double,
    val muN: Double, val muP: Double,
    val epsR: Double,
    val direct: Boolean
) {
    fun eg(t: Double) = eg0 - alpha * t * t / (t + beta)
    fun nc(t: Double) = nc300 * (t / 300).pow(1.5)
    fun nv(t: Double) = nv300 * (t / 300).pow(1.5)
    /** Intrinsic carrier density in cm⁻³: nᵢ = √(N_C N_V) exp(−E_g/2k_BT). */
    fun ni(t: Double) = sqrt(nc(t) * nv(t)) * exp(-eg(t) / (2 * SemiconductorPhysics.kT(t)))
}

object SemiconductorPhysics {
    /** Thermal energy k_BT in eV. */
    fun kT(t: Double) = Phys.kB * t / Phys.e

    val MATERIALS = listOf(
        Semiconductor("Silizium (Si)", 1.170, 4.73e-4, 636.0, 2.8e19, 1.04e19, 1400.0, 450.0, 11.7, false),
        Semiconductor("Germanium (Ge)", 0.7437, 4.774e-4, 235.0, 1.04e19, 6.0e18, 3900.0, 1900.0, 16.0, false),
        Semiconductor("Galliumarsenid (GaAs)", 1.519, 5.405e-4, 204.0, 4.7e17, 7.0e18, 8500.0, 400.0, 12.9, true),
        Semiconductor("4H-Siliziumkarbid (SiC)", 3.29, 3.3e-4, 0.0, 1.7e19, 2.5e19, 900.0, 120.0, 9.7, false),
        Semiconductor("Galliumnitrid (GaN)", 3.47, 7.7e-4, 600.0, 2.3e18, 4.6e19, 1000.0, 30.0, 8.9, true)
    )

    /** Fermi–Dirac occupation f(E) = 1/(1 + e^{(E−E_F)/k_BT}). */
    fun fermi(eMinusEf: Double, t: Double): Double = 1 / (1 + exp(eMinusEf / kT(t)))

    /**
     * Carrier densities with full ionisation from charge neutrality n − p = N_D − N_A and np = nᵢ²:
     *   n = (N_D − N_A)/2 + √(((N_D − N_A)/2)² + nᵢ²).
     * Written to avoid cancellation for p-type material.
     */
    fun carriers(nd: Double, na: Double, ni: Double): Pair<Double, Double> {
        val h = (nd - na) / 2
        val root = sqrt(h * h + ni * ni)
        return if (h >= 0) { val n = h + root; n to ni * ni / n } else { val p = -h + root; ni * ni / p to p }
    }

    /** Built-in voltage of an abrupt pn junction: V_bi = (k_BT/e) ln(N_A N_D / nᵢ²). */
    fun builtIn(na: Double, nd: Double, ni: Double, t: Double) = kT(t) * ln(na * nd / (ni * ni))

    /** Depletion width (cm) W = √(2ε(V_bi − V)/e · (N_A + N_D)/(N_A N_D)) with densities in cm⁻³. */
    fun depletionWidth(epsR: Double, vbi: Double, v: Double, na: Double, nd: Double): Double {
        val eps = epsR * Phys.eps0 / 100 // F/cm
        return sqrt(2 * eps * max(1e-6, vbi - v) / Phys.e * (na + nd) / (na * nd))
    }

    /**
     * Diode current with series resistance: I = I_s (exp((V − IR_s)/(n V_T)) − 1), solved for I by
     * Newton's method on g(I) = I_s(e^{(V−IR)/nV_T} − 1) − I (monotone, so it converges from I = 0 with damping).
     */
    fun diodeCurrent(v: Double, iS: Double, n: Double, t: Double, rs: Double): Double {
        val vt = kT(t)
        if (rs <= 0) return iS * (exp(v / (n * vt)) - 1)
        // Start from the smaller of the ideal diode current and the resistor limit.
        var i = minOf(iS * (exp(minOf(v / (n * vt), 200.0)) - 1), max(0.0, v / rs))
        repeat(100) {
            val e = exp(minOf((v - i * rs) / (n * vt), 300.0))
            val g = iS * (e - 1) - i
            val dg = -iS * e * rs / (n * vt) - 1
            val step = g / dg
            i -= step
            if (abs(step) < 1e-15 + 1e-12 * abs(i)) return i
        }
        return i
    }

    val calculators: List<Calculator> = listOf(
        Calculator(
            "intrinsic", Topic.SEMICONDUCTOR, "Bandlücke und intrinsische Ladungsträgerdichte",
            "Die Eigenleitungsdichte wächst exponentiell mit T. Im Arrhenius-Plot (log nᵢ über 1000/T) ist die Steigung ∝ −E_g/2k_B. Die Bandlücke schrumpft mit der Temperatur (Varshni).",
            "nᵢ = √(N_C N_V) e^{−E_g/2k_BT},   N_{C,V} ∝ T^{3/2},   E_g(T) = E_g(0) − αT²/(T + β)",
            listOf(Param("mat", "Material", "", 0.0, choices = MATERIALS.map { it.name }), Param("T", "Temperatur T", "K", 300.0, 1.0, 2000.0))
        ) { v ->
            val m = MATERIALS[v.getValue("mat").toInt()]; val t = v.getValue("T")
            val ts = grid(200.0, 800.0, 150)
            CalcResult(
                listOf(
                    Output("E_g(T)", m.eg(t), "eV"), Output("Absorptionskante λ = hc/E_g", 1239.84198 / m.eg(t), "nm", if (m.direct) "direkte Lücke" else "indirekte Lücke"),
                    Output("N_C", m.nc(t), "cm⁻³"), Output("N_V", m.nv(t), "cm⁻³"), Output("nᵢ", m.ni(t), "cm⁻³"),
                    Output("Intrinsischer spez. Widerstand", 1 / (Phys.e * m.ni(t) * (m.muN + m.muP)), "Ω cm"),
                    Output("Intrinsisches Niveau E_i − E_mitte = (k_BT/2) ln(N_V/N_C)", kT(t) / 2 * ln(m.nv(t) / m.nc(t)) * 1e3, "meV")
                ),
                listOf(Curve("Arrhenius-Darstellung", "1000/T in K⁻¹", "log₁₀(nᵢ/cm⁻³)", MATERIALS.map { mm ->
                    CurveSeries(mm.name.substringBefore(" ("), ts.map { 1000 / it }, ts.map { log10(mm.ni(it)) }, dashed = mm != m)
                })),
                listOf("nᵢ = √(${Fmt.num(m.nc(t))}·${Fmt.num(m.nv(t))}) · exp(−${Fmt.num(m.eg(t))} eV / (2·${Fmt.num(kT(t))} eV)) = ${Fmt.num(m.ni(t))} cm⁻³")
            )
        },
        Calculator(
            "doping", Topic.SEMICONDUCTOR, "Dotierung, Fermi-Niveau und Leitfähigkeit",
            "Bei vollständiger Ionisation legt die Netto-Dotierung die Majoritätsdichte fest; das Massenwirkungsgesetz np = nᵢ² die Minoritäten. Bei hohen Temperaturen wird jeder Halbleiter wieder intrinsisch.",
            "n − p = N_D − N_A,   np = nᵢ²,   E_F − E_i = k_BT ln(n/nᵢ),   σ = e(nμ_n + pμ_p)",
            listOf(
                Param("mat", "Material", "", 0.0, choices = MATERIALS.map { it.name }),
                Param("ND", "Donatoren N_D", "cm⁻³", 1e16, 0.0, 1e22), Param("NA", "Akzeptoren N_A", "cm⁻³", 0.0, 0.0, 1e22),
                Param("T", "Temperatur T", "K", 300.0, 1.0, 2000.0)
            )
        ) { v ->
            val m = MATERIALS[v.getValue("mat").toInt()]; val nd = v.getValue("ND"); val na = v.getValue("NA"); val t = v.getValue("T")
            val (n, p) = carriers(nd, na, m.ni(t))
            val sigma = Phys.e * (n * m.muN + p * m.muP)
            val ts = grid(250.0, 1000.0, 200)
            CalcResult(
                listOf(
                    Output("Elektronen n", n, "cm⁻³"), Output("Löcher p", p, "cm⁻³"),
                    Output("Typ", 0.0, display = if (n > 10 * p) "n-leitend" else if (p > 10 * n) "p-leitend" else "nahezu intrinsisch"),
                    Output("E_F − E_i", kT(t) * ln(n / m.ni(t)), "eV"),
                    Output("E_C − E_F", kT(t) * ln(m.nc(t) / n), "eV", "> 3k_BT: nicht entartet"),
                    Output("Leitfähigkeit σ", sigma, "S/cm"), Output("Spez. Widerstand ρ", 1 / sigma, "Ω cm"),
                    Output("Diffusionskonstante D_n = μ_n k_BT/e", m.muN * kT(t), "cm²/s", "Einstein-Relation")
                ),
                listOf(Curve("Ladungsträger über der Temperatur", "T in K", "log₁₀(Dichte/cm⁻³)", listOf(
                    CurveSeries("n", ts, ts.map { log10(carriers(nd, na, m.ni(it)).first) }),
                    CurveSeries("p", ts, ts.map { log10(carriers(nd, na, m.ni(it)).second) }, dashed = true),
                    CurveSeries("nᵢ", ts, ts.map { log10(m.ni(it)) }, dashed = true)
                )))
            )
        },
        Calculator(
            "fermi_dirac", Topic.SEMICONDUCTOR, "Fermi-Dirac-Verteilung",
            "Besetzungswahrscheinlichkeit eines Zustands der Energie E. Die Verteilung ist punktsymmetrisch um E_F; mehr als ≈ 3k_BT oberhalb von E_F geht sie in die Boltzmann-Verteilung über.",
            "f(E) = 1/(1 + e^{(E−E_F)/k_BT}),   für E − E_F ≫ k_BT:  f ≈ e^{−(E−E_F)/k_BT},   −∂f/∂E hat die Breite ≈ 3,5 k_BT",
            listOf(Param("T", "Temperatur T", "K", 300.0, 0.1, 1e5), Param("dE", "E − E_F", "meV", 50.0, -1e4, 1e4))
        ) { v ->
            val t = v.getValue("T"); val de = v.getValue("dE") / 1000
            val es = grid(-0.3, 0.3, 300)
            CalcResult(
                listOf(Output("k_BT", kT(t) * 1e3, "meV"), Output("f(E)", fermi(de, t), ""), Output("Boltzmann-Näherung", exp(-de / kT(t)), "", if (de < 3 * kT(t)) "hier ungenau" else "gut"), Output("Lochbesetzung 1 − f", 1 - fermi(de, t), "")),
                listOf(Curve("f(E) bei verschiedenen Temperaturen", "E − E_F in eV", "f", listOf(
                    CurveSeries("T", es, es.map { fermi(it, t) }),
                    CurveSeries("T/4", es, es.map { fermi(it, t / 4) }, dashed = true),
                    CurveSeries("4T", es, es.map { fermi(it, 4 * t) }, dashed = true)
                ), -0.02 to 1.05))
            )
        },
        Calculator(
            "pn", Topic.SEMICONDUCTOR, "pn-Übergang (Schottky-Näherung)",
            "Abrupter Übergang mit vollständiger Verarmung: Die Raumladungszone ragt weiter in die schwächer dotierte Seite (N_A x_p = N_D x_n). Feld dreieckförmig, Potential parabelförmig.",
            "V_bi = (k_BT/e) ln(N_A N_D/nᵢ²),   W = √(2ε(V_bi − V)/e · (N_A + N_D)/(N_A N_D)),   E_max = 2(V_bi − V)/W,   C_j = ε/W",
            listOf(
                Param("mat", "Material", "", 0.0, choices = MATERIALS.map { it.name }),
                Param("NA", "N_A (p-Seite)", "cm⁻³", 1e17, 1e10, 1e21), Param("ND", "N_D (n-Seite)", "cm⁻³", 1e15, 1e10, 1e21),
                Param("V", "Angelegte Spannung V", "V", 0.0, -1000.0, 2.0, hint = "negativ = Sperrrichtung"), Param("T", "Temperatur T", "K", 300.0, 10.0, 1000.0)
            )
        ) { v ->
            val m = MATERIALS[v.getValue("mat").toInt()]; val na = v.getValue("NA"); val nd = v.getValue("ND"); val vv = v.getValue("V"); val t = v.getValue("T")
            val vbi = builtIn(na, nd, m.ni(t), t)
            val w = depletionWidth(m.epsR, vbi, vv, na, nd)
            val xn = w * na / (na + nd); val xp = w * nd / (na + nd)
            val eMax = 2 * max(1e-6, vbi - vv) / w
            val eps = m.epsR * Phys.eps0 / 100
            val xs = grid(-1.5 * xp, 1.5 * xn, 300)
            fun field(x: Double) = when {
                x < -xp || x > xn -> 0.0
                x < 0 -> eMax * (x + xp) / xp
                else -> eMax * (xn - x) / xn
            }
            fun potential(x: Double): Double = when {
                x <= -xp -> 0.0
                x < 0 -> eMax / (2 * xp) * (x + xp).pow(2)
                x < xn -> (vbi - vv) - eMax / (2 * xn) * (xn - x).pow(2)
                else -> vbi - vv
            }
            val warn = if (vv >= vbi) listOf("V ≥ V_bi: Schottky-Näherung bricht zusammen (Hochinjektion).") else emptyList()
            CalcResult(
                listOf(
                    Output("Diffusionsspannung V_bi", vbi, "V"), Output("Weite W", w * 1e4, "μm"), Output("x_n", xn * 1e4, "μm"), Output("x_p", xp * 1e4, "μm"),
                    Output("Maximales Feld |E_max|", eMax / 1e3, "kV/cm"), Output("Sperrschichtkapazität C_j", eps / w * 1e9, "nF/cm²"),
                    Output("Debye-Länge n-Seite √(εk_BT/(e²N_D))", sqrt(eps * kT(t) / (Phys.e * nd)) * 1e4, "μm")
                ),
                listOf(
                    Curve("Elektrisches Feld", "x in μm", "|E| in kV/cm", listOf(CurveSeries("|E(x)|", xs.map { it * 1e4 }, xs.map { field(it) / 1e3 }))),
                    Curve("Elektrostatisches Potential", "x in μm", "φ in V", listOf(CurveSeries("φ(x)", xs.map { it * 1e4 }, xs.map { potential(it) })))
                ),
                listOf("V_bi = ${Fmt.num(kT(t))} V · ln(${Fmt.num(na)}·${Fmt.num(nd)}/${Fmt.num(m.ni(t))}²) = ${Fmt.num(vbi)} V"),
                warn
            )
        },
        Calculator(
            "diode", Topic.SEMICONDUCTOR, "Diodenkennlinie (Shockley)",
            "Exponentielle Kennlinie mit Idealitätsfaktor n und Serienwiderstand R_s (implizite Gleichung, Newton-Verfahren). Pro Dekade Strom steigt die Spannung um n·ln10·k_BT/e ≈ n·60 mV.",
            "I = I_s (e^{(V − IR_s)/nV_T} − 1),   V_T = k_BT/e,   r_d = nV_T/I",
            listOf(
                Param("Is", "Sättigungsstrom I_s", "A", 1e-12, 1e-30, 1.0), Param("n", "Idealitätsfaktor n", "", 1.0, 0.5, 5.0),
                Param("Rs", "Serienwiderstand R_s", "Ω", 1.0, 0.0, 1e6), Param("T", "Temperatur T", "K", 300.0, 10.0, 800.0), Param("V", "Spannung V", "V", 0.65, -10.0, 5.0)
            )
        ) { v ->
            val iS = v.getValue("Is"); val n = v.getValue("n"); val rs = v.getValue("Rs"); val t = v.getValue("T"); val vv = v.getValue("V")
            val i = diodeCurrent(vv, iS, n, t, rs)
            val vs = grid(0.0, 1.0, 250)
            CalcResult(
                listOf(Output("Strom I", i, "A"), Output("V_T", kT(t) * 1e3, "mV"), Output("Diff. Widerstand r_d", if (i > 0) n * kT(t) / i + rs else Double.POSITIVE_INFINITY, "Ω"),
                    Output("Spannung pro Dekade", n * ln(10.0) * kT(t) * 1e3, "mV"), Output("Spannung für 1 mA (ohne R_s)", n * kT(t) * ln(1e-3 / iS + 1), "V")),
                listOf(
                    Curve("Kennlinie", "V in V", "I in mA", listOf(
                        CurveSeries("mit R_s", vs, vs.map { diodeCurrent(it, iS, n, t, rs) * 1e3 }),
                        CurveSeries("ideal", vs, vs.map { iS * (exp(it / (n * kT(t))) - 1) * 1e3 }, dashed = true)
                    ), -1.0 to 100.0),
                    Curve("Halblogarithmisch", "V in V", "log₁₀(I/A)", listOf(CurveSeries("log I", vs.drop(1), vs.drop(1).map { log10(max(1e-30, diodeCurrent(it, iS, n, t, rs))) })))
                )
            )
        },
        Calculator(
            "solar", Topic.SEMICONDUCTOR, "Solarzelle",
            "Eine beleuchtete Diode: Der Photostrom verschiebt die Kennlinie nach unten. Leerlaufspannung, Kurzschlussstrom, Füllfaktor und Wirkungsgrad folgen aus der Ein-Dioden-Kennlinie. Grenze für Si nach Shockley-Queisser ≈ 33 %.",
            "I = I_L − I_0 (e^{V/nV_T} − 1),   V_oc = nV_T ln(I_L/I_0 + 1),   FF = P_max/(V_oc I_sc),   η = P_max/(E·A)",
            listOf(
                Param("E", "Bestrahlungsstärke E", "W/m²", 1000.0, 0.0, 1e5), Param("A", "Fläche A", "cm²", 100.0, 1e-6, 1e8),
                Param("jsc", "Kurzschlussstromdichte bei 1000 W/m²", "mA/cm²", 40.0, 0.0, 100.0),
                Param("j0", "Sperrsättigungsstromdichte j₀", "A/cm²", 1e-12, 1e-30, 1e-3), Param("n", "Idealität n", "", 1.0, 0.5, 3.0), Param("T", "Zelltemperatur T", "K", 298.0, 200.0, 400.0)
            )
        ) { v ->
            val area = v.getValue("A"); val il = v.getValue("jsc") * 1e-3 * area * v.getValue("E") / 1000
            val i0 = v.getValue("j0") * area; val n = v.getValue("n"); val t = v.getValue("T")
            val vt = n * kT(t)
            val voc = vt * ln(il / i0 + 1)
            val vs = grid(0.0, voc, 300)
            val iv = vs.map { il - i0 * (exp(it / vt) - 1) }
            val pv = vs.indices.map { vs[it] * iv[it] }
            val k = pv.indices.maxByOrNull { pv[it] } ?: 0
            val pin = v.getValue("E") * area * 1e-4
            CalcResult(
                listOf(
                    Output("Kurzschlussstrom I_sc", il, "A"), Output("Leerlaufspannung V_oc", voc, "V"),
                    Output("MPP-Spannung", vs[k], "V"), Output("MPP-Strom", iv[k], "A"), Output("Maximale Leistung", pv[k], "W"),
                    Output("Füllfaktor FF", if (il > 0) pv[k] / (voc * il) else 0.0, ""), Output("Wirkungsgrad η", if (pin > 0) 100 * pv[k] / pin else 0.0, "%")
                ),
                listOf(Curve("Kennlinie und Leistung", "V in V", "I in A / P in W", listOf(CurveSeries("I(V)", vs, iv), CurveSeries("P(V)", vs, pv, dashed = true))))
            )
        },
        Calculator(
            "mosfet", Topic.SEMICONDUCTOR, "MOSFET (Quadratisches Modell)",
            "n-Kanal-MOSFET mit Langkanal-Näherung und Kanallängenmodulation λ. Unterhalb von V_th fließt (hier vernachlässigter) Subthreshold-Strom; im Sättigungsbereich ist I_D unabhängig von V_DS bis auf λ.",
            "C_ox = ε_ox/t_ox,   linear: I_D = μC_ox(W/L)[(V_GS−V_th)V_DS − V_DS²/2],   Sättigung: I_D = ½μC_ox(W/L)(V_GS−V_th)²(1 + λV_DS),   g_m = √(2μC_ox(W/L)I_D)",
            listOf(
                Param("mu", "Kanalbeweglichkeit μ_n", "cm²/(V s)", 400.0, 1.0, 1e5), Param("tox", "Oxiddicke t_ox", "nm", 10.0, 0.1, 1000.0),
                Param("W", "Weite W", "μm", 10.0, 0.01, 1e6), Param("L", "Länge L", "μm", 1.0, 0.01, 1e6),
                Param("Vth", "Schwellspannung V_th", "V", 0.7, -10.0, 10.0), Param("Vgs", "V_GS", "V", 2.0, -20.0, 20.0), Param("Vds", "V_DS", "V", 1.0, 0.0, 50.0),
                Param("lambda", "Kanallängenmodulation λ", "1/V", 0.02, 0.0, 1.0)
            )
        ) { v ->
            val cox = 3.9 * Phys.eps0 / (v.getValue("tox") * 1e-9) // F/m²
            val k = v.getValue("mu") * 1e-4 * cox * v.getValue("W") / v.getValue("L")
            val vth = v.getValue("Vth"); val lam = v.getValue("lambda")
            fun id(vgs: Double, vds: Double): Double {
                val ov = vgs - vth
                return when {
                    ov <= 0 -> 0.0
                    vds < ov -> k * (ov * vds - vds * vds / 2) * (1 + lam * vds)
                    else -> 0.5 * k * ov * ov * (1 + lam * vds)
                }
            }
            val ids = id(v.getValue("Vgs"), v.getValue("Vds"))
            val vds = grid(0.0, 5.0, 200)
            CalcResult(
                listOf(
                    Output("C_ox", cox * 1e3, "fF/μm²"), Output("μC_ox W/L", k * 1e3, "mA/V²"), Output("Drainstrom I_D", ids * 1e3, "mA"),
                    Output("Bereich", 0.0, display = when { v.getValue("Vgs") <= vth -> "gesperrt"; v.getValue("Vds") < v.getValue("Vgs") - vth -> "linear (Triode)"; else -> "Sättigung" }),
                    Output("Steilheit g_m", sqrt(2 * k * ids) * 1e3, "mS"), Output("Ausgangswiderstand r_o ≈ 1/(λI_D)", if (lam > 0 && ids > 0) 1 / (lam * ids) else Double.POSITIVE_INFINITY, "Ω")
                ),
                listOf(Curve("Ausgangskennlinienfeld", "V_DS in V", "I_D in mA", (1..4).map { s ->
                    val vgs = vth + 0.5 * s
                    CurveSeries("V_GS = ${Fmt.num(vgs, 3)} V", vds, vds.map { id(vgs, it) * 1e3 })
                }))
            )
        },
        Calculator(
            "hall", Topic.SEMICONDUCTOR, "Hall-Effekt",
            "Ein Magnetfeld senkrecht zum Strom lenkt die Ladungsträger ab, bis das Hall-Feld die Lorentzkraft kompensiert. Vorzeichen und Größe der Hall-Spannung liefern Trägertyp und -dichte; mit dem Widerstand folgt die Beweglichkeit.",
            "U_H = IB/(n e d),   R_H = 1/(ne),   μ = |R_H|/ρ = |R_H| σ",
            listOf(
                Param("I", "Strom I", "mA", 10.0, 1e-9, 1e6), Param("B", "Magnetfeld B", "T", 0.5, 1e-9, 100.0),
                Param("d", "Probendicke d", "μm", 500.0, 1e-3, 1e6), Param("UH", "Hall-Spannung U_H", "mV", 5.0, -1e6, 1e6, hint = "negativ = Elektronen"),
                Param("rho", "Spez. Widerstand ρ", "Ω cm", 1.0, 1e-9, 1e12)
            )
        ) { v ->
            val i = v.getValue("I") * 1e-3; val b = v.getValue("B"); val d = v.getValue("d") * 1e-6; val uh = v.getValue("UH") * 1e-3
            val n = i * b / (Phys.e * d * abs(uh)) // m⁻³
            val rh = 1 / (n * Phys.e) * if (uh < 0) -1 else 1
            CalcResult(listOf(
                Output("Ladungsträgerdichte", n * 1e-6, "cm⁻³"), Output("Hall-Konstante R_H", rh * 1e6, "cm³/C"),
                Output("Trägertyp", 0.0, display = if (uh < 0) "Elektronen (n-Typ)" else "Löcher (p-Typ)"),
                Output("Hall-Beweglichkeit μ_H", abs(rh * 1e6) / v.getValue("rho"), "cm²/(V s)")
            ), steps = listOf("n = IB/(e d |U_H|) = ${Fmt.num(i)}·${Fmt.num(b)}/(1,602·10⁻¹⁹·${Fmt.num(d)}·${Fmt.num(abs(uh))}) m⁻³"))
        }
    )
}

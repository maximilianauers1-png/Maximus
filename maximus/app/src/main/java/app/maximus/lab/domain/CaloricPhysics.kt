package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tanh

/**
 * Caloric effects: reversible temperature changes of a solid when an external field (electric E,
 * stress σ, magnetic H, pressure p) is applied adiabatically. Thermodynamic backbone:
 *   dG = −S dT − P dE − ε dσ − μ₀M dH + V dp   (per unit volume, sign of σ for tension)
 * gives the Maxwell relations (∂S/∂E)_T = (∂P/∂T)_E, (∂S/∂σ)_T = (∂ε/∂T)_σ, (∂S/∂H)_T = μ₀(∂M/∂T)_H,
 * (∂S/∂p)_T = −(∂V/∂T)_p. The adiabatic temperature change follows from S(T_f, field) = S(T_i, 0).
 */
object CaloricPhysics {

    // ------------------------------------------------------------------ Electrocaloric (Landau)

    /**
     * Landau–Devonshire free energy of BaTiO₃ for polarisation along [001] (Bell & Cross coefficients):
     *   G = α₁P² + α₁₁P⁴ + α₁₁₁P⁶ − EP,
     *   α₁ = 3.34·10⁵ (T − 381) J m C⁻²,  α₁₁ = 4.69·10⁶ (T − 393) − 2.02·10⁸,  α₁₁₁ = −5.52·10⁷ (T − 393) + 2.76·10⁹.
     * The transition is first order (α₁₁ < 0 near T_C), so P jumps at T_C ≈ 393 K.
     */
    object BaTiO3 {
        const val RHO = 6020.0
        const val CP = 434.0
        private const val A1T = 3.34e5
        private const val A11T = 4.69e6
        private const val A111T = -5.52e7
        fun a1(t: Double) = A1T * (t - 381)
        fun a11(t: Double) = A11T * (t - 393) - 2.02e8
        fun a111(t: Double) = A111T * (t - 393) + 2.76e9

        fun g(p: Double, e: Double, t: Double) = a1(t) * p * p + a11(t) * p.pow(4) + a111(t) * p.pow(6) - e * p

        /** Equilibrium polarisation (C/m²) for field E ≥ 0: the real root P ≥ 0 of ∂G/∂P = 0 with the lowest G. */
        fun polarization(e: Double, t: Double): Double {
            val roots = Polynomials.roots(listOf(-e, 2 * a1(t), 0.0, 4 * a11(t), 0.0, 6 * a111(t)))
                .filter { abs(it.im) < 1e-9 * max(1.0, abs(it.re)) && it.re >= -1e-12 }
                .map { max(0.0, it.re) }
            val candidates = roots + 0.0
            return candidates.minByOrNull { g(it, e, t) } ?: 0.0
        }

        /** Dipolar entropy per volume S = −∂G/∂T = −(A1T P² + A11T P⁴ + A111T P⁶)  (J m⁻³ K⁻¹). */
        fun dipolarEntropy(p: Double) = -(A1T * p * p + A11T * p.pow(4) + A111T * p.pow(6))

        /** Adiabatic ΔT for applying E at initial temperature T from entropy conservation with constant lattice heat capacity. */
        fun adiabaticDeltaT(e: Double, t: Double): Double {
            val cv = RHO * CP
            val s0 = dipolarEntropy(polarization(0.0, t))
            // Lattice: S_lat = C ln T. Solve C ln(T_f/T) + S_dip(E, T_f) − S_dip(0, T) = 0.
            val f = { tf: Double -> cv * ln(tf / t) + dipolarEntropy(polarization(e, tf)) - s0 }
            val tf = Calculus.brent(f, t - 5, t + 40) ?: return Double.NaN
            return tf - t
        }
    }

    // ------------------------------------------------------------------ Magnetocaloric (mean field)

    /** Brillouin function B_J(x) = ((2J+1)/2J) coth((2J+1)x/2J) − (1/2J) coth(x/2J). */
    fun brillouin(j: Double, x: Double): Double {
        if (abs(x) < 1e-6) return (j + 1) / (3 * j) * x
        val a = (2 * j + 1) / (2 * j); val b = 1 / (2 * j)
        return a / tanh(a * x) - b / tanh(b * x)
    }

    /**
     * Mean-field ferromagnet with spin J: reduced magnetisation m = B_J(x), x = gμ_BJ μ₀H/(k_BT) + 3J/(J+1) (T_C/T) m.
     * Magnetic entropy per mole: S_m = R[ln(sinh((2J+1)x/2J)/sinh(x/2J)) − x m].
     */
    data class MeanField(val j: Double, val g: Double, val tc: Double) {
        /** Self-consistent m from the positive root of m − B_J(x(m)) = 0 (Brent); m = 0 above T_C without field. */
        fun magnetization(b: Double, t: Double): Double {
            if (b <= 0 && t >= tc) return 0.0
            val h = g * Phys.muB * j * b / (Phys.kB * t)
            val k = 3 * j / (j + 1) * (tc / t)
            val f = { m: Double -> m - brillouin(j, h + k * m) }
            return Calculus.brent(f, 1e-12, 1.0, 1e-13) ?: 0.0
        }

        fun entropy(b: Double, t: Double): Double {
            val m = magnetization(b, t)
            val x = g * Phys.muB * j * b / (Phys.kB * t) + 3 * j / (j + 1) * (tc / t) * m
            if (abs(x) < 1e-8) return Phys.R * ln(2 * j + 1)
            val a = (2 * j + 1) / (2 * j); val c = 1 / (2 * j)
            // ln(sinh(a x)/sinh(c x)) computed stably for large x.
            val lnRatio = if (x > 50) (a - c) * x + ln((1 - exp(-2 * a * x)) / (1 - exp(-2 * c * x))) else ln(sinh(a * x) / sinh(c * x))
            return Phys.R * (lnRatio - x * m)
        }
    }

    /** Gadolinium: J = 7/2, g = 2, T_C = 293 K, Θ_D ≈ 184 K, molar mass 157.25 g/mol. */
    val GD = MeanField(3.5, 2.0, 293.0)
    private const val GD_DEBYE = 184.0

    /** Lattice entropy per mole from the Debye model, S = ∫₀ᵀ C_V/T' dT', tabulated once per Θ_D in 0.5 K steps up to 1000 K. */
    fun debyeEntropy(t: Double, thetaD: Double): Double {
        val table = debyeTables.getOrPut(thetaD) {
            val step = 0.5
            val n = 2000
            val out = DoubleArray(n + 1)
            // C_V ∝ T³ at low T, so S(T) = C_V(T)/3 there; start from 0.5 K and integrate with Simpson per step.
            out[1] = ThermoPhysics.debyeCv(step, thetaD) / 3
            for (k in 2..n) {
                val a = (k - 1) * step; val b = k * step
                val fa = ThermoPhysics.debyeCv(a, thetaD) / a; val fb = ThermoPhysics.debyeCv(b, thetaD) / b
                val fm = ThermoPhysics.debyeCv((a + b) / 2, thetaD) / ((a + b) / 2)
                out[k] = out[k - 1] + step / 6 * (fa + 4 * fm + fb)
            }
            out
        }
        val x = (t / 0.5).coerceIn(0.0, (table.size - 1).toDouble())
        val i = x.toInt().coerceAtMost(table.size - 2)
        val frac = x - i
        return table[i] * (1 - frac) + table[i + 1] * frac
    }

    private val debyeTables = java.util.concurrent.ConcurrentHashMap<Double, DoubleArray>()

    fun magnetocaloricDeltaT(model: MeanField, thetaD: Double, b: Double, t: Double, electronicGamma: Double = 6.4e-3): Double {
        // Electronic entropy γT (γ for Gd ≈ 6.4 mJ mol⁻¹ K⁻²).
        fun sTotal(field: Double, tt: Double) = model.entropy(field, tt) + debyeEntropy(tt, thetaD) + electronicGamma * tt
        val s0 = sTotal(0.0, t)
        val tf = Calculus.brent({ tf -> sTotal(b, tf) - s0 }, t - 5, t + 40, tol = 1e-6) ?: return Double.NaN
        return tf - t
    }

    // ------------------------------------------------------------------ Elastocaloric

    data class Sma(val name: String, val latentJg: Double, val cJgK: Double, val dSigmaDT: Double, val sigmaAtRt: Double, val width: Double, val rho: Double)
    private val SMAS = listOf(
        Sma("NiTi (superelastisch)", 15.0, 0.50, 6.5, 400.0, 150.0, 6450.0),
        Sma("Cu-Zn-Al", 7.0, 0.40, 2.0, 120.0, 60.0, 7900.0),
        Sma("Ni-Mn-Ti (all-d-metal Heusler)", 25.0, 0.48, 4.0, 300.0, 200.0, 7400.0),
        Sma("Naturkautschuk (Dehnungskristallisation)", 15.0, 1.9, 0.0, 2.0, 2.0, 930.0)
    )

    // ------------------------------------------------------------------ Calculators

    val calculators: List<Calculator> = listOf(
        Calculator(
            "ec_landau", Topic.CALORIC, "Elektrokalorik: Landau-Modell für BaTiO₃",
            "Ein elektrisches Feld ordnet die Dipole, die Dipolentropie sinkt; unter adiabatischen Bedingungen erwärmt sich der Kristall. Das Landau-Devonshire-Potential mit Bell-Cross-Koeffizienten beschreibt den Phasenübergang 1. Ordnung von BaTiO₃ bei ≈ 393 K. Größter Effekt knapp oberhalb von T_C.",
            "G = α₁P² + α₁₁P⁴ + α₁₁₁P⁶ − EP,   S_dip = −∂G/∂T,   ∫ C/T dT + ΔS_dip = 0  ⇒  ΔT_ad;   linear: ΔT ≈ −(T/ρc)∫(∂P/∂T)_E dE",
            listOf(Param("T", "Anfangstemperatur T", "K", 400.0, 300.0, 480.0), Param("E", "Feldstärke E", "kV/cm", 20.0, 0.0, 300.0))
        ) { v ->
            val t = v.getValue("T"); val e = v.getValue("E") * 1e5
            val p0 = BaTiO3.polarization(0.0, t); val p1 = BaTiO3.polarization(e, t)
            val dT = BaTiO3.adiabaticDeltaT(e, t)
            val ts = grid(370.0, 450.0, 120)
            val fields = listOf(10.0, 20.0, 40.0).map { it * 1e5 }
            CalcResult(
                listOf(
                    Output("Polarisation ohne Feld", p0 * 100, "μC/cm²"), Output("Polarisation mit Feld", p1 * 100, "μC/cm²"),
                    Output("ΔS_iso (Dipole)", BaTiO3.dipolarEntropy(p1) - BaTiO3.dipolarEntropy(p0), "J m⁻³ K⁻¹"),
                    Output("ΔS_iso pro Masse", (BaTiO3.dipolarEntropy(p1) - BaTiO3.dipolarEntropy(p0)) / BaTiO3.RHO, "J kg⁻¹ K⁻¹"),
                    Output("Adiabatisches ΔT", dT, "K"), Output("EC-Stärke ΔT/ΔE", if (e > 0) dT / (e / 1e5) * 1e3 else 0.0, "mK cm/kV")
                ),
                listOf(
                    Curve("ΔT_ad gegen Temperatur", "T in K", "ΔT in K", fields.map { f -> CurveSeries("E = ${Fmt.num(f / 1e5, 3)} kV/cm", ts, ts.map { BaTiO3.adiabaticDeltaT(f, it) }) }),
                    Curve("Spontane Polarisation (Phasenübergang 1. Ordnung)", "T in K", "P in μC/cm²", listOf(
                        CurveSeries("E = 0", ts, ts.map { BaTiO3.polarization(0.0, it) * 100 }),
                        CurveSeries("E gewählt", ts, ts.map { BaTiO3.polarization(e, it) * 100 }, dashed = true)
                    ))
                ),
                listOf("Lösung von 2α₁P + 4α₁₁P³ + 6α₁₁₁P⁵ = E über die Nullstellen des Polynoms (Begleitmatrix), Auswahl des Minimums von G.",
                    "ΔT aus Entropieerhaltung ρc·ln(T_f/T) + S_dip(E, T_f) − S_dip(0, T) = 0 (Brent-Verfahren).")
            )
        },
        Calculator(
            "ec_pyro", Topic.CALORIC, "Elektrokalorik: indirekte Methode (Dünnschichten)",
            "Aus gemessenen P(T)-Kurven bei mehreren Feldern folgt über die Maxwell-Relation der elektrokalorische Effekt. Dünne Schichten halten enorme Felder aus: PbZr₀.₉₅Ti₀.₀₅O₃-Filme erreichen ≈ 12 K bei 480 kV/cm (Mischenko et al., Science 2006). Rechnung mit mittlerem Pyrokoeffizienten.",
            "ΔT = −(T/(ρc)) ∫_{E₁}^{E₂} (∂P/∂T)_E dE ≈ −(T/(ρc)) · p̄ · ΔE",
            listOf(
                Param("T", "Temperatur T", "K", 499.0, 1.0, 2000.0), Param("p", "Mittlerer Pyrokoeffizient (∂P/∂T)_E", "μC m⁻² K⁻¹", -1000.0, -1e7, 1e7),
                Param("E", "Feldänderung ΔE", "kV/cm", 480.0, 0.0, 1e4), Param("rho", "Dichte ρ", "kg/m³", 8300.0, 1.0, 3e4), Param("c", "Spez. Wärme c", "J/(kg K)", 330.0, 1.0, 1e4)
            )
        ) { v ->
            val t = v.getValue("T"); val p = v.getValue("p") * 1e-6; val de = v.getValue("E") * 1e5; val rho = v.getValue("rho"); val c = v.getValue("c")
            val dt = -t / (rho * c) * p * de
            CalcResult(listOf(
                Output("ΔT_ad", dt, "K"), Output("ΔS_iso = p̄ΔE / ρ", p * de / rho, "J kg⁻¹ K⁻¹"),
                Output("Isotherme Wärme Q = TΔS", t * p * de / rho, "J/kg"), Output("EC-Stärke ΔT/ΔE", dt / (de / 1e5) * 1e3, "mK cm/kV")
            ), steps = listOf("ΔT = −(${Fmt.num(t)} K / (${Fmt.num(rho)}·${Fmt.num(c)})) · ${Fmt.num(p)} C m⁻²K⁻¹ · ${Fmt.num(de)} V/m = ${Fmt.num(dt)} K"))
        },
        Calculator(
            "elastocaloric", Topic.CALORIC, "Elastokalorik: Formgedächtnislegierungen",
            "Mechanische Spannung induziert in superelastischen Legierungen die Umwandlung Austenit → Martensit. Die latente Wärme L wird frei (Erwärmung), beim Entlasten wieder aufgenommen (Abkühlung). Die Umwandlungsspannung steigt nach Clausius-Clapeyron linear mit T. Elastokalorik gilt als aussichtsreichste Alternative zum Kompressorkühlschrank (NiTi: ΔT bis ≈ 25 K).",
            "dσ_tr/dT = −ρΔs_tr/ε_tr  (Clausius-Clapeyron),   ΔT_ad ≈ f·L/c,   f = Martensitanteil;   thermoelastisch (elastisch): ΔT = −(αT/(ρc))·Δσ",
            listOf(
                Param("mat", "Material", "", 0.0, choices = SMAS.map { it.name }),
                Param("T", "Temperatur T", "°C", 25.0, -100.0, 200.0), Param("sigma", "Zugspannung σ", "MPa", 600.0, 0.0, 3000.0),
                Param("eff", "Umwandlungs-Wirkungsgrad (Hysterese, Wärmeabfluss)", "", 0.85, 0.0, 1.0)
            )
        ) { v ->
            val m = SMAS[v.getValue("mat").toInt()]; val t = v.getValue("T"); val s = v.getValue("sigma"); val eff = v.getValue("eff")
            fun onset(tt: Double) = m.sigmaAtRt + m.dSigmaDT * (tt - 25)
            fun fraction(sig: Double, tt: Double) = ((sig - onset(tt)) / m.width).coerceIn(0.0, 1.0)
            val f = fraction(s, t)
            val dt = eff * f * m.latentJg / m.cJgK
            val sigs = grid(0.0, max(s, onset(t) + 2 * m.width), 200)
            val dsTr = m.latentJg * 1e3 / (t + 273.15)
            CalcResult(
                listOf(
                    Output("Umwandlungsspannung σ_tr(T)", onset(t), "MPa"), Output("Martensitanteil f", f, ""),
                    Output("Adiabatisches ΔT", dt, "K"), Output("Max. ΔT = L/c", m.latentJg / m.cJgK, "K"),
                    Output("Umwandlungsentropie Δs = L/T", dsTr, "J kg⁻¹ K⁻¹"),
                    Output("Material-COP (Carnot bei ΔT)", if (dt > 0) (t + 273.15) / dt else Double.NaN, "", "obere Schranke für Spanne = ΔT")
                ),
                listOf(Curve("ΔT gegen Spannung bei drei Temperaturen", "σ in MPa", "ΔT in K", listOf(t - 20, t, t + 20).map { tt ->
                    CurveSeries("${Fmt.num(tt, 3)} °C", sigs, sigs.map { eff * fraction(it, tt) * m.latentJg / m.cJgK })
                }))
            )
        },
        Calculator(
            "magnetocaloric", Topic.CALORIC, "Magnetokalorik: Gadolinium im Molekularfeld",
            "Ein Magnetfeld richtet die 4f-Spins von Gd aus und senkt die magnetische Entropie. Molekularfeldmodell mit J = 7/2 und T_C = 293 K, Gitterentropie nach Debye. Das Modell überschätzt die Messwerte (≈ 2,5–3 K/T nahe T_C) etwas, zeigt aber die Physik exakt: Maximum bei T_C, Skalierung ∝ H^{2/3} am Übergang.",
            "m = B_J(x),  x = gμ_BJμ₀H/(k_BT) + 3J/(J+1)·(T_C/T)·m,   S_m = R[ln(sinh((2J+1)x/2J)/sinh(x/2J)) − x m],   (∂S/∂H)_T = μ₀(∂M/∂T)_H",
            listOf(Param("T", "Temperatur T", "K", 293.0, 150.0, 400.0), Param("B", "Feld μ₀H", "T", 2.0, 0.0, 10.0))
        ) { v ->
            val t = v.getValue("T"); val b = v.getValue("B")
            val dT = magnetocaloricDeltaT(GD, GD_DEBYE, b, t)
            val ts = grid(230.0, 360.0, 70)
            CalcResult(
                listOf(
                    Output("Reduzierte Magnetisierung m(0)", GD.magnetization(0.0, t), ""), Output("m(H)", GD.magnetization(b, t), ""),
                    Output("ΔS_m (isotherm)", (GD.entropy(b, t) - GD.entropy(0.0, t)) / 0.15725, "J kg⁻¹ K⁻¹"),
                    Output("ΔT_ad", dT, "K"), Output("Max. magnetische Entropie R ln(2J+1)", Phys.R * ln(8.0), "J mol⁻¹ K⁻¹")
                ),
                listOf(
                    Curve("ΔT_ad gegen Temperatur", "T in K", "ΔT in K", listOf(1.0, 2.0, 5.0).map { bb -> CurveSeries("${Fmt.num(bb, 2)} T", ts, ts.map { magnetocaloricDeltaT(GD, GD_DEBYE, bb, it) }) }),
                    Curve("Magnetisierung m(T)", "T in K", "M/M_s", listOf(CurveSeries("0 T", ts, ts.map { GD.magnetization(0.0, it) }), CurveSeries("${Fmt.num(b, 2)} T", ts, ts.map { GD.magnetization(b, it) }, dashed = true)), 0.0 to 1.05)
                )
            )
        },
        Calculator(
            "barocaloric", Topic.CALORIC, "Barokalorik",
            "Hydrostatischer Druck ändert die Entropie über die thermische Ausdehnung. Abseits von Phasenübergängen ist der Effekt klein; an Übergängen mit großer Volumenänderung (plastische Kristalle wie Neopentylglykol: „kolossale“ Barokalorik, Δs ≈ 400–500 J kg⁻¹K⁻¹) wird er riesig.",
            "(∂S/∂p)_T = −(∂V/∂T)_p   ⇒   ΔS = −v α_V Δp,   ΔT_ad ≈ (T v α_V / c_p) Δp;   am Übergang: ΔT ≈ T Δs_tr / c_p",
            listOf(
                Param("T", "Temperatur T", "K", 300.0, 1.0, 2000.0), Param("alpha", "Vol.-Ausdehnungskoeffizient α_V", "10⁻⁶ K⁻¹", 500.0, -1e5, 1e5),
                Param("rho", "Dichte ρ", "kg/m³", 1000.0, 1.0, 3e4), Param("c", "Spez. Wärme c_p", "J/(kg K)", 2000.0, 1.0, 1e5),
                Param("dp", "Druckänderung Δp", "MPa", 100.0, 0.0, 1e4), Param("dstr", "Übergangsentropie Δs_tr (0 = kein Übergang)", "J/(kg K)", 0.0, 0.0, 2000.0)
            )
        ) { v ->
            val t = v.getValue("T"); val a = v.getValue("alpha") * 1e-6; val vSpec = 1 / v.getValue("rho"); val c = v.getValue("c"); val dp = v.getValue("dp") * 1e6; val dstr = v.getValue("dstr")
            val dsLin = -vSpec * a * dp
            val dtLin = t * vSpec * a * dp / c
            CalcResult(listOf(
                Output("ΔS_iso (linear)", dsLin, "J kg⁻¹ K⁻¹"), Output("ΔT_ad (linear)", dtLin, "K"),
                Output("ΔT_ad (Übergang)", t * dstr / c, "K"), Output("Wärme pro Zyklus TΔs", t * (abs(dsLin) + dstr) / 1e3, "kJ/kg")
            ))
        },
        Calculator(
            "multicaloric", Topic.CALORIC, "Multikalorik: Kopplung zweier Felder",
            "In multiferroischen Materialien (z. B. ferroelektrisch + ferroelastisch) koppeln die Felder. Für kleine Felder genügt eine quadratische Entwicklung der Gibbs-Energie. Die Entropieänderung bei gleichzeitigem Anlegen enthält einen Kreuzterm, der von der Temperaturabhängigkeit der Kopplung (hier des piezoelektrischen Koeffizienten d) herrührt — er kann den Effekt verstärken oder schwächen und erlaubt Zyklen mit kleinerer Hysterese.",
            "G = G₀ − ½χ_E E² − ½ s σ² − d E σ   ⇒   ΔS = ½ χ_E' E² + ½ s' σ² + d' E σ   (′ = ∂/∂T),   ΔT ≈ −T ΔS/(ρc)",
            listOf(
                Param("T", "Temperatur T", "K", 400.0, 1.0, 2000.0),
                Param("chiE", "∂χ_E/∂T", "10⁻¹² F m⁻¹ K⁻¹", -500.0, -1e7, 1e7, hint = "ε₀∂ε_r/∂T; negativ oberhalb T_C"),
                Param("sT", "∂s/∂T (Nachgiebigkeit)", "10⁻¹⁵ Pa⁻¹ K⁻¹", -50.0, -1e7, 1e7),
                Param("dT", "∂d/∂T (Piezokoeffizient)", "10⁻¹² C N⁻¹ K⁻¹", -2.0, -1e5, 1e5),
                Param("E", "Feld E", "kV/cm", 50.0, -1e4, 1e4), Param("sigma", "Spannung σ", "MPa", 100.0, -1e4, 1e4),
                Param("rhoc", "Wärmekapazität ρc", "MJ m⁻³ K⁻¹", 2.6, 0.01, 100.0)
            )
        ) { v ->
            val t = v.getValue("T"); val chi = v.getValue("chiE") * 1e-12; val sp = v.getValue("sT") * 1e-15; val dp = v.getValue("dT") * 1e-12
            val e = v.getValue("E") * 1e5; val sig = v.getValue("sigma") * 1e6; val rc = v.getValue("rhoc") * 1e6
            val sE = 0.5 * chi * e * e; val sS = 0.5 * sp * sig * sig; val sX = dp * e * sig
            fun dT(s: Double) = -t * s / rc
            CalcResult(
                listOf(
                    Output("ΔS nur E", sE, "J m⁻³ K⁻¹"), Output("ΔS nur σ", sS, "J m⁻³ K⁻¹"), Output("Kreuzterm d′Eσ", sX, "J m⁻³ K⁻¹"),
                    Output("ΔT nur E", dT(sE), "K"), Output("ΔT nur σ", dT(sS), "K"), Output("ΔT gleichzeitig", dT(sE + sS + sX), "K"),
                    Output("Kopplungsgewinn", dT(sX), "K", if (sX < 0) "verstärkt" else "schwächt")
                ),
                listOf(Curve("ΔT gegen E bei drei Spannungen", "E in kV/cm", "ΔT in K", listOf(0.0, sig, -sig).map { ss ->
                    val es = grid(-abs(e) * 1.5 - 1e5, abs(e) * 1.5 + 1e5, 120)
                    CurveSeries("σ = ${Fmt.num(ss / 1e6, 3)} MPa", es.map { it / 1e5 }, es.map { ee -> dT(0.5 * chi * ee * ee + 0.5 * sp * ss * ss + dp * ee * ss) })
                }))
            )
        },
        Calculator(
            "caloric_cycle", Topic.CALORIC, "Kalorischer Kühlkreisprozess",
            "Ein Brayton-Zyklus aus Feld an (adiabatisch, Erwärmung um ΔT_ad) → Wärme an die warme Seite → Feld aus (Abkühlung) → Wärme von der kalten Seite. Ohne Regenerator ist die Temperaturspanne kleiner als ΔT_ad; aktive Regeneratoren (AMR) kaskadieren viele kleine Zyklen zu großen Spannen.",
            "Q_c ≈ m c (ΔT_ad − ΔT_Spanne) f,   COP_Carnot = T_c/(T_h − T_c),   Gütegrad = COP/COP_Carnot",
            listOf(
                Param("dTad", "Adiabatisches ΔT_ad", "K", 10.0, 0.01, 200.0), Param("span", "Temperaturspanne T_h − T_c", "K", 5.0, 0.0, 200.0),
                Param("Tc", "Kalte Seite T_c", "°C", 20.0, -270.0, 500.0), Param("m", "Masse Arbeitsmaterial", "g", 50.0, 0.001, 1e7),
                Param("c", "Spez. Wärme c", "J/(kg K)", 460.0, 1.0, 1e5), Param("f", "Zyklusfrequenz f", "Hz", 1.0, 1e-3, 1e3), Param("work", "Hysteresearbeit pro Zyklus", "J/kg", 200.0, 0.0, 1e6)
            )
        ) { v ->
            val tc = v.getValue("Tc") + 273.15; val span = v.getValue("span"); val dta = v.getValue("dTad"); val m = v.getValue("m") * 1e-3; val c = v.getValue("c"); val f = v.getValue("f")
            val qc = m * c * max(0.0, dta - span) * f
            val wMin = qc * span / tc
            val w = wMin + v.getValue("work") * m * f
            val cop = if (w > 0) qc / w else Double.POSITIVE_INFINITY
            val spans = grid(0.0, dta, 100)
            CalcResult(
                listOf(
                    Output("Kälteleistung Q̇_c", qc, "W"), Output("COP (Schätzung)", cop, ""), Output("COP_Carnot", if (span > 0) tc / span else Double.POSITIVE_INFINITY, ""),
                    Output("Gütegrad", if (span > 0) cop / (tc / span) else Double.NaN, ""), Output("Spezifische Kälteleistung", if (m > 0) qc / m else 0.0, "W/kg")
                ),
                listOf(Curve("Kälteleistung gegen Spanne", "ΔT_Spanne in K", "Q̇_c in W", listOf(CurveSeries("Q̇_c", spans, spans.map { m * c * (dta - it) * f })))),
                warnings = if (span >= dta) listOf("Ohne Regenerator kann die Spanne ΔT_ad nicht erreichen.") else emptyList()
            )
        },
        Calculator(
            "calorimetry", Topic.CALORIC, "Mischungskalorimetrie",
            "Zwei Körper erreichen ohne Wärmeverluste eine gemeinsame Temperatur. Mit Phasenübergang (Eis bei 0 °C, Schmelzwärme 334 J/g) wird geprüft, ob alles Eis schmilzt.",
            "Σ mᵢcᵢ(T_m − Tᵢ) = 0   ⇒   T_m = Σ mᵢcᵢTᵢ / Σ mᵢcᵢ;   Q_Schmelz = m·L_s",
            listOf(
                Param("m1", "Masse 1 (Wasser)", "g", 200.0, 0.0, 1e9), Param("T1", "Temperatur 1", "°C", 80.0, 0.0, 100.0),
                Param("m2", "Masse 2 (Eis bei 0 °C oder Metall)", "g", 50.0, 0.0, 1e9), Param("T2", "Temperatur 2", "°C", 0.0, -270.0, 2000.0),
                Param("kind", "Körper 2", "", 0.0, choices = listOf("Eis (schmilzt bei 0 °C)", "Wasser", "Kupfer (0,385 J/gK)", "Aluminium (0,897 J/gK)", "Eisen (0,449 J/gK)"))
            )
        ) { v ->
            val m1 = v.getValue("m1"); val t1 = v.getValue("T1"); val m2 = v.getValue("m2"); val t2 = v.getValue("T2"); val kind = v.getValue("kind").toInt()
            val cw = 4.186; val ls = 334.0
            val out = ArrayList<Output>(); val steps = ArrayList<String>()
            if (kind == 0) {
                val ice = 2.09
                val heatAvail = m1 * cw * t1
                val needMelt = m2 * ice * (0 - min(0.0, t2)) + m2 * ls
                if (heatAvail >= needMelt) {
                    val tm = (heatAvail - needMelt) / ((m1 + m2) * cw)
                    out += Output("Mischtemperatur", tm, "°C"); out += Output("Geschmolzenes Eis", m2, "g")
                    steps += "Wärme reicht: ${Fmt.num(heatAvail)} J ≥ ${Fmt.num(needMelt)} J zum Schmelzen"
                } else {
                    val melted = max(0.0, (heatAvail - m2 * ice * (0 - min(0.0, t2))) / ls)
                    out += Output("Mischtemperatur", 0.0, "°C", "Eis-Wasser-Gemisch"); out += Output("Geschmolzenes Eis", melted, "g"); out += Output("Restliches Eis", m2 - melted, "g")
                }
            } else {
                val c2 = doubleArrayOf(0.0, cw, 0.385, 0.897, 0.449)[kind]
                val tm = (m1 * cw * t1 + m2 * c2 * t2) / (m1 * cw + m2 * c2)
                out += Output("Mischtemperatur", tm, "°C"); out += Output("Übertragene Wärme", abs(m1 * cw * (tm - t1)), "J")
                steps += "T_m = (${Fmt.num(m1)}·4,186·${Fmt.num(t1)} + ${Fmt.num(m2)}·${Fmt.num(c2)}·${Fmt.num(t2)}) / (${Fmt.num(m1)}·4,186 + ${Fmt.num(m2)}·${Fmt.num(c2)})"
            }
            CalcResult(out, steps = steps)
        }
    )
}

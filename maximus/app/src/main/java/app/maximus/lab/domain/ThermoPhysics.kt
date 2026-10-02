package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.expm1
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

object ThermoPhysics {
    /** Carnot efficiency η = 1 − T_c/T_h. */
    fun carnot(th: Double, tc: Double) = 1 - tc / th

    /** Otto cycle (two isentropes, two isochores): η = 1 − r^(1−γ). */
    fun otto(r: Double, gamma: Double) = 1 - r.pow(1 - gamma)

    /** Diesel cycle with compression ratio r and cut-off ratio ρ: η = 1 − r^(1−γ)(ρ^γ − 1)/(γ(ρ − 1)). */
    fun diesel(r: Double, rho: Double, gamma: Double) = 1 - r.pow(1 - gamma) * (rho.pow(gamma) - 1) / (gamma * (rho - 1))

    /** Brayton cycle with pressure ratio π: η = 1 − π^((1−γ)/γ). */
    fun brayton(pi: Double, gamma: Double) = 1 - pi.pow((1 - gamma) / gamma)

    /** Spectral radiance per wavelength B_λ(T) = 2hc²/λ⁵ · 1/(exp(hc/λk_BT) − 1), in W sr⁻¹ m⁻³. */
    fun planckLambda(lambda: Double, t: Double): Double {
        val x = Phys.h * Phys.c / (lambda * Phys.kB * t)
        return 2 * Phys.h * Phys.c * Phys.c / lambda.pow(5) / expm1(x)
    }

    /** Maxwell–Boltzmann speed density f(v) = 4π (m/2πk_BT)^(3/2) v² exp(−mv²/2k_BT). */
    fun maxwell(v: Double, massKg: Double, t: Double): Double {
        val a = massKg / (2 * Phys.kB * t)
        return 4 * PI * (a / PI).pow(1.5) * v * v * exp(-a * v * v)
    }

    /** Debye heat capacity per mole: C_V = 9R (T/Θ)³ ∫₀^{Θ/T} x⁴eˣ/(eˣ−1)² dx. */
    fun debyeCv(t: Double, thetaD: Double): Double {
        if (t <= 0) return 0.0
        val xd = thetaD / t
        val integral = Calculus.gaussLegendre({ x -> if (x < 1e-8) x * x else { val ex = exp(x); x.pow(4) * ex / ((ex - 1) * (ex - 1)) } }, 0.0, xd, 48)
        return 9 * Phys.R * (t / thetaD).pow(3) * integral
    }

    /** Einstein heat capacity per mole: C_V = 3R (Θ_E/T)² e^{Θ_E/T} / (e^{Θ_E/T} − 1)². */
    fun einsteinCv(t: Double, thetaE: Double): Double {
        if (t <= 0) return 0.0
        val x = thetaE / t
        if (x > 700) return 0.0
        val ex = exp(x)
        return 3 * Phys.R * x * x * ex / ((ex - 1) * (ex - 1))
    }

    /** Van der Waals pressure p = RT/(V_m − b) − a/V_m². */
    fun vdwPressure(t: Double, vm: Double, a: Double, b: Double) = Phys.R * t / (vm - b) - a / (vm * vm)

    private data class Gas(val name: String, val molarMass: Double, val a: Double, val b: Double, val gamma: Double)
    private val GASES = listOf(
        Gas("Helium", 4.0026e-3, 0.00346, 2.38e-5, 5.0 / 3),
        Gas("Stickstoff N₂", 28.014e-3, 0.137, 3.87e-5, 1.40),
        Gas("Sauerstoff O₂", 31.998e-3, 0.138, 3.18e-5, 1.40),
        Gas("Kohlendioxid CO₂", 44.009e-3, 0.364, 4.27e-5, 1.29),
        Gas("Wasserdampf H₂O", 18.015e-3, 0.5536, 3.049e-5, 1.33),
        Gas("Wasserstoff H₂", 2.016e-3, 0.02476, 2.661e-5, 1.41)
    )

    private data class Solid(val name: String, val debye: Double)
    private val SOLIDS = listOf(Solid("Blei", 105.0), Solid("Kupfer", 343.0), Solid("Aluminium", 428.0), Solid("Silizium", 645.0), Solid("Diamant", 2230.0))

    val calculators: List<Calculator> = listOf(
        Calculator(
            "ideal_gas", Topic.THERMO, "Ideales Gas",
            "Zustandsgleichung des idealen Gases: Stoffmenge, Teilchenzahl, Dichte und mittlere Energie pro Teilchen aus Druck, Volumen und Temperatur.",
            "pV = nRT = Nk_BT,   ⟨E_kin⟩ = (3/2) k_BT,   ρ = pM/(RT)",
            listOf(
                Param("p", "Druck p", "kPa", 101.325, 1e-6, 1e7),
                Param("V", "Volumen V", "L", 22.414, 1e-9, 1e9),
                Param("T", "Temperatur T", "K", 273.15, 1e-3, 1e6),
                Param("gas", "Gas", "", 1.0, choices = GASES.map { it.name })
            )
        ) { v ->
            val g = GASES[v.getValue("gas").toInt()]
            val p = v.getValue("p") * 1e3; val vol = v.getValue("V") * 1e-3; val t = v.getValue("T")
            val n = p * vol / (Phys.R * t)
            val vs = grid(vol * 0.3, vol * 3.0, 120)
            CalcResult(
                listOf(
                    Output("Stoffmenge n", n, "mol"),
                    Output("Teilchenzahl N", n * Phys.NA, ""),
                    Output("Masse m", n * g.molarMass * 1e3, "g"),
                    Output("Dichte ρ", p * g.molarMass / (Phys.R * t), "kg m⁻³"),
                    Output("Mittlere kinetische Energie", 1.5 * Phys.kB * t / Phys.e * 1e3, "meV"),
                    Output("Innere Energie U = n·c_V·T", n * Phys.R / (g.gamma - 1) * t, "J"),
                    Output("Teilchendichte", n * Phys.NA / vol, "m⁻³")
                ),
                listOf(Curve("Isotherme und Adiabate durch den Zustand", "V in L", "p in kPa", listOf(
                    CurveSeries("Isotherme pV = const", vs.map { it * 1e3 }, vs.map { p * vol / it / 1e3 }),
                    CurveSeries("Adiabate pV^γ = const (γ = ${Fmt.num(g.gamma, 3)})", vs.map { it * 1e3 }, vs.map { p * (vol / it).pow(g.gamma) / 1e3 }, dashed = true)
                ))),
                listOf("n = pV/(RT) = ${Fmt.num(p)} Pa · ${Fmt.num(vol)} m³ / (8,314 J mol⁻¹K⁻¹ · ${Fmt.num(t)} K) = ${Fmt.num(n)} mol")
            )
        },
        Calculator(
            "cycles", Topic.THERMO, "Kreisprozesse und Wirkungsgrad",
            "Vergleich der idealen Wirkungsgrade von Carnot-, Otto-, Diesel- und Joule/Brayton-Prozess. Kein realer Prozess zwischen denselben Temperaturen schlägt Carnot (2. Hauptsatz).",
            "η_C = 1 − T_k/T_w,   η_Otto = 1 − r^(1−γ),   η_Diesel = 1 − r^(1−γ)(ρ^γ−1)/(γ(ρ−1)),   η_Brayton = 1 − π^((1−γ)/γ)",
            listOf(
                Param("Th", "Heiße Temperatur T_w", "K", 2000.0, 1.0, 1e5),
                Param("Tc", "Kalte Temperatur T_k", "K", 300.0, 0.01, 1e5),
                Param("r", "Verdichtungsverhältnis r", "", 10.0, 1.01, 100.0),
                Param("rho", "Einspritzverhältnis ρ (Diesel)", "", 2.0, 1.01, 10.0),
                Param("pi", "Druckverhältnis π (Brayton)", "", 15.0, 1.01, 100.0),
                Param("gamma", "Adiabatenexponent γ", "", 1.4, 1.01, 1.67)
            )
        ) { v ->
            val th = v.getValue("Th"); val tc = v.getValue("Tc"); val r = v.getValue("r"); val rho = v.getValue("rho"); val pi = v.getValue("pi"); val g = v.getValue("gamma")
            val rs = grid(1.5, 30.0, 120)
            val warnings = if (tc >= th) listOf("T_k muss kleiner als T_w sein.") else emptyList()
            CalcResult(
                listOf(
                    Output("Carnot η_C", 100 * carnot(th, tc), "%"),
                    Output("Otto η", 100 * otto(r, g), "%"),
                    Output("Diesel η", 100 * diesel(r, rho, g), "%"),
                    Output("Brayton η", 100 * brayton(pi, g), "%"),
                    Output("Kältemaschine COP_C = T_k/(T_w−T_k)", tc / (th - tc), ""),
                    Output("Wärmepumpe COP_C = T_w/(T_w−T_k)", th / (th - tc), "")
                ),
                listOf(Curve("Wirkungsgrad gegen Verdichtungsverhältnis", "r", "η in %", listOf(
                    CurveSeries("Otto", rs, rs.map { 100 * otto(it, g) }),
                    CurveSeries("Diesel (ρ = ${Fmt.num(rho, 3)})", rs, rs.map { 100 * diesel(it, rho, g) }, dashed = true),
                    CurveSeries("Carnot-Grenze", rs, rs.map { 100 * carnot(th, tc) }, dashed = true)
                ), 0.0 to 100.0)),
                listOf(
                    "η_C = 1 − ${Fmt.num(tc)}/${Fmt.num(th)} = ${Fmt.num(carnot(th, tc))}",
                    "η_Otto = 1 − ${Fmt.num(r)}^(1 − ${Fmt.num(g)}) = ${Fmt.num(otto(r, g))}"
                ),
                warnings
            )
        },
        Calculator(
            "planck", Topic.THERMO, "Schwarzer Strahler (Planck)",
            "Spektrale Strahldichte nach Planck, Wiensches Verschiebungsgesetz und Stefan-Boltzmann-Gesetz. Die Kurve zeigt B_λ für T und zum Vergleich für T/2 und 2T.",
            "B_λ(T) = (2hc²/λ⁵) · 1/(e^{hc/λk_BT} − 1),   λ_max = b/T,   M = σT⁴",
            listOf(
                Param("T", "Temperatur T", "K", 5772.0, 1.0, 1e6),
                Param("A", "Fläche A", "m²", 1.0, 0.0, 1e20),
                Param("eps", "Emissionsgrad ε", "", 1.0, 0.0, 1.0)
            )
        ) { v ->
            val t = v.getValue("T"); val a = v.getValue("A"); val eps = v.getValue("eps")
            val lmax = Phys.wienB / t
            val ls = grid(lmax * 0.1, lmax * 6, 240)
            CalcResult(
                listOf(
                    Output("λ_max (Wien)", lmax * 1e9, "nm"),
                    Output("Frequenz-Maximum ν_max = 2,821 k_BT/h", 2.821439372 * Phys.kB * t / Phys.h / 1e12, "THz"),
                    Output("Spezifische Ausstrahlung M = εσT⁴", eps * Phys.sigmaSB * t.pow(4), "W m⁻²"),
                    Output("Leistung P = εσAT⁴", eps * Phys.sigmaSB * a * t.pow(4), "W"),
                    Output("Photonendichte n = 0,2436 (k_BT/ħc)³", 2.404113806 / (PI * PI) * (Phys.kB * t / (Phys.hbar * Phys.c)).pow(3), "m⁻³"),
                    Output("Mittlere Photonenenergie ≈ 2,701 k_BT", 2.701178 * Phys.kB * t / Phys.e, "eV")
                ),
                listOf(Curve("Spektrale Strahldichte", "λ in nm", "B_λ in kW sr⁻¹ m⁻² nm⁻¹", listOf(
                    CurveSeries("T", ls.map { it * 1e9 }, ls.map { planckLambda(it, t) * 1e-12 }),
                    CurveSeries("T/2", ls.map { it * 1e9 }, ls.map { planckLambda(it, t / 2) * 1e-12 }, dashed = true),
                    CurveSeries("2T (Skala gedeckelt)", ls.map { it * 1e9 }, ls.map { planckLambda(it, 2 * t) * 1e-12 }, dashed = true)
                ), 0.0 to planckLambda(lmax, t) * 1e-12 * 1.15)),
                listOf("λ_max = b/T = 2,8978·10⁻³ m K / ${Fmt.num(t)} K = ${Fmt.num(lmax * 1e9)} nm")
            )
        },
        Calculator(
            "maxwell", Topic.THERMO, "Maxwell-Boltzmann-Verteilung",
            "Geschwindigkeitsverteilung eines idealen Gases. Wahrscheinlichste, mittlere und quadratisch gemittelte Geschwindigkeit stehen im Verhältnis √2 : √(8/π) : √3.",
            "f(v) = 4π (m/2πk_BT)^{3/2} v² e^{−mv²/2k_BT},   v_p = √(2k_BT/m),   ⟨v⟩ = √(8k_BT/πm),   v_rms = √(3k_BT/m)",
            listOf(
                Param("T", "Temperatur T", "K", 300.0, 1.0, 1e5),
                Param("gas", "Gas", "", 1.0, choices = GASES.map { it.name })
            )
        ) { v ->
            val t = v.getValue("T"); val g = GASES[v.getValue("gas").toInt()]
            val m = g.molarMass / Phys.NA
            val vp = sqrt(2 * Phys.kB * t / m); val vm = sqrt(8 * Phys.kB * t / (PI * m)); val vr = sqrt(3 * Phys.kB * t / m)
            val vs = grid(0.0, 4 * vp, 200)
            CalcResult(
                listOf(
                    Output("Wahrscheinlichste v_p", vp, "m/s"), Output("Mittlere ⟨v⟩", vm, "m/s"), Output("Effektive v_rms", vr, "m/s"),
                    Output("Schallgeschwindigkeit √(γRT/M)", sqrt(g.gamma * Phys.R * t / g.molarMass), "m/s"),
                    Output("Anteil schneller als 2v_p", 1 - Calculus.integrate({ maxwell(it, m, t) }, 0.0, 2 * vp), "")
                ),
                listOf(Curve("Verteilung f(v) für ${g.name}", "v in m/s", "f(v) in s/km", listOf(
                    CurveSeries("T", vs, vs.map { maxwell(it, m, t) * 1e3 }),
                    CurveSeries("T/2", vs, vs.map { maxwell(it, m, t / 2) * 1e3 }, dashed = true),
                    CurveSeries("2T", vs, vs.map { maxwell(it, m, 2 * t) * 1e3 }, dashed = true)
                )))
            )
        },
        Calculator(
            "heat_rod", Topic.THERMO, "Wärmeleitung im Stab (Simulation)",
            "Eindimensionale Wärmeleitungsgleichung mit dem expliziten FTCS-Verfahren. Ein Stab mit Anfangstemperatur T₀ wird an beiden Enden auf T_L bzw. T_R gehalten. Stabil nur für Δt ≤ Δx²/(2a).",
            "∂T/∂t = a ∂²T/∂x²,   a = λ/(ρc),   T_i^{n+1} = T_i^n + aΔt/Δx² (T_{i+1}^n − 2T_i^n + T_{i−1}^n)",
            listOf(
                Param("L", "Länge L", "cm", 10.0, 0.1, 1000.0),
                Param("mat", "Material", "", 0.0, choices = listOf("Kupfer (a = 1,11·10⁻⁴ m²/s)", "Aluminium (9,7·10⁻⁵)", "Stahl (1,2·10⁻⁵)", "Glas (3,4·10⁻⁷)", "Wasser (1,43·10⁻⁷)")),
                Param("T0", "Anfangstemperatur T₀", "°C", 20.0, -273.0, 3000.0),
                Param("TL", "Linkes Ende T_L", "°C", 100.0, -273.0, 3000.0),
                Param("TR", "Rechtes Ende T_R", "°C", 20.0, -273.0, 3000.0),
                Param("t", "Simulierte Zeit t", "s", 60.0, 0.001, 1e7)
            )
        ) { v ->
            val diff = doubleArrayOf(1.11e-4, 9.7e-5, 1.2e-5, 3.4e-7, 1.43e-7)[v.getValue("mat").toInt()]
            val len = v.getValue("L") / 100
            val n = 60
            val dx = len / n
            val dtMax = dx * dx / (2 * diff)
            // At most 200 000 stable steps; longer runs are shortened (the profile is then stationary anyway).
            val maxSteps = 200_000
            val tEnd = minOf(v.getValue("t"), maxSteps * 0.9 * dtMax)
            val shortened = tEnd < v.getValue("t")
            val steps = max(1, kotlin.math.ceil(tEnd / (0.9 * dtMax)).toInt()).coerceAtMost(maxSteps)
            val dt = tEnd / steps
            val fo = diff * dt / (dx * dx)
            var u = DoubleArray(n + 1) { v.getValue("T0") }
            u[0] = v.getValue("TL"); u[n] = v.getValue("TR")
            val snapshots = ArrayList<Pair<Double, DoubleArray>>()
            val marks = listOf(0.1, 0.3, 1.0).map { (it * steps).toInt().coerceAtLeast(1) }.toSet()
            for (s in 1..steps) {
                val next = u.copyOf()
                for (i in 1 until n) next[i] = u[i] + fo * (u[i + 1] - 2 * u[i] + u[i - 1])
                u = next
                if (s in marks) snapshots += s * dt to u.copyOf()
            }
            val xs = (0..n).map { it * dx * 100 }
            // Characteristic diffusion time τ = L²/(π²a) of the slowest Fourier mode.
            val tau = len * len / (PI * PI * diff)
            CalcResult(
                listOf(
                    Output("Temperaturleitfähigkeit a", diff, "m² s⁻¹"),
                    Output("Fourier-Zahl pro Schritt aΔt/Δx²", fo, "", "≤ 0,5 für Stabilität"),
                    Output("Zeitschritte", steps.toDouble(), "", digits = 6),
                    Output("Relaxationszeit τ = L²/(π²a)", tau, "s"),
                    Output("Temperatur in der Mitte", u[n / 2], "°C")
                ),
                listOf(Curve("Temperaturprofil T(x) zu drei Zeiten", "x in cm", "T in °C",
                    snapshots.mapIndexed { i, (time, prof) -> CurveSeries("t = ${Fmt.num(time, 3)} s", xs, prof.toList(), dashed = i < snapshots.size - 1) })),
                listOf("Stationärer Endzustand: lineares Profil von T_L nach T_R; die Abweichung klingt wie e^{−t/τ} ab."),
                if (shortened) listOf("Simulation nach ${Fmt.num(tEnd, 3)} s beendet (Rechenzeit); bei t ≫ τ ist das Profil ohnehin stationär.") else emptyList()
            )
        },
        Calculator(
            "vdw", Topic.THERMO, "Van-der-Waals-Gas und kritischer Punkt",
            "Reale Gase: Eigenvolumen b und Anziehung a. Unterhalb von T_c zeigen die Isothermen eine Schleife; die Maxwell-Konstruktion ersetzt sie durch die Phasenkoexistenz.",
            "(p + a/V_m²)(V_m − b) = RT,   T_c = 8a/(27Rb),   p_c = a/(27b²),   V_c = 3b,   Z_c = 3/8",
            listOf(Param("gas", "Gas", "", 3.0, choices = GASES.map { it.name }), Param("T", "Temperatur T", "K", 280.0, 1.0, 5000.0))
        ) { v ->
            val g = GASES[v.getValue("gas").toInt()]; val t = v.getValue("T")
            val tc = 8 * g.a / (27 * Phys.R * g.b); val pc = g.a / (27 * g.b * g.b); val vc = 3 * g.b
            val vs = logGrid(g.b * 1.3, vc * 12, 260)
            CalcResult(
                listOf(
                    Output("Kritische Temperatur T_c", tc, "K"), Output("Kritischer Druck p_c", pc / 1e5, "bar"),
                    Output("Kritisches Molvolumen V_c", vc * 1e6, "cm³/mol"), Output("Reduzierte Temperatur T/T_c", t / tc, ""),
                    Output("Druck bei V_m = 1 L/mol", vdwPressure(t, 1e-3, g.a, g.b) / 1e5, "bar"),
                    Output("Ideales Gas bei 1 L/mol", Phys.R * t / 1e-3 / 1e5, "bar")
                ),
                listOf(Curve("Isothermen von ${g.name}", "V_m in cm³/mol", "p in bar", listOf(
                    CurveSeries("T", vs.map { it * 1e6 }, vs.map { vdwPressure(t, it, g.a, g.b) / 1e5 }),
                    CurveSeries("T_c", vs.map { it * 1e6 }, vs.map { vdwPressure(tc, it, g.a, g.b) / 1e5 }, dashed = true),
                    CurveSeries("1,2 T_c", vs.map { it * 1e6 }, vs.map { vdwPressure(1.2 * tc, it, g.a, g.b) / 1e5 }, dashed = true)
                ), -0.5 * pc / 1e5 to 3 * pc / 1e5))
            )
        },
        Calculator(
            "debye", Topic.THERMO, "Wärmekapazität von Festkörpern (Debye/Einstein)",
            "Phononen bestimmen C_V: Dulong-Petit (3R) bei hohen T, Debyesches T³-Gesetz bei tiefen T. Das Einstein-Modell (Θ_E ≈ 0,806 Θ_D) fällt dagegen exponentiell ab.",
            "C_V^D = 9R (T/Θ_D)³ ∫₀^{Θ_D/T} x⁴eˣ/(eˣ−1)² dx,   C_V ≈ (12π⁴/5) R (T/Θ_D)³ für T ≪ Θ_D",
            listOf(Param("mat", "Festkörper", "", 1.0, choices = SOLIDS.map { "${it.name} (Θ_D = ${it.debye.toInt()} K)" }), Param("T", "Temperatur T", "K", 77.0, 0.1, 5000.0))
        ) { v ->
            val s = SOLIDS[v.getValue("mat").toInt()]; val t = v.getValue("T")
            val ts = grid(1.0, 1.5 * s.debye, 200)
            CalcResult(
                listOf(
                    Output("C_V Debye", debyeCv(t, s.debye), "J mol⁻¹ K⁻¹"),
                    Output("C_V Einstein (Θ_E = 0,806 Θ_D)", einsteinCv(t, 0.806 * s.debye), "J mol⁻¹ K⁻¹"),
                    Output("Dulong-Petit 3R", 3 * Phys.R, "J mol⁻¹ K⁻¹"),
                    Output("T³-Näherung", 12 * PI.pow(4) / 5 * Phys.R * (t / s.debye).pow(3), "J mol⁻¹ K⁻¹", "nur für T ≪ Θ_D")
                ),
                listOf(Curve("C_V/3R für ${s.name}", "T in K", "C_V / 3R", listOf(
                    CurveSeries("Debye", ts, ts.map { debyeCv(it, s.debye) / (3 * Phys.R) }),
                    CurveSeries("Einstein", ts, ts.map { einsteinCv(it, 0.806 * s.debye) / (3 * Phys.R) }, dashed = true)
                ), 0.0 to 1.1))
            )
        },
        Calculator(
            "entropy", Topic.THERMO, "Entropieänderung idealer Gase",
            "Entropie ist eine Zustandsfunktion: ΔS hängt nur von Anfangs- und Endzustand ab. Für das ideale Gas folgt sie aus dS = nc_V dT/T + nR dV/V. Die Mischungsentropie zweier Gase ist stets positiv.",
            "ΔS = n c_V ln(T₂/T₁) + n R ln(V₂/V₁),   ΔS_mix = −nR Σ xᵢ ln xᵢ",
            listOf(
                Param("n", "Stoffmenge n", "mol", 1.0, 1e-9, 1e9),
                Param("T1", "T₁", "K", 300.0, 1e-3, 1e6), Param("T2", "T₂", "K", 600.0, 1e-3, 1e6),
                Param("V1", "V₁", "L", 10.0, 1e-9, 1e9), Param("V2", "V₂", "L", 20.0, 1e-9, 1e9),
                Param("f", "Freiheitsgrade f (c_V = fR/2)", "", 3.0, 1.0, 12.0),
                Param("x", "Molenbruch x (Mischung)", "", 0.5, 1e-9, 1 - 1e-9)
            )
        ) { v ->
            val n = v.getValue("n"); val cv = v.getValue("f") / 2 * Phys.R
            val dsT = n * cv * ln(v.getValue("T2") / v.getValue("T1")); val dsV = n * Phys.R * ln(v.getValue("V2") / v.getValue("V1"))
            val x = v.getValue("x")
            CalcResult(
                listOf(
                    Output("ΔS aus Temperatur", dsT, "J/K"), Output("ΔS aus Volumen", dsV, "J/K"), Output("ΔS gesamt", dsT + dsV, "J/K"),
                    Output("Mischungsentropie", -n * Phys.R * (x * ln(x) + (1 - x) * ln(1 - x)), "J/K"),
                    Output("Zahl zugänglicher Mikrozustände: Ω₂/Ω₁ = e^{ΔS/k_B}, log₁₀", (dsT + dsV) / Phys.kB / ln(10.0), "")
                ),
                steps = listOf("ΔS = ${Fmt.num(n)}·${Fmt.num(cv)}·ln(${Fmt.num(v.getValue("T2"))}/${Fmt.num(v.getValue("T1"))}) + ${Fmt.num(n)}·8,314·ln(${Fmt.num(v.getValue("V2"))}/${Fmt.num(v.getValue("V1"))})")
            )
        },
        Calculator(
            "clausius", Topic.THERMO, "Dampfdruck (Clausius-Clapeyron)",
            "Integrierte Clausius-Clapeyron-Gleichung für konstante Verdampfungsenthalpie. Für Wasser: L ≈ 40,66 kJ/mol bei 100 °C.",
            "dp/dT = L/(TΔv) ≈ pL/(RT²)   ⇒   p(T) = p₀ exp[−(L/R)(1/T − 1/T₀)]",
            listOf(
                Param("L", "Verdampfungsenthalpie L", "kJ/mol", 40.66, 0.1, 1000.0),
                Param("T0", "Siedepunkt T₀ bei p₀", "°C", 100.0, -270.0, 3000.0),
                Param("p0", "Bezugsdruck p₀", "kPa", 101.325, 1e-6, 1e6),
                Param("T", "Temperatur T", "°C", 20.0, -270.0, 3000.0)
            )
        ) { v ->
            val l = v.getValue("L") * 1e3; val t0 = v.getValue("T0") + 273.15; val p0 = v.getValue("p0"); val t = v.getValue("T") + 273.15
            fun p(tk: Double) = p0 * exp(-l / Phys.R * (1 / tk - 1 / t0))
            val ts = grid(t0 - 100, t0 + 30, 160)
            CalcResult(
                listOf(Output("Dampfdruck p(T)", p(t), "kPa"), Output("Siedepunkt bei 70 kPa (≈ 3000 m)", 1 / (1 / t0 - Phys.R / l * ln(70 / p0)) - 273.15, "°C")),
                listOf(Curve("Dampfdruckkurve", "T in °C", "p in kPa", listOf(CurveSeries("p(T)", ts.map { it - 273.15 }, ts.map { p(it) })))),
                listOf("p = ${Fmt.num(p0)} kPa · exp[−(${Fmt.num(l)}/8,314)(1/${Fmt.num(t)} − 1/${Fmt.num(t0)})] = ${Fmt.num(p(t))} kPa")
            )
        }
    )
}

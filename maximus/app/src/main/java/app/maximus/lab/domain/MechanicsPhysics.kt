package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object MechanicsPhysics {
    const val M_SUN = 1.98892e30
    const val AU = 1.495978707e11
    private const val YEAR = 365.25 * 86400

    /** Preset bodies (mass in kg, radius in m) for the relativity calculator. */
    private val BODIES = listOf(
        Triple("Erde", 5.9722e24, 6.371e6), Triple("Sonne", M_SUN, 6.957e8), Triple("Weißer Zwerg (Sirius B)", 1.02 * M_SUN, 5.8e6),
        Triple("Neutronenstern", 1.4 * M_SUN, 1.2e4), Triple("Mond", 7.342e22, 1.7374e6)
    )

    /** Projectile with quadratic air drag, F = −k|v|v, integrated with RK4 until impact. Returns (t, x, y) samples. */
    fun projectile(v0: Double, angleDeg: Double, kOverM: Double, g: Double): List<DoubleArray> {
        val a = angleDeg * PI / 180
        val f = { _: Double, y: DoubleArray ->
            val sp = sqrt(y[2] * y[2] + y[3] * y[3])
            doubleArrayOf(y[2], y[3], -kOverM * sp * y[2], -g - kOverM * sp * y[3])
        }
        val tVac = 2 * v0 * sin(a) / g
        val dt = maxOf(tVac, 1e-3) / 2000
        val out = ArrayList<DoubleArray>()
        var y = doubleArrayOf(0.0, 0.0, v0 * cos(a), v0 * sin(a))
        var t = 0.0
        out += doubleArrayOf(t, y[0], y[1])
        while (out.size < 20000) {
            val next = Ode.rk4(f, y, t, t + dt, 1).y.last()
            if (next[1] < 0 && t > 0) {
                val s = y[1] / (y[1] - next[1])
                out += doubleArrayOf(t + s * dt, y[0] + s * (next[0] - y[0]), 0.0)
                break
            }
            y = next; t += dt
            out += doubleArrayOf(t, y[0], y[1])
        }
        return out
    }

    /** GR perihelion advance per orbit (rad): Δφ = 6πGM/(c²a(1 − e²)). */
    fun perihelionShift(mKg: Double, a: Double, e: Double) = 6 * PI * Phys.G * mKg / (Phys.c * Phys.c * a * (1 - e * e))

    val calculators: List<Calculator> = listOf(
        Calculator(
            "mech_projectile", Topic.MECHANICS, "Schiefer Wurf mit Luftwiderstand",
            "Ohne Luft ist die Bahn eine Parabel und 45° optimal. Mit quadratischer Reibung F = −k|v|v (k = ½ρc_wA) ist die DGL nichtlinear und wird mit Runge-Kutta 4 gelöst: die Bahn wird asymmetrisch, die optimale Abwurfhöhe sinkt unter 45°.",
            "m r̈ = −mg ê_z − k|v|v,   Vakuum: R = v₀² sin 2α/g,   h = v₀² sin²α/(2g)",
            listOf(
                Param("v0", "Abwurfgeschwindigkeit v₀", "m/s", 30.0, 0.1, 2000.0), Param("alpha", "Winkel α", "°", 45.0, 1.0, 89.0),
                Param("m", "Masse m", "kg", 0.43, 1e-3, 1e4), Param("cwA", "c_w · A", "m²", 0.0094, 0.0, 10.0, hint = "Fußball: c_w ≈ 0,25, A ≈ 0,038 m²"),
                Param("g", "g", "m/s²", 9.81, 0.1, 300.0)
            )
        ) { v ->
            val v0 = v.getValue("v0"); val al = v.getValue("alpha"); val g = v.getValue("g")
            val kOverM = 0.5 * 1.204 * v.getValue("cwA") / v.getValue("m")
            val traj = projectile(v0, al, kOverM, g)
            val a = al * PI / 180
            val rVac = v0 * v0 * sin(2 * a) / g
            val hVac = (v0 * sin(a)).pow(2) / (2 * g)
            val best = (10..80).maxBy { projectile(v0, it.toDouble(), kOverM, g).last()[1] }
            val vac = grid(0.0, rVac, 120)
            CalcResult(
                listOf(
                    Output("Wurfweite (mit Luft)", traj.last()[1], "m"), Output("Wurfweite (Vakuum)", rVac, "m"),
                    Output("Gipfelhöhe (mit Luft)", traj.maxOf { it[2] }, "m"), Output("Gipfelhöhe (Vakuum)", hVac, "m"),
                    Output("Flugzeit (mit Luft)", traj.last()[0], "s"),
                    Output("Endgeschwindigkeit v_∞ = √(g/(k/m))", if (kOverM > 0) sqrt(g / kOverM) else Double.POSITIVE_INFINITY, "m/s"),
                    Output("Optimaler Winkel (mit Luft, ganzzahlig)", best.toDouble(), "°", digits = 2)
                ),
                listOf(Curve("Bahnkurve", "x in m", "y in m", listOf(
                    CurveSeries("mit Luftwiderstand", traj.map { it[1] }, traj.map { it[2] }),
                    CurveSeries("Vakuum-Parabel", vac, vac.map { x -> x * kotlin.math.tan(a) - g * x * x / (2 * (v0 * cos(a)).pow(2)) }, dashed = true)
                ))),
                steps = listOf("k/m = ½ρc_wA/m = ${Fmt.num(kOverM, 4)} m⁻¹", "Vakuum: R = v₀² sin 2α/g = ${Fmt.num(rVac, 4)} m")
            )
        },
        Calculator(
            "mech_kepler", Topic.MECHANICS, "Kepler-Bahn und Periheldrehung",
            "Aus Zentralmasse, großer Halbachse und Exzentrizität folgen Periode (Kepler III), Bahngeschwindigkeiten (Vis-viva), Energie und Drehimpuls pro Masse. Die ART fügt eine Periheldrehung hinzu — mit den Merkur-Werten ergeben sich die berühmten 43″ pro Jahrhundert.",
            "T² = 4π²a³/(GM),   v² = GM(2/r − 1/a),   r(φ) = a(1 − ε²)/(1 + ε cos φ),   Δφ = 6πGM/(c²a(1 − ε²))",
            listOf(Param("M", "Zentralmasse", "M_☉", 1.0, 1e-9, 1e10), Param("a", "Große Halbachse a", "AE", 0.387098, 1e-6, 1e5), Param("e", "Exzentrizität ε", "", 0.205630, 0.0, 0.99))
        ) { v ->
            val m = v.getValue("M") * M_SUN; val a = v.getValue("a") * AU; val e = v.getValue("e")
            val gm = Phys.G * m
            val period = 2 * PI * sqrt(a * a * a / gm)
            val rp = a * (1 - e); val ra = a * (1 + e)
            val vp = sqrt(gm * (2 / rp - 1 / a)); val va = sqrt(gm * (2 / ra - 1 / a))
            val dphi = perihelionShift(m, a, e)
            val perCentury = dphi * (100 * YEAR / period) * 180 / PI * 3600
            val phis = grid(0.0, 2 * PI, 240)
            val p = a * (1 - e * e)
            CalcResult(
                listOf(
                    Output("Umlaufzeit T", period / 86400, "Tage"), Output("Perihel r_p", rp / AU, "AE"), Output("Aphel r_a", ra / AU, "AE"),
                    Output("v im Perihel", vp / 1e3, "km/s"), Output("v im Aphel", va / 1e3, "km/s"),
                    Output("Spez. Energie E/m = −GM/2a", -gm / (2 * a), "J/kg"), Output("Spez. Drehimpuls L/m = √(GMp)", sqrt(gm * p), "m²/s"),
                    Output("ART-Periheldrehung pro Umlauf", dphi * 180 / PI * 3600, "″"), Output("ART-Periheldrehung pro Jahrhundert", perCentury, "″")
                ),
                listOf(Curve("Bahn (Zentralkörper im Ursprung)", "x in AE", "y in AE", listOf(
                    CurveSeries("Bahn", phis.map { p / (1 + e * cos(it)) * cos(it) / AU }, phis.map { p / (1 + e * cos(it)) * sin(it) / AU }),
                    CurveSeries("Brennpunkt", listOf(0.0), listOf(0.0))
                ))),
                steps = listOf(
                    "GM = ${Fmt.num(gm, 5)} m³/s²", "T = 2π√(a³/GM) = ${Fmt.num(period, 5)} s",
                    "v_p/v_a = r_a/r_p = (1 + ε)/(1 − ε) = ${Fmt.num((1 + e) / (1 - e), 4)} (Flächensatz)"
                )
            )
        },
        Calculator(
            "mech_oscillator", Topic.MECHANICS, "Gedämpfter, getriebener Oszillator",
            "Resonanzkurve, Phasengang und Einschwingvorgang. Die Lösung ist Einschwingen (homogen, klingt mit e^{−γt} ab) plus stationäre Antwort mit Amplitude A(ω) und Phase φ(ω).",
            "ẍ + 2γẋ + ω₀²x = f₀ cos ωt,   A = f₀/√((ω₀² − ω²)² + 4γ²ω²),   tan φ = 2γω/(ω₀² − ω²)",
            listOf(
                Param("w0", "Eigenfrequenz ω₀", "s⁻¹", 2 * PI, 1e-3, 1e4), Param("gamma", "Dämpfung γ", "s⁻¹", 0.4, 0.0, 1e4),
                Param("f0", "Antrieb f₀", "m/s²", 1.0, 0.0, 1e6), Param("w", "Antriebsfrequenz ω", "s⁻¹", 2 * PI, 0.0, 1e5)
            )
        ) { v ->
            val w0 = v.getValue("w0"); val g = v.getValue("gamma"); val f0 = v.getValue("f0"); val w = v.getValue("w")
            fun amp(x: Double) = f0 / sqrt((w0 * w0 - x * x).pow(2) + 4 * g * g * x * x)
            val regime = when { g < w0 -> "Schwingfall"; g == w0 -> "aperiodischer Grenzfall"; else -> "Kriechfall" }
            val tEnd = minOf(12 * 2 * PI / w0, if (g > 0) 8 / g else 1e9)
            val sol = Ode.rk4({ t, y -> doubleArrayOf(y[1], f0 * cos(w * t) - 2 * g * y[1] - w0 * w0 * y[0]) }, doubleArrayOf(0.0, 0.0), 0.0, tEnd, 2000)
            val ws = grid(0.0, 2.5 * w0, 300)
            CalcResult(
                listOf(
                    Output("Regime", 0.0, display = regime), Output("Güte Q = ω₀/2γ", if (g > 0) w0 / (2 * g) else Double.POSITIVE_INFINITY, ""),
                    Output("Gedämpfte Frequenz ω_d", if (g < w0) sqrt(w0 * w0 - g * g) else 0.0, "s⁻¹"),
                    Output("Resonanzfrequenz √(ω₀² − 2γ²)", if (2 * g * g < w0 * w0) sqrt(w0 * w0 - 2 * g * g) else 0.0, "s⁻¹"),
                    Output("Stationäre Amplitude A(ω)", amp(w), "m"), Output("Phasenverschiebung φ", atan2(2 * g * w, w0 * w0 - w * w) * 180 / PI, "°"),
                    Output("Mittlere Leistung pro Masse", f0 * f0 * g * w * w / ((w0 * w0 - w * w).pow(2) + 4 * g * g * w * w), "W/kg")
                ),
                listOf(
                    Curve("Resonanzkurve", "ω in s⁻¹", "A in m", listOf(CurveSeries("A(ω)", ws, ws.map { amp(it) }))),
                    Curve("Phasengang", "ω in s⁻¹", "φ in Grad", listOf(CurveSeries("φ(ω)", ws, ws.map { atan2(2 * g * it, w0 * w0 - it * it) * 180 / PI }))),
                    Curve("Einschwingvorgang x(t), Start in Ruhe", "t in s", "x in m", listOf(
                        CurveSeries("x(t) (RK4)", sol.t, sol.y.map { it[0] }),
                        CurveSeries("stationär", sol.t, sol.t.map { amp(w) * cos(w * it - atan2(2 * g * w, w0 * w0 - w * w)) }, dashed = true)
                    ))
                )
            )
        },
        Calculator(
            "mech_coupled", Topic.MECHANICS, "Gekoppelte Pendel und Normalmoden",
            "Zwei gleiche Pendel, durch eine Feder gekoppelt. Die Säkulargleichung det(K − ω²M) = 0 liefert die gleich- und gegenphasige Normalmode. Startet nur ein Pendel, wandert die Energie mit der Schwebungsfrequenz hin und her.",
            "ẍ₁ = −ω₀²x₁ − κ(x₁ − x₂),   ẍ₂ = −ω₀²x₂ − κ(x₂ − x₁),   ω₁ = ω₀,   ω₂ = √(ω₀² + 2κ)",
            listOf(Param("l", "Pendellänge l", "m", 1.0, 0.01, 100.0), Param("kappa", "Kopplung κ = k/m", "s⁻²", 0.5, 0.0, 1e3), Param("t", "Zeitspanne", "s", 40.0, 1.0, 600.0))
        ) { v ->
            val w0sq = 9.81 / v.getValue("l"); val kap = v.getValue("kappa"); val tEnd = v.getValue("t")
            val mat = Matrix(2, 2, arrayOf(doubleArrayOf(w0sq + kap, -kap), doubleArrayOf(-kap, w0sq + kap)))
            val ev = mat.eigenvalues().map { it.re }.sorted()
            val w1 = sqrt(ev[0]); val w2 = sqrt(ev[1])
            val sol = Ode.rk4({ _, y -> doubleArrayOf(y[2], y[3], -w0sq * y[0] - kap * (y[0] - y[1]), -w0sq * y[1] - kap * (y[1] - y[0])) },
                doubleArrayOf(0.1, 0.0, 0.0, 0.0), 0.0, tEnd, 4000)
            CalcResult(
                listOf(
                    Output("ω₁ (gleichphasig)", w1, "s⁻¹"), Output("ω₂ (gegenphasig)", w2, "s⁻¹"),
                    Output("Schwebungsperiode 2π/(ω₂ − ω₁)", if (w2 > w1) 2 * PI / (w2 - w1) else Double.POSITIVE_INFINITY, "s"),
                    Output("Energieübertrag nach", if (w2 > w1) PI / (w2 - w1) else Double.POSITIVE_INFINITY, "s")
                ),
                listOf(Curve("Auslenkungen (x₁(0) = 0,1, x₂(0) = 0)", "t in s", "x", listOf(
                    CurveSeries("Pendel 1", sol.t, sol.y.map { it[0] }), CurveSeries("Pendel 2", sol.t, sol.y.map { it[1] }, dashed = true)
                ))),
                steps = listOf("K = [[ω₀² + κ, −κ], [−κ, ω₀² + κ]] mit ω₀² = ${Fmt.num(w0sq, 4)} s⁻²", "Eigenwerte ω² = ${Fmt.num(ev[0], 5)}, ${Fmt.num(ev[1], 5)}; Moden (1, 1)/√2 und (1, −1)/√2")
            )
        },
        Calculator(
            "mech_sr", Topic.MECHANICS, "Spezielle Relativität",
            "Lorentz-Faktor, Rapidität, Zeitdilatation, Längenkontraktion, Doppler-Effekt und Energie eines Teilchens bei gegebener Geschwindigkeit. Beispiel: Myonen mit β = 0,995 leben im Laborsystem rund zehnmal länger.",
            "γ = 1/√(1 − β²),   η = artanh β,   Δt = γΔτ,   L = L₀/γ,   f'/f = √((1 + β)/(1 − β)),   E = γmc²",
            listOf(
                Param("beta", "Geschwindigkeit β = v/c", "", 0.995, 0.0, 0.999999999), Param("tau", "Eigenzeit Δτ", "μs", 2.197, 0.0, 1e12),
                Param("L0", "Eigenlänge L₀", "m", 100.0, 0.0, 1e20), Param("m", "Ruhemasse", "MeV/c²", Phys.mmuMeV, 0.0, 1e9)
            )
        ) { v ->
            val b = v.getValue("beta"); val gam = 1 / sqrt(1 - b * b); val m = v.getValue("m")
            val eta = 0.5 * ln((1 + b) / (1 - b))
            val bs = grid(0.0, 0.99, 200)
            CalcResult(
                listOf(
                    Output("Lorentz-Faktor γ", gam, ""), Output("Rapidität η", eta, ""),
                    Output("Laborzeit Δt = γΔτ", gam * v.getValue("tau"), "μs"), Output("Flugstrecke βcγΔτ", b * Phys.c * gam * v.getValue("tau") * 1e-6, "m"),
                    Output("Kontrahierte Länge L₀/γ", v.getValue("L0") / gam, "m"),
                    Output("Doppler-Faktor (Annäherung)", sqrt((1 + b) / (1 - b)), ""), Output("Doppler-Faktor (Entfernung)", sqrt((1 - b) / (1 + b)), ""),
                    Output("Gesamtenergie γmc²", gam * m, "MeV"), Output("Kinetische Energie (γ − 1)mc²", (gam - 1) * m, "MeV"), Output("Impuls γmβc", gam * m * b, "MeV/c")
                ),
                listOf(Curve("Lorentz-Faktor", "β", "γ", listOf(
                    CurveSeries("γ(β)", bs, bs.map { 1 / sqrt(1 - it * it) }), CurveSeries("1 + β²/2 (klassisch)", bs, bs.map { 1 + it * it / 2 }, dashed = true)
                ))),
                steps = listOf("γ = 1/√(1 − ${Fmt.num(b, 6)}²) = ${Fmt.num(gam, 5)}", "Kontrolle: E² − (pc)² = (mc²)² = ${Fmt.num((gam * m).pow(2) - (gam * m * b).pow(2), 5)} MeV²")
            )
        },
        Calculator(
            "mech_gr", Topic.MECHANICS, "Schwarzschild-Raumzeit und Uhren im Orbit",
            "Schwarzschild-Radius, gravitative Rotverschiebung und Lichtablenkung am Rand eines Körpers. Für eine Uhr auf einer Kreisbahn in Höhe h kombinieren sich ART (schneller oben) und SRT (langsamer durch Bahngeschwindigkeit): dτ/dt = √(1 − 3GM/(rc²)). Erde mit h = 20 200 km ergibt die GPS-Korrektur von ≈ +38 μs/Tag.",
            "r_s = 2GM/c²,   dτ/dt = √(1 − r_s/r),   Kreisbahn: dτ/dt = √(1 − 3GM/(rc²)),   δ = 4GM/(c²b)",
            listOf(Param("body", "Körper", "", 0.0, choices = BODIES.map { it.first }), Param("h", "Bahnhöhe h", "km", 20200.0, 0.0, 1e9))
        ) { v ->
            val (name, m, rr) = BODIES[v.getValue("body").toInt()]
            val gm = Phys.G * m; val c2 = Phys.c * Phys.c
            val rs = 2 * gm / c2
            val r = rr + v.getValue("h") * 1e3
            val surface = sqrt(1 - rs / rr)
            val orbit = sqrt(1 - 3 * gm / (r * c2))
            val day = 86400e6
            val xs = grid(1.0, 10.0, 200)
            CalcResult(
                listOf(
                    Output("Schwarzschild-Radius r_s", rs, "m"), Output("Kompaktheit r_s/R", rs / rr, ""),
                    Output("Rotverschiebung von der Oberfläche z", 1 / surface - 1, ""),
                    Output("Lichtablenkung am Rand", 4 * gm / (c2 * rr) * 180 / PI * 3600, "″"),
                    Output("Orbit vs. Oberfläche (gesamt)", (orbit / surface - 1) * day, "μs/Tag"),
                    Output("davon ART", (sqrt(1 - rs / r) / surface - 1) * day, "μs/Tag"),
                    Output("davon SRT (v² = GM/r)", (orbit / sqrt(1 - rs / r) - 1) * day, "μs/Tag"),
                    Output("Photonensphäre 1,5 r_s / ISCO 3 r_s", 0.0, display = "${Fmt.num(1.5 * rs, 4)} m / ${Fmt.num(3 * rs, 4)} m")
                ),
                listOf(Curve("Gangrate einer ruhenden Uhr ($name)", "r/R", "1 − dτ/dt", listOf(CurveSeries("1 − √(1 − r_s/r)", xs, xs.map { 1 - sqrt(1 - rs / (it * rr)) })))),
                warnings = if (abs(rs / rr) > 0.3) listOf("Starkes Feld: schwache-Feld-Näherungen (Lichtablenkung) sind nur grob.") else emptyList()
            )
        }
    )

    /** Energy check helper used in tests: relative drift of ½v² + ½ω²x² for the undamped oscillator under RK4. */
    fun harmonicEnergyDrift(w: Double, steps: Int): Double {
        val sol = Ode.rk4({ _, y -> doubleArrayOf(y[1], -w * w * y[0]) }, doubleArrayOf(1.0, 0.0), 0.0, 20 * PI / w, steps)
        val e0 = 0.5 * w * w
        return abs(0.5 * sol.y.last()[1].pow(2) + 0.5 * w * w * sol.y.last()[0].pow(2) - e0) / e0
    }
}

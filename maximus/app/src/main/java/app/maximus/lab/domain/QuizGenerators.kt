package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/** Randomised calculation problems. Each value is drawn from a "nice" grid so the numbers stay readable. */
internal object QuizGenerators {
    private fun Random.pick(vararg xs: Double) = xs[nextInt(xs.size)]
    private fun Random.int(lo: Int, hi: Int) = lo + nextInt(hi - lo + 1)
    private fun n(v: Double, d: Int = 4) = Fmt.num(v, d)
    private var counter = 0
    private fun id(prefix: String) = "$prefix-${++counter}"

    private fun q(topic: Topic, diff: Int, prompt: String, answer: Double, unit: String, solution: String, chapter: String, tol: Double = 0.02) =
        Question(id(topic.short), topic, diff, prompt, solution, answer = answer, unit = unit, tolerance = tol, chapterKey = chapter)

    private val thermo = listOf(
        QuestionGenerator { r ->
            val tw = r.int(30, 90) * 10.0; val tk = r.int(1, 6) * 10.0
            val eta = 1 - (tk + 273.15) / (tw + 273.15)
            q(Topic.THERMO, 1, "Eine Wärmekraftmaschine arbeitet zwischen ${n(tw)} °C und ${n(tk)} °C. Wie groß ist ihr maximaler (Carnot-)Wirkungsgrad in Prozent?",
                100 * eta, "%", "η_C = 1 − T_k/T_w mit Kelvin: 1 − ${n(tk + 273.15)}/${n(tw + 273.15)} = ${n(eta)} → ${n(100 * eta)} %. Celsius einzusetzen wäre der klassische Fehler.", "th_cycles")
        },
        QuestionGenerator { r ->
            val nm = r.pick(0.5, 1.0, 2.0, 3.0); val t = r.int(27, 127).toDouble(); val v = r.pick(10.0, 20.0, 25.0, 50.0)
            val p = nm * Phys.R * (t + 273.15) / (v * 1e-3) / 1e3
            q(Topic.THERMO, 1, "${n(nm)} mol eines idealen Gases nehmen bei ${n(t)} °C ein Volumen von ${n(v)} L ein. Welcher Druck in kPa herrscht?",
                p, "kPa", "p = nRT/V = ${n(nm)} · 8,314 · ${n(t + 273.15)} / ${n(v * 1e-3)} m³ = ${n(p * 1e3)} Pa = ${n(p)} kPa.", "th_gas")
        },
        QuestionGenerator { r ->
            val t = r.pick(3000.0, 4000.0, 5800.0, 6000.0, 10000.0, 310.0)
            val l = Phys.wienB / t * 1e9
            q(Topic.THERMO, 1, "Bei welcher Wellenlänge (in nm) strahlt ein schwarzer Körper der Temperatur ${n(t)} K am stärksten?",
                l, "nm", "Wien: λ_max = b/T = 2,898·10⁻³ m K / ${n(t)} K = ${n(l)} nm.", "th_stat")
        },
        QuestionGenerator { r ->
            val nm = r.pick(1.0, 2.0); val t = r.pick(300.0, 350.0, 400.0); val ratio = r.pick(2.0, 3.0, 4.0, 10.0)
            val w = nm * Phys.R * t * ln(ratio)
            q(Topic.THERMO, 2, "${n(nm)} mol ideales Gas expandieren isotherm bei ${n(t)} K auf das ${n(ratio)}-fache Volumen. Wie viel Arbeit (in J, Betrag) gibt das Gas ab?",
                w, "J", "W = nRT ln(V₂/V₁) = ${n(nm)} · 8,314 · ${n(t)} · ln ${n(ratio)} = ${n(w)} J. Da ΔU = 0, nimmt das Gas genau diese Wärme auf.", "th_gas")
        },
        QuestionGenerator { r ->
            val rad = r.pick(0.05, 0.1, 0.2); val t = r.pick(500.0, 800.0, 1000.0, 1500.0)
            val p = Phys.sigmaSB * 4 * PI * rad * rad * t.pow(4)
            q(Topic.THERMO, 2, "Eine schwarze Kugel mit Radius ${n(rad * 100)} cm hat die Temperatur ${n(t)} K. Welche Leistung in W strahlt sie ab?",
                p, "W", "P = σ·4πr²·T⁴ = 5,670·10⁻⁸ · 4π·${n(rad)}² · ${n(t)}⁴ = ${n(p)} W.", "th_transport")
        },
        QuestionGenerator { r ->
            val m1 = r.pick(100.0, 200.0, 300.0); val t1 = r.pick(80.0, 70.0, 90.0); val m2 = r.pick(100.0, 150.0, 200.0); val t2 = r.pick(10.0, 20.0, 15.0)
            val tm = (m1 * t1 + m2 * t2) / (m1 + m2)
            q(Topic.THERMO, 1, "${n(m1)} g Wasser von ${n(t1)} °C werden mit ${n(m2)} g Wasser von ${n(t2)} °C gemischt. Mischtemperatur in °C (ohne Verluste)?",
                tm, "°C", "Gleiche spezifische Wärme: T_m = (m₁T₁ + m₂T₂)/(m₁ + m₂) = ${n(tm)} °C.", "ca_calorimetry")
        },
        QuestionGenerator { r ->
            val t = r.pick(273.0, 300.0, 400.0, 600.0)
            val v = sqrt(3 * Phys.kB * t / (28.014e-3 / Phys.NA))
            q(Topic.THERMO, 2, "Wie groß ist die effektive (quadratisch gemittelte) Geschwindigkeit v_rms von N₂-Molekülen (M = 28 g/mol) bei ${n(t)} K in m/s?",
                v, "m/s", "v_rms = √(3RT/M) = √(3 · 8,314 · ${n(t)} / 0,028) = ${n(v)} m/s.", "th_gas")
        },
        QuestionGenerator { r ->
            val rr = r.pick(8.0, 9.0, 10.0, 11.0, 12.0)
            val eta = ThermoPhysics.otto(rr, 1.4)
            q(Topic.THERMO, 2, "Idealer Otto-Prozess mit Verdichtungsverhältnis r = ${n(rr)} und γ = 1,4: Wirkungsgrad in Prozent?",
                100 * eta, "%", "η = 1 − r^{1−γ} = 1 − ${n(rr)}^{−0,4} = ${n(eta)}.", "th_cycles")
        }
    )

    private val electro = listOf(
        QuestionGenerator { r ->
            val q1 = r.pick(1.0, 2.0, 5.0); val q2 = r.pick(-3.0, 3.0, 4.0); val d = r.pick(5.0, 10.0, 20.0)
            val f = ElectroPhysics.K * q1 * 1e-6 * abs(q2) * 1e-6 / (d / 100).pow(2)
            q(Topic.ELECTRO, 1, "Zwei Punktladungen ${n(q1)} μC und ${n(q2)} μC sind ${n(d)} cm voneinander entfernt. Wie groß ist der Betrag der Coulomb-Kraft in N?",
                f, "N", "F = q₁q₂/(4πε₀r²) = 8,988·10⁹ · ${n(q1)}·10⁻⁶ · ${n(abs(q2))}·10⁻⁶ / (${n(d / 100)})² = ${n(f)} N (${if (q2 < 0) "anziehend" else "abstoßend"}).", "el_static")
        },
        QuestionGenerator { r ->
            val a = r.pick(50.0, 100.0, 200.0); val d = r.pick(0.5, 1.0, 2.0); val er = r.pick(1.0, 2.2, 4.0)
            val c = Phys.eps0 * er * a * 1e-4 / (d * 1e-3) * 1e12
            q(Topic.ELECTRO, 1, "Plattenkondensator: Fläche ${n(a)} cm², Abstand ${n(d)} mm, ε_r = ${n(er)}. Kapazität in pF?",
                c, "pF", "C = ε₀ε_rA/d = 8,854·10⁻¹² · ${n(er)} · ${n(a * 1e-4)} / ${n(d * 1e-3)} = ${n(c)} pF.", "el_static")
        },
        QuestionGenerator { r ->
            val res = r.pick(1.0, 2.2, 4.7, 10.0); val cap = r.pick(10.0, 22.0, 47.0, 100.0); val u0 = r.pick(5.0, 10.0, 12.0)
            val tau = res * 1e3 * cap * 1e-6
            val t = tau * r.pick(0.5, 1.0, 2.0)
            val u = u0 * (1 - exp(-t / tau))
            q(Topic.ELECTRO, 2, "Ein Kondensator C = ${n(cap)} μF wird über R = ${n(res)} kΩ an ${n(u0)} V geladen. Welche Spannung (V) liegt nach ${n(t * 1e3)} ms an?",
                u, "V", "τ = RC = ${n(tau * 1e3)} ms; u = U₀(1 − e^{−t/τ}) = ${n(u0)}(1 − e^{−${n(t / tau)}}) = ${n(u)} V.", "el_circuits")
        },
        QuestionGenerator { r ->
            val l = r.pick(1.0, 10.0, 100.0); val c = r.pick(1.0, 10.0, 100.0)
            val f = 1 / (2 * PI * sqrt(l * 1e-3 * c * 1e-9)) / 1e3
            q(Topic.ELECTRO, 1, "Schwingkreis mit L = ${n(l)} mH und C = ${n(c)} nF: Eigenfrequenz in kHz?",
                f, "kHz", "f₀ = 1/(2π√(LC)) = 1/(2π√(${n(l * 1e-3)}·${n(c * 1e-9)})) = ${n(f)} kHz.", "el_circuits")
        },
        QuestionGenerator { r ->
            val i = r.pick(5.0, 10.0, 20.0, 50.0); val d = r.pick(1.0, 2.0, 5.0, 10.0)
            val b = Phys.mu0 * i / (2 * PI * d / 100) * 1e6
            q(Topic.ELECTRO, 1, "Welches Magnetfeld (in μT) erzeugt ein langer gerader Draht mit ${n(i)} A im Abstand ${n(d)} cm?",
                b, "μT", "B = μ₀I/(2πr) = 4π·10⁻⁷ · ${n(i)} / (2π · ${n(d / 100)}) = ${n(b)} μT. Zum Vergleich: Erdfeld ≈ 50 μT.", "el_magneto")
        },
        QuestionGenerator { r ->
            val e = r.pick(1.0, 2.0, 5.0, 10.0); val b = r.pick(1.0, 2.0, 5.0)
            val v = sqrt(2 * e * 1e3 * Phys.e / Phys.me)
            val rad = Phys.me * v / (Phys.e * b * 1e-3) * 100
            q(Topic.ELECTRO, 2, "Ein Elektron mit ${n(e)} keV kinetischer Energie bewegt sich senkrecht zu einem Feld von ${n(b)} mT. Bahnradius in cm (nichtrelativistisch)?",
                rad, "cm", "v = √(2E/m) = ${n(v)} m/s; r = mv/(eB) = ${n(rad)} cm.", "el_magneto", tol = 0.03)
        },
        QuestionGenerator { r ->
            val n1 = r.pick(500.0, 1000.0, 2000.0); val n2 = r.pick(50.0, 100.0, 200.0); val u1 = r.pick(230.0, 400.0)
            val u2 = u1 * n2 / n1
            q(Topic.ELECTRO, 1, "Idealer Transformator: N₁ = ${n(n1)}, N₂ = ${n(n2)}, U₁ = ${n(u1)} V. Sekundärspannung in V?",
                u2, "V", "U₂ = U₁ N₂/N₁ = ${n(u2)} V.", "el_induction")
        },
        QuestionGenerator { r ->
            val c = r.pick(10.0, 100.0, 470.0, 1000.0); val u = r.pick(5.0, 12.0, 50.0)
            val w = 0.5 * c * 1e-6 * u * u * 1e3
            q(Topic.ELECTRO, 1, "Wie viel Energie (mJ) speichert ein Kondensator mit ${n(c)} μF bei ${n(u)} V?",
                w, "mJ", "W = CU²/2 = ${n(c * 1e-6)} · ${n(u)}² / 2 = ${n(w)} mJ.", "el_static")
        }
    )

    private val quantum = listOf(
        QuestionGenerator { r ->
            val l = r.pick(400.0, 500.0, 532.0, 633.0, 700.0, 1064.0, 254.0)
            val e = 1239.84198 / l
            q(Topic.QUANTUM, 1, "Welche Energie (eV) hat ein Photon der Wellenlänge ${n(l)} nm?",
                e, "eV", "E = hc/λ = 1239,84 eV nm / ${n(l)} nm = ${n(e)} eV.", "qm_origins")
        },
        QuestionGenerator { r ->
            val e = r.pick(10.0, 54.0, 100.0, 150.0, 1000.0)
            val l = Phys.h / sqrt(2 * Phys.me * e * Phys.e) * 1e9
            q(Topic.QUANTUM, 1, "De-Broglie-Wellenlänge (nm) eines Elektrons mit ${n(e)} eV kinetischer Energie (nichtrelativistisch)?",
                l, "nm", "λ = h/√(2mE) = 1,226 nm/√(E/eV) = ${n(l)} nm. (54 eV: Davisson-Germer!)", "qm_origins")
        },
        QuestionGenerator { r ->
            val l = r.pick(0.5, 1.0, 2.0); val nn = r.int(1, 4)
            val e = nn * nn * PI * PI * Phys.hbar * Phys.hbar / (2 * Phys.me * (l * 1e-9).pow(2)) / Phys.e
            q(Topic.QUANTUM, 2, "Elektron im unendlich tiefen Potentialtopf der Breite ${n(l)} nm: Energie des Niveaus n = $nn in eV?",
                e, "eV", "Eₙ = n²h²/(8mL²) = $nn² · 0,376 eV/(L/nm)² = ${n(e)} eV.", "qm_1d")
        },
        QuestionGenerator { r ->
            val n1 = r.int(1, 3); val n2 = n1 + r.int(1, 3)
            val de = Phys.RyEv / (1 + Phys.me / Phys.mp) * (1.0 / (n1 * n1) - 1.0 / (n2 * n2))
            val l = 1239.84198 / de
            q(Topic.QUANTUM, 2, "Wasserstoff: Welche Wellenlänge (nm) hat das Photon beim Übergang n = $n2 → n = $n1?",
                l, "nm", "ΔE = 13,6 eV (1/$n1² − 1/$n2²) = ${n(de)} eV (mit reduzierter Masse); λ = hc/ΔE = ${n(l)} nm.", "qm_hydrogen", tol = 0.01)
        },
        QuestionGenerator { r ->
            val l = r.pick(200.0, 250.0, 300.0, 350.0); val w = r.pick(2.14, 2.29, 4.33, 4.65)
            val ek = 1239.84198 / l - w
            q(Topic.QUANTUM, 1, "Licht der Wellenlänge ${n(l)} nm fällt auf ein Metall mit Austrittsarbeit ${n(w)} eV. Maximale kinetische Energie der Photoelektronen in eV?",
                ek, "eV", "E_kin = hc/λ − W_A = ${n(1239.84198 / l)} − ${n(w)} = ${n(ek)} eV.", "qm_origins")
        },
        QuestionGenerator { r ->
            val th = r.pick(30.0, 60.0, 90.0, 120.0, 180.0)
            val d = Phys.lambdaC * (1 - kotlin.math.cos(th * PI / 180)) * 1e12
            q(Topic.QUANTUM, 1, "Compton-Streuung unter ${n(th)}°: Wellenlängenverschiebung in pm?",
                d, "pm", "Δλ = λ_C(1 − cos θ) = 2,426 pm · (1 − cos ${n(th)}°) = ${n(d)} pm.", "qm_origins")
        },
        QuestionGenerator { r ->
            val f = r.pick(10.0, 30.0, 64.0, 100.0)
            val e = Phys.h * f * 1e12 / Phys.e * 1e3
            q(Topic.QUANTUM, 2, "Ein Molekül schwingt harmonisch mit ${n(f)} THz. Abstand zweier benachbarter Energieniveaus in meV?",
                e, "meV", "ΔE = ħω = hf = 4,1357·10⁻¹⁵ eV s · ${n(f)}·10¹² Hz = ${n(e)} meV.", "qm_1d")
        }
    )

    private val semiconductor = listOf(
        QuestionGenerator { r ->
            val nd = r.pick(1e15, 1e16, 1e17, 1e18)
            val p = 1e20 / nd
            q(Topic.SEMICONDUCTOR, 1, "n-Silizium mit N_D = ${n(nd)} cm⁻³ bei 300 K (nᵢ = 10¹⁰ cm⁻³): Löcherdichte in cm⁻³?",
                p, "cm⁻³", "Massenwirkungsgesetz: p = nᵢ²/n ≈ nᵢ²/N_D = 10²⁰/${n(nd)} = ${n(p)} cm⁻³.", "sc_carriers")
        },
        QuestionGenerator { r ->
            val na = r.pick(1e16, 1e17, 1e18); val nd = r.pick(1e15, 1e16, 1e17)
            val v = 0.025852 * ln(na * nd / 1e20)
            q(Topic.SEMICONDUCTOR, 2, "Si-pn-Übergang mit N_A = ${n(na)} cm⁻³ und N_D = ${n(nd)} cm⁻³ bei 300 K (k_BT/e = 25,85 mV, nᵢ = 10¹⁰ cm⁻³). Diffusionsspannung in V?",
                v, "V", "V_bi = (k_BT/e) ln(N_AN_D/nᵢ²) = 0,02585 · ln(${n(na * nd / 1e20)}) = ${n(v)} V.", "sc_pn")
        },
        QuestionGenerator { r ->
            val nd = r.pick(1e14, 1e15, 1e16); val mu = 1350.0
            val rho = 1 / (Phys.e * nd * mu)
            q(Topic.SEMICONDUCTOR, 1, "n-Si mit N_D = ${n(nd)} cm⁻³ und μ_n = 1350 cm²/(V s): spezifischer Widerstand in Ω cm?",
                rho, "Ω cm", "ρ = 1/(e n μ_n) = 1/(1,602·10⁻¹⁹ · ${n(nd)} · 1350) = ${n(rho)} Ω cm.", "sc_transport")
        },
        QuestionGenerator { r ->
            val iS = r.pick(1e-14, 1e-12, 1e-10); val v = r.pick(0.4, 0.5, 0.6, 0.7)
            val i = iS * (exp(v / 0.025852) - 1) * 1e3
            q(Topic.SEMICONDUCTOR, 2, "Ideale Diode, I_s = ${n(iS)} A, V = ${n(v)} V, T = 300 K (V_T = 25,85 mV). Strom in mA?",
                i, "mA", "I = I_s(e^{V/V_T} − 1) = ${n(iS)} · (e^{${n(v / 0.025852)}} − 1) = ${n(i)} mA. Jede 60 mV verzehnfachen den Strom.", "sc_pn", tol = 0.03)
        },
        QuestionGenerator { r ->
            val eg = r.pick(0.66, 1.12, 1.42, 2.26, 3.4)
            val l = 1239.84198 / eg
            q(Topic.SEMICONDUCTOR, 1, "Ein Halbleiter hat die Bandlücke ${n(eg)} eV. Bis zu welcher Wellenlänge (nm) absorbiert er Licht?",
                l, "nm", "λ_g = hc/E_g = 1239,84/${n(eg)} = ${n(l)} nm.", "sc_bands")
        },
        QuestionGenerator { r ->
            val nd = r.pick(1e15, 1e16, 1e17)
            val de = 0.025852 * ln(nd / 1e10)
            q(Topic.SEMICONDUCTOR, 2, "Wie weit (in eV) liegt das Fermi-Niveau von n-Si (N_D = ${n(nd)} cm⁻³, 300 K) über dem intrinsischen Niveau E_i?",
                de, "eV", "E_F − E_i = k_BT ln(n/nᵢ) = 0,02585 · ln(${n(nd / 1e10)}) = ${n(de)} eV.", "sc_carriers")
        },
        QuestionGenerator { r ->
            val i = r.pick(1.0, 5.0, 10.0); val b = r.pick(0.2, 0.5, 1.0); val d = r.pick(100.0, 500.0); val uh = r.pick(1.0, 2.0, 5.0)
            val nn = i * 1e-3 * b / (Phys.e * d * 1e-6 * uh * 1e-3) * 1e-6
            q(Topic.SEMICONDUCTOR, 2, "Hall-Messung: I = ${n(i)} mA, B = ${n(b)} T, Dicke ${n(d)} μm, |U_H| = ${n(uh)} mV. Ladungsträgerdichte in cm⁻³?",
                nn, "cm⁻³", "n = IB/(e d U_H) = ${n(nn * 1e6)} m⁻³ = ${n(nn)} cm⁻³.", "sc_transport")
        }
    )

    private val caloric = listOf(
        QuestionGenerator { r ->
            val m = r.pick(50.0, 100.0, 250.0)
            val qk = m * 334.0 / 1e3
            q(Topic.CALORIC, 1, "Wie viel Wärme (kJ) braucht man, um ${n(m)} g Eis von 0 °C zu schmelzen (L_s = 334 J/g)?",
                qk, "kJ", "Q = mL = ${n(m)} g · 334 J/g = ${n(qk)} kJ — so viel wie für das Erwärmen derselben Wassermenge um 80 K.", "ca_calorimetry")
        },
        QuestionGenerator { r ->
            val mw = r.pick(200.0, 300.0); val tw = r.pick(20.0, 25.0); val mm = r.pick(100.0, 200.0); val tm = r.pick(100.0, 150.0, 200.0)
            val c = 0.385
            val t = (mw * 4.186 * tw + mm * c * tm) / (mw * 4.186 + mm * c)
            q(Topic.CALORIC, 2, "${n(mm)} g Kupfer (c = 0,385 J/(g K)) mit ${n(tm)} °C fallen in ${n(mw)} g Wasser von ${n(tw)} °C. Mischtemperatur in °C?",
                t, "°C", "T_m = (m_w c_w T_w + m_Cu c_Cu T_Cu)/(m_w c_w + m_Cu c_Cu) = ${n(t)} °C.", "ca_calorimetry")
        },
        QuestionGenerator { r ->
            val t = r.pick(300.0, 350.0, 400.0); val p = r.pick(-200.0, -500.0, -1000.0); val e = r.pick(100.0, 200.0, 500.0); val rc = r.pick(2.5, 3.0)
            val dt = -t * p * 1e-6 * e * 1e5 / (rc * 1e6)
            q(Topic.CALORIC, 2, "Elektrokalorik (indirekt): T = ${n(t)} K, (∂P/∂T)_E = ${n(p)} μC m⁻²K⁻¹, ΔE = ${n(e)} kV/cm, ρc = ${n(rc)} MJ m⁻³K⁻¹. ΔT_ad in K?",
                dt, "K", "ΔT = −(T/ρc)(∂P/∂T)ΔE = −${n(t)}/(${n(rc)}·10⁶) · ${n(p)}·10⁻⁶ · ${n(e)}·10⁵ = ${n(dt)} K.", "ca_electro")
        },
        QuestionGenerator { r ->
            val l = r.pick(10.0, 12.0, 15.0, 20.0); val c = r.pick(0.45, 0.5)
            val dt = l / c
            q(Topic.CALORIC, 1, "Eine Formgedächtnislegierung hat die Umwandlungswärme L = ${n(l)} J/g und c = ${n(c)} J/(g K). Maximale adiabatische Erwärmung bei vollständiger Umwandlung in K?",
                dt, "K", "ΔT_ad ≈ L/c = ${n(l)}/${n(c)} = ${n(dt)} K.", "ca_elasto")
        },
        QuestionGenerator { r ->
            val tk = r.pick(-20.0, 0.0, 5.0); val tw = r.pick(25.0, 35.0, 45.0)
            val cop = (tk + 273.15) / (tw - tk)
            q(Topic.CALORIC, 1, "Maximale Leistungszahl (COP) einer Kältemaschine zwischen ${n(tk)} °C und ${n(tw)} °C?",
                cop, "", "COP_C = T_k/(T_w − T_k) = ${n(tk + 273.15)}/${n(tw - tk)} = ${n(cop)}.", "ca_multi")
        },
        QuestionGenerator { r ->
            val slope = r.pick(5.0, 6.5, 7.0); val eps = r.pick(0.04, 0.05, 0.06); val rho = 6450.0
            val ds = slope * 1e6 * eps / rho
            q(Topic.CALORIC, 3, "NiTi: dσ_tr/dT = ${n(slope)} MPa/K, Umwandlungsdehnung ε_tr = ${n(eps)}, ρ = 6450 kg/m³. Betrag der Umwandlungsentropie in J/(kg K)?",
                ds, "J/(kg K)", "Clausius-Clapeyron: |Δs| = (dσ/dT) ε_tr/ρ = ${n(slope * 1e6)} · ${n(eps)} / 6450 = ${n(ds)} J/(kg K).", "ca_elasto")
        },
        QuestionGenerator { r ->
            val s = r.pick(100.0, 200.0, 300.0)
            val dt = -1.2e-5 * 300 * s * 1e6 / (7850 * 460) * 1e3
            q(Topic.CALORIC, 3, "Thermoelastischer Effekt: Stahl (α = 1,2·10⁻⁵ K⁻¹, ρ = 7850 kg/m³, c = 460 J/(kg K)) wird bei 300 K adiabatisch um ${n(s)} MPa auf Zug belastet. ΔT in mK?",
                dt, "mK", "ΔT = −αTΔσ/(ρc) = −1,2·10⁻⁵ · 300 · ${n(s * 1e6)} / (7850 · 460) = ${n(dt)} mK — Zug kühlt Metalle.", "ca_elasto")
        }
    )

    private val qft = listOf(
        QuestionGenerator { r ->
            val e = r.pick(0.1, 0.2, 1.0, 10.0, 91.2)
            val l = Phys.hbarcMeVfm / (e * 1e3)
            q(Topic.QFT, 1, "Welcher Länge (in fm) entspricht in natürlichen Einheiten die Energie ${n(e)} GeV, also ħc/E?",
                l, "fm", "L = ħc/E = 197,327 MeV fm / ${n(e * 1e3)} MeV = ${n(l)} fm.", "qft_relativity")
        },
        QuestionGenerator { r ->
            val m = r.pick(139.57, 497.6, 775.3, 80377.0, 91187.6)
            val l = Phys.hbarcMeVfm / m
            q(Topic.QFT, 1, "Reichweite (fm) einer Kraft, die durch ein Teilchen der Masse ${n(m)} MeV vermittelt wird (Yukawa)?",
                l, "fm", "R = ħ/(mc) = ħc/(mc²) = 197,327/${n(m)} fm = ${n(l)} fm.", "qft_scalar")
        },
        QuestionGenerator { r ->
            val e = r.pick(1.0, 10.0, 100.0)
            val g = e * 1e3 / Phys.mpMeV
            q(Topic.QFT, 1, "Ein Proton hat die Gesamtenergie ${n(e)} GeV. Lorentzfaktor γ?",
                g, "", "γ = E/(mc²) = ${n(e * 1e3)}/938,27 = ${n(g)}.", "qft_relativity")
        },
        QuestionGenerator { r ->
            val rs = r.pick(10.0, 30.0, 58.0, 91.2)
            val s = 4 * PI * Phys.alpha.pow(2) / (3 * rs * rs) * Phys.hbarc2GeV2mb * 1e6
            q(Topic.QFT, 2, "Wirkungsquerschnitt σ(e⁺e⁻ → μ⁺μ⁻) in nb bei √s = ${n(rs)} GeV in niedrigster Ordnung (nur Photon)?",
                s, "nb", "σ = 4πα²/(3s) = 86,8 nb/(s/GeV²) = 86,8/${n(rs * rs)} nb = ${n(s)} nb.", "qft_feynman", tol = 0.03)
        },
        QuestionGenerator { r ->
            val e = r.pick(10.0, 100.0, 450.0, 1000.0)
            val rs = sqrt(2 * (Phys.mpMeV / 1e3).pow(2) + 2 * e * Phys.mpMeV / 1e3)
            q(Topic.QFT, 2, "Ein Protonenstrahl von ${n(e)} GeV trifft auf ruhende Protonen. Schwerpunktsenergie √s in GeV?",
                rs, "GeV", "√s = √(2m² + 2Em) = √(2·0,938² + 2·${n(e)}·0,938) = ${n(rs)} GeV — ein Collider mit 2 × ${n(e)} GeV hätte ${n(2 * e)} GeV.", "qft_relativity")
        },
        QuestionGenerator { r ->
            val e = r.pick(1.0, 3.0, 10.0)
            val g = e * 1e3 / Phys.mmuMeV
            val l = sqrt(g * g - 1) * Phys.c * 2.1969811e-6 / 1e3
            q(Topic.QFT, 2, "Mittlere Zerfallslänge (km) eines Myons mit ${n(e)} GeV (τ = 2,197 μs, m = 105,66 MeV)?",
                l, "km", "L = γβcτ = √(γ² − 1) · cτ, γ = ${n(g)}, cτ = 658,6 m → L = ${n(l)} km.", "qft_sm")
        },
        QuestionGenerator { r ->
            val mm = r.pick(91.1876, 125.25)
            val m1 = r.pick(0.10566, 0.000511)
            val p = QftPhysics.twoBodyMomentum(mm, m1, m1)
            q(Topic.QFT, 3, "Impuls (GeV) jedes Tochterteilchens im Zerfall eines ruhenden Teilchens der Masse ${n(mm)} GeV in zwei Teilchen der Masse ${n(m1)} GeV?",
                p, "GeV", "p* = √(M²/4 − m²) = ${n(p)} GeV (fast M/2, da m ≪ M).", "qft_relativity", tol = 0.005)
        }
    )

    private val math = listOf(
        QuestionGenerator { r ->
            val a = r.int(1, 5); val b = r.int(-4, 4); val c = r.int(-5, 5); val x = r.int(-3, 3)
            val d = 3.0 * a * x * x + 2.0 * b * x + c
            q(Topic.MATH, 1, "Ableitung von f(x) = ${a}x³ + ${b}x² + ${c}x an der Stelle x = $x?",
                d, "", "f'(x) = ${3 * a}x² + ${2 * b}x + $c → f'($x) = ${n(d)}.", "ma_analysis")
        },
        QuestionGenerator { r ->
            val a = r.int(1, 6); val b = r.int(0, 5); val c = r.int(1, 4)
            val v = a * c.toDouble().pow(3) / 3 + b * c
            q(Topic.MATH, 1, "∫₀^$c (${a}x² + $b) dx = ?",
                v, "", "Stammfunktion ${a}x³/3 + ${b}x; Einsetzen: ${n(v)}.", "ma_analysis")
        },
        QuestionGenerator { r ->
            val m = List(9) { r.int(-4, 5).toDouble() }
            val det = Matrix.of(m.subList(0, 3).toDoubleArray(), m.subList(3, 6).toDoubleArray(), m.subList(6, 9).toDoubleArray()).determinant()
            q(Topic.MATH, 2, "Determinante von A = (${m.subList(0, 3).joinToString(" ") { n(it) }}; ${m.subList(3, 6).joinToString(" ") { n(it) }}; ${m.subList(6, 9).joinToString(" ") { n(it) }})?",
                Math.rint(det), "", "Regel von Sarrus oder Entwicklung nach der ersten Zeile ergibt det A = ${n(Math.rint(det))}.", "ma_linalg", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val a = r.int(1, 6).toDouble(); val d = r.int(1, 6).toDouble(); val b = r.int(1, 4).toDouble()
            val l = (a + d) / 2 + sqrt(((a - d) / 2).pow(2) + b * b)
            q(Topic.MATH, 2, "Größter Eigenwert der symmetrischen Matrix (${n(a)} ${n(b)}; ${n(b)} ${n(d)})?",
                l, "", "λ = (a + d)/2 ± √(((a − d)/2)² + b²) → λ_max = ${n(l)}.", "ma_linalg")
        },
        QuestionGenerator { r ->
            val nn = r.int(5, 12); val k = r.int(1, nn - 1); val p = r.pick(0.2, 0.3, 0.5)
            val v = Stats.binomial(nn, k, p)
            q(Topic.MATH, 2, "Wahrscheinlichkeit für genau $k Erfolge in $nn unabhängigen Versuchen mit p = ${n(p)}?",
                v, "", "P = C($nn,$k) p^$k (1−p)^${nn - k} = ${n(v)}.", "ma_prob")
        },
        QuestionGenerator { r ->
            val a = r.int(-4, 4).toDouble(); val b = r.int(-4, 4).toDouble(); val c = r.int(-4, 4).toDouble(); val d = r.int(-4, 4).toDouble()
            val m = Complex(a, b) * Complex(c, d)
            q(Topic.MATH, 1, "Betrag des Produkts (${n(a)} + ${n(b)}i)(${n(c)} + ${n(d)}i)?",
                m.abs, "", "|z₁z₂| = |z₁||z₂| = ${n(Complex(a, b).abs)} · ${n(Complex(c, d).abs)} = ${n(m.abs)}.", "ma_complex")
        },
        QuestionGenerator { r ->
            val x1 = r.int(-5, 5).toDouble(); val x2 = x1 + r.int(1, 6)
            val b = -(x1 + x2); val c = x1 * x2
            q(Topic.MATH, 1, "Größere Lösung von x² ${if (b >= 0) "+" else "−"} ${n(abs(b))}x ${if (c >= 0) "+" else "−"} ${n(abs(c))} = 0?",
                x2, "", "p-q-Formel: x = −p/2 ± √((p/2)² − q) → x = ${n(x1)} oder ${n(x2)}. Vieta: Summe ${n(x1 + x2)}, Produkt ${n(c)}.", "ma_analysis", tol = 1e-6)
        },
        QuestionGenerator { r ->
            val s = r.pick(2.0, 5.0, 10.0); val nn = r.pick(4.0, 16.0, 25.0, 100.0)
            val se = s / sqrt(nn)
            q(Topic.MATH, 1, "Messreihe mit Standardabweichung σ = ${n(s)} und n = ${n(nn)} Messungen: Standardfehler des Mittelwerts?",
                se, "", "σ_x̄ = σ/√n = ${n(s)}/${n(sqrt(nn))} = ${n(se)}.", "ma_prob")
        }
    )

    val all: Map<Topic, List<QuestionGenerator>> = mapOf(
        Topic.THERMO to thermo, Topic.ELECTRO to electro, Topic.QUANTUM to quantum, Topic.SEMICONDUCTOR to semiconductor,
        Topic.CALORIC to caloric, Topic.QFT to qft, Topic.MATH to math
    )
}

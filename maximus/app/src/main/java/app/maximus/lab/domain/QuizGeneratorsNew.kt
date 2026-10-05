package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/** Calculation tasks for polymer physics, electrical engineering and AI engineering. */
internal object QuizGeneratorsNew {
    private fun Random.pick(vararg xs: Double) = xs[nextInt(xs.size)]
    private fun Random.int(lo: Int, hi: Int) = lo + nextInt(hi - lo + 1)
    private fun n(v: Double, d: Int = 4) = Fmt.num(v, d)
    private var counter = 0

    private fun q(topic: Topic, diff: Int, prompt: String, answer: Double, unit: String, solution: String, chapter: String, tol: Double = 0.02) =
        Question("${topic.short}-n${++counter}", topic, diff, prompt, solution, answer = answer, unit = unit, tolerance = tol, chapterKey = chapter)

    private val P = Topic.POLYMER
    private val E = Topic.ELECTRICAL
    private val A = Topic.AI

    val polymer = listOf(
        QuestionGenerator { r ->
            val nK = r.pick(100.0, 400.0, 1000.0, 2500.0, 10000.0); val b = r.pick(0.5, 1.0, 1.5, 2.0)
            val rr = b * sqrt(nK)
            q(P, 1, "Ideale Kette aus N = ${n(nK)} Kuhn-Segmenten der Länge b = ${n(b)} nm. Wurzel aus dem mittleren End-zu-End-Abstandsquadrat in nm?",
                rr, "nm", "Zufallsweg: ⟨R²⟩ = Nb² ⇒ √⟨R²⟩ = b√N = ${n(b)}·√${n(nK)} = ${n(rr)} nm (ausgestreckt wären es ${n(nK * b)} nm).", "po_chain")
        },
        QuestionGenerator { r ->
            val rr = r.pick(10.0, 20.0, 30.0, 60.0)
            val rg = rr / sqrt(6.0)
            q(P, 1, "Eine ideale Kette hat einen End-zu-End-Abstand √⟨R²⟩ = ${n(rr)} nm. Gyrationsradius in nm?",
                rg, "nm", "Für ideale Ketten gilt R_g² = ⟨R²⟩/6 ⇒ R_g = ${n(rr)}/√6 = ${n(rg)} nm.", "po_chain")
        },
        QuestionGenerator { r ->
            val f = r.pick(2.0, 4.0, 10.0)
            val ratio = f.pow(0.588)
            q(P, 2, "Gutes Lösungsmittel (ν ≈ 0,588): Um welchen Faktor wächst die Knäuelgröße, wenn die Molmasse um den Faktor ${n(f)} steigt?",
                ratio, "", "R ∝ N^ν ⇒ ${n(f)}^0,588 = ${n(ratio)}. Im Theta-Lösungsmittel (ν = ½) wären es ${n(sqrt(f))}.", "po_real")
        },
        QuestionGenerator { r ->
            val nn = r.pick(100.0, 1000.0, 10000.0, 400.0)
            val chi = 0.5 * (1 + 1 / sqrt(nn)).pow(2)
            q(P, 2, "Flory-Huggins: kritischer Wechselwirkungsparameter χ_c für eine Polymerlösung mit N = ${n(nn)}?",
                chi, "", "χ_c = ½(1 + 1/√N)² = ½(1 + ${n(1 / sqrt(nn))})² = ${n(chi)}. Für N → ∞ geht χ_c → ½ (Theta-Punkt).", "po_solutions")
        },
        QuestionGenerator { r ->
            val rho = r.pick(900.0, 1000.0, 1100.0); val mx = r.pick(2.0, 5.0, 10.0, 20.0); val t = r.pick(293.0, 300.0, 350.0)
            val g = rho * Phys.R * t / mx / 1e6
            q(P, 2, "Gummi mit Dichte ${n(rho)} kg/m³ und Molmasse zwischen Vernetzungen M_x = ${n(mx)} kg/mol bei ${n(t)} K. Schermodul (affines Netz) in MPa?",
                g, "MPa", "G = ρRT/M_x = ${n(rho)}·8,314·${n(t)}/${n(mx)} Pa = ${n(g)} MPa. Steigt mit T: Entropieelastizität.", "po_rubber")
        },
        QuestionGenerator { r ->
            val g = r.pick(0.5, 1.0, 2.0); val l = r.pick(1.5, 2.0, 3.0)
            val s = g * (l - 1 / (l * l))
            q(P, 2, "Gummi mit G = ${n(g)} MPa wird uniaxial auf λ = ${n(l)} gedehnt. Nominalspannung in MPa (affines Netz)?",
                s, "MPa", "σ = G(λ − 1/λ²) = ${n(g)}·(${n(l)} − ${n(1 / (l * l))}) = ${n(s)} MPa.", "po_rubber")
        },
        QuestionGenerator { r ->
            val dt = r.pick(10.0, 20.0, 30.0, 50.0)
            val loga = -17.44 * dt / (51.6 + dt)
            q(P, 3, "WLF mit universellen Konstanten (T_r = T_g): log₁₀ a_T bei T = T_g + ${n(dt)} K?",
                loga, "", "log a_T = −C₁(T − T_g)/(C₂ + T − T_g) = −17,44·${n(dt)}/(51,6 + ${n(dt)}) = ${n(loga)}. Relaxationen sind also um den Faktor 10^${n(-loga, 3)} schneller als bei T_g.", "po_dynamics")
        },
        QuestionGenerator { r ->
            val f = r.pick(2.0, 3.0, 5.0)
            val ratio = f.pow(3.4)
            q(P, 2, "Verschlaufte Schmelze (M ≫ M_c): Um welchen Faktor steigt die Viskosität, wenn M um den Faktor ${n(f)} wächst?",
                ratio, "", "η ∝ M^{3,4} ⇒ ${n(f)}^{3,4} = ${n(ratio)}. Reine Reptation sagt M³ voraus (${n(f.pow(3.0))}).", "po_dynamics")
        },
        QuestionGenerator { r ->
            val t1 = r.pick(373.0, 378.0, 353.0); val t2 = r.pick(200.0, 220.0, 250.0); val w1 = r.pick(0.3, 0.5, 0.7)
            val tg = 1 / (w1 / t1 + (1 - w1) / t2)
            q(P, 2, "Verträgliche Mischung: T_g1 = ${n(t1)} K (Massenanteil ${n(w1)}), T_g2 = ${n(t2)} K. T_g nach Fox in K?",
                tg, "K", "1/T_g = w₁/T_g1 + w₂/T_g2 ⇒ T_g = ${n(tg)} K.", "po_glass")
        },
        QuestionGenerator { r ->
            val k = r.pick(0.001, 0.01, 0.05); val nn = r.pick(2.0, 3.0, 4.0); val t = r.pick(5.0, 10.0, 20.0)
            val x = 1 - exp(-k * t.pow(nn))
            q(P, 2, "Avrami-Kinetik mit k = ${n(k)} minⁿ und n = ${n(nn)}: kristallisierter Anteil nach t = ${n(t)} min?",
                x, "", "X = 1 − exp(−k tⁿ) = 1 − exp(−${n(k * t.pow(nn))}) = ${n(x)}.", "po_cryst")
        },
        QuestionGenerator { r ->
            val er = r.pick(2.2, 10.0, 50.0); val e = r.pick(100.0, 200.0, 400.0)
            val u = 0.5 * Phys.eps0 * er * (e * 1e6).pow(2) / 1e6
            q(P, 2, "Linearer dielektrischer Polymerfilm mit ε_r = ${n(er)} bei E = ${n(e)} MV/m. Gespeicherte Energiedichte in J/cm³?",
                u, "J/cm³", "u = ½ε₀ε_rE² = ½·8,854·10⁻¹²·${n(er)}·(${n(e)}·10⁶)² J/m³ = ${n(u)} J/cm³.", "po_electroactive")
        }
    )

    val electrical = listOf(
        QuestionGenerator { r ->
            val u = r.pick(5.0, 12.0, 24.0); val r1 = r.pick(1.0, 2.2, 4.7, 10.0); val r2 = r.pick(1.0, 3.3, 10.0)
            val u2 = u * r2 / (r1 + r2)
            q(E, 1, "Spannungsteiler: U = ${n(u)} V, R₁ = ${n(r1)} kΩ, R₂ = ${n(r2)} kΩ. Spannung an R₂ (unbelastet) in V?",
                u2, "V", "U₂ = U·R₂/(R₁ + R₂) = ${n(u)}·${n(r2)}/${n(r1 + r2)} = ${n(u2)} V.", "ee_dc")
        },
        QuestionGenerator { r ->
            val u = r.pick(5.0, 9.0, 12.0); val ri = r.pick(2.0, 4.0, 8.0, 50.0)
            val p = u * u / (4 * ri)
            q(E, 1, "Quelle mit U₀ = ${n(u)} V und Innenwiderstand ${n(ri)} Ω. Maximal an eine Last abgebbare Leistung in W?",
                p, "W", "Leistungsanpassung R_L = R_i: P_max = U₀²/(4R_i) = ${n(u * u)}/${n(4 * ri)} = ${n(p)} W (Wirkungsgrad dann 50 %).", "ee_dc")
        },
        QuestionGenerator { r ->
            val rr = r.pick(1.0, 10.0, 4.7); val c = r.pick(10.0, 100.0, 22.0)
            val f = 1 / (2 * PI * rr * 1e3 * c * 1e-9)
            q(E, 1, "RC-Tiefpass mit R = ${n(rr)} kΩ und C = ${n(c)} nF. Grenzfrequenz (−3 dB) in Hz?",
                f, "Hz", "f_g = 1/(2πRC) = 1/(2π·${n(rr * 1e3)} Ω·${n(c * 1e-9)} F) = ${n(f)} Hz.", "ee_ac")
        },
        QuestionGenerator { r ->
            val l = r.pick(1.0, 10.0, 100.0); val c = r.pick(1.0, 10.0, 100.0)
            val f = 1 / (2 * PI * sqrt(l * 1e-6 * c * 1e-9))
            q(E, 2, "Schwingkreis mit L = ${n(l)} μH und C = ${n(c)} nF. Resonanzfrequenz in kHz?",
                f / 1e3, "kHz", "f₀ = 1/(2π√(LC)) = ${n(f)} Hz = ${n(f / 1e3)} kHz.", "ee_ac")
        },
        QuestionGenerator { r ->
            val rf = r.pick(10.0, 47.0, 100.0); val r1 = r.pick(1.0, 4.7, 10.0); val inv = r.nextBoolean()
            val g = if (inv) -rf / r1 else 1 + rf / r1
            q(E, 1, "${if (inv) "Invertierender" else "Nichtinvertierender"} OPV-Verstärker mit R_f = ${n(rf)} kΩ, R₁ = ${n(r1)} kΩ. Spannungsverstärkung?",
                g, "", (if (inv) "A = −R_f/R₁" else "A = 1 + R_f/R₁") + " = ${n(g)} (virtueller Kurzschluss, kein Eingangsstrom).", "ee_opamp")
        },
        QuestionGenerator { r ->
            val gbw = r.pick(1.0, 10.0, 100.0); val a = r.pick(10.0, 20.0, 100.0)
            val bw = gbw * 1e3 / a
            q(E, 2, "OPV mit Verstärkungs-Bandbreite-Produkt ${n(gbw)} MHz als Verstärker mit A = ${n(a)}. Bandbreite in kHz?",
                bw, "kHz", "f_−3dB = GBW/A = ${n(gbw)} MHz/${n(a)} = ${n(bw)} kHz.", "ee_opamp")
        },
        QuestionGenerator { r ->
            val bits = r.int(8, 24)
            val snr = 6.02 * bits + 1.76
            q(E, 1, "Idealer ${bits}-Bit-ADC mit sinusförmigem Vollaussteuerungssignal: Signal-Rausch-Verhältnis in dB?",
                snr, "dB", "SNR = 6,02 N + 1,76 dB = ${n(snr)} dB (Quantisierungsrauschen LSB²/12).", "ee_analog")
        },
        QuestionGenerator { r ->
            val rr = r.pick(1.0, 10.0, 100.0); val b = r.pick(10.0, 20.0, 1000.0)
            val u = sqrt(4 * Phys.kB * 300 * rr * 1e3 * b * 1e3) * 1e6
            q(E, 2, "Thermisches Rauschen eines ${n(rr)}-kΩ-Widerstands bei 300 K in ${n(b)} kHz Bandbreite: Effektivwert in μV?",
                u, "μV", "u = √(4k_BTRΔf) = √(4·1,381·10⁻²³·300·${n(rr * 1e3)}·${n(b * 1e3)}) V = ${n(u)} μV.", "ee_analog")
        },
        QuestionGenerator { r ->
            val k = r.pick(100.0, 200.0, 400.0); val wl = r.pick(2.0, 5.0, 10.0); val ov = r.pick(0.2, 0.3, 0.5)
            val id = 0.5 * k * wl * ov * ov
            q(E, 2, "MOSFET in Sättigung (Langkanal): μC_ox = ${n(k)} μA/V², W/L = ${n(wl)}, U_GS − U_th = ${n(ov)} V. Drainstrom in μA?",
                id, "μA", "I_D = ½ μC_ox (W/L)(U_GS − U_th)² = ½·${n(k)}·${n(wl)}·${n(ov * ov)} = ${n(id)} μA; g_m = 2I_D/U_ov = ${n(2 * id / ov)} μS.", "ee_devices")
        },
        QuestionGenerator { r ->
            val a = r.pick(0.1, 0.15, 0.2); val c = r.pick(1.0, 5.0, 10.0); val v = r.pick(0.7, 0.8, 1.0); val f = r.pick(1.0, 2.0, 3.0)
            val p = a * c * 1e-9 * v * v * f * 1e9
            q(E, 2, "Digitalschaltung: Aktivität α = ${n(a)}, geschaltete Kapazität ${n(c)} nF, U = ${n(v)} V, f = ${n(f)} GHz. Dynamische Leistung in W?",
                p, "W", "P = αCU²f = ${n(a)}·${n(c)}·10⁻⁹·${n(v * v)}·${n(f)}·10⁹ = ${n(p)} W. Halbe Spannung ⇒ Viertel der Leistung.", "ee_digital")
        },
        QuestionGenerator { r ->
            val tcq = r.pick(30.0, 50.0); val tl = r.pick(200.0, 300.0, 450.0); val ts = r.pick(20.0, 40.0)
            val f = 1e3 / (tcq + tl + ts)
            q(E, 2, "Registerpfad: t_cq = ${n(tcq)} ps, Logik ${n(tl)} ps, Setup ${n(ts)} ps, kein Skew. Maximale Taktfrequenz in GHz?",
                f, "GHz", "T_min = t_cq + t_logic + t_setup = ${n(tcq + tl + ts)} ps ⇒ f_max = ${n(f)} GHz.", "ee_digital")
        },
        QuestionGenerator { r ->
            val k1 = r.pick(0.3, 0.4); val euv = r.nextBoolean(); val lam = if (euv) 13.5 else 193.0; val na = if (euv) r.pick(0.33, 0.55) else 1.35
            val cd = k1 * lam / na
            q(E, 2, "Lithografie: λ = ${n(lam)} nm, NA = ${n(na)}, k₁ = ${n(k1)}. Kleinste Strukturbreite in nm (Rayleigh)?",
                cd, "nm", "CD = k₁λ/NA = ${n(k1)}·${n(lam)}/${n(na)} = ${n(cd)} nm.", "ee_fab")
        },
        QuestionGenerator { r ->
            val d0 = r.pick(0.05, 0.1, 0.2); val a = r.pick(1.0, 2.0, 6.0, 8.0)
            val y = exp(-d0 * a) * 100
            q(E, 2, "Defektdichte D₀ = ${n(d0)} cm⁻², Chipfläche ${n(a)} cm². Ausbeute nach Poisson in %?",
                y, "%", "Y = e^{−D₀A} = e^{−${n(d0 * a)}} = ${n(y)} %. Große Chips sind deshalb teuer — Chiplets helfen.", "ee_fab")
        },
        QuestionGenerator { r ->
            val vin = r.pick(12.0, 24.0, 48.0); val d = r.pick(0.25, 0.4, 0.5)
            val v = vin * d
            q(E, 1, "Idealer Tiefsetzsteller (Buck) mit U_e = ${n(vin)} V und Tastverhältnis D = ${n(d)}. Ausgangsspannung in V?",
                v, "V", "U_a = D·U_e = ${n(v)} V.", "ee_analog")
        }
    )

    val ai = listOf(
        QuestionGenerator { r ->
            val nin = r.pick(64.0, 128.0, 512.0, 784.0); val nout = r.pick(10.0, 64.0, 256.0)
            val p = nin * nout + nout
            q(A, 1, "Vollverbundene Schicht mit ${n(nin)} Eingängen und ${n(nout)} Ausgängen (mit Bias). Anzahl Parameter?",
                p, "", "Gewichte n_in·n_out = ${n(nin * nout, 8)} plus ${n(nout)} Bias = ${n(p, 8)}.", "ai_nn", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val z1 = r.int(0, 4).toDouble(); val z2 = r.int(-2, 2).toDouble(); val z3 = r.int(-2, 2).toDouble()
            val p = exp(z1) / (exp(z1) + exp(z2) + exp(z3))
            q(A, 1, "Softmax über die Logits (${n(z1)}, ${n(z2)}, ${n(z3)}): Wahrscheinlichkeit der ersten Klasse?",
                p, "", "p₁ = e^{z₁}/Σe^{zⱼ} = ${n(exp(z1))}/${n(exp(z1) + exp(z2) + exp(z3))} = ${n(p)}.", "ai_linear")
        },
        QuestionGenerator { r ->
            val p = r.pick(0.9, 0.5, 0.2, 0.05, 0.99)
            val l = -ln(p)
            q(A, 1, "Das Modell gibt der richtigen Klasse die Wahrscheinlichkeit ${n(p)}. Kreuzentropie-Verlust (natürlicher Log)?",
                l, "", "ℓ = −ln p = ${n(l)}. Sichere Fehler (p → 0) werden sehr teuer.", "ai_linear")
        },
        QuestionGenerator { r ->
            val a = r.pick(1.0, 2.0); val c = r.pick(3.0, 1.0); val w0 = r.pick(0.0, 5.0); val eta = r.pick(0.1, 0.2, 0.05)
            val w1 = w0 - eta * 2 * a * (w0 - c)
            q(A, 1, "Verlust L(w) = ${n(a)}(w − ${n(c)})². Ein Gradientenschritt mit η = ${n(eta)} ab w₀ = ${n(w0)}: neues w?",
                w1, "", "∇L = 2·${n(a)}(w − ${n(c)}) = ${n(2 * a * (w0 - c))}; w₁ = w₀ − η∇L = ${n(w1)}. Stabil, solange η < 2/λ_max = ${n(1 / a)}.", "ai_linear", tol = 1e-6)
        },
        QuestionGenerator { r ->
            val tp = r.int(40, 90).toDouble(); val fp = r.int(5, 30).toDouble(); val fn = r.int(5, 30).toDouble()
            val p = tp / (tp + fp); val rc = tp / (tp + fn); val f1 = 2 * p * rc / (p + rc)
            q(A, 1, "Klassifikator: TP = ${n(tp)}, FP = ${n(fp)}, FN = ${n(fn)}. F1-Score?",
                f1, "", "Precision = ${n(p)}, Recall = ${n(rc)}, F1 = 2PR/(P + R) = ${n(f1)}.", "ai_intro")
        },
        QuestionGenerator { r ->
            val nb = r.pick(1.0, 7.0, 70.0); val dt = r.pick(1.0, 2.0, 15.0)
            val c = 6 * nb * 1e9 * dt * 1e12 / 1e21
            q(A, 2, "Training eines Modells mit ${n(nb)} Mrd. Parametern auf ${n(dt)} Billionen Token. Compute in Einheiten von 10²¹ FLOP?",
                c, "·10²¹ FLOP", "C ≈ 6ND = 6·${n(nb)}·10⁹·${n(dt)}·10¹² = ${n(c)}·10²¹ FLOP.", "ai_llm")
        },
        QuestionGenerator { r ->
            val nb = r.pick(1.0, 3.0, 8.0, 70.0)
            val d = 20 * nb
            q(A, 1, "Chinchilla-Faustregel: Wie viele Trainingstoken (in Mrd.) sind für ein compute-optimales Modell mit ${n(nb)} Mrd. Parametern ideal?",
                d, "Mrd. Token", "D ≈ 20N = ${n(d)} Mrd. Token. Für Inferenz-optimierte kleine Modelle trainiert man oft deutlich länger.", "ai_llm")
        },
        QuestionGenerator { r ->
            val nb = r.pick(1.0, 2.0, 4.0, 8.0); val bits = r.pick(4.0, 8.0, 16.0)
            val gb = nb * bits / 8
            q(A, 1, "Speicher für die Gewichte eines Modells mit ${n(nb)} Mrd. Parametern bei ${n(bits)} Bit pro Gewicht, in GB?",
                gb, "GB", "M = N·Bits/8 = ${n(nb)}·10⁹·${n(bits)}/8 Byte = ${n(gb)} GB (plus KV-Cache und Laufzeit).", "ai_inference")
        },
        QuestionGenerator { r ->
            val layers = r.pick(16.0, 26.0, 32.0); val kv = r.pick(1.0, 4.0, 8.0); val dh = r.pick(64.0, 128.0, 256.0); val ctx = r.pick(2048.0, 4096.0, 8192.0)
            val mb = 2 * layers * kv * dh * ctx * 2 / 1024 / 1024
            q(A, 3, "KV-Cache in fp16: ${n(layers)} Schichten, ${n(kv)} KV-Köpfe à ${n(dh)} Dimensionen, Kontext ${n(ctx)} Token. Speicher in MiB?",
                mb, "MiB", "M_KV = 2·L·n_kv·d_head·n_ctx·2 Byte = ${n(mb)} MiB. Grouped-Query-Attention (wenige KV-Köpfe) spart hier.", "ai_inference")
        },
        QuestionGenerator { r ->
            val bw = r.pick(30.0, 50.0, 60.0, 100.0); val size = r.pick(1.0, 2.0, 4.0)
            val tps = bw / size
            q(A, 2, "Handy mit ${n(bw)} GB/s Speicherbandbreite, Modellgewichte ${n(size)} GB. Obere Grenze der Decode-Geschwindigkeit in Token/s?",
                tps, "Token/s", "Jedes Token liest alle Gewichte: Token/s ≲ Bandbreite/Größe = ${n(tps)}. Real oft 50–70 % davon.", "ai_inference")
        },
        QuestionGenerator { r ->
            val nin = r.pick(28.0, 32.0, 224.0); val k = r.pick(3.0, 5.0); val p = r.pick(0.0, 1.0, 2.0); val s = r.pick(1.0, 2.0)
            val out = kotlin.math.floor((nin + 2 * p - k) / s) + 1
            q(A, 1, "Faltung: Eingabe ${n(nin)}×${n(nin)}, Kern ${n(k)}×${n(k)}, Padding ${n(p)}, Stride ${n(s)}. Seitenlänge der Ausgabe?",
                out, "", "n_out = ⌊(n_in + 2p − k)/s⌋ + 1 = ${n(out)}.", "ai_arch", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val d = r.pick(4096.0, 2048.0, 1024.0); val k = d; val rank = r.pick(8.0, 16.0, 64.0)
            val p = rank * (d + k)
            q(A, 2, "LoRA auf einer ${n(d)}×${n(k)}-Gewichtsmatrix mit Rang r = ${n(rank)}: Zahl der trainierbaren Parameter?",
                p, "", "ΔW = BA mit B ∈ ℝ^{d×r}, A ∈ ℝ^{r×k}: r(d + k) = ${n(p, 8)} statt ${n(d * k, 8)} (${n(100 * p / (d * k), 3)} %).", "ai_llm", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val layers = r.pick(12.0, 24.0, 32.0); val d = r.pick(768.0, 2048.0, 4096.0)
            val pb = 12 * layers * d * d / 1e9
            q(A, 2, "Transformer mit ${n(layers)} Blöcken und Breite d = ${n(d)}: grobe Parameterzahl ohne Embeddings (12·L·d²) in Mrd.?",
                pb, "Mrd.", "≈ 12Ld² = 12·${n(layers)}·${n(d)}² = ${n(pb)} Mrd. (4d² Attention + 8d² MLP pro Block).", "ai_transformer")
        },
        QuestionGenerator { r ->
            val t = r.pick(0.5, 2.0); val z1 = 2.0; val z2 = 0.0
            val p = exp(z1 / t) / (exp(z1 / t) + exp(z2 / t))
            q(A, 2, "Zwei Kandidaten-Token mit Logits 2 und 0. Wahrscheinlichkeit des ersten bei Temperatur T = ${n(t)}?",
                p, "", "p = e^{2/T}/(e^{2/T} + 1) = ${n(p)} (bei T = 1 wären es ${n(exp(2.0) / (exp(2.0) + 1))}). T < 1 schärft, T > 1 glättet.", "ai_inference")
        }
    )
}

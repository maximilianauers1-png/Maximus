package app.maximus.ai.domain

import app.maximus.lab.domain.Fmt
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/** Calculation tasks on network sizes, attention and LLM inference (moved from the physics lab). */
internal object QuizAiSizes {
    private fun Random.pick(vararg xs: Double) = xs[nextInt(xs.size)]
    private fun Random.int(lo: Int, hi: Int) = lo + nextInt(hi - lo + 1)
    private fun n(v: Double, d: Int = 4) = Fmt.num(v, d)
    private var counter = 0

    private fun q(diff: Int, prompt: String, answer: Double, unit: String, solution: String, chapter: String, tol: Double = 0.02) =
        NumericTask("ai-s${++counter}", chapter, diff, prompt, answer, unit, solution)

    val all: List<TaskGenerator> = listOf(
        TaskGenerator { r ->
            val nin = r.pick(64.0, 128.0, 512.0, 784.0); val nout = r.pick(10.0, 64.0, 256.0)
            val p = nin * nout + nout
            q(1, "Vollverbundene Schicht mit ${n(nin)} Eingängen und ${n(nout)} Ausgängen (mit Bias). Anzahl Parameter?",
                p, "", "Gewichte n_in·n_out = ${n(nin * nout, 8)} plus ${n(nout)} Bias = ${n(p, 8)}.", "ai_nn", tol = 1e-9)
        },
        TaskGenerator { r ->
            val z1 = r.int(0, 4).toDouble(); val z2 = r.int(-2, 2).toDouble(); val z3 = r.int(-2, 2).toDouble()
            val p = exp(z1) / (exp(z1) + exp(z2) + exp(z3))
            q(1, "Softmax über die Logits (${n(z1)}, ${n(z2)}, ${n(z3)}): Wahrscheinlichkeit der ersten Klasse?",
                p, "", "p₁ = e^{z₁}/Σe^{zⱼ} = ${n(exp(z1))}/${n(exp(z1) + exp(z2) + exp(z3))} = ${n(p)}.", "ai_linear")
        },
        TaskGenerator { r ->
            val p = r.pick(0.9, 0.5, 0.2, 0.05, 0.99)
            val l = -ln(p)
            q(1, "Das Modell gibt der richtigen Klasse die Wahrscheinlichkeit ${n(p)}. Kreuzentropie-Verlust (natürlicher Log)?",
                l, "", "ℓ = −ln p = ${n(l)}. Sichere Fehler (p → 0) werden sehr teuer.", "ai_linear")
        },
        TaskGenerator { r ->
            val a = r.pick(1.0, 2.0); val c = r.pick(3.0, 1.0); val w0 = r.pick(0.0, 5.0); val eta = r.pick(0.1, 0.2, 0.05)
            val w1 = w0 - eta * 2 * a * (w0 - c)
            q(1, "Verlust L(w) = ${n(a)}(w − ${n(c)})². Ein Gradientenschritt mit η = ${n(eta)} ab w₀ = ${n(w0)}: neues w?",
                w1, "", "∇L = 2·${n(a)}(w − ${n(c)}) = ${n(2 * a * (w0 - c))}; w₁ = w₀ − η∇L = ${n(w1)}. Stabil, solange η < 2/λ_max = ${n(1 / a)}.", "ai_linear", tol = 1e-6)
        },
        TaskGenerator { r ->
            val tp = r.int(40, 90).toDouble(); val fp = r.int(5, 30).toDouble(); val fn = r.int(5, 30).toDouble()
            val p = tp / (tp + fp); val rc = tp / (tp + fn); val f1 = 2 * p * rc / (p + rc)
            q(1, "Klassifikator: TP = ${n(tp)}, FP = ${n(fp)}, FN = ${n(fn)}. F1-Score?",
                f1, "", "Precision = ${n(p)}, Recall = ${n(rc)}, F1 = 2PR/(P + R) = ${n(f1)}.", "ai_intro")
        },
        TaskGenerator { r ->
            val nb = r.pick(1.0, 7.0, 70.0); val dt = r.pick(1.0, 2.0, 15.0)
            val c = 6 * nb * 1e9 * dt * 1e12 / 1e21
            q(2, "Training eines Modells mit ${n(nb)} Mrd. Parametern auf ${n(dt)} Billionen Token. Compute in Einheiten von 10²¹ FLOP?",
                c, "·10²¹ FLOP", "C ≈ 6ND = 6·${n(nb)}·10⁹·${n(dt)}·10¹² = ${n(c)}·10²¹ FLOP.", "ai_llm")
        },
        TaskGenerator { r ->
            val nb = r.pick(1.0, 3.0, 8.0, 70.0)
            val d = 20 * nb
            q(1, "Chinchilla-Faustregel: Wie viele Trainingstoken (in Mrd.) sind für ein compute-optimales Modell mit ${n(nb)} Mrd. Parametern ideal?",
                d, "Mrd. Token", "D ≈ 20N = ${n(d)} Mrd. Token. Für Inferenz-optimierte kleine Modelle trainiert man oft deutlich länger.", "ai_llm")
        },
        TaskGenerator { r ->
            val nb = r.pick(1.0, 2.0, 4.0, 8.0); val bits = r.pick(4.0, 8.0, 16.0)
            val gb = nb * bits / 8
            q(1, "Speicher für die Gewichte eines Modells mit ${n(nb)} Mrd. Parametern bei ${n(bits)} Bit pro Gewicht, in GB?",
                gb, "GB", "M = N·Bits/8 = ${n(nb)}·10⁹·${n(bits)}/8 Byte = ${n(gb)} GB (plus KV-Cache und Laufzeit).", "ai_inference")
        },
        TaskGenerator { r ->
            val layers = r.pick(16.0, 26.0, 32.0); val kv = r.pick(1.0, 4.0, 8.0); val dh = r.pick(64.0, 128.0, 256.0); val ctx = r.pick(2048.0, 4096.0, 8192.0)
            val mb = 2 * layers * kv * dh * ctx * 2 / 1024 / 1024
            q(3, "KV-Cache in fp16: ${n(layers)} Schichten, ${n(kv)} KV-Köpfe à ${n(dh)} Dimensionen, Kontext ${n(ctx)} Token. Speicher in MiB?",
                mb, "MiB", "M_KV = 2·L·n_kv·d_head·n_ctx·2 Byte = ${n(mb)} MiB. Grouped-Query-Attention (wenige KV-Köpfe) spart hier.", "ai_inference")
        },
        TaskGenerator { r ->
            val bw = r.pick(30.0, 50.0, 60.0, 100.0); val size = r.pick(1.0, 2.0, 4.0)
            val tps = bw / size
            q(2, "Handy mit ${n(bw)} GB/s Speicherbandbreite, Modellgewichte ${n(size)} GB. Obere Grenze der Decode-Geschwindigkeit in Token/s?",
                tps, "Token/s", "Jedes Token liest alle Gewichte: Token/s ≲ Bandbreite/Größe = ${n(tps)}. Real oft 50–70 % davon.", "ai_inference")
        },
        TaskGenerator { r ->
            val nin = r.pick(28.0, 32.0, 224.0); val k = r.pick(3.0, 5.0); val p = r.pick(0.0, 1.0, 2.0); val s = r.pick(1.0, 2.0)
            val out = kotlin.math.floor((nin + 2 * p - k) / s) + 1
            q(1, "Faltung: Eingabe ${n(nin)}×${n(nin)}, Kern ${n(k)}×${n(k)}, Padding ${n(p)}, Stride ${n(s)}. Seitenlänge der Ausgabe?",
                out, "", "n_out = ⌊(n_in + 2p − k)/s⌋ + 1 = ${n(out)}.", "ai_arch", tol = 1e-9)
        },
        TaskGenerator { r ->
            val d = r.pick(4096.0, 2048.0, 1024.0); val k = d; val rank = r.pick(8.0, 16.0, 64.0)
            val p = rank * (d + k)
            q(2, "LoRA auf einer ${n(d)}×${n(k)}-Gewichtsmatrix mit Rang r = ${n(rank)}: Zahl der trainierbaren Parameter?",
                p, "", "ΔW = BA mit B ∈ ℝ^{d×r}, A ∈ ℝ^{r×k}: r(d + k) = ${n(p, 8)} statt ${n(d * k, 8)} (${n(100 * p / (d * k), 3)} %).", "ai_llm", tol = 1e-9)
        },
        TaskGenerator { r ->
            val layers = r.pick(12.0, 24.0, 32.0); val d = r.pick(768.0, 2048.0, 4096.0)
            val pb = 12 * layers * d * d / 1e9
            q(2, "Transformer mit ${n(layers)} Blöcken und Breite d = ${n(d)}: grobe Parameterzahl ohne Embeddings (12·L·d²) in Mrd.?",
                pb, "Mrd.", "≈ 12Ld² = 12·${n(layers)}·${n(d)}² = ${n(pb)} Mrd. (4d² Attention + 8d² MLP pro Block).", "ai_transformer")
        },
        TaskGenerator { r ->
            val t = r.pick(0.5, 2.0); val z1 = 2.0; val z2 = 0.0
            val p = exp(z1 / t) / (exp(z1 / t) + exp(z2 / t))
            q(2, "Zwei Kandidaten-Token mit Logits 2 und 0. Wahrscheinlichkeit des ersten bei Temperatur T = ${n(t)}?",
                p, "", "p = e^{2/T}/(e^{2/T} + 1) = ${n(p)} (bei T = 1 wären es ${n(exp(2.0) / (exp(2.0) + 1))}). T < 1 schärft, T > 1 glättet.", "ai_inference")
        }
    )
}

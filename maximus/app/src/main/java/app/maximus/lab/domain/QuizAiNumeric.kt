package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

/** Calculation tasks for the AI learning path: metrics, trees, clustering, association, Bayes, RL, CNN sizes … */
internal object QuizAiNumeric {
    private fun Random.pick(vararg xs: Double) = xs[nextInt(xs.size)]
    private fun Random.int(lo: Int, hi: Int) = lo + nextInt(hi - lo + 1)
    private fun n(v: Double, d: Int = 4) = Fmt.num(v, d)
    private var counter = 0
    private val A = Topic.AI

    private fun q(diff: Int, prompt: String, answer: Double, unit: String, solution: String, chapter: String, tol: Double = 0.02) =
        Question("ai-n${++counter}", A, diff, prompt, solution, answer = answer, unit = unit, tolerance = tol, chapterKey = chapter)

    private fun log2(x: Double) = ln(x) / ln(2.0)
    private fun h2(p: Double) = if (p <= 0.0 || p >= 1.0) 0.0 else -p * log2(p) - (1 - p) * log2(1 - p)

    val all: List<QuestionGenerator> = listOf(
        // ---- AI von Null ----
        QuestionGenerator { r ->
            val y1 = r.int(100, 300).toDouble(); val y2 = r.int(100, 300).toDouble()
            val e1 = r.int(-30, 30).toDouble(); val e2 = r.int(-30, 30).toDouble()
            val mse = (e1 * e1 + e2 * e2) / 2
            q(1, "Echte Werte ${n(y1)} und ${n(y2)}, Vorhersagen ${n(y1 + e1)} und ${n(y2 + e2)}. Mittlerer quadratischer Fehler (MSE)?",
                mse, "", "Fehler ${n(e1)} und ${n(e2)}; MSE = (${n(e1 * e1)} + ${n(e2 * e2)})/2 = ${n(mse)}.", "ai0_model", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val w = r.pick(0.5, 1.0, 2.0); val x = r.pick(1.0, 2.0, 3.0); val y = r.pick(4.0, 6.0, 10.0); val eta = r.pick(0.01, 0.05, 0.1)
            val g = 2 * (w * x - y) * x
            val w1 = w - eta * g
            q(1, "Modell ŷ = w·x mit w = ${n(w)}, Beispiel x = ${n(x)}, y = ${n(y)}, Verlust (ŷ − y)². Neues w nach einem Gradientenschritt mit η = ${n(eta)}?",
                w1, "", "∂L/∂w = 2(wx − y)x = ${n(g)}; w ← w − η·∂L/∂w = ${n(w)} − ${n(eta)}·${n(g)} = ${n(w1)}.", "ai0_model", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val w = doubleArrayOf(r.int(-2, 3).toDouble(), r.int(-2, 3).toDouble(), r.int(-2, 3).toDouble())
            val x = doubleArrayOf(r.int(0, 3).toDouble(), r.int(0, 3).toDouble(), r.int(0, 3).toDouble())
            val dot = w.indices.sumOf { w[it] * x[it] }
            q(1, "Skalarprodukt von w = (${w.joinToString("; ") { n(it) }}) und x = (${x.joinToString("; ") { n(it) }})?",
                dot, "", "w·x = Σ wᵢxᵢ = ${w.indices.joinToString(" + ") { "${n(w[it])}·${n(x[it])}" }} = ${n(dot)}.", "ai0_math", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val mu = r.pick(70.0, 100.0, 180.0); val sd = r.pick(5.0, 10.0, 15.0); val x = mu + sd * r.pick(-2.0, -1.0, 0.5, 1.5, 2.0)
            val z = (x - mu) / sd
            q(1, "Merkmal mit Mittelwert ${n(mu)} und Standardabweichung ${n(sd)}. Standardisierter Wert von x = ${n(x)}?",
                z, "", "z = (x − μ)/σ = (${n(x)} − ${n(mu)})/${n(sd)} = ${n(z)}.", "ev_features", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val lo = r.pick(0.0, 10.0, 20.0); val hi = lo + r.pick(50.0, 100.0, 200.0); val x = lo + (hi - lo) * r.pick(0.25, 0.4, 0.75, 0.9)
            val s = (x - lo) / (hi - lo)
            q(1, "Min-Max-Skalierung: Minimum ${n(lo)}, Maximum ${n(hi)}. Skalierter Wert von x = ${n(x)}?",
                s, "", "x′ = (x − x_min)/(x_max − x_min) = ${n(x - lo)}/${n(hi - lo)} = ${n(s)}.", "ev_features", tol = 1e-3)
        },

        // ---- Regression ----
        QuestionGenerator { r ->
            val b = r.pick(0.5, 1.5, 2.0, 3.0); val a = r.pick(1.0, 2.0, 5.0)
            val ys = (1..3).map { a + b * it }
            q(1, "Drei Punkte (1; ${n(ys[0])}), (2; ${n(ys[1])}), (3; ${n(ys[2])}) liegen exakt auf einer Geraden. Steigung θ₁ nach kleinsten Quadraten?",
                b, "", "x̄ = 2, ȳ = ${n(ys.average())}; θ₁ = Σ(x − x̄)(y − ȳ)/Σ(x − x̄)² = ${n(2 * b)}/2 = ${n(b)}.", "ml_regression", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val e = List(4) { r.int(-6, 6).toDouble() }
            val mae = e.sumOf { abs(it) } / 4
            q(1, "Residuen (y − ŷ): ${e.joinToString("; ") { n(it) }}. Mittlerer absoluter Fehler (MAE)?",
                mae, "", "MAE = (|${e.joinToString("| + |") { n(it) }}|)/4 = ${n(mae)}.", "ml_regression", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val e = List(4) { r.int(-6, 6).toDouble() }.let { if (it.all { v -> v == 0.0 }) listOf(1.0, -2.0, 3.0, 0.0) else it }
            val rmse = sqrt(e.sumOf { it * it } / 4)
            q(2, "Residuen ${e.joinToString("; ") { n(it) }}. RMSE?",
                rmse, "", "MSE = (${e.joinToString(" + ") { n(it * it) }})/4 = ${n(e.sumOf { it * it } / 4)}; RMSE = √MSE = ${n(rmse)}.", "ml_regression")
        },
        QuestionGenerator { r ->
            val tot = r.pick(200.0, 500.0, 1000.0); val res = tot * r.pick(0.05, 0.1, 0.25, 0.4)
            val r2 = 1 - res / tot
            q(1, "Summe der quadrierten Residuen SS_res = ${n(res)}, Gesamtquadratsumme SS_tot = ${n(tot)}. Bestimmtheitsmaß R²?",
                r2, "", "R² = 1 − SS_res/SS_tot = 1 − ${n(res / tot)} = ${n(r2)}.", "ml_regression", tol = 1e-3)
        },

        // ---- Klassifikation und Metriken ----
        QuestionGenerator { r ->
            val tp = r.int(20, 90).toDouble(); val fp = r.int(5, 40).toDouble()
            val p = tp / (tp + fp)
            q(1, "Ein Klassifikator meldet ${n(tp + fp)} Fälle als positiv, davon sind ${n(tp)} wirklich positiv. Precision?",
                p, "", "Precision = TP/(TP + FP) = ${n(tp)}/${n(tp + fp)} = ${n(p)}.", "ev_metrics")
        },
        QuestionGenerator { r ->
            val pos = r.int(50, 200).toDouble(); val tp = (pos * r.pick(0.5, 0.7, 0.8, 0.9)).let { kotlin.math.round(it) }
            val rec = tp / pos
            q(1, "Von ${n(pos)} tatsächlich Kranken erkennt das Modell ${n(tp)}. Recall?",
                rec, "", "Recall = TP/(TP + FN) = ${n(tp)}/${n(pos)} = ${n(rec)}.", "ev_metrics")
        },
        QuestionGenerator { r ->
            val tp = r.int(30, 80).toDouble(); val tn = r.int(100, 300).toDouble(); val fp = r.int(5, 30).toDouble(); val fn = r.int(5, 30).toDouble()
            val acc = (tp + tn) / (tp + tn + fp + fn)
            q(1, "Konfusionsmatrix: TP = ${n(tp)}, TN = ${n(tn)}, FP = ${n(fp)}, FN = ${n(fn)}. Accuracy?",
                acc, "", "Accuracy = (TP + TN)/N = ${n(tp + tn)}/${n(tp + tn + fp + fn)} = ${n(acc)}.", "ev_metrics")
        },
        QuestionGenerator { r ->
            val tn = r.int(100, 400).toDouble(); val fp = r.int(5, 60).toDouble()
            val spec = tn / (tn + fp)
            q(2, "TN = ${n(tn)}, FP = ${n(fp)}. Spezifität?",
                spec, "", "Spezifität = TN/(TN + FP) = ${n(tn)}/${n(tn + fp)} = ${n(spec)}; die Falsch-Positiv-Rate ist ${n(1 - spec)}.", "ev_metrics")
        },
        QuestionGenerator { r ->
            val p = r.pick(0.5, 0.6, 0.8, 0.9); val rc = r.pick(0.4, 0.5, 0.7, 0.95)
            val f1 = 2 * p * rc / (p + rc)
            q(1, "Precision ${n(p)}, Recall ${n(rc)}. F1-Score?",
                f1, "", "F1 = 2PR/(P + R) = 2·${n(p)}·${n(rc)}/${n(p + rc)} = ${n(f1)}.", "ev_metrics")
        },
        QuestionGenerator { r ->
            val inter = r.int(2, 6).toDouble(); val a = inter + r.int(0, 4); val b = inter + r.int(0, 4)
            val j = inter / (a + b - inter)
            q(2, "Vorhergesagte Menge mit ${n(a)} Elementen, wahre Menge mit ${n(b)} Elementen, ${n(inter)} gemeinsam. Jaccard-Index?",
                j, "", "|A ∪ B| = ${n(a)} + ${n(b)} − ${n(inter)} = ${n(a + b - inter)}; J = ${n(inter)}/${n(a + b - inter)} = ${n(j)}.", "ml_classification")
        },
        QuestionGenerator { r ->
            val k = r.int(3, 10).toDouble()
            val m = k * (k - 1) / 2
            q(1, "Wie viele binäre Klassifikatoren braucht One-vs-One bei ${n(k)} Klassen?",
                m, "", "K(K − 1)/2 = ${n(k)}·${n(k - 1)}/2 = ${n(m)} (One-vs-Rest bräuchte ${n(k)}).", "ml_classification", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val p = r.pick(0.9, 0.7, 0.5, 0.2, 0.05)
            val ll = -ln(p)
            q(2, "Ein positives Beispiel bekommt die Wahrscheinlichkeit p = ${n(p)}. Log-Loss-Beitrag (natürlicher Log)?",
                ll, "", "−ln p = ${n(ll)}. Je sicherer falsch (p → 0), desto teurer.", "ml_classification")
        },

        // ---- KNN, Bäume, Ensembles ----
        QuestionGenerator { r ->
            val x1 = r.int(0, 5).toDouble(); val y1 = r.int(0, 5).toDouble(); val dx = r.pick(3.0, 6.0, 5.0, 8.0); val dy = r.pick(4.0, 8.0, 12.0, 6.0)
            val d = sqrt(dx * dx + dy * dy)
            q(1, "Euklidische Distanz zwischen (${n(x1)}; ${n(y1)}) und (${n(x1 + dx)}; ${n(y1 + dy)})?",
                d, "", "d = √(${n(dx)}² + ${n(dy)}²) = √${n(dx * dx + dy * dy)} = ${n(d)}.", "alg_knn")
        },
        QuestionGenerator { r ->
            val a = List(3) { r.int(0, 9).toDouble() }; val b = List(3) { r.int(0, 9).toDouble() }
            val d = a.indices.sumOf { abs(a[it] - b[it]) }
            q(1, "Manhattan-Distanz zwischen (${a.joinToString("; ") { n(it) }}) und (${b.joinToString("; ") { n(it) }})?",
                d, "", "Σ|aᵢ − bᵢ| = ${a.indices.joinToString(" + ") { n(abs(a[it] - b[it])) }} = ${n(d)}.", "ml_clustering", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val k = r.pick(3.0, 5.0, 7.0); val a = r.int(((k + 1) / 2).toInt(), k.toInt()).toDouble()
            val share = a / k
            q(1, "KNN mit k = ${n(k)}: ${n(a)} Nachbarn gehören zu Klasse A, der Rest zu B. Welchen Anteil der Stimmen bekommt A?",
                share, "", "${n(a)}/${n(k)} = ${n(share)} – A gewinnt die Mehrheit.", "alg_knn")
        },
        QuestionGenerator { r ->
            val p = r.pick(0.5, 0.25, 0.1, 0.2, 0.4, 0.125)
            val h = h2(p)
            q(2, "Ein Knoten enthält die Klassen im Verhältnis ${n(p)} zu ${n(1 - p)}. Entropie in Bit?",
                h, "Bit", "H = −p log₂p − (1 − p) log₂(1 − p) = ${n(h)} Bit (50/50 wäre 1 Bit, rein wäre 0).", "alg_trees")
        },
        QuestionGenerator { r ->
            val p = r.pick(0.5, 0.8, 0.9, 0.7, 0.6)
            val g = 1 - p * p - (1 - p) * (1 - p)
            q(2, "Gini-Unreinheit eines Knotens mit Klassenanteilen ${n(p)} und ${n(1 - p)}?",
                g, "", "G = 1 − (${n(p)}² + ${n(1 - p)}²) = ${n(g)}.", "alg_trees", tol = 1e-3)
        },
        QuestionGenerator { r ->
            // Parent 8 A / 8 B (H = 1 Bit); children of 8 each: left la A / (8 − la) B, right (8 − la) A / la B.
            val la = r.int(5, 8)
            val hc = h2(la / 8.0)
            val ig = 1 - hc
            q(3, "Elternknoten 8 A / 8 B (Entropie 1 Bit). Eine Frage teilt in links $la A / ${8 - la} B und rechts ${8 - la} A / $la B. Informationsgewinn in Bit?",
                ig, "Bit", "Beide Kinder haben H = ${n(hc)} Bit und je die Hälfte der Beispiele; IG = 1 − ½·${n(hc)} − ½·${n(hc)} = ${n(ig)} Bit.", "alg_trees", tol = 0.03)
        },
        QuestionGenerator { r ->
            val nn = r.pick(10.0, 50.0, 1000.0)
            val oob = (1 - 1 / nn).pow(nn) * 100
            q(3, "Bootstrap-Stichprobe aus N = ${n(nn)} Beispielen (mit Zurücklegen). Wie viel Prozent der Beispiele fehlen im Mittel (Out-of-Bag)?",
                oob, "%", "(1 − 1/N)^N = ${n(oob / 100)} → ${n(oob)} % (Grenzwert 1/e ≈ 36,8 %).", "alg_ensembles")
        },
        QuestionGenerator { r ->
            val eps = r.pick(0.1, 0.2, 0.3, 0.4)
            val alpha = 0.5 * ln((1 - eps) / eps)
            q(3, "Ein schwacher Klassifikator in AdaBoost hat gewichteten Fehler ε = ${n(eps)}. Stimmgewicht α?",
                alpha, "", "α = ½ ln((1 − ε)/ε) = ½ ln(${n((1 - eps) / eps)}) = ${n(alpha)}. Je kleiner ε, desto lauter die Stimme.", "alg_ensembles")
        },

        // ---- Logistische Regression, Bayes ----
        QuestionGenerator { r ->
            val z = r.pick(-2.0, -1.0, 0.0, 0.5, 1.0, 2.0, 3.0)
            val p = 1 / (1 + exp(-z))
            q(1, "Logistische Regression: θᵀx = ${n(z)}. Vorhergesagte Wahrscheinlichkeit P(y = 1)?",
                p, "", "σ(z) = 1/(1 + e^{−z}) = 1/(1 + ${n(exp(-z))}) = ${n(p)}.", "alg_logreg")
        },
        QuestionGenerator { r ->
            val th = r.pick(0.3, 0.7, 1.1, -0.5)
            val or = exp(th)
            q(2, "Koeffizient θⱼ = ${n(th)}. Um welchen Faktor ändern sich die Odds, wenn xⱼ um 1 steigt?",
                or, "", "Odds-Verhältnis e^{θⱼ} = e^{${n(th)}} = ${n(or)}.", "alg_logreg")
        },
        QuestionGenerator { r ->
            val p = r.pick(0.2, 0.5, 0.75, 0.8, 0.9)
            val odds = p / (1 - p)
            q(1, "Eine Wahrscheinlichkeit p = ${n(p)}. Wie groß sind die Odds?",
                odds, "", "odds = p/(1 − p) = ${n(p)}/${n(1 - p)} = ${n(odds)}.", "alg_logreg")
        },
        QuestionGenerator { r ->
            val prior = r.pick(0.2, 0.3, 0.4, 0.5); val ls = r.pick(0.3, 0.5, 0.6); val ln_ = r.pick(0.02, 0.05, 0.1)
            val post = prior * ls / (prior * ls + (1 - prior) * ln_)
            q(2, "Spamfilter: P(Spam) = ${n(prior)}, P(Wort | Spam) = ${n(ls)}, P(Wort | kein Spam) = ${n(ln_)}. P(Spam | Wort)?",
                post, "", "Zähler ${n(prior)}·${n(ls)} = ${n(prior * ls)}; Nenner + ${n(1 - prior)}·${n(ln_)} = ${n(prior * ls + (1 - prior) * ln_)}; P = ${n(post)}.", "alg_bayes")
        },
        QuestionGenerator { r ->
            val cnt = r.int(0, 5).toDouble(); val total = r.pick(100.0, 200.0, 500.0); val vocab = r.pick(1000.0, 2000.0, 5000.0)
            val p = (cnt + 1) / (total + vocab)
            q(3, "Laplace-Glättung: Das Wort kam ${n(cnt)}-mal unter ${n(total)} Wörtern der Klasse vor, Vokabular ${n(vocab)}. Geglättete P(Wort | Klasse)?",
                p, "", "(n + 1)/(N + |V|) = ${n(cnt + 1)}/${n(total + vocab)} = ${n(p)}.", "alg_bayes")
        },

        // ---- Clustering, Assoziation, Anomalie, Sequenz, Empfehlung, PCA ----
        QuestionGenerator { r ->
            val xs = List(3) { r.int(0, 20).toDouble() }
            val c = xs.average()
            q(1, "k-Means (1D): Ein Cluster enthält die Punkte ${xs.joinToString("; ") { n(it) }}. Neues Zentrum?",
                c, "", "Mittelwert = (${xs.joinToString(" + ") { n(it) }})/3 = ${n(c)}.", "alg_kmeans", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val c = r.int(3, 10).toDouble(); val xs = List(3) { c + r.int(-3, 3) }
            val wcss = xs.sumOf { (it - c) * (it - c) }
            q(2, "Cluster mit Zentrum ${n(c)} und Punkten ${xs.joinToString("; ") { n(it) }} (1D). Beitrag zur Inertia (WCSS)?",
                wcss, "", "Σ(x − μ)² = ${xs.joinToString(" + ") { n((it - c) * (it - c)) }} = ${n(wcss)}.", "alg_kmeans", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val a = r.pick(1.0, 2.0, 3.0); val b = a + r.pick(2.0, 3.0, 5.0)
            val s = (b - a) / maxOf(a, b)
            q(2, "Ein Punkt hat mittleren Abstand a = ${n(a)} zum eigenen Cluster und b = ${n(b)} zum nächsten fremden. Silhouettenwert?",
                s, "", "s = (b − a)/max(a, b) = ${n(b - a)}/${n(b)} = ${n(s)}.", "ml_clustering")
        },
        QuestionGenerator { r ->
            val total = r.pick(1000.0, 2000.0, 5000.0); val a = total * r.pick(0.1, 0.2, 0.25); val both = a * r.pick(0.3, 0.4, 0.5, 0.6)
            val supp = both / total * 100
            q(1, "${n(total)} Warenkörbe, ${n(both)} enthalten Brot und Butter. Support von {Brot, Butter} in %?",
                supp, "%", "supp = ${n(both)}/${n(total)} = ${n(supp)} %.", "ml_association")
        },
        QuestionGenerator { r ->
            val a = r.pick(100.0, 200.0, 250.0); val both = a * r.pick(0.3, 0.4, 0.5, 0.8)
            val conf = both / a * 100
            q(1, "${n(a)} Körbe enthalten Chips, ${n(both)} davon auch Bier. Konfidenz der Regel Chips ⇒ Bier in %?",
                conf, "%", "conf = supp(A ∪ B)/supp(A) = ${n(both)}/${n(a)} = ${n(conf)} %.", "ml_association")
        },
        QuestionGenerator { r ->
            val total = 1000.0; val a = r.pick(100.0, 200.0); val b = r.pick(50.0, 80.0, 100.0, 300.0); val both = minOf(a, b) * r.pick(0.3, 0.5)
            val lift = (both / a) / (b / total)
            q(2, "${n(total)} Körbe: ${n(a)} mit A, ${n(b)} mit B, ${n(both)} mit beiden. Lift der Regel A ⇒ B?",
                lift, "", "conf = ${n(both)}/${n(a)} = ${n(both / a)}; supp(B) = ${n(b / total)}; Lift = ${n(lift)} (> 1: mehr als Zufall).", "ml_association")
        },
        QuestionGenerator { r ->
            val mu = r.pick(30.0, 50.0, 100.0); val sd = r.pick(4.0, 5.0, 10.0); val x = mu + sd * r.pick(1.5, 2.5, 3.5, 4.0, -3.0)
            val z = (x - mu) / sd
            q(1, "Zahlungen: Mittelwert ${n(mu)} €, Standardabweichung ${n(sd)} €. z-Score einer Zahlung über ${n(x)} €?",
                z, "", "z = (${n(x)} − ${n(mu)})/${n(sd)} = ${n(z)}. Ab |z| > 3 ist ein Wert bei Normalverteilung sehr auffällig.", "ml_anomaly", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val q1 = r.pick(10.0, 20.0, 40.0); val q3 = q1 + r.pick(10.0, 20.0, 30.0)
            val upper = q3 + 1.5 * (q3 - q1)
            q(1, "Boxplot: Q1 = ${n(q1)}, Q3 = ${n(q3)}. Ab welchem Wert gilt ein Punkt nach der IQR-Regel als oberer Ausreißer?",
                upper, "", "IQR = ${n(q3 - q1)}; obere Grenze Q3 + 1,5·IQR = ${n(upper)}.", "ml_anomaly", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val ys = List(3) { r.int(10, 40).toDouble() }
            val ma = ys.average()
            q(1, "Gleitender Mittelwert über 3: Die letzten Werte sind ${ys.joinToString("; ") { n(it) }}. Prognose für den nächsten Wert?",
                ma, "", "ŷ = (${ys.joinToString(" + ") { n(it) }})/3 = ${n(ma)}.", "ml_sequence", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val a = r.pick(0.2, 0.3, 0.5); val s0 = r.int(10, 30).toDouble(); val y = r.int(10, 40).toDouble()
            val s1 = a * y + (1 - a) * s0
            q(2, "Exponentielle Glättung mit α = ${n(a)}: alter Glättungswert ${n(s0)}, neuer Messwert ${n(y)}. Neuer Glättungswert?",
                s1, "", "s = αy + (1 − α)s_alt = ${n(a)}·${n(y)} + ${n(1 - a)}·${n(s0)} = ${n(s1)}.", "ml_sequence", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val from = r.int(20, 80).toDouble(); val to = (from * r.pick(0.25, 0.4, 0.5)).let { kotlin.math.round(it) }
            val p = to / from
            q(1, "In den Daten folgt auf Zustand ‚Startseite‘ ${n(from)}-mal ein Klick, davon ${n(to)}-mal auf ‚Angebote‘. Übergangswahrscheinlichkeit der Markov-Kette?",
                p, "", "P(j | i) = n(i → j)/n(i) = ${n(to)}/${n(from)} = ${n(p)}.", "ml_sequence")
        },
        QuestionGenerator { r ->
            val u = doubleArrayOf(r.int(1, 5).toDouble(), r.int(0, 5).toDouble(), r.int(1, 5).toDouble())
            val v = doubleArrayOf(r.int(1, 5).toDouble(), r.int(0, 5).toDouble(), r.int(0, 5).toDouble())
            val dot = u.indices.sumOf { u[it] * v[it] }
            val cos = dot / (sqrt(u.sumOf { it * it }) * sqrt(v.sumOf { it * it }))
            q(2, "Bewertungsvektoren zweier Nutzer: u = (${u.joinToString("; ") { n(it) }}), v = (${v.joinToString("; ") { n(it) }}). Kosinusähnlichkeit?",
                cos, "", "u·v = ${n(dot)}; |u| = ${n(sqrt(u.sumOf { it * it }))}, |v| = ${n(sqrt(v.sumOf { it * it }))}; cos = ${n(cos)}.", "ml_recommender")
        },
        QuestionGenerator { r ->
            val l = listOf(r.pick(4.0, 5.0, 6.0, 8.0), r.pick(1.0, 2.0, 3.0), r.pick(0.5, 1.0))
            val ratio = l[0] / l.sum() * 100
            q(1, "PCA: Eigenwerte der Kovarianzmatrix ${l.joinToString("; ") { n(it) }}. Erklärte Varianz der ersten Hauptkomponente in %?",
                ratio, "%", "λ₁/Σλ = ${n(l[0])}/${n(l.sum())} = ${n(ratio)} %.", "ml_dimred")
        },

        // ---- Neuronale Netze ----
        QuestionGenerator { r ->
            val w = doubleArrayOf(r.pick(-1.0, 0.5, 1.0, 2.0), r.pick(-2.0, -0.5, 1.0)); val x = doubleArrayOf(r.int(0, 3).toDouble(), r.int(0, 3).toDouble())
            val b = r.pick(-1.0, 0.0, 0.5)
            val z = w[0] * x[0] + w[1] * x[1] + b
            val a = maxOf(0.0, z)
            q(1, "Neuron mit Gewichten (${n(w[0])}; ${n(w[1])}), Bias ${n(b)}, ReLU-Aktivierung, Eingabe (${n(x[0])}; ${n(x[1])}). Ausgabe?",
                a, "", "z = ${n(w[0])}·${n(x[0])} + ${n(w[1])}·${n(x[1])} + ${n(b)} = ${n(z)}; ReLU(z) = max(0, z) = ${n(a)}.", "dl_neuron", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val nIn = r.pick(8.0, 64.0, 128.0, 784.0); val h = r.pick(16.0, 32.0, 64.0); val out = r.pick(1.0, 3.0, 10.0)
            val p = nIn * h + h + h * out + out
            q(2, "Netz ${n(nIn)} → Dense(${n(h)}) → Dense(${n(out)}). Gesamtzahl der Parameter (mit Bias)?",
                p, "", "${n(nIn)}·${n(h)} + ${n(h)} = ${n(nIn * h + h, 8)}; ${n(h)}·${n(out)} + ${n(out)} = ${n(h * out + out)}; Summe ${n(p, 8)}.", "dl_keras", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val nData = r.pick(10000.0, 50000.0, 60000.0, 1234.0); val b = r.pick(32.0, 64.0, 128.0)
            val steps = kotlin.math.ceil(nData / b)
            q(1, "${n(nData, 6)} Trainingsbeispiele, Batchgröße ${n(b)}. Wie viele Gradientenschritte pro Epoche?",
                steps, "", "⌈N/B⌉ = ⌈${n(nData / b)}⌉ = ${n(steps)}.", "dl_keras", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val k = r.pick(3.0, 5.0); val cin = r.pick(1.0, 3.0, 16.0); val cout = r.pick(8.0, 16.0, 32.0, 64.0)
            val p = (k * k * cin + 1) * cout
            q(2, "Faltungsschicht: ${n(cout)} Filter der Größe ${n(k)}×${n(k)} auf einer Eingabe mit ${n(cin)} Kanälen. Anzahl Parameter (mit Bias)?",
                p, "", "(k·k·C_in + 1)·C_out = (${n(k * k * cin)} + 1)·${n(cout)} = ${n(p, 8)} – unabhängig von der Bildgröße.", "dl_cnn", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val nIn = r.pick(28.0, 32.0, 64.0, 224.0); val k = r.pick(3.0, 5.0, 7.0); val p = r.pick(0.0, 1.0, 2.0); val s = r.pick(1.0, 2.0)
            val out = kotlin.math.floor((nIn + 2 * p - k) / s) + 1
            q(2, "Eingabe ${n(nIn)}×${n(nIn)}, Filter ${n(k)}×${n(k)}, Padding ${n(p)}, Stride ${n(s)}. Breite der Ausgabe?",
                out, "", "⌊(n + 2p − k)/s⌋ + 1 = ⌊${n((nIn + 2 * p - k) / s)}⌋ + 1 = ${n(out)}.", "dl_cnn", tol = 1e-9)
        },
        QuestionGenerator { r ->
            val x = r.pick(1.0, 2.0); val w1 = r.pick(0.5, 1.0); val w2 = r.pick(1.0, 2.0); val y = r.pick(3.0, 4.0, 5.0)
            val h = maxOf(0.0, w1 * x); val yh = w2 * h
            val g = (yh - y) * w2 * (if (w1 * x > 0) 1.0 else 0.0) * x
            q(3, "Mini-Netz: h = ReLU(w₁x), ŷ = w₂h, L = ½(ŷ − y)² mit x = ${n(x)}, w₁ = ${n(w1)}, w₂ = ${n(w2)}, y = ${n(y)}. ∂L/∂w₁?",
                g, "", "ŷ = ${n(yh)}; ∂L/∂ŷ = ${n(yh - y)}; ∂L/∂w₁ = (ŷ − y)·w₂·ReLU′·x = ${n(g)}.", "dl_backprop_numbers", tol = 1e-3)
        },

        // ---- Reinforcement Learning ----
        QuestionGenerator { r ->
            val g = r.pick(0.5, 0.8, 0.9, 0.99); val rs = List(3) { r.int(0, 10).toDouble() }
            val ret = rs[0] + g * rs[1] + g * g * rs[2]
            q(1, "Belohnungen ${rs.joinToString("; ") { n(it) }} in den nächsten drei Schritten, Diskontfaktor γ = ${n(g)}. Return G?",
                ret, "", "G = ${n(rs[0])} + ${n(g)}·${n(rs[1])} + ${n(g * g)}·${n(rs[2])} = ${n(ret)}.", "rl_basics", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val qv = r.int(0, 5).toDouble(); val rew = r.pick(-1.0, 0.0, 1.0, 5.0); val mx = r.int(0, 10).toDouble()
            val g = r.pick(0.9, 0.95, 0.5); val a = r.pick(0.1, 0.5, 0.2)
            val target = rew + g * mx; val nq = qv + a * (target - qv)
            q(2, "Q-Learning: Q(s,a) = ${n(qv)}, Belohnung ${n(rew)}, max Q(s′,·) = ${n(mx)}, γ = ${n(g)}, α = ${n(a)}. Neuer Q-Wert?",
                nq, "", "Ziel = ${n(rew)} + ${n(g)}·${n(mx)} = ${n(target)}; δ = ${n(target - qv)}; Q ← ${n(qv)} + ${n(a)}·${n(target - qv)} = ${n(nq)}.", "rl_qlearning", tol = 1e-3)
        },
        QuestionGenerator { r ->
            val eps = r.pick(0.1, 0.2, 0.3); val k = r.pick(2.0, 4.0, 5.0)
            val p = 1 - eps + eps / k
            q(3, "ε-greedy mit ε = ${n(eps)} und ${n(k)} Aktionen. Mit welcher Wahrscheinlichkeit wird die beste Aktion gewählt?",
                p, "", "1 − ε (gierig) + ε/k (zufällig doch die beste) = ${n(1 - eps)} + ${n(eps / k)} = ${n(p)}.", "rl_basics", tol = 1e-3)
        },

        // ---- Generative KI ----
        QuestionGenerator { r ->
            val z1 = r.pick(2.0, 3.0); val z2 = r.pick(1.0, 0.0); val t = r.pick(0.5, 1.0, 2.0)
            val p = exp(z1 / t) / (exp(z1 / t) + exp(z2 / t))
            q(2, "Zwei Token mit Logits ${n(z1)} und ${n(z2)}, Temperatur T = ${n(t)}. Wahrscheinlichkeit des ersten Tokens?",
                p, "", "p = e^{${n(z1)}/T}/(e^{${n(z1)}/T} + e^{${n(z2)}/T}) = ${n(p)}. Kleines T schärft, großes glättet.", "gen_prompting")
        },
        QuestionGenerator { r ->
            val d = r.pick(1024.0, 4096.0, 8192.0); val rr = r.pick(8.0, 16.0, 64.0)
            val pct = rr * (d + d) / (d * d) * 100
            q(3, "LoRA auf einer ${n(d, 6)}×${n(d, 6)}-Matrix mit Rang r = ${n(rr)}. Anteil trainierbarer Parameter in %?",
                pct, "%", "r(d + k)/(d·k) = ${n(rr * 2 * d, 8)}/${n(d * d, 10)} = ${n(pct)} %.", "gen_finetune")
        },
        QuestionGenerator { r ->
            val loss = r.pick(1.0, 2.0, 2.5, 3.0)
            val ppl = exp(loss)
            q(2, "Ein Sprachmodell hat mittleren Kreuzentropie-Verlust ${n(loss)} (natürlicher Log). Perplexität?",
                ppl, "", "PPL = e^L = ${n(ppl)} – das Modell ist im Mittel so unsicher wie bei einer Wahl unter ${n(ppl)} gleich wahrscheinlichen Token.", "ai_llm")
        }
    )
}

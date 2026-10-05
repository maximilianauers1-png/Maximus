package app.maximus.lab.domain

/**
 * AI engineering from zero: written for a physicist who knows the maths but not the field. Each chapter
 * starts with the intuition, then the formulas, then the engineering consequences.
 */
internal object CompendiumAi {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "AI-Grundlagen",
        chapter(
            "ai_intro", T, 1, "Was ist maschinelles Lernen?",
            "Lernen aus Daten, Lernarten, Trainings-/Validierungs-/Testdaten, Generalisierung, Overfitting, Metriken.",
            emptyList(),
            sec("Die Grundidee", """
                Klassische Software: Ein Mensch schreibt die Regeln. Maschinelles Lernen (ML): Man gibt Beispiele vor und lässt einen Algorithmus die Parameter θ eines Modells f_θ so einstellen, dass es die Beispiele gut beschreibt — und vor allem neue, ungesehene Daten. Physikalisch gesprochen: ein Fit mit sehr vielen Parametern, bei dem nicht die Anpassung an die Messpunkte zählt, sondern die Vorhersage neuer Messpunkte.

                • Überwacht (supervised): Paare (x, y), z. B. Bild → „Katze“. Regression (y kontinuierlich) oder Klassifikation (y Klasse).
                • Unüberwacht: nur x; Strukturen finden (Clustering, Dimensionsreduktion, Dichteschätzung).
                • Selbstüberwacht: Labels aus den Daten selbst, z. B. das nächste Wort eines Textes — die Grundlage großer Sprachmodelle.
                • Bestärkendes Lernen (RL): ein Agent handelt, erhält Belohnungen und lernt eine Strategie.
            """,
                fm("Empirisches Risiko", "L(θ) = (1/N) Σᵢ ℓ(f_θ(xᵢ), yᵢ)"),
                fm("Ziel", "θ* = argmin_θ L(θ)  (auf Trainingsdaten), bewertet auf Testdaten")
            ),
            sec("Generalisierung und Overfitting", """
                Ein Modell, das die Trainingsdaten auswendig lernt, scheitert an neuen Daten (Overfitting) — wie ein Polynom hohen Grades durch verrauschte Messpunkte. Zu einfache Modelle verfehlen die Struktur (Underfitting). Deshalb teilt man die Daten: Trainingsmenge (Parameter lernen), Validierungsmenge (Hyperparameter wie Modellgröße oder Lernrate wählen), Testmenge (einmal am Ende, ehrliche Schätzung). Datenlecks — Testinformation, die ins Training gelangt — sind der häufigste Fehler in der Praxis.
            """,
                fm("Bias-Varianz-Zerlegung", "E[(y − f̂)²] = Bias² + Varianz + σ²_Rauschen"),
                fm("k-fache Kreuzvalidierung", "Fehler = Mittel über k Aufteilungen")
            ),
            sec("Metriken für Klassifikation", """
                Accuracy (Anteil richtiger Vorhersagen) täuscht bei unausgewogenen Klassen: Bei 1 % Kranken erreicht „immer gesund“ 99 %. Deshalb Precision (wie viele der als positiv Erkannten sind es wirklich), Recall (wie viele der Positiven werden gefunden) und F1. Die ROC-Kurve trägt die Trefferrate gegen die Fehlalarmrate über alle Schwellen auf; AUC ist die Fläche darunter.
            """,
                fm("Precision", "P = TP/(TP + FP)"),
                fm("Recall", "R = TP/(TP + FN)"),
                fm("F1", "F1 = 2PR/(P + R)"),
                fm("Accuracy", "(TP + TN)/(TP + TN + FP + FN)")
            )
        ),
        chapter(
            "ai_linear", T, 1, "Lineare Modelle und Gradientenabstieg",
            "Lineare und logistische Regression, Verlustfunktionen, Gradientenabstieg, Lernrate, Regularisierung.",
            emptyList(),
            sec("Lineare Regression", """
                Das einfachste Modell: ŷ = wᵀx + b. Mit quadratischem Fehler (MSE) hat das Problem eine geschlossene Lösung (Normalengleichungen) — dieselbe Mathematik wie ein linearer Least-Squares-Fit im Praktikum. Statistisch entspricht MSE der Maximum-Likelihood-Schätzung bei gaußschem Rauschen.
            """,
                fm("MSE", "L = (1/N) Σ (ŷᵢ − yᵢ)²"),
                fm("Normalengleichungen", "w = (XᵀX)⁻¹Xᵀy")
            ),
            sec("Logistische Regression", """
                Für Ja/Nein-Klassifikation wird die lineare Größe z = wᵀx + b durch die Sigmoidfunktion auf eine Wahrscheinlichkeit abgebildet. Der passende Verlust ist die Kreuzentropie (negative Log-Likelihood): Sie bestraft sichere Fehlvorhersagen stark. Für mehrere Klassen ersetzt Softmax die Sigmoidfunktion.
            """,
                fm("Sigmoid", "σ(z) = 1/(1 + e^{−z})"),
                fm("Binäre Kreuzentropie", "ℓ = −[y ln p + (1 − y) ln(1 − p)]"),
                fm("Softmax", "pₖ = e^{zₖ}/Σⱼ e^{zⱼ}"),
                fm("Kreuzentropie (Klassen)", "ℓ = −ln p_{richtig}")
            ),
            sec("Gradientenabstieg", """
                Ohne geschlossene Lösung minimiert man iterativ: Ein Schritt entgegen dem Gradienten, skaliert mit der Lernrate η. Zu große η divergiert (Oszillation über das Tal), zu kleine ist langsam. Stochastischer Gradientenabstieg (SGD) schätzt den Gradienten aus kleinen Teilmengen (Mini-Batches): billiger, und das Rauschen hilft sogar, aus schlechten Bereichen zu entkommen. Eine Epoche ist ein Durchlauf durch alle Trainingsdaten.
            """,
                fm("Update", "θ ← θ − η ∇_θ L"),
                fm("Stabilitätsgrenze (quadratisch)", "η < 2/λ_max(Hesse-Matrix)"),
                fm("Mini-Batch-Gradient", "g = (1/B) Σ_{i∈Batch} ∇ℓᵢ")
            ),
            sec("Regularisierung", """
                Strafterme auf große Gewichte verhindern Überanpassung: L2 (Ridge, „weight decay“) zieht Gewichte gleichmäßig zur Null, L1 (Lasso) setzt viele exakt auf null (Sparsity). Bayesianisch entsprechen sie gaußschen bzw. Laplace-Priors.
            """,
                fm("L2", "L_reg = L + λ Σ wᵢ²"),
                fm("L1", "L_reg = L + λ Σ |wᵢ|")
            )
        )
    ) + course(
        "Deep Learning",
        chapter(
            "ai_nn", T, 2, "Neuronale Netze und Backpropagation",
            "Neuronen, Schichten, Aktivierungsfunktionen, universelle Approximation, Rechengraph, Backprop, Initialisierung.",
            emptyList(),
            sec("Vom Neuron zum Netz", """
                Ein künstliches Neuron berechnet eine gewichtete Summe seiner Eingänge plus Bias und wendet eine nichtlineare Aktivierungsfunktion an. Schichten solcher Neuronen hintereinander bilden ein mehrschichtiges Perzeptron (MLP). Ohne Nichtlinearität wäre das ganze Netz nur eine einzige lineare Abbildung — die Aktivierungsfunktion macht es ausdrucksstark. Universeller Approximationssatz: Schon eine verborgene Schicht kann jede stetige Funktion auf einem Kompaktum beliebig genau annähern (aber nicht unbedingt effizient oder lernbar).
            """,
                fm("Schicht", "h = φ(W x + b)"),
                fm("ReLU", "ReLU(z) = max(0, z)"),
                fm("GELU", "GELU(z) = z Φ(z)"),
                fm("Parameter einer Dense-Schicht", "n_in · n_out + n_out")
            ),
            sec("Backpropagation", """
                Training braucht den Gradienten des Verlusts nach allen Parametern. Backpropagation ist schlicht die Kettenregel, rückwärts durch den Rechengraphen angewandt (Reverse-Mode-Autodiff): ein Vorwärtsdurchlauf speichert Zwischenwerte, ein Rückwärtsdurchlauf propagiert ∂L/∂h von der Ausgabe zur Eingabe. Kosten: etwa das Doppelte des Vorwärtsdurchlaufs — unabhängig von der Zahl der Parameter. Frameworks wie PyTorch und JAX machen das automatisch.
            """,
                fm("Kettenregel", "∂L/∂W = (∂L/∂h)(∂h/∂z)(∂z/∂W)"),
                fm("Rückwärts durch eine Schicht", "δ_l = (W_{l+1}ᵀ δ_{l+1}) ⊙ φ′(z_l)"),
                fm("Rechenaufwand Training", "≈ 3 × Vorwärtsdurchlauf")
            ),
            sec("Initialisierung und verschwindende Gradienten", """
                Werden Gradienten über viele Schichten mit Faktoren < 1 multipliziert, verschwinden sie; mit > 1 explodieren sie. Gute Initialisierung hält die Varianz der Aktivierungen über die Schichten konstant (Xavier/Glorot für tanh, He für ReLU). Residualverbindungen (x + F(x)) und Normalisierungsschichten lösen das Problem für sehr tiefe Netze.
            """,
                fm("Xavier", "Var(W) = 2/(n_in + n_out)"),
                fm("He", "Var(W) = 2/n_in"),
                fm("Residualblock", "y = x + F(x)")
            )
        ),
        chapter(
            "ai_training", T, 2, "Training in der Praxis",
            "Optimierer (Momentum, Adam), Batchgröße, Lernratenpläne, Normalisierung, Dropout, Mixed Precision, Fehlersuche.",
            emptyList(),
            sec("Optimierer", """
                Momentum mittelt Gradienten über die Zeit wie eine Kugel mit Trägheit und dämpft Zickzack in schmalen Tälern. Adam kombiniert Momentum mit einer pro Parameter angepassten Schrittweite (Division durch die Wurzel des gleitenden Mittels der quadrierten Gradienten) und ist der Standard für Transformer; AdamW entkoppelt den Weight Decay.
            """,
                fm("Momentum", "v ← βv + g,  θ ← θ − ηv"),
                fm("Adam", "m ← β₁m + (1 − β₁)g,  s ← β₂s + (1 − β₂)g²,  θ ← θ − η m̂/(√ŝ + ε)"),
                fm("Typische Werte", "β₁ = 0,9,  β₂ = 0,95–0,999,  ε = 10⁻⁸")
            ),
            sec("Lernrate, Batchgröße, Pläne", """
                Die Lernrate ist der wichtigste Hyperparameter. Üblich: lineares Aufwärmen (Warmup) über einige hundert bis tausend Schritte, dann Cosinus-Abfall. Größere Batches liefern genauere Gradienten und erlauben größere Lernraten (näherungsweise linear skalieren), bis zu einer kritischen Batchgröße.
            """,
                fm("Cosinus-Plan", "η(t) = η_min + ½(η_max − η_min)(1 + cos(πt/T))"),
                fm("Lineare Skalierung", "η ∝ B  (bis zur kritischen Batchgröße)")
            ),
            sec("Normalisierung und Regularisierung", """
                BatchNorm normiert jede Merkmalsdimension über den Batch, LayerNorm über die Merkmale eines Beispiels (Standard in Transformern, unabhängig von der Batchgröße). Dropout schaltet im Training zufällig Neuronen ab und wirkt wie ein Ensemble. Early Stopping beendet das Training, wenn der Validierungsfehler steigt. Datenaugmentation (Drehen, Zuschneiden, Rauschen) vergrößert den Datensatz künstlich.
            """,
                fm("LayerNorm", "y = γ (x − μ)/√(σ² + ε) + β"),
                fm("Dropout (Training)", "h̃ = h ⊙ m/(1 − p),  m ~ Bernoulli(1 − p)")
            ),
            sec("Zahlenformate und Fehlersuche", """
                Gemischte Präzision (bf16/fp16 für Rechnen, fp32 für Master-Gewichte) halbiert Speicher und verdoppelt Durchsatz auf modernen Beschleunigern. Rezept zur Fehlersuche: erst ein winziges Teilset perfekt überanpassen (prüft, ob das Modell überhaupt lernen kann), Lernkurven von Training und Validierung plotten, Gradientennormen beobachten, nur eine Änderung pro Experiment, Seeds festhalten.
            """,
                fm("Speicher Training (Adam, gemischt)", "≈ 16 Byte pro Parameter (Gewichte, Gradienten, 2 Momente, Master-Kopie)")
            )
        ),
        chapter(
            "ai_arch", T, 2, "Architekturen: CNN, RNN, Embeddings",
            "Faltungen und rezeptives Feld, ResNet, rekurrente Netze und LSTM, Embeddings als gelernte Darstellungen.",
            emptyList(),
            sec("Faltungsnetze (CNN)", """
                Bilder haben lokale Struktur und Translationssymmetrie. Eine Faltungsschicht wendet denselben kleinen Filter (z. B. 3×3) an jeder Position an: wenige Parameter, Gewichtsteilung, Translationsäquivarianz. Gestapelte Faltungen vergrößern das rezeptive Feld; Pooling oder Strides verkleinern die Auflösung. ResNets mit Residualverbindungen machten Netze mit über 100 Schichten trainierbar.
            """,
                fm("Ausgabegröße", "n_out = ⌊(n_in + 2p − k)/s⌋ + 1"),
                fm("Parameter einer Faltung", "k² · C_in · C_out + C_out")
            ),
            sec("Rekurrente Netze", """
                RNNs verarbeiten Sequenzen schrittweise mit einem verborgenen Zustand. Sie leiden unter verschwindenden Gradienten über lange Sequenzen; LSTMs und GRUs führen Gates ein, die Information gezielt speichern oder vergessen. Sie sind sequentiell und damit schwer zu parallelisieren — der Hauptgrund, warum Transformer sie weitgehend ersetzt haben. Moderne Zustandsraummodelle (z. B. Mamba) greifen die Idee mit parallelisierbarer Struktur wieder auf.
            """,
                fm("RNN", "h_t = tanh(W_h h_{t−1} + W_x x_t + b)")
            ),
            sec("Embeddings", """
                Ein Embedding bildet diskrete Objekte (Wörter, Token, Nutzer, Produkte) auf Vektoren ab, deren Geometrie Bedeutung trägt: Ähnliches liegt nahe beieinander. Sie werden mitgelernt. Ähnlichkeit misst man meist mit dem Kosinus. Embeddings sind die Grundlage für semantische Suche und RAG.
            """,
                fm("Kosinusähnlichkeit", "cos θ = a·b/(|a||b|)"),
                fm("Embedding-Tabelle", "Parameter = |Vokabular| · d")
            )
        ),
        chapter(
            "ai_transformer", T, 3, "Transformer und Attention",
            "Tokenisierung, Self-Attention, Multi-Head, Positionskodierung (RoPE), Residualstrom, kausale Maske, Komplexität.",
            emptyList(),
            sec("Token", """
                Text wird in Token zerlegt — meist Teilwörter per Byte-Pair-Encoding (BPE): häufige Zeichenfolgen werden zu einem Token zusammengefasst. Ein Vokabular hat typisch 30 000–250 000 Token; ein deutsches Wort entspricht oft 1–3 Token. Jedes Token wird über eine Embedding-Tabelle zu einem Vektor der Dimension d (Modellbreite).
            """,
                fm("Faustregel", "1 Token ≈ 3–4 Zeichen (Englisch), etwas weniger effizient auf Deutsch")
            ),
            sec("Self-Attention", """
                Jedes Token bildet drei Vektoren: Query (wonach suche ich?), Key (was biete ich an?) und Value (was gebe ich weiter?). Die Ähnlichkeit von Query und Keys bestimmt per Softmax, wie stark jedes andere Token in die neue Darstellung einfließt. So kann jedes Token direkt auf jedes andere zugreifen — unabhängig vom Abstand. Mehrere Köpfe (Multi-Head) betrachten parallel verschiedene Beziehungen.
            """,
                fm("Attention", "Attention(Q, K, V) = softmax(QKᵀ/√d_k) V"),
                fm("Multi-Head", "MHA = Concat(head₁, …, head_h) W_O"),
                fm("Kausale Maske", "Score_ij = −∞ für j > i")
            ),
            sec("Der Transformer-Block", """
                Ein Block: (Layer-)Norm → Multi-Head-Attention → Residual-Addition → Norm → Feed-Forward-Netz (MLP, typisch 4d breit) → Residual-Addition. Viele solcher Blöcke werden gestapelt; der „Residualstrom“ trägt die Information durch das Netz, jeder Block liest daraus und schreibt hinein. Sprachmodelle wie GPT, Llama oder Gemma sind „decoder-only“: kausale Attention, Vorhersage des nächsten Tokens.

                Positionsinformation: Attention allein ist reihenfolgeblind. Lösungen: sinusförmige oder gelernte Positionsembeddings, heute meist Rotary Position Embeddings (RoPE), die Query und Key positionsabhängig drehen.
            """,
                fm("Block", "x ← x + MHA(LN(x)),  x ← x + MLP(LN(x))"),
                fm("Parameter pro Block (grob)", "≈ 12 d²"),
                fm("RoPE (2D-Paar)", "q′ = R(mθ) q,  k′ = R(nθ) k ⇒ q′·k′ hängt von m − n ab")
            ),
            sec("Rechenaufwand", """
                Attention vergleicht alle Tokenpaare: Aufwand und Speicher wachsen quadratisch mit der Kontextlänge n. Lange Kontexte sind deshalb teuer; Varianten wie FlashAttention (speichereffizient, exakt), Sliding-Window- oder lineare Attention mildern das.
            """,
                fm("Attention-Aufwand", "O(n² d) pro Schicht"),
                fm("Gesamt-FLOPs Vorwärts", "≈ 2N pro Token (N Parameter)")
            )
        )
    ) + course(
        "Große Sprachmodelle",
        chapter(
            "ai_llm", T, 3, "Große Sprachmodelle: Training",
            "Vortraining, Perplexität, Skalierungsgesetze, Compute-Budget, Feinabstimmung (SFT), RLHF/DPO, LoRA.",
            emptyList(),
            sec("Vortraining", """
                Ein Sprachmodell lernt, das nächste Token vorherzusagen, auf Billionen Token aus Web, Büchern, Code. Der Verlust ist die Kreuzentropie; ihre Exponentialfunktion heißt Perplexität (effektive Zahl gleichwahrscheinlicher Kandidaten). Aus dieser einfachen Aufgabe entstehen Grammatik, Weltwissen und Schlussfolgerungsfähigkeiten — weil gute Vorhersage Verständnis der Daten erfordert.
            """,
                fm("Sprachmodell", "p(x₁…x_T) = Π_t p(x_t | x_{<t})"),
                fm("Perplexität", "PPL = exp(−(1/T) Σ_t ln p(x_t | x_{<t}))"),
                fm("Trainings-Compute", "C ≈ 6 N D  (N Parameter, D Token)")
            ),
            sec("Skalierungsgesetze", """
                Der Testverlust fällt über viele Größenordnungen als Potenzgesetz in Parametern, Daten und Compute. Für ein festes Compute-Budget gibt es eine optimale Aufteilung: Nach den Chinchilla-Ergebnissen (Hoffmann et al. 2022) etwa 20 Trainingstoken pro Parameter. Heute trainiert man kleine Modelle oft weit darüber hinaus, weil sie in der Anwendung (Inferenz) billiger sind — wichtig für Modelle auf dem Handy.
            """,
                fm("Potenzgesetz", "L(N) ≈ (N_c/N)^α + L_∞"),
                fm("Chinchilla-Faustregel", "D_opt ≈ 20 N")
            ),
            sec("Vom Basismodell zum Assistenten", """
                1. Supervised Fine-Tuning (SFT): Training auf Beispieldialogen (Anweisung → gute Antwort).
                2. Präferenzoptimierung: Menschen (oder Modelle) vergleichen Antworten; RLHF trainiert ein Belohnungsmodell und optimiert die Strategie mit PPO, DPO optimiert direkt auf den Präferenzpaaren ohne separates Belohnungsmodell.
                3. Reasoning-Training: Verstärkendes Lernen auf überprüfbaren Aufgaben (Mathe, Code) erzeugt längere Denkketten.

                Parameter-effizientes Feinabstimmen: LoRA friert die Gewichte ein und lernt nur eine niedrigrangige Korrektur ΔW = BA — wenige Millionen statt Milliarden Parameter, auf einer einzelnen GPU machbar.
            """,
                fm("LoRA", "W′ = W + (α/r) B A,  B ∈ ℝ^{d×r}, A ∈ ℝ^{r×k}, r ≪ d"),
                fm("DPO-Verlust", "L = −ln σ(β[ln(π(y_w)/π_ref(y_w)) − ln(π(y_l)/π_ref(y_l))])")
            )
        ),
        chapter(
            "ai_inference", T, 3, "Inferenz: Sampling, KV-Cache, Quantisierung",
            "Temperatur, Top-k/Top-p, KV-Cache, Prefill vs. Decode, Quantisierung, Speicherbedarf — wie Maximus auf dem Handy läuft.",
            emptyList(),
            sec("Sampling", """
                Das Modell liefert eine Wahrscheinlichkeitsverteilung über das nächste Token. Greedy nimmt immer das wahrscheinlichste (deterministisch, neigt zu Wiederholungen). Temperatur T skaliert die Logits: T < 1 schärft, T > 1 glättet. Top-k beschränkt auf die k wahrscheinlichsten, Top-p (Nucleus) auf die kleinste Menge mit kumulierter Wahrscheinlichkeit ≥ p. Für Code und Mathe niedrige Temperatur, für kreatives Schreiben höhere.
            """,
                fm("Temperatur", "pₖ = e^{zₖ/T}/Σⱼ e^{zⱼ/T}"),
                fm("Top-p", "kleinste Menge V_p mit Σ_{k∈V_p} pₖ ≥ p")
            ),
            sec("Prefill, Decode und KV-Cache", """
                Prefill: Der gesamte Prompt wird parallel verarbeitet (rechenintensiv, schnell pro Token). Decode: Jedes neue Token braucht einen eigenen Vorwärtsdurchlauf; um nicht alle früheren Token neu zu berechnen, speichert man ihre Keys und Values im KV-Cache. Decoding ist speicherbandbreitenbegrenzt: Pro Token müssen alle Gewichte einmal aus dem Speicher gelesen werden. Deshalb: Tokens/s ≈ Bandbreite / Modellgröße in Byte.
            """,
                fm("KV-Cache-Speicher", "M_KV = 2 · n_layer · n_kv_heads · d_head · n_ctx · Bytes"),
                fm("Decode-Obergrenze", "Token/s ≲ Speicherbandbreite / Modellgröße"),
                fm("Grouped-Query Attention", "n_kv_heads < n_heads ⇒ kleinerer KV-Cache")
            ),
            sec("Quantisierung", """
                Gewichte in 8 oder 4 Bit statt 16 Bit: 2–4× weniger Speicher und Bandbreite, meist mit kleinem Qualitätsverlust. Techniken: gruppenweise Skalierung, Kalibrierung auf Beispieldaten (GPTQ, AWQ), quantisierungsbewusstes Training. Ein 2-Milliarden-Parameter-Modell in int4 braucht etwa 1 GB für die Gewichte — so passt es auf ein Smartphone.
            """,
                fm("Gewichtsspeicher", "M ≈ N · Bits/8"),
                fm("Lineare Quantisierung", "w_q = round(w/s),  w ≈ s · w_q")
            ),
            sec("Grenzen", """
                Halluzinationen: Das Modell erzeugt plausible, aber falsche Aussagen, weil es Wahrscheinlichkeiten von Text optimiert, nicht Wahrheit. Gegenmittel: Quellen mitgeben (RAG), Werkzeuge für exakte Rechnungen (wie die Werkzeuge in Maximus), niedrige Temperatur für Fakten, Unsicherheit erfragen und Ergebnisse prüfen.
            """)
        )
    ) + course(
        "AI Engineering in der Praxis",
        chapter(
            "ai_rag_agents", T, 3, "RAG, Werkzeuge und Agenten",
            "Prompting, Retrieval-Augmented Generation, Chunking, hybride Suche, Funktionsaufrufe, Agentenschleifen, Prompt-Injection.",
            emptyList(),
            sec("Prompting", """
                Ein guter Prompt nennt Rolle, Aufgabe, Kontext, Format und Beispiele (Few-Shot). Schrittweises Denken (Chain-of-Thought) verbessert Rechen- und Logikaufgaben. Systematisch vorgehen: Prompts wie Code versionieren und gegen eine Testmenge von Beispielen bewerten, nicht nach Gefühl.
            """),
            sec("Retrieval-Augmented Generation (RAG)", """
                Statt Wissen in den Gewichten zu suchen, holt man passende Textstücke aus einer Wissensbasis und gibt sie dem Modell im Prompt mit. Pipeline: Dokumente in Abschnitte (Chunks) zerlegen → Embeddings berechnen → in einem Vektorindex speichern → zur Frage die ähnlichsten Chunks suchen (oft hybrid: BM25-Schlüsselwortsuche + Vektorsuche, danach Reranking) → Antwort mit Quellenangaben erzeugen. Maximus nutzt BM25 über das Kompendium genau so.
            """,
                fm("BM25-Term", "idf(t) · f(t,D)(k₁ + 1)/(f(t,D) + k₁(1 − b + b|D|/avgdl))"),
                fm("Reciprocal Rank Fusion", "score(d) = Σ_r 1/(k + rang_r(d))")
            ),
            sec("Werkzeuge und Agenten", """
                Beim Funktionsaufruf (Tool Use) gibt das Modell strukturierte Aufrufe aus (z. B. JSON), die Software ausführt; das Ergebnis geht zurück ins Gespräch. Ein Agent wiederholt Planen → Handeln → Beobachten, bis die Aufgabe erledigt ist. Erfolgsfaktoren: wenige, klar beschriebene Werkzeuge, begrenzte Schrittzahl, Protokollierung jedes Schritts, menschliche Bestätigung bei folgenreichen Aktionen.

                Sicherheit: Prompt-Injection — Text aus Webseiten, Dokumenten oder Werkzeugausgaben kann Anweisungen enthalten, die das Modell befolgt. Gegenmittel: fremde Inhalte als Daten markieren, Rechte minimal halten, kritische Aktionen bestätigen lassen.
            """)
        ),
        chapter(
            "ai_mlops", T, 2, "Evaluation, Betrieb und Verantwortung",
            "Evaluation, Benchmarks, Datenpipelines, Serving, Latenz und Kosten, On-Device-AI, Monitoring, Datenschutz.",
            emptyList(),
            sec("Evaluation zuerst", """
                Ohne Messung kein Fortschritt: Lege früh eine Evaluationsmenge an, die echte Nutzungsfälle abdeckt, mit klaren Bewertungskriterien. Automatische Metriken (Genauigkeit, exakte Übereinstimmung, Tests für Code), Modell-als-Richter mit Stichproben-Kontrolle durch Menschen, und A/B-Tests im Betrieb. Öffentliche Benchmarks sind nützlich, aber oft in Trainingsdaten enthalten (Kontamination).
            """),
            sec("Betrieb", """
                Serving-Kennzahlen: Zeit bis zum ersten Token (TTFT), Tokens pro Sekunde, Durchsatz bei vielen Nutzern (Batching, kontinuierliches Batching), Kosten pro Million Token. Datenpipelines müssen versioniert und reproduzierbar sein; Experimente werden protokolliert (Hyperparameter, Code-Stand, Daten-Version, Ergebnisse). Im Betrieb ändert sich die Datenverteilung (Drift) — Monitoring erkennt das.
            """,
                fm("Latenz einer Antwort", "t ≈ TTFT + n_Token / (Token/s)")
            ),
            sec("On-Device-AI", """
                Lokale Modelle (wie Maximus mit Gemma auf dem Galaxy) bieten Datenschutz, Offline-Betrieb und keine laufenden Kosten; dafür begrenzen Speicher, Bandbreite und Akku die Modellgröße. Werkzeuge: quantisierte Modelle (int4/int8), optimierte Laufzeiten (MediaPipe/LiteRT, llama.cpp, ExecuTorch), KV-Cache-Wiederverwendung zwischen Gesprächsrunden, kleine spezialisierte Modelle plus Werkzeuge statt eines riesigen Allzweckmodells.
            """),
            sec("Verantwortung", """
                Datenschutz (welche Daten verlassen das Gerät?), Fairness (verzerrte Trainingsdaten erzeugen verzerrte Entscheidungen), Transparenz (Grenzen offenlegen), Sicherheit (Missbrauch, Prompt-Injection), Urheberrecht und Lizenzen der Daten und Modelle. In der EU regelt der AI Act Pflichten nach Risikoklassen.
            """)
        )
    )
}

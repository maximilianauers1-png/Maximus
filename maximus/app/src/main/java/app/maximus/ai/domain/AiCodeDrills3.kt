package app.maximus.ai.domain

/**
 * „Code-Werkstatt“: additional code questions across all thirteen lessons — predicting outputs, finding
 * the bug, choosing the right line — so that every tournament has enough code to fight with.
 */
internal object AiCodeDrills3 {
    val questions: List<AiQuestion> = quiz("cw") {
        // ---------------- Lektion 1 ----------------
        on("ml_linreg_code")
        code(2, "Was ist an dieser Bewertung falsch?", """
            model.fit(X_train, y_train)
            print(r2_score(y_train, model.predict(X_train)))
        """, "R² wird auf den Trainingsdaten berechnet; aussagekräftig ist nur der Wert auf ungesehenen Testdaten", "r2_score erwartet zuerst die Vorhersage", "predict braucht y", "Nichts",
            why = "Mehr Merkmale erhöhen R² auf Trainingsdaten immer.")
        on("ml_polyreg")
        codeOut(2, "Wie viele Spalten erzeugt der Code?", """
            PolynomialFeatures(degree=2, include_bias=False).fit_transform(np.zeros((5, 3))).shape[1]
        """, "9", "10", "6", "3",
            why = "Mit Bias wären es C(5, 2) = 10; ohne die Spalte aus Einsen 9 (3 linear, 3 Quadrate, 3 Produkte).")
        on("ml_pipelines")
        codeOut(2, "Was gibt der Code aus?", """
            pipe = Pipeline([("sc", StandardScaler()), ("clf", LogisticRegression())])
            print(list(pipe.named_steps))
        """, "['sc', 'clf']", "['StandardScaler', 'LogisticRegression']", "[0, 1]", "['pipe']",
            why = "named_steps ist ein Wörterbuch mit den Schrittnamen als Schlüssel.")
        on("ev_metrics")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            from sklearn.metrics import f1_score
            print(round(f1_score([1, 1, 0, 0], [1, 0, 1, 0]), 2))
        """, "0.5", "0.75", "0.25", "1.0",
            why = "TP = 1, FP = 1, FN = 1 → P = R = 0,5 → F1 = 0,5.")
        on("alg_trees")
        code(2, "Was zeigt feature_importances_ hier?", """
            tree = DecisionTreeClassifier().fit(X, y)
            print(sorted(zip(tree.feature_importances_, X.columns), reverse=True)[:3])
        """, "Die drei Merkmale, die die Unreinheit im Baum am stärksten gesenkt haben", "Die drei häufigsten Werte", "Die drei Klassen", "Die Tiefe des Baums",
            why = "Impurity-basierte Wichtigkeit; Permutation Importance ist robuster gegen viele Ausprägungen.")

        // ---------------- Lektion 2 ----------------
        on("dl_forward")
        codeOut(2, "Was gibt der Code aus?", """
            import numpy as np
            relu = lambda z: np.maximum(0, z)
            W = np.array([[1, -1], [2, 0]]); x = np.array([1, 3])
            print(relu(W @ x))
        """, "[0 2]", "[-2 2]", "[1 3]", "[2 0]",
            why = "W @ x = (1 − 3, 2 + 0) = (−2, 2); ReLU macht daraus (0, 2).")
        on("dl_vanishing")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            print(round(0.25 ** 10, 8))
        """, "9.5e-07", "0.0025", "2.5", "0.025",
            why = "Zehn Sigmoid-Ableitungen von höchstens 1/4 hintereinander: (1/4)¹⁰ ≈ 10⁻⁶ – der Gradient verschwindet.")
        on("keras_classification")
        code(2, "Was passiert bei diesem Aufruf, wenn y_train_int Ganzzahl-Labels 0–9 enthält?", """
            model.compile(optimizer="adam", loss="categorical_crossentropy")
            model.fit(X_train, y_train_int)
        """, "Keras meldet einen Formfehler, weil categorical_crossentropy One-Hot-Labels der Form (n, 10) erwartet", "Alles korrekt", "Adam passt das automatisch an", "Keras wandelt die Labels stillschweigend um",
            why = "Für Ganzzahl-Labels sparse_categorical_crossentropy verwenden.")
        on("dl_cnn")
        codeOut(2, "Wie viele Parameter hat diese Schicht?", """
            layers.Conv2D(32, (3, 3), input_shape=(28, 28, 1))
        """, "320", "288", "9248", "32",
            why = "32 · (3·3·1 + 1) = 320.")

        // ---------------- Lektion 3 ----------------
        on("tf_basics")
        codeOut(1, "Was gibt der Code aus?", """
            x = tf.constant([[1, 2], [3, 4]])
            print(tf.reduce_sum(x, axis=0).numpy())
        """, "[4 6]", "[3 7]", "10", "[1 2 3 4]",
            why = "axis=0 summiert über die Zeilen: Spaltensummen 1 + 3 und 2 + 4.")
        on("keras_functional")
        code(2, "Was ist hier falsch?", """
            inputs = keras.Input(shape=(10,))
            x = layers.Dense(8)(inputs)
            model = keras.Model(inputs=x, outputs=inputs)
        """, "Eingabe und Ausgabe sind vertauscht – inputs muss der Eingang, x (oder später) der Ausgang sein", "Dense braucht eine Aktivierung", "Input braucht eine Batchgröße", "Nichts",
            why = "keras.Model(inputs=inputs, outputs=x).")
        on("keras_custom_training")
        codeOut(2, "Was gibt der Code aus?", """
            acc = keras.metrics.SparseCategoricalAccuracy()
            acc.update_state([0, 1, 2], [[0.9, 0.1, 0.0], [0.2, 0.7, 0.1], [0.5, 0.4, 0.1]])
            print(float(acc.result()))
        """, "0.6666666865348816", "1.0", "0.3333333432674408", "0.0",
            why = "Vorhersagen 0, 1, 0 gegen Labels 0, 1, 2: zwei von drei richtig (float32).")
        on("keras_optimize")
        codeOut(2, "Wie groß wird das Modell etwa?", """
            params = 25_600_000
            print(params * 4 / 1e6, "MB in float32;", params * 1 / 1e6, "MB in int8")
        """, "102.4 MB in float32; 25.6 MB in int8", "25.6 MB in float32; 102.4 MB in int8", "51.2 MB in float32; 12.8 MB in int8", "204.8 MB in float32; 51.2 MB in int8",
            why = "ResNet-50: 4 Byte bzw. 1 Byte je Parameter.")
        on("rl_qlearning")
        codeOut(2, "Welche Aktion wählt die gierige Politik?", """
            Q = np.array([[0.1, 0.5, 0.2], [0.0, 0.0, 0.9]])
            print(Q[0].argmax(), Q[1].argmax())
        """, "1 2", "0 0", "2 1", "1 0",
            why = "In Zustand 0 hat Aktion 1 den höchsten Q-Wert, in Zustand 1 Aktion 2.")

        // ---------------- Lektion 4 ----------------
        on("pt_tensors")
        codeOut(2, "Was gibt der Code aus?", """
            t = torch.tensor([[1, 2, 3], [4, 5, 6]])
            print(t[:, 1:].sum().item())
        """, "16", "21", "9", "6",
            why = "Spalten 1 und 2: 2 + 3 + 5 + 6 = 16.")
        on("pt_autograd")
        codeOut(2, "Was gibt der Code aus?", """
            x = torch.tensor([1.0, 2.0, 3.0], requires_grad=True)
            (x ** 2).sum().backward()
            print(x.grad)
        """, "tensor([2., 4., 6.])", "tensor([1., 4., 9.])", "tensor(14.)", "tensor([1., 1., 1.])",
            why = "∂(Σxᵢ²)/∂xᵢ = 2xᵢ.")
        on("pt_linreg")
        code(2, "Warum stagniert dieses Training?", """
            for epoch in range(100):
                loss = criterion(model(X), Y)
                loss.backward()
                optimizer.step()
        """, "optimizer.zero_grad() fehlt; die Gradienten summieren sich auf und die Schritte werden falsch", "backward ist falsch geschrieben", "criterion braucht die Gewichte", "100 Epochen sind zu wenig",
            why = "Nullen vor jedem backward ist Pflicht.")
        on("pt_logreg")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            print(round(torch.nn.BCELoss()(torch.tensor([0.9]), torch.tensor([1.0])).item(), 3))
        """, "0.105", "0.9", "0.1", "2.303",
            why = "−ln 0,9 ≈ 0,105. Bei p = 0,1 wären es 2,303.")
        on("pt_sgd")
        code(3, "Welche Wirkung hat diese Zeile im Optimierer?", """
            optim.SGD(model.parameters(), lr=0.01, weight_decay=1e-4)
        """, "L2-Regularisierung: Gewichte werden bei jedem Schritt leicht Richtung null gezogen", "Die Lernrate fällt pro Schritt", "Momentum wird aktiviert", "Gradienten werden geclippt",
            why = "Bei SGD ist weight_decay äquivalent zu einer L2-Strafe ½λ‖θ‖².")

        // ---------------- Lektion 5 ----------------
        on("pt_softmax")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            z = torch.tensor([[2.0, 0.0, 0.0]])
            print(round(nn.CrossEntropyLoss()(z, torch.tensor([0])).item(), 3))
        """, "0.24", "0.0", "1.099", "2.0",
            why = "p₀ = e²/(e² + 2) ≈ 0,787; −ln 0,787 = ln(1 + 2e⁻²) ≈ 0,240.")
        on("pt_dropout")
        code(2, "Was ist an dieser Auswertung falsch?", """
            model.train()
            with torch.no_grad():
                acc = (model(X_val).argmax(1) == y_val).float().mean()
        """, "Das Modell ist im train-Modus; Dropout und BatchNorm verfälschen die Auswertung – model.eval() fehlt", "no_grad ist falsch", "argmax braucht dim=0", "Nichts",
            why = "no_grad und eval gehören bei der Auswertung zusammen.")
        on("pt_cnn")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            x = torch.randn(1, 3, 64, 64)
            print(nn.Conv2d(3, 8, kernel_size=3, stride=2, padding=1)(x).shape)
        """, "torch.Size([1, 8, 32, 32])", "torch.Size([1, 8, 64, 64])", "torch.Size([1, 3, 32, 32])", "torch.Size([1, 8, 31, 31])",
            why = "⌊(64 + 2 − 3)/2⌋ + 1 = 32.")
        on("pt_momentum_bn")
        codeOut(2, "Was ergibt die normierte Ausgabe im Training (γ = 1, β = 0)?", """
            bn = nn.BatchNorm1d(1)
            print(bn(torch.tensor([[1.0], [3.0]])).detach().view(-1))
        """, "tensor([-1.0000,  1.0000])", "tensor([1., 3.])", "tensor([0., 0.])", "tensor([-2.,  2.])",
            why = "Mittelwert 2, Varianz 1 (biased): (1 − 2)/1 = −1, (3 − 2)/1 = 1 (bis auf ε).")

        // ---------------- Lektion 6 ----------------
        on("cap_keras_cnn")
        code(2, "Warum ist dieses Modell für zwei Klassen inkonsistent?", """
            layers.Dense(2, activation="sigmoid")
            model.compile(loss="sparse_categorical_crossentropy")
        """, "Zwei unabhängige Sigmoide passen nicht zur kategorialen Kreuzentropie – entweder Dense(1, sigmoid) + binary_crossentropy oder Dense(2, softmax)", "Sigmoid gibt es nicht in Keras", "Der Verlust ist zu klein", "Alles korrekt",
            why = "Ausgabe und Verlust müssen zusammenpassen.")
        on("cap_vit")
        codeOut(2, "Wie viele Tokens (mit [CLS]) erzeugt ein ViT mit Patchgröße 32 für 224 × 224?", """
            print((224 // 32) ** 2 + 1)
        """, "50", "197", "49", "65",
            why = "7 × 7 = 49 Patches plus Klassentoken.")
        on("cap_hybrid")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.metrics import roc_auc_score
            print(roc_auc_score([0, 0, 1, 1], [0.1, 0.4, 0.35, 0.8]))
        """, "0.75", "1.0", "0.5", "0.25",
            why = "Von 4 Positiv-Negativ-Paaren sind 3 richtig geordnet (0,35 < 0,4 ist falsch).")

        // ---------------- Lektion 7 ----------------
        on("tok_basics")
        codeOut(1, "Was gibt der Code aus?", """
            vocab = {"<pad>": 0, "<unk>": 1, "der": 2, "ritter": 3}
            print([vocab.get(w, 1) for w in "der ritter reitet".split()])
        """, "[2, 3, 1]", "[2, 3]", "[2, 3, 0]", "[0, 1, 2]",
            why = "Unbekanntes „reitet“ wird zu <unk> = 1.")
        on("nlp_dataloader")
        code(2, "Warum stürzt der Standard-DataLoader hier ab?", """
            DataLoader([torch.tensor([1, 2]), torch.tensor([3, 4, 5])], batch_size=2)
            next(iter(loader))
        """, "Die Folgen sind unterschiedlich lang; ohne collate_fn mit Padding lassen sie sich nicht stapeln", "batch_size muss 1 sein", "Tensoren sind nicht erlaubt", "Er stürzt nicht ab",
            why = "Standard-Collate ruft torch.stack auf – das braucht gleiche Formen.")
        on("gen_libs")
        codeOut(2, "Was gibt der Code aus?", """
            enc = tok("Hallo Welt", return_tensors="pt")
            print(list(enc.keys()))   # BERT-Tokenizer
        """, "['input_ids', 'token_type_ids', 'attention_mask']", "['tokens']", "['ids', 'mask']", "['text']",
            why = "BERT-Tokenizer liefern zusätzlich token_type_ids für Satzpaare.")

        // ---------------- Lektion 8 ----------------
        on("nlp_onehot")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.feature_extraction.text import CountVectorizer
            cv = CountVectorizer().fit(["ritter reitet", "ritter kämpft"])
            print(sorted(cv.vocabulary_))
        """, "['kämpft', 'reitet', 'ritter']", "['ritter', 'reitet', 'kämpft']", "['ritter']", "['ritter reitet', 'ritter kämpft']",
            why = "Drei verschiedene Wörter bilden das Vokabular.")
        on("nlp_word2vec")
        codeOut(2, "Welches Wort liegt bei diesen Vektoren näher an ‚koenig‘?", """
            v = {"koenig": np.array([1.0, 1.0]), "koenigin": np.array([0.9, 1.1]), "ruebe": np.array([-1.0, 0.2])}
            cos = lambda a, b: a @ b / (np.linalg.norm(a) * np.linalg.norm(b))
            print(max(["koenigin", "ruebe"], key=lambda w: cos(v["koenig"], v[w])))
        """, "koenigin", "ruebe", "koenig", "beide gleich",
            why = "Kosinus ≈ 0,995 gegen ≈ −0,55.")
        on("nlp_seq2seq")
        code(2, "Welche Dekodierung setzt dieser Code um?", """
            tok = BOS
            while tok != EOS:
                logits, h = decoder(tok, h)
                tok = logits.argmax(-1)
        """, "Greedy-Dekodierung: in jedem Schritt das wahrscheinlichste Token", "Beam Search", "Top-p-Sampling", "Teacher Forcing",
            why = "Ohne Längenbegrenzung kann die Schleife zudem endlos laufen – max_len gehört dazu.")
        on("nlp_metrics")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            import math
            print(round(math.exp(2.0), 2))   # mittlerer Verlust pro Token 2,0 Nats
        """, "7.39", "2.0", "4.0", "0.14",
            why = "Perplexität = exp(Verlust).")

        // ---------------- Lektion 9 ----------------
        on("tr_attention")
        codeOut(2, "Was gibt der Code aus?", """
            w = torch.softmax(torch.tensor([0.0, 0.0, float("-inf")]), dim=0)
            print(w)
        """, "tensor([0.5000, 0.5000, 0.0000])", "tensor([0.3333, 0.3333, 0.3333])", "tensor([nan, nan, nan])", "tensor([1., 1., 0.])",
            why = "−∞ bekommt Gewicht 0; der Rest teilt sich gleichmäßig auf.")
        on("tr_multihead")
        code(2, "Warum schlägt der Aufruf fehl?", """
            nn.MultiheadAttention(embed_dim=100, num_heads=8)
        """, "embed_dim muss durch num_heads teilbar sein; 100/8 ist keine ganze Zahl", "100 ist zu klein", "num_heads muss ungerade sein", "Er schlägt nicht fehl",
            why = "Jeder Kopf bekommt embed_dim/num_heads Dimensionen.")
        on("tr_gpt")
        codeOut(2, "Welche Labels sieht das Modell für die Eingabe [5, 9, 2, 7]?", """
            ids = [5, 9, 2, 7]
            print(ids[:-1], "→", ids[1:])
        """, "[5, 9, 2] → [9, 2, 7]", "[5, 9, 2, 7] → [5, 9, 2, 7]", "[9, 2, 7] → [5, 9, 2]", "[5] → [7]",
            why = "Jede Position sagt das Folgetoken vorher.")
        on("tr_bert")
        codeOut(2, "Wie viele Tokens werden bei 512 Tokens im Mittel maskiert, wie viele davon durch [MASK]?", """
            n = 512
            print(round(0.15 * n), round(0.15 * n * 0.8))
        """, "77 61", "51 41", "77 77", "102 82",
            why = "15 % ≈ 77 Tokens, davon 80 % ≈ 61 als [MASK].")
        on("tr_posenc")
        codeOut(2, "Was ist PE(t = 0) für die ersten vier Dimensionen?", """
            print(positional_encoding(1, 4)[0])
        """, "tensor([0., 1., 0., 1.])", "tensor([1., 0., 1., 0.])", "tensor([0., 0., 0., 0.])", "tensor([1., 1., 1., 1.])",
            why = "sin(0) = 0 in geraden, cos(0) = 1 in ungeraden Dimensionen.")

        // ---------------- Lektion 10 ----------------
        on("ft_lora")
        codeOut(2, "Wie viele LoRA-Parameter entstehen für eine 768 × 768-Projektion mit r = 4?", """
            d = k = 768; r = 4
            print(r * (d + k))
        """, "6144", "589824", "3072", "768",
            why = "r(d + k) = 4 · 1536 = 6144 statt 589 824 – Faktor 96.")
        on("ft_peft")
        code(2, "Was gibt print_trainable_parameters typischerweise aus?", """
            model = get_peft_model(base, LoraConfig(r=8, target_modules=["q_proj", "v_proj"], task_type="CAUSAL_LM"))
            model.print_trainable_parameters()
        """, "Die Zahl trainierbarer Parameter, die Gesamtzahl und den Anteil – oft unter 1 %", "Die Accuracy", "Die Lernrate", "Die Liste aller Schichten",
            why = "Ein schneller Plausibilitätscheck nach dem Einbinden von PEFT.")
        on("ft_full")
        code(2, "Was fehlt in compute_metrics?", """
            def compute_metrics(eval_pred):
                logits, labels = eval_pred
                return {"accuracy": (logits == labels).mean()}
        """, "Das argmax über die Logits: preds = logits.argmax(-1)", "Ein Softmax über die Labels", "Ein Aufruf von backward", "Nichts",
            why = "Logits sind Zahlen pro Klasse, keine Klassenindizes.")
        on("ft_hf_pt")
        codeOut(2, "Wie viel Speicher brauchen die Gewichte eines 1-Mrd.-Modells in float32?", """
            print(1e9 * 4 / 1e9, "GB")
        """, "4.0 GB", "1.0 GB", "2.0 GB", "8.0 GB",
            why = "4 Byte je Parameter; in bf16 2 GB.")

        // ---------------- Lektion 11 ----------------
        on("aft_dpo")
        codeOut(3, "Wie groß ist der DPO-Verlust, wenn Politik und Referenz identisch sind?", """
            logits = 0.1 * ((0.0) - (0.0))
            print(round(-math.log(1 / (1 + math.exp(-logits))), 3))
        """, "0.693", "0.0", "1.0", "0.1",
            why = "Zu Beginn ist π_θ = π_ref: −ln σ(0) = ln 2.")
        on("aft_reward")
        codeOut(2, "Was ergibt die Präferenzwahrscheinlichkeit bei r_w − r_l = 2 (gerundet)?", """
            print(round(1 / (1 + math.exp(-2)), 3))
        """, "0.881", "0.5", "0.119", "2.0",
            why = "σ(2) ≈ 0,881.")
        on("aft_rlhf")
        codeOut(3, "Was gibt der geclippte PPO-Term aus?", """
            ratio, adv, eps = 1.5, 2.0, 0.2
            print(min(ratio * adv, max(min(ratio, 1 + eps), 1 - eps) * adv))
        """, "2.4", "3.0", "2.0", "1.6",
            why = "clip(1,5; 0,8; 1,2) = 1,2; min(3,0; 2,4) = 2,4.")
        on("aft_instruct")
        codeOut(2, "Wie viele Tokens tragen zum Verlust bei?", """
            labels = [-100, -100, -100, 17, 42, 9]
            print(sum(1 for l in labels if l != -100))
        """, "3", "6", "0", "4",
            why = "Nur die Antwort-Tokens zählen; −100 wird ignoriert.")

        // ---------------- Lektion 12 ----------------
        on("rag_dpr")
        code(2, "Warum normiert man hier die Vektoren?", """
            faiss.normalize_L2(P); faiss.normalize_L2(q)
            index = faiss.IndexFlatIP(P.shape[1])
        """, "Damit das Skalarprodukt (Inner Product) der Kosinusähnlichkeit entspricht", "Um Speicher zu sparen", "Weil FAISS nur positive Zahlen kann", "Um die Dimension zu halbieren",
            why = "Bei Einheitsvektoren gilt qᵀp = cos(q, p).")
        on("lc_lcel")
        code(2, "Warum schlägt dieser Aufruf fehl?", """
            prompt = ChatPromptTemplate.from_template("Erkläre {thema} für {publikum}.")
            (prompt | llm).invoke({"thema": "LoRA"})
        """, "Der Platzhalter publikum fehlt im Eingabewörterbuch", "LCEL kennt keine Vorlagen", "invoke braucht eine Liste", "llm muss vor prompt stehen",
            why = "Alle Variablen der Vorlage müssen gefüllt werden (oder per partial vorbelegt).")
        on("lc_agents")
        code(2, "Was ist an diesem Werkzeug riskant?", """
            @tool
            def run_shell(cmd: str) -> str:
                "Führt einen beliebigen Shell-Befehl aus."
                return subprocess.run(cmd, shell=True, capture_output=True, text=True).stdout
        """, "Ein Agent (oder eine Prompt-Injection) könnte beliebige Befehle ausführen – minimale Rechte und Bestätigung fehlen", "Es hat keinen Docstring", "Es gibt keinen Rückgabewert", "Nichts",
            why = "Werkzeuge mit minimalen Rechten; gefährliche Aktionen nur mit menschlicher Freigabe.")
        on("rag_pipeline")
        code(2, "Was verbessert diese Änderung am Prompt?", """
            prompt += "\nWenn der Kontext die Antwort nicht enthält, antworte: Das steht nicht in den Unterlagen."
        """, "Das Modell darf und soll Unwissen zugeben – Halluzinationen sinken", "Die Antwort wird länger", "Das Retrieval wird schneller", "Nichts",
            why = "Ein expliziter Ausweg verhindert erfundene Antworten.")

        // ---------------- Lektion 13 ----------------
        on("prj_splitters")
        codeOut(2, "Wie viele Chunks erzeugt dieser Splitter für einen Text ohne Leerzeichen der Länge 25?", """
            s = RecursiveCharacterTextSplitter(chunk_size=10, chunk_overlap=0, separators=[""])
            print(len(s.split_text("a" * 25)))
        """, "3", "2", "25", "1",
            why = "10 + 10 + 5 Zeichen.")
        on("prj_retrievers")
        codeOut(2, "Was gibt der MMR-Score aus?", """
            lam, sim_q, max_sim_selected = 0.5, 0.9, 0.95
            print(round(lam * sim_q - (1 - lam) * max_sim_selected, 3))
        """, "-0.025", "0.9", "0.45", "0.925",
            why = "Sehr relevant, aber fast ein Duplikat eines gewählten Treffers – MMR bestraft das.")
        on("prj_embed_store")
        code(2, "Warum findet die Suche nach dem Neustart nichts mehr?", """
            db = Chroma.from_documents(chunks, emb)          # ohne persist_directory
            # … Programm neu gestartet …
            db = Chroma(embedding_function=emb)
        """, "Der Index lag nur im Speicher; ohne persist_directory ist er nach dem Neustart weg", "Chroma löscht immer alles", "emb hat sich geändert", "Die Chunks sind zu groß",
            why = "Mit persist_directory schreiben und beim Laden denselben Ordner angeben.")
        on("prj_qabot")
        codeOut(2, "Auf welchem Port läuft die Gradio-App?", """
            app.launch(server_name="0.0.0.0", server_port=7860)
        """, "7860", "8080", "0", "443",
            why = "7860 ist Gradios Standardport; 0.0.0.0 macht die App im Netzwerk erreichbar.")
        on("prj_loaders")
        codeOut(2, "Was gibt der Code aus?", """
            docs = CSVLoader("ritter.csv").load()      # Datei mit Kopfzeile und 4 Datenzeilen
            print(len(docs))
        """, "4", "5", "1", "0",
            why = "Ein Document pro Datenzeile; die Kopfzeile liefert die Spaltennamen im Text.")
    }
}

package app.maximus.ai.domain

/**
 * Code drills and extra questions for the carried-over bites of Lektionen 2–13 (deep learning, RL,
 * engineering, LLMs, RAG).
 */
internal object AiCodeDrills2 {
    val questions: List<AiQuestion> = quiz("cd2") {
        on("dl_neuron")
        codeOut(1, "Was gibt das Perzeptron aus?", """
            import numpy as np
            w = np.array([0.5, -1.0]); b = 0.2; x = np.array([1.0, 0.1])
            print(int(w @ x + b > 0))
        """, "1", "0", "-1", "0.6",
            why = "0,5 − 0,1 + 0,2 = 0,6 > 0 → Ausgabe 1 (Stufenfunktion).")
        code(2, "Was macht diese Perzeptron-Lernregel?", """
            if y_hat != y:
                w = w + eta * (y - y_hat) * x
                b = b + eta * (y - y_hat)
        """, "Bei einem Fehler wird die Gerade in Richtung des falsch klassifizierten Punkts verschoben", "Sie macht nichts", "Sie normiert die Gewichte", "Sie berechnet den Gradienten der MSE",
            why = "Bei linear trennbaren Daten konvergiert das in endlich vielen Schritten (Novikoff).")

        on("ai_nn")
        codeOut(2, "Wie viele Parameter hat dieses Netz?", """
            nn.Sequential(nn.Linear(4, 16), nn.ReLU(), nn.Linear(16, 16), nn.ReLU(), nn.Linear(16, 3))
        """, "403", "339", "384", "51",
            why = "4·16 + 16 = 80; 16·16 + 16 = 272; 16·3 + 3 = 51; Summe 403.")
        code(2, "Warum lernt dieses Netz nicht mehr als eine lineare Abbildung?", """
            nn.Sequential(nn.Linear(10, 64), nn.Linear(64, 64), nn.Linear(64, 1))
        """, "Ohne Aktivierungen ist die Verkettung linearer Schichten wieder linear", "Weil 64 zu klein ist", "Weil der Bias fehlt", "Weil nn.Linear keine Gradienten hat",
            why = "W₃W₂W₁x + b′ – eine einzige lineare Schicht.")
        mc(2, "Warum ist Backpropagation so effizient?",
            "Alle Gradienten kosten zusammen nur etwa so viel wie ein bis zwei Vorwärtsdurchläufe, unabhängig von der Parameterzahl", "Weil sie nur einen Gradienten berechnet", "Weil sie keine Ableitungen braucht", "Weil sie zufällig ist",
            why = "Reverse-Mode-Autodiff nutzt Zwischenergebnisse der Kettenregel mehrfach.")

        on("dl_backprop_numbers")
        codeOut(2, "Was ist dL/dw für L = (w·x − y)² bei w = 2, x = 3, y = 5?", """
            w, x, y = 2.0, 3.0, 5.0
            print(2 * (w * x - y) * x)
        """, "6.0", "2.0", "1.0", "12.0",
            why = "Kettenregel: 2(wx − y)·x = 2·1·3 = 6.")
        code(2, "Welche Ableitung berechnet diese Zeile in der Backpropagation?", """
            delta_hidden = (W2.T @ delta_out) * a1 * (1 - a1)
        """, "Den Fehler der versteckten Schicht: zurückprojizierter Ausgabefehler mal Sigmoid-Ableitung", "Den Verlust", "Die Lernrate", "Die Ausgabe des Netzes",
            why = "δ₁ = (W₂ᵀδ₂) ⊙ σ′(z₁), mit σ′ = a(1 − a).")

        on("dl_keras")
        codeOut(1, "Wie viele Parameter meldet model.summary()?", """
            model = keras.Sequential([keras.Input(shape=(20,)), layers.Dense(32, activation="relu"), layers.Dense(1)])
        """, "705", "672", "640", "33",
            why = "20·32 + 32 = 672 und 32·1 + 1 = 33.")
        line(1, "Welche Zeile startet das Training in Keras?",
            "model.fit(X_train, y_train, epochs=10, batch_size=32, validation_split=0.2)", "model.train(X_train)", "model.compile(X_train, y_train)", "keras.fit(model, X_train)",
            why = "compile konfiguriert, fit trainiert.")

        on("ai_arch")
        code(2, "Welche Architektur passt zu diesem Problem?", """
            # Eingabe: Satz als Folge von Wörtern, Ausgabe: Stimmung positiv/negativ
        """, "Ein Sequenzmodell (RNN/LSTM oder Transformer) über Worteinbettungen", "Ein CNN für Bilder", "k-Means", "Lineare Regression auf Pixeln",
            why = "Folgen brauchen Modelle, die Reihenfolge und Kontext verarbeiten.")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            emb = nn.Embedding(5000, 32)
            print(emb(torch.randint(0, 5000, (8, 20))).shape)
        """, "torch.Size([8, 20, 32])", "torch.Size([8, 32])", "torch.Size([20, 32])", "torch.Size([8, 5000])",
            why = "8 Sätze à 20 Tokens, je ein 32-dimensionaler Vektor.")
        mc(2, "Welche induktive Verzerrung bringt ein CNN mit?",
            "Lokalität und Verschiebungsäquivarianz durch geteilte Kerne", "Reihenfolge der Zeit", "Keine", "Globale Attention",
            why = "RNNs bringen Sequentialität mit, Transformer kaum Vorannahmen.")
        mc(2, "Wofür eignen sich Einbettungen (Embeddings)?",
            "Um diskrete Objekte wie Wörter, Nutzer oder Artikel als dichte Vektoren darzustellen, in denen Ähnliches nahe liegt", "Um Bilder zu komprimieren", "Um Gradienten zu speichern", "Nur für Zahlen",
            why = "Grundlage von NLP, Empfehlungssystemen und semantischer Suche.")

        on("dl_cnn")
        codeOut(2, "Welche Ausgabegröße hat Conv2D(16, 3) ohne Padding auf 32 × 32 × 3?", """
            layers.Conv2D(16, 3, padding="valid")   # Eingabe (32, 32, 3)
        """, "(30, 30, 16)", "(32, 32, 16)", "(30, 30, 3)", "(16, 16, 16)",
            why = "32 − 3 + 1 = 30; 16 Filter → 16 Kanäle.")
        code(2, "Was ist die Aufgabe dieser Schicht?", """
            layers.MaxPooling2D(pool_size=2)
        """, "Räumliche Größe halbieren und kleine Verschiebungen tolerieren", "Kanäle verdoppeln", "Die Daten normieren", "Klassen ausgeben",
            why = "Max-Pooling nimmt pro 2 × 2-Fenster das Maximum.")

        on("dl_rnn")
        codeOut(2, "Welche Form hat out?", """
            lstm = nn.LSTM(input_size=10, hidden_size=32, batch_first=True)
            out, (h, c) = lstm(torch.randn(4, 7, 10))
            print(out.shape)
        """, "torch.Size([4, 7, 32])", "torch.Size([4, 32])", "torch.Size([1, 4, 32])", "torch.Size([7, 4, 10])",
            why = "out enthält den verborgenen Zustand für jeden der 7 Zeitschritte; h nur den letzten.")
        code(2, "Wozu dienen die Gates eines LSTM?", """
            f = sigmoid(Wf @ [h, x]); i = sigmoid(Wi @ [h, x]); o = sigmoid(Wo @ [h, x])
            c = f * c + i * tanh(Wc @ [h, x])
        """, "Sie steuern, was der Zellzustand vergisst, aufnimmt und ausgibt – und lassen Gradienten über lange Zeit fließen", "Sie erhöhen die Lernrate", "Sie ersetzen die Einbettung", "Sie berechnen Attention",
            why = "Der additive Zellpfad c = f·c + … mildert verschwindende Gradienten.")
        mc(2, "Was unterscheidet ein GRU von einem LSTM?",
            "GRU hat nur zwei Gates und keinen separaten Zellzustand – weniger Parameter, oft ähnliche Leistung", "GRU hat keine Gates", "GRU ist bidirektional", "GRU ist ein CNN",
            why = "Update- und Reset-Gate statt Forget-, Input- und Output-Gate.")

        on("dl_transfer")
        code(2, "Was bewirkt include_top=False?", """
            base = keras.applications.ResNet50(weights="imagenet", include_top=False, input_shape=(224, 224, 3))
            base.trainable = False
        """, "Der ImageNet-Klassifikationskopf wird weggelassen; man setzt einen eigenen Kopf auf den eingefrorenen Merkmalsextraktor", "Das Modell wird ohne Gewichte geladen", "Nur die oberste Schicht wird trainiert", "Die Bilder werden nicht skaliert",
            why = "Klassisches Transfer Learning: Basis einfrieren, neuen Kopf trainieren.")
        line(2, "Welche Zeile setzt einen Kopf für 5 Klassen auf die eingefrorene Basis?",
            "outputs = layers.Dense(5, activation=\"softmax\")(layers.GlobalAveragePooling2D()(base.output))",
            "base.add(Dense(5))",
            "outputs = base(5)",
            "base.top = 5",
            why = "Pooling reduziert die Merkmalskarten auf einen Vektor, Dense mit Softmax klassifiziert.")
        mc(2, "Wann nimmt man beim Transfer Learning eine sehr kleine Lernrate?",
            "Beim Auftauen und Fine-Tunen der vortrainierten Schichten", "Beim Training des neuen Kopfs", "Nie", "Nur bei Textdaten",
            why = "Sonst zerstören große Gradienten die vortrainierten Merkmale.")
        mc(1, "Warum ist Transfer Learning bei wenig Daten so wirksam?",
            "Das vortrainierte Netz bringt allgemeine Merkmale mit; nur wenig muss neu gelernt werden", "Weil es keine Daten braucht", "Weil es kleiner ist", "Weil es keinen Optimierer braucht",
            why = "Merkmale wie Kanten und Texturen sind über viele Aufgaben hinweg nützlich.")

        on("dl_autoencoder")
        code(2, "Was lernt dieses Modell?", """
            enc = keras.Sequential([layers.Dense(32, activation="relu")])
            dec = keras.Sequential([layers.Dense(784, activation="sigmoid")])
            ae = keras.Sequential([keras.Input(shape=(784,)), enc, dec])
            ae.compile(optimizer="adam", loss="binary_crossentropy"); ae.fit(X, X, epochs=10)
        """, "Einen Autoencoder: die Eingabe über einen 32-dimensionalen Engpass zu rekonstruieren", "Eine Klassifikation in 32 Klassen", "Eine Regression auf Labels", "Ein GAN",
            why = "Ziel ist die Eingabe selbst: fit(X, X).")
        mc(2, "Wofür kann man den Rekonstruktionsfehler eines Autoencoders nutzen?",
            "Zur Anomalieerkennung: ungewöhnliche Eingaben werden schlecht rekonstruiert", "Als Lernrate", "Zur Tokenisierung", "Er ist nutzlos",
            why = "Das Modell hat nur „normale“ Daten gut zu komprimieren gelernt.")
        mc(3, "Was unterscheidet einen VAE von einem gewöhnlichen Autoencoder?",
            "Der Encoder gibt eine Verteilung (μ, σ) aus; ein KL-Term formt den latenten Raum, aus dem man sampeln kann", "Er hat keinen Decoder", "Er ist nicht differenzierbar", "Er braucht Labels",
            why = "Mit dem Reparametrisierungstrick z = μ + σ·ε bleibt alles differenzierbar.")
        codeOut(3, "Was berechnet diese Zeile?", """
            z = mu + torch.exp(0.5 * logvar) * torch.randn_like(mu)
        """, "Eine Stichprobe aus N(μ, σ²) über den Reparametrisierungstrick", "Den KL-Abstand", "Die Rekonstruktion", "Den Mittelwert",
            why = "Der Zufall steckt in ε, sodass Gradienten durch μ und σ fließen.")

        on("dl_generative")
        code(2, "Welche Rolle spielt D in diesem Trainingsschritt?", """
            d_loss = bce(D(real), ones) + bce(D(G(z).detach()), zeros)
            g_loss = bce(D(G(z)), ones)
        """, "D ist der Diskriminator: er soll echte von erzeugten Bildern unterscheiden; G versucht ihn zu täuschen", "D ist der Decoder eines VAE", "D ist der Datensatz", "D ist die Lernrate",
            why = "Das Minimax-Spiel des GAN; detach verhindert, dass der D-Schritt G aktualisiert.")
        mc(2, "Was lernt ein Diffusionsmodell?",
            "Das Rauschen vorherzusagen, das einem Bild in einem bestimmten Schritt hinzugefügt wurde – und es so schrittweise zu entfernen", "Bilder zu klassifizieren", "Text zu übersetzen", "Den Diskriminator zu täuschen",
            why = "Verlust ‖ε − ε_θ(x_t, t)‖²; erzeugt wird durch viele Entrauschungsschritte.")

        on("rl_basics")
        codeOut(2, "Wie groß ist der diskontierte Return?", """
            rewards = [1, 1, 1]; gamma = 0.9
            print(sum(r * gamma ** t for t, r in enumerate(rewards)))
        """, "2.71", "3.0", "2.9", "0.729",
            why = "1 + 0,9 + 0,81 = 2,71.")
        code(2, "Was macht diese Zeile?", """
            a = env.action_space.sample() if random.random() < epsilon else int(np.argmax(Q[s]))
        """, "ε-gierige Aktionswahl: mit Wahrscheinlichkeit ε erkunden, sonst die beste bekannte Aktion nutzen", "Sie berechnet die Belohnung", "Sie setzt die Umgebung zurück", "Sie trainiert ein CNN",
            why = "Der Kompromiss zwischen Exploration und Exploitation.")

        on("rl_qlearning")
        codeOut(2, "Was ist der neue Q-Wert?", """
            q, r, gamma, alpha, max_next = 2.0, 1.0, 0.9, 0.5, 4.0
            print(round(q + alpha * (r + gamma * max_next - q), 2))
        """, "3.3", "4.6", "2.8", "2.0",
            why = "TD-Ziel r + γ·max Q = 1 + 0,9·4 = 4,6; TD-Fehler 4,6 − 2 = 2,6; neuer Wert 2 + 0,5·2,6 = 3,3.")
        line(2, "Welche Zeile ist das Q-Learning-Update?",
            "Q[s, a] += alpha * (r + gamma * Q[s2].max() - Q[s, a])",
            "Q[s, a] = r",
            "Q[s, a] += alpha * (r - gamma * Q[s, a])",
            "Q[s2].max() += r",
            why = "Off-Policy-TD-Update mit dem Maximum über die Folgeaktionen (Bellman-Optimalität).")
        mc(2, "Was unterscheidet SARSA von Q-Learning?",
            "SARSA nutzt den Q-Wert der tatsächlich gewählten nächsten Aktion (on-policy), Q-Learning das Maximum (off-policy)", "SARSA hat keine Belohnung", "Q-Learning braucht ein Modell der Umgebung", "Es gibt keinen Unterschied",
            why = "SARSA lernt die Werte der Politik, die es tatsächlich befolgt – vorsichtiger nahe an Abgründen.")
        mc(1, "Wofür steht γ (Diskontfaktor)?",
            "Wie stark zukünftige Belohnungen gegenüber sofortigen zählen", "Die Lernrate", "Die Explorationsrate", "Die Zahl der Episoden",
            why = "γ nahe 1: weitsichtig, γ nahe 0: kurzsichtig.")

        on("rl_deep")
        code(3, "Wozu dient das Zielnetz in DQN?", """
            target = r + gamma * target_net(s2).max(1).values * (1 - done)
            loss = F.smooth_l1_loss(q_net(s).gather(1, a), target.unsqueeze(1))
        """, "Ein langsam aktualisiertes Netz liefert stabile Zielwerte; sonst jagt das Netz seinem eigenen, ständig wandernden Ziel hinterher", "Es ersetzt den Replay Buffer", "Es wählt die Aktionen", "Es ist das Belohnungsmodell",
            why = "Zusammen mit Experience Replay machte das DQN auf Atari stabil (Mnih et al. 2015).")
        mc(2, "Was ist Experience Replay?",
            "Erfahrungen werden in einem Puffer gespeichert und zufällig gezogen – das bricht Korrelationen aufeinanderfolgender Schritte", "Das Wiederholen von Videos", "Das Speichern des besten Modells", "Ein Optimierer",
            why = "Zufällige Mini-Batches aus dem Puffer machen das Training stabiler und dateneffizienter.")
        mc(2, "Warum braucht man für Atari-Spiele ein neuronales Netz statt einer Q-Tabelle?",
            "Der Zustandsraum (Bildschirmpixel) ist riesig; das Netz verallgemeinert über ähnliche Zustände", "Weil Tabellen keine Zahlen speichern", "Weil Atari kein RL ist", "Aus Lizenzgründen",
            why = "Funktionsapproximation statt Nachschlagen.")
        code(2, "Was steht hinter (1 - done) im Zielwert?", """
            target = r + gamma * next_q * (1 - done)
        """, "Am Episodenende gibt es keinen Folgezustand; dann zählt nur die Belohnung", "Es halbiert den Zielwert", "Es ist ein Tippfehler", "Es erzwingt Exploration",
            why = "Für terminale Zustände ist der Wert der Zukunft null.")

        on("dl_pytorch")
        code(1, "Welche vier Zeilen bilden das Herz jeder PyTorch-Trainingsschleife?", """
            pred = model(x)
            loss = loss_fn(pred, y)
            optimizer.zero_grad(); loss.backward(); optimizer.step()
        """, "Vorwärts rechnen, Verlust, Gradienten löschen und berechnen, Gewichte aktualisieren", "Laden, speichern, drucken, beenden", "Tokenisieren, einbetten, sampeln, dekodieren", "Mischen, batchen, zählen, plotten",
            why = "Diese Schleife bleibt vom linearen Modell bis zum LLM gleich.")
        codeOut(1, "Was gibt der Code aus?", """
            x = torch.tensor([1.0, 2.0, 3.0])
            print(x.mean(), x.shape)
        """, "tensor(2.) torch.Size([3])", "2.0 (3,)", "tensor([2.]) torch.Size([1])", "tensor(6.) torch.Size([3])",
            why = "Reduktionen liefern 0D-Tensoren; .item() machte daraus eine Python-Zahl.")
        mc(2, "Was unterscheidet PyTorch historisch von TensorFlow 1?",
            "Dynamische Graphen: Der Rechengraph entsteht beim Ausführen (define-by-run)", "PyTorch kann keine GPUs", "PyTorch hat keine Autograd", "TensorFlow 1 war dynamisch",
            why = "TensorFlow 2 hat mit Eager Execution nachgezogen.")

        on("ai_training")
        code(2, "Was macht dieser Lernratenplan?", """
            sched = torch.optim.lr_scheduler.CosineAnnealingLR(opt, T_max=100)
        """, "Er senkt die Lernrate über 100 Schritte entlang einer halben Kosinuskurve", "Er erhöht die Lernrate linear", "Er hält sie konstant", "Er setzt sie zufällig",
            why = "ηₜ = η_min + ½(η_max − η_min)(1 + cos(πt/T)).")
        code(2, "Was verhindert dieser Callback-Ersatz in PyTorch?", """
            if val_loss < best - 1e-4: best, wait = val_loss, 0; torch.save(model.state_dict(), "best.pt")
            else:
                wait += 1
                if wait >= patience: break
        """, "Überanpassung durch zu langes Training – es ist Early Stopping", "Explodierende Gradienten", "Datenlecks", "Zu kleine Batches",
            why = "Gespeichert wird das Modell mit dem besten Validierungsverlust.")
        mc(2, "Wozu dient Gradientenakkumulation?",
            "Große effektive Batches bei kleinem Speicher: Gradienten mehrerer Mini-Batches summieren, dann einen Schritt machen", "Gradienten zu löschen", "Die Lernrate zu erhöhen", "Daten zu augmentieren",
            why = "Man ruft optimizer.step() nur alle k Batches auf (und teilt den Verlust durch k).")

        on("eng_deploy")
        code(2, "Was stellt dieser Code bereit?", """
            from fastapi import FastAPI
            app = FastAPI()
            @app.post("/predict")
            def predict(features: list[float]):
                return {"prediction": float(model.predict([features])[0])}
        """, "Einen REST-Endpunkt, der Vorhersagen des Modells per HTTP liefert", "Ein Trainingsskript", "Eine Datenbank", "Eine Weboberfläche zum Zeichnen",
            why = "Der Standardweg, ein Modell anderen Systemen zugänglich zu machen.")
        line(2, "Wie speichert man ein scikit-learn-Modell für die Bereitstellung?",
            "joblib.dump(pipeline, \"model.joblib\")", "pipeline.save(\"model.h5\")", "torch.save(pipeline)", "pipeline.export_onnx()",
            why = "Laden mit joblib.load; die ganze Pipeline speichern, damit die Vorverarbeitung mitreist.")
        mc(2, "Warum verpackt man ein Modell in einen Container (Docker)?",
            "Damit Code, Bibliotheksversionen und Modell überall identisch laufen", "Damit es schneller trainiert", "Damit es genauer wird", "Damit man keine Daten braucht",
            why = "„Bei mir läuft es“ wird zu „läuft überall“.")

        on("eng_spark")
        code(2, "Was macht dieser Spark-Code?", """
            from pyspark.ml.feature import VectorAssembler
            from pyspark.ml.regression import LinearRegression
            va = VectorAssembler(inputCols=["hp", "wt"], outputCol="features")
            model = LinearRegression(featuresCol="features", labelCol="mpg").fit(va.transform(df))
        """, "Er fasst Spalten zu einem Merkmalsvektor zusammen und trainiert verteilt eine lineare Regression", "Er lädt Daten in pandas", "Er trainiert ein CNN", "Er visualisiert Daten",
            why = "Spark MLlib erwartet die Merkmale in einer einzigen Vektorspalte.")
        mc(2, "Warum ist Spark ‚lazy‘?",
            "Transformationen werden erst bei einer Aktion ausgeführt; so kann Spark den ganzen Plan optimieren", "Weil es langsam ist", "Weil es keine Daten lädt", "Weil es nur einen Kern nutzt",
            why = "Aktionen wie count, collect oder write lösen die Berechnung aus.")
        line(2, "Welche Spark-Aktion löst die Berechnung aus und zählt die Zeilen?",
            "df.count()", "df.select(\"a\")", "df.filter(df.a > 0)", "df.withColumn(\"b\", df.a * 2)",
            why = "select, filter und withColumn sind lazy Transformationen.")

        on("ai_mlops")
        code(2, "Was erkennt dieser Vergleich im Betrieb?", """
            from scipy.stats import ks_2samp
            stat, p = ks_2samp(train_df["alter"], live_df["alter"])
        """, "Datendrift: ob sich die Verteilung eines Merkmals im Betrieb gegenüber dem Training verschoben hat", "Die Genauigkeit des Modells", "Einen Programmierfehler", "Die Latenz",
            why = "Kleines p deutet auf eine signifikante Verschiebung – Anlass zum Nachtrainieren.")
        mc(2, "Was ist Konzeptdrift?",
            "Der Zusammenhang zwischen Eingaben und Ziel ändert sich, z. B. neue Betrugsmaschen", "Die Eingabeverteilung bleibt gleich", "Ein Fehler im Code", "Eine neue Modellversion",
            why = "P(y | x) ändert sich – Monitoring der Leistung auf frischen Labels deckt das auf.")
        mc(2, "Was gehört zu verantwortungsvollem Betrieb eines Modells?",
            "Monitoring, Dokumentation (Model Card), Fairness-Prüfungen, Datenschutz und ein Plan für Fehlerfälle", "Nur eine hohe Accuracy", "Möglichst wenig Protokolle", "Das Modell nie zu aktualisieren",
            why = "Der EU AI Act verlangt je nach Risikoklasse genau solche Maßnahmen.")

        on("eng_mlops")
        code(2, "Was protokolliert dieser Code?", """
            import mlflow
            with mlflow.start_run():
                mlflow.log_param("max_depth", 6)
                mlflow.log_metric("f1", 0.87)
                mlflow.sklearn.log_model(model, "model")
        """, "Parameter, Metrik und Modell eines Trainingslaufs zur Nachverfolgung (Experiment-Tracking)", "Nur die Daten", "Die Systemauslastung", "Nichts",
            why = "So lässt sich jedes Ergebnis später reproduzieren und vergleichen.")
        mc(2, "Was ist ein Modellregister?",
            "Ein versionierter Speicher für trainierte Modelle mit Status wie Staging oder Production", "Eine Liste aller Python-Pakete", "Ein Protokoll der Nutzer", "Ein Datensatz",
            why = "Es macht nachvollziehbar, welche Version wann ausgeliefert wurde.")
        mc(2, "Was bedeutet CI/CD für ML (Continuous Training)?",
            "Tests, Training, Bewertung und Auslieferung laufen automatisiert, sobald sich Code oder Daten ändern", "Manuelles Kopieren von Dateien", "Ein einmaliges Training", "Ein Cloud-Anbieter",
            why = "Mit Qualitätsschranken: Ein neues Modell geht nur live, wenn es das alte schlägt.")

        on("ai_transformer")
        codeOut(2, "Welche Form hat QKᵀ für eine Folge der Länge 6 mit d_k = 8 (Batch 1)?", """
            Q = torch.randn(1, 6, 8); K = torch.randn(1, 6, 8)
            print((Q @ K.transpose(-2, -1)).shape)
        """, "torch.Size([1, 6, 6])", "torch.Size([1, 8, 8])", "torch.Size([1, 6, 8])", "torch.Size([6, 6, 8])",
            why = "Eine Score-Matrix Token × Token.")
        code(2, "Was ist die Rolle des Residualstroms in diesem Code?", """
            x = x + attn(norm1(x))
            x = x + mlp(norm2(x))
        """, "Jeder Unterblock liest aus x und addiert sein Ergebnis – Information und Gradienten fließen ungehindert durch alle Schichten", "Er löscht x", "Er ersetzt die Attention", "Er verdoppelt die Parameter",
            why = "Ohne Residuen wären tiefe Transformer kaum trainierbar.")

        on("ai_llm")
        code(2, "Was berechnet diese Faustformel?", """
            flops = 6 * n_params * n_tokens
        """, "Den ungefähren Rechenaufwand des Trainings in Gleitkommaoperationen", "Die Zahl der GPUs", "Den Speicherbedarf", "Die Perplexität",
            why = "≈ 2 FLOPs pro Parameter und Token vorwärts, 4 rückwärts.")
        mc(2, "Was besagen die Chinchilla-Skalierungsgesetze grob?",
            "Bei festem Rechenbudget sollten Parameter und Trainingstokens etwa gleich stark wachsen – rund 20 Tokens pro Parameter", "Größere Modelle brauchen weniger Daten", "Nur die Datenmenge zählt", "Nur die Modellgröße zählt",
            why = "Viele frühe LLMs waren gemessen daran untertrainiert.")

        on("gen_finetune")
        code(2, "Welche Datenform erwartet ein SFT-Datensatz typischerweise?", """
            {"messages": [{"role": "user", "content": "Erkläre Entropie."}, {"role": "assistant", "content": "Entropie misst …"}]}
        """, "Gespräche mit Rollen (user, assistant), aus denen die Chat-Vorlage den Trainingstext macht", "Bilder mit Labels", "Zahlenpaare", "Nur Antworten ohne Fragen",
            why = "Der SFTTrainer wendet die Chat-Vorlage automatisch an.")
        mc(2, "Wann lohnt sich Fine-Tuning statt nur Prompting?",
            "Wenn ein festes Format, Stil oder Fachverhalten zuverlässig und günstig gebraucht wird", "Immer zuerst", "Wenn man aktuelles Wissen braucht", "Nie",
            why = "Für aktuelles oder privates Wissen ist RAG meist besser.")
        mc(3, "Was ist katastrophales Vergessen?",
            "Beim Feintunen auf eine neue Aufgabe verliert das Modell früher erlernte Fähigkeiten", "Das Löschen der Gewichte", "Ein Speicherfehler", "Das Vergessen des Prompts",
            why = "Kleine Lernraten, PEFT und gemischte Daten mildern es.")

        on("ai_inference")
        codeOut(2, "Wie groß ist der KV-Cache für eine Sequenz?", """
            L, T, d, bytes_ = 32, 4096, 4096, 2
            print(2 * L * T * d * bytes_ / 1e9, "GB")
        """, "2.147483648 GB", "1.07 GB", "0.5 GB", "8.6 GB",
            why = "2 (K und V) · 32 · 4096 · 4096 · 2 Byte ≈ 2,1 GB – Grund für Techniken wie Grouped-Query-Attention.")
        code(2, "Was bewirkt temperature nahe 0 hier?", """
            model.generate(ids, do_sample=True, temperature=0.01)
        """, "Das Sampling wird praktisch greedy – fast immer das wahrscheinlichste Token", "Völlig zufällige Tokens", "Längere Texte", "Keine Ausgabe",
            why = "Logits werden durch T geteilt; kleines T schärft die Verteilung extrem.")

        on("gen_prompting")
        code(2, "Welche Technik nutzt dieser Prompt?", """
            prompt = "Frage: Ein Ritter hat 3 Pferde und kauft 2 dazu. Wie viele hat er? Denke Schritt für Schritt."
        """, "Chain-of-Thought: das Modell zu schrittweisem Denken anregen", "Few-Shot mit Beispielen", "Retrieval", "Fine-Tuning",
            why = "Zwischenschritte verbessern Rechen- und Logikaufgaben deutlich.")
        code(2, "Welche Technik nutzt dieser Prompt?", """
            prompt = "Stimmung: 'Toll!' → positiv\nStimmung: 'Schrecklich.' → negativ\nStimmung: 'Ganz okay.' →"
        """, "Few-Shot-Prompting: Beispiele im Prompt zeigen das gewünschte Format", "Zero-Shot", "Chain-of-Thought", "Systemprompt",
            why = "In-Context-Learning ohne Änderung der Gewichte.")

        on("gen_vector")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            import numpy as np
            a = np.array([1.0, 1.0]); b = np.array([1.0, 0.0])
            print(round(a @ b / (np.linalg.norm(a) * np.linalg.norm(b)), 3))
        """, "0.707", "1.0", "0.5", "0.0",
            why = "cos 45° = 1/√2.")
        line(2, "Welche Zeile berechnet Satz-Einbettungen mit Sentence-Transformers?",
            "SentenceTransformer(\"all-MiniLM-L6-v2\").encode(saetze)", "AutoTokenizer.encode(saetze)", "np.embed(saetze)", "faiss.encode(saetze)",
            why = "encode liefert ein Array (Anzahl × 384).")
        mc(2, "Was ist HNSW?",
            "Ein graphbasierter Index für schnelle approximative Nächste-Nachbarn-Suche", "Ein Tokenizer", "Ein Einbettungsmodell", "Ein Kompressionsformat",
            why = "Hierarchical Navigable Small World – Standard in vielen Vektordatenbanken.")

        on("ai_rag_agents")
        code(2, "Was ist das Muster dieser Funktion?", """
            def answer(q):
                docs = retriever.invoke(q)
                return llm.invoke(f"Kontext: {docs}\nFrage: {q}")
        """, "Retrieval-Augmented Generation: erst passende Dokumente holen, dann mit Kontext antworten", "Fine-Tuning", "Ein Agent mit Werkzeugen", "Prompt-Kompression",
            why = "Die einfachste RAG-Kette.")
        mc(2, "Was unterscheidet einen Agenten von einer festen Kette?",
            "Der Agent entscheidet selbst, welche Werkzeuge er in welcher Reihenfolge nutzt", "Er braucht kein LLM", "Er ist immer schneller", "Er hat keine Werkzeuge",
            why = "Mehr Flexibilität, aber auch mehr Risiko und Kosten.")

        on("gen_frameworks")
        code(2, "Was entsteht hier?", """
            chain = ChatPromptTemplate.from_template("Fasse zusammen: {text}") | llm | StrOutputParser()
        """, "Eine LCEL-Kette aus Prompt-Vorlage, Modell und Parser", "Ein trainiertes Modell", "Ein Vektorindex", "Ein Agent",
            why = "Aufruf mit chain.invoke({\"text\": …}).")
        code(2, "Wozu dient dieser Platzhalter in einem Chat-Prompt?", """
            MessagesPlaceholder("history")
        """, "Er fügt den bisherigen Gesprächsverlauf ein", "Er löscht den Verlauf", "Er ruft ein Werkzeug", "Er speichert Einbettungen",
            why = "Grundlage für Chatbots mit Gedächtnis.")
        mc(2, "Was ist die Aufgabe eines Document Loaders?",
            "Inhalte aus Dateien oder Webseiten als Dokumente mit Text und Metadaten einzulesen", "Modelle zu laden", "Gewichte zu speichern", "Prompts zu schreiben",
            why = "Erster Schritt jeder RAG-Pipeline.")
        mc(2, "Warum zerlegt man Dokumente vor dem Einbetten?",
            "Kleinere Abschnitte passen ins Einbettungsmodell und liefern präzisere Treffer", "Um Speicher zu sparen", "Weil LLMs keine Dokumente lesen", "Um Metadaten zu löschen",
            why = "Chunk-Größe und Überlappung sind wichtige Stellschrauben.")

        on("gen_eval")
        code(2, "Was prüft dieser Test?", """
            antwort = bot("Wie hoch ist der Mond aus Käse?")
            assert "steht nicht im Dokument" in antwort or "weiß ich nicht" in antwort
        """, "Ob der Bot bei fehlender Information ehrlich bleibt statt zu halluzinieren", "Die Antwortzeit", "Die Rechtschreibung", "Die Zahl der Tokens",
            why = "Negative Testfälle gehören in jede Evaluation.")
        mc(2, "Was ist LLM-as-a-Judge?",
            "Ein starkes LLM bewertet Antworten anderer Modelle nach Kriterien – schnell, aber mit eigenen Verzerrungen", "Ein Gericht für KI", "Ein Benchmark für Juristen", "Ein Tokenizer",
            why = "Typische Verzerrungen: Vorliebe für lange Antworten oder die eigene Modellfamilie.")
        mc(2, "Was ist eine Prompt-Injection-Abwehr in RAG-Anwendungen?",
            "Abgerufene Inhalte als Daten behandeln, Anweisungen darin ignorieren und kritische Aktionen absichern", "Längere Prompts", "Höhere Temperatur", "Mehr Dokumente abrufen",
            why = "Inhalte aus Dokumenten oder dem Web dürfen das System nicht umprogrammieren.")
    }
}

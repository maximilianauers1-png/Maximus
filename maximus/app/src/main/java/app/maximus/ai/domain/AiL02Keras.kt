package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 2 (IBM „Introduction to Deep Learning & Neural Networks with Keras“): forward propagation with
 * numbers, vanishing gradients and activations, and the two classic Keras projects (regression on
 * concrete strength, classification on MNIST).
 */
internal object AiL02Keras {
    val bites: List<Bite> = listOf(
        bite(
            "dl_forward", 1, "Vorwärtsausbreitung Schritt für Schritt",
            "Wie ein Netz aus Eingaben eine Ausgabe berechnet: gewichtete Summe, Aktivierung, Schicht für Schicht – mit Zahlen und in Matrixform.",
            goals(
                "Die Ausgabe eines kleinen Netzes von Hand berechnen",
                "Vorwärtsausbreitung als Folge von Matrixmultiplikationen schreiben",
                "Die Zahl der Parameter einer Dense-Schicht bestimmen"
            ),
            sec("Ein Neuron, eine Rechnung", """
                Jedes künstliche Neuron macht zwei Dinge: Es bildet die gewichtete Summe seiner Eingänge plus einen Bias und schickt das Ergebnis durch eine Aktivierungsfunktion. Im IBM-Kurs heißt die gewichtete Summe z und die Aktivierung a.

                Beispiel mit zwei Eingängen x₁ = 0,1 und x₂ = 0,6, Gewichten w₁ = 0,15, w₂ = 0,25 und Bias b = 0,4: z = 0,1·0,15 + 0,6·0,25 + 0,4 = 0,565. Mit der Sigmoid-Funktion ist a = 1/(1 + e^{−0,565}) ≈ 0,638.
            """,
                fm("Gewichtete Summe", "z = Σᵢ wᵢxᵢ + b = wᵀx + b"),
                fm("Aktivierung", "a = σ(z),  σ(z) = 1/(1 + e^{−z})")
            ),
            sec("Schicht für Schicht", """
                Ein Netz besteht aus Schichten. Die Ausgabe einer Schicht ist der Eingang der nächsten. Fasst man die Gewichte einer Schicht mit n_in Eingängen und n_out Neuronen zu einer Matrix W ∈ ℝ^{n_out × n_in} zusammen, wird die ganze Schicht zu einer einzigen Matrixmultiplikation. Für einen Stapel (Batch) von B Beispielen ist X eine B × n_in-Matrix, und alles läuft parallel auf der GPU.

                Fortsetzung des Beispiels: Das zweite versteckte Neuron mit Gewichten (0,20; 0,30) und Bias 0,4 hat z₂ = 0,02 + 0,18 + 0,4 = 0,6 und a₂ = σ(0,6) ≈ 0,646. Ein Ausgabeneuron mit Gewichten (0,45; 0,65) und Bias 0,65 rechnet z = 0,638·0,45 + 0,646·0,65 + 0,65 ≈ 1,357 und gibt σ(1,357) ≈ 0,795 aus.
            """,
                fm("Schicht", "a^{(l)} = φ(W^{(l)} a^{(l−1)} + b^{(l)})"),
                fm("Batch-Form", "A^{(l)} = φ(A^{(l−1)} W^{(l)ᵀ} + b^{(l)}),  A ∈ ℝ^{B×n}"),
                fm("Parameter einer Dense-Schicht", "P = n_in · n_out + n_out")
            ),
            sec("In NumPy", """
                $ import numpy as np
                $ def sigmoid(z):
                $     return 1 / (1 + np.exp(-z))
                $ x = np.array([0.1, 0.6])
                $ W1 = np.array([[0.15, 0.25], [0.20, 0.30]]); b1 = np.array([0.4, 0.4])
                $ a1 = sigmoid(W1 @ x + b1)          # versteckte Schicht, 2 Neuronen
                $ W2 = np.array([[0.45, 0.65]]); b2 = np.array([0.65])
                $ y_hat = sigmoid(W2 @ a1 + b2)      # Ausgabe

                @ ist die Matrixmultiplikation. Ein Netz mit 2 Eingängen, 2 versteckten Neuronen und 1 Ausgang hat 2·2 + 2 + 2·1 + 1 = 9 Parameter.
            """),
            sec("Warum nicht nur linear?", """
                Ohne Aktivierungsfunktion wäre jede Schicht eine lineare Abbildung, und die Hintereinanderschaltung linearer Abbildungen ist wieder linear: W₂(W₁x) = (W₂W₁)x. Ein tiefes Netz könnte dann nicht mehr als eine einzige lineare Schicht. Erst die Nichtlinearität macht Netze zu universellen Approximatoren: Schon eine versteckte Schicht mit genügend Neuronen kann jede stetige Funktion auf einem kompakten Bereich beliebig genau annähern.

                > Vorwärts: Eingabe → gewichtete Summe → Aktivierung → nächste Schicht → Ausgabe → Verlust. Rückwärts (Backpropagation) fließen dann die Gradienten denselben Weg zurück.
            """)
        ),
        bite(
            "dl_vanishing", 2, "Verschwindende Gradienten und Aktivierungsfunktionen",
            "Warum Sigmoid tiefe Netze lähmt, wie ReLU und gute Initialisierung helfen, und welche Aktivierung an welche Stelle gehört.",
            goals(
                "Das Problem verschwindender und explodierender Gradienten mit der Kettenregel begründen",
                "Sigmoid, tanh, ReLU, Leaky ReLU und Softmax mit Ableitungen kennen",
                "Für versteckte Schichten und Ausgabeschichten die passende Aktivierung wählen"
            ),
            sec("Die Kette der Ableitungen", """
                Backpropagation multipliziert Ableitungen entlang des Netzes. Für ein Gewicht in der ersten von L Schichten steht im Gradienten ein Produkt aus L Faktoren der Form w·σ′(z). Die Ableitung der Sigmoid-Funktion ist höchstens 1/4 (bei z = 0). Bei zehn Schichten ist (1/4)¹⁰ ≈ 10⁻⁶: Die vorderen Schichten lernen praktisch nicht mehr. Das ist das Problem der verschwindenden Gradienten (vanishing gradients), das tiefe Netze bis etwa 2010 bremste. Sind die Faktoren dagegen größer als 1, explodieren die Gradienten.
            """,
                fm("Kettenregel durch L Schichten", "∂J/∂a^{(1)} = Πₗ W^{(l)ᵀ} diag(φ′(z^{(l)})) · ∂J/∂a^{(L)}"),
                fm("Sigmoid-Ableitung", "σ′(z) = σ(z)(1 − σ(z)) ≤ 1/4"),
                fm("Abschätzung", "|Gradient| ~ (w · max σ′)^L = (w/4)^L")
            ),
            sec("Die Aktivierungsfunktionen", """
                • Sigmoid σ(z) = 1/(1 + e^{−z}): Werte in (0, 1), sättigt für große |z|, nicht nullzentriert. Heute fast nur noch in Ausgabeschichten für binäre Klassifikation.
                • tanh: Werte in (−1, 1), nullzentriert, Ableitung bis 1 – besser als Sigmoid, sättigt aber ebenfalls.
                • ReLU max(0, z): Ableitung 1 für z > 0, kein Sättigen nach oben, billig. Standard für versteckte Schichten. Risiko: „tote“ Neuronen mit dauerhaft z < 0.
                • Leaky ReLU max(αz, z) mit kleinem α (z. B. 0,01): Kein totes Neuron, weil auch für z < 0 ein Gradient fließt.
                • Softmax: wandelt einen Vektor in Wahrscheinlichkeiten, die sich zu 1 addieren. Ausgabeschicht für Mehrklassen-Klassifikation.
                • Linear (keine Aktivierung): Ausgabeschicht für Regression.
            """,
                fm("tanh", "tanh(z) = (e^z − e^{−z})/(e^z + e^{−z}),  tanh′ = 1 − tanh²"),
                fm("ReLU", "ReLU(z) = max(0, z),  ReLU′(z) = 1 für z > 0, sonst 0"),
                fm("Softmax", "softmax(z)ᵢ = e^{zᵢ} / Σⱼ e^{zⱼ}")
            ),
            sec("Gegenmittel", """
                • ReLU-Familie in versteckten Schichten statt Sigmoid.
                • Gute Initialisierung: Xavier/Glorot (für tanh) oder He (für ReLU) halten die Varianz der Aktivierungen über die Schichten konstant.
                • Batch-Normalisierung hält die Eingänge jeder Schicht im gut differenzierbaren Bereich.
                • Residualverbindungen (ResNet) geben dem Gradienten eine Abkürzung: y = x + F(x).
                • Gegen explodierende Gradienten: Gradient Clipping, vor allem bei RNNs.

                $ from tensorflow import keras
                $ layer = keras.layers.Dense(64, activation="relu", kernel_initializer="he_normal")
            """,
                fm("He-Initialisierung", "Var(w) = 2/n_in"),
                fm("Xavier/Glorot", "Var(w) = 2/(n_in + n_out)")
            ),
            sec("Faustregeln für die Ausgabeschicht", """
                Die Ausgabeschicht folgt aus der Aufgabe – und mit ihr die Verlustfunktion:

                • Regression: 1 Neuron, linear, Verlust MSE (oder MAE, Huber).
                • Binäre Klassifikation: 1 Neuron, Sigmoid, binäre Kreuzentropie.
                • Mehrklassen, genau eine Klasse richtig: K Neuronen, Softmax, kategoriale Kreuzentropie.
                • Mehrere Labels gleichzeitig möglich (Multi-Label): K Neuronen, je Sigmoid, binäre Kreuzentropie.

                > Prüfungsklassiker: Softmax-Ausgaben summieren sich zu 1, unabhängige Sigmoid-Ausgaben nicht.
            """)
        ),
        bite(
            "keras_regression", 1, "Regression mit Keras: Betonfestigkeit vorhersagen",
            "Das erste vollständige Keras-Projekt nach dem IBM-Kurs: Daten normieren, Sequential-Modell bauen, kompilieren, trainieren, auswerten.",
            goals(
                "Ein Sequential-Modell mit Dense-Schichten für Regression aufbauen",
                "compile, fit und evaluate mit sinnvollen Argumenten aufrufen",
                "Den Einfluss von Normierung, Epochenzahl und Tiefe auf den Testfehler untersuchen"
            ),
            sec("Die Aufgabe", """
                Der Datensatz enthält rund 1000 Betonmischungen mit acht Merkmalen – Zement, Hochofenschlacke, Flugasche, Wasser, Fließmittel, grober und feiner Zuschlag sowie das Alter in Tagen – und als Ziel die Druckfestigkeit in MPa. Es ist eine klassische Regressionsaufgabe: eine Zahl vorhersagen.

                $ import pandas as pd
                $ concrete = pd.read_csv("concrete_data.csv")
                $ predictors = concrete.drop(columns=["Strength"])
                $ target = concrete["Strength"]
                $ predictors_norm = (predictors - predictors.mean()) / predictors.std()
                $ n_cols = predictors_norm.shape[1]   # 8

                Normieren ist bei neuronalen Netzen fast immer nötig: Merkmale mit großen Zahlen (Zement in kg/m³) würden sonst den Gradienten dominieren.
            """),
            sec("Modell bauen und kompilieren", """
                $ from tensorflow import keras
                $ from tensorflow.keras import layers
                $ def regression_model():
                $     model = keras.Sequential([
                $         keras.Input(shape=(n_cols,)),
                $         layers.Dense(50, activation="relu"),
                $         layers.Dense(50, activation="relu"),
                $         layers.Dense(1)
                $     ])
                $     model.compile(optimizer="adam", loss="mean_squared_error")
                $     return model

                Die letzte Schicht hat ein Neuron ohne Aktivierung, weil die Festigkeit jede reelle Zahl sein kann. compile legt Optimierer, Verlust und optional Metriken fest; erst danach kann man trainieren. Parameterzahl: 8·50 + 50 + 50·50 + 50 + 50·1 + 1 = 3001.
            """,
                fm("Parameter", "P = Σₗ (n_{l−1} · n_l + n_l)")
            ),
            sec("Trainieren und bewerten", """
                $ model = regression_model()
                $ history = model.fit(predictors_norm, target, validation_split=0.3, epochs=100, verbose=2)
                $ mse = model.evaluate(X_test, y_test)
                $ preds = model.predict(X_test)

                • Eine Epoche ist ein vollständiger Durchlauf durch die Trainingsdaten; der Standard-batch_size ist 32.
                • validation_split=0.3 nimmt die letzten 30 % der Daten als Validierung (nicht gemischt – vorher mischen!).
                • history.history enthält die Verlustkurven loss und val_loss für jede Epoche.

                Im IBM-Abschlussprojekt wiederholt man das 50-mal mit verschiedenen Zufallsaufteilungen und berichtet Mittelwert und Standardabweichung des MSE – eine ehrliche Fehlerabschätzung, wie man sie aus dem Labor kennt.
            """),
            sec("Was die Experimente zeigen", """
                • Ohne Normierung: deutlich höherer und stärker streuender Testfehler.
                • 50 statt 100 Epochen: höherer Fehler – das Netz ist noch nicht auskonvergiert.
                • Drei statt einer versteckten Schicht: meist niedrigerer Fehler, weil das Netz komplexere Zusammenhänge abbilden kann.

                > Ein Experiment, eine Änderung: Wie im Physiklabor variiert man immer nur eine Größe und misst mehrfach.
            """,
                fm("Mittelwert und Streuung", "MSE̅ = (1/R) Σ MSE_r,  s = √(Σ(MSE_r − MSE̅)²/(R − 1))")
            )
        ),
        bite(
            "keras_classification", 1, "Klassifikation mit Keras: handgeschriebene Ziffern",
            "MNIST mit einem dichten Netz: Bilder flach machen, One-Hot-Labels, Softmax, kategoriale Kreuzentropie, Modell speichern und laden.",
            goals(
                "Bilddaten für ein Dense-Netz vorbereiten (flatten, skalieren, to_categorical)",
                "Softmax und kategoriale Kreuzentropie korrekt kombinieren",
                "Ein Modell speichern, laden und Vorhersagen in Klassen umrechnen"
            ),
            sec("Daten vorbereiten", """
                MNIST enthält 70 000 Graustufenbilder von Ziffern, je 28 × 28 Pixel mit Werten 0–255. Ein dichtes Netz braucht einen Vektor: 28 · 28 = 784 Eingänge.

                $ from tensorflow import keras
                $ (X_train, y_train), (X_test, y_test) = keras.datasets.mnist.load_data()
                $ X_train = X_train.reshape(-1, 784).astype("float32") / 255.0
                $ X_test = X_test.reshape(-1, 784).astype("float32") / 255.0
                $ y_train = keras.utils.to_categorical(y_train)   # 5 → [0,0,0,0,0,1,0,0,0,0]
                $ y_test = keras.utils.to_categorical(y_test)
                $ num_classes = y_test.shape[1]                    # 10
            """,
                fm("Eingangsgröße", "28 × 28 = 784"),
                fm("One-Hot", "y = 5 → e₅ ∈ {0,1}^{10}")
            ),
            sec("Modell", """
                $ model = keras.Sequential([
                $     keras.Input(shape=(784,)),
                $     keras.layers.Dense(784, activation="relu"),
                $     keras.layers.Dense(100, activation="relu"),
                $     keras.layers.Dense(num_classes, activation="softmax")
                $ ])
                $ model.compile(optimizer="adam", loss="categorical_crossentropy", metrics=["accuracy"])
                $ model.fit(X_train, y_train, validation_data=(X_test, y_test), epochs=10, verbose=2)

                Die Softmax-Schicht liefert zehn Wahrscheinlichkeiten. Die kategoriale Kreuzentropie bestraft eine kleine Wahrscheinlichkeit für die richtige Klasse logarithmisch: p = 0,9 kostet 0,105, p = 0,01 kostet 4,6.
            """,
                fm("Kategoriale Kreuzentropie", "L = −Σₖ yₖ ln pₖ = −ln p_{richtig}"),
                fm("Parameter erste Schicht", "784 · 784 + 784 = 615 440")
            ),
            sec("Labels: One-Hot oder Ganzzahl?", """
                Es gibt zwei gleichwertige Wege. Mit One-Hot-Labels (to_categorical) nimmt man loss="categorical_crossentropy". Mit Ganzzahl-Labels 0–9 nimmt man loss="sparse_categorical_crossentropy" und spart sich die Umwandlung. Wer beides vermischt, bekommt einen Formfehler oder ein sinnloses Training.

                $ probs = model.predict(X_test[:3])     # shape (3, 10)
                $ classes = probs.argmax(axis=1)         # z. B. [7 2 1]
            """),
            sec("Speichern und laden", """
                $ model.save("classification_model.keras")
                $ restored = keras.models.load_model("classification_model.keras")

                Das .keras-Format speichert Architektur, Gewichte und den Zustand des Optimierers – man kann nahtlos weitertrainieren. Ältere Kurse verwenden noch .h5 (HDF5).

                > Ein dichtes Netz erreicht auf MNIST etwa 98 %. Es weiß aber nichts von Nachbarschaft der Pixel – genau das bringen erst Faltungsnetze (CNN), die nächsten Happen.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l2") {
        on("dl_forward")
        mc(1, "Was berechnet ein künstliches Neuron zuerst?",
            "Die gewichtete Summe der Eingänge plus Bias", "Die Ableitung des Verlusts", "Den Mittelwert der Gewichte", "Die Softmax aller Ausgänge",
            why = "z = Σ wᵢxᵢ + b; danach folgt die Aktivierung a = φ(z).")
        mc(1, "Eingänge x = (0,1; 0,6), Gewichte w = (0,15; 0,25), Bias 0,4. Wie groß ist z?",
            "0,565", "0,4", "0,165", "1,0",
            why = "0,1·0,15 + 0,6·0,25 + 0,4 = 0,015 + 0,15 + 0,4 = 0,565.")
        mc(2, "Wie viele Parameter hat ein Netz 4 → 8 → 3 aus Dense-Schichten mit Bias?",
            "67", "56", "35", "96",
            why = "4·8 + 8 = 40 und 8·3 + 3 = 27; zusammen 67.")
        codeOut(1, "Welche Form hat a1?", """
            X = np.random.rand(32, 10)     # Batch von 32 Beispielen
            W1 = np.random.rand(10, 64)
            a1 = np.maximum(0, X @ W1)
            print(a1.shape)
        """, "(32, 64)", "(64, 32)", "(10, 64)", "(32, 10)",
            why = "(32 × 10)·(10 × 64) ergibt 32 × 64: für jedes Beispiel 64 Aktivierungen. np.maximum(0, …) ist ReLU.")
        mc(1, "Warum braucht ein Netz nichtlineare Aktivierungen?",
            "Ohne sie wäre jede Schichtfolge wieder nur eine lineare Abbildung", "Damit die Gewichte positiv bleiben", "Damit das Netz schneller rechnet", "Damit keine Biases nötig sind",
            why = "W₂(W₁x) = (W₂W₁)x. Erst φ macht tiefe Netze ausdrucksstark (universelle Approximation).")
        code(2, "Was ist an dieser Vorwärtsausbreitung falsch?", """
            a1 = sigmoid(W1 @ x + b1)
            y_hat = W2 @ a1 + b2
            y_hat = sigmoid(W2 @ a1 + b2)
        """, "Nichts Grundsätzliches – die mittlere Zeile ist überflüssig, weil sie sofort überschrieben wird",
            "Sigmoid darf nicht zweimal vorkommen", "Der Bias muss vor der Matrixmultiplikation addiert werden", "a1 müsste transponiert werden",
            why = "Die zweite Zeile berechnet die lineare Ausgabe, die dritte ersetzt sie durch die aktivierte. Toter Code, aber kein Rechenfehler.")
        codeOut(2, "Was gibt der Code (gerundet) aus?", """
            import numpy as np
            z = np.array([0.0])
            print(1 / (1 + np.exp(-z)))
        """, "[0.5]", "[0.]", "[1.]", "[0.25]",
            why = "σ(0) = 1/(1 + 1) = 0,5. Dort ist auch die Steigung maximal: σ′(0) = 0,25.")
        mc(2, "Was besagt der universelle Approximationssatz?",
            "Ein Netz mit einer versteckten Schicht und genügend Neuronen kann jede stetige Funktion auf einem kompakten Bereich beliebig genau annähern",
            "Jedes Netz findet beim Training das globale Minimum", "Tiefe Netze brauchen keine Daten", "Lineare Netze sind universell",
            why = "Der Satz garantiert Existenz, nicht Lernbarkeit: Wie viele Neuronen nötig sind und ob Gradientenabstieg sie findet, sagt er nicht.")

        on("dl_vanishing")
        mc(1, "Was ist das Problem verschwindender Gradienten?",
            "In tiefen Netzen werden die Gradienten der vorderen Schichten so klein, dass diese kaum lernen", "Die Gewichte verschwinden aus dem Speicher",
            "Der Verlust wird negativ", "Das Netz vergisst die Trainingsdaten",
            why = "Backpropagation multipliziert viele Faktoren w·φ′(z); mit Sigmoid ist φ′ ≤ 1/4, das Produkt schrumpft exponentiell.")
        mc(2, "Wie groß ist die Ableitung der Sigmoid-Funktion höchstens?",
            "1/4", "1", "1/2", "Sie ist unbeschränkt",
            why = "σ′ = σ(1 − σ) ist bei σ = 1/2 maximal: 1/2 · 1/2 = 1/4.")
        mc(1, "Welche Aktivierung ist heute Standard in versteckten Schichten?",
            "ReLU (oder eine Variante wie Leaky ReLU, GELU)", "Sigmoid", "Softmax", "Die Identität",
            why = "ReLU sättigt nach oben nicht und hat Ableitung 1 für z > 0 – der Gradient fließt.")
        mc(2, "Was ist ein ‚totes‘ ReLU-Neuron?",
            "Ein Neuron, dessen Eingang für alle Daten negativ ist; es gibt immer 0 aus und bekommt keinen Gradienten mehr", "Ein Neuron mit Gewicht null",
            "Ein Neuron in der Ausgabeschicht", "Ein Neuron, das nur 1 ausgibt",
            why = "Für z < 0 ist ReLU′ = 0. Leaky ReLU oder eine kleinere Lernrate verhindern das.")
        line(1, "Welche Ausgabeschicht passt zu einer Klassifikation in 5 sich ausschließende Klassen?",
            "layers.Dense(5, activation=\"softmax\")",
            "layers.Dense(1, activation=\"sigmoid\")",
            "layers.Dense(5, activation=\"relu\")",
            "layers.Dense(5)",
            why = "Softmax gibt eine Wahrscheinlichkeitsverteilung über die 5 Klassen; dazu kategoriale Kreuzentropie.")
        line(2, "Welche Ausgabeschicht passt, wenn ein Bild gleichzeitig ‚Hund‘, ‚Katze‘ und ‚Gras‘ zeigen kann (Multi-Label)?",
            "layers.Dense(3, activation=\"sigmoid\")",
            "layers.Dense(3, activation=\"softmax\")",
            "layers.Dense(1, activation=\"softmax\")",
            "layers.Dense(3, activation=\"tanh\")",
            why = "Jede Klasse ist eine eigene Ja/Nein-Frage – unabhängige Sigmoide mit binärer Kreuzentropie. Softmax würde die Klassen gegeneinander konkurrieren lassen.")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            import numpy as np
            z = np.array([2.0, 1.0, 0.0])
            p = np.exp(z) / np.exp(z).sum()
            print(p.round(2), p.sum())
        """, "[0.67 0.24 0.09] 1.0", "[0.5 0.33 0.17] 1.0", "[2. 1. 0.] 3.0", "[0.33 0.33 0.33] 1.0",
            why = "e² = 7,39, e¹ = 2,72, e⁰ = 1; Summe 11,1. Softmax-Ausgaben summieren sich immer zu 1.")
        mc(3, "Welche Initialisierung ist für ReLU-Netze gedacht?",
            "He-Initialisierung mit Var(w) = 2/n_in", "Alle Gewichte null", "Xavier mit Var(w) = 1/n_out", "Gleichverteilung auf [−10, 10]",
            why = "ReLU halbiert im Mittel die Varianz (die Hälfte der Eingänge wird 0); der Faktor 2 gleicht das aus. Nullgewichte würden alle Neuronen gleich machen (Symmetrie).")
        code(3, "Was bewirkt clipnorm in diesem Optimierer?", """
            opt = keras.optimizers.Adam(learning_rate=1e-3, clipnorm=1.0)
        """, "Ist die Norm des Gradienten größer als 1, wird er auf Norm 1 skaliert – Schutz vor explodierenden Gradienten",
            "Er begrenzt die Gewichte auf [−1, 1]", "Er stoppt das Training nach einer Epoche", "Er setzt die Lernrate auf 1",
            why = "Gradient Clipping ist das Standardmittel bei RNNs und Transformern gegen gelegentliche Gradientenexplosionen.")

        on("keras_regression")
        mc(1, "Welche Ausgabeschicht passt zur Vorhersage der Betonfestigkeit?",
            "Ein Neuron ohne Aktivierung (linear)", "Zehn Neuronen mit Softmax", "Ein Neuron mit Sigmoid", "Zwei Neuronen mit ReLU",
            why = "Regression liefert eine unbeschränkte reelle Zahl. Sigmoid würde auf (0, 1) einschränken.")
        line(1, "Welche Zeile normiert die Merkmale wie im IBM-Kurs?",
            "X_norm = (X - X.mean()) / X.std()",
            "X_norm = X / X.sum()",
            "X_norm = (X - X.min()) * X.max()",
            "X_norm = X.mean() / X.std()",
            why = "Spaltenweise Standardisierung (pandas rechnet mean() und std() je Spalte). Damit liegen alle Merkmale in derselben Größenordnung.")
        mc(1, "Was macht model.compile(optimizer=\"adam\", loss=\"mean_squared_error\")?",
            "Es legt Optimierer und Verlustfunktion fest; erst danach kann man trainieren", "Es trainiert das Modell", "Es speichert das Modell", "Es übersetzt das Modell in C",
            why = "compile konfiguriert das Training. fit trainiert, evaluate misst, predict sagt voraus.")
        mc(2, "Wie viele Parameter hat Input(8) → Dense(10, relu) → Dense(1)?",
            "101", "90", "88", "19",
            why = "8·10 + 10 = 90 und 10·1 + 1 = 11 – zusammen 101.")
        code(2, "Was ist an validation_split zu beachten?", """
            model.fit(X, y, validation_split=0.3, epochs=100)
        """, "Keras nimmt die letzten 30 % der Daten ohne Mischen – sind die Daten sortiert, ist die Validierung verzerrt",
            "Keras wählt die 30 % zufällig in jeder Epoche neu", "validation_split trainiert auf 30 % der Daten", "Die Validierungsdaten fließen in die Gewichte ein",
            why = "Der Split erfolgt vor dem Mischen. Deshalb mischt man vorher selbst oder übergibt validation_data explizit.")
        mc(2, "Im IBM-Projekt verringert man die Epochen von 100 auf 50. Was ist typisch?",
            "Der Testfehler steigt, weil das Netz noch nicht auskonvergiert ist", "Der Testfehler sinkt immer", "Nichts ändert sich", "Das Modell wird größer",
            why = "Bei diesem kleinen Netz und Datensatz ist 100 Epochen noch keine Überanpassung, also hilft mehr Training.")
        codeOut(2, "Was enthält history.history nach diesem Aufruf?", """
            history = model.fit(X, y, validation_split=0.2, epochs=5)
            print(list(history.history.keys()))
        """, "['loss', 'val_loss']", "['accuracy', 'val_accuracy']", "['mse']", "['epochs', 'loss']",
            why = "Ohne zusätzliche metrics nur der Verlust für Training und Validierung, je ein Wert pro Epoche.")
        mc(3, "Warum wiederholt man das Training 50-mal mit verschiedenen Aufteilungen und mittelt den MSE?",
            "Ein einzelner Lauf schwankt durch Zufall in Aufteilung und Initialisierung; Mittelwert und Standardabweichung geben eine belastbare Schätzung",
            "Damit das Modell 50-mal so groß wird", "Weil Keras sonst nicht konvergiert", "Um die Testdaten zu vergrößern",
            why = "Wie bei Messreihen: Der Mittelwert schätzt den Erwartungswert, s/√R seine Unsicherheit.")

        on("keras_classification")
        mc(1, "Wie viele Eingänge braucht ein dichtes Netz für 28 × 28-Graustufenbilder?",
            "784", "28", "56", "2828",
            why = "Das Bild wird zu einem Vektor der Länge 28 · 28 = 784 abgeflacht.")
        codeOut(1, "Was gibt der Code aus?", """
            from tensorflow import keras
            print(keras.utils.to_categorical([2], num_classes=4))
        """, "[[0. 0. 1. 0.]]", "[[2. 0. 0. 0.]]", "[[0. 1. 0. 0.]]", "[[1. 1. 1. 0.]]",
            why = "One-Hot: an Position 2 (von 0 an gezählt) steht eine 1.")
        line(1, "Welcher Verlust passt zu Ganzzahl-Labels 0–9 und einer Softmax-Ausgabe?",
            "loss=\"sparse_categorical_crossentropy\"",
            "loss=\"categorical_crossentropy\"",
            "loss=\"binary_crossentropy\"",
            "loss=\"mean_squared_error\"",
            why = "sparse_… erwartet Klassenindizes, categorical_… One-Hot-Vektoren. Mathematisch identisch.")
        mc(2, "Die richtige Klasse bekommt die Wahrscheinlichkeit 0,01. Wie groß ist die kategoriale Kreuzentropie?",
            "≈ 4,6", "≈ 0,01", "≈ 0,99", "≈ 100",
            why = "L = −ln 0,01 = ln 100 ≈ 4,6. Bei p = 0,9 wären es nur 0,105.")
        code(1, "Was liefert dieser Ausdruck?", """
            classes = model.predict(X_test).argmax(axis=1)
        """, "Für jedes Bild den Index der Klasse mit der höchsten Wahrscheinlichkeit", "Die höchste Wahrscheinlichkeit selbst",
            "Die Zahl der Klassen", "Die Accuracy",
            why = "predict liefert (n, 10) Wahrscheinlichkeiten; argmax entlang axis=1 wählt je Zeile die wahrscheinlichste Ziffer.")
        mc(2, "Warum teilt man die Pixelwerte durch 255?",
            "Damit die Eingaben in [0, 1] liegen und die Optimierung gut konditioniert ist", "Weil Keras keine Zahlen über 1 akzeptiert", "Um Speicher zu sparen", "Um die Bilder zu schärfen",
            why = "Große Eingabewerte führen zu großen gewichteten Summen, gesättigten Aktivierungen und instabilen Gradienten.")
        line(2, "Mit welcher Zeile lädt man ein gespeichertes Keras-Modell?",
            "model = keras.models.load_model(\"classification_model.keras\")",
            "model = keras.Sequential.load(\"classification_model.keras\")",
            "model = pickle.load(\"classification_model.keras\")",
            "model = keras.open(\"classification_model.keras\")",
            why = "load_model stellt Architektur, Gewichte und Optimiererzustand wieder her.")
        mc(3, "Warum sind dichte Netze für Bilder eigentlich die falsche Architektur?",
            "Sie ignorieren die räumliche Nachbarschaft der Pixel und haben sehr viele Parameter; CNNs teilen Gewichte über das Bild",
            "Sie können keine Graustufen verarbeiten", "Sie erreichen auf MNIST höchstens 50 %", "Sie brauchen keine Aktivierungen",
            why = "Schon die erste Schicht hat 615 440 Parameter. Faltungen nutzen Lokalität und Translationsinvarianz mit wenigen geteilten Gewichten.")
    }
}

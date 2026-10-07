package app.maximus.lab.domain

/**
 * "AI von Null": the very first bites of the AI learning path, for someone who has never trained a model.
 * Every chapter goes intuition → everyday example → the one formula that matters → a few lines of code.
 * Code lines start with "$ " (rendered monospace).
 */
internal object CompendiumAiZero {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "AI von Null",
        chapter(
            "ai0_what", T, 1, "Was ist KI überhaupt?",
            "KI, maschinelles Lernen, Deep Learning und generative KI – was ist was, und wo steckt es im Alltag?",
            emptyList(),
            sec("Vier Begriffe, ineinander geschachtelt", """
                Künstliche Intelligenz (KI, englisch AI) ist der Oberbegriff: Computer sollen Aufgaben lösen, für die man bei Menschen Intelligenz bräuchte – sehen, sprechen, planen, entscheiden. Darin steckt das maschinelle Lernen (ML): Statt Regeln von Hand zu programmieren, lernt der Computer sie aus Beispielen. Darin wiederum steckt Deep Learning (DL): maschinelles Lernen mit tiefen neuronalen Netzen aus vielen Schichten. Und ein Teil davon ist generative KI (GenAI): Modelle, die neue Inhalte erzeugen – Text, Bilder, Musik, Code.

                • KI ⊃ Maschinelles Lernen ⊃ Deep Learning ⊃ Generative KI (Sprachmodelle wie ChatGPT, Bildgeneratoren)
                • Nicht jede KI lernt: Ein Schachprogramm mit fest einprogrammierter Suche ist KI ohne ML.
                • Nicht jedes ML ist Deep Learning: Ein Entscheidungsbaum, der Kreditanträge bewertet, ist ML ohne neuronales Netz.

                > Merksatz: KI ist das Ziel, maschinelles Lernen der wichtigste Weg dorthin, Deep Learning die zurzeit stärkste Methode.
            """),
            sec("Klassisches Programmieren gegen Lernen", """
                Klassisch schreibt ein Mensch die Regel: „Wenn die E-Mail ‚Gewinn‘ und ‚Konto‘ enthält, dann Spam.“ Das bricht, sobald Spammer andere Wörter benutzen. Beim maschinellen Lernen gibt man dem Computer tausende E-Mails mit der Markierung Spam/kein Spam, und ein Lernverfahren findet selbst heraus, welche Muster zählen.

                • Klassisch: Daten + Regeln → Antworten
                • Maschinelles Lernen: Daten + Antworten → Regeln (das Modell)
                • Danach wendet man das gelernte Modell auf neue Daten an: Vorhersage oder Inferenz.

                Für dich als Physiker: Das ist wie ein Fit. Man hat Messpunkte und eine Modellfunktion mit Parametern und stellt die Parameter so ein, dass die Kurve passt. Der Unterschied: Im ML hat das Modell oft Millionen bis Milliarden Parameter, und man interessiert sich nicht für die Parameter selbst, sondern nur dafür, wie gut neue Punkte vorhergesagt werden.
            """,
                fm("Lernen als Fit", "Modell ŷ = f_θ(x); Training: θ so wählen, dass ŷ ≈ y")
            ),
            sec("Wo dir KI täglich begegnet", """
                • Handy: Gesichtsentsperrung (Bilderkennung), Autokorrektur und Wortvorschläge (Sprachmodell), Sprachassistent (Spracherkennung)
                • Streaming und Shops: „Das könnte dir auch gefallen“ (Empfehlungssystem)
                • Bank: Betrugserkennung bei Kartenzahlungen (Anomalieerkennung)
                • E-Mail: Spamfilter (Klassifikation)
                • Navigation: Ankunftszeit-Schätzung (Regression), Routenplanung (Suche und Optimierung)
                • Medizin: Tumorerkennung in Röntgenbildern (Deep Learning)
                • Diese App: Maximus ist ein kleines Sprachmodell, das komplett auf deinem Handy läuft.
            """),
            sec("Eine Mini-Geschichte der KI", """
                • 1950: Alan Turing fragt „Können Maschinen denken?“ und schlägt den Turing-Test vor.
                • 1956: Dartmouth-Konferenz – der Begriff „Artificial Intelligence“ entsteht.
                • 1958: Rosenblatts Perzeptron, das erste lernende künstliche Neuron.
                • 1970er und späte 1980er: „KI-Winter“ – zu große Versprechen, zu wenig Rechenleistung.
                • 1986: Backpropagation macht mehrschichtige Netze trainierbar.
                • 1997: Deep Blue schlägt Kasparow im Schach.
                • 2012: AlexNet gewinnt ImageNet mit großem Abstand – Beginn des Deep-Learning-Booms (GPUs + große Datensätze).
                • 2016: AlphaGo schlägt Lee Sedol im Go.
                • 2017: „Attention Is All You Need“ – der Transformer.
                • 2022: ChatGPT bringt große Sprachmodelle zu allen Menschen.

                > Der Durchbruch kam nicht durch eine einzige Idee, sondern durch drei Zutaten gleichzeitig: viele Daten, viel Rechenleistung (GPUs) und bessere Algorithmen.
            """),
            sec("Schwache und starke KI", """
                Alle heutigen Systeme sind „schwache“ oder „enge“ KI (narrow AI): Sie sind in einer Aufgabe gut – Bilder sortieren, Text fortsetzen –, aber haben kein allgemeines Verständnis der Welt. „Starke KI“ oder AGI (Artificial General Intelligence) wäre ein System, das jede intellektuelle Aufgabe eines Menschen lernen kann. Ob und wann das kommt, ist offen. Für dein Studium und deine Arbeit zählt: Moderne Modelle sind extrem nützliche Werkzeuge, die man verstehen, prüfen und verantwortungsvoll einsetzen muss.
            """)
        ),
        chapter(
            "ai0_data", T, 1, "Daten: der Rohstoff",
            "Beispiele, Merkmale, Labels, Datentypen, Tabellen, Datenqualität – warum gute Daten wichtiger sind als ein schlauer Algorithmus.",
            emptyList(),
            sec("Die Datentabelle", """
                Fast alles im klassischen maschinellen Lernen beginnt mit einer Tabelle. Jede Zeile ist ein Beispiel (auch: Datenpunkt, Instanz, Beobachtung, englisch sample). Jede Spalte ist ein Merkmal (Feature, Attribut, unabhängige Variable). Eine besondere Spalte ist das Ziel: das Label oder die abhängige Variable – das, was das Modell vorhersagen soll.

                Beispiel Hauspreise: Zeilen sind Häuser; Merkmale sind Wohnfläche, Zimmerzahl, Baujahr, Stadtteil; das Label ist der Verkaufspreis.

                • X: die Merkmalsmatrix mit N Zeilen (Beispiele) und d Spalten (Merkmale)
                • y: der Vektor der Labels, ein Wert pro Beispiel
                • Ein Beispiel ist ein Vektor x ∈ ℝ^d – geometrisch ein Punkt im d-dimensionalen Merkmalsraum.
            """,
                fm("Datensatz", "D = {(x₁, y₁), …, (x_N, y_N)}, xᵢ ∈ ℝ^d"),
                fm("Merkmalsmatrix", "X ∈ ℝ^{N×d}, y ∈ ℝ^N")
            ),
            sec("Datentypen", """
                • Numerisch, kontinuierlich: Temperatur, Gewicht, Preis
                • Numerisch, diskret: Anzahl Zimmer, Anzahl Klicks
                • Kategorisch, nominal: Farbe, Stadt, Marke (keine Reihenfolge)
                • Kategorisch, ordinal: Schulnote, Kleidergröße S/M/L (mit Reihenfolge)
                • Unstrukturiert: Text, Bilder, Audio, Video – hier lernt Deep Learning die Merkmale meist selbst.
                • Zeitreihen: Messwerte mit Zeitstempel, Reihenfolge ist wichtig (Aktienkurse, Sensoren)

                Modelle rechnen nur mit Zahlen. Kategorien muss man deshalb umwandeln, meist per One-Hot-Kodierung: Aus „Stadt ∈ {Berlin, Köln, Wien}“ werden drei Spalten mit 0 oder 1.
            """),
            sec("Datenqualität: Garbage in, garbage out", """
                Ein Modell kann nur lernen, was in den Daten steckt. Typische Probleme:

                • Fehlende Werte (leere Zellen) – löschen, mit Mittelwert oder Median füllen, oder ein eigenes Merkmal „fehlt“
                • Ausreißer und Tippfehler (ein Haus mit 9000 m²)
                • Duplikate
                • Falsche Labels (ein Hund, als Katze markiert)
                • Verzerrung (Bias): Die Daten bilden die Wirklichkeit schief ab, z. B. nur Fotos heller Haut für eine Hautkrebs-Erkennung.
                • Verteilungsverschiebung: Die Daten beim Einsatz sehen anders aus als beim Training.

                > In echten Projekten stecken oft 60–80 % der Arbeit in Sammeln, Säubern und Verstehen der Daten – nicht im Modell.
            """),
            sec("Erste Schritte mit pandas", """
                In Python ist pandas das Standardwerkzeug für Tabellen. Ein DataFrame ist eine Tabelle, eine Series eine Spalte.

                $ import pandas as pd
                $ df = pd.read_csv("haeuser.csv")     # Tabelle laden
                $ df.head()                           # erste 5 Zeilen ansehen
                $ df.info()                           # Spalten, Datentypen, fehlende Werte
                $ df.describe()                       # Mittelwert, Std., Min, Max je Spalte
                $ df["preis"].hist()                  # Verteilung einer Spalte
                $ X = df[["flaeche", "zimmer"]]       # Merkmale
                $ y = df["preis"]                     # Label

                Diese fünf Befehle sind der erste Blick auf jeden neuen Datensatz: erst ansehen, dann modellieren.
            """)
        ),
        chapter(
            "ai0_model", T, 1, "Was ist ein Modell, was ist Training?",
            "Modell = Funktion mit Stellschrauben; Verlust misst den Fehler; Training dreht an den Schrauben; Inferenz nutzt das Ergebnis.",
            emptyList(),
            sec("Ein Modell ist eine Funktion mit Stellschrauben", """
                Ein Modell nimmt Merkmale x und liefert eine Vorhersage ŷ. Das einfachste Beispiel ist eine Gerade: ŷ = w·x + b. Die Stellschrauben w (Gewicht, Steigung) und b (Bias, Achsenabschnitt) heißen Parameter. Ein großes Sprachmodell ist im Prinzip dasselbe – nur mit Milliarden Stellschrauben.

                • Parameter: werden beim Training gelernt (Gewichte, Bias).
                • Hyperparameter: legt der Mensch vor dem Training fest (Lernrate, Anzahl Bäume, Netzgröße, k bei k-Means).
            """,
                fm("Lineares Modell", "ŷ = w·x + b"),
                fm("Mehrere Merkmale", "ŷ = w₁x₁ + w₂x₂ + … + w_dx_d + b = wᵀx + b")
            ),
            sec("Der Verlust: eine Zahl für „wie falsch“", """
                Damit der Computer weiß, ob eine Einstellung gut ist, braucht er eine Verlustfunktion (Loss, Kostenfunktion). Sie vergleicht Vorhersage ŷ und Wahrheit y und gibt eine Zahl aus – je kleiner, desto besser. Für Zahlenvorhersagen nimmt man meist den mittleren quadratischen Fehler (MSE).

                Beispiel: Echte Preise 200, 300 (Tausend €), Vorhersagen 210, 280. Fehler: −10 und +20 (bzw. 10 und −20). MSE = (10² + 20²)/2 = 250.

                > Training heißt: die Parameter so lange verändern, bis der Verlust auf den Trainingsdaten klein ist.
            """,
                fm("Mittlerer quadratischer Fehler", "MSE = (1/N) Σᵢ (ŷᵢ − yᵢ)²"),
                fm("Trainingsziel", "θ* = argmin_θ L(θ)")
            ),
            sec("Wie dreht man an den Schrauben? Gradientenabstieg in einem Bild", """
                Stell dir den Verlust als Gebirgslandschaft vor: Jede Position ist eine Parametereinstellung, die Höhe ist der Verlust. Du stehst im Nebel und willst ins Tal. Du spürst nur die Neigung unter deinen Füßen – den Gradienten – und gehst einen kleinen Schritt bergab. Wiederholen, bis es nicht mehr bergab geht. Die Schrittweite heißt Lernrate η.

                • Zu große Lernrate: Du springst über das Tal hinweg und landest höher (Divergenz).
                • Zu kleine Lernrate: Du kommst an, aber erst nach Ewigkeiten.
                • Probier es aus: AI-Spielwiese → „Gradientenabstieg“.
            """,
                fm("Gradientenschritt", "θ ← θ − η · ∂L/∂θ")
            ),
            sec("Training, Validierung, Test, Inferenz", """
                • Training: Parameter lernen auf der Trainingsmenge (typisch 60–80 % der Daten).
                • Validierung: Hyperparameter wählen, Modelle vergleichen (10–20 %).
                • Test: Ganz am Ende, einmal, als ehrliche Note (10–20 %). Wie eine Klausur mit unbekannten Aufgaben.
                • Inferenz: Das fertige Modell im Einsatz auf neuen Daten.

                $ from sklearn.model_selection import train_test_split
                $ from sklearn.linear_model import LinearRegression
                $ X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
                $ model = LinearRegression()
                $ model.fit(X_train, y_train)          # Training
                $ y_pred = model.predict(X_test)       # Inferenz
                $ print(model.coef_, model.intercept_) # gelernte w und b

                > Die scikit-learn-Formel für fast jedes Modell: erzeugen → fit → predict → bewerten.
            """)
        ),
        chapter(
            "ai0_types", T, 1, "Die Lernarten",
            "Überwacht, unüberwacht, halbüberwacht, selbstüberwacht und bestärkend – mit Alltagsbeispielen und der Frage, welche wann passt.",
            emptyList(),
            sec("Überwachtes Lernen (supervised)", """
                Man hat Beispiele mit richtiger Antwort – wie ein Schüler mit Lösungsheft. Zwei Unterarten:

                • Regression: Die Antwort ist eine Zahl. Hauspreis, Temperatur morgen, Ankunftszeit, e1RM nach 12 Wochen.
                • Klassifikation: Die Antwort ist eine Kategorie. Spam/kein Spam, Ziffer 0–9, gutartig/bösartig.

                > Frage dich: Ist das, was ich vorhersagen will, eine Zahl (Regression) oder eine Schublade (Klassifikation)?
            """),
            sec("Unüberwachtes Lernen (unsupervised)", """
                Es gibt keine Antworten, nur Daten. Das Modell soll selbst Struktur finden.

                • Clustering: ähnliche Beispiele gruppieren – Kundensegmente, Gentypen, Themen in Dokumenten.
                • Dimensionsreduktion: viele Merkmale auf wenige wichtige zusammendrücken – zum Visualisieren oder Entrauschen.
                • Assoziationsregeln: „Wer Chips kauft, kauft oft auch Bier.“
                • Anomalieerkennung: das Ungewöhnliche finden.
            """),
            sec("Halb- und selbstüberwachtes Lernen", """
                Halbüberwacht: wenige Beispiele mit Label, sehr viele ohne. Man nutzt die unmarkierten, um die Struktur der Daten zu lernen.

                Selbstüberwacht: Die Labels entstehen automatisch aus den Daten selbst. Ein Sprachmodell lernt, das nächste Wort vorherzusagen – das richtige nächste Wort steht ja im Text. So kann man auf dem ganzen Internet trainieren, ohne dass ein Mensch etwas markiert. Das ist das Geheimnis großer Sprachmodelle.
            """),
            sec("Bestärkendes Lernen (reinforcement learning)", """
                Ein Agent handelt in einer Umgebung und bekommt Belohnungen oder Strafen – wie ein Hund, der für Sitz ein Leckerli bekommt. Er lernt eine Strategie (Policy), die die Summe der Belohnungen maximiert. Beispiele: AlphaGo, Roboter, die laufen lernen, Feinabstimmung von Chatbots auf menschliche Vorlieben (RLHF).

                • Probier es aus: AI-Spielwiese → „Q-Learning-Labyrinth“.
            """),
            sec("Welche Lernart passt?", """
                • Ich habe Beispiele mit richtiger Antwort → überwacht.
                • Ich will Gruppen oder Muster entdecken, habe aber keine Antworten → unüberwacht.
                • Ich habe riesige Mengen Rohdaten (Text, Bilder) → selbstüberwacht vortrainieren, dann mit wenigen Labels feinabstimmen.
                • Ein System muss Entscheidungen nacheinander treffen und bekommt erst später Rückmeldung → bestärkend.
            """)
        ),
        chapter(
            "ai0_math", T, 1, "Der Mathe-Werkzeugkasten",
            "Vektoren, Skalarprodukt, Matrizen, Ableitung und Gradient, Wahrscheinlichkeit, Mittelwert und Varianz – nur das, was ML wirklich braucht.",
            emptyList(),
            sec("Vektoren und Skalarprodukt", """
                Ein Beispiel mit d Merkmalen ist ein Vektor x = (x₁, …, x_d). Das Skalarprodukt w·x = Σ wᵢxᵢ ist die wichtigste Rechenoperation im gesamten maschinellen Lernen: Jedes Neuron, jede lineare Regression, jede Attention berechnet Skalarprodukte. Geometrisch misst es, wie sehr zwei Vektoren in dieselbe Richtung zeigen.
            """,
                fm("Skalarprodukt", "w·x = Σᵢ wᵢxᵢ = |w||x| cos φ"),
                fm("Euklidische Distanz", "d(x, z) = √(Σᵢ (xᵢ − zᵢ)²)"),
                fm("Kosinusähnlichkeit", "cos φ = w·x / (|w||x|)")
            ),
            sec("Matrizen: viele Skalarprodukte auf einmal", """
                Eine Matrix W mit m Zeilen und d Spalten bildet einen Vektor x ∈ ℝ^d auf Wx ∈ ℝ^m ab: m Skalarprodukte gleichzeitig. Eine Schicht eines neuronalen Netzes ist genau das, plus Bias und eine Nichtlinearität. GPUs sind so schnell für KI, weil sie Matrixmultiplikationen massiv parallel ausführen.
            """,
                fm("Matrix-Vektor-Produkt", "(Wx)ᵢ = Σⱼ Wᵢⱼ xⱼ"),
                fm("Neuronale Schicht", "h = σ(Wx + b)")
            ),
            sec("Ableitung und Gradient", """
                Die Ableitung sagt, wie stark sich eine Funktion ändert, wenn man ihren Eingang ein wenig verändert. Bei vielen Parametern sammelt der Gradient ∇L alle partiellen Ableitungen in einem Vektor; er zeigt in Richtung des steilsten Anstiegs. Gradientenabstieg geht in die Gegenrichtung. Die Kettenregel – Ableitungen verketteter Funktionen multiplizieren sich – ist die Grundlage von Backpropagation.
            """,
                fm("Gradient", "∇L = (∂L/∂θ₁, …, ∂L/∂θₙ)"),
                fm("Kettenregel", "d/dx f(g(x)) = f′(g(x))·g′(x)"),
                fm("Ableitung des MSE nach w", "∂/∂w (wx − y)² = 2(wx − y)·x")
            ),
            sec("Statistik und Wahrscheinlichkeit", """
                • Mittelwert μ: der Schwerpunkt der Daten.
                • Varianz σ² und Standardabweichung σ: wie weit die Daten streuen.
                • Normalverteilung: die Glockenkurve; viele Messgrößen sind näherungsweise normalverteilt.
                • Bedingte Wahrscheinlichkeit P(A|B): Wahrscheinlichkeit von A, wenn B bekannt ist – Grundlage von Naive Bayes.
                • Klassifikatoren geben meist Wahrscheinlichkeiten aus; „Klasse“ heißt dann: die mit der höchsten Wahrscheinlichkeit.
            """,
                fm("Mittelwert", "μ = (1/N) Σᵢ xᵢ"),
                fm("Varianz", "σ² = (1/N) Σᵢ (xᵢ − μ)²"),
                fm("Standardisierung (z-Wert)", "z = (x − μ)/σ"),
                fm("Satz von Bayes", "P(A|B) = P(B|A)·P(A)/P(B)")
            ),
            sec("NumPy: Mathe in Python", """
                $ import numpy as np
                $ x = np.array([1.0, 2.0, 3.0])
                $ w = np.array([0.5, -1.0, 2.0])
                $ print(w @ x)                  # Skalarprodukt: 0.5 - 2 + 6 = 4.5
                $ W = np.random.randn(4, 3)     # 4×3-Matrix
                $ h = np.maximum(0, W @ x)      # eine Schicht mit ReLU
                $ print(x.mean(), x.std())      # Mittelwert, Standardabweichung

                > Als Physiker kennst du das alles schon. In ML heißt es nur anders: Ein Feature-Vektor ist ein Zustandsvektor, ein Gewicht ein Fitparameter, der Verlust ein χ².
            """)
        ),
        chapter(
            "ai0_workflow", T, 1, "Der Ablauf eines ML-Projekts",
            "Von der Frage zum Modell im Einsatz: Problem, Daten, Aufbereitung, Modellwahl, Training, Bewertung, Bereitstellung, Überwachung (CRISP-DM und IBM-Methodik).",
            emptyList(),
            sec("Die acht Schritte", """
                • 1. Problem verstehen: Was soll vorhergesagt werden, wie wird Erfolg gemessen, was kostet ein Fehler?
                • 2. Daten sammeln: Welche Daten gibt es, sind sie erlaubt (Datenschutz), sind sie repräsentativ?
                • 3. Daten verstehen (EDA, explorative Datenanalyse): Verteilungen, Zusammenhänge, Auffälligkeiten.
                • 4. Daten aufbereiten: säubern, fehlende Werte, Merkmale bauen, skalieren, kodieren.
                • 5. Modell wählen und trainieren: mit einer einfachen Basislinie beginnen!
                • 6. Bewerten: auf Validierungs- und Testdaten, mit der passenden Metrik.
                • 7. Bereitstellen (Deployment): als App, API oder auf dem Gerät.
                • 8. Überwachen: Leistung im Betrieb messen, bei Drift neu trainieren.

                > Der Prozess ist ein Kreis, keine Linie: Fast immer springt man von Schritt 6 zurück zu 3 oder 4.
            """),
            sec("CRISP-DM und die IBM-Datenwissenschafts-Methodik", """
                CRISP-DM (Cross-Industry Standard Process for Data Mining) ist der verbreitetste Standard: Business Understanding → Data Understanding → Data Preparation → Modeling → Evaluation → Deployment. Die IBM-Methodik von John Rollins zerlegt dasselbe feiner in zehn Fragen: von „Was ist das Problem?“ über analytischen Ansatz, Datenanforderungen, Sammlung, Verständnis, Aufbereitung, Modellierung und Bewertung bis zu Bereitstellung und Feedback.
            """),
            sec("Die Basislinie: das wichtigste Modell, das niemand baut", """
                Bevor man ein kompliziertes Modell trainiert, misst man eine dumme Basislinie: bei Regression „immer den Mittelwert vorhersagen“, bei Klassifikation „immer die häufigste Klasse“. Nur wenn das echte Modell deutlich besser ist, hat es etwas gelernt. Danach: einfaches Modell (lineare oder logistische Regression), dann erst Bäume, Ensembles, Netze.
            """),
            sec("Explorative Datenanalyse in vier Zeilen", """
                $ df.describe()                          # Kennzahlen
                $ df.isna().sum()                        # fehlende Werte je Spalte
                $ df.corr(numeric_only=True)             # Korrelationen
                $ pd.plotting.scatter_matrix(df)         # alle Paare als Streudiagramm
            """)
        ),
        chapter(
            "ai0_tools", T, 1, "Werkzeuge: Python, scikit-learn, Keras, PyTorch",
            "Das Ökosystem des IBM-Kurses: Jupyter, NumPy, pandas, Matplotlib, scikit-learn, Keras/TensorFlow, PyTorch, Hugging Face, Spark.",
            emptyList(),
            sec("Die Landkarte", """
                • Jupyter Notebook: interaktives Dokument mit Code, Text und Grafiken – die Werkbank der Datenwissenschaft.
                • NumPy: schnelle Arrays und lineare Algebra.
                • pandas: Tabellen laden, säubern, gruppieren.
                • Matplotlib / Seaborn: Diagramme.
                • scikit-learn: klassisches ML (Regression, Klassifikation, Clustering, Bewertung) mit einheitlicher fit/predict-Schnittstelle.
                • Keras (auf TensorFlow): neuronale Netze in wenigen Zeilen, ideal zum Einstieg.
                • PyTorch: flexibles Deep Learning, Standard in der Forschung und bei LLMs.
                • Hugging Face: vortrainierte Modelle, Datensätze, Tokenizer.
                • Apache Spark: ML auf großen Datenmengen, verteilt auf viele Rechner.
            """),
            sec("Die scikit-learn-Grammatik", """
                Jedes Modell in scikit-learn spricht dieselbe Sprache. Wer sie einmal kann, kann alle:

                $ from sklearn.preprocessing import StandardScaler
                $ from sklearn.neighbors import KNeighborsClassifier
                $ from sklearn.pipeline import make_pipeline
                $ from sklearn.metrics import accuracy_score
                $ model = make_pipeline(StandardScaler(), KNeighborsClassifier(n_neighbors=5))
                $ model.fit(X_train, y_train)
                $ print(accuracy_score(y_test, model.predict(X_test)))

                • Estimator: hat fit (lernen) und predict (vorhersagen), Klassifikatoren auch predict_proba.
                • Transformer: hat fit und transform (z. B. Skalierer, PCA).
                • Pipeline: hängt Transformer und Estimator zusammen, damit Vorverarbeitung nie Testdaten sieht.
            """),
            sec("Keras und PyTorch im Vergleich", """
                Keras (dasselbe Netz in drei Zeilen):

                $ model = keras.Sequential([layers.Dense(64, activation="relu"), layers.Dense(1)])
                $ model.compile(optimizer="adam", loss="mse")
                $ model.fit(X_train, y_train, epochs=20, validation_split=0.2)

                PyTorch (die Trainingsschleife schreibt man selbst):

                $ for xb, yb in loader:
                $     loss = loss_fn(model(xb), yb)
                $     optimizer.zero_grad()
                $     loss.backward()
                $     optimizer.step()

                > Keras ist bequemer, PyTorch durchsichtiger. Der IBM-Kurs nutzt beide – die Konzepte sind identisch.
            """)
        )
    )
}

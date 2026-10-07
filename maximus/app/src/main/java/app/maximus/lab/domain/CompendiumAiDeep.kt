package app.maximus.lab.domain

/** Deep learning with Keras and PyTorch (the IBM AI Engineering stack) and reinforcement learning. */
internal object CompendiumAiDeep {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "Deep Learning",
        chapter(
            "dl_neuron", T, 1, "Das künstliche Neuron und das Perzeptron",
            "Biologisches Vorbild, gewichtete Summe, Bias, Aktivierung, Perzeptron-Lernregel, das XOR-Problem und warum man Schichten braucht.",
            emptyList(),
            sec("Vom Nervenzell-Vorbild zur Formel", """
                Eine Nervenzelle sammelt Signale über ihre Dendriten, und wenn die Summe eine Schwelle überschreitet, „feuert“ sie. Das künstliche Neuron macht dasselbe mit Zahlen: Jeder Eingang xᵢ wird mit einem Gewicht wᵢ multipliziert (wie wichtig ist dieser Eingang?), alles wird addiert, ein Bias b verschiebt die Schwelle, und eine Aktivierungsfunktion entscheidet über die Ausgabe.

                Beispiel: Soll ich heute trainieren? x₁ = ausgeschlafen (1), x₂ = Muskelkater (1), x₃ = Wettkampf bald (1). Gewichte 2, −3, 4, Bias −1: z = 2 − 3 + 4 − 1 = 2 > 0 → ja.
            """,
                fm("Neuron", "a = φ(Σᵢ wᵢxᵢ + b) = φ(wᵀx + b)"),
                fm("Stufenfunktion (Perzeptron)", "φ(z) = 1 für z ≥ 0, sonst 0")
            ),
            sec("Die Perzeptron-Lernregel (Rosenblatt 1958)", """
                Für jedes Trainingsbeispiel: Vorhersage berechnen. Ist sie richtig, nichts tun. Ist sie falsch, die Gewichte ein Stück in Richtung des richtigen Ergebnisses schieben. Bei linear trennbaren Daten findet das Perzeptron garantiert eine trennende Gerade (Konvergenzsatz). Spiel: AI-Spielwiese → „Perzeptron“.
            """,
                fm("Perzeptron-Update", "w ← w + η (y − ŷ) x,  b ← b + η (y − ŷ)")
            ),
            sec("Das XOR-Problem", """
                XOR ist 1, wenn genau einer von zwei Eingängen 1 ist. Die vier Punkte (0,0)→0, (1,1)→0, (0,1)→1, (1,0)→1 lassen sich durch keine Gerade trennen. Minsky und Papert zeigten 1969, dass ein einzelnes Perzeptron daran scheitert – ein Grund für den ersten KI-Winter. Die Lösung: mehrere Schichten. Eine versteckte Schicht mit zwei Neuronen (OR und NAND) und ein Ausgangsneuron (AND) berechnen XOR.

                > Eine Schicht kann nur gerade Grenzen ziehen. Mehrere Schichten mit Nichtlinearität können beliebig gekrümmte Grenzen ziehen.
            """),
            sec("Aktivierungsfunktionen", """
                Die Stufenfunktion hat überall Ableitung null – damit kann man nicht mit Gradienten lernen. Moderne Netze nutzen glatte oder stückweise lineare Funktionen:

                • Sigmoid: (0, 1), für Wahrscheinlichkeiten am Ausgang; sättigt, Gradienten verschwinden.
                • tanh: (−1, 1), zentriert, sättigt auch.
                • ReLU: max(0, z) – einfach, schnell, Standard in versteckten Schichten.
                • Leaky ReLU, GELU, SiLU/Swish: weiche Varianten, GELU ist Standard in Transformern.
                • Softmax: am Ausgang für mehrere Klassen.
            """,
                fm("ReLU", "ReLU(z) = max(0, z)"),
                fm("tanh", "tanh(z) = (e^z − e^{−z})/(e^z + e^{−z})"),
                fm("Ableitung Sigmoid", "σ′(z) = σ(z)(1 − σ(z))")
            )
        ),
        chapter(
            "dl_backprop_numbers", T, 2, "Backpropagation mit echten Zahlen",
            "Ein Mini-Netz Schritt für Schritt: Vorwärtsrechnung, Verlust, Kettenregel rückwärts, Gewichts-Update – einmal komplett von Hand.",
            emptyList(),
            sec("Das Mini-Netz", """
                Ein Eingang x = 1, ein verstecktes Neuron mit Gewicht w₁ = 0,5 und ReLU, ein Ausgangsneuron mit Gewicht w₂ = 2 (linear), Zielwert y = 3, Verlust L = ½(ŷ − y)². Biases lassen wir der Übersicht halber weg.

                Vorwärts:
                • z₁ = w₁·x = 0,5
                • h = ReLU(z₁) = 0,5
                • ŷ = w₂·h = 1
                • L = ½(1 − 3)² = 2
            """,
                fm("Vorwärts", "z₁ = w₁x, h = ReLU(z₁), ŷ = w₂h, L = ½(ŷ − y)²")
            ),
            sec("Rückwärts mit der Kettenregel", """
                Wir wollen ∂L/∂w₂ und ∂L/∂w₁. Wir gehen vom Verlust rückwärts und multiplizieren lokale Ableitungen:

                • ∂L/∂ŷ = ŷ − y = −2
                • ∂L/∂w₂ = ∂L/∂ŷ · h = −2 · 0,5 = −1
                • ∂L/∂h = ∂L/∂ŷ · w₂ = −2 · 2 = −4
                • ∂L/∂z₁ = ∂L/∂h · ReLU′(z₁) = −4 · 1 = −4
                • ∂L/∂w₁ = ∂L/∂z₁ · x = −4

                Update mit η = 0,1: w₂ = 2 − 0,1·(−1) = 2,1 und w₁ = 0,5 − 0,1·(−4) = 0,9. Neue Vorwärtsrechnung: ŷ = 2,1 · 0,9 = 1,89, L ≈ 0,62. Der Verlust ist von 2 auf 0,62 gefallen.

                > Backpropagation ist nichts anderes als die Kettenregel, geschickt organisiert: Jeder Zwischenwert wird einmal berechnet und wiederverwendet.
            """,
                fm("Kettenregel im Netz", "∂L/∂w₁ = ∂L/∂ŷ · ∂ŷ/∂h · ∂h/∂z₁ · ∂z₁/∂w₁"),
                fm("Fehlersignal einer Schicht", "δ⁽ˡ⁾ = (W⁽ˡ⁺¹⁾ᵀ δ⁽ˡ⁺¹⁾) ⊙ φ′(z⁽ˡ⁾)"),
                fm("Gewichtsgradient", "∂L/∂W⁽ˡ⁾ = δ⁽ˡ⁾ a⁽ˡ⁻¹⁾ᵀ")
            ),
            sec("Warum verschwinden Gradienten?", """
                Mit Sigmoid ist φ′ höchstens 0,25. In einem Netz mit 20 Schichten multiplizieren sich 20 solche Faktoren: 0,25²⁰ ≈ 10⁻¹². Die vorderen Schichten lernen praktisch nichts. ReLU (Ableitung 1 im aktiven Bereich), gute Initialisierung (He, Xavier), Normalisierung und Residualverbindungen haben dieses Problem gelöst. Das Gegenteil, explodierende Gradienten, zähmt man mit Gradient Clipping.
            """),
            sec("Autograd macht es automatisch", """
                In PyTorch merkt sich jeder Tensor, wie er berechnet wurde (Rechengraph); loss.backward() wendet die Kettenregel rückwärts an:

                $ import torch
                $ w1 = torch.tensor(0.5, requires_grad=True); w2 = torch.tensor(2.0, requires_grad=True)
                $ y_hat = w2 * torch.relu(w1 * 1.0)
                $ loss = 0.5 * (y_hat - 3.0) ** 2
                $ loss.backward()
                $ print(w1.grad, w2.grad)     # tensor(-4.) tensor(-1.)
            """)
        ),
        chapter(
            "dl_keras", T, 2, "Keras: Netze in wenigen Zeilen",
            "Sequential-Modell, Dense-Schichten, compile (Optimierer, Verlust, Metriken), fit, evaluate, predict, Callbacks, ein vollständiges Beispiel.",
            emptyList(),
            sec("Die drei Schritte", """
                • Bauen: Schichten stapeln (Sequential) oder als Graph verbinden (Functional API).
                • Kompilieren: Optimierer, Verlust und Metriken festlegen.
                • Trainieren: fit mit Epochen und Batchgröße, Validierungsdaten beobachten.

                $ from tensorflow import keras
                $ from keras import layers
                $ model = keras.Sequential([
                $     layers.Input(shape=(8,)),
                $     layers.Dense(64, activation="relu"),
                $     layers.Dropout(0.2),
                $     layers.Dense(32, activation="relu"),
                $     layers.Dense(1, activation="sigmoid"),
                $ ])
                $ model.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])
                $ history = model.fit(X_train, y_train, epochs=30, batch_size=32, validation_split=0.2)
                $ model.evaluate(X_test, y_test)
            """),
            sec("Welcher Verlust, welche letzte Schicht?", """
                • Regression: letzte Schicht Dense(1) ohne Aktivierung, Verlust „mse“ oder „mae“.
                • Binäre Klassifikation: Dense(1, sigmoid), Verlust „binary_crossentropy“.
                • Mehrere Klassen, Labels als Zahlen: Dense(K, softmax), „sparse_categorical_crossentropy“.
                • Mehrere Klassen, Labels one-hot: Dense(K, softmax), „categorical_crossentropy“.
            """),
            sec("Epochen, Batches, Schritte", """
                Eine Epoche ist ein Durchlauf durch alle Trainingsdaten. Die Daten werden in Batches (z. B. 32 Beispiele) zerlegt; pro Batch gibt es einen Gradientenschritt. Bei 10 000 Beispielen und Batchgröße 32 sind das 313 Schritte pro Epoche.
            """,
                fm("Schritte pro Epoche", "⌈N / B⌉")
            ),
            sec("Parameter zählen und Callbacks", """
                model.summary() zeigt die Parameter: Eine Dense-Schicht von n_in auf n_out hat n_in·n_out + n_out Parameter. Im Beispiel: 8·64 + 64 = 576, 64·32 + 32 = 2080, 32 + 1 = 33.

                Callbacks greifen ins Training ein:

                $ cb = [keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True),
                $       keras.callbacks.ModelCheckpoint("best.keras", save_best_only=True)]
                $ model.fit(X_train, y_train, validation_split=0.2, epochs=200, callbacks=cb)
            """,
                fm("Parameter einer Dense-Schicht", "P = n_in·n_out + n_out")
            )
        ),
        chapter(
            "dl_pytorch", T, 2, "PyTorch: Tensoren, Autograd, Trainingsschleife",
            "Tensoren und Geräte, nn.Module, Dataset und DataLoader, die Standard-Trainingsschleife, Evaluation ohne Gradienten, Speichern und Laden.",
            emptyList(),
            sec("Tensoren", """
                Ein Tensor ist ein mehrdimensionales Array wie in NumPy, kann aber auf der GPU liegen und merkt sich Rechenwege für Gradienten. Form (shape) ist alles: Ein Bild-Batch hat z. B. die Form (Batch, Kanäle, Höhe, Breite) = (32, 3, 224, 224).

                $ import torch
                $ x = torch.randn(32, 10)                 # 32 Beispiele, 10 Merkmale
                $ device = "cuda" if torch.cuda.is_available() else "cpu"
                $ x = x.to(device)
                $ print(x.shape, x.dtype)
            """),
            sec("Ein Modell als Klasse", """
                $ import torch.nn as nn
                $ class Net(nn.Module):
                $     def __init__(self):
                $         super().__init__()
                $         self.layers = nn.Sequential(nn.Linear(10, 64), nn.ReLU(), nn.Linear(64, 1))
                $     def forward(self, x):
                $         return self.layers(x)
                $ model = Net().to(device)

                forward beschreibt die Vorwärtsrechnung; die Rückwärtsrechnung erzeugt Autograd automatisch.
            """),
            sec("Daten laden", """
                Ein Dataset liefert einzelne Beispiele (Länge und Zugriff per Index), ein DataLoader bündelt sie zu gemischten Batches:

                $ from torch.utils.data import TensorDataset, DataLoader
                $ ds = TensorDataset(torch.tensor(X_train, dtype=torch.float32), torch.tensor(y_train, dtype=torch.float32))
                $ loader = DataLoader(ds, batch_size=32, shuffle=True)
            """),
            sec("Die Trainingsschleife – auswendig lernen!", """
                $ loss_fn = nn.MSELoss()
                $ optimizer = torch.optim.Adam(model.parameters(), lr=1e-3)
                $ for epoch in range(20):
                $     model.train()
                $     for xb, yb in loader:
                $         xb, yb = xb.to(device), yb.to(device)
                $         pred = model(xb).squeeze(1)      # 1. vorwärts
                $         loss = loss_fn(pred, yb)         # 2. Verlust
                $         optimizer.zero_grad()            # 3. alte Gradienten löschen
                $         loss.backward()                  # 4. rückwärts
                $         optimizer.step()                 # 5. Gewichte anpassen

                > Fünf Schritte: vorwärts, Verlust, zero_grad, backward, step. Vergisst man zero_grad, addieren sich die Gradienten über die Batches.
            """),
            sec("Auswerten und speichern", """
                $ model.eval()
                $ with torch.no_grad():                  # keine Gradienten, spart Speicher
                $     test_pred = model(X_test_t)
                $ torch.save(model.state_dict(), "model.pt")
                $ model.load_state_dict(torch.load("model.pt"))

                model.eval() schaltet Dropout ab und lässt BatchNorm die gelernten Statistiken nutzen.
            """)
        ),
        chapter(
            "dl_cnn", T, 2, "Faltungsnetze (CNN) ausführlich",
            "Faltung als Mustersuche, Filter und Feature Maps, Padding und Stride, Pooling, Kanäle, Parameterzahl, von LeNet bis ResNet, Datenaugmentation.",
            emptyList(),
            sec("Faltung: ein wandernder Mustersucher", """
                Ein Filter (Kernel) ist ein kleines Gewichtsfenster, z. B. 3×3. Er gleitet über das Bild, und an jeder Stelle wird das Skalarprodukt aus Fenster und Bildausschnitt berechnet. Das Ergebnis ist eine Feature Map: Sie leuchtet dort, wo das Muster des Filters vorkommt (Kanten, Ecken, Texturen). Dasselbe Gewicht wird an allen Stellen benutzt (Gewichtsteilung) – eine Katze ist eine Katze, egal wo im Bild.

                • Frühe Schichten: Kanten und Farbflecken.
                • Mittlere Schichten: Texturen, Teile (Augen, Räder).
                • Späte Schichten: ganze Objekte.
            """,
                fm("Diskrete Faltung (Kreuzkorrelation)", "S(i, j) = Σₘ Σₙ I(i + m, j + n)·K(m, n)")
            ),
            sec("Größen berechnen", """
                Padding (Rand mit Nullen auffüllen) erhält die Bildgröße, Stride (Schrittweite) verkleinert sie. Ein Pooling-Schritt (meist Max-Pooling 2×2) halbiert Höhe und Breite und macht das Netz unempfindlicher gegen kleine Verschiebungen.

                Beispiel: Eingabe 32×32×3, 16 Filter 3×3, Padding 1, Stride 1 → 32×32×16. Parameter: (3·3·3 + 1)·16 = 448. Eine vollverbundene Schicht für dieselbe Abbildung bräuchte 3072·16384 ≈ 50 Millionen.
            """,
                fm("Ausgabegröße", "n_out = ⌊(n_in + 2p − k)/s⌋ + 1"),
                fm("Parameter einer Faltungsschicht", "P = (k·k·C_in + 1)·C_out")
            ),
            sec("Berühmte Architekturen", """
                • LeNet-5 (1998): Ziffern auf Schecks.
                • AlexNet (2012): ReLU, Dropout, GPUs – der Durchbruch auf ImageNet.
                • VGG (2014): viele kleine 3×3-Filter hintereinander.
                • ResNet (2015): Residualverbindungen y = x + F(x) erlauben über 100 Schichten.
                • EfficientNet, MobileNet: effizient für Handys.
                • Vision Transformer (2020): Bilder als Folge von Kacheln (Patches), Attention statt Faltung.
            """),
            sec("Ein CNN in Keras", """
                $ model = keras.Sequential([
                $     layers.Input(shape=(28, 28, 1)),
                $     layers.Conv2D(32, 3, activation="relu"), layers.MaxPooling2D(),
                $     layers.Conv2D(64, 3, activation="relu"), layers.MaxPooling2D(),
                $     layers.Flatten(), layers.Dense(128, activation="relu"),
                $     layers.Dense(10, activation="softmax"),
                $ ])
                $ model.compile("adam", "sparse_categorical_crossentropy", metrics=["accuracy"])

                Datenaugmentation (zufällig spiegeln, drehen, zuschneiden, Helligkeit ändern) vervielfacht die Trainingsdaten und ist bei Bildern eine der wirksamsten Regularisierungen.
            """)
        ),
        chapter(
            "dl_rnn", T, 2, "Rekurrente Netze, LSTM und GRU",
            "Gedächtnis über die Zeit, Entfalten und Backpropagation through Time, verschwindende Gradienten, LSTM-Tore, GRU, Seq2Seq, Zeitreihen mit Keras.",
            emptyList(),
            sec("Ein Netz mit Gedächtnis", """
                Ein rekurrentes Netz (RNN) liest eine Folge Element für Element und trägt einen versteckten Zustand hₜ weiter – eine Zusammenfassung von allem bisher Gelesenen. Dieselben Gewichte werden in jedem Zeitschritt benutzt. Zum Trainieren entfaltet man das Netz über die Zeit und wendet Backpropagation an (BPTT).
            """,
                fm("RNN-Zustand", "hₜ = tanh(W_h hₜ₋₁ + W_x xₜ + b)"),
                fm("Ausgabe", "ŷₜ = W_y hₜ + c")
            ),
            sec("LSTM: Tore gegen das Vergessen", """
                Über viele Schritte verschwinden die Gradienten in einem einfachen RNN – es vergisst, was vor 20 Wörtern stand. Das LSTM (Long Short-Term Memory, 1997) hat eine Zellspur cₜ, die Information fast ungestört weitertransportiert, und drei Tore mit Werten zwischen 0 und 1:

                • Vergessenstor fₜ: was aus dem Gedächtnis gelöscht wird
                • Eingangstor iₜ: was Neues aufgenommen wird
                • Ausgangstor oₜ: was nach außen gegeben wird

                Die GRU ist eine schlankere Variante mit zwei Toren und oft gleich gut.
            """,
                fm("Zellzustand", "cₜ = fₜ ⊙ cₜ₋₁ + iₜ ⊙ c̃ₜ"),
                fm("Versteckter Zustand", "hₜ = oₜ ⊙ tanh(cₜ)"),
                fm("Tor", "fₜ = σ(W_f [hₜ₋₁, xₜ] + b_f)")
            ),
            sec("Einsatzformen", """
                • Viele-zu-eins: Satz → Stimmung (Sentiment), Zeitreihe → Prognose.
                • Viele-zu-viele (gleich lang): Wortarten taggen.
                • Seq2Seq (Encoder-Decoder): Übersetzung, Zusammenfassung – Vorläufer des Transformers.

                $ model = keras.Sequential([
                $     layers.Input(shape=(30, 1)),          # 30 vergangene Werte
                $     layers.LSTM(64),
                $     layers.Dense(1),                      # nächster Wert
                $ ])
                $ model.compile("adam", "mse")
            """),
            sec("Warum heute oft Transformer?", """
                RNNs verarbeiten Schritt für Schritt – nicht parallelisierbar und bei sehr langen Folgen vergesslich. Transformer schauen per Attention direkt auf alle Positionen und trainieren auf GPUs viel schneller. Für kleine Zeitreihen auf dem Handy oder Sensoren sind LSTMs und GRUs aber weiterhin effizient und beliebt.
            """)
        ),
        chapter(
            "dl_transfer", T, 2, "Transfer Learning und vortrainierte Modelle",
            "Warum man selten bei null anfängt, Merkmalsextraktion gegen Feinabstimmung, Schichten einfrieren, kleine Lernrate, Hugging Face und Keras Applications.",
            emptyList(),
            sec("Auf den Schultern von Riesen", """
                Ein auf Millionen Bildern (ImageNet) trainiertes Netz hat in seinen frühen Schichten allgemeine Merkmale gelernt: Kanten, Texturen, Formen. Diese sind für fast jede Bildaufgabe nützlich. Statt bei null zu beginnen, nimmt man das vortrainierte Netz und passt es mit wenigen eigenen Daten an. Dasselbe gilt für Sprache: Jedes LLM-Projekt startet mit einem vortrainierten Modell.
            """),
            sec("Zwei Strategien", """
                • Merkmalsextraktion: Das vortrainierte Netz einfrieren, nur einen neuen Kopf (letzte Schichten) trainieren. Schnell, braucht wenige Daten.
                • Feinabstimmung (Fine-Tuning): Danach einige obere Schichten auftauen und mit sehr kleiner Lernrate (z. B. 10⁻⁵) mittrainieren. Besser, wenn genug Daten vorhanden sind.

                $ base = keras.applications.MobileNetV2(include_top=False, weights="imagenet", input_shape=(224, 224, 3))
                $ base.trainable = False
                $ model = keras.Sequential([base, layers.GlobalAveragePooling2D(), layers.Dense(5, activation="softmax")])
            """),
            sec("Faustregeln", """
                • Wenige Daten, ähnliche Aufgabe → nur den Kopf trainieren.
                • Viele Daten oder andere Domäne (Röntgenbilder statt Fotos) → mehr Schichten feinabstimmen.
                • Eingaben genauso vorverarbeiten wie beim Vortraining (Größe, Normalisierung).
                • Hugging Face: from_pretrained lädt Tausende Modelle für Text, Bild und Audio.
            """)
        ),
        chapter(
            "dl_autoencoder", T, 2, "Autoencoder und variationale Autoencoder",
            "Komprimieren und rekonstruieren, Flaschenhals, Entrauschen, Anomalieerkennung, VAE mit latentem Raum und KL-Term, Generieren durch Sampling.",
            emptyList(),
            sec("Komprimieren lernen", """
                Ein Autoencoder besteht aus Encoder f (Eingabe → kleiner Code z) und Decoder g (Code → Rekonstruktion). Trainiert wird darauf, die Eingabe möglichst genau wiederherzustellen. Weil der Code schmaler ist als die Eingabe (Flaschenhals), muss das Netz das Wesentliche lernen – eine nichtlineare Verallgemeinerung der PCA.
            """,
                fm("Autoencoder", "z = f(x), x̂ = g(z)"),
                fm("Rekonstruktionsverlust", "L = |x − g(f(x))|²")
            ),
            sec("Anwendungen", """
                • Entrauschen (denoising): verrauschtes Bild rein, sauberes Bild als Ziel.
                • Anomalieerkennung: hoher Rekonstruktionsfehler = ungewöhnlich (Maschinensensoren, Betrug).
                • Kompression und Merkmalslernen für andere Modelle.
            """),
            sec("Variationale Autoencoder (VAE)", """
                Ein VAE macht den Code zu einer Wahrscheinlichkeitsverteilung: Der Encoder gibt Mittelwert μ und Streuung σ aus, z wird daraus gezogen. Ein Zusatzterm (Kullback-Leibler-Divergenz) drückt diese Verteilung zur Standardnormalverteilung. Dadurch wird der latente Raum glatt und lückenlos: Zieht man ein beliebiges z ~ N(0, 1) und decodiert es, entsteht ein neues, plausibles Beispiel. Der VAE ist damit ein generatives Modell – und sein Encoder steckt heute in Bildgeneratoren wie Stable Diffusion.
            """,
                fm("VAE-Verlust", "L = |x − x̂|² + KL(N(μ, σ²) ‖ N(0, 1))"),
                fm("Reparametrisierung", "z = μ + σ ⊙ ε, ε ~ N(0, 1)")
            )
        ),
        chapter(
            "dl_generative", T, 3, "Generative Modelle: GANs und Diffusion",
            "Generator gegen Diskriminator, Minimax-Spiel, Trainingsprobleme, Diffusionsmodelle als schrittweises Entrauschen, Text-zu-Bild, Klassifikator-freie Führung.",
            emptyList(),
            sec("GAN: Fälscher gegen Gutachter", """
                Ein Generative Adversarial Network (Goodfellow 2014) besteht aus zwei Netzen: Der Generator G macht aus Zufallsrauschen z ein Bild, der Diskriminator D soll echte von gefälschten Bildern unterscheiden. Beide trainieren gegeneinander – wie ein Geldfälscher und die Polizei. Im Gleichgewicht sind die Fälschungen von echten nicht mehr zu unterscheiden.
            """,
                fm("GAN-Ziel", "min_G max_D E[ln D(x)] + E[ln(1 − D(G(z)))]")
            ),
            sec("Warum GANs schwierig sind", """
                • Mode Collapse: Der Generator erzeugt immer wieder dieselben wenigen Bilder.
                • Instabiles Training: Ist der Diskriminator zu gut, bekommt der Generator keine nützlichen Gradienten.
                • Keine einfache Verlustkurve, die Qualität anzeigt – man bewertet mit FID (Abstand von Merkmalsverteilungen).
            """),
            sec("Diffusionsmodelle", """
                Die heute dominierende Methode (DALL·E 3, Stable Diffusion, Midjourney, Sora). Idee aus der Physik der Diffusion: Man zerstört ein Bild in vielen kleinen Schritten mit Rauschen, bis nur noch Rauschen übrig ist (Vorwärtsprozess). Ein Netz lernt, in jedem Schritt das hinzugefügte Rauschen vorherzusagen (Rückwärtsprozess). Zum Generieren startet man bei reinem Rauschen und entrauscht Schritt für Schritt – ein Bild entsteht.

                • Text-zu-Bild: Der Entrauscher bekommt die Textbeschreibung als Bedingung (über Cross-Attention auf ein Text-Embedding).
                • Latente Diffusion: Man entrauscht nicht Pixel, sondern den kompakten Code eines Autoencoders – viel billiger.
                • Klassifikator-freie Führung: verstärkt den Einfluss des Textes, Regler für „wie wörtlich“.
            """,
                fm("Vorwärtsprozess", "x_t = √(ᾱ_t) x₀ + √(1 − ᾱ_t) ε"),
                fm("Trainingsverlust", "L = |ε − ε_θ(x_t, t)|²")
            ),
            sec("Physik-Brücke", """
                Diffusionsmodelle sind eng mit der Langevin-Dynamik und der Fokker-Planck-Gleichung verwandt: Das Netz lernt den Score ∇ₓ ln p(x), also die „Kraft“, die Rauschen zurück zu wahrscheinlichen Daten zieht. Generieren ist dann ein stochastischer Prozess, der gegen eine Gleichgewichtsverteilung läuft – nur rückwärts in der Zeit.
            """,
                fm("Score", "s_θ(x) ≈ ∇ₓ ln p(x)")
            )
        )
    ) + course(
        "Reinforcement Learning",
        chapter(
            "rl_basics", T, 2, "Bestärkendes Lernen: Agent, Umgebung, Belohnung",
            "Zustände, Aktionen, Belohnungen, Episoden, Markov-Entscheidungsprozess, Return und Diskontfaktor, Policy und Wertfunktion, Exploration gegen Ausbeutung.",
            emptyList(),
            sec("Die Grundschleife", """
                Ein Agent beobachtet den Zustand s der Umgebung, wählt eine Aktion a, bekommt eine Belohnung r und landet in einem neuen Zustand s′. Wiederholen. Ziel: eine Strategie (Policy π), die auf Dauer möglichst viel Belohnung sammelt. Niemand sagt dem Agenten die richtige Aktion – er muss es durch Ausprobieren herausfinden, und Belohnungen kommen oft erst spät (Schach: erst am Ende).

                • Spiele: Schach, Go, Atari, Dota
                • Robotik: laufen, greifen
                • Steuerung: Kühlung von Rechenzentren, Plasmaregelung im Fusionsreaktor
                • Sprachmodelle: RLHF
            """),
            sec("Markov-Entscheidungsprozess (MDP)", """
                Formal: Zustände S, Aktionen A, Übergangswahrscheinlichkeiten P(s′ | s, a), Belohnungen R(s, a) und ein Diskontfaktor γ ∈ [0, 1). Der Return ist die diskontierte Summe zukünftiger Belohnungen: γ nahe 0 = kurzsichtig, nahe 1 = weitsichtig.

                Beispiel: Belohnungen 1, 1, 10 mit γ = 0,9: G = 1 + 0,9 + 0,81·10 = 10,0.
            """,
                fm("Return", "G_t = r_{t+1} + γ r_{t+2} + γ² r_{t+3} + … = Σₖ γᵏ r_{t+k+1}"),
                fm("Zustandswert", "V^π(s) = E_π[G_t | s_t = s]"),
                fm("Aktionswert", "Q^π(s, a) = E_π[G_t | s_t = s, a_t = a]")
            ),
            sec("Exploration gegen Ausbeutung", """
                Soll der Agent die bisher beste bekannte Aktion nehmen (Ausbeutung) oder etwas Neues probieren, das vielleicht besser ist (Exploration)? Wie im Restaurant: das Lieblingsgericht oder etwas Neues? ε-greedy löst das einfach: mit Wahrscheinlichkeit ε zufällig, sonst die beste Aktion; ε sinkt im Lauf des Trainings.
            """,
                fm("ε-greedy", "a = zufällig mit Wahrscheinlichkeit ε, sonst argmax_a Q(s, a)")
            )
        ),
        chapter(
            "rl_qlearning", T, 2, "Bellman-Gleichung und Q-Learning",
            "Rekursive Wertfunktion, Bellman-Optimalität, Q-Learning-Update Schritt für Schritt, Lernrate und Diskont, SARSA, Q-Tabelle im Gitterlabyrinth.",
            emptyList(),
            sec("Die Bellman-Gleichung", """
                Der Wert eines Zustands ist die sofortige Belohnung plus der diskontierte Wert dessen, was danach kommt. Diese Rekursion ist das Herz des bestärkenden Lernens – dieselbe Idee wie dynamische Programmierung.
            """,
                fm("Bellman-Optimalität", "Q*(s, a) = E[r + γ max_{a′} Q*(s′, a′)]")
            ),
            sec("Q-Learning", """
                Man hält eine Tabelle Q(s, a) und verbessert sie nach jedem Schritt: Das „Ziel“ ist r + γ·max Q(s′, ·), der Fehler (TD-Fehler) ist Ziel minus alter Wert, und man geht mit Lernrate α ein Stück darauf zu. Q-Learning lernt die optimale Strategie, auch wenn der Agent beim Sammeln der Erfahrung zufällig erkundet (off-policy).

                Rechenbeispiel: Q(s, rechts) = 2, Belohnung r = 1, im neuen Zustand max Q = 5, γ = 0,9, α = 0,5. Ziel = 1 + 0,9·5 = 5,5. TD-Fehler = 3,5. Neuer Wert = 2 + 0,5·3,5 = 3,75.
            """,
                fm("Q-Learning-Update", "Q(s, a) ← Q(s, a) + α [r + γ max_{a′} Q(s′, a′) − Q(s, a)]"),
                fm("TD-Fehler", "δ = r + γ max_{a′} Q(s′, a′) − Q(s, a)")
            ),
            sec("SARSA und die Wahl der Hyperparameter", """
                SARSA nimmt statt des Maximums die Aktion, die der Agent wirklich als Nächstes wählt (on-policy) – vorsichtiger, wenn Erkunden gefährlich ist (Klippe). Hyperparameter: α (wie schnell man umlernt), γ (wie weit man vorausschaut), ε (wie viel man erkundet). Im Spiel „Q-Learning-Labyrinth“ siehst du, wie sich die Pfeile der Strategie Episode für Episode zum Ziel ausrichten.
            """,
                fm("SARSA-Update", "Q(s, a) ← Q(s, a) + α [r + γ Q(s′, a′) − Q(s, a)]")
            )
        ),
        chapter(
            "rl_deep", T, 3, "Deep Reinforcement Learning",
            "Wenn die Tabelle zu groß wird: DQN mit Replay und Zielnetz, Policy Gradients, Actor-Critic, PPO, AlphaGo/AlphaZero, RLHF für Sprachmodelle.",
            emptyList(),
            sec("Deep Q-Networks (DQN)", """
                Bei Atari-Spielen ist der Zustand ein Bild – unendlich viele Zustände, keine Tabelle möglich. DQN (DeepMind 2015) ersetzt die Tabelle durch ein neuronales Netz Q_θ(s, a). Zwei Tricks machen es stabil: Experience Replay (Erfahrungen speichern und zufällig gemischt wiederverwenden) und ein Zielnetz, das nur ab und zu aktualisiert wird.
            """,
                fm("DQN-Verlust", "L = (r + γ max_{a′} Q_{θ⁻}(s′, a′) − Q_θ(s, a))²")
            ),
            sec("Policy Gradients und Actor-Critic", """
                Statt Werte zu lernen, kann man die Strategie direkt als Netz π_θ(a | s) parametrisieren und den erwarteten Return per Gradientenaufstieg maximieren: Aktionen, die zu gutem Ergebnis führten, werden wahrscheinlicher. Actor-Critic kombiniert beides: Der Actor (Policy) handelt, der Critic (Wertfunktion) bewertet und reduziert das Rauschen. PPO (Proximal Policy Optimization) begrenzt, wie stark sich die Policy pro Schritt ändern darf – robust und Standard.
            """,
                fm("Policy-Gradient (REINFORCE)", "∇J(θ) = E[∇_θ ln π_θ(a | s) · G_t]"),
                fm("Vorteil", "A(s, a) = Q(s, a) − V(s)")
            ),
            sec("Meilensteine und RLHF", """
                • AlphaGo (2016): Policy- und Wertnetze plus Monte-Carlo-Baumsuche.
                • AlphaZero (2017): lernt Schach, Go und Shogi nur durch Spiel gegen sich selbst.
                • RLHF (Reinforcement Learning from Human Feedback): Menschen vergleichen Antworten eines Sprachmodells, ein Belohnungsmodell lernt diese Vorlieben, und das Sprachmodell wird mit PPO darauf optimiert. DPO erreicht Ähnliches ohne explizites RL.
            """)
        )
    )
}

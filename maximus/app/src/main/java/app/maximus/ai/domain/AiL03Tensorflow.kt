package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 3 (IBM „Deep Learning with Keras and TensorFlow“): TensorFlow 2 basics, the functional and
 * subclassing APIs, custom layers and callbacks, augmentation, transformers in Keras, custom training
 * loops with hyperparameter tuning, and model optimisation for deployment.
 */
internal object AiL03Tensorflow {
    val bites: List<Bite> = listOf(
        bite(
            "tf_basics", 1, "TensorFlow 2: Tensoren, Eager Execution, tf.function, GradientTape",
            "Was unter Keras arbeitet: unveränderliche Tensoren, Variablen, sofortige Ausführung, Graphen durch Tracing und automatische Ableitung.",
            goals(
                "tf.Tensor und tf.Variable unterscheiden und Formen lesen",
                "Mit tf.GradientTape eine Ableitung berechnen",
                "Erklären, was @tf.function beschleunigt und welche Fallen es hat"
            ),
            sec("Tensoren und Variablen", """
                Ein Tensor ist ein mehrdimensionales Array mit fester Form (shape) und Datentyp (dtype). Ein Skalar hat Rang 0, ein Vektor Rang 1, eine Matrix Rang 2, ein Stapel farbiger Bilder Rang 4 mit der Form (Batch, Höhe, Breite, Kanäle). tf.Tensor ist unveränderlich; trainierbare Gewichte sind tf.Variable, die man mit assign ändert.

                $ import tensorflow as tf
                $ x = tf.constant([[1.0, 2.0], [3.0, 4.0]])
                $ print(x.shape, x.dtype)          # (2, 2) float32
                $ w = tf.Variable(tf.random.normal((2, 1)))
                $ y = x @ w                         # Matrixprodukt, shape (2, 1)
                $ w.assign_sub(0.1 * tf.ones_like(w))
            """,
                fm("Bildstapel", "shape = (B, H, W, C), z. B. (32, 224, 224, 3)")
            ),
            sec("GradientTape: Ableiten auf Band", """
                TensorFlow 2 führt Operationen sofort aus (Eager Execution), wie NumPy. Will man ableiten, zeichnet ein GradientTape die Operationen auf und rechnet dann rückwärts (Reverse-Mode-Autodiff).

                $ x = tf.Variable(3.0)
                $ with tf.GradientTape() as tape:
                $     y = x ** 2 + 2 * x
                $ print(tape.gradient(y, x))        # 2·3 + 2 = 8.0

                Variablen werden automatisch beobachtet, Konstanten nur mit tape.watch(t). Ein Band ist nach einem gradient-Aufruf verbraucht, außer man öffnet es mit persistent=True.
            """,
                fm("Beispiel", "y = x² + 2x  ⇒  dy/dx = 2x + 2 = 8 bei x = 3")
            ),
            sec("tf.function: Graphen für Geschwindigkeit", """
                Mit dem Dekorator @tf.function verfolgt TensorFlow beim ersten Aufruf die Python-Funktion (Tracing) und baut daraus einen Rechengraphen. Spätere Aufrufe mit gleicher Eingabeform laufen als optimierter Graph – ohne Python-Overhead, mit Operator-Fusion, exportierbar als SavedModel.

                $ @tf.function
                $ def step(x):
                $     return tf.reduce_sum(x * x)

                • Python-Seiteneffekte wie print laufen nur beim Tracing; tf.print läuft jedes Mal.
                • Neue Eingabeformen oder Python-Werte als Argumente lösen ein erneutes Tracing aus (Retracing) – teuer.
                • Keras kompiliert train_step automatisch zu einem Graphen; run_eagerly=True schaltet das zum Debuggen ab.
            """),
            sec("TensorFlow und Keras heute", """
                Keras ist die High-Level-API; seit Keras 3 kann sie auf TensorFlow, JAX oder PyTorch laufen. TensorFlow liefert darunter die Tensoren, die automatische Ableitung, die Datenpipeline tf.data, Verteilung auf mehrere GPUs und die Bereitstellung (TF Serving, TF Lite).

                $ ds = tf.data.Dataset.from_tensor_slices((X, y)).shuffle(1000).batch(32).prefetch(tf.data.AUTOTUNE)

                > tf.data liest, mischt und bündelt Daten parallel zum Training; prefetch überlappt Laden und Rechnen, damit die GPU nicht wartet.
            """)
        ),
        bite(
            "keras_functional", 2, "Funktionale API und Subclassing",
            "Modelle als Graph aus Schichten: mehrere Eingänge und Ausgänge, geteilte Schichten, Verzweigungen – und eigene Modellklassen.",
            goals(
                "Ein Modell mit der funktionalen API aufbauen",
                "Mehrere Eingänge und Ausgänge sowie geteilte Schichten verwenden",
                "Wissen, wann Sequential, funktional oder Subclassing passt"
            ),
            sec("Grenzen von Sequential", """
                Sequential stapelt Schichten in einer Linie: ein Eingang, ein Ausgang, keine Verzweigungen. Viele Architekturen sind aber Graphen: Residualnetze addieren den Eingang wieder auf, Siamesische Netze teilen Gewichte zwischen zwei Eingängen, Multi-Task-Modelle haben mehrere Köpfe. Dafür gibt es die funktionale API: Man ruft Schichten wie Funktionen auf Tensoren auf.

                $ from tensorflow import keras
                $ from tensorflow.keras import layers
                $ inputs = keras.Input(shape=(784,))
                $ x = layers.Dense(64, activation="relu")(inputs)
                $ x = layers.Dense(64, activation="relu")(x)
                $ outputs = layers.Dense(10, activation="softmax")(x)
                $ model = keras.Model(inputs=inputs, outputs=outputs)
            """),
            sec("Mehrere Eingänge und Ausgänge", """
                Ein Ticket-System soll aus Titel (Text), Beschreibung (Text) und Tags die Priorität (Regression) und die Abteilung (Klassifikation) vorhersagen.

                $ title = keras.Input(shape=(None,), name="title")
                $ body = keras.Input(shape=(None,), name="body")
                $ tags = keras.Input(shape=(12,), name="tags")
                $ t = layers.LSTM(32)(layers.Embedding(10000, 64)(title))
                $ b = layers.LSTM(32)(layers.Embedding(10000, 64)(body))
                $ x = layers.concatenate([t, b, tags])
                $ priority = layers.Dense(1, name="priority")(x)
                $ dept = layers.Dense(4, activation="softmax", name="dept")(x)
                $ model = keras.Model([title, body, tags], [priority, dept])
                $ model.compile(optimizer="adam", loss={"priority": "mse", "dept": "categorical_crossentropy"}, loss_weights={"priority": 1.0, "dept": 0.2})

                Der Gesamtverlust ist die gewichtete Summe der Einzelverluste.
            """,
                fm("Multi-Task-Verlust", "L = Σₖ λₖ Lₖ")
            ),
            sec("Geteilte Schichten und Residuen", """
                Eine Schichtinstanz, zweimal aufgerufen, benutzt dieselben Gewichte – so vergleicht ein Siamesisches Netz zwei Sätze mit demselben Encoder. Eine Residualverbindung addiert den Eingang eines Blocks zu seiner Ausgabe.

                $ encoder = layers.Dense(64, activation="relu")
                $ a = encoder(input_a); b = encoder(input_b)        # gleiche Gewichte
                $ y = layers.Dense(64, activation="relu")(x)
                $ y = layers.add([x, y])                            # Residuum, gleiche Form nötig
            """),
            sec("Subclassing: volle Freiheit", """
                Für dynamische Abläufe (Schleifen, Bedingungen, rekursive Strukturen) schreibt man eine eigene Klasse. Schichten werden im Konstruktor angelegt, der Vorwärtsweg steht in call.

                $ class MLP(keras.Model):
                $     def __init__(self):
                $         super().__init__()
                $         self.d1 = layers.Dense(64, activation="relu")
                $         self.out = layers.Dense(10, activation="softmax")
                $     def call(self, x, training=False):
                $         return self.out(self.d1(x))

                • Sequential: lineare Stapel, schnell geschrieben.
                • Funktional: jeder Graph, Form wird beim Bauen geprüft, model.summary() und plot_model funktionieren.
                • Subclassing: maximale Flexibilität, aber weniger Prüfungen und schwerer zu serialisieren.
            """)
        ),
        bite(
            "keras_custom_layers", 2, "Eigene Schichten, Verluste und Callbacks",
            "Keras erweitern: Layer mit build und call, eigene Verlustfunktionen, Callbacks wie EarlyStopping und ModelCheckpoint, TensorBoard.",
            goals(
                "Eine eigene Schicht mit build und call schreiben",
                "Eigene Verlustfunktionen und Metriken definieren",
                "Training mit Callbacks steuern"
            ),
            sec("Eine eigene Dense-Schicht", """
                Eine Schicht erbt von keras.layers.Layer. In build legt man die Gewichte an – erst dann ist die Eingabeform bekannt (lazy building). In call steht die Rechnung.

                $ class MyDense(keras.layers.Layer):
                $     def __init__(self, units=32, **kwargs):
                $         super().__init__(**kwargs)
                $         self.units = units
                $     def build(self, input_shape):
                $         self.w = self.add_weight(shape=(input_shape[-1], self.units), initializer="glorot_uniform", trainable=True)
                $         self.b = self.add_weight(shape=(self.units,), initializer="zeros", trainable=True)
                $     def call(self, inputs):
                $         return tf.matmul(inputs, self.w) + self.b

                Schichten mit unterschiedlichem Verhalten in Training und Inferenz (Dropout, BatchNorm) bekommen einen Parameter training in call.
            """),
            sec("Eigene Verluste", """
                Ein Verlust ist eine Funktion (y_true, y_pred) → Tensor. Beispiel Huber-Verlust: quadratisch für kleine Fehler, linear für große – robust gegen Ausreißer.

                $ def huber(y_true, y_pred, delta=1.0):
                $     err = y_true - y_pred
                $     small = tf.abs(err) <= delta
                $     return tf.where(small, 0.5 * err ** 2, delta * (tf.abs(err) - 0.5 * delta))
                $ model.compile(optimizer="adam", loss=huber)
            """,
                fm("Huber-Verlust", "L_δ(e) = ½e² für |e| ≤ δ, sonst δ(|e| − δ/2)")
            ),
            sec("Callbacks: Das Training überwachen", """
                Callbacks sind Haken, die Keras zu Beginn und Ende von Epochen und Batches aufruft.

                $ callbacks = [
                $     keras.callbacks.EarlyStopping(monitor="val_loss", patience=5, restore_best_weights=True),
                $     keras.callbacks.ModelCheckpoint("best.keras", monitor="val_loss", save_best_only=True),
                $     keras.callbacks.ReduceLROnPlateau(monitor="val_loss", factor=0.5, patience=2),
                $     keras.callbacks.TensorBoard(log_dir="logs")
                $ ]
                $ model.fit(X, y, validation_split=0.2, epochs=200, callbacks=callbacks)

                EarlyStopping beendet das Training, wenn sich der Validierungsverlust patience Epochen lang nicht verbessert – eine einfache, sehr wirksame Regularisierung. restore_best_weights setzt danach die besten Gewichte zurück.
            """),
            sec("Eigene Callbacks", """
                $ class PrintLR(keras.callbacks.Callback):
                $     def on_epoch_end(self, epoch, logs=None):
                $         lr = float(self.model.optimizer.learning_rate)
                $         print(f"Epoche {epoch}: val_loss={logs['val_loss']:.4f}, lr={lr:.2e}")

                > Merke: Schicht = Rechnung mit Gewichten, Verlust = Zielgröße, Metrik = Anzeige für Menschen, Callback = Steuerung des Trainingsablaufs.
            """)
        ),
        bite(
            "keras_augment", 2, "Datenaugmentation und Bildvorverarbeitung",
            "Mehr Daten aus denselben Bildern: Spiegeln, Drehen, Zoomen, Kontrast – als Keras-Schichten oder im Generator, und warum nur im Training.",
            goals(
                "Augmentation als Regularisierung verstehen",
                "Keras-Vorverarbeitungsschichten und ImageDataGenerator benutzen",
                "Augmentationen wählen, die das Label nicht verändern"
            ),
            sec("Die Idee", """
                Ein Hund bleibt ein Hund, wenn man das Foto spiegelt, leicht dreht oder heller macht. Augmentation erzeugt aus jedem Trainingsbild in jeder Epoche zufällige Varianten. Das Netz sieht so effektiv mehr Daten und lernt Invarianzen, statt Pixelmuster auswendig zu lernen – eine Regularisierung, die oft mehr bringt als jede andere.

                Wichtig: Nur Transformationen, die das Label erhalten. Bei Ziffern ist eine Spiegelung tödlich (eine gespiegelte 2 ist keine 2), eine Drehung um 180° macht aus 6 eine 9. Bei Satellitenbildern dagegen sind beliebige Drehungen und Spiegelungen erlaubt.
            """),
            sec("Augmentation als Schichten", """
                $ from tensorflow import keras
                $ from tensorflow.keras import layers
                $ augment = keras.Sequential([
                $     layers.RandomFlip("horizontal"),
                $     layers.RandomRotation(0.1),       # ±10 % von 360°, also ±36°
                $     layers.RandomZoom(0.2),
                $     layers.RandomContrast(0.2)
                $ ])
                $ inputs = keras.Input(shape=(180, 180, 3))
                $ x = augment(inputs)
                $ x = layers.Rescaling(1.0 / 255)(x)

                Diese Schichten sind nur im Training aktiv; bei evaluate und predict reichen sie die Bilder unverändert durch. Sie laufen auf der GPU und reisen mit dem gespeicherten Modell mit.
            """),
            sec("ImageDataGenerator und Verzeichnisse", """
                Ältere Kurse und viele Projekte nutzen ImageDataGenerator, der Bilder aus Ordnern lädt und dabei augmentiert. Jeder Unterordner ist eine Klasse.

                $ from tensorflow.keras.preprocessing.image import ImageDataGenerator
                $ gen = ImageDataGenerator(rescale=1/255, rotation_range=40, width_shift_range=0.2, horizontal_flip=True, validation_split=0.2)
                $ train = gen.flow_from_directory("data/", target_size=(64, 64), batch_size=32, class_mode="binary", subset="training")

                Moderner ist keras.utils.image_dataset_from_directory mit tf.data und Augmentationsschichten.
            """),
            sec("Mixup und Cutout", """
                Fortgeschrittene Verfahren mischen Bilder: Mixup bildet konvexe Kombinationen zweier Bilder und ihrer Labels, Cutout schwärzt zufällige Rechtecke, CutMix klebt Ausschnitte eines Bildes in ein anderes und mischt die Labels nach Fläche. Sie wirken als starke Regularisierer und verbessern die Kalibrierung.
            """,
                fm("Mixup", "x̃ = λxᵢ + (1 − λ)xⱼ,  ỹ = λyᵢ + (1 − λ)yⱼ,  λ ~ Beta(α, α)")
            )
        ),
        bite(
            "keras_transformer", 3, "Transformer in Keras: Attention-Schicht und Zeitreihen",
            "MultiHeadAttention als Keras-Schicht, ein Transformer-Encoderblock in wenigen Zeilen und seine Anwendung auf Text und Zeitreihen.",
            goals(
                "Einen Transformer-Encoderblock mit MultiHeadAttention bauen",
                "Positionsinformation über eine Embedding-Schicht ergänzen",
                "Transformer auf Zeitreihenvorhersage anwenden"
            ),
            sec("Self-Attention in einem Satz", """
                Jedes Element einer Folge schaut auf alle anderen und bildet einen gewichteten Mittelwert ihrer Werte. Die Gewichte entstehen aus der Ähnlichkeit von Query und Key. Die Mathematik vertiefen die Happen der Lektion 9; hier zählt die Benutzung als Baustein.
            """,
                fm("Scaled Dot-Product Attention", "Attention(Q, K, V) = softmax(QKᵀ/√d_k) V")
            ),
            sec("Ein Encoderblock", """
                $ from tensorflow import keras
                $ from tensorflow.keras import layers
                $ class TransformerBlock(layers.Layer):
                $     def __init__(self, d_model, heads, ff_dim, rate=0.1):
                $         super().__init__()
                $         self.att = layers.MultiHeadAttention(num_heads=heads, key_dim=d_model // heads)
                $         self.ffn = keras.Sequential([layers.Dense(ff_dim, activation="relu"), layers.Dense(d_model)])
                $         self.norm1 = layers.LayerNormalization(epsilon=1e-6)
                $         self.norm2 = layers.LayerNormalization(epsilon=1e-6)
                $         self.drop = layers.Dropout(rate)
                $     def call(self, x, training=False):
                $         a = self.att(x, x)                       # Self-Attention: Query = Key = Value = x
                $         x = self.norm1(x + self.drop(a, training=training))
                $         f = self.ffn(x)
                $         return self.norm2(x + self.drop(f, training=training))

                Zwei Unterblöcke, jeweils mit Residuum und LayerNorm: Attention mischt Information zwischen Positionen, das Feed-Forward-Netz verarbeitet jede Position einzeln.
            """),
            sec("Text klassifizieren", """
                Attention allein kennt keine Reihenfolge. Man addiert deshalb eine gelernte Positionseinbettung.

                $ class TokenAndPosition(layers.Layer):
                $     def __init__(self, maxlen, vocab, d):
                $         super().__init__()
                $         self.tok = layers.Embedding(vocab, d)
                $         self.pos = layers.Embedding(maxlen, d)
                $     def call(self, x):
                $         positions = tf.range(start=0, limit=tf.shape(x)[-1], delta=1)
                $         return self.tok(x) + self.pos(positions)
                $ inputs = keras.Input(shape=(200,))
                $ x = TransformerBlock(32, 2, 32)(TokenAndPosition(200, 20000, 32)(inputs))
                $ x = layers.GlobalAveragePooling1D()(x)
                $ outputs = layers.Dense(2, activation="softmax")(x)
            """),
            sec("Zeitreihen vorhersagen", """
                Für Zeitreihen (z. B. Aktienkurse oder Sensordaten im IBM-Lab) schneidet man Fenster der Länge T aus der Reihe, projiziert jeden Zeitschritt mit einer Dense-Schicht auf d Dimensionen, schickt die Folge durch Transformerblöcke und sagt mit einer Dense-Schicht den nächsten Wert voraus. Gegenüber LSTMs sehen Transformer alle Zeitschritte gleichzeitig und lassen sich parallel trainieren; der Aufwand wächst aber quadratisch mit T.
            """,
                fm("Fensterbildung", "Eingabe (x_{t−T+1}, …, x_t) → Ziel x_{t+1}"),
                fm("Aufwand Self-Attention", "O(T² · d)")
            )
        ),
        bite(
            "keras_custom_training", 3, "Eigene Trainingsschleifen und Hyperparameter-Tuning",
            "Was model.fit eigentlich tut: eine Trainingsschleife mit GradientTape von Hand, train_step überschreiben, und Keras Tuner für die Suche.",
            goals(
                "Eine Trainingsschleife mit GradientTape schreiben",
                "train_step überschreiben, um fit weiter nutzen zu können",
                "Mit Keras Tuner Hyperparameter suchen"
            ),
            sec("Die Schleife von Hand", """
                Für GANs, Reinforcement Learning oder Forschung braucht man volle Kontrolle über jeden Schritt.

                $ optimizer = keras.optimizers.Adam(1e-3)
                $ loss_fn = keras.losses.SparseCategoricalCrossentropy()
                $ acc = keras.metrics.SparseCategoricalAccuracy()
                $ for epoch in range(5):
                $     for x_batch, y_batch in train_ds:
                $         with tf.GradientTape() as tape:
                $             logits = model(x_batch, training=True)
                $             loss = loss_fn(y_batch, logits)
                $         grads = tape.gradient(loss, model.trainable_weights)
                $         optimizer.apply_gradients(zip(grads, model.trainable_weights))
                $         acc.update_state(y_batch, logits)
                $     print(epoch, float(acc.result())); acc.reset_state()

                Vier Schritte, immer gleich: vorwärts rechnen, Verlust, Gradienten, Update. training=True schaltet Dropout und BatchNorm in den Trainingsmodus.
            """,
                fm("Update", "θ ← θ − η · ∇_θ L(θ; Batch)")
            ),
            sec("train_step überschreiben", """
                Der Mittelweg: eigene Logik pro Schritt, aber Callbacks, Fortschrittsbalken und Verteilung von fit behalten.

                $ class CustomModel(keras.Model):
                $     def train_step(self, data):
                $         x, y = data
                $         with tf.GradientTape() as tape:
                $             y_pred = self(x, training=True)
                $             loss = self.compute_loss(y=y, y_pred=y_pred)
                $         grads = tape.gradient(loss, self.trainable_variables)
                $         self.optimizer.apply_gradients(zip(grads, self.trainable_variables))
                $         return {"loss": loss}
            """),
            sec("Keras Tuner", """
                Hyperparameter wie Schichtbreite, Lernrate oder Dropout sucht man automatisch. Keras Tuner bietet Zufallssuche, Bayessche Optimierung und Hyperband (früh abbrechen, was schlecht startet).

                $ import keras_tuner as kt
                $ def build(hp):
                $     model = keras.Sequential([layers.Dense(hp.Int("units", 32, 512, step=32), activation="relu"), layers.Dense(10, activation="softmax")])
                $     lr = hp.Float("lr", 1e-4, 1e-2, sampling="log")
                $     model.compile(keras.optimizers.Adam(lr), "sparse_categorical_crossentropy", metrics=["accuracy"])
                $     return model
                $ tuner = kt.Hyperband(build, objective="val_accuracy", max_epochs=20)
                $ tuner.search(X_train, y_train, validation_split=0.2)
                $ best_hp = tuner.get_best_hyperparameters(1)[0]

                Lernraten sucht man logarithmisch: Der Unterschied zwischen 10⁻⁴ und 10⁻³ ist so wichtig wie der zwischen 10⁻³ und 10⁻².
            """,
                fm("Log-Skala", "η = 10^u,  u ~ U(−4, −2)")
            )
        ),
        bite(
            "keras_optimize", 3, "Modelle optimieren und ausliefern",
            "Mixed Precision, Pruning, Quantisierung, Destillation und die Formate SavedModel, TF Lite und TF Serving.",
            goals(
                "Mixed Precision und ihre Grenzen kennen",
                "Pruning, Quantisierung und Wissensdestillation unterscheiden",
                "Ein Modell als SavedModel und als TF-Lite-Datei exportieren"
            ),
            sec("Mixed Precision", """
                Moderne GPUs rechnen in 16-Bit-Gleitkomma (float16 oder bfloat16) mehrfach schneller und mit halbem Speicher. Mixed Precision rechnet die Schichten in 16 Bit, hält aber die Mastergewichte in 32 Bit. Bei float16 skaliert man den Verlust hoch (Loss Scaling), damit kleine Gradienten nicht auf null abrunden; bfloat16 hat den Exponentenbereich von float32 und braucht das nicht.

                $ keras.mixed_precision.set_global_policy("mixed_float16")
                $ outputs = layers.Dense(10, activation="softmax", dtype="float32")(x)   # Ausgabe numerisch stabil in 32 Bit
            """,
                fm("Speicher je Parameter", "float32: 4 Byte, float16/bfloat16: 2 Byte, int8: 1 Byte, int4: 0,5 Byte")
            ),
            sec("Kleiner und schneller", """
                • Pruning: Gewichte mit kleinem Betrag auf null setzen und nachtrainieren. Bei 80 % Sparsity bleibt die Genauigkeit oft fast gleich; schneller wird es nur mit strukturierter Sparsity oder spezieller Hardware.
                • Quantisierung: Gewichte (und Aktivierungen) in int8 statt float32 – Faktor 4 kleiner. Nachträglich (Post-Training-Quantisierung) oder im Training simuliert (Quantization-Aware Training).
                • Wissensdestillation: Ein kleines Schülermodell lernt die weichen Wahrscheinlichkeiten eines großen Lehrermodells.
            """,
                fm("Affine Quantisierung", "q = round(x/s) + z,  x ≈ s(q − z)"),
                fm("Destillation", "L = (1 − α) CE(y, p_S) + α T² KL(p_T^{(T)} ‖ p_S^{(T)})")
            ),
            sec("Exportieren", """
                $ model.export("export/my_model")                   # SavedModel für TF Serving
                $ converter = tf.lite.TFLiteConverter.from_saved_model("export/my_model")
                $ converter.optimizations = [tf.lite.Optimize.DEFAULT]   # Post-Training-Quantisierung
                $ open("model.tflite", "wb").write(converter.convert())

                SavedModel enthält Graph und Gewichte, sprachunabhängig – TF Serving stellt es als REST- oder gRPC-Dienst bereit. TF Lite (heute LiteRT) läuft auf Handys und Mikrocontrollern; Maximus selbst nutzt eine solche Laufzeit für sein Sprachmodell.
            """),
            sec("Was bringt es?", """
                Ein ResNet-50 hat 25,6 Millionen Parameter: in float32 rund 102 MB, in int8 rund 26 MB. Die Latenz auf dem Handy sinkt typischerweise um den Faktor 2–4, die Genauigkeit oft nur um Bruchteile eines Prozents.

                > Reihenfolge in der Praxis: erst messen (Latenz, Speicher, Genauigkeit), dann quantisieren, nur wenn nötig prunen oder destillieren – und nach jedem Schritt wieder messen.
            """,
                fm("Modellgröße", "Bytes ≈ Parameter × Bytes je Parameter")
            )
        )
    )

    val questions: List<AiQuestion> = quiz("l3") {
        on("tf_basics")
        mc(1, "Welche Form hat ein Stapel von 32 RGB-Bildern der Größe 224 × 224 in TensorFlow (Standard)?",
            "(32, 224, 224, 3)", "(3, 224, 224, 32)", "(224, 224, 32)", "(32, 3, 224, 224)",
            why = "TensorFlow nutzt standardmäßig channels-last: (Batch, Höhe, Breite, Kanäle). PyTorch dagegen (B, C, H, W).")
        mc(1, "Was unterscheidet tf.Variable von tf.Tensor?",
            "Eine Variable ist veränderlich (assign) und wird für trainierbare Gewichte benutzt", "Variablen liegen immer auf der CPU",
            "Tensoren können nicht abgeleitet werden", "Es gibt keinen Unterschied",
            why = "tf.Tensor ist unveränderlich; Gewichte müssen beim Training geändert werden und sind deshalb Variablen.")
        codeOut(1, "Was gibt der Code aus?", """
            x = tf.Variable(3.0)
            with tf.GradientTape() as tape:
                y = x ** 2 + 2 * x
            print(float(tape.gradient(y, x)))
        """, "8.0", "15.0", "6.0", "None",
            why = "dy/dx = 2x + 2 = 8 bei x = 3.")
        code(2, "Warum gibt dieser Gradient None zurück?", """
            x = tf.constant(3.0)
            with tf.GradientTape() as tape:
                y = x * x
            print(tape.gradient(y, x))
        """, "Konstanten werden nicht automatisch beobachtet; tape.watch(x) fehlt", "x * x ist nicht differenzierbar",
            "GradientTape funktioniert nur mit Matrizen", "Man muss tape.gradient vor y berechnen",
            why = "Nur tf.Variable werden automatisch verfolgt. Mit tape.watch(x) oder x = tf.Variable(3.0) kommt 6.0 heraus.")
        mc(2, "Was macht @tf.function?",
            "Es verfolgt die Python-Funktion beim ersten Aufruf und führt sie danach als optimierten Graphen aus", "Es parallelisiert Python-Schleifen auf CPU-Kernen",
            "Es macht die Funktion differenzierbar", "Es speichert die Funktion als Datei",
            why = "Tracing erzeugt einen Graphen; Python-Code läuft dann nicht mehr bei jedem Aufruf. Neue Eingabeformen lösen Retracing aus.")
        code(2, "Warum erscheint die Ausgabe ‚hallo‘ nur einmal, obwohl f dreimal aufgerufen wird?", """
            @tf.function
            def f(x):
                print("hallo")
                return x + 1
            f(tf.constant(1)); f(tf.constant(2)); f(tf.constant(3))
        """, "Python-print läuft nur beim Tracing; der Graph enthält es nicht – tf.print liefe jedes Mal",
            "tf.function unterdrückt alle Ausgaben", "Die Aufrufe zwei und drei werden übersprungen", "Python puffert die Ausgabe",
            why = "Gleiche Form und dtype → kein Retracing; der Graph enthält nur TensorFlow-Operationen.")
        line(2, "Welche Zeile baut eine effiziente Eingabepipeline mit Mischen, Batches und Vorausladen?",
            "ds = tf.data.Dataset.from_tensor_slices((X, y)).shuffle(1000).batch(32).prefetch(tf.data.AUTOTUNE)",
            "ds = tf.data.Dataset(X, y, batch=32, shuffle=True)",
            "ds = tf.data.Dataset.from_tensor_slices(X).batch(32).shuffle(1000).prefetch()",
            "ds = tf.Tensor((X, y)).batch(32)",
            why = "Erst mischen, dann bündeln, dann vorausladen. Mischt man nach batch, werden nur ganze Batches gemischt.")
        mc(3, "Was bedeutet persistent=True beim GradientTape?",
            "Man kann mehrmals tape.gradient aufrufen, z. B. für Ableitungen verschiedener Ziele", "Das Band wird auf die Festplatte geschrieben",
            "Es werden auch Ableitungen zweiter Ordnung automatisch berechnet", "Das Training wird schneller",
            why = "Normal gibt das Band nach dem ersten gradient-Aufruf seine Ressourcen frei. Für höhere Ableitungen schachtelt man Bänder.")

        on("keras_functional")
        mc(1, "Welche Architektur lässt sich mit Sequential nicht bauen?",
            "Ein Modell mit zwei Eingängen, die zusammengeführt werden", "Ein Netz mit drei Dense-Schichten", "Ein CNN mit Pooling", "Ein Netz mit Dropout",
            why = "Sequential erlaubt nur lineare Stapel mit einem Eingang und einem Ausgang. Verzweigungen brauchen die funktionale API.")
        line(1, "Wie definiert man in der funktionalen API das Modell aus inputs und outputs?",
            "model = keras.Model(inputs=inputs, outputs=outputs)",
            "model = keras.Sequential(inputs, outputs)",
            "model = keras.layers.Model(inputs + outputs)",
            "model = keras.compile(inputs, outputs)",
            why = "keras.Model verbindet Eingabe- und Ausgabetensoren und leitet den Graphen dazwischen her.")
        code(2, "Was passiert hier mit den Gewichten?", """
            shared = layers.Dense(64)
            a = shared(input_a)
            b = shared(input_b)
        """, "Beide Aufrufe benutzen dieselben Gewichte – eine geteilte Schicht", "Es entstehen zwei unabhängige Schichten",
            "Der zweite Aufruf überschreibt den ersten", "Das ist ein Fehler, Schichten darf man nur einmal aufrufen",
            why = "Eine Schichtinstanz besitzt ihre Gewichte. Siamesische Netze nutzen das, um zwei Eingaben mit demselben Encoder zu vergleichen.")
        mc(2, "Ein Modell hat Verluste L₁ (MSE) und L₂ (Kreuzentropie) mit loss_weights 1,0 und 0,2. Was wird minimiert?",
            "L₁ + 0,2·L₂", "max(L₁, L₂)", "L₁ · L₂", "Nur L₁",
            why = "Keras summiert die gewichteten Einzelverluste. Die Gewichte gleichen unterschiedliche Größenordnungen aus.")
        code(2, "Warum schlägt diese Residualverbindung fehl?", """
            x = keras.Input(shape=(32,))
            y = layers.Dense(64, activation="relu")(x)
            z = layers.add([x, y])
        """, "x hat 32, y hat 64 Merkmale – add braucht gleiche Formen", "add kann nur zwei Eingänge verarbeiten",
            "Residuen gibt es nur in CNNs", "relu darf nicht vor add stehen",
            why = "Für Residuen muss die Form passen; sonst projiziert man x mit einer Dense(64) ohne Aktivierung auf die richtige Breite.")
        line(2, "In welcher Methode steht beim Subclassing der Vorwärtsweg?",
            "def call(self, x, training=False):",
            "def forward(self, x):",
            "def build(self, x):",
            "def run(self, x):",
            why = "Keras: call. PyTorch heißt die entsprechende Methode forward.")
        mc(2, "Welchen Vorteil hat die funktionale API gegenüber Subclassing?",
            "Der Graph ist statisch bekannt: Formen werden beim Bauen geprüft, summary() und Serialisierung funktionieren zuverlässig",
            "Sie erlaubt beliebige Python-Schleifen im Vorwärtsweg", "Sie ist immer schneller im Training", "Sie braucht keine Eingabeform",
            why = "Subclassing ist flexibler, aber Keras kennt den Graphen erst beim Aufruf.")
        codeOut(3, "Wie viele Parameter hat dieses Modell?", """
            inp = keras.Input(shape=(10,))
            h = layers.Dense(4)(inp)
            out1 = layers.Dense(1)(h)
            out2 = layers.Dense(3)(h)
            model = keras.Model(inp, [out1, out2])
        """, "64", "59", "44", "47",
            why = "10·4 + 4 = 44; 4·1 + 1 = 5; 4·3 + 3 = 15. Summe 64.")

        on("keras_custom_layers")
        mc(2, "Warum legt man die Gewichte einer eigenen Schicht in build an und nicht im Konstruktor?",
            "Erst in build ist die Eingabeform bekannt, aus der die Gewichtsform folgt", "Weil der Konstruktor keine Variablen erlaubt",
            "Damit die Gewichte nicht trainiert werden", "Das ist nur eine Stilfrage",
            why = "Lazy Building: Die Schicht kann so mit beliebiger Eingangsbreite verwendet werden.")
        line(2, "Welche Zeile legt in build eine trainierbare Gewichtsmatrix an?",
            "self.w = self.add_weight(shape=(input_shape[-1], self.units), initializer=\"glorot_uniform\", trainable=True)",
            "self.w = tf.constant(np.random.rand(self.units))",
            "self.w = keras.Input(shape=(self.units,))",
            "self.w = tf.zeros((self.units,), trainable=True)",
            why = "add_weight registriert die Variable bei der Schicht, sodass sie in trainable_weights auftaucht.")
        mc(1, "Was macht EarlyStopping(monitor=\"val_loss\", patience=5)?",
            "Es beendet das Training, wenn sich der Validierungsverlust 5 Epochen lang nicht verbessert", "Es stoppt nach 5 Epochen",
            "Es halbiert die Lernrate nach 5 Epochen", "Es speichert alle 5 Epochen",
            why = "Mit restore_best_weights=True werden danach die Gewichte der besten Epoche zurückgeholt.")
        mc(2, "Wozu dient ReduceLROnPlateau?",
            "Es senkt die Lernrate, wenn die überwachte Größe stagniert", "Es erhöht die Batchgröße", "Es entfernt Schichten", "Es stoppt das Training",
            why = "Feinere Schritte in der Nähe eines Minimums – oft ein Sprung nach unten in der Verlustkurve.")
        code(2, "Welchen Vorteil hat dieser Verlust gegenüber MSE?", """
            def huber(y_true, y_pred, delta=1.0):
                err = y_true - y_pred
                small = tf.abs(err) <= delta
                return tf.where(small, 0.5 * err ** 2, delta * (tf.abs(err) - 0.5 * delta))
        """, "Er ist für große Fehler nur linear und damit robust gegen Ausreißer", "Er ist immer kleiner als null",
            "Er kann nur für Klassifikation benutzt werden", "Er braucht keine Ableitung",
            why = "Huber verbindet das glatte Minimum der MSE mit der Robustheit der MAE.")
        codeOut(2, "Welcher Wert ergibt huber(0, 3) mit delta = 1?", """
            print(float(huber(tf.constant(0.0), tf.constant(3.0))))
        """, "2.5", "4.5", "3.0", "9.0",
            why = "|e| = 3 > δ: δ(|e| − δ/2) = 1·(3 − 0,5) = 2,5. MSE/2 wäre 4,5.")
        line(2, "Welche Methode überschreibt man in einem eigenen Callback, um nach jeder Epoche etwas auszugeben?",
            "def on_epoch_end(self, epoch, logs=None):",
            "def after_epoch(self, epoch):",
            "def epoch_done(self, logs):",
            "def on_train_batch(self, epoch):",
            why = "Keras ruft on_epoch_end mit dem Epochenindex und dem logs-Wörterbuch (loss, val_loss, …) auf.")
        mc(3, "Warum braucht eine eigene Dropout-Schicht ein Argument training in call?",
            "Weil Dropout nur im Training Neuronen abschalten darf; bei der Inferenz muss es durchlassen", "Weil sonst keine Gradienten fließen",
            "Damit Keras die Schicht speichern kann", "Weil training die Lernrate setzt",
            why = "Keras übergibt training=True in fit und training=False in evaluate und predict.")

        on("keras_augment")
        mc(1, "Warum wirkt Datenaugmentation gegen Überanpassung?",
            "Das Netz sieht in jeder Epoche andere Varianten und lernt Invarianzen statt Pixel auswendig", "Sie macht das Netz kleiner",
            "Sie entfernt falsche Labels", "Sie erhöht die Lernrate",
            why = "Augmentation vergrößert die effektive Datenmenge mit labelerhaltenden Transformationen.")
        mc(2, "Welche Augmentation ist bei handgeschriebenen Ziffern gefährlich?",
            "Horizontales Spiegeln und große Drehungen", "Kleine Verschiebungen", "Leichtes Rauschen", "Kleine Helligkeitsänderungen",
            why = "Eine gespiegelte 2 ist keine 2, eine um 180° gedrehte 6 ist eine 9 – das Label würde falsch.")
        mc(2, "Was tun Keras-Augmentationsschichten wie RandomFlip bei model.predict?",
            "Nichts – sie sind nur im Training aktiv", "Sie spiegeln jedes Bild", "Sie mitteln über alle Spiegelungen", "Sie werfen einen Fehler",
            why = "Bei Inferenz (training=False) reichen sie die Eingabe unverändert durch.")
        codeOut(2, "Um wie viel Grad dreht RandomRotation(0.1) höchstens?", """
            layers.RandomRotation(0.1)
        """, "±36°", "±0,1°", "±10°", "±18°",
            why = "Der Faktor ist ein Anteil einer vollen Umdrehung: 0,1 · 360° = 36°.")
        line(1, "Welche Schicht skaliert Pixel von [0, 255] auf [0, 1]?",
            "layers.Rescaling(1.0 / 255)",
            "layers.Normalization(255)",
            "layers.Resizing(255)",
            "layers.Dense(255)",
            why = "Rescaling multipliziert mit einem festen Faktor. Resizing ändert die Bildgröße.")
        code(2, "Was bedeutet class_mode=\"binary\" hier?", """
            gen.flow_from_directory("data/", target_size=(64, 64), batch_size=32, class_mode="binary")
        """, "Zwei Klassenordner; die Labels sind 0 oder 1 für eine Sigmoid-Ausgabe", "Die Bilder werden schwarz-weiß",
            "Es gibt genau zwei Bilder je Batch", "Die Pixel werden binarisiert",
            why = "Bei mehr Klassen nimmt man class_mode=\"categorical\" (One-Hot) oder \"sparse\" (Ganzzahl).")
        mc(3, "Was macht Mixup mit zwei Trainingsbeispielen?",
            "Es bildet eine konvexe Kombination der Bilder und mit demselben λ der Labels", "Es vertauscht ihre Labels",
            "Es klebt die Bilder nebeneinander", "Es löscht das schwächere Beispiel",
            why = "x̃ = λxᵢ + (1 − λ)xⱼ, ỹ = λyᵢ + (1 − λ)yⱼ mit λ ~ Beta(α, α).")
        mc(2, "Darf man die Validierungs- oder Testbilder augmentieren?",
            "Für die Bewertung nein – man misst auf unveränderten Daten; Test-Time-Augmentation ist eine bewusst separate Technik",
            "Ja, immer genauso stark wie im Training", "Ja, sonst ist die Accuracy zu niedrig", "Nur die Testdaten, nie die Trainingsdaten",
            why = "Die Bewertung soll die echte Einsatzsituation abbilden. TTA mittelt Vorhersagen über Varianten und wird separat berichtet.")

        on("keras_transformer")
        mc(2, "Was ist bei Self-Attention in Keras der Aufruf self.att(x, x)?",
            "Query und Value (und damit Key) stammen aus derselben Folge x", "x wird mit sich selbst multipliziert", "Die Folge wird verdoppelt", "Es ist Cross-Attention",
            why = "MultiHeadAttention(query, value, key=value): Bei Self-Attention sind alle aus x. Bei Cross-Attention kommt die Query aus dem Decoder.")
        mc(1, "Wozu dient die Positionseinbettung im Transformer?",
            "Attention ist reihenfolgeblind; die Position muss explizit hinzugefügt werden", "Sie ersetzt die Tokeneinbettung",
            "Sie verkleinert das Vokabular", "Sie beschleunigt die Softmax",
            why = "Ohne Position wäre „Hund beißt Mann“ dasselbe wie „Mann beißt Hund“.")
        code(2, "Welche zwei Unterblöcke hat dieser Encoderblock?", """
            a = self.att(x, x)
            x = self.norm1(x + self.drop(a))
            f = self.ffn(x)
            return self.norm2(x + self.drop(f))
        """, "Multi-Head-Self-Attention und ein positionsweises Feed-Forward-Netz, je mit Residuum und LayerNorm",
            "Zwei Faltungsschichten", "Ein LSTM und eine Dense-Schicht", "Encoder und Decoder",
            why = "Attention mischt zwischen Positionen, das FFN verarbeitet jede Position einzeln. Residuen und Normen stabilisieren das Training.")
        mc(3, "Wie wächst der Rechenaufwand der Self-Attention mit der Folgenlänge T?",
            "Quadratisch, O(T²·d)", "Linear, O(T·d)", "Logarithmisch", "Kubisch",
            why = "Jede Position vergleicht sich mit jeder: Die Matrix QKᵀ ist T × T.")
        line(2, "Welche Schicht macht aus der Folge der Transformer-Ausgaben einen Vektor für die Klassifikation?",
            "layers.GlobalAveragePooling1D()",
            "layers.Flatten3D()",
            "layers.MaxPooling2D()",
            "layers.Embedding(1)",
            why = "Mittelung über die Zeitachse; alternativ ein spezielles [CLS]-Token wie bei BERT.")
        mc(2, "Was ist ein Vorteil von Transformern gegenüber LSTMs bei Zeitreihen?",
            "Alle Zeitschritte werden parallel verarbeitet und lange Abhängigkeiten sind direkt erreichbar", "Sie brauchen keinen Speicher",
            "Ihr Aufwand wächst nur linear", "Sie brauchen keine Trainingsdaten",
            why = "Der Pfad zwischen zwei Zeitpunkten hat Länge 1 statt T; dafür kostet Attention O(T²).")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            mha = layers.MultiHeadAttention(num_heads=4, key_dim=8)
            x = tf.random.normal((2, 10, 32))
            print(mha(x, x).shape)
        """, "(2, 10, 32)", "(2, 4, 10, 8)", "(2, 32)", "(10, 10)",
            why = "Die Ausgabe wird auf die Breite der Query zurückprojiziert: Batch 2, Länge 10, Breite 32.")
        mc(1, "Wie schneidet man eine Zeitreihe für ein Vorhersagemodell zu?",
            "In Fenster der Länge T als Eingabe und den jeweils folgenden Wert als Ziel", "Man mischt alle Zeitpunkte zufällig",
            "Man nimmt nur den letzten Wert", "Man sortiert die Werte der Größe nach",
            why = "Sliding Windows erhalten die zeitliche Struktur; die Aufteilung in Training und Test muss zeitlich bleiben.")

        on("keras_custom_training")
        mc(1, "Welche vier Schritte hat jede Trainingsschleife?",
            "Vorwärts rechnen, Verlust berechnen, Gradienten bestimmen, Gewichte aktualisieren", "Laden, speichern, drucken, beenden",
            "Initialisieren, mischen, testen, exportieren", "Kompilieren, evaluieren, vorhersagen, plotten",
            why = "fit macht genau das für jeden Batch, plus Metriken und Callbacks.")
        line(2, "Welche Zeile wendet die Gradienten an?",
            "optimizer.apply_gradients(zip(grads, model.trainable_weights))",
            "model.trainable_weights -= grads",
            "optimizer.minimize(grads)",
            "tape.apply(grads, model)",
            why = "apply_gradients erwartet Paare (Gradient, Variable).")
        code(2, "Was ist an dieser Schleife falsch?", """
            for x, y in ds:
                with tf.GradientTape() as tape:
                    pred = model(x, training=True)
                loss = loss_fn(y, pred)
                grads = tape.gradient(loss, model.trainable_weights)
        """, "Der Verlust wird außerhalb des Bandes berechnet; der Gradient fehlt dann für die Verlustoperation", "training=True ist verboten",
            "loss_fn erwartet zuerst pred", "Die Gradienten müssen vor dem Vorwärtsdurchlauf berechnet werden",
            why = "Alles zwischen Gewichten und Verlust muss auf dem Band stehen; sonst liefert gradient None.")
        mc(2, "Warum überschreibt man train_step statt eine ganz eigene Schleife zu schreiben?",
            "Man behält fit mit Callbacks, Fortschrittsanzeige und Verteilung, ändert aber die Logik pro Schritt", "Weil eigene Schleifen nicht funktionieren",
            "Weil train_step schneller ist als GradientTape", "Damit das Modell keine Gewichte braucht",
            why = "Ein Mittelweg zwischen Komfort und Kontrolle, z. B. für GANs oder eigene Verluste.")
        mc(2, "Warum sucht man Lernraten auf einer logarithmischen Skala?",
            "Ihr Einfluss hängt vom Größenordnungsverhältnis ab: 10⁻⁴→10⁻³ ist so wichtig wie 10⁻³→10⁻²", "Weil Lernraten negativ sein können",
            "Weil Keras Tuner nichts anderes kann", "Weil die Lernrate ganzzahlig ist",
            why = "Linear zwischen 10⁻⁴ und 10⁻² würden fast alle Proben über 10⁻³ liegen.")
        code(2, "Was ist hp.Int(\"units\", 32, 512, step=32)?", """
            layers.Dense(hp.Int("units", 32, 512, step=32), activation="relu")
        """, "Ein ganzzahliger Hyperparameter mit den Werten 32, 64, …, 512", "Eine Zufallszahl zwischen 32 und 512 ohne Raster",
            "Die Zahl der Epochen", "Die Batchgröße",
            why = "Keras Tuner kennt Int, Float (auch log), Choice und Boolean als Suchräume.")
        mc(3, "Was macht Hyperband?",
            "Es startet viele Konfigurationen mit kleinem Budget und gibt nur den besten mehr Epochen", "Es trainiert alle Konfigurationen gleich lang",
            "Es nutzt Gradienten der Hyperparameter", "Es sucht nur die Lernrate",
            why = "Successive Halving: schlechte Läufe werden früh abgebrochen, das Budget wandert zu den vielversprechenden.")
        mc(2, "Wozu braucht man training=True im Aufruf model(x, training=True)?",
            "Damit Dropout und BatchNorm im Trainingsmodus arbeiten", "Damit Gradienten berechnet werden", "Damit die Gewichte gespeichert werden", "Damit Keras die Form prüft",
            why = "Gradienten zeichnet das Band unabhängig davon auf; training steuert nur das Verhalten modusabhängiger Schichten.")

        on("keras_optimize")
        mc(1, "Wie viel Speicher brauchen 1 Milliarde Parameter in int8?",
            "Etwa 1 GB", "Etwa 4 GB", "Etwa 8 GB", "Etwa 125 MB",
            why = "1 Byte je Parameter. In float32 wären es 4 GB, in int4 rund 0,5 GB.")
        mc(2, "Warum braucht float16-Mixed-Precision Loss Scaling?",
            "Kleine Gradienten würden in float16 auf null abrunden (Unterlauf)", "float16 kann keine negativen Zahlen", "Weil der Verlust zu groß wird", "Weil bfloat16 schneller ist",
            why = "Man multipliziert den Verlust mit einem Faktor, berechnet Gradienten und teilt wieder. bfloat16 hat den Exponentenbereich von float32 und braucht das nicht.")
        code(2, "Warum bekommt die Ausgabeschicht dtype=\"float32\"?", """
            keras.mixed_precision.set_global_policy("mixed_float16")
            outputs = layers.Dense(10, activation="softmax", dtype="float32")(x)
        """, "Softmax und Verlust sollen numerisch stabil in voller Genauigkeit rechnen", "float32 ist schneller als float16",
            "Sonst kann das Modell nicht gespeichert werden", "Weil Dense kein float16 unterstützt",
            why = "Exponentialfunktionen und Logarithmen in float16 können überlaufen oder ungenau werden.")
        mc(2, "Was ist Wissensdestillation?",
            "Ein kleines Schülermodell lernt die weichen Ausgaben eines großen Lehrermodells", "Das Entfernen kleiner Gewichte",
            "Das Umwandeln in int8", "Das Zusammenführen mehrerer Datensätze",
            why = "Die weichen Wahrscheinlichkeiten (mit Temperatur T) tragen mehr Information als harte Labels („diese 7 sieht ein bisschen wie eine 1 aus“).")
        mc(2, "Wann macht Pruning ein Modell tatsächlich schneller?",
            "Wenn die Sparsity strukturiert ist (ganze Kanäle, Blöcke) oder die Hardware dünne Matrizen beschleunigt", "Immer, sobald Gewichte null sind",
            "Nie, Pruning spart nur Speicher", "Nur bei Sigmoid-Netzen",
            why = "Verstreute Nullen in dichten Matrizen werden trotzdem multipliziert. Komprimiert speichern lässt es sich aber.")
        line(2, "Welche Zeile aktiviert Post-Training-Quantisierung im TF-Lite-Konverter?",
            "converter.optimizations = [tf.lite.Optimize.DEFAULT]",
            "converter.quantize = True",
            "converter.dtype = \"int8\"",
            "model.quantize(8)",
            why = "Optimize.DEFAULT quantisiert die Gewichte; mit einem repräsentativen Datensatz auch die Aktivierungen.")
        mc(3, "Affine Quantisierung mit Skala s = 0,1 und Nullpunkt z = 0: Welchen int-Wert bekommt x = 0,73?",
            "7", "73", "0", "8",
            why = "q = round(x/s) + z = round(7,3) = 7. Rückgerechnet x ≈ 0,7 – ein Quantisierungsfehler von 0,03.")
        mc(1, "Wofür ist TF Lite (LiteRT) gedacht?",
            "Für die Ausführung von Modellen auf Handys, eingebetteten Geräten und Mikrocontrollern", "Für das Training sehr großer Modelle",
            "Für die Datenvorverarbeitung", "Für das Plotten von Verlustkurven",
            why = "Kleine Laufzeit, quantisierte Modelle, Hardwarebeschleunigung auf dem Gerät – wie bei Maximus.")
    }
}

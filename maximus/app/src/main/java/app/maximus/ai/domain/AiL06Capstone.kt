package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 6 (IBM „AI Capstone Project with Deep Learning“): geospatial land classification on satellite
 * images — loading data, CNNs in Keras and PyTorch, vision transformers, CNN-ViT hybrids and comparison.
 */
internal object AiL06Capstone {
    val bites: List<Bite> = listOf(
        bite(
            "cap_data", 1, "Bilddaten laden: speicherbasiert gegen generatorbasiert",
            "Das Capstone-Projekt beginnt mit Daten: Satellitenbilder von landwirtschaftlichen und nicht landwirtschaftlichen Flächen, in den Speicher oder als Strom geladen.",
            goals(
                "Die Aufgabe des Capstone-Projekts beschreiben",
                "Speicherbasiertes und generatorbasiertes Laden vergleichen",
                "Datensätze in Keras und PyTorch aus Ordnerstrukturen laden"
            ),
            sec("Die Aufgabe", """
                Im Capstone sollst du Satellitenbildkacheln automatisch in zwei Klassen einteilen: landwirtschaftlich genutzt (agri) oder nicht (non-agri). Solche Modelle helfen bei Ernteprognosen, Landnutzungsplanung und der Überwachung von Entwaldung. Die Daten liegen als Bilddateien in zwei Ordnern, je einer pro Klasse – die übliche Struktur, aus der Keras und PyTorch Labels automatisch ableiten.

                • class_0_non_agri/ … tausende Kacheln
                • class_1_agri/ … tausende Kacheln
            """),
            sec("Speicherbasiert", """
                Man lädt alle Bilder als NumPy-Array in den Arbeitsspeicher. Das ist einfach und schnell, sobald alles geladen ist – aber der Speicherbedarf wächst linear mit der Datenmenge. 10 000 RGB-Bilder mit 64 × 64 Pixeln in float32 brauchen 10 000 · 64 · 64 · 3 · 4 Byte ≈ 492 MB; bei 224 × 224 wären es schon 6 GB.

                $ import numpy as np, glob
                $ from PIL import Image
                $ files = sorted(glob.glob("data/*/*.jpg"))
                $ X = np.stack([np.asarray(Image.open(f).resize((64, 64)), dtype=np.float32) / 255 for f in files])
                $ y = np.array([1 if "class_1_agri" in f else 0 for f in files])
            """,
                fm("Speicherbedarf", "Bytes = N · H · W · C · Bytes je Wert")
            ),
            sec("Generatorbasiert", """
                Ein Generator oder Datenlader liest immer nur einen Batch von der Platte, augmentiert ihn und reicht ihn ans Modell. Der Speicher bleibt konstant, beliebig große Datensätze sind möglich, und mit Parallelität und prefetch wartet die GPU kaum. Preis: etwas mehr Code und Abhängigkeit von der Lesegeschwindigkeit.

                $ import tensorflow as tf
                $ train_ds = tf.keras.utils.image_dataset_from_directory("data/", validation_split=0.2, subset="training", seed=42, image_size=(64, 64), batch_size=32)
                $ train_ds = train_ds.prefetch(tf.data.AUTOTUNE)

                $ from torchvision import datasets, transforms
                $ from torch.utils.data import DataLoader
                $ ds = datasets.ImageFolder("data/", transform=transforms.Compose([transforms.Resize((64, 64)), transforms.ToTensor()]))
                $ loader = DataLoader(ds, batch_size=32, shuffle=True, num_workers=4)
                $ print(ds.class_to_idx)    # {'class_0_non_agri': 0, 'class_1_agri': 1}
            """),
            sec("Prüfen, bevor man trainiert", """
                • Klassenverteilung zählen: Ist sie stark unausgewogen, ist Accuracy eine irreführende Metrik.
                • Einige Bilder mit Label anzeigen – vertauschte Ordner fallen sofort auf.
                • Bildgrößen und Kanäle prüfen (RGB oder RGBA?).
                • Für Satellitenbilder sind Drehungen um 90° und Spiegelungen erlaubte Augmentationen; es gibt kein „oben“.

                > Die Faustregel: Daten, die in den Speicher passen und oft wiederverwendet werden, lädt man einmal; alles Große und Wachsende streamt man.
            """)
        ),
        bite(
            "cap_keras_cnn", 2, "Landklassifikation mit einem Keras-CNN",
            "Ein CNN in Keras für die zwei Landklassen: Architektur, Augmentation, binäre Ausgabe, Training mit Callbacks und Auswertung.",
            goals(
                "Ein CNN für binäre Bildklassifikation in Keras bauen",
                "Augmentation und Callbacks im Projekt einsetzen",
                "Lernkurven lesen und das Modell speichern"
            ),
            sec("Die Architektur", """
                $ from tensorflow import keras
                $ from tensorflow.keras import layers
                $ model = keras.Sequential([
                $     keras.Input(shape=(64, 64, 3)),
                $     layers.RandomFlip("horizontal_and_vertical"), layers.RandomRotation(0.25),
                $     layers.Rescaling(1 / 255),
                $     layers.Conv2D(32, 3, padding="same", activation="relu"), layers.BatchNormalization(), layers.MaxPooling2D(),
                $     layers.Conv2D(64, 3, padding="same", activation="relu"), layers.BatchNormalization(), layers.MaxPooling2D(),
                $     layers.Conv2D(128, 3, padding="same", activation="relu"), layers.BatchNormalization(), layers.MaxPooling2D(),
                $     layers.GlobalAveragePooling2D(),
                $     layers.Dropout(0.5),
                $     layers.Dense(1, activation="sigmoid")
                $ ])

                Muster: Die Kanalzahl verdoppelt sich, während die räumliche Größe halbiert wird (64 → 32 → 16 → 8). GlobalAveragePooling2D mittelt jede Merkmalskarte zu einer Zahl und spart die riesige Flatten-Dense-Verbindung.
            """,
                fm("Größenverlauf", "64 → 32 → 16 → 8, Kanäle 32 → 64 → 128"),
                fm("Conv2D-Parameter", "P = C_out(K²C_in + 1), z. B. 64·(9·32 + 1) = 18 496")
            ),
            sec("Kompilieren und trainieren", """
                $ model.compile(optimizer=keras.optimizers.Adam(1e-3), loss="binary_crossentropy", metrics=["accuracy", keras.metrics.AUC(name="auc")])
                $ cb = [keras.callbacks.EarlyStopping(monitor="val_loss", patience=4, restore_best_weights=True),
                $       keras.callbacks.ModelCheckpoint("land_cnn.keras", save_best_only=True)]
                $ history = model.fit(train_ds, validation_data=val_ds, epochs=30, callbacks=cb)

                Eine Ausgabe mit Sigmoid und binärer Kreuzentropie passt zu zwei Klassen mit Labels 0/1. Die AUC misst die Trennschärfe unabhängig von der Schwelle.
            """),
            sec("Lernkurven lesen", """
                • Trainings- und Validierungsverlust fallen gemeinsam: Das Modell lernt, weiter so.
                • Trainingsverlust fällt, Validierungsverlust steigt: Überanpassung – mehr Augmentation, Dropout oder früher stoppen.
                • Beide bleiben hoch: Unteranpassung – größeres Modell, mehr Epochen oder höhere Lernrate.
                • Validierungsverlust springt stark: zu hohe Lernrate oder zu kleine Validierungsmenge.

                $ import matplotlib.pyplot as plt
                $ plt.plot(history.history["loss"], label="Training"); plt.plot(history.history["val_loss"], label="Validierung"); plt.legend()
            """)
        ),
        bite(
            "cap_pytorch_cnn", 2, "Dieselbe Aufgabe in PyTorch",
            "Das Land-CNN in PyTorch: ImageFolder, nn.Module, Trainings- und Validierungsschleife, Gerät, Speichern – und der direkte Vergleich mit Keras.",
            goals(
                "Ein CNN für das Capstone in PyTorch schreiben",
                "Eine vollständige Trainings- und Validierungsschleife mit Gerätewahl aufsetzen",
                "Keras und PyTorch im Projektalltag vergleichen"
            ),
            sec("Das Modell", """
                $ import torch
                $ from torch import nn
                $ class LandCNN(nn.Module):
                $     def __init__(self):
                $         super().__init__()
                $         def block(c_in, c_out):
                $             return nn.Sequential(nn.Conv2d(c_in, c_out, 3, padding=1), nn.BatchNorm2d(c_out), nn.ReLU(), nn.MaxPool2d(2))
                $         self.features = nn.Sequential(block(3, 32), block(32, 64), block(64, 128))
                $         self.head = nn.Sequential(nn.AdaptiveAvgPool2d(1), nn.Flatten(), nn.Dropout(0.5), nn.Linear(128, 2))
                $     def forward(self, x):
                $         return self.head(self.features(x))

                Hier zwei Logits mit nn.CrossEntropyLoss statt einer Sigmoid-Ausgabe – beides ist für zwei Klassen gleichwertig. AdaptiveAvgPool2d(1) entspricht Kerass GlobalAveragePooling2D.
            """),
            sec("Trainings- und Validierungsschleife", """
                $ device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
                $ model = LandCNN().to(device)
                $ opt = torch.optim.Adam(model.parameters(), lr=1e-3)
                $ crit = nn.CrossEntropyLoss()
                $ for epoch in range(20):
                $     model.train()
                $     for x, y in train_loader:
                $         x, y = x.to(device), y.to(device)
                $         opt.zero_grad(); loss = crit(model(x), y); loss.backward(); opt.step()
                $     model.eval(); correct = 0
                $     with torch.no_grad():
                $         for x, y in val_loader:
                $             correct += (model(x.to(device)).argmax(1) == y.to(device)).sum().item()
                $     print(epoch, correct / len(val_loader.dataset))
                $ torch.save(model.state_dict(), "land_cnn.pt")
            """),
            sec("Keras oder PyTorch?", """
                • Keras: sehr kurzer Code, fit erledigt Schleife, Metriken und Callbacks; ideal für Standardaufgaben und schnelle Prototypen.
                • PyTorch: explizite Schleife, volle Kontrolle, sehr verbreitet in Forschung und bei Hugging Face; Debuggen mit normalem Python.
                • Kanal-Konvention: Keras (B, H, W, C), PyTorch (B, C, H, W).
                • Ausgabe: oft Sigmoid + binäre Kreuzentropie in Keras, zwei Logits + CrossEntropyLoss in PyTorch.

                > Beide erreichen auf diesem Datensatz nahezu dieselbe Genauigkeit. Die Wahl ist eine Frage des Ökosystems, nicht der Qualität.
            """)
        ),
        bite(
            "cap_vit", 3, "Vision Transformer: Bilder als Folge von Patches",
            "Wie der Transformer zu Bildern kam: Patches, lineare Einbettung, Klassentoken, Positionseinbettungen – und warum ViTs viele Daten brauchen.",
            goals(
                "Die Zerlegung eines Bildes in Patches und die Tokenzahl berechnen",
                "Den Aufbau eines ViT mit Klassentoken und Positionseinbettung erklären",
                "Induktive Verzerrungen von CNN und ViT vergleichen"
            ),
            sec("Ein Bild ist eine Folge", """
                Der Vision Transformer (Dosovitskiy et al. 2020, „An Image is Worth 16×16 Words“) zerlegt ein Bild in nicht überlappende Patches, z. B. 16 × 16 Pixel. Jeder Patch wird flachgemacht und linear auf d Dimensionen projiziert – das ist ein Token. Vorn wird ein lernbares Klassentoken [CLS] angehängt, zu allen Tokens eine lernbare Positionseinbettung addiert. Danach folgt ein gewöhnlicher Transformer-Encoder; die Ausgabe am [CLS]-Token geht in den Klassifikationskopf.
            """,
                fm("Tokenzahl", "N = (H/P)(W/P), z. B. (224/16)² = 196, mit [CLS] 197"),
                fm("Patch-Einbettung", "zᵢ = E · flatten(xᵢ) + pᵢ,  E ∈ ℝ^{d × P²C}"),
                fm("Aufwand", "Self-Attention O(N²d)")
            ),
            sec("Patches im Code", """
                Die Patch-Einbettung ist mathematisch eine Faltung mit Kernel = Stride = P:

                $ patch = nn.Conv2d(3, 768, kernel_size=16, stride=16)
                $ x = torch.randn(1, 3, 224, 224)
                $ tokens = patch(x).flatten(2).transpose(1, 2)   # (1, 196, 768)
                $ cls = nn.Parameter(torch.zeros(1, 1, 768))
                $ tokens = torch.cat([cls.expand(1, -1, -1), tokens], dim=1)   # (1, 197, 768)
                $ pos = nn.Parameter(torch.zeros(1, 197, 768))
                $ tokens = tokens + pos

                In Keras baut man dasselbe mit einer Conv2D-Schicht (oder Patches per tf.image.extract_patches) und Dense-Projektion.
            """),
            sec("Induktive Verzerrung", """
                CNNs bringen Annahmen mit: Lokalität (nahe Pixel gehören zusammen) und Verschiebungsäquivarianz (Gewichtsteilung). ViTs haben fast keine solchen Annahmen – jedes Patch kann sich von Anfang an mit jedem anderen austauschen. Folge: Mit wenig Daten sind CNNs überlegen, mit sehr vielen Daten (ImageNet-21k, JFT-300M) überholen ViTs sie, weil sie globale Zusammenhänge flexibel lernen.

                • Kleine Datensätze: CNN, oder ViT nur vortrainiert und feinabgestimmt.
                • Kleinere Patches = mehr Tokens = feinere Details, aber quadratisch mehr Rechenaufwand.
            """),
            sec("Vortrainierte ViTs nutzen", """
                $ import timm
                $ vit = timm.create_model("vit_base_patch16_224", pretrained=True, num_classes=2)
                $ from transformers import ViTForImageClassification
                $ vit2 = ViTForImageClassification.from_pretrained("google/vit-base-patch16-224", num_labels=2, ignore_mismatched_sizes=True)

                > Im Capstone vergleicht man ein selbst trainiertes CNN mit einem ViT – und sieht, wie viel Vortraining für Transformer ausmacht.
            """)
        ),
        bite(
            "cap_hybrid", 3, "CNN-ViT-Hybride und der faire Modellvergleich",
            "CNN als Merkmalsextraktor, Transformer für globale Zusammenhänge – und wie man zwei Modelle mit Konfusionsmatrix, ROC und Signifikanz vergleicht.",
            goals(
                "Einen CNN-ViT-Hybrid aufbauen",
                "Modelle mit Konfusionsmatrix, Precision, Recall, F1, ROC-AUC vergleichen",
                "Einen fairen Vergleich (gleiche Daten, mehrere Läufe) planen"
            ),
            sec("Der Hybrid", """
                Statt rohe Pixel-Patches in den Transformer zu geben, nimmt man die Merkmalskarte eines CNN: Jede Position der 8 × 8-Karte mit 128 Kanälen wird ein Token. Das CNN liefert lokale Merkmale effizient und mit gutem induktivem Bias, der Transformer verknüpft sie global. So kombiniert der Hybrid die Dateneffizienz des CNN mit der Flexibilität des Transformers – im Capstone häufig die beste Variante.

                $ class Hybrid(nn.Module):
                $     def __init__(self, d=128, heads=4, layers=2):
                $         super().__init__()
                $         self.cnn = LandCNN().features                 # (B, 128, 8, 8)
                $         enc = nn.TransformerEncoderLayer(d_model=d, nhead=heads, batch_first=True)
                $         self.tr = nn.TransformerEncoder(enc, num_layers=layers)
                $         self.pos = nn.Parameter(torch.zeros(1, 64, d))
                $         self.head = nn.Linear(d, 2)
                $     def forward(self, x):
                $         f = self.cnn(x).flatten(2).transpose(1, 2)    # (B, 64, 128)
                $         h = self.tr(f + self.pos)
                $         return self.head(h.mean(dim=1))
            """),
            sec("Metriken für den Vergleich", """
                • Konfusionsmatrix: TP, FP, FN, TN – zeigt, welche Fehler passieren.
                • Precision: Wie viele als „agri“ markierte Kacheln sind wirklich agri?
                • Recall: Wie viele echte Agri-Kacheln wurden gefunden?
                • F1: harmonisches Mittel beider.
                • ROC-AUC: Wahrscheinlichkeit, dass ein zufälliges positives Beispiel einen höheren Score bekommt als ein zufälliges negatives – schwellenunabhängig.

                $ from sklearn.metrics import classification_report, confusion_matrix, roc_auc_score
                $ print(confusion_matrix(y_true, y_pred))
                $ print(classification_report(y_true, y_pred, target_names=["non-agri", "agri"]))
                $ print(roc_auc_score(y_true, y_score))
            """,
                fm("Precision und Recall", "P = TP/(TP + FP),  R = TP/(TP + FN)"),
                fm("F1", "F1 = 2PR/(P + R)"),
                fm("AUC als Wahrscheinlichkeit", "AUC = P(s(x⁺) > s(x⁻))")
            ),
            sec("Ein fairer Vergleich", """
                • Gleiche Aufteilung in Training, Validierung und Test für alle Modelle.
                • Gleiche Augmentation und gleiches Budget an Epochen bzw. Rechenzeit – oder ehrlich berichten, wenn nicht.
                • Mehrere Seeds: Mittelwert und Standardabweichung statt eines einzelnen Laufs.
                • Unterschiede von wenigen Zehntelprozent bei kleiner Testmenge sind oft Rauschen: Bei 1000 Testbildern und 95 % Genauigkeit ist der Standardfehler schon 0,7 Prozentpunkte.
                • Auch Kosten berichten: Parameter, Trainingszeit, Inferenzzeit pro Bild.
            """,
                fm("Standardfehler einer Genauigkeit", "SE = √(p(1 − p)/n), z. B. √(0,95·0,05/1000) ≈ 0,0069")
            ),
            sec("Abschluss des Capstones", """
                Am Ende stehen ein Bericht und eine Empfehlung: welches Modell, warum, mit welchen Grenzen. Gute Berichte zeigen Lernkurven, Konfusionsmatrizen, Beispiele für Fehler und eine Tabelle mit Metriken ± Streuung.

                > Ein Modell ist erst fertig, wenn ein anderer Mensch deine Zahlen nachvollziehen und reproduzieren kann.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l6") {
        on("cap_data")
        mc(1, "Was ist die Aufgabe im IBM-Capstone zur Landklassifikation?",
            "Satellitenbildkacheln in landwirtschaftlich und nicht landwirtschaftlich genutzt einteilen", "Wettervorhersage aus Satellitendaten",
            "Gebäude in 3D rekonstruieren", "Wolken aus Bildern entfernen",
            why = "Binäre Bildklassifikation mit Keras und PyTorch, CNN und Vision Transformer im Vergleich.")
        mc(2, "Wie viel Speicher brauchen 10 000 RGB-Bilder mit 64 × 64 Pixeln in float32?",
            "Etwa 492 MB", "Etwa 49 MB", "Etwa 4,9 GB", "Etwa 123 MB",
            why = "10 000 · 64 · 64 · 3 · 4 Byte = 491 520 000 Byte.")
        mc(1, "Was ist der Hauptvorteil eines generatorbasierten Ladens?",
            "Konstanter Speicherbedarf – beliebig große Datensätze sind möglich", "Es ist immer schneller", "Es braucht keine Festplatte", "Es macht Augmentation überflüssig",
            why = "Nur der aktuelle Batch liegt im Speicher; mit Parallelität und prefetch bleibt die GPU ausgelastet.")
        codeOut(2, "Was gibt der Code aus, wenn data/ die Ordner class_0_non_agri und class_1_agri enthält?", """
            ds = datasets.ImageFolder("data/")
            print(ds.class_to_idx)
        """, "{'class_0_non_agri': 0, 'class_1_agri': 1}", "{'agri': 1, 'non_agri': 0}", "[0, 1]", "{'data': 0}",
            why = "ImageFolder sortiert die Ordnernamen alphabetisch und vergibt Indizes.")
        line(2, "Welche Zeile lädt in Keras einen Trainingsdatensatz mit 20 % Validierung aus Ordnern?",
            "tf.keras.utils.image_dataset_from_directory(\"data/\", validation_split=0.2, subset=\"training\", seed=42, image_size=(64, 64))",
            "tf.keras.datasets.load(\"data/\", 0.2)",
            "tf.data.Dataset.from_folder(\"data/\", split=0.8)",
            "keras.utils.load_images(\"data/\", validation=0.2)",
            why = "Für die Validierung ruft man dieselbe Funktion mit subset=\"validation\" und gleichem seed auf.")
        mc(2, "Welche Augmentation ist bei Satellitenbildern unbedenklich, bei Fotos von Straßenszenen aber nicht?",
            "Drehungen um 90° und vertikales Spiegeln", "Leichte Helligkeitsänderung", "Kleine Verschiebung", "Leichtes Rauschen",
            why = "Satellitenbilder haben kein „oben“; eine auf dem Kopf stehende Straßenszene ist unrealistisch.")
        mc(1, "Warum sollte man vor dem Training einige Bilder mit ihren Labels anzeigen?",
            "Um vertauschte Ordner, falsche Formate oder fehlerhafte Labels früh zu entdecken", "Um das Modell zu beschleunigen", "Weil Keras das verlangt", "Um die Bilder zu komprimieren",
            why = "Datenfehler kosten mehr Zeit als jedes Modelltuning.")
        code(3, "Was ist bei dieser Label-Zuweisung riskant?", """
            y = np.array([1 if "agri" in f else 0 for f in files])
        """, "\"agri\" steckt auch in \"non_agri\" – alle Bilder würden Klasse 1", "np.array kann keine Listen", "files ist unsortiert", "Nichts",
            why = "Deshalb prüft man auf den vollständigen Ordnernamen class_1_agri oder nutzt ImageFolder.")

        on("cap_keras_cnn")
        mc(1, "Welche Ausgabeschicht passt zu zwei Klassen mit Labels 0/1 und binary_crossentropy?",
            "Dense(1, activation=\"sigmoid\")", "Dense(2, activation=\"relu\")", "Dense(1)", "Dense(10, activation=\"softmax\")",
            why = "Eine Wahrscheinlichkeit für Klasse 1; alternativ zwei Neuronen mit Softmax und kategorialer Kreuzentropie.")
        mc(2, "Was macht GlobalAveragePooling2D?",
            "Es mittelt jede Merkmalskarte zu einer Zahl – aus (8, 8, 128) wird ein Vektor der Länge 128", "Es halbiert die Bildgröße", "Es mittelt über den Batch", "Es entfernt Kanäle",
            why = "Spart die vielen Parameter einer Flatten-Dense-Verbindung und ist robuster gegen Überanpassung.")
        mc(2, "Wie viele Parameter hat Conv2D(64, 3) auf einer Eingabe mit 32 Kanälen?",
            "18 496", "18 432", "576", "2 112",
            why = "64 · (3·3·32 + 1) = 64 · 289 = 18 496.")
        mc(2, "Der Trainingsverlust fällt weiter, der Validierungsverlust steigt seit Epoche 8. Was tun?",
            "Überanpassung bekämpfen: früher stoppen, mehr Augmentation oder Dropout", "Lernrate erhöhen", "Mehr Epochen trainieren", "Validierungsdaten entfernen",
            why = "EarlyStopping mit restore_best_weights holt die Gewichte von Epoche 8 zurück.")
        code(2, "Was speichert dieser Callback?", """
            keras.callbacks.ModelCheckpoint("land_cnn.keras", save_best_only=True)
        """, "Das Modell nur dann, wenn sich die überwachte Größe (Standard val_loss) verbessert", "Nach jeder Epoche eine neue Datei",
            "Nur die Augmentationsschichten", "Die Trainingsdaten",
            why = "So liegt am Ende das beste Modell auf der Platte, auch wenn das Training danach schlechter wurde.")
        mc(1, "Wozu dient die Metrik AUC im Projekt?",
            "Sie misst die Trennschärfe des Modells unabhängig von der gewählten Schwelle", "Sie zählt die Epochen", "Sie misst den Speicherverbrauch", "Sie ersetzt den Verlust",
            why = "AUC = 1 heißt perfekte Rangfolge, 0,5 Zufall.")
        mc(2, "Warum verdoppelt man oft die Kanalzahl, wenn Pooling die Größe halbiert?",
            "Die Rechenlast pro Schicht bleibt ähnlich, während die Merkmale abstrakter und vielfältiger werden", "Weil Keras es erzwingt", "Damit das Bild größer wird", "Um Speicher zu sparen",
            why = "H·W wird durch 4 geteilt, C verdoppelt – die Aktivierungsgröße halbiert sich, die Ausdruckskraft wächst.")
        line(2, "Welche Zeile zeichnet Trainings- und Validierungsverlust aus history?",
            "plt.plot(history.history[\"loss\"]); plt.plot(history.history[\"val_loss\"])",
            "plt.plot(history.loss, history.val_loss)",
            "plt.plot(model.history)",
            "history.plot(\"loss\")",
            why = "history.history ist ein Wörterbuch mit Listen pro Epoche.")

        on("cap_pytorch_cnn")
        mc(1, "Welche Kanalreihenfolge erwartet PyTorch für Bilder?",
            "(B, C, H, W)", "(B, H, W, C)", "(H, W, C, B)", "(C, B, H, W)",
            why = "Keras/TensorFlow nutzen standardmäßig (B, H, W, C).")
        line(1, "Welche Schicht entspricht Kerass GlobalAveragePooling2D?",
            "nn.AdaptiveAvgPool2d(1)", "nn.AvgPool2d(2)", "nn.Flatten()", "nn.MaxPool2d(1)",
            why = "AdaptiveAvgPool2d(1) liefert (B, C, 1, 1) unabhängig von der Eingangsgröße; danach Flatten.")
        code(2, "Was fehlt in der Validierungsschleife?", """
            for x, y in val_loader:
                pred = model(x.to(device)).argmax(1)
                correct += (pred == y.to(device)).sum().item()
        """, "model.eval() und torch.no_grad() davor", "optimizer.step()", "loss.backward()", "Nichts",
            why = "Ohne eval sind Dropout und BatchNorm im Trainingsmodus; ohne no_grad wird unnötig ein Graph gebaut.")
        mc(2, "Sind zwei Logits mit CrossEntropyLoss und eine Sigmoid-Ausgabe mit BCE für zwei Klassen gleichwertig?",
            "Ja – die Softmax zweier Logits ist die Sigmoid ihrer Differenz", "Nein, Sigmoid ist immer besser", "Nein, CrossEntropy funktioniert nur ab drei Klassen", "Nur auf der GPU",
            why = "Die Parametrisierung unterscheidet sich, die darstellbaren Modelle sind dieselben.")
        codeOut(2, "Welche Form hat die Ausgabe von self.features für 64 × 64-Bilder?", """
            self.features = nn.Sequential(block(3, 32), block(32, 64), block(64, 128))   # jeder block halbiert
            print(self.features(torch.randn(16, 3, 64, 64)).shape)
        """, "torch.Size([16, 128, 8, 8])", "torch.Size([16, 128, 16, 16])", "torch.Size([16, 8, 8, 128])", "torch.Size([16, 128])",
            why = "64 → 32 → 16 → 8 bei 128 Kanälen.")
        line(2, "Welche Zeile wählt automatisch die GPU, falls vorhanden?",
            "device = torch.device(\"cuda\" if torch.cuda.is_available() else \"cpu\")",
            "device = torch.gpu()",
            "torch.set_device(\"auto\")",
            "device = \"gpu\" if torch.has_gpu else \"cpu\"",
            why = "Modell und Daten werden dann mit .to(device) verschoben.")
        mc(2, "Was ist ein typischer Vorteil von PyTorch gegenüber Keras im Projekt?",
            "Volle Kontrolle über die Trainingsschleife und Debuggen mit gewöhnlichem Python", "Es braucht weniger Code", "Es kann keine GPUs nutzen", "Es hat keine Datenlader",
            why = "Keras ist kürzer, PyTorch expliziter – beide sind produktionsreif.")
        mc(3, "Warum teilt man correct durch len(val_loader.dataset) und nicht durch len(val_loader)?",
            "len(val_loader) ist die Zahl der Batches, nicht der Bilder", "Weil val_loader leer ist", "Weil dataset gemischt ist", "Das ist dasselbe",
            why = "Bei 1000 Bildern und Batchgröße 32 wäre len(val_loader) = 32.")

        on("cap_vit")
        mc(1, "Wie viele Patch-Tokens hat ein 224 × 224-Bild bei Patchgröße 16?",
            "196", "224", "16", "256",
            why = "(224/16)² = 14² = 196; mit [CLS]-Token 197.")
        mc(2, "Warum ist die Patch-Einbettung eine Faltung mit Kernel = Stride = P?",
            "Jedes nicht überlappende P × P-Fenster wird mit derselben linearen Abbildung auf d Dimensionen projiziert", "Weil Transformer Faltungen brauchen",
            "Weil Pooling fehlt", "Das ist nur eine Näherung",
            why = "Eine lineare Projektion jedes flachen Patches ist exakt eine Faltung ohne Überlappung.")
        mc(1, "Wozu dient das [CLS]-Token im ViT?",
            "Seine Ausgabe sammelt Information aus allen Patches und geht in den Klassifikationskopf", "Es markiert das Bildende", "Es enthält das Label", "Es ersetzt die Positionseinbettung",
            why = "Alternativ mittelt man über alle Patch-Ausgaben.")
        codeOut(2, "Welche Form hat tokens?", """
            patch = nn.Conv2d(3, 768, kernel_size=16, stride=16)
            tokens = patch(torch.randn(2, 3, 224, 224)).flatten(2).transpose(1, 2)
            print(tokens.shape)
        """, "torch.Size([2, 196, 768])", "torch.Size([2, 768, 196])", "torch.Size([2, 14, 14, 768])", "torch.Size([196, 768])",
            why = "(2, 768, 14, 14) → flatten(2) → (2, 768, 196) → transpose → (2, 196, 768).")
        mc(2, "Warum brauchen ViTs ohne Vortraining viele Daten?",
            "Sie haben kaum induktive Verzerrungen wie Lokalität und müssen diese Strukturen selbst lernen", "Weil sie mehr Schichten haben", "Weil Attention nicht differenzierbar ist", "Das stimmt nicht",
            why = "CNNs bringen Lokalität und Gewichtsteilung als Vorwissen mit und sind deshalb dateneffizienter.")
        mc(3, "Wie ändert sich der Attention-Aufwand, wenn man die Patchgröße von 16 auf 8 halbiert?",
            "Die Tokenzahl vervierfacht sich, der Attention-Aufwand wächst etwa um den Faktor 16", "Er halbiert sich", "Er verdoppelt sich", "Er bleibt gleich",
            why = "N ∝ 1/P², Aufwand ∝ N² ∝ 1/P⁴: (16/8)⁴ = 16.")
        line(2, "Welche Zeile lädt einen vortrainierten ViT mit neuem Kopf für zwei Klassen (timm)?",
            "timm.create_model(\"vit_base_patch16_224\", pretrained=True, num_classes=2)",
            "timm.vit(2)",
            "torch.load(\"vit_base_patch16_224\")",
            "nn.Transformer(pretrained=True, classes=2)",
            why = "num_classes ersetzt den Kopf; der Rest bleibt vortrainiert.")
        mc(2, "Warum addiert man Positionseinbettungen zu den Patch-Tokens?",
            "Self-Attention ist permutationsinvariant und wüsste sonst nicht, wo ein Patch im Bild liegt", "Um die Bilder zu normieren", "Damit das [CLS]-Token entsteht", "Um Speicher zu sparen",
            why = "Ohne Position wären vertauschte Patches ununterscheidbar.")

        on("cap_hybrid")
        mc(2, "Was ist die Idee eines CNN-ViT-Hybrids?",
            "Ein CNN extrahiert lokale Merkmale; jede Position seiner Merkmalskarte wird ein Token für den Transformer", "CNN und ViT stimmen getrennt ab",
            "Der Transformer ersetzt das Pooling", "Zwei CNNs hintereinander",
            why = "Dateneffizienz des CNN plus globale Verknüpfung durch Attention.")
        codeOut(2, "Wie viele Tokens entstehen aus einer Merkmalskarte (B, 128, 8, 8)?", """
            f = feats.flatten(2).transpose(1, 2)
            print(f.shape[1:])
        """, "torch.Size([64, 128])", "torch.Size([128, 64])", "torch.Size([8, 8])", "torch.Size([1024])",
            why = "8 · 8 = 64 Positionen, jede mit 128 Merkmalen.")
        mc(1, "Was misst der Recall für die Klasse agri?",
            "Welcher Anteil der echten Agri-Kacheln gefunden wurde", "Welcher Anteil der Agri-Vorhersagen stimmt", "Die Gesamtgenauigkeit", "Die Trennschärfe",
            why = "R = TP/(TP + FN). Precision fragt umgekehrt nach den Vorhersagen.")
        mc(2, "TP = 80, FP = 20, FN = 10. Wie groß ist F1?",
            "≈ 0,84", "≈ 0,80", "≈ 0,89", "≈ 0,90",
            why = "P = 0,8, R = 80/90 ≈ 0,889; F1 = 2·0,8·0,889/1,689 ≈ 0,842.")
        mc(2, "Was bedeutet eine ROC-AUC von 0,5?",
            "Das Modell trennt die Klassen nicht besser als Zufall", "Perfekte Trennung", "Die Hälfte der Daten fehlt", "Die Schwelle liegt bei 0,5",
            why = "AUC = P(Score eines Positiven > Score eines Negativen); 0,5 ist Münzwurf.")
        mc(3, "Modell A hat 95,2 %, Modell B 94,8 % auf 1000 Testbildern. Was folgt?",
            "Der Unterschied liegt innerhalb des Standardfehlers von etwa 0,7 Prozentpunkten – nicht belastbar ohne mehr Läufe", "A ist klar besser",
            "B ist klar besser", "Beide sind wertlos",
            why = "SE = √(p(1 − p)/n) ≈ 0,0069. Mehrere Seeds und ein gepaarter Test (z. B. McNemar) sind nötig.")
        line(2, "Welche Zeile gibt Precision, Recall und F1 je Klasse aus?",
            "print(classification_report(y_true, y_pred, target_names=[\"non-agri\", \"agri\"]))",
            "print(confusion_matrix(y_pred))",
            "print(roc_auc_score(y_pred))",
            "print(model.metrics(y_true))",
            why = "classification_report fasst alles zusammen, inklusive Support (Zahl der Beispiele je Klasse).")
        mc(1, "Was gehört zu einem fairen Modellvergleich?",
            "Gleiche Datenaufteilung, gleiches Budget, mehrere Seeds und berichtete Streuung", "Jedes Modell auf eigenen Daten testen",
            "Nur den besten Lauf berichten", "Die Testdaten für das Tuning verwenden",
            why = "Sonst vergleicht man Glück und Aufwand statt Modelle.")
    }
}

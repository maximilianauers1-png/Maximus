package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 5 (IBM „Deep Learning with PyTorch“): softmax regression, shallow and deep networks, dropout,
 * weight initialisation, momentum, batch normalisation, convolutional networks and pretrained TorchVision models.
 */
internal object AiL05DeepTorch {
    val bites: List<Bite> = listOf(
        bite(
            "pt_softmax", 1, "Softmax-Regression und MNIST",
            "Mehrklassen-Klassifikation mit einer linearen Schicht: Logits, Softmax, nn.CrossEntropyLoss und argmax – und was die gelernten Gewichte zeigen.",
            goals(
                "Softmax als Verallgemeinerung der Sigmoid verstehen",
                "nn.CrossEntropyLoss korrekt mit Logits und Klassenindizes verwenden",
                "Ein Softmax-Modell auf MNIST trainieren und auswerten"
            ),
            sec("Von zwei zu K Klassen", """
                Bei K Klassen berechnet eine lineare Schicht K Werte zₖ = wₖᵀx + bₖ, die Logits. Die Softmax macht daraus Wahrscheinlichkeiten. Geometrisch zerlegen die K Gewichtsvektoren den Raum in Gebiete, in denen jeweils ein zₖ am größten ist. Für K = 2 reduziert sich Softmax auf die Sigmoid der Logit-Differenz.
            """,
                fm("Softmax", "pₖ = e^{zₖ} / Σⱼ e^{zⱼ}"),
                fm("Vorhersage", "ŷ = argmaxₖ zₖ = argmaxₖ pₖ"),
                fm("K = 2", "p₁ = σ(z₁ − z₀)")
            ),
            sec("Im Code", """
                $ import torch
                $ from torch import nn
                $ from torchvision import datasets, transforms
                $ train = datasets.MNIST("./data", train=True, download=True, transform=transforms.ToTensor())
                $ class SoftMax(nn.Module):
                $     def __init__(self, d_in, d_out):
                $         super().__init__()
                $         self.linear = nn.Linear(d_in, d_out)
                $     def forward(self, x):
                $         return self.linear(x)            # Logits, keine Softmax!
                $ model = SoftMax(28 * 28, 10)
                $ criterion = nn.CrossEntropyLoss()
                $ optimizer = torch.optim.SGD(model.parameters(), lr=0.1)
                $ for x, y in torch.utils.data.DataLoader(train, batch_size=100):
                $     optimizer.zero_grad()
                $     loss = criterion(model(x.view(-1, 28 * 28)), y)
                $     loss.backward(); optimizer.step()
            """),
            sec("CrossEntropyLoss ganz genau", """
                nn.CrossEntropyLoss kombiniert LogSoftmax und negative Log-Likelihood in einem numerisch stabilen Schritt. Daraus folgen zwei Regeln:

                • Das Modell gibt Logits aus – keine Softmax davor, sonst wird doppelt normiert und das Training verlangsamt sich stark.
                • Die Labels sind Klassenindizes vom Typ int64 (torch.long) mit Form (N,), nicht One-Hot.

                Der Gradient bezüglich der Logits ist besonders einfach: p − y (y One-Hot). Ein Modell, das auf MNIST nur mit einer Softmax-Schicht arbeitet, erreicht etwa 92 % Genauigkeit. Visualisiert man die zehn Gewichtsvektoren als 28 × 28-Bilder, sieht man verwaschene „Schablonen“ der Ziffern.
            """,
                fm("Kreuzentropie", "ℓ = −zᵧ + ln Σⱼ e^{zⱼ}"),
                fm("Gradient", "∂ℓ/∂zₖ = pₖ − 1[k = y]")
            ),
            sec("Auswerten", """
                $ correct = 0
                $ with torch.no_grad():
                $     for x, y in test_loader:
                $         z = model(x.view(-1, 784))
                $         _, yhat = torch.max(z, 1)        # Index des größten Logits
                $         correct += (yhat == y).sum().item()
                $ accuracy = correct / len(test_set)

                > Softmax ändert die Reihenfolge der Logits nicht – für argmax braucht man sie gar nicht. Man braucht sie nur, wenn man echte Wahrscheinlichkeiten anzeigen will.
            """)
        ),
        bite(
            "pt_shallow", 2, "Flache Netze: eine versteckte Schicht, mehrere Neuronen, mehrere Klassen",
            "Warum eine versteckte Schicht nichtlineare Grenzen lernt, wie Neuronenzahl und Überanpassung zusammenhängen und wie man Mehrklassen-Netze baut.",
            goals(
                "Ein Netz mit einer versteckten Schicht in PyTorch bauen",
                "Erklären, wie mehrere Neuronen nichtlineare Entscheidungsgrenzen formen",
                "Den Einfluss der Neuronenzahl auf Unter- und Überanpassung einschätzen"
            ),
            sec("Eine versteckte Schicht", """
                $ class Net(nn.Module):
                $     def __init__(self, d_in, H, d_out):
                $         super().__init__()
                $         self.linear1 = nn.Linear(d_in, H)
                $         self.linear2 = nn.Linear(H, d_out)
                $     def forward(self, x):
                $         x = torch.sigmoid(self.linear1(x))
                $         return self.linear2(x)
                $ model = Net(2, 4, 1)          # 2 Eingänge, 4 versteckte Neuronen, 1 Ausgang

                Im IBM-Lab löst so ein Netz das XOR-Problem, das keine einzelne Gerade trennen kann: Die versteckten Neuronen bilden je eine Halbebene, die Ausgabe kombiniert sie zu einem nichtkonvexen Gebiet.
            """,
                fm("Zwei Schichten", "ŷ = W₂ φ(W₁x + b₁) + b₂")
            ),
            sec("Mehr Neuronen, mehr Form", """
                Jedes versteckte Sigmoid-Neuron trägt eine weiche Stufe bei. Mit mehr Neuronen lassen sich immer feinere Grenzen formen – bei Regression eine Treppe aus Stufen, die jede stetige Funktion annähert. Zu wenige Neuronen führen zu Unteranpassung (die Grenze ist zu grob), zu viele bei wenig Daten zu Überanpassung (die Grenze umschlingt einzelne Punkte).

                • Parameterzahl: d_in·H + H + H·d_out + d_out.
                • Die richtige Größe findet man per Validierung – oder man nimmt ein großes Netz und regularisiert (Dropout, Weight Decay, Early Stopping).
            """,
                fm("Parameter", "P = H(d_in + 1) + d_out(H + 1)")
            ),
            sec("Mehrere Klassen", """
                Für K Klassen hat die Ausgabeschicht K Neuronen und man trainiert mit nn.CrossEntropyLoss. Der Unterschied zur Softmax-Regression ist nur die versteckte Schicht dazwischen.

                $ model = nn.Sequential(nn.Linear(784, 100), nn.Sigmoid(), nn.Linear(100, 10))
                $ criterion = nn.CrossEntropyLoss()

                Mit 100 versteckten Neuronen erreicht man auf MNIST rund 97 % – fünf Prozentpunkte mehr als ohne versteckte Schicht.
            """),
            sec("Backpropagation durch zwei Schichten", """
                Der Fehler am Ausgang δ₂ = ŷ − y wird mit W₂ᵀ zurückprojiziert und mit der Ableitung der Aktivierung gewichtet. PyTorch erledigt das mit loss.backward(); trotzdem lohnt es sich, die Formen zu kennen: Jeder Gradient hat genau die Form seines Parameters.
            """,
                fm("Ausgabefehler", "δ₂ = ŷ − y"),
                fm("Versteckter Fehler", "δ₁ = (W₂ᵀ δ₂) ⊙ φ′(z₁)"),
                fm("Gradienten", "∂ℓ/∂W₂ = δ₂ a₁ᵀ,  ∂ℓ/∂W₁ = δ₁ xᵀ")
            )
        ),
        bite(
            "pt_deep", 2, "Tiefe Netze: nn.ModuleList und Aktivierungen im Vergleich",
            "Mehrere versteckte Schichten flexibel bauen, Sigmoid, tanh und ReLU auf MNIST vergleichen, und warum Tiefe hilft.",
            goals(
                "Tiefe Netze mit nn.ModuleList aus einer Liste von Schichtbreiten erzeugen",
                "Den Einfluss der Aktivierungsfunktion auf das Training beurteilen",
                "Erklären, warum Tiefe oft effizienter ist als Breite"
            ),
            sec("Schichten aus einer Liste", """
                $ class Net(nn.Module):
                $     def __init__(self, layers):
                $         super().__init__()
                $         self.hidden = nn.ModuleList()
                $         for d_in, d_out in zip(layers, layers[1:]):
                $             self.hidden.append(nn.Linear(d_in, d_out))
                $     def forward(self, x):
                $         L = len(self.hidden)
                $         for i, layer in enumerate(self.hidden):
                $             x = layer(x)
                $             if i < L - 1:
                $                 x = torch.relu(x)
                $         return x
                $ model = Net([784, 128, 64, 10])

                nn.ModuleList registriert die Schichten, sodass ihre Parameter in model.parameters() auftauchen. Eine gewöhnliche Python-Liste täte das nicht – der Optimierer sähe die Gewichte nicht.
            """),
            sec("Aktivierungen im Vergleich", """
                Im IBM-Lab trainiert man dasselbe Netz mit Sigmoid, tanh und ReLU auf MNIST. Typisches Ergebnis: ReLU und tanh lernen deutlich schneller als Sigmoid, ReLU erreicht die höchste Validierungsgenauigkeit. Mit Sigmoid und mehreren Schichten zeigen sich die verschwindenden Gradienten.

                • tanh ist nullzentriert und hat Ableitung bis 1.
                • ReLU sättigt nicht für positive Eingänge und ist billig zu berechnen.
                • Moderne Netze nutzen oft GELU oder SiLU (glatte ReLU-Varianten).
            """,
                fm("GELU", "GELU(z) = z·Φ(z) ≈ 0,5z(1 + tanh(√(2/π)(z + 0,044715z³)))")
            ),
            sec("Warum Tiefe?", """
                Viele Funktionen lassen sich mit tiefen Netzen exponentiell sparsamer darstellen als mit flachen: Jede Schicht kann die Darstellung der vorigen neu falten. Ein ReLU-Netz zerlegt den Eingaberaum in lineare Stücke; die Zahl dieser Stücke kann mit der Tiefe exponentiell wachsen, mit der Breite nur polynomial. Außerdem lernen tiefe Netze Hierarchien: Kanten → Formen → Objektteile → Objekte.

                > Tiefe bringt Ausdruckskraft, aber auch Trainingsprobleme. Die nächsten Happen behandeln die Werkzeuge dagegen: Dropout, Initialisierung, Momentum, Batch-Normalisierung.
            """)
        ),
        bite(
            "pt_dropout", 2, "Dropout und Gewichtsinitialisierung",
            "Zufälliges Abschalten als Regularisierung mit model.train() und model.eval(), und Initialisierung nach Xavier und He statt Gleichverteilung.",
            goals(
                "Dropout mathematisch und im Code verstehen, inklusive Skalierung",
                "Zwischen Trainings- und Auswertemodus umschalten",
                "Initialisierungen nach Xavier und He begründen und setzen"
            ),
            sec("Dropout", """
                Dropout schaltet im Training jedes Neuron mit Wahrscheinlichkeit p zufällig ab. Kein Neuron kann sich auf ein bestimmtes anderes verlassen; das Netz lernt redundante, robuste Merkmale. Man kann es als implizites Ensemble exponentiell vieler Teilnetze sehen. PyTorch nutzt „inverted dropout“: Die überlebenden Aktivierungen werden mit 1/(1 − p) skaliert, damit der Erwartungswert gleich bleibt und bei der Auswertung nichts angepasst werden muss.

                $ class Net(nn.Module):
                $     def __init__(self, d_in, H, d_out, p=0.5):
                $         super().__init__()
                $         self.drop = nn.Dropout(p=p)
                $         self.l1 = nn.Linear(d_in, H); self.l2 = nn.Linear(H, d_out)
                $     def forward(self, x):
                $         return self.l2(self.drop(torch.relu(self.l1(x))))
            """,
                fm("Inverted Dropout", "ã = m ⊙ a / (1 − p),  mᵢ ~ Bernoulli(1 − p),  E[ã] = a")
            ),
            sec("train() und eval()", """
                $ model.train()                  # Dropout aktiv, BatchNorm nutzt Batch-Statistiken
                $ for x, y in train_loader: ...
                $ model.eval()                   # Dropout aus, BatchNorm nutzt gelernte Mittelwerte
                $ with torch.no_grad():
                $     val_pred = model(x_val)

                Wer model.eval() vergisst, bekommt bei jeder Auswertung andere, verrauschte Vorhersagen. Wer model.train() nach der Validierung vergisst, trainiert ohne Dropout weiter. Im IBM-Lab sieht man: Ohne Dropout passt sich das Netz an verrauschte Daten an, mit p = 0,5 ist die Validierungsgenauigkeit höher.
            """),
            sec("Initialisierung", """
                Alle Gewichte gleich (etwa null) zu setzen ist fatal: Alle Neuronen einer Schicht berechnen dasselbe und bekommen denselben Gradienten – die Symmetrie bricht nie. Zufallswerte müssen aber richtig skaliert sein, sonst wächst oder schrumpft die Varianz der Aktivierungen von Schicht zu Schicht.

                • Xavier/Glorot (für tanh, Sigmoid): Var(w) = 2/(n_in + n_out).
                • He/Kaiming (für ReLU): Var(w) = 2/n_in.
                • PyTorch-Standard für nn.Linear: Kaiming-uniform mit a = √5, also U(−1/√n_in, 1/√n_in).

                $ linear = nn.Linear(784, 256)
                $ nn.init.kaiming_uniform_(linear.weight, nonlinearity="relu")
                $ nn.init.zeros_(linear.bias)
                $ nn.init.xavier_uniform_(tanh_layer.weight)
            """,
                fm("Varianzerhalt", "Var(z) = n_in Var(w) Var(x)  ⇒  Var(w) = 1/n_in (linear), 2/n_in (ReLU)"),
                fm("Xavier uniform", "w ~ U(−√(6/(n_in + n_out)), √(6/(n_in + n_out)))")
            ),
            sec("Was das Lab zeigt", """
                Vergleicht man auf MNIST Gleichverteilung U(0, 1), PyTorch-Standard und He-Initialisierung mit ReLU, so startet die naive Gleichverteilung mit riesigen Aktivierungen und lernt kaum, während He-Initialisierung den Verlust am schnellsten senkt.

                > Dropout gegen Überanpassung, gute Initialisierung gegen schlechten Start – beide kosten eine Zeile Code.
            """)
        ),
        bite(
            "pt_momentum_bn", 2, "Momentum und Batch-Normalisierung",
            "Physikalisch gedacht: Gradientenabstieg mit Trägheit, Nesterov-Variante, und Batch-Normalisierung als Normierung jeder Schicht mit lernbarer Skala.",
            goals(
                "Momentum als gedämpfte Bewegung einer Kugel deuten",
                "Batch-Normalisierung mathematisch formulieren und im Code einsetzen",
                "Den Unterschied zwischen Trainings- und Auswertemodus bei BatchNorm kennen"
            ),
            sec("Momentum", """
                Für dich als Physiker: Gradientenabstieg ohne Momentum ist eine überdämpfte Bewegung – die Kugel kriecht den Hang hinab. Mit Momentum bekommt sie Masse und Trägheit. In langen, schmalen Tälern mittelt sie das Zickzack quer zum Tal heraus und beschleunigt entlang der Talsohle; kleine lokale Minima und Plateaus überrollt sie. β = 0,9 ist der übliche Wert; der Geschwindigkeitsterm ist dann effektiv ein Mittel über etwa 1/(1 − β) = 10 Schritte.

                $ optimizer = torch.optim.SGD(model.parameters(), lr=0.01, momentum=0.9, nesterov=True)
            """,
                fm("Momentum", "v ← βv + ∇ℓ(θ),  θ ← θ − ηv"),
                fm("Nesterov", "v ← βv + ∇ℓ(θ − ηβv),  θ ← θ − ηv"),
                fm("Effektive Schrittweite", "η_eff ≈ η/(1 − β) bei konstantem Gradienten")
            ),
            sec("Batch-Normalisierung", """
                Die Verteilung der Eingänge jeder Schicht ändert sich während des Trainings, weil sich die Schichten davor ändern. Batch-Normalisierung (Ioffe & Szegedy 2015) normiert deshalb jede Größe über den Mini-Batch auf Mittelwert 0 und Varianz 1 und gibt ihr mit γ und β eine lernbare Skala und Verschiebung zurück. Folgen: deutlich höhere Lernraten möglich, weniger Abhängigkeit von der Initialisierung, eine leichte Regularisierung durch das Batch-Rauschen.
            """,
                fm("Normierung", "x̂ = (x − μ_B)/√(σ²_B + ε)"),
                fm("Skala und Verschiebung", "y = γ x̂ + β"),
                fm("Laufende Statistik", "μ_run ← (1 − m)μ_run + m μ_B,  m = 0,1")
            ),
            sec("Im Code", """
                $ class NetBN(nn.Module):
                $     def __init__(self, d_in, H1, H2, d_out):
                $         super().__init__()
                $         self.l1 = nn.Linear(d_in, H1); self.bn1 = nn.BatchNorm1d(H1)
                $         self.l2 = nn.Linear(H1, H2); self.bn2 = nn.BatchNorm1d(H2)
                $         self.l3 = nn.Linear(H2, d_out)
                $     def forward(self, x):
                $         x = torch.relu(self.bn1(self.l1(x)))
                $         x = torch.relu(self.bn2(self.l2(x)))
                $         return self.l3(x)

                Für Bilder nach Faltungen nimmt man nn.BatchNorm2d(Kanäle). Im Training nutzt BatchNorm die Statistik des aktuellen Batches, im eval-Modus die laufenden Mittelwerte. Bei Batchgröße 1 im Training schlägt BatchNorm fehl oder wird unbrauchbar – dort hilft LayerNorm, die in Transformern Standard ist.
            """),
            sec("Wann was?", """
                • Momentum oder Adam: fast immer – reines SGD ohne Momentum ist selten optimal.
                • BatchNorm: CNNs und tiefe MLPs mit ausreichend großen Batches.
                • LayerNorm: Transformer, RNNs, kleine oder variable Batches.
                • Im IBM-Lab konvergiert das Netz mit BatchNorm in weniger Epochen zu höherer Genauigkeit als dasselbe Netz ohne.
            """,
                fm("LayerNorm", "Normierung über die Merkmale eines Beispiels statt über den Batch")
            )
        ),
        bite(
            "pt_cnn", 2, "CNNs in PyTorch: Faltung, Pooling, Kanäle, Ausgabegrößen",
            "nn.Conv2d und nn.MaxPool2d verstehen, Ausgabegrößen berechnen, Kanäle als Merkmalskarten, und ein vollständiges CNN für MNIST.",
            goals(
                "Die Ausgabegröße einer Faltung aus Kernel, Stride und Padding berechnen",
                "Parameterzahlen von Faltungsschichten bestimmen",
                "Ein CNN mit Conv2d, ReLU, MaxPool2d und Linear in PyTorch bauen"
            ),
            sec("Faltung", """
                Ein Faltungskern (z. B. 3 × 3) gleitet über das Bild und bildet an jeder Stelle das Skalarprodukt mit dem darunterliegenden Ausschnitt – genau wie die Kreuzkorrelation in der Signalverarbeitung. Derselbe Kern wird überall verwendet (Gewichtsteilung): Das spart Parameter und macht die Erkennung verschiebungsinvariant. Jeder Kern erzeugt eine Merkmalskarte; mehrere Kerne ergeben mehrere Ausgabekanäle.

                $ conv = nn.Conv2d(in_channels=1, out_channels=16, kernel_size=5, stride=1, padding=2)
                $ x = torch.randn(64, 1, 28, 28)
                $ print(conv(x).shape)        # torch.Size([64, 16, 28, 28])
            """,
                fm("Ausgabegröße", "M = ⌊(N + 2P − K)/S⌋ + 1"),
                fm("Parameter Conv2d", "P = C_out · (C_in · K_h · K_w + 1)")
            ),
            sec("Aktivierung und Pooling", """
                Nach der Faltung folgt eine Aktivierung (ReLU). Max-Pooling nimmt in jedem Fenster (typisch 2 × 2, Stride 2) den größten Wert: Die Karte schrumpft auf ein Viertel, kleine Verschiebungen werden toleriert, und die nächste Schicht sieht einen größeren Bildausschnitt (rezeptives Feld).

                $ pool = nn.MaxPool2d(kernel_size=2, stride=2)
                $ print(pool(torch.relu(conv(x))).shape)   # torch.Size([64, 16, 14, 14])
            """),
            sec("Ein CNN für MNIST", """
                $ class CNN(nn.Module):
                $     def __init__(self, out_1=16, out_2=32):
                $         super().__init__()
                $         self.cnn1 = nn.Conv2d(1, out_1, kernel_size=5, padding=2)
                $         self.cnn2 = nn.Conv2d(out_1, out_2, kernel_size=5, padding=2)
                $         self.pool = nn.MaxPool2d(2)
                $         self.fc = nn.Linear(out_2 * 7 * 7, 10)
                $     def forward(self, x):
                $         x = self.pool(torch.relu(self.cnn1(x)))   # 28 → 14
                $         x = self.pool(torch.relu(self.cnn2(x)))   # 14 → 7
                $         x = x.view(x.size(0), -1)                 # flach: 32·7·7 = 1568
                $         return self.fc(x)

                Das Netz hat 16·(25 + 1) + 32·(16·25 + 1) + 1568·10 + 10 = 416 + 12 832 + 15 690 = 28 938 Parameter – weniger als 5 % eines dichten Netzes mit einer 784er-Schicht – und erreicht über 98 %.
            """),
            sec("Mehrere Eingangskanäle", """
                Bei Farbbildern hat jeder Kern die Tiefe C_in: Ein 3 × 3-Kern auf RGB hat 27 Gewichte und summiert über alle drei Kanäle. Die Ausgabe hat so viele Kanäle, wie es Kerne gibt. Ein 1 × 1-Kern mischt nur Kanäle – ein billiger Weg, die Kanalzahl zu ändern.

                > Formel für die Prüfung: (N + 2P − K)/S + 1. Mit K = 3, P = 1, S = 1 bleibt die Größe gleich („same“-Padding).
            """)
        ),
        bite(
            "pt_pretrained", 2, "Vortrainierte Modelle mit TorchVision",
            "ResNet und Co. aus torchvision.models laden, Gewichte einfrieren, den Kopf austauschen und mit den richtigen Transformationen nachtrainieren.",
            goals(
                "Ein vortrainiertes Modell mit Gewichten laden",
                "Feature-Extraktion (eingefroren) und Fine-Tuning unterscheiden",
                "Die letzte Schicht für eine neue Klassenzahl ersetzen und nur sie trainieren"
            ),
            sec("Laden", """
                $ import torchvision.models as models
                $ weights = models.ResNet18_Weights.DEFAULT
                $ model = models.resnet18(weights=weights)
                $ preprocess = weights.transforms()      # Resize, CenterCrop, ToTensor, Normalize (ImageNet)

                Das Modell wurde auf ImageNet (1,28 Mio. Bilder, 1000 Klassen) trainiert. Seine Faltungsschichten haben allgemeine Merkmale gelernt: Kanten, Texturen, Formen, Objektteile. Die Eingaben müssen genauso vorverarbeitet werden wie im Vortraining – vor allem die ImageNet-Normierung.
            """),
            sec("Einfrieren und Kopf tauschen", """
                $ for p in model.parameters():
                $     p.requires_grad = False                 # Merkmalsextraktor einfrieren
                $ model.fc = nn.Linear(model.fc.in_features, 7)   # neuer Kopf für 7 Klassen, trainierbar
                $ optimizer = torch.optim.Adam(model.fc.parameters(), lr=1e-3)
                $ criterion = nn.CrossEntropyLoss()

                Der neue Kopf ist frisch initialisiert und hat requires_grad=True. Der Optimierer bekommt nur seine Parameter. Bei ResNet-18 hat model.fc 512 Eingänge.
            """,
                fm("Neuer Kopf", "P = 512 · K + K")
            ),
            sec("Feature-Extraktion oder Fine-Tuning?", """
                • Wenig Daten, ähnliche Domäne: alles einfrieren, nur den Kopf trainieren (schnell, kaum Überanpassung).
                • Mehr Daten oder andere Domäne (Röntgen, Satellit): danach die oberen Blöcke auftauen und mit kleiner Lernrate (z. B. 10⁻⁵) nachtrainieren.
                • Unterschiedliche Lernraten je Teil über Parametergruppen im Optimierer.

                $ optimizer = torch.optim.Adam([
                $     {"params": model.layer4.parameters(), "lr": 1e-5},
                $     {"params": model.fc.parameters(), "lr": 1e-3}
                $ ])
            """),
            sec("Auswerten und Fehler finden", """
                Im IBM-Lab klassifiziert man so Bilder (z. B. Risse in Beton oder Zeichen) und schaut sich die falsch klassifizierten Beispiele an. Das ist mehr als Kosmetik: Oft zeigen die Fehler Label-Rauschen, verwirrende Grenzfälle oder systematische Lücken in den Trainingsdaten.

                $ model.eval()
                $ with torch.no_grad():
                $     for x, y in val_loader:
                $         yhat = model(x).argmax(1)
                $         wrong = (yhat != y).nonzero()
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l5") {
        on("pt_softmax")
        mc(1, "Was macht die Softmax mit den Logits?",
            "Sie wandelt sie in positive Wahrscheinlichkeiten um, die sich zu 1 addieren", "Sie setzt negative Werte auf null", "Sie sortiert sie", "Sie normiert sie auf Länge 1",
            why = "pₖ = e^{zₖ}/Σⱼe^{zⱼ}. Die Reihenfolge der Werte bleibt erhalten.")
        code(2, "Was ist an diesem Modell für nn.CrossEntropyLoss falsch?", """
            model = nn.Sequential(nn.Linear(784, 10), nn.Softmax(dim=1))
            criterion = nn.CrossEntropyLoss()
        """, "CrossEntropyLoss enthält bereits LogSoftmax; die zusätzliche Softmax normiert doppelt und bremst das Lernen",
            "Softmax braucht dim=0", "784 ist die falsche Eingangsgröße", "CrossEntropyLoss braucht One-Hot-Labels",
            why = "Das Modell soll Logits ausgeben. Mit doppelter Softmax liegen die Eingaben der Verlustfunktion in [0, 1], die Gradienten werden klein.")
        mc(1, "Welche Labels erwartet nn.CrossEntropyLoss standardmäßig?",
            "Klassenindizes vom Typ int64 mit Form (N,)", "One-Hot-Vektoren vom Typ float", "Wahrscheinlichkeiten", "Strings mit Klassennamen",
            why = "Seit PyTorch 1.10 gehen auch Wahrscheinlichkeitsziele, Standard sind aber Indizes.")
        codeOut(2, "Was gibt der Code aus?", """
            z = torch.tensor([[1.0, 3.0, 2.0], [0.5, 0.1, 0.2]])
            _, yhat = torch.max(z, 1)
            print(yhat)
        """, "tensor([1, 0])", "tensor([3., 0.5])", "tensor([2, 0])", "tensor([1, 2])",
            why = "torch.max entlang Dimension 1 liefert (Werte, Indizes); die größten Logits stehen an Position 1 bzw. 0.")
        mc(2, "Wie lautet der Gradient der Kreuzentropie nach den Logits?",
            "p − y (vorhergesagte minus wahre Verteilung)", "y − p", "p(1 − p)", "ln p",
            why = "Das macht Softmax plus Kreuzentropie so angenehm: kein Sättigungsproblem.")
        mc(2, "Worauf reduziert sich eine Softmax mit zwei Klassen?",
            "Auf die Sigmoid der Logit-Differenz", "Auf ReLU", "Auf tanh", "Auf eine Konstante",
            why = "p₁ = e^{z₁}/(e^{z₀} + e^{z₁}) = 1/(1 + e^{−(z₁ − z₀)}).")
        line(1, "Wie macht man ein MNIST-Batch x der Form (B, 1, 28, 28) für nn.Linear(784, 10) passend?",
            "x.view(-1, 28 * 28)", "x.view(28, 28)", "x.squeeze()", "x.reshape(784, -1)",
            why = "(B, 784): Jede Zeile ist ein flaches Bild. reshape(784, −1) würde Bilder und Pixel vermischen.")
        mc(3, "Wie groß ist die Kreuzentropie bei Logits (0, 0, 0) und wahrer Klasse 0?",
            "ln 3 ≈ 1,10", "0", "1/3", "3",
            why = "Gleichverteilung p = 1/3; −ln(1/3) = ln 3. Ein guter Startwert-Check: Bei K Klassen beginnt das Training um ln K.")

        on("pt_shallow")
        mc(1, "Warum kann ein Netz mit einer versteckten Schicht XOR lösen, eine logistische Regression aber nicht?",
            "Die versteckten Neuronen bilden mehrere Halbebenen, die die Ausgabe zu einer nichtlinearen Grenze kombiniert", "Weil es mehr Daten sieht",
            "Weil es keine Aktivierung hat", "Weil XOR linear trennbar ist",
            why = "XOR braucht zwei Geraden; eine einzelne lineare Grenze reicht nicht.")
        mc(2, "Wie viele Parameter hat ein Netz 2 → 4 → 1?",
            "17", "12", "13", "9",
            why = "2·4 + 4 = 12 und 4·1 + 1 = 5; zusammen 17.")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            model = nn.Sequential(nn.Linear(784, 100), nn.Sigmoid(), nn.Linear(100, 10))
            print(model(torch.randn(32, 784)).shape)
        """, "torch.Size([32, 10])", "torch.Size([32, 100])", "torch.Size([10])", "torch.Size([784, 10])",
            why = "Batch von 32, zehn Logits pro Bild.")
        mc(2, "Was passiert typischerweise mit sehr vielen versteckten Neuronen und wenig Daten?",
            "Überanpassung: Die Grenze umschlingt einzelne Trainingspunkte", "Unteranpassung", "Das Training wird instabil und divergiert immer", "Nichts",
            why = "Mehr Kapazität ohne Regularisierung lernt das Rauschen mit.")
        code(2, "Was fehlt, damit dieses Netz mehr als ein lineares Modell ist?", """
            model = nn.Sequential(nn.Linear(2, 8), nn.Linear(8, 1))
        """, "Eine Aktivierung zwischen den Schichten, z. B. nn.ReLU()", "Ein Bias", "Eine dritte Linear-Schicht", "Ein Optimierer",
            why = "Zwei lineare Schichten hintereinander sind eine lineare Schicht.")
        mc(3, "Wie lautet der Fehlerterm δ₁ der versteckten Schicht in der Backpropagation?",
            "δ₁ = (W₂ᵀδ₂) ⊙ φ′(z₁)", "δ₁ = W₁δ₂", "δ₁ = ŷ − y", "δ₁ = φ(z₁) − x",
            why = "Der Ausgabefehler wird mit W₂ᵀ zurückprojiziert und mit der lokalen Ableitung der Aktivierung multipliziert.")
        mc(1, "Wie viel Genauigkeit gewinnt man auf MNIST etwa durch eine versteckte Schicht mit 100 Neuronen gegenüber reiner Softmax-Regression?",
            "Rund 5 Prozentpunkte (≈ 92 % → ≈ 97 %)", "Rund 50 Prozentpunkte", "Nichts", "Man verliert Genauigkeit",
            why = "Die versteckte Schicht erlaubt nichtlineare Grenzen zwischen den Ziffern.")
        line(2, "Welche Zeile gibt die Zahl der trainierbaren Parameter aus?",
            "print(sum(p.numel() for p in model.parameters() if p.requires_grad))",
            "print(model.num_parameters)",
            "print(len(model.parameters()))",
            "print(model.state_dict().size())",
            why = "numel() zählt die Elemente eines Tensors; len(parameters) gäbe nur die Zahl der Tensoren.")

        on("pt_deep")
        code(2, "Warum würde der Optimierer hier keine Gewichte finden?", """
            class Net(nn.Module):
                def __init__(self):
                    super().__init__()
                    self.hidden = [nn.Linear(784, 64), nn.Linear(64, 10)]
        """, "Eine Python-Liste registriert die Schichten nicht; nn.ModuleList wäre nötig", "Die Schichten brauchen eine Aktivierung",
            "784 ist zu groß", "super().__init__() steht falsch",
            why = "Nur Attribute vom Typ nn.Module, nn.ModuleList, nn.ModuleDict oder nn.Parameter werden registriert.")
        codeOut(2, "Welche Schichten erzeugt Net([784, 128, 64, 10])?", """
            for d_in, d_out in zip(layers, layers[1:]):
                self.hidden.append(nn.Linear(d_in, d_out))
        """, "Linear(784,128), Linear(128,64), Linear(64,10)", "Linear(784,10)", "Linear(784,128), Linear(64,10)", "Vier Schichten",
            why = "zip bildet aufeinanderfolgende Paare: drei Schichten.")
        mc(1, "Welche Aktivierung lernt in tiefen Netzen auf MNIST typischerweise am langsamsten?",
            "Sigmoid", "ReLU", "tanh", "Alle gleich",
            why = "Sigmoid sättigt und hat kleine Ableitungen; über mehrere Schichten verschwinden die Gradienten.")
        mc(2, "Warum ist tanh in versteckten Schichten meist besser als Sigmoid?",
            "tanh ist nullzentriert und hat eine maximale Ableitung von 1 statt 1/4", "tanh ist nicht differenzierbar", "tanh gibt nur positive Werte", "tanh ist linear",
            why = "Nullzentrierte Aktivierungen vermeiden systematisch gleichgerichtete Gradienten der nächsten Schicht.")
        mc(3, "Warum können tiefe ReLU-Netze manche Funktionen sparsamer darstellen als flache?",
            "Die Zahl der linearen Stücke kann mit der Tiefe exponentiell, mit der Breite nur polynomial wachsen", "Weil tiefe Netze weniger Rechenzeit brauchen",
            "Weil flache Netze keine ReLU haben dürfen", "Das stimmt nicht",
            why = "Jede Schicht faltet die Darstellung der vorigen; Komposition multipliziert die Zahl der Regionen.")
        code(2, "Was macht diese Bedingung in forward?", """
            for i, layer in enumerate(self.hidden):
                x = layer(x)
                if i < L - 1:
                    x = torch.relu(x)
        """, "Sie wendet ReLU nach jeder Schicht außer der letzten an, die Logits liefert", "Sie überspringt die letzte Schicht",
            "Sie wendet ReLU nur auf die letzte Schicht an", "Sie bricht die Schleife ab",
            why = "Die Ausgabeschicht bleibt linear, weil CrossEntropyLoss Logits erwartet.")
        mc(2, "Was ist GELU?",
            "Eine glatte ReLU-Variante z·Φ(z), Standard in vielen Transformern", "Eine Pooling-Methode", "Ein Optimierer", "Eine Verlustfunktion",
            why = "GELU gewichtet die Eingabe mit der Wahrscheinlichkeit, dass eine Normalverteilung unter ihr liegt.")
        line(1, "Wie schaltet man in nn.Sequential ReLU zwischen zwei Linear-Schichten?",
            "nn.Sequential(nn.Linear(784, 64), nn.ReLU(), nn.Linear(64, 10))",
            "nn.Sequential(nn.Linear(784, 64), torch.relu, nn.Linear(64, 10))",
            "nn.Sequential(nn.ReLU(784, 64), nn.Linear(64, 10))",
            "nn.Sequential(nn.Linear(784, 64, relu=True), nn.Linear(64, 10))",
            why = "In Sequential gehören Module (nn.ReLU()), keine bloßen Funktionen.")

        on("pt_dropout")
        mc(1, "Was macht nn.Dropout(p=0.5) im Training?",
            "Es setzt jede Aktivierung mit Wahrscheinlichkeit 0,5 auf null und skaliert die übrigen mit 2", "Es löscht die Hälfte der Gewichte dauerhaft",
            "Es halbiert die Lernrate", "Es überspringt jede zweite Epoche",
            why = "Inverted Dropout: Skalierung mit 1/(1 − p) hält den Erwartungswert konstant.")
        mc(1, "Was bewirkt model.eval()?",
            "Dropout wird abgeschaltet und BatchNorm nutzt die laufenden Statistiken", "Gradienten werden abgeschaltet", "Das Modell wird gespeichert", "Die Gewichte werden eingefroren",
            why = "Gradienten schaltet erst torch.no_grad() ab – beides gehört bei der Auswertung zusammen.")
        code(2, "Warum schwanken die Vorhersagen dieses Modells bei gleicher Eingabe?", """
            model = Net(p=0.5)
            train(model)
            print(model(x), model(x))
        """, "model.eval() fehlt – Dropout ist noch aktiv", "Die Gewichte ändern sich bei jedem Aufruf", "x ist zufällig", "PyTorch rundet unterschiedlich",
            why = "Im train-Modus zieht Dropout bei jedem Aufruf eine neue Maske.")
        mc(2, "Warum darf man nicht alle Gewichte mit null initialisieren?",
            "Alle Neuronen einer Schicht rechnen dann dasselbe und bekommen denselben Gradienten – die Symmetrie bricht nie", "Weil null keine Zahl ist",
            "Weil das Netz dann divergiert", "Weil PyTorch es verbietet",
            why = "Zufällige Initialisierung bricht die Symmetrie; Biases dürfen dagegen null sein.")
        mc(2, "Welche Varianz hat die He-Initialisierung?",
            "Var(w) = 2/n_in", "Var(w) = 1", "Var(w) = 2/(n_in + n_out)", "Var(w) = n_in/2",
            why = "Für ReLU, das im Mittel die Hälfte der Signale nullt; Xavier hat 2/(n_in + n_out).")
        line(2, "Welche Zeile initialisiert eine ReLU-Schicht nach He (Kaiming)?",
            "nn.init.kaiming_uniform_(layer.weight, nonlinearity=\"relu\")",
            "nn.init.xavier_uniform_(layer.bias)",
            "layer.weight = torch.zeros_like(layer.weight)",
            "nn.init.uniform_(layer.weight, 0, 1)",
            why = "Der Unterstrich am Ende markiert In-place-Operationen in PyTorch.")
        mc(3, "Warum kann man Dropout als Ensemble deuten?",
            "Jede Maske definiert ein Teilnetz; das Training mittelt implizit über exponentiell viele Teilnetze mit geteilten Gewichten",
            "Weil man mehrere Modelle speichert", "Weil Dropout Bagging auf den Daten macht", "Weil Dropout mehrere Optimierer benutzt",
            why = "Bei n Neuronen gibt es 2ⁿ Masken. Die Skalierung bei der Inferenz approximiert das Mittel ihrer Vorhersagen.")
        codeOut(2, "Welchen Erwartungswert hat ein Ausgabeelement im Training?", """
            drop = nn.Dropout(p=0.25)
            y = drop(torch.ones(1_000_000))
            print(round(y.mean().item(), 2))
        """, "1.0", "0.75", "0.25", "1.33",
            why = "75 % der Einträge werden zu 1/0,75 = 1,33, der Rest 0 – Mittelwert 1.")

        on("pt_momentum_bn")
        mc(1, "Was bewirkt Momentum beim Gradientenabstieg?",
            "Es mittelt vergangene Gradienten, dämpft Zickzack und beschleunigt entlang flacher Täler", "Es setzt die Lernrate auf null",
            "Es normiert die Eingaben", "Es verhindert Überanpassung vollständig",
            why = "Physikalisch: Trägheit. Die Kugel überrollt kleine Mulden und Plateaus.")
        mc(2, "Über etwa wie viele Schritte mittelt Momentum mit β = 0,9?",
            "Etwa 10", "Etwa 0,9", "Etwa 100", "Genau 1",
            why = "Gewichte βᵏ fallen auf 1/e nach ~1/(1 − β) Schritten; die effektive Schrittweite ist η/(1 − β).")
        mc(1, "Worauf normiert Batch-Normalisierung im Training?",
            "Auf Mittelwert und Varianz des aktuellen Mini-Batches, danach lernbare Skala γ und Verschiebung β", "Auf den Mittelwert des ganzen Datensatzes",
            "Auf die Gewichte der Schicht", "Auf den Wertebereich [0, 1]",
            why = "Im eval-Modus werden stattdessen die laufenden Mittelwerte benutzt.")
        line(1, "Welche Schicht normiert die Kanäle nach einer Conv2d mit 32 Ausgabekanälen?",
            "nn.BatchNorm2d(32)", "nn.BatchNorm1d(32)", "nn.LayerNorm(2)", "nn.BatchNorm2d(2)",
            why = "BatchNorm2d normiert je Kanal über Batch, Höhe und Breite.")
        mc(2, "Warum ist BatchNorm bei Batchgröße 1 im Training problematisch?",
            "Varianz und Mittelwert eines einzelnen Beispiels sind unbrauchbar; die Normierung zerstört das Signal", "Weil BatchNorm nur auf GPUs läuft",
            "Weil die Lernrate dann null wird", "Das ist kein Problem",
            why = "Dann nimmt man LayerNorm oder GroupNorm, die nicht vom Batch abhängen.")
        code(2, "Was macht diese Konfiguration zusätzlich zu Momentum?", """
            torch.optim.SGD(params, lr=0.01, momentum=0.9, nesterov=True)
        """, "Den Gradienten an der vorausgeschätzten Position θ − ηβv auswerten (Nesterov)", "Die Lernrate automatisch anpassen",
            "Weight Decay hinzufügen", "Das Momentum abschalten",
            why = "Nesterov schaut voraus und korrigiert früher – oft etwas stabiler.")
        mc(3, "Wozu dienen γ und β in der Batch-Normalisierung?",
            "Sie erlauben dem Netz, jede gewünschte Skala und Verschiebung wiederherzustellen – bis hin zur Identität", "Sie sind die Lernrate und das Momentum",
            "Sie zählen die Batches", "Sie sind feste Konstanten",
            why = "Ohne sie wäre jede Schicht auf Mittelwert 0 und Varianz 1 festgelegt, was die Ausdruckskraft einschränkt.")
        mc(2, "Was ist ein typischer Effekt von BatchNorm im IBM-Lab?",
            "Schnellere Konvergenz zu höherer Genauigkeit, auch bei höheren Lernraten", "Langsameres Training", "Keine Wirkung", "Höherer Trainingsfehler am Ende",
            why = "Die besser konditionierten Schichteingänge erlauben größere Schritte.")

        on("pt_cnn")
        mc(1, "Wie groß ist die Ausgabe einer Faltung mit N = 28, K = 5, P = 2, S = 1?",
            "28", "24", "14", "32",
            why = "(28 + 4 − 5)/1 + 1 = 28 – „same“-Padding.")
        mc(2, "Wie groß ist die Ausgabe bei N = 32, K = 3, P = 0, S = 2?",
            "15", "16", "30", "14",
            why = "⌊(32 − 3)/2⌋ + 1 = ⌊14,5⌋ + 1 = 15.")
        mc(2, "Wie viele Parameter hat nn.Conv2d(3, 16, kernel_size=3)?",
            "448", "432", "144", "160",
            why = "16 · (3·3·3 + 1) = 16 · 28 = 448.")
        codeOut(1, "Welche Form hat die Ausgabe?", """
            x = torch.randn(8, 16, 28, 28)
            print(nn.MaxPool2d(2)(x).shape)
        """, "torch.Size([8, 16, 14, 14])", "torch.Size([8, 8, 28, 28])", "torch.Size([8, 16, 27, 27])", "torch.Size([4, 16, 14, 14])",
            why = "Pooling halbiert Höhe und Breite, die Kanäle bleiben.")
        code(2, "Warum hat self.fc genau 32·7·7 Eingänge?", """
            x = self.pool(torch.relu(self.cnn1(x)))   # 1×28×28 → 16×14×14
            x = self.pool(torch.relu(self.cnn2(x)))   # → 32×7×7
            x = x.view(x.size(0), -1)
        """, "Nach zwei Poolings ist 28 auf 7 geschrumpft, bei 32 Kanälen sind das 1568 Werte je Bild", "Weil 32 die Batchgröße ist",
            "Weil jeder Kern 7 × 7 groß ist", "Das ist eine willkürliche Zahl",
            why = "Faltungen mit padding=2 erhalten die Größe, jedes Pooling halbiert sie.")
        mc(2, "Was bedeutet Gewichtsteilung in CNNs?",
            "Derselbe Kern wird an jeder Bildposition verwendet", "Mehrere Modelle teilen sich einen Speicher", "Die Gewichte werden halbiert", "Jede Schicht hat dieselben Gewichte",
            why = "Das spart Parameter und macht die Merkmalserkennung verschiebungsäquivariant.")
        mc(3, "Wozu dient eine 1 × 1-Faltung?",
            "Sie mischt die Kanäle an jeder Position und ändert billig die Kanalzahl", "Sie macht nichts", "Sie verkleinert das Bild", "Sie ersetzt Pooling",
            why = "Bekannt aus Inception und ResNet-Bottlenecks: C_in → C_out mit C_in·C_out + C_out Parametern.")
        line(2, "Welche Faltung erhält bei Kernel 3 und Stride 1 die räumliche Größe?",
            "nn.Conv2d(16, 32, kernel_size=3, padding=1)",
            "nn.Conv2d(16, 32, kernel_size=3, padding=0)",
            "nn.Conv2d(16, 32, kernel_size=3, stride=2)",
            "nn.Conv2d(16, 32, kernel_size=3, padding=3)",
            why = "P = (K − 1)/2 = 1.")

        on("pt_pretrained")
        mc(1, "Warum funktionieren auf ImageNet vortrainierte Modelle auch für ganz andere Bilder?",
            "Die frühen Schichten haben allgemeine Merkmale wie Kanten und Texturen gelernt", "Weil ImageNet alle Bilder der Welt enthält",
            "Weil die letzte Schicht alle Klassen kennt", "Sie funktionieren nur für ImageNet-Klassen",
            why = "Die Merkmalshierarchie ist weitgehend universell; nur der Kopf ist aufgabenspezifisch.")
        line(1, "Wie friert man alle Parameter eines Modells ein?",
            "for p in model.parameters(): p.requires_grad = False",
            "model.freeze()",
            "model.eval()",
            "torch.no_grad(model)",
            why = "eval() schaltet nur den Modus um; trainierbar bleiben die Gewichte trotzdem.")
        codeOut(2, "Wie viele trainierbare Parameter hat der neue Kopf?", """
            model = models.resnet18(weights=models.ResNet18_Weights.DEFAULT)
            for p in model.parameters(): p.requires_grad = False
            model.fc = nn.Linear(model.fc.in_features, 7)
        """, "3591", "3584", "512", "7",
            why = "512 · 7 + 7 = 3591. Die neu angelegte Schicht ist automatisch trainierbar.")
        mc(2, "Warum muss man die Eingaben mit den ImageNet-Mittelwerten und -Standardabweichungen normieren?",
            "Das Modell wurde mit genau dieser Vorverarbeitung trainiert; andere Skalen verschieben alle Aktivierungen", "Weil Bilder sonst zu groß sind",
            "Weil PyTorch es verlangt", "Es ist egal",
            why = "weights.transforms() liefert die passende Kette automatisch.")
        mc(2, "Wann lohnt es sich, auch obere Blöcke aufzutauen (Fine-Tuning)?",
            "Bei genug Daten oder einer Domäne, die sich deutlich von ImageNet unterscheidet – mit kleiner Lernrate", "Immer sofort mit großer Lernrate",
            "Nie", "Nur wenn das Modell schlechter als Zufall ist",
            why = "Erst den Kopf trainieren, dann vorsichtig auftauen, sonst zerstören große Gradienten die vortrainierten Merkmale.")
        code(2, "Was erreicht dieser Optimierer?", """
            torch.optim.Adam([
                {"params": model.layer4.parameters(), "lr": 1e-5},
                {"params": model.fc.parameters(), "lr": 1e-3}])
        """, "Unterschiedliche Lernraten: der vortrainierte Block lernt vorsichtig, der neue Kopf schnell", "Zwei getrennte Modelle",
            "Er trainiert nur layer4", "Er mittelt die Lernraten",
            why = "Parametergruppen erlauben diskriminatives Fine-Tuning.")
        mc(3, "Warum lohnt der Blick auf falsch klassifizierte Bilder?",
            "Er zeigt Label-Fehler, Grenzfälle und Lücken in den Daten, die keine Metrik verrät", "Er erhöht die Accuracy direkt", "Er ersetzt die Validierung", "Er ist nur Kosmetik",
            why = "Fehleranalyse ist eine der wirksamsten Methoden der Praxis – oft wirksamer als ein größeres Modell.")
        mc(1, "Wie viele Klassen hat ImageNet-1k?",
            "1000", "100", "10", "21 000",
            why = "Rund 1,28 Mio. Trainingsbilder in 1000 Klassen; ImageNet-21k ist die größere Variante.")
    }
}

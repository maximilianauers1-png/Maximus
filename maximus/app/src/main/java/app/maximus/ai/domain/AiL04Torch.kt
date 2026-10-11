package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 4 (IBM „Introduction to Neural Networks and PyTorch“): tensors, autograd, datasets, linear
 * regression by hand and the PyTorch way, mini-batches and optimisers, multiple outputs and logistic
 * regression with cross entropy.
 */
internal object AiL04Torch {
    val bites: List<Bite> = listOf(
        bite(
            "pt_tensors", 1, "Tensoren in PyTorch: 1D, 2D, Indizes, Broadcasting",
            "Der Grundstoff von PyTorch: Tensoren anlegen, Formen ändern, indizieren, rechnen, zwischen NumPy und GPU wechseln.",
            goals(
                "Tensoren anlegen und Form, Datentyp und Gerät abfragen",
                "Mit view/reshape, Indizes und Slices arbeiten",
                "Broadcasting-Regeln anwenden und Elementweise- von Matrixprodukt unterscheiden"
            ),
            sec("Tensoren anlegen", """
                Ein Tensor ist PyTorchs mehrdimensionales Array – wie ein NumPy-Array, aber mit GPU-Unterstützung und automatischer Ableitung.

                $ import torch
                $ a = torch.tensor([0, 1, 2, 3, 4])       # dtype int64
                $ b = torch.tensor([0.0, 1.0, 2.0])       # dtype float32
                $ print(a.dtype, a.type(), a.size(), a.ndimension())   # torch.int64 torch.LongTensor torch.Size([5]) 1
                $ c = a.type(torch.FloatTensor)           # Typ umwandeln
                $ z = torch.zeros(2, 3); o = torch.ones(2, 3); r = torch.randn(2, 3)
                $ lin = torch.linspace(-2, 2, steps=5)    # tensor([-2., -1., 0., 1., 2.])

                Neuronale Netze rechnen fast immer in float32; Ganzzahltensoren (int64) dienen als Klassenlabels und Indizes.
            """),
            sec("Formen, Indizes, Slices", """
                $ a = torch.arange(6)          # tensor([0, 1, 2, 3, 4, 5])
                $ M = a.view(2, 3)             # [[0, 1, 2], [3, 4, 5]]
                $ col = a.view(-1, 1)          # Form (6, 1)
                $ print(M[1, 2], M[:, 1])      # tensor(5) tensor([1, 4])
                $ M[0, 0] = 10                 # verändert auch a (view teilt den Speicher)
                $ x = torch.tensor([5.0]); print(x.item())   # 5.0 als Python-Zahl

                view braucht zusammenhängenden Speicher und teilt ihn mit dem Original; reshape kopiert bei Bedarf. −1 bedeutet „berechne diese Dimension“. Mit .numpy() und torch.from_numpy(arr) wechselt man zu NumPy – ebenfalls speicherteilend.
            """,
                fm("Elementanzahl", "numel = Πᵢ shapeᵢ, z. B. (2, 3) → 6")
            ),
            sec("Rechnen", """
                • Elementweise: u + v, u * v (Hadamard-Produkt), torch.sin(u).
                • Skalarprodukt: torch.dot(u, v) für 1D-Tensoren.
                • Matrixprodukt: torch.mm(A, B) für 2D oder allgemein A @ B (torch.matmul).
                • Reduktionen: a.mean(), a.max(), a.sum(dim=0).

                $ u = torch.tensor([1.0, 2.0]); v = torch.tensor([3.0, 4.0])
                $ print(u * v, torch.dot(u, v))     # tensor([3., 8.]) tensor(11.)
                $ A = torch.tensor([[0.0, 1.0, 1.0], [1.0, 0.0, 1.0]])   # (2, 3)
                $ B = torch.tensor([[1.0, 1.0], [1.0, 1.0], [-1.0, 1.0]])  # (3, 2)
                $ print(A @ B)                       # tensor([[0., 2.], [0., 2.]])
            """,
                fm("Matrixprodukt", "(A B)ᵢⱼ = Σₖ Aᵢₖ Bₖⱼ, (m × n)(n × p) → (m × p)"),
                fm("Skalarprodukt", "u · v = Σᵢ uᵢvᵢ")
            ),
            sec("Broadcasting und Geräte", """
                Bei Operationen mit unterschiedlichen Formen werden Dimensionen von hinten verglichen: Sie passen, wenn sie gleich sind oder eine davon 1 ist; fehlende Dimensionen werden vorne als 1 ergänzt. Ein (3, 1)-Tensor plus ein (4,)-Tensor ergibt (3, 4).

                $ x = torch.arange(3).view(3, 1)    # (3, 1)
                $ y = torch.arange(4)               # (4,)
                $ print((x + y).shape)              # torch.Size([3, 4])
                $ device = "cuda" if torch.cuda.is_available() else "cpu"
                $ t = torch.randn(1000, 1000, device=device)

                > Beide Operanden müssen auf demselben Gerät liegen – „Expected all tensors to be on the same device“ ist einer der häufigsten Fehler.
            """)
        ),
        bite(
            "pt_autograd", 1, "Ableitungen und Autograd",
            "requires_grad, backward und grad: wie PyTorch Ableitungen und partielle Ableitungen automatisch berechnet – und wann man sie abschaltet.",
            goals(
                "Mit requires_grad und backward eine Ableitung berechnen",
                "Partielle Ableitungen mehrerer Variablen ablesen",
                "Gradienten nullen, detach und torch.no_grad korrekt einsetzen"
            ),
            sec("Eine Ableitung", """
                PyTorch baut beim Rechnen einen dynamischen Rechengraphen auf (define-by-run). Ruft man backward auf einem Skalar auf, wendet es die Kettenregel rückwärts an und legt die Ableitungen in .grad der Blätter ab.

                $ x = torch.tensor(2.0, requires_grad=True)
                $ y = x ** 2
                $ y.backward()
                $ print(x.grad)      # tensor(4.)  denn dy/dx = 2x = 4

                Ein zweites Beispiel aus dem IBM-Kurs: y = x² + 2x + 1 bei x = 2 hat die Ableitung 2x + 2 = 6.
            """,
                fm("Beispiele", "d(x²)/dx = 2x;  d(x² + 2x + 1)/dx = 2x + 2")
            ),
            sec("Partielle Ableitungen", """
                $ u = torch.tensor(1.0, requires_grad=True)
                $ v = torch.tensor(2.0, requires_grad=True)
                $ f = u * v + u ** 2
                $ f.backward()
                $ print(u.grad, v.grad)   # tensor(4.) tensor(1.)

                ∂f/∂u = v + 2u = 4 und ∂f/∂v = u = 1. Für einen Tensor x mit vielen Einträgen und eine Funktion, die pro Eintrag angewandt wird, erhält man mit y.sum().backward() die Ableitung an jeder Stelle – so plottet man etwa die Ableitung von ReLU.
            """,
                fm("Partielle Ableitungen", "f = uv + u²  ⇒  ∂f/∂u = v + 2u,  ∂f/∂v = u")
            ),
            sec("Gradienten summieren sich", """
                PyTorch addiert Gradienten bei jedem backward zu .grad, statt sie zu ersetzen. Das ist gewollt (z. B. für Gradientenakkumulation über mehrere Batches), führt aber zu falschen Updates, wenn man vergisst, sie zu nullen.

                $ for step in range(100):
                $     loss = criterion(model(x), y)
                $     optimizer.zero_grad()      # alte Gradienten löschen
                $     loss.backward()            # neue berechnen
                $     optimizer.step()           # Gewichte aktualisieren
            """),
            sec("Abschalten: no_grad und detach", """
                Bei der Auswertung braucht man keine Gradienten. torch.no_grad() spart Speicher und Zeit, weil kein Graph aufgebaut wird. detach() trennt einen Tensor vom Graphen – nötig etwa, um ihn mit NumPy weiterzuverarbeiten oder zu plotten.

                $ with torch.no_grad():
                $     preds = model(X_test)
                $ arr = y_hat.detach().numpy()

                > Merke: requires_grad=True markiert, was abgeleitet wird; backward rechnet; .grad hält das Ergebnis; zero_grad räumt auf.
            """)
        ),
        bite(
            "pt_dataset", 1, "Dataset, Transforms und DataLoader",
            "Daten in PyTorch: eine eigene Dataset-Klasse mit __len__ und __getitem__, Transformationen, torchvision-Datensätze und Mini-Batches mit dem DataLoader.",
            goals(
                "Eine eigene Dataset-Klasse schreiben",
                "Transformationen mit transforms.Compose verketten",
                "Mit dem DataLoader Mini-Batches erzeugen und Iterationen pro Epoche berechnen"
            ),
            sec("Eine eigene Dataset-Klasse", """
                Ein Dataset muss nur zwei Dinge können: sagen, wie viele Beispiele es hat (__len__), und das i-te Beispiel liefern (__getitem__).

                $ from torch.utils.data import Dataset
                $ class ToySet(Dataset):
                $     def __init__(self, length=100, transform=None):
                $         self.x = 2 * torch.ones(length, 2)
                $         self.y = torch.ones(length, 1)
                $         self.len = length
                $         self.transform = transform
                $     def __getitem__(self, index):
                $         sample = self.x[index], self.y[index]
                $         if self.transform:
                $             sample = self.transform(sample)
                $         return sample
                $     def __len__(self):
                $         return self.len
            """),
            sec("Transformationen", """
                Eine Transformation ist ein aufrufbares Objekt, das ein Beispiel verändert. Mehrere verkettet man mit Compose; die Reihenfolge zählt.

                $ from torchvision import transforms
                $ tf = transforms.Compose([
                $     transforms.Resize((224, 224)),
                $     transforms.ToTensor(),                        # PIL-Bild → Tensor (C, H, W) in [0, 1]
                $     transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
                $ ])
                $ from torchvision import datasets
                $ mnist = datasets.MNIST(root="./data", train=True, download=True, transform=transforms.ToTensor())

                Normalize subtrahiert je Kanal den Mittelwert und teilt durch die Standardabweichung – die Werte oben sind die ImageNet-Statistiken, die vortrainierte Modelle erwarten.
            """,
                fm("Normalize", "x′_c = (x_c − μ_c)/σ_c je Kanal c")
            ),
            sec("DataLoader", """
                Der DataLoader holt Beispiele aus dem Dataset, bündelt sie zu Mini-Batches, mischt sie auf Wunsch in jeder Epoche neu und lädt parallel.

                $ from torch.utils.data import DataLoader
                $ loader = DataLoader(dataset=mnist, batch_size=64, shuffle=True, num_workers=2)
                $ for images, labels in loader:
                $     print(images.shape, labels.shape)   # torch.Size([64, 1, 28, 28]) torch.Size([64])
                $     break

                Iterationen pro Epoche: ⌈N / Batchgröße⌉ – bei 60 000 MNIST-Bildern und Batchgröße 64 sind das 938 (der letzte Batch hat 32 Bilder; drop_last=True würde ihn verwerfen).
            """,
                fm("Iterationen pro Epoche", "⌈N / B⌉")
            ),
            sec("Gute Praxis", """
                • Trainings-Loader mit shuffle=True, Validierungs- und Test-Loader ohne.
                • Schwere Augmentation gehört in die Transform-Kette des Trainings-Datasets, nicht in die des Tests.
                • random_split teilt ein Dataset zufällig in Training und Validierung.

                $ from torch.utils.data import random_split
                $ train_set, val_set = random_split(full_set, [50000, 10000], generator=torch.Generator().manual_seed(0))
            """)
        ),
        bite(
            "pt_linreg", 1, "Lineare Regression von Hand: Vorhersage, Verlust, Gradientenabstieg",
            "Die IBM-Methode: erst alles mit Tensoren und Autograd selbst bauen, dann nn.Linear und eine eigene nn.Module-Klasse.",
            goals(
                "Eine Gerade mit Tensoren und Autograd per Gradientenabstieg fitten",
                "Die Gradienten der MSE nach Steigung und Achsenabschnitt herleiten",
                "Dasselbe Modell mit nn.Linear und einer eigenen nn.Module-Klasse ausdrücken"
            ),
            sec("Vorhersage", """
                Lineare Regression mit einem Merkmal: ŷ = w·x + b. In PyTorch gibt es drei Wege, die man im Kurs nacheinander lernt: von Hand mit Tensoren, mit der fertigen Schicht nn.Linear und mit einer eigenen Klasse.

                $ import torch
                $ from torch import nn
                $ w = torch.tensor(2.0, requires_grad=True); b = torch.tensor(-1.0, requires_grad=True)
                $ def forward(x):
                $     return w * x + b
                $ print(forward(torch.tensor([[1.0], [2.0]])))   # tensor([[1.], [3.]])
                $ lr = nn.Linear(in_features=1, out_features=1)
                $ print(list(lr.parameters()))                    # Gewicht und Bias, zufällig initialisiert
            """),
            sec("Verlust und Gradient", """
                Als Verlust dient der mittlere quadratische Fehler. Seine Ableitungen sind einfache Summen – Autograd rechnet sie aus, aber man sollte sie einmal von Hand gesehen haben.
            """,
                fm("Verlust", "ℓ(w, b) = (1/N) Σₙ (yₙ − (w xₙ + b))²"),
                fm("Gradient nach w", "∂ℓ/∂w = −(2/N) Σₙ xₙ (yₙ − ŷₙ)"),
                fm("Gradient nach b", "∂ℓ/∂b = −(2/N) Σₙ (yₙ − ŷₙ)"),
                fm("Update", "w ← w − η ∂ℓ/∂w,  b ← b − η ∂ℓ/∂b")
            ),
            sec("Training von Hand", """
                $ X = torch.arange(-3, 3, 0.1).view(-1, 1)
                $ Y = -3 * X + 1 + 0.1 * torch.randn(X.size())
                $ w = torch.tensor(-15.0, requires_grad=True); b = torch.tensor(-10.0, requires_grad=True)
                $ lr = 0.1
                $ for epoch in range(15):
                $     yhat = w * X + b
                $     loss = torch.mean((yhat - Y) ** 2)
                $     loss.backward()
                $     w.data = w.data - lr * w.grad.data
                $     b.data = b.data - lr * b.grad.data
                $     w.grad.data.zero_(); b.grad.data.zero_()

                Die Zuweisung über .data umgeht Autograd beim Update. Ohne das Nullen würden sich die Gradienten über die Epochen aufsummieren. Mit der Zeit nähert sich w dem wahren Wert −3 und b dem Wert 1. Die Verlustfläche über (w, b) ist ein Paraboloid – im Konturplot sieht man den Weg des Abstiegs.
            """),
            sec("Eine eigene Modulklasse", """
                $ class LR(nn.Module):
                $     def __init__(self, input_size, output_size):
                $         super().__init__()
                $         self.linear = nn.Linear(input_size, output_size)
                $     def forward(self, x):
                $         return self.linear(x)
                $ model = LR(1, 1)
                $ print(model.state_dict())   # OrderedDict mit linear.weight und linear.bias

                model(x) ruft forward über __call__ auf (das auch Hooks ausführt) – forward nie direkt aufrufen. state_dict ist ein Wörterbuch aller Parameter; damit speichert und lädt man Modelle.
            """)
        ),
        bite(
            "pt_sgd", 2, "SGD, Mini-Batches und Optimierer – the PyTorch way",
            "Vom Batch-Gradientenabstieg zu stochastischem und Mini-Batch-Abstieg, nn.MSELoss, torch.optim und die saubere Trainingsschleife mit Validierung.",
            goals(
                "Batch-, stochastischen und Mini-Batch-Gradientenabstieg vergleichen",
                "Die Standard-Trainingsschleife mit optimizer.zero_grad, backward und step schreiben",
                "Hyperparameter per Validierungsverlust wählen und das beste Modell sichern"
            ),
            sec("Drei Arten des Abstiegs", """
                • Batch-Gradientenabstieg: Gradient über alle N Beispiele – genau, aber teuer pro Schritt.
                • Stochastischer Gradientenabstieg (SGD): ein Beispiel pro Schritt – billig, verrauscht; das Rauschen hilft sogar, flache Minima zu finden.
                • Mini-Batch: B Beispiele pro Schritt (typisch 32–512) – der Kompromiss, der GPUs gut auslastet.

                Der Mini-Batch-Gradient ist ein erwartungstreuer Schätzer des vollen Gradienten; seine Varianz sinkt wie 1/B.
            """,
                fm("Mini-Batch-Gradient", "g = (1/B) Σ_{n∈Batch} ∇ℓₙ,  E[g] = ∇ℓ,  Var[g] ∝ 1/B"),
                fm("Iterationen", "Schritte pro Epoche = ⌈N/B⌉")
            ),
            sec("The PyTorch way", """
                $ from torch import nn, optim
                $ from torch.utils.data import DataLoader
                $ model = nn.Linear(1, 1)
                $ criterion = nn.MSELoss()
                $ optimizer = optim.SGD(model.parameters(), lr=0.01)
                $ loader = DataLoader(dataset=train_data, batch_size=16, shuffle=True)
                $ for epoch in range(20):
                $     for x, y in loader:
                $         yhat = model(x)
                $         loss = criterion(yhat, y)
                $         optimizer.zero_grad()
                $         loss.backward()
                $         optimizer.step()

                Der Optimierer kennt die Parameter (model.parameters()) und ihre Gradienten; step() führt das Update aus. Ein Wechsel zu Adam ist eine Zeile: optim.Adam(model.parameters(), lr=1e-3).
            """),
            sec("Training, Validierung, Test", """
                Lernrate und andere Hyperparameter wählt man, indem man mehrere Modelle trainiert und auf Validierungsdaten vergleicht. Im IBM-Lab trainiert man Modelle mit Lernraten 0,0001 bis 0,1 und wählt die mit dem kleinsten Validierungsverlust.

                $ best_loss, best_state = float("inf"), None
                $ for lr in [0.0001, 0.001, 0.01, 0.1]:
                $     model = nn.Linear(1, 1); opt = optim.SGD(model.parameters(), lr=lr)
                $     train(model, opt, epochs=10)
                $     model.eval()
                $     with torch.no_grad():
                $         val = criterion(model(val_x), val_y).item()
                $     if val < best_loss:
                $         best_loss, best_state = val, model.state_dict()
                $ torch.save(best_state, "best_model.pt")
            """),
            sec("Mehr als SGD", """
                • Momentum: v ← βv + g, θ ← θ − ηv. Glättet Zickzack in Schluchten (Lektion 5).
                • Adam: Momentum plus Schrittweite je Parameter aus dem gleitenden Mittel der quadrierten Gradienten – robuste Standardwahl.
                • Lernratenplan: torch.optim.lr_scheduler.StepLR oder CosineAnnealingLR senken die Lernrate im Verlauf.
            """,
                fm("Adam", "m ← β₁m + (1−β₁)g,  v ← β₂v + (1−β₂)g²,  θ ← θ − η m̂/(√v̂ + ε)")
            )
        ),
        bite(
            "pt_multi", 2, "Mehrere Eingänge und Ausgänge: nn.Linear in Matrixform",
            "Multiple lineare Regression und mehrere Ziele gleichzeitig: Gewichtsmatrizen, Formen, Parameterzahlen und Batch-Rechnung.",
            goals(
                "nn.Linear(in, out) als ŷ = xWᵀ + b lesen",
                "Formen von Gewichten, Bias und Ausgaben vorhersagen",
                "Ein Modell mit mehreren Ausgängen trainieren"
            ),
            sec("Mehrere Eingänge", """
                Mit d Merkmalen ist die Vorhersage ein Skalarprodukt plus Bias. Für einen Batch X mit N Zeilen rechnet nn.Linear alles auf einmal. PyTorch speichert das Gewicht als (out, in)-Matrix und rechnet xWᵀ.

                $ model = nn.Linear(in_features=2, out_features=1)
                $ print(model.weight.shape, model.bias.shape)   # torch.Size([1, 2]) torch.Size([1])
                $ X = torch.tensor([[1.0, 1.0], [1.0, 2.0], [1.0, 3.0]])
                $ print(model(X).shape)                          # torch.Size([3, 1])
            """,
                fm("nn.Linear", "ŷ = x Wᵀ + b,  W ∈ ℝ^{out×in},  b ∈ ℝ^{out}"),
                fm("Parameterzahl", "P = in · out + out")
            ),
            sec("Mehrere Ausgänge", """
                Will man mehrere Zielgrößen gleichzeitig vorhersagen – etwa Temperatur und Luftfeuchte –, setzt man out_features > 1. Jede Ausgabe hat ihre eigene Zeile in W und ihren eigenen Bias; die MSE mittelt über alle Ausgaben.

                $ model = nn.Linear(2, 2)
                $ x = torch.tensor([[1.0, 2.0]])
                $ print(model(x).shape)     # torch.Size([1, 2])

                Mit einer eigenen Klasse:

                $ class MultiLR(nn.Module):
                $     def __init__(self, d_in, d_out):
                $         super().__init__()
                $         self.linear = nn.Linear(d_in, d_out)
                $     def forward(self, x):
                $         return self.linear(x)
            """),
            sec("Geometrie", """
                Mit zwei Merkmalen beschreibt das Modell eine Ebene über der (x₁, x₂)-Ebene, mit d Merkmalen eine Hyperebene. Der Gradient der MSE nach W ist (2/N)(Ŷ − Y)ᵀX – dieselbe Normalengleichungsstruktur wie bei scikit-learn, nur iterativ statt geschlossen gelöst. Für große d und N ist der iterative Weg günstiger als das Invertieren von XᵀX (Aufwand O(d³)).
            """,
                fm("Gradient", "∂ℓ/∂W = (2/N)(Ŷ − Y)ᵀ X"),
                fm("Aufwand", "geschlossen O(N d² + d³), iterativ O(N d) pro Epoche")
            )
        ),
        bite(
            "pt_logreg", 2, "Logistische Regression in PyTorch: Sigmoid, Kreuzentropie, Initialisierung",
            "Binäre Klassifikation mit nn.Sequential(nn.Linear, nn.Sigmoid), warum MSE hier schlecht ist, BCELoss und BCEWithLogitsLoss, und was schlechte Initialisierung anrichtet.",
            goals(
                "Logistische Regression als lineares Modell plus Sigmoid bauen",
                "Begründen, warum man Kreuzentropie statt MSE verwendet",
                "BCELoss und BCEWithLogitsLoss korrekt einsetzen und Schwellen anwenden"
            ),
            sec("Das Modell", """
                Logistische Regression ist eine lineare Regression, deren Ausgabe durch die Sigmoid-Funktion in eine Wahrscheinlichkeit gedrückt wird. Ab der Schwelle 0,5 sagt man Klasse 1 voraus; die Entscheidungsgrenze wx + b = 0 ist eine Gerade bzw. Hyperebene.

                $ model = nn.Sequential(nn.Linear(1, 1), nn.Sigmoid())
                $ yhat = model(torch.tensor([[1.0], [100.0]]))
                $ pred = (yhat > 0.5).float()
            """,
                fm("Modell", "p = σ(wᵀx + b) = 1/(1 + e^{−(wᵀx + b)})"),
                fm("Entscheidungsgrenze", "wᵀx + b = 0  ⇔  p = 0,5")
            ),
            sec("Warum nicht MSE?", """
                Mit Sigmoid und MSE ist die Verlustfläche nicht konvex und hat flache Plateaus: Wenn das Modell sehr sicher falsch liegt, ist σ′ fast null und der Gradient verschwindet. Die Kreuzentropie (negative Log-Likelihood der Bernoulli-Verteilung) ist für logistische Regression konvex, und ihr Gradient ist schlicht (p − y)x.

                Im IBM-Lab sieht man das an „schlechter Initialisierung“: Startet man mit w = −15, b = −10 und MSE, bleibt das Training auf einem Plateau hängen; mit Kreuzentropie läuft es zuverlässig.
            """,
                fm("Binäre Kreuzentropie", "ℓ = −(1/N) Σ [yₙ ln pₙ + (1 − yₙ) ln(1 − pₙ)]"),
                fm("Gradient", "∂ℓ/∂w = (1/N) Σ (pₙ − yₙ) xₙ")
            ),
            sec("BCELoss und BCEWithLogitsLoss", """
                $ criterion = nn.BCELoss()                 # erwartet Wahrscheinlichkeiten (nach Sigmoid)
                $ loss = criterion(model(x), y)
                $ logit_model = nn.Linear(2, 1)            # ohne Sigmoid
                $ criterion2 = nn.BCEWithLogitsLoss()      # Sigmoid ist eingebaut, numerisch stabiler
                $ loss2 = criterion2(logit_model(x), y)

                BCEWithLogitsLoss rechnet Sigmoid und Logarithmus gemeinsam mit dem log-sum-exp-Trick – keine Probleme mit ln(0). Die Labels müssen Gleitkommazahlen (0.0/1.0) mit derselben Form wie die Ausgabe sein.
            """),
            sec("Bewerten", """
                $ model.eval()
                $ with torch.no_grad():
                $     probs = torch.sigmoid(logit_model(X_test))
                $     acc = ((probs > 0.5).float() == y_test).float().mean().item()

                Die Schwelle 0,5 ist nur der Standard. Bei seltenen positiven Fällen (Betrug, Krankheit) verschiebt man sie, um den Recall zu erhöhen – das Spiel „Schwellen-Regler“ auf der Spielwiese zeigt die Abwägung.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l4") {
        on("pt_tensors")
        codeOut(1, "Was gibt der Code aus?", """
            a = torch.tensor([0, 1, 2, 3, 4])
            print(a.dtype, a.ndimension())
        """, "torch.int64 1", "torch.float32 1", "torch.int32 5", "torch.int64 5",
            why = "Ganzzahlen werden zu int64 (LongTensor); ein 1D-Tensor hat eine Dimension, aber fünf Elemente.")
        codeOut(1, "Welche Form hat b?", """
            a = torch.arange(12)
            b = a.view(3, -1)
            print(b.shape)
        """, "torch.Size([3, 4])", "torch.Size([4, 3])", "torch.Size([3, -1])", "torch.Size([12, 3])",
            why = "−1 wird berechnet: 12/3 = 4.")
        codeOut(1, "Was gibt der Code aus?", """
            u = torch.tensor([1.0, 2.0]); v = torch.tensor([3.0, 4.0])
            print(u * v, torch.dot(u, v))
        """, "tensor([3., 8.]) tensor(11.)", "tensor(11.) tensor([3., 8.])", "tensor([4., 6.]) tensor(11.)", "tensor([3., 8.]) tensor([11.])",
            why = "* ist elementweise; dot summiert die Produkte: 3 + 8 = 11.")
        code(2, "Was passiert mit a?", """
            a = torch.arange(6)
            M = a.view(2, 3)
            M[0, 0] = 10
            print(a[0])
        """, "a[0] ist jetzt 10, weil view den Speicher teilt", "a bleibt unverändert, view kopiert", "Es gibt einen Fehler", "a[0] ist 0, M[0,0] ist 10",
            why = "view erzeugt nur eine neue Sicht auf dieselben Daten. clone() würde kopieren.")
        codeOut(2, "Welche Form hat das Ergebnis (Broadcasting)?", """
            x = torch.ones(3, 1)
            y = torch.ones(4)
            print((x + y).shape)
        """, "torch.Size([3, 4])", "torch.Size([3, 1])", "torch.Size([4])", "Fehler: Formen passen nicht",
            why = "(3, 1) und (4,) → (3, 1) und (1, 4) → (3, 4).")
        line(1, "Wie bekommt man aus einem Ein-Element-Tensor eine Python-Zahl?",
            "x.item()", "x.value()", "x.numpy()[0][0]", "float.from_tensor(x)",
            why = "item() liefert eine Python-Zahl; numpy() ein Array.")
        code(2, "Warum schlägt diese Zeile fehl?", """
            model = model.to("cuda")
            x = torch.randn(8, 10)
            y = model(x)
        """, "x liegt auf der CPU, das Modell auf der GPU – beide müssen auf demselben Gerät sein", "randn erzeugt keine Gleitkommazahlen",
            "model ist nach .to nicht mehr aufrufbar", "Die Batchgröße 8 ist nicht erlaubt",
            why = "x = x.to(\"cuda\") (oder device=...) löst den Fehler „Expected all tensors to be on the same device“.")
        codeOut(2, "Was ergibt A @ B?", """
            A = torch.tensor([[1.0, 2.0], [3.0, 4.0]])
            B = torch.tensor([[1.0], [1.0]])
            print(A @ B)
        """, "tensor([[3.], [7.]])", "tensor([[1., 2.], [3., 4.]])", "tensor([[4.], [6.]])", "tensor([3., 7.])",
            why = "(2 × 2)(2 × 1) = (2 × 1): Zeilensummen 1 + 2 = 3 und 3 + 4 = 7.")

        on("pt_autograd")
        codeOut(1, "Was gibt der Code aus?", """
            x = torch.tensor(2.0, requires_grad=True)
            y = x ** 2 + 2 * x + 1
            y.backward()
            print(x.grad)
        """, "tensor(6.)", "tensor(9.)", "tensor(4.)", "None",
            why = "dy/dx = 2x + 2 = 6 bei x = 2.")
        codeOut(2, "Was sind u.grad und v.grad?", """
            u = torch.tensor(1.0, requires_grad=True)
            v = torch.tensor(2.0, requires_grad=True)
            f = u * v + u ** 2
            f.backward()
            print(u.grad, v.grad)
        """, "tensor(4.) tensor(1.)", "tensor(2.) tensor(1.)", "tensor(3.) tensor(2.)", "tensor(1.) tensor(4.)",
            why = "∂f/∂u = v + 2u = 4; ∂f/∂v = u = 1.")
        code(2, "Was passiert, wenn man das zweimal ausführt, ohne zu nullen?", """
            y = (w * x).sum()
            y.backward()
        """, "Die Gradienten werden in w.grad addiert und sind beim zweiten Mal doppelt so groß", "Der zweite Aufruf ersetzt den Gradienten",
            "PyTorch nullt automatisch", "Es gibt immer einen Fehler",
            why = "PyTorch akkumuliert. Ohne optimizer.zero_grad() sind die Updates falsch. (Ein zweites backward auf demselben Graphen bräuchte zudem retain_graph=True.)")
        mc(1, "Wozu dient torch.no_grad()?",
            "Es schaltet die Graphaufzeichnung ab – für Auswertung und Inferenz spart das Speicher und Zeit", "Es setzt alle Gradienten auf null",
            "Es friert das Modell dauerhaft ein", "Es verhindert Überanpassung",
            why = "Innerhalb des Blocks entstehen keine Graphen; requires_grad wird für Ergebnisse False.")
        line(2, "Wie wandelt man einen Tensor mit requires_grad=True in ein NumPy-Array um?",
            "arr = y_hat.detach().numpy()", "arr = y_hat.numpy()", "arr = numpy(y_hat.grad)", "arr = y_hat.tolist().numpy()",
            why = "numpy() ist auf Tensoren mit Gradient verboten; detach() trennt vom Graphen (bei GPU-Tensoren zusätzlich .cpu()).")
        mc(2, "Was heißt ‚define-by-run‘ bei PyTorch?",
            "Der Rechengraph entsteht dynamisch beim Ausführen des Codes; Schleifen und Bedingungen sind normales Python", "Man muss den Graphen vorher deklarieren",
            "Modelle laufen nur einmal", "Gradienten werden vorab berechnet",
            why = "Jeder Vorwärtsdurchlauf baut seinen eigenen Graphen – ideal zum Debuggen und für variable Strukturen.")
        codeOut(2, "Was ist x.grad für ReLU an diesen Stellen?", """
            x = torch.tensor([-2.0, 3.0], requires_grad=True)
            torch.relu(x).sum().backward()
            print(x.grad)
        """, "tensor([0., 1.])", "tensor([-2., 3.])", "tensor([1., 1.])", "tensor([0., 3.])",
            why = "ReLU′ ist 0 für negative und 1 für positive Eingänge. sum() macht aus dem Vektor einen Skalar für backward.")
        mc(3, "Warum braucht backward() normalerweise einen Skalar?",
            "Reverse-Mode berechnet den Gradienten einer skalaren Größe; für Vektorausgaben muss man einen Vektor für das Vektor-Jacobi-Produkt angeben",
            "Weil PyTorch keine Vektoren ableiten kann", "Weil Skalare schneller sind", "Das ist nicht so, backward akzeptiert alles",
            why = "y.backward(v) berechnet vᵀJ. Ein Verlust ist ohnehin skalar.")

        on("pt_dataset")
        mc(1, "Welche zwei Methoden muss eine eigene Dataset-Klasse implementieren?",
            "__len__ und __getitem__", "fit und predict", "forward und backward", "__init__ und __call__",
            why = "Der DataLoader fragt die Länge ab und holt Beispiele per Index.")
        mc(1, "Wie viele Iterationen hat eine Epoche bei 60 000 Bildern und Batchgröße 64?",
            "938", "937", "64", "60 000",
            why = "⌈60 000 / 64⌉ = 938; der letzte Batch hat 32 Bilder. Mit drop_last=True wären es 937.")
        codeOut(1, "Welche Form hat images im ersten Batch?", """
            loader = DataLoader(datasets.MNIST("./data", transform=transforms.ToTensor()), batch_size=64)
            images, labels = next(iter(loader))
            print(images.shape)
        """, "torch.Size([64, 1, 28, 28])", "torch.Size([64, 28, 28, 1])", "torch.Size([28, 28])", "torch.Size([1, 64, 28, 28])",
            why = "PyTorch nutzt (Batch, Kanäle, Höhe, Breite); MNIST hat einen Graustufenkanal.")
        mc(2, "Was macht transforms.ToTensor() mit einem PIL-Bild?",
            "Es erzeugt einen Tensor der Form (C, H, W) mit Werten in [0, 1]", "Es normiert auf Mittelwert 0", "Es verkleinert das Bild", "Es macht das Bild grau",
            why = "Kanäle nach vorn und Division durch 255. Normalize kommt erst danach.")
        code(2, "Was ist an dieser Reihenfolge falsch?", """
            transforms.Compose([transforms.Normalize(mean, std), transforms.ToTensor()])
        """, "Normalize erwartet einen Tensor; ToTensor muss vorher kommen", "Compose akzeptiert nur eine Transformation",
            "mean und std müssen Tensoren sein", "Die Reihenfolge ist egal",
            why = "Compose führt in der angegebenen Reihenfolge aus. Normalize auf einem PIL-Bild schlägt fehl.")
        mc(2, "Warum mischt man den Trainings-Loader, aber nicht den Test-Loader?",
            "Mischen verhindert, dass Batches systematisch sortiert sind; für die Bewertung ist die Reihenfolge egal und Reproduzierbarkeit erwünscht",
            "Mischen der Testdaten verfälscht die Labels", "Weil der Test-Loader keine Batches kennt", "Weil shuffle nur bei Bildern geht",
            why = "Sortierte Trainingsdaten (erst alle Nullen, dann alle Einsen) führen zu schlechten Gradientenschritten.")
        line(2, "Welche Zeile teilt ein Dataset reproduzierbar in 80 % und 20 %?",
            "random_split(ds, [0.8, 0.2], generator=torch.Generator().manual_seed(0))",
            "train_test_split(ds, 0.8)",
            "ds.split(0.8, 0.2, seed=0)",
            "DataLoader(ds, split=0.8)",
            why = "random_split akzeptiert absolute Größen oder Anteile; der Generator macht den Split reproduzierbar.")
        code(3, "Was liefert ds[3] bei dieser Klasse?", """
            class Sq(Dataset):
                def __init__(self): self.x = torch.arange(5.0)
                def __len__(self): return 5
                def __getitem__(self, i): return self.x[i], self.x[i] ** 2
            ds = Sq()
        """, "(tensor(3.), tensor(9.))", "tensor(9.)", "(3, 9)", "tensor([3., 9.])",
            why = "__getitem__ gibt ein Tupel aus zwei 0D-Tensoren zurück: Eingabe 3 und Ziel 9.")

        on("pt_linreg")
        codeOut(1, "Was gibt forward aus?", """
            w = torch.tensor(2.0); b = torch.tensor(-1.0)
            def forward(x): return w * x + b
            print(forward(torch.tensor([[1.0], [2.0]])))
        """, "tensor([[1.], [3.]])", "tensor([[2.], [4.]])", "tensor([[-1.], [-1.]])", "tensor([1., 3.])",
            why = "2·1 − 1 = 1 und 2·2 − 1 = 3; die Form (2, 1) bleibt erhalten.")
        mc(2, "Wie lautet ∂ℓ/∂b für ℓ = (1/N)Σ(yₙ − (wxₙ + b))²?",
            "−(2/N) Σ (yₙ − ŷₙ)", "−(2/N) Σ xₙ(yₙ − ŷₙ)", "(1/N) Σ yₙ", "2b",
            why = "Die innere Ableitung nach b ist −1. Nach w käme der zusätzliche Faktor xₙ.")
        code(2, "Was fehlt in dieser Trainingsschleife von Hand?", """
            for epoch in range(15):
                loss = torch.mean((w * X + b - Y) ** 2)
                loss.backward()
                w.data = w.data - lr * w.grad.data
                b.data = b.data - lr * b.grad.data
        """, "Das Nullen der Gradienten nach dem Update (w.grad.data.zero_())", "Ein Aufruf von optimizer.step()",
            "Das Transponieren von X", "Nichts, die Schleife ist korrekt",
            why = "Ohne Nullen addieren sich die Gradienten über die Epochen, die Schritte werden immer größer.")
        mc(1, "Wie viele Parameter hat nn.Linear(1, 1)?",
            "2", "1", "3", "4",
            why = "Ein Gewicht und ein Bias.")
        line(2, "Welche Methode muss eine eigene nn.Module-Klasse neben __init__ definieren?",
            "def forward(self, x):", "def call(self, x):", "def predict(self, x):", "def __getitem__(self, x):",
            why = "PyTorch ruft forward über model(x) auf. call ist der Keras-Name.")
        mc(2, "Warum ruft man model(x) statt model.forward(x)?",
            "__call__ führt zusätzlich registrierte Hooks aus und setzt Infrastruktur auf; forward direkt umgeht sie", "forward existiert nicht",
            "model(x) ist schneller kompiliert", "Es gibt keinen Unterschied",
            why = "Hooks werden etwa für Feature-Extraktion, Profiling oder Quantisierung benutzt.")
        codeOut(2, "Was enthält state_dict()?", """
            class LR(nn.Module):
                def __init__(self):
                    super().__init__(); self.linear = nn.Linear(1, 1)
            print(list(LR().state_dict().keys()))
        """, "['linear.weight', 'linear.bias']", "['weight', 'bias']", "['linear']", "['w', 'b']",
            why = "Die Schlüssel spiegeln die Attributnamen wider: Modulname, Punkt, Parametername.")
        mc(3, "Welche Form hat die MSE-Verlustfläche ℓ(w, b) der linearen Regression?",
            "Ein konvexes Paraboloid mit einem einzigen Minimum", "Viele lokale Minima", "Eine Ebene", "Ein Sattel ohne Minimum",
            why = "ℓ ist eine positiv semidefinite quadratische Form in (w, b) – deshalb konvergiert Gradientenabstieg bei passender Lernrate sicher.")

        on("pt_sgd")
        mc(1, "Was ist der Unterschied zwischen SGD und Batch-Gradientenabstieg?",
            "SGD nutzt pro Schritt ein Beispiel, Batch-Abstieg alle Beispiele", "SGD braucht keine Lernrate", "Batch-Abstieg ist zufällig", "Es gibt keinen",
            why = "SGD ist billig und verrauscht, Batch-Abstieg genau und teuer; Mini-Batch liegt dazwischen.")
        mc(2, "Wie verändert sich die Varianz des Mini-Batch-Gradienten mit der Batchgröße B?",
            "Sie sinkt proportional zu 1/B", "Sie steigt mit B", "Sie ist unabhängig von B", "Sie sinkt wie 1/B²",
            why = "Mittelwert von B unabhängigen Stichproben: Var ∝ σ²/B. Die Standardabweichung sinkt nur wie 1/√B.")
        line(1, "Welche Reihenfolge ist in der Trainingsschleife richtig?",
            "optimizer.zero_grad(); loss.backward(); optimizer.step()",
            "optimizer.step(); loss.backward(); optimizer.zero_grad()",
            "loss.backward(); optimizer.zero_grad(); optimizer.step()",
            "optimizer.zero_grad(); optimizer.step(); loss.backward()",
            why = "Erst alte Gradienten löschen, dann neue berechnen, dann anwenden. Wer nach backward nullt, löscht die frischen Gradienten.")
        code(2, "Was macht dieser Optimierer?", """
            optimizer = optim.SGD(model.parameters(), lr=0.01, momentum=0.9)
        """, "Gradientenabstieg mit Lernrate 0,01 und Momentum 0,9", "Er trainiert 9 Epochen", "Er wählt 1 % der Daten", "Er initialisiert die Gewichte",
            why = "Momentum mittelt vergangene Gradienten und glättet die Bahn.")
        mc(2, "Wie wählt man im IBM-Lab die beste Lernrate?",
            "Mehrere Modelle mit verschiedenen Lernraten trainieren und das mit dem kleinsten Validierungsverlust nehmen", "Die mit dem kleinsten Trainingsverlust",
            "Immer die größte, die nicht divergiert", "Die mit dem kleinsten Testverlust",
            why = "Die Testmenge bleibt für die Endbewertung unberührt.")
        codeOut(2, "Wie viele optimizer.step()-Aufrufe gibt es hier?", """
            loader = DataLoader(ds, batch_size=10)   # len(ds) == 95
            for epoch in range(3):
                for x, y in loader:
                    ...; optimizer.step()
        """, "30", "28", "3", "285",
            why = "⌈95/10⌉ = 10 Schritte pro Epoche × 3 Epochen = 30.")
        line(3, "Wie speichert man nur die Gewichte des besten Modells?",
            "torch.save(model.state_dict(), \"best_model.pt\")",
            "model.save(\"best_model.pt\")",
            "pickle.dump(model.parameters, \"best_model.pt\")",
            "torch.export(model, \"best_model.pt\")",
            why = "Laden: model.load_state_dict(torch.load(\"best_model.pt\")). Achtung: state_dict() liefert Referenzen – für ein Zwischenergebnis copy.deepcopy verwenden.")
        mc(3, "Warum kann das Rauschen von SGD sogar nützlich sein?",
            "Es hilft, scharfe Minima und Sattelpunkte zu verlassen und flachere, besser generalisierende Minima zu finden", "Es macht das Training deterministisch",
            "Es ersetzt die Validierung", "Es verhindert jede Überanpassung",
            why = "Flache Minima sind robuster gegen kleine Verschiebungen zwischen Trainings- und Testverteilung.")

        on("pt_multi")
        codeOut(1, "Welche Form hat model.weight?", """
            model = nn.Linear(in_features=3, out_features=2)
            print(model.weight.shape)
        """, "torch.Size([2, 3])", "torch.Size([3, 2])", "torch.Size([6])", "torch.Size([2])",
            why = "PyTorch speichert (out, in) und rechnet xWᵀ + b.")
        mc(1, "Wie viele Parameter hat nn.Linear(10, 4)?",
            "44", "40", "14", "50",
            why = "10·4 Gewichte + 4 Biases = 44.")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            model = nn.Linear(5, 3)
            X = torch.randn(16, 5)
            print(model(X).shape)
        """, "torch.Size([16, 3])", "torch.Size([3, 16])", "torch.Size([5, 3])", "torch.Size([16, 5])",
            why = "Jede der 16 Zeilen wird von 5 auf 3 Merkmale abgebildet.")
        code(2, "Warum schlägt der Aufruf fehl?", """
            model = nn.Linear(2, 1)
            x = torch.tensor([1.0, 2.0, 3.0])
            model(x)
        """, "Die letzte Dimension des Eingangs muss 2 sein, x hat 3", "nn.Linear akzeptiert keine 1D-Tensoren", "Der Bias fehlt", "x muss int64 sein",
            why = "mat1 and mat2 shapes cannot be multiplied: Die Eingangsbreite muss in_features entsprechen.")
        mc(2, "Was beschreibt ein lineares Modell mit zwei Merkmalen geometrisch?",
            "Eine Ebene über der (x₁, x₂)-Ebene", "Eine Gerade", "Eine Kugel", "Einen Kreis",
            why = "ŷ = w₁x₁ + w₂x₂ + b ist eine Ebene; bei d Merkmalen eine Hyperebene.")
        mc(3, "Warum löst man große lineare Probleme iterativ statt mit der Normalengleichung?",
            "Das Invertieren von XᵀX kostet O(d³) und ist numerisch heikel; ein Gradientenschritt kostet nur O(N·d)", "Weil die Normalengleichung falsch ist",
            "Weil PyTorch keine Matrizen invertieren kann", "Weil iterative Verfahren immer exakt sind",
            why = "Bei Millionen Merkmalen ist eine Inversion unbezahlbar, Mini-Batch-Abstieg skaliert dagegen linear.")
        line(2, "Welche Zeile definiert ein Modell, das aus 4 Merkmalen 2 Zielgrößen vorhersagt?",
            "model = nn.Linear(4, 2)", "model = nn.Linear(2, 4)", "model = nn.Linear(4, 1) * 2", "model = nn.Linear((4, 2))",
            why = "in_features = 4, out_features = 2.")
        mc(2, "Wie geht der Verlust bei mehreren Ausgängen mit nn.MSELoss() (Standard) ein?",
            "Als Mittelwert der quadrierten Fehler über alle Beispiele und alle Ausgänge", "Nur der erste Ausgang zählt", "Als Summe ohne Mittelung", "Als Maximum",
            why = "reduction=\"mean\" mittelt über alle Elemente; reduction=\"sum\" summiert.")

        on("pt_logreg")
        line(1, "Welche Zeile baut eine logistische Regression mit einem Merkmal?",
            "model = nn.Sequential(nn.Linear(1, 1), nn.Sigmoid())",
            "model = nn.Sequential(nn.Sigmoid(), nn.Linear(1, 1))",
            "model = nn.Linear(1, 2)",
            "model = nn.Sequential(nn.Linear(1, 1), nn.ReLU())",
            why = "Erst die lineare Kombination, dann die Sigmoid-Funktion für eine Wahrscheinlichkeit.")
        mc(2, "Warum ist MSE für logistische Regression ungünstig?",
            "Mit Sigmoid wird die Verlustfläche nichtkonvex mit flachen Plateaus; bei sicheren Fehlern verschwindet der Gradient", "MSE ist nicht differenzierbar",
            "MSE kann nur positive Zahlen", "MSE ist langsamer zu berechnen",
            why = "Die Kreuzentropie ist konvex und hat den einfachen Gradienten (p − y)x.")
        mc(2, "Wo liegt die Entscheidungsgrenze der logistischen Regression?",
            "Wo wᵀx + b = 0, also p = 0,5", "Wo p = 1", "Wo x = 0", "Wo der Verlust null ist",
            why = "σ(0) = 0,5. Die Grenze ist linear – logistische Regression ist ein linearer Klassifikator.")
        code(2, "Was ist hier falsch?", """
            model = nn.Sequential(nn.Linear(2, 1), nn.Sigmoid())
            criterion = nn.BCEWithLogitsLoss()
            loss = criterion(model(x), y)
        """, "Die Sigmoid wird doppelt angewandt – BCEWithLogitsLoss erwartet Logits ohne Sigmoid", "BCEWithLogitsLoss gibt es nicht",
            "Die Labels müssen One-Hot sein", "Nichts, das ist korrekt",
            why = "Entweder Sigmoid + BCELoss oder ohne Sigmoid + BCEWithLogitsLoss (numerisch stabiler).")
        mc(1, "Wie groß ist die binäre Kreuzentropie, wenn y = 1 und p = 1?",
            "0", "1", "∞", "0,5",
            why = "−ln 1 = 0. Für p → 0 bei y = 1 strebt der Verlust gegen ∞.")
        codeOut(2, "Was ergibt die Accuracy?", """
            probs = torch.tensor([[0.9], [0.4], [0.6], [0.2]])
            y = torch.tensor([[1.0], [1.0], [0.0], [0.0]])
            print(((probs > 0.5).float() == y).float().mean().item())
        """, "0.5", "0.75", "0.25", "1.0",
            why = "Vorhersagen [1, 0, 1, 0] gegen [1, 1, 0, 0]: zwei von vier richtig.")
        mc(3, "Was passiert im IBM-Lab bei ‚schlechter Initialisierung‘ (w = −15, b = −10) mit MSE?",
            "Das Training bleibt auf einem flachen Plateau hängen, weil die Sigmoid gesättigt ist und die Gradienten fast null sind", "Das Training divergiert sofort",
            "Das Modell ist sofort perfekt", "PyTorch verweigert die Initialisierung",
            why = "Mit Kreuzentropie heben sich σ′ und die Ableitung des Logarithmus auf; der Gradient (p − y)x bleibt groß.")
        mc(2, "Welchen Datentyp müssen die Labels für nn.BCELoss haben?",
            "Gleitkomma (0.0/1.0) mit derselben Form wie die Ausgabe", "int64-Klassenindizes", "Strings", "Bool ohne Form",
            why = "Für nn.CrossEntropyLoss (Mehrklassen) sind es dagegen int64-Indizes ohne Kanaldimension.")
    }
}

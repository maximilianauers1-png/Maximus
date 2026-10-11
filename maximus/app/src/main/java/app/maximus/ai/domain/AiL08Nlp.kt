package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 8 (IBM „Gen AI Foundational Models for NLP & Language Understanding“): one-hot, bag of words
 * and EmbeddingBag, document classification, n-gram language models, word2vec and GloVe, sequence-to-
 * sequence models and evaluation metrics for generated text.
 */
internal object AiL08Nlp {
    val bites: List<Bite> = listOf(
        bite(
            "nlp_onehot", 1, "One-Hot, Bag of Words und EmbeddingBag",
            "Die einfachsten Textdarstellungen: One-Hot-Vektoren, Wortsäcke mit Zählung, TF-IDF – und warum eine Einbettungsschicht dasselbe effizienter tut.",
            goals(
                "One-Hot- und Bag-of-Words-Vektoren bilden",
                "Erkennen, dass eine lineare Schicht auf One-Hot-Vektoren eine Tabellenabfrage ist",
                "nn.Embedding und nn.EmbeddingBag mit Offsets einsetzen"
            ),
            sec("One-Hot", """
                Jedes Wort des Vokabulars V bekommt einen Vektor der Länge |V| mit einer einzigen 1 an seiner Indexposition. Alle Wörter sind gleich weit voneinander entfernt – „König“ ist „Königin“ nicht ähnlicher als „Rübe“. Die Vektoren sind riesig und dünn besetzt.
            """,
                fm("One-Hot", "e_w ∈ {0, 1}^{|V|},  ‖e_w − e_v‖ = √2 für w ≠ v")
            ),
            sec("Bag of Words und TF-IDF", """
                Ein Dokument wird als Summe der One-Hot-Vektoren seiner Wörter dargestellt: ein Zählvektor über das Vokabular. Die Reihenfolge geht verloren („Hund beißt Mann“ = „Mann beißt Hund“), aber für Themenklassifikation reicht das oft erstaunlich gut. TF-IDF gewichtet Wörter herunter, die in fast allen Dokumenten vorkommen („der“, „und“).

                $ from sklearn.feature_extraction.text import CountVectorizer, TfidfVectorizer
                $ docs = ["der Ritter reitet", "der Ritter kämpft und der Ritter siegt"]
                $ bow = CountVectorizer().fit_transform(docs)
                $ tfidf = TfidfVectorizer().fit_transform(docs)
            """,
                fm("Bag of Words", "x_d = Σ_{w ∈ d} e_w"),
                fm("TF-IDF", "tfidf(w, d) = tf(w, d) · ln(N / df(w))")
            ),
            sec("Einbettung statt One-Hot", """
                Multipliziert man einen One-Hot-Vektor mit einer Gewichtsmatrix W (|V| × d), kommt genau die Zeile des Wortes heraus. Eine Einbettungsschicht spart sich die Multiplikation und schlägt die Zeile direkt nach – mit denselben lernbaren Parametern, aber ohne riesige dünne Vektoren. Dichte Einbettungen mit d = 50 … 1000 erlauben, dass ähnliche Wörter ähnliche Vektoren bekommen.

                $ import torch
                $ from torch import nn
                $ emb = nn.Embedding(num_embeddings=10000, embedding_dim=64)
                $ ids = torch.tensor([[12, 7, 942]])
                $ print(emb(ids).shape)          # torch.Size([1, 3, 64])
            """,
                fm("Äquivalenz", "e_wᵀ W = W[w, :]")
            ),
            sec("EmbeddingBag", """
                nn.EmbeddingBag schlägt die Vektoren mehrerer Wörter nach und reduziert sie sofort (Mittelwert, Summe oder Maximum) – ein „Bag of Embeddings“. Statt aufzufüllen übergibt man alle Indizes in einem flachen Tensor und dazu Offsets, wo jedes Dokument beginnt. Das ist schnell und speichersparend und der Kern des IBM-Modells für Nachrichtenklassifikation.

                $ bag = nn.EmbeddingBag(10000, 64, mode="mean")
                $ text = torch.tensor([3, 17, 42, 8, 99])     # Dokument 1: 3, 17, 42 – Dokument 2: 8, 99
                $ offsets = torch.tensor([0, 3])
                $ print(bag(text, offsets).shape)            # torch.Size([2, 64])
            """)
        ),
        bite(
            "nlp_doc_class", 2, "Dokumentklassifikation mit neuronalen Netzen",
            "Nachrichten in vier Rubriken sortieren (AG News): Tokenisierung, Vokabular, EmbeddingBag-Modell, Training und Vorhersage für neue Texte.",
            goals(
                "Die Pipeline Text → Indizes → EmbeddingBag → Linear → Klasse aufbauen",
                "Eine collate-Funktion mit Offsets schreiben",
                "Ein trainiertes Modell auf neue Schlagzeilen anwenden"
            ),
            sec("Die Aufgabe", """
                AG News enthält 120 000 Trainings- und 7600 Testartikel in vier Klassen: World, Sports, Business, Sci/Tech. Ziel ist, aus Titel und Kurztext die Rubrik vorherzusagen – klassische Dokumentklassifikation. Ein überraschend einfaches Modell reicht für rund 90 % Genauigkeit: gemittelte Worteinbettungen und eine lineare Schicht.
            """),
            sec("Das Modell", """
                $ class TextClassifier(nn.Module):
                $     def __init__(self, vocab_size, embed_dim, num_class):
                $         super().__init__()
                $         self.embedding = nn.EmbeddingBag(vocab_size, embed_dim, sparse=False)
                $         self.fc = nn.Linear(embed_dim, num_class)
                $         self.init_weights()
                $     def init_weights(self):
                $         self.embedding.weight.data.uniform_(-0.5, 0.5)
                $         self.fc.weight.data.uniform_(-0.5, 0.5); self.fc.bias.data.zero_()
                $     def forward(self, text, offsets):
                $         return self.fc(self.embedding(text, offsets))
                $ model = TextClassifier(len(vocab), 64, 4)

                Parameter: |V| · 64 für die Einbettung plus 64 · 4 + 4 für die Ausgabe. Bei 95 000 Wörtern sind das gut 6 Millionen – fast alles Einbettung.
            """,
                fm("Modell", "ŷ = softmax(W · (1/|d|) Σ_{w∈d} E[w] + b)")
            ),
            sec("Batches mit Offsets", """
                $ def collate_batch(batch):
                $     labels, texts, offsets = [], [], [0]
                $     for label, text in batch:
                $         labels.append(label - 1)                    # Labels 1..4 → 0..3
                $         ids = torch.tensor(vocab(tokenizer(text)), dtype=torch.int64)
                $         texts.append(ids); offsets.append(ids.size(0))
                $     offsets = torch.tensor(offsets[:-1]).cumsum(dim=0)
                $     return torch.tensor(labels), torch.cat(texts), offsets

                Die Offsets sind die kumulierten Längen: Für Längen 3, 5, 2 sind sie [0, 3, 8].
            """,
                fm("Offsets", "oᵢ = Σ_{j<i} Lⱼ")
            ),
            sec("Training und Vorhersage", """
                $ criterion = nn.CrossEntropyLoss()
                $ optimizer = torch.optim.SGD(model.parameters(), lr=5)
                $ scheduler = torch.optim.lr_scheduler.StepLR(optimizer, 1.0, gamma=0.1)
                $ for label, text, offsets in train_loader:
                $     optimizer.zero_grad()
                $     loss = criterion(model(text, offsets), label)
                $     loss.backward()
                $     torch.nn.utils.clip_grad_norm_(model.parameters(), 0.1)
                $     optimizer.step()
                $ def predict(text):
                $     with torch.no_grad():
                $         ids = torch.tensor(vocab(tokenizer(text)))
                $         return model(ids, torch.tensor([0])).argmax(1).item() + 1

                Die ungewöhnlich hohe Lernrate (5) funktioniert hier wegen der flachen Architektur und des Gradient Clippings. Die Konfusionsmatrix zeigt typischerweise Verwechslungen zwischen Business und Sci/Tech.
            """)
        ),
        bite(
            "nlp_ngram", 2, "N-Gramm-Sprachmodelle",
            "Wahrscheinlichkeiten für das nächste Wort aus Zählungen: Markov-Annahme, Bigramme, Glättung, Perplexität und das neuronale N-Gramm-Modell.",
            goals(
                "Bigramm-Wahrscheinlichkeiten aus Zählungen berechnen",
                "Die Markov-Annahme und Glättung erklären",
                "Ein neuronales N-Gramm-Modell in PyTorch skizzieren"
            ),
            sec("Die Markov-Annahme", """
                Ein Sprachmodell weist Folgen eine Wahrscheinlichkeit zu. Die Kettenregel zerlegt sie exakt in bedingte Wahrscheinlichkeiten. Ein N-Gramm-Modell macht die Markov-Annahme: Das nächste Wort hängt nur von den N − 1 vorherigen ab. Beim Bigramm (N = 2) nur vom direkten Vorgänger.
            """,
                fm("Kettenregel", "P(w₁…w_T) = Πₜ P(wₜ | w₁…w_{t−1})"),
                fm("Markov (N-Gramm)", "P(wₜ | w₁…w_{t−1}) ≈ P(wₜ | w_{t−N+1}…w_{t−1})"),
                fm("Bigramm-Schätzung", "P(wₜ | w_{t−1}) = C(w_{t−1} wₜ) / C(w_{t−1})")
            ),
            sec("Ein Beispiel", """
                Korpus: „ich mag tee“, „ich mag kaffee“, „du magst tee“. C(ich) = 2, C(ich mag) = 2, also P(mag | ich) = 1. C(mag) = 2, C(mag tee) = 1, also P(tee | mag) = 0,5. Damit ist P(ich mag tee | Start mit „ich“) = 1 · 0,5 = 0,5.

                $ from collections import Counter
                $ tokens = "ich mag tee ich mag kaffee du magst tee".split()
                $ uni = Counter(tokens)
                $ bi = Counter(zip(tokens, tokens[1:]))
                $ p = bi[("mag", "tee")] / uni["mag"]       # 0.5
            """),
            sec("Glättung und Grenzen", """
                Ungesehene Paare hätten Wahrscheinlichkeit null – ein einziges unbekanntes Bigramm macht den ganzen Satz unmöglich. Laplace-Glättung addiert 1 zu jeder Zählung; bessere Verfahren (Kneser-Ney) verteilen Masse geschickter. Das Grundproblem bleibt: Mit wachsendem N explodiert die Zahl möglicher Kontexte (|V|^{N−1}), die Daten werden dünn, und Kontext jenseits von N − 1 Wörtern ist unsichtbar.
            """,
                fm("Laplace-Glättung", "P(wₜ | w_{t−1}) = (C(w_{t−1}wₜ) + 1) / (C(w_{t−1}) + |V|)"),
                fm("Perplexität", "PP = exp(−(1/T) Σₜ ln P(wₜ | Kontext))")
            ),
            sec("Das neuronale N-Gramm-Modell", """
                Bengio et al. (2003) ersetzten die Zähltabelle durch ein Netz: Die Einbettungen der N − 1 Kontextwörter werden aneinandergehängt, durch eine versteckte Schicht geschickt und mit Softmax über das Vokabular das nächste Wort vorhergesagt. Ähnliche Wörter teilen sich so statistische Stärke – „Katze schläft“ hilft bei „Hund schläft“.

                $ class NGramLM(nn.Module):
                $     def __init__(self, V, d, context, H):
                $         super().__init__()
                $         self.emb = nn.Embedding(V, d)
                $         self.fc1 = nn.Linear(context * d, H)
                $         self.fc2 = nn.Linear(H, V)
                $     def forward(self, ctx):                      # ctx: (B, context)
                $         x = self.emb(ctx).view(ctx.size(0), -1)
                $         return self.fc2(torch.relu(self.fc1(x)))  # Logits über das Vokabular

                Im IBM-Lab erzeugt so ein Modell Liedtexte Wort für Wort – mit Kontextgröße 2 noch holprig, mit größerer Kontextgröße flüssiger.
            """)
        ),
        bite(
            "nlp_word2vec", 2, "Word2Vec: CBOW, Skip-Gram und GloVe",
            "Bedeutung aus Nachbarschaft: Einbettungen, die Analogien rechnen können, Negativ-Sampling und globale Kookkurrenz mit GloVe.",
            goals(
                "CBOW und Skip-Gram unterscheiden und ihre Trainingspaare bilden",
                "Kosinusähnlichkeit und Vektoranalogien berechnen",
                "GloVe und vortrainierte Einbettungen einsetzen"
            ),
            sec("Die Verteilungshypothese", """
                „You shall know a word by the company it keeps“ (Firth 1957): Wörter, die in ähnlichen Kontexten vorkommen, haben ähnliche Bedeutung. Word2Vec (Mikolov et al. 2013) lernt Einbettungen, indem ein flaches Netz Wörter aus ihrem Kontext vorhersagt – die Einbettungen sind das eigentliche Produkt, die Vorhersage nur der Vorwand.

                • CBOW (Continuous Bag of Words): sagt das mittlere Wort aus dem Mittel der Kontextwörter voraus. Schnell, gut für häufige Wörter.
                • Skip-Gram: sagt aus dem mittleren Wort jedes Kontextwort voraus. Langsamer, besser für seltene Wörter.
            """,
                fm("Skip-Gram-Ziel", "max Σₜ Σ_{−c≤j≤c, j≠0} ln p(w_{t+j} | wₜ)"),
                fm("Softmax", "p(o | c) = exp(u_oᵀ v_c) / Σ_w exp(u_wᵀ v_c)")
            ),
            sec("Trainingspaare und Negativ-Sampling", """
                Satz „der edle ritter reitet heim“, Fensterbreite c = 1: Skip-Gram bildet die Paare (edle → der), (edle → ritter), (ritter → edle), (ritter → reitet) … Die volle Softmax über ein großes Vokabular ist teuer. Negativ-Sampling macht daraus binäre Klassifikation: Das echte Paar soll hohe Wahrscheinlichkeit bekommen, k zufällig gezogene „falsche“ Kontextwörter niedrige.

                $ def skipgram_pairs(tokens, c=1):
                $     return [(tokens[i], tokens[j]) for i in range(len(tokens)) for j in range(max(0, i - c), min(len(tokens), i + c + 1)) if j != i]
            """,
                fm("Negativ-Sampling", "ln σ(u_oᵀv_c) + Σ_{k=1}^{K} ln σ(−u_{n_k}ᵀ v_c)")
            ),
            sec("Rechnen mit Bedeutung", """
                Ähnlichkeit misst man mit dem Kosinus des Winkels zwischen zwei Vektoren. Berühmt sind Analogien: König − Mann + Frau ≈ Königin, Berlin − Deutschland + Frankreich ≈ Paris. Die Differenzvektoren kodieren Beziehungen wie Geschlecht oder Hauptstadt.

                $ from gensim.models import Word2Vec
                $ w2v = Word2Vec(sentences, vector_size=100, window=5, min_count=2, sg=1)   # sg=1: Skip-Gram
                $ print(w2v.wv.most_similar("ritter", topn=3))
                $ print(w2v.wv.most_similar(positive=["koenig", "frau"], negative=["mann"], topn=1))
            """,
                fm("Kosinusähnlichkeit", "cos(a, b) = a·b / (‖a‖ ‖b‖)")
            ),
            sec("GloVe und vortrainierte Einbettungen", """
                GloVe (Pennington et al. 2014) nutzt globale Kookkurrenzzählungen Xᵢⱼ und passt die Vektoren so an, dass ihr Skalarprodukt den Logarithmus der Zählung wiedergibt. Vortrainierte GloVe- oder Word2Vec-Vektoren kann man als Startwerte einer Einbettungsschicht verwenden:

                $ glove = torchtext.vocab.GloVe(name="6B", dim=100)
                $ emb = nn.Embedding.from_pretrained(glove.get_vecs_by_tokens(vocab.get_itos()), freeze=True)

                Grenze aller statischen Einbettungen: Ein Wort hat genau einen Vektor. „Bank“ (Sitzbank) und „Bank“ (Geldinstitut) teilen ihn. Kontextabhängige Einbettungen aus Transformern (BERT) lösen das.
            """,
                fm("GloVe-Ziel", "J = Σᵢⱼ f(Xᵢⱼ)(wᵢᵀw̃ⱼ + bᵢ + b̃ⱼ − ln Xᵢⱼ)²")
            )
        ),
        bite(
            "nlp_seq2seq", 2, "Sequenz-zu-Sequenz: Encoder-Decoder mit RNNs",
            "Übersetzung als Abbildung einer Folge auf eine andere: Encoder, Kontextvektor, Decoder, Teacher Forcing, Greedy- und Beam-Suche.",
            goals(
                "Den Aufbau eines RNN-Encoder-Decoders beschreiben",
                "Teacher Forcing und den Engpass des Kontextvektors erklären",
                "Greedy- und Beam-Search beim Dekodieren vergleichen"
            ),
            sec("Encoder und Decoder", """
                Sutskever et al. (2014) übersetzten mit zwei LSTMs: Der Encoder liest den Quellsatz Token für Token und fasst ihn in seinem letzten verborgenen Zustand zusammen – dem Kontextvektor. Der Decoder startet mit diesem Zustand und erzeugt den Zielsatz Token für Token, jeweils bedingt auf das zuvor erzeugte Token, bis er <eos> ausgibt. Eingabe und Ausgabe dürfen verschieden lang sein.
            """,
                fm("Encoder", "hₜ = f(hₜ₋₁, xₜ),  c = h_T"),
                fm("Decoder", "sₜ = g(sₜ₋₁, yₜ₋₁, c),  p(yₜ | y_{<t}, x) = softmax(W sₜ)")
            ),
            sec("Im Code", """
                $ class Encoder(nn.Module):
                $     def __init__(self, V, d, H):
                $         super().__init__(); self.emb = nn.Embedding(V, d); self.rnn = nn.LSTM(d, H, batch_first=True)
                $     def forward(self, src):
                $         _, (h, c) = self.rnn(self.emb(src)); return h, c
                $ class Decoder(nn.Module):
                $     def __init__(self, V, d, H):
                $         super().__init__(); self.emb = nn.Embedding(V, d); self.rnn = nn.LSTM(d, H, batch_first=True); self.out = nn.Linear(H, V)
                $     def forward(self, tok, h, c):
                $         o, (h, c) = self.rnn(self.emb(tok).unsqueeze(1), (h, c)); return self.out(o.squeeze(1)), h, c
            """),
            sec("Teacher Forcing", """
                Im Training gibt man dem Decoder als nächste Eingabe das richtige Token aus dem Zielsatz statt seiner eigenen Vorhersage. Das stabilisiert und beschleunigt das Lernen erheblich. Bei der Inferenz gibt es kein richtiges Token – das Modell sieht dann eigene Fehler, die es im Training nie sah (Exposure Bias). Abhilfe: Teacher Forcing nur mit einer Wahrscheinlichkeit anwenden (Scheduled Sampling).

                $ use_tf = random.random() < teacher_forcing_ratio
                $ next_input = trg[:, t] if use_tf else logits.argmax(1)
            """),
            sec("Dekodieren und der Engpass", """
                • Greedy: in jedem Schritt das wahrscheinlichste Token – schnell, aber kurzsichtig.
                • Beam Search: verfolgt die k besten Teilsätze parallel und wählt am Ende den mit der höchsten (längennormierten) Gesamtwahrscheinlichkeit.

                Der Kontextvektor fester Größe ist ein Engpass: Lange Sätze passen schlecht hinein. Bahdanau et al. (2015) führten deshalb Attention ein – der Decoder darf in jedem Schritt auf alle Encoder-Zustände schauen. Daraus wurde 2017 der Transformer (Lektion 9).
            """,
                fm("Beam-Score", "score(y) = (1/|y|^α) Σₜ ln p(yₜ | y_{<t}, x)")
            )
        ),
        bite(
            "nlp_metrics", 2, "Generierten Text bewerten: Perplexität, BLEU, ROUGE, METEOR",
            "Wie man misst, ob ein Sprachmodell oder eine Übersetzung gut ist: Likelihood-basiert, N-Gramm-Überlappung und ihre Grenzen.",
            goals(
                "Perplexität aus dem Kreuzentropieverlust berechnen",
                "BLEU mit modifizierter Präzision und Kürzestrafe verstehen",
                "ROUGE, METEOR und menschliche Bewertung einordnen"
            ),
            sec("Perplexität", """
                Die Perplexität ist die Exponentialfunktion der mittleren Kreuzentropie pro Token. Anschaulich: Zwischen wie vielen gleich wahrscheinlichen Wörtern schwankt das Modell im Mittel? Ein Modell, das bei jedem Token gleichmäßig zwischen 10 Kandidaten rät, hat Perplexität 10. Kleiner ist besser; vergleichbar sind nur Modelle mit demselben Tokenizer und Testtext.
            """,
                fm("Perplexität", "PP = exp(−(1/T) Σₜ ln p(wₜ | w_{<t})) = exp(Verlust)"),
                fm("Gleichverteilung", "p = 1/k für alle Tokens ⇒ PP = k")
            ),
            sec("BLEU", """
                BLEU (Papineni et al. 2002) vergleicht die N-Gramme einer maschinellen Übersetzung mit einer oder mehreren Referenzen. Für N = 1 … 4 berechnet man die modifizierte Präzision: Wie viele N-Gramme der Kandidatin kommen in der Referenz vor, wobei jedes Referenz-N-Gramm nur so oft zählt, wie es dort vorkommt (Clipping). Das geometrische Mittel wird mit einer Kürzestrafe multipliziert, damit sehr kurze Ausgaben nicht gewinnen.

                $ from nltk.translate.bleu_score import sentence_bleu
                $ ref = [["der", "ritter", "reitet", "nach", "hause"]]
                $ cand = ["der", "ritter", "reitet", "heim"]
                $ print(sentence_bleu(ref, cand, weights=(0.5, 0.5)))   # Uni- und Bigramme
            """,
                fm("BLEU", "BLEU = BP · exp(Σₙ wₙ ln pₙ)"),
                fm("Kürzestrafe", "BP = 1 für c > r, sonst e^{1 − r/c}")
            ),
            sec("ROUGE und METEOR", """
                • ROUGE (Zusammenfassung): misst vor allem den Recall – wie viele N-Gramme der Referenz in der Zusammenfassung vorkommen. ROUGE-L nutzt die längste gemeinsame Teilfolge.
                • METEOR (Übersetzung): gleicht Wörter auch über Stammformen und Synonyme ab, kombiniert Precision und Recall (Recall stärker gewichtet) und bestraft zerrissene Wortreihenfolge.
                • BERTScore: vergleicht kontextuelle Einbettungen statt exakter Wörter.
            """,
                fm("ROUGE-N", "ROUGE-N = Σ count_match(n-gram) / Σ count_ref(n-gram)")
            ),
            sec("Grenzen", """
                Alle Überlappungsmetriken belohnen Oberfläche, nicht Bedeutung: „Der Ritter kehrt heim“ und „Der Ritter reitet nach Hause“ teilen kaum N-Gramme, sagen aber dasselbe. Bei modernen LLMs ergänzt man daher menschliche Bewertung, paarweise Vergleiche oder ein starkes LLM als Richter (LLM-as-a-Judge) – mit eigenen Verzerrungen, etwa zugunsten längerer Antworten.

                > Keine Metrik ist die Wahrheit. Man wählt die, deren Fehler man kennt, und prüft Stichproben von Hand.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l8") {
        on("nlp_onehot")
        mc(1, "Was ist der Hauptnachteil von One-Hot-Vektoren für Wörter?",
            "Sie sind riesig und dünn, und alle Wörter sind gleich weit voneinander entfernt – keine Ähnlichkeit", "Sie sind nicht eindeutig", "Sie brauchen ein neuronales Netz", "Sie sind zu klein",
            why = "Der Abstand zweier verschiedener One-Hot-Vektoren ist immer √2.")
        mc(1, "Was geht bei Bag of Words verloren?",
            "Die Reihenfolge der Wörter", "Die Häufigkeit der Wörter", "Das Vokabular", "Die Zahl der Dokumente",
            why = "„Hund beißt Mann“ und „Mann beißt Hund“ haben denselben Wortsack.")
        mc(2, "Warum ist nn.Embedding dasselbe wie eine lineare Schicht auf One-Hot-Vektoren?",
            "e_wᵀW wählt genau die Zeile w aus; die Einbettung schlägt diese Zeile direkt nach", "Weil beide Softmax benutzen", "Weil beide keine Parameter haben", "Das stimmt nicht",
            why = "Gleiche Parameter, aber ohne die Multiplikation mit riesigen dünnen Vektoren.")
        codeOut(1, "Welche Form hat die Ausgabe?", """
            emb = nn.Embedding(10000, 64)
            print(emb(torch.tensor([[12, 7, 942]])).shape)
        """, "torch.Size([1, 3, 64])", "torch.Size([3, 64])", "torch.Size([1, 10000])", "torch.Size([64])",
            why = "Batch 1, drei Tokens, je ein 64-dimensionaler Vektor.")
        codeOut(2, "Welche Form hat die Ausgabe von EmbeddingBag?", """
            bag = nn.EmbeddingBag(10000, 64, mode="mean")
            text = torch.tensor([3, 17, 42, 8, 99]); offsets = torch.tensor([0, 3])
            print(bag(text, offsets).shape)
        """, "torch.Size([2, 64])", "torch.Size([5, 64])", "torch.Size([1, 64])", "torch.Size([2, 3, 64])",
            why = "Zwei Dokumente (ab Index 0 und 3), jedes zu einem gemittelten Vektor reduziert.")
        mc(2, "Was bewirkt der IDF-Faktor ln(N/df) in TF-IDF?",
            "Er gewichtet Wörter herunter, die in vielen Dokumenten vorkommen", "Er zählt die Wörter im Dokument", "Er normiert auf Länge 1", "Er entfernt seltene Wörter",
            why = "Ein Wort in allen Dokumenten hat df = N und IDF = ln 1 = 0.")
        mc(2, "Wie viele Parameter hat nn.Embedding(50 000, 300)?",
            "15 Millionen", "50 300", "300", "1,5 Millionen",
            why = "50 000 · 300 = 1,5·10⁷.")
        line(2, "Welcher Vektorisierer erzeugt aus Dokumenten Wortzählungen?",
            "CountVectorizer().fit_transform(docs)", "OneHotEncoder().fit(docs)", "StandardScaler().fit(docs)", "PCA().fit_transform(docs)",
            why = "Ergebnis ist eine dünne Matrix Dokumente × Vokabular.")

        on("nlp_doc_class")
        mc(1, "Wie viele Klassen hat der AG-News-Datensatz?",
            "Vier: World, Sports, Business, Sci/Tech", "Zwei", "Zehn", "Hundert",
            why = "120 000 Trainings- und 7600 Testartikel.")
        mc(2, "Woraus besteht das einfache AG-News-Modell im IBM-Lab?",
            "Einer EmbeddingBag-Schicht (Mittel der Worteinbettungen) und einer linearen Schicht", "Einem tiefen CNN", "Einem LSTM mit Attention", "Einem Entscheidungsbaum",
            why = "Gemittelte Einbettungen plus linearer Klassifikator erreichen rund 90 %.")
        codeOut(2, "Was ergeben die Offsets für Dokumentlängen 3, 5 und 2?", """
            lengths = [3, 5, 2]
            offsets = torch.tensor([0] + lengths[:-1]).cumsum(dim=0)
            print(offsets)
        """, "tensor([0, 3, 8])", "tensor([3, 8, 10])", "tensor([0, 3, 5])", "tensor([0, 5, 7])",
            why = "Jedes Dokument beginnt nach der Summe der Längen davor.")
        code(2, "Warum steht hier label - 1?", """
            labels.append(label - 1)
        """, "Die Rohlabels laufen von 1 bis 4, CrossEntropyLoss erwartet Klassenindizes 0 bis 3", "Um Ausreißer zu entfernen",
            "Damit der Verlust kleiner wird", "Weil das letzte Label ignoriert wird",
            why = "Bei der Vorhersage addiert man entsprechend wieder 1.")
        mc(2, "Wozu dient clip_grad_norm_ im Trainingsschritt?",
            "Es begrenzt die Norm der Gradienten und ermöglicht so stabile Schritte trotz hoher Lernrate", "Es entfernt kleine Gradienten", "Es speichert das Modell", "Es misst die Genauigkeit",
            why = "Mit lr = 5 wären einzelne große Gradienten sonst verheerend.")
        line(2, "Wie sagt man für einen einzelnen neuen Text die Klasse vorher?",
            "model(torch.tensor(vocab(tokenizer(text))), torch.tensor([0])).argmax(1).item()",
            "model(text).argmax()",
            "model.predict(vocab(text))",
            "model(torch.tensor([text]), None).item()",
            why = "Ein Dokument → Offset [0]; argmax über die vier Logits.")
        mc(3, "Welche Klassen verwechselt ein AG-News-Modell typischerweise am häufigsten?",
            "Business und Sci/Tech", "Sports und World", "Sports und Sci/Tech", "Keine – es macht keine Fehler",
            why = "Artikel über Tech-Firmen, Börsengänge und Produkte liegen thematisch zwischen beiden.")
        mc(2, "Warum dominiert die Einbettung die Parameterzahl dieses Modells?",
            "|V| · d ist bei großem Vokabular viel größer als d · K + K für die Ausgabeschicht", "Weil die Ausgabeschicht keine Parameter hat", "Weil EmbeddingBag zweimal zählt", "Das stimmt nicht",
            why = "95 000 · 64 ≈ 6 Mio. gegen 64 · 4 + 4 = 260.")

        on("nlp_ngram")
        mc(1, "Was besagt die Markov-Annahme eines Bigramm-Modells?",
            "Das nächste Wort hängt nur vom direkt vorhergehenden Wort ab", "Alle Wörter sind unabhängig", "Das nächste Wort hängt vom ganzen Text ab", "Wörter sind gleichverteilt",
            why = "P(wₜ | w₁…wₜ₋₁) ≈ P(wₜ | wₜ₋₁).")
        mc(2, "Korpus „ich mag tee ich mag kaffee“. Wie groß ist P(tee | mag)?",
            "0,5", "1", "0,25", "0",
            why = "C(mag) = 2, C(mag tee) = 1 ⇒ 1/2.")
        codeOut(2, "Was gibt der Code aus?", """
            from collections import Counter
            t = "a b a b a c".split()
            bi = Counter(zip(t, t[1:])); uni = Counter(t)
            print(bi[("a", "b")] / uni["a"])
        """, "0.6666666666666666", "0.5", "1.0", "0.3333333333333333",
            why = "C(a) = 3, C(a b) = 2 ⇒ 2/3. (Streng genommen zählt man nur a als Vorgänger: das letzte Wort hat keinen Nachfolger.)")
        mc(2, "Warum braucht man Glättung?",
            "Ungesehene N-Gramme hätten sonst Wahrscheinlichkeit null und machten ganze Sätze unmöglich", "Um das Training zu beschleunigen", "Um seltene Wörter zu löschen", "Weil Zählungen negativ werden",
            why = "Laplace: +1 zu jeder Zählung; Kneser-Ney ist in der Praxis besser.")
        mc(3, "Was ist das Kernproblem großer N in N-Gramm-Modellen?",
            "Die Zahl möglicher Kontexte wächst wie |V|^{N−1}; fast alle werden nie beobachtet (Datendünne)", "Die Modelle werden zu klein", "N muss gerade sein", "Es gibt keine Probleme",
            why = "Neuronale Modelle teilen statistische Stärke über ähnliche Wörter und lösen das teilweise.")
        mc(2, "Was verbessert das neuronale N-Gramm-Modell gegenüber Zählungen?",
            "Ähnliche Wörter haben ähnliche Einbettungen und teilen statistische Stärke", "Es braucht keine Daten", "Es sieht beliebig langen Kontext", "Es ist immer kleiner",
            why = "„Katze schläft“ hilft, „Hund schläft“ vorherzusagen.")
        codeOut(2, "Welche Eingangsbreite hat fc1 bei Kontextgröße 2 und Einbettungsdimension 10?", """
            self.emb = nn.Embedding(V, 10)
            self.fc1 = nn.Linear(context * 10, 128)   # context = 2
        """, "20", "10", "128", "2",
            why = "Die beiden Kontexteinbettungen werden aneinandergehängt: 2 · 10.")
        mc(1, "Welche Perplexität hat ein Modell, das jedes Mal gleichmäßig zwischen 8 Wörtern rät?",
            "8", "1/8", "ln 8", "64",
            why = "PP = exp(−ln(1/8)) = 8.")

        on("nlp_word2vec")
        mc(1, "Was sagt Skip-Gram voraus?",
            "Die Kontextwörter aus dem mittleren Wort", "Das mittlere Wort aus den Kontextwörtern", "Die Satzlänge", "Die Wortart",
            why = "CBOW ist umgekehrt: Kontext → Mitte.")
        codeOut(2, "Welche Paare liefert skipgram_pairs für [\"a\", \"b\", \"c\"] mit c = 1?", """
            print(skipgram_pairs(["a", "b", "c"], c=1))
        """, "[('a', 'b'), ('b', 'a'), ('b', 'c'), ('c', 'b')]", "[('a', 'c')]", "[('b', 'a'), ('b', 'c')]", "[('a', 'b'), ('b', 'c')]",
            why = "Jedes Wort mit jedem Nachbarn im Fenster ±1.")
        mc(2, "Was ersetzt Negativ-Sampling?",
            "Die teure Softmax über das ganze Vokabular durch binäre Entscheidungen für das echte Paar und k zufällige falsche", "Die Einbettungen", "Das Fenster", "Die Tokenisierung",
            why = "Aufwand O(k) statt O(|V|) pro Paar.")
        mc(1, "Welche Analogie lässt sich mit Word2Vec-Vektoren typischerweise rechnen?",
            "König − Mann + Frau ≈ Königin", "König + Mann = Königin", "König · Frau = Mann", "Keine",
            why = "Differenzvektoren kodieren Beziehungen wie Geschlecht.")
        mc(2, "Wie groß ist die Kosinusähnlichkeit von (1, 0) und (1, 1)?",
            "≈ 0,707", "1", "0", "0,5",
            why = "1/(1 · √2) = 1/√2.")
        line(2, "Welche gensim-Zeile trainiert ein Skip-Gram-Modell?",
            "Word2Vec(sentences, vector_size=100, window=5, sg=1)", "Word2Vec(sentences, sg=0)", "Word2Vec.skipgram(sentences)", "Word2Vec(sentences, mode=\"cbow\")",
            why = "sg=1 ist Skip-Gram, sg=0 (Standard) CBOW.")
        mc(2, "Was ist die Grundidee von GloVe?",
            "Skalarprodukte der Wortvektoren sollen den Logarithmus globaler Kookkurrenzzählungen wiedergeben", "Wörter zufällig einbetten", "Nur Buchstaben zählen", "Bilder und Wörter gemeinsam einbetten",
            why = "wᵢᵀw̃ⱼ + bᵢ + b̃ⱼ ≈ ln Xᵢⱼ, gewichtet mit f(Xᵢⱼ).")
        mc(3, "Was können statische Einbettungen wie Word2Vec grundsätzlich nicht?",
            "Mehrdeutige Wörter je nach Kontext unterschiedlich darstellen", "Ähnlichkeiten messen", "Analogien rechnen", "Vortrainiert werden",
            why = "„Bank“ hat genau einen Vektor. Kontextuelle Modelle wie BERT liefern je Vorkommen einen eigenen.")

        on("nlp_seq2seq")
        mc(1, "Was ist der Kontextvektor in einem RNN-Encoder-Decoder?",
            "Der letzte verborgene Zustand des Encoders, der den Quellsatz zusammenfasst", "Das erste Wort des Zielsatzes", "Die Attention-Gewichte", "Das Vokabular",
            why = "Der Decoder startet mit diesem Zustand.")
        mc(2, "Was ist Teacher Forcing?",
            "Im Training bekommt der Decoder das richtige vorherige Zieltoken statt seiner eigenen Vorhersage", "Ein Lehrer korrigiert die Labels", "Das Modell wird gezwungen, schneller zu lernen", "Eine Form von Dropout",
            why = "Stabilisiert das Training, führt aber zum Exposure Bias bei der Inferenz.")
        mc(2, "Was ist der Exposure Bias?",
            "Bei der Inferenz sieht das Modell eigene Fehler als Eingabe, die es im Training (mit Teacher Forcing) nie sah", "Eine Verzerrung der Trainingsdaten", "Zu lange Belichtung von Bildern", "Ein Problem der Tokenisierung",
            why = "Scheduled Sampling mischt Teacher Forcing und eigene Vorhersagen.")
        code(2, "Was entscheidet diese Zeile?", """
            next_input = trg[:, t] if random.random() < teacher_forcing_ratio else logits.argmax(1)
        """, "Ob der Decoder als nächste Eingabe das wahre Zieltoken oder seine eigene Vorhersage bekommt", "Ob das Training endet", "Welche Lernrate gilt", "Ob der Encoder neu startet",
            why = "Teacher Forcing mit einer Wahrscheinlichkeit – Scheduled Sampling.")
        mc(2, "Warum ist der Kontextvektor fester Größe ein Engpass?",
            "Ein langer Satz muss in einen einzigen Vektor gepresst werden; Information geht verloren", "Weil Vektoren nicht differenzierbar sind", "Weil er zu viele Parameter hat", "Er ist kein Engpass",
            why = "Attention erlaubt dem Decoder, direkt auf alle Encoder-Zustände zu schauen.")
        mc(2, "Was macht Beam Search mit Beam-Breite k?",
            "Es verfolgt die k besten Teilsätze parallel und wählt am Ende den besten Gesamtsatz", "Es wählt in jedem Schritt das beste Token", "Es sampelt zufällig", "Es übersetzt k Sätze gleichzeitig",
            why = "Greedy ist der Spezialfall k = 1.")
        codeOut(2, "Welche Form haben h und c des Encoders?", """
            rnn = nn.LSTM(input_size=32, hidden_size=64, batch_first=True)
            _, (h, c) = rnn(torch.randn(8, 15, 32))
            print(h.shape)
        """, "torch.Size([1, 8, 64])", "torch.Size([8, 15, 64])", "torch.Size([8, 64])", "torch.Size([15, 64])",
            why = "(Schichten × Richtungen, Batch, H) – auch mit batch_first bleiben h und c in dieser Form.")
        mc(3, "Warum normiert man den Beam-Score durch die Länge?",
            "Längere Sätze haben sonst automatisch kleinere Log-Wahrscheinlichkeiten und würden benachteiligt", "Um die Rechenzeit zu senken", "Weil Logarithmen negativ sind", "Das macht man nicht",
            why = "Summe von Log-Wahrscheinlichkeiten < 0 fällt mit jedem Token; ohne Normierung gewinnen zu kurze Übersetzungen.")

        on("nlp_metrics")
        mc(1, "Wie hängt die Perplexität mit dem Kreuzentropieverlust pro Token zusammen?",
            "PP = exp(Verlust)", "PP = Verlust²", "PP = 1/Verlust", "PP = ln(Verlust)",
            why = "Ein Verlust von 2,3 Nats entspricht PP ≈ 10.")
        mc(2, "Was ist die modifizierte Präzision in BLEU?",
            "Der Anteil der Kandidaten-N-Gramme in der Referenz, wobei jedes Referenz-N-Gramm nur so oft zählt, wie es dort vorkommt", "Der Recall über alle Wörter",
            "Die Zahl der Satzzeichen", "Die Länge der Übersetzung",
            why = "Clipping verhindert, dass „der der der der“ eine perfekte Unigramm-Präzision bekommt.")
        mc(2, "Wozu dient die Kürzestrafe (Brevity Penalty) in BLEU?",
            "Damit sehr kurze Übersetzungen mit hoher Präzision nicht belohnt werden", "Um lange Sätze zu bestrafen", "Um Satzzeichen zu ignorieren", "Um Synonyme zu erkennen",
            why = "BP = e^{1 − r/c} für c ≤ r.")
        mc(2, "Welche Metrik ist für Zusammenfassungen üblich und betont den Recall?",
            "ROUGE", "BLEU", "Perplexität", "Accuracy",
            why = "ROUGE-N und ROUGE-L messen, wie viel der Referenz abgedeckt wird.")
        mc(2, "Was berücksichtigt METEOR zusätzlich zu exakten Wörtern?",
            "Stammformen und Synonyme sowie die Wortreihenfolge", "Nur Satzzeichen", "Die Satzlänge allein", "Bildinhalte",
            why = "Recall wird stärker gewichtet, zerrissene Reihenfolge bestraft.")
        line(2, "Welche Zeile berechnet einen Satz-BLEU nur mit Uni- und Bigrammen?",
            "sentence_bleu(ref, cand, weights=(0.5, 0.5))", "sentence_bleu(cand, ref)", "bleu(ref, cand, n=2)", "sentence_bleu(ref, cand, weights=(1, 1, 1, 1))",
            why = "Die Gewichte bestimmen, welche N-Gramm-Präzisionen in das geometrische Mittel eingehen; ref ist eine Liste von Referenzen.")
        mc(3, "Warum versagen N-Gramm-Metriken bei Paraphrasen?",
            "Sie messen Oberflächenübereinstimmung; gleichbedeutende Sätze mit anderen Wörtern bekommen niedrige Werte", "Weil Paraphrasen zu lang sind", "Weil sie Satzzeichen zählen", "Sie versagen nicht",
            why = "Deshalb ergänzt man BERTScore, menschliche Urteile oder LLM-as-a-Judge.")
        mc(2, "Ein Verlust pro Token von ln 20 ≈ 3,0 Nats. Wie groß ist die Perplexität?",
            "20", "3", "e³ ≈ 2,7", "400",
            why = "exp(ln 20) = 20.")
    }
}

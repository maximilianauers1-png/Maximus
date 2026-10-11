package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 7 (IBM „Generative AI and LLMs: Architecture and Data Preparation“): generative architectures,
 * LLM families, the libraries, tokenisation (word, character, subword) and data loaders for text.
 */
internal object AiL07GenArch {
    val bites: List<Bite> = listOf(
        bite(
            "gen_arch", 1, "Generative Architekturen im Überblick",
            "RNN, VAE, GAN, Diffusionsmodelle und Transformer: wie jede Familie neue Daten erzeugt, wo sie glänzt und wo sie schwächelt.",
            goals(
                "Diskriminative und generative Modelle unterscheiden",
                "Die fünf großen generativen Architekturen mit ihrem Trainingsprinzip zuordnen",
                "Für Text, Bild und Audio die heute vorherrschende Architektur nennen"
            ),
            sec("Diskriminativ gegen generativ", """
                Ein diskriminatives Modell lernt p(y | x): Gegeben ein Bild, welche Klasse? Ein generatives Modell lernt die Verteilung der Daten selbst, p(x) oder p(x | y), und kann daraus neue Beispiele ziehen: neue Sätze, Bilder, Moleküle, Musik. Generative KI ist im Kern Dichteschätzung in sehr hohen Dimensionen plus effizientes Sampling.
            """,
                fm("Diskriminativ", "lernt p(y | x)"),
                fm("Generativ", "lernt p(x) bzw. p(x | Bedingung), Sampling x ~ p_θ")
            ),
            sec("Die fünf Familien", """
                • RNN/LSTM-Sprachmodelle: erzeugen Folgen Token für Token aus einem verborgenen Zustand; historisch wichtig, heute von Transformern abgelöst.
                • Variational Autoencoder (VAE): Encoder bildet auf eine Verteilung im latenten Raum ab, Decoder rekonstruiert; Training über die Evidenzuntergrenze (ELBO). Glatte latente Räume, oft etwas verschwommene Bilder.
                • GAN: Generator und Diskriminator spielen ein Nullsummenspiel; scharfe Bilder, aber instabiles Training und Modus-Kollaps.
                • Diffusionsmodelle: lernen, schrittweise Rauschen zu entfernen; Stand der Technik für Bilder, Video und Audio (Stable Diffusion, DALL·E 3).
                • Transformer: autoregressive Vorhersage des nächsten Tokens mit Attention; Stand der Technik für Text und Code (GPT, Llama, Gemma, Claude).
            """,
                fm("ELBO", "ln p(x) ≥ E_q[ln p(x|z)] − KL(q(z|x) ‖ p(z))"),
                fm("GAN-Spiel", "min_G max_D E[ln D(x)] + E[ln(1 − D(G(z)))]"),
                fm("Autoregressiv", "p(x₁…x_T) = Πₜ p(xₜ | x_{<t})")
            ),
            sec("Generative KI für Sprache", """
                Für Text hat sich der Transformer durchgesetzt. Er wird zuerst auf riesigen Textmengen vortrainiert (das nächste Token vorhersagen) und dann für Aufgaben angepasst: Übersetzung, Zusammenfassung, Fragen beantworten, Code schreiben, Chatten. Diese großen vortrainierten Modelle heißen Fundamentmodelle (Foundation Models), weil viele Anwendungen auf ihnen aufbauen.

                • Textgenerierung und Chatbots
                • Maschinelle Übersetzung
                • Zusammenfassung und Extraktion
                • Semantische Suche und RAG
                • Code-Assistenz
            """),
            sec("Was bei allen gleich ist", """
                Jede generative Architektur braucht drei Dinge: eine Darstellung der Daten (Tokens, Pixel, latente Vektoren), ein Trainingsziel, das die Modellverteilung an die Datenverteilung annähert (Likelihood, ELBO, adversariales Spiel, Entrauschen), und ein Sampling-Verfahren (Token für Token, ein Durchlauf des Generators, viele Entrauschungsschritte).

                > Die Frage „Welche Architektur?“ beantwortet heute meist die Modalität: Text → Transformer, Bild → Diffusion (oft mit Transformer-Rückgrat), Struktur im latenten Raum → VAE.
            """)
        ),
        bite(
            "gen_llm_families", 2, "LLM-Familien: GPT, BERT, BART und T5",
            "Encoder-only, Decoder-only und Encoder-Decoder: welche Vortrainingsaufgabe zu welcher Architektur gehört und wofür man sie einsetzt.",
            goals(
                "Encoder-, Decoder- und Encoder-Decoder-Modelle unterscheiden",
                "Die Vortrainingsziele kausales LM, maskiertes LM und Denoising zuordnen",
                "Für eine Aufgabe die passende Modellfamilie wählen"
            ),
            sec("Drei Bauformen", """
                • Decoder-only (GPT, Llama, Gemma, Mistral): kausale Attention – jedes Token sieht nur frühere. Vortraining: nächstes Token vorhersagen. Stark im Generieren; heute die Basis fast aller Chat-Modelle.
                • Encoder-only (BERT, RoBERTa, DistilBERT): bidirektionale Attention – jedes Token sieht alle. Vortraining: maskierte Tokens rekonstruieren (MLM). Stark im Verstehen: Klassifikation, Named Entity Recognition, Embeddings für Suche.
                • Encoder-Decoder (T5, BART, das ursprüngliche Transformer-Modell): Der Encoder liest die Eingabe bidirektional, der Decoder erzeugt die Ausgabe kausal und schaut per Cross-Attention auf den Encoder. Stark bei Übersetzung und Zusammenfassung.
            """,
                fm("Kausales LM", "L = −Σₜ ln p(xₜ | x_{<t})"),
                fm("Maskiertes LM", "L = −Σ_{t∈M} ln p(xₜ | x_{∖M})")
            ),
            sec("BART und T5", """
                BART (Facebook) ist ein Denoising-Autoencoder: Der Text wird verdorben (Spannen maskiert, Sätze vertauscht, Tokens gelöscht), der Decoder muss das Original rekonstruieren. T5 (Google, „Text-to-Text Transfer Transformer“) formuliert jede Aufgabe als Text zu Text: „translate English to German: …“, „summarize: …“, „cola sentence: …“. Vortraining: zufällige Spannen durch Platzhalter ersetzen und diese rekonstruieren (Span Corruption).

                $ from transformers import pipeline
                $ summarizer = pipeline("summarization", model="facebook/bart-large-cnn")
                $ t5 = pipeline("text2text-generation", model="t5-small")
                $ print(t5("translate English to German: The knight rides at dawn."))
            """),
            sec("Welches Modell wofür?", """
                • Sentiment eines Satzes klassifizieren: BERT-artiger Encoder plus Klassifikationskopf.
                • Dokumente für die Suche einbetten: Encoder (z. B. Sentence-BERT).
                • Freies Schreiben, Chat, Code: Decoder-only.
                • Übersetzung, Zusammenfassung mit fester Ausgabestruktur: Encoder-Decoder – oder ein großer Decoder mit passendem Prompt.

                > In der Praxis haben große Decoder-Modelle viele Aufgaben übernommen, weil sie mit Prompts flexibel sind. Encoder bleiben unschlagbar effizient für Embeddings und Klassifikation.
            """)
        ),
        bite(
            "gen_libs", 1, "Die Werkzeuge: Hugging Face, PyTorch und LangChain",
            "Was welche Bibliothek tut: transformers, datasets und tokenizers von Hugging Face, PyTorch als Rechenkern, LangChain für Anwendungen.",
            goals(
                "Die Rollen von PyTorch, Hugging Face und LangChain unterscheiden",
                "Mit pipeline, AutoTokenizer und AutoModel ein Modell laden",
                "Datensätze aus dem Hugging Face Hub laden"
            ),
            sec("Wer macht was?", """
                • PyTorch: Tensoren, Autograd, Schichten, Trainingsschleifen – der Rechenkern.
                • Hugging Face transformers: tausende vortrainierte Modelle mit einheitlicher API (AutoModel, AutoTokenizer), Trainer für Fine-Tuning.
                • Hugging Face datasets: Datensätze laden, streamen, vorverarbeiten (map, filter).
                • Hugging Face tokenizers: schnelle Tokenizer (Rust) für BPE, WordPiece, Unigram.
                • LangChain: Bausteine für LLM-Anwendungen – Prompt-Vorlagen, Ketten, Retriever, Agenten, Werkzeuge.
            """),
            sec("Die schnellste Inferenz: pipeline", """
                $ from transformers import pipeline
                $ clf = pipeline("sentiment-analysis")
                $ print(clf("Der Kurs ist großartig!"))           # [{'label': 'POSITIVE', 'score': 0.99…}]
                $ gen = pipeline("text-generation", model="gpt2")
                $ print(gen("Es war einmal ein Ritter", max_new_tokens=20))

                pipeline kapselt Tokenisierung, Modellaufruf und Nachbearbeitung. Für mehr Kontrolle lädt man die Teile einzeln.
            """),
            sec("AutoTokenizer und AutoModel", """
                $ from transformers import AutoTokenizer, AutoModelForSequenceClassification
                $ name = "distilbert-base-uncased-finetuned-sst-2-english"
                $ tok = AutoTokenizer.from_pretrained(name)
                $ model = AutoModelForSequenceClassification.from_pretrained(name)
                $ batch = tok(["I love this course", "This is boring"], padding=True, truncation=True, return_tensors="pt")
                $ logits = model(**batch).logits
                $ print(logits.softmax(-1))

                Die Auto-Klassen lesen die Konfiguration des Modells und wählen die passende Architektur. Der Tokenizer gibt input_ids und attention_mask zurück.
            """),
            sec("Datensätze", """
                $ from datasets import load_dataset
                $ imdb = load_dataset("imdb")
                $ print(imdb["train"][0]["text"][:80], imdb["train"][0]["label"])
                $ tokenized = imdb.map(lambda b: tok(b["text"], truncation=True), batched=True)

                > Kurz: PyTorch rechnet, Hugging Face liefert Modelle und Daten, LangChain baut daraus Anwendungen.
            """)
        ),
        bite(
            "tok_basics", 1, "Tokenisierung: Wort, Zeichen, Teilwort",
            "Wie Text zu Zahlen wird: Tokens, Vokabular, Indizes, Sonderzeichen – und warum weder Wörter noch Zeichen allein taugen.",
            goals(
                "Wort-, Zeichen- und Teilwort-Tokenisierung vergleichen",
                "Ein Vokabular mit Indizes und Sondertokens aufbauen",
                "Mit NLTK, spaCy und Hugging Face tokenisieren"
            ),
            sec("Was ist ein Token?", """
                Modelle verarbeiten Zahlen, keine Buchstaben. Tokenisierung zerlegt Text in Einheiten (Tokens) und ordnet jeder Einheit eine Zahl aus dem Vokabular zu. Die Wahl der Einheit ist ein Kompromiss zwischen Vokabulargröße und Folgenlänge.

                • Wortbasiert: „Der Ritter reitet.“ → [Der, Ritter, reitet, .] – kurze Folgen, aber riesige Vokabulare und unbekannte Wörter (OOV, out of vocabulary) werden zu [UNK]. „reitet“ und „ritt“ haben nichts gemeinsam.
                • Zeichenbasiert: [D, e, r, ␣, R, …] – winziges Vokabular, nie unbekannt, aber sehr lange Folgen und wenig Bedeutung pro Token.
                • Teilwortbasiert: [Der, Ritter, reit, ##et, .] – häufige Wörter bleiben ganz, seltene werden in Bausteine zerlegt. Der heutige Standard.
            """),
            sec("Tokenisieren in Python", """
                $ import nltk
                $ nltk.download("punkt")
                $ from nltk.tokenize import word_tokenize
                $ print(word_tokenize("Don't stop the knight!"))   # ['Do', "n't", 'stop', 'the', 'knight', '!']
                $ import spacy
                $ nlp = spacy.load("en_core_web_sm")
                $ print([t.text for t in nlp("Don't stop the knight!")])
                $ from transformers import AutoTokenizer
                $ tok = AutoTokenizer.from_pretrained("bert-base-uncased")
                $ print(tok.tokenize("tokenization is unbelievable"))  # ['token', '##ization', 'is', 'unbelievable']
            """),
            sec("Vokabular und Indizes", """
                Das Vokabular ordnet jedem Token eine Ganzzahl zu. Sondertokens haben feste Plätze: [PAD] zum Auffüllen, [UNK] für Unbekanntes, [CLS]/[SEP] bei BERT, <bos>/<eos> für Anfang und Ende einer Folge. In PyTorch-Projekten baut man das Vokabular oft mit torchtext:

                $ from torchtext.data.utils import get_tokenizer
                $ from torchtext.vocab import build_vocab_from_iterator
                $ tokenizer = get_tokenizer("basic_english")
                $ vocab = build_vocab_from_iterator((tokenizer(t) for t in texts), specials=["<unk>", "<pad>"])
                $ vocab.set_default_index(vocab["<unk>"])
                $ print(vocab(tokenizer("The knight rides")))   # z. B. [5, 812, 1290]
            """,
                fm("Kodierung", "Text → Tokens (t₁, …, t_n) → Indizes (i₁, …, i_n) ∈ {0, …, |V| − 1}ⁿ")
            ),
            sec("Warum das wichtig ist", """
                • Kosten und Kontextfenster von LLMs werden in Tokens gemessen; auf Englisch ist ein Token im Mittel etwa 4 Zeichen, auf Deutsch eher weniger.
                • Zahlen und seltene Wörter werden oft in viele Tokens zerlegt – ein Grund, warum Sprachmodelle schlecht rechnen.
                • Ein Modell funktioniert nur mit genau dem Tokenizer, mit dem es trainiert wurde.

                > Merksatz: Wortbasiert = OOV-Problem, zeichenbasiert = Längenproblem, Teilwort = der Kompromiss.
            """)
        ),
        bite(
            "tok_subword", 2, "Teilwort-Verfahren: BPE, WordPiece, Unigram, SentencePiece",
            "Wie die Teilwort-Vokabulare entstehen: häufigste Paare verschmelzen, Likelihood maximieren oder von einem großen Vokabular abschneiden.",
            goals(
                "Byte-Pair Encoding von Hand auf einem kleinen Korpus durchführen",
                "WordPiece, Unigram und SentencePiece unterscheiden",
                "Zu einem Modell den passenden Tokenizer nennen"
            ),
            sec("Byte-Pair Encoding (BPE)", """
                BPE beginnt mit einzelnen Zeichen und verschmilzt wiederholt das häufigste benachbarte Paar zu einem neuen Token, bis die gewünschte Vokabulargröße erreicht ist. Beispielkorpus mit Häufigkeiten: low ×5, lower ×2, newest ×6, widest ×3. Das häufigste Paar ist „e s“ (9-mal in newest und widest) → neues Token „es“; danach „es t“ → „est“ (9-mal), und so weiter. GPT-2 und GPT-4 nutzen byte-level BPE: Die Grundeinheiten sind Bytes, deshalb gibt es nie ein unbekanntes Zeichen.
            """,
                fm("BPE-Schritt", "(a, b)* = argmax_{(a,b)} count(a b);  V ← V ∪ {ab}")
            ),
            sec("WordPiece und Unigram", """
                • WordPiece (BERT): verschmilzt wie BPE, wählt aber das Paar, das die Likelihood des Korpus am stärksten erhöht – also count(ab)/(count(a)·count(b)). Fortsetzungsstücke tragen das Präfix ##.
                • Unigram (in SentencePiece, T5, ALBERT): startet mit einem großen Vokabular und entfernt schrittweise die Tokens, deren Verlust die Likelihood am wenigsten senkt. Ein Wort kann mehrere Zerlegungen haben; gewählt wird die wahrscheinlichste (Viterbi).
            """,
                fm("WordPiece-Score", "score(a, b) = count(ab) / (count(a) · count(b))"),
                fm("Unigram-Likelihood", "P(x) = Π p(tᵢ), Zerlegung x* = argmax_{Zerlegungen} Π p(tᵢ)")
            ),
            sec("SentencePiece", """
                SentencePiece behandelt Text als rohen Zeichenstrom inklusive Leerzeichen (dargestellt als ▁) und braucht keine sprachspezifische Vorab-Zerlegung in Wörter – ideal für Sprachen ohne Leerzeichen (Japanisch, Chinesisch) und für mehrsprachige Modelle. Es implementiert BPE und Unigram. Llama, T5 und Gemma nutzen SentencePiece-Tokenizer.

                $ from transformers import AutoTokenizer
                $ t5 = AutoTokenizer.from_pretrained("t5-small")
                $ print(t5.tokenize("Der Ritter reitet"))     # z. B. ['▁Der', '▁R', 'itter', '▁reite', 't']
                $ gpt = AutoTokenizer.from_pretrained("gpt2")
                $ print(gpt.tokenize(" knight"))             # ['Ġknight']  (Ġ markiert das Leerzeichen)
            """),
            sec("Vokabulargröße", """
                Typische Größen: BERT 30 522, GPT-2 50 257, Llama 2 32 000, Llama 3 128 256, Gemma 256 000. Größere Vokabulare verkürzen die Folgen (billigere Attention, mehr Text im Kontextfenster), vergrößern aber die Einbettungsmatrix und die Ausgabeschicht: Bei d = 4096 und |V| = 128 000 sind das über 500 Millionen Parameter allein in der Einbettung.
            """,
                fm("Einbettungsparameter", "P = |V| · d")
            )
        ),
        bite(
            "nlp_dataloader", 2, "Datenlader für NLP: Vokabular, Padding und collate_fn",
            "Texte unterschiedlicher Länge zu Batches machen: Indizes, Padding, Attention-Masken, eigene collate-Funktionen und Sortieren nach Länge.",
            goals(
                "Ein Text-Dataset in PyTorch schreiben",
                "Mit einer collate_fn Folgen variabler Länge auffüllen",
                "Attention-Masken und padding_idx korrekt einsetzen"
            ),
            sec("Das Problem", """
                Bilder lassen sich auf eine feste Größe skalieren, Sätze nicht – sie sind unterschiedlich lang. Tensoren müssen aber rechteckig sein. Lösung: Innerhalb eines Batches füllt man kürzere Folgen mit dem Padding-Index auf (Padding) und merkt sich in einer Maske, welche Positionen echt sind.
            """,
                fm("Batch-Form", "(B, L_max) mit L_max = max Länge im Batch"),
                fm("Maske", "mᵢₜ = 1 für echte Tokens, 0 für Padding")
            ),
            sec("Dataset und collate_fn", """
                $ import torch
                $ from torch.utils.data import Dataset, DataLoader
                $ from torch.nn.utils.rnn import pad_sequence
                $ class TextSet(Dataset):
                $     def __init__(self, texts, labels): self.texts, self.labels = texts, labels
                $     def __len__(self): return len(self.texts)
                $     def __getitem__(self, i): return self.texts[i], self.labels[i]
                $ PAD = vocab["<pad>"]
                $ def collate_fn(batch):
                $     ids = [torch.tensor(vocab(tokenizer(t)), dtype=torch.long) for t, _ in batch]
                $     labels = torch.tensor([y for _, y in batch])
                $     x = pad_sequence(ids, batch_first=True, padding_value=PAD)
                $     return x, labels
                $ loader = DataLoader(TextSet(texts, labels), batch_size=32, shuffle=True, collate_fn=collate_fn)

                Die collate_fn bekommt eine Liste von Beispielen und baut daraus den Batch. pad_sequence füllt auf die längste Folge im Batch auf.
            """),
            sec("Padding im Modell beachten", """
                • nn.Embedding(V, d, padding_idx=PAD) hält den Vektor des Padding-Tokens bei null und trainiert ihn nicht.
                • Bei Attention verhindert eine Maske, dass echte Tokens auf Padding achten.
                • Beim Mitteln über die Folge nur echte Tokens mitteln: Σ mₜhₜ / Σ mₜ.
                • Bei Hugging Face erledigt der Tokenizer das: padding=True erzeugt attention_mask.

                $ batch = tok(["kurz", "ein deutlich längerer Satz"], padding=True, return_tensors="pt")
                $ print(batch["input_ids"].shape, batch["attention_mask"])
            """,
                fm("Maskiertes Mittel", "h̄ = Σₜ mₜ hₜ / Σₜ mₜ")
            ),
            sec("Effizienz", """
                Füllt man zufällig gemischte Sätze auf, verschwendet man Rechenzeit auf Padding. Bucketing (Sätze ähnlicher Länge in denselben Batch) oder dynamisches Padding pro Batch spart viel. Für Seq2Seq-Aufgaben (Übersetzung) braucht die collate_fn zwei aufgefüllte Folgen – Quelle und Ziel – und für das Ziel zusätzlich <bos> und <eos>.

                > Faustregel: Padding ist unvermeidlich, aber es darf nie das Ergebnis beeinflussen – dafür sind Masken und padding_idx da.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l7") {
        on("gen_arch")
        mc(1, "Was lernt ein generatives Modell im Unterschied zu einem diskriminativen?",
            "Die Verteilung der Daten p(x) (oder p(x | Bedingung)), aus der es neue Beispiele ziehen kann", "Nur die Grenze zwischen Klassen p(y | x)",
            "Nur die Labels", "Nur die Mittelwerte",
            why = "Diskriminativ: p(y|x). Generativ: p(x) – damit kann man sampeln.")
        mc(1, "Welche Architektur dominiert heute die Bilderzeugung?",
            "Diffusionsmodelle", "RNNs", "Entscheidungsbäume", "k-Means",
            why = "Stable Diffusion, DALL·E 3, Imagen: schrittweises Entrauschen, oft mit Transformer- oder U-Net-Rückgrat.")
        mc(2, "Welches Problem ist typisch für GANs?",
            "Instabiles Training und Modus-Kollaps (der Generator erzeugt nur wenige Varianten)", "Zu verschwommene Bilder", "Sie können nicht sampeln", "Sie brauchen keine Daten",
            why = "Das Minimax-Spiel hat oft kein stabiles Gleichgewicht; VAEs sind stabil, aber verschwommener.")
        mc(2, "Womit wird ein VAE trainiert?",
            "Mit der Evidenzuntergrenze (ELBO): Rekonstruktion plus KL-Abstand zum Prior", "Mit einem Diskriminator", "Mit Q-Learning", "Mit reinem MSE ohne latenten Raum",
            why = "ln p(x) ≥ E_q[ln p(x|z)] − KL(q(z|x) ‖ p(z)).")
        mc(1, "Wie erzeugt ein autoregressives Sprachmodell Text?",
            "Token für Token, jedes bedingt auf alle vorherigen", "Alle Tokens gleichzeitig", "Rückwärts vom Satzende", "Durch Entrauschen eines Bildes",
            why = "p(x₁…x_T) = Π p(xₜ | x_{<t}).")
        code(2, "Welche Familie verwendet diese Pipeline?", """
            from diffusers import StableDiffusionPipeline
            pipe = StableDiffusionPipeline.from_pretrained("runwayml/stable-diffusion-v1-5")
            image = pipe("a knight in a library").images[0]
        """, "Ein Diffusionsmodell, das aus Rauschen ein Bild entrauscht, gesteuert vom Text", "Ein GAN", "Ein RNN", "Ein Encoder-only-Transformer",
            why = "Stable Diffusion entrauscht im latenten Raum eines VAE; ein Text-Encoder (CLIP) liefert die Bedingung.")
        mc(2, "Was ist ein Fundamentmodell (Foundation Model)?",
            "Ein großes, breit vortrainiertes Modell, auf dem viele Anwendungen durch Anpassung aufbauen", "Ein Modell ohne Parameter", "Ein Modell nur für Bilder", "Ein Regelwerk",
            why = "GPT, Llama, Gemma, BERT – vortrainiert auf riesigen Daten, angepasst per Prompt oder Fine-Tuning.")
        mc(3, "Welche drei Dinge braucht jede generative Architektur?",
            "Eine Datenrepräsentation, ein Trainingsziel zur Annäherung an die Datenverteilung und ein Sampling-Verfahren", "Eine GPU, Internet und eine Datenbank",
            "Labels, Test- und Validierungsdaten", "Encoder, Decoder und Diskriminator",
            why = "Die Familien unterscheiden sich in genau diesen drei Wahlen.")

        on("gen_llm_families")
        mc(1, "Zu welcher Familie gehört BERT?",
            "Encoder-only, vortrainiert mit maskiertem Sprachmodell", "Decoder-only, kausal", "Encoder-Decoder", "Diffusionsmodelle",
            why = "BERT sieht den Kontext beidseitig und rekonstruiert maskierte Tokens.")
        mc(1, "Zu welcher Familie gehören GPT und Llama?",
            "Decoder-only mit kausaler Attention", "Encoder-only", "Encoder-Decoder", "Faltungsnetze",
            why = "Sie sagen das nächste Token voraus; jedes Token sieht nur frühere.")
        mc(2, "Wie formuliert T5 jede Aufgabe?",
            "Als Text zu Text mit einem Aufgabenpräfix, z. B. „summarize: …“", "Als Klassifikation", "Als Bild", "Als Regression",
            why = "Text-to-Text Transfer Transformer: eine Architektur, ein Verlust für alle Aufgaben.")
        mc(2, "Wie wird BART vortrainiert?",
            "Als Denoising-Autoencoder: verdorbenen Text rekonstruieren", "Nächstes Token ohne Encoder", "Mit Kontrastlernen auf Bildern", "Mit Q-Learning",
            why = "Maskierte Spannen, vertauschte Sätze, gelöschte Tokens – der Decoder stellt das Original her.")
        mc(2, "Für welche Aufgabe ist ein Encoder-only-Modell besonders effizient?",
            "Texte klassifizieren und Embeddings für semantische Suche berechnen", "Lange Geschichten schreiben", "Bilder erzeugen", "Reinforcement Learning",
            why = "Bidirektionaler Kontext und eine Ausgabe pro Token oder pro Satz – keine teure autoregressive Generierung.")
        codeOut(2, "Was macht dieser Aufruf?", """
            t5 = pipeline("text2text-generation", model="t5-small")
            print(t5("translate English to German: The knight rides at dawn."))
        """, "Er übersetzt den Satz ins Deutsche, gesteuert durch das Aufgabenpräfix", "Er fasst den Satz zusammen",
            "Er klassifiziert die Stimmung", "Er gibt die Tokens aus",
            why = "T5 wurde mit solchen Präfixen auf Übersetzung trainiert.")
        mc(3, "Welches Vortrainingsziel gehört zum kausalen Sprachmodell?",
            "L = −Σₜ ln p(xₜ | x_{<t})", "L = −Σ_{t∈M} ln p(xₜ | x_{∖M})", "L = ‖x − x̂‖²", "L = KL(q ‖ p)",
            why = "Das zweite ist das maskierte LM (BERT).")
        mc(2, "Was verbindet im Encoder-Decoder den Decoder mit dem Encoder?",
            "Cross-Attention: Die Queries kommen vom Decoder, Keys und Values vom Encoder", "Eine Residualverbindung", "Ein gemeinsamer Tokenizer", "Pooling",
            why = "So kann jedes erzeugte Wort gezielt auf die passenden Eingabewörter achten.")

        on("gen_libs")
        mc(1, "Welche Bibliothek liefert tausende vortrainierte Transformer mit einheitlicher API?",
            "Hugging Face transformers", "scikit-learn", "NumPy", "matplotlib",
            why = "AutoModel, AutoTokenizer und der Model Hub.")
        mc(1, "Wofür ist LangChain gedacht?",
            "Um aus LLMs Anwendungen zu bauen: Prompt-Vorlagen, Ketten, Retriever, Agenten, Werkzeuge", "Um Modelle von Grund auf zu trainieren",
            "Um Tensoren auf der GPU zu rechnen", "Um Bilder zu augmentieren",
            why = "LangChain orchestriert; das Modell selbst kommt von Hugging Face, OpenAI, IBM watsonx und anderen.")
        codeOut(1, "Was gibt der Code ungefähr aus?", """
            clf = pipeline("sentiment-analysis")
            print(clf("Der Kurs ist großartig!"))
        """, "[{'label': 'POSITIVE', 'score': 0.99…}]", "0.99", "POSITIVE", "[1, 0]",
            why = "pipeline gibt pro Eingabe ein Wörterbuch mit Label und Konfidenz zurück.")
        code(2, "Was enthält batch nach diesem Aufruf?", """
            batch = tok(["I love this", "Boring"], padding=True, truncation=True, return_tensors="pt")
        """, "input_ids und attention_mask als PyTorch-Tensoren gleicher Länge", "Nur die Wörter als Strings", "Die Logits des Modells", "Die Labels",
            why = "padding=True füllt auf die längste Folge auf; die Maske markiert echte Tokens.")
        line(2, "Wie übergibt man den Tokenizer-Output an ein Hugging-Face-Modell?",
            "logits = model(**batch).logits", "logits = model(batch[0])", "logits = model.predict(batch)", "logits = model(batch).softmax",
            why = "**batch entpackt input_ids, attention_mask (und ggf. token_type_ids) als Schlüsselwortargumente.")
        mc(2, "Was macht datasets.map(..., batched=True)?",
            "Es wendet die Funktion auf Stapel von Beispielen an – viel schneller beim Tokenisieren", "Es zeichnet eine Karte", "Es mischt die Daten", "Es lädt die Daten neu",
            why = "Schnelle Tokenizer verarbeiten Listen von Texten parallel.")
        mc(2, "Wozu gibt es AutoModelForSequenceClassification statt AutoModel?",
            "Es lädt das Modell mit passendem Klassifikationskopf für Sequenzen", "Es ist eine schnellere Variante", "Es lädt nur den Tokenizer", "Es trainiert automatisch",
            why = "AutoModel liefert nur die verborgenen Zustände; die Aufgabenklassen ergänzen den passenden Kopf.")
        mc(3, "Warum muss man genau den Tokenizer des Modells verwenden?",
            "Die Einbettungsmatrix ist an dessen Vokabular und Indizes gebunden; ein anderer Tokenizer liefert falsche Indizes", "Aus Lizenzgründen",
            "Weil Tokenizer sonst zu langsam sind", "Das ist nicht nötig",
            why = "Index 2054 bedeutet bei BERT „what“, bei GPT-2 etwas völlig anderes.")

        on("tok_basics")
        mc(1, "Was ist der Hauptnachteil wortbasierter Tokenisierung?",
            "Riesige Vokabulare und unbekannte Wörter (OOV) werden zu [UNK]", "Zu lange Folgen", "Sie funktioniert nur auf Deutsch", "Sie ist nicht deterministisch",
            why = "Zeichenbasiert hat das umgekehrte Problem: kleine Vokabulare, aber sehr lange Folgen.")
        mc(1, "Welche Tokenisierung ist heute Standard bei LLMs?",
            "Teilwort-Tokenisierung (BPE, WordPiece, Unigram)", "Wortbasiert", "Zeichenbasiert", "Satzbasiert",
            why = "Häufige Wörter bleiben ganz, seltene werden in Bausteine zerlegt – kein OOV, moderate Längen.")
        codeOut(2, "Was gibt der BERT-Tokenizer aus?", """
            tok = AutoTokenizer.from_pretrained("bert-base-uncased")
            print(tok.tokenize("tokenization"))
        """, "['token', '##ization']", "['tokenization']", "['t', 'o', 'k', 'e', 'n']", "['Token', 'ization']",
            why = "WordPiece: ## markiert ein Fortsetzungsstück; uncased schreibt alles klein.")
        mc(2, "Wozu dient das Sondertoken [PAD]?",
            "Zum Auffüllen kürzerer Folgen im Batch auf eine gemeinsame Länge", "Für unbekannte Wörter", "Für das Satzende", "Für Zahlen",
            why = "[UNK] steht für Unbekanntes, [SEP]/<eos> markieren Grenzen.")
        line(2, "Welche Zeile sorgt in torchtext dafür, dass unbekannte Wörter auf <unk> abgebildet werden?",
            "vocab.set_default_index(vocab[\"<unk>\"])",
            "vocab.unknown = True",
            "vocab.add(\"<unk>\")",
            "tokenizer.set_unk(\"<unk>\")",
            why = "Ohne Default-Index wirft der Zugriff auf ein unbekanntes Wort einen Fehler.")
        mc(2, "Wie viele Zeichen umfasst ein Token bei englischem Text im Mittel ungefähr?",
            "Etwa 4", "Etwa 1", "Etwa 20", "Genau ein Wort",
            why = "Faustregel: 1 Token ≈ 4 Zeichen ≈ 0,75 Wörter auf Englisch.")
        mc(3, "Warum rechnen Sprachmodelle oft schlecht mit großen Zahlen?",
            "Zahlen werden uneinheitlich in mehrere Tokens zerlegt; Stellenwerte sind für das Modell nicht klar ausgerichtet", "Weil sie keine Zahlen kennen",
            "Weil sie keine Attention haben", "Weil Zahlen [UNK] werden",
            why = "„12345“ kann je nach Tokenizer zu „123“ + „45“ werden. Deshalb nutzen Agenten Werkzeuge wie Taschenrechner – auch Maximus.")
        codeOut(1, "Was liefert NLTK?", """
            from nltk.tokenize import word_tokenize
            print(word_tokenize("Don't stop!"))
        """, "['Do', \"n't\", 'stop', '!']", "['Don't', 'stop!']", "['D', 'o', 'n']", "['Don', 't', 'stop']",
            why = "Der Penn-Treebank-Tokenizer trennt Kontraktionen und Satzzeichen ab.")

        on("tok_subword")
        mc(2, "Was verschmilzt BPE in jedem Schritt?",
            "Das häufigste benachbarte Tokenpaar im Korpus", "Das seltenste Paar", "Zufällige Paare", "Ganze Sätze",
            why = "Beginnend bei Zeichen (oder Bytes) wächst das Vokabular, bis die Zielgröße erreicht ist.")
        mc(2, "Korpus: low×5, lower×2, newest×6, widest×3. Wie oft kommt das Zeichenpaar „e s“ vor, und wie oft „l o“?",
            "„e s“ 9-mal, „l o“ 7-mal", "„e s“ 6-mal, „l o“ 5-mal", "„e s“ 3-mal, „l o“ 2-mal", "„e s“ 15-mal, „l o“ 12-mal",
            why = "„es“ steckt in newest (6) und widest (3), „lo“ in low (5) und lower (2). Damit gehört „es“ (gleichauf mit „st“) zu den ersten Verschmelzungen.")
        mc(2, "Wie wählt WordPiece das nächste Paar?",
            "Nach count(ab)/(count(a)·count(b)) – dem Gewinn an Likelihood", "Nach reiner Häufigkeit", "Alphabetisch", "Zufällig",
            why = "Paare aus seltenen Teilen, die oft zusammen auftreten, werden bevorzugt.")
        mc(2, "Wie arbeitet der Unigram-Tokenizer?",
            "Er startet mit einem großen Vokabular und entfernt Tokens, deren Verlust die Likelihood am wenigsten senkt", "Er verschmilzt Paare wie BPE",
            "Er nutzt nur einzelne Zeichen", "Er nutzt ein festes Wörterbuch",
            why = "Die Zerlegung eines Wortes wählt er per Viterbi als wahrscheinlichste.")
        mc(1, "Welches Zeichen markiert in SentencePiece ein Leerzeichen?",
            "▁ (U+2581)", "##", "Ġ", "_",
            why = "## ist WordPiece (Fortsetzung), Ġ die byte-level-BPE-Darstellung von GPT-2.")
        codeOut(2, "Was gibt der GPT-2-Tokenizer aus?", """
            gpt = AutoTokenizer.from_pretrained("gpt2")
            print(gpt.tokenize(" knight"))
        """, "['Ġknight']", "['knight']", "['▁knight']", "['kn', '##ight']",
            why = "Byte-level BPE stellt das führende Leerzeichen als Ġ dar.")
        mc(3, "Wie viele Parameter hat eine Einbettungsmatrix mit |V| = 128 000 und d = 4096?",
            "Etwa 524 Millionen", "Etwa 5 Millionen", "Etwa 52 Milliarden", "Etwa 131 000",
            why = "128 000 · 4096 ≈ 5,24·10⁸ – bei Llama 3 8B also ein spürbarer Anteil.")
        mc(2, "Warum ist byte-level BPE robust gegen unbekannte Zeichen?",
            "Die Grundeinheiten sind die 256 Bytes; jeder Text lässt sich daraus darstellen", "Weil es ein Wörterbuch aller Sprachen hat", "Weil es [UNK] oft nutzt", "Weil es keine Sonderzeichen gibt",
            why = "Emoji, Sonderzeichen oder Tippfehler zerfallen schlimmstenfalls in Bytes, aber nie in [UNK].")

        on("nlp_dataloader")
        mc(1, "Warum braucht man bei Text Padding?",
            "Sätze sind unterschiedlich lang, Tensoren eines Batches müssen aber rechteckig sein", "Um Text zu verschlüsseln", "Um das Vokabular zu vergrößern", "Um Tokens zu sortieren",
            why = "Kürzere Folgen werden mit dem PAD-Index aufgefüllt.")
        codeOut(2, "Welche Form hat x?", """
            seqs = [torch.tensor([4, 8]), torch.tensor([1, 2, 3, 9]), torch.tensor([7])]
            x = pad_sequence(seqs, batch_first=True, padding_value=0)
            print(x.shape)
        """, "torch.Size([3, 4])", "torch.Size([4, 3])", "torch.Size([3, 2])", "torch.Size([7])",
            why = "Drei Folgen, aufgefüllt auf die längste (4).")
        mc(2, "Was ist die Aufgabe einer collate_fn im DataLoader?",
            "Aus einer Liste einzelner Beispiele einen Batch bauen – z. B. tokenisieren und auffüllen", "Die Daten mischen", "Das Modell aufrufen", "Den Verlust berechnen",
            why = "Standard-Collate stapelt nur gleich große Tensoren; für Text braucht man eine eigene.")
        line(2, "Welche Einbettung hält den Vektor des Padding-Tokens auf null und trainiert ihn nicht?",
            "nn.Embedding(V, d, padding_idx=PAD)", "nn.Embedding(V, d, pad=True)", "nn.Embedding(V + PAD, d)", "nn.EmbeddingBag(V, d, mode=\"pad\")",
            why = "padding_idx schließt den Eintrag vom Gradienten aus.")
        mc(2, "Wozu dient die attention_mask?",
            "Sie verhindert, dass Tokens auf Padding-Positionen achten, und markiert echte Tokens", "Sie maskiert zufällig Wörter für das Training", "Sie zählt die Batches", "Sie ist das Label",
            why = "Ohne Maske würden Padding-Tokens die Darstellungen verfälschen.")
        code(3, "Was berechnet dieser Ausdruck?", """
            mean = (h * mask.unsqueeze(-1)).sum(1) / mask.sum(1, keepdim=True)
        """, "Den Mittelwert der verborgenen Zustände nur über echte Tokens (Padding ausgeschlossen)", "Den Mittelwert über alle Positionen", "Die Summe der Masken", "Die Länge der längsten Folge",
            why = "Maskiertes Mitteln: Σ mₜhₜ / Σ mₜ – typisch für Satz-Embeddings.")
        mc(2, "Was spart Bucketing (Sätze ähnlicher Länge in einen Batch)?",
            "Rechenzeit, weil weniger Padding verarbeitet wird", "Speicher für das Vokabular", "Labels", "Gradienten",
            why = "Bei stark unterschiedlichen Längen kann Padding den Großteil der Rechnung ausmachen.")
        mc(2, "Was braucht eine collate_fn für eine Übersetzungsaufgabe zusätzlich?",
            "Zwei aufgefüllte Folgen (Quelle und Ziel) und <bos>/<eos> für das Ziel", "Nur Labels als Ganzzahlen", "Bilder", "Nichts",
            why = "Der Decoder bekommt das Ziel um eins verschoben: Eingabe <bos> y₁ …, Ziel y₁ … <eos> (Teacher Forcing).")
    }
}

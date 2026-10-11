package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 9 (IBM „Generative AI Language Modeling with Transformers“): positional encoding, attention
 * step by step, multi-head attention and the block, GPT-style decoders, BERT-style encoders and the
 * encoder-decoder transformer for translation.
 */
internal object AiL09Transformers {
    val bites: List<Bite> = listOf(
        bite(
            "tr_posenc", 2, "Positionskodierung",
            "Warum Attention die Reihenfolge nicht kennt, wie Sinus-Kosinus-Kodierungen und gelernte Positionen helfen, und was RoPE anders macht.",
            goals(
                "Begründen, warum ein Transformer Positionsinformation braucht",
                "Die sinusförmige Positionskodierung berechnen und deuten",
                "Gelernte, sinusförmige und rotierende Positionen (RoPE) vergleichen"
            ),
            sec("Attention ist reihenfolgeblind", """
                Self-Attention berechnet gewichtete Mittel über alle Tokens; vertauscht man die Eingabetokens, vertauschen sich die Ausgaben genauso (Permutationsäquivarianz). Ohne Zusatzinformation wären „Hund beißt Mann“ und „Mann beißt Hund“ nicht zu unterscheiden. Deshalb addiert man zu jeder Token-Einbettung einen Positionsvektor gleicher Dimension.
            """,
                fm("Eingabe des Transformers", "xₜ = E[tokenₜ] + PE(t)")
            ),
            sec("Sinus und Kosinus", """
                Das Original (Vaswani et al. 2017) nutzt feste Wellen unterschiedlicher Frequenz: Gerade Dimensionen bekommen einen Sinus, ungerade einen Kosinus; die Wellenlänge wächst geometrisch von 2π bis 10 000 · 2π. Für dich als Physiker: eine Fourier-Basis der Position. Jede Position bekommt einen eindeutigen „Fingerabdruck“, und weil sin(a + b) und cos(a + b) Linearkombinationen von sin a und cos a sind, lässt sich eine feste Verschiebung k als lineare Abbildung ausdrücken – das Modell kann relative Positionen lernen.

                $ import torch, math
                $ def positional_encoding(T, d):
                $     pe = torch.zeros(T, d)
                $     pos = torch.arange(T).unsqueeze(1).float()
                $     div = torch.exp(torch.arange(0, d, 2).float() * (-math.log(10000.0) / d))
                $     pe[:, 0::2] = torch.sin(pos * div)
                $     pe[:, 1::2] = torch.cos(pos * div)
                $     return pe
            """,
                fm("Sinus-Kodierung", "PE(t, 2i) = sin(t / 10000^{2i/d}),  PE(t, 2i+1) = cos(t / 10000^{2i/d})"),
                fm("Verschiebung", "PE(t + k) = M_k · PE(t) mit blockweiser Drehmatrix M_k")
            ),
            sec("Gelernte Positionen und RoPE", """
                • Gelernte absolute Positionen (BERT, GPT-2): eine Einbettungstabelle mit einem Vektor pro Position bis zur maximalen Länge – einfach, aber nicht über die Trainingslänge hinaus nutzbar.
                • Rotary Position Embedding (RoPE; Llama, Gemma, Mistral): dreht Query- und Key-Vektoren paarweise um einen Winkel proportional zur Position. Im Skalarprodukt qᵀk bleibt dann nur die Differenz der Positionen übrig – Attention hängt natürlich von relativen Abständen ab.
                • ALiBi: addiert eine lineare Strafe für den Abstand direkt auf die Attention-Scores.
            """,
                fm("RoPE", "⟨R_m q, R_n k⟩ = ⟨q, R_{n−m} k⟩")
            ),
            sec("Wichtige Details", """
                Die Positionskodierung hat dieselbe Dimension d wie die Einbettung, damit man sie addieren kann. In vielen Implementierungen skaliert man die Token-Einbettung vorher mit √d, damit die Positionsinformation sie nicht übertönt. Bei Lektion 3 (Keras) war die Positionseinbettung eine gelernte Embedding-Schicht – beide Varianten funktionieren in der Praxis ähnlich gut.

                $ x = token_emb(ids) * math.sqrt(d) + pe[:ids.size(1)]
            """)
        ),
        bite(
            "tr_attention", 2, "Attention und Self-Attention Schritt für Schritt",
            "Queries, Keys, Values: Ähnlichkeit per Skalarprodukt, Skalierung mit √d_k, Softmax, gewichtetes Mittel – mit einem Rechenbeispiel von Hand.",
            goals(
                "Scaled Dot-Product Attention von Hand für ein kleines Beispiel ausrechnen",
                "Die Rolle von Q, K, V und der Skalierung √d_k begründen",
                "Attention in wenigen Zeilen PyTorch implementieren"
            ),
            sec("Die Idee: weiche Suche", """
                Attention ist eine weiche Version eines Wörterbuchzugriffs. Jedes Token stellt eine Anfrage (Query). Alle Tokens bieten einen Schlüssel (Key) und einen Inhalt (Value). Die Ähnlichkeit von Query und Key (Skalarprodukt) bestimmt, wie viel vom jeweiligen Value in die Ausgabe einfließt. Q, K und V entstehen aus derselben Eingabe X durch drei gelernte lineare Abbildungen.
            """,
                fm("Projektionen", "Q = XW_Q,  K = XW_K,  V = XW_V"),
                fm("Attention", "Attention(Q, K, V) = softmax(QKᵀ / √d_k) V"),
                fm("Ausgabe eines Tokens", "oᵢ = Σⱼ αᵢⱼ vⱼ,  αᵢⱼ = softmax_j(qᵢ·kⱼ/√d_k)")
            ),
            sec("Ein Rechenbeispiel", """
                Zwei Tokens, d_k = 2. Die Query des ersten Tokens sei q = (1, 0), die Keys k₁ = (1, 0) und k₂ = (0, 1), die Values v₁ = (1, 2) und v₂ = (3, 4).

                • Scores: q·k₁ = 1, q·k₂ = 0.
                • Skaliert mit √2: 0,707 und 0.
                • Softmax: e^{0,707} = 2,03 und e⁰ = 1 → α = (0,67; 0,33).
                • Ausgabe: 0,67·(1, 2) + 0,33·(3, 4) = (1,66; 2,66).

                Das erste Token „achtet“ also zu zwei Dritteln auf sich selbst und zu einem Drittel auf das zweite.
            """),
            sec("Warum durch √d_k teilen?", """
                Sind die Komponenten von q und k unabhängig mit Mittelwert 0 und Varianz 1, hat das Skalarprodukt die Varianz d_k. Bei d_k = 64 lägen die Scores typischerweise bei ±8 – die Softmax wäre fast ein harter Maximum-Operator mit winzigen Gradienten. Die Division durch √d_k bringt die Varianz zurück auf 1.
            """,
                fm("Varianz des Skalarprodukts", "Var(q·k) = Σᵢ Var(qᵢkᵢ) = d_k"),
                fm("Nach Skalierung", "Var(q·k/√d_k) = 1")
            ),
            sec("In PyTorch", """
                $ import torch, torch.nn.functional as F
                $ def attention(Q, K, V, mask=None):
                $     d_k = Q.size(-1)
                $     scores = Q @ K.transpose(-2, -1) / d_k ** 0.5      # (B, T, T)
                $     if mask is not None:
                $         scores = scores.masked_fill(mask == 0, float("-inf"))
                $     weights = F.softmax(scores, dim=-1)                 # jede Zeile summiert zu 1
                $     return weights @ V, weights
                $ out = F.scaled_dot_product_attention(Q, K, V, is_causal=True)   # fertige, schnelle Version

                Die Gewichtsmatrix ist T × T – daher der quadratische Aufwand. Masken setzen verbotene Scores auf −∞, die nach der Softmax zu exakt 0 werden.
            """)
        ),
        bite(
            "tr_multihead", 2, "Multi-Head-Attention und der Transformer-Block",
            "Mehrere Köpfe für verschiedene Beziehungen, Zusammenführen, Residuen, LayerNorm, Feed-Forward – und die Parameterbilanz eines Blocks.",
            goals(
                "Multi-Head-Attention als parallele Attention in Unterräumen verstehen",
                "Den Aufbau eines Transformer-Blocks (Pre-Norm) beschreiben",
                "Die Parameterzahl eines Blocks abschätzen"
            ),
            sec("Viele Köpfe", """
                Ein einzelner Attention-Kopf mittelt mit einer einzigen Gewichtsverteilung. Mehrere Köpfe lernen verschiedene Beziehungen gleichzeitig: einer achtet auf das vorige Wort, einer auf das Subjekt zum Verb, einer auf passende Klammern. Man teilt d_model auf h Köpfe der Dimension d_k = d_model/h auf, rechnet Attention in jedem Unterraum und fügt die Ergebnisse mit einer Ausgabeprojektion W_O zusammen. Der Rechenaufwand ist ungefähr derselbe wie bei einem großen Kopf.
            """,
                fm("Kopf", "headᵢ = Attention(XW_Qⁱ, XW_Kⁱ, XW_Vⁱ)"),
                fm("Multi-Head", "MHA(X) = Concat(head₁, …, head_h) W_O"),
                fm("Kopfdimension", "d_k = d_model / h, z. B. 768/12 = 64")
            ),
            sec("Der Block", """
                Ein moderner Transformer-Block (Pre-Norm, wie GPT-2 und Llama):

                • h = x + MHA(LayerNorm(x))
                • y = h + FFN(LayerNorm(h)) mit FFN(z) = W₂ φ(W₁z), meist 4-fach breit, φ = GELU oder SwiGLU

                Die Residualverbindungen bilden einen „Residualstrom“, in den jeder Block liest und schreibt. LayerNorm normiert jedes Token über seine Merkmale.

                $ class Block(nn.Module):
                $     def __init__(self, d, h):
                $         super().__init__()
                $         self.ln1, self.ln2 = nn.LayerNorm(d), nn.LayerNorm(d)
                $         self.attn = nn.MultiheadAttention(d, h, batch_first=True)
                $         self.ffn = nn.Sequential(nn.Linear(d, 4 * d), nn.GELU(), nn.Linear(4 * d, d))
                $     def forward(self, x, mask=None):
                $         a = self.ln1(x)
                $         x = x + self.attn(a, a, a, attn_mask=mask, need_weights=False)[0]
                $         return x + self.ffn(self.ln2(x))
            """),
            sec("Parameterbilanz", """
                Attention hat vier Matrizen d × d (Q, K, V, O): 4d². Das FFN hat zwei Matrizen d × 4d: 8d². Pro Block also etwa 12d² Parameter (ohne Biases und Normen). GPT-2 small: d = 768, 12 Blöcke → 12 · 12 · 768² ≈ 85 Mio., dazu die Einbettung 50 257 · 768 ≈ 38,6 Mio. – zusammen rund 124 Mio.
            """,
                fm("Parameter pro Block", "P ≈ 4d² + 8d² = 12d²"),
                fm("Gesamtmodell", "P ≈ 12 L d² + |V| d")
            ),
            sec("Was die Köpfe lernen", """
                Untersuchungen (Interpretierbarkeit) zeigen spezialisierte Köpfe: Positionsköpfe, syntaktische Köpfe, „Induction Heads“, die Muster wie „A B … A → B“ vervollständigen und für In-Context-Learning wichtig sind. Viele Köpfe lassen sich nach dem Training entfernen, ohne dass die Leistung stark sinkt – ein Ansatzpunkt für Pruning.

                > Merke: Attention mischt zwischen Tokens, das FFN verarbeitet jedes Token für sich, die Residuen halten alles zusammen.
            """)
        ),
        bite(
            "tr_gpt", 2, "Decoder-Modelle: GPT und das kausale Sprachmodell",
            "Kausale Maske, Training auf das nächste Token mit verschobenen Labels, autoregressive Generierung und Sampling-Strategien.",
            goals(
                "Die kausale Maske konstruieren und ihre Wirkung erklären",
                "Eingaben und Labels für das Training eines kausalen LM bilden",
                "Text mit Greedy, Temperatur, Top-k und Top-p erzeugen"
            ),
            sec("Die kausale Maske", """
                Ein Decoder-Modell darf beim Vorhersagen von Token t nur Tokens 1 … t sehen. Eine untere Dreiecksmaske setzt alle Scores oberhalb der Diagonale auf −∞. Damit kann man alle T Vorhersagen eines Satzes in einem einzigen parallelen Durchlauf trainieren – jede Position lernt, ihr nächstes Token vorherzusagen.

                $ T = 5
                $ mask = torch.tril(torch.ones(T, T))        # 1 = erlaubt
                $ scores = scores.masked_fill(mask == 0, float("-inf"))
            """,
                fm("Kausale Maske", "M_{ij} = 0 für j ≤ i, −∞ für j > i")
            ),
            sec("Training: verschobene Labels", """
                Eingabe und Ziel sind derselbe Text, um eins verschoben: Aus „der Ritter reitet heim“ wird Eingabe [der, Ritter, reitet] und Ziel [Ritter, reitet, heim]. Hugging Face macht die Verschiebung intern, wenn man labels = input_ids übergibt.

                $ from transformers import AutoTokenizer, AutoModelForCausalLM
                $ tok = AutoTokenizer.from_pretrained("gpt2"); model = AutoModelForCausalLM.from_pretrained("gpt2")
                $ batch = tok("der Ritter reitet heim", return_tensors="pt")
                $ out = model(**batch, labels=batch["input_ids"])
                $ print(out.loss, torch.exp(out.loss))     # Kreuzentropie und Perplexität
            """,
                fm("Verlust", "L = −(1/(T−1)) Σ_{t=1}^{T−1} ln p_θ(x_{t+1} | x_{≤t})")
            ),
            sec("Generieren", """
                Bei der Inferenz erzeugt das Modell ein Token, hängt es an und rechnet weiter. Der KV-Cache speichert Keys und Values früherer Tokens, damit pro Schritt nur das neue Token berechnet werden muss.

                • Greedy: immer das wahrscheinlichste Token – deterministisch, neigt zu Wiederholungen.
                • Temperatur T: Logits durch T teilen; T < 1 schärft, T > 1 flacht ab.
                • Top-k: nur aus den k wahrscheinlichsten Tokens ziehen.
                • Top-p (Nucleus): aus der kleinsten Menge ziehen, deren Wahrscheinlichkeit zusammen ≥ p ist.

                $ ids = tok("Es war einmal ein Ritter", return_tensors="pt").input_ids
                $ out = model.generate(ids, max_new_tokens=40, do_sample=True, temperature=0.8, top_p=0.9)
                $ print(tok.decode(out[0], skip_special_tokens=True))
            """,
                fm("Temperatur", "pᵢ = exp(zᵢ/T) / Σⱼ exp(zⱼ/T)")
            ),
            sec("Ein Mini-GPT von Grund auf", """
                Im IBM-Lab baut man ein kleines Decoder-Modell aus Einbettung, Positionskodierung, einem nn.TransformerEncoder mit kausaler Maske und einer linearen Ausgabeschicht über das Vokabular – „Encoder“ heißt die PyTorch-Klasse nur, weil sie keine Cross-Attention hat. Mit der Maske ist sie ein Decoder im GPT-Sinn.

                $ layer = nn.TransformerEncoderLayer(d_model=128, nhead=4, batch_first=True)
                $ body = nn.TransformerEncoder(layer, num_layers=2)
                $ causal = nn.Transformer.generate_square_subsequent_mask(T)
                $ logits = lm_head(body(emb(ids) + pe[:T], mask=causal, is_causal=True))
            """)
        ),
        bite(
            "tr_bert", 2, "Encoder-Modelle: BERT, MLM und NSP",
            "Bidirektionaler Kontext, maskiertes Sprachmodell mit der 80-10-10-Regel, Next Sentence Prediction, [CLS] und [SEP], und Fine-Tuning für Klassifikation.",
            goals(
                "Das MLM-Vortraining mit der 80-10-10-Regel beschreiben",
                "Eingaben für BERT mit [CLS], [SEP] und Segment-IDs bilden",
                "BERT für Klassifikation und Fragebeantwortung einsetzen"
            ),
            sec("Bidirektional dank Maskierung", """
                BERT (Devlin et al. 2018) ist ein Transformer-Encoder ohne kausale Maske: Jedes Token sieht den ganzen Satz. Damit es beim Vorhersagen nicht einfach abschreibt, maskiert man 15 % der Tokens und lässt nur diese rekonstruieren (Masked Language Modeling). Von den ausgewählten Tokens werden 80 % durch [MASK] ersetzt, 10 % durch ein zufälliges Token und 10 % bleiben unverändert – so lernt das Modell auch für Eingaben ohne [MASK] gute Darstellungen.
            """,
                fm("MLM-Verlust", "L = −Σ_{t ∈ M} ln p(xₜ | x̃)"),
                fm("80-10-10", "von 15 % der Tokens: 80 % [MASK], 10 % zufällig, 10 % unverändert")
            ),
            sec("Next Sentence Prediction und Eingabeformat", """
                Die zweite Vortrainingsaufgabe: Folgt Satz B tatsächlich auf Satz A (50 %) oder ist er zufällig (50 %)? Die Entscheidung liest man am [CLS]-Token ab. Spätere Arbeiten (RoBERTa) zeigten, dass NSP wenig bringt und ließen es weg.

                $ tok = AutoTokenizer.from_pretrained("bert-base-uncased")
                $ enc = tok("The knight rides.", "He reaches the castle.")
                $ print(tok.convert_ids_to_tokens(enc["input_ids"]))
                $ # ['[CLS]', 'the', 'knight', 'rides', '.', '[SEP]', 'he', 'reaches', 'the', 'castle', '.', '[SEP]']
                $ print(enc["token_type_ids"])   # 0 für Satz A, 1 für Satz B

                Die Eingabe-Einbettung ist die Summe aus Token-, Positions- und Segment-Einbettung.
            """,
                fm("Eingabe-Einbettung", "xₜ = E_token + E_position + E_segment")
            ),
            sec("Maskierte Vorhersage ausprobieren", """
                $ from transformers import pipeline
                $ fill = pipeline("fill-mask", model="bert-base-uncased")
                $ print(fill("The knight rode his [MASK] to the castle.")[0]["token_str"])   # z. B. 'horse'

                BERT-base hat 12 Schichten, d = 768, 12 Köpfe und 110 Mio. Parameter; BERT-large 24 Schichten, d = 1024, 340 Mio.
            """),
            sec("Fine-Tuning", """
                Für Satzklassifikation setzt man einen linearen Kopf auf die Ausgabe am [CLS]-Token, für Named Entity Recognition einen Kopf pro Token, für Frage-Antwort zwei Köpfe, die Start und Ende der Antwortspanne im Kontext vorhersagen. Das ganze Modell wird einige Epochen mit kleiner Lernrate (2–5·10⁻⁵) nachtrainiert.

                $ from transformers import AutoModelForSequenceClassification
                $ clf = AutoModelForSequenceClassification.from_pretrained("bert-base-uncased", num_labels=2)

                > BERT versteht, GPT erzeugt: Für Klassifikation und Suche ist ein Encoder oft schneller und genauer als ein großes generatives Modell.
            """)
        ),
        bite(
            "tr_translation", 3, "Der Encoder-Decoder-Transformer für Übersetzung",
            "Das Originalmodell von 2017: Encoder-Stapel, Decoder mit maskierter Self-Attention und Cross-Attention, Training mit Teacher Forcing und Dekodierung.",
            goals(
                "Die drei Attention-Arten im Encoder-Decoder unterscheiden",
                "nn.Transformer mit Quell- und Zielmasken korrekt aufrufen",
                "Übersetzungen erzeugen und mit BLEU bewerten"
            ),
            sec("Drei Arten von Attention", """
                • Encoder-Self-Attention: jedes Quelltoken sieht alle Quelltokens (keine kausale Maske, nur Padding-Maske).
                • Maskierte Decoder-Self-Attention: jedes Zieltoken sieht nur frühere Zieltokens (kausale Maske).
                • Cross-Attention: Queries aus dem Decoder, Keys und Values aus der Encoder-Ausgabe („Memory“) – hier entscheidet der Decoder, auf welche Quellwörter er beim nächsten Wort achtet.
            """,
                fm("Cross-Attention", "CA = softmax(Q_dec K_encᵀ/√d_k) V_enc")
            ),
            sec("nn.Transformer", """
                $ model = nn.Transformer(d_model=512, nhead=8, num_encoder_layers=3, num_decoder_layers=3, batch_first=True)
                $ src = src_emb(src_ids) + pe[:S]                       # (B, S, d)
                $ tgt_in = tgt_ids[:, :-1]                              # <bos> y₁ … y_{n−1}
                $ tgt = tgt_emb(tgt_in) + pe[:tgt_in.size(1)]
                $ tgt_mask = nn.Transformer.generate_square_subsequent_mask(tgt_in.size(1))
                $ out = model(src, tgt, tgt_mask=tgt_mask, src_key_padding_mask=(src_ids == PAD), tgt_key_padding_mask=(tgt_in == PAD))
                $ logits = generator(out)                               # (B, T−1, |V_ziel|)
                $ loss = F.cross_entropy(logits.reshape(-1, logits.size(-1)), tgt_ids[:, 1:].reshape(-1), ignore_index=PAD)

                Teacher Forcing ist hier eingebaut: Der Decoder bekommt das Ziel um eins verschoben und sagt jede Position parallel voraus. ignore_index sorgt dafür, dass Padding nicht in den Verlust eingeht.
            """),
            sec("Übersetzen", """
                Bei der Inferenz kodiert man den Quellsatz einmal und erzeugt das Ziel Token für Token (greedy oder mit Beam Search), bis <eos> erscheint.

                $ memory = model.encoder(src)
                $ ys = torch.tensor([[BOS]])
                $ for _ in range(max_len):
                $     mask = nn.Transformer.generate_square_subsequent_mask(ys.size(1))
                $     out = model.decoder(tgt_emb(ys) + pe[:ys.size(1)], memory, tgt_mask=mask)
                $     next_id = generator(out[:, -1]).argmax(-1, keepdim=True)
                $     ys = torch.cat([ys, next_id], dim=1)
                $     if next_id.item() == EOS: break

                Im IBM-Lab übersetzt das Modell Deutsch ↔ Englisch (Multi30k) und wird mit BLEU bewertet.
            """),
            sec("Das Originalmodell in Zahlen", """
                Transformer-base (2017): 6 Encoder- und 6 Decoder-Schichten, d = 512, 8 Köpfe, FFN 2048, 65 Mio. Parameter; trainiert auf WMT 2014 Englisch–Deutsch erreichte es 27,3 BLEU (big-Variante 28,4) – damals Bestwert, bei einem Bruchteil der Trainingskosten rekurrenter Modelle.

                > Encoder-Decoder ist die natürliche Wahl, wenn Eingabe und Ausgabe verschiedene Folgen sind: Übersetzung, Zusammenfassung, Spracherkennung (Whisper).
            """,
                fm("Lernratenplan (Original)", "η = d^{−0,5} · min(step^{−0,5}, step · warmup^{−1,5})")
            )
        )
    )

    val questions: List<AiQuestion> = quiz("l9") {
        on("tr_posenc")
        mc(1, "Warum braucht ein Transformer eine Positionskodierung?",
            "Self-Attention ist permutationsäquivariant und kennt die Reihenfolge der Tokens nicht", "Um das Vokabular zu verkleinern", "Um Padding zu entfernen", "Weil die Softmax sonst nicht summiert",
            why = "Vertauschte Eingaben ergäben nur vertauschte Ausgaben.")
        mc(2, "Wie lautet die sinusförmige Kodierung für gerade Dimensionen?",
            "PE(t, 2i) = sin(t / 10000^{2i/d})", "PE(t, 2i) = t / d", "PE(t, 2i) = e^{−t}", "PE(t, 2i) = cos(i · t)",
            why = "Ungerade Dimensionen bekommen den Kosinus mit derselben Frequenz.")
        mc(2, "Warum kann das Modell mit Sinus-Kodierungen relative Positionen lernen?",
            "PE(t + k) ist eine feste lineare Abbildung (Drehung) von PE(t)", "Weil die Werte ganzzahlig sind", "Weil sie gelernt werden", "Weil sie zufällig sind",
            why = "Additionstheoreme: sin(a + b), cos(a + b) sind Linearkombinationen von sin a, cos a.")
        codeOut(2, "Welche Form hat pe?", """
            pe = positional_encoding(T=50, d=16)
            print(pe.shape, float(pe[0, 1]))
        """, "torch.Size([50, 16]) 1.0", "torch.Size([16, 50]) 0.0", "torch.Size([50, 8]) 1.0", "torch.Size([50, 16]) 0.0",
            why = "Für t = 0 ist sin(0) = 0 und cos(0) = 1 – Spalte 1 ist ein Kosinus.")
        mc(2, "Was macht RoPE?",
            "Es dreht Query- und Key-Vektoren positionsabhängig, sodass ihr Skalarprodukt nur vom Abstand abhängt", "Es addiert eine gelernte Tabelle", "Es entfernt die Positionen", "Es halbiert die Einbettung",
            why = "⟨R_m q, R_n k⟩ = ⟨q, R_{n−m} k⟩ – Grundlage von Llama, Gemma, Mistral.")
        mc(2, "Welchen Nachteil haben gelernte absolute Positionen wie bei BERT?",
            "Sie existieren nur bis zur maximalen Trainingslänge (z. B. 512)", "Sie sind nicht differenzierbar", "Sie brauchen keine Parameter", "Sie funktionieren nur auf Englisch",
            why = "Längere Eingaben haben keinen Positionsvektor.")
        line(2, "Wie kombiniert man Token-Einbettung und Positionskodierung (Original)?",
            "x = token_emb(ids) * math.sqrt(d) + pe[:ids.size(1)]",
            "x = torch.cat([token_emb(ids), pe], dim=0)",
            "x = token_emb(ids) * pe",
            "x = pe[ids]",
            why = "Addition gleicher Dimensionen; √d-Skalierung verhindert, dass die Positionen die Token-Information übertönen.")
        mc(3, "Welche Wellenlängen decken die Sinus-Kodierungen des Originals ab?",
            "Geometrisch von 2π bis 10 000 · 2π", "Alle gleich 2π", "Linear von 1 bis d", "Zufällig",
            why = "Niedrige Dimensionen schwingen schnell (feine Position), hohe langsam (grobe Position) – wie die Stellen einer Uhr.")

        on("tr_attention")
        mc(1, "Was bestimmt in der Attention, wie stark ein Token auf ein anderes achtet?",
            "Die Ähnlichkeit (Skalarprodukt) seiner Query mit dem Key des anderen Tokens", "Der Abstand der Tokens im Satz", "Die Länge der Wörter", "Der Value allein",
            why = "αᵢⱼ = softmax_j(qᵢ·kⱼ/√d_k).")
        mc(2, "q = (1, 0), k₁ = (1, 0), k₂ = (0, 1), d_k = 2. Wie groß ist das Gewicht auf Token 1?",
            "≈ 0,67", "1,0", "0,5", "≈ 0,33",
            why = "Scores 1/√2 ≈ 0,707 und 0; e^{0,707}/(e^{0,707} + 1) ≈ 2,03/3,03 ≈ 0,67.")
        mc(2, "Warum teilt man die Scores durch √d_k?",
            "Die Varianz des Skalarprodukts wächst mit d_k; ohne Skalierung sättigt die Softmax und die Gradienten werden winzig", "Um negative Werte zu vermeiden",
            "Um die Rechnung zu beschleunigen", "Weil d_k gerade ist",
            why = "Var(q·k) = d_k bei unabhängigen Komponenten mit Varianz 1.")
        codeOut(2, "Welche Form hat weights?", """
            Q = torch.randn(4, 10, 64); K = torch.randn(4, 10, 64); V = torch.randn(4, 10, 64)
            out, weights = attention(Q, K, V)
            print(weights.shape)
        """, "torch.Size([4, 10, 10])", "torch.Size([4, 10, 64])", "torch.Size([10, 10])", "torch.Size([4, 64, 64])",
            why = "Für jedes Token eine Verteilung über alle 10 Tokens.")
        code(2, "Was bewirkt masked_fill mit −∞ vor der Softmax?", """
            scores = scores.masked_fill(mask == 0, float("-inf"))
            weights = F.softmax(scores, dim=-1)
        """, "Maskierte Positionen erhalten nach der Softmax exakt das Gewicht 0", "Sie bekommen das Gewicht 1", "Die Softmax liefert NaN für alle", "Die Maske wird ignoriert",
            why = "e^{−∞} = 0. (Ist eine ganze Zeile maskiert, entsteht allerdings NaN – das vermeidet man.)")
        mc(1, "Woher stammen Q, K und V bei Self-Attention?",
            "Aus derselben Eingabe X über drei gelernte Matrizen W_Q, W_K, W_V", "Aus drei verschiedenen Sätzen", "Q aus dem Encoder, K und V aus dem Decoder", "Sie sind fest vorgegeben",
            why = "Bei Cross-Attention kämen K und V dagegen aus einer anderen Folge.")
        mc(2, "Was gilt für jede Zeile der Attention-Gewichtsmatrix?",
            "Sie summiert zu 1 und alle Einträge sind nichtnegativ", "Sie summiert zu 0", "Sie ist immer symmetrisch", "Sie enthält nur Nullen und Einsen",
            why = "Jede Ausgabe ist eine konvexe Kombination der Values.")
        line(3, "Welche fertige PyTorch-Funktion berechnet kausale Scaled Dot-Product Attention effizient?",
            "F.scaled_dot_product_attention(Q, K, V, is_causal=True)", "torch.attention(Q, K, V)", "nn.Softmax(Q @ K)", "F.multi_head(Q, K, V)",
            why = "Sie nutzt je nach Hardware Flash-Attention oder speichersparende Kerne.")

        on("tr_multihead")
        mc(1, "Wozu dienen mehrere Attention-Köpfe?",
            "Verschiedene Köpfe können gleichzeitig verschiedene Beziehungen zwischen Tokens lernen", "Um mehrere Sätze gleichzeitig zu übersetzen", "Um die Parameterzahl zu vervielfachen", "Um Padding zu entfernen",
            why = "Jeder Kopf arbeitet in einem eigenen Unterraum der Dimension d/h.")
        mc(1, "d_model = 768 und 12 Köpfe. Welche Dimension hat jeder Kopf?",
            "64", "768", "12", "9216",
            why = "768 / 12 = 64.")
        mc(2, "Wie viele Parameter hat ein Transformer-Block etwa bei d = 1024 (ohne Biases)?",
            "≈ 12,6 Millionen", "≈ 1 Million", "≈ 4 Millionen", "≈ 100 Millionen",
            why = "12d² = 12 · 1 048 576 ≈ 12,6 Mio.")
        mc(2, "Wie verteilen sich die Parameter eines Blocks ungefähr?",
            "Ein Drittel Attention (4d²), zwei Drittel Feed-Forward (8d²)", "Alles Attention", "Alles LayerNorm", "Halb und halb",
            why = "Bei 4-fach breitem FFN: 2 · d · 4d = 8d².")
        code(2, "Was ist das Muster in diesem Block?", """
            x = x + self.attn(self.ln1(x))
            x = x + self.ffn(self.ln2(x))
        """, "Pre-Norm mit Residuen: erst normieren, dann Unterblock, dann aufaddieren", "Post-Norm ohne Residuen", "Zwei getrennte Modelle", "Ein RNN",
            why = "Pre-Norm (GPT-2, Llama) trainiert stabiler als das ursprüngliche Post-Norm.")
        mc(3, "Wie viele Parameter hat GPT-2 small (L = 12, d = 768, |V| = 50 257) ungefähr?",
            "≈ 124 Millionen", "≈ 12 Millionen", "≈ 1,5 Milliarden", "≈ 345 Millionen",
            why = "12 · 12 · 768² ≈ 85 Mio. plus 50 257 · 768 ≈ 38,6 Mio. Einbettung.")
        codeOut(2, "Welche Form hat die Ausgabe?", """
            mha = nn.MultiheadAttention(embed_dim=256, num_heads=8, batch_first=True)
            x = torch.randn(2, 20, 256)
            out, w = mha(x, x, x)
            print(out.shape, w.shape)
        """, "torch.Size([2, 20, 256]) torch.Size([2, 20, 20])", "torch.Size([2, 20, 32]) torch.Size([2, 8, 20, 20])", "torch.Size([2, 256]) torch.Size([20, 20])", "torch.Size([20, 2, 256]) torch.Size([2, 20, 20])",
            why = "Standardmäßig werden die Gewichte über die Köpfe gemittelt (average_attn_weights=True).")
        mc(2, "Was sind ‚Induction Heads‘?",
            "Köpfe, die Muster wie „A B … A → B“ vervollständigen und In-Context-Learning stützen", "Köpfe für Bildinduktion", "Köpfe, die nur Padding betrachten", "Ein Optimierer",
            why = "Ein klassischer Befund der mechanistischen Interpretierbarkeit.")

        on("tr_gpt")
        codeOut(1, "Was gibt der Code aus?", """
            print(torch.tril(torch.ones(3, 3)))
        """, "tensor([[1., 0., 0.], [1., 1., 0.], [1., 1., 1.]])", "tensor([[1., 1., 1.], [0., 1., 1.], [0., 0., 1.]])", "tensor([[1., 0., 0.], [0., 1., 0.], [0., 0., 1.]])", "tensor([[0., 0., 0.], [1., 0., 0.], [1., 1., 0.]])",
            why = "Untere Dreiecksmatrix: Token i darf Tokens 1 … i sehen.")
        mc(2, "Wie bildet man Eingabe und Ziel für ein kausales Sprachmodell?",
            "Ziel = Eingabe um ein Token nach links verschoben", "Ziel = Eingabe rückwärts", "Ziel = maskierte Tokens", "Ziel = Satzklasse",
            why = "Jede Position sagt ihr Folgetoken vorher; Hugging Face verschiebt intern, wenn labels = input_ids.")
        mc(2, "Warum kann man ein GPT trotz autoregressiver Generierung parallel trainieren?",
            "Die kausale Maske lässt alle Positionen gleichzeitig ihr nächstes Token vorhersagen, ohne in die Zukunft zu sehen", "Weil Training ohne Maske läuft", "Weil das Modell klein ist", "Das geht nicht",
            why = "Nur die Generierung ist sequenziell.")
        mc(2, "Was bewirkt eine Temperatur T = 0,5 beim Sampling?",
            "Die Verteilung wird schärfer; wahrscheinliche Tokens werden noch wahrscheinlicher", "Sie wird flacher", "Sie wird gleichverteilt", "Nichts",
            why = "Logits durch 0,5 teilen verdoppelt die Abstände; T → 0 ist Greedy.")
        mc(2, "Was ist Top-p-Sampling (Nucleus)?",
            "Ziehen aus der kleinsten Tokenmenge, deren Wahrscheinlichkeit zusammen mindestens p ist", "Ziehen aus den p wahrscheinlichsten Tokens", "Immer das Token mit Wahrscheinlichkeit p", "Ein Lernratenplan",
            why = "Die Größe der Menge passt sich an die Unsicherheit an – anders als Top-k.")
        code(2, "Was liefert out.loss hier?", """
            batch = tok("der Ritter reitet heim", return_tensors="pt")
            out = model(**batch, labels=batch["input_ids"])
        """, "Die mittlere Kreuzentropie der Vorhersagen des jeweils nächsten Tokens", "Die Zahl der Tokens", "Die Perplexität direkt", "Den BLEU-Wert",
            why = "torch.exp(out.loss) ist die Perplexität dieses Satzes.")
        mc(2, "Wozu dient der KV-Cache bei der Generierung?",
            "Keys und Values früherer Tokens werden gespeichert, sodass pro Schritt nur das neue Token berechnet wird", "Er speichert die Gewichte", "Er speichert die Trainingsdaten", "Er ersetzt die Softmax",
            why = "Ohne Cache wäre jeder Schritt eine Neuberechnung der ganzen Folge.")
        line(2, "Welche Zeile erzeugt eine kausale Maske für nn.Transformer-Module?",
            "nn.Transformer.generate_square_subsequent_mask(T)", "torch.causal_mask(T)", "nn.Mask(T, causal=True)", "torch.ones(T, T).triu()",
            why = "Sie liefert eine Float-Maske mit −∞ oberhalb der Diagonale und 0 sonst.")

        on("tr_bert")
        mc(1, "Wie viel Prozent der Tokens wählt BERT für das MLM-Vortraining aus?",
            "15 %", "50 %", "1 %", "100 %",
            why = "Davon 80 % [MASK], 10 % zufällig, 10 % unverändert.")
        mc(2, "Warum ersetzt BERT nicht alle ausgewählten Tokens durch [MASK]?",
            "Beim Fine-Tuning gibt es kein [MASK]; das Modell soll auch unveränderte Tokens gut darstellen", "Um Rechenzeit zu sparen", "Weil [MASK] selten ist", "Aus Lizenzgründen",
            why = "Die 10 % zufälligen und 10 % unveränderten Tokens verringern die Diskrepanz zwischen Vortraining und Anwendung.")
        codeOut(2, "Welche Tokens erzeugt der BERT-Tokenizer für ein Satzpaar?", """
            enc = tok("Hi.", "Bye.")
            print(tok.convert_ids_to_tokens(enc["input_ids"]))
        """, "['[CLS]', 'hi', '.', '[SEP]', 'bye', '.', '[SEP]']", "['hi', '.', 'bye', '.']", "['[CLS]', 'hi', '.', 'bye', '.']", "['<s>', 'hi', '.', '</s>', 'bye', '.', '</s>']",
            why = "[CLS] am Anfang, [SEP] nach jedem Satz; uncased schreibt klein.")
        mc(2, "Was ist Next Sentence Prediction?",
            "Vorhersage, ob Satz B tatsächlich auf Satz A folgt", "Vorhersage des nächsten Tokens", "Übersetzung in den nächsten Satz", "Sortieren von Sätzen",
            why = "Gelesen am [CLS]-Token; RoBERTa zeigte, dass es verzichtbar ist.")
        mc(2, "Welche drei Einbettungen werden bei BERT addiert?",
            "Token, Position und Segment", "Token, Satz und Absatz", "Wort, Zeichen und Byte", "Query, Key und Value",
            why = "Die Segment-Einbettung unterscheidet Satz A und B (token_type_ids).")
        line(1, "Welche Pipeline füllt eine [MASK]-Lücke?",
            "pipeline(\"fill-mask\", model=\"bert-base-uncased\")", "pipeline(\"text-generation\", model=\"bert-base-uncased\")", "pipeline(\"mask\", model=\"gpt2\")", "pipeline(\"translation\", model=\"bert\")",
            why = "Sie gibt die wahrscheinlichsten Tokens für die Lücke mit Scores zurück.")
        mc(2, "Wie nutzt man BERT für extraktive Fragebeantwortung?",
            "Zwei Köpfe sagen pro Kontexttoken voraus, ob dort die Antwort beginnt bzw. endet", "Es generiert die Antwort Wort für Wort", "Es klassifiziert die Frage in zwei Klassen", "Gar nicht",
            why = "Die Antwort ist die Spanne mit dem besten Start-End-Paar (SQuAD).")
        mc(3, "Wie viele Parameter hat BERT-base?",
            "Etwa 110 Millionen", "Etwa 1,1 Milliarden", "Etwa 11 Millionen", "Etwa 340 Millionen",
            why = "12 Schichten, d = 768, 12 Köpfe; BERT-large hat 340 Mio.")

        on("tr_translation")
        mc(1, "Woher kommen bei der Cross-Attention Queries, Keys und Values?",
            "Queries aus dem Decoder, Keys und Values aus der Encoder-Ausgabe", "Alle aus dem Encoder", "Alle aus dem Decoder", "Queries aus dem Encoder",
            why = "So wählt der Decoder, welche Quellwörter für das nächste Zielwort zählen.")
        mc(2, "Welche Maske braucht die Self-Attention im Decoder?",
            "Eine kausale Maske, damit kein Zieltoken in die Zukunft sieht", "Keine", "Nur eine Padding-Maske der Quelle", "Eine zufällige Maske",
            why = "Zusätzlich Padding-Masken für Quelle und Ziel.")
        code(2, "Warum wird tgt_ids so zerlegt?", """
            tgt_in = tgt_ids[:, :-1]
            target = tgt_ids[:, 1:]
        """, "Teacher Forcing: Der Decoder bekommt <bos> y₁ … und soll y₁ … <eos> vorhersagen", "Um das letzte Wort zu löschen", "Um Speicher zu sparen", "Wegen der Positionskodierung",
            why = "Eingabe und Ziel sind um eine Position verschoben.")
        mc(2, "Wozu dient ignore_index=PAD in der Kreuzentropie?",
            "Padding-Positionen gehen nicht in den Verlust ein", "Unbekannte Wörter werden ignoriert", "Das erste Token wird ignoriert", "Es beschleunigt das Training",
            why = "Sonst würde das Modell lernen, Padding vorherzusagen.")
        mc(2, "Wie oft wird bei der greedy Übersetzung der Encoder ausgeführt?",
            "Einmal pro Quellsatz; der Decoder läuft pro erzeugtem Token", "Einmal pro Token", "Gar nicht", "Zweimal",
            why = "Die Encoder-Ausgabe (memory) wird wiederverwendet.")
        mc(3, "Welchen BLEU erreichte Transformer-big 2017 auf WMT14 Englisch–Deutsch?",
            "28,4", "12,0", "45,0", "99,0",
            why = "Transformer-base 27,3, big 28,4 – damals Bestwert.")
        codeOut(2, "Welche Form haben die Logits?", """
            out = model(src, tgt, tgt_mask=mask)       # src (8, 20, 512), tgt (8, 15, 512)
            logits = generator(out)                    # nn.Linear(512, 9000)
            print(logits.shape)
        """, "torch.Size([8, 15, 9000])", "torch.Size([8, 20, 9000])", "torch.Size([8, 15, 512])", "torch.Size([15, 9000])",
            why = "Die Decoder-Ausgabe hat die Länge des Ziels; pro Position Logits über das Zielvokabular.")
        mc(2, "Für welche Aufgabe ist ein Encoder-Decoder besonders natürlich?",
            "Wenn Eingabe und Ausgabe verschiedene Folgen sind – Übersetzung, Zusammenfassung, Spracherkennung", "Für Bildklassifikation", "Für Clustering", "Für lineare Regression",
            why = "Whisper (Sprache → Text) und T5 sind Encoder-Decoder.")
    }
}

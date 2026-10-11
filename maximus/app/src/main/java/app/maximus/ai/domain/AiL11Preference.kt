package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 11 (IBM „Generative AI Advanced Fine-Tuning for LLMs“): instruction tuning, reward models,
 * RLHF with PPO, direct preference optimisation and the TRL library.
 */
internal object AiL11Preference {
    val bites: List<Bite> = listOf(
        bite(
            "aft_instruct", 2, "Instruction Tuning und Chat-Vorlagen",
            "Vom Textfortsetzer zum Assistenten: Anweisungs-Antwort-Paare, Chat-Vorlagen mit Rollen, Verlust nur auf der Antwort, und typische Datensätze.",
            goals(
                "Erklären, warum ein vortrainiertes LLM noch kein Assistent ist",
                "Daten für Instruction Tuning formatieren und Chat-Vorlagen anwenden",
                "Den Verlust auf die Antwort-Tokens beschränken"
            ),
            sec("Vom Fortsetzer zum Assistenten", """
                Ein nur vortrainiertes Modell setzt Text fort. Auf „Wie koche ich Tee?“ antwortet es vielleicht mit weiteren Fragen aus einem FAQ, statt Tee zu erklären. Instruction Tuning (Supervised Fine-Tuning, SFT) trainiert es auf Paaren aus Anweisung und gewünschter Antwort – zehntausende bis Millionen Beispiele, oft von Menschen geschrieben oder von stärkeren Modellen erzeugt. Danach folgt das Modell Anweisungen, bleibt beim Thema und antwortet im gewünschten Stil. Das ist der erste Schritt der klassischen Ausrichtungs-Pipeline: Vortraining → SFT → Präferenzoptimierung (RLHF oder DPO).
            """),
            sec("Chat-Vorlagen", """
                Gesprächsverläufe werden mit Rollen (system, user, assistant) und Sondertokens in einen einzigen Text gebracht. Jedes Modell hat seine eigene Vorlage; der Tokenizer kennt sie.

                $ messages = [
                $     {"role": "system", "content": "Du bist ein hilfreicher Lehrmeister."},
                $     {"role": "user", "content": "Was ist ein Gradient?"},
                $ ]
                $ text = tok.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
                $ print(text)   # z. B. <|start_header_id|>user<|end_header_id|> … <|start_header_id|>assistant<|end_header_id|>

                add_generation_prompt=True hängt den Beginn der Assistentenrolle an, damit das Modell dort weiterschreibt. Wer beim Inferieren eine andere Vorlage benutzt als beim Training, bekommt deutlich schlechtere Antworten.
            """),
            sec("Verlust nur auf der Antwort", """
                Beim SFT soll das Modell lernen, Antworten zu schreiben, nicht Fragen nachzuplappern. Man setzt deshalb die Labels aller Prompt-Tokens auf −100 – diesen Wert ignoriert die Kreuzentropie in PyTorch und Hugging Face. Nur die Antwort-Tokens tragen zum Verlust bei (completion-only loss).

                $ labels = input_ids.clone()
                $ labels[:, :prompt_len] = -100
                $ loss = model(input_ids=input_ids, labels=labels).loss
            """,
                fm("SFT-Verlust", "L = −Σ_{t ∈ Antwort} ln p_θ(yₜ | x, y_{<t})")
            ),
            sec("Daten", """
                • Bekannte Datensätze: Alpaca (52 000 von einem LLM erzeugte Anweisungen), Dolly (15 000 von Menschen), FLAN (Sammlung vieler NLP-Aufgaben als Anweisungen), OpenAssistant (Gesprächsbäume).
                • Qualität schlägt Menge: Die LIMA-Studie erreichte mit 1000 sorgfältig ausgewählten Beispielen erstaunlich gute Assistenten.
                • Vielfalt der Aufgaben ist entscheidend für die Verallgemeinerung auf neue Anweisungen.

                > SFT bringt dem Modell bei, wie eine gute Antwort aussieht. Welche von zwei guten Antworten besser ist, lernt es erst mit Präferenzen.
            """)
        ),
        bite(
            "aft_reward", 2, "Belohnungsmodelle: Präferenzen und Bradley-Terry",
            "Menschen vergleichen zwei Antworten; ein Modell lernt daraus eine skalare Belohnung. Das Bradley-Terry-Modell, der paarweise Verlust und typische Fallen.",
            goals(
                "Präferenzdaten (Prompt, gewählte, abgelehnte Antwort) beschreiben",
                "Den paarweisen Verlust aus dem Bradley-Terry-Modell herleiten",
                "Ein Belohnungsmodell trainieren und seine Schwächen kennen"
            ),
            sec("Vergleichen ist leichter als bewerten", """
                Menschen tun sich schwer, einer Antwort eine absolute Note von 1 bis 10 zu geben – verschiedene Bewerter haben verschiedene Maßstäbe. Zwei Antworten zu vergleichen gelingt dagegen zuverlässig. Präferenzdaten bestehen deshalb aus Tripeln: ein Prompt x, eine bevorzugte Antwort y_w (chosen) und eine abgelehnte y_l (rejected).
            """),
            sec("Das Bradley-Terry-Modell", """
                Bradley und Terry (1952) modellierten Paarvergleiche, etwa im Schach: Die Wahrscheinlichkeit, dass A gegen B gewinnt, hängt nur von der Differenz ihrer Stärken ab. Übertragen auf Antworten: Ein Belohnungsmodell r_φ(x, y) liefert eine Zahl; die Präferenzwahrscheinlichkeit ist die Sigmoid der Differenz. Maximum Likelihood führt zum paarweisen logistischen Verlust. Die absolute Lage von r ist dabei frei – nur Differenzen zählen.
            """,
                fm("Bradley-Terry", "P(y_w ≻ y_l | x) = σ(r(x, y_w) − r(x, y_l))"),
                fm("Verlust", "L(φ) = −E[ln σ(r_φ(x, y_w) − r_φ(x, y_l))]"),
                fm("Invarianz", "r → r + c(x) ändert keine Präferenz")
            ),
            sec("Im Code", """
                Ein Belohnungsmodell ist meist ein LLM mit einem skalaren Kopf (eine Zahl pro Sequenz, gelesen am letzten Token).

                $ from transformers import AutoModelForSequenceClassification
                $ rm = AutoModelForSequenceClassification.from_pretrained("gpt2", num_labels=1)
                $ r_w = rm(**tok(prompt + chosen, return_tensors="pt")).logits
                $ r_l = rm(**tok(prompt + rejected, return_tensors="pt")).logits
                $ loss = -torch.nn.functional.logsigmoid(r_w - r_l).mean()

                Mit TRL: RewardTrainer(model=rm, args=RewardConfig(...), train_dataset=prefs, processing_class=tok) erwartet Spalten chosen und rejected.
            """),
            sec("Fallen", """
                • Reward Hacking: Die Politik findet Schwächen des Belohnungsmodells (z. B. längere Antworten werden bevorzugt) und optimiert diese statt echter Qualität.
                • Verteilungsverschiebung: Das Belohnungsmodell wurde auf Antworten des SFT-Modells trainiert; die optimierte Politik erzeugt bald andere Texte.
                • Uneinige Bewerter: Präferenzen sind verrauscht; die Genauigkeit guter Belohnungsmodelle auf Testpaaren liegt oft nur bei 65–75 %.

                > Das Belohnungsmodell ist ein Stellvertreter für menschliches Urteil – und jeder Stellvertreter kann ausgenutzt werden (Goodharts Gesetz).
            """)
        ),
        bite(
            "aft_rlhf", 3, "RLHF mit PPO: Politik, Wertfunktion, KL-Strafe",
            "Das Sprachmodell als Politik im Reinforcement Learning: Belohnung vom Belohnungsmodell, KL-Strafe zum Referenzmodell, Proximal Policy Optimization mit Clipping.",
            goals(
                "Textgenerierung als RL-Problem (Zustand, Aktion, Politik, Belohnung) formulieren",
                "Das RLHF-Ziel mit KL-Strafe aufschreiben und begründen",
                "Den geclippten PPO-Verlust und die Rolle des Vorteils erklären"
            ),
            sec("Text als Reinforcement Learning", """
                Im RLHF ist das Sprachmodell die Politik π_θ: Der Zustand ist Prompt plus bisher erzeugter Text, die Aktion das nächste Token. Am Ende der Antwort vergibt das Belohnungsmodell eine Belohnung. Ziel ist, die erwartete Belohnung zu maximieren – ohne sich zu weit vom SFT-Modell (Referenz π_ref) zu entfernen. Die KL-Strafe verhindert Reward Hacking und den Verlust sprachlicher Qualität.
            """,
                fm("RLHF-Ziel", "max_θ E_{x, y ~ π_θ}[r_φ(x, y)] − β · KL(π_θ(·|x) ‖ π_ref(·|x))"),
                fm("KL je Token (Schätzer)", "ln π_θ(yₜ|…) − ln π_ref(yₜ|…)")
            ),
            sec("Proximal Policy Optimization", """
                PPO (Schulman et al. 2017) verbessert die Politik in kleinen, sicheren Schritten. Es vergleicht die neue mit der alten Wahrscheinlichkeit jeder Aktion (Verhältnis ρ) und gewichtet mit dem Vorteil A – wie viel besser die Aktion als erwartet war. Das Clipping von ρ auf [1 − ε, 1 + ε] (typisch ε = 0,2) verhindert, dass ein einzelner Batch die Politik zu stark verändert. Den Vorteil schätzt eine gelernte Wertfunktion (Critic) mit Generalized Advantage Estimation.
            """,
                fm("Verhältnis", "ρₜ = π_θ(aₜ|sₜ) / π_θ_alt(aₜ|sₜ)"),
                fm("PPO-Clip", "L = E[min(ρₜ Aₜ, clip(ρₜ, 1−ε, 1+ε) Aₜ)]"),
                fm("GAE", "Aₜ = Σ_l (γλ)^l δ_{t+l},  δₜ = rₜ + γV(sₜ₊₁) − V(sₜ)")
            ),
            sec("Die Schleife", """
                • Prompts ziehen, mit der aktuellen Politik Antworten erzeugen (Rollout).
                • Belohnungsmodell bewerten lassen, KL-Strafe zum Referenzmodell pro Token abziehen.
                • Vorteile mit der Wertfunktion schätzen.
                • Mehrere PPO-Epochen über diese Erfahrungen: Politik und Wertfunktion aktualisieren.
                • Wiederholen.

                Im Speicher liegen dabei vier Modelle: Politik, Referenz, Belohnungsmodell und Wertfunktion. Das macht RLHF teuer und empfindlich gegenüber Hyperparametern (β, ε, Lernrate, Antwortlänge).
            """),
            sec("Mit TRL (Skizze)", """
                $ from trl import PPOConfig, PPOTrainer
                $ config = PPOConfig(learning_rate=1.4e-5, batch_size=64, mini_batch_size=16, kl_coef=0.05, cliprange=0.2)
                $ trainer = PPOTrainer(args=config, processing_class=tok, model=policy, ref_model=ref, reward_model=rm, value_model=value, train_dataset=prompts)
                $ trainer.train()

                Im IBM-Lab optimiert man so ein kleines Modell darauf, positive Filmkritiken zu schreiben; ein Sentiment-Klassifikator dient als Belohnungsmodell. Mit wachsender Belohnung sieht man die Texte freundlicher werden – und bei zu kleinem β beginnt das Modell, Floskeln zu wiederholen.
            """)
        ),
        bite(
            "aft_dpo", 3, "Direct Preference Optimization (DPO)",
            "RLHF ohne RL: Das optimale Belohnungsmodell steckt in der Politik selbst. Herleitung, Verlust, β und Varianten wie IPO, KTO und ORPO.",
            goals(
                "Die geschlossene Lösung des KL-regularisierten Ziels angeben",
                "Den DPO-Verlust herleiten und deuten",
                "DPO mit RLHF nach Aufwand und Stabilität vergleichen"
            ),
            sec("Die Schlüsselbeobachtung", """
                Das RLHF-Ziel mit KL-Strafe hat eine geschlossene Lösung: Die optimale Politik ist die Referenzpolitik, umgewichtet mit dem Exponential der Belohnung. Löst man nach r auf, wird die Belohnung zu einem Log-Verhältnis von Politik und Referenz (plus einem Term, der nur vom Prompt abhängt). Setzt man das ins Bradley-Terry-Modell ein, kürzt sich der Prompt-Term heraus – übrig bleibt ein Verlust, der nur von der Politik abhängt. Rafailov et al. (2023): „Your Language Model Is Secretly a Reward Model.“
            """,
                fm("Optimale Politik", "π*(y|x) = (1/Z(x)) π_ref(y|x) exp(r(x, y)/β)"),
                fm("Implizite Belohnung", "r(x, y) = β ln(π*(y|x)/π_ref(y|x)) + β ln Z(x)"),
                fm("DPO-Verlust", "L = −E[ln σ(β ln(π_θ(y_w|x)/π_ref(y_w|x)) − β ln(π_θ(y_l|x)/π_ref(y_l|x)))]")
            ),
            sec("Was der Verlust tut", """
                DPO ist im Kern eine logistische Regression auf Präferenzpaaren: Er erhöht die Log-Wahrscheinlichkeit der gewählten Antwort relativ zur Referenz und senkt die der abgelehnten. Der Gradient wird mit σ(r̂_l − r̂_w) gewichtet – Paare, die das Modell schon richtig ordnet, tragen wenig bei, falsch geordnete viel. β regelt, wie weit sich die Politik von der Referenz entfernen darf: klein = weit, groß = konservativ (typisch 0,1).

                $ def dpo_loss(pi_w, pi_l, ref_w, ref_l, beta=0.1):
                $     # pi_w usw.: summierte Log-Wahrscheinlichkeiten der Antwort-Tokens
                $     logits = beta * ((pi_w - ref_w) - (pi_l - ref_l))
                $     return -torch.nn.functional.logsigmoid(logits).mean()
            """,
                fm("Gewichtung des Gradienten", "∇L ∝ −β σ(r̂_l − r̂_w)(∇ ln π(y_w|x) − ∇ ln π(y_l|x))")
            ),
            sec("DPO gegen RLHF", """
                • Kein separates Belohnungsmodell, keine Wertfunktion, kein Sampling während des Trainings – nur Politik und eingefrorene Referenz.
                • Stabil wie überwachtes Lernen, wenige Hyperparameter.
                • Offline: lernt nur aus vorhandenen Paaren; RLHF kann online neue Antworten erkunden.
                • In der Praxis erreichen DPO-Modelle (z. B. Zephyr, viele offene Llama-Feinabstimmungen) vergleichbare Qualität bei einem Bruchteil des Aufwands.
            """),
            sec("Varianten", """
                • IPO: ersetzt den logistischen Verlust durch einen quadratischen, um Überanpassung an deterministische Präferenzen zu vermeiden.
                • KTO: braucht keine Paare, nur einzelne Antworten mit „gut“ oder „schlecht“ (nach der Prospect Theory).
                • ORPO: kombiniert SFT und Präferenzoptimierung in einem Schritt ohne Referenzmodell.
                • SimPO: nutzt die längennormierte Log-Wahrscheinlichkeit als implizite Belohnung, ebenfalls ohne Referenz.

                > DPO hat gezeigt: Präferenzlernen braucht nicht zwingend Reinforcement Learning – die Mathematik des KL-regularisierten Ziels erlaubt eine Abkürzung.
            """)
        ),
        bite(
            "aft_trl", 2, "Die TRL-Bibliothek: SFT, Belohnung, PPO, DPO in der Praxis",
            "Hugging Face TRL bündelt die Ausrichtungs-Pipeline: SFTTrainer, RewardTrainer, PPOTrainer, DPOTrainer – mit LoRA kombinierbar, mit Datensätzen im richtigen Format.",
            goals(
                "Die Trainer von TRL einer Stufe der Pipeline zuordnen",
                "Datensätze im Format für SFT und DPO vorbereiten",
                "Eine DPO-Feinabstimmung mit LoRA aufsetzen und bewerten"
            ),
            sec("Die Pipeline in TRL", """
                • SFTTrainer: überwachtes Fine-Tuning auf Anweisungen oder Gesprächen, mit Chat-Vorlagen, Packing und Completion-only-Verlust.
                • RewardTrainer: Belohnungsmodell aus Paaren (chosen, rejected).
                • PPOTrainer: RLHF mit Politik, Referenz, Belohnungs- und Wertmodell.
                • DPOTrainer: direkte Präferenzoptimierung aus Paaren, mit implizitem Referenzmodell.
                • Alle akzeptieren eine peft_config – damit läuft jede Stufe auch mit LoRA oder QLoRA.
            """),
            sec("SFT", """
                $ from trl import SFTTrainer, SFTConfig
                $ from datasets import load_dataset
                $ data = load_dataset("trl-lib/Capybara", split="train")      # Spalte "messages"
                $ trainer = SFTTrainer(model="Qwen/Qwen2.5-0.5B", train_dataset=data, args=SFTConfig(output_dir="sft", max_length=1024), peft_config=LoraConfig(r=16, lora_alpha=32, task_type="CAUSAL_LM"))
                $ trainer.train()

                SFTTrainer erkennt Gesprächsformate (Spalte messages) und wendet die Chat-Vorlage automatisch an. Packing füllt Sequenzen mit mehreren kurzen Beispielen auf und spart Rechenzeit.
            """),
            sec("DPO", """
                Ein DPO-Datensatz hat die Spalten prompt, chosen und rejected (als Text oder als Nachrichtenlisten).

                $ from trl import DPOTrainer, DPOConfig
                $ prefs = load_dataset("trl-lib/ultrafeedback_binarized", split="train")
                $ args = DPOConfig(output_dir="dpo", beta=0.1, learning_rate=5e-7, per_device_train_batch_size=4)
                $ trainer = DPOTrainer(model=sft_model, ref_model=None, args=args, train_dataset=prefs, processing_class=tok, peft_config=lora)
                $ trainer.train()

                Mit LoRA kann ref_model=None bleiben: Die Referenz ist dann einfach das Basismodell mit abgeschaltetem Adapter – kein zweites Modell im Speicher. Die Lernrate ist bei DPO sehr klein (10⁻⁷ bis 10⁻⁶).
            """),
            sec("Bewerten und überwachen", """
                • rewards/chosen und rewards/rejected: implizite Belohnungen; ihr Abstand (rewards/margins) sollte wachsen.
                • rewards/accuracies: Anteil der Paare, bei denen chosen höher bewertet wird.
                • Zusätzlich echte Bewertung: Benchmarks (MT-Bench, AlpacaEval), Stichproben von Hand, LLM-as-a-Judge – und auf Regressionen achten (verliert das Modell Wissen oder Höflichkeit?).

                > Die Reihenfolge der Praxis: gutes SFT-Modell → saubere Präferenzdaten → DPO mit LoRA → sorgfältige Evaluation. Erst wenn das nicht reicht, lohnt sich der Aufwand von PPO.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l11") {
        on("aft_instruct")
        mc(1, "Warum ist ein nur vortrainiertes LLM noch kein guter Assistent?",
            "Es setzt Text fort, statt Anweisungen zu befolgen", "Es kennt keine Wörter", "Es ist zu klein", "Es kann nur Englisch",
            why = "Instruction Tuning (SFT) bringt ihm bei, auf Anweisungen hilfreich zu antworten.")
        mc(1, "Welche Reihenfolge hat die klassische Ausrichtungs-Pipeline?",
            "Vortraining → SFT → Präferenzoptimierung (RLHF oder DPO)", "SFT → Vortraining → RLHF", "RLHF → Vortraining → SFT", "Nur Vortraining",
            why = "So entstanden InstructGPT, ChatGPT und viele offene Chat-Modelle.")
        code(2, "Wozu dient add_generation_prompt=True?", """
            text = tok.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
        """, "Es hängt den Beginn der Assistentenrolle an, damit das Modell dort mit seiner Antwort beginnt", "Es erzeugt eine Antwort", "Es entfernt den Systemprompt", "Es übersetzt die Nachrichten",
            why = "Ohne diesen Marker würde das Modell womöglich die Nutzerrolle fortsetzen.")
        code(2, "Was bewirkt der Wert −100 in den Labels?", """
            labels = input_ids.clone()
            labels[:, :prompt_len] = -100
        """, "Diese Positionen werden von der Kreuzentropie ignoriert; nur die Antwort trägt zum Verlust bei", "Sie werden besonders stark gewichtet", "Sie werden gelöscht", "Sie werden zu Padding",
            why = "−100 ist der Standard-ignore_index in PyTorch und Hugging Face.")
        mc(2, "Was zeigte die LIMA-Studie?",
            "Schon etwa 1000 sorgfältig ausgewählte Beispiele können einen guten Assistenten erzeugen – Qualität schlägt Menge", "Man braucht Milliarden Beispiele", "SFT ist nutzlos", "RLHF ist immer nötig",
            why = "Die Fähigkeiten kommen größtenteils aus dem Vortraining; SFT lehrt vor allem Format und Stil.")
        mc(2, "Was passiert, wenn man bei der Inferenz eine andere Chat-Vorlage nutzt als im Training?",
            "Die Antwortqualität sinkt deutlich, weil die Rollenmarker nicht erkannt werden", "Nichts", "Das Modell wird schneller", "Das Modell weigert sich immer",
            why = "Die Sondertokens der Vorlage sind Teil dessen, was das Modell gelernt hat.")
        mc(1, "Welche Rollen gibt es typischerweise in einer Chat-Vorlage?",
            "system, user, assistant", "input, output", "query, key, value", "train, test",
            why = "Manche Vorlagen kennen zusätzlich tool für Werkzeugausgaben.")
        mc(3, "Was ist ‚Packing‘ beim SFT?",
            "Mehrere kurze Beispiele werden zu einer langen Sequenz zusammengefügt, um Padding zu sparen", "Das Komprimieren der Gewichte", "Das Mischen der Daten", "Das Speichern als ZIP",
            why = "Mit passenden Masken bleiben die Beispiele voneinander getrennt.")

        on("aft_reward")
        mc(1, "Warum sammelt man Präferenzen als Vergleiche statt als absolute Noten?",
            "Vergleiche sind für Menschen zuverlässiger und konsistenter als absolute Bewertungen", "Weil Noten zu viel Speicher brauchen", "Weil das Modell keine Zahlen kann", "Aus Datenschutzgründen",
            why = "Verschiedene Bewerter haben verschiedene Skalen; bei Paarvergleichen fällt das weg.")
        mc(2, "Wie lautet die Präferenzwahrscheinlichkeit im Bradley-Terry-Modell?",
            "σ(r(x, y_w) − r(x, y_l))", "r(x, y_w) / r(x, y_l)", "r(x, y_w) · r(x, y_l)", "max(r_w, r_l)",
            why = "Nur die Differenz der Belohnungen zählt.")
        mc(2, "Wie groß ist P(y_w ≻ y_l), wenn r_w − r_l = 0?",
            "0,5", "0", "1", "e",
            why = "σ(0) = 0,5 – das Modell ist unentschieden.")
        line(2, "Welche Zeile berechnet den paarweisen Verlust des Belohnungsmodells?",
            "loss = -F.logsigmoid(r_w - r_l).mean()", "loss = (r_w - r_l) ** 2", "loss = F.cross_entropy(r_w, r_l)", "loss = r_l - r_w",
            why = "Negative Log-Likelihood des Bradley-Terry-Modells.")
        mc(2, "Was ist Reward Hacking?",
            "Die Politik nutzt Schwächen des Belohnungsmodells aus, z. B. überlange Antworten, statt echter Qualität", "Das Stehlen von Belohnungsmodellen", "Ein Hackerangriff auf die GPU", "Das Löschen von Labels",
            why = "Goodharts Gesetz: Wird ein Maß zum Ziel, hört es auf, ein gutes Maß zu sein.")
        mc(3, "Warum ändert r → r + c(x) keine Präferenz?",
            "In der Differenz r(x, y_w) − r(x, y_l) hebt sich c(x) heraus", "Weil c(x) immer null ist", "Weil σ linear ist", "Es ändert sie doch",
            why = "Belohnungen sind nur bis auf eine promptabhängige Konstante bestimmt – das nutzt die DPO-Herleitung.")
        mc(2, "Woraus besteht ein typisches Belohnungsmodell?",
            "Aus einem LLM mit skalarem Kopf, der eine Zahl pro Sequenz ausgibt", "Aus einem Entscheidungsbaum", "Aus einer Tabelle", "Aus dem Tokenizer",
            why = "AutoModelForSequenceClassification mit num_labels=1.")
        mc(2, "Welche Spalten erwartet der RewardTrainer von TRL?",
            "chosen und rejected", "input und output", "question und answer", "text und label",
            why = "Optional mit gemeinsamem prompt.")

        on("aft_rlhf")
        mc(1, "Was ist im RLHF die ‚Politik‘?",
            "Das Sprachmodell, das Token für Token Aktionen wählt", "Das Belohnungsmodell", "Die Menschen, die bewerten", "Der Tokenizer",
            why = "Zustand = Prompt + bisheriger Text, Aktion = nächstes Token.")
        mc(2, "Wozu dient die KL-Strafe zum Referenzmodell?",
            "Sie hält die Politik nahe am SFT-Modell und verhindert Reward Hacking und Sprachzerfall", "Sie beschleunigt das Sampling", "Sie ersetzt das Belohnungsmodell", "Sie misst die Accuracy",
            why = "Ziel: E[r] − β·KL(π_θ ‖ π_ref).")
        mc(2, "Was bewirkt das Clipping in PPO?",
            "Es begrenzt das Verhältnis neuer zu alter Wahrscheinlichkeit auf [1−ε, 1+ε] und verhindert zu große Politikänderungen", "Es begrenzt die Gradientennorm", "Es kürzt die Antworten", "Es entfernt Tokens",
            why = "Typisch ε = 0,2; die Politik ändert sich pro Update nur vorsichtig.")
        mc(2, "Was misst der Vorteil Aₜ?",
            "Wie viel besser eine Aktion war als der Erwartungswert der Wertfunktion", "Die absolute Belohnung", "Die Antwortlänge", "Die Lernrate",
            why = "A > 0: Aktion wahrscheinlicher machen, A < 0: unwahrscheinlicher.")
        mc(3, "Wie viele Modelle liegen beim klassischen RLHF mit PPO im Speicher?",
            "Vier: Politik, Referenz, Belohnungsmodell, Wertfunktion", "Eins", "Zwei", "Zehn",
            why = "Deshalb ist RLHF teuer – DPO braucht nur Politik und Referenz.")
        mc(2, "Wie groß ist ρ, wenn das neue Modell einer Aktion 0,3 statt vorher 0,2 Wahrscheinlichkeit gibt, und was macht das Clipping mit ε = 0,2 bei positivem Vorteil?",
            "ρ = 1,5; es wird auf 1,2 begrenzt", "ρ = 0,1; keine Änderung", "ρ = 1,5; keine Begrenzung", "ρ = 0,67; Begrenzung auf 0,8",
            why = "min(ρA, clip(ρ, 0,8, 1,2)A) = 1,2A für A > 0.")
        code(2, "Was steuert kl_coef in dieser Konfiguration?", """
            PPOConfig(learning_rate=1.4e-5, kl_coef=0.05, cliprange=0.2)
        """, "Die Stärke β der KL-Strafe zum Referenzmodell", "Die Länge der Antworten", "Die Zahl der Epochen", "Die Batchgröße",
            why = "Zu klein: Reward Hacking und Floskeln; zu groß: kaum Verbesserung.")
        mc(2, "Was diente im IBM-Lab als Belohnungsmodell, um positive Filmkritiken zu erzeugen?",
            "Ein Sentiment-Klassifikator", "Ein Mensch in Echtzeit", "Die Perplexität", "Ein Wörterbuch",
            why = "Seine Wahrscheinlichkeit für ‚positiv‘ ist die Belohnung.")

        on("aft_dpo")
        mc(1, "Was braucht DPO im Gegensatz zu RLHF mit PPO nicht?",
            "Ein separates Belohnungsmodell, eine Wertfunktion und Sampling während des Trainings", "Präferenzdaten", "Ein Referenzmodell", "Gradienten",
            why = "DPO optimiert direkt auf Paaren mit einem Verlust wie bei überwachtem Lernen.")
        mc(2, "Wie sieht die optimale Politik des KL-regularisierten Ziels aus?",
            "π* ∝ π_ref · exp(r/β)", "π* = r", "π* = π_ref", "π* ∝ r²",
            why = "Die Referenz wird mit dem Exponential der Belohnung umgewichtet.")
        mc(2, "Welche Größe ist im DPO die ‚implizite Belohnung‘ einer Antwort?",
            "β · ln(π_θ(y|x) / π_ref(y|x))", "Die Länge der Antwort", "Die Perplexität", "π_θ(y|x) allein",
            why = "Bis auf β ln Z(x), das sich in der Differenz kürzt.")
        codeOut(2, "Was ergibt dpo_loss für pi_w − ref_w = 2, pi_l − ref_l = −2, beta = 0,1 (gerundet)?", """
            logits = 0.1 * ((2.0) - (-2.0))
            print(round(-math.log(1 / (1 + math.exp(-logits))), 3))
        """, "0.513", "0.693", "0.0", "1.0",
            why = "logits = 0,4; −ln σ(0,4) = ln(1 + e^{−0,4}) ≈ 0,513. Bei logits = 0 wären es ln 2 ≈ 0,693.")
        mc(2, "Was regelt β in DPO?",
            "Wie weit sich die Politik von der Referenz entfernen darf: klein = weit, groß = konservativ", "Die Lernrate", "Die Batchgröße", "Die Zahl der Paare",
            why = "Typisch β = 0,1.")
        mc(3, "Welche Paare tragen bei DPO am meisten zum Gradienten bei?",
            "Die, die das Modell aktuell falsch ordnet – sie werden mit σ(r̂_l − r̂_w) stark gewichtet", "Die längsten", "Die bereits richtig geordneten", "Alle gleich",
            why = "Wie bei logistischer Regression lernt man vor allem aus Fehlern.")
        mc(2, "Was ist ein Nachteil von DPO gegenüber Online-RLHF?",
            "Es lernt nur aus vorhandenen Paaren und erkundet keine neuen Antworten", "Es ist instabiler", "Es braucht mehr Modelle", "Es braucht keine Daten",
            why = "Online-Varianten erzeugen neue Antworten und lassen sie bewerten.")
        mc(2, "Was ist KTO?",
            "Eine Präferenzmethode, die statt Paaren nur einzelne Antworten mit ‚gut‘ oder ‚schlecht‘ braucht", "Ein Tokenizer", "Ein Belohnungsmodell für Bilder", "Eine Variante von PPO",
            why = "Angelehnt an die Prospect Theory von Kahneman und Tversky.")

        on("aft_trl")
        mc(1, "Welcher TRL-Trainer gehört zum überwachten Instruction Tuning?",
            "SFTTrainer", "DPOTrainer", "PPOTrainer", "RewardTrainer",
            why = "SFT = Supervised Fine-Tuning.")
        mc(2, "Welche Spalten braucht ein DPO-Datensatz?",
            "prompt, chosen, rejected", "text, label", "question, context, answer", "input_ids, labels",
            why = "Als Text oder als Nachrichtenlisten im Chat-Format.")
        code(2, "Warum kann hier ref_model=None stehen?", """
            DPOTrainer(model=sft_model, ref_model=None, args=args, train_dataset=prefs, processing_class=tok, peft_config=lora)
        """, "Mit LoRA dient das Basismodell mit abgeschaltetem Adapter als Referenz – kein zweites Modell nötig", "Weil DPO keine Referenz braucht",
            "Weil das SFT-Modell gelöscht wird", "Das ist ein Fehler",
            why = "Spart den Speicher einer zweiten Modellkopie.")
        mc(2, "Welche Lernraten sind bei DPO typisch?",
            "Sehr klein, etwa 10⁻⁷ bis 10⁻⁶", "Etwa 0,1", "Etwa 1", "Etwa 10⁻²",
            why = "Präferenzoptimierung ist ein feiner Nachschliff auf einem fertigen SFT-Modell.")
        mc(2, "Was sollte während des DPO-Trainings bei rewards/margins passieren?",
            "Der Abstand zwischen impliziter Belohnung von chosen und rejected sollte wachsen", "Er sollte null bleiben", "Er sollte negativ werden", "Er ist bedeutungslos",
            why = "rewards/accuracies zeigt zusätzlich den Anteil richtig geordneter Paare.")
        line(2, "Wie übergibt man LoRA an einen TRL-Trainer?",
            "peft_config=LoraConfig(r=16, lora_alpha=32, task_type=\"CAUSAL_LM\")", "lora=True", "model.lora(16)", "use_peft=\"yes\"",
            why = "Alle TRL-Trainer akzeptieren peft_config.")
        mc(1, "Was erkennt SFTTrainer automatisch an einer Spalte messages?",
            "Das Gesprächsformat; er wendet die Chat-Vorlage des Tokenizers an", "Die Sprache", "Die Labels für Klassifikation", "Bilder",
            why = "So muss man die Vorlage nicht von Hand anwenden.")
        mc(3, "Wann lohnt sich nach SFT und DPO noch PPO?",
            "Wenn Online-Erkundung mit einem guten Belohnungsmodell nötig ist und die Ressourcen für vier Modelle da sind", "Immer zuerst", "Nie", "Nur für Bilder",
            why = "Die pragmatische Reihenfolge: SFT → DPO mit LoRA → Evaluation → bei Bedarf PPO.")
    }
}

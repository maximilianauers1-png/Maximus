package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 10 (IBM „Generative AI Engineering and Fine-Tuning Transformers“): Hugging Face with PyTorch,
 * full fine-tuning with Trainer and a plain loop, parameter-efficient fine-tuning, LoRA and QLoRA.
 */
internal object AiL10FineTune {
    val bites: List<Bite> = listOf(
        bite(
            "ft_hf_pt", 1, "Hugging Face und PyTorch: Modelle laden, Inferenz, Speicher",
            "Wie ein Hugging-Face-Modell ein PyTorch-Modul ist: Konfiguration, Gewichte, Tokenizer, Geräte, Datentypen und der Speicherbedarf großer Modelle.",
            goals(
                "Modell und Tokenizer laden und eine Vorhersage von Hand berechnen",
                "Datentyp und Gerät beim Laden wählen",
                "Den Speicherbedarf für Inferenz und Training abschätzen"
            ),
            sec("Ein Modell ist ein nn.Module", """
                Jedes Hugging-Face-Modell ist eine PyTorch-Klasse: Man kann es mit .to(device) verschieben, mit .eval() umschalten, seine Parameter zählen und in eigene Trainingsschleifen einbauen. from_pretrained lädt die Konfiguration (config.json) und die Gewichte (model.safetensors) aus dem Hub oder einem lokalen Ordner.

                $ import torch
                $ from transformers import AutoTokenizer, AutoModelForSequenceClassification
                $ name = "distilbert-base-uncased-finetuned-sst-2-english"
                $ tok = AutoTokenizer.from_pretrained(name)
                $ model = AutoModelForSequenceClassification.from_pretrained(name).eval()
                $ inputs = tok("The castle is magnificent", return_tensors="pt")
                $ with torch.no_grad():
                $     logits = model(**inputs).logits
                $ print(model.config.id2label[logits.argmax(-1).item()])   # POSITIVE
            """),
            sec("Datentypen und Geräte", """
                Große Modelle lädt man in halber Genauigkeit und verteilt sie automatisch auf die verfügbaren GPUs:

                $ from transformers import AutoModelForCausalLM
                $ llm = AutoModelForCausalLM.from_pretrained("meta-llama/Llama-3.2-1B", torch_dtype=torch.bfloat16, device_map="auto")
                $ print(sum(p.numel() for p in llm.parameters()) / 1e9, "Mrd. Parameter")
                $ llm.save_pretrained("./mein_modell"); tok.save_pretrained("./mein_modell")

                device_map="auto" (über die Bibliothek accelerate) legt Schichten auf GPU, CPU oder Festplatte, je nachdem wo Platz ist.
            """),
            sec("Speicher abschätzen", """
                Für Inferenz braucht man vor allem die Gewichte (plus KV-Cache). Für volles Training mit Adam kommen Gradienten und zwei Optimiererzustände hinzu, dazu die Aktivierungen. Ein 7-Mrd.-Modell braucht in bf16 zur Inferenz rund 14 GB, für volles Training mit Adam in gemischter Genauigkeit rund 16 Byte je Parameter, also über 110 GB – deshalb sind parameter-effiziente Verfahren so wichtig.
            """,
                fm("Inferenz", "M ≈ P · b (b = 2 Byte bei bf16, 0,5 bei int4)"),
                fm("Training mit Adam (gemischt)", "M ≈ P · (2 + 2 + 4 + 4 + 4) = 16P Byte + Aktivierungen"),
                fm("KV-Cache", "M_KV = 2 · L · T · d · b je Sequenz")
            ),
            sec("Pipelines unter der Haube", """
                Eine pipeline macht nur drei Schritte: Tokenisieren, Modell aufrufen, Ausgabe nachbearbeiten (Softmax, Labels, Dekodieren). Wer das von Hand kann, kann jedes Modell in eigene Systeme einbauen – etwa in eine LangChain-Kette oder einen eigenen Webdienst.

                > from_pretrained lädt, save_pretrained speichert, push_to_hub teilt. Das Format ist überall dasselbe – darauf beruht das ganze Ökosystem.
            """)
        ),
        bite(
            "ft_full", 2, "Volles Fine-Tuning mit Trainer und PyTorch-Schleife",
            "Ein vortrainiertes Modell auf eine Aufgabe zuschneiden: Datensatz tokenisieren, TrainingArguments, Trainer, Metriken – und dieselbe Logik als eigene Schleife.",
            goals(
                "Einen Datensatz für das Fine-Tuning tokenisieren",
                "Mit Trainer und TrainingArguments trainieren und bewerten",
                "Dieselbe Aufgabe mit einer eigenen PyTorch-Schleife lösen und die Unterschiede kennen"
            ),
            sec("Vortraining und Fine-Tuning", """
                Vortraining lernt aus riesigen unbeschrifteten Textmengen allgemeine Sprachkenntnis (selbstüberwacht). Fine-Tuning passt das Modell mit einem kleineren, beschrifteten Datensatz an eine konkrete Aufgabe an – etwa Kinokritiken als positiv oder negativ einordnen (IMDB oder Yelp im IBM-Lab). Beim vollen Fine-Tuning werden alle Gewichte angepasst, mit kleiner Lernrate (typisch 2·10⁻⁵ bis 5·10⁻⁵), damit das vortrainierte Wissen nicht zerstört wird (katastrophales Vergessen).
            """),
            sec("Mit dem Trainer", """
                $ from datasets import load_dataset
                $ from transformers import AutoTokenizer, AutoModelForSequenceClassification, TrainingArguments, Trainer
                $ ds = load_dataset("imdb")
                $ tok = AutoTokenizer.from_pretrained("bert-base-cased")
                $ ds = ds.map(lambda b: tok(b["text"], truncation=True, padding="max_length", max_length=256), batched=True)
                $ model = AutoModelForSequenceClassification.from_pretrained("bert-base-cased", num_labels=2)
                $ args = TrainingArguments(output_dir="out", learning_rate=2e-5, per_device_train_batch_size=16, num_train_epochs=2, weight_decay=0.01, eval_strategy="epoch")
                $ trainer = Trainer(model=model, args=args, train_dataset=ds["train"], eval_dataset=ds["test"], compute_metrics=compute_metrics)
                $ trainer.train()

                compute_metrics bekommt (logits, labels) und gibt ein Wörterbuch wie {"accuracy": …} zurück. Der Trainer übernimmt Batches, Gerät, gemischte Genauigkeit, Lernratenplan, Checkpoints und Logging.
            """),
            sec("Dasselbe als eigene Schleife", """
                $ from torch.utils.data import DataLoader
                $ from transformers import get_scheduler
                $ ds = ds.remove_columns(["text"]).rename_column("label", "labels").with_format("torch")
                $ loader = DataLoader(ds["train"], batch_size=16, shuffle=True)
                $ opt = torch.optim.AdamW(model.parameters(), lr=2e-5, weight_decay=0.01)
                $ steps = 2 * len(loader)
                $ sched = get_scheduler("linear", optimizer=opt, num_warmup_steps=0, num_training_steps=steps)
                $ model.train()
                $ for epoch in range(2):
                $     for batch in loader:
                $         out = model(**{k: v.to(device) for k, v in batch.items()})
                $         out.loss.backward()
                $         opt.step(); sched.step(); opt.zero_grad()

                Übergibt man labels, berechnet das Modell den Verlust selbst. AdamW entkoppelt den Weight Decay vom adaptiven Schritt.
            """,
                fm("AdamW", "θ ← θ − η(m̂/(√v̂ + ε) + λθ)"),
                fm("Linearer Plan", "ηₜ = η₀ (1 − t/T)")
            ),
            sec("Worauf achten?", """
                • Kleine Lernrate und wenige Epochen (2–4) – mehr führt schnell zu Überanpassung.
                • Validierungsmenge getrennt halten, Bestes Checkpoint wählen (load_best_model_at_end).
                • Der Kopf ist neu initialisiert; die Warnung „Some weights … newly initialized“ ist beim Laden normal.
                • Volles Fine-Tuning eines großen LLM braucht ein Vielfaches des Inferenzspeichers – nächster Happen: PEFT.
            """)
        ),
        bite(
            "ft_peft", 2, "PEFT: Adapter, Soft Prompts und Prefix Tuning",
            "Parameter-effizientes Fine-Tuning: nur einen winzigen Teil der Gewichte trainieren – Adapter-Module, Prompt Tuning, Prefix Tuning, und was das an Speicher spart.",
            goals(
                "Die Idee von PEFT und ihre Vorteile beschreiben",
                "Adapter, Soft Prompts (Prompt Tuning) und Prefix Tuning unterscheiden",
                "Die Zahl der trainierbaren Parameter eines Adapters berechnen"
            ),
            sec("Warum PEFT?", """
                Volles Fine-Tuning ändert alle Milliarden Gewichte, braucht enormen Speicher und erzeugt pro Aufgabe eine vollständige Modellkopie. PEFT friert das vortrainierte Modell ein und trainiert nur wenige zusätzliche oder ausgewählte Parameter – oft unter 1 %. Vorteile: viel weniger Speicher (keine Gradienten und Optimiererzustände für die eingefrorenen Gewichte), kleine Dateien pro Aufgabe (Megabytes statt Gigabytes), weniger katastrophales Vergessen, und ein Basismodell kann viele Aufgaben-Adapter bedienen.
            """),
            sec("Adapter", """
                Houlsby et al. (2019) fügen in jeden Transformer-Block kleine Flaschenhals-Module ein: herunterprojizieren auf r Dimensionen, Nichtlinearität, wieder hochprojizieren, Residuum. Nur die Adapter werden trainiert. Bei d = 768 und r = 64 sind das 2 · 768 · 64 + 64 + 768 ≈ 99 000 Parameter je Adapter.

                $ class Adapter(nn.Module):
                $     def __init__(self, d, r):
                $         super().__init__()
                $         self.down = nn.Linear(d, r); self.up = nn.Linear(r, d)
                $         nn.init.zeros_(self.up.weight); nn.init.zeros_(self.up.bias)   # startet als Identität
                $     def forward(self, h):
                $         return h + self.up(torch.relu(self.down(h)))
            """,
                fm("Adapter", "h ← h + W_up φ(W_down h),  W_down ∈ ℝ^{r×d}, W_up ∈ ℝ^{d×r}"),
                fm("Parameter", "P ≈ 2dr + d + r")
            ),
            sec("Soft Prompts und Prefix Tuning", """
                • Prompt Tuning (Lester et al. 2021): vor die Eingabe-Einbettungen werden k lernbare „virtuelle Tokens“ gesetzt – ein Soft Prompt. Nur diese k · d Werte werden trainiert. Bei großen Modellen erreicht das fast die Qualität vollen Fine-Tunings.
                • Prefix Tuning (Li & Liang 2021): lernbare Präfixe für Keys und Values in jeder Schicht – mächtiger als Prompt Tuning bei kleineren Modellen.
                • (IA)³: lernt nur Skalierungsvektoren für Keys, Values und FFN-Aktivierungen – extrem wenige Parameter.

                $ from peft import PromptTuningConfig, get_peft_model, TaskType
                $ cfg = PromptTuningConfig(task_type=TaskType.CAUSAL_LM, num_virtual_tokens=20)
                $ model = get_peft_model(base_model, cfg)
                $ model.print_trainable_parameters()
            """,
                fm("Soft-Prompt-Parameter", "P = k · d, z. B. 20 · 4096 = 81 920")
            ),
            sec("Die Landkarte", """
                • Additiv: Adapter, Soft Prompts, Prefix Tuning – neue Parameter kommen hinzu.
                • Selektiv: nur ausgewählte vorhandene Parameter trainieren (z. B. nur Biases: BitFit).
                • Reparametrisierend: Gewichtsänderungen in kompakter Form lernen – LoRA, der nächste Happen und heute der Standard.

                > PEFT ist keine Notlösung: Für die meisten Anpassungen ist es so gut wie volles Fine-Tuning – bei einem Bruchteil der Kosten.
            """)
        ),
        bite(
            "ft_lora", 3, "LoRA und QLoRA mathematisch und im Code",
            "Niedrigrangige Updates ΔW = BA, Rang, Alpha, welche Schichten, Zusammenführen nach dem Training – und QLoRA mit 4-Bit-NF4-Basis auf einer einzigen GPU.",
            goals(
                "LoRA als niedrigrangige Zerlegung der Gewichtsänderung formulieren",
                "Trainierbare Parameter für gegebenen Rang berechnen",
                "LoRA und QLoRA mit der peft-Bibliothek konfigurieren und Adapter zusammenführen"
            ),
            sec("Die Idee", """
                Hu et al. (2021) beobachteten: Die Gewichtsänderung beim Fine-Tuning hat einen niedrigen „intrinsischen Rang“. LoRA (Low-Rank Adaptation) friert W ein und lernt die Änderung als Produkt zweier schmaler Matrizen B (d × r) und A (r × k) mit r ≪ min(d, k). A wird zufällig, B mit null initialisiert – zu Beginn ist ΔW = 0 und das Modell unverändert. Der Faktor α/r skaliert das Update unabhängig vom gewählten Rang.
            """,
                fm("LoRA", "h = W₀x + ΔWx = W₀x + (α/r) B A x"),
                fm("Formen", "B ∈ ℝ^{d×r}, A ∈ ℝ^{r×k}, r ≪ min(d, k)"),
                fm("Parameter", "voll: d·k;  LoRA: r(d + k)")
            ),
            sec("Was das spart", """
                Für eine 4096 × 4096-Projektion hat das volle Update 16,8 Mio. Parameter, LoRA mit r = 8 nur 8 · (4096 + 4096) = 65 536 – ein Faktor 256. Angewandt auf Query und Value aller 32 Schichten eines 7-Mrd.-Modells sind das rund 4,2 Mio. trainierbare Parameter, also 0,06 %. Nach dem Training kann man BA in W₀ hineinrechnen (merge): keine zusätzliche Latenz bei der Inferenz.
            """,
                fm("Zusammenführen", "W = W₀ + (α/r) B A")
            ),
            sec("LoRA mit peft", """
                $ from peft import LoraConfig, get_peft_model, TaskType
                $ cfg = LoraConfig(task_type=TaskType.SEQ_CLS, r=8, lora_alpha=16, lora_dropout=0.1, target_modules=["q_lin", "v_lin"])
                $ model = get_peft_model(base_model, cfg)
                $ model.print_trainable_parameters()   # trainable params: … || all params: … || trainable%: 0.9
                $ trainer = Trainer(model=model, args=args, train_dataset=train, eval_dataset=val)
                $ trainer.train()
                $ merged = model.merge_and_unload()    # LoRA in die Basisgewichte einrechnen

                target_modules hängt von der Architektur ab: bei DistilBERT q_lin/v_lin, bei Llama q_proj/k_proj/v_proj/o_proj (und oft die MLP-Projektionen).
            """),
            sec("QLoRA", """
                Dettmers et al. (2023) kombinieren LoRA mit einem auf 4 Bit quantisierten Basismodell. Drei Zutaten: der Datentyp NF4 (4-Bit NormalFloat, informationstheoretisch optimal für normalverteilte Gewichte), doppelte Quantisierung der Skalierungskonstanten und Paged Optimizers gegen Speicherspitzen. Gerechnet wird in bf16: Die 4-Bit-Gewichte werden blockweise dequantisiert, die LoRA-Matrizen bleiben in voller Genauigkeit. So lässt sich ein 65-Mrd.-Modell auf einer 48-GB-GPU feinabstimmen.

                $ from transformers import BitsAndBytesConfig
                $ bnb = BitsAndBytesConfig(load_in_4bit=True, bnb_4bit_quant_type="nf4", bnb_4bit_use_double_quant=True, bnb_4bit_compute_dtype=torch.bfloat16)
                $ base = AutoModelForCausalLM.from_pretrained(name, quantization_config=bnb, device_map="auto")
                $ from peft import prepare_model_for_kbit_training
                $ base = prepare_model_for_kbit_training(base)
                $ model = get_peft_model(base, LoraConfig(r=16, lora_alpha=32, target_modules=["q_proj", "k_proj", "v_proj", "o_proj"], task_type="CAUSAL_LM"))
            """,
                fm("Speicher 7B in NF4", "≈ 7·10⁹ · 0,5 Byte ≈ 3,5 GB (+ Konstanten, LoRA, Aktivierungen)")
            )
        )
    )

    val questions: List<AiQuestion> = quiz("l10") {
        on("ft_hf_pt")
        mc(1, "Was ist ein Hugging-Face-Transformer-Modell technisch?",
            "Ein PyTorch-nn.Module mit Konfiguration und vortrainierten Gewichten", "Eine Datenbank", "Ein REST-Dienst", "Eine reine Textdatei",
            why = "Deshalb funktionieren .to(device), .eval(), parameters() und eigene Trainingsschleifen.")
        codeOut(1, "Was gibt der Code (für ein Sentiment-Modell) aus?", """
            logits = model(**tok("The castle is magnificent", return_tensors="pt")).logits
            print(model.config.id2label[logits.argmax(-1).item()])
        """, "POSITIVE", "NEGATIVE", "1", "[0.01, 0.99]",
            why = "argmax über die Logits, dann Übersetzung des Index in den Klassennamen.")
        mc(2, "Wie viel Speicher brauchen die Gewichte eines 7-Mrd.-Modells in bf16 ungefähr?",
            "14 GB", "7 GB", "28 GB", "3,5 GB",
            why = "2 Byte je Parameter. In int4 wären es etwa 3,5 GB, in float32 28 GB.")
        mc(3, "Warum braucht volles Training mit Adam rund 16 Byte je Parameter (gemischte Genauigkeit)?",
            "bf16-Gewichte und -Gradienten (je 2) plus fp32-Mastergewichte und zwei fp32-Adam-Momente (je 4)", "Weil Adam 16 Kopien speichert", "Wegen des Tokenizers", "Das stimmt nicht, es sind 2 Byte",
            why = "2 + 2 + 4 + 4 + 4 = 16 Byte, dazu Aktivierungen.")
        line(2, "Wie lädt man ein Modell in bf16 und verteilt es automatisch auf die Geräte?",
            "AutoModelForCausalLM.from_pretrained(name, torch_dtype=torch.bfloat16, device_map=\"auto\")",
            "AutoModelForCausalLM.from_pretrained(name).half().cuda(all=True)",
            "torch.load(name, dtype=\"bf16\")",
            "AutoModel(name, gpu=True)",
            why = "device_map=\"auto\" nutzt accelerate, um Schichten auf GPU, CPU oder Platte zu legen.")
        mc(2, "Welche drei Schritte stecken in jeder pipeline?",
            "Tokenisieren, Modell aufrufen, Ausgabe nachbearbeiten", "Laden, trainieren, speichern", "Mischen, batchen, cachen", "Übersetzen, prüfen, löschen",
            why = "Wer sie von Hand kann, kann jedes Modell in eigene Systeme einbauen.")
        line(1, "Wie speichert man ein Modell im Hugging-Face-Format lokal?",
            "model.save_pretrained(\"./mein_modell\")", "torch.save(model, \"hub\")", "model.export(\"./mein_modell\")", "model.to_disk()",
            why = "Laden später mit from_pretrained(\"./mein_modell\").")
        mc(2, "Wovon hängt die Größe des KV-Caches ab?",
            "Von Schichtenzahl, Folgenlänge, Modellbreite und Bytes je Wert", "Nur von der Vokabulargröße", "Nur von der Batchgröße", "Von der Lernrate",
            why = "M_KV = 2·L·T·d·b je Sequenz – bei langen Kontexten oft mehrere GB.")

        on("ft_full")
        mc(1, "Was unterscheidet Fine-Tuning vom Vortraining?",
            "Fine-Tuning passt ein vortrainiertes Modell mit kleineren, beschrifteten Daten an eine konkrete Aufgabe an", "Fine-Tuning startet mit zufälligen Gewichten",
            "Vortraining braucht Labels", "Es gibt keinen Unterschied",
            why = "Vortraining ist selbstüberwacht auf riesigen Textmengen.")
        mc(2, "Warum nimmt man beim Fine-Tuning sehr kleine Lernraten wie 2·10⁻⁵?",
            "Damit das vortrainierte Wissen nicht durch große Schritte zerstört wird (katastrophales Vergessen)", "Weil Adam sonst nicht funktioniert", "Weil die Daten klein sind", "Aus Gewohnheit",
            why = "Die Gewichte liegen schon in einem guten Bereich; man will nur nachjustieren.")
        code(2, "Was macht dieser Aufruf?", """
            args = TrainingArguments(output_dir="out", learning_rate=2e-5, num_train_epochs=2, eval_strategy="epoch")
            trainer = Trainer(model=model, args=args, train_dataset=train, eval_dataset=test, compute_metrics=compute_metrics)
            trainer.train()
        """, "Er trainiert zwei Epochen mit Lernrate 2·10⁻⁵ und bewertet nach jeder Epoche", "Er trainiert nur auf den Testdaten", "Er lädt ein neues Modell", "Er speichert nur die Daten",
            why = "Der Trainer kümmert sich um Batches, Gerät, Plan, Checkpoints und Logging.")
        mc(2, "Was bekommt compute_metrics übergeben?",
            "Die Vorhersagen (Logits) und die Labels der Evaluationsmenge", "Das Modell und den Optimierer", "Nur den Verlust", "Die Rohtexte",
            why = "Man rechnet z. B. accuracy aus argmax(logits) und labels.")
        mc(2, "Warum gibt das Modell einen Verlust zurück, wenn man labels übergibt?",
            "Die Aufgabenklassen berechnen die passende Kreuzentropie intern", "Weil labels die Gewichte sind", "Das passiert nie", "Weil der Tokenizer ihn berechnet",
            why = "out.loss kann direkt mit backward() benutzt werden.")
        line(2, "Welche Zeile in der eigenen Schleife gehört direkt nach out.loss.backward()?",
            "opt.step(); sched.step(); opt.zero_grad()", "model.eval()", "loader.reset()", "out.loss.zero_()",
            why = "Gewichte aktualisieren, Lernrate weiterschalten, Gradienten löschen.")
        mc(2, "Was unterscheidet AdamW von Adam mit L2-Strafe?",
            "Weight Decay wird direkt auf die Gewichte angewandt, entkoppelt vom adaptiven Schritt", "AdamW hat kein Momentum", "AdamW braucht keine Lernrate", "Es gibt keinen Unterschied",
            why = "Bei Adam + L2 würde die Strafe durch √v̂ mitskaliert und wirkt ungleichmäßig.")
        mc(1, "Was bedeutet die Warnung „Some weights … are newly initialized“ beim Laden?",
            "Der Klassifikationskopf ist neu und muss trainiert werden – beim Fine-Tuning normal", "Das Modell ist defekt", "Der Tokenizer fehlt", "Die GPU ist zu klein",
            why = "Das vortrainierte Basismodell hatte keinen Kopf für deine Klassenzahl.")

        on("ft_peft")
        mc(1, "Was ist die Kernidee von PEFT?",
            "Das Basismodell einfrieren und nur sehr wenige zusätzliche oder ausgewählte Parameter trainieren", "Das Modell neu von null trainieren", "Nur die Daten verändern", "Das Modell löschen",
            why = "Oft unter 1 % trainierbare Parameter bei fast gleicher Qualität.")
        mc(2, "Warum spart PEFT so viel Speicher?",
            "Für eingefrorene Gewichte braucht man weder Gradienten noch Optimiererzustände", "Weil das Modell kleiner wird", "Weil keine Aktivierungen nötig sind", "Weil die Daten komprimiert werden",
            why = "Adam speichert sonst zwei Momente je Parameter – bei Milliarden Parametern gigantisch.")
        mc(2, "Wie viele Parameter hat ein Adapter mit d = 768 und r = 64 ungefähr?",
            "≈ 99 000", "≈ 768", "≈ 590 000", "≈ 49 000",
            why = "2 · 768 · 64 + 64 + 768 = 99 136.")
        code(2, "Warum wird up mit null initialisiert?", """
            nn.init.zeros_(self.up.weight); nn.init.zeros_(self.up.bias)
            return h + self.up(torch.relu(self.down(h)))
        """, "Der Adapter startet als Identität und verändert das vortrainierte Modell anfangs nicht", "Um Speicher zu sparen", "Damit keine Gradienten fließen", "Weil ReLU sonst negativ wird",
            why = "Training beginnt exakt beim Verhalten des Basismodells.")
        mc(2, "Was ist ein Soft Prompt?",
            "Eine Folge lernbarer Einbettungsvektoren (virtuelle Tokens) vor der Eingabe", "Ein höflich formulierter Prompt", "Ein Prompt in Kleinbuchstaben", "Ein Prompt ohne Satzzeichen",
            why = "Nur k · d Werte werden trainiert; sie entsprechen keinen echten Wörtern.")
        mc(3, "Was unterscheidet Prefix Tuning von Prompt Tuning?",
            "Prefix Tuning lernt Präfixe für Keys und Values in jeder Schicht, Prompt Tuning nur an der Eingabe", "Prefix Tuning ändert alle Gewichte", "Es gibt keinen Unterschied", "Prompt Tuning nutzt Adapter",
            why = "Mehr Eingriffspunkte, mehr Ausdruckskraft bei kleineren Modellen.")
        codeOut(2, "Wie viele Parameter trainiert Prompt Tuning mit 20 virtuellen Tokens bei d = 4096?", """
            cfg = PromptTuningConfig(task_type=TaskType.CAUSAL_LM, num_virtual_tokens=20)
            # Modellbreite d = 4096
        """, "81 920", "20", "4096", "83 886 080",
            why = "20 · 4096 = 81 920.")
        mc(2, "Welcher Vorteil ergibt sich beim Betrieb vieler Aufgaben?",
            "Ein Basismodell kann viele kleine Aufgaben-Adapter (wenige MB) bedienen", "Jede Aufgabe braucht eine eigene GPU", "Adapter machen das Modell langsamer", "Keiner",
            why = "Statt n vollständiger Kopien nur ein Modell plus n Adapter.")

        on("ft_lora")
        mc(1, "Wie stellt LoRA die Gewichtsänderung dar?",
            "Als Produkt zweier schmaler Matrizen ΔW = BA mit kleinem Rang r", "Als Kopie von W", "Als Diagonalmatrix", "Als zufälliges Rauschen",
            why = "h = W₀x + (α/r)BAx; W₀ bleibt eingefroren.")
        mc(2, "Wie viele Parameter hat LoRA mit r = 8 für eine 4096 × 4096-Matrix?",
            "65 536", "16 777 216", "8 192", "32 768",
            why = "r(d + k) = 8 · 8192 = 65 536 – Faktor 256 weniger als voll.")
        mc(2, "Warum wird B mit null initialisiert?",
            "Dann ist ΔW = 0 zu Beginn und das Modell startet exakt im vortrainierten Zustand", "Damit A nicht trainiert wird", "Um Speicher zu sparen", "Weil B nie trainiert wird",
            why = "A ist zufällig, damit überhaupt Gradienten für B entstehen.")
        mc(2, "Was bewirkt merge_and_unload()?",
            "Es rechnet BA in W₀ ein; das Modell hat danach keine zusätzliche Latenz", "Es löscht die LoRA-Gewichte", "Es lädt ein neues Basismodell", "Es quantisiert das Modell",
            why = "W = W₀ + (α/r)BA – danach ist es ein gewöhnliches Modell.")
        line(2, "Welche Konfiguration setzt LoRA mit Rang 8 auf die Query- und Value-Projektionen von DistilBERT?",
            "LoraConfig(r=8, lora_alpha=16, target_modules=[\"q_lin\", \"v_lin\"], task_type=TaskType.SEQ_CLS)",
            "LoraConfig(rank=8, layers=\"all\")",
            "LoraConfig(r=8, target_modules=[\"q_proj\", \"v_proj\"], task_type=\"IMAGE\")",
            "LoraConfig(alpha=8)",
            why = "Die Modulnamen hängen von der Architektur ab (DistilBERT: q_lin, v_lin; Llama: q_proj, v_proj …).")
        mc(3, "Welche Rolle spielt α/r?",
            "Er skaliert das Update, sodass man r ändern kann, ohne die Lernrate neu einstellen zu müssen", "Er ist die Lernrate", "Er bestimmt die Zahl der Schichten", "Er ist die Dropout-Rate",
            why = "Größeres α verstärkt den Einfluss des Adapters.")
        mc(2, "Was ist NF4 in QLoRA?",
            "Ein 4-Bit-Datentyp, optimal für normalverteilte Gewichte", "Eine Netzwerkfunktion", "Ein Optimierer", "Eine 4-fache Lernrate",
            why = "NormalFloat-4 verteilt die 16 Quantisierungsstufen nach den Quantilen einer Normalverteilung.")
        code(3, "Welche Rolle spielt bnb_4bit_compute_dtype hier?", """
            BitsAndBytesConfig(load_in_4bit=True, bnb_4bit_quant_type="nf4", bnb_4bit_compute_dtype=torch.bfloat16)
        """, "Gespeichert wird in 4 Bit, gerechnet wird nach blockweiser Dequantisierung in bf16", "Alles wird in 4 Bit gerechnet", "Die LoRA-Matrizen werden 4-bittig", "Es schaltet die Quantisierung ab",
            why = "Speicher wie 4 Bit, Rechengenauigkeit wie bf16 – der Kern von QLoRA.")
    }
}

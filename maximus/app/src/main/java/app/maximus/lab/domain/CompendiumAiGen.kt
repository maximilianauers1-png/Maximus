package app.maximus.lab.domain

/** Generative AI in practice (prompting, embeddings, frameworks, fine-tuning, evaluation) and AI engineering for production. */
internal object CompendiumAiGen {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "Generative KI und LLMs",
        chapter(
            "gen_prompting", T, 2, "Prompt Engineering",
            "Wie ein Sprachmodell einen Prompt liest, Rolle und Kontext, Zero- und Few-Shot, Schritt-für-Schritt-Denken, strukturierte Ausgaben, Systemprompts, typische Fehler.",
            emptyList(),
            sec("Was ein Sprachmodell eigentlich tut", """
                Ein LLM setzt Text fort: Es berechnet für das nächste Token eine Wahrscheinlichkeitsverteilung und wählt daraus. Alles, was im Prompt steht, ist Kontext, der diese Verteilung verschiebt. Ein guter Prompt macht die gewünschte Antwort zur wahrscheinlichsten Fortsetzung. Das Modell hat kein Gedächtnis zwischen Gesprächen – nur das, was im aktuellen Kontextfenster steht.
            """,
                fm("Autoregressive Erzeugung", "P(x₁…x_T) = Πₜ P(xₜ | x₁…xₜ₋₁)")
            ),
            sec("Bausteine eines guten Prompts", """
                • Rolle: „Du bist ein erfahrener Datenwissenschaftler und erklärst für Physiker.“
                • Aufgabe: konkret und messbar – „Erkläre in fünf Sätzen …“ statt „Erzähl was über …“.
                • Kontext: Daten, Dokumente, Zielgruppe, Einschränkungen.
                • Format: „Antworte als JSON mit den Feldern …“, Tabelle, Stichpunkte.
                • Beispiele (Few-Shot): zwei, drei Eingabe-Ausgabe-Paare zeigen das gewünschte Muster.
                • Grenzen: „Wenn die Antwort nicht im Text steht, sag ‚weiß ich nicht‘.“
            """),
            sec("Zero-Shot, Few-Shot, Chain of Thought", """
                • Zero-Shot: nur die Anweisung. Reicht bei einfachen Aufgaben.
                • Few-Shot: einige Beispiele im Prompt – das Modell lernt das Muster „im Kontext“ (in-context learning), ohne dass Gewichte geändert werden.
                • Chain of Thought: „Denke Schritt für Schritt“ oder ein Beispiel mit ausgeschriebenem Lösungsweg. Bei Rechen- und Logikaufgaben deutlich besser, weil das Modell Zwischenschritte als Text „zum Denken“ nutzt.
                • Selbstkonsistenz: mehrere Lösungswege erzeugen und die häufigste Antwort nehmen.
                • ReAct: Denken und Handeln (Werkzeugaufrufe) abwechseln – die Basis von Agenten.
            """),
            sec("Temperatur und andere Regler", """
                • Temperatur T: teilt die Logits vor dem Softmax. T → 0: immer das wahrscheinlichste Token (deterministisch, gut für Fakten und Code). T ≈ 1: kreativer, vielfältiger.
                • Top-p (Nucleus): nur aus den Token wählen, die zusammen Wahrscheinlichkeit p ausmachen.
                • Maximale Länge, Stoppsequenzen.
            """,
                fm("Temperatur-Softmax", "pᵢ = e^{zᵢ/T} / Σⱼ e^{zⱼ/T}")
            ),
            sec("Typische Fehler", """
                • Vage Aufgaben („mach es besser“).
                • Widersprüchliche Anweisungen.
                • Zu viel irrelevanter Kontext – das Modell verliert den Faden („lost in the middle“).
                • Annahme, das Modell kenne aktuelle oder private Fakten – dafür braucht es RAG oder Werkzeuge.
                • Ergebnisse nicht prüfen: LLMs können überzeugend falsch sein (Halluzinationen).
            """)
        ),
        chapter(
            "gen_vector", T, 2, "Embeddings und Vektordatenbanken",
            "Bedeutung als Vektor, Kosinusähnlichkeit, Satz-Embeddings, semantische Suche, approximative Nachbarsuche (HNSW, IVF), Chunking, hybride Suche.",
            emptyList(),
            sec("Bedeutung als Richtung im Raum", """
                Ein Embedding-Modell bildet einen Text (Wort, Satz, Absatz) auf einen Vektor mit einigen hundert bis tausend Zahlen ab. Texte mit ähnlicher Bedeutung landen nah beieinander, auch wenn sie keine Wörter teilen: „Kniebeuge“ und „Squat“ liegen nah. Gemessen wird meist mit der Kosinusähnlichkeit. Berühmt: König − Mann + Frau ≈ Königin – Bedeutungsbeziehungen als Vektordifferenzen.
            """,
                fm("Kosinusähnlichkeit", "cos(a, b) = a·b / (|a||b|)"),
                fm("Für normierte Vektoren", "|a − b|² = 2 − 2 cos(a, b)")
            ),
            sec("Semantische Suche", """
                • Alle Dokumente in Abschnitte (Chunks) zerlegen und einbetten.
                • Die Frage mit demselben Modell einbetten.
                • Die k ähnlichsten Chunks suchen.
                • Optional: Ergebnisse mit einem genaueren Cross-Encoder neu sortieren (Reranking).

                $ from sentence_transformers import SentenceTransformer
                $ model = SentenceTransformer("all-MiniLM-L6-v2")
                $ doc_vecs = model.encode(chunks, normalize_embeddings=True)
                $ q = model.encode(["Wie peake ich für einen Wettkampf?"], normalize_embeddings=True)
                $ scores = doc_vecs @ q.T                # Kosinus, da normiert
            """),
            sec("Vektordatenbanken", """
                Bei Millionen Vektoren ist exakte Suche (Abstand zu allen) zu langsam. Approximative Nachbarsuche (ANN) tauscht ein wenig Genauigkeit gegen enorme Geschwindigkeit:

                • HNSW: ein mehrschichtiger Nachbarschaftsgraph, durch den man sich vom Groben ins Feine zum Ziel hangelt.
                • IVF: Vektoren in Cluster einteilen (k-Means!) und nur in den nächstgelegenen Clustern suchen.
                • Produktquantisierung: Vektoren komprimieren.
                • Systeme: FAISS, Chroma, Milvus, Qdrant, Weaviate, pgvector, Elasticsearch.
            """),
            sec("Chunking und hybride Suche", """
                • Chunk-Größe: zu klein → Zusammenhang fehlt; zu groß → unscharfe Treffer. Typisch 200–800 Token mit etwas Überlappung, an Absätzen und Überschriften geschnitten.
                • Metadaten (Quelle, Datum, Kapitel) mitspeichern und filtern.
                • Hybride Suche: Embeddings verstehen Bedeutung, Stichwortsuche (BM25) findet exakte Begriffe, Namen und Zahlen zuverlässiger – beide kombinieren.
            """)
        ),
        chapter(
            "gen_frameworks", T, 2, "LLM-Anwendungen bauen: LangChain, Ketten, Werkzeuge",
            "Bausteine einer LLM-App, Prompt-Vorlagen, Ketten, Gesprächsgedächtnis, Werkzeuge und Funktionsaufrufe, RAG-Kette in Code, Agenten-Frameworks.",
            emptyList(),
            sec("Die Bausteine", """
                Eine LLM-Anwendung ist mehr als ein Modell: Prompt-Vorlagen, Datenquellen, Retriever, Speicher für das Gespräch, Werkzeuge (Rechner, Suche, Datenbank, Code) und Logik, die alles verbindet. Frameworks wie LangChain, LlamaIndex oder Haystack liefern diese Bausteine fertig; der IBM-Kurs nutzt LangChain mit watsonx-Modellen.
            """),
            sec("Eine RAG-Kette in LangChain", """
                $ from langchain_text_splitters import RecursiveCharacterTextSplitter
                $ from langchain_community.vectorstores import Chroma
                $ chunks = RecursiveCharacterTextSplitter(chunk_size=500, chunk_overlap=50).split_documents(docs)
                $ store = Chroma.from_documents(chunks, embeddings)
                $ retriever = store.as_retriever(search_kwargs={"k": 4})
                $ prompt = ChatPromptTemplate.from_template(
                $     "Beantworte nur mit dem Kontext.\nKontext: {context}\nFrage: {question}")
                $ chain = {"context": retriever, "question": RunnablePassthrough()} | prompt | llm | StrOutputParser()
                $ print(chain.invoke("Was ist INOL?"))

                Die Pipe-Schreibweise (|) verbindet Schritte: Retriever → Prompt → Modell → Textausgabe.
            """),
            sec("Gedächtnis", """
                Das Modell selbst ist zustandslos. Gesprächsgedächtnis heißt: bisherige Nachrichten wieder in den Prompt packen – vollständig, als laufende Zusammenfassung oder als durchsuchbarer Vektorspeicher für sehr lange Verläufe. Das Kontextfenster ist begrenzt und Token kosten Zeit und Geld; deshalb kürzt und fasst man zusammen.
            """),
            sec("Werkzeuge und Agenten", """
                Bei Funktionsaufrufen beschreibt man Werkzeuge mit Name, Zweck und Parametern (JSON-Schema). Das Modell antwortet dann nicht mit Text, sondern mit „rufe Werkzeug X mit Argumenten Y“. Die Anwendung führt es aus und gibt das Ergebnis zurück. Ein Agent wiederholt diese Schleife (denken → Werkzeug → beobachten), bis die Aufgabe gelöst ist. Frameworks: LangGraph, CrewAI, AutoGen; das Model Context Protocol (MCP) standardisiert, wie Werkzeuge an Modelle angeschlossen werden.

                > Werkzeuge machen LLMs verlässlich bei Dingen, die sie schlecht können: rechnen, aktuelle Fakten, Datenbanken. Maximus in dieser App nutzt genau das (/1rm, /rechne, /formel).
            """)
        ),
        chapter(
            "gen_finetune", T, 3, "Feinabstimmung in der Praxis",
            "Wann Prompting, wann RAG, wann Fine-Tuning; Datensätze bauen, Instruction-Tuning, LoRA und QLoRA, Hyperparameter, Hugging Face Trainer, Überprüfung.",
            emptyList(),
            sec("Prompting, RAG oder Fine-Tuning?", """
                • Prompting: zuerst immer. Kostet nichts, sofort änderbar.
                • RAG: wenn das Modell Wissen braucht, das es nicht hat oder das sich ändert (Firmendokumente, deine Trainingsdaten).
                • Fine-Tuning: wenn sich Verhalten, Stil oder Format ändern soll, eine Spezialaufgabe sehr zuverlässig werden muss, oder ein kleines Modell eine Aufgabe eines großen übernehmen soll.

                > Faustregel: RAG für Wissen, Fine-Tuning für Verhalten.
            """),
            sec("Daten sind alles", """
                Ein Instruction-Datensatz besteht aus Paaren (Anweisung, ideale Antwort), oft im Chat-Format. Wenige hundert bis einige tausend sehr gute Beispiele schlagen hunderttausend mittelmäßige. Vielfalt, Korrektheit und einheitlicher Stil zählen. Eine Testmenge, die nicht im Training vorkommt, ist Pflicht.
            """),
            sec("LoRA und QLoRA", """
                Alle Milliarden Gewichte zu trainieren ist teuer. LoRA (Low-Rank Adaptation) friert das Modell ein und lernt nur eine kleine Korrektur ΔW = B·A mit niedrigem Rang r (z. B. 8–64). Das spart über 99 % der trainierbaren Parameter und viel Speicher; der Adapter ist wenige Megabyte groß und kann getauscht werden. QLoRA lädt das eingefrorene Modell zusätzlich 4-bit-quantisiert – so passt die Feinabstimmung eines 7B-Modells auf eine einzelne Grafikkarte.
            """,
                fm("LoRA", "W′ = W + (α/r)·BA,  B ∈ ℝ^{d×r}, A ∈ ℝ^{r×k}"),
                fm("Trainierbare Parameter", "P_LoRA = r(d + k)  statt  d·k")
            ),
            sec("Im Code", """
                $ from peft import LoraConfig, get_peft_model
                $ config = LoraConfig(r=16, lora_alpha=32, target_modules=["q_proj", "v_proj"], lora_dropout=0.05)
                $ model = get_peft_model(base_model, config)
                $ model.print_trainable_parameters()     # z. B. 0,1 % der Gewichte

                Typische Fehler: zu hohe Lernrate (das Modell „vergisst“ Allgemeinwissen – katastrophales Vergessen), zu viele Epochen (auswendig lernen), Trainings- und Testdaten vermischt.
            """)
        ),
        chapter(
            "gen_eval", T, 3, "LLM-Anwendungen bewerten und absichern",
            "Testsätze, automatische Metriken, LLM als Richter, RAG-Metriken (Treue, Relevanz), Halluzinationen, Prompt-Injection, Guardrails, Datenschutz.",
            emptyList(),
            sec("Ohne Tests keine Verbesserung", """
                Man baut früh einen festen Testsatz echter Fragen mit erwarteten Antworten oder Bewertungskriterien. Jede Änderung (Prompt, Modell, Chunking) wird darauf gemessen. Sonst verbessert man eine Frage und verschlechtert zehn andere, ohne es zu merken.

                • Exakte Übereinstimmung, wo es eine richtige Antwort gibt (Zahlen, Klassen).
                • Textähnlichkeit (BLEU, ROUGE) – grob, eher für Übersetzung und Zusammenfassung.
                • LLM als Richter: ein starkes Modell bewertet Antworten nach einer Checkliste – günstig, aber stichprobenartig von Menschen prüfen.
                • Menschliche Bewertung für das Wichtigste.
            """),
            sec("RAG gezielt messen", """
                • Retrieval: Ist der richtige Abschnitt unter den Top-k? (Recall@k)
                • Treue (Faithfulness): Steht alles in der Antwort auch in den gefundenen Quellen?
                • Antwortrelevanz: Beantwortet sie die Frage?
                • Ist die Antwort schlecht, zuerst das Retrieval prüfen – die meisten Fehler entstehen dort.
            """,
                fm("Recall@k", "R@k = (relevante Treffer unter Top-k) / (alle relevanten)")
            ),
            sec("Halluzinationen", """
                LLMs erzeugen plausiblen Text, nicht geprüfte Wahrheit. Gegenmittel: Quellen mitliefern (RAG) und zitieren lassen, „ich weiß es nicht“ erlauben, niedrige Temperatur für Fakten, Werkzeuge für Rechnungen, kritische Ausgaben automatisch gegenprüfen.
            """),
            sec("Sicherheit", """
                • Prompt-Injection: Text in Dokumenten oder Webseiten, der dem Modell neue Befehle gibt („Ignoriere alle Anweisungen und …“). Daten nie als Anweisung behandeln, Werkzeugrechte minimal halten, gefährliche Aktionen bestätigen lassen.
                • Datenlecks: keine Geheimnisse in Prompts, die nach außen gehen; personenbezogene Daten minimieren – lokale Modelle wie Maximus vermeiden das Problem ganz.
                • Guardrails: Ein- und Ausgaben filtern und auf Format prüfen.
            """)
        )
    ) + course(
        "AI Engineering: vom Notebook zur Anwendung",
        chapter(
            "eng_deploy", T, 2, "Modelle bereitstellen",
            "Modell speichern (joblib, ONNX, SavedModel), REST-API mit Flask/FastAPI, Gradio-Demos, Docker-Container, Batch- gegen Echtzeit-Vorhersage, On-Device.",
            emptyList(),
            sec("Vom Notebook zum Dienst", """
                Ein Modell im Notebook nützt niemandem. Bereitstellen heißt: das trainierte Modell speichern, in eine Anwendung einbetten, die Anfragen annimmt, und das zuverlässig, schnell und überwacht. Zuerst klären: Brauche ich Vorhersagen in Echtzeit (eine Anfrage, Antwort in Millisekunden) oder als Stapel (jede Nacht alle Kunden bewerten)?
            """),
            sec("Speichern und laden", """
                • scikit-learn: joblib.dump(model, "model.joblib") – die ganze Pipeline inklusive Vorverarbeitung speichern!
                • Keras: model.save("model.keras"); TensorFlow SavedModel; TFLite fürs Handy.
                • PyTorch: state_dict oder TorchScript.
                • ONNX: neutrales Format, läuft in vielen Laufzeitumgebungen (auch auf Android).
            """),
            sec("Eine Vorhersage-API mit FastAPI", """
                $ from fastapi import FastAPI
                $ from pydantic import BaseModel
                $ import joblib
                $ app = FastAPI()
                $ model = joblib.load("model.joblib")
                $ class Haus(BaseModel):
                $     flaeche: float
                $     zimmer: int
                $ @app.post("/predict")
                $ def predict(h: Haus):
                $     return {"preis": float(model.predict([[h.flaeche, h.zimmer]])[0])}

                Start mit „uvicorn main:app“. Für eine schnelle Demo-Oberfläche: Gradio oder Streamlit, beides mit wenigen Zeilen.
            """),
            sec("Container und Hardware", """
                Docker packt Code, Bibliotheken und Modell in ein Image, das überall gleich läuft – Schluss mit „bei mir funktioniert es“. Skaliert wird mit Kubernetes oder Cloud-Diensten (IBM watsonx, AWS SageMaker, Google Vertex AI). Für LLMs gibt es spezialisierte Server (vLLM, TGI, Ollama). On-Device (wie Maximus) spart Server, Latenz und schützt Daten, braucht aber kleine, quantisierte Modelle.
            """)
        ),
        chapter(
            "eng_spark", T, 2, "Big Data mit Apache Spark",
            "Warum verteilt rechnen, Spark-Architektur (Driver, Executor), RDD und DataFrame, Lazy Evaluation, Spark SQL, Spark MLlib-Pipelines, wann sich Spark lohnt.",
            emptyList(),
            sec("Wenn die Daten nicht mehr auf einen Rechner passen", """
                pandas lädt alles in den Arbeitsspeicher eines Rechners. Bei Terabytes geht das nicht mehr. Apache Spark verteilt Daten und Rechnungen auf viele Rechner (einen Cluster). Ein Driver-Programm plant die Arbeit, viele Executors erledigen sie parallel auf ihren Datenstücken (Partitionen). Spark hält Zwischenergebnisse im Speicher und ist deshalb viel schneller als das ältere Hadoop MapReduce.
            """),
            sec("RDD, DataFrame und Lazy Evaluation", """
                • RDD (Resilient Distributed Dataset): die verteilte Grunddatenstruktur; „resilient“, weil verlorene Teile aus ihrer Entstehungsgeschichte neu berechnet werden können.
                • DataFrame: verteilte Tabelle mit Spalten, ähnlich pandas, mit Optimierer – heute der Standard.
                • Transformationen (filter, select, groupBy) werden nur geplant. Erst eine Aktion (count, show, write) löst die Berechnung aus. Spark optimiert so den ganzen Plan auf einmal.

                $ from pyspark.sql import SparkSession
                $ spark = SparkSession.builder.appName("demo").getOrCreate()
                $ df = spark.read.csv("transaktionen.csv", header=True, inferSchema=True)
                $ df.filter(df.betrag > 1000).groupBy("land").count().show()
            """),
            sec("Spark MLlib", """
                MLlib bietet verteilte Versionen der klassischen Verfahren (Regression, Bäume, Random Forest, Gradient Boosting, k-Means, ALS für Empfehlungen) und dasselbe Pipeline-Konzept wie scikit-learn:

                $ from pyspark.ml import Pipeline
                $ from pyspark.ml.feature import VectorAssembler, StandardScaler
                $ from pyspark.ml.classification import LogisticRegression
                $ assembler = VectorAssembler(inputCols=["alter", "einkommen"], outputCol="raw")
                $ scaler = StandardScaler(inputCol="raw", outputCol="features")
                $ lr = LogisticRegression(featuresCol="features", labelCol="label")
                $ model = Pipeline(stages=[assembler, scaler, lr]).fit(train_df)
                $ preds = model.transform(test_df)

                Merkmale müssen in einer einzigen Vektorspalte stehen – dafür ist der VectorAssembler da.
            """),
            sec("Wann lohnt sich Spark?", """
                • Daten passen in den Speicher eines Rechners → pandas/scikit-learn sind einfacher und oft schneller.
                • Daten verteilt in Data Lakes, Terabytes, regelmäßige große ETL-Jobs → Spark.
                • Deep Learning auf großen Daten: Spark für Vorverarbeitung, Training mit PyTorch/TensorFlow verteilt.
            """)
        ),
        chapter(
            "eng_mlops", T, 2, "MLOps: Modelle im Dauerbetrieb",
            "Versionierung von Code, Daten und Modellen, Experiment-Tracking, automatisierte Pipelines und CI/CD, Monitoring, Daten- und Konzeptdrift, Neutraining, Rollback.",
            emptyList(),
            sec("Warum MLOps?", """
                Software ändert sich, wenn jemand den Code ändert. Ein ML-System ändert sich auch, wenn sich die Welt ändert – die Daten. MLOps überträgt DevOps-Praktiken auf ML: alles reproduzierbar, automatisiert, überwacht.
            """),
            sec("Reproduzierbarkeit", """
                • Code mit Git versionieren.
                • Daten und Modelle versionieren (DVC, Modell-Registry).
                • Experimente protokollieren: Parameter, Metriken, Datenversion, Artefakte (MLflow, Weights & Biases).
                • Zufallszahlen fixieren, Umgebung im Container.

                $ import mlflow
                $ with mlflow.start_run():
                $     mlflow.log_param("C", 0.1)
                $     mlflow.log_metric("f1", f1)
                $     mlflow.sklearn.log_model(model, "model")
            """),
            sec("Drift und Monitoring", """
                • Datendrift: Die Eingaben verändern sich (neue Kundengruppen, neuer Sensor).
                • Konzeptdrift: Der Zusammenhang zwischen Eingabe und Ziel ändert sich (Betrüger lernen dazu, Pandemie verändert Kaufverhalten).
                • Überwachen: Verteilungen der Eingaben, Vorhersagen, Latenz, Fehlerraten und – sobald Labels nachkommen – die echte Güte.
                • Reaktion: Alarm, Neutraining auf aktuellen Daten, neue Version zuerst im Schattenbetrieb oder für einen kleinen Teil der Nutzer (Canary), Rollback bei Problemen.
            """,
                fm("Population Stability Index", "PSI = Σᵢ (aᵢ − eᵢ) ln(aᵢ/eᵢ)")
            ),
            sec("Die automatisierte Pipeline", """
                • Daten einlesen und prüfen (Schema, fehlende Werte, Ausreißer)
                • Merkmale berechnen
                • Trainieren und bewerten
                • Vergleich mit dem laufenden Modell – nur besser darf live
                • Bereitstellen und überwachen

                Werkzeuge: Airflow, Kubeflow, GitHub Actions, Cloud-Pipelines.
            """)
        ),
        chapter(
            "eng_capstone", T, 2, "Dein Abschlussprojekt: ein ML-Projekt von A bis Z",
            "Ein vollständiges Projekt im Stil des IBM-Capstone: Fragestellung, Daten, EDA, Basislinie, Modelle, Bewertung, Bericht, Demo – mit Checkliste und Projektideen.",
            emptyList(),
            sec("Projektidee mit Bezug zu dir", """
                • Strongman: e1RM-Prognose aus deinem Trainingslog (Regression), Erkennen von Übertrainingsphasen (Anomalie), Trainingstage clustern.
                • Physik: Phasenübergänge in Simulationsdaten klassifizieren, Materialeigenschaften elektrokalorischer Polymere vorhersagen, Rauschen in Messdaten mit Autoencoder entfernen.
                • Klassiker aus dem Kurs: Wetter- oder Kreditrisiko-Klassifikation, Bilder klassifizieren mit Transfer Learning, ein RAG-Assistent für die eigene Doktorarbeit.
            """),
            sec("Die Checkliste", """
                • Frage und Erfolgsmetrik schriftlich festhalten.
                • Daten beschaffen, beschreiben, Lizenz und Datenschutz prüfen.
                • EDA: Verteilungen, Korrelationen, fehlende Werte, Auffälligkeiten.
                • Aufteilen (zeitlich bei Zeitreihen!), dann Pipeline mit Vorverarbeitung.
                • Basislinie messen.
                • Mindestens drei Modelle vergleichen (linear, Baum-Ensemble, ggf. Netz) mit Kreuzvalidierung.
                • Hyperparameter suchen, bestes Modell einmal auf dem Testset prüfen.
                • Fehler analysieren: Wo liegt das Modell daneben, und warum?
                • Erklärbarkeit: wichtigste Merkmale.
                • Bericht oder Notebook mit Geschichte: Frage → Daten → Ergebnis → Grenzen.
                • Demo: kleine API oder Gradio-App.
            """),
            sec("Bewertungskriterien wie im Kurs", """
                Ein Prüfer schaut weniger auf die letzte Nachkommastelle der Accuracy als darauf, ob du sauber gearbeitet hast: kein Datenleck, passende Metrik, ehrlicher Vergleich mit Basislinie, nachvollziehbare Begründungen, klare Visualisierungen und eine ehrliche Diskussion der Grenzen.

                > Ein einfaches Modell, das du vollständig erklären kannst, ist mehr wert als ein kompliziertes, das du nicht verstehst.
            """)
        )
    )
}

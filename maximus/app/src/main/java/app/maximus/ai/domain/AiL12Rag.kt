package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 12 (IBM „Fundamentals of AI Agents Using RAG and LangChain“): dense passage retrieval with
 * FAISS, a RAG pipeline with Hugging Face and PyTorch, LangChain core concepts and LCEL, memory, tools
 * and agents.
 */
internal object AiL12Rag {
    val bites: List<Bite> = listOf(
        bite(
            "rag_dpr", 2, "RAG-Bausteine: DPR-Encoder, FAISS und Ähnlichkeitssuche",
            "Wie man zu einer Frage die passenden Textstellen findet: zwei Encoder für Fragen und Passagen, Skalarprodukt-Suche, FAISS-Indizes und ihre Kompromisse.",
            goals(
                "Die Idee dichter Retrieval-Modelle (DPR) mit getrennten Encodern erklären",
                "Ähnlichkeit per Skalarprodukt und Kosinus berechnen",
                "Einen FAISS-Index bauen und durchsuchen"
            ),
            sec("Warum Retrieval?", """
                Ein Sprachmodell kennt nur, was in seinen Trainingsdaten stand – nichts über deine Firmendokumente, nichts über Ereignisse nach dem Stichtag. Es halluziniert, wenn es raten muss. Retrieval-Augmented Generation (RAG; Lewis et al. 2020) holt deshalb zur Frage passende Textstellen aus einer Wissensbasis und gibt sie dem Generator als Kontext mit. Das Modell zitiert dann aus Quellen statt aus seinem Gedächtnis.
            """),
            sec("Dense Passage Retrieval", """
                DPR (Karpukhin et al. 2020) nutzt zwei BERT-Encoder: einen für Fragen, einen für Passagen. Beide bilden auf Vektoren derselben Dimension (768) ab; die Relevanz ist ihr Skalarprodukt. Trainiert wird kontrastiv: Die richtige Passage soll ein höheres Skalarprodukt haben als andere Passagen im Batch (In-Batch-Negatives). Weil die Passagen unabhängig von der Frage kodiert werden, kann man alle vorab einbetten und speichern.

                $ from transformers import DPRQuestionEncoder, DPRQuestionEncoderTokenizer, DPRContextEncoder, DPRContextEncoderTokenizer
                $ ctx_tok = DPRContextEncoderTokenizer.from_pretrained("facebook/dpr-ctx_encoder-single-nq-base")
                $ ctx_enc = DPRContextEncoder.from_pretrained("facebook/dpr-ctx_encoder-single-nq-base")
                $ q_tok = DPRQuestionEncoderTokenizer.from_pretrained("facebook/dpr-question_encoder-single-nq-base")
                $ q_enc = DPRQuestionEncoder.from_pretrained("facebook/dpr-question_encoder-single-nq-base")
                $ P = ctx_enc(**ctx_tok(passages, padding=True, truncation=True, return_tensors="pt")).pooler_output   # (N, 768)
                $ q = q_enc(**q_tok("Wer erfand den Buchdruck?", return_tensors="pt")).pooler_output                   # (1, 768)
            """,
                fm("Relevanz", "sim(q, p) = E_Q(q)ᵀ E_P(p)"),
                fm("Kontrastiver Verlust", "L = −ln( e^{sim(q, p⁺)} / Σⱼ e^{sim(q, pⱼ)} )")
            ),
            sec("FAISS", """
                FAISS (Facebook AI Similarity Search) findet zu einem Vektor die k nächsten Nachbarn unter Millionen Vektoren. IndexFlatIP rechnet exakt alle Skalarprodukte (O(N·d) pro Anfrage); approximative Indizes wie IVF (Clustern in Zellen) oder HNSW (Navigationsgraph) sind um Größenordnungen schneller, auf Kosten eines kleinen Recall-Verlusts.

                $ import faiss, numpy as np
                $ index = faiss.IndexFlatIP(768)
                $ index.add(P.detach().numpy().astype("float32"))
                $ D, I = index.search(q.detach().numpy().astype("float32"), k=3)
                $ print([passages[i] for i in I[0]])     # die drei relevantesten Passagen

                Für Kosinusähnlichkeit normiert man alle Vektoren vorher auf Länge 1 (faiss.normalize_L2) – dann ist das Skalarprodukt der Kosinus.
            """,
                fm("Kosinus", "cos(q, p) = qᵀp / (‖q‖‖p‖)"),
                fm("Exakte Suche", "Aufwand O(N · d) je Anfrage")
            ),
            sec("Dicht oder dünn?", """
                • Dünnes Retrieval (BM25, TF-IDF): exakte Wortübereinstimmung, gut für Namen, Codes, seltene Begriffe.
                • Dichtes Retrieval (DPR, Sentence-Transformers): Bedeutungsähnlichkeit, findet Paraphrasen.
                • Hybrid: beide Ranglisten kombinieren (z. B. Reciprocal Rank Fusion) – in der Praxis oft am besten.
                • Re-Ranking: Ein Cross-Encoder bewertet die Top-k-Treffer genauer, indem er Frage und Passage gemeinsam liest.
            """,
                fm("Reciprocal Rank Fusion", "RRF(d) = Σ_r 1/(k + rank_r(d)),  k ≈ 60")
            )
        ),
        bite(
            "rag_pipeline", 2, "RAG mit Hugging Face und PyTorch: Retriever plus Generator",
            "Die vollständige Kette: Wissensbasis einbetten, Top-k abrufen, Prompt mit Kontext bauen, Antwort generieren – und was dabei schiefgehen kann.",
            goals(
                "Die drei Phasen Indexieren, Abrufen, Generieren umsetzen",
                "Einen Prompt mit abgerufenem Kontext korrekt bauen",
                "Typische Fehlerquellen von RAG erkennen"
            ),
            sec("Drei Phasen", """
                • Indexieren (einmalig): Dokumente laden, in Abschnitte zerlegen, einbetten, im Vektorindex speichern.
                • Abrufen (pro Frage): Frage einbetten, k ähnlichste Abschnitte holen.
                • Generieren: Frage und Abschnitte in einen Prompt setzen, das LLM antworten lassen – möglichst mit Quellenangabe.
            """,
                fm("RAG", "p(y | x) ≈ p_LLM(y | x, top_k(retrieve(x)))")
            ),
            sec("Im Code", """
                $ from transformers import AutoTokenizer, AutoModelForSeq2SeqLM
                $ gen_tok = AutoTokenizer.from_pretrained("google/flan-t5-base")
                $ gen = AutoModelForSeq2SeqLM.from_pretrained("google/flan-t5-base")
                $ def answer(question, k=3):
                $     q = q_enc(**q_tok(question, return_tensors="pt")).pooler_output.detach().numpy()
                $     _, I = index.search(q, k)
                $     context = "\n".join(passages[i] for i in I[0])
                $     prompt = f"Beantworte die Frage nur mit dem Kontext.\nKontext:\n{context}\nFrage: {question}\nAntwort:"
                $     ids = gen_tok(prompt, return_tensors="pt", truncation=True, max_length=512)
                $     out = gen.generate(**ids, max_new_tokens=80)
                $     return gen_tok.decode(out[0], skip_special_tokens=True)

                Die Anweisung „nur mit dem Kontext“ und die Erlaubnis „sonst sage, dass du es nicht weißt“ verringern Halluzinationen deutlich.
            """),
            sec("Was schiefgehen kann", """
                • Retrieval verfehlt: Die richtige Stelle ist nicht unter den Top-k (schlechte Einbettung, zu große Abschnitte, falsche Sprache). Dann kann auch das beste LLM nicht helfen.
                • Kontext zu lang: Wichtiges geht in der Mitte verloren („lost in the middle“) oder wird abgeschnitten.
                • Widersprüchliche Quellen: Das Modell mischt sie ungeprüft.
                • Das Modell ignoriert den Kontext und antwortet aus seinem Gedächtnis.

                Deshalb bewertet man Retrieval und Generierung getrennt: Recall@k für den Abruf, Treue (Faithfulness) und Relevanz für die Antwort.
            """,
                fm("Recall@k", "Anteil der Fragen, bei denen eine relevante Passage unter den Top-k ist")
            ),
            sec("RAG oder Fine-Tuning?", """
                • RAG: aktuelles, häufig wechselndes oder privates Wissen; Quellen sollen zitierbar sein; kein Training nötig.
                • Fine-Tuning: Stil, Format, Fachsprache, Verhalten – Wissen, das sich selten ändert.
                • Oft beides: ein feinabgestimmtes Modell, das gelernt hat, mit abgerufenem Kontext gut umzugehen.

                > RAG ändert, was das Modell weiß; Fine-Tuning ändert, wie es sich verhält.
            """)
        ),
        bite(
            "lc_lcel", 2, "LangChain-Kernkonzepte: Modelle, Prompt-Vorlagen, LCEL, Parser",
            "Die Bausteine von LangChain und die LangChain Expression Language: Komponenten mit dem Pipe-Operator zu Ketten verbinden, strukturiert ausgeben.",
            goals(
                "Chat-Modelle, Prompt-Vorlagen und Ausgabe-Parser einsetzen",
                "Ketten mit LCEL (prompt | llm | parser) bauen und aufrufen",
                "Runnables parallel und mit Durchreichen kombinieren"
            ),
            sec("Die Bausteine", """
                • Modelle: LLMs (Text → Text) und Chat-Modelle (Nachrichten → Nachricht), von IBM watsonx.ai, OpenAI, Hugging Face, lokal mit Ollama …
                • Prompt-Vorlagen: Texte mit Platzhaltern, gefüllt zur Laufzeit.
                • Ausgabe-Parser: wandeln die Modellantwort in Strings, JSON oder Pydantic-Objekte.
                • Dokumente, Loader, Splitter, Einbettungen, Vektorspeicher, Retriever – für RAG.
                • Gedächtnis, Werkzeuge, Agenten – für Gespräche und Handlungen.
            """),
            sec("LCEL: Ketten mit dem Pipe-Operator", """
                Jede Komponente ist ein Runnable mit den Methoden invoke, batch und stream. Mit | verbindet man sie: Die Ausgabe der linken wird die Eingabe der rechten.

                $ from langchain_core.prompts import ChatPromptTemplate
                $ from langchain_core.output_parsers import StrOutputParser
                $ from langchain_ibm import ChatWatsonx
                $ llm = ChatWatsonx(model_id="ibm/granite-3-8b-instruct", url=URL, project_id=PROJECT_ID)
                $ prompt = ChatPromptTemplate.from_messages([("system", "Du bist ein Lehrmeister für {fach}."), ("user", "{frage}")])
                $ chain = prompt | llm | StrOutputParser()
                $ print(chain.invoke({"fach": "Physik", "frage": "Was ist Entropie?"}))
                $ for chunk in chain.stream({"fach": "KI", "frage": "Was ist LoRA?"}): print(chunk, end="")
            """),
            sec("Strukturierte Ausgabe", """
                $ from langchain_core.output_parsers import JsonOutputParser
                $ from pydantic import BaseModel, Field
                $ class Ritter(BaseModel):
                $     name: str = Field(description="Name des Ritters")
                $     staerke: int = Field(description="Stärke von 1 bis 10")
                $ parser = JsonOutputParser(pydantic_object=Ritter)
                $ prompt = ChatPromptTemplate.from_template("Erfinde einen Ritter.\n{format_instructions}").partial(format_instructions=parser.get_format_instructions())
                $ ritter = (prompt | llm | parser).invoke({})

                Viele Chat-Modelle unterstützen auch llm.with_structured_output(Ritter) – das nutzt Funktionsaufrufe und ist robuster als Textparsing.
            """),
            sec("Verzweigen und Durchreichen", """
                RunnableParallel führt mehrere Ketten auf derselben Eingabe aus und liefert ein Wörterbuch; RunnablePassthrough reicht die Eingabe unverändert weiter. Das ist genau das Muster einer RAG-Kette: Kontext abrufen und Frage durchreichen.

                $ from langchain_core.runnables import RunnablePassthrough
                $ rag_chain = {"context": retriever | format_docs, "question": RunnablePassthrough()} | rag_prompt | llm | StrOutputParser()
                $ rag_chain.invoke("Was steht im Handbuch zur Wartung?")

                Ein Wörterbuch in einer LCEL-Kette wird automatisch zu einem RunnableParallel.
            """)
        ),
        bite(
            "lc_agents", 3, "Gedächtnis, Werkzeuge und Agenten in LangChain",
            "Vom Chatbot mit Gedächtnis zum Agenten, der Werkzeuge wählt: Gesprächsverlauf, Tool-Definitionen, Funktionsaufrufe, die ReAct-Schleife und Sicherheitsgrenzen.",
            goals(
                "Gesprächsverlauf in einer Kette mitführen",
                "Werkzeuge definieren und an ein Modell binden",
                "Die Agentenschleife (ReAct) erklären und Risiken begrenzen"
            ),
            sec("Gedächtnis", """
                Ein LLM ist zustandslos: Jede Anfrage beginnt bei null. Gedächtnis heißt, den bisherigen Verlauf (oder eine Zusammenfassung davon) bei jeder Anfrage mitzuschicken. Bei langen Gesprächen nimmt man nur die letzten n Nachrichten (Fenster) oder fasst ältere zusammen, damit das Kontextfenster reicht.

                $ from langchain_core.prompts import MessagesPlaceholder
                $ from langchain_core.chat_history import InMemoryChatMessageHistory
                $ from langchain_core.runnables.history import RunnableWithMessageHistory
                $ prompt = ChatPromptTemplate.from_messages([("system", "Du bist Maximus."), MessagesPlaceholder("history"), ("user", "{input}")])
                $ store = {}
                $ chat = RunnableWithMessageHistory(prompt | llm, lambda sid: store.setdefault(sid, InMemoryChatMessageHistory()), input_messages_key="input", history_messages_key="history")
                $ chat.invoke({"input": "Ich heiße Max."}, config={"configurable": {"session_id": "1"}})
            """),
            sec("Werkzeuge", """
                Ein Werkzeug ist eine Funktion mit Namen, Beschreibung und Parameterschema. Moderne Chat-Modelle können Werkzeugaufrufe als strukturierte Ausgabe erzeugen (Function Calling): Statt Text kommt „rufe multiply mit a = 6, b = 7 auf“. Die Anwendung führt die Funktion aus und gibt das Ergebnis zurück.

                $ from langchain_core.tools import tool
                $ @tool
                $ def multiply(a: float, b: float) -> float:
                $     "Multipliziert zwei Zahlen."
                $     return a * b
                $ llm_tools = llm.bind_tools([multiply])
                $ msg = llm_tools.invoke("Was ist 6 mal 7?")
                $ print(msg.tool_calls)    # [{'name': 'multiply', 'args': {'a': 6, 'b': 7}, …}]

                Die Beschreibung ist entscheidend – sie ist alles, was das Modell über das Werkzeug weiß.
            """),
            sec("Die Agentenschleife", """
                Ein Agent lässt das Modell selbst entscheiden, welche Werkzeuge es in welcher Reihenfolge nutzt. Das ReAct-Muster (Reason + Act; Yao et al. 2022) wechselt ab: Gedanke → Aktion (Werkzeugaufruf) → Beobachtung (Ergebnis) → nächster Gedanke … bis eine Endantwort steht.

                $ from langgraph.prebuilt import create_react_agent
                $ agent = create_react_agent(llm, tools=[multiply, search_docs])
                $ result = agent.invoke({"messages": [("user", "Wie viele Stunden hat die Lektion 1, mal drei?")]})

                Maximus arbeitet ähnlich: Für Rechnungen, Würfel oder 1RM ruft es Werkzeuge auf, statt zu raten.
            """,
                fm("ReAct", "Gedanke → Aktion → Beobachtung → … → Antwort")
            ),
            sec("Grenzen und Sicherheit", """
                • Iterationsgrenze (max_iterations) gegen Endlosschleifen.
                • Werkzeuge mit minimalen Rechten; gefährliche Aktionen (Mails senden, Dateien löschen, bezahlen) nur mit Bestätigung durch einen Menschen.
                • Prompt-Injection: Abgerufene Dokumente oder Webseiten können Anweisungen enthalten („ignoriere alle Regeln“). Werkzeugergebnisse sind Daten, keine Befehle.
                • Protokollieren (Tracing, z. B. mit LangSmith), um Fehlentscheidungen nachzuvollziehen.

                > Ein Agent ist so vertrauenswürdig wie sein schwächstes Werkzeug – und so klug wie die Beschreibungen, die du ihm gibst.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l12") {
        on("rag_dpr")
        mc(1, "Welches Problem löst Retrieval-Augmented Generation vor allem?",
            "Das Modell bekommt aktuelles oder privates Wissen als Kontext und halluziniert weniger", "Das Modell wird kleiner", "Das Training wird schneller", "Tokenisierung wird überflüssig",
            why = "Das LLM antwortet auf Basis abgerufener Quellen statt nur aus seinen Parametern.")
        mc(2, "Warum hat DPR zwei getrennte Encoder?",
            "Fragen und Passagen sind verschieden; getrennte Encoder erlauben, alle Passagen vorab einzubetten", "Weil ein Encoder zu klein wäre", "Für Deutsch und Englisch", "Aus Lizenzgründen",
            why = "Zur Anfragezeit muss nur die Frage kodiert werden – dann eine Vektorsuche.")
        mc(2, "Was sind In-Batch-Negatives beim DPR-Training?",
            "Die Passagen der anderen Fragen im selben Batch dienen als falsche Beispiele", "Negative Zahlen im Batch", "Gelöschte Daten", "Fragen ohne Antwort",
            why = "Billige, zahlreiche Negativbeispiele für den kontrastiven Verlust.")
        codeOut(2, "Was liefert index.search?", """
            index = faiss.IndexFlatIP(768)
            index.add(P)                     # 1000 Passagen
            D, I = index.search(q, k=3)
            print(D.shape, I.shape)
        """, "(1, 3) (1, 3)", "(3,) (3,)", "(1000, 3) (1000, 3)", "(768, 3) (1, 3)",
            why = "D enthält die Skalarprodukte, I die Indizes der drei besten Passagen – je eine Zeile pro Anfrage.")
        mc(2, "Wann ist das Skalarprodukt gleich der Kosinusähnlichkeit?",
            "Wenn alle Vektoren auf Länge 1 normiert sind", "Immer", "Nie", "Wenn d = 768",
            why = "cos = qᵀp/(‖q‖‖p‖); bei ‖q‖ = ‖p‖ = 1 bleibt qᵀp.")
        mc(2, "Was ist der Vorteil approximativer Indizes wie HNSW oder IVF?",
            "Sie sind um Größenordnungen schneller als exakte Suche, bei kleinem Recall-Verlust", "Sie sind immer exakt", "Sie brauchen keine Vektoren", "Sie funktionieren nur auf der CPU",
            why = "Exakte Suche kostet O(N·d) pro Anfrage – bei Milliarden Vektoren zu teuer.")
        mc(2, "Wofür ist BM25 besser als dichtes Retrieval?",
            "Für exakte Begriffe wie Namen, Produktcodes oder seltene Fachwörter", "Für Paraphrasen", "Für Bilder", "Für Übersetzungen",
            why = "Deshalb kombiniert man oft beides (hybride Suche).")
        mc(3, "Was macht ein Cross-Encoder beim Re-Ranking?",
            "Er liest Frage und Passage gemeinsam und bewertet ihre Relevanz genauer als ein Bi-Encoder", "Er übersetzt die Frage", "Er kodiert nur die Passage", "Er ersetzt den Vektorindex",
            why = "Zu teuer für Millionen Passagen, aber ideal für die Top-50.")

        on("rag_pipeline")
        mc(1, "Welche drei Phasen hat eine RAG-Anwendung?",
            "Indexieren, Abrufen, Generieren", "Trainieren, Testen, Löschen", "Tokenisieren, Zählen, Sortieren", "Laden, Rechnen, Plotten",
            why = "Indexieren einmalig, Abrufen und Generieren pro Frage.")
        code(2, "Warum steht im Prompt „Beantworte die Frage nur mit dem Kontext“?", """
            prompt = f"Beantworte die Frage nur mit dem Kontext.\nKontext:\n{context}\nFrage: {question}\nAntwort:"
        """, "Damit das Modell sich auf die Quellen stützt und weniger halluziniert", "Damit die Antwort kürzer wird", "Weil T5 sonst nicht startet", "Aus Höflichkeit",
            why = "Ergänzt um „sonst sage, dass du es nicht weißt“ sinkt die Halluzinationsrate deutlich.")
        mc(2, "Die richtige Passage ist nicht unter den Top-k. Was folgt?",
            "Auch das beste LLM kann die Frage dann nicht korrekt aus dem Kontext beantworten", "Das LLM findet sie trotzdem", "Die Antwort wird automatisch richtig", "k spielt keine Rolle",
            why = "Deshalb misst man Recall@k des Retrievers separat.")
        mc(2, "Was bedeutet ‚lost in the middle‘?",
            "Informationen in der Mitte langer Kontexte werden von LLMs schlechter genutzt als am Anfang oder Ende", "Das Modell verliert seine Gewichte", "Der Index ist leer", "Die Frage geht verloren",
            why = "Wichtige Passagen stellt man deshalb an den Anfang oder hält den Kontext kurz.")
        mc(2, "Wann ist RAG dem Fine-Tuning vorzuziehen?",
            "Bei häufig wechselndem oder privatem Wissen, das zitierbar sein soll", "Um den Antwortstil zu ändern", "Um ein Modell kleiner zu machen", "Nie",
            why = "RAG ändert, was das Modell weiß; Fine-Tuning, wie es sich verhält.")
        codeOut(2, "Was ergibt Recall@3 für diese Ergebnisse?", """
            # relevante Passage gefunden in Top-3?  Frage 1: ja, 2: nein, 3: ja, 4: ja
            print(3 / 4)
        """, "0.75", "0.25", "1.0", "0.5",
            why = "Drei von vier Fragen hatten eine relevante Passage unter den ersten drei Treffern.")
        mc(2, "Was misst ‚Faithfulness‘ (Treue) einer RAG-Antwort?",
            "Ob die Aussagen der Antwort durch den abgerufenen Kontext gestützt werden", "Wie schnell die Antwort kommt", "Wie lang die Antwort ist", "Ob die Frage höflich war",
            why = "Eine treue Antwort erfindet nichts über den Kontext hinaus.")
        line(2, "Wie setzt man die abgerufenen Passagen zu einem Kontextstring zusammen?",
            "context = \"\\n\".join(passages[i] for i in I[0])", "context = passages + I", "context = I.join(passages)", "context = str(index)",
            why = "I[0] enthält die Indizes der Top-k-Treffer der ersten (einzigen) Anfrage.")

        on("lc_lcel")
        mc(1, "Was macht der Operator | in LCEL?",
            "Er verbindet Runnables: Die Ausgabe der linken Komponente wird die Eingabe der rechten", "Er bildet ein logisches Oder", "Er teilt Text", "Er parallelisiert GPUs",
            why = "prompt | llm | parser ist eine Kette.")
        codeOut(1, "Was liefert chain.invoke hier?", """
            chain = prompt | llm | StrOutputParser()
            print(type(chain.invoke({"fach": "Physik", "frage": "Was ist Entropie?"})))
        """, "<class 'str'>", "<class 'dict'>", "<class 'AIMessage'>", "<class 'list'>",
            why = "Ohne Parser käme eine AIMessage; StrOutputParser extrahiert den Text.")
        mc(2, "Welche Methoden hat jedes Runnable?",
            "invoke, batch und stream (plus asynchrone Varianten)", "fit und predict", "forward und backward", "open und close",
            why = "Damit lassen sich Ketten einheitlich aufrufen, bündeln und streamen.")
        mc(2, "Was füllt ChatPromptTemplate.from_messages([(\"system\", \"… {fach}\"), (\"user\", \"{frage}\")])?",
            "Die Platzhalter fach und frage aus dem Eingabewörterbuch", "Nur den Systemprompt", "Die Modellgewichte", "Den Verlauf",
            why = "Fehlt ein Schlüssel, wirft die Vorlage einen Fehler.")
        code(2, "Was ist die Rolle des Wörterbuchs am Anfang dieser Kette?", """
            {"context": retriever | format_docs, "question": RunnablePassthrough()} | rag_prompt | llm
        """, "Es wird zu einem RunnableParallel: Kontext abrufen und Frage unverändert durchreichen, beides als Eingabe für den Prompt", "Es ist ein Kommentar",
            "Es speichert den Verlauf", "Es definiert Werkzeuge",
            why = "Das Standardmuster einer RAG-Kette in LCEL.")
        mc(2, "Wozu dient ein Ausgabe-Parser wie JsonOutputParser?",
            "Er wandelt die Textantwort in strukturierte Daten um und liefert Formatanweisungen für den Prompt", "Er tokenisiert die Eingabe", "Er ruft Werkzeuge auf", "Er bewertet die Antwort",
            why = "with_structured_output nutzt dafür Funktionsaufrufe und ist oft robuster.")
        line(2, "Wie streamt man die Antwort einer Kette Stück für Stück?",
            "for chunk in chain.stream(inputs): print(chunk, end=\"\")", "chain.invoke(inputs, stream=True)", "chain.batch(inputs)", "print(chain(inputs).stream)",
            why = "Streaming verbessert die gefühlte Geschwindigkeit in Chat-Oberflächen deutlich.")
        mc(1, "Welcher LangChain-Baustein verbindet IBM watsonx.ai-Modelle?",
            "langchain_ibm (z. B. ChatWatsonx, WatsonxLLM)", "langchain_torch", "sklearn.ibm", "transformers.watsonx",
            why = "Die IBM-Kurse nutzen Granite-, Llama- oder Mixtral-Modelle über watsonx.ai.")

        on("lc_agents")
        mc(1, "Warum braucht ein Chatbot explizites Gedächtnis?",
            "LLMs sind zustandslos; der Verlauf muss bei jeder Anfrage mitgeschickt werden", "Weil LLMs alles vergessen, was sie gelernt haben", "Weil der Tokenizer es verlangt", "Das braucht er nicht",
            why = "Gedächtnis = Verlauf (oder Zusammenfassung) im Prompt.")
        mc(2, "Wozu dient MessagesPlaceholder(\"history\") in der Vorlage?",
            "An dieser Stelle wird der bisherige Gesprächsverlauf als Nachrichtenliste eingefügt", "Er löscht den Verlauf", "Er speichert die Antwort", "Er ruft ein Werkzeug auf",
            why = "RunnableWithMessageHistory füllt ihn pro session_id.")
        code(2, "Warum ist die Docstring-Zeile hier wichtig?", """
            @tool
            def multiply(a: float, b: float) -> float:
                "Multipliziert zwei Zahlen."
                return a * b
        """, "Sie wird zur Werkzeugbeschreibung, nach der das Modell entscheidet, wann es das Werkzeug nutzt", "Sie ist nur ein Kommentar ohne Wirkung", "Sie beschleunigt die Funktion", "Sie ist Pflicht für Python",
            why = "Name, Beschreibung und Typen bilden das Schema, das das Modell sieht.")
        codeOut(2, "Was enthält msg.tool_calls ungefähr?", """
            llm_tools = llm.bind_tools([multiply])
            msg = llm_tools.invoke("Was ist 6 mal 7?")
            print(msg.tool_calls)
        """, "[{'name': 'multiply', 'args': {'a': 6, 'b': 7}, …}]", "42", "'6 mal 7 ist 42'", "[]",
            why = "Das Modell rechnet nicht selbst, sondern beantragt den Werkzeugaufruf; die Anwendung führt ihn aus.")
        mc(2, "Was ist das ReAct-Muster?",
            "Abwechselnd denken, ein Werkzeug aufrufen und das Ergebnis beobachten, bis eine Antwort feststeht", "Eine Reaktion auf Fehler", "Ein JavaScript-Framework", "Ein Lernratenplan",
            why = "Reason + Act (Yao et al. 2022).")
        mc(2, "Was ist Prompt-Injection bei Agenten?",
            "Abgerufene Inhalte enthalten versteckte Anweisungen, die das Modell zu unerwünschten Aktionen verleiten", "Ein schneller Prompt", "Ein Trainingsverfahren", "Das Einfügen von Platzhaltern",
            why = "Werkzeugergebnisse sind Daten, keine Befehle – kritische Aktionen brauchen menschliche Bestätigung.")
        mc(2, "Wozu dient max_iterations bei einem Agenten?",
            "Es begrenzt die Zahl der Denk-Handlungs-Runden und verhindert Endlosschleifen", "Es setzt die Lernrate", "Es begrenzt die Tokens", "Es zählt die Werkzeuge",
            why = "Ohne Grenze kann ein Agent zwischen Werkzeugen pendeln.")
        mc(3, "Wie hält man den Verlauf langer Gespräche im Kontextfenster?",
            "Nur die letzten n Nachrichten mitschicken oder ältere Teile zusammenfassen", "Den Verlauf jedes Mal löschen", "Das Kontextfenster vergrößern, indem man mehr GPUs nutzt", "Gar nicht",
            why = "Fenster- und Zusammenfassungsgedächtnis sind die Standardtechniken.")
    }
}

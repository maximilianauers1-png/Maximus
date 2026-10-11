package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 13 (IBM „Project: Generative AI Applications with RAG and LangChain“): a question-answering bot
 * over one's own documents — loaders, splitters, embeddings, vector stores, retrievers and a Gradio UI.
 */
internal object AiL13Project {
    val bites: List<Bite> = listOf(
        bite(
            "prj_loaders", 1, "Dokumente laden: PDF, Web, Text und Metadaten",
            "Der erste Schritt des QA-Bots: Inhalte aus PDFs, Webseiten und Dateien in LangChain-Dokumente mit Text und Metadaten verwandeln.",
            goals(
                "Das Document-Objekt mit page_content und metadata kennen",
                "PDF-, Web-, Text- und CSV-Loader einsetzen",
                "Metadaten für Quellenangaben und Filter nutzen"
            ),
            sec("Das Ziel des Projekts", """
                Am Ende dieser Lektion steht ein QA-Bot: Du lädst ein PDF hoch (etwa ein Forschungspaper oder ein Handbuch) und stellst Fragen in natürlicher Sprache; der Bot antwortet mit Belegen aus dem Dokument. Die Kette besteht aus sechs Gliedern: Laden → Zerlegen → Einbetten → Speichern → Abrufen → Antworten, dazu eine Weboberfläche mit Gradio und ein LLM von IBM watsonx.ai.
            """),
            sec("Das Document-Objekt", """
                Jeder Loader liefert eine Liste von Document-Objekten. Ein Document hat den Text (page_content) und ein Wörterbuch mit Metadaten (metadata) – Quelle, Seitenzahl, Titel, Datum. Metadaten wandern durch die ganze Kette und erlauben später Quellenangaben („Seite 7“) und Filter („nur Dokumente von 2024“).

                $ from langchain_community.document_loaders import PyPDFLoader
                $ docs = PyPDFLoader("paper.pdf").load()          # ein Document pro Seite
                $ print(len(docs), docs[0].metadata)               # z. B. 12 {'source': 'paper.pdf', 'page': 0}
                $ print(docs[0].page_content[:200])
            """),
            sec("Viele Quellen", """
                $ from langchain_community.document_loaders import TextLoader, CSVLoader, WebBaseLoader, PyMuPDFLoader, UnstructuredMarkdownLoader
                $ txt = TextLoader("notizen.txt", encoding="utf-8").load()
                $ csv = CSVLoader("ritter.csv").load()                     # ein Document pro Zeile
                $ web = WebBaseLoader("https://www.ibm.com/topics/rag").load()
                $ pdf = PyMuPDFLoader("handbuch.pdf").load()               # schneller, oft bessere Textextraktion

                Für große Bestände gibt es DirectoryLoader und lazy_load(), das Dokumente einzeln liefert statt alles auf einmal in den Speicher zu laden.
            """),
            sec("Qualität beginnt beim Laden", """
                • Gescannte PDFs enthalten nur Bilder – ohne OCR kommt leerer Text heraus.
                • Zweispaltige Layouts, Tabellen, Kopf- und Fußzeilen erzeugen Textsalat; Spezial-Loader oder Nachbearbeitung helfen.
                • Formeln gehen oft verloren – für Physik-Paper ein echtes Problem.
                • Immer eine Stichprobe des geladenen Texts ansehen, bevor man weiterbaut.

                > Garbage in, garbage out gilt für RAG doppelt: Ein schlecht geladener Text ruiniert Zerlegen, Einbetten, Abrufen und Antwort zugleich.
            """)
        ),
        bite(
            "prj_splitters", 2, "Text zerlegen: Chunk-Größe, Überlappung, rekursive Splitter",
            "Warum man Dokumente in Abschnitte schneidet, wie RecursiveCharacterTextSplitter arbeitet, und wie Chunk-Größe und Overlap die Qualität bestimmen.",
            goals(
                "Begründen, warum Dokumente in Chunks zerlegt werden",
                "RecursiveCharacterTextSplitter mit chunk_size und chunk_overlap konfigurieren",
                "Die Zahl der Chunks abschätzen und strukturbewusste Splitter wählen"
            ),
            sec("Warum zerlegen?", """
                • Einbettungsmodelle haben eine Eingabegrenze (oft 256–512 Tokens); längerer Text wird abgeschnitten.
                • Ein Vektor für ein ganzes 30-Seiten-Dokument verwässert alle Einzelthemen – die Suche wird unscharf.
                • Das LLM soll nur die relevanten Passagen bekommen, nicht das ganze Dokument (Kosten, Kontextfenster, „lost in the middle“).

                Zu kleine Chunks verlieren Zusammenhang („Er“ – wer?), zu große verwässern die Einbettung. Typische Startwerte: 500–1000 Zeichen mit 10–20 % Überlappung.
            """),
            sec("Der rekursive Splitter", """
                RecursiveCharacterTextSplitter versucht, an natürlichen Grenzen zu schneiden: zuerst an Absätzen (\n\n), dann an Zeilen (\n), dann an Leerzeichen, zuletzt mitten im Wort – immer so, dass die Chunks höchstens chunk_size Zeichen haben. Die Überlappung wiederholt das Ende eines Chunks am Anfang des nächsten, damit Sätze an der Grenze nicht zerrissen werden.

                $ from langchain_text_splitters import RecursiveCharacterTextSplitter
                $ splitter = RecursiveCharacterTextSplitter(chunk_size=800, chunk_overlap=100, separators=["\n\n", "\n", " ", ""])
                $ chunks = splitter.split_documents(docs)
                $ print(len(chunks), chunks[0].metadata)          # Metadaten werden übernommen
            """,
                fm("Anzahl Chunks (grob)", "n ≈ ⌈(L − o) / (s − o)⌉ für Länge L, Größe s, Überlappung o")
            ),
            sec("Andere Splitter", """
                • CharacterTextSplitter: schneidet nur an einem Trennzeichen.
                • TokenTextSplitter: misst in Tokens statt Zeichen – passt genau zur Grenze des Einbettungsmodells.
                • MarkdownHeaderTextSplitter: schneidet an Überschriften und schreibt sie in die Metadaten.
                • Sprachbewusste Splitter für Code (Python, Java …): schneiden an Funktionen und Klassen.
                • Semantische Splitter: schneiden dort, wo sich die Einbettung benachbarter Sätze stark ändert.

                $ from langchain_text_splitters import Language
                $ py_splitter = RecursiveCharacterTextSplitter.from_language(Language.PYTHON, chunk_size=400, chunk_overlap=0)
            """),
            sec("Einstellen wie ein Experiment", """
                Die richtige Chunk-Größe hängt von Dokumenttyp und Fragen ab. Man misst sie: einige Testfragen mit bekannter Antwortstelle, verschiedene Einstellungen, Recall@k des Retrievers vergleichen. Faustregel: Faktenfragen (Zahlen, Definitionen) mögen kleinere Chunks, Fragen nach Zusammenhängen größere.

                > Ein Chunk sollte für sich allein verständlich sein – lies ein paar zufällige Chunks und frag dich, ob ein Mensch damit eine Frage beantworten könnte.
            """)
        ),
        bite(
            "prj_embed_store", 2, "Einbettungen und Vektorspeicher: Chroma und FAISS",
            "Chunks in Vektoren verwandeln (watsonx, Hugging Face), in Chroma oder FAISS speichern, persistent machen und mit Metadaten filtern.",
            goals(
                "Einbettungsmodelle in LangChain einsetzen und ihre Dimension kennen",
                "Einen Chroma- oder FAISS-Vektorspeicher aus Dokumenten bauen und speichern",
                "Ähnlichkeitssuche mit Scores und Metadatenfiltern durchführen"
            ),
            sec("Einbettungsmodelle", """
                Ein Einbettungsmodell bildet einen Text auf einen Vektor fester Dimension ab, sodass inhaltlich ähnliche Texte nahe beieinander liegen. Im IBM-Projekt nutzt man watsonx.ai (z. B. ibm/slate-125m-english-rtrvr), alternativ Sentence-Transformers von Hugging Face. Wichtig: Für Indexieren und Anfragen muss dasselbe Modell verwendet werden.

                $ from langchain_ibm import WatsonxEmbeddings
                $ emb = WatsonxEmbeddings(model_id="ibm/slate-125m-english-rtrvr", url=URL, project_id=PROJECT_ID)
                $ from langchain_huggingface import HuggingFaceEmbeddings
                $ emb = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
                $ v = emb.embed_query("Was ist ein Ritter?")
                $ print(len(v))        # 384 bei all-MiniLM-L6-v2
            """,
                fm("Speicher des Index", "M ≈ N_chunks · d · 4 Byte (float32)")
            ),
            sec("Chroma", """
                Chroma ist eine einfache, quelloffene Vektordatenbank, die Vektoren, Texte und Metadaten zusammen speichert.

                $ from langchain_chroma import Chroma
                $ db = Chroma.from_documents(chunks, emb, persist_directory="./chroma_db")
                $ hits = db.similarity_search("Wie wartet man die Maschine?", k=4)
                $ scored = db.similarity_search_with_score("Wartung", k=4)       # (Document, Distanz)
                $ filtered = db.similarity_search("Wartung", k=4, filter={"source": "handbuch.pdf"})

                Mit persist_directory bleibt der Index auf der Platte; beim nächsten Start lädt man ihn mit Chroma(persist_directory=…, embedding_function=emb), statt alles neu einzubetten.
            """),
            sec("FAISS in LangChain", """
                $ from langchain_community.vectorstores import FAISS
                $ fdb = FAISS.from_documents(chunks, emb)
                $ fdb.save_local("faiss_index")
                $ fdb = FAISS.load_local("faiss_index", emb, allow_dangerous_deserialization=True)

                FAISS ist eine Bibliothek im Speicher, sehr schnell, ohne Server. Chroma, Milvus, Weaviate, pgvector oder Elasticsearch sind Datenbanken mit Persistenz, Filtern und Mehrbenutzerbetrieb. Achtung bei Scores: Manche Speicher liefern Distanzen (kleiner = ähnlicher), andere Ähnlichkeiten (größer = ähnlicher).
            """),
            sec("Daten aktuell halten", """
                • Neue Dokumente: add_documents statt alles neu bauen.
                • Geänderte Dokumente: alte Chunks über eine stabile ID löschen und neu einfügen.
                • Wechsel des Einbettungsmodells: kompletter Neuaufbau – Vektoren verschiedener Modelle sind nicht vergleichbar.

                > Der Vektorspeicher ist das Langzeitgedächtnis des Bots. Pflege ihn wie eine Datenbank, nicht wie einen Cache.
            """)
        ),
        bite(
            "prj_retrievers", 2, "Retriever: Ähnlichkeit, MMR, Multi-Query, Self-Query, Parent Document",
            "Vom Vektorspeicher zum Retriever: Top-k, Schwellen, maximale marginale Relevanz für Vielfalt, umformulierte Anfragen, Metadatenfilter aus Sprache und kleine Chunks mit großem Kontext.",
            goals(
                "Einen Retriever aus einem Vektorspeicher erzeugen und konfigurieren",
                "MMR mathematisch verstehen und einsetzen",
                "Multi-Query-, Self-Query- und Parent-Document-Retriever unterscheiden"
            ),
            sec("Der einfache Retriever", """
                Ein Retriever ist ein Runnable: Eingabe eine Frage, Ausgabe eine Liste von Dokumenten. Jeder Vektorspeicher liefert einen mit as_retriever.

                $ retriever = db.as_retriever(search_type="similarity", search_kwargs={"k": 4})
                $ docs = retriever.invoke("Wie oft muss man das Öl wechseln?")
                $ strict = db.as_retriever(search_type="similarity_score_threshold", search_kwargs={"score_threshold": 0.5})
            """),
            sec("Maximale marginale Relevanz (MMR)", """
                Die Top-k nach Ähnlichkeit sind oft fast identisch – vier Chunks mit demselben Absatz aus verschiedenen Kopien. MMR wählt schrittweise das Dokument, das der Frage ähnlich und den schon gewählten unähnlich ist. λ = 1 ist reine Relevanz, λ = 0 reine Vielfalt.

                $ mmr = db.as_retriever(search_type="mmr", search_kwargs={"k": 4, "fetch_k": 20, "lambda_mult": 0.5})

                fetch_k Kandidaten werden nach Ähnlichkeit geholt, daraus k per MMR ausgewählt.
            """,
                fm("MMR", "d* = argmax_{d ∈ R∖S} [λ sim(d, q) − (1 − λ) max_{s ∈ S} sim(d, s)]")
            ),
            sec("Klügere Retriever", """
                • Multi-Query: Ein LLM formuliert die Frage auf mehrere Arten um; die Treffer aller Varianten werden vereinigt. Hilft bei unglücklicher Wortwahl.
                • Self-Query: Ein LLM übersetzt „Welche Paper von 2023 behandeln LoRA?“ in eine semantische Anfrage („LoRA“) plus Metadatenfilter (year = 2023).
                • Parent Document: Gesucht wird mit kleinen, präzisen Chunks; zurückgegeben wird der größere Elternabschnitt – präzise Suche, genug Kontext.
                • Kontextuelle Kompression: Ein LLM schneidet aus jedem Treffer nur den relevanten Teil heraus.

                $ from langchain.retrievers.multi_query import MultiQueryRetriever
                $ mq = MultiQueryRetriever.from_llm(retriever=db.as_retriever(), llm=llm)
                $ from langchain.retrievers import ParentDocumentRetriever
                $ from langchain.storage import InMemoryStore
                $ pdr = ParentDocumentRetriever(vectorstore=db, docstore=InMemoryStore(), child_splitter=RecursiveCharacterTextSplitter(chunk_size=200), parent_splitter=RecursiveCharacterTextSplitter(chunk_size=1500))
                $ pdr.add_documents(docs)
            """),
            sec("Welcher Retriever wann?", """
                • Kleine, saubere Wissensbasis: Ähnlichkeit mit k = 3–5.
                • Viele Duplikate: MMR.
                • Nutzer formulieren unpräzise: Multi-Query.
                • Reiche Metadaten (Jahr, Autor, Abteilung): Self-Query.
                • Lange, zusammenhängende Dokumente: Parent Document.

                > Jede Stufe Klugheit kostet LLM-Aufrufe und Latenz. Erst messen, ob der einfache Retriever wirklich versagt.
            """)
        ),
        bite(
            "prj_qabot", 2, "Der QA-Bot: Retrieval-Kette und Gradio-Oberfläche",
            "Alles zusammen: LLM von watsonx.ai, Retrieval-Kette mit Quellen, eine Gradio-Weboberfläche zum PDF-Hochladen – und wie man den Bot prüft.",
            goals(
                "Eine vollständige Frage-Antwort-Kette mit Quellenangaben bauen",
                "Eine Gradio-Oberfläche mit Datei-Upload und Textfeld erstellen",
                "Den Bot systematisch testen und typische Fehler beheben"
            ),
            sec("Die Kette", """
                $ from langchain_ibm import WatsonxLLM
                $ from langchain_core.prompts import ChatPromptTemplate
                $ from langchain.chains.combine_documents import create_stuff_documents_chain
                $ from langchain.chains import create_retrieval_chain
                $ llm = WatsonxLLM(model_id="mistralai/mixtral-8x7b-instruct-v01", url=URL, project_id=PROJECT_ID, params={"max_new_tokens": 256, "temperature": 0.2})
                $ prompt = ChatPromptTemplate.from_template("Beantworte die Frage nur anhand des Kontexts. Wenn die Antwort fehlt, sage das.\n\nKontext:\n{context}\n\nFrage: {input}")
                $ qa = create_retrieval_chain(retriever, create_stuff_documents_chain(llm, prompt))
                $ result = qa.invoke({"input": "Welche Methode schlägt das Paper vor?"})
                $ print(result["answer"]); print([d.metadata["page"] for d in result["context"]])

                „Stuff“ heißt: alle abgerufenen Dokumente werden in den Kontext gestopft. Bei sehr vielen Treffern gibt es map-reduce-artige Varianten. Ältere Kurse nutzen RetrievalQA.from_chain_type(llm, chain_type="stuff", retriever=retriever, return_source_documents=True) – das Prinzip ist dasselbe.
            """),
            sec("Die Oberfläche mit Gradio", """
                Gradio baut aus einer Python-Funktion eine Weboberfläche: Eingabekomponenten, Ausgabekomponenten, fertig.

                $ import gradio as gr
                $ def retriever_qa(file, query):
                $     docs = PyPDFLoader(file).load()
                $     chunks = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=50).split_documents(docs)
                $     db = Chroma.from_documents(chunks, emb)
                $     qa = create_retrieval_chain(db.as_retriever(), create_stuff_documents_chain(llm, prompt))
                $     return qa.invoke({"input": query})["answer"]
                $ app = gr.Interface(fn=retriever_qa, inputs=[gr.File(label="PDF hochladen", file_types=[".pdf"], type="filepath"), gr.Textbox(label="Frage")], outputs=gr.Textbox(label="Antwort"), title="QA-Bot")
                $ app.launch(server_name="0.0.0.0", server_port=7860)

                In der Produktion würde man den Index nicht bei jeder Frage neu bauen, sondern pro Datei einmal und dann zwischenspeichern.
            """),
            sec("Testen wie ein Ingenieur", """
                • Fragen mit bekannter Antwort und Fundstelle: Ist die Fundstelle unter den abgerufenen Chunks? Stimmt die Antwort?
                • Fragen ohne Antwort im Dokument: Sagt der Bot ehrlich „steht nicht drin“?
                • Paraphrasen und Tippfehler: Ist der Bot robust?
                • Angriffe: „Ignoriere den Kontext und …“ – bleibt er bei seinen Regeln?
                • Latenz und Kosten pro Frage messen.

                Häufige Fehler und Abhilfe: falsche Seite gefunden → Chunk-Größe und k anpassen, MMR; Antwort erfunden → strengerer Prompt, niedrigere Temperatur; Text fehlt → Loader prüfen (OCR?).
            """),
            sec("Abschluss der Reise", """
                Mit diesem Projekt hast du den Bogen des ganzen Kurses geschlagen: Daten laden und vorbereiten (Lektion 1), neuronale Netze und Einbettungen (Lektionen 2–8), Transformer und Fine-Tuning (9–11), RAG und Agenten (12) – zusammengesetzt zu einer echten Anwendung.

                > Ein AI Engineer baut nicht nur Modelle, sondern Systeme: Daten, Modell, Abruf, Oberfläche, Tests und Betrieb. Jetzt reite zum letzten Turnier.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l13") {
        on("prj_loaders")
        mc(1, "Welche zwei Felder hat ein LangChain-Document?",
            "page_content und metadata", "text und label", "input_ids und attention_mask", "title und body",
            why = "Metadaten wie Quelle und Seite wandern durch die ganze Kette.")
        codeOut(1, "Was liefert PyPDFLoader für ein PDF mit 12 Seiten?", """
            docs = PyPDFLoader("paper.pdf").load()
            print(len(docs))
        """, "12", "1", "Die Zahl der Zeichen", "0",
            why = "Standardmäßig ein Document pro Seite, mit page in den Metadaten.")
        mc(2, "Warum liefert ein gescanntes PDF oft leeren Text?",
            "Es enthält nur Bilder der Seiten; ohne OCR gibt es keinen Text zu extrahieren", "Weil PDFs verschlüsselt sind", "Weil der Loader nur Englisch kann", "Weil PDFs keine Seiten haben",
            why = "Dann braucht man einen OCR-fähigen Loader.")
        mc(2, "Wofür sind Metadaten im QA-Bot nützlich?",
            "Für Quellenangaben (Seite, Datei) und zum Filtern der Suche", "Um die Einbettung zu ersetzen", "Um das Modell zu trainieren", "Sie haben keinen Nutzen",
            why = "„Antwort laut Seite 7“ schafft Vertrauen und Nachprüfbarkeit.")
        line(2, "Welcher Loader lädt eine Webseite?",
            "WebBaseLoader(\"https://www.ibm.com/topics/rag\").load()", "PyPDFLoader(\"https://…\").load()", "TextLoader(url).fetch()", "CSVLoader(\"https://…\").load()",
            why = "Er lädt das HTML und extrahiert den Text (mit BeautifulSoup).")
        mc(2, "Wozu dient lazy_load()?",
            "Es liefert Dokumente einzeln, statt alle auf einmal in den Speicher zu laden", "Es lädt langsamer", "Es lädt nur die Metadaten", "Es lädt nichts",
            why = "Wichtig bei großen Dokumentbeständen.")
        mc(1, "Aus welchen Gliedern besteht die Kette des QA-Bots?",
            "Laden → Zerlegen → Einbetten → Speichern → Abrufen → Antworten", "Trainieren → Testen → Speichern", "Tokenisieren → Klassifizieren", "Laden → Antworten",
            why = "Dazu eine Oberfläche (Gradio) und ein LLM (watsonx.ai).")
        mc(3, "Warum sollte man nach dem Laden Stichproben des Texts ansehen?",
            "Layoutprobleme, Tabellensalat oder fehlende Formeln verderben alle späteren Schritte unbemerkt", "Weil LangChain es verlangt", "Um Speicher zu sparen", "Das ist unnötig",
            why = "Datenqualität ist der wichtigste Hebel jeder RAG-Anwendung.")

        on("prj_splitters")
        mc(1, "Warum zerlegt man Dokumente vor dem Einbetten in Chunks?",
            "Einbettungsmodelle haben Eingabegrenzen, und kleinere Abschnitte ergeben präzisere Treffer", "Um Text zu verschlüsseln", "Weil LLMs keine langen Wörter kennen", "Um Metadaten zu löschen",
            why = "Ein Vektor für 30 Seiten verwässert alle Einzelthemen.")
        mc(2, "Wozu dient chunk_overlap?",
            "Das Ende eines Chunks wird am Anfang des nächsten wiederholt, damit Sätze an Grenzen nicht zerrissen werden", "Es verdoppelt alle Chunks", "Es setzt die Chunk-Größe", "Es entfernt Duplikate",
            why = "Typisch 10–20 % der Chunk-Größe.")
        mc(2, "In welcher Reihenfolge versucht RecursiveCharacterTextSplitter standardmäßig zu trennen?",
            "Absätze, Zeilen, Leerzeichen, zuletzt einzelne Zeichen", "Zeichen, Wörter, Sätze", "Nur an Punkten", "Zufällig",
            why = "So bleiben natürliche Einheiten möglichst erhalten.")
        mc(2, "Etwa wie viele Chunks entstehen aus 10 000 Zeichen mit chunk_size = 1000 und overlap = 200?",
            "Etwa 13", "Etwa 10", "Etwa 50", "Etwa 8",
            why = "(L − o)/(s − o) = 9800/800 ≈ 12,25 → 13 Chunks (der Splitter schneidet an Grenzen, daher nur ungefähr).")
        codeOut(2, "Was bleibt in den Chunks erhalten?", """
            chunks = splitter.split_documents(docs)
            print(chunks[0].metadata)
        """, "Die Metadaten des Ursprungsdokuments, z. B. {'source': 'paper.pdf', 'page': 0}", "Nichts", "Nur die Chunk-Nummer", "Die Einbettung",
            why = "split_documents kopiert die Metadaten in jeden Chunk.")
        line(2, "Welcher Splitter misst die Größe in Tokens statt in Zeichen?",
            "TokenTextSplitter(chunk_size=256, chunk_overlap=32)", "CharacterTextSplitter(tokens=True)", "RecursiveCharacterTextSplitter(unit=\"token\")", "MarkdownHeaderTextSplitter(256)",
            why = "Passt exakt zur Tokengrenze des Einbettungsmodells.")
        mc(2, "Was macht MarkdownHeaderTextSplitter?",
            "Er schneidet an Überschriften und schreibt sie in die Metadaten der Chunks", "Er entfernt Überschriften", "Er wandelt Markdown in PDF", "Er zählt Überschriften",
            why = "So weiß jeder Chunk, zu welchem Kapitel er gehört.")
        mc(3, "Wie findet man eine gute Chunk-Größe?",
            "Mit Testfragen bekannter Fundstelle verschiedene Einstellungen vergleichen (z. B. Recall@k)", "Immer 100 Zeichen nehmen", "So groß wie möglich", "Raten",
            why = "Wie ein Experiment: eine Größe variieren, messen, vergleichen.")

        on("prj_embed_store")
        mc(1, "Was muss beim Indexieren und Abfragen gleich sein?",
            "Das Einbettungsmodell", "Die Chunk-Größe der Frage", "Die Temperatur des LLM", "Der Dateiname",
            why = "Vektoren verschiedener Modelle liegen in verschiedenen Räumen und sind nicht vergleichbar.")
        codeOut(1, "Wie lang ist der Vektor bei all-MiniLM-L6-v2?", """
            emb = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")
            print(len(emb.embed_query("Was ist ein Ritter?")))
        """, "384", "768", "1536", "512",
            why = "Ein kleines, schnelles Modell mit 384 Dimensionen.")
        mc(2, "Wie viel Speicher braucht ein Index mit 100 000 Chunks bei d = 768 in float32?",
            "Etwa 307 MB", "Etwa 30 MB", "Etwa 3 GB", "Etwa 77 MB",
            why = "100 000 · 768 · 4 Byte = 307,2 MB (ohne Texte und Metadaten).")
        line(2, "Wie baut man einen persistenten Chroma-Speicher aus Chunks?",
            "Chroma.from_documents(chunks, emb, persist_directory=\"./chroma_db\")", "Chroma(chunks).save()", "Chroma.load(chunks)", "FAISS.persist(chunks)",
            why = "Später lädt man ihn mit Chroma(persist_directory=…, embedding_function=emb).")
        code(2, "Was bewirkt der filter-Parameter?", """
            db.similarity_search("Wartung", k=4, filter={"source": "handbuch.pdf"})
        """, "Es werden nur Chunks aus handbuch.pdf durchsucht", "Es entfernt handbuch.pdf aus dem Index", "Es sortiert nach Dateinamen", "Es hat keine Wirkung",
            why = "Metadatenfilter grenzen die Suche ein – Grundlage des Self-Query-Retrievers.")
        mc(2, "Worauf muss man bei similarity_search_with_score achten?",
            "Manche Speicher liefern Distanzen (kleiner = ähnlicher), andere Ähnlichkeiten (größer = ähnlicher)", "Scores sind immer Prozent", "Scores sind immer negativ", "Es gibt keine Scores",
            why = "Chroma liefert standardmäßig Distanzen.")
        mc(2, "Was ist nötig, wenn man das Einbettungsmodell wechselt?",
            "Den Index komplett neu aufbauen", "Nichts", "Nur die Chunk-Größe ändern", "Nur das LLM wechseln",
            why = "Alte und neue Vektoren sind nicht vergleichbar.")
        mc(3, "Was unterscheidet FAISS grundsätzlich von Chroma oder pgvector?",
            "FAISS ist eine Suchbibliothek im Speicher, die anderen sind Datenbanken mit Persistenz, Filtern und Mehrbenutzerbetrieb", "FAISS kann keine Vektoren", "Chroma ist schneller als jede Bibliothek", "Es gibt keinen Unterschied",
            why = "Für Prototypen genügt FAISS, für Produktivsysteme braucht man meist eine Datenbank.")

        on("prj_retrievers")
        line(1, "Wie erzeugt man aus einem Vektorspeicher einen Retriever mit k = 4?",
            "db.as_retriever(search_kwargs={\"k\": 4})", "db.retriever(4)", "Retriever(db, top=4)", "db.search(k=4).retriever",
            why = "Ein Retriever ist ein Runnable: Frage rein, Dokumente raus.")
        mc(2, "Wozu dient Maximal Marginal Relevance (MMR)?",
            "Treffer sollen relevant und zugleich untereinander verschieden sein – Duplikate werden vermieden", "Die relevantesten Treffer doppelt zurückgeben", "Treffer zufällig mischen", "Nur den ersten Treffer liefern",
            why = "λ steuert den Kompromiss zwischen Relevanz und Vielfalt.")
        mc(2, "Was bedeutet lambda_mult = 1 bei MMR?",
            "Reine Relevanz, keine Vielfaltsstrafe", "Reine Vielfalt", "Zufällige Auswahl", "Keine Treffer",
            why = "MMR = λ·sim(d, q) − (1 − λ)·max sim(d, s).")
        mc(2, "Was macht ein Multi-Query-Retriever?",
            "Ein LLM formuliert die Frage mehrfach um und vereinigt die Treffer aller Varianten", "Er stellt mehrere Fragen an den Nutzer", "Er durchsucht mehrere Datenbanken", "Er übersetzt die Frage",
            why = "Robust gegen unglückliche Formulierungen.")
        mc(2, "Was leistet ein Self-Query-Retriever?",
            "Er übersetzt eine natürliche Frage in eine semantische Suche plus Metadatenfilter", "Er fragt sich selbst", "Er braucht keine Vektoren", "Er speichert Fragen",
            why = "„Paper von 2023 über LoRA“ → Suche „LoRA“, Filter year = 2023.")
        mc(2, "Was ist die Idee des Parent-Document-Retrievers?",
            "Mit kleinen Chunks präzise suchen, aber die größeren Elternabschnitte als Kontext zurückgeben", "Nur ganze Dokumente speichern", "Nur Überschriften suchen", "Eltern- und Kinddokumente löschen",
            why = "Präzision der Suche und genug Kontext für die Antwort.")
        code(2, "Was bedeuten fetch_k und k hier?", """
            db.as_retriever(search_type="mmr", search_kwargs={"k": 4, "fetch_k": 20})
        """, "20 Kandidaten werden nach Ähnlichkeit geholt, daraus 4 per MMR ausgewählt", "4 werden geholt, 20 zurückgegeben", "Es werden 24 Dokumente zurückgegeben", "fetch_k ist die Chunk-Größe",
            why = "MMR wählt aus einem größeren Kandidatenpool.")
        mc(3, "Warum sollte man nicht sofort den klügsten Retriever einsetzen?",
            "Jede Stufe kostet zusätzliche LLM-Aufrufe und Latenz; erst messen, ob der einfache Retriever versagt", "Weil kluge Retriever falsch sind", "Weil LangChain sie verbietet", "Weil sie keine Metadaten nutzen",
            why = "Einfach anfangen, messen, gezielt verbessern.")

        on("prj_qabot")
        mc(1, "Was bedeutet der Kettentyp ‚stuff‘?",
            "Alle abgerufenen Dokumente werden zusammen in den Kontext des Prompts gesteckt", "Die Dokumente werden zusammengefasst", "Nur das erste Dokument wird verwendet", "Die Dokumente werden übersetzt",
            why = "Einfach und gut, solange der Kontext ins Fenster passt.")
        codeOut(2, "Was enthält result nach diesem Aufruf?", """
            qa = create_retrieval_chain(retriever, create_stuff_documents_chain(llm, prompt))
            result = qa.invoke({"input": "Welche Methode schlägt das Paper vor?"})
            print(sorted(result.keys()))
        """, "['answer', 'context', 'input']", "['answer']", "['output', 'sources']", "['text']",
            why = "context enthält die abgerufenen Dokumente – ideal für Quellenangaben.")
        mc(2, "Warum setzt man die Temperatur des LLM im QA-Bot niedrig (z. B. 0,2)?",
            "Für faktentreue, reproduzierbare Antworten statt kreativer Variation", "Damit es schneller antwortet", "Damit es längere Antworten schreibt", "Weil watsonx es verlangt",
            why = "Kreativität ist beim Zitieren aus Dokumenten unerwünscht.")
        line(2, "Welche Gradio-Komponente nimmt eine PDF-Datei als Pfad entgegen?",
            "gr.File(label=\"PDF hochladen\", file_types=[\".pdf\"], type=\"filepath\")", "gr.Textbox(type=\"pdf\")", "gr.Image(\"pdf\")", "gr.Upload(pdf=True)",
            why = "type=\"filepath\" übergibt der Funktion den Pfad der hochgeladenen Datei.")
        code(2, "Was ist an dieser Funktion für die Produktion ineffizient?", """
            def retriever_qa(file, query):
                docs = PyPDFLoader(file).load()
                chunks = splitter.split_documents(docs)
                db = Chroma.from_documents(chunks, emb)
                ...
        """, "Bei jeder Frage wird das PDF neu geladen, zerlegt und eingebettet; besser einmal pro Datei indexieren und zwischenspeichern", "PyPDFLoader ist zu schnell",
            "Chroma kann keine PDFs", "Nichts",
            why = "Einbetten ist der teuerste Schritt – er gehört nicht in den Pfad jeder Anfrage.")
        mc(2, "Wie prüft man, ob der Bot ehrlich ist?",
            "Mit Fragen, deren Antwort nicht im Dokument steht – er sollte sagen, dass es dort nicht steht", "Mit sehr langen Fragen", "Indem man die Temperatur erhöht", "Gar nicht",
            why = "Ein guter Prompt erlaubt und verlangt diese Ehrlichkeit.")
        mc(2, "Der Bot findet die falschen Seiten. Was hilft zuerst?",
            "Chunk-Größe, k und Retriever-Typ (z. B. MMR) anpassen und mit Testfragen messen", "Ein größeres LLM", "Höhere Temperatur", "Mehr Gradio-Komponenten",
            why = "Wenn der Abruf versagt, kann das LLM nicht helfen.")
        mc(1, "Wofür ist Gradio gedacht?",
            "Um aus Python-Funktionen schnell eine Weboberfläche zu bauen", "Um Modelle zu trainieren", "Um Gradienten zu berechnen", "Um Daten zu speichern",
            why = "gr.Interface(fn, inputs, outputs).launch() – fertig ist der Prototyp.")
    }
}

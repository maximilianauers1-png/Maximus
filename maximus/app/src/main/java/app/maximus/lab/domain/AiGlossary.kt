package app.maximus.lab.domain

import kotlin.random.Random

/** One glossary entry: a term, a beginner-friendly definition, and the chapter where it is explained. */
data class GlossaryTerm(val term: String, val definition: String, val chapterKey: String, val group: String)

/**
 * The AI/ML glossary. Every term also becomes two quiz questions ("Was bedeutet …?" and "Welcher Begriff
 * passt …?") and feeds the "Begriffe-Duell" game; distractors come from the same group so they look alike.
 */
object AiGlossary {
    private fun g(group: String, chapter: String, vararg pairs: Pair<String, String>) =
        pairs.map { (t, d) -> GlossaryTerm(t, d, chapter, group) }

    val terms: List<GlossaryTerm> = listOf(
        g("Grundbegriffe", "ai0_what",
            "Künstliche Intelligenz (KI)" to "Oberbegriff für Systeme, die Aufgaben lösen, für die man bei Menschen Intelligenz bräuchte",
            "Maschinelles Lernen (ML)" to "Teilgebiet der KI, in dem Regeln aus Beispieldaten gelernt statt von Hand programmiert werden",
            "Deep Learning" to "Maschinelles Lernen mit tiefen neuronalen Netzen aus vielen Schichten",
            "Generative KI" to "Modelle, die neue Inhalte wie Text, Bilder oder Musik erzeugen",
            "Schwache KI" to "KI, die nur in eng umgrenzten Aufgaben gut ist – alle heutigen Systeme",
            "AGI" to "Hypothetische allgemeine KI, die jede intellektuelle Aufgabe eines Menschen lernen könnte"
        ) + g("Grundbegriffe", "ai0_data",
            "Datenpunkt (Sample)" to "Ein einzelnes Beispiel im Datensatz, eine Zeile der Tabelle",
            "Merkmal (Feature)" to "Eine Eingangsgröße, die ein Beispiel beschreibt, eine Spalte der Tabelle",
            "Label" to "Die richtige Antwort zu einem Beispiel, die das Modell vorhersagen soll",
            "Datensatz" to "Die Sammlung aller Beispiele, mit denen gearbeitet wird",
            "One-Hot-Kodierung" to "Umwandlung einer Kategorie in mehrere 0/1-Spalten, eine pro möglichem Wert",
            "Fehlende Werte" to "Leere Einträge in den Daten, die man löschen, auffüllen oder markieren muss",
            "Ausreißer" to "Datenpunkt, der extrem von den übrigen abweicht – Messfehler oder echter Extremfall"
        ) + g("Grundbegriffe", "ai0_model",
            "Modell" to "Eine Funktion mit lernbaren Parametern, die aus Eingaben Vorhersagen macht",
            "Parameter" to "Vom Training eingestellte Zahlen eines Modells, etwa Gewichte und Bias",
            "Hyperparameter" to "Vor dem Training vom Menschen festgelegte Einstellungen wie Lernrate oder Baumtiefe",
            "Verlustfunktion" to "Zahl, die misst, wie falsch die Vorhersagen sind; das Training minimiert sie",
            "Training" to "Das Einstellen der Parameter, sodass der Verlust auf den Trainingsdaten klein wird",
            "Inferenz" to "Die Anwendung eines fertig trainierten Modells auf neue Daten",
            "Gradient" to "Vektor der partiellen Ableitungen, zeigt in Richtung des steilsten Anstiegs des Verlusts",
            "Gradientenabstieg" to "Verfahren, das die Parameter schrittweise entgegen dem Gradienten verschiebt",
            "Lernrate" to "Schrittweite des Gradientenabstiegs; zu groß divergiert, zu klein ist langsam",
            "Epoche" to "Ein vollständiger Durchlauf durch alle Trainingsdaten",
            "Batch" to "Kleine Teilmenge der Trainingsdaten, aus der ein Gradientenschritt berechnet wird"
        ) + g("Lernarten", "ai0_types",
            "Überwachtes Lernen" to "Lernen aus Beispielen mit bekannter richtiger Antwort",
            "Unüberwachtes Lernen" to "Struktur in Daten finden, ohne dass Antworten vorgegeben sind",
            "Selbstüberwachtes Lernen" to "Die Labels entstehen automatisch aus den Daten, etwa das nächste Wort im Text",
            "Halbüberwachtes Lernen" to "Lernen aus wenigen markierten und vielen unmarkierten Beispielen",
            "Bestärkendes Lernen" to "Ein Agent lernt durch Ausprobieren und Belohnungen eine Handlungsstrategie"
        ) + g("Die sieben Techniken", "ml_techniques",
            "Regression" to "Vorhersage einer kontinuierlichen Zahl, etwa eines Preises",
            "Klassifikation" to "Vorhersage einer Kategorie, etwa Spam oder kein Spam",
            "Clustering" to "Ähnliche Beispiele ohne Labels zu Gruppen zusammenfassen",
            "Assoziationsregeln" to "Muster der Form ‚wer A kauft, kauft auch B‘ in Transaktionsdaten",
            "Anomalieerkennung" to "Ungewöhnliche, seltene Beobachtungen finden, etwa Kartenbetrug",
            "Sequenz-Mining" to "Muster in geordneten Folgen finden oder das nächste Element vorhersagen",
            "Empfehlungssystem" to "System, das Nutzern passende Objekte wie Filme oder Produkte vorschlägt",
            "Dimensionsreduktion" to "Viele Merkmale auf wenige aussagekräftige zusammenfassen"
        ) + g("Regression", "ml_regression",
            "Lineare Regression" to "Modell, das die Zielgröße als gewichtete Summe der Merkmale plus Konstante vorhersagt",
            "Multiple Regression" to "Lineare Regression mit mehreren Eingangsmerkmalen",
            "Polynomiale Regression" to "Regression mit Potenzen der Merkmale als zusätzliche Merkmale, für gekrümmte Zusammenhänge",
            "Methode der kleinsten Quadrate" to "Parameterwahl, die die Summe der quadrierten Abweichungen minimiert",
            "MAE" to "Mittlerer absoluter Fehler: durchschnittlicher Betrag der Abweichung",
            "MSE" to "Mittlerer quadratischer Fehler: Durchschnitt der quadrierten Abweichungen",
            "RMSE" to "Wurzel aus dem MSE, wieder in der Einheit der Zielgröße",
            "R² (Bestimmtheitsmaß)" to "Anteil der Streuung der Zielgröße, den das Modell erklärt",
            "Residuum" to "Differenz zwischen echtem Wert und Vorhersage eines Beispiels",
            "Multikollinearität" to "Starke Korrelation zwischen Merkmalen, die Koeffizienten instabil macht"
        ) + g("Klassifikation", "ml_classification",
            "Entscheidungsgrenze" to "Fläche im Merkmalsraum, an der die Vorhersage von einer Klasse zur anderen wechselt",
            "Binäre Klassifikation" to "Klassifikation mit genau zwei Klassen",
            "One-vs-Rest" to "Mehrklassenstrategie mit je einem Modell ‚Klasse k gegen alle anderen‘",
            "Schwelle" to "Wahrscheinlichkeitsgrenze, ab der ein Beispiel als positiv eingestuft wird",
            "Jaccard-Index" to "Größe der Schnittmenge geteilt durch Größe der Vereinigung von Vorhersage und Wahrheit",
            "Log-Loss" to "Kreuzentropie-Verlust, der sichere falsche Wahrscheinlichkeiten stark bestraft"
        ) + g("Algorithmen", "alg_knn",
            "k-nächste Nachbarn (KNN)" to "Klassifiziert nach der Mehrheit der k ähnlichsten Trainingsbeispiele",
            "Euklidische Distanz" to "Gerader Abstand zweier Punkte: Wurzel aus der Summe quadrierter Differenzen",
            "Fluch der Dimensionalität" to "In sehr vielen Dimensionen werden Daten dünn und Abstände verlieren ihre Aussagekraft"
        ) + g("Algorithmen", "alg_trees",
            "Entscheidungsbaum" to "Modell aus verschachtelten Ja/Nein-Fragen über die Merkmale",
            "Entropie" to "Maß für die Unordnung oder Unsicherheit einer Klassenverteilung",
            "Informationsgewinn" to "Abnahme der Entropie durch eine Aufteilung der Daten",
            "Gini-Unreinheit" to "Wahrscheinlichkeit, ein zufälliges Beispiel bei zufälligem Raten falsch einzuordnen",
            "Pruning" to "Beschneiden eines Baumes, um Überanpassung zu verringern",
            "Blatt" to "Endknoten eines Entscheidungsbaums, der eine Vorhersage enthält"
        ) + g("Algorithmen", "alg_ensembles",
            "Ensemble" to "Kombination vieler Modelle zu einer gemeinsamen Vorhersage",
            "Bagging" to "Viele Modelle auf Bootstrap-Stichproben trainieren und abstimmen lassen",
            "Random Forest" to "Ensemble von Entscheidungsbäumen mit zufälligen Stichproben und zufälliger Merkmalswahl",
            "Boosting" to "Modelle nacheinander trainieren, jedes korrigiert die Fehler der Vorgänger",
            "Gradient Boosting" to "Boosting, bei dem jedes neue Modell die Residuen des bisherigen Ensembles fittet",
            "XGBoost" to "Hochoptimierte Gradient-Boosting-Bibliothek, oft Sieger auf Tabellendaten",
            "Feature-Importance" to "Maß dafür, wie stark ein Merkmal zur Vorhersage beiträgt"
        ) + g("Algorithmen", "alg_logreg",
            "Logistische Regression" to "Lineares Modell, das über die Sigmoidfunktion Klassenwahrscheinlichkeiten ausgibt",
            "Sigmoidfunktion" to "S-förmige Funktion, die jede Zahl in das Intervall zwischen 0 und 1 abbildet",
            "Odds" to "Chance p/(1 − p), das Verhältnis von Eintritt zu Nichteintritt",
            "Logit" to "Logarithmus der Odds; bei der logistischen Regression linear in den Merkmalen"
        ) + g("Algorithmen", "alg_svm",
            "Support Vector Machine (SVM)" to "Klassifikator, der die Trennung mit dem größten Abstand zu beiden Klassen sucht",
            "Margin" to "Breite der ‚Straße‘ zwischen Entscheidungsgrenze und den nächsten Punkten",
            "Stützvektoren" to "Die Trainingspunkte am Rand der Margin, die die Lösung allein bestimmen",
            "Kernel-Trick" to "Skalarprodukte in einem höherdimensionalen Raum berechnen, ohne ihn auszurechnen"
        ) + g("Algorithmen", "alg_bayes",
            "Naive Bayes" to "Klassifikator nach dem Satz von Bayes mit der Annahme unabhängiger Merkmale",
            "Prior" to "Wahrscheinlichkeit einer Klasse, bevor man die Merkmale kennt",
            "Likelihood" to "Wahrscheinlichkeit der beobachteten Merkmale unter einer Klasse",
            "Laplace-Glättung" to "Zu jeder Zählung 1 addieren, damit keine Wahrscheinlichkeit exakt null wird"
        ) + g("Clustering", "alg_kmeans",
            "k-Means" to "Clustering, das Punkte dem nächsten von k Zentren zuordnet und Zentren auf Mittelwerte setzt",
            "Zentroid" to "Mittelpunkt eines Clusters, der Mittelwert seiner Punkte",
            "Inertia (WCSS)" to "Summe der quadrierten Abstände aller Punkte zu ihrem Clusterzentrum",
            "Ellbogenmethode" to "k dort wählen, wo die Inertia-Kurve über k deutlich abknickt",
            "Silhouettenkoeffizient" to "Maß dafür, wie gut ein Punkt zu seinem Cluster passt verglichen mit dem nächsten",
            "k-means++" to "Kluge Wahl weit auseinanderliegender Startzentren für k-Means"
        ) + g("Clustering", "alg_hier_dbscan",
            "Hierarchisches Clustering" to "Cluster schrittweise verschmelzen und als Stammbaum darstellen",
            "Dendrogramm" to "Baumdiagramm der Verschmelzungsschritte beim hierarchischen Clustering",
            "Linkage" to "Regel, wie der Abstand zweier Cluster gemessen wird",
            "DBSCAN" to "Dichtebasiertes Clustering, das beliebige Formen findet und Rauschpunkte markiert",
            "Kernpunkt" to "Punkt mit mindestens minPts Nachbarn im Radius ε"
        ) + g("Muster", "ml_association",
            "Support" to "Anteil aller Transaktionen, die eine bestimmte Artikelkombination enthalten",
            "Konfidenz" to "Anteil der Transaktionen mit A, die auch B enthalten",
            "Lift" to "Konfidenz geteilt durch die Häufigkeit von B; über 1 heißt mehr als Zufall",
            "Apriori-Algorithmus" to "Findet häufige Artikelmengen, indem nur häufige kleinere Mengen erweitert werden",
            "Itemset" to "Eine Menge von Artikeln, die gemeinsam in Transaktionen vorkommen kann"
        ) + g("Muster", "ml_anomaly",
            "z-Score" to "Abstand eines Wertes vom Mittelwert in Standardabweichungen",
            "Isolation Forest" to "Anomalieerkennung über zufällige Bäume; Anomalien sind schnell isoliert",
            "Rekonstruktionsfehler" to "Abweichung zwischen Eingabe und Rekonstruktion eines Autoencoders",
            "Kontextanomalie" to "Wert, der nur in seinem Zusammenhang ungewöhnlich ist"
        ) + g("Muster", "ml_sequence",
            "Markov-Kette" to "Modell, in dem der nächste Zustand nur vom aktuellen abhängt",
            "Zeitreihe" to "Folge von Messwerten mit Zeitstempel, bei der die Reihenfolge zählt",
            "Saisonalität" to "Regelmäßig wiederkehrendes Muster in einer Zeitreihe",
            "ARIMA" to "Klassisches Zeitreihenmodell aus Autoregression, Differenzenbildung und gleitendem Mittel"
        ) + g("Muster", "ml_recommender",
            "Inhaltsbasierte Empfehlung" to "Empfehlungen anhand der Merkmale von Objekten, die der Nutzer mochte",
            "Kollaboratives Filtern" to "Empfehlungen anhand der Bewertungen ähnlicher Nutzer",
            "Kaltstart-Problem" to "Für neue Nutzer oder Objekte gibt es noch keine Bewertungen",
            "Matrixfaktorisierung" to "Bewertungsmatrix als Produkt kleiner Nutzer- und Objekt-Faktorvektoren nähern",
            "Kosinusähnlichkeit" to "Kosinus des Winkels zwischen zwei Vektoren als Ähnlichkeitsmaß"
        ) + g("Muster", "ml_dimred",
            "PCA" to "Hauptkomponentenanalyse: Drehung auf die Richtungen größter Varianz",
            "Hauptkomponente" to "Richtung größter Streuung der Daten, ein Eigenvektor der Kovarianzmatrix",
            "Erklärte Varianz" to "Anteil der Gesamtstreuung, den eine Hauptkomponente abbildet",
            "t-SNE" to "Nichtlineares Verfahren, um hochdimensionale Daten in 2D zu visualisieren"
        ) + g("Bewertung", "ev_split",
            "Trainingsmenge" to "Daten, auf denen die Parameter gelernt werden",
            "Validierungsmenge" to "Daten zur Wahl von Hyperparametern und Modellen",
            "Testmenge" to "Daten für die einmalige, ehrliche Schlussbewertung",
            "Kreuzvalidierung" to "Mehrfach mit wechselnden Validierungsteilen trainieren und die Ergebnisse mitteln",
            "Stratifizierung" to "Aufteilen so, dass das Klassenverhältnis in jedem Teil gleich bleibt",
            "Datenleck" to "Information im Training, die beim echten Einsatz nicht verfügbar wäre"
        ) + g("Bewertung", "ev_metrics",
            "Konfusionsmatrix" to "Tabelle der richtig und falsch positiven und negativen Vorhersagen",
            "Accuracy" to "Anteil aller richtigen Vorhersagen",
            "Precision" to "Anteil der als positiv vorhergesagten Fälle, die wirklich positiv sind",
            "Recall" to "Anteil der wirklich positiven Fälle, die gefunden werden",
            "F1-Score" to "Harmonisches Mittel aus Precision und Recall",
            "ROC-Kurve" to "Trefferquote gegen Falsch-Positiv-Rate über alle Schwellen",
            "AUC" to "Fläche unter der ROC-Kurve; Wahrscheinlichkeit, dass ein Positives höher bewertet wird als ein Negatives",
            "Falsch positiv" to "Fehlalarm: als positiv vorhergesagt, aber in Wahrheit negativ",
            "Falsch negativ" to "Übersehen: als negativ vorhergesagt, aber in Wahrheit positiv"
        ) + g("Bewertung", "ev_features",
            "Standardisierung" to "Merkmal auf Mittelwert 0 und Standardabweichung 1 umrechnen",
            "Min-Max-Skalierung" to "Merkmal linear auf das Intervall von 0 bis 1 abbilden",
            "Imputation" to "Fehlende Werte durch geschätzte Werte ersetzen",
            "Feature Engineering" to "Neue, aussagekräftige Merkmale aus vorhandenen Daten bauen",
            "Pipeline" to "Verkettung von Vorverarbeitung und Modell, damit nichts aus den Testdaten durchsickert"
        ) + g("Bewertung", "ev_tuning",
            "Overfitting" to "Modell lernt Trainingsdetails und Rauschen auswendig und versagt bei neuen Daten",
            "Underfitting" to "Modell ist zu einfach und erfasst schon die Trainingsdaten schlecht",
            "Bias (Verzerrung)" to "Systematischer Fehler durch zu einfache Modellannahmen",
            "Varianz" to "Empfindlichkeit des Modells gegenüber zufälligen Schwankungen der Trainingsdaten",
            "Regularisierung" to "Strafterm oder Technik, die zu komplexe Modelle verhindert",
            "Grid Search" to "Alle Kombinationen eines Hyperparameter-Gitters systematisch ausprobieren",
            "Early Stopping" to "Training abbrechen, sobald der Validierungsfehler wieder steigt",
            "Lernkurve" to "Fehler in Abhängigkeit von der Anzahl der Trainingsbeispiele"
        ) + g("Bewertung", "ev_fair",
            "SMOTE" to "Erzeugt synthetische Beispiele der seltenen Klasse zwischen benachbarten Punkten",
            "SHAP-Werte" to "Faire Aufteilung einer Vorhersage auf die Beiträge der einzelnen Merkmale",
            "Permutation Importance" to "Wichtigkeit eines Merkmals als Gütverlust, wenn man die Spalte zufällig mischt"
        ) + g("Neuronale Netze", "dl_neuron",
            "Neuron" to "Recheneinheit, die eine gewichtete Summe ihrer Eingänge bildet und eine Aktivierung anwendet",
            "Gewicht" to "Faktor, mit dem ein Eingang in die Summe eines Neurons eingeht",
            "Bias (Neuron)" to "Konstanter Summand eines Neurons, der seine Schwelle verschiebt",
            "Aktivierungsfunktion" to "Nichtlineare Funktion, die auf die gewichtete Summe eines Neurons angewendet wird",
            "ReLU" to "Aktivierung max(0, z): negative Werte werden null, positive bleiben",
            "Perzeptron" to "Einfachstes lernendes Neuron mit Stufenfunktion als Aktivierung",
            "XOR-Problem" to "Zeigt, dass ein einzelnes lineares Neuron nicht jede Funktion lernen kann",
            "Softmax" to "Wandelt einen Vektor von Zahlen in Wahrscheinlichkeiten um, die sich zu 1 addieren"
        ) + g("Neuronale Netze", "dl_backprop_numbers",
            "Backpropagation" to "Berechnung aller Gradienten im Netz durch die Kettenregel, von hinten nach vorn",
            "Kettenregel" to "Die Ableitung einer Verkettung ist das Produkt der einzelnen Ableitungen",
            "Verschwindender Gradient" to "Gradienten werden über viele Schichten so klein, dass vordere Schichten kaum lernen",
            "Autograd" to "Automatisches Differenzieren, das den Rechengraphen speichert und Gradienten berechnet"
        ) + g("Neuronale Netze", "ai_training",
            "Adam" to "Optimierer mit gleitenden Mitteln von Gradient und quadriertem Gradient je Parameter",
            "Dropout" to "Zufälliges Abschalten von Neuronen im Training als Regularisierung",
            "Batch-Normalisierung" to "Normiert Aktivierungen je Batch, um Training zu stabilisieren",
            "Weight Decay" to "L2-Strafterm, der Gewichte in Richtung null zieht"
        ) + g("Neuronale Netze", "dl_keras",
            "Sequential-Modell" to "Keras-Modell, dessen Schichten einfach nacheinander gestapelt werden",
            "Dense-Schicht" to "Vollverbundene Schicht: jedes Eingangsneuron ist mit jedem Ausgangsneuron verbunden",
            "Callback" to "Funktion, die während des Trainings eingreift, etwa für Early Stopping"
        ) + g("Neuronale Netze", "dl_pytorch",
            "Tensor" to "Mehrdimensionales Zahlenfeld, die Grunddatenstruktur von Deep-Learning-Bibliotheken",
            "DataLoader" to "PyTorch-Hilfsmittel, das Daten in gemischte Batches zerlegt",
            "zero_grad" to "Löscht die alten Gradienten vor dem nächsten Rückwärtsschritt"
        ) + g("Architekturen", "dl_cnn",
            "Faltungsnetz (CNN)" to "Netz, das kleine Filter über Bilder schiebt und so lokale Muster erkennt",
            "Filter (Kernel)" to "Kleines Gewichtsfenster, das in einer Faltungsschicht über die Eingabe gleitet",
            "Feature Map" to "Ausgabe eines Filters: zeigt, wo sein Muster im Bild vorkommt",
            "Pooling" to "Verkleinert eine Feature Map, etwa durch Maximum über 2×2-Felder",
            "Padding" to "Auffüllen des Randes mit Nullen, damit die Größe erhalten bleibt",
            "Stride" to "Schrittweite, mit der ein Filter über die Eingabe wandert",
            "Datenaugmentation" to "Künstliche Vermehrung von Trainingsdaten durch Spiegeln, Drehen, Zuschneiden"
        ) + g("Architekturen", "dl_rnn",
            "RNN" to "Netz mit verstecktem Zustand, das Folgen Schritt für Schritt verarbeitet",
            "LSTM" to "Rekurrentes Netz mit Toren und Zellzustand gegen das Vergessen über lange Folgen",
            "GRU" to "Schlankere Variante des LSTM mit zwei Toren"
        ) + g("Architekturen", "dl_transfer",
            "Transfer Learning" to "Ein vortrainiertes Modell für eine neue Aufgabe weiterverwenden",
            "Feinabstimmung (Fine-Tuning)" to "Ein vortrainiertes Modell mit eigenen Daten und kleiner Lernrate weitertrainieren",
            "Einfrieren" to "Schichten vom Training ausnehmen, sodass ihre Gewichte unverändert bleiben"
        ) + g("Architekturen", "dl_autoencoder",
            "Autoencoder" to "Netz, das Eingaben komprimiert und wieder rekonstruiert",
            "Latenter Raum" to "Der komprimierte Darstellungsraum in der Mitte eines Autoencoders",
            "VAE" to "Autoencoder mit Wahrscheinlichkeitsverteilung im latenten Raum, kann neue Beispiele erzeugen"
        ) + g("Architekturen", "dl_generative",
            "GAN" to "Generator und Diskriminator, die gegeneinander trainiert werden",
            "Diffusionsmodell" to "Erzeugt Daten, indem es Rauschen Schritt für Schritt entfernt",
            "Mode Collapse" to "Ein GAN erzeugt nur noch wenige, immer gleiche Ausgaben"
        ) + g("Transformer und LLMs", "ai_transformer",
            "Transformer" to "Architektur aus Self-Attention und vollverbundenen Schichten, Basis moderner Sprachmodelle",
            "Self-Attention" to "Jede Position gewichtet alle anderen Positionen nach Relevanz und mischt deren Information",
            "Query, Key, Value" to "Die drei Projektionen, aus denen Attention-Gewichte und Ausgaben berechnet werden",
            "Positionskodierung" to "Information über die Reihenfolge der Token, die dem Transformer hinzugefügt wird",
            "Token" to "Texteinheit, die ein Sprachmodell verarbeitet, oft ein Wortteil",
            "Kontextfenster" to "Maximale Anzahl Token, die ein Modell auf einmal berücksichtigen kann"
        ) + g("Transformer und LLMs", "ai_llm",
            "Großes Sprachmodell (LLM)" to "Riesiges Transformer-Netz, das auf enormen Textmengen das nächste Token vorhersagen lernt",
            "Vortraining" to "Erstes, sehr teures Training eines Modells auf allgemeinen Daten",
            "Perplexität" to "Exponential des mittleren Kreuzentropie-Verlusts; wie ‚verwirrt‘ ein Sprachmodell ist",
            "Skalierungsgesetz" to "Empirischer Zusammenhang zwischen Modellgröße, Datenmenge, Rechenaufwand und Verlust",
            "Instruction-Tuning" to "Feinabstimmung auf Anweisung-Antwort-Paare, damit das Modell Aufgaben befolgt",
            "RLHF" to "Feinabstimmung mit bestärkendem Lernen anhand menschlicher Bewertungen",
            "LoRA" to "Feinabstimmung, die nur kleine Niedrigrang-Korrekturmatrizen lernt"
        ) + g("Transformer und LLMs", "ai_inference",
            "Temperatur" to "Regler, der die Verteilung beim Sampling schärft (klein) oder glättet (groß)",
            "Top-p-Sampling" to "Nur aus den wahrscheinlichsten Token wählen, die zusammen die Masse p ausmachen",
            "KV-Cache" to "Zwischenspeicher der Keys und Values bereits verarbeiteter Token",
            "Quantisierung" to "Gewichte mit weniger Bits speichern, um Speicher und Zeit zu sparen",
            "Halluzination" to "Plausibel klingende, aber falsche Ausgabe eines Sprachmodells"
        ) + g("Transformer und LLMs", "gen_prompting",
            "Prompt" to "Die Eingabe an ein Sprachmodell mit Anweisung und Kontext",
            "Few-Shot-Prompting" to "Einige Beispiele im Prompt zeigen das gewünschte Muster",
            "Chain of Thought" to "Das Modell schreibt Zwischenschritte auf, bevor es antwortet",
            "Systemprompt" to "Übergeordnete Anweisung, die Rolle und Regeln für ein ganzes Gespräch festlegt"
        ) + g("Transformer und LLMs", "gen_vector",
            "Embedding" to "Dichter Vektor, der die Bedeutung eines Wortes oder Textes darstellt",
            "Vektordatenbank" to "Datenbank für schnelle Ähnlichkeitssuche unter vielen Vektoren",
            "Chunking" to "Dokumente für die Suche in passend große Abschnitte zerlegen",
            "HNSW" to "Graphbasiertes Verfahren für schnelle approximative Nachbarsuche"
        ) + g("Transformer und LLMs", "ai_rag_agents",
            "RAG" to "Vor der Antwort passende Dokumente suchen und dem Modell als Kontext geben",
            "Agent" to "LLM, das in einer Schleife plant, Werkzeuge aufruft und Ergebnisse auswertet",
            "Funktionsaufruf" to "Das Modell gibt strukturiert an, welches Werkzeug mit welchen Argumenten laufen soll",
            "Prompt-Injection" to "Untergeschobener Text, der dem Modell fremde Anweisungen erteilen will"
        ) + g("Bestärkendes Lernen", "rl_basics",
            "Agent (RL)" to "Der Lernende, der in einer Umgebung Aktionen wählt",
            "Belohnung" to "Rückmeldung der Umgebung nach einer Aktion",
            "Policy" to "Strategie, die zu jedem Zustand die Aktion festlegt",
            "Diskontfaktor" to "Gewicht γ, mit dem spätere Belohnungen weniger zählen",
            "Exploration" to "Neue Aktionen ausprobieren, um vielleicht Besseres zu finden",
            "MDP" to "Markov-Entscheidungsprozess: formales Modell aus Zuständen, Aktionen, Übergängen und Belohnungen"
        ) + g("Bestärkendes Lernen", "rl_qlearning",
            "Q-Wert" to "Erwarteter künftiger Return, wenn man im Zustand s die Aktion a wählt",
            "Q-Learning" to "Lernt Q-Werte aus Erfahrung mit dem Ziel r + γ·max Q des Folgezustands",
            "Bellman-Gleichung" to "Rekursion: Wert = sofortige Belohnung + diskontierter Wert des Folgezustands",
            "ε-greedy" to "Mit Wahrscheinlichkeit ε zufällig handeln, sonst die beste bekannte Aktion"
        ) + g("Engineering", "eng_deploy",
            "Deployment" to "Ein Modell so bereitstellen, dass Anwendungen es nutzen können",
            "REST-API" to "Schnittstelle, über die Programme per HTTP Vorhersagen abfragen",
            "Docker" to "Verpackt Code, Bibliotheken und Modell in einen überall gleich laufenden Container",
            "ONNX" to "Offenes Austauschformat für trainierte Modelle"
        ) + g("Engineering", "eng_spark",
            "Apache Spark" to "System für verteilte Datenverarbeitung auf vielen Rechnern",
            "RDD" to "Robuste verteilte Datenstruktur von Spark",
            "Lazy Evaluation" to "Rechnungen werden erst geplant und bei einer Aktion ausgeführt",
            "Spark MLlib" to "Bibliothek für maschinelles Lernen auf verteilten Daten in Spark"
        ) + g("Engineering", "eng_mlops",
            "MLOps" to "Praktiken für reproduzierbares, automatisiertes und überwachtes ML im Betrieb",
            "Datendrift" to "Die Verteilung der Eingabedaten verändert sich im Betrieb",
            "Konzeptdrift" to "Der Zusammenhang zwischen Eingabe und Ziel ändert sich mit der Zeit",
            "Experiment-Tracking" to "Protokollieren von Parametern, Metriken und Artefakten jedes Trainingslaufs"
        )
    ).flatten()

    val groups: List<String> = terms.map { it.group }.distinct()

    /** Three distractor definitions (or terms) from the same group first, then from anywhere, never duplicates. */
    fun distractors(t: GlossaryTerm, r: Random, ofTerms: Boolean): List<String> {
        val pick: (GlossaryTerm) -> String = if (ofTerms) { x -> x.term } else { x -> x.definition }
        val same = terms.filter { it.group == t.group && it != t }.shuffled(r)
        val other = terms.filter { it.group != t.group }.shuffled(r)
        return (same + other).map(pick).distinct().filter { it != pick(t) }.take(3)
    }

    /** Two questions per term, with deterministic ids; options are shuffled later by the quiz engine. */
    val questions: List<Question> by lazy {
        val r = Random(4711)
        terms.flatMapIndexed { i, t ->
            val diff = when (t.group) { "Grundbegriffe", "Lernarten", "Die sieben Techniken" -> 1; "Transformer und LLMs", "Bestärkendes Lernen", "Engineering" -> 3; else -> 2 }
            listOf(
                Question("gl-$i-d", Topic.AI, diff, "Was bedeutet „${t.term}“?", "${t.term}: ${t.definition}.",
                    listOf(t.definition) + distractors(t, r, ofTerms = false), 0, chapterKey = t.chapterKey),
                Question("gl-$i-t", Topic.AI, diff, "Welcher Begriff passt? „${t.definition}“", "${t.term}: ${t.definition}.",
                    listOf(t.term) + distractors(t, r, ofTerms = true), 0, chapterKey = t.chapterKey)
            )
        }
    }
}

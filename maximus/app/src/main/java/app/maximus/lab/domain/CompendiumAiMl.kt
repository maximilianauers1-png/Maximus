package app.maximus.lab.domain

/**
 * "Machine Learning: Die sieben Techniken" – the technique map of IBM's "Machine Learning with Python":
 * regression/estimation, classification, clustering, associations, anomaly detection, sequence mining and
 * recommendation systems, plus dimensionality reduction as the eighth tool every course also teaches.
 */
internal object CompendiumAiMl {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "ML: Die sieben Techniken",
        chapter(
            "ml_techniques", T, 1, "Die Landkarte der sieben Techniken",
            "Regression, Klassifikation, Clustering, Assoziation, Anomalieerkennung, Sequenz-Mining, Empfehlungssysteme – und welche Technik zu welcher Frage passt.",
            emptyList(),
            sec("Sieben Fragen, sieben Techniken", """
                Fast jedes ML-Problem lässt sich einer von sieben Grundtechniken zuordnen. Merke dir die Frage, die jede beantwortet:

                • Regression (Schätzung): „Wie viel?“ – eine Zahl vorhersagen. CO₂-Ausstoß eines Autos, Hauspreis, Umsatz.
                • Klassifikation: „Welche Kategorie?“ – Kreditnehmer zahlt zurück ja/nein, Tumor gutartig/bösartig, Ziffer 0–9.
                • Clustering: „Welche Gruppen gibt es?“ – Kunden in Segmente einteilen, ohne die Segmente vorher zu kennen.
                • Assoziation: „Was tritt zusammen auf?“ – Warenkorbanalyse: Brot und Butter werden oft gemeinsam gekauft.
                • Anomalieerkennung: „Was ist ungewöhnlich?“ – Kreditkartenbetrug, defekte Sensoren, Netzwerkangriffe.
                • Sequenz-Mining: „Was kommt als Nächstes?“ – Klickpfade auf Webseiten, Kaufreihenfolgen, Zeitreihen.
                • Empfehlungssysteme: „Was gefällt dieser Person?“ – Filme, Produkte, Musik.

                Dazu kommt fast immer die Dimensionsreduktion: „Wie fasse ich viele Merkmale auf wenige zusammen?“ – als Vorverarbeitung oder zur Visualisierung.
            """),
            sec("Überwacht oder unüberwacht?", """
                • Überwacht (mit Labels): Regression, Klassifikation; Empfehlungen oft auch (es gibt Bewertungen als Label).
                • Unüberwacht (ohne Labels): Clustering, Assoziation, Dimensionsreduktion; Anomalieerkennung meist auch.
                • Sequenz-Mining kann beides sein: Muster finden (unüberwacht) oder das nächste Element vorhersagen (überwacht).
            """),
            sec("Entscheidungshilfe", """
                • Zielgröße ist eine Zahl → Regression.
                • Zielgröße ist eine Kategorie und es gibt Beispiele mit Antworten → Klassifikation.
                • Keine Zielgröße, ich will Gruppen sehen → Clustering.
                • Transaktionen (Warenkörbe) und die Frage „was hängt zusammen“ → Assoziation.
                • Seltene, auffällige Ereignisse, oft ohne Beispiele für jede Art von Fehler → Anomalieerkennung.
                • Die Reihenfolge der Daten ist wesentlich → Sequenz-Mining oder Zeitreihenmodell.
                • Nutzer, Objekte und Vorlieben → Empfehlungssystem.

                > Tipp für den IBM-Kurs: Lies die Aufgabe, unterstreiche die Zielgröße, frage „Zahl oder Schublade oder gar keine?“. Dann ist die Technik meist klar. Trainiere das im Spiel „Technik-Detektiv“ auf der AI-Spielwiese.
            """),
            sec("Beliebte Algorithmen je Technik", """
                • Regression: lineare, multiple, polynomiale Regression, Regressionsbäume, Random Forest, Gradient Boosting, neuronale Netze
                • Klassifikation: k-nächste Nachbarn (KNN), Entscheidungsbaum, logistische Regression, Support Vector Machine (SVM), Naive Bayes, neuronale Netze
                • Clustering: k-Means, hierarchisches Clustering, DBSCAN, Gaußsche Mischmodelle
                • Assoziation: Apriori, FP-Growth
                • Anomalien: z-Score, Isolation Forest, One-Class-SVM, Local Outlier Factor, Autoencoder
                • Sequenzen: GSP, PrefixSpan, Markov-Ketten, ARIMA, LSTM, Transformer
                • Empfehlungen: inhaltsbasiert, kollaboratives Filtern, Matrixfaktorisierung
                • Dimensionsreduktion: PCA, t-SNE, UMAP, Autoencoder
            """)
        ),
        chapter(
            "ml_regression", T, 1, "Regression: Zahlen vorhersagen",
            "Einfache, multiple, polynomiale und nichtlineare Regression, kleinste Quadrate, Koeffizienten deuten, Fehlermaße MAE, MSE, RMSE, R².",
            emptyList(),
            sec("Einfache lineare Regression", """
                Eine Eingangsgröße, eine Zielgröße, eine Gerade. Beispiel aus dem IBM-Kurs: CO₂-Ausstoß eines Autos aus der Motorgröße. Die Gerade ŷ = θ₀ + θ₁x wird so gelegt, dass die Summe der quadrierten senkrechten Abstände minimal ist (Methode der kleinsten Quadrate, OLS). Es gibt eine geschlossene Lösung:
            """,
                fm("Gerade", "ŷ = θ₀ + θ₁x"),
                fm("Steigung (OLS)", "θ₁ = Σ(xᵢ − x̄)(yᵢ − ȳ) / Σ(xᵢ − x̄)²"),
                fm("Achsenabschnitt", "θ₀ = ȳ − θ₁x̄")
            ),
            sec("Multiple lineare Regression", """
                Mehrere Merkmale: Motorgröße, Zylinder, Verbrauch. Das Modell ist eine Ebene bzw. Hyperebene. Jeder Koeffizient θⱼ sagt: Um wie viel ändert sich ŷ, wenn xⱼ um eins steigt und alle anderen Merkmale gleich bleiben. Achtung bei stark korrelierten Merkmalen (Multikollinearität): Dann werden Koeffizienten instabil und schwer zu deuten.
            """,
                fm("Multiple Regression", "ŷ = θ₀ + θ₁x₁ + … + θ_dx_d = θᵀx"),
                fm("Normalengleichung", "θ = (XᵀX)⁻¹Xᵀy")
            ),
            sec("Polynomiale und nichtlineare Regression", """
                Ist der Zusammenhang gekrümmt, fügt man Potenzen als neue Merkmale hinzu: x, x², x³. Das Modell bleibt linear in den Parametern, deshalb funktioniert derselbe Algorithmus. Mit zu hohem Grad passt die Kurve jedes Rauschen an (Overfitting) – siehe Spiel „Überanpassung“. Echte nichtlineare Modelle (z. B. exponentielles Wachstum ŷ = a·e^{bx}, logistische Kurven) fittet man iterativ, wie im Praktikum mit curve_fit.

                $ from sklearn.preprocessing import PolynomialFeatures
                $ from sklearn.pipeline import make_pipeline
                $ model = make_pipeline(PolynomialFeatures(degree=3), LinearRegression())
                $ model.fit(X_train, y_train)
            """,
                fm("Polynom-Regression", "ŷ = θ₀ + θ₁x + θ₂x² + … + θ_kx^k")
            ),
            sec("Wie gut ist die Regression? Die Fehlermaße", """
                • MAE (mittlerer absoluter Fehler): durchschnittlich so viele Einheiten daneben. Leicht zu deuten, robust gegen Ausreißer.
                • MSE (mittlerer quadratischer Fehler): bestraft große Fehler stark; das, was beim Training minimiert wird.
                • RMSE: Wurzel aus MSE, wieder in der Einheit der Zielgröße.
                • RAE / RSE: relativer absoluter bzw. quadratischer Fehler – Fehler im Verhältnis zum „immer den Mittelwert vorhersagen“.
                • R² (Bestimmtheitsmaß): Anteil der Streuung, den das Modell erklärt. 1 = perfekt, 0 = so gut wie der Mittelwert, negativ = schlechter als der Mittelwert.
            """,
                fm("MAE", "MAE = (1/N) Σ |yᵢ − ŷᵢ|"),
                fm("MSE", "MSE = (1/N) Σ (yᵢ − ŷᵢ)²"),
                fm("RMSE", "RMSE = √MSE"),
                fm("RAE", "RAE = Σ|yᵢ − ŷᵢ| / Σ|yᵢ − ȳ|"),
                fm("RSE", "RSE = Σ(yᵢ − ŷᵢ)² / Σ(yᵢ − ȳ)²"),
                fm("Bestimmtheitsmaß", "R² = 1 − RSE = 1 − SS_res/SS_tot")
            ),
            sec("Annahmen und Fallstricke", """
                • Linearität: Der Zusammenhang muss (nach Transformation) linear sein – Residuen gegen ŷ plotten; sieht man Muster, fehlt etwas.
                • Ausreißer ziehen die Gerade stark, weil Fehler quadriert werden.
                • Extrapolation ist gefährlich: Außerhalb des Trainingsbereichs gilt das Modell nicht.
                • Korrelation ist keine Kausalität: Ein großer Koeffizient heißt nicht, dass das Merkmal die Ursache ist.
            """)
        ),
        chapter(
            "ml_classification", T, 1, "Klassifikation: Schubladen vorhersagen",
            "Binär und mehrklassig, Entscheidungsgrenzen, die fünf Klassiker KNN, Entscheidungsbaum, logistische Regression, SVM, Naive Bayes, One-vs-Rest.",
            emptyList(),
            sec("Die Idee", """
                Ein Klassifikator ordnet jedem Beispiel eine Klasse zu. Im Merkmalsraum zieht er Entscheidungsgrenzen: Auf der einen Seite „Spam“, auf der anderen „kein Spam“. Lineare Klassifikatoren ziehen gerade Grenzen (Ebenen), andere können beliebig gekrümmte Grenzen lernen.

                • Binär: zwei Klassen (ja/nein, 0/1).
                • Mehrklassig (multiclass): mehr als zwei Klassen, genau eine ist richtig (Ziffer 0–9).
                • Mehrfach-Label (multilabel): mehrere Klassen gleichzeitig möglich (ein Foto zeigt Hund und Katze).
            """),
            sec("Die fünf Klassiker auf einen Blick", """
                • k-nächste Nachbarn (KNN): Schau dir die k ähnlichsten Trainingsbeispiele an und nimm die Mehrheitsklasse. Kein echtes Training, aber langsame Vorhersage.
                • Entscheidungsbaum: Eine Folge von Ja/Nein-Fragen („Alter > 40?“). Leicht zu erklären, neigt zu Overfitting.
                • Logistische Regression: lineare Grenze, gibt Wahrscheinlichkeiten aus. Schnell, gut deutbar, starke Basislinie.
                • Support Vector Machine (SVM): sucht die Grenze mit dem größten Sicherheitsabstand; mit Kernels auch gekrümmt.
                • Naive Bayes: Wahrscheinlichkeiten nach dem Satz von Bayes, mit der „naiven“ Annahme unabhängiger Merkmale. Sehr schnell, gut für Text.

                Jeden dieser Algorithmen gibt es im Kurs „ML-Algorithmen im Detail“ ausführlich.
            """),
            sec("Mehr als zwei Klassen", """
                Manche Algorithmen können von Natur aus mehrere Klassen (KNN, Bäume, Naive Bayes, Softmax-Regression). Binäre Verfahren wie SVM erweitert man:

                • One-vs-Rest (OvR): je ein Klassifikator „Klasse k gegen alle anderen“; es gewinnt der sicherste. K Modelle.
                • One-vs-One (OvO): je ein Klassifikator pro Klassenpaar; Mehrheitsabstimmung. K(K−1)/2 Modelle.
            """,
                fm("Anzahl Modelle One-vs-One", "K(K − 1)/2"),
                fm("Softmax", "P(y = k | x) = e^{zₖ} / Σⱼ e^{zⱼ}")
            ),
            sec("Wahrscheinlichkeit und Schwelle", """
                Viele Klassifikatoren liefern eine Wahrscheinlichkeit p für die positive Klasse. Die Entscheidung entsteht erst durch eine Schwelle (Standard 0,5). Senkt man sie, findet man mehr Positive (höherer Recall), aber auch mehr Fehlalarme (niedrigere Precision). Wo die Schwelle liegt, ist eine Geschäftsentscheidung: Bei Krebsdiagnostik lieber ein Fehlalarm zu viel. Probier es im Spiel „Schwellen-Regler“.

                $ proba = model.predict_proba(X_test)[:, 1]
                $ y_pred = (proba > 0.3).astype(int)   # eigene Schwelle
            """),
            sec("Bewertung von Klassifikatoren (Kurzfassung)", """
                • Accuracy: Anteil richtig – täuscht bei seltenen Klassen.
                • Konfusionsmatrix: zählt TP, FP, TN, FN.
                • Precision, Recall, F1: siehe Kapitel „Metriken“.
                • Jaccard-Index: Überlapp von vorhergesagter und wahrer Menge.
                • Log-Loss: bestraft sichere Fehlvorhersagen – bewertet die Wahrscheinlichkeiten, nicht nur die Klassen.
            """,
                fm("Jaccard-Index", "J(y, ŷ) = |y ∩ ŷ| / |y ∪ ŷ|"),
                fm("Log-Loss", "LL = −(1/N) Σ [yᵢ ln pᵢ + (1 − yᵢ) ln(1 − pᵢ)]")
            )
        ),
        chapter(
            "ml_clustering", T, 1, "Clustering: Gruppen entdecken",
            "Ähnlichkeit und Distanz, k-Means, hierarchisches Clustering, DBSCAN, wie viele Cluster, Anwendungen wie Kundensegmentierung.",
            emptyList(),
            sec("Was ist ein Cluster?", """
                Ein Cluster ist eine Gruppe von Beispielen, die einander ähnlicher sind als den Beispielen anderer Gruppen. Es gibt keine Labels – das Verfahren entdeckt die Gruppen selbst. Was „ähnlich“ heißt, bestimmt das Distanzmaß.

                • Kundensegmentierung: Vielkäufer, Schnäppchenjäger, Gelegenheitskunden
                • Biologie: Gene mit ähnlichem Ausdrucksmuster
                • Dokumente nach Themen gruppieren
                • Bildkompression: Farben auf k Clusterzentren reduzieren
            """,
                fm("Euklidische Distanz", "d(x, z) = √(Σⱼ (xⱼ − zⱼ)²)"),
                fm("Manhattan-Distanz", "d(x, z) = Σⱼ |xⱼ − zⱼ|")
            ),
            sec("Drei Familien", """
                • Partitionierend (k-Means): Man gibt k vor; jedes Beispiel gehört genau zu einem Zentrum. Schnell, für kugelförmige, ähnlich große Cluster.
                • Hierarchisch (agglomerativ): Jeder Punkt startet als eigener Cluster, die zwei nächsten werden immer wieder verschmolzen. Ergebnis: ein Baum (Dendrogramm), den man auf beliebiger Höhe schneidet.
                • Dichtebasiert (DBSCAN): Cluster sind dichte Regionen, getrennt durch dünne. Findet beliebige Formen und markiert Rauschpunkte als Ausreißer; k muss man nicht vorgeben.
            """),
            sec("k-Means in vier Sätzen", """
                Wähle k zufällige Zentren. Ordne jeden Punkt dem nächsten Zentrum zu. Setze jedes Zentrum auf den Mittelwert seiner Punkte. Wiederhole, bis sich nichts mehr ändert. Das minimiert die Summe der quadrierten Abstände zum eigenen Zentrum (Inertia, WCSS). Spiele es selbst durch: AI-Spielwiese → „k-Means“.
            """,
                fm("Inertia (WCSS)", "J = Σₖ Σ_{x∈Cₖ} |x − μₖ|²"),
                fm("Zentrums-Update", "μₖ = (1/|Cₖ|) Σ_{x∈Cₖ} x")
            ),
            sec("Wie viele Cluster?", """
                • Ellbogenmethode: Inertia gegen k auftragen; wo die Kurve „abknickt“, lohnt sich ein weiteres Cluster kaum noch.
                • Silhouettenkoeffizient: für jeden Punkt (b − a)/max(a, b), a = mittlerer Abstand zum eigenen Cluster, b = zum nächsten fremden. Nahe 1 = gut getrennt.
                • Fachwissen: Oft gibt das Problem k vor (drei Kundenstufen).

                > Skalieren nicht vergessen! Ohne Standardisierung dominiert das Merkmal mit den größten Zahlen (Einkommen in € gegen Alter in Jahren).
            """,
                fm("Silhouette", "s = (b − a) / max(a, b)")
            )
        ),
        chapter(
            "ml_association", T, 1, "Assoziation: Was gehört zusammen?",
            "Warenkorbanalyse, Itemsets, Support, Konfidenz, Lift, Apriori-Prinzip, FP-Growth, Anwendungen und Fallstricke.",
            emptyList(),
            sec("Warenkorbanalyse", """
                Ein Supermarkt hat Millionen Kassenbons (Transaktionen). Jede ist eine Menge von Artikeln (Items). Gesucht sind Regeln der Form „Wer A kauft, kauft auch B“ (A ⇒ B). Daraus folgen Regalplanung, Bundles, Empfehlungen. Dieselbe Methode findet zusammen auftretende Symptome, Fehlermeldungen oder Webseitenbesuche.
            """),
            sec("Support, Konfidenz, Lift", """
                • Support: Wie häufig kommt die Kombination überhaupt vor? Seltene Regeln sind uninteressant.
                • Konfidenz: Wenn A im Korb ist, wie oft ist dann auch B drin? Eine bedingte Wahrscheinlichkeit.
                • Lift: Ist das mehr als Zufall? Lift > 1 heißt: A und B treten öfter zusammen auf, als wenn sie unabhängig wären. Lift = 1: unabhängig. Lift < 1: schließen sich eher aus.

                Beispiel: 1000 Körbe, 100 mit Windeln, 80 mit Bier, 40 mit beidem. Support(Windeln ⇒ Bier) = 4 %, Konfidenz = 40/100 = 40 %, Lift = 0,4 / 0,08 = 5.
            """,
                fm("Support", "supp(A ⇒ B) = |Körbe mit A und B| / |alle Körbe|"),
                fm("Konfidenz", "conf(A ⇒ B) = supp(A ∪ B) / supp(A)"),
                fm("Lift", "lift(A ⇒ B) = conf(A ⇒ B) / supp(B)")
            ),
            sec("Der Apriori-Algorithmus", """
                Alle Kombinationen durchzuprobieren ist bei tausenden Artikeln unmöglich (2ⁿ Teilmengen). Das Apriori-Prinzip rettet: Jede Teilmenge einer häufigen Menge ist ebenfalls häufig – umgekehrt kann eine Menge, die eine seltene Teilmenge enthält, nicht häufig sein. Also: erst häufige Einzelartikel finden, daraus Paare bilden, nur häufige Paare zu Tripeln erweitern, und so weiter. Aus den häufigen Mengen erzeugt man dann Regeln mit hoher Konfidenz. FP-Growth erreicht dasselbe ohne Kandidatengenerierung über einen kompakten Präfixbaum und ist meist schneller.

                $ from mlxtend.frequent_patterns import apriori, association_rules
                $ freq = apriori(onehot_df, min_support=0.02, use_colnames=True)
                $ rules = association_rules(freq, metric="lift", min_threshold=1.2)
            """,
                fm("Apriori-Prinzip", "X ⊆ Y ⇒ supp(X) ≥ supp(Y)")
            ),
            sec("Fallstricke", """
                • Hohe Konfidenz kann täuschen, wenn B ohnehin in fast jedem Korb liegt (Milch) – deshalb Lift prüfen.
                • Assoziation ist keine Kausalität.
                • Bei sehr vielen Regeln: nach Lift und Support filtern, Fachleute draufschauen lassen.
            """)
        ),
        chapter(
            "ml_anomaly", T, 2, "Anomalieerkennung: das Ungewöhnliche finden",
            "Punkt-, Kontext- und Kollektivanomalien, z-Score und IQR, Isolation Forest, One-Class-SVM, Local Outlier Factor, Autoencoder, Bewertung bei seltenen Ereignissen.",
            emptyList(),
            sec("Arten von Anomalien", """
                • Punktanomalie: ein einzelner Wert ist absurd – eine Kartenzahlung über 9000 € bei sonst 30 €.
                • Kontextanomalie: der Wert ist nur in seinem Zusammenhang seltsam – 30 °C im Januar in Berlin.
                • Kollektivanomalie: eine Folge ist auffällig, einzelne Werte nicht – ein EKG-Abschnitt mit falschem Rhythmus.

                Anomalien sind selten und vielfältig, oft gibt es kaum Beispiele mit Label. Deshalb lernt man meist, wie „normal“ aussieht, und meldet alles, was davon stark abweicht.
            """),
            sec("Statistische Methoden", """
                • z-Score: Wie viele Standardabweichungen liegt ein Wert vom Mittelwert? |z| > 3 ist bei Normalverteilung sehr selten (0,27 %).
                • IQR-Regel (robust, Boxplot): Ausreißer liegen unterhalb Q1 − 1,5·IQR oder oberhalb Q3 + 1,5·IQR, mit IQR = Q3 − Q1.
                • Für mehrere Merkmale: Mahalanobis-Distanz berücksichtigt Korrelationen.
            """,
                fm("z-Score", "z = (x − μ)/σ"),
                fm("IQR-Grenzen", "x < Q1 − 1,5·IQR  oder  x > Q3 + 1,5·IQR"),
                fm("Mahalanobis-Distanz", "D² = (x − μ)ᵀ Σ⁻¹ (x − μ)")
            ),
            sec("ML-Methoden", """
                • Isolation Forest: Zufällige Bäume schneiden die Daten auf. Anomalien sind „leicht zu isolieren“ und landen nach wenigen Schnitten allein – kurze Pfadlänge = verdächtig.
                • One-Class-SVM: lernt eine Hülle um die normalen Daten.
                • Local Outlier Factor (LOF): vergleicht die lokale Dichte eines Punktes mit der seiner Nachbarn.
                • Autoencoder: ein Netz, das Daten komprimiert und rekonstruiert. Auf normalen Daten trainiert, rekonstruiert es Anomalien schlecht – großer Rekonstruktionsfehler = Alarm.
                • DBSCAN markiert Punkte in dünnen Regionen direkt als Rauschen.

                $ from sklearn.ensemble import IsolationForest
                $ iso = IsolationForest(contamination=0.01, random_state=0).fit(X)
                $ flags = iso.predict(X)        # −1 = Anomalie, 1 = normal
            """,
                fm("Rekonstruktionsfehler", "e(x) = |x − g(f(x))|²")
            ),
            sec("Bewertung bei seltenen Ereignissen", """
                Accuracy ist hier nutzlos: Bei 0,1 % Betrug ist „nie Alarm“ zu 99,9 % richtig. Man schaut auf Recall (wie viel Betrug wird gefunden), Precision (wie viele Alarme stimmen) und die Precision-Recall-Kurve. Die Alarmschwelle richtet sich nach den Kosten: Ein übersehener Betrug kostet Geld, ein Fehlalarm einen genervten Kunden.
            """)
        ),
        chapter(
            "ml_sequence", T, 2, "Sequenz-Mining und Zeitreihen",
            "Sequentielle Muster (GSP, PrefixSpan), Markov-Ketten, Zeitreihenkomponenten, gleitender Mittelwert, AR/ARIMA, LSTM, zeitlich korrektes Validieren.",
            emptyList(),
            sec("Wenn die Reihenfolge zählt", """
                Bei Sequenzdaten ist die Reihenfolge Teil der Information: Klickpfade, Kaufhistorien, DNA, Sätze, Sensordaten. Zwei Fragestellungen:

                • Sequentielle Muster finden: „Wer Zelt, dann Schlafsack kauft, kauft innerhalb eines Monats oft eine Isomatte.“ Algorithmen: GSP, PrefixSpan, SPADE – wie Apriori, aber mit Reihenfolge.
                • Das Nächste vorhersagen: nächster Klick, nächstes Wort, nächster Messwert.
            """),
            sec("Markov-Ketten", """
                Das einfachste Vorhersagemodell: Die Wahrscheinlichkeit des nächsten Zustands hängt nur vom aktuellen ab. Man schätzt eine Übergangsmatrix aus gezählten Übergängen. Ein Bigramm-Sprachmodell ist eine Markov-Kette über Wörter; große Sprachmodelle verallgemeinern das auf Tausende vorherige Token.
            """,
                fm("Markov-Eigenschaft", "P(x_{t+1} | x_t, …, x_1) = P(x_{t+1} | x_t)"),
                fm("Übergangswahrscheinlichkeit", "P(j | i) = n(i → j) / n(i)")
            ),
            sec("Zeitreihen", """
                Eine Zeitreihe zerlegt man gedanklich in Trend (langfristige Richtung), Saisonalität (wiederkehrende Muster: Wochentag, Jahreszeit) und Rest (Rauschen). Einfache Prognosen:

                • Naiv: morgen = heute.
                • Gleitender Mittelwert: Mittel der letzten k Werte.
                • Exponentielle Glättung: neuere Werte zählen mehr.
                • AR(p): linearer Fit auf die letzten p Werte; ARIMA ergänzt Differenzenbildung (gegen Trend) und Fehlerterme.
                • Deep Learning: LSTM, temporale Faltungen, Transformer.
            """,
                fm("Gleitender Mittelwert", "ŷ_{t+1} = (1/k) Σ_{i=0}^{k−1} y_{t−i}"),
                fm("Exponentielle Glättung", "s_t = α y_t + (1 − α) s_{t−1}"),
                fm("AR(p)", "y_t = c + Σ_{i=1}^{p} φᵢ y_{t−i} + ε_t")
            ),
            sec("Der größte Fehler: in die Zukunft schauen", """
                Bei Zeitreihen darf man nicht zufällig in Training und Test teilen – sonst lernt das Modell aus der Zukunft. Immer zeitlich teilen: trainieren auf der Vergangenheit, testen auf dem späteren Zeitraum. Für Kreuzvalidierung nimmt man „rollierende Fenster“ (TimeSeriesSplit).

                $ from sklearn.model_selection import TimeSeriesSplit
                $ for train_idx, test_idx in TimeSeriesSplit(n_splits=5).split(X):
                $     ...  # Test liegt immer nach dem Training
            """)
        ),
        chapter(
            "ml_recommender", T, 2, "Empfehlungssysteme",
            "Inhaltsbasierte Empfehlungen, kollaboratives Filtern (nutzer- und objektbasiert), Kosinusähnlichkeit, Matrixfaktorisierung, Kaltstart, Bewertung.",
            emptyList(),
            sec("Zwei Grundideen", """
                • Inhaltsbasiert (content-based): „Du mochtest Actionfilme mit Keanu Reeves – hier ist noch einer.“ Man beschreibt Objekte durch Merkmale (Genre, Schauspieler) und baut aus deinen Bewertungen ein Nutzerprofil. Empfohlen wird, was dem Profil am ähnlichsten ist.
                • Kollaboratives Filtern: „Menschen wie du mochten auch …“ Man braucht keine Merkmale der Objekte, nur die Bewertungsmatrix Nutzer × Objekt.

                In der Praxis kombiniert man beides (hybride Systeme).
            """),
            sec("Kollaboratives Filtern", """
                • Nutzerbasiert: Finde Nutzer, deren Bewertungen deinen ähneln, und gewichte deren Bewertungen eines Films mit der Ähnlichkeit.
                • Objektbasiert: Finde Filme, die von denselben Leuten ähnlich bewertet wurden wie Filme, die du mochtest. Stabiler, weil sich Filmähnlichkeiten seltener ändern als Nutzergeschmack.

                Ähnlichkeit misst man oft mit der Kosinusähnlichkeit der Bewertungsvektoren oder der Pearson-Korrelation (zieht persönliche Strenge ab).
            """,
                fm("Kosinusähnlichkeit", "sim(u, v) = (u·v) / (|u||v|)"),
                fm("Gewichtete Vorhersage", "r̂_{u,i} = Σ_v sim(u, v)·r_{v,i} / Σ_v |sim(u, v)|")
            ),
            sec("Matrixfaktorisierung", """
                Die Bewertungsmatrix R ist riesig und fast leer (jeder sieht nur wenige Filme). Man nähert sie als Produkt zweier schmaler Matrizen: Jeder Nutzer und jeder Film bekommt einen Vektor mit k versteckten Faktoren (z. B. „wie actionlastig“, „wie romantisch“). Die vorhergesagte Bewertung ist das Skalarprodukt. Gelernt wird nur auf den bekannten Einträgen, mit Regularisierung. Das gewann den Netflix-Preis und ist der Vorläufer heutiger Embedding-Methoden.
            """,
                fm("Faktorisierung", "R ≈ P Qᵀ, r̂_{u,i} = p_u · q_i"),
                fm("Verlust", "L = Σ_{(u,i) bekannt} (r_{u,i} − p_u·q_i)² + λ(|p_u|² + |q_i|²)")
            ),
            sec("Probleme aus der Praxis", """
                • Kaltstart: neue Nutzer oder Objekte haben keine Bewertungen → inhaltsbasiert oder beliebte Objekte zeigen.
                • Spärlichkeit: 99 % der Matrix sind leer.
                • Filterblase und Popularitätsverzerrung: Es wird immer mehr vom Gleichen empfohlen.
                • Bewertung: offline mit RMSE oder Precision@k, online mit A/B-Tests (Klickrate, Verweildauer).
            """,
                fm("Precision@k", "P@k = (relevante unter den Top-k) / k")
            )
        ),
        chapter(
            "ml_dimred", T, 2, "Dimensionsreduktion",
            "Fluch der Dimensionalität, Merkmalsauswahl gegen Merkmalsextraktion, PCA mit Eigenvektoren, erklärte Varianz, t-SNE und UMAP zur Visualisierung.",
            emptyList(),
            sec("Warum weniger Dimensionen?", """
                Mit vielen Merkmalen werden Daten im Raum dünn: Das Volumen wächst exponentiell, alle Punkte sind weit voneinander entfernt, Distanzen verlieren ihre Aussagekraft (Fluch der Dimensionalität). Weniger Dimensionen bedeuten schnelleres Training, weniger Overfitting, und bei zwei oder drei Dimensionen kann man die Daten ansehen.

                • Merkmalsauswahl: unwichtige Spalten weglassen (Korrelation, Feature-Importance, L1).
                • Merkmalsextraktion: neue, kombinierte Merkmale berechnen (PCA, Autoencoder).
            """),
            sec("Hauptkomponentenanalyse (PCA)", """
                PCA dreht das Koordinatensystem so, dass die erste Achse in Richtung der größten Streuung zeigt, die zweite senkrecht dazu in Richtung der zweitgrößten, und so weiter. Mathematisch: die Eigenvektoren der Kovarianzmatrix, sortiert nach ihren Eigenwerten. Man behält die ersten k Achsen. Für Physiker: Das ist die Hauptachsentransformation des Trägheitstensors – nur für eine Datenwolke.
            """,
                fm("Kovarianzmatrix", "C = (1/N) Σᵢ (xᵢ − μ)(xᵢ − μ)ᵀ"),
                fm("Hauptachsen", "C vₖ = λₖ vₖ"),
                fm("Erklärte Varianz", "Anteil_k = λₖ / Σⱼ λⱼ"),
                fm("Projektion", "z = Vₖᵀ (x − μ)")
            ),
            sec("Wie viele Komponenten?", """
                Man trägt die kumulierte erklärte Varianz auf und nimmt so viele Komponenten, dass z. B. 95 % erreicht sind. Vorher unbedingt standardisieren, sonst dominieren Merkmale mit großen Zahlen.

                $ from sklearn.decomposition import PCA
                $ pca = PCA(n_components=0.95)           # so viele wie für 95 % Varianz nötig
                $ Z = pca.fit_transform(X_scaled)
                $ print(pca.explained_variance_ratio_)
            """),
            sec("t-SNE und UMAP", """
                PCA ist linear. Für die Visualisierung komplizierter Daten (Bilder, Embeddings) nutzt man nichtlineare Verfahren: t-SNE und UMAP versuchen, Nachbarschaften zu erhalten – was hochdimensional nah ist, soll in 2D nah bleiben. Ideal zum Anschauen von Clustern, aber Abstände zwischen weit entfernten Gruppen und Clustergrößen sind in diesen Bildern nicht deutbar.
            """)
        )
    )
}

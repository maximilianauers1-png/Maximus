package app.maximus.lab.domain

/**
 * The classic algorithms in depth (one per bite, with a worked number example each) and the craft of
 * evaluation and data preparation.
 */
internal object CompendiumAiAlgo {
    private val T = Topic.AI

    val chapters: List<Chapter> = course(
        "ML-Algorithmen im Detail",
        chapter(
            "alg_knn", T, 2, "k-nächste Nachbarn (KNN)",
            "Abstimmung der Nachbarn, Distanzmaße, Wahl von k, Skalierung, gewichtete Stimmen, KNN-Regression, Rechenaufwand.",
            emptyList(),
            sec("Der faulste Lerner der Welt", """
                KNN „lernt“ nichts: Beim Training speichert er alle Beispiele. Für ein neues Beispiel sucht er die k ähnlichsten gespeicherten (Nachbarn) und lässt sie abstimmen. Bei Regression nimmt er den Mittelwert ihrer Zielwerte. IBM-Beispiel: Telefonanbieter sagt aus Alter, Einkommen und Wohnort voraus, welches Tarifpaket ein Kunde wählt.

                Zahlenbeispiel: k = 3, die drei nächsten Nachbarn haben die Klassen A, A, B → Vorhersage A (2 : 1).
            """,
                fm("Klassifikation", "ŷ = Mehrheitsklasse unter den k nächsten Nachbarn"),
                fm("Regression", "ŷ = (1/k) Σ_{i∈N_k(x)} yᵢ"),
                fm("Distanzgewichtete Stimme", "wᵢ = 1 / d(x, xᵢ)")
            ),
            sec("Wie wählt man k?", """
                • k = 1: Die Grenze folgt jedem einzelnen Punkt, auch Rauschen → Overfitting, hohe Varianz.
                • k sehr groß: Alles wird zur Mehrheitsklasse geglättet → Underfitting, hoher Bias.
                • Praxis: verschiedene k auf Validierungsdaten testen, das beste nehmen; bei zwei Klassen ungerade k gegen Gleichstand.
            """),
            sec("Ohne Skalierung geht es schief", """
                KNN misst Distanzen. Ist das Einkommen in Euro (Zehntausende) und das Alter in Jahren (Zehner), bestimmt praktisch nur das Einkommen die Distanz. Deshalb jedes Merkmal standardisieren (z = (x − μ)/σ) oder auf [0, 1] skalieren. Außerdem leidet KNN am Fluch der Dimensionalität: In hundert Dimensionen sind alle Punkte ungefähr gleich weit entfernt.

                $ from sklearn.neighbors import KNeighborsClassifier
                $ for k in range(1, 15):
                $     knn = make_pipeline(StandardScaler(), KNeighborsClassifier(n_neighbors=k)).fit(X_train, y_train)
                $     print(k, knn.score(X_val, y_val))
            """),
            sec("Stärken und Schwächen", """
                • Stärken: einfach, keine Trainingszeit, beliebige Entscheidungsgrenzen, mehrklassig von Natur aus.
                • Schwächen: langsame Vorhersage (Abstand zu allen N Punkten, O(N·d)), viel Speicher, empfindlich gegen Skalierung und irrelevante Merkmale.
                • Beschleunigung: KD-Bäume, Ball-Trees, approximative Nachbarsuche (dieselbe Technik wie in Vektordatenbanken für RAG).
            """)
        ),
        chapter(
            "alg_trees", T, 2, "Entscheidungsbäume",
            "Fragen-Bäume, Entropie, Informationsgewinn, Gini-Unreinheit, Aufteilen numerischer Merkmale, Tiefe und Pruning, Regressionsbäume.",
            emptyList(),
            sec("Zwanzig Fragen", """
                Ein Entscheidungsbaum ist ein Flussdiagramm aus Ja/Nein-Fragen über die Merkmale. Jeder innere Knoten testet ein Merkmal („Blutdruck hoch?“), jeder Ast ist eine Antwort, jedes Blatt eine Vorhersage. IBM-Beispiel: Welches von zwei Medikamenten passt zu einem Patienten, abhängig von Alter, Geschlecht, Blutdruck, Cholesterin.

                Der Baum wird gierig (greedy) von oben gebaut: An jedem Knoten nimmt man die Frage, die die Daten am saubersten trennt. Ein Knoten ist „rein“, wenn alle Beispiele darin dieselbe Klasse haben.
            """),
            sec("Entropie und Informationsgewinn", """
                Entropie misst die Unordnung eines Knotens – dieselbe Formel wie in der statistischen Physik und Informationstheorie. Ein Knoten mit 50 % / 50 % hat Entropie 1 Bit (maximal unsicher), ein reiner Knoten 0. Der Informationsgewinn einer Frage ist die Entropie vorher minus die gewichtete Entropie der Kinder danach. Gewählt wird die Frage mit dem größten Gewinn.

                Beispiel: 14 Patienten, 9 A / 5 B: H = −(9/14)log₂(9/14) − (5/14)log₂(5/14) ≈ 0,940 Bit. Teilt eine Frage in 8 (6 A / 2 B, H ≈ 0,811) und 6 (3 A / 3 B, H = 1), bleibt (8/14)·0,811 + (6/14)·1 ≈ 0,892. Gewinn ≈ 0,048 Bit.
            """,
                fm("Entropie", "H = −Σₖ pₖ log₂ pₖ"),
                fm("Informationsgewinn", "IG = H(Eltern) − Σⱼ (Nⱼ/N)·H(Kind j)"),
                fm("Gini-Unreinheit", "G = 1 − Σₖ pₖ²")
            ),
            sec("Gini statt Entropie", """
                scikit-learn nutzt standardmäßig die Gini-Unreinheit: die Wahrscheinlichkeit, ein zufälliges Beispiel falsch zu klassifizieren, wenn man zufällig nach der Klassenverteilung rät. Sie ist billiger (kein Logarithmus) und führt fast immer zu denselben Bäumen. Für 50/50: G = 0,5; rein: G = 0.

                Numerische Merkmale (Alter) werden an Schwellen geteilt: Der Algorithmus probiert die Mittelpunkte zwischen sortierten Werten durch.
            """),
            sec("Overfitting und Pruning", """
                Ein Baum ohne Begrenzung wächst, bis jedes Blatt rein ist – und lernt dabei jedes Rauschen. Gegenmittel:

                • max_depth (maximale Tiefe), min_samples_leaf (Mindestgröße eines Blattes), min_samples_split
                • Pruning (Beschneiden): Äste entfernen, die auf Validierungsdaten nicht helfen (cost-complexity pruning, ccp_alpha)
                • Ensembles: viele Bäume kombinieren (nächster Happen)

                $ from sklearn.tree import DecisionTreeClassifier, plot_tree
                $ tree = DecisionTreeClassifier(criterion="entropy", max_depth=4).fit(X_train, y_train)
                $ plot_tree(tree, feature_names=X.columns, filled=True)
            """),
            sec("Regressionsbäume", """
                Für Zahlenwerte sagt jedes Blatt den Mittelwert seiner Beispiele voraus. Statt Entropie minimiert man die Varianz (MSE) in den Kindern. Ergebnis: eine stückweise konstante Treppenfunktion – deshalb glätten Ensembles das Ergebnis.
            """,
                fm("Split-Kriterium Regression", "Σ_Kinder Nⱼ·Var(yⱼ) minimal")
            )
        ),
        chapter(
            "alg_ensembles", T, 2, "Ensembles: Random Forest und Boosting",
            "Weisheit der Vielen, Bagging, Random Forest, Out-of-Bag, Feature-Importance, AdaBoost, Gradient Boosting, XGBoost, Stacking.",
            emptyList(),
            sec("Die Weisheit der Vielen", """
                Viele mittelmäßige Modelle, deren Fehler nicht gleich sind, ergeben zusammen ein gutes Modell. Mittelt man B Modelle mit Varianz σ² und paarweiser Korrelation ρ, bleibt Varianz ρσ² + (1 − ρ)σ²/B übrig. Je unabhängiger die Modelle, desto mehr hilft es.
            """,
                fm("Varianz eines Mittels", "Var = ρσ² + (1 − ρ)σ²/B")
            ),
            sec("Bagging und Random Forest", """
                Bagging (Bootstrap Aggregating): Jeder Baum bekommt eine Zufallsstichprobe der Daten mit Zurücklegen; am Ende wird abgestimmt (Klassifikation) oder gemittelt (Regression). Random Forest geht weiter: An jedem Knoten darf der Baum nur aus einer zufälligen Teilmenge der Merkmale wählen (typisch √d). So werden die Bäume verschiedener, ρ sinkt, das Ensemble wird besser.

                • Out-of-Bag-Fehler: Jeder Baum sieht etwa 63 % der Daten; die restlichen 37 % dienen als eingebaute Validierung.
                • Feature-Importance: Wie sehr senkt jedes Merkmal im Mittel die Unreinheit? Praktisch zum Verstehen der Daten.
                • Random Forest ist robust, braucht kaum Feineinstellung und ist eine starke Basislinie für Tabellendaten.
            """,
                fm("Anteil im Bootstrap", "1 − (1 − 1/N)^N → 1 − 1/e ≈ 63,2 %")
            ),
            sec("Boosting", """
                Boosting baut Modelle nacheinander, jedes korrigiert die Fehler der Vorgänger.

                • AdaBoost: Falsch klassifizierte Beispiele bekommen mehr Gewicht; jedes schwache Modell erhält ein Stimmgewicht nach seiner Güte.
                • Gradient Boosting: Jedes neue Bäumchen fittet die Residuen (allgemein: den negativen Gradienten des Verlusts) des bisherigen Ensembles, mit kleiner Lernrate.
                • XGBoost, LightGBM, CatBoost: hochoptimierte Gradient-Boosting-Bibliotheken; seit Jahren die Gewinner auf Tabellendaten in Wettbewerben.
            """,
                fm("Gradient Boosting", "F_m(x) = F_{m−1}(x) + η·h_m(x), h_m ≈ −∂L/∂F"),
                fm("AdaBoost-Stimmgewicht", "α_m = ½ ln((1 − ε_m)/ε_m)")
            ),
            sec("Bagging gegen Boosting", """
                • Bagging senkt vor allem die Varianz (tiefe Bäume, parallel trainierbar).
                • Boosting senkt vor allem den Bias (flache Bäume, sequentiell), kann aber überanpassen – deshalb Lernrate und Early Stopping.
                • Stacking: Ein Meta-Modell lernt, wie man die Vorhersagen verschiedener Modelle am besten kombiniert.

                $ from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
                $ rf = RandomForestClassifier(n_estimators=300, oob_score=True).fit(X_train, y_train)
                $ print(rf.oob_score_, rf.feature_importances_)
            """)
        ),
        chapter(
            "alg_logreg", T, 2, "Logistische Regression ausführlich",
            "Wahrscheinlichkeiten statt Zahlen, Sigmoid, Odds und Log-Odds, lineare Entscheidungsgrenze, Kreuzentropie, Gradient, Regularisierung C.",
            emptyList(),
            sec("Warum nicht einfach lineare Regression?", """
                Für Ja/Nein-Fragen will man eine Wahrscheinlichkeit zwischen 0 und 1. Eine Gerade schießt darüber hinaus. Die logistische Regression berechnet erst wie gewohnt z = θᵀx und drückt z dann mit der Sigmoidfunktion in das Intervall (0, 1). IBM-Beispiel: Welche Kunden kündigen ihren Telefonvertrag (Churn)?
            """,
                fm("Sigmoid", "σ(z) = 1/(1 + e^{−z})"),
                fm("Modell", "P(y = 1 | x) = σ(θᵀx)")
            ),
            sec("Odds und Log-Odds", """
                Odds sind die Chance p/(1 − p): p = 0,8 bedeutet 4 : 1. Die logistische Regression ist linear in den Log-Odds. Deshalb lassen sich Koeffizienten deuten: Steigt xⱼ um 1, multiplizieren sich die Odds mit e^{θⱼ}. θⱼ = 0,7 heißt etwa „doppelte Chance“.
            """,
                fm("Odds", "odds = p/(1 − p)"),
                fm("Logit", "ln(p/(1 − p)) = θᵀx"),
                fm("Odds-Verhältnis", "OR = e^{θⱼ}")
            ),
            sec("Training mit Kreuzentropie", """
                Man maximiert die Wahrscheinlichkeit der Daten (Maximum Likelihood); das ist dasselbe wie die Kreuzentropie (Log-Loss) zu minimieren. Der Gradient hat eine schöne Form: Fehler mal Eingabe – genau wie bei der linearen Regression. Es gibt keine geschlossene Lösung, aber der Verlust ist konvex: Gradientenabstieg findet das globale Minimum.
            """,
                fm("Log-Loss", "L = −(1/N) Σ [yᵢ ln pᵢ + (1 − yᵢ) ln(1 − pᵢ)]"),
                fm("Gradient", "∂L/∂θ = (1/N) Σ (pᵢ − yᵢ) xᵢ")
            ),
            sec("Entscheidungsgrenze und Regularisierung", """
                Bei Schwelle 0,5 liegt die Grenze bei θᵀx = 0 – eine Gerade/Ebene. Gekrümmte Grenzen erhält man mit polynomialen Merkmalen. scikit-learn regularisiert standardmäßig mit L2; der Parameter C ist die inverse Stärke: kleines C = stärkere Regularisierung = einfacheres Modell.

                $ from sklearn.linear_model import LogisticRegression
                $ lr = LogisticRegression(C=0.1).fit(X_train, y_train)
                $ print(lr.predict_proba(X_test)[:5])
            """,
                fm("Entscheidungsgrenze", "θᵀx = 0  ⇔  p = 0,5")
            )
        ),
        chapter(
            "alg_svm", T, 2, "Support Vector Machines",
            "Maximaler Abstand, Stützvektoren, weiche Ränder und C, Kernel-Trick (RBF, Polynom), wann SVMs glänzen.",
            emptyList(),
            sec("Die breiteste Straße", """
                Wenn zwei Klassen linear trennbar sind, gibt es unendlich viele trennende Geraden. Die SVM wählt diejenige, die den größten Abstand (Margin) zu den nächsten Punkten beider Klassen hat – die breiteste Straße zwischen den Klassen. Nur die Punkte am Straßenrand bestimmen die Lösung: die Stützvektoren (support vectors). Alle anderen könnte man weglassen.
            """,
                fm("Trennebene", "wᵀx + b = 0"),
                fm("Margin-Breite", "2/|w|"),
                fm("Hard-Margin-Problem", "min ½|w|²  mit  yᵢ(wᵀxᵢ + b) ≥ 1")
            ),
            sec("Weiche Ränder und C", """
                Echte Daten überlappen. Die Soft-Margin-SVM erlaubt Verletzungen ξᵢ, bestraft sie aber mit C. Großes C: wenige Fehler erlaubt, schmale Straße, Gefahr von Overfitting. Kleines C: breite Straße, mehr Fehler toleriert, glatter.
            """,
                fm("Soft Margin", "min ½|w|² + C Σ ξᵢ  mit  yᵢ(wᵀxᵢ + b) ≥ 1 − ξᵢ"),
                fm("Hinge-Verlust", "ℓ = max(0, 1 − y·f(x))")
            ),
            sec("Der Kernel-Trick", """
                Nicht trennbare Daten werden oft trennbar, wenn man sie in einen höherdimensionalen Raum abbildet – z. B. Punkte auf zwei Ringen werden mit dem Merkmal x₁² + x₂² linear trennbar. Die SVM braucht nur Skalarprodukte zwischen Beispielen; der Kernel K(x, z) berechnet das Skalarprodukt im großen Raum, ohne ihn je auszurechnen. Beliebt: RBF-Kernel (Gauß), Polynom-Kernel. Beim RBF-Kernel steuert γ die Reichweite: großes γ = sehr lokale, zackige Grenze.
            """,
                fm("RBF-Kernel", "K(x, z) = exp(−γ|x − z|²)"),
                fm("Polynom-Kernel", "K(x, z) = (xᵀz + c)^d")
            ),
            sec("Wann SVM?", """
                • Gut bei mittelgroßen Datensätzen mit vielen Merkmalen (Text, Genexpression), klarer Trennung.
                • Training skaliert schlecht (etwa N² bis N³) – für Millionen Beispiele lieber lineare Modelle oder Boosting.
                • Skalierung der Merkmale ist Pflicht.

                $ from sklearn.svm import SVC
                $ svm = make_pipeline(StandardScaler(), SVC(kernel="rbf", C=1.0, gamma="scale")).fit(X_train, y_train)
            """)
        ),
        chapter(
            "alg_bayes", T, 2, "Naive Bayes",
            "Satz von Bayes, Prior und Likelihood, die naive Unabhängigkeitsannahme, Spamfilter Schritt für Schritt, Laplace-Glättung, Gauß- und Multinomial-Variante.",
            emptyList(),
            sec("Bayes in einem Satz", """
                Wie wahrscheinlich ist die Klasse c, wenn ich die Merkmale x sehe? Bayes dreht die Frage um: Wie wahrscheinlich sind diese Merkmale in Klasse c (Likelihood), mal wie häufig ist c überhaupt (Prior). Den Nenner P(x) braucht man zum Vergleichen der Klassen nicht.
            """,
                fm("Satz von Bayes", "P(c | x) = P(x | c)·P(c) / P(x)"),
                fm("Entscheidung", "ĉ = argmax_c P(c)·P(x | c)")
            ),
            sec("Die naive Annahme", """
                P(x | c) für viele Merkmale gemeinsam zu schätzen bräuchte astronomisch viele Daten. Naive Bayes nimmt an, dass die Merkmale innerhalb einer Klasse unabhängig sind. Dann zerfällt die Likelihood in ein Produkt. Die Annahme ist fast immer falsch – und trotzdem funktioniert das Verfahren erstaunlich gut, weil es für die Entscheidung nur auf die Reihenfolge der Klassenwahrscheinlichkeiten ankommt. Man rechnet mit Logarithmen, damit das Produkt vieler kleiner Zahlen nicht auf null fällt.
            """,
                fm("Naive Likelihood", "P(x | c) = Πⱼ P(xⱼ | c)"),
                fm("Log-Form", "ln P(c | x) = ln P(c) + Σⱼ ln P(xⱼ | c) + konst.")
            ),
            sec("Ein Spamfilter von Hand", """
                Training: 40 % aller Mails sind Spam. Das Wort „Gewinn“ steht in 30 % der Spam-Mails und 1 % der normalen. Eine neue Mail enthält „Gewinn“.

                • Spam: 0,4 · 0,30 = 0,12
                • Normal: 0,6 · 0,01 = 0,006
                • Normiert: P(Spam | „Gewinn“) = 0,12/(0,12 + 0,006) ≈ 95 %.

                Laplace-Glättung: Ein Wort, das im Training nie in normalen Mails vorkam, hätte Wahrscheinlichkeit 0 und würde alles auslöschen. Deshalb zählt man zu jeder Häufigkeit 1 dazu.
            """,
                fm("Laplace-Glättung", "P(w | c) = (n_{w,c} + 1) / (n_c + |V|)")
            ),
            sec("Varianten", """
                • Multinomial NB: Wortzählungen, Text.
                • Bernoulli NB: Wort vorhanden ja/nein.
                • Gaussian NB: kontinuierliche Merkmale, je Klasse eine Normalverteilung pro Merkmal.

                $ from sklearn.feature_extraction.text import CountVectorizer
                $ from sklearn.naive_bayes import MultinomialNB
                $ spam = make_pipeline(CountVectorizer(), MultinomialNB()).fit(texte, labels)
            """)
        ),
        chapter(
            "alg_kmeans", T, 2, "k-Means im Detail",
            "Lloyd-Algorithmus Schritt für Schritt, Konvergenz, lokale Minima, k-means++, Ellbogen und Silhouette, Grenzen von k-Means.",
            emptyList(),
            sec("Der Lloyd-Algorithmus", """
                • Initialisieren: k Zentren wählen.
                • Zuordnen: Jeder Punkt geht zum nächsten Zentrum.
                • Aktualisieren: Jedes Zentrum wandert in den Schwerpunkt seiner Punkte.
                • Wiederholen, bis sich die Zuordnung nicht mehr ändert.

                Zahlenbeispiel in 1D: Punkte 1, 2, 3, 10, 11, 12, Startzentren 1 und 3. Zuordnung: {1, 2} zu 1, {3, 10, 11, 12} zu 3. Neue Zentren 1,5 und 9. Neue Zuordnung {1, 2, 3} und {10, 11, 12}, Zentren 2 und 11 – fertig.
            """,
                fm("Zuordnung", "c(i) = argminₖ |xᵢ − μₖ|²"),
                fm("Aktualisierung", "μₖ = Mittelwert der Punkte mit c(i) = k")
            ),
            sec("Konvergenz und lokale Minima", """
                Jeder Schritt senkt die Inertia oder lässt sie gleich; da es nur endlich viele Zuordnungen gibt, endet der Algorithmus immer. Aber er findet nur ein lokales Minimum – schlechte Startzentren führen zu schlechten Clustern. Deshalb startet man mehrfach (n_init) und nimmt das beste Ergebnis.

                k-means++ wählt die Startzentren klug: das erste zufällig, jedes weitere mit Wahrscheinlichkeit proportional zum quadrierten Abstand zum nächsten bisherigen Zentrum. Das verteilt die Zentren und ist in scikit-learn Standard.
            """,
                fm("k-means++ Auswahl", "P(x) = D(x)² / Σ D(x′)²")
            ),
            sec("Grenzen", """
                • k muss man vorgeben.
                • Nimmt kugelförmige, ähnlich große Cluster an – bei Ringen, Bananenformen oder sehr unterschiedlicher Dichte scheitert k-Means (dann DBSCAN oder Gaußsche Mischmodelle).
                • Empfindlich gegen Ausreißer (Mittelwert!) – Alternative: k-Medoids.
                • Skalierung beachten.

                $ from sklearn.cluster import KMeans
                $ km = KMeans(n_clusters=4, n_init=10, random_state=0).fit(X_scaled)
                $ print(km.inertia_, km.cluster_centers_)
            """)
        ),
        chapter(
            "alg_hier_dbscan", T, 2, "Hierarchisches Clustering und DBSCAN",
            "Agglomeratives Clustering, Linkage-Arten, Dendrogramm; DBSCAN mit ε und minPts, Kern-, Rand- und Rauschpunkte.",
            emptyList(),
            sec("Agglomeratives Clustering", """
                Start: jeder Punkt ein Cluster. Dann wiederholt die zwei nächstgelegenen Cluster verschmelzen, bis nur noch einer übrig ist. Das Ergebnis ist ein Dendrogramm – ein Stammbaum der Verschmelzungen. Schneidet man ihn auf einer Höhe, erhält man eine bestimmte Clusterzahl. IBM-Beispiel: Fahrzeugmodelle nach technischen Daten gruppieren.

                Wie misst man den Abstand zweier Cluster (Linkage)?
                • Single: kleinster Abstand zweier Punkte – findet lange Ketten.
                • Complete: größter Abstand – kompakte Cluster.
                • Average: mittlerer Abstand aller Paare.
                • Ward: Verschmelzung, die die Varianz am wenigsten erhöht – ähnlich k-Means, oft die beste Wahl.
            """),
            sec("DBSCAN", """
                Dichtebasiert: Ein Punkt ist Kernpunkt, wenn in seinem Radius ε mindestens minPts Punkte liegen. Kernpunkte, die einander erreichen, bilden einen Cluster; Randpunkte gehören dazu, sind selbst aber nicht dicht; alles andere ist Rauschen.

                • Findet beliebige Formen (Ringe, Bananen) und erkennt Ausreißer.
                • Die Clusterzahl ergibt sich von selbst.
                • Schwierig bei stark unterschiedlicher Dichte; ε wählt man über den „k-Distanz-Plot“.

                $ from sklearn.cluster import DBSCAN
                $ db = DBSCAN(eps=0.3, min_samples=5).fit(X_scaled)
                $ labels = db.labels_      # −1 = Rauschen
            """,
                fm("Kernpunkt", "|{y : d(x, y) ≤ ε}| ≥ minPts")
            ),
            sec("Welches Clustering wann?", """
                • Viele Punkte, runde Cluster, k bekannt → k-Means.
                • Hierarchie interessant, eher wenige Punkte (bis einige Tausend) → agglomerativ.
                • Seltsame Formen, Ausreißer, k unbekannt → DBSCAN (oder HDBSCAN).
                • Weiche Zugehörigkeiten (Wahrscheinlichkeiten) → Gaußsche Mischmodelle.
            """)
        )
    ) + course(
        "Bewertung und Datenpraxis",
        chapter(
            "ev_split", T, 2, "Daten aufteilen und Kreuzvalidierung",
            "Train/Validation/Test, k-fache und stratifizierte Kreuzvalidierung, Datenlecks erkennen und vermeiden.",
            emptyList(),
            sec("Warum aufteilen?", """
                Auf den Trainingsdaten sieht jedes ausreichend große Modell gut aus. Der Trainingsfehler ist zu optimistisch. Was zählt, ist der Fehler auf neuen Daten (Generalisierungsfehler) – den schätzt man auf Daten, die das Modell nie gesehen hat.

                • Train: Parameter lernen.
                • Validierung: Hyperparameter und Modellwahl.
                • Test: einmal am Ende, Finger weg bis dahin.
            """),
            sec("k-fache Kreuzvalidierung", """
                Bei wenigen Daten verschwendet eine feste Validierungsmenge zu viel. Lösung: Daten in k Teile (Folds) teilen, k-mal trainieren, jedes Mal ein anderer Teil zum Validieren, Ergebnisse mitteln. Typisch k = 5 oder 10. Stratifiziert heißt: In jedem Fold ist das Klassenverhältnis wie im Ganzen – wichtig bei seltenen Klassen.

                $ from sklearn.model_selection import cross_val_score, StratifiedKFold
                $ scores = cross_val_score(model, X, y, cv=StratifiedKFold(5, shuffle=True, random_state=0))
                $ print(scores.mean(), scores.std())
            """,
                fm("CV-Schätzer", "Fehler_CV = (1/k) Σⱼ Fehler(Fold j)")
            ),
            sec("Datenlecks: der stille Killer", """
                Ein Datenleck ist Information im Training, die beim echten Einsatz nicht verfügbar wäre. Folge: traumhafte Testwerte, Absturz in der Praxis.

                • Skalierer oder Imputer auf allen Daten statt nur auf dem Training fitten → immer Pipelines benutzen.
                • Merkmale, die das Ergebnis verraten („Kündigungsdatum“ beim Vorhersagen von Kündigungen).
                • Zeitreihen zufällig teilen (Zukunft im Training).
                • Dieselbe Person oder dasselbe Bild in Training und Test (Gruppen mit GroupKFold trennen).

                > Faustregel: Wenn ein Ergebnis zu gut aussieht, um wahr zu sein, such zuerst das Leck.
            """)
        ),
        chapter(
            "ev_metrics", T, 2, "Metriken richtig lesen",
            "Konfusionsmatrix, Accuracy, Precision, Recall, Spezifität, F1, ROC und AUC, Precision-Recall-Kurve, Log-Loss, Regressionsmetriken im Vergleich.",
            emptyList(),
            sec("Die Konfusionsmatrix", """
                Für zwei Klassen gibt es vier Ausgänge:
                • TP (richtig positiv): krank, als krank erkannt
                • FN (falsch negativ): krank, übersehen – Fehler 2. Art
                • FP (falsch positiv): gesund, fälschlich Alarm – Fehler 1. Art
                • TN (richtig negativ): gesund, als gesund erkannt

                Alle Klassifikationsmetriken sind Verhältnisse aus diesen vier Zahlen.
            """,
                fm("Accuracy", "(TP + TN) / (TP + TN + FP + FN)"),
                fm("Precision", "TP / (TP + FP)"),
                fm("Recall (Sensitivität)", "TP / (TP + FN)"),
                fm("Spezifität", "TN / (TN + FP)"),
                fm("F1-Score", "F1 = 2·P·R / (P + R)")
            ),
            sec("Precision oder Recall?", """
                • Recall wichtiger: Krebs-Screening, Betrug, Sicherheit – ein Übersehen ist teuer.
                • Precision wichtiger: Spamfilter (eine wichtige Mail im Spam ist schlimm), Empfehlungen, Rechtsfälle.
                • F1 ist das harmonische Mittel – hoch nur, wenn beide hoch sind. Fβ gewichtet Recall β-mal so stark.

                Rechenbeispiel: 100 Kranke, das Modell meldet 120 Fälle, davon 80 richtig. Precision = 80/120 ≈ 0,67, Recall = 80/100 = 0,8, F1 ≈ 0,73.
            """,
                fm("F-beta", "F_β = (1 + β²)PR / (β²P + R)")
            ),
            sec("ROC, AUC und PR-Kurve", """
                Die ROC-Kurve trägt Recall (Trefferquote) gegen die Falsch-Positiv-Rate FP/(FP + TN) für alle Schwellen auf. AUC ist die Fläche darunter: die Wahrscheinlichkeit, dass ein zufälliges positives Beispiel einen höheren Score bekommt als ein zufälliges negatives. 0,5 = Raten, 1 = perfekt. Bei sehr seltenen Positiven ist die Precision-Recall-Kurve aussagekräftiger, weil sie die vielen TN ignoriert.
            """,
                fm("Falsch-Positiv-Rate", "FPR = FP / (FP + TN)"),
                fm("AUC-Deutung", "AUC = P(Score(pos) > Score(neg))")
            ),
            sec("Mehrklassig und Regression", """
                • Makro-Mittel: Metrik je Klasse, dann ungewichtet mitteln (seltene Klassen zählen gleich).
                • Mikro-Mittel: TP, FP, FN über alle Klassen summieren.
                • Gewichtet: Makro, gewichtet nach Klassengröße.
                • Regression: MAE (robust, deutbar), RMSE (große Fehler zählen mehr), R² (relativ zum Mittelwert), MAPE (prozentual, Vorsicht bei Werten nahe 0).

                $ from sklearn.metrics import classification_report, confusion_matrix
                $ print(confusion_matrix(y_test, y_pred))
                $ print(classification_report(y_test, y_pred))
            """,
                fm("MAPE", "MAPE = (100 %/N) Σ |yᵢ − ŷᵢ| / |yᵢ|")
            )
        ),
        chapter(
            "ev_features", T, 2, "Feature Engineering und Vorverarbeitung",
            "Skalieren (Standardisierung, Min-Max), Kodieren (One-Hot, ordinal), fehlende Werte, Ausreißer, neue Merkmale bauen, Pipelines und ColumnTransformer.",
            emptyList(),
            sec("Skalieren", """
                Viele Verfahren (KNN, SVM, k-Means, PCA, neuronale Netze, Gradientenabstieg) sind empfindlich für die Größenordnung der Merkmale. Bäume sind es nicht.

                • Standardisierung: Mittelwert 0, Standardabweichung 1.
                • Min-Max: auf [0, 1].
                • Robust: Median und IQR statt Mittel und σ – unempfindlich gegen Ausreißer.
            """,
                fm("Standardisierung", "x′ = (x − μ)/σ"),
                fm("Min-Max-Skalierung", "x′ = (x − x_min)/(x_max − x_min)")
            ),
            sec("Kategorien kodieren", """
                • One-Hot: je Kategorie eine 0/1-Spalte. Für nominale Merkmale ohne Ordnung.
                • Ordinal: Zahlen in der natürlichen Reihenfolge (S = 0, M = 1, L = 2).
                • Bei sehr vielen Kategorien (Postleitzahlen): Target-Encoding oder Embeddings.

                Achtung: Kategorien einfach durchzunummerieren (Berlin = 1, Köln = 2, Wien = 3) täuscht eine Ordnung vor, die es nicht gibt.
            """),
            sec("Fehlende Werte und Ausreißer", """
                • Zeilen löschen: nur wenn wenige und zufällig fehlend.
                • Imputieren: Mittelwert/Median (numerisch), häufigster Wert (kategorisch), oder per Modell (KNN-Imputer).
                • Indikator-Spalte „war fehlend“ – manchmal ist das Fehlen selbst informativ.
                • Ausreißer: prüfen, ob Messfehler oder echte Extremwerte; kappen (winsorisieren) oder robuste Modelle nutzen.
            """),
            sec("Neue Merkmale bauen", """
                Gute Merkmale schlagen oft bessere Modelle. Beispiele: Body-Mass-Index aus Größe und Gewicht, Wochentag und Stunde aus einem Zeitstempel, Preis pro m², Logarithmus schiefer Verteilungen (Einkommen), Verhältnisse und Differenzen. Fachwissen ist hier dein größter Vorteil – als Physiker baust du dimensionslose Kennzahlen.
            """),
            sec("Alles in einer Pipeline", """
                $ from sklearn.compose import ColumnTransformer
                $ from sklearn.impute import SimpleImputer
                $ from sklearn.preprocessing import OneHotEncoder, StandardScaler
                $ num = make_pipeline(SimpleImputer(strategy="median"), StandardScaler())
                $ cat = OneHotEncoder(handle_unknown="ignore")
                $ prep = ColumnTransformer([("num", num, ["alter", "einkommen"]), ("cat", cat, ["stadt"])])
                $ model = make_pipeline(prep, LogisticRegression())
                $ model.fit(X_train, y_train)

                > Die Pipeline lernt Mittelwerte und Kategorien nur aus den Trainingsdaten – kein Datenleck.
            """)
        ),
        chapter(
            "ev_tuning", T, 2, "Bias, Varianz und Hyperparameter-Suche",
            "Under- und Overfitting diagnostizieren, Lernkurven, Validierungskurven, Grid Search, Random Search, Bayes-Optimierung, Early Stopping.",
            emptyList(),
            sec("Bias und Varianz", """
                • Hoher Bias (Underfitting): Modell zu einfach. Trainings- und Validierungsfehler beide hoch. → komplexeres Modell, mehr Merkmale, weniger Regularisierung.
                • Hohe Varianz (Overfitting): Modell zu flexibel. Trainingsfehler niedrig, Validierungsfehler hoch. → mehr Daten, Regularisierung, einfacheres Modell, Ensembles, Early Stopping.

                Der erwartete Fehler zerfällt in Bias² + Varianz + nicht reduzierbares Rauschen. Komplexität verschiebt das Gleichgewicht: die berühmte U-Kurve des Validierungsfehlers.
            """,
                fm("Bias-Varianz-Zerlegung", "E[(y − f̂(x))²] = Bias[f̂]² + Var[f̂] + σ²")
            ),
            sec("Lernkurven", """
                Fehler gegen Anzahl Trainingsbeispiele auftragen:
                • Beide Kurven hoch und nah beieinander → Bias: mehr Daten helfen nicht.
                • Große Lücke zwischen Training und Validierung → Varianz: mehr Daten helfen.
            """),
            sec("Hyperparameter suchen", """
                • Grid Search: alle Kombinationen eines Gitters ausprobieren – gründlich, aber teuer.
                • Random Search: zufällige Kombinationen – bei gleichem Budget meist besser, weil oft nur wenige Hyperparameter wichtig sind.
                • Bayes-Optimierung (Optuna): lernt aus bisherigen Versuchen, wo es sich lohnt zu suchen.
                • Immer mit Kreuzvalidierung, nie auf dem Testset.

                $ from sklearn.model_selection import GridSearchCV
                $ grid = GridSearchCV(SVC(), {"C": [0.1, 1, 10], "gamma": [0.01, 0.1, 1]}, cv=5)
                $ grid.fit(X_train, y_train)
                $ print(grid.best_params_, grid.best_score_)
            """),
            sec("Early Stopping", """
                Bei iterativen Verfahren (Boosting, neuronale Netze) beobachtet man den Validierungsfehler während des Trainings und hört auf, wenn er wieder steigt. Das ist eine der wirksamsten und billigsten Regularisierungen.
            """)
        ),
        chapter(
            "ev_fair", T, 2, "Unausgewogene Daten, Erklärbarkeit und Fairness",
            "Seltene Klassen (Gewichte, Resampling, SMOTE, Schwellen), Feature-Importance, Permutation Importance, SHAP, Bias und Fairness, Datenschutz.",
            emptyList(),
            sec("Seltene Klassen", """
                • Klassengewichte: Fehler bei der seltenen Klasse teurer machen (class_weight="balanced").
                • Undersampling der häufigen oder Oversampling der seltenen Klasse; SMOTE erzeugt synthetische Beispiele zwischen Nachbarn.
                • Schwelle verschieben statt bei 0,5 zu entscheiden.
                • Richtige Metrik: Recall, Precision, F1, PR-AUC statt Accuracy.
                • Resampling nur auf den Trainingsdaten, nie auf Validierung oder Test.
            """),
            sec("Erklärbarkeit", """
                • Koeffizienten linearer Modelle (nach Standardisierung vergleichbar).
                • Feature-Importance von Bäumen.
                • Permutation Importance: Wie stark sinkt die Güte, wenn man eine Spalte zufällig durchmischt? Modellunabhängig.
                • SHAP-Werte: verteilen die Vorhersage fair auf die Merkmale (Shapley-Werte aus der Spieltheorie); erklären einzelne Vorhersagen.
                • Partial-Dependence-Plots: wie hängt die Vorhersage im Mittel von einem Merkmal ab?
            """,
                fm("Additive Erklärung (SHAP)", "f(x) = φ₀ + Σⱼ φⱼ")
            ),
            sec("Fairness und Verantwortung", """
                Modelle übernehmen Verzerrungen aus den Daten: Ein Bewerbungsfilter, der mit historischen Einstellungen trainiert wurde, benachteiligt Gruppen, die früher benachteiligt wurden. Gegenmaßnahmen: Daten prüfen, Metriken je Gruppe vergleichen (gleiche Recall-Raten?), sensible Merkmale und ihre Stellvertreter (Postleitzahl) kritisch betrachten, Menschen in der Entscheidungsschleife lassen. Dazu Datenschutz (DSGVO): nur nötige Daten, Zweckbindung, Löschung. Der EU AI Act stuft Hochrisiko-Anwendungen (Personal, Kredit, Medizin) besonders streng ein.
            """)
        )
    )
}

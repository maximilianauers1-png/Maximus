package app.maximus.ai.domain

/**
 * Code drills and extra questions for the bites carried over from the old lab path, so that every bite
 * has its own quiz of at least six questions including code. Part 1: Lektion 1 (Machine Learning with Python).
 */
internal object AiCodeDrills {
    val questions: List<AiQuestion> by lazy { lesson1 + AiCodeDrills2.questions + AiCodeDrills3.questions }

    private val lesson1: List<AiQuestion> = quiz("cd") {
        on("ai0_what")
        code(1, "Welche Art von KI steckt in diesem Programm?", """
            def ist_spam(mail):
                return "gewinn" in mail.lower() and "konto" in mail.lower()
        """, "Regelbasierte KI ohne Lernen – ein Mensch hat die Regel geschrieben", "Maschinelles Lernen", "Deep Learning", "Generative KI",
            why = "Klassisch: Daten + Regeln → Antworten. ML würde die Regel aus beschrifteten Mails lernen.")
        line(1, "Welche Zeile ist typisch für maschinelles Lernen statt klassischer Programmierung?",
            "model.fit(X_train, y_train)", "if temperatur > 30: print(\"heiß\")", "for i in range(10): print(i)", "x = 2 + 3",
            why = "fit lernt Parameter aus Beispielen mit bekannten Antworten.")

        on("ai0_data")
        codeOut(1, "Welche Form hat X?", """
            import pandas as pd
            df = pd.DataFrame({"flaeche": [80, 120, 95], "zimmer": [3, 4, 3], "preis": [300, 450, 350]})
            X = df[["flaeche", "zimmer"]]
            print(X.shape)
        """, "(3, 2)", "(2, 3)", "(3, 3)", "(3,)",
            why = "Drei Beispiele (Zeilen), zwei Merkmale (Spalten); preis wäre das Label y.")
        code(1, "Was macht diese Zeile?", """
            df = pd.get_dummies(df, columns=["stadt"])
        """, "One-Hot-Kodierung: Aus der Spalte stadt werden 0/1-Spalten, eine pro Stadt", "Sie löscht die Spalte stadt", "Sie sortiert nach Stadt", "Sie füllt fehlende Städte auf",
            why = "So bekommt ein Modell keine falsche Reihenfolge wie Berlin = 1, Köln = 2 vorgetäuscht.")

        on("ai0_model")
        codeOut(1, "Was gibt der Code aus?", """
            import numpy as np
            y = np.array([3.0, 5.0]); y_hat = np.array([2.0, 7.0])
            print(np.mean((y - y_hat) ** 2))
        """, "2.5", "1.5", "5.0", "3.0",
            why = "Fehler −1 und 2 → Quadrate 1 und 4 → Mittelwert 2,5 (MSE).")
        code(2, "Was passiert in dieser Schleife mit w?", """
            w = 0.0
            for _ in range(100):
                grad = 2 * (w - 3)
                w = w - 0.1 * grad
        """, "w nähert sich 3, dem Minimum von (w − 3)²", "w wächst unbegrenzt", "w bleibt 0", "w springt zwischen 0 und 6",
            why = "Gradientenabstieg auf einer Parabel: w ← w − 0,2(w − 3) konvergiert gegen 3.")

        on("ai0_types")
        code(1, "Welche Lernart zeigt dieser Code?", """
            from sklearn.cluster import KMeans
            labels = KMeans(n_clusters=3).fit_predict(X)
        """, "Unüberwachtes Lernen – es gibt kein y", "Überwachtes Lernen", "Bestärkendes Lernen", "Selbstüberwachtes Vortraining",
            why = "k-Means bekommt nur X und findet Gruppen selbst.")
        code(1, "Welche Lernart zeigt dieser Code?", """
            clf = LogisticRegression().fit(X_train, y_train)
        """, "Überwachtes Lernen – Beispiele mit Labels", "Unüberwachtes Lernen", "Bestärkendes Lernen", "Clustering",
            why = "Klassifikation mit bekannten Antworten y.")

        on("ai0_math")
        codeOut(1, "Was gibt der Code aus?", """
            import numpy as np
            w = np.array([1, 2]); x = np.array([3, 4])
            print(w @ x)
        """, "11", "[3 8]", "[4 6]", "10",
            why = "Skalarprodukt 1·3 + 2·4 = 11 – das Herzstück jedes linearen Modells.")
        codeOut(2, "Was gibt der Code aus?", """
            import numpy as np
            A = np.array([[1, 0], [0, 2]]); v = np.array([3, 4])
            print(A @ v)
        """, "[3 8]", "[3 4]", "11", "[4 6]",
            why = "Matrix mal Vektor: erste Komponente bleibt, zweite wird verdoppelt.")

        on("ai0_workflow")
        line(1, "Welcher Schritt gehört an den Anfang jedes ML-Projekts?",
            "Problem und Erfolgsmetrik definieren, dann Daten sammeln und erkunden", "model.fit(X, y)", "Modell exportieren", "Hyperparameter tunen",
            why = "Ohne klares Ziel optimiert man das Falsche.")
        code(1, "Welcher Schritt des ML-Ablaufs ist das?", """
            df.describe(); df.isna().sum(); df.hist()
        """, "Explorative Datenanalyse: Verteilungen, fehlende Werte, Ausreißer prüfen", "Modelltraining", "Bereitstellung", "Hyperparameter-Suche",
            why = "Erst verstehen, dann modellieren.")

        on("ai0_tools")
        line(1, "Welche Bibliothek importiert man für klassisches ML mit fit/predict?",
            "from sklearn.linear_model import LinearRegression", "import torch.nn as nn", "from tensorflow import keras", "import matplotlib.pyplot as plt",
            why = "scikit-learn für klassische Verfahren; Keras und PyTorch für neuronale Netze.")
        codeOut(1, "Was gibt der Code aus?", """
            import numpy as np
            a = np.array([[1, 2, 3], [4, 5, 6]])
            print(a.shape, a.mean())
        """, "(2, 3) 3.5", "(3, 2) 3.5", "(2, 3) 21", "(6,) 3.0",
            why = "Zwei Zeilen, drei Spalten; Mittelwert aller sechs Zahlen 21/6 = 3,5.")
        mc(1, "Wofür benutzt man pandas vor allem?",
            "Für Tabellen: Daten laden, filtern, gruppieren, bereinigen", "Für neuronale Netze", "Für Bildverarbeitung auf der GPU", "Für Webserver",
            why = "DataFrame ist das Arbeitspferd der Datenvorbereitung.")

        on("ml_techniques")
        code(1, "Welche der sieben Techniken setzt dieser Code um?", """
            from mlxtend.frequent_patterns import apriori, association_rules
            rules = association_rules(apriori(baskets, min_support=0.05), metric="lift", min_threshold=1.2)
        """, "Assoziation (Warenkorbanalyse)", "Regression", "Klassifikation", "Anomalieerkennung",
            why = "Apriori findet häufige Itemsets, daraus Regeln mit Support, Konfidenz und Lift.")
        code(1, "Welche Technik setzt dieser Code um?", """
            from sklearn.ensemble import IsolationForest
            flags = IsolationForest(contamination=0.01).fit_predict(transactions)
        """, "Anomalieerkennung", "Clustering", "Empfehlungssystem", "Dimensionsreduktion",
            why = "Isolation Forest markiert seltene, leicht isolierbare Punkte mit −1.")

        on("ml_regression")
        code(1, "Welche Aufgabe löst dieser Code?", """
            reg = LinearRegression().fit(df[["ENGINESIZE"]], df["CO2EMISSIONS"])
        """, "Regression: eine Zahl (CO₂ in g/km) vorhersagen", "Klassifikation", "Clustering", "Assoziation",
            why = "Das Ziel ist kontinuierlich.")
        line(1, "Welche Metrik passt zu einer Regression?",
            "mean_absolute_error(y_test, y_pred)", "accuracy_score(y_test, y_pred)", "f1_score(y_test, y_pred)", "roc_auc_score(y_test, y_pred)",
            why = "Accuracy, F1 und AUC sind Klassifikationsmetriken.")

        on("ml_classification")
        code(1, "Welche Aufgabe löst dieser Code?", """
            clf = DecisionTreeClassifier().fit(X_train, y_train)   # y: "Spam" oder "kein Spam"
        """, "Klassifikation in zwei Klassen", "Regression", "Clustering", "Dimensionsreduktion",
            why = "Ein Klassifikator sagt diskrete Klassen voraus.")
        codeOut(1, "Was gibt der Code aus?", """
            from sklearn.metrics import accuracy_score
            print(accuracy_score([1, 0, 1, 1], [1, 0, 0, 1]))
        """, "0.75", "0.5", "1.0", "0.25",
            why = "Drei von vier Vorhersagen stimmen.")

        on("alg_logreg")
        codeOut(2, "Was gibt der Code aus (gerundet)?", """
            import numpy as np
            z = np.log(3)          # Logit
            print(round(1 / (1 + np.exp(-z)), 2))
        """, "0.75", "0.33", "0.5", "3.0",
            why = "σ(ln 3) = 3/(1 + 3) = 0,75 – Odds von 3 : 1.")
        line(1, "Welche Methode liefert bei LogisticRegression Wahrscheinlichkeiten?",
            "clf.predict_proba(X_test)", "clf.predict(X_test)", "clf.decision_path(X_test)", "clf.coef_(X_test)",
            why = "predict gibt Klassen (Schwelle 0,5), predict_proba die Wahrscheinlichkeiten je Klasse.")

        on("alg_knn")
        code(1, "Was ist hier der wichtigste Hyperparameter?", """
            knn = KNeighborsClassifier(n_neighbors=5).fit(X_train, y_train)
        """, "k = n_neighbors: wie viele Nachbarn abstimmen", "Die Lernrate", "Die Baumtiefe", "Die Zahl der Epochen",
            why = "Kleines k: unruhige Grenzen (Overfitting), großes k: zu glatt.")
        code(2, "Warum gehört bei KNN ein Scaler davor?", """
            make_pipeline(StandardScaler(), KNeighborsClassifier(n_neighbors=7))
        """, "KNN misst Abstände; Merkmale mit großer Skala würden sonst den Abstand dominieren", "KNN braucht Werte zwischen 0 und 1", "Aus Geschwindigkeitsgründen", "Gar nicht nötig",
            why = "Einkommen in Euro würde Alter in Jahren völlig überstimmen.")
        codeOut(2, "Welche Klasse sagt 3-NN voraus, wenn die drei nächsten Nachbarn die Labels [A, B, A] haben?", """
            from collections import Counter
            print(Counter(["A", "B", "A"]).most_common(1)[0][0])
        """, "A", "B", "AB", "Unentschieden",
            why = "Mehrheitsabstimmung 2 : 1.")

        on("alg_trees")
        codeOut(2, "Was gibt der Code aus (Gini-Unreinheit)?", """
            p = [0.5, 0.5]
            print(1 - sum(pi ** 2 for pi in p))
        """, "0.5", "0.0", "1.0", "0.25",
            why = "Gini = 1 − Σpᵢ²; bei 50/50 maximal für zwei Klassen.")
        code(1, "Was begrenzt max_depth=3?", """
            DecisionTreeClassifier(max_depth=3, criterion="entropy")
        """, "Die Zahl der Fragen von der Wurzel bis zu einem Blatt – gegen Überanpassung", "Die Zahl der Merkmale", "Die Zahl der Klassen", "Die Trainingszeit in Sekunden",
            why = "Ein Baum der Tiefe 3 hat höchstens 8 Blätter.")

        on("alg_svm")
        code(2, "Was bewirkt kernel=\"rbf\"?", """
            SVC(kernel="rbf", C=1.0, gamma="scale")
        """, "Die SVM trennt in einem impliziten, hochdimensionalen Merkmalsraum und kann so nichtlineare Grenzen ziehen", "Sie macht die SVM linear", "Sie wählt Merkmale aus", "Sie skaliert die Daten",
            why = "Kernel-Trick: K(x, x′) = exp(−γ‖x − x′‖²) ersetzt das Skalarprodukt.")
        code(2, "Was passiert bei sehr großem C?", """
            SVC(kernel="linear", C=1000)
        """, "Fehlklassifikationen werden stark bestraft – schmaler Rand, Gefahr der Überanpassung", "Der Rand wird sehr breit", "Die SVM ignoriert die Daten", "Die Lernrate steigt",
            why = "Kleines C erlaubt mehr Randverletzungen und regularisiert stärker.")

        on("alg_bayes")
        code(1, "Welche Variante passt zu Wortzählungen in Texten?", """
            from sklearn.naive_bayes import MultinomialNB
            MultinomialNB().fit(word_counts, labels)
        """, "Multinomiales Naive Bayes – für Häufigkeiten wie Wortzählungen", "Gaußsches Naive Bayes", "Lineare Regression", "k-Means",
            why = "GaussianNB passt zu kontinuierlichen Merkmalen, BernoulliNB zu Ja/Nein-Merkmalen.")
        codeOut(2, "Was gibt der Code aus (Satz von Bayes)?", """
            p_k, p_pos_k, p_pos = 0.01, 0.9, 0.05
            print(round(p_k * p_pos_k / p_pos, 2))
        """, "0.18", "0.9", "0.01", "0.05",
            why = "P(krank | positiv) = 0,01 · 0,9 / 0,05 = 0,18 – trotz 90 % Sensitivität.")
        mc(2, "Was ist die ‚naive‘ Annahme von Naive Bayes?",
            "Die Merkmale sind gegeben der Klasse unabhängig", "Alle Klassen sind gleich wahrscheinlich", "Die Daten sind normalverteilt", "Es gibt nur zwei Merkmale",
            why = "P(x₁, …, x_d | y) = Π P(xᵢ | y) – falsch, aber oft erstaunlich wirksam.")

        on("alg_ensembles")
        code(1, "Was ist ein Random Forest?", """
            RandomForestClassifier(n_estimators=300, max_features="sqrt")
        """, "Viele Entscheidungsbäume auf Bootstrap-Stichproben mit zufälliger Merkmalsauswahl, die abstimmen", "Ein einziger sehr tiefer Baum", "Ein neuronales Netz", "Ein k-Means-Verfahren",
            why = "Bagging plus zufällige Merkmale senkt die Varianz deutlich.")
        code(2, "Was unterscheidet diesen Ansatz vom Random Forest?", """
            from xgboost import XGBClassifier
            XGBClassifier(n_estimators=500, learning_rate=0.05, max_depth=4)
        """, "Boosting: Bäume werden nacheinander trainiert, jeder korrigiert die Fehler der vorigen", "Die Bäume werden parallel und unabhängig trainiert", "Es benutzt keine Bäume", "Es ist unüberwacht",
            why = "Gradient Boosting senkt vor allem den Bias; die Lernrate dämpft jeden Schritt.")

        on("ml_clustering")
        code(1, "Was liefert fit_predict hier?", """
            labels = KMeans(n_clusters=4, n_init=10).fit_predict(X)
        """, "Für jeden Punkt die Nummer seines Clusters (0–3)", "Die Clusterzentren", "Die Accuracy", "Die Zahl der Cluster",
            why = "Die Zentren stehen danach in cluster_centers_.")
        line(2, "Welche Metrik bewertet ein Clustering ohne Labels?",
            "silhouette_score(X, labels)", "accuracy_score(y, labels)", "r2_score(X, labels)", "mean_squared_error(X, labels)",
            why = "Der Silhouettenkoeffizient vergleicht Abstand im eigenen und zum nächsten Cluster (−1 … 1).")

        on("alg_kmeans")
        code(2, "Was zeigt der ‚Ellbogen‘ in diesem Plot?", """
            inertias = [KMeans(n_clusters=k, n_init=10).fit(X).inertia_ for k in range(1, 10)]
            plt.plot(range(1, 10), inertias)
        """, "Das k, ab dem zusätzliche Cluster die Inertia kaum noch senken", "Das k mit der größten Inertia", "Den Fehler des Modells", "Die Zahl der Ausreißer",
            why = "Inertia sinkt immer mit k; der Knick ist ein Hinweis auf eine natürliche Clusterzahl.")
        codeOut(2, "Was ist das neue Zentrum nach einem Lloyd-Schritt?", """
            import numpy as np
            punkte = np.array([[0, 0], [2, 0], [1, 3]])   # alle diesem Zentrum zugeordnet
            print(punkte.mean(axis=0))
        """, "[1. 1.]", "[0. 0.]", "[1. 3.]", "[3. 3.]",
            why = "Das Zentrum wandert zum Mittelwert der zugeordneten Punkte.")
        mc(2, "Warum startet man k-Means mehrfach (n_init)?",
            "Das Ergebnis hängt von der Initialisierung ab; man behält den Lauf mit der kleinsten Inertia", "Damit k größer wird", "Weil k-Means nicht konvergiert", "Um Ausreißer zu entfernen",
            why = "Lloyd findet nur lokale Minima; k-means++ wählt zusätzlich gut verteilte Startzentren.")

        on("alg_hier_dbscan")
        code(2, "Was bedeuten eps und min_samples?", """
            DBSCAN(eps=0.5, min_samples=5).fit_predict(X)
        """, "Radius der Nachbarschaft und Mindestzahl von Punkten darin, damit ein Kernpunkt entsteht", "Lernrate und Batchgröße", "Zahl der Cluster und Iterationen", "Toleranz und Seed",
            why = "Punkte ohne dichte Nachbarschaft werden als Rauschen (Label −1) markiert.")
        codeOut(2, "Was bedeutet das Label −1 bei DBSCAN?", """
            labels = DBSCAN(eps=0.3, min_samples=4).fit_predict(X)
            print((labels == -1).sum())
        """, "Die Zahl der als Rauschen markierten Punkte", "Die Zahl der Cluster", "Die Zahl der Kernpunkte", "Einen Fehler",
            why = "DBSCAN zwingt nicht jeden Punkt in einen Cluster.")
        code(2, "Was erzeugt dieser Code?", """
            from scipy.cluster.hierarchy import linkage, dendrogram
            dendrogram(linkage(X, method="ward"))
        """, "Ein Dendrogramm des agglomerativen Clusterings; ein Schnitt in passender Höhe ergibt die Cluster", "Ein Streudiagramm", "Eine Konfusionsmatrix", "Eine Lernkurve",
            why = "Ward verschmilzt jeweils die Cluster mit dem geringsten Varianzzuwachs.")

        on("ml_dimred")
        codeOut(2, "Was gibt der Code aus?", """
            pca = PCA(n_components=2).fit(X)      # X hat 10 Merkmale
            print(pca.components_.shape)
        """, "(2, 10)", "(10, 2)", "(2,)", "(10, 10)",
            why = "Zwei Hauptkomponenten, jede ein Richtungsvektor im 10-dimensionalen Merkmalsraum.")
        code(2, "Was misst explained_variance_ratio_?", """
            print(PCA().fit(X).explained_variance_ratio_.cumsum())
        """, "Den kumulierten Anteil der Gesamtvarianz, den die ersten Komponenten erklären", "Die Genauigkeit", "Den Abstand der Cluster", "Den Fehler der Rekonstruktion in Prozent",
            why = "Man wählt oft so viele Komponenten, dass z. B. 95 % erklärt sind.")

        on("ml_association")
        codeOut(2, "Was ergibt die Konfidenz der Regel {Brot} → {Butter}?", """
            support_brot, support_beides = 0.4, 0.2
            print(support_beides / support_brot)
        """, "0.5", "0.2", "0.4", "0.08",
            why = "Konfidenz = Support(A ∪ B)/Support(A).")
        codeOut(2, "Was ergibt der Lift, wenn Support(Butter) = 0,25?", """
            konfidenz, support_butter = 0.5, 0.25
            print(konfidenz / support_butter)
        """, "2.0", "0.125", "0.5", "0.75",
            why = "Lift > 1: Brotkäufer kaufen doppelt so oft Butter wie der Durchschnitt.")

        on("ml_anomaly")
        codeOut(2, "Welcher Wert ist nach der z-Regel (|z| > 3) eine Anomalie?", """
            mu, sigma = 100, 10
            werte = [95, 128, 135, 72]
            print([w for w in werte if abs(w - mu) / sigma > 3])
        """, "[135]", "[128, 135]", "[72, 135]", "[]",
            why = "z(135) = 3,5; z(128) = 2,8 und z(72) = −2,8 liegen innerhalb.")
        code(2, "Warum ist Accuracy hier eine schlechte Metrik?", """
            # 10 000 Transaktionen, davon 12 Betrugsfälle
            print(accuracy_score(y, np.zeros_like(y)))
        """, "Ein Modell, das nie Betrug meldet, erreicht 99,9 % Accuracy und findet keinen einzigen Fall", "Accuracy ist bei Betrug immer null", "Weil Betrug kontinuierlich ist", "Sie ist hier perfekt",
            why = "Bei seltenen Klassen zählen Recall, Precision und PR-AUC.")

        on("ml_sequence")
        code(2, "Warum wird hier nicht gemischt?", """
            tscv = TimeSeriesSplit(n_splits=5)
            for train_idx, test_idx in tscv.split(X): ...
        """, "Bei Zeitreihen darf das Modell nur aus der Vergangenheit lernen und auf der Zukunft getestet werden", "Weil Mischen verboten ist", "Aus Geschwindigkeitsgründen", "Weil TimeSeriesSplit keine Indizes kennt",
            why = "Zufälliges Mischen würde Zukunftsinformation ins Training lecken.")
        codeOut(2, "Was erzeugt shift(1)?", """
            s = pd.Series([10, 20, 30])
            print(s.shift(1).tolist())
        """, "[nan, 10.0, 20.0]", "[20, 30, nan]", "[10, 20, 30]", "[0, 10, 20]",
            why = "Verzögerte Werte (Lags) sind die wichtigsten Merkmale für Zeitreihenmodelle.")
        mc(2, "Was ist ein Lag-Merkmal?",
            "Der Wert der Zeitreihe zu einem früheren Zeitpunkt als Eingabe", "Eine Verzögerung im Training", "Ein fehlender Wert", "Die Zielgröße",
            why = "Etwa der Verbrauch von gestern zur Vorhersage von heute.")

        on("ml_recommender")
        codeOut(2, "Wie groß ist die Kosinusähnlichkeit zweier Nutzer mit Bewertungen (5, 0) und (5, 0)?", """
            import numpy as np
            a = np.array([5, 0]); b = np.array([5, 0])
            print(a @ b / (np.linalg.norm(a) * np.linalg.norm(b)))
        """, "1.0", "0.0", "25.0", "0.5",
            why = "Gleiche Richtung, gleicher Geschmack.")
        code(2, "Was zerlegt diese Faktorisierung?", """
            # R ≈ U @ V.T  mit R: Nutzer × Filme, U: Nutzer × k, V: Filme × k
        """, "Die Bewertungsmatrix in Nutzer- und Film-Faktoren mit k latenten Geschmacksdimensionen", "Die Filme in Genres", "Die Nutzer in Altersgruppen", "Den Text der Bewertungen",
            why = "Matrixfaktorisierung ist der Kern des kollaborativen Filterns (Netflix-Preis).")
        mc(2, "Was ist das Kaltstartproblem?",
            "Neue Nutzer oder Artikel haben noch keine Bewertungen, kollaboratives Filtern kann sie nicht einordnen", "Der Server ist kalt", "Das Modell startet zu langsam", "Die Bewertungen sind zu alt",
            why = "Inhaltsbasierte Merkmale oder Popularität überbrücken den Kaltstart.")

        on("ev_split")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.model_selection import cross_val_score
            scores = cross_val_score(model, X, y, cv=5)
            print(len(scores))
        """, "5", "1", "10", "Die Zahl der Beispiele",
            why = "Ein Score pro Faltung; man berichtet Mittelwert und Standardabweichung.")
        code(2, "Wozu dient stratify=y?", """
            train_test_split(X, y, test_size=0.2, stratify=y, random_state=0)
        """, "Die Klassenanteile bleiben in Training und Test gleich – wichtig bei unausgewogenen Klassen", "Es sortiert nach y", "Es entfernt seltene Klassen", "Es verdoppelt die Testdaten",
            why = "Sonst landen bei 2 % Positiven womöglich kaum Positive im Test.")

        on("ev_metrics")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.metrics import precision_score, recall_score
            y = [1, 1, 1, 0, 0]; p = [1, 0, 1, 1, 0]
            print(precision_score(y, p), recall_score(y, p))
        """, "0.6666666666666666 0.6666666666666666", "0.5 0.5", "1.0 0.6666666666666666", "0.6 0.6",
            why = "TP = 2, FP = 1, FN = 1 → Precision 2/3, Recall 2/3.")
        code(2, "Was liefert dieser Aufruf?", """
            confusion_matrix(y_true, y_pred)   # zwei Klassen
        """, "Die 2 × 2-Matrix [[TN, FP], [FN, TP]] (Zeilen: wahre Klasse, Spalten: Vorhersage)", "Precision und Recall", "Die ROC-Kurve", "Den F1-Wert",
            why = "scikit-learn ordnet die Klassen aufsteigend: erst 0, dann 1.")

        on("ev_features")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.preprocessing import MinMaxScaler
            print(MinMaxScaler().fit_transform([[10], [20], [30]]).ravel())
        """, "[0.  0.5 1. ]", "[10. 20. 30.]", "[-1.  0.  1.]", "[0.33 0.67 1.  ]",
            why = "x′ = (x − min)/(max − min).")
        code(2, "Was macht dieser Schritt?", """
            SimpleImputer(strategy="median").fit_transform(X)
        """, "Er ersetzt fehlende Werte je Spalte durch den Median (gelernt aus den Trainingsdaten)", "Er löscht Zeilen mit fehlenden Werten", "Er skaliert die Daten", "Er kodiert Kategorien",
            why = "Der Median ist robuster gegen Ausreißer als der Mittelwert.")

        on("ev_tuning")
        code(2, "Was lässt sich aus dieser Ausgabe ablesen: Trainings-Score 0,99, Validierungs-Score 0,71?", """
            print(model.score(X_train, y_train), model.score(X_val, y_val))
        """, "Hohe Varianz (Überanpassung): Regularisieren, vereinfachen oder mehr Daten", "Hoher Bias (Unteranpassung)", "Perfektes Modell", "Ein Datenleck ist sicher",
            why = "Die große Lücke zwischen Training und Validierung ist das Kennzeichen hoher Varianz.")
        code(2, "Was ist der Unterschied zu GridSearchCV?", """
            RandomizedSearchCV(model, param_distributions, n_iter=40, cv=5)
        """, "Es prüft 40 zufällig gezogene Kombinationen statt aller Gitterpunkte", "Es prüft alle Kombinationen", "Es braucht keine Kreuzvalidierung", "Es optimiert Gradienten",
            why = "Bei vielen Hyperparametern effizienter bei gleichem Budget.")

        on("ev_fair")
        code(2, "Was berechnet diese Zeile?", """
            df.groupby("geschlecht").apply(lambda g: recall_score(g.y, g.pred))
        """, "Den Recall getrennt je Gruppe – ein einfacher Fairness-Check", "Die Accuracy insgesamt", "Die Klassenverteilung", "Die Feature-Wichtigkeit",
            why = "Große Unterschiede zwischen Gruppen deuten auf Benachteiligung hin (Equal Opportunity).")
        code(2, "Wozu dient class_weight=\"balanced\"?", """
            LogisticRegression(class_weight="balanced")
        """, "Fehler auf der seltenen Klasse werden stärker gewichtet, umgekehrt proportional zur Klassenhäufigkeit", "Die Klassen werden gelöscht", "Die Merkmale werden skaliert", "Es erzeugt neue Daten",
            why = "Eine Alternative zu Oversampling (SMOTE) oder Undersampling.")
        mc(2, "Was zeigen SHAP-Werte?",
            "Den Beitrag jedes Merkmals zur einzelnen Vorhersage, fair aufgeteilt nach der Shapley-Logik", "Die Accuracy je Klasse", "Die Trainingszeit", "Die Zahl der Bäume",
            why = "Grundlage aus der kooperativen Spieltheorie.")

        on("ai_intro")
        code(2, "Was misst dieser Vergleich?", """
            train_loss, val_loss = 0.05, 0.60
        """, "Die Generalisierungslücke – hier sehr groß, das Modell überanpasst", "Die Lernrate", "Den Bias", "Die Datenmenge",
            why = "Empirisches Risiko auf Trainingsdaten gegen geschätztes wahres Risiko auf Validierungsdaten.")
        codeOut(2, "Was gibt der Code aus?", """
            import numpy as np
            y = np.array([1, 0, 0, 0]); p = np.array([0.8, 0.1, 0.1, 0.2])
            print(round(-np.mean(y * np.log(p) + (1 - y) * np.log(1 - p)), 3))
        """, "0.164", "0.223", "0.5", "1.0",
            why = "(−ln 0,8 − 2 ln 0,9 − ln 0,8)/4 = (0,223 + 0,211 + 0,223)/4 ≈ 0,164 – die mittlere binäre Kreuzentropie.")

        on("ai_linear")
        code(2, "Was ist an dieser Lernrate problematisch, wenn der größte Eigenwert der Hesse-Matrix 4 ist?", """
            eta = 0.6
            w = w - eta * grad
        """, "η > 2/λ_max = 0,5: Der Gradientenabstieg divergiert", "Sie ist viel zu klein", "Sie ist perfekt", "Lernraten über 0,1 sind immer verboten",
            why = "Für eine quadratische Mulde ist Gradientenabstieg genau für η < 2/λ_max stabil.")
        codeOut(2, "Was gibt der Code aus?", """
            import numpy as np
            X = np.array([[1, 0], [1, 1], [1, 2]]); y = np.array([1, 3, 5])
            print(np.linalg.solve(X.T @ X, X.T @ y))
        """, "[1. 2.]", "[2. 1.]", "[0. 2.]", "[1. 3.]",
            why = "Normalengleichung: Die Punkte liegen exakt auf y = 1 + 2x.")
        mc(2, "Warum entspricht die Minimierung der MSE der Maximum-Likelihood-Schätzung?",
            "Unter der Annahme gaußschen Rauschens ist −ln p(y|x) bis auf Konstanten der quadrierte Fehler", "Weil MSE immer null ist", "Weil Likelihood linear ist", "Das stimmt nicht",
            why = "Bei Laplace-Rauschen käme analog die MAE heraus.")

        on("eng_capstone")
        line(1, "Welcher Schritt steht am Anfang des Abschlussprojekts?",
            "Die Frage, die Daten und die Erfolgsmetrik klar festlegen", "Das größte Modell trainieren", "Eine Weboberfläche bauen", "Hyperparameter suchen",
            why = "Erst das Problem, dann die Werkzeuge.")
        code(2, "Was fehlt in diesem Projektablauf am Ende?", """
            df = load(); X_tr, X_te, y_tr, y_te = split(df)
            model = tune(Pipeline(...), X_tr, y_tr)
            print(model.score(X_tr, y_tr))
        """, "Die einmalige Bewertung auf den Testdaten X_te, y_te – der Trainings-Score sagt wenig", "Ein zweites Modell", "Mehr Daten", "Nichts",
            why = "Ehrliche Endbewertung: einmal, auf ungesehenen Daten.")
        mc(1, "Was gehört in einen guten Projektbericht?",
            "Frage, Daten, Vorgehen, Metriken mit Streuung, Fehleranalyse und Grenzen", "Nur die beste Zahl", "Nur der Code", "Nur Diagramme ohne Erklärung",
            why = "Andere sollen die Ergebnisse nachvollziehen und reproduzieren können.")
        codeOut(2, "Was gibt der Code aus?", """
            import numpy as np
            scores = np.array([0.82, 0.85, 0.80, 0.84, 0.84])
            print(round(scores.mean(), 3), round(scores.std(ddof=1), 3))
        """, "0.83 0.02", "0.84 0.0", "0.8 0.05", "0.85 0.02",
            why = "Mittelwert 0,83; Stichproben-Standardabweichung ≈ 0,0200.")
        mc(2, "Warum sollte man eine einfache Baseline trainieren?",
            "Sie zeigt, ob das komplexe Modell überhaupt einen echten Gewinn bringt", "Weil sie immer gewinnt", "Weil der Kurs es so will", "Um Zeit zu verschwenden",
            why = "Etwa „immer Mehrheitsklasse“ oder lineares Modell – alles muss sich daran messen.")
    }
}

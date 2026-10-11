package app.maximus.ai.domain

import app.maximus.lab.domain.fm
import app.maximus.lab.domain.sec

/**
 * Lektion 1 (IBM „Machine Learning with Python“): the new bites on scikit-learn practice — the estimator
 * API, linear and polynomial regression in code, regularisation and pipelines with honest validation.
 */
internal object AiL01Ml {
    val bites: List<Bite> = listOf(
        bite(
            "ml_sklearn", 1, "Die scikit-learn-Schmiede: fit, predict, transform",
            "Die eine Schnittstelle, die alle klassischen Modelle teilen: Schätzer, Transformer, NumPy-Arrays und pandas-Tabellen.",
            goals(
                "Die Estimator-API (fit, predict, score) und die Transformer-API (fit, transform) unterscheiden",
                "Daten mit NumPy und pandas in die Form X (n × d) und y (n) bringen",
                "Einen kompletten Lernzyklus in fünf Zeilen schreiben und die Ausgabe deuten"
            ),
            sec("Warum scikit-learn?", """
                scikit-learn ist die Standardbibliothek für klassisches maschinelles Lernen in Python. Ihr großer Trick ist eine einheitliche Schnittstelle: Ob lineare Regression, Entscheidungsbaum oder k-Means – jedes Modell ist ein Objekt mit denselben Methoden. Wer eine Klasse beherrscht, beherrscht alle.

                • Schätzer (Estimator): lernt mit fit(X, y) und sagt mit predict(X) voraus. Beispiele: LinearRegression, LogisticRegression, DecisionTreeClassifier, KNeighborsClassifier.
                • Transformer: lernt mit fit(X) Kennzahlen der Daten und verändert sie mit transform(X). Beispiele: StandardScaler, OneHotEncoder, PCA.
                • fit_transform(X) ist die Abkürzung für fit gefolgt von transform – nur auf Trainingsdaten benutzen!
                • score(X, y) liefert eine Standardmetrik: R² bei Regressoren, Accuracy bei Klassifikatoren.

                > Gelernte Größen tragen einen Unterstrich am Ende: coef_, intercept_, mean_, classes_, n_features_in_. Existiert das Attribut noch nicht, wurde das Modell nicht trainiert.
            """),
            sec("Die Form der Daten", """
                scikit-learn erwartet die Merkmale als zweidimensionale Matrix X mit n Zeilen (Beispiele) und d Spalten (Merkmale), das Ziel als eindimensionalen Vektor y der Länge n. Der häufigste Anfängerfehler ist ein einzelnes Merkmal als 1D-Array: Dann fehlt die Spaltendimension.

                $ import numpy as np
                $ x = np.array([1.0, 2.0, 3.0, 4.0])   # shape (4,)
                $ X = x.reshape(-1, 1)                  # shape (4, 1): 4 Beispiele, 1 Merkmal
                $ y = np.array([2.1, 3.9, 6.2, 7.8])

                Mit pandas wählt man Spalten über Namen. Doppelte eckige Klammern liefern einen DataFrame (2D), einfache eine Series (1D).

                $ import pandas as pd
                $ df = pd.read_csv("FuelConsumption.csv")
                $ X = df[["ENGINESIZE", "CYLINDERS"]]   # DataFrame, shape (n, 2)
                $ y = df["CO2EMISSIONS"]                # Series, shape (n,)
            """,
                fm("Datenform", "X ∈ ℝ^{n×d},  y ∈ ℝ^n"),
                fm("reshape", "x.shape = (n,) → x.reshape(−1, 1).shape = (n, 1)")
            ),
            sec("Ein ganzer Lernzyklus", """
                Das Muster ist immer gleich: Daten teilen, Modell anlegen, trainieren, vorhersagen, bewerten.

                $ from sklearn.model_selection import train_test_split
                $ from sklearn.linear_model import LinearRegression
                $ X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
                $ model = LinearRegression()
                $ model.fit(X_train, y_train)
                $ y_pred = model.predict(X_test)
                $ print(model.score(X_test, y_test))   # R² auf ungesehenen Daten

                random_state macht die zufällige Aufteilung reproduzierbar. test_size=0.2 hält 20 % der Daten zurück. Wichtig ist die Reihenfolge der vier Rückgabewerte: erst beide X, dann beide y.
            """),
            sec("Transformer richtig benutzen", """
                Ein StandardScaler lernt Mittelwert und Standardabweichung jeder Spalte. Diese Kennzahlen dürfen nur aus den Trainingsdaten stammen – sonst sickert Information aus der Testmenge ins Modell (Datenleck).

                $ from sklearn.preprocessing import StandardScaler
                $ scaler = StandardScaler()
                $ X_train_s = scaler.fit_transform(X_train)   # lernt μ, σ und wendet an
                $ X_test_s = scaler.transform(X_test)         # nur anwenden!

                > Regel der Schmiede: fit nur auf Training; transform auf alles. Später erledigt eine Pipeline das automatisch.
            """,
                fm("Standardisierung", "z = (x − μ_train) / σ_train")
            )
        ),
        bite(
            "ml_linreg_code", 1, "Lineare Regression in scikit-learn: einfach und multipel",
            "Kleinste Quadrate, Normalengleichung, Koeffizienten deuten, MAE, MSE, RMSE und R² berechnen und lesen.",
            goals(
                "Das Kleinste-Quadrate-Problem aufstellen und die Normalengleichung herleiten",
                "Einfache und multiple lineare Regression in scikit-learn trainieren",
                "Koeffizienten, Achsenabschnitt und die Metriken MAE, MSE, RMSE und R² deuten"
            ),
            sec("Das Modell", """
                Lineare Regression sagt eine Zahl als gewichtete Summe der Merkmale voraus. Bei einem Merkmal ist das eine Gerade (einfache Regression), bei mehreren eine Hyperebene (multiple Regression). Die Parameter θ₀ (Achsenabschnitt, intercept) und θ₁ … θ_d (Steigungen, coef) werden so gewählt, dass die Summe der quadrierten Residuen minimal wird.

                Für dich als Physiker ist das der klassische Ausgleichsfit. Mit der Designmatrix X (eine Spalte Einsen für θ₀) ist das Problem konvex und hat die geschlossene Lösung der Normalengleichung – solange XᵀX invertierbar ist, also keine perfekt korrelierten Merkmale vorliegen.
            """,
                fm("Modell", "ŷ = θ₀ + θ₁x₁ + … + θ_d x_d = Xθ"),
                fm("Kleinste Quadrate", "J(θ) = Σᵢ (yᵢ − ŷᵢ)² = ‖y − Xθ‖²"),
                fm("Normalengleichung", "∇J = −2Xᵀ(y − Xθ) = 0  ⇒  θ̂ = (XᵀX)⁻¹Xᵀy"),
                fm("Einfache Regression", "θ₁ = Cov(x, y)/Var(x),  θ₀ = ȳ − θ₁x̄")
            ),
            sec("Im Code", """
                Das IBM-Beispiel: CO₂-Ausstoß von Autos aus Hubraum vorhersagen, danach aus Hubraum, Zylinderzahl und Verbrauch.

                $ from sklearn.linear_model import LinearRegression
                $ reg = LinearRegression()
                $ reg.fit(X_train[["ENGINESIZE"]], y_train)
                $ print(reg.coef_, reg.intercept_)   # z. B. [39.0] 125.3
                $ multi = LinearRegression().fit(X_train[["ENGINESIZE", "CYLINDERS", "FUELCONSUMPTION_COMB"]], y_train)
                $ print(multi.coef_)                 # ein Gewicht je Merkmal

                Deutung: coef_[0] = 39 heißt „ein Liter mehr Hubraum erhöht den vorhergesagten Ausstoß um 39 g/km, wenn alle anderen Merkmale gleich bleiben“. Bei multipler Regression sind Koeffizienten nur vergleichbar, wenn die Merkmale standardisiert wurden – sonst hängt ihre Größe von der Einheit ab.
            """),
            sec("Metriken für Regression", """
                • MAE: mittlerer Betrag des Fehlers, in der Einheit von y, robust gegen Ausreißer.
                • MSE: mittlerer quadratischer Fehler, bestraft große Fehler stark; die Größe, die das Training minimiert.
                • RMSE: Wurzel aus MSE, wieder in der Einheit von y.
                • R²: Anteil der erklärten Varianz. 1 ist perfekt, 0 entspricht der Vorhersage „immer ȳ“, negativ ist schlechter als das.

                $ from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
                $ mae = mean_absolute_error(y_test, y_pred)
                $ rmse = mean_squared_error(y_test, y_pred) ** 0.5
                $ r2 = r2_score(y_test, y_pred)
            """,
                fm("MAE", "MAE = (1/n) Σ |yᵢ − ŷᵢ|"),
                fm("MSE und RMSE", "MSE = (1/n) Σ (yᵢ − ŷᵢ)²,  RMSE = √MSE"),
                fm("Bestimmtheitsmaß", "R² = 1 − Σ(yᵢ − ŷᵢ)² / Σ(yᵢ − ȳ)²")
            ),
            sec("Annahmen und Grenzen", """
                Die lineare Regression ist schnell, gut interpretierbar und braucht kein Hyperparameter-Tuning. Sie setzt aber voraus, dass der Zusammenhang (nach geeigneter Merkmalswahl) linear ist. Residuen sollten keine Struktur zeigen: Ein bogenförmiges Muster im Residuenplot verrät eine fehlende Nichtlinearität, ein Trichter eine wachsende Streuung (Heteroskedastizität).

                • Ausreißer ziehen die Gerade stark, weil der Fehler quadriert eingeht.
                • Stark korrelierte Merkmale (Multikollinearität) machen die Koeffizienten instabil, obwohl die Vorhersage gut bleibt.
                • Mehr Merkmale erhöhen R² auf den Trainingsdaten immer – darum zählt R² auf Testdaten.
            """)
        ),
        bite(
            "ml_polyreg", 2, "Polynomiale und nichtlineare Regression",
            "Nichtlineare Zusammenhänge mit linearen Modellen: Polynommerkmale, Logarithmen, Exponentialfits mit curve_fit und der Preis zu hoher Grade.",
            goals(
                "Erklären, warum polynomiale Regression ein lineares Modell ist",
                "PolynomialFeatures und curve_fit einsetzen",
                "Die Zahl der Polynommerkmale berechnen und Überanpassung erkennen"
            ),
            sec("Linear in den Parametern", """
                „Linear“ in linearer Regression bezieht sich auf die Parameter, nicht auf x. Ersetzt man x durch die neuen Merkmale x, x², x³, bleibt das Modell linear in θ – man kann es mit derselben Normalengleichung lösen. Das ist polynomiale Regression: eine Merkmalstransformation gefolgt von gewöhnlicher linearer Regression.

                Echte nichtlineare Regression liegt vor, wenn die Parameter nichtlinear eingehen, etwa im Exponentialmodell y = a·e^{bx} oder der logistischen Wachstumskurve. Manche lassen sich durch Logarithmieren linearisieren (ln y = ln a + bx), andere fittet man iterativ.
            """,
                fm("Polynommodell", "ŷ = θ₀ + θ₁x + θ₂x² + … + θ_k x^k"),
                fm("Linearisierung", "y = a·e^{bx}  ⇒  ln y = ln a + b·x"),
                fm("Anzahl Polynommerkmale", "Grad k, d Merkmale, mit Bias: C(d + k, k)")
            ),
            sec("PolynomialFeatures", """
                $ from sklearn.preprocessing import PolynomialFeatures
                $ from sklearn.linear_model import LinearRegression
                $ poly = PolynomialFeatures(degree=2)
                $ X_poly = poly.fit_transform(X_train)        # [1, x, x²] bei einem Merkmal
                $ model = LinearRegression().fit(X_poly, y_train)
                $ y_pred = model.predict(poly.transform(X_test))

                Bei zwei Merkmalen a, b und Grad 2 entstehen 1, a, b, a², ab, b² – also auch Wechselwirkungsterme. Die Zahl der Spalten wächst kombinatorisch: Bei 10 Merkmalen und Grad 3 sind es schon 286.
            """),
            sec("Nichtlinearer Fit mit SciPy", """
                Für Modelle mit nichtlinearen Parametern nutzt man scipy.optimize.curve_fit (Levenberg-Marquardt). Ein IBM-Beispiel ist das chinesische Bruttoinlandsprodukt, das einer logistischen Kurve folgt.

                $ import numpy as np
                $ from scipy.optimize import curve_fit
                $ def sigmoid(x, beta1, beta2):
                $     return 1 / (1 + np.exp(-beta1 * (x - beta2)))
                $ popt, pcov = curve_fit(sigmoid, x_norm, y_norm)
                $ print(popt)            # beste Parameter
                $ print(np.sqrt(np.diag(pcov)))   # Standardfehler

                Vor dem Fit normiert man x und y auf Größenordnung 1, sonst konvergiert das Verfahren schlecht. Gute Startwerte (p0) helfen bei mehreren lokalen Minima.
            """),
            sec("Der Grad als Hyperparameter", """
                Mit wachsendem Grad sinkt der Trainingsfehler immer – ein Polynom vom Grad n−1 geht durch alle n Punkte. Der Validierungsfehler fällt erst und steigt dann: die U-Kurve aus Bias und Varianz. Den Grad wählt man darum per Validierung, nie per Trainingsfehler. Im Spiel „Überanpassung“ der Spielwiese kannst du das selbst ausprobieren.

                > Hoher Grad plus Extrapolation ist gefährlich: Außerhalb des Datenbereichs schießen Polynome hoher Ordnung nach ±∞.
            """)
        ),
        bite(
            "ml_regularization", 2, "Regularisierung: Ridge, Lasso und Elastic Net",
            "Strafterme gegen Überanpassung, Bayes-Deutung, Sparsamkeit durch L1, der Hyperparameter α und warum man vorher skaliert.",
            goals(
                "Ridge (L2), Lasso (L1) und Elastic Net mathematisch formulieren",
                "Erklären, warum L1 Koeffizienten exakt auf null setzt und L2 nicht",
                "α per Kreuzvalidierung wählen und Datenlecks vermeiden"
            ),
            sec("Strafe für große Gewichte", """
                Regularisierung ergänzt die Verlustfunktion um einen Term, der große Koeffizienten bestraft. Das Modell muss dann zwischen „passt gut zu den Daten“ und „bleibt einfach“ abwägen. Die Stärke regelt der Hyperparameter α (in scikit-learn alpha; bei LogisticRegression heißt der Kehrwert C = 1/α).

                Ridge hat sogar eine geschlossene Lösung: (XᵀX + αI) ist für α > 0 immer invertierbar – die Regularisierung heilt also auch Multikollinearität. Bayesianisch entspricht L2 einem Gauß-Prior auf θ, L1 einem Laplace-Prior.
            """,
                fm("Ridge (L2)", "J = ‖y − Xθ‖² + α‖θ‖₂²,  θ̂ = (XᵀX + αI)⁻¹Xᵀy"),
                fm("Lasso (L1)", "J = (1/2n)‖y − Xθ‖² + α‖θ‖₁"),
                fm("Elastic Net", "J = (1/2n)‖y − Xθ‖² + αρ‖θ‖₁ + α(1 − ρ)/2 ‖θ‖₂²")
            ),
            sec("Warum Lasso Merkmale auswählt", """
                Geometrisch ist der L1-Ball eine Raute mit Ecken auf den Achsen, der L2-Ball eine Kugel. Die Niveaulinien des quadratischen Fehlers berühren die Raute typischerweise in einer Ecke – dort sind manche Koeffizienten exakt null. Lasso liefert also sparsame Modelle und damit eine eingebaute Merkmalsauswahl. Ridge schrumpft alle Gewichte gleichmäßig, setzt aber keines exakt auf null.

                • Viele irrelevante Merkmale → Lasso.
                • Viele kleine, korrelierte Effekte → Ridge.
                • Gruppen korrelierter Merkmale, trotzdem Auswahl gewünscht → Elastic Net.
            """),
            sec("Im Code", """
                $ from sklearn.linear_model import Ridge, Lasso, ElasticNet
                $ from sklearn.preprocessing import StandardScaler
                $ from sklearn.pipeline import make_pipeline
                $ lasso = make_pipeline(StandardScaler(), Lasso(alpha=0.1))
                $ lasso.fit(X_train, y_train)
                $ coefs = lasso[-1].coef_
                $ print((coefs == 0).sum(), "Merkmale verworfen")

                Skalieren ist Pflicht: Die Strafe hängt von der Größe der Koeffizienten ab, und die hängt von der Einheit der Merkmale ab. Ohne Standardisierung würde ein Merkmal in Millimetern viel stärker bestraft als dasselbe in Metern.
            """),
            sec("Alpha wählen", """
                α = 0 ist gewöhnliche Regression, α → ∞ drückt alle Koeffizienten gegen null (nur noch der Achsenabschnitt bleibt). Man sucht α auf einer logarithmischen Skala mit Kreuzvalidierung; RidgeCV und LassoCV machen das eingebaut.

                $ from sklearn.linear_model import LassoCV
                $ import numpy as np
                $ model = make_pipeline(StandardScaler(), LassoCV(alphas=np.logspace(-3, 1, 30), cv=5))
                $ model.fit(X_train, y_train)
                $ print(model[-1].alpha_)
            """,
                fm("Regularisierungspfad", "θ̂(α): α ↑ ⇒ ‖θ̂‖ ↓, Bias ↑, Varianz ↓")
            )
        ),
        bite(
            "ml_pipelines", 2, "Pipelines, GridSearchCV und Datenlecks",
            "Vorverarbeitung und Modell als ein Objekt, ColumnTransformer für gemischte Tabellen, Hyperparameter-Suche ohne Schummeln.",
            goals(
                "Pipeline und ColumnTransformer aufbauen",
                "GridSearchCV mit Parametern der Form schritt__parameter benutzen",
                "Typische Datenlecks erkennen: Skalierung vor dem Split, Zielvariable in den Merkmalen, Zeitreihen gemischt"
            ),
            sec("Warum Pipelines?", """
                Eine Pipeline kettet Transformer und am Ende einen Schätzer zu einem einzigen Objekt. fit ruft auf jedem Schritt fit_transform und am Ende fit auf; predict ruft transform und am Ende predict. Damit ist garantiert, dass bei Kreuzvalidierung jede Faltung ihre eigene Skalierung lernt – nur aus ihren Trainingsdaten.

                $ from sklearn.pipeline import Pipeline
                $ from sklearn.preprocessing import StandardScaler
                $ from sklearn.decomposition import PCA
                $ from sklearn.svm import SVC
                $ pipe = Pipeline([("scaler", StandardScaler()), ("pca", PCA(n_components=10)), ("svc", SVC())])
                $ pipe.fit(X_train, y_train)
                $ print(pipe.score(X_test, y_test))
            """),
            sec("Gemischte Tabellen: ColumnTransformer", """
                Echte Tabellen haben Zahlen und Kategorien. Der ColumnTransformer wendet je Spaltengruppe eine eigene Vorverarbeitung an und fügt das Ergebnis zusammen.

                $ from sklearn.compose import ColumnTransformer
                $ from sklearn.preprocessing import OneHotEncoder
                $ from sklearn.impute import SimpleImputer
                $ num = Pipeline([("imp", SimpleImputer(strategy="median")), ("sc", StandardScaler())])
                $ pre = ColumnTransformer([("num", num, ["Temp", "Humidity"]), ("cat", OneHotEncoder(handle_unknown="ignore"), ["WindDir", "Location"])])
                $ clf = Pipeline([("pre", pre), ("model", RandomForestClassifier(random_state=0))])

                handle_unknown="ignore" verhindert einen Absturz, wenn in den Testdaten eine Kategorie auftaucht, die im Training fehlte.
            """),
            sec("GridSearchCV", """
                GridSearchCV probiert jede Kombination aus einem Parametergitter mit k-facher Kreuzvalidierung und trainiert am Ende das beste Modell auf allen Trainingsdaten neu. Parameter von Pipeline-Schritten adressiert man mit doppeltem Unterstrich.

                $ from sklearn.model_selection import GridSearchCV, StratifiedKFold
                $ grid = {"model__n_estimators": [100, 300], "model__max_depth": [None, 10, 20]}
                $ cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
                $ search = GridSearchCV(clf, grid, cv=cv, scoring="f1", n_jobs=-1)
                $ search.fit(X_train, y_train)
                $ print(search.best_params_, search.best_score_)
                $ best = search.best_estimator_

                Aufwand: 2 × 3 = 6 Kombinationen mal 5 Faltungen = 30 Trainingsläufe plus ein letzter auf allen Daten. Bei großen Gittern ist RandomizedSearchCV effizienter.
            """,
                fm("Rechenaufwand", "Läufe = (Π |Gitterachsen|) · k + 1")
            ),
            sec("Datenlecks", """
                Ein Datenleck liegt vor, wenn beim Training Information verwendet wird, die zur Vorhersagezeit nicht verfügbar wäre. Das Ergebnis sind glänzende Validierungswerte und ein Absturz im Einsatz.

                • Skalieren, Imputieren oder PCA vor dem Split auf allen Daten: Testinformation fließt in die Vorverarbeitung. Lösung: Pipeline.
                • Merkmale, die das Ziel enthalten oder erst danach entstehen: etwa „Regenmenge heute“ zur Vorhersage von „Regen heute“ (IBM-Abschlussprojekt Australien-Wetter).
                • Zeitreihen zufällig gemischt: Das Modell sieht die Zukunft. Lösung: TimeSeriesSplit.
                • Duplikate desselben Patienten in Training und Test. Lösung: GroupKFold.

                > Wenn ein Ergebnis zu gut ist, um wahr zu sein, suche zuerst das Leck.
            """)
        )
    )

    val questions: List<AiQuestion> = quiz("l1") {
        on("ml_sklearn")
        mc(1, "Welche Methode haben in scikit-learn alle Schätzer gemeinsam, um aus Daten zu lernen?",
            "fit(X, y)", "learn(X, y)", "train(X, y)", "compile(X, y)",
            why = "Die einheitliche API: fit lernt, predict sagt voraus, score bewertet. Transformer haben zusätzlich transform.")
        mc(1, "Woran erkennt man in scikit-learn ein gelerntes Attribut?",
            "Am Unterstrich am Ende des Namens, z. B. coef_", "Am Präfix learned_", "An Großbuchstaben", "Es gibt keine Konvention",
            why = "coef_, intercept_, mean_, classes_ entstehen erst durch fit. Fehlen sie, ist das Modell untrainiert (NotFittedError).")
        codeOut(1, "Welche Form hat X nach dieser Zeile?", """
            x = np.array([3.0, 1.0, 4.0, 1.0, 5.0])
            X = x.reshape(-1, 1)
            print(X.shape)
        """, "(5, 1)", "(1, 5)", "(5,)", "(-1, 1)",
            why = "−1 heißt „so viele wie nötig“: 5 Zeilen, 1 Spalte – genau die Form, die scikit-learn für ein einzelnes Merkmal erwartet.")
        code(1, "Warum wirft dieser Code einen Fehler?", """
            model = LinearRegression()
            model.fit(df["ENGINESIZE"], df["CO2EMISSIONS"])
        """, "X ist eine 1D-Series; scikit-learn erwartet eine 2D-Matrix – df[[\"ENGINESIZE\"]] wäre richtig",
            "LinearRegression braucht immer mindestens zwei Merkmale", "Die Spaltennamen müssen klein geschrieben sein", "fit erwartet zuerst y, dann X",
            why = "Einfache eckige Klammern liefern eine Series (n,), doppelte einen DataFrame (n, 1). Die Fehlermeldung lautet „Expected 2D array, got 1D array instead“.")
        line(1, "Welche Zeile teilt die Daten korrekt in 80 % Training und 20 % Test?",
            "X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.2)",
            "X_tr, y_tr, X_te, y_te = train_test_split(X, y, test_size=0.2)",
            "X_tr, X_te = train_test_split(X, y, test_size=0.8)",
            "train, test = train_test_split(X, y, train_size=0.2)",
            why = "Die Rückgabe ist immer: alle Teile von X, dann alle Teile von y – also X_train, X_test, y_train, y_test.")
        code(2, "Was ist an diesem Code methodisch falsch?", """
            scaler = StandardScaler()
            X_train_s = scaler.fit_transform(X_train)
            X_test_s = scaler.fit_transform(X_test)
        """, "Der Scaler wird auf den Testdaten neu angepasst; er darf dort nur transform anwenden",
            "StandardScaler kann man nicht zweimal verwenden", "fit_transform gibt es nicht", "Die Testdaten müssen gar nicht skaliert werden",
            why = "Training und Test müssen mit denselben μ und σ (aus dem Training) skaliert werden. Ein neues fit auf Test verschiebt die Skala und ist zudem ein kleines Datenleck.")
        mc(1, "Was liefert model.score(X_test, y_test) bei einem Regressor in scikit-learn?",
            "Das Bestimmtheitsmaß R²", "Den mittleren absoluten Fehler", "Die Accuracy", "Den Verlust des letzten Trainingsschritts",
            why = "Regressoren liefern R², Klassifikatoren die Accuracy. Andere Metriken holt man aus sklearn.metrics.")
        codeOut(2, "Was gibt der Code aus?", """
            from sklearn.preprocessing import StandardScaler
            import numpy as np
            X = np.array([[1.0], [2.0], [3.0]])
            s = StandardScaler().fit(X)
            print(s.mean_, s.transform([[2.0]]))
        """, "[2.] [[0.]]", "[2.] [[1.]]", "[0.] [[2.]]", "[1.5] [[0.5]]",
            why = "Der Mittelwert ist 2; der Wert 2 liegt genau auf dem Mittelwert und wird zu z = 0.")

        on("ml_linreg_code")
        mc(1, "Was minimiert die gewöhnliche lineare Regression?",
            "Die Summe der quadrierten Residuen Σ(yᵢ − ŷᵢ)²", "Die Summe der Beträge der Koeffizienten", "Die Zahl der falsch klassifizierten Punkte", "Den größten Einzelfehler",
            why = "Kleinste Quadrate: J(θ) = ‖y − Xθ‖². Die Lösung ist die Normalengleichung θ̂ = (XᵀX)⁻¹Xᵀy.")
        mc(2, "Wann ist die Normalengleichung θ̂ = (XᵀX)⁻¹Xᵀy nicht anwendbar?",
            "Wenn XᵀX singulär ist, etwa bei perfekt korrelierten Merkmalen", "Wenn es mehr Beispiele als Merkmale gibt", "Wenn y negative Werte enthält", "Wenn die Merkmale standardisiert sind",
            why = "Exakte Kollinearität (z. B. Länge in m und in cm als zwei Spalten) macht XᵀX singulär. Ridge-Regularisierung oder das Entfernen von Spalten hilft.")
        mc(1, "Ein Modell hat R² = 0 auf den Testdaten. Was bedeutet das?",
            "Es ist genauso gut wie die konstante Vorhersage des Mittelwerts ȳ", "Es macht keinen einzigen Fehler", "Es sagt immer null voraus", "R² kann nicht null sein",
            why = "R² = 1 − SS_res/SS_tot. Bei SS_res = SS_tot erklärt das Modell nichts über den Mittelwert hinaus; negative Werte sind noch schlechter.")
        codeOut(1, "Was gibt der Code aus?", """
            from sklearn.linear_model import LinearRegression
            import numpy as np
            X = np.array([[0], [1], [2], [3]])
            y = 2 * X.ravel() + 1
            m = LinearRegression().fit(X, y)
            print(round(m.coef_[0], 2), round(m.intercept_, 2))
        """, "2.0 1.0", "1.0 2.0", "3.0 0.0", "2.0 0.0",
            why = "Die Daten liegen exakt auf y = 2x + 1; kleinste Quadrate findet Steigung 2 und Achsenabschnitt 1.")
        line(1, "Welche Zeile berechnet den RMSE?",
            "rmse = mean_squared_error(y_test, y_pred) ** 0.5",
            "rmse = mean_absolute_error(y_test, y_pred) ** 2",
            "rmse = r2_score(y_test, y_pred) ** 0.5",
            "rmse = (y_test - y_pred).mean() ** 0.5",
            why = "RMSE = √MSE. Der Mittelwert der Residuen kann null sein, obwohl die Fehler groß sind – positive und negative heben sich auf.")
        mc(2, "In einer multiplen Regression ist coef_ für ‚Zylinderzahl‘ negativ, obwohl mehr Zylinder allein mehr CO₂ bedeuten. Was ist die wahrscheinlichste Ursache?",
            "Multikollinearität: Hubraum und Verbrauch erklären den Effekt bereits, der Koeffizient misst nur noch den Rest bei festen anderen Merkmalen",
            "Ein Programmierfehler in scikit-learn", "Zylinderzahl ist eine Kategorie und kann nicht in die Regression", "Negative Koeffizienten sind bei Regression unmöglich",
            why = "Ein Koeffizient ist ein partieller Effekt: Änderung von ŷ bei Änderung dieses Merkmals und festen übrigen Merkmalen. Bei stark korrelierten Merkmalen werden Vorzeichen instabil.")
        mc(2, "Der Residuenplot (Residuen gegen ŷ) zeigt einen deutlichen Bogen. Was folgt daraus?",
            "Der Zusammenhang ist nichtlinear – Polynom- oder Transformationsmerkmale fehlen", "Das Modell ist perfekt", "Es gibt zu wenige Daten", "Die Lernrate war zu groß",
            why = "Residuen eines passenden linearen Modells streuen strukturlos um null. Systematische Muster zeigen fehlende Terme.")
        code(2, "Was berechnet diese Funktion?", """
            def f(x, y):
                xm, ym = x.mean(), y.mean()
                return ((x - xm) * (y - ym)).sum() / ((x - xm) ** 2).sum()
        """, "Die Steigung θ₁ der einfachen linearen Regression", "Den Achsenabschnitt θ₀", "Das Bestimmtheitsmaß R²", "Die Korrelation r",
            why = "θ₁ = Cov(x, y)/Var(x). Die Korrelation hätte zusätzlich die Standardabweichung von y im Nenner.")

        on("ml_polyreg")
        mc(1, "Warum ist polynomiale Regression trotz x² und x³ ein lineares Modell?",
            "Sie ist linear in den Parametern θ; nur die Merkmale sind nichtlinear transformiert", "Weil man x² vorher linearisiert", "Sie ist es nicht, sie braucht Gradientenabstieg", "Weil Polynome Geraden sind",
            why = "ŷ = θ₀ + θ₁x + θ₂x² ist eine Linearkombination der Merkmale 1, x, x². Damit gilt die Normalengleichung unverändert.")
        codeOut(2, "Welche Spalten erzeugt dieser Code für ein Merkmal x?", """
            poly = PolynomialFeatures(degree=3)
            print(poly.fit_transform([[2.0]]))
        """, "[[1. 2. 4. 8.]]", "[[2. 4. 8.]]", "[[1. 2. 3.]]", "[[8. 4. 2. 1.]]",
            why = "Mit include_bias=True (Standard) entstehen 1, x, x², x³ – für x = 2 also 1, 2, 4, 8.")
        mc(2, "Wie viele Merkmale erzeugt PolynomialFeatures(degree=2) aus zwei Merkmalen a und b (mit Bias)?",
            "6: 1, a, b, a², ab, b²", "4: 1, a², b², ab", "5: a, b, a², ab, b²", "3: a², ab, b²",
            why = "C(d + k, k) = C(4, 2) = 6. Der Wechselwirkungsterm ab ist mit dabei.")
        mc(1, "Ein Polynom vom Grad 9 trifft alle 10 Trainingspunkte exakt, auf Testdaten ist es schlecht. Wie heißt das?",
            "Überanpassung (Overfitting)", "Unteranpassung (Underfitting)", "Regularisierung", "Konvergenz",
            why = "Grad n−1 interpoliert n Punkte, lernt also auch das Rauschen. Grad per Validierung wählen oder regularisieren.")
        line(2, "Welche Zeile fittet y = a·e^{bx} durch Linearisierung mit einer linearen Regression?",
            "LinearRegression().fit(x.reshape(-1, 1), np.log(y))",
            "LinearRegression().fit(np.exp(x).reshape(-1, 1), y)",
            "LinearRegression().fit(x.reshape(-1, 1), np.exp(y))",
            "LinearRegression().fit(np.log(x).reshape(-1, 1), y)",
            why = "ln y = ln a + b·x ist linear in x. Danach a = e^{intercept_}, b = coef_[0]. Achtung: Das Logarithmieren gewichtet die Fehler um.")
        code(2, "Was liefert curve_fit zurück?", """
            popt, pcov = curve_fit(sigmoid, xdata, ydata)
        """, "Die besten Parameter und ihre Kovarianzmatrix", "Die Vorhersagen und die Residuen", "Den Gradienten und die Hesse-Matrix", "Das trainierte Modell und R²",
            why = "popt minimiert die Fehlerquadrate; √diag(pcov) gibt die Standardfehler der Parameter.")
        mc(3, "Warum ist Extrapolation mit Polynomen hohen Grades besonders gefährlich?",
            "Weil der höchste Term x^k außerhalb des Datenbereichs dominiert und die Kurve nach ±∞ treibt", "Weil scikit-learn das verbietet", "Weil Polynome periodisch sind", "Weil die Koeffizienten dann null werden",
            why = "Innerhalb der Daten halten sich die Terme die Waage; außerhalb wächst |x|^k ungebremst. Physikalisch motivierte Modelle extrapolieren besser.")

        on("ml_regularization")
        mc(1, "Was bewirkt Regularisierung grundsätzlich?",
            "Sie bestraft große Koeffizienten und senkt so die Varianz auf Kosten von etwas Bias", "Sie erhöht die Zahl der Merkmale", "Sie macht das Training schneller", "Sie entfernt Ausreißer",
            why = "Ein Strafterm α·‖θ‖ zieht Gewichte zur Null; einfachere Modelle generalisieren oft besser.")
        mc(2, "Welche Regularisierung setzt Koeffizienten exakt auf null?",
            "L1 (Lasso)", "L2 (Ridge)", "Keine von beiden", "Beide gleichermaßen",
            why = "Die Ecken des L1-Balls liegen auf den Achsen; das Optimum landet oft dort. L2 schrumpft nur.")
        mc(2, "Was passiert bei Ridge für α → ∞?",
            "Alle Koeffizienten (außer dem Achsenabschnitt) gehen gegen null", "Das Modell wird zur Interpolation", "Die Koeffizienten wachsen unbegrenzt", "Nichts, α hat keinen Einfluss",
            why = "Die Strafe dominiert; die Vorhersage wird zur Konstanten ȳ – maximaler Bias, minimale Varianz.")
        code(2, "Warum gehört der Scaler hier vor das Lasso?", """
            model = make_pipeline(StandardScaler(), Lasso(alpha=0.1))
        """, "Die Strafe hängt von der Größe der Koeffizienten ab, die von den Einheiten der Merkmale abhängt",
            "Lasso funktioniert nur mit Werten zwischen 0 und 1", "Sonst konvergiert der Scaler nicht", "Das ist nur Stil, macht keinen Unterschied",
            why = "Ein Merkmal in mm hat 1000-fach kleinere Koeffizienten als in m und würde viel weniger bestraft. Standardisierung macht die Strafe fair.")
        line(2, "Welche Zeile zählt die von Lasso verworfenen Merkmale in einer Pipeline pipe?",
            "(pipe[-1].coef_ == 0).sum()",
            "pipe.coef_.count(0)",
            "len(pipe.dropped_features_)",
            "(pipe[0].coef_ == 0).sum()",
            why = "pipe[-1] ist der letzte Schritt (das Lasso); dessen coef_ enthält exakte Nullen für verworfene Merkmale.")
        mc(3, "Welcher Prior entspricht der L2-Regularisierung in der Bayes-Sicht?",
            "Ein Gauß-Prior mit Mittelwert null auf den Gewichten", "Ein Laplace-Prior", "Ein Gleichverteilungsprior", "Ein Poisson-Prior",
            why = "MAP-Schätzung: −ln p(θ) ∝ ‖θ‖²/(2τ²). Ein Laplace-Prior ∝ e^{−|θ|/b} ergibt L1.")
        mc(2, "Bei LogisticRegression in scikit-learn steht C = 0.01. Was bedeutet das?",
            "Starke Regularisierung, denn C ist die inverse Regularisierungsstärke", "Schwache Regularisierung", "Eine Lernrate von 0,01", "1 % der Daten werden verworfen",
            why = "C = 1/α. Kleines C heißt große Strafe. Das ist eine klassische Prüfungsfalle.")
        codeOut(3, "Was gibt der Code ungefähr aus?", """
            from sklearn.linear_model import Ridge
            import numpy as np
            X = np.array([[1.0], [2.0], [3.0]]); y = np.array([2.0, 4.0, 6.0])
            print(Ridge(alpha=0.0).fit(X, y).coef_, Ridge(alpha=100.0).fit(X, y).coef_)
        """, "[2.] [0.039…]", "[2.] [2.]", "[0.039…] [2.]", "[0.] [2.]",
            why = "Ohne Strafe ergibt sich die exakte Steigung 2. Mit α = 100: θ = Σxy_c/(Σx_c² + α) = 4/(2 + 100) ≈ 0,039 (zentrierte Daten).")

        on("ml_pipelines")
        mc(1, "Was ist der Hauptvorteil einer Pipeline bei der Kreuzvalidierung?",
            "Jede Faltung lernt ihre Vorverarbeitung nur aus ihren eigenen Trainingsdaten – kein Datenleck", "Sie macht das Modell genauer", "Sie spart Speicher", "Sie ersetzt die Testmenge",
            why = "Die Pipeline wird als Ganzes in jeder Faltung neu gefittet; Skalierung, Imputation und PCA sehen die Validierungsfaltung nicht.")
        line(2, "Wie adressiert man in GridSearchCV den Parameter max_depth des Schritts \"model\" einer Pipeline?",
            "{\"model__max_depth\": [5, 10]}",
            "{\"model.max_depth\": [5, 10]}",
            "{\"max_depth\": [5, 10]}",
            "{\"model\": {\"max_depth\": [5, 10]}}",
            why = "Schrittname, doppelter Unterstrich, Parametername. Verschachtelt geht es weiter: pre__num__imp__strategy.")
        mc(2, "Ein Gitter hat 3 Werte für n_estimators und 4 für max_depth, cv = 5. Wie viele Modelle trainiert GridSearchCV (mit refit)?",
            "61", "12", "60", "17",
            why = "3 · 4 = 12 Kombinationen × 5 Faltungen = 60, plus ein abschließendes refit auf allen Trainingsdaten = 61.")
        code(2, "Wozu dient handle_unknown=\"ignore\"?", """
            OneHotEncoder(handle_unknown="ignore")
        """, "Unbekannte Kategorien in neuen Daten werden zu einer Nullzeile statt einen Fehler auszulösen",
            "Fehlende Werte werden ignoriert", "Seltene Kategorien werden gelöscht", "Der Encoder lernt keine Kategorien",
            why = "Taucht beim Vorhersagen eine Stadt auf, die es im Training nicht gab, wären alle One-Hot-Spalten 0 – das Modell läuft weiter.")
        mc(2, "Im IBM-Abschlussprojekt soll ‚RainToday‘ vorhergesagt werden. Welches Merkmal wäre ein Datenleck?",
            "Die heute gemessene Niederschlagsmenge", "Die Luftfeuchtigkeit um 9 Uhr", "Der Ort der Wetterstation", "Die Windrichtung am Vortag",
            why = "Die Regenmenge von heute steht erst fest, wenn man die Antwort schon kennt. Solche Merkmale muss man entfernen oder das Ziel umdefinieren (z. B. RainTomorrow aus Daten von heute).")
        mc(2, "Warum ist ein zufälliger KFold bei Zeitreihen problematisch?",
            "Das Modell trainiert auf zukünftigen Daten und wird auf vergangenen getestet – Information aus der Zukunft leckt", "KFold ist zu langsam", "Zeitreihen haben keine Labels", "Weil die Faltungen gleich groß sind",
            why = "TimeSeriesSplit lässt das Trainingsfenster wachsen und testet immer auf dem nachfolgenden Zeitraum.")
        codeOut(2, "Was ist best_estimator_ nach diesem Code?", """
            search = GridSearchCV(pipe, grid, cv=5, refit=True)
            search.fit(X_train, y_train)
            best = search.best_estimator_
        """, "Die ganze Pipeline mit den besten Parametern, neu trainiert auf allen Trainingsdaten",
            "Nur das letzte Modell ohne Vorverarbeitung", "Das Modell der besten einzelnen Faltung", "Eine Liste aller getesteten Modelle",
            why = "Mit refit=True wird die beste Konfiguration auf dem kompletten X_train neu gefittet – inklusive Scaler und Encoder.")
        mc(3, "Wann ist RandomizedSearchCV einer GridSearchCV überlegen?",
            "Bei vielen Hyperparametern, von denen nur wenige wirklich wichtig sind – Zufallsproben decken jede Achse dichter ab", "Nie, Gittersuche ist immer besser",
            "Nur bei linearen Modellen", "Wenn man keine Kreuzvalidierung will",
            why = "Bergstra & Bengio (2012): Bei gleichem Budget testet die Zufallssuche je wichtiger Achse viel mehr verschiedene Werte als ein Gitter.")
    }
}

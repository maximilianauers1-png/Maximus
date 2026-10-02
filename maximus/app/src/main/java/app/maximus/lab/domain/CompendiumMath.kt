package app.maximus.lab.domain

internal object CompendiumMath {
    val chapters = listOf(
        chapter(
            "ma_analysis", Topic.MATH, 1, "Analysis einer Veränderlichen",
            "Grenzwerte, Ableitung, Integral, Taylor-Reihen und die wichtigsten Sätze.",
            listOf("taylor", "quadrature"),
            sec("Grenzwerte und Stetigkeit", """
                Eine Folge (aₙ) konvergiert gegen a, wenn für jedes ε > 0 ein N existiert mit |aₙ − a| < ε für alle n > N. Eine Funktion ist stetig in x₀, wenn kleine Änderungen des Arguments kleine Änderungen des Wertes bewirken (ε-δ-Definition).

                Wichtige Sätze für stetige Funktionen auf [a, b]: Zwischenwertsatz (jeder Wert zwischen f(a) und f(b) wird angenommen — Grundlage der Bisektion) und Satz vom Maximum (Extrema werden angenommen).
            """,
                fm("Konvergenz", "∀ε > 0 ∃N: |aₙ − a| < ε  ∀n > N"),
                fm("Wichtige Grenzwerte", "lim (1 + 1/n)ⁿ = e,   lim sin x / x = 1 (x → 0)")
            ),
            sec("Differentialrechnung", """
                Die Ableitung ist die lokale lineare Näherung: f(x + h) = f(x) + f'(x)h + o(h). Regeln: linear, Produkt-, Quotienten- und Kettenregel. Mittelwertsatz: Es gibt ein ξ ∈ (a, b) mit f'(ξ) = (f(b) − f(a))/(b − a). L'Hospital für Grenzwerte vom Typ 0/0.
            """,
                fm("Ableitung", "f'(x) = lim_{h→0} (f(x + h) − f(x))/h"),
                fm("Kettenregel", "(f ∘ g)'(x) = f'(g(x)) g'(x)"),
                fm("Produktregel", "(fg)' = f'g + fg'"),
                fm("Mittelwertsatz", "f(b) − f(a) = f'(ξ)(b − a)")
            ),
            sec("Integralrechnung", """
                Das bestimmte Integral ist der Grenzwert von Riemann-Summen. Der Hauptsatz verbindet Ableitung und Integral: Integrieren ist (bis auf eine Konstante) die Umkehrung des Ableitens. Techniken: partielle Integration, Substitution, Partialbruchzerlegung.
            """,
                fm("Hauptsatz", "∫ₐᵇ f(x) dx = F(b) − F(a),   F' = f"),
                fm("Partielle Integration", "∫ u v' dx = uv − ∫ u' v dx"),
                fm("Substitution", "∫ f(g(x)) g'(x) dx = ∫ f(u) du"),
                fm("Gauß-Integral", "∫_{−∞}^{∞} e^{−ax²} dx = √(π/a)")
            ),
            sec("Taylor-Reihen", """
                Eine glatte Funktion lässt sich um x₀ durch Polynome annähern; das Restglied kontrolliert den Fehler. Konvergiert die Reihe gegen f, heißt f analytisch. Der Konvergenzradius ist der Abstand zur nächsten Singularität in der komplexen Ebene — darum hat 1/(1 + x²) Radius 1, obwohl es auf ℝ überall glatt ist.
            """,
                fm("Taylor", "f(x) = Σ f⁽ⁿ⁾(x₀)(x − x₀)ⁿ/n!"),
                fm("Lagrange-Restglied", "R_n = f⁽ⁿ⁺¹⁾(ξ)(x − x₀)ⁿ⁺¹/(n + 1)!"),
                fm("Exponentialreihe", "eˣ = Σ xⁿ/n!"),
                fm("Euler-Formel", "e^{iφ} = cos φ + i sin φ")
            )
        ),
        chapter(
            "ma_vector", Topic.MATH, 2, "Mehrdimensionale Analysis und Vektoranalysis",
            "Partielle Ableitungen, Gradient, Divergenz, Rotation und die Integralsätze.",
            emptyList(),
            sec("Partielle Ableitungen und Gradient", """
                Für f: ℝⁿ → ℝ zeigt der Gradient in Richtung des steilsten Anstiegs; die Richtungsableitung ist ∇f · n. Die Jacobi-Matrix verallgemeinert die Ableitung auf Vektorfunktionen; ihre Determinante beschreibt die Volumenänderung beim Koordinatenwechsel. Die Hesse-Matrix entscheidet über die Art kritischer Punkte.
            """,
                fm("Gradient", "∇f = (∂f/∂x₁, …, ∂f/∂xₙ)"),
                fm("Totales Differential", "df = Σ (∂f/∂xᵢ) dxᵢ"),
                fm("Transformationssatz", "∫ f(x) dⁿx = ∫ f(x(u)) |det J| dⁿu")
            ),
            sec("Divergenz und Rotation", """
                Die Divergenz misst die Quellstärke eines Vektorfeldes, die Rotation seine Wirbelstärke. Identitäten: rot grad = 0 (Gradientenfelder sind wirbelfrei), div rot = 0 (Wirbelfelder sind quellfrei). Helmholtz: Jedes hinreichend schnell abfallende Feld zerlegt sich in einen wirbelfreien und einen quellfreien Anteil.
            """,
                fm("Divergenz", "∇ · F = ∂F_x/∂x + ∂F_y/∂y + ∂F_z/∂z"),
                fm("Rotation", "∇ × F = (∂_yF_z − ∂_zF_y, ∂_zF_x − ∂_xF_z, ∂_xF_y − ∂_yF_x)"),
                fm("Laplace (Kugelkoordinaten)", "∇²f = (1/r²)∂_r(r²∂_rf) + (1/(r² sin θ))∂_θ(sin θ ∂_θf) + (1/(r² sin²θ))∂_φ²f"),
                fm("Identitäten", "∇ × (∇f) = 0,   ∇ · (∇ × F) = 0,   ∇ × (∇ × F) = ∇(∇·F) − ∇²F")
            ),
            sec("Integralsätze", """
                Gauß: Der Fluss durch eine geschlossene Fläche ist das Volumenintegral der Divergenz. Stokes: Die Zirkulation längs einer geschlossenen Kurve ist der Fluss der Rotation durch eine aufgespannte Fläche. Beide sind Spezialfälle des allgemeinen Satzes von Stokes ∫_M dω = ∫_∂M ω für Differentialformen.
            """,
                fm("Gaußscher Satz", "∫_V ∇ · F dV = ∮_∂V F · dA"),
                fm("Satz von Stokes", "∫_A (∇ × F) · dA = ∮_∂A F · dℓ"),
                fm("Allgemeiner Stokes", "∫_M dω = ∫_∂M ω")
            )
        ),
        chapter(
            "ma_linalg", Topic.MATH, 1, "Lineare Algebra",
            "Vektorräume, Matrizen, Determinanten, Eigenwerte, Spektralsatz und SVD.",
            listOf("matrix3", "polynomial"),
            sec("Lineare Gleichungssysteme", """
                Ax = b ist genau dann für jedes b eindeutig lösbar, wenn A invertierbar ist, also det A ≠ 0 bzw. Rang A = n. Numerisch löst man mit LU-Zerlegung und Pivotisierung (Aufwand ~ n³/3), nie über die explizite Inverse. Die Kondition κ(A) = ‖A‖‖A⁻¹‖ gibt an, wie stark Eingabefehler verstärkt werden.
            """,
                fm("Dimensionsformel", "dim ker A + rang A = n"),
                fm("Cramersche Regel", "xᵢ = det Aᵢ / det A"),
                fm("Kondition", "‖δx‖/‖x‖ ≤ κ(A) ‖δb‖/‖b‖")
            ),
            sec("Determinante", """
                Die Determinante ist das orientierte Volumen des von den Spalten aufgespannten Parallelepipeds. Sie ist multiplikativ, ändert bei Zeilenvertauschung das Vorzeichen und ist genau dann null, wenn die Spalten linear abhängig sind.
            """,
                fm("Multiplikativität", "det(AB) = det A · det B"),
                fm("Leibniz", "det A = Σ_σ sgn(σ) Π aᵢ,σ(ᵢ)"),
                fm("Inverse 2×2", "(a b; c d)⁻¹ = (d −b; −c a)/(ad − bc)")
            ),
            sec("Eigenwerte und Diagonalisierung", """
                Av = λv: Eigenvektoren werden nur gestreckt. Die Eigenwerte sind die Nullstellen des charakteristischen Polynoms. Spur = Summe, Determinante = Produkt der Eigenwerte. Hat A n linear unabhängige Eigenvektoren, ist A = VΛV⁻¹ diagonalisierbar; dann sind Matrixfunktionen wie e^{At} leicht.

                Spektralsatz: Symmetrische (hermitesche) Matrizen haben reelle Eigenwerte und eine Orthonormalbasis aus Eigenvektoren — darum sind quantenmechanische Messwerte reell. Singulärwertzerlegung A = UΣV^T existiert für jede Matrix und liefert Rang, Norm und beste Niedrigrang-Näherung.
            """,
                fm("Eigenwertgleichung", "det(A − λI) = 0"),
                fm("Spur und Determinante", "tr A = Σλᵢ,   det A = Πλᵢ"),
                fm("Spektralsatz", "A = A^T ⇒ A = QΛQ^T,  Q orthogonal"),
                fm("SVD", "A = UΣV^T")
            )
        ),
        chapter(
            "ma_ode", Topic.MATH, 2, "Gewöhnliche Differentialgleichungen",
            "Lösungsmethoden, Schwingungen, Systeme, Stabilität und numerische Verfahren.",
            listOf("pendulum", "euler_rk4", "lorenz", "rlc"),
            sec("Lineare Gleichungen", """
                Erste Ordnung y' + p(x)y = q(x): Lösung mit integrierendem Faktor. Lineare Gleichungen mit konstanten Koeffizienten: Ansatz e^{λx} führt auf das charakteristische Polynom. Die allgemeine Lösung ist homogene Lösung plus eine partikuläre (Variation der Konstanten oder Ansatz vom Typ der rechten Seite).
            """,
                fm("Integrierender Faktor", "y = e^{−∫p} (∫ q e^{∫p} dx + C)"),
                fm("Charakteristische Gleichung", "y'' + ay' + by = 0  ⇒  λ² + aλ + b = 0")
            ),
            sec("Schwingungen", """
                Der gedämpfte, getriebene Oszillator ist die wichtigste Gleichung der Physik. Je nach Dämpfung: Schwingfall, aperiodischer Grenzfall, Kriechfall. Bei Anregung zeigt die Amplitude Resonanz nahe ω₀, die Phase springt von 0 auf π.
            """,
                fm("Oszillator", "ẍ + 2δẋ + ω₀²x = (F₀/m) cos ωt"),
                fm("Gedämpfte Frequenz", "ω_d = √(ω₀² − δ²)"),
                fm("Resonanzamplitude", "A(ω) = (F₀/m)/√((ω₀² − ω²)² + 4δ²ω²)")
            ),
            sec("Systeme und Stabilität", """
                Jede Gleichung n-ter Ordnung ist ein System erster Ordnung. Lineare Systeme ẋ = Ax lösen sich mit e^{At}; die Eigenwerte entscheiden über die Stabilität (Realteile < 0: asymptotisch stabil). Nichtlineare Systeme linearisiert man um Fixpunkte (Jacobi-Matrix). Ab drei Dimensionen ist Chaos möglich (Lorenz, 1963); in zwei Dimensionen verbietet es der Satz von Poincaré-Bendixson.
            """,
                fm("Lineares System", "ẋ = Ax  ⇒  x(t) = e^{At}x(0)"),
                fm("Stabilität", "Re λᵢ < 0 ∀i  ⇒  asymptotisch stabil")
            ),
            sec("Numerische Verfahren", """
                Euler explizit ist einfach, aber nur erster Ordnung und für steife Probleme instabil. Runge-Kutta 4 ist der Standard für glatte Probleme. Symplektische Verfahren (Verlet, Leapfrog) erhalten die Phasenraumstruktur Hamiltonscher Systeme und zeigen keine Energiedrift — ideal für Planetenbahnen und Molekulardynamik. Steife Probleme brauchen implizite Verfahren (BDF).
            """,
                fm("Euler", "yₙ₊₁ = yₙ + h f(tₙ, yₙ),   Fehler O(h)"),
                fm("RK4", "yₙ₊₁ = yₙ + (h/6)(k₁ + 2k₂ + 2k₃ + k₄),   Fehler O(h⁴)"),
                fm("Velocity Verlet", "x += vh + ah²/2,   v += (a + a_neu)h/2")
            )
        ),
        chapter(
            "ma_fourier", Topic.MATH, 2, "Fourier-Analyse und partielle DGL",
            "Fourier-Reihen und -Transformation, Faltung und die drei Grundtypen von PDGL.",
            listOf("fourier", "heat_rod"),
            sec("Fourier-Reihen", """
                Periodische Funktionen zerlegen sich in Sinus- und Kosinusschwingungen. Die Koeffizienten folgen aus Orthogonalitätsrelationen. Glattheit bestimmt die Abklingrate der Koeffizienten; Sprünge erzeugen das Gibbs-Phänomen. Parseval: Die Energie im Zeitbereich gleicht der Summe der Energien der Moden.
            """,
                fm("Komplexe Reihe", "f(x) = Σ cₙ e^{inx},   cₙ = (1/2π)∫_{−π}^{π} f e^{−inx} dx"),
                fm("Parseval", "(1/2π)∫|f|² dx = Σ |cₙ|²")
            ),
            sec("Fourier-Transformation", """
                Für nichtperiodische Funktionen wird die Summe zum Integral. Ableitungen werden zu Multiplikationen mit ik, Faltungen zu Produkten. Die Unschärferelation Δx Δk ≥ 1/2 ist eine Eigenschaft der Fourier-Transformation — die Quantenmechanik erbt sie. Numerisch: FFT mit Aufwand N log N (Cooley-Tukey).
            """,
                fm("Transformation", "f̂(k) = ∫ f(x) e^{−ikx} dx,   f(x) = (1/2π)∫ f̂(k) e^{ikx} dk"),
                fm("Faltungssatz", "(f * g)^ = f̂ · ĝ"),
                fm("Ableitung", "(f')^ = ik f̂"),
                fm("Gauß", "e^{−x²/2σ²}  ↔  σ√(2π) e^{−σ²k²/2}")
            ),
            sec("Partielle Differentialgleichungen", """
                • Wärmeleitungsgleichung (parabolisch): u_t = a∇²u, glättet und vergisst Details.
                • Wellengleichung (hyperbolisch): u_tt = c²∇²u, transportiert Information mit endlicher Geschwindigkeit; d'Alembert-Lösung in 1D.
                • Laplace-/Poisson-Gleichung (elliptisch): Gleichgewichtszustände, Randwertprobleme.

                Separation der Variablen plus Fourier-Reihen löst sie auf einfachen Gebieten; numerisch Finite Differenzen, Finite Elemente, Spektralmethoden.
            """,
                fm("Wärmeleitungskern", "u(x, t) = (4πat)^{−1/2} e^{−x²/4at}"),
                fm("d'Alembert", "u(x, t) = f(x − ct) + g(x + ct)"),
                fm("Separation (Stab)", "u = Σ bₙ e^{−a(nπ/L)²t} sin(nπx/L)")
            )
        ),
        chapter(
            "ma_complex", Topic.MATH, 3, "Komplexe Analysis",
            "Holomorphie, Cauchy-Riemann, Cauchys Integralsatz, Laurent-Reihen und Residuensatz.",
            listOf("complex"),
            sec("Holomorphe Funktionen", """
                Eine komplexe Funktion ist holomorph, wenn sie komplex differenzierbar ist; das ist viel stärker als reelle Differenzierbarkeit: Sie ist dann beliebig oft differenzierbar und analytisch. Real- und Imaginärteil erfüllen die Cauchy-Riemann-Gleichungen und sind harmonisch.
            """,
                fm("Cauchy-Riemann", "∂u/∂x = ∂v/∂y,   ∂u/∂y = −∂v/∂x"),
                fm("Harmonisch", "∇²u = ∇²v = 0")
            ),
            sec("Cauchy und Residuen", """
                Das Integral einer holomorphen Funktion über eine geschlossene Kurve verschwindet. Die Cauchysche Integralformel bestimmt Funktionswerte im Inneren aus den Randwerten. An isolierten Singularitäten entwickelt man in Laurent-Reihen; das Residuum ist der Koeffizient von 1/(z − z₀).

                Mit dem Residuensatz berechnet man reelle Integrale, die sonst kaum zugänglich sind, z. B. ∫ dx/(1 + x²) = π oder Fourier-Integrale über Jordans Lemma. In der Physik: Kramers-Kronig-Relationen, Green-Funktionen, die iε-Vorschrift des Feynman-Propagators.
            """,
                fm("Integralsatz", "∮ f(z) dz = 0"),
                fm("Integralformel", "f(z₀) = (1/2πi) ∮ f(z)/(z − z₀) dz"),
                fm("Residuensatz", "∮ f(z) dz = 2πi Σ Res(f, zₖ)"),
                fm("Einfacher Pol", "Res(f, z₀) = lim (z − z₀) f(z)")
            )
        ),
        chapter(
            "ma_prob", Topic.MATH, 1, "Wahrscheinlichkeit und Statistik",
            "Bayes, Verteilungen, Grenzwertsätze, Schätzer, Fehlerfortpflanzung und Regression.",
            listOf("binomial"),
            sec("Grundbegriffe", """
                Kolmogorovs Axiome: P ≥ 0, P(Ω) = 1, σ-Additivität. Bedingte Wahrscheinlichkeit und der Satz von Bayes kehren Schlüsse um — wichtig etwa bei medizinischen Tests mit kleiner Prävalenz (viele positive Befunde sind falsch positiv).
            """,
                fm("Bayes", "P(A|B) = P(B|A) P(A)/P(B)"),
                fm("Totale Wahrscheinlichkeit", "P(B) = Σ P(B|Aᵢ) P(Aᵢ)")
            ),
            sec("Verteilungen und Kenngrößen", """
                • Binomial B(n, p): Anzahl Erfolge, μ = np, σ² = np(1 − p).
                • Poisson(λ): seltene Ereignisse, μ = σ² = λ (radioaktiver Zerfall, Zählraten).
                • Normal N(μ, σ²): Summe vieler kleiner Einflüsse.
                • Exponential: Wartezeiten, gedächtnislos.

                Zentraler Grenzwertsatz: Die normierte Summe vieler unabhängiger Zufallsvariablen mit endlicher Varianz ist näherungsweise normalverteilt. Der Mittelwert von n Messungen hat die Standardabweichung σ/√n.
            """,
                fm("Erwartungswert, Varianz", "E[X] = Σ x p(x),   Var X = E[X²] − E[X]²"),
                fm("Normalverteilung", "φ(x) = (1/(σ√(2π))) e^{−(x−μ)²/(2σ²)}"),
                fm("Standardfehler", "σ_x̄ = σ/√n"),
                fm("Tschebyschow", "P(|X − μ| ≥ kσ) ≤ 1/k²")
            ),
            sec("Messfehler und Regression", """
                Gaußsche Fehlerfortpflanzung für unabhängige Fehler: quadratische Addition, gewichtet mit den partiellen Ableitungen. Die Methode der kleinsten Quadrate minimiert die Summe der quadrierten Residuen; für eine Gerade gibt es geschlossene Formeln samt Unsicherheiten der Parameter. Das Bestimmtheitsmaß R² misst den erklärten Varianzanteil.
            """,
                fm("Fehlerfortpflanzung", "σ_f² = Σ (∂f/∂xᵢ)² σᵢ²"),
                fm("Geradensteigung", "b = Σ(xᵢ − x̄)(yᵢ − ȳ) / Σ(xᵢ − x̄)²"),
                fm("Bestimmtheitsmaß", "R² = 1 − SS_res/SS_tot")
            )
        ),
        chapter(
            "ma_special", Topic.MATH, 3, "Spezielle Funktionen und Gruppentheorie",
            "Gamma, orthogonale Polynome, Bessel-Funktionen, Gruppen, Darstellungen und Lie-Algebren.",
            listOf("oscillator", "hydrogen"),
            sec("Spezielle Funktionen", """
                Sie entstehen als Lösungen der Differentialgleichungen der Physik nach Separation der Variablen:
                • Gamma-Funktion: verallgemeinert die Fakultät.
                • Hermite-Polynome: harmonischer Oszillator.
                • Legendre-Polynome und Kugelflächenfunktionen: Laplace-Gleichung in Kugelkoordinaten, Multipole, Drehimpuls.
                • Laguerre-Polynome: Radialfunktionen des Wasserstoffs.
                • Bessel-Funktionen: Zylindersymmetrie (Trommelfell, Wellenleiter, Beugung an der Kreisblende).

                Orthogonale Polynome erfüllen Drei-Term-Rekursionen — numerisch der stabile Weg zur Auswertung.
            """,
                fm("Gamma-Funktion", "Γ(z) = ∫₀^∞ t^{z−1} e^{−t} dt,   Γ(n + 1) = n!"),
                fm("Stirling", "ln n! ≈ n ln n − n + ½ ln(2πn)"),
                fm("Hermite-Rekursion", "H_{n+1} = 2xHₙ − 2nH_{n−1}"),
                fm("Legendre-Rekursion", "(n + 1)P_{n+1} = (2n + 1)xPₙ − nP_{n−1}"),
                fm("Bessel-Gleichung", "x²y'' + xy' + (x² − ν²)y = 0")
            ),
            sec("Gruppen und Symmetrien", """
                Symmetrien bilden Gruppen: Abgeschlossenheit, Assoziativität, neutrales Element, Inverse. Diskrete Gruppen (Punktgruppen der Kristalle), kontinuierliche Lie-Gruppen (Drehungen SO(3), Lorentz-Gruppe, Eichgruppen U(1), SU(2), SU(3)).

                Eine Lie-Gruppe wird nahe der Identität durch ihre Lie-Algebra beschrieben: Generatoren mit Kommutatorrelationen. SU(2) ist die zweifache Überlagerung von SO(3) — deshalb gibt es halbzahligen Spin. Darstellungstheorie ordnet Teilchen in Multipletts (Isospin, Quarkmodell SU(3)_flavor).
            """,
                fm("Lie-Algebra su(2)", "[Jᵢ, Jⱼ] = iεᵢⱼₖ Jₖ"),
                fm("Gruppenelement", "U = exp(−iθ n · J)"),
                fm("Dimension SU(N)", "dim SU(N) = N² − 1")
            )
        )
    )
}

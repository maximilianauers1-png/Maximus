package app.maximus.lab.domain

/**
 * Mathematics for physicists, ordered like the lectures of a physics degree:
 * Grundlagen → Analysis I → Analysis II → Analysis III → Lineare Algebra → DGL → Funktionentheorie →
 * Fourier & PDGL → Funktionalanalysis → Variationsrechnung & Tensoren → Stochastik & Numerik.
 */
internal object CompendiumMath {
    private val M = Topic.MATH

    val chapters: List<Chapter> =
        course("Grundlagen", *MathBasics.chapters) +
            course("Analysis I", *MathAnalysis1.chapters) +
            course("Analysis II", *MathAnalysis2.chapters) +
            course("Analysis III", *MathAnalysis3.chapters) +
            course("Lineare Algebra", *MathLinearAlgebra.chapters) +
            course("Differentialgleichungen", *MathOde.chapters) +
            course("Funktionentheorie", *MathComplex.chapters) +
            course("Fourier-Analysis und PDGL", *MathFourier.chapters) +
            course("Funktionalanalysis", *MathFunctional.chapters) +
            course("Variationsrechnung und Tensoren", *MathGeometry.chapters) +
            course("Stochastik und Numerik", *MathStochastics.chapters)

    internal fun ch(key: String, level: Int, title: String, summary: String, calcs: List<String>, vararg sections: Section) =
        chapter(key, M, level, title, summary, calcs, *sections)
}

internal object MathBasics {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_basics", 1, "Logik, Mengen, Beweise",
            "Das Handwerkszeug: Aussagenlogik, Quantoren, Mengen, Abbildungen, vollständige Induktion.",
            emptyList(),
            sec("Aussagen und Quantoren", """
                Eine Implikation A ⇒ B ist nur falsch, wenn A wahr und B falsch ist. Sie ist äquivalent zur Kontraposition ¬B ⇒ ¬A — die Grundlage indirekter Beweise. Beim Widerspruchsbeweis nimmt man ¬B zusammen mit A an und leitet einen Widerspruch her.

                Quantoren werden bei der Negation vertauscht: ¬(∀x: P(x)) ⇔ ∃x: ¬P(x). Die Reihenfolge ist entscheidend: „∀ε ∃δ“ (Stetigkeit) ist etwas völlig anderes als „∃δ ∀ε“.
            """,
                fm("Kontraposition", "(A ⇒ B) ⇔ (¬B ⇒ ¬A)"),
                fm("De Morgan", "¬(A ∧ B) ⇔ ¬A ∨ ¬B,   ¬(A ∨ B) ⇔ ¬A ∧ ¬B"),
                fm("Negation von Quantoren", "¬∀x P(x) ⇔ ∃x ¬P(x)")
            ),
            sec("Mengen und Abbildungen", """
                Eine Abbildung f: X → Y ist injektiv (verschiedene Urbilder haben verschiedene Bilder), surjektiv (jedes y wird getroffen) oder bijektiv (beides, dann existiert f⁻¹). Mächtigkeiten vergleicht man über Bijektionen: ℕ, ℤ und ℚ sind abzählbar, ℝ ist überabzählbar (Cantors Diagonalargument). Die Potenzmenge ist stets mächtiger als die Menge selbst.
            """,
                fm("Mengenalgebra", "A ∖ (B ∪ C) = (A ∖ B) ∩ (A ∖ C)"),
                fm("Urbild", "f⁻¹(A ∩ B) = f⁻¹(A) ∩ f⁻¹(B),   f⁻¹(Aᶜ) = (f⁻¹(A))ᶜ")
            ),
            sec("Vollständige Induktion und wichtige Ungleichungen", """
                Induktion: Aussage für n₀ zeigen (Anfang) und A(n) ⇒ A(n+1) (Schritt). Damit beweist man Summenformeln, die Bernoulli-Ungleichung und den binomischen Lehrsatz.

                Die Dreiecksungleichung, die Ungleichung zwischen arithmetischem und geometrischem Mittel und die Cauchy-Schwarz-Ungleichung sind die meistgenutzten Abschätzungen der Analysis.
            """,
                fm("Gauß-Summe", "Σ_{k=1}^n k = n(n+1)/2,   Σ k² = n(n+1)(2n+1)/6"),
                fm("Geometrische Summe", "Σ_{k=0}^n qᵏ = (1 − q^{n+1})/(1 − q),  q ≠ 1"),
                fm("Binomischer Lehrsatz", "(a + b)ⁿ = Σ_{k=0}^n C(n,k) aᵏ b^{n−k}"),
                fm("Bernoulli-Ungleichung", "(1 + x)ⁿ ≥ 1 + nx  für x ≥ −1"),
                fm("Dreiecksungleichung", "|a + b| ≤ |a| + |b|,   ||a| − |b|| ≤ |a − b|"),
                fm("AM-GM", "(x₁ + … + xₙ)/n ≥ (x₁⋯xₙ)^{1/n}  (xᵢ ≥ 0)"),
                fm("Cauchy-Schwarz", "|⟨x, y⟩| ≤ ‖x‖ ‖y‖")
            ),
            sec("Zahlbereiche und Vollständigkeit", """
                ℕ ⊂ ℤ ⊂ ℚ ⊂ ℝ ⊂ ℂ. ℚ hat Lücken (√2 ∉ ℚ). ℝ ist ein vollständig angeordneter Körper: Jede nach oben beschränkte, nichtleere Menge besitzt ein Supremum (Vollständigkeitsaxiom). Äquivalent: Jede Cauchy-Folge konvergiert; jede Intervallschachtelung enthält genau einen Punkt. Archimedisches Axiom: Zu jedem x gibt es n ∈ ℕ mit n > x.
            """,
                fm("Supremum", "s = sup A ⇔ s ist obere Schranke und ∀ε > 0 ∃a ∈ A: a > s − ε")
            )
        )
    )
}

internal object MathAnalysis1 {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_seq", 1, "Folgen und Konvergenz",
            "Grenzwerte, Monotonie, Bolzano-Weierstraß, Cauchy-Folgen, Limes superior.",
            emptyList(),
            sec("Konvergenz", """
                (aₙ) konvergiert gegen a, wenn in jeder ε-Umgebung von a fast alle Folgenglieder liegen. Grenzwerte sind eindeutig, konvergente Folgen beschränkt. Grenzwertsätze erlauben, Summen, Produkte und Quotienten gliedweise zu behandeln (Nenner ≠ 0).

                Sandwich-Lemma: aₙ ≤ bₙ ≤ cₙ und aₙ, cₙ → a ⇒ bₙ → a.
            """,
                fm("Definition", "aₙ → a ⇔ ∀ε > 0 ∃N ∀n ≥ N: |aₙ − a| < ε"),
                fm("Grenzwertsätze", "lim(aₙ + bₙ) = lim aₙ + lim bₙ,   lim(aₙbₙ) = lim aₙ · lim bₙ")
            ),
            sec("Existenzsätze", """
                • Monotonieprinzip: Jede monotone, beschränkte Folge konvergiert (gegen sup bzw. inf).
                • Bolzano-Weierstraß: Jede beschränkte Folge in ℝⁿ hat eine konvergente Teilfolge.
                • Cauchy-Kriterium: (aₙ) konvergiert ⇔ ∀ε ∃N: |aₙ − aₘ| < ε für n, m ≥ N. Man muss den Grenzwert nicht kennen.

                Der Limes superior ist der größte Häufungspunkt; eine beschränkte Folge konvergiert genau dann, wenn lim sup = lim inf.
            """,
                fm("Cauchy-Folge", "∀ε > 0 ∃N ∀n, m ≥ N: |aₙ − aₘ| < ε"),
                fm("Limes superior", "lim sup aₙ = lim_{n→∞} sup_{k≥n} a_k")
            ),
            sec("Wichtige Grenzwerte", """
                Diese Grenzwerte sollte man auswendig können; sie tauchen überall in Abschätzungen auf. Exponentialfunktionen schlagen jede Potenz, Potenzen jeden Logarithmus, die Fakultät jede Exponentialfunktion.
            """,
                fm("Euler-Zahl", "lim (1 + x/n)ⁿ = eˣ"),
                fm("n-te Wurzel", "lim ⁿ√n = 1,   lim ⁿ√a = 1  (a > 0)"),
                fm("Wachstumshierarchie", "lim (ln n)ᵃ/nᵇ = 0,   lim nᵇ/qⁿ = 0 (q > 1),   lim qⁿ/n! = 0"),
                fm("Stirling", "n! ~ √(2πn) (n/e)ⁿ")
            )
        ),
        CompendiumMath.ch(
            "ma_series", 1, "Reihen und Potenzreihen",
            "Konvergenzkriterien, absolute Konvergenz, Umordnung, Cauchy-Produkt, Konvergenzradius.",
            listOf("taylor"),
            sec("Konvergenzkriterien", """
                Eine Reihe Σaₖ konvergiert, wenn ihre Partialsummen konvergieren. Notwendig ist aₖ → 0 (nicht hinreichend: harmonische Reihe!).

                • Majorantenkriterium: |aₖ| ≤ bₖ und Σbₖ konvergent ⇒ Σaₖ absolut konvergent.
                • Quotientenkriterium: lim sup |aₖ₊₁/aₖ| < 1 ⇒ absolut konvergent; > 1 ⇒ divergent.
                • Wurzelkriterium: lim sup ᵏ√|aₖ| < 1 ⇒ absolut konvergent (schärfer als Quotient).
                • Leibniz: alternierende Reihe mit monoton fallender Nullfolge konvergiert; Fehler ≤ erstes weggelassenes Glied.
                • Integralkriterium: Für f ≥ 0 monoton fallend konvergiert Σf(k) ⇔ ∫₁^∞ f dx < ∞.
                • Cauchy-Verdichtung: Σaₖ ↔ Σ 2ᵏ a_{2ᵏ} (aₖ monoton fallend).
            """,
                fm("Harmonische Reihe", "Σ 1/k = ∞,   Σ 1/kˢ < ∞ ⇔ s > 1"),
                fm("Geometrische Reihe", "Σ_{k=0}^∞ qᵏ = 1/(1 − q),  |q| < 1"),
                fm("Basler Problem", "Σ 1/k² = π²/6"),
                fm("Alternierende harmonische Reihe", "Σ (−1)^{k+1}/k = ln 2"),
                fm("Teleskopsumme", "Σ_{k=1}^∞ 1/(k(k+1)) = 1")
            ),
            sec("Absolute Konvergenz und Umordnung", """
                Absolut konvergente Reihen darf man beliebig umordnen und mit dem Cauchy-Produkt multiplizieren. Bedingt konvergente Reihen nicht: Nach dem Riemannschen Umordnungssatz lässt sich jeder Wert in ℝ ∪ {±∞} durch Umordnung erzeugen.
            """,
                fm("Cauchy-Produkt", "(Σaₖ)(Σbₖ) = Σₙ Σ_{k=0}^n a_k b_{n−k}")
            ),
            sec("Potenzreihen", """
                Σcₙ(x − x₀)ⁿ konvergiert absolut für |x − x₀| < R und divergiert für |x − x₀| > R; am Rand ist alles möglich. Im Inneren darf man gliedweise differenzieren und integrieren — der Radius bleibt gleich. Der Radius ist der Abstand zur nächsten Singularität in ℂ.
            """,
                fm("Cauchy-Hadamard", "1/R = lim sup ⁿ√|cₙ|"),
                fm("Quotientenformel", "R = lim |cₙ/cₙ₊₁|  (falls existent)")
            )
        ),
        CompendiumMath.ch(
            "ma_continuity", 1, "Stetigkeit und elementare Funktionen",
            "ε-δ-Stetigkeit, Zwischenwertsatz, gleichmäßige und Lipschitz-Stetigkeit, Exponential- und Winkelfunktionen.",
            emptyList(),
            sec("Stetigkeit", """
                f ist stetig in x₀, wenn f(xₙ) → f(x₀) für jede Folge xₙ → x₀ (Folgenkriterium), äquivalent ε-δ. Summen, Produkte, Quotienten und Verkettungen stetiger Funktionen sind stetig.

                Auf kompakten Intervallen [a, b] gilt für stetige f: Zwischenwertsatz (jeder Wert zwischen f(a) und f(b) wird angenommen), Extremwertsatz (Maximum und Minimum werden angenommen) und gleichmäßige Stetigkeit (δ hängt nicht von x ab, Heine).
            """,
                fm("ε-δ-Stetigkeit", "∀ε > 0 ∃δ > 0 ∀x: |x − x₀| < δ ⇒ |f(x) − f(x₀)| < ε"),
                fm("Lipschitz", "|f(x) − f(y)| ≤ L |x − y|  ⇒ gleichmäßig stetig"),
                fm("Hierarchie", "stetig differenzierbar ⇒ lokal Lipschitz ⇒ gleichmäßig stetig (auf kompakt) ⇒ stetig")
            ),
            sec("Exponentialfunktion, Logarithmus, Winkelfunktionen", """
                exp ist über ihre Potenzreihe definiert; die Funktionalgleichung exp(x + y) = exp(x)exp(y) folgt aus dem Cauchy-Produkt. Der Logarithmus ist ihre Umkehrfunktion, allgemeine Potenzen aˣ = e^{x ln a}. Sinus und Kosinus definiert man ebenfalls über Reihen oder über e^{ix}; π ist dann das Doppelte der kleinsten positiven Nullstelle des Kosinus.

                Hyperbelfunktionen: cosh² − sinh² = 1; sie parametrisieren die Hyperbel wie cos, sin den Kreis — und die Lorentz-Transformation (Rapidität).
            """,
                fm("Exponentialreihe", "eˣ = Σ xⁿ/n!"),
                fm("Euler", "e^{ix} = cos x + i sin x,   cos x = (e^{ix} + e^{−ix})/2"),
                fm("Additionstheoreme", "sin(x ± y) = sin x cos y ± cos x sin y,   cos(x ± y) = cos x cos y ∓ sin x sin y"),
                fm("Hyperbelfunktionen", "cosh x = (eˣ + e^{−x})/2,   sinh x = (eˣ − e^{−x})/2,   cosh² − sinh² = 1"),
                fm("Areafunktionen", "arsinh x = ln(x + √(x² + 1)),   artanh x = ½ ln((1 + x)/(1 − x))")
            )
        ),
        CompendiumMath.ch(
            "ma_analysis", 1, "Differentialrechnung einer Variablen",
            "Ableitung, Regeln und Tabelle, Mittelwertsätze, Kurvendiskussion, L'Hospital, Newton-Verfahren.",
            listOf("polynomial"),
            sec("Ableitung und Rechenregeln", """
                f ist differenzierbar in x₀, wenn der Differenzenquotient konvergiert; gleichwertig: f(x₀ + h) = f(x₀) + f'(x₀)h + o(h). Differenzierbar ⇒ stetig, nicht umgekehrt (|x| in 0, Weierstraß-Funktion: überall stetig, nirgends differenzierbar).
            """,
                fm("Definition", "f'(x₀) = lim_{h→0} (f(x₀ + h) − f(x₀))/h"),
                fm("Produktregel", "(fg)' = f'g + fg'"),
                fm("Quotientenregel", "(f/g)' = (f'g − fg')/g²"),
                fm("Kettenregel", "(f ∘ g)'(x) = f'(g(x)) · g'(x)"),
                fm("Umkehrfunktion", "(f⁻¹)'(y) = 1/f'(f⁻¹(y))"),
                fm("Logarithmische Ableitung", "(f^g)' = f^g (g' ln f + g f'/f)")
            ),
            sec("Ableitungstabelle", """
                Die Standardableitungen — die Grundlage jeder Rechnung. Merkhilfe für arcsin und arctan: Ableitung der Umkehrfunktion mit sin² + cos² = 1 bzw. 1 + tan² = 1/cos².
            """,
                fm("Potenzen", "(xⁿ)' = n xⁿ⁻¹,   (√x)' = 1/(2√x)"),
                fm("Exponential und Log", "(eˣ)' = eˣ,   (aˣ)' = aˣ ln a,   (ln x)' = 1/x,   (log_a x)' = 1/(x ln a)"),
                fm("Trigonometrisch", "(sin x)' = cos x,   (cos x)' = −sin x,   (tan x)' = 1/cos² x = 1 + tan² x"),
                fm("Arcusfunktionen", "(arcsin x)' = 1/√(1 − x²),   (arccos x)' = −1/√(1 − x²),   (arctan x)' = 1/(1 + x²)"),
                fm("Hyperbolisch", "(sinh x)' = cosh x,   (cosh x)' = sinh x,   (tanh x)' = 1/cosh² x"),
                fm("Areafunktionen", "(arsinh x)' = 1/√(x² + 1),   (artanh x)' = 1/(1 − x²)")
            ),
            sec("Mittelwertsätze und Kurvendiskussion", """
                Rolle: f(a) = f(b) ⇒ ∃ξ: f'(ξ) = 0. Daraus folgt der Mittelwertsatz und mit ihm: f' > 0 ⇒ streng monoton wachsend; f' ≡ 0 ⇒ konstant.

                Kurvendiskussion: Nullstellen, Extrema (f' = 0 und f'' ≠ 0, allgemein erste nichtverschwindende Ableitung gerader Ordnung), Wendepunkte (f'' wechselt das Vorzeichen), Konvexität (f'' ≥ 0 ⇔ f konvex), Asymptoten.
            """,
                fm("Mittelwertsatz", "f(b) − f(a) = f'(ξ)(b − a)"),
                fm("Verallgemeinerter MWS", "(f(b) − f(a)) g'(ξ) = (g(b) − g(a)) f'(ξ)"),
                fm("Konvexität", "f(λx + (1−λ)y) ≤ λf(x) + (1−λ)f(y)"),
                fm("Jensen", "f(Σλᵢxᵢ) ≤ Σλᵢ f(xᵢ)  (f konvex, Σλᵢ = 1)")
            ),
            sec("Grenzwerte mit L'Hospital und Newton-Verfahren", """
                L'Hospital: Für unbestimmte Ausdrücke 0/0 oder ∞/∞ gilt lim f/g = lim f'/g', falls der rechte Grenzwert existiert. Andere Formen (0·∞, ∞ − ∞, 1^∞, 0⁰) bringt man durch Umformen oder Logarithmieren auf diese Gestalt.

                Newton-Verfahren: Nullstellen durch Tangenten-Iteration; nahe einer einfachen Nullstelle konvergiert es quadratisch (die Zahl der korrekten Stellen verdoppelt sich pro Schritt).
            """,
                fm("L'Hospital", "lim f/g = lim f'/g'  (bei 0/0 oder ∞/∞)"),
                fm("Newton-Iteration", "x_{n+1} = xₙ − f(xₙ)/f'(xₙ)"),
                fm("Quadratische Konvergenz", "|e_{n+1}| ≈ |f''/(2f')| eₙ²")
            )
        ),
        CompendiumMath.ch(
            "ma_taylor", 2, "Taylor-Entwicklung und Funktionenfolgen",
            "Restglieder, Standardreihen, Landau-Symbole, gleichmäßige Konvergenz und Vertauschungssätze.",
            listOf("taylor"),
            sec("Taylor-Formel", """
                Das Taylor-Polynom n-ter Ordnung approximiert f bis auf ein Restglied O((x − x₀)^{n+1}). Für die Fehlerabschätzung nutzt man die Lagrange- oder Integraldarstellung des Restes. Achtung: Eine glatte Funktion muss nicht durch ihre Taylor-Reihe dargestellt werden — e^{−1/x²} hat in 0 die Taylor-Reihe 0.
            """,
                fm("Taylor-Formel", "f(x) = Σ_{k=0}^n f⁽ᵏ⁾(x₀)(x − x₀)ᵏ/k! + Rₙ(x)"),
                fm("Lagrange-Restglied", "Rₙ = f⁽ⁿ⁺¹⁾(ξ)(x − x₀)ⁿ⁺¹/(n + 1)!"),
                fm("Integralrestglied", "Rₙ = (1/n!) ∫_{x₀}^x (x − t)ⁿ f⁽ⁿ⁺¹⁾(t) dt")
            ),
            sec("Standardreihen", """
                Die wichtigsten Entwicklungen um 0 mit Konvergenzbereich. In der Physik braucht man ständig die ersten zwei, drei Glieder (Kleinwinkelnäherung, nichtrelativistischer Grenzfall, Störungsrechnung).
            """,
                fm("Sinus, Kosinus", "sin x = x − x³/3! + x⁵/5! − …,   cos x = 1 − x²/2! + x⁴/4! − …  (x ∈ ℝ)"),
                fm("Logarithmus", "ln(1 + x) = x − x²/2 + x³/3 − …  (−1 < x ≤ 1)"),
                fm("Binomialreihe", "(1 + x)^α = Σ C(α,k) xᵏ = 1 + αx + α(α−1)x²/2 + …  (|x| < 1)"),
                fm("Arcustangens", "arctan x = x − x³/3 + x⁵/5 − …  (|x| ≤ 1)"),
                fm("Tangens", "tan x = x + x³/3 + 2x⁵/15 + …  (|x| < π/2)"),
                fm("Relativistische Energie", "γ = (1 − β²)^{−1/2} = 1 + β²/2 + 3β⁴/8 + …")
            ),
            sec("Landau-Symbole und Asymptotik", """
                f = O(g): |f| ≤ C|g| nahe dem Grenzpunkt; f = o(g): f/g → 0. f ~ g: f/g → 1. Damit rechnet man Grenzwerte elegant: (sin x − x)/x³ = (−x³/6 + o(x³))/x³ → −1/6.
            """,
                fm("Landau", "f = o(g) ⇔ f/g → 0,   f = O(g) ⇔ |f/g| beschränkt")
            ),
            sec("Funktionenfolgen und gleichmäßige Konvergenz", """
                Punktweise Konvergenz erhält Stetigkeit nicht (xⁿ auf [0, 1]). Gleichmäßige Konvergenz (sup |fₙ − f| → 0) schon. Vertauschungssätze:
                • Grenzwert und Integral: fₙ → f gleichmäßig auf [a, b] ⇒ ∫fₙ → ∫f.
                • Grenzwert und Ableitung: fₙ' konvergiert gleichmäßig und fₙ(x₀) konvergiert ⇒ (lim fₙ)' = lim fₙ'.
                • Weierstraß-M-Test: |fₙ(x)| ≤ Mₙ mit ΣMₙ < ∞ ⇒ Σfₙ konvergiert gleichmäßig.
                • Weierstraßscher Approximationssatz: Polynome liegen dicht in C[a, b].
            """,
                fm("Gleichmäßige Konvergenz", "‖fₙ − f‖_∞ = sup_x |fₙ(x) − f(x)| → 0")
            )
        ),
        CompendiumMath.ch(
            "ma_integral", 1, "Integralrechnung einer Variablen",
            "Riemann-Integral, Hauptsatz, Integrationstechniken, Stammfunktionentabelle, uneigentliche Integrale.",
            listOf("quadrature"),
            sec("Riemann-Integral und Hauptsatz", """
                f ist Riemann-integrierbar, wenn Ober- und Untersummen bei Verfeinerung zusammenlaufen. Stetige und monotone Funktionen sind integrierbar. Der Hauptsatz: F(x) = ∫ₐˣ f dt ist für stetiges f differenzierbar mit F' = f, und jede Stammfunktion liefert das bestimmte Integral.
            """,
                fm("Hauptsatz", "∫ₐᵇ f(x) dx = F(b) − F(a),   d/dx ∫ₐˣ f(t) dt = f(x)"),
                fm("Leibniz-Regel", "d/dx ∫_{a(x)}^{b(x)} f(t) dt = f(b)b' − f(a)a'"),
                fm("Mittelwertsatz der Integralrechnung", "∫ₐᵇ f g dx = f(ξ) ∫ₐᵇ g dx  (f stetig, g ≥ 0)")
            ),
            sec("Integrationstechniken", """
                • Partielle Integration: für Produkte, bei denen ein Faktor beim Ableiten einfacher wird (x eˣ, x sin x, ln x = 1 · ln x). Bei eˣ sin x zweimal anwenden und nach dem Integral auflösen.
                • Substitution: inneren Ausdruck ersetzen, dx = du/g'(x); Grenzen mittransformieren.
                • Partialbruchzerlegung für rationale Funktionen: Polynomdivision, Nenner faktorisieren, Ansatz A/(x − a) + (Bx + C)/(x² + px + q).
                • Trigonometrische Substitutionen: √(a² − x²) mit x = a sin t, √(a² + x²) mit x = a sinh t, √(x² − a²) mit x = a cosh t.
                • Rationale Funktionen in sin, cos: t = tan(x/2), dann sin x = 2t/(1 + t²), cos x = (1 − t²)/(1 + t²), dx = 2dt/(1 + t²).
            """,
                fm("Partielle Integration", "∫ u v' dx = uv − ∫ u' v dx"),
                fm("Substitution", "∫ₐᵇ f(g(x)) g'(x) dx = ∫_{g(a)}^{g(b)} f(u) du"),
                fm("Logarithmische Integration", "∫ f'(x)/f(x) dx = ln |f(x)| + C"),
                fm("Weierstraß-Substitution", "t = tan(x/2):  dx = 2 dt/(1 + t²)")
            ),
            sec("Stammfunktionen-Tabelle", """
                Die häufigsten Stammfunktionen (Konstante C weggelassen). Bei Physik-Integralen über Gauß-Funktionen und Potenzen hilft zusätzlich die Gamma-Funktion.
            """,
                fm("Potenzen", "∫ xⁿ dx = xⁿ⁺¹/(n + 1)  (n ≠ −1),   ∫ dx/x = ln |x|"),
                fm("Exponential", "∫ e^{ax} dx = e^{ax}/a,   ∫ x e^{ax} dx = e^{ax}(x/a − 1/a²)"),
                fm("Logarithmus", "∫ ln x dx = x ln x − x"),
                fm("Trigonometrisch", "∫ sin² x dx = x/2 − sin(2x)/4,   ∫ tan x dx = −ln |cos x|"),
                fm("Arcus-Typen", "∫ dx/(a² + x²) = (1/a) arctan(x/a),   ∫ dx/√(a² − x²) = arcsin(x/a)"),
                fm("Hyperbel-Typen", "∫ dx/√(x² + a²) = arsinh(x/a) = ln(x + √(x² + a²))"),
                fm("Exponentiell-trigonometrisch", "∫ e^{ax} sin(bx) dx = e^{ax}(a sin bx − b cos bx)/(a² + b²)")
            ),
            sec("Uneigentliche Integrale und Gamma-Funktion", """
                Unbeschränkte Intervalle oder Integranden behandelt man als Grenzwert. ∫₁^∞ x^{−s} dx konvergiert für s > 1, ∫₀¹ x^{−s} dx für s < 1. Majorantenkriterium wie bei Reihen. Das Dirichlet-Integral ∫₀^∞ sin x/x dx = π/2 konvergiert nur bedingt.
            """,
                fm("Gamma-Funktion", "Γ(s) = ∫₀^∞ t^{s−1} e^{−t} dt,   Γ(s + 1) = sΓ(s),   Γ(½) = √π"),
                fm("Gauß-Momente", "∫₀^∞ x^{2n} e^{−x²} dx = Γ(n + ½)/2"),
                fm("Dirichlet-Integral", "∫₀^∞ sin x/x dx = π/2"),
                fm("Beta-Funktion", "B(a, b) = ∫₀¹ t^{a−1}(1 − t)^{b−1} dt = Γ(a)Γ(b)/Γ(a + b)")
            )
        )
    )
}

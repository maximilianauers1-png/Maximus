package app.maximus.lab.domain

internal object MathAnalysis2 {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_topology", 2, "Metrische und normierte Räume",
            "Normen, offene und kompakte Mengen, Vollständigkeit, Banachscher Fixpunktsatz.",
            emptyList(),
            sec("Metriken und Normen", """
                Eine Metrik d misst Abstände (positiv definit, symmetrisch, Dreiecksungleichung), eine Norm Längen (zusätzlich homogen). Jede Norm induziert die Metrik d(x, y) = ‖x − y‖. Auf ℝⁿ sind alle Normen äquivalent — Konvergenz ist normunabhängig. In unendlichdimensionalen Räumen (Funktionenräume) gilt das nicht.
            """,
                fm("p-Normen", "‖x‖_p = (Σ|xᵢ|^p)^{1/p},   ‖x‖_∞ = maxᵢ |xᵢ|"),
                fm("Normäquivalenz in ℝⁿ", "‖x‖_∞ ≤ ‖x‖₂ ≤ ‖x‖₁ ≤ √n ‖x‖₂ ≤ n ‖x‖_∞"),
                fm("Operatornorm", "‖A‖ = sup_{‖x‖=1} ‖Ax‖,   ‖A‖₂ = größter Singulärwert")
            ),
            sec("Topologische Grundbegriffe", """
                U ist offen, wenn um jeden Punkt eine Kugel in U liegt; abgeschlossen, wenn das Komplement offen ist (äquivalent: enthält alle Grenzwerte konvergenter Folgen). Stetigkeit: Urbilder offener Mengen sind offen.

                Kompakt: jede offene Überdeckung hat eine endliche Teilüberdeckung, in metrischen Räumen gleichwertig: folgenkompakt. Heine-Borel: In ℝⁿ kompakt ⇔ abgeschlossen und beschränkt. Stetige Bilder kompakter Mengen sind kompakt — daher nehmen stetige Funktionen auf Kompakta Extrema an.

                Zusammenhängend: nicht in zwei disjunkte offene Teile zerlegbar; stetige Bilder zusammenhängender Mengen sind zusammenhängend (verallgemeinerter Zwischenwertsatz).
            """,
                fm("Heine-Borel", "K ⊂ ℝⁿ kompakt ⇔ K abgeschlossen und beschränkt")
            ),
            sec("Vollständigkeit und Fixpunkte", """
                Ein metrischer Raum ist vollständig, wenn jede Cauchy-Folge konvergiert; ein vollständiger normierter Raum heißt Banach-Raum. Banachscher Fixpunktsatz: Eine Kontraktion auf einem vollständigen Raum hat genau einen Fixpunkt, und die Iteration konvergiert geometrisch dagegen. Er liefert Picard-Lindelöf, den Satz über implizite Funktionen und Konvergenzbeweise numerischer Verfahren.
            """,
                fm("Kontraktion", "d(Φ(x), Φ(y)) ≤ q d(x, y),  q < 1"),
                fm("A-priori-Abschätzung", "d(xₙ, x*) ≤ qⁿ/(1 − q) · d(x₁, x₀)")
            )
        ),
        CompendiumMath.ch(
            "ma_multidiff", 2, "Differentialrechnung im ℝⁿ",
            "Partielle und totale Ableitung, Jacobi-Matrix, Kettenregel, Hesse-Matrix, Taylor und lokale Extrema.",
            emptyList(),
            sec("Totale Differenzierbarkeit", """
                f: ℝⁿ → ℝᵐ ist in x differenzierbar, wenn es eine lineare Abbildung Df(x) (die Jacobi-Matrix) gibt mit f(x + h) = f(x) + Df(x)h + o(‖h‖). Existenz aller partiellen Ableitungen reicht nicht (f = xy/(x² + y²) ist in 0 nicht einmal stetig). Hinreichend: partielle Ableitungen existieren und sind stetig (f ∈ C¹).

                Richtungsableitung ∂_v f = ∇f · v; der Gradient zeigt in Richtung des steilsten Anstiegs und steht senkrecht auf den Niveaumengen.
            """,
                fm("Jacobi-Matrix", "(Df)ᵢⱼ = ∂fᵢ/∂xⱼ"),
                fm("Kettenregel", "D(f ∘ g)(x) = Df(g(x)) · Dg(x)"),
                fm("Richtungsableitung", "∂_v f(x) = ∇f(x) · v"),
                fm("Totales Differential", "df = Σᵢ (∂f/∂xᵢ) dxᵢ")
            ),
            sec("Höhere Ableitungen und Taylor", """
                Satz von Schwarz: Für f ∈ C² vertauschen die partiellen Ableitungen, die Hesse-Matrix ist symmetrisch. Taylor im ℝⁿ bis zur zweiten Ordnung enthält Gradient und Hesse-Matrix.
            """,
                fm("Schwarz", "∂²f/∂xᵢ∂xⱼ = ∂²f/∂xⱼ∂xᵢ  (f ∈ C²)"),
                fm("Hesse-Matrix", "H_f = (∂²f/∂xᵢ∂xⱼ)ᵢⱼ"),
                fm("Taylor 2. Ordnung", "f(x + h) = f(x) + ∇f·h + ½ hᵀH_f h + o(‖h‖²)")
            ),
            sec("Lokale Extrema", """
                Notwendig: ∇f(x₀) = 0 (kritischer Punkt). Hinreichend: H_f(x₀) positiv definit ⇒ Minimum, negativ definit ⇒ Maximum, indefinit ⇒ Sattelpunkt; semidefinit ⇒ keine Aussage.

                Definitheit über Eigenwerte oder das Hurwitz-Kriterium (Hauptminoren). In 2D: det H > 0 und f_xx > 0 ⇒ Minimum; det H > 0 und f_xx < 0 ⇒ Maximum; det H < 0 ⇒ Sattel.
            """,
                fm("Hurwitz-Kriterium", "A positiv definit ⇔ alle führenden Hauptminoren > 0"),
                fm("2D-Test", "D = f_xx f_yy − f_xy²:  D > 0 Extremum, D < 0 Sattel")
            )
        ),
        CompendiumMath.ch(
            "ma_implicit", 2, "Umkehrsatz, implizite Funktionen, Lagrange",
            "Lokale Invertierbarkeit, implizit definierte Funktionen, Extrema unter Nebenbedingungen, Untermannigfaltigkeiten.",
            emptyList(),
            sec("Satz über die Umkehrfunktion", """
                Ist f ∈ C¹ und Df(x₀) invertierbar (det ≠ 0), so ist f nahe x₀ ein C¹-Diffeomorphismus, und D(f⁻¹)(f(x₀)) = Df(x₀)⁻¹. Lokal, nicht global: Polarkoordinaten sind überall lokal umkehrbar (außer r = 0), aber nicht injektiv.
            """,
                fm("Ableitung der Umkehrung", "D(f⁻¹)(y₀) = [Df(x₀)]⁻¹")
            ),
            sec("Satz über implizite Funktionen", """
                Ist F(x, y) = 0 mit F ∈ C¹ und ∂F/∂y in (x₀, y₀) invertierbar, so lässt sich die Gleichung nahe (x₀, y₀) eindeutig als y = g(x) auflösen. Die Ableitung folgt durch implizites Differenzieren. In der Thermodynamik ist das die Herkunft der Kettenregel-Identitäten wie (∂x/∂y)_z (∂y/∂z)_x (∂z/∂x)_y = −1.
            """,
                fm("Implizite Ableitung", "g'(x) = −(∂F/∂y)⁻¹ ∂F/∂x"),
                fm("Zyklische Relation", "(∂x/∂y)_z (∂y/∂z)_x (∂z/∂x)_y = −1")
            ),
            sec("Extrema unter Nebenbedingungen", """
                Gesucht: Extrema von f auf M = {g = 0}. Notwendig ist, dass ∇f in einem kritischen Punkt eine Linearkombination der Gradienten der Nebenbedingungen ist (∇g regulär vorausgesetzt). Geometrisch: Die Niveaufläche von f berührt M.

                Physik: Lagrange-Multiplikatoren sind Zwangskräfte (Lagrange-Gleichungen 1. Art), in der Statistik das chemische Potential und β = 1/k_BT bei der Maximierung der Entropie.
            """,
                fm("Lagrange-Bedingung", "∇f(x) = Σₖ λₖ ∇gₖ(x),   gₖ(x) = 0"),
                fm("Lagrange-Funktion", "L(x, λ) = f(x) − Σ λₖ gₖ(x),   ∇_{x,λ} L = 0")
            ),
            sec("Untermannigfaltigkeiten", """
                M ⊂ ℝⁿ ist eine k-dimensionale Untermannigfaltigkeit, wenn sie lokal Nullstellenmenge von n − k Funktionen mit linear unabhängigen Gradienten ist (oder lokal Graph, oder lokal parametrisierbar). Der Tangentialraum ist der Kern der Jacobi-Matrix der Nebenbedingungen, der Normalenraum wird von den Gradienten aufgespannt. Beispiele: Sphäre Sⁿ⁻¹, Konfigurationsräume mechanischer Systeme mit holonomen Zwangsbedingungen.
            """,
                fm("Tangentialraum", "T_xM = ker Dg(x),   N_xM = span{∇g₁, …, ∇g_{n−k}}")
            )
        ),
        CompendiumMath.ch(
            "ma_curves", 2, "Kurven, Kurvenintegrale und Potentiale",
            "Bogenlänge, Krümmung, Frenet, Kurvenintegrale, Gradientenfelder und das Poincaré-Lemma.",
            emptyList(),
            sec("Kurven", """
                Eine reguläre Kurve γ: [a, b] → ℝⁿ hat γ' ≠ 0. Bogenlänge als Integral über |γ'|; nach Bogenlänge parametrisiert ist |γ'| = 1. Im ℝ³: Frenet-Dreibein aus Tangente T, Normale N und Binormale B mit Krümmung κ und Torsion τ.
            """,
                fm("Bogenlänge", "L = ∫ₐᵇ |γ'(t)| dt"),
                fm("Krümmung", "κ = |γ' × γ''|/|γ'|³"),
                fm("Frenet-Gleichungen", "T' = κN,   N' = −κT + τB,   B' = −τN")
            ),
            sec("Kurvenintegrale", """
                Erster Art (skalar): ∫_γ f ds = ∫ f(γ(t))|γ'(t)| dt (Masse eines Drahtes). Zweiter Art (Vektorfeld): ∫_γ F · dx = ∫ F(γ(t)) · γ'(t) dt (Arbeit). Das zweite hängt von der Orientierung ab, nicht von der Parametrisierung.
            """,
                fm("Kurvenintegral 1. Art", "∫_γ f ds = ∫ₐᵇ f(γ(t)) |γ'(t)| dt"),
                fm("Kurvenintegral 2. Art", "∫_γ F · dx = ∫ₐᵇ F(γ(t)) · γ'(t) dt")
            ),
            sec("Gradientenfelder und Wegunabhängigkeit", """
                F = ∇φ (konservativ) ⇔ Kurvenintegrale sind wegunabhängig ⇔ Integrale über geschlossene Kurven verschwinden. Dann ist ∫_γ F · dx = φ(Ende) − φ(Anfang). Notwendig: Integrabilitätsbedingung ∂ᵢFⱼ = ∂ⱼFᵢ (im ℝ³: rot F = 0). Hinreichend ist sie auf einfach zusammenhängenden (z. B. sternförmigen) Gebieten — Poincaré-Lemma. Gegenbeispiel: das Wirbelfeld (−y, x)/(x² + y²) auf ℝ² ∖ {0} ist rotationsfrei, aber nicht konservativ (Umlaufintegral 2π, Aharonov-Bohm!).
            """,
                fm("Hauptsatz für Kurvenintegrale", "∫_γ ∇φ · dx = φ(γ(b)) − φ(γ(a))"),
                fm("Integrabilitätsbedingung", "∂Fᵢ/∂xⱼ = ∂Fⱼ/∂xᵢ  (rot F = 0)"),
                fm("Potential (sternförmig)", "φ(x) = ∫₀¹ F(tx) · x dt")
            )
        )
    )
}

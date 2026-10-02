package app.maximus.lab.domain

internal object MathAnalysis3 {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_measure", 3, "Maßtheorie",
            "σ-Algebren, Maße, Borel- und Lebesgue-Maß, Nullmengen, messbare Funktionen.",
            emptyList(),
            sec("σ-Algebren und Maße", """
                Nicht jeder Teilmenge des ℝⁿ lässt sich sinnvoll ein Volumen zuordnen (Vitali-Menge, Banach-Tarski). Man beschränkt sich auf eine σ-Algebra 𝒜: enthält ∅, ist abgeschlossen unter Komplement und abzählbaren Vereinigungen. Ein Maß μ: 𝒜 → [0, ∞] ist σ-additiv.

                Die Borel-σ-Algebra wird von den offenen Mengen erzeugt. Das Lebesgue-Maß λⁿ ist das eindeutige translationsinvariante Maß auf ihr mit λⁿ([0, 1]ⁿ) = 1. Die Vervollständigung (Teilmengen von Nullmengen messbar machen) liefert die Lebesgue-σ-Algebra.
            """,
                fm("σ-Additivität", "μ(⋃ₖ Aₖ) = Σₖ μ(Aₖ)  (Aₖ paarweise disjunkt)"),
                fm("Stetigkeit von unten", "A₁ ⊂ A₂ ⊂ …  ⇒  μ(⋃Aₖ) = lim μ(Aₖ)"),
                fm("Lineare Abbildungen", "λⁿ(T(A)) = |det T| λⁿ(A)")
            ),
            sec("Konstruktion nach Carathéodory", """
                Man startet mit einem Prämaß auf einem Ring (Volumen von Quadern), bildet das äußere Maß μ*(A) = inf Σ μ(Qₖ) über abzählbare Überdeckungen und nennt A messbar, wenn μ*(E) = μ*(E ∩ A) + μ*(E ∖ A) für alle E. Die messbaren Mengen bilden eine σ-Algebra, auf der μ* ein Maß ist (Fortsetzungssatz).
            """,
                fm("Äußeres Maß", "μ*(A) = inf { Σ vol(Qₖ) : A ⊂ ⋃Qₖ }")
            ),
            sec("Nullmengen und „fast überall“", """
                Abzählbare Mengen (z. B. ℚ) haben Lebesgue-Maß 0; die Cantor-Menge ist überabzählbar und trotzdem eine Nullmenge. Eine Eigenschaft gilt fast überall (f. ü.), wenn sie außerhalb einer Nullmenge gilt. Für das Lebesgue-Integral sind Funktionen, die f. ü. übereinstimmen, gleich.

                f ist messbar, wenn Urbilder von Borel-Mengen messbar sind. Stetige Funktionen, Grenzwerte messbarer Funktionen, sup und inf abzählbar vieler sind messbar.
            """,
                fm("Messbarkeit", "f⁻¹((a, ∞)) ∈ 𝒜  ∀a ∈ ℝ")
            )
        ),
        CompendiumMath.ch(
            "ma_lebesgue", 3, "Lebesgue-Integral und Konvergenzsätze",
            "Definition, monotone und majorisierte Konvergenz, Fatou, Parameterintegrale, Lᵖ-Räume.",
            emptyList(),
            sec("Konstruktion", """
                Für einfache Funktionen Σcᵢ𝟙_{Aᵢ} ist ∫ = Σcᵢ μ(Aᵢ). Für f ≥ 0 messbar: Supremum über alle einfachen Funktionen darunter. Allgemein f = f⁺ − f⁻, integrierbar wenn ∫|f| < ∞. Statt den Definitionsbereich (Riemann) wird der Wertebereich zerlegt.

                Vergleich: Jede eigentlich Riemann-integrierbare Funktion ist Lebesgue-integrierbar mit gleichem Wert. Lebesgue-Kriterium: f beschränkt ist Riemann-integrierbar ⇔ Menge der Unstetigkeitsstellen ist Nullmenge. Die Dirichlet-Funktion 𝟙_ℚ ist nicht Riemann-, aber Lebesgue-integrierbar (Integral 0).
            """,
                fm("Einfache Funktionen", "∫ Σ cᵢ 𝟙_{Aᵢ} dμ = Σ cᵢ μ(Aᵢ)"),
                fm("Integrierbarkeit", "f ∈ ℒ¹ ⇔ ∫ |f| dμ < ∞")
            ),
            sec("Die drei Konvergenzsätze", """
                Sie sind der Grund, warum Physiker Grenzwerte und Integrale (meist) bedenkenlos vertauschen dürfen:
                • Monotone Konvergenz (Beppo Levi): 0 ≤ f₁ ≤ f₂ ≤ … ⇒ ∫ lim fₙ = lim ∫ fₙ.
                • Lemma von Fatou: fₙ ≥ 0 ⇒ ∫ lim inf fₙ ≤ lim inf ∫ fₙ.
                • Majorisierte Konvergenz (Lebesgue): fₙ → f f. ü. und |fₙ| ≤ g mit g ∈ ℒ¹ ⇒ ∫fₙ → ∫f.

                Gegenbeispiel ohne Majorante: fₙ = n 𝟙_{(0, 1/n)} → 0 punktweise, aber ∫fₙ = 1 (die Masse entwischt).
            """,
                fm("Monotone Konvergenz", "0 ≤ fₙ ↑ f  ⇒  ∫ fₙ dμ ↑ ∫ f dμ"),
                fm("Fatou", "∫ lim inf fₙ ≤ lim inf ∫ fₙ  (fₙ ≥ 0)"),
                fm("Majorisierte Konvergenz", "fₙ → f, |fₙ| ≤ g ∈ ℒ¹  ⇒  ∫ fₙ → ∫ f")
            ),
            sec("Parameterintegrale", """
                F(t) = ∫ f(x, t) dx ist stetig in t, wenn f stetig in t ist und eine integrierbare Majorante unabhängig von t existiert. Differenzierbar mit F'(t) = ∫ ∂ₜf dx, wenn ∂ₜf existiert und eine Majorante hat (Differentiation unter dem Integral, „Feynman-Trick“). Beispiel: ∫₀^∞ e^{−tx} sin x/x dx nach t ableiten liefert arctan und das Dirichlet-Integral.
            """,
                fm("Differentiation unter dem Integral", "d/dt ∫ f(x, t) dx = ∫ ∂f/∂t (x, t) dx  (bei Majorante für ∂ₜf)")
            ),
            sec("Lᵖ-Räume", """
                Lᵖ ist der Raum der (Klassen f. ü. gleicher) messbaren Funktionen mit ∫|f|ᵖ < ∞. Hölder und Minkowski machen ‖·‖_p zur Norm; nach Riesz-Fischer ist Lᵖ vollständig (Banach-Raum), L² mit ⟨f, g⟩ = ∫ f̄g ein Hilbert-Raum — der Zustandsraum der Wellenmechanik. Auf endlichem Maß gilt Lᵖ ⊂ Lᵠ für p > q; auf ℝ nicht.
            """,
                fm("Hölder", "‖fg‖₁ ≤ ‖f‖_p ‖g‖_q,   1/p + 1/q = 1"),
                fm("Minkowski", "‖f + g‖_p ≤ ‖f‖_p + ‖g‖_p"),
                fm("Lᵖ-Norm", "‖f‖_p = (∫ |f|ᵖ dμ)^{1/p}")
            )
        ),
        CompendiumMath.ch(
            "ma_fubini", 3, "Fubini, Transformationssatz und Volumina",
            "Mehrfachintegrale, Vertauschung der Integrationsreihenfolge, Koordinatenwechsel, Gauß-Integral, n-Kugel.",
            listOf("quadrature"),
            sec("Fubini und Tonelli", """
                Tonelli: Für messbare f ≥ 0 auf X × Y darf man iteriert in beliebiger Reihenfolge integrieren (Wert eventuell ∞). Fubini: Für f ∈ ℒ¹(X × Y) ebenso, mit endlichem Wert. Die Integrierbarkeit prüft man mit Tonelli an |f|.

                Gegenbeispiel: f = (x² − y²)/(x² + y²)² auf [0, 1]² liefert iteriert ±π/4 — sie ist nicht integrierbar.
            """,
                fm("Fubini", "∫_{X×Y} f d(μ⊗ν) = ∫_X (∫_Y f(x, y) dν(y)) dμ(x) = ∫_Y (∫_X f dμ) dν"),
                fm("Cavalieri", "λⁿ⁺¹(A) = ∫ λⁿ(A_t) dt")
            ),
            sec("Transformationssatz", """
                Für einen Diffeomorphismus Φ: U → V gilt ∫_V f dy = ∫_U f(Φ(x)) |det DΦ(x)| dx. Die Jacobi-Determinante ist der lokale Volumenverzerrungsfaktor. Die drei Klassiker der Physik:
                • Polarkoordinaten: dx dy = r dr dφ.
                • Zylinderkoordinaten: dV = r dr dφ dz.
                • Kugelkoordinaten: dV = r² sin θ dr dθ dφ.
            """,
                fm("Transformationssatz", "∫_{Φ(U)} f(y) dy = ∫_U f(Φ(x)) |det DΦ(x)| dx"),
                fm("Polarkoordinaten", "dx dy = r dr dφ"),
                fm("Kugelkoordinaten", "dV = r² sin θ dr dθ dφ,   dΩ = sin θ dθ dφ"),
                fm("Zylinderkoordinaten", "dV = r dr dφ dz")
            ),
            sec("Gauß-Integral und n-dimensionale Volumina", """
                Der Klassiker: I² = ∫∫ e^{−(x²+y²)} dx dy = ∫₀^{2π}∫₀^∞ e^{−r²} r dr dφ = π. Daraus folgen alle Gauß-Integrale und das Volumen der n-Kugel, das für große n gegen 0 geht (fast das ganze Volumen eines hochdimensionalen Würfels liegt in den Ecken — wichtig für die statistische Physik).
            """,
                fm("Gauß-Integral", "∫_{−∞}^{∞} e^{−ax²} dx = √(π/a)"),
                fm("Mehrdimensional", "∫_{ℝⁿ} e^{−xᵀAx} dⁿx = πⁿᐟ²/√det A"),
                fm("Mit linearem Term", "∫ e^{−ax² + bx} dx = √(π/a) e^{b²/4a}"),
                fm("Volumen der n-Kugel", "Vₙ(r) = πⁿᐟ² rⁿ/Γ(n/2 + 1)"),
                fm("Oberfläche der Einheitssphäre", "|Sⁿ⁻¹| = 2πⁿᐟ²/Γ(n/2)")
            )
        ),
        CompendiumMath.ch(
            "ma_vector", 2, "Vektoranalysis und Integralsätze",
            "grad, div, rot, krummlinige Koordinaten, Flächenintegrale, Gauß, Stokes, Green, Helmholtz.",
            emptyList(),
            sec("Differentialoperatoren", """
                Divergenz misst die Quelldichte, Rotation die Wirbeldichte. Identitäten folgen aus dem Satz von Schwarz und dem ε-Tensor: εᵢⱼₖεᵢₗₘ = δⱼₗδₖₘ − δⱼₘδₖₗ.
            """,
                fm("Divergenz", "∇ · F = ∂ᵢFᵢ"),
                fm("Rotation", "(∇ × F)ᵢ = εᵢⱼₖ ∂ⱼFₖ"),
                fm("Identitäten", "∇ × ∇φ = 0,   ∇ · (∇ × F) = 0,   ∇ × (∇ × F) = ∇(∇·F) − ΔF"),
                fm("Produktregeln", "∇·(φF) = ∇φ·F + φ∇·F,   ∇×(φF) = ∇φ×F + φ∇×F"),
                fm("ε-δ-Identität", "εᵢⱼₖεᵢₗₘ = δⱼₗδₖₘ − δⱼₘδₖₗ")
            ),
            sec("Krummlinige Koordinaten", """
                In orthogonalen Koordinaten mit Skalenfaktoren hᵢ (|∂r/∂qᵢ|) gelten allgemeine Formeln. Kugel: h = (1, r, r sin θ); Zylinder: h = (1, r, 1).
            """,
                fm("Gradient (Kugel)", "∇f = ∂_r f e_r + (1/r)∂_θ f e_θ + (1/(r sin θ))∂_φ f e_φ"),
                fm("Divergenz (Kugel)", "∇·F = (1/r²)∂_r(r²F_r) + (1/(r sin θ))∂_θ(sin θ F_θ) + (1/(r sin θ))∂_φF_φ"),
                fm("Laplace (Zylinder)", "Δf = (1/r)∂_r(r ∂_r f) + (1/r²)∂_φ²f + ∂_z²f"),
                fm("Laplace (Kugel)", "Δf = (1/r²)∂_r(r²∂_r f) + (1/(r² sin θ))∂_θ(sin θ ∂_θ f) + (1/(r² sin²θ))∂_φ²f")
            ),
            sec("Flächenintegrale", """
                Für eine parametrisierte Fläche Φ(u, v) ist das Flächenelement |Φ_u × Φ_v| du dv bzw. allgemein √det(DΦᵀDΦ) (Gramsche Determinante). Der Fluss eines Vektorfeldes ist ∫ F · n dA. Kugeloberfläche: dA = R² sin θ dθ dφ, n = e_r.
            """,
                fm("Flächenelement", "dA = |∂_uΦ × ∂_vΦ| du dv = √det(DΦᵀDΦ) du dv"),
                fm("Fluss", "∫_A F · dA = ∫ F(Φ) · (∂_uΦ × ∂_vΦ) du dv")
            ),
            sec("Integralsätze", """
                • Gauß: Fluss durch die geschlossene Oberfläche = Volumenintegral der Divergenz.
                • Stokes: Zirkulation längs des Randes = Fluss der Rotation durch die Fläche.
                • Green (Ebene): Spezialfall von Stokes; Flächeninhalt A = ½∮(x dy − y dx).
                • Greensche Identitäten: Grundlage der Green-Funktionen und der Eindeutigkeit von Randwertproblemen.
                • Helmholtz: Ein im Unendlichen hinreichend schnell abfallendes Feld ist durch div und rot eindeutig bestimmt; F = −∇φ + ∇ × A.

                Physik: Gaußsches Gesetz, Ampère-Gesetz, Kontinuitätsgleichungen — integrale und differentielle Form sind durch genau diese Sätze äquivalent.
            """,
                fm("Gauß", "∫_V ∇·F dV = ∮_{∂V} F · dA"),
                fm("Stokes", "∫_A (∇×F) · dA = ∮_{∂A} F · dℓ"),
                fm("Green (Ebene)", "∫_A (∂ₓQ − ∂ᵧP) dx dy = ∮_{∂A} (P dx + Q dy)"),
                fm("1. Greensche Identität", "∫_V (φΔψ + ∇φ·∇ψ) dV = ∮_{∂V} φ ∂ₙψ dA"),
                fm("2. Greensche Identität", "∫_V (φΔψ − ψΔφ) dV = ∮_{∂V} (φ∂ₙψ − ψ∂ₙφ) dA"),
                fm("Helmholtz-Zerlegung", "F = −∇φ + ∇ × A")
            )
        ),
        CompendiumMath.ch(
            "ma_forms", 3, "Differentialformen und Mannigfaltigkeiten",
            "Mannigfaltigkeiten, äußeres Produkt, äußere Ableitung, Pullback, allgemeiner Satz von Stokes, Hodge-Stern.",
            emptyList(),
            sec("Mannigfaltigkeiten", """
                Eine glatte n-Mannigfaltigkeit ist lokal diffeomorph zum ℝⁿ (Karten mit glatten Kartenwechseln). Tangentialvektoren sind Derivationen (Richtungsableitungen) ∂/∂xⁱ; der Kotangentialraum hat die Basis dxⁱ. Untermannigfaltigkeiten des ℝⁿ sind die konkreten Beispiele; die Raumzeit der ART ist eine abstrakte 4-Mannigfaltigkeit.
            """,
                fm("Basis-Dualität", "dxⁱ(∂/∂xʲ) = δⁱⱼ")
            ),
            sec("Formen und äußere Ableitung", """
                Eine k-Form ist ein glattes Feld alternierender k-Linearformen. Das Dachprodukt ist antisymmetrisch für 1-Formen. Die äußere Ableitung d ist linear, erfüllt die Leibniz-Regel mit Vorzeichen und d² = 0. Im ℝ³ übersetzen sich d auf 0-, 1-, 2-Formen in grad, rot, div — und d² = 0 in rot grad = 0 und div rot = 0.

                Geschlossen: dω = 0; exakt: ω = dη. Exakt ⇒ geschlossen; Poincaré-Lemma: auf sternförmigen Gebieten auch umgekehrt. Die Abweichung misst die de-Rham-Kohomologie (Topologie!).
            """,
                fm("Dachprodukt", "α ∧ β = (−1)^{kl} β ∧ α  (α ∈ Ωᵏ, β ∈ Ωˡ)"),
                fm("Äußere Ableitung", "d(f dx^{i₁}∧…∧dx^{iₖ}) = Σⱼ ∂ⱼf dxʲ ∧ dx^{i₁}∧…∧dx^{iₖ}"),
                fm("Nilpotenz", "d ∘ d = 0"),
                fm("Leibniz", "d(α ∧ β) = dα ∧ β + (−1)ᵏ α ∧ dβ"),
                fm("Pullback", "Φ*(dω) = d(Φ*ω),   ∫_{Φ(M)} ω = ∫_M Φ*ω")
            ),
            sec("Allgemeiner Satz von Stokes", """
                Für eine kompakte orientierte n-Mannigfaltigkeit M mit Rand ∂M und eine (n−1)-Form ω gilt ∫_M dω = ∫_{∂M} ω. Hauptsatz der Analysis, Gauß, Stokes und Green sind Spezialfälle.

                Hodge-Stern (mit Metrik und Orientierung) bildet k-Formen auf (n−k)-Formen ab; damit Laplace Δ = dδ + δd. Elektrodynamik in Formen: Mit dem Feldstärke-2-Form F = dA gilt dF = 0 (homogene Gleichungen, automatisch) und d⋆F = ⋆J (inhomogene).
            """,
                fm("Stokes (allgemein)", "∫_M dω = ∮_{∂M} ω"),
                fm("Maxwell in Formen", "F = dA,   dF = 0,   d⋆F = μ₀ ⋆J")
            )
        )
    )
}

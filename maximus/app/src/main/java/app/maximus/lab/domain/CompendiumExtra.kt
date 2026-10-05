package app.maximus.lab.domain

/** Additional lessons for the existing topics. */
internal object CompendiumExtra {
    val chapters: List<Chapter> = listOf(
        chapter(
            "th_noneq", Topic.THERMO, 3, "Nichtgleichgewicht und Fluktuationen",
            "Lineare Antworttheorie, Onsager-Relationen, Entropieproduktion, Fluktuations-Dissipations-Theorem, Boltzmann-Gleichung.",
            emptyList(),
            sec("Flüsse und Kräfte", """
                Nahe dem Gleichgewicht sind Flüsse J (Wärme, Teilchen, Ladung) linear in den thermodynamischen Kräften X (Gradienten von 1/T, μ/T, Potential). Die Entropieproduktion ist positiv definit. Onsager zeigte aus der Zeitumkehrinvarianz der Mikrodynamik, dass die Kopplungsmatrix symmetrisch ist — daher hängen z. B. Seebeck- und Peltier-Koeffizient zusammen (Π = S T).
            """,
                fm("Lineare Antwort", "Jᵢ = Σⱼ Lᵢⱼ Xⱼ"),
                fm("Onsager", "Lᵢⱼ = Lⱼᵢ  (bei Magnetfeld: Lᵢⱼ(B) = Lⱼᵢ(−B))"),
                fm("Entropieproduktion", "σ = Σᵢ Jᵢ Xᵢ ≥ 0"),
                fm("Kelvin-Relation", "Π = S T")
            ),
            sec("Fluktuation und Dissipation", """
                Dieselben mikroskopischen Prozesse, die Reibung verursachen, erzeugen auch Fluktuationen. Das Fluktuations-Dissipations-Theorem verknüpft die Antwortfunktion mit dem Spektrum der Gleichgewichtsfluktuationen: Einstein-Relation bei der Brownschen Bewegung, Johnson-Nyquist-Rauschen beim Widerstand.
            """,
                fm("Einstein-Relation", "D = μ k_BT = k_BT/γ"),
                fm("FDT (klassisch)", "S_x(ω) = (2k_BT/ω) Im χ(ω)"),
                fm("Langevin", "m ẍ = −γẋ + F(t),  ⟨F(t)F(t′)⟩ = 2γk_BT δ(t − t′)")
            ),
            sec("Kinetische Theorie", """
                Die Boltzmann-Gleichung beschreibt die Einteilchenverteilung f(r, p, t) unter Strömung und Stößen. In Relaxationszeitnäherung erhält man Transportkoeffizienten (Leitfähigkeit, Wärmeleitung, Viskosität). Das H-Theorem zeigt, wie aus reversiblen Stößen mit der Annahme molekularen Chaos irreversibles Verhalten folgt.
            """,
                fm("Boltzmann-Gleichung", "∂f/∂t + v·∇_r f + F·∇_p f = (∂f/∂t)_Stoß"),
                fm("Relaxationszeitnäherung", "(∂f/∂t)_Stoß = −(f − f₀)/τ"),
                fm("Wiedemann-Franz", "κ/(σT) = (π²/3)(k_B/e)²")
            )
        ),
        chapter(
            "el_optics", Topic.ELECTRO, 2, "Optik",
            "Brechung, Fresnel-Formeln, Interferenz, Beugung, Auflösungsgrenze, Polarisation, Kohärenz.",
            emptyList(),
            sec("Brechung und Reflexion", """
                An Grenzflächen gilt das Snelliussche Gesetz; oberhalb des Grenzwinkels tritt Totalreflexion auf (Glasfasern). Die Fresnel-Formeln geben die reflektierten Amplituden für s- und p-Polarisation; beim Brewster-Winkel wird p-polarisiertes Licht nicht reflektiert.
            """,
                fm("Snellius", "n₁ sin θ₁ = n₂ sin θ₂"),
                fm("Grenzwinkel", "sin θ_c = n₂/n₁"),
                fm("Brewster-Winkel", "tan θ_B = n₂/n₁"),
                fm("Reflexion (senkrecht)", "R = ((n₁ − n₂)/(n₁ + n₂))²")
            ),
            sec("Interferenz und Beugung", """
                Kohärente Wellen überlagern sich mit festen Phasen; Intensitätsmaxima entstehen bei Gangunterschieden von ganzzahligen Wellenlängen. Beugung am Spalt und an Öffnungen begrenzt die Auflösung jedes optischen Instruments (Rayleigh-Kriterium) — auch der Lithografie und des Mikroskops (Abbe).
            """,
                fm("Doppelspalt (Maxima)", "d sin θ = m λ"),
                fm("Einzelspalt (Minima)", "a sin θ = m λ,  m ≠ 0"),
                fm("Rayleigh (Kreisblende)", "θ_min = 1,22 λ/D"),
                fm("Abbe-Grenze", "d_min = λ/(2 NA)")
            ),
            sec("Polarisation und Kohärenz", """
                Polarisatoren lassen nur eine Feldrichtung durch (Malus-Gesetz). Doppelbrechende Kristalle und Verzögerungsplatten wandeln lineare in zirkulare Polarisation um. Die Kohärenzlänge bestimmt, über welche Gangunterschiede Interferenz sichtbar bleibt: lang bei Lasern, kurz bei Glühlicht.
            """,
                fm("Malus", "I = I₀ cos² θ"),
                fm("Kohärenzlänge", "ℓ_c ≈ λ²/Δλ = c/Δν")
            )
        ),
        chapter(
            "qm_scattering", Topic.QUANTUM, 3, "Streutheorie",
            "Wirkungsquerschnitt, Bornsche Näherung, Partialwellen und Streuphasen, Resonanzen, Fermis Goldene Regel.",
            emptyList(),
            sec("Wirkungsquerschnitt", """
                Streuexperimente messen, wie viele Teilchen pro Raumwinkel abgelenkt werden. Der differentielle Wirkungsquerschnitt ist das Betragsquadrat der Streuamplitude f(θ) der auslaufenden Kugelwelle. Integriert ergibt sich der totale Querschnitt; das optische Theorem verknüpft ihn mit dem Imaginärteil der Vorwärtsamplitude (Wahrscheinlichkeitserhaltung).
            """,
                fm("Asymptotik", "ψ → e^{ikz} + f(θ, φ) e^{ikr}/r"),
                fm("Differentieller Querschnitt", "dσ/dΩ = |f(θ, φ)|²"),
                fm("Optisches Theorem", "σ_tot = (4π/k) Im f(0)")
            ),
            sec("Bornsche Näherung", """
                Für schwache Potentiale ist die Streuamplitude die Fourier-Transformierte des Potentials beim Impulsübertrag q. Für das abgeschirmte Coulomb-Potential (Yukawa) erhält man im Grenzfall die Rutherford-Formel — zufällig exakt auch quantenmechanisch.
            """,
                fm("Erste Bornsche Näherung", "f(q) = −(m/(2πħ²)) ∫ V(r) e^{−iq·r} d³r"),
                fm("Impulsübertrag", "q = 2k sin(θ/2)"),
                fm("Rutherford", "dσ/dΩ = (Z₁Z₂e²/(16πε₀E))² / sin⁴(θ/2)")
            ),
            sec("Partialwellen und Resonanzen", """
                Für Zentralpotentiale zerlegt man nach Drehimpuls l; jede Partialwelle erhält eine Phasenverschiebung δ_l. Bei niedriger Energie dominiert die s-Welle (Streulänge a). Durchläuft δ_l schnell π/2, entsteht eine Resonanz mit Breit-Wigner-Form — so findet man kurzlebige Teilchen.
            """,
                fm("Partialwellen", "σ = (4π/k²) Σ_l (2l + 1) sin² δ_l"),
                fm("Breit-Wigner", "σ(E) ∝ (Γ/2)²/((E − E_R)² + (Γ/2)²)"),
                fm("Fermis Goldene Regel", "Γ_{i→f} = (2π/ħ) |⟨f|V|i⟩|² ρ(E_f)")
            )
        ),
        chapter(
            "sc_hetero", Topic.SEMICONDUCTOR, 3, "Heterostrukturen und Quanten-Hall-Effekt",
            "Bandkantenanpassung, Quantentöpfe, 2DEG, Modulationsdotierung, HEMT, Landau-Niveaus, QHE.",
            emptyList(),
            sec("Heterostrukturen", """
                Grenzflächen verschiedener Halbleiter (z. B. GaAs/AlGaAs) bilden Bandkantensprünge. Ein dünner Film mit kleinerer Lücke zwischen zwei größeren ist ein Quantentopf mit diskreten Subbändern — Grundlage von Halbleiterlasern und LEDs. Modulationsdotierung trennt Dotieratome räumlich von den Elektronen: Es entsteht ein zweidimensionales Elektronengas (2DEG) mit extrem hoher Beweglichkeit (HEMT-Transistoren in Mobilfunk-Verstärkern).
            """,
                fm("Subbänder (unendlicher Topf)", "E_n = ħ²π²n²/(2m*L²)"),
                fm("2D-Zustandsdichte", "g_2D = m*/(πħ²)  (mit Spin, konstant)")
            ),
            sec("Landau-Niveaus und Quanten-Hall-Effekt", """
                Im starken Magnetfeld kondensieren die Zustände eines 2DEG zu Landau-Niveaus mit großer Entartung. Liegt die Fermi-Energie zwischen zwei Niveaus, verschwindet der Längswiderstand, und der Hall-Widerstand ist auf h/(νe²) quantisiert — so genau, dass er heute das Ohm definiert. Topologisch: Die Hall-Leitfähigkeit ist eine Chern-Zahl.
            """,
                fm("Landau-Niveaus", "E_n = ħω_c (n + ½),  ω_c = eB/m*"),
                fm("Entartung pro Fläche", "n_B = eB/h"),
                fm("Quanten-Hall", "R_xy = h/(ν e²),  R_xx = 0")
            )
        ),
        chapter(
            "me_chaos", Topic.MECHANICS, 3, "Nichtlineare Dynamik und Chaos",
            "Fixpunkte und Stabilität, Bifurkationen, logistische Abbildung, Lyapunov-Exponenten, seltsame Attraktoren, KAM.",
            listOf("lorenz", "pendulum"),
            sec("Stabilität und Bifurkationen", """
                Fixpunkte einer DGL ẋ = f(x) werden durch die Eigenwerte der Jacobi-Matrix klassifiziert (Knoten, Sattel, Strudel, Zentrum). Ändert ein Parameter die Stabilität, entsteht eine Bifurkation: Sattel-Knoten, Pitchfork (Symmetriebrechung), Hopf (Entstehung eines Grenzzyklus).
            """,
                fm("Linearisierung", "δẋ = J(x*) δx,  J = ∂f/∂x"),
                fm("Hopf-Bifurkation", "Re λ wechselt Vorzeichen, Im λ ≠ 0")
            ),
            sec("Chaos", """
                Deterministische Systeme können empfindlich von Anfangsbedingungen abhängen: Abstände wachsen exponentiell mit dem größten Lyapunov-Exponenten. Die logistische Abbildung zeigt den Weg ins Chaos über Periodenverdopplungen mit der universellen Feigenbaum-Konstante. In dissipativen Systemen entstehen seltsame Attraktoren mit fraktaler Dimension (Lorenz).
            """,
                fm("Lyapunov-Exponent", "|δx(t)| ≈ |δx(0)| e^{λt}"),
                fm("Logistische Abbildung", "x_{n+1} = r xₙ (1 − xₙ)"),
                fm("Feigenbaum-Konstante", "δ = 4,6692…")
            ),
            sec("Hamiltonsches Chaos", """
                Integrable Hamilton-Systeme bewegen sich auf invarianten Tori. Das KAM-Theorem zeigt: Bei kleinen Störungen überleben die meisten Tori (mit hinreichend irrationalen Frequenzverhältnissen); resonante werden zerstört, dazwischen entsteht chaotische Bewegung. Poincaré-Schnitte machen das sichtbar.
            """,
                fm("KAM-Bedingung", "|k·ω| ≥ γ/|k|^τ  für alle k ≠ 0")
            )
        ),
        chapter(
            "ma_groups", Topic.MATH, 3, "Gruppentheorie für Physiker",
            "Gruppen, Darstellungen, Charaktere, Lie-Gruppen und Lie-Algebren, SU(2), Auswahlregeln, Wigner-Eckart.",
            emptyList(),
            sec("Gruppen und Darstellungen", """
                Symmetrien bilden Gruppen. Eine Darstellung ordnet jedem Gruppenelement eine Matrix zu, verträglich mit der Verknüpfung. Irreduzible Darstellungen sind die Bausteine; für endliche Gruppen gelten Orthogonalitätsrelationen der Charaktere. Physik: Entartungen von Energieniveaus entsprechen Dimensionen irreduzibler Darstellungen der Symmetriegruppe.
            """,
                fm("Darstellung", "D(g h) = D(g) D(h)"),
                fm("Charakterorthogonalität", "(1/|G|) Σ_g χ^{(a)}(g)* χ^{(b)}(g) = δ_ab"),
                fm("Dimensionssumme", "Σ_a d_a² = |G|")
            ),
            sec("Lie-Gruppen und Lie-Algebren", """
                Kontinuierliche Symmetrien werden durch Generatoren erzeugt; ihre Kommutatoren definieren die Lie-Algebra. SU(2) und SO(3) haben dieselbe Algebra (Drehimpuls), aber SU(2) besitzt zusätzlich halbzahlige Darstellungen — Spin ½. Das Standardmodell beruht auf SU(3) × SU(2) × U(1).
            """,
                fm("Exponentialabbildung", "U = exp(−i θ·J/ħ)"),
                fm("su(2)", "[Jᵢ, Jⱼ] = iħ εᵢⱼₖ Jₖ"),
                fm("Kopplung", "j₁ ⊗ j₂ = |j₁ − j₂| ⊕ … ⊕ (j₁ + j₂)")
            ),
            sec("Auswahlregeln", """
                Matrixelemente ⟨a|T|b⟩ eines Tensoroperators verschwinden, wenn das Produkt der Darstellungen von a, T und b die triviale Darstellung nicht enthält. Wigner-Eckart trennt Geometrie (Clebsch-Gordan) von Dynamik (reduziertes Matrixelement): Daraus folgen die Dipolauswahlregeln Δl = ±1, Δm = 0, ±1.
            """,
                fm("Wigner-Eckart", "⟨j′m′|T^k_q|jm⟩ = ⟨jm; kq|j′m′⟩ ⟨j′‖T^k‖j⟩/√(2j′ + 1)")
            )
        )
    )
}

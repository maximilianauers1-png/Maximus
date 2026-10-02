package app.maximus.lab.domain

internal object CompendiumQuantum {
    val chapters = listOf(
        chapter(
            "qm_origins", Topic.QUANTUM, 1, "Ursprünge der Quantenphysik",
            "Planck, Einstein, Compton, de Broglie und Bohr: Experimente, die die klassische Physik sprengten.",
            listOf("planck", "photoeffect", "compton", "photon"),
            sec("Lichtquanten", """
                1900 erklärte Planck das Spektrum des schwarzen Strahlers mit der Annahme, dass Oszillatoren Energie nur in Paketen hν austauschen. Einstein nahm 1905 die Quanten ernst: Licht besteht aus Photonen. Beim Photoeffekt hängt die Energie der ausgelösten Elektronen nur von der Frequenz ab, ihre Anzahl von der Intensität — klassisch unerklärlich.

                Compton (1923): Röntgenphotonen streuen an Elektronen wie Billardkugeln mit Impuls h/λ.
            """,
                fm("Photonenenergie", "E = hν = ħω"),
                fm("Photonenimpuls", "p = h/λ = ħk"),
                fm("Photoeffekt", "E_kin,max = hν − W_A"),
                fm("Compton-Verschiebung", "Δλ = (h/mₑc)(1 − cos θ)")
            ),
            sec("Materiewellen", """
                De Broglie (1924) postulierte umgekehrt, dass auch Teilchen Wellen sind. Davisson und Germer bestätigten 1927 die Elektronenbeugung am Nickelkristall. Heute interferieren sogar Moleküle mit über 2000 Atomen.
            """,
                fm("De-Broglie-Wellenlänge", "λ = h/p"),
                fm("Thermische Wellenlänge", "λ_th = h/√(2πmk_BT)")
            ),
            sec("Bohrsches Atommodell", """
                Bohr (1913) postulierte stationäre Bahnen mit quantisiertem Drehimpuls L = nħ. Damit folgen die Energieniveaus und die Spektrallinien des Wasserstoffs korrekt — die Feinstruktur und Mehrelektronenatome aber nicht. Die volle Erklärung brachte erst die Schrödinger-Gleichung.
            """,
                fm("Bohr-Radius", "a₀ = 4πε₀ħ²/(mₑe²) = 0,529 Å"),
                fm("Energieniveaus", "Eₙ = −13,6 eV · Z²/n²"),
                fm("Rydberg-Formel", "1/λ = R_∞ Z² (1/n₁² − 1/n₂²)")
            )
        ),
        chapter(
            "qm_formalism", Topic.QUANTUM, 1, "Schrödinger-Gleichung und Formalismus",
            "Wellenfunktion, Operatoren, Messung, Unschärfe und Dirac-Notation.",
            listOf("box", "wavepacket"),
            sec("Wellenfunktion und Schrödinger-Gleichung", """
                Der Zustand eines Teilchens ist eine komplexe Wellenfunktion ψ(r, t). Nach Born ist |ψ|² die Aufenthaltswahrscheinlichkeitsdichte; ψ muss normiert sein. Die zeitliche Entwicklung bestimmt die Schrödinger-Gleichung, eine lineare Gleichung erster Ordnung in der Zeit.

                Für zeitunabhängige Potentiale führt der Separationsansatz ψ = φ(r) e^{−iEt/ħ} auf das Eigenwertproblem des Hamilton-Operators: Stationäre Zustände haben scharfe Energie.
            """,
                fm("Schrödinger-Gleichung", "iħ ∂ψ/∂t = −(ħ²/2m)∇²ψ + Vψ"),
                fm("Stationär", "Ĥφ = Eφ"),
                fm("Born-Regel", "∫ |ψ|² d³r = 1"),
                fm("Wahrscheinlichkeitsstrom", "j = (ħ/2mi)(ψ*∇ψ − ψ∇ψ*)")
            ),
            sec("Observablen und Messung", """
                Messgrößen sind hermitesche Operatoren; mögliche Messwerte sind ihre (reellen) Eigenwerte. Die Wahrscheinlichkeit für den Eigenwert aₙ ist |⟨aₙ|ψ⟩|²; nach der Messung befindet sich das System im zugehörigen Eigenzustand.

                Nicht vertauschende Operatoren können nicht gleichzeitig scharf sein. Für Ort und Impuls gilt die kanonische Vertauschungsrelation, daraus die Heisenbergsche Unschärferelation (Robertson-Ungleichung).
            """,
                fm("Impulsoperator", "p̂ = −iħ∇"),
                fm("Erwartungswert", "⟨A⟩ = ⟨ψ|Â|ψ⟩"),
                fm("Kanonischer Kommutator", "[x̂, p̂] = iħ"),
                fm("Robertson", "σ_A σ_B ≥ |⟨[Â, B̂]⟩|/2"),
                fm("Heisenberg", "σ_x σ_p ≥ ħ/2")
            ),
            sec("Dirac-Notation und Postulate", """
                Zustände sind Vektoren |ψ⟩ in einem Hilbertraum, ⟨φ|ψ⟩ ist das Skalarprodukt. Vollständige Orthonormalbasen erlauben die Zerlegung 1 = Σ |n⟩⟨n|. Die Zeitentwicklung ist unitär: |ψ(t)⟩ = e^{−iĤt/ħ}|ψ(0)⟩.

                Das Ehrenfest-Theorem zeigt, dass Erwartungswerte den klassischen Bewegungsgleichungen folgen, solange das Potential über die Breite des Wellenpakets glatt ist.
            """,
                fm("Zeitentwicklung", "|ψ(t)⟩ = e^{−iĤt/ħ}|ψ(0)⟩"),
                fm("Heisenberg-Gleichung", "dÂ/dt = (i/ħ)[Ĥ, Â] + ∂Â/∂t"),
                fm("Ehrenfest", "d⟨p⟩/dt = −⟨∇V⟩")
            )
        ),
        chapter(
            "qm_1d", Topic.QUANTUM, 2, "Eindimensionale Probleme",
            "Potentialtöpfe, Stufen, Tunneleffekt und der harmonische Oszillator.",
            listOf("box", "finite_well", "tunnel", "oscillator"),
            sec("Potentialtöpfe", """
                Im unendlich tiefen Topf erzwingen die Randbedingungen ψ = 0 stehende Wellen; die Energien wachsen mit n². Die Nullpunktsenergie E₁ > 0 ist eine direkte Folge der Unschärferelation.

                Im endlichen Topf dringen die Wellenfunktionen exponentiell in die Wände ein. Die Energien sind kleiner als im unendlichen Topf gleicher Breite, und es gibt nur endlich viele gebundene Zustände — in 1D aber stets mindestens einen.
            """,
                fm("Unendlicher Topf", "Eₙ = n²π²ħ²/(2mL²),   ψₙ = √(2/L) sin(nπx/L)"),
                fm("Endlicher Topf (gerade)", "z tan z = √(z₀² − z²),   z₀ = a√(2mV₀)/ħ")
            ),
            sec("Tunneleffekt", """
                Trifft ein Teilchen mit E < V₀ auf eine Barriere, ist die Wellenfunktion darin nicht null, sondern fällt mit κ = √(2m(V₀−E))/ħ ab. Ist die Barriere endlich breit, bleibt eine Transmissionswahrscheinlichkeit.

                Anwendungen: α-Zerfall (Gamow), Rastertunnelmikroskop (exponentielle Abstandsabhängigkeit!), Tunneldioden, Flash-Speicher, Kernfusion in der Sonne.
            """,
                fm("Transmission (dicke Barriere)", "T ≈ 16 (E/V₀)(1 − E/V₀) e^{−2κa}"),
                fm("WKB-Tunneln", "T ≈ exp(−(2/ħ) ∫ √(2m(V − E)) dx)")
            ),
            sec("Harmonischer Oszillator", """
                Jedes Potential ist nahe seinem Minimum näherungsweise harmonisch, daher ist der Oszillator allgegenwärtig: Molekülschwingungen, Phononen, Moden des elektromagnetischen Feldes.

                Die algebraische Lösung mit Leiteroperatoren ist elegant: â† erzeugt, â vernichtet ein Quant ħω. Der Grundzustand ist eine Gauß-Funktion minimaler Unschärfe.
            """,
                fm("Energien", "Eₙ = ħω(n + ½)"),
                fm("Leiteroperatoren", "â = √(mω/2ħ)(x̂ + ip̂/mω),   [â, â†] = 1"),
                fm("Hamilton", "Ĥ = ħω(â†â + ½)"),
                fm("Wirkung", "â†|n⟩ = √(n+1)|n+1⟩,   â|n⟩ = √n|n−1⟩")
            )
        ),
        chapter(
            "qm_hydrogen", Topic.QUANTUM, 2, "Drehimpuls und Wasserstoffatom",
            "Kugelflächenfunktionen, Radialgleichung, Quantenzahlen und Spektren.",
            listOf("hydrogen"),
            sec("Drehimpuls", """
                Die Komponenten des Drehimpulses vertauschen nicht miteinander, aber jede mit L². Gemeinsame Eigenfunktionen von L² und L_z sind die Kugelflächenfunktionen Y_l^m mit l = 0, 1, 2, … und m = −l … l.
            """,
                fm("Kommutator", "[L̂_x, L̂_y] = iħ L̂_z"),
                fm("Eigenwerte", "L² = ħ² l(l+1),   L_z = ħm"),
                fm("Leiteroperatoren", "L̂± = L̂_x ± iL̂_y")
            ),
            sec("Wasserstoffatom", """
                Im Coulomb-Potential separiert die Schrödinger-Gleichung in Kugelkoordinaten. Die Radialgleichung liefert die Hauptquantenzahl n = 1, 2, … mit l ≤ n − 1. Die Energie hängt nur von n ab (zufällige Entartung, Folge der verborgenen SO(4)-Symmetrie mit dem Runge-Lenz-Vektor): Entartungsgrad n² (2n² mit Spin).

                Feinstruktur (Größenordnung α²), Lamb-Verschiebung (QED) und Hyperfeinstruktur (21-cm-Linie) spalten die Niveaus weiter auf.
            """,
                fm("Energie", "Eₙ = −mₑc²α²/(2n²) = −13,606 eV/n²"),
                fm("Grundzustand", "ψ₁₀₀ = (πa₀³)^{−1/2} e^{−r/a₀}"),
                fm("Radialfunktion", "Rₙₗ ∝ e^{−r/na₀} ρ^l L^{2l+1}_{n−l−1}(ρ),   ρ = 2r/na₀"),
                fm("Erwartungswert", "⟨r⟩ = (a₀/2)(3n² − l(l+1))")
            )
        ),
        chapter(
            "qm_spin", Topic.QUANTUM, 2, "Spin und identische Teilchen",
            "Pauli-Matrizen, Stern-Gerlach, Drehimpulsaddition, Pauli-Prinzip und Periodensystem.",
            listOf("rabi"),
            sec("Spin ½", """
                Der Spin ist ein innerer Drehimpuls ohne klassisches Gegenstück. Für Spin ½ bilden die Pauli-Matrizen die Operatoren; eine Drehung um 2π multipliziert den Zustand mit −1 (Spinor). Im Stern-Gerlach-Versuch spaltet ein Silberstrahl in zwei Teilstrahlen.

                Das magnetische Moment des Elektrons ist μ = −g μ_B S/ħ mit g ≈ 2,00232; die Abweichung von 2 ist ein Präzisionstest der QED.
            """,
                fm("Pauli-Matrizen", "σ_x = (0 1; 1 0),  σ_y = (0 −i; i 0),  σ_z = (1 0; 0 −1)"),
                fm("Spinoperator", "Ŝ = (ħ/2) σ"),
                fm("Algebra", "σᵢσⱼ = δᵢⱼ + iεᵢⱼₖσₖ"),
                fm("Larmor-Frequenz", "ω_L = gμ_B B/ħ")
            ),
            sec("Addition von Drehimpulsen", """
                Zwei Spins ½ koppeln zu Triplett (S = 1, symmetrisch) und Singulett (S = 0, antisymmetrisch). Allgemein läuft der Gesamtdrehimpuls von |j₁ − j₂| bis j₁ + j₂; die Clebsch-Gordan-Koeffizienten vermitteln zwischen den Basen. Die Spin-Bahn-Kopplung ∝ L·S erzeugt die Feinstruktur.
            """,
                fm("Kopplung", "j = |j₁ − j₂|, …, j₁ + j₂"),
                fm("Singulett", "|0,0⟩ = (|↑↓⟩ − |↓↑⟩)/√2")
            ),
            sec("Identische Teilchen", """
                Vertauschen zweier identischer Teilchen ändert den Zustand höchstens um ein Vorzeichen. Fermionen (halbzahliger Spin) sind antisymmetrisch, Bosonen symmetrisch (Spin-Statistik-Theorem, ein Resultat der relativistischen QFT). Daraus folgt das Pauli-Prinzip: Keine zwei Fermionen im selben Zustand.

                Konsequenzen: der Aufbau des Periodensystems, die Stabilität der Materie, Austauschwechselwirkung und Ferromagnetismus, Fermi-Druck in weißen Zwergen.
            """,
                fm("Slater-Determinante", "Ψ = (1/√N!) det[φᵢ(xⱼ)]")
            )
        ),
        chapter(
            "qm_approx", Topic.QUANTUM, 3, "Näherungsmethoden",
            "Störungstheorie, Variationsprinzip, WKB und Fermis Goldene Regel.",
            listOf("tunnel"),
            sec("Zeitunabhängige Störungstheorie", """
                Für Ĥ = Ĥ₀ + λV̂ mit bekannter Lösung von Ĥ₀ entwickelt man nach Potenzen von λ. Die Korrektur erster Ordnung ist der Erwartungswert der Störung; die zweite Ordnung senkt den Grundzustand immer ab. Bei Entartung muss man zuerst V̂ im entarteten Unterraum diagonalisieren.
            """,
                fm("Erste Ordnung", "Eₙ⁽¹⁾ = ⟨n|V̂|n⟩"),
                fm("Zweite Ordnung", "Eₙ⁽²⁾ = Σ_{k≠n} |⟨k|V̂|n⟩|²/(Eₙ − E_k)")
            ),
            sec("Variationsverfahren und WKB", """
                Jeder normierte Versuchszustand liefert eine obere Schranke für die Grundzustandsenergie. Mit geschickten Ansätzen (z. B. Gauß-Funktionen für das Heliumatom) erhält man sehr gute Werte; die Quantenchemie basiert darauf.

                Die WKB-Näherung gilt, wenn sich das Potential auf der Skala der Wellenlänge langsam ändert. Sie liefert die Bohr-Sommerfeld-Quantisierung mit Maslov-Korrektur und Tunnelraten.
            """,
                fm("Variationsprinzip", "E₀ ≤ ⟨ψ|Ĥ|ψ⟩/⟨ψ|ψ⟩"),
                fm("Bohr-Sommerfeld", "∮ p dx = 2πħ(n + ½)")
            ),
            sec("Zeitabhängige Störungen", """
                Eine periodische Störung induziert Übergänge zwischen Niveaus. Für ein Kontinuum von Endzuständen ergibt sich eine konstante Übergangsrate: Fermis Goldene Regel. Sie beschreibt Absorption, spontane Emission, β-Zerfall und Streuprozesse.
            """,
                fm("Goldene Regel", "Γ_{i→f} = (2π/ħ) |⟨f|V̂|i⟩|² ρ(E_f)"),
                fm("Rabi-Formel", "P(t) = Ω²/(Ω² + Δ²) · sin²(√(Ω² + Δ²) t/2)")
            )
        ),
        chapter(
            "qm_info", Topic.QUANTUM, 3, "Verschränkung und Quanteninformation",
            "Qubits, Dichtematrizen, Bellsche Ungleichungen und Dekohärenz.",
            emptyList(),
            sec("Qubits und Dichtematrix", """
                Ein Qubit ist ein Zwei-Niveau-System |ψ⟩ = α|0⟩ + β|1⟩, darstellbar als Punkt auf der Bloch-Kugel. Gemischte Zustände beschreibt die Dichtematrix ρ; reine Zustände erfüllen Tr ρ² = 1. Die von-Neumann-Entropie misst die Gemischtheit.
            """,
                fm("Bloch-Darstellung", "|ψ⟩ = cos(θ/2)|0⟩ + e^{iφ} sin(θ/2)|1⟩"),
                fm("Dichtematrix", "ρ = Σ pᵢ |ψᵢ⟩⟨ψᵢ|,   ⟨A⟩ = Tr(ρÂ)"),
                fm("Von-Neumann-Entropie", "S = −Tr(ρ ln ρ)")
            ),
            sec("Verschränkung und Bell", """
                Ein Zustand zweier Systeme heißt verschränkt, wenn er sich nicht als Produkt schreiben lässt (z. B. Bell-Zustände). Messungen an verschränkten Teilchen sind stärker korreliert, als jede lokal-realistische Theorie erlaubt: Die CHSH-Größe ist klassisch ≤ 2, quantenmechanisch bis 2√2 (Tsirelson). Die Experimente von Aspect, Clauser und Zeilinger (Nobelpreis 2022) bestätigen die Quantenmechanik.

                Verschränkung ermöglicht Teleportation, Quantenkryptographie (E91) und ist die Ressource des Quantencomputers. Das No-Cloning-Theorem verbietet das Kopieren unbekannter Zustände.
            """,
                fm("Bell-Zustand", "|Φ⁺⟩ = (|00⟩ + |11⟩)/√2"),
                fm("CHSH", "|S| ≤ 2 (lokal-realistisch),   |S| ≤ 2√2 (Quantenmechanik)")
            )
        )
    )
}

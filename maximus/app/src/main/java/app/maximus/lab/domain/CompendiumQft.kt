package app.maximus.lab.domain

internal object CompendiumQft {
    val chapters = listOf(
        chapter(
            "qft_relativity", Topic.QFT, 1, "Relativität und natürliche Einheiten",
            "Vierervektoren, Metrik, Lorentz-Gruppe und das Rechnen mit ħ = c = 1.",
            listOf("natural_units", "kinematics"),
            sec("Minkowski-Raum", """
                Ereignisse sind Vierervektoren x^μ = (ct, x). Wir verwenden die Metrik g_μν = diag(+1, −1, −1, −1). Lorentz-Transformationen Λ lassen das Skalarprodukt x·y = g_μν x^μ y^ν invariant; sie bilden die Lorentz-Gruppe SO(1,3), zusammen mit Translationen die Poincaré-Gruppe.

                Indizes werden mit der Metrik gehoben und gesenkt; über doppelt auftretende Indizes (einmal oben, einmal unten) wird summiert (Einsteinsche Summenkonvention).
            """,
                fm("Metrik", "g_μν = diag(1, −1, −1, −1)"),
                fm("Invariantes Intervall", "s² = c²t² − x²"),
                fm("Lorentz-Bedingung", "Λ^T g Λ = g"),
                fm("Viererimpuls", "p^μ = (E/c, p),   p² = m²c²")
            ),
            sec("Natürliche Einheiten", """
                Mit ħ = c = 1 haben alle Größen die Dimension einer Potenz der Energie: Masse und Impuls ~ E, Länge und Zeit ~ 1/E. Zurück ins SI rechnet man mit ħc = 197,327 MeV fm und ħ = 6,582·10⁻²² MeV s. Wirkungsquerschnitte: 1 GeV⁻² = 0,3894 mb.

                Die Feinstrukturkonstante α = e²/(4πε₀ħc) ≈ 1/137 ist dimensionslos und daher in allen Einheitensystemen gleich; in Heaviside-Lorentz-Einheiten mit ħ = c = 1 gilt e = √(4πα) ≈ 0,303.
            """,
                fm("Umrechnung Länge", "ħc = 197,327 MeV fm"),
                fm("Umrechnung Zeit", "ħ = 6,582·10⁻²² MeV s"),
                fm("Querschnitt", "1 GeV⁻² = 0,3894 mb"),
                fm("Feinstrukturkonstante", "α = e²/(4π) ≈ 1/137,036  (ħ = c = ε₀ = 1)")
            ),
            sec("Kinematik", """
                Invariante Größen vereinfachen alles: die Schwerpunktsenergie √s eines Stoßes, die Masse eines Zerfallsprodukts aus den Vierervektoren der Töchter (invariante Masse, so wurde das Higgs-Boson gefunden), die Mandelstam-Variablen s, t, u eines 2→2-Prozesses.
            """,
                fm("Mandelstam", "s = (p₁ + p₂)²,   t = (p₁ − p₃)²,   u = (p₁ − p₄)²"),
                fm("Summenregel", "s + t + u = Σ mᵢ²"),
                fm("Rapidität", "y = ½ ln((E + p_z)/(E − p_z))")
            )
        ),
        chapter(
            "qft_classical", Topic.QFT, 1, "Klassische Feldtheorie",
            "Lagrange-Dichte, Euler-Lagrange-Gleichungen, Noether-Theorem und Eichsymmetrie.",
            emptyList(),
            sec("Wirkungsprinzip für Felder", """
                Ein Feld φ(x) ist ein System mit unendlich vielen Freiheitsgraden. Die Dynamik folgt aus einer lokalen, lorentzinvarianten Lagrange-Dichte 𝓛(φ, ∂_μφ) über das Prinzip stationärer Wirkung.
            """,
                fm("Wirkung", "S = ∫ d⁴x 𝓛(φ, ∂_μφ)"),
                fm("Euler-Lagrange", "∂_μ (∂𝓛/∂(∂_μφ)) − ∂𝓛/∂φ = 0"),
                fm("Skalarfeld", "𝓛 = ½ ∂_μφ ∂^μφ − ½ m²φ² − (λ/4!) φ⁴")
            ),
            sec("Noether-Theorem", """
                Jede kontinuierliche Symmetrie der Wirkung liefert einen erhaltenen Strom. Raumzeit-Translationen ergeben den Energie-Impuls-Tensor (Energie- und Impulserhaltung), Lorentz-Transformationen den Drehimpuls, Phasendrehungen ψ → e^{iα}ψ eine erhaltene Ladung.
            """,
                fm("Noether-Strom", "j^μ = (∂𝓛/∂(∂_μφ)) δφ,   ∂_μ j^μ = 0"),
                fm("Erhaltene Ladung", "Q = ∫ d³x j⁰"),
                fm("Energie-Impuls-Tensor", "T^μν = (∂𝓛/∂(∂_μφ)) ∂^νφ − g^μν 𝓛")
            ),
            sec("Eichsymmetrie", """
                Fordert man die Phasensymmetrie lokal, ψ → e^{iα(x)}ψ, muss die Ableitung durch eine kovariante Ableitung mit einem Eichfeld A_μ ersetzt werden. So erzwingt die Symmetrie die Wechselwirkung: Die QED ist die Eichtheorie der Gruppe U(1). Ein Massenterm m²A_μA^μ ist nicht eichinvariant — das Photon ist masselos.
            """,
                fm("Kovariante Ableitung", "D_μ = ∂_μ + ieA_μ"),
                fm("Eichtransformation", "ψ → e^{iα(x)}ψ,   A_μ → A_μ − (1/e)∂_μα"),
                fm("Maxwell-Lagrange", "𝓛 = −¼ F_μν F^μν,   F_μν = ∂_μA_ν − ∂_νA_μ")
            )
        ),
        chapter(
            "qft_scalar", Topic.QFT, 2, "Quantisierung des Skalarfeldes",
            "Klein-Gordon, Fock-Raum, Teilchen als Feldquanten, Propagator und Kausalität.",
            listOf("dispersion", "yukawa"),
            sec("Klein-Gordon-Gleichung", """
                Ersetzt man in E² = p² + m² Energie und Impuls durch Operatoren, entsteht die Klein-Gordon-Gleichung. Als Einteilchen-Wellengleichung hat sie Probleme (negative Energien, keine positiv-definite Wahrscheinlichkeitsdichte); als Feldgleichung ist sie konsistent.
            """,
                fm("Klein-Gordon", "(□ + m²)φ = 0,   □ = ∂_t² − ∇²"),
                fm("Dispersion", "ω_p = √(p² + m²)")
            ),
            sec("Kanonische Quantisierung", """
                Das Feld und sein konjugierter Impuls π = φ̇ erhalten gleichzeitige Vertauschungsrelationen. Die Fourier-Moden sind unabhängige harmonische Oszillatoren; ihre Leiteroperatoren erzeugen und vernichten Teilchen mit Impuls p. Der Fock-Raum enthält Zustände mit beliebiger Teilchenzahl, aufgebaut aus dem Vakuum |0⟩.

                Die Nullpunktsenergien aller Moden summieren sich zu einer divergenten Vakuumenergie; man entfernt sie durch Normalordnung. (Ihre Gravitationswirkung, das Problem der kosmologischen Konstante, ist ungelöst.)
            """,
                fm("Kommutator", "[φ(x, t), π(y, t)] = iδ³(x − y)"),
                fm("Modenentwicklung", "φ(x) = ∫ d³p/((2π)³√(2ω_p)) (a_p e^{−ip·x} + a_p† e^{ip·x})"),
                fm("Leiteroperatoren", "[a_p, a_q†] = (2π)³ δ³(p − q)"),
                fm("Hamilton-Operator", "H = ∫ d³p/(2π)³ ω_p a_p† a_p")
            ),
            sec("Kausalität und Propagator", """
                Der Kommutator [φ(x), φ(y)] verschwindet für raumartige Abstände: Messungen außerhalb des Lichtkegels beeinflussen sich nicht. Die Amplitude für die Ausbreitung eines Teilchens ist der Feynman-Propagator, eine Green-Funktion des Klein-Gordon-Operators. Die iε-Vorschrift sorgt dafür, dass positive Energien vorwärts und negative rückwärts in der Zeit laufen — Antiteilchen.

                Der Austausch eines massiven Teilchens erzeugt im statischen Grenzfall das Yukawa-Potential.
            """,
                fm("Feynman-Propagator", "D_F(p) = i/(p² − m² + iε)"),
                fm("Yukawa-Potential", "V(r) = −(g²/4π) e^{−mr}/r")
            )
        ),
        chapter(
            "qft_dirac", Topic.QFT, 2, "Das Dirac-Feld",
            "Dirac-Gleichung, γ-Matrizen, Spinoren, Antiteilchen und Spin-Statistik.",
            emptyList(),
            sec("Dirac-Gleichung", """
                Dirac suchte 1928 eine Gleichung erster Ordnung in der Zeit. Sie erfordert 4×4-Matrizen γ^μ, die eine Clifford-Algebra erfüllen, und vierkomponentige Spinoren. Ergebnis: Spin ½, g = 2 und Lösungen negativer Energie, die Dirac als Antiteilchen deutete — das Positron wurde 1932 entdeckt.
            """,
                fm("Dirac-Gleichung", "(iγ^μ∂_μ − m)ψ = 0"),
                fm("Clifford-Algebra", "{γ^μ, γ^ν} = 2g^μν"),
                fm("Dirac-Lagrange", "𝓛 = ψ̄(iγ^μ∂_μ − m)ψ,   ψ̄ = ψ†γ⁰"),
                fm("Feynman-Slash", "a̸ = γ^μ a_μ,   a̸a̸ = a²")
            ),
            sec("Spinoren und Chiralität", """
                Freie Lösungen sind u(p)e^{−ipx} (Teilchen) und v(p)e^{ipx} (Antiteilchen). γ⁵ = iγ⁰γ¹γ²γ³ definiert die Chiralität; die Projektoren P_{L,R} = (1 ∓ γ⁵)/2 trennen links- und rechtshändige Komponenten. Die schwache Wechselwirkung koppelt nur an linkshändige Fermionen (Paritätsverletzung).
            """,
                fm("Spinsummen", "Σ_s u ū = p̸ + m,   Σ_s v v̄ = p̸ − m"),
                fm("Chirale Projektoren", "P_L = (1 − γ⁵)/2,   P_R = (1 + γ⁵)/2")
            ),
            sec("Quantisierung mit Antikommutatoren", """
                Das Dirac-Feld muss mit Antikommutatoren quantisiert werden; mit Kommutatoren wäre die Energie nicht nach unten beschränkt. Damit folgt das Pauli-Prinzip automatisch. Allgemein: Mikrokausalität und positive Energie erzwingen Bose-Statistik für ganzzahligen und Fermi-Statistik für halbzahligen Spin (Spin-Statistik-Theorem).
            """,
                fm("Antikommutator", "{ψ_a(x), ψ_b†(y)} = δ_ab δ³(x − y)"),
                fm("Fermion-Propagator", "S_F(p) = i(p̸ + m)/(p² − m² + iε)")
            )
        ),
        chapter(
            "qft_feynman", Topic.QFT, 3, "Wechselwirkung und Feynman-Diagramme",
            "Wechselwirkungsbild, S-Matrix, Wick-Theorem, Feynman-Regeln, Querschnitte und Zerfallsraten.",
            listOf("ee_mumu", "klein_nishina"),
            sec("Störungstheorie und S-Matrix", """
                Im Wechselwirkungsbild entwickelt sich der Zustand mit dem Wechselwirkungsanteil H_I. Die Streumatrix verknüpft einlaufende und auslaufende Teilchen; die Dyson-Reihe entwickelt sie in Potenzen der Kopplung. Das Wick-Theorem zerlegt zeitgeordnete Produkte in Propagatoren; jeder Term entspricht einem Feynman-Diagramm.
            """,
                fm("Dyson-Reihe", "S = T exp(−i ∫ d⁴x 𝓗_I(x))"),
                fm("Matrixelement", "⟨f|S − 1|i⟩ = (2π)⁴ δ⁴(P_f − P_i) i𝓜")
            ),
            sec("Feynman-Regeln der QED", """
                • Äußeres Elektron: u(p) einlaufend, ū(p) auslaufend; Positron: v̄ einlaufend, v auslaufend.
                • Äußeres Photon: Polarisationsvektor ε_μ bzw. ε_μ*.
                • Vertex: −ieγ^μ, Viererimpulserhaltung.
                • Interner Fermion-Propagator: i(p̸ + m)/(p² − m² + iε).
                • Interner Photon-Propagator (Feynman-Eichung): −ig_μν/(q² + iε).
                • Über unbestimmte Schleifenimpulse integrieren: ∫ d⁴k/(2π)⁴; geschlossene Fermion-Schleife: Faktor −1 und Spur.
            """,
                fm("QED-Lagrange", "𝓛 = ψ̄(iD̸ − m)ψ − ¼F_μνF^μν"),
                fm("QED-Vertex", "−ieγ^μ"),
                fm("Photon-Propagator", "−ig_μν/q²")
            ),
            sec("Wirkungsquerschnitt und Zerfallsrate", """
                Aus |𝓜|² folgt durch Integration über den lorentzinvarianten Phasenraum der Querschnitt bzw. die Zerfallsbreite. Für 2→2 im Schwerpunktsystem und masselose Endzustände vereinfacht sich das stark. Spin-Mittelung über Anfangs- und Summation über Endzustände erfolgt mit Spurtheoremen.
            """,
                fm("Querschnitt (CM, 2→2)", "dσ/dΩ = |𝓜|²/(64π² s) · |p_f|/|p_i|"),
                fm("Zerfallsrate (2-Körper)", "Γ = |p*| |𝓜|²/(8π M²)"),
                fm("Spurtheoreme", "Tr(γ^μγ^ν) = 4g^μν,   Tr(γ^μγ^νγ^ργ^σ) = 4(g^μνg^ρσ − g^μρg^νσ + g^μσg^νρ)")
            ),
            sec("Klassische QED-Ergebnisse", """
                e⁺e⁻ → μ⁺μ⁻ ist der einfachste Prozess: ein Photon im s-Kanal, Winkelverteilung 1 + cos²θ. Compton-Streuung liefert die Klein-Nishina-Formel, Møller- und Bhabha-Streuung die e⁻e⁻- und e⁺e⁻-Streuung. Das R-Verhältnis von Hadronen zu Myonpaaren bewies drei Farben.
            """,
                fm("e⁺e⁻ → μ⁺μ⁻", "σ = 4πα²/(3s),   dσ/dΩ = (α²/4s)(1 + cos²θ)"),
                fm("R-Verhältnis", "R = N_c Σ_q Q_q²")
            )
        ),
        chapter(
            "qft_renorm", Topic.QFT, 3, "Renormierung und laufende Kopplungen",
            "Schleifendivergenzen, Regularisierung, Renormierungsgruppe, β-Funktion, asymptotische Freiheit, g − 2.",
            listOf("running"),
            sec("Divergenzen und Regularisierung", """
                Schleifendiagramme enthalten Integrale über beliebig hohe Impulse, die divergieren. Man regularisiert (Impuls-Cutoff, Pauli-Villars oder dimensionale Regularisierung mit d = 4 − 2ε) und absorbiert die Divergenzen in die nicht beobachtbaren „nackten“ Parameter. Eine Theorie ist renormierbar, wenn endlich viele Gegenterme genügen; QED, QCD und das Standardmodell sind es (’t Hooft, Veltman, Nobelpreis 1999).
            """,
                fm("Oberflächliche Divergenz (QED)", "D = 4 − (3/2)E_e − E_γ")
            ),
            sec("Renormierungsgruppe", """
                Die Kopplungen hängen von der Energieskala μ ab, an der man sie definiert. Die β-Funktion beschreibt diese Abhängigkeit. In der QED ist β > 0: Virtuelle e⁺e⁻-Paare schirmen die Ladung ab, α wächst mit der Energie (1/137 → 1/128 bei M_Z). In der QCD dominieren Gluonen mit Selbstwechselwirkung, β < 0: asymptotische Freiheit (Gross, Wilczek, Politzer, Nobelpreis 2004). Bei kleinen Energien wird α_s groß — Confinement, Λ_QCD ≈ 200 MeV.
            """,
                fm("β-Funktion", "μ dg/dμ = β(g)"),
                fm("QED, 1-Loop", "β(α) = 2α²/(3π)  (ein Fermion)"),
                fm("QCD, 1-Loop", "α_s(Q²) = 4π/(b₀ ln(Q²/Λ²)),   b₀ = 11 − 2n_f/3")
            ),
            sec("Anomales magnetisches Moment", """
                Die Dirac-Theorie sagt g = 2 voraus. Schleifenkorrekturen ergeben a = (g − 2)/2; der erste Term α/2π stammt von Schwinger (1948). Das Elektronmoment ist auf über 10 Stellen gemessen und berechnet — der genaueste Vergleich von Theorie und Experiment in der Physik. Beim Myon wird um eine mögliche Abweichung gerungen.
            """,
                fm("Schwinger-Term", "a = (g − 2)/2 = α/(2π) + O(α²) ≈ 0,00116")
            )
        ),
        chapter(
            "qft_sm", Topic.QFT, 3, "Eichtheorien und Standardmodell",
            "Yang-Mills, QCD, Higgs-Mechanismus und elektroschwache Vereinigung.",
            listOf("muon_decay", "kinematics"),
            sec("Yang-Mills-Theorien", """
                Verallgemeinert man die lokale Eichsymmetrie auf nichtabelsche Gruppen (SU(N)), tragen die Eichbosonen selbst Ladung und wechselwirken miteinander (Drei- und Vier-Gluon-Vertices). Die Feldstärke enthält einen Kommutatorterm. Die QCD ist die SU(3)-Eichtheorie der Farbe mit acht Gluonen und Quarks in drei Farben.
            """,
                fm("Feldstärke", "F^a_μν = ∂_μA^a_ν − ∂_νA^a_μ + g f^{abc} A^b_μ A^c_ν"),
                fm("Yang-Mills-Lagrange", "𝓛 = −¼ F^a_μν F^{aμν} + ψ̄(iD̸ − m)ψ"),
                fm("Kovariante Ableitung", "D_μ = ∂_μ − igT^a A^a_μ")
            ),
            sec("Higgs-Mechanismus", """
                Massenterme für W- und Z-Bosonen würden die Eichsymmetrie brechen. Stattdessen erhält ein komplexes Skalardublett ein Potential mit Minimum bei ⟨φ⟩ = v/√2 ≠ 0 (v ≈ 246 GeV): spontane Symmetriebrechung. Drei Goldstone-Moden werden zu den longitudinalen Polarisationen von W± und Z; übrig bleibt das Higgs-Boson (entdeckt 2012 am LHC, m_H ≈ 125 GeV). Fermionmassen entstehen über Yukawa-Kopplungen m_f = y_f v/√2.
            """,
                fm("Higgs-Potential", "V(φ) = −μ²|φ|² + λ|φ|⁴,   v = μ/√λ"),
                fm("Eichbosonmassen", "m_W = gv/2,   m_Z = m_W/cos θ_W"),
                fm("Fermi-Konstante", "G_F/√2 = g²/(8m_W²) = 1/(2v²)")
            ),
            sec("Das Standardmodell", """
                Eichgruppe SU(3)_C × SU(2)_L × U(1)_Y. Materie: drei Generationen von Quarks (u, d; c, s; t, b) und Leptonen (e, ν_e; μ, ν_μ; τ, ν_τ). Eichbosonen: 8 Gluonen, W±, Z, Photon; dazu das Higgs. 19 freie Parameter (plus Neutrinomassen und -mischung).

                Offene Fragen: Dunkle Materie, Neutrinomassen, Materie-Antimaterie-Asymmetrie, Hierarchieproblem, Quantengravitation.
            """,
                fm("Weinberg-Winkel", "e = g sin θ_W,   sin²θ_W ≈ 0,231"),
                fm("Myon-Lebensdauer", "Γ_μ = G_F² m_μ⁵/(192π³)")
            )
        )
    )
}

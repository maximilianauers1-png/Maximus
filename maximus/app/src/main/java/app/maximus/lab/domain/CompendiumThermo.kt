package app.maximus.lab.domain

internal object CompendiumThermo {
    val chapters = listOf(
        chapter(
            "th_laws", Topic.THERMO, 1, "Grundbegriffe und Hauptsätze",
            "Systeme, Zustandsgrößen und die vier Hauptsätze als Fundament der Thermodynamik.",
            listOf("ideal_gas", "cycles"),
            sec("System und Zustand", """
                Ein thermodynamisches System ist ein abgegrenzter Teil der Welt. Offene Systeme tauschen Energie und Materie aus, geschlossene nur Energie, abgeschlossene (isolierte) gar nichts.

                Zustandsgrößen hängen nur vom momentanen Gleichgewichtszustand ab, nicht vom Weg dorthin: Druck p, Volumen V, Temperatur T, innere Energie U, Entropie S. Wärme Q und Arbeit W sind dagegen Prozessgrößen; ihre Differentiale sind unvollständig (δQ, δW).

                • Extensive Größen skalieren mit der Systemgröße (V, U, S, N).
                • Intensive Größen nicht (p, T, chemisches Potential μ).
                • Das Verhältnis zweier extensiver Größen ist intensiv (Dichte, molare Größen).
            """),
            sec("Nullter und erster Hauptsatz", """
                Nullter Hauptsatz: Sind zwei Systeme jeweils mit einem dritten im thermischen Gleichgewicht, so auch miteinander. Er macht die Temperatur zu einer wohldefinierten Messgröße.

                Erster Hauptsatz: Energieerhaltung inklusive Wärme. Die Änderung der inneren Energie ist die zugeführte Wärme plus die am System verrichtete Arbeit. Ein Perpetuum mobile erster Art ist unmöglich.

                > Vorzeichenkonvention (IUPAC): Was dem System zugeführt wird, ist positiv.

                Bei Volumenarbeit gegen äußeren Druck gilt δW = −p dV. Für Prozesse bei konstantem Druck ist die Enthalpie H = U + pV die natürliche Größe: dH = δQ bei p = const.
            """,
                fm("Erster Hauptsatz", "dU = δQ + δW = δQ − p dV", "geschlossenes System, nur Volumenarbeit"),
                fm("Enthalpie", "H = U + pV,   dH = T dS + V dp"),
                fm("Wärmekapazitäten", "C_V = (∂U/∂T)_V,   C_p = (∂H/∂T)_p"),
                fm("Mayer-Relation", "C_p − C_V = nR", "ideales Gas")
            ),
            sec("Zweiter Hauptsatz", """
                Wärme fließt von selbst nur von warm nach kalt (Clausius). Es gibt keine periodisch arbeitende Maschine, die nur Wärme aus einem Reservoir entnimmt und vollständig in Arbeit umwandelt (Kelvin-Planck). Beide Formulierungen sind äquivalent.

                Mathematisch: Es existiert eine Zustandsgröße Entropie S mit dS = δQ_rev/T. In einem abgeschlossenen System nimmt sie nie ab; im Gleichgewicht ist sie maximal.

                Folgerung: Der Wirkungsgrad jeder Wärmekraftmaschine zwischen zwei Reservoiren ist durch den Carnot-Wirkungsgrad begrenzt.
            """,
                fm("Clausius-Ungleichung", "∮ δQ/T ≤ 0", "Gleichheit für reversible Kreisprozesse"),
                fm("Entropiedefinition", "dS = δQ_rev/T"),
                fm("Entropiezunahme", "ΔS_abgeschlossen ≥ 0"),
                fm("Carnot-Wirkungsgrad", "η_C = 1 − T_k/T_w")
            ),
            sec("Dritter Hauptsatz", """
                Nernst-Theorem: Für T → 0 strebt die Entropie eines reinen, perfekt kristallinen Stoffes gegen eine Konstante (gegen null, Planck). Daraus folgt die Unerreichbarkeit des absoluten Nullpunkts in endlich vielen Schritten.

                Konsequenzen: Wärmekapazitäten und thermische Ausdehnungskoeffizienten verschwinden bei T → 0. Debyes T³-Gesetz für Isolatoren und das lineare γT-Gesetz der Elektronen in Metallen erfüllen dies.
            """,
                fm("Nernst-Theorem", "lim_{T→0} S(T) = S₀ = 0  (perfekter Kristall)")
            )
        ),
        chapter(
            "th_gas", Topic.THERMO, 1, "Ideales Gas und kinetische Gastheorie",
            "Vom Teilchenstoß zum Druck: Gleichverteilungssatz, Wärmekapazitäten und Zustandsänderungen.",
            listOf("ideal_gas", "maxwell"),
            sec("Druck aus Teilchenstößen", """
                Ein Teilchen der Masse m mit Geschwindigkeitskomponente v_x überträgt bei elastischer Reflexion an einer Wand den Impuls 2mv_x. Mittelt man über alle Teilchen und nutzt die Isotropie ⟨v_x²⟩ = ⟨v²⟩/3, folgt der Druck als Impulsfluss.

                Der Vergleich mit pV = Nk_BT liefert die mikroskopische Bedeutung der Temperatur: Sie misst die mittlere kinetische Energie der Translation.
            """,
                fm("Kinetischer Druck", "p = (1/3) n m ⟨v²⟩ = (2/3) n ⟨E_kin⟩"),
                fm("Temperatur mikroskopisch", "⟨E_kin⟩ = (3/2) k_B T"),
                fm("Zustandsgleichung", "pV = nRT = N k_B T")
            ),
            sec("Gleichverteilungssatz", """
                Im klassischen Gleichgewicht trägt jeder quadratisch in die Hamilton-Funktion eingehende Freiheitsgrad im Mittel k_BT/2 zur Energie bei.

                • Einatomiges Gas: f = 3, c_V = (3/2)R, γ = 5/3.
                • Zweiatomiges Gas bei Raumtemperatur: 3 Translation + 2 Rotation, f = 5, γ = 7/5. Die Schwingung ist „eingefroren“, weil ħω ≫ k_BT.
                • Festkörper: 6 quadratische Terme pro Atom → Dulong-Petit 3R.

                Das Einfrieren von Freiheitsgraden bei tiefen Temperaturen war eines der ersten Indizien für die Quantenmechanik.
            """,
                fm("Gleichverteilung", "⟨E⟩ = (f/2) k_B T pro Teilchen"),
                fm("Adiabatenexponent", "γ = C_p/C_V = (f + 2)/f")
            ),
            sec("Zustandsänderungen", """
                • Isotherm (T = const): pV = const, ΔU = 0, Q = −W = nRT ln(V₂/V₁).
                • Isobar (p = const): V/T = const, Q = nC_pΔT.
                • Isochor (V = const): p/T = const, W = 0, Q = nC_VΔT.
                • Adiabatisch-reversibel (Q = 0): pV^γ = const, TV^{γ−1} = const.

                In einem p-V-Diagramm ist die Adiabate um den Faktor γ steiler als die Isotherme durch denselben Punkt.
            """,
                fm("Isotherme Arbeit", "W = −nRT ln(V₂/V₁)"),
                fm("Poisson-Gleichungen", "pV^γ = const,   TV^{γ−1} = const,   T^γ p^{1−γ} = const"),
                fm("Adiabatische Arbeit", "W = (p₂V₂ − p₁V₁)/(γ − 1)")
            ),
            sec("Maxwell-Boltzmann-Verteilung", """
                Die Geschwindigkeitskomponenten eines idealen Gases sind unabhängig normalverteilt mit Varianz k_BT/m. Für den Betrag ergibt sich durch Integration über die Kugelschale die Maxwell-Verteilung.

                Die mittlere freie Weglänge λ = 1/(√2 nσ) mit Stoßquerschnitt σ bestimmt Transportgrößen wie Viskosität und Wärmeleitfähigkeit.
            """,
                fm("Maxwell-Verteilung", "f(v) = 4π (m/2πk_BT)^{3/2} v² e^{−mv²/2k_BT}"),
                fm("Charakteristische Geschwindigkeiten", "v_p = √(2k_BT/m),  ⟨v⟩ = √(8k_BT/πm),  v_rms = √(3k_BT/m)"),
                fm("Mittlere freie Weglänge", "λ = 1/(√2 n σ)")
            )
        ),
        chapter(
            "th_cycles", Topic.THERMO, 2, "Kreisprozesse und Maschinen",
            "Carnot, Otto, Diesel, Stirling, Brayton; Kältemaschinen, Wärmepumpen und Exergie.",
            listOf("cycles"),
            sec("Carnot-Prozess", """
                Zwei Isothermen und zwei Adiabaten. Er ist reversibel und daher der effizienteste Prozess zwischen zwei Temperaturen; sein Wirkungsgrad hängt nur von diesen ab, nicht vom Arbeitsmedium.

                Beweisidee: Gäbe es eine bessere Maschine, könnte man sie mit einer rückwärts laufenden Carnot-Maschine koppeln und netto Wärme von kalt nach warm pumpen — Widerspruch zum zweiten Hauptsatz.
            """,
                fm("Carnot", "η_C = W/Q_w = 1 − T_k/T_w"),
                fm("Kältemaschine", "ε_K = Q_k/W ≤ T_k/(T_w − T_k)"),
                fm("Wärmepumpe", "ε_W = Q_w/W ≤ T_w/(T_w − T_k)")
            ),
            sec("Technische Kreisprozesse", """
                • Otto (Benzinmotor): Verdichtung, isochore Verbrennung, Expansion, isochore Abgabe. η hängt nur vom Verdichtungsverhältnis r ab.
                • Diesel: Verbrennung isobar; höheres r möglich (Selbstzündung), daher in der Praxis effizienter.
                • Joule/Brayton (Gasturbine, Strahltriebwerk): zwei Isobaren, zwei Adiabaten; η hängt vom Druckverhältnis ab.
                • Stirling: zwei Isothermen, zwei Isochoren mit Regenerator; erreicht theoretisch Carnot.
                • Clausius-Rankine (Dampfkraftwerk): mit Phasenübergang Wasser–Dampf.
            """,
                fm("Otto", "η = 1 − r^{1−γ}"),
                fm("Diesel", "η = 1 − r^{1−γ}(ρ^γ − 1)/(γ(ρ − 1))"),
                fm("Brayton", "η = 1 − π^{(1−γ)/γ}")
            ),
            sec("Exergie und Irreversibilität", """
                Exergie ist der Anteil einer Energie, der sich bei gegebener Umgebungstemperatur T₀ maximal in Arbeit umwandeln lässt. Wärme Q bei der Temperatur T hat die Exergie Q(1 − T₀/T).

                Jede Irreversibilität (Reibung, Wärmeleitung über endliche Temperaturdifferenzen, Drosselung, Mischung) erzeugt Entropie S_gen und vernichtet Exergie: W_verloren = T₀ S_gen (Gouy-Stodola).
            """,
                fm("Exergie der Wärme", "E_x = Q (1 − T₀/T)"),
                fm("Gouy-Stodola", "W_verloren = T₀ S_gen")
            )
        ),
        chapter(
            "th_potentials", Topic.THERMO, 2, "Entropie und thermodynamische Potentiale",
            "Legendre-Transformationen, Maxwell-Relationen und Gleichgewichtsbedingungen.",
            listOf("entropy"),
            sec("Fundamentalrelation", """
                Für ein einfaches System ist U(S, V, N) ein vollständiges thermodynamisches Potential. Aus der Fundamentalrelation folgen T, p und μ als partielle Ableitungen.

                Statistisch ist die Entropie der Logarithmus der Zahl Ω der Mikrozustände, die mit dem Makrozustand verträglich sind (Boltzmann). Damit wird der zweite Hauptsatz zu einer Aussage über Wahrscheinlichkeiten: Makrozustände mit überwältigend vielen Mikrozuständen setzen sich durch.
            """,
                fm("Fundamentalrelation", "dU = T dS − p dV + μ dN"),
                fm("Boltzmann-Entropie", "S = k_B ln Ω"),
                fm("Gibbs-Entropie", "S = −k_B Σ pᵢ ln pᵢ")
            ),
            sec("Potentiale durch Legendre-Transformation", """
                Je nach kontrollierten Variablen ist ein anderes Potential minimal im Gleichgewicht:

                • Freie Energie F = U − TS: Minimum bei festem T, V.
                • Enthalpie H = U + pV: Minimum bei festem S, p.
                • Freie Enthalpie (Gibbs-Energie) G = H − TS: Minimum bei festem T, p — die Größe der Chemie und der Phasenübergänge.
                • Großkanonisches Potential Ω = F − μN.

                Euler-Relation: Für extensive Systeme gilt U = TS − pV + μN, daraus G = μN und die Gibbs-Duhem-Gleichung.
            """,
                fm("Freie Energie", "F = U − TS,   dF = −S dT − p dV + μ dN"),
                fm("Gibbs-Energie", "G = H − TS,   dG = −S dT + V dp + μ dN"),
                fm("Gibbs-Duhem", "S dT − V dp + N dμ = 0")
            ),
            sec("Maxwell-Relationen", """
                Weil die Potentiale Zustandsfunktionen sind, vertauschen ihre gemischten zweiten Ableitungen (Satz von Schwarz). So entstehen Beziehungen zwischen schwer und leicht messbaren Größen.

                Beispiel: Die Entropieänderung bei isothermer Kompression folgt aus der thermischen Ausdehnung — die Grundlage der kalorischen Effekte (siehe Kalorik).
            """,
                fm("Maxwell (F)", "(∂S/∂V)_T = (∂p/∂T)_V"),
                fm("Maxwell (G)", "(∂S/∂p)_T = −(∂V/∂T)_p"),
                fm("Maxwell (H)", "(∂T/∂p)_S = (∂V/∂S)_p"),
                fm("Allgemeine Relation", "C_p − C_V = T V α²/κ_T")
            )
        ),
        chapter(
            "th_phase", Topic.THERMO, 3, "Phasenübergänge und kritische Phänomene",
            "Clausius-Clapeyron, reale Gase, Landau-Theorie und Universalität.",
            listOf("vdw", "clausius", "ec_landau"),
            sec("Phasengleichgewicht", """
                Zwei Phasen koexistieren, wenn ihre chemischen Potentiale gleich sind. Entlang der Koexistenzkurve p(T) folgt daraus die Clausius-Clapeyron-Gleichung. Die Gibbssche Phasenregel zählt die Freiheitsgrade: F = K − P + 2.

                Am Tripelpunkt koexistieren drei Phasen (Wasser: 273,16 K, 611,657 Pa); am kritischen Punkt endet die Koexistenzkurve Flüssigkeit–Gas.
            """,
                fm("Clausius-Clapeyron", "dp/dT = L/(T Δv)"),
                fm("Gibbssche Phasenregel", "F = K − P + 2")
            ),
            sec("Van-der-Waals-Gas", """
                Die einfachste Zustandsgleichung mit Phasenübergang. b ist das Eigenvolumen, a beschreibt die Anziehung. Unterhalb von T_c besitzen die Isothermen ein instabiles Stück mit (∂p/∂V)_T > 0; die Maxwell-Konstruktion (flächengleiche Gerade) liefert den Dampfdruck.

                In reduzierten Variablen p/p_c, V/V_c, T/T_c wird die Gleichung universell — das Gesetz der korrespondierenden Zustände.
            """,
                fm("Van der Waals", "(p + a n²/V²)(V − nb) = nRT"),
                fm("Kritischer Punkt", "T_c = 8a/(27Rb),  p_c = a/(27b²),  V_c = 3b"),
                fm("Reduzierte Form", "(π + 3/φ²)(3φ − 1) = 8τ")
            ),
            sec("Landau-Theorie", """
                Nahe eines kontinuierlichen Übergangs wird die freie Energie nach einem Ordnungsparameter η entwickelt (Magnetisierung, Polarisation). Symmetrie erlaubt nur gerade Potenzen. Der quadratische Koeffizient wechselt bei T_c das Vorzeichen.

                Ist der quartische Koeffizient negativ, braucht man einen sechsten Ordnung-Term und der Übergang wird erster Ordnung (Sprung im Ordnungsparameter, latente Wärme, Hysterese) — so bei BaTiO₃.

                Kritische Exponenten der Landau-Theorie (Molekularfeld): β = 1/2, γ = 1, δ = 3, α = 0 (Sprung). Reale Systeme in 3D weichen ab (3D-Ising: β ≈ 0,326), weil Fluktuationen wichtig werden (Ginzburg-Kriterium); die Renormierungsgruppe erklärt die Universalität.
            """,
                fm("Landau-Entwicklung", "F = F₀ + a(T − T_c)η² + bη⁴ + cη⁶ − hη"),
                fm("Ordnungsparameter", "η ∝ (T_c − T)^β,  β = 1/2 (Landau)"),
                fm("Suszeptibilität (Curie-Weiss)", "χ ∝ 1/(T − T_c),  γ = 1")
            )
        ),
        chapter(
            "th_stat", Topic.THERMO, 3, "Statistische Physik",
            "Ensembles, Zustandssumme, Quantenstatistik, Planck und Debye.",
            listOf("planck", "debye", "fermi_dirac"),
            sec("Ensembles und Zustandssumme", """
                Mikrokanonisch (E fest): alle zugänglichen Mikrozustände gleich wahrscheinlich. Kanonisch (T fest): Boltzmann-Gewichte e^{−βE}. Großkanonisch (T, μ fest): Gewichte e^{−β(E−μN)}. Im thermodynamischen Limes liefern alle dieselbe Thermodynamik.

                Die Zustandssumme Z ist die zentrale Größe: Alle thermodynamischen Größen folgen aus ihren Ableitungen. Energiefluktuationen sind mit der Wärmekapazität verknüpft (Fluktuations-Dissipations-Beziehung).
            """,
                fm("Kanonische Zustandssumme", "Z = Σᵢ e^{−βEᵢ},   β = 1/k_BT"),
                fm("Freie Energie", "F = −k_B T ln Z"),
                fm("Mittlere Energie", "⟨E⟩ = −∂ ln Z/∂β"),
                fm("Energiefluktuation", "⟨ΔE²⟩ = k_B T² C_V")
            ),
            sec("Quantenstatistik", """
                Ununterscheidbare Teilchen: Fermionen (halbzahliger Spin) unterliegen dem Pauli-Prinzip, Bosonen (ganzzahliger Spin) nicht. Daraus folgen die mittleren Besetzungszahlen.

                • Elektronen im Metall: entartetes Fermi-Gas, E_F ≈ einige eV ≫ k_BT, daher trägt nur ein Bruchteil ~ k_BT/E_F zur Wärmekapazität bei.
                • Photonen (μ = 0): Planck-Strahlung.
                • Massive Bosonen: Bose-Einstein-Kondensation unterhalb von T_c (1995 an Rubidium realisiert).
            """,
                fm("Fermi-Dirac", "⟨n⟩ = 1/(e^{(E−μ)/k_BT} + 1)"),
                fm("Bose-Einstein", "⟨n⟩ = 1/(e^{(E−μ)/k_BT} − 1)"),
                fm("BEC-Temperatur", "T_c = (2πħ²/mk_B) (n/ζ(3/2))^{2/3}")
            ),
            sec("Planck und Debye", """
                Planck quantisierte 1900 die Energie der Strahlungsmoden in Vielfachen von hν und löste damit die Ultraviolett-Katastrophe der Rayleigh-Jeans-Formel. Integration über alle Frequenzen ergibt das Stefan-Boltzmann-Gesetz.

                Debye behandelte die Gitterschwingungen (Phononen) als Bosonen mit linearer Dispersion bis zu einer Grenzfrequenz ω_D. Ergebnis: Dulong-Petit bei hohen T, T³-Gesetz bei tiefen T.
            """,
                fm("Planck (Frequenz)", "u(ν, T) = (8πhν³/c³) · 1/(e^{hν/k_BT} − 1)"),
                fm("Stefan-Boltzmann", "M = σT⁴,   σ = 2π⁵k_B⁴/(15h³c²)"),
                fm("Wien", "λ_max T = 2,898 mm K"),
                fm("Debye T³", "C_V ≈ (12π⁴/5) N k_B (T/Θ_D)³")
            )
        ),
        chapter(
            "th_transport", Topic.THERMO, 2, "Wärmetransport",
            "Leitung, Konvektion und Strahlung; die Wärmeleitungsgleichung.",
            listOf("heat_rod", "planck"),
            sec("Wärmeleitung", """
                Fouriersches Gesetz: Der Wärmestrom ist proportional zum Temperaturgradienten. Zusammen mit der Energieerhaltung folgt die Wärmeleitungsgleichung, eine parabolische partielle Differentialgleichung.

                Die Temperaturleitfähigkeit a = λ/(ρc) bestimmt, wie schnell sich Temperaturstörungen ausbreiten: Eine Störung erreicht nach der Zeit t etwa die Tiefe √(at). Stationär (∂T/∂t = 0) bleibt die Laplace-Gleichung.

                Analogie: Wärmewiderstand R_th = d/(λA) verhält sich wie ein elektrischer Widerstand; Schichten addieren sich in Reihe.
            """,
                fm("Fourier-Gesetz", "q = −λ ∇T"),
                fm("Wärmeleitungsgleichung", "∂T/∂t = a ∇²T,   a = λ/(ρc)"),
                fm("Diffusionslänge", "ℓ ≈ √(4at)"),
                fm("Wärmewiderstand", "R_th = d/(λA),   Q̇ = ΔT/R_th")
            ),
            sec("Konvektion und Strahlung", """
                Konvektion: Wärme wird mit strömender Materie transportiert; Newtons Abkühlungsgesetz fasst sie über den Wärmeübergangskoeffizienten h zusammen. Dimensionslose Kennzahlen (Nusselt, Reynolds, Prandtl, Rayleigh) ordnen die Strömungsregime.

                Strahlung: Jeder Körper strahlt nach Stefan-Boltzmann; reale Körper mit Emissionsgrad ε. Nach Kirchhoff ist ε(λ) = α(λ) — gute Absorber sind gute Strahler.
            """,
                fm("Newton-Abkühlung", "Q̇ = h A (T − T_∞)"),
                fm("Strahlungsaustausch", "Q̇ = εσA(T₁⁴ − T₂⁴)"),
                fm("Rayleigh-Zahl", "Ra = gβΔT L³/(νa)")
            )
        )
    )
}

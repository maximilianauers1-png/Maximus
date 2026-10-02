package app.maximus.lab.domain

internal object CompendiumMechanics {
    private val T = Topic.MECHANICS

    val chapters: List<Chapter> =
        course(
            "Theoretische Mechanik",
            chapter(
                "me_newton", T, 1, "Newtonsche Mechanik und Zentralkräfte",
                "Bewegungsgleichungen, Erhaltungssätze, Zweikörperproblem, effektives Potential und die Keplerschen Gesetze.",
                listOf("mech_projectile", "mech_kepler"),
                sec("Newtonsche Axiome und Erhaltungssätze", """
                    In einem Inertialsystem gilt ṗ = F. Für ein System von N Teilchen mit inneren Kräften, die actio = reactio erfüllen und (starke Form) entlang der Verbindungslinie wirken, folgen:

                    • Impulssatz: Ṗ = F_ext — ohne äußere Kräfte bewegt sich der Schwerpunkt geradlinig gleichförmig.
                    • Drehimpulssatz: L̇ = M_ext.
                    • Energiesatz: Ist F = −∇V (konservativ), so ist E = T + V erhalten.

                    > Erhaltungssätze sind Folgen von Symmetrien (Noether): Translation → Impuls, Rotation → Drehimpuls, Zeittranslation → Energie.
                """,
                    fm("Newton II", "ṗ = F,   p = m v"),
                    fm("Drehimpuls", "L = r × p,   L̇ = r × F = M"),
                    fm("Arbeit und Energie", "W = ∫ F · dr = ΔT"),
                    fm("Virialsatz", "2⟨T⟩ = ⟨r · ∇V⟩,   V ∝ rᵏ ⇒ 2⟨T⟩ = k⟨V⟩")
                ),
                sec("Zweikörperproblem und effektives Potential", """
                    Zwei Körper mit Zentralkraft separieren in Schwerpunkt- und Relativbewegung mit reduzierter Masse μ. Wegen L = const verläuft die Bahn in einer Ebene; mit Polarkoordinaten bleibt ein eindimensionales Problem im effektiven Potential, in dem die Zentrifugalbarriere L²/(2μr²) auftritt.

                    Die Binet-Gleichung (u = 1/r als Funktion von φ) liefert direkt die Bahnform; für V = −k/r sind das Kegelschnitte. Satz von Bertrand: nur das Kepler- und das harmonische Potential haben für alle gebundenen Anfangsbedingungen geschlossene Bahnen.
                """,
                    fm("Reduzierte Masse", "μ = m₁m₂/(m₁ + m₂)"),
                    fm("Effektives Potential", "V_eff(r) = V(r) + L²/(2μr²)"),
                    fm("Radiale Energie", "E = ½μṙ² + V_eff(r)"),
                    fm("Binet-Gleichung", "u'' + u = −μ/(L²u²) · F(1/u),   u = 1/r")
                ),
                sec("Kepler-Problem", """
                    Für V = −k/r (Gravitation: k = GMm) ist die Bahn ein Kegelschnitt mit Exzentrizität ε: Ellipse (E < 0), Parabel (E = 0), Hyperbel (E > 0). Der Runge-Lenz-Vektor ist eine zusätzliche Erhaltungsgröße (verborgene SO(4)-Symmetrie, die auch die l-Entartung im Wasserstoffatom erklärt).

                    • 1. Gesetz: Ellipsenbahnen mit der Sonne im Brennpunkt.
                    • 2. Gesetz: Flächensatz, Ȧ = L/(2μ) = const.
                    • 3. Gesetz: T² ∝ a³.
                """,
                    fm("Bahngleichung", "r(φ) = p/(1 + ε cos φ),   p = L²/(μk)"),
                    fm("Exzentrizität", "ε = √(1 + 2EL²/(μk²))"),
                    fm("Große Halbachse", "a = −k/(2E)"),
                    fm("Kepler III", "T² = 4π² a³/(G(M + m))"),
                    fm("Vis-viva", "v² = GM(2/r − 1/a)"),
                    fm("Runge-Lenz-Vektor", "A = p × L − μk r̂")
                ),
                sec("Beschleunigte Bezugssysteme", """
                    In einem mit ω rotierenden System treten Scheinkräfte auf: Zentrifugal-, Coriolis- und Euler-Kraft. Beispiele: Foucault-Pendel (Präzession mit ω sin φ_geo), Ablenkung von Luftmassen (Zyklonen), Ostabweichung beim freien Fall.
                """,
                    fm("Zeitableitung im rotierenden System", "(dA/dt)_I = (dA/dt)_R + ω × A"),
                    fm("Scheinkräfte", "F' = F − m ω × (ω × r) − 2m ω × v' − m ω̇ × r"),
                    fm("Foucault-Pendel", "Ω = ω_E sin φ")
                )
            ),
            chapter(
                "me_lagrange", T, 2, "Lagrange-Formalismus",
                "Zwangsbedingungen, d'Alembert-Prinzip, Hamiltonsches Prinzip, Lagrange-Gleichungen, Noether-Theorem.",
                listOf("pendulum"),
                sec("Zwangsbedingungen und generalisierte Koordinaten", """
                    Holonome Zwangsbedingungen f(r₁, …, r_N, t) = 0 reduzieren die 3N Koordinaten auf f = 3N − k Freiheitsgrade, beschrieben durch generalisierte Koordinaten q₁ … q_f. Skleronom: zeitunabhängig, rheonom: explizit zeitabhängig. Nichtholonome Bedingungen (z. B. rollende Kugel, nur differentiell) lassen sich nicht so eliminieren.

                    Prinzip von d'Alembert: Zwangskräfte leisten an virtuellen Verrückungen keine Arbeit.
                """,
                    fm("d'Alembert", "Σᵢ (Fᵢ − mᵢ r̈ᵢ) · δrᵢ = 0"),
                    fm("Lagrange 1. Art", "mᵢ r̈ᵢ = Fᵢ + Σₖ λₖ ∇ᵢ fₖ")
                ),
                sec("Hamiltonsches Prinzip und Euler-Lagrange-Gleichungen", """
                    Die physikalische Bahn macht die Wirkung stationär. Aus δS = 0 folgen die Lagrange-Gleichungen 2. Art. L ist nur bis auf eine totale Zeitableitung dL' = L + dF(q, t)/dt bestimmt (Eichfreiheit). Geschwindigkeitsabhängige Potentiale sind erlaubt: das geladene Teilchen hat L = ½mv² − qφ + qv · A.
                """,
                    fm("Wirkung", "S[q] = ∫_{t₁}^{t₂} L(q, q̇, t) dt,   δS = 0"),
                    fm("Lagrange 2. Art", "d/dt (∂L/∂q̇ᵢ) − ∂L/∂qᵢ = 0"),
                    fm("Lagrange-Funktion", "L = T − V"),
                    fm("Geladenes Teilchen", "L = ½mv² − qφ + q v · A")
                ),
                sec("Symmetrien und Erhaltungsgrößen", """
                    Ist qᵢ zyklisch (∂L/∂qᵢ = 0), so ist der kanonische Impuls pᵢ = ∂L/∂q̇ᵢ erhalten. Noether-Theorem: Jede kontinuierliche Symmetrie der Wirkung liefert eine Erhaltungsgröße. Hängt L nicht explizit von t ab, ist die Energiefunktion h erhalten (bei skleronomen Bedingungen und V(q) gilt h = T + V).
                """,
                    fm("Kanonischer Impuls", "pᵢ = ∂L/∂q̇ᵢ"),
                    fm("Noether-Ladung", "Q = Σᵢ (∂L/∂q̇ᵢ) δqᵢ − F,   δL = dF/dt"),
                    fm("Energiefunktion", "h = Σᵢ q̇ᵢ ∂L/∂q̇ᵢ − L,   dh/dt = −∂L/∂t")
                ),
                sec("Beispiel: mathematisches Pendel", """
                    L = ½ml²θ̇² + mgl cos θ ergibt θ̈ = −(g/l) sin θ. Für kleine Ausschläge harmonisch mit ω₀ = √(g/l); exakt hängt die Periode über das vollständige elliptische Integral K von der Amplitude ab.
                """,
                    fm("Pendelperiode exakt", "T = 4√(l/g) K(sin(θ₀/2)),   K(k) = ∫₀^{π/2} dφ/√(1 − k² sin²φ)"),
                    fm("Amplitudenkorrektur", "T ≈ 2π√(l/g)(1 + θ₀²/16 + 11θ₀⁴/3072)")
                )
            ),
            chapter(
                "me_hamilton", T, 2, "Hamilton-Formalismus",
                "Legendre-Transformation, kanonische Gleichungen, Poisson-Klammern, kanonische Transformationen, Liouville, Hamilton-Jacobi.",
                emptyList(),
                sec("Kanonische Gleichungen", """
                    Die Legendre-Transformation von L bezüglich q̇ liefert die Hamilton-Funktion H(q, p, t) auf dem 2f-dimensionalen Phasenraum. Die Bewegung folgt aus 2f Gleichungen erster Ordnung.
                """,
                    fm("Hamilton-Funktion", "H(q, p, t) = Σ pᵢq̇ᵢ − L"),
                    fm("Hamilton-Gleichungen", "q̇ᵢ = ∂H/∂pᵢ,   ṗᵢ = −∂H/∂qᵢ"),
                    fm("Energieänderung", "dH/dt = ∂H/∂t = −∂L/∂t")
                ),
                sec("Poisson-Klammern", """
                    Die Zeitentwicklung jeder Observablen wird durch die Poisson-Klammer mit H erzeugt. f ist Erhaltungsgröße ⇔ {f, H} + ∂f/∂t = 0. Die Klammer ist antisymmetrisch, bilinear, erfüllt Leibniz- und Jacobi-Identität — Dirac: Quantisierung ersetzt { , } durch [ , ]/(iħ). Satz von Poisson: Die Klammer zweier Erhaltungsgrößen ist erhalten.
                """,
                    fm("Poisson-Klammer", "{f, g} = Σᵢ (∂f/∂qᵢ ∂g/∂pᵢ − ∂f/∂pᵢ ∂g/∂qᵢ)"),
                    fm("Zeitentwicklung", "df/dt = {f, H} + ∂f/∂t"),
                    fm("Fundamentale Klammern", "{qᵢ, pⱼ} = δᵢⱼ,   {qᵢ, qⱼ} = {pᵢ, pⱼ} = 0"),
                    fm("Drehimpulsalgebra", "{Lᵢ, Lⱼ} = εᵢⱼₖ Lₖ")
                ),
                sec("Kanonische Transformationen und Liouville", """
                    Transformationen (q, p) → (Q, P), die die Form der Hamilton-Gleichungen erhalten, heißen kanonisch; sie erhalten die Poisson-Klammern, ihre Jacobi-Matrix ist symplektisch. Sie werden von Erzeugenden F₁(q, Q, t) … F₄(p, P, t) erzeugt.

                    Satz von Liouville: Der Hamiltonsche Fluss erhält das Phasenraumvolumen; die Dichte ist entlang Trajektorien konstant — Grundlage der statistischen Mechanik und Grund, warum symplektische Integratoren (Verlet) keine Energiedrift zeigen.
                """,
                    fm("Symplektische Bedingung", "Mᵀ J M = J,   J = [[0, 1], [−1, 0]]"),
                    fm("Erzeugende F₂", "pᵢ = ∂F₂/∂qᵢ,   Qᵢ = ∂F₂/∂Pᵢ,   K = H + ∂F₂/∂t"),
                    fm("Liouville-Gleichung", "∂ρ/∂t + {ρ, H} = 0")
                ),
                sec("Hamilton-Jacobi-Theorie und Wirkungs-Winkel-Variablen", """
                    Sucht man eine Transformation mit K = 0, erfüllt die Erzeugende S(q, P, t) (Hamiltons Wirkungsfunktion) die Hamilton-Jacobi-Gleichung. Für separable Systeme führt das auf Wirkungsvariablen J = ∮ p dq; die Frequenzen sind ω = ∂H/∂J. Die Wirkung ist eine adiabatische Invariante — historisch die Bohr-Sommerfeld-Quantisierung J = nh, heute die WKB-Näherung.
                """,
                    fm("Hamilton-Jacobi-Gleichung", "∂S/∂t + H(q, ∂S/∂q, t) = 0"),
                    fm("Wirkungsvariable", "J = ∮ p dq,   ω = ∂H/∂J"),
                    fm("Harmonischer Oszillator", "H = ωJ/(2π) bzw. H = ω I mit I = J/2π")
                )
            ),
            chapter(
                "me_rigid", T, 2, "Starrer Körper",
                "Trägheitstensor, Hauptachsen, Steiner, Euler-Gleichungen, Kreisel und Präzession.",
                emptyList(),
                sec("Trägheitstensor", """
                    Kinematik: Jede Bewegung eines starren Körpers ist Translation eines Bezugspunktes plus Rotation, v = V + ω × r. Der Drehimpuls ist im Allgemeinen nicht parallel zu ω; der Zusammenhang ist der symmetrische, positiv (semi)definite Trägheitstensor. In seinem Eigensystem (Hauptachsen) ist er diagonal.
                """,
                    fm("Trägheitstensor", "Iᵢⱼ = ∫ ρ(r)(r²δᵢⱼ − xᵢxⱼ) d³r"),
                    fm("Drehimpuls", "L = I ω"),
                    fm("Rotationsenergie", "T_rot = ½ ωᵀ I ω = ½ Σ Iₖ ωₖ²"),
                    fm("Steiner", "I_A = I_S + M(a²·1 − a aᵀ),   skalar I = I_S + M d²")
                ),
                sec("Standard-Trägheitsmomente", """
                    Für homogene Körper um Symmetrieachsen durch den Schwerpunkt:
                """,
                    fm("Vollzylinder (Achse)", "I = ½MR²"),
                    fm("Vollkugel", "I = ⅖MR²"),
                    fm("Hohlkugel (dünn)", "I = ⅔MR²"),
                    fm("Stab um Mitte", "I = (1/12)ML²")
                ),
                sec("Euler-Gleichungen und Kreisel", """
                    Im körperfesten Hauptachsensystem gelten die Euler-Gleichungen. Kräftefreier symmetrischer Kreisel (I₁ = I₂): ω präzediert im Körpersystem mit Ω = ω₃(I₃ − I₁)/I₁ um die Figurenachse (Erde: Chandler-Wobble). Satz vom Zwischenachsen (Tennisschläger-Effekt): Rotation um die Achse mit mittlerem Trägheitsmoment ist instabil.

                    Schwerer Kreisel: Unter dem Drehmoment der Gewichtskraft präzediert ein schneller Kreisel langsam mit Ω_P = mgl/(I₃ω₃), überlagert von Nutation.
                """,
                    fm("Euler-Gleichungen", "I₁ω̇₁ − (I₂ − I₃)ω₂ω₃ = M₁ (zyklisch)"),
                    fm("Präzession (schneller Kreisel)", "Ω_P = mgl/(I₃ ω₃)"),
                    fm("Freie Präzession", "Ω = ω₃ (I₃ − I₁)/I₁")
                )
            ),
            chapter(
                "me_oscillation", T, 2, "Schwingungen",
                "Gedämpfter und getriebener Oszillator, Resonanz, kleine Schwingungen, Normalmoden, parametrische Resonanz.",
                listOf("mech_oscillator", "mech_coupled"),
                sec("Gedämpfter harmonischer Oszillator", """
                    ẍ + 2γẋ + ω₀²x = 0 mit dem Ansatz e^{λt}: λ = −γ ± √(γ² − ω₀²). Schwingfall (γ < ω₀), aperiodischer Grenzfall (γ = ω₀, schnellste Rückkehr ohne Überschwingen) und Kriechfall (γ > ω₀). Güte Q = ω₀/(2γ): Zahl der Schwingungen bis zum Abfall der Energie um e^{−2π}.
                """,
                    fm("Bewegungsgleichung", "ẍ + 2γẋ + ω₀²x = f(t)"),
                    fm("Gedämpfte Frequenz", "ω_d = √(ω₀² − γ²)"),
                    fm("Güte", "Q = ω₀/(2γ)")
                ),
                sec("Getriebener Oszillator und Resonanz", """
                    Bei Antrieb f₀cos ωt bleibt nach dem Einschwingen die partikuläre Lösung mit Amplitude A(ω) und Phasenverschiebung φ, die durch die Resonanz von 0 nach π läuft. Die Green-Funktion G(t) = Θ(t) e^{−γt} sin(ω_d t)/ω_d liefert die Antwort auf beliebige Kräfte als Faltung — Kausalität ⇒ Kramers-Kronig.
                """,
                    fm("Amplitude", "A(ω) = f₀/√((ω₀² − ω²)² + 4γ²ω²)"),
                    fm("Phase", "tan φ = 2γω/(ω₀² − ω²)"),
                    fm("Resonanzfrequenz", "ω_res = √(ω₀² − 2γ²)"),
                    fm("Halbwertsbreite", "Δω ≈ 2γ = ω₀/Q")
                ),
                sec("Kleine Schwingungen und Normalmoden", """
                    Um ein stabiles Gleichgewicht q₀ entwickelt man T = ½ q̇ᵀ M q̇ und V ≈ ½ qᵀ K q. Der Ansatz q = a e^{iωt} führt auf das verallgemeinerte Eigenwertproblem; die Eigenvektoren (Normalmoden) entkoppeln das System. Zwei gleiche Pendel mit Kopplungsfeder: ω₁ = ω₀ (gleichphasig), ω₂ = √(ω₀² + 2κ) (gegenphasig); bei schwacher Kopplung Schwebung mit ω₂ − ω₁.
                """,
                    fm("Säkulargleichung", "det(K − ω²M) = 0"),
                    fm("Gekoppelte Pendel", "ω₁ = ω₀,   ω₂ = √(ω₀² + 2k/m)"),
                    fm("Lineare Kette", "ω(k) = 2√(K/m) |sin(ka/2)|")
                ),
                sec("Nichtlineare und parametrische Schwingungen", """
                    Duffing-Oszillator: amplitudenabhängige Frequenz, Hysterese der Resonanzkurve, für starke Antriebe Chaos. Parametrische Resonanz (Schaukel, Mathieu-Gleichung): Modulation von ω₀² mit 2ω₀ führt zu exponentiellem Wachstum. Kapitza-Pendel: schnelle Vertikalvibration stabilisiert die obere Lage.
                """,
                    fm("Mathieu-Gleichung", "ẍ + ω₀²(1 + h cos(2ω₀t)) x = 0"),
                    fm("Duffing", "ẍ + 2γẋ + ω₀²x + βx³ = f₀ cos ωt")
                )
            ),
            chapter(
                "me_continuum", T, 3, "Kontinuumsmechanik",
                "Spannung und Verzerrung, Elastizität, Euler- und Navier-Stokes-Gleichungen, Bernoulli, Reynolds-Zahl.",
                emptyList(),
                sec("Elastizitätstheorie", """
                    Verschiebungsfeld u(x), Verzerrungstensor εᵢⱼ (symmetrischer Teil des Verschiebungsgradienten), Spannungstensor σᵢⱼ (Kraft pro Fläche, symmetrisch wegen Drehimpulserhaltung). Hookesches Gesetz für isotrope Körper mit den Lamé-Konstanten. Ingenieursgrößen: Elastizitätsmodul E, Poisson-Zahl ν, Schubmodul G.
                """,
                    fm("Verzerrungstensor", "εᵢⱼ = ½(∂ᵢuⱼ + ∂ⱼuᵢ)"),
                    fm("Hooke (isotrop)", "σᵢⱼ = λ εₖₖ δᵢⱼ + 2μ εᵢⱼ"),
                    fm("Moduln", "G = μ = E/(2(1 + ν)),   K = E/(3(1 − 2ν))"),
                    fm("Bewegungsgleichung", "ρ ü = ∇ · σ + f"),
                    fm("Schallgeschwindigkeiten", "c_L = √((λ + 2μ)/ρ),   c_T = √(μ/ρ)")
                ),
                sec("Ideale Flüssigkeiten", """
                    Kontinuitätsgleichung (Masseerhaltung) und Euler-Gleichung (Newton für ein Fluidelement, mit konvektiver Ableitung). Entlang Stromlinien einer stationären, reibungsfreien, inkompressiblen Strömung gilt die Bernoulli-Gleichung. Wirbelfreie Strömung: v = ∇φ mit Δφ = 0 (Potentialströmung). Kelvin: Zirkulation bleibt erhalten.
                """,
                    fm("Kontinuitätsgleichung", "∂ρ/∂t + ∇ · (ρv) = 0"),
                    fm("Euler-Gleichung", "ρ(∂v/∂t + (v · ∇)v) = −∇p + f"),
                    fm("Bernoulli", "p + ½ρv² + ρgz = const"),
                    fm("Torricelli", "v = √(2gh)")
                ),
                sec("Viskose Flüssigkeiten", """
                    Newtonsche Flüssigkeit: Schubspannung proportional zum Geschwindigkeitsgradienten. Die Navier-Stokes-Gleichungen enthalten den Reibungsterm ηΔv. Die Reynolds-Zahl misst das Verhältnis von Trägheits- zu Reibungskräften; Re ≲ 2300 im Rohr laminar. Exakte Lösungen: Hagen-Poiseuille (Rohr), Couette (Scherspalt), Stokes-Reibung einer Kugel.
                """,
                    fm("Navier-Stokes (inkompressibel)", "ρ(∂ₜv + (v·∇)v) = −∇p + ηΔv + f,   ∇·v = 0"),
                    fm("Reynolds-Zahl", "Re = ρvL/η"),
                    fm("Hagen-Poiseuille", "V̇ = πR⁴Δp/(8ηL)"),
                    fm("Stokes-Reibung", "F = 6πηRv")
                )
            )
        ) + course(
            "Relativitätstheorie",
            chapter(
                "me_sr", T, 2, "Spezielle Relativitätstheorie",
                "Lorentz-Transformation, Zeitdilatation, Längenkontraktion, Geschwindigkeitsaddition, Viererimpuls, Doppler.",
                listOf("mech_sr"),
                sec("Postulate und Lorentz-Transformation", """
                    Einstein: (1) Die Naturgesetze sind in allen Inertialsystemen gleich. (2) Die Lichtgeschwindigkeit ist in allen Inertialsystemen gleich c. Daraus folgt, dass das Intervall s² = c²t² − x² invariant ist; die linearen Abbildungen, die es erhalten, sind die Lorentz-Transformationen. Ein Boost ist eine hyperbolische Drehung mit der Rapidität η; Rapiditäten addieren sich.
                """,
                    fm("Lorentz-Boost", "t' = γ(t − vx/c²),   x' = γ(x − vt)"),
                    fm("Lorentz-Faktor", "γ = 1/√(1 − β²),   β = v/c"),
                    fm("Rapidität", "β = tanh η,   γ = cosh η"),
                    fm("Geschwindigkeitsaddition", "u = (u' + v)/(1 + u'v/c²)")
                ),
                sec("Kinematische Effekte", """
                    • Relativität der Gleichzeitigkeit: Ereignisse, die in S gleichzeitig sind, sind es in S' nicht.
                    • Zeitdilatation: Bewegte Uhren gehen langsamer (Myonen aus der Höhenstrahlung erreichen den Boden).
                    • Längenkontraktion in Bewegungsrichtung.
                    • Zwillingsparadoxon: Die Eigenzeit ist entlang der unbeschleunigten Weltlinie maximal.
                """,
                    fm("Eigenzeit", "dτ = dt/γ,   τ = ∫ √(1 − v²/c²) dt"),
                    fm("Längenkontraktion", "L = L₀/γ"),
                    fm("Doppler longitudinal", "f_obs = f √((1 + β)/(1 − β))  (Annäherung)"),
                    fm("Transversaler Doppler", "f_obs = f/γ")
                ),
                sec("Relativistische Dynamik", """
                    Vierergeschwindigkeit u^μ = γ(c, v), Viererimpuls p^μ = m u^μ = (E/c, p). Die invariante Länge p² = m²c² ergibt die Energie-Impuls-Beziehung. Die Ruheenergie mc² und die kinetische Energie (γ − 1)mc² sind getrennt. Bei Stößen ist der Viererimpuls erhalten, die Summe der Ruhemassen nicht (Kernspaltung, Paarerzeugung).
                """,
                    fm("Energie-Impuls-Beziehung", "E² = (pc)² + (mc²)²"),
                    fm("Energie und Impuls", "E = γmc²,   p = γmv,   v = pc²/E"),
                    fm("Kraft", "F = dp/dt,   F^μ = dp^μ/dτ"),
                    fm("Schwellenenergie (ruhendes Target)", "E_th = ((Σm_f)² − m₁² − m₂²)c²/(2m₂)")
                ),
                sec("Kovariante Elektrodynamik", """
                    Mit dem Viererpotential A^μ = (φ/c, A) wird der Feldstärketensor F^μν = ∂^μA^ν − ∂^νA^μ gebildet; E und B sind Komponenten eines Objekts und mischen unter Boosts. Die Maxwell-Gleichungen lauten ∂_μF^μν = μ₀j^ν und die Bianchi-Identität. Invarianten: E² − c²B² und E · B.
                """,
                    fm("Inhomogene Maxwell-Gl.", "∂_μ F^{μν} = μ₀ j^ν"),
                    fm("Feldinvarianten", "F_μν F^μν = 2(B² − E²/c²),   F̃F ∝ E · B"),
                    fm("Lorentz-Kraft kovariant", "dp^μ/dτ = q F^μν u_ν")
                )
            ),
            chapter(
                "me_gr", T, 3, "Allgemeine Relativitätstheorie",
                "Äquivalenzprinzip, Metrik und Geodäten, Krümmung, Einstein-Gleichungen, Schwarzschild, klassische Tests, Gravitationswellen, Kosmologie.",
                listOf("mech_gr"),
                sec("Äquivalenzprinzip und Geodäten", """
                    Schwere und träge Masse sind gleich; lokal lässt sich Gravitation durch Wahl eines frei fallenden Systems wegtransformieren. Gravitation ist daher Geometrie: Die Raumzeit ist eine Lorentz-Mannigfaltigkeit mit Metrik g_μν, frei fallende Teilchen folgen Geodäten (Extremale der Eigenzeit). Im Newtonschen Grenzfall ist g₀₀ ≈ 1 + 2Φ/c².
                """,
                    fm("Linienelement", "ds² = g_μν dx^μ dx^ν"),
                    fm("Christoffel-Symbole", "Γ^λ_μν = ½ g^{λσ}(∂_μ g_σν + ∂_ν g_σμ − ∂_σ g_μν)"),
                    fm("Geodätengleichung", "d²x^λ/dτ² + Γ^λ_μν (dx^μ/dτ)(dx^ν/dτ) = 0"),
                    fm("Newtonscher Grenzfall", "g₀₀ ≈ 1 + 2Φ/c²")
                ),
                sec("Krümmung und Einstein-Gleichungen", """
                    Krümmung zeigt sich, wenn kovariante Ableitungen nicht vertauschen (Riemann-Tensor), physikalisch als Gezeitenkräfte (Geodätenabweichung). Kontraktion liefert Ricci-Tensor und -Skalar. Die Einstein-Gleichungen koppeln die Krümmung an den Energie-Impuls-Tensor; die kontrahierte Bianchi-Identität garantiert ∇_μT^μν = 0.
                """,
                    fm("Riemann-Tensor", "[∇_μ, ∇_ν] V^ρ = R^ρ_σμν V^σ"),
                    fm("Einstein-Gleichungen", "G_μν + Λg_μν = (8πG/c⁴) T_μν,   G_μν = R_μν − ½Rg_μν"),
                    fm("Geodätenabweichung", "D²ξ^μ/dτ² = −R^μ_νρσ u^ν ξ^ρ u^σ")
                ),
                sec("Schwarzschild-Lösung", """
                    Die eindeutige kugelsymmetrische Vakuumlösung (Birkhoff). Bei r_s liegt der Ereignishorizont eines Schwarzen Lochs (Koordinatensingularität, die echte Krümmungssingularität sitzt bei r = 0). Wichtige Radien: Photonensphäre 1,5 r_s, innerster stabiler Kreisorbit (ISCO) 3 r_s.

                    Klassische Tests: Periheldrehung des Merkur (43″/Jahrhundert), Lichtablenkung an der Sonne (1,75″, doppelt so groß wie newtonsch), gravitative Rotverschiebung (Pound-Rebka), Shapiro-Verzögerung. GPS korrigiert +45 μs/Tag (ART) − 7 μs/Tag (SRT).
                """,
                    fm("Schwarzschild-Metrik", "ds² = (1 − r_s/r)c²dt² − dr²/(1 − r_s/r) − r²dΩ²"),
                    fm("Schwarzschild-Radius", "r_s = 2GM/c²"),
                    fm("Gravitative Zeitdilatation", "dτ/dt = √(1 − r_s/r)"),
                    fm("Periheldrehung", "Δφ = 6πGM/(c²a(1 − ε²)) pro Umlauf"),
                    fm("Lichtablenkung", "δ = 4GM/(c²b)")
                ),
                sec("Gravitationswellen und Kosmologie", """
                    Linearisierte Einstein-Gleichungen um flache Raumzeit (g = η + h) liefern in Lorenz-Eichung eine Wellengleichung: transversale Wellen mit Lichtgeschwindigkeit und zwei Polarisationen (+, ×). Abgestrahlt wird Quadrupolstrahlung; der Hulse-Taylor-Pulsar und LIGO (2015) bestätigen die Vorhersage.

                    Kosmologie: Die homogene, isotrope FLRW-Metrik mit Skalenfaktor a(t) und die Friedmann-Gleichungen beschreiben die Expansion; Hubble-Gesetz v = H₀d.
                """,
                    fm("Wellengleichung", "□ h̄_μν = −(16πG/c⁴) T_μν"),
                    fm("Quadrupolformel", "P = (G/5c⁵) ⟨d³Q_ij/dt³ d³Q^ij/dt³⟩"),
                    fm("Friedmann-Gleichung", "(ȧ/a)² = 8πGρ/3 − kc²/a² + Λc²/3"),
                    fm("Hubble-Gesetz", "v = H₀ d")
                )
            )
        )
}

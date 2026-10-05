package app.maximus.lab.domain

/** Polymer physics from ideal chains to functional (electroactive) polymers, in the spirit of Rubinstein & Colby, Doi, de Gennes, Strobl. */
internal object CompendiumPolymer {
    private val T = Topic.POLYMER

    val chapters: List<Chapter> = course(
        "Polymerphysik I: Ketten und Lösungen",
        chapter(
            "po_chain", T, 1, "Ideale Ketten",
            "Molmassen, Random Walk, Kuhn-Länge, Gyrationsradius, Gauß-Verteilung, entropische Feder, wurmartige Kette.",
            emptyList(),
            sec("Molmassen und Verteilungen", """
                Polymere sind Makromoleküle aus N Monomeren. Synthetische Proben sind polydispers; man unterscheidet das Zahlenmittel M_n (jede Kette zählt gleich) und das Gewichtsmittel M_w (schwere Ketten zählen mehr). Ihr Verhältnis ist die Dispersität Đ = M_w/M_n ≥ 1. Messung: GPC/SEC (relativ), Lichtstreuung (M_w absolut), Osmometrie (M_n).
            """,
                fm("Zahlenmittel", "M_n = Σ nᵢMᵢ / Σ nᵢ"),
                fm("Gewichtsmittel", "M_w = Σ nᵢMᵢ² / Σ nᵢMᵢ"),
                fm("Dispersität", "Đ = M_w/M_n ≥ 1")
            ),
            sec("Die Kette als Zufallsweg", """
                Im einfachsten Modell (frei verbundene Kette, FJC) sind N Segmente der Länge b unabhängig orientiert. Der End-zu-End-Vektor ist dann eine Summe unabhängiger Zufallsvektoren: Sein Mittel verschwindet, sein mittleres Quadrat wächst linear mit N. Reale Ketten mit festen Bindungswinkeln und gehinderter Rotation verhalten sich auf großen Skalen genauso, wenn man die Segmentlänge durch die Kuhn-Länge ersetzt.

                > Größe ∝ √N: Eine Kette mit 10⁴ Monomeren ist nur etwa 100 Kuhn-Längen groß, aber ausgestreckt 10⁴ lang.
            """,
                fm("Mittleres Quadrat", "⟨R²⟩ = N b²  (FJC)"),
                fm("Charakteristisches Verhältnis", "⟨R²⟩ = C_∞ n ℓ²"),
                fm("Kuhn-Länge", "b = ⟨R²⟩/R_max,  N_K = R_max²/⟨R²⟩"),
                fm("Gyrationsradius (ideal)", "R_g² = ⟨R²⟩/6")
            ),
            sec("Gauß-Verteilung und entropische Elastizität", """
                Für große N ist die Verteilung des End-zu-End-Vektors gaußisch (zentraler Grenzwertsatz). Die Entropie einer Kette mit vorgegebenem Abstand R ist S(R) = k_B ln P(R); Strecken verringert die Zahl der Konformationen. Daraus folgt eine rein entropische Rückstellkraft, linear in R und proportional zur Temperatur — die Grundlage der Gummielastizität.
            """,
                fm("Gauß-Verteilung", "P(R) = (3/(2πNb²))^{3/2} exp(−3R²/(2Nb²))"),
                fm("Freie Energie", "F(R) = 3k_BT R²/(2Nb²) + const"),
                fm("Entropische Feder", "f = 3k_BT R/(Nb²)")
            ),
            sec("Steife Ketten: das Wurmkettenmodell", """
                Für steife Polymere (DNA, Aktin, konjugierte Polymere) beschreibt die Persistenzlänge ℓ_p die Länge, über die die Kettenrichtung korreliert bleibt. Kurze Ketten (L ≪ ℓ_p) sind stäbchenförmig, lange (L ≫ ℓ_p) wieder gaußisch mit Kuhn-Länge b = 2ℓ_p. Das Kraft-Dehnungs-Verhalten (Marko-Siggia) wird in Einzelmolekül-Experimenten mit optischen Pinzetten gemessen.
            """,
                fm("Tangentenkorrelation", "⟨t(s)·t(0)⟩ = e^{−s/ℓ_p}"),
                fm("Kratky-Porod", "⟨R²⟩ = 2ℓ_pL − 2ℓ_p²(1 − e^{−L/ℓ_p})"),
                fm("Marko-Siggia", "f ℓ_p/(k_BT) = x/L + 1/(4(1 − x/L)²) − 1/4")
            )
        ),
        chapter(
            "po_real", T, 2, "Reale Ketten und Skalengesetze",
            "Ausgeschlossenes Volumen, Flory-Theorie, Lösungsmittelqualität, Theta-Zustand, Blobs, Kollaps.",
            emptyList(),
            sec("Ausgeschlossenes Volumen und Flory-Argument", """
                Monomere können sich nicht durchdringen; in einem guten Lösungsmittel stoßen sie sich effektiv ab (ausgeschlossenes Volumen v > 0). Flory balanciert die Abstoßungsenergie (∝ vN²/R³) gegen die entropische Elastizität (∝ R²/(Nb²)) und erhält R ∝ N^{3/5}. Renormierungsgruppen-Rechnungen geben ν ≈ 0,588 — Flory liegt erstaunlich nahe, weil sich zwei Fehler teilweise kompensieren.
            """,
                fm("Flory-Energie", "F/k_BT ≈ R²/(Nb²) + v N²/R³"),
                fm("Flory-Exponent", "R ≈ b N^ν,  ν = 3/(d + 2) = 3/5 (d = 3)"),
                fm("Exakter Exponent", "ν ≈ 0,588 (3D),  ν = 3/4 (2D)")
            ),
            sec("Lösungsmittelqualität", """
                Das ausgeschlossene Volumen hängt von der Temperatur ab: v ≈ b³(1 − θ/T). Bei der Theta-Temperatur heben sich Abstoßung und Anziehung auf (v = 0), die Kette ist ideal. Im schlechten Lösungsmittel (T < θ) kollabiert sie zum Globulus mit R ∝ N^{1/3}.
            """,
                fm("Ausgeschlossenes Volumen", "v ≈ b³(1 − θ/T) = b³(1 − 2χ)"),
                fm("Regime", "gut: ν ≈ 0,588 · θ: ν = 1/2 · schlecht: ν = 1/3")
            ),
            sec("Blobs", """
                Auf kleinen Skalen dominiert die Kettenstatistik, auf großen die Wechselwirkung. Die thermische Blob-Größe ξ_T ist die Skala, auf der die Wechselwirkungsenergie k_BT erreicht: Innerhalb eines Blobs ist die Kette ideal, die Blobs selbst bilden eine selbstvermeidende Kette. Blob-Bilder erklären auch gestreckte Ketten (Pincus-Blobs) und halbverdünnte Lösungen.
            """,
                fm("Thermischer Blob", "ξ_T ≈ b⁴/|v|,  g_T ≈ b⁶/v²"),
                fm("Kette aus Blobs", "R ≈ ξ_T (N/g_T)^{3/5}")
            )
        ),
        chapter(
            "po_solutions", T, 2, "Polymerlösungen und Mischungen",
            "Flory-Huggins-Theorie, χ-Parameter, Phasendiagramm, osmotischer Druck, Überlappung, Blockcopolymere.",
            emptyList(),
            sec("Flory-Huggins-Theorie", """
                Gittermodell: Volumenbruch φ des Polymers, Kettenlänge N, Wechselwirkungsparameter χ. Die Mischungsentropie eines Polymers ist um den Faktor 1/N kleiner als die kleiner Moleküle, weil die Monomere einer Kette nicht unabhängig platziert werden. Deshalb entmischen Polymere schon bei kleinem positivem χ.
            """,
                fm("Freie Mischungsenergie", "ΔF_mix/(k_BT) = (φ/N) ln φ + (1 − φ) ln(1 − φ) + χ φ(1 − φ)  (pro Gitterplatz)"),
                fm("Kritischer Punkt (Lösung)", "χ_c = ½ (1 + 1/√N)²,  φ_c = 1/(1 + √N)"),
                fm("Spinodale", "∂²ΔF/∂φ² = 0"),
                fm("Mischung zweier Polymere", "χ_c = 2/N  (N_A = N_B = N)")
            ),
            sec("Osmotischer Druck und Konzentrationsregime", """
                In verdünnter Lösung folgt der osmotische Druck einer Virialentwicklung, deren zweiter Koeffizient am Theta-Punkt verschwindet. Oberhalb der Überlappungskonzentration c* durchdringen sich die Ketten (halbverdünnte Lösung): Eigenschaften hängen dann nur noch von der Konzentration ab, nicht von N (de Gennes).
            """,
                fm("Virialentwicklung", "Π/(c k_BT) = 1/N + A₂ c + …  (c in Monomeren pro Volumen)"),
                fm("Überlappung", "c* ≈ N/R³ ∝ N^{1−3ν}"),
                fm("Halbverdünnt (gut)", "Π ∝ c^{9/4},  ξ ∝ c^{−3/4}")
            ),
            sec("Blockcopolymere", """
                Sind zwei unverträgliche Blöcke kovalent verbunden, können sie nicht makroskopisch entmischen; sie bilden Mikrophasen (Lamellen, Gyroid, Zylinder, Kugeln) mit Perioden von 10–100 nm, je nach Volumenanteil f. Für symmetrische Diblöcke liegt die Ordnungs-Unordnungs-Umwandlung bei χN ≈ 10,5 (Mittelfeld). Anwendung: Nanolithografie, Membranen.
            """,
                fm("Ordnungs-Unordnungs-Übergang", "(χN)_ODT ≈ 10,5  (f = ½, Mittelfeld)"),
                fm("Periode (stark segregiert)", "D ∝ b N^{2/3} χ^{1/6}")
            )
        )
    ) + course(
        "Polymerphysik II: Netzwerke, Dynamik, Festkörper",
        chapter(
            "po_rubber", T, 2, "Gummielastizität und Netzwerke",
            "Affines Netzwerk, Schermodul aus Vernetzungsdichte, Mooney-Rivlin, Quellung, thermoelastische Inversion.",
            listOf("elastocaloric"),
            sec("Affines Netzwerkmodell", """
                Ein vernetztes Elastomer ist ein Netz entropischer Federn. Unter der Annahme, dass die Vernetzungspunkte die makroskopische Deformation mitmachen (affin), folgt die Spannung bei uniaxialer Dehnung λ. Der Schermodul ist proportional zur Dichte elastisch wirksamer Netzketten und zur Temperatur — warme Gummis sind steifer, im Gegensatz zu Metallen.
            """,
                fm("Schermodul", "G = n k_BT = ρRT/M_x"),
                fm("Uniaxial (Nominalspannung)", "σ = G (λ − 1/λ²)"),
                fm("Freie Energie", "ΔF = (G/2)(λ₁² + λ₂² + λ₃² − 3),  λ₁λ₂λ₃ = 1")
            ),
            sec("Abweichungen und Quellung", """
                Reale Gummis weichen bei mittleren Dehnungen ab (Mooney-Rivlin, Phantomnetz mit Fluktuationen der Vernetzungspunkte: G = (1 − 2/f) n k_BT für Funktionalität f) und versteifen bei großen Dehnungen durch endliche Kettenlänge. In einem Lösungsmittel quillt das Netz, bis sich Mischungs- und Elastizitätsbeitrag ausgleichen (Flory-Rehner).
            """,
                fm("Mooney-Rivlin", "σ/(λ − 1/λ²) = 2C₁ + 2C₂/λ"),
                fm("Phantomnetz", "G = (1 − 2/f) n k_BT")
            ),
            sec("Thermoelastik", """
                Bei konstanter Länge steigt die Kraft eines gedehnten Gummis mit der Temperatur (entropisch). Bei kleinen Dehnungen überwiegt die thermische Ausdehnung, und die Kraft sinkt zunächst — die thermoelastische Inversion bei etwa 10 % Dehnung. Adiabatisches Dehnen erwärmt den Gummi (Gough-Joule-Effekt), Grundlage elastokalorischer Polymere.
            """,
                fm("Energetischer Kraftanteil", "f_e/f = T d ln⟨R²⟩₀/dT")
            )
        ),
        chapter(
            "po_dynamics", T, 3, "Polymerdynamik und Viskoelastizität",
            "Rouse- und Zimm-Modell, Verschlaufungen, Reptation, Viskosität, Maxwell-Modell, G′/G″, WLF.",
            emptyList(),
            sec("Rouse und Zimm", """
                Rouse: Kette aus N Perlen und Federn, Reibung jedes Segments unabhängig (gilt in Schmelzen, wo hydrodynamische Wechselwirkungen abgeschirmt sind). Zimm: In verdünnter Lösung bewegt sich das Lösungsmittel in der Knäuel mit — hydrodynamische Wechselwirkung macht die Kette schneller.
            """,
                fm("Rouse-Zeit", "τ_R ≈ ζ b² N²/(k_BT)"),
                fm("Zimm-Zeit", "τ_Z ≈ η_s R³/(k_BT) ∝ N^{3ν}"),
                fm("Diffusion", "D_Rouse = k_BT/(Nζ),  D_Zimm ∝ k_BT/(η_s R)")
            ),
            sec("Verschlaufungen und Reptation", """
                Oberhalb der Verschlaufungsmolmasse M_e behindern sich Ketten topologisch. De Gennes und Doi/Edwards: Jede Kette bewegt sich wie eine Schlange in einer Röhre aus Nachbarn (Reptation). Die Röhre wird erst nach der Ausreptationszeit τ_d erneuert. Das Modell sagt η ∝ N³ voraus, experimentell N^{3,4}; die Differenz erklären Röhrenlängenfluktuationen und constraint release. Die Schmelze zeigt dazwischen ein Gummiplateau mit Modul G_N⁰.
            """,
                fm("Reptationszeit", "τ_d ≈ τ_e (N/N_e)³"),
                fm("Plateaumodul", "G_N⁰ = ρRT/M_e"),
                fm("Viskosität", "η ∝ M (M < M_c),  η ∝ M^{3,4} (M > M_c)")
            ),
            sec("Lineare Viskoelastizität", """
                Bei einer Scherverformung γ(t) antwortet ein viskoelastisches Material mit einer Spannung, die vom gesamten Verlauf abhängt (Boltzmann-Superposition). Oszillatorisch misst man Speicher- und Verlustmodul. Das Maxwell-Element (Feder und Dämpfer in Reihe) ist das einfachste Modell einer Flüssigkeit mit Gedächtnis.
            """,
                fm("Boltzmann-Superposition", "σ(t) = ∫_{−∞}^{t} G(t − t′) γ̇(t′) dt′"),
                fm("Maxwell-Modell", "G(t) = G e^{−t/τ},  η = Gτ"),
                fm("Dynamische Moduln", "G′ = G ω²τ²/(1 + ω²τ²),  G″ = G ωτ/(1 + ω²τ²)"),
                fm("Verlustfaktor", "tan δ = G″/G′")
            ),
            sec("Zeit-Temperatur-Superposition", """
                Bei vielen amorphen Polymeren verschieben sich alle Relaxationszeiten mit der Temperatur um denselben Faktor a_T. Messkurven bei verschiedenen Temperaturen lassen sich so zu einer Masterkurve über viele Dekaden verschieben. Oberhalb T_g beschreibt die WLF-Gleichung a_T (äquivalent zu Vogel-Fulcher-Tammann).
            """,
                fm("WLF", "log a_T = −C₁(T − T_r)/(C₂ + T − T_r)"),
                fm("Universelle Konstanten (T_r = T_g)", "C₁ ≈ 17,4,  C₂ ≈ 51,6 K"),
                fm("VFT", "τ = τ₀ exp[B/(T − T₀)]")
            )
        ),
        chapter(
            "po_glass", T, 2, "Glasübergang",
            "Kinetischer Charakter, freies Volumen, Fox- und Flory-Fox-Gleichung, Fragilität, physikalische Alterung.",
            emptyList(),
            sec("Was ist der Glasübergang?", """
                Beim Abkühlen wird die segmentale Beweglichkeit (α-Relaxation) so langsam, dass das System auf der Zeitskala des Experiments nicht mehr ins Gleichgewicht kommt: Es erstarrt amorph. T_g hängt deshalb von der Kühlrate ab (etwa 3 K pro Dekade). In der DSC zeigt sich eine Stufe in c_p, in der Dilatometrie ein Knick im Volumen, mechanisch ein Abfall des Moduls von ≈ 1 GPa auf ≈ 1 MPa.
            """,
                fm("Kühlratenabhängigkeit", "ΔT_g ≈ 3 K pro Dekade Kühlrate"),
                fm("Doolittle (freies Volumen)", "η = A exp(B V₀/V_f)")
            ),
            sec("Einflüsse auf T_g", """
                Kettensteifigkeit, sperrige Seitengruppen und starke Wechselwirkungen erhöhen T_g; Weichmacher und kurze Ketten (mehr Kettenenden = mehr freies Volumen) senken es. Mischungen verträglicher Polymere zeigen ein einziges T_g dazwischen.
            """,
                fm("Flory-Fox", "T_g(M_n) = T_g^∞ − K/M_n"),
                fm("Fox-Gleichung", "1/T_g = w₁/T_g1 + w₂/T_g2"),
                fm("Gordon-Taylor", "T_g = (w₁T_g1 + k w₂T_g2)/(w₁ + k w₂)")
            ),
            sec("Fragilität und Alterung", """
                Die Fragilität m beschreibt, wie steil die Relaxationszeit bei T_g mit der Temperatur ansteigt (Angell-Plot). Unterhalb von T_g relaxiert das Glas langsam weiter in Richtung Gleichgewicht: Volumen und Enthalpie sinken, das Material wird spröder (physikalische Alterung).
            """,
                fm("Fragilität", "m = d log τ / d(T_g/T) |_{T = T_g}")
            )
        ),
        chapter(
            "po_cryst", T, 2, "Teilkristalline Polymere",
            "Lamellen, Kettenfaltung, Gibbs-Thomson, Hoffman-Lauritzen, Avrami-Kinetik, Sphärolithe, Kristallinität.",
            listOf("calorimetry"),
            sec("Morphologie", """
                Polymere kristallisieren nie vollständig: Ketten falten sich zu Lamellen von 5–20 nm Dicke, dazwischen liegen amorphe Bereiche. Lamellen wachsen radial zu Sphärolithen (im Polarisationsmikroskop als Malteserkreuz sichtbar). Die Kristallinität liegt typisch bei 30–70 %.
            """,
                fm("Kristallinität (Dichte)", "X_c = ρ_c(ρ − ρ_a)/(ρ(ρ_c − ρ_a))"),
                fm("Langperiode (SAXS)", "L = 2π/q_max")
            ),
            sec("Schmelzpunkt dünner Lamellen", """
                Dünne Lamellen haben relativ viel Oberfläche (Faltflächen mit Energie σ_e) und schmelzen deshalb unter dem Gleichgewichtsschmelzpunkt T_m⁰. Die Hoffman-Weeks-Extrapolation bestimmt T_m⁰ aus T_m gegen die Kristallisationstemperatur.
            """,
                fm("Gibbs-Thomson", "T_m = T_m⁰ (1 − 2σ_e/(Δh_v ℓ))"),
                fm("Lamellendicke (Hoffman-Lauritzen)", "ℓ ≈ 2σ_e T_m⁰/(Δh_v ΔT) + δℓ")
            ),
            sec("Kristallisationskinetik", """
                Die Kristallisation erfolgt durch Keimbildung und Wachstum. Die Avrami-Gleichung beschreibt den kristallisierten Anteil; der Exponent n verrät Keimbildungsart und Wachstumsgeometrie. Die Wachstumsrate hat ein Maximum zwischen T_g und T_m: Nahe T_m fehlt die Triebkraft, nahe T_g die Beweglichkeit.
            """,
                fm("Avrami", "X(t) = 1 − exp(−k tⁿ)"),
                fm("Wachstumsrate (Hoffman-Lauritzen)", "G = G₀ exp(−U*/(R(T − T_∞))) exp(−K_g/(T ΔT f))")
            )
        )
    ) + course(
        "Funktionale Polymere",
        chapter(
            "po_electroactive", T, 3, "Dielektrische und elektroaktive Polymere",
            "Dielektrische Relaxation, Havriliak-Negami, Ferro- und Piezoelektrika, Elektrete, leitfähige Polymere, DEA.",
            listOf("ec_landau"),
            sec("Dielektrische Spektroskopie", """
                Die komplexe Permittivität ε*(ω) = ε′ − iε″ zeigt Relaxationsprozesse: die α-Relaxation (kooperative Segmentbewegung, Glasübergang, VFT-Temperaturabhängigkeit), β-Prozesse (lokale Bewegungen, Arrhenius) und bei Leitfähigkeit einen ansteigenden ε″ ∝ σ/(ε₀ω) bei tiefen Frequenzen. Reale Peaks sind breiter als Debye und werden mit Cole-Cole oder Havriliak-Negami angepasst.
            """,
                fm("Debye", "ε*(ω) = ε_∞ + Δε/(1 + iωτ)"),
                fm("Havriliak-Negami", "ε*(ω) = ε_∞ + Δε/(1 + (iωτ)^α)^β"),
                fm("Leitfähigkeitsbeitrag", "ε″_σ = σ_dc/(ε₀ω)")
            ),
            sec("Piezo-, Pyro- und Ferroelektrika", """
                PVDF und P(VDF-TrFE) sind die wichtigsten ferroelektrischen Polymere. Gepolte Filme sind piezoelektrisch (d₃₃ ≈ −20 bis −30 pC/N, negatives Vorzeichen) und pyroelektrisch. Anwendung: Sensoren, Hydrophone, Ultraschallwandler, energieautarke Sensorik. Relaxor-Terpolymere bieten hohe Permittivität und große Elektrostriktion für Aktoren und Elektrokalorik.
            """,
                fm("Piezoeffekt", "D = d σ + ε^σ E"),
                fm("Pyroelektrischer Koeffizient", "p = (∂P/∂T)_E")
            ),
            sec("Leitfähige Polymere", """
                Konjugierte Polymere (Polyacetylen, Polythiophen, PEDOT:PSS) haben alternierende Einfach- und Doppelbindungen; Peierls-Instabilität öffnet eine Lücke von typisch 1–3 eV. Durch Dotierung (Oxidation/Reduktion) entstehen Polaronen und Bipolaronen als Ladungsträger; die Leitfähigkeit steigt um viele Größenordnungen (Nobelpreis 2000: Heeger, MacDiarmid, Shirakawa). Anwendungen: OLEDs, organische Photovoltaik, Transistoren, bioelektronische Elektroden.
            """,
                fm("Leitfähigkeit", "σ = n e μ"),
                fm("Bandlücke konjugierter Ketten", "E_g ≈ 1–3 eV (Peierls)")
            ),
            sec("Dielektrische Elastomere und Kondensatoren", """
                Dielektrische Elastomeraktoren (DEA) sind weiche Kondensatoren: Die Maxwell-Spannung der Elektroden presst den Film zusammen, er wird dünner und breiter. Hohe Dehnungen sind möglich, begrenzt durch elektromechanische Instabilität (pull-in) und Durchschlag. Polymerkondensatoren (BOPP) speichern Energie mit hoher Durchschlagfestigkeit; die Energiedichte wächst mit ε und E².
            """,
                fm("Maxwell-Druck", "p = ε₀ ε_r E²"),
                fm("Energiedichte (linear)", "u = ½ ε₀ ε_r E²"),
                fm("Energiedichte (nichtlinear)", "u = ∫ E dD")
            )
        )
    )
}

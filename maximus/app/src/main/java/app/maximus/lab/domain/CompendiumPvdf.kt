package app.maximus.lab.domain

/**
 * Caloric physics at doctoral level, centred on the relaxor terpolymer P(VDF-TrFE-CFE). Literature
 * numbers are given as ranges and orders of magnitude on purpose: reported ΔT values depend strongly
 * on composition, processing, film thickness, field profile and above all on the measurement method.
 */
internal object CompendiumPvdf {
    private val T = Topic.CALORIC

    val chapters: List<Chapter> = course(
        "P(VDF-TrFE-CFE) und Relaxor-Polymere",
        chapter(
            "ca_pvdf_structure", T, 2, "PVDF und seine Copolymere: Struktur und Phasen",
            "Monomer-Dipole, Konformationen, α/β/γ-Phasen, die Rolle von TrFE und die Curie-Umwandlung.",
            listOf("ec_landau"),
            sec("Das VDF-Monomer als Dipol", """
                Polyvinylidenfluorid (PVDF) besteht aus –CH₂–CF₂–-Einheiten. Die stark elektronegativen Fluoratome und die Wasserstoffatome sitzen auf gegenüberliegenden Seiten der Kette; jedes Monomer trägt ein Dipolmoment von etwa 7·10⁻³⁰ C m (≈ 2,1 D) senkrecht zur Kettenachse.

                Ob sich diese Dipole makroskopisch addieren, hängt von der Konformation der Kette ab: In der all-trans-Zickzack-Konformation (TTTT) zeigen alle CF₂-Dipole in dieselbe Richtung, in TGTG′-Folgen kompensieren sie sich teilweise.

                > Ferroelektrizität in Polymeren ist eine Eigenschaft der kristallinen Bereiche; der amorphe Anteil (oft 40–60 %) trägt zur Permittivität, aber nicht zur remanenten Polarisation bei.
            """,
                fm("Spontane Polarisation (Idealkristall)", "P_s = N μ / V,  ideal β-PVDF ≈ 0,13 C/m²", "gemessene Remanenz meist 0,05–0,1 C/m², reduziert durch Kristallinität und Orientierung"),
                fm("Dipolmoment VDF", "μ ≈ 7·10⁻³⁰ C m ≈ 2,1 D")
            ),
            sec("Kristallphasen", """
                • α-Phase (TGTG′): aus der Schmelze, unpolar (antiparallele Ketten), am häufigsten.
                • β-Phase (all-trans, orthorhombisch): polar, größte Polarisation, durch Recken, Polen oder Copolymerisation.
                • γ-Phase (T₃GT₃G′): schwach polar.
                • δ-Phase: polare Variante von α (durch starke Felder).

                Reines PVDF zeigt keine Curie-Umwandlung unterhalb der Schmelztemperatur, die β-Phase muss mechanisch oder elektrisch erzwungen werden.
            """,
                fm("Konformationen", "α: TGTG′ (unpolar) · β: TTTT (polar) · γ: T₃GT₃G′")
            ),
            sec("TrFE: Copolymer P(VDF-TrFE)", """
                Trifluorethylen (–CHF–CF₂–) ist sterisch größer. Ab etwa 20 mol-% TrFE kristallisiert das Copolymer direkt in die polare, all-trans-ähnliche Phase, ohne Recken. Gleichzeitig wird das Gitter aufgeweitet; dadurch tritt eine echte ferro-paraelektrische Curie-Umwandlung unterhalb der Schmelze auf (Übergang 1. Ordnung mit thermischer Hysterese).

                Mit steigendem TrFE-Anteil sinkt T_C (bei 70/30 grob 100 °C, bei 55/45 etwa 60–70 °C). In der paraelektrischen Phase herrschen Gauche-Defekte vor (dynamisch ungeordnete Konformationen).
            """,
                fm("Curie-Übergang", "ferro (all-trans) ⇄ para (TG-ungeordnet),  ΔH, ΔS ≠ 0 (1. Ordnung)"),
                fm("Curie-Weiss oberhalb T_C", "ε_r ≈ C/(T − T₀)")
            ),
            sec("CFE: der Weg zum Relaxor", """
                Chlorfluorethylen (–CH₂–CFCl–) wird als dritte Komponente in geringer Menge (typisch 5–10 mol-%) eingebaut. Das große Cl-Atom wirkt als statistisch verteilter Defekt: Es weitet das Gitter weiter auf, stört die langreichweitige polare Ordnung und zerlegt sie in polare Nanobereiche.

                Ergebnis: ein relaxor-ferroelektrisches Polymer mit breitem, frequenzabhängigem Permittivitätsmaximum nahe Raumtemperatur, sehr hoher Permittivität (ε_r ≈ 40–60 bei 1 kHz) und schlanker P(E)-Schleife mit kleiner Remanenz. Verwandte Wege zum selben Zustand: CTFE statt CFE oder Elektronenbestrahlung von P(VDF-TrFE).

                > Für die Elektrokalorik ist das entscheidend: hohe feldinduzierte Polarisationsänderung ΔP bei kleiner Hysterese, also große reversible Entropieänderung bei kleinen Verlusten.
            """,
                fm("Typische Zusammensetzung", "VDF/TrFE/CFE ≈ 60–65 / 28–35 / 5–9 mol-%"),
                fm("Schlanke Schleife", "P_r ≪ P_max,  Verlust pro Zyklus W_loss = ∮ E dP klein")
            ),
            sec("Herstellung und Morphologie", """
                Filme entstehen durch Lösungsgießen (z. B. aus DMF oder MEK), Spin-Coating oder Schmelzpressen; anschließendes Tempern unterhalb der Schmelze (typisch 100–120 °C) erhöht Kristallinität und Lamellendicke. Uniaxiales Recken orientiert die Ketten in der Filmebene, die Dipole stehen dann senkrecht dazu und können vom Feld zwischen Ober- und Unterelektrode optimal angesprochen werden.

                Kennzahlen, die man in einer Verteidigung parat haben sollte: Kristallinität (DSC, XRD), Phasenanteil (FTIR-Banden: β ≈ 1280 und 840 cm⁻¹, α ≈ 763 und 975 cm⁻¹), Lamellendicke (SAXS), Durchschlagfeldstärke (Weibull-Statistik).
            """,
                fm("Kristallinität aus DSC", "X_c = ΔH_m/ΔH_m⁰"),
                fm("Weibull-Verteilung", "P_f(E) = 1 − exp[−(E/E_b)^β]")
            )
        ),
        chapter(
            "ca_relaxor", T, 3, "Relaxor-Ferroelektrika",
            "Polare Nanobereiche, Vogel-Fulcher, Ergodizität, feldinduzierte Übergänge und Modelle.",
            emptyList(),
            sec("Phänomenologie", """
                Relaxoren zeigen statt eines scharfen Curie-Peaks ein breites Permittivitätsmaximum bei T_m, das sich mit steigender Messfrequenz zu höheren Temperaturen verschiebt und dabei kleiner wird. Unterhalb von T_m gibt es keine makroskopische Symmetriebrechung, aber lokale polare Ordnung.

                Charakteristische Temperaturen:
                • Burns-Temperatur T_B: Entstehung polarer Nanobereiche (PNR), Abweichung vom Curie-Weiss-Gesetz.
                • T_m(f): frequenzabhängiges Maximum.
                • Einfriertemperatur T_f: Divergenz der Relaxationszeit (Vogel-Fulcher), Übergang ergodisch → nichtergodisch.
            """,
                fm("Vogel-Fulcher", "f = f₀ exp[−E_a/(k_B(T_m − T_f))]"),
                fm("Quadratisches Gesetz", "1/ε − 1/ε_m = (T − T_m)²/(2ε_m δ²)  (T > T_m)"),
                fm("Diffusivität", "1/ε − 1/ε_m ∝ (T − T_m)^γ,  1 ≤ γ ≤ 2")
            ),
            sec("Ergodisch und nichtergodisch", """
                Oberhalb T_f fluktuieren die PNR thermisch: Der Zustand ist ergodisch, ein angelegtes Feld richtet sie reversibel aus, die Polarisation verschwindet beim Abschalten. Unterhalb T_f sind sie eingefroren: Ein starkes Feld kann einen langreichweitig geordneten ferroelektrischen Zustand induzieren, der nach dem Abschalten erhalten bleibt.

                Konsequenz für die Kalorik: Im ergodischen Bereich ist die Feldantwort weitgehend reversibel, im nichtergodischen Bereich treten Hysterese und Gedächtniseffekte auf. Maxwell-Relationen setzen Gleichgewicht voraus und sind im nichtergodischen Zustand nicht anwendbar.
            """,
                fm("Ergodizitätsbruch", "T < T_f:  Zeitmittel ≠ Ensemblemittel")
            ),
            sec("Feldinduzierte Umwandlung und kritischer Endpunkt", """
                Im E-T-Phasendiagramm vieler Relaxoren endet die Linie des feldinduzierten Relaxor-Ferroelektrik-Übergangs in einem kritischen Endpunkt. Dort ist die Polarisation besonders empfindlich gegenüber Temperatur und Feld: (∂P/∂T)_E ist groß, und die Entropieänderung pro Feldänderung maximal. Viele der größten elektrokalorischen Effekte liegen in der Nähe solcher Punkte bzw. der Widom-Linie oberhalb davon.
            """,
                fm("Clausius-Clapeyron (feldinduziert)", "dE/dT = −ΔS/ΔP"),
                fm("EC-Stärke", "ΔT/ΔE,  ΔS/ΔE als Gütezahlen")
            ),
            sec("Modelle", """
                • Spherical-Random-Bond-Random-Field-Modell (Pirc, Blinc): PNR als wechselwirkende Pseudospins mit zufälligen Bindungen und Feldern; liefert P(E, T) und die EC-Antwort auch im Relaxorzustand.
                • Landau-Ansatz mit verteilten Übergangstemperaturen: Mittelung über Bereiche mit lokalen T₀.
                • Polar-Entropie-Argument: Je mehr nahezu entartete polare Zustände (Orientierungen, Konformationen) zugänglich sind, desto größer ist die durch das Feld unterdrückbare Entropie. Bei Polymeren tragen Konformationsfreiheitsgrade (Trans-Gauche) zusätzlich bei.
            """,
                fm("Obergrenze polare Entropie", "ΔS_max ≈ (N/m) k_B ln Ω  (N Dipole, Ω Orientierungen, Masse m)")
            )
        ),
        chapter(
            "ca_pvdf_ece", T, 3, "Elektrokalorik in P(VDF-TrFE-CFE)",
            "Thermodynamik, Größenordnungen, Feld- und Temperaturabhängigkeit, Verluste und Grenzen.",
            listOf("ec_landau", "ec_pyro"),
            sec("Thermodynamische Grundlage", """
                Die adiabatische Temperaturänderung folgt aus der Maxwell-Relation (∂S/∂E)_T = (∂P/∂T)_E. Sie ist groß, wenn die Polarisation stark von der Temperatur abhängt (Nähe zu Übergängen) und wenn große Felder anwendbar sind. Polymere haben gegenüber Keramiken zwei Vorteile: sehr hohe Durchschlagfestigkeit (einige 100 MV/m in dünnen Filmen) und eine große Zahl zugänglicher polarer Zustände; Nachteil: niedrige Wärmeleitfähigkeit (≈ 0,2 W/(m K)) und kleine thermische Masse pro Fläche.
            """,
                fm("Indirekte Methode", "ΔT = −∫_{E₁}^{E₂} (T/(ρ c_E)) (∂P/∂T)_E dE"),
                fm("Isotherme Entropieänderung", "ΔS = ∫_{E₁}^{E₂} (∂P/∂T)_E dE"),
                fm("Landau (Polarisation)", "ΔS = −(a/2)(P₂² − P₁²) ⇒ ΔT ≈ (T a/(2ρc)) ΔP²")
            ),
            sec("Größenordnungen aus der Literatur", """
                Die Arbeiten der Gruppe um Q. M. Zhang (Penn State) zeigten 2008 große EC-Effekte in P(VDF-TrFE)-Copolymeren und Relaxor-Terpolymeren (Neese et al., Science 2008): Temperaturänderungen um ≈ 12 K bei sehr hohen Feldern (> 150 MV/m), damals indirekt bestimmt. Direkte Messungen an Terpolymerfilmen ergeben bei moderaten Feldern (50–100 MV/m) typischerweise einige Kelvin, mit großer Streuung je nach Material und Methode.

                • Spezifische Wärme c ≈ 1,3–1,5 kJ/(kg K), Dichte ρ ≈ 1,8 g/cm³.
                • Bei kleinen Feldern gilt ΔT ∝ E² (ΔP ∝ E), bei hohen Feldern flacht die Kurve ab (Sättigung von P).
                • Das Maximum von ΔT(T) liegt beim Terpolymer breit um Raumtemperatur — ein Vorteil gegenüber scharfen Curie-Peaks von Copolymeren.

                > In einer Verteidigung: immer angeben, ob ein Wert direkt oder indirekt bestimmt wurde, bei welchem Feld, welcher Temperatur, an welcher Probe (freistehend oder auf Substrat) und mit welcher Feldform.
            """,
                fm("Kleinfeld-Näherung", "ΔT ≈ (T/(2ρc)) (∂χ/∂T) ε₀ E²  (linear-dielektrisch)"),
                fm("EC-Stärke typischer Terpolymere", "ΔT/ΔE ≈ 0,02–0,06 K·m/MV")
            ),
            sec("Irreversible Beiträge", """
                Jede Feldänderung erzeugt neben dem reversiblen EC-Effekt Wärme:
                • Hysteresverlust: Pro Zyklus und Volumen W = ∮ E dP, vollständig dissipiert.
                • Leitungsverluste (Joule): q̇ = σE²; bei hohen Feldern und Temperaturen wächst σ stark (Hopping, Ionenleitung).
                • Dielektrische Relaxationsverluste: ε″ bei der Zyklusfrequenz.

                Diese Beiträge sind nicht zeitumkehrbar: Beim Abschalten erwärmt sich die Probe ebenfalls. Saubere Experimente trennen sie, indem Heiz- und Kühlspitze beim Ein- und Ausschalten verglichen werden (ΔT_on ≈ −ΔT_off für reine EC-Effekte).
            """,
                fm("Hysteresverlust", "W_loss = ∮ E dP  (pro Volumen und Zyklus)"),
                fm("Joulesche Wärme", "q̇ = σ E²"),
                fm("Materialeffizienz", "η_mat = Q_EC/W_el,  W_el = ∫ E dP beim Aufladen")
            ),
            sec("Ermüdung und Zuverlässigkeit", """
                Für Kühlgeräte zählen Millionen Zyklen. Relevante Mechanismen: Ladungsinjektion und Raumladung an den Elektroden, elektrochemische Degradation, mechanisches Kriechen durch Elektrostriktion, lokale Durchschläge (Selbstheilung bei dünnen Elektroden möglich). Die Zuverlässigkeit beschreibt man über Weibull-Statistik der Durchschlagfeldstärke und über ΔT nach n Zyklen.
            """,
                fm("Feld-Lebensdauer (empirisch)", "t_f ∝ E^{−n}  (inverses Potenzgesetz)")
            )
        ),
        chapter(
            "ca_ec_measure", T, 3, "Messung elektrokalorischer Effekte",
            "Direkte, quasi-direkte und indirekte Verfahren, Fehlerquellen, Wärmeübertrag an dünnen Filmen.",
            emptyList(),
            sec("Indirekt über Maxwell-Relationen", """
                Man misst P(E) bei mehreren Temperaturen (Schleifen), bildet (∂P/∂T)_E und integriert. Vorteile: einfache Ausrüstung. Grenzen: setzt thermodynamisches Gleichgewicht und Eindeutigkeit von P(E, T) voraus. Bei Hysterese, nichtergodischen Relaxoren, Leitfähigkeitsbeiträgen zur gemessenen „Polarisation“ und bei Übergängen 1. Ordnung liefert die Methode fehlerhafte, oft zu große oder sogar vorzeichenfalsche Werte.
            """,
                fm("Numerische Ableitung", "(∂P/∂T)_E ≈ [P(T + ΔT) − P(T − ΔT)]/(2ΔT)")
            ),
            sec("Direkte Methoden", """
                • Thermistor oder Thermoelement auf der Probe: misst ΔT direkt; die thermische Masse des Sensors und der Elektroden verkleinert das Signal.
                • Infrarot-Thermografie: berührungslos, ortsaufgelöst; Emissivität kalibrieren, Zeitauflösung beachten.
                • Hochauflösende Kalorimeter (modifizierte Kalorimeter mit Wärmestrommessung): integrierte Wärme Q beim Ein- und Ausschalten.
                • Scanning thermal microscopy für sehr dünne Filme.

                Wärmeübertrag: Ein Film der Dicke d hat die thermische Diffusionszeit τ ≈ d²/α (α ≈ 10⁻⁷ m²/s; für d = 10 μm also ≈ 1 ms). Liegt der Film auf einem Substrat, fließt die Wärme in Millisekunden ab — der gemessene Spitzenwert muss über Wärmeübergangsmodelle korrigiert werden.
            """,
                fm("Diffusionszeit", "τ ≈ d²/α"),
                fm("Wärmekapazitäts-Korrektur", "ΔT_Probe = ΔT_gemessen · (C_Probe + C_Sensor + C_Elektroden)/C_Probe")
            ),
            sec("Quasi-direkt: DSC und Wärmestrom", """
                Eine DSC mit elektrischen Durchführungen misst den Wärmestrom beim Feldschalten bei konstanter Temperatur: Integriert ergibt das Q = T ΔS (isotherm). Daraus folgt mit c_E die adiabatische Änderung ΔT ≈ −T ΔS/c_E. Vorteil: echte Wärme, keine Annahme von Gleichgewicht. Joule- und Hysteresewärme erscheinen als zusätzliche, bei Ein- und Ausschalten gleichgerichtete Beiträge.
            """,
                fm("Isotherme Wärme", "Q = ∫ q̇ dt = T ΔS"),
                fm("Umrechnung", "ΔT_ad ≈ −T ΔS / c_E")
            ),
            sec("Checkliste Messprotokoll", """
                • Feldform (Rechteck, Rampe), Anstiegszeit und Haltezeit dokumentieren.
                • Ein- und Ausschaltspitze getrennt auswerten (Reversibilität).
                • Leckstrom parallel messen (Joule-Anteil abschätzen).
                • Probengeometrie: freistehend vs. geklemmt (geklemmte Filme: andere Elektrostriktion, andere Wärmekapazität).
                • Unsicherheitsbudget: Sensorkalibrierung, Wärmeverluste, Probendicke (geht quadratisch in E und linear in die thermische Masse ein).
            """)
        ),
        chapter(
            "ca_ec_devices", T, 3, "Elektrokalorische Kühlgeräte",
            "Kreisprozesse, Regeneration, Wärmeübertrag, Leistungszahl, elektrostatische Aktuierung, Kaskaden.",
            listOf("caloric_cycle"),
            sec("Kreisprozesse", """
                • Brayton-Zyklus: adiabatisch polarisieren (Erwärmung), Wärme an die warme Seite abgeben bei konstantem Feld, adiabatisch entpolarisieren (Abkühlung), Wärme von der kalten Seite aufnehmen ohne Feld.
                • Ericsson-Zyklus: Feldänderungen isotherm, Wärmeaustausch über einen Regenerator.
                • Carnot ist praktisch nicht erreichbar; realistische Geräte erreichen Bruchteile der Carnot-Leistungszahl.

                Ein einzelner Film liefert nur ΔT_ad als Temperaturspanne. Größere Spannen entstehen durch Regeneration (aktiver elektrokalorischer Regenerator, AER) oder Kaskaden mehrerer Stufen mit gestaffelten Arbeitstemperaturen.
            """,
                fm("Carnot-Leistungszahl (Kühlen)", "COP_C = T_k/(T_w − T_k)"),
                fm("Leistungszahl", "COP = Q_k/W_el"),
                fm("Exergetischer Wirkungsgrad", "η_II = COP/COP_C")
            ),
            sec("Wärmeübertrag und Frequenz", """
                Die Kühlleistung pro Volumen wächst mit der Zyklusfrequenz, solange der Film in jeder Halbperiode thermisch relaxieren kann. Grenze: f ≲ 1/(2τ) mit τ = d²/α (Film) und den Kontaktwiderständen zu Wärmequelle und -senke. Dünne Filme, guter Kontakt (Biot-Zahl klein) und hohe Frequenzen ergeben hohe Leistungsdichten.
            """,
                fm("Kühlleistung (grob)", "Q̇_k ≈ f · ρ c ΔT_ad · V · η_HT"),
                fm("Biot-Zahl", "Bi = h d/k  (Bi ≪ 1: Film nahezu isotherm)")
            ),
            sec("Gerätekonzepte", """
                • Elektrostatisch bewegte Filme: Ein Polymerfilm wird durch elektrostatische Kräfte abwechselnd an Wärmequelle und Wärmesenke gedrückt (Ma et al., Science 2017, mit P(VDF-TrFE-CFE)); kein Fluid, gute Kontaktwärmeübertragung, kompakt.
                • Fluidbasierte Regeneratoren: Stapel aus Filmen, durchströmt von einem Wärmeträgerfluid, das zwischen den Halbzyklen hin- und herpumpt.
                • Rotierende oder linear bewegte Ringe aus EC-Material.
                • Mehrschichtkondensatoren (MLC) aus Keramik als Gegenstück zu Polymerfilmen.

                Polymerfilme sind flexibel und leicht, eignen sich für tragbare Kühlung und Elektronikkühlung; Keramik-MLCs haben höhere Wärmeleitfähigkeit.
            """),
            sec("Energierückgewinnung", """
                Der EC-Film ist ein Kondensator: Die beim Laden gespeicherte elektrische Energie (ohne die als Wärme umgesetzte) kann beim Entladen in den nächsten Kondensator zurückgeführt werden. Ohne Rückgewinnung ist die Leistungszahl klein; mit hoher Rückgewinnungsrate (> 90 %) wird sie konkurrenzfähig. Daher ist der Wirkungsgrad der Leistungselektronik Teil jeder realistischen COP-Betrachtung.
            """,
                fm("Mit Rückgewinnung", "W_netto = W_lade − η_rück W_entlade")
            )
        ),
        chapter(
            "ca_electromech", T, 3, "Elektrostriktion und elastokalorische Polymere",
            "Elektromechanische Kopplung in Relaxorpolymeren, Spannung als Stellgröße, Gummi und Polymere.",
            listOf("elastocaloric"),
            sec("Elektrostriktion in P(VDF-TrFE-CFE)", """
                Polymere Relaxoren zeigen große feldinduzierte Dehnungen von mehreren Prozent, quadratisch in der Polarisation. Der Elektrostriktionskoeffizient Q₃₃ von PVDF-basierten Polymeren ist negativ: Der Film wird in Feldrichtung dünner und dehnt sich in der Ebene aus. Ursache ist die Konformationsänderung von Gauche- zu Trans-Sequenzen und die Ausrichtung der polaren Bereiche.

                Konsequenz für EC-Messungen: Mechanische Randbedingungen (freistehend vs. auf Substrat geklemmt) ändern P(E) und damit den EC-Effekt. Zugleich koppeln mechanische Spannungen an die Polarisation — die Grundlage für multikalorische Ansätze.
            """,
                fm("Elektrostriktion", "S_ij = Q_ijkl P_k P_l  (bzw. S = M E² bei kleinen Feldern)"),
                fm("Elektrostatische Druckkomponente (Maxwell-Spannung)", "S_M = −ε₀ ε_r E²/(2Y)")
            ),
            sec("Elastokalorik in Polymeren", """
                In Elastomeren ist die Rückstellkraft überwiegend entropisch: Recken ordnet die Ketten, ihre Konformationsentropie sinkt, adiabatisch erwärmt sich das Material. Bei Naturkautschuk kommt dehnungsinduzierte Kristallisation hinzu, die große latente Wärmen beiträgt (ΔT von mehreren Kelvin bei großen Dehnungen). Nachteile: große Dehnungen nötig, Ermüdung, Hysterese.

                In ferroelektrischen Polymeren kann mechanische Spannung die Phasenumwandlung verschieben (Clausius-Clapeyron für Spannung) und damit den EC-Effekt bei gegebener Temperatur verstärken.
            """,
                fm("Thermoelastischer Effekt (Gummi)", "ΔT ≈ (T/c) (∂f/∂T)_L ΔL/m"),
                fm("Clausius-Clapeyron (mechanisch)", "dσ/dT = −ΔS/(ε_tr V_m)")
            ),
            sec("Barokalorik in Polymeren", """
                Hydrostatischer Druck koppelt an das Volumen. Polymere haben große Volumenausdehnungskoeffizienten und weiche Gitter; barokalorische Effekte in Elastomeren und an Übergängen (z. B. Glasübergang, Ordnungs-Unordnungs-Übergänge) wurden als groß berichtet. Für die isotherme Entropieänderung gilt die Volumen-Maxwell-Relation.
            """,
                fm("Barokalorik", "ΔS = −∫ (∂V/∂T)_p dp")
            )
        ),
        chapter(
            "ca_multi_theory", T, 3, "Multikalorik: Thermodynamik gekoppelter Felder",
            "Gibbs-Funktion G(T, E, σ, H), Kreuz-Maxwell-Relationen, Pfadabhängigkeit, Hysteresereduktion.",
            listOf("multicaloric"),
            sec("Gemeinsames Potential", """
                Wirken mehrere Felder (E, σ, H, p) auf ein Material, ist die Gibbs-Funktion G(T, E, σ, H) das natürliche Potential. Alle Entropieänderungen folgen aus ihren gemischten zweiten Ableitungen. Die Gesamtänderung entlang eines Pfades im Feldraum ist die Summe der Beiträge — im Gleichgewicht wegunabhängig, mit Hysterese wegabhängig.
            """,
                fm("Differential", "dG = −S dT − P dE − ε dσ − μ₀M dH"),
                fm("Maxwell (EC)", "(∂S/∂E)_{T,σ} = (∂P/∂T)_{E,σ}"),
                fm("Maxwell (eC)", "(∂S/∂σ)_{T,E} = (∂ε/∂T)_{σ,E}"),
                fm("Kreuzkopplung", "(∂P/∂σ)_{T,E} = (∂ε/∂E)_{T,σ}  (Piezo/Elektrostriktion)")
            ),
            sec("Wozu zwei Felder?", """
                • Verschieben: Ein Hilfsfeld (z. B. Spannung) verschiebt die Übergangstemperatur, sodass der EC-Effekt bei der gewünschten Arbeitstemperatur maximal ist.
                • Hysterese umgehen: Bei Übergängen 1. Ordnung kann man die Hin-Umwandlung mit einem Feld und die Rück-Umwandlung mit dem anderen treiben und so die Schleife „umfahren“.
                • Verstärken: Beide Felder senken dieselbe Entropie (gleiches Vorzeichen der Kopplung) oder ein Feld erleichtert dem anderen die Umwandlung.

                Kopplung kann direkt (ein Material, mehrere Ordnungsparameter) oder indirekt (Komposit, z. B. EC-Schicht auf magnetostriktivem Substrat, Kopplung über Dehnung) sein.
            """,
                fm("Gesamtentropie entlang Pfad", "ΔS = ∫ (∂P/∂T) dE + ∫ (∂ε/∂T) dσ + ∫ μ₀(∂M/∂T) dH")
            ),
            sec("Grenzen", """
                Kreuzeffekte sind oft klein gegen die Haupteffekte; zusätzliche Aktuatoren kosten Energie und Bauraum. Der Nutzen muss deshalb über das Gesamtsystem (Leistungszahl, Temperaturspanne, Lebensdauer) bewertet werden, nicht nur über ΔT.
            """)
        ),
        chapter(
            "ca_defense", T, 3, "Promotionsverteidigung: Kalorik und Relaxor-Polymere",
            "Kernaussagen, kritische Einwände und wie man sie beantwortet, Benchmarks, offene Fragen.",
            emptyList(),
            sec("Die Geschichte in drei Sätzen", """
                Bereite für die Verteidigung eine Kernaussage vor, die in drei Sätzen trägt: (1) Welches Problem (z. B. effiziente, kompakte, kältemittelfreie Kühlung)? (2) Welcher physikalische Hebel (feldgesteuerte polare Entropie in einem Relaxor-Polymer mit schlanker Schleife)? (3) Welcher neue Beitrag (Messung, Modell, Material, Gerät) mit welcher Zahl und welcher Unsicherheit?

                > Jede Zahl auf einer Folie braucht: Methode, Feld, Temperatur, Probe, Unsicherheit.
            """),
            sec("Typische Einwände und Antworten", """
                • „Ist Ihr ΔT wirklich elektrokalorisch und nicht Joulesche Wärme?“ → Vergleich Ein- und Ausschaltspitze; Leckstrom gemessen; Abschätzung σE²·t ≪ c ΔT.
                • „Warum indirekte Methode bei einem Relaxor?“ → Maxwell setzt Gleichgewicht voraus; Gültigkeit im ergodischen Bereich prüfen, direkte Kontrollmessung zeigen.
                • „Wie reproduzierbar?“ → mehrere Proben, Streuung, Weibull-Durchschlag, Zyklenfestigkeit.
                • „Was begrenzt die Leistungszahl im Gerät?“ → Wärmeübertrag (Kontakt, Frequenz), Hysterese- und Leitungsverluste, Energierückgewinnung der Elektronik.
                • „Warum Polymer statt Keramik?“ → höhere Durchschlagfestigkeit, Flexibilität, großer ΔS pro Masse; dafür geringe Wärmeleitfähigkeit und begrenzte Temperaturstabilität.
                • „Welche Rolle spielt die Mikrostruktur?“ → Kristallinität, Phase, Lamellendicke, Orientierung, CFE-Verteilung bestimmen P(E, T).
            """),
            sec("Benchmarks (Größenordnungen)", """
                • Relaxor-Polymerfilme: ΔT einige K bei 50–100 MV/m, um 10 K und mehr bei Feldern nahe der Durchschlagfestigkeit (Methode beachten).
                • Keramische Dünnschichten (z. B. PZT-basiert): ΔT ≈ 10 K bei sehr hohen Feldern und hohen Temperaturen (Mischenko et al., Science 2006).
                • Keramische Bulk/MLC (PST, PMN-PT, BaTiO₃): ΔT ≈ 1–3 K bei ≈ 5–15 MV/m.
                • Magnetokalorik (Gd bei 2 T): ΔT ≈ 5 K; Elastokalorik (NiTi): ΔT ≈ 20–25 K bei großen Dehnungen, aber mit Ermüdungsproblemen.
            """),
            sec("Offene Forschungsfragen", """
                • Mikroskopische Theorie der polaren Entropie in Polymeren (Anteil Konformation vs. Dipolorientierung).
                • Langzeitstabilität bei Millionen Zyklen und erhöhten Temperaturen.
                • Hohe Wärmeleitfähigkeit ohne Verlust der EC-Eigenschaften (Nanokomposite, z. B. mit BN).
                • Geräte mit großer Temperaturspanne (Regeneration, Kaskaden) und hoher Rückgewinnung der elektrischen Energie.
                • Multikalorische Konzepte, die Hysterese real reduzieren.
            """)
        )
    )
}

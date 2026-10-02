package app.maximus.lab.domain

internal object CompendiumCaloric {
    val chapters = listOf(
        chapter(
            "ca_calorimetry", Topic.CALORIC, 1, "Kalorimetrie",
            "Wärmekapazität, latente Wärme und wie man Wärme misst.",
            listOf("calorimetry"),
            sec("Wärme und Wärmekapazität", """
                Wärme ist Energie, die aufgrund einer Temperaturdifferenz übertragen wird. Die Wärmekapazität C = δQ/dT gibt an, wie viel Wärme pro Kelvin nötig ist; spezifisch pro Masse c, molar pro Stoffmenge. Wasser hat mit 4,186 J/(g K) eine außergewöhnlich hohe spezifische Wärme — deshalb dämpfen Ozeane das Klima.

                Bei Phasenübergängen erster Ordnung fließt Wärme ohne Temperaturänderung: latente Wärme (Schmelzen von Eis 334 J/g, Verdampfen von Wasser 2257 J/g).
            """,
                fm("Wärme", "Q = m c ΔT"),
                fm("Latente Wärme", "Q = m L"),
                fm("Mischungstemperatur", "T_m = Σ mᵢcᵢTᵢ / Σ mᵢcᵢ")
            ),
            sec("Messmethoden", """
                • Mischungskalorimeter: Bilanz in einem isolierten Gefäß; der Wasserwert berücksichtigt die Wärmekapazität des Gefäßes.
                • Dynamische Differenzkalorimetrie (DSC): Probe und Referenz werden mit gleicher Rate geheizt; die Differenz des Wärmestroms liefert c_p(T) und latente Wärmen. Standardmethode für kalorische Materialien.
                • Relaxations- und Adiabatkalorimetrie für tiefe Temperaturen.
                • Direkte Messung kalorischer Effekte: Thermoelement, Infrarotkamera oder Thermistor auf der Probe, während das Feld schnell (adiabatisch) geschaltet wird.
            """)
        ),
        chapter(
            "ca_thermo", Topic.CALORIC, 2, "Thermodynamik kalorischer Effekte",
            "Ein Prinzip für alle Felder: Maxwell-Relationen, isothermes ΔS und adiabatisches ΔT.",
            listOf("ec_pyro", "caloric_cycle"),
            sec("Das gemeinsame Prinzip", """
                Ein kalorischer Effekt ist die reversible thermische Antwort eines Materials auf das Ein- und Ausschalten eines äußeren Feldes. Das Feld ordnet einen inneren Freiheitsgrad (Dipole, Spins, Kristallstruktur) und senkt damit die Entropie. Geschieht das isotherm, gibt das Material die Wärme TΔS ab; geschieht es adiabatisch, steigt die Temperatur um ΔT_ad.

                Je nach Feld unterscheidet man: elektrokalorisch (E), magnetokalorisch (H), elastokalorisch (einachsige Spannung σ), barokalorisch (hydrostatischer Druck p). Treten mehrere gleichzeitig auf: multikalorisch. Elasto- und Barokalorik fasst man als mechanokalorisch zusammen.
            """,
                fm("Verallgemeinerte Gibbs-Energie", "dG = −S dT − P dE − μ₀M dH − ε dσ + V dp")
            ),
            sec("Maxwell-Relationen und die indirekte Methode", """
                Aus der Vertauschbarkeit der zweiten Ableitungen von G folgen für jedes Feld Beziehungen zwischen Entropie und dem temperaturabhängigen Ordnungsparameter. Misst man P(T), M(T) oder ε(T) bei mehreren Feldern, erhält man ΔS durch Integration (indirekte Methode).

                Achtung: Maxwell-Relationen gelten nur im Gleichgewicht. An Übergängen erster Ordnung mit Hysterese kann die indirekte Methode Artefakte liefern; dort ist die direkte Messung oder Clausius-Clapeyron vorzuziehen.
            """,
                fm("Elektrokalorisch", "(∂S/∂E)_T = (∂P/∂T)_E"),
                fm("Magnetokalorisch", "(∂S/∂H)_T = μ₀(∂M/∂T)_H"),
                fm("Elastokalorisch", "(∂S/∂σ)_T = (∂ε/∂T)_σ"),
                fm("Barokalorisch", "(∂S/∂p)_T = −(∂V/∂T)_p")
            ),
            sec("Isothermes ΔS und adiabatisches ΔT", """
                Isotherme Entropieänderung (Kühlkapazität pro Zyklus) und adiabatische Temperaturänderung (erreichbare Temperaturspanne) sind die beiden Kennzahlen. Sie hängen über die Wärmekapazität zusammen; am Phasenübergang sind beide maximal.

                Für die Kühlung wichtig sind auch die Hysterese (irreversibel verlorene Arbeit), die Ermüdung über 10⁶–10⁹ Zyklen, die nötige Feldstärke und die Geschwindigkeit des Wärmeaustauschs.
            """,
                fm("Adiabatisches ΔT", "ΔT_ad = −∫ (T/C_x)(∂X/∂T)_x dx"),
                fm("Näherung", "ΔT_ad ≈ −T ΔS_iso/C"),
                fm("Clausius-Clapeyron (Übergang)", "dx_tr/dT = −ΔS_tr/ΔX_tr")
            )
        ),
        chapter(
            "ca_electro", Topic.CALORIC, 2, "Elektrokalorik",
            "Ferroelektrika, Landau-Theorie, Dünnschichten, Relaxoren und Polymere.",
            listOf("ec_landau", "ec_pyro"),
            sec("Physikalisches Bild", """
                In polaren Materialien richtet ein elektrisches Feld die Dipole aus. Der Effekt ist am größten nahe dem ferroelektrischen Übergang, wo die Polarisation stark von T abhängt (großes ∂P/∂T). Im paraelektrischen Bereich induziert das Feld Polarisation und senkt die Entropie; der Kristall erwärmt sich.

                Elektrische Felder sind billig, leise und schnell schaltbar — der große Vorteil gegenüber der Magnetokalorik, die starke Permanentmagnete braucht.
            """),
            sec("Landau-Devonshire-Beschreibung", """
                Entwickelt man die freie Energie in der Polarisation, ist die Dipolentropie S = −∂G/∂T. Im einfachsten Fall (nur α₁ temperaturabhängig, α₁ = a(T − T₀)) gilt ΔS = −(a/2)(P² − P₀²): Die Entropieänderung ist proportional zur Änderung von P². Daraus folgt eine universelle Abschätzung für ΔT.

                Bei BaTiO₃ ist der Übergang erster Ordnung; hier kann ein Feld den Übergang selbst induzieren und so die latente Wärme nutzbar machen (BaTiO₃-Einkristall: ≈ 0,9 K bei 12 kV/cm).
            """,
                fm("Landau-Potential", "G = α₁P² + α₁₁P⁴ + α₁₁₁P⁶ − EP"),
                fm("Dipolentropie", "ΔS = −(a/2)(P² − P₀²)"),
                fm("EC-Temperaturänderung", "ΔT ≈ (aT/2ρc)(P² − P₀²)")
            ),
            sec("Materialien", """
                • Dünne Schichten halten Felder bis > 1 MV/cm aus: PbZr₀,₉₅Ti₀,₀₅O₃ ≈ 12 K bei 480 kV/cm und 222 °C (Mischenko et al., 2006) — der Durchbruch, der das Feld wiederbelebte.
                • Ferroelektrische Polymere P(VDF-TrFE) und Relaxor-Terpolymere: ≈ 12 K bei Raumtemperatur, flexibel.
                • Relaxor-Ferroelektrika (PMN-PT) und bleifreie Systeme (BaZrTiO₃, NaBiTiO₃) für breite Temperaturfenster.
                • Vielschicht-Kondensatoren (MLCC) aus PbSc₀,₅Ta₀,₅O₃ erreichen über 5 K im Bauteil; damit wurden erste elektrokalorische Kühlprototypen mit 13 K Spanne gebaut.
            """)
        ),
        chapter(
            "ca_elasto", Topic.CALORIC, 2, "Elastokalorik",
            "Martensitische Umwandlung, Superelastizität, NiTi, Gummi und der thermoelastische Effekt.",
            listOf("elastocaloric"),
            sec("Martensitische Umwandlung", """
                Formgedächtnislegierungen wandeln diffusionslos zwischen einer hochsymmetrischen Austenitphase (hohe T) und einer niedersymmetrischen Martensitphase um. Oberhalb der Austenit-Endtemperatur induziert mechanische Spannung den Martensit: Superelastizität mit bis zu 8 % reversibler Dehnung.

                Die Umwandlung setzt latente Wärme frei. Belastet man schnell (adiabatisch), erwärmt sich der Draht; beim Entlasten kühlt er sich um etwa denselben Betrag ab. NiTi-Drähte erreichen ΔT ≈ 25 K. Ein Bericht des US-Energieministeriums (2014) stufte die Elastokalorik als aussichtsreichste Alternative zur Dampfkompression ein.
            """,
                fm("Adiabatische Erwärmung", "ΔT_ad ≈ f L/c  (f = umgewandelter Anteil)")
            ),
            sec("Clausius-Clapeyron für Spannung", """
                Die Umwandlungsspannung steigt linear mit der Temperatur. Steigung und Umwandlungsdehnung bestimmen die Umwandlungsentropie; für NiTi dσ/dT ≈ 6–7 MPa/K.

                Herausforderungen: Spannungshysterese (Arbeitsverlust, senkt den COP), Ermüdung unter Zug (Druckbelastung verbessert die Lebensdauer drastisch), Wärmeübertragung aus dem Draht.
            """,
                fm("Clausius-Clapeyron", "dσ_tr/dT = −ρΔs_tr/ε_tr"),
                fm("Materialeffizienz", "COP_mat = Q/W_Hysterese")
            ),
            sec("Thermoelastischer Effekt und Gummi", """
                Auch ohne Phasenübergang ändert elastische Verformung die Temperatur (Kelvin, 1853): Metalle kühlen sich bei Zug leicht ab (Bruchteile eines Kelvins), weil sie sich beim Erwärmen ausdehnen. Gummi dagegen erwärmt sich beim Dehnen deutlich: Seine Elastizität ist entropisch — gestreckte Polymerketten haben weniger Konformationen. Bei starker Dehnung kristallisiert Naturkautschuk zusätzlich (ΔT bis ≈ 12 K).
            """,
                fm("Thermoelastischer Effekt", "ΔT = −(αT/ρc) Δσ"),
                fm("Entropische Elastizität", "F = −T (∂S/∂ℓ)_T")
            )
        ),
        chapter(
            "ca_magneto_baro", Topic.CALORIC, 2, "Magneto- und Barokalorik",
            "Gadolinium, Riesen-Magnetokalorik, Molekularfeld und kolossale Barokalorik.",
            listOf("magnetocaloric", "barocaloric"),
            sec("Magnetokalorischer Effekt", """
                Entdeckt von Weiss und Piccard (1917). Ein Magnetfeld richtet magnetische Momente aus; die Spinentropie sinkt. Die adiabatische Entmagnetisierung paramagnetischer Salze (Giauque, Debye 1926) war der erste Weg unter 1 K und ist bis heute Standard für Millikelvin-Kühlung.

                Raumtemperatur: Gadolinium (T_C ≈ 294 K) zeigt etwa 3 K pro Tesla. Riesen-Magnetokalorik an magnetostrukturellen Übergängen erster Ordnung: Gd₅Si₂Ge₂ (Pecharsky & Gschneidner, 1997), La(Fe,Si)₁₃H, MnFe(P,Si). Kommerzielle Prototypen nutzen Permanentmagnete (≈ 1–1,5 T) und aktive magnetische Regeneratoren.
            """,
                fm("Maximale Spinentropie", "S_max = R ln(2J + 1)  pro Mol"),
                fm("Molekularfeld", "M = M_s B_J(gμ_BJμ₀(H + λM)/k_BT)"),
                fm("Curie-Temperatur (MF)", "T_C = λ C,   C = μ₀ng²μ_B²J(J+1)/(3k_B)")
            ),
            sec("Barokalorik", """
                Hydrostatischer Druck wirkt auf das Volumen. Fern von Übergängen ist der Effekt klein (Gase sind die Ausnahme: Kompression heizt stark — das ist der klassische Kompressorkühlschrank!). An Übergängen mit großer Volumenänderung wird er riesig: Plastische Kristalle wie Neopentylglykol (NPG) zeigen „kolossale“ Barokalorik mit ΔS ≈ 400–500 J/(kg K), vergleichbar mit Kältemitteln. Nachteil: hohe Drücke (≈ 100 MPa) und Hysterese.
            """,
                fm("Linearer barokalorischer Effekt", "ΔT ≈ (Tvα_V/c_p) Δp"),
                fm("Übergang", "ΔT ≈ TΔs_tr/c_p")
            )
        ),
        chapter(
            "ca_multi", Topic.CALORIC, 3, "Multikalorik und kalorische Kühlung",
            "Gekoppelte Felder, Multiferroika, Kreisprozesse, Regeneratoren und Effizienz.",
            listOf("multicaloric", "caloric_cycle"),
            sec("Multikalorische Effekte", """
                In Materialien mit gekoppelten Freiheitsgraden (Multiferroika, magnetostrukturelle Übergänge) reagiert die Entropie auf mehrere Felder. Die quadratische Entwicklung der Gibbs-Energie enthält Kreuzterme wie den piezoelektrischen Koeffizienten d (E–σ), den magnetoelektrischen Koeffizienten α (E–H) oder die Magnetostriktion (H–σ).

                Die gesamte Entropieänderung bei gleichzeitigen Feldern ist nicht die Summe der Einzeleffekte: Die Temperaturabhängigkeit der Kopplung liefert einen Kreuzterm. Praktisch nützlich: Ein zweites Feld kann einen Übergang erster Ordnung verschieben und so die Hysterese umgehen oder das nötige Hauptfeld verringern (Beispiele: FeRh mit Feld und Druck, Ni-Mn-In-Heusler mit Feld und Spannung).
            """,
                fm("Kopplungsterm", "G ⊃ −d E σ − α E H − λ_ms H σ"),
                fm("Multikalorisches ΔS", "ΔS = ½χ′E² + ½s′σ² + d′Eσ  (′ = ∂/∂T)")
            ),
            sec("Kreisprozesse und Regeneration", """
                Brayton-Zyklus: adiabatisch Feld an → Wärme an die warme Seite abgeben → adiabatisch Feld aus → Wärme von der kalten Seite aufnehmen. Ericsson-Zyklus: isotherm statt adiabatisch Feld schalten, mit Regenerator.

                Weil ΔT_ad nur einige Kelvin beträgt, nutzen reale Geräte aktive Regeneratoren (AMR): Ein poröses Bett des Materials wird zyklisch geschaltet, während ein Fluid hin und her pumpt; so bildet sich ein Temperaturgradient über das Bett, der ein Vielfaches von ΔT_ad erreicht. Kaskaden verschiedener Materialien decken das Temperaturfenster ab.
            """,
                fm("Carnot-COP", "COP_C = T_k/(T_w − T_k)"),
                fm("Kälteleistung (Brayton, grob)", "Q̇_k ≈ m c (ΔT_ad − ΔT_Spanne) f")
            ),
            sec("Warum kalorische Kühlung?", """
                Dampfkompressionsanlagen nutzen Kältemittel mit hohem Treibhauspotential (GWP); die EU-F-Gas-Verordnung schränkt sie stark ein. Festkörperkühlung ist kältemittelfrei, leise und skalierbar. Ziel sind Gütegrade (COP/COP_Carnot) über 50 %, wie bei guten Kompressoren. Engpässe sind Materialermüdung, Hysterese, Wärmeübertragung und Kosten.
            """)
        )
    )
}

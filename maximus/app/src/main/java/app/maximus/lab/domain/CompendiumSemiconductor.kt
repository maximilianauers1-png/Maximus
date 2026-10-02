package app.maximus.lab.domain

internal object CompendiumSemiconductor {
    val chapters = listOf(
        chapter(
            "sc_bands", Topic.SEMICONDUCTOR, 1, "Kristalle und Bänderstruktur",
            "Bloch-Theorem, Bänder, effektive Masse, Löcher und Bandlücken.",
            listOf("intrinsic"),
            sec("Bloch-Elektronen", """
                In einem periodischen Gitterpotential sind die Eigenzustände Bloch-Wellen: eine ebene Welle mal eine gitterperiodische Funktion. Der Kristallimpuls ħk ist nur bis auf reziproke Gittervektoren bestimmt; alle Information steckt in der ersten Brillouin-Zone.

                Die Energien bilden Bänder Eₙ(k), getrennt durch Lücken. Ein Festkörper ist Isolator oder Halbleiter, wenn das oberste besetzte Band (Valenzband) voll ist und eine Lücke E_g zum leeren Leitungsband besteht; Halbleiter haben E_g ≲ 3–4 eV.
            """,
                fm("Bloch-Theorem", "ψ_{nk}(r) = e^{ik·r} u_{nk}(r),   u(r + R) = u(r)"),
                fm("Gruppengeschwindigkeit", "v = (1/ħ) ∇_k E(k)")
            ),
            sec("Effektive Masse und Löcher", """
                Nahe den Bandkanten ist E(k) näherungsweise parabolisch; die Krümmung definiert die effektive Masse. Elektronen am Leitungsbandminimum verhalten sich wie freie Teilchen mit m*.

                Ein fehlendes Elektron im fast vollen Valenzband verhält sich wie ein positiv geladenes Teilchen mit positiver effektiver Masse: das Loch.
            """,
                fm("Effektive Masse", "1/m* = (1/ħ²) ∂²E/∂k²"),
                fm("Parabolische Näherung", "E(k) = E_C + ħ²k²/(2m*)"),
                fm("Bewegungsgleichung", "ħ dk/dt = F = −e(E + v × B)")
            ),
            sec("Direkte und indirekte Halbleiter", """
                Liegen Valenzbandmaximum und Leitungsbandminimum beim selben k, ist die Lücke direkt (GaAs, GaN, InP): Photonen können Übergänge allein vermitteln, daher effiziente LEDs und Laser. Bei indirekten Halbleitern (Si, Ge, SiC) braucht ein optischer Übergang zusätzlich ein Phonon für den Impulsausgleich; Lichtemission ist ineffizient.

                Die Bandlücke sinkt mit der Temperatur (Gitterausdehnung, Elektron-Phonon-Kopplung), empirisch nach Varshni.
            """,
                fm("Varshni", "E_g(T) = E_g(0) − αT²/(T + β)"),
                fm("Absorptionskante", "λ_g = hc/E_g ≈ 1240 nm·eV / E_g")
            )
        ),
        chapter(
            "sc_carriers", Topic.SEMICONDUCTOR, 1, "Ladungsträgerstatistik und Dotierung",
            "Zustandsdichte, Fermi-Niveau, intrinsische Dichte, Donatoren und Akzeptoren.",
            listOf("intrinsic", "doping", "fermi_dirac"),
            sec("Zustandsdichte und Besetzung", """
                Die Zahl der Zustände pro Energie und Volumen wächst nahe der Bandkante wie √(E − E_C). Die Elektronendichte ergibt sich aus Zustandsdichte mal Fermi-Dirac-Besetzung. Ist das Fermi-Niveau mehr als ≈ 3k_BT von den Bandkanten entfernt (nicht entartet), wird das Integral zur effektiven Zustandsdichte N_C mal einem Boltzmann-Faktor.
            """,
                fm("Zustandsdichte", "D(E) = (1/2π²)(2m*/ħ²)^{3/2} √(E − E_C)"),
                fm("Elektronendichte", "n = N_C e^{−(E_C − E_F)/k_BT}"),
                fm("Löcherdichte", "p = N_V e^{−(E_F − E_V)/k_BT}"),
                fm("Effektive Zustandsdichte", "N_C = 2(m*k_BT/2πħ²)^{3/2}")
            ),
            sec("Massenwirkungsgesetz und Eigenleitung", """
                Das Produkt np hängt nicht von der Lage des Fermi-Niveaus ab, sondern nur von Material und Temperatur: np = nᵢ². Im undotierten Halbleiter gilt n = p = nᵢ. Bei Raumtemperatur: Si ≈ 10¹⁰ cm⁻³, Ge ≈ 2·10¹³ cm⁻³, GaAs ≈ 2·10⁶ cm⁻³ — bei 5·10²² Atomen pro cm³.
            """,
                fm("Massenwirkungsgesetz", "np = nᵢ² = N_C N_V e^{−E_g/k_BT}"),
                fm("Intrinsisches Niveau", "E_i = (E_C + E_V)/2 + (k_BT/2) ln(N_V/N_C)")
            ),
            sec("Dotierung", """
                Fünfwertige Fremdatome (P, As in Si) geben ein Elektron leicht ab (Ionisationsenergie ≈ 45 meV, wasserstoffähnlich mit m* und ε_r): Donatoren, n-Typ. Dreiwertige (B) nehmen eines auf: Akzeptoren, p-Typ.

                Temperaturverlauf der Elektronendichte eines n-Halbleiters: Ausfrierbereich (tiefe T, Donatoren nicht ionisiert) → Erschöpfungsbereich (n ≈ N_D, technisch genutzt) → intrinsischer Bereich (nᵢ > N_D, hohe T).
            """,
                fm("Neutralität", "n + N_A⁻ = p + N_D⁺"),
                fm("n-Typ (Erschöpfung)", "n ≈ N_D,   p = nᵢ²/N_D"),
                fm("Fermi-Niveau", "E_F − E_i = k_BT ln(n/nᵢ)"),
                fm("Donator-Bindung", "E_D ≈ 13,6 eV · (m*/mₑ)/ε_r²")
            )
        ),
        chapter(
            "sc_transport", Topic.SEMICONDUCTOR, 2, "Ladungstransport",
            "Drift, Beweglichkeit, Diffusion, Einstein-Relation, Rekombination und Hall-Effekt.",
            listOf("doping", "hall", "drude"),
            sec("Drift und Beweglichkeit", """
                Im Feld werden Ladungsträger beschleunigt und durch Stöße mit Phononen und Störstellen gebremst; im Mittel stellt sich eine Driftgeschwindigkeit proportional zum Feld ein. Die Beweglichkeit μ = eτ/m* sinkt mit der Temperatur (Phononenstreuung, ∝ T^{−3/2}) und mit der Dotierung (Störstellenstreuung). Bei hohen Feldern sättigt v (≈ 10⁷ cm/s in Si).
            """,
                fm("Driftgeschwindigkeit", "v_d = μE,   μ = eτ/m*"),
                fm("Leitfähigkeit", "σ = e(nμ_n + pμ_p)"),
                fm("Matthiessen-Regel", "1/μ = 1/μ_Gitter + 1/μ_Störstellen")
            ),
            sec("Diffusion", """
                Konzentrationsgradienten treiben einen Diffusionsstrom. Im Gleichgewicht heben sich Drift und Diffusion auf; daraus folgt die Einstein-Relation zwischen Diffusionskonstante und Beweglichkeit.
            """,
                fm("Stromdichte Elektronen", "j_n = enμ_n E + eD_n ∇n"),
                fm("Einstein-Relation", "D = μ k_BT/e"),
                fm("Diffusionslänge", "L = √(Dτ)")
            ),
            sec("Generation und Rekombination", """
                Überschussladungsträger rekombinieren mit einer Lebensdauer τ. Mechanismen: strahlend (direkt, wichtig in GaAs), über Störstellen nach Shockley-Read-Hall (dominant in Si; tiefe Niveaus in der Lückenmitte sind die effizientesten Fallen), Auger (bei hoher Dichte). Die Kontinuitätsgleichung bilanziert alles.
            """,
                fm("Kontinuitätsgleichung", "∂n/∂t = (1/e)∇·j_n + G − R"),
                fm("Niedrige Injektion", "R = Δn/τ"),
                fm("SRH", "R = (np − nᵢ²)/(τ_p(n + n₁) + τ_n(p + p₁))")
            ),
            sec("Hall-Effekt", """
                Im Magnetfeld lenkt die Lorentzkraft die Träger seitlich ab, bis ein Querfeld sie kompensiert. Das Vorzeichen der Hall-Spannung verrät den Trägertyp, ihre Größe die Dichte. Zusammen mit dem spezifischen Widerstand folgt die Beweglichkeit. Im zweidimensionalen Elektronengas bei tiefen T wird der Hall-Widerstand quantisiert: R_xy = h/(νe²) (von Klitzing, 1985).
            """,
                fm("Hall-Spannung", "U_H = IB/(ned)"),
                fm("Hall-Konstante", "R_H = 1/(ne)"),
                fm("Quanten-Hall", "R_xy = h/(νe²) = R_K/ν")
            )
        ),
        chapter(
            "sc_pn", Topic.SEMICONDUCTOR, 2, "pn-Übergang und Diode",
            "Raumladungszone, Diffusionsspannung, Shockley-Gleichung, Durchbruch und Kapazitäten.",
            listOf("pn", "diode"),
            sec("Der Übergang im Gleichgewicht", """
                Bringt man p- und n-Gebiet zusammen, diffundieren Majoritätsträger hinüber und rekombinieren. Zurück bleiben ortsfeste ionisierte Dotieratome: die Raumladungszone (RLZ). Ihr Feld treibt einen Driftstrom, der den Diffusionsstrom im Gleichgewicht genau kompensiert. Das Fermi-Niveau ist überall konstant; die Bänder sind um eV_bi verbogen.

                In der Schottky-Näherung (vollständige Verarmung) folgt aus der Poisson-Gleichung ein dreieckiges Feld und ein parabolisches Potential.
            """,
                fm("Diffusionsspannung", "V_bi = (k_BT/e) ln(N_A N_D/nᵢ²)"),
                fm("RLZ-Weite", "W = √(2ε(V_bi − V)(N_A + N_D)/(e N_A N_D))"),
                fm("Ladungsneutralität", "N_A x_p = N_D x_n"),
                fm("Sperrschichtkapazität", "C_j = εA/W ∝ (V_bi − V)^{−1/2}")
            ),
            sec("Kennlinie", """
                In Durchlassrichtung sinkt die Barriere, und injizierte Minoritäten diffundieren in die neutralen Gebiete (Randdichte n_p = n_p0 e^{eV/k_BT}). In Sperrrichtung fließt nur der kleine, durch thermische Generation bestimmte Sättigungsstrom.

                Reale Dioden: Idealitätsfaktor n zwischen 1 (Diffusion) und 2 (Rekombination in der RLZ), Serienwiderstand bei hohem Strom, Durchbruch in Sperrrichtung durch Zener-Tunneln (hohe Dotierung, < 5 V) oder Lawinenmultiplikation.
            """,
                fm("Shockley-Gleichung", "I = I_s (e^{eV/nk_BT} − 1)"),
                fm("Sättigungsstrom", "I_s = eAnᵢ² (D_n/(L_n N_A) + D_p/(L_p N_D))"),
                fm("Diffusionskapazität", "C_d = (e/nk_BT) I τ")
            ),
            sec("Metall-Halbleiter-Kontakte", """
                Je nach Austrittsarbeiten entsteht ein gleichrichtender Schottky-Kontakt (Barriere Φ_B, schnelle Majoritätsträger-Diode) oder ein ohmscher Kontakt (hohe Dotierung ermöglicht Tunneln durch die dünne Barriere).
            """,
                fm("Schottky-Strom", "I = A A** T² e^{−eΦ_B/k_BT}(e^{eV/nk_BT} − 1)")
            )
        ),
        chapter(
            "sc_opto", Topic.SEMICONDUCTOR, 2, "Optoelektronik und Solarzellen",
            "Absorption, LED, Laser, Photodetektor und Photovoltaik.",
            listOf("solar", "intrinsic"),
            sec("Licht und Halbleiter", """
                Photonen mit hν > E_g erzeugen Elektron-Loch-Paare. Der Absorptionskoeffizient steigt bei direkten Halbleitern steil an (α ∝ √(hν − E_g)), bei indirekten flacher; Silizium braucht daher ~100 μm Dicke für vollständige Absorption, GaAs nur wenige μm.

                • LED: strahlende Rekombination in Durchlassrichtung; Farbe über E_g (blaue GaN-LED: Nobelpreis 2014).
                • Laserdiode: Besetzungsinversion im aktiven Gebiet, optische Rückkopplung durch Spiegel.
                • Photodiode: in Sperrrichtung; Photostrom ∝ Lichtleistung, Responsivität R = ηe/(hν).
            """,
                fm("Lambert-Beer", "I(x) = I₀ e^{−αx}"),
                fm("Responsivität", "R = η eλ/(hc)")
            ),
            sec("Solarzelle", """
                Die beleuchtete pn-Diode trennt erzeugte Paare im Feld der RLZ. Kennlinie: Diodenkennlinie minus Photostrom. Kenngrößen sind Kurzschlussstrom, Leerlaufspannung, Füllfaktor und Wirkungsgrad.

                Shockley-Queisser-Grenze: Für eine Einfachzelle unter AM1.5 maximal ≈ 33 % bei E_g ≈ 1,34 eV. Verluste: Photonen unter E_g gehen durch, Überschussenergie thermalisiert, strahlende Rekombination. Tandemzellen (Perowskit/Si > 33 %) umgehen die Grenze.
            """,
                fm("Solarzellen-Kennlinie", "I = I_L − I₀(e^{eV/nk_BT} − 1)"),
                fm("Leerlaufspannung", "V_oc = (nk_BT/e) ln(I_L/I₀ + 1)"),
                fm("Füllfaktor, Wirkungsgrad", "FF = P_max/(V_oc I_sc),   η = FF V_oc I_sc/P_in")
            )
        ),
        chapter(
            "sc_transistor", Topic.SEMICONDUCTOR, 3, "Transistoren",
            "MOS-Struktur, MOSFET-Kennlinien, Skalierung und Bipolartransistor.",
            listOf("mosfet"),
            sec("MOS-Kondensator", """
                Metall (Gate), Oxid, Halbleiter. Mit steigender Gatespannung durchläuft ein p-Substrat Akkumulation, Verarmung und Inversion: Ab der Schwellspannung bildet sich an der Oberfläche ein leitfähiger Elektronenkanal. Starke Inversion setzt ein, wenn das Oberflächenpotential 2φ_F erreicht.
            """,
                fm("Oxidkapazität", "C_ox = ε_ox/t_ox"),
                fm("Schwellspannung", "V_th = V_FB + 2φ_F + √(2εeN_A 2φ_F)/C_ox"),
                fm("Fermi-Potential", "φ_F = (k_BT/e) ln(N_A/nᵢ)")
            ),
            sec("MOSFET", """
                Die Gatespannung steuert die Kanalladung, die Drainspannung treibt den Strom. Im linearen Bereich wirkt der Kanal als steuerbarer Widerstand, ab V_DS = V_GS − V_th schnürt er am Drain ab (Sättigung). Unterhalb der Schwelle fällt der Strom exponentiell, mindestens 60 mV pro Dekade bei 300 K (Boltzmann-Tyrannei).

                Skalierung (Dennard): Alle Längen und Spannungen um 1/κ verkleinern hält die Leistungsdichte konstant. Heute begrenzen Leckströme und Kurzkanaleffekte; FinFET und Gate-all-around verbessern die Gate-Kontrolle.
            """,
                fm("Linearer Bereich", "I_D = μC_ox(W/L)[(V_GS − V_th)V_DS − V_DS²/2]"),
                fm("Sättigung", "I_D = ½μC_ox(W/L)(V_GS − V_th)²"),
                fm("Subthreshold-Swing", "S = ln10 · (k_BT/e)(1 + C_D/C_ox) ≥ 60 mV/dec")
            ),
            sec("Bipolartransistor", """
                Zwei pn-Übergänge (npn): Der Emitter injiziert Elektronen in die dünne Basis, die fast alle zum Kollektor diffundieren. Kleine Basisströme steuern große Kollektorströme; die Stromverstärkung β = I_C/I_B liegt typisch bei 100.
            """,
                fm("Kollektorstrom", "I_C = I_S e^{V_BE/V_T}"),
                fm("Steilheit", "g_m = I_C/V_T")
            )
        )
    )
}

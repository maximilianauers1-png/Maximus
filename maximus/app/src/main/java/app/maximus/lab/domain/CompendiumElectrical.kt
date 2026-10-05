package app.maximus.lab.domain

/** Electrical engineering: circuit theory, analogue and digital electronics, and how chips are designed and made. */
internal object CompendiumElectrical {
    private val T = Topic.ELECTRICAL

    val chapters: List<Chapter> = course(
        "Schaltungstechnik",
        chapter(
            "ee_dc", T, 1, "Gleichstromnetzwerke",
            "Kirchhoff, Knoten- und Maschenanalyse, Superposition, Thevenin/Norton, Leistungsanpassung.",
            listOf("rc"),
            sec("Kirchhoffsche Regeln", """
                Knotenregel: Die Summe der Ströme in einen Knoten ist null (Ladungserhaltung). Maschenregel: Die Summe der Spannungen um eine geschlossene Masche ist null (Energieerhaltung, konservatives Feld). Zusammen mit den Bauteilgleichungen (Ohm) bestimmen sie jedes lineare Netzwerk.

                Systematisch: Knotenpotentialverfahren — für n Knoten n − 1 Gleichungen in den Potentialen, in Matrixform G·φ = I. So arbeiten auch Schaltungssimulatoren (SPICE: Modified Nodal Analysis).
            """,
                fm("Knotenregel", "Σ I_k = 0"),
                fm("Maschenregel", "Σ U_k = 0"),
                fm("Knotenanalyse", "G φ = I_q"),
                fm("Spannungsteiler", "U₂ = U · R₂/(R₁ + R₂)"),
                fm("Stromteiler", "I₁ = I · R₂/(R₁ + R₂)")
            ),
            sec("Ersatzquellen und Superposition", """
                Jedes lineare Zweipol-Netzwerk verhält sich an zwei Klemmen wie eine Spannungsquelle mit Innenwiderstand (Thevenin) oder eine Stromquelle mit Parallelwiderstand (Norton). In linearen Netzwerken überlagern sich die Wirkungen mehrerer Quellen (Superposition): Man rechnet jede Quelle allein, die anderen ersetzt (Spannungsquelle → Kurzschluss, Stromquelle → Leerlauf).
            """,
                fm("Thevenin", "U_th = U_Leerlauf,  R_th = U_Leerlauf/I_Kurzschluss"),
                fm("Norton", "I_N = U_th/R_th"),
                fm("Leistungsanpassung", "P_max bei R_L = R_th:  P_max = U_th²/(4R_th)")
            ),
            sec("Leistung und Energie", """
                Elektrische Leistung ist das Produkt aus Spannung und Strom. Bei Leistungsanpassung erhält die Last die maximale Leistung, der Wirkungsgrad ist dann aber nur 50 % — in der Energietechnik will man deshalb R_L ≫ R_th, in der Nachrichtentechnik Anpassung.
            """,
                fm("Leistung", "P = U I = I²R = U²/R"),
                fm("Wirkungsgrad", "η = R_L/(R_L + R_th)")
            )
        ),
        chapter(
            "ee_ac", T, 2, "Wechselstrom, Filter und Resonanz",
            "Zeiger, Impedanz, Wirk-, Blind- und Scheinleistung, RC/RL-Filter, Bode-Diagramm, Schwingkreis.",
            listOf("rlc"),
            sec("Komplexe Wechselstromrechnung", """
                Für sinusförmige Größen ersetzt man u(t) = Re{Û e^{iωt}} durch den komplexen Zeiger Û. Ableitungen werden zu Multiplikationen mit iω, Bauteile zu Impedanzen. Damit gelten Kirchhoff und Ohm wie im Gleichstromfall, nur komplex.
            """,
                fm("Impedanzen", "Z_R = R,  Z_L = iωL,  Z_C = 1/(iωC)"),
                fm("Effektivwert", "U_eff = Û/√2"),
                fm("Leistungen", "S = U_eff I_eff,  P = S cos φ,  Q = S sin φ")
            ),
            sec("Filter und Bode-Diagramm", """
                Ein RC-Tiefpass lässt tiefe Frequenzen durch und dämpft oberhalb der Grenzfrequenz mit −20 dB/Dekade (Filter 1. Ordnung). Das Bode-Diagramm zeigt Betrag in dB und Phase über log ω; Pole und Nullstellen erzeugen Knicke um ∓20 dB/Dekade.
            """,
                fm("RC-Tiefpass", "H(iω) = 1/(1 + iωRC),  ω_g = 1/(RC)"),
                fm("Dezibel", "|H|_dB = 20 log₁₀|H|"),
                fm("Grenzfrequenz", "|H(ω_g)| = 1/√2 ≙ −3 dB,  φ = −45°")
            ),
            sec("Schwingkreis", """
                Im Serienschwingkreis heben sich die Blindwiderstände von L und C bei der Resonanzfrequenz auf; der Strom ist maximal und nur durch R begrenzt. Die Güte Q beschreibt die Schärfe der Resonanz und die Spannungsüberhöhung an L und C.
            """,
                fm("Resonanzfrequenz", "ω₀ = 1/√(LC)"),
                fm("Güte (Serie)", "Q = (1/R)√(L/C) = ω₀/Δω")
            )
        ),
        chapter(
            "ee_opamp", T, 2, "Operationsverstärker",
            "Ideale Regeln, invertierender und nichtinvertierender Verstärker, Integrator, Verstärkungs-Bandbreite, Slew Rate.",
            emptyList(),
            sec("Der ideale OPV mit Gegenkopplung", """
                Ein Operationsverstärker hat sehr hohe Leerlaufverstärkung A, hohe Eingangsimpedanz und niedrige Ausgangsimpedanz. Mit Gegenkopplung stellt er seinen Ausgang so ein, dass die Differenzspannung zwischen den Eingängen nahezu null wird (virtueller Kurzschluss), und es fließt kein Eingangsstrom. Diese zwei Regeln genügen für die meisten Schaltungen.
            """,
                fm("Regeln", "U₊ ≈ U₋,  I₊ = I₋ ≈ 0"),
                fm("Invertierend", "U_a/U_e = −R_f/R₁"),
                fm("Nichtinvertierend", "U_a/U_e = 1 + R_f/R₁"),
                fm("Integrator", "U_a = −(1/(RC)) ∫ U_e dt"),
                fm("Summierer", "U_a = −R_f Σ U_k/R_k")
            ),
            sec("Grenzen des realen OPV", """
                Die Leerlaufverstärkung fällt oberhalb einer niedrigen Eckfrequenz mit −20 dB/Dekade; das Produkt aus Verstärkung und Bandbreite ist konstant. Große Signale sind durch die Slew Rate begrenzt. Dazu kommen Offsetspannung, Biasströme, Rauschen und Versorgungsgrenzen.
            """,
                fm("Verstärkungs-Bandbreite", "GBW = A_v · f_−3dB = const"),
                fm("Slew-Rate-Grenze", "f_max = SR/(2π Û)"),
                fm("Gegengekoppelte Verstärkung", "A_v = A/(1 + Aβ) ≈ 1/β")
            )
        ),
        chapter(
            "ee_analog", T, 3, "Analog-Mixed-Signal und Rauschen",
            "Abtasttheorem, ADC/DAC, Quantisierungsrauschen, thermisches Rauschen, kT/C, Schaltregler, PLL.",
            emptyList(),
            sec("Abtastung und Wandlung", """
                Ein Signal mit Bandbreite B lässt sich aus Abtastwerten rekonstruieren, wenn f_s > 2B (Nyquist-Shannon); sonst falten sich höhere Frequenzen zurück (Aliasing), daher Anti-Aliasing-Filter vor dem ADC. Ein idealer N-Bit-Wandler hat Quantisierungsrauschen, das das Signal-Rausch-Verhältnis begrenzt. Architekturen: SAR (mittlere Rate, effizient), Delta-Sigma (hohe Auflösung durch Überabtastung und Rauschformung), Flash (sehr schnell, 2^N Komparatoren), Pipeline.
            """,
                fm("Abtasttheorem", "f_s > 2 f_max"),
                fm("Quantisierungsstufe", "LSB = U_FS/2^N"),
                fm("Ideales SNR", "SNR = 6,02 N + 1,76 dB"),
                fm("Effektive Bits", "ENOB = (SINAD − 1,76)/6,02")
            ),
            sec("Rauschen", """
                Jeder Widerstand rauscht thermisch (Johnson-Nyquist) mit weißem Spektrum. Ein Abtastkondensator hält eine Rauschspannung, die nur von C abhängt (kT/C), was die Kondensatorgröße in präzisen Wandlern nach unten begrenzt. Ströme über Barrieren (Dioden, Transistoren) zeigen Schrotrauschen; bei tiefen Frequenzen dominiert 1/f-Rauschen.
            """,
                fm("Thermisches Rauschen", "⟨u²⟩ = 4 k_BT R Δf"),
                fm("kT/C-Rauschen", "u_rms = √(k_BT/C)"),
                fm("Schrotrauschen", "⟨i²⟩ = 2 e I Δf")
            ),
            sec("Leistungselektronik und Takterzeugung", """
                Schaltregler wandeln Spannungen mit hohem Wirkungsgrad, indem ein Transistor schnell ein- und ausschaltet und eine Spule die Energie zwischenspeichert. Im Tiefsetzsteller (Buck) ist die Ausgangsspannung das Tastverhältnis mal der Eingangsspannung. Phasenregelschleifen (PLL) erzeugen aus einem Referenztakt hochfrequente, phasenstarre Takte für Prozessoren und Funkchips.
            """,
                fm("Buck-Wandler", "U_a = D U_e"),
                fm("Boost-Wandler", "U_a = U_e/(1 − D)"),
                fm("PLL-Ausgang", "f_out = N f_ref")
            )
        )
    ) + course(
        "Halbleiterbauelemente und Digitaltechnik",
        chapter(
            "ee_devices", T, 2, "Dioden und Transistoren in Schaltungen",
            "Diodenmodelle, MOSFET-Kennlinien, Kleinsignalmodell, Verstärkerstufen, Bipolartransistor.",
            listOf("mosfet", "diode"),
            sec("Diode", """
                Die Shockley-Gleichung beschreibt die Kennlinie; für Handrechnungen genügt oft ein Konstantspannungsmodell (≈ 0,6–0,7 V bei Silizium). Anwendungen: Gleichrichter, Freilauf, Spannungsbegrenzung, Zener-Referenzen. Der Kleinsignal-Widerstand im Arbeitspunkt ist U_T/I.
            """,
                fm("Shockley", "I = I_s (e^{U/(nU_T)} − 1)"),
                fm("Temperaturspannung", "U_T = k_BT/e ≈ 25,9 mV (300 K)"),
                fm("Kleinsignalwiderstand", "r_d = n U_T/I")
            ),
            sec("MOSFET", """
                Der MOSFET leitet, wenn die Gate-Source-Spannung die Schwellspannung U_th übersteigt. Im Triodenbereich verhält er sich wie ein spannungsgesteuerter Widerstand, in der Sättigung wie eine Stromquelle (Verstärkerbetrieb). Moderne Kurzkanaltransistoren folgen nicht mehr dem quadratischen Gesetz (Geschwindigkeitssättigung), aber es bleibt das Lehrbuchmodell.
            """,
                fm("Sättigung (Langkanal)", "I_D = ½ μ C_ox (W/L)(U_GS − U_th)²"),
                fm("Steilheit", "g_m = ∂I_D/∂U_GS = 2I_D/(U_GS − U_th)"),
                fm("Kanallängenmodulation", "r_o ≈ 1/(λ I_D)"),
                fm("Unterschwellsteigung", "S = n U_T ln 10 ≥ 60 mV/Dekade (300 K)")
            ),
            sec("Verstärkerstufen", """
                Source-Schaltung: invertierende Spannungsverstärkung. Drain-Schaltung (Source-Folger): Verstärkung ≈ 1, niedrige Ausgangsimpedanz (Puffer). Gate-Schaltung: niedriger Eingangswiderstand, hohe Bandbreite. Differenzverstärker unterdrücken Gleichtaktstörungen (CMRR) und bilden die Eingangsstufe jedes OPV.
            """,
                fm("Source-Schaltung", "A_v ≈ −g_m (R_D ∥ r_o)"),
                fm("Intrinsische Verstärkung", "A_0 = g_m r_o"),
                fm("Bipolar: Steilheit", "g_m = I_C/U_T")
            )
        ),
        chapter(
            "ee_digital", T, 2, "Digitaltechnik und CMOS-Logik",
            "Boolesche Algebra, CMOS-Gatter, Flipflops, Timing (Setup/Hold, kritischer Pfad), Leistungsaufnahme.",
            emptyList(),
            sec("Logik und CMOS-Gatter", """
                Boolesche Funktionen lassen sich aus NAND (oder NOR) allein aufbauen. In CMOS besteht jedes Gatter aus einem Pull-up-Netz aus pMOS und einem komplementären Pull-down-Netz aus nMOS; im statischen Zustand leitet nur eines, daher fließt (bis auf Leckströme) kein Querstrom. Der Inverter hat eine steile Übertragungskennlinie mit großem Störabstand.
            """,
                fm("De Morgan", "¬(A ∧ B) = ¬A ∨ ¬B"),
                fm("Gatterverzögerung (RC)", "t_p ≈ 0,69 R_on C_L"),
                fm("Fan-out-of-4", "FO4 als technologieneutrales Verzögerungsmaß")
            ),
            sec("Sequentielle Logik und Timing", """
                Flipflops speichern ein Bit bei der Taktflanke. Damit eine synchrone Schaltung funktioniert, muss das Signal zwischen zwei Registern innerhalb einer Taktperiode stabil ankommen: Taktperiode ≥ Clock-to-Q + Logiklaufzeit + Setup-Zeit (+ Clock-Skew). Die längste solche Kette ist der kritische Pfad; er bestimmt die maximale Taktfrequenz. Hold-Verletzungen (zu schnelle Pfade) sind frequenzunabhängig und gefährlicher, weil man sie nicht durch langsameren Takt beheben kann.
            """,
                fm("Setup-Bedingung", "T_clk ≥ t_cq + t_logic,max + t_setup + t_skew"),
                fm("Hold-Bedingung", "t_cq + t_logic,min ≥ t_hold + t_skew"),
                fm("Pipelining", "Durchsatz ↑, Latenz in Takten ↑")
            ),
            sec("Leistungsaufnahme", """
                Dynamische Leistung entsteht beim Umladen der Lastkapazitäten, proportional zu Aktivität, Kapazität, Spannung zum Quadrat und Frequenz — deshalb ist Spannungsabsenkung der stärkste Hebel (DVFS). Statische Leistung kommt von Leckströmen (Unterschwelle, Gate-Tunneln). Seit etwa 2005 sinkt die Versorgungsspannung kaum noch (Ende des Dennard-Scaling): Leistungsdichte statt Transistorzahl begrenzt die Taktfrequenz, daher Mehrkern- und Spezialprozessoren.
            """,
                fm("Dynamische Leistung", "P_dyn = α C U² f"),
                fm("Statische Leistung", "P_stat = U I_leak"),
                fm("Energie pro Schaltvorgang", "E = C U²  (½ in C gespeichert, ½ im Widerstand verheizt)")
            )
        )
    ) + course(
        "Chip Engineering",
        chapter(
            "ee_fab", T, 3, "Halbleiterfertigung",
            "Wafer, Oxidation, Lithografie (DUV/EUV), Ätzen, Abscheidung, Implantation, CMP, Metallisierung, FinFET/GAA, Ausbeute.",
            emptyList(),
            sec("Der Prozessfluss", """
                Ein Chip entsteht in hunderten Schritten auf einem Siliziumwafer (heute 300 mm). Front-End-of-Line (FEOL): Transistoren (Isolation durch Shallow Trench Isolation, Gate-Stapel, Source/Drain durch Implantation und Ausheilen). Back-End-of-Line (BEOL): bis über 15 Metalllagen aus Kupfer in Damascene-Technik mit Low-k-Dielektrika verdrahten die Transistoren.

                Grundoperationen: Schicht aufbringen (CVD, PVD, ALD für atomlagengenaue Filme), strukturieren (Lithografie + Ätzen), dotieren (Ionenimplantation), planarisieren (chemisch-mechanisches Polieren, CMP).
            """,
                fm("Prozessschleife", "Abscheiden → Belichten → Entwickeln → Ätzen → Reinigen")
            ),
            sec("Lithografie", """
                Die kleinste abbildbare Strukturbreite folgt aus dem Rayleigh-Kriterium. Hebel: kürzere Wellenlänge (193 nm ArF-Immersion, 13,5 nm EUV), größere numerische Apertur (Immersion: NA ≈ 1,35; High-NA-EUV: 0,55) und kleinere Prozessfaktoren k₁ durch Tricks (Phasenmasken, OPC, Mehrfachbelichtung). EUV benötigt reflektive Optiken im Vakuum und Zinn-Plasma-Quellen.
            """,
                fm("Rayleigh-Auflösung", "CD = k₁ λ/NA"),
                fm("Schärfentiefe", "DOF = k₂ λ/NA²")
            ),
            sec("Transistorarchitekturen", """
                Planare MOSFETs verloren bei kurzen Kanälen die Kontrolle des Gates über den Kanal (Kurzkanaleffekte, Leckströme). FinFETs (ab 22 nm) umschließen den Kanal von drei Seiten, Gate-All-Around-Nanosheets (ab ≈ 3–2 nm-Generationen) von allen Seiten. High-k-Gatedielektrika (HfO₂) mit Metallgates ersetzten SiO₂, damit das Gate dünn wirken kann, ohne zu tunneln. „Nodes“ wie 3 nm sind Marketingnamen, keine physikalische Gatelänge.
            """,
                fm("Gatekapazität", "C_ox = ε_ox/t_ox,  EOT = t_highk · 3,9/ε_highk")
            ),
            sec("Ausbeute und Kosten", """
                Zufällige Defekte mit Dichte D₀ zerstören Chips; die Ausbeute sinkt exponentiell mit der Chipfläche. Deshalb sind große Chips teuer und Chiplets (mehrere kleine Dies im Gehäuse, verbunden über Interposer oder Hybrid Bonding) attraktiv.
            """,
                fm("Poisson-Ausbeute", "Y = e^{−D₀ A}"),
                fm("Murphy-Modell", "Y = ((1 − e^{−D₀A})/(D₀A))²"),
                fm("Dies pro Wafer (grob)", "N ≈ π(d/2)²/A − πd/√(2A)")
            )
        ),
        chapter(
            "ee_design", T, 3, "Chipentwurf: von RTL bis Tape-out",
            "Hardwarebeschreibung, Verifikation, Synthese, Place & Route, Taktbaum, Timing-Analyse, DRC/LVS, Test, FPGA vs. ASIC.",
            emptyList(),
            sec("Der digitale Entwurfsablauf", """
                1. Spezifikation und Architektur (Durchsatz, Latenz, Fläche, Leistung).
                2. RTL-Beschreibung in Verilog/SystemVerilog oder VHDL: Register und kombinatorische Logik dazwischen.
                3. Verifikation: Simulation mit Testbenches, constrained-random, Coverage, formale Verifikation; oft der größte Aufwand.
                4. Logiksynthese: RTL → Netzliste aus Standardzellen der Bibliothek unter Timing-Constraints.
                5. Physical Design: Floorplan, Platzierung, Taktbaumsynthese, Verdrahtung.
                6. Signoff: statische Timing-Analyse über alle Corner (Prozess, Spannung, Temperatur), Leistungs- und IR-Drop-Analyse, DRC (Designregeln) und LVS (Layout = Schaltplan).
                7. Tape-out: Maskendaten (GDSII/OASIS) gehen an die Fab.
            """,
                fm("Optimierungsziel", "PPA: Performance, Power, Area")
            ),
            sec("Statische Timing-Analyse", """
                STA prüft alle Pfade ohne Simulation von Eingangsmustern: Für jeden Pfad wird die Ankunftszeit gegen die geforderte Zeit verglichen; die Differenz heißt Slack. Negativer Slack ist eine Verletzung. Variationen werden über Corner und statistische Verfahren (OCV, POCV) abgedeckt.
            """,
                fm("Slack", "Slack = t_required − t_arrival")
            ),
            sec("Test und Fertigungsprüfung", """
                Gefertigte Chips müssen auf Defekte geprüft werden. Design-for-Test fügt Scan-Ketten ein: Alle Flipflops werden im Testmodus zu einem Schieberegister verbunden, sodass Testmuster (ATPG, Stuck-at- und Übergangsfehlermodelle) eingeschoben und Antworten ausgelesen werden können. Speicher erhalten eingebaute Selbsttests (BIST).
            """,
                fm("Fehlerabdeckung", "FC = erkannte Fehler / modellierte Fehler")
            ),
            sec("FPGA, ASIC und Spezialprozessoren", """
                FPGAs sind rekonfigurierbar (LUTs, Flipflops, DSP-Blöcke, Block-RAM) und ideal für Prototypen und kleine Stückzahlen; ASICs sind schneller und sparsamer, aber mit hohen Einmalkosten (Masken, Entwurf). KI-Beschleuniger nutzen systolische Arrays für Matrixmultiplikation und große On-Chip-Speicher, weil Datenbewegung mehr Energie kostet als Rechnen.
            """,
                fm("Amdahl", "S = 1/((1 − p) + p/N)"),
                fm("Roofline", "Leistung = min(Spitzenrechenleistung, Bandbreite · arithmetische Intensität)")
            )
        )
    )
}

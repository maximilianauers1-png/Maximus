package app.maximus.lab.domain

internal object CompendiumElectro {
    val chapters = listOf(
        chapter(
            "el_static", Topic.ELECTRO, 1, "Elektrostatik",
            "Ladungen, Felder, Potentiale, der Gaußsche Satz und Kondensatoren.",
            listOf("coulomb", "capacitor"),
            sec("Coulomb-Gesetz und Feld", """
                Ruhende Ladungen wirken mit einer Kraft aufeinander, die mit dem Quadrat des Abstands abfällt und dem Superpositionsprinzip gehorcht. Das elektrische Feld ist die Kraft pro Probeladung.

                Ladung ist quantisiert (Vielfache von e, Quarks tragen ±e/3, ±2e/3, kommen aber nur gebunden vor) und streng erhalten.
            """,
                fm("Coulomb-Kraft", "F = (1/4πε₀) q₁q₂/r² · r̂"),
                fm("Feld einer Punktladung", "E = (1/4πε₀) q/r² · r̂"),
                fm("Kraft auf Ladung", "F = qE")
            ),
            sec("Gaußscher Satz und Potential", """
                Der elektrische Fluss durch eine geschlossene Fläche ist proportional zur eingeschlossenen Ladung. Bei hoher Symmetrie (Kugel, Zylinder, Ebene) liefert er das Feld sofort.

                Das elektrostatische Feld ist wirbelfrei, also Gradient eines Potentials φ. Einsetzen in den Gaußschen Satz ergibt die Poisson-Gleichung; im ladungsfreien Raum die Laplace-Gleichung. Lösungen der Laplace-Gleichung haben keine lokalen Extrema (Earnshaw: keine stabile elektrostatische Falle).
            """,
                fm("Gauß (Integral)", "∮ E · dA = Q_ein/ε₀"),
                fm("Gauß (differentiell)", "∇ · E = ρ/ε₀"),
                fm("Potential", "E = −∇φ,   φ(r) = (1/4πε₀) ∫ ρ(r')/|r − r'| d³r'"),
                fm("Poisson", "∇²φ = −ρ/ε₀"),
                fm("Unendliche Ebene", "E = σ/(2ε₀)")
            ),
            sec("Dipole und Multipole", """
                Ein Dipol p = qd erzeugt ein Feld, das wie 1/r³ abfällt. Im äußeren Feld erfährt er ein Drehmoment und hat eine orientierungsabhängige Energie. Die Multipolentwicklung ordnet beliebige Ladungsverteilungen nach Monopol, Dipol, Quadrupol …
            """,
                fm("Dipolpotential", "φ = (1/4πε₀) p · r̂ / r²"),
                fm("Drehmoment, Energie", "M = p × E,   W = −p · E")
            ),
            sec("Kapazität und Feldenergie", """
                Ein Kondensator speichert Ladung und Feldenergie. Ein Dielektrikum mit ε_r verringert das Feld bei fester Ladung durch Polarisation und erhöht so die Kapazität.

                Die Energie sitzt im Feld selbst; ihre Dichte ist ε₀E²/2.
            """,
                fm("Plattenkondensator", "C = ε₀ε_r A/d"),
                fm("Energie", "W = CU²/2 = Q²/(2C)"),
                fm("Energiedichte", "w = ε₀ε_r E²/2"),
                fm("Reihe/Parallel", "1/C_ges = Σ 1/Cᵢ,   C_ges = Σ Cᵢ")
            )
        ),
        chapter(
            "el_circuits", Topic.ELECTRO, 1, "Stromkreise und Wechselstrom",
            "Ohm, Kirchhoff, Schaltvorgänge und komplexe Wechselstromrechnung.",
            listOf("rc", "rlc"),
            sec("Gleichstrom", """
                Strom ist Ladung pro Zeit. In ohmschen Leitern ist die Stromdichte proportional zum Feld. Kirchhoffs Regeln folgen aus Ladungs- und Energieerhaltung.

                • Knotenregel: Σ I = 0 an jedem Knoten.
                • Maschenregel: Σ U = 0 in jeder Masche.
            """,
                fm("Ohmsches Gesetz", "U = RI,   j = σE"),
                fm("Widerstand", "R = ρℓ/A"),
                fm("Leistung", "P = UI = I²R = U²/R")
            ),
            sec("Schaltvorgänge", """
                Kondensator und Spule speichern Energie; Änderungen erfolgen mit Zeitkonstanten. Beim RC-Glied ändert sich die Kondensatorspannung exponentiell, beim RL-Glied der Strom. Der RLC-Kreis ist ein gedämpfter harmonischer Oszillator — exakt dieselbe Mathematik wie ein Feder-Masse-Dämpfer-System.
            """,
                fm("RC-Laden", "u_C(t) = U₀(1 − e^{−t/RC})"),
                fm("RL-Einschalten", "i(t) = (U₀/R)(1 − e^{−Rt/L})"),
                fm("RLC-Gleichung", "L q̈ + R q̇ + q/C = u(t)"),
                fm("Eigenfrequenz", "ω₀ = 1/√(LC),   δ = R/2L")
            ),
            sec("Komplexe Wechselstromrechnung", """
                Für sinusförmige Größen ersetzt man u(t) = Re(Û e^{iωt}). Dann werden Ableitungen zu Multiplikationen mit iω und Bauteile zu komplexen Impedanzen; Netzwerke rechnet man wie mit Widerständen.

                Wirkleistung entsteht nur im Realteil; Blindleistung pendelt zwischen Quelle und Speichern. Der Leistungsfaktor cos φ misst das Verhältnis.
            """,
                fm("Impedanzen", "Z_R = R,   Z_L = iωL,   Z_C = 1/(iωC)"),
                fm("Wirkleistung", "P = U_eff I_eff cos φ"),
                fm("Effektivwert", "U_eff = Û/√2  (Sinus)"),
                fm("Güte", "Q = ω₀L/R = ω₀/Δω")
            )
        ),
        chapter(
            "el_magneto", Topic.ELECTRO, 2, "Magnetostatik",
            "Lorentzkraft, Biot-Savart, Ampère, Vektorpotential und Magnetismus in Materie.",
            listOf("bfield", "cyclotron"),
            sec("Lorentzkraft", """
                Auf bewegte Ladungen wirkt im Magnetfeld eine Kraft senkrecht zu Geschwindigkeit und Feld. Sie verrichtet keine Arbeit, ändert nur die Richtung: Kreis- oder Schraubenbahnen. Auf stromdurchflossene Leiter wirkt entsprechend F = I ℓ × B.
            """,
                fm("Lorentzkraft", "F = q(E + v × B)"),
                fm("Zyklotronfrequenz", "ω_c = qB/m"),
                fm("Leiterkraft", "F = I ℓ × B")
            ),
            sec("Felder von Strömen", """
                Biot-Savart beschreibt das Feld eines Stromelements; das Ampèresche Gesetz ist die integrale Form für symmetrische Anordnungen. Magnetische Monopole wurden nie beobachtet: ∇ · B = 0, daher existiert ein Vektorpotential A mit B = ∇ × A.
            """,
                fm("Biot-Savart", "dB = (μ₀/4π) I dℓ × r̂ / r²"),
                fm("Ampère (Statik)", "∮ B · dℓ = μ₀ I_ein,   ∇ × B = μ₀ j"),
                fm("Langer Draht", "B = μ₀I/(2πr)"),
                fm("Lange Spule", "B = μ₀ N I/ℓ"),
                fm("Quellenfreiheit", "∇ · B = 0,   B = ∇ × A")
            ),
            sec("Magnetismus in Materie", """
                • Diamagnetismus: induzierte Momente entgegen dem Feld (χ < 0, klein; Supraleiter: χ = −1).
                • Paramagnetismus: permanente Momente richten sich aus; Curie-Gesetz χ = C/T.
                • Ferromagnetismus: Austauschwechselwirkung koppelt Spins parallel; spontane Magnetisierung unterhalb T_C, Domänen, Hysterese. Oberhalb T_C gilt Curie-Weiss.
                • Antiferro- und Ferrimagnetismus: antiparallele Ordnung.
            """,
                fm("Materialgleichungen", "B = μ₀(H + M) = μ₀μ_r H,   M = χH"),
                fm("Curie-Weiss", "χ = C/(T − Θ)")
            )
        ),
        chapter(
            "el_induction", Topic.ELECTRO, 1, "Induktion",
            "Faraday, Lenz, Selbstinduktion, Generator und Transformator.",
            listOf("generator", "skin"),
            sec("Faradaysches Induktionsgesetz", """
                Eine Änderung des magnetischen Flusses durch eine Leiterschleife induziert eine Umlaufspannung. Das Minuszeichen (Lenzsche Regel) drückt aus, dass der induzierte Strom seiner Ursache entgegenwirkt — sonst wäre Energieerhaltung verletzt.

                In differentieller Form: Ein zeitlich veränderliches B-Feld erzeugt ein elektrisches Wirbelfeld.
            """,
                fm("Induktionsgesetz", "U_ind = −dΦ/dt,   Φ = ∫ B · dA"),
                fm("Differentiell", "∇ × E = −∂B/∂t")
            ),
            sec("Selbstinduktion und Energie", """
                Ein Strom durch eine Spule erzeugt einen Fluss durch die Spule selbst. Änderungen des Stroms induzieren eine Gegenspannung; L ist die Induktivität. Die Energie steckt im Magnetfeld.
            """,
                fm("Selbstinduktion", "U = −L dI/dt"),
                fm("Spule", "L = μ₀μ_r N² A/ℓ"),
                fm("Energie", "W = LI²/2,   w = B²/(2μ₀μ_r)")
            ),
            sec("Generator, Transformator, Wirbelströme", """
                Der Generator wandelt Rotation in Wechselspannung. Der Transformator koppelt zwei Spulen über einen Eisenkern; ideal gilt Leistungserhaltung. Wirbelströme in massiven Leitern verursachen Verluste und Bremsung (Wirbelstrombremse) und den Skin-Effekt bei hohen Frequenzen.
            """,
                fm("Generator", "U(t) = NBAω sin ωt"),
                fm("Transformator", "U₂/U₁ = N₂/N₁ = I₁/I₂"),
                fm("Skin-Tiefe", "δ = √(2/(ωμσ))")
            )
        ),
        chapter(
            "el_maxwell", Topic.ELECTRO, 2, "Maxwell-Gleichungen und elektromagnetische Wellen",
            "Die vier Gleichungen, Verschiebungsstrom, Wellen, Energie- und Impulstransport.",
            listOf("skin"),
            sec("Die Maxwell-Gleichungen", """
                Maxwell ergänzte das Ampèresche Gesetz um den Verschiebungsstrom ε₀∂E/∂t; nur so ist es mit der Kontinuitätsgleichung verträglich. Die vier Gleichungen beschreiben zusammen mit der Lorentzkraft die gesamte klassische Elektrodynamik.
            """,
                fm("Gauß (E)", "∇ · E = ρ/ε₀"),
                fm("Gauß (B)", "∇ · B = 0"),
                fm("Faraday", "∇ × E = −∂B/∂t"),
                fm("Ampère-Maxwell", "∇ × B = μ₀ j + μ₀ε₀ ∂E/∂t"),
                fm("Kontinuität", "∂ρ/∂t + ∇ · j = 0")
            ),
            sec("Elektromagnetische Wellen", """
                Im Vakuum folgen aus den Maxwell-Gleichungen Wellengleichungen für E und B mit der Ausbreitungsgeschwindigkeit c = 1/√(μ₀ε₀) — Licht ist eine elektromagnetische Welle. E, B und k stehen senkrecht aufeinander, |E| = c|B|.

                In Medien ist die Phasengeschwindigkeit c/n mit n = √(ε_r μ_r); Dispersion n(ω) führt zu unterschiedlicher Phasen- und Gruppengeschwindigkeit.
            """,
                fm("Wellengleichung", "∇²E − (1/c²) ∂²E/∂t² = 0"),
                fm("Lichtgeschwindigkeit", "c = 1/√(μ₀ε₀)"),
                fm("Ebene Welle", "E = E₀ e^{i(k·r − ωt)},   ω = ck,   B = k̂ × E/c")
            ),
            sec("Energie und Impuls", """
                Der Poynting-Vektor gibt die Energiestromdichte an; der Satz von Poynting ist die lokale Energiebilanz des Feldes. Felder tragen auch Impuls: Strahlungsdruck I/c bei Absorption, 2I/c bei Reflexion.
            """,
                fm("Poynting-Vektor", "S = E × B/μ₀"),
                fm("Poynting-Satz", "∂w/∂t + ∇ · S = −j · E"),
                fm("Intensität", "I = ⟨S⟩ = cε₀E₀²/2"),
                fm("Strahlungsdruck", "p = I/c  (Absorption)")
            ),
            sec("Potentiale, Eichung und Relativität", """
                Mit E = −∇φ − ∂A/∂t und B = ∇ × A sind die homogenen Gleichungen automatisch erfüllt. Die Potentiale sind nur bis auf Eichtransformationen bestimmt. In Lorenz-Eichung entkoppeln die Gleichungen zu Wellengleichungen mit Quellen.

                Relativistisch bilden (φ/c, A) einen Vierervektor und E, B den Feldstärketensor F^{μν}; die Maxwell-Gleichungen werden zu ∂_μ F^{μν} = μ₀ j^ν. Die Elektrodynamik war die erste lorentzkovariante Theorie.
            """,
                fm("Eichtransformation", "A → A + ∇χ,   φ → φ − ∂χ/∂t"),
                fm("Lorenz-Eichung", "∇ · A + (1/c²) ∂φ/∂t = 0"),
                fm("Kovariante Form", "∂_μ F^{μν} = μ₀ j^ν,   F^{μν} = ∂^μA^ν − ∂^νA^μ"),
                fm("Larmor-Formel", "P = q²a²/(6πε₀c³)")
            )
        )
    )
}

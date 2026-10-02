package app.maximus.lab.domain

internal object MathLinearAlgebra {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_linalg", 1, "Lineare Algebra I",
            "Vektorräume, Basen, lineare Abbildungen, Rang, Gleichungssysteme, Determinanten, Eigenwerte.",
            listOf("matrix3", "polynomial"),
            sec("Vektorräume und lineare Abbildungen", """
                Basis = linear unabhängiges Erzeugendensystem; alle Basen haben gleich viele Elemente (Dimension). Eine lineare Abbildung ist durch die Bilder einer Basis festgelegt; bezüglich gewählter Basen ist sie eine Matrix. Basiswechsel: A' = S⁻¹AS. Kern und Bild sind Unterräume.
            """,
                fm("Dimensionsformel", "dim V = dim ker f + dim im f"),
                fm("Basiswechsel", "A' = S⁻¹ A S"),
                fm("Rang", "rang A = dim im A = Zeilenrang = Spaltenrang")
            ),
            sec("Lineare Gleichungssysteme", """
                Ax = b ist lösbar ⇔ rang A = rang(A | b). Die Lösungsmenge ist x_p + ker A (affiner Raum). Gauß-Elimination bringt A auf Zeilenstufenform. Numerisch: LU-Zerlegung mit Pivotisierung; die Kondition κ(A) begrenzt die erreichbare Genauigkeit.
            """,
                fm("Lösbarkeit", "Ax = b lösbar ⇔ rang A = rang(A|b)"),
                fm("Kondition", "‖δx‖/‖x‖ ≤ κ(A) ‖δb‖/‖b‖,   κ = ‖A‖‖A⁻¹‖")
            ),
            sec("Determinante", """
                Die Determinante ist die eindeutige multilineare, alternierende, normierte Form auf den Spalten — das orientierte Volumen des Parallelepipeds. Laplace-Entwicklung nach Zeilen/Spalten, Sarrus für 3×3. Inverse über die Adjunkte.
            """,
                fm("Leibniz-Formel", "det A = Σ_{σ∈Sₙ} sgn(σ) Πᵢ a_{i,σ(i)}"),
                fm("Multiplikativität", "det(AB) = det A det B,   det Aᵀ = det A,   det A⁻¹ = 1/det A"),
                fm("Inverse", "A⁻¹ = adj(A)/det A"),
                fm("Sarrus", "det(3×3) = aei + bfg + cdh − ceg − bdi − afh")
            ),
            sec("Eigenwerte und Diagonalisierung", """
                Eigenwerte sind Nullstellen des charakteristischen Polynoms χ_A(λ) = det(A − λI). A ist diagonalisierbar ⇔ algebraische = geometrische Vielfachheit für alle Eigenwerte ⇔ es gibt eine Basis aus Eigenvektoren. Eigenvektoren zu verschiedenen Eigenwerten sind linear unabhängig.
            """,
                fm("Charakteristisches Polynom", "χ_A(λ) = det(A − λI) = (−λ)ⁿ + tr A (−λ)ⁿ⁻¹ + … + det A"),
                fm("Spur und Determinante", "tr A = Σλᵢ,   det A = Πλᵢ"),
                fm("2×2-Eigenwerte", "λ = tr/2 ± √((tr/2)² − det)"),
                fm("Diagonalisierung", "A = V Λ V⁻¹  ⇒  Aⁿ = V Λⁿ V⁻¹,  e^A = V e^Λ V⁻¹")
            )
        ),
        CompendiumMath.ch(
            "ma_linalg2", 2, "Lineare Algebra II",
            "Jordan-Normalform, Cayley-Hamilton, Skalarprodukte, Spektralsatz, Hauptachsen, SVD, Dual- und Tensorräume.",
            listOf("matrix3"),
            sec("Jordan-Normalform", """
                Über ℂ ist jede Matrix ähnlich zu einer Blockdiagonalmatrix aus Jordan-Blöcken J(λ) = λI + N mit nilpotentem N. Größe der Blöcke aus den Dimensionen von ker (A − λI)ᵏ. Das Minimalpolynom teilt das charakteristische Polynom; Cayley-Hamilton: χ_A(A) = 0. Anwendung: e^{tJ} = e^{λt} Σ tᵏNᵏ/k! — so entstehen Lösungen t e^{λt} bei entarteten Eigenwerten von DGL-Systemen.
            """,
                fm("Cayley-Hamilton", "χ_A(A) = 0"),
                fm("Jordan-Block-Exponential", "e^{t(λI + N)} = e^{λt} Σ_{k} tᵏ Nᵏ/k!")
            ),
            sec("Skalarprodukte und Orthogonalität", """
                Ein Skalarprodukt ist bilinear (sesquilinear über ℂ), symmetrisch (hermitesch) und positiv definit. Gram-Schmidt orthonormalisiert jede Basis; die QR-Zerlegung ist seine Matrixform. Orthogonale Projektion auf U: P = Σ|eₖ⟩⟨eₖ|. Orthogonale (unitäre) Matrizen erhalten Längen: QᵀQ = I.
            """,
                fm("Gram-Schmidt", "e_k = (v_k − Σ_{j<k} ⟨eⱼ, v_k⟩ eⱼ)/‖…‖"),
                fm("Projektion", "P = Σₖ eₖ eₖᵀ,   P² = P = Pᵀ"),
                fm("Kleinste Quadrate", "x̂ = (AᵀA)⁻¹ Aᵀ b  (Normalgleichungen)")
            ),
            sec("Spektralsatz und Hauptachsentransformation", """
                Normale Matrizen (AA* = A*A) sind unitär diagonalisierbar; hermitesche haben reelle Eigenwerte, unitäre Eigenwerte vom Betrag 1. Reell-symmetrische Matrizen werden orthogonal diagonalisiert: Hauptachsentransformation von Quadriken, Trägheitstensor, Normalmoden, Spannungstensor.

                Sylvesters Trägheitssatz: Die Zahl positiver, negativer und verschwindender Eigenwerte einer symmetrischen Bilinearform ist basisunabhängig (Signatur; Minkowski-Metrik: (1, 3)).
            """,
                fm("Spektralsatz", "A = A* ⇒ A = U Λ U*,  λᵢ ∈ ℝ"),
                fm("Rayleigh-Quotient", "λ_min ≤ xᵀAx/xᵀx ≤ λ_max"),
                fm("Courant-Fischer", "λₖ = min_{dim U = k} max_{x∈U} xᵀAx/xᵀx")
            ),
            sec("Singulärwertzerlegung, Dual- und Tensorraum", """
                Jede Matrix A = UΣVᵀ mit Singulärwerten σᵢ = √λᵢ(AᵀA). Beste Rang-k-Approximation: die k größten Singulärwerte behalten (Eckart-Young). Pseudoinverse A⁺ = VΣ⁺Uᵀ.

                Der Dualraum V* besteht aus Linearformen; ein Skalarprodukt identifiziert V und V* (Index heben/senken). Das Tensorprodukt V ⊗ W hat die Dimension dim V · dim W — Zustandsraum zusammengesetzter Quantensysteme, Verschränkung.
            """,
                fm("SVD", "A = U Σ Vᵀ,   ‖A‖₂ = σ₁"),
                fm("Pseudoinverse", "A⁺ = V Σ⁺ Uᵀ"),
                fm("Tensorprodukt", "dim(V ⊗ W) = dim V · dim W")
            )
        )
    )
}

internal object MathOde {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_ode", 1, "Gewöhnliche DGL: Lösungsmethoden",
            "Trennung der Variablen, lineare Gleichungen, Bernoulli, exakte DGL, konstante Koeffizienten, Schwingungen.",
            listOf("pendulum", "euler_rk4", "rlc"),
            sec("Erste Ordnung", """
                • Trennung der Variablen: y' = f(x)g(y) ⇒ ∫dy/g(y) = ∫f(x)dx.
                • Linear: y' + p(x)y = q(x) mit integrierendem Faktor μ = e^{∫p}.
                • Bernoulli: y' + py = qyⁿ wird mit z = y^{1−n} linear.
                • Exakt: P dx + Q dy = 0 mit ∂_yP = ∂_xQ ⇒ Potential F mit F = const; sonst integrierenden Faktor suchen.
                • Homogen: y' = f(y/x) mit u = y/x.
            """,
                fm("Trennung der Variablen", "y' = f(x)g(y)  ⇒  ∫ dy/g(y) = ∫ f(x) dx"),
                fm("Lineare DGL 1. Ordnung", "y = e^{−∫p} (∫ q e^{∫p} dx + C)"),
                fm("Bernoulli", "z = y^{1−n}:  z' + (1−n)p z = (1−n) q")
            ),
            sec("Lineare Gleichungen mit konstanten Koeffizienten", """
                Ansatz y = e^{λx} führt auf das charakteristische Polynom. Mehrfache Nullstellen λ (Vielfachheit m) liefern xᵏe^{λx}, k < m; komplexe Paare α ± iβ liefern e^{αx}cos βx, e^{αx}sin βx. Partikuläre Lösung: Ansatz vom Typ der rechten Seite (Resonanzfall: mit x multiplizieren) oder Variation der Konstanten.
            """,
                fm("Charakteristisches Polynom", "Σ aₖ y⁽ᵏ⁾ = 0  ⇒  Σ aₖ λᵏ = 0"),
                fm("Variation der Konstanten (2. Ordnung)", "y_p = −y₁ ∫ y₂ r/W dx + y₂ ∫ y₁ r/W dx,   W = y₁y₂' − y₁'y₂")
            ),
            sec("Schwingungen", """
                Der gedämpfte, getriebene Oszillator ẍ + 2δẋ + ω₀²x = f₀cos ωt: Schwingfall, Grenzfall, Kriechfall; stationäre Antwort mit Amplitude A(ω) und Phase φ(ω). Resonanzfrequenz ω_R = √(ω₀² − 2δ²); Halbwertsbreite ≈ 2δ.
            """,
                fm("Oszillator", "ẍ + 2δẋ + ω₀²x = f₀ cos ωt"),
                fm("Amplitude", "A(ω) = f₀/√((ω₀² − ω²)² + 4δ²ω²)"),
                fm("Phase", "tan φ = 2δω/(ω₀² − ω²)")
            ),
            sec("Numerik", """
                Euler explizit (Ordnung 1, für steife Probleme instabil), Runge-Kutta 4 (Ordnung 4), Verlet/Leapfrog (symplektisch, keine Energiedrift), implizite Verfahren für steife Systeme. Schrittweitensteuerung über eingebettete Verfahren (Dormand-Prince).
            """,
                fm("RK4", "y_{n+1} = yₙ + (h/6)(k₁ + 2k₂ + 2k₃ + k₄)"),
                fm("Stabilität Euler", "|1 + hλ| ≤ 1")
            )
        ),
        CompendiumMath.ch(
            "ma_ode_theory", 2, "Gewöhnliche DGL: Theorie",
            "Picard-Lindelöf, Gronwall, Fundamentalsysteme, Matrixexponential, Stabilität und Lyapunov.",
            listOf("lorenz"),
            sec("Existenz und Eindeutigkeit", """
                Picard-Lindelöf: Ist f stetig und lokal Lipschitz in y, hat y' = f(t, y), y(t₀) = y₀ eine eindeutige lokale Lösung (Beweis: Banachscher Fixpunktsatz für den Integraloperator). Peano: Stetigkeit allein gibt Existenz, aber nicht Eindeutigkeit (y' = √|y|, y(0) = 0 hat unendlich viele Lösungen). Lösungen lassen sich bis zum Rand des Definitionsbereichs oder bis zur Explosion fortsetzen (y' = y² explodiert in endlicher Zeit).
            """,
                fm("Picard-Iteration", "y_{k+1}(t) = y₀ + ∫_{t₀}^t f(s, y_k(s)) ds"),
                fm("Gronwall", "u(t) ≤ a + ∫_{t₀}^t b u ds  ⇒  u(t) ≤ a e^{∫ b ds}"),
                fm("Stetige Abhängigkeit", "|y(t) − z(t)| ≤ |y₀ − z₀| e^{L|t − t₀|}")
            ),
            sec("Lineare Systeme", """
                Die Lösungen von y' = A(t)y bilden einen n-dimensionalen Vektorraum; eine Fundamentalmatrix Y(t) hat Basislösungen als Spalten. Liouville: det Y erfüllt eine skalare DGL. Für konstantes A ist Y = e^{tA}; inhomogene Systeme mit Duhamel (Variation der Konstanten).
            """,
                fm("Matrixexponential", "e^{tA} = Σ (tA)ᵏ/k!,   d/dt e^{tA} = A e^{tA}"),
                fm("Duhamel", "y(t) = e^{tA}y₀ + ∫₀ᵗ e^{(t−s)A} b(s) ds"),
                fm("Liouville", "det Y(t) = det Y(t₀) exp(∫ tr A ds)"),
                fm("Determinante und Spur", "det e^A = e^{tr A}")
            ),
            sec("Autonome Systeme und Stabilität", """
                Gleichgewicht x* mit f(x*) = 0. Linearisierung: Jacobi-Matrix J. Re λ < 0 für alle Eigenwerte ⇒ asymptotisch stabil; ein Re λ > 0 ⇒ instabil (Hartman-Grobman). Typen in 2D: Knoten, Sattel, Strudel, Zentrum (über tr J und det J).

                Lyapunov-Funktion V > 0 mit V̇ ≤ 0 beweist Stabilität ohne Lösen (Energie beim Pendel). In 2D verbietet Poincaré-Bendixson Chaos; ab 3D möglich (Lorenz).
            """,
                fm("Linearisierung", "ẋ = J(x*)(x − x*),   J = Df(x*)"),
                fm("2D-Klassifikation", "det J < 0 Sattel;  det J > 0, tr J < 0 stabil;  tr² < 4 det Spirale"),
                fm("Lyapunov", "V(x*) = 0, V > 0 sonst, V̇ = ∇V·f ≤ 0  ⇒  stabil")
            )
        )
    )
}

internal object MathComplex {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_complex", 2, "Funktionentheorie",
            "Holomorphie, Cauchy-Riemann, Integralsatz und -formel, Laurent, Residuen, reelle Integrale, Liouville.",
            listOf("complex"),
            sec("Holomorphe Funktionen", """
                Komplex differenzierbar ⇔ reell differenzierbar und Cauchy-Riemann. Holomorphe Funktionen sind beliebig oft differenzierbar und analytisch; Real- und Imaginärteil sind harmonisch. Identitätssatz: Stimmen zwei holomorphe Funktionen auf einer Menge mit Häufungspunkt überein, sind sie gleich (Grundlage der analytischen Fortsetzung, z. B. Γ und ζ).
            """,
                fm("Cauchy-Riemann", "∂ₓu = ∂ᵧv,   ∂ᵧu = −∂ₓv   (f = u + iv)"),
                fm("Wirtinger", "∂f/∂z̄ = 0 ⇔ f holomorph")
            ),
            sec("Cauchy-Integralsatz und -formel", """
                Auf einfach zusammenhängenden Gebieten verschwindet das Integral einer holomorphen Funktion über jede geschlossene Kurve. Die Integralformel stellt f und alle Ableitungen durch Randwerte dar. Folgerungen: Liouville (beschränkt und ganz ⇒ konstant), Fundamentalsatz der Algebra, Maximumprinzip.
            """,
                fm("Integralsatz", "∮_γ f(z) dz = 0"),
                fm("Integralformel", "f⁽ⁿ⁾(z₀) = (n!/2πi) ∮ f(z)/(z − z₀)ⁿ⁺¹ dz"),
                fm("Cauchy-Abschätzung", "|f⁽ⁿ⁾(z₀)| ≤ n! max_{|z−z₀|=r} |f| / rⁿ")
            ),
            sec("Laurent-Reihen und Singularitäten", """
                Auf Kreisringen entwickelt man in Laurent-Reihen. Der Hauptteil (negative Potenzen) klassifiziert isolierte Singularitäten: hebbar (kein Hauptteil), Pol der Ordnung m (endlich), wesentlich (unendlich; Casorati-Weierstraß: Bild nahe der Singularität dicht, z. B. e^{1/z}).
            """,
                fm("Laurent-Reihe", "f(z) = Σ_{n=−∞}^{∞} aₙ(z − z₀)ⁿ,   aₙ = (1/2πi) ∮ f(z)/(z − z₀)ⁿ⁺¹ dz"),
                fm("Residuum (Pol m-ter Ordnung)", "Res(f, z₀) = (1/(m−1)!) lim d^{m−1}/dz^{m−1} [(z − z₀)ᵐ f(z)]"),
                fm("Einfacher Pol von g/h", "Res = g(z₀)/h'(z₀)")
            ),
            sec("Residuensatz und reelle Integrale", """
                ∮ f dz = 2πi Σ Res. Typische Anwendungen:
                • Rationale Integranden über ℝ: obere Halbebene schließen (Grad Nenner ≥ Grad Zähler + 2).
                • Fourier-Integrale ∫ f(x)e^{ikx}dx: Jordans Lemma, für k > 0 oben schließen.
                • Trigonometrische Integrale über [0, 2π]: z = e^{iθ} auf dem Einheitskreis.
                • Pole auf der reellen Achse: Cauchy-Hauptwert, Sokhotski-Plemelj 1/(x ∓ iε) = P(1/x) ± iπδ(x) — die iε-Vorschrift der Propagatoren.
                • Argumentprinzip: (1/2πi)∮f'/f dz = Nullstellen − Pole.
            """,
                fm("Residuensatz", "∮_γ f dz = 2πi Σₖ Res(f, zₖ)"),
                fm("Beispiel", "∫_{−∞}^{∞} dx/(x² + a²) = π/a"),
                fm("Jordan-Lemma", "∫_{C_R} f(z) e^{ikz} dz → 0  (k > 0, f → 0)"),
                fm("Sokhotski-Plemelj", "1/(x ∓ iε) = P(1/x) ± iπ δ(x)"),
                fm("Kramers-Kronig", "Re χ(ω) = (1/π) P∫ Im χ(ω')/(ω' − ω) dω'")
            )
        )
    )
}

internal object MathFourier {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_fourier", 2, "Fourier-Reihen und Fourier-Transformation",
            "Orthogonalität, Konvergenz, Parseval, Faltungssatz, Unschärfe, Dirac-Delta, FFT.",
            listOf("fourier"),
            sec("Fourier-Reihen", """
                {e^{inx}} ist ein vollständiges Orthonormalsystem in L²(−π, π). Jede L²-Funktion ist die L²-Summe ihrer Fourier-Reihe (Riesz-Fischer); punktweise Konvergenz für stückweise glatte Funktionen (Dirichlet), an Sprungstellen gegen den Mittelwert, mit Gibbs-Überschwingen. Glattheit ↔ Abklingen der Koeffizienten.
            """,
                fm("Koeffizienten", "cₙ = (1/2π) ∫_{−π}^{π} f(x) e^{−inx} dx"),
                fm("Reelle Form", "aₙ = (1/π)∫ f cos nx dx,   bₙ = (1/π)∫ f sin nx dx"),
                fm("Parseval", "(1/2π) ∫ |f|² dx = Σ |cₙ|²"),
                fm("Rechteck", "sgn(x) = (4/π) Σ_{k} sin((2k+1)x)/(2k+1)")
            ),
            sec("Fourier-Transformation", """
                Auf L¹ und (durch Fortsetzung) L² definiert; auf L² ist sie unitär (Plancherel). Sie überführt Ableitungen in Multiplikationen, Faltungen in Produkte und Translationen in Phasen. Gauß-Funktionen sind Eigenfunktionen; die Breiten in x und k erfüllen Δx Δk ≥ 1/2. Physikalische Konventionen variieren um Faktoren 2π.
            """,
                fm("Transformation", "f̂(k) = ∫ f(x) e^{−ikx} dx,   f(x) = (1/2π) ∫ f̂(k) e^{ikx} dk"),
                fm("Plancherel", "∫ |f|² dx = (1/2π) ∫ |f̂|² dk"),
                fm("Faltungssatz", "(f * g)^ = f̂ ĝ"),
                fm("Ableitung und Translation", "(f')^ = ik f̂,   (f(x − a))^ = e^{−ika} f̂"),
                fm("Unschärfe", "Δx Δk ≥ ½")
            ),
            sec("Dirac-Delta und Distributionen", """
                δ ist keine Funktion, sondern ein lineares Funktional: ⟨δ, φ⟩ = φ(0). Ableitungen von Distributionen definiert man über partielle Integration. Nützliche Darstellungen und Rechenregeln:
            """,
                fm("Fourier-Darstellung", "δ(x) = (1/2π) ∫ e^{ikx} dk"),
                fm("Skalierung", "δ(g(x)) = Σ δ(x − xᵢ)/|g'(xᵢ)|"),
                fm("Ableitung", "∫ δ'(x) φ(x) dx = −φ'(0)"),
                fm("Heaviside", "θ'(x) = δ(x)"),
                fm("Laplace von 1/r", "Δ(1/r) = −4π δ³(r)")
            )
        ),
        CompendiumMath.ch(
            "ma_pde", 3, "Partielle Differentialgleichungen",
            "Klassifikation, Separation, Kugelflächenfunktionen, Green-Funktionen, Wellen- und Wärmeleitungsgleichung.",
            listOf("heat_rod"),
            sec("Klassifikation", """
                Lineare PDGL 2. Ordnung a u_xx + 2b u_xy + c u_yy + … = 0: elliptisch (b² < ac, Laplace — Randwertprobleme, Glättung), parabolisch (b² = ac, Wärmeleitung — Anfangs-Randwertprobleme, Irreversibilität), hyperbolisch (b² > ac, Wellen — endliche Ausbreitungsgeschwindigkeit, Charakteristiken).
            """,
                fm("Diskriminante", "b² − ac < 0 elliptisch,  = 0 parabolisch,  > 0 hyperbolisch")
            ),
            sec("Separation und Eigenfunktionen", """
                Ansatz u = X(x)T(t) führt auf Sturm-Liouville-Eigenwertprobleme; deren Eigenfunktionen bilden Orthonormalbasen. In Kugelkoordinaten entstehen für die Laplace-Gleichung die Kugelflächenfunktionen Y_l^m und radial rˡ bzw. r^{−l−1} — die Multipolentwicklung. In Zylinderkoordinaten: Bessel-Funktionen.
            """,
                fm("Laplace-Lösungen (Kugel)", "Φ = Σ (A_l rˡ + B_l r^{−l−1}) Y_l^m(θ, φ)"),
                fm("Kugelflächenfunktionen", "Δ_S Y_l^m = −l(l+1) Y_l^m,   ∫ Y_l^m* Y_l'^m' dΩ = δ_ll' δ_mm'"),
                fm("Multipolentwicklung", "1/|r − r'| = Σ_l (r<ˡ/r>^{l+1}) P_l(cos γ)")
            ),
            sec("Green-Funktionen", """
                Für einen linearen Operator L ist G die Lösung von LG = δ; dann löst u = ∫G f die Gleichung Lu = f. Retardierte Green-Funktion der Wellengleichung: Wirkung breitet sich mit c aus (Liénard-Wiechert). Wärmeleitungskern: Gauß-Glocke, die mit √t breiter wird.
            """,
                fm("Poisson", "Δu = −f  ⇒  u(r) = (1/4π) ∫ f(r')/|r − r'| d³r'"),
                fm("Wärmeleitungskern", "G(x, t) = (4πat)^{−n/2} e^{−|x|²/4at}"),
                fm("Retardierte Green-Funktion", "G(r, t) = δ(t − r/c)/(4πr)"),
                fm("d'Alembert (1D)", "u = ½[f(x−ct) + f(x+ct)] + (1/2c)∫_{x−ct}^{x+ct} g ds")
            )
        )
    )
}

internal object MathFunctional {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_functional", 3, "Funktionalanalysis für die Quantenmechanik",
            "Hilbert-Räume, Orthonormalbasen, Riesz, Operatoren, Adjungierte, Selbstadjungiertheit, Spektralsatz.",
            emptyList(),
            sec("Hilbert-Räume", """
                Ein Hilbert-Raum ist ein vollständiger Raum mit Skalarprodukt. Separable Hilbert-Räume besitzen abzählbare Orthonormalbasen; jeder ist isomorph zu ℓ² (darum sind Matrizen- und Wellenmechanik äquivalent). Projektionssatz: Zu jedem abgeschlossenen Unterraum gibt es eine eindeutige Bestapproximation. Riesz: Jede stetige Linearform ist ⟨y, ·⟩ — die Grundlage der Dirac-Notation.
            """,
                fm("Parseval", "‖x‖² = Σ |⟨eₙ, x⟩|²"),
                fm("Bessel", "Σ |⟨eₙ, x⟩|² ≤ ‖x‖²  (beliebiges ONS)"),
                fm("Riesz-Darstellung", "ℓ ∈ H* ⇒ ∃! y: ℓ(x) = ⟨y, x⟩")
            ),
            sec("Operatoren", """
                Beschränkte Operatoren sind stetig. Der Adjungierte erfüllt ⟨Ax, y⟩ = ⟨x, A*y⟩. Physikalisch wichtige Operatoren (x̂, p̂, Ĥ) sind unbeschränkt und nur auf einem dichten Teilraum definiert (Hellinger-Toeplitz). Symmetrisch (⟨Ax, y⟩ = ⟨x, Ay⟩) genügt nicht; nötig ist Selbstadjungiertheit (A = A* inklusive Definitionsbereich). Beispiel: −i d/dx auf [0, 1] braucht passende Randbedingungen (Phasenbedingung), sonst ist es nicht selbstadjungiert.

                Stone: Selbstadjungierte Operatoren erzeugen genau die stark stetigen unitären Gruppen e^{−iHt} — die quantenmechanische Zeitentwicklung.
            """,
                fm("Adjungierter", "⟨Ax, y⟩ = ⟨x, A*y⟩"),
                fm("Operatornorm", "‖A‖ = sup_{‖x‖=1} ‖Ax‖"),
                fm("Stone", "H = H*  ⇔  U(t) = e^{−iHt} unitär, stark stetig")
            ),
            sec("Spektrum und Spektralsatz", """
                Das Spektrum σ(A) besteht aus den λ, für die A − λ nicht beschränkt invertierbar ist: Punktspektrum (Eigenwerte, gebundene Zustände), kontinuierliches Spektrum (Streuzustände, keine normierbaren Eigenvektoren, „uneigentliche“ Eigenvektoren wie e^{ikx}). Selbstadjungierte Operatoren haben reelles Spektrum und eine Spektralzerlegung A = ∫ λ dE(λ); kompakte selbstadjungierte Operatoren sind wie Matrizen diagonalisierbar (Hilbert-Schmidt).
            """,
                fm("Spektralsatz", "A = ∫_{σ(A)} λ dE_λ,   f(A) = ∫ f(λ) dE_λ"),
                fm("Resolvente", "R(λ) = (A − λ)⁻¹,   ‖R(λ)‖ = 1/dist(λ, σ(A))  (A selbstadjungiert)")
            )
        )
    )
}

internal object MathGeometry {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_variation", 2, "Variationsrechnung",
            "Euler-Lagrange, Beltrami-Identität, Brachistochrone, Nebenbedingungen, Felder.",
            emptyList(),
            sec("Euler-Lagrange-Gleichung", """
                Gesucht ist eine Funktion y(x), die das Funktional J[y] = ∫ L(x, y, y') dx stationär macht. Variation y + εη mit η(a) = η(b) = 0 und das Fundamentallemma der Variationsrechnung liefern die Euler-Lagrange-Gleichung. Hängt L nicht explizit von x ab, gibt es das erste Integral (Beltrami) — in der Mechanik die Energieerhaltung.
            """,
                fm("Euler-Lagrange", "∂L/∂y − d/dx ∂L/∂y' = 0"),
                fm("Beltrami", "∂L/∂x = 0  ⇒  L − y' ∂L/∂y' = const"),
                fm("Mehrere Variablen (Felder)", "∂𝓛/∂φ − ∂_μ (∂𝓛/∂(∂_μφ)) = 0")
            ),
            sec("Klassische Probleme", """
                • Kürzeste Verbindung in der Ebene: Gerade.
                • Brachistochrone (schnellster Abstieg): Zykloide x = r(t − sin t), y = r(1 − cos t).
                • Kettenlinie (minimale potentielle Energie bei fester Länge): y = a cosh(x/a), mit Lagrange-Multiplikator für die Länge.
                • Minimalfläche (Rotationsfläche): Katenoid.
                • Fermat-Prinzip der Optik: Licht nimmt den Weg extremaler Laufzeit ⇒ Snellius.
            """,
                fm("Isoperimetrisch", "δ(J − λK) = 0  bei Nebenbedingung K[y] = const")
            )
        ),
        CompendiumMath.ch(
            "ma_tensor", 3, "Tensorrechnung und Differentialgeometrie",
            "Ko- und kontravariante Komponenten, Metrik, Christoffel-Symbole, kovariante Ableitung, Geodäten, Krümmung.",
            emptyList(),
            sec("Tensoren", """
                Ein Tensor vom Typ (r, s) ist eine multilineare Abbildung auf r Kovektoren und s Vektoren; seine Komponenten transformieren mit r Jacobi-Matrizen und s inversen. Die Metrik g_μν hebt und senkt Indizes. Kontraktion eines oberen mit einem unteren Index ergibt wieder einen Tensor (Spur, Skalarprodukt).
            """,
                fm("Transformation", "T'^μ_ν = (∂x'^μ/∂x^α)(∂x^β/∂x'^ν) T^α_β"),
                fm("Index heben/senken", "V_μ = g_μν V^ν,   g^μα g_αν = δ^μ_ν"),
                fm("Linienelement", "ds² = g_μν dx^μ dx^ν")
            ),
            sec("Kovariante Ableitung und Geodäten", """
                Partielle Ableitungen von Vektorkomponenten sind in krummlinigen Koordinaten keine Tensoren. Die kovariante Ableitung korrigiert das mit den Christoffel-Symbolen (Levi-Civita-Zusammenhang: metrikverträglich, torsionsfrei). Geodäten sind Kurven, deren Tangente parallel transportiert wird — in der ART die Bahnen frei fallender Körper.
            """,
                fm("Christoffel-Symbole", "Γ^λ_μν = ½ g^λσ (∂_μ g_σν + ∂_ν g_σμ − ∂_σ g_μν)"),
                fm("Kovariante Ableitung", "∇_μ V^ν = ∂_μ V^ν + Γ^ν_μλ V^λ,   ∇_μ ω_ν = ∂_μ ω_ν − Γ^λ_μν ω_λ"),
                fm("Geodätengleichung", "d²x^λ/dτ² + Γ^λ_μν (dx^μ/dτ)(dx^ν/dτ) = 0")
            ),
            sec("Krümmung", """
                Der Riemann-Tensor misst, wie sehr Parallelverschiebung um eine infinitesimale Schleife vom Weg abhängt (Kommutator kovarianter Ableitungen). Kontraktionen: Ricci-Tensor und Ricci-Skalar. Bianchi-Identität ∇_μ G^μν = 0 — passend zur Energie-Impuls-Erhaltung in Einsteins Gleichungen.
            """,
                fm("Riemann-Tensor", "R^ρ_σμν = ∂_μΓ^ρ_νσ − ∂_νΓ^ρ_μσ + Γ^ρ_μλΓ^λ_νσ − Γ^ρ_νλΓ^λ_μσ"),
                fm("Ricci", "R_μν = R^λ_μλν,   R = g^μν R_μν"),
                fm("Einstein-Tensor", "G_μν = R_μν − ½ R g_μν,   ∇^μ G_μν = 0")
            )
        ),
        CompendiumMath.ch(
            "ma_special", 3, "Spezielle Funktionen und Gruppentheorie",
            "Orthogonale Polynome, Bessel, Kugelflächenfunktionen, Gruppen, Darstellungen, Lie-Algebren.",
            listOf("oscillator", "hydrogen"),
            sec("Spezielle Funktionen", """
                Sie entstehen bei der Separation der Gleichungen der Physik:
                • Hermite: harmonischer Oszillator, Gewicht e^{−x²}.
                • Legendre Pₗ und zugeordnete P_l^m: Laplace in Kugelkoordinaten, Multipole.
                • Laguerre: Radialfunktionen des Wasserstoffs.
                • Bessel Jₙ: Zylindersymmetrie, Beugung an der Kreisblende (Airy-Scheibchen 1,22 λ/D).
                • Gamma und Beta: Phasenraumintegrale, Dimensionsregularisierung.
                Alle erfüllen Drei-Term-Rekursionen, Rodrigues-Formeln und haben erzeugende Funktionen.
            """,
                fm("Legendre-Rodrigues", "Pₗ(x) = (1/(2ˡ l!)) dˡ/dxˡ (x² − 1)ˡ"),
                fm("Erzeugende Funktion (Legendre)", "1/√(1 − 2xt + t²) = Σ Pₗ(x) tˡ"),
                fm("Hermite-Rekursion", "H_{n+1} = 2xHₙ − 2nH_{n−1}"),
                fm("Bessel-Gleichung", "x²y'' + xy' + (x² − ν²)y = 0"),
                fm("Stirling", "ln n! ≈ n ln n − n + ½ ln(2πn)")
            ),
            sec("Gruppen und Darstellungen", """
                Symmetrien bilden Gruppen. Eine Darstellung ordnet jedem Element eine lineare Abbildung zu; irreduzible Darstellungen sind die Bausteine (Schur-Lemma). Teilchen sind irreduzible Darstellungen von Symmetriegruppen: Spin ↔ SU(2), Teilchen ↔ Poincaré-Gruppe (Wigner: Masse und Spin), Quarks ↔ SU(3)_Farbe.
            """,
                fm("Schur-Lemma", "[A, D(g)] = 0 ∀g, D irreduzibel  ⇒  A = λ·1"),
                fm("Charakter-Orthogonalität", "(1/|G|) Σ_g χ_i(g)* χ_j(g) = δ_ij")
            ),
            sec("Lie-Gruppen und Lie-Algebren", """
                Kontinuierliche Gruppen werden nahe der Identität durch ihre Lie-Algebra beschrieben: Generatoren Tₐ mit [Tₐ, T_b] = i f_abc T_c. SO(3) und SU(2) haben dieselbe Algebra; SU(2) ist die zweifache Überlagerung — daher halbzahliger Spin. Casimir-Operatoren (J²) kommutieren mit allen Generatoren und kennzeichnen Multipletts.
            """,
                fm("su(2)", "[Jᵢ, Jⱼ] = iεᵢⱼₖ Jₖ,   J² = j(j+1)"),
                fm("Exponentialabbildung", "U(θ) = exp(−iθₐTₐ)"),
                fm("Dimension", "dim SU(N) = N² − 1,   dim SO(N) = N(N−1)/2")
            )
        )
    )
}

internal object MathStochastics {
    val chapters = arrayOf(
        CompendiumMath.ch(
            "ma_prob", 1, "Wahrscheinlichkeit und Statistik",
            "Kolmogorov, Bayes, Verteilungen, Grenzwertsätze, Schätzer, Fehlerfortpflanzung, Regression.",
            listOf("binomial"),
            sec("Grundbegriffe", """
                Ein Wahrscheinlichkeitsraum ist ein Maßraum mit P(Ω) = 1 — Stochastik ist Maßtheorie. Zufallsvariablen sind messbare Funktionen, Erwartungswerte Lebesgue-Integrale. Bayes kehrt bedingte Wahrscheinlichkeiten um.
            """,
                fm("Bayes", "P(A|B) = P(B|A) P(A)/P(B)"),
                fm("Erwartungswert", "E[X] = ∫ X dP = ∫ x f(x) dx"),
                fm("Varianz", "Var X = E[X²] − E[X]²,   Var(aX + b) = a² Var X"),
                fm("Kovarianz", "Cov(X, Y) = E[XY] − E[X]E[Y],   Var(X+Y) = Var X + Var Y + 2Cov")
            ),
            sec("Verteilungen und Grenzwertsätze", """
                • Binomial B(n, p): μ = np, σ² = np(1 − p).
                • Poisson(λ): μ = σ² = λ — Zählstatistik, √N-Fehler.
                • Normal N(μ, σ²): 68,3 / 95,4 / 99,7 % in 1σ / 2σ / 3σ.
                • Exponential: Wartezeiten, gedächtnislos.
                • χ²-Verteilung: Summe quadrierter Standardnormalverteilter, für Anpassungstests.
                Gesetz der großen Zahlen: Mittelwerte konvergieren gegen den Erwartungswert. Zentraler Grenzwertsatz: (Sₙ − nμ)/(σ√n) → N(0, 1).
            """,
                fm("Normalverteilung", "f(x) = (1/(σ√(2π))) e^{−(x−μ)²/(2σ²)}"),
                fm("Poisson", "P(k) = λᵏ e^{−λ}/k!"),
                fm("Zentraler Grenzwertsatz", "(Σ Xᵢ − nμ)/(σ√n) → N(0, 1)"),
                fm("Charakteristische Funktion", "φ_X(t) = E[e^{itX}],   Normal: e^{iμt − σ²t²/2}")
            ),
            sec("Messauswertung", """
                Mittelwert und Standardabweichung mit Bessel-Korrektur n − 1, Standardfehler σ/√n. Gaußsche Fehlerfortpflanzung linearisiert die Messfunktion. Maximum-Likelihood und kleinste Quadrate (für Gauß-Fehler identisch); χ²/Freiheitsgrad ≈ 1 bei guter Anpassung.
            """,
                fm("Standardfehler", "σ_x̄ = s/√n,   s² = Σ(xᵢ − x̄)²/(n − 1)"),
                fm("Fehlerfortpflanzung", "σ_f² = Σ (∂f/∂xᵢ)² σᵢ²  (unkorreliert)"),
                fm("Relative Fehler bei Produkten", "(σ_f/f)² = Σ (nᵢ σᵢ/xᵢ)²  für f = Π xᵢ^{nᵢ}"),
                fm("Gewichtetes Mittel", "x̄ = Σ wᵢxᵢ/Σ wᵢ,   wᵢ = 1/σᵢ²,   σ_x̄ = 1/√(Σ wᵢ)"),
                fm("χ²", "χ² = Σ (yᵢ − f(xᵢ))²/σᵢ²")
            )
        ),
        CompendiumMath.ch(
            "ma_numerics", 2, "Numerische Mathematik",
            "Gleitkommazahlen, Kondition, Nullstellen, Interpolation, Quadratur, lineare Systeme, Stabilität.",
            listOf("quadrature", "euler_rk4"),
            sec("Gleitkomma und Kondition", """
                Doppelte Genauigkeit: Maschinengenauigkeit ε ≈ 2,2·10⁻¹⁶. Auslöschung beim Subtrahieren fast gleicher Zahlen — deshalb √(x+1) − √x = 1/(√(x+1) + √x) umformen und quadratische Gleichungen über q = −(b + sgn(b)√D)/2 lösen. Die Kondition eines Problems ist eine Eigenschaft der Aufgabe, die Stabilität eine des Algorithmus.
            """,
                fm("Maschinengenauigkeit", "ε = 2⁻⁵² ≈ 2,2·10⁻¹⁶"),
                fm("Relative Kondition", "κ = |x f'(x)/f(x)|"),
                fm("Stabile Mitternachtsformel", "q = −(b + sgn(b)√(b² − 4ac))/2,   x₁ = q/a,   x₂ = c/q")
            ),
            sec("Nullstellen und Interpolation", """
                Bisektion (linear, sicher), Sekante (Ordnung ≈ 1,618), Newton (quadratisch, braucht Ableitung und guten Start), Brent (kombiniert alles, sicher und schnell). Interpolation mit Polynomen hohen Grades auf äquidistanten Stützstellen oszilliert (Runge); besser Tschebyschow-Stützstellen oder Splines.
            """,
                fm("Lagrange-Interpolation", "p(x) = Σ yᵢ Πⱼ≠ᵢ (x − xⱼ)/(xᵢ − xⱼ)"),
                fm("Interpolationsfehler", "f(x) − p(x) = f⁽ⁿ⁺¹⁾(ξ)/(n+1)! · Π(x − xᵢ)"),
                fm("Sekantenverfahren", "x_{n+1} = xₙ − f(xₙ)(xₙ − x_{n−1})/(f(xₙ) − f(x_{n−1}))")
            ),
            sec("Quadratur und lineare Systeme", """
                Trapez O(h²), Simpson O(h⁴), Gauß-Legendre mit n Punkten exakt bis Grad 2n − 1; Romberg extrapoliert Trapezsummen. Lineare Systeme: LU (Aufwand n³/3), Cholesky für symmetrisch positiv definite Matrizen (n³/6), iterativ (Jacobi, Gauß-Seidel, konjugierte Gradienten) für große dünnbesetzte Systeme aus PDGL.
            """,
                fm("Trapezregel", "∫ f ≈ h(f₀/2 + f₁ + … + f_{n−1} + fₙ/2),   Fehler −(b−a)h²f''/12"),
                fm("Simpson", "∫ f ≈ (h/3)(f₀ + 4f₁ + 2f₂ + … + 4f_{n−1} + fₙ)"),
                fm("Richardson-Extrapolation", "A ≈ (2ᵖ A(h/2) − A(h))/(2ᵖ − 1)")
            )
        )
    )
}

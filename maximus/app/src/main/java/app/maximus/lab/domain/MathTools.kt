package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

object MathTools {
    /** Complete elliptic integral of the first kind K(k) by the arithmetic–geometric mean: K = π / (2 AGM(1, √(1−k²))). */
    fun ellipticK(k: Double): Double {
        var a = 1.0; var b = sqrt(1 - k * k)
        repeat(40) { val an = (a + b) / 2; b = sqrt(a * b); a = an }
        return PI / (2 * a)
    }

    /** Partial Fourier sums of three classic periodic signals with period 2π. */
    fun fourierPartial(kind: Int, x: Double, terms: Int): Double = when (kind) {
        0 -> (0 until terms).sumOf { k -> val n = 2 * k + 1; 4 / PI * sin(n * x) / n }              // square
        1 -> (1..terms).sumOf { n -> 2 * (if (n % 2 == 1) 1 else -1) * sin(n * x) / n }              // sawtooth x on (−π, π)
        else -> (0 until terms).sumOf { k -> val n = 2 * k + 1; 8 / (PI * PI) * (if (k % 2 == 0) 1 else -1) * sin(n * x) / (n * n) } // triangle
    }

    fun fourierExact(kind: Int, x: Double): Double {
        val y = ((x + PI) % (2 * PI) + 2 * PI) % (2 * PI) - PI
        return when (kind) {
            0 -> if (y >= 0) 1.0 else -1.0
            1 -> y
            else -> if (abs(y) <= PI / 2) 2 * y / PI else 2 * (if (y > 0) PI - y else -PI - y) / PI
        }
    }

    private val TAYLOR_FUNCS = listOf("sin x", "cos x", "eˣ", "ln(1+x)", "1/(1−x)", "arctan x")
    private fun taylorTerm(kind: Int, n: Int): Double = when (kind) {
        0 -> if (n % 2 == 1) (if ((n / 2) % 2 == 0) 1.0 else -1.0) / factorial(n) else 0.0
        1 -> if (n % 2 == 0) (if ((n / 2) % 2 == 0) 1.0 else -1.0) / factorial(n) else 0.0
        2 -> 1.0 / factorial(n)
        3 -> if (n == 0) 0.0 else (if (n % 2 == 1) 1.0 else -1.0) / n
        4 -> 1.0
        else -> if (n % 2 == 1) (if ((n / 2) % 2 == 0) 1.0 else -1.0) / n else 0.0
    }
    private fun taylorExact(kind: Int, x: Double) = when (kind) { 0 -> sin(x); 1 -> cos(x); 2 -> exp(x); 3 -> ln(1 + x); 4 -> 1 / (1 - x); else -> atan(x) }
    private fun factorial(n: Int): Double = (1..n).fold(1.0) { a, k -> a * k }

    val calculators: List<Calculator> = listOf(
        Calculator(
            "polynomial", Topic.MATH, "Polynomgleichungen bis Grad 4",
            "Alle reellen und komplexen Nullstellen von a₄x⁴ + a₃x³ + a₂x² + a₁x + a₀ als Eigenwerte der Begleitmatrix (Francis-QR-Verfahren). Nach dem Fundamentalsatz der Algebra gibt es genau so viele Nullstellen wie der Grad, mit Vielfachheit.",
            "p(x) = Σ aₖxᵏ,   Begleitmatrix C mit det(xI − C) = p(x)/aₙ,   Vieta: Σxᵢ = −a_{n−1}/aₙ,   Πxᵢ = (−1)ⁿa₀/aₙ",
            listOf(Param("a4", "a₄", "", 0.0), Param("a3", "a₃", "", 1.0), Param("a2", "a₂", "", -6.0), Param("a1", "a₁", "", 11.0), Param("a0", "a₀", "", -6.0))
        ) { v ->
            val c = listOf(v.getValue("a0"), v.getValue("a1"), v.getValue("a2"), v.getValue("a3"), v.getValue("a4"))
            val roots = Polynomials.roots(c)
            val deg = c.indexOfLast { it != 0.0 }
            fun p(x: Double) = c.indices.sumOf { c[it] * x.pow(it) }
            val real = roots.filter { abs(it.im) < 1e-9 }.map { it.re }
            val span = max(3.0, (real.maxOfOrNull { abs(it) } ?: 1.0) * 1.5)
            val xs = grid(-span, span, 300)
            CalcResult(
                roots.mapIndexed { i, r -> Output("x${i + 1}", r.re, display = r.format(6)) } +
                    listOf(Output("Grad", deg.toDouble(), digits = 2), Output("Diskriminante (Grad 2)", if (deg == 2) c[1] * c[1] - 4 * c[2] * c[0] else Double.NaN)),
                listOf(Curve("p(x)", "x", "p(x)", listOf(CurveSeries("p", xs, xs.map { p(it) })), run { val ys = xs.map { p(it) }; val m = ys.maxOf { abs(it) }.coerceAtMost(1e6); -m * 0.6 to m * 0.6 })),
                if (deg < 1) listOf("Konstantes Polynom: keine Nullstellen (oder alle).") else emptyList()
            )
        },
        Calculator(
            "matrix3", Topic.MATH, "3×3-Matrix: Determinante, Inverse, Eigenwerte",
            "LU-Zerlegung mit Spaltenpivotisierung für Determinante und Inverse, Hessenberg-Reduktion und Francis-Doppelshift-QR für die Eigenwerte. Spur = Summe, Determinante = Produkt der Eigenwerte.",
            "det A = Π λᵢ,   tr A = Σ λᵢ,   A⁻¹ = adj(A)/det A,   det(A − λI) = 0",
            listOf(
                Param("a11", "a₁₁", "", 2.0), Param("a12", "a₁₂", "", 1.0), Param("a13", "a₁₃", "", 0.0),
                Param("a21", "a₂₁", "", 1.0), Param("a22", "a₂₂", "", 3.0), Param("a23", "a₂₃", "", 1.0),
                Param("a31", "a₃₁", "", 0.0), Param("a32", "a₃₂", "", 1.0), Param("a33", "a₃₃", "", 4.0)
            )
        ) { v ->
            val m = Matrix.of(
                doubleArrayOf(v.getValue("a11"), v.getValue("a12"), v.getValue("a13")),
                doubleArrayOf(v.getValue("a21"), v.getValue("a22"), v.getValue("a23")),
                doubleArrayOf(v.getValue("a31"), v.getValue("a32"), v.getValue("a33"))
            )
            val inv = m.inverse()
            val ev = m.eigenvalues()
            CalcResult(
                listOf(Output("Determinante", m.determinant(), digits = 6), Output("Spur", m.trace(), digits = 6), Output("Rang", m.rank().toDouble(), digits = 2)) +
                    ev.mapIndexed { i, e -> Output("λ${i + 1}", e.re, display = e.format(6)) } +
                    (inv?.let { iv -> (0 until 3).map { r -> Output("Zeile ${r + 1} von A⁻¹", 0.0, display = (0 until 3).joinToString("   ") { Fmt.num(iv[r, it], 5) }) } } ?: listOf(Output("A⁻¹", 0.0, display = "existiert nicht (singulär)"))),
                steps = listOf("Probe: Σλ = ${Fmt.num(ev.sumOf { it.re }, 6)} = Spur, Πλ = ${ev.fold(Complex(1.0)) { a, b -> a * b }.format(6)} = det")
            )
        },
        Calculator(
            "complex", Topic.MATH, "Komplexe Zahlen",
            "Rechnen in kartesischer und Polarform. Multiplikation dreht und streckt, Division dreht zurück. Die n-ten Wurzeln liegen gleichmäßig auf einem Kreis.",
            "z = a + bi = r e^{iφ},   r = √(a² + b²),   φ = atan2(b, a),   z₁z₂ = r₁r₂ e^{i(φ₁+φ₂)},   ⁿ√z = ⁿ√r e^{i(φ + 2πk)/n}",
            listOf(Param("a", "Re z₁", "", 3.0), Param("b", "Im z₁", "", 4.0), Param("c", "Re z₂", "", 1.0), Param("d", "Im z₂", "", -2.0), Param("n", "Wurzelgrad n (für z₁)", "", 3.0, 1.0, 12.0))
        ) { v ->
            val z1 = Complex(v.getValue("a"), v.getValue("b")); val z2 = Complex(v.getValue("c"), v.getValue("d")); val n = v.getValue("n").toInt()
            val roots = (0 until n).map { k -> Complex.polar(z1.abs.pow(1.0 / n), (z1.arg + 2 * PI * k) / n) }
            val circle = grid(0.0, 2 * PI, 120)
            val rr = z1.abs.pow(1.0 / n)
            CalcResult(
                listOf(
                    Output("|z₁|", z1.abs), Output("arg z₁", z1.arg * 180 / PI, "°"),
                    Output("z₁ + z₂", 0.0, display = (z1 + z2).format(5)), Output("z₁ · z₂", 0.0, display = (z1 * z2).format(5)),
                    Output("z₁ / z₂", 0.0, display = (z1 / z2).format(5)), Output("e^{z₁}", 0.0, display = z1.exp().format(5)),
                    Output("ln z₁ (Hauptwert)", 0.0, display = z1.ln().format(5))
                ) + roots.mapIndexed { k, r -> Output("Wurzel ${k + 1}", 0.0, display = r.format(5)) },
                listOf(Curve("$n-te Wurzeln von z₁ in der Gaußschen Ebene (Im über Re)", "Re", "Im", listOf(
                    CurveSeries("Kreis |w| = ⁿ√|z₁|", circle.map { rr * cos(it) }, circle.map { rr * sin(it) }, dashed = true),
                    CurveSeries("Wurzeln (Polygon)", (roots + roots.first()).map { it.re }, (roots + roots.first()).map { it.im })
                )))
            )
        },
        Calculator(
            "fourier", Topic.MATH, "Fourier-Reihen und Gibbs-Phänomen",
            "Jede stückweise glatte periodische Funktion ist eine Summe von Sinus- und Kosinusschwingungen. An Sprungstellen überschwingt die Partialsumme um ≈ 8,95 % der Sprunghöhe — unabhängig von der Termzahl (Gibbs). Die Koeffizienten fallen wie 1/n (Sprung) bzw. 1/n² (Knick).",
            "f(x) = a₀/2 + Σ (aₙ cos nx + bₙ sin nx),   bₙ = (1/π)∫_{−π}^{π} f(x) sin nx dx;   Rechteck: (4/π) Σ sin((2k+1)x)/(2k+1)",
            listOf(Param("kind", "Signal", "", 0.0, choices = listOf("Rechteck", "Sägezahn", "Dreieck")), Param("N", "Anzahl Terme N", "", 7.0, 1.0, 200.0))
        ) { v ->
            val kind = v.getValue("kind").toInt(); val n = v.getValue("N").toInt()
            val xs = grid(-PI, PI, 600)
            val partial = xs.map { fourierPartial(kind, it, n) }
            val overshoot = partial.maxOrNull() ?: 0.0
            val l2 = sqrt(xs.indices.sumOf { (partial[it] - fourierExact(kind, xs[it])).pow(2) } / xs.size)
            CalcResult(
                listOf(Output("Maximum der Partialsumme", overshoot, ""), Output("Überschwinger (Sprung 2 → Anteil)", if (kind == 0) (overshoot - 1) / 2 else Double.NaN, "", "Grenzwert 0,0895"), Output("RMS-Fehler", l2, "")),
                listOf(Curve("Partialsumme mit $n Termen", "x", "f(x)", listOf(
                    CurveSeries("Partialsumme", xs, partial),
                    CurveSeries("exakt", xs, xs.map { fourierExact(kind, it) }, dashed = true)
                )))
            )
        },
        Calculator(
            "taylor", Topic.MATH, "Taylor-Reihen und Konvergenzradius",
            "Polynome approximieren glatte Funktionen um x₀ = 0. Der Fehler wird durch das Restglied begrenzt; außerhalb des Konvergenzradius (z. B. |x| < 1 für ln(1+x) und 1/(1−x), bestimmt durch die nächste Singularität in ℂ) divergiert die Reihe.",
            "f(x) = Σ f⁽ⁿ⁾(0)xⁿ/n!,   R_N(x) = f⁽ᴺ⁺¹⁾(ξ)x^{N+1}/(N+1)!,   arctan: Radius 1 wegen der Pole bei ±i",
            listOf(Param("f", "Funktion", "", 0.0, choices = TAYLOR_FUNCS), Param("N", "Ordnung N", "", 5.0, 0.0, 40.0), Param("x", "Auswertestelle x", "", 0.5, -10.0, 10.0))
        ) { v ->
            val kind = v.getValue("f").toInt(); val n = v.getValue("N").toInt(); val x = v.getValue("x")
            fun poly(xx: Double, order: Int) = (0..order).sumOf { taylorTerm(kind, it) * xx.pow(it) }
            val range = if (kind in 3..5) 1.6 else 6.0
            val xs = grid(-range, range, 300).filter { kind != 3 || it > -0.999 }.filter { kind != 4 || abs(it - 1) > 0.02 }
            val exact = taylorExact(kind, x)
            CalcResult(
                listOf(Output("T_N(x)", poly(x, n), digits = 8), Output("f(x)", exact, digits = 8), Output("Fehler |f − T_N|", abs(exact - poly(x, n)), digits = 3)),
                listOf(Curve("${TAYLOR_FUNCS[kind]} und Taylor-Polynome", "x", "y", listOf(CurveSeries("exakt", xs, xs.map { taylorExact(kind, it) })) +
                    listOf(1, 3, n).distinct().filter { it <= n || it == n }.map { o -> CurveSeries("T_$o", xs, xs.map { poly(it, o) }, dashed = true) },
                    -4.0 to 4.0))
            )
        },
        Calculator(
            "quadrature", Topic.MATH, "Numerische Integration: Konvergenzordnungen",
            "Fehler von Rechteck-, Trapez-, Simpson- und Gauß-Legendre-Regel für ∫₀^π sin x dx = 2 bei n Teilintervallen. Im log-log-Diagramm ist die Steigung die Ordnung: 2 (Mittelpunkt, Trapez), 4 (Simpson), 10 (Gauß, 5 Punkte).",
            "Trapez: E ≈ −(b−a)h²f''/12,   Simpson: E ≈ −(b−a)h⁴f⁽⁴⁾/180,   Gauß-n: exakt bis Grad 2n−1",
            listOf(Param("n", "Teilintervalle n", "", 8.0, 1.0, 1000.0))
        ) { v ->
            val f = { x: Double -> sin(x) }
            fun mid(n: Int): Double { val h = PI / n; return h * (0 until n).sumOf { f((it + 0.5) * h) } }
            fun trap(n: Int): Double { val h = PI / n; return h * ((1 until n).sumOf { f(it * h) } + (f(0.0) + f(PI)) / 2) }
            fun simp(n0: Int): Double { val n = if (n0 % 2 == 0) n0 else n0 + 1; val h = PI / n; return h / 3 * (f(0.0) + f(PI) + (1 until n).sumOf { (if (it % 2 == 1) 4 else 2) * f(it * h) }) }
            fun gauss(n: Int) = Calculus.gaussLegendre(f, 0.0, PI, n)
            val n = v.getValue("n").toInt()
            val ns = listOf(1, 2, 4, 8, 16, 32, 64, 128)
            fun lg(e: Double) = log10(max(abs(e), 1e-16))
            CalcResult(
                listOf(Output("Mittelpunkt", mid(n), digits = 12), Output("Trapez", trap(n), digits = 12), Output("Simpson", simp(n), digits = 12), Output("Gauß-Legendre (5 Pkt./Intervall)", gauss(n), digits = 14)),
                listOf(Curve("Fehler gegen n (log-log)", "log₁₀ n", "log₁₀ |Fehler|", listOf(
                    CurveSeries("Mittelpunkt", ns.map { log10(it.toDouble()) }, ns.map { lg(mid(it) - 2) }),
                    CurveSeries("Trapez", ns.map { log10(it.toDouble()) }, ns.map { lg(trap(it) - 2) }, dashed = true),
                    CurveSeries("Simpson", ns.map { log10(it.toDouble()) }, ns.map { lg(simp(it) - 2) }),
                    CurveSeries("Gauß", ns.map { log10(it.toDouble()) }, ns.map { lg(gauss(it) - 2) }, dashed = true)
                )))
            )
        },
        Calculator(
            "pendulum", Topic.MATH, "Nichtlineares Pendel (DGL)",
            "θ'' = −(g/L) sin θ ist nicht elementar lösbar. Die Periode folgt exakt aus dem vollständigen elliptischen Integral; numerisch löst das Runge-Kutta-Verfahren 4. Ordnung. Für kleine Ausschläge ergibt sin θ ≈ θ die harmonische Näherung.",
            "θ'' + (g/L) sin θ = 0,   T = 4√(L/g) K(sin(θ₀/2)),   T ≈ 2π√(L/g)(1 + θ₀²/16 + 11θ₀⁴/3072)",
            listOf(Param("L", "Länge L", "m", 1.0, 0.01, 1e4), Param("theta", "Amplitude θ₀", "°", 60.0, 0.1, 179.9), Param("g", "Fallbeschleunigung g", "m/s²", 9.81, 0.01, 300.0))
        ) { v ->
            val l = v.getValue("L"); val th0 = v.getValue("theta") * PI / 180; val g = v.getValue("g")
            val t0 = 2 * PI * sqrt(l / g)
            val texact = 4 * sqrt(l / g) * ellipticK(sin(th0 / 2))
            val sol = Ode.rk4({ _, y -> doubleArrayOf(y[1], -g / l * sin(y[0])) }, doubleArrayOf(th0, 0.0), 0.0, 3 * texact, 1500)
            val amps = grid(0.05, 3.1, 120)
            CalcResult(
                listOf(Output("Harmonische Periode T₀", t0, "s"), Output("Exakte Periode T", texact, "s"), Output("Verhältnis T/T₀", texact / t0, ""), Output("Reihennäherung", t0 * (1 + th0 * th0 / 16 + 11 * th0.pow(4) / 3072), "s"),
                    Output("Max. Geschwindigkeit (Energiesatz)", sqrt(2 * g * l * (1 - cos(th0))), "m/s")),
                listOf(
                    Curve("Auslenkung θ(t)", "t in s", "θ in Grad", listOf(
                        CurveSeries("nichtlinear (RK4)", sol.t, sol.y.map { it[0] * 180 / PI }),
                        CurveSeries("harmonisch", sol.t, sol.t.map { th0 * cos(2 * PI * it / t0) * 180 / PI }, dashed = true)
                    )),
                    Curve("Periode gegen Amplitude", "θ₀ in Grad", "T/T₀", listOf(CurveSeries("exakt", amps.map { it * 180 / PI }, amps.map { 2 / PI * ellipticK(sin(it / 2)) })))
                )
            )
        },
        Calculator(
            "lorenz", Topic.MATH, "Lorenz-Attraktor (deterministisches Chaos)",
            "Drei gekoppelte nichtlineare DGL (Konvektionsmodell). Für r > 24,74 ist die Dynamik chaotisch: Benachbarte Bahnen entfernen sich exponentiell (Lyapunov-Exponent ≈ 0,9). Die Kurve zeigt die x-z-Projektion und zwei Läufe mit 10⁻⁸ Unterschied.",
            "ẋ = σ(y − x),   ẏ = x(r − z) − y,   ż = xy − bz;   σ = 10, b = 8/3, r = 28 (klassisch)",
            listOf(Param("r", "Rayleigh-Zahl r", "", 28.0, 0.0, 200.0), Param("t", "Zeitspanne", "", 30.0, 1.0, 100.0))
        ) { v ->
            val r = v.getValue("r"); val tEnd = v.getValue("t")
            val f = { _: Double, y: DoubleArray -> doubleArrayOf(10 * (y[1] - y[0]), y[0] * (r - y[2]) - y[1], y[0] * y[1] - 8.0 / 3 * y[2]) }
            val steps = (tEnd * 200).toInt()
            val a = Ode.rk4(f, doubleArrayOf(1.0, 1.0, 1.0), 0.0, tEnd, steps)
            val b = Ode.rk4(f, doubleArrayOf(1.0 + 1e-8, 1.0, 1.0), 0.0, tEnd, steps)
            val sep = a.y.indices.map { i -> sqrt((0..2).sumOf { (a.y[i][it] - b.y[i][it]).pow(2) }) }
            val idx = sep.indices.filter { sep[it] in 1e-6..1.0 }
            val lyap = if (idx.size > 10) Stats.linearFit(idx.map { a.t[it] }, idx.map { ln(sep[it]) })?.slope else null
            CalcResult(
                listOf(Output("Geschätzter Lyapunov-Exponent", lyap ?: Double.NaN, "", "Literatur ≈ 0,906"), Output("Endzustand x", a.y.last()[0]), Output("Abstand der Läufe am Ende", sep.last())),
                listOf(
                    Curve("Projektion auf x-z", "x", "z", listOf(CurveSeries("Bahn", a.y.map { it[0] }, a.y.map { it[2] }))),
                    Curve("Abstand zweier Bahnen (log)", "t", "log₁₀ |δ|", listOf(CurveSeries("δ(t)", a.t, sep.map { log10(max(it, 1e-16)) })))
                )
            )
        },
        Calculator(
            "binomial", Topic.MATH, "Binomial-, Poisson- und Normalverteilung",
            "Die Binomialverteilung B(n, p) zählt Erfolge in n unabhängigen Versuchen. Für große n nähert sie sich der Normalverteilung (Satz von de Moivre-Laplace, zentraler Grenzwertsatz), für kleines p bei festem np der Poisson-Verteilung.",
            "P(X = k) = C(n,k) pᵏ(1−p)^{n−k},   μ = np,   σ² = np(1−p),   Poisson: λᵏe^{−λ}/k!",
            listOf(Param("n", "Versuche n", "", 30.0, 1.0, 2000.0), Param("p", "Erfolgswahrscheinlichkeit p", "", 0.3, 0.0, 1.0), Param("k", "k für P(X ≤ k)", "", 10.0, 0.0, 2000.0))
        ) { v ->
            val n = v.getValue("n").toInt(); val p = v.getValue("p"); val k = v.getValue("k").toInt()
            val mu = n * p; val sd = sqrt(n * p * (1 - p))
            val ks = (0..n).map { it.toDouble() }
            val cdf = (0..minOf(k, n)).sumOf { Stats.binomial(n, it, p) }
            CalcResult(
                listOf(Output("Erwartungswert μ", mu), Output("Standardabweichung σ", sd), Output("P(X ≤ k) exakt", cdf), Output("Normalnäherung mit Stetigkeitskorrektur", Stats.normalCdf((k + 0.5 - mu) / sd)), Output("P(X = k)", Stats.binomial(n, k, p))),
                listOf(Curve("Verteilung", "k", "P", listOf(
                    CurveSeries("Binomial", ks, ks.map { Stats.binomial(n, it.toInt(), p) }),
                    CurveSeries("Normal", ks, ks.map { Stats.normalPdf((it - mu) / sd) / sd }, dashed = true),
                    CurveSeries("Poisson(λ = np)", ks, ks.map { Stats.poisson(it.toInt(), mu) }, dashed = true)
                )))
            )
        },
        Calculator(
            "euler_rk4", Topic.MATH, "Euler gegen Runge-Kutta",
            "Testproblem y' = λy, y(0) = 1, exakte Lösung e^{λt}. Das explizite Euler-Verfahren ist erster Ordnung und für λh < −2 instabil; RK4 ist vierter Ordnung. Halbiert man h, sinkt der Fehler um 2 bzw. 16.",
            "Euler: yₙ₊₁ = yₙ + h f(tₙ, yₙ),   RK4: yₙ₊₁ = yₙ + (h/6)(k₁ + 2k₂ + 2k₃ + k₄),   Stabilität Euler: |1 + λh| ≤ 1",
            listOf(Param("lambda", "λ", "", -1.0, -50.0, 5.0), Param("h", "Schrittweite h", "", 0.25, 1e-4, 5.0), Param("T", "Endzeit T", "", 5.0, 0.1, 100.0))
        ) { v ->
            val lam = v.getValue("lambda"); val h = v.getValue("h"); val tEnd = v.getValue("T")
            val steps = max(1, round(tEnd / h).toInt())
            val f = { _: Double, y: DoubleArray -> doubleArrayOf(lam * y[0]) }
            val e = Ode.euler(f, doubleArrayOf(1.0), 0.0, tEnd, steps)
            val r = Ode.rk4(f, doubleArrayOf(1.0), 0.0, tEnd, steps)
            val exact = exp(lam * tEnd)
            CalcResult(
                listOf(Output("Exakt y(T)", exact, digits = 8), Output("Euler", e.y.last()[0], digits = 8), Output("RK4", r.y.last()[0], digits = 8),
                    Output("Fehler Euler", abs(e.y.last()[0] - exact), digits = 3), Output("Fehler RK4", abs(r.y.last()[0] - exact), digits = 3), Output("λh", lam * h, "", if (lam * h < -2) "Euler instabil!" else "")),
                listOf(Curve("Lösungen", "t", "y", listOf(
                    CurveSeries("exakt", grid(0.0, tEnd, 200), grid(0.0, tEnd, 200).map { exp(lam * it) }),
                    CurveSeries("Euler", e.t, e.y.map { it[0] }, dashed = true),
                    CurveSeries("RK4", r.t, r.y.map { it[0] }, dashed = true)
                )))
            )
        }
    )
}

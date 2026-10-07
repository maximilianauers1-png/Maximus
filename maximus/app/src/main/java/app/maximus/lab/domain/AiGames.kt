package app.maximus.lab.domain

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** The games of the "AI-Spielwiese". Each returns a score 0…100 for XP; all logic is pure and seeded. */
enum class AiGame(val title: String, val tagline: String, val chapterKey: String) {
    TECHNIQUE("Technik-Detektiv", "Welche der sieben ML-Techniken passt zum Fall?", "ml_techniques"),
    GRADIENT("Gradientenabstieg", "Finde mit der richtigen Lernrate ins Tal", "ai0_model"),
    PERCEPTRON("Perzeptron", "Trenne zwei Klassen mit einer Geraden – von Hand und lernend", "dl_neuron"),
    KMEANS("k-Means", "Zuordnen, verschieben, wiederholen: Cluster finden", "alg_kmeans"),
    OVERFIT("Überanpassung", "Wähle den Polynomgrad, der neue Daten am besten trifft", "ev_tuning"),
    THRESHOLD("Schwellen-Regler", "Precision gegen Recall: erfülle die Mission", "ev_metrics"),
    TREE("Baumeister", "Wähle die Frage mit dem größten Informationsgewinn", "alg_trees"),
    QLEARN("Q-Learning-Labyrinth", "Ein Agent lernt den Weg zum Schatz", "rl_qlearning"),
    GLOSSARY("Begriffe-Duell", "Fachbegriffe gegen die Uhr", "ai0_what")
}

// =====================================================================================================
// Technik-Detektiv
// =====================================================================================================

enum class MlTechnique(val title: String) {
    REGRESSION("Regression"), CLASSIFICATION("Klassifikation"), CLUSTERING("Clustering"), ASSOCIATION("Assoziation"),
    ANOMALY("Anomalieerkennung"), SEQUENCE("Sequenz-Mining"), RECOMMENDATION("Empfehlungssystem"), DIMRED("Dimensionsreduktion")
}

data class TechniqueCase(val scenario: String, val answer: MlTechnique, val why: String)

object TechniqueDetective {
    private fun t(s: String, a: MlTechnique, w: String) = TechniqueCase(s, a, w)
    private val R = MlTechnique.REGRESSION
    private val C = MlTechnique.CLASSIFICATION
    private val CL = MlTechnique.CLUSTERING
    private val AS = MlTechnique.ASSOCIATION
    private val AN = MlTechnique.ANOMALY
    private val SQ = MlTechnique.SEQUENCE
    private val RE = MlTechnique.RECOMMENDATION
    private val DR = MlTechnique.DIMRED

    val cases: List<TechniqueCase> = listOf(
        t("Schätze den Verkaufspreis eines Gebrauchtwagens aus Alter, Kilometerstand und Marke.", R, "Zielgröße ist eine Zahl (Euro)."),
        t("Sage voraus, wie viele Kilowattstunden ein Haus morgen verbraucht.", R, "Eine kontinuierliche Zahl."),
        t("Schätze dein e1RM im Kreuzheben in 12 Wochen aus deinem Trainingslog.", R, "Eine Zahl in kg – Regression (auf einer Zeitreihe)."),
        t("Schätze die Ankunftszeit eines Pakets in Stunden.", R, "Zahl vorhersagen."),
        t("Sage den CO₂-Ausstoß eines Autos aus Motorgröße und Zylinderzahl voraus.", R, "Klassisches IBM-Beispiel für Regression."),
        t("Bestimme die Temperatur, bei der ein Polymer schmilzt, aus seiner Molekülstruktur.", R, "Eine physikalische Zahl."),
        t("Entscheide, ob eine E-Mail Spam ist.", C, "Zwei Klassen mit Beispielen."),
        t("Erkenne, welche Ziffer (0–9) auf einem handgeschriebenen Bild steht.", C, "Zehn Klassen, genau eine richtig."),
        t("Sage voraus, ob ein Kunde seinen Vertrag im nächsten Monat kündigt.", C, "Ja/Nein – Churn-Klassifikation."),
        t("Bestimme, ob ein Tumor gutartig oder bösartig ist.", C, "Zwei Klassen, historische Diagnosen als Labels."),
        t("Entscheide, ob ein Kreditantrag bewilligt werden sollte (zahlt zurück oder nicht).", C, "Binäre Klassifikation mit Ausfallhistorie."),
        t("Ordne Kundenbewertungen als positiv, neutral oder negativ ein.", C, "Drei Klassen – Sentiment-Klassifikation."),
        t("Erkenne auf einem Foto, ob ein Hund oder eine Katze zu sehen ist.", C, "Bildklassifikation."),
        t("Teile Kunden in Gruppen mit ähnlichem Kaufverhalten ein, ohne die Gruppen vorzugeben.", CL, "Keine Labels, Gruppen entstehen – Segmentierung."),
        t("Gruppiere Nachrichtenartikel nach Themen, ohne dass Themen vorgegeben sind.", CL, "Unüberwacht gruppieren."),
        t("Finde Gruppen von Sternen mit ähnlichen Spektren in einem Himmelskatalog.", CL, "Ähnlichkeitsgruppen ohne Vorwissen."),
        t("Fasse deine Trainingstage nach Art (schwer, leicht, Event-Tag) zusammen, ohne sie vorher zu markieren.", CL, "Muster in ungelabelten Daten."),
        t("Reduziere ein Foto auf 16 typische Farben.", CL, "k-Means auf den Pixelfarben, k = 16."),
        t("Finde heraus, welche Produkte im Baumarkt oft zusammen gekauft werden.", AS, "Warenkorbanalyse."),
        t("Erkenne, welche Symptome bei Patienten häufig gemeinsam auftreten.", AS, "Häufige Itemsets in Transaktionen."),
        t("Ein Online-Shop will ‚Häufig zusammen gekauft‘-Bundles schnüren.", AS, "Support, Konfidenz, Lift."),
        t("Finde betrügerische Kreditkartenzahlungen unter Millionen normaler.", AN, "Seltene, auffällige Ereignisse."),
        t("Erkenne einen defekten Sensor, der plötzlich unplausible Werte liefert.", AN, "Abweichung vom Normalverhalten."),
        t("Entdecke ungewöhnliche Zugriffe in einem Firmennetzwerk.", AN, "Intrusion Detection."),
        t("Bemerke, wenn eine Maschine in der Fabrik ungewöhnlich vibriert.", AN, "Normalverhalten lernen, Abweichungen melden."),
        t("Finde typische Reihenfolgen, in denen Besucher die Seiten eines Shops ansehen.", SQ, "Sequentielle Muster in Klickpfaden."),
        t("Sage das nächste Wort in einem Satz voraus.", SQ, "Reihenfolge zählt – Sequenzmodell (Sprachmodell)."),
        t("Erkenne Muster in DNA-Abfolgen.", SQ, "Geordnete Folgen."),
        t("Finde heraus, dass Kunden nach einem Zelt oft einen Schlafsack und dann eine Isomatte kaufen.", SQ, "Reihenfolge der Käufe – nicht nur gemeinsames Auftreten."),
        t("Schlage einem Nutzer Filme vor, die ihm gefallen könnten.", RE, "Nutzer × Objekte × Vorlieben."),
        t("Empfiehl Songs auf Basis dessen, was ähnliche Hörer mögen.", RE, "Kollaboratives Filtern."),
        t("Zeige in einem Shop ‚Kunden, die das gekauft haben, kauften auch …‘ personalisiert für jeden Nutzer.", RE, "Personalisierte Empfehlung."),
        t("Schlage einem Forscher passende Fachartikel zu seinen bisherigen Lesern vor.", RE, "Inhaltsbasierte Empfehlung."),
        t("Stelle 50 Messgrößen pro Probe in einem 2D-Bild dar.", DR, "PCA oder t-SNE."),
        t("Komprimiere 1000 Merkmale auf 20, bevor ein Modell trainiert wird.", DR, "Merkmalsextraktion."),
        t("Finde die wenigen Richtungen, in denen deine Messdaten am stärksten schwanken.", DR, "Hauptkomponentenanalyse.")
    )

    fun round(seed: Long, n: Int = 10): List<TechniqueCase> = cases.shuffled(Random(seed)).take(n)

    /** Score: 10 points per correct answer, +2 per answer within a streak of three or more, capped at 100. */
    fun score(correctFlags: List<Boolean>): Int {
        var s = 0; var streak = 0
        for (c in correctFlags) { if (c) { streak++; s += 10 + if (streak >= 3) 2 else 0 } else streak = 0 }
        return min(100, s)
    }
}

// =====================================================================================================
// Gradientenabstieg
// =====================================================================================================

data class LossLandscape(val title: String, val f: (Double) -> Double, val df: (Double) -> Double, val start: Double,
                         val range: Pair<Double, Double>, val minimum: Double, val hint: String)

data class GdLevel(val landscape: LossLandscape, val steps: Int, val targetLoss: Double, val allowMomentum: Boolean)

object GradientGame {
    val levels: List<GdLevel> = listOf(
        GdLevel(LossLandscape("Sanftes Tal", { w -> (w - 2) * (w - 2) }, { w -> 2 * (w - 2) }, -3.0, -4.0 to 6.0, 0.0,
            "L = (w − 2)². Krümmung 2 ⇒ stabil für η < 1, am schnellsten bei η = 0,5."), 10, 0.01, false),
        GdLevel(LossLandscape("Steile Schlucht", { w -> 5 * w * w }, { w -> 10 * w }, 3.0, -4.0 to 4.0, 0.0,
            "L = 5w². Krümmung 10 ⇒ Divergenz ab η > 0,2."), 8, 0.01, false),
        GdLevel(LossLandscape("Flache Ebene", { w -> 0.05 * (w + 1) * (w + 1) }, { w -> 0.1 * (w + 1) }, 8.0, -4.0 to 10.0, 0.0,
            "L = 0,05(w + 1)². Sehr flach – eine kleine Lernrate kommt nie an."), 12, 0.05, true),
        GdLevel(LossLandscape("Doppeltal", { w -> w * w * w * w - 3 * w * w + w }, { w -> 4 * w * w * w - 6 * w + 1 }, 2.0, -2.3 to 2.3,
            -3.5139, "L = w⁴ − 3w² + w. Rechts liegt ein lokales, links das globale Minimum – Momentum hilft hinüber."), 25, -3.4, true),
        GdLevel(LossLandscape("Wellblech", { w -> 0.3 * w * w + sin(3 * w) }, { w -> 0.6 * w + 3 * cos(3 * w) }, 4.0, -5.0 to 5.0,
            -0.9, "L = 0,3w² + sin(3w): viele kleine Mulden auf einem großen Trichter."), 30, -0.8, true)
    )

    /** Gradient descent with optional heavy-ball momentum: v ← βv − η∇L, w ← w + v. Divergence stops early. */
    fun run(l: LossLandscape, eta: Double, beta: Double, steps: Int): List<Double> {
        var w = l.start; var v = 0.0
        val path = ArrayList<Double>(); path += w
        repeat(steps) {
            v = beta * v - eta * l.df(w)
            w += v
            path += w
            if (!w.isFinite() || abs(w) > 1e3) return path
        }
        return path
    }

    fun finalLoss(l: LossLandscape, path: List<Double>): Double = path.last().let { if (it.isFinite() && abs(it) <= 1e3) l.f(it) else Double.POSITIVE_INFINITY }

    /** 100 when the target is reached; otherwise partial credit by how far the loss came down from the start. */
    fun score(level: GdLevel, path: List<Double>): Int {
        val l = level.landscape
        val end = finalLoss(l, path)
        if (end <= level.targetLoss) return 100
        if (!end.isFinite()) return 0
        val start = l.f(l.start)
        val frac = ((start - end) / (start - l.minimum)).coerceIn(0.0, 1.0)
        return (frac * 80).toInt()
    }
}

// =====================================================================================================
// Perzeptron
// =====================================================================================================

data class Pt(val x: Double, val y: Double, val label: Int)

data class Perceptron(val w1: Double, val w2: Double, val b: Double) {
    fun predict(x: Double, y: Double): Int = if (w1 * x + w2 * y + b >= 0) 1 else 0
    fun accuracy(ds: List<Pt>): Double = if (ds.isEmpty()) 0.0 else ds.count { predict(it.x, it.y) == it.label }.toDouble() / ds.size

    /** Rosenblatt rule on one example: w ← w + η(y − ŷ)x. Returns the updated perceptron (unchanged if correct). */
    fun learn(p: Pt, eta: Double): Perceptron {
        val err = p.label - predict(p.x, p.y)
        return if (err == 0) this else Perceptron(w1 + eta * err * p.x, w2 + eta * err * p.y, b + eta * err)
    }
}

object PerceptronGame {
    /** Points in [−1, 1]², labelled by a random line with a margin, so the set is linearly separable. */
    fun separable(seed: Long, n: Int = 40): List<Pt> {
        val r = Random(seed)
        val a = r.nextDouble(0.0, 2 * Math.PI)
        val nx = cos(a); val ny = sin(a); val c = r.nextDouble(-0.3, 0.3)
        val out = ArrayList<Pt>()
        while (out.size < n) {
            val x = r.nextDouble(-1.0, 1.0); val y = r.nextDouble(-1.0, 1.0)
            val d = nx * x + ny * y - c
            if (abs(d) < 0.08) continue
            out += Pt(x, y, if (d > 0) 1 else 0)
        }
        return out
    }

    /** XOR-like quadrants: not linearly separable – the perceptron cannot reach 100 %. */
    fun xor(seed: Long, n: Int = 40): List<Pt> {
        val r = Random(seed)
        return List(n) {
            var x: Double; var y: Double
            do { x = r.nextDouble(-1.0, 1.0); y = r.nextDouble(-1.0, 1.0) } while (abs(x) < 0.1 || abs(y) < 0.1)
            Pt(x, y, if ((x > 0) != (y > 0)) 1 else 0)
        }
    }

    /** One epoch of the learning rule over the data in a fixed order. */
    fun epoch(p: Perceptron, ds: List<Pt>, eta: Double): Perceptron = ds.fold(p) { acc, pt -> acc.learn(pt, eta) }

    fun score(accuracy: Double): Int = (accuracy * 100).toInt().coerceIn(0, 100)
}

// =====================================================================================================
// k-Means
// =====================================================================================================

data class P2(val x: Double, val y: Double)

object KMeansGame {
    /** k Gaussian blobs in [0, 1]². */
    fun blobs(seed: Long, k: Int = 3, perBlob: Int = 18): List<P2> {
        val r = Random(seed)
        val centers = List(k) { P2(r.nextDouble(0.2, 0.8), r.nextDouble(0.2, 0.8)) }
        return centers.flatMap { c ->
            List(perBlob) {
                // Box-Muller
                val u1 = r.nextDouble(1e-9, 1.0); val u2 = r.nextDouble()
                val rr = sqrt(-2 * ln(u1)) * 0.07
                P2((c.x + rr * cos(2 * Math.PI * u2)).coerceIn(0.0, 1.0), (c.y + rr * sin(2 * Math.PI * u2)).coerceIn(0.0, 1.0))
            }
        }
    }

    private fun d2(a: P2, b: P2) = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)

    fun assign(points: List<P2>, centers: List<P2>): IntArray = IntArray(points.size) { i ->
        centers.indices.minBy { d2(points[i], centers[it]) }
    }

    /** Centers move to the mean of their points; an empty cluster keeps its center. */
    fun update(points: List<P2>, labels: IntArray, centers: List<P2>): List<P2> = centers.indices.map { k ->
        val mine = points.filterIndexed { i, _ -> labels[i] == k }
        if (mine.isEmpty()) centers[k] else P2(mine.sumOf { it.x } / mine.size, mine.sumOf { it.y } / mine.size)
    }

    fun inertia(points: List<P2>, labels: IntArray, centers: List<P2>): Double = points.indices.sumOf { d2(points[it], centers[labels[it]]) }

    /** Full Lloyd iteration from given centers until the labels stop changing. */
    fun lloyd(points: List<P2>, start: List<P2>, maxIter: Int = 100): Pair<IntArray, List<P2>> {
        var c = start
        var labels = assign(points, c)
        repeat(maxIter) {
            c = update(points, labels, c)
            val next = assign(points, c)
            if (next.contentEquals(labels)) return labels to c
            labels = next
        }
        return labels to c
    }

    /** Best inertia over many random restarts – the reference for the score. */
    fun bestInertia(points: List<P2>, k: Int, restarts: Int = 30, seed: Long = 1): Double {
        val r = Random(seed)
        return (0 until restarts).minOf {
            val (l, c) = lloyd(points, points.shuffled(r).take(k))
            inertia(points, l, c)
        }
    }

    fun score(inertia: Double, best: Double): Int = if (inertia <= 0) 100 else (100 * min(1.0, best / inertia)).toInt()
}

// =====================================================================================================
// Überanpassung (polynomial degree)
// =====================================================================================================

data class FitResult(val degree: Int, val coef: DoubleArray, val trainMse: Double, val valMse: Double) {
    fun eval(x: Double): Double { var acc = 0.0; for (i in coef.indices.reversed()) acc = acc * x + coef[i]; return acc }
}

object OverfitGame {
    data class Data(val trainX: List<Double>, val trainY: List<Double>, val valX: List<Double>, val valY: List<Double>, val truth: (Double) -> Double)

    fun data(seed: Long, nTrain: Int = 14, nVal: Int = 40, noise: Double = 0.25): Data {
        val r = Random(seed)
        val f: (Double) -> Double = { x -> sin(2.5 * x) + 0.4 * x }
        fun gauss(): Double { val u1 = r.nextDouble(1e-9, 1.0); val u2 = r.nextDouble(); return sqrt(-2 * ln(u1)) * cos(2 * Math.PI * u2) }
        val tx = List(nTrain) { r.nextDouble(-1.0, 1.0) }.sorted()
        val vx = List(nVal) { r.nextDouble(-1.0, 1.0) }.sorted()
        return Data(tx, tx.map { f(it) + noise * gauss() }, vx, vx.map { f(it) + noise * gauss() }, f)
    }

    /** Least squares polynomial fit via normal equations with a tiny ridge (1e-9) for numerical stability. */
    fun fit(d: Data, degree: Int): FitResult {
        val m = degree + 1
        val a = Array(m) { DoubleArray(m) }
        val rhs = DoubleArray(m)
        for ((x, y) in d.trainX.zip(d.trainY)) {
            val pw = DoubleArray(m); pw[0] = 1.0
            for (i in 1 until m) pw[i] = pw[i - 1] * x
            for (i in 0 until m) { rhs[i] += pw[i] * y; for (j in 0 until m) a[i][j] += pw[i] * pw[j] }
        }
        for (i in 0 until m) a[i][i] += 1e-9
        val coef = solve(a, rhs)
        val res = FitResult(degree, coef, 0.0, 0.0)
        val train = d.trainX.indices.sumOf { val e = res.eval(d.trainX[it]) - d.trainY[it]; e * e } / d.trainX.size
        val valid = d.valX.indices.sumOf { val e = res.eval(d.valX[it]) - d.valY[it]; e * e } / d.valX.size
        return res.copy(trainMse = train, valMse = valid)
    }

    /** Gaussian elimination with partial pivoting. */
    fun solve(a0: Array<DoubleArray>, b0: DoubleArray): DoubleArray {
        val n = b0.size
        val a = Array(n) { a0[it].copyOf() }; val b = b0.copyOf()
        for (c in 0 until n) {
            val p = (c until n).maxBy { abs(a[it][c]) }
            if (p != c) { val t = a[p]; a[p] = a[c]; a[c] = t; val tb = b[p]; b[p] = b[c]; b[c] = tb }
            val piv = a[c][c]
            if (abs(piv) < 1e-300) continue
            for (r in c + 1 until n) {
                val f = a[r][c] / piv
                if (f == 0.0) continue
                for (k in c until n) a[r][k] -= f * a[c][k]
                b[r] -= f * b[c]
            }
        }
        val x = DoubleArray(n)
        for (r in n - 1 downTo 0) {
            var s = b[r]
            for (k in r + 1 until n) s -= a[r][k] * x[k]
            x[r] = if (abs(a[r][r]) < 1e-300) 0.0 else s / a[r][r]
        }
        return x
    }

    val degrees = 0..12

    fun bestDegree(d: Data): Int = degrees.minBy { fit(d, it).valMse }

    /** 100 for the best degree, otherwise by how close the validation error is to the best. */
    fun score(d: Data, chosen: Int): Int {
        val best = fit(d, bestDegree(d)).valMse
        val mine = fit(d, chosen).valMse
        return (100 * (best / mine).coerceIn(0.0, 1.0)).toInt()
    }
}

// =====================================================================================================
// Schwellen-Regler
// =====================================================================================================

data class Confusion(val tp: Int, val fp: Int, val tn: Int, val fn: Int) {
    val precision: Double get() = if (tp + fp == 0) 1.0 else tp.toDouble() / (tp + fp)
    val recall: Double get() = if (tp + fn == 0) 0.0 else tp.toDouble() / (tp + fn)
    val f1: Double get() = if (precision + recall == 0.0) 0.0 else 2 * precision * recall / (precision + recall)
    val accuracy: Double get() = (tp + tn).toDouble() / max(1, tp + fp + tn + fn)
    val fpr: Double get() = if (fp + tn == 0) 0.0 else fp.toDouble() / (fp + tn)
}

enum class ThresholdMission(val title: String, val description: String) {
    SCREENING("Krebs-Vorsorge", "Recall ≥ 0,95 – kaum ein Fall darf übersehen werden. Maximiere dabei die Precision."),
    SPAM("Spamfilter", "Precision ≥ 0,95 – kaum eine echte Mail darf im Spam landen. Maximiere dabei den Recall."),
    BALANCE("Ausgewogen", "Maximiere den F1-Score.")
}

object ThresholdGame {
    data class Scores(val positives: List<Double>, val negatives: List<Double>)

    /** Classifier scores: negatives ~ N(0.35, 0.13), positives ~ N(0.65, 0.13), clipped to [0, 1]; 25 % positives. */
    fun scores(seed: Long, n: Int = 400): Scores {
        val r = Random(seed)
        fun gauss(): Double { val u1 = r.nextDouble(1e-9, 1.0); val u2 = r.nextDouble(); return sqrt(-2 * ln(u1)) * cos(2 * Math.PI * u2) }
        val pos = List(n / 4) { (0.65 + 0.13 * gauss()).coerceIn(0.0, 1.0) }
        val neg = List(n - n / 4) { (0.35 + 0.13 * gauss()).coerceIn(0.0, 1.0) }
        return Scores(pos, neg)
    }

    fun confusion(s: Scores, threshold: Double): Confusion {
        val tp = s.positives.count { it >= threshold }; val fn = s.positives.size - tp
        val fp = s.negatives.count { it >= threshold }; val tn = s.negatives.size - fp
        return Confusion(tp, fp, tn, fn)
    }

    /** Objective value of a mission, or null if its constraint is violated. */
    fun objective(m: ThresholdMission, c: Confusion): Double? = when (m) {
        ThresholdMission.SCREENING -> if (c.recall >= 0.95) c.precision else null
        ThresholdMission.SPAM -> if (c.precision >= 0.95) c.recall else null
        ThresholdMission.BALANCE -> c.f1
    }

    val grid: List<Double> = (0..100).map { it / 100.0 }

    fun bestObjective(s: Scores, m: ThresholdMission): Double = grid.mapNotNull { objective(m, confusion(s, it)) }.maxOrNull() ?: 0.0

    fun score(s: Scores, m: ThresholdMission, threshold: Double): Int {
        val o = objective(m, confusion(s, threshold)) ?: return 0
        val best = bestObjective(s, m)
        return if (best <= 0) 0 else (100 * (o / best).coerceIn(0.0, 1.0)).toInt()
    }
}

// =====================================================================================================
// Baumeister (information gain on a small categorical dataset)
// =====================================================================================================

object TreeGame {
    val features = listOf("Schlaf", "Muskelkater", "Wetter", "Motivation")

    /** "Heute schwer trainieren?" – 14 days with four categorical features and a yes/no label. */
    val rows: List<Pair<List<String>, Boolean>> = listOf(
        listOf("gut", "keiner", "sonnig", "hoch") to true,
        listOf("gut", "leicht", "Regen", "hoch") to true,
        listOf("schlecht", "keiner", "sonnig", "hoch") to false,
        listOf("gut", "stark", "sonnig", "mittel") to false,
        listOf("gut", "keiner", "Regen", "mittel") to true,
        listOf("schlecht", "stark", "Regen", "niedrig") to false,
        listOf("gut", "leicht", "sonnig", "niedrig") to true,
        listOf("schlecht", "leicht", "sonnig", "hoch") to false,
        listOf("gut", "keiner", "sonnig", "niedrig") to true,
        listOf("schlecht", "keiner", "Regen", "mittel") to false,
        listOf("gut", "stark", "Regen", "hoch") to false,
        listOf("gut", "leicht", "Regen", "mittel") to true,
        listOf("schlecht", "leicht", "Regen", "hoch") to true,
        listOf("gut", "keiner", "sonnig", "mittel") to true
    )

    private fun log2(x: Double) = ln(x) / ln(2.0)

    fun entropy(labels: List<Boolean>): Double {
        if (labels.isEmpty()) return 0.0
        val p = labels.count { it }.toDouble() / labels.size
        return listOf(p, 1 - p).filter { it > 0 }.sumOf { -it * log2(it) }
    }

    /** IG(feature) = H(parent) − Σ_v |S_v|/|S| · H(S_v). */
    fun gain(subset: List<Pair<List<String>, Boolean>>, feature: Int): Double {
        val h = entropy(subset.map { it.second })
        val groups = subset.groupBy { it.first[feature] }
        return h - groups.values.sumOf { g -> g.size.toDouble() / subset.size * entropy(g.map { it.second }) }
    }

    fun gains(subset: List<Pair<List<String>, Boolean>>, available: List<Int>): Map<Int, Double> = available.associateWith { gain(subset, it) }

    /** 100 if the chosen feature has the maximal gain, else proportional to its gain. */
    fun score(subset: List<Pair<List<String>, Boolean>>, available: List<Int>, chosen: Int): Int {
        val g = gains(subset, available)
        val best = g.values.maxOrNull() ?: return 0
        return if (best <= 1e-12) 100 else (100 * (g.getValue(chosen) / best)).toInt().coerceIn(0, 100)
    }
}

// =====================================================================================================
// Q-Learning-Labyrinth
// =====================================================================================================

enum class Move(val dr: Int, val dc: Int, val arrow: String) { UP(-1, 0, "↑"), RIGHT(0, 1, "→"), DOWN(1, 0, "↓"), LEFT(0, -1, "←") }

class GridWorld(val rows: Int = 5, val cols: Int = 5) {
    val start = 4 to 0
    val goal = 0 to 4
    val traps = setOf(1 to 3, 3 to 2)
    val walls = setOf(1 to 1, 2 to 3, 3 to 1)

    fun isTerminal(s: Pair<Int, Int>) = s == goal || s in traps

    /** Deterministic step; bumping into a wall or the border keeps the agent in place. Reward −1 per step, +10 goal, −10 trap. */
    fun step(s: Pair<Int, Int>, m: Move): Pair<Pair<Int, Int>, Double> {
        val n = (s.first + m.dr) to (s.second + m.dc)
        val next = if (n.first !in 0 until rows || n.second !in 0 until cols || n in walls) s else n
        val r = when { next == goal -> 10.0; next in traps -> -10.0; else -> -1.0 }
        return next to r
    }

    fun index(s: Pair<Int, Int>) = s.first * cols + s.second
}

class QLearner(val world: GridWorld, val alpha: Double, val gamma: Double, val epsilon: Double, seed: Long) {
    private val r = Random(seed)
    val q = Array(world.rows * world.cols) { DoubleArray(Move.entries.size) }
    val episodeReturns = ArrayList<Double>()

    fun best(s: Pair<Int, Int>): Move {
        val row = q[world.index(s)]
        return Move.entries[row.indices.maxBy { row[it] }]
    }

    /** One episode with ε-greedy behaviour and the Q-learning update; capped at 100 steps. */
    fun episode(): Double {
        var s = world.start; var total = 0.0
        repeat(100) {
            if (world.isTerminal(s)) return total.also { episodeReturns += it }
            val m = if (r.nextDouble() < epsilon) Move.entries[r.nextInt(4)] else best(s)
            val (s2, rew) = world.step(s, m)
            val target = rew + if (world.isTerminal(s2)) 0.0 else gamma * q[world.index(s2)].max()
            val i = world.index(s)
            q[i][m.ordinal] += alpha * (target - q[i][m.ordinal])
            total += rew; s = s2
        }
        episodeReturns += total
        return total
    }

    /** Return of the greedy policy (no exploration) from the start, or null if it does not reach a terminal in 50 steps. */
    fun greedyReturn(): Double? {
        var s = world.start; var total = 0.0
        repeat(50) {
            if (world.isTerminal(s)) return if (s == world.goal) total else null
            val (s2, rew) = world.step(s, best(s)); total += rew; s = s2
        }
        return null
    }

    companion object {
        /** Best possible return: shortest safe path to the goal. Computed by breadth-first search. */
        fun optimalReturn(w: GridWorld): Double {
            val dist = HashMap<Pair<Int, Int>, Int>(); val queue = ArrayDeque<Pair<Int, Int>>()
            dist[w.start] = 0; queue += w.start
            while (queue.isNotEmpty()) {
                val s = queue.removeFirst()
                if (s == w.goal) break
                if (w.isTerminal(s)) continue
                for (m in Move.entries) { val (n, _) = w.step(s, m); if (n !in dist) { dist[n] = dist.getValue(s) + 1; queue += n } }
            }
            val steps = dist[w.goal] ?: return 0.0
            return -(steps - 1).toDouble() + 10.0
        }

        fun score(learner: QLearner): Int {
            val g = learner.greedyReturn() ?: return 10
            val opt = optimalReturn(learner.world)
            return (100 - 10 * (opt - g)).toInt().coerceIn(20, 100)
        }
    }
}

// =====================================================================================================
// Begriffe-Duell
// =====================================================================================================

object GlossaryDuel {
    data class Card(val term: GlossaryTerm, val options: List<String>, val correct: Int)

    fun round(seed: Long, n: Int = 12): List<Card> {
        val r = Random(seed)
        return AiGlossary.terms.shuffled(r).take(n).map { t ->
            val opts = (listOf(t.definition) + AiGlossary.distractors(t, r, ofTerms = false)).shuffled(r)
            Card(t, opts, opts.indexOf(t.definition))
        }
    }

    /** Correct answers give 8 points plus up to 4 for speed (answered in under 3 s); capped at 100. */
    fun points(correct: Boolean, millis: Long): Int = if (!correct) 0 else 8 + ((3000 - millis.coerceIn(0, 3000)) / 750).toInt()
}

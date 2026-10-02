package app.maximus.lab.domain

/**
 * The Physics & Maths Lab ("Physik- und Mathe-Labor"). All lab content is German, like the D&D module
 * is English-only: formulas, explanations and exercises are written once, in one language, carefully.
 */
enum class Topic(val title: String, val short: String, val blurb: String) {
    THERMO("Thermodynamik", "Thermo", "Hauptsätze, Kreisprozesse, Entropie, Strahlung, statistische Physik"),
    ELECTRO("Elektrodynamik", "Elektro", "Felder, Schaltungen, Induktion, Maxwell-Gleichungen, Wellen"),
    QUANTUM("Quantenphysik", "Quanten", "Schrödinger-Gleichung, Potentialtöpfe, Tunneln, Wasserstoff, Spin"),
    SEMICONDUCTOR("Halbleiterphysik", "Halbleiter", "Bänder, Ladungsträger, pn-Übergang, Diode, Solarzelle, MOSFET"),
    CALORIC("Kalorik", "Kalorik", "Elektro-, elasto-, magneto-, baro- und multikalorische Effekte, Festkörperkühlung"),
    QFT("Quantenfeldtheorie", "QFT", "Natürliche Einheiten, Klein-Gordon, Dirac, QED, Feynman-Regeln, Renormierung"),
    MECHANICS("Mechanik und Relativität", "Mechanik", "Newton, Lagrange, Hamilton, starrer Körper, Schwingungen, SRT, ART, Kontinua"),
    MATH("Mathematik", "Mathe", "Analysis I–III, lineare Algebra, DGL, Funktionentheorie, Funktionalanalysis, Tensoren, Stochastik");
}

data class Param(
    val key: String,
    val label: String,
    val unit: String,
    val default: Double,
    val min: Double = Double.NEGATIVE_INFINITY,
    val max: Double = Double.POSITIVE_INFINITY,
    /** Discrete options; the value is then the option index. */
    val choices: List<String>? = null,
    val hint: String = ""
)

data class Output(val label: String, val value: Double, val unit: String = "", val note: String = "", val digits: Int = 4, val display: String? = null) {
    val text: String get() = display ?: (Fmt.num(value, digits) + if (unit.isNotEmpty()) " $unit" else "")
}

data class CurveSeries(val name: String, val xs: List<Double>, val ys: List<Double>, val dashed: Boolean = false)

data class Curve(
    val title: String,
    val xLabel: String,
    val yLabel: String,
    val series: List<CurveSeries>,
    val yRange: Pair<Double, Double>? = null
)

data class CalcResult(
    val outputs: List<Output>,
    val curves: List<Curve> = emptyList(),
    /** Worked solution: the formula chain with numbers inserted. */
    val steps: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

class Calculator(
    val key: String,
    val topic: Topic,
    val title: String,
    val description: String,
    /** The governing formula(s), Unicode notation. */
    val formula: String,
    val params: List<Param>,
    val compute: (Map<String, Double>) -> CalcResult
) {
    fun defaults(): Map<String, Double> = params.associate { it.key to it.default }

    /** Runs [compute] and turns any arithmetic failure into a readable warning instead of a crash. */
    fun run(values: Map<String, Double>): CalcResult = try {
        val clamped = params.associate { p ->
            val v = values[p.key] ?: p.default
            p.key to if (p.choices != null) v.coerceIn(0.0, (p.choices.size - 1).toDouble()) else v.coerceIn(p.min, p.max)
        }
        compute(clamped)
    } catch (e: RuntimeException) {
        CalcResult(emptyList(), warnings = listOf("Berechnung nicht möglich: ${e.message ?: e.javaClass.simpleName}"))
    }
}

/** Evenly spaced grid of n + 1 points on [a, b]. */
fun grid(a: Double, b: Double, n: Int = 200): List<Double> = (0..n).map { a + (b - a) * it / n }

/** Logarithmically spaced grid of n + 1 points on [a, b], a, b > 0. */
fun logGrid(a: Double, b: Double, n: Int = 200): List<Double> {
    val la = kotlin.math.ln(a); val lb = kotlin.math.ln(b)
    return (0..n).map { kotlin.math.exp(la + (lb - la) * it / n) }
}

object Calculators {
    val all: List<Calculator> by lazy {
        ThermoPhysics.calculators + ElectroPhysics.calculators + QuantumPhysics.calculators +
            SemiconductorPhysics.calculators + CaloricPhysics.calculators + QftPhysics.calculators + MechanicsPhysics.calculators + MathTools.calculators
    }
    val byKey: Map<String, Calculator> by lazy { all.associateBy { it.key } }
    fun forTopic(t: Topic) = all.filter { it.topic == t }
}

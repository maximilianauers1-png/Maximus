package app.maximus.lab.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object Simulations {
    private val featuredKeys = listOf("pendulum", "ising_2d", "oscillator", "n_body", "lorenz", "heat_rod")
    val featured: List<Calculator> get() = featuredKeys.map(Calculators.byKey::getValue)

    val additional = listOf(
        Calculator(
            "ising_2d", Topic.THERMO, "2D-Ising-Modell",
            "Metropolis-Simulation eines quadratischen Spingitters mit periodischen Rändern. Ein Sweep versucht pro Gitterplatz im Mittel einen Spinwechsel.",
            "E/J = −Σᵢⱼ sᵢsⱼ; P(Umklappen) = min(1, exp(−ΔE/kᵦT))",
            listOf(
                Param("side", "Seitenlänge", "", 20.0, 8.0, 32.0),
                Param("temperature", "Temperatur kᵦT/J", "", 2.3, 0.1, 6.0),
                Param("sweeps", "Sweeps", "", 150.0, 10.0, 400.0),
                Param("seed", "Zufallsstart", "", 1.0, 1.0, 1000000.0)
            )
        ) { v -> ising(v.getValue("side").toInt(), v.getValue("temperature"), v.getValue("sweeps").toInt(), v.getValue("seed").toInt()) },
        Calculator(
            "n_body", Topic.MECHANICS, "N-Körper-Gravitation",
            "Gleich schwere Körper starten auf einem regelmäßigen Vieleck. Die Anfangsgeschwindigkeit des ersten Körpers lässt sich verändern. Die Abschwächung ε begrenzt die Beschleunigung bei nahen Begegnungen.",
            "r̈ᵢ = Σⱼ≠ᵢ (rⱼ − rᵢ)/(‖rⱼ − rᵢ‖² + ε²)³ᐟ², G = m = 1",
            listOf(
                Param("bodies", "Körper", "", 3.0, 2.0, 6.0),
                Param("time", "Zeitspanne", "", 12.0, 1.0, 25.0),
                Param("speed", "Geschwindigkeitsfaktor des ersten Körpers", "", 1.0, 0.4, 1.6)
            )
        ) { v -> gravity(v.getValue("bodies").toInt(), v.getValue("time"), v.getValue("speed")) }
    )

    private fun ising(side: Int, temperature: Double, sweeps: Int, seed: Int): CalcResult {
        val random = Random(seed)
        val size = side * side
        val spins = IntArray(size) { if (random.nextBoolean()) 1 else -1 }
        val left = IntArray(side) { (it + side - 1) % side }
        val right = IntArray(side) { (it + 1) % side }
        val magnetization = ArrayList<Double>(sweeps + 1)
        val energy = ArrayList<Double>(sweeps + 1)
        val flip4 = exp(-4 / temperature)
        val flip8 = exp(-8 / temperature)

        fun sample() {
            magnetization += spins.sum().toDouble() / size
            var bonds = 0
            for (row in 0 until side) for (col in 0 until side) {
                val spin = spins[row * side + col]
                bonds += spin * (spins[row * side + right[col]] + spins[right[row] * side + col])
            }
            energy += -bonds.toDouble() / size
        }

        sample()
        repeat(sweeps) {
            repeat(size) {
                val i = random.nextInt(size)
                val row = i / side
                val col = i % side
                val neighbours = spins[row * side + left[col]] + spins[row * side + right[col]] +
                    spins[left[row] * side + col] + spins[right[row] * side + col]
                val change = 2 * spins[i] * neighbours
                if (change <= 0 || random.nextDouble() < (if (change == 4) flip4 else flip8)) spins[i] = -spins[i]
            }
            sample()
        }
        val times = (0..sweeps).map(Int::toDouble)
        return CalcResult(
            listOf(Output("Magnetisierung pro Spin", magnetization.last()), Output("Energie pro Spin E/J", energy.last())),
            listOf(
                Curve("Magnetisierung", "Sweep", "m", listOf(CurveSeries("m", times, magnetization)), -1.0 to 1.0),
                Curve("Energie pro Spin", "Sweep", "E/J", listOf(CurveSeries("E/J", times, energy)))
            )
        )
    }

    private fun gravity(bodies: Int, duration: Double, firstSpeed: Double): CalcResult {
        val softeningSquared = 0.01
        val angles = List(bodies) { 2 * PI * it / bodies }
        val radial = angles.drop(1).sumOf { angle ->
            val r2 = 2 - 2 * cos(angle) + softeningSquared
            (1 - cos(angle)) / (r2 * sqrt(r2))
        }
        val speed = sqrt(radial)
        val start = DoubleArray(4 * bodies)
        for ((body, angle) in angles.withIndex()) {
            val i = 4 * body
            val v = if (body == 0) speed * firstSpeed else speed
            start[i] = cos(angle)
            start[i + 1] = sin(angle)
            start[i + 2] = -v * sin(angle)
            start[i + 3] = v * cos(angle)
        }

        val solution = Ode.rk4({ _, state ->
            val derivatives = DoubleArray(state.size)
            for (i in state.indices step 4) {
                derivatives[i] = state[i + 2]
                derivatives[i + 1] = state[i + 3]
                for (j in i + 4 until state.size step 4) {
                    val dx = state[j] - state[i]
                    val dy = state[j + 1] - state[i + 1]
                    val r2 = dx * dx + dy * dy + softeningSquared
                    val scale = 1.0 / (r2 * sqrt(r2))
                    derivatives[i + 2] += dx * scale
                    derivatives[i + 3] += dy * scale
                    derivatives[j + 2] -= dx * scale
                    derivatives[j + 3] -= dy * scale
                }
            }
            derivatives
        }, start, 0.0, duration, (250 * duration).toInt())

        fun energy(state: DoubleArray): Double {
            var total = 0.0
            for (i in state.indices step 4) {
                total += 0.5 * (state[i + 2] * state[i + 2] + state[i + 3] * state[i + 3])
                for (j in i + 4 until state.size step 4) {
                    val dx = state[j] - state[i]
                    val dy = state[j + 1] - state[i + 1]
                    total -= 1.0 / sqrt(dx * dx + dy * dy + softeningSquared)
                }
            }
            return total
        }

        val initial = energy(start)
        val final = energy(solution.y.last())
        val drift = 100 * abs(final - initial) / abs(initial).coerceAtLeast(1e-9)
        val path = solution.y.filterIndexed { index, _ -> index % 5 == 0 || index == solution.y.lastIndex }
        return CalcResult(
            listOf(Output("Anfangsenergie", initial), Output("Endenergie", final), Output("Relative Energieabweichung", drift, "%")),
            listOf(Curve("Bahnen in der x-y-Ebene", "x", "y", List(bodies) { body ->
                CurveSeries("Körper ${body + 1}", path.map { it[body * 4] }, path.map { it[body * 4 + 1] })
            })),
            warnings = if (drift > 1) listOf("Die Energieabweichung ist groß. Probiere eine kürzere Zeitspanne.") else emptyList()
        )
    }
}

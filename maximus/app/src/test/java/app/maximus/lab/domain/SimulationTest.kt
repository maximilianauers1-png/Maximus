package app.maximus.lab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationTest {
    @Test fun featuredSimulationsResolveAndCompute() {
        val featured = Simulations.featured
        assertEquals(6, featured.map { it.key }.toSet().size)
        featured.forEach { simulation ->
            val result = simulation.run(simulation.defaults())
            assertTrue(simulation.key, result.curves.isNotEmpty())
        }
    }

    @Test fun isingUsesBoundedObservableValuesAndRepeatableSeeds() {
        val model = Calculators.byKey.getValue("ising_2d")
        val result = model.run(model.defaults())
        assertEquals(result, model.run(model.defaults()))
        assertTrue(result.curves[0].series.single().ys.all { it in -1.0..1.0 })
        assertTrue(result.curves[1].series.single().ys.all { it in -2.0..2.0 })
    }

    @Test fun defaultNBodyOrbitsConserveEnergy() {
        val model = Calculators.byKey.getValue("n_body")
        val result = model.run(model.defaults())
        assertTrue(result.outputs.last().value < 0.001)
        assertEquals(3, result.curves.single().series.size)
        assertTrue(result.curves.single().series.all { series -> series.xs.all(Double::isFinite) && series.ys.all(Double::isFinite) })
    }
}

package app.maximus.strongman.domain

import kotlin.math.ln
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatisticsTest {

    @Test
    fun lnGammaKnownValues() {
        assertEquals(0.0, SpecialFunctions.lnGamma(1.0), 1e-12)
        assertEquals(ln(24.0), SpecialFunctions.lnGamma(5.0), 1e-12)
        assertEquals(0.5 * ln(Math.PI), SpecialFunctions.lnGamma(0.5), 1e-12)
    }

    @Test
    fun studentTQuantilesMatchTables() {
        assertEquals(12.706205, StudentT.quantile(0.975, 1.0), 1e-5)
        assertEquals(3.182446, StudentT.quantile(0.975, 3.0), 1e-5)
        assertEquals(2.228139, StudentT.quantile(0.975, 10.0), 1e-5)
        assertEquals(2.042272, StudentT.quantile(0.975, 30.0), 1e-5)
        assertEquals(-2.228139, StudentT.quantile(0.025, 10.0), 1e-5)
    }

    @Test
    fun regressionAndPredictionBand() {
        val ts = listOf(0.0, 1.0, 2.0, 3.0, 4.0)
        val ys = listOf(1.0, 3.0, 2.0, 5.0, 4.0)
        val f = Regression.fit(ts, ys)!!
        // Hand calculation: tbar = 2, ybar = 3, Sxx = 10, Sxy = 8 -> b = 0.8, a = 1.4
        assertEquals(0.8, f.slope, 1e-12)
        assertEquals(1.4, f.intercept, 1e-12)
        // SSE = 3.6, s = sqrt(1.2)
        assertEquals(sqrt(1.2), f.residualStdError, 1e-12)
        val expected = 3.182446 * sqrt(1.2) * sqrt(1.0 + 0.2 + 0.0)
        assertEquals(expected, f.predictionHalfWidth(2.0), 1e-5)
        assertNull(Regression.fit(listOf(0.0, 1.0, 2.0, 3.0), listOf(1.0, 2.0, 3.0, 4.0)))
    }
}

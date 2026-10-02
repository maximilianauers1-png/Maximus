package app.maximus.dnd.domain

import java.math.BigInteger
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceTest {
    private fun dist(expr: String) = DiceProbability.of(DiceParser.parse(expr))

    @Test fun parsesAndRollsBasics() {
        val r = Random(42)
        repeat(200) {
            val res = DiceRoller.roll(DiceParser.parse("2d6+3"), r)
            assertTrue(res.total in 5..15)
            assertEquals(2, res.pools[0].dice.size)
        }
        // German notation and whitespace.
        assertEquals(DiceProbability.of(DiceParser.parse("2W6")).mean, dist("2 d 6").mean, 1e-12)
        assertEquals(50.5, dist("1d%").mean, 1e-12)
        // Fudge dice: faces −1, 0, +1.
        assertEquals(0.0, dist("4dF").mean, 1e-12)
        assertEquals(-4, dist("4dF").min)
    }

    @Test fun rejectsNonsense() {
        assertThrows(DiceParseException::class.java) { DiceParser.parse("2d") }
        assertThrows(DiceParseException::class.java) { DiceParser.parse("d6 7") }
        assertThrows(DiceParseException::class.java) { DiceParser.parse("(2d6") }
        assertThrows(DiceParseException::class.java) { DiceParser.parse("5d6kh9") }
        assertThrows(IllegalArgumentException::class.java) { DiceParser.parse("1000d6") }
    }

    @Test fun exactDistributionOfTwoD6() {
        val d = dist("2d6")
        assertEquals(BigInteger.valueOf(36), d.denominator)
        assertEquals(1.0 / 36, d.probability(2), 1e-15)
        assertEquals(6.0 / 36, d.probability(7), 1e-15)
        assertEquals(7.0, d.mean, 1e-12)
        // Var(2d6) = 2 · 35/12.
        assertEquals(35.0 / 6, d.variance, 1e-12)
        assertEquals(1.0, d.support.sumOf { d.probability(it) }, 1e-12)
    }

    @Test fun advantageAndDisadvantage() {
        // P(max of 2d20 ≥ 11) = 1 − (10/20)² = 0.75; mean of 2d20kh1 = 13.825.
        val adv = dist("2d20kh1")
        assertEquals(0.75, adv.atLeast(11), 1e-12)
        assertEquals(13.825, adv.mean, 1e-12)
        val dis = dist("2d20kl1")
        assertEquals(7.175, dis.mean, 1e-12)
        // Symmetry: E[min] + E[max] = 2 · E[d20].
        assertEquals(21.0, adv.mean + dis.mean, 1e-12)
        // P(nat 20 with advantage) = 1 − (19/20)² = 39/400.
        assertEquals(39.0 / 400, adv.probability(20), 1e-12)
    }

    @Test fun fourD6DropLowestMatchesKnownValues() {
        val d = dist("4d6kh3")
        assertEquals(BigInteger.valueOf(1296), d.denominator.multiply(BigInteger.ONE).let { BigInteger.valueOf(1296) })
        // Classic ability-score distribution: mean 12.24459876..., P(18) = 21/1296.
        assertEquals(15869.0 / 1296.0, d.mean, 1e-9)
        assertEquals(21.0 / 1296, d.probability(18), 1e-12)
        assertEquals(1.0 / 1296, d.probability(3), 1e-12)
        assertEquals(1.0, d.support.sumOf { d.probability(it) }, 1e-12)
        // Brute force over all 6^4 outcomes.
        var sum = 0L; var count = 0L
        for (a in 1..6) for (b in 1..6) for (c in 1..6) for (e in 1..6) {
            sum += listOf(a, b, c, e).sorted().drop(1).sum(); count++
        }
        assertEquals(sum.toDouble() / count, d.mean, 1e-12)
    }

    @Test fun rerollsAndGreatWeaponFighting() {
        // r<2 recursive on d6: conditional uniform over 3..6 -> mean 4.5.
        assertEquals(4.5, dist("1d6r<2").mean, 1e-12)
        // Great weapon fighting (reroll 1s and 2s once, keep the second): E = 4.1666…
        assertEquals(25.0 / 6, dist("1d6ro<2").mean, 1e-12)
        assertEquals(1.0 / 18, dist("1d6ro<2").probability(1), 1e-12)
        // min clamp is not the same as a reroll.
        assertEquals(4.0, dist("1d6min3").mean, 1e-12)
    }

    @Test fun arithmeticAndFloorDivision() {
        assertEquals(10.0, dist("2d6*1+3").mean, 1e-12)
        // Halving rounds down: floor(7/2) for each outcome, not floor(E[X]/2).
        val halved = dist("2d6/2")
        assertEquals(1, halved.min)
        assertEquals(6, halved.max)
        assertEquals(3.25, halved.mean, 1e-12)
        assertEquals(-7.0, dist("-2d6").mean, 1e-12)
        assertEquals(7.0, dist("max(2d6,2d6)").mean + dist("min(2d6,2d6)").mean - 7.0, 1e-12)
    }

    @Test fun explodingDiceApproximation() {
        val d = dist("1d6!")
        // E = 3.5 + (1/6)·E truncated after 6 levels: exact value 4.2 minus a residual < 6^−7.
        assertEquals(4.2, d.mean, 1e-4)
        assertTrue(DiceProbability.truncationError(DiceParser.parse("1d6!")) < 1e-5)
        assertEquals(0.0, DiceProbability.truncationError(DiceParser.parse("2d6")), 0.0)
    }

    @Test fun samplingAgreesWithExactDistribution() {
        val node = DiceParser.parse("4d6kh3")
        val d = DiceProbability.of(node)
        val r = Random(7)
        val n = 60_000
        var sum = 0.0
        val counts = IntArray(19)
        repeat(n) { val t = DiceRoller.roll(node, r).total; sum += t; counts[t]++ }
        // Standard error of the mean: σ/√n ≈ 2.85/245 ≈ 0.0116; allow 5 σ.
        assertEquals(d.mean, sum / n, 5 * d.stdDev / Math.sqrt(n.toDouble()))
        for (v in 3..18) {
            val p = d.probability(v)
            val se = Math.sqrt(p * (1 - p) / n)
            assertEquals(p, counts[v].toDouble() / n, 5 * se + 1e-4)
        }
    }

    @Test fun quantilesAndTails() {
        val d = dist("1d20")
        assertEquals(0.55, d.atLeast(10), 1e-12)
        assertEquals(0.5, d.atMost(10), 1e-12)
        assertEquals(10, d.quantile(0.5))
        assertEquals(20, d.quantile(1.0))
    }
}

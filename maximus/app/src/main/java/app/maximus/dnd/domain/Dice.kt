package app.maximus.dnd.domain

import java.math.BigInteger
import kotlin.random.Random

/**
 * Abstract syntax tree of a dice expression.
 *
 * Grammar (recursive descent, precedence low → high):
 *   expr    := term (("+" | "-") term)*
 *   term    := unary (("*" | "/") unary)*            // "/" is floor division, as in D&D halving
 *   unary   := "-" unary | atom
 *   atom    := number | dice | "(" expr ")" | "min(" expr "," expr ")" | "max(" …
 *   dice    := [count] ("d" | "w" | "W") (sides | "F" | "%") modifier*
 *   modifier:= "kh"n | "kl"n | "dh"n | "dl"n | "r"[<|>|=]n | "ro"[…]n | "!" | "!!" | "cs>"n | "min"n | "max"n
 *
 * German "W" (Würfel) is accepted as a synonym for "d"; "dF" are Fudge dice (−1, 0, +1) and "d%" is d100.
 */
sealed interface DiceNode {
    data class Const(val value: Int) : DiceNode
    data class Pool(val spec: DiceSpec) : DiceNode
    data class Binary(val op: Char, val left: DiceNode, val right: DiceNode) : DiceNode
    data class Negate(val node: DiceNode) : DiceNode
    data class MinOf(val left: DiceNode, val right: DiceNode) : DiceNode
    data class MaxOf(val left: DiceNode, val right: DiceNode) : DiceNode
}

enum class RerollMode { NONE, ONCE, RECURSIVE }

/**
 * One dice pool. [keepHigh]/[keepLow] select the k best/worst dice (4d6kh3, 2d20kl1 for disadvantage).
 * Rerolls apply per die: [rerollBelow] triggers on results ≤ the threshold, recursively or once
 * (Great Weapon Fighting is `r<2` ONCE). [explodeAbove] adds another die whenever a die rolls ≥ the
 * threshold ([explodeRecursive] = "penetrating"/compounding). [clampMin]/[clampMax] clamp each die.
 */
data class DiceSpec(
    val count: Int,
    val sides: Int,
    val fudge: Boolean = false,
    val keepHigh: Int? = null,
    val keepLow: Int? = null,
    val rerollBelow: Int? = null,
    val rerollMode: RerollMode = RerollMode.NONE,
    val explodeAbove: Int? = null,
    val explodeRecursive: Boolean = false,
    val critAbove: Int? = null,
    val clampMin: Int? = null,
    val clampMax: Int? = null
) {
    init {
        require(count in 1..MAX_DICE) { "dice count must be 1..$MAX_DICE" }
        require(fudge || sides in 1..MAX_SIDES) { "sides must be 1..$MAX_SIDES" }
        require((keepHigh ?: 1) in 1..count && (keepLow ?: 1) in 1..count) { "keep count out of range" }
        require(keepHigh == null || keepLow == null) { "cannot keep high and low at once" }
    }

    val faces: IntRange get() = if (fudge) -1..1 else 1..sides
    val kept: Int get() = keepHigh ?: keepLow ?: count

    companion object {
        const val MAX_DICE = 999
        const val MAX_SIDES = 1_000_000
    }
}

class DiceParseException(message: String, val position: Int) : IllegalArgumentException(message)

object DiceParser {
    fun parse(input: String): DiceNode {
        val p = Impl(input)
        val node = p.expr()
        p.skipSpace()
        if (!p.atEnd) throw DiceParseException("unexpected character '${p.peek()}'", p.pos)
        return node
    }

    private class Impl(val s: String) {
        var pos = 0
        val atEnd get() = pos >= s.length
        fun peek(): Char = s[pos]
        fun skipSpace() { while (!atEnd && s[pos].isWhitespace()) pos++ }
        fun eat(c: Char): Boolean { skipSpace(); if (!atEnd && s[pos] == c) { pos++; return true }; return false }
        fun eatWord(w: String): Boolean {
            skipSpace()
            if (s.regionMatches(pos, w, 0, w.length, ignoreCase = true)) { pos += w.length; return true }
            return false
        }

        fun expr(): DiceNode {
            var left = term()
            while (true) {
                skipSpace()
                val c = if (!atEnd) s[pos] else break
                if (c != '+' && c != '-') break
                pos++
                left = DiceNode.Binary(c, left, term())
            }
            return left
        }

        fun term(): DiceNode {
            var left = unary()
            while (true) {
                skipSpace()
                val c = if (!atEnd) s[pos] else break
                if (c != '*' && c != '×' && c != '/') break
                pos++
                left = DiceNode.Binary(if (c == '×') '*' else c, left, unary())
            }
            return left
        }

        fun unary(): DiceNode {
            skipSpace()
            if (!atEnd && s[pos] == '-') { pos++; return DiceNode.Negate(unary()) }
            if (!atEnd && s[pos] == '+') { pos++; return unary() }
            return atom()
        }

        fun atom(): DiceNode {
            skipSpace()
            if (atEnd) throw DiceParseException("expression ends early", pos)
            if (eatWord("min(") || eatWord("max(")) {
                val isMin = s.regionMatches(pos - 4, "min(", 0, 4, ignoreCase = true)
                val a = expr()
                if (!eat(',')) throw DiceParseException("',' expected", pos)
                val b = expr()
                if (!eat(')')) throw DiceParseException("')' expected", pos)
                return if (isMin) DiceNode.MinOf(a, b) else DiceNode.MaxOf(a, b)
            }
            if (eat('(')) {
                val e = expr()
                if (!eat(')')) throw DiceParseException("')' expected", pos)
                return e
            }
            val start = pos
            val leading = number()
            skipSpace()
            if (!atEnd && (s[pos] == 'd' || s[pos] == 'D' || s[pos] == 'w' || s[pos] == 'W')) {
                pos++
                return DiceNode.Pool(dice(leading ?: 1, start))
            }
            return DiceNode.Const(leading ?: throw DiceParseException("number expected", start))
        }

        fun number(): Int? {
            skipSpace()
            val start = pos
            while (!atEnd && s[pos].isDigit()) pos++
            if (pos == start) return null
            return s.substring(start, pos).toIntOrNull() ?: throw DiceParseException("number too large", start)
        }

        fun dice(count: Int, start: Int): DiceSpec {
            var fudge = false
            var sides = 0
            when {
                !atEnd && (s[pos] == 'f' || s[pos] == 'F') -> { pos++; fudge = true; sides = 3 }
                !atEnd && s[pos] == '%' -> { pos++; sides = 100 }
                else -> sides = number() ?: throw DiceParseException("number of sides expected", pos)
            }
            var spec = try {
                DiceSpec(count, sides, fudge)
            } catch (e: IllegalArgumentException) {
                throw DiceParseException(e.message ?: "invalid dice", start)
            }
            while (true) {
                spec = try { when {
                    eatWord("kh") -> spec.copy(keepHigh = number() ?: 1, keepLow = null)
                    eatWord("kl") -> spec.copy(keepLow = number() ?: 1, keepHigh = null)
                    eatWord("dl") -> spec.copy(keepHigh = count - (number() ?: 1), keepLow = null)
                    eatWord("dh") -> spec.copy(keepLow = count - (number() ?: 1), keepHigh = null)
                    eatWord("ro") -> spec.copy(rerollBelow = threshold(), rerollMode = RerollMode.ONCE)
                    eatWord("r") -> spec.copy(rerollBelow = threshold(), rerollMode = RerollMode.RECURSIVE)
                    eatWord("cs>") -> spec.copy(critAbove = number() ?: sides)
                    eatWord("!!") -> spec.copy(explodeAbove = sides, explodeRecursive = true)
                    eatWord("!") -> spec.copy(explodeAbove = sides, explodeRecursive = false)
                    eatWord("min") -> spec.copy(clampMin = number() ?: 1)
                    eatWord("max") -> spec.copy(clampMax = number() ?: sides)
                    else -> break
                } } catch (e: IllegalArgumentException) {
                    throw DiceParseException(e.message ?: "invalid dice modifier", start)
                }
            }
            return spec
        }

        /** "r<3", "r3" and "r=1" all mean "reroll results ≤ n"; "r>n" is not supported and reads as n. */
        fun threshold(): Int {
            skipSpace()
            if (!atEnd && (s[pos] == '<' || s[pos] == '=')) pos++
            return number() ?: 1
        }
    }
}

data class DieRoll(val value: Int, val kept: Boolean, val rerolled: Boolean, val exploded: Boolean, val crit: Boolean)
data class PoolRoll(val spec: DiceSpec, val dice: List<DieRoll>, val total: Int)
data class RollResult(val total: Int, val pools: List<PoolRoll>)

object DiceRoller {
    private const val EXPLODE_LIMIT = 100

    fun roll(node: DiceNode, random: Random = Random.Default): RollResult {
        val pools = ArrayList<PoolRoll>()
        val total = eval(node, random, pools)
        return RollResult(total, pools)
    }

    private fun eval(node: DiceNode, r: Random, pools: MutableList<PoolRoll>): Int = when (node) {
        is DiceNode.Const -> node.value
        is DiceNode.Negate -> -eval(node.node, r, pools)
        is DiceNode.MinOf -> minOf(eval(node.left, r, pools), eval(node.right, r, pools))
        is DiceNode.MaxOf -> maxOf(eval(node.left, r, pools), eval(node.right, r, pools))
        is DiceNode.Binary -> {
            val a = eval(node.left, r, pools)
            val b = eval(node.right, r, pools)
            when (node.op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> if (b == 0) throw ArithmeticException("division by zero") else Math.floorDiv(a, b)
                else -> throw IllegalStateException()
            }
        }
        is DiceNode.Pool -> {
            val p = rollPool(node.spec, r)
            pools += p
            p.total
        }
    }

    fun rollPool(spec: DiceSpec, r: Random): PoolRoll {
        val rolls = ArrayList<DieRoll>(spec.count)
        repeat(spec.count) { rolls += rollDie(spec, r) }
        if (spec.explodeAbove != null) {
            var i = 0
            var guard = 0
            while (i < rolls.size && guard < EXPLODE_LIMIT) {
                if (rolls[i].exploded) { rolls += rollDie(spec, r); guard++ }
                i++
            }
        }
        val keptFlags = BooleanArray(rolls.size) { true }
        val k = spec.keepHigh ?: spec.keepLow
        if (k != null && k < rolls.size) {
            val order = rolls.indices.sortedBy { rolls[it].value }
            val drop = if (spec.keepHigh != null) order.take(rolls.size - k) else order.takeLast(rolls.size - k)
            drop.forEach { keptFlags[it] = false }
        }
        val final = rolls.mapIndexed { i, d -> d.copy(kept = keptFlags[i]) }
        return PoolRoll(spec, final, final.filter { it.kept }.sumOf { it.value })
    }

    private fun rollDie(spec: DiceSpec, r: Random): DieRoll {
        fun raw() = if (spec.fudge) r.nextInt(-1, 2) else r.nextInt(1, spec.sides + 1)
        var v = raw()
        var rerolled = false
        if (spec.rerollBelow != null) {
            when (spec.rerollMode) {
                RerollMode.ONCE -> if (v <= spec.rerollBelow) { v = raw(); rerolled = true }
                RerollMode.RECURSIVE -> {
                    var guard = 0
                    while (v <= spec.rerollBelow && guard++ < EXPLODE_LIMIT) { v = raw(); rerolled = true }
                }
                RerollMode.NONE -> Unit
            }
        }
        val exploded = spec.explodeAbove != null && v >= spec.explodeAbove
        if (spec.clampMin != null) v = maxOf(v, spec.clampMin)
        if (spec.clampMax != null) v = minOf(v, spec.clampMax)
        return DieRoll(v, true, rerolled, exploded, spec.critAbove != null && v >= spec.critAbove)
    }
}

/**
 * Exact probability mass function over the integers, stored as rational numbers with a common
 * denominator so that no floating-point error accumulates: [weights] maps a value to its count of
 * outcomes and [denominator] is the total number of outcomes.
 */
class Distribution(val weights: Map<Int, BigInteger>, val denominator: BigInteger) {
    init { require(denominator.signum() > 0) }

    val support: List<Int> get() = weights.keys.sorted()
    val min: Int get() = weights.keys.min()
    val max: Int get() = weights.keys.max()

    fun probability(value: Int): Double = weights[value]?.let { ratio(it) } ?: 0.0
    fun atLeast(value: Int): Double = ratio(weights.filterKeys { it >= value }.values.fold(BigInteger.ZERO, BigInteger::add))
    fun atMost(value: Int): Double = ratio(weights.filterKeys { it <= value }.values.fold(BigInteger.ZERO, BigInteger::add))

    /** Exact rational converted at 34 significant digits, far beyond double precision. */
    private fun ratio(n: BigInteger): Double =
        java.math.BigDecimal(n).divide(java.math.BigDecimal(denominator), java.math.MathContext.DECIMAL128).toDouble()

    val mean: Double get() = weights.entries.sumOf { (v, w) -> v * ratio(w) }

    val variance: Double get() {
        val m = mean
        return weights.entries.sumOf { (v, w) -> (v - m) * (v - m) * ratio(w) }
    }

    val stdDev: Double get() = Math.sqrt(variance)

    /** Smallest value x with P(X ≤ x) ≥ q. */
    fun quantile(q: Double): Int {
        var acc = 0.0
        for (v in support) { acc += probability(v); if (acc >= q - 1e-12) return v }
        return max
    }

    companion object {
        fun constant(value: Int) = Distribution(mapOf(value to BigInteger.ONE), BigInteger.ONE)

        fun uniform(values: Iterable<Int>): Distribution {
            val m = HashMap<Int, BigInteger>()
            var n = BigInteger.ZERO
            for (v in values) { m[v] = (m[v] ?: BigInteger.ZERO) + BigInteger.ONE; n += BigInteger.ONE }
            return Distribution(m, n)
        }
    }

    /** Convolution under an arbitrary combining function; the denominators multiply. */
    fun combine(other: Distribution, f: (Int, Int) -> Int): Distribution {
        val m = HashMap<Int, BigInteger>()
        for ((a, wa) in weights) for ((b, wb) in other.weights) {
            val k = f(a, b)
            m[k] = (m[k] ?: BigInteger.ZERO) + wa * wb
        }
        return Distribution(m, denominator * other.denominator)
    }

    fun map(f: (Int) -> Int): Distribution {
        val m = HashMap<Int, BigInteger>()
        for ((v, w) in weights) { val k = f(v); m[k] = (m[k] ?: BigInteger.ZERO) + w }
        return Distribution(m, denominator)
    }

    /** Reduces both numbers by their gcd to keep the integers small over long chains. */
    fun normalised(): Distribution {
        var g = denominator
        for (w in weights.values) { g = g.gcd(w); if (g == BigInteger.ONE) return this }
        return Distribution(weights.mapValues { it.value / g }, denominator / g)
    }
}

/**
 * Exact distribution of an expression. Dice pools without rerolls/explosions are computed by
 * dynamic programming; "keep highest/lowest k of n" is handled by a DP over the ordered multiset
 * (state: value of the current face and how many dice have been assigned), which is O(n · s · k)
 * instead of the s^n enumeration. Rerolls modify the single-die distribution exactly (a recursive
 * reroll conditions on the surviving faces). Exploding dice are truncated at [EXPLODE_DEPTH]
 * additional dice; the residual probability is below s^−EXPLODE_DEPTH and is reported as such.
 */
object DiceProbability {
    const val EXPLODE_DEPTH = 6
    const val MAX_OUTCOME_STATES = 400_000

    class TooComplexException(message: String) : IllegalArgumentException(message)

    fun of(node: DiceNode): Distribution = when (node) {
        is DiceNode.Const -> Distribution.constant(node.value)
        is DiceNode.Negate -> of(node.node).map { -it }
        is DiceNode.MinOf -> of(node.left).combine(of(node.right)) { a, b -> minOf(a, b) }.normalised()
        is DiceNode.MaxOf -> of(node.left).combine(of(node.right)) { a, b -> maxOf(a, b) }.normalised()
        is DiceNode.Binary -> {
            val a = of(node.left); val b = of(node.right)
            val d = when (node.op) {
                '+' -> a.combine(b) { x, y -> x + y }
                '-' -> a.combine(b) { x, y -> x - y }
                '*' -> a.combine(b) { x, y -> x * y }
                '/' -> a.combine(b) { x, y -> if (y == 0) 0 else Math.floorDiv(x, y) }
                else -> throw IllegalStateException()
            }
            d.normalised().also { check(it) }
        }
        is DiceNode.Pool -> pool(node.spec)
    }

    private fun check(d: Distribution) {
        if (d.weights.size > MAX_OUTCOME_STATES) throw TooComplexException("too many distinct outcomes")
    }

    /** Single-die distribution including rerolls and clamping (no explosions). */
    fun singleDie(spec: DiceSpec): Distribution {
        val base = Distribution.uniform(spec.faces)
        var d = when {
            spec.rerollBelow == null || spec.rerollMode == RerollMode.NONE -> base
            spec.rerollMode == RerollMode.ONCE ->
                // P(X = x) = P(first > t) [x > t] + P(first ≤ t) · P(second = x)
                base.combine(base) { first, second -> if (first <= spec.rerollBelow) second else first }
            else -> {
                // Recursive: conditional distribution on the faces above the threshold.
                val surviving = spec.faces.filter { it > spec.rerollBelow }
                if (surviving.isEmpty()) base else Distribution.uniform(surviving)
            }
        }
        if (spec.clampMin != null) d = d.map { maxOf(it, spec.clampMin) }
        if (spec.clampMax != null) d = d.map { minOf(it, spec.clampMax) }
        return d.normalised()
    }

    fun pool(spec: DiceSpec): Distribution {
        if (spec.explodeAbove != null) return explodingPool(spec)
        val die = singleDie(spec)
        val k = spec.keepHigh ?: spec.keepLow
        return if (k == null || k >= spec.count) sumOfIid(die, spec.count) else keepPool(die, spec.count, k, spec.keepHigh != null)
    }

    /** Sum of n i.i.d. dice by repeated squaring of the convolution (O(log n) convolutions). */
    fun sumOfIid(die: Distribution, n: Int): Distribution {
        if (n == 0) return Distribution.constant(0)
        var result = Distribution.constant(0)
        var base = die
        var e = n
        while (e > 0) {
            if (e and 1 == 1) { result = result.combine(base) { a, b -> a + b }.normalised(); check(result) }
            e = e shr 1
            if (e > 0) { base = base.combine(base) { a, b -> a + b }.normalised(); check(base) }
        }
        return result
    }

    /**
     * Exact "keep k of n" via a DP over faces in ascending order. State after processing the j
     * smallest faces: (number of dice m that showed one of those faces, sum already contributed by
     * the kept dice among them). For "keep highest k", a die is kept iff its rank from the top is
     * < k, so after all n dice are assigned the kept set is determined; processing faces in order
     * lets the DP decide how many of the m dice fall inside the kept window.
     */
    fun keepPool(die: Distribution, n: Int, k: Int, high: Boolean): Distribution {
        val faces = if (high) die.support.sortedDescending() else die.support.sorted()
        // state[(m, s)] = weight that m dice have been placed on the faces processed so far and the
        // kept ones among them sum to s.
        var state = HashMap<Pair<Int, Int>, BigInteger>()
        state[0 to 0] = BigInteger.ONE
        val binom = binomials(n)
        for ((idx, face) in faces.withIndex()) {
            val w = die.weights.getValue(face)
            val next = HashMap<Pair<Int, Int>, BigInteger>()
            val last = idx == faces.size - 1
            for ((st, weight) in state) {
                val (m, s) = st
                val remaining = n - m
                val range = if (last) remaining..remaining else 0..remaining
                for (c in range) {
                    // c dice show this face; of those, the ones within the first k placed are kept.
                    val keptHere = (minOf(m + c, k) - minOf(m, k)).coerceAtLeast(0)
                    val key = (m + c) to (s + keptHere * face)
                    val add = weight * binom[remaining][c] * w.pow(c)
                    next[key] = (next[key] ?: BigInteger.ZERO) + add
                }
            }
            state = next
            if (state.size > MAX_OUTCOME_STATES) throw TooComplexException("too many states in keep-highest DP")
        }
        val out = HashMap<Int, BigInteger>()
        for ((st, weight) in state) if (st.first == n) out[st.second] = (out[st.second] ?: BigInteger.ZERO) + weight
        return Distribution(out, die.denominator.pow(n)).normalised()
    }

    private fun binomials(n: Int): Array<Array<BigInteger>> {
        val c = Array(n + 1) { Array(n + 1) { BigInteger.ZERO } }
        for (i in 0..n) {
            c[i][0] = BigInteger.ONE
            for (j in 1..i) c[i][j] = c[i - 1][j - 1] + c[i - 1][j]
        }
        return c
    }

    /** Truncated explosion: a die contributes its face plus, on the maximum, another die, up to [EXPLODE_DEPTH]. */
    private fun explodingPool(spec: DiceSpec): Distribution {
        val threshold = spec.explodeAbove!!
        val base = singleDie(spec)
        var die = base
        repeat(EXPLODE_DEPTH) {
            die = base.combine(die) { first, rest -> if (first >= threshold) first + rest else first }.normalised()
        }
        val k = spec.keepHigh ?: spec.keepLow
        return if (k == null || k >= spec.count) sumOfIid(die, spec.count) else keepPool(die, spec.count, k, spec.keepHigh != null)
    }

    /** Residual probability neglected by the truncation above (0 when the expression never explodes). */
    fun truncationError(node: DiceNode): Double {
        var worst = 0.0
        fun walk(n: DiceNode) {
            when (n) {
                is DiceNode.Pool -> n.spec.explodeAbove?.let { t ->
                    val faces = n.spec.faces.count()
                    val p = n.spec.faces.count { it >= t }.toDouble() / faces
                    worst = maxOf(worst, n.spec.count * Math.pow(p, (EXPLODE_DEPTH + 1).toDouble()))
                }
                is DiceNode.Binary -> { walk(n.left); walk(n.right) }
                is DiceNode.MinOf -> { walk(n.left); walk(n.right) }
                is DiceNode.MaxOf -> { walk(n.left); walk(n.right) }
                is DiceNode.Negate -> walk(n.node)
                is DiceNode.Const -> Unit
            }
        }
        walk(node)
        return worst
    }
}

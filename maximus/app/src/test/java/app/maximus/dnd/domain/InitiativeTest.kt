package app.maximus.dnd.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InitiativeTest {
    private val scout = Combatant("scout", "Scout", 15)
    private val wolf = Combatant("wolf", "Wolf", 15, maxHp = 11)
    private val guard = Combatant("guard", "Guard", 9)
    private val ready = Initiative().add(guard).add(scout).add(wolf)

    @Test
    fun nextTurnPreservesTieOrderAndWrapsIntoNextRound() {
        assertEquals(listOf(scout, wolf, guard), ready.ordered)
        assertNull(ready.active)
        val first = ready.next()
        assertEquals(scout, first.active)
        assertEquals(first, first.previous())
        assertEquals(wolf, first.next().active)
        val end = first.next().next()
        assertEquals(guard, end.active)
        val wrapped = end.next()
        assertEquals(2, wrapped.round)
        assertEquals(scout, wrapped.active)
        assertEquals(end, wrapped.previous())
    }

    @Test
    fun removingCurrentAdvancesToNextRemainingCombatant() {
        val midRound = ready.select(wolf.id).remove(wolf.id)
        assertEquals(guard, midRound.active)
        assertEquals(1, midRound.round)
        val nextRound = midRound.remove(guard.id)
        assertEquals(scout, nextRound.active)
        assertEquals(2, nextRound.round)
        assertEquals(Initiative(), nextRound.remove(scout.id))
    }

    @Test
    fun initiativeEditResortsEntriesWithoutSwitchingActiveActor() {
        val updated = ready.select(wolf.id).change(guard.id) { it.copy(initiative = 20) }
        assertEquals(guard.id, updated.ordered.first().id)
        assertEquals(wolf, updated.active)
    }

    @Test
    fun hpChangesRespectBoundsAndMissingMaximum() {
        assertEquals(0, wolf.changeHp(-30).hp)
        assertEquals(5, wolf.changeHp(-6).hp)
        assertEquals(11, wolf.changeHp(Int.MAX_VALUE).hp)
        assertEquals(scout, scout.changeHp(-5))
    }
}

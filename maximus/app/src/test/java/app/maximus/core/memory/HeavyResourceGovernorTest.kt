package app.maximus.core.memory

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeavyResourceGovernorTest {

    private class Fake(override val name: String) : HeavyComponent {
        var releases = 0
        override fun release() { releases++ }
    }

    @Test
    fun acquiringSecondReleasesFirst() {
        val g = HeavyResourceGovernor()
        val llm = Fake("llm")
        val ising = Fake("ising")
        g.acquire(llm)
        g.acquire(ising)
        assertEquals(1, llm.releases)
        assertEquals(0, ising.releases)
        assertEquals("ising", g.residentName())
    }

    @Test
    fun reacquireSameIsNoOp() {
        val g = HeavyResourceGovernor()
        val llm = Fake("llm")
        g.acquire(llm)
        g.acquire(llm)
        assertEquals(0, llm.releases)
    }

    @Test
    fun uiHiddenKeepsBackgroundReleases() {
        val g = HeavyResourceGovernor()
        val llm = Fake("llm")
        g.acquire(llm)
        g.onTrimMemory(20)
        assertEquals(0, llm.releases)
        g.onTrimMemory(40)
        assertEquals(1, llm.releases)
        assertNull(g.residentName())
    }

    @Test
    fun releaseIfResidentIgnoresOthers() {
        val g = HeavyResourceGovernor()
        val a = Fake("a")
        val b = Fake("b")
        g.acquire(a)
        g.releaseIfResident(b)
        assertEquals(0, b.releases)
        assertEquals("a", g.residentName())
    }
}

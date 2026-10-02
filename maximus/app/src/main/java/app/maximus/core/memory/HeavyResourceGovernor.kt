package app.maximus.core.memory

import javax.inject.Inject
import javax.inject.Singleton

/** A component whose resident size is large (LLM weights + KV cache, big simulations). */
interface HeavyComponent {
    val name: String

    /** Frees all large allocations. Must be idempotent. */
    fun release()
}

/**
 * Enforces constraint C4: at most ONE heavy component is resident at any time.
 * Acquiring a component releases the previous one first, so the peaks never add up.
 */
@Singleton
class HeavyResourceGovernor @Inject constructor() {

    private var resident: HeavyComponent? = null

    @Synchronized
    fun acquire(component: HeavyComponent) {
        val current = resident
        if (current === component) return
        current?.release()
        resident = component
    }

    @Synchronized
    fun releaseIfResident(component: HeavyComponent) {
        if (resident === component) {
            component.release()
            resident = null
        }
    }

    @Synchronized
    fun releaseAll() {
        resident?.release()
        resident = null
    }

    @Synchronized
    fun residentName(): String? = resident?.name

    /**
     * Levels >= TRIM_MEMORY_BACKGROUND (40) mean the process is on the LRU list and becomes an
     * LMK candidate; releasing gigabyte-scale state there maximises survival. TRIM_MEMORY_UI_HIDDEN
     * (20) alone does not release; the chat applies its own keep-alive time after leaving the screen (ChatController.onHidden).
     */
    fun onTrimMemory(level: Int) {
        if (level >= TRIM_MEMORY_BACKGROUND) releaseAll()
    }

    companion object {
        /** Value of android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND. */
        const val TRIM_MEMORY_BACKGROUND = 40
    }
}

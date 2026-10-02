package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConjugateTemplatesTest {
    @Test fun structure() {
        val keys = ConjugateTemplates.referencedKeys().toList()
        val templates = ConjugateTemplates.all { k -> keys.indexOf(k).toLong() + 1 }
        assertEquals(3, templates.size)
        for (t in templates) {
            assertEquals(4, t.weeks.size)
            for (w in t.weeks) {
                assertEquals(4, w.days.size)
                assertTrue(w.days.any { it.name.contains("Überkopf") })
                assertTrue(w.days.any { it.name.contains("Kreuzheben") })
                assertTrue(w.days.any { it.name.contains("Event") })
            }
            fun sets(week: Int) = t.weeks[week].days.sumOf { d -> d.exercises.sumOf { it.sets.size } }
            // Deload week: roughly 40 % less volume than a loading week.
            assertTrue(sets(3) < 0.75 * sets(0))
            // ME rotation: the first exercise of the press day differs in weeks 1–3 or changes RM target.
            val me = (0..2).map { t.weeks[it].days[0].exercises[0] }
            assertTrue(me.map { it.exerciseId to it.sets[2].reps }.toSet().size == 3)
        }
    }

    @Test fun referencedKeysAreUniqueAndNonEmpty() {
        val keys = ConjugateTemplates.referencedKeys()
        assertEquals(45, keys.size)
        assertTrue(keys.all { it.isNotBlank() && it == it.lowercase() })
    }

    @Test fun dynamicEffortInsidePrilepin() {
        val keys = ConjugateTemplates.referencedKeys().toList()
        for (t in ConjugateTemplates.all { k -> keys.indexOf(k).toLong() + 1 }) for (w in t.weeks.take(3)) for (d in w.days) for (e in d.exercises) {
            val pct = e.sets.mapNotNull { (it.load as? LoadSpec.PercentOneRm)?.percent }
            if (pct.size == e.sets.size && pct.all { it in 50.0..70.0 }) {
                val lifts = e.sets.sumOf { it.reps }
                assertTrue(lifts in 8..24)
            }
        }
    }
}

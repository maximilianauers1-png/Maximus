package app.maximus.strongman.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {

    private fun week(rule: ProgressionRule, sets: List<SetPrescription>) =
        ProgramWeek(listOf(ProgramDay("A", listOf(ProgramExercise(1L, rule, sets)))))

    private fun sets(w: ProgramWeek) = w.days[0].exercises[0].sets

    @Test
    fun linearAddsPerWeek() {
        val base = week(ProgressionRule.Linear(2.5, 0.0), listOf(SetPrescription(5, LoadSpec.Absolute(100.0))))
        val weeks = ProgramGenerator.generate(base, 4, 0)
        assertEquals(listOf(100.0, 102.5, 105.0, 107.5), weeks.map { (sets(it)[0].load as LoadSpec.Absolute).kg })
    }

    @Test
    fun deloadWeekKeepsLevelAndCutsVolume() {
        val base = week(ProgressionRule.Linear(5.0, 0.0), List(5) { SetPrescription(5, LoadSpec.Absolute(100.0)) })
        val weeks = ProgramGenerator.generate(base, 5, 4)
        // levels: 1,2,3,3,4
        assertEquals(listOf(100.0, 105.0, 110.0, 110.0, 115.0), weeks.map { (sets(it)[0].load as LoadSpec.Absolute).kg })
        assertEquals(25, sets(weeks[2]).sumOf { it.reps })
        assertEquals(15, sets(weeks[3]).sumOf { it.reps })
    }

    @Test
    fun deloadTrimsToExactTarget() {
        val out = ProgramGenerator.deload(listOf(SetPrescription(5, LoadSpec.Absolute(1.0)), SetPrescription(3, LoadSpec.Absolute(1.0))))
        // R = 8, T = round(4.8) = 5
        assertEquals(listOf(5), out.map { it.reps })
    }

    @Test
    fun plannedDoubleProgression() {
        val base = week(ProgressionRule.DoubleProgression(8, 10, 5.0), listOf(SetPrescription(8, LoadSpec.Absolute(60.0))))
        val weeks = ProgramGenerator.generate(base, 5, 0)
        assertEquals(listOf(8, 9, 10, 8, 9), weeks.map { sets(it)[0].reps })
        assertEquals(listOf(60.0, 60.0, 60.0, 65.0, 65.0), weeks.map { (sets(it)[0].load as LoadSpec.Absolute).kg })
    }

    @Test
    fun waveLoading() {
        val base = week(ProgressionRule.Wave(5.0, 2.5, 3), listOf(SetPrescription(3, LoadSpec.PercentOneRm(75.0))))
        val weeks = ProgramGenerator.generate(base, 6, 0)
        assertEquals(listOf(75.0, 80.0, 85.0, 77.5, 82.5, 87.5), weeks.map { (sets(it)[0].load as LoadSpec.PercentOneRm).percent })
    }

    @Test
    fun rpeAutoregulationResolvesFromLatestE1rm() {
        val base = week(ProgressionRule.RpeAutoregulation(0.5), listOf(SetPrescription(5, LoadSpec.AtRpe(7.0))))
        val w3 = ProgramGenerator.generate(base, 3, 0)[2]
        assertEquals(8.0, (sets(w3)[0].load as LoadSpec.AtRpe).rpe, 1e-12)
        val kg = LoadResolver.resolve(sets(w3)[0], LoadContext(200.0, null, 2.5, FTable()))
        assertEquals(162.5, kg!!, 1e-9)
    }

    @Test
    fun doubleProgressionLogic() {
        assertEquals(DoubleProgressionLogic.Next(105.0, 8), DoubleProgressionLogic.next(100.0, 10, 8, 10, 5.0, listOf(10, 10, 10)))
        assertEquals(DoubleProgressionLogic.Next(100.0, 9), DoubleProgressionLogic.next(100.0, 8, 8, 10, 5.0, listOf(8, 9, 8)))
        assertEquals(DoubleProgressionLogic.Next(100.0, 8), DoubleProgressionLogic.next(100.0, 8, 8, 10, 5.0, listOf(8, 7)))
    }

    @Test
    fun copyWeek() {
        val p = Program(1, "P", false, 0, listOf(week(ProgressionRule.None, listOf(SetPrescription(5, LoadSpec.Absolute(100.0)))),
            ProgramWeek(emptyList())))
        assertTrue(p.copyWeek(0, 1).weeks[1].days.isNotEmpty())
    }
}

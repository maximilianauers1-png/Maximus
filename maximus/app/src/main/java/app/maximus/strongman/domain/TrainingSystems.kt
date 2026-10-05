package app.maximus.strongman.domain

/** One prescribed set of a generated system; [amrap] = "so viele saubere Wiederholungen wie möglich". */
data class SysSet(val reps: Int, val kg: Double, val percent: Double, val amrap: Boolean = false, val note: String = "")

data class SysDay(val name: String, val sets: List<SysSet>)

data class SysWeek(val name: String, val days: List<SysDay>)

enum class TrainingSystem(val title: String, val origin: String, val summary: String, val forWhom: String) {
    WENDLER_531(
        "5/3/1", "Jim Wendler",
        "Trainingsmaximum = 90 % des 1RM. Drei Wellen (5er, 3er, 5/3/1) mit AMRAP-Satz, dann Deload. Nach jedem Zyklus +2,5 kg (Oberkörper) bzw. +5 kg (Unterkörper).",
        "Langfristig, robust, ideal für Naturalathleten neben Event-Training."
    ),
    WENDLER_BBB(
        "5/3/1 Boring But Big", "Jim Wendler",
        "5/3/1-Hauptsätze plus 5 × 10 mit 50–60 % TM für Hypertrophie.",
        "Off-Season: Masse und Arbeitskapazität."
    ),
    SMOLOV_JR(
        "Smolov Jr.", "nach S. Smolov",
        "3 Wochen, 4 Tage: 6×6 @70 %, 7×5 @75 %, 8×4 @80 %, 10×3 @85 %; Woche 2 und 3 jeweils mehr Last.",
        "Kurzer Spezialisierungsblock für EINE Übung, nicht in den letzten 6 Wochen vor einem Wettkampf."
    ),
    RPE_BLOCK(
        "RPE-Block (Top-Satz + Back-off)", "nach Tuchscherer / RTS",
        "Top-Satz nach RPE, dann Back-off-Sätze mit 7,5–10 % weniger Last (Load Drop). Selbstregulierend.",
        "Fortgeschrittene, die ihre RPE ehrlich einschätzen können."
    ),
    EVENT_PEAK(
        "Event-Peaking (6 Wochen)", "Strongman-Praxis",
        "Wettkampflast-basiert: 70 → 95 % mit fallenden Wiederholungen, Woche 6 Wettkampfsimulation.",
        "Die letzten 6 Wochen vor dem Wettkampf, pro Wettkampf-Event."
    )
}

/**
 * Generators for well-known strength systems. All loads are rounded to [increment]; percentages refer to
 * the 1RM (e1RM) unless a system defines its own reference (5/3/1: the training max).
 */
object TrainingSystems {
    fun generate(system: TrainingSystem, oneRm: Double, increment: Double, lowerBody: Boolean): List<SysWeek> = when (system) {
        TrainingSystem.WENDLER_531 -> wendler(oneRm, increment, bbb = false)
        TrainingSystem.WENDLER_BBB -> wendler(oneRm, increment, bbb = true)
        TrainingSystem.SMOLOV_JR -> smolovJr(oneRm, increment, if (lowerBody) 5.0 else 2.5)
        TrainingSystem.RPE_BLOCK -> rpeBlock(oneRm, increment)
        TrainingSystem.EVENT_PEAK -> eventPeak(oneRm, increment)
    }

    private fun r(x: Double, inc: Double) = roundToIncrement(x, inc)

    /** 5/3/1: TM = 0.9 · 1RM; week k uses the classic percentage triplets of the TM. */
    fun wendler(oneRm: Double, inc: Double, bbb: Boolean): List<SysWeek> {
        val tm = 0.9 * oneRm
        val waves = listOf(
            "Woche 1 · 5er" to listOf(0.65 to 5, 0.75 to 5, 0.85 to 5),
            "Woche 2 · 3er" to listOf(0.70 to 3, 0.80 to 3, 0.90 to 3),
            "Woche 3 · 5/3/1" to listOf(0.75 to 5, 0.85 to 3, 0.95 to 1),
            "Woche 4 · Deload" to listOf(0.40 to 5, 0.50 to 5, 0.60 to 5)
        )
        val bbbPct = listOf(0.50, 0.55, 0.60, 0.0)
        return waves.mapIndexed { w, (name, sets) ->
            val main = sets.mapIndexed { i, (p, reps) ->
                val last = i == 2 && w < 3
                SysSet(reps, r(tm * p, inc), 100 * p * 0.9, amrap = last, note = if (last) "AMRAP – sauber, 1–2 Wdh. im Tank" else "")
            }
            val extra = if (bbb && w < 3) List(5) { SysSet(10, r(tm * bbbPct[w], inc), 100 * bbbPct[w] * 0.9, note = "BBB") } else emptyList()
            SysWeek(name, listOf(SysDay("Haupttag", main + extra)))
        }
    }

    /** Smolov Jr.: 4 days per week; weeks 2 and 3 add Δ and 2Δ on the week-1 loads. */
    fun smolovJr(oneRm: Double, inc: Double, delta: Double): List<SysWeek> {
        val days = listOf(Triple("Tag 1", 6, 6) to 0.70, Triple("Tag 2", 7, 5) to 0.75, Triple("Tag 3", 8, 4) to 0.80, Triple("Tag 4", 10, 3) to 0.85)
        return (0 until 3).map { w ->
            SysWeek("Woche ${w + 1}", days.map { (d, p) ->
                val kg = r(oneRm * p + w * delta, inc)
                SysDay(d.first, List(d.second) { SysSet(d.third, kg, 100 * kg / oneRm) })
            })
        } + SysWeek("Woche 4 · Test", listOf(SysDay("Testtag", listOf(
            SysSet(1, r(oneRm * 0.9, inc), 90.0, note = "Aufwärm-Single"),
            SysSet(1, r(oneRm * 1.03, inc), 103.0, note = "Neuer Rekordversuch (≈ +3–5 %)")
        ))))
    }

    /** Four-week RPE block: top set at rising RPE, back-offs by load drop; week 4 pivot/deload. */
    fun rpeBlock(oneRm: Double, inc: Double): List<SysWeek> {
        val t = FTable()
        fun top(reps: Int, rpe: Double) = t.prescribe(oneRm, reps, rpe, inc)
        val plan = listOf(
            Triple("Woche 1", 5, 7.0) to 0.10, Triple("Woche 2", 4, 8.0) to 0.08,
            Triple("Woche 3", 3, 9.0) to 0.075, Triple("Woche 4 · Pivot", 5, 6.0) to 0.0
        )
        return plan.map { (p, drop) ->
            val (name, reps, rpe) = p
            val kg = top(reps, rpe)
            val sets = mutableListOf(SysSet(reps, kg, 100 * kg / oneRm, note = "Top-Satz @ RPE ${fmtRpe(rpe)}"))
            if (drop > 0) repeat(3) { sets += SysSet(reps, r(kg * (1 - drop), inc), 100 * kg * (1 - drop) / oneRm, note = "Back-off −${(drop * 100).toInt()} %, stoppen bei RPE 8,5") }
            SysWeek(name, listOf(SysDay("Haupttag", sets)))
        }
    }

    /** Event peaking against the contest load (here: the e1RM or contest weight passed as [oneRm]). */
    fun eventPeak(oneRm: Double, inc: Double): List<SysWeek> {
        val plan = listOf(
            Triple(0.70, 5, 3), Triple(0.75, 4, 3), Triple(0.80, 4, 2),
            Triple(0.85, 3, 2), Triple(0.90, 3, 1), Triple(0.95, 2, 1)
        )
        return plan.mapIndexed { i, (p, sets, reps) ->
            val note = when (i) {
                5 -> "Wettkampfsimulation mit Originalablauf, danach Taper"
                4 -> "Schwerster Tag – Technik unter Last"
                else -> "Explosiv, jede Wiederholung wie im Wettkampf"
            }
            SysWeek("Woche ${i + 1}", listOf(SysDay("Event-Tag", List(sets) { SysSet(reps, r(oneRm * p, inc), 100 * p, note = note) })))
        }
    }

    private fun fmtRpe(x: Double) = if (x % 1.0 == 0.0) x.toInt().toString() else x.toString().replace('.', ',')

    /** Converts a generated system into a log-ready program week list for one exercise (absolute loads). */
    fun toProgramWeeks(weeks: List<SysWeek>, exerciseId: Long): List<ProgramWeek> = weeks.map { w ->
        ProgramWeek(w.days.map { d ->
            ProgramDay("${w.name} · ${d.name}", listOf(ProgramExercise(exerciseId, ProgressionRule.None,
                d.sets.map { s -> SetPrescription(s.reps, LoadSpec.Absolute(s.kg), 180, listOf(if (s.amrap) "AMRAP" else "", s.note).filter { it.isNotBlank() }.joinToString(" · ")) })))
        })
    }
}

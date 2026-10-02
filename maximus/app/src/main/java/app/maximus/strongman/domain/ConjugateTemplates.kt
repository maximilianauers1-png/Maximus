package app.maximus.strongman.domain

/**
 * Three 4-week conjugate templates for strongman (Westside principles adapted to log/axle and deadlift
 * variants, plus a dedicated event day). Structure of every week:
 *
 *   Max effort (ME): one special exercise worked up to a 1RM or 3RM of the day (RPE ≈ 9, no grinding),
 *   rotated every week so no single variant is maxed more often than every third week (Simmons 2007:
 *   rotation limits accommodation); weeks 1–3 rotate, week 4 is a deload (volume ≈ −40 %, RPE ≤ 7).
 *   Dynamic effort (DE): 50–70 % 1RM for 8–10 × 1–2 at maximal intended velocity, 45–60 s rest;
 *   per session this is ≈ 16–20 lifts, inside Prilepin's optimal range for 55–65 % (≈ 24 ± 6) and
 *   below the range for 70–80 % (≈ 18), so bar speed stays high.
 *   Accessories: 3–4 × 8–15 at RPE 7–8 with ordinary gym equipment; they carry most of the hypertrophy
 *   volume for triceps, upper back, hamstrings and trunk, the usual limiters of log press and deadlift.
 *   Events: 3–5 working sets, loads chosen by RPE, on the day farthest from the ME deadlift.
 *
 * Loads are given as RPE or %1RM and resolve against the e1RM of the respective exercise from the log.
 */
object ConjugateTemplates {
    data class Template(val name: String, val weeks: List<ProgramWeek>)

    const val WEEKS = 4
    const val DELOAD_EVERY = 4

    private class B(val id: (String) -> Long) {
        fun ex(key: String, sets: List<SetPrescription>) = ProgramExercise(id(key), ProgressionRule.None, sets)

        /** ME 1RM: ramp 3 @ RPE 7, 2 @ 8, top single @ 9, then 2 × 3 back-off @ 8. */
        fun me1(key: String) = ex(key, listOf(
            SetPrescription(3, LoadSpec.AtRpe(7.0), 150, "Rampe"),
            SetPrescription(2, LoadSpec.AtRpe(8.0), 180, "Rampe"),
            SetPrescription(1, LoadSpec.AtRpe(9.0), 240, "Tagesmaximum, sauber, kein Grinding"),
            SetPrescription(3, LoadSpec.AtRpe(8.0), 180, "Back-off"),
            SetPrescription(3, LoadSpec.AtRpe(8.0), 180, "Back-off")
        ))

        /** ME 3RM: ramp 5 @ 7, 3 @ 8, top triple @ 9, then 2 × 5 back-off @ 7.5. */
        fun me3(key: String) = ex(key, listOf(
            SetPrescription(5, LoadSpec.AtRpe(7.0), 150, "Rampe"),
            SetPrescription(3, LoadSpec.AtRpe(8.0), 180, "Rampe"),
            SetPrescription(3, LoadSpec.AtRpe(9.0), 240, "3RM des Tages"),
            SetPrescription(5, LoadSpec.AtRpe(7.5), 180, "Back-off"),
            SetPrescription(5, LoadSpec.AtRpe(7.5), 180, "Back-off")
        ))

        /** Deload technique work: 5 × 2 at 65 % 1RM. */
        fun tech(key: String) = ex(key, List(5) { SetPrescription(2, LoadSpec.PercentOneRm(65.0), 120, "Technik, Deload") })

        fun de(key: String, sets: Int, reps: Int, percent: Double) =
            ex(key, List(sets) { SetPrescription(reps, LoadSpec.PercentOneRm(percent), 60, "maximal explosiv") })

        fun acc(key: String, sets: Int, reps: Int, rpe: Double = 8.0, rest: Int = 90) =
            ex(key, List(sets) { SetPrescription(reps, LoadSpec.AtRpe(rpe), rest) })

        fun carry(key: String, sets: Int, meters: Double, rpe: Double = 8.0) =
            ex(key, List(sets) { SetPrescription(1, LoadSpec.AtRpe(rpe), 180, "", EventSpec(distanceM = meters, mode = EventMode.FOR_TIME)) })

        fun event(key: String, sets: Int, reps: Int, rpe: Double = 8.0, mode: EventMode = EventMode.MAX_REPS) =
            ex(key, List(sets) { SetPrescription(reps, LoadSpec.AtRpe(rpe), 180, "", EventSpec(mode = mode)) })
    }

    /** Deload multiplier on accessory/event set counts in week 4: ⌈0.6 n⌉. */
    private fun dl(n: Int, week: Int) = if (week == 3) ((n * 3 + 4) / 5).coerceAtLeast(1) else n
    private fun rpeDl(rpe: Double, week: Int) = if (week == 3) minOf(rpe, 7.0) else rpe

    fun all(id: (String) -> Long): List<Template> = listOf(classic(id), eventFocus(id), hybrid(id))

    /** A: ME overhead, ME deadlift, DE/repetition upper body, event day. */
    fun classic(id: (String) -> Long): Template {
        val b = B(id)
        val pressMe = listOf(b.me1("log_clean_press"), b.me3("axle_press"), b.me3("z_press"), b.tech("log_clean_press"))
        val pullMe = listOf(b.me3("deadlift_deficit"), b.me1("deadlift_18inch"), b.me1("deadlift_axle"), b.tech("deadlift_conventional"))
        val dePct = listOf(60.0, 65.0, 70.0, 50.0)
        val weeks = (0 until WEEKS).map { w ->
            ProgramWeek(listOf(
                ProgramDay("Tag 1 – Max Effort Überkopf", listOf(
                    pressMe[w],
                    b.acc("close_grip_bench", dl(4, w), 6, rpeDl(8.0, w), 150),
                    b.acc("jm_press", dl(3, w), 10, rpeDl(8.0, w)),
                    b.acc("db_lateral_raise", dl(3, w), 15, rpeDl(8.0, w), 60),
                    b.acc("face_pull", dl(3, w), 15, rpeDl(8.0, w), 60),
                    b.acc("ab_wheel", dl(3, w), 10, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 2 – Max Effort Kreuzheben", listOf(
                    pullMe[w],
                    b.acc("romanian_deadlift", dl(3, w), 8, rpeDl(7.5, w), 150),
                    b.acc("barbell_row", dl(4, w), 8, rpeDl(8.0, w), 120),
                    b.acc("back_extension", dl(3, w), 12, rpeDl(8.0, w)),
                    b.acc("leg_curl", dl(3, w), 12, rpeDl(8.0, w)),
                    b.acc("hanging_leg_raise", dl(3, w), 12, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 3 – Dynamik & Wiederholung Oberkörper", listOf(
                    b.de("push_press", if (w == 3) 5 else 8, 2, dePct[w]),
                    b.acc("db_bench_press", dl(4, w), 10, rpeDl(8.0, w)),
                    b.acc("db_row", dl(4, w), 12, rpeDl(8.0, w)),
                    b.acc("pull_up", dl(4, w), 8, rpeDl(8.0, w)),
                    b.acc("triceps_pushdown", dl(3, w), 15, rpeDl(8.0, w), 60),
                    b.acc("hammer_curl", dl(3, w), 12, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 4 – Event-Tag", listOf(
                    b.carry("yoke_carry", dl(4, w), 20.0, rpeDl(8.0, w)),
                    b.carry("farmers_walk", dl(4, w), 20.0, rpeDl(8.0, w)),
                    b.event("atlas_stone_over_bar", dl(5, w), 1, rpeDl(8.0, w)),
                    b.carry("sled_drag", dl(3, w), 30.0, rpeDl(7.0, w))
                ))
            ))
        }
        return Template("Konjugat A – Klassik", weeks)
    }

    /** B: ME overhead, ME deadlift, moving events, static events with overhead volume. */
    fun eventFocus(id: (String) -> Long): Template {
        val b = B(id)
        val pressMe = listOf(b.me1("axle_clean_press"), b.me3("push_press"), b.me1("log_clean_press"), b.tech("axle_clean_press"))
        val pullMe = listOf(b.me1("deadlift_axle"), b.me3("deadlift_trap_bar"), b.me3("deadlift_deficit"), b.tech("deadlift_conventional"))
        val weeks = (0 until WEEKS).map { w ->
            ProgramWeek(listOf(
                ProgramDay("Tag 1 – Max Effort Überkopf", listOf(
                    pressMe[w],
                    b.acc("floor_press", dl(4, w), 5, rpeDl(8.0, w), 150),
                    b.acc("dips", dl(3, w), 10, rpeDl(8.0, w)),
                    b.acc("face_pull", dl(3, w), 15, rpeDl(8.0, w), 60),
                    b.acc("hanging_leg_raise", dl(3, w), 12, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 2 – Max Effort Kreuzheben", listOf(
                    pullMe[w],
                    b.acc("good_morning", dl(3, w), 8, rpeDl(7.5, w), 150),
                    b.acc("lat_pulldown", dl(4, w), 10, rpeDl(8.0, w)),
                    b.acc("back_extension", dl(3, w), 15, rpeDl(8.0, w)),
                    b.acc("shrug", dl(3, w), 12, rpeDl(8.0, w))
                )),
                ProgramDay("Tag 3 – Events in Bewegung", listOf(
                    b.carry("yoke_carry", dl(5, w), 20.0, rpeDl(8.5, w)),
                    b.carry("frame_carry", dl(4, w), 20.0, rpeDl(8.0, w)),
                    b.carry("sandbag_carry", dl(3, w), 30.0, rpeDl(8.0, w)),
                    b.carry("sled_push", dl(3, w), 20.0, rpeDl(7.5, w)),
                    b.acc("walking_lunge", dl(3, w), 12, rpeDl(7.5, w))
                )),
                ProgramDay("Tag 4 – Statische Events & Überkopf-Volumen", listOf(
                    b.de("log_clean_press", if (w == 3) 3 else 5, 3, listOf(70.0, 72.5, 75.0, 60.0)[w]),
                    b.event("atlas_stone_platform", dl(5, w), 1, rpeDl(8.5, w), EventMode.MAX_HEIGHT),
                    b.event("sandbag_over_bar", dl(3, w), 3, rpeDl(8.0, w)),
                    b.event("tire_flip", dl(3, w), 4, rpeDl(8.0, w)),
                    b.acc("db_row", dl(3, w), 12, rpeDl(8.0, w))
                ))
            ))
        }
        return Template("Konjugat B – Event-Schwerpunkt", weeks)
    }

    /** C: ME overhead, DE lower body (box squat + speed pulls), ME deadlift, event medley. */
    fun hybrid(id: (String) -> Long): Template {
        val b = B(id)
        val pressMe = listOf(b.me3("strict_press"), b.me1("log_clean_press"), b.me3("close_grip_bench"), b.tech("log_clean_press"))
        val pullMe = listOf(b.me1("deadlift_18inch"), b.me3("deadlift_deficit"), b.me1("deadlift_conventional"), b.tech("deadlift_conventional"))
        val squatPct = listOf(50.0, 55.0, 60.0, 45.0)
        val pullPct = listOf(60.0, 65.0, 70.0, 55.0)
        val weeks = (0 until WEEKS).map { w ->
            ProgramWeek(listOf(
                ProgramDay("Tag 1 – Max Effort Überkopf", listOf(
                    pressMe[w],
                    b.acc("db_shoulder_press", dl(3, w), 10, rpeDl(8.0, w)),
                    b.acc("jm_press", dl(3, w), 10, rpeDl(8.0, w)),
                    b.acc("db_row", dl(4, w), 12, rpeDl(8.0, w)),
                    b.acc("face_pull", dl(3, w), 15, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 2 – Dynamik Unterkörper", listOf(
                    b.de("box_squat", if (w == 3) 6 else 10, 2, squatPct[w]),
                    b.de("deadlift_conventional", if (w == 3) 5 else 8, 1, pullPct[w]),
                    b.acc("bulgarian_split_squat", dl(3, w), 10, rpeDl(8.0, w)),
                    b.acc("leg_curl", dl(3, w), 12, rpeDl(8.0, w)),
                    b.acc("ab_wheel", dl(3, w), 10, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 3 – Max Effort Kreuzheben", listOf(
                    pullMe[w],
                    b.acc("romanian_deadlift", dl(3, w), 8, rpeDl(7.5, w), 150),
                    b.acc("barbell_row", dl(4, w), 8, rpeDl(8.0, w), 120),
                    b.acc("back_extension", dl(3, w), 12, rpeDl(8.0, w)),
                    b.acc("hanging_leg_raise", dl(3, w), 12, rpeDl(8.0, w), 60)
                )),
                ProgramDay("Tag 4 – Event-Medley", listOf(
                    b.event("axle_clean_press", dl(4, w), 5, rpeDl(8.0, w)),
                    b.carry("farmers_walk", dl(4, w), 25.0, rpeDl(8.0, w)),
                    b.carry("sandbag_carry", dl(3, w), 25.0, rpeDl(8.0, w)),
                    b.event("atlas_stone_over_bar", dl(4, w), 2, rpeDl(8.0, w)),
                    b.carry("sled_drag", dl(3, w), 30.0, rpeDl(7.0, w))
                ))
            ))
        }
        return Template("Konjugat C – Hybrid mit Kniebeuge", weeks)
    }

    /** All seed keys referenced by the templates (for validation). */
    fun referencedKeys(): Set<String> {
        val keys = LinkedHashSet<String>()
        val ids = HashMap<Long, String>()
        all { k -> ids.entries.firstOrNull { it.value == k }?.key ?: (ids.size.toLong() + 1).also { ids[it] = k; keys += k } }
        return keys
    }
}

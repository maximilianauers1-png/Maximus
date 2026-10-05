package app.maximus.strongman.domain

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

/** Best performance of one standard lift: the best e1RM and the set that produced it. */
data class LiftBest(
    val lift: StandardLift,
    val e1rm: Double,
    val weightKg: Double,
    val reps: Int,
    val epochDay: Long,
    val bestSingleKg: Double?,
    val historic: Boolean
)

data class OverallStanding(val z: Double, val percentile: Double, val rank: Rank, val rating: Int, val lifts: Int)

data class LevelState(val xp: Long, val level: Int, val xpIntoLevel: Long, val xpForLevel: Long) {
    val progress: Double get() = if (xpForLevel <= 0) 1.0 else xpIntoLevel.toDouble() / xpForLevel
}

data class Badge(
    val id: String,
    val group: String,
    val title: String,
    val description: String,
    val progress: Double,
    val achievedDay: Long?
) {
    val achieved: Boolean get() = achievedDay != null || progress >= 1.0
}

data class Quest(val id: String, val title: String, val description: String, val current: Double, val target: Double, val xp: Int) {
    val done: Boolean get() = current >= target
    val progress: Double get() = if (target <= 0) 1.0 else (current / target).coerceIn(0.0, 1.0)
}

enum class BossKind(val title: String) { PAST_SELF("Schatten der Vergangenheit"), GATEKEEPER("Torwächter"), RECORD("Der eigene Rekord") }

data class Boss(val kind: BossKind, val lift: StandardLift, val name: String, val targetKg: Double, val currentKg: Double) {
    val defeated: Boolean get() = currentKg >= targetKg
    /** Remaining "hit points" as a fraction: the gap relative to 15 % of the target. */
    val hp: Double get() = ((targetKg - currentKg) / (0.15 * targetKg)).coerceIn(0.0, 1.0)
}

/**
 * The game layer of the strongman module. Everything is derived from the training log and the record
 * book on the fly (nothing to keep in sync): ranks, experience, badges, weekly quests and bosses.
 */
object Arena {
    /** A logged set or historic record mapped to a standard lift. */
    data class LiftEvent(val lift: StandardLift, val epochDay: Long, val weightKg: Double, val reps: Int, val e1rm: Double?)

    fun liftEvents(sets: List<AnalyticsSet>, historic: List<HistoricRecord>, liftOf: (Long) -> StandardLift?): List<LiftEvent> {
        val a = sets.mapNotNull { s -> liftOf(s.exerciseId)?.let { LiftEvent(it, s.epochDay, s.weightKg, s.reps, Performance.e1rm(s.weightKg, s.reps, s.rpe, s.e1rm)) } }
        val b = historic.mapNotNull { h -> liftOf(h.exerciseId)?.let { LiftEvent(it, h.epochDay, h.weightKg, h.reps, h.e1rm) } }
        return (a + b).filter { it.weightKg > 0 && it.reps > 0 }.sortedBy { it.epochDay }
    }

    /**
     * Best e1RM per lift (rep PRs count through their e1RM) plus the heaviest true single. The SBD total
     * is the sum of the three best e1RMs when all three exist; [historicDays] marks events from the record book.
     */
    fun bests(events: List<LiftEvent>, historicDays: Set<Pair<StandardLift, Long>> = emptySet(), asOfDay: Long = Long.MAX_VALUE): Map<StandardLift, LiftBest> {
        val out = HashMap<StandardLift, LiftBest>()
        for ((lift, list) in events.filter { it.epochDay <= asOfDay && it.e1rm != null }.groupBy { it.lift }) {
            val top = list.maxBy { it.e1rm!! }
            val single = list.filter { it.reps == 1 }.maxOfOrNull { it.weightKg }
            out[lift] = LiftBest(lift, top.e1rm!!, top.weightKg, top.reps, top.epochDay, single, (lift to top.epochDay) in historicDays)
        }
        val s = out[StandardLift.SQUAT]; val b = out[StandardLift.BENCH]; val d = out[StandardLift.DEADLIFT]
        if (s != null && b != null && d != null) {
            out[StandardLift.TOTAL] = LiftBest(StandardLift.TOTAL, s.e1rm + b.e1rm + d.e1rm, s.e1rm + b.e1rm + d.e1rm, 1,
                maxOf(s.epochDay, b.epochDay, d.epochDay), null, false)
        }
        return out
    }

    fun placements(bests: Map<StandardLift, LiftBest>, population: Population, sex: Sex, bodyweightKg: Double?, mode: ScoreMode): List<Placement> =
        StandardLift.entries.mapNotNull { l -> bests[l]?.let { StrengthStandards.place(l, it.e1rm, population, sex, bodyweightKg, mode) } }

    /** Overall standing from the single lifts (the total is excluded, it would count S, B and D twice). */
    fun overall(placements: List<Placement>): OverallStanding? {
        val zs = placements.filter { it.lift != StandardLift.TOTAL }.map { it.z }
        val z = StrengthStandards.combinedZ(zs) ?: return null
        val p = 100 * Normal.cdf(z)
        return OverallStanding(z, p, Ranks.forPercentile(p), Ranks.rating(z), zs.size)
    }

    // ---------------- Experience ----------------

    const val XP_DAY = 40
    const val XP_SET = 2
    const val XP_PER_100KG = 1
    const val XP_E1RM_PR = 60
    const val XP_REP_PR = 25

    fun xpOf(sets: List<AnalyticsSet>): Long {
        val days = sets.map { it.epochDay }.distinct().size
        val tonnage = sets.sumOf { it.weightKg * it.reps }
        return days * XP_DAY.toLong() + sets.size * XP_SET.toLong() + (tonnage / 100.0 * XP_PER_100KG).roundToLong() +
            sets.count { it.isE1rmPr } * XP_E1RM_PR.toLong() + sets.count { it.isRepPr && !it.isE1rmPr } * XP_REP_PR.toLong()
    }

    /** Cumulative XP needed to reach level L: 150 (L − 1)^1.55 — each level a bit longer than the last. */
    fun xpForLevel(level: Int): Long = (150.0 * (level - 1).toDouble().pow(1.55)).roundToLong()

    fun level(xp: Long): LevelState {
        var l = max(1, floor((xp / 150.0).pow(1 / 1.55)).toInt() + 1)
        while (xpForLevel(l + 1) <= xp) l++
        while (l > 1 && xpForLevel(l) > xp) l--
        val base = xpForLevel(l)
        return LevelState(xp, l, xp - base, xpForLevel(l + 1) - base)
    }

    // ---------------- Streaks ----------------

    /** Consecutive Monday-weeks with at least one training day, ending this week (or last week, still alive). */
    fun weekStreak(days: Collection<Long>, today: Long): Int {
        val weeks = days.map { WeekMath.mondayWeekIndex(it) }.toSet()
        var w = WeekMath.mondayWeekIndex(today)
        if (w !in weeks) w -= 1
        var n = 0
        while (w in weeks) { n++; w-- }
        return n
    }

    fun longestWeekStreak(days: Collection<Long>): Int {
        val weeks = days.map { WeekMath.mondayWeekIndex(it) }.toSortedSet()
        var best = 0; var run = 0; var prev: Long? = null
        for (w in weeks) { run = if (prev != null && w == prev + 1) run + 1 else 1; best = max(best, run); prev = w }
        return best
    }

    // ---------------- Weekly quests ----------------

    /**
     * Quests of the week containing [day]. [isEvent] marks strongman event exercises (stones, carries,
     * implements). Tonnage target: 110 % of the mean of the previous four weeks, at least 5 t.
     */
    fun quests(sets: List<AnalyticsSet>, day: Long, isEvent: (Long) -> Boolean): List<Quest> {
        val week = WeekMath.mondayWeekIndex(day)
        val mine = sets.filter { WeekMath.mondayWeekIndex(it.epochDay) == week }
        val prev = (1..4).map { k -> sets.filter { WeekMath.mondayWeekIndex(it.epochDay) == week - k }.sumOf { it.weightKg * it.reps } }
        val tonTarget = max(5000.0, 1.1 * prev.average())
        val bestBefore = HashMap<Long, Double>()
        sets.filter { WeekMath.mondayWeekIndex(it.epochDay) < week }
            .forEach { s -> Performance.e1rm(s.weightKg, s.reps, s.rpe, s.e1rm)?.let { bestBefore.merge(s.exerciseId, it, ::maxOf) } }
        val heavy = mine.count { s -> val b = bestBefore[s.exerciseId]; b != null && s.weightKg >= 0.9 * b }
        return listOf(
            Quest("days", "Drei Schlachten", "Trainiere an 3 Tagen dieser Woche.", mine.map { it.epochDay }.distinct().size.toDouble(), 3.0, 120),
            Quest("pr", "Rekordjäger", "Stelle einen e1RM- oder Wiederholungs-Rekord auf.", mine.count { it.isE1rmPr || it.isRepPr }.toDouble(), 1.0, 100),
            Quest("tonnage", "Eisenberg", "Bewege ${"%.1f".format(tonTarget / 1000)} t (110 % deines 4-Wochen-Schnitts).", mine.sumOf { it.weightKg * it.reps }, tonTarget, 80),
            Quest("event", "Arena-Ruf", "Trainiere mindestens 3 Sätze an Strongman-Events.", mine.count { isEvent(it.exerciseId) }.toDouble(), 3.0, 80),
            Quest("heavy", "Schwerer Tag", "Bewege eine Last ≥ 90 % deines bisherigen e1RM.", heavy.toDouble(), 1.0, 60)
        )
    }

    /** XP from every completed quest in every past and the current week (retroactive, deterministic). */
    fun questXp(sets: List<AnalyticsSet>, isEvent: (Long) -> Boolean): Long =
        sets.map { WeekMath.mondayWeekIndex(it.epochDay) }.distinct()
            .sumOf { w -> quests(sets, WeekMath.mondayOfWeek(w), isEvent).filter { it.done }.sumOf { it.xp.toLong() } }

    // ---------------- Bosses ----------------

    fun bosses(
        events: List<LiftEvent>, today: Long, placementsByLift: Map<StandardLift, Placement>
    ): List<Boss> {
        val now = bests(events)
        val yearAgo = bests(events, asOfDay = today - 365)
        val out = ArrayList<Boss>()
        for (lift in StandardLift.singles) {
            val cur = now[lift] ?: continue
            yearAgo[lift]?.let { out += Boss(BossKind.PAST_SELF, lift, "Dein Ich vor einem Jahr", it.e1rm, recentBest(events, lift, today)) }
            placementsByLift[lift]?.let { p ->
                val next = Ranks.next(p.rank)
                if (p.nextTierKg != null && next != null) out += Boss(BossKind.GATEKEEPER, lift, "Torwächter der ${next.title}", p.nextTierKg, cur.e1rm)
            }
            out += Boss(BossKind.RECORD, lift, "Allzeit-Rekord + 2,5 kg", cur.e1rm + 2.5, recentBest(events, lift, today))
        }
        return out
    }

    /** Best e1RM within the last 120 days (current form). */
    fun recentBest(events: List<LiftEvent>, lift: StandardLift, today: Long): Double =
        events.filter { it.lift == lift && it.epochDay > today - 120 && it.e1rm != null }.maxOfOrNull { it.e1rm!! } ?: 0.0

    // ---------------- Badges ----------------

    private val PLATES = mapOf(
        StandardLift.DEADLIFT to (1..8), StandardLift.SQUAT to (1..7), StandardLift.BENCH to (1..5),
        StandardLift.STRICT_PRESS to (1..3), StandardLift.LOG to (1..4), StandardLift.AXLE_DEADLIFT to (1..7), StandardLift.STONE to (1..4)
    )
    private val BW_MULTIPLES = mapOf(
        StandardLift.DEADLIFT to listOf(1.5, 2.0, 2.5, 3.0), StandardLift.SQUAT to listOf(1.25, 1.5, 2.0, 2.5),
        StandardLift.BENCH to listOf(1.0, 1.25, 1.5), StandardLift.STRICT_PRESS to listOf(0.75, 1.0), StandardLift.LOG to listOf(1.0, 1.25, 1.5)
    )

    /** Load for n 20-kg plates per side on a 20-kg bar: 20 + 40 n. */
    fun plateLoad(n: Int): Double = 20.0 + 40.0 * n

    fun badges(
        sets: List<AnalyticsSet>,
        events: List<LiftEvent>,
        bodyweightKg: Double?,
        categoryOf: (Long) -> String?,
        bestRank: Rank?
    ): List<Badge> {
        val out = ArrayList<Badge>()
        fun firstDay(lift: StandardLift, kg: Double): Long? = events.firstOrNull { it.lift == lift && it.weightKg >= kg - 1e-9 }?.epochDay
        fun maxKg(lift: StandardLift): Double = events.filter { it.lift == lift }.maxOfOrNull { it.weightKg } ?: 0.0

        for ((lift, range) in PLATES) for (n in range) {
            val kg = plateLoad(n)
            out += Badge("plate_${lift.name}_$n", "Scheiben", "$n ${if (n == 1) "Scheibe" else "Scheiben"} · ${lift.short}",
                "${lift.title} mit ${kg.toInt()} kg (20-kg-Scheiben je Seite).", min(1.0, maxKg(lift) / kg), firstDay(lift, kg))
        }
        if (bodyweightKg != null && bodyweightKg > 0) for ((lift, list) in BW_MULTIPLES) for (m in list) {
            val kg = m * bodyweightKg
            out += Badge("bw_${lift.name}_$m", "Körpergewicht", "${fmtMultiple(m)}× KG · ${lift.short}",
                "${lift.title} mit dem ${fmtMultiple(m)}-fachen Körpergewicht (aktuell ${kg.toInt()} kg).", min(1.0, maxKg(lift) / kg), firstDay(lift, kg))
        }

        val days = sets.map { it.epochDay }.distinct().sorted()
        for (n in listOf(1, 10, 25, 50, 100, 250, 500)) {
            out += Badge("sessions_$n", "Ausdauer", if (n == 1) "Erster Schritt" else "$n Trainingstage",
                "Trainiere an $n Tagen.", min(1.0, days.size / n.toDouble()), days.getOrNull(n - 1))
        }
        val chrono = sets.sortedBy { it.epochDay }
        for (t in listOf(10, 50, 100, 250, 500, 1000)) {
            var acc = 0.0; var hit: Long? = null
            for (s in chrono) { acc += s.weightKg * s.reps; if (acc >= t * 1000.0) { hit = s.epochDay; break } }
            val total = sets.sumOf { it.weightKg * it.reps }
            out += Badge("tonnage_$t", "Tonnage", "$t Tonnen bewegt", "Kumuliertes Volumen von $t t.", min(1.0, total / (t * 1000.0)), hit)
        }
        val prs = chrono.filter { it.isE1rmPr }
        for (n in listOf(1, 10, 25, 50, 100)) {
            out += Badge("prs_$n", "Rekorde", if (n == 1) "Erster Rekord" else "$n e1RM-Rekorde", "Stelle $n e1RM-Rekorde auf.",
                min(1.0, prs.size / n.toDouble()), prs.getOrNull(n - 1)?.epochDay)
        }
        val longest = longestWeekStreak(days)
        for (w in listOf(4, 8, 12, 26, 52)) {
            out += Badge("streak_$w", "Serie", "$w Wochen am Stück", "Trainiere $w Wochen ohne Lücke.", min(1.0, longest / w.toDouble()), null)
        }
        fun firstOfCategory(cat: String) = chrono.firstOrNull { categoryOf(it.exerciseId) == cat }?.epochDay
        out += Badge("cat_stone", "Events", "Steinbeißer", "Hebe zum ersten Mal einen Atlas Stone.", if (firstOfCategory("STONE") != null) 1.0 else 0.0, firstOfCategory("STONE"))
        out += Badge("cat_carry", "Events", "Lastenträger", "Trage zum ersten Mal Yoke, Farmers oder Sandsack.", if (firstOfCategory("CARRY") != null) 1.0 else 0.0, firstOfCategory("CARRY"))
        out += Badge("cat_event", "Events", "Arena betreten", "Trainiere ein Strongman-Event.", if (firstOfCategory("EVENT") != null) 1.0 else 0.0, firstOfCategory("EVENT"))
        val distinctEvents = chrono.filter { categoryOf(it.exerciseId) in setOf("STONE", "CARRY", "EVENT") }.map { it.exerciseId }.distinct().size
        for (n in listOf(5, 10, 20)) {
            out += Badge("events_$n", "Events", "$n verschiedene Events", "Trainiere $n verschiedene Event-Übungen.", min(1.0, distinctEvents / n.toDouble()), null)
        }
        for (r in listOf(Rank.RITTER, Rank.KRIEGSHERR, Rank.TITAN, Rank.LEGENDE)) {
            val have = bestRank != null && bestRank.ordinal >= r.ordinal
            out += Badge("rank_${r.name}", "Ränge", "Rang: ${r.title}", "Erreiche in einer Disziplin den Rang ${r.title} (Strongman-Population).",
                if (have) 1.0 else ((bestRank?.ordinal ?: 0).toDouble() / r.ordinal).coerceIn(0.0, 0.99), null)
        }
        return out
    }

    private fun fmtMultiple(m: Double): String = if (m == floor(m)) m.toInt().toString() else m.toString().replace('.', ',')
}

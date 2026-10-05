package app.maximus.strongman.domain

import kotlin.math.exp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArenaCoachTest {

    // ---------------- Normal distribution ----------------

    @Test
    fun normalCdfAndQuantileAreInverse() {
        assertEquals(0.5, Normal.cdf(0.0), 1e-7)
        assertEquals(0.975002, Normal.cdf(1.959964), 1e-5)
        for (p in listOf(1e-6, 0.01, 0.2, 0.5, 0.8, 0.99, 0.999999)) assertEquals(p, Normal.cdf(Normal.quantile(p)), 1e-6 * 10 + p * 1e-6)
        assertEquals(1.959964, Normal.quantile(0.975), 1e-5)
    }

    // ---------------- Population placement ----------------

    @Test
    fun medianLifterIsFiftiethPercentileInBothModes() {
        val n = StrengthStandards.norm(Population.POWERLIFTING, Sex.MALE)
        val abs = StrengthStandards.place(StandardLift.DEADLIFT, 230.0, Population.POWERLIFTING, Sex.MALE, null, ScoreMode.ABSOLUTE)!!
        assertEquals(50.0, abs.percentile, 1e-4)
        val dots = StrengthStandards.place(StandardLift.DEADLIFT, 230.0, Population.POWERLIFTING, Sex.MALE, n.refBodyweightKg, ScoreMode.DOTS)!!
        assertEquals(50.0, dots.percentile, 1e-4)
        assertEquals(Rank.LANDSKNECHT, dots.rank)
        assertEquals(1000, dots.rating)
    }

    @Test
    fun dotsRewardsTheLighterLifterAbsoluteDoesNot() {
        val light = StrengthStandards.place(StandardLift.DEADLIFT, 250.0, Population.STRONGMAN, Sex.MALE, 80.0, ScoreMode.DOTS)!!
        val heavy = StrengthStandards.place(StandardLift.DEADLIFT, 250.0, Population.STRONGMAN, Sex.MALE, 130.0, ScoreMode.DOTS)!!
        assertTrue(light.percentile > heavy.percentile + 10)
        val a1 = StrengthStandards.place(StandardLift.DEADLIFT, 250.0, Population.STRONGMAN, Sex.MALE, 80.0, ScoreMode.ABSOLUTE)!!
        val a2 = StrengthStandards.place(StandardLift.DEADLIFT, 250.0, Population.STRONGMAN, Sex.MALE, 130.0, ScoreMode.ABSOLUTE)!!
        assertEquals(a1.percentile, a2.percentile, 1e-9)
        assertNull(StrengthStandards.place(StandardLift.DEADLIFT, 250.0, Population.STRONGMAN, Sex.MALE, null, ScoreMode.DOTS))
    }

    @Test
    fun nextTierKgLandsExactlyOnTheTierBoundary() {
        for (mode in ScoreMode.entries) {
            val p = StrengthStandards.place(StandardLift.SQUAT, 180.0, Population.STRONGMAN, Sex.MALE, 100.0, mode)!!
            val next = Ranks.next(p.rank)!!
            val q = StrengthStandards.place(StandardLift.SQUAT, p.nextTierKg!! + 1e-6, Population.STRONGMAN, Sex.MALE, 100.0, mode)!!
            assertEquals(next.minPercentile, q.percentile, 1e-3)
            assertEquals(next, q.rank)
        }
    }

    @Test
    fun populationsAreOrderedForTheSameLift() {
        fun pct(pop: Population) = StrengthStandards.place(StandardLift.DEADLIFT, 250.0, pop, Sex.MALE, 100.0, ScoreMode.ABSOLUTE)!!.percentile
        assertTrue(pct(Population.GYM) > pct(Population.POWERLIFTING))
        assertTrue(pct(Population.POWERLIFTING) > pct(Population.STRONGMAN))
    }

    @Test
    fun densityIntegratesToOne() {
        val p = StrengthStandards.place(StandardLift.BENCH, 140.0, Population.GYM, Sex.MALE, 90.0, ScoreMode.DOTS)!!
        val d = StrengthStandards.density(p, 400)
        var area = 0.0
        for (i in 1 until d.size) area += (d[i].first - d[i - 1].first) * (d[i].second + d[i - 1].second) / 2
        assertEquals(1.0, area, 0.02)
    }

    @Test
    fun combinedZRescalesCorrelatedMean() {
        assertEquals(1.0, StrengthStandards.combinedZ(listOf(1.0))!!, 1e-12)
        // k = 4, ρ = 0.7: var = (1 + 3·0.7)/4 = 0.775 → z = 1/√0.775
        assertEquals(1 / kotlin.math.sqrt(0.775), StrengthStandards.combinedZ(List(4) { 1.0 })!!, 1e-12)
        assertNull(StrengthStandards.combinedZ(emptyList()))
    }

    @Test
    fun ranksAndDivisions() {
        assertEquals(Rank.KNAPPE, Ranks.forPercentile(5.0))
        assertEquals(Rank.RITTER, Ranks.forPercentile(60.0))
        assertEquals(Rank.LEGENDE, Ranks.forPercentile(99.9))
        assertEquals(3, Ranks.division(60.5))
        assertEquals(1, Ranks.division(74.5))
        assertTrue(Ranks.tierProgress(70.0) in 0.0..1.0)
    }

    // ---------------- Arena ----------------

    private fun set(id: Long, day: Long, ex: Long, w: Double, r: Int, pr: Boolean = false, repPr: Boolean = false) =
        AnalyticsSet(id, day, ex, w, r, null, (OneRepMax.estimate(w, r) as? OneRmResult.Estimate)?.kg, pr, repPr)

    private val liftOf: (Long) -> StandardLift? = { when (it) { 1L -> StandardLift.SQUAT; 2L -> StandardLift.BENCH; 3L -> StandardLift.DEADLIFT; else -> null } }

    @Test
    fun bestsIncludeHistoricRecordsAndTotal() {
        val sets = listOf(set(1, 1000, 1, 200.0, 1), set(2, 1000, 2, 140.0, 1), set(3, 1000, 3, 250.0, 3))
        val hist = listOf(HistoricRecord(9, 3, 500, 270.0, 1, competition = true))
        val events = Arena.liftEvents(sets, hist, liftOf)
        val b = Arena.bests(events)
        assertEquals(270.0, b.getValue(StandardLift.DEADLIFT).e1rm, 1e-9)
        assertEquals(200.0 + 140.0 + 270.0, b.getValue(StandardLift.TOTAL).e1rm, 1e-9)
        val asOf = Arena.bests(events, asOfDay = 400)
        assertTrue(asOf.isEmpty())
    }

    @Test
    fun levelCurveIsMonotoneAndConsistent() {
        var last = 0
        for (xp in listOf(0L, 10L, 149L, 150L, 1000L, 10_000L, 123_456L)) {
            val s = Arena.level(xp)
            assertTrue(s.level >= last); last = s.level
            assertTrue(Arena.xpForLevel(s.level) <= xp && xp < Arena.xpForLevel(s.level + 1))
            assertTrue(s.progress in 0.0..1.0)
        }
        assertEquals(1, Arena.level(0).level)
    }

    @Test
    fun weekStreakCountsConsecutiveWeeks() {
        val monday = WeekMath.mondayOfWeek(3000)
        val days = listOf(monday, monday - 7, monday - 13, monday - 28)
        assertEquals(3, Arena.weekStreak(days, monday + 2))
        assertEquals(3, Arena.weekStreak(days, monday + 9)) // current week empty: streak still alive
        assertEquals(0, Arena.weekStreak(days, monday + 16))
        assertEquals(3, Arena.longestWeekStreak(days))
    }

    @Test
    fun questsTrackTheWeek() {
        val monday = WeekMath.mondayOfWeek(3000)
        val sets = listOf(set(1, monday, 1, 100.0, 5), set(2, monday + 2, 1, 100.0, 5, repPr = true), set(3, monday + 4, 7, 120.0, 2))
        val q = Arena.quests(sets, monday + 4) { it == 7L }.associateBy { it.id }
        assertTrue(q.getValue("days").done)
        assertTrue(q.getValue("pr").done)
        assertFalse(q.getValue("event").done)
        assertEquals(1.0, q.getValue("event").current, 1e-9)
    }

    @Test
    fun plateBadgeCarriesTheDayOfFirstSuccess() {
        val sets = listOf(set(1, 10, 3, 140.0, 1), set(2, 20, 3, 180.0, 1), set(3, 30, 3, 175.0, 1))
        val events = Arena.liftEvents(sets, emptyList(), liftOf)
        val badges = Arena.badges(sets, events, 90.0, { null }, null).associateBy { it.id }
        assertEquals(20L, badges.getValue("plate_DEADLIFT_4").achievedDay)
        assertNull(badges.getValue("plate_DEADLIFT_5").achievedDay)
        assertEquals(180.0 / 220.0, badges.getValue("plate_DEADLIFT_5").progress, 1e-9)
        assertEquals(20L, badges.getValue("bw_DEADLIFT_2.0").achievedDay)
    }

    @Test
    fun bossesIncludePastSelfAndGatekeeper() {
        val sets = listOf(set(1, 100, 3, 200.0, 1), set(2, 600, 3, 230.0, 1))
        val events = Arena.liftEvents(sets, emptyList(), liftOf)
        val pl = StrengthStandards.place(StandardLift.DEADLIFT, 230.0, Population.STRONGMAN, Sex.MALE, 100.0, ScoreMode.DOTS)!!
        val bosses = Arena.bosses(events, 620, mapOf(StandardLift.DEADLIFT to pl))
        val past = bosses.first { it.kind == BossKind.PAST_SELF }
        assertEquals(200.0, past.targetKg, 1e-9)
        assertTrue(past.defeated)
        assertTrue(bosses.any { it.kind == BossKind.GATEKEEPER && !it.defeated })
    }

    // ---------------- Strength curve ----------------

    @Test
    fun saturatingDataRecoversThePlateau() {
        // y = 250 − 100 e^{−0.8 t}, monthly points over 5 years
        val pts = (0 until 60).map { i ->
            val day = 10_000L + i * 30L
            val t = (day - 10_000L) / 365.25
            PerfPoint(day, 250 - 100 * exp(-0.8 * t), 0.0, 1, false)
        }
        val fit = StrengthCurve.fit(pts)!!
        assertEquals(CurveModel.SATURATING, fit.best.model)
        assertEquals(250.0, fit.plateau!!, 3.0)
        assertNull(fit.dayReaching(260.0))
        assertNotNull(fit.dayReaching(245.0))
        assertEquals(1.0, fit.models.sumOf { it.weight }, 1e-9)
    }

    @Test
    fun linearDataPredictsLinearly() {
        val pts = (0 until 12).map { i -> PerfPoint(20_000L + i * 30L, 150.0 + i * 2.0 + (if (i % 2 == 0) 0.3 else -0.3), 0.0, 1, false) }
        val fit = StrengthCurve.fit(pts)!!
        val last = fit.lastDay.toDouble()
        assertEquals(2.0, fit.ratePerMonth(last), 0.3)
        val d = fit.dayReaching(180.0)!!
        assertTrue(d > fit.lastDay)
        assertTrue(fit.halfWidth(last + 365) > fit.halfWidth(last))
    }

    @Test
    fun binningAndEnvelope() {
        val pts = listOf(PerfPoint(0, 100.0, 100.0, 1, false), PerfPoint(5, 110.0, 110.0, 1, false), PerfPoint(40, 105.0, 105.0, 1, false), PerfPoint(70, 120.0, 120.0, 1, true))
        assertEquals(listOf(110.0, 105.0, 120.0), StrengthCurve.binMaxima(pts).map { it.e1rm })
        assertEquals(listOf(100.0, 110.0, 120.0), StrengthCurve.envelope(pts).map { it.e1rm })
        assertNull(StrengthCurve.fit(pts))
    }

    @Test
    fun historicCodecRoundTrip() {
        val r = HistoricRecord(42, 3, 17_000, 262.5, 2, true, "OSG Qualifier | Hamburg")
        val back = HistoricCodec.decode(HistoricCodec.key(42), HistoricCodec.encode(r))
        assertEquals(r, back)
        assertNull(HistoricCodec.decode("strongman.hist.x", "v1|1|2|3|4|0|"))
        assertNull(HistoricCodec.decode(HistoricCodec.key(1), "v2|1|2|3|4|0|"))
    }

    // ---------------- Session report ----------------

    @Test
    fun sessionReportComparesWithThePast() {
        val all = listOf(set(1, 100, 3, 200.0, 1), set(2, 107, 3, 210.0, 1, pr = true))
        val today = listOf(all[1])
        val r = SessionReports.build(107, today, all)
        val ex = r.exercises.single()
        assertEquals(200.0, ex.previousBestE1rm!!, 1e-9)
        assertEquals(5.0, ex.deltaPercent!!, 1e-9)
        assertEquals(1, r.e1rmPrs)
        assertEquals(listOf(100L to 200.0, 107L to 210.0), ex.e1rmHistory)
        assertEquals(203.3333, all[0].e1rm!!, 1e-3) // stored estimate is not used for true singles
        assertTrue(r.verdict.startsWith("Rekordtag"))
        // INOL: 1 rep at 105 % of the previous best → 1 / max(1, −5) = 1
        assertEquals(1.0, ex.inol!!, 1e-9)
    }

    // ---------------- Coach ----------------

    @Test
    fun readinessScalesLoads() {
        val great = Coach.readiness(ReadinessInput(9.0, 5, 1, 1, 5))
        val poor = Coach.readiness(ReadinessInput(4.0, 1, 5, 5, 1))
        assertEquals(100, great.score)
        assertEquals(0, poor.score)
        assertTrue(great.loadFactor > 1.0 && poor.loadFactor < 0.9)
        val spiked = Coach.readiness(ReadinessInput(9.0, 5, 1, 1, 5, acwr = 1.8))
        assertEquals(80, spiked.score)
        val plans = Coach.topSets(200.0, FTable(), 2.5, poor)
        assertTrue(plans.all { it.rpe <= 7.0 })
    }

    @Test
    fun attemptsAndPhases() {
        val a = Coach.attempts(250.0)
        assertEquals(227.5, a.opener, 1e-9)
        assertEquals(240.0, a.second, 1e-9)
        assertEquals(252.5, a.third, 1e-9)
        assertEquals(MeetPhase.TAPER, Coach.phaseFor(10))
        assertEquals(MeetPhase.MEET_WEEK, Coach.phaseFor(3))
        assertEquals(MeetPhase.OFFSEASON, Coach.phaseFor(200))
        val plan = Coach.meetPlan(1000, 1050)
        assertEquals(MeetPhase.INTENSIFICATION, plan.phase)
        assertEquals(15L, plan.nextPhaseInDays)
    }

    @Test
    fun banisterPeakFollowsClosedForm() {
        // τ₁ = 42, τ₂ = 7, k₂/k₁ = 2: t* = 42·7·ln(12)/35 ≈ 20.87 d
        assertEquals(20.873, Coach.banisterPeakDays(), 1e-3)
    }

    @Test
    fun weakPointsAndRatios() {
        val places = listOf(
            StrengthStandards.place(StandardLift.SQUAT, 260.0, Population.STRONGMAN, Sex.MALE, 110.0, ScoreMode.DOTS)!!,
            StrengthStandards.place(StandardLift.DEADLIFT, 320.0, Population.STRONGMAN, Sex.MALE, 110.0, ScoreMode.DOTS)!!,
            StrengthStandards.place(StandardLift.LOG, 100.0, Population.STRONGMAN, Sex.MALE, 110.0, ScoreMode.DOTS)!!
        )
        assertEquals(StandardLift.LOG, Coach.weakPoints(places).first().lift)
        val r = Coach.ratios(mapOf(StandardLift.SQUAT to 230.0, StandardLift.DEADLIFT to 280.0), Sex.MALE).single()
        assertEquals(0.0, r.deviation, 1e-12)
    }

    @Test
    fun weightClassVerdict() {
        val ok = Coach.weightClass(104.0, 105.0, 0, 70)
        assertEquals(0.0, ok.kgPerWeek, 1e-12)
        val fast = Coach.weightClass(112.0, 105.0, 0, 28)
        assertTrue(fast.percentPerWeek > 1.0)
    }

    // ---------------- Systems ----------------

    @Test
    fun wendlerUsesTrainingMax() {
        val w = TrainingSystems.generate(TrainingSystem.WENDLER_531, 200.0, 2.5, true)
        assertEquals(4, w.size)
        val wk3 = w[2].days.single().sets
        // TM = 180; 95 % TM = 171 → 170
        assertEquals(170.0, wk3.last().kg, 1e-9)
        assertTrue(wk3.last().amrap)
        assertFalse(w[3].days.single().sets.any { it.amrap })
    }

    @Test
    fun smolovJrAddsLoadEachWeek() {
        val w = TrainingSystems.generate(TrainingSystem.SMOLOV_JR, 200.0, 2.5, true)
        assertEquals(140.0, w[0].days[0].sets[0].kg, 1e-9)
        assertEquals(145.0, w[1].days[0].sets[0].kg, 1e-9)
        assertEquals(150.0, w[2].days[0].sets[0].kg, 1e-9)
        assertEquals(6, w[0].days[0].sets.size)
        assertEquals(10, w[0].days[3].sets.size)
        val prog = TrainingSystems.toProgramWeeks(w, 77)
        assertEquals(4, prog[0].days.size)
        assertEquals(77L, prog[0].days[0].exercises.single().exerciseId)
    }

    @Test
    fun rpeBlockTopSetsFollowTheTable() {
        val w = TrainingSystems.generate(TrainingSystem.RPE_BLOCK, 200.0, 2.5, true)
        assertEquals(FTable().prescribe(200.0, 3, 9.0, 2.5), w[2].days[0].sets[0].kg, 1e-9)
        assertEquals(4, w[0].days[0].sets.size)
        assertEquals(1, w[3].days[0].sets.size)
    }
}

package app.maximus.strongman.domain

import kotlin.math.ceil
import kotlin.math.ln

/** Subjective readiness questionnaire (each 1..5; stress and soreness: 5 = high). */
data class ReadinessInput(
    val sleepHours: Double,
    val sleepQuality: Int,
    val stress: Int,
    val soreness: Int,
    val motivation: Int,
    /** Acute:chronic workload ratio from [TrainingLoad], if known. */
    val acwr: Double? = null
)

data class Readiness(
    val score: Int,
    /** Multiplier for today's planned loads (e.g. 0.95 = −5 %). */
    val loadFactor: Double,
    /** Multiplier for planned back-off volume. */
    val volumeFactor: Double,
    val maxRpe: Double,
    val title: String,
    val advice: String
)

data class TopSetPlan(val reps: Int, val rpe: Double, val kg: Double)

data class WeakPoint(val lift: StandardLift, val z: Double, val gap: Double, val advice: String)

data class RatioCheck(val title: String, val actual: Double, val reference: Double) {
    val deviation: Double get() = actual / reference - 1
}

enum class MeetPhase(val title: String, val intensity: String, val volume: String, val focus: String) {
    OFFSEASON("Off-Season · Basis", "65–75 % 1RM, RPE 6–8", "hoch: 12–20 harte Sätze je Muster/Woche",
        "Muskelaufbau, Arbeitskapazität, Schwachstellen. Events technisch und leicht, Griffkraft und Rumpf separat."),
    ACCUMULATION("Akkumulation", "70–80 % 1RM, RPE 7–8", "mittel–hoch: 10–15 harte Sätze",
        "Grundkraft in den Wettkampf-Mustern. Events 1× pro Woche mit 70–80 % der Wettkampflast."),
    INTENSIFICATION("Intensivierung", "80–90 % 1RM, RPE 8–9", "mittel: 6–10 harte Sätze",
        "Wettkampfgeräte, 2 Event-Tage pro Woche mit 85–95 %, Übergänge (Clean, Laden) unter Ermüdung üben."),
    REALIZATION("Realisierung · Peaking", "88–100 %, Singles @ RPE 8–9", "niedrig: 3–6 harte Sätze",
        "Wettkampfsimulation mit Originallasten. Letzter schwerer Event-Durchlauf 10–14 Tage vor dem Wettkampf."),
    TAPER("Taper", "Intensität halten (≈ 85–90 %), keine Rekordversuche", "Volumen −40 bis −60 %",
        "Ermüdung abbauen, Fitness erhalten (Bosquet 2007). Letzte schwere Einheit 5–7 Tage vorher, dann nur Openers."),
    MEET_WEEK("Wettkampfwoche", "Openers bei ≤ 90 %", "minimal",
        "Schlaf, Essen, Gewichtsklasse, Ausrüstung. Zwei leichte Einheiten, 48–72 h vorher Ruhe."),
    POST("Nach dem Wettkampf", "60–75 %", "niedrig, Spaß-Übungen",
        "1–2 Wochen aktive Erholung, dann neuen Block planen und Schwachstellen aus dem Wettkampf auswerten.")
}

data class MeetPlan(
    val daysOut: Long,
    val weeksOut: Int,
    val phase: MeetPhase,
    val nextPhaseInDays: Long?,
    val checklist: List<String>
)

data class Attempts(val opener: Double, val second: Double, val third: Double, val conservativeThird: Double)

data class WeightClassCheck(val currentKg: Double, val limitKg: Double, val weeksLeft: Double, val kgPerWeek: Double, val percentPerWeek: Double, val verdict: String)

/**
 * The pocket coach. Rules are drawn from the sport-science literature on autoregulation (Helms 2016;
 * Mann 2010), tapering (Bosquet 2007; Pritchard 2015), block periodization (Issurin 2010) and from
 * established strength-sport practice; every number is a starting point, not a law.
 */
object Coach {

    /**
     * Readiness 0–100: weighted sum of normalized items (sleep duration 25 %, quality 20 %, stress 15 %,
     * soreness 20 %, motivation 20 %), minus a penalty when the ACWR leaves the 0.8–1.3 band.
     */
    fun readiness(i: ReadinessInput): Readiness {
        fun pos(v: Int) = ((v.coerceIn(1, 5) - 1) / 4.0)
        fun neg(v: Int) = 1 - pos(v)
        val sleep = ((i.sleepHours - 4.0) / 4.5).coerceIn(0.0, 1.0)
        var s = 100 * (0.25 * sleep + 0.20 * pos(i.sleepQuality) + 0.15 * neg(i.stress) + 0.20 * neg(i.soreness) + 0.20 * pos(i.motivation))
        val a = i.acwr
        if (a != null && a > 1.3) s -= ((a - 1.3) * 40).coerceAtMost(20.0)
        val score = s.toInt().coerceIn(0, 100)
        return when {
            score >= 80 -> Readiness(score, 1.025, 1.0, 9.5, "Bereit für Großes",
                "Top-Tag: Plane wie vorgesehen; ein Rekordversuch ist erlaubt, wenn die Aufwärmsätze schnell laufen.")
            score >= 62 -> Readiness(score, 1.0, 1.0, 9.0, "Normal",
                "Trainiere nach Plan. Top-Satz nach RPE, nicht nach Ego.")
            score >= 45 -> Readiness(score, 0.95, 0.7, 8.0, "Gedrosselt",
                "Lasten −5 %, Back-off-Volumen −30 %. Technik sauber halten, nichts erzwingen.")
            else -> Readiness(score, 0.85, 0.5, 7.0, "Erholung",
                "Technik- und Erholungstag: ≤ RPE 7, kurze Einheit, Fokus auf Mobilität, Spaziergang, Schlaf.")
        }
    }

    /** Top sets for today from an e1RM through the RPE table, scaled by the readiness load factor. */
    fun topSets(e1rm: Double, table: FTable, increment: Double, readiness: Readiness?): List<TopSetPlan> {
        val f = readiness?.loadFactor ?: 1.0
        val cap = readiness?.maxRpe ?: 9.0
        return listOf(1 to 8.0, 1 to 9.0, 3 to 8.0, 5 to 8.0, 8 to 7.0).filter { it.second <= cap }
            .map { (r, rpe) -> TopSetPlan(r, rpe, table.prescribe(e1rm * f, r, rpe, increment)) }
    }

    /**
     * Weak points: lifts whose population z-score lies at least 0.35 SD below the athlete's own mean
     * z (relative imbalance, independent of the overall level).
     */
    fun weakPoints(placements: List<Placement>): List<WeakPoint> {
        val singles = placements.filter { it.lift != StandardLift.TOTAL }
        if (singles.size < 2) return emptyList()
        val mean = singles.map { it.z }.average()
        return singles.filter { it.z - mean <= -0.35 }.sortedBy { it.z }
            .map { WeakPoint(it.lift, it.z, it.z - mean, adviceFor(it.lift)) }
    }

    /** Classic structural-balance ratios against the medians of the strongman population. */
    fun ratios(bests: Map<StandardLift, Double>, sex: Sex): List<RatioCheck> {
        val n = StrengthStandards.norm(Population.STRONGMAN, sex).lifts
        fun ref(a: StandardLift, b: StandardLift) = n.getValue(a).medianKg / n.getValue(b).medianKg
        val pairs = listOf(
            Triple("Kniebeuge / Kreuzheben", StandardLift.SQUAT, StandardLift.DEADLIFT),
            Triple("Bankdrücken / Kniebeuge", StandardLift.BENCH, StandardLift.SQUAT),
            Triple("Strict Press / Bankdrücken", StandardLift.STRICT_PRESS, StandardLift.BENCH),
            Triple("Log / Strict Press", StandardLift.LOG, StandardLift.STRICT_PRESS),
            Triple("Axle / Log", StandardLift.AXLE, StandardLift.LOG),
            Triple("Axle-KH / Kreuzheben", StandardLift.AXLE_DEADLIFT, StandardLift.DEADLIFT)
        )
        return pairs.mapNotNull { (t, a, b) ->
            val x = bests[a]; val y = bests[b]
            if (x != null && y != null && y > 0) RatioCheck(t, x / y, ref(a, b)) else null
        }
    }

    fun adviceFor(lift: StandardLift): String = when (lift) {
        StandardLift.SQUAT -> "Pause-Kniebeugen, Front- oder SSB-Kniebeugen 3–5 × 3–6; Quadrizeps-Volumen (Hack/Beinpresse); Rumpfsteifigkeit (Bracing, Belt Squat)."
        StandardLift.BENCH -> "Close-Grip und Larsen Press, Trizeps (JM Press, Dips), oberer Rücken für Stabilität; Pausen-Bankdrücken für Startkraft."
        StandardLift.DEADLIFT -> "Defizit- oder Paused Deadlifts für die Startkraft, Block Pulls für den Lockout, RDL und Good Mornings für die Hüftstrecker; Griff separat."
        StandardLift.STRICT_PRESS -> "Z-Press, Pin Press, Seitheben, Trizeps; Overhead-Volumen 2×/Woche – Strict-Kraft ist das Fundament jedes Log-Presses."
        StandardLift.LOG -> "Log Clean separat (Laps, Rumpf), Log Push Press und Strict für den Lockout; Clean-Wiederholungen mit 60–70 % für die Technik."
        StandardLift.AXLE -> "Continental-Clean-Technik (Gürtel-Pause), Axle Push Press, Griffkraft mit dicker Stange; Clean für Clean üben, nicht nur pressen."
        StandardLift.AXLE_DEADLIFT -> "Doppelt-Überhand-Griff mit Haken/Ziehhilfen nur dosiert, Thick-Bar-Holds, Rack Pulls von Kniehöhe mit Axle."
        StandardLift.STONE -> "Stein-Laps vom Boden, Rundrücken-Kreuzheben (Jefferson, Sandbag), Bizeps und Unterarme; Steinserien für die Ausdauer."
        StandardLift.TOTAL -> "Die Summe folgt den Einzeldisziplinen."
    }

    // ---------------- Meet preparation (e.g. an OSG contest) ----------------

    fun phaseFor(daysOut: Long): MeetPhase = when {
        daysOut < 0 -> MeetPhase.POST
        daysOut <= 6 -> MeetPhase.MEET_WEEK
        daysOut <= 13 -> MeetPhase.TAPER
        daysOut <= 35 -> MeetPhase.REALIZATION
        daysOut <= 70 -> MeetPhase.INTENSIFICATION
        daysOut <= 112 -> MeetPhase.ACCUMULATION
        else -> MeetPhase.OFFSEASON
    }

    private val phaseStarts = listOf(112L, 70L, 35L, 13L, 6L, -1L)

    fun meetPlan(today: Long, meetDay: Long): MeetPlan {
        val d = meetDay - today
        val next = phaseStarts.firstOrNull { d > it }?.let { d - it }
        val phase = phaseFor(d)
        val list = when (phase) {
            MeetPhase.OFFSEASON, MeetPhase.ACCUMULATION -> listOf(
                "Ausschreibung lesen: Disziplinen, Gewichte, Zeitlimits, Gerätemaße (Log-Durchmesser, Steinhöhe).",
                "Fehlende Geräte organisieren oder Ersatz planen (Sandsack statt Stein, Trap Bar statt Farmers).",
                "Gewichtsklasse festlegen; mehr als 0,5–1 % Körpergewicht pro Woche zu verändern kostet Kraft.")
            MeetPhase.INTENSIFICATION -> listOf(
                "Jede Wettkampfdisziplin mindestens alle 2 Wochen trainieren.",
                "Wettkampfregeln üben: Startsignal, Down-Signal, Lockout halten.",
                "Pacing für Wiederholungs- und Zeit-Events festlegen.")
            MeetPhase.REALIZATION -> listOf(
                "Generalprobe: kompletter Wettkampftag in Reihenfolge 2–3 Wochen vorher.",
                "Openers festlegen (siehe Versuchsplanung) und mehrfach sicher bewältigen.",
                "Keine neuen Übungen, keine neuen Schuhe oder Gürtel.")
            MeetPhase.TAPER -> listOf(
                "Volumen um 40–60 % senken, Intensität halten.",
                "Schlaf ≥ 8 h, Kohlenhydrate in den letzten 2–3 Tagen erhöhen.",
                "Tasche packen: Gürtel, Kreide, Tacky, Handgelenkbandagen, Ärmel, Essen, Wasser.")
            MeetPhase.MEET_WEEK -> listOf(
                "Wiegen, Regelbesprechung und Gerätetest nicht verpassen.",
                "Zwischen den Events essen und trinken (schnelle Kohlenhydrate, Salz).",
                "Nur Versuche wählen, die du schon im Training sicher geschafft hast – bis auf den dritten.")
            MeetPhase.POST -> listOf(
                "Ergebnisse und Videos auswerten; Zeitverluste bei Übergängen notieren.",
                "Neue Rekorde im Rekordbuch eintragen.",
                "Nächsten Wettkampf wählen und rückwärts planen.")
        }
        return MeetPlan(d, ceil(d / 7.0).toInt(), phase, next, list)
    }

    /**
     * Attempt selection for max-load events or the powerlifts, from the current e1RM:
     * opener ≈ 90–92 % (a weight you can hit on your worst day), second ≈ 96 %, third ≈ 100–102 %.
     * The conservative third (≈ 98 %) is the choice when placing matters more than a record.
     */
    fun attempts(e1rm: Double, increment: Double = 2.5): Attempts {
        fun r(x: Double) = roundToIncrement(x, increment)
        return Attempts(r(0.91 * e1rm), r(0.96 * e1rm), r(1.01 * e1rm), r(0.985 * e1rm))
    }

    /** Weight-class change needed until the weigh-in; natural athletes lose strength above ≈ 1 % BW/week. */
    fun weightClass(currentKg: Double, limitKg: Double, today: Long, meetDay: Long): WeightClassCheck {
        val weeks = ((meetDay - today) / 7.0).coerceAtLeast(0.1)
        val delta = currentKg - limitKg
        val perWeek = if (delta > 0) delta / weeks else 0.0
        val pct = 100 * perWeek / currentKg
        val verdict = when {
            delta <= 0 -> "Im Limit (${"%.1f".format(-delta)} kg Puffer). Kein Gewichtmachen nötig."
            pct <= 0.5 -> "Gut machbar: ${"%.2f".format(perWeek)} kg/Woche über leichtes Defizit (≈ 300–500 kcal/Tag)."
            pct <= 1.0 -> "Machbar, aber spürbar: Protein ≥ 2 g/kg, Volumen halten, Defizit moderat."
            else -> "Zu schnell (> 1 %/Woche): Kraftverlust wahrscheinlich. Höhere Klasse erwägen oder nur die letzten 1–2 % per Wasser-Manipulation mit erfahrener Betreuung."
        }
        return WeightClassCheck(currentKg, limitKg, weeks, perWeek, pct, verdict)
    }

    /**
     * Banister impulse–response view of a taper: performance p(t) = p₀ + k₁·fitness − k₂·fatigue with
     * fitness and fatigue decaying with τ₁ = 42 d and τ₂ = 7 d. After a load stop the net effect
     * k₁e^{−t/τ₁} − k₂e^{−t/τ₂} peaks at t* = τ₁τ₂ ln(k₂τ₁/(k₁τ₂)) / (τ₁ − τ₂) — the classic argument why
     * the last hard session belongs roughly one to two weeks before the contest.
     */
    fun banisterPeakDays(k1: Double = 1.0, k2: Double = 2.0, tau1: Double = 42.0, tau2: Double = 7.0): Double {
        require(tau1 > tau2 && k1 > 0 && k2 > 0)
        val arg = k2 * tau1 / (k1 * tau2)
        return if (arg <= 1) 0.0 else tau1 * tau2 * ln(arg) / (tau1 - tau2)
    }
}

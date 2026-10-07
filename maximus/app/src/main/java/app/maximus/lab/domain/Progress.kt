package app.maximus.lab.domain

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

data class TopicStat(val answered: Int = 0, val correct: Int = 0, val mastery: Double = 0.0)

/** Leitner box (0…5) and the epoch day on which the card is due again. */
data class CardState(val box: Int = 0, val dueDay: Long = 0)

data class LabProgress(
    val xp: Long = 0,
    val answered: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDay: Long = Long.MIN_VALUE,
    val lastDailyDay: Long = Long.MIN_VALUE,
    val dailyCount: Int = 0,
    val perfectSessions: Int = 0,
    val reviews: Int = 0,
    val readChapters: Set<String> = emptySet(),
    val usedCalculators: Set<String> = emptySet(),
    val badges: Set<String> = emptySet(),
    val topics: Map<Topic, TopicStat> = emptyMap(),
    val cards: Map<String, CardState> = emptyMap(),
    /** Best score 0…100 per AI game (key = AiGame name). */
    val games: Map<String, Int> = emptyMap()
) {
    fun topic(t: Topic) = topics[t] ?: TopicStat()
}

data class Badge(val key: String, val title: String, val description: String, val condition: (LabProgress) -> Boolean)

/** Something worth celebrating in the UI after an action. */
sealed interface LabEvent {
    data class Xp(val amount: Int, val reason: String) : LabEvent
    data class LevelUp(val level: Int, val title: String) : LabEvent
    data class BadgeUnlocked(val badge: Badge) : LabEvent
    data class StreakExtended(val days: Int) : LabEvent
}

/**
 * Gamification rules. Everything is a pure function of the previous [LabProgress], so it is testable
 * and the repository only has to persist the result.
 *
 * Level curve: reaching level L needs X(L) = 50·L·(L − 1) XP in total, i.e. 100, 300, 600, 1000 … —
 * each level costs 100 XP more than the previous one. Inverse: L = ⌊(1 + √(1 + 0,08·XP))/2⌋.
 * XP per answer: base (10 choice, 20 calculation) × difficulty × streak bonus (1 + 0,1·min(streak, 10)).
 * Topic mastery is an exponential moving average m ← m + α(c − m) with α = 0,1 + 0,05(difficulty − 1).
 */
object LabRules {
    fun xpForLevel(level: Int): Long = 50L * level * (level - 1)

    fun level(xp: Long): Int = floor((1 + sqrt(1 + 0.08 * xp)) / 2).toInt().coerceAtLeast(1)

    /** Fraction of the way from the current to the next level. */
    fun levelProgress(xp: Long): Double {
        val l = level(xp)
        val lo = xpForLevel(l); val hi = xpForLevel(l + 1)
        return ((xp - lo).toDouble() / (hi - lo)).coerceIn(0.0, 1.0)
    }

    private val TITLES = listOf(
        1 to "Lehrling", 3 to "Knappe der Formeln", 5 to "Experimentator", 8 to "Ritter der Analysis",
        12 to "Hüter der Hauptsätze", 16 to "Meister der Felder", 20 to "Quantenritter", 25 to "Großmeister der Physik",
        30 to "Laplacescher Dämon", 40 to "Maxwells Dämon"
    )

    fun title(level: Int): String = TITLES.last { it.first <= level }.second

    fun streakMultiplier(streak: Int) = 1 + 0.1 * min(streak, 10)

    val LEITNER_DAYS = intArrayOf(0, 1, 2, 4, 8, 16)

    private val TOPIC_BADGE = mapOf(
        Topic.THERMO to ("Carnots Erbe" to "Thermodynamik zu 80 % gemeistert"),
        Topic.ELECTRO to ("Maxwells Schüler" to "Elektrodynamik zu 80 % gemeistert"),
        Topic.QUANTUM to ("Schrödingers Katze" to "Quantenphysik zu 80 % gemeistert — lebendig und tot zugleich"),
        Topic.SEMICONDUCTOR to ("Shockleys Erbe" to "Halbleiterphysik zu 80 % gemeistert"),
        Topic.CALORIC to ("Kühlkünstler" to "Kalorik zu 80 % gemeistert"),
        Topic.QFT to ("Feynmans Diagramm" to "Quantenfeldtheorie zu 80 % gemeistert"),
        Topic.POLYMER to ("Flory-Schüler" to "Polymerphysik zu 80 % gemeistert"),
        Topic.ELECTRICAL to ("Schaltkreis-Schmied" to "Elektrotechnik und Chips zu 80 % gemeistert"),
        Topic.AI to ("Gradientenreiter" to "AI Engineering zu 80 % gemeistert"),
        Topic.MECHANICS to ("Newtons Apfel" to "Mechanik und Relativität zu 80 % gemeistert"),
        Topic.MATH to ("Gauß' Zögling" to "Mathematik zu 80 % gemeistert")
    )

    val BADGES: List<Badge> = listOf(
        Badge("first", "Erster Schritt", "Die erste Aufgabe beantwortet") { it.answered >= 1 },
        Badge("c10", "Fleißig", "10 richtige Antworten") { it.correct >= 10 },
        Badge("c50", "Unermüdlich", "50 richtige Antworten") { it.correct >= 50 },
        Badge("c100", "Centurio", "100 richtige Antworten") { it.correct >= 100 },
        Badge("c250", "Rechenwerk", "250 richtige Antworten") { it.correct >= 250 },
        Badge("c500", "Avogadros Geduld", "500 richtige Antworten") { it.correct >= 500 },
        Badge("s3", "Dranbleiben", "3 Tage in Folge aktiv") { it.bestStreak >= 3 },
        Badge("s7", "Eine Woche Wissenschaft", "7 Tage in Folge aktiv") { it.bestStreak >= 7 },
        Badge("s30", "Halbwertszeit unendlich", "30 Tage in Folge aktiv") { it.bestStreak >= 30 },
        Badge("perfect", "Fehlerfrei", "Eine Trainingsrunde ohne Fehler") { it.perfectSessions >= 1 },
        Badge("perfect5", "Präzisionsmessung", "Fünf fehlerfreie Runden") { it.perfectSessions >= 5 },
        Badge("daily1", "Tagesaufgabe", "Die erste Tageschallenge geschafft") { it.dailyCount >= 1 },
        Badge("daily10", "Täglich grüßt das Labor", "10 Tageschallenges") { it.dailyCount >= 10 },
        Badge("read5", "Leseratte", "5 Kapitel des Kompendiums gelesen") { it.readChapters.size >= 5 },
        Badge("read20", "Bibliothekar", "20 Kapitel gelesen") { it.readChapters.size >= 20 },
        Badge("readAll", "Enzyklopädist", "Das ganze Kompendium gelesen") { p -> Compendium.all.all { it.key in p.readChapters } },
        Badge("calc10", "Laborratte", "10 verschiedene Rechner benutzt") { it.usedCalculators.size >= 10 },
        Badge("calcAll", "Instrumentenkunde", "Jeden Rechner benutzt") { p -> Calculators.all.all { it.key in p.usedCalculators } },
        Badge("cards50", "Karteikasten", "50 Karteikarten wiederholt") { it.reviews >= 50 },
        Badge("cards20m", "Langzeitgedächtnis", "20 Karteikarten im höchsten Fach") { p -> p.cards.values.count { it.box >= 5 } >= 20 },
        Badge("lvl10", "Zweistellig", "Stufe 10 erreicht") { level(it.xp) >= 10 },
        Badge("lvl20", "Quantensprung", "Stufe 20 erreicht") { level(it.xp) >= 20 },
        Badge("aiPlay", "Spielkind", "Ein Spiel der AI-Spielwiese gespielt") { it.games.isNotEmpty() },
        Badge("aiPlay5", "Neugieriges Netz", "Fünf verschiedene AI-Spiele gespielt") { it.games.size >= 5 },
        Badge("aiAll80", "Großmeister der Spielwiese", "In jedem AI-Spiel mindestens 80 Punkte") { p -> AiGame.entries.all { (p.games[it.name] ?: 0) >= 80 } },
        Badge("aiPath10", "Zehn Happen", "Zehn Kapitel des AI-Lernpfads gelesen") { p -> AiCurriculum.keys.count { it in p.readChapters } >= 10 },
        Badge("aiPath30", "Halbzeit im AI-Kurs", "Dreißig Happen des AI-Lernpfads gelesen") { p -> AiCurriculum.keys.count { it in p.readChapters } >= 30 },
        Badge("aiPathAll", "AI Engineer", "Den ganzen AI-Lernpfad gelesen") { p -> AiCurriculum.keys.all { it in p.readChapters } },
        Badge("allround", "Universalgelehrter", "In jedem Gebiet mindestens 5 richtige Antworten") { p -> Topic.entries.all { p.topic(it).correct >= 5 } }
    ) + TOPIC_BADGE.map { (t, b) -> Badge("m_${t.name}", b.first, b.second) { it.topic(t).mastery >= 0.8 && it.topic(t).answered >= 10 } }

    val badgeByKey: Map<String, Badge> by lazy { BADGES.associateBy { it.key } }

    // ---------------------------------------------------------------- transitions

    private fun touchDay(p: LabProgress, today: Long, events: MutableList<LabEvent>): LabProgress {
        if (p.lastActiveDay == today) return p
        val streak = if (p.lastActiveDay == today - 1) p.streak + 1 else 1
        if (streak > 1) events += LabEvent.StreakExtended(streak)
        return p.copy(streak = streak, bestStreak = maxOf(p.bestStreak, streak), lastActiveDay = today)
    }

    private fun gain(p: LabProgress, amount: Int, reason: String, events: MutableList<LabEvent>): LabProgress {
        if (amount <= 0) return p
        val before = level(p.xp)
        val next = p.copy(xp = p.xp + amount)
        events += LabEvent.Xp(amount, reason)
        val after = level(next.xp)
        if (after > before) events += LabEvent.LevelUp(after, title(after))
        return next
    }

    private fun badges(p: LabProgress, events: MutableList<LabEvent>): LabProgress {
        val fresh = BADGES.filter { it.key !in p.badges && it.condition(p) }
        if (fresh.isEmpty()) return p
        fresh.forEach { events += LabEvent.BadgeUnlocked(it) }
        return p.copy(badges = p.badges + fresh.map { it.key })
    }

    fun answer(p0: LabProgress, q: Question, correct: Boolean, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        var p = touchDay(p0, today, ev)
        val stat = p.topic(q.topic)
        val alpha = 0.1 + 0.05 * (q.difficulty - 1)
        val mastery = stat.mastery + alpha * ((if (correct) 1.0 else 0.0) - stat.mastery)
        p = p.copy(
            answered = p.answered + 1, correct = p.correct + if (correct) 1 else 0,
            topics = p.topics + (q.topic to TopicStat(stat.answered + 1, stat.correct + if (correct) 1 else 0, mastery))
        )
        val base = if (q.isChoice) 10 else 20
        val xp = if (correct) (base * q.difficulty * streakMultiplier(p.streak)).toInt() else 2
        p = gain(p, xp, if (correct) "Richtig" else "Versuch zählt", ev)
        return badges(p, ev) to ev
    }

    fun sessionFinished(p0: LabProgress, correct: Int, total: Int, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        var p = touchDay(p0, today, ev)
        if (total >= 5 && correct == total) {
            p = p.copy(perfectSessions = p.perfectSessions + 1)
            p = gain(p, 25 * total / 5, "Fehlerfreie Runde", ev)
        }
        return badges(p, ev) to ev
    }

    fun dailyFinished(p0: LabProgress, correct: Int, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        if (p0.lastDailyDay == today) return p0 to ev
        var p = touchDay(p0, today, ev)
        p = p.copy(lastDailyDay = today, dailyCount = p.dailyCount + 1)
        p = gain(p, 30 + 10 * correct, "Tageschallenge", ev)
        return badges(p, ev) to ev
    }

    fun chapterRead(p0: LabProgress, key: String, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        if (key in p0.readChapters) return p0 to ev
        var p = touchDay(p0, today, ev).copy(readChapters = p0.readChapters + key)
        p = gain(p, 15, "Kapitel gelesen", ev)
        return badges(p, ev) to ev
    }

    fun calculatorUsed(p0: LabProgress, key: String, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        if (key in p0.usedCalculators) return p0 to ev
        var p = touchDay(p0, today, ev).copy(usedCalculators = p0.usedCalculators + key)
        p = gain(p, 5, "Neues Instrument", ev)
        return badges(p, ev) to ev
    }

    /**
     * A finished AI game with score 0…100: XP = 5 + score/5 every time, plus 25 for a new personal best
     * (so practice pays, but grinding the same easy game does not explode the XP).
     */
    fun gameFinished(p0: LabProgress, game: AiGame, score: Int, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        val s = score.coerceIn(0, 100)
        var p = touchDay(p0, today, ev)
        val old = p.games[game.name]
        p = p.copy(games = p.games + (game.name to maxOf(old ?: 0, s)))
        p = gain(p, 5 + s / 5, game.title, ev)
        if (old == null || s > old) p = gain(p, 25, "Neuer Bestwert", ev)
        return badges(p, ev) to ev
    }

    /** Leitner step: known → next box (max 5), due after LEITNER_DAYS[box]; unknown → box 1, due tomorrow. */
    fun reviewCard(p0: LabProgress, id: String, knew: Boolean, today: Long): Pair<LabProgress, List<LabEvent>> {
        val ev = ArrayList<LabEvent>()
        var p = touchDay(p0, today, ev)
        val old = p.cards[id] ?: CardState()
        val box = if (knew) min(5, old.box + 1) else 1
        val due = today + LEITNER_DAYS[box]
        p = p.copy(cards = p.cards + (id to CardState(box, due)), reviews = p.reviews + 1)
        p = gain(p, if (knew) 3 else 1, "Karteikarte", ev)
        return badges(p, ev) to ev
    }

    /** Cards due today: unseen cards and cards whose due day has come, oldest due first. */
    fun dueCards(p: LabProgress, today: Long, topic: Topic? = null): List<FlashCard> =
        Compendium.flashcards
            .filter { topic == null || it.chapter.topic == topic }
            .filter { (p.cards[it.id]?.dueDay ?: Long.MIN_VALUE) <= today }
            .sortedBy { p.cards[it.id]?.dueDay ?: Long.MIN_VALUE }

    // ---------------------------------------------------------------- persistence

    /** Line-based text encoding ("key=value"); unknown or malformed lines are ignored on decode. */
    fun encode(p: LabProgress): String = buildString {
        appendLine("v=1")
        appendLine("xp=${p.xp}")
        appendLine("answered=${p.answered}")
        appendLine("correct=${p.correct}")
        appendLine("streak=${p.streak}")
        appendLine("best=${p.bestStreak}")
        appendLine("last=${p.lastActiveDay}")
        appendLine("daily=${p.lastDailyDay}")
        appendLine("dailyCount=${p.dailyCount}")
        appendLine("perfect=${p.perfectSessions}")
        appendLine("reviews=${p.reviews}")
        appendLine("read=${p.readChapters.joinToString(",")}")
        appendLine("calcs=${p.usedCalculators.joinToString(",")}")
        appendLine("badges=${p.badges.joinToString(",")}")
        p.topics.forEach { (t, s) -> appendLine("topic.${t.name}=${s.answered},${s.correct},${s.mastery}") }
        p.cards.forEach { (id, c) -> appendLine("card.$id=${c.box},${c.dueDay}") }
        p.games.forEach { (g, s) -> appendLine("game.$g=$s") }
    }

    fun decode(text: String?): LabProgress {
        if (text.isNullOrBlank()) return LabProgress()
        val m = text.lines().mapNotNull { l -> l.indexOf('=').takeIf { it > 0 }?.let { l.substring(0, it) to l.substring(it + 1) } }
        fun long(k: String, d: Long) = m.firstOrNull { it.first == k }?.second?.toLongOrNull() ?: d
        fun int(k: String) = long(k, 0).toInt()
        fun set(k: String) = m.firstOrNull { it.first == k }?.second?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        val topics = m.filter { it.first.startsWith("topic.") }.mapNotNull { (k, v) ->
            val t = Topic.entries.firstOrNull { it.name == k.removePrefix("topic.") } ?: return@mapNotNull null
            val parts = v.split(',')
            t to TopicStat(parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0, parts.getOrNull(2)?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 0.0)
        }.toMap()
        val cards = m.filter { it.first.startsWith("card.") }.mapNotNull { (k, v) ->
            val parts = v.split(',')
            val box = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 5) ?: return@mapNotNull null
            k.removePrefix("card.") to CardState(box, parts.getOrNull(1)?.toLongOrNull() ?: 0)
        }.toMap()
        val games = m.filter { it.first.startsWith("game.") }.mapNotNull { (k, v) ->
            v.toIntOrNull()?.coerceIn(0, 100)?.let { k.removePrefix("game.") to it }
        }.toMap()
        return LabProgress(
            xp = long("xp", 0).coerceAtLeast(0), answered = int("answered"), correct = int("correct"),
            streak = int("streak"), bestStreak = int("best"), lastActiveDay = long("last", Long.MIN_VALUE),
            lastDailyDay = long("daily", Long.MIN_VALUE), dailyCount = int("dailyCount"), perfectSessions = int("perfect"),
            reviews = int("reviews"), readChapters = set("read"), usedCalculators = set("calcs"), badges = set("badges"),
            topics = topics, cards = cards, games = games
        )
    }
}

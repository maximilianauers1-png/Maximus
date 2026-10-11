package app.maximus.ai.domain

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

/** Answers and mastery of one lesson; mastery is an exponential moving average of hits. */
data class LessonStat(val answered: Int = 0, val correct: Int = 0, val mastery: Double = 0.0)

data class AiProgress(
    val xp: Long = 0,
    val answered: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDay: Long = Long.MIN_VALUE,
    val lastDailyDay: Long = Long.MIN_VALUE,
    val dailyCount: Int = 0,
    /** Bite quizzes without a single mistake. */
    val perfectQuizzes: Int = 0,
    val readBites: Set<String> = emptySet(),
    /** Stars 1…3 of the best bite quiz. */
    val stars: Map<String, Int> = emptyMap(),
    /** Trophy tier 1…3 per lesson number (Bronze, Silber, Gold). */
    val trophies: Map<Int, Int> = emptyMap(),
    val tournamentsFought: Int = 0,
    /** Best score 0…100 per game (key = AiGame name). */
    val games: Map<String, Int> = emptyMap(),
    val badges: Set<String> = emptySet(),
    val lessons: Map<Int, LessonStat> = emptyMap(),
    /** Ids of hand-written questions answered wrongly and not yet answered correctly since. */
    val wrong: Set<String> = emptySet()
) {
    fun lesson(n: Int) = lessons[n] ?: LessonStat()
    val starTotal: Int get() = stars.values.sum()
}

data class AiBadge(val key: String, val title: String, val description: String, val condition: (AiProgress) -> Boolean)

sealed interface AiEvent {
    data class Xp(val amount: Int, val reason: String) : AiEvent
    data class RankUp(val level: Int, val rank: String) : AiEvent
    data class BadgeUnlocked(val badge: AiBadge) : AiEvent
    data class StreakExtended(val days: Int) : AiEvent
    data class Stars(val bite: String, val stars: Int) : AiEvent
    data class Trophy(val lesson: Int, val tier: Int) : AiEvent
}

/**
 * Gamification of the academy, as pure functions of the previous [AiProgress].
 *
 * Level curve as in the lab: level L needs X(L) = 50·L·(L − 1) XP, inverse L = ⌊(1 + √(1 + 0,08·XP))/2⌋.
 * Every level has its own rank name (no name twice); beyond the named ranks the legend counts on in
 * Roman numerals.
 *
 * XP: answer = base (concept/term 10, code and calculation 14) × difficulty × streak bonus (1 + 0,1·min(streak, 10));
 * wrong answer 2. Bite read 15; bite quiz stars (≥ 60 % ★, ≥ 85 % ★★, 100 % ★★★) 20 XP per new star;
 * tournament 10 + 2 per hit, trophy 100/175/250 XP (only the improvement is paid); daily trial 30 + 10 per hit.
 */
object AiRules {
    fun xpForLevel(level: Int): Long = 50L * level * (level - 1)

    fun level(xp: Long): Int = floor((1 + sqrt(1 + 0.08 * xp)) / 2).toInt().coerceAtLeast(1)

    fun levelProgress(xp: Long): Double {
        val l = level(xp)
        val lo = xpForLevel(l); val hi = xpForLevel(l + 1)
        return ((xp - lo).toDouble() / (hi - lo)).coerceIn(0.0, 1.0)
    }

    /** One rank per level, from the punch-card apprentice to Lovelace's heir. */
    val RANKS: List<String> = listOf(
        "Lehrbub der Lochkarte", "Tintenjunge des Datensatzes", "Zeilenzähler im Skriptorium", "Merkmalssammler",
        "Knappe des Labels", "Hüter der Trainingsmenge", "Geselle der kleinsten Quadrate", "Steigungsmesser am Hang des Verlusts",
        "Wächter des Validierungstors", "Ritter der Regression", "Schildträger der Sigmoide", "Förster der Entscheidungsbäume",
        "Hauptmann der Hyperebene", "Bannerträger des Zufallswaldes", "Kundschafter der Cluster", "Kartograf der Hauptkomponenten",
        "Vogt der Kreuzvalidierung", "Neuronenschmied", "Feuermeister der Aktivierung", "Herold der Backpropagation",
        "Bogenschütze des Gradienten", "Kastellan der Keras-Burg", "Tensorweber", "Glasmaler der Faltungsfenster",
        "Fackelträger von PyTorch", "Alchemist des Autograd", "Proviantmeister der Mini-Batches", "Mönch vom Orden des Dropout",
        "Normenrichter der Batch-Normalisierung", "Landvermesser der Satellitenbilder", "Mosaikleger des Vision Transformers", "Tokenschneider",
        "Wortschmied der Einbettungen", "Seher des Skip-Gramms", "Dolmetscher zwischen Encoder und Decoder", "Freiherr der Positionskodierung",
        "Graf der Aufmerksamkeit", "Markgraf der vielen Köpfe", "Prophet des nächsten Tokens", "Maskenbildner von BERT",
        "Feinschmied der Gewichte", "Adaptermeister der Zunft", "Großmeister des niedrigen Rangs", "Münzmeister der Quantisierung",
        "Hofmeister der Instruktionen", "Richter des Belohnungsmodells", "Berater der proximalen Politik", "Herzog der Präferenzen",
        "Archivar des Vektorgewölbes", "Oberbibliothekar von Retrievia", "Kettenschmied von LangChain", "Kommandant der Agenten",
        "Baumeister der RAG-Kathedrale", "Erzkanzler der Inferenz", "Admiral der Modellflotte", "Kurfürst der Fundamentmodelle",
        "Erzmagier der Transformer", "Kaiserlicher AI-Ingenieur", "Hüter des Turing-Feuers", "Lovelaces Erbe der denkenden Maschinen"
    )

    fun rank(level: Int): String = RANKS.getOrNull(level - 1) ?: "Legende der denkenden Maschinen ${roman(level - RANKS.size)}"

    private fun roman(n: Int): String {
        val v = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val s = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var x = n; val b = StringBuilder()
        for (i in v.indices) while (x >= v[i]) { b.append(s[i]); x -= v[i] }
        return b.toString()
    }

    fun streakMultiplier(streak: Int) = 1 + 0.1 * min(streak, 10)

    fun stars(correct: Int, total: Int): Int {
        if (total <= 0) return 0
        val f = correct.toDouble() / total
        return when { correct == total -> 3; f >= 0.85 -> 2; f >= 0.6 -> 1; else -> 0 }
    }

    val TROPHY_XP = intArrayOf(0, 100, 175, 250)
    const val WRONG_CAP = 300

    private fun lessonBadges(): List<AiBadge> = AiCourse.lessons.flatMap { l ->
        listOf(
            AiBadge("lesson${l.number}", "Lektion ${l.number} vollendet", "Alle Happen von „${l.title}“ gelesen") { p ->
                l.biteKeys.all { it in p.readBites }
            },
            AiBadge("champ${l.number}", "Bezwinger: ${l.champion.name}", "Das Turnier der Lektion ${l.number} gewonnen") { p -> (p.trophies[l.number] ?: 0) >= 1 }
        )
    }

    val BADGES: List<AiBadge> by lazy {
        listOf(
            AiBadge("first", "Der erste Federstrich", "Die erste Frage beantwortet") { it.answered >= 1 },
            AiBadge("c25", "Fleißiger Skriptor", "25 richtige Antworten") { it.correct >= 25 },
            AiBadge("c100", "Hundert Treffer", "100 richtige Antworten") { it.correct >= 100 },
            AiBadge("c500", "Unermüdlicher Rechenknecht", "500 richtige Antworten") { it.correct >= 500 },
            AiBadge("c1000", "Tausend Gradientenschritte", "1000 richtige Antworten") { it.correct >= 1000 },
            AiBadge("s3", "Drei Tage am Amboss", "3 Tage in Folge gelernt") { it.bestStreak >= 3 },
            AiBadge("s7", "Eine Woche Schmiedefeuer", "7 Tage in Folge gelernt") { it.bestStreak >= 7 },
            AiBadge("s30", "Ein Mond voller Epochen", "30 Tage in Folge gelernt") { it.bestStreak >= 30 },
            AiBadge("read10", "Zehn Happen verspeist", "10 Happen gelesen") { it.readBites.size >= 10 },
            AiBadge("read50", "Festmahl des Wissens", "50 Happen gelesen") { it.readBites.size >= 50 },
            AiBadge("readAll", "Die ganze Tafel geleert", "Jeden Happen des Kurses gelesen") { p -> AiCourse.keys.all { it in p.readBites } },
            AiBadge("star1", "Erster Stern", "Ein Happen-Quiz mit mindestens einem Stern") { it.stars.isNotEmpty() },
            AiBadge("perfect", "Makellose Rüstung", "Ein Happen-Quiz ohne Fehler") { it.perfectQuizzes >= 1 },
            AiBadge("perfect10", "Zehnfach makellos", "Zehn Happen-Quizze ohne Fehler") { it.perfectQuizzes >= 10 },
            AiBadge("stars100", "Sternenhimmel", "100 Sterne gesammelt") { it.starTotal >= 100 },
            AiBadge("joust1", "Erste Tjost", "An einem Turnier teilgenommen") { it.tournamentsFought >= 1 },
            AiBadge("gold1", "Goldene Lanze", "Ein Turnier ohne verlorenen Schild gewonnen") { p -> p.trophies.values.any { it >= 3 } },
            AiBadge("gold13", "Herr aller Turniere", "Alle 13 Turniere in Gold gewonnen") { p -> AiCourse.lessons.all { (p.trophies[it.number] ?: 0) >= 3 } },
            AiBadge("daily1", "Tagesprobe bestanden", "Die erste Tagesprobe abgelegt") { it.dailyCount >= 1 },
            AiBadge("daily10", "Zehn Morgenappelle", "10 Tagesproben") { it.dailyCount >= 10 },
            AiBadge("play1", "Spielmann", "Ein Spiel der Spielwiese gespielt") { it.games.isNotEmpty() },
            AiBadge("playAll80", "Meister der Spielwiese", "In jedem Spiel mindestens 80 Punkte") { p -> AiGame.entries.all { (p.games[it.name] ?: 0) >= 80 } },
            AiBadge("rank10", "Zum Ritter geschlagen", "Stufe 10 erreicht") { level(it.xp) >= 10 },
            AiBadge("rank30", "In den Adelsstand erhoben", "Stufe 30 erreicht") { level(it.xp) >= 30 },
            AiBadge("ibm", "AI Engineer", "Alle 13 Lektionen gelesen und alle Turniere gewonnen") { p ->
                AiCourse.keys.all { it in p.readBites } && AiCourse.lessons.all { (p.trophies[it.number] ?: 0) >= 1 }
            }
        ) + lessonBadges()
    }

    val badgeByKey: Map<String, AiBadge> by lazy { BADGES.associateBy { it.key } }

    // ---------------------------------------------------------------- transitions

    private fun touchDay(p: AiProgress, today: Long, ev: MutableList<AiEvent>): AiProgress {
        if (p.lastActiveDay == today) return p
        val streak = if (p.lastActiveDay == today - 1) p.streak + 1 else 1
        if (streak > 1) ev += AiEvent.StreakExtended(streak)
        return p.copy(streak = streak, bestStreak = maxOf(p.bestStreak, streak), lastActiveDay = today)
    }

    private fun gain(p: AiProgress, amount: Int, reason: String, ev: MutableList<AiEvent>): AiProgress {
        if (amount <= 0) return p
        val before = level(p.xp)
        val next = p.copy(xp = p.xp + amount)
        ev += AiEvent.Xp(amount, reason)
        val after = level(next.xp)
        if (after > before) ev += AiEvent.RankUp(after, rank(after))
        return next
    }

    private fun badges(p: AiProgress, ev: MutableList<AiEvent>): AiProgress {
        val fresh = BADGES.filter { it.key !in p.badges && it.condition(p) }
        if (fresh.isEmpty()) return p
        fresh.forEach { ev += AiEvent.BadgeUnlocked(it) }
        return p.copy(badges = p.badges + fresh.map { it.key })
    }

    fun xpFor(q: AiQuestion, streak: Int): Int {
        val base = if (q.kind == QKind.CODE || q.kind == QKind.CALC) 14 else 10
        return (base * q.difficulty * streakMultiplier(streak)).toInt()
    }

    fun answer(p0: AiProgress, q: AiQuestion, correct: Boolean, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        var p = touchDay(p0, today, ev)
        val lesson = AiCourse.lessonOf(q.bite)
        val s = p.lesson(lesson)
        val alpha = 0.1 + 0.05 * (q.difficulty - 1)
        val mastery = s.mastery + alpha * ((if (correct) 1.0 else 0.0) - s.mastery)
        // Only hand-written questions can be revisited; generated ids are not stable.
        val known = q.id in AiCourse.questionById
        val wrong = when {
            !known -> p.wrong
            correct -> p.wrong - q.id
            else -> (p.wrong + q.id).let { if (it.size > WRONG_CAP) it.drop(it.size - WRONG_CAP).toSet() else it }
        }
        p = p.copy(
            answered = p.answered + 1, correct = p.correct + if (correct) 1 else 0, wrong = wrong,
            lessons = p.lessons + (lesson to LessonStat(s.answered + 1, s.correct + if (correct) 1 else 0, mastery))
        )
        p = gain(p, if (correct) xpFor(q, p.streak) else 2, if (correct) "Treffer" else "Versuch zählt", ev)
        return badges(p, ev) to ev
    }

    fun biteRead(p0: AiProgress, key: String, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        if (key in p0.readBites) return p0 to ev
        var p = touchDay(p0, today, ev).copy(readBites = p0.readBites + key)
        p = gain(p, 15, "Happen gelesen", ev)
        return badges(p, ev) to ev
    }

    fun biteQuizFinished(p0: AiProgress, key: String, correct: Int, total: Int, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        var p = touchDay(p0, today, ev)
        val s = stars(correct, total)
        val old = p.stars[key] ?: 0
        if (total > 0 && correct == total) p = p.copy(perfectQuizzes = p.perfectQuizzes + 1)
        if (s > old) {
            p = p.copy(stars = p.stars + (key to s))
            ev += AiEvent.Stars(key, s)
            p = gain(p, 20 * (s - old), "${"★".repeat(s)} Happen-Quiz", ev)
        }
        return badges(p, ev) to ev
    }

    fun tournamentFinished(p0: AiProgress, lesson: Int, tier: Int, hits: Int, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        var p = touchDay(p0, today, ev).copy(tournamentsFought = p0.tournamentsFought + 1)
        p = gain(p, 10 + 2 * hits, "Turnier", ev)
        val old = p.trophies[lesson] ?: 0
        if (tier > old) {
            p = p.copy(trophies = p.trophies + (lesson to tier))
            ev += AiEvent.Trophy(lesson, tier)
            p = gain(p, TROPHY_XP[tier] - TROPHY_XP[old], Joust.tierName(tier), ev)
        }
        return badges(p, ev) to ev
    }

    fun dailyFinished(p0: AiProgress, correct: Int, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        if (p0.lastDailyDay == today) return p0 to ev
        var p = touchDay(p0, today, ev).copy(lastDailyDay = today, dailyCount = p0.dailyCount + 1)
        p = gain(p, 30 + 10 * correct, "Tagesprobe", ev)
        return badges(p, ev) to ev
    }

    /** A finished game with score 0…100: 5 + score/5 XP every time, plus 25 for a new personal best. */
    fun gameFinished(p0: AiProgress, game: AiGame, score: Int, today: Long): Pair<AiProgress, List<AiEvent>> {
        val ev = ArrayList<AiEvent>()
        val s = score.coerceIn(0, 100)
        var p = touchDay(p0, today, ev)
        val old = p.games[game.name]
        p = p.copy(games = p.games + (game.name to maxOf(old ?: 0, s)))
        p = gain(p, 5 + s / 5, game.title, ev)
        if (old == null || s > old) p = gain(p, 25, "Neuer Bestwert", ev)
        return badges(p, ev) to ev
    }

    // ---------------------------------------------------------------- persistence

    fun encode(p: AiProgress): String = buildString {
        appendLine("v=1")
        appendLine("xp=${p.xp}")
        appendLine("answered=${p.answered}")
        appendLine("correct=${p.correct}")
        appendLine("streak=${p.streak}")
        appendLine("best=${p.bestStreak}")
        appendLine("last=${p.lastActiveDay}")
        appendLine("daily=${p.lastDailyDay}")
        appendLine("dailyCount=${p.dailyCount}")
        appendLine("perfect=${p.perfectQuizzes}")
        appendLine("fought=${p.tournamentsFought}")
        appendLine("read=${p.readBites.joinToString(",")}")
        appendLine("badges=${p.badges.joinToString(",")}")
        appendLine("wrong=${p.wrong.joinToString(",")}")
        p.stars.forEach { (k, s) -> appendLine("star.$k=$s") }
        p.trophies.forEach { (l, t) -> appendLine("trophy.$l=$t") }
        p.games.forEach { (g, s) -> appendLine("game.$g=$s") }
        p.lessons.forEach { (l, s) -> appendLine("lesson.$l=${s.answered},${s.correct},${s.mastery}") }
    }

    private fun pairs(text: String) = text.lines().mapNotNull { l -> l.indexOf('=').takeIf { it > 0 }?.let { l.substring(0, it) to l.substring(it + 1) } }

    fun decode(text: String?): AiProgress {
        if (text.isNullOrBlank()) return AiProgress()
        val m = pairs(text)
        fun long(k: String, d: Long) = m.firstOrNull { it.first == k }?.second?.toLongOrNull() ?: d
        fun int(k: String) = long(k, 0).toInt()
        fun set(k: String) = m.firstOrNull { it.first == k }?.second?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        fun <K> prefixed(prefix: String, key: (String) -> K?, range: IntRange) = m.filter { it.first.startsWith(prefix) }.mapNotNull { (k, v) ->
            val kk = key(k.removePrefix(prefix)) ?: return@mapNotNull null
            v.toIntOrNull()?.coerceIn(range)?.let { kk to it }
        }.toMap()
        val lessons = m.filter { it.first.startsWith("lesson.") }.mapNotNull { (k, v) ->
            val n = k.removePrefix("lesson.").toIntOrNull() ?: return@mapNotNull null
            val parts = v.split(',')
            n to LessonStat(parts.getOrNull(0)?.toIntOrNull() ?: 0, parts.getOrNull(1)?.toIntOrNull() ?: 0,
                parts.getOrNull(2)?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 0.0)
        }.toMap()
        return AiProgress(
            xp = long("xp", 0).coerceAtLeast(0), answered = int("answered"), correct = int("correct"),
            streak = int("streak"), bestStreak = int("best"), lastActiveDay = long("last", Long.MIN_VALUE),
            lastDailyDay = long("daily", Long.MIN_VALUE), dailyCount = int("dailyCount"), perfectQuizzes = int("perfect"),
            tournamentsFought = int("fought"), readBites = set("read"), badges = set("badges"), wrong = set("wrong"),
            stars = prefixed("star.", { it }, 0..3), trophies = prefixed("trophy.", { it.toIntOrNull() }, 0..3),
            games = prefixed("game.", { it }, 0..100), lessons = lessons
        )
    }

    /**
     * One-time import from the physics lab, where the AI path used to live: read AI chapters become read
     * bites (15 XP each) and the game best scores carry over. [labText] is the lab's stored record.
     */
    fun importFromLab(labText: String?): AiProgress {
        if (labText.isNullOrBlank()) return AiProgress()
        val m = pairs(labText)
        val read = m.firstOrNull { it.first == "read" }?.second?.split(',')?.filter { it in AiCourse.byKey }?.toSet() ?: emptySet()
        val games = m.filter { it.first.startsWith("game.") }.mapNotNull { (k, v) ->
            val g = k.removePrefix("game.")
            if (AiGame.entries.none { it.name == g }) null else v.toIntOrNull()?.coerceIn(0, 100)?.let { g to it }
        }.toMap()
        if (read.isEmpty() && games.isEmpty()) return AiProgress()
        val p = AiProgress(xp = 15L * read.size + 25L * games.size, readBites = read, games = games)
        return p.copy(badges = BADGES.filter { it.condition(p) }.map { it.key }.toSet())
    }
}

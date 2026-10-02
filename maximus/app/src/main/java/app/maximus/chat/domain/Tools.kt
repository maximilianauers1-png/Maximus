package app.maximus.chat.domain

import app.maximus.dnd.domain.DiceParser
import app.maximus.dnd.domain.DiceProbability
import app.maximus.dnd.domain.DiceRoller
import app.maximus.lab.domain.ExprParser
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.Phys
import app.maximus.strongman.domain.FTable
import app.maximus.strongman.domain.OneRepMax
import app.maximus.strongman.domain.OneRmResult
import app.maximus.strongman.domain.PlateCalculator
import app.maximus.strongman.domain.PlateResult
import app.maximus.strongman.domain.PlateStock
import app.maximus.strongman.domain.RefusalReason
import kotlin.random.Random

/** A tool result: a card for the user and a compact text line for the model's prompt. */
data class ToolResult(val card: ToolCard, val context: String)

/**
 * Deterministic tools. A 1–4-B-parameter model cannot reliably do arithmetic, roll fair dice or recall
 * exact rule texts, so the app computes these itself and hands the model the exact results. Slash
 * commands run a tool directly and answer instantly without the model.
 */
object ChatTools {
    data class Command(val names: List<String>, val usage: String, val help: String)

    val COMMANDS = listOf(
        Command(listOf("rechne", "r", "calc"), "/rechne 2^10 + sqrt(2)·π", "Taschenrechner mit Funktionen (sin, ln, sqrt, gamma, erf …) und Konstanten"),
        Command(listOf("w", "würfel", "wuerfel", "roll"), "/w 2d20kh1+7", "Würfeln mit Verteilung: kh/kl, r<2, Explodieren, Fudge"),
        Command(listOf("1rm", "e1rm"), "/1rm 180x5 @8", "1RM-Schätzung und Zielgewichte nach Wiederholungen und RPE"),
        Command(listOf("scheiben", "plates"), "/scheiben 182,5 20", "Scheibenbeladung pro Seite (Ziel, Stange)"),
        Command(listOf("zauber", "spell", "regel", "dnd"), "/zauber Eldritch Blast", "D&D-Nachschlagen: Zauber, Feats, Invocations, Metamagie, Pakte, Kampfstile"),
        Command(listOf("formel", "kompendium", "k"), "/formel Fermi-Dirac", "Suche im Physik- und Mathe-Kompendium"),
        Command(listOf("konstante", "const"), "/konstante Boltzmann", "Physikalische Konstanten (CODATA)"),
        Command(listOf("hilfe", "help", "?"), "/hilfe", "Diese Übersicht")
    )

    // ------------------------------------------------------------------ calculator

    fun calculate(expr: String): ToolResult {
        val clean = expr.trim().trimEnd('=', '?').trim()
        return try {
            val e = ExprParser.parse(clean)
            val v = e.eval(emptyMap())
            val shown = formatNumber(v)
            ToolResult(ToolCard("rechne", "Rechner", "$clean = $shown"), "Rechner: $clean = $shown")
        } catch (ex: RuntimeException) {
            ToolResult(ToolCard("rechne", "Rechner", "„$clean“ ist kein gültiger Ausdruck: ${ex.message}", ok = false), "")
        }
    }

    /** Integers exactly, everything else with 10 significant digits (German decimal comma, like the lab). */
    fun formatNumber(v: Double): String =
        if (v.isFinite() && v == Math.rint(v) && kotlin.math.abs(v) < 1e15) String.format(java.util.Locale.GERMANY, "%,d", v.toLong()).replace(',', '.').replace('-', '−')
        else Fmt.num(v, 10)

    // ------------------------------------------------------------------ dice

    /** German "2w6" is accepted as "2d6". */
    fun normaliseDice(s: String): String = s.trim().replace(Regex("(?<=\\d|^|[^a-zA-Z])[wW](?=\\d|%|F)"), "d")

    fun dice(spec: String, random: Random = Random.Default, roll: Boolean = true): ToolResult {
        val norm = normaliseDice(spec)
        return try {
            val node = DiceParser.parse(norm)
            val sb = StringBuilder()
            val ctx = StringBuilder()
            if (roll) {
                val r = DiceRoller.roll(node, random)
                val dice = r.pools.joinToString("  ") { p ->
                    p.spec.let { "${it.count}d${it.sides}" } + ": " + p.dice.joinToString(" ") { d ->
                        (if (d.kept) "${d.value}" else "(${d.value})") + (if (d.crit) "!" else "") + (if (d.exploded) "↑" else "")
                    }
                }
                sb.append("Ergebnis **${r.total}**").append(if (dice.isNotEmpty()) "  ·  $dice" else "")
                ctx.append("Würfelwurf $norm = ${r.total}. ")
            }
            runCatching { DiceProbability.of(node) }.getOrNull()?.let { d ->
                val line = "Ø ${Fmt.num(d.mean, 4)}, σ ${Fmt.num(d.stdDev, 3)}, Bereich ${d.min}…${d.max}, Median ${d.quantile(0.5)}"
                if (sb.isNotEmpty()) sb.append('\n')
                sb.append(line)
                ctx.append("Verteilung von $norm: $line.")
            }
            ToolResult(ToolCard("w", "Würfel $norm", sb.toString()), ctx.toString().trim())
        } catch (ex: RuntimeException) {
            ToolResult(ToolCard("w", "Würfel", "„$spec“ ist keine gültige Würfelangabe: ${ex.message}", ok = false), "")
        }
    }

    // ------------------------------------------------------------------ strength

    private val SET_RE = Regex("(\\d+(?:[.,]\\d+)?)\\s*(?:kg)?\\s*[x×*]\\s*(\\d{1,2})(?:\\s*@\\s*(?:rpe\\s*)?(\\d+(?:[.,]5)?))?", RegexOption.IGNORE_CASE)

    fun oneRm(input: String): ToolResult {
        val m = SET_RE.find(input) ?: return ToolResult(ToolCard("1rm", "1RM", "Format: Gewicht x Wiederholungen [@RPE], z. B. 180x5 @8", ok = false), "")
        val w = m.groupValues[1].replace(',', '.').toDouble()
        val reps = m.groupValues[2].toInt()
        val rpe = m.groupValues[3].takeIf { it.isNotEmpty() }?.replace(',', '.')?.toDouble()
        val table = FTable()
        val est = if (rpe != null) table.e1rm(w, reps, rpe) else OneRepMax.estimate(w, reps)
        return when (est) {
            is OneRmResult.Refused -> ToolResult(ToolCard("1rm", "1RM", refusal(est.reason), ok = false), "")
            is OneRmResult.Estimate -> {
                val one = est.kg
                val targets = listOf(1, 2, 3, 5, 8, 10).joinToString("\n") { r ->
                    "$r Wdh.: @8 → ${Fmt.num(table.prescribe(one, r, 8.0, 2.5), 4)} kg · @10 → ${Fmt.num(table.prescribe(one, r, 10.0, 2.5), 4)} kg"
                }
                val how = if (rpe != null) "RPE-Tabelle (RIR-basiert)" else "Mittel aus Epley und Brzycki"
                val head = "${Fmt.num(w, 4)} kg × $reps${rpe?.let { " @ RPE ${Fmt.num(it, 2)}" } ?: ""} → e1RM **${Fmt.num(one, 4)} kg** ($how)"
                ToolResult(
                    ToolCard("1rm", "1RM-Schätzung", "$head\n$targets"),
                    "1RM-Rechner: ${Fmt.num(w, 4)} kg × $reps${rpe?.let { " @RPE ${Fmt.num(it, 2)}" } ?: ""} ergibt geschätztes 1RM ${Fmt.num(one, 4)} kg ($how). " +
                        "Zielgewichte @RPE 8: 3 Wdh. ${Fmt.num(table.prescribe(one, 3, 8.0, 2.5), 4)} kg, 5 Wdh. ${Fmt.num(table.prescribe(one, 5, 8.0, 2.5), 4)} kg."
                )
            }
        }
    }

    private fun refusal(r: RefusalReason) = when (r) {
        RefusalReason.NON_POSITIVE_LOAD -> "Das Gewicht muss positiv sein."
        RefusalReason.REPS_BELOW_ONE -> "Mindestens eine Wiederholung."
        RefusalReason.REPS_ABOVE_LIMIT -> "Über ${OneRepMax.MAX_REPS} Wiederholungen ist eine 1RM-Schätzung zu ungenau."
        RefusalReason.INVALID_RPE -> "RPE muss zwischen 5 und 10 liegen (halbe Schritte)."
    }

    /** Standard gym stock (pairs), same as the strongman module's default. */
    val STANDARD_STOCK = listOf(
        PlateStock(25.0, 4), PlateStock(20.0, 2), PlateStock(15.0, 1), PlateStock(10.0, 2),
        PlateStock(5.0, 2), PlateStock(2.5, 2), PlateStock(1.25, 2), PlateStock(0.5, 1), PlateStock(0.25, 1)
    )

    fun plates(args: String, stock: List<PlateStock> = STANDARD_STOCK): ToolResult {
        val nums = Regex("\\d+(?:[.,]\\d+)?").findAll(args).map { it.value.replace(',', '.').toDouble() }.toList()
        val target = nums.getOrNull(0) ?: return ToolResult(ToolCard("scheiben", "Scheiben", "Format: /scheiben Ziel [Stange], z. B. /scheiben 182,5 20", ok = false), "")
        val bar = nums.getOrNull(1) ?: 20.0
        return when (val r = PlateCalculator.solve(target, bar, stock)) {
            is PlateResult.Loadable -> {
                val side = if (r.perSide.isEmpty()) "nur die Stange" else r.perSide.joinToString(" + ") { Fmt.num(it, 4) }
                ToolResult(ToolCard("scheiben", "Beladung ${Fmt.num(target, 5)} kg", "Pro Seite: $side\nStange ${Fmt.num(bar, 4)} kg"),
                    "Scheibenrechner: ${Fmt.num(target, 5)} kg mit ${Fmt.num(bar, 4)}-kg-Stange = pro Seite $side.")
            }
            is PlateResult.NotLoadable -> ToolResult(
                ToolCard("scheiben", "Beladung ${Fmt.num(target, 5)} kg",
                    "Mit dem Standard-Scheibensatz nicht exakt ladbar. Nächste Gewichte: " +
                        listOfNotNull(r.nearestBelowKg?.let { "${Fmt.num(it, 5)} kg" }, r.nearestAboveKg?.let { "${Fmt.num(it, 5)} kg" }).joinToString(" oder "),
                    ok = false), ""
            )
        }
    }

    // ------------------------------------------------------------------ lookups

    fun dndLookup(query: String): ToolResult {
        val d = Knowledge.lookupDnd(query)
            ?: return ToolResult(ToolCard("zauber", "D&D", "Nichts zu „$query“ gefunden.", ok = false), "")
        return ToolResult(ToolCard("zauber", "${d.source}: ${d.title}", d.text), "${d.source} „${d.title}“: ${clip(d.text, 700)}")
    }

    fun compendium(query: String, limit: Int = 2): ToolResult? {
        val hits = Knowledge.compendium.search(query, limit)
        if (hits.isEmpty()) return null
        val body = hits.joinToString("\n\n") { "**${it.doc.title}**\n${clip(it.doc.text, 420)}" }
        val ctx = hits.joinToString("\n") { "[${it.doc.title}] ${clip(it.doc.text, 650)}" }
        return ToolResult(ToolCard("formel", "Kompendium", body), ctx)
    }

    fun constant(query: String): ToolResult {
        val q = query.trim().lowercase()
        val c = Phys.all.firstOrNull { it.symbol.lowercase() == q || it.key.lowercase() == q }
            ?: Phys.all.firstOrNull { it.name.lowercase().contains(q) }
            ?: Phys.all.minByOrNull { Knowledge.levenshtein(it.name.lowercase().take(q.length), q) }?.takeIf {
                Knowledge.levenshtein(it.name.lowercase().take(q.length), q) <= 2
            }
            ?: return ToolResult(ToolCard("konstante", "Konstante", "Keine Konstante „$query“ gefunden.", ok = false), "")
        val v = "${c.symbol} = ${Fmt.num(c.value, 10)} ${c.unit}" + if (c.exact) " (exakt)" else " (rel. Unsicherheit ${Fmt.num(c.relUncertainty, 2)})"
        return ToolResult(ToolCard("konstante", c.name, v), "${c.name}: $v")
    }

    fun help(): ToolResult = ToolResult(
        ToolCard("hilfe", "Befehle", COMMANDS.joinToString("\n") { "`${it.usage}` – ${it.help}" } +
            "\n\nOhne Befehl erkennt Maximus Würfelangaben, Sätze wie „180 kg x 5“, Rechenausdrücke, D&D-Namen und Fachbegriffe " +
            "automatisch und gibt dem Modell die exakten Ergebnisse mit."),
        ""
    )

    /** Compact profile of the user's lifts for strongman questions, e.g. "Kniebeuge e1RM 215 kg". */
    fun trainingSummary(lifts: List<Pair<String, Double>>, sessionsLast28Days: Int): String? {
        if (lifts.isEmpty() && sessionsLast28Days == 0) return null
        val top = lifts.sortedByDescending { it.second }.take(8).joinToString(", ") { "${it.first} e1RM ${Fmt.num(it.second, 4)} kg" }
        return listOfNotNull(top.takeIf { it.isNotEmpty() }, "Einheiten in den letzten 28 Tagen: $sessionsLast28Days").joinToString(". ") + "."
    }

    fun clip(s: String, n: Int): String = if (s.length <= n) s else s.take(n).substringBeforeLast(' ') + " …"
}

/** What the router decided for one user message. */
data class RoutedTurn(
    val focus: Focus,
    val results: List<ToolResult>,
    /** A slash command was executed: answer directly, no model needed. */
    val direct: Boolean,
    /** The user text without the command (what the model should answer). */
    val query: String
) {
    /** Prompt context from the tools; empty lines are dropped. */
    val context: String get() = results.map { it.context }.filter { it.isNotBlank() }.joinToString("\n")
}

/**
 * Decides which tools run for a message. Explicit slash commands win; otherwise lightweight detectors
 * fire only on unambiguous patterns, and retrieval only in the matching focus, so the prompt stays short.
 */
object ToolRouter {
    private val DICE = Regex("(?<![\\p{L}\\d])(\\d{0,3}[dDwW](?:\\d{1,4}|%|F)(?:k[hl]\\d+|r<?\\d+|!)*(?:\\s*[+\\-]\\s*\\d+)?)(?![\\p{L}\\d])")
    private val ROLL_INTENT = Regex("\\b(würfl|würfel|wuerfel|roll|wirf|werfe)", RegexOption.IGNORE_CASE)
    private val SET = Regex("\\b\\d{2,3}(?:[.,]\\d+)?\\s*(?:kg)?\\s*[x×]\\s*\\d{1,2}(?:\\s*@\\s*(?:rpe\\s*)?\\d+(?:[.,]5)?)?\\b", RegexOption.IGNORE_CASE)
    private val CALC_INTENT = Regex("(?i)(rechne|berechne|was ist|was gibt|wie viel ist|wieviel ist|ergibt)\\s+|=\\s*\\?|=\\s*$")
    private val EXPR = Regex("[0-9πe(][0-9a-zπ√²³!.,+\\-*/^()·× ]*[0-9)π²³!]")

    fun route(text: String, focusMode: Focus?, previous: Focus?, random: Random = Random.Default): RoutedTurn {
        val trimmed = text.trim()
        if (trimmed.startsWith("/")) command(trimmed, previous, random)?.let { return it }

        val focus = focusMode ?: FocusDetector.detect(trimmed, previous)
        val results = ArrayList<ToolResult>()

        // Dice: rolled only on an explicit request, otherwise the exact distribution is provided.
        val wantsRoll = ROLL_INTENT.containsMatchIn(trimmed)
        if (focus == Focus.DND || wantsRoll) {
            DICE.findAll(trimmed).map { it.groupValues[1] }.distinct().take(3)
                .forEach { d -> ChatTools.dice(d, random, roll = wantsRoll).takeIf { it.card.ok }?.let(results::add) }
        }
        // A logged set like "180 kg x 5 @8".
        SET.find(trimmed)?.let { m -> ChatTools.oneRm(m.value).takeIf { it.card.ok }?.let(results::add) }
        // Arithmetic, only with an explicit intent ("rechne …", "… = ?").
        if (CALC_INTENT.containsMatchIn(trimmed)) {
            EXPR.findAll(trimmed).map { it.value.trim() }
                .filter { e -> e.any { it.isDigit() } && e.any { it in "+-*/^·×√!²³" || it.isLetter() } && e.length >= 3 }
                .mapNotNull { e -> ChatTools.calculate(e).takeIf { it.card.ok } }
                .firstOrNull()?.let(results::add)
        }
        // Grounding from the app's own texts.
        when (focus) {
            Focus.DND -> Knowledge.dndMentions(trimmed).forEach { d ->
                results += ToolResult(ToolCard("zauber", "${d.source}: ${d.title}", d.text), "${d.source} „${d.title}“: ${ChatTools.clip(d.text, 600)}")
            }
            Focus.SCIENCE -> ChatTools.compendium(trimmed)?.takeIf { relevant(trimmed) }?.let(results::add)
            else -> Unit
        }
        return RoutedTurn(focus, results, direct = false, query = trimmed)
    }

    /** Retrieval is used only when the best hit is clearly relevant (BM25 above a corpus-relative threshold). */
    private fun relevant(query: String): Boolean {
        val hit = Knowledge.compendium.search(query, 1).firstOrNull() ?: return false
        val maxIdf = Text.terms(query).maxOfOrNull { Knowledge.compendium.idf(it) } ?: return false
        return hit.score >= 0.6 * maxIdf
    }

    private fun command(text: String, previous: Focus?, random: Random): RoutedTurn? {
        val name = text.drop(1).takeWhile { !it.isWhitespace() }.lowercase()
        val args = text.drop(1 + name.length).trim()
        val cmd = ChatTools.COMMANDS.firstOrNull { name in it.names } ?: return null
        val (focus, result) = when (cmd.names.first()) {
            "rechne" -> Focus.SCIENCE to ChatTools.calculate(args)
            "w" -> Focus.DND to ChatTools.dice(args.ifEmpty { "1d20" }, random)
            "1rm" -> Focus.STRONGMAN to ChatTools.oneRm(args)
            "scheiben" -> Focus.STRONGMAN to ChatTools.plates(args)
            "zauber" -> Focus.DND to ChatTools.dndLookup(args)
            "formel" -> Focus.SCIENCE to (ChatTools.compendium(args, 3)
                ?: ToolResult(ToolCard("formel", "Kompendium", "Nichts zu „$args“ gefunden.", ok = false), ""))
            "konstante" -> Focus.SCIENCE to ChatTools.constant(args)
            else -> (previous ?: Focus.GENERAL) to ChatTools.help()
        }
        return RoutedTurn(focus, listOf(result), direct = true, query = args)
    }
}

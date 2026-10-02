package app.maximus.chat.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatDomainTest {
    @Test
    fun conversationCodecRoundTripsArbitraryText() {
        val tricky = "12:34 Uhr\n```kotlin\nval x = \"a:b\"\n```\nÄÖÜ ħ∫ 🎲 5:5"
        val c = Conversation(
            7, "Titel: mit Doppelpunkt", listOf(
                ChatMessage(1, Role.USER, tricky, focus = Focus.CODE, createdAt = 11),
                ChatMessage(
                    2, Role.ASSISTANT, "Antwort", tools = listOf(ToolCard("w", "Würfel 1d20", "Ergebnis **17**\n3:4", ok = false)),
                    thinking = "<denken>", stats = GenStats(120, 64, 350, 4200, "GPU"), focus = Focus.DND, stopped = true, createdAt = 12
                )
            ), 99, Focus.SCIENCE, pinned = true
        )
        assertEquals(c, ChatCodec.decode(ChatCodec.encode(c)))
        assertNull(ChatCodec.decode("kaputt"))
        assertNull(ChatCodec.decode("5:c1"))
    }

    @Test
    fun settingsCodecRoundTrips() {
        val s = ChatSettings(ModelFile("/data/m/gemma.task", "gemma-3n-E2B-it-int4.task", 3_100_000_000), Backend.CPU, 2048, 512, false, 60, false)
        assertEquals(s, ChatSettings.decode(ChatSettings.encode(s)))
        assertEquals(ChatSettings(), ChatSettings.decode(null))
    }

    @Test
    fun titleIsShortAndCutAtAWord() {
        assertEquals("Neuer Chat", Conversation.titleFrom("  "))
        val t = Conversation.titleFrom("Wie programmiere ich einen Conjugate-Block für Log und Yoke vor einem Wettkampf?")
        assertTrue(t, t.length <= 44 && t.endsWith("…"))
    }

    @Test
    fun focusDetection() {
        assertEquals(Focus.STRONGMAN, FocusDetector.detect("Wie baue ich Conjugate für Log und Yoke auf? Mein Kreuzheben stagniert."))
        assertEquals(Focus.DND, FocusDetector.detect("Lohnt sich Agonizing Blast für meinen Hexblade-Warlock?"))
        assertEquals(Focus.CODE, FocusDetector.detect("Schreib mir eine Kotlin-Funktion mit Coroutines"))
        assertEquals(Focus.SCIENCE, FocusDetector.detect("Leite den Erwartungswert des Hamilton-Operators im Grundzustand her"))
        assertEquals(Focus.SCIENCE, FocusDetector.detect("Berechne ∫ x² dx"))
        assertEquals(Focus.GENERAL, FocusDetector.detect("Hallo, wie geht's?"))
        assertEquals(Focus.DND, FocusDetector.detect("und dann?", previous = Focus.DND))
    }

    @Test
    fun calculatorAndNumberFormat() {
        assertEquals("2^10 + 1 = 1.025", ChatTools.calculate("2^10 + 1").card.body)
        assertTrue(ChatTools.calculate("sqrt(2)").card.body.startsWith("sqrt(2) = 1,414213562"))
        assertFalse(ChatTools.calculate("2 +* x").card.ok)
        assertEquals("−42", ChatTools.formatNumber(-42.0))
    }

    @Test
    fun diceToolRollsAndGivesTheExactDistribution() {
        assertEquals("2d6+3", ChatTools.normaliseDice("2w6+3"))
        val r = ChatTools.dice("2w6", Random(3))
        assertTrue(r.card.ok)
        val total = Regex("\\*\\*(\\d+)\\*\\*").find(r.card.body)!!.groupValues[1].toInt()
        assertTrue(total in 2..12)
        assertTrue(r.card.body, r.card.body.contains("Ø 7"))
        assertFalse(ChatTools.dice("2x9").card.ok)
        val dist = ChatTools.dice("4d6kh3", roll = false)
        assertTrue(dist.context, dist.context.contains("Ø 12,24"))
    }

    @Test
    fun strengthTools() {
        val r = ChatTools.oneRm("100x5")
        assertTrue(r.card.body, r.card.body.contains("e1RM **114,6 kg**"))
        assertTrue(ChatTools.oneRm("180 kg x 3 @8").card.ok)
        assertFalse(ChatTools.oneRm("100x20").card.ok)
        val p = ChatTools.plates("182,5")
        assertTrue(p.card.body, p.card.body.startsWith("Pro Seite: 25 + 25 + 25 + 5 + 1,25"))
    }

    @Test
    fun lookups() {
        assertEquals("Eldritch Blast", Knowledge.lookupDnd("eldritch blast")!!.title)
        assertEquals("Fireball", Knowledge.lookupDnd("firebal")!!.title)
        val mentions = Knowledge.dndMentions("Hex oder Hexblade's Curse? Und Agonizing Blast auf Eldritch Blast!").map { it.title }
        assertTrue(mentions.toString(), "Agonizing Blast" in mentions && "Eldritch Blast" in mentions)
        assertTrue(ChatTools.compendium("Fermi-Dirac-Verteilung")!!.card.body.contains("Fermi"))
        assertTrue(ChatTools.constant("Boltzmann").card.body.startsWith("k_B = 1,380649"))
        assertFalse(ChatTools.constant("Quatschkonstante").card.ok)
    }

    @Test
    fun routerRunsToolsOnlyOnClearSignals() {
        val set = ToolRouter.route("Heute 180 kg x 5 @8 im Kreuzheben, was heißt das?", null, null)
        assertEquals(Focus.STRONGMAN, set.focus)
        assertEquals("1rm", set.results.single().card.tool)
        assertFalse(set.direct)

        val roll = ToolRouter.route("Würfle 4d6kh3 für meine Attribute", null, null, Random(1))
        assertTrue(roll.results.any { it.card.tool == "w" && it.card.body.startsWith("Ergebnis") })

        val calc = ToolRouter.route("Was ist 17*23 = ?", null, null)
        assertTrue(calc.context, calc.context.contains("17*23 = 391"))

        val dnd = ToolRouter.route("Lohnt sich Agonizing Blast für meinen Hexblade?", null, null)
        assertTrue(dnd.results.any { it.card.title.endsWith("Agonizing Blast") })

        val sci = ToolRouter.route("Erkläre die Fermi-Dirac-Verteilung im Halbleiter", null, null)
        assertTrue(sci.results.any { it.card.tool == "formel" })

        val plain = ToolRouter.route("Hallo Maximus", null, null)
        assertTrue(plain.results.isEmpty())

        val cmd = ToolRouter.route("/zauber hex", null, null)
        assertTrue(cmd.direct)
        assertEquals("Zauber: Hex", cmd.results.single().card.title)
        assertTrue(ToolRouter.route("/hilfe", null, null).results.single().card.body.contains("/w"))
        assertFalse(ToolRouter.route("/gibtsnicht", null, null).direct)
    }

    @Test
    fun streamFilterSeparatesThinkingAndMarkers() {
        val f = StreamFilter()
        f.push("<think>Erst rechnen")
        assertEquals("", f.snapshot().visible)
        assertEquals("Erst rechnen", f.snapshot().thinking)
        f.push(": 2+2=4</think>Die Antwort ist 4.<end_of")
        assertEquals("Die Antwort ist 4.", f.snapshot().visible)
        assertFalse(f.snapshot().ended)
        f.push("_turn>Rest")
        assertTrue(f.snapshot().ended)
        assertEquals("Die Antwort ist 4.", f.snapshot().visible)

        val g = StreamFilter()
        g.push("Nur Denken ohne Öffnung</think>Antwort")
        assertEquals("Antwort", g.snapshot().visible)
        assertEquals("Nur Denken ohne Öffnung", g.snapshot().thinking)
    }

    @Test
    fun loopGuard() {
        assertTrue(StreamFilter.isLooping("Einleitung. " + "Das ist eine Wiederholung. ".repeat(3)))
        assertFalse(StreamFilter.isLooping("Ein ganz normaler Satz ohne Wiederholung, der einfach weitergeht und endet."))
        val f = StreamFilter()
        var looping = false
        repeat(40) { looping = looping || f.push("immer wieder dasselbe ").looping }
        assertTrue(looping)
    }

    @Test
    fun markdownBlocks() {
        val md = """
            ## Ergebnis
            Das ist **fett**, *kursiv* und `code`, aber 2 * 3 und snake_case bleiben.

            - erster Punkt
            - zweiter Punkt
            1. eins
            2. zwei

            | a | b |
            |---|---|
            | 1 | 2 |

            ${"$$"}E = mc^2${"$$"}
            > Zitat
            ```kotlin
            fun f() = 1
        """.trimIndent()
        val b = Markdown.parse(md)
        assertTrue(b[0] is MdBlock.Heading)
        val p = b[1] as MdBlock.Paragraph
        assertEquals(setOf(SpanStyle.BOLD), p.spans.first { it.text == "fett" }.styles)
        assertEquals(setOf(SpanStyle.ITALIC), p.spans.first { it.text == "kursiv" }.styles)
        assertEquals(setOf(SpanStyle.CODE), p.spans.first { it.text == "code" }.styles)
        assertTrue(p.spans.joinToString("") { it.text }.contains("2 * 3 und snake_case"))
        assertEquals(2, (b[2] as MdBlock.ListBlock).items.size)
        assertTrue((b[3] as MdBlock.ListBlock).ordered)
        assertEquals(1, (b[4] as MdBlock.Table).rows.size)
        assertEquals("E = mc²", (b[5] as MdBlock.Math).text)
        assertTrue(b[6] is MdBlock.Quote)
        val code = b[7] as MdBlock.Code
        assertEquals("kotlin", code.language)
        assertFalse(code.closed)
        assertEquals("fun f() = 1", code.code)
    }

    @Test
    fun latexToUnicode() {
        val s = Latex.toUnicode("\\frac{\\hbar^2}{2m}\\nabla^2\\psi + V\\psi = E\\psi")
        assertFalse(s, s.contains('\\'))
        assertTrue(s, s.contains("ħ²") && s.contains("∇²ψ"))
        assertEquals("x₀ ≤ √(a+b) · α", Latex.toUnicode("x_0 \\leq \\sqrt{a+b} \\cdot \\alpha"))
        assertEquals("ℝⁿ → ℂ", Latex.toUnicode("\\mathbb{R}^n \\to \\mathbb{C}"))
        assertEquals("E = mc²", Latex.carets("E = mc^2"))
        assertEquals("file_name", Latex.carets("file_name"))
    }

    @Test
    fun promptPlannerKeepsNewestHistoryWithinBudget() {
        val history = (1..40).flatMap { i ->
            listOf(ChatMessage(i * 2L, Role.USER, "Frage $i " + "x".repeat(200)), ChatMessage(i * 2L + 1, Role.ASSISTANT, "Antwort $i " + "y".repeat(400)))
        }
        val replay = PromptPlanner.replay(history, 600)
        assertTrue(PromptPlanner.estimateTokens(replay) <= 640)
        assertTrue(replay.contains("Antwort 40"))
        assertFalse(replay.contains("Frage 1 "))
        val opening = PromptPlanner.opening(Focus.DND, history, "Neue Frage", 4096)
        assertTrue(opening.startsWith(Focus.PERSONA))
        assertTrue(opening.endsWith("Neue Frage"))
        assertTrue(PromptPlanner.fits(1000, 200, 4096, 1024))
        assertFalse(PromptPlanner.fits(3000, 200, 4096, 1024))
        assertEquals("Exakte Ergebnisse und Nachschlagewerk der App:\nk\n\nFrage: q", PromptPlanner.turn("q", "k"))
        assertTrue(PromptPlanner.followUp(Focus.CODE, Focus.DND, "t").startsWith("[Fokus jetzt: Code."))
    }

    @Test
    fun modelCatalog() {
        assertEquals("gemma3n-e2b", ModelCatalog.profileFor("gemma-3n-E2B-it-int4.task").key)
        assertEquals("gemma3-1b", ModelCatalog.profileFor("gemma3-1b-it-int4.task").key)
        assertEquals(4096, ModelCatalog.maxTokensFor("Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.task"))
        assertEquals(1280, ModelCatalog.maxTokensFor("x_ekv1280.task"))
        assertTrue(ModelCatalog.profileFor("DeepSeek-R1-Distill-Qwen-1.5B_q8_ekv4096.task").thinking)
        assertTrue(ModelCatalog.isModelFile("a.task"))
        assertFalse(ModelCatalog.isModelFile("a.gguf"))
        assertEquals(listOf(4096, 2048, 1024), ModelCatalog.fallbackLadder(4096))
        assertEquals(listOf(8192, 4096, 2048, 1024), ModelCatalog.fallbackLadder(8192))
        assertNotNull(ChatTools.trainingSummary(listOf("Kreuzheben" to 260.0), 9))
        assertNull(ChatTools.trainingSummary(emptyList(), 0))
    }

    @Test
    fun codeHighlighter() {
        val code = "fun main() { val s = \"a // b\" // Kommentar\n  println(42 + Foo.bar) }"
        val t = CodeHighlighter.tokens(code, "kotlin")
        fun kindOf(word: String) = t.first { code.substring(it.start, it.end) == word }.kind
        assertEquals(CodeHighlighter.Kind.KEYWORD, kindOf("fun"))
        assertEquals(CodeHighlighter.Kind.STRING, kindOf("\"a // b\""))
        assertEquals(CodeHighlighter.Kind.COMMENT, kindOf("// Kommentar"))
        assertEquals(CodeHighlighter.Kind.NUMBER, kindOf("42"))
        assertEquals(CodeHighlighter.Kind.TYPE, kindOf("Foo"))
        val py = CodeHighlighter.tokens("def f(x):  # hi\n    return x2", "python")
        assertTrue(py.any { it.kind == CodeHighlighter.Kind.COMMENT })
        assertTrue(py.none { it.kind == CodeHighlighter.Kind.NUMBER }) // the 2 in x2 is part of a name
        // An unterminated string (streaming) ends at the line break instead of swallowing the rest.
        assertEquals(CodeHighlighter.Kind.KEYWORD, CodeHighlighter.tokens("val s = \"abc\nval", "kotlin").last().kind)
    }
}

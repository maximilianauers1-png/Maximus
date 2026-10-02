package app.maximus.notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownLiteTest {
    @Test fun blocks() {
        val src = "# Titel\nText eins\nText zwei\n\n- a\n  - b\n- [x] erledigt\n- [ ] offen\n3. drei\n> Zitat\n---\n```\ncode\n  x\n```"
        val b = MarkdownLite.parse(src)
        assertEquals(MdBlock.Heading(0, 1, "Titel"), b[0])
        assertEquals(MdBlock.Paragraph(1, "Text eins Text zwei"), b[1])
        assertTrue(b[2] is MdBlock.Blank)
        assertEquals(MdBlock.Bullet(4, 0, "a"), b[3])
        assertEquals(MdBlock.Bullet(5, 1, "b"), b[4])
        assertEquals(MdBlock.Task(6, 0, true, "erledigt"), b[5])
        assertEquals(MdBlock.Task(7, 0, false, "offen"), b[6])
        assertEquals(MdBlock.Numbered(8, 0, 3, "drei"), b[7])
        assertEquals(MdBlock.Quote(9, "Zitat"), b[8])
        assertEquals(MdBlock.Rule(10), b[9])
        assertEquals(MdBlock.Code(11, "code\n  x"), b[10])
    }

    @Test fun inlineSpans() {
        val s = MarkdownLite.inline("a **fett *kursiv*** `x*y` 2 * 3")
        assertEquals(MdSpan("a "), s[0])
        assertEquals(MdSpan("fett ", bold = true), s[1])
        assertEquals(MdSpan("kursiv", bold = true, italic = true), s[2])
        assertEquals(MdSpan(" "), s[3])
        assertEquals(MdSpan("x*y", code = true), s[4])
        assertEquals(MdSpan(" 2 * 3"), s[5])
    }

    @Test fun toggleTaskAndTags() {
        val src = "x\n  - [ ] eins\n- [X] zwei"
        assertEquals("x\n  - [x] eins\n- [X] zwei", MarkdownLite.toggleTask(src, 1))
        assertEquals("x\n  - [ ] eins\n- [ ] zwei", MarkdownLite.toggleTask(src, 2))
        assertEquals(src, MarkdownLite.toggleTask(src, 0))
        assertEquals(listOf("physik", "training"), MarkdownLite.normaliseTags("#Training, physik; training"))
    }
}

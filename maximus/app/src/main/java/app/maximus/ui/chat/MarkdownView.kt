package app.maximus.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle as TextSpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.maximus.chat.domain.CodeHighlighter
import app.maximus.chat.domain.MdBlock
import app.maximus.chat.domain.Span
import app.maximus.chat.domain.SpanStyle
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.SteelRule
import app.maximus.ui.theme.Palette

/** Colours for code and maths, tuned to the dark iron palette (all ≥ 4.5:1 on the code background). */
internal object ChatColors {
    val CodeBg = Color(0xFF0B0C0E)
    val CodeHeader = Color(0xFF16181B)
    val Keyword = Color(0xFFD7A6E8)
    val StringLit = Color(0xFFA9D49B)
    val Comment = Color(0xFF7D858E)
    val Number = Color(0xFFE6B57E)
    val Type = Color(0xFF8FC1E3)
    val InlineCodeBg = Color(0xFF23262A)
    val MathAccent = Color(0xFF8FA7BF)
}

private fun spansToAnnotated(spans: List<Span>, codeColor: Color): AnnotatedString = buildAnnotatedString {
    for (s in spans) {
        val style = TextSpanStyle(
            fontWeight = if (SpanStyle.BOLD in s.styles) FontWeight.SemiBold else null,
            fontStyle = if (SpanStyle.ITALIC in s.styles) FontStyle.Italic else null,
            fontFamily = if (SpanStyle.CODE in s.styles) FontFamily.Monospace else null,
            background = if (SpanStyle.CODE in s.styles) ChatColors.InlineCodeBg else Color.Unspecified,
            color = if (SpanStyle.CODE in s.styles) codeColor else Color.Unspecified,
            fontSize = if (SpanStyle.CODE in s.styles) 13.5.sp else androidx.compose.ui.unit.TextUnit.Unspecified
        )
        withStyle(style) { append(s.text) }
    }
}

/** Renders parsed Markdown blocks. [onCopy] receives the text of a code block. */
@Composable
fun MarkdownView(blocks: List<MdBlock>, onCopy: (String) -> Unit, modifier: Modifier = Modifier) {
    val body = MaterialTheme.typography.bodyLarge
    val codeColor = ChatColors.Number
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (b in blocks) {
            when (b) {
                is MdBlock.Heading -> Text(
                    spansToAnnotated(b.spans, codeColor),
                    style = if (b.level <= 2) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    color = Palette.SteelLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
                is MdBlock.Paragraph -> Text(spansToAnnotated(b.spans, codeColor), style = body)
                is MdBlock.ListBlock -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (item in b.items) {
                        Row(Modifier.padding(start = (item.indent * 16).dp)) {
                            Text(
                                item.marker, style = body, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.widthIn(min = if (b.ordered) 26.dp else 16.dp)
                            )
                            Text(spansToAnnotated(item.spans, codeColor), style = body)
                        }
                    }
                }
                is MdBlock.Code -> CodeBlock(b.language, b.code, b.closed, onCopy)
                is MdBlock.Quote -> Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                    Text(
                        spansToAnnotated(b.spans, codeColor), style = body.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 10.dp)
                    )
                }
                is MdBlock.Math -> MathBlock(b.text)
                is MdBlock.Table -> TableBlock(b, codeColor)
                MdBlock.Rule -> SteelRule()
            }
        }
    }
}

@Composable
private fun MathBlock(text: String) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(36.dp).background(ChatColors.MathAccent, RoundedCornerShape(2.dp)))
        Box(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontSize = 18.sp), color = Palette.SteelLight)
        }
    }
}

@Composable
private fun TableBlock(t: MdBlock.Table, codeColor: Color) {
    val outline = MaterialTheme.colorScheme.outlineVariant
    Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Column(Modifier.border(1.dp, outline, RoundedCornerShape(8.dp))) {
            (listOf(t.header) + t.rows).forEachIndexed { r, row ->
                Row(Modifier.background(if (r == 0) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent)) {
                    row.forEach { cell ->
                        Text(
                            spansToAnnotated(cell, codeColor),
                            style = if (r == 0) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.widthIn(min = 72.dp, max = 220.dp).padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(language: String, code: String, closed: Boolean, onCopy: (String) -> Unit) {
    // Highlighting is cached per text; while streaming the block re-lexes in O(n), only for the open block.
    val highlighted = remember(code, language) { highlight(code, language) }
    Column(
        Modifier.fillMaxWidth()
            .background(ChatColors.CodeBg, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().background(ChatColors.CodeHeader, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                language.ifEmpty { "code" } + if (!closed) "  …" else "",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f)
            )
            GlyphButton(Glyph.COPY, "Code kopieren", { onCopy(code) }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(highlighted, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 19.sp), color = Palette.Linen, softWrap = false)
        }
    }
}

internal fun highlight(code: String, language: String): AnnotatedString = buildAnnotatedString {
    append(code)
    for (t in CodeHighlighter.tokens(code, language)) {
        val color = when (t.kind) {
            CodeHighlighter.Kind.KEYWORD -> ChatColors.Keyword
            CodeHighlighter.Kind.STRING -> ChatColors.StringLit
            CodeHighlighter.Kind.COMMENT -> ChatColors.Comment
            CodeHighlighter.Kind.NUMBER -> ChatColors.Number
            CodeHighlighter.Kind.TYPE -> ChatColors.Type
        }
        addStyle(TextSpanStyle(color = color, fontStyle = if (t.kind == CodeHighlighter.Kind.COMMENT) FontStyle.Italic else null), t.start, t.end)
    }
}

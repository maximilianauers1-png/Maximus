package app.maximus.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import app.maximus.chat.domain.ChatController
import app.maximus.chat.domain.ChatMessage
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.GenStats
import app.maximus.chat.domain.Markdown
import app.maximus.chat.domain.ToolCard
import app.maximus.lab.domain.Fmt
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.theme.Design
import app.maximus.ui.theme.DesignConcept
import app.maximus.ui.theme.KnightAvatar
import app.maximus.ui.theme.Palette

/**
 * Focus accents. In the colour concepts only three heraldic tones are used (amber, red, silver-blue);
 * the classic design keeps one colour per focus.
 */
internal fun focusColor(f: Focus?): Color = if (Design.concept == DesignConcept.KLASSISCH) classicFocusColor(f) else when (f) {
    Focus.STRONGMAN, Focus.DND -> Color(0xFFE0565C)
    Focus.SCIENCE, Focus.NUTRITION -> Color(0xFFA9C4EE)
    Focus.CODE, Focus.GENERAL, null -> Color(0xFFFFB000)
}

private fun classicFocusColor(f: Focus?): Color = when (f) {
    Focus.STRONGMAN -> Palette.Heraldic
    Focus.DND -> Color(0xFFB9A3E3)
    Focus.CODE -> Color(0xFF8FC9A3)
    Focus.SCIENCE -> Palette.Blued
    Focus.NUTRITION -> Color(0xFFE6B57E)
    Focus.GENERAL, null -> Color(0xFFFFB000)
}

/** The user's input as a terminal line: green phosphor prompt "> " on a dark plate, right-aligned. */
@Composable
fun UserBubble(message: ChatMessage, onCopy: (String) -> Unit) {
    val green = MaterialTheme.colorScheme.secondary
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Row(
            Modifier.widthIn(max = 330.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                .border(1.dp, Brush.linearGradient(listOf(green.copy(alpha = 0.6f), MaterialTheme.colorScheme.outlineVariant)), RoundedCornerShape(12.dp, 12.dp, 2.dp, 12.dp))
                .clickable { onCopy(message.text) }
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text("> ", style = MaterialTheme.typography.bodyLarge, color = green)
            SelectionContainer { Text(message.text, style = MaterialTheme.typography.bodyLarge, color = Palette.Linen) }
        }
    }
}

/** Maximus's answer: header with focus, tool cards, collapsible reasoning, Markdown body and footer. */
@Composable
fun AssistantMessage(
    message: ChatMessage,
    showStats: Boolean,
    isLast: Boolean,
    onCopy: (String) -> Unit,
    onRegenerate: () -> Unit
) {
    val blocks = remember(message.text) { Markdown.parse(message.text) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistantHeader(message.focus, if (message.stopped) "abgebrochen" else null)
        message.tools.forEach { ToolCardView(it, onCopy) }
        if (message.thinking.isNotBlank()) ThinkingPanel(message.thinking, live = false)
        if (message.text.isNotBlank()) SelectionContainer { MarkdownView(blocks, onCopy) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val stats = message.stats
            if (showStats && stats != null) StatsLine(stats, Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
            if (message.text.isNotBlank()) GlyphButton(Glyph.COPY, "Antwort kopieren", { onCopy(Markdown.plain(blocks)) }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            if (isLast) GlyphButton(Glyph.REFRESH, "Neu antworten", onRegenerate, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The answer while it streams in. */
@Composable
fun LiveMessage(live: ChatController.Live, focus: Focus?, onCopy: (String) -> Unit) {
    val blocks = remember(live.text) { Markdown.parse(live.text) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistantHeader(focus, null)
        live.tools.forEach { ToolCardView(it, onCopy) }
        if (live.thinking.isNotBlank()) ThinkingPanel(live.thinking, live = live.text.isEmpty())
        when {
            live.phase == ChatController.Phase.LOADING -> TypingIndicator("lade modell")
            live.phase == ChatController.Phase.PREFILL -> TypingIndicator("lese eingabe")
            live.text.isEmpty() && live.thinking.isEmpty() -> TypingIndicator("denke nach")
            live.text.isNotEmpty() -> {
                MarkdownView(blocks, onCopy)
                BlinkingCursor()
            }
        }
    }
}

@Composable
private fun AssistantHeader(focus: Focus?, note: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        KnightAvatar(30.dp, border = MaterialTheme.colorScheme.primary)
        Text("MAXIMUS:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
        if (focus != null && focus != Focus.GENERAL) FocusBadge(focus, Modifier.padding(start = 8.dp))
        if (note != null) Text(note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
fun FocusBadge(focus: Focus, modifier: Modifier = Modifier) {
    val c = focusColor(focus)
    Text(
        focus.label, style = MaterialTheme.typography.labelSmall, color = c,
        modifier = modifier.border(1.dp, c.copy(alpha = 0.55f), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 1.dp)
    )
}

private fun toolGlyphLabel(tool: String): String = when (tool) {
    "rechne" -> "∑"
    "w" -> "⚄"
    "1rm", "scheiben" -> "⛁"
    "zauber" -> "✦"
    "formel" -> "∫"
    "konstante" -> "ħ"
    else -> "§"
}

/** An engraved plate with the exact result of an in-app tool; long bodies collapse to three lines. */
@Composable
fun ToolCardView(card: ToolCard, onCopy: (String) -> Unit) {
    var expanded by rememberSaveable(card.title, card.body) { mutableStateOf(card.body.length < 260) }
    val accent = if (card.ok) MaterialTheme.colorScheme.primary else Palette.Heraldic
    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(toolGlyphLabel(card.tool), style = MaterialTheme.typography.titleMedium, color = accent, modifier = Modifier.width(24.dp))
            Text(card.title, style = MaterialTheme.typography.labelLarge, color = accent, modifier = Modifier.weight(1f))
            Text(if (card.ok) "exakt" else "Hinweis", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        val blocks = remember(card.body) { Markdown.parse(card.body) }
        Box(Modifier.padding(top = 4.dp)) {
            if (expanded) MarkdownView(blocks, onCopy)
            else Text(Markdown.plain(blocks), style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThinkingPanel(text: String, live: Boolean) {
    var open by remember { mutableStateOf(false) }
    val words = remember(text) { text.split(Regex("\\s+")).count { it.isNotEmpty() } }
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(10.dp))
            .clickable { open = !open }.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            (if (live) "Denkt nach … " else "Gedankengang ") + "($words Wörter) " + if (open) "▴" else "▾",
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AnimatedVisibility(open, enter = expandVertically(), exit = shrinkVertically()) {
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun StatsLine(s: GenStats, modifier: Modifier) {
    val parts = buildList {
        if (s.decodeTokensPerSecond > 0) add("${Fmt.num(s.decodeTokensPerSecond, 3)} Tok/s")
        add("${Fmt.num(s.firstTokenMs / 1000.0, 2)} s bis zum 1. Token")
        add("${s.outputTokens} Tok")
        add(s.backend)
    }
    Text(parts.joinToString("  ·  "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = modifier)
}

/** Block cursor of a retro terminal, blinking at ≈ 1 Hz at the end of the streaming answer. */
@Composable
fun BlinkingCursor() {
    val t = rememberInfiniteTransition(label = "cursor")
    val on by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1000), RepeatMode.Restart), label = "blink")
    Box(Modifier.size(width = 10.dp, height = 18.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = if (on < 0.5f) 1f else 0f)))
}

/** Three phosphor lozenges pulsing in sequence, with a terminal status text. */
@Composable
fun TypingIndicator(label: String) {
    val t = rememberInfiniteTransition(label = "typing")
    val phase by t.animateFloat(0f, 3f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "phase")
    val amber = MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        Canvas(Modifier.size(width = 46.dp, height = 14.dp)) {
            for (k in 0 until 3) {
                val d = ((phase - k + 3) % 3)
                val a = (1f - (d / 1.5f)).coerceIn(0.25f, 1f)
                val cx = size.height / 2 + k * (size.width - size.height) / 2
                val r = size.height / 2 * (0.65f + 0.35f * a)
                val p = Path().apply { moveTo(cx, size.height / 2 - r); lineTo(cx + r, size.height / 2); lineTo(cx, size.height / 2 + r); lineTo(cx - r, size.height / 2); close() }
                drawPath(p, amber.copy(alpha = a))
            }
        }
        Text("$label …", style = MaterialTheme.typography.labelMedium, color = amber, modifier = Modifier.padding(start = 10.dp))
    }
}

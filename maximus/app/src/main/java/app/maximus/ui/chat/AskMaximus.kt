@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package app.maximus.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.ChatController
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.Role
import app.maximus.ui.theme.Design
import app.maximus.ui.theme.KnightAvatar
import app.maximus.ui.theme.ModuleStyle
import app.maximus.ui.theme.ModuleTheme
import app.maximus.ui.theme.Palette
import kotlinx.coroutines.launch

/**
 * A question to Maximus from inside a module. [context] is collected only when the question is sent
 * (it may read the database) and goes into the prompt, not into the visible chat.
 */
data class AskRequest(
    val title: String,
    val focus: Focus,
    val suggestions: List<String> = emptyList(),
    /** Pre-filled question; with [autoSend] it is asked immediately (e.g. "explain this quiz question"). */
    val question: String? = null,
    val autoSend: Boolean = false,
    val context: suspend () -> String = { "" }
)

/** Small steel pill for top bars: opens "Frag Maximus" for the current module. */
@Composable
fun AskMaximusButton(onClick: () -> Unit, label: String = "Maximus") {
    val terminal = Design.concept.palette(ModuleStyle.MAXIMUS)
    val accent = terminal?.primary ?: MaterialTheme.colorScheme.primary
    Row(
        Modifier.padding(end = 6.dp).clip(RoundedCornerShape(50))
            .background(terminal?.ground ?: MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, Brush.linearGradient(listOf(accent, terminal?.secondary ?: MaterialTheme.colorScheme.outline)), RoundedCornerShape(50))
            .clickable(onClick = onClick).padding(start = 3.dp, end = 12.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KnightAvatar(24.dp, phosphor = terminal?.terminal == true)
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontFamily = if (terminal != null) FontFamily.Monospace else null), color = accent,
            modifier = Modifier.padding(start = 6.dp))
    }
}

/**
 * Bottom sheet: ask, watch the answer stream in, then continue in the full chat if wanted. The question
 * becomes a normal saved conversation, so nothing is lost when the sheet closes.
 */
@Suppress("DEPRECATION")
@Composable
fun AskMaximusSheet(service: ChatService, request: AskRequest, onDismiss: () -> Unit, onOpenChat: () -> Unit) {
    val controller = service.controller
    val state by controller.state.collectAsState()
    val settings by service.settings.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var conversationId by remember(request) { mutableStateOf<Long?>(null) }
    var input by remember(request) { mutableStateOf(request.question.orEmpty()) }
    var preparing by remember(request) { mutableStateOf(false) }
    var blocked by remember(request) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val copy: (String) -> Unit = { clipboard.setText(AnnotatedString(it)) }

    fun ask(question: String) {
        if (question.isBlank() || preparing) return
        preparing = true
        scope.launch {
            val context = runCatching { request.context() }.getOrDefault("")
            val id = controller.ask(question, context, request.focus)
            blocked = id == null
            if (id != null) conversationId = id
            preparing = false
        }
    }

    // Warm the model while the sheet is open, release later like the chat screen does.
    LaunchedEffect(request) {
        service.ensureSettings()
        controller.onVisible()
        if (request.autoSend && !request.question.isNullOrBlank()) ask(request.question)
    }
    DisposableEffect(Unit) { onDispose { controller.onHidden() } }

    ModuleTheme(ModuleStyle.MAXIMUS) {
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(
                Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KnightAvatar(40.dp, border = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text("Frag Maximus", style = MaterialTheme.typography.titleLarge)
                        Text(request.title + " · lokal auf dem Gerät", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                    FocusBadge(request.focus)
                }

                val conv = state.conversation?.takeIf { it.id == conversationId }
                if (conversationId == null) {
                    OutlinedTextField(
                        input, { input = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 6,
                        placeholder = { Text("Deine Frage …") }, shape = RoundedCornerShape(16.dp)
                    )
                    if (request.suggestions.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            request.suggestions.forEach { s ->
                                Text(
                                    s, style = MaterialTheme.typography.labelMedium, color = focusColor(request.focus),
                                    modifier = Modifier.clip(RoundedCornerShape(50)).border(1.dp, focusColor(request.focus).copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .clickable { input = s; ask(s) }.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (blocked) Text("Maximus beantwortet gerade noch eine andere Frage. Kurz warten oder im Chat stoppen.",
                        style = MaterialTheme.typography.bodySmall, color = Palette.Heraldic)
                    Button(onClick = { ask(input) }, enabled = input.isNotBlank() && !preparing, modifier = Modifier.fillMaxWidth()) {
                        Text(if (preparing) "Sammle Kontext …" else "Fragen")
                    }
                    Text("Maximus bekommt automatisch die passenden Daten aus diesem Bereich mit. Alles bleibt auf dem Gerät.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                } else if (conv != null) {
                    conv.messages.firstOrNull { it.role == Role.USER }?.let { UserBubble(it, copy) }
                    val live = state.live
                    val answer = conv.messages.lastOrNull { it.role == Role.ASSISTANT }
                    when {
                        live != null -> LiveMessage(live, request.focus, copy)
                        answer != null -> AssistantMessage(answer, settings.showStats, isLast = false, onCopy = copy, onRegenerate = {})
                    }
                    if (live == null && answer != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = onOpenChat, modifier = Modifier.weight(1f)) {
                                Text(if (answer.text == ChatController.NO_MODEL) "Modell einrichten" else "Im Chat weiterfragen")
                            }
                            OutlinedButton(onClick = { conversationId = null; input = "" }, modifier = Modifier.weight(1f)) { Text("Neue Frage") }
                        }
                    } else if (live != null) {
                        OutlinedButton(onClick = { controller.stop() }, modifier = Modifier.fillMaxWidth()) { Text("Stopp") }
                    }
                }
            }
        }
    }
}

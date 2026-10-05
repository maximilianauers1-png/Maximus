package app.maximus.ui.chat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.drawWithCache
import app.maximus.ui.theme.KnightAvatar
import app.maximus.ui.theme.LocalModuleStyle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.ChatTools
import app.maximus.chat.domain.Conversation
import app.maximus.chat.domain.EngineState
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.ModelCatalog
import app.maximus.chat.domain.Role
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.MaximusTopBar
import app.maximus.ui.components.SteelRule
import app.maximus.ui.theme.Palette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Quick tools above the input: label and the command they insert. */
private val TOOL_CHIPS = listOf("⚄ Würfeln" to "/w ", "⛁ 1RM" to "/1rm ", "⛁ Scheiben" to "/scheiben ", "∑ Rechnen" to "/rechne ",
    "✦ D&D" to "/zauber ", "∫ Formel" to "/formel ", "ħ Konstante" to "/konstante ")

/** Starter prompts on an empty chat, one per field. */
private val STARTERS = listOf(
    Focus.STRONGMAN to "Plane mir einen 6-Wochen-Block für Log und Yoke (2 Einheiten pro Woche).",
    Focus.DND to "Hexblade 3 / Sorcerer 17 gegen Sorcadin: was macht mehr Schaden auf Stufe 11?",
    Focus.SCIENCE to "Leite die Fermi-Dirac-Verteilung aus dem großkanonischen Ensemble her.",
    Focus.CODE to "Erkläre Kotlin-Flows: cold vs. hot, mit kurzem Beispiel.",
    Focus.SCIENCE to "Beweise den Satz von Stokes für Differentialformen in Kurzfassung."
)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(service: ChatService, onBack: () -> Unit) {
    val controller = service.controller
    val state by controller.state.collectAsState()
    val engine by controller.engine.collectAsState()
    val settings by service.settings.collectAsState()
    val conversations by service.repository.conversations.collectAsState(initial = emptyList())
    var showModel by rememberSaveable { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    var toast by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Warm the model while the screen is open; release it some time after leaving (see ChatSettings).
    DisposableEffect(Unit) { onDispose { controller.onHidden() } }
    LaunchedEffect(settings.model?.path) {
        service.ensureSettings()
        controller.onVisible()
    }
    LaunchedEffect(toast) { if (toast != null) { delay(1400); toast = null } }

    val copy: (String) -> Unit = { text -> clipboard.setText(AnnotatedString(text)); toast = "Kopiert" }

    if (showModel) {
        BackHandler { showModel = false }
        ModelScreen(service, onBack = { showModel = false })
        return
    }

    val conversation = state.conversation
    val messages = conversation?.messages.orEmpty()
    val live = state.live
    val busy = live != null
    val listState = rememberLazyListState()
    // Reverse layout: index 0 is the newest item, so the view stays glued to the bottom while text streams in.
    val reversed = remember(messages) { messages.asReversed() }
    val scrolledUp by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(0) }

    fun send(text: String) {
        if (controller.send(text)) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            input = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                MaximusTopBar(
                    title = conversation?.title?.takeIf { messages.isNotEmpty() } ?: "Maximus",
                    onBack = onBack,
                    onTitleClick = { showHistory = true }
                ) {
                    GlyphButton(Glyph.HISTORY, "Verlauf", { showHistory = true }, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    GlyphButton(Glyph.PLUS, "Neuer Chat", { controller.open(null); input = "" }, tint = MaterialTheme.colorScheme.onSurfaceVariant, enabled = !busy)
                    GlyphButton(Glyph.CHIP, "Modell und Einstellungen", { showModel = true }, tint = MaterialTheme.colorScheme.primary)
                }
                EngineStrip(engine, settings.model?.fileName, onClick = { showModel = true })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            val terminal = LocalModuleStyle.current?.terminal == true
            Box(Modifier.weight(1f).fillMaxWidth().then(if (terminal) Modifier.crtScanlines() else Modifier)) {
                if (messages.isEmpty() && live == null) {
                    Welcome(hasModel = settings.model != null, onStarter = { send(it) }, onSetup = { showModel = true })
                } else {
                    LazyColumn(
                        state = listState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.Bottom)
                    ) {
                        if (live != null) item(key = "live", contentType = "live") { LiveMessage(live, messages.lastOrNull()?.focus, copy) }
                        items(reversed, key = { it.id }, contentType = { it.role }) { m ->
                            if (m.role == Role.USER) UserBubble(m, copy)
                            else AssistantMessage(m, settings.showStats, isLast = !busy && m.id == messages.last().id, onCopy = copy, onRegenerate = { controller.regenerate() })
                        }
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    scrolledUp, enter = fadeIn(), exit = fadeOut(),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape).clickable { scope.launch { listState.animateScrollToItem(0) } },
                        contentAlignment = Alignment.Center
                    ) { Text("↓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                }
                toast?.let {
                    Text(
                        it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Composer(
                input = input,
                onInput = { input = it },
                busy = busy,
                focusMode = conversation?.focusMode,
                onFocusMode = { controller.setFocusMode(it) },
                onSend = { send(input) },
                onStop = { controller.stop() }
            )
        }
    }

    if (showHistory) {
        ModalBottomSheet(onDismissRequest = { showHistory = false }, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
            HistorySheet(
                conversations = conversations,
                currentId = conversation?.id,
                onOpen = { c -> if (!busy) { controller.open(c); showHistory = false } },
                onPin = { c -> scope.launch { service.repository.save(c.copy(pinned = !c.pinned)) } },
                onDelete = { c ->
                    scope.launch {
                        service.repository.delete(c.id)
                        if (c.id == conversation?.id && !busy) controller.open(null)
                    }
                }
            )
        }
    }
}

/** Engine status under the top bar: coloured dot, state and model. */
@Composable
private fun EngineStrip(engine: EngineState, modelName: String?, onClick: () -> Unit) {
    val (color, text) = when (engine) {
        is EngineState.Ready -> Color(0xFF8FC9A3) to "Bereit · ${engine.info.backend.label} · ${engine.info.maxTokens} Tok Kontext · ${ModelCatalog.profileFor(engine.model.fileName).name}"
        is EngineState.Loading -> Color(0xFFE6B57E) to "Lädt ${ModelCatalog.profileFor(engine.model.fileName).name} (${engine.backend.label}) …"
        is EngineState.Failed -> Palette.Heraldic to "Fehler: ${engine.message}"
        EngineState.Idle -> if (modelName == null) Palette.SteelDeep to "Kein Modell · nur Werkzeuge · tippen zum Einrichten"
        else Palette.SteelDeep to "Ruht · ${ModelCatalog.profileFor(modelName).name} · lädt bei der ersten Frage"
    }
    val pulse = if (engine is EngineState.Loading) {
        val t = rememberInfiniteTransition(label = "dot")
        t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a").value
    } else 1f
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(8.dp)) { drawCircle(color.copy(alpha = pulse)) }
        Text(
            text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp).weight(1f)
        )
        Text("🔒 offline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

/** Boot lines of the retro terminal, typed out one after another. */
@Composable
private fun BootLog() {
    val lines = listOf(
        "> BOOT MAXIMUS ............ OK",
        "> NETZWERK ............... AUS (keine Berechtigung)",
        "> WERKZEUGE .............. 8 geladen",
        "> KOMPENDIUM ............. bereit",
        "> WARTE AUF EINGABE"
    )
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (shown < lines.size) { delay(180); shown++ } }
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)).padding(12.dp)
    ) {
        lines.take(shown).forEachIndexed { i, l ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(l, style = MaterialTheme.typography.labelMedium, color = if (i == lines.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                if (i == lines.lastIndex) { Spacer(Modifier.width(6.dp)); BlinkingCursor() }
            }
        }
    }
}

@Composable
private fun Welcome(hasModel: Boolean, onStarter: (String) -> Unit, onSetup: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KnightAvatar(84.dp, border = MaterialTheme.colorScheme.primary)
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    "MAXIMUS",
                    style = MaterialTheme.typography.headlineLarge.copy(brush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))))
                )
                Text("RITTER-TERMINAL · v2", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
        BootLog()
        Text(
            "Dein Berater für Strongman, D&D, Code, Mathe und Physik. Läuft komplett auf diesem Gerät: keine Cloud, keine Internet-Berechtigung.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasModel) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)).clickable(onClick = onSetup).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlyphIcon(Glyph.CHIP, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Sprachmodell einrichten", style = MaterialTheme.typography.titleMedium)
                    Text("Empfohlen für das Galaxy A55: Gemma 3n E2B. Die Werkzeuge unten funktionieren schon jetzt.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                GlyphIcon(Glyph.CHEVRON_RIGHT, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
            }
        }
        Text("Vorschläge", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 6.dp))
        STARTERS.forEach { (focus, prompt) ->
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .clickable { onStarter(prompt) }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(width = 3.dp, height = 30.dp).background(focusColor(focus), RoundedCornerShape(2.dp)))
                Text(prompt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
            }
        }
        Text("Werkzeuge ohne Modell, sofort und exakt: " + ChatTools.COMMANDS.dropLast(1).joinToString("  ") { "/" + it.names.first() },
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun Composer(
    input: String,
    onInput: (String) -> Unit,
    busy: Boolean,
    focusMode: Focus?,
    onFocusMode: (Focus?) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    val slash = input.startsWith("/") && ' ' !in input
    val matches = if (slash) ChatTools.COMMANDS.filter { c -> c.names.any { it.startsWith(input.drop(1).lowercase()) } } else emptyList()
    Column(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLowest).navigationBarsPadding()
    ) {
        SteelRule(lozenge = false)
        AnimatedVisibility(matches.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                matches.forEach { c ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onInput("/" + c.names.first() + " ") }.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(c.usage, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(170.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(c.help, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                }
            }
        }
        // Focus selector, then (with an empty input) the tool shortcuts.
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FocusChip("Auto", focusMode == null, MaterialTheme.colorScheme.primary) { onFocusMode(null) }
            Focus.entries.filter { it != Focus.GENERAL }.forEach { f -> FocusChip(f.label, focusMode == f, focusColor(f)) { onFocusMode(if (focusMode == f) null else f) } }
            if (input.isEmpty()) TOOL_CHIPS.forEach { (label, cmd) -> FocusChip(label, false, MaterialTheme.colorScheme.outline) { onInput(cmd) } }
        }
        Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, bottom = 8.dp, top = 2.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(
                value = input,
                onValueChange = onInput,
                placeholder = { Text("> frag maximus … oder /hilfe") },
                modifier = Modifier.weight(1f),
                maxLines = 6,
                shape = RoundedCornerShape(22.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
            val enabled = busy || input.isNotBlank()
            Box(
                Modifier.padding(start = 8.dp, bottom = 4.dp).size(48.dp).clip(CircleShape)
                    .background(
                        if (busy) Brush.linearGradient(listOf(Palette.Heraldic, Color(0xFF8E4F4A)))
                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))),
                        alpha = if (enabled) 1f else 0.35f
                    )
                    .clickable(enabled = enabled) { if (busy) onStop() else onSend() },
                contentAlignment = Alignment.Center
            ) {
                GlyphIcon(if (busy) Glyph.STOP else Glyph.SEND, tint = MaterialTheme.colorScheme.onPrimary, size = 22.dp)
            }
        }
    }
}

@Composable
private fun FocusChip(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimary else color) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected, borderColor = color.copy(alpha = 0.45f))
    )
}

private val DATE = DateTimeFormatter.ofPattern("d. MMM, HH:mm")

@Composable
private fun HistorySheet(
    conversations: List<Conversation>,
    currentId: Long?,
    onOpen: (Conversation) -> Unit,
    onPin: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var armed by remember { mutableStateOf<Long?>(null) }
    val shown = remember(conversations, query) {
        if (query.isBlank()) conversations
        else conversations.filter { c -> c.title.contains(query, true) || c.messages.any { it.text.contains(query, true) } }
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
        Text("Verlauf", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(query, { query = it }, placeholder = { Text("Chats durchsuchen") }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), shape = RoundedCornerShape(14.dp))
        if (shown.isEmpty()) Text("Noch keine Chats.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))
        LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(shown, key = { it.id }) { c ->
                val focus = c.focusMode ?: c.messages.lastOrNull { it.focus != null }?.focus
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (c.id == currentId) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onOpen(c) }.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(width = 3.dp, height = 34.dp).background(focusColor(focus), RoundedCornerShape(2.dp)))
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(c.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            Instant.ofEpochMilli(c.updatedAt).atZone(ZoneId.systemDefault()).format(DATE) + " · ${c.messages.count { it.role == Role.USER }} Fragen",
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline
                        )
                    }
                    GlyphButton(if (c.pinned) Glyph.STAR_FILLED else Glyph.PIN, if (c.pinned) "Lösen" else "Anheften", { onPin(c) },
                        tint = if (c.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    GlyphButton(Glyph.TRASH, if (armed == c.id) "Wirklich löschen" else "Löschen", {
                        if (armed == c.id) { onDelete(c); armed = null } else armed = c.id
                    }, tint = if (armed == c.id) Palette.Heraldic else MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

/**
 * CRT scanlines over the terminal area: one dark line every 3 px. The path is built once per size
 * (drawWithCache), so streaming text does not rebuild it; drawing is a single path per frame.
 */
private fun Modifier.crtScanlines(): Modifier = drawWithCache {
    val lines = Path().apply {
        var y = 0f
        while (y < size.height) { moveTo(0f, y); lineTo(size.width, y); y += 3f }
    }
    val vignette = Brush.radialGradient(
        listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
        center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), radius = size.maxDimension * 0.75f
    )
    onDrawWithContent {
        drawContent()
        drawPath(lines, Color.Black.copy(alpha = 0.16f), style = Stroke(width = 1f))
        drawRect(vignette)
    }
}

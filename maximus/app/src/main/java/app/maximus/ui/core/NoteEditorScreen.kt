package app.maximus.ui.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.notes.domain.MarkdownLite
import app.maximus.notes.domain.MdBlock
import app.maximus.ui.components.SteelRule
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.MaximusTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(services: AppServices, noteId: Long, onBack: () -> Unit) {
    val repo = services.notes
    val scope = rememberCoroutineScope()
    var id by remember { mutableLongStateOf(noteId) }
    var loaded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var pinned by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        if (noteId == 0L) {
            id = repo.createEmpty()
        } else {
            repo.get(noteId)?.let { n ->
                title = n.title; body = n.body; tags = n.tags.replace(",", ", "); pinned = n.pinned
                preview = n.body.isNotBlank()
            }
        }
        loaded = true
    }
    // Debounced autosave (700 ms after the last keystroke).
    LaunchedEffect(title, body, tags, pinned, loaded) {
        if (!loaded || id == 0L) return@LaunchedEffect
        delay(700)
        repo.persistAsync(id, title, body, tags, pinned)
    }
    val latest by rememberUpdatedState(listOf(title, body, tags))
    val latestPinned by rememberUpdatedState(pinned)
    val latestDeleted by rememberUpdatedState(deleted)
    DisposableEffect(Unit) {
        onDispose {
            if (id != 0L && !latestDeleted) repo.closeEditorAsync(id, latest[0], latest[1], latest[2], latestPinned)
        }
    }

    Scaffold(
        topBar = {
            MaximusTopBar(
                title = title.ifBlank { stringResource(R.string.notes_untitled) },
                onBack = onBack,
                actions = {
                    GlyphButton(
                        if (pinned) Glyph.STAR_FILLED else Glyph.STAR, stringResource(R.string.notes_pin), { pinned = !pinned },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 16.dp)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                SegmentedButton(selected = !preview, onClick = { preview = false }, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text(stringResource(R.string.notes_edit)) }
                SegmentedButton(selected = preview, onClick = { preview = true }, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text(stringResource(R.string.notes_preview)) }
            }
            if (preview) {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (title.isNotBlank()) {
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        SteelRule(lozenge = false, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    MarkdownView(body) { line -> body = MarkdownLite.toggleTask(body, line) }
                    Spacer(Modifier.height(24.dp))
                }
            } else {
                val fieldColors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                )
                androidx.compose.material3.TextField(
                    title, { title = it }, placeholder = { Text(stringResource(R.string.notes_title)) },
                    textStyle = MaterialTheme.typography.headlineMedium, singleLine = true, colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(tags, { tags = it }, label = { Text(stringResource(R.string.notes_tags)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                androidx.compose.material3.TextField(
                    body, { body = it }, placeholder = { Text(stringResource(R.string.notes_body_hint)) },
                    textStyle = MaterialTheme.typography.bodyLarge, colors = fieldColors,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.End) {
                val words = body.split(Regex("\\s+")).count { it.isNotBlank() }
                Text(stringResource(R.string.notes_words, words), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f).align(Alignment.CenterVertically))
                TextButton(onClick = { confirmDelete = true }) { Text(stringResource(R.string.delete)) }
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.notes_delete_title)) },
            text = { Text(stringResource(R.string.notes_delete_text)) },
            confirmButton = {
                TextButton(onClick = {
                    deleted = true
                    confirmDelete = false
                    scope.launch { repo.delete(id); onBack() }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
fun MarkdownView(source: String, onToggleTask: (Int) -> Unit) {
    val blocks = remember(source) { MarkdownLite.parse(source) }
    val cs = MaterialTheme.colorScheme
    val ty = MaterialTheme.typography
    val codeStyle = SpanStyle(fontFamily = FontFamily.Monospace, background = cs.surfaceContainerHigh, color = cs.secondary)
    fun rich(text: String): AnnotatedString = buildAnnotatedString {
        for (s in MarkdownLite.inline(text)) {
            val style = when {
                s.code -> codeStyle
                else -> SpanStyle(
                    fontWeight = if (s.bold) FontWeight.SemiBold else null,
                    fontStyle = if (s.italic) FontStyle.Italic else null
                )
            }
            withStyle(style) { append(s.text) }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (b in blocks) {
            when (b) {
                is MdBlock.Heading -> Text(
                    rich(b.text),
                    style = when (b.level) { 1 -> ty.headlineSmall; 2 -> ty.titleLarge; else -> ty.titleMedium },
                    color = if (b.level == 1) cs.primary else cs.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                is MdBlock.Paragraph -> Text(rich(b.text), style = ty.bodyLarge)
                is MdBlock.Bullet -> Row(Modifier.padding(start = (b.depth * 16).dp)) {
                    Text(if (b.depth == 0) "•" else "◦", color = cs.primary, modifier = Modifier.width(18.dp))
                    Text(rich(b.text), style = ty.bodyLarge)
                }
                is MdBlock.Numbered -> Row(Modifier.padding(start = (b.depth * 16).dp)) {
                    Text("${b.number}.", color = cs.primary, style = ty.bodyLarge, modifier = Modifier.width(28.dp))
                    Text(rich(b.text), style = ty.bodyLarge)
                }
                is MdBlock.Task -> Row(Modifier.padding(start = (b.depth * 16).dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = b.done, onCheckedChange = { onToggleTask(b.line) })
                    Text(
                        rich(b.text),
                        style = ty.bodyLarge.copy(textDecoration = if (b.done) TextDecoration.LineThrough else null),
                        color = if (b.done) cs.onSurfaceVariant else cs.onSurface
                    )
                }
                is MdBlock.Quote -> Row(Modifier.padding(vertical = 2.dp)) {
                    Box(Modifier.width(3.dp).height(24.dp).background(cs.primary))
                    Text(rich(b.text), style = ty.bodyLarge.copy(fontStyle = FontStyle.Italic), color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 10.dp))
                }
                is MdBlock.Code -> Text(
                    b.text,
                    style = ty.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = cs.secondary,
                    modifier = Modifier.fillMaxWidth().background(cs.surfaceContainerHigh, MaterialTheme.shapes.small).padding(10.dp)
                )
                is MdBlock.Rule -> SteelRule(modifier = Modifier.padding(vertical = 6.dp))
                is MdBlock.Blank -> Spacer(Modifier.height(4.dp))
            }
        }
    }
}

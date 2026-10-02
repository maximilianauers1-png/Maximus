@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.notes.data.NoteEntity
import app.maximus.notes.domain.MarkdownLite
import app.maximus.notes.domain.MdBlock
import app.maximus.ui.components.EmptyState
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.PlateCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NotesTab(services: AppServices, onOpenNote: (Long) -> Unit) {
    val notes by services.notes.notes.collectAsState(initial = emptyList())
    var query by rememberSaveable { mutableStateOf("") }
    var tag by rememberSaveable { mutableStateOf<String?>(null) }
    val allTags = notes.flatMap { it.tagList() }.distinct().sorted()
    val q = query.trim().lowercase()
    val filtered = notes.filter { n ->
        (tag == null || tag in n.tagList()) &&
            (q.isEmpty() || n.title.lowercase().contains(q) || n.body.lowercase().contains(q) || n.tags.contains(q))
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.search)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        if (allTags.isNotEmpty()) item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allTags.forEach { t -> FilterChip(tag == t, { tag = if (tag == t) null else t }, label = { Text("#$t") }) }
            }
        }
        item { Button(onClick = { onOpenNote(0L) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.notes_new)) } }
        if (filtered.isEmpty()) item { EmptyState(stringResource(R.string.notes_empty)) }
        items(filtered, key = { it.id }) { n -> NoteRow(n) { onOpenNote(n.id) } }
    }
}

fun NoteEntity.tagList(): List<String> = if (tags.isBlank()) emptyList() else tags.split(',')

/** First non-empty text line, with Markdown markers stripped, for the list preview. */
private fun preview(body: String): String = MarkdownLite.parse(body).firstNotNullOfOrNull { b ->
    when (b) {
        is MdBlock.Paragraph -> b.text
        is MdBlock.Heading -> b.text
        is MdBlock.Bullet -> b.text
        is MdBlock.Numbered -> b.text
        is MdBlock.Task -> (if (b.done) "☑ " else "☐ ") + b.text
        is MdBlock.Quote -> b.text
        else -> null
    }
}?.let { t -> MarkdownLite.inline(t).joinToString("") { it.text } } ?: ""

@Composable
private fun NoteRow(n: NoteEntity, onClick: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val date = Instant.ofEpochMilli(n.updatedAt).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale))
    val tasks = MarkdownLite.parse(n.body).filterIsInstance<MdBlock.Task>()
    PlateCard(onClick = onClick) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(n.title.ifBlank { stringResource(R.string.notes_untitled) }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (n.pinned) GlyphIcon(Glyph.STAR_FILLED, tint = MaterialTheme.colorScheme.primary, size = 16.dp)
            }
            val p = preview(n.body)
            if (p.isNotBlank()) Text(p, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val meta = buildList {
                add(date)
                if (tasks.isNotEmpty()) add("${tasks.count { it.done }}/${tasks.size} ✓")
                addAll(n.tagList().map { "#$it" })
            }.joinToString("   ")
            Text(meta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

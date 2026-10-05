package app.maximus.ui.strongman

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.Focus
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.MaximusTopBar

/** The three areas of the strongman module: logging and tools, the pocket coach, and the game with statistics. */
private enum class SmSection(val title: String, val subtitle: String, val tabs: List<String>) {
    TRAINING("Training", "Loggen · Pläne · Werkzeuge", listOf("Training", "Programme", "Rechner", "Werkzeuge", "Bibliothek")),
    COACH("Coach", "Heute · Wettkampf · Systeme", listOf("Heute", "Wettkampf", "Systeme", "Schwachstellen")),
    ARENA("Arena", "Ränge · Ruhm · Statistik", listOf("Rang", "Vergleich", "Ruhm", "Progression", "Statistik", "Rekorde"))
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrongmanHubScreen(
    repository: StrongmanRepository,
    onBack: () -> Unit,
    onOpenProgram: (Long) -> Unit,
    chat: ChatService? = null,
    onOpenChat: () -> Unit = {}
) {
    var sectionIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(rememberSaveable { mutableIntStateOf(0) }, rememberSaveable { mutableIntStateOf(0) }, rememberSaveable { mutableIntStateOf(0) })
    var ask by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { repository.ensureSeeded() }
    val section = SmSection.entries[sectionIndex]
    val tab = tabs[sectionIndex].intValue.coerceIn(0, section.tabs.lastIndex)
    fun selectTab(i: Int) { tabs[sectionIndex].intValue = i }

    Scaffold(
        topBar = {
            MaximusTopBar(title = stringResource(R.string.module_strongman), onBack = onBack) {
                if (chat != null) AskMaximusButton(onClick = { ask = true })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmSection.entries.forEachIndexed { i, s -> SectionTile(s.title, s.subtitle, i == sectionIndex, Modifier.weight(1f)) { sectionIndex = i } }
            }
            ScrollableTabRow(
                selectedTabIndex = tab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                section.tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { selectTab(i) }, text = { Text(t) }) }
            }
            when (section) {
                SmSection.TRAINING -> when (tab) {
                    0 -> TrainingTab(repository)
                    1 -> ProgramsTab(repository, onOpenProgram)
                    2 -> CalculatorTab(repository)
                    3 -> ToolsTab(repository)
                    else -> LibraryTab(repository)
                }
                SmSection.COACH -> when (tab) {
                    0 -> CoachTodayTab(repository)
                    1 -> CoachMeetTab(repository)
                    2 -> CoachSystemsTab(repository)
                    else -> CoachWeakTab(repository)
                }
                SmSection.ARENA -> when (tab) {
                    0 -> ArenaRankTab(repository)
                    1 -> ArenaCompareTab(repository)
                    2 -> ArenaGloryTab(repository)
                    3 -> ArenaProgressionTab(repository)
                    4 -> HistoryTab(repository)
                    else -> RecordsTab(repository)
                }
            }
        }
    }
    if (ask && chat != null) {
        AskMaximusSheet(
            chat,
            AskRequest(
                "Strongman", Focus.STRONGMAN,
                listOf("Analysiere meine letzten Einheiten", "Wie breche ich mein Plateau im Kreuzheben?", "Wann sollte ich einen Deload machen?",
                    "Wie peake ich als Naturalathlet für einen OSG-Wettkampf?", "Technik-Check: Log Clean and Press",
                    "Wie verbessere ich meine Griffkraft für Farmer's Walk?"),
                context = { chat.strongmanContext() }
            ),
            onDismiss = { ask = false },
            onOpenChat = { ask = false; onOpenChat() }
        )
    }
}

@Composable
private fun SectionTile(title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (selected) Brush.verticalGradient(listOf(cs.primary.copy(alpha = 0.28f), cs.surfaceContainerHigh)) else Brush.verticalGradient(listOf(cs.surfaceContainerLow, cs.surfaceContainerLow)))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Brush.linearGradient(listOf(cs.primary, cs.secondary)) else Brush.linearGradient(listOf(cs.outlineVariant, cs.outlineVariant)), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (selected) cs.primary else cs.onSurface,
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(subtitle, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2,
            modifier = Modifier.fillMaxWidth())
    }
}

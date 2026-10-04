package app.maximus.ui.strongman

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import app.maximus.R
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.Focus
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.MaximusTopBar
import app.maximus.strongman.data.StrongmanRepository

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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var ask by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { repository.ensureSeeded() }
    val titles = listOf(
        R.string.sm_tab_calc, R.string.sm_tab_programs, R.string.sm_tab_training,
        R.string.sm_tab_history, R.string.sm_tab_records, R.string.sm_tab_tools, R.string.sm_tab_library
    )
    Scaffold(
        topBar = {
            MaximusTopBar(title = stringResource(R.string.module_strongman), onBack = onBack) {
                if (chat != null) AskMaximusButton(onClick = { ask = true })
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = tab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                titles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(t)) }) }
            }
            when (tab) {
                0 -> CalculatorTab(repository)
                1 -> ProgramsTab(repository, onOpenProgram)
                2 -> TrainingTab(repository)
                3 -> HistoryTab(repository)
                4 -> RecordsTab(repository)
                5 -> ToolsTab(repository)
                else -> LibraryTab(repository)
            }
        }
    }
    if (ask && chat != null) {
        AskMaximusSheet(
            chat,
            AskRequest(
                "Strongman", Focus.STRONGMAN,
                listOf("Analysiere meine letzten Einheiten", "Wie breche ich mein Plateau im Kreuzheben?", "Wann sollte ich einen Deload machen?",
                    "Technik-Check: Log Clean and Press", "Wie verbessere ich meine Griffkraft für Farmer's Walk?"),
                context = { chat.strongmanContext() }
            ),
            onDismiss = { ask = false },
            onOpenChat = { ask = false; onOpenChat() }
        )
    }
}

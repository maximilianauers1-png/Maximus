package app.maximus.ui.strongman

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.Focus
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.MaximusTopBar

private enum class StrongmanArea(
    @StringRes val label: Int,
    val icon: Glyph,
    val tabs: List<Int> = emptyList()
) {
    TRAINING(R.string.sm_tab_training, Glyph.BARBELL, listOf(
        R.string.sm_nav_log, R.string.sm_tab_programs, R.string.sm_tab_tools, R.string.sm_tab_library
    )),
    CALCULATOR(R.string.sm_tab_calc, Glyph.CALCULATOR),
    COACH(R.string.sm_nav_coach, Glyph.BOOK, listOf(
        R.string.sm_nav_today, R.string.sm_nav_meet, R.string.sm_nav_systems, R.string.sm_nav_weak
    )),
    ARENA(R.string.sm_nav_arena, Glyph.SHIELD, listOf(
        R.string.sm_nav_rank, R.string.sm_nav_compare, R.string.sm_nav_glory,
        R.string.sm_nav_progression, R.string.sm_nav_stats, R.string.sm_tab_records
    ))
}

@Composable
fun StrongmanHubScreen(
    repository: StrongmanRepository,
    onBack: () -> Unit,
    onOpenProgram: (Long) -> Unit,
    chat: ChatService? = null,
    onOpenChat: () -> Unit = {}
) {
    var area by rememberSaveable { mutableStateOf(StrongmanArea.TRAINING) }
    var requestedSetId by rememberSaveable { mutableStateOf<Long?>(null) }
    var ask by remember { mutableStateOf(false) }
    val stateHolder = rememberSaveableStateHolder()

    LaunchedEffect(repository) { repository.ensureSeeded() }
    BackHandler(enabled = area != StrongmanArea.TRAINING) { area = StrongmanArea.TRAINING }
    Scaffold(
        topBar = {
            MaximusTopBar(title = stringResource(R.string.module_strongman), onBack = {
                if (area == StrongmanArea.TRAINING) onBack() else area = StrongmanArea.TRAINING
            }) {
                if (chat != null) AskMaximusButton(onClick = { ask = true })
            }
        },
        bottomBar = {
            NavigationBar {
                StrongmanArea.entries.forEach { item ->
                    NavigationBarItem(
                        selected = area == item,
                        onClick = { area = item },
                        icon = { GlyphIcon(item.icon, size = 22.dp) },
                        label = { Text(stringResource(item.label), maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        stateHolder.SaveableStateProvider(area.name) {
            var tab by rememberSaveable { mutableIntStateOf(0) }
            Column(Modifier.fillMaxSize().padding(padding)) {
                if (area.tabs.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        area.tabs.forEachIndexed { index, label ->
                            FilterChip(
                                selected = tab == index,
                                onClick = { tab = index },
                                label = { Text(stringResource(label)) }
                            )
                        }
                    }
                }
                stateHolder.SaveableStateProvider("${area.name}/$tab") {
                    when (area) {
                        StrongmanArea.TRAINING -> when (tab) {
                            0 -> TrainingTab(repository) { setId ->
                                requestedSetId = setId
                                area = StrongmanArea.CALCULATOR
                            }
                            1 -> ProgramsTab(repository, onOpenProgram)
                            2 -> ToolsTab(repository)
                            else -> LibraryTab(repository)
                        }
                        StrongmanArea.CALCULATOR -> CalculatorTab(repository, requestedSetId) { requestedSetId = null }
                        StrongmanArea.COACH -> when (tab) {
                            0 -> CoachTodayTab(repository)
                            1 -> CoachMeetTab(repository)
                            2 -> CoachSystemsTab(repository)
                            else -> CoachWeakTab(repository)
                        }
                        StrongmanArea.ARENA -> when (tab) {
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

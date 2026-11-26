package app.maximus.ui.dnd

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.maximus.R
import app.maximus.chat.domain.Focus
import app.maximus.core.app.AppServices
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphIcon
import app.maximus.ui.components.MaximusTopBar

private enum class DndArea(val label: Int, val icon: Glyph) {
    TABLE(R.string.dnd_nav_table, Glyph.DICE),
    CHARACTERS(R.string.dnd_nav_characters, Glyph.SHIELD),
    COMBAT(R.string.dnd_nav_combat, Glyph.PULSE),
    RULES(R.string.dnd_nav_reference, Glyph.BOOK)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndHubScreen(
    services: AppServices,
    onBack: () -> Unit,
    onOpenSheet: (Long, Boolean) -> Unit,
    onEditCharacter: (Long) -> Unit,
    onOpenChat: () -> Unit = {}
) {
    var ask by remember { mutableStateOf(false) }
    var area by rememberSaveable { mutableStateOf(DndArea.TABLE) }
    var tools by rememberSaveable { mutableStateOf(false) }
    var figuresTab by rememberSaveable { mutableIntStateOf(0) }
    var combatTab by rememberSaveable { mutableIntStateOf(0) }
    val content = rememberSaveableStateHolder()
    val scope = rememberCoroutineScope()
    val bubble by services.dnd.bubbleEnabled.collectAsState(initial = true)
    val characters by services.dnd.characters.collectAsState(initial = emptyList())
    val initiative by services.dnd.initiative.collectAsState(initial = null)

    val stepBack = {
        when {
            area != DndArea.TABLE -> { area = DndArea.TABLE; tools = false }
            tools -> tools = false
            else -> onBack()
        }
    }
    BackHandler(area != DndArea.TABLE || tools, onBack = stepBack)

    Scaffold(
        topBar = {
            val title = if (area == DndArea.TABLE && tools) R.string.dnd_table_advanced else R.string.module_dnd
            MaximusTopBar(stringResource(title), stepBack) { AskMaximusButton(onClick = { ask = true }) }
        },
        bottomBar = {
            NavigationBar {
                DndArea.entries.forEach { item ->
                    NavigationBarItem(
                        selected = area == item,
                        onClick = { area = item; tools = false },
                        icon = { GlyphIcon(item.icon) },
                        label = { Text(stringResource(item.label), maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                when (area) {
                    DndArea.TABLE -> {
                        if (tools) {
                            content.SaveableStateProvider("dice-tools") { DiceTab(services) }
                        } else {
                            content.SaveableStateProvider("table") {
                                TableTab(services.dnd, characters, initiative, scope,
                                    onOpenCharacter = { onOpenSheet(it, true) },
                                    onOpenDice = { tools = true },
                                    onOpenCombat = { area = DndArea.COMBAT; combatTab = 0 },
                                    onOpenCharacters = { area = DndArea.CHARACTERS; figuresTab = 0 })
                            }
                        }
                    }
                    DndArea.CHARACTERS -> {
                        PrimaryTabRow(selectedTabIndex = figuresTab) {
                            listOf(R.string.dnd_nav_characters, R.string.dnd_nav_builds).forEachIndexed { index, label ->
                                Tab(selected = figuresTab == index, onClick = { figuresTab = index }, text = { Text(stringResource(label)) })
                            }
                        }
                        content.SaveableStateProvider("figures/$figuresTab") {
                            if (figuresTab == 0) CharactersTab(services, { onOpenSheet(it, false) }, onEditCharacter)
                            else BuildsTab(services, onEditCharacter)
                        }
                    }
                    DndArea.COMBAT -> {
                        PrimaryTabRow(selectedTabIndex = combatTab) {
                            listOf(R.string.dnd_nav_initiative, R.string.dnd_nav_planner).forEachIndexed { index, label ->
                                Tab(selected = combatTab == index, onClick = { combatTab = index }, text = { Text(stringResource(label)) })
                            }
                        }
                        content.SaveableStateProvider("combat/$combatTab") {
                            if (combatTab == 0) InitiativeTab(services.dnd, initiative, characters, scope)
                            else EncounterTab()
                        }
                    }
                    DndArea.RULES -> content.SaveableStateProvider("rules") { ReferenceTab() }
                }
            }
            if (bubble && (area == DndArea.CHARACTERS || area == DndArea.RULES)) QuickRollBubble(services)
        }
    }
    if (ask) {
        AskMaximusSheet(
            services.chat,
            AskRequest(
                stringResource(R.string.module_dnd), Focus.DND,
                listOf("Wie funktioniert Konzentration genau?", "Sorlock oder Sorcadin: Was passt zu meiner Gruppe?",
                    "Gib mir eine Encounter-Idee für Stufe 5", "Welche Invocations lohnen sich für einen Hexblade?", "Erkläre mir Grappling"),
                context = { services.chat.dndContext() }
            ),
            onDismiss = { ask = false },
            onOpenChat = { ask = false; onOpenChat() }
        )
    }
}

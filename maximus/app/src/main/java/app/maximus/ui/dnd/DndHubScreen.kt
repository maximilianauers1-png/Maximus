package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.ui.components.MaximusTopBar

@Suppress("DEPRECATION")
@Composable
fun DndHubScreen(services: AppServices, onBack: () -> Unit, onOpenSheet: (Long) -> Unit, onEditCharacter: (Long) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Dice", "Characters", "Builds", "Monsters", "Encounter", "Reference")
    val bubble by services.dnd.bubbleEnabled.collectAsState(initial = true)
    Scaffold(topBar = { MaximusTopBar("Dungeons & Dragons", onBack) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = tab, edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
                ) {
                    titles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
                }
                when (tab) {
                    0 -> DiceTab(services)
                    1 -> CharactersTab(services, onOpenSheet, onEditCharacter)
                    2 -> BuildsTab(services, onEditCharacter)
                    3 -> MonstersTab(services)
                    4 -> EncounterTab(services)
                    else -> ReferenceTab()
                }
            }
            // Floating d20: opens the quick-roll window from every tab; switch it off on the Dice tab.
            if (bubble) QuickRollBubble(services)
        }
    }
}

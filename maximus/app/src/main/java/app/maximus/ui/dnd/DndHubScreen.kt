package app.maximus.ui.dnd

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val titles = listOf("Dice", "Characters", "Monsters", "Encounter", "Reference")
    Scaffold(topBar = { MaximusTopBar("Dungeons & Dragons", onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = tab, edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
            ) {
                titles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
            }
            when (tab) {
                0 -> DiceTab(services)
                1 -> CharactersTab(services, onOpenSheet, onEditCharacter)
                2 -> MonstersTab(services)
                3 -> EncounterTab(services)
                else -> ReferenceTab()
            }
        }
    }
}

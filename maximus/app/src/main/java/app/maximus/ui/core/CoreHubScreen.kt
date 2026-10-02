package app.maximus.ui.core

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.ui.components.MaximusTopBar

@Suppress("DEPRECATION")
@Composable
fun CoreHubScreen(services: AppServices, onBack: () -> Unit, onOpenNote: (Long) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf(R.string.core_tab_vault, R.string.core_tab_calendar, R.string.core_tab_notes)
    Scaffold(topBar = { MaximusTopBar(stringResource(R.string.module_core), onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = tab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                titles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(t)) }) }
            }
            when (tab) {
                0 -> VaultTab(services)
                1 -> CalendarTab(services)
                else -> NotesTab(services, onOpenNote)
            }
        }
    }
}

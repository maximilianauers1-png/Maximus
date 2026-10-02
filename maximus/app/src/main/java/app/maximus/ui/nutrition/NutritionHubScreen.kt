package app.maximus.ui.nutrition

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.maximus.R
import app.maximus.core.app.AppServices
import app.maximus.nutrition.data.NutritionLogEntity
import app.maximus.nutrition.data.toPoint
import app.maximus.nutrition.domain.AdaptiveTdee
import app.maximus.nutrition.domain.NutritionProfile
import app.maximus.nutrition.domain.NutritionTargets
import app.maximus.nutrition.domain.Recipe
import app.maximus.ui.components.MaximusTopBar
import java.time.LocalDate

/** Locale-dependent recipe texts (German for "de", English otherwise). */
@Composable
fun isGerman(): Boolean = LocalConfiguration.current.locales[0].language == "de"

fun Recipe.name(de: Boolean) = if (de) nameDe else nameEn
fun Recipe.steps(de: Boolean) = if (de) stepsDe else stepsEn

@Suppress("DEPRECATION")
@Composable
fun NutritionHubScreen(services: AppServices, onBack: () -> Unit) {
    val repo = services.nutrition
    val profile by repo.profile.collectAsState(initial = null)
    val log by repo.log.collectAsState(initial = emptyList<NutritionLogEntity>())
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val today = remember { LocalDate.now().toEpochDay() }
    val titles = listOf(R.string.nu_tab_goals, R.string.nu_tab_plan, R.string.nu_tab_recipes, R.string.nu_tab_journal, R.string.nu_tab_tools)

    Scaffold(topBar = { MaximusTopBar(stringResource(R.string.module_nutrition), onBack) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = tab, edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
            ) {
                titles.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(stringResource(t)) }) }
            }
            val p = profile
            if (p == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }
            val adaptive = remember(log) { AdaptiveTdee.estimate(log.map { it.toPoint() }, today) }
            val derived = remember(p, adaptive) { NutritionTargets.derive(p, today, adaptive) }
            when (tab) {
                0 -> GoalsTab(p, derived, adaptive, today) { services.nutrition.saveProfileAsync(it) }
                1 -> PlanTab(p, derived) { services.nutrition.saveProfileAsync(it) }
                2 -> RecipesTab(p) { services.nutrition.saveProfileAsync(it) }
                3 -> JournalTab(log, adaptive, today, repo)
                else -> ToolsTab(p)
            }
        }
    }
}


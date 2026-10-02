package app.maximus.ui.lab

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.core.app.AppServices
import app.maximus.lab.domain.Calculators
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.LabEvent
import app.maximus.lab.domain.LabProgress
import app.maximus.lab.domain.Topic
import app.maximus.ui.components.MaximusTopBar
import java.time.LocalDate
import kotlinx.coroutines.launch

/** What a quiz run should contain. [daily] runs the fixed daily challenge of [epochDay]. */
data class QuizConfig(val topic: Topic?, val difficulty: Int, val count: Int, val daily: Boolean = false, val seed: Long = System.nanoTime())

/** Everything the lab tabs need from the hub: current progress and the navigation/update callbacks. */
class LabContext(
    val progress: LabProgress,
    val today: Long,
    val act: ((LabProgress) -> Pair<LabProgress, List<LabEvent>>) -> Unit,
    val openChapter: (String) -> Unit,
    val openCalculator: (String) -> Unit,
    val startQuiz: (QuizConfig) -> Unit
)

private val TABS = listOf("Übersicht", "Kompendium", "Rechner", "Mathe-Werkzeuge", "Training", "Karteikarten", "Konstanten")

@Suppress("DEPRECATION")
@Composable
fun LabHubScreen(services: AppServices, onBack: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var chapterKey by rememberSaveable { mutableStateOf<String?>(null) }
    var calcKey by rememberSaveable { mutableStateOf<String?>(null) }
    var quiz by remember { mutableStateOf<QuizConfig?>(null) }
    var toasts by remember { mutableStateOf(listOf<LabToast>()) }
    val ids = remember { LongArray(1) }
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toEpochDay() }
    val progress by services.lab.progress.collectAsState(initial = LabProgress())

    fun act(transform: (LabProgress) -> Pair<LabProgress, List<LabEvent>>) {
        scope.launch {
            val events = services.lab.update(transform)
            if (events.isNotEmpty()) toasts = toasts + toastsOf(events) { ++ids[0] }
        }
    }

    val ctx = LabContext(
        progress, today, ::act,
        openChapter = { chapterKey = it },
        openCalculator = { calcKey = it },
        startQuiz = { quiz = it }
    )
    val inDetail = quiz != null || calcKey != null || chapterKey != null
    fun closeDetail() {
        when {
            quiz != null -> quiz = null
            calcKey != null -> calcKey = null
            else -> chapterKey = null
        }
    }
    BackHandler(enabled = inDetail) { closeDetail() }

    val title = when {
        quiz != null -> if (quiz?.daily == true) "Tageschallenge" else "Training"
        calcKey != null -> Calculators.byKey[calcKey]?.title ?: "Rechner"
        chapterKey != null -> Compendium.byKey[chapterKey]?.title ?: "Kapitel"
        else -> "Physik- und Mathe-Labor"
    }

    Scaffold(topBar = { MaximusTopBar(title, onBack = { if (inDetail) closeDetail() else onBack() }) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val q = quiz
            val ck = calcKey
            val chk = chapterKey
            when {
                q != null -> QuizRunner(ctx, q, onClose = { quiz = null })
                ck != null -> CalculatorScreen(ctx, ck)
                chk != null -> ChapterScreen(ctx, chk)
                else -> Column(Modifier.fillMaxSize()) {
                    ScrollableTabRow(
                        selectedTabIndex = tab, edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        TABS.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
                    }
                    when (tab) {
                        0 -> OverviewTab(ctx)
                        1 -> CompendiumTab(ctx)
                        2 -> CalculatorsTab(ctx)
                        3 -> MathToolsTab(ctx)
                        4 -> TrainingTab(ctx)
                        5 -> FlashcardsTab(ctx)
                        else -> ConstantsTab()
                    }
                }
            }
            LabToastHost(toasts) { id -> toasts = toasts.filter { it.id != id } }
        }
    }
}

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
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.ModuleContext
import app.maximus.core.app.AppServices
import app.maximus.lab.domain.Calculators
import app.maximus.lab.domain.Chapter
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.LabEvent
import app.maximus.lab.domain.LabProgress
import app.maximus.lab.domain.QuizMode
import app.maximus.lab.domain.Topic
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.MaximusTopBar
import java.time.LocalDate
import kotlinx.coroutines.launch

/** What a quiz run should contain. [daily] runs the fixed daily challenge of [epochDay]. */
data class QuizConfig(
    val topic: Topic?,
    val difficulty: Int,
    val count: Int,
    val daily: Boolean = false,
    val seed: Long = System.nanoTime(),
    /** Restricts the round to one lecture of the topic, e.g. "Analysis III". */
    val course: String? = null,
    val mode: QuizMode = QuizMode.CONCEPT
)

/** Everything the lab tabs need from the hub: current progress and the navigation/update callbacks. */
class LabContext(
    val progress: LabProgress,
    val today: Long,
    val act: ((LabProgress) -> Pair<LabProgress, List<LabEvent>>) -> Unit,
    val openChapter: (String) -> Unit,
    val openCalculator: (String) -> Unit,
    val startQuiz: (QuizConfig) -> Unit,
    /** Opens "Frag Maximus" with lab context (quiz explanation, chapter questions). */
    val ask: (AskRequest) -> Unit = {}
)

private val TABS = listOf("Übersicht", "AI-Spielwiese", "Kompendium", "Rechner", "Mathe-Werkzeuge", "Training", "Karteikarten", "Konstanten")

@Suppress("DEPRECATION")
@Composable
fun LabHubScreen(services: AppServices, onBack: () -> Unit, onOpenChat: () -> Unit = {}) {
    var ask by remember { mutableStateOf<AskRequest?>(null) }
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
        startQuiz = { quiz = it },
        ask = { ask = it }
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

    Scaffold(topBar = {
        MaximusTopBar(title, onBack = { if (inDetail) closeDetail() else onBack() }) {
            AskMaximusButton(onClick = { ask = labRequest(Compendium.byKey[chapterKey]) })
        }
    }) { padding ->
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
                        1 -> AiPlaygroundTab(ctx)
                        2 -> CompendiumTab(ctx)
                        3 -> CalculatorsTab(ctx)
                        4 -> MathToolsTab(ctx)
                        5 -> TrainingTab(ctx)
                        6 -> FlashcardsTab(ctx)
                        else -> ConstantsTab()
                    }
                }
            }
            LabToastHost(toasts) { id -> toasts = toasts.filter { it.id != id } }
        }
    }

    ask?.let { r -> AskMaximusSheet(services.chat, r, onDismiss = { ask = null }, onOpenChat = { ask = null; onOpenChat() }) }
}

/** Maximus plays the examiner of a doctoral defense on electrocaloric relaxor polymers. */
internal const val DEFENSE_PROMPT = "Sei der Prüfer in meiner Promotionsverteidigung zu Elektrokalorik in P(VDF-TrFE-CFE): " +
    "Stelle mir eine kritische Frage, warte auf meine Antwort, bewerte sie streng und stelle dann die nächste."

/** "Frag Maximus" in the lab: about the open chapter, or general study help. */
internal fun labRequest(chapter: Chapter?): AskRequest =
    if (chapter != null) AskRequest(
        "Kapitel „${chapter.title}“", Focus.SCIENCE,
        (if (chapter.topic == Topic.CALORIC) listOf(DEFENSE_PROMPT) else emptyList()) +
            (if (chapter.topic == Topic.AI) listOf(
                "Erkläre mir das, als hätte ich noch nie programmiert", "Zeig mir ein kleines Python-Beispiel dazu",
                "Stell mir eine Prüfungsfrage wie im IBM AI Engineering Kurs"
            ) else emptyList()) +
            listOf("Erkläre mir das Kapitel anschaulich", "Woher kommt die wichtigste Formel?", "Gib mir ein Anwendungsbeispiel aus der Forschung",
                "Welche typischen Prüfungsfragen gibt es dazu?"),
        context = { ModuleContext.chapter(chapter) }
    ) else AskRequest(
        "Physik- und Mathe-Labor", Focus.SCIENCE,
        listOf(DEFENSE_PROMPT, "Erkläre mir die sieben Techniken des maschinellen Lernens mit Beispielen",
            "Was ist der Unterschied zwischen Keras und PyTorch?", "Erkläre mir anschaulich die Fermi-Dirac-Verteilung", "Was ist der Unterschied zwischen Lagrange und Hamilton?",
            "Wie hängen Fourier-Reihen und Quantenmechanik zusammen?", "Stelle mir eine knifflige Verständnisfrage zur Thermodynamik")
    )

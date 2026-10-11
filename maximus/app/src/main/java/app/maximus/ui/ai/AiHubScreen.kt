package app.maximus.ui.ai

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiEvent
import app.maximus.ai.domain.AiProgress
import app.maximus.ai.domain.Bite
import app.maximus.chat.domain.Focus
import app.maximus.chat.domain.ModuleContext
import app.maximus.core.app.AppServices
import app.maximus.ui.chat.AskMaximusButton
import app.maximus.ui.chat.AskMaximusSheet
import app.maximus.ui.chat.AskRequest
import app.maximus.ui.components.MaximusTopBar
import app.maximus.ui.lab.LabToast
import app.maximus.ui.lab.LabToastHost
import java.time.LocalDate
import kotlinx.coroutines.launch

private val TABS = listOf("Burgsaal", "Lehrpfad", "Turniere", "Übungsplatz", "Spielwiese", "Glossar")

/**
 * "AI-Ingenieurwesen": the academy hub. Tabs for the hall (rank, daily trial, coats of arms), the path of
 * thirteen lessons, the tournaments, free training, the games and the glossary; bites, quizzes and
 * tournaments open as detail screens on top.
 */
@Suppress("DEPRECATION")
@Composable
fun AiHubScreen(services: AppServices, onBack: () -> Unit, onOpenChat: () -> Unit = {}) {
    var ask by remember { mutableStateOf<AskRequest?>(null) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    // Detail screens as a stack: "b:<bite>", "q:<n>" (quiz config in [quizzes]), "t:<lesson>"; back pops one level.
    var stack by remember { mutableStateOf(listOf<String>()) }
    var quizzes by remember { mutableStateOf(mapOf<String, AiQuizConfig>()) }
    var toasts by remember { mutableStateOf(listOf<LabToast>()) }
    val ids = remember { LongArray(1) }
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toEpochDay() }
    val progress by services.ai.progress.collectAsState(initial = AiProgress())

    LaunchedEffect(Unit) { services.ai.importFromLabOnce() }

    fun act(transform: (AiProgress) -> Pair<AiProgress, List<AiEvent>>) {
        scope.launch {
            val events = services.ai.update(transform)
            if (events.isNotEmpty()) toasts = toasts + aiToasts(events) { ++ids[0] }
        }
    }

    fun push(entry: String) { stack = stack + entry }
    fun pop() {
        val gone = stack.lastOrNull()
        stack = stack.dropLast(1)
        if (gone != null && gone.startsWith("q:")) quizzes = quizzes - gone
    }
    val top = stack.lastOrNull()
    val biteKey = top?.takeIf { it.startsWith("b:") }?.removePrefix("b:")
    val tournament = top?.takeIf { it.startsWith("t:") }?.removePrefix("t:")?.toIntOrNull()
    val quizConfig = top?.let { quizzes[it] }

    val ctx = AiContext(
        progress, today, ::act,
        openBite = { push("b:$it") },
        startQuiz = { c -> val id = "q:${ids[0]++}"; quizzes = quizzes + (id to c); push(id) },
        startTournament = { push("t:$it") },
        ask = { ask = it }
    )
    BackHandler(enabled = stack.isNotEmpty()) { pop() }

    val title = when {
        quizConfig != null -> when (quizConfig.mode) {
            AiQuizMode.BITE -> "Happen-Quiz"; AiQuizMode.DAILY -> "Tagesprobe"; AiQuizMode.WEAK -> "Fehlerschmiede"; else -> "Übungsplatz"
        }
        tournament != null -> "Turnier · Lektion $tournament"
        biteKey != null -> AiCourse.byKey[biteKey]?.let { "Happen ${it.number}" } ?: "Happen"
        else -> "AI-Ingenieurwesen"
    }

    Scaffold(topBar = {
        MaximusTopBar(title, onBack = { if (stack.isNotEmpty()) pop() else onBack() }) {
            AskMaximusButton(onClick = { ask = aiRequest(AiCourse.byKey[biteKey]) })
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                quizConfig != null -> key(top) { AiQuizRunner(ctx, quizConfig, onClose = { pop() }) }
                tournament != null -> TournamentRunner(ctx, tournament, onClose = { pop() })
                biteKey != null -> BiteScreen(ctx, biteKey)
                else -> Column(Modifier.fillMaxSize()) {
                    ScrollableTabRow(
                        selectedTabIndex = tab, edgePadding = 12.dp,
                        containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        TABS.forEachIndexed { i, s -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(s) }) }
                    }
                    when (tab) {
                        0 -> HallTab(ctx)
                        1 -> PathTab(ctx)
                        2 -> TournamentsTab(ctx)
                        3 -> TrainingGroundTab(ctx)
                        4 -> AiPlaygroundTab(ctx)
                        else -> GlossaryTab(ctx)
                    }
                }
            }
            LabToastHost(toasts) { id -> toasts = toasts.filter { it.id != id } }
        }
    }

    ask?.let { r -> AskMaximusSheet(services.chat, r, onDismiss = { ask = null }, onOpenChat = { ask = null; onOpenChat() }) }
}

/** "Frag Maximus" in the academy: about the open bite, or general study help along the IBM path. */
internal fun aiRequest(bite: Bite?): AskRequest =
    if (bite != null) AskRequest(
        "Happen ${bite.number} · ${bite.title}", Focus.CODE,
        listOf(
            "Erkläre mir diesen Happen, als hätte ich noch nie programmiert", "Zeig mir ein kleines Python-Beispiel dazu",
            "Welche typischen Fehler macht man hier im Code?", "Stell mir eine Prüfungsfrage wie im IBM AI Engineering Kurs",
            "Wie hängt das mit Physik zusammen?"
        ),
        context = { ModuleContext.aiBite(bite) }
    ) else AskRequest(
        "AI-Ingenieurwesen", Focus.CODE,
        listOf(
            "Wie baue ich meinen Lernplan für das IBM AI Engineering Zertifikat?", "Was ist der Unterschied zwischen Keras und PyTorch?",
            "Erkläre mir Attention mit einem Rechenbeispiel", "Wann nehme ich LoRA, wann volles Fine-Tuning?",
            "Wie baue ich eine RAG-Anwendung mit LangChain Schritt für Schritt?"
        )
    )

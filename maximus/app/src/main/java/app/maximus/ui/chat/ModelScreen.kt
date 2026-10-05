@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.chat

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.maximus.chat.data.ChatService
import app.maximus.chat.domain.Backend
import app.maximus.chat.domain.EngineState
import app.maximus.chat.domain.GenStats
import app.maximus.chat.domain.ModelCatalog
import app.maximus.chat.domain.ModelFile
import app.maximus.lab.domain.Fmt
import app.maximus.ui.components.Glyph
import app.maximus.ui.components.GlyphButton
import app.maximus.ui.components.MaximusTopBar
import app.maximus.ui.strongman.SectionCard
import app.maximus.ui.theme.Palette
import kotlinx.coroutines.launch

/** Model management, engine settings and the speed test. */
@Suppress("DEPRECATION")
@Composable
fun ModelScreen(service: ChatService, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val settings by service.settings.collectAsState()
    val engine by service.controller.engine.collectAsState()
    var models by remember { mutableStateOf<List<ModelFile>>(emptyList()) }
    var refresh by remember { mutableIntStateOf(0) }
    var importProgress by remember { mutableStateOf<Float?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var armedDelete by remember { mutableStateOf<String?>(null) }
    var bench by remember { mutableStateOf<List<GenStats>>(emptyList()) }
    var benchRunning by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) { models = service.repository.listModels() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            importProgress = 0f
            message = null
            try {
                val m = service.repository.importModel(uri) { p -> importProgress = p }
                service.update { it.copy(model = m, maxTokens = 0) }
                message = "${m.fileName} importiert und aktiviert."
            } catch (e: Exception) {
                message = "Import fehlgeschlagen: ${e.message}"
            } finally {
                importProgress = null
                refresh++
            }
        }
    }

    val memory = remember {
        val am = context.getSystemService(ActivityManager::class.java)
        ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { MaximusTopBar("Modell und Einstellungen", onBack = onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SectionCard("Installierte Modelle") {
                    if (models.isEmpty()) Text("Noch kein Modell auf dem Gerät.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    models.forEach { m ->
                        val profile = ModelCatalog.profileFor(m.fileName)
                        val active = settings.model?.path == m.path
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .background(if (active) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                                .border(1.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .clickable { service.update { it.copy(model = m, maxTokens = 0) } }
                                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text((if (active) "● " else "") + profile.name, style = MaterialTheme.typography.titleSmall, color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text("${m.fileName} · ${Fmt.num(m.sizeBytes / 1e9, 3)} GB · Kontext ${ModelCatalog.maxTokensFor(m.fileName)}",
                                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            GlyphButton(Glyph.TRASH, if (armedDelete == m.path) "Wirklich löschen" else "Löschen", {
                                if (armedDelete == m.path) {
                                    scope.launch {
                                        if (active) { service.controller.releaseNow(); service.update { it.copy(model = null) } }
                                        service.repository.deleteModel(m)
                                        armedDelete = null
                                        refresh++
                                    }
                                } else armedDelete = m.path
                            }, tint = if (armedDelete == m.path) Palette.Heraldic else MaterialTheme.colorScheme.outline)
                        }
                    }
                    importProgress?.let { p ->
                        Text("Importiere … ${(p * 100).toInt()} %", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }
                    Button(onClick = { picker.launch(arrayOf("*/*")) }, enabled = importProgress == null, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("Modelldatei (.task) importieren")
                    }
                    Text(
                        "Alternativ per USB direkt in diesen Ordner kopieren (kein doppelter Speicherbedarf):\n${service.repository.modelDir().absolutePath}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)
                    )
                    OutlinedButton(onClick = { refresh++ }, modifier = Modifier.padding(top = 4.dp)) { Text("Ordner neu einlesen") }
                    message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp)) }
                }
            }
            item {
                SectionCard("Empfohlene Modelle") {
                    Text(
                        "Download am PC oder im Browser (Hugging Face, bei Gemma nach Annahme der Lizenz), danach importieren. " +
                            "Die App selbst hat keinen Netzzugriff. RAM: ${Fmt.num(memory.totalMem / 1e9, 3)} GB gesamt, ${Fmt.num(memory.availMem / 1e9, 3)} GB frei.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ModelCatalog.profiles.forEach { p ->
                        Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(p.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("≈ ${Fmt.num(p.approxGb, 2)} GB", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            }
                            if (p.recommended) Text("Empfehlung für das Galaxy A55", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(p.strengths, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (p.approxGb * 1e9 > memory.totalMem * 0.5) Text("Achtung: belegt mehr als die Hälfte des RAM.", style = MaterialTheme.typography.labelSmall, color = Palette.Heraldic)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { clipboard.setText(AnnotatedString("https://" + p.source)); message = "Link kopiert" }) { Text("Link kopieren") }
                                OutlinedButton(onClick = {
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://" + p.source)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                                }) { Text("Im Browser öffnen") }
                            }
                        }
                    }
                }
            }
            item {
                SectionCard("Leistung") {
                    Text("Recheneinheit", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Backend.entries.forEach { b -> FilterChip(settings.backend == b, { service.update { it.copy(backend = b) } }, label = { Text(b.label) }) }
                    }
                    Text("GPU ist meist schneller beim Lesen langer Fragen; fällt sie aus, nutzt Maximus automatisch die CPU.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Kontextlänge (Frage + Verlauf + Antwort)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(settings.maxTokens == 0, { service.update { it.copy(maxTokens = 0) } }, label = { Text(if (settings.maxTokens == 0) "Auto (${settings.effectiveMaxTokens()})" else "Auto") })
                        listOf(1024, 2048, 4096).forEach { n -> FilterChip(settings.maxTokens == n, { service.update { it.copy(maxTokens = n) } }, label = { Text("$n") }) }
                    }
                    Text("Maximale Antwortlänge (Token)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(256, 512, 1024, 2048).forEach { n -> FilterChip(settings.maxReply == n, { service.update { it.copy(maxReply = n) } }, label = { Text("$n") }) }
                    }
                    Text("Im Speicher halten nach Verlassen des Chats", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to "sofort frei", 60 to "1 min", 300 to "5 min", 900 to "15 min", 1800 to "30 min").forEach { (s, l) ->
                            FilterChip(settings.keepLoadedSeconds == s, { service.update { it.copy(keepLoadedSeconds = s) } }, label = { Text(l) })
                        }
                    }
                    SwitchRow("Trainingsdaten bei Strongman-Fragen einbeziehen (bleibt lokal)", settings.includeTraining) { v -> service.update { it.copy(includeTraining = v) } }
                    SwitchRow("Geschwindigkeit unter Antworten anzeigen", settings.showStats) { v -> service.update { it.copy(showStats = v) } }
                }
            }
            item {
                SectionCard("Leistungstest") {
                    Text("Misst Lese- (Prefill) und Schreibtempo (Decode) des aktiven Modells auf GPU und CPU mit derselben Frage.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    bench.forEach { s ->
                        Text("${s.backend}: ${Fmt.num(s.decodeTokensPerSecond, 3)} Tok/s schreiben · ${Fmt.num(s.prefillTokensPerSecond, 3)} Tok/s lesen · erstes Token nach ${Fmt.num(s.firstTokenMs / 1000.0, 2)} s",
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                    val best = bench.maxByOrNull { it.decodeTokensPerSecond }
                    if (bench.size == 2 && best != null) {
                        val b = Backend.entries.first { it.label == best.backend }
                        Button(onClick = { service.update { it.copy(backend = b) } }, enabled = settings.backend != b, modifier = Modifier.padding(top = 6.dp)) {
                            Text(if (settings.backend == b) "${b.label} ist bereits aktiv" else "${b.label} übernehmen (schneller)")
                        }
                    }
                    if (benchRunning) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    OutlinedButton(
                        enabled = settings.model != null && !benchRunning && !service.controller.busy,
                        onClick = {
                            scope.launch {
                                benchRunning = true
                                bench = emptyList()
                                try {
                                    for (b in listOf(Backend.GPU, Backend.CPU)) bench = bench + service.controller.benchmark(b)
                                } catch (e: Exception) {
                                    message = "Test abgebrochen: ${e.message}"
                                } finally {
                                    benchRunning = false
                                }
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Test starten (≈ 1 min)") }
                }
            }
            item {
                SectionCard("Speicher und Datenschutz") {
                    val resident = engine is EngineState.Ready || engine is EngineState.Loading
                    Text(
                        if (resident) "Das Modell ist geladen. Android gibt es bei Speichermangel automatisch frei." else "Das Modell ist nicht geladen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(onClick = { service.controller.releaseNow() }, enabled = resident && !service.controller.busy, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Jetzt aus dem Speicher entfernen")
                    }
                    Text(
                        "Maximus besitzt keine Internet-Berechtigung (im Manifest entfernt). Modelle, Chats und Trainingsdaten bleiben auf dem Gerät; " +
                            "Chats liegen verschlüsselt (SQLCipher) in der App-Datenbank.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked, onChange)
    }
}

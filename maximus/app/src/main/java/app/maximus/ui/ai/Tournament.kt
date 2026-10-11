package app.maximus.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import app.maximus.ai.domain.AiCourse
import app.maximus.ai.domain.AiQuizEngine
import app.maximus.ai.domain.AiRules
import app.maximus.ai.domain.Joust
import app.maximus.ai.domain.QKind
import app.maximus.ui.components.PlateCard
import app.maximus.ui.lab.ProgressBar
import app.maximus.ui.strongman.SectionCard
import kotlin.random.Random
import kotlinx.coroutines.delay

/** "Turniere": one tournament per lesson, with its champion and the best trophy. */
@Composable
fun TournamentsTab(ctx: AiContext) {
    val p = ctx.progress
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SectionCard("Turnierordnung") {
                Text(
                    "Jede Lektion endet mit einem Turnier in drei Tjosten: Knappentjost (leicht), Rittertjost (mittel) und Königstjost gegen den Champion " +
                        "der Lektion (schwer, mit Code und Rechnen). Du trägst ${Joust.SHIELDS} Schilde; jeder Fehler und jede abgelaufene Sanduhr kostet einen. " +
                        "Treffer in Folge verlängern deine Lanze: mehr Punkte je Treffer. Ohne Schildverlust gibt es Gold, mit einem Silber, mit zweien Bronze.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Pokale: ${p.trophies.size}/13 · Gold: ${p.trophies.values.count { it >= 3 }}", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
            }
        }
        items(AiCourse.lessons, key = { it.number }) { l ->
            val bites = AiCourse.bitesOf(l.number)
            val read = bites.count { it.key in p.readBites }
            PlateCard(onClick = { ctx.startTournament(l.number) }) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lektion ${l.number} · ${l.title}", style = MaterialTheme.typography.labelLarge, color = lessonColor(l.number),
                            modifier = Modifier.weight(1f), maxLines = 1)
                        TrophyBadge(p.trophies[l.number] ?: 0)
                    }
                    Text(l.champion.name, style = MaterialTheme.typography.titleMedium)
                    Text(l.champion.epithet, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(
                        if (read < bites.size) "Vorbereitung: $read/${bites.size} Happen gelesen – antreten darfst du trotzdem." else "Bestens vorbereitet.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private enum class Phase { INTRO, FIGHT, ROUND_WON, DEFEAT, VICTORY }

/** Seconds on the hourglass: code and calculations get more time. */
private fun secondsFor(kind: QKind, round: Int) = (if (kind == QKind.CODE || kind == QKind.CALC) 75 else 45) - 5 * round

@Composable
fun TournamentRunner(ctx: AiContext, lessonNo: Int, onClose: () -> Unit) {
    val lesson = AiCourse.lesson(lessonNo)
    var seed by remember { mutableLongStateOf(System.nanoTime()) }
    val rounds = remember(lessonNo, seed) { AiQuizEngine.tournament(lessonNo, Random(seed)) }
    var phase by remember(rounds) { mutableStateOf(Phase.INTRO) }
    var round by remember(rounds) { mutableIntStateOf(0) }
    var index by remember(rounds) { mutableIntStateOf(0) }
    var shields by remember(rounds) { mutableIntStateOf(Joust.SHIELDS) }
    var combo by remember(rounds) { mutableIntStateOf(0) }
    var bestCombo by remember(rounds) { mutableIntStateOf(0) }
    var score by remember(rounds) { mutableIntStateOf(0) }
    var hits by remember(rounds) { mutableIntStateOf(0) }
    var choice by remember(rounds, round, index) { mutableIntStateOf(-1) }
    var left by remember(rounds, round, index) { mutableIntStateOf(0) }
    val startXp = remember(rounds) { ctx.progress.xp }

    val q = rounds.getOrNull(round)?.getOrNull(index)

    fun resolve(pick: Int) {
        if (q == null || choice != -1) return
        choice = pick
        val ok = pick >= 0 && q.check(pick)
        if (ok) {
            hits++; combo++; bestCombo = maxOf(bestCombo, combo); score += Joust.points(round, combo - 1)
        } else {
            combo = 0; shields--
        }
        ctx.act { p -> AiRules.answer(p, q, ok, ctx.today) }
    }

    fun finish(won: Boolean) {
        phase = if (won) Phase.VICTORY else Phase.DEFEAT
        val tier = Joust.tier(won, shields)
        val h = hits
        ctx.act { p -> AiRules.tournamentFinished(p, lessonNo, tier, h, ctx.today) }
    }

    fun next() {
        when {
            shields <= 0 -> finish(false)
            index + 1 < rounds[round].size -> index++
            round + 1 < rounds.size -> phase = Phase.ROUND_WON
            else -> finish(true)
        }
    }

    // The hourglass: counts down while the question is open; at zero it costs a shield.
    LaunchedEffect(phase, round, index, choice) {
        if (phase != Phase.FIGHT || q == null || choice != -1) return@LaunchedEffect
        left = secondsFor(q.kind, round)
        while (left > 0) { delay(1000); left-- }
        resolve(-2)
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (phase) {
            Phase.INTRO -> item {
                SectionCard("Turnier der Lektion ${lesson.number}") {
                    Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = lessonColor(lesson.number))
                    Text("Dein Gegner: ${lesson.champion.name}, ${lesson.champion.epithet}", style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp))
                    Text("„${lesson.champion.taunt}“", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                    AiQuizEngine.ROUNDS.forEachIndexed { i, r ->
                        Text("${i + 1}. ${r.name}: ${rounds.getOrNull(i)?.size ?: 0} Fragen, ${diamonds(r.difficulty)}", style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = if (i == 0) 8.dp else 0.dp))
                    }
                    Button(onClick = { phase = Phase.FIGHT }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text("Lanze senken – antreten!") }
                    OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Zurück") }
                }
            }
            Phase.ROUND_WON -> item {
                SectionCard("${AiQuizEngine.ROUNDS[round].name} bestanden!") {
                    Text("Schilde: ${"🛡".repeat(shields)}   Punkte: $score", style = MaterialTheme.typography.titleMedium)
                    val nextRound = AiQuizEngine.ROUNDS[round + 1]
                    Text(
                        if (round + 1 == rounds.size - 1) "Nun reitet ${lesson.champion.name} selbst in die Bahn: „${lesson.champion.taunt}“"
                        else "Als Nächstes: ${nextRound.name}.",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp)
                    )
                    Button(onClick = { round++; index = 0; phase = Phase.FIGHT }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                        Text("Zur ${nextRound.name}")
                    }
                }
            }
            Phase.DEFEAT, Phase.VICTORY -> item {
                val won = phase == Phase.VICTORY
                val tier = Joust.tier(won, shields)
                SectionCard(if (won) "Sieg über ${lesson.champion.name}!" else "Aus dem Sattel gehoben") {
                    TrophyBadge(tier)
                    Text(if (won) Joust.tierName(tier) else "Diesmal kein Pokal – lies die Happen nach und reite erneut an.",
                        style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                    Text("Punkte: $score · Treffer: $hits · längste Lanze: $bestCombo", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    Text("Gewonnene XP: ${ctx.progress.xp - startXp}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                        OutlinedButton(onClick = { seed = System.nanoTime() }, modifier = Modifier.weight(1f)) { Text("Erneut antreten") }
                        Button(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Zur Burg") }
                    }
                }
            }
            Phase.FIGHT -> if (q != null) {
                item {
                    val r = AiQuizEngine.ROUNDS[round]
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${r.name} · ${index + 1}/${rounds[round].size}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text("🛡".repeat(shields.coerceAtLeast(0)) + "·".repeat((Joust.SHIELDS - shields).coerceAtLeast(0)), style = MaterialTheme.typography.titleMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Text("Punkte $score" + if (combo >= 2) " · Lanze ×${minOf(combo, 5) + 1}" else "", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            Text(if (choice == -1) "⏳ $left s" else "", style = MaterialTheme.typography.labelMedium,
                                color = if (left <= 10) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ProgressBar(if (choice == -1) left.toFloat() / secondsFor(q.kind, round) else 0f,
                            if (left <= 10) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary, Modifier.padding(top = 4.dp), height = 4.dp)
                    }
                }
                questionItems(
                    ctx, q, round * 100 + index, choice, onPick = { resolve(it) },
                    nextLabel = when {
                        shields <= 0 -> "Ergebnis"
                        index + 1 < rounds[round].size -> "Nächster Ritt"
                        round + 1 < rounds.size -> "Runde abschließen"
                        else -> "Siegerehrung"
                    },
                    onNext = { next() }, showExplainButton = false
                )
            }
        }
    }
}

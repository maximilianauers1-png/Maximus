@file:OptIn(ExperimentalLayoutApi::class)

package app.maximus.ui.strongman

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.maximus.strongman.data.StrongmanRepository
import app.maximus.strongman.domain.Arena
import app.maximus.strongman.domain.Badge
import app.maximus.strongman.domain.Boss
import app.maximus.strongman.domain.BossKind
import app.maximus.strongman.domain.LiftBest
import app.maximus.strongman.domain.Performance
import app.maximus.strongman.domain.Placement
import app.maximus.strongman.domain.Population
import app.maximus.strongman.domain.Quest
import app.maximus.strongman.domain.Rank
import app.maximus.strongman.domain.Ranks
import app.maximus.strongman.domain.ScoreMode
import app.maximus.strongman.domain.StandardLift
import app.maximus.strongman.domain.StrengthStandards
import app.maximus.strongman.domain.Normal
import app.maximus.ui.charts.ChartFrame
import app.maximus.ui.charts.drawDistribution
import app.maximus.ui.components.EmptyState
import kotlinx.coroutines.launch

private const val NO_DATA = "Noch keine Wertung. Logge Kniebeuge, Bankdrücken, Kreuzheben, Strict Press, Log, Axle, Axle-Kreuzheben oder Atlas Stones – " +
    "oder trage alte Rekorde unter Arena → Progression ein."

// =====================================================================================================
// Rang
// =====================================================================================================

@Composable
fun ArenaRankTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val scope = rememberCoroutineScope()
    val placements = remember(d) { d.placements() }
    val overall = remember(placements) { Arena.overall(placements) }
    val bw = d.settings.bodyweightKg

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PopulationModeSelector(d.population, d.mode,
                onPopulation = { scope.launch { repository.setPref(StrongmanData.PREF_POPULATION, it.name) } },
                onMode = { scope.launch { repository.setPref(StrongmanData.PREF_MODE, it.name) } })
        }
        if (d.mode == ScoreMode.DOTS && bw == null) item {
            Hint("Für die DOTS-Wertung fehlt dein Körpergewicht: Training → Werkzeuge → Einstellungen.", MaterialTheme.colorScheme.error)
        }
        item { HeroCard(d, overall) }
        if (placements.isEmpty()) {
            item { EmptyState(NO_DATA) }
        } else {
            items(placements, key = { it.lift.name }) { p -> LiftRankCard(p, d.bests[p.lift]) }
            item { AllPopulationsCard(d) }
        }
        item {
            SectionCard("So wird gewertet") {
                Hint(
                    "Jede Population ist als Lognormalverteilung modelliert: ln X ~ N(ln m, σ²). Perzentil = Φ((ln x − ln m)/σ). " +
                        "DOTS: x = Last · 500/P(KG) mit dem DOTS-Polynom P – damit zählt die Leistung relativ zum Körpergewicht. " +
                        "Absolut: rohe Kilos, unabhängig vom Körpergewicht; σ_abs² = σ_DOTS² + (⅔·σ_lnKG)². " +
                        "Kraftwert = 1000 + 250·z (Median = 1000). Gesamtrang: z̄ korrelierter Disziplinen (ρ ≈ 0,7), " +
                        "reskaliert mit √((1+(k−1)ρ)/k). Wiederholungs-PRs zählen über ihren e1RM, echte Singles als sie selbst."
                )
                Hint(
                    "Die Parameter sind Schätzungen aus öffentlich verfügbaren Wettkampf- und Kraftstandard-Daten, keine Volkszählung. " +
                        "Die Powerlifting-Population sind getestete Raw-Heber – der fairste Vergleich für dich als Naturalathlet. " +
                        "Die Strongman-Population ist weitgehend ungetestet: Jeder Rang dort zählt doppelt.",
                    MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun HeroCard(d: StrongmanData, overall: app.maximus.strongman.domain.OverallStanding?) {
    val streak = remember(d) { Arena.weekStreak(d.trainingDays, d.today) }
    SectionCard("Arena · ${d.population.title} · ${if (d.mode == ScoreMode.DOTS) "DOTS" else "Absolut"}") {
        if (overall == null) {
            Hint("Noch kein Gesamtrang – sobald eine Disziplin gewertet ist, erscheint dein Wappen hier.")
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankEmblem(overall.rank, Ranks.division(overall.percentile), 84.dp)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("${overall.rank.title} ${Ranks.roman(Ranks.division(overall.percentile))}", style = MaterialTheme.typography.headlineSmall,
                        color = rankColor(overall.rank), fontWeight = FontWeight.Bold)
                    Text("Stärker als ${pct(overall.percentile)} der ${d.population.title}-Population", style = MaterialTheme.typography.bodyMedium)
                    Text(overall.rank.motto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            MeterBar(Ranks.tierProgress(overall.percentile), rankColor(overall.rank), Modifier.padding(top = 10.dp))
            Ranks.next(overall.rank)?.let { Hint("Fortschritt bis ${it.title} (ab ${fmt(it.minPercentile, 1)}. Perzentil)") }
        }
        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(overall?.rating?.toString() ?: "—", "Kraftwert", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
            StatPill("${d.level.level}", "Level", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
            StatPill("$streak", "Wochen-Serie", Modifier.weight(1f))
        }
        Text("XP ${d.level.xp} · noch ${d.level.xpForLevel - d.level.xpIntoLevel} bis Level ${d.level.level + 1}",
            style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
        MeterBar(d.level.progress, MaterialTheme.colorScheme.primary, Modifier.padding(top = 4.dp), 6.dp)
    }
}

@Composable
private fun LiftRankCard(p: Placement, best: LiftBest?) {
    val div = Ranks.division(p.percentile)
    val color = rankColor(p.rank)
    SectionCard(p.lift.title) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RankEmblem(p.rank, div, 56.dp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("${p.rank.title} ${Ranks.roman(div)}", style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
                Text("${pct(p.percentile)} · Kraftwert ${p.rating}", style = MaterialTheme.typography.bodyMedium)
                if (p.mode == ScoreMode.DOTS) Hint("DOTS-Punkte ${fmt(p.score, 1)}")
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${fmtKg(p.valueKg)} kg", style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Bold)
                if (best != null && p.lift != StandardLift.TOTAL) {
                    Hint(if (best.reps == 1) "Single · ${fmtDay(best.epochDay)}" else "${fmtKg(best.weightKg)} × ${best.reps} · ${fmtDay(best.epochDay)}")
                    if (best.historic) Hint("aus dem Rekordbuch", MaterialTheme.colorScheme.secondary)
                }
            }
        }
        MeterBar(Ranks.tierProgress(p.percentile), color, Modifier.padding(top = 10.dp))
        val next = Ranks.next(p.rank)
        if (next != null && p.nextTierKg != null) {
            Text("Noch ${fmtKg(maxOf(0.0, p.nextTierKg - p.valueKg))} kg bis ${next.title} (${fmtKg(p.nextTierKg)} kg)",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
        } else Text("Höchster Rang erreicht.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun AllPopulationsCard(d: StrongmanData) {
    val rows = remember(d) {
        StandardLift.entries.filter { d.bests.containsKey(it) }.map { lift ->
            lift to Population.entries.map { pop ->
                ScoreMode.entries.map { m -> d.placements(pop, m).firstOrNull { it.lift == lift } }
            }
        }
    }
    SectionCard("Alle Populationen auf einen Blick") {
        Row(Modifier.fillMaxWidth()) {
            Text("", Modifier.width(64.dp))
            Population.entries.forEach { Text(it.title, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
        }
        Hint("Perzentil DOTS / absolut")
        rows.forEach { (lift, pops) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(lift.short, Modifier.width(64.dp), style = MaterialTheme.typography.labelLarge)
                pops.forEach { modes ->
                    Text(modes.joinToString(" / ") { it?.let { p -> fmt(p.percentile, 0) } ?: "–" }, Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
    }
}

// =====================================================================================================
// Vergleich: Verteilung, alle Populationen, Wiederholungs-PRs
// =====================================================================================================

@Composable
fun ArenaCompareTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val scope = rememberCoroutineScope()
    val placements = remember(d) { d.placements() }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val current = placements.firstOrNull { it.lift.name == selected } ?: placements.firstOrNull()

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PopulationModeSelector(d.population, d.mode,
                onPopulation = { scope.launch { repository.setPref(StrongmanData.PREF_POPULATION, it.name) } },
                onMode = { scope.launch { repository.setPref(StrongmanData.PREF_MODE, it.name) } })
        }
        if (current == null) { item { EmptyState(NO_DATA) }; return@LazyColumn }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                placements.forEach { p -> FilterChip(current.lift == p.lift, { selected = p.lift.name }, label = { Text(p.lift.title) }) }
            }
        }
        item { DistributionCard(current, d) }
        if (current.lift != StandardLift.TOTAL) item { RepPrCard(current.lift, d) }
    }
}

@Composable
private fun DistributionCard(p: Placement, d: StrongmanData) {
    val density = remember(p) { StrengthStandards.density(p) }
    val tiers = remember(p) {
        Rank.entries.drop(1).map { r ->
            val z = Normal.quantile(r.minPercentile / 100)
            p.medianKgEquivalent * kotlin.math.exp(p.sigma * z) to r.title
        }
    }
    SectionCard("${p.lift.title} in der ${p.population.title}-Population") {
        ResultLine("Deine Leistung", "${fmtKg(p.valueKg)} kg")
        ResultLine(if (p.mode == ScoreMode.DOTS) "Median bei deinem KG" else "Median", "${fmtKg(p.medianKgEquivalent)} kg")
        ResultLine("Perzentil", pct(p.percentile))
        ResultLine("z-Wert", (if (p.z >= 0) "+" else "") + fmt(p.z, 2) + " σ")
        ChartFrame("Verteilung (Fläche links = Anteil schwächer als du)", "maximus-verteilung-${p.lift.name.lowercase()}", Modifier.padding(top = 8.dp), height = 220.dp) { hits, m, c ->
            drawDistribution(hits, m, c, density, p.valueKg, "Du ${fmtKg(p.valueKg)} kg", tiers)
        }
        Hint("Gestrichelte Linien: Rangschwellen (antippen für Namen und Last). " +
            if (p.mode == ScoreMode.DOTS) "Im DOTS-Modus ist die Verteilung auf dein Körpergewicht umgerechnet." else "Absolut: gleiche Kurve für jedes Körpergewicht.")
    }
}

@Composable
private fun RepPrCard(lift: StandardLift, d: StrongmanData) {
    val rows = remember(d, lift) {
        d.events.filter { it.lift == lift && it.reps in 1..12 }.groupBy { it.reps }.toSortedMap()
            .map { (_, list) -> list.maxBy { it.weightKg } }
    }
    SectionCard("Wiederholungs-PRs gegen die Population") {
        if (rows.isEmpty()) { Hint("Noch keine Sätze für ${lift.title}."); return@SectionCard }
        Row(Modifier.fillMaxWidth()) {
            Text("Wdh.", Modifier.width(44.dp), style = MaterialTheme.typography.labelMedium)
            Text("Last", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            Text("e1RM", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            Text("Rang", Modifier.weight(1.4f), style = MaterialTheme.typography.labelMedium)
        }
        rows.forEach { e ->
            val e1 = Performance.e1rm(e.weightKg, e.reps, null, e.e1rm) ?: return@forEach
            val p = StrengthStandards.place(lift, e1, d.population, d.settings.sex, d.settings.bodyweightKg, d.mode)
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${e.reps}", Modifier.width(44.dp), style = MaterialTheme.typography.titleSmall)
                Text("${fmtKg(e.weightKg)} kg", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                Text(fmt(e1, 1), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                Text(p?.let { "${it.rank.title} · ${fmt(it.percentile, 0)} %" } ?: "—", Modifier.weight(1.4f),
                    style = MaterialTheme.typography.bodyMedium, color = p?.let { rankColor(it.rank) } ?: MaterialTheme.colorScheme.onSurface)
            }
        }
        Hint("Jeder Wiederholungsrekord wird über die e1RM-Schätzung (Epley/Brzycki-Mittel, ≤ 10 Wdh. am genauesten) in die Population gestellt. " +
            "Liegt ein höherer Wiederholungsbereich deutlich über deinem Single, ist dein Maximum vermutlich unterschätzt – Zeit für einen Test.")
    }
}

// =====================================================================================================
// Ruhm: Level, Quests, Bosse, Abzeichen
// =====================================================================================================

@Composable
fun ArenaGloryTab(repository: StrongmanRepository) {
    val d = rememberStrongmanData(repository)
    val quests = remember(d) { Arena.quests(d.sets, d.today, d.isEvent) }
    val placements = remember(d) { d.placements(Population.STRONGMAN, d.mode) }
    val bosses = remember(d, placements) { Arena.bosses(d.events, d.today, placements.associateBy { it.lift }) }
    val bestRank = placements.filter { it.lift != StandardLift.TOTAL }.maxByOrNull { it.rank.ordinal }?.rank
    val badges = remember(d, bestRank) { Arena.badges(d.sets, d.events, d.settings.bodyweightKg, d.categoryOf, bestRank) }
    val streak = remember(d) { Arena.weekStreak(d.trainingDays, d.today) }
    val longest = remember(d) { Arena.longestWeekStreak(d.trainingDays) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard("Level ${d.level.level}") {
                MeterBar(d.level.progress, MaterialTheme.colorScheme.primary, height = 10.dp)
                Text("${d.level.xpIntoLevel} / ${d.level.xpForLevel} XP · gesamt ${d.level.xp}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill("$streak", "Serie (Wochen)", Modifier.weight(1f), MaterialTheme.colorScheme.secondary)
                    StatPill("$longest", "Längste Serie", Modifier.weight(1f))
                    StatPill("${badges.count { it.achieved }}/${badges.size}", "Abzeichen", Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                }
                Hint("XP: ${Arena.XP_DAY} pro Trainingstag, ${Arena.XP_SET} pro Satz, 1 pro 100 kg Volumen, ${Arena.XP_E1RM_PR} pro e1RM-Rekord, " +
                    "${Arena.XP_REP_PR} pro Wiederholungsrekord, Quest-Belohnungen und ${StrongmanData.XP_HISTORIC} pro Rekordbuch-Eintrag. " +
                    "Level L braucht 150·(L−1)^1,55 XP.")
            }
        }
        item { QuestCard(quests) }
        if (bosses.isNotEmpty()) item { BossCard(bosses) }
        badges.groupBy { it.group }.forEach { (group, list) -> item(key = "badges_$group") { BadgeGroup(group, list) } }
    }
}

@Composable
private fun QuestCard(quests: List<Quest>) {
    SectionCard("Quests dieser Woche") {
        quests.forEach { q ->
            Column(Modifier.padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text((if (q.done) "✔ " else "") + q.title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                        color = if (q.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                    Text("+${q.xp} XP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Hint(q.description)
                MeterBar(q.progress, if (q.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, Modifier.padding(top = 3.dp), 6.dp)
            }
        }
        Hint("Quests erneuern sich jeden Montag. Erfüllte Quests geben XP – auch rückwirkend für vergangene Wochen.")
    }
}

@Composable
private fun BossCard(bosses: List<Boss>) {
    SectionCard("Bosskämpfe") {
        bosses.sortedWith(compareBy({ it.defeated }, { it.hp })).forEach { b ->
            Column(Modifier.padding(vertical = 5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${b.name} · ${b.lift.short}", style = MaterialTheme.typography.titleSmall,
                            color = if (b.defeated) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                        Hint(b.kind.title + when (b.kind) {
                            BossKind.PAST_SELF -> " – schlage deinen e1RM von vor 12 Monaten mit deiner aktuellen Form (120 Tage)."
                            BossKind.GATEKEEPER -> " – bewacht den nächsten Rang in der Strongman-Population."
                            BossKind.RECORD -> " – übertriff deinen Allzeit-Rekord in aktueller Form."
                        })
                    }
                    Text(if (b.defeated) "BESIEGT" else "${fmtKg(b.targetKg)} kg", style = MaterialTheme.typography.labelLarge,
                        color = if (b.defeated) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                if (!b.defeated) {
                    MeterBar(b.hp, MaterialTheme.colorScheme.error, Modifier.padding(top = 3.dp), 7.dp)
                    Hint("Lebenspunkte: noch ${fmtKg(b.targetKg - b.currentKg)} kg (aktuell ${fmtKg(b.currentKg)} kg)")
                }
            }
        }
    }
}

@Composable
private fun BadgeGroup(group: String, list: List<Badge>) {
    SectionCard("Abzeichen · $group (${list.count { it.achieved }}/${list.size})") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            list.forEach { b -> BadgeTile(b) }
        }
    }
}

@Composable
private fun BadgeTile(b: Badge) {
    val cs = MaterialTheme.colorScheme
    val color = if (b.achieved) cs.secondary else cs.outline
    Column(
        Modifier.width(148.dp).clip(RoundedCornerShape(12.dp))
            .background(if (b.achieved) cs.secondary.copy(alpha = 0.12f) else cs.surfaceContainerHigh)
            .border(1.dp, color.copy(alpha = if (b.achieved) 0.9f else 0.4f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Text(b.title, style = MaterialTheme.typography.labelLarge, color = if (b.achieved) cs.secondary else cs.onSurface, fontWeight = FontWeight.Bold, maxLines = 2)
        Text(b.description, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant, maxLines = 3)
        if (b.achieved) {
            Text(b.achievedDay?.let { "erreicht ${fmtDay(it)}" } ?: "erreicht", style = MaterialTheme.typography.labelSmall, color = cs.secondary)
        } else {
            Box(Modifier.padding(top = 4.dp)) { MeterBar(b.progress, cs.primary, height = 5.dp) }
            Text("${(b.progress * 100).toInt()} %", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
        }
    }
}

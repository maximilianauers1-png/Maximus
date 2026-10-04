package app.maximus.chat.domain

import app.maximus.dnd.domain.CharacterSheet
import app.maximus.lab.domain.Chapter
import app.maximus.lab.domain.Compendium
import app.maximus.lab.domain.Fmt
import app.maximus.lab.domain.Question
import app.maximus.nutrition.domain.DerivedTargets
import app.maximus.nutrition.domain.NutritionProfile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Compact, factual summaries of what the user is looking at in a module, handed to Maximus as hidden
 * prompt context ("Frag Maximus"). Kept short on purpose (≈ 100–400 tokens): prefill time on the phone
 * grows linearly with the prompt, and small models drown in long contexts.
 */
object ModuleContext {
    private val DAY = DateTimeFormatter.ofPattern("dd.MM.")
    private fun day(epochDay: Long) = LocalDate.ofEpochDay(epochDay).format(DAY)

    // ------------------------------------------------------------------ lab

    /** A quiz question with the user's answer, the correct one and the stored explanation. */
    fun quiz(q: Question, chosen: String?, correct: Boolean): String = buildString {
        append("Quizfrage aus dem Physik- und Mathe-Labor: ").append(q.prompt).append('\n')
        if (q.isChoice) {
            append("Optionen: ").append(q.options.joinToString(" | ")).append('\n')
            append("Richtige Antwort: ").append(q.options[q.correctIndex]).append('\n')
        } else {
            append("Richtiges Ergebnis: ").append(Fmt.num(q.answer, 5)).append(' ').append(q.unit).append('\n')
        }
        if (chosen != null) append("Antwort des Nutzers: ").append(chosen).append(if (correct) " (richtig)" else " (falsch)").append('\n')
        append("Musterlösung: ").append(ChatTools.clip(q.solution, 700)).append('\n')
        q.chapterKey?.let { Compendium.byKey[it] }?.let { ch ->
            append("Formeln aus „").append(ch.title).append("“: ")
            append(ch.formulas.take(6).joinToString("; ") { "${it.name}: ${it.expr}" })
        }
    }

    /** Question text the user sees for a quiz explanation. */
    fun quizQuestion(q: Question, correct: Boolean): String =
        if (correct) "Erkläre mir diese Frage genauer: " + ChatTools.clip(q.prompt.replace('\n', ' '), 160)
        else "Ich habe diese Frage falsch beantwortet. Erkläre mir, warum meine Antwort falsch ist und wie man richtig denkt: " +
            ChatTools.clip(q.prompt.replace('\n', ' '), 160)

    fun chapter(ch: Chapter): String = buildString {
        append("Kompendiumskapitel „").append(ch.title).append("“ (").append(ch.topic.title)
        if (ch.course.isNotEmpty()) append(", Vorlesung ").append(ch.course)
        append("): ").append(ch.summary).append('\n')
        append("Abschnitte: ").append(ch.sections.joinToString("; ") { it.title }).append('\n')
        append("Formeln: ").append(ChatTools.clip(ch.formulas.joinToString("; ") { "${it.name}: ${it.expr}" }, 900))
    }

    // ------------------------------------------------------------------ strongman

    /** One logged set, already resolved to an exercise name. */
    data class LoggedSet(val epochDay: Long, val exercise: String, val weightKg: Double, val reps: Int, val rpe: Double?)

    /** Last [sessions] training days, sets grouped per exercise: "02.10.: Kreuzheben 220×3 @8, 230×1". */
    fun strongman(sets: List<LoggedSet>, sessions: Int = 6): String? {
        if (sets.isEmpty()) return null
        val days = sets.map { it.epochDay }.distinct().sortedDescending().take(sessions).sorted()
        return "Letzte Trainingseinheiten:\n" + days.joinToString("\n") { d ->
            val ofDay = sets.filter { it.epochDay == d }
            day(d) + ": " + ofDay.groupBy { it.exercise }.entries.joinToString("; ") { (ex, list) ->
                ex + " " + list.joinToString(", ") { s ->
                    "${Fmt.num(s.weightKg, 4)}×${s.reps}" + (s.rpe?.let { " @${Fmt.num(it, 2)}" } ?: "")
                }
            }
        }
    }

    // ------------------------------------------------------------------ nutrition

    data class DayLog(val epochDay: Long, val weightKg: Double?, val kcal: Double?, val proteinG: Double?)

    fun nutrition(p: NutritionProfile, t: DerivedTargets, log: List<DayLog>, today: Long): String = buildString {
        append("Ernährungsprofil: ${p.sex.name.lowercase()}, ${p.ageYears} J., ${Fmt.num(p.heightCm, 4)} cm, ${Fmt.num(p.weightKg, 4)} kg")
        p.bodyFatPercent?.let { append(", ${Fmt.num(it, 3)} % KFA") }
        p.goalWeightKg?.let { append(", Zielgewicht ${Fmt.num(it, 4)} kg") }
        append(".\n")
        append("Grundumsatz ${Fmt.num(t.bmr, 4)} kcal, Gesamtumsatz ${Fmt.num(t.tdee, 4)} kcal (${if (t.tdeeSource.name == "ADAPTIVE") "aus dem Protokoll geschätzt" else "Formel"}).\n")
        append("Tagesziel: ${Fmt.num(t.macros.kcal, 4)} kcal, Protein ${Fmt.num(t.macros.proteinG, 3)} g, Fett ${Fmt.num(t.macros.fatG, 3)} g, ")
        append("Kohlenhydrate ${Fmt.num(t.macros.carbG, 3)} g, Ballaststoffe ${Fmt.num(t.macros.fiberG, 3)} g.\n")
        val recent = log.filter { it.epochDay > today - 14 }.sortedBy { it.epochDay }
        if (recent.isNotEmpty()) {
            append("Protokoll der letzten 14 Tage: ")
            append(recent.joinToString("; ") { d ->
                day(d.epochDay) + listOfNotNull(
                    d.weightKg?.let { " ${Fmt.num(it, 4)} kg" }, d.kcal?.let { " ${Fmt.num(it, 4)} kcal" }, d.proteinG?.let { " ${Fmt.num(it, 3)} g Protein" }
                ).joinToString(",")
            })
        }
    }

    // ------------------------------------------------------------------ D&D

    fun dnd(s: CharacterSheet): String = buildString {
        val b = s.build
        append("D&D-Charakter „${b.name.ifBlank { "ohne Namen" }}“: ${s.race.name}, ${s.classLine} (Gesamtstufe ${b.totalLevel}).\n")
        append("Attribute: ").append(s.scores.entries.joinToString(", ") { (a, v) -> "${a.short} $v (${if (s.modifiers.getValue(a) >= 0) "+" else ""}${s.modifiers.getValue(a)})" }).append('\n')
        append("TP ${s.maxHp}, RK ${s.armorClass}, Initiative ${s.initiative}, Übungsbonus +${s.proficiency}, Bewegung ${s.speed} ft.\n")
        if (s.spellcasting.isNotEmpty()) {
            append("Zauberwirken: ").append(s.spellcasting.joinToString("; ") { "${it.className} (${it.ability.short}, SG ${it.saveDc}, Angriff +${it.attackBonus})" })
            val slots = s.spellSlots.withIndex().filter { it.value > 0 }.joinToString(" ") { "${it.index + 1}:${it.value}" }
            if (slots.isNotEmpty()) append(", Plätze ").append(slots)
            if (s.pactSlots.first > 0) append(", Paktplätze ${s.pactSlots.first}× Grad ${s.pactSlots.second}")
            append('\n')
        }
        val attacks = s.attacks + s.spellAttacks
        if (attacks.isNotEmpty()) append("Angriffe: ").append(attacks.take(5).joinToString("; ") { "${it.name} +${it.attackBonus}, ${it.damage} ${it.damageType}" }).append('\n')
        if (s.feats.isNotEmpty()) append("Feats: ").append(s.feats.joinToString { it.name }).append('\n')
        val options = (b.invocationKeys + b.metamagicKeys + listOfNotNull(b.pactBoon) + b.fightingStyles)
        if (options.isNotEmpty()) append("Klassenoptionen: ").append(options.joinToString()).append('\n')
        if (b.spellKeys.isNotEmpty()) append("Zauber: ").append(ChatTools.clip(b.spellKeys.joinToString(), 400)).append('\n')
        if (s.resources.isNotEmpty()) append("Ressourcen: ").append(s.resources.joinToString { "${it.first} ${it.second}" })
    }
}

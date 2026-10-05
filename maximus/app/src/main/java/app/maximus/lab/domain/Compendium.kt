package app.maximus.lab.domain

/** A named formula; every formula also becomes a flashcard. */
data class Formula(val name: String, val expr: String, val note: String = "")

/**
 * A section of a chapter. [body] uses a tiny markup: paragraphs are separated by blank lines, lines
 * starting with "• " are bullets, and lines starting with "> " are highlighted key statements.
 */
data class Section(val title: String, val body: String, val formulas: List<Formula> = emptyList())

data class Chapter(
    val key: String,
    val topic: Topic,
    /** 1 = Grundlagen, 2 = Vertiefung, 3 = Fortgeschritten. */
    val level: Int,
    val title: String,
    val summary: String,
    val sections: List<Section>,
    val calculatorKeys: List<String> = emptyList(),
    /** Lecture the chapter belongs to, e.g. "Analysis II"; used as a filter for revising a course. */
    val course: String = ""
) {
    val formulas: List<Formula> get() = sections.flatMap { it.formulas }
}

data class FlashCard(val id: String, val chapter: Chapter, val formula: Formula)

internal fun chapter(key: String, topic: Topic, level: Int, title: String, summary: String, calcs: List<String>, vararg sections: Section) =
    Chapter(key, topic, level, title, summary, sections.toList(), calcs)

internal fun course(name: String, vararg chapters: Chapter) = chapters.map { it.copy(course = name) }

internal fun sec(title: String, body: String, vararg formulas: Formula) = Section(title, body.trimIndent(), formulas.toList())

internal fun fm(name: String, expr: String, note: String = "") = Formula(name, expr, note)

object Compendium {
    val all: List<Chapter> by lazy {
        CompendiumThermo.chapters + CompendiumElectro.chapters + CompendiumQuantum.chapters + CompendiumSemiconductor.chapters +
            CompendiumCaloric.chapters + CompendiumPvdf.chapters + CompendiumQft.chapters + CompendiumPolymer.chapters +
            CompendiumElectrical.chapters + CompendiumAi.chapters + CompendiumMechanics.chapters + CompendiumMath.chapters +
            CompendiumExtra.chapters
    }
    val byKey: Map<String, Chapter> by lazy { all.associateBy { it.key } }
    fun forTopic(t: Topic) = all.filter { it.topic == t }

    /** Courses (lectures) of a topic in their natural order of study. */
    fun courses(t: Topic): List<String> = forTopic(t).map { it.course }.filter { it.isNotEmpty() }.distinct()

    val flashcards: List<FlashCard> by lazy {
        all.flatMap { ch -> ch.formulas.mapIndexed { i, f -> FlashCard("${ch.key}#$i", ch, f) } }
    }
    val flashcardById: Map<String, FlashCard> by lazy { flashcards.associateBy { it.id } }

    fun levelLabel(level: Int) = when (level) { 1 -> "Grundlagen"; 2 -> "Vertiefung"; else -> "Fortgeschritten" }
}

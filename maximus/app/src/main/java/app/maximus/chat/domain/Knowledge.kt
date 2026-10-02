package app.maximus.chat.domain

import app.maximus.dnd.domain.EldritchInvocations
import app.maximus.dnd.domain.FightingStyles
import app.maximus.dnd.domain.MetamagicOptions
import app.maximus.dnd.domain.PactBoons
import app.maximus.dnd.domain.Spells
import app.maximus.dnd.domain.SrdFeats
import app.maximus.dnd.domain.SupplementFeats
import app.maximus.dnd.domain.SupplementSpells
import app.maximus.lab.domain.Compendium
import kotlin.math.ln

/** One retrievable passage of the app's own knowledge (lab compendium, D&D content). */
data class KnowledgeDoc(val id: String, val source: String, val title: String, val text: String)

data class Hit(val doc: KnowledgeDoc, val score: Double)

/**
 * Okapi BM25 over the app's built-in texts. Small models hallucinate formulas and rules; injecting the
 * matching compendium section or spell text ("retrieval-augmented generation") grounds the answer,
 * entirely offline and for a few hundred prompt tokens.
 *
 *   score(D, Q) = Σ_{t∈Q} idf(t) · f(t,D)(k₁ + 1) / (f(t,D) + k₁(1 − b + b|D|/avgdl)),
 *   idf(t) = ln(1 + (N − n_t + ½)/(n_t + ½)),   k₁ = 1.2, b = 0.75.
 */
class Bm25Index(val docs: List<KnowledgeDoc>, private val k1: Double = 1.2, private val b: Double = 0.75) {
    private val termFreqs: List<Map<String, Int>> = docs.map { d ->
        // Titles count three times: a hit in "Satz von Stokes" matters more than one in a long body.
        (Text.terms(d.title).let { it + it + it } + Text.terms(d.text)).groupingBy { it }.eachCount()
    }
    private val lengths = termFreqs.map { it.values.sum().toDouble() }
    private val avgLength = lengths.average().takeIf { it > 0 } ?: 1.0
    private val docFreq: Map<String, Int> = HashMap<String, Int>().also { m -> termFreqs.forEach { tf -> tf.keys.forEach { m[it] = (m[it] ?: 0) + 1 } } }

    fun idf(term: String): Double {
        val n = docFreq[term] ?: 0
        return ln(1 + (docs.size - n + 0.5) / (n + 0.5))
    }

    fun search(query: String, limit: Int = 3): List<Hit> {
        val q = Text.terms(query).distinct()
        if (q.isEmpty()) return emptyList()
        return docs.indices.map { i ->
            val tf = termFreqs[i]
            var s = 0.0
            for (t in q) {
                val f = tf[t] ?: continue
                s += idf(t) * f * (k1 + 1) / (f + k1 * (1 - b + b * lengths[i] / avgLength))
            }
            Hit(docs[i], s)
        }.filter { it.score > 0 }.sortedByDescending { it.score }.take(limit)
    }
}

/** Tokenisation shared by retrieval and lookups: lower case, umlauts kept, crude German/English suffix stripping. */
object Text {
    private val STOP = setOf(
        "der", "die", "das", "und", "oder", "ist", "sind", "ein", "eine", "einer", "eines", "einem", "einen", "den", "dem", "des",
        "mit", "von", "für", "auf", "aus", "bei", "wie", "was", "wer", "wann", "warum", "wieso", "welche", "welcher", "welches",
        "im", "in", "zu", "zum", "zur", "an", "am", "es", "ich", "du", "mir", "mich", "mein", "meine", "kann", "können", "man",
        "nicht", "auch", "noch", "nur", "so", "wenn", "dann", "als", "the", "a", "an", "of", "to", "is", "and", "or", "for",
        "erkläre", "erklär", "bitte", "mal", "gib", "zeig", "sag", "über", "uns", "bzw", "dass", "da", "hier", "dies", "diese"
    )
    private val SUFFIXES = listOf("ungen", "ung", "en", "er", "es", "em", "e", "n", "s")

    fun normalise(token: String): String {
        var t = token
        for (s in SUFFIXES) if (t.length - s.length >= 4 && t.endsWith(s)) { t = t.dropLast(s.length); break }
        return t
    }

    fun terms(text: String): List<String> =
        text.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 2 && it !in STOP }.map(::normalise)
}

/** Lazily built corpora; building the compendium index takes a few milliseconds and happens once. */
object Knowledge {
    val compendium: Bm25Index by lazy {
        Bm25Index(Compendium.all.flatMap { ch ->
            ch.sections.mapIndexed { i, s ->
                KnowledgeDoc(
                    "${ch.key}#$i", "Kompendium · ${ch.topic.title}", "${ch.title} – ${s.title}",
                    s.body + if (s.formulas.isEmpty()) "" else "\n" + s.formulas.joinToString("\n") { "${it.name}: ${it.expr}" }
                )
            }
        })
    }

    /** D&D entries with their kind, used for exact name lookups and for retrieval. */
    val dnd: List<KnowledgeDoc> by lazy {
        val spells = (Spells.all + SupplementSpells.all).distinctBy { it.key }.map { s ->
            KnowledgeDoc(
                "spell:${s.key}", "Zauber", s.name,
                "${s.levelLabel}, ${s.school.label}${if (s.concentration) ", Konzentration" else ""}${if (s.ritual) ", Ritual" else ""}. " +
                    "Zeitaufwand ${s.castingTime}, Reichweite ${s.range}, Komponenten ${s.components}, Dauer ${s.duration}. " +
                    "Klassen: ${s.classes.joinToString()}. ${s.text}"
            )
        }
        val feats = (SrdFeats.all + SupplementFeats.all).distinctBy { it.key }.map { f ->
            KnowledgeDoc("feat:${f.key}", "Feat", f.name, "Voraussetzung: ${f.prerequisite}. ${f.text}")
        }
        val options = listOf(
            "Eldritch Invocation" to EldritchInvocations.all, "Metamagic" to MetamagicOptions.all,
            "Pact Boon" to PactBoons.all, "Fighting Style" to FightingStyles.all
        ).flatMap { (kind, list) ->
            list.map { o -> KnowledgeDoc("opt:${o.key}", kind, o.name, "Voraussetzung: ${o.prerequisite}, ab Stufe ${o.minLevel}. ${o.text}") }
        }
        spells + feats + options
    }

    val dndIndex: Bm25Index by lazy { Bm25Index(dnd) }

    /**
     * D&D entries whose full name occurs in [text] as a whole phrase ("Eldritch Blast", "Hex"), longest
     * names first, so "Hex" does not shadow "Hexblade's Curse"; overlapping shorter matches are dropped.
     */
    fun dndMentions(text: String, limit: Int = 3): List<KnowledgeDoc> {
        val lower = " " + text.lowercase().replace(Regex("[^\\p{L}\\p{N}']+"), " ") + " "
        val taken = ArrayList<IntRange>()
        val out = ArrayList<KnowledgeDoc>()
        for (d in dnd.sortedByDescending { it.title.length }) {
            val name = " " + d.title.lowercase().replace(Regex("[^\\p{L}\\p{N}']+"), " ").trim() + " "
            if (name.length < 4) continue
            val at = lower.indexOf(name)
            if (at < 0) continue
            val range = at until at + name.length
            if (taken.any { it.first < range.last && range.first < it.last }) continue
            taken += range
            out += d
            if (out.size == limit) break
        }
        return out
    }

    /** Best D&D entry for a lookup query: exact name, then name prefix, then edit distance ≤ 2, then BM25. */
    fun lookupDnd(query: String): KnowledgeDoc? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null
        dnd.firstOrNull { it.title.lowercase() == q }?.let { return it }
        dnd.filter { it.title.lowercase().startsWith(q) }.minByOrNull { it.title.length }?.let { return it }
        dnd.map { it to levenshtein(it.title.lowercase(), q) }.filter { it.second <= 2 }.minByOrNull { it.second }?.let { return it.first }
        return dndIndex.search(query, 1).firstOrNull()?.doc
    }

    fun levenshtein(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1)
            cur[0] = i
            for (j in 1..b.length) cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            prev = cur
        }
        return prev[b.length]
    }
}

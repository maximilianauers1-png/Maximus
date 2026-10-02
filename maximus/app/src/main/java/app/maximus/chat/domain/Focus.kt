package app.maximus.chat.domain

/** Sampling parameters for one generation. */
data class Sampling(val temperature: Float, val topK: Int, val topP: Float)

/**
 * Subject focus of a message. It selects a short instruction (small models follow short prompts better,
 * and every prompt token costs prefill time), the sampling temperature and which tools may run on their own.
 */
enum class Focus(val label: String, val instruction: String, val sampling: Sampling) {
    GENERAL(
        "Allgemein",
        "Beantworte allgemeine Fragen sachlich und knapp.",
        Sampling(0.6f, 40, 0.95f)
    ),
    STRONGMAN(
        "Strongman",
        "Fokus Kraftsport und Strongman: Programmierung (Conjugate, RPE, Prilepin, Deload), Technik der Events " +
            "(Log, Stein, Yoke, Farmer's, Kreuzheben), Regeneration und Ernährung. Nenne konkrete Gewichte, Sätze und " +
            "Wiederholungen; Sicherheit geht vor.",
        Sampling(0.5f, 40, 0.95f)
    ),
    DND(
        "D&D",
        "Fokus Dungeons & Dragons 5e (Regeln von 2014, SRD und Ergänzungen): Regeln exakt und mit Quelle, Builds " +
            "(Hexblade, Sorlock, Sorcadin, Nuclear Wizard), Taktik, Abenteuerideen. Würfelergebnisse kommen nur aus dem Würfelwerkzeug.",
        Sampling(0.8f, 64, 0.95f)
    ),
    CODE(
        "Code",
        "Fokus Programmierung: korrekter, idiomatischer Code in Codeblöcken mit Sprachangabe (```kotlin), danach eine " +
            "kurze Erklärung mit Laufzeitkomplexität. Keine erfundenen APIs.",
        Sampling(0.2f, 20, 0.9f)
    ),
    SCIENCE(
        "Mathe & Physik",
        "Fokus Mathematik und Physik auf Universitätsniveau: Herleitungen Schritt für Schritt, Einheiten und Grenzfälle " +
            "prüfen, Formeln in Unicode (∫, ∂, √, ħ, ², ₀). Nutze den Kompendium-Kontext, wenn er mitgeliefert wird.",
        Sampling(0.3f, 32, 0.9f)
    );

    companion object {
        /** Persona shared by all foci; deliberately short. */
        const val PERSONA =
            "Du bist Maximus, ein KI-Assistent, der vollständig offline auf dem Smartphone deines Nutzers läuft. " +
                "Dein Nutzer ist Physiker, trainiert Strongman und spielt D&D. Antworte auf Deutsch (außer bei Code und " +
                "englischen Regelbegriffen), präzise und ohne Floskeln, mit Markdown (Überschriften, Listen, **fett**). " +
                "Wenn du etwas nicht sicher weißt, sag es. Werkzeugergebnisse sind exakt berechnet: übernimm ihre Zahlen unverändert."
    }
}

/** Keyword scoring for the automatic focus. Prefix matching on normalised tokens tolerates German inflection. */
object FocusDetector {
    private val KEYWORDS: Map<Focus, List<String>> = mapOf(
        Focus.STRONGMAN to listOf(
            "strongman", "kniebeug", "squat", "bankdrück", "bench", "kreuzheb", "deadlift", "log", "yoke", "atlas", "steine",
            "farmer", "rpe", "1rm", "e1rm", "einerwiederhol", "arbeitssätze", "wiederhol", "deload", "training", "trainier",
            "hypertroph", "maximalkraft", "conjugate", "westside", "prilepin", "aufwärm", "hantel", "langhantel", "griffkraft",
            "overhead", "schulterdrück", "zughilfe", "gürtel", "regeneration", "protein", "muskel", "wettkampf", "sandbag", "keg"
        ),
        Focus.DND to listOf(
            "d&d", "dnd", "d20", "w20", "zauber", "spell", "hexblade", "warlock", "sorcerer", "sorlock", "sorcadin", "paladin",
            "wizard", "magier", "hexenmeister", "smite", "eldritch", "invocation", "metamagic", "rüstungsklasse", "initiative",
            "kampagne", "dungeon", "spielleiter", "monster", "feat", "multiclass", "subclass", "cantrip", "slot", "rettungswurf",
            "saving", "concentration", "konzentration", "character", "charakter", "level", "stufe", "barbar", "druid", "cleric",
            "kleriker", "rogue", "schurke", "ranger", "bard", "barde", "fighter", "kämpfer", "goblin", "drache", "dragon", "hex"
        ),
        Focus.CODE to listOf(
            "kotlin", "python", "java", "c++", "cpp", "rust", "javascript", "typescript", "code", "programmier", "funktion",
            "function", "klasse", "class", "bug", "compile", "kompil", "exception", "fehlermeldung", "stacktrace", "compose",
            "android", "gradle", "git", "sql", "regex", "algorithm", "algorithmus", "datenstruktur", "array", "liste", "hashmap",
            "rekursion", "coroutine", "thread", "api", "json", "numpy", "matplotlib", "script", "skript", "debug", "refactor"
        ),
        Focus.SCIENCE to listOf(
            "integral", "integrier", "ableit", "differenz", "matrix", "matriz", "eigenwert", "eigenvektor", "hamilton", "lagrange",
            "schrödinger", "quant", "thermodynam", "entropie", "maxwell", "tensor", "fourier", "laplace", "beweis", "beweise",
            "energie", "impuls", "halbleiter", "feldtheorie", "qft", "feynman", "relativität", "lorentz", "vektor", "gradient",
            "divergenz", "rotation", "konverg", "reihe", "grenzwert", "dgl", "differentialgleich", "physik", "mathe", "analysis",
            "topologie", "maß", "lebesgue", "hilbert", "operator", "spin", "elektron", "photon", "teilchen", "kalorik", "kalorisch",
            "wellenfunktion", "potential", "hamiltonian", "statistik", "wahrscheinlich", "varianz", "residu", "holomorph"
        )
    )

    private val SYMBOLS: Map<Focus, Regex> = mapOf(
        Focus.SCIENCE to Regex("[∫∂∇∑√ħ]|\\\\(frac|int|partial|sum)|\\b[a-z]\\^\\d"),
        Focus.CODE to Regex("```|\\bfun |\\bdef |;\\s*$|\\{\\s*$|=>|->\\s*\\{", RegexOption.MULTILINE),
        Focus.DND to Regex("\\b\\d*[dw](4|6|8|10|12|20|100)\\b", RegexOption.IGNORE_CASE),
        Focus.STRONGMAN to Regex("\\b\\d{2,3}(?:[.,]\\d+)?\\s*kg\\s*[x×*]\\s*\\d{1,2}\\b", RegexOption.IGNORE_CASE)
    )

    fun tokens(text: String): List<String> =
        text.lowercase().split(Regex("[^\\p{L}\\p{N}+&#]+")).filter { it.isNotEmpty() }

    /** Scores per focus: 1 per keyword hit (prefix match), 2 per symbol pattern hit. */
    fun scores(text: String): Map<Focus, Int> {
        val toks = tokens(text)
        return KEYWORDS.mapValues { (f, keys) ->
            toks.count { t -> keys.any { k -> t.startsWith(k) && (k.length >= 4 || t == k) } } +
                2 * (SYMBOLS[f]?.findAll(text)?.count() ?: 0)
        }
    }

    /** Best-scoring focus; without any signal the previous focus of the conversation is kept. */
    fun detect(text: String, previous: Focus? = null): Focus {
        val s = scores(text)
        val best = s.maxByOrNull { it.value } ?: return previous ?: Focus.GENERAL
        return if (best.value == 0) previous ?: Focus.GENERAL else best.key
    }
}

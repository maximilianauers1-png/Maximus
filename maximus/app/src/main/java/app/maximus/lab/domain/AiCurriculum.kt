package app.maximus.lab.domain

/**
 * The AI learning path: every AI chapter in the order of study, grouped into courses and numbered as
 * "Happen" (bites) 1…N. The path is the single source of order and course names for Topic.AI.
 */
object AiCurriculum {
    val path: List<Pair<String, List<String>>> = listOf(
        "AI von Null" to listOf("ai0_what", "ai0_data", "ai0_model", "ai0_types", "ai0_math", "ai0_workflow", "ai0_tools"),
        "ML: Die sieben Techniken" to listOf(
            "ml_techniques", "ml_regression", "ml_classification", "ml_clustering", "ml_association",
            "ml_anomaly", "ml_sequence", "ml_recommender", "ml_dimred"
        ),
        "ML-Algorithmen im Detail" to listOf("alg_knn", "alg_trees", "alg_ensembles", "alg_logreg", "alg_svm", "alg_bayes", "alg_kmeans", "alg_hier_dbscan"),
        "Bewertung und Datenpraxis" to listOf("ev_split", "ev_metrics", "ev_features", "ev_tuning", "ev_fair"),
        "Mathematik des Lernens" to listOf("ai_intro", "ai_linear"),
        "Deep Learning" to listOf(
            "dl_neuron", "ai_nn", "dl_backprop_numbers", "dl_keras", "dl_pytorch", "ai_training", "ai_arch",
            "dl_cnn", "dl_rnn", "dl_transfer", "dl_autoencoder", "dl_generative", "ai_transformer"
        ),
        "Reinforcement Learning" to listOf("rl_basics", "rl_qlearning", "rl_deep"),
        "Generative KI und LLMs" to listOf("ai_llm", "ai_inference", "gen_prompting", "gen_vector", "ai_rag_agents", "gen_frameworks", "gen_finetune", "gen_eval"),
        "AI Engineering: vom Notebook zur Anwendung" to listOf("eng_deploy", "eng_spark", "ai_mlops", "eng_mlops", "eng_capstone")
    )

    val keys: List<String> = path.flatMap { it.second }

    private val raw: List<Chapter> by lazy {
        CompendiumAiZero.chapters + CompendiumAiMl.chapters + CompendiumAiAlgo.chapters + CompendiumAi.chapters +
            CompendiumAiDeep.chapters + CompendiumAiGen.chapters
    }

    /** All AI chapters in path order, titled "Happen n · …", with the course of the path. */
    val chapters: List<Chapter> by lazy {
        val byKey = raw.associateBy { it.key }
        var n = 0
        path.flatMap { (course, ks) ->
            ks.mapNotNull { k -> byKey[k]?.let { ch -> ch.copy(title = "Happen ${++n} · ${ch.title}", course = course) } }
        } + raw.filter { it.key !in keys }
    }

    /** 1-based position of a chapter on the path, or null for chapters outside it. */
    fun bite(key: String): Int? = keys.indexOf(key).takeIf { it >= 0 }?.plus(1)

    /** The first chapter of the path the learner has not read yet. */
    fun next(read: Set<String>): String? = keys.firstOrNull { it !in read }
}

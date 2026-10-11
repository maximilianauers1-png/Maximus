package app.maximus.ai.domain

/**
 * The curriculum: thirteen lessons in the order of the IBM AI Engineering Professional Certificate.
 * Each lesson lists its bites in study order; bites are numbered 1…N across the whole course.
 * This object is the single source of order, lesson membership and question pools.
 */
object AiCourse {
    val lessons: List<Lesson> = listOf(
        Lesson(
            1, "Maschinelles Lernen mit Python", "Machine Learning with Python", 20,
            "Vom ersten Datensatz bis zur Pipeline: Regression, Klassifikation, Clustering, Dimensionsreduktion, Bewertung und scikit-learn.",
            Champion("Sir Regressus von Kleinstquadrat", "Bewahrer der Residuen", "Mein Fehlerquadrat ist kleiner als deines – beweise das Gegenteil!"),
            listOf(
                "ai0_what", "ai0_data", "ai0_model", "ai0_types", "ai0_math", "ai0_workflow", "ai0_tools", "ml_sklearn",
                "ml_techniques", "ml_regression", "ml_linreg_code", "ml_polyreg", "ml_classification", "alg_logreg", "alg_knn",
                "alg_trees", "alg_svm", "alg_bayes", "alg_ensembles", "ml_clustering", "alg_kmeans", "alg_hier_dbscan", "ml_dimred",
                "ml_association", "ml_anomaly", "ml_sequence", "ml_recommender", "ev_split", "ev_metrics", "ev_features",
                "ml_regularization", "ev_tuning", "ml_pipelines", "ev_fair", "ai_intro", "ai_linear", "eng_capstone"
            )
        ),
        Lesson(
            2, "Einführung in Deep Learning und neuronale Netze mit Keras", "Introduction to Deep Learning & Neural Networks with Keras", 10,
            "Neuronen, Vorwärtsausbreitung, Backpropagation, Aktivierungen, die ersten Keras-Modelle und ein Blick auf CNN und RNN.",
            Champion("Markgräfin Neurona vom Feuernden Axon", "Herrin der Synapsen", "Meine Gewichte sind geschmiedet – deine noch zufällig initialisiert."),
            listOf(
                "dl_neuron", "ai_nn", "dl_forward", "dl_backprop_numbers", "dl_vanishing", "dl_keras", "keras_regression",
                "keras_classification", "ai_arch", "dl_cnn", "dl_rnn"
            )
        ),
        Lesson(
            3, "Deep Learning mit Keras und TensorFlow", "Deep Learning with Keras and TensorFlow", 23,
            "TensorFlow 2, funktionale API, eigene Schichten, Augmentation, Transfer Learning, Transformer, Autoencoder, GANs, Diffusion und RL.",
            Champion("Baron Tensorius vom Rechengraphen", "Kastellan der Keras-Burg", "Mein Graph ist kompiliert, mein Gradientenband zeichnet jeden deiner Fehler auf."),
            listOf(
                "tf_basics", "keras_functional", "keras_custom_layers", "keras_augment", "dl_transfer", "keras_transformer",
                "dl_autoencoder", "dl_generative", "keras_custom_training", "keras_optimize", "rl_basics", "rl_qlearning", "rl_deep"
            )
        ),
        Lesson(
            4, "Einführung in neuronale Netze und PyTorch", "Introduction to Neural Networks and PyTorch", 19,
            "Tensoren, Autograd, Datasets, lineare und logistische Regression – alles von Hand und dann „the PyTorch way“.",
            Champion("Graf Autograd zu Fackelstein", "Träger der ewigen Fackel", "Ich rufe backward() – und jeder deiner Irrtümer erhält seinen Gradienten."),
            listOf("dl_pytorch", "pt_tensors", "pt_autograd", "pt_dataset", "pt_linreg", "pt_sgd", "pt_multi", "pt_logreg")
        ),
        Lesson(
            5, "Deep Learning mit PyTorch", "Deep Learning with PyTorch", 21,
            "Softmax, flache und tiefe Netze, Dropout, Initialisierung, Momentum, Batch-Norm, CNNs und vortrainierte Modelle.",
            Champion("Ritterin Dropout die Vergessliche", "Schwester der zufälligen Stille", "Die Hälfte meiner Neuronen schläft – und dennoch schlage ich dich."),
            listOf("pt_softmax", "pt_shallow", "pt_deep", "pt_dropout", "pt_momentum_bn", "ai_training", "pt_cnn", "pt_pretrained")
        ),
        Lesson(
            6, "AI-Capstone-Projekt mit Deep Learning", "AI Capstone Project with Deep Learning", 15,
            "Satellitenbilder klassifizieren: Daten laden, CNN in Keras und PyTorch, Vision Transformer, Hybride, Vergleich und Betrieb.",
            Champion("Der Landvermesser von Satellitenau", "Kartograf aus dem Orbit", "Acker oder Wald? Ich erkenne es aus vierhundert Meilen Höhe."),
            listOf("cap_data", "cap_keras_cnn", "cap_pytorch_cnn", "cap_vit", "cap_hybrid", "eng_deploy", "eng_spark", "ai_mlops", "eng_mlops")
        ),
        Lesson(
            7, "Generative KI und LLMs: Architektur und Datenvorbereitung", "Generative AI and LLMs: Architecture and Data Preparation", 6,
            "Generative Architekturen, LLM-Familien, Hugging Face und LangChain, Tokenisierung und Datenlader für Text.",
            Champion("Herold Token von Zerlegung", "Zerteiler aller Wörter", "Ich zerschneide deinen Satz in Teilwörter, ehe du ihn zu Ende gesprochen hast."),
            listOf("gen_arch", "gen_llm_families", "gen_libs", "tok_basics", "tok_subword", "nlp_dataloader")
        ),
        Lesson(
            8, "Fundamentmodelle für NLP und Sprachverständnis", "Gen AI Foundational Models for NLP & Language Understanding", 10,
            "One-Hot und Bag of Words, Dokumentklassifikation, N-Gramme, Word2Vec und GloVe, Seq2Seq und Metriken wie BLEU.",
            Champion("Meister Skip-Gramm vom Kontextfenster", "Deuter der Nachbarwörter", "Sag mir deine Nachbarn, und ich sage dir, wer du bist."),
            listOf("nlp_onehot", "nlp_doc_class", "nlp_ngram", "nlp_word2vec", "nlp_seq2seq", "nlp_metrics")
        ),
        Lesson(
            9, "Sprachmodellierung mit Transformern", "Generative AI Language Modeling with Transformers", 9,
            "Positionskodierung, Attention, Multi-Head, Transformer-Block, GPT als Decoder, BERT als Encoder, Übersetzung.",
            Champion("Herzogin Attentia die Allsehende", "Wächterin über Query, Key und Value", "Jedes deiner Tokens beachte ich zugleich – du kannst dich nicht verstecken."),
            listOf("tr_posenc", "tr_attention", "tr_multihead", "ai_transformer", "tr_gpt", "tr_bert", "tr_translation")
        ),
        Lesson(
            10, "Generative AI Engineering und Fine-Tuning von Transformern", "Generative AI Engineering and Fine-Tuning Transformers", 8,
            "Hugging Face und PyTorch, Vortraining, volles Fine-Tuning, PEFT, LoRA und QLoRA, Inferenz und Quantisierung.",
            Champion("Feinschmied LoRA von Niedrigrang", "Meister der kleinen Matrizen", "Ich ändere nur ein Promille der Gewichte – und das Modell gehorcht mir."),
            listOf("ft_hf_pt", "ai_llm", "ft_full", "gen_finetune", "ft_peft", "ft_lora", "ai_inference")
        ),
        Lesson(
            11, "Fortgeschrittenes Fine-Tuning für LLMs", "Generative AI Advanced Fine-Tuning for LLMs", 9,
            "Instruction Tuning, Belohnungsmodelle, RLHF mit PPO, Direct Preference Optimization und die TRL-Bibliothek.",
            Champion("Kardinal Präferenz vom Belohnungsmodell", "Richter über gute und schlechte Antworten", "Von zwei Antworten wähle ich die bessere – und deine ist die schlechtere."),
            listOf("aft_instruct", "aft_reward", "aft_rlhf", "aft_dpo", "aft_trl")
        ),
        Lesson(
            12, "Grundlagen von AI-Agenten mit RAG und LangChain", "Fundamentals of AI Agents Using RAG and LangChain", 9,
            "Prompt Engineering, Embeddings, DPR und FAISS, RAG-Pipelines, LangChain mit LCEL, Gedächtnis, Werkzeuge und Agenten.",
            Champion("Der Bibliothekar von Retrievia", "Hüter des Vektorgewölbes", "In meinen Regalen finde ich jede Antwort – findest du die deine?"),
            listOf("gen_prompting", "gen_vector", "rag_dpr", "rag_pipeline", "ai_rag_agents", "gen_frameworks", "lc_lcel", "lc_agents")
        ),
        Lesson(
            13, "Projekt: Generative AI-Anwendungen mit RAG und LangChain", "Project: Generative AI Applications with RAG and LangChain", 9,
            "Ein QA-Bot über eigene Dokumente: Loader, Splitter, Embeddings, Vektorspeicher, Retriever, Gradio-Oberfläche und Bewertung.",
            Champion("Königin Kettenreim von Langkette", "Herrscherin über Loader und Retriever", "Meine Kette hat kein schwaches Glied. Hat deine eines?"),
            listOf("prj_loaders", "prj_splitters", "prj_embed_store", "prj_retrievers", "prj_qabot", "gen_eval")
        )
    )

    private val rawBites: List<Bite> by lazy {
        CompendiumAiZero.bites + CompendiumAiMl.bites + CompendiumAiAlgo.bites + CompendiumAi.bites + CompendiumAiDeep.bites +
            CompendiumAiGen.bites + AiL01Ml.bites + AiL02Keras.bites + AiL03Tensorflow.bites + AiL04Torch.bites + AiL05DeepTorch.bites +
            AiL06Capstone.bites + AiL07GenArch.bites + AiL08Nlp.bites + AiL09Transformers.bites + AiL10FineTune.bites +
            AiL11Preference.bites + AiL12Rag.bites + AiL13Project.bites
    }

    /** All bites in study order, with lesson and global number. */
    val bites: List<Bite> by lazy {
        val byKey = rawBites.associateBy { it.key }
        var n = 0
        lessons.flatMap { l -> l.biteKeys.mapNotNull { k -> byKey[k]?.copy(lesson = l.number, number = ++n) } }
    }

    val byKey: Map<String, Bite> by lazy { bites.associateBy { it.key } }
    val keys: List<String> by lazy { bites.map { it.key } }
    fun lesson(n: Int): Lesson = lessons[n - 1]
    fun bitesOf(lesson: Int): List<Bite> = bites.filter { it.lesson == lesson }
    fun lessonOf(biteKey: String): Int = byKey[biteKey]?.lesson ?: 0

    /** Every hand-written question (concept, code, glossary). */
    val questions: List<AiQuestion> by lazy {
        QuizAi.all + QuizAiConcepts.all + AiGlossary.questions + AiL01Ml.questions + AiL02Keras.questions + AiL03Tensorflow.questions +
            AiL04Torch.questions + AiL05DeepTorch.questions + AiL06Capstone.questions + AiL07GenArch.questions + AiL08Nlp.questions +
            AiL09Transformers.questions + AiL10FineTune.questions + AiL11Preference.questions + AiL12Rag.questions + AiL13Project.questions +
            AiCodeDrills.questions
    }

    val questionsByBite: Map<String, List<AiQuestion>> by lazy { questions.groupBy { it.bite } }
    val questionById: Map<String, AiQuestion> by lazy { questions.associateBy { it.id } }

    /** Calculation tasks, turned into multiple choice by the engine. */
    val tasks: List<TaskGenerator> by lazy { QuizAiNumeric.all + QuizAiSizes.all }

    /** Bite of each generator, read from a sample (a generator always stays within one bite). */
    val taskBite: Map<TaskGenerator, String> by lazy { tasks.associateWith { it.make(kotlin.random.Random(0)).bite } }

    /** The first bite the learner has not read yet. */
    fun next(read: Set<String>): Bite? = bites.firstOrNull { it.key !in read }
}

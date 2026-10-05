package app.maximus.core.app

import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.ui.theme.Design
import app.maximus.ui.theme.DesignConcept
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Persists the chosen design concept (app_meta "ui.design") and mirrors it into [Design.concept]. */
@Singleton
class DesignRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    suspend fun load() {
        val stored = withContext(Dispatchers.IO) { database.get().appMetaDao().get(KEY) }
        DesignConcept.entries.firstOrNull { it.name == stored }?.let { Design.concept = it }
    }

    suspend fun select(concept: DesignConcept) {
        Design.concept = concept
        withContext(Dispatchers.IO) { database.get().appMetaDao().upsert(AppMetaEntity(KEY, concept.name)) }
    }

    companion object {
        const val KEY = "ui.design"
    }
}

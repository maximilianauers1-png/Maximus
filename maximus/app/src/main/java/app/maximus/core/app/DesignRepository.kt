package app.maximus.core.app

import app.maximus.data.db.AppMetaEntity
import app.maximus.data.db.MaximusDatabase
import app.maximus.ui.theme.Design
import app.maximus.ui.theme.BrandArtwork
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Persists the selected hero and avatar on this device. */
@Singleton
class DesignRepository @Inject constructor(private val database: Lazy<MaximusDatabase>) {
    suspend fun load() {
        val stored = withContext(Dispatchers.IO) { database.get().appMetaDao().get(KEY) }
        Design.artwork = BrandArtwork.entries.firstOrNull { it.name == stored } ?: BrandArtwork.ATHLETE
    }

    suspend fun select(artwork: BrandArtwork) {
        withContext(Dispatchers.IO) { database.get().appMetaDao().upsert(AppMetaEntity(KEY, artwork.name)) }
        Design.artwork = artwork
    }

    companion object {
        const val KEY = "ui.artwork"
    }
}

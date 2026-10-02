package app.maximus.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Key/value table for schema stamps and app-level metadata. */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String
)

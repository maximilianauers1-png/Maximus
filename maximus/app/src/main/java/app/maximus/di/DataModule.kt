package app.maximus.di

import android.content.Context
import androidx.room.Room
import app.maximus.core.security.DatabaseKeyManager
import app.maximus.data.db.MIGRATION_1_2
import app.maximus.data.db.MIGRATION_2_3
import app.maximus.data.db.MIGRATION_3_4
import app.maximus.data.db.MIGRATION_4_5
import app.maximus.data.db.MaximusDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabaseKeyManager(@ApplicationContext context: Context): DatabaseKeyManager =
        DatabaseKeyManager(
            directory = context.noBackupFilesDir,
            kekAlias = DatabaseKeyManager.DEFAULT_KEK_ALIAS,
            keyFileName = DatabaseKeyManager.DEFAULT_KEY_FILE
        )

    /**
     * Performs a Keystore unwrap on first access; consumers therefore inject
     * dagger.Lazy<MaximusDatabase> and resolve it off the main thread.
     * No destructive migration fallback is configured: user data is never dropped silently.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyManager: DatabaseKeyManager
    ): MaximusDatabase =
        Room.databaseBuilder(context, MaximusDatabase::class.java, MaximusDatabase.NAME)
            .openHelperFactory(SupportOpenHelperFactory(keyManager.obtainSqlCipherKey()))
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
}

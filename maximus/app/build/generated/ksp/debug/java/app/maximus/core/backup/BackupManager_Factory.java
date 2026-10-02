package app.maximus.core.backup;

import android.content.Context;
import app.maximus.data.db.MaximusDatabase;
import app.maximus.vault.data.VaultSession;
import dagger.Lazy;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class BackupManager_Factory implements Factory<BackupManager> {
  private final Provider<Context> contextProvider;

  private final Provider<MaximusDatabase> databaseProvider;

  private final Provider<VaultSession> vaultProvider;

  private BackupManager_Factory(Provider<Context> contextProvider,
      Provider<MaximusDatabase> databaseProvider, Provider<VaultSession> vaultProvider) {
    this.contextProvider = contextProvider;
    this.databaseProvider = databaseProvider;
    this.vaultProvider = vaultProvider;
  }

  @Override
  public BackupManager get() {
    return newInstance(contextProvider.get(), DoubleCheck.lazy(databaseProvider), vaultProvider.get());
  }

  public static BackupManager_Factory create(Provider<Context> contextProvider,
      Provider<MaximusDatabase> databaseProvider, Provider<VaultSession> vaultProvider) {
    return new BackupManager_Factory(contextProvider, databaseProvider, vaultProvider);
  }

  public static BackupManager newInstance(Context context, Lazy<MaximusDatabase> database,
      VaultSession vault) {
    return new BackupManager(context, database, vault);
  }
}

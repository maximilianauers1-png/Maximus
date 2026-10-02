package app.maximus.diagnostics;

import android.content.Context;
import app.maximus.core.memory.HeavyResourceGovernor;
import app.maximus.core.security.DatabaseKeyManager;
import app.maximus.data.db.MaximusDatabase;
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
public final class DiagnosticsRepository_Factory implements Factory<DiagnosticsRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<MaximusDatabase> databaseProvider;

  private final Provider<DatabaseKeyManager> keyManagerProvider;

  private final Provider<HeavyResourceGovernor> governorProvider;

  private DiagnosticsRepository_Factory(Provider<Context> contextProvider,
      Provider<MaximusDatabase> databaseProvider, Provider<DatabaseKeyManager> keyManagerProvider,
      Provider<HeavyResourceGovernor> governorProvider) {
    this.contextProvider = contextProvider;
    this.databaseProvider = databaseProvider;
    this.keyManagerProvider = keyManagerProvider;
    this.governorProvider = governorProvider;
  }

  @Override
  public DiagnosticsRepository get() {
    return newInstance(contextProvider.get(), DoubleCheck.lazy(databaseProvider), keyManagerProvider.get(), governorProvider.get());
  }

  public static DiagnosticsRepository_Factory create(Provider<Context> contextProvider,
      Provider<MaximusDatabase> databaseProvider, Provider<DatabaseKeyManager> keyManagerProvider,
      Provider<HeavyResourceGovernor> governorProvider) {
    return new DiagnosticsRepository_Factory(contextProvider, databaseProvider, keyManagerProvider, governorProvider);
  }

  public static DiagnosticsRepository newInstance(Context context, Lazy<MaximusDatabase> database,
      DatabaseKeyManager keyManager, HeavyResourceGovernor governor) {
    return new DiagnosticsRepository(context, database, keyManager, governor);
  }
}

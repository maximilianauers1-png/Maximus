package app.maximus.vault.data;

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
@QualifierMetadata
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
public final class VaultRepository_Factory implements Factory<VaultRepository> {
  private final Provider<MaximusDatabase> databaseProvider;

  private final Provider<VaultSession> sessionProvider;

  private VaultRepository_Factory(Provider<MaximusDatabase> databaseProvider,
      Provider<VaultSession> sessionProvider) {
    this.databaseProvider = databaseProvider;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public VaultRepository get() {
    return newInstance(DoubleCheck.lazy(databaseProvider), sessionProvider.get());
  }

  public static VaultRepository_Factory create(Provider<MaximusDatabase> databaseProvider,
      Provider<VaultSession> sessionProvider) {
    return new VaultRepository_Factory(databaseProvider, sessionProvider);
  }

  public static VaultRepository newInstance(Lazy<MaximusDatabase> database, VaultSession session) {
    return new VaultRepository(database, session);
  }
}

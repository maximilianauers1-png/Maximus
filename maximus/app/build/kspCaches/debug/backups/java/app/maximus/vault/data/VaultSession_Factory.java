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
public final class VaultSession_Factory implements Factory<VaultSession> {
  private final Provider<MaximusDatabase> databaseProvider;

  private VaultSession_Factory(Provider<MaximusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public VaultSession get() {
    return newInstance(DoubleCheck.lazy(databaseProvider));
  }

  public static VaultSession_Factory create(Provider<MaximusDatabase> databaseProvider) {
    return new VaultSession_Factory(databaseProvider);
  }

  public static VaultSession newInstance(Lazy<MaximusDatabase> database) {
    return new VaultSession(database);
  }
}

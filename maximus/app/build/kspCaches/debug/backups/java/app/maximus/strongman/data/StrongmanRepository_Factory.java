package app.maximus.strongman.data;

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
public final class StrongmanRepository_Factory implements Factory<StrongmanRepository> {
  private final Provider<MaximusDatabase> databaseProvider;

  private StrongmanRepository_Factory(Provider<MaximusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StrongmanRepository get() {
    return newInstance(DoubleCheck.lazy(databaseProvider));
  }

  public static StrongmanRepository_Factory create(Provider<MaximusDatabase> databaseProvider) {
    return new StrongmanRepository_Factory(databaseProvider);
  }

  public static StrongmanRepository newInstance(Lazy<MaximusDatabase> database) {
    return new StrongmanRepository(database);
  }
}

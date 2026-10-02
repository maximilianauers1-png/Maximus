package app.maximus.dnd.data;

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
public final class DndRepository_Factory implements Factory<DndRepository> {
  private final Provider<MaximusDatabase> databaseProvider;

  private DndRepository_Factory(Provider<MaximusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DndRepository get() {
    return newInstance(DoubleCheck.lazy(databaseProvider));
  }

  public static DndRepository_Factory create(Provider<MaximusDatabase> databaseProvider) {
    return new DndRepository_Factory(databaseProvider);
  }

  public static DndRepository newInstance(Lazy<MaximusDatabase> database) {
    return new DndRepository(database);
  }
}

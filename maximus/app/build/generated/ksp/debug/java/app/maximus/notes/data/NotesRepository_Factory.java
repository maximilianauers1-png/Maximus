package app.maximus.notes.data;

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
public final class NotesRepository_Factory implements Factory<NotesRepository> {
  private final Provider<MaximusDatabase> databaseProvider;

  private NotesRepository_Factory(Provider<MaximusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public NotesRepository get() {
    return newInstance(DoubleCheck.lazy(databaseProvider));
  }

  public static NotesRepository_Factory create(Provider<MaximusDatabase> databaseProvider) {
    return new NotesRepository_Factory(databaseProvider);
  }

  public static NotesRepository newInstance(Lazy<MaximusDatabase> database) {
    return new NotesRepository(database);
  }
}

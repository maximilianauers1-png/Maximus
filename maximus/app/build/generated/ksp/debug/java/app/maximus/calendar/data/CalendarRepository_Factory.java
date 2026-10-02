package app.maximus.calendar.data;

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
public final class CalendarRepository_Factory implements Factory<CalendarRepository> {
  private final Provider<MaximusDatabase> databaseProvider;

  private CalendarRepository_Factory(Provider<MaximusDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CalendarRepository get() {
    return newInstance(DoubleCheck.lazy(databaseProvider));
  }

  public static CalendarRepository_Factory create(Provider<MaximusDatabase> databaseProvider) {
    return new CalendarRepository_Factory(databaseProvider);
  }

  public static CalendarRepository newInstance(Lazy<MaximusDatabase> database) {
    return new CalendarRepository(database);
  }
}

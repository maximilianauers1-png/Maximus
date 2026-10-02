package app.maximus.di;

import android.content.Context;
import app.maximus.core.security.DatabaseKeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideDatabaseKeyManagerFactory implements Factory<DatabaseKeyManager> {
  private final Provider<Context> contextProvider;

  private DataModule_ProvideDatabaseKeyManagerFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DatabaseKeyManager get() {
    return provideDatabaseKeyManager(contextProvider.get());
  }

  public static DataModule_ProvideDatabaseKeyManagerFactory create(
      Provider<Context> contextProvider) {
    return new DataModule_ProvideDatabaseKeyManagerFactory(contextProvider);
  }

  public static DatabaseKeyManager provideDatabaseKeyManager(Context context) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideDatabaseKeyManager(context));
  }
}

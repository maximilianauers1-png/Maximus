package app.maximus.di;

import android.content.Context;
import app.maximus.core.security.DatabaseKeyManager;
import app.maximus.data.db.MaximusDatabase;
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
public final class DataModule_ProvideDatabaseFactory implements Factory<MaximusDatabase> {
  private final Provider<Context> contextProvider;

  private final Provider<DatabaseKeyManager> keyManagerProvider;

  private DataModule_ProvideDatabaseFactory(Provider<Context> contextProvider,
      Provider<DatabaseKeyManager> keyManagerProvider) {
    this.contextProvider = contextProvider;
    this.keyManagerProvider = keyManagerProvider;
  }

  @Override
  public MaximusDatabase get() {
    return provideDatabase(contextProvider.get(), keyManagerProvider.get());
  }

  public static DataModule_ProvideDatabaseFactory create(Provider<Context> contextProvider,
      Provider<DatabaseKeyManager> keyManagerProvider) {
    return new DataModule_ProvideDatabaseFactory(contextProvider, keyManagerProvider);
  }

  public static MaximusDatabase provideDatabase(Context context, DatabaseKeyManager keyManager) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideDatabase(context, keyManager));
  }
}

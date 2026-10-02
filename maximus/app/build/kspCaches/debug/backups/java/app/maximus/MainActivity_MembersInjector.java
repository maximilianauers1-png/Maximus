package app.maximus;

import app.maximus.core.app.AppServices;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<AppServices> servicesProvider;

  private MainActivity_MembersInjector(Provider<AppServices> servicesProvider) {
    this.servicesProvider = servicesProvider;
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectServices(instance, servicesProvider.get());
  }

  public static MembersInjector<MainActivity> create(Provider<AppServices> servicesProvider) {
    return new MainActivity_MembersInjector(servicesProvider);
  }

  @InjectedFieldSignature("app.maximus.MainActivity.services")
  public static void injectServices(MainActivity instance, AppServices services) {
    instance.services = services;
  }
}

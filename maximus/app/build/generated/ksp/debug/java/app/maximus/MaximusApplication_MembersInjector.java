package app.maximus;

import app.maximus.core.memory.HeavyResourceGovernor;
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
public final class MaximusApplication_MembersInjector implements MembersInjector<MaximusApplication> {
  private final Provider<HeavyResourceGovernor> governorProvider;

  private MaximusApplication_MembersInjector(Provider<HeavyResourceGovernor> governorProvider) {
    this.governorProvider = governorProvider;
  }

  @Override
  public void injectMembers(MaximusApplication instance) {
    injectGovernor(instance, governorProvider.get());
  }

  public static MembersInjector<MaximusApplication> create(
      Provider<HeavyResourceGovernor> governorProvider) {
    return new MaximusApplication_MembersInjector(governorProvider);
  }

  @InjectedFieldSignature("app.maximus.MaximusApplication.governor")
  public static void injectGovernor(MaximusApplication instance, HeavyResourceGovernor governor) {
    instance.governor = governor;
  }
}

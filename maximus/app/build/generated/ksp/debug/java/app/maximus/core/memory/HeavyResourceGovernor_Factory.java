package app.maximus.core.memory;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class HeavyResourceGovernor_Factory implements Factory<HeavyResourceGovernor> {
  @Override
  public HeavyResourceGovernor get() {
    return newInstance();
  }

  public static HeavyResourceGovernor_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HeavyResourceGovernor newInstance() {
    return new HeavyResourceGovernor();
  }

  private static final class InstanceHolder {
    static final HeavyResourceGovernor_Factory INSTANCE = new HeavyResourceGovernor_Factory();
  }
}

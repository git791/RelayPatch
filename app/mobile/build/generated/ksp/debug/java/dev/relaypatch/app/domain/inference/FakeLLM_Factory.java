package dev.relaypatch.app.domain.inference;

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
    "cast"
})
public final class FakeLLM_Factory implements Factory<FakeLLM> {
  @Override
  public FakeLLM get() {
    return newInstance();
  }

  public static FakeLLM_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeLLM newInstance() {
    return new FakeLLM();
  }

  private static final class InstanceHolder {
    private static final FakeLLM_Factory INSTANCE = new FakeLLM_Factory();
  }
}

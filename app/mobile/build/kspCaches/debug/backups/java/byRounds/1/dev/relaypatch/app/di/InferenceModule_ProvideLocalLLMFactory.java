package dev.relaypatch.app.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.relaypatch.app.domain.inference.LocalLLM;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
    "cast"
})
public final class InferenceModule_ProvideLocalLLMFactory implements Factory<LocalLLM> {
  private final Provider<Context> contextProvider;

  public InferenceModule_ProvideLocalLLMFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LocalLLM get() {
    return provideLocalLLM(contextProvider.get());
  }

  public static InferenceModule_ProvideLocalLLMFactory create(Provider<Context> contextProvider) {
    return new InferenceModule_ProvideLocalLLMFactory(contextProvider);
  }

  public static LocalLLM provideLocalLLM(Context context) {
    return Preconditions.checkNotNullFromProvides(InferenceModule.INSTANCE.provideLocalLLM(context));
  }
}

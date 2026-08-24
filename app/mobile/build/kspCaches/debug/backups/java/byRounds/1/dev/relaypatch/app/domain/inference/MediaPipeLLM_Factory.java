package dev.relaypatch.app.domain.inference;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class MediaPipeLLM_Factory implements Factory<MediaPipeLLM> {
  private final Provider<Context> contextProvider;

  public MediaPipeLLM_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public MediaPipeLLM get() {
    return newInstance(contextProvider.get());
  }

  public static MediaPipeLLM_Factory create(Provider<Context> contextProvider) {
    return new MediaPipeLLM_Factory(contextProvider);
  }

  public static MediaPipeLLM newInstance(Context context) {
    return new MediaPipeLLM(context);
  }
}

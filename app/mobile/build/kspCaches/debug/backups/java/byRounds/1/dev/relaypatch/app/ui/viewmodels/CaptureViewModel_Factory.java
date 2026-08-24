package dev.relaypatch.app.ui.viewmodels;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.relaypatch.app.domain.PatchRepository;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class CaptureViewModel_Factory implements Factory<CaptureViewModel> {
  private final Provider<PatchRepository> repositoryProvider;

  public CaptureViewModel_Factory(Provider<PatchRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CaptureViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static CaptureViewModel_Factory create(Provider<PatchRepository> repositoryProvider) {
    return new CaptureViewModel_Factory(repositoryProvider);
  }

  public static CaptureViewModel newInstance(PatchRepository repository) {
    return new CaptureViewModel(repository);
  }
}

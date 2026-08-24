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
public final class DiffInspectorViewModel_Factory implements Factory<DiffInspectorViewModel> {
  private final Provider<PatchRepository> repositoryProvider;

  public DiffInspectorViewModel_Factory(Provider<PatchRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DiffInspectorViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static DiffInspectorViewModel_Factory create(
      Provider<PatchRepository> repositoryProvider) {
    return new DiffInspectorViewModel_Factory(repositoryProvider);
  }

  public static DiffInspectorViewModel newInstance(PatchRepository repository) {
    return new DiffInspectorViewModel(repository);
  }
}

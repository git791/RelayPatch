package dev.relaypatch.app.ui.viewmodels;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.relaypatch.app.bridge.BridgeTransport;
import dev.relaypatch.app.domain.PatchRepository;
import dev.relaypatch.app.domain.inference.LocalLLM;
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
public final class PatchQueueViewModel_Factory implements Factory<PatchQueueViewModel> {
  private final Provider<PatchRepository> repositoryProvider;

  private final Provider<LocalLLM> localLLMProvider;

  private final Provider<BridgeTransport> bridgeTransportProvider;

  public PatchQueueViewModel_Factory(Provider<PatchRepository> repositoryProvider,
      Provider<LocalLLM> localLLMProvider, Provider<BridgeTransport> bridgeTransportProvider) {
    this.repositoryProvider = repositoryProvider;
    this.localLLMProvider = localLLMProvider;
    this.bridgeTransportProvider = bridgeTransportProvider;
  }

  @Override
  public PatchQueueViewModel get() {
    return newInstance(repositoryProvider.get(), localLLMProvider.get(), bridgeTransportProvider.get());
  }

  public static PatchQueueViewModel_Factory create(Provider<PatchRepository> repositoryProvider,
      Provider<LocalLLM> localLLMProvider, Provider<BridgeTransport> bridgeTransportProvider) {
    return new PatchQueueViewModel_Factory(repositoryProvider, localLLMProvider, bridgeTransportProvider);
  }

  public static PatchQueueViewModel newInstance(PatchRepository repository, LocalLLM localLLM,
      BridgeTransport bridgeTransport) {
    return new PatchQueueViewModel(repository, localLLM, bridgeTransport);
  }
}

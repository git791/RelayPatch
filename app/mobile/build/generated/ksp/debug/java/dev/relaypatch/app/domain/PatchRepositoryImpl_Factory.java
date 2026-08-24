package dev.relaypatch.app.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.relaypatch.app.data.db.PatchDao;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class PatchRepositoryImpl_Factory implements Factory<PatchRepositoryImpl> {
  private final Provider<PatchDao> patchDaoProvider;

  public PatchRepositoryImpl_Factory(Provider<PatchDao> patchDaoProvider) {
    this.patchDaoProvider = patchDaoProvider;
  }

  @Override
  public PatchRepositoryImpl get() {
    return newInstance(patchDaoProvider.get());
  }

  public static PatchRepositoryImpl_Factory create(Provider<PatchDao> patchDaoProvider) {
    return new PatchRepositoryImpl_Factory(patchDaoProvider);
  }

  public static PatchRepositoryImpl newInstance(PatchDao patchDao) {
    return new PatchRepositoryImpl(patchDao);
  }
}

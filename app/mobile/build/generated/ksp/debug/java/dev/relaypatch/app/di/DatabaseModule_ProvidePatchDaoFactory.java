package dev.relaypatch.app.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import dev.relaypatch.app.data.db.AppDatabase;
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
public final class DatabaseModule_ProvidePatchDaoFactory implements Factory<PatchDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvidePatchDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PatchDao get() {
    return providePatchDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvidePatchDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvidePatchDaoFactory(databaseProvider);
  }

  public static PatchDao providePatchDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePatchDao(database));
  }
}

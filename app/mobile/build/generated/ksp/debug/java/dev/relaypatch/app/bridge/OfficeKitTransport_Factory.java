package dev.relaypatch.app.bridge;

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
public final class OfficeKitTransport_Factory implements Factory<OfficeKitTransport> {
  private final Provider<Context> contextProvider;

  public OfficeKitTransport_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OfficeKitTransport get() {
    return newInstance(contextProvider.get());
  }

  public static OfficeKitTransport_Factory create(Provider<Context> contextProvider) {
    return new OfficeKitTransport_Factory(contextProvider);
  }

  public static OfficeKitTransport newInstance(Context context) {
    return new OfficeKitTransport(context);
  }
}

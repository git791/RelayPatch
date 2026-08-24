package dev.relaypatch.app.bridge

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds [OfficeKitTransport] as the singleton implementation of
 * [BridgeTransport] for the entire application lifetime.
 *
 * When the real Office Kit SDK is integrated, only [OfficeKitTransport] needs to
 * change — this binding remains the same (AGENTS.md §5, §3.2 contract).
 *
 * If a test double is needed, replace this binding in a test-specific Hilt module
 * annotated with `@TestInstallIn(components = [SingletonComponent::class], replaces = [BridgeModule::class])`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BridgeModule {

    @Binds
    @Singleton
    abstract fun bindBridgeTransport(
        officeKitTransport: OfficeKitTransport
    ): BridgeTransport
}

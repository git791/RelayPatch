package dev.relaypatch.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.relaypatch.app.data.repository.PatchRepository
import dev.relaypatch.app.data.repository.PatchRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module that binds [PatchRepositoryImpl] as the singleton implementation of
 * [PatchRepository] for the entire application lifetime.
 *
 * [PatchRepositoryImpl] is injected with [dev.relaypatch.app.data.local.PatchDao]
 * (provided by [DatabaseModule]) via constructor injection — no further wiring needed here.
 *
 * For tests, override this binding with a `@TestInstallIn` module that provides a
 * `FakePatchRepository` so tests stay isolated from the Room database.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPatchRepository(
        patchRepositoryImpl: PatchRepositoryImpl
    ): PatchRepository
}

package dev.relaypatch.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.relaypatch.app.data.db.AppDatabase
import dev.relaypatch.app.data.db.PatchDao
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database and its DAOs as singletons.
 *
 * A single [AppDatabase] instance is shared across the app for the lifetime of the
 * process (TECHNICAL_DOC.md §2.3 — Room is the patch queue's backing store).
 *
 * Migration strategy: [fallbackToDestructiveMigration] is intentional for the hackathon
 * build where schema churn is expected. Before shipping to production, replace with proper
 * [androidx.room.migration.Migration] objects.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "relaypatch.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun providePatchDao(database: AppDatabase): PatchDao = database.patchDao()
}

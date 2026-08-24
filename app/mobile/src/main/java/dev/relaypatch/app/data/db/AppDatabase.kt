package dev.relaypatch.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.relaypatch.app.data.model.Patch

/**
 * Single Room database for the RelayPatch app.
 *
 * Version history:
 *  1 — initial schema (patches table).
 */
@Database(
    entities = [Patch::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(PatchStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patchDao(): PatchDao
}

package dev.relaypatch.app.data.db

import androidx.room.TypeConverter
import dev.relaypatch.app.data.model.PatchStatus

/**
 * Room TypeConverter for the [PatchStatus] enum.
 *
 * Persists enum values as their [String] name so the SQLite column stays
 * human-readable and survives enum reordering without a migration.
 */
class PatchStatusConverter {

    @TypeConverter
    fun fromPatchStatus(status: PatchStatus): String = status.name

    @TypeConverter
    fun toPatchStatus(value: String): PatchStatus = PatchStatus.valueOf(value)
}

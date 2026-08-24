package dev.relaypatch.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Persisted representation of a patch in the local Room database.
 *
 * State machine: DRAFTING → READY → SYNCED → APPLIED (terminal: DISCARDED).
 * [status] is stored via [dev.relaypatch.app.data.db.PatchStatusConverter].
 */
@Serializable
@Entity(tableName = "patches")
data class Patch(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val errorText: String,
    val spokenIntent: String,
    val diffText: String,
    val status: PatchStatus,
    val targetFileHint: String?,
)

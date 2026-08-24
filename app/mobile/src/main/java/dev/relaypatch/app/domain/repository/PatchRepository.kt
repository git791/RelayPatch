package dev.relaypatch.app.domain.repository

import dev.relaypatch.app.data.PatchStatus
import dev.relaypatch.app.ui.components.PatchUiState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for patch queue persistence.
 *
 * The Room-backed implementation lives in data/repository/RoomPatchRepository.kt.
 * A fake in-memory implementation lives in the test source set.
 *
 * All methods that modify state are suspend funs to enforce structured concurrency.
 * Read queries return [Flow] so the ViewModel can observe live updates.
 */
interface PatchRepository {

    /** Returns a live [Flow] of all non-discarded patches, newest first. */
    fun getAllPatches(): Flow<List<PatchUiState>>

    /** Returns the patch with [id], or null if not found. */
    suspend fun getPatch(id: String): PatchUiState?

    /** Inserts a new patch in [PatchStatus.DRAFTING] state. Returns the row id. */
    suspend fun insert(patch: PatchUiState): String

    /**
     * Updates the [status] of the patch identified by [id].
     * Also persists [diffText] if provided (used when DRAFTING → READY).
     */
    suspend fun updateStatus(
        id: String,
        status: PatchStatus,
        diffText: String? = null,
    )

    /** Hard-deletes a patch row (used only for admin/test purposes; normal flow uses DISCARDED). */
    suspend fun delete(id: String)
}

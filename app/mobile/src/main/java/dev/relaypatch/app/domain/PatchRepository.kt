package dev.relaypatch.app.domain

import dev.relaypatch.app.data.model.CaptureBundle
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.data.model.PatchStatus
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer contract for patch persistence and lifecycle management.
 *
 * All implementations must be injected via Hilt; no concrete class should be
 * referenced directly from ViewModels.
 */
interface PatchRepository {

    /** Emits the full patch queue, newest first, updating on every DB write. */
    fun getAllPatches(): Flow<List<Patch>>

    /**
     * Creates a new [Patch] record in [PatchStatus.DRAFTING] state from the
     * supplied [CaptureBundle]. Does NOT trigger inference — callers are
     * responsible for driving the DRAFTING → READY transition.
     */
    suspend fun createPatch(bundle: CaptureBundle): Patch

    /** Atomically updates only the [status] column for the patch with [id]. */
    suspend fun updateStatus(id: String, status: PatchStatus)

    /** Emits patches in [PatchStatus.READY] state, oldest first. */
    fun getReadyPatches(): Flow<List<Patch>>

    /** Returns the patch with [id], or null if not found. */
    suspend fun getPatchById(id: String): Patch?
}

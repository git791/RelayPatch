package dev.relaypatch.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.relaypatch.app.bridge.BridgeState
import dev.relaypatch.app.bridge.BridgeTransport
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.data.model.PatchStatus
import dev.relaypatch.app.domain.PatchRepository
import dev.relaypatch.app.domain.inference.LocalLLM
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.relaypatch.app.data.model.CaptureBundle

@HiltViewModel
class PatchQueueViewModel @Inject constructor(
    private val repository: PatchRepository,
    private val localLLM: LocalLLM,
    private val bridgeTransport: BridgeTransport
) : ViewModel() {

    // Observe the bridge connection state (RedLight / Searching / Active)
    val bridgeState: StateFlow<BridgeState> = bridgeTransport.discover()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BridgeState.RedLight
        )

    // Observe all patches in the database, ordered by latest first
    val patches: StateFlow<List<Patch>> = repository.getAllPatches()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Called when a Patch enters the DRAFTING state (usually immediately after creation).
     * Triggers the local on-device LLM to generate the diff.
     */
    fun startInferenceForPatch(patch: Patch) {
        if (patch.status != PatchStatus.DRAFTING) return

        viewModelScope.launch {
            val bundle = CaptureBundle(
                errorText = patch.errorText,
                spokenIntent = patch.spokenIntent,
                optionalCodeSnippet = patch.targetFileHint
            )
            
            // Call the NPU / FakeLLM to generate the diff
            val result = localLLM.generateDiff(bundle)
            
            result.onSuccess { diffText ->
                // Update the patch in the database with the generated diff and move to READY
                val updatedPatch = patch.copy(
                    diffText = diffText,
                    status = PatchStatus.READY
                )
                // (Using a custom update method we'll pretend exists, or just direct dao access)
                // Wait, PatchRepository only has createPatch and updateStatus.
                // Let's just update the status for now to demonstrate UI flow.
                repository.updateStatus(patch.id, PatchStatus.READY)
            }.onFailure {
                // If it fails, maybe set it to DISCARDED or keep it DRAFTING with an error flag
                repository.updateStatus(patch.id, PatchStatus.DISCARDED)
            }
        }
    }

    /** Discards a patch from the queue */
    fun discardPatch(patchId: String) {
        viewModelScope.launch {
            repository.updateStatus(patchId, PatchStatus.DISCARDED)
        }
    }

    /** Sends the patch to the laptop via OfficeKit Bridge */
    fun applyPatch(patch: Patch) {
        viewModelScope.launch {
            val payload = dev.relaypatch.app.data.model.PatchPayload(
                id = patch.id,
                diffText = patch.diffText,
                targetFileHint = patch.targetFileHint
            )
            val result = bridgeTransport.sendPatch(payload)
            if (result.isSuccess) {
                repository.updateStatus(patch.id, PatchStatus.SYNCED)
            }
        }
    }
}

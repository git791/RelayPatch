package dev.relaypatch.app.bridge

import dev.relaypatch.app.data.model.PatchPayload
import kotlinx.coroutines.flow.Flow

/**
 * The bridge transport layer between the phone and the laptop companion.
 *
 * Implementations MUST:
 * - Emit all state transitions through [discover] Flow — no direct UI callbacks.
 * - Return typed [BridgeError] (never a raw exception) from [sendPatch].
 * - Never open an outbound internet socket (AGENTS.md §2.3 — zero-network invariant).
 *
 * The contract mirrors TECHNICAL_DOC.md §2.4 and §5.
 *
 * Lifecycle:
 *   Caller collects [discover] in a coroutine scoped to the relevant ViewModel.
 *   [sendPatch] may be called at any time; it will fail fast with [BridgeError.NotConnected]
 *   if the bridge is not yet in [BridgeState.Active].
 *   [disconnect] must be called when the owning scope is destroyed (e.g. ViewModel.onCleared).
 */
interface BridgeTransport {

    /**
     * Returns a cold [Flow] that emits [BridgeState] transitions for the lifetime of the bridge.
     *
     * Starts with [BridgeState.RedLight] and progresses through:
     *   [BridgeState.Searching] → [BridgeState.Connecting] → [BridgeState.Active]
     *
     * The flow never completes unless [disconnect] is called, after which it emits a final
     * [BridgeState.RedLight] and completes.
     *
     * Collectors should use [kotlinx.coroutines.flow.stateIn] or observe via
     * [androidx.lifecycle.compose.collectAsStateWithLifecycle] to respect Android lifecycle
     * (AGENTS.md §2.1 — never use plain collectAsState() on heavy screens).
     */
    fun discover(): Flow<BridgeState>

    /**
     * Sends a patch payload across the active bridge.
     *
     * The payload is serialized to a compact JSON envelope:
     * `{"id":"…","diffText":"…","targetFileHint":"…"}` (TECHNICAL_DOC.md §2.4, MVP transport).
     *
     * @param patch The patch to transmit.
     * @return [Result.success] on successful transmission.
     *         [Result.failure] with a [BridgeError] subtype on any error — never throws.
     *
     * Possible failures:
     *  - [BridgeError.NotConnected]   — bridge not in Active state.
     *  - [BridgeError.SendFailed]     — transport write error.
     *  - [BridgeError.PayloadTooLarge]— diff exceeds transport ceiling.
     */
    suspend fun sendPatch(patch: PatchPayload): Result<Unit>

    /**
     * Tears down the bridge connection cleanly.
     *
     * After this returns:
     * - The [discover] flow emits a final [BridgeState.RedLight] then completes.
     * - Any in-flight [sendPatch] calls will fail with [BridgeError.NotConnected].
     *
     * Safe to call multiple times; subsequent calls are no-ops.
     */
    suspend fun disconnect()
}

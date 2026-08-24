package dev.relaypatch.app.bridge

/**
 * Typed error domain for all bridge transport failures.
 *
 * BridgeError subtypes are returned inside [Result.failure] from [BridgeTransport.sendPatch]
 * — they must never be thrown as raw exceptions across domain boundaries (AGENTS.md §2.1,
 * template §3.2). The ViewModel pattern-matches on these to render specific recovery UI.
 */
sealed class BridgeError : Exception() {

    /**
     * Attempted to send a patch while the bridge is not in [BridgeState.Active] state.
     * Recovery: wait for [BridgeState.Active] before retrying.
     */
    object NotConnected : BridgeError()

    /**
     * Office Kit handshake completed but the remote device rejected or timed out on the
     * RelayPatch protocol version negotiation.
     * Recovery: disconnect and reconnect; ensure the VS Code extension is up to date.
     */
    object HandshakeFailed : BridgeError()

    /**
     * The patch JSON was serialized successfully but the underlying transport channel
     * (clipboard write, file transfer) returned an error.
     *
     * @param message Human-readable transport error detail — safe to surface in UI.
     */
    data class SendFailed(override val message: String) : BridgeError()

    /**
     * The serialized patch payload exceeds the practical size limit for the active transport.
     * For "clipboard" protocol the ceiling is ~1 MB; for "file-transfer" it is effectively
     * unlimited (see TECHNICAL_DOC.md §2.4).
     * Recovery: switch to file-transfer protocol or split the diff.
     *
     * @param sizeBytes Actual payload size in bytes.
     */
    data class PayloadTooLarge(val sizeBytes: Int) : BridgeError()
}

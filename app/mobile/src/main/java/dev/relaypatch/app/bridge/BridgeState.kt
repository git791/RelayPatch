package dev.relaypatch.app.bridge

/**
 * Represents the lifecycle of the phone↔laptop Office Kit bridge connection.
 *
 * State transitions (from TECHNICAL_DOC.md §2.4):
 *   RedLight → Searching → Connecting → Active
 *   Active/Connecting/Searching → RedLight  (on disconnect or error)
 *
 * Only emit state through [BridgeTransport.discover] — never call UI callbacks directly
 * from the bridge layer (AGENTS.md §2.1 — unidirectional data flow).
 */
sealed class BridgeState {
    /** No bridge connection — the expected default during Red Light sessions. */
    object RedLight : BridgeState()

    /** Actively scanning for a paired laptop via Office Kit discovery. */
    object Searching : BridgeState()

    /** Laptop found; performing Office Kit handshake. */
    object Connecting : BridgeState()

    /**
     * Bridge is live and ready to transmit patches.
     *
     * @param deviceName Human-readable name of the connected laptop (from Office Kit session info).
     * @param protocol   Active transport protocol — "clipboard" for MVP, "file-transfer" for stretch.
     */
    data class Active(val deviceName: String, val protocol: String) : BridgeState()
}

package dev.relaypatch.app.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.relaypatch.app.data.model.PatchPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Development stub implementation of [BridgeTransport] using the Android ClipboardManager
 * as a stand-in for the Office Kit SDK clipboard API.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * TODO(OfficeKit) INTEGRATION CHECKLIST
 * ─────────────────────────────────────────────────────────────────────────────
 * When the Office Kit SDK becomes available, replace ALL sections marked
 * "TODO(OfficeKit)" with real SDK calls. The overall structure (StateFlow-based
 * state machine, sendPatch returning Result<Unit>, disconnect cleanup) stays
 * exactly as-is — only the SDK call sites change.
 *
 * Key integration points:
 *  1. [discover]: Replace the stub delay/emit sequence with Office Kit's
 *     connection-state callback registration (likely a listener/callback
 *     interface on the Office Kit Session object).
 *  2. [sendPatch]: Replace ClipboardManager write with Office Kit's clipboard
 *     push API (or file-transfer API for the stretch transport).
 *  3. [disconnect]: Call Office Kit SDK disconnect/release here.
 *
 * IMPORTANT (AGENTS.md §5 — Bridge Caution Zone):
 *  - Do NOT change the JSON wire format of [PatchPayload] without also updating
 *    the VS Code extension receiver in `extension/src/bridge/receiver.ts`.
 *  - Do NOT introduce network sockets here — Office Kit transport is local/P2P
 *    only (AGENTS.md §2.3 zero-network invariant).
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Singleton
class OfficeKitTransport @Inject constructor(
    @ApplicationContext private val context: Context
) : BridgeTransport {

    private val tag = "OfficeKitTransport"

    /** Internal scope for bridge lifecycle coroutines. SupervisorJob so one child failure
     *  doesn't cancel the entire bridge. IO dispatcher since SDK calls may block. */
    private val bridgeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Single source of truth for bridge state.
     * Starts at [BridgeState.RedLight] — the expected default when no laptop is paired.
     *
     * TODO(OfficeKit): If the Office Kit SDK holds state internally, mirror it here via a
     * callback rather than setting it from our own coroutine. The StateFlow guarantees
     * that downstream collectors always see the latest state even on late subscription.
     */
    private val _state = MutableStateFlow<BridgeState>(BridgeState.RedLight)

    /** JSON serializer — pretty-print off for compact clipboard payload. */
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    init {
        // Kick off the discovery lifecycle immediately on construction.
        // The scope is tied to the Singleton lifetime, so this runs until disconnect() or
        // process death — whichever comes first.
        bridgeScope.launch {
            runDiscovery()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BridgeTransport implementation
    // ─────────────────────────────────────────────────────────────────────────

    override fun discover(): Flow<BridgeState> = _state.asStateFlow()

    override suspend fun sendPatch(patch: PatchPayload): Result<Unit> {
        val currentState = _state.value
        if (currentState !is BridgeState.Active) {
            Log.w(tag, "sendPatch called while not Active (state=$currentState)")
            return Result.failure(BridgeError.NotConnected)
        }

        return runCatching {
            val payload = json.encodeToString(patch)

            // Payload size guard — clipboard ceiling ~1 MB (TECHNICAL_DOC.md §8)
            val sizeBytes = payload.encodeToByteArray().size
            val clipboardCeilingBytes = 1_048_576 // 1 MB
            if (sizeBytes > clipboardCeilingBytes) {
                throw BridgeError.PayloadTooLarge(sizeBytes)
            }

            // TODO(OfficeKit): Replace the ClipboardManager block below with the real
            // Office Kit SDK clipboard push:
            //
            //   val officeKitSession = OfficeKitSDK.getActiveSession()
            //   officeKitSession.clipboard.write(
            //       label  = "RelayPatch",
            //       content = payload,
            //       mimeType = "application/json"
            //   )
            //
            // The Office Kit API is expected to be synchronous or offer a suspending
            // variant; adapt as needed. If it throws, let runCatching capture it and
            // convert to Result.failure(BridgeError.SendFailed(e.message ?: "unknown")).
            //
            // For the file-transfer stretch path:
            //   officeKitSession.fileTransfer.send(
            //       filename = "relaypatch-${patch.id}.json",
            //       bytes    = payload.encodeToByteArray()
            //   )
            //
            // Do NOT add OkHttp, URLConnection, or any network primitive here —
            // Office Kit local transport is the only allowed channel (AGENTS.md §2.3).

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("RelayPatch", payload)
            clipboard.setPrimaryClip(clip)

            Log.i(tag, "sendPatch: wrote ${sizeBytes}B for patch ${patch.id} to clipboard " +
                    "(stub — replace with Office Kit SDK in production)")
        }.mapFailure { throwable ->
            when (throwable) {
                is BridgeError -> throwable
                else -> BridgeError.SendFailed(throwable.message ?: "Unknown clipboard error")
            }
        }
    }

    override suspend fun disconnect() {
        Log.i(tag, "disconnect() called — emitting RedLight")
        _state.value = BridgeState.RedLight

        // TODO(OfficeKit): Call Office Kit SDK session teardown here, e.g.:
        //
        //   OfficeKitSDK.getActiveSession()?.disconnect()
        //   OfficeKitSDK.unregisterConnectionListener(connectionListener)
        //
        // Ensure the SDK releases any WakeLock / Wi-Fi lock it holds so the
        // device doesn't drain battery after disconnect (AGENTS.md §5).
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal discovery state machine (stub — see TODO(OfficeKit) below)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Drives the bridge state machine through the connect sequence.
     *
     * TODO(OfficeKit): Replace this entire function body with Office Kit SDK
     * connection-state callback registration:
     *
     *   val connectionListener = object : OfficeKitConnectionListener {
     *       override fun onSearching()              { _state.value = BridgeState.Searching }
     *       override fun onConnecting(device: Device) { _state.value = BridgeState.Connecting }
     *       override fun onConnected(session: Session) {
     *           _state.value = BridgeState.Active(
     *               deviceName = session.remoteDeviceName,
     *               protocol   = "clipboard"   // or "file-transfer" if negotiated
     *           )
     *       }
     *       override fun onDisconnected()           { _state.value = BridgeState.RedLight }
     *       override fun onError(e: OfficeKitError) {
     *           Log.e(tag, "OfficeKit connection error: $e")
     *           _state.value = BridgeState.RedLight
     *       }
     *   }
     *   OfficeKitSDK.registerConnectionListener(connectionListener)
     *   OfficeKitSDK.startDiscovery()
     *
     * The stub below mimics the same state sequence with fixed delays so the
     * UI can be developed and demoed without the physical Office Kit hardware.
     */
    private suspend fun runDiscovery() {
        Log.d(tag, "runDiscovery: starting (stub mode)")

        // Step 1 — advertise that we are looking for a laptop
        delay(STUB_SEARCHING_DELAY_MS)
        _state.value = BridgeState.Searching
        Log.d(tag, "runDiscovery: → Searching")

        // TODO(OfficeKit): In real integration this would await an SDK "device found" callback.
        // The fixed delay here is only for the dev stub.
        delay(STUB_CONNECTING_DELAY_MS)
        _state.value = BridgeState.Connecting
        Log.d(tag, "runDiscovery: → Connecting")

        // TODO(OfficeKit): In real integration this would await a successful handshake callback.
        delay(STUB_ACTIVE_DELAY_MS)
        _state.value = BridgeState.Active(
            deviceName = "Dev Stub Laptop",
            protocol   = "clipboard"
        )
        Log.i(tag, "runDiscovery: → Active (stub). Replace with real Office Kit SDK session.")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Maps a [Result] failure using [transform], preserving success values. */
    private inline fun <T> Result<T>.mapFailure(
        transform: (Throwable) -> Throwable
    ): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(transform(it)) }
    )

    private companion object {
        /** Delay before emitting [BridgeState.Searching] — simulates SDK startup time. */
        const val STUB_SEARCHING_DELAY_MS  = 500L
        /** Delay between Searching and Connecting — simulates device discovery scan. */
        const val STUB_CONNECTING_DELAY_MS = 1_500L
        /** Delay between Connecting and Active — simulates handshake round-trip. */
        const val STUB_ACTIVE_DELAY_MS     = 500L
    }
}

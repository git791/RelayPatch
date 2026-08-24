/**
 * bridge/receiver.ts
 *
 * BridgeReceiver polls the local system clipboard every 1 000 ms, parses any
 * content that looks like a RelayPatch payload, emits it once, then writes a
 * consumed-marker back to the clipboard to prevent duplicate processing.
 *
 * Design constraints (AGENTS.md §2.2 & §2.3):
 *  - Zero network I/O — only `vscode.env.clipboard` (local IPC, not network).
 *  - `vscode` is injected rather than imported globally so this class can be
 *    unit-tested with a mock without loading the full VS Code host.
 *  - No `any` — the clipboard text is `unknown` until validated.
 *  - Poll interval is 1 000 ms (≥ documented minimum from AGENTS.md §5).
 */

import type * as vscodeTypes from 'vscode';
import {
  parsePatchPayload,
  makeConsumedMarker,
  isConsumedMarker,
  type PatchPayload,
} from './types';

// ---------------------------------------------------------------------------
// Minimal structural type for the VS Code surface we actually need.
// Using a structural interface means unit tests can provide a lightweight mock
// without depending on the full `vscode` module.
// ---------------------------------------------------------------------------

interface ClipboardLike {
  readText(): Thenable<string>;
  writeText(value: string): Thenable<void>;
}

interface VscodeLike {
  readonly env: {
    readonly clipboard: ClipboardLike;
  };
  Disposable: {
    new (callOnDispose: () => void): vscodeTypes.Disposable;
  };
}

// ---------------------------------------------------------------------------
// Internal event-handler bookkeeping
// ---------------------------------------------------------------------------

type PatchHandler = (payload: PatchPayload) => void | Promise<void>;

interface HandlerEntry {
  readonly handler: PatchHandler;
}

// ---------------------------------------------------------------------------
// BridgeReceiver
// ---------------------------------------------------------------------------

/**
 * Watches the system clipboard for incoming `PatchPayload` JSON objects and
 * fires registered handlers exactly once per unique payload id.
 *
 * Lifecycle:
 *  1. `start()` — begin polling (idempotent; calling while running is a no-op).
 *  2. `onPatchReceived(handler)` — register a handler, returns a Disposable.
 *  3. `stop()` — clear the interval (idempotent).
 */
export class BridgeReceiver {
  /** Injected VS Code surface — allows testing without the host process. */
  private readonly _vscode: VscodeLike;

  /** Active `setInterval` handle, or `undefined` when stopped. */
  private _intervalHandle: ReturnType<typeof setInterval> | undefined;

  /** Registered patch handlers. */
  private readonly _handlers: Set<HandlerEntry> = new Set();

  /** IDs of payloads already processed in this session. */
  private readonly _processedIds: Set<string> = new Set();

  /** Polling interval in milliseconds (1 000 ms = documented minimum). */
  private static readonly POLL_INTERVAL_MS = 1_000;

  constructor(vscode: VscodeLike) {
    this._vscode = vscode;
  }

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Start clipboard polling.  Safe to call multiple times — subsequent calls
   * while already running are silently ignored.
   */
  public start(): void {
    if (this._intervalHandle !== undefined) {
      return; // already running
    }
    this._intervalHandle = setInterval(
      () => void this._tick(),
      BridgeReceiver.POLL_INTERVAL_MS,
    );
  }

  /**
   * Stop clipboard polling and clear all in-memory state.
   * Safe to call multiple times.
   */
  public stop(): void {
    if (this._intervalHandle !== undefined) {
      clearInterval(this._intervalHandle);
      this._intervalHandle = undefined;
    }
  }

  /**
   * Register a handler that is called whenever a new, unprocessed
   * `PatchPayload` is detected on the clipboard.
   *
   * @returns A `vscode.Disposable` — call `dispose()` to unregister.
   */
  public onPatchReceived(handler: PatchHandler): vscodeTypes.Disposable {
    const entry: HandlerEntry = { handler };
    this._handlers.add(entry);
    return new this._vscode.Disposable(() => {
      this._handlers.delete(entry);
    });
  }

  // -------------------------------------------------------------------------
  // Private polling logic
  // -------------------------------------------------------------------------

  /** Single clipboard-poll tick — called by the interval. */
  private async _tick(): Promise<void> {
    let clipboardText: string;
    try {
      clipboardText = await this._vscode.env.clipboard.readText();
    } catch (err) {
      console.warn('[RelayPatch] Failed to read clipboard:', err);
      return;
    }

    // Skip empty content and already-consumed markers
    if (clipboardText.trim().length === 0 || isConsumedMarker(clipboardText)) {
      return;
    }

    const result = parsePatchPayload(clipboardText);

    if ('kind' in result && result.kind === 'ParseError') {
      // Not a RelayPatch payload — ignore silently (clipboard may hold anything)
      return;
    }

    const payload = result as PatchPayload;

    // Deduplicate — skip if we already processed this id in the current session
    if (this._processedIds.has(payload.id)) {
      return;
    }
    this._processedIds.add(payload.id);

    // Mark consumed immediately so parallel ticks don't double-fire
    try {
      await this._vscode.env.clipboard.writeText(makeConsumedMarker(payload.id));
    } catch (err) {
      console.warn('[RelayPatch] Failed to write consumed marker to clipboard:', err);
      // Non-fatal — we still dispatch the payload; the in-memory set guards
      // against re-processing within the same session.
    }

    // Dispatch to all registered handlers
    for (const entry of this._handlers) {
      try {
        await Promise.resolve(entry.handler(payload));
      } catch (err) {
        console.error('[RelayPatch] Patch handler threw an error:', err);
      }
    }
  }
}

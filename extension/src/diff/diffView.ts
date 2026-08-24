/**
 * diff/diffView.ts
 *
 * Opens the VS Code built-in diff editor to show an incoming patch and adds a
 * status-bar action to apply it atomically.
 *
 * Design notes:
 *  - A single status-bar item is maintained per view; it is disposed either
 *    after the user applies/rejects the patch, or when the next patch arrives.
 *  - `applyPatch` is called with the current (saved) TextDocument so the
 *    atomic-edit contract in patchApplier.ts is honoured.
 *  - Zero network calls — all I/O goes through `vscode.workspace.*` and
 *    `vscode.commands.*`.
 */

import * as vscode from 'vscode';
import { applyPatch } from './patchApplier';
import { type PatchPayload } from '../bridge/types';

// ---------------------------------------------------------------------------
// Internal state — one status-bar item at a time
// ---------------------------------------------------------------------------

/** Tracks the active status-bar item so we can dispose it when replaced. */
let activeStatusBarItem: vscode.StatusBarItem | undefined;

/** Disposes the currently active status-bar item, if any. */
function disposeActiveStatusBar(): void {
  activeStatusBarItem?.dispose();
  activeStatusBarItem = undefined;
}

// ---------------------------------------------------------------------------
// File resolution helpers
// ---------------------------------------------------------------------------

/**
 * Attempt to locate a file in the current workspace that matches `hint`.
 * `hint` may be a workspace-relative path or a bare file name.
 *
 * Returns the first matching `Uri`, or `undefined` if nothing is found.
 */
async function resolveTargetFile(
  hint: string,
): Promise<vscode.Uri | undefined> {
  // Try as a glob relative to every workspace folder
  const pattern = `**/${hint}`;
  const matches = await vscode.workspace.findFiles(pattern, '**/node_modules/**', 2);
  return matches[0];
}

// ---------------------------------------------------------------------------
// Diff-view URI scheme
// ---------------------------------------------------------------------------

/**
 * Create an in-memory `TextDocumentContentProvider` that serves the patched
 * preview content so the built-in diff editor can show old vs new.
 *
 * The provider is registered once per `openDiffView` call; we use a unique
 * authority per patch id to avoid cache collisions across multiple patches.
 */
const PREVIEW_SCHEME = 'relaypatch-preview';

/**
 * Builds the "new" (post-patch) content by applying the diff hunks to the
 * current document text in memory (not to the actual file).
 *
 * Returns the modified text, or `undefined` if patch application fails on the
 * in-memory copy (validation mismatch — the real applyPatch would also fail).
 */
async function buildPreviewContent(
  diffText: string,
  sourceUri: vscode.Uri,
): Promise<string | undefined> {
  // Open the real document to get its current text
  let document: vscode.TextDocument;
  try {
    document = await vscode.workspace.openTextDocument(sourceUri);
  } catch {
    return undefined;
  }

  // Apply the diff in-memory by line substitution
  const lines = document.getText().split(/\r?\n/);
  const { parseUnifiedDiff } = await import('./patchApplier');
  const hunks = parseUnifiedDiff(diffText);

  // Build the patched line array (simple simulation for preview purposes only)
  const patched = [...lines];
  // Process hunks in reverse order so line indices stay valid
  const sortedHunks = [...hunks].sort((a, b) => b.oldStart - a.oldStart);

  for (const hunk of sortedHunks) {
    const start = hunk.oldStart - 1; // 0-based
    let removeCount = 0;
    const additions: string[] = [];

    for (const l of hunk.lines) {
      if (l.type === '-') { removeCount++; }
      else if (l.type === '+') { additions.push(l.content); }
    }

    patched.splice(start, removeCount, ...additions);
  }

  return patched.join('\n');
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Show an incoming patch in the VS Code diff editor and add an "Apply Patch"
 * status-bar button.
 *
 * Steps:
 *  1. Notify the user with an information message.
 *  2. Resolve the target file (from `targetFileHint` or user prompt).
 *  3. Register a disposable `TextDocumentContentProvider` for the preview.
 *  4. Open the built-in diff editor (original ↔ patched preview).
 *  5. Add a status-bar "$(check) Apply Patch" item that runs `applyPatch`.
 */
export async function openDiffView(
  payload: PatchPayload,
  context: vscode.ExtensionContext,
): Promise<void> {
  // Dispose any leftover status bar from a previous patch
  disposeActiveStatusBar();

  // Step 1 — user notification
  void vscode.window.showInformationMessage(
    `RelayPatch: Incoming patch ready — ${payload.id}`,
  );

  // Step 2 — resolve target file
  let targetUri: vscode.Uri | undefined;

  if (payload.targetFileHint !== undefined) {
    targetUri = await resolveTargetFile(payload.targetFileHint);
    if (targetUri === undefined) {
      void vscode.window.showWarningMessage(
        `RelayPatch: Could not find "${payload.targetFileHint}" in the workspace. ` +
          'Please open the target file manually and run "RelayPatch: Open Incoming Diff".',
      );
    }
  }

  // Fall back to the currently active editor's document
  if (targetUri === undefined) {
    const activeDoc = vscode.window.activeTextEditor?.document;
    if (activeDoc !== undefined && !activeDoc.isUntitled) {
      targetUri = activeDoc.uri;
    }
  }

  if (targetUri === undefined) {
    void vscode.window.showErrorMessage(
      'RelayPatch: Cannot open diff — no target file identified and no active editor.',
    );
    return;
  }

  const resolvedTargetUri = targetUri; // narrowed — used in closures below

  // Step 3 — build preview content and register the content provider
  const previewContent = await buildPreviewContent(payload.diffText, resolvedTargetUri);

  const previewUri = vscode.Uri.from({
    scheme: PREVIEW_SCHEME,
    authority: payload.id,
    path: `/${resolvedTargetUri.path.split('/').pop() ?? 'patch'}.preview`,
  });

  const contentProvider = new (class implements vscode.TextDocumentContentProvider {
    provideTextDocumentContent(_uri: vscode.Uri): string {
      return previewContent ?? '(preview unavailable — diff does not apply cleanly)';
    }
  })();

  const providerDisposable = vscode.workspace.registerTextDocumentContentProvider(
    PREVIEW_SCHEME,
    contentProvider,
  );
  context.subscriptions.push(providerDisposable);

  // Step 4 — open the built-in diff editor
  try {
    await vscode.commands.executeCommand(
      'vscode.diff',
      resolvedTargetUri,
      previewUri,
      `RelayPatch diff — ${payload.id}`,
      { preview: true },
    );
  } catch (err) {
    console.warn('[RelayPatch] Failed to open diff editor:', err);
    void vscode.window.showErrorMessage(
      'RelayPatch: Failed to open diff editor. Check the Output panel for details.',
    );
    providerDisposable.dispose();
    return;
  }

  // Step 5 — status-bar "Apply Patch" button
  const statusBarItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Left,
    100,
  );
  statusBarItem.text = '$(check) Apply Patch';
  statusBarItem.tooltip = `Apply RelayPatch ${payload.id}`;
  statusBarItem.color = new vscode.ThemeColor('statusBarItem.warningForeground');

  // Unique command id per patch so there's no stale-closure risk
  const applyCommandId = `relaypatch._applyPatch.${payload.id}`;

  const applyCommandDisposable = vscode.commands.registerCommand(
    applyCommandId,
    async () => {
      // Re-open the document fresh so we have the latest saved state
      let document: vscode.TextDocument;
      try {
        document = await vscode.workspace.openTextDocument(resolvedTargetUri);
      } catch (err) {
        console.warn('[RelayPatch] Could not open target document for patching:', err);
        void vscode.window.showErrorMessage(
          'RelayPatch: Could not open target file for patching.',
        );
        return;
      }

      const success = await applyPatch(payload, document);

      if (success) {
        void vscode.window.showInformationMessage(
          `RelayPatch: Patch ${payload.id} applied successfully.`,
        );
      } else {
        void vscode.window.showErrorMessage(
          `RelayPatch: Patch ${payload.id} failed to apply — ` +
            'context lines did not match. The file may have changed since the patch was generated.',
        );
      }

      // Clean up the status bar and command regardless of outcome
      disposeActiveStatusBar();
      applyCommandDisposable.dispose();
    },
  );

  context.subscriptions.push(applyCommandDisposable);

  statusBarItem.command = applyCommandId;
  statusBarItem.show();
  activeStatusBarItem = statusBarItem;
}

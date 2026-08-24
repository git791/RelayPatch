/**
 * diff/patchApplier.ts
 *
 * Parses unified diff text into typed hunks and applies them atomically to a
 * VS Code `TextDocument`.
 *
 * Atomic-edit contract (AGENTS.md §2.2):
 *  1. Parse the entire diff into `DiffHunk[]`.
 *  2. For every hunk, verify that each context line (' ') and removal line ('-')
 *     in the document matches what the diff expects (line-exact comparison).
 *  3. Only if ALL hunks pass validation, build a single `WorkspaceEdit` and
 *     call `applyEdit` once.
 *  4. If any hunk fails, log a warning and return `false` without touching the
 *     document — partial application is treated as a data-loss bug.
 *
 * Zero-network: only `vscode.workspace.*` APIs — no network primitives.
 */

import * as vscode from 'vscode';
import type { PatchPayload } from '../bridge/types';

// ---------------------------------------------------------------------------
// Public types
// ---------------------------------------------------------------------------

/** A single line entry inside a diff hunk. */
export interface DiffHunkLine {
  /** `' '` = context, `'+'` = addition, `'-'` = removal. */
  readonly type: '+' | '-' | ' ';
  /** Line content WITHOUT the leading type character or newline. */
  readonly content: string;
}

/** A parsed unified-diff hunk (`@@ -a,b +c,d @@`). */
export interface DiffHunk {
  /** 1-based line number in the original file where this hunk starts. */
  readonly oldStart: number;
  /** Number of lines from the original file covered by this hunk. */
  readonly oldCount: number;
  /** 1-based line number in the new file where this hunk starts. */
  readonly newStart: number;
  /** Number of lines in the new file after applying this hunk. */
  readonly newCount: number;
  /** Ordered list of lines in this hunk. */
  readonly lines: ReadonlyArray<DiffHunkLine>;
}

// ---------------------------------------------------------------------------
// Unified diff parser
// ---------------------------------------------------------------------------

/** Regex that matches a unified diff hunk header: `@@ -a[,b] +c[,d] @@`. */
const HUNK_HEADER_RE = /^@@ -(\d+)(?:,(\d+))? \+(\d+)(?:,(\d+))? @@/;

/**
 * Parse a unified diff string into an array of `DiffHunk` objects.
 *
 * Lines before the first `@@` header (e.g. `---`/`+++` file headers) are
 * skipped.  Returns an empty array if no hunk headers are found.
 *
 * No `any` — every intermediate value is narrowly typed.
 */
export function parseUnifiedDiff(diffText: string): DiffHunk[] {
  const rawLines = diffText.split('\n');
  const hunks: DiffHunk[] = [];

  let i = 0;

  while (i < rawLines.length) {
    const line = rawLines[i];
    if (line === undefined) {
      i++;
      continue;
    }

    const match = HUNK_HEADER_RE.exec(line);
    if (match === null) {
      i++;
      continue;
    }

    // Parse header numbers (groups 1-4); group 2 and 4 default to 1 if absent
    const oldStart = parseInt(match[1] ?? '1', 10);
    const oldCount = parseInt(match[2] ?? '1', 10);
    const newStart = parseInt(match[3] ?? '1', 10);
    const newCount = parseInt(match[4] ?? '1', 10);

    if (
      !isFinite(oldStart) ||
      !isFinite(oldCount) ||
      !isFinite(newStart) ||
      !isFinite(newCount)
    ) {
      // Malformed header — skip
      i++;
      continue;
    }

    const hunkLines: DiffHunkLine[] = [];
    i++; // advance past the @@ line

    while (i < rawLines.length) {
      const hunkLine = rawLines[i];
      if (hunkLine === undefined) {
        i++;
        break;
      }

      // Stop when we hit the next hunk header
      if (HUNK_HEADER_RE.test(hunkLine)) {
        break;
      }

      // Skip `--- a/` and `+++ b/` file-header lines that appear inside diffs
      if (hunkLine.startsWith('--- ') || hunkLine.startsWith('+++ ')) {
        i++;
        continue;
      }

      const firstChar = hunkLine[0];

      if (firstChar === '+') {
        hunkLines.push({ type: '+', content: hunkLine.slice(1) });
      } else if (firstChar === '-') {
        hunkLines.push({ type: '-', content: hunkLine.slice(1) });
      } else if (firstChar === ' ') {
        hunkLines.push({ type: ' ', content: hunkLine.slice(1) });
      } else if (firstChar === '\\') {
        // "\ No newline at end of file" — skip
      }
      // Any other prefix (blank trailing lines from some diff tools) is skipped

      i++;
    }

    hunks.push({ oldStart, oldCount, newStart, newCount, lines: hunkLines });
  }

  return hunks;
}

// ---------------------------------------------------------------------------
// Hunk validation
// ---------------------------------------------------------------------------

/**
 * Returns `true` if all context and removal lines in `hunk` match the
 * corresponding lines in `document`.
 *
 * All line numbers are 0-based when passed to the VS Code API.
 */
function validateHunk(hunk: DiffHunk, document: vscode.TextDocument): boolean {
  // oldStart is 1-based; convert to 0-based for VS Code
  let docLineIndex = hunk.oldStart - 1;

  for (const hunkLine of hunk.lines) {
    if (hunkLine.type === '+') {
      // Addition lines don't correspond to an existing doc line — skip
      continue;
    }

    // Context (' ') and removal ('-') lines must match the document
    if (docLineIndex < 0 || docLineIndex >= document.lineCount) {
      console.warn(
        `[RelayPatch] Hunk validation failed: line index ${docLineIndex} is ` +
          `out of bounds (document has ${document.lineCount} lines).`,
      );
      return false;
    }

    const docLine = document.lineAt(docLineIndex);
    if (docLine.text !== hunkLine.content) {
      console.warn(
        `[RelayPatch] Hunk validation failed at document line ${docLineIndex + 1}:\n` +
          `  expected: ${JSON.stringify(hunkLine.content)}\n` +
          `  actual:   ${JSON.stringify(docLine.text)}`,
      );
      return false;
    }

    docLineIndex++;
  }

  return true;
}

// ---------------------------------------------------------------------------
// WorkspaceEdit builder
// ---------------------------------------------------------------------------

/**
 * Build a `WorkspaceEdit` that applies `hunk` to `document`.
 * Callers must have already validated the hunk via `validateHunk`.
 */
function buildHunkEdit(
  hunk: DiffHunk,
  document: vscode.TextDocument,
  edit: vscode.WorkspaceEdit,
): void {
  // We walk the hunk line list, grouping consecutive '-' runs into deletions
  // and '+' runs into insertions so the edit stays minimal.
  let docLineIndex = hunk.oldStart - 1; // 0-based

  let i = 0;
  while (i < hunk.lines.length) {
    const line = hunk.lines[i];
    if (line === undefined) {
      i++;
      continue;
    }

    if (line.type === ' ') {
      // Context line — advance document cursor, no edit
      docLineIndex++;
      i++;
      continue;
    }

    if (line.type === '-') {
      // Collect a run of removals (possibly followed by additions = replacement)
      const removeStart = docLineIndex;
      const removeLines: string[] = [];
      while (i < hunk.lines.length && hunk.lines[i]?.type === '-') {
        removeLines.push(hunk.lines[i]?.content ?? '');
        docLineIndex++;
        i++;
      }

      // Collect immediately following additions
      const addLines: string[] = [];
      while (i < hunk.lines.length && hunk.lines[i]?.type === '+') {
        addLines.push(hunk.lines[i]?.content ?? '');
        i++;
      }

      // Build a replacement range covering the removed lines
      const rangeStart = document.lineAt(removeStart).range.start;
      // The range must include the newline after the last removed line so that
      // the entire line (including the EOL) is replaced.
      const lastRemovedLine = document.lineAt(removeStart + removeLines.length - 1);
      const rangeEnd =
        removeStart + removeLines.length < document.lineCount
          ? document.lineAt(removeStart + removeLines.length).range.start
          : lastRemovedLine.rangeIncludingLineBreak.end;

      const replacementText =
        addLines.length > 0
          ? addLines.join(getEol(document)) + getEol(document)
          : '';

      edit.replace(document.uri, new vscode.Range(rangeStart, rangeEnd), replacementText);
      continue;
    }

    if (line.type === '+') {
      // Standalone addition (not preceded by a '-' run)
      const insertLines: string[] = [];
      while (i < hunk.lines.length && hunk.lines[i]?.type === '+') {
        insertLines.push(hunk.lines[i]?.content ?? '');
        i++;
      }
      const insertPos = document.lineAt(docLineIndex).range.start;
      edit.insert(
        document.uri,
        insertPos,
        insertLines.join(getEol(document)) + getEol(document),
      );
      continue;
    }
  }
}

/** Returns the document's end-of-line character(s) as a string. */
function getEol(document: vscode.TextDocument): string {
  return document.eol === vscode.EndOfLine.CRLF ? '\r\n' : '\n';
}

// ---------------------------------------------------------------------------
// Public entry point
// ---------------------------------------------------------------------------

/**
 * Apply `patchPayload.diffText` to `document` atomically.
 *
 * @returns `true` on success, `false` if any hunk validation fails (document
 * is left untouched) or if `applyEdit` reports failure.
 */
export async function applyPatch(
  patchPayload: PatchPayload,
  document: vscode.TextDocument,
): Promise<boolean> {
  const hunks = parseUnifiedDiff(patchPayload.diffText);

  if (hunks.length === 0) {
    console.warn('[RelayPatch] applyPatch: no hunks found in diff, aborting.');
    return false;
  }

  // Phase 1 — validate ALL hunks before touching the document (atomic contract)
  for (const hunk of hunks) {
    if (!validateHunk(hunk, document)) {
      console.warn(
        `[RelayPatch] applyPatch: hunk starting at line ${hunk.oldStart} ` +
          'failed validation — aborting entire patch to prevent partial application.',
      );
      return false;
    }
  }

  // Phase 2 — build the complete WorkspaceEdit
  const edit = new vscode.WorkspaceEdit();
  for (const hunk of hunks) {
    buildHunkEdit(hunk, document, edit);
  }

  // Phase 3 — single atomic apply
  const applied = await vscode.workspace.applyEdit(edit);
  if (!applied) {
    console.warn('[RelayPatch] applyPatch: vscode.workspace.applyEdit() returned false.');
  }
  return applied;
}

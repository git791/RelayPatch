/**
 * bridge/types.ts
 *
 * Defines the canonical wire-format types that flow from the mobile app through
 * the clipboard bridge into the VS Code extension.  All values arriving from
 * the clipboard are treated as `unknown` and must pass through `parsePatchPayload`
 * before being used — no casts, no `any`.
 */

// ---------------------------------------------------------------------------
// Domain types
// ---------------------------------------------------------------------------

/**
 * A validated patch payload received from the mobile app.
 * Every field is `readonly` — consumers must not mutate a received payload.
 */
export interface PatchPayload {
  /** Stable UUID-style identifier produced by the mobile app. */
  readonly id: string;
  /** Unified diff text (starts with `---` or contains `@@` hunk headers). */
  readonly diffText: string;
  /**
   * Optional workspace-relative or basename hint for the target file.
   * Present when the mobile OCR layer was confident enough to identify it.
   */
  readonly targetFileHint?: string;
}

/**
 * Returned by `parsePatchPayload` when the raw clipboard content cannot be
 * interpreted as a valid `PatchPayload`.
 */
export interface ParseError {
  readonly kind: 'ParseError';
  /** Human-readable explanation suitable for `console.warn`. */
  readonly message: string;
  /** The original raw value (stringified) for diagnostics. */
  readonly raw: string;
}

// ---------------------------------------------------------------------------
// Consumed-marker helpers
// ---------------------------------------------------------------------------

/** Prefix written back to the clipboard after a payload is processed. */
export const CONSUMED_MARKER_PREFIX = 'relaypatch:consumed:' as const;

/**
 * Returns the consumed-marker string for a given payload id.
 * Writing this back to the clipboard prevents re-processing the same payload
 * on the next polling tick.
 */
export function makeConsumedMarker(id: string): string {
  return `${CONSUMED_MARKER_PREFIX}${id}`;
}

/**
 * Returns `true` when `text` is a consumed-marker (i.e. the payload was
 * already handled in a previous tick).
 */
export function isConsumedMarker(text: string): boolean {
  return text.startsWith(CONSUMED_MARKER_PREFIX);
}

// ---------------------------------------------------------------------------
// Runtime type guard / parser
// ---------------------------------------------------------------------------

/**
 * Parses an `unknown` value (typically from `JSON.parse` on clipboard text)
 * into a validated `PatchPayload`.
 *
 * Validation rules:
 *  1. `raw` must be a non-empty string that is valid JSON representing an object.
 *  2. `id` must be a non-empty string.
 *  3. `diffText` must be a non-empty string that contains `---` or `@@`
 *     (basic unified-diff sanity check).
 *  4. `targetFileHint`, if present, must be a string (may be absent entirely).
 *
 * This function never throws — all failure paths return a `ParseError`.
 */
export function parsePatchPayload(raw: unknown): PatchPayload | ParseError {
  // Step 1 — raw must be a non-empty string (clipboard content is always string)
  if (typeof raw !== 'string' || raw.trim().length === 0) {
    return {
      kind: 'ParseError',
      message: 'Clipboard content is not a non-empty string.',
      raw: String(raw),
    };
  }

  // Step 2 — try JSON.parse; return ParseError on syntax errors
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    const message =
      err instanceof SyntaxError
        ? `JSON parse error: ${err.message}`
        : 'JSON parse failed with an unknown error.';
    return { kind: 'ParseError', message, raw };
  }

  // Step 3 — parsed value must be a plain object (not null, not array)
  if (
    typeof parsed !== 'object' ||
    parsed === null ||
    Array.isArray(parsed)
  ) {
    return {
      kind: 'ParseError',
      message: `Expected a JSON object but got: ${Array.isArray(parsed) ? 'array' : typeof parsed}.`,
      raw,
    };
  }

  // Step 4 — use an index-signature approach to safely read fields without `any`
  const obj: Record<string, unknown> = parsed as Record<string, unknown>;

  // Validate `id`
  const id = obj['id'];
  if (typeof id !== 'string' || id.trim().length === 0) {
    return {
      kind: 'ParseError',
      message: `Field "id" must be a non-empty string, got: ${typeof id}.`,
      raw,
    };
  }

  // Validate `diffText`
  const diffText = obj['diffText'];
  if (typeof diffText !== 'string' || diffText.trim().length === 0) {
    return {
      kind: 'ParseError',
      message: `Field "diffText" must be a non-empty string, got: ${typeof diffText}.`,
      raw,
    };
  }

  // Diff sanity check — must contain '---' or '@@' to look like a unified diff
  const looksLikeDiff =
    diffText.includes('---') || diffText.includes('@@');
  if (!looksLikeDiff) {
    return {
      kind: 'ParseError',
      message:
        'Field "diffText" does not look like a unified diff (missing "---" or "@@" markers).',
      raw,
    };
  }

  // Validate `targetFileHint` — optional, but if present must be a string
  const rawHint = obj['targetFileHint'];
  if (rawHint !== undefined && typeof rawHint !== 'string') {
    return {
      kind: 'ParseError',
      message: `Field "targetFileHint" must be a string or absent, got: ${typeof rawHint}.`,
      raw,
    };
  }
  const targetFileHint: string | undefined =
    typeof rawHint === 'string' ? rawHint : undefined;

  // All checks passed — return the validated payload
  const payload: PatchPayload = {
    id: id.trim(),
    diffText,
    ...(targetFileHint !== undefined ? { targetFileHint } : {}),
  };

  return payload;
}

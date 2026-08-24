package dev.relaypatch.app.data

/**
 * Lifecycle state of a patch, as stored in Room and exposed through the UI.
 *
 * State machine (TECHNICAL_DOC.md §2.3):
 *   DRAFTING → READY → SYNCED → APPLIED
 *   Any state → DISCARDED (terminal)
 */
enum class PatchStatus {
    /** NPU inference is running; card is non-interactive (except cancel). */
    DRAFTING,

    /** Diff generated successfully, waiting for bridge to become active. */
    READY,

    /** Diff sent to VS Code via Office Kit bridge. */
    SYNCED,

    /** Developer applied the patch in VS Code. Card collapses to compact row. */
    APPLIED,

    /** User discarded the patch. Excluded from the live queue view. */
    DISCARDED,
}

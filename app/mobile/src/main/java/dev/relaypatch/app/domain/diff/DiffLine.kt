package dev.relaypatch.app.domain.diff

/**
 * A single line of a unified diff, parsed from raw diffText.
 *
 * Used by [DiffInspectorContent] and [DiffLineRow] to render the diff body.
 */
data class DiffLine(
    /** Index of this line within the diff (used as the LazyColumn key). */
    val index: Int,
    /** Hunk number this line belongs to (0-indexed). Used for stage/unstage toggling. */
    val hunkIndex: Int,
    val type: DiffLineType,
    /** Line number in the old (deleted) file, null for addition-only lines. */
    val oldLineNumber: Int?,
    /** Line number in the new (added) file, null for deletion-only lines. */
    val newLineNumber: Int?,
    /** Raw source text (without the leading +/-/ glyph). */
    val content: String,
)

enum class DiffLineType { ADDITION, DELETION, CONTEXT, HUNK_HEADER }



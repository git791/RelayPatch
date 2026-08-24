package dev.relaypatch.app.domain.diff

/**
 * Stateless parser for unified diff text.
 *
 * Validates structure and converts raw diff text into a typed list of
 * [DiffLine] records that the Diff Inspector composable can render directly.
 */
object DiffParser {

    // ── Public types ──────────────────────────────────────────────────────────

    enum class LineType {
        ADDITION,
        DELETION,
        CONTEXT,
        HEADER,
        HUNK_HEADER,
    }

    /**
     * A single parsed line from a unified diff.
     *
     * @property lineType    Classification of the line.
     * @property oldLineNum  1-based old-file line number; null for HEADER / HUNK_HEADER / ADDITION.
     * @property newLineNum  1-based new-file line number; null for HEADER / HUNK_HEADER / DELETION.
     * @property content     The raw line text (including leading +/-/space sigil).
     */
    data class DiffLine(
        val lineType: LineType,
        val oldLineNum: Int?,
        val newLineNum: Int?,
        val content: String,
    )

    // ── Regexes ───────────────────────────────────────────────────────────────

    private val HUNK_HEADER_REGEX =
        Regex("""^@@ -(\d+)(?:,\d+)? \+(\d+)(?:,\d+)? @@.*""")

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns `true` if [text] contains the minimum structure of a valid
     * unified diff: `---` and `+++` header lines plus at least one `@@` hunk.
     */
    fun isValidUnifiedDiff(text: String): Boolean {
        val lines = text.lines()
        val hasOldHeader = lines.any { it.startsWith("--- ") }
        val hasNewHeader = lines.any { it.startsWith("+++ ") }
        val hasHunk = lines.any { it.startsWith("@@ ") }
        return hasOldHeader && hasNewHeader && hasHunk
    }

    /**
     * Parses [diffText] into a structured list of [DiffLine] records.
     *
     * - Lines starting with `---` / `+++` are classified as [LineType.HEADER].
     * - Lines starting with `@@` are classified as [LineType.HUNK_HEADER];
     *   old/new base line numbers are extracted from the hunk header.
     * - Lines starting with `+` are [LineType.ADDITION].
     * - Lines starting with `-` are [LineType.DELETION].
     * - All other lines within a hunk are [LineType.CONTEXT].
     *
     * Line numbers are tracked incrementally from the hunk header offsets.
     */
    fun parse(diffText: String): List<DiffLine> {
        val result = mutableListOf<DiffLine>()
        var oldLine = 0
        var newLine = 0

        for (rawLine in diffText.lines()) {
            when {
                rawLine.startsWith("--- ") || rawLine.startsWith("+++ ") -> {
                    result += DiffLine(
                        lineType = LineType.HEADER,
                        oldLineNum = null,
                        newLineNum = null,
                        content = rawLine,
                    )
                }

                rawLine.startsWith("@@ ") -> {
                    val match = HUNK_HEADER_REGEX.matchEntire(rawLine)
                    if (match != null) {
                        oldLine = match.groupValues[1].toIntOrNull() ?: 0
                        newLine = match.groupValues[2].toIntOrNull() ?: 0
                    }
                    result += DiffLine(
                        lineType = LineType.HUNK_HEADER,
                        oldLineNum = null,
                        newLineNum = null,
                        content = rawLine,
                    )
                }

                rawLine.startsWith("+") -> {
                    result += DiffLine(
                        lineType = LineType.ADDITION,
                        oldLineNum = null,
                        newLineNum = newLine,
                        content = rawLine,
                    )
                    newLine++
                }

                rawLine.startsWith("-") -> {
                    result += DiffLine(
                        lineType = LineType.DELETION,
                        oldLineNum = oldLine,
                        newLineNum = null,
                        content = rawLine,
                    )
                    oldLine++
                }

                else -> {
                    result += DiffLine(
                        lineType = LineType.CONTEXT,
                        oldLineNum = oldLine,
                        newLineNum = newLine,
                        content = rawLine,
                    )
                    oldLine++
                    newLine++
                }
            }
        }

        return result
    }
}

package dev.relaypatch.app.domain.diff

/**
 * A single classified span of text within a diff line, used for syntax
 * highlighting in the Diff Inspector composable.
 *
 * @property text The raw text content of this token.
 * @property type Classification used to select a highlight colour.
 */
data class LineToken(
    val text: String,
    val type: TokenType,
)

/** Classification categories understood by the syntax-highlight renderer. */
enum class TokenType {
    KEYWORD,
    STRING,
    COMMENT,
    IDENTIFIER,
    PUNCTUATION,
    PLAIN,
}

package dev.relaypatch.app.domain.diff.tokenizers

import dev.relaypatch.app.domain.diff.LineToken
import dev.relaypatch.app.domain.diff.TokenType

/**
 * Regex-based syntax tokenizer for Kotlin source lines.
 *
 * Designed to run in <5 ms on a 200-character line (no AST/full-parse overhead).
 * Classifies spans into [TokenType] categories understood by the highlight renderer.
 *
 * Benchmark via DiffTokenizerBenchmark.kt before adding complexity.
 */
object KotlinTokenizer {

    private val KEYWORDS = setOf(
        "fun", "val", "var", "class", "object", "if", "else", "when",
        "return", "null", "true", "false", "is", "in", "for", "while",
        "data", "sealed", "suspend", "override", "private", "public",
        "internal", "companion", "import", "package", "throw", "try",
        "catch", "finally", "by", "as", "typealias",
    )

    // Order matters: COMMENT and STRING are matched first to avoid false keyword
    // hits inside string literals or commented-out code.
    private val TOKEN_REGEX = Regex(
        """(//[^\n]*)""" +                          // group 1: COMMENT
        """|(\"(?:[^\"\\]|\\.)*\")""" +             // group 2: STRING double-quoted
        """|(\'(?:[^\'\\]|\\.)*\')""" +             // group 3: CHAR literal → STRING
        """|\b([A-Za-z_][A-Za-z0-9_]*)\b""" +      // group 4: IDENTIFIER / KEYWORD
        """|([\{\}\(\)\[\]<>,;:\.=!+\-*/%&|^~])""" // group 5: PUNCTUATION
    )

    fun tokenize(line: String): List<LineToken> {
        val tokens = mutableListOf<LineToken>()
        var cursor = 0

        for (match in TOKEN_REGEX.findAll(line)) {
            // Emit any un-matched gap as PLAIN
            if (match.range.first > cursor) {
                tokens += LineToken(line.substring(cursor, match.range.first), TokenType.PLAIN)
            }

            val text = match.value
            val type = when {
                match.groupValues[1].isNotEmpty() -> TokenType.COMMENT
                match.groupValues[2].isNotEmpty() -> TokenType.STRING
                match.groupValues[3].isNotEmpty() -> TokenType.STRING
                match.groupValues[4].isNotEmpty() ->
                    if (text in KEYWORDS) TokenType.KEYWORD else TokenType.IDENTIFIER
                match.groupValues[5].isNotEmpty() -> TokenType.PUNCTUATION
                else -> TokenType.PLAIN
            }
            tokens += LineToken(text, type)
            cursor = match.range.last + 1
        }

        // Trailing un-matched text
        if (cursor < line.length) {
            tokens += LineToken(line.substring(cursor), TokenType.PLAIN)
        }

        return tokens
    }
}

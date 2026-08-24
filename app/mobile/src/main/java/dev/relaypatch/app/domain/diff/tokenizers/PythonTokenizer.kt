package dev.relaypatch.app.domain.diff.tokenizers

import dev.relaypatch.app.domain.diff.LineToken
import dev.relaypatch.app.domain.diff.TokenType

/**
 * Regex-based syntax tokenizer for Python source lines.
 *
 * Designed to run in <5 ms on a 200-character line (no AST/full-parse overhead).
 */
object PythonTokenizer {

    private val KEYWORDS = setOf(
        "def", "class", "if", "elif", "else", "for", "while", "return",
        "import", "from", "as", "with", "try", "except", "finally", "raise",
        "pass", "None", "True", "False", "lambda", "yield", "async", "await",
        "not", "and", "or", "in", "is",
    )

    // Triple-quoted strings are matched first (greedily) before single-quoted.
    private val TOKEN_REGEX = Regex(
        """(#[^\n]*)""" +                          // group 1: COMMENT
        """|(\"\"\"[\s\S]*?\"\"\")""" +            // group 2: triple-double STRING
        """|(\'\'\'[\s\S]*?\'\'\')""" +            // group 3: triple-single STRING
        """|(\"(?:[^\"\\]|\\.)*\")""" +            // group 4: double-quoted STRING
        """|(\'(?:[^\'\\]|\\.)*\')""" +            // group 5: single-quoted STRING
        """|\b([A-Za-z_][A-Za-z0-9_]*)\b""" +     // group 6: IDENTIFIER / KEYWORD
        """|([\{\}\(\)\[\]<>,;:\.=!+\-*/%&|^~])"""// group 7: PUNCTUATION
    )

    fun tokenize(line: String): List<LineToken> {
        val tokens = mutableListOf<LineToken>()
        var cursor = 0

        for (match in TOKEN_REGEX.findAll(line)) {
            if (match.range.first > cursor) {
                tokens += LineToken(line.substring(cursor, match.range.first), TokenType.PLAIN)
            }

            val text = match.value
            val type = when {
                match.groupValues[1].isNotEmpty() -> TokenType.COMMENT
                match.groupValues[2].isNotEmpty() -> TokenType.STRING
                match.groupValues[3].isNotEmpty() -> TokenType.STRING
                match.groupValues[4].isNotEmpty() -> TokenType.STRING
                match.groupValues[5].isNotEmpty() -> TokenType.STRING
                match.groupValues[6].isNotEmpty() ->
                    if (text in KEYWORDS) TokenType.KEYWORD else TokenType.IDENTIFIER
                match.groupValues[7].isNotEmpty() -> TokenType.PUNCTUATION
                else -> TokenType.PLAIN
            }
            tokens += LineToken(text, type)
            cursor = match.range.last + 1
        }

        if (cursor < line.length) {
            tokens += LineToken(line.substring(cursor), TokenType.PLAIN)
        }

        return tokens
    }
}

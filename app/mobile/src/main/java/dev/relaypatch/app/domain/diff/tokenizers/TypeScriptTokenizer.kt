package dev.relaypatch.app.domain.diff.tokenizers

import dev.relaypatch.app.domain.diff.LineToken
import dev.relaypatch.app.domain.diff.TokenType

/**
 * Regex-based syntax tokenizer for TypeScript / JavaScript source lines.
 *
 * Covers .ts, .js, .tsx, and .jsx extensions (see [TokenizerRegistry]).
 * Designed to run in <5 ms on a 200-character line (no AST/full-parse overhead).
 */
object TypeScriptTokenizer {

    private val KEYWORDS = setOf(
        "function", "const", "let", "var", "class", "if", "else", "for",
        "while", "return", "import", "export", "from", "as", "type",
        "interface", "enum", "extends", "implements", "null", "undefined",
        "true", "false", "async", "await", "try", "catch", "finally",
        "throw", "new", "this", "typeof", "instanceof",
    )

    private val TOKEN_REGEX = Regex(
        """(//[^\n]*)""" +                           // group 1: single-line COMMENT
        """|(\/\*[\s\S]*?\*\/)""" +                  // group 2: block COMMENT
        """|(`)(?:[^`\\]|\\.)*`""" +                 // group 3: template literal STRING
        """|(\"(?:[^\"\\]|\\.)*\")""" +              // group 4: double-quoted STRING
        """|(\'(?:[^\'\\]|\\.)*\')""" +              // group 5: single-quoted STRING
        """|\b([A-Za-z_\$][A-Za-z0-9_\$]*)\b""" +   // group 6: IDENTIFIER / KEYWORD
        """|([\{\}\(\)\[\]<>,;:\.=!+\-*/%&|^~])"""  // group 7: PUNCTUATION
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
                match.groupValues[2].isNotEmpty() -> TokenType.COMMENT
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

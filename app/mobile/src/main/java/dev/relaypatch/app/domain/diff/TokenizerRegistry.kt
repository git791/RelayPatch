package dev.relaypatch.app.domain.diff

import dev.relaypatch.app.domain.diff.tokenizers.KotlinTokenizer
import dev.relaypatch.app.domain.diff.tokenizers.PythonTokenizer
import dev.relaypatch.app.domain.diff.tokenizers.TypeScriptTokenizer

/**
 * Maps a file extension to the appropriate syntax tokenizer function.
 *
 * To add a new language: implement a tokenizer object in `tokenizers/` and
 * register it here — no other files need changing (see AGENTS.md §3.1 template).
 */
object TokenizerRegistry {

    /**
     * Returns a tokenizer function for the given [fileExtension] (without leading dot).
     *
     * Falls back to a no-op tokenizer that wraps each line in a single
     * [TokenType.PLAIN] token for unsupported extensions.
     */
    fun tokenizerFor(fileExtension: String): (String) -> List<LineToken> =
        when (fileExtension.lowercase()) {
            "kt", "kts"         -> KotlinTokenizer::tokenize
            "py"                -> PythonTokenizer::tokenize
            "ts", "tsx",
            "js", "jsx"         -> TypeScriptTokenizer::tokenize
            else                -> { line -> listOf(LineToken(line, TokenType.PLAIN)) }
        }
}

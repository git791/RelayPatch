package dev.relaypatch.app.domain.llm

/**
 * Local on-device LLM inference abstraction.
 *
 * Backed by MediaPipe LLM Inference API / LiteRT (TECHNICAL_DOC.md §2.2).
 * The interface is deliberately narrow so alternative runtimes (llama.cpp via
 * Nexa SDK) can be swapped with a single-file change (TECHNICAL_DOC.md §2.2).
 *
 * CAUTION: implementations live in /app/mobile/inference/ — see AGENTS.md §4
 * before modifying quantization, delegate config, or model file references.
 */
interface LocalLLM {

    /**
     * Generates a unified diff from the given context inputs.
     *
     * The system prompt constrains the model to output only a valid unified diff
     * (TECHNICAL_DOC.md §2.2 — "output is parsed as a diff, not free text").
     *
     * @param errorText  OCR-extracted error message text.
     * @param spokenIntent Transcribed developer intent from voice dictation.
     * @param codeSnippet Optional code context pasted/captured by the user.
     *
     * @return [Result.success] with the raw diff string on success.
     *         [Result.failure] with [LLMError] if the model output doesn't parse
     *         as a valid diff after one re-prompt attempt.
     */
    suspend fun generateDiff(
        errorText: String,
        spokenIntent: String,
        codeSnippet: String?,
    ): Result<String>
}

/** Typed errors crossing the LLM domain boundary. */
sealed class LLMError : Exception() {
    /** Model output could not be parsed as a unified diff after one retry. */
    object InvalidDiffOutput : LLMError()

    /** Inference timed out (model not loaded or NPU unavailable). */
    object Timeout : LLMError()

    /** General inference failure with a human-readable message. */
    data class InferenceFailure(override val message: String) : LLMError()
}

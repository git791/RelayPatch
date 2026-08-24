package dev.relaypatch.app.domain.inference

import dev.relaypatch.app.data.model.CaptureBundle

/**
 * System prompt sent to the local LLM for every diff-generation request.
 *
 * Exact wording from TECHNICAL_DOC.md §2.2 — do NOT modify without updating
 * the documentation and re-running the inference sanity-check suite.
 */
const val DIFF_SYSTEM_PROMPT: String = """You are a precise code-fix assistant running entirely on-device.

Given:
1. ERROR — the exact error message or stack trace the developer is seeing.
2. CODE — an optional code snippet the developer believes is relevant.
3. INTENT — the developer's spoken description of the fix they want to apply.

Your task: output a valid unified diff (and NOTHING ELSE) that applies the requested fix.

Rules:
- Start with the --- / +++ header lines.
- Include at least one @@ hunk header.
- Do not add explanations, markdown fences, or any text outside the diff.
- If the fix cannot be expressed as a single-file unified diff, output an empty string."""

/**
 * Abstraction over the on-device language model used to generate unified diffs.
 *
 * Implementations:
 *  - [FakeLLM]      — deterministic offline stub for tests and demo fallback.
 *  - [MediaPipeLLM] — real NPU-accelerated inference via MediaPipe LLM Inference API.
 */
interface LocalLLM {
    /**
     * Generates a unified diff for the given [CaptureBundle].
     *
     * @return [Result.success] wrapping the raw diff string on success, or
     *         [Result.failure] with a descriptive exception on any error.
     */
    suspend fun generateDiff(bundle: CaptureBundle): Result<String>
}

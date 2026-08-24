package dev.relaypatch.app.data.model

/**
 * Output of the Capture Layer (Camera+OCR + Mic+STT).
 * Passed directly to the Inference Layer to generate a diff.
 *
 * @property errorText           Raw error text extracted via OCR or manual entry.
 * @property spokenIntent        Developer's fix intent transcribed from speech.
 * @property optionalCodeSnippet Manually pasted code snippet for additional context; may be null.
 */
data class CaptureBundle(
    val errorText: String,
    val spokenIntent: String,
    val optionalCodeSnippet: String?,
)

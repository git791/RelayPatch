package dev.relaypatch.app.domain.inference

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.relaypatch.app.data.model.CaptureBundle
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// TODO(OfficeKit): This is the real NPU inference path. MediaPipe automatically
//  routes to the Qualcomm QNN / Android NNAPI delegate on Snapdragon devices.
//  Do NOT change the model path or quantization settings without a verified
//  on-device benchmark — silent accuracy regressions are hard to catch.
//  See AGENTS.md §4 for the full caution-zone policy.

private const val MODEL_PATH = "/data/local/tmp/relaypatch/gemma-3n-E2B-it-int4.task"

/**
 * Production [LocalLLM] backed by the MediaPipe LLM Inference API (LiteRT).
 *
 * Loads [MODEL_PATH] once at construction time. If the model file is absent,
 * every [generateDiff] call returns [Result.failure] with a clear message so
 * the caller can gracefully fall back to [FakeLLM].
 *
 * Acceleration: MediaPipe automatically selects the best available delegate.
 * On Snapdragon devices this routes through Qualcomm QNN / Android NNAPI to
 * the NPU. CPU fallback is automatic — no code branch required.
 */
@Singleton
class MediaPipeLLM @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalLLM {

    private val llmInference: LlmInference? = runCatching {
        val modelFile = File(MODEL_PATH)
        if (!modelFile.exists()) {
            null
        } else {
            val options = LlmInferenceOptions.builder()
                .setModelPath(MODEL_PATH)
                .setMaxTokens(1024)
                .setTopK(40)
                .setTemperature(0.1f)
                .setRandomSeed(42)
                .build()
            LlmInference.createFromOptions(context, options)
        }
    }.getOrNull()

    override suspend fun generateDiff(bundle: CaptureBundle): Result<String> {
        val inference = llmInference
            ?: return Result.failure(
                IllegalStateException(
                    "Model file not found at $MODEL_PATH. " +
                        "Run: adb push gemma-3n-E2B-it-int4.task /data/local/tmp/relaypatch/ " +
                        "See models/README.md for full instructions."
                )
            )

        val prompt = buildPrompt(bundle)

        return runCatching {
            suspendCancellableCoroutine { continuation ->
                inference.generateResponseAsync(prompt) { partialResult, done ->
                    if (done) {
                        continuation.resume(partialResult ?: "")
                    }
                }
            }
        }
    }

    private fun buildPrompt(bundle: CaptureBundle): String = buildString {
        appendLine(DIFF_SYSTEM_PROMPT)
        appendLine()
        appendLine("ERROR:")
        appendLine(bundle.errorText)
        appendLine()
        appendLine("INTENT:")
        appendLine(bundle.spokenIntent)
        if (!bundle.optionalCodeSnippet.isNullOrBlank()) {
            appendLine()
            appendLine("CODE:")
            appendLine(bundle.optionalCodeSnippet)
        }
    }
}

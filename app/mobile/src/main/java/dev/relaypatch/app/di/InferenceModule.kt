package dev.relaypatch.app.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.relaypatch.app.domain.inference.FakeLLM
import dev.relaypatch.app.domain.inference.LocalLLM
import dev.relaypatch.app.domain.inference.MediaPipeLLM
import java.io.File
import javax.inject.Singleton

/**
 * Hilt module that provides the [LocalLLM] inference backend as a singleton.
 *
 * Selection logic at app startup:
 *  1. Check whether the Gemma 3n E2B int4 model file exists at [MODEL_PATH].
 *  2. If YES  → return [MediaPipeLLM] backed by the real MediaPipe LLM Inference API.
 *  3. If NO   → log a warning and return [FakeLLM] (deterministic stub for UI dev/testing).
 *
 * This approach ensures:
 *  - UI and ViewModel code can be developed without the 3.1 GB model file present.
 *  - A missing model is never a silent crash — it degrades gracefully with a log warning.
 *  - Swapping runtimes (e.g. to llama.cpp/Nexa SDK fallback — see TECHNICAL_DOC.md §2.2)
 *    requires only changing this module, not call sites across the codebase.
 *
 * CAUTION (AGENTS.md §4 — Inference Caution Zone):
 *  - Do NOT change [MODEL_PATH] without an explicit human instruction — this matches the
 *    path agreed with the demo device setup script.
 *  - Do NOT add a cloud-inference fallback here. If the model is missing, the stub is the
 *    correct degradation — no network calls, no external APIs (AGENTS.md §2.3).
 */
@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    /**
     * Absolute path to the Gemma 3n E2B int4 model on the device.
     * Loaded via `adb push` during device setup (see README §7, Build & Deploy).
     *
     * CAUTION: Do not modify without also updating the setup script and TECHNICAL_DOC.md §2.2.
     */
    private const val MODEL_PATH = "/data/local/tmp/relaypatch/gemma-3n-E2B-it-int4.task"
    private const val TAG = "InferenceModule"

    @Provides
    @Singleton
    fun provideLocalLLM(
        @ApplicationContext context: Context
    ): LocalLLM {
        val modelFile = File(MODEL_PATH)
        return if (modelFile.exists()) {
            Log.i(TAG, "Model found at $MODEL_PATH — using MediaPipeLLM")
            MediaPipeLLM(context)
        } else {
            Log.w(
                TAG,
                "Model NOT found at $MODEL_PATH. Falling back to FakeLLM. " +
                    "Run: adb push <model> $MODEL_PATH to enable real inference."
            )
            FakeLLM()
        }
    }
}

# RelayPatch On-Device Model Setup

RelayPatch runs **Gemma 3n E2B int4** entirely on-device via the MediaPipe
Tasks GenAI runtime. No model weights are committed to the repo — they are too
large for Git and are `.gitignore`d (`models/*.task`).

---

## 1. Download the Model

Run the provided helper script:

```bash
bash models/download_gemma3n.sh
```

The script downloads `gemma-3n-E2B-it-int4.task` into the `models/` directory.

> **Hugging Face login required**  
> The model is gated on Hugging Face. You must accept the licence at  
> <https://huggingface.co/google/gemma-3n-E2B-it-litert-preview> and be  
> logged in (`huggingface-cli login`) **or** provide a token via the  
> `HF_TOKEN` environment variable before running the script.

---

## 2. Push the Model to Your Android Device

After downloading, push the `.task` file to the device with ADB:

```bash
# Create the target directory on the device
adb shell mkdir -p /data/local/tmp/relaypatch/models

# Push the model (this takes 1–3 minutes over USB; ~2 GB)
adb push models/gemma-3n-E2B-it-int4.task \
    /data/local/tmp/relaypatch/models/gemma-3n-E2B-it-int4.task

# Verify it landed correctly
adb shell ls -lh /data/local/tmp/relaypatch/models/
```

---

## 3. Set the Model Path in the App

The app reads the model path from a string resource that you override in a
local `app/mobile/src/main/res/values/local.xml` file (excluded from Git):

```xml
<!-- app/mobile/src/main/res/values/local.xml  (DO NOT COMMIT) -->
<resources>
    <string name="gemma_model_path" translatable="false">
        /data/local/tmp/relaypatch/models/gemma-3n-E2B-it-int4.task
    </string>
</resources>
```

Alternatively, set the path at runtime via **Settings → Developer Options →
Model path** inside the app (debug builds only).

---

## 4. FakeLLM — Testing Without the Model

For unit tests and CI, the app ships a `FakeLlmEngine` that returns scripted
responses without loading any `.task` file. It is automatically activated when:

- The `BuildConfig.USE_FAKE_LLM` flag is `true` (set via `build.gradle.kts`
  `buildConfigField`), **or**
- The model file path resolves to a non-existent file.

To force `FakeLlmEngine` in a local debug build, add to
`app/mobile/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        buildConfigField("Boolean", "USE_FAKE_LLM", "true")
    }
}
```

`FakeLlmEngine` produces deterministic, token-by-token streamed responses so
all ViewModel / UI tests run without the 2 GB model on disk.

---

## 5. Supported Devices

| Requirement | Minimum |
|-------------|---------|
| RAM | 8 GB recommended (6 GB minimum) |
| Storage free | 3 GB |
| Android | 10 (API 29) |
| ABI | arm64-v8a |

Pixel 8 / 8 Pro and newer are tested. Older SoCs may run but will be slow.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `FileNotFoundException` on model path | Re-run `adb push` and verify path |
| `OutOfMemoryError` during model init | Close background apps; need ≥ 2 GB free RAM |
| Slow inference (> 30 s/token) | Device does not have a capable NPU; use `FakeLlmEngine` for development |
| Hugging Face 401 error in download script | Run `huggingface-cli login` or export `HF_TOKEN=<your_token>` |

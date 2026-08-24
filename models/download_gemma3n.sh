#!/usr/bin/env bash
# download_gemma3n.sh
#
# Downloads the Gemma 3n E2B int4 LiteRT model from Hugging Face.
#
# IMPORTANT: This model is gated on Hugging Face.
#   1. Accept the licence at:
#      https://huggingface.co/google/gemma-3n-E2B-it-litert-preview
#   2. Either:
#      a) Run `huggingface-cli login` before executing this script, OR
#      b) Export your token: export HF_TOKEN=hf_xxxxxxxxxxxxxxxxxxxx
#
# The downloaded file (~2 GB) will be saved to:
#   <repo-root>/models/gemma-3n-E2B-it-int4.task

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_FILE="${SCRIPT_DIR}/gemma-3n-E2B-it-int4.task"

HF_REPO="google/gemma-3n-E2B-it-litert-preview"
HF_FILENAME="gemma-3n-E2B-it-int4.task"
HF_URL="https://huggingface.co/${HF_REPO}/resolve/main/${HF_FILENAME}"

# ── Resolve auth header ───────────────────────────────────────────────────────
AUTH_HEADER=""
if [ -n "${HF_TOKEN:-}" ]; then
    AUTH_HEADER="Authorization: Bearer ${HF_TOKEN}"
    echo "[INFO] Using HF_TOKEN from environment."
else
    # Try to read the token cached by `huggingface-cli login`
    HF_TOKEN_FILE="${HOME}/.cache/huggingface/token"
    if [ -f "${HF_TOKEN_FILE}" ]; then
        CACHED_TOKEN="$(cat "${HF_TOKEN_FILE}" | tr -d '[:space:]')"
        if [ -n "${CACHED_TOKEN}" ]; then
            AUTH_HEADER="Authorization: Bearer ${CACHED_TOKEN}"
            echo "[INFO] Using cached Hugging Face token from ${HF_TOKEN_FILE}."
        fi
    fi
fi

if [ -z "${AUTH_HEADER}" ]; then
    echo ""
    echo "[WARN] No Hugging Face token found."
    echo "       If the download fails with HTTP 401 or 403, run:"
    echo "         huggingface-cli login"
    echo "       or set the HF_TOKEN environment variable."
    echo ""
fi

# ── Download ──────────────────────────────────────────────────────────────────
echo "[INFO] Downloading ${HF_FILENAME} from Hugging Face..."
echo "       Source : ${HF_URL}"
echo "       Dest   : ${OUTPUT_FILE}"
echo ""

if command -v wget &>/dev/null; then
    WGET_ARGS=(
        --show-progress
        --progress=bar:force
        -L                          # follow redirects
        -O "${OUTPUT_FILE}"
    )
    if [ -n "${AUTH_HEADER}" ]; then
        WGET_ARGS+=(--header="${AUTH_HEADER}")
    fi
    wget "${WGET_ARGS[@]}" "${HF_URL}"
elif command -v curl &>/dev/null; then
    CURL_ARGS=(
        -L                          # follow redirects
        --progress-bar
        -o "${OUTPUT_FILE}"
    )
    if [ -n "${AUTH_HEADER}" ]; then
        CURL_ARGS+=(-H "${AUTH_HEADER}")
    fi
    curl "${CURL_ARGS[@]}" "${HF_URL}"
else
    echo "[ERROR] Neither wget nor curl is available. Install one and retry."
    exit 1
fi

echo ""
echo "[OK] Download complete: ${OUTPUT_FILE}"
echo "     Size: $(du -sh "${OUTPUT_FILE}" | cut -f1)"

# ── ADB push instructions ─────────────────────────────────────────────────────
echo ""
echo "════════════════════════════════════════════════════════════"
echo "  Next step: push the model to your Android device via ADB"
echo "════════════════════════════════════════════════════════════"
echo ""
echo "  1. Connect your device via USB and enable USB debugging."
echo ""
echo "  2. Create the target directory on the device:"
echo "       adb shell mkdir -p /data/local/tmp/relaypatch/models"
echo ""
echo "  3. Push the model file (~2 GB; takes 1–3 minutes over USB):"
echo "       adb push \\"
echo "         \"${OUTPUT_FILE}\" \\"
echo "         /data/local/tmp/relaypatch/models/${HF_FILENAME}"
echo ""
echo "  4. Verify the push succeeded:"
echo "       adb shell ls -lh /data/local/tmp/relaypatch/models/"
echo ""
echo "  5. In the app, set the model path to:"
echo "       /data/local/tmp/relaypatch/models/${HF_FILENAME}"
echo "     (see models/README.md for details)"
echo ""

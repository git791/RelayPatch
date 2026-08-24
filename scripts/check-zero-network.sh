#!/usr/bin/env bash
# check-zero-network.sh
#
# Enforces the RelayPatch zero-network invariant:
# No code in app/mobile or extension/src may use network-capable symbols
# EXCEPT the explicitly allow-listed bridge module.
#
# Forbidden symbols (case-sensitive):
#   fetch | axios | OkHttp | URLConnection | node:http | node:https
#   Retrofit | HttpClient
#
# Allow-list (excluded from search):
#   app/mobile/src/main/java/dev/relaypatch/app/bridge/OfficeKitTransport.kt

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

SEARCH_DIRS=(
    "${REPO_ROOT}/app/mobile"
    "${REPO_ROOT}/extension/src"
)

ALLOWLIST=(
    "${REPO_ROOT}/app/mobile/src/main/java/dev/relaypatch/app/bridge/OfficeKitTransport.kt"
)

# Build the forbidden pattern as a single alternation
FORBIDDEN_PATTERN='fetch|axios|OkHttp|URLConnection|node:http|node:https|Retrofit|HttpClient'

# Build grep exclude arguments from the allow-list
EXCLUDE_ARGS=()
for allowed_file in "${ALLOWLIST[@]}"; do
    EXCLUDE_ARGS+=("--exclude=$(basename "${allowed_file}")")
done

echo "=== RelayPatch Zero-Network Invariant Check ==="
echo "Searching for forbidden symbols: ${FORBIDDEN_PATTERN}"
echo ""

VIOLATIONS_FOUND=0

for dir in "${SEARCH_DIRS[@]}"; do
    if [ ! -d "${dir}" ]; then
        echo "[SKIP] Directory not found, skipping: ${dir}"
        continue
    fi

    echo "[SCAN] ${dir}"

    # Collect matches, excluding the allow-listed files
    while IFS= read -r match; do
        # Double-check: skip if the matched file path matches any allow-listed file
        skip=false
        for allowed_file in "${ALLOWLIST[@]}"; do
            if [[ "${match}" == *"$(basename "${allowed_file}")"* ]]; then
                # Verify by absolute path
                match_path="${match%%:*}"
                resolved_match="$(cd "$(dirname "${match_path}" 2>/dev/null || echo "${dir}")" 2>/dev/null && pwd)/$(basename "${match_path}" 2>/dev/null)" 2>/dev/null || true
                if [[ "${resolved_match}" == "${allowed_file}" ]]; then
                    skip=true
                    break
                fi
            fi
        done

        if [ "${skip}" = false ]; then
            if [ "${VIOLATIONS_FOUND}" -eq 0 ]; then
                echo ""
                echo "[FAIL] Forbidden network symbols detected outside the allow-list:"
                echo "---------------------------------------------------------------"
            fi
            echo "  ${match}"
            VIOLATIONS_FOUND=1
        fi
    done < <(
        grep -rn \
            --include="*.kt" \
            --include="*.java" \
            --include="*.ts" \
            --include="*.js" \
            "${EXCLUDE_ARGS[@]}" \
            -E "${FORBIDDEN_PATTERN}" \
            "${dir}" 2>/dev/null || true
    )
done

echo ""
if [ "${VIOLATIONS_FOUND}" -ne 0 ]; then
    echo "[RESULT] FAILED — zero-network invariant violated. Fix the violations above."
    echo "         If a symbol is legitimately needed, add the file to the allow-list"
    echo "         in scripts/check-zero-network.sh and get explicit approval in PR."
    exit 1
else
    echo "[RESULT] PASSED — no forbidden network symbols found outside the allow-list."
    exit 0
fi

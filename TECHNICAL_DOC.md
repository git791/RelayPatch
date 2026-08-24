# Technical Documentation — RelayPatch

Audience: engineers building or reviewing the codebase. Pairs with [`PRD.md`](./PRD.md) (why) and [`FRONTEND_DESIGN.md`](./FRONTEND_DESIGN.md) (how it looks).

---

## 1. System Overview

RelayPatch has **no server**. It is two cooperating clients:

```
┌───────────────────────────────────────────────────────────┐
│                        PHONE (Android)                      │
│                                                               │
│  ┌───────────┐   ┌────────────┐   ┌────────────────────┐   │
│  │  Capture   │→ │ Inference  │→ │   Patch Queue        │   │
│  │  Layer     │   │  Layer     │   │   (Room DB)          │   │
│  │ Camera+OCR │   │ Gemma 3n   │   │ Drafting→Ready→Synced│   │
│  │ Mic+STT    │   │ int4, NPU  │   └──────────┬──────────┘   │
│  └───────────┘   └────────────┘               │              │
│                                                 ▼              │
│                                     ┌────────────────────┐    │
│                                     │   Bridge Layer      │    │
│                                     │  Office Kit clipboard│   │
│                                     │  / file transfer     │   │
│                                     └──────────┬──────────┘    │
└────────────────────────────────────────────────┼──────────────┘
                                                   │  (physical bridge,
                                                   │   only active when
                                                   │   phone + laptop paired)
                                                   ▼
┌───────────────────────────────────────────────────────────┐
│                       LAPTOP (VS Code)                      │
│                                                               │
│  ┌────────────┐   ┌────────────────┐                        │
│  │  Watcher    │→ │  Diff View      │                        │
│  │ (extension) │   │  + Apply button │                        │
│  └────────────┘   └────────────────┘                        │
└───────────────────────────────────────────────────────────┘
```

**Design principle:** every arrow above that crosses the phone/laptop boundary can *only* fire while Office Kit is actively bridged. This is intentional — it's what makes Office Kit Telemetry meaningfully high rather than incidental.

---

## 2. Layer-by-Layer Breakdown

### 2.1 Capture Layer (`app/capture/`)
- **Camera + OCR:** `CameraX` for capture, `ML Kit Text Recognition v2` for on-device text extraction. Runs entirely offline; no `INTERNET` permission required for this path.
- **Mic + STT:** Android's built-in `SpeechRecognizer` in offline mode where available (`RecognizerIntent.EXTRA_PREFER_OFFLINE`); falls back to a bundled `whisper.cpp` tiny/int8 model for devices/locales where offline recognition isn't installed.
- **Output:** a `CaptureBundle { errorText: String, spokenIntent: String, optionalCodeSnippet: String? }`

### 2.2 Inference Layer (`app/inference/`)
- **Runtime:** Google's **MediaPipe LLM Inference API** (built on **LiteRT**), which loads a `.task`-format model file and exposes a simple `generateResponse()` streaming call.
- **Model:** **Gemma 3n E2B, int4 quantized** (~3.1 GB on disk). Chosen over larger variants (E4B) for faster time-to-first-token on a live demo; chosen over pure-text models because Gemma 3n supports vision input directly, letting us skip a separate captioning step for screenshots that OCR handles poorly (e.g. handwritten whiteboard notes, in the stretch scope).
- **Acceleration:** MediaPipe automatically selects the best available delegate; on Snapdragon devices this routes through **Qualcomm QNN** / **Android NNAPI** to the NPU. CPU fallback is automatic — no code branch required.
- **Prompting strategy:** a fixed system prompt frames the task strictly as "given this error, this code, and this fix intent, output a unified diff and nothing else" — output is parsed as a diff, not free text, and rejected/re-prompted once if it fails to parse.
- **Alternative considered:** `llama.cpp` via GGUF (through the Nexa SDK) — kept as a documented fallback in `models/README.md` in case Gemma 3n's licensing or size becomes a blocker mid-event; the inference layer is written against a small internal interface (`LocalLLM`) so swapping runtimes is a single-file change.

### 2.3 Patch Queue (`app/patchqueue/`)
- **Storage:** Room (SQLite), single `patches` table.
- **State machine:** `DRAFTING → READY → SYNCED → APPLIED` (terminal states `DISCARDED` also supported).
- **Why local persistence matters:** a developer may generate several patches across an entire Red Light session before ever reconnecting a laptop — the queue must survive app restarts and phone reboots. Room does this by default.

```kotlin
@Entity(tableName = "patches")
data class Patch(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val errorText: String,
    val spokenIntent: String,
    val diffText: String,
    val status: PatchStatus,      // enum: DRAFTING, READY, SYNCED, APPLIED, DISCARDED
    val targetFileHint: String?
)
```

### 2.4 Bridge Layer (`app/bridge/`)
- Wraps the Office Kit SDK's clipboard write and (stretch) file-transfer APIs behind a small `BridgeTransport` interface.
- Listens for a **connection-state callback** from Office Kit; only attempts a sync when the bridge reports "active."
- MVP transport: serialize the `Patch` as a small JSON envelope (`{id, diffText, targetFileHint}`) and push to the shared clipboard the instant the bridge goes active and at least one `READY` patch exists in the queue.
- Stretch transport: write the same envelope to a file and use Office Kit's file-transfer channel for larger diffs that would be awkward via clipboard.

### 2.5 Laptop Companion (`companion/`)
- **VS Code extension**, TypeScript, built on the standard Extension API.
- `watcher.ts` polls the OS clipboard (or a watched sync folder, in file-transfer mode) for the RelayPatch JSON envelope.
- `diffView.ts` parses the envelope, opens VS Code's built-in diff editor (`vscode.diff` command) against the current file, and adds a status-bar **"Apply Patch"** action that performs a standard text edit via the Workspace Edit API.
- No patch is ever applied without this explicit click.

---

## 3. Data Flow (end-to-end)

1. User photographs an error → OCR extracts `errorText`.
2. User speaks a fix → STT extracts `spokenIntent`.
3. (Optional) user pastes the relevant function/snippet.
4. Inference layer prompts the local LLM → returns a `diffText`.
5. A new `Patch` row is written with status `READY`.
6. Bridge layer watches for Office Kit's active-connection callback.
7. On connect, all `READY` patches sync (status → `SYNCED`) via clipboard/file.
8. VS Code watcher picks up the envelope, opens a diff view.
9. Developer reviews and clicks Apply → local file is edited, patch status conceptually becomes `APPLIED` (tracked client-side in the extension; not synced back to the phone in MVP).

---

## 4. Tech Stack — Full Table

| Concern | Technology | Free-tier notes |
|---|---|---|
| Mobile language/UI | Kotlin, Jetpack Compose (Material 3 Expressive) | Fully free/open |
| Camera | CameraX | Free, part of AndroidX |
| OCR | ML Kit Text Recognition v2 | Free, on-device model bundled |
| Speech-to-text | Android `SpeechRecognizer` (offline mode) + whisper.cpp tiny (fallback) | Free; whisper.cpp is Apache-2.0/MIT |
| LLM runtime | MediaPipe LLM Inference API / LiteRT | Free, Apache-2.0 |
| LLM weights | Gemma 3n E2B int4 | Free, open weights (Gemma license — permissive, allows redistribution) |
| NPU delegate | Qualcomm QNN / Android NNAPI | Free, ships with the OS/hardware |
| Local DB | Room (SQLite) | Free, AndroidX |
| Bridge | Office Kit SDK (clipboard + file transfer) | Provided by the event/hardware vendor |
| Laptop companion | VS Code Extension API, TypeScript | Free; publishing to Marketplace is free |
| Version control | GitHub | Free for public/private repos this size |
| CI | GitHub Actions | Free tier: 2,000 min/month on private repos, unlimited on public |
| Design | Figma | Free tier sufficient for a component library this size |
| Diagramming | Excalidraw / Mermaid (in-repo) | Free |

**No paid API is used anywhere in the pipeline** — this was a hard constraint from day one, and it falls out naturally from the "everything runs on-device" principle rather than being a workaround.

---

## 5. Security & Privacy Model

- The phone app requests **no `INTERNET` permission** for its core loop. (It may request local network permissions strictly for Office Kit pairing, which is peer-to-peer, not internet-routed.)
- Patch queue data is stored via Android's `EncryptedFile` API (AES-256-GCM), keyed to the app's own Keystore-backed key.
- The VS Code companion has no telemetry of its own and makes no network calls.
- Because there is no backend, there is no account system, no PII collection, and nothing to leak in a breach — the attack surface is intentionally close to zero.

---

## 6. Testing Strategy (30-hour-appropriate)

Given the timeframe, testing is prioritized by demo-risk, not coverage:

1. **Golden-path integration test** (highest priority): one scripted bug (e.g. a Kotlin null-pointer scenario) with a known-good expected diff, run end-to-end on the actual demo device before Sunday's evaluation rounds.
2. **Inference sanity checks:** a small fixed set of (error, intent) pairs run against the model with assertions on "output parses as a valid diff," not on exact text match (LLM output isn't deterministic enough for that).
3. **Bridge fallback test:** manually verify clipboard sync still works if Office Kit's file-transfer path misbehaves.
4. Unit tests for the `Patch` state machine transitions (fast, cheap, worth having).

---

## 7. Build & Deploy

```bash
# Phone app — debug build for the demo device
cd app
./gradlew assembleDebug
adb install -r build/outputs/apk/debug/app-debug.apk

# Laptop companion — package as .vsix for judge-side install
cd companion
npm install
npx vsce package
code --install-extension relaypatch-0.1.0.vsix
```

No CI/CD pipeline is required for the hackathon build, but a minimal GitHub Actions workflow (`assemble + unit test on push`) is included in `.github/workflows/ci.yml` as a credibility signal for the "Technical Depth" score component.

---

## 8. Known Limitations (state these honestly to judges if asked)

- Single-file diffs only in MVP — no cross-file refactor awareness.
- OCR accuracy depends on screen glare/legibility; a manual text-correction step exists precisely because of this.
- Clipboard-based sync (MVP transport) has a practical size ceiling; large diffs need the stretch file-transfer path.
- The demo device's specific NPU/QNN delegate availability should be verified at Saturday check-in — CPU fallback exists but is visibly slower and should be avoided live if possible.

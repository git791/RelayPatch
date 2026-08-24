# SKILLS.md — RelayPatch Contributor Learning Roadmap

**Purpose:** A free, zero-cost path for a student or new contributor to go from "never touched this stack" to "can meaningfully contribute to RelayPatch" across all four technical pillars: on-device NPU inference, Compose/CameraX mobile development, VS Code extension development, and local IPC/bridge protocol design.

**How to use this doc:** Work top-to-bottom within each track. Tracks 1–3 can be studied in parallel if you're working with a team; Track 4 (Bridge/IPC) assumes basic familiarity with both Track 2 and Track 3, so do it last if solo.

Every resource listed is free. No paid courses, no required textbook purchases, no API keys with billing attached.

---

## Track 1: On-Device NPU Inference, LiteRT & Quantization

**Goal:** Understand how a quantized LLM (Gemma 3n int4) runs directly on a phone's NPU, and how to wire that into an Android app via MediaPipe/LiteRT.

| Step | Topic | Resource | Format |
|---|---|---|---|
| 1.1 | Why on-device inference exists (latency, privacy, offline) | Google AI Edge documentation — "AI Edge overview" | Docs |
| 1.2 | Quantization fundamentals (int8/int4, why accuracy trades off for size/speed) | Hugging Face "Quantization" course chapter (free, no signup required to read) | Docs/course |
| 1.3 | LiteRT (formerly TensorFlow Lite) core concepts | LiteRT official docs — "Get started with LiteRT" | Docs |
| 1.4 | MediaPipe LLM Inference API | Google AI Edge — "LLM Inference guide for Android" | Docs + sample code |
| 1.5 | Running Gemma 3n on-device | Google's Gemma model card + "Gemma 3n on-device" developer blog post | Docs/blog |
| 1.6 | Qualcomm NPU delegate specifics (QNN) | Qualcomm AI Engine Direct SDK documentation (free developer account, no cost) | Docs |
| 1.7 | Hands-on: run a quantized model locally | Google AI Edge GitHub samples repo — clone and run the Android LLM inference sample app | Code (GitHub, free) |
| 1.8 | Benchmarking inference latency/memory on-device | Android Studio Profiler documentation — CPU/Memory/Energy profilers | Docs (built into free Android Studio) |

**Milestone check:** You can explain, in your own words, why int4 quantization was chosen for this project over int8 or fp16, and you've run at least one quantized model inference locally on an emulator or device.

---

## Track 2: Jetpack Compose, Material 3 Expressive & CameraX

**Goal:** Be able to build and modify the mobile app's UI layer — capture flow, patch queue, diff inspector — following the state-hoisting rules in `AGENTS.md`.

| Step | Topic | Resource | Format |
|---|---|---|---|
| 2.1 | Kotlin fundamentals (if new to Kotlin) | Kotlin official "Kotlin Koans" + JetBrains "Kotlin for Android" free track | Interactive course |
| 2.2 | Jetpack Compose basics | Android Developers — "Jetpack Compose Basics" codelab | Codelab (free, self-paced) |
| 2.3 | State & state hoisting | Android Developers — "State in Jetpack Compose" | Docs |
| 2.4 | Material 3 Expressive | Android Developers — "Material 3 Expressive" design + implementation guide | Docs |
| 2.5 | Compose architecture (ViewModel, StateFlow, unidirectional data flow) | Android Developers — "App architecture guide" | Docs |
| 2.6 | CameraX fundamentals | Android Developers — "CameraX overview" + "Getting Started with CameraX" codelab | Codelab |
| 2.7 | ML Kit Text Recognition v2 (OCR) | Google ML Kit documentation — "Recognize text in images" (Android quickstart) | Docs + sample code |
| 2.8 | Room DB + encryption | Android Developers "Room" docs + SQLCipher for Android (open-source encrypted SQLite) documentation | Docs |
| 2.9 | Android SpeechRecognizer API | Android Developers reference docs — `android.speech.SpeechRecognizer` | Docs |
| 2.10 | Whisper.cpp fallback (offline STT) | `whisper.cpp` GitHub repo README + Android JNI binding examples in the repo's community bindings | Code (GitHub, free) |
| 2.11 | Compose previews & UI testing | Android Developers — "Preview your UI" + "Compose testing cheat sheet" | Docs |

**Milestone check:** You've built a small Compose screen from scratch with hoisted state, wired a CameraX preview with a basic overlay, and can explain the difference between `collectAsState()` and `collectAsStateWithLifecycle()`.

---

## Track 3: VS Code Extension API & Workspace Diff Manipulation

**Goal:** Be able to build and modify the companion VS Code extension, including the diff editor integration and file-transfer receiving logic.

| Step | Topic | Resource | Format |
|---|---|---|---|
| 3.1 | TypeScript fundamentals (if needed) | TypeScript official Handbook | Docs |
| 3.2 | VS Code Extension API — "Your First Extension" | code.visualstudio.com — Extension API "Get Started" guide | Docs + generator (`yo code`, free) |
| 3.3 | Extension anatomy: `package.json`, activation events, contribution points | VS Code Extension API — "Extension Anatomy" | Docs |
| 3.4 | Commands & the Command API | VS Code Extension API — "Command" guide | Docs |
| 3.5 | `WorkspaceEdit` and programmatic file edits | VS Code Extension API reference — `vscode.WorkspaceEdit` class docs | Docs |
| 3.6 | Custom Editors & Webviews (for the custom diff view) | VS Code Extension API — "Custom Editor API" + "Webview API" guides | Docs |
| 3.7 | Built-in diffing APIs as reference | VS Code Extension API — `vscode.diff` command + `TextDocumentContentProvider` | Docs |
| 3.8 | Strict TypeScript configuration | TypeScript Handbook — "tsconfig Reference" (`strict`, `noUncheckedIndexedAccess`) | Docs |
| 3.9 | Testing VS Code extensions | VS Code Extension API — "Testing Extensions" guide (`@vscode/test-electron`) | Docs |
| 3.10 | Publishing/packaging basics (useful even if not publishing) | VS Code Extension API — "Publishing Extensions" | Docs |

**Milestone check:** You've scaffolded a "Hello World" extension via `yo code`, registered a command, and used `WorkspaceEdit` to programmatically insert text into an open file.

---

## Track 4: Office Kit / Local IPC Data Modeling

**Goal:** Understand how the phone and laptop exchange patch data over a local, zero-network bridge, and be able to reason about or extend the `BridgeTransport` interface.

| Step | Topic | Resource | Format |
|---|---|---|---|
| 4.1 | IPC fundamentals (why local transport ≠ network transport) | MDN Web Docs / general OS docs — "Inter-process communication" overview (search "IPC overview" on a general CS reference like OSDev wiki, free) | Docs |
| 4.2 | Office Kit SDK — Clipboard API | iQOO/vivo Office Kit developer documentation (provided in hackathon dev portal) | Docs |
| 4.3 | Office Kit SDK — File-transfer API | iQOO/vivo Office Kit developer documentation (provided in hackathon dev portal) | Docs |
| 4.4 | Designing a handshake/state-machine protocol | Read `TECHNICAL_DOC.md` "Bridge Protocol & Handshake Sequence" in this repo first, then compare against a general reference: "Designing Data-Intensive Applications" author Martin Kleppmann's free blog posts on protocol design (specific posts, not the paid book) | Blog |
| 4.5 | Kotlin `Flow` for state streams | Kotlin official docs — "Asynchronous Flow" | Docs |
| 4.6 | Serialization for cross-language payloads (Kotlin ↔ TypeScript) | `kotlinx.serialization` official docs (Kotlin side) + TypeScript's built-in `JSON` handling with hand-written type guards (TypeScript Handbook "Narrowing") | Docs |
| 4.7 | Error handling across a transport boundary | Read `AGENTS.md` §5 (Bridge caution zone) in this repo, then Kotlin docs on `Result<T>` and sealed classes for typed errors | Docs |

**Milestone check:** You can draw the full Red Light → Green Light → Bridge Active → Sync Complete state diagram from memory and explain what payload crosses the wire at each transition.

---

## Suggested Order for a Solo Contributor New to Everything

1. Track 2, steps 2.1–2.5 (Kotlin/Compose basics) — you need this to read *any* of the mobile codebase.
2. Track 1, steps 1.1–1.4 (inference concepts) — enough to understand `inference/` without needing to modify it yet.
3. Track 2, steps 2.6–2.11 (CameraX, OCR, STT, Room) — now you can meaningfully touch the capture flow.
4. Track 3, all steps — the VS Code side is largely independent and can be learned in a self-contained sprint.
5. Track 4 last — it requires vocabulary from both Track 2 and Track 3 to be meaningful.

Budget roughly 2–3 focused evenings per numbered step for a contributor with general programming experience but no prior exposure to that specific technology; faster if you already know adjacent tools (e.g., any prior React experience accelerates Compose, any prior mobile dev accelerates CameraX).

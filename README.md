# 🔌 RelayPatch

**Fix code with your phone. Ship it from your laptop. Nothing leaves your device.**

> Built for **iQOO City Battles 2026** (Developer Tools track) — but designed to outlive the hackathon.

[![Track](https://img.shields.io/badge/Track-Developer%20Tools-2DE1C2)](#) [![Offline](https://img.shields.io/badge/Runs-100%25%20Offline-34D399)](#) [![On--Device%20AI](https://img.shields.io/badge/AI-On--Device%20NPU-8C7CFF)](#) [![License](https://img.shields.io/badge/License-MIT-9AA4B2)](#)

---

## 🧭 What is this, in one breath?

You're away from your laptop — commuting, in a meeting, or (this weekend) forced into a **phone-only Red Light build sprint**. Something breaks. Instead of losing the next hour, you open RelayPatch, **photograph the error**, **speak the fix you have in mind**, and a language model running entirely *on your phone's NPU* drafts a real code patch — no internet, no cloud, no API keys. The moment your laptop reconnects, the patch is sitting there ready to review and apply.

That's it. That's the whole idea. Everything else in this document is detail.

---

## 📖 Table of Contents

1. [Why this exists](#-why-this-exists)
2. [Quickstart — I just want to see it work](#-quickstart--i-just-want-to-see-it-work)
3. [How it works (user's point of view)](#-how-it-works-users-point-of-view)
4. [How it works (developer's point of view)](#-how-it-works-developers-point-of-view)
5. [Project structure](#-project-structure)
6. [Tech stack at a glance](#-tech-stack-at-a-glance)
7. [Setting up your dev environment](#-setting-up-your-dev-environment)
8. [Running the app](#-running-the-app)
9. [The document set — what to read next](#-the-document-set--what-to-read-next)
10. [Privacy & data model](#-privacy--data-model)
11. [FAQ](#-faq)
12. [Glossary](#-glossary)
13. [Credits & license](#-credits--license)

---

## 🎯 Why this exists

Every hackathon team building "an app that runs on a phone" is optimizing for the 15% of the judging rubric that rewards camera/voice/NPU usage. Almost nobody optimizes for the other 10% — **Office Kit bridge telemetry** — because that requires designing your *entire product loop* around the laptop↔phone split, not just tolerating it.

RelayPatch is built the other way around: the phone-only moment isn't an inconvenience to survive, it's the product's reason to exist. That's also why it's not just a hackathon gimmick — any developer who's ever wanted to fix a bug on a train, during a stand-up, or in the field without a laptop has the same problem.

**Problem statement:** *Developers lose all momentum the moment they're away from a laptop. Existing on-device AI tools are chat toys — none of them turn a phone-only moment into an actual, applyable code fix waiting for you the second you reopen your laptop.*

---

## ⚡ Quickstart — I just want to see it work

You don't need to understand any of the architecture below to try RelayPatch. Five steps:

1. **Install the phone app** — sideload the APK from `/app/release/` onto an Android device (Snapdragon NPU recommended, but it'll run on CPU too, just slower).
2. **Install the laptop companion** — a tiny VS Code extension, `code --install-extension relaypatch.vsix`.
3. **On your phone:** tap the camera icon, point it at any error message or stack trace (a screenshot works too).
4. **Speak your fix**, e.g. *"add a null check before calling getUser"* — RelayPatch transcribes it on-device and drafts a patch.
5. **Open your laptop.** The moment RelayPatch detects the Office Kit bridge is live, the patch appears as a notification in VS Code with a one-click **Apply Diff** button.

No sign-up. No API key. No internet required at any step above.

---

## 🧑‍💻 How it works (user's point of view)

```
 ┌─────────────────────────┐        ┌──────────────────────────┐
 │        YOUR PHONE        │        │        YOUR LAPTOP        │
 │   (Red Light — offline)  │        │   (Green Light — bridged) │
 ├─────────────────────────┤        ├──────────────────────────┤
 │ 1. 📷 Snap the error      │        │                            │
 │ 2. 🎙️ Speak the fix       │        │                            │
 │ 3. 🧠 On-device model      │        │                            │
 │    drafts a patch         │        │                            │
 │ 4. 📥 Patch queues locally│  ⇄  Office Kit reconnects  ⇄        │
 │                           │        │ 5. 🔔 Patch lands in VS Code│
 │                           │        │ 6. 👀 Review the diff      │
 │                           │        │ 7. ✅ One-click apply       │
 └─────────────────────────┘        └──────────────────────────┘
```

You can queue up **multiple patches** across an entire Red Light sprint — RelayPatch keeps a running list (with status: *Drafting → Ready → Synced → Applied*) so nothing gets lost even if you generate five fixes in a row before ever touching your laptop again.

---

## 🛠 How it works (developer's point of view)

RelayPatch is two apps that talk to each other only when physically bridged:

- **`/app`** — an Android app (Kotlin + Jetpack Compose) that owns capture (camera/OCR + mic/STT), on-device inference (quantized LLM via NPU), and a local patch queue (Room database).
- **`/companion`** — a VS Code extension (TypeScript) that watches for an incoming patch over the Office Kit clipboard/file bridge, parses it into a real unified diff, and renders an in-editor review UI.

There is **no server**. The "backend" is the phone itself. See [`TECHNICAL_DOC.md`](./TECHNICAL_DOC.md) for the full architecture, data flow, and module breakdown.

---

## 📁 Project structure

```
relaypatch/
├── app/                      # Android app (phone side)
│   ├── capture/               # Camera OCR + mic STT
│   ├── inference/              # On-device LLM wrapper (MediaPipe/LiteRT)
│   ├── patchqueue/             # Room DB — local patch state machine
│   ├── bridge/                 # Office Kit clipboard/file sync
│   └── ui/                     # Jetpack Compose screens (Material 3 Expressive)
├── companion/                 # VS Code extension (laptop side)
│   ├── src/watcher.ts          # Detects incoming patch
│   ├── src/diffView.ts         # Renders diff + apply button
│   └── package.json
├── models/                    # Quantized model download scripts (not committed)
├── docs/                      # This document set
└── README.md                  # You are here
```

---

## 🧱 Tech stack at a glance

| Layer | Choice | Why | Cost |
|---|---|---|---|
| Mobile UI | Kotlin + Jetpack Compose, Material 3 Expressive | Native performance, matches 2026 Android design language, works on OriginOS 6 | Free |
| On-device LLM | Gemma 3n (E2B, int4) via MediaPipe LLM Inference API / LiteRT | Multimodal (text+vision), purpose-built for phone NPUs, actively maintained by Google | Free, open weights |
| NPU acceleration | Qualcomm QNN delegate / Android NNAPI | Uses the Snapdragon NPU the hardware is built around | Free (on-device) |
| OCR (error capture) | ML Kit Text Recognition v2 | On-device, no network call, fast | Free |
| Speech-to-text | Android SpeechRecognizer (on-device mode) / whisper.cpp (tiny, int8) as fallback | Offline, low latency | Free |
| Local persistence | Room (SQLite) | Standard, reliable, zero infra | Free |
| Bridge sync | Office Kit clipboard & file-transfer APIs | The event's own proprietary bridge — required for telemetry scoring | Provided by event |
| Laptop companion | VS Code Extension API (TypeScript) | Meets developers where they already work | Free |
| Source control / CI | GitHub + GitHub Actions | Free tier is generous for a project this size | Free |
| Design | Figma (free tier) | Component library, prototyping | Free |

See [`TECHNICAL_DOC.md`](./TECHNICAL_DOC.md) for full rationale and alternatives considered.

---

## 🖥 Setting up your dev environment

**Prerequisites**
- Android Studio (Ladybug or newer) with an Android 13+ target
- Node.js 20+ and VS Code (for the companion extension)
- A physical Android device with a Snapdragon NPU is *strongly* recommended — emulators can't exercise NPU acceleration
- Git + GitHub CLI (optional but handy)

**Clone & bootstrap**
```bash
git clone https://github.com/<your-org>/relaypatch.git
cd relaypatch

# Phone app
cd app && ./gradlew assembleDebug

# Laptop companion
cd ../companion && npm install && npm run build
```

**Download the model** (not committed to git — see `models/README.md`):
```bash
cd models
./download_gemma3n.sh   # pulls the int4 quantized .task file from Hugging Face
```

---

## ▶️ Running the app

1. Push the model file to the device: `adb push models/gemma-3n-E2B-it-int4.task /data/local/tmp/relaypatch/`
2. Install the debug build: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. In VS Code, run `Extensions: Install from VSIX...` and pick `companion/relaypatch.vsix`
4. Pair the two devices with Office Kit as you normally would
5. Grant camera + microphone permissions on first launch — everything after that is offline

---

## 📚 The document set — what to read next

This README is the front door. Depending on what you're here for:

| I want to... | Read |
|---|---|
| Understand *why* we're building this and for whom | [`PRD.md`](./PRD.md) |
| Understand the architecture, data flow, and model choices | [`TECHNICAL_DOC.md`](./TECHNICAL_DOC.md) |
| Understand the visual design system — colors, type, motion | [`FRONTEND_DESIGN.md`](./FRONTEND_DESIGN.md) |
| Work on this repo using an AI coding agent (Claude Code, Cursor, etc.) | [`AGENTS.md`](./AGENTS.md) |
| Know what skills to learn before contributing, and where to learn them free | [`SKILLS.md`](./SKILLS.md) |
| Rehearse the live demo/pitch | [`DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) |
| Contribute code | [`CONTRIBUTING.md`](./CONTRIBUTING.md) |

---

## 🔒 Privacy & data model

RelayPatch's entire pitch rests on one sentence being true, always: **your code and your voice never leave your device unless you personally carry it there.**

- No network permission is requested by the phone app beyond what Android requires for local Wi-Fi Direct/Office Kit pairing.
- The LLM, OCR, and speech-to-text all run on-device. There is no cloud fallback, by design — not even an optional one. This is a constraint, not a limitation we plan to "fix" later.
- The patch queue lives in a local Room database, encrypted at rest via Android's `EncryptedFile` API, and is cleared on demand.
- The laptop companion never phones home either — it only reads what the bridge hands it.

---

## ❓ FAQ

**Does this replace my laptop?**
No — Green Light (dual-device) time is still where you'll do heavy refactors and architecture work. RelayPatch exists for the *other* time: when only the phone is available.

**What if the model gets the fix wrong?**
Every patch is a *proposed* diff, never auto-applied. You review it in VS Code like any PR before it touches your code.

**Does it work without a Snapdragon NPU?**
Yes, falling back to CPU inference — just slower (roughly 3–5x). The NPU path is what unlocks fast, seamless drafting.

**Why not just use a chatbot app and copy-paste?**
Because copy-pasting across a phone-only lockout (like Red Light) isn't possible — Office Kit's bridge is the only sanctioned channel, and a plain chat app doesn't understand "this is a diff, sync it, and surface it as an editor action" the way RelayPatch's patch queue does.

---

## 📗 Glossary

| Term | Meaning |
|---|---|
| **Red Light** | Phone-only build phase (laptop closed) — where RelayPatch's capture flow happens |
| **Green Light** | Dual-device build phase — where the patch actually reaches your code |
| **Patch** | A generated unified diff, not yet applied, sitting in the local queue |
| **Bridge** | Office Kit's clipboard/file-transfer channel between phone and laptop |
| **NPU** | Neural Processing Unit — the Snapdragon chip's dedicated AI accelerator |
| **On-device inference** | Running the AI model locally, with no network call |

---

## 🙌 Credits & license

Built for iQOO City Battles 2026 (Developer Tools track), organized by iQOO × Reskilll.

### 👥 Team
- **Mohammed Ayaan Adil Ahmed**
- **BiBi Sufiya Shariff**

Model weights: Google Gemma 3n (Gemma license). Code: MIT License unless noted otherwise.

*Great ideas don't need desks — and apparently, neither do bug fixes.*

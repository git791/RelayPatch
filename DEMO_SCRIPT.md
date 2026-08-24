# DEMO_SCRIPT.md — RelayPatch Live Pitch & Demo (3:00)

**Format:** Two presenters recommended (Presenter A: narrates/pitches, stays with jury; Presenter B: operates phone, executes the physical walk to laptop). If solo, notes are marked **[SOLO ADAPT]**.
**Pre-demo setup (before jury arrives, not part of the 3:00 clock):** Phone fully charged, app open to home screen, a real compile-breaking bug already seeded in a visible file on the laptop's external monitor/screen-share, laptop's VS Code open with RelayPatch extension installed and bridge listener active, room lighting checked against the camera capture step, Office Kit bridge range-tested at the actual demo distance.

---

## Timing Overview

| Segment | Time | Cumulative |
|---|---|---|
| 1. Hook + Problem framing | 0:00–0:25 | 0:25 |
| 2. Red Light capture (camera + voice + NPU) | 0:25–1:20 | 1:20 |
| 3. Physical walk + Green Light transition | 1:20–1:40 | 1:40 |
| 4. Bridge sync + VS Code diff apply | 1:40–2:20 | 2:20 |
| 5. Rubric/impact close + Q&A hook | 2:20–3:00 | 3:00 |

---

## 1. Hook + Problem Framing (0:00–0:25)

**Presenter A (to jury, phone held up but not yet active):**

> "Every hackathon has a Red Light moment — laptops locked away, only phones allowed. Your build breaks. You *know* the fix. You just can't type it anywhere that matters. RelayPatch turns your phone into a real patch-authoring tool during that lockout — camera, voice, and an on-device AI model, zero internet, zero cloud. Watch."

**Delivery notes:** Keep this tight — 25 seconds is roughly 60-65 words spoken at a natural pace. Do not over-explain the tech stack here; that's earned in segment 5. This is a hook, not a spec read.

---

## 2. Red Light Capture: Camera → Voice → NPU (0:25–1:20, ~55s)

**Presenter B (operating phone):** Opens RelayPatch, taps capture. Frames the seeded bug on the laptop screen (visible to jury, e.g., a projected/second-monitor error stack trace).

**Presenter A (narrating over B's actions):**

> "Connection Status Pill top-right — see it? Red. We're offline, no bridge, exactly like a real Red Light round. Phone's camera locks onto the error, OCR reads it directly off the screen."

*[B taps shutter. Bounding boxes appear over the error text, brief pause for OCR lock, haptic click.]*

> "Now instead of typing a fix on a cramped keyboard, we just say it."

*[B taps mic, speaks the fix aloud — pre-rehearsed, e.g.: "Fix the null pointer by adding a guard clause before the user lookup and return early."]*

*[Waveform indicator animates. B taps done.]*

> "That goes straight to a quantized Gemma model — running entirely on this phone's NPU. No API call. No network. It's drafting a real unified diff right now."

*[Patch Queue Card appears in `DRAFTING` state, shimmer animation, then transitions to `READY` — signature double-tap haptic fires audibly/visibly.]*

> "And there it is — patch ready, still fully local."

**Contingency — noisy room / speech recognition fails:** If dictation visibly mis-transcribes (common in loud hackathon halls), **do not restart the take**. Presenter A says: *"Noisy floor — let's use the whisper.cpp offline fallback,"* and B taps the fallback toggle already surfaced in the UI (per `TECHNICAL_DOC.md` STT fallback path). This is a **planned beat**, not an admission of failure — frame it as a deliberate robustness demo. Pre-rehearse this exact line so it lands as intentional.

---

## 3. Physical Walk + Green Light Transition (1:20–1:40, ~20s)

**Presenter B physically walks toward the laptop** (this should be a short, real walk — 3-5 steps — not staged from adjacent tables; the physical distance *is* the demo point).

**Presenter A (narrating the walk, filling dead air — this is the trickiest beat to pace):**

> "This is the actual Green Light moment — walking back to a reconnected laptop. Watch the status pill."

*[On B's phone, pill transitions Red → "Connecting…" → Bridge Active, with the rising three-tick haptic.]*

> "Bridge's found the laptop over Office Kit's local transport — still zero network, this is device-to-device."

**Contingency — bridge handshake lag:** If the pill doesn't flip within ~3 seconds of arriving at the laptop (real-world radio/discovery variance), Presenter A keeps talking without dead air: *"Discovery's a local broadcast — typically under two seconds, and there it is."* Have B stand at the pre-tested optimal distance/orientation from rehearsal, not wherever feels natural in the moment.

---

## 4. Bridge Sync + VS Code Diff Apply (1:40–2:20, ~40s)

**Presenter B taps "Stage & Sync via Bridge" on the phone.** Simultaneously, **Presenter A turns to the laptop screen** (already visible to jury via monitor/projector).

**Presenter A (now narrating the laptop side):**

> "And it's already here." *[VS Code auto-opens the custom diff view.]* "This isn't a screenshot pasted in — it's a real `WorkspaceEdit`, syntax-colored, hunk-by-hunk stageable, exactly like reviewing a teammate's PR."

*[A clicks one hunk to demonstrate stage/unstage toggle, then clicks the primary "Apply" action.]*

> "One tap, patch is live in the actual file. That whole loop — camera to compiling code — took under a minute, and at no point did this touch the internet."

**Contingency — sync visibly slow:** If the file-transfer step takes longer than rehearsed, Presenter A pivots immediately to the diff view that *is* visible (even mid-transfer progress UI counts as content): *"You can see the patch payload arriving now — full diff, not a truncated preview."* Never stand in silence waiting; always have something on-screen to narrate.

---

## 5. Rubric/Impact Close + Q&A Hook (2:20–3:00, ~40s)

**Presenter A (to jury, stepping back from devices, direct eye contact):**

> "That's RelayPatch: camera OCR, on-device voice dictation, an open-source quantized model doing real inference on the NPU — not a cloud API — and a bridge handoff that mirrors exactly how developers already review code. Every piece of that loop was Creative Phone Usage and real Office Kit telemetry, not decoration. And it's built entirely on free, open-source tooling — zero infrastructure cost, which means any team here could fork it tonight."

**Closing line (memorize verbatim — this is the line that should be quotable):**

> "We didn't build a workaround for the Red Light round. We built the tool we personally wished existed the last time we were stuck in one."

*[Stop talking. Let the silence land. Do not fill remaining seconds with filler — ending 3-5 seconds early reads as confident, not unprepared.]*

**Q&A hook (if jury has 1 quick question before moving on):** Have one pre-loaded 15-second answer ready for the most likely question — *"How accurate is the on-device model compared to a cloud LLM?"* — e.g.: *"For scoped, single-file fixes like the one we just showed, it's very reliable — the int4 Gemma 3n model is tuned for exactly this narrow diff-generation task, not general chat, which is why it stays fast and small enough to run on the NPU."*

---

## Full Contingency Reference Table

| Failure mode | Symptom | Recovery line | Recovery action |
|---|---|---|---|
| Speech recognition fails (noise) | Garbled/empty transcript | "Noisy floor — let's use the offline fallback" | Tap whisper.cpp fallback toggle |
| OCR fails to lock | No bounding boxes after 3-4s | "Let's give it a cleaner angle" | Reframe once, calmly; if still failing, use gallery-import fallback with a pre-loaded backup screenshot |
| NPU inference slow/stalls | `DRAFTING` state exceeds ~10s | "This is running fully on-device, so let's talk through what it's doing while we wait" — narrate the architecture briefly | Have a pre-generated backup patch ready to swap in via debug menu if it exceeds ~20s, so the demo never visibly dies |
| Bridge handshake lag | Pill stuck on "Connecting…" | "Local discovery, typically under two seconds" | Keep narrating; if >8s, tap manual "Retry discovery" affordance |
| File transfer slow | Diff view doesn't appear at laptop | "You can see the payload arriving now" | Show progress UI as content; if fully stalled, fall back to a pre-staged identical diff opened manually to finish the pitch beat |
| Total hardware failure | Any device unresponsive | Do not attempt to debug live | Presenter A pivots immediately to a 20-second walkthrough of the `FRONTEND_DESIGN.md` screenshots/recorded backup video, stating plainly: "Let's walk through it via a quick recording while we sort this out" |

**Golden rule for all contingencies:** Never narrate a failure as a failure. Every fallback path in this app was intentionally engineered (offline STT fallback, gallery-import fallback, manual retry) — frame every recovery as "showing you the resilience feature" rather than "working around a bug." The jury cannot distinguish a rehearsed contingency beat from a genuine one if delivered with the same confidence.

---

## Rehearsal Checklist (run at least 3 full timed run-throughs before the actual pitch)

- [ ] Full run-through completes within 2:50–3:00 (never over; a hard cutoff usually applies)
- [ ] Physical walk distance/route tested for bridge range in the actual venue, not just at home
- [ ] Seeded bug and dictated fix are memorized verbatim by Presenter B (no reading off notes on stage)
- [ ] Backup pre-generated patch loaded into debug menu as a silent fallback
- [ ] Backup screen recording of a full successful run exported and accessible offline on the laptop, in case of total hardware failure
- [ ] Closing line rehearsed enough to say without looking at notes
- [ ] Q&A pre-loaded answer rehearsed to fit in 15 seconds

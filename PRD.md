# PRD — RelayPatch

**Status:** Draft for iQOO City Battles 2026
**Track:** Developer Tools
**Owner:** [team name]
**Last updated:** 2026

---

## 1. Problem Statement

> Developers lose all momentum the moment they're away from a laptop — during a commute, a client visit, field work, or (for this event specifically) a mandated phone-only Red Light sprint. Existing on-device AI coding tools are chat toys; none of them turn a phone-only moment into an actual, applyable code fix waiting for the developer the second they reopen their laptop.

This is not a hackathon-only inconvenience. It's a daily, universal developer problem that the event's own Red Light/Green Light mechanic happens to simulate at high intensity.

---

## 2. Goals

| Goal | Metric |
|---|---|
| Let a developer generate a real, applyable code patch using only their phone | A working diff appears in the laptop companion within one demo cycle |
| Do it with zero network dependency | 0 outbound network calls during capture → inference → queue |
| Make the laptop handoff feel instant | Patch surfaces in VS Code within seconds of Office Kit reconnect |
| Be usable by a nervous demo-er in under 60 seconds | End-to-end capture-to-patch flow takes < 60s on stage |

### Non-goals (explicitly out of scope for MVP)
- Multi-file refactors or whole-repo context (v1 targets single-function/single-file fixes)
- Auto-applying patches without human review — this is a hard product principle, not a missing feature
- Cloud sync, teams, accounts, or any backend — the phone *is* the backend
- Support for languages beyond a small curated set at launch (start with Python + Kotlin + JS/TS, expand later)

---

## 3. Target Users / Personas

**Primary: "Priya," working professional, backend engineer**
Attends the hackathon as part of the Working Professionals bucket. Comfortable with git, IDEs, and code review. Wants tools that respect her time and don't require learning a new workflow.

**Secondary: "Rohan," CS student, hackathon regular**
Student bucket. Builds fast, cares about demo polish, is judged separately but on the same rubric. Wants something visually impressive and easy to explain in a 3-minute pitch.

**Tertiary (real-world, post-hackathon): any mobile-first or field-based developer**
Someone doing on-call debugging from a phone, a DevOps engineer diagnosing an issue during a commute, or a developer in a low-connectivity environment.

---

## 4. User Stories

1. *As a developer mid-Red-Light, I want to capture an error without typing it out, so that I don't waste time transcribing a stack trace by hand.*
2. *As a developer, I want to describe my intended fix in my own words, so that I don't have to write pseudo-code on a phone keyboard.*
3. *As a developer, I want to see a proposed diff — not have code silently changed — so that I stay in control of what ships.*
4. *As a developer with multiple bugs queued up, I want each patch tracked with a clear status, so that I don't lose track of what's ready vs. still drafting.*
5. *As a developer, I want the patch to appear automatically the moment I reconnect my laptop, so that I don't have to manually transfer anything.*
6. *As a judge, I want to see the phone and laptop demonstrably required in sequence, so that the "wow" is legible in a live demo, not just claimed in a pitch.*

---

## 5. Judging-Rubric Alignment (from the official iQOO Hackathon Guide)

| Rubric component | Weight | RelayPatch's answer |
|---|---|---|
| End Product Quality | 30% | Working capture → inference → diff-apply loop, demoable live |
| Novelty & Real-World Impact | 20% | No prior art found combining offline capture + voice intent + NPU code-diff generation + bridge-gated delivery (see prior research in project history) |
| Technical Depth | 15% | Real quantized LLM run on NPU, not an API wrapper; OCR + STT pipelines; diff generation and application logic |
| Demo & Live Pitch | 10% | Physically legible two-device demo: photograph → speak → walk to laptop → patch appears |
| Creative Phone Usage (telemetry) | 15% | Camera, mic, and NPU are the *core loop*, not bolted on |
| Office Kit Telemetry (telemetry) | 10% | The product's value proposition is unreachable without the bridge — maximizes intentional bridge usage |
| Bonus: local/open-source model at the core | uncapped | Gemma 3n int4, fully open-weight, on-device |

---

## 6. Scope — MVP vs. Stretch

### MVP (must work for the Saturday evening Checkpoint 1)
- [ ] Camera capture → on-device OCR of an error/stack trace
- [ ] Mic capture → on-device transcription of spoken fix intent
- [ ] On-device LLM drafts a single-file unified diff from (error text + fix intent + relevant code snippet, manually pasted for MVP)
- [ ] Patch queue with 3 states: Drafting → Ready → Synced
- [ ] Office Kit bridge sync of the patch text (clipboard-based is acceptable for MVP)
- [ ] VS Code companion: detect incoming patch, show as a notification, open a diff view

### Stretch (Sunday, if ahead of schedule)
- [ ] Auto-attach relevant code context by scanning an already-synced repo snapshot on the phone, instead of manual paste
- [ ] Multi-patch queue UI with swipe-to-reorder
- [ ] "Most iQOO Usage"-flavored polish: haptic feedback on patch-ready, Red/Green Light-themed UI states
- [ ] Support for 2 additional languages
- [ ] File-based sync (not just clipboard) for larger diffs via Office Kit's file transfer

### Explicitly not attempted in 30 hours
- Multi-file / cross-repo context
- Team accounts or shared queues
- iOS companion (VS Code + Android only)

---

## 7. Success Metrics (for the live demo, not production KPIs)

| Metric | Target |
|---|---|
| Time from photo capture to drafted patch | < 15 seconds on-device |
| Time from Office Kit reconnect to patch visible in VS Code | < 5 seconds |
| Diff correctness for a curated demo bug | Applies cleanly, compiles |
| Judges asking "wait, is that actually running on the phone?" | At least once, unprompted |

---

## 8. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| On-device model too slow for a live demo | Use the smallest viable quantized model (Gemma 3n E2B int4); pre-warm the model at app launch |
| Office Kit bridge behaves unpredictably under demo conditions | Have a clipboard-based fallback path that doesn't depend on file-transfer timing |
| OCR misreads a messy stack trace | Let the user manually correct the transcribed text before generating the patch |
| Judges don't understand the two-device flow without narration | Script the demo explicitly (see `DEMO_SCRIPT.md`) — narrate every hardware dependency out loud |
| Model produces a plausible-looking but wrong diff | Always show a diff review step; never auto-apply; frame this as a feature (trust), not a limitation |

---

## 9. Open Questions

- What's the actual public surface of the Office Kit SDK (clipboard vs. file API, connection-state callbacks)? Confirm at Saturday check-in with mentors.
- Is a VS Code extension acceptable as "the laptop side," or should we also support a plain desktop watcher for non-VS-Code users? (Decision: VS Code only for MVP — highest judge legibility, lowest build risk.)
- Should patch history persist across app restarts? (Decision: yes, Room DB survives restarts by default — no extra work needed.)

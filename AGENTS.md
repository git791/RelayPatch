# AGENTS.md — RelayPatch AI Coding Agent Protocol

**Audience:** Cursor, Claude Code, GitHub Copilot Pro, Windsurf, and any other AI coding agent operating on this repository.
**Precedence:** This file governs agent behavior. If it conflicts with an agent's own default heuristics, `AGENTS.md` wins. If it conflicts with an explicit human instruction in the current session, the human instruction wins for that session only — do not persist the override into this file without human sign-off.

---

## 1. Repository Map & Ownership Boundaries

| Path | Layer | Language | Agent may edit freely? |
|---|---|---|---|
| `/app/mobile/` | Android client | Kotlin, Jetpack Compose | Yes |
| `/app/mobile/inference/` | On-device NPU inference wrapper | Kotlin + LiteRT/MediaPipe bindings | **Caution** — see §4 |
| `/bridge/` | Office Kit bridge transport layer | Kotlin (mobile side) + TypeScript (desktop side) | **Caution** — see §5 |
| `/extension/` | VS Code extension | TypeScript | Yes |
| `/docs/` | This document set | Markdown | Ask before regenerating completed files (see README §2) |
| `/scripts/` | Build/CI/local tooling | Shell, Node | Yes |

**Never touch without explicit human request:** `google-services.json`, any `*.keystore` file, `local.properties`, `.env*` files. These either don't exist in this zero-network, zero-cloud-dependency project or are placeholders that must stay out of version control.

---

## 2. Coding Standards & Idioms

### 2.1 Kotlin / Jetpack Compose — State Hoisting Rules

- **Composables are stateless by default.** Any composable that owns mutable UI state (`remember { mutableStateOf(...) }`) must hoist that state to its caller unless the state is purely presentational and never read outside the composable's own recomposition scope (e.g., a ripple animation progress value).
- **Single source of truth**: patch lifecycle state (`DRAFTING → READY → SYNCED → APPLIED`) lives in the `PatchQueueViewModel` (`StateFlow<List<PatchUiState>>`), never duplicated in local `remember` blocks. Composables observe via `collectAsStateWithLifecycle()`, never `collectAsState()` (the latter doesn't respect lifecycle and will leak collectors during camera/mic-heavy screens).
- **Unidirectional data flow**: events flow up via lambda parameters (`onCapture: () -> Unit`, `onStageHunk: (hunkId: String) -> Unit`), state flows down via parameters. Do not pass ViewModel references into leaf composables — pass only the specific state and callbacks needed.
- **Side effects**: use `LaunchedEffect` keyed on the minimal set of dependencies that should retrigger it. Camera/mic permission requests must go through `rememberLauncherForActivityResult`, never manual `ActivityCompat.requestPermissions` calls inside composables.
- **Naming**: `XyzScreen` (stateful, ViewModel-aware) wraps `XyzContent` (stateless, previewable). Every `XyzContent` must have a `@Preview` with at least the `DRAFTING`, `READY`, and error states represented.
- **No business logic in Composables.** OCR post-processing, diff parsing, and NPU prompt construction belong in `domain/` or `data/` layers, injected via constructor (Hilt), never instantiated inline in a `@Composable`.

### 2.2 TypeScript — VS Code Extension Strict Typing

- `tsconfig.json` runs with `"strict": true`, `"noUncheckedIndexedAccess": true`, `"exactOptionalPropertyTypes": true`. Do not weaken these to silence errors — fix the underlying type instead.
- **No `any`.** If a VS Code API returns a loosely-typed value, define a narrow local interface and validate at the boundary (a `parseIncomingPatch(raw: unknown): Patch | ParseError` pattern, not a cast).
- All bridge-received payloads are treated as `unknown` until validated by a runtime schema check (lightweight hand-written type guards are acceptable given the zero-dependency infra constraint — do not add a schema-validation library like `zod` unless the human explicitly approves adding a new dependency).
- `WorkspaceEdit` construction must be atomic per patch: build the full `WorkspaceEdit` object, validate every hunk applies cleanly against current file content (`vscode.workspace.fs.stat` + content hash check), and only then call `vscode.workspace.applyEdit()`. Never apply hunks one at a time against a live document — partial application on failure is a data-loss bug class we explicitly guard against.
- Extension activation events must remain minimal (`onCommand:relaypatch.openDiff`, bridge-socket-listener registration) — do not add `"*"` or broad activation events; this is a battery/perf commitment tied to the "zero background cost when idle" product principle.

### 2.3 Zero-Network Security Assertion Policy

This is a **non-negotiable architectural invariant**, not a preference:

1. **No outbound network calls anywhere in the codebase**, on either the mobile app or the VS Code extension, except: (a) initial one-time model/asset download during setup (explicitly user-triggered, clearly labeled), (b) the Office Kit bridge transport itself, which is a local/direct device-to-device channel, not internet-routed.
2. Any agent-authored code that imports `fetch`, `axios`, `OkHttp`, `URLConnection`, `node:http`, `node:https`, or equivalent network primitives **must** be flagged in the PR/commit description with an explicit justification, and must not be merged without human review. Agents should treat adding such an import as equivalent to a breaking architectural change, not a routine code edit.
3. CI includes a static grep-based check (`/scripts/check-zero-network.sh`) that fails the build if new network-capable symbols appear outside the allow-listed bridge module. Agents must not modify or bypass this script's allow-list without an explicit human instruction citing the specific reason.
4. Telemetry, crash reporting, and analytics SDKs are **out of scope entirely** for this codebase. Do not add any, even "opt-in" or "anonymous" ones, even if asked to "add basic analytics" — flag this back to the human as a policy conflict rather than implementing it.

---

## 3. Prompt Templates & Context Injection

These are canonical prompts for common implementation tasks. Use them as-is or as a structural template; do not skip the "Context to load" step — agents that jump straight to code generation without loading the referenced files consistently produce architecture-inconsistent output on this repo.

### 3.1 Template: Add a language tokenizer to the diff parser

```
Context to load:
- /app/mobile/domain/diff/DiffParser.kt (current parser interface)
- /app/mobile/domain/diff/tokenizers/ (existing tokenizer implementations, if any)
- /docs/TECHNICAL_DOC.md, section "Diff Parsing & Syntax Highlighting"

Task:
Implement a lightweight tokenizer for <LANGUAGE> that classifies diff line content
into { KEYWORD, STRING, COMMENT, IDENTIFIER, PUNCTUATION, PLAIN } token spans, matching
the `LineToken` data class contract already used by existing tokenizers.

Constraints:
- Regex-based only; do not add a full parser/AST dependency (violates zero-cost-startup
  and small-APK-size goals for the hackathon build).
- Must run in <5ms for a 200-character line on a mid-tier NPU-class device (benchmark
  via the existing DiffTokenizerBenchmark.kt harness).
- Add unit tests in DiffParserTest.kt covering: single-line comments, multi-line string
  literals spanning a diff hunk boundary, and at least one pathological/edge-case input.

Deliverable: the tokenizer file + its test file + a one-line registration in
TokenizerRegistry.kt. Do not modify DiffInspector composables in this task.
```

### 3.2 Template: Implement the `BridgeTransport` interface

```
Context to load:
- /bridge/BridgeTransport.kt (interface definition — DO NOT change the interface
  signature without flagging it; other implementations depend on it)
- /bridge/officekit/OfficeKitTransport.kt (reference implementation, if present)
- /docs/TECHNICAL_DOC.md, section "Bridge Protocol & Handshake Sequence"

Task:
Implement `BridgeTransport` for <TARGET> using the Office Kit SDK's Clipboard and
File-transfer APIs.

Contract to satisfy:
    interface BridgeTransport {
        suspend fun discover(): Flow<BridgeState>   // Red Light / Green Light / Active
        suspend fun sendPatch(patch: PatchPayload): Result<SyncAck>
        suspend fun disconnect()
    }

Constraints:
- All state transitions must emit through the `Flow<BridgeState>` — no direct UI
  callbacks from this layer (violates unidirectional data flow, see §2.1).
- Handshake failures must produce a typed `BridgeError` (never a raw exception
  crossing the domain boundary) so the ViewModel can render a specific recovery UI.
- No network sockets — Office Kit local transport only (see §2.3, zero-network policy).
- Add a fake/in-memory `BridgeTransport` implementation for tests if one does not
  already exist at /bridge/test/FakeBridgeTransport.kt.

Deliverable: implementation file + updated DI binding (Hilt module) + tests.
```

### 3.3 Template: General bug fix / small feature (default template)

```
Context to load:
- The specific file(s) named in the task
- /docs/TECHNICAL_DOC.md relevant section (search by feature name first)
- /docs/AGENTS.md sections 2.1–2.3 (this file) for standards

Before editing:
1. State your understanding of the current behavior and the desired behavior in
   1-3 sentences.
2. Identify which architectural layer(s) this touches (UI / domain / data / bridge)
   and confirm no layer-boundary is being crossed improperly.

After editing:
- List every file changed and why, in the commit message body (not just the title).
- If the change touches inference (§4) or bridge (§5) code, explicitly note this
  in the PR description per the caution flags below.
```

---

## 4. Caution Zone: `/app/mobile/inference/`

This wraps the on-device NPU inference call (Gemma 3n E2B int4 via MediaPipe LLM Inference API / LiteRT with the Qualcomm QNN delegate). Agents may refactor call sites and error handling freely, but must **not**:

- Change quantization settings, delegate configuration, or model file references without an explicit human instruction — these are hand-tuned for the target device class and silent changes here can cause silent accuracy regressions that are hard to detect without on-device benchmarking.
- Add any fallback path that silently routes inference to a cloud API "for reliability" — this directly violates the project's zero-outbound-network constraint and its scoring strategy (bonus points are tied to local/open-source on-device inference).
- Remove or bypass the confidence-score gating that marks a `DRAFTING` patch as low-confidence (surfaces the "needs review" state in the UI per `FRONTEND_DESIGN.md` §4.1) — this is a trust-safety feature, not a cosmetic one.

## 5. Caution Zone: `/bridge/`

The bridge is the highest-stakes integration point (it is directly scored via Office Kit Telemetry, 10% of the automated rubric). Agents must:

- Preserve the exact handshake sequence documented in `TECHNICAL_DOC.md` — jury demo reliability depends on this being deterministic and fast.
- Never introduce a polling loop shorter than the documented interval without justification (battery/perf regression risk on a hackathon demo device that needs to survive a multi-hour event).
- Treat any change to the wire format between `OfficeKitTransport.kt` (mobile) and the extension's receiver (`extension/src/bridge/receiver.ts`) as a breaking change requiring updates on **both** sides in the same commit/PR — never merge a one-sided protocol change.

---

## 6. Testing Expectations for Agent-Authored Code

- Every new `domain/` or `bridge/` function requires a corresponding unit test in the same PR — no exceptions, no "will add tests later" comments.
- Composables require a `@Preview` covering at minimum the default and error/empty states; interactive composables with multiple lifecycle states (Patch Queue Card, Connection Status Pill) require previews for **every** documented state in `FRONTEND_DESIGN.md` §4.
- Agents must run the existing test suite locally (or via the CI script) before declaring a task complete, and must report any pre-existing failing tests they encounter rather than silently working around them.

## 7. Communication Norms for Agents

- When a task is ambiguous or touches a Caution Zone (§4, §5), state the ambiguity and proposed default explicitly rather than guessing silently — this codebase is small enough that a wrong silent guess costs more than a clarifying question.
- Do not regenerate `README.md`, `PRD.md`, or `TECHNICAL_DOC.md` (see `README.md` §2, "Completed Files"). If a task seems to require changing product scope or architecture described in those files, flag the conflict instead of editing them unprompted.
- Commit messages: `<layer>: <imperative summary>` (e.g., `bridge: fix handshake timeout on cold start`). Body explains *why*, not just *what* — the diff already shows what.

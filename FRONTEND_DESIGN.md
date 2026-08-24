# RelayPatch — Frontend Design Specification

**Doc status:** Production-ready · **Scope:** Mobile app (Jetpack Compose, Material 3 Expressive) + VS Code extension companion UI
**Related docs:** `README.md`, `PRD.md`, `TECHNICAL_DOC.md`, `AGENTS.md`

---

## 1. Design Philosophy

RelayPatch's UI sits at the intersection of two personas: a developer working heads-down under time pressure (hackathon Red Light lockout), and the same developer moments later at a laptop expecting a familiar IDE-grade diff experience. The design philosophy is built on three pillars:

1. **Material 3 Expressive as the mobile substrate.** Dynamic color, expressive shape morphing, and spring-based motion give the capture-to-patch loop a sense of liveliness that plain M3 lacks — important because the phone flow (camera → voice → NPU inference) has several seconds of latent processing that needs to *feel* alive, not frozen.
2. **Developer Dark Mode as the default surface.** The app assumes a dark, low-glare environment (hackathon floor, dim rooms) and a code-literate audience. Dark is not an alternate theme; it is the primary theme, with light mode as the accessibility fallback.
3. **State transparency over decoration.** Every screen must make the bridge state (Red Light / Green Light / Bridge Active) and every patch's lifecycle state instantly legible at a glance, since the core value proposition is "know exactly where your fix is in the pipeline."

---

## 2. Color System

### 2.1 Core Semantic Tokens (Dark — default)

| Token | Hex | Usage |
|---|---|---|
| `md.sys.color.primary` | `#8FD3A8` | Primary actions, active nav, brand accent (muted mint-green, evokes "patch applied") |
| `md.sys.color.on-primary` | `#00391A` | Text/icons on primary |
| `md.sys.color.primary-container` | `#1E5033` | Filled buttons, selected chips |
| `md.sys.color.on-primary-container` | `#ABF2C1` | Text on primary container |
| `md.sys.color.secondary` | `#8FCBE0` | Secondary actions, voice/OCR affordances |
| `md.sys.color.on-secondary` | `#00344A` | Text on secondary |
| `md.sys.color.secondary-container` | `#1B4A60` | Capture HUD chrome |
| `md.sys.color.surface` | `#101416` | App background |
| `md.sys.color.surface-dim` | `#0A0D0E` | Deepest background (camera overlay backdrop) |
| `md.sys.color.surface-bright` | `#363A3C` | Elevated sheets, modals |
| `md.sys.color.surface-container-low` | `#181C1E` | Cards at rest |
| `md.sys.color.surface-container` | `#1C2022` | Cards, list rows |
| `md.sys.color.surface-container-high` | `#262A2C` | Dialogs, bottom sheets |
| `md.sys.color.on-surface` | `#E1E3E3` | Primary text |
| `md.sys.color.on-surface-variant` | `#A9ADAE` | Secondary text, hints |
| `md.sys.color.outline` | `#767979` | Dividers, input borders |
| `md.sys.color.error` | `#FFB4A9` | Destructive/error states |
| `md.sys.color.on-error` | `#680003` | Text on error |

### 2.2 Product-Specific Tokens

| Token | Hex | Usage | Contrast vs. Surface (`#101416`) |
|---|---|---|---|
| `diff.addition.bg` | `#173928` | Line background for added code | — |
| `diff.addition.fg` | `#7EE2A8` | Added line text/gutter `+` | 9.8:1 |
| `diff.deletion.bg` | `#3A1B1E` | Line background for removed code | — |
| `diff.deletion.fg` | `#F29B96` | Removed line text/gutter `-` | 8.1:1 |
| `diff.context.fg` | `#B7BABA` | Unchanged context lines | 7.4:1 |
| `status.redlight.bg` | `#4A1418` | Red Light badge background | — |
| `status.redlight.fg` | `#FF8A80` | Red Light badge text/icon | 7.9:1 |
| `status.greenlight.bg` | `#123321` | Green Light badge background | — |
| `status.greenlight.fg` | `#6FDB94` | Green Light badge text/icon | 9.1:1 |
| `status.bridgeactive.bg` | `#0F2E3E` | Bridge Active pulse background | — |
| `status.bridgeactive.fg` | `#7FD4F2` | Bridge Active text/icon | 8.6:1 |
| `status.neutral.bg` | `#232629` | Disconnected/idle badge background | — |
| `status.neutral.fg` | `#C4C7C7` | Disconnected/idle badge text | 8.3:1 |

### 2.3 Light Mode Overrides (Accessibility Fallback)

| Token | Hex |
|---|---|
| `md.sys.color.surface` | `#FAFDF9` |
| `md.sys.color.on-surface` | `#191C1B` |
| `md.sys.color.primary` | `#1E6B3D` |
| `diff.addition.bg` | `#DCF5E4` |
| `diff.addition.fg` | `#0F5C2C` |
| `diff.deletion.bg` | `#FBE1DE` |
| `diff.deletion.fg` | `#9E2418` |

### 2.4 Accessibility Compliance

- All body-text token pairs meet **WCAG 2.1 AA** (≥4.5:1) at 14sp+; status badges and diff gutters meet **AAA** (≥7:1) since they carry safety-critical meaning (do-not-lose-this-patch signaling).
- Color is never the sole differentiator: diff additions carry a `+` glyph and left border stripe (3dp, `diff.addition.fg`); deletions carry `-` and strikethrough-free border stripe in `diff.deletion.fg`.
- Status badges pair color with an icon (Red Light = filled circle + lock glyph; Green Light = filled circle + link glyph; Bridge Active = pulsing ring + waveform glyph) so colorblind users (deuteranopia/protanopia, ~8% of male users) retain full legibility.
- Minimum touch target: 48×48dp for all interactive elements, including diff line "stage/unstage" toggles.

---

## 3. Typography

### 3.1 Type Families

| Role | Family | Rationale |
|---|---|---|
| System UI (Sans) | **Roboto Flex** (variable) | Native M3 Expressive default; variable width/weight enables expressive headline motion |
| Code / Diff | **JetBrains Mono** (fallback: `Roboto Mono`) | Ligature-free, disambiguates `0/O`, `1/l/I` — critical for reading OCR'd + AI-patched code under time pressure |

### 3.2 Modular Scale (base 14sp, ratio 1.2 minor third, rounded to nearest sp)

| Style | Size / Line-height | Weight | Family | Usage |
|---|---|---|---|---|
| Display Large | 40sp / 48sp | 400 | Roboto Flex | Onboarding hero only |
| Headline Large | 28sp / 36sp | 500 | Roboto Flex | Screen titles ("Patch Queue") |
| Headline Small | 22sp / 28sp | 500 | Roboto Flex | Section headers |
| Title Large | 18sp / 24sp | 500 | Roboto Flex | Card titles, dialog titles |
| Title Medium | 16sp / 22sp | 500 | Roboto Flex | List item titles |
| Body Large | 16sp / 24sp | 400 | Roboto Flex | Primary reading text |
| Body Medium | 14sp / 20sp | 400 | Roboto Flex | Secondary text, descriptions |
| Label Large | 14sp / 20sp | 500 | Roboto Flex | Button labels |
| Label Small | 11sp / 16sp | 500 | Roboto Flex | Badges, timestamps |
| Code Block | 13sp / 20sp | 400 | JetBrains Mono | Diff body text |
| Code Inline | 13sp / 18sp | 500 | JetBrains Mono | Inline code refs in prose |
| Code Caption | 11sp / 16sp | 400 | JetBrains Mono | File paths, line numbers, hashes |

### 3.3 Hierarchy Rules

- Never exceed **3 type roles** per screen region (e.g., a card uses Title Medium + Body Medium + Label Small — no more).
- Diff content always renders in JetBrains Mono regardless of surrounding sans context, with a 4dp visual gutter separating it from sans UI chrome.
- Monospace numerals (`tnum`) are forced on for line numbers and timestamps to prevent layout jitter during live updates (e.g., patch generation progress counters).

---

## 4. Component Specs

### 4.1 Patch Queue Card

A `Card` (M3 Expressive, `surface-container` elevation 1, 16dp corner radius, morphs to 24dp on press) representing one captured issue → patch lifecycle.

**States & visual treatment:**

| State | Badge | Badge Color Token | Leading Icon | Behavior |
|---|---|---|---|---|
| `DRAFTING` | "Drafting…" | `status.neutral.bg/fg` + animated shimmer | Pulsing NPU chip icon | Indeterminate progress bar under title; card is non-tappable except for cancel |
| `READY` | "Ready to Sync" | `status.redlight.bg/fg` (still local-only) | Diff/patch icon, solid | Shows patch summary (files changed, +/- line counts); primary CTA "Preview" |
| `SYNCED` | "Synced" | `status.bridgeactive.bg/fg` | Bridge/link icon | Shows target repo + branch; secondary CTA "View in VS Code" (deep-link handoff) |
| `APPLIED` | "Applied ✓" | `status.greenlight.bg/fg` | Checkmark, filled | Card collapses to compact 1-line summary row; swipe to archive |

**Anatomy (top to bottom):**
1. Header row: source thumbnail (72×72dp OCR capture crop, rounded 8dp) + title (auto-generated from error text, Title Medium) + state badge (top-right, pill shape)
2. Metadata row: file path (Code Caption), timestamp (Label Small), diff stat chip (`+12 -4` in split addition/deletion colors)
3. Footer: primary action button (state-dependent) + overflow menu (retry, discard, view raw OCR/transcript)

Card height: 128dp collapsed / 96dp compact (APPLIED). Spacing between cards: 8dp. List uses `LazyColumn` with `animateItemPlacement` for state-transition reflows.

### 4.2 Capture Overlay HUD

Full-bleed camera viewport (`CameraX PreviewView`) with layered overlay, edge-to-edge, `surface-dim` backdrop bleeding 64dp from top/bottom for control legibility.

**Layers (back to front):**
1. **Live camera preview** — full screen.
2. **OCR bounding-box overlay** — Canvas-drawn rectangles around detected text regions from ML Kit Text Recognition v2, stroke 2dp in `secondary` token, corner accent brackets (M3 Expressive "focus corners" motif) at 8dp radius. Boxes fade in 150ms after detection stabilizes for ≥2 consecutive frames (debounce to avoid jitter).
3. **Capture guidance banner** (top, `surface-container-high` @ 90% opacity, 8dp corner): contextual text e.g. "Align error message in frame" → "Text detected — hold steady."
4. **Waveform audio dictation indicator** (bottom third, appears after photo capture, during voice dictation step): 24 vertical bars, amplitude-reactive, `secondary` token, centered above a large circular mic button (72dp, `primary-container` fill, pulses radius ±4dp on speech onset).
5. **Bottom action bar**: shutter button (80dp, M3 Expressive squircle-morphing FAB — circle at rest, morphs to rounded-square while processing), flash toggle, gallery-import fallback icon.

**Transition:** Shutter press triggers a 200ms flash-mask + haptic (see §5), then the frame freezes with a subtle 4px Gaussian blur behind the bounding-box confirmation step before advancing to voice dictation.

### 4.3 Diff Inspector

A dedicated full-screen composable for reviewing the NPU-generated unified diff before sync.

**Layout:**
- **App bar**: back arrow, file name (Title Medium, Code font for the actual filename), overflow (copy diff, discard, regenerate).
- **Diff stat header**: sticky, `surface-container-low`, shows `filename.ext`, `+N -M` stat chip, and a segmented toggle: **Unified / Side-by-side** (side-by-side only enabled ≥600dp width, i.e., foldables/tablets).
- **Diff body**: `LazyColumn` of `DiffLineRow`:
  - Gutter: line number (old | new, Code Caption, right-aligned, 32dp each) + change glyph (`+`/`-`/` `).
  - Content: syntax-tokenized code (basic client-side highlighting via a lightweight regex tokenizer for keywords/strings/comments — full LSP highlighting is out of scope for MVP).
  - Row background: `diff.addition.bg` / `diff.deletion.bg` / transparent for context, with a 3dp left border stripe in the corresponding `fg` token.
  - Long lines: horizontal scroll within the row (not full-screen), with a persistent thin scrollbar indicator.
- **Bottom action bar** (sticky, `surface-container-high`, elevation 3): "Regenerate" (secondary, outlined) · "Stage & Sync via Bridge" (primary, filled, disabled until Bridge Active).

**Interaction:** Tapping a line's gutter toggles that hunk between staged/unstaged (checkbox affordance appears on tap, 200ms fade); unstaged hunks dim to 40% opacity but remain visible for context (never hidden — trust/transparency principle).

### 4.4 Connection Status Pill

Persistent, non-dismissible pill anchored top-right of every screen's app bar (or a floating pill on full-bleed screens like the Capture HUD, offset to avoid camera controls).

| State | Label | Token | Motion |
|---|---|---|---|
| Red Light (no bridge) | "Offline" | `status.redlight.bg/fg` | Static, no animation — deliberately calm since this is the expected default state |
| Searching | "Looking for bridge…" | `status.neutral.bg/fg` | 3-dot loading ellipsis, 900ms cycle |
| Green Light (bridge found, handshaking) | "Connecting…" | `status.greenlight.bg/fg` @ 70% opacity | Soft pulse, 1.2s cycle |
| Bridge Active | "Bridge Active" | `status.bridgeactive.bg/fg` | Slow radial pulse ring behind icon, 2s cycle, respects `prefers-reduced-motion` (falls back to static icon + subtle 8% opacity breathe) |

Pill shape: fully rounded (999dp radius), 32dp height, 12dp horizontal padding, 16dp leading icon. Tapping expands it into a small popover with bridge device name, transfer protocol, and last-sync timestamp.

---

## 5. Motion & Haptics

### 5.1 Motion Principles

- **Spring-based, not duration-based**: all M3 Expressive transitions use spring specs (`dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium` as default), not fixed-duration eases, so motion feels responsive to input velocity (e.g., card swipe-to-archive).
- **Shape morphing** is reserved for state-defining moments: shutter FAB (circle → squircle during processing → checkmark burst on capture success), and the Patch Queue Card badge (pill morphs width/color across state transitions rather than cross-fading, reinforcing continuity of the *same* patch object).
- **Reduced motion**: all pulse/shimmer/morph animations degrade to opacity-only or static-state changes when the system `Reduce Motion` accessibility setting is on.

### 5.2 Haptic Feedback Profiles

| Event | Haptic Pattern (Android `VibrationEffect`) | Rationale |
|---|---|---|
| Photo captured (OCR frame locked) | Single `EFFECT_CLICK` (short, ~20ms) | Standard shutter confirmation |
| Voice dictation started | `EFFECT_TICK` (very light) | Non-intrusive cue that mic is live |
| **NPU patch generation complete** | Double-tap pattern: `EFFECT_HEAVY_CLICK` + 80ms pause + `EFFECT_CLICK` | Distinct, satisfying "something was made" signal — this is the app's signature haptic, deliberately different from any system default so it becomes a recognizable brand cue over a demo |
| Patch generation failed / low confidence | `EFFECT_DOUBLE_CLICK` at low amplitude | Gentle "needs attention" without alarm |
| **Bridge handshake success (Green Light achieved)** | Rising pattern: three ticks of increasing amplitude (20ms/40ms/60ms, 60ms gaps) | Mimics a "connection completing" feeling, reinforces the physical-world moment of walking back to the laptop |
| Sync complete (patch delivered to VS Code) | Single `EFFECT_HEAVY_CLICK` | Weighty confirmation, mirrors "file saved" muscle memory |
| Swipe-to-archive threshold crossed | `EFFECT_TICK` at threshold, then `EFFECT_CLICK` on release | Standard M3 swipe-action confirmation pattern |

All haptics respect system-level "vibrate on touch" settings and degrade silently (no crash, no fallback sound) on devices/emulators without a vibrator.

---

## 6. Responsive & Foldable Considerations (OriginOS 6 / Flip & Fold Devices)

- **Diff Inspector** switches to side-by-side layout automatically at width ≥600dp (unfolded state), matching VS Code's own diff view for visual continuity between phone and laptop.
- **Capture Overlay HUD** on cover-screen/flip form factors uses a condensed single-column control layout (shutter only, no flash toggle) below 360dp width.
- All spacing uses `WindowSizeClass` breakpoints (Compact / Medium / Expanded) per M3 guidance rather than fixed dp breakpoints, ensuring OriginOS 6 multi-window and split-screen modes degrade gracefully.

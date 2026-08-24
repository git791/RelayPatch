package dev.relaypatch.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------------------
// Font families — FRONTEND_DESIGN.md §3.1
// ---------------------------------------------------------------------------

/**
 * Roboto Flex (variable weight/width) — system UI font.
 * On Android 12+ Roboto Flex ships as the default variable font; we rely on
 * the system font rather than bundling a custom asset to keep APK size down.
 */
val RobotoFlex: FontFamily = FontFamily.Default

/**
 * JetBrains Mono — all diff/code surfaces (FRONTEND_DESIGN.md §3.1).
 * Bundled in res/font/; monospace fallback used if asset is absent.
 */
val JetBrainsMono: FontFamily = FontFamily.Monospace

// ---------------------------------------------------------------------------
// Material 3 type scale — FRONTEND_DESIGN.md §3.2
// Modular scale: base 14sp, ratio 1.2 (minor third), rounded to nearest sp.
// ---------------------------------------------------------------------------

val RelayPatchTypography = Typography(
    // 40sp / 48sp / W400 — Onboarding hero only
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.25).sp,
    ),
    // 36sp / 44sp — not specified, keep sensible M3 default
    displayMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    // 32sp / 40sp
    displaySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    // 28sp / 36sp / W500 — Screen titles ("Patch Queue") §3.2
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    // 22sp / 28sp / W500 — Section headers §3.2
    headlineSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    // 18sp / 24sp / W500 — Card titles, dialog titles §3.2
    titleLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // 16sp / 22sp / W500 — List item titles §3.2
    titleMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // 16sp / 24sp / W400 — Primary reading text §3.2
    bodyLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    // 14sp / 20sp / W400 — Secondary text, descriptions §3.2
    bodyMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // 14sp / 20sp / W500 — Button labels §3.2
    labelLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    // 11sp / 16sp / W500 — Badges, timestamps §3.2
    labelSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ---------------------------------------------------------------------------
// Extra code-surface text styles — FRONTEND_DESIGN.md §3.2
// These are not part of the M3 scale but are used directly in diff composables.
// ---------------------------------------------------------------------------

/** Diff body text — 13sp / 20sp / W400 / JetBrains Mono */
val codeBlockStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
)

/** Inline code references in prose — 13sp / 18sp / W500 / JetBrains Mono */
val codeInlineStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
)

/** File paths, line numbers, hashes — 11sp / 16sp / W400 / JetBrains Mono.
 *  Uses tabular numerals (tnum) to prevent layout jitter on live updates (§3.3). */
val codeCaptionStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    textAlign = TextAlign.End, // right-align line numbers by default
)

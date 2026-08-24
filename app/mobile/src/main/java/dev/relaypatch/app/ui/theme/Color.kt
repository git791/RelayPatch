package dev.relaypatch.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Material 3 system color tokens — Dark (primary / default) theme
// §2.1 of FRONTEND_DESIGN.md
// ---------------------------------------------------------------------------

val Primary = Color(0xFF8FD3A8)
val OnPrimary = Color(0xFF00391A)
val PrimaryContainer = Color(0xFF1E5033)
val OnPrimaryContainer = Color(0xFFABF2C1)

val Secondary = Color(0xFF8FCBE0)
val OnSecondary = Color(0xFF00344A)
val SecondaryContainer = Color(0xFF1B4A60)
val OnSecondaryContainer = Color(0xFFBEE8FF)

val Tertiary = Color(0xFFB5C4E8)
val OnTertiary = Color(0xFF1E2E4D)
val TertiaryContainer = Color(0xFF354565)
val OnTertiaryContainer = Color(0xFFD8E2FF)

val Error = Color(0xFFFFB4A9)
val OnError = Color(0xFF680003)
val ErrorContainer = Color(0xFF930006)
val OnErrorContainer = Color(0xFFFFDAD4)

// Surface family
val Surface = Color(0xFF101416)
val SurfaceDim = Color(0xFF0A0D0E)
val SurfaceBright = Color(0xFF363A3C)
val SurfaceContainerLowest = Color(0xFF0D1112)
val SurfaceContainerLow = Color(0xFF181C1E)
val SurfaceContainer = Color(0xFF1C2022)
val SurfaceContainerHigh = Color(0xFF262A2C)
val SurfaceContainerHighest = Color(0xFF313535)

val OnSurface = Color(0xFFE1E3E3)
val OnSurfaceVariant = Color(0xFFA9ADAE)
val Outline = Color(0xFF767979)
val OutlineVariant = Color(0xFF3E4345)
val InverseSurface = Color(0xFFE1E3E3)
val InverseOnSurface = Color(0xFF2E3132)
val InversePrimary = Color(0xFF006D39)

val Scrim = Color(0xFF000000)
val Shadow = Color(0xFF000000)

// ---------------------------------------------------------------------------
// Material 3 system color tokens — Light (accessibility fallback)
// §2.3 of FRONTEND_DESIGN.md
// ---------------------------------------------------------------------------

val PrimaryLight = Color(0xFF1E6B3D)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFA8F2C1)
val OnPrimaryContainerLight = Color(0xFF002110)

val SecondaryLight = Color(0xFF1A6C8C)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFBDE8FF)
val OnSecondaryContainerLight = Color(0xFF00212F)

val TertiaryLight = Color(0xFF4A5E8A)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFD8E2FF)
val OnTertiaryContainerLight = Color(0xFF03174A)

val ErrorLight = Color(0xFFBA1B1B)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD4)
val OnErrorContainerLight = Color(0xFF410001)

val SurfaceLight = Color(0xFFFAFDF9)
val SurfaceDimLight = Color(0xFFD9DDD9)
val SurfaceBrightLight = Color(0xFFFAFDF9)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF3F7F3)
val SurfaceContainerLight = Color(0xFFEDF1ED)
val SurfaceContainerHighLight = Color(0xFFE7EBE7)
val SurfaceContainerHighestLight = Color(0xFFE2E5E2)

val OnSurfaceLight = Color(0xFF191C1B)
val OnSurfaceVariantLight = Color(0xFF3F4946)
val OutlineLight = Color(0xFF6F7975)
val OutlineVariantLight = Color(0xFFBEC9C4)
val InverseSurfaceLight = Color(0xFF2E3130)
val InverseOnSurfaceLight = Color(0xFFEFF1F0)
val InversePrimaryLight = Color(0xFF8FD3A8)

// ---------------------------------------------------------------------------
// Product-specific tokens — §2.2 of FRONTEND_DESIGN.md
// ---------------------------------------------------------------------------

// Diff viewer
val DiffAdditionBg = Color(0xFF173928)
val DiffAdditionFg = Color(0xFF7EE2A8)
val DiffDeletionBg = Color(0xFF3A1B1E)
val DiffDeletionFg = Color(0xFFF29B96)
val DiffContextFg = Color(0xFFB7BABA)

// Light-mode diff overrides
val DiffAdditionBgLight = Color(0xFFDCF5E4)
val DiffAdditionFgLight = Color(0xFF0F5C2C)
val DiffDeletionBgLight = Color(0xFFFBE1DE)
val DiffDeletionFgLight = Color(0xFF9E2418)

// Status — Red Light (no bridge / offline)
val StatusRedLightBg = Color(0xFF4A1418)
val StatusRedLightFg = Color(0xFFFF8A80)

// Status — Green Light (bridge found, handshaking)
val StatusGreenLightBg = Color(0xFF123321)
val StatusGreenLightFg = Color(0xFF6FDB94)

// Status — Bridge Active
val StatusBridgeActiveBg = Color(0xFF0F2E3E)
val StatusBridgeActiveFg = Color(0xFF7FD4F2)

// Status — Neutral (disconnected / idle / searching)
val StatusNeutralBg = Color(0xFF232629)
val StatusNeutralFg = Color(0xFFC4C7C7)

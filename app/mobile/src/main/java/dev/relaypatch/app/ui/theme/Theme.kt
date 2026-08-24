package dev.relaypatch.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// Dark color scheme — the PRIMARY theme (FRONTEND_DESIGN.md §1, §2.1)
// "Dark is not an alternate theme; it is the primary theme."
// ---------------------------------------------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,

    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    outline = Outline,
    outlineVariant = OutlineVariant,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,

    scrim = Scrim,
)

// ---------------------------------------------------------------------------
// Light color scheme — accessibility fallback only (FRONTEND_DESIGN.md §2.3)
// ---------------------------------------------------------------------------

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,

    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,

    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,

    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,

    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
)

// ---------------------------------------------------------------------------
// RelayPatchTheme
// ---------------------------------------------------------------------------

/**
 * Top-level theme composable.
 *
 * Always defaults to the dark color scheme (FRONTEND_DESIGN.md §1 — "Dark is
 * the primary theme"). Light is provided as an accessibility fallback only and
 * is only applied when [forceLightTheme] is explicitly true.
 *
 * @param forceLightTheme Override to force the light scheme (accessibility
 *   testing, edge-lit environments). Not tied to system dark-mode preference —
 *   we intentionally ignore [isSystemInDarkTheme] for this app.
 */
@Composable
fun RelayPatchTheme(
    forceLightTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (forceLightTheme) LightColorScheme else DarkColorScheme

    // Update status-bar / navigation-bar appearance to match dark-primary surface.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                forceLightTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RelayPatchTypography,
        shapes = RelayPatchShapes,
        content = content,
    )
}

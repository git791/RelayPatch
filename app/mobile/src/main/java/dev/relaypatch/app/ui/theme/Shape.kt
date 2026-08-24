package dev.relaypatch.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale for RelayPatch.
 *
 * Design spec (FRONTEND_DESIGN.md §4.1):
 *  - Card corner:   16dp at rest, morphs to 24dp on press
 *  - Dialog corner: 24dp
 *  - Pill/badge:    999dp (fully rounded — handled inline via CircleShape where needed)
 */
val RelayPatchShapes = Shapes(
    // XSmall — used for chips, small indicators
    extraSmall = RoundedCornerShape(4.dp),
    // Small — used for text fields, compact elements
    small = RoundedCornerShape(8.dp),
    // Medium — default card shape (16dp per spec)
    medium = RoundedCornerShape(16.dp),
    // Large — dialogs, bottom sheets (24dp per spec)
    large = RoundedCornerShape(24.dp),
    // XLarge — full-bleed sheets
    extraLarge = RoundedCornerShape(28.dp),
)

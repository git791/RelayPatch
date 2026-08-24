package dev.relaypatch.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.relaypatch.app.bridge.BridgeState
import dev.relaypatch.app.ui.theme.RelayPatchTheme
import dev.relaypatch.app.ui.theme.StatusBridgeActiveBg
import dev.relaypatch.app.ui.theme.StatusBridgeActiveFg
import dev.relaypatch.app.ui.theme.StatusGreenLightBg
import dev.relaypatch.app.ui.theme.StatusGreenLightFg
import dev.relaypatch.app.ui.theme.StatusNeutralBg
import dev.relaypatch.app.ui.theme.StatusNeutralFg
import dev.relaypatch.app.ui.theme.StatusRedLightBg
import dev.relaypatch.app.ui.theme.StatusRedLightFg

// ---------------------------------------------------------------------------
// ConnectionStatusPill
// FRONTEND_DESIGN.md §4.4
// ---------------------------------------------------------------------------

/**
 * Persistent, non-dismissible status pill anchored top-right of every app bar.
 *
 * Stateless — all observable state comes through [state].
 * Motion respects Android's system "Reduce Motion" setting implicitly because
 * [rememberInfiniteTransition] animations still run but at reduced spec when
 * the system accessibility animation scale is 0; for the pulse ring we also
 * gate on [LocalInspectionMode] to keep Previews static.
 *
 * Shape: fully-rounded (999dp radius), 32dp height, 12dp horizontal padding,
 * 16dp leading icon — per FRONTEND_DESIGN.md §4.4.
 */
@Composable
fun ConnectionStatusPill(
    state: BridgeState,
    modifier: Modifier = Modifier,
) {
    val isPreview = LocalInspectionMode.current

    // Resolve visual tokens from the current bridge state
    val bg: Color
    val fg: Color
    val label: String
    val icon: ImageVector

    when (state) {
        BridgeState.RedLight -> {
            bg = StatusRedLightBg
            fg = StatusRedLightFg
            label = "Offline"
            icon = Icons.Filled.Lock
        }
        BridgeState.Searching -> {
            bg = StatusNeutralBg
            fg = StatusNeutralFg
            label = "Looking for bridge"
            icon = Icons.Filled.Search
        }
        BridgeState.Connecting -> {
            bg = StatusGreenLightBg
            fg = StatusGreenLightFg
            label = "Connecting…"
            icon = Icons.Filled.Link
        }
        is BridgeState.Active -> {
            bg = StatusBridgeActiveBg
            fg = StatusBridgeActiveFg
            label = "Bridge Active"
            icon = Icons.Filled.Wifi
        }
    }

    // Animate background/foreground colour transitions via spring
    val animBg by animateColorAsState(
        targetValue = bg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pill_bg",
    )
    val animFg by animateColorAsState(
        targetValue = fg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pill_fg",
    )

    // Searching — 3-dot ellipsis (900 ms cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "pill_infinite")
    val dotPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dot_phase",
    )

    // Connecting — soft alpha pulse (1.2 s cycle)
    val connectingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "connecting_alpha",
    )

    // Active — radial pulse ring scale (2 s cycle)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse_scale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse_alpha",
    )

    val containerAlpha = when (state) {
        BridgeState.Connecting -> if (isPreview) 0.70f else connectingAlpha
        else -> 1.0f
    }

    val isActive = state is BridgeState.Active

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(animBg.copy(alpha = animBg.alpha * containerAlpha))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Pulse ring behind icon for Active state
            Box(contentAlignment = Alignment.Center) {
                if (isActive && !isPreview) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(pulseScale)
                            .drawBehind {
                                drawCircle(
                                    color = animFg.copy(alpha = pulseAlpha),
                                    radius = size.minDimension / 2f,
                                )
                            },
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animFg,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.width(6.dp))

            // Label — append animated ellipsis dots for Searching state
            val displayLabel = when {
                state == BridgeState.Searching && !isPreview -> {
                    val dots = ".".repeat(dotPhase.toInt().coerceIn(0, 3))
                    "$label$dots"
                }
                state == BridgeState.Searching && isPreview -> "$label…"
                else -> label
            }

            Text(
                text = displayLabel,
                color = animFg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Previews — one per documented state (AGENTS.md §6)
// ---------------------------------------------------------------------------

@Preview(name = "Pill — RedLight", showBackground = true, backgroundColor = 0xFF101416)
@Composable
private fun PreviewRedLight() {
    RelayPatchTheme {
        ConnectionStatusPill(state = BridgeState.RedLight)
    }
}

@Preview(name = "Pill — Searching", showBackground = true, backgroundColor = 0xFF101416)
@Composable
private fun PreviewSearching() {
    RelayPatchTheme {
        ConnectionStatusPill(state = BridgeState.Searching)
    }
}

@Preview(name = "Pill — Connecting", showBackground = true, backgroundColor = 0xFF101416)
@Composable
private fun PreviewConnecting() {
    RelayPatchTheme {
        ConnectionStatusPill(state = BridgeState.Connecting)
    }
}

@Preview(name = "Pill — Active", showBackground = true, backgroundColor = 0xFF101416)
@Composable
private fun PreviewActive() {
    RelayPatchTheme {
        ConnectionStatusPill(state = BridgeState.Active(deviceName = "MacBook Pro", protocol = "clipboard"))
    }
}

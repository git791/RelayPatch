package dev.relaypatch.app.ui.screens.queue

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.relaypatch.app.bridge.BridgeState
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.data.model.PatchStatus
import dev.relaypatch.app.ui.viewmodels.PatchQueueViewModel

@Composable
fun PatchQueueScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDiff: (String) -> Unit = {},
    viewModel: PatchQueueViewModel = hiltViewModel()
) {
    // AGENTS.md §2.1: collectAsStateWithLifecycle for lifecycle-aware state flow collection
    val patches by viewModel.patches.collectAsStateWithLifecycle()
    val bridgeState by viewModel.bridgeState.collectAsStateWithLifecycle()

    // Filter out discarded patches for the UI
    val visiblePatches = patches.filter { it.status != PatchStatus.DISCARDED }

    // Auto-trigger inference for any new DRAFTING patches
    LaunchedEffect(visiblePatches) {
        visiblePatches.filter { it.status == PatchStatus.DRAFTING }.forEach { patch ->
            viewModel.startInferenceForPatch(patch)
        }
    }

    PatchQueueContent(
        patches = visiblePatches,
        bridgeState = bridgeState,
        onDiscard = viewModel::discardPatch,
        onApply = viewModel::applyPatch,
        onNavigateToDiff = onNavigateToDiff,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatchQueueContent(
    patches: List<Patch>,
    bridgeState: BridgeState,
    onDiscard: (String) -> Unit,
    onApply: (Patch) -> Unit,
    onNavigateToDiff: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patch Queue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    BridgeConnectionPill(bridgeState)
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        if (patches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No patches in queue.\nGo capture a fix!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(patches, key = { it.id }) { patch ->
                    PatchCard(
                        patch = patch,
                        onDiscard = { onDiscard(patch.id) },
                        onApply = { onApply(patch) },
                        onClick = { onNavigateToDiff(patch.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BridgeConnectionPill(state: BridgeState) {
    val (color, text) = when (state) {
        is BridgeState.RedLight -> MaterialTheme.colorScheme.error to "Disconnected"
        is BridgeState.Searching -> MaterialTheme.colorScheme.tertiary to "Searching..."
        is BridgeState.Connecting -> MaterialTheme.colorScheme.secondary to "Pairing..."
        is BridgeState.Active -> Color(0xFF3DDC84) to "Connected" // Android Green
    }

    val animatedColor by animateColorAsState(targetValue = color, label = "pill_color")

    Surface(
        color = animatedColor.copy(alpha = 0.2f),
        contentColor = animatedColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(animatedColor, androidx.compose.foundation.shape.CircleShape)
            )
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PatchCard(
    patch: Patch,
    onDiscard: () -> Unit,
    onApply: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Intent: ${patch.spokenIntent}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Error: ${patch.errorText}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDiscard) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Discard Patch",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = patch.status)
                
                if (patch.status == PatchStatus.READY) {
                    Button(onClick = onApply) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Apply to Laptop")
                    }
                } else if (patch.status == PatchStatus.DRAFTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: PatchStatus) {
    val (color, label) = when (status) {
        PatchStatus.DRAFTING -> MaterialTheme.colorScheme.tertiary to "Generating Diff..."
        PatchStatus.READY -> Color(0xFF3DDC84) to "Ready to Apply"
        PatchStatus.SYNCED -> MaterialTheme.colorScheme.primary to "Sent via Bridge"
        PatchStatus.APPLIED -> MaterialTheme.colorScheme.secondary to "Applied Successfully"
        PatchStatus.DISCARDED -> MaterialTheme.colorScheme.error to "Discarded"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

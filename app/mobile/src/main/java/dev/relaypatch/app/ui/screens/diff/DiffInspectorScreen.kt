package dev.relaypatch.app.ui.screens.diff

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.relaypatch.app.data.model.Patch
import dev.relaypatch.app.domain.diff.*
import dev.relaypatch.app.ui.viewmodels.DiffInspectorViewModel

@Composable
fun DiffInspectorScreen(
    patchId: String,
    onNavigateBack: () -> Unit,
    viewModel: DiffInspectorViewModel = hiltViewModel()
) {
    // Load the patch when the screen opens
    LaunchedEffect(patchId) {
        viewModel.loadPatch(patchId)
    }

    val patch by viewModel.patch.collectAsStateWithLifecycle()
    val parsedLines by viewModel.parsedDiffLines.collectAsStateWithLifecycle()

    if (patch == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    DiffInspectorContent(
        patch = patch!!,
        diffLines = parsedLines,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffInspectorContent(
    patch: Patch,
    diffLines: List<DiffLine>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Patch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Context header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intent: ${patch.spokenIntent}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Hint: ${patch.targetFileHint ?: "None"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Diff Viewer
            if (diffLines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Diff is empty or unparseable.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    items(diffLines, key = { it.index }) { line ->
                        DiffLineRow(line)
                    }
                }
            }
        }
    }
}

@Composable
fun DiffLineRow(line: DiffLine) {
    val backgroundColor = when (line.type) {
        DiffLineType.ADDITION -> Color(0xFF1E3A2F) // Dark green
        DiffLineType.DELETION -> Color(0xFF3F1D24) // Dark red
        DiffLineType.HUNK_HEADER -> Color(0xFF1A334A) // Dark blue
        DiffLineType.CONTEXT -> Color.Transparent
    }

    val textColor = when (line.type) {
        DiffLineType.ADDITION -> Color(0xFF4CAF50)
        DiffLineType.DELETION -> Color(0xFFE53935)
        DiffLineType.HUNK_HEADER -> Color(0xFF64B5F6)
        DiffLineType.CONTEXT -> MaterialTheme.colorScheme.onSurface
    }

    val prefix = when (line.type) {
        DiffLineType.ADDITION -> "+"
        DiffLineType.DELETION -> "-"
        DiffLineType.HUNK_HEADER -> "@@"
        DiffLineType.CONTEXT -> " "
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        // Line numbers
        Text(
            text = "${line.oldLineNumber ?: "  "} ${line.newLineNumber ?: "  "} ",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.width(60.dp)
        )
        
        // Prefix
        Text(
            text = "$prefix ",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = textColor
        )

        // Content
        Text(
            text = line.content,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = textColor
        )
    }
}

package dev.relaypatch.app.ui.screens.capture

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.relaypatch.app.ui.viewmodels.CaptureViewModel

@Composable
fun CaptureScreen(
    onNavigateToQueue: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel()
) {
    val errorText by viewModel.errorText.collectAsState()
    val spokenIntent by viewModel.spokenIntent.collectAsState()

    CaptureContent(
        errorText = errorText,
        spokenIntent = spokenIntent,
        onErrorTextChanged = viewModel::updateErrorText,
        onSpokenIntentChanged = viewModel::updateSpokenIntent,
        onSubmit = {
            viewModel.submitCapture(onSuccess = onNavigateToQueue)
        },
        onNavigateToQueue = onNavigateToQueue
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureContent(
    errorText: String,
    spokenIntent: String,
    onErrorTextChanged: (String) -> Unit,
    onSpokenIntentChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToQueue: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RelayPatch Capture") },
                actions = {
                    TextButton(onClick = onNavigateToQueue) {
                        Text("View Queue")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mock Camera Viewfinder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "📷 Camera Viewfinder\n(Imagine looking at your laptop screen)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // OCR Error Text Field
            OutlinedTextField(
                value = errorText,
                onValueChange = onErrorTextChanged,
                label = { Text("Scanned Error Text") },
                placeholder = { Text("e.g. NullPointerException at line 42") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // Voice Intent Text Field
            OutlinedTextField(
                value = spokenIntent,
                onValueChange = onSpokenIntentChanged,
                label = { Text("Spoken Fix Intent") },
                placeholder = { Text("e.g. 'Add a null check before calling display'") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { /* TODO: Trigger STT */ }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice Intent",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                minLines = 2,
                maxLines = 3
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = errorText.isNotBlank() && spokenIntent.isNotBlank()
            ) {
                Text("Generate Patch")
            }
        }
    }
}

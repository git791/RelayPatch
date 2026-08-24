package dev.relaypatch.app.ui.screens.capture

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CaptureScreen(onNavigateToQueue: () -> Unit) {
    CaptureContent(onNavigateToQueue = onNavigateToQueue)
}

@Composable
fun CaptureContent(onNavigateToQueue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Capture Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToQueue) {
            Text("Go to Queue")
        }
    }
}

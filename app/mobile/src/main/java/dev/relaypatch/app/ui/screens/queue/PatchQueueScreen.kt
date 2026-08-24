package dev.relaypatch.app.ui.screens.queue

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PatchQueueScreen(onNavigateBack: () -> Unit, onNavigateToDiff: (String) -> Unit) {
    PatchQueueContent(onNavigateBack = onNavigateBack, onNavigateToDiff = onNavigateToDiff)
}

@Composable
fun PatchQueueContent(onNavigateBack: () -> Unit, onNavigateToDiff: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Patch Queue", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}

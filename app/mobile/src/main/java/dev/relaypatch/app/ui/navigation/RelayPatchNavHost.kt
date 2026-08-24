package dev.relaypatch.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.relaypatch.app.ui.screens.capture.CaptureScreen
import dev.relaypatch.app.ui.screens.queue.PatchQueueScreen

@Composable
fun RelayPatchNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "capture") {
        composable("capture") {
            CaptureScreen(
                onNavigateToQueue = { navController.navigate("queue") }
            )
        }
        composable("queue") {
            PatchQueueScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDiff = { patchId -> navController.navigate("diff/$patchId") }
            )
        }
        composable("diff/{patchId}") { backStackEntry ->
            val patchId = backStackEntry.arguments?.getString("patchId") ?: return@composable
            dev.relaypatch.app.ui.screens.diff.DiffInspectorScreen(
                patchId = patchId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

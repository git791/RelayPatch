package dev.relaypatch.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.relaypatch.app.ui.navigation.RelayPatchNavHost
import dev.relaypatch.app.ui.theme.RelayPatchTheme

/**
 * Single-activity host for the RelayPatch Compose UI.
 *
 * Annotated with [@AndroidEntryPoint] so Hilt can inject ViewModels into any composable
 * in the nav graph via [androidx.hilt.navigation.compose.hiltViewModel].
 *
 * Architecture notes (AGENTS.md §2.1):
 * - This activity owns no state itself — all state lives in ViewModels.
 * - [RelayPatchNavHost] is the root composable; it owns the [NavController] and
 *   composes all Screen-level composables.
 * - [RelayPatchTheme] wraps the entire graph so Material 3 Expressive tokens are
 *   available to every leaf composable without prop-drilling.
 * - [enableEdgeToEdge] is called before [setContent] so window insets are handled
 *   by the Compose layout rather than the system.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RelayPatchTheme {
                RelayPatchNavHost()
            }
        }
    }
}

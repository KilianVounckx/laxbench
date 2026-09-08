package io.github.kilianvounckx.laxbench

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 * The confirmation pop-up shown when an attempted platform back navigation is caught while
 * [GameScreen] is on its main sub-screen with no other dialog open (see [BackHandler] and
 * [GameScreen]). The game already autosaves continuously (see [GameScreen]'s autosave
 * `LaunchedEffect`); this dialog's body text only describes that existing behavior, it does not
 * trigger any save of its own.
 *
 * Tapping "Close" invokes [onConfirmClose], which lets the previously-suppressed back navigation
 * happen for real (see [PlatformBackNavigation.leaveApp]). Tapping "Stay in the app", dismissing by
 * tapping outside, or a further attempted back navigation (this dialog's own secondary control here
 * is "Stay in the app") all invoke [onStay] instead, leaving [GameScreen] exactly as it was.
 */
@Composable
fun ConfirmQuitDialog(onConfirmClose: () -> Unit, onStay: () -> Unit) {
  BackHandler(onBack = onStay)
  AlertDialog(
    onDismissRequest = onStay,
    properties = DialogProperties(dismissOnClickOutside = false),
    text = {
      Text(
        "Are you sure you want to close this app? The game will be saved on your device, but all" +
          " clocks will stop while the app is not open."
      )
    },
    confirmButton = { TextButton(onClick = onConfirmClose) { Text("Close") } },
    dismissButton = { TextButton(onClick = onStay) { Text("Stay in the app") } },
  )
}

package io.github.kilianvounckx.laxbench

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 * Renders when a lacrosse game ends in a tie (equal score for both teams at the end of the 4th
 * quarter): a pop-up displaying the fixed message "The game ended in a tie." with a single "OK"
 * button to dismiss it. Called from [GameScreen] when [TimerViewModel.quarterEndedEvents] fires for
 * [Quarter.FOURTH] and both teams have equal score.
 */
@Composable
fun TieDialog(onDismiss: () -> Unit) {
  BackHandler(onBack = onDismiss)
  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
    title = { Text("Game over") },
    text = { Text("The game ended in a tie.") },
    confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
  )
}

package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * A single-step pop-up offering the three time-out choices available once the clocks have just been
 * stopped (see [App]): "Time-out Home", "Officials Time-out", and "Time-out Visiting", always shown
 * in that order. Unlike [FoulDialog], this never needs more than one step, since no further data
 * needs to be collected beyond which of the three options was picked.
 *
 * Choosing "Time-out Home" or "Time-out Visiting" invokes [onConfirm] with the corresponding
 * [ScoreViewModel.Team] and closes the pop-up. Choosing "Officials Time-out" records nothing -- it
 * behaves exactly like dismissing the pop-up by tapping outside it or via a system back gesture,
 * invoking [onDismiss] instead of [onConfirm]. The clocks are already stopped by the time this
 * pop-up is shown (see [App]), regardless of which option is eventually chosen, or even if none is.
 */
@Composable
fun TimeOutDialog(onConfirm: (team: ScoreViewModel.Team) -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Time-out") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onConfirm(ScoreViewModel.Team.HOME) }) { Text("Time-out Home") }
        TextButton(onClick = onDismiss) { Text("Officials Time-out") }
        TextButton(onClick = { onConfirm(ScoreViewModel.Team.VISITING) }) {
          Text("Time-out Visiting")
        }
      }
    },
    confirmButton = {},
  )
}

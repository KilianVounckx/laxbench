package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * A single-step pop-up offering the two team choices available once the "Save" button has been
 * tapped (see [App]): "Save Home" and "Save Visiting", always shown in that order. Unlike
 * [FoulDialog], this never needs more than one step, since no further data needs to be collected
 * beyond which team's goalie made the save.
 *
 * Choosing either option invokes [onConfirm] with the corresponding [ScoreViewModel.Team] and
 * closes the pop-up. The dedicated "Cancel" button records nothing and invokes [onDismiss], exactly
 * like dismissing the pop-up by tapping outside it or via a system back gesture.
 */
@Composable
fun SaveDialog(onConfirm: (team: ScoreViewModel.Team) -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Save") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onConfirm(ScoreViewModel.Team.HOME) }) { Text("Save Home") }
        TextButton(onClick = { onConfirm(ScoreViewModel.Team.VISITING) }) { Text("Save Visiting") }
      }
    },
    confirmButton = {},
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

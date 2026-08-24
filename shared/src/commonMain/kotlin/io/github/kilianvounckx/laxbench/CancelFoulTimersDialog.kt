package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

private sealed class CancelFoulTimersStep {
  data object ChooseAction : CancelFoulTimersStep()

  data object ChooseOne : CancelFoulTimersStep()
}

/**
 * A pop-up to cancel [player]'s foul timer(s) (see [GameScreen], [CurrentFoulsScreen], and
 * [FoulTimerViewModel]), structurally mirroring [FoulDialog]'s "cancel only this one" vs "cancel
 * all" choice: the first step offers "Cancel one specific foul" (leading to a second step listing
 * [details] -- that player's currently running+queued foul timers, each labeled with its live
 * remaining/queued time and identified as running vs. queued via [FoulTimerDetail.kind] rather than
 * its position in the list; tapping one invokes [onCancelOne] with its id and closes the pop-up) or
 * "Cancel all" (invokes [onCancelAll] directly and closes the pop-up). Cancelling here only
 * stops/removes the live countdown(s) -- it never touches that team's permanent recorded foul
 * history (see [FoulViewModel]).
 */
@Composable
fun CancelFoulTimersDialog(
  player: FoulTimerPlayer,
  teams: TeamsInfo,
  details: List<FoulTimerDetail>,
  onCancelOne: (id: Long) -> Unit,
  onCancelAll: () -> Unit,
  onDismiss: () -> Unit,
) {
  var step by remember { mutableStateOf<CancelFoulTimersStep>(CancelFoulTimersStep.ChooseAction) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Cancel fouls for ${teams.label(player)}") },
    text = {
      when (step) {
        CancelFoulTimersStep.ChooseAction ->
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { step = CancelFoulTimersStep.ChooseOne }) {
              Text("Cancel one specific foul")
            }
            TextButton(
              onClick = {
                onCancelAll()
                onDismiss()
              }
            ) {
              Text("Cancel all")
            }
          }
        CancelFoulTimersStep.ChooseOne ->
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            details.forEach { detail ->
              TextButton(
                onClick = {
                  onCancelOne(detail.id)
                  onDismiss()
                }
              ) {
                Text(
                  when (detail.kind) {
                    FoulTimerEntryKind.RUNNING ->
                      "Cancel running foul (${detail.remainingTime.format()} remaining)"
                    FoulTimerEntryKind.QUEUED ->
                      "Cancel queued foul (${detail.remainingTime.format()})"
                  }
                )
              }
            }
          }
      }
    },
    confirmButton = {},
    dismissButton = {
      when (step) {
        CancelFoulTimersStep.ChooseAction -> TextButton(onClick = onDismiss) { Text("Cancel") }
        CancelFoulTimersStep.ChooseOne ->
          TextButton(onClick = { step = CancelFoulTimersStep.ChooseAction }) { Text("Back") }
      }
    },
  )
}

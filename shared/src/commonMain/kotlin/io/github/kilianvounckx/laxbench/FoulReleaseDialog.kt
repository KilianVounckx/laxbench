package io.github.kilianvounckx.laxbench

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private val DISMISS_DELAY = 5.seconds

/**
 * A pop-up notification shown once a player's last remaining foul timer finishes counting down
 * naturally (see [GameScreen] and [FoulTimerViewModel.releaseEvents]) -- never shown for a timer
 * discarded via [CancelFoulTimersDialog]. Dismissed automatically [DISMISS_DELAY] after being
 * shown, or immediately if the user taps outside it, whichever happens first; [onDismiss] is
 * invoked by whichever happens first (calling it a second time, e.g. if both race, is harmless
 * since the caller removes this pop-up from a list, and removing an absent element is a no-op).
 * [message] is the full "<color> <number> is released" text, built by the caller since composing it
 * needs both [FoulTimerPlayer] and [TeamsInfo].
 */
@Composable
fun FoulReleaseDialog(message: String, onDismiss: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(DISMISS_DELAY)
    onDismiss()
  }
  AlertDialog(onDismissRequest = onDismiss, text = { Text(message) }, confirmButton = {})
}

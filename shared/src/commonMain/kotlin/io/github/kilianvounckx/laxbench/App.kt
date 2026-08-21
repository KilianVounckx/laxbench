package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.kilianvounckx.laxbench.domain.Score

@Composable
@Preview
fun App() {
  MaterialTheme {
    val timerViewModel: TimerViewModel = viewModel { TimerViewModel() }
    val elapsedTime by timerViewModel.elapsedTime.collectAsStateWithLifecycle()
    val runState by timerViewModel.runState.collectAsStateWithLifecycle()

    val scoreViewModel: ScoreViewModel = viewModel { ScoreViewModel() }
    val ourScore by scoreViewModel.ourScore.collectAsStateWithLifecycle()
    val opponentScore by scoreViewModel.opponentScore.collectAsStateWithLifecycle()

    Column(
      modifier = Modifier.safeContentPadding().fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        ScoreNumber(
          score = ourScore,
          onIncrement = scoreViewModel::incrementOurScore,
          onDecrement = scoreViewModel::decrementOurScore,
        )
        Text(text = " - ", style = MaterialTheme.typography.headlineMedium)
        ScoreNumber(
          score = opponentScore,
          onIncrement = scoreViewModel::incrementOpponentScore,
          onDecrement = scoreViewModel::decrementOpponentScore,
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = elapsedTime.format(), style = MaterialTheme.typography.displayMedium)
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { timerViewModel.toggle() }) {
        Text(
          text =
            when (runState) {
              TimerViewModel.RunState.NotStarted -> "Start game"
              TimerViewModel.RunState.Running -> "Stop all clocks"
              TimerViewModel.RunState.Paused -> "Resume game"
            }
        )
      }
    }
  }
}

/**
 * A single tappable score number: tapping calls [onIncrement] (recording a goal for that side), and
 * long-pressing calls [onDecrement] (correcting a mistaken tap). Used for both our team's and the
 * opponent's score number in [App] — both behave identically and differ only in which callbacks
 * they're wired to, so the tap/long-press wiring lives here once instead of being duplicated per
 * side.
 */
@Composable
private fun ScoreNumber(score: Score, onIncrement: () -> Unit, onDecrement: () -> Unit) {
  Text(
    text = score.count.toString(),
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.combinedClickable(onClick = onIncrement, onLongClick = onDecrement),
  )
}

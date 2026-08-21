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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Score

/**
 * State backing the goal-recording pop-up (see [App] and [GoalDialog]): which
 * [team][ScoreViewModel.Team] tapped their score number, and the elapsed game time at the exact
 * moment of that tap. [elapsedTime] is captured once, when the tap opens the pop-up, and held
 * unchanged for as long as the pop-up stays open -- including if the user takes a while to fill in
 * the scorer/assist fields and confirm -- so the eventually-recorded [Goal] is timestamped to when
 * the goal actually happened, not to whenever the user finishes the form.
 */
private data class GoalDialogRequest(
  val team: ScoreViewModel.Team,
  val elapsedTime: ElapsedTime,
)

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

    var goalDialogRequest by remember { mutableStateOf<GoalDialogRequest?>(null) }

    Column(
      modifier = Modifier.safeContentPadding().fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        ScoreNumber(
          score = ourScore,
          onTap = {
            goalDialogRequest =
              GoalDialogRequest(ScoreViewModel.Team.OUR, timerViewModel.elapsedTime.value)
          },
          onLongPress = { scoreViewModel.decrementScore(ScoreViewModel.Team.OUR) },
        )
        Text(text = " - ", style = MaterialTheme.typography.headlineMedium)
        ScoreNumber(
          score = opponentScore,
          onTap = {
            goalDialogRequest =
              GoalDialogRequest(ScoreViewModel.Team.OPPONENT, timerViewModel.elapsedTime.value)
          },
          onLongPress = { scoreViewModel.decrementScore(ScoreViewModel.Team.OPPONENT) },
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
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { scoreViewModel.printDebugSummary() }) { Text("Print debug summary") }
    }

    goalDialogRequest?.let { request ->
      GoalDialog(
        onConfirm = { scorer, assist ->
          scoreViewModel.recordGoal(
            request.team,
            Goal(scorer = scorer, assist = assist, elapsedTime = request.elapsedTime),
          )
          goalDialogRequest = null
        },
        onDismiss = { goalDialogRequest = null },
      )
    }
  }
}

/**
 * A single tappable score number: tapping calls [onTap] (opening the goal-recording pop-up for that
 * side, see [App] and [GoalDialog]), and long-pressing calls [onLongPress] (correcting a mistaken
 * tap/confirmation by decrementing the score and removing the latest recorded goal for that side).
 * Used for both our team's and the opponent's score number in [App] -- both behave identically and
 * differ only in which callbacks they're wired to, so the tap/long-press wiring lives here once
 * instead of being duplicated per side.
 */
@Composable
private fun ScoreNumber(score: Score, onTap: () -> Unit, onLongPress: () -> Unit) {
  Text(
    text = score.count.toString(),
    style = MaterialTheme.typography.headlineMedium,
    modifier = Modifier.combinedClickable(onClick = onTap, onLongClick = onLongPress),
  )
}

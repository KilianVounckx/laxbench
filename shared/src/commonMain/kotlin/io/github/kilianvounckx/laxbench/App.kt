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
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.Score
import io.github.kilianvounckx.laxbench.domain.TimeOut

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

/**
 * The elapsed game time captured at the moment the "Foul" button was tapped (see [App] and
 * [FoulDialog]), held unchanged for the rest of the foul-recording pop-up flow so the eventually
 * recorded foul is timestamped to when it happened, not to whenever the multi-step form is
 * eventually completed -- mirroring [GoalDialogRequest]'s rationale. Unlike [GoalDialogRequest],
 * this does not also capture the team, since which team committed the foul is chosen as the first
 * step inside [FoulDialog] itself (there is no per-team "Foul" button to tap).
 */
private data class FoulDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Save" button was tapped (see [App] and
 * [SaveDialog]), held unchanged for as long as the save-recording pop-up stays open, so the
 * eventually recorded save is timestamped to when it happened, not to whenever the team choice is
 * eventually made -- mirroring [FoulDialogRequest]'s rationale.
 */
private data class SaveDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Face-off" button was tapped (see [App] and
 * [FaceOffDialog]), held unchanged for as long as the face-off-recording pop-up stays open, so the
 * eventually recorded face-off win is timestamped to when it happened, not to whenever the team
 * choice is eventually made -- mirroring [SaveDialogRequest]'s rationale.
 */
private data class FaceOffDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Stop all clocks" button was tapped -- i.e. the
 * [TimerViewModel.RunState.Running] -> [TimerViewModel.RunState.Paused] transition (see [App] and
 * [TimeOutDialog]) -- held unchanged for as long as the time-out pop-up stays open, so a team
 * time-out eventually recorded from it is timestamped to when the clocks were actually stopped, not
 * to whenever the pop-up is eventually closed. This is read right after [TimerViewModel.toggle]
 * runs (not before it, and not from the separately-collected `elapsedTime` UI state), since
 * [toggle] synchronously freezes the exact elapsed time at the pause instant, which is more precise
 * than whatever value was last ticked before the tap.
 */
private data class TimeOutDialogRequest(val elapsedTime: ElapsedTime)

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

    val foulViewModel: FoulViewModel = viewModel { FoulViewModel() }
    val saveViewModel: SaveViewModel = viewModel { SaveViewModel() }
    val faceOffViewModel: FaceOffViewModel = viewModel { FaceOffViewModel() }
    val timeOutViewModel: TimeOutViewModel = viewModel { TimeOutViewModel() }

    var goalDialogRequest by remember { mutableStateOf<GoalDialogRequest?>(null) }
    var foulDialogRequest by remember { mutableStateOf<FoulDialogRequest?>(null) }
    var saveDialogRequest by remember { mutableStateOf<SaveDialogRequest?>(null) }
    var faceOffDialogRequest by remember { mutableStateOf<FaceOffDialogRequest?>(null) }
    var timeOutDialogRequest by remember { mutableStateOf<TimeOutDialogRequest?>(null) }

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
              GoalDialogRequest(ScoreViewModel.Team.HOME, timerViewModel.elapsedTime.value)
          },
          onLongPress = { scoreViewModel.decrementScore(ScoreViewModel.Team.HOME) },
        )
        Text(text = " - ", style = MaterialTheme.typography.headlineMedium)
        ScoreNumber(
          score = opponentScore,
          onTap = {
            goalDialogRequest =
              GoalDialogRequest(ScoreViewModel.Team.VISITING, timerViewModel.elapsedTime.value)
          },
          onLongPress = { scoreViewModel.decrementScore(ScoreViewModel.Team.VISITING) },
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = elapsedTime.format(), style = MaterialTheme.typography.displayMedium)
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = {
          val wasRunning = runState == TimerViewModel.RunState.Running
          timerViewModel.toggle()
          if (wasRunning) {
            timeOutDialogRequest = TimeOutDialogRequest(timerViewModel.elapsedTime.value)
          }
        }
      ) {
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
      Button(
        onClick = { foulDialogRequest = FoulDialogRequest(timerViewModel.elapsedTime.value) }
      ) {
        Text("Foul")
      }
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = { saveDialogRequest = SaveDialogRequest(timerViewModel.elapsedTime.value) }
      ) {
        Text("Save")
      }
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = { faceOffDialogRequest = FaceOffDialogRequest(timerViewModel.elapsedTime.value) }
      ) {
        Text("Face-off")
      }
      Spacer(modifier = Modifier.height(16.dp))
      Button(
        onClick = {
          scoreViewModel.printDebugSummary()
          foulViewModel.printDebugSummary()
          timeOutViewModel.printDebugSummary()
          saveViewModel.printDebugSummary()
          faceOffViewModel.printDebugSummary()
        }
      ) {
        Text("Print debug summary")
      }
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

    foulDialogRequest?.let { request ->
      FoulDialog(
        onConfirm = { team, player, severity ->
          foulViewModel.recordFoul(
            team,
            Foul(player = player, severity = severity, elapsedTime = request.elapsedTime),
          )
          foulDialogRequest = null
        },
        onDismiss = { foulDialogRequest = null },
      )
    }

    timeOutDialogRequest?.let { request ->
      TimeOutDialog(
        onConfirm = { team ->
          timeOutViewModel.recordTimeOut(team, TimeOut(elapsedTime = request.elapsedTime))
          timeOutDialogRequest = null
        },
        onDismiss = { timeOutDialogRequest = null },
      )
    }

    saveDialogRequest?.let { request ->
      SaveDialog(
        onConfirm = { team ->
          saveViewModel.recordSave(team, Save(elapsedTime = request.elapsedTime))
          saveDialogRequest = null
        },
        onDismiss = { saveDialogRequest = null },
      )
    }

    faceOffDialogRequest?.let { request ->
      FaceOffDialog(
        onConfirm = { team ->
          faceOffViewModel.recordFaceOff(team, FaceOff(elapsedTime = request.elapsedTime))
          faceOffDialogRequest = null
        },
        onDismiss = { faceOffDialogRequest = null },
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

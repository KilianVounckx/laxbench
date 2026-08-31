package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Score
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private val BLINK_INTERVAL = 500.milliseconds

/**
 * State backing the goal-recording pop-up (see [GameScreen] and [GoalDialog]): which
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
 * The elapsed game time captured at the moment the "Foul" button was tapped (see [GameScreen] and
 * [FoulDialog]), held unchanged for the rest of the foul-recording pop-up flow so the eventually
 * recorded foul is timestamped to when it happened, not to whenever the multi-step form is
 * eventually completed -- mirroring [GoalDialogRequest]'s rationale. Unlike [GoalDialogRequest],
 * this does not also capture the team, since which team committed the foul is chosen as the first
 * step inside [FoulDialog] itself (there is no per-team "Foul" button to tap).
 */
private data class FoulDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Save" button was tapped (see [GameScreen] and
 * [SaveDialog]), held unchanged for as long as the save-recording pop-up stays open, so the
 * eventually recorded save is timestamped to when it happened, not to whenever the team choice is
 * eventually made -- mirroring [FoulDialogRequest]'s rationale.
 */
private data class SaveDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Face-off" button was tapped (see [GameScreen]
 * and [FaceOffDialog]), held unchanged for as long as the face-off-recording pop-up stays open, so
 * the eventually recorded face-off win is timestamped to when it happened, not to whenever the team
 * choice is eventually made -- mirroring [SaveDialogRequest]'s rationale.
 */
private data class FaceOffDialogRequest(val elapsedTime: ElapsedTime)

/**
 * The elapsed game time captured at the moment the "Stop all clocks" button was tapped -- i.e. the
 * [TimerViewModel.RunState.Running] -> [TimerViewModel.RunState.Paused] transition (see
 * [GameScreen] and [TimeOutDialog]) -- held unchanged for as long as the time-out pop-up stays
 * open, so a team time-out eventually recorded from it is timestamped to when the clocks were
 * actually stopped, not to whenever the pop-up is eventually closed. This is read right after
 * [TimerViewModel.toggle] runs (not before it, and not from the separately-collected `elapsedTime`
 * UI state), since [toggle] synchronously freezes the exact elapsed time at the pause instant,
 * which is more precise than whatever value was last ticked before the tap.
 */
private data class TimeOutDialogRequest(val elapsedTime: ElapsedTime)

private data class PopupMessage(val id: Long, val message: String)

/**
 * Which sub-screen of the game view is currently shown: [MAIN] is the ordinary score/timer/buttons
 * view; [CURRENT_FOULS] is the "Current fouls" list (see `CurrentFoulsScreen`), reached via its
 * button and left via its own "Back" button. Modeled as an enum rather than a `showCurrentFouls`
 * boolean, per this repo's enum-over-boolean convention and mirroring [App]'s own `Screen`
 * sealed-interface pattern for "which screen is shown" one level up.
 */
private enum class GameSubScreen {
  MAIN,
  CURRENT_FOULS,
  MANAGE_GAME,
}

/**
 * The game screen displaying the current score, timer, and buttons for recording goals, fouls,
 * saves, face-offs, and time-outs. All per-game ViewModels are obtained from the provided
 * [viewModelStoreOwner].
 */
@Composable
internal fun GameScreen(initialTeams: TeamsInfo, viewModelStoreOwner: ViewModelStoreOwner) {
  val timerViewModel: TimerViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { TimerViewModel() }
  val elapsedTime by timerViewModel.elapsedTime.collectAsStateWithLifecycle()
  val runState by timerViewModel.runState.collectAsStateWithLifecycle()

  val scoreViewModel: ScoreViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { ScoreViewModel() }
  val ourScore by scoreViewModel.ourScore.collectAsStateWithLifecycle()
  val opponentScore by scoreViewModel.opponentScore.collectAsStateWithLifecycle()

  val teamsViewModel: TeamsViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { TeamsViewModel(initialTeams) }
  val teamsInfo by teamsViewModel.teamsInfo.collectAsStateWithLifecycle()

  val foulViewModel: FoulViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { FoulViewModel() }
  val saveViewModel: SaveViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { SaveViewModel() }
  val faceOffViewModel: FaceOffViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { FaceOffViewModel() }
  val timeOutViewModel: TimeOutViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { TimeOutViewModel() }
  val timeOutCountdownViewModel: TimeOutCountdownViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { TimeOutCountdownViewModel() }
  val timeOutCountdownRemainingTime by
    timeOutCountdownViewModel.remainingTime.collectAsStateWithLifecycle()
  val timeOutCountdownIsExpired by timeOutCountdownViewModel.isExpired.collectAsStateWithLifecycle()
  val timeOutCountdownIsVisible by timeOutCountdownViewModel.isVisible.collectAsStateWithLifecycle()

  val foulTimerViewModel: FoulTimerViewModel =
    viewModel(viewModelStoreOwner = viewModelStoreOwner) { FoulTimerViewModel() }
  val foulTimerRemainingTimes by foulTimerViewModel.remainingTimes.collectAsStateWithLifecycle()
  val foulTimerDetails by foulTimerViewModel.details.collectAsStateWithLifecycle()

  val pdfSaver = rememberPdfSaver()

  var goalDialogRequest by remember { mutableStateOf<GoalDialogRequest?>(null) }
  var foulDialogRequest by remember { mutableStateOf<FoulDialogRequest?>(null) }
  var saveDialogRequest by remember { mutableStateOf<SaveDialogRequest?>(null) }
  var faceOffDialogRequest by remember { mutableStateOf<FaceOffDialogRequest?>(null) }
  var timeOutDialogRequest by remember { mutableStateOf<TimeOutDialogRequest?>(null) }

  var gameSubScreen by remember { mutableStateOf(GameSubScreen.MAIN) }
  var cancelFoulTimersRequest by remember { mutableStateOf<FoulTimerPlayer?>(null) }
  val popups = remember { mutableStateListOf<PopupMessage>() }
  var nextPopupId by remember { mutableStateOf(0L) }
  fun addPopup(message: String) {
    popups.add(PopupMessage(nextPopupId, message))
    nextPopupId += 1
  }
  LaunchedEffect(foulTimerViewModel) {
    foulTimerViewModel.releaseEvents.collect { player ->
      addPopup("${teamsInfo.label(player)} is released")
    }
  }
  LaunchedEffect(foulTimerDetails, cancelFoulTimersRequest) {
    val requestedPlayer = cancelFoulTimersRequest
    if (requestedPlayer != null && foulTimerDetails[requestedPlayer] == null) {
      cancelFoulTimersRequest = null
    }
  }

  fun addFoul(
    team: ScoreViewModel.Team,
    player: PlayerNumber,
    severity: FoulSeverity,
    elapsedTime: ElapsedTime,
  ) {
    val foul = foulViewModel.recordFoul(team, player, severity, elapsedTime)
    foulTimerViewModel.recordFoul(
      team,
      player,
      severity,
      id = foul.id,
      isGameClockRunning = runState == TimerViewModel.RunState.Running,
    )
  }

  fun updateFoul(team: ScoreViewModel.Team, original: Foul, edited: Foul) {
    foulViewModel.updateFoul(team, edited)
    if (edited.severity.timerDuration != original.severity.timerDuration) {
      foulTimerViewModel.adjustDuration(edited.id, edited.severity.timerDuration)
    }
    if (edited.player != original.player) {
      foulTimerViewModel.movePlayer(
        team = team,
        id = edited.id,
        newPlayer = edited.player,
        isGameClockRunning = runState == TimerViewModel.RunState.Running,
      )
    }
  }

  fun deleteFoul(team: ScoreViewModel.Team, foul: Foul) {
    foulViewModel.removeFoul(team, foul.id)
    if (foulTimerViewModel.cancelById(foul.id) == FoulTimerCancelOutcome.RELEASED) {
      val label = teamsInfo.label(FoulTimerPlayer(team, foul.player))
      addPopup("$label's foul was removed, they are released")
    }
  }

  when (gameSubScreen) {
    GameSubScreen.CURRENT_FOULS ->
      CurrentFoulsScreen(
        teams = teamsInfo,
        remainingTimes = foulTimerRemainingTimes,
        onPlayerTapped = { cancelFoulTimersRequest = it },
        onBack = { gameSubScreen = GameSubScreen.MAIN },
      )
    GameSubScreen.MAIN ->
      Column(
        modifier = Modifier.safeContentPadding().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ScoreNumber(
            label = teamsInfo.label(ScoreViewModel.Team.HOME),
            score = ourScore,
            onTap = {
              goalDialogRequest =
                GoalDialogRequest(ScoreViewModel.Team.HOME, timerViewModel.elapsedTime.value)
            },
          )
          Text(text = " - ", style = MaterialTheme.typography.headlineMedium)
          ScoreNumber(
            label = teamsInfo.label(ScoreViewModel.Team.VISITING),
            score = opponentScore,
            onTap = {
              goalDialogRequest =
                GoalDialogRequest(ScoreViewModel.Team.VISITING, timerViewModel.elapsedTime.value)
            },
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = elapsedTime.format(), style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            when (runState) {
              TimerViewModel.RunState.Paused -> {
                timeOutCountdownViewModel.cancel()
                timeOutDialogRequest = null
                timerViewModel.toggle()
                foulTimerViewModel.resume()
              }
              TimerViewModel.RunState.NotStarted,
              TimerViewModel.RunState.Running -> {
                val wasRunning = runState == TimerViewModel.RunState.Running
                timerViewModel.toggle()
                if (wasRunning) {
                  timeOutDialogRequest = TimeOutDialogRequest(timerViewModel.elapsedTime.value)
                  timeOutCountdownViewModel.start()
                  foulTimerViewModel.pause()
                } else {
                  foulTimerViewModel.resume()
                }
              }
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
        if (timeOutCountdownIsVisible) {
          timeOutCountdownRemainingTime?.let { remaining ->
            TimeOutCountdownDisplay(
              remainingTime = remaining,
              isExpired = timeOutCountdownIsExpired,
            )
            Spacer(modifier = Modifier.height(16.dp))
          }
        }
        Button(
          onClick = { foulDialogRequest = FoulDialogRequest(timerViewModel.elapsedTime.value) }
        ) {
          Text("Foul")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (foulTimerRemainingTimes.isNotEmpty()) {
          Button(onClick = { gameSubScreen = GameSubScreen.CURRENT_FOULS }) {
            Text("Current fouls")
          }
          Spacer(modifier = Modifier.height(16.dp))
        }
        Button(
          onClick = { saveDialogRequest = SaveDialogRequest(timerViewModel.elapsedTime.value) }
        ) {
          Text("Save")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            faceOffDialogRequest = FaceOffDialogRequest(timerViewModel.elapsedTime.value)
          }
        ) {
          Text("Face-off")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            val data =
              ScoreSheetData.of(
                homeGoals = scoreViewModel.goals(ScoreViewModel.Team.HOME).value,
                visitingGoals = scoreViewModel.goals(ScoreViewModel.Team.VISITING).value,
                homeFouls = foulViewModel.fouls(ScoreViewModel.Team.HOME).value,
                visitingFouls = foulViewModel.fouls(ScoreViewModel.Team.VISITING).value,
                homeTimeOuts = timeOutViewModel.timeOuts(ScoreViewModel.Team.HOME).value,
                visitingTimeOuts = timeOutViewModel.timeOuts(ScoreViewModel.Team.VISITING).value,
                homeSaves = saveViewModel.saves(ScoreViewModel.Team.HOME).value,
                visitingSaves = saveViewModel.saves(ScoreViewModel.Team.VISITING).value,
                homeFaceOffs = faceOffViewModel.faceOffs(ScoreViewModel.Team.HOME).value,
                visitingFaceOffs = faceOffViewModel.faceOffs(ScoreViewModel.Team.VISITING).value,
                homeName = teamsInfo.home.name,
                visitingName = teamsInfo.visiting.name,
              )
            pdfSaver.save("laxbench-scoresheet.pdf", data.toPdfBytes())
          }
        ) {
          Text("Generate PDF")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = {
            scoreViewModel.printDebugSummary()
            foulViewModel.printDebugSummary()
            timeOutViewModel.printDebugSummary()
            saveViewModel.printDebugSummary()
            faceOffViewModel.printDebugSummary()
            foulTimerViewModel.printDebugSummary()
          }
        ) {
          Text("Print debug summary")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { gameSubScreen = GameSubScreen.MANAGE_GAME }) { Text("Manage game") }
      }
    GameSubScreen.MANAGE_GAME ->
      ManageGameScreen(
        teamsInfo = teamsInfo,
        elapsedTime = elapsedTime,
        homeGoals = scoreViewModel.goals(ScoreViewModel.Team.HOME),
        visitingGoals = scoreViewModel.goals(ScoreViewModel.Team.VISITING),
        homeFouls = foulViewModel.fouls(ScoreViewModel.Team.HOME),
        visitingFouls = foulViewModel.fouls(ScoreViewModel.Team.VISITING),
        homeFaceOffs = faceOffViewModel.faceOffs(ScoreViewModel.Team.HOME),
        visitingFaceOffs = faceOffViewModel.faceOffs(ScoreViewModel.Team.VISITING),
        homeTimeOuts = timeOutViewModel.timeOuts(ScoreViewModel.Team.HOME),
        visitingTimeOuts = timeOutViewModel.timeOuts(ScoreViewModel.Team.VISITING),
        homeSaves = saveViewModel.saves(ScoreViewModel.Team.HOME),
        visitingSaves = saveViewModel.saves(ScoreViewModel.Team.VISITING),
        onUpdateTeam = teamsViewModel::update,
        onAddGoal = { team, scorer, assist, time ->
          scoreViewModel.recordGoal(team, scorer, assist, time)
        },
        onUpdateGoal = { team, edited -> scoreViewModel.updateGoal(team, edited) },
        onDeleteGoal = { team, id -> scoreViewModel.removeGoal(team, id) },
        onAddFoul = ::addFoul,
        onUpdateFoul = ::updateFoul,
        onDeleteFoul = ::deleteFoul,
        onAddFaceOff = { team, time -> faceOffViewModel.recordFaceOff(team, time) },
        onDeleteFaceOff = { team, id -> faceOffViewModel.removeFaceOff(team, id) },
        onAddTimeOut = { team, time -> timeOutViewModel.recordTimeOut(team, time) },
        onDeleteTimeOut = { team, id -> timeOutViewModel.removeTimeOut(team, id) },
        onAddSave = { team, time -> saveViewModel.recordSave(team, time) },
        onDeleteSave = { team, id -> saveViewModel.removeSave(team, id) },
        onBack = { gameSubScreen = GameSubScreen.MAIN },
      )
  }

  goalDialogRequest?.let { request ->
    GoalDialog(
      onConfirm = { scorer, assist ->
        scoreViewModel.recordGoal(request.team, scorer, assist, request.elapsedTime)
        goalDialogRequest = null
      },
      onDismiss = { goalDialogRequest = null },
    )
  }

  foulDialogRequest?.let { request ->
    FoulDialog(
      teams = teamsInfo,
      onConfirm = { team, player, severity ->
        addFoul(team, player, severity, request.elapsedTime)
      },
      onDismiss = { foulDialogRequest = null },
    )
  }

  timeOutDialogRequest?.let { request ->
    TimeOutDialog(
      teams = teamsInfo,
      onConfirm = { team ->
        timeOutViewModel.recordTimeOut(team, request.elapsedTime)
        timeOutCountdownViewModel.show()
        timeOutDialogRequest = null
      },
      onDismiss = {
        timeOutCountdownViewModel.cancel()
        timeOutDialogRequest = null
      },
    )
  }

  saveDialogRequest?.let { request ->
    SaveDialog(
      teams = teamsInfo,
      onConfirm = { team ->
        saveViewModel.recordSave(team, request.elapsedTime)
        saveDialogRequest = null
      },
      onDismiss = { saveDialogRequest = null },
    )
  }

  faceOffDialogRequest?.let { request ->
    FaceOffDialog(
      teams = teamsInfo,
      onConfirm = { team ->
        faceOffViewModel.recordFaceOff(team, request.elapsedTime)
        faceOffDialogRequest = null
      },
      onDismiss = { faceOffDialogRequest = null },
    )
  }

  cancelFoulTimersRequest?.let { player ->
    foulTimerDetails[player]?.let { playerDetails ->
      CancelFoulTimersDialog(
        player = player,
        teams = teamsInfo,
        details = playerDetails,
        onCancelOne = { id -> foulTimerViewModel.cancelOne(player, id) },
        onCancelAll = { foulTimerViewModel.cancelAll(player) },
        onDismiss = { cancelFoulTimersRequest = null },
      )
    }
  }

  for (popup in popups) {
    key(popup.id) {
      FoulReleaseDialog(
        message = popup.message,
        onDismiss = { popups.remove(popup) },
      )
    }
  }
}

/**
 * A single tappable score number: tapping calls [onTap] (opening the goal-recording pop-up for that
 * side, see [GameScreen] and [GoalDialog]). Used for both our team's and the opponent's score
 * number in [GameScreen] -- both behave identically and differ only in which callback they're wired
 * to, so the tap wiring lives here once instead of being duplicated per side.
 */
@Composable
private fun ScoreNumber(
  label: String,
  score: Score,
  onTap: () -> Unit,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
    Text(
      text = score.count.toString(),
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.clickable(onClick = onTap),
    )
  }
}

/**
 * The visible portion of the time-out countdown started the instant the clocks are stopped (see
 * [GameScreen], [TimeOutCountdownViewModel], and [TimeOutDialog]): renders [remainingTime] the same
 * way the main game clock is rendered, and once [isExpired], blinks by alternating its text color
 * between the theme's normal content color and its error color on a fixed interval instead of
 * counting any further -- there is nothing left to count down once frozen at zero, so the blink is
 * a plain, purely visual, indefinitely-repeating cue that 90 seconds have elapsed, with no other
 * effect (no sound/haptics, per the feature's non-goals). The text itself is never hidden; only its
 * color changes.
 */
@Composable
private fun TimeOutCountdownDisplay(remainingTime: ElapsedTime, isExpired: Boolean) {
  var blinkShowsErrorColor by remember { mutableStateOf(false) }
  LaunchedEffect(isExpired) {
    if (isExpired) {
      while (true) {
        delay(BLINK_INTERVAL)
        blinkShowsErrorColor = !blinkShowsErrorColor
      }
    } else {
      blinkShowsErrorColor = false
    }
  }
  Text(
    text = "Time-out: ${remainingTime.format()}",
    style = MaterialTheme.typography.headlineSmall,
    color =
      if (isExpired && blinkShowsErrorColor) MaterialTheme.colorScheme.error
      else MaterialTheme.colorScheme.onSurface,
  )
}

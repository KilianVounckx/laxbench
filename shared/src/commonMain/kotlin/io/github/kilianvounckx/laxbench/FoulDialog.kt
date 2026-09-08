package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.kilianvounckx.laxbench.domain.FoulDuration
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.MajorFoulType
import io.github.kilianvounckx.laxbench.domain.MinorFoulType
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

/**
 * Which step of the foul-recording pop-up (see [App] and [FoulDialog]) is currently shown, together
 * with exactly the data already collected in earlier steps, so a later step is never reachable
 * without the team/player number it depends on.
 */
private sealed class FoulDialogStep {
  /** First step: choose which team committed the foul. */
  data object ChooseTeam : FoulDialogStep()

  /** Second step: enter the offending player's number, for the team chosen in [ChooseTeam]. */
  data class EnterPlayer(val team: ScoreViewModel.Team) : FoulDialogStep()

  /** Third step: choose the foul's severity, for the team/player chosen in earlier steps. */
  data class ChooseSeverity(val team: ScoreViewModel.Team, val player: PlayerNumber) :
    FoulDialogStep()

  /** Reached after choosing "Minor" in [ChooseSeverity]: choose the specific minor foul type. */
  data class ChooseMinorType(val team: ScoreViewModel.Team, val player: PlayerNumber) :
    FoulDialogStep()

  /** Reached after choosing "Major" in [ChooseSeverity]: choose the specific major foul type. */
  data class ChooseMajorType(val team: ScoreViewModel.Team, val player: PlayerNumber) :
    FoulDialogStep()

  /**
   * Reached after choosing a specific [MajorFoulType] in [ChooseMajorType]: choose the penalty
   * [FoulDuration] for that major foul. This is the last step for a Major foul -- picking a
   * duration here is what finally records it.
   */
  data class ChooseFoulDuration(
    val team: ScoreViewModel.Team,
    val player: PlayerNumber,
    val type: MajorFoulType,
  ) : FoulDialogStep()

  /**
   * Reached immediately after a foul has been fully filled in -- the point where, before this
   * feature, [FoulDialog] would have called onConfirm and closed. Asks whether there is another
   * simultaneous foul to log for the same incident. Answering "Add another foul" restarts the
   * wizard from [ChooseTeam] for a new foul while keeping everything already queued; answering
   * "Done" commits the whole queued batch and closes the dialog. See [FoulDialog]'s class doc for
   * full details.
   */
  data object ConfirmMore : FoulDialogStep()

  /**
   * Reached when "Cancel" is pressed on [ChooseTeam] (the only step whose secondary button reads
   * "Cancel" rather than "Back" -- see [previous]) while at least one foul has already been queued
   * in this invocation's pending batch. Offers an explicit choice between discarding only the
   * not-yet-completed foul (returning to [ConfirmMore] with the batch left intact) and discarding
   * the in-progress foul together with the entire batch (closing the dialog with nothing recorded).
   * Unreachable while the batch is empty: in that case "Cancel" on [ChooseTeam] invokes
   * [FoulDialog]'s onDismiss directly, exactly as it always has.
   */
  data object ConfirmCancelChoice : FoulDialogStep()
}

/**
 * The step shown immediately before [this] one in the foul-recording flow, or `null` if [this] is
 * the first step ([FoulDialogStep.ChooseTeam]), which has no previous step to go back to. Drives
 * [FoulDialog]'s single secondary button: from any step but the first that button goes back here
 * instead of closing the pop-up. Whatever was already entered for the step returned to (e.g.
 * player-number text already typed) is unaffected, since navigating between steps never clears any
 * of the separately-`remember`ed input state.
 */
private fun FoulDialogStep.previous(): FoulDialogStep? =
  when (this) {
    is FoulDialogStep.ChooseTeam -> null
    is FoulDialogStep.EnterPlayer -> FoulDialogStep.ChooseTeam
    is FoulDialogStep.ChooseSeverity -> FoulDialogStep.EnterPlayer(team)
    is FoulDialogStep.ChooseMinorType -> FoulDialogStep.ChooseSeverity(team, player)
    is FoulDialogStep.ChooseMajorType -> FoulDialogStep.ChooseSeverity(team, player)
    is FoulDialogStep.ChooseFoulDuration -> FoulDialogStep.ChooseMajorType(team, player)
    FoulDialogStep.ConfirmMore -> null
    FoulDialogStep.ConfirmCancelChoice -> null
  }

/**
 * Which of three shapes the current [FoulDialogStep] has, for the sole purpose of computing
 * [foulDialogBackAction]: [FIRST] is [FoulDialogStep.ChooseTeam] (no step to go back to);
 * [WITH_PREVIOUS] is any step [FoulDialogStep.previous] returns non-null for; [BATCH_CONFIRMATION]
 * is [FoulDialogStep.ConfirmMore] or [FoulDialogStep.ConfirmCancelChoice] -- the two steps with no
 * visible secondary button of their own today.
 */
internal enum class FoulDialogStepKind {
  FIRST,
  WITH_PREVIOUS,
  BATCH_CONFIRMATION,
}

private fun FoulDialogStep.kind(): FoulDialogStepKind =
  when (this) {
    FoulDialogStep.ConfirmMore,
    FoulDialogStep.ConfirmCancelChoice -> FoulDialogStepKind.BATCH_CONFIRMATION
    FoulDialogStep.ChooseTeam -> FoulDialogStepKind.FIRST
    is FoulDialogStep.EnterPlayer,
    is FoulDialogStep.ChooseSeverity,
    is FoulDialogStep.ChooseMinorType,
    is FoulDialogStep.ChooseMajorType,
    is FoulDialogStep.ChooseFoulDuration -> FoulDialogStepKind.WITH_PREVIOUS
  }

/**
 * The action [FoulDialog]'s secondary control -- its visible button for
 * [FoulDialogStepKind.FIRST]/[FoulDialogStepKind.WITH_PREVIOUS] steps, or an intercepted platform
 * back navigation for every step, including [FoulDialogStepKind.BATCH_CONFIRMATION] steps which
 * have no visible button of their own -- should take, computed purely from [stepKind] and whether
 * the pending batch is currently empty. See [FoulDialog]'s class doc for the full behavioral
 * rationale.
 */
internal enum class FoulDialogBackAction {
  GO_TO_PREVIOUS_STEP,
  DISMISS,
  GO_TO_CONFIRM_CANCEL_CHOICE,
  COMMIT_PENDING_BATCH_AND_DISMISS,
}

internal fun foulDialogBackAction(
  stepKind: FoulDialogStepKind,
  pendingBatchIsEmpty: Boolean,
): FoulDialogBackAction =
  when (stepKind) {
    FoulDialogStepKind.BATCH_CONFIRMATION -> FoulDialogBackAction.COMMIT_PENDING_BATCH_AND_DISMISS
    FoulDialogStepKind.WITH_PREVIOUS -> FoulDialogBackAction.GO_TO_PREVIOUS_STEP
    FoulDialogStepKind.FIRST ->
      if (pendingBatchIsEmpty) FoulDialogBackAction.DISMISS
      else FoulDialogBackAction.GO_TO_CONFIRM_CANCEL_CHOICE
  }

/**
 * One foul that has been fully filled in during this [FoulDialog] invocation and is waiting,
 * in-memory only, to be committed via onConfirm once the scorekeeper finishes by answering "Done"
 * -- or to be discarded entirely if "Cancel all" is chosen instead. Holds the team alongside the
 * player/severity (unlike [Foul], which omits the team) because a single invocation's batch can
 * span both teams.
 */
private data class PendingFoul(
  val team: ScoreViewModel.Team,
  val player: PlayerNumber,
  val severity: FoulSeverity,
)

/**
 * A multi-step pop-up for recording fouls, analogous to [GoalDialog] but with more steps: which
 * team committed the foul, the offending player's number, and the foul's severity (with a further
 * specific-type step for [FoulSeverity.Minor]/[FoulSeverity.Major], since [FoulSeverity.Expulsion]
 * has no sub-type of its own, and, for [FoulSeverity.Major] only, one more step after that to
 * choose the penalty duration). Shown when the "Foul" button is tapped (see [GameScreen]).
 *
 * After a foul is fully filled in, the wizard shows a plain "any more fouls?" step
 * ([FoulDialogStep.ConfirmMore]) instead of immediately committing. "Add another foul" queues the
 * completed foul into an in-memory pending batch and restarts the wizard at
 * [FoulDialogStep.ChooseTeam] (for either team, any player/severity). "Done" queues the completed
 * foul, then commits the whole batch by invoking [onConfirm] once per queued foul (oldest first)
 * and finally invoking [onDismiss] exactly once to close the dialog.
 *
 * [onConfirm] is therefore now invoked zero or more times per dialog invocation (never as a side
 * effect of closing the dialog), always immediately followed by exactly one [onDismiss] call when
 * the batch is committed.
 *
 * Cancelling on the very first step ([FoulDialogStep.ChooseTeam]) while the pending batch is still
 * empty behaves exactly as before: [onDismiss] is invoked directly, nothing is recorded. Cancelling
 * on [FoulDialogStep.ChooseTeam] once the pending batch already holds at least one queued foul
 * instead shows [FoulDialogStep.ConfirmCancelChoice], offering "Cancel only this foul" (discard the
 * not-yet-completed entry, return to [FoulDialogStep.ConfirmMore], batch untouched) or "Cancel all"
 * (discard everything, including already-queued fouls, and invoke [onDismiss] with nothing
 * recorded).
 *
 * An attempted platform back navigation (see [BackHandler]) is handled by [performBackAction] via
 * [foulDialogBackAction], exactly mirroring whatever this dialog's own secondary button would do
 * for the current step: from any step [FoulDialogStep.previous] returns non-null for, it goes back
 * one step; from [FoulDialogStep.ChooseTeam] it dismisses outright if the pending batch is empty,
 * or opens [FoulDialogStep.ConfirmCancelChoice] otherwise; from [FoulDialogStep.ConfirmMore] or
 * [FoulDialogStep.ConfirmCancelChoice] it commits the pending batch and dismisses, exactly like
 * "Done". It is [onDismiss] itself, not a back gesture, that is invoked unconditionally and
 * immediately regardless of step or batch contents, but only via `onDismissRequest` -- tapping
 * outside is disabled on every step via `dismissOnClickOutside = false`.
 *
 * There is still no way to review, edit, or remove an individual already-queued foul short of
 * discarding the whole batch via "Cancel all".
 */
@Composable
fun FoulDialog(
  teams: TeamsInfo,
  onConfirm: (team: ScoreViewModel.Team, player: PlayerNumber, severity: FoulSeverity) -> Unit,
  onDismiss: () -> Unit,
) {
  var step by remember { mutableStateOf<FoulDialogStep>(FoulDialogStep.ChooseTeam) }
  var playerText by remember { mutableStateOf("") }
  var pendingBatch by remember { mutableStateOf<List<PendingFoul>>(emptyList()) }
  val player = PlayerNumber.parse(playerText)

  fun completeFoul(team: ScoreViewModel.Team, player: PlayerNumber, severity: FoulSeverity) {
    pendingBatch = pendingBatch + PendingFoul(team, player, severity)
    step = FoulDialogStep.ConfirmMore
  }

  fun commitPendingBatchAndDismiss() {
    pendingBatch.forEach { onConfirm(it.team, it.player, it.severity) }
    onDismiss()
  }

  fun performBackAction() {
    when (foulDialogBackAction(step.kind(), pendingBatch.isEmpty())) {
      FoulDialogBackAction.GO_TO_PREVIOUS_STEP -> step = step.previous()!!
      FoulDialogBackAction.DISMISS -> onDismiss()
      FoulDialogBackAction.GO_TO_CONFIRM_CANCEL_CHOICE -> step = FoulDialogStep.ConfirmCancelChoice
      FoulDialogBackAction.COMMIT_PENDING_BATCH_AND_DISMISS -> commitPendingBatchAndDismiss()
    }
  }

  BackHandler(onBack = ::performBackAction)

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
    title = { Text("Record foul") },
    text = {
      ScrollableDialogContent {
        when (val currentStep = step) {
          is FoulDialogStep.ChooseTeam ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              ScoreViewModel.Team.entries.forEach { team ->
                TextButton(onClick = { step = FoulDialogStep.EnterPlayer(team) }) {
                  Text(teams.label(team))
                }
              }
            }
          is FoulDialogStep.EnterPlayer ->
            OutlinedTextField(
              value = playerText,
              onValueChange = { playerText = it },
              label = { Text("Player number") },
              isError = playerText.isNotBlank() && player == null,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
            )
          is FoulDialogStep.ChooseSeverity ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(
                onClick = {
                  step = FoulDialogStep.ChooseMinorType(currentStep.team, currentStep.player)
                }
              ) {
                Text("Minor")
              }
              TextButton(
                onClick = {
                  step = FoulDialogStep.ChooseMajorType(currentStep.team, currentStep.player)
                }
              ) {
                Text("Major")
              }
              TextButton(
                onClick = {
                  completeFoul(currentStep.team, currentStep.player, FoulSeverity.Expulsion)
                }
              ) {
                Text("Expulsion")
              }
            }
          is FoulDialogStep.ChooseMinorType ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              MinorFoulType.entries.forEach { type ->
                TextButton(
                  onClick = {
                    completeFoul(currentStep.team, currentStep.player, FoulSeverity.Minor(type))
                  }
                ) {
                  Text(type.label)
                }
              }
            }
          is FoulDialogStep.ChooseMajorType ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              MajorFoulType.entries.forEach { type ->
                TextButton(
                  onClick = {
                    step =
                      FoulDialogStep.ChooseFoulDuration(currentStep.team, currentStep.player, type)
                  }
                ) {
                  Text(type.label)
                }
              }
            }
          is FoulDialogStep.ChooseFoulDuration ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              FoulDuration.entries.forEach { duration ->
                TextButton(
                  onClick = {
                    completeFoul(
                      currentStep.team,
                      currentStep.player,
                      FoulSeverity.Major(currentStep.type, duration),
                    )
                  }
                ) {
                  Text(duration.label)
                }
              }
            }
          is FoulDialogStep.ConfirmMore ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(
                onClick = {
                  playerText = ""
                  step = FoulDialogStep.ChooseTeam
                }
              ) {
                Text("Add another foul")
              }
              TextButton(onClick = ::commitPendingBatchAndDismiss) { Text("Done") }
            }
          is FoulDialogStep.ConfirmCancelChoice ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              TextButton(onClick = { step = FoulDialogStep.ConfirmMore }) {
                Text("Cancel only this foul")
              }
              TextButton(onClick = onDismiss) { Text("Cancel all") }
            }
        }
      }
    },
    confirmButton = {
      val currentStep = step
      if (currentStep is FoulDialogStep.EnterPlayer) {
        TextButton(
          onClick = { player?.let { step = FoulDialogStep.ChooseSeverity(currentStep.team, it) } },
          enabled = player != null,
        ) {
          Text("Enter")
        }
      }
    },
    dismissButton = {
      val kind = step.kind()
      if (kind != FoulDialogStepKind.BATCH_CONFIRMATION) {
        TextButton(onClick = ::performBackAction) {
          Text(if (kind == FoulDialogStepKind.FIRST) "Cancel" else "Back")
        }
      }
    },
  )
}

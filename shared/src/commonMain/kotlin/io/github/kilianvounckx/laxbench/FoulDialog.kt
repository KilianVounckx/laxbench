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
  }

/**
 * A multi-step pop-up for recording a foul, analogous to [GoalDialog] but with more steps: which
 * team committed the foul, the offending player's number, and the foul's severity (with a further
 * specific-type step for [FoulSeverity.Minor]/[FoulSeverity.Major], since [FoulSeverity.Expulsion]
 * has no sub-type of its own, and, for [FoulSeverity.Major] only, one more step after that to
 * choose the penalty duration). Shown when the "Foul" button is tapped (see [GameScreen]).
 *
 * [onConfirm] is invoked exactly once, as soon as enough has been chosen to build a complete
 * [FoulSeverity]: immediately for [FoulSeverity.Expulsion]; after its specific type is picked for
 * [FoulSeverity.Minor]; and only once both its specific type and its penalty duration are picked
 * for [FoulSeverity.Major] -- always together with the team and player number collected in the
 * earlier steps. There is no confirmation/summary step afterwards; saving and closing always happen
 * together.
 *
 * On the first step, the dialog's secondary button reads "Cancel" and invokes [onDismiss],
 * discarding everything collected so far and closing the pop-up. On every later step, that same
 * button instead reads "Back" and returns to the immediately preceding step, keeping whatever was
 * already entered for it (see [FoulDialogStep.previous]). Regardless of step, [onDismiss] is also
 * always invoked if the dialog is dismissed by tapping outside it or via a system back gesture --
 * that always fully closes the pop-up, never just goes back a step.
 */
@Composable
fun FoulDialog(
  teams: TeamsInfo,
  onConfirm: (team: ScoreViewModel.Team, player: PlayerNumber, severity: FoulSeverity) -> Unit,
  onDismiss: () -> Unit,
) {
  var step by remember { mutableStateOf<FoulDialogStep>(FoulDialogStep.ChooseTeam) }
  var playerText by remember { mutableStateOf("") }
  val player = PlayerNumber.parse(playerText)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record foul") },
    text = {
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
              onClick = { onConfirm(currentStep.team, currentStep.player, FoulSeverity.Expulsion) }
            ) {
              Text("Expulsion")
            }
          }
        is FoulDialogStep.ChooseMinorType ->
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MinorFoulType.entries.forEach { type ->
              TextButton(
                onClick = {
                  onConfirm(currentStep.team, currentStep.player, FoulSeverity.Minor(type))
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
                  onConfirm(
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
      val previousStep = step.previous()
      TextButton(onClick = { previousStep?.let { step = it } ?: onDismiss() }) {
        Text(if (previousStep == null) "Cancel" else "Back")
      }
    },
  )
}

package io.github.kilianvounckx.laxbench

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
import io.github.kilianvounckx.laxbench.domain.PlayerNumber

/**
 * A pop-up prompting for the player number of a goal's scorer (required) and, optionally, of the
 * assisting player, shown when a team's score number is tapped (see [App]).
 *
 * Both fields hold free-form text so the user can type and edit them freely (including clearing a
 * field back to blank); [PlayerNumber.parse] is applied to each on every change purely to decide
 * whether confirming is currently possible, not to constrain what can be typed. Confirming is only
 * enabled once the scorer field parses to a valid [PlayerNumber] and the assist field is either
 * blank (meaning no assist) or itself parses to a valid [PlayerNumber]; while disabled, tapping the
 * confirm button does nothing, so [onConfirm] is only ever invoked with a valid scorer and either
 * `null` or a valid assist -- never with an invalid/missing scorer. Dismissing the dialog -- via
 * the cancel button, tapping outside it, or a system back gesture -- invokes [onDismiss] instead,
 * with no other effect.
 */
@Composable
fun GoalDialog(
  onConfirm: (scorer: PlayerNumber, assist: PlayerNumber?) -> Unit,
  onDismiss: () -> Unit,
) {
  var scorerText by remember { mutableStateOf("") }
  var assistText by remember { mutableStateOf("") }

  val scorer = PlayerNumber.parse(scorerText)
  val assistIsBlank = assistText.isBlank()
  val assist = if (assistIsBlank) null else PlayerNumber.parse(assistText)
  val canConfirm = scorer != null && (assistIsBlank || assist != null)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record goal") },
    text = {
      Column {
        OutlinedTextField(
          value = scorerText,
          onValueChange = { scorerText = it },
          label = { Text("Scorer number") },
          isError = scorerText.isNotBlank() && scorer == null,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )
        OutlinedTextField(
          value = assistText,
          onValueChange = { assistText = it },
          label = { Text("Assist number (optional)") },
          isError = !assistIsBlank && assist == null,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
        )
      }
    },
    confirmButton = {
      TextButton(onClick = { scorer?.let { onConfirm(it, assist) } }, enabled = canConfirm) {
        Text("Confirm")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

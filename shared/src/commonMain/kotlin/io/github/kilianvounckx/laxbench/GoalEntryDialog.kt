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
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.PlayerNumber

@Composable
fun GoalEntryDialog(
  title: String,
  initialScorerText: String,
  initialAssistText: String,
  initialElapsedTime: ElapsedTime,
  onConfirm: (scorer: PlayerNumber, assist: PlayerNumber?, elapsedTime: ElapsedTime) -> Unit,
  onDismiss: () -> Unit,
) {
  var scorerText by remember { mutableStateOf(initialScorerText) }
  var assistText by remember { mutableStateOf(initialAssistText) }
  var elapsedTime by remember { mutableStateOf(initialElapsedTime) }

  val scorer = if (scorerText.isBlank()) null else PlayerNumber.parse(scorerText)
  val assist = if (assistText.isBlank()) null else PlayerNumber.parse(assistText)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        OutlinedTextField(
          value = scorerText,
          onValueChange = { scorerText = it },
          label = { Text("Scorer") },
          singleLine = true,
          isError = scorerText.isNotBlank() && scorer == null,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          supportingText = {
            if (scorerText.isNotBlank() && scorer == null) {
              Text("Enter a valid player number")
            }
          },
        )
        OutlinedTextField(
          value = assistText,
          onValueChange = { assistText = it },
          label = { Text("Assist (optional)") },
          singleLine = true,
          isError = assistText.isNotBlank() && assist == null,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          supportingText = {
            if (assistText.isNotBlank() && assist == null) {
              Text("Enter a valid player number or leave blank")
            }
          },
        )
        ElapsedTimeField(value = elapsedTime, onValueChange = { elapsedTime = it })
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (scorer != null) {
            onConfirm(scorer, assist, elapsedTime)
          }
        },
        enabled = scorer != null,
      ) {
        Text("Confirm")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.kilianvounckx.laxbench.domain.ElapsedTime

@Composable
fun ElapsedTimeEntryDialog(
  title: String,
  initialElapsedTime: ElapsedTime,
  onConfirm: (ElapsedTime) -> Unit,
  onDismiss: () -> Unit,
) {
  var elapsedTime by remember { mutableStateOf(initialElapsedTime) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column { ElapsedTimeField(value = elapsedTime, onValueChange = { elapsedTime = it }) }
    },
    confirmButton = { TextButton(onClick = { onConfirm(elapsedTime) }) { Text("Confirm") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

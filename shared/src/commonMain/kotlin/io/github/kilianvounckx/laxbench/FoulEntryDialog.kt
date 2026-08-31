package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FoulDuration
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.MajorFoulType
import io.github.kilianvounckx.laxbench.domain.MinorFoulType
import io.github.kilianvounckx.laxbench.domain.PlayerNumber

private enum class FoulSeverityKind {
  MINOR,
  MAJOR,
  EXPULSION,
}

@Composable
fun FoulEntryDialog(
  title: String,
  initialPlayerText: String,
  initialSeverity: FoulSeverity?,
  initialElapsedTime: ElapsedTime,
  onConfirm: (player: PlayerNumber, severity: FoulSeverity, elapsedTime: ElapsedTime) -> Unit,
  onDismiss: () -> Unit,
) {
  var playerText by remember { mutableStateOf(initialPlayerText) }
  var kind by remember {
    mutableStateOf(
      when (initialSeverity) {
        is FoulSeverity.Minor -> FoulSeverityKind.MINOR
        is FoulSeverity.Major -> FoulSeverityKind.MAJOR
        FoulSeverity.Expulsion -> FoulSeverityKind.EXPULSION
        null -> null
      }
    )
  }
  var minorType by remember { mutableStateOf((initialSeverity as? FoulSeverity.Minor)?.type) }
  var majorType by remember { mutableStateOf((initialSeverity as? FoulSeverity.Major)?.type) }
  var duration by remember { mutableStateOf((initialSeverity as? FoulSeverity.Major)?.duration) }
  var elapsedTime by remember { mutableStateOf(initialElapsedTime) }

  val player = if (playerText.isBlank()) null else PlayerNumber.parse(playerText)
  val severity =
    when {
      kind == FoulSeverityKind.MINOR && minorType != null -> FoulSeverity.Minor(minorType!!)
      kind == FoulSeverityKind.MAJOR && majorType != null && duration != null ->
        FoulSeverity.Major(majorType!!, duration!!)
      kind == FoulSeverityKind.EXPULSION -> FoulSeverity.Expulsion
      else -> null
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Column {
        OutlinedTextField(
          value = playerText,
          onValueChange = { playerText = it },
          label = { Text("Player number") },
          singleLine = true,
          isError = playerText.isNotBlank() && player == null,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          supportingText = {
            if (playerText.isNotBlank() && player == null) {
              Text("Enter a valid player number")
            }
          },
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          SelectableOptionButton(kind == FoulSeverityKind.MINOR, "Minor") {
            kind = FoulSeverityKind.MINOR
          }
          SelectableOptionButton(kind == FoulSeverityKind.MAJOR, "Major") {
            kind = FoulSeverityKind.MAJOR
          }
          SelectableOptionButton(kind == FoulSeverityKind.EXPULSION, "Expulsion") {
            kind = FoulSeverityKind.EXPULSION
          }
        }

        if (kind == FoulSeverityKind.MINOR) {
          Column {
            for (type in MinorFoulType.entries) {
              SelectableOptionButton(minorType == type, type.label) { minorType = type }
            }
          }
        }

        if (kind == FoulSeverityKind.MAJOR) {
          Column {
            Text("Foul type")
            for (type in MajorFoulType.entries) {
              SelectableOptionButton(majorType == type, type.label) { majorType = type }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Duration")
            for (dur in FoulDuration.entries) {
              SelectableOptionButton(duration == dur, dur.label) { duration = dur }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ElapsedTimeField(value = elapsedTime, onValueChange = { elapsedTime = it })
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (player != null && severity != null) {
            onConfirm(player, severity, elapsedTime)
          }
        },
        enabled = player != null && severity != null,
      ) {
        Text("Confirm")
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
  )
}

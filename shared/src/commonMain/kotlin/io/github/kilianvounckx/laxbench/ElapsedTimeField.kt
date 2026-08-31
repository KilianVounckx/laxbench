package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.maskedEdit

/**
 * A masked "MM:SS.DD" elapsed-time entry field, shared by every Manage Game dialog that lets the
 * scorekeeper type an elapsed time by hand ([GoalEntryDialog], [FoulEntryDialog],
 * [ElapsedTimeEntryDialog]) instead of duplicating the same masking behavior three times. The
 * displayed text is always exactly [value]'s [ElapsedTime.format] output, with the text cursor
 * pinned to the very end on every recomposition, so typing/backspacing always happens at the end;
 * see [ElapsedTime.maskedEdit] for the exact digit-shifting semantics. Since every value this field
 * can ever produce is already a valid [ElapsedTime], callers no longer need to separately
 * parse/validate it the way the old free-text field required.
 */
@Composable
fun ElapsedTimeField(
  value: ElapsedTime,
  onValueChange: (ElapsedTime) -> Unit,
  label: String = "Elapsed time",
) {
  val displayText = value.format()
  OutlinedTextField(
    value = TextFieldValue(text = displayText, selection = TextRange(displayText.length)),
    onValueChange = { new ->
      val updated = value.maskedEdit(displayText, new.text)
      if (updated != value) onValueChange(updated)
    },
    label = { Text(label) },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
  )
}

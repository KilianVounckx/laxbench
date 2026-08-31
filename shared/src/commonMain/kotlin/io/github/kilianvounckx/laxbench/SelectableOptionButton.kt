package io.github.kilianvounckx.laxbench

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * A toggle-style button used wherever choosing one option among several, or a collapsed/expanded
 * toggle, is expressed as emphasis rather than a checkbox or arrow glyph: a filled [Button] while
 * [selected], an [OutlinedButton] otherwise. Used by [FoulEntryDialog] for its severity-kind/type/
 * duration option rows, and by [ManageGameScreen] for each section's collapse/expand toggle -- both
 * are the same "selected vs. not" visual idiom applied to two different use cases (picking a value;
 * toggling a section open), so it is defined once here instead of being reimplemented per file.
 */
@Composable
internal fun SelectableOptionButton(selected: Boolean, label: String, onClick: () -> Unit) {
  if (selected) {
    Button(onClick = onClick) { Text(label) }
  } else {
    OutlinedButton(onClick = onClick) { Text(label) }
  }
}

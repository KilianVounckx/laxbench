package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A vertically scrollable [Column], used for every dialog's `text` slot that contains at least one
 * text input field (see [GoalDialog], [GoalEntryDialog], [FoulDialog], [FoulEntryDialog], and
 * [ElapsedTimeEntryDialog]). Material3's `AlertDialog` constrains its content to fit on screen but
 * does not itself scroll that content, so once the on-screen keyboard shrinks the available space
 * (see `index.html`/`styles.css` for the corresponding browser-side viewport fix), content taller
 * than what remains would otherwise be clipped with no way to reach it; wrapping it here instead
 * lets the user scroll to reach any field or the dialog's confirm/dismiss buttons. Has no visible
 * effect, and does not scroll, when content already fits the available height.
 */
@Composable
fun ScrollableDialogContent(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(modifier = modifier.verticalScroll(rememberScrollState()), content = content)
}

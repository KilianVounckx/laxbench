package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

/**
 * Lists every player, across both teams, who currently has at least one active/queued foul timer
 * (see [FoulTimerViewModel.remainingTimes]), each showing that player's label (see
 * [TeamsInfo.label]) and single combined total-remaining-time countdown. Tapping an entry invokes
 * [onPlayerTapped] to open the cancel pop-up for that player (see [CancelFoulTimersDialog]). Rows
 * are sorted by team (home before visiting), then ascending player number, for a stable order as
 * timers come and go.
 */
@Composable
internal fun CurrentFoulsScreen(
  teams: TeamsInfo,
  remainingTimes: Map<FoulTimerPlayer, ElapsedTime>,
  onPlayerTapped: (FoulTimerPlayer) -> Unit,
  onBack: () -> Unit,
) {
  Column(
    modifier = Modifier.safeContentPadding().fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text("Current fouls", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(16.dp))
    remainingTimes.entries
      .sortedWith(compareBy({ it.key.team }, { it.key.player.number }))
      .forEach { (key, remaining) ->
        TextButton(onClick = { onPlayerTapped(key) }) {
          Text("${teams.label(key)} — ${remaining.format()}")
        }
      }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onBack) { Text("Back") }
  }
}

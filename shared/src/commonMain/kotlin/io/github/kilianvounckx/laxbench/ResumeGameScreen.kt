package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

@Composable
fun ResumeGameScreen(savedTeams: TeamsInfo, onContinue: () -> Unit, onNewGame: () -> Unit) {
  Column(
    modifier = Modifier.safeContentPadding().fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text =
        "There is already a saved game on this device. Do you want to continue " +
          "${savedTeams.home.name.value} vs ${savedTeams.visiting.name.value} or start a new game? " +
          "Starting a new game will overwrite the saved game.",
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = onContinue) { Text("Continue") }
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = onNewGame) { Text("New game") }
  }
}

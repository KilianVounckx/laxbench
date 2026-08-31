package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kilianvounckx.laxbench.domain.TeamColor
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamName
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

/**
 * The first screen shown on launch, collecting team names and colors for the "home" and "visiting"
 * sides. All fields start empty. Invokes [onStartGame] when "Start game" is pressed with a fresh,
 * fully validated [TeamsInfo].
 */
@Composable
fun SetupScreen(onStartGame: (TeamsInfo) -> Unit) {
  var homeNameText by remember { mutableStateOf("") }
  var homeColorText by remember { mutableStateOf("") }
  var visitingNameText by remember { mutableStateOf("") }
  var visitingColorText by remember { mutableStateOf("") }

  val homeName = TeamName.parse(homeNameText)
  val homeColor = TeamColor.parse(homeColorText)
  val visitingName = TeamName.parse(visitingNameText)
  val visitingColor = TeamColor.parse(visitingColorText)

  Column(
    modifier = Modifier.safeContentPadding().fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text("Home team", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
      value = homeNameText,
      onValueChange = { homeNameText = it },
      label = { Text("Team name") },
      singleLine = true,
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = homeColorText,
      onValueChange = { homeColorText = it },
      label = { Text("Team color") },
      singleLine = true,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text("Visiting team", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(
      value = visitingNameText,
      onValueChange = { visitingNameText = it },
      label = { Text("Team name") },
      singleLine = true,
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = visitingColorText,
      onValueChange = { visitingColorText = it },
      label = { Text("Team color") },
      singleLine = true,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
      onClick = {
        onStartGame(
          TeamsInfo(
            home = TeamInfo(homeName!!, homeColor!!),
            visiting = TeamInfo(visitingName!!, visitingColor!!),
          )
        )
      },
      enabled =
        homeName != null && homeColor != null && visitingName != null && visitingColor != null,
    ) {
      Text("Start game")
    }
  }
}

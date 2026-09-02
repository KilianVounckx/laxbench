package io.github.kilianvounckx.laxbench

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import io.github.kilianvounckx.laxbench.domain.TeamsInfo

private sealed interface Screen {
  data object Setup : Screen

  data class Game(val teams: TeamsInfo, val viewModelStoreOwner: GameViewModelStoreOwner) : Screen
}

/**
 * Owns the [ViewModelStore] for exactly one game session: every per-game ViewModel (score, fouls,
 * saves, face-offs, time-outs, the timer, the time-out countdown, and the intermission countdown)
 * is requested from this store while that particular game is active. [App] creates a fresh instance
 * each time "Start game" is pressed. The store is never cleared and lives for the rest of the
 * process once a game starts, consistent with there being no way to end a game and return to setup.
 */
private class GameViewModelStoreOwner : ViewModelStoreOwner {
  override val viewModelStore: ViewModelStore = ViewModelStore()
}

@Composable
@Preview
fun App() {
  MaterialTheme {
    var screen by remember { mutableStateOf<Screen>(Screen.Setup) }

    when (val current = screen) {
      Screen.Setup ->
        SetupScreen(
          onStartGame = { teams -> screen = Screen.Game(teams, GameViewModelStoreOwner()) }
        )
      is Screen.Game ->
        GameScreen(initialTeams = current.teams, viewModelStoreOwner = current.viewModelStoreOwner)
    }
  }
}

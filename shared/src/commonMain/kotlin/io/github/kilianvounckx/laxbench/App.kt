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
  data class Setup(val prefill: TeamsInfo?) : Screen

  data class Game(val teams: TeamsInfo, val viewModelStoreOwner: GameViewModelStoreOwner) : Screen
}

/**
 * Owns the [ViewModelStore] for exactly one game session: every per-game ViewModel (score, fouls,
 * saves, face-offs, time-outs, the timer, and the time-out countdown) is requested from this store
 * while that particular game is active. [App] creates a fresh instance each time "Start game" is
 * pressed and calls `viewModelStore.clear()` on the previous one when a session ends (leaving via
 * "Back to setup", right before starting the next game) — that is what makes every new game start
 * with completely fresh state and stops the previous game's background work (e.g. the timer's
 * ticking coroutine) instead of leaking it.
 */
private class GameViewModelStoreOwner : ViewModelStoreOwner {
  override val viewModelStore: ViewModelStore = ViewModelStore()
}

@Composable
@Preview
fun App() {
  MaterialTheme {
    var screen by remember { mutableStateOf<Screen>(Screen.Setup(prefill = null)) }

    when (val current = screen) {
      is Screen.Setup ->
        SetupScreen(
          prefill = current.prefill,
          onStartGame = { teams -> screen = Screen.Game(teams, GameViewModelStoreOwner()) },
        )
      is Screen.Game ->
        GameScreen(
          teams = current.teams,
          viewModelStoreOwner = current.viewModelStoreOwner,
          onBackToSetup = {
            current.viewModelStoreOwner.viewModelStore.clear()
            screen = Screen.Setup(prefill = current.teams)
          },
        )
    }
  }
}

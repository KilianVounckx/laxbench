package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.Saves
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [Save]s for the rest of the app session, the
 * same in-memory-only, non-persisted way [FoulViewModel] and [TimeOutViewModel] hold each team's
 * history. There is no requirement to display save counts/history anywhere in the main UI and no
 * undo/correction mechanism for a mistakenly recorded save -- so, like [TimeOutViewModel], this
 * class exposes no `StateFlow` and no decrement/removal operation: [recordSave] is the only way to
 * change either team's history, and [printDebugSummary] is the only way to observe it.
 *
 * This class reuses [ScoreViewModel.Team] to identify which team's goalie made a save, rather than
 * defining a fourth, parallel team enum -- this [SaveViewModel], [TimeOutViewModel],
 * [FoulViewModel], and [ScoreViewModel] each independently track different per-team data about the
 * exact same two sides of the same game.
 *
 * As with [ScoreViewModel], [FoulViewModel], [TimeOutViewModel], and [TimerViewModel], a fresh
 * instance (obtained the same way, via the Compose Multiplatform `viewModel()` API) always starts
 * both histories empty, and this class does not persist across process death.
 */
class SaveViewModel : ViewModel() {

  private val _homeSaves = MutableStateFlow(Saves.empty)
  private val _visitingSaves = MutableStateFlow(Saves.empty)

  /** Records [save] for [team], appending it to that team's save history. */
  fun recordSave(team: ScoreViewModel.Team, save: Save) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeSaves.update { it.recorded(save) }
      ScoreViewModel.Team.VISITING -> _visitingSaves.update { it.recorded(save) }
    }
  }

  /** Prints both teams' recorded save histories, for debugging. */
  fun printDebugSummary() {
    println("Home saves: ${_homeSaves.value}")
    println("Visiting saves: ${_visitingSaves.value}")
  }
}

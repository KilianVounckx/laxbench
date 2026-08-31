package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.Saves
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [Save]s for the rest of the app session, the
 * same in-memory-only, non-persisted way [FoulViewModel] and [TimeOutViewModel] hold each team's
 * history. Each save gets a unique id when recorded. [recordSave] is the only way to add a save,
 * always appending it as the newest entry with a unique id. [removeSave] removes a specific save by
 * id. [printDebugSummary] prints both teams' histories at once for debugging, and [saves] returns a
 * single team's current history as a reactive [StateFlow].
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

  private var nextId = 0L

  /**
   * Records a save for [team] at the given [elapsedTime], appending it to that team's save history.
   */
  fun recordSave(team: ScoreViewModel.Team, elapsedTime: ElapsedTime) {
    val save = Save(id = nextId++, elapsedTime = elapsedTime)
    when (team) {
      ScoreViewModel.Team.HOME -> _homeSaves.update { it.recorded(save) }
      ScoreViewModel.Team.VISITING -> _visitingSaves.update { it.recorded(save) }
    }
  }

  /** Removes the save identified by [id] for [team]. This is a no-op if that id is not present. */
  fun removeSave(team: ScoreViewModel.Team, id: Long) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeSaves.update { it.removed(id) }
      ScoreViewModel.Team.VISITING -> _visitingSaves.update { it.removed(id) }
    }
  }

  /** Returns the save history for the given [team]. */
  fun saves(team: ScoreViewModel.Team): StateFlow<Saves> =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeSaves.asStateFlow()
      ScoreViewModel.Team.VISITING -> _visitingSaves.asStateFlow()
    }

  /** Prints both teams' recorded save histories, for debugging. */
  fun printDebugSummary() {
    println("Home saves: ${_homeSaves.value}")
    println("Visiting saves: ${_visitingSaves.value}")
  }
}

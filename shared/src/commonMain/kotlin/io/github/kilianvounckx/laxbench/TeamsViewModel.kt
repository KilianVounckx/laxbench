package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the live, editable team info for a game session: name and color for home and visiting
 * teams. Created once per game at the same time as every other per-game ViewModel, initialized with
 * the [TeamsInfo] the game was started with, and updated reactively whenever Manage Game changes a
 * team's details. Edits to team info never touch the game clock, score, foul timers, or any
 * recorded history -- this is the single source of truth for team info once a game has started,
 * separate from and independent of all other game state.
 */
class TeamsViewModel(initial: TeamsInfo) : ViewModel() {
  private val _teamsInfo = MutableStateFlow(initial)
  val teamsInfo: StateFlow<TeamsInfo> = _teamsInfo.asStateFlow()

  /** Updates the info for [team], without affecting any other game state. */
  fun update(team: ScoreViewModel.Team, info: TeamInfo) {
    _teamsInfo.update { current ->
      when (team) {
        ScoreViewModel.Team.HOME -> current.copy(home = info)
        ScoreViewModel.Team.VISITING -> current.copy(visiting = info)
      }
    }
  }
}

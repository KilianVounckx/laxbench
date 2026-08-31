package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [Foul]s for the rest of the app session, the
 * same in-memory-only, non-persisted way [ScoreViewModel] holds each team's goal history. Each foul
 * gets a unique id when recorded, used to identify it in edit/delete operations and linked to its
 * matching foul-timer entry so the timer can be adjusted or cancelled by Manage Game.
 *
 * [recordFoul] is the only way to add a foul, always appending it as the newest entry with a unique
 * id and returning the created [Foul] so the caller can link it to a foul-timer entry with the same
 * id. [updateFoul] updates a foul's details without changing anything else. [removeFoul] removes a
 * specific foul by id. [printDebugSummary] prints both teams' histories at once for debugging, and
 * [fouls] returns a single team's current history as a reactive [StateFlow].
 *
 * This class reuses [ScoreViewModel.Team] to identify which side committed a foul, rather than
 * defining a second, parallel team enum -- this [FoulViewModel] and [ScoreViewModel] independently
 * track different per-team data (foul history here, score/goal history there) about the exact same
 * two sides of the same game.
 *
 * As with [ScoreViewModel] and [TimerViewModel], a fresh instance (obtained the same way, via the
 * Compose Multiplatform `viewModel()` API) always starts both histories empty, and this class does
 * not persist across process death.
 */
class FoulViewModel : ViewModel() {

  private val _homeFouls = MutableStateFlow(Fouls.empty)
  private val _visitingFouls = MutableStateFlow(Fouls.empty)

  private var nextId = 0L

  /**
   * Records a foul for [team], appending it to that team's foul history with a unique id, and
   * returns the created [Foul] (whose id is used to link a matching foul-timer entry).
   */
  fun recordFoul(
    team: ScoreViewModel.Team,
    player: PlayerNumber,
    severity: FoulSeverity,
    elapsedTime: ElapsedTime,
  ): Foul {
    val foul = Foul(id = nextId++, player = player, severity = severity, elapsedTime = elapsedTime)
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.update { it.recorded(foul) }
      ScoreViewModel.Team.VISITING -> _visitingFouls.update { it.recorded(foul) }
    }
    return foul
  }

  /**
   * Updates the foul identified by [edited.id] for [team]. This is a no-op if that id is not
   * present.
   */
  fun updateFoul(team: ScoreViewModel.Team, edited: Foul) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.update { it.updated(edited) }
      ScoreViewModel.Team.VISITING -> _visitingFouls.update { it.updated(edited) }
    }
  }

  /** Removes the foul identified by [id] for [team]. This is a no-op if that id is not present. */
  fun removeFoul(team: ScoreViewModel.Team, id: Long) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.update { it.removed(id) }
      ScoreViewModel.Team.VISITING -> _visitingFouls.update { it.removed(id) }
    }
  }

  /** Returns the foul history for the given [team]. */
  fun fouls(team: ScoreViewModel.Team): StateFlow<Fouls> =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.asStateFlow()
      ScoreViewModel.Team.VISITING -> _visitingFouls.asStateFlow()
    }

  /** Prints both teams' recorded foul histories, for debugging. */
  fun printDebugSummary() {
    println("Home fouls: ${_homeFouls.value}")
    println("Visiting fouls: ${_visitingFouls.value}")
  }
}

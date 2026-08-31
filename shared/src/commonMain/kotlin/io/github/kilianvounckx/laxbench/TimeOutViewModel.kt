package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.TimeOut
import io.github.kilianvounckx.laxbench.domain.TimeOuts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [TimeOut] requests for the rest of the app
 * session, the same in-memory-only, non-persisted way [FoulViewModel] holds each team's foul
 * history. Each time-out gets a unique id when recorded. [recordTimeOut] is the only way to add a
 * time-out, always appending it as the newest entry with a unique id. [removeTimeOut] removes a
 * specific time-out by id. [printDebugSummary] prints both teams' histories at once for debugging,
 * and [timeOuts] returns a single team's current history as a reactive [StateFlow].
 *
 * This class reuses [ScoreViewModel.Team] to identify which side requested a time-out, rather than
 * defining a third, parallel team enum -- this [TimeOutViewModel], [FoulViewModel], and
 * [ScoreViewModel] each independently track different per-team data about the exact same two sides
 * of the same game.
 *
 * As with [ScoreViewModel], [FoulViewModel], and [TimerViewModel], a fresh instance (obtained the
 * same way, via the Compose Multiplatform `viewModel()` API) always starts both histories empty,
 * and this class does not persist across process death.
 */
class TimeOutViewModel : ViewModel() {

  private val _homeTimeOuts = MutableStateFlow(TimeOuts.empty)
  private val _visitingTimeOuts = MutableStateFlow(TimeOuts.empty)

  private var nextId = 0L

  /**
   * Records a time-out for [team] at the given [elapsedTime], appending it to that team's time-out
   * history.
   */
  fun recordTimeOut(team: ScoreViewModel.Team, elapsedTime: ElapsedTime) {
    val timeOut = TimeOut(id = nextId++, elapsedTime = elapsedTime)
    when (team) {
      ScoreViewModel.Team.HOME -> _homeTimeOuts.update { it.recorded(timeOut) }
      ScoreViewModel.Team.VISITING -> _visitingTimeOuts.update { it.recorded(timeOut) }
    }
  }

  /**
   * Removes the time-out identified by [id] for [team]. This is a no-op if that id is not present.
   */
  fun removeTimeOut(team: ScoreViewModel.Team, id: Long) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeTimeOuts.update { it.removed(id) }
      ScoreViewModel.Team.VISITING -> _visitingTimeOuts.update { it.removed(id) }
    }
  }

  /** Returns the time-out history for the given [team]. */
  fun timeOuts(team: ScoreViewModel.Team): StateFlow<TimeOuts> =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeTimeOuts.asStateFlow()
      ScoreViewModel.Team.VISITING -> _visitingTimeOuts.asStateFlow()
    }

  /** Prints both teams' recorded time-out histories, for debugging. */
  fun printDebugSummary() {
    println("Home time-outs: ${_homeTimeOuts.value}")
    println("Visiting time-outs: ${_visitingTimeOuts.value}")
  }
}

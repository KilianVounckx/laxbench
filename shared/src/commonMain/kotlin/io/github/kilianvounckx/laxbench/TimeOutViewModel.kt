package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.TimeOut
import io.github.kilianvounckx.laxbench.domain.TimeOuts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [TimeOut] requests for the rest of the app
 * session, the same in-memory-only, non-persisted way [FoulViewModel] holds each team's foul
 * history. There is no requirement to display time-out counts/history anywhere in the main UI, no
 * maximum-number-of-time-outs rule to enforce, and no undo/correction mechanism for a mistakenly
 * recorded time-out -- so, like [FoulViewModel], this class exposes no reactive `StateFlow` and no
 * decrement/removal operation: [recordTimeOut] is the only way to change either team's history.
 * Reading it back is always a one-shot snapshot, never a subscription: [printDebugSummary] prints
 * both teams' histories at once for debugging, and [timeOuts] returns a single team's current
 * history on demand (e.g. for building an exported scoresheet).
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

  /** Records [timeOut] for [team], appending it to that team's time-out history. */
  fun recordTimeOut(team: ScoreViewModel.Team, timeOut: TimeOut) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeTimeOuts.update { it.recorded(timeOut) }
      ScoreViewModel.Team.VISITING -> _visitingTimeOuts.update { it.recorded(timeOut) }
    }
  }

  /** Returns the time-out history for the given [team]. */
  fun timeOuts(team: ScoreViewModel.Team): TimeOuts =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeTimeOuts.value
      ScoreViewModel.Team.VISITING -> _visitingTimeOuts.value
    }

  /** Prints both teams' recorded time-out histories, for debugging. */
  fun printDebugSummary() {
    println("Home time-outs: ${_homeTimeOuts.value}")
    println("Visiting time-outs: ${_visitingTimeOuts.value}")
  }
}

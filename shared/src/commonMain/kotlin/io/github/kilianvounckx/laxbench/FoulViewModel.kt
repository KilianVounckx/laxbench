package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.Fouls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [Foul]s for the rest of the app session, the
 * same in-memory-only, non-persisted way [ScoreViewModel] holds each team's goal history. There is
 * no requirement to display foul counts/history anywhere in the main UI, and no undo/correction
 * mechanism for a mistakenly recorded foul -- so unlike [ScoreViewModel] this class exposes no
 * reactive `StateFlow` and no decrement/removal operation: [recordFoul] is the only way to change
 * either team's history. Reading it back is always a one-shot snapshot, never a subscription:
 * [printDebugSummary] prints both teams' histories at once for debugging, and [fouls] returns a
 * single team's current history on demand (e.g. for building an exported scoresheet).
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

  /** Records [foul] for [team], appending it to that team's foul history. */
  fun recordFoul(team: ScoreViewModel.Team, foul: Foul) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.update { it.recorded(foul) }
      ScoreViewModel.Team.VISITING -> _visitingFouls.update { it.recorded(foul) }
    }
  }

  /** Returns the foul history for the given [team]. */
  fun fouls(team: ScoreViewModel.Team): Fouls =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFouls.value
      ScoreViewModel.Team.VISITING -> _visitingFouls.value
    }

  /** Prints both teams' recorded foul histories, for debugging. */
  fun printDebugSummary() {
    println("Home fouls: ${_homeFouls.value}")
    println("Visiting fouls: ${_visitingFouls.value}")
  }
}

package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.Score
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Publishes the two independent running goal tallies shown on the score tracker: [ourScore] for our
 * team and [opponentScore] for the opponent. Each tally starts at [Score.zero] when this
 * [ScoreViewModel] is created and changes only one goal at a time, via
 * [incrementOurScore]/[decrementOurScore] and [incrementOpponentScore]/[decrementOpponentScore].
 * All of the "never below zero" bookkeeping is done by [Score], a pure domain type; this class only
 * holds the current [Score] for each side and republishes it immediately on every change, in the
 * same StateFlow-based style [TimerViewModel] uses for [TimerViewModel.elapsedTime] and
 * [TimerViewModel.runState].
 *
 * This class is entirely independent of [TimerViewModel]: nothing here reads or writes the timer's
 * state, and nothing in [TimerViewModel] reads or writes this class's state, so scoring and the
 * game clock can never affect each other. It does not persist across process death, matching the
 * timer's existing non-persistence behavior; a fresh instance (obtained the same way as
 * [TimerViewModel], via the Compose Multiplatform `viewModel()` API) always starts both tallies at
 * zero.
 */
class ScoreViewModel : ViewModel() {

  private val _ourScore = MutableStateFlow(Score.zero)
  val ourScore: StateFlow<Score> = _ourScore.asStateFlow()

  private val _opponentScore = MutableStateFlow(Score.zero)
  val opponentScore: StateFlow<Score> = _opponentScore.asStateFlow()

  /** Records a goal for our team: increments [ourScore] by one. */
  fun incrementOurScore() {
    _ourScore.update { it.incremented() }
  }

  /**
   * Corrects a mistaken tap for our team: decrements [ourScore] by one, or leaves it at zero if it
   * is already zero.
   */
  fun decrementOurScore() {
    _ourScore.update { it.decremented() }
  }

  /** Records a goal for the opponent: increments [opponentScore] by one. */
  fun incrementOpponentScore() {
    _opponentScore.update { it.incremented() }
  }

  /**
   * Corrects a mistaken tap for the opponent: decrements [opponentScore] by one, or leaves it at
   * zero if it is already zero.
   */
  fun decrementOpponentScore() {
    _opponentScore.update { it.decremented() }
  }
}

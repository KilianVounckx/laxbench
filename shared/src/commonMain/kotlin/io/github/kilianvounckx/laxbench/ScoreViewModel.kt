package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.Score
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Publishes the two independent running goal tallies shown on the score tracker: [ourScore] for our
 * team and [opponentScore] for the opponent. Each tally starts at [Score.zero] when this
 * [ScoreViewModel] is created. Alongside each tally, this class privately keeps that side's full
 * history of recorded [Goal]s (starting at [Goals.empty]), so that a long-press correction knows
 * exactly which goal to remove.
 *
 * A score and its goal history always change together and stay in lockstep: [recordGoal] is the
 * only way to increment a score, and it always also appends the same [Goal] to that side's history;
 * [decrementScore] is the only way to decrement a score, and it always also removes that side's
 * most recently recorded goal. Because every increment goes through [recordGoal], a side's score
 * count and the size of its goal history are always equal, so [decrementScore] never has a score
 * left to decrement without a matching goal left to remove, or vice versa: both operations are
 * simply a no-op on the side that is already empty/zero. All of the "never below zero" and
 * "append/remove at the end" bookkeeping is done by [Score] and [Goals], pure domain types; this
 * class only holds the current value of each and republishes the scores immediately on every
 * change, in the same StateFlow-based style [TimerViewModel] uses for [TimerViewModel.elapsedTime]
 * and [TimerViewModel.runState].
 *
 * This class is entirely independent of [TimerViewModel]: it never reads the timer's state itself.
 * The [Goal] passed to [recordGoal] already carries whatever elapsed time the caller captured when
 * the goal-recording pop-up was opened (at tap time, not at confirmation time), so scoring and the
 * game clock still can never affect each other from within this class. It does not persist across
 * process death, matching the timer's existing non-persistence behavior; a fresh instance (obtained
 * the same way as [TimerViewModel], via the Compose Multiplatform `viewModel()` API) always starts
 * both tallies at zero and both goal histories empty.
 */
class ScoreViewModel : ViewModel() {

  /** Which side of the score tracker a goal, or a correction, belongs to. */
  enum class Team {
    HOME,
    VISITING,
  }

  private val _ourScore = MutableStateFlow(Score.zero)
  val ourScore: StateFlow<Score> = _ourScore.asStateFlow()

  private val _opponentScore = MutableStateFlow(Score.zero)
  val opponentScore: StateFlow<Score> = _opponentScore.asStateFlow()

  private val _ourGoals = MutableStateFlow(Goals.empty)
  private val _opponentGoals = MutableStateFlow(Goals.empty)

  /**
   * Records [goal] for [team]: increments that team's score by one and appends [goal] to that
   * team's goal history.
   */
  fun recordGoal(team: Team, goal: Goal) {
    when (team) {
      Team.HOME -> {
        _ourScore.update { it.incremented() }
        _ourGoals.update { it.recorded(goal) }
      }
      Team.VISITING -> {
        _opponentScore.update { it.incremented() }
        _opponentGoals.update { it.recorded(goal) }
      }
    }
  }

  /**
   * Corrects a mistaken tap/recording for [team]: decrements that team's score by one (or leaves it
   * at zero if it is already zero) and removes that team's most recently recorded goal, if one
   * exists.
   */
  fun decrementScore(team: Team) {
    when (team) {
      Team.HOME -> {
        _ourScore.update { it.decremented() }
        _ourGoals.update { it.latestRemoved() }
      }
      Team.VISITING -> {
        _opponentScore.update { it.decremented() }
        _opponentGoals.update { it.latestRemoved() }
      }
    }
  }

  /** Prints both teams' recorded goal histories, for debugging. */
  fun printDebugSummary() {
    println("Our goals: ${_ourGoals.value}")
    println("Opponent goals: ${_opponentGoals.value}")
  }
}

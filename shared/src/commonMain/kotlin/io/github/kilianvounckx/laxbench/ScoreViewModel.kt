package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Score
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Publishes the two independent running goal tallies shown on the score tracker: [ourScore] for our
 * team and [opponentScore] for the opponent. Each tally starts at [Score.zero] when this
 * [ScoreViewModel] is created.
 *
 * A score and its goal history always change together and stay in lockstep through three
 * operations: [recordGoal] is the only way to increment a score, and it always also appends the
 * same [Goal] to that side's history with a unique id; [updateGoal] updates a goal's details
 * without changing the score; [removeGoal] is the only way to decrement a score, and it always also
 * removes that side's goal with the matching id. Because every increment goes through [recordGoal],
 * a side's score count and the size of its goal history are always equal. All of the "never below
 * zero" and "append/remove at the end" bookkeeping is done by [Score] and [Goals], pure domain
 * types; this class only holds the current value of each and republishes the scores immediately on
 * every change, in the same StateFlow-based style [TimerViewModel] uses for
 * [TimerViewModel.elapsedTime] and [TimerViewModel.runState].
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

  private var nextGoalId = 0L

  /**
   * Records a goal for [team]: increments that team's score by one and appends a new [Goal] with a
   * unique id to that team's goal history.
   */
  fun recordGoal(
    team: Team,
    scorer: PlayerNumber,
    assist: PlayerNumber?,
    elapsedTime: ElapsedTime,
  ) {
    val goal = Goal(id = nextGoalId++, scorer = scorer, assist = assist, elapsedTime = elapsedTime)
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
   * Updates the goal identified by [edited.id] for [team], without changing that team's score. This
   * is a no-op if no goal with that id exists.
   */
  fun updateGoal(team: Team, edited: Goal) {
    when (team) {
      Team.HOME -> _ourGoals.update { it.updated(edited) }
      Team.VISITING -> _opponentGoals.update { it.updated(edited) }
    }
  }

  /**
   * Removes the goal identified by [id] for [team]: decrements that team's score by one and removes
   * that goal from that team's history. This is a genuine no-op — neither the score nor the goal
   * history changes — if no goal with that id exists, e.g. a duplicate/late call such as a
   * double-tap on Manage Game's "Delete" button for the same goal before recomposition removes the
   * row; this keeps the score count and goal-history size in lockstep exactly as this class's own
   * invariant requires.
   */
  fun removeGoal(team: Team, id: Long) {
    when (team) {
      Team.HOME -> {
        if (_ourGoals.value.all.none { it.id == id }) return
        _ourScore.update { it.decremented() }
        _ourGoals.update { it.removed(id) }
      }
      Team.VISITING -> {
        if (_opponentGoals.value.all.none { it.id == id }) return
        _opponentScore.update { it.decremented() }
        _opponentGoals.update { it.removed(id) }
      }
    }
  }

  /** Returns the goal history for the given [team]. */
  fun goals(team: Team): StateFlow<Goals> =
    when (team) {
      Team.HOME -> _ourGoals.asStateFlow()
      Team.VISITING -> _opponentGoals.asStateFlow()
    }

  /** Prints both teams' recorded goal histories, for debugging. */
  fun printDebugSummary() {
    println("Our goals: ${_ourGoals.value}")
    println("Opponent goals: ${_opponentGoals.value}")
  }
}

/** Returns the [TeamInfo] entered for [team] within [this] game. */
fun TeamsInfo.info(team: ScoreViewModel.Team): TeamInfo =
  when (team) {
    ScoreViewModel.Team.HOME -> home
    ScoreViewModel.Team.VISITING -> visiting
  }

/**
 * Returns the display label for [team] within [this] game, e.g. "Lions (Red)" -- replaces the old
 * hardcoded "Home"/"Visiting" label.
 */
fun TeamsInfo.label(team: ScoreViewModel.Team): String = info(team).label()

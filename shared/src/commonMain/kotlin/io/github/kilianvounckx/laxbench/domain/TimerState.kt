package io.github.kilianvounckx.laxbench.domain

import kotlin.time.ComparableTimeMark

/**
 * The run/pause state of a single toggleable timer, together with just enough information to
 * compute its current [ElapsedTime] at any later instant with no drift and no double-counting of
 * paused time.
 *
 * - [Running] means the timer is actively counting up: [Running.accumulated] is the elapsed time
 *   built up over all earlier run segments (zero the first time the timer starts), and
 *   [Running.mark] is the instant the current segment started or was last resumed. The live elapsed
 *   time at any later instant `now` is `accumulated + (now - mark)`.
 * - [Paused] means the timer is frozen at exactly [Paused.elapsed] until it is next toggled back to
 *   running; querying [elapsedTime] at any later instant while paused still returns exactly that
 *   frozen value, ignoring the passage of time.
 *
 * The only supported transition is [toggled], which flips [Running] to [Paused] (freezing at the
 * elapsed time as of `now`) or [Paused] to [Running] (resuming from the frozen value, with a fresh
 * [Running.mark] of `now`, so the very next [elapsedTime] call after a resume returns exactly the
 * pre-pause value with zero elapsed since `now == mark`). This guarantees a finished run segment's
 * time is folded into the next state exactly once — never re-measured, never lost, never
 * double-counted — no matter how many pause/resume cycles happen.
 */
sealed class TimerState {

  /** Actively counting up: see [TimerState] for how [accumulated] and [mark] combine. */
  data class Running(val accumulated: ElapsedTime, val mark: ComparableTimeMark) : TimerState()

  /** Frozen at [elapsed] until next toggled back to [Running]. */
  data class Paused(val elapsed: ElapsedTime) : TimerState()

  /** The elapsed time this state represents as of [now]. */
  fun elapsedTime(now: ComparableTimeMark): ElapsedTime =
    when (this) {
      is Running -> ElapsedTime.of(accumulated.duration + (now - mark)) ?: ElapsedTime.zero
      is Paused -> elapsed
    }

  /**
   * Flips [Running] to [Paused] (freezing at the elapsed time as of [now]), or [Paused] back to
   * [Running] (resuming from its frozen elapsed time, starting a fresh run segment at [now]).
   */
  fun toggled(now: ComparableTimeMark): TimerState =
    when (this) {
      is Running -> Paused(elapsedTime(now))
      is Paused -> Running(elapsed, now)
    }

  companion object {
    /** The initial state: running, with zero accumulated elapsed time, starting at [mark]. */
    fun initial(mark: ComparableTimeMark): TimerState = Running(ElapsedTime.zero, mark)
  }
}

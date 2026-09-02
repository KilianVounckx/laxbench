package io.github.kilianvounckx.laxbench.domain

import kotlin.time.ComparableTimeMark

/**
 * The run/pause state of a single toggleable timer, together with just enough information to
 * compute its current [ElapsedTime] at any later instant with no drift and no double-counting of
 * paused time.
 *
 * - [NotStarted] means the timer has never been started: [elapsedTime] is always zero, no matter
 *   how much real time passes, until the first [toggled] call.
 * - [Running] means the timer is actively counting up: [Running.accumulated] is the elapsed time
 *   built up over all earlier run segments (zero the first time the timer starts), and
 *   [Running.mark] is the instant the current segment started or was last resumed. The live elapsed
 *   time at any later instant `now` is `accumulated + (now - mark)`.
 * - [Paused] means the timer is frozen at exactly [Paused.elapsed] until it is next toggled back to
 *   running; querying [elapsedTime] at any later instant while paused still returns exactly that
 *   frozen value, ignoring the passage of time.
 * - [Locked] means the timer is permanently frozen at exactly [Locked.elapsed] and can never be
 *   toggled or resumed: it is only reached when the quarters feature auto-stops the game clock at
 *   the end of the 4th quarter. [toggled] is a no-op on a [Locked] state, returning it unchanged
 *   forever.
 *
 * [toggled] flips [NotStarted] to [Running] (starting the very first run segment from zero at
 * `now`), [Running] to [Paused] (freezing at the elapsed time as of `now`), [Paused] to [Running]
 * (resuming from the frozen value, with a fresh [Running.mark] of `now`, so the very next
 * [elapsedTime] call after a resume returns exactly the pre-pause value with zero elapsed since
 * `now == mark`), or [Locked] to [Locked] (no-op). [NotStarted] is only ever the starting state and
 * is never returned to once left. This guarantees a finished run segment's time is folded into the
 * next state exactly once — never re-measured, never lost, never double-counted — no matter how
 * many pause/resume cycles happen, until the timer is locked at game end.
 */
sealed class TimerState {

  /** The state before the timer has ever been started: always zero elapsed time, never ticking. */
  data object NotStarted : TimerState()

  /** Actively counting up: see [TimerState] for how [accumulated] and [mark] combine. */
  data class Running(val accumulated: ElapsedTime, val mark: ComparableTimeMark) : TimerState()

  /** Frozen at [elapsed] until next toggled back to [Running]. */
  data class Paused(val elapsed: ElapsedTime) : TimerState()

  /** Frozen permanently at [elapsed]: [toggled] is a no-op, returning this unchanged, forever. */
  data class Locked(val elapsed: ElapsedTime) : TimerState()

  /** The elapsed time this state represents as of [now]. */
  fun elapsedTime(now: ComparableTimeMark): ElapsedTime =
    when (this) {
      is NotStarted -> ElapsedTime.zero
      is Running -> ElapsedTime.of(accumulated.duration + (now - mark)) ?: ElapsedTime.zero
      is Paused -> elapsed
      is Locked -> elapsed
    }

  /**
   * Flips [NotStarted] to [Running] (starting the first run segment from zero at [now]), [Running]
   * to [Paused] (freezing at the elapsed time as of [now]), [Paused] back to [Running] (resuming
   * from its frozen elapsed time, starting a fresh run segment at [now]), or [Locked] to [Locked]
   * (no-op, returning this unchanged).
   */
  fun toggled(now: ComparableTimeMark): TimerState =
    when (this) {
      is NotStarted -> Running(ElapsedTime.zero, now)
      is Running -> Paused(elapsedTime(now))
      is Paused -> Running(elapsed, now)
      is Locked -> this
    }
}

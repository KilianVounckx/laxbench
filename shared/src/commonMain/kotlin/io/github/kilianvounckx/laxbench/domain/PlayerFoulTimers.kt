package io.github.kilianvounckx.laxbench.domain

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

/**
 * One player's full set of currently active/queued foul timers: [running] is the one foul currently
 * ticking down for this player, with [runningState] holding its elapsed-time bookkeeping (reusing
 * [TimerState] -- only [TimerState.Running]/[TimerState.Paused] are ever used here, since a foul
 * timer starts ticking, or starts paused if the game clock is already paused, the instant it
 * becomes this player's [running] entry, and is never in a "not started" state of its own).
 * [queued] holds every later foul for the same player, oldest first, each waiting untouched at its
 * full duration until it becomes [running] -- only the [running] entry ever counts down; [queued]
 * entries carry no timing state at all.
 *
 * There is no "empty" variant of this type: a player with no foul timers simply has no
 * [PlayerFoulTimers] at all in whichever map holds them (see `FoulTimerViewModel`), mirroring how
 * [TimeOutCountdownViewModel] models "no countdown" with a nullable [TimeOutCountdownState] rather
 * than an internal not-running case.
 */
data class PlayerFoulTimers(
  val running: FoulTimerEntry,
  val runningState: TimerState,
  val queued: List<FoulTimerEntry>,
) {

  /** The [running] entry's own remaining time as of [now], floored at [ElapsedTime.zero]. */
  fun runningRemainingTime(now: ComparableTimeMark): ElapsedTime {
    val elapsed = runningState.elapsedTime(now).duration
    val remaining = running.duration - elapsed
    val floored = if (remaining.isNegative()) Duration.ZERO else remaining
    return ElapsedTime.of(floored)!!
  }

  /**
   * The single combined remaining time shown in the UI: [runningRemainingTime] plus the full
   * [FoulTimerEntry.duration] of every entry still in [queued] (which have not started counting
   * down at all).
   */
  fun remainingTime(now: ComparableTimeMark): ElapsedTime {
    val queuedTotal = queued.fold(Duration.ZERO) { total, entry -> total + entry.duration }
    return ElapsedTime.of(runningRemainingTime(now).duration + queuedTotal) ?: ElapsedTime.zero
  }

  /** True once [running]'s elapsed time as of [now] has reached its [FoulTimerEntry.duration]. */
  fun isRunningExpired(now: ComparableTimeMark): Boolean =
    runningState.elapsedTime(now).duration >= running.duration

  /**
   * Advances past a finished [running] entry, cascading through as many completed entries as [now]
   * warrants (e.g. after the app was backgrounded through several fouls' worth of real time): while
   * [isRunningExpired], promotes the next [queued] entry to [running] via [promoted], carrying
   * forward any overrun past the finished entry's duration into the newly-promoted entry's starting
   * elapsed time so no real time is silently dropped. Returns `this` unchanged if not yet expired,
   * or `null` once expiry empties the queue entirely (this player now has zero remaining timers).
   */
  fun updated(now: ComparableTimeMark): PlayerFoulTimers? =
    if (!isRunningExpired(now)) this else promoted(now)?.updated(now)

  /**
   * A copy with [runningState] toggled between running/paused as of [now] (see
   * [TimerState.toggled]); [queued] is untouched since only [running] ever carries timing state.
   */
  fun toggled(now: ComparableTimeMark): PlayerFoulTimers =
    copy(runningState = runningState.toggled(now))

  /** A copy with [entry] appended to the end of [queued]. */
  fun enqueued(entry: FoulTimerEntry): PlayerFoulTimers = copy(queued = queued + entry)

  /**
   * A copy with the entry identified by [id] (whether [running] or in [queued]) given
   * [newDuration]; a no-op copy if [id] matches neither. Leaves elapsed/paused bookkeeping
   * untouched -- changing [running]'s duration changes its remaining time by exactly the difference
   * between the old and new duration, without resetting how much of it has already elapsed.
   */
  fun withDuration(id: Long, newDuration: Duration): PlayerFoulTimers =
    when {
      running.id == id -> copy(running = running.copy(duration = newDuration))
      else -> copy(queued = queued.map { if (it.id == id) it.copy(duration = newDuration) else it })
    }

  /**
   * Cancels the single entry identified by [id]. If it is the [running] entry, promotes the next
   * [queued] entry (if any) via [promoted] -- deliberately only one level, never cascading further
   * even if the newly-promoted entry also happens to already be expired as of [now], so a
   * cancellation itself can never be mistaken for (or itself trigger) a natural completion; any
   * further natural expiry is instead picked up moments later by the regular tick calling
   * [updated]. If [id] is a queued entry instead, it is removed from [queued], preserving the
   * relative order of the rest, and [running] is untouched. If [id] matches neither, returns an
   * equal copy unchanged.
   */
  fun cancelled(id: Long, now: ComparableTimeMark): PlayerFoulTimers? =
    if (running.id == id) promoted(now) else copy(queued = queued.filterNot { it.id == id })

  /**
   * Promotes the first [queued] entry (if any) to [running], carrying [running]'s overrun past its
   * own duration (elapsed beyond [running]'s [FoulTimerEntry.duration], floored at zero) forward as
   * the promoted entry's starting elapsed time, and starting a fresh [TimerState.Running] mark at
   * [now] if the current mode is running, or a fresh [TimerState.Paused] at that carried elapsed
   * value if the current mode is paused (so promotion itself never changes whether the queue is
   * paused). Returns `null` if [queued] is empty (nothing to promote to).
   */
  private fun promoted(now: ComparableTimeMark): PlayerFoulTimers? {
    val next = queued.firstOrNull() ?: return null
    val overrun = runningState.elapsedTime(now).duration - running.duration
    val carriedElapsed = ElapsedTime.of(overrun) ?: ElapsedTime.zero
    val freshState =
      if (runningState is TimerState.Paused) TimerState.Paused(carriedElapsed)
      else TimerState.Running(carriedElapsed, now)
    return PlayerFoulTimers(running = next, runningState = freshState, queued = queued.drop(1))
  }

  companion object {
    /**
     * Starts a brand-new queue for a player with no existing timers: [entry] becomes [running]
     * immediately, running from zero elapsed if [isGameClockRunning], or paused at zero elapsed
     * otherwise (matching a foul logged while the game clock is stopped or has not started yet).
     */
    fun started(
      entry: FoulTimerEntry,
      now: ComparableTimeMark,
      isGameClockRunning: Boolean,
    ): PlayerFoulTimers =
      PlayerFoulTimers(
        running = entry,
        runningState =
          if (isGameClockRunning) TimerState.Running(ElapsedTime.zero, now)
          else TimerState.Paused(ElapsedTime.zero),
        queued = emptyList(),
      )
  }
}

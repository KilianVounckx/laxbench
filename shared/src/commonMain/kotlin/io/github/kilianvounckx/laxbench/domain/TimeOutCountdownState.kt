package io.github.kilianvounckx.laxbench.domain

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The state of a single 90-second time-out countdown, from the moment it [started] until it either
 * reaches zero and becomes [Expired], or is discarded entirely by whoever holds it. There is no
 * "cancelled" variant here: cancellation is modeled by the holder simply dropping its reference to
 * this state (e.g. setting a nullable property to `null`), not by a value inside the type itself.
 *
 * - [Running] means the countdown is actively counting down from [DURATION]: [Running.mark] is the
 *   instant it was started, via [started]. The remaining time at any later instant `now` is
 *   `DURATION - (now - mark)`, floored at zero -- see [remainingTime].
 * - [Expired] means the countdown has reached zero and is frozen there until discarded; it carries
 *   no data of its own.
 *
 * Unlike [TimerState], there is deliberately no pause/resume here: per the time-out countdown
 * feature, once started this countdown only ever counts down towards [Expired], and the only way it
 * goes away is for the holder to discard it outright (there is nothing to resume it into).
 */
sealed class TimeOutCountdownState {

  /** Actively counting down from [DURATION] since [mark]. */
  data class Running(val mark: ComparableTimeMark) : TimeOutCountdownState()

  /** Frozen at zero remaining time until discarded. */
  data object Expired : TimeOutCountdownState()

  /**
   * The time remaining as of [now]: `DURATION - (now - mark)` while [Running], floored at
   * [ElapsedTime.zero] (never negative, even if [now] is far past [DURATION] after [mark]); always
   * [ElapsedTime.zero] once [Expired].
   */
  fun remainingTime(now: ComparableTimeMark): ElapsedTime =
    when (this) {
      is Running -> {
        val remaining = DURATION - (now - mark)
        if (remaining.isNegative()) ElapsedTime.zero
        else ElapsedTime.of(remaining) ?: ElapsedTime.zero
      }
      is Expired -> ElapsedTime.zero
    }

  /**
   * Whether this countdown has reached zero as of [now] -- i.e. whether it should become [Expired].
   */
  fun isExpired(now: ComparableTimeMark): Boolean =
    when (this) {
      is Running -> now - mark >= DURATION
      is Expired -> true
    }

  /**
   * Advances a [Running] state to [Expired] once [isExpired] as of [now]; otherwise returns this
   * unchanged. Calling this on an already-[Expired] state always returns [Expired].
   */
  fun updated(now: ComparableTimeMark): TimeOutCountdownState =
    if (this is Expired) this else if (isExpired(now)) Expired else this

  companion object {
    /** The fixed duration a time-out countdown counts down from: 90 seconds (1:30). */
    val DURATION: Duration = 90.seconds

    /** Starts a fresh countdown, counting down from [DURATION] as of [now]. */
    fun started(now: ComparableTimeMark): TimeOutCountdownState = Running(now)
  }
}

package io.github.kilianvounckx.laxbench.domain

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration

/**
 * The state of a single countdown of a given [Running.duration], from the moment it [started] until
 * it either reaches zero and becomes [Expired], or is discarded entirely by whoever holds it. There
 * is no "cancelled" variant here: cancellation is modeled by the holder simply dropping its
 * reference to this state (e.g. setting a nullable property to `null`), not by a value inside the
 * type itself.
 *
 * This single type backs every countdown-to-zero feature in this app: the fixed 90-second time-out
 * countdown (see `TimeOutCountdownViewModel`, which always calls [started] with its own fixed
 * duration constant) and the variable-length (2 or 10 minute) intermission countdown between
 * lacrosse quarters (see `IntermissionCountdownViewModel`, which passes whichever duration the
 * quarter that just ended calls for). The "count down from a duration to zero, floor at zero,
 * become Expired" rule is identical in both cases; only the duration and what the surrounding UI
 * does with the result differ, and those differences (e.g. time-outs have a separate `show()`/
 * visibility step and a published `isExpired` used to blink, while intermissions are always
 * immediately visible and never blink) belong entirely to the respective ViewModels, not to this
 * domain type -- this type itself has no notion of visibility or blinking either way.
 *
 * - [Running] means the countdown is actively counting down from [Running.duration]: [Running.mark]
 *   is the instant it was started, via [started]. The remaining time at any later instant `now` is
 *   `duration - (now - mark)`, floored at zero -- see [remainingTime].
 * - [Expired] means the countdown has reached zero and is frozen there until discarded; it carries
 *   no data of its own.
 *
 * Unlike [TimerState], there is deliberately no pause/resume here: once started, a countdown only
 * ever counts down towards [Expired], and the only way it goes away is for the holder to discard it
 * outright (there is nothing to resume it into).
 */
sealed class CountdownState {

  /** Actively counting down from [duration] since [mark]. */
  data class Running(val duration: Duration, val mark: ComparableTimeMark) : CountdownState()

  /** Frozen at zero remaining time until discarded. */
  data object Expired : CountdownState()

  /**
   * The time remaining as of [now]: `duration - (now - mark)` while [Running], floored at
   * [ElapsedTime.zero] (never negative, even if [now] is far past [Running.duration] after
   * [Running.mark]); always [ElapsedTime.zero] once [Expired].
   */
  fun remainingTime(now: ComparableTimeMark): ElapsedTime =
    when (this) {
      is Running -> {
        val remaining = duration - (now - mark)
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
      is Running -> now - mark >= duration
      is Expired -> true
    }

  /**
   * Advances a [Running] state to [Expired] once [isExpired] as of [now]; otherwise returns this
   * unchanged. Calling this on an already-[Expired] state always returns [Expired].
   */
  fun updated(now: ComparableTimeMark): CountdownState =
    if (this is Expired) this else if (isExpired(now)) Expired else this

  companion object {
    /** Starts a fresh countdown with the given [duration], counting down from it as of [now]. */
    fun started(duration: Duration, now: ComparableTimeMark): CountdownState =
      Running(duration, now)
  }
}

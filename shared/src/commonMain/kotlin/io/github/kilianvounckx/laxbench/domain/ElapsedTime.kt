package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline
import kotlin.time.Duration

/**
 * A non-negative duration elapsed since a timer started running.
 *
 * Constructed only through [of] or [zero], so every existing instance is guaranteed to wrap a
 * duration that is not negative.
 */
@JvmInline
value class ElapsedTime private constructor(val duration: Duration) {

  /**
   * Renders this elapsed time as `MM:SS.DD` — minutes, seconds, and hundredths of a second. Seconds
   * and hundredths are always exactly two digits, zero-padded. Minutes are zero-padded to a minimum
   * of two digits but are never capped or rolled over into an hours field: once 100 minutes or more
   * have elapsed, the minutes portion simply grows past two digits (e.g. `125:00.00`). There is no
   * hours component in the output under any circumstances. The hundredths-of-a-second digits are
   * truncated (not rounded) from the underlying duration.
   */
  fun format(): String {
    val totalMillis = duration.inWholeMilliseconds
    val minutes = totalMillis / 60_000L
    val seconds = (totalMillis / 1_000L) % 60L
    val hundredths = (totalMillis / 10L) % 100L
    val minutesText = minutes.toString().padStart(2, '0')
    val secondsText = seconds.toString().padStart(2, '0')
    val hundredthsText = hundredths.toString().padStart(2, '0')
    return "$minutesText:$secondsText.$hundredthsText"
  }

  companion object {
    /** An elapsed time of zero. */
    val zero: ElapsedTime = ElapsedTime(Duration.ZERO)

    /** Returns an [ElapsedTime] wrapping [duration], or `null` if [duration] is negative. */
    fun of(duration: Duration): ElapsedTime? =
      if (duration.isNegative()) null else ElapsedTime(duration)
  }
}

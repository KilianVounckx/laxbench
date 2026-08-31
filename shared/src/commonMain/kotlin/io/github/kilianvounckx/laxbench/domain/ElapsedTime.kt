package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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

private const val MAX_TOTAL_HUNDREDTHS = 999_999_999L

/**
 * Applies one masked-input edit to this [ElapsedTime] for the "MM:SS.DD" masked field (see
 * `ElapsedTimeField`): [oldText] is this value's own [format] output (what the field was showing
 * right before the edit), and [newText] is the raw text the field's `onValueChange` just received.
 *
 * The whole formatted string is treated as a live decimal encoding of the total elapsed
 * hundredths-of-a-second, right-anchored, so digits always fill in from the right (like a
 * calculator/currency-style masked input) rather than a fixed left-to-right template — this is what
 * lets minutes grow unboundedly past two digits while seconds/hundredths, derived from that running
 * total by `/100 % 60` and `% 100`, can never become invalid, since they are never typed directly:
 * - if [newText] is exactly [oldText] with one extra character appended at the end, and that
 *   character is a digit, the digit is shifted in from the right (`total = total * 10 + digit`,
 *   coerced to [MAX_TOTAL_HUNDREDTHS] so the field cannot grow without bound).
 * - if [newText] is exactly [oldText] with its last character removed, the total loses its last
 *   digit (`total /= 10`) — a plain backspace.
 * - any other shape (appending a non-digit, pasting, editing in the middle of the field, replacing
 *   a selection, ...) is rejected outright: this [ElapsedTime] is returned unchanged. This mask
 *   intentionally only supports single-keystroke typing/backspacing at the end, which is what a
 *   numeric keyboard naturally produces.
 */
fun ElapsedTime.maskedEdit(oldText: String, newText: String): ElapsedTime {
  val oldTotalHundredths = duration.inWholeMilliseconds / 10
  val newTotalHundredths =
    when {
      newText.length == oldText.length + 1 && newText.startsWith(oldText) -> {
        val appended = newText.last()
        if (!appended.isDigit()) return this
        (oldTotalHundredths * 10 + appended.digitToInt()).coerceAtMost(MAX_TOTAL_HUNDREDTHS)
      }
      newText.length == oldText.length - 1 && oldText.startsWith(newText) -> oldTotalHundredths / 10
      else -> return this
    }
  return ElapsedTime.of((newTotalHundredths * 10).milliseconds)!!
}

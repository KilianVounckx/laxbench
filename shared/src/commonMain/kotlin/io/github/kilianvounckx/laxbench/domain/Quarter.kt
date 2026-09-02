package io.github.kilianvounckx.laxbench.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * A lacrosse game quarter: one of the four 15-minute segments making up a complete 60-minute game.
 *
 * The game clock is a single cumulative [ElapsedTime] that never resets: it counts from 0:00 to
 * 60:00 with no pause between quarters (actual game pauses — timeouts, fouls, etc. — are modeled
 * separately via [TimerState.Paused]). Each `Quarter` is simply a label over a fixed time range
 * within that cumulative clock.
 *
 * - [endTime] for each quarter marks the boundary: 15:00 for [FIRST], 30:00 for [SECOND], 45:00 for
 *   [THIRD], 60:00 for [FOURTH].
 * - [intermissionDuration] is the break between quarters: 2 minutes after [FIRST] and [THIRD], 10
 *   minutes after [SECOND], and `null` (no intermission) after [FOURTH], since the game ends there.
 * - [of] maps an elapsed time to the containing quarter, explicitly clamping any time at or past
 *   [GAME_DURATION] (60:00) to [FOURTH], so there is no 5th quarter or overtime.
 * - [quarterJustEnded] detects which boundary was crossed between two consecutive [ElapsedTime]
 *   readings (never skipping a boundary even if a single tick jumps far enough to pass multiple at
 *   once), used by [TimerViewModel] to auto-stop and auto-clamp the clock exactly at quarter
 *   boundaries instead of overshooting.
 *
 * All comparisons in this type use [ElapsedTime]'s own [Comparable] ordering directly (`<`, `>=`,
 * etc. on [ElapsedTime] values) rather than reaching into `.duration` at each call site.
 */
enum class Quarter {
  FIRST,
  SECOND,
  THIRD,
  FOURTH;

  /**
   * The elapsed time at the exact end of this quarter: 15:00 for [FIRST], 30:00 for [SECOND], 45:00
   * for [THIRD], 60:00 for [FOURTH]. Computed by scaling the [Duration] [DURATION] (never an
   * [ElapsedTime] itself, which has no scaling operator) and wrapping the result once, at the end.
   */
  val endTime: ElapsedTime
    get() = ElapsedTime.of(DURATION * (ordinal + 1)) ?: ElapsedTime.zero

  /**
   * The length of the intermission following this quarter, or `null` if there is no intermission
   * (only [FOURTH] has no intermission, since the game ends).
   */
  val intermissionDuration: Duration?
    get() =
      when (this) {
        FIRST -> INTERMISSION_AFTER_FIRST_OR_THIRD
        SECOND -> INTERMISSION_AFTER_SECOND
        THIRD -> INTERMISSION_AFTER_FIRST_OR_THIRD
        FOURTH -> null
      }

  companion object {
    /** The fixed duration of each quarter: 15 minutes, never configurable. */
    val DURATION: Duration = 15.minutes

    /** The intermission after the 1st and 3rd quarters: 2 minutes. */
    val INTERMISSION_AFTER_FIRST_OR_THIRD: Duration = 2.minutes

    /** The intermission after the 2nd quarter: 10 minutes (halftime). */
    val INTERMISSION_AFTER_SECOND: Duration = 10.minutes

    /** The total duration of a complete game: 4 quarters × 15 minutes = 60 minutes. */
    val GAME_DURATION: ElapsedTime = ElapsedTime.of(DURATION * entries.size) ?: ElapsedTime.zero

    /**
     * Returns the quarter containing [elapsedTime]. Any time at or past [GAME_DURATION] returns
     * [FOURTH] immediately, clamping defensively so there is no 5th quarter or overtime; otherwise
     * the containing quarter is found by dividing [elapsedTime] by [DURATION].
     */
    fun of(elapsedTime: ElapsedTime): Quarter {
      if (elapsedTime >= GAME_DURATION) return FOURTH
      val index = elapsedTime.duration.inWholeMilliseconds / DURATION.inWholeMilliseconds
      return entries[index.toInt().coerceIn(0, entries.size - 1)]
    }

    /**
     * Returns the earliest quarter boundary crossed going from [previous] to [current], or `null`
     * if no boundary was crossed in between (including if [previous] already equals a boundary and
     * [current] equals the same value, preventing false re-triggers on a clamped state).
     *
     * If a single tick jumps far enough to pass multiple boundaries at once, this returns the
     * *earliest* (lowest quarter index) boundary crossed, never skipping any: e.g. if [previous] is
     * 0:00 and [current] jumps to 61:00 (past all four boundaries), this still returns [FIRST].
     *
     * This MUST be implemented exactly as the expression below -- returning the matched `Quarter`
     * entry directly from `firstOrNull`, never by first computing a boundary `ElapsedTime` and then
     * calling `Quarter.of(boundary)`: [of] treats a value exactly equal to a quarter's [endTime] as
     * already belonging to the *next* quarter, so `of(boundary)` would return the quarter *after*
     * the one that actually just ended for every boundary except the last (e.g. crossing 15:00
     * would wrongly resolve to [SECOND] instead of [FIRST]).
     */
    fun quarterJustEnded(previous: ElapsedTime, current: ElapsedTime): Quarter? =
      entries.firstOrNull {
        previous < it.endTime && current >= it.endTime
      }
  }
}

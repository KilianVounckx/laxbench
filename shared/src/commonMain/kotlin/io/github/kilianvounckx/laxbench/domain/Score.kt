package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * A non-negative goal count for one side of a game (e.g. our team's tally or the opponent's).
 *
 * Constructed only through [zero], so every existing instance is guaranteed to hold a [count] that
 * is not negative. [incremented] and [decremented] are the only ways to derive a new [Score] from
 * an existing one, and both preserve that invariant: [decremented] never lets the count drop below
 * zero, it simply returns this same value unchanged once it is already zero. The same type is used
 * for both sides of the score tracker so the increment/decrement rules are defined exactly once and
 * reused, rather than duplicated per side.
 */
@JvmInline
value class Score private constructor(val count: Int) {

  /** Returns a [Score] with [count] one higher than this one. */
  fun incremented(): Score = Score(count + 1)

  /**
   * Returns a [Score] with [count] one lower than this one, or this same [Score] unchanged if
   * [count] is already zero.
   */
  fun decremented(): Score = if (count == 0) this else Score(count - 1)

  companion object {
    /** A score of zero. */
    val zero: Score = Score(0)
  }
}

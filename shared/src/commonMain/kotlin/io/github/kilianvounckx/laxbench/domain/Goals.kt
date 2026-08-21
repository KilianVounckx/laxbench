package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of goals recorded so far for one side of a game (e.g. our team's goals or
 * the opponent's), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a goal, always appending it as the newest/last entry;
 * [latestRemoved] is the only way to remove one, and it always removes the most recently recorded
 * (last) entry -- it mirrors [Score.decremented] in leaving this same value unchanged, rather than
 * failing, when there is nothing left to remove. The same type is used for both sides of the score
 * tracker so this "append at the end, remove from the end" bookkeeping is defined exactly once and
 * reused, rather than duplicated per side.
 */
@JvmInline
value class Goals private constructor(val all: List<Goal>) {

  /** Returns a [Goals] with [goal] appended as the newest entry. */
  fun recorded(goal: Goal): Goals = Goals(all + goal)

  /**
   * Returns a [Goals] with the most recently recorded entry removed, or this same [Goals] unchanged
   * if [all] is already empty.
   */
  fun latestRemoved(): Goals = if (all.isEmpty()) this else Goals(all.dropLast(1))

  companion object {
    /** A [Goals] with no recorded goals. */
    val empty: Goals = Goals(emptyList())
  }
}

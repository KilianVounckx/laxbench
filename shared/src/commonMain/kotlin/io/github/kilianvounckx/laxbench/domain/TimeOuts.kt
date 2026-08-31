package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of time-outs requested so far by one side of a game (e.g. the home team's or
 * the visiting team's), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a time-out, always appending it as the newest/last entry.
 * [removed] is the only way to remove a time-out by id, with no-op semantics if the id is not
 * present. The same type is used for both sides of the time-out tally so this "append at the end"
 * bookkeeping is defined exactly once and reused, rather than duplicated per side.
 */
@JvmInline
value class TimeOuts private constructor(val all: List<TimeOut>) {

  /** Returns a [TimeOuts] with [timeOut] appended as the newest entry. */
  fun recorded(timeOut: TimeOut): TimeOuts = TimeOuts(all + timeOut)

  /**
   * Returns a [TimeOuts] with the entry identified by [id] removed, or this same [TimeOuts]
   * unchanged if [id] is not present.
   */
  fun removed(id: Long): TimeOuts = TimeOuts(all.filterNot { it.id == id })

  companion object {
    /** A [TimeOuts] with no recorded time-outs. */
    val empty: TimeOuts = TimeOuts(emptyList())
  }
}

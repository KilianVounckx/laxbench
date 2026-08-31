package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of goals recorded so far for one side of a game (e.g. our team's goals or
 * the opponent's), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a goal, always appending it as the newest/last entry. [removed]
 * removes a goal by id, with no-op semantics if the id is not present. [updated] edits a goal by
 * id, with no-op semantics if the id is not present. The same type is used for both sides of the
 * score tracker so this bookkeeping is defined exactly once and reused, rather than duplicated per
 * side.
 */
@JvmInline
value class Goals private constructor(val all: List<Goal>) {

  /** Returns a [Goals] with [goal] appended as the newest entry. */
  fun recorded(goal: Goal): Goals = Goals(all + goal)

  /**
   * Returns a [Goals] with the entry identified by [id] removed, or this same [Goals] unchanged if
   * [id] is not present.
   */
  fun removed(id: Long): Goals = Goals(all.filterNot { it.id == id })

  /**
   * Returns a [Goals] with the entry identified by [edited.id] replaced, or this same [Goals]
   * unchanged if [edited.id] is not present.
   */
  fun updated(edited: Goal): Goals = Goals(all.map { if (it.id == edited.id) edited else it })

  companion object {
    /** A [Goals] with no recorded goals. */
    val empty: Goals = Goals(emptyList())
  }
}

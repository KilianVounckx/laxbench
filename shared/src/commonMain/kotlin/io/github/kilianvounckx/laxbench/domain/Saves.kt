package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of goalie saves recorded so far by one side of a game (e.g. the home team's
 * or the visiting team's goalie), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a save, always appending it as the newest/last entry. [removed]
 * is the only way to remove a save by id, with no-op semantics if the id is not present. The same
 * type is used for both sides of the save tally so this "append at the end" bookkeeping is defined
 * exactly once and reused, rather than duplicated per side.
 */
@JvmInline
value class Saves private constructor(val all: List<Save>) {

  /** Returns a [Saves] with [save] appended as the newest entry. */
  fun recorded(save: Save): Saves = Saves(all + save)

  /**
   * Returns a [Saves] with the entry identified by [id] removed, or this same [Saves] unchanged if
   * [id] is not present.
   */
  fun removed(id: Long): Saves = Saves(all.filterNot { it.id == id })

  companion object {
    /** A [Saves] with no recorded saves. */
    val empty: Saves = Saves(emptyList())
  }
}

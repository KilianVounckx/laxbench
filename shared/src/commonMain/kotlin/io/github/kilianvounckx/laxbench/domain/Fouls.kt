package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of fouls recorded so far for one side of a game (e.g. our team's fouls or
 * the opponent's), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a foul, always appending it as the newest/last entry. [removed]
 * is the only way to remove a foul by id, with no-op semantics if the id is not present. [updated]
 * is the only way to edit a foul by id, with no-op semantics if the id is not present. The same
 * type is used for both sides of the foul tally so this "append at the end" bookkeeping is defined
 * exactly once and reused, rather than duplicated per side.
 */
@JvmInline
value class Fouls private constructor(val all: List<Foul>) {

  /** Returns a [Fouls] with [foul] appended as the newest entry. */
  fun recorded(foul: Foul): Fouls = Fouls(all + foul)

  /**
   * Returns a [Fouls] with the entry identified by [id] removed, or this same [Fouls] unchanged if
   * [id] is not present.
   */
  fun removed(id: Long): Fouls = Fouls(all.filterNot { it.id == id })

  /**
   * Returns a [Fouls] with the entry identified by [edited.id] replaced, or this same [Fouls]
   * unchanged if [edited.id] is not present.
   */
  fun updated(edited: Foul): Fouls = Fouls(all.map { if (it.id == edited.id) edited else it })

  companion object {
    /** A [Fouls] with no recorded fouls. */
    val empty: Fouls = Fouls(emptyList())
  }
}

package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of fouls recorded so far for one side of a game (e.g. our team's fouls or
 * the opponent's), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a foul, always appending it as the newest/last entry. Unlike
 * [Goals], there is no removal operation: this feature has no undo/correction mechanism for a
 * mistakenly recorded foul, so no "remove the latest" behavior is needed or provided. The same type
 * is used for both sides of the foul tally so this "append at the end" bookkeeping is defined
 * exactly once and reused, rather than duplicated per side.
 */
@JvmInline
value class Fouls private constructor(val all: List<Foul>) {

  /** Returns a [Fouls] with [foul] appended as the newest entry. */
  fun recorded(foul: Foul): Fouls = Fouls(all + foul)

  companion object {
    /** A [Fouls] with no recorded fouls. */
    val empty: Fouls = Fouls(emptyList())
  }
}

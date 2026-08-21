package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of face-off wins recorded so far by one side of a game (e.g. the home team's
 * or the visiting team's face-off wins), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a face-off win, always appending it as the newest/last entry.
 * As with [Saves], [TimeOuts], and [Fouls], there is no removal operation: no undo/correction
 * mechanism exists for a mistakenly recorded face-off win. The same type is used for both sides of
 * the face-off tally so this "append at the end" bookkeeping is defined exactly once and reused,
 * rather than duplicated per side.
 */
@JvmInline
value class FaceOffs private constructor(val all: List<FaceOff>) {

  /** Returns a [FaceOffs] with [faceOff] appended as the newest entry. */
  fun recorded(faceOff: FaceOff): FaceOffs = FaceOffs(all + faceOff)

  companion object {
    /** A [FaceOffs] with no recorded face-off wins. */
    val empty: FaceOffs = FaceOffs(emptyList())
  }
}

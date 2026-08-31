package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * The ordered sequence of face-off wins recorded so far by one side of a game (e.g. the home team's
 * or the visiting team's face-off wins), oldest first.
 *
 * Constructed only through [empty], so every existing instance starts from an empty list.
 * [recorded] is the only way to add a face-off win, always appending it as the newest/last entry.
 * [removed] is the only way to remove a face-off win by id, with no-op semantics if the id is not
 * present. The same type is used for both sides of the face-off tally so this "append at the end"
 * bookkeeping is defined exactly once and reused, rather than duplicated per side.
 */
@JvmInline
value class FaceOffs private constructor(val all: List<FaceOff>) {

  /** Returns a [FaceOffs] with [faceOff] appended as the newest entry. */
  fun recorded(faceOff: FaceOff): FaceOffs = FaceOffs(all + faceOff)

  /**
   * Returns a [FaceOffs] with the entry identified by [id] removed, or this same [FaceOffs]
   * unchanged if [id] is not present.
   */
  fun removed(id: Long): FaceOffs = FaceOffs(all.filterNot { it.id == id })

  companion object {
    /** A [FaceOffs] with no recorded face-off wins. */
    val empty: FaceOffs = FaceOffs(emptyList())
  }
}

package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * A team's color, as entered in the setup screen — free text only, e.g. "Red", "Navy Blue". Never
 * interpreted as an actual color anywhere. This app has no color validation, so a [TeamColor] is
 * nothing more than a validated non-blank string.
 *
 * Constructed only through [parse], so every existing instance is guaranteed to wrap a [value] that
 * is not blank (after trimming leading/trailing whitespace).
 */
@JvmInline
value class TeamColor private constructor(val value: String) {

  companion object {
    /**
     * Parses [text] (after trimming leading/trailing whitespace) as a [TeamColor]. Returns `null`
     * if the trimmed text is empty.
     */
    fun parse(text: String): TeamColor? =
      text.trim().let { if (it.isEmpty()) null else TeamColor(it) }
  }
}

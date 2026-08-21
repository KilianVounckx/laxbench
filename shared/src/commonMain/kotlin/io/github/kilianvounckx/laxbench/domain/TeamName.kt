package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * A team's name, as entered in the setup screen. This app has no roster or name validation, so a
 * [TeamName] is nothing more than a validated non-blank string -- it is not checked against any
 * list of known teams.
 *
 * Constructed only through [parse], so every existing instance is guaranteed to wrap a [value] that
 * is not blank (after trimming leading/trailing whitespace).
 */
@JvmInline
value class TeamName private constructor(val value: String) {

  companion object {
    /**
     * Parses [text] (after trimming leading/trailing whitespace) as a [TeamName]. Returns `null` if
     * the trimmed text is empty.
     */
    fun parse(text: String): TeamName? =
      text.trim().let { if (it.isEmpty()) null else TeamName(it) }
  }
}

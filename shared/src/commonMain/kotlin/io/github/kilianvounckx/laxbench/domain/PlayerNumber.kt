package io.github.kilianvounckx.laxbench.domain

import kotlin.jvm.JvmInline

/**
 * A non-negative jersey/player number, as entered for a goal's scorer or assisting player. This app
 * has no player roster, so a [PlayerNumber] is nothing more than a validated non-negative integer
 * -- it is not checked against, or otherwise tied to, any list of known players, and has no upper
 * bound.
 *
 * Constructed only through [of] or [parse], so every existing instance is guaranteed to wrap a
 * [number] that is not negative.
 */
@JvmInline
value class PlayerNumber private constructor(val number: Int) {

  companion object {
    /** Returns a [PlayerNumber] wrapping [number], or `null` if [number] is negative. */
    fun of(number: Int): PlayerNumber? = if (number < 0) null else PlayerNumber(number)

    /**
     * Parses [text] (after trimming leading/trailing whitespace) as a [PlayerNumber]. Returns
     * `null` if [text] is blank, is not a valid integer, or parses to a negative integer.
     */
    fun parse(text: String): PlayerNumber? = text.trim().toIntOrNull()?.let(::of)
  }
}

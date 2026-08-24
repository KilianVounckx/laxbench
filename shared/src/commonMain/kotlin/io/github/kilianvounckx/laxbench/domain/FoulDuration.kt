package io.github.kilianvounckx.laxbench.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * The length of the penalty for a [FoulSeverity.Major] foul, chosen in the last step of recording a
 * major foul (see [FoulDialog]) -- a major foul is only recorded once both its [MajorFoulType] and
 * a [FoulDuration] have been chosen.
 *
 * Unlike [MinorFoulType]/[MajorFoulType], whose values have no natural order and so are declared
 * alphabetically by label, these three values already have an unambiguous order (how long the
 * penalty lasts), so they are declared in that ascending order instead. [length] carries the
 * real-world duration of the penalty alongside its display text.
 */
enum class FoulDuration(val label: String, val length: Duration) {
  ONE_MINUTE("1 Minute", 1.minutes),
  TWO_MINUTES("2 Minutes", 2.minutes),
  THREE_MINUTES("3 Minutes", 3.minutes),
}

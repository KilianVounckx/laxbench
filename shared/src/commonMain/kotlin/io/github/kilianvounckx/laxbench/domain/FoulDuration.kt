package io.github.kilianvounckx.laxbench.domain

/**
 * The length of the penalty for a [FoulSeverity.Major] foul, chosen in the last step of recording a
 * major foul (see [FoulDialog]) -- a major foul is only recorded once both its [MajorFoulType] and
 * a [FoulDuration] have been chosen.
 *
 * Unlike [MinorFoulType]/[MajorFoulType], whose values have no natural order and so are declared
 * alphabetically by label, these three values already have an unambiguous order (how long the
 * penalty lasts), so they are declared in that ascending order instead.
 */
enum class FoulDuration(val label: String) {
  ONE_MINUTE("1 Minute"),
  TWO_MINUTES("2 Minutes"),
  THREE_MINUTES("3 Minutes"),
}

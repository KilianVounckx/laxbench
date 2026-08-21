package io.github.kilianvounckx.laxbench.domain

/**
 * The specific type of a major foul, as defined by the rules of the game. Declared in alphabetical
 * order by [label] -- the order these are offered to the user when recording a major foul.
 */
enum class MajorFoulType(val label: String) {
  CROSS_CHECK("Cross-Check"),
  ILLEGAL_BODY_CHECK("Illegal Body-Check"),
  SLASHING("Slashing"),
  TRIPPING("Tripping"),
  UNNECESSARY_ROUGHNESS("Unnecessary Roughness"),
  UNSPORTSMANLIKE_CONDUCT("Unsportsmanlike Conduct"),
}

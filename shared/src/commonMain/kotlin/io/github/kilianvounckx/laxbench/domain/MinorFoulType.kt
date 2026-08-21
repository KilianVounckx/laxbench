package io.github.kilianvounckx.laxbench.domain

/**
 * The specific type of a minor foul, as defined by the rules of the game. Declared in alphabetical
 * order by [label] -- the order these are offered to the user when recording a minor foul -- with
 * the "Kicking an Opponent's Stick" and "Withholding the Ball from Play" spellings corrected from a
 * legacy reference list that had them as "Oppontent's"/"Witholding".
 */
enum class MinorFoulType(val label: String) {
  CONDUCT_FOUL("Conduct Foul"),
  GOAL_CREASE_VIOLATION("Goal Crease Violation"),
  HANDLING_THE_BALL("Handling The Ball"),
  HOLDING("Holding"),
  ILLEGAL_PICK("Illegal Pick"),
  ILLEGAL_PROCEDURE("Illegal Procedure"),
  INTERFERENCE("Interference"),
  KICKING_AN_OPPONENTS_STICK("Kicking an Opponent's Stick"),
  OFFSIDE("Offside"),
  PUSHING("Pushing"),
  STALLING("Stalling"),
  WARDING("Warding"),
  WITHHOLDING_THE_BALL_FROM_PLAY("Withholding the Ball from Play"),
}

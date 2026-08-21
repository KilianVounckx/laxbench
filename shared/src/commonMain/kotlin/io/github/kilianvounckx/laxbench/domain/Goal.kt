package io.github.kilianvounckx.laxbench.domain

/**
 * A single recorded goal event: the [scorer]'s player number, the [assist]ing player's number (or
 * `null` if the goal was unassisted), and the [elapsedTime] of the game clock at the moment the
 * goal-recording pop-up was opened (i.e. when the score number was tapped) -- not at the moment the
 * pop-up was later confirmed. Which team scored is not part of this type -- it is implied by which
 * team's [Goals] the [Goal] was [Goals.recorded] into.
 */
data class Goal(val scorer: PlayerNumber, val assist: PlayerNumber?, val elapsedTime: ElapsedTime)

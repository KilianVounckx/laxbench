package io.github.kilianvounckx.laxbench.domain

/**
 * A single recorded time-out request: the [id] uniquely identifies this time-out request and just
 * the [elapsedTime] of the game clock at the moment the "Stop all clocks" button was tapped that
 * opened the time-out pop-up requesting it. Which team requested the time-out is not part of this
 * type -- it is implied by which team's [TimeOuts] the [TimeOut] was [TimeOuts.recorded] into,
 * mirroring how [Foul] omits which team committed it. There is no maximum-number-of-time-outs rule
 * and no reason/duration recorded here -- this is simply an unbounded log of requests.
 */
data class TimeOut(val id: Long, val elapsedTime: ElapsedTime)

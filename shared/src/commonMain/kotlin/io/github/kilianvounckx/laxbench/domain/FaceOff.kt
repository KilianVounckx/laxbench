package io.github.kilianvounckx.laxbench.domain

/**
 * A single recorded face-off win: just the [elapsedTime] of the game clock at the moment the
 * "Face-off" button was tapped that opened the face-off-recording pop-up requesting it. Which team
 * won the face-off is not part of this type -- it is implied by which team's [FaceOffs] the
 * [FaceOff] was [FaceOffs.recorded] into, mirroring how [Save] omits which team's goalie made it
 * and [TimeOut] omits which team requested it.
 */
data class FaceOff(val elapsedTime: ElapsedTime)

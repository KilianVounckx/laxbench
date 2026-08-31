package io.github.kilianvounckx.laxbench.domain

/**
 * A single recorded goalie save: the [id] uniquely identifies this save and just the [elapsedTime]
 * of the game clock at the moment the "Save" button was tapped that opened the save-recording
 * pop-up requesting it. Which team's goalie made the save is not part of this type -- it is implied
 * by which team's [Saves] the [Save] was [Saves.recorded] into, mirroring how [TimeOut] omits which
 * team requested it and [Foul] omits which team committed it.
 */
data class Save(val id: Long, val elapsedTime: ElapsedTime)

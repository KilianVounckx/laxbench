package io.github.kilianvounckx.laxbench.domain

/**
 * A single recorded foul event: the offending player's [player] number, the [severity] of the foul
 * (and, for [FoulSeverity.Minor]/[FoulSeverity.Major], its specific type), and the [elapsedTime] of
 * the game clock at the moment the foul-recording pop-up was opened -- not at the moment the pop-up
 * was later completed. Which team committed the foul is not part of this type -- it is implied by
 * which team's [Fouls] the [Foul] was [Fouls.recorded] into, mirroring how [Goal] omits which team
 * scored.
 */
data class Foul(val player: PlayerNumber, val severity: FoulSeverity, val elapsedTime: ElapsedTime)

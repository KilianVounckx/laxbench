package io.github.kilianvounckx.laxbench.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * How serious a recorded [Foul] was, together with exactly the extra data that severity level
 * requires -- modeled so an invalid combination (a minor/major foul with no specific type, an
 * expulsion carrying a specific type it doesn't have, or a major foul with no penalty duration)
 * cannot be represented at all, in the same spirit as [TimerState] attaching only the payload each
 * of its states needs.
 *
 * [Minor] carries the specific [MinorFoulType] that was called. [Major] carries both the specific
 * [MajorFoulType] and the penalty [FoulDuration] chosen for it -- a [Major] foul can never be
 * constructed without both. [Expulsion] carries no further sub-type: unlike the other two
 * severities, an expulsion is not broken down into specific kinds -- being an expulsion is itself
 * the specific foul type.
 *
 * [timerDuration] computes each severity's corresponding foul-timer countdown length: 30 seconds
 * for any [Minor], the chosen [FoulDuration.length] for a [Major], or 5 minutes for [Expulsion].
 */
sealed class FoulSeverity {
  data class Minor(val type: MinorFoulType) : FoulSeverity()

  data class Major(val type: MajorFoulType, val duration: FoulDuration) : FoulSeverity()

  data object Expulsion : FoulSeverity()

  /** The countdown duration for this foul's timer. */
  val timerDuration: Duration
    get() =
      when (this) {
        is Minor -> 30.seconds
        is Major -> duration.length
        Expulsion -> 5.minutes
      }
}

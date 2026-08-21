package io.github.kilianvounckx.laxbench.domain

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
 */
sealed class FoulSeverity {
  data class Minor(val type: MinorFoulType) : FoulSeverity()

  data class Major(val type: MajorFoulType, val duration: FoulDuration) : FoulSeverity()

  data object Expulsion : FoulSeverity()
}

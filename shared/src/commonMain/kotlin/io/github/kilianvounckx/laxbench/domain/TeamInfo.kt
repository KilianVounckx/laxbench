package io.github.kilianvounckx.laxbench.domain

/** Holds a team's name and color as entered in the setup screen. */
data class TeamInfo(val name: TeamName, val color: TeamColor) {
  /** Returns a display label for this team, e.g. "Lions (Red)". */
  fun label(): String = "${name.value} (${color.value})"
}

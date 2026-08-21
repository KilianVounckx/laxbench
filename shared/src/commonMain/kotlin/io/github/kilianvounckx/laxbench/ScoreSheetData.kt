package io.github.kilianvounckx.laxbench

import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Saves
import io.github.kilianvounckx.laxbench.domain.TeamName
import io.github.kilianvounckx.laxbench.domain.TimeOuts

data class ScoreSheetData(
  val goals: List<GoalEntry>,
  val fouls: List<FoulEntry>,
  val homeTimeOuts: List<ElapsedTime>,
  val visitingTimeOuts: List<ElapsedTime>,
  val homeSaves: Int,
  val visitingSaves: Int,
  val homeFaceOffs: Int,
  val visitingFaceOffs: Int,
  val homeName: TeamName,
  val visitingName: TeamName,
) {
  companion object {
    fun of(
      homeGoals: Goals,
      visitingGoals: Goals,
      homeFouls: Fouls,
      visitingFouls: Fouls,
      homeTimeOuts: TimeOuts,
      visitingTimeOuts: TimeOuts,
      homeSaves: Saves,
      visitingSaves: Saves,
      homeFaceOffs: FaceOffs,
      visitingFaceOffs: FaceOffs,
      homeName: TeamName,
      visitingName: TeamName,
    ): ScoreSheetData {
      val combinedGoals =
        homeGoals.all.map { ScoreViewModel.Team.HOME to it } +
          visitingGoals.all.map { ScoreViewModel.Team.VISITING to it }
      val sortedGoals = combinedGoals.sortedBy { (_, goal) -> goal.elapsedTime.duration }
      var homeScore = 0
      var visitingScore = 0
      val goalEntries = sortedGoals.map { (team, goal) ->
        when (team) {
          ScoreViewModel.Team.HOME -> homeScore++
          ScoreViewModel.Team.VISITING -> visitingScore++
        }
        GoalEntry(
          goal.elapsedTime,
          team,
          homeScore,
          visitingScore,
          goal.scorer,
          goal.assist,
        )
      }

      val foulEntries =
        (homeFouls.all.map { ScoreViewModel.Team.HOME to it } +
            visitingFouls.all.map { ScoreViewModel.Team.VISITING to it })
          .sortedBy { (_, foul) -> foul.elapsedTime.duration }
          .map { (team, foul) -> FoulEntry(foul.elapsedTime, team, foul.player, foul.severity) }

      return ScoreSheetData(
        goals = goalEntries,
        fouls = foulEntries,
        homeTimeOuts = homeTimeOuts.all.map { it.elapsedTime },
        visitingTimeOuts = visitingTimeOuts.all.map { it.elapsedTime },
        homeSaves = homeSaves.all.size,
        visitingSaves = visitingSaves.all.size,
        homeFaceOffs = homeFaceOffs.all.size,
        visitingFaceOffs = visitingFaceOffs.all.size,
        homeName = homeName,
        visitingName = visitingName,
      )
    }
  }
}

data class GoalEntry(
  val elapsedTime: ElapsedTime,
  val team: ScoreViewModel.Team,
  val homeScoreAfter: Int,
  val visitingScoreAfter: Int,
  val scorer: PlayerNumber,
  val assist: PlayerNumber?,
)

data class FoulEntry(
  val elapsedTime: ElapsedTime,
  val team: ScoreViewModel.Team,
  val player: PlayerNumber,
  val severity: FoulSeverity,
)

package io.github.kilianvounckx.laxbench

import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.Saves
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import io.github.kilianvounckx.laxbench.domain.TimeOuts

data class GameInitialState(
  val teams: TeamsInfo,
  val elapsedTime: ElapsedTime,
  val runState: TimerViewModel.RunState,
  val homeGoals: Goals,
  val visitingGoals: Goals,
  val homeFouls: Fouls,
  val visitingFouls: Fouls,
  val homeSaves: Saves,
  val visitingSaves: Saves,
  val homeFaceOffs: FaceOffs,
  val visitingFaceOffs: FaceOffs,
  val homeTimeOuts: TimeOuts,
  val visitingTimeOuts: TimeOuts,
) {
  companion object {
    fun fresh(teams: TeamsInfo): GameInitialState =
      GameInitialState(
        teams = teams,
        elapsedTime = ElapsedTime.zero,
        runState = TimerViewModel.RunState.NotStarted,
        homeGoals = Goals.empty,
        visitingGoals = Goals.empty,
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
      )
  }
}

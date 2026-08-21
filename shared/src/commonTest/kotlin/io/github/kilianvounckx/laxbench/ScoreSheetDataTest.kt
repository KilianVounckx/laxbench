package io.github.kilianvounckx.laxbench

import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.Saves
import io.github.kilianvounckx.laxbench.domain.TimeOut
import io.github.kilianvounckx.laxbench.domain.TimeOuts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ScoreSheetDataTest {
  @Test
  fun `empty collections produce all-empty data`() {
    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty,
        visitingGoals = Goals.empty,
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
      )

    assertTrue(data.goals.isEmpty())
    assertTrue(data.fouls.isEmpty())
    assertTrue(data.homeTimeOuts.isEmpty())
    assertTrue(data.visitingTimeOuts.isEmpty())
    assertEquals(0, data.homeSaves)
    assertEquals(0, data.visitingSaves)
    assertEquals(0, data.homeFaceOffs)
    assertEquals(0, data.visitingFaceOffs)
  }

  @Test
  fun `home goal followed by later visiting goal produces correct order with running scores`() {
    val homeGoal =
      Goal(
        scorer = PlayerNumber.of(1)!!,
        assist = null,
        elapsedTime = ElapsedTime.of(5000.milliseconds)!!,
      )
    val visitingGoal =
      Goal(
        scorer = PlayerNumber.of(2)!!,
        assist = null,
        elapsedTime = ElapsedTime.of(10000.milliseconds)!!,
      )

    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty.recorded(homeGoal),
        visitingGoals = Goals.empty.recorded(visitingGoal),
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
      )

    assertEquals(2, data.goals.size)
    assertEquals(ScoreViewModel.Team.HOME, data.goals[0].team)
    assertEquals(1, data.goals[0].homeScoreAfter)
    assertEquals(0, data.goals[0].visitingScoreAfter)
    assertEquals(ScoreViewModel.Team.VISITING, data.goals[1].team)
    assertEquals(1, data.goals[1].homeScoreAfter)
    assertEquals(1, data.goals[1].visitingScoreAfter)
  }

  @Test
  fun `two goals at same elapsed time with home first ties to home-first in result`() {
    val homeGoal =
      Goal(
        scorer = PlayerNumber.of(1)!!,
        assist = null,
        elapsedTime = ElapsedTime.of(5000.milliseconds)!!,
      )
    val visitingGoal =
      Goal(
        scorer = PlayerNumber.of(2)!!,
        assist = null,
        elapsedTime = ElapsedTime.of(5000.milliseconds)!!,
      )

    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty.recorded(homeGoal),
        visitingGoals = Goals.empty.recorded(visitingGoal),
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
      )

    assertEquals(2, data.goals.size)
    assertEquals(ScoreViewModel.Team.HOME, data.goals[0].team)
    assertEquals(ScoreViewModel.Team.VISITING, data.goals[1].team)
  }

  @Test
  fun `fouls tie-break to home-first at same elapsed time`() {
    val homeFoul =
      Foul(
        player = PlayerNumber.of(1)!!,
        severity = FoulSeverity.Expulsion,
        elapsedTime = ElapsedTime.of(5000.milliseconds)!!,
      )
    val visitingFoul =
      Foul(
        player = PlayerNumber.of(2)!!,
        severity = FoulSeverity.Expulsion,
        elapsedTime = ElapsedTime.of(5000.milliseconds)!!,
      )

    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty,
        visitingGoals = Goals.empty,
        homeFouls = Fouls.empty.recorded(homeFoul),
        visitingFouls = Fouls.empty.recorded(visitingFoul),
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
      )

    assertEquals(2, data.fouls.size)
    assertEquals(ScoreViewModel.Team.HOME, data.fouls[0].team)
    assertEquals(ScoreViewModel.Team.VISITING, data.fouls[1].team)
  }

  @Test
  fun `home and visiting time-outs are kept separate per team`() {
    val homeTimeOut = TimeOut(elapsedTime = ElapsedTime.of(5000.milliseconds)!!)
    val visitingTimeOut = TimeOut(elapsedTime = ElapsedTime.of(7000.milliseconds)!!)

    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty,
        visitingGoals = Goals.empty,
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeTimeOuts = TimeOuts.empty.recorded(homeTimeOut),
        visitingTimeOuts = TimeOuts.empty.recorded(visitingTimeOut),
        homeSaves = Saves.empty,
        visitingSaves = Saves.empty,
        homeFaceOffs = FaceOffs.empty,
        visitingFaceOffs = FaceOffs.empty,
      )

    assertEquals(1, data.homeTimeOuts.size)
    assertEquals(ElapsedTime.of(5000.milliseconds)!!, data.homeTimeOuts[0])
    assertEquals(1, data.visitingTimeOuts.size)
    assertEquals(ElapsedTime.of(7000.milliseconds)!!, data.visitingTimeOuts[0])
  }

  @Test
  fun `saves and face-offs are rendered as counts`() {
    val homeSave1 = Save(elapsedTime = ElapsedTime.of(5000.milliseconds)!!)
    val homeSave2 = Save(elapsedTime = ElapsedTime.of(6000.milliseconds)!!)
    val visitingSave = Save(elapsedTime = ElapsedTime.of(7000.milliseconds)!!)
    val homeFaceOff1 = FaceOff(elapsedTime = ElapsedTime.of(1000.milliseconds)!!)
    val homeFaceOff2 = FaceOff(elapsedTime = ElapsedTime.of(2000.milliseconds)!!)
    val homeFaceOff3 = FaceOff(elapsedTime = ElapsedTime.of(3000.milliseconds)!!)
    val visitingFaceOff = FaceOff(elapsedTime = ElapsedTime.of(4000.milliseconds)!!)

    val data =
      ScoreSheetData.of(
        homeGoals = Goals.empty,
        visitingGoals = Goals.empty,
        homeFouls = Fouls.empty,
        visitingFouls = Fouls.empty,
        homeTimeOuts = TimeOuts.empty,
        visitingTimeOuts = TimeOuts.empty,
        homeSaves = Saves.empty.recorded(homeSave1).recorded(homeSave2),
        visitingSaves = Saves.empty.recorded(visitingSave),
        homeFaceOffs =
          FaceOffs.empty.recorded(homeFaceOff1).recorded(homeFaceOff2).recorded(homeFaceOff3),
        visitingFaceOffs = FaceOffs.empty.recorded(visitingFaceOff),
      )

    assertEquals(2, data.homeSaves)
    assertEquals(1, data.visitingSaves)
    assertEquals(3, data.homeFaceOffs)
    assertEquals(1, data.visitingFaceOffs)
  }
}

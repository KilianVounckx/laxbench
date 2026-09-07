package io.github.kilianvounckx.laxbench.persistence

import io.github.kilianvounckx.laxbench.TimerViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulDuration
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.MajorFoulType
import io.github.kilianvounckx.laxbench.domain.MinorFoulType
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.TeamColor
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamName
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import io.github.kilianvounckx.laxbench.domain.TimeOut
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class GameSnapshotMapperTest {

  @Test
  fun teamsInfoRoundTrip() {
    val original =
      TeamsInfo(
        home = TeamInfo(TeamName.parse("Lions")!!, TeamColor.parse("Red")!!),
        visiting = TeamInfo(TeamName.parse("Tigers")!!, TeamColor.parse("Blue")!!),
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun goalRoundTrip() {
    val original =
      Goal(
        id = 42,
        scorer = PlayerNumber.of(23)!!,
        assist = PlayerNumber.of(17),
        elapsedTime = ElapsedTime.of(125.seconds)!!,
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun goalWithoutAssistRoundTrip() {
    val original =
      Goal(
        id = 1,
        scorer = PlayerNumber.of(5)!!,
        assist = null,
        elapsedTime = ElapsedTime.of(10.seconds)!!,
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun foulMinorRoundTrip() {
    val original =
      Foul(
        id = 100,
        player = PlayerNumber.of(7)!!,
        severity = FoulSeverity.Minor(MinorFoulType.HOLDING),
        elapsedTime = ElapsedTime.of(300.seconds)!!,
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun foulMajorRoundTrip() {
    val original =
      Foul(
        id = 101,
        player = PlayerNumber.of(12)!!,
        severity = FoulSeverity.Major(MajorFoulType.CROSS_CHECK, FoulDuration.TWO_MINUTES),
        elapsedTime = ElapsedTime.of(450.seconds)!!,
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun foulExpulsionRoundTrip() {
    val original =
      Foul(
        id = 102,
        player = PlayerNumber.of(4)!!,
        severity = FoulSeverity.Expulsion,
        elapsedTime = ElapsedTime.of(600.seconds)!!,
      )
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun saveRoundTrip() {
    val original = Save(id = 200, elapsedTime = ElapsedTime.of(150.seconds)!!)
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun faceOffRoundTrip() {
    val original = FaceOff(id = 300, elapsedTime = ElapsedTime.of(75.seconds)!!)
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun timeOutRoundTrip() {
    val original = TimeOut(id = 400, elapsedTime = ElapsedTime.of(500.seconds)!!)
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomainOrNull()
    assertEquals(original, restored)
  }

  @Test
  fun timerRunStateNotStartedRoundTrip() {
    val original = TimerViewModel.RunState.NotStarted
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomain()
    assertEquals(original, restored)
  }

  @Test
  fun timerRunStateRunningRoundTrip() {
    val original = TimerViewModel.RunState.Running
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomain()
    assertEquals(original, restored)
  }

  @Test
  fun timerRunStatePausedRoundTrip() {
    val original = TimerViewModel.RunState.Paused
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomain()
    assertEquals(original, restored)
  }

  @Test
  fun timerRunStateLockedRoundTrip() {
    val original = TimerViewModel.RunState.Locked
    val snapshot = original.toSnapshot()
    val restored = snapshot.toDomain()
    assertEquals(original, restored)
  }

  @Test
  fun gameSnapshotRejectsInvalidTeamName() {
    val snapshot =
      GameSnapshot(
        teams =
          TeamsInfoSnapshot(
            home = TeamInfoSnapshot("", "Red"),
            visiting = TeamInfoSnapshot("Tigers", "Blue"),
          ),
        timer = TimerSnapshot(TimerRunStateSnapshot.NOT_STARTED, 0),
        homeGoals = emptyList(),
        visitingGoals = emptyList(),
        homeFouls = emptyList(),
        visitingFouls = emptyList(),
        homeSaves = emptyList(),
        visitingSaves = emptyList(),
        homeFaceOffs = emptyList(),
        visitingFaceOffs = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
      )
    assertNull(snapshot.toGameInitialStateOrNull())
  }

  @Test
  fun gameSnapshotRejectsNegativeElapsedMillis() {
    val snapshot =
      GameSnapshot(
        teams =
          TeamsInfoSnapshot(
            home = TeamInfoSnapshot("Lions", "Red"),
            visiting = TeamInfoSnapshot("Tigers", "Blue"),
          ),
        timer = TimerSnapshot(TimerRunStateSnapshot.NOT_STARTED, -1),
        homeGoals = emptyList(),
        visitingGoals = emptyList(),
        homeFouls = emptyList(),
        visitingFouls = emptyList(),
        homeSaves = emptyList(),
        visitingSaves = emptyList(),
        homeFaceOffs = emptyList(),
        visitingFaceOffs = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
      )
    assertNull(snapshot.toGameInitialStateOrNull())
  }

  @Test
  fun gameSnapshotRejectsInvalidTeamColor() {
    val snapshot =
      GameSnapshot(
        teams =
          TeamsInfoSnapshot(
            home = TeamInfoSnapshot("Lions", ""),
            visiting = TeamInfoSnapshot("Tigers", "Blue"),
          ),
        timer = TimerSnapshot(TimerRunStateSnapshot.NOT_STARTED, 0),
        homeGoals = emptyList(),
        visitingGoals = emptyList(),
        homeFouls = emptyList(),
        visitingFouls = emptyList(),
        homeSaves = emptyList(),
        visitingSaves = emptyList(),
        homeFaceOffs = emptyList(),
        visitingFaceOffs = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
      )
    assertNull(snapshot.toGameInitialStateOrNull())
  }

  @Test
  fun gameSnapshotRejectsUnknownFoulType() {
    val snapshot =
      GameSnapshot(
        teams =
          TeamsInfoSnapshot(
            home = TeamInfoSnapshot("Lions", "Red"),
            visiting = TeamInfoSnapshot("Tigers", "Blue"),
          ),
        timer = TimerSnapshot(TimerRunStateSnapshot.NOT_STARTED, 0),
        homeGoals = emptyList(),
        visitingGoals = emptyList(),
        homeFouls =
          listOf(
            FoulSnapshot(
              id = 1,
              player = 5,
              kind = FoulSeverityKindSnapshot.MINOR,
              minorType = "UNKNOWN_FOUL_TYPE",
              majorType = null,
              majorDuration = null,
              elapsedMillis = 100,
            )
          ),
        visitingFouls = emptyList(),
        homeSaves = emptyList(),
        visitingSaves = emptyList(),
        homeFaceOffs = emptyList(),
        visitingFaceOffs = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
      )
    assertNull(snapshot.toGameInitialStateOrNull())
  }
}

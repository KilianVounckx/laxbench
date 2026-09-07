package io.github.kilianvounckx.laxbench.persistence

import io.github.kilianvounckx.laxbench.GameInitialState
import io.github.kilianvounckx.laxbench.TimerViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulDuration
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.MajorFoulType
import io.github.kilianvounckx.laxbench.domain.MinorFoulType
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Save
import io.github.kilianvounckx.laxbench.domain.Saves
import io.github.kilianvounckx.laxbench.domain.TeamColor
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamName
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import io.github.kilianvounckx.laxbench.domain.TimeOut
import io.github.kilianvounckx.laxbench.domain.TimeOuts
import kotlin.time.Duration.Companion.milliseconds

fun ElapsedTime.toMillis(): Long = duration.inWholeMilliseconds

fun Long.toElapsedTimeOrNull(): ElapsedTime? = ElapsedTime.of(this.milliseconds)

fun TeamsInfo.toSnapshot(): TeamsInfoSnapshot =
  TeamsInfoSnapshot(
    home = TeamInfoSnapshot(home.name.value, home.color.value),
    visiting = TeamInfoSnapshot(visiting.name.value, visiting.color.value),
  )

fun TeamsInfoSnapshot.toDomainOrNull(): TeamsInfo? {
  val homeName = TeamName.parse(home.name) ?: return null
  val homeColor = TeamColor.parse(home.color) ?: return null
  val visitingName = TeamName.parse(visiting.name) ?: return null
  val visitingColor = TeamColor.parse(visiting.color) ?: return null
  return TeamsInfo(
    home = TeamInfo(homeName, homeColor),
    visiting = TeamInfo(visitingName, visitingColor),
  )
}

fun Goal.toSnapshot(): GoalSnapshot =
  GoalSnapshot(
    id = id,
    scorer = scorer.number,
    assist = assist?.number,
    elapsedMillis = elapsedTime.toMillis(),
  )

fun GoalSnapshot.toDomainOrNull(): Goal? {
  val scorer = PlayerNumber.of(scorer) ?: return null
  val assist = assist?.let { PlayerNumber.of(it) ?: return null }
  val elapsedTime = elapsedMillis.toElapsedTimeOrNull() ?: return null
  return Goal(
    id = id,
    scorer = scorer,
    assist = assist,
    elapsedTime = elapsedTime,
  )
}

fun Foul.toSnapshot(): FoulSnapshot {
  val kind: FoulSeverityKindSnapshot
  val minorType: String?
  val majorType: String?
  val majorDuration: String?
  when (val s = severity) {
    is FoulSeverity.Minor -> {
      kind = FoulSeverityKindSnapshot.MINOR
      minorType = s.type.name
      majorType = null
      majorDuration = null
    }
    is FoulSeverity.Major -> {
      kind = FoulSeverityKindSnapshot.MAJOR
      minorType = null
      majorType = s.type.name
      majorDuration = s.duration.name
    }
    FoulSeverity.Expulsion -> {
      kind = FoulSeverityKindSnapshot.EXPULSION
      minorType = null
      majorType = null
      majorDuration = null
    }
  }
  return FoulSnapshot(
    id = id,
    player = player.number,
    kind = kind,
    minorType = minorType,
    majorType = majorType,
    majorDuration = majorDuration,
    elapsedMillis = elapsedTime.toMillis(),
  )
}

fun FoulSnapshot.toDomainOrNull(): Foul? {
  val player = PlayerNumber.of(player) ?: return null

  val severity =
    when (kind) {
      FoulSeverityKindSnapshot.MINOR -> {
        val minorTypeName = minorType ?: return null
        val minorType =
          runCatching { enumValueOf<MinorFoulType>(minorTypeName) }.getOrNull() ?: return null
        FoulSeverity.Minor(minorType)
      }
      FoulSeverityKindSnapshot.MAJOR -> {
        val majorTypeName = majorType ?: return null
        val durationName = majorDuration ?: return null
        val majorType =
          runCatching { enumValueOf<MajorFoulType>(majorTypeName) }.getOrNull() ?: return null
        val duration =
          runCatching { enumValueOf<FoulDuration>(durationName) }.getOrNull() ?: return null
        FoulSeverity.Major(majorType, duration)
      }
      FoulSeverityKindSnapshot.EXPULSION -> FoulSeverity.Expulsion
    }

  val elapsedTime = elapsedMillis.toElapsedTimeOrNull() ?: return null
  return Foul(
    id = id,
    player = player,
    severity = severity,
    elapsedTime = elapsedTime,
  )
}

fun Save.toSnapshot(): SaveSnapshot = SaveSnapshot(id = id, elapsedMillis = elapsedTime.toMillis())

fun SaveSnapshot.toDomainOrNull(): Save? {
  val elapsedTime = elapsedMillis.toElapsedTimeOrNull() ?: return null
  return Save(id = id, elapsedTime = elapsedTime)
}

fun FaceOff.toSnapshot(): FaceOffSnapshot =
  FaceOffSnapshot(id = id, elapsedMillis = elapsedTime.toMillis())

fun FaceOffSnapshot.toDomainOrNull(): FaceOff? {
  val elapsedTime = elapsedMillis.toElapsedTimeOrNull() ?: return null
  return FaceOff(id = id, elapsedTime = elapsedTime)
}

fun TimeOut.toSnapshot(): TimeOutSnapshot =
  TimeOutSnapshot(id = id, elapsedMillis = elapsedTime.toMillis())

fun TimeOutSnapshot.toDomainOrNull(): TimeOut? {
  val elapsedTime = elapsedMillis.toElapsedTimeOrNull() ?: return null
  return TimeOut(id = id, elapsedTime = elapsedTime)
}

fun TimerViewModel.RunState.toSnapshot(): TimerRunStateSnapshot =
  when (this) {
    TimerViewModel.RunState.NotStarted -> TimerRunStateSnapshot.NOT_STARTED
    TimerViewModel.RunState.Running -> TimerRunStateSnapshot.RUNNING
    TimerViewModel.RunState.Paused -> TimerRunStateSnapshot.PAUSED
    TimerViewModel.RunState.Locked -> TimerRunStateSnapshot.LOCKED
  }

fun TimerRunStateSnapshot.toDomain(): TimerViewModel.RunState =
  when (this) {
    TimerRunStateSnapshot.NOT_STARTED -> TimerViewModel.RunState.NotStarted
    TimerRunStateSnapshot.RUNNING -> TimerViewModel.RunState.Running
    TimerRunStateSnapshot.PAUSED -> TimerViewModel.RunState.Paused
    TimerRunStateSnapshot.LOCKED -> TimerViewModel.RunState.Locked
  }

private fun List<GoalSnapshot>.toGoalsOrNull(): Goals? {
  var acc = Goals.empty
  for (s in this) {
    val g = s.toDomainOrNull() ?: return null
    acc = acc.recorded(g)
  }
  return acc
}

private fun List<FoulSnapshot>.toFoulsOrNull(): Fouls? {
  var acc = Fouls.empty
  for (s in this) {
    val f = s.toDomainOrNull() ?: return null
    acc = acc.recorded(f)
  }
  return acc
}

private fun List<SaveSnapshot>.toSavesOrNull(): Saves? {
  var acc = Saves.empty
  for (s in this) {
    val save = s.toDomainOrNull() ?: return null
    acc = acc.recorded(save)
  }
  return acc
}

private fun List<FaceOffSnapshot>.toFaceOffsOrNull(): FaceOffs? {
  var acc = FaceOffs.empty
  for (s in this) {
    val faceOff = s.toDomainOrNull() ?: return null
    acc = acc.recorded(faceOff)
  }
  return acc
}

private fun List<TimeOutSnapshot>.toTimeOutsOrNull(): TimeOuts? {
  var acc = TimeOuts.empty
  for (s in this) {
    val timeOut = s.toDomainOrNull() ?: return null
    acc = acc.recorded(timeOut)
  }
  return acc
}

fun GameSnapshot.toGameInitialStateOrNull(): GameInitialState? {
  val teams = teams.toDomainOrNull() ?: return null
  val elapsedTime = timer.elapsedMillis.toElapsedTimeOrNull() ?: return null
  val homeGoals = homeGoals.toGoalsOrNull() ?: return null
  val visitingGoals = visitingGoals.toGoalsOrNull() ?: return null
  val homeFouls = homeFouls.toFoulsOrNull() ?: return null
  val visitingFouls = visitingFouls.toFoulsOrNull() ?: return null
  val homeSaves = homeSaves.toSavesOrNull() ?: return null
  val visitingSaves = visitingSaves.toSavesOrNull() ?: return null
  val homeFaceOffs = homeFaceOffs.toFaceOffsOrNull() ?: return null
  val visitingFaceOffs = visitingFaceOffs.toFaceOffsOrNull() ?: return null
  val homeTimeOuts = homeTimeOuts.toTimeOutsOrNull() ?: return null
  val visitingTimeOuts = visitingTimeOuts.toTimeOutsOrNull() ?: return null

  return GameInitialState(
    teams = teams,
    elapsedTime = elapsedTime,
    runState = timer.runState.toDomain(),
    homeGoals = homeGoals,
    visitingGoals = visitingGoals,
    homeFouls = homeFouls,
    visitingFouls = visitingFouls,
    homeSaves = homeSaves,
    visitingSaves = visitingSaves,
    homeFaceOffs = homeFaceOffs,
    visitingFaceOffs = visitingFaceOffs,
    homeTimeOuts = homeTimeOuts,
    visitingTimeOuts = visitingTimeOuts,
  )
}

fun GameInitialState.toSnapshot(): GameSnapshot =
  GameSnapshot(
    teams = teams.toSnapshot(),
    timer =
      TimerSnapshot(
        runState = runState.toSnapshot(),
        elapsedMillis = elapsedTime.toMillis(),
      ),
    homeGoals = homeGoals.all.map { it.toSnapshot() },
    visitingGoals = visitingGoals.all.map { it.toSnapshot() },
    homeFouls = homeFouls.all.map { it.toSnapshot() },
    visitingFouls = visitingFouls.all.map { it.toSnapshot() },
    homeSaves = homeSaves.all.map { it.toSnapshot() },
    visitingSaves = visitingSaves.all.map { it.toSnapshot() },
    homeFaceOffs = homeFaceOffs.all.map { it.toSnapshot() },
    visitingFaceOffs = visitingFaceOffs.all.map { it.toSnapshot() },
    homeTimeOuts = homeTimeOuts.all.map { it.toSnapshot() },
    visitingTimeOuts = visitingTimeOuts.all.map { it.toSnapshot() },
  )

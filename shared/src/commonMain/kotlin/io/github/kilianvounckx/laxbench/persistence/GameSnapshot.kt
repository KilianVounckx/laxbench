package io.github.kilianvounckx.laxbench.persistence

import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(
  val teams: TeamsInfoSnapshot,
  val timer: TimerSnapshot,
  val homeGoals: List<GoalSnapshot>,
  val visitingGoals: List<GoalSnapshot>,
  val homeFouls: List<FoulSnapshot>,
  val visitingFouls: List<FoulSnapshot>,
  val homeSaves: List<SaveSnapshot>,
  val visitingSaves: List<SaveSnapshot>,
  val homeFaceOffs: List<FaceOffSnapshot>,
  val visitingFaceOffs: List<FaceOffSnapshot>,
  val homeTimeOuts: List<TimeOutSnapshot>,
  val visitingTimeOuts: List<TimeOutSnapshot>,
)

@Serializable
data class TeamsInfoSnapshot(val home: TeamInfoSnapshot, val visiting: TeamInfoSnapshot)

@Serializable data class TeamInfoSnapshot(val name: String, val color: String)

@Serializable
enum class TimerRunStateSnapshot {
  NOT_STARTED,
  RUNNING,
  PAUSED,
  LOCKED,
}

@Serializable data class TimerSnapshot(val runState: TimerRunStateSnapshot, val elapsedMillis: Long)

@Serializable
data class GoalSnapshot(val id: Long, val scorer: Int, val assist: Int?, val elapsedMillis: Long)

@Serializable
enum class FoulSeverityKindSnapshot {
  MINOR,
  MAJOR,
  EXPULSION,
}

@Serializable
data class FoulSnapshot(
  val id: Long,
  val player: Int,
  val kind: FoulSeverityKindSnapshot,
  val minorType: String? = null,
  val majorType: String? = null,
  val majorDuration: String? = null,
  val elapsedMillis: Long,
)

@Serializable data class SaveSnapshot(val id: Long, val elapsedMillis: Long)

@Serializable data class FaceOffSnapshot(val id: Long, val elapsedMillis: Long)

@Serializable data class TimeOutSnapshot(val id: Long, val elapsedMillis: Long)

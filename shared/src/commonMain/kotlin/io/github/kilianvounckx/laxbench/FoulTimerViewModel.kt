package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.FoulTimerEntry
import io.github.kilianvounckx.laxbench.domain.PlayerFoulTimers
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Identifies one player, on one team, for foul-timer bookkeeping. Reuses [ScoreViewModel.Team] the
 * same way [FoulViewModel] does, rather than a second team enum, and reuses [PlayerNumber] -- the
 * only player-identifying type in this codebase -- rather than inventing a new one.
 */
data class FoulTimerPlayer(val team: ScoreViewModel.Team, val player: PlayerNumber)

/**
 * Returns the display label for [player] within [this] game, e.g. "Red 23". Mirrors
 * [TeamsInfo.label] (the existing whole-team label extension in `ScoreViewModel.kt`) one level
 * down, at the individual-player level, and is the single place that builds this text: every UI
 * surface that shows a player label ([CancelFoulTimersDialog]'s title, `CurrentFoulsScreen`'s rows,
 * and [GameScreen]'s release pop-up message) calls this instead of independently re-formatting
 * `"${color} ${number}"` itself.
 */
fun TeamsInfo.label(player: FoulTimerPlayer): String =
  "${info(player.team).color.value} ${player.player.number}"

/**
 * Which kind of entry a [FoulTimerDetail] represents, so the UI never has to infer "is this the
 * running one?" from list position: [RUNNING] is the single entry currently ticking down for that
 * player; [QUEUED] is any later entry still waiting, untouched, at its full duration.
 */
enum class FoulTimerEntryKind {
  RUNNING,
  QUEUED,
}

/**
 * One foul-timer entry's live display info for the cancel pop-up (see [CancelFoulTimersDialog]):
 * [id] to cancel it, [remainingTime] as its live remaining time if [kind] is
 * [FoulTimerEntryKind.RUNNING], or its full untouched duration if [kind] is
 * [FoulTimerEntryKind.QUEUED].
 */
data class FoulTimerDetail(
  val id: Long,
  val remainingTime: ElapsedTime,
  val kind: FoulTimerEntryKind,
)

/**
 * Runs every foul's own countdown timer (see [PlayerFoulTimers], [FoulTimerEntry]), one independent
 * FIFO queue per [FoulTimerPlayer], and publishes only what the UI needs: [remainingTimes] (one
 * combined total per player with any timers, backing both the "Current fouls" button's visibility
 * and list -- see [GameScreen] and `CurrentFoulsScreen`), [details] (each player's individual
 * running+queued entries, for the cancel pop-up -- see `CancelFoulTimersDialog`), and
 * [releaseEvents] (fired exactly once per player whenever their queue naturally empties on its own,
 * never on cancellation -- see `FoulReleaseDialog`).
 *
 * [recordFoul] is called once per individual foul actually recorded (including once per foul in a
 * simultaneous-foul batch, in logging order -- see [FoulDialog]/[GameScreen]), starting a brand new
 * queue for that player if they have none yet, or appending to the end of their existing queue
 * otherwise -- [PlayerFoulTimers] handles which of those two happens and enforces the FIFO
 * ordering.
 *
 * [pause]/[resume] pause/resume every player's running entry at once, called from [GameScreen] at
 * exactly the same "Stop all clocks"/"Resume game" transitions that already pause/resume the game
 * clock and the time-out countdown -- mirroring [TimeOutCountdownViewModel]'s explicit
 * start()/cancel() call-site style rather than reactively observing the clock's state. [resume] is
 * also called when the game clock starts for the very first time, to correctly resume any foul
 * logged before "Start game" was ever pressed (an edge case with no dedicated UI gating, handled
 * here defensively): [recordFoul]'s caller passes whether the clock is running *at the moment the
 * foul is recorded*, and a queue created while it is not running starts paused, exactly like this.
 *
 * The production tick loop and every other "now" this class reads always come from
 * [TimeSource.Monotonic] -- never [kotlin.time.TestTimeSource], which is a manual, non-advancing
 * clock meant only for unit tests and would make elapsed time never advance if used here.
 *
 * As with the other per-game ViewModels, this does not persist across process death, and a fresh
 * instance always starts with no timers.
 */
class FoulTimerViewModel : ViewModel() {

  private var nextId = 0L

  private val _queues = MutableStateFlow<Map<FoulTimerPlayer, PlayerFoulTimers>>(emptyMap())

  private val _remainingTimes = MutableStateFlow<Map<FoulTimerPlayer, ElapsedTime>>(emptyMap())
  val remainingTimes: StateFlow<Map<FoulTimerPlayer, ElapsedTime>> = _remainingTimes.asStateFlow()

  private val _details = MutableStateFlow<Map<FoulTimerPlayer, List<FoulTimerDetail>>>(emptyMap())
  val details: StateFlow<Map<FoulTimerPlayer, List<FoulTimerDetail>>> = _details.asStateFlow()

  private val _releaseEvents = MutableSharedFlow<FoulTimerPlayer>(extraBufferCapacity = 16)
  val releaseEvents: SharedFlow<FoulTimerPlayer> = _releaseEvents

  init {
    viewModelScope.launch {
      while (true) {
        refresh(TimeSource.Monotonic.markNow())
        delay(TICK_INTERVAL)
      }
    }
  }

  /**
   * Starts (or appends to) [team]/[player]'s foul-timer queue for a newly recorded foul of
   * [severity]. [isGameClockRunning] must reflect the game clock's run state at the moment the foul
   * was recorded, only used if this is that player's first outstanding timer.
   */
  fun recordFoul(
    team: ScoreViewModel.Team,
    player: PlayerNumber,
    severity: FoulSeverity,
    isGameClockRunning: Boolean,
  ) {
    val now = TimeSource.Monotonic.markNow()
    val key = FoulTimerPlayer(team, player)
    val entry = FoulTimerEntry(id = nextId++, duration = severity.timerDuration)
    _queues.value =
      _queues.value.toMutableMap().apply {
        val existing = this[key]
        this[key] =
          existing?.enqueued(entry) ?: PlayerFoulTimers.started(entry, now, isGameClockRunning)
      }
    refresh(now)
  }

  /** Pauses every player's running foul timer, in lockstep with the game clock stopping. */
  fun pause() = toggleAll()

  /**
   * Resumes every player's paused foul timer, in lockstep with the game clock resuming (or starting
   * for the first time).
   */
  fun resume() = toggleAll()

  private fun toggleAll() {
    val now = TimeSource.Monotonic.markNow()
    _queues.value = _queues.value.mapValues { (_, timers) -> timers.toggled(now) }
    refresh(now)
  }

  /**
   * Cancels one specific foul timer for [key], identified by [id]; see
   * [PlayerFoulTimers.cancelled]. Never emits a [releaseEvents] release, even if this empties
   * [key]'s queue.
   */
  fun cancelOne(key: FoulTimerPlayer, id: Long) {
    val now = TimeSource.Monotonic.markNow()
    val existing = _queues.value[key] ?: return
    val updated = existing.cancelled(id, now)
    _queues.value = if (updated == null) _queues.value - key else _queues.value + (key to updated)
    refresh(now)
  }

  /** Cancels every foul timer for [key] at once. Never emits a [releaseEvents] release. */
  fun cancelAll(key: FoulTimerPlayer) {
    _queues.value = _queues.value - key
    refresh(TimeSource.Monotonic.markNow())
  }

  /**
   * Prints every player's current foul-timer queues, for debugging (see [GameScreen]'s debug
   * button).
   */
  fun printDebugSummary() {
    println("Foul timer queues: ${_queues.value}")
  }

  private fun refresh(now: ComparableTimeMark) {
    val next = LinkedHashMap<FoulTimerPlayer, PlayerFoulTimers>()
    _queues.value.forEach { (key, timers) ->
      val updated = timers.updated(now)
      if (updated != null) next[key] = updated else _releaseEvents.tryEmit(key)
    }
    _queues.value = next
    _remainingTimes.value = next.mapValues { (_, timers) -> timers.remainingTime(now) }
    _details.value = next.mapValues { (_, timers) -> timers.toDetails(now) }
  }

  private companion object {
    val TICK_INTERVAL = 10.milliseconds
  }
}

private fun PlayerFoulTimers.toDetails(now: ComparableTimeMark): List<FoulTimerDetail> =
  listOf(FoulTimerDetail(running.id, runningRemainingTime(now), FoulTimerEntryKind.RUNNING)) +
    queued.map {
      FoulTimerDetail(
        it.id,
        ElapsedTime.of(it.duration) ?: ElapsedTime.zero,
        FoulTimerEntryKind.QUEUED,
      )
    }

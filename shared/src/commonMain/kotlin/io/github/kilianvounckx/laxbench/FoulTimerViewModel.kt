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
 * The outcome of cancelling one specific foul-timer entry by id via
 * [FoulTimerViewModel.cancelById]: [NOT_FOUND] if no entry with that id exists in any player's
 * queue (e.g. it was already cancelled or had already naturally expired); [STILL_HAS_TIMERS] if the
 * entry was found and cancelled, but that player still has at least one other running or queued
 * foul-timer entry left afterward, so this cancellation alone did not release them; [RELEASED] if
 * cancelling this entry emptied that player's entire queue -- this cancellation is what fully
 * released them.
 */
enum class FoulTimerCancelOutcome {
  NOT_FOUND,
  STILL_HAS_TIMERS,
  RELEASED,
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
 * simultaneous-foul batch, in logging order -- see [FoulDialog]/[GameScreen]), taking the foul's
 * [id] supplied externally (matching the [Foul.id] from [FoulViewModel]), starting a brand new
 * queue for that player if they have none yet, or appending to the end of their existing queue
 * otherwise -- [PlayerFoulTimers] handles which of those two happens and enforces the FIFO
 * ordering.
 *
 * [adjustDuration] changes a running-or-queued timer's duration by id, re-running the natural
 * expiry cascade if the adjustment brings any timer's remaining time to zero or below, firing
 * [releaseEvents] exactly as if the natural expiry had occurred. [cancelById] cancels a specific
 * timer by id without firing [releaseEvents] -- the caller is responsible for any UI feedback for
 * manual cancellation (see [GameScreen]), and returns a [FoulTimerCancelOutcome] to distinguish
 * whether the entry was found, whether the player was fully released, or whether timers remain.
 *
 * [pause]/[resume] pause/resume every player's running entry at once, called from [GameScreen] at
 * exactly the same "Stop all clocks"/"Resume game" transitions that already pause/resume the game
 * clock and the time-out countdown, and also from the automatic quarter-boundary auto-stop /
 * resume-into-next-quarter transitions -- mirroring [TimeOutCountdownViewModel]'s explicit
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
   * [severity]. [id] must be the same as the [Foul.id] returned by [FoulViewModel.recordFoul] for
   * the same foul, used to later identify this timer entry for adjustment or cancellation by Manage
   * Game. [isGameClockRunning] must reflect the game clock's run state at the moment the foul was
   * recorded, only used if this is that player's first outstanding timer.
   */
  fun recordFoul(
    team: ScoreViewModel.Team,
    player: PlayerNumber,
    severity: FoulSeverity,
    id: Long,
    isGameClockRunning: Boolean,
  ) {
    val now = TimeSource.Monotonic.markNow()
    val key = FoulTimerPlayer(team, player)
    val entry = FoulTimerEntry(id = id, duration = severity.timerDuration)
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
   * Adjusts the remaining duration of the timer entry identified by [id] to [newDuration]. If a
   * queue's running or queued timer has its duration changed, the natural expiry cascade is re-run:
   * if the adjustment brings the remaining time to zero or below, it is treated like a natural
   * expiry and fires [releaseEvents], otherwise it simply continues its countdown with the new
   * remaining time. Returns `true` if an entry with [id] was found and adjusted, `false` otherwise.
   */
  fun adjustDuration(id: Long, newDuration: kotlin.time.Duration): Boolean {
    val key = keyFor(id) ?: return false
    val existing = _queues.value[key] ?: return false
    _queues.value = _queues.value + (key to existing.withDuration(id, newDuration))
    refresh(TimeSource.Monotonic.markNow())
    return true
  }

  /**
   * Cancels the specific timer entry identified by [id], without firing [releaseEvents] -- the
   * caller must handle any UI feedback for manual cancellation. Returns
   * [FoulTimerCancelOutcome.NOT_FOUND] if no entry with [id] exists,
   * [FoulTimerCancelOutcome.RELEASED] if cancelling it emptied that player's whole queue, or
   * [FoulTimerCancelOutcome.STILL_HAS_TIMERS] if the player still has at least one other running or
   * queued entry left. [movePlayer] handles the separate case of a foul's player number itself
   * being corrected, moving its timer entry to the corrected player's queue while preserving its
   * live remaining time.
   */
  fun cancelById(id: Long): FoulTimerCancelOutcome {
    val key = keyFor(id) ?: return FoulTimerCancelOutcome.NOT_FOUND
    val existing = _queues.value[key] ?: return FoulTimerCancelOutcome.NOT_FOUND
    val now = TimeSource.Monotonic.markNow()
    val updated = existing.cancelled(id, now)
    _queues.value = if (updated == null) _queues.value - key else _queues.value + (key to updated)
    refresh(now)
    return if (updated == null) FoulTimerCancelOutcome.RELEASED
    else FoulTimerCancelOutcome.STILL_HAS_TIMERS
  }

  /**
   * Moves the foul-timer entry identified by [id] from whichever [FoulTimerPlayer] it currently
   * belongs to onto [team]/[newPlayer] instead -- called when Manage Game corrects a foul's player
   * number after the fact (see [GameScreen.updateFoul]). A no-op if no entry with [id] exists
   * anywhere, or if it is already filed under [team]/[newPlayer].
   *
   * Otherwise, the entry's current *remaining* time is carried over -- its live countdown if it was
   * the [FoulTimerEntryKind.RUNNING] entry, or its untouched full duration if it was
   * [FoulTimerEntryKind.QUEUED] -- never its original, possibly already-partially-elapsed duration,
   * since that time has genuinely already passed under the correct player too. The entry is then
   * re-inserted at [team]/[newPlayer] exactly as a freshly recorded foul would be via [recordFoul]:
   * immediately running if that player has no other outstanding timers, or enqueued behind their
   * existing ones otherwise. [isGameClockRunning] is used the same way [recordFoul] uses it, only
   * if [team]/[newPlayer] has no existing queue yet.
   *
   * The player the entry is moved away from keeps whatever remains of their own queue afterward
   * (possibly now fully released), mirroring [PlayerFoulTimers.cancelled]. Like [cancelById], this
   * never itself fires [releaseEvents] for either player -- the caller decides whether any UI
   * feedback is warranted; [GameScreen] currently shows none for this case, treating it as a plain
   * correction rather than a release.
   */
  fun movePlayer(
    team: ScoreViewModel.Team,
    id: Long,
    newPlayer: PlayerNumber,
    isGameClockRunning: Boolean,
  ) {
    val oldKey = keyFor(id) ?: return
    val newKey = FoulTimerPlayer(team, newPlayer)
    if (oldKey == newKey) return
    val now = TimeSource.Monotonic.markNow()
    val oldTimers = _queues.value.getValue(oldKey)
    val remainingDuration =
      if (oldTimers.running.id == id) oldTimers.runningRemainingTime(now).duration
      else oldTimers.queued.first { it.id == id }.duration
    val afterRemoval = oldTimers.cancelled(id, now)
    val movedEntry = FoulTimerEntry(id = id, duration = remainingDuration)
    _queues.value =
      _queues.value.toMutableMap().apply {
        if (afterRemoval == null) remove(oldKey) else this[oldKey] = afterRemoval
        val existingNewTimers = this[newKey]
        this[newKey] =
          existingNewTimers?.enqueued(movedEntry)
            ?: PlayerFoulTimers.started(movedEntry, now, isGameClockRunning)
      }
    refresh(now)
  }

  /**
   * Finds which player's queue contains a foul-timer entry identified by [id], or `null` if no such
   * entry exists.
   */
  private fun keyFor(id: Long): FoulTimerPlayer? =
    _queues.value.entries
      .firstOrNull { (_, timers) -> timers.running.id == id || timers.queued.any { it.id == id } }
      ?.key

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

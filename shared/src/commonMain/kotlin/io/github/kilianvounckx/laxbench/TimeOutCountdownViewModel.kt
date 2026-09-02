package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kilianvounckx.laxbench.domain.CountdownState
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs the 90-second time-out countdown described in the time-out countdown feature.
 *
 * [start] begins the countdown silently (not yet [isVisible]) the instant the game clock pauses
 * (see [App] and [TimerViewModel]). [show] reveals the already-running countdown in the UI, exactly
 * where it has already reached -- called once the time-out pop-up's outcome is known to be a
 * recorded team time-out. [cancel] discards the countdown entirely, whether it was ever shown or
 * not, as if it had never started -- called for every other outcome: "Officials Time-out",
 * dismissing the pop-up, or clicking "Resume game" at any point.
 *
 * There is no pause/resume for this countdown itself, and no queueing: per the feature story, at
 * most one time-out countdown is ever live at a time, since the UI only allows starting a new one
 * (by pausing the clock again) after the previous one has already been resolved via [cancel].
 *
 * [io.github.kilianvounckx.laxbench.domain.CountdownState] (a pure domain type, like
 * [io.github.kilianvounckx.laxbench.domain.TimerState]) holds the timing data; this class only
 * holds the current (nullable) state and re-derives [remainingTime]/[isExpired] every
 * [TICK_INTERVAL], as well as immediately on every [start]/[cancel] call so the displayed value
 * updates instantly rather than waiting for the next tick -- mirroring [TimerViewModel.toggle]'s
 * immediate re-derivation. [isVisible] is not part of [CountdownState] at all: it is a pure
 * UI-visibility concern, controlled only by [show] and [cancel], independent of whether the
 * countdown is silently running, visibly counting down, or expired and blinking.
 */
class TimeOutCountdownViewModel : ViewModel() {

  private val _state = MutableStateFlow<CountdownState?>(null)

  private val _remainingTime = MutableStateFlow<ElapsedTime?>(null)
  val remainingTime: StateFlow<ElapsedTime?> = _remainingTime.asStateFlow()

  private val _isExpired = MutableStateFlow(false)
  val isExpired: StateFlow<Boolean> = _isExpired.asStateFlow()

  private val _isVisible = MutableStateFlow(false)
  val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

  init {
    viewModelScope.launch {
      while (true) {
        refresh(TimeSource.Monotonic.markNow())
        delay(TICK_INTERVAL)
      }
    }
  }

  /** Starts a fresh countdown, silently (not [isVisible]). See class doc. */
  fun start() {
    val now = TimeSource.Monotonic.markNow()
    _state.value = CountdownState.started(DURATION, now)
    _isVisible.value = false
    refresh(now)
  }

  /** Reveals the already-running countdown in the UI. See class doc. */
  fun show() {
    _isVisible.value = true
  }

  /**
   * Discards the countdown entirely, as if it had never started. Safe to call when none is running.
   */
  fun cancel() {
    _state.value = null
    _remainingTime.value = null
    _isExpired.value = false
    _isVisible.value = false
  }

  private fun refresh(now: ComparableTimeMark) {
    _state.update { it?.updated(now) }
    val current = _state.value
    _remainingTime.value = current?.remainingTime(now)
    _isExpired.value = current is CountdownState.Expired
  }

  private companion object {
    val TICK_INTERVAL = 10.milliseconds
    val DURATION: Duration = 90.seconds
  }
}

package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kilianvounckx.laxbench.domain.CountdownState
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs an intermission countdown of a caller-supplied duration (2 or 10 minutes, depending on which
 * quarter just ended -- see
 * [io.github.kilianvounckx.laxbench.domain.Quarter.intermissionDuration]), starting automatically
 * each time a quarter ends (via [start]) and stopping when the game resumes into the next quarter
 * (via [cancel]).
 *
 * Reuses [CountdownState] -- the same domain type backing [TimeOutCountdownViewModel]'s fixed
 * 90-second time-out countdown -- since "count down from a duration to zero" is exactly the same
 * rule in both cases; only the duration, and what this class does around it, differ. Unlike
 * [TimeOutCountdownViewModel], this class has no separate show/visibility step (an intermission is
 * always immediately visible once started) and does not publish `isExpired` (reaching zero has no
 * special effect beyond [remainingTime] reading [ElapsedTime.zero] -- no blinking or other visual
 * alarm, per this feature's non-goals).
 *
 * [start] is only ever called automatically by `GameScreen` reacting to
 * `TimerViewModel.quarterEndedEvents`, and [cancel] is only ever called when the scorekeeper
 * resumes into the next quarter -- there is no manual start/pause/restart entry point anywhere in
 * the UI.
 */
class IntermissionCountdownViewModel : ViewModel() {

  private val _state = MutableStateFlow<CountdownState?>(null)

  private val _remainingTime = MutableStateFlow<ElapsedTime?>(null)
  val remainingTime: StateFlow<ElapsedTime?> = _remainingTime.asStateFlow()

  init {
    viewModelScope.launch {
      while (true) {
        refresh(TimeSource.Monotonic.markNow())
        delay(TICK_INTERVAL)
      }
    }
  }

  /** Starts a fresh intermission countdown with the given [duration]. */
  fun start(duration: Duration) {
    val now = TimeSource.Monotonic.markNow()
    _state.value = CountdownState.started(duration, now)
    refresh(now)
  }

  /** Discards the countdown entirely. Safe to call when none is running. */
  fun cancel() {
    _state.value = null
    _remainingTime.value = null
  }

  private fun refresh(now: ComparableTimeMark) {
    _state.update { it?.updated(now) }
    _remainingTime.value = _state.value?.remainingTime(now)
  }

  private companion object {
    val TICK_INTERVAL = 10.milliseconds
  }
}

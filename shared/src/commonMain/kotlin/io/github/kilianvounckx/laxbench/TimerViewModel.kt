package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.Quarter
import io.github.kilianvounckx.laxbench.domain.TimerState
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Publishes the time elapsed since this [TimerViewModel] was created, and lets that timer be paused
 * and resumed any number of times via [toggle].
 *
 * The timer does not start automatically: it is created in a not-yet-started state showing zero
 * elapsed time, and only begins counting up the first time [toggle] is called. All pause/resume
 * bookkeeping — freezing the displayed value on pause and resuming it from exactly that value with
 * no drift or double-counting of paused time — is done by [TimerState], a pure domain type; this
 * class only holds the current [TimerState], re-derives [elapsedTime] and [runState] immediately
 * whenever [toggle] is called (rather than waiting up to [TICK_INTERVAL] for the next tick), so the
 * displayed value and button label update instantly on click.
 *
 * The clock additionally auto-stops (and, for the 4th quarter, permanently locks) at quarter
 * boundaries (15:00, 30:00, 45:00, 60:00), detected via [Quarter.quarterJustEnded] to never skip a
 * boundary even if a single tick jumps far enough to pass multiple. This is published via
 * [quarterEndedEvents], which fires exactly once per quarter boundary crossed (in cumulative
 * elapsed time, never repeating), allowing the surrounding UI to react by starting an intermission
 * countdown or checking for a tie.
 *
 * As an androidx.lifecycle `ViewModel` obtained via the Compose Multiplatform `viewModel()` API, a
 * single instance of this class is retained by the platform's `ViewModelStore` for as long as its
 * owner is alive. On Android that includes surviving a configuration change such as rotation: the
 * same instance (same [TimerState], same coroutine already ticking in [viewModelScope]) is reused
 * rather than recreated, so the displayed time and pause/resume state keep counting/holding
 * correctly across rotation with no Android-specific code required here or anywhere else in the
 * app.
 */
class TimerViewModel : ViewModel() {

  /**
   * The four run/pause statuses [TimerState] can be in, without the timing payload ([TimerState]
   * carries the accumulated/mark/elapsed data needed to compute [ElapsedTime]; this enum exists
   * purely so callers such as [App] can switch on status — e.g. for the button label — without
   * depending on [TimerState] or its internal fields).
   */
  enum class RunState {
    NotStarted,
    Running,
    Paused,
    Locked,
  }

  private val _state = MutableStateFlow<TimerState>(TimerState.NotStarted)

  private val _elapsedTime = MutableStateFlow(ElapsedTime.zero)
  val elapsedTime: StateFlow<ElapsedTime> = _elapsedTime.asStateFlow()

  private val _runState = MutableStateFlow(RunState.NotStarted)
  val runState: StateFlow<RunState> = _runState.asStateFlow()

  private val _quarterEndedEvents = MutableSharedFlow<Quarter>(extraBufferCapacity = 4)
  val quarterEndedEvents: SharedFlow<Quarter> = _quarterEndedEvents

  init {
    viewModelScope.launch {
      while (true) {
        refresh(TimeSource.Monotonic.markNow())
        delay(TICK_INTERVAL)
      }
    }
  }

  /** Toggles between running and paused; see [TimerState.toggled]. */
  fun toggle() {
    val now = TimeSource.Monotonic.markNow()
    _state.update { it.toggled(now) }
    refresh(now)
  }

  private fun refresh(now: ComparableTimeMark) {
    val previousElapsed = _elapsedTime.value
    val stateBeforeCheck = _state.value
    var liveElapsed = stateBeforeCheck.elapsedTime(now)

    if (stateBeforeCheck is TimerState.Running) {
      Quarter.quarterJustEnded(previousElapsed, liveElapsed)?.let { quarterEnded ->
        val boundary = quarterEnded.endTime
        val newState =
          if (quarterEnded == Quarter.FOURTH) TimerState.Locked(boundary)
          else TimerState.Paused(boundary)
        _state.value = newState
        liveElapsed = boundary
        _quarterEndedEvents.tryEmit(quarterEnded)
      }
    }

    _elapsedTime.value = liveElapsed
    _runState.value =
      when (_state.value) {
        is TimerState.NotStarted -> RunState.NotStarted
        is TimerState.Running -> RunState.Running
        is TimerState.Paused -> RunState.Paused
        is TimerState.Locked -> RunState.Locked
      }
  }

  private companion object {
    val TICK_INTERVAL = 10.milliseconds
  }
}

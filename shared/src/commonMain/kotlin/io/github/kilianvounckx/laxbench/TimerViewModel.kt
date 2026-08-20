package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Publishes the time elapsed since this [TimerViewModel] was created.
 *
 * The timer starts automatically the instant this instance is created (i.e. the instant the app
 * launches) and runs indefinitely: there is deliberately no way to stop, pause, or reset it — no
 * such method exists on this class, matching the explicit "no start/stop/pause/reset controls of
 * any kind" requirement. A single monotonic start mark is recorded once, in the property
 * initializer, and every tick simply recomputes elapsed time as "now minus that mark" — so the
 * published value never drifts from coroutine scheduling jitter and there is no per-tick
 * accumulation state to keep in sync.
 *
 * As an androidx.lifecycle `ViewModel` obtained via the Compose Multiplatform `viewModel()` API, a
 * single instance of this class is retained by the platform's `ViewModelStore` for as long as its
 * owner is alive. On Android that includes surviving a configuration change such as rotation: the
 * same instance (same start mark, same coroutine already ticking in [viewModelScope]) is reused
 * rather than recreated, so the displayed time keeps counting up correctly across rotation with no
 * Android-specific code required here or anywhere else in the app.
 */
class TimerViewModel : ViewModel() {

  private val startMark = TimeSource.Monotonic.markNow()

  private val _elapsedTime = MutableStateFlow(ElapsedTime.zero)
  val elapsedTime: StateFlow<ElapsedTime> = _elapsedTime.asStateFlow()

  init {
    viewModelScope.launch {
      while (true) {
        _elapsedTime.value = ElapsedTime.of(startMark.elapsedNow()) ?: ElapsedTime.zero
        delay(TICK_INTERVAL)
      }
    }
  }

  private companion object {
    val TICK_INTERVAL = 10.milliseconds
  }
}

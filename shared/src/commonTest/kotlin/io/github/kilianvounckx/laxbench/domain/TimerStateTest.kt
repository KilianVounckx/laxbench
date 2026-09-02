package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class TimerStateTest {

  @Test
  fun `NotStarted has zero elapsed time regardless of how much time passes`() {
    val timeSource = TestTimeSource()
    val state: TimerState = TimerState.NotStarted
    assertEquals(ElapsedTime.zero, state.elapsedTime(timeSource.markNow()))
    timeSource += 5.seconds
    assertEquals(ElapsedTime.zero, state.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `toggled on NotStarted starts running from zero at now`() {
    val timeSource = TestTimeSource()
    val mark = timeSource.markNow()
    val running = TimerState.NotStarted.toggled(mark)
    assertEquals(ElapsedTime.zero, running.elapsedTime(mark))
    timeSource += 1500.milliseconds
    assertEquals(ElapsedTime.of(1500.milliseconds), running.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `elapsedTime of a running state grows with time since its mark`() {
    val timeSource = TestTimeSource()
    val state = TimerState.NotStarted.toggled(timeSource.markNow())
    timeSource += 1500.milliseconds
    assertEquals(ElapsedTime.of(1500.milliseconds), state.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `toggled on a running state freezes elapsed time as of now`() {
    val timeSource = TestTimeSource()
    val state = TimerState.NotStarted.toggled(timeSource.markNow())
    timeSource += 2.seconds
    val paused = state.toggled(timeSource.markNow())
    timeSource += 5.seconds
    assertEquals(ElapsedTime.of(2.seconds), paused.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `toggled on a paused state resumes from its frozen elapsed time with no jump`() {
    val timeSource = TestTimeSource()
    val state = TimerState.NotStarted.toggled(timeSource.markNow())
    timeSource += 2.seconds
    val paused = state.toggled(timeSource.markNow())
    timeSource += 5.seconds
    val resumeMark = timeSource.markNow()
    val running = paused.toggled(resumeMark)
    assertEquals(ElapsedTime.of(2.seconds), running.elapsedTime(resumeMark))
  }

  @Test
  fun `elapsedTime of a resumed state continues advancing from the paused value`() {
    val timeSource = TestTimeSource()
    val state = TimerState.NotStarted.toggled(timeSource.markNow())
    timeSource += 2.seconds
    val paused = state.toggled(timeSource.markNow())
    timeSource += 5.seconds
    val running = paused.toggled(timeSource.markNow())
    timeSource += 3.seconds
    assertEquals(ElapsedTime.of(5.seconds), running.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `repeated pause and resume cycles never accumulate drift`() {
    val timeSource = TestTimeSource()
    var state: TimerState = TimerState.NotStarted.toggled(timeSource.markNow())
    timeSource += 1.seconds
    state = state.toggled(timeSource.markNow()) // pause at 1s
    timeSource += 10.seconds // paused gap, must be ignored
    state = state.toggled(timeSource.markNow()) // resume
    timeSource += 1.seconds
    state = state.toggled(timeSource.markNow()) // pause at 2s total
    timeSource += 10.seconds // paused gap, must be ignored
    state = state.toggled(timeSource.markNow()) // resume
    timeSource += 1.seconds
    assertEquals(ElapsedTime.of(3.seconds), state.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `Locked elapsedTime returns its frozen value regardless of how much time passes`() {
    val timeSource = TestTimeSource()
    val frozen = ElapsedTime.of(30.seconds)!!
    val state: TimerState = TimerState.Locked(frozen)
    assertEquals(frozen, state.elapsedTime(timeSource.markNow()))
    timeSource += 5.seconds
    assertEquals(frozen, state.elapsedTime(timeSource.markNow()))
  }

  @Test
  fun `toggled on Locked returns the exact same Locked value unchanged`() {
    val frozen = ElapsedTime.of(30.seconds)!!
    val state = TimerState.Locked(frozen)
    val toggled = state.toggled(TestTimeSource().markNow())
    assertEquals(state, toggled)
  }

  @Test
  fun `calling toggled multiple times on Locked always returns the same value`() {
    val frozen = ElapsedTime.of(30.seconds)!!
    val state: TimerState = TimerState.Locked(frozen)
    val timeSource = TestTimeSource()
    var current: TimerState = state
    repeat(3) {
      current = current.toggled(timeSource.markNow())
      timeSource += 10.seconds
    }
    assertEquals(state, current)
  }
}

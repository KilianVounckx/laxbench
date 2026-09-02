package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class CountdownStateTest {

  @Test
  fun `started returns a Running state whose remainingTime at the start mark equals the given duration`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val mark = timeSource.markNow()
    val state = CountdownState.started(duration, mark)
    assertEquals(ElapsedTime.of(duration), state.remainingTime(mark))
  }

  @Test
  fun `started returns a Running state whose isExpired is false at the start mark`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val mark = timeSource.markNow()
    val state = CountdownState.started(duration, mark)
    assertEquals(false, state.isExpired(mark))
  }

  @Test
  fun `remainingTime decreases correctly partway through`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 30.seconds
    assertEquals(ElapsedTime.of(60.seconds), state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime floors at ElapsedTime zero once more than duration has elapsed`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 91.seconds
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime floors at ElapsedTime zero far past duration has elapsed`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 200.seconds
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime on Expired is always ElapsedTime zero`() {
    val timeSource = TestTimeSource()
    val state: CountdownState = CountdownState.Expired
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `isExpired is false just before duration`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 89.seconds + 999.milliseconds
    assertEquals(false, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired is true at exactly duration`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += duration
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired is true well past duration`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 100.seconds
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired on Expired is always true`() {
    val timeSource = TestTimeSource()
    val state: CountdownState = CountdownState.Expired
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `updated returns the same Running state unchanged before expiry`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 30.seconds
    val updated = state.updated(timeSource.markNow())
    assertEquals(state, updated)
  }

  @Test
  fun `updated transitions Running to Expired once expired`() {
    val timeSource = TestTimeSource()
    val duration = 90.seconds
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += duration
    val updated = state.updated(timeSource.markNow())
    assertEquals(CountdownState.Expired, updated)
  }

  @Test
  fun `updated on Expired stays Expired`() {
    val timeSource = TestTimeSource()
    val state: CountdownState = CountdownState.Expired
    val updated = state.updated(timeSource.markNow())
    assertEquals(CountdownState.Expired, updated)
  }

  @Test
  fun `different duration (2 minutes) works correctly`() {
    val timeSource = TestTimeSource()
    val duration = 2.minutes
    val state = CountdownState.started(duration, timeSource.markNow())
    assertEquals(ElapsedTime.of(duration), state.remainingTime(timeSource.markNow()))
    timeSource += 30.seconds
    assertEquals(ElapsedTime.of(90.seconds), state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `different duration (10 minutes) after 2 minutes elapsed is not expired`() {
    val timeSource = TestTimeSource()
    val duration = 10.minutes
    val state = CountdownState.started(duration, timeSource.markNow())
    timeSource += 2.minutes
    assertEquals(false, state.isExpired(timeSource.markNow()))
    assertEquals(ElapsedTime.of(8.minutes)!!, state.remainingTime(timeSource.markNow()))
  }
}

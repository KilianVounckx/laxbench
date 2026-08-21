package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class TimeOutCountdownStateTest {

  @Test
  fun `started returns a Running state whose remainingTime at the start mark equals DURATION`() {
    val timeSource = TestTimeSource()
    val mark = timeSource.markNow()
    val state = TimeOutCountdownState.started(mark)
    assertEquals(
      ElapsedTime.of(TimeOutCountdownState.DURATION),
      state.remainingTime(mark),
    )
  }

  @Test
  fun `started returns a Running state whose isExpired is false at the start mark`() {
    val timeSource = TestTimeSource()
    val mark = timeSource.markNow()
    val state = TimeOutCountdownState.started(mark)
    assertEquals(false, state.isExpired(mark))
  }

  @Test
  fun `remainingTime decreases correctly partway through`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 30.seconds
    assertEquals(ElapsedTime.of(60.seconds), state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime floors at ElapsedTime zero once more than DURATION has elapsed`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 91.seconds
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime floors at ElapsedTime zero far past DURATION has elapsed`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 200.seconds
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime on Expired is always ElapsedTime zero`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.Expired
    assertEquals(ElapsedTime.zero, state.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `isExpired is false just before DURATION`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 89.seconds + 999.milliseconds
    assertEquals(false, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired is true at exactly DURATION`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += TimeOutCountdownState.DURATION
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired is true well past DURATION`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 100.seconds
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `isExpired on Expired is always true`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.Expired
    assertEquals(true, state.isExpired(timeSource.markNow()))
  }

  @Test
  fun `updated returns the same Running state unchanged before expiry`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += 30.seconds
    val updated = state.updated(timeSource.markNow())
    assertEquals(state, updated)
  }

  @Test
  fun `updated transitions Running to Expired once expired`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.started(timeSource.markNow())
    timeSource += TimeOutCountdownState.DURATION
    val updated = state.updated(timeSource.markNow())
    assertEquals(TimeOutCountdownState.Expired, updated)
  }

  @Test
  fun `updated on Expired stays Expired`() {
    val timeSource = TestTimeSource()
    val state = TimeOutCountdownState.Expired
    val updated = state.updated(timeSource.markNow())
    assertEquals(TimeOutCountdownState.Expired, updated)
  }
}

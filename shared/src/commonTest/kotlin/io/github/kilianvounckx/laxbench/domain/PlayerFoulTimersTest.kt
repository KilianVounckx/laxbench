package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class PlayerFoulTimersTest {

  @Test
  fun `started with isGameClockRunning true produces a Running state whose remainingTime at the start mark equals the entry's full duration`() {
    val timeSource = TestTimeSource()
    val mark = timeSource.markNow()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, mark, isGameClockRunning = true)
    assertEquals(ElapsedTime.of(30.seconds), timers.runningRemainingTime(mark))
    assertEquals(emptyList(), timers.queued)
  }

  @Test
  fun `started with isGameClockRunning false produces a Paused state with the same remaining time`() {
    val timeSource = TestTimeSource()
    val mark = timeSource.markNow()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, mark, isGameClockRunning = false)
    assertEquals(ElapsedTime.of(30.seconds), timers.runningRemainingTime(mark))
    assertEquals(TimerState.Paused(ElapsedTime.zero), timers.runningState)
  }

  @Test
  fun `runningRemainingTime and remainingTime decrease correctly partway through`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 10.seconds
    assertEquals(ElapsedTime.of(20.seconds), timers.runningRemainingTime(timeSource.markNow()))
    assertEquals(ElapsedTime.of(20.seconds), timers.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `runningRemainingTime floors at zero once more than duration has elapsed without calling updated`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 40.seconds
    assertEquals(ElapsedTime.zero, timers.runningRemainingTime(timeSource.markNow()))
  }

  @Test
  fun `remainingTime sums running entry's remaining time plus full duration of every queued entry`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued1 = FoulTimerEntry(id = 2L, duration = 20.seconds)
    val queued2 = FoulTimerEntry(id = 3L, duration = 10.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued1).enqueued(queued2)
    timeSource += 10.seconds
    // running has 20 seconds left, queued1 has 20 seconds, queued2 has 10 seconds = 50 total
    assertEquals(ElapsedTime.of(50.seconds), timers.remainingTime(timeSource.markNow()))
  }

  @Test
  fun `isRunningExpired is false before duration`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 20.seconds
    assertEquals(false, timers.isRunningExpired(timeSource.markNow()))
  }

  @Test
  fun `isRunningExpired is true at exactly duration`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 30.seconds
    assertEquals(true, timers.isRunningExpired(timeSource.markNow()))
  }

  @Test
  fun `isRunningExpired is true well past duration`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 50.seconds
    assertEquals(true, timers.isRunningExpired(timeSource.markNow()))
  }

  @Test
  fun `updated returns this unchanged before expiry`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 20.seconds
    val updated = timers.updated(timeSource.markNow())
    assertEquals(timers, updated)
  }

  @Test
  fun `updated promotes to the next queued entry once expired with empty queue afterwards`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    timeSource += 30.seconds
    val updated = timers.updated(timeSource.markNow())
    assertEquals(queued, updated!!.running)
    assertEquals(emptyList(), updated.queued)
  }

  @Test
  fun `updated preserves relative order of remaining queued entries`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued1 = FoulTimerEntry(id = 2L, duration = 20.seconds)
    val queued2 = FoulTimerEntry(id = 3L, duration = 10.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued1).enqueued(queued2)
    timeSource += 30.seconds
    val updated = timers.updated(timeSource.markNow())
    assertEquals(queued1, updated!!.running)
    assertEquals(listOf(queued2), updated.queued)
  }

  @Test
  fun `updated on a Paused queue whose running entry is expired promotes to a Paused state`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    timeSource += 30.seconds
    timers = timers.toggled(timeSource.markNow()) // pause the timer
    val updated = timers.updated(timeSource.markNow())
    assertEquals(queued, updated!!.running)
    assertEquals(TimerState.Paused(ElapsedTime.zero), updated.runningState)
  }

  @Test
  fun `updated cascades through two completions in a single call carrying overrun into second promotion`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 10.seconds)
    val queued1 = FoulTimerEntry(id = 2L, duration = 10.seconds)
    val queued2 = FoulTimerEntry(id = 3L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued1).enqueued(queued2)
    // Jump past both running and queued1 with 5 seconds of overrun into queued2
    timeSource += 25.seconds
    val updated = timers.updated(timeSource.markNow())
    assertEquals(queued2, updated!!.running)
    // The promoted entry should start with 5 seconds of carried elapsed time
    val remainingInSecond = updated.runningRemainingTime(timeSource.markNow())
    assertEquals(ElapsedTime.of(15.seconds), remainingInSecond)
  }

  @Test
  fun `updated returns null when the running entry expires with an empty queued`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 30.seconds
    val updated = timers.updated(timeSource.markNow())
    assertNull(updated)
  }

  @Test
  fun `toggled freezes elapsed time on Running to Paused`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    var timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    timeSource += 10.seconds
    timers = timers.toggled(timeSource.markNow())
    timeSource += 5.seconds
    // Should still be at 10 seconds elapsed, so 20 seconds remaining
    assertEquals(ElapsedTime.of(20.seconds), timers.runningRemainingTime(timeSource.markNow()))
  }

  @Test
  fun `toggled resumes without drift on Paused to Running`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    var timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = false)
    timeSource += 5.seconds
    val pauseMark = timeSource.markNow()
    timers = timers.toggled(pauseMark)
    // Should have zero elapsed when resumed, so 30 seconds remaining
    assertEquals(ElapsedTime.of(30.seconds), timers.runningRemainingTime(pauseMark))
    timeSource += 3.seconds
    // Should have 3 seconds elapsed after 3 more seconds, so 27 remaining
    assertEquals(ElapsedTime.of(27.seconds), timers.runningRemainingTime(timeSource.markNow()))
  }

  @Test
  fun `enqueued appends without touching running or runningState`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    val newEntry = FoulTimerEntry(id = 2L, duration = 20.seconds)
    val enqueued = timers.enqueued(newEntry)
    assertEquals(entry, enqueued.running)
    assertEquals(listOf(newEntry), enqueued.queued)
  }

  @Test
  fun `cancelled with running entry id and non-empty queue promotes the next entry`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    val cancelled = timers.cancelled(1L, timeSource.markNow())
    assertEquals(queued, cancelled!!.running)
    assertEquals(emptyList(), cancelled.queued)
  }

  @Test
  fun `cancelled with running entry id and empty queue returns null`() {
    val timeSource = TestTimeSource()
    val entry = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val timers = PlayerFoulTimers.started(entry, timeSource.markNow(), isGameClockRunning = true)
    val cancelled = timers.cancelled(1L, timeSource.markNow())
    assertNull(cancelled)
  }

  @Test
  fun `cancelled with queued entry id removes only that entry preserving order of the rest`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued1 = FoulTimerEntry(id = 2L, duration = 20.seconds)
    val queued2 = FoulTimerEntry(id = 3L, duration = 10.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued1).enqueued(queued2)
    val cancelled = timers.cancelled(2L, timeSource.markNow())
    assertEquals(running, cancelled!!.running)
    assertEquals(listOf(queued2), cancelled.queued)
  }

  @Test
  fun `cancelled with id matching neither returns an equal unchanged copy`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    val cancelled = timers.cancelled(99L, timeSource.markNow())
    assertEquals(timers, cancelled)
  }

  @Test
  fun `cancelled with running entry id while Paused (not yet expired) promotes to Paused at zero carried elapsed`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    timeSource += 10.seconds
    // Pause the timer while running entry still has 20 seconds left (not yet expired)
    timers = timers.toggled(timeSource.markNow())
    val cancelled = timers.cancelled(1L, timeSource.markNow())
    assertEquals(queued, cancelled!!.running)
    assertEquals(TimerState.Paused(ElapsedTime.zero), cancelled.runningState)
  }

  @Test
  fun `cancelled with running entry id while Paused with nonzero overrun promotes to Paused with carried elapsed`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    timeSource += 35.seconds
    // At this point running entry has 5 seconds of overrun (35 - 30 = 5)
    // Pause while overrun exists
    timers = timers.toggled(timeSource.markNow())
    val cancelled = timers.cancelled(1L, timeSource.markNow())
    assertEquals(queued, cancelled!!.running)
    assertEquals(
      TimerState.Paused(ElapsedTime.of(5.seconds) ?: ElapsedTime.zero),
      cancelled.runningState,
    )
  }

  @Test
  fun `withDuration changes running duration when id matches running id`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    val updated = timers.withDuration(1L, 20.seconds)
    assertEquals(20.seconds, updated.running.duration)
    assertEquals(emptyList(), updated.queued)
  }

  @Test
  fun `withDuration changes only the matching queued entry duration`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued1 = FoulTimerEntry(id = 2L, duration = 20.seconds)
    val queued2 = FoulTimerEntry(id = 3L, duration = 10.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued1).enqueued(queued2)
    val updated = timers.withDuration(2L, 25.seconds)
    assertEquals(running, updated.running)
    assertEquals(2, updated.queued.size)
    assertEquals(25.seconds, updated.queued[0].duration)
    assertEquals(10.seconds, updated.queued[1].duration)
  }

  @Test
  fun `withDuration returns unchanged copy when id matches neither`() {
    val timeSource = TestTimeSource()
    val running = FoulTimerEntry(id = 1L, duration = 30.seconds)
    val queued = FoulTimerEntry(id = 2L, duration = 20.seconds)
    var timers = PlayerFoulTimers.started(running, timeSource.markNow(), isGameClockRunning = true)
    timers = timers.enqueued(queued)
    val updated = timers.withDuration(999L, 15.seconds)
    assertEquals(timers, updated)
  }
}

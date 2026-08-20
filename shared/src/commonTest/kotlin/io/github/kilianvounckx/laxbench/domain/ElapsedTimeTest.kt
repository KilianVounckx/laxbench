package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ElapsedTimeTest {

  @Test
  fun `of returns null for a negative duration`() {
    assertNull(ElapsedTime.of((-1).milliseconds))
  }

  @Test
  fun `of returns an instance wrapping a zero duration`() {
    val elapsedTime = ElapsedTime.of(Duration.ZERO)
    assertEquals(Duration.ZERO, elapsedTime?.duration)
  }

  @Test
  fun `of returns an instance wrapping a positive duration`() {
    val duration = 90.seconds
    val elapsedTime = ElapsedTime.of(duration)
    assertEquals(duration, elapsedTime?.duration)
  }

  @Test
  fun `zero wraps a zero duration`() {
    assertEquals(Duration.ZERO, ElapsedTime.zero.duration)
  }

  @Test
  fun `format renders zero with zero-padded minutes, seconds, and hundredths`() {
    assertEquals("00:00.00", ElapsedTime.zero.format())
  }

  @Test
  fun `format renders sub-minute durations with a two-digit hundredths pair`() {
    val elapsedTime = ElapsedTime.of(5.seconds + 500.milliseconds)!!
    assertEquals("00:05.50", elapsedTime.format())
  }

  @Test
  fun `format truncates instead of rounding the hundredths digits`() {
    val elapsedTime = ElapsedTime.of(1.seconds + 234.milliseconds)!!
    assertEquals("00:01.23", elapsedTime.format())
  }

  @Test
  fun `format renders minutes once a minute has elapsed`() {
    val elapsedTime = ElapsedTime.of(1.minutes + 5.seconds + 300.milliseconds)!!
    assertEquals("01:05.30", elapsedTime.format())
  }

  @Test
  fun `format keeps incrementing minutes uncapped past 59 with no hours field`() {
    val elapsedTime = ElapsedTime.of(71.minutes + 45.seconds + 870.milliseconds)!!
    assertEquals("71:45.87", elapsedTime.format())
  }

  @Test
  fun `format grows the minutes field past two digits once 100 minutes have elapsed`() {
    val elapsedTime = ElapsedTime.of(125.minutes)!!
    assertEquals("125:00.00", elapsedTime.format())
  }
}

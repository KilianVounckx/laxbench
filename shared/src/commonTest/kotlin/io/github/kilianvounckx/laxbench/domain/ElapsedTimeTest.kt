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

  @Test
  fun `maskedEdit appends a typed digit onto zero`() {
    val result = ElapsedTime.zero.maskedEdit("00:00.00", "00:00.001")
    assertEquals(ElapsedTime.of(10.milliseconds), result)
  }

  @Test
  fun `maskedEdit shifts digits in from the right, carrying into seconds and minutes`() {
    val current = ElapsedTime.of(12340.milliseconds)!!
    val result = current.maskedEdit("00:12.34", "00:12.345")
    assertEquals(ElapsedTime.of(123450.milliseconds), result)
  }

  @Test
  fun `maskedEdit ignores a non-digit character appended at the end`() {
    val original = ElapsedTime.zero
    val result = original.maskedEdit("00:00.00", "00:00.00a")
    assertEquals(original, result)
  }

  @Test
  fun `maskedEdit removes the last digit on backspace`() {
    val current = ElapsedTime.of(50.milliseconds)!!
    val result = current.maskedEdit("00:00.05", "00:00.0")
    assertEquals(ElapsedTime.zero, result)
  }

  @Test
  fun `maskedEdit backspacing an already-zero value stays at zero`() {
    val original = ElapsedTime.zero
    val result = original.maskedEdit("00:00.00", "00:00.0")
    assertEquals(original, result)
  }

  @Test
  fun `maskedEdit rejects an edit that is not a single trailing append or removal`() {
    val original = ElapsedTime.zero
    val result1 = original.maskedEdit("00:00.00", "99:99.99")
    assertEquals(original, result1)
    val result2 = original.maskedEdit("00:00.00", "00:00.0012")
    assertEquals(original, result2)
  }

  @Test
  fun `maskedEdit coerces the total at the maximum instead of growing without bound`() {
    val current = ElapsedTime.of((999_999_999L * 10).milliseconds)!!
    val result = current.maskedEdit(current.format(), current.format() + "9")
    assertEquals(current, result)
  }

  @Test
  fun `compareTo returns zero for two ElapsedTime instances wrapping equal durations`() {
    val time1 = ElapsedTime.of(5.seconds)!!
    val time2 = ElapsedTime.of(5.seconds)!!
    assertEquals(0, time1.compareTo(time2))
    assertEquals(false, time1 < time2)
    assertEquals(false, time1 > time2)
    assertEquals(true, time1 <= time2)
    assertEquals(true, time1 >= time2)
  }

  @Test
  fun `a smaller ElapsedTime compares less than a larger one`() {
    val smaller = ElapsedTime.zero
    val larger = ElapsedTime.of(1.seconds)!!
    assertEquals(true, smaller < larger)
    assertEquals(false, larger < smaller)
  }

  @Test
  fun `ElapsedTime ordering matches the ordering of the wrapped duration`() {
    val durations = listOf(10.seconds, 2.seconds, 90.seconds)
    val elapsedTimes = durations.map { ElapsedTime.of(it)!! }
    val sorted = elapsedTimes.sorted()
    val expected = listOf(2.seconds, 10.seconds, 90.seconds).map { ElapsedTime.of(it)!! }
    assertEquals(expected, sorted)
  }
}

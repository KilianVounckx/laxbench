package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class QuarterTest {

  @Test
  fun `of returns FIRST for zero elapsed time`() {
    assertEquals(Quarter.FIRST, Quarter.of(ElapsedTime.zero))
  }

  @Test
  fun `of returns FIRST for time just under 15:00`() {
    val elapsed = ElapsedTime.of(14.minutes + 59.seconds + 999.milliseconds)!!
    assertEquals(Quarter.FIRST, Quarter.of(elapsed))
  }

  @Test
  fun `of returns SECOND for exactly 15:00`() {
    val elapsed = ElapsedTime.of(15.minutes)!!
    assertEquals(Quarter.SECOND, Quarter.of(elapsed))
  }

  @Test
  fun `of returns SECOND for time between 15:00 and 30:00`() {
    val elapsed = ElapsedTime.of(22.minutes)!!
    assertEquals(Quarter.SECOND, Quarter.of(elapsed))
  }

  @Test
  fun `of returns THIRD for exactly 30:00`() {
    val elapsed = ElapsedTime.of(30.minutes)!!
    assertEquals(Quarter.THIRD, Quarter.of(elapsed))
  }

  @Test
  fun `of returns THIRD for time between 30:00 and 45:00`() {
    val elapsed = ElapsedTime.of(37.minutes)!!
    assertEquals(Quarter.THIRD, Quarter.of(elapsed))
  }

  @Test
  fun `of returns FOURTH for exactly 45:00`() {
    val elapsed = ElapsedTime.of(45.minutes)!!
    assertEquals(Quarter.FOURTH, Quarter.of(elapsed))
  }

  @Test
  fun `of returns FOURTH for time between 45:00 and 60:00`() {
    val elapsed = ElapsedTime.of(50.minutes)!!
    assertEquals(Quarter.FOURTH, Quarter.of(elapsed))
  }

  @Test
  fun `of returns FOURTH for exactly 60:00`() {
    val elapsed = ElapsedTime.of(60.minutes)!!
    assertEquals(Quarter.FOURTH, Quarter.of(elapsed))
  }

  @Test
  fun `of returns FOURTH for time past 60:00 (defensive clamp, no 5th quarter)`() {
    val elapsed = ElapsedTime.of(61.minutes)!!
    assertEquals(Quarter.FOURTH, Quarter.of(elapsed))
  }

  @Test
  fun `endTime is 15:00 for FIRST`() {
    assertEquals(ElapsedTime.of(15.minutes)!!, Quarter.FIRST.endTime)
  }

  @Test
  fun `endTime is 30:00 for SECOND`() {
    assertEquals(ElapsedTime.of(30.minutes)!!, Quarter.SECOND.endTime)
  }

  @Test
  fun `endTime is 45:00 for THIRD`() {
    assertEquals(ElapsedTime.of(45.minutes)!!, Quarter.THIRD.endTime)
  }

  @Test
  fun `endTime is 60:00 for FOURTH`() {
    assertEquals(ElapsedTime.of(60.minutes)!!, Quarter.FOURTH.endTime)
  }

  @Test
  fun `intermissionDuration is 2 minutes for FIRST`() {
    assertEquals(2.minutes, Quarter.FIRST.intermissionDuration)
  }

  @Test
  fun `intermissionDuration is 10 minutes for SECOND`() {
    assertEquals(10.minutes, Quarter.SECOND.intermissionDuration)
  }

  @Test
  fun `intermissionDuration is 2 minutes for THIRD`() {
    assertEquals(2.minutes, Quarter.THIRD.intermissionDuration)
  }

  @Test
  fun `intermissionDuration is null for FOURTH`() {
    assertNull(Quarter.FOURTH.intermissionDuration)
  }

  @Test
  fun `GAME_DURATION is 60 minutes`() {
    assertEquals(ElapsedTime.of(60.minutes)!!, Quarter.GAME_DURATION)
  }

  @Test
  fun `quarterJustEnded returns null when no boundary is crossed`() {
    val previous = ElapsedTime.of(5.minutes)!!
    val current = ElapsedTime.of(10.minutes)!!
    assertNull(Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns null when previous already equals a boundary and current equals the same value`() {
    val boundary = ElapsedTime.of(15.minutes)!!
    assertNull(Quarter.quarterJustEnded(boundary, boundary))
  }

  @Test
  fun `quarterJustEnded returns FIRST when crossing from just under 15:00 to at or past 15:00`() {
    val previous = ElapsedTime.of(14.minutes + 59.seconds)!!
    val current = ElapsedTime.of(15.minutes)!!
    assertEquals(Quarter.FIRST, Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns SECOND when crossing from just under 30:00 to at or past 30:00`() {
    val previous = ElapsedTime.of(29.minutes + 59.seconds)!!
    val current = ElapsedTime.of(30.minutes)!!
    assertEquals(Quarter.SECOND, Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns THIRD when crossing from just under 45:00 to at or past 45:00`() {
    val previous = ElapsedTime.of(44.minutes + 59.seconds)!!
    val current = ElapsedTime.of(45.minutes)!!
    assertEquals(Quarter.THIRD, Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns FOURTH when crossing from just under 60:00 to at or past 60:00`() {
    val previous = ElapsedTime.of(59.minutes + 59.seconds)!!
    val current = ElapsedTime.of(60.minutes)!!
    assertEquals(Quarter.FOURTH, Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns earliest boundary (FIRST) when jumping past all four boundaries at once`() {
    val previous = ElapsedTime.zero
    val current = ElapsedTime.of(61.minutes)!!
    assertEquals(Quarter.FIRST, Quarter.quarterJustEnded(previous, current))
  }

  @Test
  fun `quarterJustEnded returns null when both previous and current are already past GAME_DURATION`() {
    val previous = ElapsedTime.of(61.minutes)!!
    val current = ElapsedTime.of(65.minutes)!!
    assertNull(Quarter.quarterJustEnded(previous, current))
  }
}

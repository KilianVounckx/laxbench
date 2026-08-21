package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class TimeOutsTest {

  private val firstTimeOut = TimeOut(elapsedTime = ElapsedTime.zero)
  private val secondTimeOut = TimeOut(elapsedTime = ElapsedTime.of(30.seconds)!!)
  private val thirdTimeOut = TimeOut(elapsedTime = ElapsedTime.of(90.seconds)!!)

  @Test
  fun `empty has no time-outs`() {
    assertTrue(TimeOuts.empty.all.isEmpty())
  }

  @Test
  fun `recorded appends a time-out to an empty history`() {
    assertEquals(listOf(firstTimeOut), TimeOuts.empty.recorded(firstTimeOut).all)
  }

  @Test
  fun `recorded appends a time-out after previously recorded time-outs`() {
    val timeOuts =
      TimeOuts.empty.recorded(firstTimeOut).recorded(secondTimeOut).recorded(thirdTimeOut)
    assertEquals(listOf(firstTimeOut, secondTimeOut, thirdTimeOut), timeOuts.all)
  }
}

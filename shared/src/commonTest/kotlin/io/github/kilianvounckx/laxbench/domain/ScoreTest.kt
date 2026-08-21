package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreTest {

  @Test
  fun `zero has a count of zero`() {
    assertEquals(0, Score.zero.count)
  }

  @Test
  fun `incremented from zero returns a count of one`() {
    assertEquals(1, Score.zero.incremented().count)
  }

  @Test
  fun `incremented from a positive count returns one higher`() {
    val score = Score.zero.incremented().incremented().incremented()
    assertEquals(4, score.incremented().count)
  }

  @Test
  fun `decremented from a positive count returns one lower`() {
    val score = Score.zero.incremented().incremented()
    assertEquals(1, score.decremented().count)
  }

  @Test
  fun `decremented from zero stays at zero`() {
    assertEquals(0, Score.zero.decremented().count)
  }

  @Test
  fun `decrementing below zero never goes negative even after further decrements`() {
    val score = Score.zero.decremented().decremented()
    assertEquals(0, score.count)
  }

  @Test
  fun `increment and decrement cycles compose correctly`() {
    val score = Score.zero.incremented().incremented().decremented().incremented()
    assertEquals(2, score.count)
  }
}

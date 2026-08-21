package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalsTest {

  private val firstGoal =
    Goal(scorer = PlayerNumber.of(7)!!, assist = null, elapsedTime = ElapsedTime.zero)
  private val secondGoal =
    Goal(
      scorer = PlayerNumber.of(11)!!,
      assist = PlayerNumber.of(4),
      elapsedTime = ElapsedTime.zero,
    )

  @Test
  fun `empty has no goals`() {
    assertTrue(Goals.empty.all.isEmpty())
  }

  @Test
  fun `recorded appends a goal to an empty history`() {
    assertEquals(listOf(firstGoal), Goals.empty.recorded(firstGoal).all)
  }

  @Test
  fun `recorded appends a goal after previously recorded goals`() {
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal)
    assertEquals(listOf(firstGoal, secondGoal), goals.all)
  }

  @Test
  fun `latestRemoved on an empty history stays empty`() {
    assertTrue(Goals.empty.latestRemoved().all.isEmpty())
  }

  @Test
  fun `latestRemoved removes only the most recently recorded goal`() {
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal).latestRemoved()
    assertEquals(listOf(firstGoal), goals.all)
  }

  @Test
  fun `record and remove cycles compose correctly`() {
    val goals =
      Goals.empty.recorded(firstGoal).recorded(secondGoal).latestRemoved().recorded(secondGoal)
    assertEquals(listOf(firstGoal, secondGoal), goals.all)
  }
}

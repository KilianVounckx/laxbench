package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalsTest {

  private val firstGoal =
    Goal(id = 0, scorer = PlayerNumber.of(7)!!, assist = null, elapsedTime = ElapsedTime.zero)
  private val secondGoal =
    Goal(
      id = 1,
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
  fun `record and remove cycles compose correctly`() {
    val goals =
      Goals.empty
        .recorded(firstGoal)
        .recorded(secondGoal)
        .removed(secondGoal.id)
        .recorded(secondGoal)
    assertEquals(listOf(firstGoal, secondGoal), goals.all)
  }

  @Test
  fun `removed with matching id removes the goal`() {
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal).removed(1)
    assertEquals(listOf(firstGoal), goals.all)
  }

  @Test
  fun `removed with non-matching id leaves goals unchanged`() {
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal).removed(999)
    assertEquals(listOf(firstGoal, secondGoal), goals.all)
  }

  @Test
  fun `updated with matching id replaces the goal`() {
    val updatedGoal = firstGoal.copy(scorer = PlayerNumber.of(42)!!)
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal).updated(updatedGoal)
    assertEquals(listOf(updatedGoal, secondGoal), goals.all)
  }

  @Test
  fun `updated with non-matching id leaves goals unchanged`() {
    val nonExistentGoal =
      Goal(id = 999, scorer = PlayerNumber.of(42)!!, assist = null, elapsedTime = ElapsedTime.zero)
    val goals = Goals.empty.recorded(firstGoal).recorded(secondGoal).updated(nonExistentGoal)
    assertEquals(listOf(firstGoal, secondGoal), goals.all)
  }
}

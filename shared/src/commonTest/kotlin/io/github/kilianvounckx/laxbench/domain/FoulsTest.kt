package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FoulsTest {

  private val firstFoul =
    Foul(
      id = 0,
      player = PlayerNumber.of(7)!!,
      severity = FoulSeverity.Minor(MinorFoulType.HOLDING),
      elapsedTime = ElapsedTime.zero,
    )
  private val secondFoul =
    Foul(
      id = 1,
      player = PlayerNumber.of(11)!!,
      severity = FoulSeverity.Major(MajorFoulType.TRIPPING, FoulDuration.TWO_MINUTES),
      elapsedTime = ElapsedTime.zero,
    )
  private val thirdFoul =
    Foul(
      id = 2,
      player = PlayerNumber.of(4)!!,
      severity = FoulSeverity.Expulsion,
      elapsedTime = ElapsedTime.zero,
    )

  @Test
  fun `empty has no fouls`() {
    assertTrue(Fouls.empty.all.isEmpty())
  }

  @Test
  fun `recorded appends a foul to an empty history`() {
    assertEquals(listOf(firstFoul), Fouls.empty.recorded(firstFoul).all)
  }

  @Test
  fun `recorded appends a foul after previously recorded fouls`() {
    val fouls = Fouls.empty.recorded(firstFoul).recorded(secondFoul).recorded(thirdFoul)
    assertEquals(listOf(firstFoul, secondFoul, thirdFoul), fouls.all)
  }

  @Test
  fun `removed with matching id removes the foul`() {
    val fouls = Fouls.empty.recorded(firstFoul).recorded(secondFoul).recorded(thirdFoul).removed(1)
    assertEquals(listOf(firstFoul, thirdFoul), fouls.all)
  }

  @Test
  fun `removed with non-matching id leaves fouls unchanged`() {
    val fouls =
      Fouls.empty.recorded(firstFoul).recorded(secondFoul).recorded(thirdFoul).removed(999)
    assertEquals(listOf(firstFoul, secondFoul, thirdFoul), fouls.all)
  }

  @Test
  fun `updated with matching id replaces the foul`() {
    val updatedFoul = firstFoul.copy(player = PlayerNumber.of(42)!!)
    val fouls = Fouls.empty.recorded(firstFoul).recorded(secondFoul).updated(updatedFoul)
    assertEquals(listOf(updatedFoul, secondFoul), fouls.all)
  }

  @Test
  fun `updated with non-matching id leaves fouls unchanged`() {
    val nonExistentFoul =
      Foul(
        id = 999,
        player = PlayerNumber.of(42)!!,
        severity = FoulSeverity.Expulsion,
        elapsedTime = ElapsedTime.zero,
      )
    val fouls = Fouls.empty.recorded(firstFoul).recorded(secondFoul).updated(nonExistentFoul)
    assertEquals(listOf(firstFoul, secondFoul), fouls.all)
  }
}

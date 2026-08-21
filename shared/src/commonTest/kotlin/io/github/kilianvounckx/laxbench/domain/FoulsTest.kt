package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FoulsTest {

  private val firstFoul =
    Foul(
      player = PlayerNumber.of(7)!!,
      severity = FoulSeverity.Minor(MinorFoulType.HOLDING),
      elapsedTime = ElapsedTime.zero,
    )
  private val secondFoul =
    Foul(
      player = PlayerNumber.of(11)!!,
      severity = FoulSeverity.Major(MajorFoulType.TRIPPING, FoulDuration.TWO_MINUTES),
      elapsedTime = ElapsedTime.zero,
    )
  private val thirdFoul =
    Foul(
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
}

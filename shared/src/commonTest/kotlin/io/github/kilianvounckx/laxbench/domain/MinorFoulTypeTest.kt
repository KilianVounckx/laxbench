package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MinorFoulTypeTest {
  @Test
  fun `labels are the corrected spellings, in alphabetical order`() {
    assertEquals(
      listOf(
        "Conduct Foul",
        "Goal Crease Violation",
        "Handling The Ball",
        "Holding",
        "Illegal Pick",
        "Illegal Procedure",
        "Interference",
        "Kicking an Opponent's Stick",
        "Offside",
        "Pushing",
        "Stalling",
        "Warding",
        "Withholding the Ball from Play",
      ),
      MinorFoulType.entries.map { it.label },
    )
  }
}

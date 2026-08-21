package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TeamInfoTest {

  @Test
  fun `label formats team name and color as Name (Color)`() {
    val info =
      TeamInfo(
        name = TeamName.parse("Lions")!!,
        color = TeamColor.parse("Red")!!,
      )
    assertEquals("Lions (Red)", info.label())
  }
}

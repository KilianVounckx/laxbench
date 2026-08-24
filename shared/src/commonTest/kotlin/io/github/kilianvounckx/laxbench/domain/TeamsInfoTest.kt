package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TeamsInfoTest {

  @Test
  fun `holds home and visiting team info`() {
    val home = TeamInfo(TeamName.parse("Lions")!!, TeamColor.parse("Red")!!)
    val visiting = TeamInfo(TeamName.parse("Tigers")!!, TeamColor.parse("Blue")!!)
    val teams = TeamsInfo(home, visiting)
    assertEquals(home, teams.home)
    assertEquals(visiting, teams.visiting)
  }
}

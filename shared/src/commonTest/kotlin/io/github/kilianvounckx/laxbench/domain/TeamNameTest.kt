package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeamNameTest {

  @Test
  fun `parse returns a team name for non-blank text`() {
    assertEquals("Lions", TeamName.parse("Lions")?.value)
  }

  @Test
  fun `parse trims surrounding whitespace`() {
    assertEquals("Lions", TeamName.parse("  Lions  ")?.value)
  }

  @Test
  fun `parse returns null for empty string`() {
    assertNull(TeamName.parse(""))
  }

  @Test
  fun `parse returns null for whitespace-only string`() {
    assertNull(TeamName.parse("   "))
  }
}

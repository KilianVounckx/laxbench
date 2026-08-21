package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeamColorTest {

  @Test
  fun `parse returns a color for non-blank text`() {
    assertEquals("Red", TeamColor.parse("Red")?.value)
  }

  @Test
  fun `parse trims surrounding whitespace`() {
    assertEquals("Navy Blue", TeamColor.parse("  Navy Blue  ")?.value)
  }

  @Test
  fun `parse returns null for empty string`() {
    assertNull(TeamColor.parse(""))
  }

  @Test
  fun `parse returns null for whitespace-only string`() {
    assertNull(TeamColor.parse("   "))
  }
}

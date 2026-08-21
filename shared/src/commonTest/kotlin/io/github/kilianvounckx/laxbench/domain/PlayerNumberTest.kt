package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerNumberTest {

  @Test
  fun `of returns null for a negative number`() {
    assertNull(PlayerNumber.of(-1))
  }

  @Test
  fun `of returns an instance wrapping zero`() {
    assertEquals(0, PlayerNumber.of(0)?.number)
  }

  @Test
  fun `of returns an instance wrapping a positive number`() {
    assertEquals(42, PlayerNumber.of(42)?.number)
  }

  @Test
  fun `parse returns null for blank text`() {
    assertNull(PlayerNumber.parse(""))
  }

  @Test
  fun `parse returns null for non-numeric text`() {
    assertNull(PlayerNumber.parse("abc"))
  }

  @Test
  fun `parse returns null for a negative number`() {
    assertNull(PlayerNumber.parse("-1"))
  }

  @Test
  fun `parse returns an instance wrapping zero`() {
    assertEquals(0, PlayerNumber.parse("0")?.number)
  }

  @Test
  fun `parse returns an instance wrapping a positive number`() {
    assertEquals(7, PlayerNumber.parse("7")?.number)
  }

  @Test
  fun `parse trims surrounding whitespace`() {
    assertEquals(9, PlayerNumber.parse("  9  ")?.number)
  }

  @Test
  fun `parse returns null for decimal text`() {
    assertNull(PlayerNumber.parse("1.5"))
  }
}

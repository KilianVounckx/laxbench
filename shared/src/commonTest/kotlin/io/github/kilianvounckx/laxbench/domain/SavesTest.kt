package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SavesTest {

  private val firstSave = Save(elapsedTime = ElapsedTime.zero)
  private val secondSave = Save(elapsedTime = ElapsedTime.of(30.seconds)!!)
  private val thirdSave = Save(elapsedTime = ElapsedTime.of(90.seconds)!!)

  @Test
  fun `empty has no saves`() {
    assertTrue(Saves.empty.all.isEmpty())
  }

  @Test
  fun `recorded appends a save to an empty history`() {
    assertEquals(listOf(firstSave), Saves.empty.recorded(firstSave).all)
  }

  @Test
  fun `recorded appends a save after previously recorded saves`() {
    val saves = Saves.empty.recorded(firstSave).recorded(secondSave).recorded(thirdSave)
    assertEquals(listOf(firstSave, secondSave, thirdSave), saves.all)
  }
}

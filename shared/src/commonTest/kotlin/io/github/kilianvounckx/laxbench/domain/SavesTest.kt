package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SavesTest {

  private val firstSave = Save(id = 0, elapsedTime = ElapsedTime.zero)
  private val secondSave = Save(id = 1, elapsedTime = ElapsedTime.of(30.seconds)!!)
  private val thirdSave = Save(id = 2, elapsedTime = ElapsedTime.of(90.seconds)!!)

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

  @Test
  fun `removed with matching id removes the save`() {
    val saves = Saves.empty.recorded(firstSave).recorded(secondSave).recorded(thirdSave).removed(1)
    assertEquals(listOf(firstSave, thirdSave), saves.all)
  }

  @Test
  fun `removed with non-matching id leaves saves unchanged`() {
    val saves =
      Saves.empty.recorded(firstSave).recorded(secondSave).recorded(thirdSave).removed(999)
    assertEquals(listOf(firstSave, secondSave, thirdSave), saves.all)
  }
}

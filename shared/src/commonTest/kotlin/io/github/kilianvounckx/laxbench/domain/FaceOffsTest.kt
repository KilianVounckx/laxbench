package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class FaceOffsTest {

  private val firstFaceOff = FaceOff(elapsedTime = ElapsedTime.zero)
  private val secondFaceOff = FaceOff(elapsedTime = ElapsedTime.of(30.seconds)!!)
  private val thirdFaceOff = FaceOff(elapsedTime = ElapsedTime.of(90.seconds)!!)

  @Test
  fun `empty has no face-off wins`() {
    assertTrue(FaceOffs.empty.all.isEmpty())
  }

  @Test
  fun `recorded appends a face-off win to an empty history`() {
    assertEquals(listOf(firstFaceOff), FaceOffs.empty.recorded(firstFaceOff).all)
  }

  @Test
  fun `recorded appends a face-off win after previously recorded face-off wins`() {
    val faceOffs =
      FaceOffs.empty.recorded(firstFaceOff).recorded(secondFaceOff).recorded(thirdFaceOff)
    assertEquals(listOf(firstFaceOff, secondFaceOff, thirdFaceOff), faceOffs.all)
  }
}

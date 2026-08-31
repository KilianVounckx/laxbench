package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class FaceOffsTest {

  private val firstFaceOff = FaceOff(id = 0, elapsedTime = ElapsedTime.zero)
  private val secondFaceOff = FaceOff(id = 1, elapsedTime = ElapsedTime.of(30.seconds)!!)
  private val thirdFaceOff = FaceOff(id = 2, elapsedTime = ElapsedTime.of(90.seconds)!!)

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

  @Test
  fun `removed with matching id removes the face-off`() {
    val faceOffs =
      FaceOffs.empty
        .recorded(firstFaceOff)
        .recorded(secondFaceOff)
        .recorded(thirdFaceOff)
        .removed(1)
    assertEquals(listOf(firstFaceOff, thirdFaceOff), faceOffs.all)
  }

  @Test
  fun `removed with non-matching id leaves face-offs unchanged`() {
    val faceOffs =
      FaceOffs.empty
        .recorded(firstFaceOff)
        .recorded(secondFaceOff)
        .recorded(thirdFaceOff)
        .removed(999)
    assertEquals(listOf(firstFaceOff, secondFaceOff, thirdFaceOff), faceOffs.all)
  }
}

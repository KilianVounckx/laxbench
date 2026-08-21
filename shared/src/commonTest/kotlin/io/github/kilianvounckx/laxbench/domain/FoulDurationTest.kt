package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class FoulDurationTest {
  @Test
  fun `labels are in ascending order of duration`() {
    assertEquals(
      listOf("1 Minute", "2 Minutes", "3 Minutes"),
      FoulDuration.entries.map { it.label },
    )
  }
}

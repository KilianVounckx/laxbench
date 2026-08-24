package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class FoulDurationTest {
  @Test
  fun `labels are in ascending order of duration`() {
    assertEquals(
      listOf("1 Minute", "2 Minutes", "3 Minutes"),
      FoulDuration.entries.map { it.label },
    )
  }

  @Test
  fun `lengths are in ascending order of duration`() {
    assertEquals(
      listOf(1.minutes, 2.minutes, 3.minutes),
      FoulDuration.entries.map { it.length },
    )
  }
}

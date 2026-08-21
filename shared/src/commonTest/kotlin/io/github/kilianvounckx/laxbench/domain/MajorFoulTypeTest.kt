package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MajorFoulTypeTest {
  @Test
  fun `labels are in alphabetical order`() {
    assertEquals(
      listOf(
        "Cross-Check",
        "Illegal Body-Check",
        "Slashing",
        "Tripping",
        "Unnecessary Roughness",
        "Unsportsmanlike Conduct",
      ),
      MajorFoulType.entries.map { it.label },
    )
  }
}

package io.github.kilianvounckx.laxbench.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FoulSeverityTest {

  @Test
  fun `Minor timerDuration is 30 seconds for every MinorFoulType`() {
    MinorFoulType.entries.forEach { type ->
      val severity = FoulSeverity.Minor(type)
      assertEquals(30.seconds, severity.timerDuration)
    }
  }

  @Test
  fun `Major timerDuration equals the chosen FoulDuration's length`() {
    val combinations =
      MajorFoulType.entries.flatMap { type ->
        FoulDuration.entries.map { duration -> type to duration }
      }
    combinations.forEach { (type, duration) ->
      val severity = FoulSeverity.Major(type, duration)
      assertEquals(duration.length, severity.timerDuration)
    }
  }

  @Test
  fun `Expulsion timerDuration is 5 minutes`() {
    assertEquals(5.minutes, FoulSeverity.Expulsion.timerDuration)
  }
}

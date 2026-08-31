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

  @Test
  fun `Minor typeLabel returns the MinorFoulType label`() {
    MinorFoulType.entries.forEach { type ->
      val severity = FoulSeverity.Minor(type)
      assertEquals(type.label, severity.typeLabel)
    }
  }

  @Test
  fun `Major typeLabel returns the MajorFoulType label`() {
    MajorFoulType.entries.forEach { type ->
      FoulDuration.entries.forEach { duration ->
        val severity = FoulSeverity.Major(type, duration)
        assertEquals(type.label, severity.typeLabel)
      }
    }
  }

  @Test
  fun `Expulsion typeLabel returns Expulsion`() {
    assertEquals("Expulsion", FoulSeverity.Expulsion.typeLabel)
  }

  @Test
  fun `Minor durationLabel returns null`() {
    MinorFoulType.entries.forEach { type ->
      val severity = FoulSeverity.Minor(type)
      assertEquals(null, severity.durationLabel)
    }
  }

  @Test
  fun `Major durationLabel returns the FoulDuration label`() {
    MajorFoulType.entries.forEach { type ->
      FoulDuration.entries.forEach { duration ->
        val severity = FoulSeverity.Major(type, duration)
        assertEquals(duration.label, severity.durationLabel)
      }
    }
  }

  @Test
  fun `Expulsion durationLabel returns null`() {
    assertEquals(null, FoulSeverity.Expulsion.durationLabel)
  }
}

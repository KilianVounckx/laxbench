package io.github.kilianvounckx.laxbench

import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.MinorFoulType
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ScoreSheetPdfRendererTest {

  @Test
  fun `empty scoresheet still renders headers and placeholders`() {
    val data =
      ScoreSheetData(
        goals = emptyList(),
        fouls = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
        homeSaves = 0,
        visitingSaves = 0,
        homeFaceOffs = 0,
        visitingFaceOffs = 0,
      )

    val bytes = data.toPdfBytes()
    val pdf = bytes.decodeToString()

    assertTrue(pdf.startsWith("%PDF-1.4"))
    assertTrue(bytes.isNotEmpty())
    assertTrue(pdf.contains("(Game Scoresheet) Tj"))
    assertTrue(pdf.contains("(Goals) Tj"))
    assertTrue(pdf.contains("(Fouls) Tj"))
    assertTrue(pdf.contains("(None) Tj"))
    assertTrue(pdf.contains("(Home: 0) Tj"))
    assertTrue(pdf.contains("(Visiting: 0) Tj"))
  }

  @Test
  fun `populated scoresheet renders goal, foul, time-out, save and face-off content`() {
    val data =
      ScoreSheetData(
        goals =
          listOf(
            GoalEntry(
              elapsedTime = ElapsedTime.of(5_000.milliseconds)!!,
              team = ScoreViewModel.Team.HOME,
              homeScoreAfter = 1,
              visitingScoreAfter = 0,
              scorer = PlayerNumber.of(7)!!,
              assist = PlayerNumber.of(3)!!,
            )
          ),
        fouls =
          listOf(
            FoulEntry(
              elapsedTime = ElapsedTime.of(3_000.milliseconds)!!,
              team = ScoreViewModel.Team.VISITING,
              player = PlayerNumber.of(9)!!,
              severity = FoulSeverity.Minor(MinorFoulType.HOLDING),
            )
          ),
        homeTimeOuts = listOf(ElapsedTime.of(60_000.milliseconds)!!),
        visitingTimeOuts = emptyList(),
        homeSaves = 4,
        visitingSaves = 2,
        homeFaceOffs = 6,
        visitingFaceOffs = 1,
      )

    val pdf = data.toPdfBytes().decodeToString()

    assertTrue(pdf.contains("(00:05.00) Tj"))
    assertTrue(pdf.contains("(Home) Tj"))
    assertTrue(pdf.contains("(1:0) Tj"))
    assertTrue(pdf.contains("(7) Tj"))
    assertTrue(pdf.contains("(3) Tj"))
    assertTrue(pdf.contains("(00:03.00) Tj"))
    assertTrue(pdf.contains("(Visiting) Tj"))
    assertTrue(pdf.contains("(9) Tj"))
    assertTrue(pdf.contains("(Holding) Tj"))
    assertTrue(pdf.contains("(01:00.00) Tj"))
    assertTrue(pdf.contains("(None) Tj"))
    assertTrue(pdf.contains("(Home: 4) Tj"))
    assertTrue(pdf.contains("(Visiting: 2) Tj"))
    assertTrue(pdf.contains("(Home: 6) Tj"))
    assertTrue(pdf.contains("(Visiting: 1) Tj"))
  }

  @Test
  fun `goals table overflowing onto a second page repeats its column header`() {
    val goals =
      (1..100).map { index ->
        GoalEntry(
          elapsedTime = ElapsedTime.of((index * 1_000).milliseconds)!!,
          team = ScoreViewModel.Team.HOME,
          homeScoreAfter = index,
          visitingScoreAfter = 0,
          scorer = PlayerNumber.of(1)!!,
          assist = null,
        )
      }
    val data =
      ScoreSheetData(
        goals = goals,
        fouls = emptyList(),
        homeTimeOuts = emptyList(),
        visitingTimeOuts = emptyList(),
        homeSaves = 0,
        visitingSaves = 0,
        homeFaceOffs = 0,
        visitingFaceOffs = 0,
      )

    val pdf = data.toPdfBytes().decodeToString()

    val pageObjectCount = Regex("/Type /Page /Parent").findAll(pdf).count()
    assertTrue(pageObjectCount >= 2)
    val headerOccurrences = Regex("\\(Time\\) Tj").findAll(pdf).count()
    assertTrue(headerOccurrences >= 2)
  }
}

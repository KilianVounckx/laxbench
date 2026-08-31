package io.github.kilianvounckx.laxbench

private const val PAGE_WIDTH = 595
private const val PAGE_HEIGHT = 842
private const val MARGIN = 40
private const val ROW_HEIGHT = 14
private const val SECTION_GAP = 20

fun ScoreSheetData.toPdfBytes(): ByteArray {
  val document = PdfDocumentBuilder(PAGE_WIDTH, PAGE_HEIGHT)
  var page = document.addPage()
  var y = PAGE_HEIGHT - MARGIN

  /**
   * Starts a new page (resetting [page]/[y]) if the next row wouldn't fit on the current one. When
   * that happens, [onNewPage] is invoked right after the reset -- used by [table] to redraw a
   * table's header on the fresh page before its next row is drawn.
   */
  fun ensureSpace(onNewPage: () -> Unit = {}) {
    if (y - ROW_HEIGHT < MARGIN) {
      page = document.addPage()
      y = PAGE_HEIGHT - MARGIN
      onNewPage()
    }
  }

  fun line(text: String, bold: Boolean = false, size: Int = 10, x: Int = MARGIN) {
    ensureSpace()
    page.drawText(x, y, text, bold, size)
    y -= ROW_HEIGHT
  }

  /** Returns the plain name of the given team from the scoresheet data. */
  fun nameOf(team: ScoreViewModel.Team): String =
    when (team) {
      ScoreViewModel.Team.HOME -> homeName.value
      ScoreViewModel.Team.VISITING -> visitingName.value
    }

  /**
   * Renders a table: [header] cells drawn once, immediately followed by a hairline separator, then
   * every entry of [rows] (each the list of (x, text) cells for that row). If a row no longer fits
   * on the current page, [header] and its separator are redrawn at the top of the new page before
   * that row is drawn, so a reader looking only at a continuation page still sees the column
   * labels.
   */
  fun table(header: List<Pair<Int, String>>, rows: List<List<Pair<Int, String>>>) {
    fun drawHeader() {
      header.forEach { (x, text) -> page.drawText(x, y, text) }
      page.drawLine(MARGIN, y - 4, PAGE_WIDTH - MARGIN, y - 4)
      y -= ROW_HEIGHT
    }
    ensureSpace()
    drawHeader()
    rows.forEach { cells ->
      ensureSpace(onNewPage = ::drawHeader)
      cells.forEach { (x, text) -> page.drawText(x, y, text) }
      y -= ROW_HEIGHT
    }
  }

  line("Game Scoresheet", bold = true, size = 16)
  y -= SECTION_GAP

  line("Goals", bold = true, size = 12)
  table(
    header =
      listOf(
        40 to "Time",
        105 to "Team",
        165 to "Score (H:V)",
        225 to "Scorer #",
        285 to "Assist #",
      ),
    rows =
      goals.map { g ->
        listOf(
          40 to g.elapsedTime.format(),
          105 to nameOf(g.team),
          165 to "${g.homeScoreAfter}:${g.visitingScoreAfter}",
          225 to g.scorer.number.toString(),
          285 to (g.assist?.number?.toString() ?: ""),
        )
      },
  )
  y -= SECTION_GAP

  line("Fouls", bold = true, size = 12)
  table(
    header =
      listOf(40 to "Time", 105 to "Team", 165 to "Player #", 215 to "Foul", 425 to "Duration"),
    rows =
      fouls.map { f ->
        listOf(
          40 to f.elapsedTime.format(),
          105 to nameOf(f.team),
          165 to f.player.number.toString(),
          215 to f.severity.typeLabel,
          425 to (f.severity.durationLabel ?: ""),
        )
      },
  )
  y -= SECTION_GAP

  line("Time-outs", bold = true, size = 12)
  line(homeName.value, bold = true)
  if (homeTimeOuts.isEmpty()) line("None") else homeTimeOuts.forEach { line(it.format()) }
  line(visitingName.value, bold = true)
  if (visitingTimeOuts.isEmpty()) line("None") else visitingTimeOuts.forEach { line(it.format()) }
  y -= SECTION_GAP

  line("Saves", bold = true, size = 12)
  line("${homeName.value}: $homeSaves")
  line("${visitingName.value}: $visitingSaves")
  y -= SECTION_GAP

  line("Face-offs", bold = true, size = 12)
  line("${homeName.value}: $homeFaceOffs")
  line("${visitingName.value}: $visitingFaceOffs")

  return document.build()
}

package io.github.kilianvounckx.laxbench

import kotlin.test.Test
import kotlin.test.assertTrue

class PdfDocumentBuilderTest {
  @Test
  fun `document with no pages produces valid, minimal PDF bytes`() {
    val document = PdfDocumentBuilder()
    val bytes = document.build()
    val pdf = bytes.decodeToString()

    assertTrue(pdf.startsWith("%PDF-1.4"))
    assertTrue(pdf.contains("/Type /Catalog"))
    assertTrue(pdf.contains("/Count 0"))
    assertTrue(pdf.contains("%%EOF"))
  }

  @Test
  fun `page content stream contains the drawn text and line operators`() {
    val document = PdfDocumentBuilder()
    document.addPage().apply {
      drawText(50, 100, "Test text")
      drawLine(50, 50, 200, 50)
    }

    val pdf = document.build().decodeToString()

    assertTrue(pdf.contains("(Test text) Tj"))
    assertTrue(pdf.contains("50 50 m"))
    assertTrue(pdf.contains("200 50 l"))
    assertTrue(pdf.contains("/Count 1"))
  }
}

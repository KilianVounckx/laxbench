package io.github.kilianvounckx.laxbench

class PdfDocumentBuilder(private val pageWidth: Int = 595, private val pageHeight: Int = 842) {
  private val pages = mutableListOf<PdfPageBuilder>()

  fun addPage(): PdfPageBuilder {
    val page = PdfPageBuilder()
    pages.add(page)
    return page
  }

  fun build(): ByteArray {
    val output = StringBuilder()
    val offsets = mutableListOf<Int>()

    output.append("%PDF-1.4\n")

    offsets.add(output.length) // object 1
    output.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

    offsets.add(output.length) // object 2
    output.append("2 0 obj\n<< /Type /Pages /Kids [")
    for (i in pages.indices) {
      if (i > 0) output.append(" ")
      output.append("${5 + 2 * i} 0 R")
    }
    output.append("] /Count ${pages.size} >>\nendobj\n")

    offsets.add(output.length) // object 3
    output.append("3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n")

    offsets.add(output.length) // object 4
    output.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n")

    for (i in pages.indices) {
      val pageObjNum = 5 + 2 * i
      val contentObjNum = 6 + 2 * i
      val contentBytes = pages[i].contentBytes()

      offsets.add(output.length) // page object
      output.append("$pageObjNum 0 obj\n")
      output.append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $pageWidth $pageHeight]\n")
      output.append("   /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >>\n")
      output.append("   /Contents $contentObjNum 0 R >>\nendobj\n")

      offsets.add(output.length) // content stream object
      output.append("$contentObjNum 0 obj\n<< /Length ${contentBytes.size} >>\nstream\n")
      output.append(contentBytes.decodeToString())
      output.append("\nendstream\nendobj\n")
    }

    val xrefOffset = output.length
    val totalObjects = 4 + 2 * pages.size
    output.append("xref\n0 ${totalObjects + 1}\n")
    output.append(padTo10Digits(0)).append(" 65535 f \n")
    for (i in 1..totalObjects) {
      output.append(padTo10Digits(offsets[i - 1])).append(" 00000 n \n")
    }

    output.append("trailer\n<< /Size ${totalObjects + 1} /Root 1 0 R >>\n")
    output.append("startxref\n$xrefOffset\n%%EOF\n")

    return output.toString().encodeToByteArray()
  }
}

class PdfPageBuilder {
  private val operators = StringBuilder()

  /** Draws [text] with its baseline's left edge at ([x], [y]) in PDF's bottom-left-origin space. */
  fun drawText(x: Int, y: Int, text: String, bold: Boolean = false, size: Int = 10) {
    val font = if (bold) "/F2" else "/F1"
    operators.append("BT\n$font $size Tf\n1 0 0 1 $x $y Tm\n${text.toPdfLiteral()} Tj\nET\n")
  }

  /** Draws a straight line from ([x1], [y1]) to ([x2], [y2]) with a hairline stroke width. */
  fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int) {
    operators.append("0.5 w\n$x1 $y1 m\n$x2 $y2 l\nS\n")
  }

  internal fun contentBytes(): ByteArray = operators.toString().encodeToByteArray()
}

private fun String.toPdfLiteral(): String =
  "(" + replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)") + ")"

private fun padTo10Digits(value: Int): String {
  val str = value.toString()
  return if (str.length >= 10) str else "0".repeat(10 - str.length) + str
}

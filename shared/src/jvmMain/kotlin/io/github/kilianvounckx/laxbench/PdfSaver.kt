package io.github.kilianvounckx.laxbench

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberPdfSaver(): PdfSaver = remember {
  object : PdfSaver {
    override fun save(fileName: String, bytes: ByteArray) {
      val chooser =
        JFileChooser().apply {
          selectedFile = File(fileName)
          fileFilter = FileNameExtensionFilter("PDF files", "pdf")
        }
      if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.writeBytes(bytes)
      }
    }
  }
}

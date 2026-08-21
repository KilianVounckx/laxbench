package io.github.kilianvounckx.laxbench

import androidx.compose.runtime.Composable

interface PdfSaver {
  fun save(fileName: String, bytes: ByteArray)
}

@Composable expect fun rememberPdfSaver(): PdfSaver

package io.github.kilianvounckx.laxbench

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberPdfSaver(): PdfSaver {
  val context = LocalContext.current
  return remember(context) {
    object : PdfSaver {
      override fun save(fileName: String, bytes: ByteArray) {
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        val uri =
          FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
          type = "application/pdf"
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save or share scoresheet"))
      }
    }
  }
}

package io.github.kilianvounckx.laxbench

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberPdfSaver(): PdfSaver = remember {
  object : PdfSaver {
    @OptIn(ExperimentalForeignApi::class)
    override fun save(fileName: String, bytes: ByteArray) {
      val path = NSTemporaryDirectory() + fileName
      val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.convert())
      }
      nsData.writeToFile(path, atomically = true)
      val url = NSURL.fileURLWithPath(path)
      val activityController =
        UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
      UIApplication.sharedApplication.keyWindow
        ?.rootViewController
        ?.presentViewController(
          activityController,
          animated = true,
          completion = null,
        )
    }
  }
}

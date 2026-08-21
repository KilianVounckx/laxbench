package io.github.kilianvounckx.laxbench

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.js.JsAny
import kotlin.js.toJsArray
import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

@Composable
actual fun rememberPdfSaver(): PdfSaver = remember {
  object : PdfSaver {
    @OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
    override fun save(fileName: String, bytes: ByteArray) {
      val uint8Array = Uint8Array(bytes.size)
      for (i in bytes.indices) {
        uint8Array[i] = bytes[i]
      }
      val blobParts = arrayOf<JsAny?>(uint8Array).toJsArray()
      val blob = Blob(blobParts, BlobPropertyBag(type = "application/pdf"))
      val url = URL.createObjectURL(blob)
      val anchor = document.createElement("a") as HTMLAnchorElement
      anchor.href = url
      anchor.download = fileName
      document.body?.appendChild(anchor)
      anchor.click()
      anchor.remove()
      URL.revokeObjectURL(url)
    }
  }
}

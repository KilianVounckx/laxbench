package io.github.kilianvounckx.laxbench.persistence

import kotlinx.browser.window

actual fun createKeyValueStore(): KeyValueStore =
  object : KeyValueStore {
    override fun get(key: String): String? =
      try {
        window.localStorage.getItem(key)
      } catch (e: Throwable) {
        null
      }

    override fun set(key: String, value: String) {
      try {
        window.localStorage.setItem(key, value)
      } catch (e: Throwable) {
        // Best-effort: storage can be unavailable (private browsing) or full (quota exceeded).
        // Autosave must never crash the game screen over this.
      }
    }
  }

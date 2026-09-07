package io.github.kilianvounckx.laxbench.persistence

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object GameStorage {
  private const val SAVE_KEY = "laxbench.saved-game"
  private val store: KeyValueStore = createKeyValueStore()
  private val json = Json { ignoreUnknownKeys = true }

  fun load(): GameSnapshot? {
    val raw = store.get(SAVE_KEY) ?: return null
    return try {
      json.decodeFromString<GameSnapshot>(raw)
    } catch (e: Exception) {
      // Any decode failure (corrupt/foreign/future-incompatible data) is treated as "no save".
      null
    }
  }

  fun save(snapshot: GameSnapshot) {
    store.set(SAVE_KEY, json.encodeToString(snapshot))
  }
}

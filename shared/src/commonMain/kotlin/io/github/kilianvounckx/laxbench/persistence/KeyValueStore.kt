package io.github.kilianvounckx.laxbench.persistence

interface KeyValueStore {
  fun get(key: String): String?

  fun set(key: String, value: String)
}

expect fun createKeyValueStore(): KeyValueStore

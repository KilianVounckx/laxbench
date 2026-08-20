package io.github.kilianvounckx.laxbench

interface Platform {
  val name: String
}

expect fun getPlatform(): Platform
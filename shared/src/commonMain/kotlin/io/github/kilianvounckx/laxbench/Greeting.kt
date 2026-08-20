package io.github.kilianvounckx.laxbench

class Greeting {
  private val platform = getPlatform()

  fun greet(): String {
    return sayHello(platform.name)
  }
}
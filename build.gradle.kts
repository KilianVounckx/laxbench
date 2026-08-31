import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.ktfmt) apply false
  base
}

subprojects {
  apply(plugin = "com.ncorti.ktfmt.gradle")

  extensions.configure<com.ncorti.ktfmt.gradle.KtfmtExtension> {
    googleStyle()
  }

  tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
      allWarningsAsErrors.set(true)
    }
  }
}

// All Gradle dependency and plugin versions must live in gradle/libs.versions.toml.
// This fails the build if a coordinate + literal version sneaks into a build.gradle.kts
// instead (settings.gradle.kts is exempt: the pluginManagement block resolves before the
// version catalog is available, so the foojay-resolver-convention version there is unavoidable).
val checkDependencyVersionCatalogUsage by tasks.registering {
  group = "verification"
  description = "Fails if a build.gradle.kts declares a dependency/plugin version outside gradle/libs.versions.toml."

  val buildFiles =
    fileTree(rootDir) {
        include("**/build.gradle.kts")
        exclude("**/build/**")
      }
      .files
      .toList()
  val rootDirectory = rootDir
  inputs.files(buildFiles)
  outputs.upToDateWhen { true }

  doLast {
    val coordinateWithVersion = Regex("""["'][^"'\s:]+:[^"'\s:]+:[0-9][^"'\s]*["']""")
    val pluginIdWithVersion = Regex("""id\(\s*["'][^"']+["']\s*\)\s*version\s*["']""")
    val offenders = mutableListOf<String>()
    buildFiles.forEach { file ->
      file.readLines().forEachIndexed { index, line ->
        if (line.trim().startsWith("//")) return@forEachIndexed
        if (coordinateWithVersion.containsMatchIn(line) || pluginIdWithVersion.containsMatchIn(line)) {
          offenders += "${file.relativeTo(rootDirectory)}:${index + 1}: ${line.trim()}"
        }
      }
    }
    if (offenders.isNotEmpty()) {
      throw GradleException(
        "Hardcoded dependency/plugin version(s) found outside gradle/libs.versions.toml:\n" +
          offenders.joinToString("\n")
      )
    }
  }
}

tasks.named("check") {
  dependsOn(checkDependencyVersionCatalogUsage)
}
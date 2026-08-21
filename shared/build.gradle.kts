import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kover)
}

// Every non-private domain function must be tested (see CLAUDE.md). Kover can't check
// "one test per function" directly, so this approximates it with a full line-coverage
// requirement scoped to the domain package. Kover only measures the JVM target.
kover {
  reports {
    filters { includes { classes("io.github.kilianvounckx.laxbench.domain.*") } }
    verify { rule { minBound(100) } }
  }
}

tasks.named("check") { dependsOn("koverVerify") }

kotlin {
  listOf(
      iosArm64(),
      iosSimulatorArm64(),
    )
    .forEach { iosTarget ->
      iosTarget.binaries.framework {
        baseName = "Shared"
        isStatic = true
      }
    }

  jvm()

  js { browser() }

  @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

  android {
    namespace = "io.github.kilianvounckx.laxbench.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    androidResources { enable = true }
    withHostTest { isIncludeAndroidResources = true }
    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }
      .configure { instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.androidx.core.ktx)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.compose.uiTooling)
    }
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    webMain.dependencies { implementation(libs.kotlinx.browser) }
  }
}

dependencies { androidRuntimeClasspath(libs.compose.uiTooling) }

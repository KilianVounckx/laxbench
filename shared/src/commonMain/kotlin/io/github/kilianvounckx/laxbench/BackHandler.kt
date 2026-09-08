package io.github.kilianvounckx.laxbench

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

/**
 * Tracks which currently-mounted [BackHandler] should respond to one attempted platform back
 * navigation (see [attemptBackNavigation] and [PlatformBackNavigation]). Handlers form a stack in
 * mount order: the most recently mounted entry -- e.g. an open dialog's Cancel/Back-equivalent
 * action, necessarily mounted after (i.e. on top of) whatever underlying screen registered its own
 * handler earlier in the same composition -- is the one consulted, mirroring the visual stacking of
 * dialogs on top of screens without any of them needing to know about each other. A screen with
 * nothing mounted on top of it and that registers no handler of its own (Setup, Resume) leaves the
 * stack empty, so an attempted back navigation finds nothing and must be allowed to proceed for
 * real.
 */
private object BackHandlerStack {
  private val entries = mutableListOf<BackHandlerEntry>()

  fun push(entry: BackHandlerEntry) {
    entries.add(entry)
  }

  fun remove(entry: BackHandlerEntry) {
    entries.remove(entry)
  }

  fun attemptBack(): Boolean {
    val entry = entries.lastOrNull() ?: return false
    entry.onBack()
    return true
  }
}

private class BackHandlerEntry(var onBack: () -> Unit)

/**
 * Registers [onBack] as this app's response to one attempted platform back navigation for as long
 * as this composable stays in the composition (see [BackHandlerStack]), provided nothing mounted
 * more recently -- e.g. a dialog shown on top of whatever mounted this -- claims the attempt first.
 * There is no dependency in this codebase exposing an equivalent API for Kotlin Multiplatform
 * Wasm/JS, so this is implemented from scratch; [PlatformBackNavigation] is the only
 * platform-specific piece, responsible purely for delivering the platform's back signal to
 * [attemptBackNavigation] and for performing a real "leave" once the user explicitly confirms one
 * via [ConfirmQuitDialog].
 */
@Composable
fun BackHandler(onBack: () -> Unit) {
  val currentOnBack = rememberUpdatedState(onBack)
  val entry = remember { BackHandlerEntry { currentOnBack.value() } }
  DisposableEffect(Unit) {
    BackHandlerStack.push(entry)
    onDispose { BackHandlerStack.remove(entry) }
  }
}

/**
 * Evaluates one attempted platform back navigation (browser back button/gesture -- see
 * [PlatformBackNavigation] -- conceptually a native back button/gesture on future platform targets)
 * against whichever [BackHandler] is currently responsible for it. Returns `true` if some mounted
 * [BackHandler] absorbed the attempt, meaning the platform's own back navigation must not actually
 * happen, or `false` if none did (no [BackHandler] is currently mounted at all, i.e. the user is on
 * Setup or Resume), meaning the platform's real back navigation must be allowed to proceed.
 */
fun attemptBackNavigation(): Boolean = BackHandlerStack.attemptBack()

package io.github.kilianvounckx.laxbench

/**
 * The platform-specific half of one attempted platform back navigation's real "let it happen"
 * outcome (see [BackHandler]/[attemptBackNavigation] for the platform-independent half, and
 * [PlatformBackNavigation] for the process-wide idempotency wrapper). [install] must arrange for
 * [attemptBackNavigation] to be invoked on every platform back-gesture attempt from then on: an
 * attempt it returns `false` for must be allowed to actually leave the app; one it returns `true`
 * for must be suppressed. [leaveApp] performs that same real "let it happen" action on demand, once
 * the user explicitly confirms via [ConfirmQuitDialog]'s "Close" button. Implementations do not
 * need to guard [install] against repeated calls -- [PlatformBackNavigation] already guarantees it
 * is invoked once per process.
 */
interface PlatformBackNavigator {
  fun install()

  fun leaveApp()
}

expect fun createPlatformBackNavigator(): PlatformBackNavigator

/**
 * The single process-wide entry point for this feature. [install] is called once, from a
 * `LaunchedEffect(Unit)` the first time [GameScreen] is composed -- not any earlier, from [App] --
 * because [GameScreen] is the only place any [BackHandler] is ever registered (every dialog shown
 * while playing a game is reachable only from within [GameScreen]); while the user is still on
 * Setup/Resume, with no [BackHandler] registered and [install] never called, an attempted back
 * navigation is left to do whatever the browser would do by default, guaranteeing case 4 behaves
 * exactly as it always did. [install] is idempotent (a guaranteed no-op after the first call), so
 * it is safe to call unconditionally from a `LaunchedEffect(Unit)`.
 */
object PlatformBackNavigation {
  private val navigator: PlatformBackNavigator by lazy { createPlatformBackNavigator() }
  private var isInstalled = false

  fun install() {
    if (isInstalled) return
    isInstalled = true
    navigator.install()
  }

  fun leaveApp() {
    navigator.leaveApp()
  }
}

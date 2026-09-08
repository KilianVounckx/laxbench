package io.github.kilianvounckx.laxbench

import kotlinx.browser.window

/**
 * Uses the standard SPA "back-button interception" technique on the browser History API: [install]
 * pushes one sentinel history entry immediately after the entry that was current when it ran (the
 * "load entry"), so the very next back gesture pops that sentinel (firing `popstate`) instead of
 * immediately leaving the page. Each time `popstate` fires, [attemptBackNavigation] is consulted:
 * if it absorbs the attempt, a fresh sentinel is pushed right back (re-arming the trap for the next
 * back gesture), so the app's history position is always exactly one entry above the load entry
 * whenever nothing is mid-`popstate`; if it doesn't, nothing further is done and the browser's own
 * navigation -- which has already happened by the time `popstate` fires -- is left standing,
 * genuinely leaving the app. Because [PlatformBackNavigation] only ever calls [install] once
 * [GameScreen] exists, no sentinel exists while the user is on Setup/Resume, so a back gesture
 * there is entirely unaffected.
 *
 * [leaveApp] replays that same "let it happen" outcome on demand, once the user explicitly confirms
 * via [ConfirmQuitDialog]. A single step back is not enough: [leaveApp] is only ever invoked while
 * sitting exactly on the re-armed sentinel (since [ConfirmQuitDialog] only appears in direct
 * response to an absorbed back attempt), and popping just the sentinel would land on the load entry
 * -- which shares the very same `Document` as the sentinel, since entries created by [pushState]
 * never cause a document unload when traversed between -- so the app would still be running,
 * indistinguishable from nothing having happened. [leaveApp] instead jumps back two entries at once
 * via `window.history.go(-2)`, past both the sentinel and the load entry, landing on whatever real,
 * different page the tab showed before this app was loaded -- a genuine cross-document navigation
 * that actually leaves the app. That target only exists if the load entry was not the very first
 * entry in this tab's joint session history; [canLeaveViaHistory] records whether it was, captured
 * once via the history length right before [install] pushes its own sentinel. When it wasn't (e.g.
 * opened in a fresh tab or via a bookmark/home-screen icon), [leaveApp] instead navigates the
 * current document to `about:blank`, which unconditionally unloads the app -- unlike relying on
 * `Window.close`, which most browsers silently refuse for a tab/window the page did not itself open
 * via script. That residual inability to also close the tab itself is a genuine browser-security
 * limitation, not something further history bookkeeping can work around.
 */
actual fun createPlatformBackNavigator(): PlatformBackNavigator =
  object : PlatformBackNavigator {
    private var isPerformingConfirmedExit = false
    private var canLeaveViaHistory = false

    @OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
    override fun install() {
      canLeaveViaHistory = window.history.length > 1
      window.history.pushState(null, "")
      window.onpopstate = {
        if (isPerformingConfirmedExit) {
          isPerformingConfirmedExit = false
        } else if (attemptBackNavigation()) {
          window.history.pushState(null, "")
        }
      }
    }

    @OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
    override fun leaveApp() {
      if (canLeaveViaHistory) {
        isPerformingConfirmedExit = true
        window.history.go(-2)
      } else {
        window.location.href = "about:blank"
      }
    }
  }

package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [FaceOff] wins for the rest of the app
 * session, the same in-memory-only, non-persisted way [SaveViewModel], [FoulViewModel], and
 * [TimeOutViewModel] hold each team's history. There is no requirement to display face-off-win
 * counts/history anywhere in the main UI and no undo/correction mechanism for a mistakenly recorded
 * face-off win -- so, like [SaveViewModel], this class exposes no `StateFlow` and no
 * decrement/removal operation: [recordFaceOff] is the only way to change either team's history, and
 * [printDebugSummary] is the only way to observe it.
 *
 * This class reuses [ScoreViewModel.Team] to identify which team won a face-off, rather than
 * defining a fifth, parallel team enum -- this [FaceOffViewModel], [SaveViewModel],
 * [TimeOutViewModel], [FoulViewModel], and [ScoreViewModel] each independently track different
 * per-team data about the exact same two sides of the same game.
 *
 * As with [ScoreViewModel], [FoulViewModel], [TimeOutViewModel], [SaveViewModel], and
 * [TimerViewModel], a fresh instance (obtained the same way, via the Compose Multiplatform
 * `viewModel()` API) always starts both histories empty, and this class does not persist across
 * process death.
 */
class FaceOffViewModel : ViewModel() {

  private val _homeFaceOffs = MutableStateFlow(FaceOffs.empty)
  private val _visitingFaceOffs = MutableStateFlow(FaceOffs.empty)

  /** Records [faceOff] for [team], appending it to that team's face-off-win history. */
  fun recordFaceOff(team: ScoreViewModel.Team, faceOff: FaceOff) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFaceOffs.update { it.recorded(faceOff) }
      ScoreViewModel.Team.VISITING -> _visitingFaceOffs.update { it.recorded(faceOff) }
    }
  }

  /** Prints both teams' recorded face-off-win histories, for debugging. */
  fun printDebugSummary() {
    println("Home face-off wins: ${_homeFaceOffs.value}")
    println("Visiting face-off wins: ${_visitingFaceOffs.value}")
  }
}

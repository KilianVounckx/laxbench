package io.github.kilianvounckx.laxbench

import androidx.lifecycle.ViewModel
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOff
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Privately holds each team's full history of recorded [FaceOff] wins for the rest of the app
 * session, the same in-memory-only, non-persisted way [SaveViewModel], [FoulViewModel], and
 * [TimeOutViewModel] hold each team's history. Each face-off gets a unique id when recorded.
 * [recordFaceOff] is the only way to add a face-off, always appending it as the newest entry with a
 * unique id. [removeFaceOff] removes a specific face-off by id. [printDebugSummary] prints both
 * teams' histories at once for debugging, and [faceOffs] returns a single team's current history as
 * a reactive [StateFlow].
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

  private var nextId = 0L

  /**
   * Records a face-off for [team] at the given [elapsedTime], appending it to that team's
   * face-off-win history.
   */
  fun recordFaceOff(team: ScoreViewModel.Team, elapsedTime: ElapsedTime) {
    val faceOff = FaceOff(id = nextId++, elapsedTime = elapsedTime)
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFaceOffs.update { it.recorded(faceOff) }
      ScoreViewModel.Team.VISITING -> _visitingFaceOffs.update { it.recorded(faceOff) }
    }
  }

  /**
   * Removes the face-off identified by [id] for [team]. This is a no-op if that id is not present.
   */
  fun removeFaceOff(team: ScoreViewModel.Team, id: Long) {
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFaceOffs.update { it.removed(id) }
      ScoreViewModel.Team.VISITING -> _visitingFaceOffs.update { it.removed(id) }
    }
  }

  /** Returns the face-off-win history for the given [team]. */
  fun faceOffs(team: ScoreViewModel.Team): StateFlow<FaceOffs> =
    when (team) {
      ScoreViewModel.Team.HOME -> _homeFaceOffs.asStateFlow()
      ScoreViewModel.Team.VISITING -> _visitingFaceOffs.asStateFlow()
    }

  /** Prints both teams' recorded face-off-win histories, for debugging. */
  fun printDebugSummary() {
    println("Home face-off wins: ${_homeFaceOffs.value}")
    println("Visiting face-off wins: ${_visitingFaceOffs.value}")
  }
}

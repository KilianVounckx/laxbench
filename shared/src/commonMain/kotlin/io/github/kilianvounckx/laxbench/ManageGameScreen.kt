package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.kilianvounckx.laxbench.domain.ElapsedTime
import io.github.kilianvounckx.laxbench.domain.FaceOffs
import io.github.kilianvounckx.laxbench.domain.Foul
import io.github.kilianvounckx.laxbench.domain.FoulSeverity
import io.github.kilianvounckx.laxbench.domain.Fouls
import io.github.kilianvounckx.laxbench.domain.Goal
import io.github.kilianvounckx.laxbench.domain.Goals
import io.github.kilianvounckx.laxbench.domain.PlayerNumber
import io.github.kilianvounckx.laxbench.domain.Saves
import io.github.kilianvounckx.laxbench.domain.TeamColor
import io.github.kilianvounckx.laxbench.domain.TeamInfo
import io.github.kilianvounckx.laxbench.domain.TeamName
import io.github.kilianvounckx.laxbench.domain.TeamsInfo
import io.github.kilianvounckx.laxbench.domain.TimeOuts
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun ManageGameScreen(
  teamsInfo: TeamsInfo,
  elapsedTime: ElapsedTime,
  homeGoals: StateFlow<Goals>,
  visitingGoals: StateFlow<Goals>,
  homeFouls: StateFlow<Fouls>,
  visitingFouls: StateFlow<Fouls>,
  homeFaceOffs: StateFlow<FaceOffs>,
  visitingFaceOffs: StateFlow<FaceOffs>,
  homeTimeOuts: StateFlow<TimeOuts>,
  visitingTimeOuts: StateFlow<TimeOuts>,
  homeSaves: StateFlow<Saves>,
  visitingSaves: StateFlow<Saves>,
  onUpdateTeam: (ScoreViewModel.Team, TeamInfo) -> Unit,
  onAddGoal: (ScoreViewModel.Team, PlayerNumber, PlayerNumber?, ElapsedTime) -> Unit,
  onUpdateGoal: (ScoreViewModel.Team, Goal) -> Unit,
  onDeleteGoal: (ScoreViewModel.Team, Long) -> Unit,
  onAddFoul: (ScoreViewModel.Team, PlayerNumber, FoulSeverity, ElapsedTime) -> Unit,
  onUpdateFoul: (ScoreViewModel.Team, Foul, Foul) -> Unit,
  onDeleteFoul: (ScoreViewModel.Team, Foul) -> Unit,
  onAddFaceOff: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteFaceOff: (ScoreViewModel.Team, Long) -> Unit,
  onAddTimeOut: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteTimeOut: (ScoreViewModel.Team, Long) -> Unit,
  onAddSave: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteSave: (ScoreViewModel.Team, Long) -> Unit,
  onBack: () -> Unit,
) {
  val homeGoalsValue by homeGoals.collectAsStateWithLifecycle()
  val visitingGoalsValue by visitingGoals.collectAsStateWithLifecycle()
  val homeFoulsValue by homeFouls.collectAsStateWithLifecycle()
  val visitingFoulsValue by visitingFouls.collectAsStateWithLifecycle()
  val homeFaceOffsValue by homeFaceOffs.collectAsStateWithLifecycle()
  val visitingFaceOffsValue by visitingFaceOffs.collectAsStateWithLifecycle()
  val homeTimeOutsValue by homeTimeOuts.collectAsStateWithLifecycle()
  val visitingTimeOutsValue by visitingTimeOuts.collectAsStateWithLifecycle()
  val homeSavesValue by homeSaves.collectAsStateWithLifecycle()
  val visitingSavesValue by visitingSaves.collectAsStateWithLifecycle()

  Column(Modifier.safeContentPadding().fillMaxSize()) {
    Button(onClick = onBack) { Text("Back") }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Manage game", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(16.dp))
    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
      CollapsibleSection("Teams") { TeamsSection(teamsInfo, onUpdateTeam) }
      CollapsibleSection("Goals") {
        GoalsSection(
          homeGoals = homeGoalsValue,
          visitingGoals = visitingGoalsValue,
          teamsInfo = teamsInfo,
          elapsedTime = elapsedTime,
          onAddGoal = onAddGoal,
          onUpdateGoal = onUpdateGoal,
          onDeleteGoal = onDeleteGoal,
        )
      }
      CollapsibleSection("Fouls") {
        FoulsSection(
          homeFouls = homeFoulsValue,
          visitingFouls = visitingFoulsValue,
          teamsInfo = teamsInfo,
          elapsedTime = elapsedTime,
          onAddFoul = onAddFoul,
          onUpdateFoul = onUpdateFoul,
          onDeleteFoul = onDeleteFoul,
        )
      }
      CollapsibleSection("Face-offs") {
        FaceOffsSection(
          homeFaceOffs = homeFaceOffsValue,
          visitingFaceOffs = visitingFaceOffsValue,
          teamsInfo = teamsInfo,
          currentElapsedTime = elapsedTime,
          onAddFaceOff = onAddFaceOff,
          onDeleteFaceOff = onDeleteFaceOff,
        )
      }
      CollapsibleSection("Time-outs") {
        TimeOutsSection(
          homeTimeOuts = homeTimeOutsValue,
          visitingTimeOuts = visitingTimeOutsValue,
          teamsInfo = teamsInfo,
          currentElapsedTime = elapsedTime,
          onAddTimeOut = onAddTimeOut,
          onDeleteTimeOut = onDeleteTimeOut,
        )
      }
      CollapsibleSection("Saves") {
        SavesSection(
          homeSaves = homeSavesValue,
          visitingSaves = visitingSavesValue,
          teamsInfo = teamsInfo,
          currentElapsedTime = elapsedTime,
          onAddSave = onAddSave,
          onDeleteSave = onDeleteSave,
        )
      }
    }
  }
}

@Composable
private fun CollapsibleSection(
  title: String,
  content: @Composable () -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  SelectableOptionButton(selected = expanded, label = title, onClick = { expanded = !expanded })
  if (expanded) {
    content()
  }
}

@Composable
private fun TeamsSection(
  teamsInfo: TeamsInfo,
  onUpdate: (ScoreViewModel.Team, TeamInfo) -> Unit,
) {
  TeamEditForm(
    label = "Home team",
    info = teamsInfo.home,
    onSave = { onUpdate(ScoreViewModel.Team.HOME, it) },
  )
  Spacer(modifier = Modifier.height(16.dp))
  TeamEditForm(
    label = "Visiting team",
    info = teamsInfo.visiting,
    onSave = { onUpdate(ScoreViewModel.Team.VISITING, it) },
  )
}

@Composable
private fun TeamEditForm(
  label: String,
  info: TeamInfo,
  onSave: (TeamInfo) -> Unit,
) {
  var nameText by remember { mutableStateOf(info.name.value) }
  var colorText by remember { mutableStateOf(info.color.value) }

  val name = TeamName.parse(nameText)
  val color = TeamColor.parse(colorText)

  Text(label, style = MaterialTheme.typography.bodyMedium)
  OutlinedTextField(
    value = nameText,
    onValueChange = { nameText = it },
    label = { Text("Team name") },
    singleLine = true,
    isError = nameText.isNotBlank() && name == null,
    supportingText = {
      if (nameText.isNotBlank() && name == null) {
        Text("Enter a non-blank team name")
      }
    },
  )
  Spacer(modifier = Modifier.height(8.dp))
  OutlinedTextField(
    value = colorText,
    onValueChange = { colorText = it },
    label = { Text("Team color") },
    singleLine = true,
    isError = colorText.isNotBlank() && color == null,
    supportingText = {
      if (colorText.isNotBlank() && color == null) {
        Text("Enter a valid team color")
      }
    },
  )
  Spacer(modifier = Modifier.height(8.dp))
  Button(
    onClick = { if (name != null && color != null) onSave(TeamInfo(name, color)) },
    enabled = name != null && color != null,
  ) {
    Text("Save")
  }
}

@Composable
private fun GoalsSection(
  homeGoals: Goals,
  visitingGoals: Goals,
  teamsInfo: TeamsInfo,
  elapsedTime: ElapsedTime,
  onAddGoal: (ScoreViewModel.Team, PlayerNumber, PlayerNumber?, ElapsedTime) -> Unit,
  onUpdateGoal: (ScoreViewModel.Team, Goal) -> Unit,
  onDeleteGoal: (ScoreViewModel.Team, Long) -> Unit,
) {
  GoalsTeamList(
    team = ScoreViewModel.Team.HOME,
    teamLabel = teamsInfo.label(ScoreViewModel.Team.HOME),
    goals = homeGoals.all,
    elapsedTime = elapsedTime,
    onAddGoal = onAddGoal,
    onUpdateGoal = onUpdateGoal,
    onDeleteGoal = onDeleteGoal,
  )
  Spacer(modifier = Modifier.height(16.dp))
  GoalsTeamList(
    team = ScoreViewModel.Team.VISITING,
    teamLabel = teamsInfo.label(ScoreViewModel.Team.VISITING),
    goals = visitingGoals.all,
    elapsedTime = elapsedTime,
    onAddGoal = onAddGoal,
    onUpdateGoal = onUpdateGoal,
    onDeleteGoal = onDeleteGoal,
  )
}

@Composable
private fun GoalsTeamList(
  team: ScoreViewModel.Team,
  teamLabel: String,
  goals: List<Goal>,
  elapsedTime: ElapsedTime,
  onAddGoal: (ScoreViewModel.Team, PlayerNumber, PlayerNumber?, ElapsedTime) -> Unit,
  onUpdateGoal: (ScoreViewModel.Team, Goal) -> Unit,
  onDeleteGoal: (ScoreViewModel.Team, Long) -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }
  var editingGoal by remember { mutableStateOf<Goal?>(null) }

  Text(teamLabel, style = MaterialTheme.typography.bodyMedium)
  for (goal in goals) {
    val assistText = goal.assist?.let { " (assist ${it.number})" } ?: ""
    val text = "Goal by ${goal.scorer.number}$assistText at ${goal.elapsedTime.format()}"
    Column {
      Text(text, style = MaterialTheme.typography.bodySmall)
      TextButton(onClick = { editingGoal = goal }) { Text("Edit") }
      TextButton(onClick = { onDeleteGoal(team, goal.id) }) { Text("Delete") }
    }
  }
  Button(onClick = { showDialog = true }) { Text("Add goal") }

  if (showDialog) {
    GoalEntryDialog(
      title = "Add goal",
      initialScorerText = "",
      initialAssistText = "",
      initialElapsedTime = elapsedTime,
      onConfirm = { scorer, assist, time ->
        onAddGoal(team, scorer, assist, time)
        showDialog = false
      },
      onDismiss = { showDialog = false },
    )
  }

  editingGoal?.let { goal ->
    GoalEntryDialog(
      title = "Edit goal",
      initialScorerText = goal.scorer.number.toString(),
      initialAssistText = goal.assist?.number?.toString() ?: "",
      initialElapsedTime = goal.elapsedTime,
      onConfirm = { scorer, assist, time ->
        onUpdateGoal(team, Goal(id = goal.id, scorer = scorer, assist = assist, elapsedTime = time))
        editingGoal = null
      },
      onDismiss = { editingGoal = null },
    )
  }
}

@Composable
private fun FoulsSection(
  homeFouls: Fouls,
  visitingFouls: Fouls,
  teamsInfo: TeamsInfo,
  elapsedTime: ElapsedTime,
  onAddFoul: (ScoreViewModel.Team, PlayerNumber, FoulSeverity, ElapsedTime) -> Unit,
  onUpdateFoul: (ScoreViewModel.Team, Foul, Foul) -> Unit,
  onDeleteFoul: (ScoreViewModel.Team, Foul) -> Unit,
) {
  FoulsTeamList(
    team = ScoreViewModel.Team.HOME,
    teamLabel = teamsInfo.label(ScoreViewModel.Team.HOME),
    fouls = homeFouls.all,
    elapsedTime = elapsedTime,
    onAddFoul = onAddFoul,
    onUpdateFoul = onUpdateFoul,
    onDeleteFoul = onDeleteFoul,
  )
  Spacer(modifier = Modifier.height(16.dp))
  FoulsTeamList(
    team = ScoreViewModel.Team.VISITING,
    teamLabel = teamsInfo.label(ScoreViewModel.Team.VISITING),
    fouls = visitingFouls.all,
    elapsedTime = elapsedTime,
    onAddFoul = onAddFoul,
    onUpdateFoul = onUpdateFoul,
    onDeleteFoul = onDeleteFoul,
  )
}

@Composable
private fun FoulsTeamList(
  team: ScoreViewModel.Team,
  teamLabel: String,
  fouls: List<Foul>,
  elapsedTime: ElapsedTime,
  onAddFoul: (ScoreViewModel.Team, PlayerNumber, FoulSeverity, ElapsedTime) -> Unit,
  onUpdateFoul: (ScoreViewModel.Team, Foul, Foul) -> Unit,
  onDeleteFoul: (ScoreViewModel.Team, Foul) -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }
  var editingFoul by remember { mutableStateOf<Foul?>(null) }

  Text(teamLabel, style = MaterialTheme.typography.bodyMedium)
  for (foul in fouls) {
    val durationText = foul.severity.durationLabel?.let { " ($it)" } ?: ""
    val text =
      "Foul by ${foul.player.number}: ${foul.severity.typeLabel}$durationText at ${foul.elapsedTime.format()}"
    Column {
      Text(text, style = MaterialTheme.typography.bodySmall)
      TextButton(onClick = { editingFoul = foul }) { Text("Edit") }
      TextButton(onClick = { onDeleteFoul(team, foul) }) { Text("Delete") }
    }
  }
  Button(onClick = { showDialog = true }) { Text("Add foul") }

  if (showDialog) {
    FoulEntryDialog(
      title = "Add foul",
      initialPlayerText = "",
      initialSeverity = null,
      initialElapsedTime = elapsedTime,
      onConfirm = { player, severity, time ->
        onAddFoul(team, player, severity, time)
        showDialog = false
      },
      onDismiss = { showDialog = false },
    )
  }

  editingFoul?.let { foul ->
    FoulEntryDialog(
      title = "Edit foul",
      initialPlayerText = foul.player.number.toString(),
      initialSeverity = foul.severity,
      initialElapsedTime = foul.elapsedTime,
      onConfirm = { player, severity, time ->
        onUpdateFoul(
          team,
          foul,
          Foul(id = foul.id, player = player, severity = severity, elapsedTime = time),
        )
        editingFoul = null
      },
      onDismiss = { editingFoul = null },
    )
  }
}

@Composable
private fun FaceOffsSection(
  homeFaceOffs: FaceOffs,
  visitingFaceOffs: FaceOffs,
  teamsInfo: TeamsInfo,
  currentElapsedTime: ElapsedTime,
  onAddFaceOff: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteFaceOff: (ScoreViewModel.Team, Long) -> Unit,
) {
  ElapsedTimeEntriesTeamList(
    homeTeamLabel = teamsInfo.label(ScoreViewModel.Team.HOME),
    visitingTeamLabel = teamsInfo.label(ScoreViewModel.Team.VISITING),
    homeEntries = homeFaceOffs.all,
    visitingEntries = visitingFaceOffs.all,
    currentElapsedTime = currentElapsedTime,
    addDialogTitle = "Add face-off",
    onAddHome = { onAddFaceOff(ScoreViewModel.Team.HOME, it) },
    onAddVisiting = { onAddFaceOff(ScoreViewModel.Team.VISITING, it) },
    onDeleteHome = { onDeleteFaceOff(ScoreViewModel.Team.HOME, it.id) },
    onDeleteVisiting = { onDeleteFaceOff(ScoreViewModel.Team.VISITING, it.id) },
    elapsedTimeOf = { it.elapsedTime },
  )
}

@Composable
private fun TimeOutsSection(
  homeTimeOuts: TimeOuts,
  visitingTimeOuts: TimeOuts,
  teamsInfo: TeamsInfo,
  currentElapsedTime: ElapsedTime,
  onAddTimeOut: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteTimeOut: (ScoreViewModel.Team, Long) -> Unit,
) {
  ElapsedTimeEntriesTeamList(
    homeTeamLabel = teamsInfo.label(ScoreViewModel.Team.HOME),
    visitingTeamLabel = teamsInfo.label(ScoreViewModel.Team.VISITING),
    homeEntries = homeTimeOuts.all,
    visitingEntries = visitingTimeOuts.all,
    currentElapsedTime = currentElapsedTime,
    addDialogTitle = "Add time-out",
    onAddHome = { onAddTimeOut(ScoreViewModel.Team.HOME, it) },
    onAddVisiting = { onAddTimeOut(ScoreViewModel.Team.VISITING, it) },
    onDeleteHome = { onDeleteTimeOut(ScoreViewModel.Team.HOME, it.id) },
    onDeleteVisiting = { onDeleteTimeOut(ScoreViewModel.Team.VISITING, it.id) },
    elapsedTimeOf = { it.elapsedTime },
  )
}

@Composable
private fun SavesSection(
  homeSaves: Saves,
  visitingSaves: Saves,
  teamsInfo: TeamsInfo,
  currentElapsedTime: ElapsedTime,
  onAddSave: (ScoreViewModel.Team, ElapsedTime) -> Unit,
  onDeleteSave: (ScoreViewModel.Team, Long) -> Unit,
) {
  ElapsedTimeEntriesTeamList(
    homeTeamLabel = teamsInfo.label(ScoreViewModel.Team.HOME),
    visitingTeamLabel = teamsInfo.label(ScoreViewModel.Team.VISITING),
    homeEntries = homeSaves.all,
    visitingEntries = visitingSaves.all,
    currentElapsedTime = currentElapsedTime,
    addDialogTitle = "Add save",
    onAddHome = { onAddSave(ScoreViewModel.Team.HOME, it) },
    onAddVisiting = { onAddSave(ScoreViewModel.Team.VISITING, it) },
    onDeleteHome = { onDeleteSave(ScoreViewModel.Team.HOME, it.id) },
    onDeleteVisiting = { onDeleteSave(ScoreViewModel.Team.VISITING, it.id) },
    elapsedTimeOf = { it.elapsedTime },
  )
}

@Composable
private fun <T> ElapsedTimeEntriesTeamList(
  homeTeamLabel: String,
  visitingTeamLabel: String,
  homeEntries: List<T>,
  visitingEntries: List<T>,
  currentElapsedTime: ElapsedTime,
  addDialogTitle: String,
  onAddHome: (ElapsedTime) -> Unit,
  onAddVisiting: (ElapsedTime) -> Unit,
  onDeleteHome: (T) -> Unit,
  onDeleteVisiting: (T) -> Unit,
  elapsedTimeOf: (T) -> ElapsedTime,
) {
  var showHomeDialog by remember { mutableStateOf(false) }
  var showVisitingDialog by remember { mutableStateOf(false) }

  Text(homeTeamLabel, style = MaterialTheme.typography.bodyMedium)
  for (entry in homeEntries) {
    Column {
      Text(elapsedTimeOf(entry).format(), style = MaterialTheme.typography.bodySmall)
      TextButton(onClick = { onDeleteHome(entry) }) { Text("Delete") }
    }
  }
  Button(onClick = { showHomeDialog = true }) { Text("Add entry") }

  if (showHomeDialog) {
    ElapsedTimeEntryDialog(
      title = addDialogTitle,
      initialElapsedTime = currentElapsedTime,
      onConfirm = { time ->
        onAddHome(time)
        showHomeDialog = false
      },
      onDismiss = { showHomeDialog = false },
    )
  }

  Spacer(modifier = Modifier.height(16.dp))

  Text(visitingTeamLabel, style = MaterialTheme.typography.bodyMedium)
  for (entry in visitingEntries) {
    Column {
      Text(elapsedTimeOf(entry).format(), style = MaterialTheme.typography.bodySmall)
      TextButton(onClick = { onDeleteVisiting(entry) }) { Text("Delete") }
    }
  }
  Button(onClick = { showVisitingDialog = true }) { Text("Add entry") }

  if (showVisitingDialog) {
    ElapsedTimeEntryDialog(
      title = addDialogTitle,
      initialElapsedTime = currentElapsedTime,
      onConfirm = { time ->
        onAddVisiting(time)
        showVisitingDialog = false
      },
      onDismiss = { showVisitingDialog = false },
    )
  }
}

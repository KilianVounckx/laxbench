package io.github.kilianvounckx.laxbench

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
@Preview
fun App() {
  MaterialTheme {
    val timerViewModel: TimerViewModel = viewModel { TimerViewModel() }
    val elapsedTime by timerViewModel.elapsedTime.collectAsStateWithLifecycle()
    val isRunning by timerViewModel.isRunning.collectAsStateWithLifecycle()

    Column(
      modifier = Modifier.safeContentPadding().fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(text = elapsedTime.format(), style = MaterialTheme.typography.displayMedium)
      Spacer(modifier = Modifier.height(16.dp))
      Button(onClick = { timerViewModel.toggle() }) {
        Text(text = if (isRunning) "Stop all clocks" else "Resume game")
      }
    }
  }
}

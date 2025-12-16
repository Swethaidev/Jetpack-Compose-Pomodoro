import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface { PomodoroScreen() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen() {
    // Timer settings
    var focusMinutes by remember { mutableStateOf(25) }
    var breakMinutes by remember { mutableStateOf(5) }

    // Timer state
    var isRunning by remember { mutableStateOf(false) }
    var isFocus by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableStateOf(focusMinutes * 60) }

    // When user changes minutes while not running, reset timer
    LaunchedEffect(focusMinutes, breakMinutes) {
        if (!isRunning) {
            secondsLeft = (if (isFocus) focusMinutes else breakMinutes) * 60
        }
    }

    // Main timer loop
    LaunchedEffect(isRunning, isFocus) {
        while (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        if (isRunning && secondsLeft == 0) {
            // Auto switch mode
            isRunning = false
            isFocus = !isFocus
            secondsLeft = (if (isFocus) focusMinutes else breakMinutes) * 60
        }
    }

    val totalSeconds = (if (isFocus) focusMinutes else breakMinutes) * 60
    val progress = if (totalSeconds == 0) 0f else (secondsLeft.toFloat() / totalSeconds.toFloat())
    val modeLabel = if (isFocus) "Focus" else "Break"
    val timeText = formatTime(secondsLeft)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pomodoro Timer Demo") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ElevatedCard {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(modeLabel, style = MaterialTheme.typography.headlineSmall)
                    LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                    Text(timeText, style = MaterialTheme.typography.displaySmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { isRunning = true },
                            enabled = !isRunning
                        ) { Text("Start") }

                        OutlinedButton(
                            onClick = { isRunning = false },
                            enabled = isRunning
                        ) { Text("Pause") }

                        TextButton(
                            onClick = {
                                isRunning = false
                                secondsLeft = (if (isFocus) focusMinutes else breakMinutes) * 60
                            }
                        ) { Text("Reset") }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = {
                            isRunning = false
                            isFocus = true
                            secondsLeft = focusMinutes * 60
                        }) { Text("Focus") }

                        OutlinedButton(onClick = {
                            isRunning = false
                            isFocus = false
                            secondsLeft = breakMinutes * 60
                        }) { Text("Break") }
                    }
                }
            }

            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Settings", style = MaterialTheme.typography.titleMedium)

                    SettingRow(
                        label = "Focus Minutes",
                        value = focusMinutes,
                        onMinus = { focusMinutes = (focusMinutes - 1).coerceAtLeast(1) },
                        onPlus = { focusMinutes = (focusMinutes + 1).coerceAtMost(90) }
                    )

                    SettingRow(
                        label = "Break Minutes",
                        value = breakMinutes,
                        onMinus = { breakMinutes = (breakMinutes - 1).coerceAtLeast(1) },
                        onPlus = { breakMinutes = (breakMinutes + 1).coerceAtMost(30) }
                    )

                    Text(
                        "Tip: Start the timer. When it reaches 0, it automatically switches between Focus and Break.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onMinus, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("-") }
        Text("  $value  ")
        OutlinedButton(onClick = onPlus, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("+") }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

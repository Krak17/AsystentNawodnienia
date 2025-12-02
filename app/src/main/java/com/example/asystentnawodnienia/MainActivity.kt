package com.example.asystentnawodnienia

import android.content.Intent
import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.asystentnawodnienia.data.WaterIntake
import com.example.asystentnawodnienia.services.ReminderWorker
import com.example.asystentnawodnienia.services.SensorService
import com.example.asystentnawodnienia.ui.WaterViewModel
import com.example.asystentnawodnienia.ui.WaterViewModelFactory
import com.example.asystentnawodnienia.ui.theme.AsystentNawodnieniaTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: WaterViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            scheduleReminderWork()
        } else {
            // Handle permission denial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = WaterViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)

        startService(Intent(this, SensorService::class.java))
        listenForShakes()
        askForNotificationPermission()

        setContent {
            AsystentNawodnieniaTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    private fun listenForShakes() {
        lifecycleScope.launch {
            SensorService.shakeFlow.collectLatest {
                val amountToAdd = viewModel.sliderValue.value
                viewModel.addWater(amountToAdd)
            }
        }
    }

    private fun askForNotificationPermission() {
        // Implementation details omitted for brevity
    }

    private fun scheduleReminderWork() {
        // Implementation details omitted for brevity
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WaterViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asystent Nawodnienia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = { 
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        WaterTrackerContent(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun WaterTrackerContent(viewModel: WaterViewModel, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TodayIntakeSection(viewModel)
            AddWaterSection(viewModel)
            WeeklySummaryChart()
            val history by viewModel.history
            val loading by viewModel.loading
            HistorySectionWithLoading(history = history, loading = loading)
        }
    }
}

@Composable
fun WeeklySummaryChart() {
    val weeklyData = listOf(1500, 2000, 1800, 2500, 2200, 3000, 1200)
    val days = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Podsumowanie tygodnia", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        WeeklyWaterChart(weeklyIntake = weeklyData, days = days)
    }
}

@Composable
fun WeeklyWaterChart(
    weeklyIntake: List<Int>,
    days: List<String>,
    modifier: Modifier = Modifier
) {
    val maxIntake = weeklyIntake.maxOrNull() ?: 3000
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) { 
            val barWidth = size.width / (weeklyIntake.size * 2)
            val spaceBetweenBars = barWidth

            weeklyIntake.forEachIndexed { index, intake ->
                val barHeight = (intake.toFloat() / maxIntake.toFloat()) * size.height
                val xOffset = index * (barWidth + spaceBetweenBars) + (spaceBetweenBars / 2)

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x = xOffset, y = size.height - barHeight),
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = CornerRadius(x = 10f, y = 10f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            days.forEach { day -> Text(text = day, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun TodayIntakeSection(viewModel: WaterViewModel) {
    val totalToday by viewModel.totalToday
    val dailyGoal = 3000f
    val progress = (totalToday / dailyGoal).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Dzisiejsze spożycie", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedIntakeText(totalIntake = totalToday)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp)
        )
    }
}

@Composable
fun AnimatedIntakeText(totalIntake: Int, modifier: Modifier = Modifier) {
    val animatedIntake by animateIntAsState(
        targetValue = totalIntake,
        label = "intakeAnimation"
    )
    Text(
        text = "$animatedIntake ml",
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun AddWaterSection(viewModel: WaterViewModel) {
    val sliderValue by viewModel.sliderValue
    val steps = 18

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Wybierz ilość: $sliderValue ml",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = sliderValue.toFloat(),
            onValueChange = { viewModel.updateSliderValue(it.toInt()) },
            valueRange = 50f..1000f,
            steps = steps
        )
        Button(
            onClick = { viewModel.addWater(sliderValue) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dodaj wodę", fontSize = 18.sp)
        }
    }
}

@Composable
fun HistorySectionWithLoading(
    history: List<WaterIntake>,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text("Historia", style = MaterialTheme.typography.titleLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                loading -> CircularProgressIndicator()
                history.isEmpty() -> Text(
                    text = "Brak danych",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { intake ->
                            HistoryItem(intake)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(intake: WaterIntake) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = intake.date, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${intake.amountMl} ml", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun MainScreenLightPreview() {
    AsystentNawodnieniaTheme(darkTheme = false) {
        Surface {
            WeeklySummaryChart()
        }
    }
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenDarkPreview() {
    AsystentNawodnieniaTheme(darkTheme = true) {
        Surface {
            WeeklySummaryChart()
        }
    }
}

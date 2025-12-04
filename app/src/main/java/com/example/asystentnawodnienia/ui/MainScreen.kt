package com.example.asystentnawodnienia.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.asystentnawodnienia.data.SettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: WaterViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asystent Nawodnienia") },
                actions = {
                    IconButton(onClick = { navController.navigate("week_summary") }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Podsumowanie tygodnia")
                    }
                    IconButton(onClick = { navController.navigate("today_history") }) {
                        Icon(Icons.Default.History, contentDescription = "Historia dzienna")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
    ) { paddingValues ->
        MainContent(
            modifier = Modifier.padding(paddingValues),
            viewModel = viewModel
        )
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier, viewModel: WaterViewModel) {
    // Pobieramy instancję SettingsManager, aby odczytać cel dzienny
    val settingsManager = SettingsManager(LocalContext.current)
    val dailyGoal by settingsManager.dailyGoalFlow.collectAsState(initial = 3000)

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WaterProgressIndicator(totalToday = viewModel.totalToday, dailyGoal = dailyGoal)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Wybierz ilość: ${viewModel.sliderValue} ml", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = viewModel.sliderValue.toFloat(),
            onValueChange = { viewModel.updateSliderValue(it.roundToInt()) },
            valueRange = 50f..500f,
            steps = 8
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.addWater(viewModel.sliderValue) }) {
                Text("Dodaj")
            }
            Button(onClick = { viewModel.removeWater(viewModel.sliderValue) }) {
                Text("Usuń")
            }
        }
        Button(onClick = { viewModel.resetToday() }) {
            Text("Reset dnia")
        }
    }
}

@Composable
fun WaterProgressIndicator(totalToday: Int, dailyGoal: Int) {
    val progress = (totalToday.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progressAnimation"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalToday ml",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "/ $dailyGoal ml",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

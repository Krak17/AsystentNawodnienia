package com.example.asystentnawodnienia.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController, 
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context = androidx.compose.ui.platform.LocalContext.current))
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        SettingsContent(
            modifier = Modifier.padding(paddingValues),
            settingsViewModel = settingsViewModel
        )
    }
}

@Composable
fun SettingsContent(modifier: Modifier = Modifier, settingsViewModel: SettingsViewModel) {
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val shakeEnabled by settingsViewModel.shakeEnabled.collectAsState()
    val notificationFrequency by settingsViewModel.notificationFrequency.collectAsState()
    val dailyGoal by settingsViewModel.dailyGoal.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ustawienia ogólne", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Nowa sekcja dla dziennego celu
        DailyGoalSetting(goal = dailyGoal) {
            settingsViewModel.setDailyGoal(it)
        }
        
        Divider()

        NotificationsSettingsSection(
            notificationsEnabled = notificationsEnabled,
            frequency = notificationFrequency,
            onEnabledChange = { settingsViewModel.setNotificationsEnabled(it) },
            onFrequencyChange = { settingsViewModel.setNotificationFrequency(it) }
        )

        Divider()

        ShakeSetting(shakeEnabled = shakeEnabled) {
            settingsViewModel.setShakeEnabled(it)
        }
    }
}

@Composable
fun DailyGoalSetting(goal: Int, onGoalChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Dzienny cel: $goal ml", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = goal.toFloat(),
            onValueChange = { onGoalChange(it.roundToInt()) },
            valueRange = 1500f..5000f,
            steps = 34 // ((5000-1500)/100) - 1
        )
    }
}

@Composable
fun NotificationsSettingsSection(
    notificationsEnabled: Boolean,
    frequency: Int,
    onEnabledChange: (Boolean) -> Unit,
    onFrequencyChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Powiadomienia", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = notificationsEnabled, onCheckedChange = onEnabledChange)
        }

        AnimatedVisibility(visible = notificationsEnabled) {
            NotificationFrequencySetting(frequency = frequency, onFrequencyChange = onFrequencyChange)
        }
    }
}

@Composable
fun NotificationFrequencySetting(frequency: Int, onFrequencyChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text("Częstotliwość powiadomień: $frequency godz.", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = frequency.toFloat(),
            onValueChange = { onFrequencyChange(it.roundToInt()) },
            valueRange = 1f..8f, 
            steps = 6 
        )
    }
}

@Composable
fun ShakeSetting(shakeEnabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Wykrywanie potrząśnięć", style = MaterialTheme.typography.bodyLarge)
        Switch(checked = shakeEnabled, onCheckedChange = onCheckedChange)
    }
}

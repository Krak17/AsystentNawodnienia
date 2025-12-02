package com.example.asystentnawodnienia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    // Pobieramy ViewModel, używając fabryki
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(context = androidx.compose.ui.platform.LocalContext.current))
) {
    // Obserwujemy stany z ViewModelu
    val shakeEnabled by settingsViewModel.shakeEnabled.collectAsState()
    val notificationFrequency by settingsViewModel.notificationFrequency.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ustawienia", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Ustawienie: Wykrywanie potrząśnięć
        ShakeSetting(shakeEnabled = shakeEnabled) {
            settingsViewModel.setShakeEnabled(it)
        }

        // Ustawienie: Częstotliwość powiadomień
        NotificationSetting(frequency = notificationFrequency) {
            settingsViewModel.setNotificationFrequency(it)
        }
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

@Composable
fun NotificationSetting(frequency: Int, onFrequencyChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Częstotliwość powiadomień: $frequency godz.", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = frequency.toFloat(),
            onValueChange = { onFrequencyChange(it.toInt()) },
            valueRange = 1f..8f, // np. od 1 do 8 godzin
            steps = 6 // (8-1) - 1 = 6 kroków
        )
    }
}

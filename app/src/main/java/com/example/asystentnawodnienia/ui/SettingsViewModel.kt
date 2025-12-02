package com.example.asystentnawodnienia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    // Używamy stateIn, aby przekształcić Flow z DataStore w StateFlow,
    // który można łatwo obserwować w UI Compose. Jest to wydajne i bezpieczne.
    val shakeEnabled = settingsManager.shakeDetectionEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true // Wartość początkowa
    )

    val notificationFrequency = settingsManager.notificationFrequencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2 // Wartość początkowa
    )

    // Funkcja do aktualizacji ustawienia detekcji potrząśnięć.
    fun setShakeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setShakeDetectionEnabled(isEnabled)
        }
    }

    // Funkcja do aktualizacji ustawienia częstotliwości powiadomień.
    fun setNotificationFrequency(hours: Int) {
        viewModelScope.launch {
            settingsManager.setNotificationFrequency(hours)
        }
    }
}

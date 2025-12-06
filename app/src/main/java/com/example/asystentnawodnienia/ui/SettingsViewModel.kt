package com.example.asystentnawodnienia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Trzymamy ustawienia aplikacji w jednym miejscu dla UI
class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    // Pobieramy informację czy powiadomienia są włączone
    val notificationsEnabled = settingsManager.notificationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    // Pobieramy informację czy wykrywanie potrząśnięć jest włączone
    val shakeEnabled = settingsManager.shakeDetectionEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )
    // Pobieramy co ile godzin mają przychodzić powiadomienia
    val notificationFrequency = settingsManager.notificationFrequencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2
    )

    // Pobieramy dzienny cel wody
    val dailyGoal = settingsManager.dailyGoalFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 3000
    )
    // Zapisujemy ustawienie powiadomień
    fun setNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(isEnabled)
        }
    }
    // Zapisujemy ustawienie wykrywania potrząśnięć
    fun setShakeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setShakeDetectionEnabled(isEnabled)
        }
    }

    // Zapisujemy częstotliwość powiadomień
    fun setNotificationFrequency(hours: Int) {
        viewModelScope.launch {
            settingsManager.setNotificationFrequency(hours)
        }
    }


    // Zapisujemy dzienny cel wody
    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsManager.setDailyGoal(goal)
        }
    }
}
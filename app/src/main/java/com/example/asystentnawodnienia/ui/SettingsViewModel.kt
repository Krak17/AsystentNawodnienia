package com.example.asystentnawodnienia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {

    val notificationsEnabled = settingsManager.notificationsEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val shakeEnabled = settingsManager.shakeDetectionEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val notificationFrequency = settingsManager.notificationFrequencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2
    )

    // Nowy stan dla dziennego celu
    val dailyGoal = settingsManager.dailyGoalFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 3000
    )

    fun setNotificationsEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(isEnabled)
        }
    }

    fun setShakeEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setShakeDetectionEnabled(isEnabled)
        }
    }

    fun setNotificationFrequency(hours: Int) {
        viewModelScope.launch {
            settingsManager.setNotificationFrequency(hours)
        }
    }

    // Nowa funkcja do aktualizacji dziennego celu
    fun setDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsManager.setDailyGoal(goal)
        }
    }
}

package com.example.asystentnawodnienia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SHAKE_DETECTION_ENABLED = booleanPreferencesKey("shake_detection_enabled")
        val NOTIFICATION_FREQUENCY_HOURS = intPreferencesKey("notification_frequency_hours")
        // Nowy klucz dla dziennego celu
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
    }

    // Flow dla dziennego celu
    val dailyGoalFlow: Flow<Int> = dataStore.data.map {
        it[DAILY_GOAL_ML] ?: 3000 // Domyślnie 3000 ml
    }

    // Funkcja do zapisu dziennego celu
    suspend fun setDailyGoal(goalInMl: Int) {
        dataStore.edit {
            it[DAILY_GOAL_ML] = goalInMl
        }
    }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map {
        it[NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[NOTIFICATIONS_ENABLED] = isEnabled
        }
    }

    val shakeDetectionEnabledFlow: Flow<Boolean> = dataStore.data.map {
        it[SHAKE_DETECTION_ENABLED] ?: true
    }

    suspend fun setShakeDetectionEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[SHAKE_DETECTION_ENABLED] = isEnabled
        }
    }

    val notificationFrequencyFlow: Flow<Int> = dataStore.data.map {
        it[NOTIFICATION_FREQUENCY_HOURS] ?: 2
    }

    suspend fun setNotificationFrequency(hours: Int) {
        dataStore.edit {
            it[NOTIFICATION_FREQUENCY_HOURS] = hours
        }
    }
}

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

// Rozszerzenie tworzące instancję DataStore dla całej aplikacji.
// Nazwa "settings" to nazwa pliku, w którym będą przechowywane dane.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Klasa zarządzająca zapisem i odczytem ustawień aplikacji z DataStore.
 */
class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    // Klucze do przechowywania poszczególnych ustawień.
    companion object {
        val SHAKE_DETECTION_ENABLED = booleanPreferencesKey("shake_detection_enabled")
        val NOTIFICATION_FREQUENCY_HOURS = intPreferencesKey("notification_frequency_hours")
    }

    // Flow, który emituje aktualną wartość ustawienia dla detekcji potrząśnięć.
    // Dzięki `map` UI będzie automatycznie informowane o każdej zmianie.
    val shakeDetectionEnabledFlow: Flow<Boolean> = dataStore.data.map {
        // Domyślnie włączone, jeśli nic nie jest jeszcze zapisane.
        it[SHAKE_DETECTION_ENABLED] ?: true
    }

    // Funkcja do zapisu ustawienia detekcji potrząśnięć.
    suspend fun setShakeDetectionEnabled(isEnabled: Boolean) {
        dataStore.edit {
            it[SHAKE_DETECTION_ENABLED] = isEnabled
        }
    }

    // Flow dla częstotliwości powiadomień.
    val notificationFrequencyFlow: Flow<Int> = dataStore.data.map {
        // Domyślnie 2 godziny, jeśli nic nie jest zapisane.
        it[NOTIFICATION_FREQUENCY_HOURS] ?: 2
    }

    // Funkcja do zapisu częstotliwości powiadomień.
    suspend fun setNotificationFrequency(hours: Int) {
        dataStore.edit {
            it[NOTIFICATION_FREQUENCY_HOURS] = hours
        }
    }
}

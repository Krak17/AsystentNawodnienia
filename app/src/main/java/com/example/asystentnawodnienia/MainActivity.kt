package com.example.asystentnawodnienia

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.asystentnawodnienia.data.SettingsManager
import com.example.asystentnawodnienia.services.ReminderWorker
import com.example.asystentnawodnienia.services.SensorService
import com.example.asystentnawodnienia.ui.AppNavigation
import com.example.asystentnawodnienia.ui.WaterViewModel
import com.example.asystentnawodnienia.ui.WaterViewModelFactory
import com.example.asystentnawodnienia.ui.theme.AsystentNawodnieniaTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    // Trzymamy główny ViewModel od wody
    private lateinit var viewModel: WaterViewModel
    // Trzymamy manager ustawień
    private lateinit var settingsManager: SettingsManager
    // Prosimo użytkownika o zgodę na powiadomienia
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Po uzyskaniu zgody, logika w `observeAndReactToSettings` sama zaplanuje zadanie.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Włączamy layout pod krawędzie ekranu
        enableEdgeToEdge()

        // Tworzymy ViewModel z fabryki
        val factory = WaterViewModelFactory(applicationContext)

        // Tworzymy manager ustawień
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)

        // Uruchamiamy nasłuch potrząśnięć
        settingsManager = SettingsManager(applicationContext)

        // Obserwujemy ustawienia i sterujemy usługą oraz powiadomieniami
        listenForShakes()
        observeAndReactToSettings() // Centralne miejsce zarządzania całą logiką tła.

        // Ustawiamy nawigację i UI aplikacji
        setContent {
            AsystentNawodnieniaTheme {
                AppNavigation(waterViewModel = viewModel)
            }
        }
    }

    // Reagujemy na potrząśnięcie dodaniem wody
    private fun listenForShakes() {
        lifecycleScope.launch {
            SensorService.shakeFlow.collectLatest {
                // Bierzemy wartość z suwaka
                val amountToAdd = viewModel.sliderValue
                // Dodajemy wodę
                viewModel.addWater(amountToAdd)
                // Wywołujemy krótką wibrację
                triggerVibration()
            }
        }
    }

    // Wywołujemy krótką wibrację po potrząśnięciu

    private fun triggerVibration() {
        // Pobieramy odpowiedni obiekt wibracji zależnie od wersji Androida
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        // Uruchamiamy wibrację w kompatybilny sposób
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    // Łączymy ustawienia i reagujemy na ich zmiany
    private fun observeAndReactToSettings() {
        lifecycleScope.launch {
            // Używamy `repeatOnLifecycle`, aby obserwacja była aktywna tylko gdy aplikacja jest widoczna.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    settingsManager.shakeDetectionEnabledFlow,
                    settingsManager.notificationsEnabledFlow,
                    settingsManager.notificationFrequencyFlow
                ) { shake, notifications, frequency ->
                    Triple(shake, notifications, frequency)
                }.collectLatest { (shakeEnabled, notificationsEnabled, frequency) ->

                    // Włączamy lub wyłączamy usługę potrząśnięć
                    if (shakeEnabled && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        startService(Intent(this@MainActivity, SensorService::class.java))
                    } else {
                        stopService(Intent(this@MainActivity, SensorService::class.java))
                    }

                    // Zarządzanie powiadomieniami.
                    if (notificationsEnabled) {
                        askForNotificationPermissionAndSchedule(frequency)
                    } else {
                        cancelReminderWork()
                    }
                }
            }
        }
    }

    // Sprawdzamy zgodę na powiadomienia i ustawiamy harmonogram
    private fun askForNotificationPermissionAndSchedule(frequency: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                // Mamy zgodę, więc planujemy przypomnienia
                PackageManager.PERMISSION_GRANTED -> scheduleReminderWork(frequency)
                // Nie mamy zgody, więc prosimy użytkownika
                else -> requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleReminderWork(frequency)
        }
    }
    // Planujemy cykliczne przypomnienia co X godzin
    private fun scheduleReminderWork(frequency: Int) {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            frequency.toLong(), TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }
    // Wyłączamy zaplanowane przypomnienia
    private fun cancelReminderWork() {
        WorkManager.getInstance(this).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AsystentNawodnieniaTheme {
        // Podgląd motywu
    }
}
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

    private lateinit var viewModel: WaterViewModel
    private lateinit var settingsManager: SettingsManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Po uzyskaniu zgody, logika w `observeAndReactToSettings` sama zaplanuje zadanie.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = WaterViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)
        settingsManager = SettingsManager(applicationContext)

        listenForShakes()
        observeAndReactToSettings() // Centralne miejsce zarządzania całą logiką tła.

        setContent {
            AsystentNawodnieniaTheme {
                AppNavigation(waterViewModel = viewModel)
            }
        }
    }

    // Nasłuchuje na zdarzenia potrząśnięcia i reaguje na nie.
    private fun listenForShakes() {
        lifecycleScope.launch {
            SensorService.shakeFlow.collectLatest {
                val amountToAdd = viewModel.sliderValue
                viewModel.addWater(amountToAdd)
                triggerVibration() // PRZYWRÓCONO: Wywołanie wibracji.
            }
        }
    }

    // PRZYWRÓCONO: Funkcja wywołująca krótką wibrację.
    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    // Centralna funkcja, która łączy wszystkie ustawienia i reaguje na ich zmiany.
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
                    
                    // Zarządzanie serwisem wstrząsów.
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

    private fun askForNotificationPermissionAndSchedule(frequency: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> scheduleReminderWork(frequency)
                else -> requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleReminderWork(frequency)
        }
    }

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

    private fun cancelReminderWork() {
        WorkManager.getInstance(this).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AsystentNawodnieniaTheme {
        // Preview
    }
}

package com.example.asystentnawodnienia

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: WaterViewModel
    private lateinit var settingsManager: SettingsManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            observeSettings()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = WaterViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)
        settingsManager = SettingsManager(applicationContext)

        listenForShakes()
        observeSettings()

        setContent {
            AsystentNawodnieniaTheme {
                AppNavigation(waterViewModel = viewModel)
            }
        }
    }

    private fun listenForShakes() {
        lifecycleScope.launch {
            SensorService.shakeFlow.collectLatest {
                // Usunięto .value - odwołujemy się bezpośrednio do właściwości
                val amountToAdd = viewModel.sliderValue
                viewModel.addWater(amountToAdd)
            }
        }
    }

    private fun observeSettings() {
        lifecycleScope.launch {
            settingsManager.notificationsEnabledFlow.collectLatest {
                if (it) {
                    askForNotificationPermission()
                } else {
                    cancelReminderWork()
                }
            }
        }
        lifecycleScope.launch {
            settingsManager.shakeDetectionEnabledFlow.collectLatest {
                if (it) {
                    startService(Intent(this@MainActivity, SensorService::class.java))
                } else {
                    stopService(Intent(this@MainActivity, SensorService::class.java))
                }
            }
        }
        lifecycleScope.launch {
            settingsManager.notificationFrequencyFlow.collectLatest {
                scheduleReminderWork()
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> scheduleReminderWork()
                else -> requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleReminderWork()
        }
    }

    private fun scheduleReminderWork() {
        lifecycleScope.launch {
            // Funkcja .first() jest teraz poprawnie rozpoznawana dzięki importowi
            val frequency = settingsManager.notificationFrequencyFlow.first()
            val isEnabled = settingsManager.notificationsEnabledFlow.first()
            if (!isEnabled) return@launch

            val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                frequency.toLong(), TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this@MainActivity).enqueueUniquePeriodicWork(
                ReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderWorkRequest
            )
        }
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

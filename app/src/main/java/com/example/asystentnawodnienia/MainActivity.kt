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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
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
            scheduleReminderWork()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = WaterViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(WaterViewModel::class.java)
        settingsManager = SettingsManager(applicationContext)

        listenForShakes()

        setContent {
            AsystentNawodnieniaTheme {
                AppNavigation(waterViewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Uruchom serwis, gdy aplikacja jest na pierwszym planie
        startService(Intent(this, SensorService::class.java))
    }

    override fun onPause() {
        super.onPause()
        // Zatrzymaj serwis, gdy aplikacja przechodzi do tła
        stopService(Intent(this, SensorService::class.java))
    }

    private fun listenForShakes() {
        lifecycleScope.launch {
            SensorService.shakeFlow.collectLatest {
                val amountToAdd = viewModel.sliderValue
                viewModel.addWater(amountToAdd)
                triggerVibration()
            }
        }
    }

    private fun triggerVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(200)
        }
    }

    private fun scheduleReminderWork() {
        lifecycleScope.launch {
            val frequency = settingsManager.notificationFrequencyFlow.first()
            val isEnabled = settingsManager.notificationsEnabledFlow.first()
            if (!isEnabled) return@launch

            val inputData = Data.Builder()
                .putLong("ENQUEUE_TIME", System.currentTimeMillis())
                .build()

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
    
    // ... (reszta kodu bez zmian)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AsystentNawodnieniaTheme {
        // Preview
    }
}

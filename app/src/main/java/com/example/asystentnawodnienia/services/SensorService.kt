package com.example.asystentnawodnienia.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val shakeDetector = ShakeDetector()

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private val _shakeFlow = MutableSharedFlow<Unit>()
        val shakeFlow = _shakeFlow.asSharedFlow()

        const val FOREGROUND_CHANNEL_ID = "sensor_service_channel"
        const val FOREGROUND_NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        // Uruchomienie serwisu jako pierwszoplanowego
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification())
    }

    private fun createForegroundNotification(): Notification {
        createForegroundNotificationChannel()
        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Asystent Nawodnienia")
            .setContentText("Nasłuchiwanie na gest potrząśnięcia jest aktywne.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Niski priorytet, aby nie przeszkadzać
            .build()
    }

    private fun createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Serwis w Tle",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            if (shakeDetector.detectShake(event)) {
                serviceScope.launch {
                    _shakeFlow.emit(Unit)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Ignored */ }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.coroutineContext.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private class ShakeDetector {
        private var lastX: Float = 0.0f
        private var lastY: Float = 0.0f
        private var lastZ: Float = 0.0f
        private var lastUpdateTime: Long = 0
        private var lastShakeTime: Long = 0

        companion object {
            private const val SHAKE_THRESHOLD = 800
            private const val MIN_TIME_BETWEEN_SAMPLES_MS = 100
            private const val SHAKE_COOLDOWN_MS = 2000 
        }

        fun detectShake(event: SensorEvent): Boolean {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - lastUpdateTime) > MIN_TIME_BETWEEN_SAMPLES_MS) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val speed = abs(x + y + z - lastX - lastY - lastZ) / (currentTime - lastUpdateTime) * 10000

                lastX = x
                lastY = y
                lastZ = z
                lastUpdateTime = currentTime

                if (speed > SHAKE_THRESHOLD && (currentTime - lastShakeTime) > SHAKE_COOLDOWN_MS) {
                    lastShakeTime = currentTime
                    return true
                }
            }
            return false
        }
    }
}

package com.example.asystentnawodnienia.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Serwis działający w tle, który nasłuchuje na potrząśnięcie telefonem.
 * Używa prywatnej klasy ShakeDetector do hermetyzacji logiki wykrywania gestu.
 */
class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private val shakeDetector = ShakeDetector() // 1. Stworzenie instancji detektora

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        // Kanał komunikacyjny, przez który serwis informuje aplikację o zdarzeniu.
        private val _shakeFlow = MutableSharedFlow<Unit>()
        val shakeFlow = _shakeFlow.asSharedFlow()
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // 2. Przekazanie zdarzenia do detektora i sprawdzenie wyniku.
            val isShakeDetected = shakeDetector.detectShake(event)

            if (isShakeDetected) {
                // 3. Jeśli wykryto potrząśnięcie, wyślij zdarzenie przez Flow.
                //    Aktywność (MainActivity) nasłuchuje na ten Flow i to ONA
                //    wywołuje metodę w ViewModelu.
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

    /**
     * Prywatna klasa pomocnicza do wykrywania gestu potrząśnięcia (shake).
     */
    private class ShakeDetector {
        private var lastX: Float = 0.0f
        private var lastY: Float = 0.0f
        private var lastZ: Float = 0.0f
        private var lastUpdateTime: Long = 0

        companion object {
            private const val SHAKE_THRESHOLD = 800
            private const val MIN_TIME_BETWEEN_SAMPLES_MS = 100
        }

        fun detectShake(event: SensorEvent): Boolean {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - lastUpdateTime

            if (timeSinceLastUpdate > MIN_TIME_BETWEEN_SAMPLES_MS) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val speed = abs(x + y + z - lastX - lastY - lastZ) / timeSinceLastUpdate * 10000

                lastX = x
                lastY = y
                lastZ = z
                lastUpdateTime = currentTime

                return speed > SHAKE_THRESHOLD
            }
            return false
        }
    }
}

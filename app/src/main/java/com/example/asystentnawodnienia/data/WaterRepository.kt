package com.example.asystentnawodnienia.data

import java.time.LocalDate

class WaterRepository(private val waterDao: WaterDao) {

    suspend fun addWater(waterIntake: WaterIntake) {
        waterDao.insert(waterIntake)
    }

    // Nowa funkcja do usuwania ostatniego wpisu
    suspend fun removeLastWater() {
        val today = LocalDate.now().toString()
        waterDao.deleteLastEntryForDay(today)
    }

    // Nowa funkcja do resetowania dnia
    suspend fun resetToday() {
        val today = LocalDate.now().toString()
        waterDao.deleteAllEntriesForDay(today)
    }

    suspend fun getTotalForDay(day: String): Int {
        return waterDao.getWaterIntakeForDay(day) ?: 0
    }

    suspend fun getHistory(): List<WaterIntake> {
        return waterDao.getAllWaterIntakeHistory()
    }
}

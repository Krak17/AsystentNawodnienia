package com.example.asystentnawodnienia.data

import java.time.LocalDate
// Warstwa pośrednia między ViewModel a bazą danych
class WaterRepository(private val waterDao: WaterDao) {
    // Dodaje nowy wpis o wodzie do bazy
    suspend fun addWater(waterIntake: WaterIntake) {
        waterDao.insert(waterIntake)
    }

    // Usuwa ostatni dodany wpis z dzisiejszego dnia
    suspend fun removeLastWater() {
        val today = LocalDate.now().toString()
        waterDao.deleteLastEntryForDay(today)
    }

    // Czyści wszystkie wpisy z dzisiejszego dnia
    suspend fun resetToday() {
        val today = LocalDate.now().toString()
        waterDao.deleteAllEntriesForDay(today)
    }
    // Zwraca sumę ml dla wybranego dnia
    suspend fun getTotalForDay(day: String): Int {
        return waterDao.getWaterIntakeForDay(day) ?: 0
    }
    // Zwraca pełną historię nawodnienia
    suspend fun getHistory(): List<WaterIntake> {
        return waterDao.getAllWaterIntakeHistory()
    }
}
package com.example.asystentnawodnienia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WaterDao {
    // Dodaje nowy wpis o wypitej wodzie
    @Insert
    suspend fun insert(waterIntake: WaterIntake)
    // Aktualizuje istniejący wpis
    @Update
    suspend fun update(waterIntake: WaterIntake)
    // Zwraca sumę ml wypitych w danym dniu
    @Query("SELECT SUM(amountMl) FROM water_intake WHERE date = :day")
    suspend fun getWaterIntakeForDay(day: String): Int?
    // Pobiera całą historię wpisów (od najnowszych)
    @Query("SELECT * FROM water_intake ORDER BY date DESC")
    suspend fun getAllWaterIntakeHistory(): List<WaterIntake>

    // Nowa metoda do usuwania ostatniego wpisu z danego dnia
    @Query("DELETE FROM water_intake WHERE id = (SELECT id FROM water_intake WHERE date = :day ORDER BY id DESC LIMIT 1)")
    suspend fun deleteLastEntryForDay(day: String)

    // Nowa metoda do usuwania wszystkich wpisÃ³w z danego dnia
    @Query("DELETE FROM water_intake WHERE date = :day")
    suspend fun deleteAllEntriesForDay(day: String)
}
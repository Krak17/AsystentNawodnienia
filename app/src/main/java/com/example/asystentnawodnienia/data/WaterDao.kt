package com.example.asystentnawodnienia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object (DAO) dla encji WaterIntake.
 * Ten interfejs definiuje wszystkie operacje, które można wykonać na tabeli water_intake.
 */
@Dao
interface WaterDao {

    /**
     * Wstawia nowy wpis o spożyciu wody do bazy danych.
     * Adnotacja @Insert automatycznie generuje potrzebny kod.
     */
    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    /**
     * Aktualizuje istniejący wpis w bazie danych.
     * Room znajduje odpowiedni wpis na podstawie klucza głównego.
     */
    @Update
    suspend fun update(waterIntake: WaterIntake)

    /**
     * Pobiera sumę spożytej wody (w ml) dla konkretnego dnia.
     * Zapytanie SQL jest zdefiniowane w adnotacji @Query.
     * Zwraca Int? (nullable), ponieważ suma może wynosić null, jeśli nie ma wpisów dla danego dnia.
     */
    @Query("SELECT SUM(amountMl) FROM water_intake WHERE date = :day")
    suspend fun getWaterIntakeForDay(day: String): Int?

    /**
     * Pobiera całą historię spożycia wody, posortowaną od najnowszej do najstarszej.
     * Zwraca listę obiektów WaterIntake.
     */
    @Query("SELECT * FROM water_intake ORDER BY date DESC")
    suspend fun getAllWaterIntakeHistory(): List<WaterIntake>
}

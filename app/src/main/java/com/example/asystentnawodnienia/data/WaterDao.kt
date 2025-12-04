package com.example.asystentnawodnienia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WaterDao {

    @Insert
    suspend fun insert(waterIntake: WaterIntake)

    @Update
    suspend fun update(waterIntake: WaterIntake)

    @Query("SELECT SUM(amountMl) FROM water_intake WHERE date = :day")
    suspend fun getWaterIntakeForDay(day: String): Int?

    @Query("SELECT * FROM water_intake ORDER BY date DESC")
    suspend fun getAllWaterIntakeHistory(): List<WaterIntake>

    // Metoda do usuwania ostatniego wpisu z danego dnia
    @Query("DELETE FROM water_intake WHERE id = (SELECT id FROM water_intake WHERE date = :day ORDER BY id DESC LIMIT 1)")
    suspend fun deleteLastEntryForDay(day: String)

    // Nowa metoda do usuwania wszystkich wpisów z danego dnia
    @Query("DELETE FROM water_intake WHERE date = :day")
    suspend fun deleteAllEntriesForDay(day: String)
}

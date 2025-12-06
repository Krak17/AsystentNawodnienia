package com.example.asystentnawodnienia.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// Pojedynczy wpisu o wypitej wodzie w bazie
@Entity(tableName = "water_intake")
data class WaterIntake(
    // Unikalne ID wpisu (nadawane automatycznie)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // Data wpisu (np. używana do liczenia dziennej sumy)
    val date: String,
    // Ilość wody w ml dla tego wpisu
    val amountMl: Int,

    // Czas dodania wpisu (pomaga ustalić kolejność)
    val timestamp: Long = System.currentTimeMillis(),
    // Flaga typu wpisu: dodanie wody
    val isAddition: Boolean = true
)
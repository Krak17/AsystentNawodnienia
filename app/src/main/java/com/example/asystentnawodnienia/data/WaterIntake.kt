package com.example.asystentnawodnienia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Ta klasa reprezentuje pojedynczy wpis o spożyciu wody w bazie danych.
 * Room użyje tej definicji do stworzenia tabeli.
 */
@Entity(tableName = "water_intake")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Unikalny identyfikator każdego wpisu
    val date: String,      // Data spożycia wody, np. "2025-12-01"
    val amountMl: Int      // Ilość wypitej wody w mililitrach
)

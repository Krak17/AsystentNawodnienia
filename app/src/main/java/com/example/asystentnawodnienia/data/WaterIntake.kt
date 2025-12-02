package com.example.asystentnawodnienia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intake")
data class WaterIntake(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,      
    val amountMl: Int,     

    // Nowe pola z wartościami domyślnymi
    val timestamp: Long = System.currentTimeMillis(),
    val isAddition: Boolean = true
)

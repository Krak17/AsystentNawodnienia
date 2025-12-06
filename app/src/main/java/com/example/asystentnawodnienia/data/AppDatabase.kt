package com.example.asystentnawodnienia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Główna klasa bazy danych dla aplikacji.
 * Wersja została podbita do 2 z powodu dodania nowych pól do tabeli WaterIntake.
 */
@Database(entities = [WaterIntake::class], version = 2) // <-- ZMIANA WERSJI
abstract class AppDatabase : RoomDatabase() {
    // DAO do operacji na tabeli WaterIntake (wstawianie, odczyt, itp.)
    abstract fun waterDao(): WaterDao

    companion object {
        // Zapewnia, że zmiana INSTANCE będzie widoczna między wątkami
        @Volatile
        private var INSTANCE: AppDatabase? = null
        // Zapewnia jedną wspólną bazę w całej aplikacji
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_database"
                )
                    .fallbackToDestructiveMigration() // <-- DODANA LINIA
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
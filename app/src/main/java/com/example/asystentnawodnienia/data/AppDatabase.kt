package com.example.asystentnawodnienia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Główna klasa bazy danych dla aplikacji.
 * Łączy w sobie wszystkie encje i DAO.
 */
@Database(entities = [WaterIntake::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Udostępnia DAO do operacji na tabeli water_intake.
     */
    abstract fun waterDao(): WaterDao

    /**
     * Companion object, aby zapewnić, że istnieje tylko jedna instancja bazy danych (wzorzec Singleton).
     */
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Zwróć istniejącą instancję lub stwórz nową w bezpieczny sposób
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_database"
                ).build()
                INSTANCE = instance
                // zwróć instancję
                instance
            }
        }
    }
}

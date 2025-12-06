package com.example.asystentnawodnienia.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.asystentnawodnienia.data.AppDatabase
import com.example.asystentnawodnienia.data.WaterRepository

/**
 * Fabryka do tworzenia instancji WaterViewModel.
 * Jest potrzebna, ponieważ WaterViewModel ma w konstruktorze zależność (WaterRepository),
 * której domyślny mechanizm tworzenia ViewModeli nie potrafi dostarczyć.
 *
 * @param context Kontekst aplikacji, niezbędny do utworzenia instancji bazy danych.
 */
// Tworzymy WaterViewModel z potrzebnymi zależnościami
class WaterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    /**
     * Tworzy nową instancję ViewModelu.
     *
     * @param modelClass Klasa ViewModelu do utworzenia.
     * @return Nowo utworzony ViewModel.
     * @throws IllegalArgumentException jeśli podana klasa nie jest WaterViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Sprawdź, czy żądana klasa to WaterViewModel
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            // Jeśli tak, stwórz całą hierarchię zależności:
            // 1. Pobierz instancję bazy danych
            val database = AppDatabase.getDatabase(context)
            // 2. Pobierz DAO z bazy danych
            val dao = database.waterDao()
            // 3. Stwórz repozytorium z DAO
            val repository = WaterRepository(dao)

            // 4. Stwórz i zwróć WaterViewModel z repozytorium
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository) as T
        }
        // Jeśli ktoś próbuje użyć tej fabryki do stworzenia innego ViewModelu, rzuć błąd.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
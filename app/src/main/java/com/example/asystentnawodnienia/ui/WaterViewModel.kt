package com.example.asystentnawodnienia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.asystentnawodnienia.data.WaterIntake
import com.example.asystentnawodnienia.data.WaterRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

// Trzymamy stan nawodnienia i udostępniamy go ekranom
class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    // Przechowuje aktualną sumę spożytej wody dla dzisiejszego dnia.
    var totalToday by mutableStateOf(0)
        private set

    // Przechowuje pełną historię spożycia wody.
    var history by mutableStateOf<List<WaterIntake>>(emptyList())
        private set

    // Przechowuje aktualnie wybraną wartość na suwaku.
    var sliderValue by mutableStateOf(200)
        private set

    // Wczytujemy dane na start
    init {
        refreshData()
    }

    // Dodaje nowy wpis o spożyciu wody.
    fun addWater(amount: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val waterIntake = WaterIntake(
                date = today,
                amountMl = amount,
                timestamp = System.currentTimeMillis(),
                isAddition = true
            )
            repository.addWater(waterIntake)
            refreshData()
        }
    }

    // Tworzy wpis o usunięciu wody (z ujemną wartością).
    fun removeWater(amount: Int) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val removalIntake = WaterIntake(
                date = today,
                amountMl = -amount,
                timestamp = System.currentTimeMillis(),
                isAddition = false
            )
            repository.addWater(removalIntake)
            refreshData()
        }
    }

    // Resetuje wszystkie dzisiejsze wpisy.
    fun resetToday() {
        viewModelScope.launch {
            repository.resetToday()
            refreshData()
        }
    }

    // Aktualizuje wartość suwaka na podstawie interakcji użytkownika w UI.
    fun updateSliderValue(newValue: Int) {
        sliderValue = newValue
    }

    // Odświeża wszystkie kluczowe dane (sumę i historię) z bazy danych.
    private fun refreshData() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            history = repository.getHistory()
            totalToday = history.filter { it.date == today }.sumOf { it.amountMl }
        }
    }

    // Zwraca listę wpisów tylko z dzisiejszego dnia.
    fun getTodayHistory(): List<WaterIntake> {
        val today = LocalDate.now().toString()
        return history.filter { it.date == today }.sortedByDescending { it.timestamp }
    }

    // Zwraca całą historię do analizy (np. dla wykresu tygodniowego).
    fun getWeeklySummary(): List<WaterIntake> {
        return history
    }
}
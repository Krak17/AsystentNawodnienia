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

class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    var totalToday by mutableStateOf(0)
        private set

    var history by mutableStateOf<List<WaterIntake>>(emptyList())
        private set

    var sliderValue by mutableStateOf(200)
        private set

    init {
        refreshData()
    }

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

    fun resetToday() {
        viewModelScope.launch {
            repository.resetToday()
            refreshData()
        }
    }

    fun updateSliderValue(newValue: Int) {
        sliderValue = newValue
    }

    private fun refreshData() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            history = repository.getHistory() // Pobierz całą historię
            totalToday = history.filter { it.date == today }.sumOf { it.amountMl }
        }
    }
    
    fun getTodayHistory(): List<WaterIntake> {
        val today = LocalDate.now().toString()
        return history.filter { it.date == today }.sortedByDescending { it.timestamp }
    }
    
    // ZMIANA: Funkcja zwraca teraz całą historię, a nie tylko 7 wpisów
    fun getWeeklySummary(): List<WaterIntake> {
        return history
    }
}
